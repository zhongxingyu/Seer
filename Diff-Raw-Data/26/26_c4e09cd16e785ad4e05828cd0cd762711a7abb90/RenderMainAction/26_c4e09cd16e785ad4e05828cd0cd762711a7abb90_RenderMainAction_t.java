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
 package org.bedework.webcommon;
 
 import org.bedework.appcommon.TimeView;
 import org.bedework.appcommon.client.Client;
 import org.bedework.appcommon.client.SearchParams;
import org.bedework.calfacade.exc.CalFacadeException;
 
 /** Ensure the time view data is refreshed
  *
  * @author Mike Douglass  douglm - rpi.edu
  */
 public class RenderMainAction extends BwAbstractAction {
   @Override
  public int doAction(final BwRequest request,
                      final BwActionFormBase form) throws Throwable {
    final BwModuleState mstate = request.getModule().getState();
    final Client cl = request.getClient();
    final TimeView tv = mstate.getCurTimeView();
     boolean fetch = mstate.getRefresh();
 
     SearchParams params = cl.getSearchParams();
 
     if (params == null) {
       /* Set up the search parameters */
 
       params = new SearchParams();
      final int forward = setSearchParams(request, params);
 
       if (forward != forwardSuccess) {
         return forward;
       }
 
       if (tv != null) {
         tv.refreshEvents();
       }
 
       fetch = true;
     }
 
     if (fetch) {
       /* Do the search */
      try {
        mstate.setSearchResult(cl.search(params));
        request.setRequestAttr(BwRequest.bwSearchResultName,
                               mstate.getSearchResult());
      } catch (final CalFacadeException cfe) {
        request.getErr().emit(cfe);
      }
     }
 
     if (tv != null) {
       tv.getEvents(cl, fetch);
     }
 
     mstate.setRefresh(false);
 
     return forwardSuccess;
   }
 }
 
