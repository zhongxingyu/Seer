 package by.vsu.emdsproject.model;
 
 import javax.persistence.*;
 
 @Entity
 @Table(name = "teacher")
 public class Teacher {
 
     private Long id;
     private String fisrtName;
     private String lastName;
     private String middleName;
     private String rank;
 
     public Teacher() {
     }
 
     public Teacher(String fisrtName, String lastName, String middleName) {
         this.fisrtName = fisrtName;
         this.lastName = lastName;
         this.middleName = middleName;
     }
 
     @Id
     @Column(name = "id_teacher")
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     public Long getId() {
         return id;
     }
 
     @Column(name = "fisrt_name", nullable = false, length = 25)
     public String getFisrtName() {
         return fisrtName;
     }
 
     @Column(name = "last_name", nullable = false, length = 50)
     public String getLastName() {
         return lastName;
     }
 
     @Column(name = "middle_name", nullable = false, length = 30)
     public String getMiddleName() {
         return middleName;
     }
 
     @Column(name = "rank", length = 20)
     public String getRank() {
         return rank;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public void setFisrtName(String fisrtName) {
         this.fisrtName = fisrtName;
     }
 
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 
     public void setMiddleName(String middleName) {
         this.middleName = middleName;
     }
 
     public void setRank(String rank) {
         this.rank = rank;
     }
 }
