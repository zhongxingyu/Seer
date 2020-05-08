 package com.hifiremote.jp1;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class DeviceButton.
  */
 public class DeviceButton
 {
   /**
    * Instantiates a new device button.
    * 
    * @param name
    *          the name
    * @param hiAddr
    *          the hi addr
    * @param lowAddr
    *          the low addr
    * @param typeAddr
    *          the type addr
    * @param setupCode
    *          the default setup code
    * @param maxSetupCode
    *          the maximum allowed setup code
    */
   public DeviceButton( String name, int hiAddr, int lowAddr, int typeAddr, int setupCode, int index  )
   {
     this.name = name;
     highAddress = hiAddr;
     lowAddress = lowAddr;
     typeAddress = typeAddr;
     defaultSetupCode = setupCode;
     buttonIndex = index;
   }
   
   public static final DeviceButton noButton = new DeviceButton( "<none>", 0, 0, 0, 0, -1 );
 
   /**
    * Gets the name.
    * 
    * @return the name
    */
   public String getName()
   {
     return name;
   }
 
   /**
    * Gets the default setup code.
    * 
    * @return the default setup code
    */
   public int getDefaultSetupCode()
   {
     return defaultSetupCode;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#toString()
    */
   public String toString()
   {
     return name;
   }
 
   /**
    * Gets the device type index.
    * 
    * @param data
    *          the data
    * @return the device type index
    */
   public int getDeviceTypeIndex( short[] data )
   {
     return data[ highAddress ] >> 4;
   }
   
   /**
    * Sets the device type index.
    * 
    * @param index
    *          the index
    * @param data
    *          the data
    */
   public void setDeviceTypeIndex( short index, short[] data )
   {
     if ( index == 0xFF )
     {
       data[ highAddress ] = 0xFF;
       data[ lowAddress ] = 0xFF;
     }
     else
     {
       data[ highAddress ] &= 0x0F;
       index <<= 4;
       data[ highAddress ] |= index;
     }
   }
   
   public int getDeviceGroup( short[] data )
   {
     if ( typeAddress > 0 ) return data[ typeAddress ];
    else return getDeviceTypeIndex( data );
   }
   
   public void setDeviceGroup( short group, short[] data )
   {
     if ( typeAddress > 0 )
     {
       data[ typeAddress ] = group;
     }
   }
 
 
   /**
    * Gets the setup code.
    * 
    * @param data
    *          the data
    * @return the setup code
    */
   public short getSetupCode( short[] data )
   {
     short setupCode = data[ highAddress ];
     int mask = 0x07;
     if ( SetupCode.getMax() > 2048 )
       mask = 0x0F;
     setupCode &= mask;
     setupCode <<= 8;
     setupCode |= data[ lowAddress ];
     return setupCode;
   }
 
   /**
    * Sets the setup code.
    * 
    * @param setupCode
    *          the setup code
    * @param data
    *          the data
    */
   public void setSetupCode( short setupCode, short[] data )
   {
     if ( setupCode > SetupCode.getMax() )
     {
       throw new NumberFormatException( "Setup codes must be between 0 and " + SetupCode.getMax() );
     }
     short temp = setupCode;
     temp >>= 8;
     int mask = 0xF8;
     if ( SetupCode.getMax() > 2047 )
       mask = 0xF0;
     data[ highAddress ] &= mask;
     data[ highAddress ] |= temp;
 
     data[ lowAddress ] = ( short )( setupCode & 0xFF );
   }
   
   public int getDeviceSlot( short[] data )
   {
     return ( data[ highAddress ] << 8 ) | data[ lowAddress ];
   }
   
   public void zeroDeviceSlot( short[] data )
   {
     data[ highAddress ] = 0;
     data[ lowAddress ] = 0;
   }
   
   public int getButtonIndex()
   {
     return buttonIndex;
   }
 
   /** The name. */
   private String name;
 
   /** The high address. */
   private int highAddress = 0;
 
   /** The low address. */
   private int lowAddress = 0;
 
   /** The type address. */
   private int typeAddress = 0;
 
   /** The default setup code */
   private int defaultSetupCode = 0;
   
   private int buttonIndex = 0;
 }
