 package org.vamdc.portal.session.queryBuilder.forms;
 
 import java.util.ArrayList;
 
 import org.vamdc.dictionary.Restrictable;
 import org.vamdc.portal.session.queryBuilder.QueryData;
 import org.vamdc.portal.session.queryBuilder.fields.AbstractField;
 import org.vamdc.portal.session.queryBuilder.fields.RangeField;
 import org.vamdc.portal.session.queryBuilder.fields.SimpleField;
 import org.vamdc.portal.session.queryBuilder.fields.UnitConvRangeField;
 import org.vamdc.portal.session.queryBuilder.unitConv.EnergyUnitConverter;
 
 public class AtomsForm extends AbstractForm implements Form {
 
 	public String getTitle() { return "Atoms"; }
 	public Integer getOrder() { return Order.Atoms; }
 	public String getView() { return "/xhtml/query/forms/standardForm.xhtml"; }
 	
 	public AtomsForm(QueryData queryData){
 		super(queryData);
 		fields = new ArrayList<AbstractField>();
 		fields.add(new SimpleField(Restrictable.AtomSymbol,"Atom symbol"));
 		fields.add(new SimpleField(Restrictable.InchiKey,"InChIKey"));
 		fields.add(new RangeField(Restrictable.AtomMassNumber,"Mass number"));
 		fields.add(new RangeField(Restrictable.AtomNuclearCharge,"Nuclear charge"));
 		fields.add(new RangeField(Restrictable.IonCharge,"Ion charge"));
 		fields.add(new UnitConvRangeField(
				Restrictable.StateEnergy, "Upper state energy", new EnergyUnitConverter()));
 	}
 
 }
