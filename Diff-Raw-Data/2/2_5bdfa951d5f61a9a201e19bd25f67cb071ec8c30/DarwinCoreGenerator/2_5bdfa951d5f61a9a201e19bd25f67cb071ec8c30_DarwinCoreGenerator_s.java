 package net.canadensys.dataportal.vascan.generatedcontent;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.ResourceBundle;
 
 import net.canadensys.dataportal.vascan.constant.Status;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 /**
  * Class responsible for generating a Darwin Core content.
  * ToDo : This class relies on index in List<Object[]> parameters, we should use a key/value instead.
  * ToDo : For scaling reason, we should write into a buffer instead of returning a StringBuffer.
  * @author canadensys
  *
  */
 @Component
 public class DarwinCoreGenerator {
 	
 	private String	DELIMITER = "\t";
 	private String	NEWLINE = "\n";
 	
 	private static SimpleDateFormat SDF_DATE = new SimpleDateFormat("yyyy-MM-dd");
 	
 	@Autowired
 	private GeneratedContentConfig generatedContentConfig;
 	
 	/**
 	 * Generates Taxon DarwinCore content.
 	 * @param taxonData
 	 * @param locale
 	 * @param displayHeader
 	 * @return content as StringBuffer
 	 */
 	public StringBuffer generateTaxonDwc(List<Object[]> taxonData, ResourceBundle locale, boolean displayHeader){
 		StringBuffer dwc = new StringBuffer("");
 		SimpleDateFormat sdfTime = new SimpleDateFormat("H:mmZ");
 			
 		// TAXON fields
 		String 		index;
 		String		modified;
 		String		bibliographicCitation;
 		String		references;
 		String		taxonID;
 		String		acceptedNameUsageID;
 		String		parentNameUsageID;
 		String		nameAccordingToID;
 		String		scientificName;
 		String		acceptedNameUsage;
 		String		parentNameUsage;
 		String		nameAccordingTo;
 		String		higherClassification;
 		String		_class;
 		String		order;
 		String		family;
 		String		genus;
 		String		subgenus;
 		String		taxonRank;
 		String		scientificNameAuthorship;
 		String		taxonomicStatus;
 		String		specificEpithet;
 		String		infraSpecificEpithet;
 		
 		
 		if(taxonData != null){
 			/* first line header */
 			if(displayHeader)
 				dwc.append(locale.getString("dwc_header_taxon")).append(NEWLINE);
 			
 			/* 
 			 * in some rare occasions, a child will have multiple parents, more than likely a synonym with two parents
 			 * because of the left joins in the query, that synonym will appear twice in the list of results (actually
 			 * once for every parent... 2 parents = 2 times, 3 parents = 3 times etc);
 			 * the correct way to represent theses in dwc is to have a single line for the child, and pipe concatenate
 			 * the parent ids and names ... 20158	asynonymname	1234|2345|4444	parentname1|parentname2|parentname3 
 			 * 
 			 * to achieve this, we simply store the previous line taxon id, and if the current id matches the previous one
 			 * we modify the previous line instead of creating a new one 
 			 * 
 			 */
 			int previousTaxonId = 0;
 			ArrayList<HashMap<String,String>> taxonList = new ArrayList<HashMap<String,String>>();
 			taxonList.ensureCapacity(taxonData.size());
 			for(Object[] taxon : taxonData){
 				if((Integer)taxon[0] == previousTaxonId){
 					int previousEntry = taxonList.size()-1;
 					HashMap<String,String> t = taxonList.get(previousEntry);
 					t.put("parentNameUsageID", (String)t.get("parentNameUsageID") + "|" + String.valueOf((Integer)taxon[3]));
 					t.put("parentNameUsage", (String)t.get("parentNameUsage") + "|" + (String)taxon[9]);
 					taxonList.set(previousEntry, t);
 				}
 				else{
 					previousTaxonId = (Integer)taxon[0];
 					HashMap<String,String> t = new HashMap<String,String>();
 					t.put("taxonID", String.valueOf((Integer)taxon[0]));
 					t.put("index", String.valueOf((Integer)taxon[0]));
 					t.put("modified", SDF_DATE.format(((Calendar)taxon[1]).getTime()).concat("T").concat(sdfTime.format(((Calendar)taxon[1]).getTime())));
 					t.put("references", generatedContentConfig.getTaxonUrl().concat(String.valueOf((Integer)taxon[0])));
 					t.put("acceptedNameUsageID", String.valueOf((Integer)taxon[0]));
 					t.put("parentNameUsageID", taxon[3] != null ? String.valueOf((Integer)taxon[3]) : "");
 					t.put("nameAccordingToID", (String)taxon[4]);
 					t.put("scientificName", (String)taxon[6]);
 					t.put("acceptedNameUsage", (String)taxon[6]);
 					t.put("parentNameUsage", (String)taxon[9]);
 					t.put("nameAccordingTo", (String)taxon[5]);
 					t.put("taxonRank", ((String)taxon[8]).toLowerCase());
 					t.put("scientificNameAuthorship", (String)taxon[7]);
 					t.put("taxonomicStatus", ((String)taxon[2]).toLowerCase());
 					t.put("higherClassification", (String)taxon[10]);
 					t.put("class", (String)taxon[11]);
 					t.put("order", (String)taxon[12]);
 					t.put("family", (String)taxon[13]);
 					t.put("genus", (String)taxon[14]);
 					t.put("subgenus", (String)taxon[15]);
 					t.put("specificEpithet", (String)taxon[16]);
 					t.put("infraSpecificEpithet", (String)taxon[17]);
 					taxonList.add(t);
 				}
 			}
 			
 			for(HashMap<String,String> taxon : taxonList){				
 				if((taxonID = taxon.get("taxonID")) == null)
 					taxonID = "";
 				if((index = taxon.get("index")) == null)
 					index = "";
 				if((modified = taxon.get("modified")) == null)
 					modified = "";
 				if((references = taxon.get("references")) == null)
 					references = "";
 				if((acceptedNameUsageID = taxon.get("acceptedNameUsageID")) == null)
 					acceptedNameUsageID = "";
 				if((parentNameUsageID = taxon.get("parentNameUsageID")) == null)
 					parentNameUsageID = "";
 				if((nameAccordingToID = taxon.get("nameAccordingToID")) == null)
 					nameAccordingToID = "";
 				if((scientificName = taxon.get("scientificName")) == null)
 					scientificName = "";
 				if((acceptedNameUsage = taxon.get("acceptedNameUsage")) == null)
 					acceptedNameUsage = "";
 				if((parentNameUsage = taxon.get("parentNameUsage")) == null)
 					parentNameUsage = "";
 				if((nameAccordingTo = taxon.get("nameAccordingTo")) == null)
 					nameAccordingTo = "";
 				if((taxonRank = taxon.get("taxonRank")) == null)
 					taxonRank = "";
 				if((scientificNameAuthorship = taxon.get("scientificNameAuthorship")) == null)
 					scientificNameAuthorship = "";
 				if((taxonomicStatus = taxon.get("taxonomicStatus")) == null)
 					taxonomicStatus = "";
 				if((higherClassification = taxon.get("higherClassification")) == null)
 					higherClassification = "";
 				if((_class = taxon.get("class")) == null)
 					_class = "";
 				if((order = taxon.get("order")) == null)
 					order = "";
 				if((family = taxon.get("family")) == null)
 					family = "";
 				if((genus = taxon.get("genus")) == null)
 					genus = "";
 				if((subgenus = taxon.get("subgenus")) == null)
 					subgenus = "";
 				if((specificEpithet = taxon.get("specificEpithet")) == null)
 					specificEpithet = "";
 				if((infraSpecificEpithet = taxon.get("infraSpecificEpithet")) == null)
 					infraSpecificEpithet = "";
 				
 				bibliographicCitation = String.format(locale.getString("dwc_bibliographic_citation"),scientificName,references,SDF_DATE.format(Calendar.getInstance().getTime()));
 				
 				// for a synonym, empty parentNameUsageId and parentNameUsage 
 				// and rely solely on accepteNameUsageId and acceptedName
 				if(taxonomicStatus.equals(Status.SYNONYM)){
 					acceptedNameUsageID = parentNameUsageID;
 					acceptedNameUsage = parentNameUsage;
 					parentNameUsageID = "";
 					parentNameUsage = "";
 				}
 				
 				/*********************************************
 				 * 
 				 *  ARCHIVE writing starts here
 				 * 
 				 ********************************************/
 				
 				/* index */
 				dwc.append(index).append(DELIMITER);
 				
 				/* modified */
 				dwc.append(modified.toString()).append(DELIMITER);
 				
 				/* bibliographicCitation */
 				dwc.append(bibliographicCitation).append(DELIMITER);
 				
 				/* references */
 				dwc.append(references).append(DELIMITER);
 				
 				/* taxonID */
 				dwc.append(taxonID).append(DELIMITER);
 
 				/* acceptedNameUsageID */
 				dwc.append(acceptedNameUsageID).append(DELIMITER);
 				
 				/* parentNameUsageID */
 				dwc.append(parentNameUsageID).append(DELIMITER);
 				
 				/* nameAccordingToID */
 				dwc.append(nameAccordingToID).append(DELIMITER);
 				
 				/* scientificName */
 				dwc.append(scientificName).append(DELIMITER);
 				
 				/* acceptedNameUsage */
 				dwc.append(acceptedNameUsage).append(DELIMITER);
 				
 				/* parentNameUsage */
 				dwc.append(parentNameUsage).append(DELIMITER);
 				
 				/* nameAccordingTo */
 				dwc.append(nameAccordingTo).append(DELIMITER);
 				
 				/* higherClassification */
 				dwc.append(higherClassification).append(DELIMITER);
 				
 				/* class */
 				dwc.append(_class).append(DELIMITER);
 				
 				/* order */
 				dwc.append(order).append(DELIMITER);
 				
 				/* family */
 				dwc.append(family).append(DELIMITER);
 				
 				/* genus */
 				dwc.append(genus).append(DELIMITER);
 				
 				/* subgenus */
 				dwc.append(subgenus).append(DELIMITER);
 				
 				/* specificEpithet */
 				dwc.append(specificEpithet).append(DELIMITER);
 				
 				/* infraSpecificEpithet */
 				dwc.append(infraSpecificEpithet).append(DELIMITER);
 				
 				/* taxonRank */
 				dwc.append(taxonRank).append(DELIMITER);
 				
 				/* scientificNameAuthorship */
 				dwc.append(scientificNameAuthorship).append(DELIMITER);
 				
 				/* taxonomicStatus */
 				dwc.append(taxonomicStatus).append(NEWLINE);
 			}
 		}
 		return dwc;		
 	}
 	
 	/**
 	 * Generates Distribution DarwinCore content.
 	 * @param distributionData
 	 * @param locale
 	 * @param displayHeader
 	 * @return content as StringBuffer
 	 */
 	public StringBuffer generateDistributionDwc(List<Object[]> distributionData, ResourceBundle locale, boolean displayHeader){
 		StringBuffer dwc = new StringBuffer("");
 		
 		// DISTRIBUTION fields
 		int		index;
 		String	locationID;
 		String	locality;
 		String	countryCode;
 		String	occurrenceStatus;
 		String	establishmentMeans;
 		String 	source;
 		String	occurrenceRemarks;
 
 		if(distributionData != null){
 			/* first line header */
 			if(displayHeader)
 				dwc.append(locale.getString("dwc_header_distribution")).append(NEWLINE);
 			for(Object[] distribution : distributionData){
 				index = (Integer)distribution[0];
 				locationID = (String)distribution[1];
 				locality = locale.getString("province_" + ((String)distribution[2]).toUpperCase());
 				countryCode = (String)distribution[3];
 				occurrenceStatus = (String)distribution[4];
 				establishmentMeans = (String)distribution[5];
 				source = String.valueOf((String)distribution[6] + " " + (String)distribution[7]).trim();
 				if(((String)distribution[8]) != null && !((String)distribution[8]).equals(""))
 					occurrenceRemarks = locale.getString("excluded_" + ((String)distribution[8]).toLowerCase());
 				else
 					occurrenceRemarks = "";
 				/*********************************************
 				 * 
 				 *  ARCHIVE writing starts here
 				 * 
 				 ********************************************/
 				
 				/* index */
 				dwc.append(index).append(DELIMITER);
 				
 				/* locationID */
 				if(locationID != null && !locationID.equals(""))
					dwc.append("ISO3166-2:").append(locationID).append(DELIMITER);
 				else
 					dwc.append("").append(locationID).append(DELIMITER);
 				
 				/* locality */
 				dwc.append(locality).append(DELIMITER);
 				
 				/* countryCode */
 				dwc.append(countryCode).append(DELIMITER);
 				
 				/* occurrenceStatus */
 				dwc.append(occurrenceStatus).append(DELIMITER);
 				
 				/* establishmentMeans */
 				dwc.append(establishmentMeans).append(DELIMITER);
 				
 				/* source */
 				dwc.append(source).append(DELIMITER);
 				
 				/* occurrenceRemarks */
 				if(occurrenceRemarks != null && !occurrenceRemarks.equals(""))
 					dwc.append(occurrenceRemarks).append(NEWLINE);
 				else
 					dwc.append("").append(NEWLINE);
 			}
 		}
 		return dwc;
 	}
 	
 	/**
 	 * Generates Vernacular Names DarwinCore content.
 	 * @param vernaculars
 	 * @param locale
 	 * @param displayHeader
 	 * @return content as StringBuffer
 	 */
 	public StringBuffer generateVernacularDwc(List<Object[]> vernaculars, ResourceBundle locale, boolean displayHeader){
 		StringBuffer dwc = new StringBuffer("");
 		
 		// VERNACULAR fields
 		int		index;
 		String	vernacularName;
 		String	source;
 		String	language;
 		String isPreferredName;
 		
 		if(vernaculars != null){
 			/* first line header */
 			if(displayHeader)
 				dwc.append(locale.getString("dwc_header_vernacular")).append(NEWLINE);
 			for(Object[] vernacular : vernaculars){
 				index = (Integer)vernacular[0];
 				vernacularName = (String)vernacular[1];
 				source = String.valueOf((String)vernacular[2] + " " + (String)vernacular[3]).trim();
 				language = String.valueOf((String)vernacular[4]).toUpperCase();
 				if((Integer)vernacular[5] == Status.ACCEPTED){
 					isPreferredName = "true";
 				}
 				else{
 					isPreferredName = "false";
 				}
 				
 				/*********************************************
 				 * 
 				 *  ARCHIVE writing starts here
 				 * 
 				 ********************************************/
 				
 				/* index */
 				dwc.append(index).append(DELIMITER);
 				
 				/* vernacularName */
 				dwc.append(vernacularName).append(DELIMITER);
 				
 				/* source */
 				dwc.append(source).append(DELIMITER);
 				
 				/* language */
 				dwc.append(language).append(DELIMITER);
 				
 				/* isPreferredName */
 				dwc.append(isPreferredName).append(NEWLINE);
 			}
 		}
 		return dwc;
 	}
 	
 	/**
 	 * Generates Habit DarwinCore content.
 	 * @param habitMap
 	 * @param locale
 	 * @param displayHeader
 	 * @return content as StringBuffer
 	 */
 	public StringBuffer generateHabitusDwc(Map<Integer,String> habitMap, ResourceBundle locale, boolean displayHeader){
 		StringBuffer dwc = new StringBuffer("");
 		
 		int		index;
 		String	description;
 
 		/* first line header */
 		if(displayHeader){
 			dwc.append(locale.getString("dwc_header_habit")).append(NEWLINE);
 		}
 		for(Integer taxonId : habitMap.keySet()){
 			index = taxonId;
 			description = habitMap.get(taxonId);
 
 			/*********************************************
 			 * 
 			 *  ARCHIVE writing starts here
 			 * 
 			 ********************************************/
 			
 			/* index */
 			dwc.append(index).append(DELIMITER);
 			
 			/* description */
 			dwc.append(description).append(NEWLINE);
 		}
 		return dwc;
 	}
 
 }
