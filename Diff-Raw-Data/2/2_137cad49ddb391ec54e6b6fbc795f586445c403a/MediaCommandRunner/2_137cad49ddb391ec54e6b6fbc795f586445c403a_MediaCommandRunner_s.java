 /*
  * Funambol is a mobile platform developed by Funambol, Inc.
  * Copyright (C) 2010 Funambol, Inc.
  *
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Affero General Public License version 3 as published by
  * the Free Software Foundation with the addition of the following permission
  * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
  * WORK IN WHICH THE COPYRIGHT IS OWNED BY FUNAMBOL, FUNAMBOL DISCLAIMS THE
  * WARRANTY OF NON INFRINGEMENT  OF THIRD PARTY RIGHTS.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program; if not, see http://www.gnu.org/licenses or write to
  * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301 USA.
  *
  * You can contact Funambol, Inc. headquarters at 643 Bair Island Road, Suite
  * 305, Redwood City, CA 94063, USA, or at email address info@funambol.com.
  *
  * The interactive user interfaces in modified source and object code versions
  * of this program must display Appropriate Legal Notices, as required under
  * Section 5 of the GNU Affero General Public License version 3.
  *
  * In accordance with Section 7(b) of the GNU Affero General Public License
  * version 3, these Appropriate Legal Notices must retain the display of the
  * "Powered by Funambol" logo. If the display of the logo is not reasonably
  * feasible for technical reasons, the Appropriate Legal Notices must display
  * the words "Powered by Funambol".
  */
 
 package com.funambol.client.test.media;
 
 import com.funambol.client.test.CommandRunner;
 import com.funambol.client.test.basic.BasicUserCommands;
 
 
 public class MediaCommandRunner extends CommandRunner implements MediaUserCommands {
 
     private static final String TAG_LOG = "MediaCommandRunner";
 
     public MediaCommandRunner(MediaRobot robot) {
         super(robot);
     }
 
     public boolean runCommand(String command, String pars) throws Throwable {
         if (ADD_PICTURE.equals(command)) {
             addMedia(BasicUserCommands.SOURCE_NAME_PICTURES, command, pars);
         } else if (ADD_VIDEO.equals(command)) {
             addMedia(BasicUserCommands.SOURCE_NAME_VIDEOS, command, pars);
         } else if (ADD_PICTURE_ON_SERVER.equals(command)) {
             addMediaOnServer(BasicUserCommands.SOURCE_NAME_PICTURES, command, pars);
         } else if (ADD_VIDEO_ON_SERVER.equals(command)) {
             addMediaOnServer(BasicUserCommands.SOURCE_NAME_VIDEOS, command, pars);
         } else if (DELETE_PICTURE.equals(command)) {
             deleteMedia(BasicUserCommands.SOURCE_NAME_PICTURES, command, pars);
         } else if (DELETE_VIDEO.equals(command)) {
             deleteMedia(BasicUserCommands.SOURCE_NAME_VIDEOS, command, pars);
         } else if (DELETE_PICTURE_ON_SERVER.equals(command)) {
             deleteMediaOnServer(BasicUserCommands.SOURCE_NAME_PICTURES, command, pars);
         } else if (DELETE_PICTURE_ON_SERVER.equals(command)) {
             deleteMediaOnServer(BasicUserCommands.SOURCE_NAME_VIDEOS, command, pars);
         } else if (DELETE_ALL_PICTURES.equals(command)) {
             deleteAllMedia(BasicUserCommands.SOURCE_NAME_PICTURES, command, pars);
         } else if (DELETE_ALL_VIDEOS.equals(command)) {
             deleteAllMedia(BasicUserCommands.SOURCE_NAME_VIDEOS, command, pars);
         } else if (DELETE_ALL_PICTURES_ON_SERVER.equals(command)) {
             deleteAllMediaOnServer(BasicUserCommands.SOURCE_NAME_PICTURES, command, pars);
         } else if (DELETE_ALL_VIDEOS_ON_SERVER.equals(command)) {
             deleteAllMediaOnServer(BasicUserCommands.SOURCE_NAME_VIDEOS, command, pars);
         } else {
             return false;
         }
         return true;
     }
 
     private MediaRobot getMediaRobot() {
         return (MediaRobot)robot;
     }
 
     private void addMedia(String type, String command, String args) throws Throwable {
         String filename = getParameter(args, 0);
         checkArgument(filename, "Missing filename in " + command);
         getMediaRobot().addMedia(type, filename);
     }
 
     private void addMediaOnServer(String type, String command, String args) throws Throwable {
         String filename = getParameter(args, 0);
         checkArgument(filename, "Missing filename in " + command);
        getMediaRobot().addMedia(type, filename);
     }
 
     private void deleteMedia(String type, String command, String args) throws Throwable {
         String filename = getParameter(args, 0);
         checkArgument(filename, "Missing filename in " + command);
         getMediaRobot().deleteMedia(type, filename);
     }
     
     private void deleteMediaOnServer(String type, String command, String args) throws Throwable {
         String filename = getParameter(args, 0);
         checkArgument(filename, "Missing filename in " + command);
         getMediaRobot().deleteMediaOnServer(type, filename);
     }
 
     private void deleteAllMedia(String type, String command, String args) throws Throwable {
         getMediaRobot().deleteAllMedia(type);
     }
 
     private void deleteAllMediaOnServer(String type, String command, String args) throws Throwable {
         getMediaRobot().deleteAllMediaOnServer(type);
     }
     
 }
 
