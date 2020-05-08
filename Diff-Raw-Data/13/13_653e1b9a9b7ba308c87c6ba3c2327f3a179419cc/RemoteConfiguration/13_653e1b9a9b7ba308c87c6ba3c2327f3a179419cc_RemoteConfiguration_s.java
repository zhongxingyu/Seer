 package com.hifiremote.jp1;
 
 import java.beans.*;
 import java.io.*;
 import java.lang.reflect.*;
 import java.util.*;
 import javax.swing.*;
 
 public class RemoteConfiguration
 {
   public RemoteConfiguration( File file )
     throws IOException
   {
     BufferedReader in = new BufferedReader( new FileReader( file ));
     PropertyReader pr = new PropertyReader( in );
     if ( file.getName().toLowerCase().endsWith( ".rmir" ))
       parse( pr );
     else
       importIR( pr );
     in.close();
   }
 
   public void parse( PropertyReader pr )
     throws IOException
   {
     IniSection section = pr.nextSection();
 
     if ( section == null )
       throw new IOException( "The file is empty." );
 
     if ( !"General".equals( section.getName()))
       throw new IOException( "Doesn't start with a [General] section/" );
 
     remote = RemoteManager.getRemoteManager().findRemoteByName( section.getProperty( "Remote.name" ));
     notes = section.getProperty( "Notes" );
 
     loadBuffer( pr );
 
     while (( section = pr.nextSection()) != null )
     {
       String sectionName = section.getName();
       if ( sectionName.equals( "Settings" ))
       {
         for ( Setting setting : remote.getSettings())
           setting.setValue( Integer.parseInt( section.getProperty( setting.getTitle())));
       }
       else if ( sectionName.equals( "DeviceUpgrade" ))
       {
         DeviceUpgrade upgrade = new DeviceUpgrade();
         upgrade.load( section );
         devices.add( upgrade );
       }
       else
       {
         try
         {
           Class c = Class.forName( "com.hifiremote.jp1." + sectionName );
           Constructor ct = c.getConstructor( Properties.class );
           Object o = ct.newInstance( section );
           if ( o instanceof SpecialProtocolFunction )
             specialFunctions.add(( SpecialProtocolFunction )o );
           else if ( o instanceof KeyMove )
             keymoves.add(( KeyMove )o );
           else if ( sectionName.equals( "Macro" ))
             macros.add(( Macro )o );
           else if ( sectionName.equals( "ProtocolUpgrade" ))
             protocols.add(( ProtocolUpgrade )o );
           else if ( sectionName.equals( "LearnedSignal" ))
             learned.add(( LearnedSignal )o );
         }
         catch ( Exception e )
         {
           e.printStackTrace( System.err );
           throw new IOException( "Unable to create instance of " + sectionName );
         }
       }
     }
   }
 
   private Property loadBuffer( PropertyReader pr )
     throws IOException
   {
     Property property = pr.nextProperty();
 
     if ( property.name.equals( "[Buffer]" ))
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
       Remote[] remotes = rm.findRemoteBySignature( signature );
       if ( remotes.length == 0 )
       {
         for ( int i = 0; i < sig.length; ++i )
           sig[ i ] = ( char )first[ i ];
         signature2 = new String( sig );
         remotes = rm.findRemoteBySignature( signature2 );
       }
       if (( remotes == null ) || ( remotes.length == 0 ))
       {
         String message = "No remote found for with signature " + signature + " or " + signature2;
         JOptionPane.showMessageDialog( null, message, "Unknown remote", JOptionPane.ERROR_MESSAGE );
         throw new IllegalArgumentException(  );
       }
       else if ( remotes.length == 1 )
         remote = remotes[ 0 ];
       else
       {
         if ( signature2 != null )
           signature = signature2;
         String message = "The file you are loading is for a remote with signature \"" + signature +
         "\".\nThere are multiple remotes with that signature.  Please choose the best match from the list below:";
 
         remote = ( Remote )JOptionPane.showInputDialog( null,
                                                         message,
                                                         "Unknown Remote",
                                                         JOptionPane.ERROR_MESSAGE,
                                                         null,
                                                         remotes,
                                                         remotes[ 0 ]);
         if ( remote == null )
           throw new IllegalArgumentException( "No matching remote selected for signature " + signature );
       }
     }
     remote.load();
     System.err.println( "Remote is " + remote );
 
     if ( remote.getBaseAddress() != baseAddr )
       throw new IOException( "The base address of the remote image doesn't match the remote's baseAddress." );
 
     data = new short[ remote.getEepromSize()];
     System.arraycopy( first, 0, data, 0, first.length );
 
     first = null;
     while ((  property = pr.nextProperty()) != null )
     {
       if ( property.name.length() == 0 )
         break;
       int offset = Integer.parseInt( property.name, 16 ) - baseAddr;
       Hex.parseHex( property.value, data, offset );
     }
 
     savedData = new short[ data.length ];
     System.arraycopy( data, 0, savedData, 0, data.length );
 
     return property;
   }
   
   private KeyMove findKeyMove( List< KeyMove > advCodes, String deviceName, String keyName )
   {
     DeviceButton[] deviceButtons = remote.getDeviceButtons();
     
     for ( KeyMove keyMove : advCodes )
     {
       DeviceButton devButton = deviceButtons[ keyMove.getDeviceButtonIndex()];
       if ( !devButton.getName().equals( deviceName ))
         continue;
       int keyCode = keyMove.getKeyCode();
       String buttonName = remote.getButtonName( keyCode );
       if ( buttonName.equalsIgnoreCase( keyName ))
         return keyMove;
     }
     System.err.println( "No keymove found matching " + deviceName + ':' + keyName );
     return null;
   }
 
   private Macro findMacro( String keyName )
   {
     DeviceButton[] deviceButtons = remote.getDeviceButtons();
     
     for ( Macro macro : macros )
     {
       int keyCode = macro.getKeyCode();
       String buttonName = remote.getButtonName( keyCode );
       if ( buttonName.equalsIgnoreCase( keyName ))
         return macro;
     }
     System.err.println( "No macro found assigned to key " + keyName );
     return null;
   }
 
   private ProtocolUpgrade findProtocolUpgrade( int pid )
   {
     for ( ProtocolUpgrade pu : protocols )
     {
       if ( pu.getPid() == pid )
         return pu;
     }
     System.err.println( "No protocol upgrade found w/ pid $" + Integer.toHexString( pid ));
     return null;
   }
 
   private LearnedSignal findLearnedSignal( String deviceName, String keyName )
   {
     DeviceButton[] deviceButtons = remote.getDeviceButtons();
     
     for ( LearnedSignal ls : learned )
     {
       DeviceButton devButton = deviceButtons[ ls.getDeviceButtonIndex()];
       if ( !devButton.getName().equals( deviceName ))
         continue;
       int keyCode = ls.getKeyCode();
       String buttonName = remote.getButtonName( keyCode );
       if ( buttonName.equalsIgnoreCase( keyName ))
         return ls;
     }
     System.err.println( "No learned signal found matching " + deviceName + ':' + keyName );
     return null;
   }
 
   private void importIR( PropertyReader pr )
     throws IOException
   {
     Property property = loadBuffer( pr );
 
     decodeSettings();
     decodeUpgrades();
    List< KeyMove > advCodes = decodeAdvancedCodes();
     decodeLearnedSignals();
 
     while (( property != null ) && ( !property.name.startsWith( "[" )))
       property = pr.nextProperty();
 
     if ( property == null )
       return;
 
     IniSection section = pr.nextSection();
     section.setName( property.name.substring( 1, property.name.length() - 1 ));
 
     while ( section != null )
     {
       String name = section.getName();
       if ( name.equals( "Notes" ))
       {
         for ( Enumeration< ? > keys = ( Enumeration< ? > )section.propertyNames(); keys.hasMoreElements(); )
         {
           String key = ( String )keys.nextElement();
           String text = importNotes( section.getProperty( key ));
           int base = 10;
           if ( key.charAt( 0 ) == '$' )
           {
             base = 16;
             key = key.substring( 1 );
           }
           int index = Integer.parseInt( key, base );
           int flag = index >> 12;
           index &= 0x0FFF;
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
         }
       }
       else if ( name.equals( "General" ))
       {
         for ( Enumeration< ? > keys = section.propertyNames(); keys.hasMoreElements(); )
         {
           String key = ( String )keys.nextElement();
           String text = importNotes( section.getProperty( key ));
           if ( key.equals( "Notes" ))
             notes = text;
         }
       }
       else if ( name.equals( "KeyMoves" ))
       {
         for ( Enumeration< ? > keys = section.propertyNames(); keys.hasMoreElements(); )
         {
           String key = ( String )keys.nextElement();
           String text = importNotes( section.getProperty( key ));
           StringTokenizer st = new StringTokenizer( key, ":" );
           String deviceName = st.nextToken();
           String keyName = st.nextToken();
          KeyMove km = findKeyMove( advCodes, deviceName, keyName );
           if ( km != null )
             km.setNotes( text );
         }
       }
       else if ( name.equals( "Macros" ))
       {
         for ( Enumeration< ? > keys = section.propertyNames(); keys.hasMoreElements(); )
         {
           String keyName = ( String )keys.nextElement();
           String text = importNotes( section.getProperty( keyName ));
           Macro macro = findMacro( keyName );
           if ( macro != null )
             macro.setNotes( text );
         }
       }
       else if ( name.equals( "Devices" ))
       {
         for ( Enumeration< ? > keys = section.propertyNames(); keys.hasMoreElements(); )
         {
           String key = ( String )keys.nextElement();
           String text = importNotes( section.getProperty( key ));
           StringTokenizer st = new StringTokenizer( key, ": " );
           String deviceTypeName = st.nextToken();
           int setupCode = Integer.parseInt( st.nextToken());
           DeviceUpgrade device = findDeviceUpgrade( remote.getDeviceType( deviceTypeName ).getNumber(), setupCode );
           if ( device != null )
             device.setDescription( text );
         }
       }
       else if ( name.equals( "Protocols" ))
       {
         for ( Enumeration< ? > keys = ( Enumeration< ? > )section.propertyNames(); keys.hasMoreElements(); )
         {
           String key = ( String )keys.nextElement();
           String text = importNotes( section.getProperty( key ));
           StringTokenizer st = new StringTokenizer( key, "$" );
           st.nextToken(); // discard the "Protocol: " header
           int pid = Integer.parseInt( st.nextToken(), 16 );
           ProtocolUpgrade protocol = findProtocolUpgrade( pid );
           if ( protocol != null )
             protocol.setNotes( text );
         }
       }
       else if ( name.equals( "Learned" ))
       {
         for ( Enumeration< ? > keys = section.propertyNames(); keys.hasMoreElements(); )
         {
           String key = ( String )keys.nextElement();
           String text = importNotes( section.getProperty( key ));
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
     migrateKeyMovesToDeviceUpgrades();
     
     // remove protocol upgrades that are used by device upgrades
     for ( Iterator< ProtocolUpgrade > it = protocols.iterator(); it.hasNext(); )
     {
       if ( it.next().isUsed())
         it.remove();
     }
   }
 
   private int exportAdvancedCodeNotes( List< ? extends AdvancedCode > codes, int index, PrintWriter out )
     throws IOException
   {
     for ( AdvancedCode code : codes )
     {
       String text = code.getNotes();
       if ( text != null )
         out.printf( "$%4X=%s\n", index, exportNotes( text ));
       ++index;
     }
     return index;
   }
 
   public void exportIR( File file )
     throws IOException
   {
     updateImage();
     PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( file )));
 
     int base = remote.getBaseAddress();
     for ( int i = 0; i < data.length; i += 16 )
     {
       out.print( toHex( i + base ));
       out.print( ":" );
       for ( int j = 0; j < 16; ++j )
         out.printf( "  %02X", data[ i + j ] & 0xFF );
       out.println();
     }
 
     out.println();
     out.println( "[Notes]" );
     // start with the overall notes
     if ( notes != null )
       out.println( "$0000=" + exportNotes( notes ));
 
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
         out.printf( "$%4X=%s\n", i, exportNotes( text ));
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
         if ( !requiredProtocols.containsKey( pid ))
           requiredProtocols.put( pid.get( 0 ), new ProtocolUpgrade( pid.get( 0 ), pCode, p.getName()));
       }
     }
 
     for ( ProtocolUpgrade pu : protocols )
       requiredProtocols.put( pu.getPid(), pu );
 
     i = 0x4000;
     for ( ProtocolUpgrade protocol : requiredProtocols.values())
     {
       String text = protocol.getNotes();
       if ( text != null )
         out.printf( "$%4X=%s\n", i, exportNotes( text ));
       ++i;
     }
 
     // Do the learned signals
     i = 0x5000;
     for ( LearnedSignal signal : learned )
     {
       String text = signal.getNotes();
       if ( text != null )
         out.printf( "$%4X=%s\n", i, exportNotes( text ));
       ++i;
     }
     out.close();
   }
 
   private DeviceUpgrade findDeviceUpgrade( DeviceButton deviceButton )
   {
     return findDeviceUpgrade( deviceButton.getDeviceTypeIndex( data ),
                               deviceButton.getSetupCode( data ));
   }
 
   private DeviceUpgrade findDeviceUpgrade( int deviceTypeSetupCode )
   {
     int deviceTypeIndex = deviceTypeSetupCode >> 12;
     int setupCode = deviceTypeSetupCode & 0x7FF;
     return findDeviceUpgrade( deviceTypeIndex, setupCode );
   }
 
   private DeviceUpgrade findDeviceUpgrade( int deviceTypeIndex, int setupCode )
   {
     for ( DeviceUpgrade deviceUpgrade : devices )
     {
       if (( deviceTypeIndex == deviceUpgrade.getDeviceType().getNumber()) &&
           ( setupCode == deviceUpgrade.getSetupCode()))
         return deviceUpgrade;
     }
     return null;
   }
 
   public int findBoundDeviceButtonIndex( DeviceUpgrade upgrade )
   {
     int deviceTypeIndex = upgrade.getDeviceType().getNumber();
     int setupCode = upgrade.getSetupCode();
     DeviceButton[] deviceButtons = remote.getDeviceButtons();
     for ( int i = 0; i < deviceButtons.length; ++i )
     {
       DeviceButton deviceButton = deviceButtons[ i ];
       if (( deviceButton.getDeviceTypeIndex( data ) == deviceTypeIndex ) &&
           ( deviceButton.getSetupCode( data ) == setupCode ))
         return i;
     }
     return -1;
   }
 
   public RemoteConfiguration( Remote remote )
   {
     this.remote = remote;
     data = new short[ remote.getEepromSize()];
   }
 
   public void parseData()
   {
     decodeSettings();
     decodeUpgrades();
     
     // remove protocol upgrades that are used by device upgrades
     for ( Iterator< ProtocolUpgrade > it = protocols.iterator(); it.hasNext(); )
     {
       if ( it.next().isUsed())
         it.remove();
     }
 
     decodeAdvancedCodes();
     decodeLearnedSignals();
   }
 
   public void decodeSettings()
   {
     Setting[] settings = remote.getSettings();
     for ( Setting setting : settings )
       setting.decode( data );
   }
 
   public List< SpecialProtocol > getSpecialProtocols()
   {
     // Determine which upgrades are special protocol upgrades
     List< SpecialProtocol > specialUpgrades = new ArrayList< SpecialProtocol >();
     List< SpecialProtocol > specialProtocols = remote.getSpecialProtocols();
     for ( SpecialProtocol sp : specialProtocols )
     {
       DeviceUpgrade device = sp.getDeviceUpgrade( devices );
       if ( device != null )
         specialUpgrades.add( sp );
     }
     return specialUpgrades;
   }
 
  private List< KeyMove > decodeAdvancedCodes()
   {
    List< KeyMove > advCodes = new ArrayList< KeyMove >();
     AddressRange advCodeRange = remote.getAdvanceCodeAddress();
     int offset = advCodeRange.getStart();
     int endOffset = advCodeRange.getEnd();
 
     // Determine which upgrades are special protocol upgrades
     List< DeviceUpgrade > specialUpgrades = new ArrayList< DeviceUpgrade >();
     List< SpecialProtocol > specialProtocols = remote.getSpecialProtocols();
     for ( SpecialProtocol sp : specialProtocols )
     {
       System.err.println( "Checking for Special Procotol " + sp.getName() + " w/ PID=" + sp.getPid().toString());
       DeviceUpgrade device = sp.getDeviceUpgrade( devices );
       if ( device != null )
       {
         specialUpgrades.add( device );
         System.err.println( "SpecialFunction Upgrade at " + device.getDeviceType().getName() + "/" + device.getSetupCode());
       }
     }
 
     FavKey favKey = remote.getFavKey();
     while ( offset <= endOffset )
     {
       short keyCode = data[ offset++ ];
       if ( keyCode == remote.getSectionTerminator())
         break;
       System.err.println( "Decoding advCode at $" + Integer.toHexString( offset ) + 
                         ", keyCode=" + keyCode + ':' + remote.getButtonName( keyCode ));
 
       int boundDeviceIndex = 0;
       boolean isMacro = false;
       boolean isFav = false;
       int length = 0;
       if ( remote.getAdvCodeBindFormat() == Remote.NORMAL )
       {
         boundDeviceIndex = data[ offset ] >> 4;
         length = ( data[ offset++ ] & 0x0F );
         if ( boundDeviceIndex == 1 )
           isMacro = true;
         if ( boundDeviceIndex == 3 )
         {
           isFav = true;
           if (( favKey != null ) && ( keyCode == favKey.getKeyCode()))
             length *= favKey.getEntrySize();
         }
         boundDeviceIndex >>= 1;
       }
       else // LONG
       {
         int type = data[ offset++ ];
         if ( type == 0x80 )
           isMacro = true;
         boundDeviceIndex = type & 0x0F;
         length = data[ offset++ ];
         if (( favKey != null ) && ( keyCode == favKey.getKeyCode()))
         {
           isFav = true;
           length *= favKey.getEntrySize();
         }
       }
 
       System.err.println( "length=" + length );
 
       if ( isMacro || isFav )
       {
         Hex keyCodes = Hex.subHex( data, offset, length );
         Macro macro = new Macro( keyCode, keyCodes, null );
         macros.add( macro );
       }
       else
       {
         KeyMove keyMove = null;
         Hex hex = Hex.subHex( data, offset, length );
         if ( remote.getAdvCodeFormat() == remote.HEX_FORMAT )
           keyMove = new KeyMove( keyCode, boundDeviceIndex, hex, null );
         else if ( remote.getEFCDigits() == 3 )
         {
           if ( length == 1 )
             keyMove = new KeyMoveKey( keyCode, boundDeviceIndex, hex, null );
           else
             keyMove = new KeyMoveEFC( keyCode, boundDeviceIndex, hex, null );
         }
         else // EFCDigits == 5
           keyMove = new KeyMoveEFC5( keyCode, boundDeviceIndex, hex, null );
 
         DeviceUpgrade moveUpgrade = findDeviceUpgrade( keyMove.getDeviceType(), keyMove.getSetupCode());
         if (( moveUpgrade != null ) && specialUpgrades.contains( moveUpgrade ))
         {
           SpecialProtocolFunction sf = getSpecialProtocol( moveUpgrade ).createFunction( keyMove );
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
       offset += length;
     }
     return advCodes;
   }
 
   private void migrateKeyMovesToDeviceUpgrades()
   {
     for ( Iterator< KeyMove > it = keymoves.iterator(); it.hasNext(); )
     {
       KeyMove keyMove = it.next();
       int keyCode = keyMove.getKeyCode();
 
       // check if the keymove comes from a device upgrade
       DeviceButton boundDeviceButton = remote.getDeviceButtons()[ keyMove.getDeviceButtonIndex()];
       DeviceUpgrade boundUpgrade = findDeviceUpgrade( boundDeviceButton );
       DeviceUpgrade moveUpgrade = findDeviceUpgrade( keyMove.getDeviceType(), keyMove.getSetupCode());
       if (( boundUpgrade != null ) && ( boundUpgrade == moveUpgrade ))
       {
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
           int mask = keyCode & 0xC0;
           int baseCode = keyCode & 0x3F;
           if ( baseCode != 0 )
           {
             b = remote.getButton( baseCode );
             if (( baseCode | remote.getShiftMask()) == keyCode )
               state = Button.SHIFTED_STATE;
             if (( baseCode | remote.getXShiftMask()) == keyCode )
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
               baseCode = keyCode & ~ remote.getXShiftMask();
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
 
   public int getDeviceButtonIndex( DeviceUpgrade upgrade )
   {
     DeviceButton[] deviceButtons = remote.getDeviceButtons();
     for ( int i = 0; i < deviceButtons.length; ++i )
     {
       DeviceButton button = deviceButtons[ i ];
       if (( button.getDeviceTypeIndex( data ) == upgrade.getDeviceType().getNumber()) &&
           ( button.getSetupCode( data ) == upgrade.getSetupCode()))
         return i;
     }
     return -1;
   }
 
   public SpecialProtocol getSpecialProtocol( DeviceUpgrade upgrade )
   {
     for ( SpecialProtocol sp : remote.getSpecialProtocols())
     {
       if ( upgrade.getProtocol().getID().equals( sp.getPid()))
         return sp;
     }
     return null;
   }
 
   public int getAdvancedCodeBytesUsed()
   {
     AddressRange advCodeRange = remote.getAdvanceCodeAddress();
     int offset = advCodeRange.getStart();
     int endOffset = advCodeRange.getEnd();
     while (( offset <= endOffset ) && ( data[ offset ] != remote.getSectionTerminator()))
     {
       offset++; // skip the keyCode
 
       int length = 0;
       if ( remote.getAdvCodeBindFormat() == Remote.NORMAL )
         length = data[ offset++ ] & 0x0F;
       else
       {
         offset++; // skip the type
         length = data[ offset++ ];
       }
       offset += length;
     }
     return offset - advCodeRange.getStart();
   }
 
   public void updateImage()
   {
     updateSettings();
     updateAdvancedCodes();
     updateUpgrades();
     updateLearnedSignals();
     updateCheckSums();
   }
 
   private int updateKeyMoves( List< ? extends KeyMove >moves, int offset )
   {
     for ( KeyMove keyMove : moves )
     {
       data[ offset++ ] = ( short )keyMove.getKeyCode();
       int lengthOffset;
       if ( remote.getAdvCodeBindFormat() == Remote.NORMAL )
       {
         int temp = keyMove.getDeviceButtonIndex() << 5;
         data[ offset ] = ( short )temp;
         lengthOffset = offset++;
       }
       else
       {
         data[ offset++ ] = ( short )( 0x10 | ( keyMove.getDeviceButtonIndex() << 4 ));
         lengthOffset = offset++;
         data[ lengthOffset ] = 0;
       }
       Hex hex = keyMove.getRawHex();
       int hexLength = hex.length();
       Hex.put( hex, data, offset );
       offset += hexLength;
       data[ lengthOffset ] |= ( short )hexLength;
     }
     return offset;
   }
 
   public List< KeyMove > getUpgradeKeyMoves()
   {
     List< KeyMove > rc = new ArrayList< KeyMove >();
     for ( DeviceUpgrade device : devices )
     {
       int devButtonIndex = getDeviceButtonIndex( device );
       if ( devButtonIndex == -1 )
         continue;
       for ( KeyMove keyMove : device.getKeyMoves())
       {
         keyMove.setDeviceButtonIndex( devButtonIndex );
         rc.add( keyMove );
       }
     }
     return rc;
   }
 
   public int updateAdvancedCodes()
   {
     AddressRange range = remote.getAdvanceCodeAddress();
     int offset = range.getStart();
     offset = updateKeyMoves( keymoves, offset );
     upgradeKeyMoves = getUpgradeKeyMoves();
     offset = updateKeyMoves( upgradeKeyMoves, offset );
     offset = updateKeyMoves( specialFunctions, offset );
 
     for ( Macro macro : macros )
     {
       data[ offset++ ] = ( short )macro.getKeyCode();
       int lengthOffset = 0;
       if ( remote.getAdvCodeBindFormat() == Remote.NORMAL )
       {
         data[ offset ] = 0x10;
         lengthOffset = offset++;
       }
       else
       {
         data[ offset++ ] = 0x80;
         lengthOffset = offset++;
       }
       Hex hex = macro.getData();
       int hexLength = hex.length();
       Hex.put( hex, data, offset );
       offset += hexLength;
       data[ lengthOffset ] |= ( short )hexLength;
     }
 
     data[ offset ] = remote.getSectionTerminator();
 
 //    for ( int i = offset + 1; i < range.getEnd(); ++i )
 //      data[ i ] = 0xFF;
 
     return offset - range.getStart();
   }
 
   public void updateCheckSums()
   {
     CheckSum[] sums = remote.getCheckSums();
     for ( int i = 0; i < sums.length; ++i )
       sums[ i ].setCheckSum( data );
   }
 
   public void updateSettings()
   {
     Setting[] settings = remote.getSettings();
     for ( Setting setting : settings )
       setting.store( data );
   }
 
   private ProtocolUpgrade getProtocol( int pid )
   {
     for ( ProtocolUpgrade pu : protocols )
     {
       if ( pu.getPid() == pid )
         return pu;
     }
     return null;
   }
 
   private void decodeUpgrades()
   {
     AddressRange addr = remote.getUpgradeAddress();
 
     // first parse the protocols
     int offset = Hex.get( data, addr.getStart() + 2 ) - remote.getBaseAddress(); // get offset of protocol table
     int count = Hex.get( data, offset ); // get number of entries in upgrade table
     offset += 2;  // skip to first entry
     for ( int i = 0; i < count; ++i )
     {
       int pid = Hex.get( data, offset );
       int codeOffset = Hex.get( data, offset + 2 * count ) - remote.getBaseAddress();
       int nextCode = 0;
       if ( i == count - 1 ) // the last protocol, so use the start of the device table
       {
         nextCode = addr.getStart();
         nextCode = Hex.get( data, addr.getStart()) - remote.getBaseAddress();
       }
       else
         nextCode = Hex.get( data, offset + 2 * ( count + 1 )) - remote.getBaseAddress();
       Hex code = Hex.subHex( data, codeOffset, nextCode - codeOffset );
       protocols.add( new ProtocolUpgrade( pid, code, null ));
 
       offset += 2; // for the next upgrade
     }
 
     // To keep track of the protocol upgrades that are actually used by device upgrades
     List< ProtocolUpgrade > usedProtocols = new ArrayList< ProtocolUpgrade >();
 
     // now parse the devices
     offset = Hex.get( data, addr.getStart()) - remote.getBaseAddress(); // get offset of device table
     count = Hex.get( data, offset ); // get number of entries in upgrade table
     for ( int i = 0; i < count; ++i )
     {
       offset += 2;
       int setupCode = Hex.get( data, offset ) & 0x7FF;
       DeviceType devType = remote.getDeviceTypeByIndex( data[ offset ] >> 4 );
       int codeOffset = offset + 2 * count; // compute offset to offset of upgrade code
       codeOffset = Hex.get( data, codeOffset ) - remote.getBaseAddress(); // get offset of upgrade code
       int pid = data[ codeOffset ];
       if (( data[ offset ] & 8 ) == 8 ) // pid > 0xFF
         pid += 0x100;
 
       int nextCode = 0;
       if ( i == count - 1 ) // this is the last device upgrade
       {  // try using the 1st protocol
         int pOffset = Hex.get( data, addr.getStart() + 2 ) - remote.getBaseAddress();
         int pCount = Hex.get( data, pOffset );
         pOffset += 2; // skip count & point to first PID
         if ( pCount > 0 )
           nextCode = Hex.get( data, pOffset + ( 2 * pCount )) - remote.getBaseAddress();
         else // there are no protocol upgrades, use the device upgrade table itself
           nextCode = Hex.get( data, addr.getStart()) - remote.getBaseAddress();
       }
       else
         nextCode = Hex.get( data, offset + 2 * ( count + 1 )) - remote.getBaseAddress(); // next device upgrade
       Hex deviceHex = Hex.subHex( data, codeOffset, nextCode - codeOffset );
       ProtocolUpgrade pu = getProtocol( pid );
       Hex protocolCode = null;
       if ( pu != null )
       {
         pu.setUsed( true );
         protocolCode = pu.getCode();
       }
 
       String alias = remote.getDeviceTypeAlias( devType );
 
       short[] pidHex = new short[ 2 ];
       pidHex[ 0 ] = ( short )(( pid > 0xFF ) ? 1 : 0 );
       pidHex[ 1 ] = ( short )( pid & 0xFF );
 
       DeviceUpgrade upgrade = new DeviceUpgrade();
       upgrade.importRawUpgrade( deviceHex, remote, alias, new Hex( pidHex ), protocolCode );
       upgrade.setSetupCode( setupCode );
 
       devices.add( upgrade );
     }
   }
 
   public int getUpgradeCodeBytesUsed()
   {
     AddressRange addr = remote.getUpgradeAddress();
 
     int offset = Hex.get( data, addr.getStart() + 2 ) - remote.getBaseAddress(); // get offset of protocol table
     int count = Hex.get( data, offset ); // get number of protocol upgrades
     offset += 2;  // skip to first entry
     offset += ( 4 * count ); // the are 4 bytes for each entry ( 2 for PID, 2 for the code pointer )
     return offset - addr.getStart() - 1;
   }
 
   public int updateUpgrades()
   {
     AddressRange addr = remote.getUpgradeAddress();
     int offset = addr.getStart() + 4; // skip over the table pointers
     int devCount = devices.size();
 
     LinkedHashMap< Integer, ProtocolUpgrade > requiredProtocols = new LinkedHashMap< Integer, ProtocolUpgrade >();
     for ( DeviceUpgrade dev : devices )
     {
       Hex pCode = dev.getCode();
       if ( pCode != null )
       {
         Protocol p = dev.getProtocol();
         Hex pid = p.getID();
         if ( !requiredProtocols.containsKey( pid ))
           requiredProtocols.put( pid.get( 0 ), new ProtocolUpgrade( pid.get( 0 ), pCode, p.getName()));
       }
     }
 
     for ( ProtocolUpgrade pu : protocols )
       requiredProtocols.put( pu.getPid(), pu );
 
     int prCount = requiredProtocols.size();
 
     // Handle the special case where there are no upgrades installed
     if (( devCount == 0 ) && ( prCount == 0 ))
     {
       Hex.put( offset + remote.getBaseAddress(), data, addr.getStart());
       Hex.put( offset + remote.getBaseAddress(), data, addr.getStart() + 2 );
       Hex.put( 0, data, offset );
       return offset - addr.getStart();
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
     for ( ProtocolUpgrade upgrade : requiredProtocols.values())
     {
       prOffsets[ i++ ] = offset;
       Hex hex = upgrade.getCode();
       Hex.put( hex, data, offset );
       offset += hex.length();
     }
 
     // set the pointer to the device table.
     Hex.put( offset + remote.getBaseAddress(), data, addr.getStart());
 
     // create the device table
     Hex.put( devCount, data, offset );
     offset += 2;
     // store the setup codes
     for ( DeviceUpgrade dev : devices )
     {
       Hex.put( dev.getHexSetupCode(), data, offset );
       offset += 2;
     }
     //store the offsets
     for ( int devOffset : devOffsets )
     {
       Hex.put( devOffset + remote.getBaseAddress(), data, offset );
       offset+= 2;
     }
 
     // set the pointer to the protocol table
     Hex.put( offset + remote.getBaseAddress(), data, addr.getStart() + 2 );
 
     // create the protocol table
     Hex.put( prCount, data, offset );
     offset += 2;
     for ( ProtocolUpgrade pr : requiredProtocols.values())
     {
       Hex.put( pr.getPid(), data, offset );
       offset += 2;
     }
     for ( i = 0; i < prCount; ++i )
     {
       Hex.put( prOffsets[ i ] + remote.getBaseAddress(), data, offset );
       offset+= 2;
     }
 
     return offset - addr.getStart();
   }
 
   public void decodeLearnedSignals()
   {
     AddressRange addr = remote.getLearnedAddress();
     if ( addr == null )
       return;
 
     int offset = addr.getStart();
     while (( offset < addr.getEnd()) && ( data[ offset ] != remote.getSectionTerminator()))
     {
       short keyCode = data[ offset++ ];
       int device = data[ offset++ ] >> 4;
       int length = data[ offset++ ];
       learned.add( new LearnedSignal( keyCode, device, new Hex( data, offset, length ), null ));
       offset += length;
     }
   }
 
   public int getLearnedSignalBytesUsed()
   {
     AddressRange addr = remote.getLearnedAddress();
     if ( addr == null )
       return 0;
 
     int offset = addr.getStart();
     while (( offset < addr.getEnd()) && ( data[ offset ] != remote.getSectionTerminator()))
     {
       offset += 2; // skip keycode and device button
       int length = data[ offset++ ];
       offset += length;
     }
 
     return offset - addr.getStart();
   }
 
   public int updateLearnedSignals()
   {
     AddressRange addr = remote.getLearnedAddress();
     if ( addr == null )
       return 0;
 
     int offset = addr.getStart();
     for ( LearnedSignal l : learned )
     {
       data[ offset++ ] = ( short )l.getKeyCode();
       data[ offset++ ] = ( short )( l.getDeviceButtonIndex() << 4 );
       Hex hex = l.getData();
       data[ offset++ ] = ( short )hex.length();
       Hex.put( hex, data, offset );
       offset += hex.length();
     }
     data[ offset ] = remote.getSectionTerminator();
     return offset - addr.getStart();
   }
 
   private void printNote( int index, String text, PrintWriter out )
     throws IOException
   {
     if (( text == null ) || ( text.length() == 0 ))
       return;
 
     out.println();
     out.print( '$' );
     out.print( toHex( index ));
     out.print( '=' );
     out.print( exportNotes( text ));
   }
 
   public void save( File file )
     throws IOException
   {
     PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( file )));
     PropertyWriter pw = new PropertyWriter( out );
 
     pw.printHeader( "General" );
     pw.print( "Remote.name", remote.getName());
     pw.print( "Remote.signature", remote.getSignature());
     pw.print( "Notes", notes );
 
     pw.printHeader( "Buffer" );
     int base = remote.getBaseAddress();
     for ( int i = 0; i < data.length; i += 16 )
     {
       pw.print( toHex( i + base ), Hex.toString( data, i, 16 ));
     }
 
     pw.printHeader( "Settings" );
     for ( Setting setting : remote.getSettings())
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
 
   private String importNotes( String text )
   {
     StringTokenizer st = new StringTokenizer( text, "" );
     StringWriter sw = new StringWriter( text.length() + st.countTokens());
     PrintWriter out = new PrintWriter( sw );
     boolean first = true;
     while ( st.hasMoreTokens())
     {
       if ( first )
         first = false;
       else
         out.println();
       out.print( st.nextToken());
     }
     String rc = sw.getBuffer().toString();
     return rc;
   }
 
   private String exportNotes( String text )
     throws IOException
   {
     BufferedReader br = new BufferedReader( new StringReader( text ));
     StringBuilder buff = new StringBuilder( text.length());
     String line = br.readLine();
     while ( line != null )
     {
       buff.append( line );
       line = br.readLine();
       if ( line != null )
         buff.append( '' );
     }
     return buff.toString();
   }
 
   public Remote getRemote()
   {
     return remote;
   }
 
   public String getNotes(){ return notes; }
   public void setNotes( String text ){ notes = text; }
 
   public static String toHex( int value )
   // Returns an hexadecimal string representation with 4 digits and leading 0s
   {
     return ( Integer.toHexString( 0x10000 | value ).substring( 1 ).toUpperCase());
   }
 
   public static String toHex( short value )
   // Returns an hexadecimal string representation with 2 digits and leading 0s
   {
     return ( Integer.toHexString( 0x100 | value ).substring( 1 ).toUpperCase());
   }
 
   // PropertyChangeListener
 //  public void propertyChange( PropertyChangeEvent event )
 //  {
 //    changed = true;
 //    updateAdvCodeArea();
 //  }
 
   public short[] getData(){ return data; }
   public short[] getSavedData(){ return savedData; }
   public List< KeyMove > getKeyMoves(){ return keymoves; }
   public List< Macro > getMacros(){ return macros; }
   public List< DeviceUpgrade > getDeviceUpgrades(){ return devices; }
   public List< ProtocolUpgrade > getProtocolUpgrades(){ return protocols; }
   public List< LearnedSignal > getLearnedSignals(){ return learned; }
   public List< SpecialProtocolFunction > getSpecialFunctions(){ return specialFunctions; }
 
   private Remote remote = null;
   private short[] data = null;
   private short[] savedData = null;
 
   private List< KeyMove > keymoves = new ArrayList< KeyMove >();
   private List< KeyMove > upgradeKeyMoves = new ArrayList< KeyMove >();
   private List< Macro > macros = new ArrayList< Macro >();
   private List< DeviceUpgrade > devices = new ArrayList< DeviceUpgrade >();
   private List< ProtocolUpgrade > protocols = new ArrayList< ProtocolUpgrade >();
   private List< LearnedSignal > learned = new ArrayList< LearnedSignal >();
   private List< SpecialProtocolFunction > specialFunctions = new ArrayList< SpecialProtocolFunction >();
 
   private boolean changed = false;
   private String notes = null;
 }
