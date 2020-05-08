 package commands;
 
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import de.skuzzle.polly.sdk.Command;
 import de.skuzzle.polly.sdk.MyPolly;
 import de.skuzzle.polly.sdk.Signature;
 import de.skuzzle.polly.sdk.Types;
 import de.skuzzle.polly.sdk.exceptions.CommandException;
 import de.skuzzle.polly.sdk.exceptions.DuplicatedSignatureException;
 import de.skuzzle.polly.sdk.model.User;
 
 
 public class UsersCommand extends Command {
 
     
     public UsersCommand(MyPolly polly) throws DuplicatedSignatureException {
         super(polly, "users");
         this.createSignature("Listet alle registrierten Benutzer auf");
         this.createSignature("Listet alle registrierten Benutzer auf, deren Name auf das angegebene Pattern passt", new Types.StringType());
         this.createSignature("Listet alle registrierten Benutzer auf, deren Name auf " +
         		"das angegebene Pattern passt", new Types.StringType(), new Types.BooleanType());
     }
 
     
     
     @Override
     protected boolean executeOnBoth(User executer, String channel,
         Signature signature) throws CommandException {
         
         String pattern =".*";
         boolean loggedInOnly = false;
         
         if (this.match(signature, 1)) {
             pattern = signature.getStringValue(0);
         } else if (this.match(signature, 2)) {
             pattern = signature.getStringValue(0);
             loggedInOnly = signature.getBooleanValue(1);
         }
         
         List<User> users = this.getMyPolly().users().getRegisteredUsers();
         Collections.sort(users);
         
         Pattern p = Pattern.compile(pattern.toLowerCase());
         StringBuilder b = new StringBuilder();
         Iterator<User> it = users.iterator();
         while (it.hasNext()) {
             User current = it.next();
             
             Matcher m1 = p.matcher(current.getName().toLowerCase());
            String cNick = current.getCurrentNickName().toLowerCase();
            Matcher m2 = p.matcher(cNick == null ? "" : cNick);
             
             if (m1.matches() || m2.matches()) {
                 if (loggedInOnly && !this.getMyPolly().users().isSignedOn(current)) {
                     continue;
                 }
 
                 b.append(current.getName());
                 if (this.getMyPolly().users().isSignedOn(current)) {
                     b.append(" (");
                     b.append(current.getCurrentNickName());
                     b.append(", ");
                     b.append(current.getUserLevel());
                     b.append(")");
                 }
                 
                 if (it.hasNext()) {
                     b.append(", ");
                 }
             }
         }
         
         if (users.isEmpty()) {
             b.append("Keine Benutzer registriert");
         }
         
         this.reply(channel, b.toString());
 
         return false;
     }
 }
