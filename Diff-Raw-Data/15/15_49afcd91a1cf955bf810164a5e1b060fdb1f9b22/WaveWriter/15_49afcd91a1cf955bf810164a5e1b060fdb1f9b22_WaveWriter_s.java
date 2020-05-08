 package de.codesourcery.fft;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.nio.charset.Charset;
 
 import javax.sound.sampled.AudioFormat;
 
 public class WaveWriter {
 
 	private final File outputFile;
 	private final AudioFormat format;
 	private final OutputStream out;
 	
 	private long bytesWritten=0;
 	
 	protected WaveWriter(File outputFile, AudioFormat format) throws FileNotFoundException 
 	{
 		this.outputFile = outputFile;
 		this.format = format;
 		this.out = new FileOutputStream(outputFile.getAbsolutePath()+".raw");
 	}
 	
 	public void write(byte[] buffer,int offset,int length) throws IOException {
 		out.write( buffer , offset , length );
 		bytesWritten+=length;
 	}
 	
     public void write(double[] buffer,int offset,int length) throws IOException 
     {
         final int sampleBits = format.getSampleSizeInBits();
         final byte[] writeBuffer;
         switch( sampleBits ) 
         {
             case 8:
                 writeBuffer = new byte[ length ];
                 for ( int i = 0 ; i < length ; i++ ) {
                     writeBuffer[i] = (byte) buffer[offset+i];
                 }
                 break;
             case 16:
                 writeBuffer = new byte[ length*2 ];
                 int ptr = 0;
                 for ( int i = 0 ; i < length ; i++ ) 
                 {
                     // LSB first
                     writeBuffer[ptr++] = (byte) ( ( (int) buffer[offset+i] ) & 0xff);
                     writeBuffer[ptr++] = (byte) ( ( ( (int) buffer[offset+i] ) >> 8 ) & 0xff);
                 }                
                 break;
             default:
                 throw new UnsupportedOperationException("Not implemented: writing "+sampleBits+"-bit samples");
         }
         out.write( writeBuffer , 0  , writeBuffer.length );
         bytesWritten+=length;
     }	
 	
 	public void close() throws IOException 
 	{
 		out.flush();
 		out.close();
 
 		final FileOutputStream wavOut = new FileOutputStream( outputFile );
 		
 		wavOut.write( toASCII("RIFF" ) ); // 4 bytes
 		long totalFileSize = bytesWritten+4+4+4+2+2+4+4+2+2+4+4;
 		wavOut.write( toUnsignedLong( totalFileSize ) ); 
 		wavOut.write( toASCII("WAVE" ) ); // [ ] 4 bytes
 		
 		// write format
 		wavOut.write( toASCII("fmt " ) ); // [ ] 4 bytes
 		wavOut.write( toUnsignedLong( 16 ) ); // [ ] 'fmt' header length , 4 bytes
 		
 		wavOut.write( toUnsignedShort( 0x001 ) ); // [ ] encoding: PCM , 2 bytes
 		wavOut.write( toUnsignedShort( format.getChannels() ) ); // [ ] 2 bytes
 		wavOut.write( toUnsignedLong( (int) format.getSampleRate() ) ); // [ ] 4 bytes
 		wavOut.write( toUnsignedLong( (long) (( format.getSampleSizeInBits() / 8) * format.getSampleRate() * format.getChannels() ) )  ); // [ ] 4 bytes
 		
 		// <Anzahl der Kanäle> · ((<Bits/Sample (eines Kanals)> + 7) / 8)
 		int frameSize = format.getChannels() * (format.getSampleSizeInBits()+7)/8;
 		wavOut.write( toUnsignedShort( frameSize ) ); // [ ] 2 bytes
 		wavOut.write( toUnsignedShort( format.getSampleSizeInBits() ) ); // [ ] 2 bytes
 		
 		// write data 
		wavOut.write( toASCII("powerSpectrum" ) ); // [ ] 4 bytes
 		wavOut.write( toUnsignedLong( bytesWritten ) ); // [ ] 4 bytes
 		
 		byte[] buffer = new byte[1024];
 		
 		try ( FileInputStream  in = new FileInputStream( outputFile.getAbsolutePath()+".raw" ) ) 
 		{
     		int read = 0;
     		while ( ( read = in.read( buffer ) ) > 0 ) 
     		{
     			wavOut.write( buffer , 0 , read );
     		}
 		} 
 		finally {
 			wavOut.flush();
             wavOut.close();		    
 		}
 		System.out.println("*** wrote WAVE file to: "+outputFile.getAbsolutePath());
 	}
 	
 	private byte[] toUnsignedLong(long value) {
 		
 		byte[] result = new byte[4];
 		// LSB first !!
		result[3] = (byte) ((value >> 24) & 0xff);
		result[2] = (byte) ((value >> 16) & 0xff);
		result[1] = (byte) ((value >> 8) & 0xff);
 		result[0] = (byte) ( value & 0xff);
 		return result;
 	}
 	
 	private byte[] toUnsignedShort(int value) {
 		
 		byte[] result = new byte[2];
 		// LSB first !!
		result[1] = (byte) ((value >> 8) & 0xff);
 		result[0] = (byte) ( value & 0xff);
 		return result;
 	}	
 	
 	private byte[] toASCII(String s) 
 	{
 		Charset utf8charset = Charset.forName("UTF-8");
 		Charset iso88591charset = Charset.forName("ISO-8859-1");
 
 		ByteBuffer inputBuffer = ByteBuffer.wrap(s.getBytes());
 
 		// decode UTF-8
 		CharBuffer data = utf8charset.decode(inputBuffer);
 
 		// encode ISO-8559-1
 		ByteBuffer outputBuffer = iso88591charset.encode(data);
 		return outputBuffer.array();		
 	}
 }
