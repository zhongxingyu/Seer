 package sat.utils.file;
 
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Arrays;
 import java.util.Iterator;
 
 /**
  * Un fichier segmentable pour l'envoi par la radio.
  */
 public class SegmentableFile implements Iterable<byte[]> {
 	/**
 	 * Nombre de partie à ce fichier.
 	 */
 	private int segmentsCount = 0;
 	
 	/**
 	 * Chemin vers le fichier d'origine.
 	 */
 	private String pathname;
 	
 	/**
 	 * Le flux d'entrée du fichier.
 	 */
 	private RandomAccessFile file;
 	
 	/**
 	 * La taille maximale d'un segment de fichier.
 	 */
 	public static final int SEGMENT_SIZE = 1024;
 
 	public SegmentableFile(String pathname) throws IOException, NoSuchAlgorithmException {
 		this.pathname = pathname;
 	
 		file = new RandomAccessFile(this.pathname, "rw");
 		segmentsCount = (int) (((float)getSize())/(float)SEGMENT_SIZE);
 	}
 
 	/**
 	 * Retourne un itéreateur pour parcourir ce fichier morceau par morceau.
 	 */
 	public Iterator<byte[]> iterator() {
 		return new Iterator<byte[]>() {
 			/**
 			 * La position actuelle dans le fichier.
 			 */
 			private int currentSegment = 0;
 
 			/**
 			 * Indique s'il y a un prochain morceau.
 			 */
 			public boolean hasNext() {
 				return currentSegment < segmentsCount;
 			}
 
 			/**
 			 * Retourne le prochain morceau de ce fichier.
 			 */
 			public byte[] next() {
 				try {
 					return getSegment(currentSegment++);
 				}
 				catch(IOException e) {
 					e.printStackTrace();
 					return null;
 				}
 			}
 
 			public void remove() {
 				// Not available !
 			}
 		};
 	}
 
 	public byte[] getSegment(int offset) throws IOException {
 		byte[] bytes = new byte[SEGMENT_SIZE];
		
		file.seek(offset * SEGMENT_SIZE);
		int bytesRead = file.read(bytes, 0, SEGMENT_SIZE);
 		
 		// Truncate array if we havent read a full block of data
 		if(bytesRead < SEGMENT_SIZE) {
 			bytes = Arrays.copyOfRange(bytes, 0, bytesRead);
 		}
 		
 		return bytes;
 	}
 	
 	public void writeSegment(int offset, byte[] data) throws IOException {
		file.seek(offset * SEGMENT_SIZE);
		file.write(data);
 	}
 
 	public byte[] getHash() throws NoSuchAlgorithmException, IOException {
 		MessageDigest digest = MessageDigest.getInstance("MD5");
 
 		file.seek(0);
 		
 		byte[] buffer = new byte[1024];
 		int numRead;
 
 		do {
 			numRead = file.read(buffer);
 			if(numRead > 0) {
 				digest.update(buffer, 0, numRead);
 			}
 		}
 		while(numRead != -1);
 
 		return digest.digest();
 	}
 
 	public String getFormat() {
 		String name = this.pathname;
 		int dotOffset = -1;
 
 		for(int i = name.length() - 1; i >= 0; i--) {
 			if(name.charAt(i) == '.') {
 				dotOffset = i;
 				break;
 			}
 		}
 
 		if(dotOffset == -1)
 			return "undefined";
 		else {
 			String format = "";
 			for(int i = dotOffset + 1; i < name.length(); i++)
 				format += name.charAt(i);
 			return format;
 		}
 	}
 
 	public long getSize() throws IOException {
 		return this.file.length();
 	}
	
	public void close() throws IOException {
		file.close();
	}
 }
