 package erki.xpeter.parsers;
 
 import java.util.Calendar;
 import java.util.Random;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import erki.api.util.Observer;
 import erki.xpeter.Bot;
 import erki.xpeter.msg.DelayedMessage;
 import erki.xpeter.msg.TextMessage;
 
 /**
  * Parser reminding us of the next Dagstuhl-event.
  * 
  * <p>
  * It's gonna be legen -- wait for it
  * </p>
  *
  * @author Martin Jänicke <m.jaenicke@gmail.com>
  */
 public final class DagstuhlParser implements Parser, Observer<TextMessage> {
     
     private static final String[] MSG_STARTS = new String[] {
         "So, noch ", 
         "OK, ihr habt noch ",
         "Freudige Mitteilung an alle: nur noch "
         };
     
     private static final String DAYS = " Tage bis zum nächsten mal Dagstuhl! Also ";
     
     private static final String[] COMMENTS = new String[] {
         "Käseplatten leerfuttern und Wein trinken.", 
         "im Tischtennis gegen Tobi verlieren.", 
         "Bier verköstigen. Viel Bier. Sehr viel Bier.", 
         "Koffein intravenös aufnehmen.",
         "Vorträge halten und hören.",
         "neue Forschungsergebnisse, Erkenntnisse und (Achtung Geek-Witz) Blötzinn vorstellen."
         };
     private static final String ARE_YOU_SURE_BEGIN = "Bist du dir sicher das Dagstuhl in ";
     private static final String ARE_YOU_SURE_END = " Tagen ist?";
     private static final String NEW_DAGSTUHL_DATE_SET = "Danke, dass werde ich so schnell nicht wieder vergessen.";
     
     
     private Random random;
     
     private long lastReminded;
     
     private long nextDagstuhl;
     private long possibleNextDagstuhl;
 
 	private boolean askedToSetNewDagstuhlDate;
     
     @Override
     public void init(Bot bot) {
         bot.register(TextMessage.class, this);
         this.random = new Random(31337);
         this.lastReminded = 0l;
         
         this.askedToSetNewDagstuhlDate = false;
         this.possibleNextDagstuhl = 0l;
         
         final Calendar c = Calendar.getInstance();
         c.set(Calendar.DAY_OF_MONTH, 9);
         c.set(Calendar.MONTH, Calendar.JUNE);
         c.set(Calendar.YEAR, 2014);
         this.nextDagstuhl = c.getTimeInMillis();
     }
     
     @Override
     public void destroy(Bot bot) {
         bot.deregister(TextMessage.class, this);
     }
 
     @Override
     public void inform(TextMessage msg) {
         
         long now = System.currentTimeMillis();
         
         if (now - this.lastReminded >= 24 * 60 * 60 * 1000) {
             
             if (this.random.nextDouble() < .6) {
                 
                 final long days = (this.nextDagstuhl - now) / 1000 / 60 / 60 / 24;
                 StringBuilder message = new StringBuilder(MSG_STARTS[this.random.nextInt(MSG_STARTS.length)]);
                 message.append(days);
                 message.append(DAYS);
                 message.append(COMMENTS[this.random.nextInt(COMMENTS.length)]);
                 this.lastReminded = now;
                 
                 msg.respond(new DelayedMessage(message.toString(), 1000));
             }
             this.askedToSetNewDagstuhlDate =false;
         }
         if (msg.getText().matches("das nächste Mal Dagstuhl ist am \\d{1,2}\\.\\d{1,2}\\.\\d{4}")) {
         	
         	// get the date from the message
         	Pattern pattern = Pattern.compile("\\d{1,4}");
         	Matcher matcher = pattern.matcher(msg.getText());
         	
         	int i=0;
         	final Calendar c = Calendar.getInstance();
         	while(matcher.find()) {
         		String group = matcher.group();
         		if(i ==0) c.set(Calendar.DAY_OF_MONTH,new Integer(group));
        		else if( i== 1) c.set(Calendar.MONTH, new Integer(group) -1 );
         		else if (i==2) c.set(Calendar.YEAR, new Integer(group));
         		i++;
         	}
         	
         	//construct response message
         	
         	
         	this.possibleNextDagstuhl = c.getTimeInMillis();
         	final long days = (this.possibleNextDagstuhl - now) / 1000 / 60 / 60 / 24;
         	
         	StringBuilder message = new StringBuilder(ARE_YOU_SURE_BEGIN);
         	message.append(days);
         	message.append(ARE_YOU_SURE_END);
         	
         	msg.respond(new DelayedMessage(message.toString(), 1000));
         	this.askedToSetNewDagstuhlDate = true;
         	
         }
         
         if (this.askedToSetNewDagstuhlDate && msg.getText().equals("Ja, ich bin mir sicher!")) {
         	this.nextDagstuhl = this.possibleNextDagstuhl;
         	this.possibleNextDagstuhl = 0l;
         	this.askedToSetNewDagstuhlDate = false;
         	
         	msg.respond(new DelayedMessage(NEW_DAGSTUHL_DATE_SET, 1000));
         	
         }
     }
 }
