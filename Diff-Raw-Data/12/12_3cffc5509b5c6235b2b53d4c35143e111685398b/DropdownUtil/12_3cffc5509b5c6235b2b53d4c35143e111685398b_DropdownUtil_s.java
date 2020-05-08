 package gov.nih.nci.camod.webapp.util;
 
 import gov.nih.nci.camod.domain.SexDistribution;
 import gov.nih.nci.camod.domain.Species;
 import gov.nih.nci.camod.domain.Strain;
 import gov.nih.nci.camod.domain.Taxon;
 import gov.nih.nci.camod.service.SexDistributionManager;
 import gov.nih.nci.camod.service.TaxonManager;
 import gov.nih.nci.camod.webapp.action.BaseAction;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 public class DropdownUtil extends BaseAction {
 	
 	/**
 	 * Returns a list of all Species and Strains
 	 * @return speciesStrainList
 	 */
 	public List getSpeciesStrainList()
 	{
 		// TODO: Return the SpeciesList and StrainList somehow to connect to each other, Hashmap ? 
 		// TODO: Once these Constants are stored into session memory, shouldn't have to retrieve these again for a while
 		
 		// Get values for dropdown lists for Species, Strains
 		// First get a list of all taxons
 		// for each taxon, get it's scientificName (it's species name)
 		// for each unique species name retrieve all (if any) strain names
 		TaxonManager taxonManager = (TaxonManager) getBean( "taxonManager" );
		List taxonList = taxonManager.getTaxons();			
 		List speciesNames = new ArrayList();
 		
 		// List containing type Strain Objects, returned from TaxonManager.getStrains()
 		List strainList = null;
 		
 		// List contain strings of strain names
 		List strainNames = new ArrayList();
 		
 		Taxon tmp;
 
 		for ( int i=0; i < taxonList.size(); i++ ) {
 			tmp = (Taxon) taxonList.get(i);
 			
 			if ( tmp.getScientificName() != null ) {
 				// if the speciesName is not already in the List, add it (only get unique names)
 				if ( ! speciesNames.contains( tmp.getScientificName() ) )
 					speciesNames.add( tmp.getScientificName() );	
 			}				
 		}
 		
 		//print out speciesNames	
 		System.out.println( "<DropdownUtil getSpeciesStrainList> SpeciesNames List" );	
 		Collections.sort( speciesNames );
 		for ( int j=0; j < speciesNames.size(); j++ )
 			System.out.println( j + ": " + speciesNames.get( j ).toString() );			
 		
 		Species species = new Species();	
 		
 		// for each speciesName retrieve a list of strain names
 		// TODO: create/store a collection of species and corrisponding names
 		for ( int i=0; i < speciesNames.size(); i++ ) {
 			species.setName( (String) speciesNames.get(i) );
 			
 			strainList = new ArrayList();
 			strainList = taxonManager.getStrains( species );
 
 			System.out.println( "\n\n<DropdownUtil getSpeciesStrainList> StrainNames List" );
 
 			//print out strainNames
 			for ( int j=0; j < strainList.size(); j++ ) {
 				Strain strain = (Strain) strainList.get( j );
 				
 				if ( strain.getName() != null ) {
 					strainNames.add( strain.getName() );
 					//System.out.println( j + ": " + strain.getName() );
 				}
 			}
 			
 			//	TODO: Add additional Strain named 'Other' to every list
 			strainNames.add( " Other, Strain not listed." );
 			
 			//Sort the list in 'abc' order
 			if ( strainNames.size() > 0 )
 				Collections.sort( strainNames );
 			
 			for ( int j=0; j < strainNames.size(); j++ ) {
 				System.out.println( j + ": " + strainNames.get(j) );
 			}						
 			
 			strainNames.clear();
 		}			
 		
 		//temp
 		return speciesNames;		
 	}
 	
 	/**
 	 * Retrieves a list of all sexDistributions
 	 * Returns a list of strings  
 	 * @return genderList
 	 */
 	public List getSexDistributionsList()
 	{
 		List genderList = new ArrayList();
 		
 		// Print a list of all the SexDistributions
 		System.out.println( "\n\n<DropdownUtil getSexDistributionsList> SexDistributionNames List" );
 		
 		SexDistributionManager sexDistributionManager = (SexDistributionManager) getBean( "sexDistributionManager" );
		List sexDistributionNames = sexDistributionManager.getSexDistributions();
 		
 		// TODO: REMOVE DUPLICATES!
 		
 		//Collections.sort( sexDistributionNames );
 		
 		for ( int i=0; i < sexDistributionNames.size(); i++ ) {
 			SexDistribution gender = (SexDistribution) sexDistributionNames.get(i);
 			System.out.println( i + ": " + gender.getType() ); 
 			genderList.add( gender.getType() );
 		}	
 		
 		return genderList;		
 	}
 }
