 package descent.internal.ui.text.correction;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 
 import org.eclipse.core.runtime.CoreException;
 
 import descent.core.IBuffer;
 import descent.core.ICompilationUnit;
 import descent.core.JavaModelException;
 import descent.core.compiler.IProblem;
 import descent.ui.text.java.IInvocationContext;
 import descent.ui.text.java.IJavaCompletionProposal;
 import descent.ui.text.java.IProblemLocation;
 import descent.ui.text.java.IQuickFixProcessor;
 
 /**
   */
 public class QuickFixProcessor implements IQuickFixProcessor {
 
 
 	public boolean hasCorrections(ICompilationUnit cu, int problemId) {
 		switch (problemId) {
 			case IProblem.UnterminatedStringConstant:
			case IProblem.CStyleCastIllegal:
 				return true;
 			default:
 				return false;
 		}
 	}
 
 	private static int moveBack(int offset, int start, String ignoreCharacters, ICompilationUnit cu) {
 		try {
 			IBuffer buf= cu.getBuffer();
 			while (offset >= start) {
 				if (ignoreCharacters.indexOf(buf.getChar(offset - 1)) == -1) { 
 					return offset;
 				}
 				offset--;
 			}
 		} catch(JavaModelException e) {
 		}
 		return start;
 	}
 
 
 	/* (non-Javadoc)
 	 * @see IAssistProcessor#getCorrections(descent.internal.ui.text.correction.IAssistContext, descent.internal.ui.text.correction.IProblemLocation[])
 	 */
 	public IJavaCompletionProposal[] getCorrections(IInvocationContext context, IProblemLocation[] locations) throws CoreException {
 		if (locations == null || locations.length == 0) {
 			return null;
 		}
 
 		HashSet handledProblems= new HashSet(locations.length);
 		ArrayList resultingCollections= new ArrayList();
 		for (int i= 0; i < locations.length; i++) {
 			IProblemLocation curr= locations[i];
 			Integer id= new Integer(curr.getProblemId());
 			if (handledProblems.add(id)) {
 				process(context, curr, resultingCollections);
 			}
 		}
 		return (IJavaCompletionProposal[]) resultingCollections.toArray(new IJavaCompletionProposal[resultingCollections.size()]);
 	}
 
 	private void process(IInvocationContext context, IProblemLocation problem, Collection proposals) throws CoreException {
 		int id= problem.getProblemId();
 		if (id == 0) { // no proposals for none-problem locations
 			return;
 		}
 		switch (id) {
 			case IProblem.UnterminatedStringConstant:
 				String quoteLabel= CorrectionMessages.JavaCorrectionProcessor_addquote_description;
 				int pos= moveBack(problem.getOffset() + problem.getLength(), problem.getOffset(), "\n\r", context.getCompilationUnit()); //$NON-NLS-1$
 				proposals.add(new ReplaceCorrectionProposal(quoteLabel, context.getCompilationUnit(), pos, 0, "\"", 0)); //$NON-NLS-1$
 				break;
			case IProblem.CStyleCastIllegal:
				String castLabel= CorrectionMessages.JavaCorrectionProcessor_addcast_description;
				pos= problem.getOffset();
				proposals.add(new ReplaceCorrectionProposal(castLabel, context.getCompilationUnit(), pos, 0, "cast", 0)); //$NON-NLS-1$
				break;
 			default:
 		}
 	}
 }
