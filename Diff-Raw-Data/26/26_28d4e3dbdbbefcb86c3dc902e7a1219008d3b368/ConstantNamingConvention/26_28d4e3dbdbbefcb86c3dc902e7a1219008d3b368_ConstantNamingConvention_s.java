 package com.aptana.rdt.internal.parser.warnings;
 
 import org.jruby.ast.ConstDeclNode;
 import org.jruby.evaluator.Instruction;
 import org.rubypeople.rdt.core.parser.warnings.RubyLintVisitor;
 
 import com.aptana.rdt.AptanaRDTPlugin;
 
 public class ConstantNamingConvention extends RubyLintVisitor {
 
 	public ConstantNamingConvention(String contents) {
 		super(contents);
 	}
 
 	@Override
 	protected String getOptionKey() {
 		return AptanaRDTPlugin.COMPILER_PB_CONSTANT_NAMING_CONVENTION;
 	}
 	
 	@Override
 	public Instruction visitConstDeclNode(ConstDeclNode iVisited) {
 		String name = iVisited.getName();
 		if (!name.toUpperCase().equals(name)) {
			createProblem(iVisited.getPosition(), "Constant name doesn't match ALL_CAPS_WITH_UNDERSCORES convention: " + name);
 		}
 		return super.visitConstDeclNode(iVisited);
 	}
 
 }
