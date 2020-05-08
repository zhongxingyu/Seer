 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarpweb.domain;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import javax.persistence.*;
 import no.hials.muldvarpweb.fragments.Fragment;
 
 /**
  * This class represents a programme.
  * 
  * @author johan
  */
 
 @Entity
 @Table(name="Programme")
 public class Programme implements Serializable {
     
     @Id
     @GeneratedValue
     private Long id;
     
     @Column(name="name")
     String name;
     
     @Column(name="description", length = 2048)
     String description;
     
     @Column(name="detail")
     String detail;
     
     @Column(name="ectscredits")
     String ECTScredits;
     
     @Column(name="prerequisites")
     String prerequisites;
 
     @Column(name="pstructure", length = 2048)
     String pstructure;
     
     @ManyToMany
     List<Course> courses;
 
     /**
      * Empty constructor for the Programme JPA class.
      */
     public Programme(){
         
     }
     
     /**
      * This is the constructor for the Programme JPA class.
      * 
      * @param name name of the Programme.
      * @param detail details about the Programme.
      */
     public Programme(String name, String detail) {
         this.name = name;
         this.detail = detail;   
     }
     
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public String getECTScredits() {
         return ECTScredits;
     }
 
     public void setECTScredits(String ECTScredits) {
         this.ECTScredits = ECTScredits;
     }
 
     public String getPrerequisites() {
         return prerequisites;
     }
 
     public void setPrerequisites(String prerequisites) {
         this.prerequisites = prerequisites;
     }
 
     public String getPstructure() {
         return pstructure;
     }
 
     public void setPstructure(String structure) {
         this.pstructure = structure;
     }
     
     public String getDetail() {
         return detail;
     }
 
     public void setDetail(String detail) {
         this.detail = detail;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
     
     public void addCourse(Course c) {
         getCourses().add(c);
     }
 
     public List<Course> getCourses() {
         if(courses == null) {
             courses = new ArrayList<Course>();
         }
         
         return courses;
     }
 
     public void setCourses(List<Course> courses) {
         this.courses = courses;
     }
 
     public void removeCourse(Course c) {
         getCourses().remove(c);
     }
 
     // eksperimentelle greier
    @OneToMany
     List<Fragment> fragmentBundle;
 
     public List<Fragment> getFragmentBundle() {
         if(fragmentBundle == null) {
             fragmentBundle = new ArrayList<Fragment>();
         }
         return fragmentBundle;
     }
 
     public void setFragmentBundle(List<Fragment> fragmentBundle) {
         this.fragmentBundle = fragmentBundle;
     }
     
     public void addFragment(Fragment f) {
         if(fragmentBundle == null) {
             fragmentBundle = new ArrayList<Fragment>();
         }
         fragmentBundle.add(f);
     }
     
     public void removeFragment(Fragment f) {
         fragmentBundle.remove(f);
     }
 }
