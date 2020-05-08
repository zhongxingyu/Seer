 /**
  *
  * Cryptolist - A GnuPG encrypted mailing list Copyright (C) 2013 Oliver Verlinden (http://wps-verlinden.de)
  *
  * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
  * License as published by the Free Software Foundation; either version 3 of the License, or any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
  * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with this program; if not, see
  * <http://www.gnu.org/licenses/>.
  */
 package de.wpsverlinden.cryptolist;
 
 import de.wpsverlinden.cryptolist.entities.MessageQueue;
 import de.wpsverlinden.cryptolist.pipeline.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.annotation.PostConstruct;
 import javax.ejb.Schedule;
 import javax.ejb.Singleton;
 import javax.inject.Inject;
 
 @Singleton
 public class Controller {
 
     @Inject
     Configuration config;
     @Inject
     private MailReceiver mailReceiver;
     @Inject
     private MailFilter mailFilter;
     @Inject
     private MailDecryptor mailDecryptor;
     @Inject
     private SigChecker sigChecker;
     @Inject
     private MailProcessor mailProcessor;
     @Inject
     private MailMultiplier mailMultiplier;
     @Inject
     private MailSigner mailSigner;
     @Inject
     private MailEncryptor mailEncryptor;
     @Inject
     private MailSender mailSender;
     private int pullTimer = 0;
 
     @PostConstruct
     void init() {
         linkPipeline();
     }
 
    @Schedule(hour = "*", minute = "*", second = "*/10", persistent = false)
     public void tick() {
         pullTimer += 10;
         if (pullTimer >= Integer.parseInt(config.get("pullInterval"))) {
             pullTimer = 0;
             try {
                 mailReceiver.run();
                 mailFilter.run();
                 mailDecryptor.run();
                 sigChecker.run();
                 mailProcessor.run();
                 mailMultiplier.run();
                 mailSigner.run();
                 mailEncryptor.run();
                 mailSender.run();
             } catch (Exception ex) {
                 Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 
     private void linkPipeline() {
         MessageQueue q1 = new MessageQueue();
         MessageQueue q10 = new MessageQueue();
         MessageQueue q20 = new MessageQueue();
         MessageQueue q30 = new MessageQueue();
         MessageQueue q40 = new MessageQueue();
         MessageQueue q50 = new MessageQueue();
         MessageQueue q60 = new MessageQueue();
         MessageQueue q99 = new MessageQueue();
 
         mailReceiver.setOutQueue(q1);
 
         mailFilter.setInQueue(q1);
         mailFilter.setOutQueue(q10);
         mailFilter.setErrorQueue(q99);
 
         mailDecryptor.setInQueue(q10);
         mailDecryptor.setOutQueue(q20);
         mailDecryptor.setErrorQueue(q99);
 
         sigChecker.setInQueue(q20);
         sigChecker.setOutQueue(q30);
 
         mailProcessor.setInQueue(q30);
         mailProcessor.setOutQueue(q40);
 
         mailMultiplier.setInQueue(q40);
         mailMultiplier.setOutQueue(q50);
 
         mailSigner.setInQueue(q50);
         mailSigner.setOutQueue(q60);
 
         mailEncryptor.setInQueue(q60);
         mailEncryptor.setOutQueue(q99);
 
         mailSender.setInQueue(q99);
     }
 }
