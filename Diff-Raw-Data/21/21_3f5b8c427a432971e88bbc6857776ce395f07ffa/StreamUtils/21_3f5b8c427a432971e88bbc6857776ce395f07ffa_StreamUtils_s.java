 package com.thebuzzmedia.common.util;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import com.thebuzzmedia.common.charset.DecodingUtils;
 
 public class StreamUtils {
 	public static final String MAX_BUFFER_SIZE_PROPERTY_NAME = "tbm.common.stream.maxBufferSize";
 
 	public static final int MAX_BUFFER_SIZE = Integer.getInteger(
 			MAX_BUFFER_SIZE_PROPERTY_NAME, 32768);
 
 	public static char[] toChars(InputStream stream)
 			throws IllegalArgumentException, IOException {
 		if (stream == null)
 			throw new IllegalArgumentException("stream cannot be null");
 
 		int bytesRead = 0;
 		byte[] buffer = new byte[stream.available() < MAX_BUFFER_SIZE ? stream
 				.available() : MAX_BUFFER_SIZE];
 		char[] output = new char[0];
 
 		while ((bytesRead = stream.read(buffer)) > -1) {
 			// Decode the read segment.
			char[] segment = DecodingUtils.decode(buffer, 0, bytesRead);
 
 			// Append the segment to the output.
 			output = ArrayUtils.append(segment, output);
 		}
 
 		return output;
 	}
 }
