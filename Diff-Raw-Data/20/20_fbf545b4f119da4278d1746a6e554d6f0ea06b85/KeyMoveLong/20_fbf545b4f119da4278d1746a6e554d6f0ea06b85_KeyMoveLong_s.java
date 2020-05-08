 package com.hifiremote.jp1;
 
 public class KeyMoveLong extends KeyMove
 {
   public KeyMoveLong( int keyCode, int deviceButtonIndex, Hex data, String notes )
   {
     super( keyCode, deviceButtonIndex, data, notes );
 
   }
 
   public KeyMoveLong( int keyCode, int deviceButtonIndex, int deviceType, int setupCode, Hex cmd, String notes )
   {
     super( keyCode, deviceButtonIndex, deviceType, setupCode, cmd, notes );
   }
 
   @Override
   public Hex getRawHex( int deviceType, int setupCode, Hex cmd )
   {
     Hex hex = new Hex( CMD_INDEX + 2 );
     short[] hexData = hex.getData();
     short[] cmdData = cmd.getData();
     hexData[ CMD_INDEX ] = cmdData[ 0 ];
     if ( cmdData.length == 2 )
     {
       hexData[ CMD_INDEX + 1 ] = cmdData[ 1 ];
     }
     else
     {
       hexData[ CMD_INDEX + 1 ] = EFC.parseHex( cmd );
     }
     update( deviceType, setupCode, hex );
     return hex;
   }
 }
