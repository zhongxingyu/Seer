 package org.archboy.clobaframe.media.impl;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.Date;
 import java.util.List;
 import org.apache.commons.io.IOUtils;
 import org.archboy.clobaframe.media.Media;
 import org.archboy.clobaframe.media.MediaFactory;
 import org.archboy.clobaframe.media.MediaDataSizeLimitExceededException;
 import org.archboy.clobaframe.media.MediaLoader;
 import org.archboy.clobaframe.media.UnsupportedMediaException;
 import org.archboy.clobaframe.io.ResourceInfo;
 import org.archboy.clobaframe.io.file.FileBaseResourceInfo;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import javax.inject.Inject;
 import org.springframework.beans.factory.annotation.Value;
 import javax.inject.Named;
 import org.archboy.clobaframe.io.ResourceInfoFactory;
 import org.archboy.clobaframe.io.TemporaryResources;
 import org.archboy.clobaframe.io.file.FileBaseResourceInfoFactory;
 import org.springframework.util.Assert;
 
 /**
  *
  * @author yang
  */
 @Named
 public class MediaFactoryImpl implements MediaFactory{
 
 	// default 32 MB
 	private static final long DEFAULT_MAX_HANDLE_SIZE = 32L * 1024 * 1024;
 	private long maxHandleSize = DEFAULT_MAX_HANDLE_SIZE;
 
	private final Logger logger = LoggerFactory.getLogger(MediaFactoryImpl.class);
 
 	@Inject
 	private ResourceInfoFactory resourceInfoFactory;
 	
 	@Inject
 	private FileBaseResourceInfoFactory fileBaseResourceInfoFactory;
 	
 	@Inject
 	private List<MediaLoader> mediaLoaders;
 	
 	@Value("${media.maxHandleSize}")
 	public void setMaxHandleSizeKB(int maxHandleSizeKB) {
 		this.maxHandleSize = maxHandleSizeKB * 1024L;
 	}
 	
 	@Override
 	public Media make(byte[] data, String contentType, Date lastModified, TemporaryResources temporaryResources) throws IOException {
 		if (lastModified == null) {
 			lastModified = new Date();
 		}
 		
 		ResourceInfo resourceInfo = resourceInfoFactory.make(data, contentType, lastModified);
 		return make(resourceInfo, temporaryResources);
 	}
 
 	@Override
 	public Media make(InputStream inputStream, String contentType, Date lastModified, TemporaryResources temporaryResources) throws IOException {
 		if (lastModified == null) {
 			lastModified = new Date();
 		}
 		
 		try{
 			byte[] data = toByteArrayWithSizeLimit(inputStream);
 			ResourceInfo resourceInfo = resourceInfoFactory.make(data, contentType, lastModified);
 			return make(resourceInfo, temporaryResources);
 		} finally {
 			IOUtils.closeQuietly(inputStream);
 		}
 	}
 
 	@Override
 	public Media make(File file, TemporaryResources temporaryResources) throws IOException {
 		ResourceInfo resourceInfo = fileBaseResourceInfoFactory.make(file);
 		return make(resourceInfo, temporaryResources);
 	}
 
 	@Override
 	public Media make(URL url, TemporaryResources temporaryResources) throws IOException {
 		Assert.isTrue("http".equals(url.getProtocol()) || "https".equals(url.getProtocol()));
 
 		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
 		
 		String contentType = connection.getContentType();
 		Date lastModified = null;
 		
 		long lastModifiedLong = connection.getLastModified();
 		if (lastModifiedLong == 0){
 			lastModified = new Date();
 		}else{
 			lastModified = new Date(lastModifiedLong);
 		}
 		
 		InputStream in = null;
 		try {
 			in = connection.getInputStream();
 			byte[] data = toByteArrayWithSizeLimit(in);
 			ResourceInfo resourceInfo = resourceInfoFactory.make(data, contentType, lastModified);
 			return make(resourceInfo, temporaryResources);
 			
 		} finally {
 			IOUtils.closeQuietly(in);
 			connection.disconnect();
 		}
 	}
 
 	@Override
 	public Media make(ResourceInfo resourceInfo, TemporaryResources temporaryResources) throws IOException {
 		Assert.isTrue(resourceInfo.getContentLength() > 0);
 		
 		if (resourceInfo.getContentLength() > maxHandleSize){
 			throw new MediaDataSizeLimitExceededException();
 		}
 		
 		//TemporaryResources temporaryResources = new TemporaryResources();
 		FileBaseResourceInfo fileBaseResourceInfo = fileBaseResourceInfoFactory.wrap(
 				resourceInfo, temporaryResources);
 		
 		//try{
 		Media media = null;
 
 		for (MediaLoader mediaLoader : mediaLoaders){
 			if (mediaLoader.support(resourceInfo.getContentType())){
 				media = mediaLoader.load(fileBaseResourceInfo);
 				break;
 			}
 		}
 
 		if (media == null){
 			throw new UnsupportedMediaException("Content type [" + resourceInfo.getContentType() + "] unsupported.");
 		}
 
 //		if (media instanceof AbstractMedia){
 //			for(MetaDataParser metaDataParser : metaDataParsers){
 //				if (metaDataParser.support(media.getContentType())){
 //					MetaData metaData = metaDataParser.parse(fileBaseResourceInfo);
 //					((AbstractMedia)media).setMetaData(metaData);
 //					break;
 //				}
 //			}
 //		}
 
 		return media;
 			
 //		}finally{
 //			// close the temp file.
 //			temporaryResources.close();
 //		}
 	}
 
 //	/*
 //	 * java 6 ImageInputStream does not implement Closable interface
 //	 */
 //	private void closeQuietly(ImageInputStream stream) {
 //		if (stream != null) {
 //			try {
 //				stream.close();
 //			} catch (IOException e) {
 //				// ignore
 //			}
 //		}
 //	}
 //
 //	private void closeQuietly(ImageReader reader) {
 //		if (reader != null) {
 //			reader.dispose();
 //		}
 //	}
 	
 	private byte[] toByteArrayWithSizeLimit(InputStream inputStream) throws IOException {
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 
 		int totalReadBytes = 0;
 		byte[] buffer = new byte[16 * 1024];
 
 		while (true) {
 			int readBytes = inputStream.read(buffer);
 			if (readBytes < 0) {
 				break;
 			}
 			totalReadBytes += readBytes;
 			if (totalReadBytes > maxHandleSize) {
 				throw new MediaDataSizeLimitExceededException();
 			}
 			out.write(buffer, 0, readBytes);
 		}
 
 		byte[] data = out.toByteArray();
 		out.close();
 		return data;
 	}
 }
