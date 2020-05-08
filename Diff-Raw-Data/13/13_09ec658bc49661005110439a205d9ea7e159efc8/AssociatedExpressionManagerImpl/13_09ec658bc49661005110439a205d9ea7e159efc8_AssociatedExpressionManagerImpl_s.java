 /**
  * 
 * $Id: AssociatedExpressionManagerImpl.java,v 1.9 2007-09-12 19:36:03 pandyas Exp $
  *
  * $Log: not supported by cvs2svn $
  * Revision 1.8  2007/06/20 18:05:09  pandyas
  * Fixed save and edit for organ
  *
  * Revision 1.7  2007/06/13 20:20:09  pandyas
  * Modified code for EVS trees after formal testing
  *
  * Revision 1.6  2007/04/30 20:09:43  pandyas
  * Implemented species specific vocabulary trees from EVSTree
  *
  * Revision 1.5  2006/04/17 19:11:05  pandyas
  * caMod 2.1 OM changes
  *
  * 
  */
 
 package gov.nih.nci.camod.service.impl;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.EngineeredGene;
 import gov.nih.nci.camod.domain.ExpressionFeature;
 import gov.nih.nci.camod.domain.ExpressionLevelDesc;
 import gov.nih.nci.camod.domain.Organ;
 import gov.nih.nci.camod.service.AssociatedExpressionManager;
 import gov.nih.nci.camod.webapp.form.AssociatedExpressionData;
 
 import java.util.List;
 
 public class AssociatedExpressionManagerImpl extends BaseManager implements AssociatedExpressionManager
 {
     public List getAll() throws Exception
     {
         log.trace("In AssociatedExpressionManagerImpl.getAll");
         return super.getAll(ExpressionFeature.class);
     }
 
     public ExpressionFeature get(String id) throws Exception
     {
         log.trace("In AssociatedExpressionManagerImpl.get");
         return (ExpressionFeature) super.get(id, ExpressionFeature.class);
     }
 
     public void save(ExpressionFeature expressionFeature) throws Exception
     {
         log.trace("In AssociatedExpressionManagerImpl.save");
         super.save(expressionFeature);
     }
 
     public void remove(String id,
                        EngineeredGene inEngineeredGene) throws Exception
     {
         log.trace("In AssociatedExpressionManagerImpl.remove");
         inEngineeredGene.getExpressionFeatureCollection().remove(get(id));
         super.save(inEngineeredGene);
     }
 
     public ExpressionFeature create(AssociatedExpressionData inAssociatedExpressionData) throws Exception
     {
         ExpressionFeature inExpressionFeature = new ExpressionFeature();
         populate(inAssociatedExpressionData, inExpressionFeature);
         return inExpressionFeature;
     }
 
     public void update(AssociatedExpressionData inAssociatedExpressionData,
                        ExpressionFeature inExpressionFeature) throws Exception
     {
         populate(inAssociatedExpressionData, inExpressionFeature);
         save(inExpressionFeature);
     }
 
     
     
     private void populate(AssociatedExpressionData inAssociatedExpressionData,
                           ExpressionFeature inExpressionFeature) throws Exception
     {
         // Using trees loop 
         // Update loop handeled separately for conceptCode = 00000
         if (inAssociatedExpressionData.getOrganTissueCode().equals(Constants.Dropdowns.CONCEPTCODEZEROS)){
             log.debug("Organ update loop for text: " + inAssociatedExpressionData.getOrgan()); 
             inExpressionFeature.setOrgan(new Organ());
             inExpressionFeature.getOrgan().setName(inAssociatedExpressionData.getOrgan());   
             inExpressionFeature.getOrgan().setConceptCode(
                     Constants.Dropdowns.CONCEPTCODEZEROS);            
         } else {            
             // Using trees loop, new save loop and update loop
             if (inAssociatedExpressionData.getOrganTissueCode() != null && inAssociatedExpressionData.getOrganTissueCode().length() > 0) {
                 log.debug("OrganTissueCode: " + inAssociatedExpressionData.getOrganTissueCode());
                 log.debug("OrganTissueName: " + inAssociatedExpressionData.getOrganTissueName()); 
                 
                 log.debug("OrganTissueCode() != null - getOrCreate method used");
                 // when using tree, organTissueName populates the organ name entry
                 Organ theNewOrgan = OrganManagerSingleton.instance().getOrCreate(
                         inAssociatedExpressionData.getOrganTissueCode(),
                         inAssociatedExpressionData.getOrganTissueName());
                 
                 log.debug("theNewOrgan: " + theNewOrgan);
                 inExpressionFeature.setOrgan(theNewOrgan); 
             } else {
                 // text entry loop = new save
                 log.debug("Organ (text): " + inAssociatedExpressionData.getOrgan()); 
                 inExpressionFeature.setOrgan(new Organ());
                 inExpressionFeature.getOrgan().setName(inAssociatedExpressionData.getOrgan());                
                 inExpressionFeature.getOrgan().setConceptCode(
                         Constants.Dropdowns.CONCEPTCODEZEROS);            
            }           
             
         }
 
         ExpressionLevelDesc expLevelDesc = ExpressionLevelDescManagerSingleton.instance().getByName(
                                                                                                     inAssociatedExpressionData.getExpressionLevel());
         inExpressionFeature.setExpressionLevelDesc(expLevelDesc);
     }
 }
