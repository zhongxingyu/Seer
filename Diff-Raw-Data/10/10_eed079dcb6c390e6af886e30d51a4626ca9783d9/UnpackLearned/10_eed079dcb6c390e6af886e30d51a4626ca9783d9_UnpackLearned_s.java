 package com.hifiremote.jp1;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class UnpackLearned.
  */
 public class UnpackLearned
 {
 
   /** The ok. */
   public boolean ok;
 
   /** The error. */
   public String error;
 
   /** The frequency. */
   public int frequency;
 
   /** The bursts. */
   public int[] bursts;
 
   /** The durations. */
   public int[] durations;
 
   /** The one time. */
   public int oneTime;
 
   /** The repeat. */
   public int repeat;
 
   /** The extra. */
   public int extra;
   /** The parts. */
   public int[] parts;
 
   /** The part types. */
   public boolean[] partTypes;
 
   /**
    * Instantiates a new unpack learned.
    * 
    * @param hex
    *          the hex
    */
   public UnpackLearned( Hex hex )
   {
     ok = true;
     error = "";
     if ( hex == null || hex.length() < 5 )
     {
       ok = false;
       error = "hex learned signal too short to unpack";
       return;
     }
     int period = hex.get( 0 );
     frequency = ( period == 0 ) ? 0 : 8000000 / period;
     int offset = loadBurstTable( hex );
     if ( ok )
     {
       loadDurations( hex, offset );
     }
   }
  
   public String toString()
   {
     return durationsToString( bursts, "" );
   }
 
   public static String durationsToString( int[] data, String sep )
   {
     StringBuilder str = new StringBuilder();
     if ( data != null && data.length != 0 )
     {
       boolean isSigned = false;
       for ( int d: data )
         if ( d < 0 )
         {
           isSigned = true;
           break;
        }        
      
       for ( int i = 0; i < data.length; i++ )
       {
         if ( i > 0 )
           str.append( ' ' );
         if ( !isSigned )
           str.append( ( i & 1 ) == 0 ? "+" : "-" );
         else if ( data[i] > 0 )
             str.append( '+' );
         str.append( data[i] );
         if ( i > 0 && ( i % 2 ) == 1 )
           str.append( sep );
       }
     }
     if ( str.length() == 0 )
       return "** No signal **";
 
     return str.toString();
   }
 
   /**
    * Load burst table.
    * 
    * @param hex
    *          the hex
    * @return the int
    */
   private int loadBurstTable( Hex hex )
   {
     int burstNum = hex.getData()[ 2 ];
     int result;
     if ( ( burstNum & 0x80 ) != 0 )
     {
       result = 3;
       burstNum &= 0x7F;
       if ( burstNum >= romIndex.length )
       {
         ok = false;
         error = "ROM burst index out of range";
         return 0;
       }
       burstNum = romIndex[ burstNum ];
       int count = romBursts.length - burstNum;
       if ( count > 32 )
         count = 32;
       bursts = new int[ count ];
       while ( --count >= 0 )
       {
         bursts[ count ] = 2 * romBursts[ count + burstNum ];
       }
     }
     else if ( burstNum != 0 )
     {
       result = burstNum * 4 + 3;
       if ( result >= hex.length() )
       {
         ok = false;
         error = "burst table extends beyond end of hex";
         return 0;
       }
       bursts = new int[ burstNum * 2 ];
       for ( int ndx = 0; ndx < burstNum * 2; ++ndx )
       {
         bursts[ ndx ] = hex.get( ndx * 2 + 3 ) * 2;
       }
     }
     else
     {
       ok = false;
       error = "00 found where burst table expected";
       return 0;
     }
     return result;
   }
 
   /**
    * Load durations.
    * 
    * @param hex
    *          the hex
    * @param offset
    *          the offset
    */
   private void loadDurations( Hex hex, int offset )
   {
     int partNdx = 0;
     int total = 0;
     for ( int ndx = offset; ndx != hex.length(); ++partNdx )
     {
       int count = ( hex.getData()[ ndx ] & 0x7F );
       if ( count == 0 )
       {
         ok = false;
         error = "burst index count is zero";
         return;
       }
       total += count * 2;
       ndx += ( count + 3 ) >> 1;
       if ( ndx > hex.length() )
       {
         ok = false;
         error = "duration list extends beyonds hex data";
         return;
       }
     }
     durations = new int[ total ];
     parts = new int[ partNdx ];
     partTypes = new boolean[ partNdx ];
     total = 0;
     partNdx = 0;
     for ( int ndx = offset; ndx != hex.length(); ++partNdx )
     {
       int count = hex.getData()[ ndx ];
       partTypes[ partNdx ] = ( count & 0x80 ) != 0;
       count &= 0x7F;
       parts[ partNdx ] = count;
       ++ndx;
       for ( int n = 0; n < count; ++n )
       {
         int x = hex.getData()[ ( n >> 1 ) + ndx ];
         x = ( ( ( n & 1 ) == 0 ) ? ( x >> 4 ) : ( x & 0xF ) ) * 2;
         if ( x >= bursts.length )
         {
 //          ok = false;
           // Non-fatal error
           error = "burst index out of range";
 //          return;
           durations[ total++ ] = 0;
           durations[ total++ ] = 0;
         }
         else
         {
           durations[ total++ ] = bursts[ x ];
           durations[ total++ ] = bursts[ x + 1 ];
           //System.err.println( "Durations[ " + (total-2) + "..." + (total-1) + " ] = " + durations[total-2] + " " + durations[total-1] );
           
         }
       }
       ndx += ( count + 1 ) >> 1;
     }
     repeat = 0;
     extra = 0;
 
     for ( int n = 0; n < partNdx; ++n )
     {
       if ( partTypes[ n ] && repeat == 0 )
       {
         repeat = 2 * parts[ n ];
       }
       else if ( repeat > 0 )
       {
         extra += 2 * parts[ n ];
       }
     }
 
     oneTime = total - repeat - extra;
   }
 
   private int roundTo(int value, int r)
   {
     return ((int) Math.round( (double)value / (double)r )) * r;
   }
   
   public int[] getBursts()
   {
     return getBursts(1);
   }
   public int[] getBursts(int r)
   {
     int[] temp = new int[bursts.length];
     for ( int i = 0; i < bursts.length; i++ )
       temp[i] = roundTo( bursts[i], r );
     return temp;
   }
 
   public int[] getDurations( int r, boolean signed )
   {
    return getDurations( 0, durations.length, r, signed );
   }
   public int[] getOneTimeDurations(int r, boolean signed)
   {
     return getDurations( 0, oneTime, r, signed );
   }
   public int[] getRepeatDurations(int r, boolean signed)
   {
     return getDurations( oneTime, oneTime + repeat, r, signed );
   }
   public int[] getExtraDurations(int r, boolean signed)
   {
     return getDurations( oneTime + repeat, oneTime + repeat + extra, r, signed );
   }
   private int[] getDurations( int start, int end, int r, boolean signed )
   {
     int[] temp = new int[end - start];
     int t = 0;
     for ( int i = start; i < end; i++ )
       temp[t++] = ( signed && i % 2 == 1 ? -1 : 1 ) * roundTo( durations[i], r );
     return temp;
   }
 
   /** The rom bursts. */
   private final int[] romBursts =
   {
       0x01A3, 0x4A81, 0x068F, 0x0690, 0x01A3, 0x04F6, 0x01A3, 0x01A4, // 0
       0x00D2, 0xAF0C, 0x00D2, 0x4507, 0x0277, 0x00D3, 0x00D2, 0x0278, // 8
       0x0083, 0x589B, 0x0083, 0x039B, 0x0083, 0x0189, // 16
       0x0083, 0x5D6F, 0x0083, 0x5527, 0x0083, 0x039B, 0x0083, 0x0189, // 22
       0x0009, 0xFCC0, 0x0009, 0x008C, 0x0009, 0x005A, 0x0009, 0x0028, // 30
       0x270F, 0x07D0, 0x01F3, 0x0FA0, 0x07CF, 0x07D0, 0x00F9, 0x03E8, 0x00F9, 0x01F4, // 38
       0x0118, 0x60C9, 0x08C9, 0x08CA, 0x0118, 0x034D, 0x0118, 0x0119, // 48
       0x0118, 0x4F1F, 0x1193, 0x08CA, 0x0118, 0x034D, 0x0118, 0x0119, // 56
       0x0118, 0xBE1D, 0x0118, 0x5C64, 0x08C9, 0x08CA, 0x0118, 0x034D, 0x0118, 0x0119, // 64
       0x0118, 0xBCE6, 0x0118, 0x4F21, 0x1193, 0x08CA, 0x1193, 0x0465, 0x0118, 0x034D, 0x0118, 0x0119, // 74
       0x0118, 0xBCE6, 0x0118, 0x57EE, 0x1193, 0x08CA, 0x1193, 0x0465, 0x0118, 0x034D, 0x0118, 0x0119, // 86
       0x0118, 0xB895, 0x0118, 0x2E8C, 0x1193, 0x08CA, 0x0118, 0x034D, 0x0118, 0x0119, // 98
       0x010A, 0xEE3E, 0x010A, 0x283A, 0x010A, 0x0537, 0x010A, 0x0216, 0x010A, 0x010B, // 108
       0x010A, 0xEE3E, 0x010A, 0x283A, 0x010A, 0x0537, 0x0215, 0x0216, 0x010A, 0x0216, 0x010A, 0x010B, // 118
       0x010A, 0xEE3E, 0x010A, 0x283A, 0x00F2, 0x0537, 0x0215, 0x0216, 0x0215, 0x010B, 0x010A, 0x0216, 0x010A, 0x010B, // 130
       0x01BC, 0xB0FF, 0x0379, 0x01BD, 0x01BC, 0x01BD, // 144
       0x01BC, 0xB0FF, 0x0379, 0x01BD, 0x01BC, 0x037A, 0x01BC, 0x01BD, // 150
       0x01BC, 0xB0FF, 0x0379, 0x037A, 0x0379, 0x01BD, 0x01BC, 0x037A, 0x01BC, 0x01BD, // 158
       0x0009, 0xFFFF, // 168
       0x0009, 0x1E61, 0x0009, 0x184E, 0x0009, 0x1238, 0x0009, 0x0C22, 0x0009, 0x060C, // 170
       0x0009, 0x7B65, 0x0009, 0x10CC, 0x0009, 0x0B2C, // 180
       0x006B, 0xFFFF, // 186
       0x006B, 0x1DFF, 0x006B, 0x17EC, 0x006B, 0x11D6, 0x006B, 0x0BC0, 0x006B, 0x05AA, // 188
       0x0013, 0x7B5B, 0x0013, 0x10C2, 0x0013, 0x0B22
   }; // 198
 
   /** The rom index. */
   private final int[] romIndex =
   {
       48, 56, 64, 74, 86, 8, 98, 16, 22, 30, 144, 150, 158, 108, 118, 130, 0, 38, 168, 170, 180, 186, 188, 198
   };
 }
