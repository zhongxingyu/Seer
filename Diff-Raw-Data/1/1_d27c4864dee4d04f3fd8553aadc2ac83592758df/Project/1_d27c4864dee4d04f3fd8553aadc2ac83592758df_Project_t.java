 package com.mike.domain;
 
 import org.hibernate.validator.constraints.NotEmpty;
 
 import javax.persistence.*;
 
 /**
  * Represents a work entity.
  * <p/>
  * User: mike
  * Date: 2/2/13
  * Time: 8:36 AM
  */
 @Entity
 @Table(name = "works")
 @NamedQueries({
         @NamedQuery(name = Project.FIND_BY_ID, query = "select p from Project p where p.id = :id"),
         @NamedQuery(name = Project.FIND_BY_NAME, query = "select p from Project p where p.name = :name")
 })
 public class Project
 {
     public static final String FIND_BY_ID = "work.findById";
     public static final String FIND_BY_NAME = "work.findByName";
 
     @Id
     @GeneratedValue
     private Long id;
 
     @NotEmpty(message = "{project.name.empty}")
     private String name;
 
    @NotEmpty(message = "{project.description.empty}")
     private String description;
 
     public Long getId()
     {
         return id;
     }
 
     public void setId(Long id)
     {
         this.id = id;
     }
 
     public String getName()
     {
         return name;
     }
 
     public void setName(String name)
     {
         this.name = name;
     }
 
     public String getDescription()
     {
         return description;
     }
 
     public void setDescription(String description)
     {
         this.description = description;
     }
 
     @Override
     public boolean equals(Object o)
     {
         if (this == o)
         {
             return true;
         }
         if (o == null || getClass() != o.getClass())
         {
             return false;
         }
 
         Project project = (Project) o;
 
         if (description != null ? !description.equals(project.description) : project.description != null)
         {
             return false;
         }
         if (id != null ? !id.equals(project.id) : project.id != null)
         {
             return false;
         }
         if (name != null ? !name.equals(project.name) : project.name != null)
         {
             return false;
         }
 
         return true;
     }
 
     @Override
     public int hashCode()
     {
         int result = id != null ? id.hashCode() : 0;
         result = 31 * result + (name != null ? name.hashCode() : 0);
         result = 31 * result + (description != null ? description.hashCode() : 0);
         return result;
     }
 
     @Override
     public String toString()
     {
         return "Project{" +
                 "id=" + id +
                 ", name='" + name + '\'' +
                 ", description='" + description + '\'' +
                 '}';
     }
 }
