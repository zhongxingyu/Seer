 package org.jbehave.ant;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.tools.ant.DirectoryScanner;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.types.FileSet;
 
 public class TrimFilesetParser implements FilesetParser {
 
     public String[] getClassNames(FileSet fileset, Project project) {
         DirectoryScanner ds = fileset.getDirectoryScanner(project);
         String[] includedFiles = ds.getIncludedFiles();
         List classnames = new ArrayList();
         for (int i = 0; i < includedFiles.length; i++) {
             String filename = includedFiles[i].replace('\\', '/');
 
             File base = ds.getBasedir();
             File found = new File(base, filename);
 
             String classname = found.getAbsolutePath().substring(
                     base.getAbsolutePath().length() + 1).replace('\\', '/')
                     .replace('/', '.');
 
            classname = stripJavaOrClassFromEndOfFile(classname);
             classnames.add(classname);
         }
         return (String[]) classnames.toArray(new String[classnames.size()]);
     }
 
	private String stripJavaOrClassFromEndOfFile(String classname) {
		if (classname.length() > 6 && classname.substring(classname.length() - 6, classname.length()).equals(".class")) {
		    classname = classname.substring(0, classname.length() - 6);
		} else if (classname.length() > 5 && classname.substring(classname.length() - 5, classname.length()).equals(".java")) {
		    classname = classname.substring(0, classname.length() -5);
		}
		return classname;
	}

 }
