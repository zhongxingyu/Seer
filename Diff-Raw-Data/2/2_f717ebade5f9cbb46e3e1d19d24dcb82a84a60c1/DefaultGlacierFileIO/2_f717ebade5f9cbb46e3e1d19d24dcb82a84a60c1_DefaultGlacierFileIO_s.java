 package com.neodem.aback.aws.glacier;
 
 import java.io.ByteArrayInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.channels.SeekableByteChannel;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import com.amazonaws.AmazonClientException;
 import com.amazonaws.AmazonServiceException;
 import com.amazonaws.auth.AWSCredentials;
 import com.amazonaws.services.glacier.AmazonGlacierClient;
 import com.amazonaws.services.glacier.TreeHashGenerator;
 import com.amazonaws.services.glacier.model.CompleteMultipartUploadRequest;
 import com.amazonaws.services.glacier.model.CompleteMultipartUploadResult;
 import com.amazonaws.services.glacier.model.InitiateMultipartUploadRequest;
 import com.amazonaws.services.glacier.model.InitiateMultipartUploadResult;
 import com.amazonaws.services.glacier.model.UploadMultipartPartRequest;
 import com.amazonaws.services.glacier.model.UploadMultipartPartResult;
 import com.amazonaws.services.glacier.transfer.ArchiveTransferManager;
 import com.amazonaws.services.glacier.transfer.UploadResult;
 import com.amazonaws.util.BinaryUtils;
 
 /**
  * 
  * (some code adapted from the glacier docs)
  * 
  * @author vfumo
  * 
  */
 public class DefaultGlacierFileIO implements GlacierFileIO {
 
 	private static Logger log = Logger.getLogger(DefaultGlacierFileIO.class);
 
 	/**
 	 * 1MB in bytes
 	 */
 	public static final int LARGEFILEPARTSIZE = 1048576;
 
 	/**
 	 * 5MB in bytes
 	 */
 //	private static final int FIVEMEGS = 5242880;
 	private static final int FIVEMEGS = 2242880;
 
 	private AWSCredentials awsCredentials;
 	private AmazonGlacierClient amazonGlacierClient;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.neodem.aback.aws.glacier.FileIO#writeFile(java.nio.file.Path)
 	 */
 	public String writeFile(Path path, String vaultName) throws GlacierFileIOException {
 		log.info("writeFile(" + path + "," + vaultName);
 
 		long fileSize;
 		try {
 			fileSize = Files.size(path);
 		} catch (IOException e) {
 			String msg = "Could not determine filesize : " + e.getMessage();
 			throw new GlacierFileIOException(msg, e);
 		}
 
 		if (fileSize < FIVEMEGS) {
 			return uploadFile(path, vaultName);
 		}
 
 		return uploadLargeFile(path, vaultName, fileSize);
 	}
 
 	private String uploadFile(Path path, String vaultName) throws GlacierFileIOException {
 		UploadResult result;
 		try {
 			ArchiveTransferManager atm = new ArchiveTransferManager(amazonGlacierClient, awsCredentials);
 			result = atm.upload(vaultName, "my archive " + (new Date()), path.toFile());
 		} catch (AmazonServiceException e) {
 			String msg = "Issue with the upload to glacier : " + e.getMessage();
 			throw new GlacierFileIOException(msg, e);
 		} catch (AmazonClientException e) {
 			String msg = "Issue with the upload to glacier : " + e.getMessage();
 			throw new GlacierFileIOException(msg, e);
 		} catch (FileNotFoundException e) {
 			String msg = "Issue with the upload to glacier : " + e.getMessage();
 			throw new GlacierFileIOException(msg, e);
 		}
 		return result.getArchiveId();
 	}
 
 	private String uploadLargeFile(Path path, String vaultName, long fileSize) throws GlacierFileIOException {
 		log.info("writeLargeFile(" + path + "," + vaultName);
 
 		String uploadId = initiateMultipartUpload(vaultName);
 		String checksum = uploadParts7(uploadId, path, fileSize, vaultName);
 		String archiveId = completeMultiPartUpload(uploadId, fileSize, checksum, vaultName);
 
 		return archiveId;
 	}
 
 
 
 	private String uploadParts7(String archiveId, Path path, long fileLen, String vaultName) throws GlacierFileIOException {
 		List<byte[]> binaryChecksums = new LinkedList<byte[]>();
 		SeekableByteChannel sbc = null;
 		try {
 			sbc = Files.newByteChannel(path);
 			long currentPosition = 0;
 			while (currentPosition < fileLen) {
 				currentPosition = uploadFilePart7(sbc, archiveId, currentPosition, binaryChecksums, vaultName);
 				if (currentPosition == -1) {
 					break;
 				}
 			}
 		} catch (FileNotFoundException e) {
 			String msg = "Could not find the upload file : " + e.getMessage();
 			throw new GlacierFileIOException(msg, e);
 		} catch (IOException e) {
 			String msg = "Issue with the upload to glacier : " + e.getMessage();
 			throw new GlacierFileIOException(msg, e);
 		} finally {
 			if (sbc != null) {
 				try {
 					sbc.close();
 				} catch (IOException e) {
 					String msg = "could not close the upload file : " + e.getMessage();
 					throw new GlacierFileIOException(msg, e);
 				}
 			}
 		}
 
 		String checksum = TreeHashGenerator.calculateTreeHash(binaryChecksums);
 		return checksum;
 	}
 
 	private long uploadFilePart7(SeekableByteChannel sbc, String archiveId, long startPosition, List<byte[]> binaryChecksums, String vaultName)
 			throws IOException {
 
 		ByteBuffer buf = ByteBuffer.allocate(LARGEFILEPARTSIZE);
 		int read = sbc.read(buf);
 		if (read == -1) {
 			return -1;
 		}
 		
 		buf.rewind();
 		
 		byte[] bytesToUpload;
 		if(read < LARGEFILEPARTSIZE) {
 			bytesToUpload = new byte[read];
 		} else {
 			bytesToUpload = new byte[LARGEFILEPARTSIZE];
 		}
 		buf.get(bytesToUpload, 0, read);
 		
 		String contentRange = String.format("bytes %s-%s/*", startPosition, startPosition + read - 1);
 		String checksum = TreeHashGenerator.calculateTreeHash(new ByteArrayInputStream(bytesToUpload));
 		byte[] binaryChecksum = BinaryUtils.fromHex(checksum);
 		binaryChecksums.add(binaryChecksum);
 		log.debug(contentRange);
 
 		UploadMultipartPartRequest partRequest = new UploadMultipartPartRequest().withVaultName(vaultName).withBody(new ByteArrayInputStream(bytesToUpload))
 				.withChecksum(checksum).withRange(contentRange).withUploadId(archiveId);
 
 		UploadMultipartPartResult partResult = amazonGlacierClient.uploadMultipartPart(partRequest);
 		log.debug("Part uploaded, checksum: " + partResult.getChecksum());
 
 		return startPosition + read;
 	}
 
 	private String completeMultiPartUpload(String uploadId, long fileLen, String checksum, String vaultName) {
 
 		CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest().withVaultName(vaultName).withUploadId(uploadId)
 				.withChecksum(checksum).withArchiveSize(String.valueOf(fileLen));
 
 		CompleteMultipartUploadResult compResult = amazonGlacierClient.completeMultipartUpload(compRequest);
		return compResult.getLocation();
 	}
 
 	private String initiateMultipartUpload(String vaultName) {
 		InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest().withVaultName(vaultName)
 				.withArchiveDescription("my archive " + (new Date())).withPartSize("" + LARGEFILEPARTSIZE);
 
 		InitiateMultipartUploadResult result = amazonGlacierClient.initiateMultipartUpload(request);
 
 		return result.getUploadId();
 	}
 
 	public void setAwsCredentials(AWSCredentials awsCredentials) {
 		this.awsCredentials = awsCredentials;
 	}
 
 	public void setAmazonGlacierClient(AmazonGlacierClient amazonGlacierClient) {
 		this.amazonGlacierClient = amazonGlacierClient;
 	}
 }
