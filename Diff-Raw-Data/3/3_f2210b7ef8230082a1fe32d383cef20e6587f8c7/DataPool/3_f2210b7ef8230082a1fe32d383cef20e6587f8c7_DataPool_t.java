 package net.kevxu.senselib;
 
 import java.util.Random;
 
 public class DataPool {
 
 	private int mPoolSize;
 
 	private float[][] mPool;
 	private int mStartPos;
 	private int mEndPos;
 	private int mSize;
 
 	public DataPool(int poolSize) {
 		mPoolSize = poolSize;
 		mPool = new float[mPoolSize][];
 		mStartPos = 0;
 		mEndPos = 0;
 		mSize = 0;
 	}
 
 	public int size() {
 		return mSize;
 	}
 
 	public int getPoolSize() {
 		return mPoolSize;
 	}
 
 	public void append(float[] values) {
 		if (mSize < mPoolSize) {
 			mPool[mEndPos] = values;
 			mEndPos = (mEndPos + 1) % mPoolSize;
 			mSize++;
 		} else {
 			mStartPos = (mStartPos + 1) % mPoolSize;
 			mPool[mEndPos] = values;
 			mEndPos = (mEndPos + 1) % mPoolSize;
 		}
 	}
 
 	public float[] get(int i) {
 		if (i < mSize) {
 			return mPool[(mStartPos + i) % mPoolSize];
 		} else {
 			throw new IndexOutOfBoundsException("i is larger than DataPool size.");
 		}
 	}
 
 	public float[][] getPrevious(int n) {
 		if (n > mSize) {
 			throw new IndexOutOfBoundsException("n is larger than DataPool size.");
 		}
 
 		float[][] pd = new float[n][];
 		for (int i = 0; i < n; i++) {
 			pd[i] = get(mSize - 1 - i);
 		}
 		return pd;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder str = new StringBuilder();
 
 		str.append("[");
 		for (int i = 0; i < mSize; i++) {
 			float[] values = get(i);
 			str.append("[");
 			for (float value : values) {
 				str.append(value);
 				str.append(", ");
 			}
 			str.delete(str.length() - 2, str.length());
 			str.append("], ");
 		}
 		str.delete(str.length() - 2, str.length());
 		str.append("]");
 
 		return str.toString();
 	}
 
 	public static void main(String[] args) {
 		Random r = new Random();
 		DataPool pool = new DataPool(5);
 
 		// Appending data
 		System.out.println("Appending data:");
 		for (int i = 0; i < 10; i++) {
 			float[] values = new float[3];
 			for (int j = 0; j < 3; j++) {
 				values[j] = (float) r.nextInt(10);
 			}
 			pool.append(values);
 			System.out.println(pool);
 		}
 		System.out.println();
 
 		// Test get
 		System.out.println("Testing get:");
 		for (int i = 0; i < 20; i++) {
 			try {
 				float[] value = pool.get(i);
 				StringBuilder sb = new StringBuilder();
 				sb.append("[");
 				for (int j = 0; j < 3; j++) {
 					sb.append(value[j]).append(", ");
 				}
 				sb.delete(sb.length() - 2, sb.length());
 				System.out.println(i + ": " + sb.toString());
 			} catch (IndexOutOfBoundsException e) {
 				e.printStackTrace();
 			}
 		}
 		System.out.println();
 
 		// Test getPrevious
 		System.out.println("Testing getPrevious:");
 		for (int n = 0; n < 10; n++) {
 			try {
 				float[][] pd = pool.getPrevious(n);
 				StringBuilder sb = new StringBuilder();
 				for (int i = 0; i < n; i++) {
 					sb.append("[");
 					for (int j = 0; j < 3; j++) {
 						sb.append(pd[i][j]).append(", ");
 					}
 					sb.delete(sb.length() - 2, sb.length());
 					sb.append("], ");
 				}
				if (n > 0)
					sb.delete(sb.length() - 2, sb.length());
 				System.out.println("previous " + n + ": " + sb.toString());
 			} catch (IndexOutOfBoundsException e) {
 				e.printStackTrace();
 			}
 		}
 		System.out.println();
 	}
 }
