 package rt.model.entity;
 
 import java.io.Serializable;
 import java.lang.String;
 import javax.persistence.*;
 
 /**
  * Entity implementation class for Entity: UserInfo
  *
  */
 @Entity
 public class UserInfo implements Serializable {
 
 	   
 	@Id
 	private int id;	
 	private String name;
 	private String address;
 	private String phone1;
 	private String phone2;
 	private String email;
 	private String type;
	private String company;
 	private static final long serialVersionUID = 1L;
 	
 	
 	public int getId() {
 		return this.id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	} 	
   
 	public String getName() {
 		return this.name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}   
 	public String getAddress() {
 		return this.address;
 	}
 
 	public void setAddress(String address) {
 		this.address = address;
 	}
 	public String getPhone1() {
 		return phone1;
 	}
 	public void setPhone1(String phone1) {
 		this.phone1 = phone1;
 	}
 	public String getPhone2() {
 		return phone2;
 	}
 	public void setPhone2(String phone2) {
 		this.phone2 = phone2;
 	}
 	public String getEmail() {
 		return email;
 	}
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	public String getType() {
 		return type;
 	}
 
	public void setCompany(String company) {
		this.company = company;
	}
	public String getCompany() {
		return company;
	}

 	public void setType(String type) {
 		this.type = type;
 	}
 	@Override
 	public String toString(){
 		String rtnStr= new String("");
 		rtnStr += id+", "+name; 
 		return rtnStr;
 	}
    
 }
