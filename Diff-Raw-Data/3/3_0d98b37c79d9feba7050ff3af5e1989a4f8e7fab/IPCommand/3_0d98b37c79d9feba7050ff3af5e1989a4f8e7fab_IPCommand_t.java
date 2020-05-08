 package polly.rx.commands;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 
 import polly.rx.MyPlugin;
 import de.skuzzle.polly.sdk.Command;
 import de.skuzzle.polly.sdk.MyPolly;
 import de.skuzzle.polly.sdk.Parameter;
 import de.skuzzle.polly.sdk.Signature;
 import de.skuzzle.polly.sdk.Types;
 import de.skuzzle.polly.sdk.exceptions.CommandException;
 import de.skuzzle.polly.sdk.exceptions.DuplicatedSignatureException;
 import de.skuzzle.polly.sdk.exceptions.InsufficientRightsException;
 import de.skuzzle.polly.sdk.model.User;
 
 
 public class IPCommand extends Command {
 
     private final static String REQUEST_URL = 
         "http://rx.eggsberde.de/polly/ip.php?venad=";
     
     private final static int CLAN = 1;
     private final static int QUAD = 2;
     private final static int X = 3;
     private final static int Y = 4;
     private final static int DATE = 5;
     
     
     
     public IPCommand(MyPolly polly) throws DuplicatedSignatureException {
         super(polly, "ip");
         this.createSignature("Zeigt die Position des individuellen Portals des " +
         		"angegebenen Venads an.", 
         		MyPlugin.IP_PERMISSION, 
         		new Parameter("Venad Name", Types.STRING));
         this.setHelpText("Zeigt die Position des individuellen Portals des " +
             "angegebenen Venads an.");
         this.setRegisteredOnly();
     }
 
     
     
     @Override
     protected boolean executeOnBoth(User executer, String channel,
             Signature signature) throws CommandException, InsufficientRightsException {
         
         if (this.match(signature, 0)) {
             String venad = signature.getStringValue(0);
             BufferedReader r = null;
             try {
                 URL url = new URL(REQUEST_URL + venad);
                 URLConnection c = url.openConnection();
                 r = new BufferedReader(new InputStreamReader(c.getInputStream(), "UTF-8"));
 
                 String line = r.readLine();
                 if (line == null) {
                     throw new CommandException("Fehler beim Lesen der Serverantwort");
                 } else if (line.equals("nix")) {
                     this.reply(channel, 
                         "Keine Informationen fr den angegebenen Venad vorhanden");
                     return false;
                 }
                 
                 String[] parts = line.split(";");
                 StringBuilder b = new StringBuilder();
                 b.append("IP von ");
                 b.append(venad);
                 if (!parts[CLAN].equals("")) {
                     b.append("[");
                     b.append(parts[CLAN]);
                     b.append("]");
                 }
                 b.append(": ");
                 b.append(parts[QUAD]);
                 b.append(" ");
                 b.append(parts[X]);
                 b.append(",");
                 b.append(parts[Y]);
                 b.append(" (");
                 b.append(parts[DATE]);
                 b.append(")");
                 
                 this.reply(channel, b.toString());
             } catch (MalformedURLException e) {
                 throw new CommandException(e);
             } catch (IOException e) {
                 throw new CommandException(e);
             }
         }
         return false;
     }
 }
