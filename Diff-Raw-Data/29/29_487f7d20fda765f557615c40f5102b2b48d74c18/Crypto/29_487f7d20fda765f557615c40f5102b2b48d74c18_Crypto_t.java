 
 package merkel.hellman;
 
 /**
  *
  * @author simon
  */
 public class Crypto {
     
     // private key:
         // q: prime larger than sum of superincreasing sequence
         // r: multiplier which is in [1,q)
         // w: superincreasing sequence
     // public key:
         // B: hard sequence
     
     private int[] privateKey = {1,2,4,8,16,32,64,128,256,512,1024,2048,4096,8192,16384,32768}; //sum = 65535
     private int[] publicKey = new int[privateKey.length];
     private int modulo = 65537; //has to bigger than the sum of private key
     private int multiplier = 22588; //this number can't be too small
     private int modularInverse = U.getModInverse(multiplier, modulo);
     private int blocksize = privateKey.length/8; // 8bits in a byte ;) and will always be 2
     
     public Crypto(){
         // generate public key B (hard sequence)
         for (int i = 0; i < privateKey.length; ++i){
             publicKey[i] = privateKey[i] * multiplier % modulo;
         }
     }
     
     public int[] encrypt(byte[] data){
         //U.p("\nIn encrypt()\n===");
         // encrypt for every block and get a number
         int[] result = new int[data.length / blocksize];
         for (int i = 0, j = 0; i < data.length; i += blocksize, ++j){ //i increments in blocks of 2
             byte[] piece = new byte[blocksize]; 
             for (int k = 0; k < blocksize; ++k){
                 piece[k] = data[i+k];
             }
             result[j] = encryptBlock(piece); //encrypt the byte array containing 2 elements
         }
         
         // return the encrypted bytes as int[]
         return result;
     }
     
     public int encryptBlock(byte[] block){
         //U.p("= encrypt block");
        //U.p(U.toCharArr(block));
         
         char[] chardata = U.toCharArr(block);
         int encryptedNum = 0;
         //loop through chardata and multiply each bit by its corresponding number in B[]
         for (int i = 0; i < chardata.length; ++i){
             if (chardata[i] == '1'){
                 encryptedNum += publicKey[i]; 
             }
         }
         
         //U.p(encryptedNum);
         
         return encryptedNum;
     }
     
     public byte[] decrypt(int[] encrypted){
         //U.p("\nIn decrypt()\n===");
         byte[] decrypted = new byte[encrypted.length * blocksize];
         
         // multiply encrypted number by modular inverse and mod by q      
         for (int i = 0; i < encrypted.length; ++i){
             byte[] temp = new byte[blocksize];
             temp = decryptBlock(encrypted[i]);
             for (int j = 0; j < blocksize; ++j){
                 int v = (i*blocksize)+j;
                 decrypted[(i*blocksize)+j] = temp[j];
             }
         }
         
         //U.p("\n(in decrypt(...)) Decrypted: ");
         //U.p(U.toCharArr(decrypted));
         
         // decrypt result with w and return
         return decrypted;
     }
     
     public byte[] decryptBlock(int block){
         //U.p("= decrypt block");
         //U.p(block);
         
         String res = "";
         int decryptedNum = block * modularInverse % modulo;
         byte[] decryptedBlock = new byte[blocksize];
         
         // build string representation of decrypted block
         int temp = decryptedNum;
         for (int i = privateKey.length - 1; i >= 0; --i) {  
             if(temp - privateKey[i] >= 0) {
                 temp = temp - privateKey[i];
                 res = "1" + res; 
             } 
             else {
                 res = "0" + res;
             }
         }
         
         // for each block in the binary String, convert into a byte
         for (int i = 0, j = 0; i < res.length(); i += 8, ++j){
             String tempString = res.substring(i, i+8);
             decryptedBlock[j] = U.toByte(tempString);
         }
         
         //U.p(U.toCharArr(decryptedBlock));
         
         return decryptedBlock;
     }
     
     public boolean test() {
         return /*test8bit() && */test16bit() && test16bit2()/* && test128bit()*/;
     }
     
     // We need to implement padding before including this test
     public boolean test8bit() {
         // init
 //        Crypto crypto = new Crypto();
 //        String data = "";
 //        U.p("\n***Testing " + data + " ***");
 //        int[] encryptedData = crypto.encrypt(data.getBytes());
 //        byte[] decryptedData = crypto.decrypt(encryptedData);
 //        U.p(U.toCharArr(decryptedData));
 //        
 //        String decryptedStr = new String(decryptedData);
 //        
 //        U.p(decryptedStr);
 //        return decryptedStr.equals(data);
         return true;
     }
     
     public boolean test16bit() {
         Crypto crypto = new Crypto();
         String data = "ab";
         //U.p("\n************ Testing " + data + " ************");
         int[] encryptedData = crypto.encrypt(data.getBytes());
         byte[] decryptedData = crypto.decrypt(encryptedData);
         
         String decryptedStr = new String(decryptedData);
         
         //U.p(decryptedStr.equals(data) ? "Passed" : "Failed");
         //U.p("\nDecrypted string: " + decryptedStr);
         return decryptedStr.equals(data);
     }
     
     public boolean test16bit2() {
         Crypto crypto = new Crypto();
         String data = "abcdef";
         //U.p("\n************ Testing " + data + " ************");
         int[] encryptedData = crypto.encrypt(data.getBytes());
         byte[] decryptedData = crypto.decrypt(encryptedData);
         
         String decryptedStr = new String(decryptedData);
         
         //U.p(decryptedStr.equals(data) ? "Passed" : "Failed");
         //System.out.println("Data lenght: " + data.length());
         //U.p("\nDecrypted string: " + decryptedStr);
         return decryptedStr.equals(data);
     }
     
     // We need to confirm key length before including this test
     public boolean test128bit(){
         // init
 //        Crypto crypto = new Crypto();
 //        String data = "1234567890abcdef";
 //        int[] encryptedData = crypto.encrypt(data.getBytes());
 //        byte[] decryptedData = crypto.decrypt(encryptedData);
 //        String decryptedStr = new String(decryptedData);
 //        
 //        U.p(decryptedStr);
 //        return decryptedStr.equals(data);
         return true;
     }
 }
