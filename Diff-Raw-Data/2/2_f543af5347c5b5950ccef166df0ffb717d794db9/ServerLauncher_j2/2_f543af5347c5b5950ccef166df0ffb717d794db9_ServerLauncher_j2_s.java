 /*
  * ServerLauncher_j2.java
  *
  * Originally written by: 
  * Czahrien <czahrien@gmail.com>
  * Edited by: 
  * mbaxter
  *
  * Version 6.0
  *
  * czahrien's Licensing:
  * I am not going to be a stickler about licensing and all that jazz. Just be 
  * sure to give credit to me where credit is due and we can all get along like
  * grown-ups. Okay?
  * 
  * Notes by mbaxter:
  * This version has extra features designed for the joe.to community.
  * Use them as you like. We like them.
  *
  */
 
 import java.io.*;
 import java.util.*;
 import java.util.regex.*;
 import java.text.SimpleDateFormat;
 
 public class ServerLauncher_j2 extends Thread {
 
     public static final int P_SUPERADMIN = 3;
     public static final int P_ADMIN = 2;
     public static final int P_TRUSTED = 1;
     public static final int P_ALLOWED = 0;
 
     public static void main( String[] args ) {
         try {
             // Create the ServerLauncher_j2
             ServerLauncher_j2 o = new ServerLauncher_j2();
             o.start();
             o.join();
         } catch( Exception e ) {
             e.printStackTrace();
         }
     }
 
     public ServerLauncher_j2() {
         super();
         startTime = new Date();
         lastSave = startTime;
         try {
             FileInputStream fis = new FileInputStream( "ServerLauncher.properties" );
             Properties p = new Properties();
             p.load( fis );
             String value = p.getProperty( "allowhelp" );
             if( value != null && value.equalsIgnoreCase( "false" ) ) {
                 allowHelp = false;
             } else {
                 allowHelp = true;
             }
             value = p.getProperty( "tpfortrusted" );
             if( value != null && value.equalsIgnoreCase( "false" ) ) {
                 trustedTP = false;
             } else {
                 trustedTP = true;
             }
             value = p.getProperty( "fun" );
             if( value != null && value.equalsIgnoreCase( "true" ) ) {
                 isFun = true;
                 System.out.println( "Fun mode is enabled." );
             } else {
                 isFun = false;
                 System.out.println( "Fun mode is disabled." );
             }
             value = p.getProperty( "itemwhitelist" );
             if( value != null && value.equalsIgnoreCase( "true" ) ) {
                 isItemWhiteList = true;
             } else {
                 isItemWhiteList = false;
             }
             value = p.getProperty( "playerwhitelist" );
             if( value != null && value.equalsIgnoreCase( "true" ) ) {
                 isPlayerWhiteList = true;
             } else {
                 isPlayerWhiteList = false;
             }
             value = p.getProperty( "playercap" );
             if( value != null ) {
                 try {
                     myPlayerCap = Integer.parseInt( value );
                     System.out.println( "Setting player cap to " + myPlayerCap + "." );
                 } catch( NumberFormatException e ) {
                     System.err.println( "ERROR: Invalid number for player cap." );
                     myPlayerCap = -1;
                 }
             } else {
                 myPlayerCap = -1;
             }
             value = p.getProperty( "ram" );
             if( value != null ) {
                 try {
                     myRAMUse = Integer.parseInt( value );
                     System.out.println( "Setting ram use to " + value + "mb." ); 
                 } catch( NumberFormatException e ) {
                     System.err.println( "ERROR: Invalid amount of ram." );
                     myRAMUse = 1024;
                 }
             } else {
                 myRAMUse = 1024;
             }
             value = p.getProperty( "autosave" );
             if( value != null && value.equalsIgnoreCase( "false" ) ) {
                 autoSave = false;
             } else {
                 autoSave = true;
             }
         } catch( IOException e ) {
             if( e instanceof FileNotFoundException ) {
                 System.err.println( "ERROR: Cannot open ServerLauncher.properties" );
                 Properties p = new Properties();
                 p.setProperty( "fun", "false" );
                 p.setProperty( "itemwhitelist", "true" );
                 p.setProperty( "playerwhitelist", "false" );
                 p.setProperty( "playercap", "-1" );
                 p.setProperty( "ram", "1024" );
                 p.setProperty( "autosave", "true" );
                 p.setProperty( "allowHelp","true" );
                 p.setProperty( "tpfortrusted", "true" );
                 try { 
                     p.store( new FileOutputStream( "ServerLauncher.properties" ), "Properties for ServerLauncher_j2" );
                 } catch( IOException f ) {}
             }
 
             else {
                 System.err.println( "ERROR: ServerLauncher.properties is of an invalid format." );
             }
             Properties p = new Properties();
 
             isFun = false;
             isPlayerWhiteList = true;
             isItemWhiteList = false;
             myPlayerCap = -1;
             myRAMUse = 1024;
             autoSave = true;
             allowHelp = true;
             trustedTP = true;
         }
         // Start the server process
         try {
             myProcess = Runtime.getRuntime().exec( "java -Xms" + myRAMUse + "m -Xmx" + myRAMUse + "m -jar minecraft_server.jar nogui" );
         } catch( IOException e ) {
             e.printStackTrace();
             System.err.println( "Could not run the server." );
             System.exit(1);
         }
         // Reader of stderr for the server
         myInReader = new BufferedReader( new InputStreamReader( myProcess.getInputStream() ) );
         // Reader of stdout for the server
         myErrReader = new BufferedReader( new InputStreamReader( myProcess.getErrorStream() ) );
         // Writer for stdin of the server
         myWriter = new PrintWriter( new OutputStreamWriter( myProcess.getOutputStream() ), true );        
 
         loadLists();
 
         keep_going = true;
         myPlayerCount = 0;
         connectedPlayers = "";
 
         ban_command = "ban";
         if( isPlayerWhiteList ) {
             ban_command = "allow";
         }
 
         list_command = "blacklist";
         if( isItemWhiteList ) {
             list_command = "whitelist";
         }
     }
 
     public void run() {
         try {
             String line = "";
             // Patterns for regex pattern recognition. They are thorough so
             // sneaky players cannot break it by saying [INFO] and all that jazz.
             Pattern[] patterns = new Pattern[6];
             String colors = "(?:[^\\p{Alnum}^\\p{Digit}^\\055_^\\.]\\p{Alnum}){0,1}";
             String beginning = "^\\p{Digit}{4}-\\p{Digit}{2}-\\p{Digit}{2} \\p{Digit}{2}:\\p{Digit}{2}:\\p{Digit}{2} \\[INFO\\] ";
             patterns[0] = Pattern.compile( beginning +
                     "[\\[<]" + colors + "([\\055\\w\\.]*)" + colors + "[>\\]] #([\\p{Alpha}\\p{Digit}\\p{Punct} ]*)" );
             patterns[1] = Pattern.compile( beginning +
                     "(.*) \\[/(\\p{Digit}{1,3}.\\p{Digit}{1,3}.\\p{Digit}{1,3}.\\p{Digit}{1,3}):\\p{Digit}*\\] logged in" );
             patterns[2] = Pattern.compile( beginning +
                     "Connected players: ([\\055\\w\\., ]*)" );
             patterns[3] = Pattern.compile( beginning +
                     "Giving [\\w\\055\\.]* some \\p{Digit}{1,4}" );
             patterns[4] = Pattern.compile( beginning + "Opping ([\\p{Alnum}_\\-\\.]*)$" );
             patterns[5] = Pattern.compile( beginning + "De-opping ([\\p{Alnum}_\\-\\.]*)$" );
 
 
             BufferedReader input = new BufferedReader( new InputStreamReader( System.in ) );
             while( keep_going ) {
                 Date d = new Date();
                 long offset = d.getTime() - lastSave.getTime();
                 // autosave every ten minutes
                 if( offset / 1000 / 60 >= 10 ) {
                     lastSave = new Date();
                     saveLists();
                 }
 
                 line = "";
                 // Loop while the server is still running.
                 while( input.ready() && !line.equals("stop") ) {
                     line = input.readLine();
                     myWriter.println( line );
                 }
 
                 if( line.equals( "stop" ) ) {
                     keep_going = false;
                     break;
                 }
 
                 // This loop handles stdout from the server.
                 while( myInReader.ready() ) {
                     line = myInReader.readLine();
                     System.out.println( "O: " + line );
                     // Currently the wrapper is using the server's "Player count: " messages to keep count of the players.
                     // This is a temporary measure until keeping track of the players in-game is perfect.
                     if( line.startsWith( "Player count: " ) ) {
                         myWriter.println( "list" );
                         try {
                             myPlayerCount = Integer.parseInt( line.split( ": " )[1] );
                         } catch( NumberFormatException e ) {
                             myPlayerCount = -1;
                         }
                         String cap = "";
                         if( myPlayerCap > 0 ) {
                             cap += "/" + myPlayerCap;
                         }
                         if( myPlayerCap >= myPlayerCount ) {
                             playerPrint( "There are currently " + myPlayerCount + cap + " players on the server." );
                         }
                     }
                 }
 
                 // This loop handles stderr from the server.
                 while( myErrReader.ready() ) {
                     line = myErrReader.readLine();
                     if( myWriter != null ) {
                         // Create a pattern matcher for the line read in.
                         Matcher[] m = new Matcher[patterns.length];
                         for( int i = 0; i < patterns.length; i++ ) {
                             m[i] = patterns[i].matcher( line );
                         }
 
                         // m[3] prevents give message spam.
                         // TODO: When give fails because of an invalid item-id, spam happens. Fix.
                         if( !m[3].matches() ) {
                             System.out.println( "E: " + line );
                         }
                         // m[0] matches chat messages beginning with a pound sign.
                         if( m[0].matches() ) {
                             String name = m[0].group(1);
                             String[] parts = m[0].group(2).split( "\\s+" );
                             // #summon <item-id> <count> / #summon <kit>
                             if( parts.length >= 2 && ( parts[0].equalsIgnoreCase( "summon" ) || parts[0].equalsIgnoreCase( "s" ) ) 
                                     && ( isFun || isTrusted(name) ) ) {
                                 int times = 1;
                                 // If there are >= 3 parts there may be an item count count.
                                 if( parts.length >= 3 ) {
                                     try { 
                                         times = Integer.parseInt( parts[2] );
                                     } catch( Exception e ) {
                                     times = 0;
                                     }
                                     // A reasonable cap is 64.
                                     // A reasonable mbaxter cap is 256
                                     // A reasonable cap is a fez
                                     if( times > 256 ) {
                                         times = 256;
                                     }
                                 }
 
                                 try {
                                     int id = Integer.parseInt( parts[1] );
                                     parts[1] = "" + id;
                                     // Admins are not affected by blacklisting.
                                     // Trusted players are if fun mode is off.
                                     // Added in here admin blacklist line
                                     if( !isAdmin(name) &&  
                                             !( isTrusted(name) && isFun )  &&	
                                             !( !isItemWhiteList ^ blacklist.contains( parts[1] ) ) ||
                                              ( adminblacklist.contains ( parts[1] ) ) ) {
                                         System.out.println( name + " tried to summon something blacklisted." );
                                     } else {
                                         System.out.println( "Summoning " + times + " of " + id + " for " + name );
                                         give (name, id, times);
                                     }
                                 } catch( NumberFormatException e ) {
                                     ArrayList<Integer> ids = kits.get( parts[1].toLowerCase() );
                                     if( ids != null ) {
                                         for( Integer i : ids ) {
                                             give( name, i , 1);
                                         }
                                     }
                                 }
                                 // #give <player> <item> <count> // #give <player> <kit>
                             } else if( parts[0].equalsIgnoreCase( "give" ) 
                                     && parts.length >= 3 && isAdmin(name) ) {
                                 int times = 1;
                                 // If there are >= 4 parts there may be an item count count.
                                 if( parts.length >= 4 ) {
                                     try { 
                                         times = Integer.parseInt( parts[3] );
                                     } catch( Exception e ) {
                                     }
                                     // A reasonable cap is 64.
                                     if( times > 256 ) {
                                         times = 256;
                                     }
                                 }
 
                                 try {
                                     int id = Integer.parseInt( parts[2] );
                                     give( parts[1], id, times );
                                 } catch( NumberFormatException e ) {
                                     //give( parts[1], parts[2] , 1);
                                 }
                                 // #stop
                             } else if( parts[0].equalsIgnoreCase( "points" ) ) {
                                 if( parts.length == 1 ) {
 
                                     Integer i = points.get( name.toLowerCase() );
                                     if( i == null ) {
                                         i = 0;
                                     }
                                     playerPrint( name + " has " + i + " points." );
                                 } else if( isAdmin(name) ) {
                                     Integer i = points.get( parts[1].toLowerCase() );
                                     if( i == null ) {
                                         i = 0;
                                     }
                                     if( parts.length > 2 ) {
                                         try {
                                             points.put( parts[1].toLowerCase(), i + Integer.parseInt( parts[2] ) );
                                             i = points.get( parts[1].toLowerCase() );
                                             playerPrint( parts[1] + " now has " + i + " points." );
                                         } catch( NumberFormatException e ) {}
                                     } else {
                                         playerPrint( parts[1] + " has " + i + " points." );
                                     }
 
                                 }
                             } else if( parts[0].equalsIgnoreCase( "stop" ) && isSuperAdmin(name) ) {
                                 keep_going = false;
                                 myWriter.println( "Server is going down!" );
                                 myWriter.println( "stop" );
                                 // #time
                             } else if( parts[0].equalsIgnoreCase( "time" ) ) {
                                 time();
                                 // #uptime
                             } else if( parts[0].equalsIgnoreCase( "uptime" ) ) {
                                 uptime();
                                 // #motd
                             } else if( parts[0].equalsIgnoreCase( "motd" ) ) {
                                 motd();
                                 // #rules
                             } else if( parts[0].equalsIgnoreCase( "rules" ) ) {
                                 rules();
                                 // #help
                             } else if( parts[0].equalsIgnoreCase( "help" ) ) {
                                 if( allowHelp || isAdmin( name ) ) {
                                     help( name ); 
                                 }
                                 // #reload
                             } else if ( parts[0].equalsIgnoreCase( "reload" ) && isAdmin(name) ) {
                                 loadLists();
                                 // #save
                             } else if( parts[0].equalsIgnoreCase( "save" ) && isAdmin(name) ) {
                                 saveLists();
                                 myWriter.println( "save-all" );
                                 // #list
                             } else if ( parts[0].equalsIgnoreCase( "list" ) ) {
                                 playerList();
                                 // #playercap <new cap>
                             } else if( parts.length == 2 && parts[0].equalsIgnoreCase( "playercap" ) && isAdmin( name ) ) {
                                 try {
                                     int n = Integer.parseInt( parts[1] ); 
                                     setPlayerCap( n );
                                 } catch( NumberFormatException e ) {}
                                 // #kick <name>
                             } else if ( parts.length == 2 && parts[0].equalsIgnoreCase( "kick" ) && 
                                     isAdmin(name) && !isAdmin(parts[1]) )  {
                                 // TODO: rewrite kick to be kick( kicker, kicked )
                                 kick(parts[1]);
                                 // #ban <name>
                             } else if ( parts.length == 2 && parts[0].equalsIgnoreCase( ban_command ) && 
                                     isAdmin(name) ) {
                                 ban(name, parts[1]);
                                 // #unban <name>
                             } else if ( parts.length == 2 && parts[0].equalsIgnoreCase( "un" + ban_command ) && 
                                     isAdmin(name) ) {
                                 unban( parts[1] );
                                 // #ipban <name or IP>
                             } else if( parts.length == 2 && parts[0].equalsIgnoreCase( "ipban" ) && isAdmin( name ) ) {
                                 if( ipban( parts[1] ) ) {
                                     playerPrint( parts[1] + " was IP Banned." ); 
                                 }
                             } else if( parts.length >= 1 && parts[0].equalsIgnoreCase( "iplist" ) && isAdmin( name ) ) {
                                 boolean online = parts.length == 2 && parts[1].equalsIgnoreCase( "online" );
                                 ipList( online );
                                 // #admin <name>
                             } else if ( parts.length == 2 && parts[0].equalsIgnoreCase( "admin" ) && 
                                     isAdmin(name) ) {
                                 admin( name, parts[1] );
                                 // #unadmin <name>
                             } else if ( parts.length == 2 && parts[0].equalsIgnoreCase( "unadmin" ) && isAdmin(name) ) {
                                 unadmin( name, parts[1] );
                                 // #blacklist <id>
                             } else if ( parts.length >= 1 && parts[0].equalsIgnoreCase( list_command ) ) {  
                                 if( isAdmin(name) && parts.length > 1 ) {
                                     blacklist.add( parts[1].toLowerCase() );
                                 } else {
                                     showList();
                                 }
                                 // #unblacklist <id>
                             } else if ( parts.length == 2 && parts[0].equalsIgnoreCase( "un" + list_command ) && 
                                     isAdmin(name) ) {
                                 blacklist.remove( parts[1].toLowerCase() );
                                 // #trust <id>
                             } else if ( parts.length == 2 && parts[0].equalsIgnoreCase( "trust" ) && 
                                     isAdmin(name) ) {
                                 trust( parts[1] );
                                 // #untrust <id>
                             } else if ( parts.length == 2 && parts[0].equalsIgnoreCase( "untrust" ) && 
                                     isAdmin(name) ) {
                                 untrust( parts[1] );
                             } else if ( parts.length == 2 && parts[0].equalsIgnoreCase( "op" ) && isSuperAdmin(name) ) {
                                 op( parts[1] );
                             } else if ( parts.length == 2 && parts[0].equalsIgnoreCase( "deop" ) && isSuperAdmin(name) ) {
                                 deop( parts[1] );
                                 // #fun
                             } else if( parts[0].equalsIgnoreCase( "fun" ) && isAdmin(name) ) {
                                 toggleFun();
                             } else if( parts[0].equalsIgnoreCase( "protect" ) && ( isTrusted(name) || isAdmin(name) || isSuperAdmin(name) ) ) {
                                 if(!protectedtrusted.contains(name)){protectedtrusted.add( name );}
                             } else if( parts[0].equalsIgnoreCase( "unprotect" ) && ( isTrusted(name) || isAdmin(name) || isSuperAdmin(name) ) ) {
                                 if(protectedtrusted.contains(name)){protectedtrusted.remove( name );}   
                             } else if( parts.length == 2 && parts[0].equalsIgnoreCase( "tp" ) ) {
                                 teleport( name , parts[1] );                            
                             } else if( parts.length == 2 && parts[0].equalsIgnoreCase( "bring" )  ) {
                                 bring( parts[1] , name );
                             } else {
                                 //Invalid command.
                             }
 
                         }
                         else if( m[1].matches() ) {
                             welcome( m[1].group(1), m[1].group(2) );
                         }
                         else if( m[2].matches() ) {
                             connectedPlayers = m[2].group(1);
                         }  
                     }
                 }
                 sleep( 100 );
             }
             myInReader.close();
             myErrReader.close();
             myWriter.close();
             saveLists();
         } catch( Exception e ) {
             System.err.println( "ERROR: Exception. Stopping server." );
             myWriter.println( "stop" );
             e.printStackTrace();
             keep_going = false;
         }
     }
 
 
 
     public void welcome( String name, String ip ) {
         ipassoc.put( name.toLowerCase(), ip );
         if( !isAllowed(name) ) {
             System.out.println( name + " is not allowed - kicking." );
             kick( name );
         }
 
         if( !isAdmin(name) && !isTrusted(name) ) {
             if( myPlayerCap > 0 && myPlayerCount > myPlayerCap ) {
                 kick( name );
                 playerPrint( "Player cap reached, kicking " + name + "." );
 //            } else {
 //                playerPrint( "Welcome, " + name + "!" );
             }
 //        } else {
 //            if( isAdmin(name) ) {
 //                playerPrint( "Quick! Everybody hide! " + name + " is an admin!" );
 //            } else if( isTrusted( name ) ) {
 //                playerPrint( name + " is a pretty trustworthy guy." );
 //            }
         }
         //Just commenting out the stuff for now, so you can re-enable it
         motd();
 
     }
 
     public void toggleFun() {
         isFun = !isFun;
         if( isFun ) {
             playerPrint( "Fun mode has been enabled! :D" );
         } else {
             playerPrint( "Fun mode has been disabled... :'(" );
         }
     }
 
     public boolean ipban( String nameOrIP ) {
         nameOrIP = nameOrIP.toLowerCase();
         boolean m = Pattern.matches( "(?:[\\*\\p{Digit}]{1,3}\\.){3}[\\*\\p{Digit}]{1,3}", nameOrIP );
         String ip = ipassoc.get( nameOrIP );
         if( ip != null && !isAdmin( nameOrIP ) ) {
             playerPrint( ip + " belongs to " + nameOrIP + "!" );
             myWriter.println( "ban-ip " + ip );
             kick( nameOrIP );
         } else if( m ) {
             myWriter.println( "ban-ip " + nameOrIP );
         } else {
             return false;
         }
         return true;
     }
 
     public boolean ban( String banner, String bannee ) {
         if( !isAdmin(bannee) || isSuperAdmin(banner)  )
         {
             if( !isPlayerWhiteList ) {
                 myWriter.println( "ban " + bannee );
                 kick( bannee );
             } else {
                 permissions.put( bannee.toLowerCase(), P_ALLOWED );
             }
             char n = 0;
             if( ban_command.charAt( ban_command.length() - 1 ) == 'n' ) {
                 n = 'n';
             }
             playerPrint( bannee + " has been " + ban_command + n + "ed." );
             return true;
         } else {
             return false;
         }
     }
 
     public boolean unban( String bannee ) {
         Integer b = permissions.remove( bannee.toLowerCase() );
         if( b != null ) {
             char n = 0;
             if( ban_command.charAt( ban_command.length() - 1 ) == 'n' ) {
                 n = 'n';
             }
 
             playerPrint( bannee + " has been un" + ban_command + n + "ed." );
             if( isPlayerWhiteList ) {
                 kick( bannee );
             } else {
                 myWriter.println( "unban " + bannee );
             }
         }
         return b != null;
     }
 
     public void admin( String admin, String name ) {
         if( isSuperAdmin(admin) ) {
             if( !isAdmin( name ) ) {
                 playerPrint( name + " is now an admin!" );
                 permissions.put( name.toLowerCase(), P_ADMIN );
             }
         }
     }
 
     public void unadmin( String admin, String name ) {
         if( isSuperAdmin(admin) ) {
             if( isAdmin( name ) ) {
                 playerPrint( name + " is no longer an admin!" );
                 permissions.remove( name.toLowerCase() );
                 myWriter.println( "deop " + name );
             }
         }
     }
     public void trust( String name ) {
         if( !isAdmin( name ) ) {
             playerPrint( name + " is now trusted!" );
             permissions.put( name.toLowerCase(), P_TRUSTED ); 
         }
     }
 
     public void untrust( String name ) {
         if( !isAdmin( name ) && isTrusted(name) ) {
             if(protectedtrusted.contains(name)){protectedtrusted.remove( name );}
             if( permissions.remove( name.toLowerCase() ) != null ) {
                 playerPrint( name + " is no longer trusted!" );
             }
         }
     }
     
     public void op( String name) {
         myWriter.println( "op " + name);
     }
     
     public void deop( String name) {
         myWriter.println( "deop " + name);
     }
     
     public void kick( String victim ) {
         myWriter.println( "kick " + victim );
     }
 
 
 
     public void give( String name, int id, int count ) {
         int times = count;
         while (times >0)
         {
         if (times >= 64) {
             myWriter.println( "give " + name + " " + id + " " + 64 );
             times -= 64;
             } else {
             myWriter.println( "give " + name + " " + id + " " + times );
             times = 0;
             }
         }
     }
     
     public void teleport (String playerwho, String playerto) {
        if( isFun || ( isTrusted(name) && trustedTP ) && ( !isProtected(playerto) || isAdmin(playerwho) ) ){
             myWriter.println( "tp " + playerwho + " " + playerto );
         }
         else if(isProtected(playerto)){
             playerPrint("Cannot teleport to protected player");
         }
     }
     
     public void bring (String playerwho, String playerto) {
         if( isAdmin(playerto) ){
             myWriter.println( "tp " + playerwho + " " + playerto );
         }
     }
 
     public void give( String name, String kit ) {
         ArrayList<Integer> ids = kits.get( kit.toLowerCase() );
         if( ids != null ) { 
             for( Integer i : ids ) {
                 myWriter.println( "give " + name + " " + i );
             }
         }
     }
 
     public boolean setPlayerCap( int cap ) {
         if( cap > 0 ) {
             playerPrint( "Setting player cap to " + cap + " players." );
             myPlayerCap = cap;
             return true;
         } else {
             myPlayerCap = -1;
             return false;
         }
     }
 
     public int getPlayerCap() {
         return myPlayerCap;
     }
 
     public void time() {
         SimpleDateFormat f = new SimpleDateFormat( "HH:mm:ss z" );
         Calendar c = Calendar.getInstance();
         playerPrint( "Current server time: " + f.format( c.getTime() ) );
     }
 
     public void uptime() {
         Date now = new Date();
         long d = now.getTime() - startTime.getTime();
         String time = "";
         d /= 1000;
         int hrs = (int)(d / 3600);
         if( hrs < 10 ) {
             time += "0";
         }
         time += hrs + ":";
         d %= 3600;
         int min = (int)(d / 60);
         if( min < 10 ) {
             time += "0";
         }
         time += min + ":";
         d %= 60;
         int sec = (int)d;
         if( sec < 10 ) {
             time += "0";
         }
         time += sec;
 
         playerPrint( "Uptime: " + time );
     }
 
     public void ipList( boolean onlineOnly ){ 
         Collection<String> players;
         if( onlineOnly )
             players = Arrays.asList(connectedPlayers.toLowerCase().split( ",\\s+" ));
         else
             players = ipassoc.keySet();
 
         playerPrint( "Listing IPs of users:" );
         for( String s : players ) {
             String ip = ipassoc.get( s );
             if( ip != null ) {
                 playerPrint( s + ": " + ip );
             } else {
                 System.err.println( "ERROR: Could not obtain an online user from the IP Map." );
             }
         }
     }
 
     public void playerList() {
         playerPrint( "Connected players: " + connectedPlayers );
     }
 
     public void playerPrint( String line ) {
         myWriter.println( "say " + line );   
     }
 
     public void playerPrint( List<String> lines ) {
         for( String s : lines ) {
             playerPrint( s );
         }
     }
 
     public void stopOutput() {
         keep_going = false;
     }
 
     public boolean stopped() {
         return !keep_going;
     }
 
     public boolean isSuperAdmin( String name ) {
         if( permissions.containsKey( name.toLowerCase() ) ) {
             return permissions.get( name.toLowerCase() ) >= P_SUPERADMIN;
         } else {
             return false;
         }
     }
 
     public boolean isAdmin( String name ) {
         if( permissions.containsKey( name.toLowerCase() ) ) {
             return permissions.get( name.toLowerCase() ) >= P_ADMIN;
         } else {
             return false;
         }
     }
 
     public boolean isTrusted( String name ) {
         if( permissions.containsKey( name.toLowerCase() ) ) {
             return permissions.get( name.toLowerCase() ) >= P_TRUSTED;
         } else {
             return false;
         }
     }
     
     public boolean isProtected ( String name ) {
         return protectedtrusted.contains( name.toLowerCase() );
     }
 
     public boolean isAllowed( String name )   {   
         if( !isPlayerWhiteList  ) {
             return true;
         }
         else if( permissions.containsKey( name.toLowerCase() ) ) {
             return permissions.get( name.toLowerCase() ) >= P_ALLOWED;
         } else {
             return false; 
         }
     }
 
     public void motd() {
         ArrayList<String> motd = new ArrayList<String>();
         try {
             BufferedReader b = new BufferedReader( new FileReader ( "motd.txt" ) );
             while( b.ready() ) {
                 motd.add( b.readLine() );
             }
             b.close();
         } catch( IOException e ) {
             motd.add( "--- MOTD ---" );
             motd.add( "Tell your admin to create a motd.txt!" );
             motd.add( "Type #rules for the rules and #help for help." );
         }
         playerPrint( motd );
     }
 
     public void rules() {
         ArrayList<String> rules = new ArrayList<String>();
         try {
             BufferedReader b = new BufferedReader( new FileReader ( "rules.txt" ) );
             while( b.ready() ) {
                 rules.add( b.readLine() );
             }
             b.close();
         } catch( IOException e ) {
             rules.add( "Tell your server admininstrator to create a rules.txt file!" );
         }
         playerPrint( rules );
     }
 
     public void showList() {
         String listeditems = "";
         for( String s : blacklist ) {
             listeditems += s + " ";
         }
         playerPrint( list_command + "ed items: " + listeditems );
     }
 
     public void help( String name ) {
         playerPrint("Player Commands:" );
 //        playerPrint("- #help: Displays this message" );
         playerPrint("- #summon <item-id> [<count>]: Summons items.");
         playerPrint("- #summon <kit>: Summons the specified kit if it exists." );
         playerPrint("- #list: Lists connected players");
 //        playerPrint("- #points: Lists points that you have.");
 //        playerPrint("- #time: Displays server time.");
 //        playerPrint("- #uptime: Displays time the server has been running.");
 //        playerPrint("- #motd: Displays the server's message of the day." );
 //        playerPrint("- #rules: Displays the server's rules." );
 /*
         if( isAdmin(name) ) {
             playerPrint( "" );
             playerPrint( "Admin Commands:" );
             playerPrint( "- #give <player> <id> [<count>]: Give a player items." );
             playerPrint( "- #give <player> [kit]: Gives the player a kit." );
             playerPrint( "- #fun: Toggles fun mode." );
             playerPrint( "- #kick <player>: Kicks the specified player." );
             if( isPlayerWhiteList ) {
                 playerPrint( "- #allow <player>: Allows the specified player." );
                 playerPrint( "- #unallow <player>: Unallows the specified player." );
             } else {
                 playerPrint( "- #ban <player>: Bans the specified player." );
                 playerPrint( "- #unban <player>: Unbans the specified player." );
             }
             playerPrint( "- #ipban <player or IP>: IP-Bans theplayer/IP." );
             if( isItemWhiteList ) {
                 playerPrint( "- #whitelist <item-id>: Allows summoning of <item-id>." );
                 playerPrint( "- #unwhitelist <item-id>: Allows summoning of <item-id>." );
             } else {
                 playerPrint( "- #blacklist <item-id>: Disallows summoning of <item-id>." );
                 playerPrint( "- #unblacklist <item-id>: Allows summoning of <item-id>." );
             }
             playerPrint("- #points <player> [<change>]: Get or change points." ); 
             playerPrint("- #trust <player>: Adds <player> to the trusted list." );
             playerPrint("- #untrust <player>: Detrusts <player>." );
             playerPrint("- #save: Saves the data files." );
             playerPrint("- #reload: Reloads the data files." );
             playerPrint("- #stop: Stops the server." );
         }*/
     }
 
     public void saveLists() { 
         try{
             System.out.println( "Saving admins.txt, trusted.txt, black_list.txt/white_list.txt, and banned/allowed.txt..." );
             PrintWriter writer = new PrintWriter( new FileWriter( "admins.txt" ), true );
             for( String s : permissions.keySet() ) {
                 if( isAdmin( s ) && !isSuperAdmin( s ) ) {
                     writer.println( s );
                 }
             }
             writer.close();
             writer = new PrintWriter( new FileWriter( "trusted.txt" ) );
             for( String s : permissions.keySet() ) {
                 if( isTrusted( s ) && !isAdmin( s ) ) {
                     writer.println( s );
                 }
             }
             writer.close();
             String file;
             if( isPlayerWhiteList ) {
                 file = "allowed.txt";
                 writer = new PrintWriter( new FileWriter( file ), true );
                 for( String s : permissions.keySet() ) {
                     if( isAllowed(s) ) {
                         writer.println( s );
                     }
                 }
             }
             writer.close();
             file = "black_list.txt";
             if( isItemWhiteList ) {
                 file = "white_list.txt";
             }
             writer = new PrintWriter( new FileWriter( file ), true );
             for( String s : blacklist ) {
                 writer.println( s );
             }
             writer.close();
             //adminblacklist
             if(!isItemWhiteList)
             {
              file = "admin_black_list.txt";
              writer = new PrintWriter( new FileWriter( file ), true );
              for( String s : adminblacklist ) {
                 writer.println( s );
              }
              writer.close();
             }
             /*file = "ipbans.txt";
               writer = new PrintWriter( new FileWriter( file ), true );
               for( String s : ipbans ) {
               writer.println( s );
               }*/
             file = "points.txt";
             writer = new PrintWriter( new FileWriter( file ), true );
             for( String s : points.keySet() ) {
                 writer.println( s + " " + points.get( s ) );
             }
             writer.close();
             System.out.println( "Save successful." );
         } catch( Exception e ) {
             e.printStackTrace();
         }
     }
 
     public void loadLists()
     {
         // These sets and maps correspond to data read in from files.
         permissions = new HashMap<String,Integer>();
         blacklist = new HashSet<String>();
         adminblacklist = new HashSet<String>();//adminblacklist
         kits = new HashMap<String,ArrayList<Integer>>();
         ipassoc = new HashMap<String,String>();
         points = new HashMap<String,Integer>();
 
         // Admin List
         permissions.put( "server", 3 );
         try {
             BufferedReader safile = null;
             safile = new BufferedReader( new FileReader( "superadmins.txt" ) );
             while( safile.ready() ) {
                 String name = safile.readLine();
                 permissions.put( name.toLowerCase(), 3 );
             }
             System.out.println("Senior Admins loaded");
             safile.close();
         } catch( IOException e ) {
             System.err.println( "ERROR: Could not open superadmins.txt" );
         }
         try {
             BufferedReader afile = null;
             afile = new BufferedReader( new FileReader( "admins.txt" ) );
             while( afile.ready() ) {
                 String name = afile.readLine();
                 permissions.put( name.toLowerCase(), 2 );
             }
             System.out.println("Admins loaded");
             afile.close();
         } catch( IOException e ) {
             System.err.println( "ERROR: Could not open admins.txt" );
         }
 
         // Trusted Player List
         try {
             BufferedReader tfile = null;
             tfile = new BufferedReader( new FileReader( "trusted.txt" ) );
             while( tfile.ready() ) {
                 String name = tfile.readLine();
                 if( !isTrusted( name.toLowerCase() ) ) {
                     permissions.put( name.toLowerCase(), 1 );
                 }
             }
             System.out.println("Trusted loaded");
             tfile.close();
         } catch( IOException e ) {
             System.out.println( "ERROR: Could not open trusted.txt" );
         }
 
         String file;// = "banned.txt";
         if( isPlayerWhiteList ) {
             file = "allowed.txt";
 
             // Banned Player List
             try {
                 BufferedReader bfile = null;
                 bfile = new BufferedReader( new FileReader( file ) );
                 while( bfile.ready() ) {
                     String name = bfile.readLine();
                     if( !isPlayerWhiteList ) {
                         permissions.put( name.toLowerCase(), -1 );
                     } else {
                         if( !isTrusted( name.toLowerCase() ) ) {
                             permissions.put( name.toLowerCase(), 0 );
                         }
                     }
                     //banned.add( name.toLowerCase() );
                 }
                 bfile.close();
             } catch( IOException e ) {
                 System.err.println( "ERROR: Could not open " + file );
             }
         }
         file = "black_list.txt";
         if( isItemWhiteList ) {
             file = "white_list.txt";
         }
         // Summon Item blacklist
         try {
             BufferedReader blfile = null;
 
             blfile = new BufferedReader( new FileReader( file ) );
             while( blfile.ready() ) {
                 blacklist.add( blfile.readLine() );
             }
             blfile.close();
         } catch( IOException e ) { 
             System.err.println( "ERROR: Cannot open " + file );
         } 
         //adminblacklist
         if (!isItemWhiteList)
         {
          file = "admin_black_list.txt";
          try {
               BufferedReader ablfile = null;
 
               ablfile = new BufferedReader( new FileReader( file ) );
               while( ablfile.ready() ) {
                   adminblacklist.add( ablfile.readLine() );
               }
               ablfile.close();
             } catch( IOException e ) { 
               System.err.println( "ERROR: Cannot open " + file );
           } 
          }
 
         // Summonable kits
         try { 
             BufferedReader kfile = new BufferedReader( new FileReader( "kits.txt" ) );
             while( kfile.ready() ) {
                 String line = kfile.readLine();
                 String[] split = line.split( "\\s+" );
                 if( split.length > 1 ) {
                     ArrayList<Integer> ints = new ArrayList<Integer>();
                     try {
                         for( int i = 1; i < split.length; i++ ) {
                             ints.add( Integer.parseInt( split[i] ) );
                         }
                         kits.put( split[0].toLowerCase(), ints );
                     }
                     catch( NumberFormatException e ) {
                         System.out.println( "Error with following kit: " + line );
                     }
                 }
             }
             kfile.close();
         } catch( IOException e ) {
             System.err.println( "ERROR: Cannot open kits.txt." );
         }
         /*try {
         // IP bans    
         BufferedReader ipfile = new BufferedReader( new FileReader( "ipbans.txt" ) );
         while( ipfile.ready() ) {
         String line = ipfile.readLine();
         ipbans.add( line );
         }
         ipfile.close();
         } catch( IOException e ) {
         System.err.println( "ERROR: Cannot open ipbans.txt." );
         }*/
 
         // Points.
         try{
             BufferedReader pfile = new BufferedReader( new FileReader( "points.txt" ) );
             while( pfile.ready() ) {
                 String[] line = pfile.readLine().split( "\\s+" );
                 if( line.length >= 2 ) {
                     try {
                         points.put( line[0].toLowerCase(), Integer.parseInt( line[1] ) );
                     } catch( NumberFormatException e ) {
                         System.err.println( "ERROR: Invalid line in points.txt." ); 
                     }
                 } else {
                     System.err.println( "ERROR: Invalid line in points.txt." ); 
                 }
             }
         } catch( IOException e ) {
             System.err.println( "ERROR: Cannot open points.txt." );
         }
     }
 
     // The wrapped process
     private Process myProcess;
 
     // Input/Output Readers/Writers for the process.
     private BufferedReader myErrReader;
     private BufferedReader myInReader;
     private PrintWriter myWriter;
 
     // Variables used for run() and other functions.
     private boolean keep_going;
     private int myPlayerCount;
     private String connectedPlayers;
     private String ban_command;
     private String list_command;
     private Date startTime;
     // TODO: Implement us
     private String votekickName;
     private Date votekickDate;
     private int votekickVotes;
     private boolean isVoteban;
 
     // Maps and sets
     private HashMap<String,Integer> permissions;
     private HashMap<String,Integer> items;
     private HashSet<String> blacklist;
     private HashSet<String> adminblacklist;
     private HashMap<String,ArrayList<Integer>> kits;
     private HashMap<String,String> ipassoc;
     private HashMap<String,Integer> points;
     private HashSet<String> protectedtrusted;
     // TODO: Implement me
     private HashMap<String,Date> playerLoginTimes;
 
     // Property Values
     private int myPlayerCap;
     private int myRAMUse;
     private boolean isPlayerWhiteList;
     private boolean isItemWhiteList;
     private boolean isFun;
     private boolean autoSave;
     private Date lastSave;
     private boolean allowHelp;
     private boolean trustedTP;
     }
