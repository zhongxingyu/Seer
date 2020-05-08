 package it.sevenbits.conferences.domain;
 
 import javax.persistence.*;
 
 @Entity
 @Table(name="user")
 public class User {
 
     private Long id;
     private String login;
     private String password;
     private String firstName;
     private String secondName;
     private String email;
     private String jobPosition;
     private String confirmationToken;
     private String photo;
     private String selfDescription;
     private boolean enabled;
     private Role role;
     private Company company;
 
     @Id
     @GeneratedValue
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     @Column(name = "login")
     public String getLogin() {
         return login;
     }
 
     public void setLogin(String login) {
         this.login = login;
     }
 
     @Column(name = "password")
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     @Column(name = "first_name")
     public String getFirstName() {
         return firstName;
     }
 
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
     @Column(name = "second_name")
     public String getSecondName() {
         return secondName;
     }
 
     public void setSecondName(String secondName) {
         this.secondName = secondName;
     }
 
     @Column(name = "email")
     public String getEmail() {
         return email;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     @Column(name = "job_position")
     public String getJobPosition() {
         return jobPosition;
     }
 
     public void setJobPosition(String jobPosition) {
         this.jobPosition = jobPosition;
     }
 
     @Column(name = "confirmation_token")
     public String getConfirmationToken() {
         return confirmationToken;
     }
 
     public void setConfirmationToken(String confirmationToken) {
         this.confirmationToken = confirmationToken;
     }
 
    @Column(name = "enabled")
     public boolean getEnabled() {
         return enabled;
     }
 
     public void setEnabled(boolean enabled) {
         this.enabled = enabled;
     }
 
     @OneToOne(cascade= CascadeType.ALL, fetch = FetchType.EAGER)
     @JoinTable(name="user_roles",
             joinColumns = {@JoinColumn(name="user_id", referencedColumnName="id")},
             inverseJoinColumns = {@JoinColumn(name="role_id", referencedColumnName="id")}
     )
     public Role getRole() {
         return role;
     }
 
     public void setRole(Role role) {
         this.role = role;
     }
 
     @Column(name = "photo")
     public String getPhoto() {
         return photo;
     }
 
     public void setPhoto(String photo) {
         this.photo = photo;
     }
 
     @ManyToOne
     @JoinColumn(name="company_id")
     public Company getCompany() {
         return company;
     }
 
     public void setCompany(Company company) {
         this.company = company;
     }
 
     @Column(name = "self_description")
     public String getSelfDescription() {
         return selfDescription;
     }
 
     public void setSelfDescription(String selfDescription) {
         this.selfDescription = selfDescription;
     }
 }
