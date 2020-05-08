 package gov.nysenate.billbuzz.model;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 public class BillBuzzUser
 {
     private Long id;
     private String email;
     private String firstName;
     private String lastName;
     private boolean activated;
     private Date createdAt;
     private Date confirmedAt;
     private List<BillBuzzSubscription> subscriptions = new ArrayList<BillBuzzSubscription>();
 
     public BillBuzzUser()
     {
 
     }
 
     public BillBuzzUser(String email, String firstName, String lastName, Date createdAt)
     {
         this.setEmail(email);
         this.setFirstName(firstName);
         this.setLastName(lastName);
         this.setActivated(false);
         this.setCreatedAt(createdAt);
     }
 
     public Long getId()
     {
         return id;
     }
 
     public void setId(Long id)
     {
         this.id = id;
     }
 
     public String getEmail()
     {
         return email;
     }
 
     public void setEmail(String email)
     {
         this.email = email;
     }
 
     public String getFirstName()
     {
         return firstName;
     }
 
     public void setFirstName(String firstName)
     {
         this.firstName = firstName;
     }
 
     public String getLastName()
     {
         return lastName;
     }
 
     public void setLastName(String lastName)
     {
         this.lastName = lastName;
     }
 
     public boolean isActivated()
     {
         return activated;
     }
 
     public void setActivated(boolean isActivated)
     {
         this.activated = isActivated;
     }
 
     public Date getCreatedAt()
     {
         return createdAt;
     }
 
     public void setCreatedAt(Date createdAt)
     {
         this.createdAt = createdAt;
     }
 
     public Date getConfirmedAt()
     {
         return confirmedAt;
     }
 
     public void setConfirmedAt(Date confirmedAt)
     {
         this.confirmedAt = confirmedAt;
     }
 
     public List<BillBuzzSubscription> getSubscriptions()
     {
         return subscriptions;
     }
 
     public void setSubscriptions(List<BillBuzzSubscription> subscriptions)
     {
         this.subscriptions = subscriptions;
     }
 
     public boolean equals(Object other)
     {
         if (other instanceof BillBuzzUser) {
            return id.equals(((BillBuzzUser)other).getId());
         }
         else {
             return false;
         }
     }
 }
