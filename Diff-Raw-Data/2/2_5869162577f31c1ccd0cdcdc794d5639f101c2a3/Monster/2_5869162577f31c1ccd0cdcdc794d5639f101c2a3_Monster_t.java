 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.muni.fi.pa165.entities;
 
 import com.muni.fi.pa165.enums.MonsterClass;
 import java.io.Serializable;
 import java.util.Collection;
 import javax.persistence.Basic;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 
 /**
  *
  * @author Auron
  */
 @Entity
 @Table(name = "MONSTER")
 @XmlRootElement
 @NamedQueries({
     @NamedQuery(name = "Monster.findAll", query = "SELECT m FROM Monster m"),
     @NamedQuery(name = "Monster.findById", query = "SELECT m FROM Monster m WHERE m.id = :id"),
     @NamedQuery(name = "Monster.findByAgility", query = "SELECT m FROM Monster m WHERE m.agility = :agility"),
     @NamedQuery(name = "Monster.findByDangerlevel", query = "SELECT m FROM Monster m WHERE m.dangerlevel = :dangerlevel"),
     @NamedQuery(name = "Monster.findByDescription", query = "SELECT m FROM Monster m WHERE m.description = :description"),
     @NamedQuery(name = "Monster.findByHeight", query = "SELECT m FROM Monster m WHERE m.height = :height"),
     @NamedQuery(name = "Monster.findByImagepath", query = "SELECT m FROM Monster m WHERE m.imagepath = :imagepath"),
     @NamedQuery(name = "Monster.findByMonsterclass", query = "SELECT m FROM Monster m WHERE m.monsterclass = :monsterclass"),
     @NamedQuery(name = "Monster.findByName", query = "SELECT m FROM Monster m WHERE m.name = :name"),
     @NamedQuery(name = "Monster.findByStamina", query = "SELECT m FROM Monster m WHERE m.stamina = :stamina"),
     @NamedQuery(name = "Monster.findByStrength", query = "SELECT m FROM Monster m WHERE m.strength = :strength"),
     @NamedQuery(name = "Monster.findByWeight", query = "SELECT m FROM Monster m WHERE m.weight = :weight")})
 public class Monster implements Serializable {
     private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Basic(optional = false)
     @Column(name = "ID")
     private Long id;
     // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
     @Column(name = "AGILITY")
     private Double agility;
     @Column(name = "DANGERLEVEL")
     private Double dangerlevel;
     @Column(name = "DESCRIPTION")
     private String description;
     @Column(name = "HEIGHT")
     private Double height;
     @Column(name = "IMAGEPATH")
     private String imagepath;
     @Column(name = "MONSTERCLASS")
    @Enumerated(EnumType.STRING)
     private MonsterClass monsterclass;
     @Column(name = "NAME")
     private String name;
     @Column(name = "STAMINA")
     private Double stamina;
     @Column(name = "STRENGTH")
     private Double strength;
     @Column(name = "WEIGHT")
     private Double weight;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "monster")
     private Collection<Monsterweapon> monsterweaponCollection;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "monster")
     private Collection<Monsterarea> monsterareaCollection;
 
     public Monster() {
     }
 
     public Monster(Long id) {
         this.id = id;
     }
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public Double getAgility() {
         return agility;
     }
 
     public void setAgility(Double agility) {
         this.agility = agility;
     }
 
     public Double getDangerlevel() {
         return dangerlevel;
     }
 
     public void setDangerlevel(Double dangerlevel) {
         this.dangerlevel = dangerlevel;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public Double getHeight() {
         return height;
     }
 
     public void setHeight(Double height) {
         this.height = height;
     }
 
     public String getImagepath() {
         return imagepath;
     }
 
     public void setImagepath(String imagepath) {
         this.imagepath = imagepath;
     }
 
     public MonsterClass getMonsterclass() {
         return monsterclass;
     }
 
     public void setMonsterclass(MonsterClass monsterclass) {
         this.monsterclass = monsterclass;
     }
 
 
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public Double getStamina() {
         return stamina;
     }
 
     public void setStamina(Double stamina) {
         this.stamina = stamina;
     }
 
     public Double getStrength() {
         return strength;
     }
 
     public void setStrength(Double strength) {
         this.strength = strength;
     }
 
     public Double getWeight() {
         return weight;
     }
 
     public void setWeight(Double weight) {
         this.weight = weight;
     }
 
     @XmlTransient
     public Collection<Monsterweapon> getMonsterweaponCollection() {
         return monsterweaponCollection;
     }
 
     public void setMonsterweaponCollection(Collection<Monsterweapon> monsterweaponCollection) {
         this.monsterweaponCollection = monsterweaponCollection;
     }
 
     @XmlTransient
     public Collection<Monsterarea> getMonsterareaCollection() {
         return monsterareaCollection;
     }
 
     public void setMonsterareaCollection(Collection<Monsterarea> monsterareaCollection) {
         this.monsterareaCollection = monsterareaCollection;
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
         if (!(object instanceof Monster)) {
             return false;
         }
         Monster other = (Monster) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "com.muni.fi.pa165.entities.Monster[ id=" + id + " ]";
     }
     
 }
