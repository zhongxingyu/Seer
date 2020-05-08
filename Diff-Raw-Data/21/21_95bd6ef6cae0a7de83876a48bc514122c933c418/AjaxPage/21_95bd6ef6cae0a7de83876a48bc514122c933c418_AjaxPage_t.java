 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: AjaxPage.java,v 1.7 2008-01-15 20:48:31 jonnelson Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 package com.sun.identity.config.util;
 
 import com.sun.identity.common.ISLocaleContext;
 import com.sun.identity.config.Configurator;
 import com.sun.identity.config.DummyConfigurator;
 import com.sun.identity.shared.debug.Debug;
 import com.sun.identity.shared.locale.Locale;
 import java.io.IOException;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 import net.sf.click.Page;
 
 /**
  * 
  */
 public abstract class AjaxPage extends Page {
 
     public static final String RESPONSE_TEMPLATE = "{\"valid\":${valid}, \"body\":\"${body}\"}";
     public static final String OLD_RESPONSE_TEMPLATE = "{\"isValid\":${isValid}, \"errorMessage\":\"${errorMessage}\"}";
    
     private Configurator configurator = null;
     
     private boolean rendering = false;
     
     // localization properties
     protected ResourceBundle rb = null;
     protected static final String RB_NAME = "amConfigurator";
     
     public static Debug debug = Debug.getInstance("amConfigurator");
     
     public AjaxPage() {
     }
 
     public void onInit() {
         super.onInit();
         initializeResourceBundle();
         addModel("page", this);
     }
 
     public boolean isRendering() {
         return rendering;
     }
 
     protected Configurator getConfigurator() {
         if ( this.configurator == null ) {
             //TODO - retrieve Configuration instance from runtime environment
             //servlet context lookup?  JNDI?  Still awaiting word.  Use dummy for now:
             this.configurator = new DummyConfigurator( this );
         }
         return this.configurator;
     }
 
     protected String toString( String paramName ) {
         String value = getContext().getRequest().getParameter( paramName );
         value = ( value != null ? value.trim() : null );
         value = ("".equals(value) ? null : value );
         return value;
     }
 
     protected boolean toBoolean( String paramName ) {
         String value = toString( paramName );
         return ( value != null && value.toLowerCase().equals( "true" ) );
     }
 
     protected int toInt( String paramName ) {
         int intValue = 0;
         String value = toString( paramName );
         if ( value != null ) {
             try {
                 intValue = Integer.parseInt( value );
             } catch ( NumberFormatException e ) {}
         }
         return intValue;
     }
 
     protected void writeValid() {
         writeValid( null );
     }
     protected void writeValid( String message ) {
         String out = ( message != null ? message : "" );
         writeJsonResponse(true, out);
     }
     protected void writeInvalid( String message ) {
         String out = ( message != null ? message : "" );
         writeJsonResponse( false, out );
     }
 
     protected void writeJsonResponse( boolean valid, String responseBody ) {
         String response = RESPONSE_TEMPLATE;
         response = response.replaceFirst("\\$\\{" + "valid" +  "\\}", String.valueOf(valid));
         response = response.replaceFirst("\\$\\{" + "body" +  "\\}", responseBody);
         writeToResponse(response);
     }
 
     protected void writeToResponse( String text ) {
         try {
             getContext().getResponse().getWriter().write( text );
             this.rendering = true;
         } catch ( IOException e ) {
             throw new RuntimeException( e );
         }
     }
 
     protected void writeToResponse(boolean isValid, String errorMessage) {
         String response = OLD_RESPONSE_TEMPLATE;
         response = response.replaceFirst("\\$\\{" + "isValid" +  "\\}", String.valueOf(isValid));
         response = response.replaceFirst("\\$\\{" + "errorMessage" +  "\\}", errorMessage);
         writeToResponse(response);
     }
     
     protected void initializeResourceBundle() {
         ISLocaleContext localeContext = new ISLocaleContext();
         localeContext.setLocale(getContext().getRequest());
         try {
             rb = ResourceBundle.getBundle(RB_NAME, localeContext.getLocale());
         } catch (MissingResourceException mre) {
             // do nothing
         }
     }
     
     public String getLocalizedString(String i18nKey) {
         if (rb == null) {
             initializeResourceBundle();
         }
         
         String localizedValue = null;     
         try {
             localizedValue = Locale.getString(rb, i18nKey, debug);
         } catch (MissingResourceException mre) {
             // do nothing
         }
         return (localizedValue == null) ? i18nKey : localizedValue;
     }
 }
