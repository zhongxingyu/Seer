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
 import javax.persistence.UniqueConstraint;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 import net.wazari.dao.entity.Album;
 import net.wazari.dao.entity.Carnet;
 import net.wazari.dao.entity.Photo;
 import net.wazari.dao.entity.TagPhoto;
 import org.hibernate.annotations.GenericGenerator;
 
 /**
  *
  * @author kevinpouget
  */
 @XmlRootElement
 @XmlAccessorType(XmlAccessType.FIELD)
 @Entity
 @Table(name = "Photo",
     uniqueConstraints = {@UniqueConstraint(columnNames={"PhotoPath"})}
 )
 public class JPAPhoto implements Photo, Serializable {
     private static final Logger log = LoggerFactory.getLogger(JPAPhoto.class.getName());
 
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
     @Column(name = "PhotoPath", nullable = false, length = 100)
     private String path;
 
     @XmlElement
     @Column(name = "Description", length = 200)
     private String description;
 
     @XmlElement
     @Column(name = "Stars", length = 2, nullable = false)
     private Integer stars = 3;
     
     @XmlElement
     @Column(name = "Model", length = 100)
     private String model;
 
     @XmlElement
     @Column(name = "DateMeta", length = 50)
     private String date;
 
     
     @XmlElement
     @Column(name = "Iso", length = 50)
     private String iso;
 
     @XmlElement
     @Column(name = "Exposure", length = 50)
     private String exposure;
 
     @XmlElement
     @Column(name = "Focal", length = 50)
     private String focal;
 
     @XmlElement
     @Column(name = "Flash", length = 50)
     private String flash;
 
     @XmlElement
     @Column(name = "Height", length = 50)
     private String height;
 
     @XmlElement
     @Column(name = "Width", length = 50)
     private String width;
 
     @XmlElement
     @Column(name = "Type", length = 50)
     private String type;
 
     @XmlAttribute
     @JoinColumn(name = "Droit", nullable = true)
     private Integer droit;
 
     @XmlTransient
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "photo", fetch = FetchType.LAZY)
     private List<JPATagPhoto> jPATagPhotoList;
 
     @XmlTransient
     @JoinColumn(name = "Album", referencedColumnName = "ID", nullable = false)
     @ManyToOne(optional = false, fetch = FetchType.LAZY)
     private JPAAlbum album;
 
     @XmlTransient
     @Transient
     private Integer albumId ;
 
     @ManyToMany(mappedBy="jPAPhotoList")
     private List<JPACarnet> jPACarnetList;
 
     
     
     public JPAPhoto() {
     }
 
     public JPAPhoto(Integer id) {
         this.id = id;
     }
 
     public JPAPhoto(Integer id, String path) {
         this.id = id;
         this.path = path;
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
     public Integer getStars() {
         return stars;
     }
 
     @Override
     public void setStars(Integer stars) {
         this.stars = stars;
     }
 
     @Override
     public String getPath() {
         return path;
     }
 
     @Override
     public void setPath(String path) {
         this.path = path;
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
     public String getModel() {
         return model;
     }
 
     @Override
     public void setModel(String model) {
         this.model = model;
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
     public String getIso() {
         return iso;
     }
 
     @Override
     public void setIso(String iso) {
         this.iso = iso;
     }
 
     @Override
     public String getExposure() {
         return exposure;
     }
 
     @Override
     public void setExposure(String exposure) {
         this.exposure = exposure;
     }
 
     @Override
     public String getFocal() {
         return focal;
     }
 
     @Override
     public void setFocal(String focal) {
         this.focal = focal;
     }
 
     @Override
     public String getFlash() {
         return flash;
     }
 
     @Override
     public void setFlash(String flash) {
         this.flash = flash;
     }
 
     @Override
     public String getHeight() {
         return height;
     }
 
     @Override
     public void setHeight(String height) {
         this.height = height;
     }
 
     @Override
     public String getWidth() {
         return width;
     }
 
     @Override
     public void setWidth(String width) {
         this.width = width;
     }
 
     @Override
     public String getType() {
         return type;
     }
 
     @Override
     public void setType(String type) {
         this.type = type;
     }
 
     @Override
     public Integer getDroit() {
         return droit;
     }
 
     @Override
     public void setDroit(Integer droit) {
         this.droit = droit;
     }
 
     @Override
     public List<TagPhoto> getTagPhotoList() {
         return (List) jPATagPhotoList;
     }
 
     @Override
     public void setTagPhotoList(List<TagPhoto> jPATagPhotoList) {
         this.jPATagPhotoList = (List) jPATagPhotoList;
     }
 
     @Override
     public Album getAlbum() {
         return album;
     }
 
     @Override
     public void setAlbum(Album album) {
         this.album = (JPAAlbum) album;
     }
     
     @Override
     public List<Carnet> getCarnetList() {
         return (List) jPACarnetList;
     }
 
     @Override
     public void setCarnetList(List<Carnet> jPACarnetList) {
         this.jPACarnetList = (List) jPACarnetList;
     }
 
     @XmlAttribute
     public Integer getAlbumId() {
         if (album == null) {
             return albumId ;
         } else {
             return album.getId() ;
         }
     }
 
     public void setAlbumId(Integer albumId) {
         this.albumId = albumId ;
     }
 
     @Override
     public int hashCode() {
         int hash = 0;
         hash += (id != null ? id.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object object) {
         if (!(object instanceof JPAPhoto)) {
             return false;
         }
         JPAPhoto other = (JPAPhoto) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "net.wazari.dao.jpa.entity.JPAPhoto[id=" + id + "]";
     }
 
 }
