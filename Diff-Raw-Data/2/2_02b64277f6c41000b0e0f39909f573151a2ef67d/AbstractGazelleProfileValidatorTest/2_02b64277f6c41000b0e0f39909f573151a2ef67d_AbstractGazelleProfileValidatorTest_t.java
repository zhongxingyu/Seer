 /*
  * Copyright 2013 the original author or authors.
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
 package org.openehealth.ipf.gazelle.validation.core;
 
 import ca.uhn.hl7v2.DefaultHapiContext;
 import ca.uhn.hl7v2.HL7Exception;
 import ca.uhn.hl7v2.HapiContext;
 import ca.uhn.hl7v2.Severity;
 import ca.uhn.hl7v2.conf.ProfileException;
 import ca.uhn.hl7v2.conf.parser.ProfileParser;
 import ca.uhn.hl7v2.conf.spec.RuntimeProfile;
 import ca.uhn.hl7v2.conf.store.DefaultCodeStoreRegistry;
 import ca.uhn.hl7v2.model.Message;
 import org.apache.commons.io.IOUtils;
 import org.junit.Before;
 import org.openehealth.ipf.gazelle.validation.profile.store.GazzelleProfileStore;
 
 import java.io.IOException;
 
 /**
  * @author Boris Stanojevic
  */
 public abstract class AbstractGazelleProfileValidatorTest {
 
     protected HapiContext hapiContext;
 
     @Before
     public void onBefore() throws IOException {
         hapiContext = createHapiContext(false);
     }
 
     protected void printOutExceptions(HL7Exception[] exceptions){
         for (HL7Exception exc: exceptions){
             switch (exc.getSeverity()) {
                 case ERROR:   System.err.println("ERROR:" + exc.getMessage());break;
                 case WARNING: System.out.println("WARNING:" + exc.getMessage());break;
                 case INFO:    System.out.println("INFO:" + exc.getMessage());break;
             }
         }
     }
 
     protected int countExceptions(HL7Exception[] exceptions, Severity severity){
         int count = 0;
         for (HL7Exception exc: exceptions){
             if (severity.equals(exc.getSeverity())){ ++count;}
         }
         return count;
     }
 
     protected String getMessageAsString(String resourcePath){
         String message = null;
         try {
             message = IOUtils.toString(this.getClass().getClassLoader().getResource(resourcePath));
         } catch (IOException ioe){
             ioe.printStackTrace();
         }
        return message.replaceAll("\n", "\r");
     }
 
     protected HapiContext createHapiContext(boolean validating) throws IOException {
         HapiContext hapiContext = new DefaultHapiContext();
         hapiContext.setProfileStore(new GazzelleProfileStore());
         hapiContext.getParserConfiguration().setValidating(validating);
         hapiContext.setCodeStoreRegistry(new DefaultCodeStoreRegistry());
         return hapiContext;
     }
 
     protected RuntimeProfile parseProfile(String profileId)
             throws ProfileException, IOException {
         String profileString = hapiContext.getProfileStore().getProfile(profileId);
         ProfileParser profileParser = new ProfileParser(false);
         return profileParser.parse(profileString);
     }
 
     protected Message getParsedMessage(String path) throws HL7Exception {
         return hapiContext.getPipeParser().parse(getMessageAsString(path));
     }
 
 }
