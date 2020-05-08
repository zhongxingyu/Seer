 package org.vamdc.portal.session.queryBuilder.forms;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.EntityManager;
 
 import org.vamdc.dictionary.Restrictable;
 import org.vamdc.portal.entity.EntityFacade;
 import org.vamdc.portal.session.queryBuilder.fields.AbstractField;
 import org.vamdc.portal.session.queryBuilder.fields.RangeField;
 import org.vamdc.portal.session.queryBuilder.fields.SuggestionField;
 import org.vamdc.portal.session.queryBuilder.fields.TextField;
 
 public class MoleculesForm extends AbstractForm implements Form{
 
 	/**
 	 * Molecule info from species database
 	 * 
 	 */
 	public interface MoleculeInfo{
 		public String getName();
 		public String getFormula();
 		public String getOrdinaryFormula();
 		public String getInchiKey();
 		public String getDescription();
 	}
 
 	private List<MoleculeInfo> molecules = Collections.emptyList();
 
 	private AbstractField molChemName;
 	private AbstractField molStoichForm;
 	private AbstractField molOrdForm;
 	private AbstractField inchikey;
 
 	private Map<String,Boolean> inchikeys = new HashMap<String,Boolean>();
 
 	private static final long serialVersionUID = 3499663104107031985L;
 	@Override
 	public String getTitle() { return "Molecules"; }
 	@Override
 	public Integer getOrder() { return Order.Molecules; }
 	@Override
 	public String getView() { return "/xhtml/query/forms/moleculesForm.xhtml"; }
 
 	public MoleculesForm(){
		molChemName = new SuggestionField(Restrictable.MoleculeChemicalName,"Chemical name", new ChemNameSuggestion());
 		addField(molChemName);
 
 		molStoichForm = new SuggestionField(Restrictable.MoleculeStoichiometricFormula,"Stoichiometric formula", new StoichFormSuggestion()); 
 		addField(molStoichForm);
 
 		molOrdForm = new SuggestionField(null,"Structural formula", new StructFormSuggestion());
 		addField(molOrdForm);
 
 		addField(new RangeField(Restrictable.IonCharge,"Ion charge"));
 
 		inchikey = new TextField(Restrictable.InchiKey,"InChIKey");
 		addField(inchikey);
 	}
 
 	@Override
 	public void clear(){
 		super.clear();
 		resetInchiKeys();
 		molecules = Collections.emptyList();
 	}
 
 	public List<MoleculeInfo> getMolecules() { return molecules; }
 
 	public Map<String,Boolean> getSelectedInchikeys() {	return inchikeys; }
 	public void setSelectedInchikeys(Map<String,Boolean> inchikeys) { this.inchikeys = inchikeys; }
 
 	private void fillFromMolecule(MoleculeInfo molecule){
 		this.molChemName.setValue(molecule.getName());
 		this.molStoichForm.setValue(molecule.getFormula());
 		this.molOrdForm.setValue(molecule.getOrdinaryFormula());
 	}
 
 	public void selectedInchi(){
 		this.inchikey.setValue(buildInchiList(inchikeys));
 		ignoreNameFields(this.inchikey.getValue().length()>0);
 		queryData.resetCaches();
 	}
 
 	private void ignoreNameFields(boolean ignore){
 		this.molChemName.setIgnoreField(ignore);
 		this.molStoichForm.setIgnoreField(ignore);
 	}
 
 	private void resetInchiKeys(){
 		inchikeys = new HashMap<String,Boolean>();
 		inchikey.setValue("");
 		if (molecules.size()==1){
 			inchikey.setValue(molecules.get(0).getInchiKey());
 			molecules = Collections.emptyList();
 			ignoreNameFields(true);
 		}else
 			ignoreNameFields(false);
 	}
 
 	private String buildInchiList(Map<String, Boolean> inchikeys) {
 		String result = "";
 		for (String inchikey:inchikeys.keySet()){
 			if (inchikeys.get(inchikey))
 				result+=inchikey+", ";
 		}
 		if (result.length()>2)
 			result = result.substring(0, result.length()-2);
 		return result;
 	}
 
 
 
 	public class ChemNameSuggestion implements SuggestionField.Suggestion{
 
 		private static final long serialVersionUID = 7286322175153115141L;
 
 		@Override
 		public Collection<String> options(Object input) {
 			return EntityFacade.suggestChemicalName(queryData.getEntityManager(),(String)input);
 		}
 
 		@Override
 		public String getIllegalLabel() {
 			return "not found in the database";
 		}
 
 		@Override
 		public void selected() {
 			EntityManager em = queryData.getEntityManager();
 			molecules = EntityFacade.loadMoleculesFromName(em, molChemName.getValue());
 			fillFromMolecule(EntityFacade.getMolecInfoFromID(em, EntityFacade.getSpeciesIDfromName(em, molChemName.getValue())));
 			resetInchiKeys();
 		}
 	}
 
 
 	public class StoichFormSuggestion implements SuggestionField.Suggestion{
 
 		private static final long serialVersionUID = -1570905697531370200L;
 
 		@Override
 		public Collection<String> options(Object input) {
 			return EntityFacade.suggestStoichiometricFormula(queryData.getEntityManager(),(String)input);
 		}
 
 		@Override
 		public String getIllegalLabel() {
 			return "not found in the database";
 		}
 
 		@Override
 		public void selected() {
 			EntityManager em = queryData.getEntityManager();
 			molecules = EntityFacade.loadMoleculesFromStoichForm(em, molStoichForm.getValue());
 			fillFromMolecule(EntityFacade.getMolecInfoFromID(em, EntityFacade.getSpeciesIDfromStoichForm(em, molStoichForm.getValue())));
 			resetInchiKeys();
 		}
 	}
 
 	public class StructFormSuggestion implements SuggestionField.Suggestion{
 
 		private static final long serialVersionUID = -1570905697531370201L;
 
 		@Override
 		public Collection<String> options(Object input) {
 			return EntityFacade.suggestOrdinaryFormula(queryData.getEntityManager(),(String)input);
 		}
 
 		@Override
 		public String getIllegalLabel() {
 			return "not found in the database";
 		}
 
 		@Override
 		public void selected() {
 			EntityManager em = queryData.getEntityManager();
 			molecules = EntityFacade.loadMoleculesFromOrdForm(em, molOrdForm.getValue());
 			fillFromMolecule(EntityFacade.getMolecInfoFromID(em, EntityFacade.getSpeciesIDfromOrdForm(em, molOrdForm.getValue())));
 			resetInchiKeys();
 		}
 	}
 
 
 }
