 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 package org.amanzi.neo.loader.core.preferences;
 
 /**
  * <p>
  * Preference constant
  * </p>
  * 
  * @author Cinkel_A
  * @since 1.0.0
  */
 public class DataLoadPreferences {
     public static final String DEFAULT_DIRRECTORY_LOADER = "DEFAULT_DIRRECTORY_LOADER";
     /** DataLoadPreferences REMOVE_SITE_NAME field */
     public static final String REMOVE_SITE_NAME = "REMOVE_SITE_NAME";
     public static final String NETWORK_COMBINED_CALCULATION = "USE_COMBINED_CALCULATION";
     public static final String ZOOM_TO_LAYER = "ZOOM_TO_LAYER";
     
     public static final String ADD_AMS_PROBES_TO_MAP = "ADD_AMS_PROBES_TO_MAP";
     public static final String ADD_AMS_EVENTS_TO_MAP = "ADD_AMS_EVENTS_TO_MAP";
     public static final String ADD_AMS_CALLS_TO_MAP = "ADD_AMS_CALLS_TO_MAP";
 
     // network loader headers
     public static final String NH_CITY = "NH_CITY";
     public static final String NH_MSC = "NH_MSC";
     public static final String NH_BSC = "NH_BSC";
     public static final String NH_SITE = "NH_SITE";
     public static final String NH_SECTOR = "NH_SECTOR";
     public static final String NH_SECTOR_CI = "NH_SECTOR_CI";
     public static final String NH_SECTOR_LAC = "NH_SECTOR_LAC";
     public static final String NH_LATITUDE = "NH_LATITUDE";
     public static final String NH_LONGITUDE = "NH_LONGITUDE";
     public static final String DEFAULT_CHARSET = "DEFAULT_CHARSET";
     // drive loader headers
     public static final String DR_LATITUDE = "DR_LATITUDE";
     public static final String DR_LONGITUDE = "DR_LONGITUDE";
     public static final String DR_BCCH = "DR_BCCH";
     public static final String DR_TCH = "DR_TCH";
     public static final String DR_SC = "DR_SC";
     public static final String DR_PN = "DR_PN";
     public static final String DR_EcIo = "DR_EcIo";
     public static final String DR_RSSI = "DR_RSSI";
     public static final String DR_CI = "DR_CI";
     
     // probe loader headers
     public static final String PR_NAME = "PR_NAME";
     public static final String PR_TYPE = "PR_TYPE";
     public static final String PR_LATITUDE = "PR_LATITUDE";
     public static final String PR_LONGITUDE = "PR_LONGITUDE";
     
 	// network site loader headers
     public static final String NH_BEAMWIDTH = "NS_BEAMWIDTH";
     public static final String NH_AZIMUTH = "NS_AZIMUTH";
     
     // neighbour loader headers
     public static final String NE_SRV_CI = "NE_SRV_CI";
     public static final String NE_SRV_LAC = "NE_SRV_LAC";
     public static final String NE_SRV_NAME = "NE_SRV_NAME";
     
     public static final String NE_NBR_CI = "NE_NBR_CI";
     public static final String NE_NBR_LAC = "NE_NBR_LAC";
     public static final String NE_NBR_NAME = "NE_NBR_NAME";
     
     // transmission loader headers
     public static final String TR_SITE_ID_SERV = "TR_SITE_ID_SERV";
     public static final String TR_SITE_NO_SERV = "TR_SITE_NO_SERV";
     public static final String TR_ITEM_NAME_SERV = "TR_ITEM_NAME_SERV";
     
     public static final String TR_SITE_ID_NEIB = "TR_SITE_ID_NEIB";
     public static final String TR_SITE_NO_NEIB = "TR_SITE_NO_NEIB";
     public static final String TR_ITEM_NAME_NEIB = "TR_ITEM_NAME_NEIB";
     
     public static final String COMMON_CRS_LIST = "COMMON_CRS_LIST";
     public static final String CRS_DELIMETERS = "--DELIMETER--";
     
     
     public static final String PROPERY_LISTS = "PROPERY_LISTS";
     // public static final String FILTER_RULES = "FILTER_RULES";
     public static final String SELECTED_DATA = "SELECTED_DATA";
     public static final String REMOTE_SERVER_URL = "REMOTE_SERVER_URL";
     public static final String USER_IMEI = "USER_IMEI";
     public static final String USER_IMSI = "USER_IMSI";
 
     private DataLoadPreferences() {
 
     }
 
 }
