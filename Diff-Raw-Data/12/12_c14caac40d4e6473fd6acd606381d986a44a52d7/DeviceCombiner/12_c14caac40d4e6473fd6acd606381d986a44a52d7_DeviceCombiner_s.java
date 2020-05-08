 package com.hifiremote.jp1;
 
 import java.util.*;
 import java.io.*;
 
 public class DeviceCombiner
   extends Protocol
 {
   public DeviceCombiner( String name, Hex id, Properties props )
   {
     super( name, id, props );
     cmdParmInit = new Initializer[ 1 ];
     cmdParmInit[ 0 ] = 
       new DeviceCombinerInitializer( devices, ( ChoiceCmdParm )cmdParms[ 0 ]);
   }
 
   public void reset()
   {
     devices.clear();
   }
 
   public void setProperties( Properties props )
   {
     for ( int i = 0; i < 16; i++ )
     {
       String prefix = "Combiner." + i;
       System.err.println( "Looking for " + prefix + ".name" );
       String nameStr = props.getProperty( prefix + ".name" );
       System.err.println( "Got " + nameStr );
       if ( nameStr == null )
         break;
       Hex pid = new Hex( props.getProperty( prefix + ".id" ));
       String variantName = props.getProperty( prefix + ".variant" );
       Protocol p = 
         ProtocolManager.getProtocolManager().findNearestProtocol( nameStr, pid, variantName );
       
       String parmStr = props.getProperty( prefix + ".parms" );
       Value[] values = DeviceUpgrade.stringToValueArray( parmStr );
       devices.add( new CombinerDevice( p, values ));
     }
   }
 
   public void addProtocol( Protocol p, Hex fixedData )
   {
     Value[] values = p.getDeviceParmValues();
     if ( p.deviceTranslators != null )
     {
       for ( int i = 0; i < p.deviceTranslators.length; i++ )
       {
         p.deviceTranslators[ i ].out( fixedData, values, p.devParms );
       }
     }
     CombinerDevice device = new CombinerDevice( p, values );
     devices.add( device );
   }
 
   public KMPanel getPanel( DeviceUpgrade deviceUpgrade )
   {
     System.err.println( "DeviceCombiner.getPanel()" );
     if ( panel == null )
       panel = new DeviceCombinerPanel( deviceUpgrade );
     else
       panel.setDeviceUpgrade( deviceUpgrade );
 
     return panel;
   }
 
   public Vector getDevices(){ return devices; }
   
   public void store( PropertyWriter out )
     throws IOException
   {
     System.err.println( "DeviceCombiner.store" );
     super.store( out );
     int i = 0;
     for ( Enumeration e = devices.elements(); e.hasMoreElements(); )
     {
       CombinerDevice device = ( CombinerDevice )e.nextElement();
       String prefix = "Combiner." + i++;
       Protocol p = device.getProtocol();
       out.print( prefix + ".name", p.getName());
       out.print( prefix + ".id", p.getID().toString());
       out.print( prefix + ".variant", p.getVariantName());
       Value[] values = device.getValues();
       out.print( prefix + ".parms", DeviceUpgrade.valueArrayToString( values )); 
     }
   }
 
   public boolean hasCode( Remote r )
   {
     boolean rc = true;
     String processor = r.getProcessor();
     String version = r.getProcessorVersion();
     int[] devCombAddresses = r.getDevCombAddresses();
     if ( devCombAddresses == null )
       return false;
     if ( processor.equals( "S3C80" ))
     {
       if (( devCombAddresses[ 1 ] == -1 ) || 
           ( devCombAddresses[ 2 ] == -1 ) || 
           ( devCombAddresses[ 4 ] == -1 ) || 
           ( devCombAddresses[ 5 ] == -1 ))
         rc = false;
     }
     else if ( processor.equals( "6805" ))
     {
         if (( devCombAddresses[ 1 ] == -1 ) ||
             ( devCombAddresses[ 2 ] == -1 ) ||
             ( devCombAddresses[ 3 ] == -1 ) ||
             ( devCombAddresses[ 5 ] == -1 ) ||
             ( devCombAddresses[ 6 ] == -1 ))
           rc = false;
     }
     else if ( processor.equals( "740" ))
     {
       if (( devCombAddresses[ 1 ] == -1 ) ||
           ( devCombAddresses[ 2 ] == -1 ) ||
           ( devCombAddresses[ 3 ] == -1 ))
         rc = false;
     }
     return rc;
   }
 
   public Hex getCode( Remote r )
   {
     byte[] header = new byte[ devices.size() + 1 ];
 //    Hex base = super.getCode( r );
 //    if ( base == null )
 //      return null;
 
 //    base = base.clone();
 
 //    byte[] hex = code.getData();
 
     String processor = r.getProcessor();
     String version = r.getProcessorVersion();
     StringBuffer buff = new StringBuffer();
     Hex base = null;
     int[] devComb = r.getDevCombAddresses();
     if ( devComb == null )
       return null;
 
     if ( processor.equals( "S3C80" ))
     {
       if (( devComb[ 1 ] == -1 ) || 
           ( devComb[ 5 ] == -1 ) || 
           ( devComb[ 2 ] == -1 ))
         return null;
 
       buff.append( "00 00 22 " );
       //  add code to handle favscan patch here???
       buff.append( "08 06 96 10 04 90 05 6B 03 E4 05 0D 38 04 2C " );
       buff.append( Integer.toHexString( r.getRAMAddress() >> 8 ));
       buff.append( " E7 62 ff E7 32 ff E3 42 E3 52 1C 03 E3 72 D7 17 1E A2 36 3B F7 97 01 FF 06 D9 02 56 " );
       buff.append( intToString( devComb[ 4 ])); // comb4
       buff.append( " C6 " );
       if ( devComb[ 6 ] != -1 ) // comb6
       {
         buff.append( "CA " );
         buff.append( intToString( devComb[ 6 ]));
         buff.append( " C6 " );
       }
       buff.append( "C2 " );
       buff.append( intToString( devComb[ 1 ])); //comb1
       buff.append( " 70 C3 70 C2 C6 DA " );
       buff.append( intToString( devComb[ 5 ])); // comb5
       buff.append( " 1F " );
       buff.append( intToString( devComb[ 2 ])); //comb2
 
       base = new Hex( buff.toString());
       byte[] hex = base.getData();
       int length = hex.length;
       hex[ 21 ] = ( byte )( length + 1 );
       hex[ 24 ] = ( byte )length;
     }
     else if ( processor.equals( "6805" ))
     {
       if (( devComb[ 1 ] == -1 ) ||
           ( devComb[ 2 ] == -1 ) ||
           ( devComb[ 3 ] == -1 ) ||
           ( devComb[ 5 ] == -1 ) ||
           ( devComb[ 6 ] == -1 ))
          return null;
 
       buff.append( "00 00 02 BE 5A DE ff ff BF E0 D6 ff ff B7 C1 D6 ff ff B7 C2 CD " );
       buff.append( intToString( devComb[ 1 ]));
       buff.append( " 24 01 81 AB 02 24 01 5C CD " );
       buff.append( intToString( devComb[ 3 ]));
       buff.append( " 97 54 54 54 54 B6 5B E7 5A BF " );
       buff.append( Integer.toHexString( devComb[ 5 ] & 0xFF ));
       buff.append( " BF E1 27 13 3F E2 BE E0 D6 ff ff BE E2 E7 5A 3C E0 3C E2 3A E1 26 EF CC " );
       buff.append( intToString( devComb[ 2 ]));
 
       base = new Hex( buff.toString());
       byte[] hex = base.getData();
       int pointer = devComb[ 6 ] + hex.length;
       hex[ 6 ] = ( byte )( pointer >> 8 );
       hex[ 7 ] = ( byte )( pointer & 0xFF );
       hex[ 11 ] = hex[ 6 ];
       hex[ 12 ] = hex[ 7 ];
       pointer++;
       hex[ 16 ] = ( byte )( pointer >> 8 );
       hex[ 17 ] = ( byte )( pointer & 0xFF );
       pointer++;
       hex[ 54 ] = ( byte )( pointer >> 8 );
       hex[ 55 ] = ( byte )( pointer & 0xFF );
     }
     else if ( processor.equals( "740" ))
     {
       if (( devComb[ 1 ] == -1 ) ||
           ( devComb[ 2 ] == -1 ) ||
           ( devComb[ 3 ] == -1 ))
         return null;
 
       buff.append( "00 00 02 A4 5D BE 7B 01 BD 7B 01 85 E7 BD 7C 01 85 E6 20 " ); 
       buff.append( intToStringReverse( devComb[ 1 ] ));
       buff.append( " B0 31 A0 02 B1 " );
       buff.append( Integer.toHexString( devComb[ 3 ] & 0xFF ));
       buff.append( " 4A 4A 4A 4A AA A5 5E A4 5D 95 5D E0 00 F0 1B 86" );
       buff.append( " E2 B9 7B 01 C6 E2 18 65 E2 AA A4 E2 80 08 BD 7D" );
       buff.append( " 01 99 5D 00 88 CA C0 FF D0 F4 4C " );
       buff.append( intToStringReverse( devComb[ 2 ]));
       buff.append( " 60" );
       base = new Hex( buff.toString());
     }
        
     int offset = header.length;
     if ( processor.equals( "S3C80" ))
       offset += base.length();
     Hex[] ids = new Hex[ devices.size()];
     Hex[] data = new Hex[ ids.length ];
     int i = 0;
     for ( Enumeration e = devices.elements(); e.hasMoreElements(); )
     {
       header[ i ] = ( byte )offset;
       CombinerDevice device = ( CombinerDevice )e.nextElement();
       ids[ i ] = device.getProtocol().getID();
       offset += 2;
       Hex hex = device.getFixedData();
       data[ i ] = hex;
       offset += hex.length();
       i++;
     }
     header[ i ] = ( byte )offset;
 
     if ( !processor.equals( "S3C80" ))
       offset += base.length();
 
     byte[] code = new byte[ offset ];
     System.arraycopy( base.getData(), 0, code, 0, base.length() );
     offset = base.length();
     System.arraycopy( header, 0, code, offset, header.length );
     offset += header.length;
     for ( i = 0; i < data.length; i++ )
     {
       byte[] src = ids[ i ].getData();
       System.arraycopy( src, 0, code, offset, src.length );
       offset += src.length;
       src = data[ i ].getData();
       System.arraycopy( src, 0, code, offset, src.length );
       offset += src.length;
     }
         
     return new Hex( code );
   }
 
   private String intToString( int val )
   {
     StringBuffer buff = new StringBuffer( 5 );
     buff.append( Integer.toHexString( val >> 8 ));
     buff.append( ' ' );
     buff.append( Integer.toHexString( val & 0xFF ));
     return buff.toString();
   }
 
   private String intToStringReverse( int val )
   {
     StringBuffer buff = new StringBuffer( 5 );
     buff.append( Integer.toHexString( val & 0xFF ));
     buff.append( ' ' );
     buff.append( Integer.toHexString( val >> 8 ));
     return buff.toString();
   }
 
   private DeviceCombinerPanel panel = null;
   private Vector devices = new Vector();
 }
