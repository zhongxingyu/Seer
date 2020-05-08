 /**
  * 
  */
 package models.dbentities;
 
 import java.io.Serializable;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.IdClass;
 import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
 import models.dbentities.ClassPupil.ClassPupilPK;
 
 import play.db.ebean.Model;
 
 /**
  * @author Jens N. Rammant
  *
  */
 @Entity
@Table(name="ClassPupil",uniqueConstraints=@UniqueConstraint(columnNames={"classid","indid"}) )
 @IdClass(ClassPupilPK.class)
 public class ClassPupil extends Model {
 	@Id
 	public String classid;
 	@Id
 	public String indid;
		
 	public class ClassPupilPK implements Serializable{
 		public String classid;
 		public String indid;
 		
 		public ClassPupilPK(){}
 		public ClassPupilPK(String classid,String indid){
 			this.classid = classid;
 			this.indid = indid;
 		}
 		
 		public int hashCode(){
 			return super.hashCode();
 		}
 		
 		public boolean equals(Object other){
 			if(! (other instanceof ClassPupilPK))return false;
 			ClassPupilPK oth = (ClassPupilPK) other;
 			return this.classid.equals(oth.classid) && this.indid.equals(oth.indid);
 			
 		}
 	}
 }
