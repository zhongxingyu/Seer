 package net.eledge.android.eu.europeana.search.model.record;
 
 import java.util.Map;
 
 import net.eledge.android.eu.europeana.search.model.enums.DocType;
 import net.eledge.android.toolkit.json.annotations.JsonField;
 
 public class Record {
 	
 	@JsonField(".about")
 	public String id;
 	
 	@JsonField
 	public String[] title;
 	
	@JsonField(value=".proxies[].edmType", enumMethod="safeValueOf")
 	public DocType type;
 	
 	@JsonField
 	public String[] year;
 	
 	@JsonField(".proxies[].dcCreator.def")
 	public String[] dcCreator;
 
 	@JsonField(".proxies[].dcDescription")
 	public Map<String, String[]> dcDescription;
 	
 	@JsonField(".proxies[].dcIdentifier.def")
 	public String[] dcIdentifier;
 	
 	@JsonField(".proxies[].dcSubject.def")
 	public String[] dcSubject;
 	
 	@JsonField(".proxies[].dcType.def")
 	public String[] dcType;
 	
	@JsonField("europeanaAggregation.edmPreview")
 	public String edmPreview;
 	
	@JsonField(".aggregations[].edmIsShownBy")
 	public String edmIsShownBy;
 	
	@JsonField(".aggregations[].edmIsShownAt")
 	public String edmIsShownAt;
 	
 	@JsonField(".aggregations[].edmDataProvider.def")
 	public String[] edmDataProvider;
 	
 	@JsonField(".aggregations[].edmProvider.def")
 	public String[] edmProvider;
 	
 //	@JsonField("aggregations[].webResources[].about")
 //	public String[] webResources;
 	
	@JsonField(".places[].longitude")
 	public Double longitude;
 
	@JsonField(".places[].latitude")
 	public Double latitude;
 
 }
