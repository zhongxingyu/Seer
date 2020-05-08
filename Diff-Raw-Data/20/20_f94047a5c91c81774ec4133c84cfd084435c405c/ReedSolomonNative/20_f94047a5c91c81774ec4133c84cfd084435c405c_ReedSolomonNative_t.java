 package warrenfalk.reedsolomon;
 
 import java.net.URL;
 import java.nio.ByteBuffer;
 import java.nio.file.FileSystem;
 import java.nio.file.FileSystems;
 import java.nio.file.Files;
 import java.nio.file.Path;
 
 import warrenfalk.util.math.GaloisField;
 import warrenfalk.util.math.MatrixR;
 
 public class ReedSolomonNative {
 	final int dataSize;
 	final int checksumSize;
 	final int gfbits;
 	final int gfprimitive;
 	final ByteBuffer codingMatrixBuffer;
 	final ByteBuffer recoveryMatrixBuffer;
 	final ByteBuffer gflogBuffer;
 	final ByteBuffer gfinvlogBuffer;
 	final int[] recoveryMap;
 	
 	static int state;
 	final static String LIB = "reedsolomon";
 	
 	private ReedSolomonNative(int dataSize, int checksumSize, MatrixR codingMatrix, MatrixR recoveryMatrix, int[] recoveryMap, GaloisField gf) {
 		this.dataSize = dataSize;
 		this.checksumSize = checksumSize;
 		this.codingMatrixBuffer = matrixToBuffer(codingMatrix);
 		this.recoveryMatrixBuffer = matrixToBuffer(recoveryMatrix);
 		this.gflogBuffer = intsToBytesBuffer(gf.log);
 		this.gfinvlogBuffer = intsToBytesBuffer(gf.invlog);
 		this.gfbits = gf.bits;
 		this.gfprimitive = gf.primitive;
 		this.recoveryMap = recoveryMap;
 	}
 	
 	private ByteBuffer intsToBytesBuffer(int[] bytes) {
 		if (bytes == null)
 			return null;
 		ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);
 		for (int i = 0; i < bytes.length; i++)
 			buffer.put((byte)(bytes[i] & 0xFF));
 		return buffer;
 	}
 
 	private static ByteBuffer matrixToBuffer(MatrixR matrix) {
 		if (matrix == null)
 			return null;
 		int size = matrix.width * matrix.height;
 		ByteBuffer buffer = ByteBuffer.allocateDirect(size);
 		for (int r = 0; r < matrix.height; r++)
 			for (int c = 0; c < matrix.width; c++)
 				buffer.put((byte)(matrix.get(r, c) & 0xFF));
 		buffer.position(0);
 		return buffer;
 	}
 	
 	public static ReedSolomonNative getNativeHelper(ReedSolomonCodingDomain.Coder coder) {
 		ReedSolomonCodingDomain domain = coder.getDomain();
 		return getNativeHelper(domain.dataSize, domain.checksumSize, domain.gf, domain.codingMatrix, coder.recoveryMatrix, coder.validSymbolMap);
 	}
 	
 	public static ReedSolomonNative getNativeHelper(int dataSize, int checksumSize, GaloisField gf, MatrixR codingMatrix, MatrixR recoveryMatrix, int[] recoveryMap) {
 		if (!initialize())
 			return null;
 		return new ReedSolomonNative(dataSize, checksumSize, codingMatrix, recoveryMatrix, recoveryMap, gf);
 	}
 	
 	static boolean initialize() {
 		if (state != 0)
 			return state == 1;
 		FileSystem fs = FileSystems.getDefault();
 		String classFileName = "/" + ReedSolomonNative.class.getCanonicalName().replace('.', '/') + ".class";
 		URL url = ReedSolomonNative.class.getResource(classFileName);
 		if ("file".equals(url.getProtocol())) {
 			// if we're not in a jar, find the location of this class and try that folder next
 			Path classPath = fs.getPath(url.getPath());
 			Path classFolder = classPath.getParent();
 			Path folder = classFolder;
 			while (folder != null) {
 				if (tryLoadNative(folder.resolve(LIB + ".so"))) {
 					state = 1;
 					return true;
 				}
 				// next try a "native" folder under that
 				if (tryLoadNative(folder.resolve("native").resolve(LIB + ".so"))) {
 					state = 1;
 					return true;
 				}
 				folder = folder.getParent();
 			}
 		}
 		if (tryLoadNative(LIB)) {
 			state = 1;
 			return true;
 		}
 		state = -1;
 		return false;
 	}
 	
 	static boolean tryLoadNative(Path libPath) {
 		if (!Files.exists(libPath))
 			return false;
 		return tryLoadNative(libPath.toString());
 	}
 	
 	static boolean tryLoadNative(String lib) {
 		try {
 			if (lib.contains("/"))
 				System.load(lib);
 			else
 				System.loadLibrary(lib);
 			return true;
 		}
 		catch (UnsatisfiedLinkError ule) {
 			return false;
 		}
 	}
 
	static native int nativeCalc(int dataSize, long calcMask, int height, int[] lengths, ByteBuffer[] columns, ByteBuffer matrix, int[] recoveryMap, ByteBuffer gflog, ByteBuffer gfinvlog, int gfbits, long gfprimitive);
 	
 	public int recover(ByteBuffer[] columns, long calcMask, int height) {
 		int[] lengths = getLengths(columns);
		return nativeCalc(dataSize, calcMask, height, lengths, columns, recoveryMatrixBuffer, recoveryMap, gflogBuffer, gfinvlogBuffer, gfbits, gfprimitive);
 	}
 	
 	private final static ThreadLocal<ByteBuffer[]> _columns = new ThreadLocal<ByteBuffer[]>();
 	
 	public int checksum(ByteBuffer[] columns, long calcMask, int height) {
 		int[] lengths = getLengths(columns);
		return nativeCalc(dataSize, calcMask, height, lengths, columns, codingMatrixBuffer, null, gflogBuffer, gfinvlogBuffer, gfbits, gfprimitive);
 	}
 	
 	private final static ThreadLocal<int[]> _lengths = new ThreadLocal<int[]>();
 	
 	private int[] getLengths(ByteBuffer[] columns) {
 		int[] lengths = _lengths.get();
 		if (lengths == null || lengths.length < columns.length)
 			_lengths.set(lengths = new int[columns.length]);
 		for (int i = 0; i < columns.length; i++)
 			lengths[i] = columns[i].limit();
 		return lengths;
 	}
 
 }
