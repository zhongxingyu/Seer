 /* 
  * Project       : Bachelor Thesis - Sudoku game implementation as portlet
  * Document      : Games.java
  * Author        : Ondřej Fibich <xfibic01@stud.fit.vutbr.cz>
  * Organization: : FIT VUT <http://www.fit.vutbr.cz>
  */
 
 package org.gatein.portal.examples.games.entities;
 
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.Date;
 import javax.persistence.*;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 
 /**
  * Games Entity Class
  *
  * @author Ondřej Fibich
  */
 @Entity
 @Table(name = "games")
 @XmlRootElement
 @NamedQueries({
     @NamedQuery(name = "Games.findAll", query = "SELECT g FROM Games g"),
     @NamedQuery(name = "Games.findById", query = "SELECT g FROM Games g WHERE g.id = :id"),
     @NamedQuery(name = "Games.findByInitDate", query = "SELECT g FROM Games g WHERE g.initDate = :initDate"),
     @NamedQuery(name = "Games.findByType", query = "SELECT g FROM Games g WHERE g.type = :type"),
     @NamedQuery(name = "Games.findByTypeDificulty", query = "SELECT g FROM Games g WHERE g.typeDificulty = :typeDificulty")
 })
 public class Games implements Serializable
 {
 
     private static final long serialVersionUID = 1L;
     
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Basic(optional = false)
     @Column(name = "id")
     private Integer id;
     
     @Basic(optional = false)
     @Column(name = "init_date")
     @Temporal(TemporalType.TIMESTAMP)
     private Date initDate;
     
     @Basic(optional = false)
     @Lob
     @Column(name = "init_values")
     private String initValues;
     
     @Basic(optional = false)
     @Column(name = "type")
     private String type;
     
     @Column(name = "type_dificulty")
     private String typeDificulty;
     
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "gameId")
     private Collection<GameSolutions> gameSolutionsCollection;
     
     @JoinColumn(name = "type_service_id", referencedColumnName = "id")
     @ManyToOne
     private Services typeServiceId;
 
     public Games()
     {
     }
 
     public Games(Integer id)
     {
         this.id = id;
     }
 
     public Games(Integer id, Date initDate, String initValues, String type)
     {
         this.id = id;
         this.initDate = initDate;
         this.initValues = initValues;
         this.type = type;
     }
 
     public Integer getId()
     {
         return id;
     }
 
     public void setId(Integer id)
     {
         this.id = id;
     }
 
     public Date getInitDate()
     {
         return initDate;
     }
 
     public void setInitDate(Date initDate)
     {
         this.initDate = initDate;
     }
 
     public String getInitValues()
     {
         return initValues;
     }
 
     public void setInitValues(String initValues)
     {
         this.initValues = initValues;
     }
 
     public String getType()
     {
         return type;
     }
 
     public void setType(String type)
     {
         this.type = type;
     }
 
     public String getTypeDificulty()
     {
         return typeDificulty;
     }
 
     public void setTypeDificulty(String typeDificulty)
     {
         this.typeDificulty = typeDificulty;
     }
 
     @XmlTransient
     public Collection<GameSolutions> getGameSolutionsCollection()
     {
         return gameSolutionsCollection;
     }
 
     public void setGameSolutionsCollection(Collection<GameSolutions> gameSolutionsCollection)
     {
         this.gameSolutionsCollection = gameSolutionsCollection;
     }
 
     public Services getTypeServiceId()
     {
         return typeServiceId;
     }
 
     public void setTypeServiceId(Services typeServiceId)
     {
         this.typeServiceId = typeServiceId;
     }
 
     @Override
     public int hashCode()
     {
         int hash = 0;
         hash += (id != null ? id.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object object)
     {
        // TODO: Warning - this method won't work in the case the id fields are not set
         if (!(object instanceof Games)) {
             return false;
         }
         
         Games other = (Games) object;
         
         if ((this.id == null && other.id != null) ||
             (this.id != null && !this.id.equals(other.id)))
         {
             return false;
         }
         
         return true;
     }
 
     @Override
     public String toString()
     {
         return "org.gatein.portal.examples.games.entities.Games[ id=" + id + " ]";
     }
 }
