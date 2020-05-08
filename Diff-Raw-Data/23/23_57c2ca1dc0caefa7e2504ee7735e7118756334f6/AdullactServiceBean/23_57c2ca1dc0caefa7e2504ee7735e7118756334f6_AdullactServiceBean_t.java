 /*
  * (C) Copyright ${year} Nuxeo SA (http://nuxeo.com/) and contributors.
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
  * Contributors:
  *     Benjamin JALON
  */
 
 package org.nuxeo.adullact.service;
 
 import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
 
 import org.jboss.seam.ScopeType;
 import org.jboss.seam.annotations.Factory;
 import org.jboss.seam.annotations.In;
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.annotations.Scope;
 import org.nuxeo.adullact.AdullactDossier;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.platform.query.api.PageSelection;
 import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
 import org.nuxeo.runtime.api.Framework;
 
 @Name("adullactService")
 @Scope(ScopeType.EVENT)
 public class AdullactServiceBean implements Serializable {
 
     private static final long serialVersionUID = 1L;
 
     protected AdullactService adullactService;
 
     @In
     protected NavigationContext navigationContext;
 
     public AdullactService getService() throws Exception {
         if (adullactService == null) {
             adullactService = Framework.getService(AdullactService.class);
         }
         return adullactService;
     }
 
     @Factory(value = "histogrammeDossier", scope = ScopeType.EVENT)
    public List<PageSelection<DocumentModel>> getHistogrammeDossier() throws Exception {
        List<PageSelection<DocumentModel>> items = new ArrayList<PageSelection<DocumentModel>>();

         DocumentModel document = navigationContext.getCurrentDocument();
 
        DocumentModelList docs = getService().getHistogrammeDossier(document);
        for (DocumentModel doc : docs) {
            items.add(new PageSelection<DocumentModel>(doc, false));
        }
        return items;
     }
 
     @Factory(value = "adullactDossier", scope = ScopeType.EVENT)
     public AdullactDossier getAdullactDossier() throws Exception {
         DocumentModel document = navigationContext.getCurrentDocument();
 
         return document.getAdapter(AdullactDossier.class);
     }
 
     public AdullactDossier getAdullactDossier(DocumentModel document) throws Exception {
         return document.getAdapter(AdullactDossier.class);
     }
 
 }
