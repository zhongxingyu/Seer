 package com.parc.ccn.library.io;
 
 import java.io.IOException;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.security.SignatureException;
 
 import javax.xml.stream.XMLStreamException;
 
 import com.parc.ccn.Library;
 import com.parc.ccn.data.ContentName;
 import com.parc.ccn.data.content.HeaderData;
 import com.parc.ccn.data.content.HeaderData.HeaderObject;
 import com.parc.ccn.data.security.KeyLocator;
 import com.parc.ccn.data.security.PublisherPublicKeyDigest;
 import com.parc.ccn.data.security.SignedInfo.ContentType;
 import com.parc.ccn.library.CCNFlowControl;
 import com.parc.ccn.library.CCNLibrary;
 import com.parc.ccn.library.CCNSegmenter;
 import com.parc.ccn.library.CCNFlowControl.Shape;
 import com.parc.ccn.library.profiles.SegmentationProfile;
 import com.parc.ccn.security.crypto.ContentKeys;
 
 public class CCNFileOutputStream extends CCNVersionedOutputStream {
 
 	public CCNFileOutputStream(ContentName name,
 			PublisherPublicKeyDigest publisher, ContentKeys keys,
 			CCNLibrary library)
 			throws IOException {
 		super(name, null, publisher, keys, library);
 	}
 
 	public CCNFileOutputStream(ContentName name,
 			PublisherPublicKeyDigest publisher, CCNLibrary library)
 			throws IOException {
 		super(name, null, publisher, library);
 	}
 
 	public CCNFileOutputStream(ContentName name, CCNLibrary library)
 			throws IOException {
 		super(name, library);
 	}
 
 	public CCNFileOutputStream(ContentName name, KeyLocator locator,
 			PublisherPublicKeyDigest publisher, ContentType type, CCNSegmenter segmenter)
 			throws IOException {
 		super(name, locator, publisher, type, segmenter);
 	}
 
 	public CCNFileOutputStream(ContentName name, KeyLocator locator,
 			PublisherPublicKeyDigest publisher, ContentType type, CCNFlowControl flowControl)
 			throws IOException {
 		super(name, locator, publisher, type, flowControl);
 	}
 	
 	@Override
 	protected void startWrite() throws IOException {
 		// Won't need this if header is its own stream
 		_segmenter.getFlowControl().startWrite(_baseName, Shape.STREAM_WITH_HEADER);		
 	}
 
 	protected void writeHeader() throws IOException {
 		// What do we put in the header if we have multiple merkle trees?
 		try {
 			putHeader(_baseName, lengthWritten(), getBlockSize(), _dh.digest(), null);
 		} catch (XMLStreamException e) {
 			Library.logger().fine("XMLStreamException in writing header: " + e.getMessage());
			throw new IOException("Exception in writing header", e);
 		}
 	}
 	
 	/**
 	 * Override this, not close(), because CCNOutputStream.close() currently
 	 * calls waitForPutDrain, and we don't want to call that till after we've put the header.
 	 * 
 	 * When we can, we might want to write the header earlier. Here we wait
 	 * till we know how many bytes are in the file.
 	 * @throws IOException 
 	 * @throws InterruptedException 
 	 * @throws NoSuchAlgorithmException 
 	 * @throws SignatureException 
 	 * @throws InvalidKeyException 
 	 * @throws XMLStreamException 
 	 */
 	@Override
 	protected void closeNetworkData() throws IOException, InvalidKeyException, SignatureException, NoSuchAlgorithmException, InterruptedException  {
 		super.closeNetworkData();
 		writeHeader();
 	}
 	
 	protected void putHeader(
 			ContentName name, long contentLength, int blockSize, byte [] contentDigest, 
 			byte [] contentTreeAuthenticator) throws XMLStreamException, IOException  {
 
 
 		ContentName headerName = SegmentationProfile.headerName(name);
 		HeaderData headerData = new HeaderData(contentLength, contentDigest, contentTreeAuthenticator, blockSize);
 		// DKS TODO -- deal with header encryption, making sure it has same publisher as
 		// rest of file via the segmenter
 		// The segmenter contains the flow controller. Should do the right thing whether this
 		// is a raw stream or a repo stream. It should also already have the keys. Could just share
 		// the segmenter. For now, use our own.
 		HeaderObject header = new HeaderObject(headerName, headerData, this._publisher, this._locator, this._library);
 		header.save();
 		Library.logger().info("Wrote header: " + header.getCurrentVersionName());
 	}
 }
