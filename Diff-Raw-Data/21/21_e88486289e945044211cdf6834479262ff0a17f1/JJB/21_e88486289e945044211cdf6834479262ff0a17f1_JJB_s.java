 import org.apache.commons.cli.BasicParser;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.ParseException;
 
 /** <h1>Java Jabber Bot</h1>
   * <p>This is a Java Jabber Bot based on the Smack and SQLite4java Library. This should work Crossplattform, although it was only tested on Linux.</p>
   * <p>Some features:</p>
   * <ul>
   *  <li>User register and have to be aproved by staff before they can interact with the bot</li>
   *  <li>List Tv shows the occupants like to watch (global Watchlist)</li>
   * </ul>
   * 
   * @author stylesuxx
   * @version 0.1
   */
 public class JJB{
 
   /**
    * Main Method for the JJB, parses the Command Line Parameters logs the Bot in and joins a room
    * 
    * @param args Command Line Parameters
    */
   public static void main( String[] args ){
     // Main arguments must be set via commandline
     String botJid = null;
     String botPass = null;
     String botNick = null;
     String botRoom = null;
     String botAdmin = null;
 
     // This are the optional arguments, if not set via commandline these are the default values
     String botResource = "JavaJabberBot v0.1";
     String botAuth = "DIGEST-MD5";
     int botPort = 5222;
 
     // Try to get the command Line parameters or die trying
     try {
       Options opt = new Options();
       
       opt.addOption( "h", false, "Print this Help" );
       opt.addOption( "jid", true, "Jid to use" );
       opt.addOption( "pass", true, "The password to use" );
       opt.addOption( "room", true, "The room to join" );
       opt.addOption( "nick", true, "The nick to use in the room" );
       opt.addOption( "res", true, "The bot's resource" );
       opt.addOption( "admin", true, "JID of admin" );
       opt.addOption( "enc", true, "Encription [ PLAIN | DIGEST-MD5 ]" );
       opt.addOption( "port", true, "Port to use (default 5222)" );
 
       BasicParser parser = new BasicParser();
       CommandLine cl = parser.parse( opt, args );
 
       // In case user wants help or some main arguments are missing, print the Help
       if( cl.hasOption( 'h' ) || !cl.hasOption( "admin" ) || !cl.hasOption( "jid" ) || !cl.hasOption( "pass" ) || !cl.hasOption( "room" ) || !cl.hasOption( "nick" ) ) {
 	HelpFormatter f = new HelpFormatter();
         f.printHelp( "javabot -jid <user> -pass <Password> -room <Room> -nick <Nick> -admin <JID>", opt );
 	System.exit(0);
       }
 
       // Assign arguments to variables
       else{
 	// set main variables
 	botJid = cl.getOptionValue( "jid" );
 	botPass = cl.getOptionValue( "pass" );
 	botRoom = cl.getOptionValue( "room" );
 	botNick = cl.getOptionValue( "nick" );
 	botAdmin = cl.getOptionValue( "admin" );
 
 	// Check if botJid and botAdmin are valid Jids
 	
 	// set optional variables
 	if( cl.hasOption( "res" ) ) botResource = cl.getOptionValue( "res" );
 	if( cl.hasOption( "enc" ) ) botAuth = cl.getOptionValue( "enc" );
 	if( cl.hasOption( "port" ) ) botPort = Integer.parseInt( cl.getOptionValue( "port" ) );
       }
     }catch( ParseException e ){
       System.out.println( "Seems like you missed to provide a value." ); 
       System.exit(0);
      }
 
     /* If we have all arguments we need we can finaly create und new Bot Instance, login to the Server
      * and join the room.  
      */
     Bot bot = new Bot( botJid, botPass, botResource, botAuth, botAdmin, botPort );
     bot.login();
     bot.joinRoom( botRoom, botNick );

    // Gracefully close the connection
    // bot.disconnect();
   }
 
 }
