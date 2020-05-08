 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the
  * Common Development and Distribution License, Version 1.0 only
  * (the "License").  You may not use this file except in compliance
  * with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE
  * or http://www.escidoc.de/license.
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each
  * file and include the License file at license/ESCIDOC.LICENSE.
  * If applicable, add the following below this CDDL HEADER, with the
  * fields enclosed by brackets "[]" replaced with your own identifying
  * information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  */
 
 /*
  * Copyright 2006-2008 Fachinformationszentrum Karlsruhe Gesellschaft
  * fuer wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Foerderung der Wissenschaft e.V.  
  * All rights reserved.  Use is subject to license terms.
  */
 package de.escidoc.core.test.security.client;
 
 import org.apache.http.HttpMessage;
 import org.apache.ws.security.WSPasswordCallback;
 
 import javax.security.auth.callback.Callback;
 import javax.security.auth.callback.CallbackHandler;
 import javax.security.auth.callback.UnsupportedCallbackException;
 import java.io.IOException;
 
 /**
  * PWCallback for the Client.<p/>
  * <p/>
  * Sets the eSciDoc user handle as the password if the technical username "eSciDocUser" is provided when calling the
  * webservice.<br> This password can be changed by using the <code>setHandle</code> and reset by using the
  * <code>resetHandle</code> methods
  */
 public class PWCallback implements CallbackHandler {
 
     public static final String DEFAULT_HANDLE = "testsystemadministrator";
 
     public static final String SYSTEMADMINISTRATOR_HANDLE = "testsystemadministrator1";
 
     public static final String DEPOSITOR_HANDLE = "testdepositor";
 
     public static final String DEPOSITOR_LIB_HANDLE = "testdepositor2";
 
     public static final String DEPOSITOR_WALS_HANDLE = "testdepositor3";
 
     public static final String ADMINISTRATOR_HANDLE = "testadministrator";
 
     public static final String CONTEXT_ADMINISTRATOR_HANDLE = "testcontextadministrator";
 
     public static final String CONTENT_RELATION_MANAGER_HANDLE = "testcontentrelationmanager";
 
     public static final String MODERATOR_HANDLE = "testmoderator";
 
     public static final String MD_EDITOR_HANDLE = "testmdeditor";
 
     public static final String AUTHOR_HANDLE = "testauthor";
 
     public static final String STATISTIC_EDITOR_HANDLE = "teststatisticseditor";
 
     public static final String STATISTIC_READER_HANDLE = "teststatisticsreader";
 
     public static final String AUDIENCE_HANDLE = "testaudience";
 
     public static final String COLLABORATOR_HANDLE = "testcollaborator";
 
     public static final String TEST_HANDLE = "test";
 
     public static final String TEST_HANDLE1 = "test1";
 
     public static final String ANONYMOUS_HANDLE = "";
 
     private static String handle = DEFAULT_HANDLE;
 
     public static final String PASSWORD = "escidoc";
 
     public static final String ID_PREFIX = "escidoc:";
 
     /**
      * Gets the eSciDoc user handle.
      *
      * @return The user handle.
      */
     public static String getHandle() {
 
         return handle;
     }
 
     /**
      * Sets the eSciDoc user handle to the provided value.
      *
      * @param hd The eSciDoc user handle to use.
      */
     public static void setHandle(final String hd) {
 
         handle = hd;
     }
 
     /**
      * Resets the eSciDoc user handle to the default value specified in <code>PWCallback.DEFAULT_HANDLE</code>.
      */
     public static void resetHandle() {
 
         handle = DEFAULT_HANDLE;
     }
 
     /**
      * Sets the eSciDoc user handle to anonymous role.
      */
     public static void setAnonymousHandle() {
 
         handle = ANONYMOUS_HANDLE;
     }
 
     /**
      * The handle method of the callback handler.
      *
      * @param callbacks the WSPasswordCallback implementation
      * @throws IOException                  Exception
      * @throws UnsupportedCallbackException Exception
      * @see javax.security.auth.callback.CallbackHandler#handle (javax.security.auth.callback.Callback[])
      */
     public void handle(final Callback[] callbacks) throws IOException, UnsupportedCallbackException {
         for (int i = 0; i < callbacks.length; i++) {
             if (callbacks[i] instanceof WSPasswordCallback) {
                 WSPasswordCallback pc = (WSPasswordCallback) callbacks[i];
                 if ("eSciDocUser".equals(pc.getIdentifer())) {
                     pc.setPassword(handle);
                 }
             }
             else {
                 throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
             }
         }
     }
 
     /**
      * Adds cookie escidocCookie storing the eSciDoc user handle as the content of the cookie escidocCookie to to the
      * provided http method object.<br> The adding is skipped, if the current user handle is <code>null</code> or equals
      * to an empty <code>String</code>.
      *
      * @param method The http method object to add the cookie to.
      */
     public static void addEscidocUserHandleCokie(final HttpMessage method) {
 
        if (handle == null || "".equals(handle)) {
             return;
         }
         //Cookies werden im httpclient deaktiviert
         //method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
         method.setHeader("Cookie", "escidocCookie=" + handle);
     }
 }
