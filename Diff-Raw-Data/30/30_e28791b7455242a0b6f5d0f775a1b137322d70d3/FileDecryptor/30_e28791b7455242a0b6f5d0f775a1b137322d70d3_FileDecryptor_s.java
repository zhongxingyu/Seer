 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 public class FileDecryptor
 {
    private RSAPrivateKey key;
   private input;
 
    public FileDecryptor(File input, RSAPrivateKey key)
    {
       this.input = input;
       this.key = key;
    }
 
   public void decrypt()
    {
    }
 
    public void write(File output)
    {
    }
 }
