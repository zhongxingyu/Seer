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
 package org.bedework.webcommon.category;
 
 import org.bedework.appcommon.ClientError;
 import org.bedework.calfacade.BwCategory;
 import org.bedework.webcommon.BwAbstractAction;
 import org.bedework.webcommon.BwActionFormBase;
 import org.bedework.webcommon.BwRequest;
 
 /** This action fetches a category.
  *
  * <p>Request parameters<ul>
  *      <li>catUid           The key.</li>
  * </ul>
  *.
  * <p>Forwards to:<ul>
  *      <li>"noAccess"     user not authorised.</li>
  *      <li>"notFound"     no such event.</li>
  *      <li>"continue"     continue on to update page.</li>
  * </ul>
  *
 * @author Mike Douglass   douglm  rpi.edu
  */
 public class FetchCategoryAction extends BwAbstractAction {
   /* (non-Javadoc)
    * @see org.bedework.webcommon.BwAbstractAction#doAction(org.bedework.webcommon.BwRequest, org.bedework.webcommon.BwActionFormBase)
    */
   public int doAction(BwRequest request,
                       BwActionFormBase form) throws Throwable {
     /** User requested a category from the list. Retrieve it, embed it in
      * the form so we can display the page
      */
     String catUid = request.getReqPar("catUid");
 
     if (catUid == null) {
       form.getErr().emit(ClientError.unknownCategory, "null");
       return forwardNotFound;
     }
 
     BwCategory category = request.getClient().getCategory(catUid);
 
     if (debug) {
       if (category == null) {
         logIt("No category with uid " + catUid);
       } else {
         logIt("Retrieved category " + catUid);
       }
     }
 
     form.setCategory(category);
     if (category == null) {
       form.getErr().emit(ClientError.unknownCategory, catUid);
       return forwardNotFound;
     }
 
 
     form.assignAddingCategory(false);
     return forwardContinue;
   }
 }
