 package com.hifiremote.jp1;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringReader;
 import java.lang.reflect.Constructor;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.StringTokenizer;
 
 import javax.swing.JOptionPane;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class RemoteConfiguration.
  */
 public class RemoteConfiguration
 {
 
   /**
    * Instantiates a new remote configuration.
    * 
    * @param file
    *          the file
    * @throws IOException
    *           Signals that an I/O exception has occurred.
    */
   public RemoteConfiguration( File file ) throws IOException
   {
     BufferedReader in = new BufferedReader( new FileReader( file ) );
     PropertyReader pr = new PropertyReader( in );
     if ( file.getName().toLowerCase().endsWith( ".rmir" ) )
     {
       parse( pr );
     }
     else
     {
       importIR( pr );
     }
     in.close();
     updateImage();
   }
 
   /**
    * Parses an RMIR file.
    * 
    * @param pr
    *          the pr
    * @throws IOException
    *           Signals that an I/O exception has occurred.
    */
   public void parse( PropertyReader pr ) throws IOException
   {
     IniSection section = pr.nextSection();
 
     if ( section == null )
       throw new IOException( "The file is empty." );
 
     if ( !"General".equals( section.getName() ) )
       throw new IOException( "Doesn't start with a [General] section/" );
 
     remote = RemoteManager.getRemoteManager().findRemoteByName( section.getProperty( "Remote.name" ) );
     SetupCode.setMax( remote.usesTwoBytePID() ? 4095 : 2047 );
     notes = section.getProperty( "Notes" );
 
     deviceButtonNotes = new String[ remote.getDeviceButtons().length ];
 
     loadBuffer( pr );
 
     while ( ( section = pr.nextSection() ) != null )
     {
       String sectionName = section.getName();
 
       if ( sectionName.equals( "DeviceButtonNotes" ) )
       {
         DeviceButton[] buttons = remote.getDeviceButtons();
         for ( int i = 0; i < buttons.length; ++i )
         {
           DeviceButton button = buttons[ i ];
           String note = section.getProperty( button.getName() );
           if ( note != null && !note.equals( "" ) )
           {
             deviceButtonNotes[ i ] = note;
           }
         }
       }
       else if ( sectionName.equals( "Settings" ) )
       {
         for ( Setting setting : remote.getSettings() )
           setting.setValue( Integer.parseInt( section.getProperty( setting.getTitle() ) ) );
       }
       else if ( sectionName.equals( "DeviceUpgrade" ) )
       {
         DeviceUpgrade upgrade = new DeviceUpgrade();
         upgrade.load( section, true, remote );
         devices.add( upgrade );
       }
       else
       {
         try
         {
           Class< ? > c = Class.forName( "com.hifiremote.jp1." + sectionName );
           Constructor< ? > ct = c.getConstructor( Properties.class );
           Object o = ct.newInstance( section );
           if ( o instanceof SpecialProtocolFunction )
             specialFunctions.add( ( SpecialProtocolFunction )o );
           else if ( o instanceof KeyMove )
             keymoves.add( ( KeyMove )o );
           else if ( sectionName.equals( "Macro" ) )
             macros.add( ( Macro )o );
           else if ( sectionName.equals( "ProtocolUpgrade" ) )
             protocols.add( ( ProtocolUpgrade )o );
           else if ( sectionName.equals( "LearnedSignal" ) )
             learned.add( ( LearnedSignal )o );
         }
         catch ( Exception e )
         {
           e.printStackTrace( System.err );
           throw new IOException( "Unable to create instance of " + sectionName );
         }
       }
     }
   }
 
   /**
    * Load buffer.
    * 
    * @param pr
    *          the pr
    * @return the property
    * @throws IOException
    *           Signals that an I/O exception has occurred.
    */
   private Property loadBuffer( PropertyReader pr ) throws IOException
   {
     Property property = pr.nextProperty();
 
     if ( property.name.equals( "[Buffer]" ) || property.name.equals( "" ) )
       property = pr.nextProperty();
 
     int baseAddr = Integer.parseInt( property.name, 16 );
     short[] first = Hex.parseHex( property.value );
 
     if ( remote == null )
     {
       char[] sig = new char[ 8 ];
       for ( int i = 0; i < sig.length; ++i )
         sig[ i ] = ( char )first[ i + 2 ];
 
       String signature = new String( sig );
       String signature2 = null;
       RemoteManager rm = RemoteManager.getRemoteManager();
       List< Remote > remotes = rm.findRemoteBySignature( signature );
       if ( remotes.isEmpty() )
       {
         for ( int i = 0; i < sig.length; ++i )
           sig[ i ] = ( char )first[ i ];
         signature2 = new String( sig );
         remotes = rm.findRemoteBySignature( signature2 );
       }
       if ( ( remotes == null ) || remotes.isEmpty() )
       {
         String message = "No remote found for with signature " + signature + " or " + signature2;
         JOptionPane.showMessageDialog( null, message, "Unknown remote", JOptionPane.ERROR_MESSAGE );
         throw new IllegalArgumentException();
       }
       else if ( remotes.size() == 1 )
       {
         remote = remotes.get( 0 );
       }
       else
       {
         if ( signature2 != null )
           signature = signature2;
         String message = "The file you are loading is for a remote with signature \"" + signature
             + "\".\nThere are multiple remotes with that signature.  Please choose the best match from the list below:";
 
         Remote[] choices = new Remote[ remotes.size() ];
         choices = remotes.toArray( choices );
         remote = ( Remote )JOptionPane.showInputDialog( null, message, "Unknown Remote", JOptionPane.ERROR_MESSAGE,
             null, choices, choices[ 0 ] );
         if ( remote == null )
           throw new IllegalArgumentException( "No matching remote selected for signature " + signature );
       }
     }
     remote.load();
     SetupCode.setMax( remote.usesTwoBytePID() ? 4095 : 2047 );
 
     System.err.println( "Remote is " + remote );
 
     if ( remote.getBaseAddress() != baseAddr )
       throw new IOException( "The base address of the remote image doesn't match the remote's baseAddress." );
 
     deviceButtonNotes = new String[ remote.getDeviceButtons().length];
     data = new short[ remote.getEepromSize() ];
     System.arraycopy( first, 0, data, 0, first.length );
 
     first = null;
     while ( ( property = pr.nextProperty() ) != null )
     {
       if ( property.name.length() == 0 || property.name.startsWith( "[" ) )
         break;
       int offset = Integer.parseInt( property.name, 16 ) - baseAddr;
       Hex.parseHex( property.value, data, offset );
     }
 
     savedData = new short[ data.length ];
     System.arraycopy( data, 0, savedData, 0, data.length );
 
     return property;
   }
 
   /**
    * Find key move.
    * 
    * @param advCodes
    *          the adv codes
    * @param deviceName
    *          the device name
    * @param keyName
    *          the key name
    * @return the key move
    */
   private KeyMove findKeyMove( List< KeyMove > advCodes, String deviceName, String keyName )
   {
     DeviceButton[] deviceButtons = remote.getDeviceButtons();
 
     for ( KeyMove keyMove : advCodes )
     {
       DeviceButton devButton = deviceButtons[ keyMove.getDeviceButtonIndex() ];
       if ( !devButton.getName().equals( deviceName ) )
         continue;
       int keyCode = keyMove.getKeyCode();
       String buttonName = remote.getButtonName( keyCode );
       if ( buttonName.equalsIgnoreCase( keyName ) )
         return keyMove;
     }
     System.err.println( "No keymove found matching " + deviceName + ':' + keyName );
     return null;
   }
 
   /**
    * Find macro.
    * 
    * @param keyName
    *          the key name
    * @return the macro
    */
   private Macro findMacro( String keyName )
   {
     for ( Macro macro : macros )
     {
       int keyCode = macro.getKeyCode();
       String buttonName = remote.getButtonName( keyCode );
       if ( buttonName.equalsIgnoreCase( keyName ) )
         return macro;
     }
     System.err.println( "No macro found assigned to key " + keyName );
     return null;
   }
 
   /**
    * Find protocol upgrade.
    * 
    * @param pid
    *          the pid
    * @return the protocol upgrade
    */
   private ProtocolUpgrade findProtocolUpgrade( int pid )
   {
     for ( ProtocolUpgrade pu : protocols )
     {
       if ( pu.getPid() == pid )
         return pu;
     }
     System.err.println( "No protocol upgrade found w/ pid $" + Integer.toHexString( pid ) );
     return null;
   }
 
   /**
    * Find learned signal.
    * 
    * @param deviceName
    *          the device name
    * @param keyName
    *          the key name
    * @return the learned signal
    */
   private LearnedSignal findLearnedSignal( String deviceName, String keyName )
   {
     DeviceButton[] deviceButtons = remote.getDeviceButtons();
 
     for ( LearnedSignal ls : learned )
     {
       DeviceButton devButton = deviceButtons[ ls.getDeviceButtonIndex() ];
       if ( !devButton.getName().equals( deviceName ) )
         continue;
       int keyCode = ls.getKeyCode();
       String buttonName = remote.getButtonName( keyCode );
       if ( buttonName.equalsIgnoreCase( keyName ) )
         return ls;
     }
     System.err.println( "No learned signal found matching " + deviceName + ':' + keyName );
     return null;
   }
 
   /**
    * Import ir.
    * 
    * @param pr
    *          the pr
    * @throws IOException
    *           Signals that an I/O exception has occurred.
    */
   private void importIR( PropertyReader pr ) throws IOException
   {
     Property property = null;
     if ( pr != null )
       property = loadBuffer( pr );
 
     decodeSettings();
     decodeUpgrades();
     List< AdvancedCode > advCodes = decodeAdvancedCodes();
     decodeLearnedSignals();
 
     if ( pr != null )
     {
       while ( ( property != null ) && ( !property.name.startsWith( "[" ) ) )
       {
         System.err.println( "property.name=" + property.name );
         property = pr.nextProperty();
       }
 
       if ( property == null )
         return;
 
       IniSection section = pr.nextSection();
       section.setName( property.name.substring( 1, property.name.length() - 1 ) );
 
       while ( section != null )
       {
         String name = section.getName();
         if ( name.equals( "Notes" ) )
         {
           System.err.println( "Importing notes" );
           for ( Enumeration< ? > keys = ( Enumeration< ? > )section.propertyNames(); keys.hasMoreElements(); )
           {
             String key = ( String )keys.nextElement();
             String text = section.getProperty( key );
             int base = 10;
             if ( key.charAt( 0 ) == '$' )
             {
               base = 16;
               key = key.substring( 1 );
             }
             int index = Integer.parseInt( key, base );
             int flag = index >> 12;
             index &= 0x0FFF;
             System.err.println( "index=" + index + ", flag=" + flag + ",text=" + text );
             if ( flag == 0 )
               notes = text;
             else if ( flag == 1 )
               advCodes.get( index ).setNotes( text );
             else if ( flag == 2 )
               ;// fav/scan?
             else if ( flag == 3 )
               devices.get( index ).setDescription( text );
             else if ( flag == 4 )
               protocols.get( index ).setNotes( text );
             else if ( flag == 5 )
               learned.get( index ).setNotes( text );
             else if ( flag == 6 )
             {
               deviceButtonNotes[ index ] = text;
             }
           }
         }
         else if ( name.equals( "General" ) )
         {
           for ( Enumeration< ? > keys = section.propertyNames(); keys.hasMoreElements(); )
           {
             String key = ( String )keys.nextElement();
             String text = section.getProperty( key );
             if ( key.equals( "Notes" ) )
               notes = text;
           }
         }
         else if ( name.equals( "KeyMoves" ) )
         {
           for ( Enumeration< ? > keys = section.propertyNames(); keys.hasMoreElements(); )
           {
             String key = ( String )keys.nextElement();
             String text = section.getProperty( key );
             StringTokenizer st = new StringTokenizer( key, ":" );
             String deviceName = st.nextToken();
             String keyName = st.nextToken();
             KeyMove km = findKeyMove( keymoves, deviceName, keyName );
             if ( km != null )
               km.setNotes( text );
           }
         }
         else if ( name.equals( "Macros" ) )
         {
           for ( Enumeration< ? > keys = section.propertyNames(); keys.hasMoreElements(); )
           {
             String keyName = ( String )keys.nextElement();
             String text = section.getProperty( keyName );
             Macro macro = findMacro( keyName );
             if ( macro != null )
               macro.setNotes( text );
           }
         }
         else if ( name.equals( "Devices" ) )
         {
           for ( Enumeration< ? > keys = section.propertyNames(); keys.hasMoreElements(); )
           {
             String key = ( String )keys.nextElement();
             String text = section.getProperty( key );
             StringTokenizer st = new StringTokenizer( key, ": " );
             String deviceTypeName = st.nextToken();
             int setupCode = Integer.parseInt( st.nextToken() );
             DeviceUpgrade device = findDeviceUpgrade( remote.getDeviceType( deviceTypeName ).getNumber(), setupCode );
             if ( device != null )
               device.setDescription( text );
           }
         }
         else if ( name.equals( "Protocols" ) )
         {
           for ( Enumeration< ? > keys = ( Enumeration< ? > )section.propertyNames(); keys.hasMoreElements(); )
           {
             String key = ( String )keys.nextElement();
             String text = section.getProperty( key );
             StringTokenizer st = new StringTokenizer( key, "$" );
             st.nextToken(); // discard the "Protocol: " header
             int pid = Integer.parseInt( st.nextToken(), 16 );
             ProtocolUpgrade protocol = findProtocolUpgrade( pid );
             if ( protocol != null )
               protocol.setNotes( text );
           }
         }
         else if ( name.equals( "Learned" ) )
         {
           for ( Enumeration< ? > keys = section.propertyNames(); keys.hasMoreElements(); )
           {
             String key = ( String )keys.nextElement();
             String text = section.getProperty( key );
             StringTokenizer st = new StringTokenizer( key, ": " );
             String deviceName = st.nextToken();
             String keyName = st.nextToken();
             LearnedSignal ls = findLearnedSignal( deviceName, keyName );
             if ( ls != null )
               ls.setNotes( text );
           }
         }
         section = pr.nextSection();
       }
     }
     migrateKeyMovesToDeviceUpgrades();
 
     // remove protocol upgrades that are used by device upgrades
     for ( Iterator< ProtocolUpgrade > it = protocols.iterator(); it.hasNext(); )
     {
       if ( it.next().isUsed() )
         it.remove();
     }
   }
 
   /**
    * Export advanced code notes.
    * 
    * @param codes
    *          the codes
    * @param index
    *          the index
    * @param out
    *          the out
    * @return the int
    * @throws IOException
    *           Signals that an I/O exception has occurred.
    */
   private int exportAdvancedCodeNotes( List< ? extends AdvancedCode > codes, int index, PrintWriter out )
       throws IOException
   {
     for ( AdvancedCode code : codes )
     {
       String text = code.getNotes();
       if ( text != null )
         out.printf( "$%4X=%s\n", index, exportNotes( text ) );
       ++index;
     }
     return index;
   }
 
   /**
    * Export ir.
    * 
    * @param file
    *          the file
    * @throws IOException
    *           Signals that an I/O exception has occurred.
    */
   public void exportIR( File file ) throws IOException
   {
     updateImage();
     PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( file ) ) );
 
     Hex.print( out, data, remote.getBaseAddress() );
 
     out.println();
     out.println( "[Notes]" );
     // start with the overall notes
     if ( notes != null )
       out.println( "$0000=" + exportNotes( notes ) );
 
     // Do the advanced codes
     int i = 0x1000;
     i = exportAdvancedCodeNotes( keymoves, i, out );
     i = exportAdvancedCodeNotes( upgradeKeyMoves, i, out );
     i = exportAdvancedCodeNotes( specialFunctions, i, out );
     i = exportAdvancedCodeNotes( macros, i, out );
 
     // Do the Favs????
     i = 0x2000;
 
     // Do the device upgrades
     i = 0x3000;
     for ( DeviceUpgrade device : devices )
     {
       String text = device.getDescription();
       if ( text != null )
         out.printf( "$%4X=%s\n", i, exportNotes( text ) );
       ++i;
     }
 
     // Do the protocol upgrades
     LinkedHashMap< Integer, ProtocolUpgrade > requiredProtocols = new LinkedHashMap< Integer, ProtocolUpgrade >();
     for ( DeviceUpgrade dev : devices )
     {
       Hex pCode = dev.getCode();
       if ( pCode != null )
       {
         Protocol p = dev.getProtocol();
         Hex pid = p.getID();
         if ( !requiredProtocols.containsKey( pid ) )
           requiredProtocols.put( pid.get( 0 ), new ProtocolUpgrade( pid.get( 0 ), pCode, p.getName() ) );
       }
     }
 
     for ( ProtocolUpgrade pu : protocols )
       requiredProtocols.put( pu.getPid(), pu );
 
     i = 0x4000;
     for ( ProtocolUpgrade protocol : requiredProtocols.values() )
     {
       String text = protocol.getNotes();
       if ( text != null )
         out.printf( "$%4X=%s\n", i, exportNotes( text ) );
       ++i;
     }
 
     // Do the learned signals
     i = 0x5000;
     for ( LearnedSignal signal : learned )
     {
       String text = signal.getNotes();
       if ( text != null )
         out.printf( "$%4X=%s\n", i, exportNotes( text ) );
       ++i;
     }
     out.close();
   }
 
   /**
    * Find device upgrade.
    * 
    * @param deviceButton
    *          the device button
    * @return the device upgrade
    */
   private DeviceUpgrade findDeviceUpgrade( DeviceButton deviceButton )
   {
     return findDeviceUpgrade( deviceButton.getDeviceTypeIndex( data ), deviceButton.getSetupCode( data ) );
   }
 
   /*
    * private DeviceUpgrade findDeviceUpgrade( int deviceTypeSetupCode ) { int deviceTypeIndex = deviceTypeSetupCode >>
    * 12; int setupCode = deviceTypeSetupCode & 0x7FF; return findDeviceUpgrade( deviceTypeIndex, setupCode ); }
    */
 
   /**
    * Find device upgrade.
    * 
    * @param deviceTypeIndex
    *          the device type index
    * @param setupCode
    *          the setup code
    * @return the device upgrade
    */
   public DeviceUpgrade findDeviceUpgrade( int deviceTypeIndex, int setupCode )
   {
     System.err.println( "in findDeviceUpgrade( " + deviceTypeIndex + ", " + setupCode + " )" );
     for ( DeviceUpgrade deviceUpgrade : devices )
     {
       System.err.println( "Checking " + deviceUpgrade );
       if ( ( deviceTypeIndex == deviceUpgrade.getDeviceType().getNumber() )
           && ( setupCode == deviceUpgrade.getSetupCode() ) )
       {
         System.err.println( "It's a match!" );
         return deviceUpgrade;
       }
     }
     System.err.println( "No match found!" );
     return null;
   }
 
   /**
    * Find bound device button index.
    * 
    * @param upgrade
    *          the upgrade
    * @return the int
    */
   public int findBoundDeviceButtonIndex( DeviceUpgrade upgrade )
   {
     int deviceTypeIndex = upgrade.getDeviceType().getNumber();
     int setupCode = upgrade.getSetupCode();
     return findBoundDeviceButtonIndex( deviceTypeIndex, setupCode );
   }
 
   public int findBoundDeviceButtonIndex( int deviceTypeIndex, int setupCode )
   {
     DeviceButton[] deviceButtons = remote.getDeviceButtons();
     for ( int i = 0; i < deviceButtons.length; ++i )
     {
       DeviceButton deviceButton = deviceButtons[ i ];
       if ( ( deviceButton.getDeviceTypeIndex( data ) == deviceTypeIndex )
           && ( deviceButton.getSetupCode( data ) == setupCode ) )
         return i;
     }
     return -1;
   }
 
   /**
    * Instantiates a new remote configuration.
    * 
    * @param remote
    *          the remote
    */
   public RemoteConfiguration( Remote remote )
   {
     this.remote = remote;
     SetupCode.setMax( remote.usesTwoBytePID() ? 4095 : 2047 );
 
     data = new short[ remote.getEepromSize() ];
   }
 
   /**
    * Parses the data.
    * 
    * @throws IOException
    *           Signals that an I/O exception has occurred.
    */
   public void parseData() throws IOException
   {
     importIR( null );
     /*
      * decodeSettings(); decodeUpgrades();
      * 
      * // remove protocol upgrades that are used by device upgrades for ( Iterator< ProtocolUpgrade > it =
      * protocols.iterator(); it.hasNext(); ) { if ( it.next().isUsed()) it.remove(); }
      * 
      * decodeAdvancedCodes(); migrateKeyMovesToDeviceUpgrades(); decodeLearnedSignals();
      */
   }
 
   /**
    * Decode settings.
    */
   public void decodeSettings()
   {
     Setting[] settings = remote.getSettings();
     for ( Setting setting : settings )
       setting.decode( data );
   }
 
   /**
    * Gets the special protocols.
    * 
    * @return the special protocols
    */
   public List< SpecialProtocol > getSpecialProtocols()
   {
     // Determine which upgrades are special protocol upgrades
     List< SpecialProtocol > availableSpecialProtocols = new ArrayList< SpecialProtocol >();
     List< SpecialProtocol > specialProtocols = remote.getSpecialProtocols();
     for ( SpecialProtocol sp : specialProtocols )
     {
       if ( sp.isPresent( this ) )
       {
         availableSpecialProtocols.add( sp );
       }
     }
     return availableSpecialProtocols;
   }
 
   /**
    * Decode advanced codes.
    * 
    * @return the list< advanced code>
    */
   private List< AdvancedCode > decodeAdvancedCodes()
   {
     // Determine which upgrades are special protocol upgrades
     List< DeviceUpgrade > specialUpgrades = new ArrayList< DeviceUpgrade >();
     List< SpecialProtocol > specialProtocols = remote.getSpecialProtocols();
     for ( SpecialProtocol sp : specialProtocols )
     {
       System.err.println( "Checking for Special Procotol " + sp.getName() + " w/ PID=" + sp.getPid().toString() );
       DeviceUpgrade device = sp.getDeviceUpgrade( devices );
       if ( device != null )
       {
         specialUpgrades.add( device );
         System.err.println( "SpecialFunction Upgrade at " + device.getDeviceType().getName() + "/"
             + device.getSetupCode() );
       }
     }
 
     List< AdvancedCode > advCodes = new ArrayList< AdvancedCode >();
     HexReader reader = new HexReader( data, remote.getAdvancedCodeAddress() );
     AdvancedCode advCode = null;
     while ( ( advCode = AdvancedCode.read( reader, remote ) ) != null )
     {
       if ( advCode instanceof Macro )
       {
         Macro macro = ( Macro )advCode;
         macros.add( macro );
         advCodes.add( macro );
       }
       else
       {
         KeyMove keyMove = ( KeyMove )advCode;
         SpecialProtocol sp = getSpecialProtocol( keyMove, specialUpgrades );
         if ( sp != null )
         {
           SpecialProtocolFunction sf = sp.createFunction( keyMove );
           if ( sf != null )
           {
             specialFunctions.add( sf );
             advCodes.add( sf );
           }
         }
         else
         {
           keymoves.add( keyMove );
           advCodes.add( keyMove );
         }
       }
     }
     return advCodes;
   }
 
   /**
    * Migrate key moves to device upgrades.
    */
   private void migrateKeyMovesToDeviceUpgrades()
   {
     for ( Iterator< KeyMove > it = keymoves.iterator(); it.hasNext(); )
     {
       KeyMove keyMove = it.next();
 
       // ignore key-style keymoves
       if ( keyMove.getClass() == KeyMoveKey.class )
       {
         continue;
       }
 
       int keyCode = keyMove.getKeyCode();
 
       // check if the keymove comes from a device upgrade
       DeviceButton boundDeviceButton = remote.getDeviceButtons()[ keyMove.getDeviceButtonIndex() ];
       DeviceUpgrade boundUpgrade = findDeviceUpgrade( boundDeviceButton );
       DeviceUpgrade moveUpgrade = findDeviceUpgrade( keyMove.getDeviceType(), keyMove.getSetupCode() );
       if ( ( boundUpgrade != null ) && ( boundUpgrade == moveUpgrade ) )
       {
         System.err.println( "Moving keymove on " + boundDeviceButton + ':'
             + remote.getButtonName( keyMove.getKeyCode() ) + " to device upgrade " + boundUpgrade.getDeviceType() + '/'
             + boundUpgrade.getSetupCode() );
         it.remove();
         // Add the keymove to the device upgrade instead of the keymove collection
         Hex cmd = keyMove.getCmd();
         Function f = boundUpgrade.getFunction( cmd );
         if ( f == null )
         {
           String text = keyMove.getNotes();
           if ( text == null )
             text = remote.getButtonName( keyCode );
           f = new Function( text, cmd, null );
           boundUpgrade.getFunctions().add( f );
         }
         int state = Button.NORMAL_STATE;
         Button b = remote.getButton( keyCode );
         if ( b == null )
         {
           int baseCode = keyCode & 0x3F;
           if ( baseCode != 0 )
           {
             b = remote.getButton( baseCode );
             if ( ( baseCode | remote.getShiftMask() ) == keyCode )
               state = Button.SHIFTED_STATE;
             if ( ( baseCode | remote.getXShiftMask() ) == keyCode )
               state = Button.XSHIFTED_STATE;
           }
           else
           {
             baseCode = keyCode & ~remote.getShiftMask();
             b = remote.getButton( baseCode );
             if ( b != null )
               state = Button.SHIFTED_STATE;
             else
             {
               baseCode = keyCode & ~remote.getXShiftMask();
               b = remote.getButton( baseCode );
               if ( b != null )
                 state = Button.XSHIFTED_STATE;
             }
           }
         }
         boundUpgrade.setFunction( b, f, state );
       }
     }
   }
 
   /**
    * Gets the device button index.
    * 
    * @param upgrade
    *          the upgrade
    * @return the device button index
    */
   public int getDeviceButtonIndex( DeviceUpgrade upgrade )
   {
     DeviceButton[] deviceButtons = remote.getDeviceButtons();
     for ( int i = 0; i < deviceButtons.length; ++i )
     {
       DeviceButton button = deviceButtons[ i ];
       if ( ( button.getDeviceTypeIndex( data ) == upgrade.getDeviceType().getNumber() )
           && ( button.getSetupCode( data ) == upgrade.getSetupCode() ) )
         return i;
     }
     return -1;
   }
   
   public DeviceUpgrade getAssignedDeviceUpgrade( DeviceButton deviceButton )
   {
     DeviceType deviceType = remote.getDeviceTypeByIndex( deviceButton.getDeviceTypeIndex( data ) );
     int setupCode = deviceButton.getSetupCode( data );
     DeviceUpgrade upgrade = null;
     for ( DeviceUpgrade candidate : devices )
     {
       if ( candidate.setupCode == setupCode && candidate.getDeviceType() == deviceType )
       {
         upgrade = candidate;
         break;
       }
     }
     return upgrade;
   }
 
   /**
    * Gets the special protocol.
    * 
    * @param upgrade
    *          the upgrade
    * @return the special protocol
    */
   public SpecialProtocol getSpecialProtocol( DeviceUpgrade upgrade )
   {
     for ( SpecialProtocol sp : remote.getSpecialProtocols() )
     {
       if ( upgrade.getProtocol().getID().equals( sp.getPid() ) )
         return sp;
     }
     return null;
   }
 
   private SpecialProtocol getSpecialProtocol( KeyMove keyMove, List< DeviceUpgrade > specialUpgrades )
   {
     System.err.println( "getSpecialProtocol" );
     int setupCode = keyMove.getSetupCode();
     int deviceType = keyMove.getDeviceType();
     System.err.println( "getSpecialProtocol: looking for " + deviceType + '/' + setupCode );
     for ( SpecialProtocol sp : remote.getSpecialProtocols() )
     {
       System.err.println( "Checking " + sp );
       if ( sp.isPresent( this ) )
       {
         if ( ( setupCode == sp.getSetupCode() ) && ( deviceType == sp.getDeviceType().getNumber() ) )
         {
           return sp;
         }
       }
     }
 
     DeviceUpgrade moveUpgrade = findDeviceUpgrade( keyMove.getDeviceType(), keyMove.getSetupCode() );
     if ( ( moveUpgrade != null ) && specialUpgrades.contains( moveUpgrade ) )
     {
       return getSpecialProtocol( moveUpgrade );
     }
 
     return null;
   }
 
   private int getAdvancedCodesBytesNeeded( List< ? extends AdvancedCode > codes )
   {
     int count = 0;
     for ( AdvancedCode code : codes )
     {
       count += code.getSize( remote ); // the key code and type/length
     }
     return count;
   }
 
   public int getAdvancedCodeBytesNeeded()
   {
     int size = getAdvancedCodesBytesNeeded( keymoves );
     upgradeKeyMoves = getUpgradeKeyMoves();
     size += getAdvancedCodesBytesNeeded( upgradeKeyMoves );
     size += getAdvancedCodesBytesNeeded( specialFunctions );
     size += getAdvancedCodesBytesNeeded( macros );
     size++ ; // the section terminator
     return size;
   }
 
   public int getAdvancedCodeBytesAvailable()
   {
     return remote.getAdvancedCodeAddress().getSize();
   }
 
   public void checkUnassignedUpgrades()
   {
     for ( DeviceUpgrade device : devices )
     {
       int boundDeviceButtonIndex = findBoundDeviceButtonIndex( device );
       if ( !device.getKeyMoves().isEmpty() && boundDeviceButtonIndex == -1 )
       {
         // upgrade includes keymoves but isn't bound to a device button.
         DeviceButton[] devButtons = remote.getDeviceButtons();
         DeviceButton devButton = ( DeviceButton )JOptionPane
             .showInputDialog(
                 RemoteMaster.getFrame(),
                 "The device upgrade \""
                     + device.toString()
                     + "\" uses keymoves.\n\nThese keymoves will not be available unless it is assigned to a device button.\n\nIf you like to assign this device upgrade to a device button?\nTo assign it, select the desired device button and press OK.  Otherwise please press Cancel.",
                 "Unassigned Device Upgrade", JOptionPane.QUESTION_MESSAGE, null, devButtons, null );
         if ( devButton != null )
         {
           devButton.setSetupCode( ( short )device.getSetupCode(), data );
           devButton.setDeviceTypeIndex( ( short )remote.getDeviceTypeByAliasName( device.getDeviceTypeAliasName() )
               .getNumber(), data );
         }
       }
     }
   }
 
   /**
    * Update image.
    */
   public void updateImage()
   {
     updateFixedData();
     updateAutoSet();
     updateSettings();
     updateAdvancedCodes();
     updateUpgrades();
     updateLearnedSignals();
     updateCheckSums();
 
     checkImageForByteOverflows();
   }
 
   private void checkImageForByteOverflows()
   {
     for ( int i = 0; i < data.length; i++ )
     {
       short s = data[ i ];
       if ( ( s & 0xFF00 ) != 0 )
       {
         String message = String.format( "Overflow at %04X: %04X", i, s );
         System.err.println( message );
         JOptionPane.showMessageDialog( null, message );
       }
     }
   }
 
   /**
    * Update key moves.
    * 
    * @param moves
    *          the moves
    * @param offset
    *          the offset
    * @return the int
    */
   private int updateKeyMoves( List< ? extends KeyMove > moves, int offset )
   {
     for ( KeyMove keyMove : moves )
     {
       offset = keyMove.store( data, offset, remote );
     }
     return offset;
   }
 
   /**
    * Gets the upgrade key moves.
    * 
    * @return the upgrade key moves
    */
   public List< KeyMove > getUpgradeKeyMoves()
   {
     List< KeyMove > rc = new ArrayList< KeyMove >();
     for ( DeviceUpgrade device : devices )
     {
       int devButtonIndex = getDeviceButtonIndex( device );
       if ( devButtonIndex == -1 )
         continue;
       for ( KeyMove keyMove : device.getKeyMoves() )
       {
         keyMove.setDeviceButtonIndex( devButtonIndex );
         rc.add( keyMove );
       }
     }
     return rc;
   }
 
   /**
    * Update advanced codes.
    * 
    * @return the int
    */
   private void updateAdvancedCodes()
   {
     AddressRange range = remote.getAdvancedCodeAddress();
     int offset = range.getStart();
     offset = updateKeyMoves( keymoves, offset );
     upgradeKeyMoves = getUpgradeKeyMoves();
     offset = updateKeyMoves( upgradeKeyMoves, offset );
     offset = updateKeyMoves( specialFunctions, offset );
 
     HashMap< Button, List< Macro >> multiMacros = new HashMap< Button, List< Macro >>();
     for ( Macro macro : macros )
     {
       int keyCode = macro.getKeyCode();
       Button button = remote.getButton( keyCode );
       if ( button != null )
       {
         MultiMacro multiMacro = button.getMultiMacro();
         if ( multiMacro != null )
         {
           List< Macro > list = multiMacros.get( button );
           if ( list == null )
           {
             list = new ArrayList< Macro >();
             multiMacros.put( button, list );
           }
           list.add( macro );
           macro.setSequenceNumber( list.size() );
         }
       }
       offset = macro.store( data, offset, remote );
     }
     data[ offset++ ] = remote.getSectionTerminator();
 
     // Fill the rest of the advance code section with the section terminator
     while ( offset < range.getEnd() )
     {
       data[ offset++ ] = remote.getSectionTerminator();
     }
 
     // Update the multiMacros
     for ( Map.Entry< Button, List< Macro >> entry : multiMacros.entrySet() )
     {
       Button button = entry.getKey();
       List< Macro > macros = entry.getValue();
       MultiMacro multiMacro = button.getMultiMacro();
       multiMacro.setCount( macros.size() );
       multiMacro.store( data, remote );
     }
   }
 
   /**
    * Update check sums.
    */
   private void updateCheckSums()
   {
     CheckSum[] sums = remote.getCheckSums();
     for ( int i = 0; i < sums.length; ++i )
       sums[ i ].setCheckSum( data );
   }
 
   /**
    * Update settings.
    */
   private void updateSettings()
   {
     Setting[] settings = remote.getSettings();
     for ( Setting setting : settings )
       setting.store( data );
   }
 
   private void updateFixedData()
   {
     FixedData[] fixedData = remote.getFixedData();
     if ( fixedData == null )
       return;
     for ( FixedData fixed : fixedData )
     {
       fixed.store( data );
     }
   }
 
   private void updateAutoSet()
   {
     FixedData[] autoSet = remote.getAutoSet();
     if ( autoSet == null )
       return;
     for ( FixedData auto : autoSet )
     {
       auto.store( data );
     }
   }
 
   /**
    * Gets the protocol.
    * 
    * @param pid
    *          the pid
    * @return the protocol
    */
   private ProtocolUpgrade getProtocol( int pid )
   {
     for ( ProtocolUpgrade pu : protocols )
     {
       if ( pu.getPid() == pid )
         return pu;
     }
     return null;
   }
 
   /**
    * Gets the limit.
    * 
    * @param offset
    *          the offset
    * @param bounds
    *          the bounds
    * @return the limit
    */
   private int getLimit( int offset, int[] bounds )
   {
     int limit = remote.getEepromSize();
     for ( int i = 0; i < bounds.length; ++i )
     {
       if ( ( bounds[ i ] != 0 ) && ( offset < bounds[ i ] ) && ( limit > bounds[ i ] ) )
         limit = bounds[ i ];
     }
     return limit;
   }
 
   /**
    * Decode upgrades.
    */
   private void decodeUpgrades()
   {
     AddressRange addr = remote.getUpgradeAddress();
 
     Processor processor = remote.getProcessor();
     // get the offsets to the device and protocol tables
     int deviceTableOffset = processor.getInt( data, addr.getStart() ) - remote.getBaseAddress(); // get offset of device
     // table
     int protocolTableOffset = processor.getInt( data, addr.getStart() + 2 ) - remote.getBaseAddress(); // get offset of
     // protocol table
 
     // build an array containing the ends of all the possible ranges
 
     int[] bounds = new int[ 7 ];
     bounds[ 0 ] = 0; // leave space for the next entry in the table
     bounds[ 1 ] = 0; // leave space for the 1st protocol code
     bounds[ 2 ] = deviceTableOffset;
     bounds[ 3 ] = protocolTableOffset;
     bounds[ 4 ] = addr.getEnd() - 1;
     bounds[ 5 ] = remote.getAdvancedCodeAddress().getEnd() - 1;
     if ( remote.getLearnedAddress() != null )
       bounds[ 6 ] = remote.getLearnedAddress().getEnd() - 1;
     else
       bounds[ 6 ] = 0;
 
     // parse the protocol tables
     int offset = protocolTableOffset;
     int count = processor.getInt( data, offset ); // get number of entries in upgrade table
     offset += 2; // skip to first entry
 
     for ( int i = 0; i < count; ++i )
     {
       int pid = processor.getInt( data, offset );
       int codeOffset = processor.getInt( data, offset + 2 * count ) - remote.getBaseAddress();
       if ( i == 0 )
         bounds[ 1 ] = codeOffset; // save the offset of the first protocol code
       if ( i == count - 1 ) // the last entry, so there is no next extry
         bounds[ 0 ] = 0;
       else
         bounds[ 0 ] = processor.getInt( data, offset + 2 * ( count + 1 ) ) - remote.getBaseAddress();
 
       int limit = getLimit( codeOffset, bounds );
       Hex code = Hex.subHex( data, codeOffset, limit - codeOffset );
       protocols.add( new ProtocolUpgrade( pid, code, null ) );
 
       offset += 2; // for the next upgrade
     }
 
     // now parse the devices
     offset = deviceTableOffset;
     count = processor.getInt( data, offset ); // get number of entries in upgrade table
     for ( int i = 0; i < count; ++i )
     {
       offset += 2;
       
       int fullCode = processor.getInt( data, offset );
       int setupCode = fullCode & 0xFFF;
       if ( !remote.usesTwoBytePID() )
       {
         setupCode &= 0x7FF;
       }
       DeviceType devType = remote.getDeviceTypeByIndex( (fullCode >> 12) & 0xF );
       int codeOffset = offset + 2 * count; // compute offset to offset of upgrade code
       codeOffset = processor.getInt( data, codeOffset ) - remote.getBaseAddress(); // get offset of upgrade code
       int pid = data[ codeOffset ];
       if ( remote.usesTwoBytePID() )
         pid = processor.getInt( data, codeOffset );
       else
       {
        if ( ( fullCode & 0x800 ) == 0x800 ) // pid > 0xFF
           pid += 0x100;
       }
 
       if ( i == count - 1 )
         bounds[ 0 ] = 0;
       else
         bounds[ 0 ] = processor.getInt( data, offset + 2 * ( count + 1 ) ) - remote.getBaseAddress(); // next device
       // upgrade
       int limit = getLimit( offset, bounds );
       Hex deviceHex = Hex.subHex( data, codeOffset, limit - codeOffset );
       ProtocolUpgrade pu = getProtocol( pid );
       Hex protocolCode = null;
       if ( pu != null )
       {
         pu.setUsed( true );
         protocolCode = pu.getCode();
       }
 
       String alias = remote.getDeviceTypeAlias( devType );
       if ( alias == null )
       {
         String message = String
             .format(
                 "No device type alias found for device upgrade %1$s/%2$04d.  The device upgrade could not be imported and was discarded.",
                 devType, setupCode );
         JOptionPane.showMessageDialog( null, message, "Protocol Code Mismatch", JOptionPane.ERROR_MESSAGE );
         continue;
       }
 
       short[] pidHex = new short[ 2 ];
       pidHex[ 0 ] = ( short )( ( pid > 0xFF ) ? 1 : 0 );
       pidHex[ 1 ] = ( short )( pid & 0xFF );
 
       DeviceUpgrade upgrade = new DeviceUpgrade();
       try
       {
         upgrade.importRawUpgrade( deviceHex, remote, alias, new Hex( pidHex ), protocolCode );
         upgrade.setSetupCode( setupCode );
 
         devices.add( upgrade );
       }
       catch ( java.text.ParseException pe )
       {
         pe.printStackTrace( System.err );
       }
     }
   }
 
   public HashMap< Integer, ProtocolUpgrade > getRequiredProtocolUpgrades()
   {
     // Build a list of the required protocol upgrades
     LinkedHashMap< Integer, ProtocolUpgrade > requiredProtocols = new LinkedHashMap< Integer, ProtocolUpgrade >();
     for ( DeviceUpgrade dev : devices )
     {
       if ( dev.needsProtocolCode() )
       {
         Hex pCode = dev.getCode();
         Protocol p = dev.getProtocol();
         int pid = p.getID().get( 0 );
         ProtocolUpgrade pu = requiredProtocols.get( pid );
         if ( pu == null )
         {
           requiredProtocols.put( pid, new ProtocolUpgrade( pid, pCode, p.getName() ) );
         }
         else
         {
           if ( !pu.getCode().equals( pCode ) )
           {
             String message = "The protocol code used by the device upgrade for " + dev.getDeviceTypeAliasName() + '/'
                 + dev.getSetupCode()
                 + " is different than the code already used by another device upgrade, and may not work as intended.";
             JOptionPane.showMessageDialog( null, message, "Protocol Code Mismatch", JOptionPane.ERROR_MESSAGE );
           }
         }
       }
     }
 
     // The installed protocols that aren't used by any device upgrade.
     for ( ProtocolUpgrade pu : protocols )
       requiredProtocols.put( pu.getPid(), pu );
 
     return requiredProtocols;
   }
 
   /**
    * Gets the upgrade code bytes used.
    * 
    * @return the upgrade code bytes used
    */
   public int getUpgradeCodeBytesNeeded()
   {
     int size = 4; // Allow for the table pointers
 
     int devCount = devices.size();
 
     HashMap< Integer, ProtocolUpgrade > requiredProtocols = getRequiredProtocolUpgrades();
     // Calculate the size of the upgrade table
 
     int prCount = requiredProtocols.size();
 
     // Handle the special case where there are no upgrades installed
     if ( ( devCount == 0 ) && ( prCount == 0 ) )
     {
       return size;
     }
 
     // the device upgrades
     for ( DeviceUpgrade upgrade : devices )
     {
       size += upgrade.getUpgradeHex().length();
     }
 
     // the protocol upgrades
     for ( ProtocolUpgrade upgrade : requiredProtocols.values() )
     {
       size += upgrade.getCode().length();
     }
 
     // The device upgrade table
     size += 2; // the count
     size += 4 * devCount; // the setup code and offset for each upgrade
 
     // The protocol upgrade table
     size += 2; // the count
     size += 4 * prCount; // the pid and offset for each upgrade
 
     return size;
   }
 
   /**
    * Update upgrades.
    * 
    * @return the int
    */
   private void updateUpgrades()
   {
     AddressRange addr = remote.getUpgradeAddress();
     int offset = addr.getStart() + 4; // skip over the table pointers
     int devCount = devices.size();
 
     // Build a list of the required protocol upgrades
     LinkedHashMap< Integer, ProtocolUpgrade > requiredProtocols = new LinkedHashMap< Integer, ProtocolUpgrade >();
     for ( DeviceUpgrade dev : devices )
     {
       if ( dev.needsProtocolCode() )
       {
         Hex pCode = dev.getCode();
         Protocol p = dev.getProtocol();
         int pid = p.getID().get( 0 );
         ProtocolUpgrade pu = requiredProtocols.get( pid );
         if ( pu == null )
         {
           requiredProtocols.put( pid, new ProtocolUpgrade( pid, pCode, p.getName() ) );
         }
         else
         {
           if ( !pu.getCode().equals( pCode ) )
           {
             String message = "The protocol code used by the device upgrade for " + dev.getDeviceTypeAliasName() + '/'
                 + dev.getSetupCode()
                 + " is different than the code already used by another device upgrade, and may not work as intended.";
             JOptionPane.showMessageDialog( null, message, "Protocol Code Mismatch", JOptionPane.ERROR_MESSAGE );
           }
         }
       }
     }
 
     // The installed protocols that aren't used by any device upgrade.
     for ( ProtocolUpgrade pu : protocols )
       requiredProtocols.put( pu.getPid(), pu );
 
     // Calculate the size of the upgrade table
 
     int prCount = requiredProtocols.size();
 
     Processor processor = remote.getProcessor();
     // Handle the special case where there are no upgrades installed
     if ( ( devCount == 0 ) && ( prCount == 0 ) )
     {
       processor.putInt( offset + remote.getBaseAddress(), data, addr.getStart() );
       processor.putInt( offset + remote.getBaseAddress(), data, addr.getStart() + 2 );
       processor.putInt( 0, data, offset );
       return;
     }
 
     // store the device upgrades
     int[] devOffsets = new int[ devCount ];
     int i = 0;
     for ( DeviceUpgrade dev : devices )
     {
       devOffsets[ i++ ] = offset;
       Hex hex = dev.getUpgradeHex();
       Hex.put( hex, data, offset );
       offset += hex.length();
     }
 
     // store the protocol upgrades
     int[] prOffsets = new int[ prCount ];
     i = 0;
     for ( ProtocolUpgrade upgrade : requiredProtocols.values() )
     {
       prOffsets[ i++ ] = offset;
       Hex hex = upgrade.getCode();
       Hex.put( hex, data, offset );
       offset += hex.length();
     }
 
     // set the pointer to the device table.
     processor.putInt( offset + remote.getBaseAddress(), data, addr.getStart() );
 
     // create the device table
     processor.putInt( devCount, data, offset );
     offset += 2;
     // store the setup codes
     for ( DeviceUpgrade dev : devices )
     {
       Hex.put( dev.getHexSetupCode(), data, offset );
       offset += 2;
     }
     // store the offsets
     for ( int devOffset : devOffsets )
     {
       processor.putInt( devOffset + remote.getBaseAddress(), data, offset );
       offset += 2;
     }
 
     // set the pointer to the protocol table
     processor.putInt( offset + remote.getBaseAddress(), data, addr.getStart() + 2 );
 
     // create the protocol table
     processor.putInt( prCount, data, offset );
     offset += 2;
     for ( ProtocolUpgrade pr : requiredProtocols.values() )
     {
       processor.putInt( pr.getPid(), data, offset );
       offset += 2;
     }
     for ( i = 0; i < prCount; ++i )
     {
       processor.putInt( prOffsets[ i ] + remote.getBaseAddress(), data, offset );
       offset += 2;
     }
   }
 
   /**
    * Decode learned signals.
    */
   public void decodeLearnedSignals()
   {
     AddressRange addr = remote.getLearnedAddress();
     if ( addr == null )
       return;
     HexReader reader = new HexReader( data, addr );
 
     LearnedSignal signal = null;
     while ( ( signal = LearnedSignal.read( reader, remote ) ) != null )
     {
       learned.add( signal );
     }
   }
 
   /**
    * Gets the learned signal bytes used.
    * 
    * @return the learned signal bytes used
    */
   public int getLearnedSignalBytesNeeded()
   {
     int size = 0;
     if ( remote.getLearnedAddress() == null )
       return 0;
 
     for ( LearnedSignal ls : learned )
     {
       size += ls.getSize();
     }
     size += 1; // section terminator;
     return size;
   }
 
   /**
    * Update learned signals.
    * 
    * @return the int
    */
   private void updateLearnedSignals()
   {
     AddressRange addr = remote.getLearnedAddress();
     if ( addr == null )
       return;
 
     int offset = addr.getStart();
     for ( LearnedSignal ls : learned )
     {
       offset = ls.store( data, offset, remote );
     }
     data[ offset ] = remote.getSectionTerminator();
   }
 
   /**
    * Save.
    * 
    * @param file
    *          the file
    * @throws IOException
    *           Signals that an I/O exception has occurred.
    */
   public void save( File file ) throws IOException
   {
     PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( file ) ) );
     PropertyWriter pw = new PropertyWriter( out );
 
     pw.printHeader( "General" );
     pw.print( "Remote.name", remote.getName() );
     pw.print( "Remote.signature", remote.getSignature() );
     pw.print( "Notes", notes );
 
     pw.printHeader( "Buffer" );
     int base = remote.getBaseAddress();
     for ( int i = 0; i < data.length; i += 16 )
     {
       pw.print( String.format( "%04X", i + base ), Hex.toString( data, i, 16 ) );
     }
 
     boolean haveNotes = false;
     for ( String note : deviceButtonNotes )
     {
       if ( note != null )
       {
         haveNotes = true;
         break;
       }
     }
 
     if ( haveNotes )
     {
       pw.printHeader( "DeviceButtonNotes" );
       DeviceButton[] deviceButtons = remote.getDeviceButtons();
       for ( int i = 0; i < deviceButtonNotes.length; ++i )
       {
         String note = deviceButtonNotes[ i ];
         if ( note != null )
         {
           pw.print( deviceButtons[ i ].getName(), note );
         }
       }
     }
 
     pw.printHeader( "Settings" );
     for ( Setting setting : remote.getSettings() )
       setting.store( pw );
 
     for ( KeyMove keyMove : keymoves )
     {
       String className = keyMove.getClass().getName();
       int dot = className.lastIndexOf( '.' );
       className = className.substring( dot + 1 );
       pw.printHeader( className );
       keyMove.store( pw );
     }
 
     for ( Macro macro : macros )
     {
       pw.printHeader( "Macro" );
       macro.store( pw );
     }
 
     for ( SpecialProtocolFunction sp : specialFunctions )
     {
       String className = sp.getClass().getName();
       int dot = className.lastIndexOf( '.' );
       className = className.substring( dot + 1 );
       pw.printHeader( className );
       sp.store( pw );
     }
 
     for ( DeviceUpgrade device : devices )
     {
       pw.printHeader( "DeviceUpgrade" );
       device.store( pw );
     }
 
     for ( ProtocolUpgrade protocol : protocols )
     {
       pw.printHeader( "ProtocolUpgrade" );
       protocol.store( pw );
     }
 
     for ( LearnedSignal signal : learned )
     {
       pw.printHeader( "LearnedSignal" );
       signal.store( pw );
     }
 
     out.close();
   }
 
   /**
    * Export notes.
    * 
    * @param text
    *          the text
    * @return the string
    * @throws IOException
    *           Signals that an I/O exception has occurred.
    */
   private String exportNotes( String text ) throws IOException
   {
     BufferedReader br = new BufferedReader( new StringReader( text ) );
     StringBuilder buff = new StringBuilder( text.length() );
     String line = br.readLine();
     while ( line != null )
     {
       buff.append( line );
       line = br.readLine();
       if ( line != null )
       {
         buff.append( '\u00AE' );
       }
     }
     return buff.toString();
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
    * Gets the notes.
    * 
    * @return the notes
    */
   public String getNotes()
   {
     return notes;
   }
 
   /**
    * Sets the notes.
    * 
    * @param text
    *          the new notes
    */
   public void setNotes( String text )
   {
     notes = text;
   }
 
   /**
    * Gets the data.
    * 
    * @return the data
    */
   public short[] getData()
   {
     return data;
   }
 
   /**
    * Gets the saved data.
    * 
    * @return the saved data
    */
   public short[] getSavedData()
   {
     return savedData;
   }
   
   public String[] getDeviceButtonNotes()
   {
     return deviceButtonNotes;
   }
 
   /**
    * Gets the key moves.
    * 
    * @return the key moves
    */
   public List< KeyMove > getKeyMoves()
   {
     return keymoves;
   }
 
   /**
    * Gets the macros.
    * 
    * @return the macros
    */
   public List< Macro > getMacros()
   {
     return macros;
   }
 
   /**
    * Gets the device upgrades.
    * 
    * @return the device upgrades
    */
   public List< DeviceUpgrade > getDeviceUpgrades()
   {
     return devices;
   }
 
   /**
    * Gets the protocol upgrades.
    * 
    * @return the protocol upgrades
    */
   public List< ProtocolUpgrade > getProtocolUpgrades()
   {
     return protocols;
   }
 
   /**
    * Gets the learned signals.
    * 
    * @return the learned signals
    */
   public List< LearnedSignal > getLearnedSignals()
   {
     return learned;
   }
 
   /**
    * Gets the special functions.
    * 
    * @return the special functions
    */
   public List< SpecialProtocolFunction > getSpecialFunctions()
   {
     return specialFunctions;
   }
 
   /** The remote. */
   private Remote remote = null;
 
   /** The data. */
   private short[] data = null;
 
   /** The saved data. */
   private short[] savedData = null;
 
   /** The keymoves. */
   private List< KeyMove > keymoves = new ArrayList< KeyMove >();
 
   /** The upgrade key moves. */
   private List< KeyMove > upgradeKeyMoves = new ArrayList< KeyMove >();
 
   /** The macros. */
   private List< Macro > macros = new ArrayList< Macro >();
 
   /** The devices. */
   private List< DeviceUpgrade > devices = new ArrayList< DeviceUpgrade >();
 
   /** The protocols. */
   private List< ProtocolUpgrade > protocols = new ArrayList< ProtocolUpgrade >();
 
   /** The learned. */
   private List< LearnedSignal > learned = new ArrayList< LearnedSignal >();
 
   /** The special functions. */
   private List< SpecialProtocolFunction > specialFunctions = new ArrayList< SpecialProtocolFunction >();
 
   /** The notes. */
   private String notes = null;
 
   private String[] deviceButtonNotes = null;
 }
