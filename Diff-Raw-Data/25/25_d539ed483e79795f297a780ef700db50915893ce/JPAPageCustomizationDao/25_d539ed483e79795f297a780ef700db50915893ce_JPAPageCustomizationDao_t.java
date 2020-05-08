 package com.mpower.dao.customization;
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.stereotype.Repository;
 
 import com.mpower.domain.customization.PageDefinition;
 import com.mpower.domain.customization.SectionDefinition;
 import com.mpower.domain.customization.SectionField;
 import com.mpower.type.PageType;
 
 @Repository("pageCustomizationDao")
 public class JPAPageCustomizationDao implements PageCustomizationDAO {
 
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
 
 
     @PersistenceContext
     private EntityManager em;
 
     @SuppressWarnings("unchecked")
     public List<SectionDefinition> readSectionDefinitions(String siteName, PageType pageType, List<String> roles) {
         Query query = em.createNamedQuery("READ_SECTION_DEFINITIONS");
         query.setParameter("siteName", siteName);
         query.setParameter("pageType", pageType);
         query.setParameter("roles", roles);
         return query.getResultList();
     }
 
     @SuppressWarnings("unchecked")
     public List<SectionField> readOutOfBoxSectionFields(String sectionName) {
         Query query = em.createNamedQuery("READ_OUT_OF_BOX_SECTION_FIELDS");
         query.setParameter("sectionName", sectionName);
         return query.getResultList();
     }
 
     @SuppressWarnings("unchecked")
     public List<SectionField> readCustomizedSectionFields(String siteName, Long sectionDefinitionId) {
         Query query = em.createNamedQuery("READ_CUSTOMIZED_SECTION_FIELDS");
         query.setParameter("siteName", siteName);
         query.setParameter("sectionDefinitionId", sectionDefinitionId);
         return query.getResultList();
     }
 
     @SuppressWarnings("unchecked")
     @Override
    public List<PageDefinition> readPageDefinitions(String siteName) {
         Query query = em.createNamedQuery("READ_PAGE_DEFINITIONS");
         query.setParameter("siteName", siteName);
         return query.getResultList();
     }
 }
