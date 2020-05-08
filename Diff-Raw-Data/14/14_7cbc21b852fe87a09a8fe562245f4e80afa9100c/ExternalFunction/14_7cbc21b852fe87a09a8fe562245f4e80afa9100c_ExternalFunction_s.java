 package com.hifiremote.jp1;
 
 import java.util.Properties;
 
 public class ExternalFunction
   extends Function
 {
   public boolean isExternal(){ return true; }
 
   public boolean isEmpty()
   {
     return super.isEmpty() && ( deviceTypeAliasName == null ) && ( setupCode == 0 ) &&
       ( type == EFCType );
   }
 
  public void store( Properties props, String prefix )
   {
    super.store( props, prefix );
    props.setProperty( prefix + ".type", Integer.toString( type ));
     if ( deviceTypeAliasName != null )
      props.setProperty( prefix + ".deviceType", deviceTypeAliasName );
    props.setProperty( prefix + ".setupCode", Integer.toString( setupCode ));
   }
 
   public void load( Properties props, String prefix, Remote remote )
   {
     super.load( props, prefix );
     String str = props.getProperty( prefix + ".type" );
     if ( str != null )
       setType( new Integer( str ));
     str = props.getProperty( prefix + ".deviceType" );
     if ( str != null )
       setDeviceTypeAliasName( str );
     str = props.getProperty( prefix + ".setupCode" );
     if ( str != null )
       setSetupCode( Integer.parseInt( str ));
   }
 
   public String getDeviceTypeAliasName(){ return deviceTypeAliasName; }
   public void setDeviceTypeAliasName( String name ){ deviceTypeAliasName = name; }
 
   public final static int EFCType = 0;
   public final static int HexType = 1;
 
   public String toString()
   {
     String rc = "";
     Object o = getValue();
     if ( o != null )
       rc = o.toString(); 
     return rc;
   }
 
   public void setType( int type )
   {
     this.type = type;
   }
 
   public void setType( Integer type )
   {
     setType( type.intValue());
   }
 
   public int getType()
   {
     return type;
   }
 
   public void setValue( Object value )
   {
     if ( value == null )
       setHex( null );
     else
       if ( type == EFCType )
         setEFC(( EFC )value );
       else
         setHex(( Hex )value );
   }
 
   public Object getValue()
   {
     if ( type == EFCType )
       return getEFC();
     else
       return getHex();
   }
 
   public void setSetupCode( int code ){ setupCode = code; }
   public int getSetupCode(){ return setupCode; }
 
   public EFC getEFC()
   {
     EFC rc = null;
     if ( hex != null )
       rc = Protocol.hex2efc( hex, 0 );
     return rc;
   }
 
   public void setEFC( EFC efc )
   {
     if ( efc != null )
     {
       if ( hex == null )
         hex = new Hex();
       Protocol.efc2hex( efc, hex, 0 );
     }
     else
       hex = null;
   }
 
   private String deviceTypeAliasName;
   private int setupCode;
   private int type;
 }
