 package org.vamdc.portal.session.queryBuilder.forms;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.EntityManager;
 
 import org.vamdc.dictionary.Restrictable;
 import org.vamdc.portal.entity.EntityFacade;
 import org.vamdc.portal.session.queryBuilder.fields.AbstractField;
 import org.vamdc.portal.session.queryBuilder.fields.SuggestionField;
 import org.vamdc.portal.session.queryBuilder.fields.SuggestionImpl;
 import org.vamdc.portal.session.queryBuilder.fields.TextField;
 
 public class MoleculesForm extends SpeciesForm implements Form{
 
 	/**
 	 * Molecule info from species database
 	 * 
 	 */
 	public interface MoleculeInfo{
 		public String getName();
 		public String getFormula();
 		public String getOrdinaryFormula();
 		public String getInchiKey();
 		public String getVamdcSpeciesID();
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
	public String getTitle() { return "Molecule "+ this.getPosition(); }
 	@Override
 	public Integer getOrder() { return Order.Molecules; }
 	@Override
 	public String getView() { return "/xhtml/query/forms/moleculesForm.xhtml"; }
 
 	public MoleculesForm(){
 		super();
         
 		molChemName = new SuggestionField(null,"Chemical name", new ChemNameSuggestion());
 		addField(molChemName);
 
 		molStoichForm = new SuggestionField(Restrictable.MoleculeStoichiometricFormula,"Stoichiometric formula", new StoichFormSuggestion()); 
 		addField(molStoichForm);
 
 		molOrdForm = new SuggestionField(null,"Structural formula", new StructFormSuggestion());
 		addField(molOrdForm);
 		
 		addField(new SuggestionField(Restrictable.MoleculeStateNuclearSpinIsomer,"Spin isomer",new SymmetrySuggest()));
 
 		inchikey = new TextField(Restrictable.InchiKey,"Standard InChIKey");
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
 		queryData.resetCaches();
 	}
 	
 	public String selectAllInchi(){
 		inchikeys = new HashMap<String,Boolean>();
 		for (MoleculeInfo molecule:molecules){
 			inchikeys.put(molecule.getInchiKey(), true);
 		}
 		selectedInchi();
 		return "";
 	}
 	
 	public void selectNoneInchi(){
 		inchikeys = new HashMap<String,Boolean>();
 		for (MoleculeInfo molecule:molecules){
 			inchikeys.put(molecule.getInchiKey(), false);
 		}
 		selectedInchi();
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
 			fillFromMolecules();
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
 			fillFromMolecules();
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
 			fillFromMolecules();
 			resetInchiKeys();
 		}
 	}
 	
 	/**
 	 * Fill fields from molecules list
 	 */
 	private void fillFromMolecules(){
 		if (molecules.isEmpty()){
 			this.molChemName.setValue("");
 			this.molStoichForm.setValue("");
 			this.molOrdForm.setValue("");
 		}
 		if (molecules.size()==1||moleculesContainSameName()){
 			this.molChemName.setValue(molecules.get(0).getName());
 		}else{
 			this.molChemName.setValue("");
 		}
 		this.molStoichForm.setValue(molecules.get(0).getFormula());
 		if (molecules.size()==1)
 			this.molOrdForm.setValue(molecules.get(0).getOrdinaryFormula());
 		else
 			this.molOrdForm.setValue("");
 	}
 	
 	private boolean moleculesContainSameName(){
 		String firstName=molecules.get(0).getName();
 		for(MoleculeInfo mol:molecules){
 			if (!firstName.equalsIgnoreCase(mol.getName()))
 				return false;
 		}
 		return true;
 	}
 	
 	public class SymmetrySuggest extends SuggestionImpl{
 		private static final long serialVersionUID = 8382996745391164997L;
 
 		@Override
 		protected Collection<String> loadValues() {
 			Collection<String> result = new ArrayList<String>();
 			result.add("ortho");
 			result.add("para");
 			result.add("meta");
 			result.add("A");
 			result.add("E");
 			return result;
 		}
 		
 	}
 
 
 }
