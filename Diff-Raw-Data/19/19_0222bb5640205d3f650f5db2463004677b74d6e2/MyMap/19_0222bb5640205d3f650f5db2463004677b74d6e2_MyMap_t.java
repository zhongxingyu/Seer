 package cz.muni.fi.pv243.mymaps.dto;
 
 import java.util.ArrayList;
 import javax.validation.Valid;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 
 public class MyMap {
 
     private Long id;
 
     @NotNull
     @Size(min = 1)
     private String name;
 
     private List<PointOfInterest> pointsOfInterest;
 
     @NotNull
     @Valid
     private View view;
 
     @NotNull
     private Date creationDate;
 
     @NotNull
     @Valid
     private User creator;
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public List<PointOfInterest> getPointsOfInterest() {
         if(pointsOfInterest == null){
            return new ArrayList<>();
         }
         
         return Collections.unmodifiableList(pointsOfInterest);
     }
 
     public void setPointsOfInterest(Collection<PointOfInterest> pointsOfInterest) {        
         this.pointsOfInterest = new ArrayList<>();
         this.pointsOfInterest.addAll(pointsOfInterest);
     }
 
     public View getView() {
         return view;
     }
 
     public void setView(View view) {
         this.view = view;
     }
 
     public Date getCreationDate() {
         return new Date(creationDate.getTime());
     }
 
     public void setCreationDate(Date creationDate) {
         this.creationDate = new Date(creationDate.getTime());
     }
 
     public User getCreator() {
         return creator;
     }
 
     public void setCreator(User creator) {
         this.creator = creator;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         MyMap myMap = (MyMap) o;
 
         if (id != null ? !id.equals(myMap.id) : myMap.id != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         return id != null ? id.hashCode() : 0;
     }
 }
