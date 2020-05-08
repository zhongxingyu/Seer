 package com.mpower.domain.customization;
 
 import java.io.Serializable;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.persistence.UniqueConstraint;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.mpower.domain.Site;
 import com.mpower.type.AccessType;
 import com.mpower.type.PageType;
 
 @Entity
 @Table(name = "PAGE_DEFINITION", uniqueConstraints = @UniqueConstraint(columnNames = { "PAGE_TYPE", "SITE_NAME", "ROLE" }))
 public class PageDefinition implements Serializable {
 
     private static final long serialVersionUID = 1L;
 
     @SuppressWarnings("unused")
     @Transient
     private final Log logger = LogFactory.getLog(getClass());
 
     @Id
     @GeneratedValue
     @Column(name = "PAGE_DEFINITION_ID")
     private Long id;
 
     @Column(name = "PAGE_TYPE")
     @Enumerated(EnumType.STRING)
     private PageType pageType;
 
     @ManyToOne
     @JoinColumn(name = "SITE_NAME")
     private Site site;
 
    @Column(name = "ROLE", nullable = false)
     private String role;
 
     @Column(name = "ACCESS_TYPE")
     @Enumerated(EnumType.STRING)
     private AccessType accessType;
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public Site getSite() {
         return site;
     }
 
     public void setSite(Site site) {
         this.site = site;
     }
 
     public PageType getPageType() {
         return pageType;
     }
 
     public void setPageType(PageType pageType) {
         this.pageType = pageType;
     }
 
     public String getRole() {
         return role;
     }
 
     public void setRole(String role) {
         this.role = role;
     }
 
     public AccessType getAccessType() {
         return accessType;
     }
 
     public void setAccessType(AccessType accessType) {
         this.accessType = accessType;
     }
 }
