 /*==============================================================================
  * File $Id$
  * Project: ProWim
  *
  * $LastChangedDate: 2009-10-09 14:42:12 +0200 (Fr, 09 Okt 2009) $
  * $LastChangedBy: khodaei $
  * $HeadURL: https://repository.ebcot.info/wivu/trunk/portal/src/de/ebcot/prowim/portal/i18n/configurator/ResourcesConfigurator.java $
  * $LastChangedRevision: 2469 $
  *------------------------------------------------------------------------------
  * (c) 14.05.2009 Ebcot Business Solutions GmbH. More info: http://www.ebcot.de
  * All rights reserved. Use is subject to license terms.
  *==============================================================================
  *
  *This file is part of ProWim.
 
 ProWim is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 ProWim is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with ProWim.  If not, see <http://www.gnu.org/licenses/>.
 
 Diese Datei ist Teil von ProWim.
 
 ProWim ist Freie Software: Sie können es unter den Bedingungen
 der GNU General Public License, wie von der Free Software Foundation,
 Version 3 der Lizenz oder (nach Ihrer Option) jeder späteren
 veröffentlichten Version, weiterverbreiten und/oder modifizieren.
 
 ProWim wird in der Hoffnung, dass es nützlich sein wird, aber
 OHNE JEDE GEWÄHELEISTUNG, bereitgestellt; sogar ohne die implizite
 Gewährleistung der MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK.
 Siehe die GNU General Public License für weitere Details.
 
 Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
 Programm erhalten haben. Wenn nicht, siehe <http://www.gnu.org/licenses/>.
  */
 package org.prowim.rap.framework.i18n.configurator;
 
 import java.net.URL;
 
 import org.prowim.rap.framework.i18n.consts.impl.ConfigureException;
 
 import de.ebcot.tools.logging.Logger;
 
 
 /**
  * This class is not complete and should adapted
  * 
  * @author Maziar Khodaei
  * @version $Revision: 2469 $
  */
 public final class ResourcesConfigurator
 {
     // ~ Static fields/initializers -------------------------------------------------------------------------------------------------------------------
 
     private static final String RESOURCE_PREFIX    = "resources";                                  // prefix of resources XML files
     private static final String RESOURCE_EXTENSION = ".xml";                                       // extension of resources XML files
     // private static final String SEPERATOR = "_"; // seperator for language country customer
 
     private final static Logger LOG                = Logger.getLogger(ResourcesConfigurator.class);
 
     // prevent construction
     private ResourcesConfigurator()
     {
         // do nothing
     }
 
     /**
      * 
      * Description.
      * 
      * @param class1 Class
     * @param bundle Name of plug in bundle. Is normally define as Activator ID, e.g. "de.ebcot.rap.framework".
      */
     public static void configure(@SuppressWarnings("rawtypes") Class class1, String bundle)
     {
 
         String[] resourceFiles = new String[] { RESOURCE_PREFIX };
 
         for (int i = 0; i < resourceFiles.length; i++)
         {
             // LOG.debug("Resourcename: " + resourceFiles[i] + RESOURCE_EXTENSION);
 
             URL url = class1.getResource(resourceFiles[i] + RESOURCE_EXTENSION);
 
             if ((url != null)) // && (new File(url)).exists())
             {
                 try
                 {
                     if (i < 2)
                     {
                         ConstHierarchy.configure(class1, url, bundle);
                     }
                     else
                     {
                         ConstHierarchy.configure(class1, url, ConstHierarchy.IGNORE_MISSING_ELEMENTS, bundle);
                     }
                 }
                 catch (ConfigureException e)
                 {
                     throw new RuntimeException("Something went wrong with resource configuration:" + class1.getName() + "\n" + e.getMessage(), e);
                 }
             }
             else
             {
                 LOG.debug("Could not find file: " + url);
             }
         }
     }
 }
