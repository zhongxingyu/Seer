 package org.ffplanner.bean;
 
 import org.ffplanner.entity.FestivalEdition;
 import org.ffplanner.entity.FestivalEdition_;
 
 import javax.ejb.LocalBean;
 import javax.ejb.Stateless;
 import javax.persistence.metamodel.SingularAttribute;
 import java.io.Serializable;
 
 /**
  * @author Bogdan Dumitriu
  */
 @Stateless
 @LocalBean
 public class FestivalEditionBean extends BasicEntityBean<FestivalEdition> implements Serializable {
 
    private static final long serialVersionUID = -9047566575453952764L;
 
     @Override
     protected Class<FestivalEdition> getEntityClass() {
         return FestivalEdition.class;
     }
 
     @Override
     protected SingularAttribute<FestivalEdition, Long> getIdAttribute() {
         return FestivalEdition_.id;
     }
 
     public FestivalEdition find(Long festivalEditionId, boolean loadSections) {
         final FestivalEdition festivalEdition = find(festivalEditionId);
         if (loadSections) {
             festivalEdition.loadSections();
         }
         return festivalEdition;
     }
 }
