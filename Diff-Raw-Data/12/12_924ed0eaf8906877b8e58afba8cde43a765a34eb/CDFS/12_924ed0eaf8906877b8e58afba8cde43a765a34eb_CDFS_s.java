 package edu.berkeley.icsi.cdfs;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetSocketAddress;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.BlockLocation;
 import org.apache.hadoop.fs.FSDataInputStream;
 import org.apache.hadoop.fs.FSDataOutputStream;
 import org.apache.hadoop.fs.FileStatus;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.hdfs.server.namenode.NameNode;
 import org.apache.hadoop.ipc.RPC;
 import org.apache.hadoop.util.Progressable;
 
 import edu.berkeley.icsi.cdfs.datanode.ConnectionMode;
 import edu.berkeley.icsi.cdfs.datanode.Header;
 import edu.berkeley.icsi.cdfs.protocols.ClientNameNodeProtocol;
 import edu.berkeley.icsi.cdfs.utils.PathWrapper;
 
 public class CDFS extends FileSystem {
 
 	public static final int NAMENODE_RPC_PORT = 10000;
 
 	public static final int DATANODE_RPC_PORT = 10001;
 
 	public static final int DATANODE_DATA_PORT = 10002;
 
 	public static final int CLIENT_RPC_PORT = 10003;
 
 	public static final int BLOCK_SIZE = 128 * 1024 * 1024;
 
 	public static final int BLOCK_REPLICATION = 3;
 
 	private ClientNameNodeProtocol nameNode;
 
 	private Path workingDir;
 
 	private URI uri;
 
 	@Override
 	public FSDataOutputStream append(Path arg0, int arg1, Progressable arg2)
 			throws IOException {
 
 		System.out.println("append");
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public FSDataOutputStream create(final Path f, final FsPermission permission, final boolean overwrite,
 			final int bufferSize, final short replication, final long blockSize, final Progressable progress)
 			throws IOException {
 
 		// Check if the file already exists
 		if (!this.nameNode.create(new PathWrapper(f))) {
 			throw new IOException("File " + f + " does already exist");
 		}
 
 		final DatagramSocket socket = new DatagramSocket();
 
 		final byte[] buf = new byte[256];
 		final DatagramPacket dp = new DatagramPacket(buf, buf.length);
 		dp.setSocketAddress(new InetSocketAddress("localhost", DATANODE_DATA_PORT));
 		final Header header = new Header(ConnectionMode.WRITE, f, 0L);
 		header.toPacket(dp);
 
 		socket.send(dp);
 
 		return new CDFSDataOutputStream(socket);
 	}
 
 	@Override
 	public boolean delete(Path arg0) throws IOException {
 		// TODO Auto-generated method stub
 
 		System.out.println("delete");
 
 		return false;
 	}
 
 	@Override
 	public boolean delete(Path arg0, boolean arg1) throws IOException {
 		// TODO Auto-generated method stub
 
 		System.out.println("delete");
 
 		return false;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public FileStatus getFileStatus(final Path arg0) throws IOException {
 
 		final FileStatus fileStatus = this.nameNode.getFileStatus(new PathWrapper(arg0));
 		if (fileStatus == null) {
 			throw new FileNotFoundException("File " + arg0 + " could not be found");
 		}
 
 		return fileStatus;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public URI getUri() {
 
 		return this.uri;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void initialize(final URI uri, final Configuration conf)
 			throws IOException {
 		super.initialize(uri, conf);
 		setConf(conf);
 
 		String host = uri.getHost();
 		if (host == null) {
 			throw new IOException("Incomplete CDFS URI, no host: " + uri);
 		}
 
		final InetSocketAddress nnAddress = NameNode.getAddress(uri
			.getAuthority());
 		// this.dfs = new DFSClient(namenode, conf, statistics);
 		this.uri = URI.create(uri.getScheme() + "://" + uri.getAuthority());
 		this.workingDir = getHomeDirectory();
 
 		this.nameNode = (ClientNameNodeProtocol) RPC.getProxy(ClientNameNodeProtocol.class, 1, new InetSocketAddress(
 			"localhost", CDFS.NAMENODE_RPC_PORT), new Configuration());
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Path getWorkingDirectory() {
 
 		return this.workingDir;
 	}
 
 	@Override
 	public FileStatus[] listStatus(Path arg0) throws IOException {
 		// TODO Auto-generated method stub
 
 		System.out.println("listStatus");
 
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean mkdirs(final Path arg0, final FsPermission arg1) throws IOException {
 
 		return this.nameNode.mkdirs(new PathWrapper(arg0), arg1);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public FSDataInputStream open(final Path arg0, int arg1) throws IOException {
 
 		return new CDFSDataInputStream("localhost", DATANODE_DATA_PORT, arg0);
 	}
 
 	@Override
 	public boolean rename(Path arg0, Path arg1) throws IOException {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void setWorkingDirectory(Path arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public static Path toHDFSPath(final Path cdfsPath, final String suffix) {
 
 		final URI cdfsURI = cdfsPath.toUri();
 
 		URI uri;
 		try {
 			uri = new URI("hdfs", cdfsURI.getUserInfo(), cdfsURI.getHost(), 9000, cdfsURI.getPath() + suffix,
 				cdfsURI.getQuery(), cdfsURI.getFragment());
 		} catch (URISyntaxException e) {
 			throw new RuntimeException(e);
 		}
 
 		return new Path(uri);
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public BlockLocation[] getFileBlockLocations(final FileStatus file, final long start, final long len)
 			throws IOException {
 
 		if (file == null) {
 			return null;
 		}
 
 		if ((start < 0) || (len < 0)) {
 			throw new IllegalArgumentException("Invalid start or len parameter");
 		}
 
 		if (file.getLen() < start) {
 			return new BlockLocation[0];
 
 		}
 
 		return this.nameNode.getFileBlockLocations(new PathWrapper(file.getPath()), start, len);
 	}
 }
