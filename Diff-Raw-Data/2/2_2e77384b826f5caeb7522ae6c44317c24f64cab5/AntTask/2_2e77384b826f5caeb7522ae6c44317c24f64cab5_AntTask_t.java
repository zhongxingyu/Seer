 //////////////////////////////////////////////////////////////////////////////
 // Clirr: compares two versions of a java library for binary compatibility
 // Copyright (C) 2003 - 2004  Lars Khne
 //
 // This library is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public
 // License as published by the Free Software Foundation; either
 // version 2.1 of the License, or (at your option) any later version.
 //
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 // Lesser General Public License for more details.
 //
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 //////////////////////////////////////////////////////////////////////////////
 
 package net.sf.clirr.ant;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import net.sf.clirr.Checker;
 import net.sf.clirr.event.PlainDiffListener;
 import net.sf.clirr.event.XmlDiffListener;
 import net.sf.clirr.framework.CheckerException;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.DirectoryScanner;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.Task;
 import org.apache.tools.ant.types.FileSet;
 import org.apache.tools.ant.types.Path;
 
 
 /**
  * Implements the Clirr ant task.
  * @author lkuehne
  */
 public final class AntTask extends Task
 {
     private static final String FORMATTER_TYPE_PLAIN = "plain";
     private static final String FORMATTER_TYPE_XML = "xml";
 
     /**
      * Output formater.
      */
     public static final class Formatter
     {
         private String type = null;
         private String outFile = null;
 
         public String getOutFile()
         {
             return outFile;
         }
 
         public void setOutFile(String outFile)
         {
             this.outFile = outFile;
         }
 
         public String getType()
         {
             return type;
         }
 
         public void setType(String type)
         {
             String lowerCase = type.toLowerCase();
             if (!lowerCase.equals(FORMATTER_TYPE_XML) && !lowerCase.equals(FORMATTER_TYPE_PLAIN))
             {
                 throw new BuildException("Illegal formatter type, only plain and xml are supported");
             }
 
             this.type = type;
         }
     }
 
 
     private FileSet origFiles = null;
     private FileSet newFiles = null;
     private Path newClassPath = null;
     private Path origClassPath = null;
 
     private boolean failOnError = true;
     private boolean failOnWarning = false;
     private List formatters = new LinkedList();
 
 
     public Path createNewClassPath()
     {
         if (newClassPath == null)
         {
             newClassPath = new Path(getProject());
         }
         return newClassPath.createPath();
     }
 
     public void setNewClassPath(Path path)
     {
         if (newClassPath == null)
         {
             newClassPath = path;
         }
         else
         {
             newClassPath.append(path);
         }
     }
 
     public Path createOrigClassPath()
     {
         if (origClassPath == null)
         {
             origClassPath = new Path(getProject());
         }
         return origClassPath.createPath();
     }
 
     public void setOrigClassPath(Path path)
     {
         if (origClassPath == null)
         {
             origClassPath = path;
         }
         else
         {
             origClassPath.append(path);
         }
     }
 
     public void addOrigFiles(FileSet origFiles)
     {
         if (this.origFiles != null)
         {
             throw new BuildException();
         }
         this.origFiles = origFiles;
     }
 
     public void addNewFiles(FileSet newFiles)
     {
         if (this.newFiles != null)
         {
             throw new BuildException();
         }
         this.newFiles = newFiles;
     }
 
     public void setFailOnError(boolean failOnError)
     {
         this.failOnError = failOnError;
     }
 
     public void setFailOnWarning(boolean failOnWarning)
     {
         this.failOnWarning = failOnWarning;
     }
 
     public void addFormatter(Formatter formatter)
     {
         formatters.add(formatter);
     }
 
     public void execute()
     {
         log("Running Clirr, built from tag $Name$", Project.MSG_VERBOSE);
 
         if (origFiles == null || newFiles == null)
         {
             throw new BuildException("Missing nested filesets origFiles and newFiles.", getLocation());
         }
 
         if (newClassPath == null)
         {
             newClassPath = new Path(getProject());
         }
 
         if (origClassPath == null)
         {
             origClassPath = new Path(getProject());
         }
 
         final File[] origJars = scanFileSet(origFiles);
         final File[] newJars = scanFileSet(newFiles);
 
         if (origJars.length == 0)
         {
             throw new BuildException("No files in nested fileset origFiles - nothing to check!"
                     + " Please check your fileset specification.");
         }
 
         if (newJars.length == 0)
         {
             throw new BuildException("No files in nested fileset newFiles - nothing to check!"
                     + " Please check your fileset specification.");
         }
 
         final ClassLoader origThirdPartyLoader = createClasspathLoader(origClassPath);
         final ClassLoader newThirdPartyLoader = createClasspathLoader(newClassPath);
 
         final Checker checker = new Checker();
         final ChangeCounter counter = new ChangeCounter();
 
         boolean formattersWriteToStdOut = false;
 
         for (Iterator it = formatters.iterator(); it.hasNext();)
         {
             Formatter formatter = (Formatter) it.next();
             final String type = formatter.getType();
             final String outFile = formatter.getOutFile();
 
             formattersWriteToStdOut = formattersWriteToStdOut || outFile == null;
 
             try
             {
                 if (FORMATTER_TYPE_PLAIN.equals(type))
                 {
                     checker.addDiffListener(new PlainDiffListener(outFile));
                 }
                 else if (FORMATTER_TYPE_XML.equals(type))
                 {
                     checker.addDiffListener(new XmlDiffListener(outFile));
                 }
             }
             catch (IOException ex)
             {
                 log("unable to initialize formatter: " + ex.getMessage(), Project.MSG_WARN);
             }
         }
 
         if (!formattersWriteToStdOut)
         {
             checker.addDiffListener(new AntLogger(this));
         }
 
         checker.addDiffListener(counter);
         try
         {
             checker.reportDiffs(origJars, newJars, origThirdPartyLoader, newThirdPartyLoader, null);
         }
        catch (CheckerException ex)
         {
             throw new BuildException(ex.getMessage());
         }
 
         if (counter.getWarnings() > 0 && failOnWarning || counter.getErrors() > 0 && failOnError)
         {
             throw new BuildException("detected incompatible API changes");
         }
     }
 
 
     private ClassLoader createClasspathLoader(Path classpath)
     {
         final String[] cpEntries = classpath.list();
         final URL[] cpUrls = new URL[cpEntries.length];
         for (int i = 0; i < cpEntries.length; i++)
         {
             String cpEntry = cpEntries[i];
             File entry = new File(cpEntry);
             try
             {
                 URL url = entry.toURL();
                 cpUrls[i] = url;
             }
             catch (MalformedURLException ex)
             {
                 final IllegalArgumentException illegalArgEx = new IllegalArgumentException(
                         "Cannot create classLoader from classpath entry " + entry);
                 illegalArgEx.initCause(ex);
                 throw illegalArgEx;
             }
         }
         final URLClassLoader classPathLoader = new URLClassLoader(cpUrls);
         return classPathLoader;
     }
 
     private File[] scanFileSet(FileSet fs)
     {
         Project prj = getProject();
         DirectoryScanner scanner = fs.getDirectoryScanner(prj);
         scanner.scan();
         File basedir = scanner.getBasedir();
         String[] fileNames = scanner.getIncludedFiles();
         File[] ret = new File[fileNames.length];
         for (int i = 0; i < fileNames.length; i++)
         {
             String fileName = fileNames[i];
             ret[i] = new File(basedir, fileName);
         }
         return ret;
     }
 }
