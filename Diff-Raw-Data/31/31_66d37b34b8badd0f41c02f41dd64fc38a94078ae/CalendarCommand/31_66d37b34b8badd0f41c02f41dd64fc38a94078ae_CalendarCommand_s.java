 package commands;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import core.DateUtils;
 
 import de.skuzzle.polly.sdk.Command;
 import de.skuzzle.polly.sdk.MyPolly;
 import de.skuzzle.polly.sdk.Signature;
 import de.skuzzle.polly.sdk.Types.DateType;
 import de.skuzzle.polly.sdk.exceptions.CommandException;
 import de.skuzzle.polly.sdk.exceptions.DuplicatedSignatureException;
 import de.skuzzle.polly.sdk.model.User;
 
 
 
 public class CalendarCommand extends Command {
     
 
 
     public CalendarCommand(MyPolly polly) throws DuplicatedSignatureException {
         super(polly, "cal");
         this.createSignature("Zeigt den Kalender fr ein angegebenes Datum an.", 
             new DateType());
         this.setHelpText("Zeigt den Kalender fr ein angegebenes Datum an.");
     }
     
     
     
     @Override
     protected boolean executeOnBoth(User executer, String channel,
         Signature signature) throws CommandException {
         
         if (this.match(signature, 0)) {
             Date d = signature.getDateValue(0);
             String[] lines = calendarString(d).split("\n");
             for (String line : lines) {
                 this.reply(channel, line);
             }
         }
         return false;
     }
 
     
     public static void main(String[] args) {
         Calendar c = Calendar.getInstance();
         
        //c.set(Calendar.MONTH, 0);
         System.out.println(calendarString(c.getTime()));
     }
     
     
     private static String calendarString(Date d) {
         StringBuilder b = new StringBuilder();
         Calendar firstDay = Calendar.getInstance();
         firstDay.setTime(d);
         firstDay.set(Calendar.DAY_OF_MONTH, 1);        
        
         Calendar lastMonth = Calendar.getInstance();
         lastMonth.setTime(d);
         lastMonth.add(Calendar.MONTH, -1);
         
         // days last month
         int ld = lastMonth.getActualMaximum(Calendar.DAY_OF_MONTH);
         lastMonth.set(Calendar.DAY_OF_MONTH, ld);
        
        int diff = firstDay.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY - 1;
        lastMonth.set(Calendar.DAY_OF_MONTH, ld - diff);
         
         SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy");
         b.append("Kalender fr: " + sdf.format(d) + "\n");
         b.append("\u0002\u001fKW | Mo Di Mi Do Fr Sa So\n");
         
         for (int j = 0; j < 6; ++j) {
             b.append(pad(lastMonth.get(Calendar.WEEK_OF_YEAR)));
             b.append(" | ");
             for (int i = 0; i < 7; ++i) {
                 String prefix = "";
                 String postfix = "";
                 if (lastMonth.before(firstDay) || 
                         lastMonth.get(Calendar.MONTH) > firstDay.get(Calendar.MONTH)) {
                     prefix = "\u000314";
                     postfix = "\u000f";
                 } else if (DateUtils.isToday(lastMonth.getTime())) {
                     prefix = "\u0002\u000304";
                     postfix = "\u000f";
                 } else if (DateUtils.isSameDay(lastMonth.getTime(), d)) {
                     prefix = "\u0002";
                     postfix = "\u000f";
                 }
                 b.append(prefix);
                 b.append(pad(lastMonth.get(Calendar.DAY_OF_MONTH)));
                 b.append(postfix);
                 b.append(" ");
                 lastMonth.add(Calendar.DAY_OF_MONTH, 1);
             }
             b.append("\n");
         }
         
         return b.toString();
     }
     
     
     private static String pad(int num) {
         if (num < 10) {
             return "0" + Integer.toString(num);
         }
         return Integer.toString(num);
     }
 }
