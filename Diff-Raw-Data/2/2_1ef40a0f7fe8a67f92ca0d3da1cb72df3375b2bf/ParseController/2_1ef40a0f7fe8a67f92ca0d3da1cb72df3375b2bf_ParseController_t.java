 package $PACKAGE_NAME$;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.imp.services.ILanguageSyntaxProperties;
 import org.eclipse.imp.model.ISourceProject;
 import org.eclipse.imp.parser.ILexer;
 import org.eclipse.imp.parser.IMessageHandler;
 import org.eclipse.imp.parser.IParseController;
 import org.eclipse.imp.parser.IParser;
 import org.eclipse.imp.parser.ISourcePositionLocator;
 import org.eclipse.imp.parser.MessageHandlerAdapter;
 import org.eclipse.imp.parser.SimpleLPGParseController;
 
 import $PLUGIN_PACKAGE$.$PLUGIN_CLASS$;
 import $AST_PKG_NODE$;
 
 /**
  * The $LANG_NAME$ implementation of the IParseController IMP interface.
  */
 public class $CLASS_NAME_PREFIX$ParseController
     extends SimpleLPGParseController
 {
     public $CLASS_NAME_PREFIX$ParseController() {
     	super($PLUGIN_CLASS$.kLanguageID);
     }
 
 	public ILanguageSyntaxProperties getSyntaxProperties() {
 	    return null;
 	}
 
 	/**
 	 * setFilePath() should be called before calling this method.
 	 */
 	public Object parse(String contents, IProgressMonitor monitor) {
 		PMMonitor my_monitor = new PMMonitor(monitor);
 		char[] contentsArray = contents.toCharArray();
 
 		if (fLexer == null) {
 		  fLexer = new $CLASS_NAME_PREFIX$Lexer();
 		}
 		fLexer.reset(contentsArray, fFilePath.toPortableString());
 		
 		if (fParser == null) {
 			fParser = new $CLASS_NAME_PREFIX$Parser(fLexer.getILexStream());
 		}
 		fParser.reset(fLexer.getILexStream());
 		fParser.getIPrsStream().setMessageHandler(new MessageHandlerAdapter(handler));
 
 		fLexer.lexer(my_monitor, fParser.getIPrsStream()); // Lex the stream to produce the token stream
 		if (my_monitor.isCancelled())
 			return fCurrentAst; // TODO fCurrentAst might (probably will) be inconsistent wrt the lex stream now
 
 		fCurrentAst = fParser.parser(my_monitor, 0);
 
 		// SMS 18 Dec 2007:  functionDeclarationList is a LEG-specific type
 		// not suitable for other languages; try replacing with ASTNode
 //		if (fCurrentAst instanceof functionDeclarationList) {
 //			parser.resolve((ASTNode) fCurrentAst);
 //		}
 		if (fCurrentAst instanceof ASTNode) {
			(($CLASS_NAME_PREFIX$Parser) fParser).resolve((ASTNode) fCurrentAst);
 		}
 		
 		cacheKeywordsOnce();
 
 		return fCurrentAst;
 	}
 }
