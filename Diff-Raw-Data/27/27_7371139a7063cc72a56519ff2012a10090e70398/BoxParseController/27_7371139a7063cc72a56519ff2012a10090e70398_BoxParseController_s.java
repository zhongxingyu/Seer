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
 
		parser.setMessageHandler(handler);
 		
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
		try {
			lexer = new BoxLexer(filePath.toOSString()); // Create the lexer
			parser = new BoxParser(lexer.getLexStream() /*, project*/); // Create the parser
		} catch (IOException e) {
			throw new Error(e);
		}
 	}
 
 	/**
 	 * setFilePath() should be called before calling this method.
 	 */
 	public Object parse(String contents, boolean scanOnly,
 			IProgressMonitor monitor) {
 		PMMonitor my_monitor = new PMMonitor(monitor);
 		char[] contentsArray = contents.toCharArray();
 
 		lexer.initialize(contentsArray, fFilePath.toPortableString());
 		parser.getParseStream().resetTokenStream();
 
		// SMS 28 Mar 2007
		// Commenting out to prevent clobbering of markers set by previous
		// builders in the same build phase.  This will also give behavior
		// that is more consistent with the handling of markers in the JDT.
		//        IResource file = project.getFile(filePath);
		//   	    try {
		//        	file.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
		//        } catch(CoreException e) {
		//        	System.err.println("BoxParseController.parse:  caught CoreException while deleting problem markers; continuing to parse regardless");
		//        }
 
 		lexer.lexer(my_monitor, parser.getParseStream()); // Lex the stream to produce the token stream
 		if (my_monitor.isCancelled())
 			return fCurrentAst; // TODO fCurrentAst might (probably will) be inconsistent wrt the lex stream now
 
 		fCurrentAst = (ASTNode) parser.parser(my_monitor, 0);
 //		parser.resolve((ASTNode) fCurrentAst);
 
 		cacheKeywordsOnce();
 
 		return fCurrentAst;
 	}
 
 	
 
 }
