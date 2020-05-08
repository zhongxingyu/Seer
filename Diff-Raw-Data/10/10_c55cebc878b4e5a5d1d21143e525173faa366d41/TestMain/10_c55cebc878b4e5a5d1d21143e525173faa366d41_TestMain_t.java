 package bfrc;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 
 public class TestMain {
	public static void main(String[] args) throws Exception {
 		String src = ">+++++++++[<++++++++>-]<.>+++++++[<++++>-]<+.+++++++..+++.[-]>++++++++[<++++>-]<.>+++++++++++[<+++++>-]<.>++++++++[<+++>-]<.+++.------.--------.[-]>++++++++[<++++>-]<+.[-]++++++++++.";
 
 		File srcFile = createTempFile(src);
 
 		String[] backends = { "dot", "c", "java", "aot", "jit", "int", "ook-out", "bf-out" };
 		String[] params = { null, srcFile.getAbsolutePath(), null };
 
 		for (String config : backends) {
 			// set config
 			params[0] = "-" + config;
 			params[2] = "test." + config;
 			// run
			try {
				System.out.println("run with backend " + config);
				Brainfuck.main(params);
			} catch (Exception e) {
				new RuntimeException("backend " + config + " failed", e).printStackTrace();
			}
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
