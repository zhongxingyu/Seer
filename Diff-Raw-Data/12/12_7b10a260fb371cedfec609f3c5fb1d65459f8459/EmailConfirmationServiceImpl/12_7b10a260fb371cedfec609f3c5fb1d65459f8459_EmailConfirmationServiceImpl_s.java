 // 
 // Copyright (c) 2003-2005, Caltha - Gajda, Krzewski, Mach, Potempski Sp.J. 
 // All rights reserved. 
 //   
 // Redistribution and use in source and binary forms, with or without modification,  
 // are permitted provided that the following conditions are met: 
 //   
 // * Redistributions of source code must retain the above copyright notice,  
 // this list of conditions and the following disclaimer. 
 // * Redistributions in binary form must reproduce the above copyright notice,  
 // this list of conditions and the following disclaimer in the documentation  
 // and/or other materials provided with the distribution. 
 // * Neither the name of the Caltha - Gajda, Krzewski, Mach, Potempski Sp.J.  
 // nor the names of its contributors may be used to endorse or promote products  
 // derived from this software without specific prior written permission. 
 // 
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"  
 // AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED  
 // WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 // IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,  
 // INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,  
 // BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
 // OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,  
 // WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)  
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE  
 // POSSIBILITY OF SUCH DAMAGE. 
 //
 
 package net.cyklotron.cms.confirmation;
 
import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Map;
 
 import javax.mail.Message;
 import javax.mail.internet.InternetAddress;
 
 import org.objectledge.ComponentInitializationError;
 import org.objectledge.coral.entity.EntityInUseException;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.mail.LedgeMessage;
 import org.objectledge.mail.MailSystem;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.Template;
 import org.objectledge.templating.TemplatingContext;
 
 import net.cyklotron.cms.documents.LinkRenderer;
 import net.cyklotron.cms.structure.NavigationNodeResource;
 
 public class EmailConfirmationServiceImpl
     implements EmailConfirmationService
 {
     /** the confirmationRequest data root node. */
     protected Resource confirmationRoot;
 
     protected CryptographyService cipherCryptographyService;
 
     private final MailSystem mailSystem;
 
     public EmailConfirmationServiceImpl(CryptographyService cipherCryptographyService,
         MailSystem mailSystem)
     {
         this.cipherCryptographyService = cipherCryptographyService;
         this.mailSystem = mailSystem;
     }
 
     // inherit doc
     public synchronized EmailConfirmationRequestResource getEmailConfirmationRequest(
         CoralSession coralSession, String cookie)
         throws ConfirmationRequestException
     {
         Resource[] res = coralSession.getStore().getResourceByPath(
             "/cms/confirmationRequests/" + cookie);
         if(res.length > 0)
         {
             return (EmailConfirmationRequestResourceImpl)res[0];
         }
         else
         {
             return null;
         }
     }
 
     // interit doc
 
     public synchronized String createEmailConfirmationRequest(CoralSession coralSession,
         String email, String data)
         throws ConfirmationRequestException
     {
         Resource root = getConfirmationRequestsRoot(coralSession);
         String cookie;
         Resource[] res;
         do
         {
             cookie = cipherCryptographyService.getRandomCookie();
             res = coralSession.getStore().getResource(root, cookie);
         }
         while(res.length > 0);
         try
         {
             EmailConfirmationRequestResource request = EmailConfirmationRequestResourceImpl
                 .createEmailConfirmationRequestResource(coralSession, cookie, root, email);
             request.setData(data);
             request.update();
         }
         catch(Exception e)
         {
             throw new ConfirmationRequestException("failed to create subscription request record",
                 e);
         }
         return cookie;
     }
 
     // inherit doc
     public synchronized void discardEmailConfirmationRequest(CoralSession coralSession,
         String cookie)
         throws ConfirmationRequestException
     {
         EmailConfirmationRequestResource r = getEmailConfirmationRequest(coralSession, cookie);
         if(r != null)
         {
             try
             {
                 coralSession.getStore().deleteResource(r);
             }
             catch(EntityInUseException e)
             {
                 throw new ConfirmationRequestException(
                     "failed to delete subscription change request", e);
             }
         }
     }
 
     /**
      * Get root node of confirmationRoot's data.
      * 
      * @param coralSession CoralSession.
      * @return the confirmationResources root.
      */
     public Resource getConfirmationRequestsRoot(CoralSession coralSession)
     {
         if(confirmationRoot == null)
         {
             Resource res[] = coralSession.getStore().getResourceByPath("/cms/confirmationRequests");
             if(res.length == 1)
             {
                 confirmationRoot = res[0];
             }
             else
             {
                 throw new ComponentInitializationError(
                     "failed to lookup /cms/confirmationRequests node");
             }
         }
         return confirmationRoot;
     }
 
     /**
      * {@inheritDoc}
      */
     public void sendConfirmationRequest(String cookie, String sender, String recipient,
         Map<String, Object> templatingContextEntries, NavigationNodeResource node,
         Template template, String medium, LinkRenderer linkRenderer, CoralSession coralSession)
         throws ProcessingException
     {
         LedgeMessage message = mailSystem.newMessage();
         TemplatingContext templatingContext = message.getContext();
         templatingContext.put("cookie", cookie);
         templatingContext.put("link", linkRenderer);
         try
         {
             if(node != null)
             {
                 templatingContext.put("node", node);
                 templatingContext.put("site", node.getSite());
                 templatingContext.put("baseLink", linkRenderer.getNodeURL(coralSession, node));
             }
             if(templatingContextEntries != null)
             {
                 for(Map.Entry<String, Object> entry : templatingContextEntries.entrySet())
                 {
                     templatingContext.put(entry.getKey(), entry.getValue());
                 }
             }
             message.setTemplate(template, medium);
             message.getMessage().setSentDate(new Date());
             message.getMessage().setFrom(new InternetAddress(sender));
             message.getMessage().setRecipient(Message.RecipientType.TO,
                 new InternetAddress(recipient));
             message.send(true);
         }
         catch(Exception e)
         {
             throw new ProcessingException("message rendering failed", e);
         }
     }
  
     @Override
     public void deleteNotConfirmedEmailConfirmationRequests(CoralSession coralSession,
         String howMany, String howOld)
         throws ConfirmationRequestException
     {
         Resource res = getConfirmationRequestsRoot(coralSession);
         Resource[] confirmations = res.getChildren();
         Date ServerDate = new Date();
        int i = 0;
         for(Resource r : confirmations)
         {
             if(i == Integer.parseInt(howMany))
             {
                 break;
             }
             else
             {
                 i++;
                 if(r instanceof EmailConfirmationRequestResourceImpl)
                 {
                     Date date = r.getCreationTime();
                 System.out.println(date);
                     Calendar c = Calendar.getInstance();
                     c.setTime(date);
                     c.add(Calendar.DATE, Integer.parseInt(howOld));
                     date = c.getTime();
                     if(date.before(ServerDate))
                     {
                         try
                         {
                             coralSession.getStore().deleteResource(r);
                         }
                         catch(IllegalArgumentException | EntityInUseException e)
                         {
                             new ConfirmationRequestException(
                                 "Failed to remove email confirmation request");
                         }
                     }
                 }
             }
         }
     }
    }
