 // ========================================================================
 // Copyright (C) zeroth Project Team. All rights reserved.
 // GNU AFFERO GENERAL PUBLIC LICENSE Version 3, 19 November 2007
 // http://www.gnu.org/licenses/agpl-3.0.txt
 // ========================================================================
 package zeroth.framework.enterprise.infra.messaging;
 import javax.inject.Inject;
 import javax.mail.internet.MimeMultipart;
import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import com.googlecode.jeeunit.JeeunitRunner;
 import com.googlecode.jeeunit.Transactional;
 import zeroth.framework.enterprise.shared.EnterpriseException;
 /**
  * メールサービスのユニットテスト
  * <p>
  * {@link MailServiceImpl}
  * </p>
  * @author nilcy
  */
 @RunWith(JeeunitRunner.class)
 @Transactional
 @SuppressWarnings("all")
 public class MailServiceImplTest {
     @Inject
     private MailService testee;
     /**
      * メール送信のユニットテスト
      * <p>
      * {@link MailServiceImpl#send(String, String, String, Object, String)},
      * {@link MailServiceImpl#send(String, String, String, String)},
      * {@link MailServiceImpl#send(String, String, String, javax.mail.Multipart)}
      * </p>
      * @throws EnterpriseException
      */
     @Test
    @Ignore("the trustAnchors parameter must be non-empty")
     public final void testSend() throws EnterpriseException {
         this.testee.send("zeroth.framework@gmail.com", "nilcy@mac.com", "test subject",
             "object body", "text/plain");
         this.testee
             .send("zeroth.framework@gmail.com", "nilcy@mac.com", "test subject", "text body");
         this.testee.send("zeroth.framework@gmail.com", "nilcy@mac.com", "test subject",
             new MimeMultipart());
     }
 }
