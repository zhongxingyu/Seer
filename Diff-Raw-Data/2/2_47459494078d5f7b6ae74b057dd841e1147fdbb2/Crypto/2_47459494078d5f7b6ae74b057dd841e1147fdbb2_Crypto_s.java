 package safeguard;
 
 import java.nio.file.Path;
 import java.nio.file.Paths;
 
 import com.sun.jna.Library;
 import com.sun.jna.Memory;
 import com.sun.jna.Native;
 import com.sun.jna.Pointer;
 
 public class Crypto {
     
     public interface cryptoDLL extends Library {
    	cryptoDLL INSTANCE = (cryptoDLL) Native.loadLibrary("C:\\SafeGuard\\AES256.dll", cryptoDLL.class); //FIXME hardcoded
         void hash_512(Pointer message, long length, Pointer out);
         //void hash_512(const unsigned char *message,unsigned long long length,unsigned char *out)
         void hash_256(Pointer message, long length, Pointer out);
         //void hash_256(const unsigned char *message,unsigned long long length,unsigned char *out)
         int encrypt_aes256_ecb(Pointer pbsource, int cbsource, Pointer pbkey, Pointer pboutput);
         //int encrypt_aes256_ecb(BYTE* pbsource, DWORD cbsource, BYTE* pbkey, BYTE* pboutput)
         int decrypt_aes256_ecb(Pointer pbsource, int cbsource, Pointer pbkey, Pointer pboutput, Pointer pcboutput);
         //int decrypt_aes256_ecb(BYTE* pbsource, DWORD cbsource, BYTE* pbkey, BYTE* pboutput, DWORD* pcboutput)
         int file_encrypt_aes256_ecb(Pointer pbsource, int cbsource, Pointer pbkey, Pointer file_out);
         //int file_encrypt_aes256_ecb(BYTE* pbsource, DWORD cbsource, BYTE* pbkey, char* file_out)
         int file_decrypt_aes256_ecb(Pointer file_in, Pointer pboutput, int cbsource, Pointer pbkey, Pointer pcboutput);
         //int file_decrypt_aes256_ecb(char* file_in, BYTE* pboutput, DWORD cbsource, BYTE* pbkey, DWORD* pcboutput)
         int PRGB(Pointer dst, int cbbytes);
         //int PRGB(BYTE *dst, DWORD cbbytes) 
     }
     private static Crypto cryptoInstance;
     private static cryptoDLL cryptoDll;
     private static Pointer key;
     
     private Crypto() {
         cryptoDll = cryptoDLL.INSTANCE;
     }
     public static Crypto getInstance() {
     	if (cryptoInstance != null)
     		return cryptoInstance;
     	else
     		return cryptoInstance = new Crypto();
     }
     public static void setCryptoKey(String key) {
         Memory keyMemory = new Memory(key.length() + 1);
         keyMemory.setString(0, key);
         Crypto.key = keyMemory.getPointer(0);
     }
     
     int encryptFile(Path filename, byte[] content) {
         Memory contentMemory = new Memory(content.length);
         contentMemory.write(0, content, 0, content.length);
         Memory filenameMemory = new Memory(filename.toString().length() + 1);
         filenameMemory.setString(0, filename.toString());
         return 0;//cryptoDll.file_encrypt_aes256_ecb(filenameMemory.getPointer(0), contentMemory.getPointer(0), key);
     }
     String decryptFile(Path filename) {
     	return "";
         /*Pointer contentPointer = cryptoDll.decryptFileWithDll(filename.toString(), key);
         String content = contentPointer.getString(0);
         return content;*/
     }
     String getPasswordHash(String password) {
         int length = password.length() + 1;
         Memory passwordMemory = new Memory(length);
         passwordMemory.setString(0, password);
         Memory hashMemory = new Memory(32);
         cryptoDll.hash_256(passwordMemory, length, hashMemory);
         String hash = hashMemory.getString(0);
         return hash;
     }
 }
