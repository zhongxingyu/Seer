 package editor;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 
 import node.AModule;
 import node.AStdClassdef;
 import node.PPropdef;
 import node.Start;
 
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.ITextViewer;
 import org.eclipse.jface.text.contentassist.ICompletionProposal;
 import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
 import org.eclipse.jface.text.contentassist.IContextInformation;
 import org.eclipse.jface.text.contentassist.IContextInformationValidator;
 
 import asthelpers.AstParserHelper;
 
 /**
  * @author R4PaSs
  * 
  *         Class charged to provide auto completion for the NitEditor
  */
 public class EditorContentAssistProcessor implements IContentAssistProcessor {
 
 	private String lastError;
 
 	@Override
 	public ICompletionProposal[] computeCompletionProposals(
 			ITextViewer textViewer, int documentOffset) {
 
 		// Result sets
 		ArrayList<AStdClassdef> classesToPropose = new ArrayList<AStdClassdef>();
 		ArrayList<ICompletionProposal[]> methodsToPropose = new ArrayList<ICompletionProposal[]>();
 
 		String startsWith = "";
 		IDocument document = textViewer.getDocument();
 
 		StringBuffer startBuf = new StringBuffer();
 		StringBuffer endBuf = new StringBuffer();
 
 		WordProvider wp = new WordProvider();
 
 		try {
 			char tempChar = '\0';
			int currOffset = 0;
			if (documentOffset > 0)
				currOffset = documentOffset - 1;
			else
				currOffset = 0;
 
 			// Bufferize from currOffset to start delimiter
 			while (tempChar != '.' && tempChar != ' ' && tempChar != '\n'
 					&& tempChar != '\t' && tempChar != '\r' && currOffset >= 0) {
 				tempChar = document.getChar(currOffset);
 				startBuf.append(tempChar);
 				currOffset--;
 			}
 
 			// Bufferize from currOffset to end delimiter
 			currOffset = documentOffset;
 			tempChar = '\0';
 			while (tempChar != '.' && tempChar != ' ' && tempChar != '\n'
 					&& tempChar != '\t' && tempChar != '\r'
 					&& currOffset < document.getLength()) {
 				tempChar = document.getChar(currOffset);
 				endBuf.append(tempChar);
 				currOffset++;
 			}
 
			String startStr = "";
			if (startBuf.length() > 0)
				startStr = startBuf.reverse().toString().trim();

			String endStr = "";
			if (endBuf.length() > 0)
				endStr = endBuf.toString().trim();

			startsWith = startStr + endStr;
 		} catch (Exception e) {
 		}
 
 		AstParserHelper aph = new AstParserHelper();
 
 		Start st = null;
 
 		st = aph.getAstForDocument(document);
 
 		if (st != null) {
 
 			AModule mod = aph.getModuleOfAST(st);
 
 			if (mod != null) {
 				ArrayList<AStdClassdef> astdclass = aph.getClassesOfModule(mod);
 				classesToPropose.addAll(astdclass);
 				for (AStdClassdef claas : astdclass) {
 					LinkedList<PPropdef> props = aph.getPropsOfClass(claas);
 					methodsToPropose.add(wp.buildMethProposals(
 							aph.getConcreteMethsInPropList(props),
 							aph.getDeferredMethsInPropList(props),
 							documentOffset, startsWith));
 				}
 			}
 		}
 
 		ICompletionProposal[] classesSuggestions = wp.buildClassProposals(
 				classesToPropose, documentOffset, startsWith);
 
 		int totalLength = classesSuggestions.length;
 
 		ICompletionProposal[] keyWordsSuggestions = wp.buildKeywordsProposals(
 				documentOffset, startsWith);
 
 		totalLength += keyWordsSuggestions.length;
 
 		for (ICompletionProposal[] comps : methodsToPropose) {
 			totalLength += comps.length;
 		}
 
 		ICompletionProposal[] finalProposals = new ICompletionProposal[totalLength];
 
 		int currLength = 0;
 
 		if (keyWordsSuggestions.length > 0) {
 			System.arraycopy(keyWordsSuggestions, 0, finalProposals,
 					currLength, keyWordsSuggestions.length);
 			currLength += keyWordsSuggestions.length;
 		}
 
 		if (classesSuggestions.length > 0) {
 			System.arraycopy(classesSuggestions, 0, finalProposals, currLength,
 					classesSuggestions.length);
 			currLength += classesSuggestions.length;
 		}
 
 		for (ICompletionProposal[] comps : methodsToPropose) {
 			if (comps.length > 0) {
 				System.arraycopy(comps, 0, finalProposals, currLength,
 						comps.length);
 				currLength += comps.length;
 			}
 		}
 
 		return finalProposals;
 	}
 
 	@Override
 	public IContextInformation[] computeContextInformation(
 			ITextViewer itextviewer, int i) {
 		lastError = "No Context Information available";
 		return null;
 	}
 
 	@Override
 	public char[] getCompletionProposalAutoActivationCharacters() {
 		return null;
 	}
 
 	@Override
 	public char[] getContextInformationAutoActivationCharacters() {
 		return null;
 	}
 
 	@Override
 	public IContextInformationValidator getContextInformationValidator() {
 		return null;
 	}
 
 	@Override
 	public String getErrorMessage() {
 		return lastError;
 	}
 
 }
