 /*
  *  GeoBatch - Open Source geospatial batch processing system
  *  http://code.google.com/p/geobatch/
  *  Copyright (C) 2007-2008-2009 GeoSolutions S.A.S.
  *  http://www.geo-solutions.it
  *
  *  GPLv3 + Classpath exception
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package it.geosolutions.geobatch.figis.setting;
 
 import java.util.ArrayList;
 import java.util.EventObject;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 
 import it.geosolutions.figis.model.Config;
 import it.geosolutions.figis.model.Intersection;
 import it.geosolutions.figis.model.Intersection.Status;
 import it.geosolutions.figis.requester.requester.dao.IEConfigDAO;
 import it.geosolutions.figis.requester.requester.dao.impl.IEConfigDAOImpl;
 import it.geosolutions.figis.requester.requester.util.IEConfigUtils;
 import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
 import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
 import it.geosolutions.geobatch.flow.event.action.ActionException;
 import it.geosolutions.geobatch.flow.event.action.BaseAction;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  *
  * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
  *
  */
 public class SettingAction extends BaseAction<EventObject>
 {
     private static final Logger LOGGER = LoggerFactory.getLogger(SettingAction.class);
 
     private IEConfigDAO ieConfigDAO = new IEConfigDAOImpl();
 
     private String defaultMaskLayer = null;
     private String host = null;
     private String ieServiceUsername = null;
     private String ieServicePassword = null;
 
     /**
     * configuration
     */
     private final SettingConfiguration conf;
 
     public SettingAction(SettingConfiguration configuration)
     {
         super(configuration);
         conf = configuration;
     }
 
     /**
      * Removes TemplateModelEvents from the queue and put
      */
     public Queue<EventObject> execute(Queue<EventObject> events) throws ActionException
     {
         Config xmlConfig = null;
         Config dbConfig = null;
         host = conf.getPersistencyHost();
         ieServiceUsername = conf.getIeServiceUsername();
         ieServicePassword = conf.getIeServicePassword();
         defaultMaskLayer = conf.getDefaultMaskLayer();
 
         final Queue<EventObject> ret = new LinkedList<EventObject>();
         LOGGER.info("Setting action started with parameters " + host + ", " + defaultMaskLayer);
         while (events.size() > 0)
         {
             final EventObject ev;
             try
             {
                 if ((ev = events.remove()) != null)
                 {
                     if (LOGGER.isTraceEnabled())
                     {
                         LOGGER.trace("IntersectionAction.execute(): working on incoming event: " + ev.getSource());
                     }
 
                     FileSystemEvent fileEvent = (FileSystemEvent) ev;
                     FileSystemEventType eventType = fileEvent.getEventType();
 
                     // READ THE XML AND CREATE A CONFIG OBJECT
                     xmlConfig = IEConfigUtils.parseXMLConfig(fileEvent.getSource().getAbsolutePath());
 
                     // if DB is empty lets insert the new configuration...
                     if (ieConfigDAO.dbIsEmpty(host, getIeServiceUsername(), getIeServicePassword()))
                     {
                         dbConfig = ieConfigDAO.saveOrUpdateConfig(host, xmlConfig, getIeServiceUsername(), getIeServicePassword());
 
                         dbConfig.setGlobal(xmlConfig.getGlobal());
                         dbConfig.setUpdateVersion(xmlConfig.getUpdateVersion() - 1);
 
                         ieConfigDAO.setStatus(host, dbConfig.intersections, Status.TOCOMPUTE, getIeServiceUsername(), getIeServicePassword());
                     }
                     // check for updates otherwise...
                     else
                     {
                         dbConfig = ieConfigDAO.loadConfg(host, getIeServiceUsername(), getIeServicePassword());
                     }
 
 
                     // after checking for a valid update version ...
                     if (xmlConfig.getUpdateVersion() > dbConfig.getUpdateVersion())
                     {
                         dbConfig.setUpdateVersion(xmlConfig.getUpdateVersion());
                         dbConfig.setGlobal(xmlConfig.getGlobal());
 
                         // lets compare the intersections between xml config and db
                         List<Intersection> intersectionsToAdd = new ArrayList<Intersection>();
                         for (Intersection xmlIntersection : xmlConfig.intersections)
                         {
                             Intersection dbIntersection = ieConfigDAO.searchEquivalent(host, xmlIntersection,
                                     dbConfig.intersections, getIeServiceUsername(), getIeServicePassword());
 
                             // not present in DB, lets add the new one
                             if (dbIntersection == null)
                             {
                                 xmlIntersection.setStatus(Status.TOCOMPUTE);
                                 intersectionsToAdd.add(xmlIntersection);
                             }
                             else
                             {
                                 // it already computed or computing but we want to force the re-computation ...
                                 if (dbIntersection.getStatus().equals(Status.COMPUTED) ||
                                         dbIntersection.getStatus().equals(Status.COMPUTING) ||
                                         dbIntersection.getStatus().equals(Status.NOVALUE))
                                 {
                                     if (xmlConfig.getGlobal().isClean() || xmlIntersection.isForce())
                                     {
                                         xmlIntersection.setStatus(Status.TOCOMPUTE);
                                     }
                                     else
                                     {
                                         xmlIntersection.setStatus(dbIntersection.getStatus());
                                     }
                                 }
                                 // otherwise in any case we assume the user wanted to re-schedule it for computation ...
                                 else
                                 {
                                     xmlIntersection.setStatus(Status.TOCOMPUTE);
                                 }
                                 intersectionsToAdd.add(xmlIntersection);
                             }
                         }
 
                         // if clean flag is set to true we need to schedule other intersections for deletion
                         if (xmlConfig.getGlobal().isClean())
                         {
                             for (Intersection dbIntersection : dbConfig.intersections)
                             {
                                 Intersection equivalentToAdd = ieConfigDAO.searchEquivalent(host, dbIntersection,
                                         intersectionsToAdd, getIeServiceUsername(), getIeServicePassword());
                                 if (equivalentToAdd == null)
                                 {
                                     dbIntersection.setStatus(Status.TODELETE);
                                }
                                else
                                {
                                    dbIntersection = equivalentToAdd;
                                 }
                             }
                         }
 
                         for (Intersection dbIntersection : dbConfig.intersections)
                         {
                             ieConfigDAO.deleteIntersectionById(host, dbIntersection.getId(), ieServiceUsername, ieServicePassword);
                         }
 
                         dbConfig.intersections = intersectionsToAdd;
 
                         // finally update the db-config
                         ieConfigDAO.saveOrUpdateConfig(host, dbConfig, getIeServiceUsername(), getIeServicePassword());
                     }
 
                     // add the event to the return
                     // ret.add(ev);
                 }
                 else
                 {
                     if (LOGGER.isErrorEnabled())
                     {
                         LOGGER.error("IntersectionAction.execute(): Encountered a NULL event: SKIPPING...");
                     }
 
                     continue;
                 }
             }
             catch (Exception ioe)
             {
                 ioe.printStackTrace();
 
                 final String message = "IntersectionAction.execute(): Unable to produce the output: " +
                     ioe.getLocalizedMessage();
                 if (LOGGER.isErrorEnabled())
                 {
                     LOGGER.error(message);
                 }
                 throw new ActionException(this, message);
             }
         }
 
         return ret;
     }
 
 
     public String getIeServiceUsername()
     {
         return ieServiceUsername;
     }
 
     public void setIeServiceUsername(String ieServiceUsername)
     {
         this.ieServiceUsername = ieServiceUsername;
     }
 
     public String getIeServicePassword()
     {
         return ieServicePassword;
     }
 
     public void setIeServicePassword(String ieServicePassword)
     {
         this.ieServicePassword = ieServicePassword;
     }
 
 
     // ------------------------------------------------------------------------------------------------------------------
 }
