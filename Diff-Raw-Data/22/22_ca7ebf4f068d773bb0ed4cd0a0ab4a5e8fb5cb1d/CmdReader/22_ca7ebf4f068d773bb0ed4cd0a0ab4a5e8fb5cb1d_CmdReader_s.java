 package togos.ccouch3.cmdstream;
 
 import java.io.BufferedInputStream;
 import java.io.Closeable;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 public class CmdReader implements Closeable {
 	int chunkLeft = 0;
 	public String debugPrefix = null;
 	
 	protected final InputStream is;
 	public CmdReader( InputStream is ) {
 		if( !is.markSupported() ) is = new BufferedInputStream(is);
 		this.is = is;
 	}
 	
 	protected int readFully( InputStream is, byte[] buffer ) throws IOException {
 		int r = 0;
 		int z = is.read( buffer, r, buffer.length - r );
 		while( z > 0 ) {
 			r += z;
 			z = is.read( buffer, r, buffer.length - r );
 		}
 		return r;
 	}
 	
 	protected int readLine( byte[] buf ) throws IOException {
 		is.mark( buf.length );
 		int z;
 		int r = 0;
 		int nl = 0;
 		while( (z = is.read( buf, r, buf.length - r )) > 0 ) {
 			r += z;
 			for( ; nl < r; ++nl ) {
 				if( buf[nl] == '\n' ) {
 					is.reset();
 					is.skip(nl+1);
 					return nl;
 				}
 			}
 		}
 		if( z == -1 ) {
 			return -1;
 		}
 		throw new IOException( "Didn't find newline after reading "+buf.length+" bytes (that's the size of the line buffer that was provided)");
 	}
 	
 	protected static boolean isWhitespace( byte c ) {
 		switch( c ) {
 		case(' '): case('\r'): case('\n'): case('\t'): return true;
 		default: return false;
 		}
 	}
 	
 	final byte[] cmdBuffer = new byte[1024];
 	
 	protected static String unescape( String s ) {
 		return s.replace("%20", " ").replace("%25", "%");
 	}
 	
 	protected static void flushWord( StringBuilder sb, List<String> words ) {
 		if( sb.length() > 0 ) {
 			words.add( unescape(sb.toString()) );
 			sb.setLength(0);
 		}
 	}
 	
 	protected static boolean isEntirelyWhitespace( byte[] buffer, int length ) {
 		for( int i=0; i<length; ++i ) if( !isWhitespace(buffer[i]) ) return false;
 		return true;
 	}
 	
 	public String[] readCmd() throws IOException {
 		int lineLength;
 		do {
 			lineLength = readLine( cmdBuffer );
 		} while( lineLength >= 0 && isEntirelyWhitespace( cmdBuffer, lineLength ) );
 		if( lineLength < 0 ) return null;
 		List<String> words = new ArrayList<String>();
 		StringBuilder sb = new StringBuilder();
 		for( int i=0; i<lineLength; ++i ) {
 			if( isWhitespace(cmdBuffer[i]) ) {
 				flushWord( sb, words );
 			} else {
 				sb.append( (char)cmdBuffer[i] );
 			}
 		}
 		flushWord( sb, words );
 		String[] r = words.toArray(new String[words.size()]);
 		if( debugPrefix != null ) System.err.println( debugPrefix + CmdWriter.encode(r) );
 		return r;
 	}
 	
 	// chunk <size>
 	// end-chunks
 	
 	/**
 	 * Read chunk data!
 	 * 
 	 * @param buffer
 	 * @return length of chunk, or -1 if we have reached the end of chunks.
 	 * @throws IOException
 	 */
 	public int readChunk( byte[] buffer, int off, int len ) throws IOException {
 		int z;
 		if( chunkLeft == 0 ) {
 			// Then read the next one!
 			String[] c = readCmd();
 			if( c.length == 0 ) {
 				throw new IOException("Zero-length command!");
 			}
 			if( "end-chunks".equals(c[0]) ) {
 				return -1;
 			}
 			if( !"chunk".equals(c[0]) ) {
 				throw new IOException("Not a chunk! "+c[0]);
 			}
 			chunkLeft = Integer.parseInt(c[1]);
 		}
 		z = is.read(buffer, off, Math.min( len, chunkLeft ) );
 		if( z == -1 ) throw new IOException("Hit end of stream while reading chunk");
 		chunkLeft -= z;
 		return z;
 	}
 	
 	public InputStream getChunkInputStream() {
 		return new CmdChunkInputStream(this);
 	}
 	
 	@Override
 	public void close() throws IOException {
 		is.close();
 	}
 }
