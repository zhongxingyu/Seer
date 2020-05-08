 package svm.logic.implementation.tranferobjects;
 
 import svm.domain.abstraction.exception.DomainParameterCheckException;
 import svm.domain.abstraction.modelInterfaces.IMember;
 import svm.logic.abstraction.exception.IllegalGetInstanceException;
 import svm.logic.abstraction.transferobjects.IHasModel;
 import svm.logic.abstraction.transferobjects.ITransferLocation;
 import svm.logic.abstraction.transferobjects.ITransferMember;
 import svm.logic.implementation.transferobjectcreator.TransferObjectCreator;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 /**
  * ProjectTeam: Team C
  * Date: 31.10.12
  */
 public class TransferMember implements ITransferMember, IHasModel<IMember> {
     private IMember member;
     private String firstName;
     private ITransferLocation location;
     private String userName;
     private String lastName;
     private String title;
     private String socialNumber;
     private Date birthDate;
     private String gender;
     private Date entryDate;
     private String phone1;
     private String phone2;
     private String fax;
     private String email1;
     private String email2;
     private String street;
     private String streetNumber;
     private String coordLat;
     private String coordLong;
     private boolean hasPaidFee;
 
     @Override
     public IMember getModel() {
         return this.member;
     }
 
     @Override
     public String getUsername() {
         return userName;
     }
 
     @Override
     public String getFirstName() {
         return firstName;
     }
 
     @Override
     public String getLastName() {
         return lastName;
     }
 
     @Override
     public String getTitle() {
         return title;
     }
 
     @Override
     public String getSocialNumber() {
         return socialNumber;
     }
 
     @Override
     public Date getBirthDate() {
         return birthDate;
     }
 
     @Override
     public String getGender() {
         return gender;
     }
 
     @Override
     public Date getEntryDate() {
         return entryDate;
     }
 
     @Override
     public String getPhone1() {
         return phone1;
     }
 
     @Override
     public String getPhone2() {
         return phone2;
     }
 
     @Override
     public String getEmail1() {
         return email1;
     }
 
     @Override
     public String getEmail2() {
         return email2;
     }
 
     @Override
     public String getFax() {
         return fax;
     }
 
     @Override
     public String getStreet() {
         return street;
     }
 
     @Override
     public String getStreetNumber() {
         return streetNumber;
     }
 
     @Override
     public String getLat() {
         return coordLat;
     }
 
     @Override
     public String getLong() {
         return coordLong;
     }
 
     @Override
     public ITransferLocation getLocation() throws IllegalGetInstanceException {
         return location;
     }
 
     @Override
     public boolean getPaid() throws DomainParameterCheckException {
         //return this.member.hasPaidFee(new GregorianCalendar().get(Calendar.YEAR));
         return hasPaidFee;
     }
 
     @Override
     public void setObject(Object o) throws IllegalGetInstanceException, DomainParameterCheckException {
         this.member = (IMember) o;
         this.firstName = member.getFirstName();
         this.userName = member.getUserName();
         this.lastName = member.getLastName();
         this.title = member.getTitle();
         this.socialNumber = member.getSocialNumber();
         this.birthDate = member.getBirthDate();
         this.gender = member.getGender();
         this.entryDate = member.getEntryDate();
        Integer integer=new GregorianCalendar().get(Calendar.YEAR);
        this.hasPaidFee = this.member.hasPaidFee(integer);
 
         if (!member.getContactDetails().isNull()) {
             this.phone1 = member.getContactDetails().getPhone1();
             this.phone2 = member.getContactDetails().getPhone2();
             this.fax = member.getContactDetails().getFax();
             this.email1 = member.getContactDetails().getEmail1();
             this.email2 = member.getContactDetails().getEmail2();
             this.street = member.getContactDetails().getStreet();
             this.streetNumber = member.getContactDetails().getStreetNumber();
             this.coordLat = member.getContactDetails().getCoordLat();
             this.coordLong = member.getContactDetails().getCoordLong();
             if (!member.getContactDetails().getLocation().isNull()) {
                 this.location = (ITransferLocation) TransferObjectCreator.getInstance(TransferLocation.class, this.member.getContactDetails().getLocation());
             }
         }
     }
 
     @Override
     public String toString() {
         return this.getFirstName() + " " + this.getLastName();
     }
 }
