 	package ahs.io;
 
 import ahs.test.*;
 import ahs.util.*;
 import ahs.util.thread.*;
 
 import java.io.*;
 import java.nio.*;
 import java.util.*;
 import java.util.concurrent.atomic.*;
 
 public class ReadHeadStreamToByteBufferTest extends TestCase {
 	public void setUp() {
 		$dat1 = new ByteArrayInputStream(new byte[] { 0x0, 0x1, 0x2, 0x3,    0x4, 0x5, 0x6, 0x7,    0x8, 0x9, 0xA });
 		$dat2 = new ByteArrayInputStream(Big);
 	}
 	
 	private static final int TIMES = 10000;
 	private static final byte[] Block1 = new byte[] { 0x0, 0x1, 0x2, 0x3 };
 	private static final byte[] Block2 = new byte[] { 0x4, 0x5, 0x6, 0x7 };
 	private static final byte[] Block3 = new byte[] { 0x8, 0x9, 0xA };
 	private static final byte[] Big;
 	private static final int BigBlocks = 1024 * 1024;
 	static {
 		byte[][] $t = new byte[BigBlocks][];
 		for (int $i = 0; $i < $t.length; $i++) $t[$i] = Block1;
 		Big = Arr.cat(Arr.cat($t),Block3);
 	}
 	private InputStream $dat1, $dat2;
 	
 	public void testBasic() {
 		ReadHead<ByteBuffer> $rh = new ReadHeadStreamToByteBuffer($dat1, 4);
		
 		assertNull($rh.readNow());
 		assertFalse($rh.isClosed());
 		assertFalse($rh.hasNext());
 		
 		$rh.getPump().run(1);
 		assertTrue($rh.hasNext());
 		assertEquals(Block1, $rh.read().array());
 		assertFalse($rh.hasNext());
 		assertNull($rh.readNow());
 		assertNull($rh.readNow());
 		assertFalse($rh.isClosed());
 		
 		new PumperBasic($rh.getPump()).run();	// $dat1 always returns a graceful EOF.
 		assertTrue($rh.isClosed());
 		// i'd like to put an assert for the underlying stream being closed at this point, but InputStream doesn't even have a getter for that.
 		assertTrue($rh.hasNext());
 		assertEquals(Block2, Arr.toArray($rh.readNow()));
 		assertTrue($rh.hasNext());
 		assertEquals(Block3, Arr.toArray($rh.read()));
 		assertFalse($rh.hasNext());
 		
 	}
 	
 	public void testBasicAligned() {
 		ReadHead<ByteBuffer> $rh = new ReadHeadStreamToByteBuffer(new ByteArrayInputStream(new byte[] { 0x0, 0x1, 0x2, 0x3,    0x4, 0x5, 0x6, 0x7,    0x0, 0x1, 0x2, 0x3 }), 4);
 		assertNull($rh.readNow());
 		assertFalse($rh.isClosed());
 		assertFalse($rh.hasNext());
 		
 		$rh.getPump().run(1);
 		assertTrue($rh.hasNext());
 		assertEquals(Block1, $rh.read().array());
 		assertFalse($rh.hasNext());
 		assertNull($rh.readNow());
 		assertNull($rh.readNow());
 		assertFalse($rh.isClosed());
 		
 		new PumperBasic($rh.getPump()).run();	// $dat1 always returns a graceful EOF.
 		assertTrue($rh.isClosed());
 		assertTrue($rh.hasNext());
 		assertEquals(Block2, Arr.toArray($rh.readNow()));
 		assertTrue($rh.hasNext());
 		assertEquals(Block1, Arr.toArray($rh.read()));
 		//assertEquals(new byte[0], Arr.toArray($rh.read()));	//  <-- this is what we want to avoid
 		assertFalse($rh.hasNext());
 	}
 	
 	public void testBasic10000X() {
 		testBasic();
 		for (int $i = 1; $i < TIMES; $i++) {
 			setUp();
 			testBasic();
 		}
 	}
 	
 	public void testReadAll() {
 		ReadHead<ByteBuffer> $rh = new ReadHeadStreamToByteBuffer($dat1, 4);
 		new PumperBasic($rh.getPump()).run();
 		
 		assertEquals(Block1, $rh.read().array());
 		List<ByteBuffer> $bbs = $rh.readAll();
 		assertEquals(2, $bbs.size());
 		assertEquals(Block2, Arr.toArray($bbs.get(0)));
 		assertEquals(Block3, Arr.toArray($bbs.get(1)));
 		//assertEquals(0, $rh.readAll().length);	// actually, i'm not allowed to make this assertion according to the general contract in ReadHead.
 	}
 	
 	public void testReadNao() {
 		ReadHead<ByteBuffer> $rh = new ReadHeadStreamToByteBuffer($dat1, 4);
 		new PumperBasic($rh.getPump()).run();
 		
 		assertEquals(Block1, $rh.read().array());
 		List<ByteBuffer> $bbs = $rh.readAllNow();
 		assertEquals(2, $bbs.size());
 		assertEquals(Block2, Arr.toArray($bbs.get(0)));
 		assertEquals(Block3, Arr.toArray($bbs.get(1)));
 		assertEquals(0, $rh.readAll().size());
 	}
 	
 	public void testHarder() {
 		ReadHead<ByteBuffer> $rh = new ReadHeadStreamToByteBuffer($dat2, 4);
 		assertNull($rh.readNow());
 		assertFalse($rh.isClosed());
 		assertFalse($rh.hasNext());
 		
 		new PumperBasic($rh.getPump()).start();
 		
 		for (int $i = 0; $i < BigBlocks; $i++)
 			assertEquals(Block1, Arr.toArray($rh.read()));
 		assertTrue($rh.isClosed());
 		assertTrue($rh.hasNext());
 		assertEquals(Block3, Arr.toArray($rh.read()));
 		assertFalse($rh.hasNext());
 	}
 	
 	public void testBlockingAndReadFromTwoThreads() {
 		final ReadHead<ByteBuffer> $rh = new ReadHeadStreamToByteBuffer($dat2, 4);
 		final AtomicInteger $eated = new AtomicInteger();	// all uses of this are crappy hacks.
 		
 		new Thread() {
 			public void run() {
 				for (int $i = 0; $i < BigBlocks / 2; $i++) {
 					assertEquals(Block1, Arr.toArray($rh.read()));
 					$eated.incrementAndGet();
 				}
 			}
 		}.start();
 		new Thread() {
 			public void run() {
 				for (int $i = 0; $i < BigBlocks / 2; $i++) {
 					assertEquals(Block1, Arr.toArray($rh.read()));
 					$eated.incrementAndGet();
 				}
 			}
 		}.start();
 		
 		new PumperBasic($rh.getPump()).start();
 		
 		while (true) if ($eated.get() == BigBlocks) break; else X.chill(100);
 		
 		assertTrue($rh.isClosed());
 		assertEquals(Block3, Arr.toArray(((ReadHeadStreamToByteBuffer)$rh).readCompletely()));
 	}
 }
