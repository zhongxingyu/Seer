 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.tests.embedded;
 
 import com.flexive.shared.exceptions.FxApplicationException;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Properties;
 
 /**
  * Abstract base class for a container bootstrap to be used in a "dropped in"
  * flexive application. To use this, you must subclass this class and at least
  * override the {@link #startup()} and {@link #shutdown()} methods and annotate them
  * with TestNG's BeforeSuite and AfterSuite annotations.
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public abstract class AbstractDropContainerBootstrap extends ContainerBootstrap {
     private static final Log LOG = LogFactory.getLog(AbstractDropContainerBootstrap.class);
 
     @Override
     public void startup() throws FxApplicationException {
         try {
             // deploy dropped ejbs
            deployDirectories.add(new URL(escapeUrl(getFileUrl(getFlexiveBaseDir() + "/drop"))));
             // container bootstrap
             super.startup();
         } catch (MalformedURLException e) {
             throw new IllegalArgumentException(e);
         }
     }
 
     @Override
     public void shutdown() throws FxApplicationException {
         super.shutdown();
     }
 
     @Override
     protected String getFlexiveBaseDir() {
         FileInputStream fis = null;
         try {
             final Properties buildProperties = new Properties();
             fis = new FileInputStream(new File("build.properties"));
             buildProperties.load(fis);
             return new File(buildProperties.getProperty("flexive.dir")).getCanonicalPath();
         } catch (IOException e) {
             if (fis != null) {
                 try {
                     fis.close();
                 } catch (IOException e1) {
                     // ignore
                 }
             }
             LOG.error(e.getMessage(), e);
             throw new IllegalStateException(e);
         }
     }
 
     @Override
     protected String getFlexiveDistDir() {
         return "/dist";
     }
 
 }
