 package com.hifiremote.jp1;
 
 import java.util.Properties;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class KeyMove.
  */
 public class KeyMove extends AdvancedCode implements Cloneable
 {
 
   /**
    * Instantiates a new key move.
    * 
    * @param keyCode
    *          the key code
    * @param deviceButtonIndex
    *          the device button index
    * @param data
    *          the data
    * @param notes
    *          the notes
    */
   public KeyMove( int keyCode, int deviceButtonIndex, Hex data, String notes )
   {
     super( keyCode, data, notes );
     cmd = data.subHex( CMD_INDEX );
     this.deviceButtonIndex = deviceButtonIndex;
     short[] hex = data.getData();
     deviceType = hex[ DEVICE_TYPE_INDEX ] >> 4;
    setupCode = Hex.get( hex, SETUP_CODE_INDEX ) & 0x07FF;
   }
 
   /**
    * Instantiates a new key move.
    * 
    * @param keyCode
    *          the key code
    * @param deviceButtonIndex
    *          the device button index
    * @param deviceType
    *          the device type
    * @param setupCode
    *          the setup code
    * @param cmd
    *          the cmd
    * @param notes
    *          the notes
    */
   public KeyMove( int keyCode, int deviceButtonIndex, int deviceType, int setupCode, Hex cmd, String notes )
   {
     super( keyCode, null, notes );
     setData( getRawHex( deviceType, setupCode, cmd ) );
     this.cmd = cmd;
     setDeviceButtonIndex( deviceButtonIndex );
     this.deviceType = deviceType;
     this.setupCode = setupCode;
   }
 
   public Hex getRawHex( int deviceType, int setupCode, Hex cmd )
   {
     Hex hex = new Hex( CMD_INDEX + cmd.length() );
     update( deviceType, setupCode, hex );
     hex.put( cmd, CMD_INDEX );
     return hex;
   }
 
   /**
    * Instantiates a new key move.
    * 
    * @param props
    *          the props
    */
   public KeyMove( Properties props )
   {
     super( props );
     cmd = data.subHex( CMD_INDEX );
     deviceButtonIndex = Integer.parseInt( props.getProperty( "DeviceButtonIndex" ) );
     setDeviceType( Integer.parseInt( props.getProperty( "DeviceType" ) ) );
     setSetupCode( Integer.parseInt( props.getProperty( "SetupCode" ) ) );
   }
 
   /**
    * Instantiates a new key move.
    * 
    * @param keyMove
    *          the key move
    */
   public KeyMove( KeyMove keyMove )
   {
     this( keyMove.getKeyCode(), keyMove.getDeviceButtonIndex(), keyMove.getDeviceType(), keyMove.getSetupCode(),
         new Hex( keyMove.getCmd() ), keyMove.getNotes() );
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#clone()
    */
   @Override
   protected Object clone() throws CloneNotSupportedException
   {
     return new KeyMove( getKeyCode(), getDeviceButtonIndex(), getDeviceType(), getSetupCode(), ( Hex )getCmd().clone(),
         getNotes() );
   }
 
   /**
    * Gets the eFC.
    * 
    * @return the eFC
    */
   public EFC getEFC()
   {
     return new EFC( cmd );
   }
 
   /**
    * Gets the eF c5.
    * 
    * @return the eF c5
    */
   public EFC5 getEFC5()
   {
     return new EFC5( cmd );
   }
 
   /**
    * Sets the cmd.
    * 
    * @param hex
    *          the new cmd
    */
   public void setCmd( Hex cmd )
   {
     this.cmd = cmd;
     data = getRawHex( deviceType, setupCode, cmd );
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.hifiremote.jp1.AdvancedCode#getValueString(com.hifiremote.jp1.RemoteConfiguration)
    */
   @Override
   public String getValueString( RemoteConfiguration remoteConfig )
   {
     DeviceUpgrade deviceUpgrade = remoteConfig.findDeviceUpgrade( getDeviceType(), getSetupCode() );
     if ( deviceUpgrade != null )
     {
       Function f = deviceUpgrade.getFunction( getCmd() );
       if ( f != null )
       {
         return "\"" + f.getName() + '"';
       }
     }
 
     if ( cmd.length() == 1 )
     {
       return getEFC().toString();
     }
     else
     {
       return getEFC5().toString();
     }
   }
 
   /** The device button index. */
   private int deviceButtonIndex;
 
   /**
    * Gets the device button index.
    * 
    * @return the device button index
    */
   public int getDeviceButtonIndex()
   {
     return deviceButtonIndex;
   }
 
   /**
    * Sets the device button index.
    * 
    * @param newIndex
    *          the new device button index
    */
   public void setDeviceButtonIndex( int newIndex )
   {
     deviceButtonIndex = newIndex;
   }
 
   /** The device type. */
   private int deviceType;
 
   /**
    * Gets the device type.
    * 
    * @return the device type
    */
   public int getDeviceType()
   {
     return deviceType;
   }
 
   /**
    * Sets the device type.
    * 
    * @param newDeviceType
    *          the new device type
    */
   public void setDeviceType( int newDeviceType )
   {
     deviceType = newDeviceType;
     update();
   }
 
   private void update()
   {
     update( deviceType, setupCode, data );
   }
 
   protected static void update( int deviceType, int setupCode, Hex data )
   {
     int temp = deviceType << 12 | setupCode;
     data.put( temp, SETUP_CODE_INDEX );
   }
 
   /** The setup code. */
   private int setupCode;
 
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
    * Sets the setup code.
    * 
    * @param newCode
    *          the new setup code
    */
   public void setSetupCode( int newCode )
   {
     setupCode = newCode;
     update();
   }
 
   private Hex cmd = null;
 
   public Hex getCmd()
   {
     return cmd;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.hifiremote.jp1.AdvancedCode#store(com.hifiremote.jp1.PropertyWriter)
    */
   @Override
   public void store( PropertyWriter pw )
   {
     pw.print( "DeviceButtonIndex", deviceButtonIndex );
     pw.print( "DeviceType", deviceType );
     pw.print( "SetupCode", setupCode );
     super.store( pw );
   }
 
   /** The Constant DEVICE_TYPE_INDEX. */
   protected final static int DEVICE_TYPE_INDEX = 0;
 
   /** The Constant SETUP_CODE_INDEX. */
   protected final static int SETUP_CODE_INDEX = 0;
 
   /** The Constant CMD_INDEX. */
   protected final static int CMD_INDEX = 2;
 
   /*
    * (non-Javadoc)
    * 
    * @see com.hifiremote.jp1.AdvancedCode#store(short[], int)
    */
   @Override
   public int store( short[] buffer, int offset, Remote remote )
   {
     buffer[ offset++ ] = ( short )keyCode;
     int lengthOffset;
     if ( remote.getAdvCodeBindFormat() == BindFormat.NORMAL )
     {
       int temp = deviceButtonIndex << 5;
       buffer[ offset ] = ( short )temp;
       lengthOffset = offset++ ;
     }
     else
     // LONG Format
     {
       buffer[ offset++ ] = ( short )( 0x10 | deviceButtonIndex );
       lengthOffset = offset++ ;
       buffer[ lengthOffset ] = 0;
     }
     int hexLength = data.length();
     Hex.put( data, buffer, offset );
     buffer[ lengthOffset ] |= ( short )hexLength;
 
     return offset + hexLength;
   }
 }
