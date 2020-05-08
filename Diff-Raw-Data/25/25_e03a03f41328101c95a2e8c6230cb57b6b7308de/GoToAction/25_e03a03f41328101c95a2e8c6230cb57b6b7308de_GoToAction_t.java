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
 package org.bedework.webcommon.misc;
 
import org.bedework.appcommon.client.Client;
 import org.bedework.webcommon.BwAbstractAction;
 import org.bedework.webcommon.BwActionFormBase;
 import org.bedework.webcommon.BwRequest;
 
 /**
  * Action to go to a given date and/or set a day/week/month/year view.
  * <p>Request parameter<br>
  *      "date" date to move to.
  * <p>Optional request parameter<br>
  *      "viewType" type of view we want, day, week, etc.
  *
  */
 public class GoToAction extends BwAbstractAction {
   @Override
   public int doAction(final BwRequest request,
                       final BwActionFormBase form) throws Throwable {
    final Client cl = request.getClient();

    cl.setViewMode(Client.gridViewMode);
 
     gotoDateView(request,
                  request.getDate(),
                  request.getViewTypeI());
 
     return forwardSuccess;
   }
 }
