 import javax.crypto.Cipher;
 import javax.crypto.spec.SecretKeySpec;
 import java.lang.StringBuilder;
 
 
 public class question3 {
     public static String byteArrayToString(byte [] array)
     {
         return new String(array);
     }
     public static void printByteArray(byte [] array)
     {
         System.out.print("Plain Text Hex Format: ");
         System.out.print("[");
         for(int i = 0; i < array.length-1; i++)
         {
             System.out.print(Integer.toHexString((array[i]>>4)&0x0F).toUpperCase());
             System.out.print(Integer.toHexString(array[i]&0x0F).toUpperCase() + ", ");
         }
         System.out.print(Integer.toHexString(array[array.length-1]>>4&0x0F).toUpperCase());
         System.out.println(Integer.toHexString(array[array.length-1]&0x0F).toUpperCase() + "]");
         String str = new String(array);
         System.out.println("Plain Text Readable: "+str);
     }
 
     public static void main (String [] args0)
     {
         Cipher cipher;
         SecretKeySpec key;
        
         byte [] keyBytes;
         byte [] pt;
         byte [] ct;
        
         keyBytes = new byte [] {(byte) 0x01, (byte) 0x23, (byte) 0x45, (byte) 0x67, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
         ct = new byte [] {(byte) 0x20, (byte) 0xAD, (byte) 0xFD, (byte) 0xBF, (byte) 0x1B, (byte) 0xB6, (byte) 0x9F, (byte) 0x4B};
         int counter = 1;
         int counter2 = 0;
         try{
         	
         for(int i = 0; i < 256; i++)
         {
         	cipher = Cipher.getInstance("DES/ECB/NoPadding");
             counter = 1;
             System.out.println("Byte 4 incremented "+counter2+" times");
             for (int j = 0; j < 256; j++)
             {
             	if (counter % 50 == 0)
             		System.out.println(counter);
                 //printByteArray(keyBytes);
                
                
                 for (int k = 0; k<256; k++)
                 {
                     for (int l = 0; l <256; l++)
                     {
                         key = new SecretKeySpec(keyBytes, "DES");
                             
                             cipher.init(Cipher.DECRYPT_MODE, key);
                             pt = cipher.doFinal(ct);
                            
                             //printByteArray(pt);
                             if (new String(pt).equals("Plaintxt"))
                             {
                                 printByteArray(keyBytes);
                                 System.out.println("answer");
                             }
                             //printByteArray(keyBytes);
 
                        keyBytes[7] = (byte) (keyBytes[4] + 1);
                     }
                    keyBytes[6] = (byte) (keyBytes[5] + 1);
                 }
                keyBytes[5] = (byte) (keyBytes[6] + 1);
                 counter++;
             }
            keyBytes[4] = (byte) (keyBytes[7] + 1);
            counter2++;
         }
        //delimits last for loop
     }
     catch(Exception e)
                         {
                             e.printStackTrace();
                         }
     }
 }
