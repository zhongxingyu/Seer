 package org.browsexml.ecampus.domain;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EntityManager;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.Version;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.Session;
 import org.springframework.beans.factory.annotation.Configurable;
 import org.springframework.transaction.annotation.Transactional;
 
 @Entity
 @Configurable
 @Table(name="LCUTextbooks")
 public class LCUTextbook {
 	private static Log log = LogFactory.getLog(LCUTextbook.class);
     //@ManyToOne(targetEntity = CampusSection.class)
     //@JoinColumn(name="sectionId", referencedColumnName="sectionId")
 	
 	public LCUTextbook() {
 		this.lastUpdateBy = Properties.getEmployeeId();
 	}
 	
 	public LCUTextbook(Long sectionId, LCUBookInfo book) {
 		this.sectionId = sectionId;
 		this.book = book;
 	}
 	
 	@Column
     private Long sectionId;
     
     @ManyToOne(optional=false, fetch=FetchType.EAGER)
     @JoinColumn(name="book", referencedColumnName="id")
     private LCUBookInfo book;
 
     @Column(name = "Require")
     private Boolean require;
 
 
 	@Column(name = "RequireNEW")
     private Boolean requireNew;
 
     @Column(name = "AcceptSubstitution")
     private Boolean acceptSubstitution;
     
     @Column(name = "comment")
     private String comment;
     
     @Column(name = "continuation")
     private Boolean continuation;
     
     @Column(name = "last_update")
     @Temporal(TemporalType.TIMESTAMP)
     private Date lastUpdate;
     
     @Column(name = "last_update_by")
     private String lastUpdateBy;
     
     public String getLastUpdateBy() {
 		return lastUpdateBy;
 	}
 
 	public void setLastUpdateBy(String lastUpdateBy) {
 		this.lastUpdateBy = lastUpdateBy;
 	}
 
 	public LCUBookInfo getBook() {
 		return book;
 	}
 
 	public void setBook(LCUBookInfo book) {
 		this.book = book;
 	}
 
 	public Long getSectionId() {    
         return this.sectionId;        
     }    
     
     public void setSectionId(Long sectionId) {    
         this.sectionId = sectionId;        
     }     
     
     @PersistenceContext    
     transient EntityManager entityManager;    
     
     @Id    
     @GeneratedValue(strategy = GenerationType.AUTO)    
     @Column(name = "id")    
     private Long id;    
     
     @Version    
     @Column(name = "version")    
     private Integer version;    
     
     public Long getId() {    
         return this.id;        
     }    
     
     public void setId(Long id) {    
         this.id = id;        
     }    
     
     public Integer getVersion() {    
         return this.version;        
     }    
     
     public void setVersion(Integer version) {    
         this.version = version;        
     }    
     
     @Transactional    
     public void persist() {    
         if (this.entityManager == null) this.entityManager = entityManager();        
         this.entityManager.persist(this);        
     }    
     
     @Transactional    
     public void remove() {    
         if (this.entityManager == null) this.entityManager = entityManager();        
         if (this.entityManager.contains(this)) {        
             this.entityManager.remove(this);            
         } else {        
             LCUTextbook attached = this.entityManager.find(LCUTextbook.class, this.id);            
             this.entityManager.remove(attached);            
         }        
     }    
     
     @Transactional    
     public void flush() {    
         if (this.entityManager == null) this.entityManager = entityManager();        
         this.entityManager.flush();        
     }    
     
     @Transactional    
     public void merge() {    
         if (this.entityManager == null) this.entityManager = entityManager();        
         LCUTextbook merged = this.entityManager.merge(this);        
         this.entityManager.flush();        
         this.id = merged.getId();        
     } 
     
 //    @Transactional
 //    public void addToSection(LCUBookInfo book) {
 //		book.setBook(this);
 //		getBook().add(book);
 //		merge();
 //    }
     
     public static EntityManager entityManager() {    
         EntityManager em = new LCUTextbook().entityManager;        
         if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");        
         return em;        
     }    
     
     public static HashMap<Integer, Integer> countLCUTextbooksBySection(Term term) {
 
     	Session s = (Session) entityManager().getDelegate();
     	List<Object[]> counts = s.createSQLQuery("" +
    			"select b.sectionId, count(b.sectionId) " +
     			"from LCUeCampusSections e" +
     			"	join [LCUTextbooks] b on b.sectionid = e.sectionId " +
     			"where e.academic_term = :term and e.academic_year = :year " +
     			"group by b.sectionId ")
     		.setParameter("term", term.getTerm())
     		.setParameter("year", term.getYear())
     		.list();        
     	
     	MyHashMap countMap = new MyHashMap();
     	for (Object[] count: counts) {
     		log.debug("put " + count[0] + ",  " + count[1]);
     		countMap.put(new Long((Integer)count[0]), new Long((Integer) count[1]));
     	}
     	return countMap;
     }
     
     public static long countLCUTextbooks() {    
         return (Long) entityManager().createQuery("select count(o) from LCUTextbook o").getSingleResult();        
     }    
     
     public static List<LCUTextbook> findAllLCUTextbooks() {    
         return entityManager().createQuery("select o from LCUTextbook o").getResultList();        
     }    
     
     public static LCUTextbook findLCUTextbook(Long id) {    
         if (id == null) throw new IllegalArgumentException("An identifier is required to retrieve an instance of LCUTextbook");        
         return entityManager().find(LCUTextbook.class, id);        
     }    
     
     public static List<LCUTextbook> findLCUTextbookEntries(int firstResult, int maxResults) {    
         return entityManager().createQuery("select o from LCUTextbook o").setFirstResult(firstResult).setMaxResults(maxResults).getResultList();        
     }  
     
     public static List<LCUTextbook> findLCUTextbookEntries(Long sectionId) { 
         return entityManager().createQuery(
         		"select o " +
         		"from LCUTextbook o " +
         		"where o.sectionId = :sectionId ")
         	.setParameter("sectionId", sectionId)
         	.getResultList();        
     }    
     
     public static LCUTextbook findLCUTextbookEntries(Long sectionId, LCUBookInfo book) {
     	if (book == null)
     		return null;
         try {
 			return (LCUTextbook) entityManager().createQuery(
 					"select o " +
 					"from LCUTextbook o " +
 					"where o.sectionId = :sectionId and o.book.id = :book ")
 				.setParameter("sectionId", sectionId)
 				.setParameter("book", book.getId())
 				.getSingleResult();
 		} catch (Exception e) {
 			return null;
 		}        
     }  
     
     public Boolean getRequire() {
 		return require;
 	}
 
 	public void setRequire(Boolean require) {
 		this.require = require;
 	}
 
 	public Boolean getRequireNew() {
 		return requireNew;
 	}
 
 	public void setRequireNew(Boolean requireNew) {
 		this.requireNew = requireNew;
 	}
 
 	public Boolean getAcceptSubstitution() {
 		return acceptSubstitution;
 	}
 
 	public void setAcceptSubstitution(Boolean acceptSubstitution) {
 		this.acceptSubstitution = acceptSubstitution;
 	}
 
 	public String getComment() {
 		return comment;
 	}
 
 	public void setComment(String comment) {
 		this.comment = comment;
 	}
 
 	public Boolean getContinuation() {
 		return continuation;
 	}
 
 	public void setContinuation(Boolean continuation) {
 		this.continuation = continuation;
 	}
 
 	public String toString() {    
         StringBuilder sb = new StringBuilder();               
         sb.append("Id: ").append(getId()).append(", ");        
         sb.append("Version: ").append(getVersion()).append(", ");        
         sb.append("SectionId: ").append(getSectionId()).append(", ");        
         sb.append("Book: ").append(getBook().getIsbn()).append(", ");        
         return sb.toString();        
     } 
 }
