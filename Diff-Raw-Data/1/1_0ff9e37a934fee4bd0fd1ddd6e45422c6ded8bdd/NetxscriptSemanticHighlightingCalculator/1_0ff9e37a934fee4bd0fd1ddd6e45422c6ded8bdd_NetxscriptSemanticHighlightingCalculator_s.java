 package com.netxforge.ui.highlighting;
 
 import org.eclipse.xtext.CrossReference;
 import org.eclipse.xtext.EnumRule;
 import org.eclipse.xtext.RuleCall;
 import org.eclipse.xtext.nodemodel.INode;
 import org.eclipse.xtext.resource.XtextResource;
 import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightedPositionAcceptor;
 import org.eclipse.xtext.ui.editor.syntaxcoloring.ISemanticHighlightingCalculator;
 
 public class NetxscriptSemanticHighlightingCalculator implements
 		ISemanticHighlightingCalculator {
 
	@Override
 	public void provideHighlightingFor(XtextResource resource,
 			IHighlightedPositionAcceptor acceptor) {
 
 		if (resource == null || resource.getParseResult() == null)
 			return;
 
 		INode root = resource.getParseResult().getRootNode();
 		for (INode node : root.getAsTreeIterable()) {
 
 			// if(node.getGrammarElement() != null){
 			// EObject grammarElement = node.getGrammarElement();
 			// if(grammarElement instanceof RuleCall){
 			// AbstractRule rule = ((RuleCall) grammarElement).getRule();
 			// System.out.println(" rule call node: " + rule);
 			// }
 			// }
 
 			if (node.getGrammarElement() instanceof CrossReference) {
 				acceptor.addPosition(node.getOffset(), node.getLength(),
 						NetxscriptHighlightingConfiguration.CROSS_REF);
 			}
 			if (node.getGrammarElement() instanceof RuleCall) {
 
 				// Drill down the rulle call, to find enum rules with special
 				// highlighting.
 				RuleCall rc = (RuleCall) node.getGrammarElement();
 				if (rc.getRule() instanceof EnumRule) {
 					EnumRule rule = (EnumRule) rc.getRule();
 					if (rule.getName().equals("ValueRange")) {
 						acceptor.addPosition(node.getOffset(),
 								node.getLength(),
 								NetxscriptHighlightingConfiguration.VALUE_RANGE);
 					} else if (rule.getName().equals("IntervalKind")) {
 						acceptor.addPosition(node.getOffset(),
 								node.getLength(),
 								NetxscriptHighlightingConfiguration.INTERVAL_KIND);
 
 					}
 				}
 			}
 		}
 	}
 }
