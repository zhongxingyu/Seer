 package uk.ac.bham.cs.sdsts.core.sitra_rules;
 
 import java.util.HashMap;
 
 import org.eclipse.uml2.uml.Interaction;
 import org.eclipse.uml2.uml.InteractionFragment;
 import org.eclipse.uml2.uml.Lifeline;
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
 		_SD_.set_attr(AAttr.ONE).zone = "SD";
 		
 		// create signature for SD
 		ASig SD = AlloyModel.getInstance().getSig(currentSD);
 		SD.set_attr(AAttr.ONE).set_parent(OperandAbstract);
 		SD.zone = "SD";
 		
 		// iterate messages
 		HashMap<String, ASig> lastElementOnLifeline = new HashMap<String, ASig>();
 		for (InteractionFragment interactionFragment : interaction.getFragments()) {
 			for (Lifeline lifeline : interactionFragment.getCovereds()) {
 				ASig fragmentSig = AlloyModel.getInstance().getSig(currentSD_ + interactionFragment.getName());
 				if(lastElementOnLifeline.containsKey(lifeline.getName())){
 					AlloyModel.getInstance().addFact("%s in %s.BEFORE", fragmentSig, lastElementOnLifeline.get(lifeline.getName())).zone = "Ordering";
 				}
 				lastElementOnLifeline.put(lifeline.getName(), fragmentSig);
 			}
 			ASig fragmentSig = AlloyModel.getInstance().getSig(currentSD_ + interactionFragment.getName());
 			AlloyModel.getInstance().addFact("all _F: %s | _F in %s.COVER", fragmentSig, SD).zone = "Covering: Operand->Fragment";
 		}
 		/**
 		***  Constraint: Fragment
 		**/
 		AlloyModel.getInstance().addFact("// no circle\nfact{all _F: FRAGMENT  | _F !in _F.^BEFORE}").zone = "Constraint: Fragment";
 		AlloyModel.getInstance().addFact("// should be covered by at most one Operand\nfact{all _F: FRAGMENT  | lone _OP: OPERAND | _F in _OP.COVER}").zone = "Constraint: Fragment";
 		return null;
 	}
 
 	@Override
 	public void setProperties(Object target, Object source, Transformer t) {
 		// TODO Auto-generated method stub
 	}
 
 }
