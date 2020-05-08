 package gov.nih.nci.ncicb.cadsr.loader.ext;
 import gov.nih.nci.ncicb.cadsr.domain.DomainObjectFactory;
 import gov.nih.nci.ncicb.cadsr.loader.util.PropertyAccessor;
 import java.util.*;
 
 import gov.nih.nci.ncicb.cadsr.evs.*;
 
 /**
  * Transforms cadsr public API to private API and vice versa
  * <br>
  *
  * THIS IS NOT A COMPLETE TRANSFORMER. Currently only meant to support UML Loader / SIW.
  * @author <a href="mailto:chris.ludet@oracle.com">Christophe Ludet</a>
  */
 public class CadsrTransformer {
 
   /**
    * Transforms a DE from public API to private API
    */
   public static gov.nih.nci.ncicb.cadsr.domain.DataElement dePublicToPrivate(gov.nih.nci.cadsr.domain.DataElement inDE) {
 
     gov.nih.nci.ncicb.cadsr.domain.DataElement outDE = DomainObjectFactory.newDataElement();    
 
     acPublicToPrivate(outDE, inDE);
     outDE.setDataElementConcept(decPublicToPrivate(inDE.getDataElementConcept()));
     outDE.setValueDomain(vdPublicToPrivate(inDE.getValueDomain()));
 
 
     return outDE;
 
   }
 
   /**
    * Transforms a DEC from public API to private API
    */
   public static gov.nih.nci.ncicb.cadsr.domain.DataElementConcept decPublicToPrivate(gov.nih.nci.cadsr.domain.DataElementConcept inDEC) {
 
     gov.nih.nci.ncicb.cadsr.domain.DataElementConcept outDEC = DomainObjectFactory.newDataElementConcept();    
 
     acPublicToPrivate(outDEC, inDEC);
     
    outDEC.setObjectClass(ocPublicToPrivate(inDEC.getObjectClass()));
    outDEC.setProperty(propPublicToPrivate(inDEC.getProperty()));
 
    outDEC.setConceptualDomain(cdPublicToPrivate(inDEC.getConceptualDomain()));
 
     return outDEC;
 
   }
 
 
   /**
    * Transforms an OC from public API to private API
    */
   public static gov.nih.nci.ncicb.cadsr.domain.ObjectClass ocPublicToPrivate(gov.nih.nci.cadsr.domain.ObjectClass inOC) {
 
     gov.nih.nci.ncicb.cadsr.domain.ObjectClass outOC = DomainObjectFactory.newObjectClass();    
     
     acPublicToPrivate(outOC, inOC);
 
     return outOC;
 
   }
 
 
   /**
    * Transforms a Prop from public API to private API
    */
   public static gov.nih.nci.ncicb.cadsr.domain.Property propPublicToPrivate(gov.nih.nci.cadsr.domain.Property inProp) {
 
     gov.nih.nci.ncicb.cadsr.domain.Property outProp = DomainObjectFactory.newProperty();    
 
     acPublicToPrivate(outProp, inProp);
 
     return outProp;
 
   }
   
   /**
    * Transforms a Value Domain from public API to private API
    */
   public static gov.nih.nci.ncicb.cadsr.domain.ValueDomain vdPublicToPrivate(gov.nih.nci.cadsr.domain.ValueDomain inVD) {
 
     gov.nih.nci.ncicb.cadsr.domain.ValueDomain outVD = DomainObjectFactory.newValueDomain();    
 
     acPublicToPrivate(outVD, inVD);
 
     return outVD;
 
   }
 
   public static gov.nih.nci.ncicb.cadsr.domain.ConceptualDomain cdPublicToPrivate(gov.nih.nci.cadsr.domain.ConceptualDomain inCD) {
 
     gov.nih.nci.ncicb.cadsr.domain.ConceptualDomain outCD = DomainObjectFactory.newConceptualDomain();    
 
     acPublicToPrivate(outCD, inCD);
 
     return outCD;
   }
 
   public static List<gov.nih.nci.ncicb.cadsr.domain.ConceptualDomain> cdListPublicToPrivate(List<gov.nih.nci.cadsr.domain.ConceptualDomain> inCDs) {
 
     List<gov.nih.nci.ncicb.cadsr.domain.ConceptualDomain> result = new ArrayList();
 
     for(gov.nih.nci.cadsr.domain.ConceptualDomain inCD : inCDs) {
       result.add(cdPublicToPrivate(inCD));
     }
     return result;
   }
 
   
 
   /**
    * Copies values from inAc to outAc
    */
   private static void acPublicToPrivate(gov.nih.nci.ncicb.cadsr.domain.AdminComponent outAc, gov.nih.nci.cadsr.domain.AdministeredComponent inAc) {
 
     gov.nih.nci.ncicb.cadsr.domain.Lifecycle lc = DomainObjectFactory.newLifecycle();
     gov.nih.nci.ncicb.cadsr.domain.Audit audit = DomainObjectFactory.newAudit();
     outAc.setLifecycle(lc);
     outAc.setAudit(audit);
     
     outAc.setId(inAc.getId());
     outAc.setPreferredName(inAc.getPreferredName());
     outAc.setPreferredDefinition(inAc.getPreferredDefinition());
     outAc.setLongName(inAc.getLongName());
     outAc.setVersion(inAc.getVersion());
     outAc.setWorkflowStatus(inAc.getWorkflowStatusName());
     outAc.setLatestVersionIndicator(inAc.getLatestVersionIndicator());
     outAc.getLifecycle().setBeginDate(inAc.getBeginDate());
     outAc.getLifecycle().setEndDate(inAc.getEndDate());
     outAc.setDeletedIndicator(inAc.getDeletedIndicator());
     outAc.setChangeNote(inAc.getChangeNote());
     outAc.setOrigin(inAc.getOrigin());
     outAc.getAudit().setCreationDate(inAc.getDateCreated());
     outAc.getAudit().setCreatedBy(inAc.getCreatedBy());
     outAc.getAudit().setModificationDate(inAc.getDateModified());
     outAc.getAudit().setModifiedBy(inAc.getModifiedBy());
 
     if(inAc.getPublicID() != null)
       outAc.setPublicId(inAc.getPublicID().toString());
     
     outAc.setContext(contextPublicToPrivate(inAc.getContext()));
     
   }
 
   /**
    * Transforms a Context from public API to private API
    */
   public static gov.nih.nci.ncicb.cadsr.domain.Context contextPublicToPrivate(gov.nih.nci.cadsr.domain.Context inContext) {
 
     gov.nih.nci.ncicb.cadsr.domain.Context outContext = DomainObjectFactory.newContext();    
     
     outContext.setName(inContext.getName());
     outContext.setId(inContext.getId());
 
     return outContext;
 
   }
 
   /**
    * Transforms a DE List from public API to private API
    */
   public static Collection<gov.nih.nci.ncicb.cadsr.domain.DataElement> deListPublicToPrivate(Collection<gov.nih.nci.cadsr.domain.DataElement> inDEs) {
 
     List<gov.nih.nci.ncicb.cadsr.domain.DataElement> outDEs = 
       new ArrayList<gov.nih.nci.ncicb.cadsr.domain.DataElement>();
 
     for(gov.nih.nci.cadsr.domain.DataElement privateDe : inDEs) {
       outDEs.add(dePublicToPrivate(privateDe));
     }
     return outDEs;
 
   }
 
 
   public static gov.nih.nci.ncicb.cadsr.domain.ClassificationScheme csPublicToPrivate(gov.nih.nci.cadsr.domain.ClassificationScheme inCs) {
     
     gov.nih.nci.ncicb.cadsr.domain.ClassificationScheme outCs = DomainObjectFactory.newClassificationScheme();
 
     acPublicToPrivate(outCs, inCs);
 
 
     try {
       for(Iterator it = inCs.getClassSchemeClassSchemeItemCollection().iterator(); it.hasNext(); ) {
         gov.nih.nci.cadsr.domain.ClassSchemeClassSchemeItem csCsi = (gov.nih.nci.cadsr.domain.ClassSchemeClassSchemeItem)it.next();
         outCs.addCsCsi(csCsiPublicToPrivate(csCsi));
 
       }
     } catch (org.hibernate.LazyInitializationException e){
       
     } // end of try-catch
 
     return outCs;
 
   }
 
   /**
    * Transforms a CS List from public API to private API
    */
   public static Collection<gov.nih.nci.ncicb.cadsr.domain.ClassificationScheme> csListPublicToPrivate(Collection<gov.nih.nci.cadsr.domain.ClassificationScheme> inCSs) {
 
     List<gov.nih.nci.ncicb.cadsr.domain.ClassificationScheme> outCSs = 
       new ArrayList<gov.nih.nci.ncicb.cadsr.domain.ClassificationScheme>();
 
     for(gov.nih.nci.cadsr.domain.ClassificationScheme privateCs : inCSs) {
       outCSs.add(csPublicToPrivate(privateCs));
     }
     return outCSs;
 
   }
 
   /**
    * Transforms an OC List from public API to private API
    */
   public static Collection<gov.nih.nci.ncicb.cadsr.domain.ObjectClass> ocListPublicToPrivate(Collection<gov.nih.nci.cadsr.domain.ObjectClass> inOCs) {
 
     List<gov.nih.nci.ncicb.cadsr.domain.ObjectClass> outOCs = 
       new ArrayList<gov.nih.nci.ncicb.cadsr.domain.ObjectClass>();
 
     for(gov.nih.nci.cadsr.domain.ObjectClass privateOC : inOCs) {
       outOCs.add(ocPublicToPrivate(privateOC));
     }
     return outOCs;
 
   }
 
   /**
    * Transforms an Property List from public API to private API
    */
   public static Collection<gov.nih.nci.ncicb.cadsr.domain.Property> propListPublicToPrivate(Collection<gov.nih.nci.cadsr.domain.Property> inProps) {
 
     List<gov.nih.nci.ncicb.cadsr.domain.Property> outProps = 
       new ArrayList<gov.nih.nci.ncicb.cadsr.domain.Property>();
 
     for(gov.nih.nci.cadsr.domain.Property privateProp : inProps) {
       outProps.add(propPublicToPrivate(privateProp));
     }
     return outProps;
 
   }
   
   /**
    * Transforms a Value Domain List from public API to private API
    */
   public static Collection<gov.nih.nci.ncicb.cadsr.domain.ValueDomain> vdListPublicToPrivate(Collection<gov.nih.nci.cadsr.domain.ValueDomain> inVDs) {
 
     List<gov.nih.nci.ncicb.cadsr.domain.ValueDomain> outVDs = 
       new ArrayList<gov.nih.nci.ncicb.cadsr.domain.ValueDomain>();
 
     for(gov.nih.nci.cadsr.domain.ValueDomain privateVD : inVDs) {
       outVDs.add(vdPublicToPrivate(privateVD));
     }
     return outVDs;
 
   }
   
     
   private static gov.nih.nci.ncicb.cadsr.domain.ClassSchemeClassSchemeItem csCsiPublicToPrivate(gov.nih.nci.cadsr.domain.ClassSchemeClassSchemeItem inCsCsi) {
 
     gov.nih.nci.ncicb.cadsr.domain.ClassSchemeClassSchemeItem outCsCsi = DomainObjectFactory.newClassSchemeClassSchemeItem();
 
     outCsCsi.setId(inCsCsi.getId());
 
     outCsCsi.setCsi(csiPublicToPrivate(inCsCsi.getClassificationSchemeItem()));
 
     return outCsCsi;
 
   }
 
   
   private static gov.nih.nci.ncicb.cadsr.domain.ClassificationSchemeItem csiPublicToPrivate(gov.nih.nci.cadsr.domain.ClassificationSchemeItem inCsi) {
     gov.nih.nci.ncicb.cadsr.domain.ClassificationSchemeItem outCsi = DomainObjectFactory.newClassificationSchemeItem();
     outCsi.setName(inCsi.getName());
     outCsi.setType(inCsi.getType());
 
     return outCsi;
 
   }
 
 
 }
