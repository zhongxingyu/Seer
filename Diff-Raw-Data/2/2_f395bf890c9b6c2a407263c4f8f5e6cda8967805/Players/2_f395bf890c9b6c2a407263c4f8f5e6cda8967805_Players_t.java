 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mycompany.mavenproject11;
 
 import java.io.Serializable;
 import java.util.Collection;
 import javax.persistence.*;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 import javax.xml.bind.annotation.XmlRootElement;
 import org.codehaus.jackson.annotate.JsonIgnore;
 
 /**
  *
  * @author grk
  */
 @Entity
@Table(name = "players")
 @XmlRootElement
 @NamedQueries({
     @NamedQuery(name = "Players.findAllActive", query = "SELECT p FROM Players p WHERE p.isActive = TRUE order by p.lastname"),
     @NamedQuery(name = "Players.findAllActiveByTeamId", query = "SELECT p FROM Players p WHERE p.isActive = TRUE and p.teamsId = :team order by p.lastname"),
     @NamedQuery(name = "Players.findByIdplayers", query = "SELECT p FROM Players p WHERE p.idplayers = :idplayers"),
     @NamedQuery(name = "Players.findByNickname", query = "SELECT p FROM Players p WHERE p.nickname = :nickname"),
     @NamedQuery(name = "Players.findByNumber", query = "SELECT p FROM Players p WHERE p.number = :number"),
     @NamedQuery(name = "Players.findByLastname", query = "SELECT p FROM Players p WHERE p.lastname = :lastname"),
     @NamedQuery(name = "Players.findByFirstname", query = "SELECT p FROM Players p WHERE p.firstname = :firstname"),
     @NamedQuery(name = "Players.findByBats", query = "SELECT p FROM Players p WHERE p.bats = :bats"),
     @NamedQuery(name = "Players.findByThrows1", query = "SELECT p FROM Players p WHERE p.throws1 = :throws1")})
 public class Players implements Serializable {
     
     //@OneToMany(cascade = CascadeType.ALL, )
     @OneToMany(fetch=FetchType.EAGER, mappedBy="players", targetEntity=Positions.class, cascade= CascadeType.ALL)
     private Collection<Positions> positionsCollection;
     
     /**
      * For one-to-many and many-to-many relationships, the default FetchType is LAZY. However, for one-to-one mappings, the default is EAGER, meaning that a class, and all of its one-to-one associations, are loaded into memory when it is invoked by the client. If this is not the behavior you want, you can set the FetchType to LAZY in your @OneToOne annotations.
      */
     @OneToOne
     @JoinColumn(name = "parents_idparents", referencedColumnName = "idparents")
     private Parents parents;
     
     @JoinColumn(name = "teams_id", referencedColumnName = "idteams")
     @ManyToOne(optional = false)
     private Teams teamsId;
     
     @OneToMany(cascade = CascadeType.ALL)
     @JoinColumn(name = "players_idplayers", referencedColumnName = "idplayers")
     private Collection<Photos> photosCollection;
 
     
     private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Basic(optional = false)
     @NotNull
     @Column(name = "idplayers")
     private Integer idplayers;
     @Basic(optional = false)
     @NotNull
     @Size(min = 1, max = 45)
     @Column(name = "nickname")
     private String nickname;
     @Basic(optional = false)
     @NotNull
     @Size(min = 1, max = 3)
     @Column(name = "number")
     private String number;
     @Basic(optional = false)
     @NotNull
     @Size(min = 1, max = 45)
     @Column(name = "lastname")
     private String lastname;
     @Size(max = 45)
     @Column(name = "firstname")
     private String firstname;
     @Size(max = 1)
     @Column(name = "bats")
     private String bats;
     @Size(max = 1)
     @Column(name = "throws")
     private String throws1;
     @Column(name = "active_b")
     private Boolean isActive;
 
     public Players() {
     }
 
     public Players(Integer idplayers) {
         this.idplayers = idplayers;
     }
 
     public Players(Integer idplayers, String nickname, String number, String lastname) {
         this.idplayers = idplayers;
         this.nickname = nickname;
         this.number = number;
         this.lastname = lastname;
     }
 
     public Integer getIdplayers() {
         return idplayers;
     }
 
     public void setIdplayers(Integer idplayers) {
         this.idplayers = idplayers;
     }
 
     public String getNickname() {
         return nickname;
     }
 
     public void setNickname(String nickname) {
         this.nickname = nickname;
     }
 
     public String getNumber() {
         return number;
     }
 
     public void setNumber(String number) {
         this.number = number;
     }
 
     public String getLastname() {
         return lastname;
     }
 
     public void setLastname(String lastname) {
         this.lastname = lastname;
     }
 
     public String getFirstname() {
         return firstname;
     }
 
     public void setFirstname(String firstname) {
         this.firstname = firstname;
     }
 
     public String getBats() {
         return bats;
     }
 
     public void setBats(String bats) {
         this.bats = bats;
     }
 
     public String getThrows1() {
         return throws1;
     }
 
     public void setThrows1(String throws1) {
         this.throws1 = throws1;
     }
 
     public Boolean getIsActive() {
         return isActive;
     }
 
     public void setIsActive(Boolean isActive) {
         this.isActive = isActive;
     }
     
     
     @Override
     public int hashCode() {
         int hash = 0;
         hash += (idplayers != null ? idplayers.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object object) {
         // TODO: Warning - this method won't work in the case the id fields are not set
         if (!(object instanceof Players)) {
             return false;
         }
         Players other = (Players) object;
         if ((this.idplayers == null && other.idplayers != null) || (this.idplayers != null && !this.idplayers.equals(other.idplayers))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "com.mycompany.mavenproject11.Players[ idplayers=" + idplayers + " ]";
     }
 
     public Collection<Positions> getPositionsCollection() {
         return positionsCollection;
     }
 
     public void setPositionsCollection(Collection<Positions> positionsCollection) {
         this.positionsCollection = positionsCollection;
     }
 
     public Parents getParents() {
         return parents;
     }
 
     public void setParents(Parents parents) {
         this.parents = parents;
     }
 
     public Teams getTeamsId() {
         return teamsId;
     }
 
     public void setTeamsId(Teams teamsId) {
         this.teamsId = teamsId;
     }
 
     @JsonIgnore
     public Collection<Photos> getPhotosCollection() {
         return photosCollection;
     }
 
     public void setPhotosCollection(Collection<Photos> photosCollection) {
         this.photosCollection = photosCollection;
     }
     
 }
