 package ee.itcollege.p0rn.entities;
 
 import org.joda.time.DateTime;
 import org.springframework.roo.addon.entity.RooEntity;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.roo.addon.tostring.RooToString;
 import org.springframework.security.core.context.SecurityContextHolder;
 
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import javax.persistence.AttributeOverrides;
 import javax.persistence.Column;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.PrePersist;
 import javax.persistence.PreUpdate;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.print.attribute.standard.DateTimeAtCreation;
 
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.format.annotation.DateTimeFormat;
 
 @RooJavaBean
 @RooToString
 @RooEntity
 public class Seadus extends Base {
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO)
     private Long seaduse_ID;
 	
 	public Long getId() {
 		return seaduse_ID;
 	}
 	
     @NotNull
     @Size(max = 20)
     private String kood;
 
 	@NotNull
     @Size(max = 20)
     private String nimetus;
 
     @NotNull
     @Size(max = 20)
     private String kehtiv_alates;
 
     @NotNull
     @Size(max = 20)
     private String kehtiv_kuni;
 
 	@Override
 	public String getTableName() {
 		return "Seadus";
 	}
 
 	@Override
 	public String getIdName() {
 		return "seaduse_ID";
 	}
 }
