 /*
  * ################################################################
  *
  * ProActive: The Java(TM) library for Parallel, Distributed,
  *            Concurrent computing with Security and Mobility
  *
  * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
  * Contact: proactive@objectweb.org
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
  * USA
  *
  *  Initial developer(s):               The ProActive Team
  *                        http://www.inria.fr/oasis/ProActive/contacts.html
  *  Contributor(s):
  *
  * ################################################################
  */
 package functionalTests;
 
 import org.junit.BeforeClass;
 import org.objectweb.proactive.core.config.ProActiveConfiguration;
 
 import functionalTests.FunctionalTest;
 
 
 /**
  * @author Matthieu Morel
  *
  */
 public abstract class ComponentTest extends FunctionalTest {
 
     /**
      * @param name
      */
     public ComponentTest() {
         //  super("[COMPONENTS] " + name);
     }
 
     /**
      * @param name
      * @param description
      */
     public ComponentTest(String name, String description) {
         //  super("Components : " + name, description);
     }
 
     @BeforeClass
    public static void preConditions() throws Exception {
         if (!"enable".equals(ProActiveConfiguration.getInstance()
                                                        .getProperty("proactive.future.ac"))) {
             throw new Exception(
                 "The components framework needs the automatic continuations (system property 'proactive.future.ac' set to 'enable') to be operative");
         }
 
         //-Dfractal.provider=org.objectweb.proactive.core.component.Fractive
         if (!"org.objectweb.proactive.core.component.Fractive".equals(
                     ProActiveConfiguration.getInstance()
                                               .getProperty("fractal.provider"))) {
             ProActiveConfiguration.getInstance()
                                   .setProperty("fractal.provider",
                 "org.objectweb.proactive.core.component.Fractive");
         }
     }
 }
