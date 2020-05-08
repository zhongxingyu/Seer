 package org.araqne.logdb;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.nio.channels.FileChannel;
 
 import org.araqne.logdb.query.command.IoHelper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class LocalFileMover implements FileMover {
 	private final Logger logger = LoggerFactory.getLogger(LocalFileMover.class);
 
 	@Override
 	public void move(String from, String to) throws IOException {
 		File fromFile = new File(from);
 		File toFile = new File(to);
 
 		if (!fromFile.exists())
 			throw new IOException("file not found: " + from);
 
 		if (toFile.exists())
 			throw new IOException("file already exist: " + to);
 
 		boolean moved = fromFile.renameTo(toFile);
 		boolean copied = false;
 		if (moved) {
 			logger.debug("araqne logdb: moved file from [{}] to [{}]", fromFile.getAbsolutePath(), toFile.getAbsolutePath());
 		} else {
 			FileInputStream fis = null;
 			FileOutputStream fos = null;
 			FileChannel fromChannel = null;
 			FileChannel toChannel = null;
 			try {
 				toFile.getParentFile().mkdirs();
 
 				fis = new FileInputStream(fromFile);
 				fos = new FileOutputStream(toFile);
 
 				fromChannel = fis.getChannel();
 				toChannel = fos.getChannel();
 
				fromChannel.transferTo(0, fromChannel.size(), toChannel);
 				copied = true;
 			} finally {
 				IoHelper.close(fromChannel);
 				IoHelper.close(toChannel);
 				IoHelper.close(fis);
 				IoHelper.close(fos);
 
 				if (copied) {
 					boolean deleted = fromFile.delete();
 					if (!deleted) {
 						logger.debug("araqne logdb: delete [{}] failed after copy", fromFile.getAbsolutePath());
 					}
 				}
 			}
 		}
 	}
 }
