 import org.python.core.*;
 import org.python.util.PythonInterpreter;
 import java.io.File;
 
 class PythonHandler {
 	PythonInterpreter interpreter;
 	
 	public PythonHandler() {
 		interpreter = new PythonInterpreter();
 	}
 	
 	public void invokeParser(String vcfFilePath) {
 		if (!fileExists(vcfFilePath)) {
			System.out.println("Fatal Error: file does not exist");
 			return;
 		}
 		interpreter.exec("import sys");
 		//interpreter.execfile("/home/git-vcf/src/VcfParser/loadvcf");
 	}
 	
 	private boolean fileExists(String filePath) {
 		File f = new File(filePath);
 		return f.exists();
 	}
 	
 }
