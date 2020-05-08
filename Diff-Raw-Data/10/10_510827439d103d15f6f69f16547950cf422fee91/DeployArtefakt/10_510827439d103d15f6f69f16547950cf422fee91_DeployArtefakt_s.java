 package ch.lepeit.brain2.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 
 @Entity
 public class DeployArtefakt {
     private static final long serialVersionUID = 1L;
     
     @Id
     private int id;
     
     @OneToMany(fetch=FetchType.EAGER, mappedBy="deployArtefakt")
     List<Installation> installationen;
     
     private String name;
     private String groupId;
     private String artefaktId;
     private String extension;
     
     public DeployArtefakt() {
     }
     
     public DeployArtefakt(String name){
         this.id = id;
         this.name = name;
         installationen = new ArrayList<Installation>();
     }
 
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     public List<Installation> getInstallationen() {
         return installationen;
     }
 
     public void setInstallationen(List<Installation> installationen) {
         this.installationen = installationen;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     
     public String getGroupId() {
         return groupId;
     }
 
     public void setGroupId(String groupId) {
         this.groupId = groupId;
     }
 
     public String getArtefaktId() {
         return artefaktId;
     }
 
     public void setArtefaktId(String artefaktId) {
         this.artefaktId = artefaktId;
     }
 
     public String getExtension() {
         return extension;
     }
 
     public void setExtension(String extension) {
         this.extension = extension;
     }
     
     
 }
