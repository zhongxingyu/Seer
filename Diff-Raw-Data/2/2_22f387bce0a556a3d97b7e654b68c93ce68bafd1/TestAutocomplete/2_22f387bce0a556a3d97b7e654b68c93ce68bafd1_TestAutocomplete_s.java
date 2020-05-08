 /*******************************************************************************
  * JBoss, Home of Professional Open Source
  * Copyright 2010, Red Hat, Inc. and individual contributors
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
 package org.richfaces.tests.metamer.ftest.richAutocomplete;
 
 import static org.jboss.test.selenium.utils.URLUtils.buildUrl;
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertFalse;
 import static org.jboss.test.selenium.RequestTypeModelGuard.guardXhr;
 
 import java.net.URL;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.xml.bind.JAXBException;
 
 import org.richfaces.tests.metamer.bean.Model;
 import org.richfaces.tests.metamer.ftest.AbstractMetamerTest;
 import org.richfaces.tests.metamer.ftest.annotations.Inject;
 import org.richfaces.tests.metamer.ftest.annotations.Use;
 import org.richfaces.tests.metamer.ftest.model.Autocomplete;
 import org.richfaces.tests.metamer.model.Capital;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 /**
  * @author <a href="mailto:lfryc@redhat.com">Lukas Fryc</a>
  * @version $Revision$
  */
 public class TestAutocomplete extends AbstractMetamerTest {
 
     final static Boolean[] booleanValues = new Boolean[] { true, false };
 
     AutocompleteAttributes attributes = new AutocompleteAttributes();
     Autocomplete autocomplete = new Autocomplete();
 
     @Override
     public URL getTestUrl() {
        return buildUrl(contextPath, "faces/components/richAutocomplete/autocompleteValidation.xhtml");
     }
 
     @Inject
     @Use("booleanValues")
     Boolean autofill;
 
     @Inject
     @Use("booleanValues")
     Boolean selectFirst;
 
     List<Capital> capitals;
 
     StringBuilder partialInput;
 
     {
         try {
             capitals = Model.unmarshallCapitals();
         } catch (JAXBException e) {
             throw new IllegalStateException(e);
         }
     }
 
     @BeforeMethod
     public void prepareProperties() {
         attributes.setAutofill(autofill);
         attributes.setSelectFirst(selectFirst);
         if (autofill == null) {
             autofill = false;
         }
         if (selectFirst == null) {
             selectFirst = false;
         }
     }
 
     @Test
     public void testConditions() {
 
         assertFalse(autocomplete.isCompletionVisible());
 
         typePrefix("ala");
 
         deleteAll();
     }
 
     public void deleteAll() {
         partialInput = new StringBuilder();
 
         autocomplete.textSelectAll();
         guardXhr(autocomplete).pressBackspace();
 
         assertEquals(autocomplete.getInputText(), getExpectedStateForPrefix());
         assertEquals(autocomplete.getSelectedOptionIndex(), getExpectedSelectedOptionIndex());
     }
 
     public void typePrefix(String wholeInput) {
         partialInput = new StringBuilder(autocomplete.getInputText());
 
         for (int i = 0; i < wholeInput.length(); i++) {
             String chr = String.valueOf(wholeInput.charAt(i));
 
             guardXhr(autocomplete).typeKeys(chr);
             partialInput.append(chr);
 
             assertEquals(autocomplete.getInputText(), getExpectedStateForPrefix());
             assertEquals(autocomplete.getSelectedOptionIndex(), getExpectedSelectedOptionIndex());
         }
     }
 
     public String getExpectedStateForPrefix() {
         if (selectFirst && autofill && partialInput.length() > 0) {
             return getStatesByPrefix(partialInput.toString()).get(0).toLowerCase();
         }
 
         return partialInput.toString();
     }
 
     public int getExpectedSelectedOptionIndex() {
         return (selectFirst) ? 0 : -1;
     }
 
     public List<String> getStatesByPrefix(String prefix) {
         List<String> states = new LinkedList<String>();
 
         for (Capital cap : capitals) {
             if (cap.getState().toLowerCase().startsWith(prefix)) {
                 states.add(cap.getState());
             }
         }
 
         return states;
     }
 }
