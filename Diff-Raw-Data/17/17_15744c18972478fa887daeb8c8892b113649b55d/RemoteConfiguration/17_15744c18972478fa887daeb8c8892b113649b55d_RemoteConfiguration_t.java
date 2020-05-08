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
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
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
 
 import com.hifiremote.jp1.Activity.Assister;
 import com.hifiremote.jp1.FixedData.Location;
 
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
     RemoteMaster.setSystemFilesItems( remote.isSSD() );
   }
   
   public RemoteConfiguration( String str, RemoteMaster rm, Remote remote ) throws IOException
   {
     owner = rm;
     this.remote = remote;
     BufferedReader in = new BufferedReader( new StringReader( str ) );
     PropertyReader pr = new PropertyReader( in );
     importIR( pr, false );
     in.close();
     RemoteMaster.setSystemFilesItems( remote.isSSD() );
   }
   
   private void createActivities()
   {
     Button fav = remote.getButtonByStandardName( "Favorites" );
     if ( fav != null )
     {
       activities = new LinkedHashMap< Button, Activity >();
       Activity activity = new Activity( fav, remote );
       activities.put( fav, activity );
     }
     
     LinkedHashMap< String, List< Button > > buttonGroups = remote.getButtonGroups();
     List< Button > activityBtns = null;
     if ( buttonGroups != null )
     {
       activityBtns = buttonGroups.get(  "Activity" );
     }
     if ( activityBtns == null )
     {
       return;
     }
     if ( fav == null )
     {
       activities = new LinkedHashMap< Button, Activity >();
     }
     for ( Button btn : activityBtns )
     {
       Activity activity = new Activity( btn, remote );
       Setting setting = remote.getSetting( "AudioHelp" );
       if ( setting != null )
       {
         activity.setAudioHelp( setting.getInitialValue() );
       }
       setting = remote.getSetting( "VideoHelp" );
       if ( setting != null )
       {
         activity.setVideoHelp( setting.getInitialValue() );
       }
       activities.put( btn, activity );
     }
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
     
     // Ensure any properties of the remote left from previous configuration are removed
     Remote newRemote = new Remote( remote, remote.getNameIndex() );
     RemoteManager.getRemoteManager().replaceRemote( remote, newRemote );
     remote = newRemote;
 
     String prop = section.getProperty( "Remote.sigData" );
     if ( prop != null )
     {
       sigData = Hex.parseHex( prop );
     }
     SetupCode.setMax( remote.getSegmentTypes() == null ? remote.usesTwoBytePID() ? 4095 : 2047 : 0x7FFF );
     notes = section.getProperty( "Notes" );
 
     deviceButtonNotes = new String[ remote.getDeviceButtons().length ];
 
     loadBuffer( pr );
     
     int e2FormatOffset = remote.getProcessor().getE2FormatOffset();
     if ( e2FormatOffset >= 0 )
     {
       char[] val = new char[ 6 ];
       for ( int i = 0; i < 6; i++ )
       {
         val[ i ] = ( char )data[ e2FormatOffset + i ];
       }
       eepromFormatVersion = new String( val );               
     }
     
     if ( remote.isSSD() )
     {
       loadFiles( false );
     }
     else if ( hasSegments() )
     {
       loadSegments( false );
     }
     
     HashMap< Button, Macro > activityMacros = null;
     if ( remote.hasActivitySupport() )
     {
       activityMacros = new HashMap< Button, Macro >();
     }
 
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
           String s = section.getProperty( setting.getTitle() );
           if ( s != null )
           {
             setting.setValue( Integer.parseInt( s ) );
           }
         }
       }
       else if ( sectionName.equals( "DeviceUpgrade" ) )
       {
         DeviceUpgrade upgrade = new DeviceUpgrade();
         upgrade.setRemoteConfig( this );
         upgrade.load( section, true, remote );
         devices.add( upgrade );
         if ( remote.usesEZRC() )
         {
           upgrade.classifyButtons();
         }
       }
       else if ( sectionName.equals( "FavData" ) )
       {
         Button favBtn = remote.getButton( remote.getFavKey().getKeyCode() );
         Activity activity = null;
         if ( favBtn != null && ( activity = activities.get( favBtn )) != null )
         {
           ActivityGroup.parse( section, activity );
           activity.set( remote );
         }
         String temp = section.getProperty( "Pause" );
         if ( temp != null )
         {
           favPause = Integer.parseInt( temp );
         }
         temp = section.getProperty( "FinalKey" );
         if ( temp != null )
         {
           favFinalKey = remote.getButton( Integer.parseInt( temp ) );
         }
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
             Macro macro = ( Macro )o;
             Button button;
             if ( remote.usesEZRC() )
             {
               // Convert data to form used in macros with durations
               short[] mData = macro.getData().getData();
               int len = mData.length / 2;
               Hex hex = new Hex( len );
               for ( int i = 0; i < len; i++ )
               {
                 hex.set( ( short )( mData[ i ] + ( mData[ len + i ] << 8 ) ), i );
               }
               macro.setData( hex );
               button = remote.getButton( macro.getKeyCode() );
             }
             else
             {
               button = remote.getButton( macro.getDeviceIndex() );
             }
             if ( activityMacros != null && remote.getButtonGroups().get( "Activity" ).contains( button ) )
             {
               activityMacros.put( button, macro );
             }
             else
             {
               macros.add( macro );
             }
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
           else if ( sectionName.equals( "Activity" ) )
           {
             Activity activity = ( Activity )o;
             activity.set( remote );
             if ( activities == null )
             {
               activities = new LinkedHashMap< Button, Activity >();
             }
             activities.put( activity.getButton(), activity );
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
     if ( activityMacros != null )
     {
       for ( Button btn : activityMacros.keySet() )
       {
         Activity activity = activities.get( btn );
         Macro macro = activityMacros.get( btn );
         activity.setMacro( macro );
         if ( activity.getSelector() == null )
         {
           activity.setSelector( remote.getButton( macro.getKeyCode() ) );
         }
       }
     }
     convertKeyMoves();
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
     String signature = null;
     List< short[] > values = new ArrayList< short[] >();
     Property property = pr.nextProperty();
     
     if ( property.name.equals( "[Signature]" ) )
     {
       int sigDataLen = 0;
       do
       {
         property = pr.nextProperty();
         short[] data = Hex.parseHex( property.value );
         sigDataLen += data.length;
         values.add( data );
       } 
       while ( ( property != null ) && ( property.name.length() > 0 ) );
       
       sigData = new short[ Math.min( sigDataLen, 56 ) ];
       sigDataLen = 0;
       for ( short[] data : values )
       {
         if ( sigDataLen > sigData.length )
         {
           break;
         }
         System.arraycopy( data, 0, sigData, sigDataLen, Math.min( data.length, sigData.length - sigDataLen ) );
         sigDataLen += data.length;
       }
       int sigbase = ( ( sigData[ 0 ] + sigData[ 1 ] ) == 0xFF ) ? 6 : 0;
       int sigLen = ( sigDataLen <= 8 ) ? sigDataLen : 6;
       char[] sig = new char[ sigLen ];
       for ( int i = 0; i < sigLen; i++ )
       {
         sig[ i ] = ( char )sigData[ sigbase + i ];
       }
       signature = new String( sig );
       if ( sigDataLen <= 8 )
       {
         // Not the full signature block so reset to defaults
         sigData = null;
       }
 
       while ( ( property != null ) && ( property.name.length() == 0 ) )
         property = pr.nextProperty();
     }
     
     if ( property.name.equals( "[Buffer]" ) || property.name.equals( "" ) )
     {
       property = pr.nextProperty();
     }
 
     int baseAddr = Integer.parseInt( property.name, 16 );
     
     List< Integer > offsets = new ArrayList< Integer >();    
     values.clear();
     
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
       if ( signature == null )
       {
         signature = Hex.getRemoteSignature( data );
       }
       String signature2 = null;
       RemoteManager rm = RemoteManager.getRemoteManager();
       List< Remote > remotes = null;
       for ( int i = signature.length(); i >= 4; i-- )
       {
         signature2 = signature.substring( 0, i );
         remotes = rm.findRemoteBySignature( signature2 );
         if ( !remotes.isEmpty() ) break;
       }
       signature = signature2;
       remote = filterRemotes( remotes, signature, eepromSize, data, sigData, true );
       if ( remote == null )
       {
         throw new IllegalArgumentException( "No matching remote selected for signature " + signature );
       }
     }
     if ( remote.isSSD() && eepromSize < remote.getEepromSize() )
     {
       data = Arrays.copyOf( data, remote.getEepromSize() );
       Arrays.fill( data, eepromSize, remote.getEepromSize(), ( short )0xFF );
       eepromSize = remote.getEepromSize();
     }
     remote.setFixedData( remote.getRawFixedData() );
     remote.load();
     createActivities();
     highlight = new Color[ eepromSize + 8 * remote.getSettingAddresses().size() ];
     for ( int i = 0; i < highlight.length; i++ )
     {
       highlight[ i ] = Color.WHITE;
     }
     SetupCode.setMax( remote.getSegmentTypes() == null ? remote.usesTwoBytePID() ? 4095 : 2047 : 0x7FFF );
 
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
   
   public boolean hasSegments()
   {
     return remote.getSegmentTypes() != null;
   }
   
   public LinkedHashMap< Integer, List< Segment >> getSegments()
   {
     return segments;
   }
 
   public LinkedHashMap< Button, Activity > getActivities()
   {
     return activities;
   }
   
   private void loadFiles( boolean decode )
   {
     int pos = 4;
     int index = -1;
     int status = data[ 0 ] + ( ( data[ 1 ] & 0x0F ) << 8 );
     int end = data[ 2 ] + ( data[ 3 ] << 8 ) + ( ( data[ 1 ] & 0xF0 ) << 12 );
     ssdFiles.clear();
     for ( DeviceButton db : remote.getDeviceButtons() )
     {
       db.setSegment( null );
     }
     
     Items items = new Items();
     while ( pos < end )
     {
       while ( index < 16 && ( status & ( 1 << ++index ) ) == 0 ) {};
       if ( index == 16 )
       {
         break;
       }
       String name = Remote.userFilenames[ index ];
       SSDFile file = new SSDFile();
       if ( name.endsWith( ".xcf" ) )
       {
         List< String > tagNames = new ArrayList< String >();
         file.tagNames = tagNames;
         System.err.println( name + " tags:" );
         int itemsLength = data[ pos + 14 ] + 0x100 * data[ pos + 15 ];
         pos += 16;
         int itemCount = data[ pos++ ];
         int itemsEnd = pos + itemsLength;
         char ch;
         for ( int i = 0; i < itemCount; i++ )
         {
           StringBuilder sb = new StringBuilder();
           while ( ( ch = ( char )data[ pos++ ] ) != 0 )
           {
             sb.append( ch );
           }
           String tag = Integer.toHexString( i );
           if ( tag.length() == 1 )
           {
             tag = "0" + tag;
           }
           String tagName = sb.toString();
           tagNames.add( tagName );
           System.err.println( "  " + tag + "  " + tagName );
         }
         if ( pos != itemsEnd )
         {
           System.err.println( "Parsing error in " + name );
           break;
         }
         int start = pos;
         pos = parseXCFFile( items, index, start, tagNames, decode );
         Hex hex = new Hex( data, start, pos - start );
         file.hex = hex;
         ssdFiles.put( name, file );
         System.err.println( name + " hex = " + hex );
       }
       else if ( name.endsWith( ".pkg" ) )
       {
         // icons file
         file.userIcons = new LinkedHashMap< Integer, Icon >();
         int numIcons = data[ pos + 12 ] + 0x100 * data[ pos + 13 ];
         int numEntries = data[ pos + 14 ] + 0x100 * data[ pos + 15 ];
         int fileStart = pos;
         int fileEnd = pos;
         pos += 16;
         int startIndex = pos + 28 * numIcons;
         for ( int i = 0; i < numEntries; i++ )
         {
           int j = data[ startIndex + i ];
           if ( j == 0 )
           {
             continue;
           }
           int k = pos + 28 * ( j - 1 );
           Icon icon = new Icon();
           icon.height = data[ k + 8 ] + 0x100 * data[ k + 9 ];
           icon.width = data[ k + 10 ] + 0x100 * data[ k + 11 ];
           int start = data[ k + 16 ] + 0x100 * data[ k + 17 ] + 0x10000 * data[ k + 18 ];
           int size = data[ k + 24 ] + 0x100 * data[ k + 25 ] - 0x200;
           icon.hex1 = new Hex( data, start, size );
           start += size;
           if ( start == data[ k + 20 ] + 0x100 * data[ k + 21 ] + 0x10000 * data[ k + 22 ] )
           {
             size = icon.height * icon.width;
             icon.hex2 = new Hex( data, start, size );
           }
           else
           {
             System.err.println( "Icon at index " + ( j - 1 ) + " has inconsistent data" );
           }
           file.userIcons.put( i, icon );
           fileEnd = Math.max( fileEnd, start + size );
         }
 //        if ( fileEnd != end )
 //        {
 //          System.err.println( "Data error: reported end = " + Integer.toHexString( end ) + ", calculated end = " + Integer.toHexString( fileEnd ) );
 //        }
         Hex hex = new Hex( data, fileStart, end - fileStart );
         file.hex = hex;
         ssdFiles.put( name, file );
         System.err.println( name + " hex = " + hex );
       }
 //      else
 //      {
 //        //Applies only to usericons.pkg, the last file
 //        Hex hex = new Hex( data, pos, end - pos );
 //        file.hex = hex;
 //        ssdFiles.put( name, file );
 //      }
     }
   }
   
   private void decodeItem( Items items, int fileIndex, String tag, int pos, boolean start, boolean dbOnly )
   {
     if ( fileIndex < 2 || fileIndex > 6 )
     {
       return;
     }
     int ndx = 0;
     if ( start )
     {
       if ( tag.equals( "favorites" ) )
       {
         items.activity = activities.get( remote.getButtonByStandardName( "Favorites" ) );
       }
       else if ( tag.equals( "device" ) )
       {
         int len = data[ pos++ ];
         // Convert device serial into a keycode by adding 0x50, to take it
         // out of the range of real keycodes
         int dev = 0x50 + data[ pos++ ];
         char[] ch = new char[ len / 2 ];
         for ( int i = 0; i < len - 1; i += 2 )
         {
           ch[ i / 2 ] = ( char )( data[ pos + i ] + 0x100 * data[ pos + i + 1 ] );
         }
         String name = new String( ch );
         items.db = remote.getDeviceButton( dev );
         items.dbList.add( items.db );
         items.db.setName( name );
         new Segment( 0, 0xFF, new Hex( 15 ), items.db );
       }
       else if ( tag.equals( "activity" ) )
       {
         int len = data[ pos++ ];
         // Convert activity serial into a keycode by adding 0xF0
         int keyCode = 0xF0 + data[ pos++ ];
         char[] ch = new char[ len / 2 ];
         for ( int i = 0; i < len - 1; i += 2 )
         {
           ch[ i / 2 ] = ( char )( data[ pos + i ] + 0x100 * data[ pos + i + 1 ] );
         }
         String name = new String( ch );
         Button btn = remote.getButton( keyCode );
         items.key = new Key();
         items.key.btn = btn;
         items.key.keycode = keyCode;
         items.activity = activities.get( btn );
         items.activity.setActive( true );
         items.activity.setSelector( btn );
         items.activity.setName( name );
         items.activity.getAssists().clear();
         items.activities.add( items.activity );
       }
       else if ( tag.equals( "profile" ) )
       {
         int len = data[ pos++ ];
         int profileIndex = data[ pos++ ];
         char[] ch = new char[ len / 2 ];
         for ( int i = 0; i < len - 1; i += 2 )
         {
           ch[ i / 2 ] = ( char )( data[ pos + i ] + 0x100 * data[ pos + i + 1 ] );
         }
         String name = new String( ch );
         items.activity = remote.getFavKey().createProfile( name, profileIndex, remote );
       }
       else if ( tag.equals( "favorite" ) )
       {
         int len = data[ pos++ ];
         int serial = data[ pos++ ];
         char[] ch = new char[ len / 2 ];
         for ( int i = 0; i < len - 1; i += 2 )
         {
           ch[ i / 2 ] = ( char )( data[ pos + i ] + 0x100 * data[ pos + i + 1 ] );
         }
         String name = new String( ch );
         items.fav = new FavScan( remote.getFavKey().getKeyCode(), null, null );
         items.fav.setSerial( serial );
         items.fav.setName( name );
         items.fav.setProfileIndices( new ArrayList< Integer >() );
         favScans.add( items.fav );
       }
       else if ( ( ndx = Arrays.asList( "brand", "model", "remotemodel" ).indexOf( tag ) ) >= 0 )
       {
         int len = data[ pos ];
         Segment seg = items.db.getSegment();
         int oldLen = seg.getHex().length();
         Hex hex = new Hex( seg.getHex(), 0, oldLen + len );
         for ( int i = 0; i < len + 1; i++ )
         {
           hex.getData()[ oldLen - 3 + ndx + i ] = ( short )( data[ pos++ ] & 0xFF );
         }
         seg.setHex( hex );
       }
       else if ( tag.equals( "volumemaster" ) )
       {
         items.db.setVpt( 2 );
       }
       else if ( tag.equals( "volumelock" ) )
       {
         if ( items.db.getVpt() != 2 )
         {
           items.db.setVpt( 1 );
         }
       }
       else if ( tag.equals( "profileindex" ) )
       {
         pos++;
         items.fav.getProfileIndices().add( ( int )data[ pos++ ] );
       }
       else if ( tag.equals( "deviceid" ) )
       {
         pos++;
         favKeyDevButton = remote.getDeviceButton( data[ pos++ ] + 0x50 );
       }
       else if ( tag.equals(  "iconref" ) )
       {
         pos++;
         if ( fileIndex == 2 )
         {
           if ( items.key != null )
           {
             items.key.iconref = ( int )data[ pos++ ];
           }
           else
           {
             items.db.setIconRef( data[ pos++ ] );
           }
         }
         else if ( fileIndex == 3 || fileIndex == 4 )
         {
           if ( items.key != null && items.activity.getButton() != items.key.btn )
           {
             items.key.iconref = ( int )data[ pos++ ];
           }
           else
           {
             items.activity.setIconRef( data[ pos++ ] );
           }
         }
         else if ( fileIndex == 5 )
         {
           items.fav.setIconRef( ( int )data[ pos++ ] );
         }
       }
       else if ( tag.equals(  "channelnumber" ) )
       {
         pos++;
         List< Integer > digits = new ArrayList< Integer >();
         digits.add( data[ pos + 1 ] >> 4 );
         digits.add( data[ pos + 1 ] & 0x0F );
         digits.add( data[ pos ] >> 4 );
         digits.add( data[ pos ] & 0x0F );
         for ( int i = 0; i < 4 - favKeyDevButton.getFavoriteWidth(); i++ )
         {
           digits.remove( 0 );
         }
         Hex hex = new Hex( digits.size() );
         for ( int i = 0; i < digits.size(); i++ )
         {
           String s = new String( new char[]{ ( char )( digits.get( i ) + 0x30 ) } );
           Button b = remote.getButtonByStandardName( s );
           hex.set( b == null ? 0 : b.getKeyCode(), i );
         }
         items.fav.setData( hex );
       }
       else if ( tag.equals(  "favoritewidth" ) )
       {
         pos++;
         items.db.setFavoritewidth( data[ pos++ ] );
       }
       else if ( tag.equals(  "enterkey" ) )
       {
         pos++;
         favFinalKey = remote.getButton( data[ pos++ ] );
       }
       else if ( tag.equals( "codeset" ) )
       {
         int len = data[ pos++ ];
         short[] segData = items.db.getSegment().getHex().getData();
         int typeIndex = data[ pos++ ];
         items.db.setDeviceTypeIndex( ( short )typeIndex, segData );
         char[] ch = new char[ len - 1 ];
         for ( int i = 0; i < len - 1; i++ )
         {
           ch[ i ] = ( char )data[ pos++ ];
         }
         String str = new String( ch );
         int code = 0;
         try
         {
           code = Integer.parseInt( str );
         }
         catch ( NumberFormatException e )
         {
           items.db.setConstructed( true );
           System.err.println( "Malformed setup code " + str + " at " + Integer.toHexString( pos - 4 ) );
         }
         items.setupCode = code;
         items.db.setSetupCode( ( short )items.setupCode, segData );
       }
       if ( dbOnly )
       {
         return;
       }
       if ( tag.equals( "codeset" ) )
       {
         short[] segData = items.db.getSegment().getHex().getData();
         int typeIndex = items.db.getDeviceTypeIndex( segData );
         items.upgrade = new DeviceUpgrade();
         items.upgrade.setRemoteConfig( this );
         items.upgrade.setRemote( remote );
         items.upgrade.setButtonIndependent( false );
         items.upgrade.setButtonRestriction( items.db );
         items.db.setUpgrade( items.upgrade );
         items.alias = remote.getDeviceTypeAlias( remote.getDeviceTypeByIndex( typeIndex ) );        
         devices.add( items.upgrade );
         items.keys = new ArrayList< Key >();
       }
       else if ( tag.equals( "prefix" ) )
       {
         int len = data[ pos++ ];
         items.fixedData = new Hex( len );
         items.upgrade.setSizeDevBytes( len );
         System.arraycopy( data, pos, items.fixedData.getData(), 0, len );
       }
       else if ( tag.equals( "executor" ) )
       {
         int len = data[ pos++ ];
         items.pid = new Hex( 2 );
         if ( len == 2 )
         {
           items.pid.set( data[ pos++ ], 1 );
           items.pid.set( data[ pos++ ], 0 );
         }
       }
       else if ( tag.equals( "keydef" ) )
       {
         items.key = new Key();
         items.keys.add( items.key );
         pos++;
         items.key.keycode = data[ pos++ ];
         if ( items.softPageIndex != null )
         {
           items.key.keycode += 6 * items.softPageIndex;
         }
         items.key.btn = remote.getButton( items.key.keycode );
       }
       else if ( tag.equals( "keygid" ) )
       {
         int len = data[ pos++ ];
         if ( len == 2 )
         {
           items.key.keygid = data[ pos ] + 0x100 * data[ pos + 1 ];
         }
       }
       else if ( tag.equals( "keyflags" ) )
       {
         pos++;
         items.key.keyflags = ( int )data[ pos++ ];
       }
       else if ( tag.equals( "irdata" ) )
       {
         int len = data[ pos++ ];
         items.key.irdata = new Hex( len );
         System.arraycopy( data, pos, items.key.irdata.getData(), 0, len );
         items.upgrade.setSizeCmdBytes( len );
       }
       else if ( tag.equals(  "learnedkey" ) )
       {
         // 1st byte is usual length but next 8 bytes not yet understood
         pos += 2;
         Hex header = Hex.subHex( data, pos, 7 );
         System.err.println( "Learned header: " + items.db.getName() + "/" + items.key.fname + ": " + header );
         pos += 7; 
         int len = data[ pos++ ];  // length of learned signal
         Hex hex = Hex.subHex( data, pos, len );
         LearnedSignal ls = new LearnedSignal( items.key.keycode, items.db.getButtonIndex(), 0, hex, null );
         ls.setHeader( header );
         items.key.ls = ls;
         learned.add( ls );
       }
       else if ( tag.equals( "name8" ) )
       {
         int len = data[ pos++ ];
         char[] ch = new char[ len ];
         for ( int i = 0; i < len; i++ )
         {
           ch[ i ] = ( char )data[ pos++ ];
         }
         items.key.fname = new String( ch );
       }
       else if ( tag.equals( "macroref" ) )
       {
         pos++;
         int ref = data[ pos ] + 0x100 * data[ pos + 1 ];
         pos += 2;
         items.key.macroref = ref;
         Macro macro = new Macro( items.key.keycode, null, null );
         macro.setSerial( ref );
         if ( items.db != null )
         {
           macro.setDeviceIndex( items.db.getButtonIndex() );
           macros.add( macro );
         }
         else if ( items.activity != null )
         {
           items.activity.setMacro( macro );
         }
       }
       else if ( tag.equals( "objcode" ) )
       {
         int len = data[ pos++ ];
         items.pCode = new Hex( len );
         System.arraycopy( data, pos, items.pCode.getData(), 0, len );
         decryptObjcode( items.pCode );
       }
       else if ( tag.equals( "irdef" ) )
       {
         items.key = new Key();
         items.keys.add( items.key );
         pos++;
         // Convert irdef serial into a keycode by adding 0x80
         items.key.keycode = data[ pos++ ] | 0x80;  // CHANGE LATER TO ALLOW FOR 2-BYTE IRDEF SERIAL
         remote.getButtonName( items.key.keycode ); // for its side-effect of creating button
         items.key.btn = remote.getButton( items.key.keycode );
       }
       else if ( tag.equals( "macro" ) )
       {
         pos++;
         int ref = data[ pos ] + 0x100 * data[ pos + 1 ];
         for ( Macro macro : macros )
         {
           if ( macro.getSerial() == ref )
           {
             items.macro = macro;
             break;
           }
         }
         if ( items.macro == null )
         {
           for ( Activity activity : activities.values() )
           {
             if ( !activity.isActive() )
             {
               continue;
             }
             if ( activity.getMacro() != null &&  activity.getMacro().getSerial() == ref )
             {
               items.activity = activity;
               items.macro = activity.getMacro();
             }
           }
         }
         if ( items.macro == null )
         {
           System.err.println( "Macro with reference 0x" + Integer.toHexString( ref ) +" is unassigned" );
           Macro macro = new Macro( 0, null, null );
           macro.setSerial( ref );
           macros.add( macro );
           items.macro = macro;
         }
         DSMFunction sf = new DSMFunction( items.macro, null );
         specialFunctions.add( sf );
         items.sf = sf;
         items.macroKeys = new ArrayList< Integer >();
       }
       else if ( tag.equals( "issystemmacro"  ))
       {
         pos++;
         items.macro.setSystemMacro( data[ pos++ ] == 1 );
       }
       else if ( tag.equals( "name16" ) )
       {
         int len = data[ pos++ ];
         char[] ch = new char[ len / 2 ];
         for ( int i = 0; i < len; i += 2 )
         {
           ch[ i / 2 ] = ( char )( data[ pos + i ] + 0x100 * data[ pos + i + 1 ] );
         }
         items.macro.setName( new String( ch ) );
       }
       else if ( tag.equals( "sendhardkey" ) )
       {
         pos++;
         // device serial converted to keycode
         items.macroKeys.add( 0x50 + data[ pos++ ] );
         // keycode for that device
         items.macroKeys.add( ( int )data[ pos++ ] );
       }
       else if ( tag.equals( "sendsoftkey" ) )
       {
         pos++;
         // device serial converted to keycode
         items.macroKeys.add( 0x50 + data[ pos++ ] );
         int softIndex = data[ pos++ ];
         // keycode for that device
         items.macroKeys.add( ( int )( data[ pos++ ] + 6 * softIndex ) );
       }
       else if ( tag.equals(  "sendir" ) )
       {
         pos++;
         // device serial converted to keycode
         items.macroKeys.add( 0x50 + data[ pos++ ] );
         // constructed keycode for the function
         items.macroKeys.add( 0x80 + data[ pos++ ] );
       }
       else if ( tag.equals( "duration" ) )
       {
         pos++;
         int duration = ( data[ pos ] + 0x100 * data[ pos + 1 ] ) / 100;
         if ( duration == 0 )
         {
           // Use 0xFF to indicate an explicit zero duration
           duration = 0xFF;
         }
         List< Button > holdGroup = remote.getButtonGroups().get( "Hold" );
         if ( holdGroup != null && !holdGroup.isEmpty() )
         {
           int keycode = holdGroup.get( 0 ).getKeyCode() | ( duration << 8 );
           items.macroKeys.add( items.macroKeys.size() - 1 , keycode );
         }
       }
       else if ( tag.equals(  "delay" ) )
       {
         pos++;
         if ( fileIndex == 5 )
         {
           favPause = data[ pos++ ];
         }
         else
         {
           int keycode = items.macroKeys.remove( items.macroKeys.size() - 1 );
           keycode |= data[ pos++ ] << 8;
           items.macroKeys.add( keycode );
         }
       }
       else if ( tag.equals(  "assistant" ) )
       {
         if ( items.activity != null && items.activity.getAssists().isEmpty() )
         {
           for ( int i = 0; i < 3; i++ )
           {
             items.activity.getAssists().put( i, new ArrayList< Assister >() );
           }
         }
         else if ( items.sf != null )
         {
           for ( int i = 0; i < 3; i++ )
           {
             items.sf.getAssists().put( i, new ArrayList< Assister >() );
           }
         }
       }
       else if ( tag.equals(  "pictureinputs" ) )
       {
         items.assisters = items.activity != null ? 
             items.activity.getAssists().get( 0 ) :
               items.sf.getAssists().get( 0 );
       }
       else if ( tag.equals(  "audioinputs" ) )
       {
         items.assisters = items.activity != null ? 
             items.activity.getAssists().get( 1 ) :
               items.sf.getAssists().get( 1 );
       }
       else if ( tag.equals(  "powerkeys" ) )
       {
         items.assisters = items.activity != null ? 
             items.activity.getAssists().get( 2 ) :
               items.sf.getAssists().get( 2 );
       }
       else if ( tag.equals( "hardkey" ) )
       {
         pos++;
         // device serial converted to keycode
         DeviceButton dev = remote.getDeviceButton( 0x50 + data[ pos++ ] );
         // keycode for that device
         Button btn = remote.getButton( ( int )data[ pos++ ] );
         if ( dev != null && btn != null )
         {
           items.assisters.add(  new Assister( dev, btn ) );
         }
       }
       else if ( tag.equals( "softkey" ) )
       {
         pos++;
         // device serial converted to keycode
         DeviceButton dev = remote.getDeviceButton( 0x50 + data[ pos++ ] );
         // keycode for that device
         int softpage = data[ pos++ ];
         Button btn = remote.getButton( data[ pos++ ] + 6 * softpage );
         if ( dev != null && btn != null )
         {
           items.assisters.add(  new Assister( dev, btn ) );
         }
       }
       else if ( tag.equals(  "irref" ) )
       {
         pos++;
         // device serial converted to keycode
         DeviceButton dev = remote.getDeviceButton( 0x50 + data[ pos++ ] );
         // constructed keycode for the function
         Button btn = remote.getButton( 0x80 + data[ pos++ ] );
         if ( dev != null && btn != null )
         {
           items.assisters.add(  new Assister( dev, btn ) );
         }
       }
       else if ( tag.equals(  "punchthrumap" ) )
       {
         items.map = new LinkedHashMap< Button, KeySpec >();
       }
       else if ( tag.equals(  "punchthruspec" ) )
       {
         items.key = new Key();
         int offset = items.softPageIndex == null ? 0 : 6 * items.softPageIndex;
         pos++;
         DeviceButton dev = remote.getDeviceButton( data[ pos++ ] | 0x50 );
         Button target = remote.getButton( data[ pos++ ] + offset );
         Button source = remote.getButton( data[ pos++ ] + offset );
         items.key.btn = target;
         items.key.keycode = target.getKeyCode();
         if ( dev != null && target != null && source != null )
         {
           items.map.put( source, new KeySpec( dev, target ) );
         }
       }
       else if ( tag.equals(  "softpage" ) )
       {
         pos++;
         items.softPageIndex = ( int )data[ pos++ ];
       }
     }
     else // tag end
     {
       if ( tag.equals( "devices" ) )
       {
         for ( DeviceButton db : remote.getDeviceButtons() )
         {
           if ( !items.dbList.contains( db ) )
           {
             items.dbList.add( db );
           }
         }
         for ( int i = 0; i < items.dbList.size(); i++ )
         {
           remote.getDeviceButtons()[ i ] = items.dbList.get( i );
         }
       }
       else if ( tag.equals( "favorites" ) )
       {
         items.activity = null;
       }
       else if ( tag.equals( "device" ) )
       {
         items.db = null;
         items.key = null;
       }
       else if ( tag.equals( "favorite" ) )
       {
         items.fav = null;
         items.key = null;
       }
       else if ( tag.equals( "activity" ) )
       {
         items.activity = null;
         items.key = null;
       }
       else if ( tag.equals( "profile" ) )
       {
         items.activity = null;
         items.key = null;
       }
       if ( dbOnly )
       {
         return;
       }
       if ( tag.equals( "codeset" ) )
       {
         Hex hex = new Hex( items.pid, 0, items.fixedData.length() + 3 );
         hex.put( items.fixedData, 3 );
         hex.set( ( short )0x01, 2 );
         try
         { 
           items.upgrade.importRawUpgrade( hex, remote, items.alias, items.pid, items.pCode );
           items.upgrade.setSetupCode( items.setupCode );
           for ( Key key : items.keys )
           {
             if ( key.keygid == null && key.ls == null && key.macroref == null )
             {
               continue;
             }
             String name = key.fname;
             if ( name == null )
             {
               name = "__missing" + items.missingIndex++;
             }
             if ( key.ls != null )
             {
               key.ls.setName( name );
             }
             if ( key.keygid != null || key.macroref != null )
             {
               Function f = new Function( name );
               items.upgrade.getFunctions().add( f );
               if ( key.keygid != null )
               {
                 f.setHex( key.irdata );
                 f.setIndex( key.keygid );
                 f.setIconref( key.iconref );
                 f.setKeyflags( key.keyflags );
               }
               if ( key.macroref != null )
               {
                 f.setMacroref( key.macroref );
                 f.setKeyflags( key.keyflags );
               }
               if ( key.btn != null )
               {
                 items.upgrade.getAssignments().assign( key.btn, f );
               }
             }
           }
         }
         catch ( java.text.ParseException pe )
         {
           pe.printStackTrace( System.err );
         }
 //        items.pid = null;
 //        items.fixedData = null;
         items.missingIndex = 0;
         items.upgrade = null;
         items.keys = null;
         items.pCode = null;
       }
       else if ( tag.equals( "keydef" ) )
       {
         items.key = null;
       }
       else if ( tag.equals( "macro" ) )
       {
         int len = items.macroKeys.size();
         Hex hex = new Hex( len );
         for ( int i = 0; i < len; i++ )
         {
           hex.set( ( short )( int )items.macroKeys.get( i ) , i );
         }
         items.macro.setData( hex );
         for ( Macro macro : macros )
         {
           if ( macro == items.macro || macro.getSerial() != items.macro.getSerial() )
           {
             continue;
           }
           macro.setName( items.macro.getName() );
           macro.setData( new Hex( items.macro.getData() ) );
           macro.setSystemMacro( items.macro.isSystemMacro() );
         }        
         items.macroKeys = null;
         items.macro = null;
         items.sf = null;
         items.activity = null;
         items.assisters = null;
       }
       else if ( tag.equals( "macros" ) )
       {
         for ( Iterator< Macro > it = macros.iterator(); it.hasNext(); )
         {
           Macro macro = it.next();
           if ( !macro.isSystemMacro() )
           {
             continue;
           }
           SpecialProtocolFunction sf = null;
           for ( int i = 0; i < specialFunctions.size(); i++ )
           {
             SpecialProtocolFunction f = specialFunctions.get( i );
             if ( f.getMacro() != null && f.getMacro() == macro )
             {
               sf = f;
               break;
             }
           }
           KeyMove keyMove = null;
           int sourceKey = macro.getKeyCode();
           DeviceButton sourceDev = macro.getDeviceButton( this );
           short[] keys = macro.getData().getData();
           DeviceButton targetDev = remote.getDeviceButton( keys[ 0 ] );
           short[] targetData = targetDev.getSegment().getHex().getData();
           int targetKey = keys[ 1 ] & 0xFF;
           if ( targetKey == 0xFE )
           {
             targetKey = keys[ 2 ] & 0xFF;
           }
           if ( targetKey < 0x50 )
           {
             keyMove = new KeyMoveKey( sourceKey, sourceDev.getButtonIndex(), targetDev.getDeviceTypeIndex( targetData ), targetDev.getSetupCode( targetData ), targetKey, null );
           }
           else if ( targetKey >= 0x80 )
           {
             DeviceUpgrade du = targetDev.getUpgrade();
             Function f = du.getFunction( targetKey );
             keyMove = new KeyMove( sourceKey, sourceDev.getButtonIndex(), targetDev.getDeviceTypeIndex( targetData ), targetDev.getSetupCode( targetData ), f.getHex(), null );
             keyMove.setIrSerial( targetKey - 0x80 );
           }
           if ( keyMove != null )
           {
             keyMove.setName( macro.getName() );
             keyMove.setSerial( macro.getSerial() );
             keyMove.setTargetDevice( targetDev );
             it.remove();
             keymoves.add( keyMove );
             if ( sf != null )
             {
               sf.setMacro( null );
               sf.setKeyMove( keyMove );
             }
           }  
         }
         Collections.sort( macros, MacroSorter );
       }
       else if ( tag.equals( "activities" ) )
       {
         int i = 0;
         for ( Activity a : activities.values() )
         {
           if ( !items.activities.contains( a ) )
           {
             items.activities.add( a );
           }
         }
         activities.clear();
         for ( Activity a : items.activities )
         {
           activities.put( a.getButton(), a );
         }
       }
       else if ( tag.equals(  "punchthrumap" ) )
       {
         ActivityGroup[] groups = items.activity.getActivityGroups();
         for ( ActivityGroup group : groups )
         {
           Button[] buttonGroup = group.getButtonGroup();
           if ( buttonGroup != null && buttonGroup.length > 0 )
           {
             Button btn = buttonGroup[ 0 ];
             KeySpec ks = items.map.get( btn );
             group.setDevice( ks != null ? ks.dev : null );
           }
         }
         items.map = null;
       }
       else if ( tag.equals(  "punchthruspec" ) )
       {
         KeySpec ks = items.map.get( items.key.btn );
         DeviceUpgrade upg = ks.dev.getUpgrade();
         Function f = upg.getAssignments().getAssignment( items.key.btn );
         if ( f == null && items.key.fname != null )
         {
           f = new Function( items.key.fname );
           upg.getAssignments().assign( items.key.btn, f );
         }
         if ( f != null )
         {
           f.setIconref( items.key.iconref );
         }
       }
       else if ( tag.equals(  "softpage" ) )
       {
         items.softPageIndex = null;
       }
     }
   }
   
   private int parseXCFFile( Items items, int fileIndex, int fileStart, List< String > tagNames, boolean decode )
   {
     int pos = fileStart;
     List< Integer > tags = new ArrayList< Integer >();
     while ( true )
     {
       int tag = data[ pos++ ];
       if ( ( tag & 0x80 ) == 0 )
       {
         tags.add( 0, tag );
         decodeItem( items, fileIndex, tagNames.get( tag ), pos, true, !decode );
         int len = data[ pos++ ];
         pos += len;
       }
       else
       {
         int last = tags.remove( 0 );
         if ( tag != ( last | 0x80  ) )
         {
           System.err.println( "XCF file nesting error at " + Integer.toHexString( pos - 1 ) );
           break;
         }
         decodeItem( items, fileIndex, tagNames.get( tag & 0x7F ), pos, false, !decode );
         if ( tags.isEmpty() )
         {
           break;
         }
       }  
     }
     return pos;
   }
   
   private void decryptObjcode( Hex code )
   {
     for (  int i = 0; i < code.length(); i++ )
     {
       code.getData()[ i ] ^= ( short )encryptionKey[ i % 54 ];
     }
   }
   
   private Hex encryptObjcode( Hex code )
   {
     Hex hex = new Hex( code );
     for (  int i = 0; i < hex.length(); i++ )
     {
       hex.getData()[ i ] ^= ( short )encryptionKey[ i % 54 ];
     }
     return hex;
   }
 
   private void loadSegments( boolean decode )
   {
     // first two bytes are checksum, and in XSight remotes next 18 bytes are E2 info
     int pos = remote.usesEZRC() ? 20 : 2;
     int segLength = 0;
     int segType = 0;
     segmentLoadOrder.addAll( remote.getSegmentTypes() );
     while ( pos < remote.getEepromSize() && ( segLength = Hex.get( data, pos ) ) <= remote.getEepromSize() - pos  )
     {
       segType = data[ pos + 2 ];
       int segFlags = data[ pos + 3 ];
       Hex segData = new Hex( data, pos + 4, segLength - 4 );
       pos += segLength;
       if ( ( segFlags & 0x80 ) == 0 )
       {
         // Do not load segments flagged for deletion by having flag bit 7 clear
         continue;
       }
       if ( !segmentLoadOrder.contains( segType ) )
       {
         segmentLoadOrder.add( segType );
       }
       List< Segment > list = segments.get( segType );
       if ( list == null )
       {
         list = new ArrayList< Segment >();
       }
       list.add( new Segment( segType, segFlags, segData ) );
       segments.put( segType, list );
     }
 
     setDeviceButtonSegments();
     
     if ( !decode )
       return;
     segType = 1;
     List< Segment > macroList = segments.get( 1 );
     if ( macroList == null )
     {
       segType = 3;
       macroList = segments.get( 3 );
     }
     if ( macroList != null )
     {
       for ( Segment segment : macroList )
       {
         Hex hex = segment.getHex();
         int deviceIndex = hex.getData()[ 0 ]; // Known values are 0 (not device specific) or an activity button number
         Button btn = remote.getButton( deviceIndex );
         int keyCode = hex.getData()[ 1 ];
         int count = Math.min( hex.getData()[ 2 ], hex.length() - 3 );
         Hex data = hex.subHex( 3, count );
         Macro macro = new Macro( keyCode, data, deviceIndex, 0, null );
         macro.setSegmentFlags( segment.getFlags() );
         segment.setObject( macro );
         if ( segType == 1 && btn != null && activities != null && activities.get( btn ) != null )
         {
           activities.get( btn ).setMacro( macro );
         }
         else setMacro( macro );
         if ( segType == 3 )
         {
           short[] durations = hex.subHex( 3 + count, count ).getData();
           for ( int i = 0; i < count; i++ )
           {
             data.getData()[ i ] |= durations[ i ] << 8;
           }
           int len = hex.getData()[ 3 + 2 * count ];
           if ( hex.length() > 5 + 2 * count + len )
           {
             macro.setName( hex.subString( 4 + 2 * count, len ) );
             macro.setSerial( hex.get( 4 + 2 * count + len) );
           }
         }
       }
     }
     List< Segment > multiMacroList = segments.get( 2 );
     if ( multiMacroList != null )
     {
       for ( Segment segment : multiMacroList )
       {
         Hex hex = segment.getHex();
         int deviceIndex = hex.getData()[ 0 ]; // Only 0 (not device specific) yet seen for multimacro
         int keyCode = hex.getData()[ 1 ];
         pos = 3;
         for ( int i = 0; pos < hex.length() && i < hex.getData()[ 2 ]; i++ )
         {
           int length = hex.getData()[ pos ];
           Hex keyCodes = hex.subHex( pos + 1, Math.min( length, hex.length() - pos - 1 ) );
           Macro macro = new Macro( keyCode, keyCodes, deviceIndex, i + 1, null );
           macro.setSegmentFlags( segment.getFlags() );
           segment.setObject( macro );;
           setMacro( macro );
           pos += length + 1;
         }
       }
     }
     List< Segment > keyMoveKeyList = segments.get( 7 );
     if ( keyMoveKeyList != null )
     {
       for ( Segment segment : keyMoveKeyList )
       {
         Hex hex = segment.getHex();
         KeyMove keyMove = new KeyMoveKey( hex.getData()[ 1 ], hex.getData()[ 0 ], hex.getData()[ 2 ], hex.get( 3 ), hex.getData()[ 5 ], null );
         keyMove.setSegmentFlags( segment.getFlags() );
         segment.setObject( keyMove );
         keymoves.add( keyMove );
       }
     }
     List< Segment > keyMoveEFCList = segments.get( 8 );
     if ( keyMoveEFCList != null )
     {
       for ( Segment segment : keyMoveEFCList )
       {
         Hex hex = segment.getHex();
         if ( remote.getAdvCodeFormat() == AdvancedCode.Format.EFC )
         {
           KeyMove keyMove = new KeyMoveEFC5( hex.getData()[ 1 ], hex.getData()[ 0 ], hex.getData()[ 2 ], hex.get( 3 ), ( hex.getData()[ 5 ] << 16 ) + hex.get( 6 ), null );
           keyMove.setSegmentFlags( segment.getFlags() );
           segment.setObject( keyMove );
           keymoves.add( keyMove );
         }
         else
         {
           KeyMove keyMove = new KeyMove( hex.getData()[ 1 ], hex.getData()[ 0 ], hex.getData()[ 2 ], hex.get( 3 ), hex.subHex( 6, 2 ), null );
           keyMove.setSegmentFlags( segment.getFlags() );
           segment.setObject( keyMove );
           keymoves.add( keyMove );
         }
       }
     }
     List< Segment > learnedList = segments.get( 9 );
     if ( learnedList != null )
     {
       for ( Segment segment : learnedList )
       {
         Hex hex = segment.getHex();
         LearnedSignal ls = null;
         if ( remote.getProcessor().getEquivalentName().equals( "MAXQ610" ) )
         {
           // hex.getData[ 3 ] seems always to be 0 and is not stored in the learned signal
           ls = new LearnedSignal( hex.getData()[ 1 ], hex.getData()[ 0 ], 1, hex.subHex( 4, hex.getData()[ 2 ] - 1 ), null );
         }
         else
         {
           ls = new LearnedSignal( hex.getData()[ 1 ], hex.getData()[ 0 ], 0, hex.subHex( 2 ), null );
         }
         ls.setSegmentFlags( segment.getFlags() );
         learned.add( ls );
       }
     } 
     
     List< Segment > softNamesList = segments.get( 0x0A );
     if ( softNamesList != null )
     {
       for ( Segment segment : softNamesList )
       { 
         LinkedHashMap< Integer, String > nameMap = parseNameSegment( segment );
         DeviceButton devBtn = remote.getDeviceButton( segment.getHex().getData()[ 0 ] );
         if ( devBtn == null )
         {
           continue;
         }
         HashMap< Button, String > softButtonNames = devBtn.getSoftButtonNames();
         if ( softButtonNames == null )
         {
           softButtonNames = new HashMap< Button, String >();
         }
         for ( Integer key : nameMap.keySet() )
         {
           Button btn = remote.getButton( key );
           if ( btn != null )
           {
             softButtonNames.put( btn, nameMap.get( key ) );
           }        
         }
         devBtn.setSoftButtonNames( softButtonNames );
       }
     } 
     
     softNamesList = segments.get( 0x20 );
     if ( softNamesList != null )
     {
       for ( Segment segment : softNamesList )
       { 
         LinkedHashMap< Integer, String > nameMap = parseNameSegment( segment );
         DeviceButton devBtn = remote.getDeviceButton( segment.getHex().getData()[ 0 ] );
         if ( devBtn == null )
         {
           continue;
         }
 //        devBtn.setSoftFunctionSegment( segment );
         HashMap< Button, String > softFunctionNames = devBtn.getSoftFunctionNames();
         if ( softFunctionNames == null )
         {
           softFunctionNames = new HashMap< Button, String >();
         }
         for ( Integer key : nameMap.keySet() )
         {
           Button btn = remote.getButton( key );
           if ( btn != null )
           {
             softFunctionNames.put( btn, nameMap.get( key ) );
           }        
         }
         devBtn.setSoftFunctionNames( softFunctionNames );
       }
     }  
     
     List< Segment > upgradeList = segments.get( 0x10 );
     if ( upgradeList != null )
     {
       for ( Segment segment : upgradeList )
       {
         Hex hex = segment.getHex();
         int dev = hex.getData()[ 0 ];                       
         int protocolOffset = hex.get( 2 );
         DeviceType devType = remote.getDeviceTypeByIndex( hex.getData()[ 4 ] );
         int setupCode = hex.get( 5 );
         Hex pidHex = hex.subHex( 9, 2 );
         int limit = protocolOffset == 0 ? hex.length() : Math.min( hex.length(), protocolOffset + 4 );
         Hex deviceHex = hex.subHex( 9, limit - 9 );
         Hex protocolCode = null;
         if ( protocolOffset > 0 && protocolOffset < hex.length() - 4 )
         {
           protocolCode = hex.subHex( protocolOffset + 4 );
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
         DeviceUpgrade upgrade = new DeviceUpgrade();
         try
         {
           if ( dev != 0 )
           {
             DeviceButton devBtn = remote.getDeviceButton( dev );
             if ( devBtn != null )
             {
               upgrade.setButtonIndependent( false );
               upgrade.setButtonRestriction( devBtn );
               devBtn.setUpgrade( upgrade );
             }
           }
           upgrade.setRemoteConfig( this );
           upgrade.setSizeCmdBytes( hex.getData()[ 7 ] );
           upgrade.setSizeDevBytes( hex.getData()[ 8 ] );
           upgrade.importRawUpgrade( deviceHex, remote, alias, new Hex( pidHex ), protocolCode );
           upgrade.setSetupCode( setupCode );
           
           upgrade.setSegmentFlags( segment.getFlags() );
           Protocol protocol = upgrade.getProtocol();
           if ( upgrade.getSizeCmdBytes() != protocol.getDefaultCmd().length()
               || upgrade.getSizeDevBytes() != protocol.getFixedDataLength() )
           {
             String title = "Protocol Variant Error";
             String message = "Error in RDF.  Wrong variant specified for PID = " + 
               protocol.getID().toString() + ".  Number of fixed/command bytes\n" +
               "should be " + upgrade.getSizeDevBytes() + "/" + upgrade.getSizeCmdBytes() +
               ", for specified variant it is " + protocol.getDefaultCmd().length() +
               "/" + protocol.getFixedDataLength() + ".";
             JOptionPane.showMessageDialog( null, message, title, JOptionPane.WARNING_MESSAGE );
           }
         }
         catch ( java.text.ParseException pe )
         {
           pe.printStackTrace( System.err );
           upgrade = null;
         }
         segment.setObject( upgrade );
         devices.add( upgrade );
         if ( remote.usesEZRC() )
         {
           upgrade.classifyButtons();
         }
       }
     }
 
     if ( segments.get( 0x1D ) != null && segments.get( 0x1D ).size() > 0 )
     {
       Segment favDefinitions = segments.get( 0x1D ).get( 0 );
       Hex hex = favDefinitions.getHex();
       int deviceIndex = hex.getData()[ 0 ];
       int keyCode = hex.getData()[ 1 ];
       favPause = hex.getData()[ 3 ];
       favFinalKey = remote.getButton( hex.getData()[ 4 ] );
       int skip = ( favFinalKey == null ) ? 0 : 1;
       int numFavs = hex.getData()[ 6 ];
       int j = 0; 
       DeviceButton btn = remote.getDeviceButton( deviceIndex );
       favKeyDevButton = ( btn != null ) ? btn : null;
       for ( int i = 0; i < numFavs; i++ )
       {
         int count = hex.getData()[ 7 + j ];
         Hex data = hex.subHex( 8 + j, count - skip );
         j += 1 + count;
         FavScan favScan = new FavScan( keyCode, data, null );
         favScan.setSegmentFlags( favDefinitions.getFlags() );
         favScans.add( favScan ); 
       }
       j++;
       for ( int i = 0; i < numFavs; i++ )
       {
         int count = hex.getData()[ 7 + j ];
         favScans.get( i ).setName( hex.subString( 8 + j, count ) );
         j += 1 + count;
       }
     }
 
     if ( segments.get( 0x1E ) != null && segments.get( 0x1E ).size() > 0 )
     {
       Segment activityDefinitions = segments.get( 0x1E ).get( 0 );
       Hex hex = activityDefinitions.getHex();
       int numActivities = hex.getData()[ 2 ];
       int j = 0;
       for ( int i = 0; i < numActivities; i++ )
       {
         int deviceIndex = hex.getData()[ 3 + i ];
         Button btn = remote.getButton( deviceIndex );
         Activity activity = activities.get( btn );
         activity.setActive( true );
         activity.setSelector( btn );
         int count = hex.getData()[ 3 + numActivities + j ];
         Hex data = hex.subHex( 4 + numActivities + j, count );
         short[] durations = hex.subHex( 4 + numActivities + j + count, count ).getData();
         for ( int k = 0; k < count; k++ )
         {
           data.getData()[ k ] |= durations[ k ] << 8;
         }
         j += 1 + 2 * count;
         Macro macro = new Macro( deviceIndex, data, deviceIndex, 0, null );
         activity.setSegmentFlags( activityDefinitions.getFlags() );
         activity.setSegment( activityDefinitions );
         activity.setMacro( macro ); 
       }
       j++;
       for ( int i = 0; i < numActivities; i++ )
       {
         int deviceIndex = hex.getData()[ 3 + i ];
         Button btn = remote.getButton( deviceIndex );
         Activity activity = activities.get( btn );
         int count = hex.getData()[ 3 + numActivities + j ];
         activity.setName( hex.subString( 4 + numActivities + j, count ) );
         j += 1 + count;
       }
       for ( Activity activity : activities.values() )
       {
         activity.getMacro().setSegmentFlags( activityDefinitions.getFlags() );
         activity.getMacro().setSegment( activityDefinitions );
         activity.getMacro().setName( activity.getName() );
       }
     }
     
     List< Segment > activityAssists = segments.get( 0x1F );
     if ( activityAssists != null )
     {
       for ( Segment segment : activityAssists )
       {
         Hex hex = segment.getHex();
         Button btn = remote.getButton( hex.getData()[ 0 ] );
         Activity activity = activities.get( btn );
         if ( activity == null )
         {
           continue;
         }
         activity.setHelpSegmentFlags( segment.getFlags() );
         activity.setHelpSegment( segment );
         int j = 2;
         for ( int i = 0; i < 3; i++ )
         {
           int count = hex.getData()[ j++ ];
           if ( count == 0 )
           {
             continue;
           }
           List< Assister > assists = activity.getAssists().get( i );
           for ( int k = 0; k < count; k++ )
           {
             btn = remote.getButton( hex.getData()[ j + k ] );
             DeviceButton dev = remote.getDeviceButton( hex.getData()[ j + k + count ] );
             if ( btn != null && dev != null )
             {
               assists.add( new Assister( dev, btn ) );
             }
           }
           j += 2 * count + 1;
           for ( int k = 0; k < count; k++ )
           {
             j += hex.getData()[ j ] + 1;
           }
         }
       }
     }
     
     List< Segment > activityAssignments = segments.get( 0x0B );
     if ( activityAssignments != null )
     {
       for ( Segment segment : activityAssignments )
       {
         Hex hex = segment.getHex();
         Button btn = remote.getButton( hex.getData()[ 0 ] );
         Activity activity = activities.get( btn );
         if ( activity == null )
         {
           continue;
         }
         ActivityGroup[] groups = activity.getActivityGroups();
         int count = hex.getData()[ 2 ];
         LinkedHashMap< Button, DeviceButton > map = new LinkedHashMap< Button, DeviceButton >();
         for ( int i = 0; i < count; i++ )
         {
           btn = remote.getButton( hex.getData()[ 3 + i ] );
           DeviceButton dev = remote.getDeviceButton( hex.getData()[ 3 + i + count ] );
           map.put( btn, dev );
         }
         for ( ActivityGroup group : groups )
         {
           group.setSegment( segment );
           group.setSegmentFlags( segment.getFlags() );
           Button[] buttonGroup = group.getButtonGroup();
           if ( buttonGroup != null && buttonGroup.length > 0 )
           {
             btn = buttonGroup[ 0 ];
             group.setDevice( map.get( btn ) );
           }
         }
       }
     }
 
     activityAssignments = segments.get( 0xDB );
     if ( activityAssignments != null )
     {
       for ( Segment segment : activityAssignments )
       {
         Hex hex = segment.getHex();
         Button btn = remote.getButton( hex.getData()[ 1 ] );
         Activity activity = activities.get( btn );
         int tabIndex = remote.getButtonGroups().get( "Activity" ).indexOf( btn );
         activity.setSegmentFlags( segment.getFlags() );
         activity.setActive( true );
         ActivityGroup[] groups = activity.getActivityGroups();
         if ( remote.hasActivityControl() )
         {
           short val = hex.getData()[ 2 ];
           if ( val == 0xFF )
           {
             activity.setSelector( null );
             for ( ActivityGroup group : groups )
             {
               group.setDevice( DeviceButton.noButton );
             }
           }
           else
           {
             activity.setSelector( remote.getButton( val ) );
             val = hex.getData()[ 3 ];
             DeviceButton[] devBtns = remote.getDeviceButtons();
             for ( int i = 0; i < devBtns.length; i++ )
             {
               if ( ( ( val >> i ) & 1 ) == 1 )
               {
                 DeviceButton devBtn = devBtns[ i ];                
                 for ( int j = 0; j < groups.length; j++ )
                 {
                   List< DeviceButton > list = Arrays.asList( remote.getActivityControl()[ tabIndex ][ j ] );
                   if ( list.contains( devBtn ) && ( groups[ j ].getDevice() == null
                         || list.indexOf( devBtn ) > list.indexOf( groups[ j ].getDevice() ) ) )
                   {
                     groups[ j ].setDevice( devBtn );
                   }
                 }
               }
             }   
           }
         }
         else
         {
           for ( int i = 0; i < Math.min( groups.length, hex.length() - 2 ); i++ )
           {
             int index = hex.getData()[ i + 2 ];
             groups[ i ].setDevice( index == 0xFF ? DeviceButton.noButton : remote.getDeviceButton( hex.getData()[ i + 2 ] ) );
           }
         }
         segment.setObject( activity );
       }
     }
     List< Segment > activitySettings = segments.get( 0xDC );
     if ( activitySettings != null )
     {
       for ( Segment segment : activitySettings )
       {
         Hex hex = segment.getHex();
         Button btn = remote.getButton( hex.getData()[ 1 ] );
         Activity activity = activities.get( btn );
         activity.setHelpSegmentFlags( segment.getFlags() );
         activity.setAudioHelp( hex.getData()[ 2 ] & 1 );
         activity.setVideoHelp( hex.getData()[ 3 ] & 1 );
         activity.setHelpSegment( segment );
       }
     }
     pos = 0;
   }
   
   private LinkedHashMap< Integer, String > parseNameSegment( Segment segment )
   {
     LinkedHashMap< Integer, String > nameMap = new LinkedHashMap< Integer, String >();
     Hex hex = segment.getHex();
     int count = hex.getData()[ 2 ];
     int start = 4 + count;
     for ( int i = 0; i < count; i++ )
     {
       int nameLen = hex.getData()[ start ];
       nameMap.put( new Integer(hex.getData()[ i + 3 ]), hex.subString( start + 1, nameLen ) );
       start += nameLen + 1;
     }   
     return nameMap;
   }
   
   private Hex createNameHex( LinkedHashMap< Button, String > map )
   {
     int size = 4;
     for ( String str : map.values() )
     {
       size += 2 + str.length();
     }
     size += ( size & 1 ) == 1 ? 1 : 0;
     Hex hex = new Hex( size );
     hex.set( ( short )0xFF, size - 1 );
     hex.put( 0, 0 );
     hex.set( ( short )map.size(), 2 );
     int i = 3;
     int j = 4 + map.size();
     for ( Button btn : map.keySet() )
     {
       hex.set( btn.getKeyCode(), i++ );
       String name = map.get(  btn );
       hex.set( ( short )name.length(), j++ );
       for ( char ch : name.toCharArray() )
       {
         hex.set( ( short )ch, j++ );
       }
     }
     if ( !map.isEmpty() )
     {
       hex.set( ( short )0, i );
     }
     return hex;
   }
   
   /* 
    * For remotes with segments, adds macro to macros or specialFunctions as appropriate
    */
   private void setMacro( Macro macro )
   {
     Button btn = remote.getButton( macro.getDeviceIndex() );
     if ( btn != null && remote.getButtonGroups() != null
         && remote.getButtonGroups().get( "Device" ) != null      
         && remote.getButtonGroups().get( "Device" ).contains( btn ) )
     {
       // Device Specific Macro; needs RDF to include DSM=Internal:0
       for ( SpecialProtocol sp : remote.getSpecialProtocols() )
       {
         if ( sp.isInternal() && sp.getInternalSerial() == 0 )
         {
           SpecialProtocolFunction sf = sp.createFunction( macro );
           if ( sf != null )
           {
             specialFunctions.add( sf );
           }
           break;
         }
       }
     }          
     else 
     {
       macros.add( macro );
     }
   }
   
   public void setDeviceButtonSegments()
   {
     List< Segment > dbList = segments.get( 0 );
     if ( dbList != null )
     {
       for ( Segment segment : dbList )
       {
         Hex hex = segment.getHex();
         if ( segment.getFlags() == 0xFF )
         {
           DeviceButton db = remote.getDeviceButton( hex.getData()[ 0 ] );
           if ( db != null )
           {
             db.setDefaultName();
             db.setVolumePT( getPTButton( hex.getData()[ 6 ] ) );
             db.setTransportPT( getPTButton( hex.getData()[ 7 ] ) );
             db.setChannelPT( getPTButton( hex.getData()[ 8 ] ) );
             segment.setObject( db );
           }
         }
       }
     }
     
     List< Segment >devNamesList = segments.get( 0x15 );
     if ( devNamesList != null )
     {
       for ( Segment segment : devNamesList )
       {
         LinkedHashMap< Integer, String > nameMap = parseNameSegment( segment );
         for ( Integer key : nameMap.keySet() )
         {
           DeviceButton devBtn = remote.getDeviceButton( key );
           if ( devBtn != null )
           {
             devBtn.setName( nameMap.get( key ) );
           }
         }
       }
     }
   }
   
   private DeviceButton getPTButton( int deviceIndex )
   {
     DeviceButton devBtn = remote.getDeviceButton( deviceIndex );
     return devBtn == null ? DeviceButton.noButton : devBtn; 
   }
   
   public static Remote filterRemotes( List< Remote > remotes, String signature, int eepromSize, 
       short[] data, short[] sigData, boolean allowMismatch )
   { 
     Remote remote = null;
     
     // Filter on matching eeprom size
     for ( Iterator< Remote > it = remotes.iterator(); it.hasNext(); )
     {
       Remote r = it.next();
       if ( !( r.getEepromSize() == eepromSize
           || r.isSSD() && r.getEepromSize() > eepromSize ) )
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
       choices = FixedData.filter( remotes, data, sigData );
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
     // Ensure any properties of the remote left from previous configuration are removed
     Remote newRemote = new Remote( remote, remote.getNameIndex() );
     RemoteManager.getRemoteManager().replaceRemote( remote, newRemote );
     return newRemote;
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
     for ( LearnedSignal ls : learned )
     {
       DeviceButton devButton = remote.getDeviceButton( ls.getDeviceButtonIndex() );
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
   
   private LearnedSignal findLearnedSignal( DeviceButton db, Button btn )
   {
     for ( LearnedSignal ls : learned )
     {
       DeviceButton devButton = remote.getDeviceButton( ls.getDeviceButtonIndex() );
       if ( devButton != db )
       {
         continue;
       }
       
       if ( btn.getKeyCode() == ls.getKeyCode() )
       {
         return ls;
       }
     }
     System.err.println( "No learned signal found matching " + db.getName() + ':' + btn.getName() );
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
     
     int e2FormatOffset = remote.getProcessor().getE2FormatOffset();
     if ( e2FormatOffset >= 0 )
     {
       char[] val = new char[ 6 ];
       for ( int i = 0; i < 6; i++ )
       {
         val[ i ] = ( char )data[ e2FormatOffset + i ];
       }
       eepromFormatVersion = new String( val );               
     }
     
     if ( remote.isSSD() )
     {
       loadFiles( true );
 //      parseSystemFiles();
       return;
     }
     else if ( hasSegments() )
     {
       loadSegments( true );
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
   
   public void saveAltPIDs()
   {
     for ( DeviceUpgrade du : devices )
     {
       du.getProtocol().saveAltPID( remote );
     }
     for ( ProtocolUpgrade pu : protocols )
     {
       if ( pu.getProtocol() != null )
       {
         pu.getProtocol().saveAltPID( remote );
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
     
     if ( hasSegments() )
     {
       out.println( "[Signature]" );
       short[] sd = sigData;
       if ( sd == null )
       { 
         String sig = remote.getSignature();
         sd = new short[ sig.length() ];
         for ( int i = 0; i < sig.length(); i++ )
         {
           sd[ i ] = ( short )sig.charAt( i );
         }
       }
       Hex.print( out, sd, getSigAddress() );
       out.println();
       out.println( "[Buffer]" );
     }
 
     short[] dataToSave = RemoteMaster.useSavedData() ? savedData : data;
     Hex.print( out, Arrays.copyOf( dataToSave, getDataEnd( dataToSave ) ), remote.getBaseAddress() );
 
     out.println();
     out.println( "[Notes]" );
     // start with the overall notes
     if ( notes != null && !notes.trim().isEmpty() )
     {
       out.println( "$0000=" + exportNotes( notes ) );
     }
 
     if ( remote.isSSD() )
     {
       out.close();
       return;
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
     short[] data = hasSegments() ? deviceButton.getSegment().getHex().getData() : this.data;
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
     short[] data = this.data;
     DeviceButton[] deviceButtons = remote.getDeviceButtons();
     for ( int i = 0; i < deviceButtons.length; ++i )
     {
       DeviceButton deviceButton = deviceButtons[ i ];
       if ( hasSegments() )
       {
         data = deviceButton.getSegment().getHex().getData();
       }
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
     createActivities();
     SetupCode.setMax( remote.getSegmentTypes() == null ? remote.usesTwoBytePID() ? 4095 : 2047 : 0x7FFF );
 
     int eepromSize = remote.getEepromSize();
     data = new short[ eepromSize ];
     highlight = new Color[ eepromSize + 8 * remote.getSettingAddresses().size() ];
     for ( int i = 0; i < highlight.length; i++ )
     {
       highlight[ i ] = Color.WHITE;
     }
     deviceButtonNotes = new String[ remote.getDeviceButtons().length ];
     RemoteMaster.setSystemFilesItems( remote.isSSD() );
   }
 
   /**
    * Parses the data.
    * 
    * @throws IOException
    *           Signals that an I/O exception has occurred.
    */
   public void parseData() throws IOException
   {
     System.err.println( "Data parsing started" );
     importIR( null, true );
     System.err.println( "Data parsing ended" );
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
     System.err.println( "Decoding settings" );
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
     System.err.println( "Decoding advanced codes" );
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
     if ( remote.getAdvancedCodeAddress() == null )
     {
       return advCodes;
     }
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
         int segFlags = keyMove.getSegmentFlags();
         cmd = cmd.subHex( 0, 1 );
         keyMove = new KeyMoveLong( keyCode, keyMove.getDeviceButtonIndex(), keyMove.getDeviceType(), keyMove
             .getSetupCode(), cmd, keyMove.getNotes() );
         keyMove.setSegmentFlags( segFlags );
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
       DeviceButton boundDeviceButton = remote.getDeviceButton(keyMove.getDeviceButtonIndex() );
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
     short[] data = this.data;
     for ( int i = 0; i < deviceButtons.length; ++i )
     {
       DeviceButton button = deviceButtons[ i ];
       if ( hasSegments() )
       {
         if ( button.getSegment() == null )
         {
           continue;
         }
         data = button.getSegment().getHex().getData();
       }
       if ( button.getDeviceTypeIndex( data ) == upgrade.getDeviceType().getNumber()
           && button.getSetupCode( data ) == upgrade.getSetupCode() )
       {
         dbList.add( button.getButtonIndex() );
       }
     }
     return dbList;
   }
 
   public DeviceUpgrade getAssignedDeviceUpgrade( DeviceButton deviceButton )
   {
     short[] data = this.data;
     if ( hasSegments() )
     {
       data = deviceButton.getSegment().getHex().getData();
     }
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
     if ( remote.isSSD() )
     {
       // XSight remotes
       ssdFiles.remove( "home.xcf" );
       ssdFiles.remove( "devices.xcf" );
       ssdFiles.remove( "activities.xcf" );
       ssdFiles.remove( "profiles.xcf" );
       ssdFiles.remove( "favorites.xcf" );
       ssdFiles.remove( "macros.xcf" );
       SSDFile file = makeDevicesXCF();
       if ( file != null )
       {
         ssdFiles.put( "devices.xcf", file );
       }
       file = makeActivitiesXCF();
       if ( file != null )
       {
         ssdFiles.put( "activities.xcf", file );
       }
       file = makeProfilesXCF();
       if ( file != null )
       {
         ssdFiles.put( "profiles.xcf", file );
       }
       file = makeFavoritesXCF();
       if ( file != null )
       {
         ssdFiles.put( "favorites.xcf", file );
       }
       file = makeMacrosXCF();
       if ( file != null )
       {
         ssdFiles.put( "macros.xcf", file );
       }
       file = makeHomeXCF();
       ssdFiles.put( "home.xcf", file );
       
       int pos = 4;
       int status = 0;
       Arrays.fill( data, ( short )0xFF );
       for ( int n = 0; n < Remote.userFilenames.length; n++ )
       {
         String name = Remote.userFilenames[ n ];
         file = ssdFiles.get( name );
         if ( file == null )
         {
           continue;
         }
         status |= 1 << n;
         Hex hex = name.endsWith( ".xcf" ) ? makeBXMLFile( file ) : file.hex;
         System.arraycopy( hex.getData(), 0, data, pos, hex.length() );
         pos += hex.length();      
       }
       data[ 0 ] = ( short )( status & 0xFF );
       data[ 1 ] = ( short )( ( ( status >> 8 ) & 0x0F ) | ( ( pos >> 12 ) & 0xF0 ) );
       data[ 2 ] = ( short )( pos & 0xFF );
       data[ 3 ] = ( short )( ( pos >> 8 ) & 0xFF );
       return;
     }
     
     // All other remotes
     
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
     
     if ( hasSegments() )
     {
       updateActivities();
       updateFavorites();
       int pos = 2;
       int e2Offset = remote.getProcessor().getE2FormatOffset();
       if ( e2Offset >= 0 )
       {
         Arrays.fill( data, 0, 20, ( short )0xFF );
         int size = remote.getEepromSize();
         for ( int i = 24; i >= 0; i -= 8 )
         {
           data[ pos++ ] = ( short )( ( size >> i ) & 0xFF );
         }
         for ( int i = 0; i < 6; i++ )
         {
           data[ pos++ ] = ( short )( eepromFormatVersion == null ? '-' : eepromFormatVersion.charAt( i ) );
         }
         pos = 20;
       }
       for ( int type : segmentLoadOrder )
       {
         List< Segment > list = segments.get( type );
         if ( list != null )
         {
           for ( Segment segment : list )
           {
             Hex hex = segment.getHex();
             Hex.put( hex.length() + 4, data, pos );
             data[ pos + 2 ] = ( short )type;
             data[ pos + 3 ] = ( short )segment.getFlags();
             Hex.put( hex, data, pos + 4 );
             segment.setAddress( pos );
             pos += hex.length() + 4;
           }
         }
       }
       Hex.put( 0xFFFF, data, pos );
       remote.getUsageRange().setFreeStart( pos + 2 );
       
       updateKeyMoveHighlights( keymoves );
       updateKeyMoveHighlights( upgradeKeyMoves );
       updateMacroHighlights();
       updateUpgradeHighlights();
       updateLearnedHighlights();
       if ( remote.usesEZRC() )
       {
         updateActivityHighlightsEZRC();
         updateFavoritesHighlights();
       }
       else
       {
         updateActivityHighlights();
         remote.getCheckSums()[ 0 ].getAddressRange().setEnd( pos - 1 );
       }
     }
     updateCheckSums();
     checkImageForByteOverflows();
   }
 
   private void updateKeyMoveHighlights( List< KeyMove > keyMoves )
   {
     for ( KeyMove keyMove : keyMoves )
     {
       Segment segment = keyMove.getSegment();
       if ( segment == null )
       {
         return;
       }
       int segType = segment.getType();
       int address = segment.getAddress();
       updateHighlight( keyMove, address + 4, segType == 7 ? 6 : 8 );
     }
   }
   
   private void updateMacroHighlights()
   {
     List< Macro > allMacros = new ArrayList< Macro >();
     allMacros.addAll( getAllMacros( false ) );
     allMacros.addAll( specialFunctionMacros );
     for ( Macro macro : allMacros )
     {
       Segment segment = macro.getSegment();
       if ( segment == null )
       {
         return;
       }
       int index = macro.getIndex();
       int segType = segment.getType();
       int address = segment.getAddress();
       short[] segData = segment.getHex().getData();
       if ( segType == 1 )
       {
         updateHighlight( macro, address + 4, segData[ 2 ] + 3 );
       }
       else if ( segType == 2 )
       {
         int pos = 3;
         for ( int i = 0; i < index; i++ )
         {
           pos += segData[ pos ] + 1;
         }
         updateHighlight( macro, address + pos + 4, segData[ pos ] + 1 );
         if ( index == 0 )
         {
           updateHighlight( macro, address + pos + 1, 2 );
         }
       }
       else if ( segType == 3 )
       {
         int len = 2 * segData[ 2 ]; // keys and durations
         len += segData[ 3 + len ] + 2;  // name and serial
         updateHighlight( macro, address + 4, len + 4 );
       }
     }
   }
   
   private void updateUpgradeHighlights()
   {
     for ( DeviceUpgrade dev : devices )
     {
       Segment segment = dev.getSegment();
       if ( segment == null )
       {
         return;
       }
       int address = segment.getAddress();
       int protOffset = segment.getHex().get( 2 );
       int segSize = segment.getHex().length();
       int devSize = ( protOffset == 0 ) ? segSize - 4 : protOffset;
       int protSize = ( protOffset == 0 ) ? 0 : segSize - protOffset - 4;
       updateHighlight( dev, address + 8, devSize );
       for ( int i = 0; i < protSize; i++ )
       {
         highlight[ address + protOffset + 8 + i ] = dev.getProtocolHighlight();
       }
       segment = dev.getSoftButtonSegment();
       if ( segment != null )
       {
         address = segment.getAddress();
         updateHighlight( dev, address + 4, segment.getHex().length() );
       }
       segment = dev.getSoftFunctionSegment();
       if ( segment != null )
       {
         address = segment.getAddress();
         updateHighlight( dev, address + 4, segment.getHex().length() );
       }
     }
   }
   
   private void updateLearnedHighlights()
   {
     for ( LearnedSignal ls : learned )
     {
       Segment segment = ls.getSegment();
       if ( segment == null )
       {
         return;
       }
       int address = segment.getAddress();
       int segSize = segment.getHex().length();
       updateHighlight( ls, address + 4, segSize );  
     }
   }
   
   private void updateActivityHighlights()
   {
     if ( activities == null )
     {
       return;
     }
     for ( Activity activity : activities.values() )
     {
       ActivityGroup[] groups = activity.getActivityGroups();
       Segment segment = activity.getSegment();
       int address = segment.getAddress();
       int incr = ( remote.hasActivityControl() ) ? 0 : 1;
       int pos = 1 - incr;
       for ( ActivityGroup group : groups )
       {
         group.clearMemoryUsage();
         updateHighlight( group, address + 6 + pos, 1 );
         pos += incr;
       }
       if ( remote.hasActivityControl() )
       {
         updateHighlight( activity, address + 6, 1 );
       }
       segment = activity.getHelpSegment();
       address = segment.getAddress();
       activity.clearMemoryUsage();
       updateHighlight( activity, address + 4, 4 );
       if ( remote.hasActivityControl() )
       {
         activity.addMemoryUsage( 1 );
       }
       Macro macro = activity.getMacro();
       if ( macro != null )
       {
         activity.addMemoryUsage( activity.getMacro().getMemoryUsage() );
       }
     }
   }
   
   private void updateActivityHighlightsEZRC()
   {
     if ( activities == null )
     {
       return;
     }
     for ( Activity activity : activities.values() )
     {
       if ( !activity.isActive() && ( favScans.size() == 0 || remote.getFavKey() == null 
           || activity.getButton().getKeyCode() != remote.getFavKey().getKeyCode() ) )
       {
         continue;
       }
       ActivityGroup[] groups = activity.getActivityGroups();
       if ( groups.length > 0 )
       {
         Segment segment = groups[ 0 ].getSegment();
         List< Short > keyCodes = new ArrayList< Short >();
         short[] segData = segment.getHex().getData();
         int len = segData[ 2 ];
         for ( int i = 0; i < len; i++ )
         {
           keyCodes.add( segData[ 3 + i ] );
         }
         int address = segment.getAddress();
         for ( ActivityGroup group : groups )
         {
           group.clearMemoryUsage();
           for ( Button btn : group.getButtonGroup() )
           {
             int pos = keyCodes.indexOf( btn.getKeyCode() );
             if ( pos >= 0 )
             {
               updateHighlight( group, address + 7 + pos, 1 );
               updateHighlight( group, address + len + 7 + pos, 1 );
             }
           }
           Segment softSegment = group.getSoftNamesSegment();
           if ( softSegment != null )
           {
             int softAddress = softSegment.getAddress();
             int softLen = softSegment.getHex().length();
             if ( softSegment.getHex().getData()[ softLen - 1 ] == 0xFF )
             {
               softLen--;
             }
             updateHighlight( group, softAddress + 4, softLen );
           }
         }
       }
 
       Segment segment = activity.getSegment();
       if ( segment == null )
       {
         continue;
       }
       int address = segment.getAddress();
       short[] segData = segment.getHex().getData();
       int len = segData[ 2 ];
       int index = 0;
       while ( index < len && segData[ 3 + index ] != activity.getButton().getKeyCode() )
       {
         index++;
       }
       if ( index < len )
       {
         updateHighlight( activity, address + 7 + index, 1 );
         int mPos = 3 + len;
         int nPos = mPos;
         for ( int i = 0; i < len; i++ )
         {
           int incr = 2 * segData[ nPos ] + 1;
           nPos += incr;
           if ( i < index )
           {
             mPos += incr;
           }
         }
         nPos++;
         for ( int i = 0; i < index; i++ )
         {
           nPos += segData[ nPos ] + 1;
         }
         updateHighlight( activity, address + 4 + mPos, 2 * segData[ mPos ] + 1 );
         updateHighlight( activity, address + 4 + nPos, segData[ nPos ] + 1 );
       }
       
       segment = activity.getHelpSegment();
       if ( segment != null )
       {
         address = segment.getAddress();
         updateHighlight( activity, address + 4, segment.getHex().length() );
       }
     }
   }
   
   private void updateFavoritesHighlights()
   {
     int count = favScans.size();
     if ( count == 0 )
     {
       return;
     }
     Segment segment = favScans.get( 0 ).getSegment();
     int address = segment.getAddress();
     short[] segData = segment.getHex().getData();
     int mPos = 7;
     int nPos = 7;
     for ( int i = 0; i < count; i++ )
     {
       nPos += segData[ nPos ] + 1;
     }
     nPos++;
     for ( int i = 0; i < count; i++ )
     {
       FavScan fav = favScans.get( i );
       fav.clearMemoryUsage();
       updateHighlight( fav, address + 4 + mPos, segData[ mPos ] + 1 );
       updateHighlight( fav, address + 4 + nPos, segData[ nPos ] + 1 );
       mPos += segData[ mPos ] + 1;
       nPos += segData[ nPos ] + 1;
     }
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
       keyMove.clearMemoryUsage();
       
       if ( !hasSegments() )
       {
         updateHighlight( keyMove, offset, keyMove.getSize( remote ) );
         offset = keyMove.store( data, offset, remote );
       }
       else
       {
         int type = ( keyMove instanceof KeyMoveKey ) ? 7 : 8;
         int flags = keyMove.getSegmentFlags();
         Hex segData = new Hex( type == 7 ? 6 : 8 );
         if ( !remote.getSegmentTypes().contains( type ) )
         {
           continue;
         }
         if ( segments.get( type ) == null )
         {
           segments.put(  type, new ArrayList< Segment >() );
         }
         keyMove.store( segData.getData(), offset, remote );
         segments.get( type ).add( new Segment( type, flags, segData, keyMove ) );
       }
     }
     return offset;
   }
   
   private void updateFavorites()
   {
     List< Integer > types = remote.getSegmentTypes();
     int count = favScans.size();
     if ( !types.contains( 0x1D ) || count == 0 )
     {
       return;
     }
     segments.remove( 0x1D );
     int dataLen = 8;
     int k = 7;
     int flags = 0;
     int maxSize = 0;
     for ( FavScan fav : favScans )
     {
       int size = fav.getData().length();
       if ( size > maxSize )
       {
         maxSize = size;
       }
       k += size + 1;
       dataLen += size + fav.getName().length() + 2;
       flags = fav.getSegmentFlags();
     }
     if ( favFinalKey != null )
     {
       k += count;
       dataLen += count;
 //      maxSize -= 1;
     }
     dataLen += remote.doForceEvenStarts() && ( dataLen & 1 ) == 1 ? 1 : 0;
     Hex segData = new Hex( dataLen );
     segData.set( ( short )0xFF, dataLen - 1 );
 
     if ( segments.get( 0x1D ) == null )
     {
       segments.put(  0x1D, new ArrayList< Segment >() );
     }
     Segment segment = new Segment( 0x1D, flags, segData );
 
     segData.set( ( short )favKeyDevButton.getButtonIndex(), 0 );
     segData.set( ( short )remote.getFavKey().getKeyCode(), 1 );
     segData.set( ( short )maxSize, 2 );
     segData.set( ( short )favPause, 3 );
     segData.set( ( short )( favFinalKey == null ? 0xFF : favFinalKey.getKeyCode() ), 4 );
     segData.set( ( short )0xFF, 5 );
     segData.set( ( short )count, 6 );
     segData.set( ( short )0, k++ );
     int i = 7;
     for ( FavScan fav : favScans )
     {
       int len = fav.getData().length();
       segData.put( fav.getData().getData(), i + 1 );
       if ( favFinalKey != null )
       {
         len++;
         segData.set( favFinalKey.getKeyCode(), i + len );
       }
       segData.set( ( short )len, i );
       i += len + 1;
       String name = fav.getName();
       segData.set( ( short )name.length(), k++ );
       for ( int n = 0; n < name.length(); n++ )
       {
         segData.set( ( short )name.charAt( n ), k++ );
       }
       fav.setSegment( segment );
     }
     segments.get( 0x1D ).add( segment );
     Button btn = remote.getButton( remote.getFavKey().getKeyCode() );
     updateActivityData( activities.get( btn ), true );
   }
   
   private void updateActivities()
   {
     if ( activities == null )
     {
       return;
     }
     List< Integer > types = remote.getSegmentTypes();
     if ( types.contains( 0xDB ) )
     {
       segments.remove( 0xDB );
       segments.remove( 0xDC );
       for ( Activity activity : activities.values() )
       {
         Button btn = activity.getButton();
         ActivityGroup[] groups = activity.getActivityGroups();
         int dataLen = remote.hasActivityControl() ? 4 : groups.length + 2;
         if ( remote.doForceEvenStarts() && ( dataLen & 1 ) == 1 )
         {
           dataLen++;
         }
         Hex segData = new Hex( dataLen );
         segData.set( ( short )0, 0 );
         segData.set( btn.getKeyCode(), 1 );
         int pos = 2;
         if ( remote.hasActivityControl() )
         {
           if ( activity.getSelector() == null )
           {
             segData.put( 0xFF00, pos );
           }
           else
           {
             segData.set( activity.getSelector().getKeyCode(), pos++ );
             short val = 0;
             for ( ActivityGroup group : groups )
             {
               int index = Arrays.asList( remote.getDeviceButtons() ).indexOf( group.getDevice() );
               if ( index >= 0 )
               {
                 val |= 1 << index;
               }
             }
             segData.set( val, pos++ );
           }
         }
         else
         {
           for ( ActivityGroup group : groups )
           {
             segData.set( ( short )group.getDeviceIndex(), pos++ );
           }
         }
 
         int flags = activity.getSegmentFlags();
         if ( segments.get( 0xDB ) == null )
         {
           segments.put(  0xDB, new ArrayList< Segment >() );
         }
         segments.get( 0xDB ).add( new Segment( 0xDB, flags, segData, activity ) );
         segData = new Hex( 4 );
         segData.set( ( short )0, 0 );
         segData.set( ( short )activity.getButton().getKeyCode(), 1 );
         segData.set( ( short )activity.getAudioHelp(), 2 );
         segData.set( ( short )activity.getVideoHelp(), 3 );
         flags = activity.getHelpSegmentFlags();
         if ( segments.get( 0xDC ) == null )
         {
           segments.put(  0xDC, new ArrayList< Segment >() );
         }
         Segment segment = new Segment( 0xDC, flags, segData );
         activity.setHelpSegment( segment );
         segments.get( 0xDC ).add( segment );
       }
     }
     else if ( types.contains( 0x1E ) )
     {
       Button act = remote.getButtonByStandardName( "Activities" );
       if ( act == null )
       {
         return;
       }
       segments.remove( 0x1E );
       segments.remove( 0x1F );
       segments.remove( 0x0B );
       List< Button > btns = new ArrayList< Button >();
       List< Activity > activeActivities = new ArrayList< Activity >();
       int dataLen = 4;
       int k = 3;
       int flags = 0;
       for ( Activity activity : activities.values() )
       {
         if ( activity.isActive() )
         {
           activeActivities.add( activity );
         }
       }
       Collections.sort( activeActivities, Activity.activitySort );
       for ( Activity activity : activeActivities )
       {
         btns.add( activity.getButton() );
         Macro macro = activity.getMacro();
         int size = ( macro == null ) ? 2 : 2 * macro.getData().length() + 2;
         k += size;
         dataLen += size + activity.getName().length() + 1;
         flags = activity.getSegmentFlags();
         updateActivityData( activity, false );
       }
       dataLen += remote.doForceEvenStarts() && ( dataLen & 1 ) == 1 ? 1 : 0;
       Hex segData = new Hex( dataLen );
 
       if ( segments.get( 0x1E ) == null )
       {
         segments.put(  0x1E, new ArrayList< Segment >() );
       }
       Segment segment = new Segment( 0x1E, flags, segData );
 
       segData.set( ( short )0, 0 );
       segData.set( act.getKeyCode(), 1 );
       segData.set( ( short )btns.size(), 2 );
       segData.set( ( short )0, k++ );
       segData.set( ( short )0xFF, dataLen - 1 );
       int i = 3;
       int j = 3 + btns.size();
       for ( Button btn : btns )
       {
         Activity activity = activities.get( btn );
         activity.setSegment( segment );
         activity.getMacro().setSegment( segment );
         short[] macroData = activity.getMacro().getData().getData();
         int macroLen = macroData.length;
         segData.set( activity.getSelector().getKeyCode(), i++ );
         segData.set( ( short )macroLen, j++ );
         for ( int n = 0; n < macroLen; n++ )
         {
           segData.set( ( short )( macroData[ n ] & 0xFF ), j + n );
           segData.set( ( short )( ( macroData[ n ] >> 8 ) & 0xFF ), j + n + macroLen );
         }
         j += 2 * macroLen;
         String name = activity.getName();
         segData.set( ( short )name.length(), k++ );
         for ( int n = 0; n < name.length(); n++ )
         {
           segData.set( ( short )name.charAt( n ), k++ );
         }
       }
       
       segments.get( 0x1E ).add( segment );
     }
   }
   
   private void updateActivityData( Activity activity, boolean assignmentsOnly )
   {   
     // First do button assignments, segment type 0x0B
     int count = 0;
     ActivityGroup[] groups = activity.getActivityGroups();
     for ( ActivityGroup group : groups )
     {
       count += group.getButtonGroup().length;
     }
     int dataLen = 2 * count + 4;
     Hex segData = new Hex( dataLen );
     segData.set( ( short )0xFF, dataLen - 1 );
 
     if ( segments.get( 0x0B ) == null )
     {
       segments.put(  0x0B, new ArrayList< Segment >() );
     }
     
     Segment segment = new Segment( 0x0B, groups[ 0 ].getSegmentFlags(), segData );
     int pos = 0;
     segData.set( activity.getButton().getKeyCode(), pos++ );
     segData.set( ( short )0, pos++ );
     segData.set( ( short )count, pos++ );
     for ( ActivityGroup group : groups )
     {
       group.setSegment( segment );
       for ( Button btn : group.getButtonGroup() )
       {
         segData.set( ( short )group.getDeviceIndex(), pos + count );
         segData.set( btn.getKeyCode(), pos++ );
       }
     }
     segments.get( 0x0B ).add( segment );
     
     if ( assignmentsOnly )
     {
       return;
     }
     
     // Next do soft button names, segment type 0x0A
     LinkedHashMap< Button, String > map = new LinkedHashMap< Button, String >();
     if ( segments.get( 0x0A ) == null )
     {
       segments.put(  0x0A, new ArrayList< Segment >() );
     }
     segment = new Segment( 0x0A, groups[ 0 ].getSegmentFlags(), null );
     segments.get( 0x0A ).add( segment );
     
     for ( ActivityGroup group : groups )
     {
       DeviceButton db = group.getDevice();
       DeviceUpgrade du = db.getUpgrade();
       if ( du == null || du.getSoftButtons() == null || du.getSoftButtons().isEmpty() 
           || !remote.isSoftButton( group.getButtonGroup()[ 0 ] ) )
       {
         continue;
       }
       for ( Button btn : group.getButtonGroup() )
       {
         if ( !du.getSoftButtons().contains( btn ) )
         {
           continue;
         }
         map.put( btn, du.getFunction( btn.getKeyCode() ).getName() );
       }
       group.setSoftNamesSegment( segment );
     }
     segData = createNameHex( map );
     segData.set( ( short )activity.getButton().getKeyCode(), 0 );
     segment.setHex( segData );
     
     // Finally do Activity Assists, segment type 0x1F
     dataLen = 5;
     for ( int i = 0; i < 3; i++ )
     {
       List< Assister > a = activity.getAssists().get( i );
       if ( a.size() > 0 )
       {
         dataLen += 3 * a.size() + 1;
         for ( Assister assist : a )
         {
           dataLen += assist.getDeviceName().length();
         }
       }
     }
     dataLen += remote.doForceEvenStarts() && ( dataLen & 1 ) == 1 ? 1 : 0;
     segData = new Hex( dataLen );
     segData.set( ( short )0xFF, dataLen - 1 );
 
     if ( segments.get( 0x1F ) == null )
     {
       segments.put(  0x1F, new ArrayList< Segment >() );
     }
     segment = new Segment( 0x1F, activity.getHelpSegmentFlags(), segData );
     pos = 0;
     segData.set( activity.getSelector().getKeyCode(), pos++ );
     segData.set( ( short )0, pos++ );
     for ( int i = 0; i < 3; i++ )
     {
       List< Assister > a = activity.getAssists().get( i );
       int aLen = a.size();
       segData.set( ( short )aLen, pos++ );
       if ( aLen > 0 )
       {
         segData.set( ( short )0, pos + 2 * aLen );
         int namePos = pos + 2 * aLen + 1;
         for ( int j = 0; j < aLen; j++ )
         {
           segData.set( a.get( j ).button.getKeyCode(), pos + j );
           segData.set( ( short )a.get( j ).device.getButtonIndex(), pos + j + aLen );
           int nameLen = a.get( j ).device.getName().length();
           segData.set( ( short )nameLen, namePos++ );
           for ( int k = 0; k < nameLen; k++ )
           {
             segData.set( ( short )a.get( j ).device.getName().charAt( k ), namePos++ );
           }
         }
         pos = namePos;
       }
     }
     activity.setHelpSegment( segment );
     segments.get( 0x1F ).add( segment );
 
   }
 
   public List< KeyMove > getUpgradeKeyMoves()
   {
     return upgradeKeyMoves;
   }
 
   public void setUpgradeKeyMoves()
   {
     upgradeKeyMoves.clear();
     for ( DeviceUpgrade device : devices )
     {
       for ( Integer dbIndex : getDeviceButtonIndexList( device ) )
       {
         for ( KeyMove keyMove : device.getKeyMoves( dbIndex ) )
         {
           keyMove.setDeviceButtonIndex( dbIndex );
           upgradeKeyMoves.add( keyMove );
         }
       }
     }
   }
 
   private void updateFavScans()
   {
     if ( hasSegments() || !remote.hasFavKey() || !remote.getFavKey().isSegregated() )
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
     favScan.clearMemoryUsage();
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
       timedMacro.clearMemoryUsage();
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
     int offset = 0;
     AddressRange range = remote.getAdvancedCodeAddress();
     List< Macro > allMacros = new ArrayList< Macro >();
     allMacros.addAll( getAllMacros( false ) );
     
     if ( hasSegments() && !remote.isSSD() )
     {
       List< Integer > types = remote.getSegmentTypes();
       if ( types.contains( 1 ) ) segments.remove( 1 );
       if ( types.contains( 2 ) ) segments.remove( 2 );
       if ( types.contains( 3 ) ) segments.remove( 3 );
       if ( types.contains( 7 ) ) segments.remove( 7 );
       if ( types.contains( 8 ) ) segments.remove( 8 );
       setUpgradeKeyMoves();
       updateKeyMoves( keymoves, 0 );
       updateKeyMoves( upgradeKeyMoves, 0 );
       updateSpecialFunctionSublists();
       allMacros.addAll( specialFunctionMacros );
     }
     else if ( range != null )
     {
       offset = range.getStart();
       updateSpecialFunctionSublists();
       offset = updateKeyMoves( keymoves, offset );
       setUpgradeKeyMoves();
       offset = updateKeyMoves( upgradeKeyMoves, offset );
       offset = updateKeyMoves( specialFunctionKeyMoves, offset );
     }
     else
     {
       return;
     }
     
     LinkedHashMap< Button, List< Macro >> multiMacros = new LinkedHashMap< Button, List< Macro >>();
     LinkedHashMap< Integer, List< Macro >> macroLists = new LinkedHashMap< Integer, List< Macro >>();
     for ( Macro macro : allMacros )
     {
       macro.clearMemoryUsage();
       int keyCode = macro.getKeyCode();
       Button button = remote.getButton( keyCode );
       MultiMacro multiMacro = button != null ? button.getMultiMacro() : null;
       if ( multiMacro != null )
       {
         List< Macro > list = multiMacros.get( button );
         if ( list == null )
         {
           list = new ArrayList< Macro >();
           multiMacros.put( button, list );
         }
         list.add( macro );
         macro.setSequenceNumber( hasSegments() ? 0 : list.size() );
       }
       else if ( hasSegments() )
       {
         List< Macro > list = macroLists.get( keyCode );
         if ( list == null )
         {
           list = new ArrayList< Macro >();
           macroLists.put( keyCode, list );
         }
         list.add( macro );
         macro.setSequenceNumber( 0 );
       }
       if ( !hasSegments() )
       {
         updateHighlight( macro, offset, macro.getSize( remote ) );
         offset = macro.store( data, offset, remote );
       }
     }
     
     if ( hasSegments() )
     {
       List< Integer > segmentTypes = remote.getSegmentTypes();
       for ( int keyCode : macroLists.keySet() )
       {
         List< Macro > list = macroLists.get(  keyCode );
         // Segment type depends on list and the remote, so mark as
         // undetermined by setting value 0.
         updateMacroSegments( keyCode, list, 0 );  
       }
 
       if ( segmentTypes.contains( 2 ) )
       {
         for ( Button btn : multiMacros.keySet() )
         {
           int keyCode = btn.getKeyCode();
           List< Macro > list = multiMacros.get(  btn );
           updateMacroSegments( keyCode, list, 2 );
         }
       }
       return;
     }
     
     for ( Macro macro : specialFunctionMacros )
     {
       macro.clearMemoryUsage();
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
         favScan.clearMemoryUsage();
         updateHighlight( favScan, offset, favScan.getSize( remote ) );
         offset = favScan.store( data, offset, remote );
       }
     }
     if ( remote.getMacroCodingType().hasTimedMacros() )
     {
       for ( TimedMacro timedMacro : timedMacros )
       {
         timedMacro.clearMemoryUsage();
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
   
   private List< Macro > getAllMacros( boolean forSaving )
   {
     List< Macro > allMacros = new ArrayList< Macro >();
     // for SSD remotes, put activity macros last.  Not sure if order is signficant
     // for other remotes, so in case it is, leave order unchanged for them
     if ( remote.isSSD() )
     {
       allMacros.addAll( macros );
     }
     if ( hasSegments() && ( forSaving || !remote.usesEZRC() ) && activities != null )
     {
       for ( Activity activity : activities.values() )
       {
         if ( activity.isActive() )
         {
           Macro macro = activity.getMacro();
           if ( macro != null )
           {
             allMacros.add( macro );
           }
         }
       }
     }
     if ( !remote.isSSD() )
     {
       allMacros.addAll( macros );
     }
     return allMacros;
   }
   
   private void updateMacroSegments( int keyCode, List< Macro > list, int type )
   {
     List< Integer > segmentTypes = remote.getSegmentTypes();
     LinkedHashMap< Integer, List< Macro > > bySubset = new LinkedHashMap< Integer, List<Macro> >();
     for ( Macro macro : list )
     {
       int subset = ( macro.getSegmentFlags() << 8 ) | macro.getDeviceIndex();
       List< Macro > subList = bySubset.get( subset );
       if ( subList == null )
       {
         subList = new ArrayList< Macro >();
       }
       subList.add( macro );
       bySubset.put( subset, subList );
     }
     for ( int subset : bySubset.keySet() )
     {
       list = bySubset.get( subset );
       if ( type == 0 && segmentTypes.contains( 1 ) && ( list.size() == 1 || !segmentTypes.contains( 2 ) ) )
       {
         type = 1;
       }
       else if ( type == 0 && segmentTypes.contains( 2 ) )
       {
         type = 2;
       }
       else if ( type == 0 && segmentTypes.contains( 3 ) )
       {
         type = 3;
       }
       if ( ( type == 1 || type == 3 ) && list.size() > 1 )
       {
         Macro macro = list.get( 0 );
         list.clear();
         list.add( macro );
       }
       int size = 0;
       for ( Macro macro : list )
       {
         size += macro.getData().length() + type - 1;
         if ( type == 3 )
         {
           size += macro.getData().length() + macro.getName().length() + 1;
         }
       }
       Hex segData = new Hex( size + ( remote.doForceEvenStarts() && ( size & 1 ) == 0 ? 4 : 3 ) );
       segData.set( ( short )0xFF, segData.length() - 1 );
       int flags = ( subset >> 8 ) & 0xFF;
       segData.set( ( short )( subset & 0xFF ), 0 );
       int pos = 1;
       segData.set( ( short )keyCode, pos++ );
       if ( type == 2 )
       {
         segData.set( ( short )list.size(), pos++ );
       }
       for ( Macro macro : list )
       {
         size = macro.getData().length();
         short[] data = macro.getData().getData();
         segData.set( ( short )size, pos++ );
         for ( int i = 0; i < size; i++ )
         {
           segData.set( ( short )( data[ i ] & 0xFF ), pos++ );
         }
         if ( type == 3 )
         {
           for ( int i = 0; i < size; i++ )
           {
             segData.set( ( short )( data[ i ] >> 8 ), pos++ );
           }
           int nameLen = macro.getName().length();
           segData.set( ( short )nameLen, pos++ );
           for ( int i = 0; i < nameLen; i++ )
           {
             segData.set( ( short )macro.getName().charAt( i ), pos++ );
           }
           segData.put( macro.getSerial(), pos );
           pos += 2;
         }
       }
       if ( segments.get( type ) == null )
       {
         segments.put( type, new ArrayList< Segment >() );
       }
       segments.get( type ).add( new Segment( type, flags, segData, list ) );
     }
   }
   
   private void updateHighlight( Highlight item, int offset, int length )
   {
     for ( int i = 0; i < length; i++ )
     {
       highlight[ offset + i ] = item.getHighlight();
     }
     item.addMemoryUsage( length );
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
       db.store( remote );
       db.doHighlight( highlight );
     }
     if ( !hasSegments() || remote.isSSD() )
     {
       return;
     }
     
     segments.get( 0 ).clear();
     for ( DeviceButton db : deviceButtons )
     {
       segments.get( 0 ).add(  db.getSegment() );
     }
     if ( remote.getSegmentTypes().contains( 0x15 ) || remote.getSegmentTypes().contains( 0x11 ) )
     {
       LinkedHashMap< Button, String > map = new LinkedHashMap< Button, String >();
       List< DeviceButton > list = new ArrayList< DeviceButton >();
       for ( DeviceButton db : deviceButtons )
       {
         if ( db.getSegment() != null && db.getDeviceTypeIndex( db.getSegment().getHex().getData() ) != 0xFF )
         {
           map.put( remote.getButton( db.getButtonIndex() ), db.getName() );
           list.add( db );
         }
       }
       
       if ( remote.getSegmentTypes().contains( 0x15 ) )
       {
         Hex hex = createNameHex( map );
         segments.put( 0x15, new ArrayList< Segment >() );
         segments.get( 0x15 ).add( new Segment( 0x15, 0xFF, hex ) );
       }
       if ( remote.getSegmentTypes().contains( 0x11 ) )
       {
         int size = 4 + 3 * list.size();
         size += ( size & 1 ) == 1 ? 1 : 0;
         Hex hex = new Hex( size );
         hex.put( 0xFFFF, size - 2 );
         hex.put( 0, 0 );
         hex.set( ( short )list.size(), 2 );
         int i = 3;
         for ( DeviceButton db : list )
         {
           short[] data = db.getSegment().getHex().getData();
           hex.set( ( short )db.getDeviceTypeIndex( data ), i );
           hex.put(  db.getSetupCode( data ), i + 1 );
           i += 3;
         }
         segments.put( 0x11, new ArrayList< Segment >() );
         segments.get( 0x11 ).add( new Segment( 0x11, 0xFF, hex ) );
       }
     }
   }
   
   /**
    * Update settings.
    */
   private void updateSettings()
   {
     if ( hasSegments() )
     {
       return;
     }
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
       if ( fixed.getLocation() == Location.E2 && ! fixed.check( data ) )
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
       if ( fixed.getLocation() == Location.E2 )
       {
         fixed.store( data );
       }
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
       if ( auto.getLocation() == Location.E2 )
       {
         auto.store( data );
       }
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
     System.err.println( "Decoding upgrades" );
     AddressRange addr = remote.getUpgradeAddress();
     // Also get address range for device specific upgrades, which will be null
     // if these are not used by the remote.
     AddressRange devAddr = remote.getDeviceUpgradeAddress();
 
     Processor processor = remote.getProcessor();
     if ( addr == null || processor == null )
     {
       return;
     }
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
           upgrade.setRemoteConfig( this );
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
       dev.clearMemoryUsage();
       dev.clearProtocolMemoryUsage();
       if ( dev.getButtonIndependent() )
       {
         devIndependent.add( dev );
         if ( dev.needsProtocolCode() && dev.getCode() != null )
         {
           dev.addProtocolMemoryUsage( dev.getCode().length() + 4 );
         }
       }
       if ( dev.getButtonRestriction() != DeviceButton.noButton )
       {
         devDependent.add( dev );
       }
     }
     
     // Get the address ranges
     AddressRange addr = remote.getUpgradeAddress();
     AddressRange devAddr = remote.getDeviceUpgradeAddress();
     if ( hasSegments() )
     {
       if ( !remote.getSegmentTypes().contains( 0x10 ) )
       {
         return;
       }
       segments.remove( 0x10 );
       for ( DeviceUpgrade dev : devices )
       {
         dev.setSoftButtonSegment( null );
         dev.setSoftFunctionSegment( null );
         Hex hex = dev.getUpgradeHex();
         Hex code = dev.needsProtocolCode() ? dev.getCode() : null;
         int size = hex.length() + ( ( code != null ) ? code.length() : 0 );
         size += ( remote.doForceEvenStarts() && ( size & 1 ) == 0 ) ? 10 : 9;
         Hex segData = new Hex( size );
         int flags = dev.getSegmentFlags();
         Arrays.fill( segData.getData(), 0, 4, ( short )0 );
         segData.getData()[ segData.length() - 1 ] = ( short )( remote.usesEZRC() ? 0xFF : 0 );
         if ( !dev.getButtonIndependent() )
         {
           segData.getData()[ 0 ] = ( short )dev.getButtonRestriction().getButtonIndex();
         }
         if ( code != null )
         {
           segData.put( hex.length() + 5, 2 );
         }
         segData.set( ( short )dev.getDeviceType().getNumber(), 4 );
         segData.put( dev.getSetupCode(), 5 );
         segData.set( ( short )dev.getSizeCmdBytes(), 7 );
         segData.set( ( short )dev.getSizeDevBytes(), 8 );
         segData.put( hex, 9 );
         if ( code != null )
         {
           segData.put( code, hex.length() + 9 );
         }
         if ( segments.get( 0x10 ) == null )
         {
           segments.put( 0x10, new ArrayList< Segment >() );
         }
         segments.get( 0x10 ).add( new Segment( 0x10, flags, segData, dev ) );
       }
       
       if ( remote.getSegmentTypes().contains( 0x0A ) || remote.getSegmentTypes().contains( 0x20 ) )
       {
         if ( remote.getSegmentTypes().contains( 0x0A ) )
         {
           segments.remove( 0x0A );
         }
         if ( remote.getSegmentTypes().contains( 0x20 ) )
         {
           segments.remove( 0x20 );
         }
         for ( DeviceButton db : remote.getDeviceButtons() )
         {
           DeviceUpgrade du = db.getUpgrade();
           LinkedHashMap< Button, String > map = new LinkedHashMap< Button, String >();
 
           if ( remote.getSegmentTypes().contains( 0x0A ) )
           {
             if ( segments.get( 0x0A ) == null )
             {
               segments.put( 0x0A, new ArrayList< Segment >() );
             }
             if ( du != null )
             {
               for ( Button b : du.getSoftButtons() )
               {
                 map.put( b, du.getFunction( b.getKeyCode() ).getName() );
               }
             }
             Hex hex = createNameHex( map );
             hex.set( ( short )db.getButtonIndex(), 0 );
             Segment segment = new Segment( 0x0A, 0xFF, hex );
             if ( du != null )
             {
               du.setSoftButtonSegment( segment );
             }
             segments.get( 0x0A ).add( segment );
           }
           
           map.clear();
           if ( remote.getSegmentTypes().contains( 0x20 ) && du != null && !du.getHardButtons().isEmpty())
           {
             if ( segments.get( 0x20 ) == null )
             {
               segments.put( 0x20, new ArrayList< Segment >() );
             }
             for ( Button b : du.getHardButtons() )
             {
               map.put( b, du.getFunction( b.getKeyCode() ).getName() );
             }
             Hex hex = createNameHex( map );
             hex.set( ( short )db.getButtonIndex(), 0 );
             Segment segment = new Segment( 0x20, 0xFF, hex );
             du.setSoftFunctionSegment( segment );
             segments.get( 0x20 ).add( segment );
           }
         }
       }
       return;
     }
     if ( addr == null && devAddr == null )
     {
       return;
     }
     
     // Get the protocols for the device-independent section
     LinkedHashMap< Protocol, ProtocolUpgrade > outputProtocols = getOutputProtocolUpgrades( false );
     
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
       upgrade.clearMemoryUsage();
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
       // Since memory usage not updated by next loop, do it here instead
       pr.addMemoryUsage( 2 );
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
             upg.addProtocolMemoryUsage( hex.length() );
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
   
 //  public void assignUpgrades()
 //  {
 //    if ( !remote.usesEZRC() )
 //    {
 //      return;
 //    }
 //    
 //    for ( DeviceButton db : remote.getDeviceButtons() )
 //    {
 //      DeviceUpgrade du = null;
 //      if ( db.getDeviceTypeIndex( db.getSegment().getHex().getData() ) != 0xFF )
 //      {
 //        for ( DeviceUpgrade test : devices )
 //        {
 //          if ( !test.getButtonIndependent() && test.getButtonRestriction() == db )
 //          {
 //            du = test;
 //            break;
 //          }
 //        }
 //      }
 //      db.setUpgrade( du );
 //    }
 //  }
 
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
     if ( hasSegments() )
     {
       if ( !remote.getSegmentTypes().contains( 9 ) )
       {
         return;
       }
       segments.remove( 9 );
       for ( LearnedSignal ls : learned )
       {
         boolean isMAXQ = remote.getProcessor().getEquivalentName().equals( "MAXQ610" );
         ls.clearMemoryUsage();
         Hex hex = ls.getData();
         int size = hex.length();
         int segSize = size + ( ( remote.doForceEvenStarts() && ( size & 1 ) == 1 ) ? 3 : 2 )
           + ( isMAXQ ? 2 : 0 );   
         Hex segData = new Hex( segSize );
         int flags = ls.getSegmentFlags();
         segData.set( ( short )ls.getDeviceButtonIndex(), 0 );
         segData.set( ( short )ls.getKeyCode(), 1 );
         if ( isMAXQ )
         {
           // It is not clear whether the 0 at offset 3 is high byte of 2-byte little-endian
           // length value or is a set of flags that have not yet been seen.  The fact that
           // the length value counts this byte suggests the latter.
           segData.set( ( short )( size + 1 ), 2 );
           segData.set( ( short )0, 3 );
           segData.put( hex, 4 );
           if ( ( size & 1 ) == 1 )
           {
             // set padding byte to 0xFF
             segData.set( ( short )0xFF, segSize - 1 );
           }
         }
         else
         {
           segData.put( hex, 2 );
         }
         if ( segments.get( 9 ) == null )
         {
           segments.put( 9, new ArrayList< Segment >() );
         }
         segments.get( 9 ).add( new Segment( 9, flags, segData, ls ) );
       }
       return;
     }
     
     AddressRange addr = remote.getLearnedAddress();
     if ( addr == null )
     {
       return;
     }
 
     int offset = addr.getStart();
     for ( LearnedSignal ls : learned )
     {
       ls.clearMemoryUsage();
       updateHighlight( ls, offset, ls.getSize() );
       offset = ls.store( data, offset, remote );
     }
     data[ offset++ ] = remote.getSectionTerminator();
     addr.setFreeStart( offset );
   }
   
   public int getDataEnd( short[] data )
   {
     if ( remote.isSSD() )
     {
       // get end of actual data from its format bytes
       int end = data[ 2 ] + ( data[ 3 ] << 8 ) + ( ( data[ 1 ] & 0xF0 ) << 12 );
       // round up to nearest 1kB
       return ( end + 0x3FF ) & ~0x3FF;
     }
     else
     {
       return data.length;
     }
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
     short[] dataToSave = RemoteMaster.useSavedData() ? savedData : data;
 
     pw.printHeader( "General" );
     pw.print( "Remote.name", remote.getName() );
     pw.print( "Remote.signature", remote.getSignature() );
     if ( hasSegments() && sigData != null )
     {
       pw.print( "Remote.sigData", Hex.toString( sigData ) );
     }
     pw.print( "Notes", notes );
 
     pw.printHeader( "Buffer" );
     int base = remote.getBaseAddress();
     for ( int i = 0; i < getDataEnd( dataToSave ); i += 16 )
     {
       pw.print( String.format( "%04X", i + base ), Hex.toString( dataToSave, i, 16 ) );
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
 
     for ( Macro macro : getAllMacros( true ) )
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
       pw.printHeader( "TimedMacro" );
       tm.store( pw );
     }
 
     for ( FavScan fs : favScans )
     {
       pw.printHeader( "FavScan" );
       fs.store( pw, this );
     }
     
     if ( remote.usesEZRC() && favScans.size() > 0 )
     {
       Button favBtn = remote.getButton( remote.getFavKey().getKeyCode() );
       Activity activity = null;
       if ( activities != null && ( activity = activities.get( favBtn )) != null )
       {
         pw.printHeader( "FavData" );
         pw.print( "Pause", favPause );
         pw.print( "FinalKey", favFinalKey.getKeyCode() );
         ActivityGroup.store( pw, activity.getActivityGroups() );
       }
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
     
     if ( activities != null )
     {
       for ( Activity activity : activities.values() )
       {
         if ( activity.isActive() )
         {
           pw.printHeader( "Activity" );
           activity.store( pw );
         }
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
   
   public String getSigString()
   {
     if ( sigData == null || ( sigData[ 0 ] + sigData[ 1 ] ) == 0xFF )
     {
       // second case is XSight remotes where sig string is just the 6-character signature
       return null;
     }
     char[] sig = new char[ 26 ];
     for ( int i = 0; i < Math.min( sigData.length, 26 ); i++ )
     {
       sig[ i ] = ( char )sigData[ i ];
     }
     return String.valueOf( sig );
   }
 
   private int getSigAddress()
   {
     // The assumption is that the signature block always immediately follows the block
     // of word based flash.  In the JP1.4 and JP2 remotes seen so far, this is also the
     // start of byte based flash but in JP2.1 the signature block seems to be a distinct
     // block sitting between the word based and byte based flash.
     int addrLen = remote.getProcessor().getAddressLength();
     if ( sigData == null || sigData.length < 26 + 2 * addrLen )
     {
       return 0;
     }
     if ( addrLen == 2 )
     {
       return Hex.get( sigData, 28 ) + 1;
     }
     else
     {
       return Hex.get( sigData, 30 ) * 0x10000 + Hex.get( sigData, 32 ) + 1;
     }
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
 
 //  public List< Macro > getTableMacros()
 //  {
 //    if ( !remote.isSSD() )
 //    {
 //      return getMacros();
 //    }
 //    List< Macro > list = new ArrayList< Macro >();
 //    for ( Macro macro : getMacros() )
 //    {
 ////      if ( !macro.isSystemMacro() )
 //      {
 //        list.add( macro );
 //      }
 //    }
 //    return list;
 //  }
   
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
 
   public short[] getSigData()
   {
     return sigData;
   }
 
   public String getEepromFormatVersion()
   {
     return eepromFormatVersion;
   }
 
   public void setSigData( short[] sigData )
   {
     this.sigData = sigData;
   }
 
   /** The remote. */
   private Remote remote = null;
 
   public void setRemote( Remote remote )
   {
     this.remote = remote;
   }
 
   /** The data. */
   private short[] data = null;
   
   protected short[] origData = null;
   
   private short[] sigData = null;
   
   /** Provisional interpretation of bytes 7-12 of the E2 area of an XSight remote. */
   private String eepromFormatVersion = null;
   
   private Color[] highlight = null;
 
   /** The saved data. */
   private short[] savedData = null;
   
   private LinkedHashMap< Integer, List<Segment> > segments = new LinkedHashMap< Integer, List<Segment> >();
   
   private LinkedHashMap< String, SSDFile > ssdFiles = new LinkedHashMap< String, SSDFile >();
   
   private LinkedHashMap< Button, Activity > activities = null;
   
   private List< Integer > segmentLoadOrder = new ArrayList< Integer >();
 
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
     if ( remote.usesEZRC() )
     {
       return;
     }
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
 
   public int getFavPause()
   {
     return favPause;
   }
 
   public void setFavPause( int favPause )
   {
     this.favPause = favPause;
   }
 
   public void initializeSetup( int startAddr )
   {
     // Fill buffer with 0xFF
     Arrays.fill( data, startAddr, data.length, ( short )0xFF );
     if ( remote.isSSD() )
     {
       
     }
     else if ( hasSegments() )
     {
       initializeSegments();
     }
     else
     {
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
     }
 
     // Unless remote uses soft devices, set default device types and setup codes in buffer
     SoftDevices softDevices = remote.getSoftDevices();
     if ( softDevices == null || !softDevices.inUse() || softDevices.isSetupCodesOnly() )
     {
       DeviceButton[] devBtns = remote.getDeviceButtons();
       java.util.List< DeviceType > devTypeList = remote.getDeviceTypeList();
       short[] data = this.data;
       int j = 0;
       for ( int i = 0; i < devBtns.length; i++ )
       {
         DeviceType dt = devTypeList.get( j );
         DeviceButton db = devBtns[ i ];
         if ( hasSegments() )
         {
           data = db.getSegment().getHex().getData();
         }
         db.zeroDeviceSlot( data );
         db.setDeviceTypeIndex( ( short )dt.getNumber(), data );
         db.setDeviceGroup( ( short )dt.getGroup(), data );
         if ( softDevices == null || !softDevices.inUse() )
         {
           db.setSetupCode( ( short )db.getDefaultSetupCode(), data );
         }
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
     if ( remote.hasFavKey() )
     {
       if ( remote.getFavKey().isSegregated() )
       {
         int offset = remote.getFavScanAddress().getStart();
         data[ offset++ ] = 0;
         data[ offset++ ] = 0;
       }
     }
     
     // Clear the dialogs of data from any previous setup
     resetDialogs();
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
   
   public void initializeSegments()
   {
     List< Integer > segmentTypes = remote.getSegmentTypes();
     segmentLoadOrder.addAll( segmentTypes );
     if ( hasSegments() )
     {
       List< Segment > list = new ArrayList< Segment >();
       for ( DeviceButton db : remote.getDeviceButtons() )
       {
         Hex hex = new Hex( 12 );
         short[] data = hex.getData();
         Arrays.fill( data, ( short )0xFF );
         data[ 0 ] = ( short )db.getButtonIndex();
         data[ 1 ] = ( short )0;
         list.add( new Segment( 0, 0xFF, hex, db ) );
       }
       segments.put( 0, list );
       if ( segmentTypes.contains( 0xDA ) )
       {
         list = new ArrayList< Segment >();
         Hex hex = new Hex( remote.getDeviceButtons().length + 2 );
         short[] data = hex.getData();
         Arrays.fill( data, ( short )0xFF );
         int i = 0;
         data[ i++ ] = ( short )0;
         data[ i++ ] = ( short )0;
         for ( DeviceButton db : remote.getDeviceButtons() )
         {
           data[ i++ ] = ( short )db.getButtonIndex();
         }
         list.add( new Segment( 0xDA, 0xFF, hex ) );
       }
       segments.put( 0xDA, list );
       if ( segmentTypes.contains( 0x1A ) )
       {
         list = new ArrayList< Segment >();
         Hex hex = new Hex( 3 );
         short[] data = hex.getData();
         data[ 0 ] = ( short )0;
         data[ 1 ] = ( short )0;
         data[ 2 ] = ( short )0x40;
         list.add( new Segment( 0x1A, 0xFF, hex ) );
       }
       segments.put( 0x1A, list );
     }
     
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
 
   public Button getFavFinalKey()
   {
     return favFinalKey;
   }
 
   public void setFavFinalKey( int keyCode )
   {
     favFinalKey = ( keyCode == 0xFF ) ? null : remote.getButton( keyCode );
   }
 
   public void setFavFinalKey( Button favFinalKey )
   {
     this.favFinalKey = favFinalKey;
   }
 
   /** The notes. */
   private String notes = null;
 
   private String[] deviceButtonNotes = null;
 
   private DeviceButton favKeyDevButton = null;
   
   private int favPause = 0;
   
   private Button favFinalKey = null;
   
   public ProtocolUpgrade protocolUpgradeUsed = null;
   
   private RemoteMaster owner = null;
 
   public RemoteMaster getOwner()
   {
     return owner;
   }
   
   private class KeySpec
   {
     DeviceButton dev;
     Button btn;
     
     public KeySpec( DeviceButton dev, Button btn )
     {
       this.dev = dev;
       this.btn = btn;
     }
   }
   
   private class Items
   {
     DeviceButton db = null;
     List< DeviceButton > dbList = new ArrayList< DeviceButton >();
     DeviceUpgrade upgrade = null;
     Hex fixedData = null;
     Hex pCode = null;
     Hex pid = null;
     int setupCode = 0;
     String alias = null;
     List< Key > keys = null;
     Key key = null;
     int missingIndex = 0;
     Macro macro = null;
     List< Integer > macroKeys = null;
     Activity activity = null;
     List< Activity > activities = new ArrayList< Activity >();
     List< Assister > assisters = null;
     LinkedHashMap< Button, KeySpec > map = null;
     Integer softPageIndex = null;
     FavScan fav = null;
     SpecialProtocolFunction sf = null;
   }
   
   private class Key
   {
     Button btn = null;
     int keycode = 0;
     Integer keyflags = 0;
     Integer keygid = null;
     Hex irdata = null;
     LearnedSignal ls = null;
     String fname = null;
     Integer iconref = null;
     Integer macroref = null;
   }
   
   private class Icon
   {
     int height = 0;
     int width = 0;
     Hex hex1 = null;
     Hex hex2 = null;
   }
   
   private class SSDFile
   {
     public SSDFile() {};
     
     public SSDFile( List< String > tagNames, List< Hex > sectors )
     {
       int len = 0;
       for ( Hex hex : sectors )
       {
         len += hex.length();
       }
       short[] data = new short[ len ];
       len = 0;
       for ( Hex hex : sectors )
       {
         System.arraycopy( hex.getData(), 0, data, len, hex.length() );
         len += hex.length();
       }
       this.tagNames = tagNames;
       this.hex = new Hex( data );
 //      System.err.println( "Constructed hex = " + hex );
     }
 
     List< String > tagNames = null;
     Hex hex = null;
     LinkedHashMap< Integer, Icon > userIcons = null;
   }
   
   private List< String > tagList = null;
   
   private Hex makeBXMLFile( SSDFile file )
   {
     int tagSize = 0;
     short tagCount = 0;
     for ( String s : file.tagNames )
     {
       tagSize += s.length() + 1;
       tagCount++;
     }
     int hexSize = 17 + tagSize + file.hex.length();
     Hex fileHex = new Hex( hexSize );
     fileHex.put( new Hex( "ezrc    bxml10", 8 ), 0 );
     fileHex.put( getLittleEndian( tagSize ), 14 );
     fileHex.set( tagCount, 16 );
     int pos = 17;
     for ( String s : file.tagNames )
     {
       fileHex.put( new Hex( s, 8 ), pos );
       pos += s.length();
       fileHex.set( ( short  )0, pos++ );
     }
     fileHex.put( file.hex, pos );
     return fileHex;
   }
   
   private short getTag( String name )
   {
     int tag = tagList.indexOf( name );
     if ( tag == -1 )
     {
       tag = tagList.size();
       tagList.add( name );  
     }
     return ( short )tag;
   }
   
   private Hex endTag( String name )
   {
     int tag = tagList.indexOf( name );
     if ( tag == -1 )
     {
       System.err.println( "Tag error:  undefined end tag \"" + name + "\"" );
       tag = 0x80;
     }
     else
     {
       tag |= 0x80;
     }
     return new Hex( new short[]{ ( short )tag } );
   }
   
   private Hex makeItem( String tagName, Hex value, boolean end )
   {
     Hex hex = new Hex( value.length() + ( end ? 3 : 2 ) );
     short tag = getTag( tagName );
     hex.set( tag , 0 );
     hex.set( ( short )value.length(), 1 );
     hex.put( value, 2 );
     if ( end )
     {
       hex.set( ( short )( tag | 0x80 ), value.length() + 2 );
     }
     return hex;
   }
   
   private SSDFile makeMacrosXCF()
   {
     tagList = new ArrayList< String >();
     List< Hex > work = new ArrayList< Hex >();
     
     work.add( makeItem( "macros", new Hex( "macros.xcf", 8 ), false ) );
     for ( SpecialProtocolFunction sf : specialFunctions )
     {
       Activity activity = null;
       if ( sf.getMacro() != null )
       {
         Macro macro = sf.getMacro();
         if ( activities != null )
         {
           for ( Activity a : activities.values() )
           {
             if ( a.getMacro() == macro )
             {
               activity = a;
               break;
             }
           }
         }
       }
       work.add( makeItem( "macro", getLittleEndian( sf.getSerial() ), false ) );
       if ( sf.getName() == null )
       {
         continue;
       }
       work.add( makeItem( "name16", new Hex( sf.getName(), 16 ), true ) );
 
       short[] keys = new short[ 0 ];
       String btnTag = null;
       Hex btnHex = null;
       boolean isSysMacro = false;
       if ( sf.getMacro() != null )
       {
         keys = sf.getMacro().getData().getData();
       }
       else if ( sf.getKeyMove() instanceof KeyMoveKey )
       {
         KeyMoveKey km = ( KeyMoveKey )sf.getKeyMove();
         keys = new short[ 2 ];
         keys[ 0 ] = ( short )km.getTargetDevice().getButtonIndex();
         keys[ 1 ] = km.getMovedKeyCode();
         isSysMacro = true;
       }
       else
       {
         KeyMove km = ( KeyMove )sf.getKeyMove();
         DeviceButton dev = km.getTargetDevice();
         keys = new short[ 2 ];
         keys[ 0 ] = ( short )km.getTargetDevice().getButtonIndex();
         keys[ 1 ] = ( short )( km.getIrSerial() + 0x80 );
 //        Hex cmd = km.getCmd();
         
 //        btnHex = new Hex( cmd.length() + 1 );
 //        btnHex.set( ( short )km.getTargetDevice().getButtonIndex(), 0 );
 //        btnHex.put( cmd, 1 );
 //        btnTag = "sendir";
         isSysMacro = true;
       }
 
       for ( int i = 0; i < keys.length - 1; i++ )
       {
         DeviceButton db = remote.getDeviceButton( keys[ i ] );
         if ( db == null )
         {
           // malformed macro
           continue;
         }
         short dev = ( short )( keys[ i++ ] - 0x50 );
         short keycode = ( short )( keys[ i ] & 0xFF );  
         short duration = 0;
         List< Button > holds = remote.getButtonGroups().get( "Hold" );
         if ( holds != null && !holds.isEmpty() && keycode == holds.get( 0 ).getKeyCode() )
         {
           duration = ( short )( ( keys[ i ] >> 8 ) & 0xFF );
           i++;
           keycode = ( short )( keys[ i ] & 0xFF );
         }
         boolean softKey = remote.isSoftButton( remote.getButton( keycode ) );
         boolean hardKey = ( keycode & 0x80 ) == 0 || ( keycode & 0xF0 ) == 0xF0;
         btnTag = softKey ? "sendsoftkey" : hardKey ? "sendhardkey" : "sendir" ;
         short softPage = ( short )( softKey ? ( keycode - 0x31 ) / 6 : 0 );
         keycode -= 6 * softPage;
         btnHex = softKey ? new Hex( new short[]{ dev, softPage, keycode } ) :
             hardKey ? new Hex( new short[]{ dev, keycode } ) :
             new Hex( new short[]{ dev, ( short )( keycode & 0x7F ), 0 } ); 
         short delay = ( short )( isSysMacro ? 0x03 : ( keys[ i ] >> 8 ) & 0xFF );
         work.add( makeItem( btnTag, btnHex, false ) );
         if ( isSysMacro || duration > 0 )
         {
           // explicit zero duration for sysMacro 
           if ( duration == 0xFF )
           {
             duration = 0;
           }
           work.add( makeItem( "duration", new Hex( new Hex( getLittleEndian( duration * 100 ), 0, 4 ) ), true ) );
         }
         work.add( endTag( btnTag ) );
         work.add( makeItem( "delay", new Hex( new short[]{ delay } ), true ) );
       }
       if ( isSysMacro )
       {
         work.add( makeItem( "issystemmacro", new Hex( new short[]{ 1 } ), true ) );
       }
       boolean assistantDone = false;
       if ( isSysMacro || activity != null && !activity.getAssists().isEmpty() 
           || activity == null && !sf.getAssists().isEmpty() )
       {
         assistantDone = true;
         work.add( makeItem( "assistant", new Hex( 0 ), false ) );
       }
       if ( activity != null && !activity.getAssists().isEmpty() 
           || activity == null && !sf.getAssists().isEmpty() )
       {
         LinkedHashMap< Integer, List< Assister > > assists = 
             activity != null ? activity.getAssists() : sf.getAssists();
         String[] tagNames = null;
         for ( int i = 0; i < 3; i++ )
         {
           List< Assister > assisters = assists.get(  2 - i );
           if ( assisters.size() > 0 )
           {
             if ( tagNames == null )
             {
               tagNames = new String[]{ "powerkeys", "audioinputs", "pictureinputs" };
               for ( String tagName : tagNames )
               {
                 // create the tags, whether they are used or not
                 getTag( tagName );
               }
             }
             work.add( makeItem( tagNames[ i ], new Hex( 0 ), false ) );
             for ( Assister assister : assists.get(  2 - i ) )
             {
               short dev = ( short )( assister.device.getButtonIndex() - 0x50 );
               short keycode = ( short )( assister.button.getKeyCode() & 0xFF );
               
               boolean softKey = remote.isSoftButton( remote.getButton( keycode ) );
               boolean hardKey = ( keycode & 0x80 ) == 0 || ( keycode & 0xF0 ) == 0xF0;
               btnTag = softKey ? "softkey" : hardKey ? "hardkey" : "irref" ;
               short softPage = ( short )( softKey ? ( keycode - 0x31 ) / 6 : 0 );
               keycode -= 6 * softPage;
               btnHex = softKey ? new Hex( new short[]{ dev, softPage, keycode } ) :
                   hardKey ? new Hex( new short[]{ dev, keycode } ) :
                   new Hex( new short[]{ dev, ( short )( keycode & 0x7F ), 0 } ); 
               work.add( makeItem( btnTag, btnHex, true ) );
             }
             work.add( endTag( tagNames[ i ] ) );
           }
         }
       }
       if ( assistantDone )
       {
         work.add( endTag( "assistant" ) );
       }
       work.add( endTag( "macro" ) );
     }
     work.add( endTag( "macros" ) );
     if ( tagList.size() == 1 )
     {
       return null;
     }
     SSDFile file = new SSDFile( tagList, work );
     tagList = null;
     return file;
   }
   
   private SSDFile makeDevicesXCF()
   {
     tagList = new ArrayList< String >();
     List< Hex > work = new ArrayList< Hex >();
     work.add( makeItem( "devices", new Hex( "devices.xcf", 8 ), false ) );    
     for ( DeviceButton db : remote.getDeviceButtons() )
     {
       Segment seg  = db.getSegment();
       if ( seg == null )
       {
         continue;
       }
       short[] segData = seg.getHex().getData();
       short serial = ( short )( db.getButtonIndex() - 0x50 );
       work.add( makeItem( "device", new Hex( "" + new Hex( new short[]{ serial } ) + " " + new Hex( db.getName(), 16 ) ), false ) );
       String brand = remote.getDeviceLabels().getText2( segData, 1 );
       if ( brand != null )
       {
         work.add( makeItem( "brand", new Hex( brand, 8 ), true ) );
       }
       String model = remote.getDeviceLabels().getText2( segData, 2 );
       if ( model != null )
       {
         work.add( makeItem( "model", new Hex( model, 8 ), true ) );
       }
       model = remote.getDeviceLabels().getText2( segData, 3 );
       if ( model != null )
       {
         work.add( makeItem( "remotemodel", new Hex( model, 8 ), true ) );
       }
       work.add( makeItem( "iconref", new Hex( new short[]{ ( short )db.getIconRef() } ), true ) );
       if ( favKeyDevButton == db && favFinalKey != null )
       {
         work.add( makeItem( "enterkey", new Hex( new short[]{ ( short )favFinalKey.getKeyCode() } ), true ) );
       }
       work.add( makeItem( "favoritewidth", new Hex( new short[]{ ( short )db.getFavoriteWidth() } ), true ) );
       if ( db.getVpt() == 2 )
       {
         work.add( makeItem( "volumemaster", new Hex( 0 ), true ) );
       }
       if ( db.getVpt() >= 1 )
       {
         work.add( makeItem( "volumelock", new Hex( 0 ), true ) );
       }
       String s = new String( new char[]{ ( char )db.getDeviceTypeIndex( segData ) } );
       s += db.isConstructed() ? "????" : new SetupCode( db.getSetupCode( segData ) ).toString();
       work.add( makeItem( "codeset", new Hex( s, 8 ), false ) );
       DeviceUpgrade upg = db.getUpgrade();
       Protocol prot = upg.getProtocol();
       if ( !db.isConstructed() )
       {
         Hex fixed = prot.getFixedData( upg.getParmValues() );
         if ( fixed.length() > 0 )
         {
           work.add( makeItem( "prefix", fixed, true ) );
         }
         work.add( makeItem( "executor", new Hex( new short[]{ prot.getID().getData()[ 1 ], prot.getID().getData()[ 0 ] } ), false ) );
 
         Hex code = upg.getCode();
         if ( upg.needsProtocolCode() && code != null && code.length() != 0 )
         {
           work.add( makeItem( "objcode", encryptObjcode( code ), true ) );
         }
         work.add( endTag( "executor" ) );
       }
       List< Button > buttons = new ArrayList< Button >( Arrays.asList( remote.getUpgradeButtons() ) );
       buttons.addAll( remote.getFunctionButtons() );
       Collections.sort( buttons, DeviceUpgrade.ButtonSorter );
       Integer softpage = null;
       boolean irUsed = false;
       for ( Button b : buttons )
       {
         Function f = upg.getAssignments().getAssignment( b );
         Macro macro = getMacro( db, b );
         KeyMove km = getKeyMove( db, b );
         LearnedSignal ls = findLearnedSignal( db, b );
         String btnTag = "keydef";
         String name = null;
         int keyCode = b.getKeyCode();
         
         if ( f == null && macro == null && km == null && ls == null )
         {
           continue;
         }
         if ( !remote.isSoftButton( b ) && softpage != null
             || remote.isSoftButton( b ) && softpage != null && softpage != ( keyCode - 0x31 ) / 6 )
         {
           work.add( endTag( "softpage" ) );
           softpage = null;
         }
         if ( remote.isSoftButton( b ) && softpage == null )
         {
           softpage = ( keyCode - 0x31 ) / 6;
           work.add( makeItem( "softpage", new Hex( new short[]{ ( short )( int )softpage } ), false ) );
         }
         if ( softpage != null )
         {
           keyCode -= 6 * softpage;
         }
         Hex btnHex = new Hex( new short[]{ ( short )keyCode } );
         if ( remote.isFunctionButton( b ) )
         {
           if ( !irUsed )
           {
             work.add( makeItem( "irdefs", new Hex( 0 ), false ) );
           }
           btnTag = "irdef";
           btnHex = new Hex( new short[]{ ( short )( b.getKeyCode() & 0x7F ), 0 } );
           irUsed = true;
         }
         else if ( !remote.isFunctionButton( b ) && irUsed )
         {
           work.add( endTag( "irdefs" ) );
           irUsed = false;
         }
         
         work.add( makeItem( btnTag, btnHex, false ) );
         if ( ls != null )
         {
           int len = ls.getData().length();
           Hex hex = new Hex( len + 9 );
           hex.set( ( short )( len + 8 ), 0 );
           hex.put( ls.getHeader(), 1 );
           hex.set( ( short )len, 8 );
           System.arraycopy( ls.getData().getData(), 0, hex.getData(), 9, len );
           work.add( makeItem( "learnedkey", hex, true ) );
           name = ls.getName();;
         }
         
         if ( f != null && f.getHex() != null )
         {
           work.add( makeItem( "keygid", getLittleEndian( f.getIndex() ), true ) );
           work.add( makeItem( "keyflags", new Hex( new short[]{ ( short )( f.getKeyflags() == null ? 0 : f.getKeyflags() ) } ), true ) );
           work.add( makeItem( "irdata", f.getHex(), true ) );
           if ( f.getIconref() != null )
           {
             work.add( makeItem( "iconref", new Hex( new short[]{ ( short )( int )f.getIconref() } ), true ) );
           }
           name = f.getName();
         }
         
         if ( macro != null )
         {
           work.add( makeItem( "macroref", getLittleEndian( macro.getSerial() ), true ) );
           name = f != null ? f.getName() : macro.getName();
         }
         
         if ( km != null )
         {
           work.add( makeItem( "macroref", getLittleEndian( km.getSerial() ), true ) );
           if ( name == null )
           {
             name = f != null ? f.getName() : km.getName();
           }
         }
         work.add( makeItem( "name8", new Hex( name, 8 ), true ) );
         work.add( endTag( btnTag ) );
       }
       if ( softpage != null )
       {
         work.add( endTag( "softpage" ) );
       }
       if ( irUsed )
       {
         work.add( endTag( "irdefs" ) );
       }
       work.add( endTag( "codeset" ) );
       work.add( endTag( "device" ) );
     }
     work.add( endTag( "devices" ) );
     if ( tagList.size() == 1 )
     {
       return null;
     }
 //    if ( ( tagList.size() & 1 ) == 1 )  
     {
       // This tag sometimes is included when it is not used, but can't figure out
       // just when that is.  It doesn't seem to matter if it is included or not, but
       // it makes checking easier to have it correct.
       getTag( "learnedkey" );  // tag always present even if unused
     }
     SSDFile file = new SSDFile( tagList, work );
     tagList = null;
     return file;
   }
   
   private SSDFile makeHomeXCF()
   {
     tagList = new ArrayList< String >();
     List< Hex > work = new ArrayList< Hex >();
     work.add( makeItem( "root", new Hex( 0 ), false ) );
     work.add( makeItem( "home", new Hex( 0 ), false ) );
     for ( String name : new String[]{"devices", "profiles", "favorites", "macros", "activities" } )
     {
       String fileName = name + ".xcf";
       SSDFile file = ssdFiles.get( fileName );
       work.add( makeItem( name, ( file == null ) ? new Hex( 0 ) : new Hex( fileName, 8 ), true ) );
     }
     work.add( endTag( "home" ) );
     work.add( endTag( "root" ) );
     SSDFile file = new SSDFile( tagList, work );
     tagList = null;
     return file;
   }
   
   private SSDFile makeProfilesXCF()
   {
     tagList = new ArrayList< String >();
     List< Hex > work = new ArrayList< Hex >();
     LinkedHashMap< Button, KeySpec > map = new LinkedHashMap< Button, KeySpec >();
     work.add( makeItem( "profiles", new Hex( "profiles.xcf", 8 ), false ) );
     for ( Activity activity : remote.getFavKey().getProfiles() )
     {
       work.add( makeItem( "profile", new Hex( "" + new Hex( new short[]{ ( short )activity.getProfileIndex() } ) + " " + new Hex( activity.getName(), 16 ) ), false ) );
       work.add( makeItem( "iconref", new Hex( new short[]{ ( short )activity.getIconRef() } ), true ) );
       work.add( makeItem( "punchthrumap", new Hex( 0 ), false ) );
       ActivityGroup[] groups = activity.getActivityGroups();
       for ( ActivityGroup group : groups )
       {
         DeviceButton db = group.getDevice();
         if ( db == null )
         {
           continue;
         }
         for ( Button b : group.getButtonGroup() )
         {
           map.put( b, new KeySpec( db, b ) );
         }
       }
       for ( int code : activityOrder )
       {
         Button b = remote.getButton( code );
         if ( b == null )
         {
           continue;
         }
         KeySpec ks = map.get( b );
         if ( ks == null )
         {
           continue;
         }
         short serial = ( short )( ks.dev.getButtonIndex() - 0x50 );
         work.add( makeItem( "punchthruspec", new Hex( new short[]{ serial, ( short )code, ( short )code } ), true ) );
       }
       work.add( endTag( "punchthrumap" ) );
       work.add( endTag( "profile" ) );
     }
     work.add( endTag( "profiles" ) );
     if ( tagList.size() == 1 )
     {
       return null;
     }
     SSDFile file = new SSDFile( tagList, work );
     tagList = null;
     return file;
   }
   
   private SSDFile makeFavoritesXCF()
   {
     if ( favScans.isEmpty() )
     {
       return null;
     }
     tagList = new ArrayList< String >();
     Activity activity = activities.get( remote.getButtonByStandardName( "Favorites" ) );
     List< Hex > work = new ArrayList< Hex >();
     LinkedHashMap< Button, KeySpec > map = new LinkedHashMap< Button, KeySpec >();
     work.add( makeItem( "favorites", new Hex( "favorites.xcf", 8 ), false ) );
     work.add( makeItem( "delay", new Hex( new short[]{ ( short )favPause } ), true ) );
     for ( FavScan favScan : favScans )
     {
       work.add( makeItem( "favorite", new Hex( "" + new Hex( new short[]{ ( short )favScan.getSerial() } ) + " " + new Hex( favScan.getName(), 16 ) ), false ) );
       for ( int index : favScan.getProfileIndices() )
       {
         work.add( makeItem( "profileindex", new Hex( new short[]{ ( short )index } ), true ) );
       }
       work.add( makeItem( "deviceid", new Hex( new short[]{ ( short )( favKeyDevButton.getButtonIndex() - 0x50 ) } ), true ) );
       if ( favScan.getIconRef() != null )
       {
         work.add( makeItem( "iconref", new Hex( new short[]{ ( short )( int )favScan.getIconRef() } ), true ) );
       }
       short[] data = favScan.getData().getData();
       int val = 0;
       for ( int i = 0; i < data.length; i++ )
       {
         Button b = remote.getButton( data[ data.length - i - 1 ] );
         int digit = b.getName().charAt( 0 ) & 0x0F;
         val |= digit << ( 4 * i );
       }
       Hex hex = new Hex( 4 );
       hex.set( ( short  )( val & 0xFF ), 0 );
       hex.set( ( short  )( ( val >> 8 ) & 0xFF ), 1 );
       work.add( makeItem( "channelnumber", hex, true ) );
       work.add( endTag( "favorite" ) );
     }
     work.add( makeItem( "punchthrumap", new Hex( 0 ), false ) );
     ActivityGroup[] groups = activity.getActivityGroups();
     for ( ActivityGroup group : groups )
     {
       DeviceButton db = group.getDevice();
       if ( db == null )
       {
         continue;
       }
       for ( Button b : group.getButtonGroup() )
       {
         map.put( b, new KeySpec( db, b ) );
       }
     }
     for ( int code : activityOrder )
     {
       Button b = remote.getButton( code );
       if ( b == null )
       {
         continue;
       }
       KeySpec ks = map.get( b );
       if ( ks == null )
       {
         continue;
       }
       short serial = ( short )( ks.dev.getButtonIndex() - 0x50 );
       work.add( makeItem( "punchthruspec", new Hex( new short[]{ serial, ( short )code, ( short )code } ), true ) );
     }
     work.add( endTag( "punchthrumap" ) );
     work.add( endTag( "favorites" ) );
     SSDFile file = new SSDFile( tagList, work );
     tagList = null;
     return file;
   }
   
   private SSDFile makeActivitiesXCF()
   {
     tagList = new ArrayList< String >();
     List< Hex > work = new ArrayList< Hex >();
     LinkedHashMap< Button, KeySpec > map = new LinkedHashMap< Button, KeySpec >();
 
     work.add( makeItem( "activities", new Hex( "activities.xcf", 8 ), false ) );
     for ( Activity activity : activities.values() )
     {
       Button btn = activity.getButton();
 //    for ( Button btn : remote.getButtonGroups().get( "Activity" ) )
 //    {
 //      Activity activity = activities.get( btn );
       if ( activity == null || !activity.isActive() )
       {
         continue;
       }
       short serial = ( short )( btn.getKeyCode() & 0x0F );
       work.add( makeItem( "activity", new Hex( "" + new Hex( new short[]{ serial } ) + " " + new Hex( activity.getName(), 16 ) ), false ) );
       work.add( makeItem( "iconref", new Hex( new short[]{ ( short )activity.getIconRef() } ), true ) );
       work.add( makeItem( "macroref", new Hex( new short[]{ ( short )activity.getMacro().getSerial(), 0 } ), true ) );
       work.add( makeItem( "punchthrumap", new Hex( 0 ), false ) );
       ActivityGroup[] groups = activity.getActivityGroups();
       for ( ActivityGroup group : groups )
       {
         DeviceButton db = group.getDevice();
         for ( Button b : group.getButtonGroup() )
         {
           map.put( b, new KeySpec( db, b ) );
         }
       }
       for ( int code : activityOrder )
       {
         Button b = remote.getButton( code );
         if ( b == null )
         {
           continue;
         }
         KeySpec ks = map.get( b );
         if ( ks == null || ks.dev == null )
         {
           continue;
         }
         serial = ( short )( ks.dev.getButtonIndex() - 0x50 );
         work.add( makeItem( "punchthruspec", new Hex( new short[]{ serial, ( short )code, ( short )code } ), true ) );
       }
       int count = -1;
       short softIndex = -1;
       for ( Button b : remote.getButtonGroups().get( "Soft" ) )
       {
         KeySpec ks = map.get( b );
         if ( ks == null || ks.dev == null )
         {
           continue;
         }
         DeviceButton db = ks.dev;
         serial = ( short )( db.getButtonIndex() - 0x50 );
         if ( ( ++count % 6 ) == 0 )
         {
           if ( count > 0 )
           {
             work.add( endTag( "softpage" ) );
           }
           work.add( makeItem( "softpage", new Hex( new short[]{ ++softIndex } ), false ) );
         }
         int code = b.getKeyCode() - 6 * softIndex;
         work.add( makeItem( "punchthruspec", new Hex( new short[]{ serial, ( short )code, ( short )code } ), false ) );
         DeviceUpgrade upg = db.getUpgrade();
         Function f = null;
         if ( upg != null && ( f = upg.getAssignments().getAssignment( b ) ) != null )
         {
           if ( f.getIconref() != null )
           {
             work.add( makeItem( "iconref", new Hex( new short[]{ ( short )( int )f.getIconref() } ), true ) );
           }
           work.add( makeItem( "name8", new Hex( f.getName(), 8 ), true ) );
         }
         work.add( endTag( "punchthruspec" ) );
       }
       if ( softIndex >= 0 )
       {
         work.add( endTag( "softpage" ) );
       }
       work.add( endTag( "punchthrumap" ) );
       work.add( endTag( "activity" ) );
     }
     work.add( endTag( "activities" ) );
     if ( tagList.size() == 1 )
     {
       return null;
     }
     SSDFile file = new SSDFile( tagList, work );
     tagList = null;
     return file;
   }
   
   private Macro getMacro( DeviceButton devBtn, Button btn )
   {
     for ( Macro macro : macros )
     {
       if ( macro.getDeviceButton( this ) == devBtn && macro.getKeyCode() == btn.getKeyCode() )
       return macro;
     }
     return null;
   }
   
   private KeyMove getKeyMove( DeviceButton devBtn, Button btn )
   {
     for ( KeyMove km : keymoves )
     {
       if ( km.getDeviceButtonIndex() == devBtn.getButtonIndex() && km.getKeyCode() == btn.getKeyCode() )
       return km;
     }
     return null;
   }
   
   private Hex getLittleEndian( int n )
   {
     return new Hex( new short[]{ ( short )( n & 0xFF), ( short )( ( n >> 8 ) & 0xFF ) } );
   }
   
   public static Comparator< Macro > MacroSorter = new Comparator< Macro >()
   {
     @Override
     public int compare( Macro m1, Macro m2 )
     {
       return ( new Integer( m1.getSerial()) ).compareTo( new Integer( m2.getSerial()) );
     }    
   };
   
   public static final int[] encryptionKey = { 0x35, 0x34, 0x6E, 0x64, 0x73, 0x66, 
     0x6A, 0x6B, 0x68, 0x61, 0x6B, 0x6C, 0x66, 0x64, 0x6A, 0x6B, 0x6C, 0x34, 0x65, 
     0x37, 0x39, 0x38, 0x09, 0x34, 0x20, 0x2E, 0x22, 0x7E, 0x6C, 0x66, 0x0A, 0x6A, 
     0x68, 0x66, 0x6B, 0x68, 0x67, 0x38, 0x37, 0x34, 0x39, 0x38, 0x68, 0x74, 0x51, 
     0x51, 0x34, 0x32, 0x32, 0x39, 0x33, 0x01, 0x37, 0x39 };
   
   public static final int[] activityOrder = { 0x1B, 0x17, 0x0F, 0x16, 0x11, 0x13, 
     0x0E, 0x2F, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2A, 0x2B, 0x2C, 0x2D, 0x30, 0x0C, 
     0x21, 0x1F, 0x19, 0x09, 0x0B, 0x2E, 0x14, 0x22, 0x0A, 0x10, 0x12, 0x20, 0x1D, 
     0x01, 0x24, 0x18, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3A, 
     0x3B, 0x3C, 0x3D, 0x3E, 0x3F, 0x40, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 
     0x48, 0x49, 0x4A, 0x4B, 0x4C, 0x4D, 0x4E, 0x1E, 0x1C, 0x23, 0x15, 0x0D, 0x1A
   };
     
   
 }
