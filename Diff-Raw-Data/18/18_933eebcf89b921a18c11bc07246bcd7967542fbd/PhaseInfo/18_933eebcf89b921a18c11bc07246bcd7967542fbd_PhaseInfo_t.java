 /*******************************************************************************
  * JBoss, Home of Professional Open Source
 * Copyright 2010-2011, Red Hat, Inc. and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  *******************************************************************************/
 package org.richfaces.tests.metamer.ftest;
 
 import static org.jboss.test.selenium.locator.LocatorFactory.jq;
 import static org.testng.Assert.assertEquals;
 
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.Map;
 import java.util.Set;
 
 import javax.faces.event.PhaseId;
 
 import org.jboss.test.selenium.framework.AjaxSelenium;
 import org.jboss.test.selenium.framework.AjaxSeleniumProxy;
 import org.jboss.test.selenium.locator.JQueryLocator;
 import org.jboss.test.selenium.waiting.retrievers.RetrieverFactory;
 import org.jboss.test.selenium.waiting.retrievers.TextRetriever;
 
 /**
  * Retrieves and asserts the info about life-cycle.
  * 
  * @author <a href="mailto:ppitonak@redhat.com">Lukas Fryc</a>
  * @version $Revision$
  */
 public class PhaseInfo {
 
     private AjaxSelenium selenium = AjaxSeleniumProxy.getInstance();
     private TextRetriever retrieveRequestTime = RetrieverFactory.RETRIEVE_TEXT.locator(jq("span[id$=requestTime]"));
     private JQueryLocator phasesItems = jq("div#phasesPanel li");
     private Map<PhaseId, Set<String>> map = new LinkedHashMap<PhaseId, Set<String>>();
 
     /**
      * Asserts that the phases has occurred in last request by the specified list.
      */
     public void assertPhases(PhaseId... expectedPhases) {
         initialize();
         PhaseId[] actualPhases = map.keySet().toArray(new PhaseId[map.size()]);
         assertEquals(actualPhases, expectedPhases);
     }
 
     /**
      * Asserts that in the given phase has occurred the listener or order producer writing the log message to phases
      * list.
      * 
      * @param phaseId
      *            the phase where the listener occurred
      * @param message
      *            the part of the message which it should be looked up
      */
     public void assertListener(PhaseId phaseId, String message) {
         initialize();
         Set<String> set = map.get(phaseId);
         if (set != null && set.size() > 0) {
             for (String description : set) {
                 if (description.contains(message)) {
                     return;
                 }
             }
         }
         throw new AssertionError("The '" + message + "' wasn't found across messages in phase " + phaseId);
     }
 
     private void initialize() {
         if (retrieveRequestTime.isValueChanged()) {
             retrieveRequestTime.initializeValue();
 
             int count = selenium.getCount(phasesItems);
 
             Set<String> set;
            for (int i = 1; i <= count; i++) {
                 String description = selenium.getText(phasesItems.getNthChildElement(i));
 
                 set = new LinkedHashSet<String>();
                 if (!description.startsWith("* ")) {
                     map.put(getPhaseId(description), set);
                 } else {
                     set.add(description.substring(2));
                 }
             }
         }
     }
 
     private PhaseId getPhaseId(String phaseIdentifier) {
         for (PhaseId phaseId : PhaseId.VALUES) {
             if (phaseIdentifier.startsWith(phaseId.toString())) {
                 return phaseId;
             }
         }
         throw new IllegalStateException("no such phase '" + phaseIdentifier + "'");
     }
 }
