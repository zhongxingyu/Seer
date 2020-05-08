 package com.github.croesch.controller;
 
 import org.apache.log4j.Logger;
 
 import com.github.croesch.i18n.Text;
 import com.github.croesch.model.api.IParticipantModel;
 import com.github.croesch.types.Participant;
 import com.github.croesch.view.api.IParticipantEditView;
 import com.github.croesch.view.api.IStatusView;
 
 /**
  * Component that is able to fetch the information from the {@link IParticipantEditView} and to store it in the model.
  * 
  * @author croesch
  * @since Date: Jul 1, 2011
  */
 final class ParticipantSaver {
 
   /** logging class */
   private static final Logger LOGGER = Logger.getLogger(ParticipantSaver.class);
 
   /**
    * Hidden constructor.
    * 
    * @author croesch
    * @since Date: Jul 1, 2011
    */
   private ParticipantSaver() {
     // not needed
   }
 
   /**
    * Tries saving the participant currently being edited in the {@link IParticipantEditView}.
    * 
    * @author croesch
    * @since Date: Jul 1, 2011
    * @param model the {@link IParticipantModel} to store the data with
    * @param editView the {@link IParticipantEditView} that has been editing the participant
    * @param statusView the {@link IStatusView} that is responsible for displaying the status
    */
   static void performSave(final IParticipantModel model,
                           final IParticipantEditView editView,
                           final IStatusView statusView) {
 
     try {
       Participant p = null;
       if (editView.getId() <= 0) {
         p = new Participant(editView.getLastName(),
                             editView.getFirstName(),
                             editView.getGender(),
                             editView.getDenomination(),
                             editView.getBirthDate(),
                             editView.getLivingStreet(),
                             editView.getLivingPostCode(),
                             editView.getLivingCity(),
                             editView.getCountyCouncil());
       } else {
        // FIXME: null value returned
         p = model.getParticipant(editView.getId());
         p.setLastName(editView.getLastName());
         p.setForeName(editView.getFirstName());
         p.setGender(editView.getGender());
         p.setDenomination(editView.getDenomination());
         p.setBirthDate(editView.getBirthDate());
         p.setStreet(editView.getLivingStreet());
         p.setPostCode(editView.getLivingPostCode());
         p.setCity(editView.getLivingCity());
         p.setCountyCouncil(editView.getCountyCouncil());
       }
 
       p.setBank(returnValueOrNullIfEmpty(editView.getBank()));
       p.setBankAccountNumber(editView.getBankAccountNumber());
       p.setBankCodeNumber(editView.getBankCodeNumber());
       p.setComment(returnValueOrNullIfEmpty(editView.getComment()));
       p.setDateUpToInSystem(editView.getDateUpToInDataBase());
       p.setFax(returnValueOrNullIfEmpty(editView.getFax()));
       p.setMailAddress(returnValueOrNullIfEmpty(editView.getMailAddress()));
       p.setMobilePhone(returnValueOrNullIfEmpty(editView.getMobilePhone()));
       p.setPhone(returnValueOrNullIfEmpty(editView.getPhone()));
       p.setPhoneOfParents(returnValueOrNullIfEmpty(editView.getPhoneOfParents()));
 
       p.setPossibleAGE(editView.getPossibleAGE());
       p.setPossibleBoard(editView.getPossibleBoard());
       p.setPossibleExtendedBoard(editView.getPossibleExtendedBoard());
       p.setPossibleKitchen(editView.getPossibleKitchen());
       p.setPossibleMAK(editView.getPossibleMAK());
       p.setPossibleMisc(editView.getPossibleMisc());
       p.setPossibleParticipant(editView.getPossibleParticipant());
       p.setPossibleSeminar(editView.getPossibleSeminar());
       p.setPossibleStaff(editView.getPossibleStaff());
       p.setPossibleStaffYouth(editView.getPossibleStaffYouth());
 
       p.setStreetPostal(returnValueOrNullIfEmpty(editView.getPostalStreet()));
       p.setCityPostal(returnValueOrNullIfEmpty(editView.getPostalCity()));
       p.setPostCodePostal(editView.getPostalPostCode());
 
       editView.setParticipant(p);
       model.store(p);
       statusView.showInformation(Text.INFO_PARTICIPANT_SAVED, p.getId());
     } catch (final Exception e) {
       LOGGER.debug(Text.ERROR_EXCEPTION.text(e.getClass().getName()), e);
       statusView.showError(Text.ERROR_PARTICIPANT_NOT_SAVED);
     }
   }
 
   /**
    * Returns <code>null</code>, if the given {@link String} is null or empty.
    * 
    * @since Date: Jul 10, 2011
    * @param s the string to test
    * @return the given string, if it's not <code>null</code> and not empty, <code>null</code> otherwise.
    */
   private static String returnValueOrNullIfEmpty(final String s) {
     if (s == null || s.trim().equals("")) {
       return null;
     }
     return s;
   }
 
 }
