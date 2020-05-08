 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package BE;
 
 /**
  *
  * @author Reese
  */
 public class Member
 {
     //Instance fields
     private int memberNo;
     private String lastName;
     private String firstName;
     private String address;
     private int birthYear;
     private int phoneNo;
     private String email;    
     private enum MemberType {junior, senior, recreational, retiree, administrative};
     private MemberType memberType;
     private int DTULicenceNo;
     private int DTUPoints;
     
     private Member() throws IllegalArgumentException
     {
        
     }
     
     public static Member fromDataBase(int memberNo, String lastName, String firstname, String address, int birthYear, int phoneNo, String email, String memberType, int DTULicenceNo, int DTUPoints) throws IllegalArgumentException
     {
         Member newMemberObj = new Member();
         newMemberObj.setMemberNo(memberNo);
         newMemberObj.SetLastName(lastName);
         newMemberObj.setFirstName(firstname);
         newMemberObj.setAddress(address);
         newMemberObj.setEmail(email);
         newMemberObj.setBirthYear(birthYear);
         newMemberObj.setPhoneNo(phoneNo);
         newMemberObj.setMemberType(memberType);
         newMemberObj.setDTUPoints(DTUPoints);
         newMemberObj.setDTULicenseNo(DTULicenceNo);
         return newMemberObj;
     }
     
     public static Member simpleFromDB(int memberNo, String lastName, String firstname) throws IllegalArgumentException
     {
         Member newMemberObj = new Member();
         newMemberObj.setMemberNo(memberNo);
         newMemberObj.SetLastName(lastName);
         newMemberObj.setFirstName(firstname);
         return newMemberObj;
     }
     
    public static Member createNew(String firstname, String lastName, String address, int birthYear, int phoneNo, String email, String memberType, int DTULicenceNo, int DTUPoints)
     {
         Member newMemberObj = new Member();
         newMemberObj.SetLastName(lastName);
         newMemberObj.setFirstName(firstname);
         newMemberObj.setAddress(address);
         newMemberObj.setEmail(email);
         newMemberObj.setBirthYear(birthYear);
         newMemberObj.setPhoneNo(phoneNo);
         newMemberObj.setMemberType(memberType);
         newMemberObj.setDTUPoints(DTUPoints);
         newMemberObj.setDTULicenseNo(DTULicenceNo);
         return newMemberObj;
     }
     
     public static Member createSimple(String firstName, String lastName, String address, int birthYear, int phoneNo, String email)
     {
         Member newMemberObj = new Member();
         newMemberObj.SetLastName(lastName);
         newMemberObj.setFirstName(firstName);
         newMemberObj.setAddress(address);
         newMemberObj.setEmail(email);
         newMemberObj.setBirthYear(birthYear);
         newMemberObj.setPhoneNo(phoneNo);
         return newMemberObj;
     }
     
     /**
      * @return the memberNo
      */
     public int getMemberNo()
     {
         return memberNo;
     }
 
     /**
      * @return the lastName
      */
     public String getLastName()
     {
         return lastName;
     }
 
     /**
      * @return the firstName
      */
     public String getFirstName()
     {
         return firstName;
     }
     
     public void SetLastName(String lastName)
     {
         this.lastName = lastName;
     }
     
     public void setFirstName(String firstName)
     {
         this.firstName=firstName;
     }
 
     /**
      * @return the address
      */
     public String getAddress()
     {
         return address;
     }
 
     /**
      * @return the birthYear
      */
     public int getBirthYear()
     {
         return birthYear;
     }
 
     /**
      * @return the phoneNo
      */
     public int getPhoneNo()
     {
         return phoneNo;
     }
 
     /**
      * @return the email
      */
     public String getEmail()
     {
         return email;
     }
 
     /**
      * @return the DTULicenceNo
      */
     public int getDTULicenceNo()
     {
         return DTULicenceNo;
     }
     
     /**
      * @
      */
     public String getMemberType()
     {
         return this.memberType.toString();
     }
     
     /**
      * @param address the address to set
      */
     public void setAddress(String address)
     {
         this.address = address;
     }
 
     /**
      * @param phoneNo the phoneNo to set
      */
     public void setPhoneNo(int phoneNo)
     {
         this.phoneNo = phoneNo;
     }
 
     /**
      * @param email the email to set
      */
     public void setEmail(String email)
     {
         this.email = email;
     }   
     
     /**
      * @return the DTUPoints
      */
     public int getDTUPoints()
     {
         return DTUPoints;
     }
 
     /**
      * @param DTUPoints the DTUPoints to set
      */
     public void setDTUPoints(int DTUPoints)
     {
         this.DTUPoints = DTUPoints;
     }
     
     public void setMemberType(String memberType)
     {
         switch(memberType)
         {
             case "junior": this.memberType = MemberType.junior; break;
             case "senior": this.memberType = MemberType.senior; break;
             case "recreational": this.memberType = MemberType.recreational; break;
             case "retiree": this.memberType = MemberType.retiree; break;
             default: throw new IllegalArgumentException("ERROR - invalid member type");
         } 
     }
     
     //private setters for factory constructor methods
     private void setMemberNo(int memberNo)
     {
         this.memberNo=memberNo;
     }
     
     private void setBirthYear(int birthYear)
     {
         this.birthYear=birthYear;
     }
     
     private void setDTULicenseNo(int DTULicenceNo)
     {
         this.DTULicenceNo=DTULicenceNo;
     }
     
     @Override
     public String toString()
     {
         return this.memberNo + " - " + this.firstName + " " + this.lastName;
     }
 }
