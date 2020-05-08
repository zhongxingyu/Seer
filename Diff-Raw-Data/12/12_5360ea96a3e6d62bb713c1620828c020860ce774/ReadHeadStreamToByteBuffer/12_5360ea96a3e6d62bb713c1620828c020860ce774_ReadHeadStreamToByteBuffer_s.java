 package ahs.io;
 
 import ahs.util.*;
 import ahs.util.thread.*;
 
 import java.io.*;
 import java.nio.*;
 import java.util.*;
 
 public class ReadHeadStreamToByteBuffer extends ReadHeadAdapter<ByteBuffer> {
 	public ReadHeadStreamToByteBuffer(InputStream $base, int $blockSize) {
 		super();
 		this.$base = $base;
 		this.$blockSize = $blockSize;
 	}
 	
 	private final InputStream			$base;
 	private final int				$blockSize;
 	
 	protected ByteBuffer getChunk() throws IOException {
 		byte[] $bats = new byte[$blockSize];
 		int $actualSizeRead, $currentSum = 0;
 		
 		// try to get as much as we can
 		while ($currentSum < $blockSize) {
 			$actualSizeRead = $base.read($bats, $currentSum, $blockSize-$currentSum);
			if ($actualSizeRead == -1)	// EOF
 				baseEof();
			else
 				$currentSum += $actualSizeRead;
 		}
 		
 		if ($currentSum > 0)
 			return ByteBuffer.wrap($bats, 0, $currentSum);
 		return null;
 	}
 	
 	public void close() throws IOException {
 		$base.close();
 	}
 	
 	
 	
 	/**
 	 * <p>
 	 * This method has almost identical semantics to <code>readAll()</code> (and uses
 	 * that method internally, so the same caveats about multiple invocations apply),
 	 * except that it merges all of the ByteBuffers back into a single buffer before
 	 * returning it. More memory- and copy-efficient implementations should probably
 	 * be sought if you intend to simply read a complete file all in one go, but this
 	 * is tolerable for extremely simple applications that are not concerned about
 	 * such resource bounds.
 	 * </p>
 	 */
 	public ByteBuffer readCompletely() {
 		List<ByteBuffer> $bla = readAll();
 		ByteBuffer $v = ByteBuffer.allocate($blockSize * $bla.size());	// as of this moment you're consuming twice the memory that a more direct implementation would require.
 		Iterator<ByteBuffer> $itr = $bla.iterator();
 		while ($itr.hasNext()) {
 			$v.put($itr.next());	// calling rewind on $bb would be redundant; we know where it came from after all.
 			$itr.remove();		// let it become GC'able as soon as possible
 		}
 		$v.flip();
 		return $v;
 	}
 }
