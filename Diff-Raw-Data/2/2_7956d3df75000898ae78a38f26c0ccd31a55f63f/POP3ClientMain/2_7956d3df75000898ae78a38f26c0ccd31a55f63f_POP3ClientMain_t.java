 package org.apache.james.mock.server.pop3;
 
 import com.google.common.collect.Ranges;
 import org.apache.james.builder.MimeMessageBuilder;
 import org.apache.james.builder.UserWithMessages;
 import org.apache.james.builder.UsersWithMessages;
 
 import java.util.Date;
 import java.util.List;
 
 import static com.google.common.collect.DiscreteDomains.integers;
 import static com.google.common.collect.Lists.newArrayList;
 
 
 /**
  * http://blog.codejava.net/nam/receive-e-mails-via-pop3-using-javamail/
  */
 public abstract class POP3ClientMain {
 
     public static void main(String[] args) {
 
         POP3ServerRunner pop3ServerRunner = null;
         try {
             List<MimeMessageBuilder> mimeMessages = newArrayList();
 
             for (int id : Ranges.closed(1, 100).asSet(integers())) {
                 mimeMessages.add(MimeMessageBuilder.newBuilder()
                         .withFrom("no-reply@example.org")
                         .withSubject("Some Subject " + id)
                         .withText("Some Text " + id)
                         .withSentDate(new Date()));
             }
 
             UsersWithMessages usersWithMessages = UsersWithMessages.newBuilder()
                     .withUser(
                             UserWithMessages.newBuilder("jdoe", "Password123")
                                     .withMessages(mimeMessages)
                     )
                     .build();
 
            pop3ServerRunner = POP3ServerRunner.createInstanceAndStart(usersWithMessages);
 
             Thread.sleep(60 * 60 * 1000);
         }
         catch(Exception e) {
             System.out.println("Message: " + e.getMessage());
             e.printStackTrace();
         }
         finally {
             if (pop3ServerRunner != null) {
                 pop3ServerRunner.stopAndDestroy();
             }
         }
 
     }
 
 }
