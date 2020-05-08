 package uk.ac.bham.cs.sdsts.core.sitra_rules;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.eclipse.uml2.uml.CombinedFragment;
 import org.eclipse.uml2.uml.Interaction;
 import org.eclipse.uml2.uml.InteractionFragment;
 import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
 import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
 import org.eclipse.uml2.uml.internal.impl.InteractionImpl;
 
 import uk.ac.bham.cs.sdsts.Alloy.AAttr;
 import uk.ac.bham.cs.sdsts.Alloy.ASig;
 import uk.ac.bham.cs.sdsts.core.synthesis.AlloyModel;
 import uk.ac.bham.sitra.Rule;
 import uk.ac.bham.sitra.RuleNotFoundException;
 import uk.ac.bham.sitra.Transformer;
 
 @SuppressWarnings({ "rawtypes", "restriction" })
 public class Interaction2Alloy implements Rule{
 
 	@Override
 	public boolean check(Object source) {
 		if(source instanceof InteractionImpl)
 			return true;
 		else return false;
 	}
 
 	@Override
 	public Object build(Object source, Transformer t) {
 		// transform the members first
 		Interaction interaction = (Interaction) source;
 		String currentSD = AlloyModel.getInstance().getSD();
 		String currentSD_ = currentSD + "_";
 		try {
 			t.transformAll(interaction.getOwnedElements());
 		} catch (RuleNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// add abstract for Fragment
 		// abstract sig FRAGMENT{BEFORE: set FRAGMENT}
 		ASig fragmentAbstract = AlloyModel.getInstance().getSig("FRAGMENT");
 		fragmentAbstract.set_attr(AAttr.ABSTRACT);
 		fragmentAbstract.AddField("BEFORE", fragmentAbstract.setOf());
 		fragmentAbstract.zone = "Abstract";
 		
 		// add abstract for Operand
 		// abstract sig OPERAND{COVER: set FRAGMENT}
 		ASig OperandAbstract = AlloyModel.getInstance().getSig("OPERAND");
 		OperandAbstract.set_attr(AAttr.ABSTRACT);
 		OperandAbstract.AddField("COVER", fragmentAbstract.setOf());
 		OperandAbstract.zone = "Abstract";
 		
 		// create _SD_
 		ASig _SD_ = AlloyModel.getInstance().getSig("_SD_");
 		_SD_.set_parent(OperandAbstract);
 		_SD_.set_attr(AAttr.ONE).zone = "SD";
 		
 		// create signature for SD
 		ASig SD = AlloyModel.getInstance().getSig(currentSD);
 		SD.set_attr(AAttr.LONE).set_parent(OperandAbstract);
 		SD.zone = "SD";
 		AlloyModel.getInstance().addFact(String.format("#%s = 0", SD.get_name())).zone = "Glue";
 		
 		SD.mergeTo(_SD_);
 		_SD_.set_attr(AAttr.ONE);
 		
 		// iterate messages
 		ASig combinedFragmentAbstract = null;//AlloyModel.getInstance().getSig("COMBINEDFRAGMENT");
 		HashMap<String, ASig> lastElementOnLifeline = new HashMap<String, ASig>();
 		ArrayList<ASig> CFs = new ArrayList<ASig>();
 		ArrayList<ASig> nonCFs = new ArrayList<ASig>();
 		for (InteractionFragment interactionFragment : interaction.getFragments()) {
 			if(interactionFragment instanceof MessageOccurrenceSpecification){
 				for (Lifeline lifeline : interactionFragment.getCovereds()) {
 					ASig fragmentSig = AlloyModel.getInstance().getSig(currentSD_ + interactionFragment.getName());
 					if(lastElementOnLifeline.containsKey(lifeline.getName())){
 						ASig lastElement = lastElementOnLifeline.get(lifeline.getName());
 						if(lastElement.get_parent().equals(combinedFragmentAbstract)){
 							AlloyModel.getInstance().addFact("all _L:%s, _E1:%s.COVER.COVER, _E2:%s | _E1.COVER=_L => _E2 in _E1.^BEFORE", AlloyModel.getInstance().getSig(currentSD_ + lifeline.getName()), lastElement, fragmentSig).zone = "Ordering";
 						}
 						else
 							AlloyModel.getInstance().addFact("%s in %s.^BEFORE", fragmentSig, lastElement).zone = "Ordering";
 					}
 					lastElementOnLifeline.put(lifeline.getName(), fragmentSig);
 				}
 			}
 			if(interactionFragment instanceof CombinedFragment){
 				combinedFragmentAbstract = AlloyModel.getInstance().getSig("COMBINEDFRAGMENT");
 				for (Lifeline lifeline : interactionFragment.getCovereds()) {
 					ASig fragmentSig = AlloyModel.getInstance().getSig(currentSD_ + interactionFragment.getName());
 					if(lastElementOnLifeline.containsKey(lifeline.getName())){
 						ASig lastElement = lastElementOnLifeline.get(lifeline.getName());
 						if(lastElement.get_parent().equals(combinedFragmentAbstract)){
 							AlloyModel.getInstance().addFact("all _L:%s, _E1:%s.COVER.COVER, _E2:%s.COVER.COVER | _E1.COVER=_L and _E2.COVER=_L => _E2 in _E1.^BEFORE", AlloyModel.getInstance().getSig(currentSD_ + lifeline.getName()), lastElement, fragmentSig).zone = "Ordering";
 						}
 						else
 							AlloyModel.getInstance().addFact("all _L:%s, _E1:%s, _E2:%s.COVER.COVER | _E2.COVER=_L => _E2 in _E1.^BEFORE", AlloyModel.getInstance().getSig(currentSD_ + lifeline.getName()), lastElement, fragmentSig).zone = "Ordering";
 					}
 					lastElementOnLifeline.put(lifeline.getName(), fragmentSig);
 				}
 				try {
 					t.transform(interactionFragment);
 				} catch (RuleNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			ASig fragmentSig = AlloyModel.getInstance().getSig(currentSD_ + interactionFragment.getName());
 			if(AlloyModel.getInstance().existSig("COMBINEDFRAGMENT"))
 				AlloyModel.getInstance().addFact("all _F: %s | _F in %s.*(COVER.COVER).COVER", fragmentSig, SD).zone = "Covering: Operand->Fragment";
 			else AlloyModel.getInstance().addFact("all _F: %s | _F in %s.*COVER", fragmentSig, SD).zone = "Covering: Operand->Fragment";
 			if(interactionFragment instanceof CombinedFragment)CFs.add(fragmentSig);
 			if(interactionFragment instanceof MessageOccurrenceSpecification)nonCFs.add(fragmentSig);
 		}
 		for (ASig aSig : CFs) {
 			for (ASig aSig1 : CFs) {
 				if(aSig1.equals(aSig))continue;
 				AlloyModel.getInstance().addFact("all _F: %s | _F !in %s.^(COVER.COVER)", aSig1, aSig).zone = "Covering: Operand->Fragment";
 			}
 			for (ASig aSig2 : nonCFs) {
 				AlloyModel.getInstance().addFact("all _F: %s | _F !in %s.^(COVER.COVER)", aSig2, aSig).zone = "Covering: Operand->Fragment";
 			}
 		}
 		/**
 		***  Constraint: Fragment
 		**/
 		AlloyModel.getInstance().addFact("// no circle\nfact{all _F: FRAGMENT  | _F !in _F.^BEFORE}").zone = "Constraint: Fragment";
 		
 		return null;
 	}
 
 	@Override
 	public void setProperties(Object target, Object source, Transformer t) {
 		// TODO Auto-generated method stub
 	}
 
 }
