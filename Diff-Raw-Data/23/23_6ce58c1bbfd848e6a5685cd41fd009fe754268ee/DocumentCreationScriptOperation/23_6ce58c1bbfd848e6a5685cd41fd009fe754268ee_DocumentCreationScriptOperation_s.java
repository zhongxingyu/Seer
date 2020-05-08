 /*******************************************************************************
  * (C) Copyright 2013 Open Wide (http://www.openwide.fr/) and others.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl-2.1.html
  * 
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  ******************************************************************************/
 package fr.openwide.nuxeo.dcs.automation;
 
 import org.nuxeo.ecm.automation.OperationContext;
 import org.nuxeo.ecm.automation.core.annotations.Context;
 import org.nuxeo.ecm.automation.core.annotations.Operation;
 import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
 import org.nuxeo.ecm.automation.core.annotations.Param;
 import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
 
 import fr.openwide.nuxeo.dcs.service.DocumentCreationScriptService;
 
 /**
  * 
  * @author mkalam-alami
  *
  */
 @Operation(id = DocumentCreationScriptOperation.ID)
 public class DocumentCreationScriptOperation {
 
     public static final String ID = "OpenWide.DocumentCreationScript";
 
     @Context
     protected OperationContext ctx;
     
     @Context
     protected DocumentCreationScriptService dcsService;
 
     @Param(name = "scriptName", required = true)
     private String scriptName;
 
    @Param(name = "context", required = false)
    private DocumentModel context;
 
     @Param(name = "overwrite", required = false)
     private boolean overwrite;
 
     @OperationMethod
     public void run() throws ClientException {
        if (context != null) {
            dcsService.runScript(ctx.getCoreSession(), scriptName, context, overwrite);
         }
         else {
             dcsService.runScript(ctx.getCoreSession(), scriptName, overwrite);
         }
     }
 
 }
