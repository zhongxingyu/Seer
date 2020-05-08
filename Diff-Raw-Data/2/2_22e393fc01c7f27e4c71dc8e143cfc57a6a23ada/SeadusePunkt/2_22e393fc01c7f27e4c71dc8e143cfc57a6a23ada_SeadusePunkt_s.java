 package ee.itcollege.p0rn.entities;
 
 import org.springframework.roo.addon.entity.RooEntity;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.roo.addon.tostring.RooToString;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Null;
 import javax.validation.constraints.Size;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.EntityManager;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.PersistenceContext;
 import javax.persistence.PrePersist;
 import javax.persistence.PreUpdate;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import org.springframework.format.annotation.DateTimeFormat;
 import ee.itcollege.p0rn.entities.Seadus;
 import javax.persistence.ManyToOne;
 
 @RooJavaBean
 @RooToString
 @RooEntity
 public class SeadusePunkt extends Base {
 	
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	@Column(nullable=true)
     private Long seaduse_punkt_ID;
     
 	public Long getId() {
 		return seaduse_punkt_ID;
 	}
 	
 	public String getIdName() {
 		return "seaduse_punkt_ID";
 	}
 	
 	public String getTableName() {
 		return "SeadusePunkt";
 	}
 
     @NotNull
     @Size(max = 20)
     private String paragrahv;
 
     @NotNull
     @Size(max = 20)
     private String pais;
 
     @NotNull
     @Size(max = 20)
     private String tekst;
 
     @NotNull
     @Size(max = 20)
     private String kehtiv_alates;
 
     @NotNull
     @Size(max = 20)
     private String kehtiv_kuni;
 
     @ManyToOne
     private Seadus seaduse_ID;
 
     public Seadus getSeaduse_ID() {
 		return seaduse_ID;
 	}
 
 	public void setSeaduse_ID(Seadus seaduse_ID) {
 		this.seaduse_ID = seaduse_ID;
 	}
 
 	@ManyToOne
 	private ee.itcollege.p0rn.entities.SeadusePunkt ylemus_seaduse_punkt_ID;
 
 	public static List<SeadusePunkt> findAllSeadusePunkts(long seaduse_ID, String alates, String kuni) {
     	String g = "WHERE 1=1";
     	if (seaduse_ID > 0) {
     		g = g + " AND seaduse_ID = " + seaduse_ID;
     	}
     	if (alates.length() > 0) {
     		g = g + " AND kehtiv_alates >= '" + alates + "'";
     	}
     	if (kuni.length() > 0) {
     		g = g + " AND kehtiv_kuni <= '" + kuni + "'";
     	}
    	g = g + " AND sulgeja = '' ";
     	
         return entityManager().createQuery("SELECT o FROM SeadusePunkt o " + g, SeadusePunkt.class).getResultList();
     }
     
     public static long countSeadusePunkts() {
         return entityManager().createQuery("SELECT COUNT(o) FROM SeadusePunkt o WHERE suletud > CURDATE()", Long.class).getSingleResult();
     }
     
     public static List<SeadusePunkt> findAllSeadusePunkts() {
         return entityManager().createQuery("SELECT o FROM SeadusePunkt o WHERE suletud > CURDATE()", SeadusePunkt.class).getResultList();
     }
     
     public static List<SeadusePunkt> findSeadusePunktEntries(int firstResult, int maxResults) {
         return entityManager().createQuery("SELECT o FROM SeadusePunkt o WHERE suletud > CURDATE()", SeadusePunkt.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
     }
     public static List<SeadusePunkt> findAllAlamSeadusePunkts(Long master_id) {
     	return entityManager().createQuery("SELECT o FROM SeadusePunkt o WHERE ylemus_seaduse_punkt_ID = " + master_id.toString(), SeadusePunkt.class).getResultList();
     }
     
 	public String getFormLabel() {
 		return getPais();
 	}
 }
