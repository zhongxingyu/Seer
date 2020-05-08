 package codemate.builder;
 
 /**
  * MakefileWriter
  * 
  * This class writes a Makefile for building project, and it should be able run
  * standalone.
  * 
  * @author Li Dong <dongli@lasg.iap.ac.cn>
  */
 
 import java.io.*;
 import java.util.*;
 
 import codemate.project.*;
 import codemate.compilermate.*;
 import codemate.librarymate.*;
 import codemate.ui.*;
 
 public class MakefileWriter {
 	public static void write(Project project) {
 		// ---------------------------------------------------------------------
 		String content = "";
 		content +=
 				"# ------------------------------------------------------------------------------\n"+
 				"#                              MODIFIABLE SECTION\n"+
 				"#\n"+
 				"# ------------------------------------------------------------------------------\n"+
 				"# project, compiler and library configuration\n";
 		content += "PROJECT = "+project.getName()+"\n";
 		content += "PROJECT_ROOT = "+project.getRoot().getAbsolutePath()+"\n\n";
 		content += "FC = "+CompilerMates.getDefaultCommandName("Fortran")+"\n";
 		if (project.getBuildScheme().equals("debug"))
 			content += "FFLAGS = "+CompilerMates.getDefaultDebugOptions("Fortran")+"\n";
 		else if (project.getBuildScheme().equals("release"))
 			content += "FFLAGS = "+CompilerMates.getDefaultReleaseOptions("Fortran")+"\n";
 		content += "\n";
 		Set<LibraryMate> libraries = new HashSet<LibraryMate>();
 		for (CodeEntity entity : project.entities) {
 			if (entity.getType() == CodeEntity.Type.EXECUTABLE)
 				libraries.addAll(getAllExternalDepends(entity));
 		}
 		for (LibraryMate library : libraries) {
 			if (library.getRoot() != null)
 				content += library.getLibraryName().toUpperCase()+"_ROOT = "+
 						library.getRoot()+"\n";
 		}
 		content += "\n";
 		content +=
 			"# ------------------------------------------------------------------------------\n"+
 			"#                             DON'T TOUCH SECTION\n"+
 			"#\n"+
 			"# ------------------------------------------------------------------------------\n"+
 			"# objects, targets and libraries\n";
 		content += "VPATH =";
 		for (File dir : project.getDirectories())
 			content +=
 					" \\\n    $(PROJECT_ROOT)/"+
 					dir.getPath().replace(project.getRoot().getPath(), "");
 		content += " \\\n    $(PROJECT_ROOT)/.codemate/processed_codes";
 		content += "\n\n";
 		content += "OBJECTS =";
 		for (CodeEntity entity : project.entities)
 			if (entity.isProcessed(CodeEntity.Process.TEMPLATE))
 				content += append(content, " "+entity.getName()+".t.o");
 			else
 				content += append(content, " "+entity.getName()+".o");
 		content += "\n\n";
 		content += "EXE_TARGETS =";
 		for (CodeEntity entity : project.entities)
 			if (entity.getType() == CodeEntity.Type.EXECUTABLE)
 				content += append(content, " "+entity.getName());
 		content += "\n\n";
 		content +=
 				"# DO NOT MODIFY THEM, SET THEM WHEN INVOKING CODEMATE SCAN\n"+
 				"DEPEND_MACROS =";
 		for (String macro : project.addedMacros)
 			content += append(content, " -D"+macro);
 		content += "\n\n";
 		content += "all: start $(EXE_TARGETS) end\n\n";
 		for (CodeEntity entity : project.entities) {
 			if (entity.isProcessed(CodeEntity.Process.TEMPLATE))
 				content += entity.getName()+".t.o:";
 			else
 				content += entity.getName()+".o:";
 			for (CodeEntity depend : entity.internalDepends) {
 				if (depend.isProcessed(CodeEntity.Process.TEMPLATE))
 					content += append(content, " "+depend.getName()+".t.o");
 				else
 					content += append(content, " "+depend.getName()+".o");
 			}
 			content += "\n";
 		}
 		content += "\n.SECONDEXPANSION:\n\n";
 		for (CodeEntity entity : project.entities) {
 			if (entity.getType() == CodeEntity.Type.EXECUTABLE) {
 				content += "OBJECTS_"+entity.getName()+" =";
 				for (String depend : getAllInternalDepends(entity))
 					content += " "+depend;
 				if (entity.isProcessed(CodeEntity.Process.TEMPLATE))
 					content += " "+entity.getName()+".t.o";
 				else
 					content += " "+entity.getName()+".o";
 				content += "\n";
 			}
 		}
 		content += "\n";
 		content += "INCLUDES =";
 		for (LibraryMate libraryMate : libraries) {
 			content += "\\\n    "+libraryMate.getIncludeOptions();
 		}
 		content += "\n";
 		content += "LIBRARIES =";
 		for (LibraryMate libraryMate : libraries) {
 			content += "\\\n    "+
 					libraryMate.getLibraryOptions("Fortran");
 		}
 		content += "\n\n";
 		content +=
 			"# ------------------------------------------------------------------------------\n"+
 			"# implicit building rules\n"+
 			"define fortran_implicit_rules\n"+
 			"%.o: %.$(1)\n"+
 			"\t@echo \" Creating dependency $$@\"\n"+
 			"\t@echo $$(seperator)\n"+
 			"\t@TEMPLATE_PATTERN='.*\\.t\\.$(1)'; \\\n" + 
 			"\t if [[ $$$$(dirname $$<) == '.' && '$$<' =~ $$$$TEMPLATE_PATTERN ]]; then \\\n" + 
 			"\t     SRC=$(PROJECT_ROOT)/.codemate/processed_codes/$$<; \\\n" + 
 			"\t else \\\n" + 
 			"\t     SRC=$$<; \\\n" + 
 			"\t fi; \\\n"+
 			"\t $(FC) -c $$$$SRC $(OPTIONS) $(DEPEND_MACROS) $(FFLAGS) $(INCLUDES)\n"+
 			"%.t.F90: %.$(1)\n"+
 			"\t@echo \" Processing template $$@\"\n"+
 			"\t@echo $$(seperator)\n"+
			"\t@codemate -silence scan $$< 2> $(PROJECT_ROOT)/.codemate/processed_codes/$$@\n"+
 			"endef\n"+
 			"fortran_suffices := f90 F90\n"+
 			"$(foreach suffix, $(fortran_suffices), \\\n"+
 			"    $(eval $(call fortran_implicit_rules,$(suffix))))\n";
 		content += "\n";
 		content +=
 			"# ------------------------------------------------------------------------------\n"+
 			"# target building rules\n"+
 			"define target_build_rules\n"+
 			"$(1): $$$$(OBJECTS_$(1))\n"+
 			"\t@echo \" Creating target '$(1)'\"\n"+
 			"\t\t@if [ \"$(OPTIONS)\" != \"\" ]; then \\\n"+
 			"\t\t\techo \" Configurations:\"; \\\n"+
 			"\t\t\techo \"   $(OPTIONS)\"; \\\n"+
 			"\t\tfi\n"+
 			"\t@$$(FC) -o $(1) $$(OBJECTS_$(1)) \\\n"+
 			"\t\t$(OPTIONS) $$(FFLAGS) $(INCLUDES) $(LIBRARIES) && \\\n"+
 			"\t\techo \" ---> $(1) is created.\"\n"+
 			"\t@echo $$(seperator)\n"+
 			"endef\n"+
 			"$(foreach target, $(EXE_TARGETS), \\\n"+
 			"    $(eval $(call target_build_rules,$(target))))\n";
 		content += "\n";
 		content +=
 			"# ------------------------------------------------------------------------------\n"+
 			"# auxiliary rules\n"+
 			"seperator = \"-------------------------------------------------------------\"\n"+
 			"\n"+
 			".PHONY: clean start end\n"+
 			"clean:\n"+
 			"\t@echo $(seperator)\n"+
 			"\t@if ls *.o > /dev/null 2>&1; then rm *.o; fi\n"+
 			"\t@if ls *.mod > /dev/null 2>&1; then rm *.mod; fi\n"+
 			"\t@if ls *.i90 > /dev/null 2>&1; then rm *.i90; fi\n"+
 			"\t@if ls *.cmm > /dev/null 2>&1; then rm *.cmm; fi\n"+
 			"\t@for target in $(EXE_TARGETS); do \\\n"+
 			"\t\tif ls $$target > /dev/null 2>&1; then \\\n"+
 			"\t\t\trm $$target; \\\n"+
 			"\t\tfi; \\\n"+
 			"\tdone\n"+
 			"\t@echo \" Project has been cleaned.\"\n"+
 			"\t@echo $(seperator)\n"+
 			"\n"+
 			"start:\n"+
 			"\t@echo $(seperator)\n"+
 			"\t@if test \"$(PROJECT)\" = \"\"; then \\\n"+
 			"\t\techo \" Project powered by CodeMate!\"; \\\n"+
 			"\telse \\\n"+
 			"\t\techo \" Project: >>> $(PROJECT) <<<\"; \\\n"+
 			"\tfi\n"+
 			"\t@echo $(seperator)\n"+
 			"\n"+
 			"end:\n"+
 			"\t@echo \" Finished\"\n"+
 			"\t@echo $(seperator)\n";
 		// ---------------------------------------------------------------------
 		UI.notice("codemate", "Generate Makefile.");
 		PrintWriter writer = null;
 		try {
 			writer = new PrintWriter("Makefile");
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		writer.write(content);
 		writer.flush();
 	}
 	
 	private static Set<String> getAllInternalDepends(CodeEntity entity) {
 		Set<String> internalDepends = new HashSet<String>();
 		for (CodeEntity internalDepend : entity.internalDepends) {
 			String tmp;
 			if (internalDepend.isProcessed(CodeEntity.Process.TEMPLATE))
 				tmp = internalDepend.getName()+".t.o";
 			else
 				tmp = internalDepend.getName()+".o";
 			if (!internalDepends.contains(tmp))
 				internalDepends.add(tmp);
 			internalDepends.addAll(getAllInternalDepends(internalDepend));
 		}
 		return internalDepends;
 	}
 	
 	private static Set<LibraryMate> getAllExternalDepends(CodeEntity entity) {
 		Set<LibraryMate> externalDepends = new HashSet<LibraryMate>();
 		for (CodeEntity internalDepend : entity.internalDepends) {
 			for (LibraryMate externalDepend : internalDepend.externalDepends) {
 				if (!externalDepends.contains(externalDepend))
 					externalDepends.add(externalDepend);
 			}
 			externalDepends.addAll(getAllExternalDepends(internalDepend));
 		}
 		return externalDepends;
 	}
 	
 	private static String append(String a, String b) {
 		int loc = a.lastIndexOf("\n");
 		int lineWidth = loc == -1 ? a.length() : a.length()-loc;
 		if (lineWidth+b.length() < 80) {
 			return b;
 		} else {
 			return " \\\n    "+b;
 		}
 	}
 }
