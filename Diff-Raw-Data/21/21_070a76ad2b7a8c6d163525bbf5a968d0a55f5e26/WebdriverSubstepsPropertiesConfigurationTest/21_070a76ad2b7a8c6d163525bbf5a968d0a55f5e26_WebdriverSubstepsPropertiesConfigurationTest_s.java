 /*
  *  Copyright Technophobia Ltd 2012
  *
  *   This file is part of Substeps.
  *
  *    Substeps is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU Lesser General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    Substeps is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU Lesser General Public License for more details.
  *
  *    You should have received a copy of the GNU Lesser General Public License
  *    along with Substeps.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.technophobia.webdriver.substeps.runner;
 
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.startsWith;
 
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 /**
  * @author imoore
  */
 public class WebdriverSubstepsPropertiesConfigurationTest {
 
     @Test
     public void checkOverrides() {
 
         System.setProperty("environment", "localhost");
 
         Assert.assertFalse(WebdriverSubstepsPropertiesConfiguration.INSTANCE.closeVisualWebDriveronFail());
 
     }
 
 
     @Test
     public void testRelativeURLResolvesToFileProtocol() throws SecurityException,
             NoSuchMethodException, IllegalArgumentException, IllegalAccessException,
             InvocationTargetException {
 
        final Method determineBaseURLMethod = WebdriverSubstepsPropertiesConfiguration.class
                .getDeclaredMethod("determineBaseURL", String.class);
 
         determineBaseURLMethod.setAccessible(true);
 
        final String baseUrl = (String) determineBaseURLMethod.invoke(
                WebdriverSubstepsPropertiesConfiguration.class, "src/web");
 
         Assert.assertThat(baseUrl, startsWith("file:/"));
 
        final String baseUrl2 = (String) determineBaseURLMethod.invoke(
                WebdriverSubstepsPropertiesConfiguration.class, "./src/web");
 
         final File current = new File(".");
 
 
         Assert.assertThat(baseUrl2, is(current.toURI().toString() + "src/web"));
 
        final String baseUrl3 = (String) determineBaseURLMethod.invoke(
                WebdriverSubstepsPropertiesConfiguration.class, "http://blah-blah.com/src/web");
 
         Assert.assertThat(baseUrl3, startsWith("http://"));
 
        final String baseUrl4 = (String) determineBaseURLMethod.invoke(
                WebdriverSubstepsPropertiesConfiguration.class, "file://some-path/whatever");
 
         Assert.assertThat(baseUrl4, is("file://some-path/whatever"));
 
     }
 }
