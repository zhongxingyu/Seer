 package com.hifiremote.jp1;
 
 import java.awt.Color;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringReader;
 import java.io.StringWriter;
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
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.Properties;
 import java.util.StringTokenizer;
 
 import javax.swing.JOptionPane;
 
import com.hifiremote.jp1.AdvancedCode.BindFormat;

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
   public RemoteConfiguration( File file, RemoteMaster rm ) throws IOException
   {
     owner = rm;
     BufferedReader in = new BufferedReader( new FileReader( file ) );
     PropertyReader pr = new PropertyReader( in );
     if ( file.getName().toLowerCase().endsWith( ".rmir" ) )
     {
       parse( pr );
     }
     else
     {
       importIR( pr, true );
     }
     in.close();
     updateImage();
   }
   
   public RemoteConfiguration( String str, RemoteMaster rm, Remote remote ) throws IOException
   {
     owner = rm;
     this.remote = remote;
     BufferedReader in = new BufferedReader( new StringReader( str ) );
     PropertyReader pr = new PropertyReader( in );
     importIR( pr, false );
     in.close();
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
     ProtocolManager.getProtocolManager().reset();
 
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
           else if ( sectionName.equals( "ManualProtocol" ) )
           {
             ManualProtocol mp = ( ManualProtocol )o;
             ProtocolManager.getProtocolManager().add( mp );
             // Each manual protocol entry immediately follows a corresponding protocol 
             // upgrade entry by the way these entries are generated in save(File), so
             // attach it to the most recently added protocol upgrade
             protocols.get( protocols.size() - 1 ).setProtocol( mp );
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
     
     List< Integer > offsets = new ArrayList< Integer >();    
     List< short[] > values = new ArrayList< short[] >();
     
     while ( property != null )
     {
       if ( property.name.length() == 0 || property.name.startsWith( "[" ) )
       {
         break;
       }
       offsets.add( Integer.parseInt( property.name, 16 ) - baseAddr );
       values.add( Hex.parseHex( property.value ) );
       property = pr.nextProperty();
     }
     
     int eepromSize = 0;
     for ( int i = 0; i < offsets.size(); i++ )
     {
       eepromSize = Math.max( eepromSize, offsets.get( i ) + values.get( i ).length );
     }
     
     data = new short[ eepromSize ];
 
     for ( int i = 0; i < offsets.size(); i++ )
     {
       System.arraycopy( values.get( i ), 0, data, offsets.get( i ), values.get( i ).length );
     }
 
     if ( remote == null )
     {      
 //      See comment in Hex.getRemoteSignature( short[] ) for why the lines below were not safe      
 //      String signature = new String( sig );
 //      String sig = io.getRemoteSignature();
       String signature = Hex.getRemoteSignature( data );
       String signature2 = null;
       RemoteManager rm = RemoteManager.getRemoteManager();
       List< Remote > remotes = null;
       for ( int i = 0; i < 5; i++ )
       {
         signature2 = signature.substring( 0, signature.length() - i );
         remotes = rm.findRemoteBySignature( signature2 );
         if ( !remotes.isEmpty() ) break;
       }
       signature = signature2;
       remote = filterRemotes( remotes, signature, eepromSize, data, true );
       if ( remote == null )
       {
         throw new IllegalArgumentException( "No matching remote selected for signature " + signature );
       }
     }
     remote.load();
     highlight = new Color[ eepromSize + 8 * remote.getSettingAddresses().size() ];
     for ( int i = 0; i < highlight.length; i++ )
     {
       highlight[ i ] = Color.WHITE;
     }
     SetupCode.setMax( remote.usesTwoBytePID() ? 4095 : 2047 );
 
     System.err.println( "Remote is " + remote );
 
     if ( baseAddr != remote.getBaseAddress() )
     {
       // throw new IOException( "The base address of the remote image doesn't match the remote's baseAddress." );
       // GD: This is probably because the file is a raw data file that always has a base address of 0, so
       // just print a message and continue/
       System.err.println( String.format( "Base address of image (%04X) differs from that in RDF "
           + "(%04X) but continuing execution.", baseAddr, remote.getBaseAddress() ) );
     }
 
     deviceButtonNotes = new String[ remote.getDeviceButtons().length ];
 
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
   
   public static Remote filterRemotes( List< Remote > remotes, String signature, int eepromSize, 
       short[] data, boolean allowMismatch )
   {
     Remote remote = null;
     
     // Filter on matching eeprom size
     for ( Iterator< Remote > it = remotes.iterator(); it.hasNext(); )
     {
       if ( it.next().getEepromSize() != eepromSize )
       {
           it.remove();
       }
     }        
     if ( remotes == null || remotes.isEmpty() )
     {
       String message = "No remote found with signature starting " + signature
         + " and EEPROM size " + ( eepromSize >> 10 ) + "k";
       JOptionPane.showMessageDialog( null, message, "Unknown remote", JOptionPane.ERROR_MESSAGE );
       return null;
     }
     else if ( remotes.size() == 1 && allowMismatch )
     {
       remote = remotes.get( 0 );
     }
     else
     {
       // Filter on matching fixed data
       Remote[] choices = new Remote[ 0 ];
       choices = FixedData.filter( remotes, data );
       if ( choices.length == 0 )
       {
         if ( allowMismatch )
         {
           // Either not filtered on, or none of the remotes match on, fixed data so offer whole list
           choices = remotes.toArray( choices );
         }
         else
         {
           return null;
         }
       }
       if ( choices.length == 1 )
       {
         remote = choices[ 0 ];
       }
       else
       {
         String message = "The file you are loading is for a remote with signature \"" + signature
         + "\".\nThere are multiple remotes with that signature.  Please choose the best match from the list below:";
 
         remote = ( Remote )JOptionPane.showInputDialog( null, message, "Unknown Remote", JOptionPane.ERROR_MESSAGE,
             null, choices, choices[ 0 ] );
       }
     }
     return remote;
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
   private void importIR( PropertyReader pr, boolean deleteUsedProts ) throws IOException
   {
     Property property = null;
     if ( pr != null )
     {
       property = loadBuffer( pr );
     }
 
     if ( deleteUsedProts )
     {
       ProtocolManager.getProtocolManager().reset();
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
         if ( section != null )
         {
           section.setName( property.name.substring( 1, property.name.length() - 1 ) );
         }
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
                 // Device notes are very complicated.  Play safe and test.
                 if ( index < devices.size() )
                 {
                   DeviceUpgrade device = devices.get( index );
                   if ( device != null )
                   {
                     device.setDescription( text );
                   }
                 }
               }
               else if ( flag == 4 )
               {
                 // Protocol notes are very complicated.  Play safe and test.
                 if ( index < protocols.size() )
                 {
                   protocols.get( index ).setNotes( text );
                 }
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
     convertKeyMoves();
     migrateKeyMovesToDeviceUpgrades();
 
     if ( deleteUsedProts )
     {
       // remove protocol upgrades that are used by device upgrades
       for ( Iterator< ProtocolUpgrade > it = protocols.iterator(); it.hasNext(); )
       {
         if ( it.next().isUsed() )
         {
           it.remove();
         }
       }
 
       // Add the protocol upgrades still remaining to ProtocolManager as manual protocols
       for ( ProtocolUpgrade pu : protocols )
       {
         pu.setManualProtocol( remote );
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
       if ( text != null && !text.trim().isEmpty() )
       {
         out.printf( "$%4X=%s\n", index, exportNotes( text ) );
       }
       ++index;
     }
     return index;
   }
   
   public void exportIR( File file ) throws IOException
   {
     PrintWriter pw = new PrintWriter( new BufferedWriter( new FileWriter( file ) ) );
     exportIR( pw );
   }
   
   public String exportIR() throws IOException
   {
     StringWriter sw = new StringWriter();
     PrintWriter pw = new PrintWriter( sw );
     exportIR( pw );
     return sw.toString();
   }
 
   /**
    * Export ir.
    * 
    * @param file
    *          the file
    * @throws IOException
    *           Signals that an I/O exception has occurred.
    */
   public void exportIR( PrintWriter out ) throws IOException
   {
     updateImage();
 
     Hex.print( out, data, remote.getBaseAddress() );
 
     out.println();
     out.println( "[Notes]" );
     // start with the overall notes
     if ( notes != null && !notes.trim().isEmpty() )
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
     if ( remote.hasFavKey() && !remote.getFavKey().isSegregated() )
     {
       i = exportAdvancedCodeNotes( favScans, i, out );
     }
     if ( remote.getMacroCodingType().hasTimedMacros() )
     {
       i = exportAdvancedCodeNotes( timedMacros, i, out );
     }
 
     // Do the timed macros when they are in a separate section
     i = 0x2000;
     if ( remote.getTimedMacroAddress() != null )
     {
       i = exportAdvancedCodeNotes( timedMacros, i, out );
     }
 
     // Do the device upgrades
     i = 0x3000;
     // Split the device upgrades into separate button-independent and button-
     // dependent-only lists. An upgrade can occur in only one list. Sort the
     // second list into the order in which they will be read by IR.exe.
     List< DeviceUpgrade > devIndependent = new ArrayList< DeviceUpgrade >();
     List< DeviceUpgrade > devDependent = new ArrayList< DeviceUpgrade >();
     for ( DeviceUpgrade dev : devices )
     {
       if ( dev.getButtonIndependent() )
       {
         devIndependent.add( dev );
       }
       else if ( dev.getButtonRestriction() != DeviceButton.noButton )
       {
         devDependent.add( dev );
       }
     }
     // Sort button-dependent ones into order in which they are stored in buffer.
     Collections.sort( devDependent, new DependentUpgradeComparator() );
 
     // First do the upgrades in the button-independent area
     for ( DeviceUpgrade device : devIndependent )
     {
       String text = device.getDescription();
       if ( text != null && !text.trim().isEmpty() )
       {
         out.printf( "$%4X=%s\n", i, exportNotes( text ) );
       }
       ++i;
     }
 
     // Process button-dependent upgrades in reverse order as they are stored from top downwards
     for ( int j = devDependent.size() - 1; j >= 0; j-- )
     {
       String text = devDependent.get( j ).getDescription();
       if ( text != null && !text.trim().isEmpty() )
       {
         out.printf( "$%4X=%s\n", i, exportNotes( text ) );
       }
       ++i;
     }
 
     // Get the protocol upgrades in main upgrade area
     LinkedHashMap< Protocol, ProtocolUpgrade > outputProtocols = getOutputProtocolUpgrades( false );
     
     // Add the protocol upgrades from button-dependent section, reading top downward
     for ( int j = devDependent.size() - 1; j >= 0; j-- )
     {
       DeviceUpgrade dev = devDependent.get( j );
       if ( dev.needsProtocolCode() )
       {
         Protocol p = dev.getProtocol();
         if ( outputProtocols.get( p ) == null )
         {
           int pid = p.getID().get( 0 );
           outputProtocols.put( p, new ProtocolUpgrade( pid, dev.getCode(), p.getName() ) );
         }
       }
     }
 
     // Now write the protocol notes
     i = 0x4000;
     for ( ProtocolUpgrade protocol : outputProtocols.values() )
     {
       String text = protocol.getNotes();
       if ( text != null && !text.trim().isEmpty() )
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
       if ( text != null && !text.trim().isEmpty() )
       {
         out.printf( "$%4X=%s\n", i, exportNotes( text ) );
       }
       ++i;
     }
 
     // Do the device buttons
     i = 0x6000;
     for ( int j = 0; j < deviceButtonNotes.length; j++ )
     {
       String text = deviceButtonNotes[ j ];
       if ( text != null && !text.trim().isEmpty() )
       {
         out.printf( "$%4X=%s\n", i + j, exportNotes( text ) );
       }
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
   public DeviceUpgrade findDeviceUpgrade( DeviceButton deviceButton )
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
   public RemoteConfiguration( Remote remote, RemoteMaster rm )
   {
     owner = rm;
     this.remote = remote;
     SetupCode.setMax( remote.usesTwoBytePID() ? 4095 : 2047 );
 
     int eepromSize = remote.getEepromSize();
     data = new short[ eepromSize ];
     highlight = new Color[ eepromSize + 8 * remote.getSettingAddresses().size() ];
     for ( int i = 0; i < highlight.length; i++ )
     {
       highlight[ i ] = Color.WHITE;
     }
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
     importIR( null, true );
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
           // Convert specialized keymoves such as KeyMoveEFC5 to plain KeyMove type
           // when it is data for a special function.
           keyMove = new KeyMove( keyMove.getKeyCode(), keyMove.getDeviceButtonIndex(),
               keyMove.getData(), keyMove.getNotes() );          
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
   
   private void convertKeyMoves()
   {
     for ( ListIterator< KeyMove > it = keymoves.listIterator(); it.hasNext(); )
     {
       KeyMove keyMove = it.next();
 
       // ignore key-style keymoves
       if ( keyMove instanceof KeyMoveKey )
       {
         continue;
       }
 
       int keyCode = keyMove.getKeyCode();
       DeviceUpgrade moveUpgrade = findDeviceUpgrade( keyMove.getDeviceType(), keyMove.getSetupCode() );      
       Hex cmd = keyMove.getCmd();
       if ( remote.getAdvCodeBindFormat() == AdvancedCode.BindFormat.LONG 
           && remote.getAdvCodeFormat() == AdvancedCode.Format.HEX && moveUpgrade != null
           && moveUpgrade.getProtocol().getDefaultCmd().length() == 1 && cmd.length() == 2 )
       {
         cmd = cmd.subHex( 0, 1 );
         keyMove = new KeyMoveLong( keyCode, keyMove.getDeviceButtonIndex(), keyMove.getDeviceType(), keyMove
             .getSetupCode(), cmd, keyMove.getNotes() );
         it.set( keyMove );
       }
     }
   }
 
   /**
    * Migrate key moves to device upgrades.
    */
   private void migrateKeyMovesToDeviceUpgrades()
   {
     List< KeyMove > kmToRemove = new ArrayList< KeyMove >();
 
     for ( KeyMove keyMove : keymoves )
     {
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
         Hex cmd = keyMove.getCmd();
         boolean migrate = true;   // If upgrade is not on any other device button, do migrate.
         for ( int i : getDeviceButtonIndexList( boundUpgrade ) )
         {
           if ( i == keyMove.getDeviceButtonIndex() )
           {
             // Skip current device button index
             continue;
           }
           // Bound upgrade is also on this device button, so only migrate if this button
           // has same keymove.
           migrate = false;  // If no matching keymove on this device button then do not migrate.
           for ( KeyMove km : keymoves )
           {
             // Search through all keymoves
             if ( km.getDeviceButtonIndex() != i || km.getKeyCode() != keyCode )
             {
               // Skip since either wrong device button or wrong keycode
               continue;
             }
             // This keymove has right keycode and is for device button under test.
             // See if the actual move is the same.
             migrate = ( km.getDeviceType() == keyMove.getDeviceType() 
                 && km.getSetupCode() == keyMove.getSetupCode() 
                 && km.getCmd().equals( keyMove.getCmd() ) );
             // No need to search further.
             break;
           }
           if ( !migrate )
           {
             // Move was not the same, no need to look further as we know we should not migrate.
             break;
           } 
         }
 
         if ( migrate )
         {
           // Don't migrate keymoves on buttons in the button map for the device type
           Button b = remote.getButton( keyMove.getKeyCode() );
           if ( b != null )
           {
             migrate = !remote.getDeviceTypeByIndex( keyMove.getDeviceType() ).getButtonMap().isPresent( b );
           }
         }
         
         if ( migrate )
         {
           // Create corresponding function, if it does not already exist.
           Function f = boundUpgrade.getFunction( cmd );
           if ( f == null )
           {
             // Keymove notes that happen to be names of other keys cause problems with the
             // commented-out old version, as would the same note on more than one keymove.
 
 //            String text = keyMove.getNotes();
 //            if ( text == null )
 //            {
 //              text = remote.getButtonName( keyCode );
 //            }
 //            f = new Function( text, cmd, null );
             
             f = new Function( remote.getButtonName( keyCode ), cmd, keyMove.getNotes() );
             
             boundUpgrade.getFunctions().add( f );
           }
           // Perform the migration.
           System.err.println( "Moving keymove on " + boundDeviceButton + ':'
               + remote.getButtonName( keyMove.getKeyCode() ) + " to device upgrade " + boundUpgrade.getDeviceType()
               + '/' + boundUpgrade.getSetupCode() );
           boundUpgrade.setFunction( keyCode, f );
           kmToRemove.add( keyMove );
         }
       }
     }
     for ( KeyMove km : kmToRemove )
     {
       keymoves.remove( km );
     }
   }
 
   public List<Integer> getDeviceButtonIndexList( DeviceUpgrade upgrade )
   {
     List<Integer> dbList = new ArrayList< Integer >();
     DeviceButton[] deviceButtons = remote.getDeviceButtons();
     for ( int i = 0; i < deviceButtons.length; ++i )
     {
       DeviceButton button = deviceButtons[ i ];
       if ( button.getDeviceTypeIndex( data ) == upgrade.getDeviceType().getNumber()
           && button.getSetupCode( data ) == upgrade.getSetupCode() )
       {
         dbList.add( i );
       }
     }
     return dbList;
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
     // update upgrades last so that only spare space in other regions can be used for
     // upgrade overflow
     for ( int i = 0; i < highlight.length; i++ )
     {
       highlight[ i ] = Color.WHITE;
     }
     updateFixedData( false );
     updateAutoSet();
     updateDeviceButtons();
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
     updateLearnedSignals();
     updateUpgrades();
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
       updateHighlight( keyMove, offset, keyMove.getSize( remote ) );
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
       for ( Integer dbIndex : getDeviceButtonIndexList( device ) )
       {
         for ( KeyMove keyMove : device.getKeyMoves( dbIndex ) )
         {
           keyMove.setDeviceButtonIndex( dbIndex );
           rc.add( keyMove );
         }
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
     updateHighlight( favScan, offset, favScan.getSize( remote ) );
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
       updateHighlight( timedMacro, offset, timedMacro.getSize( remote ) );
       offset = timedMacro.store( data, offset, remote );
     }
     data[ offset++ ] = remote.getSectionTerminator();
     range.setFreeStart( offset );
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
       updateHighlight( macro, offset, macro.getSize( remote ) );
       offset = macro.store( data, offset, remote );
     }
     for ( Macro macro : specialFunctionMacros )
     {
       updateHighlight( macro, offset, macro.getSize( remote ) );
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
         updateHighlight( favScan, offset, favScan.getSize( remote ) );
         offset = favScan.store( data, offset, remote );
       }
     }
     if ( remote.getMacroCodingType().hasTimedMacros() )
     {
       for ( TimedMacro timedMacro : timedMacros )
       {
         updateHighlight( timedMacro, offset, timedMacro.getSize( remote ) );
         offset = timedMacro.store( data, offset, remote );
       }
       int timedMacroCountAddress = remote.getMacroCodingType().getTimedMacroCountAddress();
       if ( timedMacroCountAddress > 0 )
       {
         data[ timedMacroCountAddress ] = ( short )timedMacros.size();
       }
     }
     data[ offset++ ] = remote.getSectionTerminator();
     range.setFreeStart( offset );
 
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
   
   private void updateHighlight( Highlight item, int offset, int length )
   {
     for ( int i = 0; i < length; i++ )
     {
       highlight[ offset + i ] = item.getHighlight();
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
 
   private void updateDeviceButtons()
   {
     DeviceButton[] deviceButtons = remote.getDeviceButtons();
     for ( DeviceButton db : deviceButtons )
     {
       db.doHighlight( highlight );
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
       int index = remote.getSettingAddresses().get( setting.getByteAddress() );
       setting.doHighlight( highlight, index );
       setting.store( data, remote );
     }
   }
 
   private void updateFixedData( boolean replace )
   {
     boolean mismatch = false;
     FixedData[] fixedData = remote.getFixedData();
     if ( fixedData == null )
     {
       return;
     }
     for ( FixedData fixed : fixedData )
     {
       if ( ! fixed.check( data ) )
       {
         mismatch = true;
         break;
       }
     }
     if ( mismatch && ! replace )  
     {
       String message = "The fixed data in the RDF does not match the values in the remote.\n"
         + "Do you want to replace the values in the remote with those from the RDF?";
       String title = "Fixed data mismatch";
       replace = JOptionPane.showConfirmDialog( null, message, title, JOptionPane.YES_NO_OPTION, 
           JOptionPane.QUESTION_MESSAGE ) == JOptionPane.YES_OPTION;
     }
     if ( ! replace )
     {
       remote.setFixedData( null );
     }
     else for ( FixedData fixed : fixedData )
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
   public ProtocolUpgrade getProtocol( int pid )
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
     bounds[ 4 ] = addr.getEnd() + 1;
     if ( remote.getAdvancedCodeAddress() != null )
     {
       bounds[ 5 ] = remote.getAdvancedCodeAddress().getEnd() + 1;
     }
     else
     {
       bounds[ 5 ] = 0;
     }
     if ( remote.getLearnedAddress() != null )
     {
       bounds[ 6 ] = remote.getLearnedAddress().getEnd() + 1;
     }
     else
     {
       bounds[ 6 ] = 0;
     }
     if ( devAddr != null )
     {
       bounds[ 7 ] = devAddr.getEnd() + 1;
     }
     else
     {
       bounds[ 7 ] = 0;
     }
 
     // parse the protocol tables
     // special handling of zero offsets follows that in IR.exe
     int offset = protocolTableOffset;
     int count = ( offset == 0 ) ? 0 : processor.getInt( data, offset ); // get number of entries in upgrade table
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
     count = ( offset == 0 ) ? 0 : processor.getInt( data, offset ); // get number of entries in upgrade table
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
       int limit = getLimit( codeOffset, bounds );
       Hex deviceHex = Hex.subHex( data, codeOffset, limit - codeOffset );
       // Get the first protocol upgrade with matching pid, if there is one, as this is the one that
       // the remote will access - regardless of whether or not the remote has a built-in protocol
       // for this pid.  It may however be changed by importRawUpgrade() if this one is incompatible
       // with the device upgrade, eg different command length.
       protocolUpgradeUsed = getProtocol( pid );
       Hex protocolCode = null;
       if ( protocolUpgradeUsed != null )
       {
         protocolCode = protocolUpgradeUsed.getCode();
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
       pidHex[ 0 ] = ( short )( pid >> 8 );
       pidHex[ 1 ] = ( short )( pid & 0xFF );
 
       DeviceUpgrade upgrade = new DeviceUpgrade();
       try
       {
         upgrade.setRemoteConfig( this );
         upgrade.importRawUpgrade( deviceHex, remote, alias, new Hex( pidHex ), protocolCode );
         upgrade.setSetupCode( setupCode );
         if ( protocolUpgradeUsed != null )
         {
           // This may have been changed by importRawUpgrade, so setUsed cannot be set earlier.
           protocolUpgradeUsed.setUsed( true );
         }
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
         else if ( data[ offset + 1 ] > 0 )
         {
           // In-line protocol exists so get it, whether it is in this upgrade or another.
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
   
   public LinkedHashMap< Protocol, ProtocolUpgrade > getOutputProtocolUpgrades( boolean check )
   {
     // Build two maps from the required protocol upgrades:
     //   firstProtocols maps against each pid the first upgrade output for that pid, which is the only
     //     one that can be accessed by the remote;
     //   outputProtocols includes every upgrade that is to be output, mapped against the corresponding
     //     protocol.  Mapping in this way prevents duplicates being output when more than one device upgrade
     //     uses the same protocol upgrade.
     LinkedHashMap< Integer, ProtocolUpgrade > firstProtocols = new LinkedHashMap< Integer, ProtocolUpgrade >();
     LinkedHashMap< Protocol, ProtocolUpgrade > outputProtocols = new LinkedHashMap< Protocol, ProtocolUpgrade >();
 
     for ( DeviceUpgrade dev : devices )
     {
       if ( dev.getButtonIndependent() && dev.needsProtocolCode() )
       {
         Protocol p = dev.getProtocol();
         ProtocolUpgrade output = p.getProtocolUpgrade( remote );
         if ( output == null )
         {
           // The protocol code is missing, so nothing to output
           continue;
         }
         Hex code = output.getCode();
         if ( code == null || code.length() == 0 )
         {
           continue;
         }
 
         output.setHighlight( dev.getProtocolHighlight() );
         int pid = output.getPid();
         ProtocolUpgrade first = firstProtocols.get( pid );
         if ( first == null )
         {
           // First device upgrade to use protocol with this pid
           firstProtocols.put( pid, output );
         }
         else if ( first.getCode().equals( code ) )
         {
           // Don't output a second copy of the same code with the same PID when it
           // comes from a different device upgrade.
           continue;
         }
         else if ( check )
         {
           String message = "The protocol code used by the device upgrade for " + dev.getDeviceTypeAliasName() + '/'
           + dev.getSetupCode()
           + " is different from the code already used by another device upgrade, and may not work as intended.";
           JOptionPane.showMessageDialog( null, message, "Protocol Code Mismatch", JOptionPane.ERROR_MESSAGE );
         }
         outputProtocols.put( p, output );
       }
     }
 
     // The installed protocols that aren't used by any device upgrade
     // also go in the device independent section.
     for ( ProtocolUpgrade pu : protocols )
     {
       outputProtocols.put( pu.getProtocol(), pu );
     }
     return outputProtocols;
   }
   
   private class updateLocator
   {
     private int activeRegion = 0; // 0 = upgrade, 1 = learned, 2 = adv codes
     private int tableSize = 0;
     private boolean full = false;
     private boolean oldOverflow = false;
     private AddressRange upgRange = remote.getUpgradeAddress();
     private AddressRange advRange = remote.getAdvancedCodeAddress();
     private AddressRange lrnRange = remote.getLearnedAddress();
     
     public updateLocator( int tableSize )
     {
       this.tableSize = tableSize;
       upgRange.setFreeStart( upgRange.getStart() + 4 ); // Bypass table pointers
       if ( lrnRange != null )
       {
         oldOverflow |= ( lrnRange.getFreeEnd() < lrnRange.getEnd() );
         lrnRange.setFreeEnd( lrnRange.getEnd() );
       }
       if ( advRange != null )
       {
         oldOverflow |= ( advRange.getFreeEnd() < advRange.getEnd() );
         advRange.setFreeEnd( advRange.getEnd() );
       }
     }
     
     public int nextOffset( int length )
     {
       if ( activeRegion == 0 )
       {
         int end = upgRange.getFreeStart() + length;
         if ( end + tableSize <= upgRange.getEnd() + 1 )
         {
           full = false;
           upgRange.setFreeStart( end );
           return end - length;
         }
         else if ( lrnRange != null )
         {
           activeRegion = 1;
         }
         else if ( advRange != null )
         {
           activeRegion = 2;
         }
         else
         {
           full = true;
           upgRange.setFreeStart( end );
           return end - length;
         }
       }
       
       if ( activeRegion == 1)
       {
         int start = lrnRange.getFreeEnd() - length + 1;
         if ( start >= lrnRange.getFreeStart() )
         {
           full = false;
           lrnRange.setFreeEnd( start - 1 );
           return start;
         }
         else if ( advRange != null )
         {
           activeRegion = 2;
         }
         else
         {
           full = true;
           lrnRange.setFreeEnd( start - 1 );
           return start;
         } 
       }
       
       if ( activeRegion == 2 )
       {
         int start = advRange.getFreeEnd() - length + 1;
         full = ( start < advRange.getFreeStart() );
         advRange.setFreeEnd( start - 1 );
         return start;
       }
       
       return 0;    
     }
     
     public boolean isFull()
     {
       return full;
     }
     
     public boolean newOverflow()
     {
       return ( activeRegion > 0 ) && !oldOverflow;
     }
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
     
     // Get the protocols for the device-independent section
     LinkedHashMap< Protocol, ProtocolUpgrade > outputProtocols = getOutputProtocolUpgrades( false );
 
     // Get the address ranges
     AddressRange addr = remote.getUpgradeAddress();
     AddressRange devAddr = remote.getDeviceUpgradeAddress();
     if ( addr == null && devAddr == null )
     {
       return;
     }
     
     // Get the processor
     Processor processor = remote.getProcessor();
 
     // Get the counts of device and protocols and calculate the size of the corresponding
     // tables, including the two bytes per table used to record their counts.
     int devCount = devIndependent.size();
     int prCount = outputProtocols.size();
     int tableSize = 4 * ( devCount + prCount + 1 );
     if ( processor.getName().equals( "740" ) )
     {
       // Remotes with the 740 processor store an additional address at the end of each
       // of the device and protocol tables.
       tableSize += 4;
     }
     
     // Initialize the update locator used to position the updates in both the upgrade
     // and overflow sections.  Create the offset variable - initial value is irrelevant.
     updateLocator ul = new updateLocator( tableSize );
     int offset = 0;
 
     // Store the device upgrades of the device independent section.  Note that
     // devUpgradesEnd is the end of those device upgrades in the upgrades section,
     // there may be others in overflow sections.
     int[] devOffsets = new int[ devCount ];
     int i = 0;
     for ( DeviceUpgrade dev : devIndependent )
     {
       Hex hex = dev.getUpgradeHex();
       offset = ul.nextOffset( hex.length() );
       // Only store the data if there is space for it, but store the (possibly hypothetical)
       // offset in all cases, though never allowing it to go negative.
       devOffsets[ i++ ] = Math.max( 0, offset );
       if ( !ul.isFull() )
       {
         updateHighlight( dev, offset, hex.length() );
         Hex.put( hex, data, offset );
       }
     }
     int devUpgradesEnd = addr.getFreeStart() + remote.getBaseAddress();
 
     // Store the protocol upgrades.  Note that protUpgradesEnd is the end of those 
     // protocol upgrades in the upgrades section, there may be others in overflow sections.
     int[] prOffsets = new int[ prCount ];
     i = 0;
     for ( ProtocolUpgrade upgrade : outputProtocols.values() )
     {
       Hex hex = upgrade.getCode();
       // Check that there is protocol code for this processor - manual settings,
       // if care is not taken, can create a protocol for the wrong processor and
       // so lead to hex being null.
       if ( hex != null )
       {
         offset = ul.nextOffset( hex.length() );
         // Only store the data if there is space for it, but store the (possibly hypothetical)
         // offset in all cases, though never allowing it to go negative.
         prOffsets[ i++ ] = Math.max( 0, offset );      
         if ( !ul.isFull() )
         {
           updateHighlight( upgrade, offset, hex.length() );
           Hex.put( hex, data, offset );
         }
       }
     }
     int protUpgradesEnd = addr.getFreeStart() + remote.getBaseAddress();
 
     // Reset offset to the first free byte in the upgrades section; before this reset
     // it may address a byte in an overflow section.
     offset = addr.getFreeStart();
 
     // set the pointer to the device table.
     processor.putInt( protUpgradesEnd, data, addr.getStart() );
 
     // create the device table
     processor.putInt( devCount, data, offset );
     offset += 2;
     // store the setup codes
     for ( DeviceUpgrade dev : devIndependent )
     {
       updateHighlight( dev, offset, 2 );
       processor.putInt( Hex.get( dev.getHexSetupCode(), 0 ), data, offset );
       offset += 2;
     }
     // store the offsets
     i = 0;
     for ( int devOffset : devOffsets )
     {
       updateHighlight( devIndependent.get( i++ ), offset, 2 );
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
     i = 0;
     Color protocolHighlights[] = new Color[ prCount ];
     for ( ProtocolUpgrade pr : outputProtocols.values() )
     {
       updateHighlight( pr, offset, 2 );
       processor.putInt( pr.getPid(), data, offset );
       offset += 2;
       protocolHighlights[ i++ ] = pr.getHighlight();
     }
     for ( i = 0; i < prCount; ++i )
     {
       highlight[ offset ] = highlight[ offset + 1 ] = protocolHighlights[ i ];
       processor.putInt( prOffsets[ i ] + remote.getBaseAddress(), data, offset );
       offset += 2;
     }
 
     if ( processor.getName().equals( "740" ) )
     {
       processor.putInt( protUpgradesEnd, data, offset );
       offset += 2;
       processor.putInt( offset - addr.getStart() + 2, data, addr.getStart() - 2 );
     }
     
     addr.setFreeStart( offset );
     
     if ( ul.newOverflow() )
     {
       String title = "Upgrade Overflow";
       String message = "The upgrades have overflowed into the Learned and/or\n" +
                        "Move/Macro regions.  Progress bars for regions that include\n" +
                        "such overflow are YELLOW instead of the normal AQUAMARINE.";
       JOptionPane.showMessageDialog( null, message, title, JOptionPane.INFORMATION_MESSAGE );
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
           if ( hex != null && hex.length() > 0 )
           {
             offset -= hex.length();
             for ( int j = 0; j < hex.length(); j++ )
             {
               highlight[ offset + j ] = upg.getProtocolHighlight();
             }
             Hex.put( hex, data, offset );
             lastProtID = protID;
             lastProtAddr = offset;
             protOffset = upgLength + 5;
           }
           else
           {
             // Protocol code is missing.  Do nothing, treating it as
             // code not required.
           }
         }
       }
       // Store the device upgrade
       Hex hex = upg.getUpgradeHex();
       offset -= upgLength + 5;
       updateHighlight( upg, offset, hex.length() + 5 );
       Hex.put( hex, data, offset + 5 );
       Hex.put( upg.getHexSetupCode(), data, offset + 3 );
       data[ offset + 2 ] = ( short )buttonIndex;
       data[ offset + 1 ] = ( short )protOffset;
       data[ offset ] = ( short )( lastDevAddr - offset );
       upg.setDependentOffset( offset );
       lastDevAddr = offset;
       devAddr.setFreeEnd( offset - 1 );
     }
     offset = devAddr.getStart();
     processor.putInt( lastDevAddr - offset, data, offset );
     devAddr.setFreeStart( offset + 2 );
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
       updateHighlight( ls, offset, ls.getSize() );
       offset = ls.store( data, offset, remote );
     }
     data[ offset++ ] = remote.getSectionTerminator();
     addr.setFreeStart( offset );
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
       ManualProtocol mp = protocol.getManualProtocol( remote );
       if ( mp != null )
       {
         pw.printHeader( "ManualProtocol" );
         pw.print( "Name", mp.getName() );
         pw.print( "PID", mp.getID() );
         mp.store( pw );
       }
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
 
   public Color[] getHighlight()
   {
     return highlight;
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
 
   public void setKeyMoves( List< KeyMove > keymoves )
   {
     this.keymoves = keymoves;
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
   
   public void setDeviceUpgrades( List< DeviceUpgrade > devices )
   {
     this.devices = devices;
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
   
   public void setProtocolUpgrades( List< ProtocolUpgrade > protocols )
   {
     this.protocols = protocols;
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
   
   private Color[] highlight = null;
 
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
 
   public void initializeSetup( int startAddr )
   {
     // Fill buffer with 0xFF
     Arrays.fill( data, startAddr, data.length, ( short )0xFF );
 
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
       if ( !setting.isInverted() && ( setting.getByteAddress() >= startAddr ) )
       {
         data[ setting.getByteAddress() ] = 0;
       }
     }    
     
     // Set the fixed data without asking for permission
     updateFixedData( true );
 
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
     MacroDialog.reset();
     TimedMacroDialog.reset();
     SpecialFunctionDialog.reset();
     FavScanDialog.reset();
     LearnedSignalDialog.reset();
   }
   
   public boolean allowHighlighting()
   {
     return owner.highlightItem.isSelected();
   }
 
   /** The notes. */
   private String notes = null;
 
   private String[] deviceButtonNotes = null;
 
   private DeviceButton favKeyDevButton = null;
   
   public ProtocolUpgrade protocolUpgradeUsed = null;
   
   private RemoteMaster owner = null;
 
   public RemoteMaster getOwner()
   {
     return owner;
   }
 
 }
