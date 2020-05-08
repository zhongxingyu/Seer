 /***
  *  Author: Yi Chen
  */
 package uk.ac.bham.cs.sdsts.core.sitra_rules;
 
 
 import java.util.ArrayList;
 import org.eclipse.uml2.uml.CombinedFragment;
 import org.eclipse.uml2.uml.InteractionConstraint;
 import org.eclipse.uml2.uml.InteractionFragment;
 import org.eclipse.uml2.uml.InteractionOperand;
 import org.eclipse.uml2.uml.InteractionOperatorKind;
 import org.eclipse.uml2.uml.Message;
 import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
 import org.eclipse.uml2.uml.internal.impl.CombinedFragmentImpl;
 import uk.ac.bham.cs.sdsts.SDConsole;
 import uk.ac.bham.cs.sdsts.Alloy.AAttr;
 import uk.ac.bham.cs.sdsts.Alloy.AFact;
 
 import uk.ac.bham.cs.sdsts.Alloy.ASig;
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
 			String currentSD = AlloyModel.getInstance().getSD();
 			String currentSD_ = currentSD + "_";
 			
 			// add abstract combined fragment type
 			// abstract sig CF_TYPE{}
 			ASig combinedFragmentType = AlloyModel.getInstance().getSig("CF_TYPE");
 			combinedFragmentType.set_attr(AAttr.ABSTRACT);
 			combinedFragmentType.zone = "Abstract";
 			
 			// add abstract for combinedFragment
 			// abstract sig COMBINEDFRAGMENT{COVER: set OPERAND, TYPE: one CF_TYPE}
 			ASig fragmentAbstract = AlloyModel.getInstance().getSig("FRAGMENT");
 			ASig operandAbstract = AlloyModel.getInstance().getSig("OPERAND");
 			ASig combinedFragmentAbstract = AlloyModel.getInstance().getSig("COMBINEDFRAGMENT");
 			combinedFragmentAbstract.set_attr(AAttr.ABSTRACT);
 			combinedFragmentAbstract.set_parent(fragmentAbstract);
 			combinedFragmentAbstract.AddField("COVER", operandAbstract.setOf());
 			combinedFragmentAbstract.AddField("TYPE", combinedFragmentType.oneOf());
 			combinedFragmentAbstract.zone = "Abstract";
 			
 			
 			// add existence to SD
 			ASig SD = AlloyModel.getInstance().getSig("_SD_");
 			SD.AddField("COMBINEDFRAGMENTS", new AFact("_SD_ one -> COMBINEDFRAGMENT"));
 			
 			// add sig for combinedFragment type
 			ASig cfType = null;
 			if(combinedFragment.getInteractionOperator() == InteractionOperatorKind.PAR_LITERAL){
 				cfType = AlloyModel.getInstance().getSig("CF_TYPE_PAR");
 			}
 			if(combinedFragment.getInteractionOperator() == InteractionOperatorKind.ALT_LITERAL){
 				cfType = AlloyModel.getInstance().getSig("CF_TYPE_ALT");
 			}
 			if(combinedFragment.getInteractionOperator() == InteractionOperatorKind.LOOP_LITERAL){
 				cfType = AlloyModel.getInstance().getSig("CF_TYPE_LOOP");
 			}
 			cfType.set_attr(AAttr.ONE);
 			cfType.set_parent(combinedFragmentType);
 			cfType.zone = "Combined Fragment Type";
 			
 			// add sig for combinedFragment
 			// one sig cf1 extends COMBINEDFRAGMENT{}
 			ASig combinedFragmentSig = AlloyModel.getInstance().getSig(currentSD_ + combinedFragment.getName());
 			combinedFragmentSig.set_attr(AAttr.ONE);
 			combinedFragmentSig.set_parent(combinedFragmentAbstract);
 			combinedFragmentSig.zone = "Combined Fragment";
 			
 			// Fact: CF.Type = Type
 			AlloyModel.getInstance().addFact("%s.TYPE = %s", combinedFragmentSig, cfType).zone = "Binding: Combined Fragment Type";
 			
 			// Relations among operands
 			for (InteractionOperand interactionOperand : combinedFragment.getOperands()) {
 				if(combinedFragment.getInteractionOperator() == InteractionOperatorKind.LOOP_LITERAL){
 					InteractionConstraint timesConstraint = combinedFragment.getOperand(null).getGuard();
 					String specification = timesConstraint.getSpecification().toString();
 					String times = specification.substring(specification.indexOf("value: "));
 					times = times.substring(times.indexOf(":")+2);
 					times = times.substring(0, times.indexOf(")"));
 					try {
 						int timeInt = Integer.parseInt(times);
 						
 						for (int i = 1; i <= timeInt; i++) {
 						    
 							ASig interactionOperandSig = (ASig) t.transform(interactionOperand);
 							//ASig interactionOperandSig = (ASig) t.transform(interactionOperand);
 							// Fact Combined Fragment covers Operand
 							AlloyModel.getInstance().addFact("%s in %s.COVER", interactionOperandSig, combinedFragmentSig).zone = "Covering: Combined Fragment->Operand";
 							
 						}
 					} catch (Exception e) {
 						SDConsole.print_has_time("Error: invalid characters in Loop definition. It must be a number.");
 						return null;
 					}
 					
 				}else{
 					interactionOperand.setName(combinedFragment.getName() + "_" + interactionOperand.getName());
 					ASig interactionOperandSig = (ASig) t.transform(interactionOperand);
 					// Fact Combined Fragment covers Operand
 					AlloyModel.getInstance().addFact("%s in %s.COVER", interactionOperandSig, combinedFragmentSig).zone = "Covering: Combined Fragment->Operand";
 					
 					// Special for ALT
 					if(combinedFragment.getInteractionOperator() == InteractionOperatorKind.ALT_LITERAL){
 						interactionOperandSig.set_attr(AAttr.LONE);
					}
					for (Message message : getMessagesInOperand(interactionOperand)) {
						ASig messageSig = AlloyModel.getInstance().getSig(currentSD_ + message.getName());
						messageSig.set_attr(AAttr.LONE);
						AlloyModel.getInstance().addFact("#%s=#%s", messageSig, interactionOperandSig).zone = "Number: Message = Operand";
 					}
 				}
 			}
 		
 			/**
 			***  Constraint: Combined Fragment
 			**/
 			// Combined Fragments have no relation of "BEFORE"
 			AlloyModel.getInstance().addFact("// Combined Fragments have no relation of BEFORE\nfact{all _CF: COMBINEDFRAGMENT | no _F: FRAGMENT | _CF in _F.BEFORE or _F in _CF.BEFORE}").zone = "Constraint: Combined Fragment";
 			// OPERAND: in one OP, at most one event for each lifeline can have no Next
 			AlloyModel.getInstance().addFact("// OPERAND: in one OP, at most one event for each lifeline can have no Next\nfact{all _L: LIFELINE, _OP: OPERAND | lone _E: EVENT | _E in _OP.COVER and _E.COVER=_L and #_E.BEFORE=0}").zone = "Constraint: Combined Fragment";
 			// OPERAND: the children can not cover their parent
 			AlloyModel.getInstance().addFact("// OPERAND: the children can not cover their parent\nfact{all _OP: OPERAND | _OP !in _OP.^(COVER.COVER)}").zone = "Constraint: Combined Fragment";
 			// OPERAND: one OP can not be before and after the same other OP
 			AlloyModel.getInstance().addFact("// OPERAND: one OP can not be before and after the same other OP\nfact{all _CF: COMBINEDFRAGMENT, _OP1: _CF.COVER, _OP2: _CF.COVER, _E1: _OP1.COVER,_E2: _OP2.COVER, _E3: _OP1.COVER | no _E4: _OP2.COVER | _OP1 != _OP2 and _E2 in _E1.BEFORE and _E3 in _E4.BEFORE}").zone = "Constraint: Combined Fragment";
 			// one CF should be covered by at most one Operand
 			AlloyModel.getInstance().addFact("// one CF should be covered by at most one Operand\nfact{all _F: FRAGMENT  | lone _OP: OPERAND | _F in _OP.COVER}\nfact{all _OP: OPERAND | lone _F: COMBINEDFRAGMENT | _OP in _F.COVER}").zone = "Constraint: Combined Fragment";
 			if(combinedFragment.getInteractionOperator() == InteractionOperatorKind.ALT_LITERAL)
 				AlloyModel.getInstance().addFact("// alt: exact one operand will be executed\nfact{all _CF: COMBINEDFRAGMENT | (_CF.TYPE = CF_TYPE_ALT) => #_CF.COVER = 1}").zone = "Constraint: Combined Fragment";
 		} catch (RuleNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return null;
 	}
 	
 	private static ArrayList<Message> getMessagesInOperand(InteractionOperand interactionOperand){
 		ArrayList<Message> messages = new ArrayList<Message>();
 		for (InteractionFragment interactionFragment : interactionOperand.getFragments()) {
 			if(interactionFragment instanceof MessageOccurrenceSpecification){
 				MessageOccurrenceSpecification event = (MessageOccurrenceSpecification) interactionFragment;
 				if(!messages.contains(event.getMessage())){
 					event.getMessage().getSendEvent().setName(interactionOperand.getName() + "_" +event.getMessage().getSendEvent().getName());
 					event.getMessage().getReceiveEvent().setName(interactionOperand.getName() + "_" +event.getMessage().getReceiveEvent().getName());
 					messages.add(event.getMessage());
 				}				 
 			}
 		}
 		return messages;
 	}
 
 	@Override
 	public void setProperties(Object target, Object source, Transformer t) {
 		
 	}
 
 }
