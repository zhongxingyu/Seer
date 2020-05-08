 /*
  * Created on Nov 1, 2005
  */
 package org.jikespg.uide.editor;
 
 import org.eclipse.jface.text.contentassist.ICompletionProposal;
 import org.eclipse.uide.editor.IContentProposer;
 import org.eclipse.uide.parser.IASTNodeLocator;
 import org.eclipse.uide.parser.IParseController;
 import org.jikespg.uide.parser.JikesPGParser.ASTNode;
 import org.jikespg.uide.parser.JikesPGParser.rhsSymbol;
 import org.jikespg.uide.parser.JikesPGParser.rhsSymbolMacro;
 
 import com.ibm.lpg.IToken;
 import com.ibm.lpg.PrsStream;
 
 public class ContentProposer implements IContentProposer {
 
     public ICompletionProposal[] getContentProposals(IParseController controller, int offset) {
 	PrsStream parseStream= controller.getParser().getParseStream();
 	int thisTokIdx= parseStream.getTokenIndexAtCharacter(offset);
 	IToken prevTok= parseStream.getTokenAt(thisTokIdx - 1);
 	ASTNode currentAst= (ASTNode) controller.getCurrentAst();
 
 	IASTNodeLocator locator= controller.getNodeLocator();
 	ASTNode prevNode= null; // locator.findNode(currentAst, prevTok.getStartOffset());
 	ASTNode parentNode= null; // locator.findParent(currentAst, prevNode);
 
 	if (parentNode instanceof rhsSymbol) {
 	    rhsSymbol symbol= (rhsSymbol) parentNode;
 	    
 	} else if (parentNode instanceof rhsSymbolMacro) {
 	    rhsSymbolMacro macro= (rhsSymbolMacro) parentNode;
 	    
 	}
 	return null;
     }
 }
