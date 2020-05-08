 package com.hifiremote.jp1;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringReader;
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.Constructor;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collections;
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
     {
       throw new IOException( "The file is empty." );
     }
 
     if ( !"General".equals( section.getName() ) )
     {
       throw new IOException( "Doesn't start with a [General] section/" );
     }
 
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
         {
           setting.setValue( Integer.parseInt( section.getProperty( setting.getTitle() ) ) );
         }
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
           {
             specialFunctions.add( ( SpecialProtocolFunction )o );
           }
           else if ( o instanceof KeyMove )
           {
             keymoves.add( ( KeyMove )o );
           }
           else if ( sectionName.equals( "Macro" ) )
           {
             macros.add( ( Macro )o );
           }
           else if ( sectionName.equals( "TimedMacro" ) )
           {
             timedMacros.add( ( TimedMacro )o );
           }
           else if ( sectionName.equals( "FavScan" ) )
           {
             FavScan favScan = ( FavScan )o;
             favKeyDevButton = favScan.getDeviceButtonFromIndex( remote );
             if ( remote.getAdvCodeBindFormat() == AdvancedCode.BindFormat.LONG )
             {
               favScan.setDeviceButton( favKeyDevButton );
             }
             favScans.add( favScan );
           }
           else if ( sectionName.equals( "ProtocolUpgrade" ) )
           {
             protocols.add( ( ProtocolUpgrade )o );
           }
           else if ( sectionName.equals( "LearnedSignal" ) )
           {
             learned.add( ( LearnedSignal )o );
           }
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
     {
       property = pr.nextProperty();
     }
 
     int baseAddr = Integer.parseInt( property.name, 16 );
     short[] first = Hex.parseHex( property.value );
 
     if ( remote == null )
     {
       char[] sig = new char[ 8 ];
       for ( int i = 0; i < sig.length; ++i )
       {
         sig[ i ] = ( char )first[ i + 2 ];
       }
 
       String signature = new String( sig );
       String signature2 = null;
       RemoteManager rm = RemoteManager.getRemoteManager();
       List< Remote > remotes = rm.findRemoteBySignature( signature );
       if ( remotes.isEmpty() )
       {
         for ( int i = 0; i < sig.length; ++i )
         {
           sig[ i ] = ( char )first[ i ];
         }
         signature2 = new String( sig );
         remotes = rm.findRemoteBySignature( signature2 );
       }
       if ( remotes == null || remotes.isEmpty() )
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
         {
           signature = signature2;
         }
         String message = "The file you are loading is for a remote with signature \"" + signature
             + "\".\nThere are multiple remotes with that signature.  Please choose the best match from the list below:";
 
         Remote[] choices = new Remote[ remotes.size() ];
         choices = remotes.toArray( choices );
         remote = ( Remote )JOptionPane.showInputDialog( null, message, "Unknown Remote", JOptionPane.ERROR_MESSAGE,
             null, choices, choices[ 0 ] );
         if ( remote == null )
         {
           throw new IllegalArgumentException( "No matching remote selected for signature " + signature );
         }
       }
     }
     remote.load();
     SetupCode.setMax( remote.usesTwoBytePID() ? 4095 : 2047 );
 
     System.err.println( "Remote is " + remote );
 
     if ( baseAddr != remote.getBaseAddress() )
     {
       //      throw new IOException( "The base address of the remote image doesn't match the remote's baseAddress." );
       // GD:  This is probably because the file is a raw data file that always has a base address of 0, so
       // just print a message and continue/
       System.err.println( String.format( "Base address of image (%04X) differs from that in RDF " +
           "(%04X) but continuing execution.", baseAddr, remote.getBaseAddress() ) );
     }
 
     deviceButtonNotes = new String[ remote.getDeviceButtons().length ];
     data = new short[ remote.getEepromSize() ];
     System.arraycopy( first, 0, data, 0, first.length );
 
     first = null;
     while ( ( property = pr.nextProperty() ) != null )
     {
       if ( property.name.length() == 0 || property.name.startsWith( "[" ) )
       {
         break;
       }
       int offset = Integer.parseInt( property.name, 16 ) - baseAddr;
       Hex.parseHex( property.value, data, offset );
     }
 
     if ( remote.hasFavKey() )
     {
       if ( remote.getAdvCodeBindFormat() == AdvancedCode.BindFormat.NORMAL )
       {
         int buttonIndex = data[ remote.getFavKey().getDeviceButtonAddress() ] & 0x0F;
         if ( buttonIndex == 0x0F )
         {
           favKeyDevButton = DeviceButton.noButton;
         }
         else
         {
           favKeyDevButton = remote.getDeviceButtons()[ buttonIndex ];
         }
       }
       else
       {
         favKeyDevButton = DeviceButton.noButton;
       }
     }
 
     setSavedData();
 
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
       {
         continue;
       }
       int keyCode = keyMove.getKeyCode();
       String buttonName = remote.getButtonName( keyCode );
       if ( buttonName.equalsIgnoreCase( keyName ) )
       {
         return keyMove;
       }
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
       {
         return macro;
       }
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
       {
         return pu;
       }
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
       {
         continue;
       }
       int keyCode = ls.getKeyCode();
       String buttonName = remote.getButtonName( keyCode );
       if ( buttonName.equalsIgnoreCase( keyName ) )
       {
         return ls;
       }
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
     {
       property = loadBuffer( pr );
     }
 
     decodeSettings();
     decodeUpgrades();
     List< AdvancedCode > advCodes = decodeAdvancedCodes();
     if ( remote.hasFavKey() && remote.getFavKey().isSegregated() )
     {
       decodeFavScans();
     }
     if ( remote.hasTimedMacroSupport() && remote.getMacroCodingType().getType() == 1 )
     {
       decodeTimedMacros();
     }
     decodeLearnedSignals();
 
     if ( pr != null )
     {
       while ( property != null && !property.name.startsWith( "[" ) )
       {
         System.err.println( "property.name=" + property.name );
         property = pr.nextProperty();
       }
 
       if ( property != null )
       {
         IniSection section = pr.nextSection();
         section.setName( property.name.substring( 1, property.name.length() - 1 ) );
         while ( section != null )
         {
           String name = section.getName();
           if ( name.equals( "Notes" ) )
           {
             System.err.println( "Importing notes" );
             for ( Enumeration< ? > keys = section.propertyNames(); keys.hasMoreElements(); )
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
               {
                 notes = text;
               }
               else if ( flag == 1 )
               {
                 // This test is needed because of a bug in IR.exe. In a remote with segregated
                 // Fav/Scans, IR.exe allows a note to be stored, but it is put in sequence with
                 // Advanced Code notes even though the Fav/Scan is not in the Advanced Code section.
                 // This causes both IR.exe and RMIR to get the association between Advanced Codes
                 // and their notes wrong, and can lead to a Note index that is out of bounds for
                 // the Advanced Codes list. "Pure" RMIR handles Fav/Scan notes for such remotes
                 // correctly.
                 if ( index < advCodes.size() )
                 {
                   advCodes.get( index ).setNotes( text );
                 }
               }
               else if ( flag == 2 && remote.getTimedMacroAddress() != null )
               {
                 timedMacros.get( index ).setNotes( text );
               }
               else if ( flag == 3 )
               {
                 DeviceUpgrade device = devices.get( index );
                 if ( device != null )
                 {
                   device.setDescription( text );
                 }
               }
               else if ( flag == 4 )
               {
                 protocols.get( index ).setNotes( text );
               }
               else if ( flag == 5 )
               {
                 learned.get( index ).setNotes( text );
               }
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
               {
                 notes = text;
               }
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
               {
                 km.setNotes( text );
               }
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
               {
                 macro.setNotes( text );
               }
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
               {
                 device.setDescription( text );
               }
             }
           }
           else if ( name.equals( "Protocols" ) )
           {
             for ( Enumeration< ? > keys = section.propertyNames(); keys.hasMoreElements(); )
             {
               String key = ( String )keys.nextElement();
               String text = section.getProperty( key );
               StringTokenizer st = new StringTokenizer( key, "$" );
               st.nextToken(); // discard the "Protocol: " header
               int pid = Integer.parseInt( st.nextToken(), 16 );
               ProtocolUpgrade protocol = findProtocolUpgrade( pid );
               if ( protocol != null )
               {
                 protocol.setNotes( text );
               }
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
               {
                 ls.setNotes( text );
               }
             }
           }
           section = pr.nextSection();
         }
       }
     }
     migrateKeyMovesToDeviceUpgrades();
 
     // remove protocol upgrades that are used by device upgrades
     for ( Iterator< ProtocolUpgrade > it = protocols.iterator(); it.hasNext(); )
     {
       if ( it.next().isUsed() )
       {
         it.remove();
       }
     }
 
     // clean up device upgrades that couldn't be imported
     for ( Iterator< DeviceUpgrade > it = devices.iterator(); it.hasNext(); )
     {
       if ( it.next() == null )
       {
         it.remove();
       }
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
       {
         out.printf( "$%4X=%s\n", index, exportNotes( text ) );
       }
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
     {
       out.println( "$0000=" + exportNotes( notes ) );
     }
 
     // Do the advanced codes
     int i = 0x1000;
     updateSpecialFunctionSublists();
     i = exportAdvancedCodeNotes( keymoves, i, out );
     i = exportAdvancedCodeNotes( upgradeKeyMoves, i, out );
     i = exportAdvancedCodeNotes( specialFunctionKeyMoves, i, out );
     i = exportAdvancedCodeNotes( macros, i, out );
     i = exportAdvancedCodeNotes( specialFunctionMacros, i, out );
 
     // Do the Favs????
     i = 0x2000;
 
     // Do the device upgrades
     i = 0x3000;
     for ( DeviceUpgrade device : devices )
     {
       String text = device.getDescription();
       if ( text != null )
       {
         out.printf( "$%4X=%s\n", i, exportNotes( text ) );
       }
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
         {
           requiredProtocols.put( pid.get( 0 ), new ProtocolUpgrade( pid.get( 0 ), pCode, p.getName() ) );
         }
       }
     }
 
     for ( ProtocolUpgrade pu : protocols )
     {
       requiredProtocols.put( pu.getPid(), pu );
     }
 
     i = 0x4000;
     for ( ProtocolUpgrade protocol : requiredProtocols.values() )
     {
       String text = protocol.getNotes();
       if ( text != null )
       {
         out.printf( "$%4X=%s\n", i, exportNotes( text ) );
       }
       ++i;
     }
 
     // Do the learned signals
     i = 0x5000;
     for ( LearnedSignal signal : learned )
     {
       String text = signal.getNotes();
       if ( text != null )
       {
         out.printf( "$%4X=%s\n", i, exportNotes( text ) );
       }
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
       if ( deviceTypeIndex == deviceUpgrade.getDeviceType().getNumber() && setupCode == deviceUpgrade.getSetupCode() )
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
       if ( deviceButton.getDeviceTypeIndex( data ) == deviceTypeIndex && deviceButton.getSetupCode( data ) == setupCode )
       {
         return i;
       }
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
     deviceButtonNotes = new String[ remote.getDeviceButtons().length ];
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
     {
       setting.decode( data, remote );
     }
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
 
   private void decodeFavScans()
   {
     if ( !remote.hasFavKey() || !remote.getFavKey().isSegregated() )
     {
       return;
     }
     HexReader reader = new HexReader( data, remote.getFavScanAddress() );
     FavScan favScan = FavScan.read( reader, remote );
     if ( favScan != null )
     {
       favScans.add( favScan );
       favKeyDevButton = favScan.getDeviceButtonFromIndex( remote );
     }
   }
 
   private void decodeTimedMacros()
   {
     if ( remote.getMacroCodingType().getType() == 2 || !remote.hasTimedMacroSupport() )
     {
       return;
     }
     HexReader reader = new HexReader( data, remote.getTimedMacroAddress() );
     TimedMacro timedMacro = null;
     while ( ( timedMacro = TimedMacro.read( reader, remote ) ) != null )
     {
       timedMacros.add( timedMacro );
     }
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
       if ( sp.isInternal() )
       {
         continue;
       }
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
         SpecialProtocol sp = getSpecialProtocol( macro );
         if ( sp != null )
         {
           SpecialProtocolFunction sf = sp.createFunction( macro );
           if ( sf != null )
           {
             specialFunctions.add( sf );
             advCodes.add( sf.getMacro() );
           }
         }
         else
         {
           macros.add( macro );
           advCodes.add( macro );
         }
       }
       else if ( advCode instanceof FavScan )
       {
         FavScan favScan = ( FavScan )advCode;
         if ( remote.getAdvCodeBindFormat() == AdvancedCode.BindFormat.NORMAL )
         {
           favScan.setDeviceIndex( data[ remote.getFavKey().getDeviceButtonAddress() ] );
           favKeyDevButton = favScan.getDeviceButtonFromIndex( remote );
         }
         else
         {
           favKeyDevButton = favScan.getDeviceButtonFromIndex( remote );
           favScan.setDeviceButton( favKeyDevButton );
         }
         favScans.add( favScan );
         advCodes.add( favScan );
       }
       else if ( advCode instanceof TimedMacro )
       {
         TimedMacro timedMacro = ( TimedMacro )advCode;
         timedMacros.add( timedMacro );
         advCodes.add( timedMacro );
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
             advCodes.add( sf.getKeyMove() );
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
       if ( boundUpgrade != null && boundUpgrade == moveUpgrade )
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
           {
             text = remote.getButtonName( keyCode );
           }
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
             {
               state = Button.SHIFTED_STATE;
             }
             if ( ( baseCode | remote.getXShiftMask() ) == keyCode )
             {
               state = Button.XSHIFTED_STATE;
             }
           }
           else
           {
             baseCode = keyCode & ~remote.getShiftMask();
             b = remote.getButton( baseCode );
             if ( b != null )
             {
               state = Button.SHIFTED_STATE;
             }
             else
             {
               baseCode = keyCode & ~remote.getXShiftMask();
               b = remote.getButton( baseCode );
               if ( b != null )
               {
                 state = Button.XSHIFTED_STATE;
               }
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
       if ( button.getDeviceTypeIndex( data ) == upgrade.getDeviceType().getNumber()
           && button.getSetupCode( data ) == upgrade.getSetupCode() )
       {
         return i;
       }
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
       {
         return sp;
       }
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
         if ( setupCode == sp.getSetupCode() && deviceType == sp.getDeviceType().getNumber() )
         {
           return sp;
         }
       }
     }
 
     DeviceUpgrade moveUpgrade = findDeviceUpgrade( keyMove.getDeviceType(), keyMove.getSetupCode() );
     if ( moveUpgrade != null && specialUpgrades.contains( moveUpgrade ) )
     {
       return getSpecialProtocol( moveUpgrade );
     }
 
     return null;
   }
 
   private SpecialProtocol getSpecialProtocol( Macro macro )
   {
     for ( SpecialProtocol sp : remote.getSpecialProtocols() )
     {
       if ( sp.isInternal() && sp.getInternalSerial() == macro.getSequenceNumber() && macro.getDeviceIndex() != 0x0F )
       {
         return sp;
       }
     }
     return null;
   }
 
   public int getTimedMacroBytesNeeded()
   {
     int count = 0;
     for ( TimedMacro timedMacro : timedMacros )
     {
       count += timedMacro.getSize( remote );
     }
     return count;
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
     updateSpecialFunctionSublists();
     int size = getAdvancedCodesBytesNeeded( keymoves );
     upgradeKeyMoves = getUpgradeKeyMoves();
     size += getAdvancedCodesBytesNeeded( upgradeKeyMoves );
     size += getAdvancedCodesBytesNeeded( specialFunctionKeyMoves );
     size += getAdvancedCodesBytesNeeded( macros );
     size += getAdvancedCodesBytesNeeded( specialFunctionMacros );
     if ( remote.hasFavKey() && !remote.getFavKey().isSegregated() )
     {
       size += getAdvancedCodesBytesNeeded( favScans );
     }
     if ( remote.getMacroCodingType().hasTimedMacros() )
     {
       size += getAdvancedCodesBytesNeeded( timedMacros );
     }
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
     if ( remote.hasFavKey() && remote.getFavKey().isSegregated() )
     {
       updateFavScans();
     }
     if ( remote.getTimedMacroAddress() != null )
     {
       updateTimedMacros();
     }
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
       {
         continue;
       }
       for ( KeyMove keyMove : device.getKeyMoves() )
       {
         keyMove.setDeviceButtonIndex( devButtonIndex );
         rc.add( keyMove );
       }
     }
     return rc;
   }
 
   private void updateFavScans()
   {
     if ( !remote.hasFavKey() || !remote.getFavKey().isSegregated() )
     {
       return;
     }
     AddressRange range = remote.getFavScanAddress();
     int offset = range.getStart();
     if ( favScans.size() == 0 )
     {
       data[ offset ] = 0; // set length to 0
       return;
     }
     // Segregated FavScan section allows only one entry.
     FavScan favScan = favScans.get( 0 );
     int buttonIndex = favKeyDevButton == DeviceButton.noButton ? 0 : favKeyDevButton.getButtonIndex();
     data[ remote.getFavKey().getDeviceButtonAddress() ] = ( short )buttonIndex;
     favScan.store( data, offset, remote );
   }
 
   private void updateTimedMacros()
   {
     AddressRange range = remote.getTimedMacroAddress();
     if ( range == null )
     {
       return;
     }
     int offset = range.getStart();
     for ( TimedMacro timedMacro : timedMacros )
     {
       offset = timedMacro.store( data, offset, remote );
     }
     data[ offset++ ] = remote.getSectionTerminator();
   }
 
   /**
    * Update advanced codes.
    * 
    * @return the int
    */
   private void updateAdvancedCodes()
   {
     AddressRange range = remote.getAdvancedCodeAddress();
     if ( range == null )
     {
       return;
     }
     int offset = range.getStart();
     updateSpecialFunctionSublists();
     offset = updateKeyMoves( keymoves, offset );
     upgradeKeyMoves = getUpgradeKeyMoves();
     offset = updateKeyMoves( upgradeKeyMoves, offset );
     offset = updateKeyMoves( specialFunctionKeyMoves, offset );
 
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
     for ( Macro macro : specialFunctionMacros )
     {
       offset = macro.store( data, offset, remote );
     }
     if ( remote.hasFavKey() && !remote.getFavKey().isSegregated() )
     {
       for ( FavScan favScan : favScans )
       {
         if ( remote.getAdvCodeBindFormat() == AdvancedCode.BindFormat.NORMAL )
         {
           // When the button is noButton, this gives a button index of 0xFF as required.
           int buttonIndex = favKeyDevButton.getButtonIndex() & 0xFF;
           data[ remote.getFavKey().getDeviceButtonAddress() ] = ( short )buttonIndex;
         }
         offset = favScan.store( data, offset, remote );
       }
     }
     if ( remote.getMacroCodingType().hasTimedMacros() )
     {
       for ( TimedMacro timedMacro : timedMacros )
       {
         offset = timedMacro.store( data, offset, remote );
       }
       int timedMacroCountAddress = remote.getMacroCodingType().getTimedMacroCountAddress();
       if ( timedMacroCountAddress > 0 )
       {
         data[ timedMacroCountAddress ] = ( short )timedMacros.size();
       }
     }
     data[ offset++ ] = remote.getSectionTerminator();
 
     // Next step commented out as it overwrites the date indicator generated by File/New
     // and is not necessary. IR.exe doesn't fill with section terminators.
 
     // // Fill the rest of the advance code section with the section terminator
     // while ( offset <= range.getEnd() )
     // {
     // data[ offset++ ] = remote.getSectionTerminator();
     // }
 
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
   public void updateCheckSums()
   {
     CheckSum[] sums = remote.getCheckSums();
     for ( int i = 0; i < sums.length; ++i )
     {
       sums[ i ].setCheckSum( data );
     }
   }
 
   /**
    * Update settings.
    */
   private void updateSettings()
   {
     Setting[] settings = remote.getSettings();
     for ( Setting setting : settings )
     {
       setting.store( data, remote );
     }
   }
 
   private void updateFixedData()
   {
     FixedData[] fixedData = remote.getFixedData();
     if ( fixedData == null )
     {
       return;
     }
     for ( FixedData fixed : fixedData )
     {
       fixed.store( data );
     }
   }
 
   private void updateAutoSet()
   {
     FixedData[] autoSet = remote.getAutoSet();
     if ( autoSet == null )
     {
       return;
     }
     for ( FixedData auto : autoSet )
     {
       auto.store( data );
     }
 
     int rdfVersionAddress = remote.getRdfVersionAddress();
     if ( rdfVersionAddress > 0 )
     {
       data[ rdfVersionAddress ] = RemoteMaster.MAX_RDF_SYNC;
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
       {
         return pu;
       }
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
       if ( bounds[ i ] != 0 && offset < bounds[ i ] && limit > bounds[ i ] )
       {
         limit = bounds[ i ];
       }
     }
     return limit;
   }
 
   /**
    * Decode upgrades.
    */
   private void decodeUpgrades()
   {
     AddressRange addr = remote.getUpgradeAddress();
     // Also get address range for device specific upgrades, which will be null
     // if these are not used by the remote.
     AddressRange devAddr = remote.getDeviceUpgradeAddress();
 
     Processor processor = remote.getProcessor();
     // get the offsets to the device and protocol tables
     int deviceTableOffset = processor.getInt( data, addr.getStart() ) - remote.getBaseAddress(); // get offset of device
     // table
     int protocolTableOffset = processor.getInt( data, addr.getStart() + 2 ) - remote.getBaseAddress(); // get offset of
     // protocol table
     int devDependentTableOffset = devAddr == null ? 0 : processor.getInt( data, devAddr.getStart() )
         + devAddr.getStart();
     // get offset of device dependent table, filled from top downwards; offset is to start of first entry
 
     // build an array containing the ends of all the possible ranges
 
     int[] bounds = new int[ 8 ];
     bounds[ 0 ] = 0; // leave space for the next entry in the table
     bounds[ 1 ] = 0; // leave space for the 1st protocol code
     bounds[ 2 ] = deviceTableOffset;
     bounds[ 3 ] = protocolTableOffset;
     // GD: Why the -1's in the following bounds? Presumably to allow for the section
     // terminator, but getEnd() returns the offset of the last byte of the section, not
     // of the byte following it, which is already the address of the section terminator.
     bounds[ 4 ] = addr.getEnd() - 1;
     bounds[ 5 ] = remote.getAdvancedCodeAddress().getEnd() - 1;
     if ( remote.getLearnedAddress() != null )
     {
       bounds[ 6 ] = remote.getLearnedAddress().getEnd() - 1;
     }
     else
     {
       bounds[ 6 ] = 0;
     }
     if ( devAddr != null )
     {
       bounds[ 7 ] = devAddr.getEnd() - 1;
     }
     else
     {
       bounds[ 7 ] = 0;
     }
 
     // parse the protocol tables
     int offset = protocolTableOffset;
     int count = processor.getInt( data, offset ); // get number of entries in upgrade table
     offset += 2; // skip to first entry
 
     for ( int i = 0; i < count; ++i )
     {
       int pid = processor.getInt( data, offset );
       int codeOffset = processor.getInt( data, offset + 2 * count ) - remote.getBaseAddress();
       if ( i == 0 )
       {
         bounds[ 1 ] = codeOffset; // save the offset of the first protocol code
       }
       if ( i == count - 1 )
       {
         bounds[ 0 ] = 0;
       }
       else
       {
         bounds[ 0 ] = processor.getInt( data, offset + 2 * ( count + 1 ) ) - remote.getBaseAddress();
       }
 
       int limit = getLimit( codeOffset, bounds );
       Hex code = Hex.subHex( data, codeOffset, limit - codeOffset );
       protocols.add( new ProtocolUpgrade( pid, code, null ) );
 
       offset += 2; // for the next upgrade
     }
 
     // now parse the devices in the device-independent upgrade section
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
       DeviceType devType = remote.getDeviceTypeByIndex( fullCode >> 12 & 0xF );
       int codeOffset = offset + 2 * count; // compute offset to offset of upgrade code
       codeOffset = processor.getInt( data, codeOffset ) - remote.getBaseAddress(); // get offset of upgrade code
       int pid = data[ codeOffset ];
       if ( remote.usesTwoBytePID() )
       {
         pid = processor.getInt( data, codeOffset );
       }
       else
       {
         if ( ( fullCode & 0x800 ) == 0x800 )
         {
           pid += 0x100;
         }
       }
 
       if ( i == count - 1 )
       {
         bounds[ 0 ] = 0;
       }
       else
       {
         bounds[ 0 ] = processor.getInt( data, offset + 2 * ( count + 1 ) ) - remote.getBaseAddress(); // next device
       }
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
//      pidHex[ 0 ] = ( short )( pid > 0xFF ? 1 : 0 );
      pidHex[ 0 ] = ( short )( pid >> 8 ); // pids can now be > 0x1FF
       pidHex[ 1 ] = ( short )( pid & 0xFF );
 
       DeviceUpgrade upgrade = new DeviceUpgrade();
       try
       {
         upgrade.importRawUpgrade( deviceHex, remote, alias, new Hex( pidHex ), protocolCode );
         upgrade.setSetupCode( setupCode );
       }
       catch ( java.text.ParseException pe )
       {
         pe.printStackTrace( System.err );
         upgrade = null;
       }
 
       devices.add( upgrade );
     }
 
     if ( devAddr == null )
     {
       return;
     }
 
     // now parse the devices and protocols in the device-dependent upgrade section
     offset = devDependentTableOffset;
     while ( data[ offset ] != remote.getSectionTerminator() )
     {
       // In this section the full code is stored big-endian, regardless of the processor!
       DeviceButton deviceButton = remote.getDeviceButtons()[ data[ offset + 2 ] ];
       int fullCode = Hex.get( data, offset + 3 );
       int setupCode = fullCode & 0xFFF;
       if ( !remote.usesTwoBytePID() )
       {
         setupCode &= 0x7FF;
       }
       int deviceTypeIndex = fullCode >> 12 & 0xF;
       // Check if this upgrade is also in the device independent section.
       DeviceUpgrade upg = findDeviceUpgrade( deviceTypeIndex, setupCode );
       if ( upg != null )
       {
         upg.setButtonRestriction( deviceButton );
       }
       else
       {
         DeviceType devType = remote.getDeviceTypeByIndex( deviceTypeIndex );
         int codeOffset = offset + 5;
         int pid = data[ codeOffset ];
         if ( remote.usesTwoBytePID() )
         {
           pid = processor.getInt( data, codeOffset );
         }
         else
         {
           if ( ( fullCode & 0x800 ) == 0x800 )
           {
             pid += 0x100;
           }
         }
         // Note that the protocol entry can start *after* the end of the entire upgrade entry,
         // if the upgrade uses the in-line protocol of another upgrade.
         bounds[ 0 ] = offset + data[ offset ]; // start of following upgrade entry
         bounds[ 1 ] = offset + data[ offset + 1 ]; // start of protocol entry (if present)
         int limit = getLimit( offset, bounds );
         Hex deviceHex = Hex.subHex( data, codeOffset, limit - codeOffset );
         ProtocolUpgrade pu = getProtocol( pid );
         Hex protocolCode = null;
         if ( pu != null )
         {
           pu.setUsed( true );
           protocolCode = pu.getCode();
         }
         else
         {
           // Get the in-line protocol, whether it is in this upgrade or another.
           codeOffset = bounds[ 1 ];
           while ( bounds[ 0 ] < codeOffset )
           {
             bounds[ 0 ] += data[ bounds[ 0 ] ];
           }
           // bounds[ 0 ] is now start of the upgrade entry following the protocol.
 
           limit = getLimit( codeOffset, bounds );
           protocolCode = Hex.subHex( data, codeOffset, limit - codeOffset );
           pu = new ProtocolUpgrade( pid, protocolCode, null );
           pu.setUsed( true );
           protocols.add( pu );
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
         pidHex[ 0 ] = ( short )( pid > 0xFF ? 1 : 0 );
         pidHex[ 1 ] = ( short )( pid & 0xFF );
 
         DeviceUpgrade upgrade = new DeviceUpgrade();
         try
         {
           upgrade.importRawUpgrade( deviceHex, remote, alias, new Hex( pidHex ), protocolCode );
           upgrade.setSetupCode( setupCode );
           upgrade.setButtonIndependent( false );
           upgrade.setButtonRestriction( deviceButton );
         }
         catch ( java.text.ParseException pe )
         {
           pe.printStackTrace( System.err );
           upgrade = null;
         }
 
         devices.add( upgrade );
       }
 
       offset += data[ offset ];
 
       if ( offset > devAddr.getEnd() )
       {
         String message = "Invalid data in device-specific upgrade.  The data appears to overrun the section.";
         JOptionPane.showMessageDialog( null, message, "Upgrade Error", JOptionPane.ERROR_MESSAGE );
         break;
       }
     }
   }
 
   public HashMap< Integer, ProtocolUpgrade > getRequiredProtocolUpgrades()
   {
     // Build a list of the required protocol upgrades
     LinkedHashMap< Integer, ProtocolUpgrade > requiredProtocols = new LinkedHashMap< Integer, ProtocolUpgrade >();
     for ( DeviceUpgrade dev : devices )
     {
       if ( dev.getButtonIndependent() && dev.needsProtocolCode() )
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
     {
       requiredProtocols.put( pu.getPid(), pu );
     }
 
     return requiredProtocols;
   }
 
   /**
    * Gets the upgrade code bytes used.
    * 
    * @return the upgrade code bytes used
    */
   public int getUpgradeCodeBytesNeeded()
   {
 
     List< DeviceUpgrade > devIndependent = new ArrayList< DeviceUpgrade >();
 
     for ( DeviceUpgrade dev : devices )
     {
       if ( dev.getButtonIndependent() )
       {
         devIndependent.add( dev );
       }
     }
 
     int size = 4; // Allow for the table pointers
 
     int devCount = devIndependent.size();
 
     HashMap< Integer, ProtocolUpgrade > requiredProtocols = getRequiredProtocolUpgrades();
     // Calculate the size of the upgrade table
 
     int prCount = requiredProtocols.size();
 
     // Handle the special case where there are no upgrades installed
     if ( devCount == 0 && prCount == 0 )
     {
       return size;
     }
 
     // the device upgrades
     for ( DeviceUpgrade upgrade : devIndependent )
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
 
     if ( remote.getProcessor().getName().equals( "740" ) )
     {
       // Remotes with the 740 processor store an additional address at the end of each
       // of the device and protocol tables.
       size += 4;
     }
 
     return size;
   }
 
   public int getDevUpgradeCodeBytesNeeded()
   {
     List< DeviceUpgrade > devDependent = new ArrayList< DeviceUpgrade >();
     for ( DeviceUpgrade dev : devices )
     {
       if ( dev.getButtonRestriction() != DeviceButton.noButton )
       {
         devDependent.add( dev );
       }
     }
 
     Collections.sort( devDependent, new DependentUpgradeComparator() );
 
     int lastProtID = -1;
     int lastProtAddr = -1;
     int offset = 0x10000; // value not relevant, it is just to prevent negative offsets
 
     for ( int i = 0; i < devDependent.size(); i++ )
     {
       DeviceUpgrade upg = devDependent.get( i );
       int upgLength = upg.getUpgradeLength();
 
       if ( upg.needsProtocolCode() )
       {
         int protID = upg.getProtocol().getID().get( 0 );
         if ( protID != lastProtID || lastProtAddr - offset + upgLength + 5 > 0xFF )
         {
           // In-line protocol required
           Hex hex = upg.getCode();
           offset -= hex.length();
           lastProtAddr = offset;
         }
       }
       // Device upgrade has an additional 5-byte header
       offset -= upgLength + 5;
 
     }
     // Allow for storage of start address and section terminator.
     return 0x10000 - offset + 3;
   }
 
   /**
    * Update upgrades.
    * 
    * @return the int
    */
   private void updateUpgrades()
   {
     // Split the device upgrades into separate device independent and device
     // dependent lists. An upgrade can occur in both lists.
     List< DeviceUpgrade > devIndependent = new ArrayList< DeviceUpgrade >();
     List< DeviceUpgrade > devDependent = new ArrayList< DeviceUpgrade >();
     for ( DeviceUpgrade dev : devices )
     {
       if ( dev.getButtonIndependent() )
       {
         devIndependent.add( dev );
       }
       if ( dev.getButtonRestriction() != DeviceButton.noButton )
       {
         devDependent.add( dev );
       }
     }
 
     // First update device independent upgrades
     AddressRange addr = remote.getUpgradeAddress();
     // Also get address range for device specific upgrades, which will be null
     // if these are not used by the remote.
     AddressRange devAddr = remote.getDeviceUpgradeAddress();
     if ( addr == null && devAddr == null )
     {
       return;
     }
 
     int offset = addr.getStart() + 4; // skip over the table pointers
 
     int devCount = devIndependent.size();
 
     // Build a list of the required protocol upgrades
     LinkedHashMap< Integer, ProtocolUpgrade > requiredProtocols = new LinkedHashMap< Integer, ProtocolUpgrade >();
     for ( DeviceUpgrade dev : devIndependent )
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
                 + " is different from the code already used by another device upgrade, and may not work as intended.";
             JOptionPane.showMessageDialog( null, message, "Protocol Code Mismatch", JOptionPane.ERROR_MESSAGE );
           }
         }
       }
     }
 
     // The installed protocols that aren't used by any device upgrade.
     // These also go in the device independent section.
     for ( ProtocolUpgrade pu : protocols )
     {
       requiredProtocols.put( pu.getPid(), pu );
     }
 
     // Calculate the size of the upgrade table
 
     int prCount = requiredProtocols.size();
 
     Processor processor = remote.getProcessor();
 
     // store the device upgrades of the device independent section
     int[] devOffsets = new int[ devCount ];
     int i = 0;
     for ( DeviceUpgrade dev : devIndependent )
     {
       devOffsets[ i++ ] = offset;
       Hex hex = dev.getUpgradeHex();
       Hex.put( hex, data, offset );
       offset += hex.length();
     }
 
     int devUpgradesEnd = offset + remote.getBaseAddress();
 
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
 
     int protUpgradesEnd = offset + remote.getBaseAddress();
 
     // set the pointer to the device table.
     processor.putInt( offset + remote.getBaseAddress(), data, addr.getStart() );
 
     // create the device table
     processor.putInt( devCount, data, offset );
     offset += 2;
     // store the setup codes
     for ( DeviceUpgrade dev : devIndependent )
     {
       processor.putInt( Hex.get( dev.getHexSetupCode(), 0 ), data, offset );
       offset += 2;
     }
     // store the offsets
     for ( int devOffset : devOffsets )
     {
       processor.putInt( devOffset + remote.getBaseAddress(), data, offset );
       offset += 2;
     }
 
     if ( processor.getName().equals( "740" ) )
     {
       processor.putInt( devUpgradesEnd, data, offset );
       offset += 2;
     }
 
     if ( devCount == 0 && prCount == 0 )
     {
       // When no devices or protocols, the tables are the same so we reset
       // the offset to the start of the device table.
       offset = protUpgradesEnd - remote.getBaseAddress();
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
 
     if ( processor.getName().equals( "740" ) )
     {
       processor.putInt( protUpgradesEnd, data, offset );
       offset += 2;
       processor.putInt( offset - addr.getStart() + 2, data, addr.getStart() - 2 );
     }
 
     if ( devAddr == null )
     {
       return;
     }
 
     // Now update the device dependent section, with updates sorted for storage efficiency.
     // Note that this section is filled from the top downwards.
     Collections.sort( devDependent, new DependentUpgradeComparator() );
 
     int lastProtID = -1;
     int lastProtAddr = -1;
     offset = devAddr.getEnd();
     int lastDevAddr = offset;
     data[ offset ] = remote.getSectionTerminator();
 
     for ( i = 0; i < devDependent.size(); i++ )
     {
       DeviceUpgrade upg = devDependent.get( i );
       int upgLength = upg.getUpgradeLength();
       int protOffset = 0; // value used when protocol upgrade not required
       int buttonIndex = upg.getButtonRestriction().getButtonIndex();
 
       if ( upg.needsProtocolCode() )
       {
         int protID = upg.getProtocol().getID().get( 0 );
         if ( protID == lastProtID && lastProtAddr - offset + upgLength + 5 <= 0xFF )
         {
           // Upgrade can use a protocol already placed in this section
           protOffset = lastProtAddr - offset + upgLength + 5;
         }
         else
         {
           // Store the protocol
           Hex hex = upg.getCode();
           offset -= hex.length();
           Hex.put( hex, data, offset );
           lastProtID = protID;
           lastProtAddr = offset;
           protOffset = upgLength + 5;
         }
       }
       // Store the device upgrade
       Hex hex = upg.getUpgradeHex();
       offset -= upgLength + 5;
       Hex.put( hex, data, offset + 5 );
       Hex.put( upg.getHexSetupCode(), data, offset + 3 );
       data[ offset + 2 ] = ( short )buttonIndex;
       data[ offset + 1 ] = ( short )protOffset;
       data[ offset ] = ( short )( lastDevAddr - offset );
       lastDevAddr = offset;
 
     }
     offset = devAddr.getStart();
     processor.putInt( lastDevAddr - offset, data, offset );
   }
 
   /**
    * Decode learned signals.
    */
   public void decodeLearnedSignals()
   {
     AddressRange addr = remote.getLearnedAddress();
     if ( addr == null )
     {
       return;
     }
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
     {
       return 0;
     }
 
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
     {
       return;
     }
 
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
     {
       setting.store( pw );
     }
 
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
       if ( sp.isInternal() )
       {
         pw.print( "Internal", "true" );
         sp.getMacro().store( pw );
       }
       else
       {
         sp.getKeyMove().store( pw );
       }
     }
 
     for ( TimedMacro tm : timedMacros )
     {
       String className = tm.getClass().getName();
       int dot = className.lastIndexOf( '.' );
       className = className.substring( dot + 1 );
       pw.printHeader( className );
       tm.store( pw );
     }
 
     for ( FavScan fs : favScans )
     {
       String className = fs.getClass().getName();
       int dot = className.lastIndexOf( '.' );
       className = className.substring( dot + 1 );
       pw.printHeader( className );
       fs.store( pw, this );
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
 
   public void setSavedData()
   {
     savedData = new short[ data.length ];
     System.arraycopy( data, 0, savedData, 0, data.length );
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
 
   public List< FavScan > getFavScans()
   {
     return favScans;
   }
 
   public List< TimedMacro > getTimedMacros()
   {
     return timedMacros;
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
 
   public void setRemote( Remote remote )
   {
     this.remote = remote;
   }
 
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
 
   private List< TimedMacro > timedMacros = new ArrayList< TimedMacro >();
 
   private List< FavScan > favScans = new ArrayList< FavScan >();
 
   /** The devices. */
   private List< DeviceUpgrade > devices = new ArrayList< DeviceUpgrade >();
 
   /** The protocols. */
   private List< ProtocolUpgrade > protocols = new ArrayList< ProtocolUpgrade >();
 
   /** The learned. */
   private List< LearnedSignal > learned = new ArrayList< LearnedSignal >();
 
   /** The special functions. */
   private List< SpecialProtocolFunction > specialFunctions = new ArrayList< SpecialProtocolFunction >();
   private List< KeyMove > specialFunctionKeyMoves = new ArrayList< KeyMove >();
   private List< Macro > specialFunctionMacros = new ArrayList< Macro >();
 
   private void updateSpecialFunctionSublists()
   {
     specialFunctionKeyMoves.clear();
     specialFunctionMacros.clear();
     for ( SpecialProtocolFunction sp : specialFunctions )
     {
       if ( sp.isInternal() )
       {
         specialFunctionMacros.add( sp.getMacro() );
       }
       else
       {
         specialFunctionKeyMoves.add( sp.getKeyMove() );
       }
     }
   }
 
   public DeviceButton getFavKeyDevButton()
   {
     return favKeyDevButton;
   }
 
   public void setFavKeyDevButton( DeviceButton devButton )
   {
     this.favKeyDevButton = devButton;
     if ( favScans.size() > 0 )
     {
       int size = favScans.size();
       favScans.get( size - 1 ).setDeviceButton( devButton );
     }
     if ( remote.getAdvCodeBindFormat() == AdvancedCode.BindFormat.NORMAL )
     {
       // When the button is noButton, this gives a button index of 0xFF as required.
       int buttonIndex = favKeyDevButton.getButtonIndex() & 0xFF;
       data[ remote.getFavKey().getDeviceButtonAddress() ] = ( short )buttonIndex;
     }
     else
     {
       updateAdvancedCodes();
     }
   }
 
   public void initializeSetup()
   {
     // Fill buffer with 0xFF
     Arrays.fill( data, ( short )0xFF );
 
     // Write signature to buffer
     int start = remote.getInterfaceType().equals( "JP1" ) ? 2 : 0;
     byte[] sigBytes = new byte[ 0 ];
     try
     {
       sigBytes = remote.getSignature().getBytes( "UTF-8" );
     }
     catch ( UnsupportedEncodingException e )
     {
       e.printStackTrace();
     }
     for ( int i = 0; i < sigBytes.length; i++ )
     {
       data[ start + i ] = ( short )( sigBytes[ i ] & 0xFF );
     }
 
     // Unless remote uses soft devices, set default device types and setup codes in buffer
     if ( remote.getSoftDevices() == null || !remote.getSoftDevices().inUse() )
     {
       DeviceButton[] devBtns = remote.getDeviceButtons();
       java.util.List< DeviceType > devTypeList = remote.getDeviceTypeList();
       int j = 0;
       for ( int i = 0; i < devBtns.length; i++ )
       {
         DeviceType dt = devTypeList.get( j );
         DeviceButton db = devBtns[ i ];
         db.zeroDeviceSlot( data );
         db.setDeviceTypeIndex( ( short )dt.getNumber(), data );
         db.setDeviceGroup( ( short )dt.getGroup(), data );
         db.setSetupCode( ( short )db.getDefaultSetupCode(), data );
         if ( j < devTypeList.size() - 1 )
         {
           j++ ;
         }
       }
     }
     else if ( remote.getSoftDevices().usesFilledSlotCount() )
     {
       data[ remote.getSoftDevices().getCountAddress() ] = 0;
     }
 
     // Zero the settings bytes for non-inverted settings
     for ( Setting setting : remote.getSettings() )
     {
       if ( !setting.isInverted() )
       {
         data[ setting.getByteAddress() ] = 0;
       }
     }
 
     // If remote has segregated Fav key, initialize Fav section
     if ( remote.hasFavKey() && remote.getFavKey().isSegregated() )
     {
       int offset = remote.getFavScanAddress().getStart();
       data[ offset++ ] = 0;
       data[ offset++ ] = 0;
     }
   }
 
   public void setDateIndicator()
   {
     // Set date in yy-mm-dd format, using BCD encoding, at end of Advanced
     // Code section as indicator that file was initially produced by New, rather
     // than by downloading from a remote.
     Calendar now = Calendar.getInstance();
     int year = now.get( Calendar.YEAR ) % 100;
     int month = now.get( Calendar.MONTH ) - Calendar.JANUARY + 1;
     int date = now.get( Calendar.DATE );
     if ( remote.getAdvancedCodeAddress() == null )
     {
       return;
     }
     int offset = remote.getAdvancedCodeAddress().getEnd() - 2;
     data[ offset++ ] = ( short )( year / 10 << 4 | year % 10 );
     data[ offset++ ] = ( short )( month / 10 << 4 | month % 10 );
     data[ offset++ ] = ( short )( date / 10 << 4 | date % 10 );
     updateCheckSums();
   }
   
   public static void resetDialogs()
   {   
     KeyMoveDialog.reset();
     MacroDialog.reset();
     TimedMacroDialog.reset();
     SpecialFunctionDialog.reset();
     FavScanDialog.reset();
     LearnedSignalDialog.reset();
   }
 
   /** The notes. */
   private String notes = null;
 
   private String[] deviceButtonNotes = null;
 
   private DeviceButton favKeyDevButton = null;
 
 }
