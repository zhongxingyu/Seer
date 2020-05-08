 package nz.co.lake_hammond.apprenons.activities.traduction;
 
 import nz.co.lake_hammond.apprenons.model.Classification;
 import nz.co.lake_hammond.apprenons.model.Classification.VerbDetails;
 import nz.co.lake_hammond.apprenons.model.Classification.VerbDetails.Group;
 import nz.co.lake_hammond.apprenons.model.Classification.VerbDetails.Transitivity;
 import nz.co.lake_hammond.apprenons.R;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CheckBox;
 import android.widget.RadioButton;
 
 public class VerbOptionsFragment extends TraductionOptionsFragment {
 
 	private String verb;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.verb_options_fragment, container, false);
 	}
 	
 	public void setDetails(Classification classification) {
 		VerbDetails details = Classification.verbDetails(classification);
 		
 		checkIrregular().setChecked(details.getGroup() == Group.IRREGULAR);
 		checkPronominal().setChecked(details.isReflexive());
 		
 		Transitivity transitivity = details.getTransitivity();
 		if (transitivity != null) {
 			switch (transitivity) {
 			case TRANSITIVE:
 				radioTransitive().setChecked(true);
 				break;
 
 			case INTRANSITIVE:
 				radioIntransitive().setChecked(true);
 				break;
 				
 			case BOTH:
 				radioEither().setChecked(true);
 				break;
 			}
 		}
 	}
 	
 	/**
 	 * Sets the verb we're specifying details for.
 	 * 
 	 * @param verb The verb, in French
 	 */
 	public void setVerb(String verb) {
 		this.verb = verb;
 	}
 	
 	public Classification getClassification() {
 		Group group = Group.IRREGULAR;
 		if (!checkIrregular().isChecked()) {
			if (verb.endsWith("er"))
				group = Group.FIRST;
			else if (verb.endsWith("ir"))
				group = Group.SECOND;
			else if (verb.endsWith("re"))
				group = Group.THIRD;
 		}
 		
 		Transitivity transitivity = null;
 		if (radioTransitive().isChecked())
 			transitivity = Transitivity.TRANSITIVE;
 		else if (radioIntransitive().isChecked()) {
 			transitivity = Transitivity.INTRANSITIVE;
 		} else if (radioEither().isChecked()) {
 			transitivity = Transitivity.BOTH;
 		}
 		
 		boolean reflexive = checkPronominal().isChecked();
 		
 		return Classification.verb(group, transitivity, reflexive);
 	}
 
 	private RadioButton radioTransitive() {
 		return (RadioButton) getView().findViewById(R.id.radioTransitive);
 	}
 
 	private RadioButton radioIntransitive() {
 		return (RadioButton) getView().findViewById(R.id.radioIntransitive);
 	}
 
 	private RadioButton radioEither() {
 		return (RadioButton) getView().findViewById(R.id.radioEither);
 	}
 
 	private CheckBox checkIrregular() {
 		return (CheckBox) getView().findViewById(R.id.checkIrregular);
 	}
 
 	private CheckBox checkPronominal() {
 		return (CheckBox) getView().findViewById(R.id.checkPronominal);
 	}
 }
