 package au.gov.nsw.records.search.model;
 
 import au.gov.nsw.records.search.service.DateHelper;
 import au.gov.nsw.records.search.service.QueryHelper;
 import au.gov.nsw.records.search.service.StringService;
 
 import java.awt.font.TextHitInfo;
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
 
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.facet.index.CategoryDocumentBuilder;
 import org.springframework.format.annotation.DateTimeFormat;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
 import org.springframework.roo.addon.tostring.RooToString;
 
 import com.google.gson.GsonBuilder;
 import com.google.gson.annotations.Expose;
 
 @RooJavaBean
 @RooToString
 @Table(name = "agencies_view")
 @RooJpaActiveRecord(versionField = "", finders = { "findAgencysByStartDateLessThan" })
 public class Agency{
 	  @Expose
     @Id
     @Column(name = "Agency_number")
     private int agencyNumber;
 
 	  @Expose
     @Column(name = "Agency_title")
     private String title;
 
 	  @Expose
     @Column(name = "Category")
     private String category;
 
 	  @Expose
     @Column(name = "Creation")
     private String creation;
 
 	  @Expose
     @Column(name = "Abolition")
     private String abolition;
 
 	  @Expose
     @Column(name = "Administrative_history_note")
     private String administrativeHistoryNote;
 
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
     @OneToMany(mappedBy = "agencyId")
     private List<AgencyLinkFunction> functions;
 
     @OneToMany(mappedBy = "agencyId")
     private List<AgencyLinkOrganisation> organisations;
 
     @OneToMany(mappedBy = "agencyId")
     private List<AgencyLinkPerson> persons;
 
     @OneToMany(mappedBy = "agencyId")
     private List<AgencyLinkSeriesControlled> seriesControlled;
 
     @OneToMany(mappedBy = "agencyId")
     private List<AgencyLinkSeriesCreated> seriesCreated;
 
     @OneToMany(mappedBy = "agencyId")
     private List<AgencyLinkPreceding> preceding;
 
     @OneToMany(mappedBy = "agencyId")
     private List<AgencyLinkRelated> related;
 
     @OneToMany(mappedBy = "agencyId")
     private List<AgencyLinkSubordinate> subordinates;
 
     @OneToMany(mappedBy = "agencyId")
     private List<AgencyLinkSucceeding> succeeding;
 
     @OneToMany(mappedBy = "agencyId")
     private List<AgencyLinkSuperior> superiors;
 
     public String getDateRange() {
         return DateHelper.dateRange(startDateQualifier, startDate, endDateQualifier, endDate);
     }
 
     public int getId() {
         return agencyNumber;
     }
 
     public String getHistoryEacFormat(){
     	return StringService.formatEacCpf(administrativeHistoryNote);
     }
     
     public static List<org.apache.lucene.document.Document> getIndexData(List<au.gov.nsw.records.search.model.Agency> agencies, CategoryDocumentBuilder builder) {
         List<Document> agenciesIndex = new ArrayList<Document>();
         for (Agency agency : agencies) {
             Document doc = new Document();
             Field f = new Field("title", agency.getTitle(), Field.Store.YES, Field.Index.ANALYZED);
             f.setBoost(2.0f);
             doc.add(f);
             doc.add(new Field("content", agency.getAdministrativeHistoryNote(), Field.Store.YES, Field.Index.ANALYZED));
             doc.add(new Field("type", "agencies", Field.Store.YES, Field.Index.ANALYZED));
             doc.add(new Field("url", String.format("/agencies/%d", agency.getAgencyNumber()), Field.Store.YES, Field.Index.ANALYZED));
             doc.add(new Field("startyear", DateHelper.getYearString(agency.getStartDate()), Field.Store.YES, Field.Index.ANALYZED));
             doc.add(new Field("endyear", DateHelper.getYearString(agency.getEndDate()), Field.Store.YES, Field.Index.ANALYZED));
             agenciesIndex.add(doc);
         }
         return agenciesIndex;
     }
 
     public static List<au.gov.nsw.records.search.model.Agency> findAgencyFromLastAmendmentDate(Date from, Date until, int page, int pageSize) {
         String additionalCondition = QueryHelper.buildAdditionalQuery(from, until);
         
         EntityManager em = Agency.entityManager();
         TypedQuery<Agency> q = em.createQuery("SELECT o FROM Agency AS o" + additionalCondition, Agency.class);
         if (from!=null){
         	q.setParameter("from", from);	
         }
         if (until!=null){
         	q.setParameter("until", until);
         }
        return q.setFirstResult((page-1)*pageSize).setMaxResults(pageSize*page).getResultList();
     }
 
     public static long countAgencyFromLastAmendmentDate(Date from, Date until) {
     	  String additionalCondition = QueryHelper.buildAdditionalQuery(from, until);
     	  EntityManager em = Agency.entityManager();
     	  TypedQuery<Long> q = em.createQuery("SELECT count(o) FROM Agency o " + additionalCondition, Long.class);
         if (from!=null){
         	q.setParameter("from", from);	
         }
         if (until!=null){
         	q.setParameter("until", until);
         }
         return q.getSingleResult();
     }
     
     public String getJsonString(){
    	 return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(this); 
     }
 }
