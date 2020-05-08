 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mne.advertmanager.model;
 
 import java.io.Serializable;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import javax.persistence.*;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 import javax.xml.bind.annotation.XmlRootElement;
 
 /**
  *
  * @author Nina Eidelshtein and Misha Lebedev
  */
 @Entity
 @Table(name = "access_log")
 @XmlRootElement
 @NamedQueries({
     @NamedQuery(name = "AccessLog.findAll", query = "SELECT a FROM AccessLog a order by a.accessTime"),
     @NamedQuery(name = "AccessLog.countAccessLog", query = "SELECT count (*) FROM AccessLog a "),
     @NamedQuery(name = "AccessLog.findById", query = "SELECT a FROM AccessLog a WHERE a.id = ?"),
     @NamedQuery(name = "AccessLog.findByAccessTime", query = "SELECT a FROM AccessLog a WHERE a.accessTime = ?"),
     @NamedQuery(name = "AccessLog.findByIpAddress", query = "SELECT a FROM AccessLog a WHERE a.ipAddress = ?"),
    @NamedQuery(name = "AccessLog.findByLocation", query = "SELECT a FROM AccessLog a WHERE a.location = ?"),
     @NamedQuery(name = "AccessLog.findByAffProgramId", query = "SELECT a FROM AccessLog a left join fetch a.sourceDomain WHERE a.affProgram.id = ? "),
     @NamedQuery(name = "AccessLog.countAffProgramAccessLog", query = "SELECT count (*) FROM AccessLog a WHERE a.affProgram.id = ?"),
     @NamedQuery(name = "AccessLog.countAffProgramAccessByDate", query = "SELECT count (*) FROM AccessLog a WHERE a.affProgram.id = ? and accessTime > ?"),
     @NamedQuery(name = "AccessLog.findByUrl", query = "SELECT a FROM AccessLog a WHERE a.targetURL = ?")
 })
 
  public class AccessLog implements Serializable {
     public static final String ACCESSLOG_FINDGEODATABYIP_QUERY = "SELECT DISTINCT cc as countryCode,cn as countryName FROM geoip.ip " +
                                                                  " natural join geoip.cc where ? BETWEEN start and end";
     private static final long serialVersionUID = 1L;
     private static DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Basic(optional = false)
     @Column(name = "id")
     private Integer id;
     
     @Basic(optional = false)
     @NotNull
     @Column(name = "access_time")
     @Temporal(TemporalType.TIMESTAMP)
     private Date accessTime;
     
     @Size(max = 256)
     @Column(name = "ip_address")
     private String ipAddress;
     
     @Size(max = 256)
     @Column(name = "country_name")
     private String countryName;
     
     @Size(max = 2)
     @Column(name = "cc")
     private String countryCode;
     
     @Size(max = 256)
     @Column(name = "target_url")
     private String targetURL;
     
    
     @Size(max = 384)
     @Column(name = "referer_url")
     private String refererURL;
 
     @Size(max = 256)
     @Column(name = "query")
     private String query;
 
     
     @JoinColumn(name = "source_domain_id", referencedColumnName = "id")
     @ManyToOne(optional = false)
     private AccessSource sourceDomain;
     
     @JoinColumn(name = "affprogram_id", referencedColumnName = "id")
     @ManyToOne(optional = false)
     private AffProgram affProgram;
 
     public AccessLog() {
     }
 
     public AccessLog(Integer id) {
         this.id = id;
     }
 
     public AccessLog(Integer id, Date accessTime) {
         this.id = id;
         this.accessTime = accessTime;
     }
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public Date getAccessTime() {
         return accessTime;
     }
     
     public String getTimeAsString() {
         return df.format(accessTime);
     }
 
     public void setAccessTime(Date accessTime) {
         this.accessTime = accessTime;
     }
 
     public String getIpAddress() {
         return ipAddress;
     }
 
     public void setIpAddress(String ipAddress) {
         this.ipAddress = ipAddress;
     }
 
     public String getCountryName() {
         return countryName;
     }
 
     public void setCountryName(String cn) {
         this.countryName = cn;
     }
 
     public String getTargetURL() {
         return targetURL;
     }
 
     public void setTargetURL(String url) {
         this.targetURL = url;
     }
 
     public String getQuery() {
         return query;
     }
 
     public void setQuery(String query) {
         this.query = query;
     }
 
     public String getRefererURL() {
         return refererURL;
     }
 
     public void setRefererURL(String refererURL) {
         this.refererURL = refererURL;
     }
 
     public String getCountryCode() {
         return countryCode;
     }
 
     public void setCountryCode(String countryCode) {
         this.countryCode = countryCode;
     }
 
     public AccessSource getSourceDomain() {
         return sourceDomain;
     }
 
     public void setSourceDomainId(AccessSource sourceDomain) {
         this.sourceDomain = sourceDomain;
     }
 
     public AffProgram getAffProgram() {
         return affProgram;
     }
 
     public void setAffProgram(AffProgram affProgram) {
         this.affProgram = affProgram;
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
         if (!(object instanceof AccessLog)) {
             return false;
         }
         AccessLog other = (AccessLog) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "com.mne.advertmanager.model.AccessLog[ id=" + id + " ]";
     }
     
 }
