 /*
  * This is free and unencumbered software released into the public domain.
  */
 package com.mycompany;
 
 
 import org.apache.wicket.Component;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.attributes.AjaxCallListener;
 import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Form;
 
 
 public class Page2 extends MyBasePage
 {
     public Page2()
     {
         add( new Page2Form( "pageForm" ) );
     }
 
     public class Page2Form extends Form
     {
        private static final String PAGE2 = "com.mycompany.Page2";
 
         public Page2Form( String id )
         {
             super( id );
 
             add( new Label( "label2", "This is Page2.  If you see it, ROLE_TEST1 &/or ROLE_TEST2 is activated within " +
                 "" + "your session" ) );
             add( new SecureIndicatingAjaxButton( "page2.button1", PAGE2, GlobalUtils.BUTTON1 )
             {
                 @Override
                 protected void onSubmit( AjaxRequestTarget target, Form form )
                 {
                     info( "Button1 Pressed" );
                     target.appendJavaScript(";alert('Page2 Button1 Pressed');");
                 }
 
                 @Override
                 public void onError( AjaxRequestTarget target, Form form )
                 {
                     System.out.println( "page2.button1" );
                 }
 
                 @Override
                 protected void updateAjaxAttributes( AjaxRequestAttributes attributes )
                 {
                     super.updateAjaxAttributes( attributes );
                     AjaxCallListener ajaxCallListener = new AjaxCallListener()
                     {
                         @Override
                         public CharSequence getFailureHandler( Component component )
                         {
                             return GlobalUtils.WINDOW_LOCATION_REPLACE_LOGIN_LOGIN_HTML;
                         }
                     };
                     attributes.getAjaxCallListeners().add( ajaxCallListener );
                 }
             } );
             add( new SecureIndicatingAjaxButton( "page2.button2", PAGE2, GlobalUtils.BUTTON2 )
             {
                 @Override
                 protected void onSubmit( AjaxRequestTarget target, Form form )
                 {
                     info( "Button2 Pressed" );
                     target.appendJavaScript(";alert('Page2 Button2 Pressed');");
                 }
 
                 @Override
                 public void onError( AjaxRequestTarget target, Form form )
                 {
                     System.out.println( "page2.button2.onError" );
                 }
 
                 @Override
                 protected void updateAjaxAttributes( AjaxRequestAttributes attributes )
                 {
                     super.updateAjaxAttributes( attributes );
                     AjaxCallListener ajaxCallListener = new AjaxCallListener()
                     {
                         @Override
                         public CharSequence getFailureHandler( Component component )
                         {
                             return GlobalUtils.WINDOW_LOCATION_REPLACE_LOGIN_LOGIN_HTML;
                         }
                     };
                     attributes.getAjaxCallListeners().add( ajaxCallListener );
                 }
             } );
             add( new SecureIndicatingAjaxButton( "page2.button3", PAGE2, GlobalUtils.BUTTON3 )
             {
                 @Override
                 protected void onSubmit( AjaxRequestTarget target, Form form )
                 {
                     info( "Button3 Pressed" );
                     target.appendJavaScript(";alert('Page2 Button3 Pressed');");
                 }
 
                 @Override
                 public void onError( AjaxRequestTarget target, Form form )
                 {
                     System.out.println( "page2.button3.onError" );
                 }
 
                 @Override
                 protected void updateAjaxAttributes( AjaxRequestAttributes attributes )
                 {
                     super.updateAjaxAttributes( attributes );
                     AjaxCallListener ajaxCallListener = new AjaxCallListener()
                     {
                         @Override
                         public CharSequence getFailureHandler( Component component )
                         {
                             return GlobalUtils.WINDOW_LOCATION_REPLACE_LOGIN_LOGIN_HTML;
                         }
                     };
                     attributes.getAjaxCallListeners().add( ajaxCallListener );
                 }
             } );
         }
     }
 }
