 package uk.ac.bham.cs.sdsts.core.sitra_rules;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.uml2.uml.InteractionFragment;
 import org.eclipse.uml2.uml.InteractionOperand;
 import org.eclipse.uml2.uml.Message;
 import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
 import org.eclipse.uml2.uml.internal.impl.InteractionOperandImpl;
 import org.eclipse.uml2.uml.internal.impl.MessageOccurrenceSpecificationImpl;
 
 import edu.mit.csail.sdg.alloy4.Err;
 import edu.mit.csail.sdg.alloy4compiler.ast.Attr;
 import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
 import edu.mit.csail.sdg.alloy4compiler.ast.Sig.PrimSig;
 import uk.ac.bham.cs.sdsts.core.synthesis.AlloyModel;
 import uk.ac.bham.sitra.Rule;
 import uk.ac.bham.sitra.RuleNotFoundException;
 import uk.ac.bham.sitra.Transformer;
 
 @SuppressWarnings({ "rawtypes", "restriction" })
 public class InteractionOperand2Alloy implements Rule {
 
 	@Override
 	public boolean check(Object source) {
 		if (source instanceof InteractionOperandImpl)
 			return true;
 		else
 			return false;
 	}
 
 	@Override
 	public Object build(Object source, Transformer t) {
 		try {
 			InteractionOperand interactionOperand = (InteractionOperand) source;
 			
 			// add abstract for InteractionOperand
 			// abstract sig INTERACTIONOPERAND {}
 			PrimSig interactionOperand_abstract = new PrimSig("INTERACTIONOPERAND", Attr.ABSTRACT);
 			interactionOperand_abstract = (PrimSig) AlloyModel.getInstance().addAbstract(interactionOperand_abstract);
 			
 			// add the interactionOperand
 			// one sig SDid_InteractionName extends INTERACTION {cover1: Message1, cover2: Message2 ...}
 			PrimSig interactionOperandSig = new PrimSig(AlloyModel.getInstance().getSD_prefix() + interactionOperand.getName(), interactionOperand_abstract, Attr.ONE);
 			
 			Map<Message, Integer> mMessage = new HashMap<Message, Integer>();
 			for (int i = 0; i < interactionOperand.getMembers().size(); i++) {
 				if(!mMessage.containsKey(((MessageOccurrenceSpecification) interactionOperand.getMembers().get(i)).getMessage())){
 					MessageOccurrenceSpecification messageOccurrenceSpecification = (MessageOccurrenceSpecification) interactionOperand.getMembers().get(i);
 					mMessage.put(messageOccurrenceSpecification.getMessage(), 1);
 				}
 			}
 			int i=1;
 			for (Message message : mMessage.keySet()) {
 				message.getSendEvent().setName(interactionOperand.getName() + "_" + message.getSendEvent().getName());
 				message.getReceiveEvent().setName(interactionOperand.getName() + "_" + message.getReceiveEvent().getName());
 				t.transform(message);
 				String messageName = AlloyModel.getInstance().getName(AlloyModel.getInstance().getSD_prefix() + message.getName());
 				PrimSig messageSig = (PrimSig) AlloyModel.getInstance().getSigByName(messageName);
 				interactionOperandSig.addField("cover" + i++, messageSig);
 			}
 			AlloyModel.getInstance().addSig(interactionOperandSig);
 			
 			// add facts
 			// for every message M(j) after M(i) {
 			for (i = 0; i < interactionOperand.getFragments().size(); i++) {
 				Message message1 = null;
 				InteractionFragment o1 = interactionOperand.getFragments().get(i);
 				if(o1.getClass().equals(MessageOccurrenceSpecificationImpl.class)){
 					if(!((MessageOccurrenceSpecificationImpl)o1).getMessage().equals(message1)){
 						message1 = ((MessageOccurrenceSpecificationImpl)o1).getMessage();
 						
 						for (int j = i+1; j < interactionOperand.getFragments().size(); j++) {
 							Message message2 = message1;
 							InteractionFragment o2 = interactionOperand.getFragments().get(j);
 							if(o2.getClass().equals(MessageOccurrenceSpecificationImpl.class)){
 								if(!((MessageOccurrenceSpecificationImpl)o2).getMessage().equals(message2)){
 									message2 = ((MessageOccurrenceSpecificationImpl)o2).getMessage();
 									MessageOccurrenceSpecification m1send = (MessageOccurrenceSpecification) message1.getSendEvent();
 									MessageOccurrenceSpecification m1rec = (MessageOccurrenceSpecification) message1.getReceiveEvent();
 									MessageOccurrenceSpecification m2send = (MessageOccurrenceSpecification) message2.getSendEvent();
 									MessageOccurrenceSpecification m2rec = (MessageOccurrenceSpecification) message2.getReceiveEvent();
 									PrimSig m1sendSig = (PrimSig) AlloyModel.getInstance().getSigByName(AlloyModel.getInstance().getSD_prefix() + m1send.getName());
 									PrimSig m1recSig = (PrimSig) AlloyModel.getInstance().getSigByName(AlloyModel.getInstance().getSD_prefix() + m1rec.getName());
 									PrimSig m2sendSig = (PrimSig) AlloyModel.getInstance().getSigByName(AlloyModel.getInstance().getSD_prefix() + m2send.getName());
 									PrimSig m2recSig = (PrimSig) AlloyModel.getInstance().getSigByName(AlloyModel.getInstance().getSD_prefix() + m2rec.getName());
 									//	Mi.send < Mj.send
 									Expr fact1 = m2sendSig.in(m1sendSig.join(m1sendSig.parent.getFields().get(0)));
 									//	not (Mj.send < Mi.send)
 									Expr fact2 = m1sendSig.in(m2sendSig.join(m1sendSig.parent.getFields().get(0))).not();
 									//	Mi.rec < Mj.rec
 									Expr fact3 = m2recSig.in(m1recSig.join(m1sendSig.parent.getFields().get(0)));
 									//	not (Mj.rec < Mi.rec)	
 									Expr fact4 = m1recSig.in(m2recSig.join(m1sendSig.parent.getFields().get(0))).not();
 									//	not (Mi.send < Mj.rec)
 									Expr fact5 = m2recSig.in(m1sendSig.join(m1sendSig.parent.getFields().get(0))).not();
 									//	not (Mi.send > Mj.rec)			
 									Expr fact6 = m1sendSig.in(m2recSig.join(m1sendSig.parent.getFields().get(0))).not();
									//	Mi.rec < Mj.send
									Expr fact7 = m2sendSig.in(m1recSig.join(m1sendSig.parent.getFields().get(0)));
									//	Mi.rec > Mj.send
 									Expr fact8 = m1recSig.in(m2sendSig.join(m1sendSig.parent.getFields().get(0))).not();
 									AlloyModel.getInstance().addFact(fact1);
 									AlloyModel.getInstance().addFact(fact2);
 									AlloyModel.getInstance().addFact(fact3);
 									AlloyModel.getInstance().addFact(fact4);
 									AlloyModel.getInstance().addFact(fact5);
 									AlloyModel.getInstance().addFact(fact6);
 									AlloyModel.getInstance().addFact(fact7);
 									AlloyModel.getInstance().addFact(fact8);
 								}
 								
 							}
 						}
 					}
 				}
 				
 			}
 			
 			
 			// add facts
 			//			for every message {
 			//				send < receive
 			//				receive !< send
 			//			}
 //			for (i = 0; i < interactionOperand.getFragments().size(); i++) {
 //				for (int j = i+1; j < interactionOperand.getFragments().size(); j++) {
 //					InteractionFragment o1 = interactionOperand.getFragments().get(i);
 //					InteractionFragment o2 = interactionOperand.getFragments().get(j);
 //					if(o1.getClass().equals(o2.getClass()) && o1.getClass().equals(MessageOccurrenceSpecificationImpl.class)){
 //						PrimSig s1 = (PrimSig) AlloyModel.getInstance().getSigByName(AlloyModel.getInstance().getSD_prefix() + o1.getName());
 //						PrimSig s2 = (PrimSig) AlloyModel.getInstance().getSigByName(AlloyModel.getInstance().getSD_prefix() + o2.getName());
 //						Expr fact = s1.in(s2.join(s2.parent.getFields().get(0)));
 //						AlloyModel.getInstance().addFact(fact);
 //					}
 //					
 //					//fact1 += String.format("%s in %s.isBefore\n", interaction.getFragments().get(j).getName(), interaction.getFragments().get(i).getName());
 //				}
 //			}
 			return interactionOperandSig;
 			
 		} catch (Err e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (RuleNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 		
 //		String alloyString = "";
 //		InteractionOperandImpl interactionOperandImpl = (InteractionOperandImpl) source;
 //		alloyString += String.format("one sig %s extends InteractionOperand {",
 //				interactionOperandImpl.getName());
 //		Map<Message, Integer> mMessage = new HashMap<Message, Integer>();
 //		for (int i = 0; i < interactionOperandImpl.getMembers().size(); i++) {
 //			if(!mMessage.containsKey(interactionOperandImpl.getMembers().get(i))){
 //				MessageOccurrenceSpecification messageOccurrenceSpecification = (MessageOccurrenceSpecification) interactionOperandImpl.getMembers().get(i);
 //				mMessage.put(messageOccurrenceSpecification.getMessage(), 1);
 //			}
 //		}
 //		int count = 1;
 //		for (Message message : mMessage.keySet()) {
 //			if(count++ != 1)alloyString += ", ";
 //			alloyString += String.format("co%d:%s", count-1, message.getName());
 //		}
 //	
 //		alloyString += "}";
 //
 //		// generate the facts
 //		String fact1 = "\nfact{\n";
 //		String fact2 = "fact{\n";
 //		count = 0;
 //		for (int i = 0; i < interactionOperandImpl.getMembers().size(); i++) {
 //			for (int j = i + 1; j < interactionOperandImpl.getMembers().size(); j++) {
 //				count++;
 //				fact1 += String.format("%s in %s.isBefore\n", interactionOperandImpl.getMembers().get(j).getName(), interactionOperandImpl.getMembers().get(i).getName());
 //				fact2 += String.format("%s !in %s.isBefore\n", interactionOperandImpl.getMembers().get(i).getName(), interactionOperandImpl.getMembers().get(j).getName());
 //
 //			}
 //		}
 //		fact1 += "}\n";
 //		fact2 += "}\n";
 //		if (count > 0) {
 //			alloyString += fact1;
 //			alloyString += fact2;
 //		}
 //		return alloyString;
 	}
 
 	@Override
 	public void setProperties(Object target, Object source, Transformer t) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
