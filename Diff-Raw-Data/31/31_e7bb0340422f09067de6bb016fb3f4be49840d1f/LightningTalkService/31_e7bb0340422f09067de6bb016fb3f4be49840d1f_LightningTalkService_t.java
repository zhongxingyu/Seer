 package com.level42.mixit.services.impl;
 
import java.util.Collections;
 import java.util.List;
 
 import com.level42.mixit.R;
import com.level42.mixit.comparator.LightningTalkComparator;
 import com.level42.mixit.exceptions.CommunicationException;
 import com.level42.mixit.exceptions.FunctionnalException;
 import com.level42.mixit.exceptions.NotFoundException;
 import com.level42.mixit.exceptions.TechnicalException;
 import com.level42.mixit.models.LightningTalk;
 import com.level42.mixit.services.ILightningTalkService;
 
 /**
  * Service de gestion des lightning talks.
  */
 public class LightningTalkService extends TalkService implements
         ILightningTalkService {
 
     /*
      * (non-Javadoc)
      * @see com.level42.mixit.services.ILightningTalkService#getLightningTalks()
      */
     public List<LightningTalk> getLightningTalks() throws FunctionnalException,
             TechnicalException {
         try {
            List<LightningTalk> lightningTalks = getWs().getLightningTalks();
            Collections.sort(lightningTalks, new LightningTalkComparator());
            return lightningTalks;
         } catch (CommunicationException e) {
             throw new TechnicalException(
                     getText(R.string.exception_message_CommunicationException),
                     e);
         }
     }
 
     /*
      * (non-Javadoc)
      * @see com.level42.mixit.services.ILightningTalkService#getLightningTalk(java.lang.Integer)
      */
     public LightningTalk getLightningTalk(Integer id)
             throws FunctionnalException, TechnicalException {
         try {
             LightningTalk lTalk = getWs().getLightningTalk(id);
             this.hydrateTalkInterests(lTalk);
             this.hydrateTalkSpeakers(lTalk);
             return lTalk;
         } catch (CommunicationException e) {
             throw new TechnicalException(
                     getText(R.string.exception_message_CommunicationException),
                     e);
         } catch (NotFoundException e) {
             throw new TechnicalException(
                     getText(R.string.exception_message_NotFoundException), e);
         }
     }
 
 }
