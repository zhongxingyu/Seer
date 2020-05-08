 package ex2;
 
 import java.io.File;
 import java.io.FileDescriptor;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import javax.swing.text.html.MinimalHTMLWriter;
 
 public class myFileInputStream extends FileInputStream {
 
	public static final int BOCK_SIZE = 16;
 	
 	public myFileInputStream(String name) throws FileNotFoundException {
 		super(name);
 	}
 
 	public myFileInputStream(File file) throws FileNotFoundException {
 		super(file);
 	}
 	
 	public myFileInputStream(FileDescriptor fd){
 		super( fd);
 	}
 	
     public int read(byte b[], int off, int len) throws IOException {
 
     	int n = 0;
     	int bytes_to_read = (len > BOCK_SIZE) ? BOCK_SIZE : len;
     	byte[] cipher;
     	byte[] plain = new byte[BOCK_SIZE];
     	
     	System.out.println("##################### IN : " + bytes_to_read + " | " + off + " | " + len);    	
         n = super.read(b, off, bytes_to_read);
         
         if ( n != -1 )
         {
         	System.out.println(new String(b,off,n));
             System.out.println(">>>>>>>>> " + n);
             
             for(int i = off, j = 0; i < off + n; i++, j++)
             	plain[j] = b[i];
             
 			cipher = SimpleSymmetricBC.cipherBC(plain, BOCK_SIZE);
 
 			for (int i = off, j = 0; j < BOCK_SIZE; i++, j++)
 				b[i] = cipher[j];
 
 			
 			System.out.println(Utils.toHex(cipher, bytes_to_read));
 			System.out.println(Utils.toHex(b, bytes_to_read+off));
         }       
         
         return ( n != -1 ) ? BOCK_SIZE : n;
     }
     
     public int read(byte b[]) throws IOException {
         return this.read(b, 0, b.length);
     }
 	
 	/**
 	 * @param args
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws IOException {
 		int nread = 0;
 		String fileName = args[0];
 		
 		myFileInputStream myfi = new myFileInputStream(fileName);
 		FileInputStream fi = new FileInputStream(fileName);
 		
 		byte[] bc = new byte[1024];		
 		byte[] b =  new byte[1024];
 		
 		myfi.read(bc,0,17);
 		nread = fi.read(b,0,17);
 		
 		System.out.println( Utils.toHex(bc, nread)  );
 		System.out.println( Utils.toHex(b , nread)  );
 		System.out.println( Utils.toHex(SimpleSymmetricBC.deCipherBC(bc), nread ));
 		System.out.println(myfi.available());
 		
 	}
 
 }
