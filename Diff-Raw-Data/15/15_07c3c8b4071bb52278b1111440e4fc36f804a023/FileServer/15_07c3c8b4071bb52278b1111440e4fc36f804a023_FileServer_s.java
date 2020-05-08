 package org.remus.fs;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.nio.ByteBuffer;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.thrift.TException;
 
 import org.remus.PeerInfo;
 import org.remus.RemusAttach;
 import org.remus.plugin.PluginManager;
 import org.remus.thrift.AppletRef;
 import org.remus.thrift.PeerType;
 
 public class FileServer extends RemusAttach {
 
 	/**
 	 * Location in the file system the attachments are stored
 	 */
 	private File basePath;
 	/**
 	 * Is the directory shared?
 	 */
 	private Boolean dirShared;
 	public final static String DIR_NAME    = "dir";
 	public final static String DIR_SHARED  = "shared";
 
 	@Override
 	public void init(Map params) {
 		this.basePath = new File((String) params.get(DIR_NAME));
 		this.dirShared = Boolean.valueOf(params.get(DIR_SHARED).toString());
 	}
 
 	@Override
 	public void deleteAttachment(AppletRef stack, String key, String name)
 			throws TException {
 		File attachFile = NameFlatten.flatten(basePath, stack.pipeline, stack.instance, stack.applet, key, name);
 		attachFile.delete();
 	}
 
 	public static boolean deleteDir(File dir) {
 		if (dir.isDirectory()) {
 			for (File child : dir.listFiles()) {
 				boolean success = deleteDir(child);
 				if (!success) {
 					return false;
 				}
 			}
 		}
 		return dir.delete();
 	}
 
 	@Override
 	public void deleteStack(AppletRef stack) throws TException {
 		File attachFile = NameFlatten.flatten(basePath, stack.pipeline, stack.instance, stack.applet, null, null);
 		File stackDir = attachFile.getParentFile().getParentFile();
 		deleteDir(stackDir);
 	}
 	
 	@Override
 	public boolean hasAttachment(AppletRef stack, String key, String name)
 			throws TException {
 		File attachFile = NameFlatten.flatten(basePath, stack.pipeline, stack.instance, stack.applet, key, name);
 		if (attachFile.exists()) {
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public void initAttachment(AppletRef stack, String key, String name) throws TException {
 		File attachFile = NameFlatten.flatten(basePath, stack.pipeline, stack.instance, stack.applet, key, name);
         try {
         	if (!attachFile.getParentFile().exists()) {
         		attachFile.getParentFile().mkdirs();
         	}
 			RandomAccessFile f = new RandomAccessFile(attachFile, "rw");
 			f.setLength(0);
 			f.close();
         } catch (FileNotFoundException e) {
 			throw new TException(e);
 		} catch (IOException e) {
 			throw new TException(e);
 		}
 	}
 
 	@Override
 	public List<String> listAttachments(AppletRef stack, String key)
 			throws TException {
 		File attachDir = NameFlatten.flatten(basePath, stack.pipeline, stack.instance, stack.applet, key, null).getParentFile();
 		LinkedList<String> out = new LinkedList<String>();
 		if ( attachDir.exists() ) {
 			for ( File file : attachDir.listFiles() ) {
 				out.add( file.getName() );
 			}
 		}
 		return out;
 	}
 
 	@Override
 	public ByteBuffer readBlock(AppletRef stack, String key, String name,
 			long offset, int length) throws TException {
 		File attachFile = NameFlatten.flatten(basePath, stack.pipeline, stack.instance, stack.applet, key, name);
         try {
 			RandomAccessFile f = new RandomAccessFile(attachFile, "rw");
 			f.seek(offset);
 			ByteBuffer buffer = ByteBuffer.allocate(length);
 			f.read( buffer.array(), 0, length );			
 			f.close();
 			return buffer;
         } catch (FileNotFoundException e) {
 			throw new TException(e);
 		} catch (IOException e) {
 			throw new TException(e);
 		}
 	}
 
 	@Override
 	public void appendBlock(AppletRef stack, String key, String name, ByteBuffer data) throws TException {
 		File attachFile = NameFlatten.flatten(basePath, stack.pipeline, stack.instance, stack.applet, key, name);
         try {
         	FileOutputStream f = new FileOutputStream(attachFile, true);
 			f.write(data.array());
 			f.close();
         } catch (FileNotFoundException e) {
 			throw new TException(e);
 		} catch (IOException e) {
 			throw new TException(e);
 		}		
 	}
 
 	@Override
 	public long getAttachmentSize(AppletRef stack, String key, String name)
 			throws TException {
 		File attachFile = NameFlatten.flatten(basePath, stack.pipeline, stack.instance, stack.applet, key, name);
 		return attachFile.length();		
 	}
 
 	@Override
 	public PeerInfo getPeerInfo() {
 		PeerInfo out = new PeerInfo();
 		out.peerType = PeerType.ATTACH_SERVER;
 		out.name = "Remus FileSystem";
 		return out;
 	}
 
 	@Override
 	public void start(PluginManager pluginManager) throws Exception {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void stop() {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 }
