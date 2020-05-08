 package me.kevinwells.darxen.shp;
 
 import java.io.InputStream;
 import java.util.Iterator;
 
 import me.kevinwells.darxen.shp.DbfFile.DbfRecord;
 
 public class DbfFile implements Iterable<DbfRecord> {
 	
 	private int mInputId;
 	private long hDbf;
 	
 	private int mFieldCount;
 	private int mRecordCount;
 	
 	public DbfFile(InputStream fDbf) {
 		init(fDbf);
 	}
 
 	private native void init(InputStream fDbf);
	public native void close();
 	
 	public native boolean isNull(int record, int field);
 	public native int getInt(int record, int field);
 	public native double getDouble(int record, int field);
 	public native String getString(int record, int field);	
 	
 	public int getFieldCount() {
 		return mFieldCount;
 	}
 	
 	public int getRecordCount() {
 		return mRecordCount;
 	}
 	
 	@Override
 	public Iterator<DbfRecord> iterator() {
 		return new DbfFileIterator();
 	}
 	
 	public class DbfFileIterator implements Iterator<DbfRecord> {
 		private int mIndex = 0;
 		@Override
 		public boolean hasNext() {
 			return mIndex < mRecordCount;
 		}
 		@Override
 		public DbfRecord next() {
 			return new DbfRecord(mIndex++);
 		}
 		@Override
 		public void remove() {
 			throw new UnsupportedOperationException();
 		}
 	}
 	
 	public class DbfRecord {
 		private int mRow;
 		public DbfRecord(int row) {
 			mRow = row;
 		}
 		public boolean isNull(int field) {
 			return DbfFile.this.isNull(mRow, field);
 		}
 		public int getInt(int field) {
 			return DbfFile.this.getInt(mRow, field);
 		}
 		public double getDouble(int field) {
 			return DbfFile.this.getDouble(mRow, field);
 		}
 		public String getString(int field) {
 			return DbfFile.this.getString(mRow, field);
 		}
 		
 	}
 
 	static {
 		System.loadLibrary("shp");
 	}
 	
 }
