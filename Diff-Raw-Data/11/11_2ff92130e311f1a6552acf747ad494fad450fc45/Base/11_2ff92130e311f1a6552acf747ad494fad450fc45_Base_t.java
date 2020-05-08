 package ee.itcollege.p0rn.entities;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.EntityManager;
 import javax.persistence.MappedSuperclass;
 import javax.persistence.PersistenceContext;
 import javax.persistence.PostPersist;
 import javax.persistence.PrePersist;
 import javax.persistence.PreUpdate;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 
 import org.springframework.format.annotation.DateTimeFormat;
 import org.springframework.roo.addon.entity.RooEntity;
 import org.springframework.roo.addon.tostring.RooToString;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.transaction.annotation.Transactional;
 
 @MappedSuperclass
 @RooToString
 @RooEntity(mappedSuperclass = true)
 public abstract class Base {
 	
     @NotNull
     private String kommentaar;
 	
 	@NotNull
     @Size(max = 32)
     @Column(updatable=false)
     private String avaja;
 
     @NotNull
     @DateTimeFormat(pattern = "dd.MM.yyyy")
     @Column(updatable=false)
     private Date avatud;
 
     @NotNull
     @Size(max = 32)
     private String muutja;
 
     @NotNull
     @DateTimeFormat(pattern = "dd.MM.yyyy")
     private Date muudetud;
 
     @NotNull
     @DateTimeFormat(pattern = "dd.MM.yyyy")
     @Column(updatable=false)
     private String sulgeja;
    
     public String getSulgeja() {
 		return sulgeja;
 	}
 
 	public void setSulgeja(String sulgeja) {
 		this.sulgeja = sulgeja;
 	}
 
     @NotNull
     @DateTimeFormat(pattern="dd.MM.yyyy")
    private Date suletud;
     
    public Date getSuletud() {
         return this.suletud;
     }
     
    public void setSuletud(Date suletud) {
         this.suletud = suletud;
     }
 	 
 
 	@PrePersist
 	public void setCreated() {
 		Calendar tempDate = Calendar.getInstance();
 		tempDate.clear();
 		tempDate.set(Calendar.YEAR, 9999);
 		tempDate.set(Calendar.MONTH, Calendar.DECEMBER);
 		tempDate.set(Calendar.DAY_OF_MONTH, 31);
 		if (SecurityContextHolder.getContext().getAuthentication() != null) {
 	   		avaja = (String) SecurityContextHolder.getContext().getAuthentication().getName();
 	   		muutja = (String) SecurityContextHolder.getContext().getAuthentication().getName();
 	   		sulgeja = "";
 		} else {
 		   	avaja = "unknown";
 		   	muutja = "unknown";
 		   	sulgeja = "unknown";
 		}
 	    avatud = new Date();
 	    muudetud = new Date();
	    this.suletud = tempDate.getTime();
 	}
 	
 	@PostPersist
 	protected void afterCreate() {
 		
 	}
 
    @PreUpdate
    protected void onUpdate() {  
 	   avaja = "värdjas";
 	   avatud = new Date();
 	   
 	   muutja = (String) SecurityContextHolder.getContext().getAuthentication().getName();
 	   muudetud = new Date();
    }
    
    
    abstract String getTableName();
    abstract String getIdName();
    abstract Long getId();
    
    @Transactional
    public void remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        setSulgeja((String) SecurityContextHolder.getContext().getAuthentication().getName());
        this.entityManager.createQuery("UPDATE "+getTableName()+" SET suletud = CURDATE(), sulgeja = '" + getSulgeja() + "' WHERE "+getIdName()+" = " + this.getId()).executeUpdate();
    }
    
    @PersistenceContext
    transient EntityManager entityManager;
    
    public static EntityManager entityManager() {
        EntityManager em = new SeadusePunkt().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }
 }
