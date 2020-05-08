 package com.atlassian.maven.plugins.amps.osgi;
 
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.artifact.Artifact;
 import org.apache.commons.io.IOUtils;
 
 import java.util.*;
 import java.util.zip.ZipInputStream;
 import java.util.zip.ZipEntry;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 
 import aQute.libg.header.OSGiHeader;
 
 /**
  * Validates the package imports in a manifest contain proper versions
  *
  * @since 3.0
  */
 public class PackageImportVersionValidator
 {
     private final MavenProject project;
     private final Map<String,Set<String>> jarPackageCache = new HashMap<String,Set<String>>();
 
     public PackageImportVersionValidator(MavenProject project)
     {
         this.project = project;
     }
 
     /**
      * Validates the package imports.
      * @param imports The package imports from the manifest
      * @throws MojoFailureException If the validation fails.  Will contain user-friendly error message trying to guess
      * a desirable bnd configuration for package imports.
      */
     public void validate(String imports) throws MojoFailureException
     {
         if (imports != null)
         {
             Map<String,String> unknownPackages = new HashMap<String,String>();
 
             Map<String,Map<String,String>> pkgImports = OSGiHeader.parseHeader(imports);
             for (Map.Entry<String,Map<String,String>> pkgImport : pkgImports.entrySet())
             {
                 String pkg = pkgImport.getKey();
                 if (pkgImport.getValue() != null && pkgImport.getValue().size() > 0)
                 {
                     Map<String,String> props = pkgImport.getValue();
                     String version = props.get("version");
                     if (version == null || version.length() == 0)
                     {
                         unknownPackages.put(pkg, guessVersion( pkg));
                     }
                 }
                 else
                 {
                     unknownPackages.put(pkg, guessVersion(pkg));
                 }
             }
 
             if (!unknownPackages.isEmpty())
             {
                 StringBuilder sb = new StringBuilder("Manifest must contain versions for all imports.  Suggested changes:\n");
                 for (Map.Entry<String,String> entry : compressPackages(unknownPackages).entrySet())
                 {
                     sb.append(entry.getKey()).append(";version=\"").append(entry.getValue()).append("\",\n");
                 }
                 throw new MojoFailureException(sb.substring(0, sb.length() - 2));
             }
         }
     }
 
     /**
      * Compress packages into sets of wildcard expressions, where applicable.
      *
      * @param unknownPackages The raw set of packages and versions
      * @return A map of import package pattern and version
      */
     static Map<String,String> compressPackages(Map<String, String> unknownPackages)
     {
         Map<String,String> pkgs = new HashMap<String,String>();
        Set<String> unmatchedPackages = new HashSet<String>(unknownPackages.keySet());
        for (String pkg : unknownPackages.keySet())
         {
             if (!unmatchedPackages.contains(pkg))
             {
                 continue;
             }
             String version = unknownPackages.get(pkg);
            Set<String> others = new HashSet<String>(unmatchedPackages);
             others.remove(pkg);
             for (int curpos = 0; curpos<pkg.length(); curpos++)
             {
                 char curchar = pkg.charAt(curpos);
                 boolean sameVersion = true;
                 for (Iterator<String> i = others.iterator(); i.hasNext(); )
                 {
                     String other = i.next();
 
                     if (otherMatchesNextChar(curpos, curchar, other))
                     {
                         i.remove();
                     }
                     else if (!unknownPackages.get(other).equals(version))
                     {
                         sameVersion = false;
                         break;
                     }
                 }
                 if (curpos == pkg.length() -1 || sameVersion)
                 {
                     if (others.size() > 0 && sameVersion)
                     {
                         StringBuilder pattern = greedlyBuildPattern(pkg, others, curpos);
                         pkgs.put(pattern + "*", version);
                     }
                     else
                     {
                         pkgs.put(pkg, version);
                     }
                     unmatchedPackages.remove(pkg);
                     if (sameVersion)
                     {
                         unmatchedPackages.removeAll(others);
                     }
                     break;
                 }
             }
         }
         return pkgs;
     }
 
     private static boolean otherMatchesNextChar(int curpos, char curchar, String other)
     {
         return other.length() <= curpos || curchar != other.charAt(curpos);
     }
 
     /**
      * Tries to consume characters to build the longest possible match string
      * @param pkg The original package
      * @param others The other matching packages
      * @param curpos The minimal position for a match
      * @return A pattern containing the maximum amount of characters to still match the pattern
      */
     private static StringBuilder greedlyBuildPattern(String pkg, Set<String> others, int curpos)
     {
         StringBuilder pattern = new StringBuilder(pkg.substring(0, curpos + 1));
         for (int greedyPos = curpos + 1; greedyPos < pkg.length(); greedyPos++)
         {
             boolean canConsumeAnotherChar = true;
             for (String greedyOther : others)
             {
                 if (otherMatchesNextChar(greedyPos, pkg.charAt(greedyPos), greedyOther))
                 {
                     canConsumeAnotherChar = false;
                     break;
                 }
             }
             if (canConsumeAnotherChar)
             {
                 pattern.append(pkg.charAt(greedyPos));
             }
         }
         return pattern;
     }
 
     /**
      * Guesses the version for the package by scanning the Maven configuration.
      *
      * @param pkg The package to guess
      * @return The guessed version, or [0.0.0,) if unknown
      */
     private String guessVersion(String pkg)
     {
         for (Artifact artifact : new HashSet<Artifact>(project.getArtifacts()))
         {
 
             File file = artifact.getFile();
             if (file.exists() && file.getName().endsWith(".jar"))
             {
                 Set<String> contents = jarPackageCache.get(file.getAbsolutePath());
                 if (contents == null)
                 {
                     contents = new HashSet<String>();
                     jarPackageCache.put(file.getAbsolutePath(), contents);
                     ZipInputStream in = null;
                     try
                     {
                         in = new ZipInputStream(new FileInputStream(file));
                         ZipEntry entry;
                         while ((entry = in.getNextEntry()) != null)
                         {
                             contents.add(entry.getName());
                         }
                     }
                     catch (IOException e)
                     {
                         // ignore
                     }
                     finally
                     {
                         IOUtils.closeQuietly(in);
                     }
                 }
 
                 if (contents.contains(pkg.replace('.','/') + "/"))
                 {
                     return artifact.getVersion();
                 }
             }
         }
         return "0.0.0";
     }
 }
