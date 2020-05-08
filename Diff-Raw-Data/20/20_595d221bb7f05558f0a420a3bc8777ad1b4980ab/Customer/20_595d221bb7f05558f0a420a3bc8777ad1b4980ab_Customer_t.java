 package no.dusken.annonseweb.models;
 
 import no.dusken.common.model.DuskenObject;
 
 import javax.persistence.ElementCollection;
 import javax.persistence.Entity;
 import javax.persistence.OneToMany;
 import javax.validation.constraints.NotNull;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.List;
 
 import static javax.persistence.CascadeType.ALL;
 import static javax.persistence.FetchType.LAZY;
 
 @Entity
 public class Customer extends DuskenObject{
 
     @NotNull
     private String name;
     @NotNull
     private String contactPerson;
     @NotNull
     private String email;
     @NotNull
     private String phoneNumber;
     @NotNull
     private String invoiceAddress;
 
     private String subscriberAddress;
 
     private BigInteger discount;
 
     @ElementCollection
     private List<String> industryTags = new ArrayList<String>();
 
     private String homepage;
 
     @OneToMany(fetch = LAZY, cascade = ALL, mappedBy = "customer")
     private List<ContactNote> contactNotes= new ArrayList<ContactNote>();
 
     @OneToMany(fetch = LAZY, cascade = ALL, mappedBy = "customer")
     private List<Sale> sales = new ArrayList<Sale>();
 
     public Customer(){}
 
     public Customer(String name, String email, String phoneNumber,
                    String invoiceAddress, String contactPerson){
         this.name = name;
         this.email = email;
         this.phoneNumber = phoneNumber;
        this.invoiceAddress = invoiceAddress;
        this.contactPerson = contactPerson;
     }
 
     public Customer(String name, String email, String phoneNumber, String invoiceAddress,
                     String subscriberAddress, String homepage, BigInteger discount, List<String> industryTags,
                     List<ContactNote> contactNotes, List<Sale> sales) {
         this.name = name;
         this.email = email;
         this.phoneNumber = phoneNumber;
         this.invoiceAddress = invoiceAddress;
         this.subscriberAddress = subscriberAddress;
         this.discount = discount;
         this.industryTags = industryTags;
         this.homepage = homepage;
         this.contactNotes = contactNotes;
         this.sales = sales;
     }
 
     public void addSale(Sale sale){
         sales.add(sale);
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     public void setPhoneNumber(String phoneNumber) {
         this.phoneNumber = phoneNumber;
     }
 
     public void setInvoiceAddress(String invoiceAddress) {
         this.invoiceAddress = invoiceAddress;
     }
 
     public void setSubscriberAddress(String subscriberAddress) {
         this.subscriberAddress = subscriberAddress;
     }
 
     public void setDiscount(BigInteger discount) {
         this.discount = discount;
     }
 
     public void setIndustryTags(List<String> industryTags) {
         this.industryTags = industryTags;
     }
 
     public String getName() {
         return name;
     }
 
     public String getEmail() {
         return email;
     }
 
     public String getPhoneNumber() {
         return phoneNumber;
     }
 
     public String getInvoiceAddress() {
         return invoiceAddress;
     }
 
     public String getSubscriberAddress() {
         return subscriberAddress;
     }
 
     public BigInteger getDiscount() {
         return discount;
     }
 
     public List<String> getIndustryTags() {
         return industryTags;
     }
 
     public List<ContactNote> getContactNotes() {
         return contactNotes;
     }
 
     public void setContactNotes(List<ContactNote> contactNotes) {
         this.contactNotes = contactNotes;
     }
 
     public List<Sale> getSales() {
         return sales;
     }
 
     public void setSales(List<Sale> sales) {
         this.sales = sales;
     }
 
     public String getHomepage() {
         return homepage;
     }
 
     public void setHomepage(String homepage) {
         this.homepage = homepage;
     }
 
     public String getContactPerson() {
         return contactPerson;
     }
 
     public void setContactPerson(String contactPerson) {
         this.contactPerson = contactPerson;
     }
 }
