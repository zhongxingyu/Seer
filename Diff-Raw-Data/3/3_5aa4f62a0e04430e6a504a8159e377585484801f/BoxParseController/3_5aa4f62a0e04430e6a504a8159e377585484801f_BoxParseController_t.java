 package org.eclipse.imp.box.parser;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.imp.box.parser.Ast.ASTNode;
 import org.eclipse.imp.model.ISourceProject;
 import org.eclipse.imp.parser.ILexer;
 import org.eclipse.imp.parser.IMessageHandler;
 import org.eclipse.imp.parser.IParseController;
 import org.eclipse.imp.parser.IParser;
 import org.eclipse.imp.parser.ISourcePositionLocator;
 import org.eclipse.imp.parser.MessageHandlerAdapter;
 import org.eclipse.imp.parser.SimpleLPGParseController;
import org.eclipse.imp.services.ILanguageSyntaxProperties;
 
 public class BoxParseController extends SimpleLPGParseController implements
 		IParseController {
 	private BoxParser parser;
 
 	private BoxLexer lexer;
 
         public BoxParseController() { }
 
 	public void initialize(IPath filePath, ISourceProject project, IMessageHandler handler) {
 		super.initialize(filePath, project, handler);
 	}
 
 	@Override
 	public ILexer getLexer() {
 	    return lexer;
 	}
 
 	@Override
 	public IParser getParser() {
 	    return parser;
 	}
 
 	public Object parse(String contents, boolean scanOnly,
 			IProgressMonitor monitor) {
 		PMMonitor my_monitor = new PMMonitor(monitor);
 		char[] contentsArray = contents.toCharArray();
 
 		if (lexer == null) {
 			lexer = new BoxLexer(contentsArray, fFilePath.toOSString());
 		} else {
 		    lexer.reset(contentsArray, fFilePath.toOSString());
 		}
 		
 		if (parser == null) {
 			parser = new BoxParser(lexer.getLexStream());
 		} else {
 		    parser.reset(lexer.getLexStream());
 		}
 		
 		parser.getParseStream().setMessageHandler(new MessageHandlerAdapter(handler));
 
 		lexer.lexer(my_monitor, parser.getParseStream()); // Lex the stream to produce the token stream
 		if (my_monitor.isCancelled()) {
 			return fCurrentAst; 
 		}
 		
 		fCurrentAst = (ASTNode) parser.parser(my_monitor, 0);
 
 		return fCurrentAst;
 	}
 
 	public ISourcePositionLocator getNodeLocator() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public ILanguageSyntaxProperties getSyntaxProperties() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 }
