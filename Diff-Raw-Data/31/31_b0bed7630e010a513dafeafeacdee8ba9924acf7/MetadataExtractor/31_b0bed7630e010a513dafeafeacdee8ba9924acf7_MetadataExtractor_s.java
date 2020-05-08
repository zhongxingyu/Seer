 package org.mdissjava.mdisscore.metadata;
 
 import java.io.IOException;
 import java.util.Map;
 
 import org.mdissjava.mdisscore.model.pojo.Metadata;
 
 import com.drew.imaging.ImageProcessingException;
 import com.drew.metadata.MetadataException;
 
 public interface MetadataExtractor {
 
 	public Metadata obtenerMetadata(byte[] photo) throws MetadataException, ImageProcessingException, IOException;
 	public Metadata getEXIFMetadata(String format) throws MetadataException, ImageProcessingException, IOException;
 	public Metadata getBasicMetadata(String format) throws MetadataException, ImageProcessingException, IOException;
 	public String getOnlyExtension(String type);
 	public long bytesToMb(int bytes);
 	public long bytesToKb(int bytes);
 	public Map<String, String> getMetadataFormatted(Metadata metadata);

 }
