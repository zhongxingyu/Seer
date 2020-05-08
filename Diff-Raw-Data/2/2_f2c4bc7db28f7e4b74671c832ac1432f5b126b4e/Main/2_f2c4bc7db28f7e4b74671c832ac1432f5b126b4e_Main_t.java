 /*******************************************************************************
  * Copyright (c) 2012, THE BOARD OF TRUSTEES OF THE LELAND STANFORD JUNIOR UNIVERSITY
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *
  *    Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  *    Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  *    Neither the name of the STANFORD UNIVERSITY nor the names of its contributors
  *    may be used to endorse or promote products derived from this software without
  *    specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
  * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
  * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  *******************************************************************************/
 package PACKAGE_NAME.NYXLET_NAME;
 
 import org.cyclades.annotations.Nyxlet;
 import org.cyclades.engine.exception.CycladesException;
 import org.cyclades.engine.nyxlet.templates.stroma.STROMANyxlet;
 
 /**
  * Please note that most if not all of your functionality will be added to this Nyxlet via ActionHandlers. The
  * ActionHandler classes will be maintained in a package below this one called "actionhandler". You will
  * most likely look to modifying *this* class when one of the following is desired:
  *
  * 1.) You need to centralize logic or resources here for all ActionHandlers to access
  * 2.) You need to initialize items global to ActionHandlers
  * 3.) Something custom etc...
  */
 @Nyxlet
 public class Main extends STROMANyxlet {
 
     public Main () throws Exception {
         super();
     }
 
     /**
      * NOTE: Your Nyxlet can be acccessed RESTfully by default, there is rarely a need to use this 
      * functionality, however it does exist in the event that it is needed.
      * 
      * Sample override for RRD ("RESTful Request Dispatch") mechanism (See the "Nyxlet" class for more info)
      * You can omit this method if the "rrd" attribute is not used in the deploy.xml resource
      *
      * This method is invoked during a rrd request to the group of Nyxlets that this Nyxlet belong to.
      * This method should return a "true" if it is deemed a match. An example algorithm to check
      * for a match:
      * -  does the request URI contain the characters "hello" in the first URI part after the Servlet name?
      *      possible matches would be "/helloworld..." or "/sayhello...."
      *
      * A Nyxlet can belong to a rrd group, and is assigned a level indicating a priority within that group.
      * (See the nyxlet_manifest.xml file)
      *
      *  rrd="testrrdgroup|/name/name/name|100"
      *
      *  This attribute states the following:
      *  1.) This Nyxlet belongs to the rrd group named "testrrdgroup"
      *  2.) The first three URI parts will be assigned to the "name" parameter, unless there exists an explicit
      *      "name" parameter defined, which will override this one. This is generally the URI_PART to PARAMETER
      *      mapping mechanism. Parameters can be defined as rrd URI parts, query parameters and STROMA parameters.
      *      STROMA parameters take the highest precedence, followed by query parameters and then URI parts.
      *  3.) This Nyxlet is assigned an order number of 100. A lower number means it will be checked for a match
      *      before any Nyxlets in the same rrd group with a higher number. Nyxlets in the same group are ordered
      *      based on this number, and are checked sequentially for a match. You can have 1 to N Nyxlets per group,
      *      of course there are performance implications tied to the number of Nyxlets per group and the cost
      *      of the matching algorithm.
      *  4.) If this is a match...and there is no "action" value specified in any of the parameter mechanisms
      *      as described above, the request will be dispatched to an ActionHandler mapped to the corresponding
      *      HTTP request method (GET,POST etc....).
      *
      * @param nyxletSession 
      * @return true if match, false otherwise
      * @throws CycladesException
      */
     /*@Override
     public boolean isRRDMatch (NyxletSession nyxletSession) throws CycladesException {
         String webServiceRequest = nyxletSession.getRequestPathInfo();
         if (webServiceRequest != null && webServiceRequest.length() > 1) {
             String[] URIParts = webServiceRequest.split("/");
             if (URIParts.length > 1 && URIParts[1].contains("hello")) return true;
         }
         return false;
     }*/
 
     /**
      * In the event that it is desired to add "initialization" code to this Nyxlet, override the
      * init method below (un-comment it) and make sure to call "super.init()" before doing anything
      * else. If necessary, make sure to also override the "destroy" method.
      */
     /*@Override
     public void init () throws CycladesException {
           final String eLabel = "Main.init: ";
           try {
               super.init();
               // Your code goes here....
           } catch (Exception e) {
               throw new CycladesException(eLabel + e);
           }
       }*/
 
     /**
      * In the event that it is desired to add "destruction" code to this Nyxlet, override the
      * destroy method below (un-comment it) and make sure to call "super.destroy()".
      */
     /*@Override
     public void destroy () throws CycladesException {
           final String eLabel = "Main.destroy: ";
           try {
               super.destroy();
               // Your code goes here....
           } catch (Exception e) {
               throw new CycladesException(eLabel + e);
           }
       }*/
 
     /**
      * This method will be called in order to evaluate the health of the Nyxlet.
      *
      * An example implementation could be as the following:
      *  - ping a database to see if it is up, and/or under too much load
      *  - if the load is deemed to heavy for the system, or if it is down, one can return "false" to flag an unhealthy Nyxlet.
      *      Folks could monitor this state via the Cyclades "health" action and take external action on the issue.
      *  - if a developer wishes to "inactivate" this nyxlet, simply call "setActive(false)" prior to returning. Please
      *      remember to include a way to "activate" this Nyxlet again. For example, when the Cyclades "health" action
      *      is called again, and the Nyxlet is deemed healthy, call "setActive(true)" prior to returning. (The Nyxlet will
      *      return the error-code "3" indicating Nyxlet inactivity when it is in the inactive state).
      *
      * This mechanism is in place to help aid in fault detection, and try to avoid failure. It is up to the Nyxlet
      * designers to provide the right algorithm for the intended behavior. The implementation below is simply an
      * example of what can be done.
      *
      * @return true if healthy, false otherwise
      * @throws CycladesException
      */
     @Override
     public boolean isHealthy () throws CycladesException {
         /***************************************************************************************/
         /** Each action handler can override the "isHealthy()" method. If desired this health **/
         /** check can be extended to include checking each ActionHandler individually         **/
         /** (calling super.isHealthy() as done below does this for you). If this is done      **/
         /** please be sure to override the "isHealthy()" method for each of your action       **/
         /** handlers to return something meaningful                                           **/
         /***************************************************************************************/
         if (super.isHealthy()) {
             // Recovery condition
             //setActive(true);
             return true;
         } else {
            //logError("Deactivating the service");
             //setActive(false);
             return false;
         }
     }
 
 }
