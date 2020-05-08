 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.bounceme.dur.usenet.model;
 
 import java.io.Serializable;
 import javax.mail.Folder;
 import javax.persistence.*;
 import javax.xml.bind.annotation.XmlRootElement;
 
 /**
  *
  * @author thufir
  */
 @Entity
 @Table(name = "newsgroups", catalog = "nntp", schema = "")
 @XmlRootElement
 @NamedQueries({
     @NamedQuery(name = "Newsgroups.findAll", query = "SELECT n FROM Newsgroups n"),
     @NamedQuery(name = "Newsgroups.findById", query = "SELECT n FROM Newsgroups n WHERE n.id = :id"),
     @NamedQuery(name = "Newsgroups.findByHash", query = "SELECT n FROM Newsgroups n WHERE n.hash = :hash")})
 public class Newsgroups implements Serializable {
 
     private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Basic(optional = false)
     @Column(name = "id", nullable = false)
     private Integer id;
     @Basic(optional = false)
     @Column(name = "hash", nullable = false, length = 32)
     private String hash;
     @Basic(optional = false)
     @Lob
     @Column(name = "newsgroup", nullable = false, length = 2147483647)
     private String newsgroup;
 
     public Newsgroups() {
     }
 
     public Newsgroups(Folder f) {
         newsgroup = f.getFullName();
        hash = "dummy hash";
     }
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public String getHash() {
         return hash;
     }
 
     public void setHash(String hash) {
         this.hash = hash;
     }
 
     public String getNewsgroup() {
         return newsgroup;
     }
 
     public void setNewsgroup(String newsgroup) {
         this.newsgroup = newsgroup;
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
         if (!(object instanceof Newsgroups)) {
             return false;
         }
         Newsgroups other = (Newsgroups) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "net.bounceme.dur.usenet.model.Newsgroups[ id=" + id + " ]";
     }
 }
