 /*
  * Copyright 2012 INRIA
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.mymed.tests.unit.manager;
 
 import static org.junit.Assert.fail;
 
 import java.io.File;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.BeforeClass;
 
 import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.controller.core.manager.authentication.AuthenticationManager;
 import com.mymed.controller.core.manager.geolocation.GeoLocationManager;
 import com.mymed.controller.core.manager.interaction.old.InteractionManager;
 import com.mymed.controller.core.manager.profile.ProfileManager;
 import com.mymed.controller.core.manager.reputation.old.ReputationManager;
 import com.mymed.controller.core.manager.session.SessionManager;
 import com.mymed.controller.core.manager.storage.StorageManager;
 import com.mymed.model.core.configuration.WrapperConfiguration;
 import com.mymed.model.data.interaction.MInteractionBean;
 import com.mymed.model.data.reputation.old.MReputationBean;
 import com.mymed.model.data.session.MAuthenticationBean;
 import com.mymed.model.data.session.MSessionBean;
 import com.mymed.model.data.user.MUserBean;
 import com.mymed.utils.MConverter;
 
 /**
  * Simple class used to initialize all the values needed by the tests.
  * <p>
  * It is just needed to extend this class to have all the necessary values.
  * 
  * @author Milo Casagrande
  */
 public class GeneralTest extends TestValues {
 
   protected static MSessionBean sessionBean;
   protected static MUserBean userBean;
   protected static MInteractionBean interactionBean;
   protected static MAuthenticationBean authenticationBean;
   protected static MReputationBean reputationBean;
 
   protected static byte[] name;
   protected static byte[] firstName;
   protected static byte[] lastName;
   protected static byte[] birthDate;
 
   protected SessionManager sessionManager;
   protected StorageManager storageManager;
   protected ProfileManager profileManager;
   protected ReputationManager reputationManager;
   protected InteractionManager interactionManager;
   protected AuthenticationManager authenticationManager;
   protected GeoLocationManager geolocationManager;
 
   @BeforeClass
   public static void setUpOnce() {
     CAL_INSTANCE.set(1971, 1, 1);
    final String date = "1971-01-01";
     birthDate = MConverter.longToByteBuffer(CAL_INSTANCE.getTimeInMillis()).array();
 
     sessionBean = new MSessionBean();
     sessionBean.setId(SESSION_ID);
     sessionBean.setUser(USER_ID);
     sessionBean.setIp(IP);
     sessionBean.setCurrentApplications("");
     sessionBean.setAccessToken(ACCESS_TOKEN);
     sessionBean.setP2P(false);
 
     userBean = new MUserBean();
     userBean.setId(USER_ID);
     userBean.setBirthday(date);
     userBean.setSocialNetworkID(NAME);
     userBean.setBuddyList(BUDDY_LST_ID);
     userBean.setEmail(EMAIL);
     userBean.setFirstName(FIRST_NAME);
     userBean.setGender(GENDER);
     userBean.setHometown(HOMETOWN);
     userBean.setLastName(LAST_NAME);
     userBean.setLink(LINK);
     userBean.setName(LOGIN);
     userBean.setSession(SESSION_ID);
     userBean.setInteractionList(INTERACTION_LST_ID);
     userBean.setLastConnection(CAL_INSTANCE.getTimeInMillis());
     userBean.setReputation(REPUTATION_ID);
     userBean.setSubscribtionList(SUBSCRIPTION_LST_ID);
 
     try {
       name = MConverter.stringToByteBuffer(NAME).array();
       firstName = MConverter.stringToByteBuffer(FIRST_NAME).array();
       lastName = MConverter.stringToByteBuffer(LAST_NAME).array();
     } catch (final InternalBackEndException ex) {
       fail(ex.getMessage());
     }
 
     interactionBean = new MInteractionBean();
 
     CAL_INSTANCE.set(2011, 1, 1);
     interactionBean.setStart(CAL_INSTANCE.getTimeInMillis());
 
     CAL_INSTANCE.set(2011, 1, 3);
     interactionBean.setEnd(CAL_INSTANCE.getTimeInMillis());
 
     interactionBean.setId(INTERACTION_ID);
     interactionBean.setApplication(APPLICATION_ID);
     interactionBean.setProducer(PRODUCER_ID);
     interactionBean.setConsumer(CONSUMER_ID);
     interactionBean.setSnooze(0);
     interactionBean.setComplexInteraction(INTERACTION_LST_ID);
 
     authenticationBean = new MAuthenticationBean();
     authenticationBean.setLogin(LOGIN);
     authenticationBean.setPassword(getRandomPwd());
     authenticationBean.setUser(USER_ID);
 
     reputationBean = new MReputationBean();
     reputationBean.setInteractionList(APPLICATION_ID + PRODUCER_ID);
     reputationBean.setRaterList(APPLICATION_ID + PRODUCER_ID);
     reputationBean.setNbInteraction(1);
     reputationBean.setValue(0.6);
   }
 
   @Before
   public void setUp() throws InternalBackEndException {
     storageManager = new StorageManager(new WrapperConfiguration(new File(CONF_FILE)));
     sessionManager = new SessionManager(storageManager);
     profileManager = new ProfileManager(storageManager);
     reputationManager = new ReputationManager(storageManager);
     interactionManager = new InteractionManager(storageManager);
     authenticationManager = new AuthenticationManager(storageManager);
     geolocationManager = new GeoLocationManager();
   }
 
   @After
   public void cleanUp() {
     sessionManager = null; // NOPMD
     storageManager = null; // NOPMD
     profileManager = null; // NOPMD
     reputationManager = null; // NOPMD
     interactionManager = null; // NOPMD
     authenticationManager = null; // NOPMD
     geolocationManager = null; // NOPMD
   }
 }
