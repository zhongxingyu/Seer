 package org.vamdc.portal.entity;
 
 import org.vamdc.portal.entity.species.SpeciesSpecies;
 import org.vamdc.portal.session.queryBuilder.forms.MoleculesForm.MoleculeInfo;
 
 public class SpeciesFacade implements MoleculeInfo{
 	private SpeciesSpecies molecule;
 	public SpeciesFacade(SpeciesSpecies iso){
 		this.molecule=iso;
 	}
 	
 	@Override
	public String getFormula(){ return molecule.getStoichiometricFormula(); }
 	@Override
 	public String getInchiKey() { return molecule.getInChIkey(); }
 	@Override
 	public String getDescription() { return molecule.getCommonName(); }
 	@Override
 	public String getName() { return molecule.getCommonName(); }
 	@Override
 	public String getOrdinaryFormula() { return molecule.getOrdinaryFormula(); }
 
 }
