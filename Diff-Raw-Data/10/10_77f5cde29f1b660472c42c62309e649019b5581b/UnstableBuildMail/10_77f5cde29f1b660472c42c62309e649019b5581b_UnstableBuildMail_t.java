 /*******************************************************************************
  *
  * Copyright (c) 2011 Oracle Corporation.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *
  *    Anton Kozak
  *
  *******************************************************************************/
 package hudson.tasks.mail.impl;
 
 import hudson.Util;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.BuildListener;
 import hudson.model.Result;
 import hudson.tasks.Messages;
 import java.util.List;
 import javax.mail.MessagingException;
 import javax.mail.internet.MimeMessage;
 
/**
 * Class used for the mail preparation if build is unstable.
 */
 public class UnstableBuildMail extends BaseBuildResultMail {
 
     public UnstableBuildMail(String recipients, boolean sendToIndividuals,
                              List<AbstractProject> upstreamProjects, String charset) {
         super(recipients, sendToIndividuals, upstreamProjects, charset);
     }
 
     /**
      * @inheritDoc
      */
     public MimeMessage getMail(AbstractBuild<?, ?> build, BuildListener listener)
         throws MessagingException, InterruptedException {
         MimeMessage msg = createEmptyMail(build, listener);
 
         String subject = Messages.MailSender_UnstableMail_Subject();
 
         AbstractBuild<?, ?> prev = build.getPreviousBuild();
         boolean still = false;
         if (prev != null) {
             if (prev.getResult() == Result.SUCCESS) {
                 subject = Messages.MailSender_UnstableMail_ToUnStable_Subject();
             } else if (prev.getResult() == Result.UNSTABLE) {
                 subject = Messages.MailSender_UnstableMail_StillUnstable_Subject();
                 still = true;
             }
         }
 
         msg.setSubject(getSubject(build, subject), getCharset());
         StringBuilder buf = new StringBuilder();
         // Link to project changes summary for "still unstable" if this or last build has changes
         if (still && !(build.getChangeSet().isEmptySet() && prev.getChangeSet().isEmptySet())) {
             appendUrl(Util.encode(build.getProject().getUrl()) + "changes", buf);
         } else {
             appendBuildUrl(build, buf);
         }
         appendFooter(buf);
         msg.setText(buf.toString(), getCharset());
 
         return msg;
     }
 }
