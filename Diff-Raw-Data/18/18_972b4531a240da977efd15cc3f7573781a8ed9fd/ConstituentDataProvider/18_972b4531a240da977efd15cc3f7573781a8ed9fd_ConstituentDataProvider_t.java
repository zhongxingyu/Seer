/*
 * Copyright (c) 2009. Orange Leap Inc. Active Constituent
 * Relationship Management Platform.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

 package com.orangeleap.tangerine.test.dataprovider;
 
 import org.testng.annotations.DataProvider;
 
 import com.orangeleap.tangerine.domain.Constituent;
 import com.orangeleap.tangerine.domain.Site;
 import com.orangeleap.tangerine.domain.communication.Address;
 import com.orangeleap.tangerine.domain.communication.Phone;
 import com.orangeleap.tangerine.domain.customization.CustomField;
 
 public class ConstituentDataProvider {
 
     @DataProvider(name = "setupCreateSite")
     public static Object[][] setupCreateSite() {
         Site site = new Site();
         site.setName("setupCreateSite");
 
         return new Object[][] { new Object[] { site } };
     }
 
     @DataProvider(name = "setupCreateConstituent")
     public static Object[][] setupCreateConstituent() {
         Site site = new Site();
         site.setName("setupCreateConstituent");
 
         Constituent constituent = new Constituent();
         constituent.setFirstName("firstname");
         constituent.setLastName("lastname");
         constituent.setSite(site);
 
         return new Object[][] { new Object[] { site, constituent } };
     }
 
     @DataProvider(name = "setupCreateConstituentAndAddress")
     public static Object[][] setupCreateConstituentAndAddress() {
         Site site = new Site();
         site.setName("setupCreateConstituentAndAddress");
 
         Constituent constituent = new Constituent();
         constituent.setFirstName("firstname");
         constituent.setLastName("lastname");
         constituent.setSite(site);
 
         Address address1 = new Address(constituent.getId());
         address1.setAddressLine1("addressLine1");
         address1.setAddressLine2("addressLine2");
         address1.setAddressLine3("addressLine3");
         address1.setCity("city");
         address1.setCountry("US");
         address1.setPostalCode("postalCode");
         address1.setStateProvince("TX");
 
         Address address2 = new Address(constituent.getId());
         address2.setAddressLine1("addressLine1");
         address2.setAddressLine2("addressLine2");
         address2.setAddressLine3("addressLine3");
         address2.setCity("city");
         address2.setCountry("US");
         address2.setPostalCode("postalCode");
         address2.setStateProvince("TX");
 
         return new Object[][] { new Object[] { site, constituent, address1, address2 } };
     }
 
     @DataProvider(name = "setupCreateConstituentAndPhone")
     public static Object[][] setupCreateConstituentAndPhone() {
         Site site = new Site();
         site.setName("site 1");
 
         Constituent constituent = new Constituent();
         constituent.setFirstName("firstname");
         constituent.setLastName("lastname");
         constituent.setSite(site);
 
         Phone phone1 = new Phone(constituent.getId());
         phone1.setNumber("111-222-3333");
 
         Phone phone2 = new Phone(constituent.getId());
         phone2.setNumber("111-222-3333");
 
         return new Object[][] { new Object[] { site, constituent, phone1, phone2 } };
     }
 
     @DataProvider(name = "setupCreateConstituentAndCustomField")
     public static Object[][] setupCreateConstituentAndCustomField() {
         Site site = new Site();
         site.setName("site 1");
 
         Constituent constituent = new Constituent();
         constituent.setFirstName("firstname");
         constituent.setLastName("lastname");
         constituent.setSite(site);
 
         CustomField cf1 = new CustomField();
         cf1.setName("dog");
         cf1.setValue("barney");
 
         CustomField cf2 = new CustomField();
         cf2.setName("dog2");
         cf2.setValue("barney2");
 
         return new Object[][] { new Object[] { site, constituent, cf1, cf2 } };
     }
 }
