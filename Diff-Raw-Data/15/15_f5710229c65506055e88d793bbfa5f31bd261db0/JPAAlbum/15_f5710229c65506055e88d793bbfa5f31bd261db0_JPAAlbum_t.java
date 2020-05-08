 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package net.wazari.dao.jpa.entity;
 
 import java.io.Serializable;
 import java.util.List;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import javax.persistence.Basic;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 import net.wazari.dao.entity.Album;
 import net.wazari.dao.entity.Carnet;
 import net.wazari.dao.entity.Photo;
 import net.wazari.dao.entity.Theme;
 import net.wazari.dao.entity.Utilisateur;
 import org.hibernate.annotations.GenericGenerator;
 
 /**
  *
  * @author kevinpouget
  */
 @Entity
 @Table(name = "Album")
 @XmlRootElement
 @XmlAccessorType(XmlAccessType.FIELD)
 public class JPAAlbum implements Album, Serializable {
     private static final Logger log = LoggerFactory.getLogger(JPACarnet.class.getName());
 
     private static final long serialVersionUID = 1L;
 
     @XmlAttribute
     @Id
     @Basic(optional = false)
     @GeneratedValue(strategy=GenerationType.IDENTITY, generator="IdOrGenerated")
     @GenericGenerator(name="IdOrGenerated",
                       strategy="net.wazari.dao.jpa.entity.idGenerator.UseIdOrGenerate"
     )
     @Column(name = "ID", nullable = false)
     private Integer id;
 
     @XmlElement
     @Basic(optional = false)
     @Column(name = "Nom", nullable = false, length = 100)
     private String nom;
 
     @XmlElement
     @Column(name = "Description", length = 255)
     private String description;
 
     @XmlAttribute
     @Basic(optional = false)
     @Column(name = "AlbumDate", nullable = false, length = 10)
     private String date;
 
     @XmlAttribute
     @Column(name = "Picture", nullable = true)
     private Integer picture;
 
     @XmlTransient
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "album", fetch = FetchType.LAZY)
     private List<JPAPhoto> jPAPhotoList;
 
     @XmlTransient
     @JoinColumn(name = "Droit", referencedColumnName = "ID", nullable = false)
     @ManyToOne(optional = false, fetch = FetchType.LAZY)
     private JPAUtilisateur droit;
     
     @XmlTransient
     @JoinColumn(name = "Theme", referencedColumnName = "ID", nullable = false)
     @ManyToOne(optional = false, fetch = FetchType.LAZY)
     private JPATheme theme;
 
    @XmlTransient
     @ManyToMany(mappedBy="jPAAlbumList")
     private List<JPACarnet> jPACarnetList;
     
     @XmlTransient
     @Transient
     private Integer themeId = null ;
 
     @XmlTransient
     @Transient
     private Integer droitId = null ;
 
     public JPAAlbum() {
     }
 
     public JPAAlbum(Integer id) {
         this.id = id;
     }
 
     public JPAAlbum(Integer id, String nom, String date) {
         this.id = id;
         this.nom = nom;
         this.date = date;
     }
 
     @Override
     public Integer getId() {
         return id;
     }
 
     @Override
     public void setId(Integer id) {
         this.id = id;
     }
 
     @Override
     public String getNom() {
         return nom;
     }
 
     @Override
     public void setNom(String nom) {
         this.nom = nom;
     }
 
     @Override
     public String getDescription() {
         return description;
     }
 
     @Override
     public void setDescription(String description) {
         this.description = description;
     }
 
     @Override
     public String getDate() {
         return date;
     }
 
     @Override
     public void setDate(String date) {
         this.date = date;
     }
 
     @Override
     public Integer getPicture() {
         return picture;
     }
 
     @Override
     public void setPicture(Integer picture) {
         this.picture = picture;
     }
 
     @Override
     public List<Photo> getPhotoList() {
         return (List) jPAPhotoList;
     }
 
     @Override
     public void setPhotoList(List<Photo> jPAPhotoList) {
         this.jPAPhotoList = (List) jPAPhotoList;
     }
 
     @Override
     public Utilisateur getDroit() {
         return (Utilisateur) droit;
     }
 
     @Override
     public void setDroit(Utilisateur droit) {
         this.droit = (JPAUtilisateur) droit;
     }
 
     @Override
     public Theme getTheme() {
         return (Theme) theme;
     }
 
     @Override
     public void setTheme(Theme theme) {
         this.theme = (JPATheme) theme;
     }
 
     @XmlAttribute
     public Integer getDroitId() {
         if (droit == null) {
             return droitId ;
         } else {
             return droit.getId() ;
         }
     }
 
     public void setDroitId(Integer droitId) {
         this.droitId = droitId ;
     }
 
     @XmlAttribute
     public Integer getThemeId() {
         if (theme == null) {
             return themeId ;
         } else {
             return theme.getId() ;
         }
     }
 
     public void setThemeId(Integer themeId) {
         this.themeId = themeId ;
     }
     
     @Override
     public List<Carnet> getCarnetList() {
         return (List) jPACarnetList;
     }
 
     @Override
     public void setCarnetList(List<Carnet> jPACarnetList) {
         this.jPACarnetList = (List) jPACarnetList;
     }
 
     @Override
     public int hashCode() {
         int hash = 0;
         hash += (id != null ? id.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object object) {
         if (!(object instanceof JPAAlbum)) {
             return false;
         }
         JPAAlbum other = (JPAAlbum) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "net.wazari.dao.jpa.entity.JPAAlbum[id=" + id + "]";
     }
 
 }
