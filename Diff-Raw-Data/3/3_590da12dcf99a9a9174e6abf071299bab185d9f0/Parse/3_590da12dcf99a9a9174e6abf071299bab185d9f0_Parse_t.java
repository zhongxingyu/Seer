 package guisim.model;
 
 public class Parse {
     public byte[] datapointFromGui(FromGui data) {
         byte[] outputData = new byte[8];
         System.arraycopy(parseToBytes( data.deg ),0,outputData,0,2);
         System.arraycopy(parseToBytes( data.P ),0,outputData,2,2);
         System.arraycopy(parseToBytes( data.I ),0, outputData, 4, 2);
         System.arraycopy(parseToBytes( data.D ),0, outputData, 6, 2);
         return outputData;
     }
 
     public FromHardware datapointFromHardware(byte[] data) {
         if (data.length != 6)
             throw new IllegalArgumentException("Invalid length, datapoint must be 6 bytes");
         short roll = parseToShort(data[0], data[1]);
         short pitch = parseToShort(data[2], data[3]);
         short yaw = parseToShort(data[4], data[5]);
         return new FromHardware(roll, pitch, yaw);
     }
 
     //TODO: 127deg error
     //TODO: 262deg error
     public short parseToShort(byte b1, byte b2) {
        //return (short) ((b1 << 8) | b2);
        return (short) ((b1 << 8) | (0xff & b2));
         //return (short) ((b2 << 8) + (b1&0xFF));
     }
 
     public byte[] parseToBytes(short s) {
         byte b1 = (byte) (s >>> 8);
         byte b2 = (byte) (s & 0xFF);
         //byte b1 = (byte) (s & 0xFF);
         //byte b2 = (byte) ((s >> 8) & 0xFF);
         return new byte[] { b1, b2 };
     }
 }
