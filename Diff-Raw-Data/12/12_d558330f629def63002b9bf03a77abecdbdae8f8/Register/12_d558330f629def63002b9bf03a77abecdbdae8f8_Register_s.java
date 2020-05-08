 package pojo;
 
 import java.sql.Timestamp;
 
 public class Register {
     private Integer id;
     private Integer uid;
     private Timestamp registerTime;
     private String registerSequence;
     private SiteUser siteUser;
 
     public SiteUser getSiteUser() {
         return siteUser;
     }
 
     public void setSiteUser(SiteUser siteUser) {
         this.siteUser = siteUser;
     }
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public Integer getUid() {
         return uid;
     }
 
     public void setUid(Integer uid) {
         this.uid = uid;
     }
 
     public Timestamp getRegisterTime() {
         return registerTime;
     }
 
     public void setRegisterTime(Timestamp registerTime) {
         this.registerTime = registerTime;
     }
 
     public String getRegisterSequence() {
         return registerSequence;
     }
 
     public void setRegisterSequence(String registerSequence) {
         this.registerSequence = registerSequence;
     }
 }
