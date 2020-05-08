 package com.aptana.rdt.internal.ui.text.correction;
 
 import java.util.Collection;
 
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.ISharedImages;
 import org.rubypeople.rdt.internal.ui.RubyPlugin;
 import org.rubypeople.rdt.ui.text.ruby.IInvocationContext;
 import org.rubypeople.rdt.ui.text.ruby.IProblemLocation;
 import org.rubypeople.rdt.ui.text.ruby.IRubyCompletionProposal;
 
 public class LocalCorrectionsSubProcessor {
 
 	public static void addUnusedMemberProposal(IInvocationContext context, IProblemLocation problem,  Collection<IRubyCompletionProposal> proposals) {
 		Image image= RubyPlugin.getDefault().getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_DELETE);
 		CorrectionProposal proposal = new CorrectionProposal("", problem.getOffset(), problem.getLength(), image, "clean up unused code", 100);
 		proposals.add(proposal);
 	}
 
	public static void addReplacementProposal(String replacement, String display, IProblemLocation problem, Collection<IRubyCompletionProposal> proposals) {
		Image image= RubyPlugin.getDefault().getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
		CorrectionProposal proposal = new CorrectionProposal(replacement, problem.getOffset(), problem.getLength(), image, display, 100);
		proposals.add(proposal);		
	}

 }
