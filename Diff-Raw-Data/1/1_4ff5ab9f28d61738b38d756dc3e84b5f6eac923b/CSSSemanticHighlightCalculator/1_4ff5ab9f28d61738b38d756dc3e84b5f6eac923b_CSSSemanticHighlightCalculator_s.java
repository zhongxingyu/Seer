 package org.eclipse.e4.ui.syntaxcoloring;
 
 import java.util.Iterator;
 
 import org.eclipse.e4.cSS.Rules;
 import org.eclipse.e4.cSS.simple_selector;
 import org.eclipse.e4.cSS.impl.stylesheetImpl;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.xtext.impl.RuleCallImpl;
 import org.eclipse.xtext.nodemodel.INode;
 import org.eclipse.xtext.resource.XtextResource;
 import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightedPositionAcceptor;
 import org.eclipse.xtext.ui.editor.syntaxcoloring.ISemanticHighlightingCalculator;
 
 public class CSSSemanticHighlightCalculator implements
 		ISemanticHighlightingCalculator {
 
 	public void provideHighlightingFor(XtextResource resource,
 			IHighlightedPositionAcceptor acceptor) {
 		 if (resource.getContents().size() > 0) {
 			 EObject obj = resource.getContents().get(0);
 //			List<Rules> list = EcoreUtil2.typeSelect(obj.eContents(), Rules.class);
 //			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
 //				Rules rules = (Rules) iterator.next();
 //			}
 			 Iterator<INode> allNodes = resource.getParseResult().getRootNode().getAsTreeIterable().iterator();
 				while(allNodes.hasNext()) {
 					INode node = allNodes.next();
 					EObject eoj = node.getGrammarElement();
 					if (eoj instanceof RuleCallImpl) {
 						
 						if (((RuleCallImpl) eoj).getRule().getName().equals("element_name")) {
 //							node.getGrammarElement()
 							acceptor.addPosition(node.getOffset(), node.getLength(), CSSHighlightingConfiguration.CSS_Element);
 						} else if (((RuleCallImpl) eoj).getRule().getName().equals("css_hash_class")) {
 //							node.getGrammarElement()
 							acceptor.addPosition(node.getOffset(), node.getLength(), CSSHighlightingConfiguration.CSS_ClassID);
 					}
 				}
 				
 //		      Object m = resource.getContents().get(0);
 //		      if (m instanceof stylesheetImpl) {
 //		    	  stylesheetImpl ss = (stylesheetImpl) m;
 //		    	  EList<Rules> r =  ss.getRuleset();
 //		      }
 		      
 		    }
 	}
 
 }
