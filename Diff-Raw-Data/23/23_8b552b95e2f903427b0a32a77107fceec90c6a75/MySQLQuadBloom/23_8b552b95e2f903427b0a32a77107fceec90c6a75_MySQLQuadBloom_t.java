 package org.xenei.compressedgraph.msqbloom;
 
 import java.nio.ByteBuffer;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xenei.compressedgraph.bloom.BloomCapabilities;
 import org.xenei.compressedgraph.bloom.BloomGraph;
 import org.xenei.compressedgraph.bloom.BloomIndex;
 
 import com.hp.hpl.jena.util.iterator.ClosableIterator;
 import com.hp.hpl.jena.util.iterator.ExtendedIterator;
 import com.hp.hpl.jena.util.iterator.NiceIterator;
 import com.hp.hpl.jena.util.iterator.WrappedIterator;
 
 import java.sql.Blob;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 import javax.sql.rowset.serial.SerialBlob;
 import javax.sql.rowset.serial.SerialException;
 
 /**
  * Table create table bloom ( bloomId int unsigned not null, data blob , index
  * blm_idx ( bloomId ) );
  * 
  */
 public class MySQLQuadBloom implements BloomCapabilities {
 	private static final Logger LOG = LoggerFactory.getLogger(MySQLQuadBloom.class);
 
 	private Connection conn = null;
 	private PreparedStatement  insert;
 	private PreparedStatement delete;
 	private PreparedStatement findExact;
 	private PreparedStatement findBloom;
 	private PreparedStatement count;
 	private PreparedStatement maxValue;
 	
 
 	// "jdbc:mysql://localhost/test?user=%s&password=%s"
 	public MySQLQuadBloom(String url, String table) throws InstantiationException,
 			IllegalAccessException, ClassNotFoundException, SQLException {
 		Class.forName("com.mysql.jdbc.Driver").newInstance();
 		conn = DriverManager.getConnection(url);
 		insert = conn.prepareStatement(String.format("INSERT INTO %s SET id1=?, id2=?, id3=?, id4=?, data=?", table));
 		delete = conn.prepareStatement(String.format("DELETE FROM %s WHERE id1=? AND id2=? AND id3=? AND id4=? AND data=?", table));
 		findExact = conn.prepareStatement(String.format("SELECT data FROM %s " +
 				"WHERE idx1=? " +
 				"AND idx2=? " +
 				"AND idx3=? " +
 				"AND idx4=? "
 				, table));
 		findBloom = conn.prepareStatement(String.format("SELECT data FROM %1$s, bloomByteMap idx1, bloomByteMap idx2, bloomByteMap idx3, bloomByteMap idx4 " +
				"WHERE %1$s.id1=idx1.value AND idx1.id=? " +
				"AND %1$s.id2=idx2.value AND idx2.id=? " +
				"AND %1$s.id3=idx3.value AND idx3.id=? " +
				"AND %1$s.id4=idx4.value AND idx4.id=? "
 				, table));
 		count = conn.prepareStatement(String.format("SELECT count(*) FROM %s", table));
 		maxValue = conn.prepareStatement(String.format("SELECT max(id1), max(id2), max(id3), max(id4) from %s",table));		
 	}
 	
 	public void populateByteMap() throws SQLException
 	{
		PreparedStatement pstmt = conn.prepareStatement( "INSERT INTO bloomByteMap SET id=?, value=?" );
 		for (int i =0;i<0xFF;i++)
 		{
 			Iterator<ByteBuffer> iter = BloomIndex.getIndexIterator( (byte)i );
 			while (iter.hasNext())
 			{
				int j = (int)(0xFF & iter.next().get(0)); 
 				pstmt.setByte(1, (byte) i );
				pstmt.setByte(2, (byte) j );
				System.out.println( pstmt.toString() );
 				pstmt.execute();
 			}
 		}
 	}
 
 	@Override
 	public boolean supportsBloomQuery() {
		return true;
 	}
 
 	@Override
 	public boolean supportsExact() {
 		return true;
 	}
 
 	@Override
 	public boolean canBeEmpty() {
 		return true;
 	}
 
 	@Override
 	public boolean sizeAccurate() {
 		return true;
 	}
 
 	@Override
 	public int getSize() {
 
 		try {
 			ResultSet rs = count.executeQuery();
 			long count = rs.getLong(1);
 			if (count > Integer.MAX_VALUE) {
 				return Integer.MAX_VALUE;
 			}
 			return (int) count;
 		} catch (SQLException e) {
 			return Integer.MAX_VALUE / 2;
 		}
 	}
 
 	@Override
 	public boolean addAllowed() {
 		return true;
 	}
 
 	@Override
 	public boolean addsDuplicates() {
 		return true;
 	}
 
 	private void closeQuietly(ResultSet rs) {
 		if (rs != null) {
 			try {
 				rs.close();
 			} catch (SQLException e) {
 				LOG.warn(e.getMessage());
 			}
 		}
 	}
 
 	@Override
 	public void write(ByteBuffer bloomValue, ByteBuffer data) {
 		try {
 			Blob b = new SerialBlob(data.array());
 
 			insert.setByte(1, bloomValue.get(0));
 			insert.setByte(2, bloomValue.get(1));
 			insert.setByte(3, bloomValue.get(2));
 			insert.setByte(4, bloomValue.get(3));
			insert.setBlob(5, b);
 			insert.executeUpdate();
 		} catch (SerialException e) {
 			LOG.error(e.getMessage(), e);
 		} catch (SQLException e) {
 			LOG.error(e.getMessage(), e);
 		}
 	}
 
 	@Override
 	public ByteBuffer getMaxBloomValue() {
 		ResultSet rs = null;
 		try {
 			rs = maxValue.executeQuery();
 			rs.next();
 			long l = rs.getLong(1);
 			return BloomGraph.encodeBloomValue(l);
 		} catch (SQLException e) {
 			LOG.error(e.getMessage(), e);
 			byte[] val = new byte[4];
 			Arrays.fill(val, (byte) 0xFF);
 			return ByteBuffer.wrap(val);
 		} finally {
 			closeQuietly(rs);
 		}
 	}
 
 	@Override
 	public boolean deleteAllowed() {
 		return true;
 	}
 
 	@Override
 	public void delete(ByteBuffer bloomValue, ByteBuffer data) {
 		try {		
 			Blob b = new SerialBlob(data.array());
 			delete.setByte( 1,  bloomValue.get(0));
 			delete.setByte( 2,  bloomValue.get(1));
 			delete.setByte( 3,  bloomValue.get(2));
 			delete.setByte( 4,  bloomValue.get(3));
 			delete.setBlob(5, b);
 			delete.executeUpdate();
 		} catch (SerialException e) {
 			LOG.error(e.getMessage(), e);
 		} catch (SQLException e) {
 			LOG.error(e.getMessage(), e);
 		}
 	}
 	
 	@Override
 	public ExtendedIterator<ByteBuffer> find(ByteBuffer bloomValue,
 			boolean exact) {
 		ResultSet rs = null;
 		try {
 			if (exact) {
 				findExact.setByte( 1,  bloomValue.get(0));
 				findExact.setByte( 2,  bloomValue.get(1));
 				findExact.setByte( 3,  bloomValue.get(2));
 				findExact.setByte( 4,  bloomValue.get(3));
 				rs = findExact.executeQuery();
 				return WrappedIterator.create(new RSIterator(rs));
 			} else {
 				findBloom.setByte( 1,  bloomValue.get(0));
 				findBloom.setByte( 2,  bloomValue.get(1));
 				findBloom.setByte( 3,  bloomValue.get(2));
 				findBloom.setByte( 4,  bloomValue.get(3));
 				rs = findBloom.executeQuery();
 				return WrappedIterator.create(new RSIterator(rs));
 			}
 		} catch (SQLException e) {
 			LOG.error(e.getMessage(), e);
 			closeQuietly(rs);
 			return NiceIterator.emptyIterator();
 		}
 	}
 
 	@Override
 	public void close() {
 		try {
 			conn.close();
 		} catch (SQLException e) {
 			LOG.error(e.getMessage(), e);
 		}
 	}
 
 	private class RSIterator implements ClosableIterator<ByteBuffer> {
 		private ResultSet rs;
 		private boolean done;
 
 		RSIterator(ResultSet rs) throws SQLException {
 			this.rs = rs;
 			done = !rs.next();
 		}
 
 		@Override
 		public boolean hasNext() {
 			return !done;
 		}
 
 		@Override
 		public ByteBuffer next() {
 			if (hasNext()) {
 				try {
 					ByteBuffer retval = ByteBuffer.wrap(rs.getBytes(1));
 					try {
 						done = !rs.next();
 					} catch (SQLException e) {
 						LOG.error(e.getMessage(), e);
 					}
 					return retval;
 
 				} catch (SQLException e) {
 					LOG.error(e.getMessage(), e);
 					throw new RuntimeException(e);
 				}
 			} else {
 				throw new NoSuchElementException();
 			}
 
 		}
 
 		@Override
 		public void remove() {
 			throw new UnsupportedOperationException("remove not supported");
 		}
 
 		@Override
 		public void close() {
 			closeQuietly(rs);
 		}
 	}
 
 }
