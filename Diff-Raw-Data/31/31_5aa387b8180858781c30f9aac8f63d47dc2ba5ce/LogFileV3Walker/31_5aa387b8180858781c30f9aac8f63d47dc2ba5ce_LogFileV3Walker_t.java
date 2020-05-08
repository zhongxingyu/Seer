 package org.araqne.logstorage.file;
 
 import java.io.IOException;
 
 import org.araqne.logstorage.file.LogFileV3Reader.LogBlockV3;
 import org.araqne.storage.api.FilePath;
 import org.araqne.storage.localfile.LocalFilePath;
 
 public class LogFileV3Walker {
 	public static void main(String[] args) throws IOException {
 		FilePath indexPath = new LocalFilePath(args[0]);
 		FilePath dataPath = indexPath.getAbsoluteFilePath().getParentFilePath().newFilePath(indexPath.getName().replace(".idx", ".dat"));
 		LogFileV3Reader reader = new LogFileV3Reader(indexPath, dataPath, null, null);
 		
 		System.out.printf("%s blocks\n", reader.getBlockCount());
 		while (reader.hasNextBlock()) {
 			LogBlockV3 block = reader.nextBlock();
			System.out.printf("%s [%s ~ %s]\n", block.getIndexBlock().getId(), block.getMinId(), block.getMaxId());
 		}
 		
 		reader.close();
 	}
 
 }
