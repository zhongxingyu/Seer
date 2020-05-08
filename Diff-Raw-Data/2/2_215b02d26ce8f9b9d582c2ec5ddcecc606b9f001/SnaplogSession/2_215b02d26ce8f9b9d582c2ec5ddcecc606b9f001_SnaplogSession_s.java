 /*
  *   Copyright 2009, Maarten Billemont
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 package com.lyndir.lhunath.snaplog.webapp;
 
 import org.apache.wicket.Request;
 import org.apache.wicket.Session;
 import org.apache.wicket.protocol.http.WebSession;
 
 import com.google.common.base.Objects;
 import com.lyndir.lhunath.lib.system.logging.Logger;
 import com.lyndir.lhunath.snaplog.data.Album;
 import com.lyndir.lhunath.snaplog.data.User;
 import com.lyndir.lhunath.snaplog.webapp.tabs.Tab;
 
 
 /**
  * <h2>{@link SnaplogSession}<br>
  * <sub>[in short] (TODO).</sub></h2>
  * 
  * <p>
  * [description / usage].
  * </p>
  * 
  * <p>
  * <i>Dec 31, 2009</i>
  * </p>
  * 
  * @author lhunath
  */
 public class SnaplogSession extends WebSession {
 
     private static final Logger logger = Logger.get( SnaplogSession.class );
 
     private User                activeUser;
    private Tab                activeTab;
     private Album               activeAlbum;
 
 
     /**
      * {@inheritDoc}
      * 
      * @param request
      *            The {@link Request} that started the session.
      */
     public SnaplogSession(Request request) {
 
         super( request );
     }
 
     /**
      * @return {@link Session#get()}
      */
     public static SnaplogSession get() {
 
         return (SnaplogSession) Session.get();
     }
 
     /**
      * @return The activeUser of this {@link SnaplogSession}.
      */
     public User getActiveUser() {
 
         return activeUser;
     }
 
     /**
      * @param activeUser
      *            The activeUser of this {@link SnaplogSession}.
      */
     public void setActiveUser(User activeUser) {
 
         if (Objects.equal( getActiveUser(), activeUser ))
             return;
 
         logger.inf( "Session user identification changed from: %s, to: %s", //
                     getActiveUser(), activeUser );
 
         this.activeUser = activeUser;
     }
 
     /**
      * @return The activeTab of this {@link SnaplogSession}.
      */
     public Tab getActiveTab() {
 
         return activeTab;
     }
 
     /**
      * @param activeTab
      *            The activeTab of this {@link SnaplogSession}.
      */
     public void setActiveTab(Tab activeTab) {
 
         this.activeTab = activeTab;
     }
 
     /**
      * @return The activeAlbum of this {@link SnaplogSession}.
      */
     public Album getActiveAlbum() {
 
         return activeAlbum;
     }
 
     /**
      * @param activeAlbum
      *            The activeAlbum of this {@link SnaplogSession}.
      */
     public void setActiveAlbum(Album activeAlbum) {
 
         this.activeAlbum = activeAlbum;
     }
 }
