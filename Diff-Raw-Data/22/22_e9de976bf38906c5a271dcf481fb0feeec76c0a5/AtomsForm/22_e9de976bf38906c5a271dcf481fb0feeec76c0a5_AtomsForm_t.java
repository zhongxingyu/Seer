 package org.vamdc.portal.session.queryBuilder.forms;
 
 import java.util.ArrayList;
 import org.vamdc.dictionary.Restrictable;
 import org.vamdc.portal.session.queryBuilder.fields.AbstractField;
 import org.vamdc.portal.session.queryBuilder.fields.RangeField;
 import org.vamdc.portal.session.queryBuilder.fields.SimpleField;
 
 public class AtomsForm extends AbstractForm implements QueryForm {
 
 	public AtomsForm(){
 		fields = new ArrayList<AbstractField>();
 		fields.add(new SimpleField(Restrictable.AtomSymbol,"Atom symbol"));
 		fields.add(new SimpleField(Restrictable.InchiKey,"InChIKey"));
 		fields.add(new RangeField(Restrictable.AtomMassNumber,"Mass number"));
 		fields.add(new RangeField(Restrictable.AtomNuclearCharge,"Nuclear charge"));
 		fields.add(new RangeField(Restrictable.IonCharge,"Ion charge"));
 		fields.add(new RangeField(Restrictable.StateEnergy,"StateEnergy"));
 	}
 
 	public String getTitle() { return "Atoms"; }
 
 	public String getView() { return "/xhtml/query/forms/atomsForm.xhtml"; }
 
 
 
 	public String getId() {
 		return "1";
 	}
 
 }
