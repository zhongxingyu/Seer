 package codemate.operator;
 
 import java.io.*;
 import java.util.*;
 
 import codemate.Fortran.*;
 import codemate.builder.*;
 import codemate.compilermate.*;
 import codemate.librarymate.*;
 import codemate.project.*;
 import codemate.ui.*;
 
 /**
  * Scan
  *
  * This class is a subcommand that scans the codes in a root directories. It
  * will conduct the following operations:
  *
  *   - Collect codes in the given root directory;
  *   - Create a project object for the collected codes;
  *   - Parse all the codes to construct parse tree;
  *   - Extract internal and external dependencies;
  *   - Process codes including rewriting and templating.
  *
  * @author      Li Dong <dongli@lasg.iap.ac.cn>
  */
 
 public class Scan {
 	public static void operate(List<String> arguments) {
 		String root = arguments.get(0);
 		
		File file = (new File(root)).getAbsoluteFile();
 		if (file.isDirectory()) {
 			UI.notice("Scan", "Scanning project in "+root+".");
 			LibraryMates.load();
 			CompilerMates.load();
 			Config.load();
			Project project = new Project(root);
 			FortranProcessor.process(project);
 			ProjectBuilder.prepare(project);
 		} else if (file.isFile()) {
			UI.notice("Scan", "Scanning single file "+root+".");
 			FortranProcessor.process(file);
 		}
 	}
 }
