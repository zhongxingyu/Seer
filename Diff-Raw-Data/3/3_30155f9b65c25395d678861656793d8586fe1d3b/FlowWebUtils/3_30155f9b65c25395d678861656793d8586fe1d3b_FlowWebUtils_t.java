 /*
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy
  * of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed
  * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
  * OR CONDITIONS OF ANY KIND, either express or implied. See the License for
  * the specific language governing permissions and limitations under the
  * License.
  */
 package org.amplafi.flow.web;
 
 import org.amplafi.flow.FlowState;
 import org.amplafi.flow.launcher.FlowLauncher;
 import org.apache.tapestry.IRequestCycle;
 import org.apache.tapestry.RedirectException;
 import org.apache.tapestry.IPage;
 import org.apache.tapestry.IExternalPage;
 import org.apache.tapestry.PageRedirectException;
 
 
 /**
  * Tapestry related utilities for interacting with flows.
  */
 public class FlowWebUtils {
 
     public static String getBlockName(int activity) {
         return "fc" + activity;
     }
 
     /**
      * if page is not null then activate the page. Also if the page is a IExternalPage, the {@link org.apache.tapestry.IExternalPage#activateExternalPage(Object[], org.apache.tapestry.IRequestCycle)}
      * is called with the current flow as the first element of the passed array.
      *
      * This allows the {@link org.amplafi.flow.FlowState} to be continued across {@link org.apache.tapestry.IExternalPage} calls.
      *
      * It also allows the destination page to start a new flow using the existing state as the starting point.
      *
      * @param cycle can be null
      * @param page can be the name of a page or an arbitrary uri.
      * @param flowState
      * TODO: NEED BETTER HANDLING if the flowState passed has completed/not current flowState.
      * TODO: might impact the page as well see {@link org.amplafi.flow.web.components.FlowBorder#onFinish(IRequestCycle)} for example. and {@link org.amplafi.flow.web.components.FlowEntryPoint#doEnterFlow(FlowLauncher, String, Iterable)}
      */
     public static void activatePageIfNotNull(IRequestCycle cycle, String page, FlowState flowState) {
         if ( page != null ) {
 
             if (page.startsWith("http")) {
                 redirect(cycle, page, "redirect");
 
             } else if (page.startsWith("client:")) {
                 // instructs client to close current window
                 String realPage = page.substring("client:".length());
                 redirect(cycle, realPage, "close");
 
             } else {
                 if (cycle==null) {
                     throw new PageRedirectException(page);
                 } else {
                     // TO_ANDY PROBLEM activating page this way.... for example canceling a flow doesn't cause the new page to be displayed.
                     // first noticed this with the SIGNUP verification flow not switching to home page when flow was canceled.
                     IPage appPage = cycle.getPage(page);
                     if ( appPage instanceof IExternalPage) {
                         ((IExternalPage)appPage).activateExternalPage(new Object[] {flowState}, cycle);
                     }
                     cycle.activate(appPage);
                    //HACK the above codes hasn't visible effect. Redirect doesn't happen.
                    //Investigate this later, if there will be any problems with redirects.
                    throw new PageRedirectException(appPage);
                 }
             }
         }
     }
 
     private static void redirect(IRequestCycle cycle, String page, String category) {
         if (cycle==null || !cycle.getResponseBuilder().isDynamic()) {
             throw new RedirectException(page);
         } else {
             cycle.getResponseBuilder().addStatusMessage(null, category, page);
         }
     }
 }
