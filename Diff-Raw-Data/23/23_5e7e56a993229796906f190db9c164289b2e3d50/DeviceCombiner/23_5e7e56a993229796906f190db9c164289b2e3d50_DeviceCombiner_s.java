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
     super.reset();
   }
 
   public void setProperties( Properties props )
   {
     System.err.println( "DeviceCombiner.setProperties()" );
     for ( int i = 0; i < 16; i++ )
     {
       String prefix = "Combiner." + i;
       String nameStr = props.getProperty( prefix + ".name" );
       if ( nameStr == null )
         break;
       System.err.println( "Name is '" + nameStr + "'" );
       Hex pid = new Hex( props.getProperty( prefix + ".id" ));
       System.err.println( "pid is " + pid.toString());
       String variantName = props.getProperty( prefix + ".variant" );
       System.err.println( "variantName is " + variantName );
       Protocol p = null;
       String parmStr = props.getProperty( prefix + ".parms" );
       Value[] values = null;
       String compStr = "PID " + pid.toString();
       System.err.println( "compStr is '" + compStr + "'" );
       String notes = props.getProperty( prefix + ".notes" );
       if ( nameStr.equals( compStr ))
       {
         System.err.println( "Creating new ManualProtocol!" );
         ManualProtocol m = new ManualProtocol( pid, new Properties());
         if (( parmStr != null ) && ( parmStr.length() > 0 ))
           m.setRawHex( new Hex( parmStr ));
         p = m;
         values = new Value[ 0 ];
       }
       else
       {
         p = ProtocolManager.getProtocolManager().findNearestProtocol( nameStr, pid, variantName );
         values = DeviceUpgrade.stringToValueArray( parmStr );
         p.setDeviceParms( values );
         values = p.getDeviceParmValues();
       }
       add( new CombinerDevice( p, values, notes ));
     }
   }
 
   public void add( CombinerDevice device )
   {
     devices.add( device );
     System.err.println( "DeviceCombiner.add(): device count=" + devices.size());
   }
 
   public KMPanel getPanel( DeviceUpgrade deviceUpgrade )
   {
     if ( panel == null )
       panel = new DeviceCombinerPanel( deviceUpgrade );
     else
       panel.setDeviceUpgrade( deviceUpgrade );
 
     return panel;
   }
 
   public Vector< CombinerDevice > getDevices(){ return devices; }
 
   public void store( PropertyWriter out, Value[] vals )
     throws IOException
   {
     super.store( out, vals );
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
       if ( p.getClass() == ManualProtocol.class )
       {
         Hex h = p.getFixedData( values );
         if ( h != null )
           out.print( prefix + ".parms", h.toString());
       }
       else
       {
         out.print( prefix + ".parms", DeviceUpgrade.valueArrayToString( values ));
       }
       out.print( prefix + ".notes", device.getNotes());
     }
   }
 
   public boolean hasCode( Remote r )
   {
     boolean rc = true;
     Processor p = r.getProcessor();
     String name = p.getName();
     String version = p.getVersion();
     int[] devCombAddresses = r.getDevCombAddresses();
     if ( devCombAddresses == null )
       return false;
 
     if ( getVariantName().equals( "S3C80" ))
     {
       if ( !name.equals( "S3C80" ) && !name.equals( "S3C80+" ))
         return false;
     }
     else
     {
       if ( name.equals( "S3C80" ) || name.equals( "S3C80+" ))
         return false;
     }
 
     if ( name.equals( "S3C80" ) || name.equals( "S3c80+" ))
     {
       if (( devCombAddresses[ 1 ] == -1 ) ||
           ( devCombAddresses[ 2 ] == -1 ) ||
           ( devCombAddresses[ 4 ] == -1 ) ||
           ( devCombAddresses[ 5 ] == -1 ))
         rc = false;
     }
     else if ( name.equals( "6805" ))
     {
         if (( devCombAddresses[ 1 ] == -1 ) ||
             ( devCombAddresses[ 2 ] == -1 ) ||
             ( devCombAddresses[ 3 ] == -1 ) ||
             ( devCombAddresses[ 5 ] == -1 ) ||
             ( devCombAddresses[ 6 ] == -1 ))
           rc = false;
     }
     else if ( name.equals( "740" ))
     {
       if (( devCombAddresses[ 1 ] == -1 ) ||
           ( devCombAddresses[ 2 ] == -1 ) ||
           ( devCombAddresses[ 3 ] == -1 ))
         rc = false;
     }
     else if ( name.equals( "HCS08" ))
     {
       if (( devCombAddresses[ 1 ] == -1 ) ||
           ( devCombAddresses[ 2 ] == -1 ))
         rc = false;
     }
     return rc;
   }
 
   public Hex getCode( Remote r )
   {
     short[] header = new short[ devices.size() + 1 ];
 
     Processor processor = r.getProcessor();
     String name = processor.getName();
     String version = processor.getVersion();
     StringBuilder buff = new StringBuilder();
     Hex base = null;
     int[] devComb = r.getDevCombAddresses();
     if ( devComb == null )
       return null;
 
     if ( name.equals( "S3C80" ) || name.equals( "S3C80+" ))
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
       short[] hex = base.getData();
       short length = ( short )hex.length;
       hex[ 21 ] = ( short )( length + 1 );
       hex[ 24 ] = length;
     }
     else if ( name.equals( "6805" ))
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
       short[] hex = base.getData();
       int pointer = devComb[ 6 ] + hex.length;
       hex[ 6 ] = ( short )( pointer >> 8 );
       hex[ 7 ] = ( short )( pointer & 0xFF );
       hex[ 11 ] = hex[ 6 ];
       hex[ 12 ] = hex[ 7 ];
       pointer++;
       hex[ 16 ] = ( short )( pointer >> 8 );
       hex[ 17 ] = ( short )( pointer & 0xFF );
       pointer++;
       hex[ 54 ] = ( short )( pointer >> 8 );
       hex[ 55 ] = ( short )( pointer & 0xFF );
     }
     else if ( name.equals( "740" ))
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
     else if ( name.equals( "HCS08" ))
     {
       if (( devComb[ 1 ] == -1 ) ||
           ( devComb[ 2 ] == -1 ))
         return null;
         
       buff.append( "20 10 00 00 02 00 B7 7A 55 BB AF 00 81 3C 7F 7E 5F 81 " );
       buff.append( "4E 61 54 45 00 10 B6 60 52 48 27 02 B7 B2 8B 86 AB " );
       buff.append( "00 " ); // placeholder x1, index = 35
       buff.append( "BD 75 9E AE 8B 9F BD 75 35 52 86 BD 75 7E 56 7E 57 BD " );
       buff.append( "7C 75 52 23 FA 45 00 54 BD 7E " );
       if ( devComb[ 4 ] != -1 )
         buff.append( intToString( devComb[ 4 ]));
 
       buff.append( "CD " );
 
       buff.append( intToString( devComb[ 2 ]));
       buff.append( "24 CA A7 02 CC " );
       buff.append( intToString( devComb[ 1 ]));
 
       base = new Hex( buff.toString());
       short[] hex = base.getData();
       short length = ( short )hex.length;
       hex[ 35 ] = length;
     }
 
     int offset = header.length;
     if ( name.equals( "S3C80" ) ||
          name.equals( "S3C80+" ) || 
          name.equals( "HCS08" ))
     {
       offset += base.length();
     }
     Hex[] ids = new Hex[ devices.size()];
     Hex[] data = new Hex[ ids.length ];
     int i = 0;
     for ( Enumeration e = devices.elements(); e.hasMoreElements(); )
     {
       header[ i ] = ( short )offset;
       CombinerDevice device = ( CombinerDevice )e.nextElement();
      ids[ i ] = device.getProtocol().getID();
       offset += 2;
       Hex hex = device.getFixedData();
       data[ i ] = hex;
       offset += hex.length();
       i++;
     }
     header[ i ] = ( short )offset;
 
     if ( !name.equals( "S3C80" ) && !name.equals( "S3C80+" ))
       offset += base.length();
 
     short[] code = new short[ offset ];
     System.arraycopy( base.getData(), 0, code, 0, base.length() );
     offset = base.length();
     System.arraycopy( header, 0, code, offset, header.length );
     offset += header.length;
     for ( i = 0; i < data.length; i++ )
     {
       short[] src = ids[ i ].getData();
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
     StringBuilder buff = new StringBuilder( 5 );
     buff.append( Integer.toHexString( val >> 8 ));
     buff.append( ' ' );
     buff.append( Integer.toHexString( val & 0xFF ));
     return buff.toString();
   }
 
   private String intToStringReverse( int val )
   {
     StringBuilder buff = new StringBuilder( 5 );
     buff.append( Integer.toHexString( val & 0xFF ));
     buff.append( ' ' );
     buff.append( Integer.toHexString( val >> 8 ));
     return buff.toString();
   }
 
   public boolean isColumnWidthFixed( int col )
   {
     if ( col == 0 )
       return false;
     else
       return true;
   }
 
   private DeviceCombinerPanel panel = null;
   private Vector< CombinerDevice > devices = new Vector< CombinerDevice >();
 }
