 package com.github.croesch.partimana.controller;
 
 import org.apache.log4j.Logger;
 
 import com.github.croesch.annotate.MayBeNull;
 import com.github.croesch.partimana.i18n.Text;
 import com.github.croesch.partimana.model.api.ICampModel;
 import com.github.croesch.partimana.types.Camp;
 import com.github.croesch.partimana.types.CampParticipant;
 import com.github.croesch.partimana.view.api.ICampEditView;
 import com.github.croesch.partimana.view.api.IStatusView;
 
 /**
  * Component that is able to fetch the information from the {@link ICampEditView} and to store it in the model.
  * 
  * @author croesch
  * @since Date: Sep 23, 2012
  */
 final class CampSaver {
 
   /** logging class */
   private static final Logger LOGGER = Logger.getLogger(CampSaver.class);
 
   /**
    * Hidden constructor.
    * 
    * @author croesch
    * @since Date: Sep 23, 2012
    */
   private CampSaver() {
     // not needed
   }
 
   /**
    * Tries saving the camp currently being edited in the {@link ICampEditView}.
    * 
    * @author croesch
    * @since Date: Sep 23, 2012
    * @param model the {@link ICampModel} to store the data with
    * @param editView the {@link ICampEditView} that has been editing the camp
    * @param statusView the {@link IStatusView} that is responsible for displaying the status
    */
   static void performSave(final ICampModel model, final ICampEditView editView, final IStatusView statusView) {
 
     try {
       Camp c = null;
       if (editView.getId() <= 0) {
         c = new Camp(editView.getNameOfCamp(),
                      editView.getFrom(),
                      editView.getUntil(),
                      editView.getLocationOfCamp(),
                      editView.getRatePerParticipant());
       } else {
         // FIXME: null value returned
         c = model.getCamp(editView.getId());
         c.setName(editView.getNameOfCamp());
         c.setFromDate(editView.getFrom());
         c.setUntilDate(editView.getUntil());
         c.setLocation(editView.getLocationOfCamp());
         c.setRatePerParticipant(editView.getRatePerParticipant());
       }
 
      c.setRatePerDayChildren(editView.getRatePerDay());
       c.removeAllParticipants();
       for (final CampParticipant cp : editView.getCampParticipants()) {
         c.addParticipant(cp);
       }
 
       editView.setCamp(c);
       model.store(c);
       statusView.showInformation(Text.INFO_CAMP_SAVED, c.getId());
     } catch (final Exception e) {
       LOGGER.debug(Text.ERROR_EXCEPTION.text(e.getClass().getName()), e);
       statusView.showError(Text.ERROR_CAMP_NOT_SAVED);
     }
   }
 
   /**
    * Returns <code>null</code>, if the given {@link String} is null or empty.
    * 
    * @since Date: Jul 10, 2011
    * @param s the string to test
    * @return the given string, if it's not <code>null</code> and not empty, <code>null</code> otherwise.
    */
   @MayBeNull
   private static String returnValueOrNullIfEmpty(final String s) {
     if (s == null || s.trim().equals("")) {
       return null;
     }
     return s;
   }
 }
