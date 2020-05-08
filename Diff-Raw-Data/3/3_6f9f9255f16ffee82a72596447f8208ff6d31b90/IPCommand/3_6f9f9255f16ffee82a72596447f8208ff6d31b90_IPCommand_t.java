 package polly.rx.commands;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 
 import polly.rx.MSG;
 import polly.rx.MyPlugin;
 import de.skuzzle.polly.sdk.Command;
 import de.skuzzle.polly.sdk.MyPolly;
 import de.skuzzle.polly.sdk.Parameter;
 import de.skuzzle.polly.sdk.Signature;
 import de.skuzzle.polly.sdk.Types;
 import de.skuzzle.polly.sdk.User;
 import de.skuzzle.polly.sdk.exceptions.CommandException;
 import de.skuzzle.polly.sdk.exceptions.DuplicatedSignatureException;
 import de.skuzzle.polly.sdk.exceptions.InsufficientRightsException;
 
 
 public class IPCommand extends Command {
 
     private final static String REQUEST_URL = 
         "http://rx.eggsberde.de/polly/ip.php?venad="; //$NON-NLS-1$
     
     private final static int CLAN = 1;
     private final static int QUAD = 2;
     private final static int X = 3;
     private final static int Y = 4;
     private final static int DATE = 5;
     
     
     
     public IPCommand(MyPolly polly) throws DuplicatedSignatureException {
         super(polly, "ip"); //$NON-NLS-1$
         this.createSignature(MSG.ipSig0Desc, 
         		MyPlugin.IP_PERMISSION, 
         		new Parameter(MSG.ipSig0Venad, Types.STRING));
         this.setHelpText(MSG.ipHelp);
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
                 r = new BufferedReader(new InputStreamReader(c.getInputStream(), "UTF-8")); //$NON-NLS-1$
 
                 String line = r.readLine();
                 if (line == null) {
                     throw new CommandException(MSG.ipInvalidAnswer);
                 } else if (line.equals("nix")) { //$NON-NLS-1$
                     this.reply(channel, MSG.ipNoIp);
                     return false;
                 }
                //ipResultWithClan = IP von %s[%s]: %s %d,%d (%s)
                //        ipResult = IP von %s: %s %d,%d (%s)
                 String[] parts = line.split(";"); //$NON-NLS-1$
                 final String result;
                 if (!parts[CLAN].equals("")) { //$NON-NLS-1$
                     result = MSG.bind(MSG.ipResultWithClan, venad, parts[CLAN], 
                             parts[QUAD], parts[X], parts[Y], parts[DATE]);
                 } else {
                     result = MSG.bind(MSG.ipResult, venad, parts[QUAD], parts[X], 
                             parts[Y], parts[DATE]);
                 }
                     
                 
                 this.reply(channel, result);
             } catch (MalformedURLException e) {
                 throw new CommandException(e);
             } catch (IOException e) {
                 throw new CommandException(e);
             }
         }
         return false;
     }
 }
