 package jacaranda.frontend;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.charset.Charset;
 
 import latte.grammar.TreeBuilder;
 import latte.grammar.TypesMismatchException;
 //import latte.grammar.lattetree;
 
 import org.antlr.runtime.RecognitionException;
 import org.antlr.runtime.tree.CommonTree;
 //import org.antlr.runtime.tree.CommonTreeNodeStream;
 
 public class Jacaranda {
 
 	public static void main(String[] args) {
 		
 		String file_path = args[0];
 		
 		String file_data;
 		try {
 			file_data = readFile(file_path);
//			System.err.println(file_data);
 			
 			TreeBuilder builder = new TreeBuilder();
 			CommonTree tree = builder.buildTree(file_data);
 
//			System.err.println(tree.toStringTree());
 			builder.checkType(tree);
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (RecognitionException e) {
 			e.printStackTrace();
 		} catch (TypesMismatchException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private static String readFile(String path) throws IOException {
 		FileInputStream stream = new FileInputStream(new File(path));
 		try {
 			FileChannel fc = stream.getChannel();
 			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
 			/* Instead of using default, pass in a decoder. */
 			return Charset.defaultCharset().decode(bb).toString();
 		}
 		finally {
 			stream.close();
 		}
 	}
 	
 }
