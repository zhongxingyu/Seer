 package com.aptana.rdt.internal.parser.warnings;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jruby.ast.ClassNode;
 import org.jruby.ast.FCallNode;
 import org.jruby.ast.LocalAsgnNode;
 import org.jruby.evaluator.Instruction;
 import org.rubypeople.rdt.core.parser.warnings.RubyLintVisitor;
 import org.rubypeople.rdt.internal.core.util.ASTUtil;
 
 import com.aptana.rdt.AptanaRDTPlugin;
 
 public class LocalVariablePossibleAttributeAccess extends RubyLintVisitor {
 
 	private List<LocalAsgnNode> locals = new ArrayList<LocalAsgnNode>();
 
 	private List<String> attributes = new ArrayList<String>();
 
 	public LocalVariablePossibleAttributeAccess(String contents) {
 		super(AptanaRDTPlugin.getDefault().getOptions(), contents);
 	}
 
 	@Override
 	protected String getOptionKey() {
 		return AptanaRDTPlugin.COMPILER_PB_LOCAL_VARIABLE_POSSIBLE_ATTRIBUTE_ACCESS;
 	}
 
 	@Override
 	public Instruction visitClassNode(ClassNode iVisited) {
 		locals.clear();
 		attributes.clear();
 		return super.visitClassNode(iVisited);
 	}
 
 	@Override
 	public Instruction visitLocalAsgnNode(LocalAsgnNode iVisited) {
 		locals.add(iVisited);
 		return super.visitLocalAsgnNode(iVisited);
 	}
 
 	@Override
 	public Instruction visitFCallNode(FCallNode iVisited) {
 		String name = iVisited.getName();
 		if (name.equals("attr_accessor") || name.equals("attr_writer")
 				|| name.equals("attr")) {
 			List<String> args = ASTUtil.getArgumentsFromFunctionCall(iVisited);
 			if (name.equals("attr")) {
 				// second arg must be "true"
 				if (args.size() < 2) { 
 					return super.visitFCallNode(iVisited);
 				}
 				if (!args.get(1).equals("true")) return super.visitFCallNode(iVisited);
 				attributes.add(args.get(0));
 				return super.visitFCallNode(iVisited);
 			}
 			attributes.addAll(args);
 		}
 		return super.visitFCallNode(iVisited);
 	}
 
 	@Override
 	public void exitClassNode(ClassNode iVisited) {
 		for (LocalAsgnNode local : locals) {
 			if (attributes.contains(local.getName())) {
 				createProblem(local.getPosition(), "Local variable assignment might be possible attribute access attempt?");
 			}
 		}
 		super.exitClassNode(iVisited);
 	}
 
 }
