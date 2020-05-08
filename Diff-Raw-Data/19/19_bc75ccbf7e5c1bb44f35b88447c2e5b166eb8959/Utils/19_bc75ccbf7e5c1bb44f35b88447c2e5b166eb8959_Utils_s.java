 package main;
 
 import java.io.*;
 import java.util.zip.*;
 
 /**
  * Utility class for things that belong to nowhere, but are needed more then
  * ones.
  */
 public class Utils {
 
 	/**
 	 * Gets an input stream out of a name
 	 * 
 	 * @param resourceName
 	 * @return
 	 */
 	public static InputStream getResourceStream(String resourceName) {
 		return ClassLoader.class.getResourceAsStream(resourceName);
 	}
 
 	public static String[] listResources(String resourceDir) {
 		return Utils.readResource(resourceDir).split("\\n");
 	}
 
 	/**
 	 * Completely reads a resource to a string.
 	 * 
 	 * @param resource
 	 * @return The resource contents.
 	 */
 	public static String readResource(String resource) {
 		StringBuilder sb = new StringBuilder();
 		InputStream fis = getResourceStream(resource);
 		int current;
 		try {
 			current = fis.read();
 			while (current != -1) {
 				sb.append((char) current);
 				current = fis.read();
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return sb.toString();
 	}
 
 	public static byte[] fastRead(InputStream is) throws IOException {
 		// Initialize 20 memory chunks
 		byte[][] chunks = new byte[50][];
 		// Allocate 1MB memory
 		byte[] buffer = new byte[1048576];
 		// read the first 1MB chunk
 		int count = is.read(buffer);
 		int oCount = 0;
 		int index = 0;
 		// while the buffer is completely full, there is probably more to read
 		while (count == 1048576) {
 			// store buffer in chunk array
 			chunks[index] = buffer;
 			// increase current index
 			index++;
 			// allocate a new 1MB array
 			buffer = new byte[1048576];
 			// read again
 			count = is.read(buffer);
 		}
 		is.close();
 		if (count == -1)
 			count = 0;
 		// calculate the overall size
 		oCount = index * 1048576 + count;
 		// allocate an array to hold all data
 		byte[] data = new byte[oCount];
 		// loop through each full 1MB chunk and write it to data
 		for (int i = 0; i < index; i++) {
 			System.arraycopy(chunks[i], 0, data, i * 1048576, 1048576);
 		}
 		// write the last incomplete 1MB chunk to data as well
 		System.arraycopy(buffer, 0, data, index * 1048576, count);
 		return data;
 	}
 
 	public static byte[] fastReadInflated(InputStream is) throws IOException {
 		return fastRead(new InflaterInputStream(is, new Inflater(), 50 * 1048576));
 	}
 
 	public static void arrayCopy(float[] src, int srcPos, float[] dst, int dstPos, int length) {
 		for (int i = 0; i < length; i++) {
 			dst[dstPos + i] = src[srcPos + i];
 		}
 	}
 }
