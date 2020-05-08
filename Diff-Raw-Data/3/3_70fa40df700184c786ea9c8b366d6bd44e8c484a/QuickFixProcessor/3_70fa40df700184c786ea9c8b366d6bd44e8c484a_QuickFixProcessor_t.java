 package com.aptana.rdt.internal.ui.text.correction;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Map;
 
 import org.eclipse.core.runtime.CoreException;
 import org.jruby.ast.ClassNode;
 import org.jruby.ast.DefnNode;
 import org.jruby.ast.ModuleNode;
 import org.jruby.ast.Node;
 import org.jruby.ast.visitor.rewriter.DefaultFormatHelper;
 import org.jruby.ast.visitor.rewriter.FormatHelper;
 import org.jruby.ast.visitor.rewriter.ReWriteVisitor;
 import org.rubypeople.rdt.core.IRubyScript;
 import org.rubypeople.rdt.core.compiler.IProblem;
 import org.rubypeople.rdt.core.formatter.Indents;
 import org.rubypeople.rdt.internal.formatter.IndentationState;
 import org.rubypeople.rdt.internal.ti.util.ClosestSpanningNodeLocator;
 import org.rubypeople.rdt.internal.ti.util.INodeAcceptor;
 import org.rubypeople.rdt.internal.ui.rubyeditor.ASTProvider;
 import org.rubypeople.rdt.refactoring.core.NodeFactory;
 import org.rubypeople.rdt.ui.text.ruby.IInvocationContext;
 import org.rubypeople.rdt.ui.text.ruby.IProblemLocation;
 import org.rubypeople.rdt.ui.text.ruby.IQuickFixProcessor;
 import org.rubypeople.rdt.ui.text.ruby.IRubyCompletionProposal;
 
 import com.aptana.rdt.internal.parser.warnings.ConstantNamingConvention;
 import com.aptana.rdt.internal.parser.warnings.MethodMissingWithoutRespondTo;
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
 		case MethodMissingWithoutRespondTo.PROBLEM_ID:
 			// FIXME Only do this stuff when we apply the proposal! Don't do all this work just to create the proposal...
 			script = context.getRubyScript();
 			src = script.getSource();
 			int offset = 0;
 			Node rootNode = ASTProvider.getASTProvider().getAST(script, ASTProvider.WAIT_YES, null);
 			Node typeNode = ClosestSpanningNodeLocator.Instance().findClosestSpanner(rootNode, problem.getOffset(), new INodeAcceptor() {
 			
 				public boolean doesAccept(Node node) {
 					return node instanceof ClassNode || node instanceof ModuleNode;
 				}
 			
 			});
 			if (typeNode instanceof ClassNode) {
 				ClassNode classNode = (ClassNode) typeNode;
 				offset = classNode.getBodyNode().getPosition().getStartOffset();
 			} else if (typeNode instanceof ModuleNode) {
 				ModuleNode classNode = (ModuleNode) typeNode;
 				offset = classNode.getBodyNode().getPosition().getStartOffset();
 			}
 			DefnNode methodNode = NodeFactory.createMethodNode("respond_to?", new String[] {"symbol", "include_private = false"}, null);			
 			Node insert = NodeFactory.createBlockNode(true, NodeFactory.createNewLineNode(methodNode));			
 			String text = ReWriteVisitor.createCodeFromNode(insert, src, getFormatHelper());
 			// Figure out indent at offset and apply that to each line of text and at end of text
 			String line = src.substring(0, src.indexOf("\n", offset));
 			line = line.substring(line.lastIndexOf("\n") + 1);
 			Map options = script.getRubyProject().getOptions(true);
 			String indent = Indents.extractIndentString(line, options);			
 			text = indent + text;
 			text = text + "\n";
			text = text.replaceAll("\\n", "\n" + indent);			
 			LocalCorrectionsSubProcessor.addReplacementProposal(offset, 0, text, "Add respond_to? method stub", proposals);
 			break;
 		default:
 		}
 	}
 	
 	protected FormatHelper getFormatHelper() {
 		return new DefaultFormatHelper();
 	}
 
 	public boolean hasCorrections(IRubyScript unit, int problemId) {
 		switch (problemId) {
 		case IProblem.UnusedPrivateMethod:
 		case IProblem.UnusedPrivateField:
 		case IProblem.LocalVariableIsNeverUsed:
 		case IProblem.ArgumentIsNeverUsed:
 		case MisspelledConstructorVisitor.PROBLEM_ID:
 		case ConstantNamingConvention.PROBLEM_ID:
 		case MethodMissingWithoutRespondTo.PROBLEM_ID:
 			return true;
 		default:
 			return false;
 		}
 	}
 }
