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
	public static int DEPARTMENT_TABLE_POS = 1;
	public static int ROLE_TABLE_POS = 2;
    public static int NEWS_TABLE_POS = 3;
	public static int ATTACHMENT_TABLE_POS = 4;
	public static int AIRLINEPLAN_TABLE_POS = 5;
	public static int AIRLINESTATUS_TABLE_POS = 6;
	public static int FACILITY_TABLE_POS = 7;
	public static int AIRCOMPANY_TABLE_POS = 8;
	public static int AIRPORT_TABLE_POS = 9;
 
 	
 	public PrivilegeType getUserTablePrivilege(){
 		return new PrivilegeType(Privilege, USER_TABLE_POS);
 	}
 
 }
