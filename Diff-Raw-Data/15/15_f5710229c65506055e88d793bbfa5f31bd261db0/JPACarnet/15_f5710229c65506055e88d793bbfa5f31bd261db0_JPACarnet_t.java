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
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.Lob;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
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
 @Table(name = "Carnet")
 @XmlRootElement
 @XmlAccessorType(XmlAccessType.FIELD)
 public class JPACarnet implements Carnet, Serializable {
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
     @Column(name = "CarnetDate", nullable = false, length = 10)
     private String date;
 
     @XmlElement
     @Column(name = "Texte")
     @Lob
     private String texte;
     
     @XmlAttribute
     @Column(name = "Picture", nullable = true)
     private Integer picture;
     
     @ManyToMany
     @JoinTable(name = "CarnetPhoto",
         joinColumns = {
           @JoinColumn(name="carnet", unique = true)           
         },
         inverseJoinColumns = {
           @JoinColumn(name="photo")
         }
     )
     private List<JPAPhoto> jPAPhotoList;
     
     @ManyToMany
     @JoinTable(name = "CarnetAlbum",
         joinColumns = {
           @JoinColumn(name="carnet", unique = true)           
         },
         inverseJoinColumns = {
           @JoinColumn(name="album")
         }
     )
     private List<JPAAlbum> jPAAlbumList;
     
     @JoinColumn(name = "Droit", referencedColumnName = "ID", nullable = false)
     @ManyToOne(optional = false, fetch = FetchType.LAZY)
     private JPAUtilisateur droit;
     
     @JoinColumn(name = "Theme", referencedColumnName = "ID", nullable = false)
     @ManyToOne(optional = false, fetch = FetchType.LAZY)
     private JPATheme theme;
 
     public JPACarnet() {
     }
 
     public JPACarnet(Integer id) {
         this.id = id;
     }
 
     public JPACarnet(Integer id, String nom, String date) {
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
     
     @Override
     public List<Album> getAlbumList() {
         return (List) jPAAlbumList;
     }
 
     @Override
     public void setAlbumList(List<Album> jPAAlbumList) {
         this.jPAAlbumList = (List) jPAAlbumList;
     }
     
     @Override
     public List<Photo> getPhotoList() {
         return (List) jPAPhotoList;
     }
 
     @Override
     public void setPhotoList(List<Photo> jPAPhotoList) {
         this.jPAPhotoList = (List) jPAPhotoList;
     }
 
     public String getTexte() {
         return texte;
     }
 
     public void setTexte(String texte) {
         this.texte = texte;
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
         JPACarnet other = (JPACarnet) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "net.wazari.dao.jpa.entity.JPACarnet[id=" + id + "]";
     }
 
     @Override
     public String getText() {
         return this.texte;
         
     }
 
     @Override
     public void setText(String text) {
         this.texte = text;
     }
 
 }
