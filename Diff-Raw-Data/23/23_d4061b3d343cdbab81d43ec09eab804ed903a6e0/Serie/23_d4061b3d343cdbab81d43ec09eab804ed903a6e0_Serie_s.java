 package au.gov.nsw.records.search.model;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.EntityManager;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.TypedQuery;
 
 import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
 import org.apache.commons.lang3.builder.ToStringStyle;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.facet.index.CategoryDocumentBuilder;
 import org.apache.lucene.facet.taxonomy.CategoryPath;
 import org.springframework.format.annotation.DateTimeFormat;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
 import org.springframework.roo.addon.json.RooJson;
 import org.springframework.roo.addon.tostring.RooToString;
 
 import com.google.gson.GsonBuilder;
 import com.google.gson.annotations.Expose;
 
 import au.gov.nsw.records.search.service.DateHelper;
 import au.gov.nsw.records.search.service.LocationHelper;
 import au.gov.nsw.records.search.service.QueryHelper;
 import au.gov.nsw.records.search.service.StringService;
 
 @RooJavaBean
 @RooToString
 @RooJpaActiveRecord(versionField = "")
 @Table(name = "series_view")
 @RooJson
 public class Serie implements Serializable{
 
 	@Expose
   @Id
 	@Column(name = "Series_number")
 	private int seriesNumber;
 
 	@Expose
   @Column(name = "Series_title")
 	private String title;
 
 	@Expose
   @Column(name="Descriptive_note")
 	private String descriptiveNote;
 
 	@Expose
   @Column(name="Format")
 	private String format;
 
 	@Expose
   @Column(name="Arrangement")
 	private String arrangement;
 
 	@Expose
   @Column(name="Copies")
 	private String copies;
 
 	@Expose
   @Column(name="Series_control_status")
 	private String seriesControlStatus;
 
 	@Expose
   @Column(name="Access_note")
 	private String accessNote;
 
 	@Expose
   @Column(name="Repository")
 	private String repository;
 
 	@Expose
   @Column(name="Bridging_aids")
 	private String bridgingAids;
 	
 //	@OneToMany(mappedBy="seriesNumber")
 //	private List<Item> items; 
 	
 	@Expose
   @Temporal(TemporalType.TIMESTAMP)
 	@DateTimeFormat(style = "M-")
 	@Column(name = "Last_amendment_date")
 	private Date lastAmendmentDate;
   
 	@Expose
   @Temporal(TemporalType.TIMESTAMP)
 	@DateTimeFormat(style = "M-")
 	@Column(name = "Start_date")
 	private Date startDate;
 
 	@Expose
   @Column(name = "Start_date_qualifier")
 	private String startDateQualifier;
 
 	@Expose
   @Temporal(TemporalType.TIMESTAMP)
 	@DateTimeFormat(style = "M-")
 	@Column(name = "End_date")
 	private Date endDate;
 
 	@Expose
   @Column(name = "End_date_qualifier")
 	private String endDateQualifier;
 
 	@Expose
   @Temporal(TemporalType.TIMESTAMP)
 	@DateTimeFormat(style = "M-")
 	@Column(name = "Contents_start_date")
 	private Date contentStartDate;
 
 	@Expose
   @Column(name = "Contents_start_date_qualifier")
 	private String contentStartDateQualifier;
 
 	@Expose
   @Temporal(TemporalType.TIMESTAMP)
 	@DateTimeFormat(style = "M-")
 	@Column(name = "Contents_End_date")
 	private Date contentEndDate;
 
 	@Expose
   @Column(name = "Contents_End_date_qualifier")
 	private String contentEndDateQualifier;
 
 	@OneToMany(mappedBy="seriesNumber")
 	private List<SerieLinkItem> items;
 	
 	@OneToMany(mappedBy="serieId")
 	private List<SerieLinkActivity> activities;
 	
 	@OneToMany(mappedBy="serieId")
 	private List<SerieLinkControllingAgency> controllingAgencies;
 	
 	@OneToMany(mappedBy="serieId")
 	private List<SerieLinkCreatingAgency> creatingAgencies;
 	
 	@OneToMany(mappedBy="serieId")
 	private List<SerieLinkPerson> persons;
 	
 	@OneToMany(mappedBy="serieId")
 	private List<SerieLinkPrecedingSerie> precedingSeries;
 	
 	@OneToMany(mappedBy="serieId")
 	private List<SerieLinkSucceedingSerie> succeedingSeries;
 	
 	@OneToMany(mappedBy="serieId")
 	private List<SerieLinkRelatedSerie> relatedSeries;
 	
 	@OneToMany(mappedBy="serieId")
 	private List<SerieLinkControllingSerie> controllingSeries;
 	
 	@OneToMany(mappedBy="serieId")
 	private List<SerieLinkControlledSerie> controlledSeries;
 	
 	public String getDateRange() {
 		return DateHelper.dateRange(startDateQualifier, startDate, endDateQualifier, endDate);
 	}
 
 	public String getContentDateRange(){
 		return DateHelper.dateRange(contentEndDateQualifier, contentStartDate, contentEndDateQualifier, contentEndDate);
 	}
 	
 	public String getLocation(){
   	return LocationHelper.getSimpleLocation(repository);
   }
 	
 	public String getLocationAddress(){
 		return LocationHelper.getAddress(repository);
 	}
 	
 	public int getId(){
 		return seriesNumber;
 	}
 	
 	public String getStartDateW3c(){
 		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");  
 		String text = df.format(startDate);  
 		String result = text.substring(0, 22) + ":" + text.substring(22);  
 		return result;
 	}
 	
 	public String getEndDateW3c(){
 		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");  
 		String text = df.format(endDate);  
 		String result = text.substring(0, 22) + ":" + text.substring(22); 
 		return result;
 	}
 	
 	public Long countItems() {
 		return entityManager().createQuery("SELECT COUNT(o) FROM SerieLinkItem o where seriesNumber = " + this.seriesNumber, Long.class).getSingleResult();
  }
 	
 	 public List<SerieLinkItem> getItems(int page, int size) {
 		 return entityManager().createQuery("SELECT o FROM SerieLinkItem o where seriesNumber = " + this.seriesNumber, SerieLinkItem.class).setFirstResult((page-1)*size).setMaxResults(size).getResultList();
   }
 	 
 	public static List<Document> getIndexData(List<Serie> series, CategoryDocumentBuilder builder) throws IOException{
   	List<Document> seriesIndex = new ArrayList<Document>();
   	for (Serie serie:series){
   		Document doc = new Document();
   		Field f = new Field("title", serie.getTitle(), Field.Store.YES, Field.Index.ANALYZED);
   		f.setBoost(2.0f);
   		doc.add(f);
   		
   		Field descriptiveNote = new Field("content", serie.getDescriptiveNote()==null? "" : serie.getDescriptiveNote(), Field.Store.YES, Field.Index.ANALYZED);
   		descriptiveNote.setBoost(1.5f);
   		doc.add(descriptiveNote);
   		
   		doc.add(new Field("type", "series", Field.Store.YES, Field.Index.NOT_ANALYZED));
   		doc.add(new Field("location", serie.getLocation(), Field.Store.YES, Field.Index.NOT_ANALYZED));
   		doc.add(new Field("series", String.valueOf(serie.getId()), Field.Store.YES, Field.Index.NOT_ANALYZED));
   		doc.add(new Field("url", String.format("/series/%d", serie.getSeriesNumber()), Field.Store.YES, Field.Index.NOT_ANALYZED));
   		doc.add(new Field("startyear", DateHelper.getYearString(serie.getStartDate()), Field.Store.YES, Field.Index.NOT_ANALYZED));
   		doc.add(new Field("endyear",  DateHelper.getYearString(serie.getEndDate()), Field.Store.YES, Field.Index.NOT_ANALYZED));
   		
   		List<CategoryPath> categories = new ArrayList<CategoryPath>();
 			categories.add(new CategoryPath("startyear", DateHelper.getYearString(serie.getStartDate())));
 			categories.add(new CategoryPath("endyear", DateHelper.getYearString(serie.getEndDate())));
 			categories.add(new CategoryPath("location", serie.getLocation()));
 			categories.add(new CategoryPath("series", String.valueOf(serie.getId())));
 			
 			builder.setCategoryPaths(categories);
 			builder.build(doc);
 			
   		seriesIndex.add(doc);
   	}
   	return seriesIndex;
 	}
 
 	public static List<Serie> findSeriesFromLastAmendmentDate(Date from, Date until, int page, int pageSize) {
 		String additionalCondition = QueryHelper.buildAdditionalQuery(from, until);
 		EntityManager em = Agency.entityManager();
     TypedQuery<Serie> q = em.createQuery("SELECT o FROM Serie o " + additionalCondition, Serie.class);
     if (from!=null){
     	q.setParameter("from", from);	
     }
     if (until!=null){
     	q.setParameter("until", until);
     }
    return q.setFirstResult((page-1)*pageSize).setMaxResults(pageSize*page).getResultList();
  }
  
 	public static long countSeriesFromLastAmendmentDate(Date from, Date until) {
 		String additionalCondition = QueryHelper.buildAdditionalQuery(from, until);
  		EntityManager em = Agency.entityManager();
     TypedQuery<Long> q = em.createQuery("SELECT count(o) FROM Serie o " + additionalCondition, Long.class);
     if (from!=null){
      	q.setParameter("from", from);	
      }
      if (until!=null){
      	q.setParameter("until", until);
      }
      return q.getSingleResult();
  }
 	
 	public String toString() {
         return ReflectionToStringBuilder.toString(this.getTitle(), ToStringStyle.SHORT_PREFIX_STYLE);
     }
 	
 	 public String getJsonString(){
   	 return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(this); 
    }
 
 	public String getDescriptiveNote() {
         return this.descriptiveNote;
     }
 	
 	public String getDescriptiveNoteNonHtml() {
     return StringService.stripHTML(this.descriptiveNote);
 }
 }
