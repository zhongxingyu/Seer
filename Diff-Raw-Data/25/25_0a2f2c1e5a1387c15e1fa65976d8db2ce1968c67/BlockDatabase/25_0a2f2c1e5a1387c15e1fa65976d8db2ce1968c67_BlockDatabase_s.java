 package edu.berkeley.gamesman.database;
 
 import java.io.IOException;
 import java.math.BigInteger;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel.MapMode;
 
 import edu.berkeley.gamesman.core.Record;
 import edu.berkeley.gamesman.util.DebugFacility;
 import edu.berkeley.gamesman.util.Util;
 
 /**
  * The BlockDatabase is a slightly more packed version of the FileDatabase.
  * It also operates on local files, and utilizes the Byte-oriented interface of
  * the Record instead of stream.
  * @author Steven Schlansker
  */
 public class BlockDatabase extends FileDatabase {
 
 	private final int headerSize = 8;
 
 	MappedByteBuffer buf;
 
 	long lastRecord;
 
 	@Override
 	public void close() {
 		flush();
 		try {
 			fd.seek(offset);
 			fd.writeLong(lastRecord);
 			long dblen = offset + headerSize + (Record.bitlength(conf)*lastRecord+7)/8;
 			Util.debug(DebugFacility.Database,"Attempting to truncate to "+dblen);
 			fd.getChannel().truncate(dblen);
 		} catch (IOException e) {
 			Util.warn("Could not cleanly close BlockDB: " + e);
 		}
 		buf = null;
 		super.close();
 	}
 
 	@Override
 	public void flush() {
 		try {
 			fd.seek(offset);
 			fd.writeLong(lastRecord);
 		} catch (IOException e) {
 			Util.warn("Could not flush BlockDB: " + e);
 		}
 		buf.force();
 		super.flush();
 	}
 
 	@Override
 	public Record getRecord(BigInteger loc) {
 		lastRecord = Math.max(lastRecord,loc.longValue());
 		return Record.read(conf, buf, loc.longValue());
 	}
 
 	@Override
 	public void initialize(String loc) {
 		super.initialize(loc);
 		try {
 			buf = fd.getChannel().map(MapMode.READ_WRITE, offset + headerSize, Integer.MAX_VALUE/4);
 			fd.seek(offset);
 			lastRecord = fd.readLong();
 		} catch (IOException e) {
 			Util.fatalError("Could not map ByteBuffer from blockdb", e);
 		}
 	}
 
 	@Override
 	public void putRecord(BigInteger loc, Record value) {
 		lastRecord = Math.max(lastRecord,loc.longValue());
 		value.write(buf, loc.longValue());
 	}
 
 }
