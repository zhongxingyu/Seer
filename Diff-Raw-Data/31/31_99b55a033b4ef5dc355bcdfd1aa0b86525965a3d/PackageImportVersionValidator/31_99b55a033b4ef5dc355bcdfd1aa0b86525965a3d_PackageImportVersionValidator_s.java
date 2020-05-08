 package com.atlassian.maven.plugins.amps.osgi;
 
 import org.apache.maven.plugin.logging.Log;
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
     private final Log log;
     private final Map<String,Set<String>> jarPackageCache = new HashMap<String,Set<String>>();
     private static final int MIN_PACKAGES_FOR_WILDCARD = 4;
 
    public PackageImportVersionValidator(MavenProject project, Log log)
     {
         this.project = project;
         this.log = log;
     }
 
     /**
      * Validates the package imports.
      * @param imports The package imports from the manifest
      * @throws MojoFailureException If the validation fails.  Will contain user-friendly error message trying to guess
      * a desirable bnd configuration for package imports.
      */
     public void validate(String imports)
     {
         if (imports != null)
         {
             Map<String,String> foundPackages = new HashMap<String,String>();
             boolean validationFailed = false;
 
             Map<String,Map<String,String>> pkgImports = OSGiHeader.parseHeader(imports);
             for (Map.Entry<String,Map<String,String>> pkgImport : pkgImports.entrySet())
             {
                 String pkg = pkgImport.getKey();
                 if (pkgImport.getValue() != null && pkgImport.getValue().size() > 0)
                 {
                     Map<String,String> props = pkgImport.getValue();
                     String version = props.get("version");
                     foundPackages.put(pkg, guessVersion( pkg));
                     if (version == null || version.length() == 0)
                     {
                         validationFailed = true;
                     }
                 }
                 else
                 {
                     validationFailed = true;
                     foundPackages.put(pkg, guessVersion(pkg));
                 }
             }
 
             if (validationFailed)
             {
                 StringBuilder sb = new StringBuilder();
                 sb.append("The manifest should contain versions for all imports to prevent ambiguity at install time ");
                sb.append("due to multiple versions of a package.  Here are some suggestions generated for this project ");
                 sb.append("to start from:\n ");
                 sb.append("  <configuration>\n");
                 sb.append("    <instructions>\n");
                 sb.append("      <Import-Package>\n");
                 for (Map.Entry<String,String> entry : compressPackages(foundPackages).entrySet())
                 {
                     sb.append("        ").append(entry.getKey()).append(";version=\"").append(entry.getValue()).append("\",\n");
                 }
                 sb.delete(sb.length() - 2, sb.length());
                 sb.append("\n");
                 sb.append("      </Import-Package>\n");
                 sb.append("    </instructions>\n");
                 sb.append("  </configuration>\n");
                 sb.append("You may notice many packages you weren't expecting.  This is usually because of a bundled jar ");
                 sb.append("that references packages that don't apply.  You can usually remove these or if necessary, ");
                 sb.append("mark them as optional by adding ';resolution:=optional' to the package import.  Packages ");
                 sb.append("that are detected as version '0.0.0' usually mean either they are JDK packages or ones that ");
                 sb.append("aren't referenced in your project, and therefore, likely candidates for removal entirely.");
                 log.warn(sb.toString());
             }
         }
     }
 
     /**
      * Compress packages into sets of wildcard expressions, where applicable.
      *
      * @param allPackages The raw set of packages and versions
      * @return A map of import package pattern and version
      */
     static Map<String,String> compressPackages(Map<String, String> allPackages)
     {
         Map<String,String> pkgs = new HashMap<String,String>();
         Set<String> unmatchedPackages = new TreeSet<String>(allPackages.keySet());
 
         // Iterate through all packages to compress
         for (String pkg : new TreeSet<String>(allPackages.keySet()))
         {
             // only process unmatched packages
             if (!unmatchedPackages.contains(pkg))
             {
                 continue;
             }
             String version = allPackages.get(pkg);
 
             // Create set of all other unmatched patches
             Set<String> others = new TreeSet<String>(unmatchedPackages);
             others.remove(pkg);
 
             // Build list of packages with the same version
             for (Iterator<String> i = others.iterator(); i.hasNext(); )
             {
                 String otherPkg = i.next();
                 if (!allPackages.get(otherPkg).equals(version))
                 {
                     i.remove();
                 }
             }
 
 
             // Iterate through characters in the current package, looking for packages with matching characters and a
             // minimum of 3 packages
             int numberOfPackages = 1;
             for (int curpos = 0; curpos<pkg.length(); curpos++)
             {
                 char curchar = pkg.charAt(curpos);
                 if (curchar == '.')
                 {
                     numberOfPackages++;
                 }
 
                 for (Iterator<String> i = others.iterator(); i.hasNext(); )
                 {
                     String otherPkg = i.next();
 
                     // Remove other package if the character at the same index is different
                     if (otherNotMatchesNextChar(curpos, curchar, otherPkg))
                     {
                         i.remove();
                     }
                 }
 
                 if (numberOfPackages == MIN_PACKAGES_FOR_WILDCARD || curpos == pkg.length() - 1)
                 {
                     if (others.size() > 0 && numberOfPackages == MIN_PACKAGES_FOR_WILDCARD)
                     {
                         String pattern = greedlyBuildPattern(pkg, others, curpos).toString();
                         pkgs.put(pattern + "*", version);
                         unmatchedPackages.removeAll(others);
                     }
                     else
                     {
                         // No wildcard possible
                         pkgs.put(pkg, version);
                     }
                     unmatchedPackages.remove(pkg);
                     break;
                 }
             }
         }
         return pkgs;
     }
 
     private static boolean otherNotMatchesNextChar(int curpos, char curchar, String other)
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
         boolean canConsumeAnotherChar = true;
         for (int greedyPos = curpos + 1; greedyPos < pkg.length() && canConsumeAnotherChar; greedyPos++)
         {
             for (String greedyOther : others)
             {
                 canConsumeAnotherChar = true;
                 if (otherNotMatchesNextChar(greedyPos, pkg.charAt(greedyPos), greedyOther))
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
