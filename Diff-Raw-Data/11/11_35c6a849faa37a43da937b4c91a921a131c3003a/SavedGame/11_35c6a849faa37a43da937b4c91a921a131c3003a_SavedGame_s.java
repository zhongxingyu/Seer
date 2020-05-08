 /* 
  * Project       : Bachelor Thesis - Sudoku game implementation as portlet
  * Document      : SavedGame.java
  * Author        : Ondřej Fibich <xfibic01@stud.fit.vutbr.cz>
  * Organization: : FIT VUT <http://www.fit.vutbr.cz>
  */
 
 package org.gatein.portal.examples.games.sudoku.entity;
 
 import java.io.Serializable;
 import java.util.Date;
 import javax.persistence.*;
 import javax.xml.bind.annotation.XmlRootElement;
 
 /**
  * SavedGame Entity Class
  *
  * @author Ondřej Fibich
  */
 @Entity
 @Table(name = "saved_games")
 @XmlRootElement
 @NamedQueries({
     @NamedQuery(name = "SavedGame.findAll", query = "SELECT s FROM SavedGame s"),
    @NamedQuery(name = "SavedGame.findByUser", query = "SELECT s FROM SavedGame s WHERE s.gameSolutionId.userId = :uid")
 })
 public class SavedGame implements Serializable
 {
 
     private static final long serialVersionUID = 1L;
     
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Basic(optional = false)
     @Column(name = "id")
     private Integer id;
     
     @Basic(optional = false)
     @Column(name = "name")
     private String name;
     
     @Basic(optional = false)
     @Column(name = "saved")
     @Temporal(TemporalType.TIMESTAMP)
     private Date saved;
     
     @Basic(optional = false)
     @Column(name = "lasting")
     private int lasting;
     
     @Basic(optional = false)
     @Lob
     @Column(name = "values_")
     private String values;
     
     @JoinColumn(name = "game_solution_id", referencedColumnName = "id")
     @ManyToOne(optional = false)
     private GameSolution gameSolutionId;
 
     public SavedGame()
     {
     }
 
     public SavedGame(Integer id)
     {
         this.id = id;
     }
 
     public SavedGame(Integer id, int lasting, String values)
     {
         this.id = id;
         this.lasting = lasting;
         this.values = values;
     }
 
     public Integer getId()
     {
         return id;
     }
 
     public void setId(Integer id)
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
 
     public Date getSaved()
     {
         return saved;
     }
 
     public void setSaved(Date saved)
     {
         this.saved = saved;
     }
 
     public int getLasting()
     {
         return lasting;
     }
 
     public void setLasting(int lasting)
     {
         this.lasting = lasting;
     }
 
     public String getValues()
     {
         return values;
     }
 
     public void setValues(String values)
     {
         this.values = values;
     }
 
     public GameSolution getGameSolutionId()
     {
         return gameSolutionId;
     }
 
     public void setGameSolutionId(GameSolution gameSolutionId)
     {
         this.gameSolutionId = gameSolutionId;
     }
 
     @Override
     public int hashCode()
     {
         return id != null ? id.hashCode() : 0;
     }
 
     @Override
     public boolean equals(Object object)
     {
         if (!(object instanceof SavedGame))
         {
             return false;
         }
         
         SavedGame other = (SavedGame) object;
         
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
         return "org.gatein.portal.examples.games.entities.SavedGames[ id=" + id + " ]";
     }
 }
