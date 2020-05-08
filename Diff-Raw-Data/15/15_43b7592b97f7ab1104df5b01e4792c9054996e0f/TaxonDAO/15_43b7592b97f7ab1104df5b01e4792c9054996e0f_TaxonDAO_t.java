 package net.canadensys.dataportal.vascan.dao;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.hibernate.type.CalendarType;
 import org.hibernate.type.IntegerType;
 import org.hibernate.type.StringType;
 
 import net.canadensys.dataportal.vascan.model.TaxonLookupModel;
 import net.canadensys.dataportal.vascan.model.TaxonModel;
 
 /**
  * Interface for accessing taxon concept including TaxonModel and TaxonLookupModel
  * @author canadensys
  *
  */
 public interface TaxonDAO {
 	
 	/**
 	 * Save a TaxonLookupModel. A TaxonLookupModel contains denormalized fields allowing faster access (than joining).
 	 * @return success or not
 	 */
 	public boolean saveTaxonLookup(TaxonLookupModel tlm);
 	
 	/**
 	 * Load a TaxonLookupModel from an identifier. TaxonLookupModel contains denormalized fields allowing faster access (than joining).
 	 * @param taxonId
 	 * @return
 	 */
 	public TaxonLookupModel loadTaxonLookup(Integer taxonId);
 	
 	/**
 	 * Get an Iterator on the TaxonLookupModel matching the criteria 
 	 * @param limitResultsTo 
 	 * @param habitus
 	 * @param taxonid
 	 * @param combination
 	 * @param region
 	 * @param status
 	 * @param rank
 	 * @param includeHybrids
 	 * @param sort
 	 * @return
 	 */
 	public Iterator<TaxonLookupModel> loadTaxonLookup(int limitResultsTo, String habitus, int taxonid, String combination, String[] region, String[] status, String[] rank, boolean includeHybrids, String sort);
 	
 	/**
 	 * Count the taxon matching the criteria 
 	 * @param habitus
 	 * @param taxonid
 	 * @param combination
 	 * @param region
 	 * @param status
 	 * @param rank
 	 * @param includeHybrids
	 * @return the count or null if the query can not be completed
 	 */
 	public Integer countTaxonLookup(String habitus, int taxonid, String combination, String[] region, String[] status, String[] rank, boolean includeHybrids);
 
 	/**
 	 * 
 	 * @param maximumRank
 	 * @return List of Object array [id:integer,calname:String,rank:String]
 	 */
 	public List<Object[]> getAcceptedTaxon(int maximumRank);
 	
 	/**
 	 * Load a TaxonModel from an identifier.
 	 * @param vernacularNameId
 	 * @return
 	 */
 	public TaxonModel loadTaxon(Integer taxonId);
 	
 	/**
 	 * Load a TaxonModel list from a taxon calculated name. More than one TaxonModel can share the same calculated name.
 	 * @param taxonName
 	 * @return
 	 */
 	public List<TaxonModel> loadTaxonByName(String taxonCalculatedName);
 	
 	/**
 	 * Load denormalized taxon data for a collection of ids.
 	 * @param taxonIdList
 	 * @return list of Object with the following content: "id","mdate","status","parentid","url","reference","calnameauthor","author","rank","parentfsn","higherclassification","class",
 			"order","family","genus","subgenus","specificepithet","infraspecificepithet"
 	 */
 	public List<Object[]> loadCompleteTaxonData(List<Integer> taxonIdList);
 
 }
