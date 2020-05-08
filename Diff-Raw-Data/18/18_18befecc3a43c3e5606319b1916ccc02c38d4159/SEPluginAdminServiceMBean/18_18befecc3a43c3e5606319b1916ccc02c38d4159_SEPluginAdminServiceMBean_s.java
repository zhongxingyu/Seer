 /*
  * SCI-Flex: Flexible Integration of SOA and CEP
  * Copyright (C) 2008, 2009  http://sci-flex.org
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  */
 
 package org.sciflex.plugins.synapse.esper.service;
 
 import org.sciflex.plugins.synapse.esper.core.util.EPLQueryObject;
 import org.sciflex.plugins.synapse.esper.core.util.AdminStatisticsObject;
 
 /**
  * Admin Service for SCI-Flex Synapse Esper Plugin Management Bean Interface
  */
 public interface SEPluginAdminServiceMBean {
 
     /**
      * Gets CEP Instance URI.
      *
      * @param mediatorUID parent mediator's UID.
      * @return            CEP Instance URI.
      * @throws Exception
      */
     public String getCEPInstanceURI(String cepInstanceEditorUID) throws Exception;
 
     /**
      * Sets CEP Instance URI.
      *
      * @param mediatorUID parent mediator's UID.
      * @param instanceURI CEP Instance URI.
      * @return            true if operation was successful, false otherwise.
      * @throws Exception
      */
     public boolean setCEPInstanceURI(String cepInstanceEditorUID, String instanceURI) throws Exception;
 
     /**
      * Gets Registry Key.
      *
      * @param mediatorUID parent mediator's UID.
      * @return            Registry Key.
      * @throws Exception
      */
     public String getRegistryKey(String registryKeyEditorUID) throws Exception;
 
     /**
      * Sets Registry Key.
      *
      * @param mediatorUID parent mediator's UID.
      * @param registryKey Registry Key.
      * @return            true if operation was successful, false otherwise.
      * @throws Exception
      */
     public boolean setRegistryKey(String registryKeyEditorUID, String registryKey) throws Exception;
 
     /**
      * Gets Static EPL.
      *
      * @param mediatorUID parent mediator's UID.
      * @return            Static EPL.
      * @throws Exception
      */
     public String getStaticEPL(String staticEPLEditorUID) throws Exception;
 
     /**
      * Sets Static EPL.
      *
      * @param mediatorUID parent mediator's UID.
      * @param staticEPL   Static EPL.
      * @return            true if operation was successful, false otherwise.
      * @throws Exception
      */
     public boolean setStaticEPL(String staticEPLEditorUID, String staticEPL) throws Exception;
 
     /**
      * Gets Dynamic EPL.
      *
      * @param mediatorUID parent mediator's UID.
      * @return            Dynamic EPL.
      * @throws Exception
      */
     public String getDynamicEPL(String dynamicEPLEditorUID) throws Exception;
 
     /**
      * Sets Dynamic EPL.
      *
      * @param mediatorUID parent mediator's UID.
      * @param dynamicEPL  Dynamic EPL.
      * @return            true if operation was successful, false otherwise.
      * @throws Exception
      */
     public boolean setDynamicEPL(String mediatorUID, String dynamicEPL) throws Exception;
 
     /**
      * Stops Mediator
      *
      * @param mediatorUID mediator's UID
      * @throws Exception
      */
     public void stopMediator(String mediatorUID) throws Exception;
 
     /**
      * Starts Mediator
      *
      * @param mediatorUID mediator's UID
      * @throws Exception
      */
     public void startMediator(String mediatorUID) throws Exception;
 
     /**
      * Refreshes Mediator
      *
      * @param mediatorUID mediator's UID
      * @throws Exception
      */
     public void refreshMediator(String mediatorUID) throws Exception;
 
     /**
      * Gets how long the Mediator has been active.
      *
      * @param mediatorUID mediator's UID
      * @return            active time
      * @throws Exception
      */
     public long getMediatorActiveTime(String mediatorUID) throws Exception;
 
     /**
      * Indicates whether the mediator is active.
      *
      * @param mediatorUID mediator's UID
      * @return            true if active, false if not
      * @throws Exception
      */
     public boolean getIsMediatorActive(String mediatorUID) throws Exception;
 
     /**
      * Resets Mediator Activity Timer.
      *
      * @param mediatorUID mediator's UID
      * @throws Exception
      */
     public void resetMediatorActivityTimer(String mediatorUID) throws Exception;
 
     /**
      * Resets Mediator Statistics Monitor.
      *
      * @param mediatorUID mediator's UID
      * @throws Exception
      */
     public void resetMediatorStatisticsMonitor(String mediatorUID) throws Exception;
 
     /**
      * Gets Maximum Response Time.
      *
      * @param mediatorUID mediator's UID
      * @return            Maximum Response Time
      * @throws Exception
      */
     public long getMaximumResponseTime(String mediatorUID) throws Exception;
 
     /**
      * Gets Maximum Request Time.
      *
      * @param mediatorUID mediator's UID
      * @return            Maximum Request Time
      * @throws Exception
      */
     public long getMaximumRequestTime(String mediatorUID) throws Exception;
 
     /**
      * Gets Minimum Response Time.
      *
      * @param mediatorUID mediator's UID
      * @return            Minimum Response Time
      * @throws Exception
      */
     public long getMinimumResponseTime(String mediatorUID) throws Exception;
 
     /**
      * Gets Minimum Request Time.
      *
      * @param mediatorUID mediator's UID
      * @return            Minimum Request Time
      * @throws Exception
      */
     public long getMinimumRequestTime(String mediatorUID) throws Exception;
 
     /**
      * Gets Average Response Time.
      *
      * @param mediatorUID mediator's UID
      * @return            Average Response Time
      * @throws Exception
      */
     public double getAverageResponseTime(String mediatorUID) throws Exception;
 
     /**
      * Gets Average Request Time.
      *
      * @param mediatorUID mediator's UID
      * @return            Average Request Time
      * @throws Exception
      */
     public double getAverageRequestTime(String mediatorUID) throws Exception;
 
     /**
      * Gets load on mediator as a percentage of total load.
      *
      * @param mediatorUID mediator's UID
      * @return            load on mediator
      * @throws Exception
      */
     public double getLoadOnMediator(String mediatorUID) throws Exception;
 
     /**
      * Resets Query Activity Monitor.
      *
      * @param mediatorUID mediator's UID
      * @throws Exception
      */
     public void resetQueryActivityMonitor(String mediatorUID) throws Exception;
 
     /**
      * Browse Query Activities.
      *
      * @param mediatorUID mediator's UID
      * @return            array of query objects
      * @throws Exception
      */
     public EPLQueryObject[] getQueryActivities(String mediatorUID) throws Exception;
 
     /**
      * Resets all Mediator Timers and Counters.
      *
      * @param mediatorUID mediator's UID
      * @throws Exception
      */
     public void resetAll(String mediatorUID) throws Exception;
 
     /**
      * Gets all Mediator statistics.
      *
      * @param mediatorUID mediator's UID
      * @return            object containing Mediator statistics
      * @throws Exception
      */
     public AdminStatisticsObject getAllStatistics(String mediatorUID) throws Exception;
 
 }
