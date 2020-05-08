 package com.euroit.militaryshop.adapter.impl;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.Writer;
 import java.nio.ByteBuffer;
 import java.nio.channels.Channels;
 import java.nio.channels.ReadableByteChannel;
 import java.nio.channels.WritableByteChannel;
 import java.util.List;
 
 import javax.xml.bind.DatatypeConverter;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.springframework.util.StopWatch;
 import org.springframework.util.StringUtils;
 
 import com.euroit.militaryshop.adapter.MediaAdapter;
 import com.euroit.militaryshop.dto.UploadedMediaDto;
 import com.euroit.militaryshop.service.UploadedMediaService;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.google.appengine.api.blobstore.BlobKey;
 import com.google.appengine.api.blobstore.BlobstoreService;
 import com.google.appengine.api.files.AppEngineFile;
 import com.google.appengine.api.files.FileReadChannel;
 import com.google.appengine.api.files.FileService;
 import com.google.appengine.api.files.FileWriteChannel;
 
 @Component
 public class MediaAdapterImpl implements MediaAdapter {
 
 	private static final Logger LOG = LoggerFactory
 			.getLogger(MediaAdapterImpl.class);
 
 	UploadedMediaService uploadedMediaService;
 
 	BlobstoreService blobstoreService;
 
 	FileService gaeFileService;
 
 	@Autowired
 	public void setUploadedMediaService(
 			UploadedMediaService uploadedMediaService) {
 		this.uploadedMediaService = uploadedMediaService;
 	}
 
 	@Autowired
 	public void setBlobstoreService(BlobstoreService blobstoreService) {
 		this.blobstoreService = blobstoreService;
 	}
 
 	@Autowired
 	public void setGaeFileService(FileService fileService) {
 		this.gaeFileService = fileService;
 	}
 
 	@Override
 	public void updateMedia(BlobKey newBlobKey, String mediaName) {
 
 		if (newBlobKey == null || !StringUtils.hasText(mediaName))
 			return;
 
 		List<UploadedMediaDto> oldUploadedMedia = uploadedMediaService
 				.getMediaByName(mediaName);
 
 		for (UploadedMediaDto umDto : oldUploadedMedia) {
 			blobstoreService.delete(new BlobKey(umDto.getBlobstoreKey()));
 		}
 
 		uploadedMediaService.removeOldMedia(mediaName);
 		uploadedMediaService.saveMedia(mediaName, newBlobKey.getKeyString());
 	}
 
 	public String getMediaBase64Encoded(String mediaName) throws IOException {
 		StopWatch stopWatch = new StopWatch();
 
 		final String blobstoreKey = uploadedMediaService
 				.getLatestBlobstoreKey(mediaName);
 		if (blobstoreKey == null) {
 			return null;
 		}
 
 		BlobKey blobKey = new BlobKey(blobstoreKey);
 		LOG.debug("Blob key:{}", blobKey);
 		AppEngineFile appEngineFile = gaeFileService.getBlobFile(blobKey);
 
 		final String printBase64Binary;
 		ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
 
 		try (FileReadChannel readChannel = gaeFileService.openReadChannel(appEngineFile, false);
 				final WritableByteChannel outputChannel = Channels
 						.newChannel(baos);) {
 			stopWatch.start();
 
 			fastChannelCopy(readChannel, outputChannel);
 
 			byte[] mediaByteArray = baos.toByteArray();
 			printBase64Binary = DatatypeConverter
 					.printBase64Binary(mediaByteArray);
 			stopWatch.stop();
 			LOG.debug("{}B exported in {}s", mediaByteArray.length,
 					stopWatch.getTotalTimeSeconds());
 		}
 
 		return printBase64Binary;
 	}
 
 	/**
 	 * Awesome http://thomaswabner.wordpress.com/2007/10/09/fast-stream-copy-using-javanio-channels/
 	 * @param src
 	 * @param dest
 	 * @throws IOException
 	 */
 	public void fastChannelCopy(final ReadableByteChannel src,
 			final WritableByteChannel dest) throws IOException {
 		final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
 		while (src.read(buffer) != -1) {
 			// prepare the buffer to be drained
 			buffer.flip();
 			// write to the channel, may block
 			dest.write(buffer);
 			// If partial transfer, shift remainder down
 			// If buffer is empty, same as doing clear()
 			buffer.compact();
 		}
 		// EOF will leave buffer in fill state
 		buffer.flip();
 		// make sure the buffer is fully drained.
 		while (buffer.hasRemaining()) {
 			dest.write(buffer);
 		}
 	}
 
 	@Override
 	public void uploadBase64EncodedMedia(String base64EncodedMedia, String mediaName) throws IOException {
 		ByteArrayInputStream bais = 
 				new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(base64EncodedMedia));
 		ReadableByteChannel readChannel = Channels.newChannel(bais);
 		
 		AppEngineFile appEngineFile = gaeFileService.createNewBlobFile("image/jpeg", mediaName);
 		
 		FileWriteChannel writeChannel = gaeFileService.openWriteChannel(appEngineFile, true);
 		
 		fastChannelCopy(readChannel, writeChannel);
 		
 		writeChannel.closeFinally();
 		readChannel.close();
 		
		updateMedia(gaeFileService.getBlobKey(appEngineFile), mediaName);
 	}
 }
