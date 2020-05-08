 package org.tcrun.slickij.data.dao;
 
 import com.google.code.morphia.Datastore;
 import com.google.code.morphia.dao.BasicDAO;
 import com.google.inject.Inject;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Date;
 import java.util.List;
 import org.bson.types.ObjectId;
 import org.tcrun.slickij.api.data.FileChunk;
 import org.tcrun.slickij.api.data.StoredFile;
 import org.tcrun.slickij.api.data.dao.FileChunkDAO;
 import org.tcrun.slickij.api.data.dao.StoredFileDAO;
 
 /**
  *
  * @author jcorbett
  */
 public class StoredFileDAOImpl extends BasicDAO<StoredFile, ObjectId> implements StoredFileDAO
 {
 	public static int DEFAULT_CHUNK_SIZE=262144;
 	private FileChunkDAO filechunkDAO;
 
 	@Inject
 	public StoredFileDAOImpl(Datastore ds, FileChunkDAO chunkDAO)
 	{
 		super(StoredFile.class, ds);
 		filechunkDAO = chunkDAO;
 	}
 
 	@Override
 	public byte[] getFileContent(ObjectId id)
 	{
 		StoredFile file = get(id);
 		if(file == null)
 			return null;
 		byte[] retval = new byte[file.getLength()];
 		List<FileChunk> chunks = filechunkDAO.getChunksForFile(id);
 		for(FileChunk chunk : chunks)
 		{
 			// we have to do the chunk's data size because it could be a partial chunk.
 			System.arraycopy(chunk.getData(), 0, retval, file.getChunkSize() * chunk.getChunkNumber(), chunk.getData().length);
 		}
 		return retval;
 	}
 
 	@Override
 	public StoredFile saveFileContent(StoredFile file, byte[] data)
 	{
 		if(file.getChunkSize() == null)
 			file.setChunkSize(DEFAULT_CHUNK_SIZE);
 		file.setLength(data.length);
 		file.setUploadDate(new Date());
 		try
 		{
 			file.setMd5(toHexString(MessageDigest.getInstance("md5").digest(data)));
 		} catch(NoSuchAlgorithmException ex)
 		{
 			// no worries
 		}
 		save(file);
 
 		// determine the number of chunks
 		int numberOfChunks = data.length / file.getChunkSize();
 		if((data.length % file.getChunkSize()) != 0)
 			numberOfChunks++;
 
 		// save each chunk
 		for(int i = 0; i < numberOfChunks; i++)
 		{
 			FileChunk chunk = new FileChunk();
 			chunk.setChunkNumber(i);
 			chunk.setFilesId(file.getObjectId());
 
 			// the size of the chunk is whatever is smaller, default chunk size, or the amount of bytes after
 			// the starting point of this chunk
 			int length = data.length - (file.getChunkSize() * i);
 			if(length > file.getChunkSize())
 				length = file.getChunkSize();
 			byte[] chunkData = new byte[length];
 
 			// copy the data into the chunk, then save the chunk
 			System.arraycopy(data, (file.getChunkSize() * i), chunkData, 0, length);
 			chunk.setData(chunkData);
 			filechunkDAO.save(chunk);
 		}
 
 		return file;
 	}
 
     @Override
     public StoredFile addChunk(StoredFile file, byte[] data)
     {
         FileChunk chunk = new FileChunk();
        int numberOfChunks = file.getLength() / file.getChunkSize();
        if((data.length % file.getChunkSize()) != 0)
            numberOfChunks++;
        chunk.setChunkNumber(numberOfChunks);
         file.setLength(file.getLength() + data.length);
         chunk.setFilesId(file.getObjectId());
         chunk.setData(data);
         filechunkDAO.save(chunk);
 
         save(file);
         return file;
     }
 
     public static String toHexString(byte[] digest)
 	{
 		StringBuilder sb = new StringBuilder();
 		for (byte b : digest)
 		{
 			sb.append(String.format("%1$02X", b));
 		}
 
 		return sb.toString();
 	}
 }
