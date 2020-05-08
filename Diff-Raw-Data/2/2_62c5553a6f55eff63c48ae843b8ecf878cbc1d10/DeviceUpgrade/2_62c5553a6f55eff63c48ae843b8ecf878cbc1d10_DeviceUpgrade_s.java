 package com.hifiremote.jp1;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.beans.PropertyChangeListener;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.text.DecimalFormat;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Properties;
 import java.util.StringTokenizer;
 
 import javax.swing.BorderFactory;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.WindowConstants;
 import javax.swing.event.SwingPropertyChangeSupport;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class DeviceUpgrade.
  */
 public class DeviceUpgrade
 {
 
   /**
    * Instantiates a new device upgrade.
    */
   public DeviceUpgrade()
   {
     this( ( String[] )null );
   }
 
   /**
    * Instantiates a new device upgrade.
    * 
    * @param defaultNames
    *          the default names
    */
   public DeviceUpgrade( String[] defaultNames )
   {
     devTypeAliasName = deviceTypeAliasNames[ 0 ];
     initFunctions( defaultNames );
   }
 
   /**
    * Instantiates a new device upgrade.
    * 
    * @param base
    *          the base
    */
   public DeviceUpgrade( DeviceUpgrade base )
   {
     description = base.description;
     setupCode = base.setupCode;
     devTypeAliasName = base.devTypeAliasName;
     remote = base.remote;
     notes = base.notes;
     protocol = base.protocol;
 
     // copy the device parameter values
     protocol.setDeviceParms( base.parmValues );
     parmValues = protocol.getDeviceParmValues();
 
     // Copy the functions and their assignments
     for ( Function f : base.functions )
     {
       Function f2 = new Function( f );
       functions.add( f2 );
       for ( Function.User user : f.getUsers() )
         assignments.assign( user.button, f2, user.state );
     }
 
     // Copy the external functions and their assignments
     for ( ExternalFunction f : base.extFunctions )
     {
       ExternalFunction f2 = new ExternalFunction( f );
       extFunctions.add( f2 );
       for ( Function.User user : f.getUsers() )
         assignments.assign( user.button, f2, user.state );
     }
   }
 
   /**
    * Reset.
    */
   public void reset()
   {
     reset( defaultNames );
   }
 
   /**
    * Reset.
    * 
    * @param defaultNames
    *          the default names
    */
   public void reset( String[] defaultNames )
   {
     description = null;
     setupCode = 0;
 
     // remove all currently assigned functions
     if ( remote != null )
       assignments.clear();
 
     Collection< Remote > remotes = RemoteManager.getRemoteManager().getRemotes();
     if ( remote == null )
       remote = remotes.iterator().next();
     devTypeAliasName = deviceTypeAliasNames[ 0 ];
 
     if ( protocol != null )
       protocol.reset();
     ProtocolManager pm = ProtocolManager.getProtocolManager();
     java.util.List< String > names = pm.getNames();
     for ( String protocolName : names )
     {
       Protocol p = pm.findProtocolForRemote( remote, protocolName );
       if ( p != null )
       {
         protocol = p;
         break;
       }
     }
 
     if ( protocol != null )
     {
       DeviceParameter[] devParms = protocol.getDeviceParameters();
       for ( int i = 0; i < devParms.length; i++ )
         devParms[ i ].setValue( null );
       setProtocol( protocol );
     }
 
     notes = null;
     file = null;
 
     functions.clear();
     initFunctions( defaultNames );
 
     extFunctions.clear();
   }
 
   /**
    * Inits the functions.
    * 
    * @param names
    *          the names
    */
   private void initFunctions( String[] names )
   {
     defaultNames = names;
     if ( defaultNames == null )
       defaultNames = defaultFunctionNames;
     for ( int i = 0; i < defaultNames.length; i++ )
       functions.add( new Function( defaultNames[ i ] ) );
   }
 
   /**
    * Sets the description.
    * 
    * @param text
    *          the new description
    */
   public void setDescription( String text )
   {
     description = text;
   }
 
   /**
    * Gets the description.
    * 
    * @return the description
    */
   public String getDescription()
   {
     return description;
   }
 
   /**
    * Sets the setup code.
    * 
    * @param setupCode
    *          the new setup code
    */
   public void setSetupCode( int setupCode )
   {
     int oldSetupCode = this.setupCode;
     this.setupCode = setupCode;
     propertyChangeSupport.firePropertyChange( "setupCode", oldSetupCode, setupCode );
   }
 
   /**
    * Gets the setup code.
    * 
    * @return the setup code
    */
   public int getSetupCode()
   {
     return setupCode;
   }
 
   /**
    * Checks for defined functions.
    * 
    * @return true, if successful
    */
   public boolean hasDefinedFunctions()
   {
     for ( Function func : functions )
     {
       if ( func.getHex() != null )
         return true;
     }
     for ( Function func : extFunctions )
     {
       if ( func.getHex() != null )
         return true;
     }
     return false;
   }
 
   /**
    * Sets the remote.
    * 
    * @param newRemote
    *          the new remote
    */
   public void setRemote( Remote newRemote )
   {
     Protocol p = protocol;
     ProtocolManager pm = ProtocolManager.getProtocolManager();
     java.util.List< Protocol > protocols = pm.getProtocolsForRemote( newRemote, false );
     if ( p == null )
       protocol = protocols.get( 0 );
     else if ( !protocols.contains( p ) )
     {
       System.err.println( "DeviceUpgrade.setRemote(), protocol " + p.getDiagnosticName() + " is not built into remote "
           + newRemote.getName() );
       Protocol newp = pm.findProtocolForRemote( newRemote, p.getName() );
 
       if ( newp != null )
       {
         if ( newp != p )
         {
           System.err.println( "Testing if protocol " + newp.getDiagnosticName() + " can be used." );
           System.err.println( "\tChecking for matching dev. parms" );
           DeviceParameter[] parms = p.getDeviceParameters();
           DeviceParameter[] parms2 = newp.getDeviceParameters();
 
           int[] map = new int[ parms.length ];
           boolean parmsMatch = true;
           for ( int i = 0; i < parms.length; i++ )
           {
             String name = parms[ i ].getName();
             System.err.print( "\tchecking " + name );
             boolean nameMatch = false;
             for ( int j = 0; j < parms2.length; j++ )
             {
               if ( name.equals( parms2[ j ].getName() ) )
               {
                 map[ i ] = j;
                 nameMatch = true;
                 System.err.print( " has a match!" );
                 break;
               }
             }
             if ( !nameMatch )
             {
               Object v = parms[ i ].getValue();
               Object d = parms[ i ].getDefaultValue();
               if ( d != null )
                 d = ( ( DefaultValue )d ).value();
               System.err.print( " no match!" );
 
               if ( ( v == null ) || ( v.equals( d ) ) )
               {
                 nameMatch = true;
                 map[ i ] = -1;
                 System.err.print( " But there's no value anyway!  " );
               }
             }
             System.err.println();
             parmsMatch = nameMatch;
             if ( !parmsMatch )
               break;
           }
           if ( parmsMatch )
           {
             // copy parameters from p to p2!
             Value[] vals = new Value[ parms2.length ];
             System.err.println( "\tCopying dev. parms" );
             for ( int i = 0; i < map.length; i++ )
             {
               if ( map[ i ] == -1 )
                 continue;
               System.err.println( "\tfrom index " + i + " (=" + parms[ i ].getValue() + ") to index " + map[ i ] );
               parms2[ map[ i ] ].setValue( parms[ i ].getValue() );
               vals[ map[ i ] ] = new Value( parms[ i ].getValue() );
             }
             newp.setDeviceParms( vals );
             System.err.println();
             System.err.println( "Protocol " + newp.getDiagnosticName() + " will be used." );
             p.convertFunctions( functions, newp );
             protocol = newp;
             parmValues = vals;
           }
           if ( ( p instanceof DeviceCombiner ) && ( newp instanceof DeviceCombiner ) )
           {
             for ( CombinerDevice dev : ( ( DeviceCombiner )p ).getDevices() )
               ( ( DeviceCombiner )newp ).add( dev );
           }
         }
       }
       else if ( ( description == null ) && ( file == null ) && ( assignments.isEmpty() ) && !hasDefinedFunctions() )
       {
         remote = newRemote;
         protocol = null;
         reset();
       }
       else
       {
         JOptionPane.showMessageDialog( RemoteMaster.getFrame(), "The selected protocol " + p.getDiagnosticName()
             + "\nis not compatible with the selected remote.\n" + "This upgrade will NOT function correctly.\n"
             + "Please choose a different protocol.", "Error", JOptionPane.ERROR_MESSAGE );
       }
 
     }
     if ( ( remote != null ) && ( remote != newRemote ) )
     {
       SetupCode.setMax( remote.usesTwoBytePID() ? 4095 : 2047 );
 
       Button[] buttons = remote.getUpgradeButtons();
       ButtonAssignments newAssignments = new ButtonAssignments();
       java.util.List< java.util.List< String >> unassigned = new ArrayList< java.util.List< String >>();
       for ( int i = 0; i < buttons.length; i++ )
       {
         Button b = buttons[ i ];
         for ( int state = Button.NORMAL_STATE; state <= Button.XSHIFTED_STATE; ++state )
         {
           Function f = assignments.getAssignment( b, state );
           if ( f != null )
           {
             assignments.assign( b, null, state );
 
             Button newB = newRemote.findByStandardName( b );
             java.util.List< String > temp = null;
             if ( f != null )
             {
               if ( ( newB != null ) && newB.allowsKeyMove( state ) )
                 newAssignments.assign( newB, f, state );
               else
               {
                 temp = new ArrayList< String >();
                 temp.add( f.getName() );
                 temp.add( b.getName() );
                 unassigned.add( temp );
               }
             }
           }
         }
       }
       if ( !unassigned.isEmpty() )
       {
         String message = "<html>Some of the functions defined in the device upgrade were assigned to buttons<br>"
             + "that do not match buttons on the newly selected remote.  The functions and the<br>"
             + "corresponding button names from the original remote are listed below."
             + "<br><br>Use the Button or Layout panel to assign those functions properly.</html>";
 
         // JOptionPane.showM
         // JFrame frame = new JFrame( "Lost Function Assignments" );
         JPanel panel = new JPanel( new BorderLayout());
 
         JLabel text = new JLabel( message );
         text.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
         panel.add( text, BorderLayout.NORTH );
         java.util.List< String > titles = new ArrayList< String >();
         titles.add( "Function name" );
         titles.add( "Button name" );
         Object[][] unassignedArray = new Object[ unassigned.size() ][];
         int i = 0;
         for ( java.util.List< String > l : unassigned )
           unassignedArray[ i++ ] = l.toArray();
         JTableX table = new JTableX( unassignedArray, titles.toArray() );
         Dimension d = table.getPreferredScrollableViewportSize();
         int showRows = 14;
         if ( unassigned.size() < showRows )
           showRows = unassigned.size();
         d.height = ( table.getRowHeight() + table.getRowMargin() ) * showRows;
         table.setPreferredScrollableViewportSize( d );
         panel.add( new JScrollPane( table ), BorderLayout.CENTER );
         
         JOptionPane.showMessageDialog( RemoteMaster.getFrame(), panel, "Lost Function Assignments", JOptionPane.PLAIN_MESSAGE );
       }
       assignments = newAssignments;
     }
     remote = newRemote;
   }
 
   /**
    * Gets the remote.
    * 
    * @return the remote
    */
   public Remote getRemote()
   {
     return remote;
   }
 
   /**
    * Sets the device type alias name.
    * 
    * @param name
    *          the new device type alias name
    */
   public void setDeviceTypeAliasName( String name )
   {
     String oldName = devTypeAliasName;
     if ( name != null )
     {
       if ( remote.getDeviceTypeByAliasName( name ) != null )
       {
         devTypeAliasName = name;
       }
       else
       {
         devTypeAliasName = deviceTypeAliasNames[ 0 ];
         System.err.println( "Unable to find device type with alias name " + name );
       }
     }
     propertyChangeSupport.firePropertyChange( "deviceTypeAliasName", oldName, devTypeAliasName );
   }
 
   /**
    * Gets the device type alias name.
    * 
    * @return the device type alias name
    */
   public String getDeviceTypeAliasName()
   {
     return devTypeAliasName;
   }
 
   /**
    * Gets the device type.
    * 
    * @return the device type
    */
   public DeviceType getDeviceType()
   {
     return remote.getDeviceTypeByAliasName( devTypeAliasName );
   }
 
   /**
    * Sets the protocol.
    * 
    * @param newProtocol
    *          the new protocol
    * @return true, if successful
    */
   public boolean setProtocol( Protocol newProtocol )
   {
     Protocol oldProtocol = protocol;
     // Convert device parameters to the new protocol
     if ( protocol != null )
     {
       if ( protocol == newProtocol )
         return false;
 
       newProtocol.reset();
 
       if ( newProtocol.getFixedDataLength() == protocol.getFixedDataLength() )
         newProtocol.importFixedData( protocol.getFixedData( parmValues ) );
 
       DeviceParameter[] parms = protocol.getDeviceParameters();
       DeviceParameter[] parms2 = newProtocol.getDeviceParameters();
 
       int[] map = new int[ parms.length ];
       for ( int i = 0; i < map.length; i++ )
         map[ i ] = -1;
       boolean parmsMatch = true;
       for ( int i = 0; i < parms.length; i++ )
       {
         String name = parms[ i ].getName();
         boolean nameMatch = false;
         for ( int j = 0; j < parms2.length; j++ )
         {
           if ( name.equals( parms2[ j ].getName() ) )
           {
             map[ i ] = j;
             nameMatch = true;
             break;
           }
         }
         if ( nameMatch )
           parmsMatch = true;
       }
 
       if ( parmsMatch )
       {
         // copy parameters from p to p2!
         System.err.println( "\tCopying dev. parms" );
         for ( int i = 0; i < map.length; i++ )
         {
           int mappedIndex = map[ i ];
           if ( mappedIndex != -1 )
           {
             System.err.println( "\tfrom index " + i + " to index " + map[ i ] );
             parms2[ mappedIndex ].setValue( parms[ i ].getValue() );
           }
         }
       }
 
       // convert the functions to the new protocol
       if ( !protocol.convertFunctions( functions, newProtocol ) )
       {
         propertyChangeSupport.firePropertyChange( "protocol", oldProtocol, oldProtocol );
         return false;
       }
     }
     protocol = newProtocol;
     parmValues = protocol.getDeviceParmValues();
     propertyChangeSupport.firePropertyChange( "protocol", oldProtocol, protocol );
     return true;
   }
 
   /**
    * Gets the protocol.
    * 
    * @return the protocol
    */
   public Protocol getProtocol()
   {
     return protocol;
   }
 
   /**
    * Sets the notes.
    * 
    * @param notes
    *          the new notes
    */
   public void setNotes( String notes )
   {
     this.notes = notes;
   }
 
   /**
    * Gets the notes.
    * 
    * @return the notes
    */
   public String getNotes()
   {
     return notes;
   }
 
   /**
    * Gets the functions.
    * 
    * @return the functions
    */
   public java.util.List< Function > getFunctions()
   {
     return functions;
   }
 
   /**
    * Gets the function.
    * 
    * @param name
    *          the name
    * @return the function
    */
   public Function getFunction( String name )
   {
     Function rc = getFunction( name, functions );
     if ( rc == null )
       rc = getFunction( name, extFunctions );
     return rc;
   }
 
   /**
    * Gets the function.
    * 
    * @param name
    *          the name
    * @param funcs
    *          the funcs
    * @return the function
    */
   public Function getFunction( String name, java.util.List< ? extends Function > funcs )
   {
     for ( Function func : funcs )
     {
       String funcName = func.getName();
       if ( ( funcName != null ) && funcName.equalsIgnoreCase( name ) )
         return func;
     }
     return null;
   }
 
   /**
    * Gets the function.
    * 
    * @param hex
    *          the hex
    * @return the function
    */
   public Function getFunction( Hex hex )
   {
     for ( Function f : functions )
     {
       if ( hex.equals( f.getHex() ) )
         return f;
     }
     return null;
   }
 
   /**
    * Gets the external functions.
    * 
    * @return the external functions
    */
   public java.util.List< ExternalFunction > getExternalFunctions()
   {
     return extFunctions;
   }
 
   /*
    * public List< KeyMove > getKeyMoves() { return keymoves; }
    */
 
   /**
    * Sets the file.
    * 
    * @param file
    *          the new file
    */
   public void setFile( File file )
   {
     this.file = file;
   }
 
   /**
    * Gets the file.
    * 
    * @return the file
    */
   public File getFile()
   {
     return file;
   }
 
   /**
    * Find digit map index.
    * 
    * @return the short
    */
   private short findDigitMapIndex()
   {
     short[] digitMaps = remote.getDigitMaps();
     if ( digitMaps == null )
       return -1;
 
     int cmdLength = protocol.getDefaultCmd().length();
     short[] digitKeyCodes = new short[ 10 * cmdLength ];
     Button[] buttons = remote.getUpgradeButtons();
     int offset = 0;
     for ( int i = 0; i < 10; i++ , offset += cmdLength )
     {
       Function f = assignments.getAssignment( buttons[ i ] );
       if ( ( f != null ) && !f.isExternal() )
         Hex.put( f.getHex(), digitKeyCodes, offset );
     }
     return DigitMaps.findDigitMapIndex( digitMaps, digitKeyCodes );
   }
 
   /**
    * Import raw upgrade.
    * 
    * @param hexCode
    *          the hex code
    * @param newRemote
    *          the new remote
    * @param newDeviceTypeAliasName
    *          the new device type alias name
    * @param pid
    *          the pid
    * @param pCode
    *          the code
    * @throws ParseException
    *           the parse exception
    */
   public void importRawUpgrade( Hex hexCode, Remote newRemote, String newDeviceTypeAliasName, Hex pid, Hex pCode )
       throws java.text.ParseException
   {
     reset();
     System.err.println( "DeviceUpgrade.importRawUpgrade" );
     System.err.println( "  hexCode=" + hexCode.toString() );
     System.err.println( "  newRemote=" + newRemote );
     System.err.println( "  newDeviceTypeAliasName=" + newDeviceTypeAliasName );
     System.err.println( "  pid=" + pid.toString() );
     System.err.println( "  pCode=" + pCode );
     int index = 1;
     if ( newRemote.usesTwoBytePID() )
       index++ ;
 
     short[] code = hexCode.getData();
     remote = newRemote;
     functions.clear();
     devTypeAliasName = newDeviceTypeAliasName;
     DeviceType devType = remote.getDeviceTypeByAliasName( devTypeAliasName );
     ButtonMap map = devType.getButtonMap();
 
     int digitMapIndex = -1;
     if ( !remote.getOmitDigitMapByte() && ( index < code.length ) )
       digitMapIndex = code[ index++ ] - 1;
     java.util.List< Button > buttons = null;
     if ( ( map != null ) && ( index < code.length ) )
       buttons = map.parseBitMap( code, index, digitMapIndex != -1 );
     else
       buttons = new ArrayList< Button >();
 
     while ( ( index < code.length ) && ( ( code[ index++ ] & 1 ) == 0 ) )
       ; // skip over the bitMap
 
     int fixedDataOffset = index;
     int fixedDataLength = 0;
     int cmdLength = 0;
     short[] fixedData = null;
     Hex fixedDataHex = null;
     if ( ( pCode != null ) && ( pCode.length() > 2 ) )
     {
       Processor proc = newRemote.getProcessor();
       fixedDataLength = Protocol.getFixedDataLengthFromCode( proc.getEquivalentName(), pCode );
       cmdLength = Protocol.getCmdLengthFromCode( proc.getEquivalentName(), pCode );
       System.err.println( "fixedDataLength=" + fixedDataLength + " and cmdLength=" + cmdLength );
       fixedData = new short[ fixedDataLength ];
       System.arraycopy( code, fixedDataOffset, fixedData, 0, fixedDataLength );
       fixedDataHex = new Hex( fixedData );
     }
     Value[] vals = parmValues;
     java.util.List< Protocol > protocols = null;
     if ( pCode == null )
     {
       protocols = ProtocolManager.getProtocolManager().getBuiltinProtocolsForRemote( remote, pid );
     }
     else
     {
       protocols = ProtocolManager.getProtocolManager().findByPID( pid );
     }
     Protocol tentative = null;
     Value[] tentativeVals = null;
     Protocol p = null;
     for ( Protocol tryit : protocols )
     {
       p = tryit;
       System.err.println( "Checking protocol " + p.getDiagnosticName() );
       if ( !remote.supportsVariant( pid, p.getVariantName() ) && !p.hasCode( remote ) )
         continue;
       int tempLength = fixedDataLength;
       if ( pCode == null )
       {
         tempLength = p.getFixedDataLength();
         fixedData = new short[ tempLength ];
         System.arraycopy( code, fixedDataOffset, fixedData, 0, tempLength );
         fixedDataHex = new Hex( fixedData );
       }
       if ( tempLength != p.getFixedDataLength() )
       {
         System.err.println( "FixedDataLength doesn't match!" );
         continue;
       }
       System.err.println( "Imported fixedData is " + fixedDataHex );
       vals = p.importFixedData( fixedDataHex );
       System.err.print( "Imported device parms are:" );
       for ( Value v : vals )
         System.err.print( " " + v.getValue() );
       System.err.println();
       Hex calculatedFixedData = p.getFixedData( vals );
       System.err.println( "Calculated fixedData is " + calculatedFixedData );
       Hex mask = p.getFixedDataMask();
       Hex maskedCalculatedData = calculatedFixedData.applyMask( mask );
       Hex maskedImportedData = fixedDataHex.applyMask( mask );
       if ( maskedCalculatedData.equals( maskedImportedData ) )
       {
         System.err.println( "It's a match!" );
         Hex tentativeCode = null;
         if ( pCode != null )
         {
           tentativeCode = getCode( p );
         }
         if ( ( tentative == null ) || ( tempLength > tentative.getFixedDataLength() )
             || ( ( pCode != null ) && pCode.equals( tentativeCode ) ) )
         {
           System.err.println( "And it's longer, or the protocol code matches!" );
           tentative = p;
           tentativeVals = vals;
         }
       }
     }
 
     ManualProtocol mp = null;
 
     if ( tentative != null ) // && (( pCode == null ) || pCode.equals( getCode( tentative ))))
     {
       // Found a match.
       // Might want to check if the protocol code matches
       p = tentative;
       System.err.println( "Using " + p.getDiagnosticName() );
       fixedDataLength = p.getFixedDataLength();
       cmdLength = p.getDefaultCmd().length();
       parmValues = tentativeVals;
     }
     else if ( ( p != null ) && ( pCode == null ) )
     {
       // Found a matching PID, and there's protocol code,
       // but couldn't recreate the fixed data.
       // Maybe there's some reason to use non-standard fixed data.
 
       System.err.println( "Creating a derived protocol" );
       Properties props = new Properties();
       for ( Processor pr : ProcessorManager.getProcessors() )
       {
         Hex hCode = p.getCode( pr );
         if ( hCode != null )
         {
           props.put( "Code." + pr.getEquivalentName(), hCode.toString() );
         }
       }
       String variant = p.getVariantName();
       if ( ( variant != null ) && ( variant.length() > 0 ) )
         props.put( "VariantName", variant );
       p = ProtocolFactory.createProtocol( "pid: " + pid.toString(), pid, "Protocol", props );
       ProtocolManager.getProtocolManager().add( p );
       fixedDataLength = p.getFixedDataLength();
       cmdLength = p.getDefaultCmd().length();
       parmValues = p.importFixedData( fixedDataHex );
     }
     else if ( pCode != null )
     {
       // Don't have anything we can use, so create a manual protocol
       System.err.println( "Using a Manual Protocol" );
       fixedData = new short[ fixedDataLength ];
       System.arraycopy( code, fixedDataOffset, fixedData, 0, fixedDataLength );
       int cmdType = ManualProtocol.ONE_BYTE;
       if ( cmdLength == 2 )
         cmdType = ManualProtocol.AFTER_CMD;
       if ( cmdLength > 2 )
         cmdType = cmdLength << 4;
 
       ArrayList< Value > parms = new ArrayList< Value >();
       for ( short temp : fixedData )
         parms.add( new Value( temp & 0xFF ) );
       parmValues = parms.toArray( new Value[ fixedDataLength ] );
 
       mp = new ManualProtocol( "Manual Settings: " + pid, pid, cmdType, "MSB", 8, parms, new short[ 0 ], 8 );
       mp.setCode( pCode, remote.getProcessor() );
       ProtocolManager.getProtocolManager().add( mp );
       p = mp;
     }
     else
       throw new ParseException( "Unable to import device upgrade", index );
 
     if ( digitMapIndex != -1 )
     {
       int mapNum = remote.getDigitMaps()[ digitMapIndex ];
       Hex[] hexCmds = DigitMaps.getHexCmds( mapNum, cmdLength );
       for ( int i = 0; i < hexCmds.length; ++i )
       {
         Function f = new Function();
         String name = Integer.toString( i );
         f.setName( name );
         Hex hex = hexCmds[ i ];
         if ( cmdLength < hex.length() )
           hex = hex.subHex( 0, cmdLength );
         f.setHex( hex );
         Button b = map.get( i );
         assignments.assign( b, f );
         functions.add( f );
       }
     }
 
     index += fixedDataLength;
 
     protocol = p;
     for ( Button b : buttons )
     {
       if ( index >= code.length )
         break;
       short[] cmd = new short[ cmdLength ];
       for ( int i = 0; i < cmdLength; i++ )
         cmd[ i ] = code[ index++ ];
       Function f = new Function();
       f.setName( b.getName() );
       f.setHex( new Hex( cmd ) );
       functions.add( f );
       assignments.assign( b, f );
     }
   }
 
   /**
    * Gets the hex setup code.
    * 
    * @return the hex setup code
    */
   public short[] getHexSetupCode()
   {
     DeviceType devType = remote.getDeviceTypeByAliasName( devTypeAliasName );
     short[] id = protocol.getID( remote ).getData();
     short temp = ( short )( devType.getNumber() * 0x1000 + setupCode - remote.getDeviceCodeOffset() );
     if ( !remote.usesTwoBytePID() )
       temp += ( id[ 0 ] & 1 ) * 0x0800;
 
     short[] rc = new short[ 2 ];
    rc[ 0 ] = ( short )( temp >> 8 );
     rc[ 1 ] = ( short )( temp & 0xFF );
     return rc;
   }
 
   /**
    * Gets the key moves.
    * 
    * @return the key moves
    */
   public java.util.List< KeyMove > getKeyMoves()
   {
     java.util.List< KeyMove > keyMoves = new ArrayList< KeyMove >();
     DeviceType devType = remote.getDeviceTypeByAliasName( devTypeAliasName );
     for ( Button button : remote.getUpgradeButtons() )
     {
       Function f = assignments.getAssignment( button, Button.NORMAL_STATE );
       KeyMove keyMove = button.getKeyMove( f, 0, setupCode, devType, remote, protocol.getKeyMovesOnly() );
       if ( keyMove != null )
         keyMoves.add( keyMove );
 
       f = assignments.getAssignment( button, Button.SHIFTED_STATE );
       if ( button.getShiftedButton() != null )
         f = null;
       keyMove = button.getKeyMove( f, remote.getShiftMask(), setupCode, devType, remote, protocol.getKeyMovesOnly() );
       if ( keyMove != null )
         keyMoves.add( keyMove );
 
       f = assignments.getAssignment( button, Button.XSHIFTED_STATE );
       if ( button.getXShiftedButton() != null )
         f = null;
       keyMove = button.getKeyMove( f, remote.getXShiftMask(), setupCode, devType, remote, protocol.getKeyMovesOnly() );
       if ( keyMove != null )
         keyMoves.add( keyMove );
     }
     return keyMoves;
   }
 
   /**
    * Gets the upgrade text.
    * 
    * @param includeNotes
    *          the include notes
    * @return the upgrade text
    */
   public String getUpgradeText()
   {
     StringBuilder buff = new StringBuilder( 400 );
     if ( remote.usesTwoBytePID() )
       buff.append( "Upgrade Code2 = " );
     else
       buff.append( "Upgrade Code 0 = " );
 
     short[] deviceCode = getHexSetupCode();
 
     buff.append( Hex.toString( deviceCode ) );
     buff.append( " (" );
     buff.append( devTypeAliasName );
     buff.append( '/' );
     DecimalFormat df = new DecimalFormat( "0000" );
     buff.append( df.format( setupCode ) );
     buff.append( ")" );
 
     String descr = "";
     if ( description != null )
       descr = description.trim();
     if ( descr.length() != 0 )
     {
       buff.append( ' ' );
       buff.append( descr );
     }
     buff.append( " (RM " );
     buff.append( RemoteMaster.version );
     buff.append( ')' );
 
     try
     {
       BufferedReader rdr = new BufferedReader( new StringReader( Hex.toString( getUpgradeHex().getData(), 16 ) ) );
       String line = null;
       while ( ( line = rdr.readLine() ) != null )
       {
         buff.append( "\n " );
         buff.append( line );
       }
     }
     catch ( IOException ioe )
     {
       ioe.printStackTrace( System.err );
     }
 
     DeviceType devType = remote.getDeviceTypeByAliasName( devTypeAliasName );
     ButtonMap map = devType.getButtonMap();
     Button[] buttons = remote.getUpgradeButtons();
     boolean hasKeyMoves = false;
     int i;
     for ( i = 0; i < buttons.length; i++ )
     {
       Button b = buttons[ i ];
       Function f = assignments.getAssignment( b, Button.NORMAL_STATE );
       Function sf = assignments.getAssignment( b, Button.SHIFTED_STATE );
       if ( b.getShiftedButton() != null )
         sf = null;
       Function xf = assignments.getAssignment( b, Button.XSHIFTED_STATE );
       if ( b.getXShiftedButton() != null )
         xf = null;
       if ( ( ( f != null ) && ( ( map == null ) || protocol.getKeyMovesOnly() || !map.isPresent( b ) || f.isExternal() ) )
           || ( ( sf != null ) && ( sf.getHex() != null ) ) || ( ( xf != null ) && ( xf.getHex() != null ) ) )
       {
         hasKeyMoves = true;
         break;
       }
     }
     if ( hasKeyMoves )
     {
       deviceCode[ 0 ] = ( short )( deviceCode[ 0 ] & 0xF7 );
       buff.append( "\nKeyMoves" );
       boolean first = true;
       for ( ; i < buttons.length; i++ )
       {
         Button button = buttons[ i ];
 
         Function f = assignments.getAssignment( button, Button.NORMAL_STATE );
         first = appendKeyMove( buff,
             button.getKeyMove( f, 0, deviceCode, devType, remote, protocol.getKeyMovesOnly() ), f, first );
         f = assignments.getAssignment( button, Button.SHIFTED_STATE );
         if ( button.getShiftedButton() != null )
           f = null;
         first = appendKeyMove( buff, button.getKeyMove( f, remote.getShiftMask(), deviceCode, devType, remote, protocol
             .getKeyMovesOnly() ), f, first );
         f = assignments.getAssignment( button, Button.XSHIFTED_STATE );
         if ( button.getXShiftedButton() != null )
           f = null;
         first = appendKeyMove( buff, button.getKeyMove( f, remote.getXShiftMask(), deviceCode, devType, remote,
             protocol.getKeyMovesOnly() ), f, first );
       }
     }
 
     buff.append( "\nEnd" );
 
     return buff.toString();
   }
 
   /**
    * Gets the upgrade length.
    * 
    * @return the upgrade length
    */
   public int getUpgradeLength()
   {
     int rc = 0;
 
     // add the 2nd byte of the PID
     rc++ ;
 
     // add the digitMapIndex
     int digitMapIndex = -1;
 
     if ( !remote.getOmitDigitMapByte() )
     {
       rc++ ;
     }
 
     DeviceType devType = remote.getDeviceTypeByAliasName( devTypeAliasName );
     ButtonMap map = devType.getButtonMap();
     if ( map != null )
     {
       rc += map.toBitMap( digitMapIndex != -1, protocol.getKeyMovesOnly(), assignments ).length;
     }
 
     rc += protocol.getFixedData( parmValues ).length();
 
     if ( map != null )
     {
       short[] data = map.toCommandList( digitMapIndex != -1, protocol.getKeyMovesOnly(), assignments );
       if ( data != null )
         rc += data.length;
     }
     return rc;
   }
 
   /**
    * Gets the upgrade hex.
    * 
    * @return the upgrade hex
    */
   public Hex getUpgradeHex()
   {
     java.util.List< short[] > work = new ArrayList< short[] >();
 
     // add the 2nd byte of the PID
 
     short[] data = null;
     if ( remote.usesTwoBytePID() )
       data = protocol.getID( remote ).getData();
     else
     {
       data = new short[ 1 ];
       data[ 0 ] = protocol.getID( remote ).getData()[ 1 ];
     }
     work.add( data );
 
     short digitMapIndex = -1;
 
     if ( !remote.getOmitDigitMapByte() )
     {
       data = new short[ 1 ];
       digitMapIndex = findDigitMapIndex();
       if ( digitMapIndex == -1 )
         data[ 0 ] = 0;
       else
         data[ 0 ] = digitMapIndex;
 
       work.add( data );
     }
 
     DeviceType devType = remote.getDeviceTypeByAliasName( devTypeAliasName );
     ButtonMap map = devType.getButtonMap();
     if ( map != null )
     {
       work.add( map.toBitMap( digitMapIndex != -1, protocol.getKeyMovesOnly(), assignments ) );
     }
 
     work.add( protocol.getFixedData( parmValues ).getData() );
 
     if ( map != null )
     {
       data = map.toCommandList( digitMapIndex != -1, protocol.getKeyMovesOnly(), assignments );
       if ( ( data != null ) && ( data.length != 0 ) )
         work.add( data );
     }
 
     int length = 0;
     for ( short[] temp : work )
       length += temp.length;
 
     int offset = 0;
     short[] rc = new short[ length ];
     for ( short[] source : work )
     {
       System.arraycopy( source, 0, rc, offset, source.length );
       offset += source.length;
     }
     return new Hex( rc );
   }
 
   /**
    * Append key move.
    * 
    * @param buff
    *          the buff
    * @param keyMove
    *          the key move
    * @param f
    *          the f
    * @param includeNotes
    *          the include notes
    * @param first
    *          the first
    * @return true, if successful
    */
   private boolean appendKeyMove( StringBuilder buff, short[] keyMove, Function f, boolean first )
   {
     if ( ( keyMove == null ) || ( keyMove.length == 0 ) )
       return first;
 
     if ( !first )
       buff.append( '\u00a6' );
 
     buff.append( "\n " );
 
     buff.append( Hex.toString( keyMove ) );
 
     buff.append( '\u00ab' );
     buff.append( f.getName() );
     String notes = f.getNotes();
     if ( ( notes != null ) && ( notes.length() != 0 ) )
     {
       buff.append( ": " );
       buff.append( notes );
     }
     buff.append( '\u00bb' );
 
     return false;
   }
 
   /**
    * Store.
    * 
    * @throws IOException
    *           Signals that an I/O exception has occurred.
    */
   public void store() throws IOException
   {
     store( file );
   }
 
   /**
    * Value array to string.
    * 
    * @param parms
    *          the parms
    * @return the string
    */
   public static String valueArrayToString( Value[] parms )
   {
     StringBuilder buff = new StringBuilder( 200 );
     for ( int i = 0; i < parms.length; i++ )
     {
       if ( i > 0 )
         buff.append( ' ' );
       Value parm = parms[ i ];
       if ( parm == null )
         buff.append( "null" );
       else
         buff.append( parms[ i ].getUserValue() );
     }
     return buff.toString();
   }
 
   /**
    * String to value array.
    * 
    * @param str
    *          the str
    * @return the value[]
    */
   public static Value[] stringToValueArray( String str )
   {
     StringTokenizer st = new StringTokenizer( str );
     Value[] parms = new Value[ st.countTokens() ];
     for ( int i = 0; i < parms.length; i++ )
     {
       String token = st.nextToken();
       Object val = null;
       if ( !token.equals( "null" ) )
       {
         if ( token.equals( "true" ) )
           val = new Integer( 1 );
         else if ( token.equals( "false" ) )
           val = new Integer( 0 );
         else
           val = Integer.parseInt( token );
       }
       parms[ i ] = new Value( val, null );
     }
     return parms;
   }
 
   /**
    * Store.
    * 
    * @param file
    *          the file
    * @throws IOException
    *           Signals that an I/O exception has occurred.
    */
   public void store( File file ) throws IOException
   {
     this.file = file;
     StringWriter sw = new StringWriter();
     PropertyWriter pw = new PropertyWriter( new PrintWriter( sw ) );
     store( pw );
     pw.close();
     baseline = sw.toString();
     FileWriter fw = new FileWriter( file );
     fw.write( baseline );
     fw.flush();
     fw.close();
   }
 
   /**
    * Store.
    * 
    * @param out
    *          the out
    * @throws IOException
    *           Signals that an I/O exception has occurred.
    */
   public void store( PropertyWriter out ) throws IOException
   {
     if ( description != null )
       out.print( "Description", description );
     out.print( "Remote.name", remote.getName() );
     out.print( "Remote.signature", remote.getSignature() );
     out.print( "DeviceType", devTypeAliasName );
     DeviceType devType = remote.getDeviceTypeByAliasName( devTypeAliasName );
     out.print( "DeviceIndex", Integer.toHexString( devType.getNumber() ) );
     out.print( "SetupCode", Integer.toString( setupCode ) );
     // protocol.setDeviceParms( parmValues );
     protocol.store( out, parmValues );
     if ( notes != null )
       out.print( "Notes", notes );
     int i = 0;
     for ( Function func : functions )
       func.store( out, "Function." + i++ );
 
     i = 0;
     for ( ExternalFunction func : extFunctions )
       func.store( out, "ExtFunction." + i++ );
 
     Button[] buttons = remote.getUpgradeButtons();
     String regex = "\\|";
     String replace = "\\\\u007c";
     for ( i = 0; i < buttons.length; i++ )
     {
       Button b = buttons[ i ];
       Function f = assignments.getAssignment( b, Button.NORMAL_STATE );
 
       String fstr;
       if ( f == null )
         fstr = "null";
       else
         fstr = f.getName().replaceAll( regex, replace );
 
       Function sf = assignments.getAssignment( b, Button.SHIFTED_STATE );
       String sstr;
       if ( sf == null )
         sstr = "null";
       else
         sstr = sf.getName().replaceAll( regex, replace );
 
       Function xf = assignments.getAssignment( b, Button.XSHIFTED_STATE );
       String xstr;
       if ( xf == null )
         xstr = null;
       else
         xstr = xf.getName().replaceAll( regex, replace );
       if ( ( f != null ) || ( sf != null ) || ( xf != null ) )
       {
         out.print( "Button." + Integer.toHexString( b.getKeyCode() ), fstr + '|' + sstr + '|' + xstr );
       }
 
     }
     out.flush();
   }
 
   private String baseline = "";
 
   public void setBaseline()
   {
     try
     {
       StringWriter sw = new StringWriter();
       store( new PropertyWriter( new PrintWriter( sw ) ) );
       baseline = sw.toString();
     }
     catch ( IOException ioe )
     {
       ioe.printStackTrace( System.err );
     }
   }
 
   /**
    * Checks for changed.
    * 
    * @param baseFile
    *          the base file
    * @return true, if successful
    * @throws IOException
    *           Signals that an I/O exception has occurred.
    */
   public boolean hasChanged() throws IOException
   {
     StringWriter sw = new StringWriter();
     store( new PropertyWriter( new PrintWriter( sw ) ) );
 
     String latest = sw.toString();
 
     BufferedReader baseReader = new BufferedReader( new StringReader( baseline ) );
     BufferedReader tempReader = new BufferedReader( new StringReader( latest ) );
     String baseLine = null;
     String tempLine = null;
     do
     {
       baseLine = baseReader.readLine();
       while ( ( baseLine != null ) && baseLine.startsWith( "#" ) )
         baseLine = baseReader.readLine();
 
       tempLine = tempReader.readLine();
       while ( ( tempLine != null ) && tempLine.startsWith( "#" ) )
         tempLine = tempReader.readLine();
       System.err.println( "baseLine=" + baseLine );
       System.err.println( "tempLine=" + tempLine );
     }
     while ( ( baseLine != null ) && ( tempLine != null ) && baseLine.equals( tempLine ) );
     baseReader.close();
     tempReader.close();
     // tempFile.delete();
 
     if ( baseLine == tempLine )
       return false;
 
     return true;
   }
 
   /**
    * Load.
    * 
    * @param file
    *          the file
    * @throws Exception
    *           the exception
    */
   public void load( File file ) throws Exception
   {
     load( file, true );
   }
 
   /**
    * Load.
    * 
    * @param file
    *          the file
    * @param loadButtons
    *          the load buttons
    * @throws Exception
    *           the exception
    */
   public void load( File file, boolean loadButtons ) throws Exception
   {
     BufferedReader reader = new BufferedReader( new FileReader( file ) );
     load( reader, loadButtons );
     if ( file.getName().toLowerCase().endsWith( ".rmdu" ) )
       this.file = file;
   }
 
   /**
    * Load.
    * 
    * @param reader
    *          the reader
    * @throws Exception
    *           the exception
    */
   public void load( BufferedReader reader ) throws Exception
   {
     load( reader, true );
   }
 
   /**
    * Load.
    * 
    * @param reader
    *          the reader
    * @param loadButtons
    *          the load buttons
    * @throws Exception
    *           the exception
    */
   public void load( BufferedReader reader, boolean loadButtons ) throws Exception
   {
     reader.mark( 160 );
     String line = reader.readLine();
     reader.reset();
     if ( line.startsWith( "Name:" ) )
     {
       reset();
       importUpgrade( reader, loadButtons );
     }
     else
     {
       Properties props = new Properties();
       Property property = new Property();
       PropertyReader pr = new PropertyReader( reader );
       while ( ( property = pr.nextProperty() ) != null )
       {
         props.put( property.name, property.value );
       }
       reader.close();
 
       load( props, loadButtons );
     }
     setBaseline();
   }
 
   /**
    * Load.
    * 
    * @param props
    *          the props
    */
   public void load( Properties props, boolean loadButtons )
   {
     load( props, true, null );
   }
 
   /**
    * Load.
    * 
    * @param props
    *          the props
    * @param loadButtons
    *          the load buttons
    */
   public void load( Properties props, boolean loadButtons, Remote theRemote )
   {
     reset();
     String str = props.getProperty( "Description" );
     if ( str != null )
       description = str;
     str = props.getProperty( "Remote.name" );
     if ( str == null )
     {
       JOptionPane.showMessageDialog( RemoteMaster.getFrame(),
           "The upgrade you are trying to import is not valid!  It does not contain a value for Remote.name",
           "Import Failure", JOptionPane.ERROR_MESSAGE );
       return;
     }
     if ( theRemote != null )
     {
       remote = theRemote;
     }
     else
     {
       theRemote = RemoteManager.getRemoteManager().findRemoteByName( str );
       if ( theRemote == null )
       {
         return;
       }
       remote = theRemote;
     }
     remote.load();
     str = props.getProperty( "DeviceIndex" );
     if ( str != null )
     {}
     setDeviceTypeAliasName( props.getProperty( "DeviceType" ) );
     setupCode = Integer.parseInt( props.getProperty( "SetupCode" ) );
 
     Hex pid = new Hex( props.getProperty( "Protocol", "0200" ) );
     String name = props.getProperty( "Protocol.name", "" );
     String variantName = props.getProperty( "Protocol.variantName", "" );
 
     ProtocolManager pm = ProtocolManager.getProtocolManager();
     if ( name.startsWith( "Manual Settings" ) || name.equals( "Manual" )
         || name.equalsIgnoreCase( "PID " + pid.toString() ) )
     {
       ManualProtocol mp = new ManualProtocol( pid, props );
       mp.setName( name );
       protocol = mp;
       pm.add( protocol );
     }
     else
     {
       // Need to consider all protocol attributes, to handle things like "Acer Keyboard (01 11)" and
       // "TiVo (01 11)"
       protocol = pm.findNearestProtocol( remote, name, pid, variantName );
 
       if ( protocol == null )
       {
         JOptionPane.showMessageDialog( RemoteMaster.getFrame(), "No protocol found with name=\"" + name + "\", ID="
             + pid.toString() + ", and variantName=\"" + variantName + "\"", "File Load Error",
             JOptionPane.ERROR_MESSAGE );
         return;
       }
     }
 
     str = props.getProperty( "ProtocolParms" );
     System.err.println( "ProtocolParms='" + str + "'" );
     if ( ( str != null ) && ( str.length() != 0 ) )
     {
       protocol.setDeviceParms( stringToValueArray( str ) );
       parmValues = protocol.getDeviceParmValues();
     }
 
     protocol.setProperties( props, remote );
 
     notes = props.getProperty( "Notes" );
 
     functions.clear();
     int i = 0;
     while ( true )
     {
       Function f = new Function();
       f.load( props, "Function." + i );
       if ( f.isEmpty() )
       {
         break;
       }
       functions.add( f );
       i++ ;
     }
 
     extFunctions.clear();
     i = 0;
     while ( true )
     {
       ExternalFunction f = new ExternalFunction();
       f.load( props, "ExtFunction." + i, remote );
       if ( f.isEmpty() )
       {
         break;
       }
       extFunctions.add( f );
       i++ ;
     }
 
     if ( loadButtons )
     {
       Button[] buttons = remote.getUpgradeButtons();
       String regex = "\\\\u007c";
       String replace = "|";
       for ( i = 0; i < buttons.length; i++ )
       {
         Button b = buttons[ i ];
         str = props.getProperty( "Button." + Integer.toHexString( b.getKeyCode() ) );
         if ( str == null )
         {
           continue;
         }
         StringTokenizer st = new StringTokenizer( str, "|" );
         str = st.nextToken();
         Function func = null;
         if ( !str.equals( "null" ) )
         {
           func = getFunction( str.replaceAll( regex, replace ) );
           assignments.assign( b, func, Button.NORMAL_STATE );
         }
         str = st.nextToken();
         if ( !str.equals( "null" ) )
         {
           func = getFunction( str.replaceAll( regex, replace ) );
           assignments.assign( b, func, Button.SHIFTED_STATE );
         }
         if ( st.hasMoreTokens() )
         {
           str = st.nextToken();
           if ( !str.equals( "null" ) )
           {
             func = getFunction( str.replaceAll( regex, replace ) );
             assignments.assign( b, func, Button.XSHIFTED_STATE );
           }
         }
       }
     }
     setBaseline();
   }
 
   /**
    * Import upgrade.
    * 
    * @param in
    *          the in
    * @throws Exception
    *           the exception
    */
   public void importUpgrade( BufferedReader in ) throws Exception
   {
     importUpgrade( in, true );
   }
 
   /**
    * Parses the int.
    * 
    * @param str
    *          the str
    * @return the int
    */
   private static int parseInt( String str )
   {
     int base = 10;
     if ( str.charAt( 0 ) == '$' )
     {
       base = 16;
       str = str.substring( 1 );
     }
     else if ( str.charAt( str.length() - 1 ) == 'h' )
     {
       base = 16;
       str = str.substring( 0, str.length() - 1 );
     }
     return Integer.parseInt( str, base );
   }
 
   /**
    * Clean name.
    * 
    * @param name
    *          the name
    * @return the string
    */
   private String cleanName( String name )
   {
     if ( ( name != null ) && ( name.length() == 5 ) && name.toLowerCase().startsWith( "num " )
         && Character.isDigit( name.charAt( 4 ) ) )
       return name.substring( 4 );
     return name;
   }
 
   /**
    * Checks if is external function name.
    * 
    * @param name
    *          the name
    * @return true, if is external function name
    */
   private boolean isExternalFunctionName( String name )
   {
     if ( name == null )
       return false;
     char firstChar = name.charAt( 0 );
     int slash = name.indexOf( '/' );
     int space = name.indexOf( ' ' );
     if ( space == -1 )
       space = name.length();
     if ( ( firstChar == '=' ) && ( slash > 1 ) && ( space > slash ) )
     {
       @SuppressWarnings( "unused" )
       String devName = name.substring( 1, slash );
       String setupString = name.substring( slash + 1, space );
       if ( setupString.length() == 4 )
       {
         try
         {
           @SuppressWarnings( "unused" )
           int setupCode = Integer.parseInt( setupString );
           return true;
         }
         catch ( NumberFormatException nfe )
         {}
       }
     }
     return false;
   }
 
   /**
    * Import upgrade.
    * 
    * @param in
    *          the in
    * @param loadButtons
    *          the load buttons
    * @throws Exception
    *           the exception
    */
   public void importUpgrade( BufferedReader in, boolean loadButtons ) throws Exception
   {
     String line = in.readLine(); // line 1 "Name:"
     String token = line.substring( 0, 5 );
     if ( !token.equals( "Name:" ) )
     {
       JOptionPane.showMessageDialog( RemoteMaster.getFrame(), "The upgrade you are trying to import is not valid!",
           "Import Failure", JOptionPane.ERROR_MESSAGE );
       return;
     }
     String delim = line.substring( 5, 6 );
     List< String > fields = LineTokenizer.tokenize( line, delim );
     description = fields.get( 1 );
     String kmVersion = fields.get( 5 );
     System.err.println( "KM version of imported file is '" + kmVersion + '\'' );
 
     String protocolLine = in.readLine(); // line 2 "Devices:"
     String manualLine = in.readLine(); // line 3 "Manual:"
 
     line = in.readLine(); // line 4 "Setup:"
     List< String > setupFields = LineTokenizer.tokenize( line, delim );
     token = setupFields.get( 1 );
     setupCode = Integer.parseInt( token );
     token = setupFields.get( 2 );
     String str = token.substring( 5 );
 
     remote = RemoteManager.getRemoteManager().findRemoteByName( str );
     if ( remote == null )
     {
       reset();
       return;
     }
     Hex pid = null;
     while ( true )
     {
       line = in.readLine();
       if ( ( line != null ) && ( line.length() > 0 ) && ( line.charAt( 0 ) == '\"' ) )
         line = line.substring( 1 );
       int equals = line.indexOf( '=' );
       if ( ( equals != -1 ) && line.substring( 0, equals ).toLowerCase().startsWith( "upgrade code" ) )
       {
         short[] id = new short[ 2 ];
         short temp = Short.parseShort( line.substring( equals + 2, equals + 4 ), 16 );
         if ( ( temp & 8 ) != 0 )
           id[ 0 ] = 1;
 
         line = in.readLine();
         temp = Short.parseShort( line.substring( 0, 2 ), 16 );
         id[ 1 ] = temp;
         pid = new Hex( id );
         break;
       }
     }
 
     remote.load();
     token = setupFields.get( 3 );
     str = token.substring( 5 );
 
     if ( remote.getDeviceTypeByAliasName( str ) == null )
     {
       String rc = null;
       String msg = "Remote \"" + remote.getName() + "\" does not support the device type " + str
           + ".  Please select one of the supported device types below to use instead.\n";
       while ( rc == null )
       {
         rc = ( String )JOptionPane.showInputDialog( RemoteMaster.getFrame(), msg, "Unsupported Device Type",
             JOptionPane.ERROR_MESSAGE, null, remote.getDeviceTypeAliasNames(), null );
       }
       str = rc;
     }
     setDeviceTypeAliasName( str );
 
     String buttonStyle = setupFields.get( 4 );
 
     List< String > deviceFields = LineTokenizer.tokenize( protocolLine, delim );
     String protocolName = deviceFields.get( 1 ); // protocol name
 
     ProtocolManager protocolManager = ProtocolManager.getProtocolManager();
     if ( protocolName.equals( "Manual Settings" ) )
     {
       System.err.println( "protocolName=" + protocolName );
       System.err.println( "manualLine=" + manualLine );
       List< String > manualFields = LineTokenizer.tokenize( manualLine, delim );
       String pidStr = manualFields.get( 1 );
       System.err.println( "pid=" + pidStr );
       if ( pidStr != null )
       {
         int space = pidStr.indexOf( ' ' );
         if ( space != -1 )
         {
           pid = new Hex( pidStr );
         }
         else
         {
           short pidInt = Short.parseShort( pidStr, 16 );
           short[] data = new short[ 2 ];
           data[ 0 ] = ( short )( ( pidInt & 0xFF00 ) >> 8 );
           data[ 1 ] = ( short )( pidInt & 0xFF );
           pid = new Hex( data );
         }
       }
       int byte2 = Integer.parseInt( manualFields.get( 2 ).substring( 0, 1 ) );
       System.err.println( "byte2=" + byte2 );
       String signalStyle = manualFields.get( 3 );
       System.err.println( "SignalStyle=" + signalStyle );
       String bitsStr = manualFields.get( 4 );
       int devBits = 8;
       int cmdBits = 8;
       try
       {
         if ( bitsStr != null )
         {
           devBits = Integer.parseInt( bitsStr.substring( 0, 1 ), 16 );
           cmdBits = Integer.parseInt( bitsStr.substring( 1 ), 16 );
         }
       }
       catch ( NumberFormatException nfe )
       {}
       System.err.println( "devBits=" + devBits + " and cmdBits=" + cmdBits );
       if ( devBits == 0 )
         devBits = 8;
       if ( cmdBits == 0 )
         cmdBits = 8;
 
       java.util.List< Value > values = new ArrayList< Value >();
 
       str = deviceFields.get( 2 ); // Device 1
       if ( str != null )
         values.add( new Value( parseInt( str ) ) );
 
       str = deviceFields.get( 3 ); // Device 2
       if ( str != null )
         values.add( new Value( parseInt( str ) ) );
 
       str = deviceFields.get( 4 ); // Device 3
       if ( str != null )
         values.add( new Value( parseInt( str ) ) );
 
       str = deviceFields.get( 5 ); // Raw Fixed Data
       if ( str == null )
         str = "";
       short[] rawHex = Hex.parseHex( str );
 
       protocol = new ManualProtocol( protocolName, pid, byte2, signalStyle, devBits, values, rawHex, cmdBits );
       protocolName = protocol.getName();
       setParmValues( protocol.getDeviceParmValues() );
       protocolManager.add( protocol );
       java.util.List< Protocol > v = protocolManager.findByPID( pid );
       ListIterator< Protocol > li = v.listIterator();
       while ( li.hasNext() )
       {
         Protocol p = li.next();
         if ( p.getFixedDataLength() != rawHex.length )
         {
           li.remove();
           continue;
         }
       }
       if ( v.size() != 0 )
       {
         Protocol p = v.get( 0 );
         Hex code = p.getCode( remote );
         if ( code != null )
           ( ( ManualProtocol )protocol ).setCode( code, remote.getProcessor() );
       }
     }
     else
     {
       // protocol = protocolManager.findProtocolForRemote( remote, protocolName );
       Protocol p = protocolManager.findNearestProtocol( remote, protocolName, pid, null );
 
       if ( p == null )
       {
         p = protocolManager.findProtocolByOldName( remote, protocolName, pid );
 
         if ( p == null )
         {
           JOptionPane.showMessageDialog( RemoteMaster.getFrame(), "No protocol found with name=\"" + protocolName
               + "\" for remote \"" + remote.getName() + "\".", "Import Failure", JOptionPane.ERROR_MESSAGE );
           reset();
           return;
         }
       }
       protocol = p;
 
       Value[] importParms = new Value[ 6 ];
       for ( int i = 0; i < importParms.length && i + 2 < deviceFields.size(); i++ )
       {
         token = deviceFields.get( 2 + i );
         Object val = null;
         if ( token == null )
           val = null;
         else
         {
           if ( token.equals( "true" ) )
             val = new Integer( 1 );
           else if ( token.equals( "false" ) )
             val = new Integer( 0 );
           else
             val = token;
           // val = new Integer( token );
         }
         importParms[ i ] = new Value( val );
       }
       protocol.importDeviceParms( importParms );
       parmValues = protocol.getDeviceParmValues();
     }
 
     // compute cmdIndex
     boolean useOBC = false; // assume OBC???
     boolean useEFC = false;
     if ( buttonStyle.equals( "OBC" ) )
       useOBC = true;
     else if ( buttonStyle.equals( "EFC" ) )
       useEFC = true;
 
     int obcIndex = 0;
     CmdParameter[] cmdParms = protocol.getCommandParameters();
     for ( int i = 0; i < cmdParms.length; i++ )
     {
       if ( cmdParms[ i ].getName().equals( "OBC" ) )
       {
         obcIndex = i;
         break;
       }
     }
 
     String match1 = "fByte2" + delim + "bButtons" + delim + "bFunctions" + delim + "fNotes" + delim + "Device Combiner";
     String match2 = "byte2" + delim + "Buttons" + delim + "Functions" + delim;
 
     while ( true )
     {
       line = in.readLine();
       if ( ( line == null ) || ( line.indexOf( match1 ) != -1 ) || ( line.indexOf( match2 ) != -1 ) )
         break;
     }
 
     fields = LineTokenizer.tokenize( line, delim );
 
     int buttonCodeIndex = fields.indexOf( "bBtnCd" );
 
     functions.clear();
 
     DeviceCombiner combiner = null;
     if ( protocol.getClass() == DeviceCombiner.class )
       combiner = ( DeviceCombiner )protocol;
 
     // save the function definition/assignment lines for later parsing
     String[] lines = new String[ 128 ];
     for ( int i = 0; i < 128; ++i )
       lines[ i ] = in.readLine();
 
     // read in the notes, which may have the protocol code
     while ( ( line = in.readLine() ) != null )
     {
       fields = LineTokenizer.tokenize( line, delim );
       token = fields.get( 0 );
       if ( token != null )
       {
         if ( token.equals( "Line Notes:" ) || token.equals( "Notes:" ) )
         {
           StringBuilder buff = new StringBuilder();
           boolean first = true;
           String tempDelim = null;
           while ( ( line = in.readLine() ) != null )
           {
             if ( ( line.length() > 0 ) && ( line.charAt( 0 ) == '"' ) )
               tempDelim = "\"";
             else
               tempDelim = delim;
             StringTokenizer st = new StringTokenizer( line, tempDelim );
             if ( st.hasMoreTokens() )
             {
               token = st.nextToken();
               if ( token.startsWith( "EOF Marker" ) )
                 break;
               if ( first )
                 first = false;
               else
                 buff.append( "\n" );
               buff.append( token.trim() );
             }
             else
               buff.append( "\n" );
           }
           notes = buff.toString().trim();
           if ( protocol.getClass() == ManualProtocol.class )
             protocol.importUpgradeCode( notes );
         }
       }
     }
 
     // Parse the function definitions
     ArrayList< ArrayList< String >> unassigned = new ArrayList< ArrayList< String >>();
     for ( int i = 0; i < 128; i++ )
     {
       line = lines[ i ];
       fields = LineTokenizer.tokenize( line, delim );
       String funcName = cleanName( fields.get( 0 ) );
       String code = fields.get( 1 );
       String byte2 = fields.get( 2 );
       @SuppressWarnings( "unused" )
       String buttonName = fields.get( 3 );
       @SuppressWarnings( "unused" )
       String assignedName = fields.get( 4 );
       String notes = fields.size() > 5 ? fields.get( 5 ) : null;
       String pidStr = fields.size() > 6 ? fields.get( 6 ) : null;
       String fixedDataStr = fields.size() > 7 ? fields.get( 7 ) : null;
 
       Function f = null;
       if ( ( code != null ) || ( byte2 != null ) || ( notes != null ) )
       {
         System.err.println( "Creating a new function, because:" );
         System.err.println( "code=" + code );
         System.err.println( "byte2=" + byte2 );
         System.err.println( "notes=" + notes );
 
         boolean isExternal = isExternalFunctionName( funcName );
         if ( isExternal )
         {
           f = new ExternalFunction();
           extFunctions.add( ( ExternalFunction )f );
         }
         else
         {
           f = new Function();
           functions.add( f );
         }
         System.err.println( "Creating function w/ name " + funcName );
         f.setName( funcName );
 
         if ( notes != null )
           f.setNotes( notes );
 
         Hex hex = null;
         if ( f.isExternal() )
         {
           ExternalFunction ef = ( ExternalFunction )f;
           String name = ef.getName();
           int slash = name.indexOf( '/' );
           String devName = name.substring( 1, slash );
           String match = null;
           String[] names = remote.getDeviceTypeAliasNames();
           for ( int j = 0; ( j < names.length ) && ( match == null ); j++ )
           {
             if ( devName.equalsIgnoreCase( names[ j ] ) )
             {
               match = names[ j ];
               break;
             }
           }
           if ( match == null )
           {
             String msg = "The Keymap Master device upgrade you are importing includes an\nexternal function that uses the unknown device type "
                 + devName + ".\n\nPlease select one of the supported device types below to use instead.";
             while ( match == null )
             {
               match = ( String )JOptionPane.showInputDialog( RemoteMaster.getFrame(), msg, "Unsupported Device Type",
                   JOptionPane.ERROR_MESSAGE, null, names, null );
             }
           }
           ef.setDeviceTypeAliasName( match );
           int space = name.indexOf( ' ', slash + 1 );
           String codeString = null;
           if ( space == -1 )
             codeString = name.substring( slash + 1 );
           else
             codeString = name.substring( slash + 1, space );
           ef.setSetupCode( Integer.parseInt( codeString ) );
           if ( ( code.indexOf( 'h' ) != -1 ) || ( code.indexOf( '$' ) != -1 ) || ( code.indexOf( ' ' ) != -1 ) )
           {
             hex = new Hex( code );
             ef.setType( ExternalFunction.HexType );
           }
           else
           {
             hex = new Hex( 1 );
             EFC.toHex( Short.parseShort( code ), hex, 0 );
             ef.setType( ExternalFunction.EFCType );
           }
         }
         else if ( code != null ) // not external and there is a command code
         {
           if ( ( code.indexOf( 'h' ) != -1 ) || ( code.indexOf( '$' ) != -1 ) || ( code.indexOf( ' ' ) != -1 ) )
           {
             hex = new Hex( code );
           }
           else
           {
             hex = protocol.getDefaultCmd();
             protocol.importCommand( hex, code, useOBC, obcIndex, useEFC );
           }
 
           if ( byte2 != null )
             protocol.importCommandParms( hex, byte2 );
         }
         f.setHex( hex );
       }
 
       if ( ( combiner != null ) && ( pidStr != null ) && !pidStr.equals( "Protocol ID" ) )
       {
         Hex fixedData = new Hex();
         if ( fixedDataStr != null )
           fixedData = new Hex( fixedDataStr );
 
         Hex newPid = new Hex( pidStr );
         Protocol p = protocolManager.findProtocolForRemote( remote, newPid, fixedData );
         if ( p != null )
         {
           CombinerDevice dev = new CombinerDevice( p, fixedData );
           combiner.add( dev );
         }
         else
         {
           ManualProtocol mp = new ManualProtocol( newPid, new Properties() );
           mp.setRawHex( fixedData );
           combiner.add( new CombinerDevice( mp, null, null ) );
         }
       }
     }
 
     // Parse the button assignments
     for ( int i = 0; i < 128; i++ )
     {
       line = lines[ i ];
       fields = LineTokenizer.tokenize( line, delim );
       @SuppressWarnings( "unused" )
       String funcName = fields.get( 0 ); // the function being defined, if any (field 1)
       @SuppressWarnings( "unused" )
       String code = fields.get( 1 ); // the EFC or OBC, if any (field 2 )
       @SuppressWarnings( "unused" )
       String byte2 = fields.get( 2 ); // byte2, if any (field 3)
       String actualName = cleanName( fields.get( 3 ) ); // get assigned button name (field 4)
       System.err.println( "actualName='" + actualName + "'" );
       if ( actualName == null )
         continue;
 
       String assignedName = fields.size() > 4 ? fields.get( 4 ) : null; // get assigned functionName
       @SuppressWarnings( "unused" )
       String notes = fields.size() > 5 ? fields.get( 5 ) : null; // get function notes
 
       String shiftAssignedName = fields.size() > 12 ? fields.get( 12 ) : null;
 
       String buttonCode = null;
       if ( buttonCodeIndex != -1 )
       {
         buttonCode = fields.get( buttonCodeIndex );
         if ( buttonCode.length() < 2 )
           buttonCode = null;
       }
 
       String buttonName = null;
       if ( actualName != null )
       {
         if ( i < genericButtonNames.length )
           buttonName = genericButtonNames[ i ];
         else
         {
           System.err.println( "No generic name available!" );
           Button b = remote.getButton( actualName );
           if ( b == null )
             b = remote.getButton( actualName.replace( ' ', '_' ) );
           if ( b != null )
             buttonName = b.getStandardName();
         }
       }
 
       Button b = null;
       if ( buttonCode != null )
       {
         int keyCode = Integer.parseInt( buttonCode, 16 );
         b = remote.getButton( keyCode );
       }
       if ( b == null && buttonName != null )
       {
         System.err.println( "Searching for button w/ name " + buttonName );
         b = remote.findByStandardName( new Button( buttonName, null, ( byte )0, remote ) );
         if ( b == null )
           b = remote.getButton( buttonName );
         System.err.println( "Found button " + b );
       }
       else
         System.err.println( "No buttonName for actualName=" + actualName + " and i=" + i );
 
       if ( ( buttonName != null ) && ( assignedName != null ) && Character.isDigit( assignedName.charAt( 0 ) )
           && Character.isDigit( assignedName.charAt( 1 ) ) && ( assignedName.charAt( 2 ) == ' ' )
           && ( assignedName.charAt( 3 ) == '-' ) && ( assignedName.charAt( 4 ) == ' ' ) )
       {
         String name = cleanName( assignedName.substring( 5 ) );
         if ( ( name.length() == 5 ) && name.startsWith( "num " ) && Character.isDigit( name.charAt( 4 ) ) )
           name = name.substring( 4 );
 
         Function func = null;
         if ( isExternalFunctionName( name ) )
           func = getFunction( name, extFunctions );
         else
           func = getFunction( name, functions );
 
         if ( func == null )
         {
           System.err.println( "Could not find function " + name );
           continue;
         }
         else
           System.err.println( "Found function " + name );
 
         if ( b == null )
         {
           ArrayList< String > temp = new ArrayList< String >( 2 );
           temp.add( name );
           temp.add( actualName );
           unassigned.add( temp );
           System.err.println( "Couldn't find button " + buttonName + " to assign function " + name );
         }
         else if ( loadButtons && ( func.getHex() != null ) )
         {
           System.err.println( "Setting function " + name + " on button " + b );
           assignments.assign( b, func, Button.NORMAL_STATE );
         }
       }
 
       if ( shiftAssignedName != null )
         System.err.println( "shiftAssignedName=" + shiftAssignedName );
       if ( ( shiftAssignedName != null ) && !shiftAssignedName.equals( "" ) )
       {
         String name = cleanName( shiftAssignedName.substring( 5 ) );
         Function func = null;
         if ( isExternalFunctionName( name ) )
           func = getFunction( name, extFunctions );
         else
           func = getFunction( name, functions );
         if ( b == null )
         {
           ArrayList< String > temp = new ArrayList< String >( 2 );
           temp.add( name );
           temp.add( "shift-" + buttonName );
           unassigned.add( temp );
         }
         else if ( loadButtons && ( func != null ) && ( func.getHex() != null ) )
           assignments.assign( b, func, Button.SHIFTED_STATE );
       }
     }
 
     /*
      * if ( !unassigned.isEmpty()) { System.err.println( "Removing undefined functions from usedFunctions" ); for(
      * ListIterator< ArrayList< String >> i = unassigned.listIterator(); i.hasNext(); ) { java.util.List< String > temp
      * = i.next(); String funcName = ( String )temp.get( 0 ); System.err.println( "Checking '" + funcName + "'" );
      * Function f = getFunction( funcName, usedFunctions ); if (( f != null ) && (( f.getHex() == null ) || (
      * f.getHex().length() == 0 ))) { System.err.println( "Removing function " + f + ", which has name '" + funcName +
      * "'" ); i.remove(); } } }
      */
 
     if ( !unassigned.isEmpty() )
     {
       String message = Integer.toString( unassigned.size() ) + " functions defined in the imported device upgrade "
           + "were assigned to buttons that could not be matched by name. "
           + "The functions and the corresponding button names are listed below."
           + "\n\nPlease post this information in the \"JP1 - Software\" section of the "
           + "JP1 Forums at www.hifi-remote.com"
           + "\n\nUse the Button or Layout panel to assign those functions properly.";
 
       JFrame frame = new JFrame( "Import Failure" );
       frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
       Container container = frame.getContentPane();
 
       JTextArea text = new JTextArea( message );
       text.setEditable( false );
       text.setLineWrap( true );
       text.setWrapStyleWord( true );
       text.setBackground( container.getBackground() );
       text.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
       container.add( text, BorderLayout.NORTH );
       java.util.List< String > titles = new ArrayList< String >();
       titles.add( "Function name" );
       titles.add( "Button name" );
       Object[][] unassignedArray = new Object[ unassigned.size() ][];
       int i = 0;
       for ( java.util.List< String > l : unassigned )
         unassignedArray[ i++ ] = l.toArray();
       JTableX table = new JTableX( unassignedArray, titles.toArray() );
       container.add( new JScrollPane( table ), BorderLayout.CENTER );
       /*
        * Dimension d = table.getPreferredScrollableViewportSize(); d.height = table.getPreferredSize().height;
        * table.setPreferredScrollableViewportSize( d );
        */
 
       frame.pack();
       frame.setLocationRelativeTo( RemoteMaster.getFrame() );
       frame.setVisible( true );
     }
     Button[] buttons = remote.getUpgradeButtons();
     System.err.println( "Removing assigned functions with no hex!" );
     for ( int i = 0; i < buttons.length; i++ )
     {
       Button b = buttons[ i ];
       for ( int state = Button.NORMAL_STATE; state <= Button.XSHIFTED_STATE; ++state )
       {
         Function f = assignments.getAssignment( b, state );
         if ( ( f != null ) && ( f.getHex() == null ) )
           assignments.assign( b, null, state );
       }
     }
     System.err.println( "Done!" );
   }
 
   /**
    * Gets the parm values.
    * 
    * @return the parm values
    */
   public Value[] getParmValues()
   {
     return parmValues.clone();
   }
 
   /**
    * Sets the parm values.
    * 
    * @param parmValues
    *          the new parm values
    */
   public void setParmValues( Value[] parmValues )
   {
     this.parmValues = parmValues.clone();
   }
 
   /**
    * Gets the device type alias names.
    * 
    * @return the device type alias names
    */
   public static final String[] getDeviceTypeAliasNames()
   {
     return deviceTypeAliasNames;
   }
 
   /**
    * Auto assign functions.
    */
   public void autoAssignFunctions()
   {
     autoAssignFunctions( functions );
     autoAssignFunctions( extFunctions );
   }
 
   /**
    * Auto assign functions.
    * 
    * @param funcs
    *          the funcs
    */
   private void autoAssignFunctions( java.util.List< ? extends Function > funcs )
   {
     Button[] buttons = remote.getUpgradeButtons();
     for ( Function func : funcs )
     {
       if ( func.getHex() != null )
       {
         for ( int i = 0; i < buttons.length; i++ )
         {
           Button b = buttons[ i ];
           if ( assignments.getAssignment( b ) == null )
           {
             if ( b.getName().equalsIgnoreCase( func.getName() )
                 || b.getStandardName().equalsIgnoreCase( func.getName() ) )
             {
               assignments.assign( b, func );
               break;
             }
           }
         }
       }
     }
   }
 
   /**
    * Check size.
    * 
    * @return true, if successful
    */
   public boolean checkSize()
   {
     Integer protocolLimit = remote.getMaxProtocolLength();
     Integer upgradeLimit = remote.getMaxUpgradeLength();
     Integer combinedLimit = remote.getMaxCombinedUpgradeLength();
 
     if ( ( protocolLimit == null ) && ( upgradeLimit == null ) && ( combinedLimit == null ) )
       return true;
 
     int protocolLength = 0;
     Hex protocolCode = getCode();
     if ( protocolCode != null )
       protocolLength = protocolCode.length();
 
     if ( ( protocolLimit != null ) && ( protocolLength > protocolLimit.intValue() ) )
     {
       JOptionPane.showMessageDialog( RemoteMaster.getFrame(),
           "The protocol upgrade exceeds the maximum allowed by the remote.", "Protocol Upgrade Limit Exceeded",
           JOptionPane.ERROR_MESSAGE );
       return false;
     }
 
     int upgradeLength = getUpgradeLength();
     if ( ( upgradeLimit != null ) && ( upgradeLength > upgradeLimit.intValue() ) )
     {
       JOptionPane.showMessageDialog( RemoteMaster.getFrame(),
           "The device upgrade exceeds the maximum allowed by the remote.", "Device Upgrade Limit Exceeded",
           JOptionPane.ERROR_MESSAGE );
       return false;
     }
 
     int combinedLength = upgradeLength + protocolLength;
     if ( ( combinedLimit != null ) && ( combinedLength > combinedLimit.intValue() ) )
     {
       JOptionPane.showMessageDialog( RemoteMaster.getFrame(),
           "The combined upgrade exceeds the maximum allowed by the remote.", "Combined Upgrade Limit Exceeded",
           JOptionPane.ERROR_MESSAGE );
       return false;
     }
 
     return true;
   }
 
   public boolean needsProtocolCode()
   {
     if ( protocol.needsCode( remote ) )
     {
       return true;
     }
     Translate[] translators = protocol.getCodeTranslators( remote );
     if ( translators != null )
     {
       for ( Translate translate : translators )
       {
         Translator translator = ( Translator )translate;
         int devParmIndex = translator.index;
         Value parmVal = parmValues[ devParmIndex ];
         if ( parmVal.hasUserValue() && !parmVal.getUserValue().equals( parmVal.getDefaultValue() ) )
         {
           return true;
         }
       }
     }
     return false;
   }
 
   /**
    * Gets the code.
    * 
    * @return the code
    */
   public Hex getCode()
   {
     return getCode( protocol );
   }
 
   /**
    * Gets the code.
    * 
    * @param p
    *          the p
    * @return the code
    */
   public Hex getCode( Protocol p )
   {
     Hex code = p.getCode( remote );
     if ( code != null )
     {
       code = remote.getProcessor().translate( code, remote );
       Translate[] xlators = protocol.getCodeTranslators( remote );
       if ( xlators != null )
       {
         Value[] values = getParmValues();
         for ( int i = 0; i < xlators.length; i++ )
           xlators[ i ].in( values, code, null, -1 );
       }
     }
     return code;
   }
 
   /** The description. */
   private String description = null;
 
   /** The setup code. */
   protected int setupCode = 0;
 
   /** The remote. */
   private Remote remote = null;
 
   /** The dev type alias name. */
   private String devTypeAliasName = null;
 
   /** The protocol. */
   protected Protocol protocol = null;
 
   /** The parm values. */
   private Value[] parmValues = new Value[ 0 ];
 
   /** The notes. */
   private String notes = null;
 
   /** The functions. */
   private java.util.List< Function > functions = new ArrayList< Function >();
 
   /** The ext functions. */
   private java.util.List< ExternalFunction > extFunctions = new ArrayList< ExternalFunction >();
   // private java.util.List< KeyMove > keymoves = new ArrayList< KeyMove >();
   /** The file. */
   private File file = null;
 
   /** The property change support. */
   private SwingPropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport( this );
 
   /**
    * Adds the property change listener.
    * 
    * @param listener
    *          the listener
    */
   public void addPropertyChangeListener( PropertyChangeListener listener )
   {
     propertyChangeSupport.addPropertyChangeListener( listener );
   }
 
   /**
    * Adds the property change listener.
    * 
    * @param propertyName
    *          the property name
    * @param listener
    *          the listener
    */
   public void addPropertyChangeListener( String propertyName, PropertyChangeListener listener )
   {
     propertyChangeSupport.addPropertyChangeListener( propertyName, listener );
   }
 
   /**
    * Removes the property change listener.
    * 
    * @param listener
    *          the listener
    */
   public void removePropertyChangeListener( PropertyChangeListener listener )
   {
     propertyChangeSupport.removePropertyChangeListener( listener );
   }
 
   /**
    * Removes the property change listener.
    * 
    * @param propertyName
    *          the property name
    * @param listener
    *          the listener
    */
   public void removePropertyChangeListener( String propertyName, PropertyChangeListener listener )
   {
     propertyChangeSupport.removePropertyChangeListener( propertyName, listener );
   }
 
   /** The assignments. */
   private ButtonAssignments assignments = new ButtonAssignments();
 
   /**
    * Sets the function.
    * 
    * @param b
    *          the b
    * @param f
    *          the f
    * @param state
    *          the state
    */
   public void setFunction( Button b, Function f, int state )
   {
     assignments.assign( b, f, state );
   }
 
   /**
    * Gets the function.
    * 
    * @param b
    *          the b
    * @param state
    *          the state
    * @return the function
    */
   public Function getFunction( Button b, int state )
   {
     return assignments.getAssignment( b, state );
   }
 
   public String toString()
   {
     return devTypeAliasName + '/' + setupCode + '(' + description + ')';
   }
 
   /** The Constant deviceTypeAliasNames. */
   private static final String[] deviceTypeAliasNames =
   {
       "Cable", "TV", "VCR", "CD", "Tuner", "DVD", "SAT", "Tape", "Laserdisc", "DAT", "Home Auto", "Misc Audio",
       "Phono", "Video Acc", "Amp", "PVR", "OEM Mode"
   };
 
   /** The default names. */
   private static String[] defaultNames = null;
 
   /** The Constant defaultFunctionNames. */
   private static final String[] defaultFunctionNames =
   {
       "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "vol up", "vol down", "mute", "channel up", "channel down",
       "power", "enter", "tv/vcr", "last (prev ch)", "menu", "program guide", "up arrow", "down arrow", "left arrow",
       "right arrow", "select", "sleep", "pip on/off", "display", "pip swap", "pip move", "play", "pause", "rewind",
       "fast fwd", "stop", "record", "exit", "surround", "input toggle", "+100", "fav/scan", "device button",
       "next track", "prev track", "shift-left", "shift-right", "pip freeze", "slow", "eject", "slow+", "slow-", "X2",
       "center", "rear"
   };
 
   /** The Constant genericButtonNames. */
   private final static String[] genericButtonNames =
   {
       "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "vol up", "vol down", "mute", "channel up", "channel down",
       "power", "enter", "tv/vcr", "prev ch", "menu", "guide", "up arrow", "down arrow", "left arrow", "right arrow",
       "select", "sleep", "pip on/off", "display", "pip swap", "pip move", "play", "pause", "rewind", "fast fwd",
       "stop", "record", "exit", "surround", "input", "+100", "fav/scan", "device button", "next track", "prev track",
       "shift-left", "shift-right", "pip freeze", "slow", "eject", "slow+", "slow-", "x2", "center", "rear", "phantom1",
       "phantom2", "phantom3", "phantom4", "phantom5", "phantom6", "phantom7", "phantom8", "phantom9", "phantom10",
       "setup", "light", "theater", "macro1", "macro2", "macro3", "macro4", "learn1", "learn2", "learn3", "learn4" // ,
   // "button85", "button86", "button87", "button88", "button89", "button90",
   // "button91", "button92", "button93", "button94", "button95", "button96",
   // "button97", "button98", "button99", "button100", "button101", "button102",
   // "button103", "button104", "button105", "button106", "button107", "button108",
   // "button109", "button110", "button112", "button113", "button114", "button115",
   // "button116", "button117", "button118", "button119", "button120", "button121",
   // "button122", "button123", "button124", "button125", "button126", "button127",
   // "button128", "button129", "button130", "button131", "button132", "button133",
   // "button134", "button135", "button136"
   };
 }
