 package org.iucn.sis.server.extensions.reports;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.hibernate.Session;
 import org.iucn.sis.server.api.persistance.SISPersistentManager;
 import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
 import org.iucn.sis.server.api.utils.LookupLoader;
 import org.iucn.sis.shared.api.criteriacalculator.ExpertResult.ResultCategory;
 import org.iucn.sis.shared.api.models.Assessment;
 import org.iucn.sis.shared.api.models.CommonName;
 import org.iucn.sis.shared.api.models.Field;
 import org.iucn.sis.shared.api.models.PrimitiveField;
 import org.iucn.sis.shared.api.models.Reference;
 import org.iucn.sis.shared.api.models.Synonym;
 import org.iucn.sis.shared.api.models.Taxon;
 import org.iucn.sis.shared.api.models.User;
 import org.iucn.sis.shared.api.models.fields.ProxyField;
 import org.iucn.sis.shared.api.models.fields.RedListCreditedUserField;
 import org.iucn.sis.shared.api.models.fields.RedListCriteriaField;
 import org.iucn.sis.shared.api.utils.CanonicalNames;
 import org.iucn.sis.shared.api.utils.CaseInsensitiveAlphanumericComparator;
 
 import com.solertium.util.portable.PortableAlphanumericComparator;
 
 
 public abstract class ReportTemplate {
 	
 	protected final Session session;
 	protected final Assessment assessment;
 	
 	protected final StringBuilder html;
 	
 	public ReportTemplate(Session session, Assessment assessment, String template) throws IOException {
 		this.session = session;
 		this.assessment = assessment;
 		this.html = new StringBuilder(new HTMLReader(template).getHTML());
 	}
 	
 	public abstract void build();
 	
 	protected void setReportValue(String key, String value) {
 		String find = "#" + key;
 		String replace = value == null ? "" : value;
 		int index;
 		if ((index = html.indexOf(find)) > -1)
 			html.replace(index, index+find.length(), replace);
 	}
 
 	@SuppressWarnings("unchecked")
 	protected String fetchSubFieldValues(Field field, String canonicalName){
 		String returnStr = "";		
 	
 		if (field != null && field.getFields() != null) {
 			StringBuilder builder = new StringBuilder();
 			List<String> list = new ArrayList<String>();
 			String value = "";
 			String topValue = "";
 			int dotCount = 0;
 			for (Field subfield : field.getFields()) {
 				value = "";
 				dotCount = 0;
 				PrimitiveField lookup = subfield.getPrimitiveField(canonicalName+"Lookup");
 					
 				value = LookupLoader.get(subfield.getName(), lookup.getName(), Integer.valueOf(lookup.getRawValue()).intValue(),true);
 				if (value != null) {
 					dotCount = value.replaceAll("[^.]", "").length();
 					if(dotCount > 0){
 						topValue = "";
 						topValue = LookupLoader.getByRef(value.substring(0,1), lookup.getName());
 						if(!list.contains(topValue))
 							list.add(topValue);
 						int j = 3;
 						for(int i =1;i < dotCount; i++){
 							topValue = "";
 							topValue = LookupLoader.getByRef(value.substring(0, j), lookup.getName());
 							if(!list.contains(topValue))
 								list.add(topValue);
 							j += 2;
 						}
 					}				
 					list.add(value);
 				}
 			}
 			if(!list.isEmpty()){
 				if(!list.contains(null)){
 					Collections.sort(list,new PortableAlphanumericComparator());
 				}
 				for (String row : list)
 					builder.append(row + "<br/>");
 				
 				returnStr = builder.toString();
 			}else
 				returnStr = "-";
 			
 		}else
 			returnStr = "-";
 		return returnStr;	
 	}
 	
 	@SuppressWarnings("unchecked")
 	protected String fetchCountrySubFieldValues(Field field, String canonicalName){
 		String returnStr = "";
 	
 		if (field != null && field.getFields() != null) {
 			Map<String, List<String>> dataMap = new HashMap<String, List<String>>();
 			
 			for (Field subfield : field.getFields()) {
 				PrimitiveField lookup = subfield.getPrimitiveField(canonicalName+"Lookup"); 
 				PrimitiveField originlookup = subfield.getPrimitiveField("origin"); 
 				
 				String origin = LookupLoader.get(canonicalName, "origin", Integer.valueOf(originlookup.getRawValue()).intValue(),false);
 				String country = LookupLoader.get(subfield.getName(), lookup.getName(), Integer.valueOf(lookup.getRawValue()).intValue(),false);
 				
 				if(dataMap.containsKey(origin)){
 					List<String> tempList = new ArrayList<String>();
 					tempList.addAll(dataMap.get(origin));
 					tempList.add(country); 							
 					dataMap.put(origin, tempList);
 				}else{
 					List<String> tempList = new ArrayList<String>();
 					tempList.add(country);
 					dataMap.put(origin, tempList);
 				}	
 			}
 				
 			String countryStr = "";
 			StringBuilder builder = new StringBuilder();
 			for (Map.Entry<String, List<String>> entry : dataMap.entrySet()) {
 				if (entry.getValue() != null) {
 					builder.append("<b>"+entry.getKey()+"</b>: <br/>");
 					countryStr = "";
 					Collections.sort(entry.getValue(),new PortableAlphanumericComparator());
 					for(String country : entry.getValue())
 						countryStr += country+"; ";
 					
 					builder.append(countryStr+"<br/>");
 				}
 			}
 			returnStr = builder.toString();
 		}else
 			returnStr = "-";
 
 		return returnStr;		
 	}	
 		
 	protected String fetchTaxaFootPrints(String[] footprints, int index) {
 		if(footprints[index] != null && footprints[index] != "")
 			return footprints[index];
 		else
 			return "-";
 	}
 	
 	protected void buildTaxonomy(){
 		Taxon taxa = assessment.getTaxon();
 		setReportValue("REPORT_TITLE",taxa.getFullName());
 		setReportValue("SCIENTIFIC_NAME",taxa.getFullName());
 		setReportValue("KINGDOM",fetchTaxaFootPrints(taxa.getFootprint(),0));
 		setReportValue("PHYLUM",fetchTaxaFootPrints(taxa.getFootprint(),1));
 		setReportValue("CLASS",fetchTaxaFootPrints(taxa.getFootprint(),2));
 		setReportValue("ORDER",fetchTaxaFootPrints(taxa.getFootprint(),3));
 		setReportValue("FAMILY",fetchTaxaFootPrints(taxa.getFootprint(),4));
 		setReportValue("SPECIES_AUTHORITY",taxa.getTaxonomicAuthority());
 		setReportValue("COMMON_NAMES",fetchCommonNames(taxa));
 		setReportValue("SYNONYMS",fetchSynonyms(taxa));
 		setReportValue("TAXA_NOTES",fetchTextPrimitiveField(assessment.getField(CanonicalNames.TaxonomicNotes), "value"));
 	}
 	
 	protected String fetchCommonNames(Taxon taxon) {
 		String commonNames = "";
 		
 		if (taxon.getCommonNames().isEmpty()) {
 			commonNames = "-";
 		} else {
 			StringBuilder builder = new StringBuilder();
 			Set<CommonName> temp = taxon.getCommonNames();
 			for (CommonName cur : temp) {
 				if(cur != null && cur.getIso() != null)
 					builder.append("&nbsp;&nbsp;&nbsp;"+cur.getLanguage()+" - "+cur.getName()+"<br/>");
 				
 			}
 			commonNames = builder.toString();
 		}
 		return commonNames;
 	}
 	
 	protected String fetchSynonyms(Taxon taxon) {
 		String synonyms = "";
 
 		if (taxon.getSynonyms().isEmpty()) {
 			synonyms = "-";
 		} else {
 			StringBuilder builder = new StringBuilder();
 			Set<Synonym> temp = taxon.getSynonyms();
 			for (Synonym cur : temp) {
 				if(cur != null && cur.getFriendlyName() != null)
 					builder.append(cur.getFriendlyName()+"<br/>");
 			}
 			synonyms = builder.toString();
 		}
 
 		return synonyms;
 	}
 	
 	protected void buildBibliography() {
 		setReportValue("BIBLIOGRAPHY",fetchReferences());
 	}	
 	
 	protected void buildCitation() {
 		setReportValue("CITATION",fetchCitation());
 	}
 	
 	protected String fetchCitation() {
 		String referenceStr = "";
 		StringBuilder builder = new StringBuilder();
 		List<String> list = new ArrayList<String>();
 		Field field = assessment.getField(CanonicalNames.RedListSource);
 		if(field != null && field.getReference() != null){			
 			Set<Reference> ref = field.getReference();
 			if(!ref.isEmpty()){
 				for (Reference cur : ref) {
 					if(cur != null){
 						if(cur.getCitation() != null && !cur.getCitation().isEmpty())
 							list.add(cur.getCitation());
 					}
 				}
 			}
 		}
 		if(!list.isEmpty()){
 			Collections.sort(list, new PortableAlphanumericComparator());
 			for (String row : list)
 				builder.append(row + "<br/>");
 			
 			referenceStr = builder.toString();
 		}else
 			referenceStr = "-";
 		return referenceStr;
 	}
 	
 	protected String fetchReferences() {
 		String referenceStr = "";
 
 		Set<String> references = new HashSet<String>();
 		if (assessment.getReference() != null){
 			for (Reference reference : assessment.getReference()) {
 				String citation = reference.generateCitationIfNotAlreadyGenerate();
 				if (citation != null && !"".equals(citation))
 					references.add(citation);
 			}
 		}
 		for (Field field : assessment.getField()) {
 			if (field.getReference() != null) {
 				for (Reference reference : field.getReference()) {
 					String citation = reference.generateCitationIfNotAlreadyGenerate();
 					if (citation != null && !"".equals(citation))
 						references.add(citation);
 				}
 			}
 		}
 		
 		if (references.isEmpty())
 			referenceStr = "-";
 		else {
 			List<String> values = new ArrayList<String>(references);
 			Collections.sort(values, new CaseInsensitiveAlphanumericComparator());
 			
 			StringBuilder builder = new StringBuilder();
 			for (String row : values)
 				builder.append(row + "<br/>");
 			
 			referenceStr = builder.toString();
 		}
 		
 		return referenceStr;
 	}
 		
 	protected String fetchTextPrimitiveField(Field field, String type){
 
 		String returnStr = "";
 
 		if(field != null){		
 			ProxyField proxy = new ProxyField(field);
 			returnStr = proxy.getTextPrimitiveField(type);			
 		}else
 			returnStr = "-";
 
 		return returnStr;
 	}	
 	
 	protected String fetchStringPrimitiveField(Field field, String type){
 
 		String returnStr = "";
 		
 		if(field != null){		
 			ProxyField proxy = new ProxyField(field);
 			returnStr = proxy.getStringPrimitiveField(type);			
 		}else
 			returnStr = "None";
 		
 		return returnStr;
 	}	
 	
 	protected String fetchDatePrimitiveField(Field field, String type){
 
 		String returnStr = "";
 
 		if(field != null){		
 			ProxyField proxy = new ProxyField(field);
 			returnStr = new SimpleDateFormat("yyyy").format(proxy.getDatePrimitiveField(type));				
 		}else
 			returnStr = "-";
 		
 		return returnStr;
 	}
 	
 	protected String fetchCategoryAndCrieteria(Field field){
 		String catAndCrit = "";
 
 		if (field != null){
 			RedListCriteriaField proxy = new RedListCriteriaField(field);
 			String category = proxy.isManual() ? proxy.getManualCategory() : proxy.getGeneratedCategory();
			String criteria = proxy.isManual() ? proxy.getManualCriteria() : proxy.getGeneratedCriteria();
 			ResultCategory type = ResultCategory.fromString(category);
 			catAndCrit = "".equals(category) ? "-" : type == null ? category : type.getName();
			catAndCrit += "".equals(criteria) ? "  N/A" : "  "+criteria;
 								
 		}else
 			catAndCrit = "-";
 
 		return catAndCrit;
 	}
 	
 	protected String fetchForeignPrimitiveField(Field field){
 
 		String returnVal = "";
 		
 		if(field != null){		
 			ProxyField proxy = new ProxyField(field);
 			returnVal = LookupLoader.get(field.getName(), "value", proxy.getForeignKeyPrimitiveField("value"),false);
 		}	
 
 		return returnVal;
 	}
 	
 	protected String fetchUsers(Field field) {
 		String returnVal = "";
 		if (field != null) {
 			RedListCreditedUserField proxy = new RedListCreditedUserField(field);
 			if (!proxy.getUsers().isEmpty()) {
 				List<User> users = new ArrayList<User>();
 				for (Integer id : proxy.getUsers()) {
 					final User user;
 					try {
 						user = SISPersistentManager.instance().getObject(session, User.class, id);
 					} catch (PersistentException e) {
 						continue;
 					}
 					if (user != null)
 						users.add(user);
 				}
 				if (users.isEmpty())
 					returnVal = proxy.getText();
 				else
 					returnVal = RedListCreditedUserField.generateText(users, proxy.getOrder());
 			}
 			else
 				returnVal = proxy.getText();
 		}
 		return returnVal;
 	}
 	
 	@Override
 	public String toString() {
 		return html.toString();
 	}
 
 }
