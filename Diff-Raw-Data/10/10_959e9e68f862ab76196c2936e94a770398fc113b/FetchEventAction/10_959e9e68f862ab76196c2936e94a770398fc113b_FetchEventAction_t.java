 /* ********************************************************************
     Licensed to Jasig under one or more contributor license
     agreements. See the NOTICE file distributed with this work
     for additional information regarding copyright ownership.
     Jasig licenses this file to you under the Apache License,
     Version 2.0 (the "License"); you may not use this file
     except in compliance with the License. You may obtain a
     copy of the License at:
 
     http://www.apache.org/licenses/LICENSE-2.0
 
     Unless required by applicable law or agreed to in writing,
     software distributed under the License is distributed on
     an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
     KIND, either express or implied. See the License for the
     specific language governing permissions and limitations
     under the License.
 */
 package org.bedework.webcommon.event;
 
 import org.bedework.appcommon.client.Client;
 import org.bedework.calfacade.RecurringRetrievalMode.Rmode;
 import org.bedework.calfacade.svc.EventInfo;
 import org.bedework.webcommon.Attendees;
 import org.bedework.webcommon.BwActionFormBase;
 import org.bedework.webcommon.BwRequest;
 import org.bedework.webcommon.BwSession;
 
 /** This action fetches events for editing
  *
  * <p>Forwards to:<ul>
  *      <li>"noAccess"     user not authorised.</li>
  *      <li>"notFound"     no such event.</li>
  *      <li>"continue"     continue on to update page.</li>
  * </ul>
  *
  * @author Mike Douglass   douglm rpi.edu
  */
 public class FetchEventAction extends EventActionBase {
   /* (non-Javadoc)
    * @see org.bedework.webcommon.BwAbstractAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
    */
   @Override
   public int doAction(final BwRequest request,
                       final BwActionFormBase form) throws Throwable {
     Client cl = request.getClient();
 
     if (cl.getPublicAdmin() && !form.getAuthorisedUser()) {
       return forwardNoAccess;
     }
 
     form.assignAddingEvent(false);
 
     Rmode mode;
     if (!request.present("recurrenceId")) {
       mode = Rmode.overrides;
     } else {
       mode = Rmode.expanded;
     }
     EventInfo einf = findEvent(request, mode);
 
     int fwd = refreshEvent(request, einf);
     form.setAttendees(new Attendees());
     form.setFbResponses(null);
     form.setFormattedFreeBusy(null);
     if (fwd == forwardContinue) {
       if (request.hasCopy()) {
         copyEvent(request, einf.getEvent());
 
         return forwardCopy;
       }
 
       resetEvent(request);
     }
 
     BwSession sess = request.getSess();
 
     sess.embedAddContentCalendarCollections(request);
     sess.embedUserCollections(request);
 
     sess.embedContactCollection(request, BwSession.ownersEntity);
     sess.embedCategories(request, false, BwSession.ownersEntity);
 
     sess.embedLocations(request, BwSession.ownersEntity);

    if (cl.getPublicAdmin()) {
      sess.embedContactCollection(request, BwSession.preferredEntity);
      sess.embedLocations(request, BwSession.preferredEntity);
    }
 
     return fwd;
   }
 }
