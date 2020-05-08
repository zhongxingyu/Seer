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
 package org.eclipse.jubula.autagent.monitoring;
 
 import java.io.File;
 import java.io.OutputStream;
 import java.util.Map;
 
 import org.eclipse.jubula.tools.objects.IMonitoringValue;
 
 /**
  * Monitoring agents classes must extend this class.
  *
  * @author BREDEX GmbH
  * @created 11.11.2010
  */
 public abstract class AbstractMonitoring implements IMonitoring {

     /** the autId */
     private String m_autId;  
     /** 
      * File referring the installtion directory. Used to find support
      * files.
      */
     private File m_installationDir;
     
     /** MonitoringDataStore which contains the configuration maps */
     private MonitoringDataStore m_mds = MonitoringDataStore.getInstance();
     /**
      * {@inheritDoc}
      */
     public void autRestartOccurred() {       
         //do nothing
     }
 
     /**
      * {@inheritDoc}
      */
     public void writeMonitoringReport(OutputStream out) {
       //do nothing
     }
 
     /**
      * {@inheritDoc}
      */
     public String createAgent() {
         
         return null;
     }
 
     /**
      * {@inheritDoc}
      */
     public Map<String, IMonitoringValue> getMonitoringData() {
        
        return null;
     }
 
     /**
      * {@inheritDoc}
      */
     public void resetMonitoringData() {       
       //do nothing
         
     }  
      
     /**
      * 
      * @return the autId 
      */
     protected String getAutId() {
         
         return m_autId; 
     }
     /**
      * {@inheritDoc}
      */
     public void setAutId(String autId) {
         
         this.m_autId = autId;
         
     }
     /** 
      *  
      * @param key The key for the configMap
      * @return The value 
      */
     protected String getMonitoringAttribute(String key) {
         
         return m_mds.getConfigValue(getAutId(), key);
 
     }
 
     /**
      * {@inheritDoc}
      */
     public void setInstallDir(File installDir) {
         m_installationDir = installDir;
     }
     
     /**
      * 
      * @return the installation directory
      */
     protected File getInstallDir() {
         return m_installationDir;
     }
 }
