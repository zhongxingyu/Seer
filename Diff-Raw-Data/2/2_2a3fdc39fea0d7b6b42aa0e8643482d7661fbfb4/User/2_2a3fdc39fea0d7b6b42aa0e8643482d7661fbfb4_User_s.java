 package tjp.domain;
 
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import javax.persistence.*;
 import javax.validation.constraints.Size;
 import org.hibernate.validator.constraints.NotBlank;
 
 
 
 @Entity(name = "USERS")
 public class User implements Serializable {
 
     @Id
     @GeneratedValue(strategy = GenerationType.TABLE)
     private Long id;
     @Column(unique = true)
     @NotBlank
     private String username;
     @NotBlank
     @Size(min=6, max=35, message="Please choose a password between 6-35 characters")
     private String password;
     @ManyToMany(mappedBy = "users", cascade = CascadeType.ALL)
     private List<Role> roles;
     @OneToMany
     private List<MultiPartFile> files;
    boolean Admin;
     
     public User() {
         roles = new ArrayList<Role>();
         files = new ArrayList<MultiPartFile>();
         
  
     }
     
     public void addUserRole(Role role) {
         roles.add(role);
     }
     
     public void addFile(MultiPartFile file) {
         files.add(file);
     }
     
     
     public List<MultiPartFile> getFiles() {
         return files;
     }
 
     public void setFiles(List<MultiPartFile> files) {
         this.files = files;
     }
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public String getPassword() {
         return password;
     }
     
     
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public List<Role> getRoles() {
         return roles;
     }
 
     public void setRoles(List<Role> roles) {
         this.roles = roles;
     }
 
     public String getUsername() {
         return username;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
     
     
     
 }
