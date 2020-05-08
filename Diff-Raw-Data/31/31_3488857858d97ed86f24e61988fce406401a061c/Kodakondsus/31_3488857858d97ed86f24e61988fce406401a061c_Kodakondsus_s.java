 package ee.itcollege.p0rn.entities;
 
 import org.springframework.roo.addon.entity.RooEntity;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.roo.addon.tostring.RooToString;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 import java.util.Date;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import org.springframework.format.annotation.DateTimeFormat;
 import ee.itcollege.p0rn.entities.Piiririkkuja;
 import javax.persistence.ManyToOne;
 
 @RooJavaBean
 @RooToString
 @RooEntity
 public class Kodakondsus {
 
    private int kodakondsus_ID;

     @NotNull
     @Size(max = 32)
     private String avaja;
 
     @NotNull
     @Temporal(TemporalType.TIMESTAMP)
     @DateTimeFormat(style = "M-")
     private Date avatud;
 
     @NotNull
     @Size(max = 32)
     private String muutja;
 
     @NotNull
     @Temporal(TemporalType.TIMESTAMP)
     @DateTimeFormat(style = "M-")
     private Date muudetud;
 
     @Size(max = 32)
     private String sulgeja;
 
     @NotNull
     @Temporal(TemporalType.TIMESTAMP)
     @DateTimeFormat(style = "M-")
     private Date suletud;
 
     private String kommentaar;
 
     @Temporal(TemporalType.TIMESTAMP)
     @DateTimeFormat(style = "M-")
     private Date alates;
 
     @Temporal(TemporalType.TIMESTAMP)
     @DateTimeFormat(style = "M-")
     private Date kuni;
 
    private int riik_ID;

     @Size(max = 20)
     private String isikukood;
 
     @ManyToOne
     private Piiririkkuja piiririkkuja_ID;
 }
