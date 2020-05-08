 package jp.ac.osaka_u.ist.sdl.ectec.ast;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jdt.core.dom.AST;
 import org.eclipse.jdt.core.dom.ASTParser;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 
 /**
  * A class to create ASTs from the given source files
  * 
  * @author k-hotta
  * 
  */
 public class ASTCreator {
 
 	public static CompilationUnit createAST(final String sourceCode) {
 		final ASTParser parser = ASTParser.newParser(AST.JLS4);
 
 		parser.setSource(sourceCode.toCharArray());
 
 		return (CompilationUnit) parser.createAST(new NullProgressMonitor());
 	}
 
	public static CompilationUnit createAST(final File file) {
 		BufferedReader reader = null;
 		
 		try {
 			reader = new BufferedReader(new FileReader(file));
 			
 			final StringBuilder builder = new StringBuilder();
 			String line;
 			
 			while ((line = reader.readLine()) != null) {
 				builder.append(line + "\n");
 			}
 			
 			return createAST(builder.toString());
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		} finally {
 			if (reader != null) {
 				try {
 					reader.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 }
