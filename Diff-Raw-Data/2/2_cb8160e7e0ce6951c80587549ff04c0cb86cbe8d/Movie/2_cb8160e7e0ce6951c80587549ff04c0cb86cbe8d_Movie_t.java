 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package reserv.entity;
 
 import java.io.Serializable;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 import javax.persistence.*;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 import reserv.config.DBManager;
 
 /**
  *
  * @author muody
  */
 @Entity
 @Table(name = "movie")
 @XmlRootElement
 @NamedQueries({
     @NamedQuery(name = "Movie.findAll", query = "SELECT m FROM Movie m"),
     @NamedQuery(name = "Movie.findById", query = "SELECT m FROM Movie m WHERE m.id = :id"),
     @NamedQuery(name = "Movie.findByName", query = "SELECT m FROM Movie m WHERE m.name = :name"),
 })
 public class Movie implements Serializable {
     private static final long serialVersionUID = 1L;
     @Id
     @Basic(optional = false)
     @NotNull
     @Column(name = "id", nullable = false)
     private Integer id;
     @Size(max = 45)
     @Column(name = "name", length = 45)
     private String name;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "movie", fetch = FetchType.EAGER)
     private Set<Seance> seanceSet;
 
     public Movie() {
     }
 
     public Movie(Integer id) {
         this.id = id;
     }
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     @XmlTransient
     public Set<Seance> getSeanceSet() {
         return seanceSet;
     }
 
     public void setSeanceSet(Set<Seance> seanceSet) {
         this.seanceSet = seanceSet;
     }
     
     public List<Movie> getTodaySeance(){
         
         EntityManager em = DBManager.getManager().createEntityManager();
         Date now = new Date();
         SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
         String actual = formatter.format(now);
         
         String begin_actual = actual + " 00:00:00";
         String end_actual = actual + " 23:59:59";
         String sql = "SELECT s FROM Seance s WHERE s.seanceDate >= '"+begin_actual+"' "
                + "AND s.seanceDate <= '"+end_actual+"' AND s.movie.id = "+this.id;
         System.out.println(sql);
         List<Movie> list = em
                 .createQuery(sql).getResultList();
         
         return list;
         
     }
 
     @Override
     public int hashCode() {
         int hash = 0;
         hash += (id != null ? id.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object object) {
         // TODO: Warning - this method won't work in the case the id fields are not set
         if (!(object instanceof Movie)) {
             return false;
         }
         Movie other = (Movie) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "reserv.entity.Movie[ id=" + id + " ]";
     }
     
 }
