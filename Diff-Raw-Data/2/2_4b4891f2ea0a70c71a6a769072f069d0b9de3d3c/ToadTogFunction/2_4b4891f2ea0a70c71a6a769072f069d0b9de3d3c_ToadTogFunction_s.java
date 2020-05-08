 package com.hifiremote.jp1;
 
 import java.util.Properties;
 
 // TODO: Auto-generated Javadoc
 /**
  * Description of the Class.
  * 
  * @author Greg
  * @created December 2, 2006
  */
 public class ToadTogFunction extends SpecialProtocolFunction
 {
 
   /**
    * Constructor for the ToadTogFunction object.
    * 
    * @param keyMove
    *          the key move
    */
   public ToadTogFunction( KeyMove keyMove )
   {
     super( keyMove );
   }
   
   public ToadTogFunction( Macro macro )
   {
     super( macro );
   }
 
   /**
    * Constructor for the ToadTogFunction object.
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
   public ToadTogFunction( int keyCode, int deviceButtonIndex, int deviceType, int setupCode, Hex cmd, String notes )
   {
     super( keyCode, deviceButtonIndex, deviceType, setupCode, cmd, notes );
   }
 
   /**
    * Constructor for the ToadTogFunction object.
    * 
    * @param props
    *          the props
    */
   public ToadTogFunction( Properties props )
   {
     super( props );
   }
 
   /**
    * Gets the toggleNumber attribute of the ToadTogFunction object.
    * 
    * @return The toggleNumber value
    */
   public int getToggleNumber()
   {
     return ( getCmd().getData()[ 0 ] & 0x70 ) >> 4;
   }
 
   /**
    * Gets the onLength attribute of the ToadTogFunction object.
    * 
    * @return The onLength value
    */
   public int getOnLength()
   {
     return getCmd().getData()[ 0 ] & 0x07;
   }
 
   /**
    * Gets the style attribute of the ToadTogFunction object.
    * 
    * @return The style value
    */
   public int getStyle()
   {
     int val = getCmd().getData()[ 0 ];
     int style = ( val & 0x80 ) >> 6;
     style |= ( val & 0x08 ) >> 3;
     return style;
   }
 
   /**
    * Gets the type attribute of the ToadTogFunction object.
    * 
    * @return The type value
    */
   public String getType( RemoteConfiguration remoteConfig )
   {
     return getUserFunctions( remoteConfig )[ 0 ];
   }
 
   /**
    * Gets the displayType attribute of the ToadTogFunction object.
    * 
    * @return The displayType value
    */
   public String getDisplayType( RemoteConfiguration remoteConfig )
   {
     int style = getStyle();
 
     StringBuilder buff = new StringBuilder();
     buff.append( getUserFunctions( remoteConfig )[0] );
     buff.append( '(' );
     buff.append( Integer.toString( getToggleNumber() ) );
     buff.append( ',' );
     buff.append( styleStrings[ style ] );
     buff.append( ')' );
     return buff.toString();
   }
 
   /**
    * Gets the valueString attribute of the ToadTogFunction object.
    * 
    * @param remoteConfig
    *          the remote config
    * @return The valueString value
    */
   public String getValueString( RemoteConfiguration remoteConfig )
   {
     Remote remote = remoteConfig.getRemote();
     int style = getStyle();
     short[] keyCodes = getCmd().getData();
 
     StringBuilder buff = new StringBuilder();
     buff.append( '[' );
     buff.append( onStrings[ style ] );
     buff.append( "]:" );
     if ( getOnLength() == 0 )
       buff.append( "<none>" );
     int i = 0;
     boolean first = true;
     while ( i < getOnLength() )
     {
       if ( first )
         first = false;
       else
         buff.append( ';' );
       buff.append( remote.getButtonName( keyCodes[ i + 1 ] ) );
       ++i;
     }
 
     buff.append( " [" );
     buff.append( offStrings[ style ] );
     buff.append( "]:" );
     if ( i == ( keyCodes.length - 1 ) )
       buff.append( "<none>" );
     first = false;
     while ( i < keyCodes.length - 1 )
     {
       if ( first )
         first = false;
       else
         buff.append( ';' );
       buff.append( remote.getButtonName( keyCodes[ i + 1 ] ) );
       ++i;
     }
 
     return buff.toString();
   }
 
   /**
    * Description of the Method.
    * 
    * @param dlg
    *          the dlg
    */
   public void update( SpecialFunctionDialog dlg )
   {
     dlg.setToggle( getToggleNumber() );
     dlg.setCondition( getStyle() );
 
    short[] keyCodes = getKeyMove().data.getData();
 
     int length = getOnLength();
     Integer[] temp = new Integer[ length ];
     int offset = 1;
     for ( int i = 0; i < length; ++i )
       temp[ i ] = new Integer( keyCodes[ offset++ ] );
     dlg.setFirstMacroButtons( temp );
 
     length = keyCodes.length - length - 1;
     temp = new Integer[ length ];
     for ( int i = 0; i < length; ++i )
       temp[ i ] = new Integer( keyCodes[ offset++ ] );
     dlg.setSecondMacroButtons( temp );
   }
 
   /**
    * Description of the Method.
    * 
    * @param dlg
    *          the dlg
    * @return Description of the Return Value
    */
   public static Hex createHex( SpecialFunctionDialog dlg )
   {
     int toggle = dlg.getToggle();
     int condition = dlg.getCondition();
 
     Integer[] firstKeyCodes = dlg.getFirstMacroButtons();
     Integer[] secondKeyCodes = dlg.getSecondMacroButtons();
 
     short[] temp = new short[ 1 + firstKeyCodes.length + secondKeyCodes.length ];
     temp[ 0 ] = ( short )( ( ( condition & 2 ) << 6 ) | ( ( condition & 1 ) << 3 ) | ( toggle << 4 ) | firstKeyCodes.length );
     int offset = 1;
     for ( int i = 0; i < firstKeyCodes.length; ++i )
       temp[ offset++ ] = firstKeyCodes[ i ].shortValue();
     for ( int i = 0; i < secondKeyCodes.length; ++i )
       temp[ offset++ ] = secondKeyCodes[ i ].shortValue();
 
     return new Hex( temp );
   }
 
   /** Description of the Field. */
   public static int TOGGLE = 0;
 
   /** Description of the Field. */
   public static int FORCE_OFF = 1;
 
   /** Description of the Field. */
   public static int TEST_ONLY = 2;
 
   /** Description of the Field. */
   public static int FORCE_ON = 3;
 
   /** Description of the Field. */
   public static String[] styleStrings =
   {
       "Toggle", "ForceOff", "TestOnly", "ForceOn"
   };
 
   /** Description of the Field. */
   public static String[] onStrings =
   {
       "On->Off", "On->Off", "On", "Already On"
   };
 
   /** Description of the Field. */
   public static String[] offStrings =
   {
       "Off->On", "Already Off", "Off", "Off->On"
   };
 }
