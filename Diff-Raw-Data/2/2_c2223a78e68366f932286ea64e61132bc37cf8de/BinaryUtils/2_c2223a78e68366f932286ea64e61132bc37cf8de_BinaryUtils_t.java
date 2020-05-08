 public class BinaryUtils {
   public static int byteToUint(byte[] arr, int start) {
     return ((int) arr[start] & 0xFF);
   }
 
   public static int beShortToUint(byte[] arr, int start) {
     return (byteToUint(arr, start) << 8) | 
       byteToUint(arr, start + 1);
   }
 
   public static int beWordToUint(byte[] arr, int start) {
     return (byteToUint(arr, start) << 24) |
       (byteToUint(arr, start + 1) << 16) |
       (byteToUint(arr, start + 2) << 8) |
       byteToUint(arr, start + 3);
   }
 
   public static void uintToByte(int i, byte[] arr, int start) {
     arr[start] = (byte) (i & 0xFF);
   }
 
   public static void uintToBEShort(int i, byte[] arr, int start) {
     uintToByte(i >> 8, arr, start);
    uintToByte(i, arr, start + 1);
   }
 
   public static void uintToBEWord(int i, byte[] arr, int start) {
     uintToByte(i >> 24, arr, start);
     uintToByte(i >> 16, arr, start + 1);
     uintToByte(i >> 8, arr, start + 2);
     uintToByte(i, arr, start + 3);
   }
 }
