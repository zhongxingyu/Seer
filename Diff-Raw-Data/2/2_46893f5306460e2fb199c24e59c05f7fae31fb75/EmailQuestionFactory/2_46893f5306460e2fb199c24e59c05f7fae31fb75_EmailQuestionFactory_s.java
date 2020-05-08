 package yao.gamelib;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Random;
 
 import javax.mail.Message;
 import javax.mail.MessagingException;
 
 public abstract class EmailQuestionFactory implements QuestionFactory {
     protected EmailStore mStore;
     final static int     NUM_ANSWERS = 4;
     private String mInbox = "inbox";
     private String mSent = "sent";
 
     public EmailQuestionFactory(EmailStore store) {
         mStore = store;
     }
 
     public EmailQuestionFactory(EmailStore store, String sent_folder, String inbox_folder) {
         mStore = store;
         mInbox = inbox_folder;
         mSent = sent_folder;
     }
 
     @Override
     public abstract Question makeQuestion();
 
     /**
      * Given the message, return a string containing the fake answer This
      * usually would be whatever field the email question represents: subject,
      * date, etc.
      * 
      * @param m
      *            the message to create a fake answer from
      * @return a string containing the fake answer
      * @throws MessagingException
      */
     protected String makeFakeAnswer( Message m )
             throws MessagingException {
         return null;
     }
 
     /**
      * Given two answers return whether they are duplicates. Default implementation calls .equals() Some email questions might want to override this to perform more robust duplication detect (such as comparing for email addresses).
      * 
      * @param answer1
      *            first answer
      * @param answer2
      *            send answer
      * @return whether the two answers are the same
      */
     protected boolean isDuplicate(String answer1, String answer2) {
         return answer1.equals(answer2);
     }
 
     protected EmailQuestion setEmailDataInbox(EmailQuestion q) {
         try {
             Message m = mStore.getNewMessageInbox();
 
             q = setEmailData(m, q);
             q.setFakeAnswers(cleanAnswers(q, makeFakeAnswers(m, getRandomRange(m, mInbox))));
             return q;
         } catch (MessagingException e) {
             e.printStackTrace();
             return null;
         }
     }
     
     protected EmailQuestion setEmailDataSent(EmailQuestion q) {
         try {
             Message m = mStore.getNewMessageSent();
 
             if (m == null)
                 return null;
 
             q = setEmailData(m, q);
             q.setFakeAnswers(cleanAnswers(q, makeFakeAnswers(m, getRandomRange(m, mSent))));
             return q;
 
         } catch (MessagingException e) {
             e.printStackTrace();
             return null;
         }
     }
     
     private EmailQuestion setEmailData( Message m, EmailQuestion q ) {
         try {
             q.setSubject(m.getSubject());
             q.setSender( m.getFrom()[0].toString() ); // TODO this only gets one sender, should we look at all the senders?
             q.setDate(m.getSentDate().toString());
         } catch (MessagingException e) {
             e.printStackTrace();
         }
         return q; 
     }
 
     private Message[] getRandomRange(Message m, String folder) throws MessagingException {
         // first, generate the range
         // from 1 to max_range_days
         final int max_range_days = 30; // the range will not be greater than 30
         final int min_range_days = 5; // the range will be at least 5
         Random randGen = new Random();
         int range_days = 0;
         do {
             range_days = randGen.nextInt( max_range_days );
         } while (range_days <= min_range_days);
 
         //        System.out.println( "Using date range " + range_days );
         // where in the range will the actual answer fall?
         int date_position = randGen.nextInt( range_days );
 
         //        System.out.println( "Using date position " + date_position );
         // calculate the range in days
         int days_after_date = range_days - date_position;
         int days_before_date = date_position;
 
         // convert those to actual dates based on the date of the message
         Calendar c = Calendar.getInstance();
         Date sent_date = m.getSentDate();
 
         c.setTime( sent_date );
         c.add( Calendar.DATE, -days_before_date );
         Date begin_range = c.getTime();
 
         c.clear();
         c.setTime( sent_date );
         c.add( Calendar.DATE, days_after_date );
         Date end_range = c.getTime();
 
         //        System.out.println( "Using date range " + begin_range.toString()
         //                + " to " + end_range.toString() );
 
         // get all messages within the range
         Message[] msgs = mStore.getMessageInRange(folder, begin_range,
                 end_range );
         // we need at least NUM_ANSWERS messages in the range that aren't the
         // actual message so we expand the range by 1 day on each end until
         // we have at least NUM_ANSWERS+1 messages
         while (msgs.length <= NUM_ANSWERS + 1) {
             c.clear();
             c.setTime( begin_range );
             c.add( Calendar.DATE, -1 );
             begin_range = c.getTime();
 
             c.clear();
             c.setTime( end_range );
             c.add( Calendar.DATE, 1 );
             end_range = c.getTime();
             msgs = mStore.getMessageInRange(folder, begin_range, end_range);
         }
         return msgs;
     }
 
     private String[] makeFakeAnswers( Message m, Message[] msgs )
             throws MessagingException {
         // using Random.nextInt() is inappropriate here as we
         // don't want any repeat numbers. So, create a list of
         // indexes and shuffle them.
         ArrayList<Integer> list = new ArrayList<Integer>( msgs.length );
         for ( int i = 0; i < msgs.length; i++ ) {
             list.add( i );
         }
         Collections.shuffle( list );
 
         ArrayList<String> answers = new ArrayList<String>( NUM_ANSWERS );
         // randomly pick messages in the range to be fake answers
         int found = 0, index = 0;
         while (found < NUM_ANSWERS && index < list.size()) {
             Message ans_m = msgs[list.get( index )];
             Date ans_date = ans_m.getSentDate();
             String ans_text = makeFakeAnswer(ans_m).trim();
             // check for duplicate answers
             boolean duplicate = false;
             for (String a : answers) {
                 duplicate = isDuplicate(a, ans_text);
             }
             if (!duplicate) {
                 //                System.out.println( "Using index: " + list.get( index ) );
                 answers.add( ans_text );
                 ++found;
             }
             ++index;
         }
         String[] answersArr = new String[NUM_ANSWERS];
         answers.toArray( answersArr );
         return answersArr;
     }
 
     /**
      * Removes null fake answers (when an answer couldnt be found) And removes fake answers that are duplicates of the actual answer.
      * 
      * @param q
      * @param answers
      * @return
      */
     private String[] cleanAnswers(EmailQuestion q, String[] answers) {
         for (int i = 0; i < answers.length; i++) {
            if (isDuplicate(answers[i], q.getAnswer()))
                 answers[i] = null; // this null will be removed in the next step
         }
         int num_not_null = 0;
         for (int i = 0; i < answers.length; i++) {
             if (answers[i] != null)
                 ++num_not_null;
         }
         String[] null_removed = new String[num_not_null];
         int added = 0;
         for (int i = 0; i < answers.length; i++) {
             if (answers[i] != null)
                 null_removed[added++] = answers[i];
         }
         return null_removed;
     }
 
 }
