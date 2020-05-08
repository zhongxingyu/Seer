 /*
  * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package beans;
 
 import beans.config.Conf;
 import controllers.routes;
 import models.Lead;
 import models.User;
 import org.apache.commons.io.FileUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import play.api.mvc.Call;
 import play.mvc.Http;
 import server.ApplicationContext;
 import server.Hmac;
 import server.MailSender;
 import beans.GsMailer.GsMailConfiguration;
 import utils.CollectionUtils;
 import utils.StringUtils;
 
 import javax.inject.Inject;
 import java.net.URLEncoder;
 
 /**
  * User: guym
  * Date: 12/14/12
  * Time: 11:20 AM
  */
 public class MailSenderImpl implements MailSender {
 
     @Inject
     private Conf conf;
 
     @Inject
     private Hmac hmac;
 
     private static Logger logger = LoggerFactory.getLogger( MailSenderImpl.class );
 
     @Override
     public void sendPoolIsEmptyMail( String stats )
     {
         try{
             logger.info( "sending email for empty pool" );
             ApplicationContext.get().getMailer().send( new GsMailConfiguration()
                     .setSubject( conf.application.name + " - Important : pool is empty" )
                     .setBodyText( "pool is empty  at " + conf.application.name + ". Pool status is " + stats )
                     .addRecipient( GsMailer.RecipientType.TO, conf.mails.poolEmpty.email , conf.mails.poolEmpty.name )
                     .setFrom( conf.smtp.user, conf.mailer.name )
                     .setReplyTo( conf.mailer )
 
             );
         }catch(RuntimeException e){
             logger.error( "error sending pool is empty email",e );
         }
     }
 
     @Override
     public void sendRegistrationMail(Lead lead) {
         logger.info( "sending registration email to lead [{}]", lead.toDebugString() );
 
         String mailContent = views.html.mail.registration.render(lead).body();
         GsMailConfiguration mConf = new GsMailConfiguration();
         mConf.addRecipient( GsMailer.RecipientType.TO,  lead.email, null)
                 .setBodyHtml( mailContent )
                 .setBodyText( mailContent )
                 .setFrom( conf.smtp.user, conf.mailer.name )
                 .setReplyTo( conf.mailer )
                 .setSubject( "Cloud Activation" );
 
         if ( conf.mails.registrationCc.isValid() ){
             mConf.addRecipient( GsMailer.RecipientType.BCC, conf.mails.registrationCc.email, conf.mails.registrationCc.name );
         }
 
         ApplicationContext.get().getMailer().send(mConf);
     }
 
     @Override
     public void sendChangelog() {
         logger.info("sending change log");
         try{
             Conf.UpgradeLogMail changeLog = conf.mails.changeLog;
             String changes = null;
             try{
                 changes = FileUtils.readFileToString( changeLog.file );
             }catch(Exception e){
                 logger.info("unable to read changelog" + e.getMessage());
             }
 
             String mailContent = views.html.mail.changeLog.render(changes).body();
 
             if (!StringUtils.isEmptyOrSpaces( changes )){
                 GsMailConfiguration mConf = new GsMailConfiguration();
                 mConf.setBodyHtml( mailContent )
                         .setBodyText( mailContent )
                         .setFrom( conf.smtp.user, conf.mailer.name )
                         .setReplyTo( conf.mailer )
                         .setSubject( conf.application.name + " was upgraded. ");
 
                boolean hasValidEmail = false;
                 if (!CollectionUtils.isEmpty(conf.mails.changeLog.addresses )){
                     for (GsMailer.Mailer address : conf.mails.changeLog.addresses) {
                         if ( address.isValid() ){
                            hasValidEmail = true;
                             mConf.addRecipient( GsMailer.RecipientType.TO, address );
                         }else{
                             logger.error("change log address [{}] is invalid. ignoring.", address);
                         }
                     }
                 }
                if ( hasValidEmail ){
                    logger.info("found changes to email. sending email");
                    ApplicationContext.get().getMailer().send(mConf);
                    FileUtils.deleteQuietly( changeLog.file ); // remove changes
                    logger.info("email sent successfully, file deleted");
                }else{
                    logger.info("no valid email address to send to. skipping email. addresses are [{}]",  conf.mails.changeLog.addresses );
                }
 
             }else{
                 logger.info("changelog is empty, skipping email");
             }
         }catch(Exception e){
             logger.error("unable to send changelog",e);
         }
 
     }
 
     @Override
     public void resetPasswordMail( User user ){
 
         logger.info( "user {} requested password reset", user.toDebugString() );
 //        String link = routes.WidgetAdmin.getAllServers();
 
 
         String encode;
         String sign = hmac.sign( user.getEmail(), user.getId(), user.getPassword() );
         try{
             encode = URLEncoder.encode( sign, "UTF-8" );
         }catch(Exception e){
             throw new RuntimeException( String.format( "unable to url encode hmac [%s]", sign ) , e );
         }
         Call call = routes.WidgetAdmin.resetPasswordAction( encode, user.getId() );
 
         // guy - this is not nice. we have to have the request for the absoluteURL.
         // we should find a way to fake the request, just in case we have a job that sends emails with links.
         String link = call.absoluteURL( Http.Context.current().request() );
 
         String mailContent = views.html.mail.resetPassword.render( user, link ).body();
         GsMailConfiguration mConf = new GsMailConfiguration();
         mConf.addRecipient( GsMailer.RecipientType.TO, user.getEmail(), user.getFullName() )
                 .setBodyHtml( mailContent )
                 .setBodyText( mailContent )
                 .setFrom( conf.smtp.user, conf.mailer.name )
                 .setReplyTo( conf.mailer )
                 .setSubject( "Reset Password" );
 
         ApplicationContext.get().getMailer().send(mConf);
     }
 
     public void setConf( Conf conf )
     {
         this.conf = conf;
     }
 
     public void setHmac( Hmac hmac )
     {
         this.hmac = hmac;
     }
 }
