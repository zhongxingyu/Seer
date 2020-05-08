 /*
  * (C) Copyright 2009 Nuxeo SAS (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  */
 package org.nuxeo.ecm.platform.replication.exporter;
 
import org.nuxeo.runtime.api.Framework;
 import org.nuxeo.runtime.management.AbstractResourceFactory;
 import org.nuxeo.runtime.management.ObjectNameFactory;
 
 /**
  * Required for MBean
 *
  * @author cpriceputu@nuxeo.com
  *
  */
 public class ExporterFactory extends AbstractResourceFactory {
 
     public void registerResources() {
        DocumentaryBaseExporterService instance = null;
        try {
            instance = Framework.getService(DocumentaryBaseExporterService.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        service.registerResource("ExporterService",
                 ObjectNameFactory.formatQualifiedName("ExporterService"),
                 DocumentaryBaseExpServiceImplMBean.class, instance);
     }
 
 }
