 package com.hifiremote.jp1;
 
 import java.util.Arrays;
 import java.util.Vector;
 import java.util.Enumeration;
 import java.util.Properties;
 import java.util.StringTokenizer;
 import java.io.FileOutputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.File;
 import java.text.DecimalFormat;
 import javax.swing.JOptionPane;
 import javax.swing.JList;
 import java.awt.Component;
 
 public class DeviceUpgrade
 {
   public DeviceUpgrade()
   {
    devTypeAliasName = deviceTypeAliasNames[ 0 ];
     initFunctions();
   }
 
   public void reset( Remote[] remotes, Vector protocols )
   {
     description = null;
     setupCode = 0;
 
     // remove all currently assigned functions
     Button[] buttons = remote.getUpgradeButtons();
     for ( int i = 0; i < buttons.length; i++ )
     {
       Button b = buttons[ i ];
       if ( b.getFunction() != null )
         b.setFunction( null );
       if ( b.getShiftedFunction() != null )
         b.setShiftedFunction( null );
     }
 
     remote = remotes[ 0 ];
     devTypeAliasName = deviceTypeAliasNames[ 0 ];
     protocol = ( Protocol )protocols.elementAt( 0 );
     notes = null;
     file = null;
 
     functions.clear();
     initFunctions();
 
     extFunctions.clear();
   }
 
   private void initFunctions()
   {
     for ( int i = 0; i < defaultFunctionNames.length; i++ )
       functions.add( new Function( defaultFunctionNames[ i ]));
   }
 
   public void setDescription( String text )
   {
     description = text;
   }
 
   public String getDescription()
   {
     return description;
   }
 
   public void setSetupCode( int setupCode )
   {
     this.setupCode = setupCode;
   }
 
   public int getSetupCode()
   {
     return setupCode;
   }
 
   public void setRemote( Remote newRemote )
   {
     if (( remote != null ) && ( remote != newRemote ))
     {
       Button[] buttons = remote.getUpgradeButtons();
       Button[] newButtons = newRemote.getUpgradeButtons();
       for ( int i = 0; i < buttons.length; i++ )
       {
         Button b = buttons[ i ];
         Function f = b.getFunction();
         Function sf = b.getShiftedFunction();
         if (( f != null ) || ( sf != null ))
         {
           if ( f != null )
             b.setFunction( null );
           if ( sf != null )
             b.setShiftedFunction( null );
 
           Button newB = newRemote.findByStandardName( b );
           if ( newB != null )
           {
             if ( f != null )
               newB.setFunction( f );
             if ( sf != null )
               newB.setShiftedFunction( sf );
           }
         }
       }
     }
     remote = newRemote;
   }
 
   public Remote getRemote()
   {
     return remote;
   }
 
   public void setDeviceTypeAliasName( String name )
   {
     devTypeAliasName = name;
   }
 
   public String getDeviceTypeAliasName()
   {
     return devTypeAliasName;
   }
 
   public DeviceType getDeviceType()
   {
     return remote.getDeviceTypeByAliasName( devTypeAliasName );
   }
 
   public void setProtocol( Protocol protocol )
   {
     this.protocol = protocol;
   }
 
   public Protocol getProtocol()
   {
     return protocol;
   }
 
   public void setNotes( String notes )
   {
     this.notes = notes;
   }
 
   public String getNotes()
   {
     return notes;
   }
 
   public Vector getFunctions()
   {
     return functions;
   }
 
   public Function getFunction( String name )
   {
     Function rc = getFunction( name, functions );
     if ( rc == null )
       rc =  getFunction( name, extFunctions );
     return rc;
   }
 
   public Function getFunction( String name, Vector funcs )
   {
     Function rc = null;
     for ( Enumeration e = funcs.elements(); e.hasMoreElements(); )
     {
       Function func = ( Function )e.nextElement();
       if ( func.getName().equals( name ))
       {
         rc = func;
         break;
       }
     }
     return rc;
   }
 
   public Vector getExternalFunctions()
   {
     return extFunctions;
   }
 
   private int findDigitMapIndex()
   {
     System.err.println( "DeviceUpgrade.findDigitIndex()" );
     Button[] buttons = remote.getUpgradeButtons();
     byte[] digitMaps = remote.getDigitMaps();
     if (( digitMaps != null ) && ( protocol.getDefaultCmd().length() == 1 ))
     {
       for ( int i = 0; i < digitMaps.length; i++ )
       {
         int mapNum = digitMaps[ i ];
         System.err.println( "Checking digitMap at index " + i + ", which is " + mapNum );
         System.err.print( "It contains codes " );
         int[] codes = DIGIT_MAP[ mapNum ];
         for ( int k = 0; k < codes.length; k++ )
           System.err.print( " " + Integer.toHexString( codes[ k ]));
         System.err.println();
         int rc = -1;
         for ( int j = 0; ; j++ )
         {
           Function f = buttons[ j ].getFunction();
           if (( f != null ) && !f.isExternal())
             if (( f.getHex().getData()[ 0 ] & 0xFF ) == DIGIT_MAP[ mapNum ][ j ])
               rc = i + 1;
             else
               break;  
           if ( j == 9 )
           {
             System.err.println( "Matches " + rc);
             return rc;
           }
         }
       }
     }
     return -1;
   }
 
   public String getUpgradeText()
   {
     StringBuffer buff = new StringBuffer( 400 );
     buff.append( "Upgrade code 0 = " );
     if ( devTypeAliasName != null )
     {
       DeviceType devType = remote.getDeviceTypeByAliasName( devTypeAliasName );
       byte[] id = protocol.getID().getData();
       int temp = devType.getNumber() * 0x1000 +
                  ( id[ 0 ] & 1 ) * 0x08 +
                  setupCode - remote.getDeviceCodeOffset();
 
       byte[] deviceCode = new byte[2];
       deviceCode[ 0 ] = ( byte )(temp >> 8 );
       deviceCode[ 1 ] = ( byte )temp;
 
       buff.append( Hex.toString( deviceCode ));
       buff.append( " (" );
       buff.append( devTypeAliasName );
       buff.append( '/' );
       DecimalFormat df = new DecimalFormat( "0000" );
       buff.append( df.format( setupCode ));
       buff.append( ")\n " );
       buff.append( Hex.toString( id[ 1 ]));
 
       int digitMapIndex = -1;
       
       if ( !remote.getOmitDigitMapByte())
       {
         buff.append( ' ' );
         digitMapIndex = findDigitMapIndex();
         if ( digitMapIndex == -1 )
           buff.append( "00" );
         else 
         {
           byte[] array = new byte[ 1 ];
           array[ 0 ] = ( byte )digitMapIndex;
           buff.append( Hex.toString( array ));
         }
       }
 
       buff.append( ' ' );
       ButtonMap map = devType.getButtonMap();
       buff.append( Hex.toString( map.toBitMap( digitMapIndex != -1 )));
 
       buff.append( ' ' );
       buff.append( protocol.getFixedData().toString());
 
       byte[] data = map.toCommandList( digitMapIndex != -1 );
       if (( data != null ) && ( data.length != 0 ))
       {
         buff.append( "\n " );
         buff.append( Hex.toString( data, 16 ));
       }
 
       Button[] buttons = remote.getUpgradeButtons();
       boolean hasKeyMoves = false;
       int startingButton = 0;
       int i;
       for ( i = 0; i < buttons.length; i++ )
       {
         Button b = buttons[ i ];
         Function f = b.getFunction();
         Function sf = b.getShiftedFunction();
         if ((( f != null ) && ( !map.isPresent( b ) || f.isExternal())) ||
             (( sf != null ) && ( sf.getHex() != null )))
         {
           hasKeyMoves = true;
           break;
         }
       }
       if ( hasKeyMoves )
       {
         deviceCode[ 0 ] = ( byte )( deviceCode[ 0 ] & 0xF7 );
         buff.append( "\nKeyMoves" );
         for ( ; i < buttons.length; i++ )
         {
           Button button = buttons[ i ];
           byte[] keyMoves = button.getKeyMoves( deviceCode, devType, remote );
           if (( keyMoves != null ) && keyMoves.length > 0 )
           {
             buff.append( "\n " );
             buff.append( Hex.toString( keyMoves ));
           }
         }
       }
 
       buff.append( "\nEND" );
     }
 
     return buff.toString();
   }
 
   public void store()
     throws IOException
   {
     store( file );
   }
 
   public static String valueArrayToString( Value[] parms )
   {
     StringBuffer buff = new StringBuffer( 200 );
     for ( int i = 0; i < parms.length; i++ )
     {
       if ( i > 0 )
         buff.append( ' ' );
       buff.append( parms[ i ].getUserValue());
     }
     return buff.toString();
   }
 
   public Value[] stringToValueArray( String str )
   {
     StringTokenizer st = new StringTokenizer( str );
     Value[] parms = new Value[ st.countTokens()];
     for ( int i = 0; i < parms.length; i++ )
     {
       String token = st.nextToken();
       Integer val = null;
       if ( !token.equals( "null" ))
         val = new Integer( token );
       parms[ i ] = new Value( val, null );
     }
     return parms;
   }
 
   public void store( File file )
     throws IOException
   {
     this.file = file;
     Properties props = new Properties();
     if ( description != null )
       props.setProperty( "Description", description );
     props.setProperty( "Remote.name", remote.getName());
     props.setProperty( "Remote.signature", remote.getSignature());
     props.setProperty( "DeviceType", devTypeAliasName );
     DeviceType devType = remote.getDeviceTypeByAliasName( devTypeAliasName );
     props.setProperty( "DeviceIndex", Integer.toHexString( devType.getNumber()));
     props.setProperty( "SetupCode", Integer.toString( setupCode ));
     props.setProperty( "Protocol", protocol.getID().toString());
     props.setProperty( "Protocol.name", protocol.getName());
     Value[] parms = protocol.getDeviceParmValues();
     if (( parms != null ) && ( parms.length != 0 ))
       props.setProperty( "ProtocolParms", valueArrayToString( parms ));
     props.setProperty( "FixedData", protocol.getFixedData().toString());
 
     if ( notes != null )
       props.setProperty( "Notes", notes );
     int i = 0;
     for ( Enumeration e = functions.elements(); e.hasMoreElements(); i++ )
     {
       Function func = ( Function )e.nextElement();
       func.store( props, "Function." + i );
     }
     i = 0;
     for ( Enumeration e = extFunctions.elements(); e.hasMoreElements(); i++ )
     {
       ExternalFunction func = ( ExternalFunction )e.nextElement();
       func.store( props, "ExtFunction." + i );
     }
     Button[] buttons = remote.getUpgradeButtons();
     for ( i = 0; i < buttons.length; i++ )
     {
       Button b = buttons[ i ];
       Function f = b.getFunction();
 
       String fstr;
       if ( f == null )
         fstr = "null";
       else
         fstr = f.getName();
 
       Function sf = b.getShiftedFunction();
       String sstr;
       if ( sf == null )
         sstr = "null";
       else
         sstr = sf.getName();
       if (( f != null ) || ( sf != null ))
       {
         props.setProperty( "Button." + Integer.toHexString( b.getKeyCode()),
                            fstr + '|' + sstr );
       }
 
     }
     FileOutputStream out = new FileOutputStream( file );
     props.store( out, null );
     out.close();
   }
 
   public void load( File file, Remote[] remotes, Vector protocols )
     throws Exception
   {
     System.err.println( "DeviceUpgrade.load()" );
     this.file = file;
     Properties props = new Properties();
     FileInputStream in = new FileInputStream( file );
     props.load( in );
     in.close();
 
     String str = props.getProperty( "Description" );
     if ( str != null )  
       description = str;
     str = props.getProperty( "Remote.name" );
     String sig = props.getProperty( "Remote.signature" );
     System.err.println( "Searching for remote " + str );
     int index = Arrays.binarySearch( remotes, str );
     if ( index < 0 )
     {
       // build a list of similar remote names, and ask the user to pick a match.
       Vector similarRemotes = new Vector();
       for ( int i = 0; i < remotes.length; i++ )
       {
         if ( remotes[ i ].getName().indexOf( str ) != -1 )
           similarRemotes.add( remotes[ i ]);
       }
 
       Object[] simRemotes = null;
       if ( similarRemotes.size() > 0 )
         simRemotes = similarRemotes.toArray();
       else
         simRemotes = remotes;
       
       String message = "Could not find an exact match for the remote \"" + str + "\".  Choose one from the list below:";
       
       Object rc = ( Remote )JOptionPane.showInputDialog( null, 
                                                          message,
                                                          "Upgrade Load Error",
                                                          JOptionPane.ERROR_MESSAGE,
                                                          null, 
                                                          simRemotes,
                                                          simRemotes[ 0 ]);
       if ( rc == null )
         return;
       else
         remote = ( Remote )rc;
     }
     else
       remote = remotes[ index ];
     index = -1;
     str = props.getProperty( "DeviceIndex" );
     if ( str != null )
       index = Integer.parseInt( str, 16 );
     devTypeAliasName = props.getProperty( "DeviceType" );
     System.err.println( "Searching for device type " + devTypeAliasName );
     DeviceType devType = remote.getDeviceTypeByAliasName( devTypeAliasName );
     if ( devType == null )
       System.err.println( "Unable to find device type with alias name " + devTypeAliasName );
     setupCode = Integer.parseInt( props.getProperty( "SetupCode" ));
 
     System.err.println( "Searching for protocol with id " + props.getProperty( "Protocol" ));
     int leastDifferent = Protocol.tooDifferent;
     for ( Enumeration e = protocols.elements(); e.hasMoreElements(); )
     {
       Protocol tentative = ( Protocol )e.nextElement();
       int difference = tentative.different( props );
       if (difference < leastDifferent)
       {
         protocol = tentative;
         leastDifferent = difference;
         if ( difference == 0 )
           break;
       }
     }
     if ( leastDifferent == Protocol.tooDifferent )
     {
       JOptionPane.showMessageDialog( null,
                                      "No matching protocol for ID " + props.getProperty( "Protocol" ) + " was found!",
                                      "File Load Error", JOptionPane.ERROR_MESSAGE );
       return;
     }
     str = props.getProperty( "ProtocolParms" );
     if (( str != null ) && ( str.length() != 0 ))
       protocol.setDeviceParms( stringToValueArray( str ));
 
     notes = props.getProperty( "Notes" );
 
     System.err.println( "Loading functions" );
     functions.clear();
     int i = 0;
     while ( true )
     {
       Function f = new Function();
       f.load( props, "Function." + i );
       if ( f.isEmpty())
       {
         break;
       }
       functions.add( f );
       i++;
     }
 
     System.err.println( "Loading external functions" );
     extFunctions.clear();
     i = 0;
     while ( true )
     {
       ExternalFunction f = new ExternalFunction();
       f.load( props, "ExtFunction." + i, remote );
       if ( f.isEmpty())
       {
         break;
       }
       extFunctions.add( f );
       i++;
     }
     System.err.println( "Assigning functions to buttons" );
     Button[] buttons = remote.getUpgradeButtons();
     for ( i = 0; i < buttons.length; i++ )
     {
       Button b = buttons[ i ];
       str = props.getProperty( "Button." + Integer.toHexString( b.getKeyCode()));
       if ( str == null )
       {
         continue;
       }
       StringTokenizer st = new StringTokenizer( str, "|" );
       str = st.nextToken();
       Function func = null;
       if ( !str.equals( "null" ))
       {
         func = getFunction( str );
         b.setFunction( func );
       }
       str = st.nextToken();
       if ( !str.equals( "null" ))
       {
         func = getFunction( str );
         b.setShiftedFunction( func );
       }
     }
   }
 
   public static final String[] getDeviceTypeAliasNames()
   {
     return deviceTypeAliasNames;
   }
 
   private String description = null;
   private int setupCode = 0;
   private Remote remote = null;
   private String devTypeAliasName = null;
   private Protocol protocol = null;
   private String notes = null;
   private Vector functions = new Vector();
   private Vector extFunctions = new Vector();
   private File file = null;
 
   private static final String[] deviceTypeAliasNames =
   {
     "Cable", "TV", "VCR", "CD", "Tuner", "DVD", "SAT", "Tape", "Laserdisc",
     "DAT", "Home Auto", "Misc Audio", "Phono", "Video Acc", "Amp"
   };
 
   private static final String[] defaultFunctionNames =
   {
     "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
     "vol up", "vol down", "mute",
     "channel up", "channel down",
     "power", "enter", "tv/vcr",
     "last (prev ch)", "menu", "program guide", "up arrow", "down arrow",
     "left arrow", "right arrow", "select", "sleep", "pip on/off", "display",
     "pip swap", "pip move", "play", "pause", "rewind", "fast fwd", "stop",
     "record", "exit", "surround", "input toggle", "+100", "fav/scan",
     "device button", "next track", "prev track", "shift-left", "shift-right",
     "pip freeze", "slow", "eject", "slow+", "slow-", "X2", "center", "rear"
   };
 
   private final static int NUM_DIGIT_TABLES = 132;
   private final static int[][] DIGIT_MAP =
   {
     { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 },
     { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09 },
     { 0x00, 0x01, 0x03, 0x02, 0x06, 0x07, 0x05, 0x04, 0x0C, 0x0D },
     { 0x00, 0x80, 0x40, 0xC0, 0x20, 0xA0, 0x60, 0xE0, 0x10, 0x90 },
     { 0x00, 0xF4, 0x74, 0xB4, 0x34, 0xD4, 0x54, 0x94, 0x14, 0xE4 },
     { 0x04, 0x00, 0x80, 0x40, 0xC0, 0x20, 0xA0, 0x60, 0xE0, 0x10 },
     { 0x04, 0x44, 0x24, 0x64, 0x14, 0x54, 0x34, 0x74, 0x0C, 0x4C },
     { 0x09, 0x89, 0x49, 0xC9, 0x29, 0xA9, 0x69, 0xE9, 0x19, 0x99 },
     { 0x0F, 0x3F, 0xBF, 0x7F, 0x1F, 0x9F, 0x5F, 0x2F, 0xAF, 0x6F },
     { 0x17, 0x16, 0x1B, 0x1A, 0x23, 0x22, 0x2F, 0x2E, 0x27, 0x26 },
     { 0x17, 0x8F, 0x0F, 0xF7, 0x77, 0xB7, 0x37, 0xD7, 0x57, 0x97 },
     { 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39 },
     { 0x31, 0x79, 0x39, 0x59, 0x19, 0x69, 0x29, 0x49, 0x09, 0x71 },
     { 0x33, 0x7B, 0xBB, 0x3B, 0xDB, 0x5B, 0x9B, 0x1B, 0xEB, 0x6B },
     { 0x33, 0xF3, 0x6B, 0x63, 0xEB, 0xFB, 0xE3, 0xB3, 0xAB, 0xBB },
     { 0x40, 0xF0, 0x70, 0xB0, 0xD0, 0x50, 0x90, 0xE0, 0x60, 0xA0 },
     { 0x44, 0x10, 0x14, 0x18, 0x20, 0x24, 0x28, 0x30, 0x34, 0x38 },
     { 0x47, 0xDF, 0x5F, 0x9F, 0x1F, 0xCF, 0x4F, 0x8F, 0x0F, 0xC7 },
     { 0x4F, 0x8F, 0x0F, 0xC7, 0x47, 0x87, 0x07, 0xDF, 0x5F, 0xCF },
     { 0x4F, 0xE7, 0x67, 0xA7, 0xD7, 0x57, 0x97, 0xF7, 0x77, 0xB7 },
     { 0x4F, 0xFF, 0x7F, 0xBF, 0xDF, 0x5F, 0x9F, 0xEF, 0x6F, 0xAF },
     { 0x50, 0x80, 0x40, 0xC0, 0x20, 0xA0, 0x60, 0xE0, 0x10, 0x90 },
     { 0x5A, 0xDE, 0x5C, 0x9C, 0x1E, 0xD4, 0x56, 0x96, 0x14, 0xD8 },
     { 0x5D, 0x8F, 0x4F, 0xCF, 0x95, 0x55, 0xD5, 0x8D, 0x4D, 0xCD },
     { 0x5F, 0xF7, 0x77, 0xB7, 0xCF, 0x4F, 0x8F, 0xEF, 0x6F, 0xAF },
     { 0x64, 0x68, 0x6C, 0x70, 0x48, 0x4C, 0x50, 0x28, 0x2C, 0x30 },
     { 0x64, 0xF4, 0x74, 0xB4, 0x34, 0xD4, 0x54, 0x94, 0x14, 0xE4 },
     { 0x66, 0xF6, 0x76, 0xB6, 0x36, 0xD6, 0x56, 0x96, 0x16, 0xE6 },
     { 0x67, 0xCF, 0x4F, 0x8F, 0xF7, 0x77, 0xB7, 0xD7, 0x57, 0x97 },
     { 0x67, 0xF7, 0x77, 0xB7, 0x37, 0xD7, 0x57, 0x97, 0x17, 0xE7 },
     { 0x68, 0x04, 0x78, 0xB8, 0x38, 0xD8, 0x58, 0x98, 0x18, 0xE8 },
     { 0x6D, 0xFD, 0x7D, 0xBD, 0x3D, 0xDD, 0x5D, 0x9D, 0x1D, 0xED },
     { 0x6F, 0xFF, 0x7F, 0xBF, 0x3F, 0xDF, 0x5F, 0x9F, 0x1F, 0xEF },
     { 0x6F, 0xFF, 0xEF, 0xF7, 0xE7, 0xFB, 0xEB, 0xF3, 0xE3, 0x7F },
     { 0x77, 0xDF, 0x5F, 0x9F, 0xEF, 0x6F, 0xAF, 0xCF, 0x4F, 0x8F },
     { 0x77, 0xFF, 0x5F, 0x9F, 0xDF, 0x6F, 0xAF, 0xEF, 0x4F, 0x8F },
     { 0x80, 0x81, 0x83, 0x82, 0x86, 0x87, 0x85, 0x84, 0x8C, 0x8D },
     { 0x80, 0xC0, 0xA0, 0xE0, 0x90, 0xD0, 0xB0, 0xF0, 0x88, 0xC8 },
     { 0x84, 0xC4, 0xA4, 0xE4, 0x94, 0xD4, 0xB4, 0xF4, 0x8C, 0xCC },
     { 0x86, 0xFA, 0xBA, 0xDA, 0x9A, 0xEA, 0xAA, 0xCA, 0x8A, 0xF2 },
     { 0x87, 0x37, 0xB7, 0x77, 0x17, 0x97, 0x57, 0x27, 0xA7, 0x67 },
     { 0x87, 0x6F, 0xE7, 0xF7, 0x27, 0x57, 0x77, 0x97, 0x67, 0x17 },
     { 0x88, 0x20, 0x28, 0x30, 0x40, 0x48, 0x50, 0x60, 0x68, 0x70 },
     { 0x8F, 0x3F, 0xBF, 0x7F, 0x1F, 0x9F, 0x5F, 0x2F, 0xAF, 0x6F },
     { 0x8F, 0xBF, 0x37, 0xCF, 0x97, 0xD7, 0x17, 0xE7, 0x4F, 0x67 },
     { 0x8F, 0xDF, 0x5F, 0x9F, 0x1F, 0xEF, 0x6F, 0xAF, 0x2F, 0xCF },
     { 0x90, 0x00, 0x80, 0x40, 0xC0, 0x20, 0xA0, 0x60, 0xE0, 0x10 },
     { 0x90, 0x20, 0x28, 0x30, 0x40, 0x48, 0x50, 0x60, 0x68, 0x70 },
     { 0x90, 0xB8, 0xB0, 0xA8, 0xD8, 0xD0, 0xC8, 0xF8, 0xF0, 0xE8 },
     { 0x97, 0x17, 0xE7, 0x67, 0xA7, 0x27, 0xC7, 0x47, 0x87, 0x07 },
     { 0x97, 0xB7, 0x37, 0x77, 0xF7, 0x0F, 0xE7, 0x17, 0xD7, 0x57 },
     { 0x98, 0xBC, 0xB8, 0xB4, 0xB0, 0xAC, 0xA8, 0xA4, 0xA0, 0x9C },
     { 0x98, 0xDC, 0x5E, 0x9E, 0x1C, 0xD6, 0x54, 0x94, 0x16, 0xDA },
     { 0x9B, 0x0B, 0x8B, 0x4B, 0xCB, 0x2B, 0xAB, 0x6B, 0xEB, 0x1B },
     { 0x9C, 0x92, 0xA2, 0x82, 0x9A, 0xAA, 0x8A, 0x96, 0xA6, 0x86 },
     { 0x9F, 0x07, 0x87, 0x47, 0xC7, 0x0F, 0x8F, 0x4F, 0xCF, 0x1F },
     { 0x9F, 0x4F, 0x8F, 0x0F, 0xC7, 0x47, 0x87, 0x07, 0xDF, 0x5F },
     { 0xA4, 0x74, 0xB4, 0x34, 0xD4, 0x54, 0x94, 0x14, 0xE4, 0x64 },
     { 0xA7, 0x77, 0xB7, 0x37, 0xD7, 0x57, 0x97, 0x17, 0xE7, 0x67 },
     { 0xA7, 0xF7, 0x77, 0xB7, 0x37, 0xD7, 0x57, 0x97, 0x17, 0xE7 },
     { 0xAC, 0x40, 0xA0, 0xC0, 0x44, 0xA4, 0xC4, 0x48, 0xA8, 0xC8 },
     { 0xAC, 0x98, 0x88, 0x90, 0xB8, 0xA8, 0xB0, 0x9C, 0x8C, 0x94 },
     { 0xAC, 0xD8, 0xB4, 0x70, 0xD4, 0xB8, 0x8C, 0xF0, 0xA8, 0x94 },
     { 0xAE, 0x7E, 0xBE, 0x3E, 0xDE, 0x5E, 0x9E, 0x1E, 0xEE, 0x6E },
     { 0xAF, 0x7F, 0xBF, 0x3F, 0xDF, 0x5F, 0x9F, 0x1F, 0xEF, 0x6F },
     { 0xAF, 0xFF, 0x7F, 0xBF, 0x3F, 0xDF, 0x5F, 0x9F, 0x1F, 0xEF },
     { 0xB0, 0x20, 0xA0, 0x60, 0xE0, 0x10, 0x90, 0x50, 0xD0, 0x30 },
     { 0xB4, 0xDC, 0xD8, 0xC0, 0xBC, 0xB8, 0xD4, 0xCC, 0xC8, 0xC4 },
     { 0xB6, 0x1E, 0x9E, 0x5E, 0x2E, 0xAE, 0x6E, 0x0E, 0x8E, 0x4E },
     { 0xB7, 0x6F, 0x47, 0x07, 0x4F, 0x67, 0x27, 0x77, 0x57, 0x17 },
     { 0xB7, 0x7F, 0x9F, 0x1F, 0x5F, 0xAF, 0x2F, 0x6F, 0x8F, 0x0F },
     { 0xBC, 0xB8, 0xB4, 0xB0, 0xAC, 0xA8, 0xA4, 0xA0, 0x9C, 0x98 },
     { 0xC0, 0xC4, 0xC8, 0xCC, 0xD0, 0xD4, 0xD8, 0xDC, 0xE0, 0xE4 },
     { 0xC4, 0xA0, 0xA2, 0xA4, 0xA8, 0xAA, 0xB0, 0xB2, 0xB4, 0xC0 },
     { 0xC4, 0xFC, 0xDC, 0xBC, 0xF8, 0xD8, 0xB8, 0xF4, 0xD4, 0xB4 },
     { 0xCF, 0x4F, 0x8F, 0x0F, 0xC7, 0x47, 0x87, 0x07, 0xDF, 0x5F },
     { 0xCF, 0xCE, 0xCD, 0xCC, 0xCB, 0xCA, 0xC9, 0xC8, 0xC7, 0xC6 },
     { 0xD2, 0x42, 0xC2, 0x22, 0xA2, 0x62, 0xE2, 0x12, 0x92, 0x52 },
     { 0xD4, 0xF8, 0xF4, 0xF0, 0xEC, 0xE8, 0xE4, 0xE0, 0xDC, 0xD8 },
     { 0xD8, 0xE8, 0xEC, 0xF0, 0xF4, 0xF8, 0xC8, 0xCC, 0xD0, 0xD4 },
     { 0xDF, 0x5F, 0x9F, 0x1F, 0xCF, 0x4F, 0x8F, 0x0F, 0xC7, 0x47 },
     { 0xDF, 0x5F, 0x9F, 0x1F, 0xEF, 0x6F, 0xAF, 0x2F, 0xCF, 0x4F },
     { 0xE4, 0xEC, 0xE4, 0xCC, 0xF0, 0xD0, 0xC8, 0xD0, 0xF8, 0xDC },
     { 0xEF, 0xFF, 0xBF, 0x3F, 0x57, 0x67, 0x2F, 0xD7, 0xE7, 0x37 },
     { 0xF4, 0x74, 0xB4, 0x34, 0xD4, 0x54, 0x94, 0x14, 0xE4, 0x64 },
     { 0xF6, 0x76, 0xB6, 0x36, 0xD6, 0x56, 0x96, 0x16, 0xE6, 0x66 },
     { 0xF7, 0x77, 0xB7, 0x37, 0xD7, 0x57, 0x97, 0x17, 0xE7, 0x67 },
     { 0xF9, 0x79, 0xB9, 0x39, 0xD9, 0x59, 0x99, 0x19, 0xE9, 0x69 },
     { 0xFA, 0x7A, 0xBA, 0x3A, 0xDA, 0x5A, 0x9A, 0x1A, 0xEA, 0x6A },
     { 0xFA, 0xBA, 0xDA, 0x9A, 0xEA, 0xAA, 0xCA, 0x8A, 0xF2, 0xB2 },
     { 0xFB, 0x7B, 0xBB, 0x3B, 0xDB, 0x5B, 0x9B, 0x1B, 0xEB, 0x6B },
     { 0xFC, 0x7C, 0xBC, 0x3C, 0xDC, 0x5C, 0x9C, 0x1C, 0xEC, 0x6C },
     { 0xFC, 0xF8, 0xF4, 0xF0, 0xEC, 0xE8, 0xE4, 0xE0, 0xDC, 0xD8 },
     { 0xFD, 0x7D, 0xBD, 0x3D, 0xDD, 0x5D, 0x9D, 0x1D, 0xED, 0x6D },
     { 0xFF, 0x7F, 0xBF, 0x3F, 0xDF, 0x5F, 0x9F, 0x1F, 0xEF, 0x6F },
     { 0xFF, 0xFE, 0xFD, 0xFC, 0xFB, 0xFA, 0xF9, 0xF8, 0xF7, 0xF6 },
     { 0x07, 0x0F, 0x17, 0x1F, 0x27, 0x2F, 0x37, 0x3F, 0x47, 0x4F },
     { 0x00, 0x20, 0x40, 0x60, 0x80, 0x88, 0x68, 0x48, 0x28, 0x08 },
     { 0xB4, 0xF4, 0x8C, 0xCC, 0xAC, 0xEC, 0x9C, 0xDC, 0xBC, 0xFC },
     { 0x96, 0xCE, 0x4E, 0x8E, 0x0E, 0xF6, 0x76, 0xB6, 0x36, 0x56 },
     { 0x17, 0xE7, 0xD7, 0xF7, 0x67, 0x57, 0x77, 0xA7, 0x97, 0xB7 },
     { 0xFF, 0x7F, 0xBF, 0x3F, 0xEF, 0x6F, 0xAF, 0x2F, 0xF7, 0x77 },
     { 0x17, 0xE7, 0xD7, 0xF7, 0x67, 0x57, 0x77, 0xA7, 0x97, 0xB7 },
     { 0xEC, 0x48, 0x4C, 0xC8, 0x88, 0xF0, 0x68, 0x28, 0xCC, 0x8C },
     { 0xC4, 0xFC, 0xDC, 0xBC, 0xEC, 0xCC, 0xAC, 0xE8, 0xC8, 0xA8 },
     { 0x58, 0xDC, 0x5E, 0x9E, 0x1C, 0xD6, 0x54, 0x94, 0x16, 0xDA },
     { 0x4F, 0xDF, 0x5F, 0x9F, 0x1F, 0xEF, 0x6F, 0xAF, 0x2F, 0xCF },
     { 0x08, 0x60, 0x68, 0x70, 0x40, 0x48, 0x50, 0x20, 0x28, 0x30 },
     { 0x60, 0x90, 0x80, 0x88, 0xB0, 0xA0, 0xA8, 0x30, 0x20, 0x28 },
     { 0x40, 0x44, 0x48, 0x4C, 0x50, 0x54, 0x58, 0x5C, 0x60, 0x64 },
     { 0x20, 0xC0, 0xC8, 0xD0, 0x60, 0x68, 0x70, 0x40, 0x48, 0x50 },
     { 0x20, 0x40, 0x48, 0x50, 0x60, 0x68, 0x70, 0x80, 0x88, 0x90 },
     { 0x2F, 0x7F, 0xBF, 0x3F, 0xDF, 0x5F, 0x9F, 0x1F, 0xEF, 0x6F },
     { 0x8C, 0xB4, 0x74, 0xF4, 0x94, 0x54, 0xD4, 0xA4, 0x64, 0xE4 },
     { 0xFE, 0x7E, 0xBE, 0x3E, 0xDE, 0x5E, 0x9E, 0x1E, 0xEE, 0x6E },
     { 0xFB, 0xBB, 0xDB, 0x9B, 0xEB, 0xAB, 0xCB, 0x8B, 0xF3, 0xB3 },
     { 0x00, 0xF8, 0xF4, 0xF0, 0xEC, 0xE8, 0xE4, 0x00, 0x00, 0x00 },
     { 0x26, 0x42, 0xC2, 0x22, 0xA2, 0x62, 0xE2, 0x12, 0x92, 0xC6 },
     { 0x6E, 0xFE, 0x7E, 0xBE, 0x3E, 0xDE, 0x5E, 0x9E, 0x1E, 0xEE },
     { 0x17, 0xE7, 0xD7, 0xF7, 0x67, 0x57, 0x77, 0xA7, 0x97, 0xB7 },
     { 0xED, 0x65, 0x75, 0xE5, 0x7D, 0xBD, 0x3D, 0xDD, 0x5D, 0x9D },
     { 0xFF, 0x7F, 0xBF, 0x3F, 0xEF, 0x6F, 0xAF, 0x2F, 0xF7, 0x77 },
     { 0x65, 0xF5, 0x75, 0xB5, 0x35, 0xD5, 0x55, 0x95, 0x15, 0xE5 },
     { 0x00, 0x75, 0xB5, 0x35, 0xD5, 0x55, 0x95, 0x00, 0x00, 0x00 },
     { 0x08, 0x88, 0x48, 0xC8, 0x28, 0xA8, 0x68, 0xE8, 0x18, 0x98 },
     { 0x50, 0x28, 0x48, 0x68, 0x88, 0x2C, 0x4C, 0x6C, 0x8C, 0x30 },
     { 0x6A, 0xFA, 0x7A, 0xBA, 0x3A, 0xDA, 0x5A, 0x9A, 0x1A, 0xEA },
     { 0x73, 0xEB, 0x6B, 0xAB, 0x2B, 0xCB, 0x4B, 0x8B, 0x0B, 0xF3 },
     { 0xB5, 0x6D, 0xAD, 0x2D, 0xCD, 0x4D, 0x8D, 0x0D, 0xF5, 0x75 },
     { 0x87, 0x5F, 0x9F, 0x1F, 0xDF, 0x9D, 0x5D, 0xE7, 0x67, 0xA7 },
     { 0x83, 0x73, 0xB3, 0x33, 0x53, 0x93, 0x13, 0x63, 0xA3, 0x23 },
     { 0xA7, 0x77, 0x8F, 0x0F, 0x57, 0xB7, 0x37, 0x67, 0x97, 0x17 }
   };
 
 }
