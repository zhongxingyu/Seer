 /*
  * Sign-up plug-in for jWebSocket <http://jwebsocket.org/>.
  *
  * Copyright (C) 2012  Jamie Forth <jamie.forth@eecs.qmul.ac.uk>, 
  * SerenA <http://www.serena.ac.uk>.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.serena.jws.plugins;
 
 // FIXME: Move to a separate shared library so it can be included both client and server-side.
 
 public final class SignUpPlugInConstants {
 
     /**
      * Sign-up plug-in namespace
      */
    public final static String NS_SIGNUP_PLUGIN = "uk.ac.serena.jws.plugins.signup";
 
     /**
      * Message types
      */
     public static final String CREATE_USER_ACCOUNT = "CreateUserAccount";
 
     /**
      * Message properties
      */
     public static final String USER_NAME = "username";
     public static final String PASSWORD = "password";
 
     /**
      * Errors
      */
     public static final int ERROR_NO_USERNAME_NOT_PROVIDED = 1;
     public static final String ERROR_MSG_USERNAME_NOT_PROVIDED = "Username not provided.";
     public static final int ERROR_NO_PASSWORD_NOT_PROVIDED = 2;
     public static final String ERROR_MSG_PASSWORD_NOT_PROVIDED = "Password not provided.";
     public static final int ERROR_NO_USERNAME_EXISTS = 3;
     public static final String ERROR_MSG_USERNAME_EXISTS = "Username already exists.";
 
 }
