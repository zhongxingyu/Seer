 package edu.berkeley.icsi.cdfs.namenode;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.hadoop.fs.Path;
 
 import com.esotericsoftware.kryo.Kryo;
 import com.esotericsoftware.kryo.KryoSerializable;
 import com.esotericsoftware.kryo.io.Input;
 import com.esotericsoftware.kryo.io.Output;
 
 final class BlockMetaData implements KryoSerializable {
 
	private static final int MAX_BLOCK_LOCATION = 100;
 
 	private int index;
 
 	private Path hdfsPath;
 
 	private int length;
 
 	private long offset;
 
 	private final Set<String> cachedCompressed = new HashSet<String>();
 
 	private final Set<String> cachedUncompressed = new HashSet<String>();
 
 	BlockMetaData(final int index, final Path hdfsPath, final int length, final long offset) {
 		this.index = index;
 		this.hdfsPath = hdfsPath;
 		this.length = length;
 		this.offset = offset;
 	}
 
 	@SuppressWarnings("unused")
 	private BlockMetaData() {
 		this.index = 0;
 		this.hdfsPath = null;
 		this.length = 0;
 		this.offset = 0L;
 	}
 
 	int getIndex() {
 		return this.index;
 	}
 
 	Path getHdfsPath() {
 		return this.hdfsPath;
 	}
 
 	int getLength() {
 		return this.length;
 	}
 
 	long getOffset() {
 		return this.offset;
 	}
 
 	String[] constructHostList(final String[] hdfsHosts) {
 
 		final List<String> hosts = new ArrayList<String>();
 
 		// Add hosts with uncompressed cached blocks
 		for (final Iterator<String> it = this.cachedUncompressed.iterator(); it.hasNext();) {
 			hosts.add(it.next());
 			if (hosts.size() == MAX_BLOCK_LOCATION) {
 				return hosts.toArray(new String[0]);
 			}
 		}
 
 		// Add additional hosts with compressed cached blocks
 		for (final Iterator<String> it = this.cachedCompressed.iterator(); it.hasNext();) {
 			final String candidate = it.next();
 			if (!this.cachedUncompressed.contains(candidate)) {
 				hosts.add(candidate);
 				if (hosts.size() == MAX_BLOCK_LOCATION) {
 					return hosts.toArray(new String[0]);
 				}
 			}
 		}
 
 		// Finally, add additional hosts with disk-local blocks
 		for (int i = 0; i < hdfsHosts.length; ++i) {
 			final String candidate = hdfsHosts[i];
 			if (!this.cachedUncompressed.contains(candidate) && !this.cachedCompressed.contains(candidate)) {
 				hosts.add(candidate);
 				if (hosts.size() == MAX_BLOCK_LOCATION) {
 					return hosts.toArray(new String[0]);
 				}
 			}
 		}
 
 		return hosts.toArray(new String[0]);
 	}
 
 	void addCachedBlock(final String host, final boolean compressed) {
 
 		if (compressed) {
 			this.cachedCompressed.add(host);
 		} else {
 			this.cachedUncompressed.add(host);
 		}
 	}
 
 	void removeCachedBlock(final String host, final boolean compressed) {
 
 		if (compressed) {
 			this.cachedCompressed.remove(host);
 		} else {
 			this.cachedUncompressed.remove(host);
 		}
 	}
 
 	boolean isCached(final boolean compressed) {
 
 		if (compressed) {
 			return !this.cachedCompressed.isEmpty();
 		} else {
 			return !this.cachedUncompressed.isEmpty();
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void write(final Kryo kryo, final Output output) {
 
 		output.writeInt(this.index);
 		output.writeString(this.hdfsPath.toString());
 		output.writeInt(this.length);
 		output.writeLong(this.offset);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void read(final Kryo kryo, final Input input) {
 
 		this.index = input.readInt();
 		this.hdfsPath = new Path(input.readString());
 		this.length = input.readInt();
 		this.offset = input.readLong();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String toString() {
 
 		return Integer.toString(this.index);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean equals(final Object obj) {
 
 		if (!(obj instanceof BlockMetaData)) {
 			return false;
 		}
 
 		final BlockMetaData bmd = (BlockMetaData) obj;
 
 		if (this.index != bmd.index) {
 			return false;
 		}
 
 		if (this.length != bmd.length) {
 			return false;
 		}
 
 		if (this.offset != bmd.offset) {
 			return false;
 		}
 
 		if (!this.hdfsPath.equals(bmd.hdfsPath)) {
 			throw new RuntimeException("Equals fails because of path: " + this.hdfsPath + " vs " + bmd.hdfsPath);
 		}
 
 		return true;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int hashCode() {
 
 		int hc = -(int) (this.offset % Integer.MAX_VALUE);
 		hc += (int) (this.length % Integer.MAX_VALUE);
 
 		return hc + (this.index * 31);
 	}
 }
