 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.core.persistence;
 
 import java.util.Collections;
 import java.util.Map;
 
 import javax.persistence.EntityManager;
 
 import org.eclipse.jubula.client.core.model.ITestResultSummaryPO;
 import org.eclipse.jubula.client.core.model.PoMaker;
 import org.eclipse.jubula.tools.objects.IMonitoringValue;
 
 /**
  * Loads monitoring values from the database
  * 
  * @author BREDEX GmbH
  * @created 10.10.2010
  */
 public class MonitoringValuePM {
 
     /** to prevent instantiation */
     private MonitoringValuePM() {
         // DO NOTHING
     }
 
     /**
      * 
      * @param summaryID
      *            The current selected summaryID
      * @return The monitored values
      */
     public static final Map<String, IMonitoringValue> loadMonitoringValues(
             Object summaryID) {
         if (Persistor.instance() == null) {
             return Collections.emptyMap();
         }
         EntityManager session = Persistor.instance().openSession();
         ITestResultSummaryPO summary;
         try {
             Persistor.instance().getTransaction(session);
             summary = session.find(PoMaker.getTestResultSummaryClass(),
                     summaryID);
            if (summary == null) {
                return Collections.emptyMap();
            }
         } finally {
             Persistor.instance().dropSession(session);
         }
         return summary.getMonitoringValues();
     }
 
 }
