 package com.aptana.rdt.internal.ui.text.correction;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 
 import org.eclipse.core.runtime.CoreException;
 import org.rubypeople.rdt.core.IRubyScript;
 import org.rubypeople.rdt.core.compiler.IProblem;
 import org.rubypeople.rdt.ui.text.ruby.IInvocationContext;
 import org.rubypeople.rdt.ui.text.ruby.IProblemLocation;
 import org.rubypeople.rdt.ui.text.ruby.IQuickFixProcessor;
 import org.rubypeople.rdt.ui.text.ruby.IRubyCompletionProposal;
 
import com.aptana.rdt.internal.parser.warnings.ConstantNamingConvention;
import com.aptana.rdt.internal.parser.warnings.MisspelledConstructorVisitor;

 public class QuickFixProcessor implements IQuickFixProcessor {
 
 	public IRubyCompletionProposal[] getCorrections(IInvocationContext context, IProblemLocation[] locations) throws CoreException {
 		if (locations == null || locations.length == 0) {
 			return null;
 		}
 
		HashSet<Integer> handledProblems = new HashSet<Integer>(locations.length);
 		ArrayList<IRubyCompletionProposal> resultingCollections = new ArrayList<IRubyCompletionProposal>();
 		for (int i = 0; i < locations.length; i++) {
 			IProblemLocation curr = locations[i];
 			Integer id = new Integer(curr.getProblemId());
 			if (handledProblems.add(id)) {
 				process(context, curr, resultingCollections);
 			}
 		}
 		return (IRubyCompletionProposal[]) resultingCollections.toArray(new IRubyCompletionProposal[resultingCollections.size()]);
 	}
 
 	private void process(IInvocationContext context, IProblemLocation problem, Collection<IRubyCompletionProposal> proposals) throws CoreException {
 		int id = problem.getProblemId();
 		if (id == 0) { // no proposals for none-problem locations
 			return;
 		}
 		switch (id) {
 		case IProblem.UnusedPrivateMethod:
 		case IProblem.UnusedPrivateField:
 		case IProblem.LocalVariableIsNeverUsed:
 		case IProblem.ArgumentIsNeverUsed:
 			LocalCorrectionsSubProcessor.addUnusedMemberProposal(context, problem, proposals);
 			break;
		case MisspelledConstructorVisitor.PROBLEM_ID:
			LocalCorrectionsSubProcessor.addReplacementProposal("initialize\n", "Rename to 'initialize'", problem, proposals);
			break;
		case ConstantNamingConvention.PROBLEM_ID:
			IRubyScript script = context.getRubyScript();
			String src = script.getSource();
			String constName = src.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
			LocalCorrectionsSubProcessor.addReplacementProposal(constName.toUpperCase(), "Convert to all uppercase", problem, proposals);
			break;
 		default:
 		}
 	}
 
 	public boolean hasCorrections(IRubyScript unit, int problemId) {
 		switch (problemId) {
 		case IProblem.UnusedPrivateMethod:
 		case IProblem.UnusedPrivateField:
 		case IProblem.LocalVariableIsNeverUsed:
 		case IProblem.ArgumentIsNeverUsed:
		case MisspelledConstructorVisitor.PROBLEM_ID:
		case ConstantNamingConvention.PROBLEM_ID:
 			return true;
 		default:
 			return false;
 		}
 	}
 }
