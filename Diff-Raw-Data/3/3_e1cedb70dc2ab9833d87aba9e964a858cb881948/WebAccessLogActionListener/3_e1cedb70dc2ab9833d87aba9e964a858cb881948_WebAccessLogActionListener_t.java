 /*
  * (C) Copyright 2006-2007 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
  *     anguenot
  *
  * $Id: WebAccessLogActionListener.java 28475 2008-01-04 09:49:02Z sfermigier $
  */
 
 package org.nuxeo.ecm.platform.audit.web.access;
 
 import static org.jboss.seam.ScopeType.CONVERSATION;
 
 import java.security.Principal;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jboss.seam.annotations.In;
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.annotations.Observer;
 import org.jboss.seam.annotations.Scope;
 import org.nuxeo.ecm.core.api.ClientException;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.event.CoreEventConstants;
 import org.nuxeo.ecm.core.api.event.DocumentEventCategories;
 import org.nuxeo.ecm.core.event.Event;
 import org.nuxeo.ecm.core.event.EventService;
 import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
 import org.nuxeo.ecm.platform.audit.web.access.api.AccessLogObserver;
 import org.nuxeo.ecm.platform.audit.web.access.api.WebAccessConstants;
 import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
 import org.nuxeo.ecm.webapp.helpers.EventNames;
 import org.nuxeo.runtime.api.Framework;
 
 /**
  * Access log action listener.
  *
  * <p>
  * Responsible of logging web access. Attention, this will decrease the
  * performances of the webapp.
  * </p>
  *
  * @author <a href="mailto:ja@nuxeo.com">Julien Anguenot</a>
  * @author <a href="mailto:vdutat@yahoo.fr">Vincent Dutat</a>
  *
  *
  */
 @Name("webAccessLogObserver")
 @Scope(CONVERSATION)
 public class WebAccessLogActionListener implements AccessLogObserver {
 
     private static final long serialVersionUID = 1L;
 
     private static final Log log = LogFactory.getLog(WebAccessLogActionListener.class);
 
     @In(create = true)
     protected NavigationContext navigationContext;
 
     @In(create = true)
     protected Principal currentUser;
 
     @Observer( { EventNames.USER_ALL_DOCUMENT_TYPES_SELECTION_CHANGED })
     public void log() {
         DocumentModel dm = navigationContext.getCurrentDocument();
        if (dm==null) {
            return;
        }
         DocumentEventContext ctx = new DocumentEventContext(navigationContext.getCurrentDocument().getCoreSession(),
                 currentUser, dm);
         ctx.setCategory(DocumentEventCategories.EVENT_DOCUMENT_CATEGORY);
         try {
             ctx.setProperty(CoreEventConstants.DOC_LIFE_CYCLE, dm.getCurrentLifeCycleState());
             ctx.setProperty(CoreEventConstants.SESSION_ID, dm.getSessionId());
         } catch (ClientException e1) {
             log.error("Error while getting document's lifecycle or session ID", e1);
         }
         Event event = ctx.newEvent(WebAccessConstants.EVENT_ID);
         EventService evtService = null;
 
         try {
             evtService = Framework.getService(EventService.class);
         }
         catch (Exception e) {
             log.error("Cannot find EventService", e);
         }
 
         if (evtService != null) {
             log.debug("Sending scheduled event id=" + WebAccessConstants.EVENT_ID + ", category="
                     + DocumentEventCategories.EVENT_DOCUMENT_CATEGORY);
             try {
                 evtService.fireEvent(event);
             } catch (ClientException e) {
                 log.error("Error while sending event to EventService", e);
             }
         }
     }
 }
