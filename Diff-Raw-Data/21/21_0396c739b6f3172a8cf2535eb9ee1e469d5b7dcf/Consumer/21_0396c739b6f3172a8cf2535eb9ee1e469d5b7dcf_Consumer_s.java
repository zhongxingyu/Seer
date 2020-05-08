 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
package com.sun.oauth.TokenService.persistence;
 
import com.sun.oauth.TokenService.persistence.AccessToken;
 import java.io.Serializable;
 import java.util.Collection;
 import javax.persistence.Basic;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 
 /**
  *
  * @author Hubert A. Le Van Gong <hubert.levangong at Sun.COM>
  */
 @Entity
 @Table(name = "CONSUMER")
 @NamedQueries({@NamedQuery(name = "Consumer.findAll", query = "SELECT c FROM Consumer c"), @NamedQuery(name = "Consumer.findById", query = "SELECT c FROM Consumer c WHERE c.id = :id"), @NamedQuery(name = "Consumer.findByConsName", query = "SELECT c FROM Consumer c WHERE c.consName = :consName"), @NamedQuery(name = "Consumer.findByConsSvcUri", query = "SELECT c FROM Consumer c WHERE c.consSvcUri = :consSvcUri"), @NamedQuery(name = "Consumer.findByConsIconUri", query = "SELECT c FROM Consumer c WHERE c.consIconUri = :consIconUri"), @NamedQuery(name = "Consumer.findByConsSecret", query = "SELECT c FROM Consumer c WHERE c.consSecret = :consSecret"), @NamedQuery(name = "Consumer.findByConsRsakey", query = "SELECT c FROM Consumer c WHERE c.consRsakey = :consRsakey"), @NamedQuery(name = "Consumer.findByConsKey", query = "SELECT c FROM Consumer c WHERE c.consKey = :consKey")})
 public class Consumer implements Serializable {
     private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Basic(optional = false)
     @Column(name = "ID")
     private Integer id;
     @Column(name = "CONS_NAME")
     private String consName;
     @Basic(optional = false)
     @Column(name = "CONS_SVC_URI")
     private String consSvcUri;
     @Column(name = "CONS_ICON_URI")
     private String consIconUri;
     @Column(name = "CONS_SECRET")
     private String consSecret;
     @Column(name = "CONS_RSAKEY")
     private String consRsakey;
     @Column(name = "CONS_KEY")
     private String consKey;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "consumerId")
     private Collection<RequestToken> reqtokenCollection;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "consumerId")
     private Collection<AccessToken> acctokenCollection;
 
     public Consumer() {
     }
 
     public Consumer(Integer id) {
         this.id = id;
     }
 
     public Consumer(Integer id, String consSvcUri) {
         this.id = id;
         this.consSvcUri = consSvcUri;
     }
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public String getConsName() {
         return consName;
     }
 
     public void setConsName(String consName) {
         this.consName = consName;
     }
 
     public String getConsSvcUri() {
         return consSvcUri;
     }
 
     public void setConsSvcUri(String consSvcUri) {
         this.consSvcUri = consSvcUri;
     }
 
     public String getConsIconUri() {
         return consIconUri;
     }
 
     public void setConsIconUri(String consIconUri) {
         this.consIconUri = consIconUri;
     }
 
     public String getConsSecret() {
         return consSecret;
     }
 
     public void setConsSecret(String consSecret) {
         this.consSecret = consSecret;
     }
 
     public String getConsRsakey() {
         return consRsakey;
     }
 
     public void setConsRsakey(String consRsakey) {
         this.consRsakey = consRsakey;
     }
 
     public String getConsKey() {
         return consKey;
     }
 
     public void setConsKey(String consKey) {
         this.consKey = consKey;
     }
 
     public Collection<RequestToken> getReqtokenCollection() {
         return reqtokenCollection;
     }
 
     public void setReqtokenCollection(Collection<RequestToken> reqtokenCollection) {
         this.reqtokenCollection = reqtokenCollection;
     }
 
     public Collection<AccessToken> getAcctokenCollection() {
         return acctokenCollection;
     }
 
     public void setAcctokenCollection(Collection<AccessToken> acctokenCollection) {
         this.acctokenCollection = acctokenCollection;
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
         if (!(object instanceof Consumer)) {
             return false;
         }
         Consumer other = (Consumer) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "com.sun.oauth.persistence.Consumer[id=" + id + "]";
     }
 
 }
