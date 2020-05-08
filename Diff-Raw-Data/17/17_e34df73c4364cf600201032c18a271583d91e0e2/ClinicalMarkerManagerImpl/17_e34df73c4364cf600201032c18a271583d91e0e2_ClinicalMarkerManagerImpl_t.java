 /**
  * @pandyas
  * 
 * $Id: ClinicalMarkerManagerImpl.java,v 1.10 2007-10-31 19:03:00 pandyas Exp $
  * 
  * $Log: not supported by cvs2svn $
 * Revision 1.9  2007/09/12 19:36:03  pandyas
 * modified debug statements for build to stage tier
 *
  * Revision 1.8  2007/08/14 17:07:42  pandyas
  * Bug #8404:  Clincal Marker - the search page shows only "Other" instead of the value that was entered which was in this
  * case "other clinical marker"
  *
  * Revision 1.7  2006/05/25 19:12:20  schroedn
  * Fixed check for 'Other'
  *
  * Revision 1.6  2006/04/20 19:19:25  pandyas
  * Added 'Other' to field and fixed save for other clinical marker
  *
  * Revision 1.5  2006/01/18 14:24:23  georgeda
  * TT# 376 - Updated to use new Java 1.5 features
  *
  * Revision 1.4  2005/11/09 00:17:16  georgeda
  * Fixed delete w/ constraints
  *
  * Revision 1.3  2005/11/07 19:15:17  pandyas
  * modified for clinical marker screen
  *
  *
  * 
  */
 
 package gov.nih.nci.camod.service.impl;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.ClinicalMarker;
 import gov.nih.nci.camod.domain.Histopathology;
 import gov.nih.nci.camod.service.ClinicalMarkerManager;
 import gov.nih.nci.camod.webapp.form.ClinicalMarkerData;
 
 /**
  * Manager provides get method
  */
 public class ClinicalMarkerManagerImpl extends BaseManager implements ClinicalMarkerManager
 {
     public ClinicalMarker get(String id) throws Exception
     {
         log.trace("In ClinicalMarkerManagerImpl.get");
         return (ClinicalMarker) super.get(id, ClinicalMarker.class);
     }
 
     public void save(ClinicalMarker clinicalMarker) throws Exception
     {
         log.trace("In ClinicalMarkerManagerImpl.save");
         super.save(clinicalMarker);
     }
 
     public void remove(String id,
                        Histopathology inHistopathology) throws Exception
     {
         log.trace("In ClinicalMarkerManagerImpl.remove");
 
         ClinicalMarker theClinicalMarker = get(id);
 
         inHistopathology.getClinicalMarkerCollection().remove(theClinicalMarker);
         super.save(inHistopathology);
     }
 
     public void create(ClinicalMarkerData inClinicalMarkerData,
                        Histopathology inHistopathology) throws Exception
     {
         log.debug("Entering HistopathologyManagerImpl.createClinicalMarker");
 
         ClinicalMarker theClinicalMarker = new ClinicalMarker();
         populateClinicalMarker(inClinicalMarkerData, theClinicalMarker);
 
         inHistopathology.addClinicalMarker(theClinicalMarker);
 
         log.debug("Exiting HistopathologyManagerImpl.createClinicalMarker");
     }
 
     public void update(ClinicalMarkerData inClinicalMarkerData,
                        ClinicalMarker inClinicalMarker) throws Exception
     {
         log.debug("Entering ClinicalMarkerManagerImpl.update");
         log.debug("Updating ClinicalMarkerData: " + inClinicalMarker.getId());
 
         // Populate w/ the new values and save
         populateClinicalMarker(inClinicalMarkerData, inClinicalMarker);
         save(inClinicalMarker);
 
         log.debug("Exiting ClinicalMarkerManagerImpl.update");
     }
 
     private void populateClinicalMarker(ClinicalMarkerData inClinicalMarkerData,
                                         ClinicalMarker inClinicalMarker)
     {
         log.debug("<ClinicalMarkerManagerImpl> Entering populateClinicalMarker");
 
         if (inClinicalMarkerData.getOtherName() != null )
         {
             inClinicalMarker.setName(null);
            inClinicalMarker.setNameAlternEntry(inClinicalMarkerData.getOtherName());            
         }
         else
         {
             inClinicalMarker.setName(inClinicalMarkerData.getName());
            inClinicalMarker.setNameAlternEntry( null );
         }
 
         if (inClinicalMarkerData.getValue() != null && inClinicalMarkerData.getValue().length() > 0)
         {
             inClinicalMarker.setValue(inClinicalMarkerData.getValue());
         }
        if (inClinicalMarkerData.getComments() != null && inClinicalMarkerData.getComments().length() > 0)
        {
            inClinicalMarker.setComments(inClinicalMarkerData.getComments());
        }        
         log.debug("<ClinicalMarkerManagerImpl> Exiting populateClinicalMarker");
     }
 
 }
