 package ru.chikuyonok.wtp.sugar;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.ITextViewer;
 import org.eclipse.jface.text.contentassist.ICompletionProposal;
 import org.eclipse.jface.text.contentassist.IContextInformation;
 import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
 import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
 import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList;
 import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
 import org.eclipse.wst.sse.ui.contentassist.ICompletionProposalComputer;
 import org.eclipse.wst.sse.ui.internal.contentassist.ContentAssistUtils;
 
 import ru.chikuyonok.wtp.sugar.provider.FileListProvider;
 
 @SuppressWarnings("restriction")
 public class CSSCompletionProposalComputer implements
 		ICompletionProposalComputer {
 	
 	private static final ICompletionProposal[] NO_PROPOSALS= new ICompletionProposal[0];
 	private static final IContextInformation[] NO_CONTEXTS= new IContextInformation[0];
 	private static final String URI_DECLARATION_TOKEN = "DECLARATION_VALUE_URI";
 	private static final String URI_TOKEN = "URI";
 
 	@Override
 	public void sessionStarted() {
 		
 	}
 
 	@Override
 	public List<ICompletionProposal> computeCompletionProposals(
 			CompletionProposalInvocationContext context,
 			IProgressMonitor monitor) {
 		
 		ITextRegion curRegion = getRegionUnderCursor(context);
 		if (isURIToken(curRegion)) {
 			return computeURLCompletionProposals(context, monitor);
 		}
 		
 		
 		return Arrays.asList(NO_PROPOSALS);
 	}
 	
 	private boolean isURIToken(ITextRegion region) {
 		if (region == null)
 			return false;
 		
 		String type = region.getType();
 		return type.equals(URI_DECLARATION_TOKEN) || type.equals(URI_TOKEN);
 	}
 	
 	public List<ICompletionProposal> computeURLCompletionProposals(
 			CompletionProposalInvocationContext context,
 			IProgressMonitor monitor) {
 		
 		int pos = context.getInvocationOffset();
 		ITextViewer viewer = context.getViewer();
 		IStructuredDocumentRegion region = ContentAssistUtils.getStructuredDocumentRegion(viewer, pos);
 		ITextRegion curRegion = getRegionUnderCursor(context);
 		String content = null;
 		int regionStart = region.getStartOffset();
 		ICompletionProposal[] proposals = NO_PROPOSALS;
 		
 		try {
 			content = context.getDocument().get(regionStart + curRegion.getStart(), curRegion.getLength());
 		} catch (BadLocationException e) {}
 		
 		if (content != null) {
			content = content.trim();
			
 			// get prefix
 			int valuePos = 4; // add 'url(' length
 			if (content.startsWith("\"", 4) || content.startsWith("'", 4)) {
 				valuePos += 1;
 			}
 			
 			int valueLen = content.length() - valuePos;
 			if (content.endsWith("\")") || content.endsWith("')"))
 				valueLen -= 2;
 			else if (content.endsWith(")"))
 				valueLen -= 1;
 			
 			// check if cursor inside value bounds
 			int valueAbsPos = regionStart + curRegion.getStart() + valuePos;
 			if (pos >= valueAbsPos && pos <= valueAbsPos + valueLen) {
 				String prefix = content.substring(valuePos, valuePos + pos - valueAbsPos);
 				proposals = FileListProvider.getSingleton()
 						.getFileCompletionProposals(
 								FileListProvider.allPattern, prefix,
 								valueAbsPos, valueLen);
 			}
 		}
 		
 		return Arrays.asList(proposals);
 	}
 
 	@Override
 	public List<IContextInformation> computeContextInformation(
 			CompletionProposalInvocationContext context,
 			IProgressMonitor monitor) {
 		return Arrays.asList(NO_CONTEXTS);
 	}
 
 	@Override
 	public String getErrorMessage() {
 		return null;
 	}
 
 	@Override
 	public void sessionEnded() {
 		FileListProvider.getSingleton().clearState();
 	}
 	
 	public ITextRegion getRegionUnderCursor(CompletionProposalInvocationContext context) {
 		int pos = context.getInvocationOffset();
 		ITextViewer viewer = context.getViewer();
 		
 		IStructuredDocumentRegion region = ContentAssistUtils.getStructuredDocumentRegion(viewer, pos);
 		ITextRegionList regions = region.getRegions();
 		
 		int regionStart = region.getStart();
 		
 		for (int i = 0; i < regions.size(); i++) {
 			ITextRegion r = regions.get(i);
 			if (regionStart + r.getStart() <= pos && regionStart + r.getEnd() >= pos) {
 				return r;
 			}
 		}
 		
 		return null;
 	}
 }
