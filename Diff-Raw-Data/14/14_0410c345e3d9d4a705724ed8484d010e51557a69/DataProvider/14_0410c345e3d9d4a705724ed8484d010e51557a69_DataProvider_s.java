 /*
  * JavaWars - browser game that teaches Java
  * Copyright (C) 2008-2009  Bartlomiej N (nbartlomiej@gmail.com)
  * 
  * This file is part of JavaWars. JavaWars is free software: you can 
  * redistribute it and/or modify it under the terms of the GNU General 
  * Public License as published by the Free Software Foundation, either 
  * version 3 of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>
  * 
  */
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package javawars.client.data;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.rpc.ServiceDefTarget;
 import javawars.client.services.ServiceProvider;
 import javawars.client.services.ServiceProviderAsync;
 
 /**
  * 
  * @author bartlomiej
  */
 public class DataProvider {
 
     /**
      * Reference to only existant instance of this class.
      */
     private static DataProvider instance;
     
     /**
      * A service object; will be initialized only once, in constructor.
      */
      private final ServiceProviderAsync service;
     
     /**
      * Constructor is made private to fulfill the singleton pattern.
      */
     private DataProvider(){
         
         // Create the client proxy. Note that although you are creating the
         // service interface proper, you cast the result to the asynchronous
         // version of
         // the interface. The cast is always safe because the generated proxy
         // implements the asynchronous interface automatically.
         
         service = (ServiceProviderAsync) GWT.create(ServiceProvider.class);
         
         // Specify the URL at which our service implementation is running.
         // Note that the target URL must reside on the same domain and port from
         // which the host page was served.
         //
         ServiceDefTarget endpoint = (ServiceDefTarget) service;
 
         String moduleRelativeURL = "/JavaWars/javawars.Main/rpc/ServiceProvider";
         
         endpoint.setServiceEntryPoint(moduleRelativeURL);
                 
         
         // Downloading session constants
         getService().getSessionConstants(initializeSessionConstants);
     }
 
     /**
      * Method that grants access to the DataProvider object (singleton)
      * @return a singleton instance of a DataProvider object.
      */
     public static DataProvider getInstance() {
         if (instance!= null) {
             return instance;
         } else {
             instance = new DataProvider();
             return instance;
         }
     }
     
     private boolean sessionConstantsInitialized = false;
     
     /**
      * Object that groups parameters that will not change during one 
      * user's session.
      */
     private SessionConstants sessionConstants;
 
     AsyncCallback<SessionConstants> initializeSessionConstants = new AsyncCallback<SessionConstants>() {
         public void onFailure(Throwable exception) {
             Window.alert("Wystąpił błąd połączenia, proszę się zalogować ponownie.");
             Window.open(GWT.getHostPageBaseURL()+"login.jsp", "_self", "");
            //throw new ConnectException();
         }
         public void onSuccess(SessionConstants result) {
             sessionConstants = result;
             sessionConstantsInitialized = true;
         }
     };
 
     /**
      * A method used to acces the session constants, i.e. parameters that
      * are constant during one logged-in user's session. This is the safe verison
      * that will either immediately supply the constants from the cache or download
      * them from the web.
      * @param callback
      */
     public void getSessionConstants(AsyncCallback<SessionConstants> callback) {
         
         // if the sessionConstants aren't initialized yet 
         // we delegate the callback directly to the service provider
         if (sessionConstantsInitialized == false){
             getService().getSessionConstants(callback);
         } else {
             callback.onSuccess(sessionConstants);
         }
 }
     
     private static boolean firstTime=false;
     
     /**
      * Returns an object that grants access to the server-side methods.
      * This object is initialized only once, in the constructor of 
      * DataProvider.
      * @return
      */
     public ServiceProviderAsync getService(){
                 
         return service;
         
     }
     
 }
