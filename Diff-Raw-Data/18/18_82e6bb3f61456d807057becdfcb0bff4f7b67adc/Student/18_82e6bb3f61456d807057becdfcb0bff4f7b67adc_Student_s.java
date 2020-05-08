 package elw.vo;
 
 public class Student implements IdNamed {
     private String id;
     public String getId() { return id; }
     public void setId(String id) { this.id = id; }
 
     private String name;
     public String getName() { return name; }
     public void setName(String name) { this.name = name; }
 
     private String email;
     public String getEmail() { return email; }
     public void setEmail(String email) { this.email = email; }
 
     private boolean lead;
     public boolean isLead() { return lead; }
     public void setLead(boolean lead) { this.lead = lead; }
 }
