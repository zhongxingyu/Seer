 package org.eclipse.imp.box.parser;
 
 import java.io.IOException;
 
 import lpg.runtime.IMessageHandler;
 
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.imp.model.ISourceProject;
 import org.eclipse.imp.parser.IASTNodeLocator;
 import org.eclipse.imp.parser.ILexer;
 import org.eclipse.imp.parser.IParseController;
 import org.eclipse.imp.parser.IParser;
 import org.eclipse.imp.parser.SimpleLPGParseController;
 
 import org.eclipse.imp.box.parser.Ast.ASTNode;
 
 /**
  * @author Stan Sutton (suttons@us.ibm.com)	(for the following modifications)
  * @since May 1,  2007	Addition of marker types
  * @since May 10, 2007	Conversion IProject -> ISourceProject
  * @since May 31, 2007  Adapted to extend SimpleLPGParseController
  */
 public class BoxParseController extends SimpleLPGParseController implements
 		IParseController {
 	private BoxParser parser;
 
 	private BoxLexer lexer;
 
 	private char keywords[][];
 
 	private boolean isKeyword[];
 
 	public void initialize(IPath filePath, ISourceProject project, IMessageHandler handler) {
 		super.initialize(filePath, project, handler);
 		
 		IPath pathToUse;
 		IPath projLoc = project.getRawProject().getLocation();
 		
 		if (!filePath.isAbsolute()) {
 			pathToUse = projLoc.append(filePath);
 		}
 		else {
 			pathToUse = filePath;
 		}
 		
 		createLexerAndParser(pathToUse);
 
		parser.getParseStream().setMessageHandler(handler);
		cacheKeywordsOnce();
 		
 	}
 
 	public IParser getParser() {
 		return parser;
 	}
 
 	public ILexer getLexer() {
 		return lexer;
 	}
 
 	public IASTNodeLocator getNodeLocator() {
 		return new BoxASTNodeLocator();
 	} //return new AstLocator(); }
 
 	public BoxParseController() {
 	}
 
 	private void createLexerAndParser(IPath filePath) {
			lexer = new BoxLexer(); // Create the lexer
			parser = new BoxParser(lexer); // Create the parser
 	}
 
 	/**
 	 * setFilePath() should be called before calling this method.
 	 */
 	public Object parse(String contents, boolean scanOnly,
 			IProgressMonitor monitor) {
 		PMMonitor my_monitor = new PMMonitor(monitor);
 		char[] contentsArray = contents.toCharArray();
 
		createLexerAndParser(fFilePath);  // TODO remove this sometime
 		lexer.initialize(contentsArray, fFilePath.toPortableString());
 		parser.getParseStream().resetTokenStream();
		parser.getParseStream().setMessageHandler(handler);
 
 
 		lexer.lexer(my_monitor, parser.getParseStream()); // Lex the stream to produce the token stream
 		if (my_monitor.isCancelled())
 			return fCurrentAst; // TODO fCurrentAst might (probably will) be inconsistent wrt the lex stream now
 
 		fCurrentAst = (ASTNode) parser.parser(my_monitor, 0);
 //		parser.resolve((ASTNode) fCurrentAst);
 
 		cacheKeywordsOnce();
 
 		return fCurrentAst;
 	}
 
 	
 
 }
