 package bfrc;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 
 public class TestMain {
	public static void main(String[] args) throws IOException {
 		String src = ">+++++++++[<++++++++>-]<.>+++++++[<++++>-]<+.+++++++..+++.[-]>++++++++[<++++>-]<.>+++++++++++[<+++++>-]<.>++++++++[<+++>-]<.+++.------.--------.[-]>++++++++[<++++>-]<+.[-]++++++++++.";
 
 		File srcFile = createTempFile(src);
 
 		String[] backends = { "dot", "c", "java", "aot", "jit", "int", "ook-out", "bf-out" };
 		String[] params = { null, srcFile.getAbsolutePath(), null };
 
 		for (String config : backends) {
 			// set config
 			params[0] = "-" + config;
 			params[2] = "test." + config;
 			// run
			Brainfuck.main(params);
 		}
 	}
 
 	private static File createTempFile(String content) throws IOException {
 		File f = File.createTempFile("bfrc", null);
 		f.deleteOnExit();
 		Writer w = new FileWriter(f);
 		w.write(content);
 		w.close();
 		f.setReadOnly();
 		return f;
 	}
 }
