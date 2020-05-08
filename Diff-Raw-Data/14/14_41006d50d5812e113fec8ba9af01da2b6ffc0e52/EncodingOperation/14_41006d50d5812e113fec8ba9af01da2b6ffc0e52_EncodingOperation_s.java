 package ua.com.zhanna.operation;
 
 import java.io.*;
 import java.nio.file.*;
 import java.nio.charset.*;
 
 public class EncodingOperation implements Operation {
     private Charset charset;
 
     public EncodingOperation() {
 	this(Charset.defaultCharset());
     }
     
     public EncodingOperation(String charset) {
 	this(Charset.forName(charset));
     }
 
     public EncodingOperation(Charset charset) {
 	this.charset = charset;
     }
 
     public Path operate(Path input) {
	/* create temp file, fill it, replace it with input, and finally return it. 
 	 * if some thing goes wrong return null
 	 */
 	try {
 	    Path temp = createTempFile();
 	    fillTempFile(temp, input);
	    replace(temp, input);
 	    return temp;
 	} catch (Exception e) { return null; }
     }
 
     private Path createTempFile() throws IOException {
 	return Files.createTempFile(null, ".zhanna");
     }
 
     private void fillTempFile(Path tmp, Path input) throws IOException {
 	try (BufferedReader reader = Files.newBufferedReader(input, charset);
 	     BufferedWriter writer = Files.newBufferedWriter(tmp, Charset.defaultCharset())) {
 		PrintWriter out = new PrintWriter(writer);
 		String line = null;
 		while ((line = reader.readLine()) != null)
 		    out.println(line);
 	    }
     }
 
    private void replace(Path temp, Path input) throws IOException {
	Files.delete(input);
	Files.move(temp, input);
    }

     /* TEST THIS */
     public static void main(String[] args) {
	new EncodingOperation("CP866").operate(Paths.get(args[0]));
     }
 }
 
 
