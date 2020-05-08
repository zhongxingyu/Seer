 package de.twenty11.skysail.server.ext.notes.domain;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.TableGenerator;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 
 import org.apache.commons.lang.Validate;
 
 import de.twenty11.skysail.common.forms.Field;
 import de.twenty11.skysail.common.forms.Form;
 import de.twenty11.skysail.server.um.domain.SkysailUser;
 
 /**
  * 
  */
 @Entity
 @Form(name = "folderform")
 public class Folder extends Component implements Comparable<Folder> {
 
     @Id
     @TableGenerator(name = "TABLE_GEN", table = "SEQUENCE", pkColumnValue = "EXT_NOTES_SEQ")
     @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
     protected int pid;// primary key for db
 
     private Folder parent;
 
     @ManyToOne()
     @JoinColumn(name = "owner", nullable = false)
     private SkysailUser owner;
 
     @Field
     @NotNull(message = "Name is mandatory")
     @Size(min = 1, message = "name  must not be empty")
     private String folderName;
 
     public int getPid() {
         return this.pid;
     }
 
     @Override
     public boolean equals(Object obj) {
        if (this == obj)
             return true;
        if (obj == null)
             return false;
        if (getClass() != obj.getClass())
             return false;
         Folder other = (Folder) obj;
        if (pid != other.getPid())
             return false;
         return true;
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + pid;
         return result;
     }
 
     public static Folder createRoot(String folderName) {
         return new Folder(null, folderName);
     }
 
     public Folder() {
         // needed for EclipseLink
     }
 
     public Folder(Folder parent, String folderName) {
         Validate.notNull(folderName, "A folder needs a name");
         this.parent = parent;
         this.folderName = folderName;
         this.parent = parent;
     }
 
     public static boolean isRootFolder(Folder folder) {
         return folder.parent == null;
     }
 
     public Folder getParent() {
         return parent;
     }
 
     public String getFolderName() {
         return folderName;
     }
 
     @Override
     public int compareTo(Folder other) {
         return folderName.compareTo(other.getFolderName());
     }
 
     public SkysailUser getOwner() {
         return owner;
     }
 
     public void setOwner(SkysailUser owner) {
         this.owner = owner;
     }
 
     @Override
     public String toString() {
         return folderName;
     }
 }
