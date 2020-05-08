 package com.hifiremote.jp1;
 
 import java.util.*;
 import java.io.*;
 import java.text.*;
 import javax.swing.*;
 import java.awt.*;
 
 public class DeviceUpgrade
 {
   public DeviceUpgrade()
   {
     devTypeAliasName = deviceTypeAliasNames[ 0 ];
     initFunctions();
   }
 
   public void reset( Remote[] remotes, ProtocolManager protocolManager )
   {
     description = null;
     setupCode = 0;
 
     // remove all currently assigned functions
     Button[] buttons = remote.getButtons();
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
 
     DeviceParameter[] devParms = protocol.getDeviceParameters();
     for ( int i = 0; i < devParms.length; i++ )
       devParms[ i ].setValue( null );
 
     Vector names = protocolManager.getNames();
     Protocol tentative = null;
     for ( Enumeration e = names.elements(); e.hasMoreElements(); )
     {
       String protocolName = ( String )e.nextElement();
       Protocol p = protocolManager.findProtocolForRemote( remote, protocolName );
       if ( p != null )
       {
         protocol = p;
         break;
       }
     }
 
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
     if ( name != null )
     {
       if ( remote.getDeviceTypeByAliasName( name ) != null )
       {
         devTypeAliasName = name;
         return;
       }
       System.err.println( "Unable to find device type with alias name " + name );
     }
     devTypeAliasName = deviceTypeAliasNames[ 0 ];
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
 
   public File getFile(){ return file; }
 
   private int findDigitMapIndex()
   {
     Button[] buttons = remote.getUpgradeButtons();
     int[] digitMaps = remote.getDigitMaps();
     if (( digitMaps != null ) && ( protocol.getDefaultCmd().length() == 1 ))
     {
       for ( int i = 0; i < digitMaps.length; i++ )
       {
         int mapNum = digitMaps[ i ];
         int[] codes = DIGIT_MAP[ mapNum ];
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
     DeviceType devType = remote.getDeviceTypeByAliasName( devTypeAliasName );
     byte[] id = protocol.getID().getData();
     int temp = devType.getNumber() * 0x1000 +
                ( id[ 0 ] & 1 ) * 0x0800 +
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
 
     ButtonMap map = devType.getButtonMap();
     if ( map != null )
     {
       buff.append( ' ' );
       buff.append( Hex.toString( map.toBitMap( digitMapIndex != -1 )));
     }
 
     buff.append( ' ' );
     buff.append( protocol.getFixedData().toString());
 
     if ( map != null )
     {
       byte[] data = map.toCommandList( digitMapIndex != -1 );
       if (( data != null ) && ( data.length != 0 ))
       {
         buff.append( "\n " );
         buff.append( Hex.toString( data, 16 ));
       }
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
       if ((( f != null ) && (( map == null ) || !map.isPresent( b ) || f.isExternal())) ||
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
       {
         if ( token.equals( "true" ))
           val = new Integer( 1 );
         else if ( token.equals( "false" ))
           val = new Integer( 0 );
         else
           val = new Integer( token );
       }
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
     if ( protocol.getVariantName().length() > 0 )
       props.setProperty( "Protocol.variantName", protocol.getVariantName());
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
 
   public void load( File file, Remote[] remotes,
                     ProtocolManager protocolManager )
     throws Exception
   {
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
     setDeviceTypeAliasName( props.getProperty( "DeviceType" ) );
     setupCode = Integer.parseInt( props.getProperty( "SetupCode" ));
 
     Hex pid = new Hex( props.getProperty( "Protocol", "0200" ));
     String name = props.getProperty( "Protocol.name", "" );
     String variantName = props.getProperty( "Protocol.variantName", "" );
 
     String showV = (variantName.equals("")) ? "" : (": " + variantName);
 
     protocol = protocolManager.findNearestProtocol( name, pid, variantName );
 
     if ( protocol == null )
     {
       JOptionPane.showMessageDialog( null,
                                      "No protocol found with name=\"" + name +
                                      "\", ID=" + pid.toString() +
                                      ", and variantName=\"" + variantName + "\"",
                                      "File Load Error", JOptionPane.ERROR_MESSAGE );
       return;
     }
 
 //  int leastDifferent = Protocol.tooDifferent;
 //
 //  for ( Enumeration e = protocols.elements(); e.hasMoreElements(); )
 //  {
 //    Protocol tentative = ( Protocol )e.nextElement();
 //    int difference = tentative.different( props );
 //    if (difference < leastDifferent)
 //    {
 //      protocol = tentative;
 //      leastDifferent = difference;
 //      if ( difference == 0 )
 //        break;
 //    }
 //  }
 //  if ( leastDifferent == Protocol.tooDifferent )
 //  {
 //    JOptionPane.showMessageDialog( null,
 //                                   "No matching protocol for ID " + props.getProperty( "Protocol" ) + " was found!",
 //                                   "File Load Error", JOptionPane.ERROR_MESSAGE );
 //    return;
 //  }
 
     str = props.getProperty( "ProtocolParms" );
     if (( str != null ) && ( str.length() != 0 ))
       protocol.setDeviceParms( stringToValueArray( str ));
 
     notes = props.getProperty( "Notes" );
 
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
 
   private String getNextField( StringTokenizer st, String delim )
   {
     String rc = null;
     if ( st.hasMoreTokens())
     {
       rc = st.nextToken();
       if ( rc.equals( delim ))
         rc = null;
       else if ( st.hasMoreTokens())
         st.nextToken(); // skip delim
     }
     return rc;
   }
 
   public void importFile( File file, Remote[] remotes,
                     ProtocolManager protocolManager )
     throws Exception
   {
     System.err.println( "DeviceUpgrade.importFile()" );
     BufferedReader in = new BufferedReader( new FileReader( file ));
 
     String line = in.readLine();
     String token = line.substring( 0, 5 );
     if ( !token.equals( "Name:" ))
     {
       System.err.println( "The file \"" + file + "\" is not a valid KM upgrade file!" );
       // Bad file!
       return;
     }
     String delim = line.substring( 5, 6 );
     StringTokenizer st = new StringTokenizer( line, delim );
     st.nextToken();
     description = st.nextToken();
     
     String protocolLine = in.readLine();
     String manualLine = in.readLine();
     
     line = in.readLine();
     st = new StringTokenizer( line, delim );
     st.nextToken();
     token = st.nextToken();
     setupCode = Integer.parseInt( token );
     token = st.nextToken();
     String str = token.substring( 5 );
     
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
     
     token = st.nextToken();
     str = token.substring( 5 );
     
     setDeviceTypeAliasName( str );
 
     String buttonStyle = st.nextToken();
     st = new StringTokenizer( protocolLine, delim, true );
     st.nextToken(); // skip header
     st.nextToken(); // skip delim
     str = st.nextToken();  // protocol name
     st.nextToken(); // skip delim
     protocol = protocolManager.findProtocolForRemote( remote, str );
 
     if ( protocol == null )
     {
      JOptionPane.showMessageDialog( null,
                                     "No protocol found with name=\"" + str +
                                     "\" for remote \"" + remote.getName(),
                                     "Import Failure", JOptionPane.ERROR_MESSAGE );
      return;
     }
 
     DeviceParameter[] devParms = protocol.getDeviceParameters();
     for ( int i = 0; i < devParms.length; i++ )
     {
       // Skip over Flag parms because KM didn't have these.
       if ( devParms[ i ].getClass() == FlagDeviceParm.class )
         continue;
 
       token = st.nextToken();
       Object val = null;
       if ( token.equals( delim ))
         val = null;
       else
       {
         st.nextToken(); // skip delim
         if ( token.equals( "true" ))
           val = new Integer( 1 );
         else if ( token.equals( "false" ))
           val = new Integer( 0 );
         else
           val = new Integer( token );
       }
       devParms[ i ].setValue( val );
     }
 
     for ( int i = 5; i < 35; i++ )
       in.readLine();
 
     // compute cmdIndex
     int cmdIndex = -1;
     boolean useOBC = false;
     boolean useEFC = false;
     if ( buttonStyle.equals( "OBC" ))
     {
       useOBC = true;
       CmdParameter[] cmdParms = protocol.getCommandParameters();
       for ( int j = 0; j < cmdParms.length; j++ )
       {
         if ( cmdParms[ j ].getName().equals( "OBC" ))
         {
           cmdIndex = j;
           break;
         }
       }
     }
     else if ( buttonStyle.equals( "EFC" ))
       useEFC = true;
 
     functions.clear();
 
     Vector unassigned = new Vector();
     Vector usedFunctions = new Vector();
     for ( int i = 0; i < 128; i++ )
     {
       line = in.readLine();
       st = new StringTokenizer( line, delim, true );
       token = getNextField( st, delim ); // get the name (field 1)
       if (( token != null ) && ( token.length() == 5 ) &&
           token.startsWith( "num " ) && Character.isDigit( token.charAt( 4 )))
         token = token.substring( 4 ); 
 
       Function f = getFunction( token, usedFunctions );
       if ( f == null )
       {
         f = new Function();
         f.setName( token );
       }
 
       token = getNextField( st, delim );  // get the function code (field 2)
       if ( token != null )
       {
         if ( useOBC )
         {
           Hex hex = protocol.getDefaultCmd();
           f.setHex( hex );
           protocol.setValueAt( cmdIndex, hex, new Integer( token ));
           token = getNextField( st, delim ); // get byte2 (field 3)
           if ( token != null )
           {
             if ( cmdIndex > 0 )
               protocol.setValueAt( cmdIndex - 1, hex, new Integer( token ));
           }
         }
         else if ( useEFC )
         {
           EFC efc = new EFC( token );
           Hex hex = protocol.efc2hex( efc, null );
           f.setHex( hex );
           token = getNextField( st, delim ); // get byte2 (field 3 )
         }
       }
       else
       {
         token = getNextField( st, delim ); // skip field 3
       }
       String buttonName = getNextField( st, delim ); // get assigned button name (field 4)
       if (( buttonName != null ) && buttonName.length() == 0 )
         buttonName = null;
       Button b = null;
       if ( buttonName != null )
         b = remote.findByStandardName( new Button( buttonName, null, ( byte )0 ));
       
       token = getNextField( st, delim );  // get normal function (field 5)
       if (( buttonName != null ) && ( token != null ) &&
            Character.isDigit( token.charAt( 0 )) && 
            Character.isDigit( token.charAt( 1 )) &&
            ( token.charAt( 2 ) == ' ' ) &&
            ( token.charAt( 3 ) == '-' ) &&
            ( token.charAt( 4 ) == ' ' ))
       {
         String name = token.substring( 5 );
         if (( name.length() == 5 ) && name.startsWith( "num " ) && 
               Character.isDigit( name.charAt( 4 )))
           name = name.substring( 4 );
         
         Function func = null;
         if (( f.getName() != null ) && f.getName().equals( name ))
           func = f;
         else
           func = getFunction( name, functions );
         if ( func == null )
         {
           func = new Function();
           func.setName( name );
           usedFunctions.add( func );
         }
 
         if ( b == null )
         {
           Vector temp = new Vector( 2 );
           temp.add( name );
           temp.add( buttonName );
           unassigned.add( temp );
         }
         else
           b.setFunction( func );
       }
       
       token = getNextField( st, delim );  // get notes (field 6)
       if ( token != null )
         f.setNotes( token );
       
       if ( !f.isEmpty())
         functions.add( f );
       
       // skip to field 13
       for ( int j = 7; j <= 13; j++ )
         token = getNextField( st, delim ); 
       
       if ( token != null )
       {
         String name = token.substring( 5 );
         if (( name.length() == 5 ) && name.startsWith( "num " ) && 
               Character.isDigit( token.charAt( 4 )))
           name = name.substring( 4 ); 
         Function func = getFunction( name, functions );
         if ( func == null )
         {
           func = new Function();
           func.setName( name );
           usedFunctions.add( func );
         }
         if ( b == null )
         {
           Vector temp = new Vector( 2 );
           temp.add( name );
           temp.add( "shift-" + buttonName );
           unassigned.add( temp );
         }
         else
           b.setShiftedFunction( func );
       }
     }
     
     while (( line = in.readLine()) != null )
     {
       line = in.readLine();
       st = new StringTokenizer( line, delim );
       token = getNextField( st, delim );
       if ( token != null )
       {
         if ( token.equals( "Line Notes:" ) || token.equals( "Notes:" ))
         {
           StringBuffer buff = new StringBuffer();
           boolean first = true;
           while (( line = in.readLine()) != null )
           {
             st = new StringTokenizer( line, delim );
             if ( st.hasMoreTokens())
             {
               token = st.nextToken();
               if ( token.startsWith( "EOF Marker" ))
                 break;
               if ( first )
                 first = false;
               else
                 buff.append( "\n" );
               buff.append( token );
             }
             else
               buff.append( "\n" );
           }
           notes = buff.toString().trim();
         }
       }
     }
     if ( !unassigned.isEmpty())
     {
       String message = "Some of the functions defined in the imported device upgrade " +
                        "were assigned to buttons that could not be matched by name. " + 
                        "The functions and the corresponding button names are listed below." + 
                        "\n\nUse the Button or Layout panel to assign those functions properly.";
 
       JFrame frame = new JFrame( "Import Failure" );
       Container container = frame.getContentPane();
 
       JTextArea text = new JTextArea( message );
       text.setEditable( false );
       text.setLineWrap( true );
       text.setWrapStyleWord( true );
       text.setBackground( container.getBackground() );
       container.add( text, BorderLayout.NORTH );
       Vector titles = new Vector();
       titles.add( "Function name" );
       titles.add( "Button name" );
       JTable table = new JTable( unassigned, titles );
       container.add( new JScrollPane( table ), BorderLayout.CENTER );
       frame.pack();
       frame.show();
     }
   }
 
   public static final String[] getDeviceTypeAliasNames()
   {
     return deviceTypeAliasNames;
   }
 
   public void autoAssignFunctions()
   {
     autoAssignFunctions( functions );
     autoAssignFunctions( extFunctions );
   }
 
   private void autoAssignFunctions( Vector funcs )
   {
     Button[] buttons = remote.getUpgradeButtons();
     for ( Enumeration e = funcs.elements(); e.hasMoreElements(); )
     {
       Function func = ( Function )e.nextElement();
       if ( func.getHex() != null )
       {
         for ( int i = 0; i < buttons.length; i++ )
         {
           Button b = buttons[ i ];
           if ( b.getFunction() == null )
           {
             if ( b.getName().equalsIgnoreCase( func.getName()) ||
                  b.getStandardName().equalsIgnoreCase( func.getName()))
             {
               b.setFunction( func );
               break;
             }
           }
         }
       }
     }
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
     { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 }, // 0
     { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09 },
     { 0x00, 0x01, 0x03, 0x02, 0x06, 0x07, 0x05, 0x04, 0x0C, 0x0D },
     { 0x00, 0x80, 0x40, 0xC0, 0x20, 0xA0, 0x60, 0xE0, 0x10, 0x90 },
     { 0x00, 0xF4, 0x74, 0xB4, 0x34, 0xD4, 0x54, 0x94, 0x14, 0xE4 },
     { 0x04, 0x00, 0x80, 0x40, 0xC0, 0x20, 0xA0, 0x60, 0xE0, 0x10 },
     { 0x04, 0x44, 0x24, 0x64, 0x14, 0x54, 0x34, 0x74, 0x0C, 0x4C },
     { 0x09, 0x89, 0x49, 0xC9, 0x29, 0xA9, 0x69, 0xE9, 0x19, 0x99 },
     { 0x0F, 0x3F, 0xBF, 0x7F, 0x1F, 0x9F, 0x5F, 0x2F, 0xAF, 0x6F },
     { 0x17, 0x16, 0x1B, 0x1A, 0x23, 0x22, 0x2F, 0x2E, 0x27, 0x26 },
     { 0x17, 0x8F, 0x0F, 0xF7, 0x77, 0xB7, 0x37, 0xD7, 0x57, 0x97 }, // 10
     { 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39 },
     { 0x31, 0x79, 0x39, 0x59, 0x19, 0x69, 0x29, 0x49, 0x09, 0x71 },
     { 0x33, 0x7B, 0xBB, 0x3B, 0xDB, 0x5B, 0x9B, 0x1B, 0xEB, 0x6B },
     { 0x33, 0xF3, 0x6B, 0x63, 0xEB, 0xFB, 0xE3, 0xB3, 0xAB, 0xBB },
     { 0x40, 0xF0, 0x70, 0xB0, 0xD0, 0x50, 0x90, 0xE0, 0x60, 0xA0 },
     { 0x44, 0x10, 0x14, 0x18, 0x20, 0x24, 0x28, 0x30, 0x34, 0x38 },
     { 0x47, 0xDF, 0x5F, 0x9F, 0x1F, 0xCF, 0x4F, 0x8F, 0x0F, 0xC7 },
     { 0x4F, 0x8F, 0x0F, 0xC7, 0x47, 0x87, 0x07, 0xDF, 0x5F, 0xCF },
     { 0x4F, 0xE7, 0x67, 0xA7, 0xD7, 0x57, 0x97, 0xF7, 0x77, 0xB7 },
     { 0x4F, 0xFF, 0x7F, 0xBF, 0xDF, 0x5F, 0x9F, 0xEF, 0x6F, 0xAF }, // 20
     { 0x50, 0x80, 0x40, 0xC0, 0x20, 0xA0, 0x60, 0xE0, 0x10, 0x90 },
     { 0x5A, 0xDE, 0x5C, 0x9C, 0x1E, 0xD4, 0x56, 0x96, 0x14, 0xD8 },
     { 0x5D, 0x8F, 0x4F, 0xCF, 0x95, 0x55, 0xD5, 0x8D, 0x4D, 0xCD },
     { 0x5F, 0xF7, 0x77, 0xB7, 0xCF, 0x4F, 0x8F, 0xEF, 0x6F, 0xAF },
     { 0x64, 0x68, 0x6C, 0x70, 0x48, 0x4C, 0x50, 0x28, 0x2C, 0x30 },
     { 0x64, 0xF4, 0x74, 0xB4, 0x34, 0xD4, 0x54, 0x94, 0x14, 0xE4 },
     { 0x66, 0xF6, 0x76, 0xB6, 0x36, 0xD6, 0x56, 0x96, 0x16, 0xE6 },
     { 0x67, 0xCF, 0x4F, 0x8F, 0xF7, 0x77, 0xB7, 0xD7, 0x57, 0x97 },
     { 0x67, 0xF7, 0x77, 0xB7, 0x37, 0xD7, 0x57, 0x97, 0x17, 0xE7 },
     { 0x68, 0x04, 0x78, 0xB8, 0x38, 0xD8, 0x58, 0x98, 0x18, 0xE8 }, // 30
     { 0x6D, 0xFD, 0x7D, 0xBD, 0x3D, 0xDD, 0x5D, 0x9D, 0x1D, 0xED },
     { 0x6F, 0xFF, 0x7F, 0xBF, 0x3F, 0xDF, 0x5F, 0x9F, 0x1F, 0xEF },
     { 0x6F, 0xFF, 0xEF, 0xF7, 0xE7, 0xFB, 0xEB, 0xF3, 0xE3, 0x7F },
     { 0x77, 0xDF, 0x5F, 0x9F, 0xEF, 0x6F, 0xAF, 0xCF, 0x4F, 0x8F },
     { 0x77, 0xFF, 0x5F, 0x9F, 0xDF, 0x6F, 0xAF, 0xEF, 0x4F, 0x8F },
     { 0x80, 0x81, 0x83, 0x82, 0x86, 0x87, 0x85, 0x84, 0x8C, 0x8D },
     { 0x80, 0xC0, 0xA0, 0xE0, 0x90, 0xD0, 0xB0, 0xF0, 0x88, 0xC8 },
     { 0x84, 0xC4, 0xA4, 0xE4, 0x94, 0xD4, 0xB4, 0xF4, 0x8C, 0xCC },
     { 0x86, 0xFA, 0xBA, 0xDA, 0x9A, 0xEA, 0xAA, 0xCA, 0x8A, 0xF2 },
     { 0x87, 0x37, 0xB7, 0x77, 0x17, 0x97, 0x57, 0x27, 0xA7, 0x67 }, // 40
     { 0x87, 0x6F, 0xE7, 0xF7, 0x27, 0x57, 0x77, 0x97, 0x67, 0x17 },
     { 0x88, 0x20, 0x28, 0x30, 0x40, 0x48, 0x50, 0x60, 0x68, 0x70 },
     { 0x8F, 0x3F, 0xBF, 0x7F, 0x1F, 0x9F, 0x5F, 0x2F, 0xAF, 0x6F },
     { 0x8F, 0xBF, 0x37, 0xCF, 0x97, 0xD7, 0x17, 0xE7, 0x4F, 0x67 },
     { 0x8F, 0xDF, 0x5F, 0x9F, 0x1F, 0xEF, 0x6F, 0xAF, 0x2F, 0xCF },
     { 0x90, 0x00, 0x80, 0x40, 0xC0, 0x20, 0xA0, 0x60, 0xE0, 0x10 },
     { 0x90, 0x20, 0x28, 0x30, 0x40, 0x48, 0x50, 0x60, 0x68, 0x70 },
     { 0x90, 0xB8, 0xB0, 0xA8, 0xD8, 0xD0, 0xC8, 0xF8, 0xF0, 0xE8 },
     { 0x97, 0x17, 0xE7, 0x67, 0xA7, 0x27, 0xC7, 0x47, 0x87, 0x07 },
     { 0x97, 0xB7, 0x37, 0x77, 0xF7, 0x0F, 0xE7, 0x17, 0xD7, 0x57 }, // 50
     { 0x98, 0xBC, 0xB8, 0xB4, 0xB0, 0xAC, 0xA8, 0xA4, 0xA0, 0x9C }, 
     { 0x98, 0xDC, 0x5E, 0x9E, 0x1C, 0xD6, 0x54, 0x94, 0x16, 0xDA },
     { 0x9B, 0x0B, 0x8B, 0x4B, 0xCB, 0x2B, 0xAB, 0x6B, 0xEB, 0x1B },
     { 0x9C, 0x92, 0xA2, 0x82, 0x9A, 0xAA, 0x8A, 0x96, 0xA6, 0x86 },
     { 0x9F, 0x07, 0x87, 0x47, 0xC7, 0x0F, 0x8F, 0x4F, 0xCF, 0x1F },
     { 0x9F, 0x4F, 0x8F, 0x0F, 0xC7, 0x47, 0x87, 0x07, 0xDF, 0x5F },
     { 0xA4, 0x74, 0xB4, 0x34, 0xD4, 0x54, 0x94, 0x14, 0xE4, 0x64 },
     { 0xA7, 0x77, 0xB7, 0x37, 0xD7, 0x57, 0x97, 0x17, 0xE7, 0x67 },
     { 0xA7, 0xF7, 0x77, 0xB7, 0x37, 0xD7, 0x57, 0x97, 0x17, 0xE7 },
     { 0xAC, 0x40, 0xA0, 0xC0, 0x44, 0xA4, 0xC4, 0x48, 0xA8, 0xC8 }, // 60
     { 0xAC, 0x98, 0x88, 0x90, 0xB8, 0xA8, 0xB0, 0x9C, 0x8C, 0x94 }, 
     { 0xAC, 0xD8, 0xB4, 0x70, 0xD4, 0xB8, 0x8C, 0xF0, 0xA8, 0x94 },
     { 0xAE, 0x7E, 0xBE, 0x3E, 0xDE, 0x5E, 0x9E, 0x1E, 0xEE, 0x6E },
     { 0xAF, 0x7F, 0xBF, 0x3F, 0xDF, 0x5F, 0x9F, 0x1F, 0xEF, 0x6F },
     { 0xAF, 0xFF, 0x7F, 0xBF, 0x3F, 0xDF, 0x5F, 0x9F, 0x1F, 0xEF },
     { 0xB0, 0x20, 0xA0, 0x60, 0xE0, 0x10, 0x90, 0x50, 0xD0, 0x30 },
     { 0xB4, 0xDC, 0xD8, 0xC0, 0xBC, 0xB8, 0xD4, 0xCC, 0xC8, 0xC4 },
     { 0xB6, 0x1E, 0x9E, 0x5E, 0x2E, 0xAE, 0x6E, 0x0E, 0x8E, 0x4E },
     { 0xB7, 0x6F, 0x47, 0x07, 0x4F, 0x67, 0x27, 0x77, 0x57, 0x17 },
     { 0xB7, 0x7F, 0x9F, 0x1F, 0x5F, 0xAF, 0x2F, 0x6F, 0x8F, 0x0F }, // 70
     { 0xBC, 0xB8, 0xB4, 0xB0, 0xAC, 0xA8, 0xA4, 0xA0, 0x9C, 0x98 }, 
     { 0xC0, 0xC4, 0xC8, 0xCC, 0xD0, 0xD4, 0xD8, 0xDC, 0xE0, 0xE4 },
     { 0xC4, 0xA0, 0xA2, 0xA4, 0xA8, 0xAA, 0xB0, 0xB2, 0xB4, 0xC0 },
     { 0xC4, 0xFC, 0xDC, 0xBC, 0xF8, 0xD8, 0xB8, 0xF4, 0xD4, 0xB4 },
     { 0xCF, 0x4F, 0x8F, 0x0F, 0xC7, 0x47, 0x87, 0x07, 0xDF, 0x5F },
     { 0xCF, 0xCE, 0xCD, 0xCC, 0xCB, 0xCA, 0xC9, 0xC8, 0xC7, 0xC6 },
     { 0xD2, 0x42, 0xC2, 0x22, 0xA2, 0x62, 0xE2, 0x12, 0x92, 0x52 },
     { 0xD4, 0xF8, 0xF4, 0xF0, 0xEC, 0xE8, 0xE4, 0xE0, 0xDC, 0xD8 },
     { 0xD8, 0xE8, 0xEC, 0xF0, 0xF4, 0xF8, 0xC8, 0xCC, 0xD0, 0xD4 },
     { 0xDF, 0x5F, 0x9F, 0x1F, 0xCF, 0x4F, 0x8F, 0x0F, 0xC7, 0x47 }, // 80
     { 0xDF, 0x5F, 0x9F, 0x1F, 0xEF, 0x6F, 0xAF, 0x2F, 0xCF, 0x4F },
     { 0xE4, 0xEC, 0xE4, 0xCC, 0xF0, 0xD0, 0xC8, 0xD0, 0xF8, 0xDC },
     { 0xEF, 0xFF, 0xBF, 0x3F, 0x57, 0x67, 0x2F, 0xD7, 0xE7, 0x37 },
     { 0xF4, 0x74, 0xB4, 0x34, 0xD4, 0x54, 0x94, 0x14, 0xE4, 0x64 },
     { 0xF6, 0x76, 0xB6, 0x36, 0xD6, 0x56, 0x96, 0x16, 0xE6, 0x66 },
     { 0xF7, 0x77, 0xB7, 0x37, 0xD7, 0x57, 0x97, 0x17, 0xE7, 0x67 },
     { 0xF9, 0x79, 0xB9, 0x39, 0xD9, 0x59, 0x99, 0x19, 0xE9, 0x69 },
     { 0xFA, 0x7A, 0xBA, 0x3A, 0xDA, 0x5A, 0x9A, 0x1A, 0xEA, 0x6A },
     { 0xFA, 0xBA, 0xDA, 0x9A, 0xEA, 0xAA, 0xCA, 0x8A, 0xF2, 0xB2 },
     { 0xFB, 0x7B, 0xBB, 0x3B, 0xDB, 0x5B, 0x9B, 0x1B, 0xEB, 0x6B }, // 90
     { 0xFC, 0x7C, 0xBC, 0x3C, 0xDC, 0x5C, 0x9C, 0x1C, 0xEC, 0x6C },
     { 0xFC, 0xF8, 0xF4, 0xF0, 0xEC, 0xE8, 0xE4, 0xE0, 0xDC, 0xD8 },
     { 0xFD, 0x7D, 0xBD, 0x3D, 0xDD, 0x5D, 0x9D, 0x1D, 0xED, 0x6D },
     { 0xFF, 0x7F, 0xBF, 0x3F, 0xDF, 0x5F, 0x9F, 0x1F, 0xEF, 0x6F },
     { 0xFF, 0xFE, 0xFD, 0xFC, 0xFB, 0xFA, 0xF9, 0xF8, 0xF7, 0xF6 },
     { 0x07, 0x0F, 0x17, 0x1F, 0x27, 0x2F, 0x37, 0x3F, 0x47, 0x4F },
     { 0x00, 0x20, 0x40, 0x60, 0x80, 0x88, 0x68, 0x48, 0x28, 0x08 },
     { 0xB4, 0xF4, 0x8C, 0xCC, 0xAC, 0xEC, 0x9C, 0xDC, 0xBC, 0xFC },
     { 0x96, 0xCE, 0x4E, 0x8E, 0x0E, 0xF6, 0x76, 0xB6, 0x36, 0x56 },
     { 0x17, 0xE7, 0xD7, 0xF7, 0x67, 0x57, 0x77, 0xA7, 0x97, 0xB7 }, // 100
     { 0xFF, 0x7F, 0xBF, 0x3F, 0xEF, 0x6F, 0xAF, 0x2F, 0xF7, 0x77 },
     { 0x17, 0xE7, 0xD7, 0xF7, 0x67, 0x57, 0x77, 0xA7, 0x97, 0xB7 },
     { 0xEC, 0x48, 0x4C, 0xC8, 0x88, 0xF0, 0x68, 0x28, 0xCC, 0x8C },
     { 0xC4, 0xFC, 0xDC, 0xBC, 0xEC, 0xCC, 0xAC, 0xE8, 0xC8, 0xA8 },
     { 0x58, 0xDC, 0x5E, 0x9E, 0x1C, 0xD6, 0x54, 0x94, 0x16, 0xDA },
     { 0x4F, 0xDF, 0x5F, 0x9F, 0x1F, 0xEF, 0x6F, 0xAF, 0x2F, 0xCF },
     { 0x08, 0x60, 0x68, 0x70, 0x40, 0x48, 0x50, 0x20, 0x28, 0x30 },
     { 0x60, 0x90, 0x80, 0x88, 0xB0, 0xA0, 0xA8, 0x30, 0x20, 0x28 },
     { 0x40, 0x44, 0x48, 0x4C, 0x50, 0x54, 0x58, 0x5C, 0x60, 0x64 },
     { 0x20, 0xC0, 0xC8, 0xD0, 0x60, 0x68, 0x70, 0x40, 0x48, 0x50 }, // 110
     { 0x20, 0x40, 0x48, 0x50, 0x60, 0x68, 0x70, 0x80, 0x88, 0x90 },
     { 0x2F, 0x7F, 0xBF, 0x3F, 0xDF, 0x5F, 0x9F, 0x1F, 0xEF, 0x6F },
     { 0x8C, 0xB4, 0x74, 0xF4, 0x94, 0x54, 0xD4, 0xA4, 0x64, 0xE4 }, 
     { 0xFE, 0x7E, 0xBE, 0x3E, 0xDE, 0x5E, 0x9E, 0x1E, 0xEE, 0x6E },
     { 0xFB, 0xBB, 0xDB, 0x9B, 0xEB, 0xAB, 0xCB, 0x8B, 0xF3, 0xB3 },
     { 0x00, 0xF8, 0xF4, 0xF0, 0xEC, 0xE8, 0xE4, 0x00, 0x00, 0x00 },
     { 0x26, 0x42, 0xC2, 0x22, 0xA2, 0x62, 0xE2, 0x12, 0x92, 0xC6 },
     { 0x6E, 0xFE, 0x7E, 0xBE, 0x3E, 0xDE, 0x5E, 0x9E, 0x1E, 0xEE },
     { 0x17, 0xE7, 0xD7, 0xF7, 0x67, 0x57, 0x77, 0xA7, 0x97, 0xB7 },
     { 0xED, 0x65, 0x75, 0xE5, 0x7D, 0xBD, 0x3D, 0xDD, 0x5D, 0x9D }, // 120
     { 0xFF, 0x7F, 0xBF, 0x3F, 0xEF, 0x6F, 0xAF, 0x2F, 0xF7, 0x77 },
     { 0x65, 0xF5, 0x75, 0xB5, 0x35, 0xD5, 0x55, 0x95, 0x15, 0xE5 },
     { 0x00, 0x75, 0xB5, 0x35, 0xD5, 0x55, 0x95, 0x00, 0x00, 0x00 }, 
     { 0x08, 0x88, 0x48, 0xC8, 0x28, 0xA8, 0x68, 0xE8, 0x18, 0x98 },
     { 0x50, 0x28, 0x48, 0x68, 0x88, 0x2C, 0x4C, 0x6C, 0x8C, 0x30 },
     { 0x6A, 0xFA, 0x7A, 0xBA, 0x3A, 0xDA, 0x5A, 0x9A, 0x1A, 0xEA },
     { 0x73, 0xEB, 0x6B, 0xAB, 0x2B, 0xCB, 0x4B, 0x8B, 0x0B, 0xF3 },
     { 0xB5, 0x6D, 0xAD, 0x2D, 0xCD, 0x4D, 0x8D, 0x0D, 0xF5, 0x75 },
     { 0x87, 0x5F, 0x9F, 0x1F, 0xDF, 0x9D, 0x5D, 0xE7, 0x67, 0xA7 },
     { 0x83, 0x73, 0xB3, 0x33, 0x53, 0x93, 0x13, 0x63, 0xA3, 0x23 }, // 130
     { 0xA7, 0x77, 0x8F, 0x0F, 0x57, 0xB7, 0x37, 0x67, 0x97, 0x17 },
     { 0x57, 0x49, 0x51, 0x41, 0x4D, 0x55, 0x45, 0x4B, 0x53, 0x43 },
     { 0x5C, 0x54, 0x5C, 0x74, 0x48, 0x68, 0x70, 0x68, 0x40, 0x64 }, 
     { 0xB8, 0xB0, 0xB8, 0x90, 0xAC, 0x8C, 0x94, 0x8C, 0xA4, 0x80 },
     { 0x00, 0x08, 0x00, 0x28, 0x14, 0x34, 0x2C, 0x34, 0x1C, 0x38 },
     { 0x22, 0xFF, 0xEE, 0xDD, 0xBB, 0xAA, 0x99, 0x77, 0x66, 0x55 },
     { 0xFB, 0xBF, 0xDF, 0x9F, 0xEF, 0xAF, 0xCF, 0x8F, 0xF7, 0xB7 },
     { 0xF0, 0xE1, 0xD2, 0xC3, 0xB4, 0xA5, 0x96, 0x87, 0x78, 0x69 },
     { 0x6C, 0xFC, 0x7C, 0xBC, 0x3C, 0xDC, 0x5C, 0x9C, 0x1C, 0xEC },
     { 0xE2, 0xFE, 0xEE, 0xDE, 0xFC, 0xEC, 0xDC, 0xFA, 0xEA, 0xDA }, // 140
     { 0x6D, 0x37, 0xF7, 0x77, 0x0F, 0xCF, 0x4F, 0x2F, 0xEF, 0x6F },
     { 0xF8, 0xB0, 0xF0, 0x90, 0xD0, 0x88, 0xC8, 0xA8, 0xE8, 0xB8 },
     { 0x80, 0x80, 0xA0, 0xB0, 0xA8, 0xB8, 0xA4, 0xB4, 0xAC, 0xBC }, 
     { 0xA0, 0x90, 0x88, 0x84, 0x82, 0x81, 0x60, 0x50, 0x48, 0x44 },
     { 0x36, 0xD6, 0x56, 0x96, 0x16, 0xE6, 0x66, 0xA6, 0x26, 0xC6 },
     { 0xF4, 0xD4, 0xB4, 0x94, 0x74, 0x54, 0x34, 0x14, 0x38, 0x18 },
     { 0x00, 0xB5, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 },
     { 0xDE, 0x5E, 0x9E, 0x1E, 0xEE, 0x6E, 0xAE, 0x2E, 0xCE, 0x4E },
     { 0xF8, 0xB0, 0xF0, 0x90, 0xD0, 0x88, 0xC8, 0xA8, 0xE8, 0xB8 },
     { 0x1F, 0x07, 0x5F, 0x57, 0x4F, 0x47, 0x3F, 0x37, 0x2F, 0x27 },  // 150
     { 0xCB, 0xFD, 0xC3, 0xA3, 0xE3, 0x93, 0xD3, 0xB3, 0xF3, 0x8B },
     { 0xC9, 0x81, 0xC1, 0xA1, 0xE1, 0x91, 0xD1, 0xB1, 0xF1, 0x89 },
     { 0x81, 0xC1, 0xA1, 0xE1, 0x91, 0xD1, 0xB1, 0xF1, 0x89, 0xC9 }
   };
 
 }
