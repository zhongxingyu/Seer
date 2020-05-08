 package com.supinfo.supinbank.entity;
 
 import java.io.Serializable;
 import java.util.Set;
 import javax.persistence.*;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Pattern;
 import javax.validation.constraints.Size;
 
 /**
  *
  * @author Gauthier
  */
 @Entity
 public class Account implements Serializable {
     private static final long serialVersionUID = 1L;
     
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Long id;
     
     @ManyToOne
     private Customer customer;
     
     @NotNull
     @Size(min=10, max=100)
     private String name;
     
     @NotNull
     private Float balance;
     
     @NotNull
     @Size(min=23, max=23)
     @Pattern(regexp="^(\\d{5})(\\d{5})(\\w{11})(\\d{2})$")
     protected String bban;
     
     @OneToMany(fetch = FetchType.EAGER)
    @OrderBy(value="id DESC")
     private Set<AccountOperation> operations;
     
     @ManyToOne
     private InterestsPlan interestsPlan;
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
     
     public Customer getCustomer() {
         return customer;
     }
     
     public void setCustomer(Customer customer) {
         this.customer = customer;
     }
 
     /**
      * @return the name
      */
     public String getName() {
         return name;
     }
 
     /**
      * @param name the name to set
      */
     public void setName(String name) {
         this.name = name;
     }
     
     /**
      * @return the balance
      */
     public Float getBalance() {
         return balance;
     }
 
     /**
      * @param balance the balance to set
      */
     public void setBalance(Float balance) {
         this.balance = balance;
     }
     
     /**
      * @return the interestsPlan
      */
     public InterestsPlan getInterestsPlan() {
         return interestsPlan;
     }
 
     /**
      * @param interestsPlan the interestsPlan to set
      */
     public void setInterestsPlan(InterestsPlan interestsPlan) {
         this.interestsPlan = interestsPlan;
     }
     
     /**
      * @return the interestsPlan
      */
     public Set<AccountOperation> getOperations() {
         return operations;
     }
 
     /**
      * @param interestsPlan the interestsPlan to set
      */
     public void setOperations(Set<AccountOperation> operations) {
         this.operations = operations;
     }
     
     public void addOperation(AccountOperation operation)
     {
         operations.add(operation);
         operation.setAccount(this);
     }
     
     public String getBban()
     {
         return bban;
     }
     
     public void setBban(String bban)
     {
         this.bban = bban;
     }
 }
