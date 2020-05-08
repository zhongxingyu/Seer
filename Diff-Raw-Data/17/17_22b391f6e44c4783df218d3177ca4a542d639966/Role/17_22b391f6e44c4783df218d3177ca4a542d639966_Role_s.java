 package models;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import org.hibernate.annotations.Columns;
 import play.db.jpa.Model;
 /**
  * 
  * @author Li
  *
  */
 /**
  * @author Administrator
  *
  */
 @Entity
 public class Role extends Model{
 	@Column(name = "Name", nullable = false, length = 255)
 	public String Name;
 	@Column(name = "Privilege", nullable = false)
 	public int Privilege;
 	
 	public class PrivilegeType {
 		public boolean Readable;
 		public boolean Writable;
 		public PrivilegeType(int privilege, int pos){
 			//* Set Readable and Writable
 			privilege >>= pos * 2;
 			privilege &= 3;
 			this.Readable = (privilege & 1)!=0;
 			this.Writable = (privilege & 2)!=0;
 		}
 	}
 	
 	
 	public static int USER_TABLE_POS = 0;
	public static int Department_TABLE_POS = 1;
	public static int Role_TABLE_POS = 2;
	public static int Attachment_TABLE_POS = 3;
	public static int AirlinePlan_TABLE_POS = 4;
	public static int AirlineStatus_TABLE_POS = 5;
	public static int Facility_TABLE_POS = 6;
	public static int AirCompany_TABLE_POS = 7;
	public static int AirPort_TABLE_POS = 8;
 
 	
 	public PrivilegeType getUserTablePrivilege(){
 		return new PrivilegeType(Privilege, USER_TABLE_POS);
 	}
 
 }
