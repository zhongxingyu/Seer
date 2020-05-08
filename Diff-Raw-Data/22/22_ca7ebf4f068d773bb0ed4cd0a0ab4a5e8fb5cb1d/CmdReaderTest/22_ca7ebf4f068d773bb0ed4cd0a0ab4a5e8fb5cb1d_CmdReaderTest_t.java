 package togos.ccouch3.cmdstream;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PipedInputStream;
 import java.io.PipedOutputStream;
 import java.util.Random;
 
 import junit.framework.TestCase;
 
 public class CmdReaderTest extends TestCase
 {
 	PipedOutputStream pos;
 	PipedInputStream pis;
 	CmdWriter w;
 	CmdReader r;
 	
 	public void setUp() throws IOException {
 		pos = new PipedOutputStream();
 		// Give a good buffer size so we don't deadlock reading+writing from the same thread:
 		pis = new PipedInputStream(pos, 65536);
 		w = new CmdWriter(pos);
 		r = new CmdReader(pis);
 		
 	}
 	
 	protected void assertEquals( String[] s1, String[] s2 ) {
 		assertEquals( s1.length, s2.length );
 		for( int i=0; i<s1.length; ++i ) assertEquals(s1[i], s2[i]);
 	}
 	
 	protected void testCmd( String[] cmd ) throws IOException {
 		w.writeCmd( cmd );
 		assertEquals( cmd, r.readCmd() );
 	}
 	
 	public void testCmd() throws IOException {
 		testCmd( new String[] { "hello", "1", "2", "3" } );
 	}
 	
 	public void testSkipNewlines() throws IOException {
 		w.writeNewline();
 		w.writeNewline();
 		w.writeNewline();
 		testCmd( new String[] { "hello", "1", "2", "3" } );
 	}
 	
 	public void testEof() throws IOException {
 		pos.close();
 		assertNull( r.readCmd() );
 	}
 	
 	public void testRead2Things() throws IOException {
 		String[] thing1 = new String[] { "hello", "1", "2", "3" };
 		String[] thing2 = new String[] { "blox", "of", "shox" };
 		w.writeCmd( thing1 );
 		w.writeCmd( thing2 );
 		assertEquals( thing1, r.readCmd() );
 		assertEquals( thing2, r.readCmd() );
 	}
 	
 	public void testReadChunk() throws IOException {
 		byte[] buf = new byte[10];
 		
 		w.writeChunk( new byte[] { 5, 0, 1, 2, 3, 6 }, 1, 4 );
 		w.writeChunk( new byte[] { 5, 0, 1, 2, 3, 6 }, 1, 4 );
 		w.endChunks();
 		
 		assertEquals( 4, r.readChunk(buf, 0, buf.length) );
 		assertEquals( 0, buf[0] );
 		assertEquals( 1, buf[1] );
 		assertEquals( 2, buf[2] );
 		assertEquals( 3, buf[3] );
 		
 		assertEquals( 4, r.readChunk(buf, 0, buf.length) );
 		assertEquals( 0, buf[0] );
 		assertEquals( 1, buf[1] );
 		assertEquals( 2, buf[2] );
 		assertEquals( 3, buf[3] );
 		
 		assertEquals( -1, r.readChunk(buf, 0, buf.length) );
 	}
 
 	public void testReadChunkInSmallerPieces() throws IOException {
 		byte[] buf = new byte[3];
 		
 		w.writeChunk( new byte[] { 5, 0, 1, 2, 3, 6 }, 1, 4 );
 		w.writeChunk( new byte[] { 5, 0, 1, 2, 3, 6 }, 1, 4 );
 		w.endChunks();
 		
 		assertEquals( 3, r.readChunk(buf, 0, buf.length) );
 		assertEquals( 0, buf[0] );
 		assertEquals( 1, buf[1] );
 		assertEquals( 2, buf[2] );
 		assertEquals( 1, r.readChunk(buf, 0, buf.length) );
 		assertEquals( 3, buf[0] );
 		
 		assertEquals( 3, r.readChunk(buf, 0, buf.length) );
 		assertEquals( 0, buf[0] );
 		assertEquals( 1, buf[1] );
 		assertEquals( 2, buf[2] );
 		assertEquals( 1, r.readChunk(buf, 0, buf.length) );
 		assertEquals( 3, buf[0] );
 		
 		assertEquals( -1, r.readChunk(buf, 0, buf.length) );
 	}
 	
	public void testReadCmdWhileReadingChunk() throws IOException {
		byte[] buf = new byte[3];
		
		w.writeChunk( new byte[] { 1, 2, 3, 4 }, 0, 4 );
		w.endChunks();
		
		r.readChunk(buf, 0, 3);
			
		try {
			r.readCmd();
			fail( "Trying to read a command while there are chunk bytes left should have caused an IOException");
		} catch( IOException e ) {
			// This is proper.
		}
	}
 	
 	public void testChunkInputStream() throws IOException {
 		byte[] chunk = new byte[1234];
 		new Random().nextBytes(chunk);
 		
 		String[] thing1 = new String[] {"foo","bar","baz" };
 		String[] thing2 = new String[] {"quux","quuux","quuuux" };
 		
 		w.writeCmd( thing1 );
 		w.writeChunk( chunk, 0, chunk.length );
 		w.writeChunk( chunk, 0, chunk.length );
 		w.endChunks();
 		w.writeCmd( thing2 );
 		
 		assertEquals( thing1, r.readCmd() );
 		InputStream cis = new CmdChunkInputStream( r );
 		byte[] buf = new byte[2468];
 		assertEquals( 1234, cis.read( buf ) );
 		for( int i=0; i<1234; ++i ) assertEquals( chunk[i], buf[i] );
 		assertEquals( 1234, cis.read( buf ) );
 		for( int i=0; i<1234; ++i ) assertEquals( chunk[i], buf[i] );
 		assertEquals( -1, cis.read( buf ) );
 		cis.close();
 		assertEquals( thing2, r.readCmd() );
 	}
 
 	public void testCloseChunkInputStream() throws IOException {
 		byte[] chunk = new byte[1234];
 		new Random().nextBytes(chunk);
 		
 		String[] thing1 = new String[] {"foo","bar","baz" };
 		String[] thing2 = new String[] {"quux","quuux","quuuux" };
 		
 		w.writeCmd( thing1 );
 		w.writeChunk( chunk, 0, chunk.length );
 		w.writeChunk( chunk, 0, chunk.length );
 		w.endChunks();
 		w.writeCmd( thing2 );
 		
 		assertEquals( thing1, r.readCmd() );
 		InputStream cis = new CmdChunkInputStream( r );
 		byte[] buf = new byte[100];
 		assertEquals( 100, cis.read( buf ) );
 		cis.close(); // Should skip the rest of the chunks
 		assertEquals( thing2, r.readCmd() );
 	}
 }
