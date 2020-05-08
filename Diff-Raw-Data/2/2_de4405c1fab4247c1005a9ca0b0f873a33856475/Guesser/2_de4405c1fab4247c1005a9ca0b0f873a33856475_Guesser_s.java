 package xkcd.hash;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.Arrays;
 
 import org.bouncycastle.crypto.digests.Skein;
 
 public class Guesser extends Thread {
 	int threadIndex;
 	short best = 1000;
 	int count = 0;
 	byte[] current;
 	
 	short bitcount;
 	int a,b,c,d,e,f,g,h,i,j;
 
 	Skein skein = new Skein(1024, 1024);
 		
 	/* target hash */
 	String targetString = "5b4da95f5fa08280fc9879df44f418c8f9f12ba424b7757de02bbdf" +
 			"bae0d4c4fdf9317c80cc5fe04c6429073466cf29706b8c25999ddd2f6540d44" +
 			"75cc977b87f4757be023f19b8f4035d7722886b78869826de916a79cf9c94cc" +
 			"79cd4347d24b567aa3e2390a573a373a48a5e676640c79cc70197e1c5e7f902" +
 			"fb53ca1858b6";
 	
 	byte[] target = new byte[128];
 		
 	public Guesser(long seed, int threadIndex) {
 		System.out.println("Seeding t" + threadIndex + " with long '" + String.valueOf(seed) + "'");
 		this.threadIndex = threadIndex;
 		try {
 			current = String.valueOf(seed).getBytes("UTF-8");
 		} catch(UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		skein.updateBits(current, 0, current.length * 8);
 		current = skein.doFinal();
 		
 		//set up the byte[] for the target hash
 		for(int i=0; i<targetString.length(); i+=2) {
 			int b = Integer.parseInt(targetString.substring(i, i+2), 16);
 			target[i/2] = (byte)b;
 		}
 	}
 	
 	public void run() {
 		System.out.println("Starting t" + threadIndex + ".");
 		long startTime = System.currentTimeMillis();
 		short diff;
 		byte[] hashed;
 		while(true) {
 			skein.updateBits(current, 0, 1024);
 			hashed = skein.doFinal();
 			diff = compare(target, hashed);
 			if(diff < best) {
 				best = diff;
 				System.out.println("t" + threadIndex + ": " + best + " from " + Arrays.toString(current));
 				try{
 					send(current);
 				} catch(IOException e) {
 					System.out.println("failed to send.");
 					e.printStackTrace();
 				}
 			}
 			
 			current = hashed;
 			count++;
 			if(count % 1000000 == 0) {
 				long elapsed = (System.currentTimeMillis() - startTime)/1000;
 				System.out.print("t" + threadIndex + " ");
 				System.out.print("Hashes: " + count + " in " + elapsed + "s; ");
 				System.out.println("average " + (count / elapsed) + " hashes/sec");
 			}
 		}
 	}
 	
 	private short compare(byte[] b1, byte[] b2) {
 		bitcount = 0;
 		for(int index=0; index<b1.length; index++) {
 			a = (byte)(b1[index] ^ b2[index]);
 			b = (byte)(a & 0x55);
 			c = (byte)((a>>1) & 0x55);
 			d = (byte)(b + c);
 			e = (byte)(d & 0x33);
 			f = (byte)((d>>2) & 0x33);
 			g = (byte)(e + f);
 			h = (byte)(g & 0xF);
 			i = (byte)((g>>4) & 0xF);
 			j = (byte)(h + i);
 			
 			bitcount += (byte)j;
 		}
 		return bitcount;
 	}
 	
 	private void send(byte[] b) throws IOException {
		URL url = new URL("http://almamater.xkcd.com/?edu=davidrorr.com");
 		HttpURLConnection c = (HttpURLConnection) (url.openConnection());
 		c.setDoOutput(true);
 		c.setRequestMethod("POST");
 		
 		BufferedOutputStream writer = new BufferedOutputStream(c.getOutputStream());
 		writer.write("hashable=".getBytes());
 		writer.write(b);
 		writer.flush();
 		
 		BufferedReader reader = new BufferedReader(new InputStreamReader(c.getInputStream()));
 		
 		writer.close();
 		reader.close();
 	}
 	
 	public static short compareByteArrays(byte[] b1, byte[] b2) {
 		short bitcount = 0;
 		int a,b,c,d,e,f,g,h,i,j;
 		for(int index=0; index<b1.length; index++) {
 			a = (byte)(b1[index] ^ b2[index]);
 			b = (byte)(a & 0x55);
 			c = (byte)((a>>1) & 0x55);
 			d = (byte)(b + c);
 			e = (byte)(d & 0x33);
 			f = (byte)((d>>2) & 0x33);
 			g = (byte)(e + f);
 			h = (byte)(g & 0xF);
 			i = (byte)((g>>4) & 0xF);
 			j = (byte)(h + i);
 			
 			bitcount += (byte)j;
 		}
 		return bitcount;
 	}
 }
