 /*
  * Copyright (C) 2000 - 2012 Silverpeas
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * As a special exception to the terms and conditions of version 3.0 of
  * the GPL, you may redistribute this Program in connection withWriter Free/Libre
  * Open Source Software ("FLOSS") applications as described in Silverpeas's
  * FLOSS exception.  You should have recieved a copy of the text describing
  * the FLOSS exception, and it is also available here:
  * "http://www.silverpeas.org/legal/licensing"
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.silverpeas.file;
 
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.Properties;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import static org.hamcrest.Matchers.*;
 
 /**
  *
  * @author ehugonnet
  */
 public class GestionVariablesTest {
 
   public GestionVariablesTest() {
   }
 
   /**
    * Test of addVariable method, of class GestionVariables.
    */
   @Test
   public void testAddVariable() throws IOException {
    String pName = "SILVERPEAS_HOME";
     String pValue = "C:/toto";
     GestionVariables instance = new GestionVariables(new Properties());
     try {
       assertThat(instance.getValue(pName), is(not(pValue)));
       fail("SILVERPEAS_HOME should not exist");
     } catch (IOException ioex) {
     }
     instance.addVariable(pName, pValue);
     assertThat(instance.getValue(pName), is(pValue));
   }
 
   /**
    * Test of resolveString method, of class GestionVariables.
    */
   @Test
   public void testResolveString() throws Exception {
     GestionVariables instance = new GestionVariables(new Properties());
     instance.addVariable("SILVERPEAS_HOME", "/home/bart/silverpeas");
     String result = instance.resolveString("${SILVERPEAS_HOME}/data/portlets/config/pcenv.conf");
     assertThat(result, is("/home/bart/silverpeas/data/portlets/config/pcenv.conf"));
   }
 
   /**
    * Test of resolveAndEvalString method, of class GestionVariables.
    */
   @Test
   public void testResolveAndEvalString() throws Exception {
     GestionVariables instance = new GestionVariables(new Properties());
     instance.addVariable("SILVERPEAS_HOME", "/home/bart/silverpeas");
     String result = instance.resolveAndEvalString(
         "${SILVERPEAS_HOME}/data/portlets/config/pcenv.conf");
     assertThat(result, is("/home/bart/silverpeas/data/portlets/config/pcenv.conf"));
   }
 }
