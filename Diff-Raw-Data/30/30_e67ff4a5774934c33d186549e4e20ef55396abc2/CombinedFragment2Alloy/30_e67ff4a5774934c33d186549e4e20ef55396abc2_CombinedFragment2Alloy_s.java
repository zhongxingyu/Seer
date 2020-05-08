 package uk.ac.bham.cs.sdsts.core.sitra_rules;
 
 
 import org.eclipse.uml2.uml.CombinedFragment;
 import org.eclipse.uml2.uml.InteractionOperand;
 import org.eclipse.uml2.uml.InteractionOperatorKind;
 import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
 import org.eclipse.uml2.uml.internal.impl.CombinedFragmentImpl;
 
 import edu.mit.csail.sdg.alloy4.Err;
 import edu.mit.csail.sdg.alloy4compiler.ast.Attr;
 import edu.mit.csail.sdg.alloy4compiler.ast.Sig.PrimSig;
 
 import uk.ac.bham.cs.sdsts.core.synthesis.AlloyModel;
 import uk.ac.bham.sitra.Rule;
 import uk.ac.bham.sitra.RuleNotFoundException;
 import uk.ac.bham.sitra.Transformer;
 
 @SuppressWarnings({ "rawtypes", "restriction" })
 public class CombinedFragment2Alloy implements Rule {
 
 	@Override
 	public boolean check(Object source) {
 		if (source instanceof CombinedFragmentImpl) {
 			return true;
 		} else
 			return false;
 	}
 
 	@Override
 	public Object build(Object source, Transformer t) {
 		try {
 			CombinedFragment combinedFragment = (CombinedFragment) source;
 			// add abstract for combinedFragment
 			// abstract sig COMBINEDFRAGMENT {}
 			PrimSig combinedFragment_abstract = new PrimSig("COMBINEDFRAGMENT", Attr.ABSTRACT);
 			combinedFragment_abstract = (PrimSig) AlloyModel.getInstance().addAbstract(combinedFragment_abstract);
 			
 			// add sig for combinedFragment type
 			PrimSig cfType = null;
 			if(combinedFragment.getInteractionOperator() == InteractionOperatorKind.PAR_LITERAL){
 				cfType = new PrimSig("CombiedFragmentType_Par", Attr.ONE) ;
 			}
 			if(combinedFragment.getInteractionOperator() == InteractionOperatorKind.ALT_LITERAL){
 				cfType = new PrimSig("CombinedFragmentType_Alt", Attr.ONE);
 			}
 			if(combinedFragment.getInteractionOperator() == InteractionOperatorKind.LOOP_LITERAL){
 				cfType = new PrimSig("CombinedFragmentType_Loop", Attr.ONE);
 			}
 			cfType = (PrimSig) AlloyModel.getInstance().addSig(cfType);
 			
 			// add sig for combinedFragment
 			// one sig SDid_CFname {op1:operand1, op2:operand2 ...}
 			PrimSig combinedFragmentSig = new PrimSig(AlloyModel.getInstance().getSD_prefix() + combinedFragment.getName(), combinedFragment_abstract, Attr.ONE);
 			combinedFragmentSig.addField("type", cfType);
 			int i = 1;
 			for (InteractionOperand interactionOperand : combinedFragment.getOperands()) {
 				PrimSig interactionOperandSig = (PrimSig) t.transform(interactionOperand);
 				combinedFragmentSig.addField("op" + i++, interactionOperandSig);
 			}
 			AlloyModel.getInstance().addSig(combinedFragmentSig);
 			
 			
 			// add fact inside the combined fragment for Par
 			if(combinedFragment.getInteractionOperator() == InteractionOperatorKind.PAR_LITERAL){
 				for (i = 0; i < combinedFragment.getOperands().size(); i++) {
					for (int j = 0; j < combinedFragment.getOperands().size(); j++) {
						if(i == j)continue;
 						InteractionOperand op1 = combinedFragment.getOperands().get(i);
 						InteractionOperand op2 = combinedFragment.getOperands().get(j);
 						MessageOccurrenceSpecification mo1 = (MessageOccurrenceSpecification) op1.getFragments().get(0);
 						MessageOccurrenceSpecification mo2 = (MessageOccurrenceSpecification) op2.getFragments().get(0);
 						
 					}
 				}
 			}
 			
 			
 			// add fact inside the combined fragment for Alt
 			if(combinedFragment.getInteractionOperator() == InteractionOperatorKind.PAR_LITERAL){
 				for (i = 0; i < combinedFragment.getOperands().size(); i++) {
 					for (int j = i+1; j < combinedFragment.getOperands().size(); j++) {
 						InteractionOperand op1 = combinedFragment.getOperands().get(i);
 						InteractionOperand op2 = combinedFragment.getOperands().get(j);
 						
 						MessageOccurrenceSpecification mo1 = (MessageOccurrenceSpecification) op1.getFragments().get(0);
 						MessageOccurrenceSpecification mo2 = (MessageOccurrenceSpecification) op2.getFragments().get(0);
 						
 					}
 				}
 			}			
 
 			
 			
 		} catch (Err e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (RuleNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return null;
 	}
 
 	@Override
 	public void setProperties(Object target, Object source, Transformer t) {
 		
 	}
 
 }
