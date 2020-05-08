 package de.fhb.dlService;
 
 import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.StringWriter;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.io.IOUtils;
 
 import com.amazonaws.services.s3.AmazonS3;
 import com.amazonaws.services.s3.model.Bucket;
 import com.amazonaws.services.s3.model.GetObjectRequest;
 import com.amazonaws.services.s3.model.ObjectListing;
 import com.amazonaws.services.s3.model.ObjectMetadata;
 import com.amazonaws.services.s3.model.PutObjectRequest;
 import com.amazonaws.services.s3.model.Region;
 import com.amazonaws.services.s3.model.S3Object;
 import com.amazonaws.services.s3.model.S3ObjectInputStream;
 import com.amazonaws.services.s3.model.S3ObjectSummary;
 
 /**
  * This Class will create an Java S3 Bucket and manage many of its operations
  * 
  * @author MaccaPC
  * @author Tony
  * 
  */
 public class BucketUtil {
 	// private Logger log = Logger.getLogger(BucketUtil.class.getName());
 	/**
 	 * IS the Amazone S3 Object attached to this instance which will be filled
 	 * on instance creation.
 	 */
 	private AmazonS3 aAmzObj = null;
 
 	/**
 	 * Default Constructor
 	 * 
 	 * @param aAmzS3Ins
 	 *            the amazons3 credential instance
 	 */
 	public BucketUtil(AmazonS3 aAmzS3Ins) {
 		aAmzObj = aAmzS3Ins;
 	}
 
 	/**
 	 * This Method will create a bucket in the Region Eu Ireland.
 	 * 
 	 * @param aBckNam
 	 *            the given bucket name
 	 */
 	public void createABucket(String aBckNam) {
 		if (aAmzObj != null || aBckNam != null) {
 			aAmzObj.createBucket(aBckNam, Region.EU_Ireland);
 		}
 	}
 
 	/**
 	 * This Method deletes a bucket. KEEP IN MIND BUCKET NEEDS TO BE EMPTY TO
 	 * DELETE !
 	 * 
 	 * @param aBckNam
 	 *            the given bucketname
 	 */
 	public void delBucket(String aBckNam) {
 		if (aAmzObj != null || aBckNam != null) {
 			aAmzObj.deleteBucket(aBckNam);
 		}
 	}
 
 	/**
 	 * This Method list all available buckets that are allowed by given user.
 	 */
 	public void listBucket() {
 		if (aAmzObj != null) {
 			for (Bucket aBckEle : aAmzObj.listBuckets()) {
 				System.out.println(aBckEle.getName());
 			}
 		}
 	}
 
 	/**
 	 * Finds the designated bucket.
 	 * 
 	 * @param aBckNam
 	 *            they bucket name to search after
 	 * @return returns true if found, otherwise false
 	 */
 	public boolean findBucket(String aBckNam) {
 		if (aAmzObj != null || aBckNam != null) {
 			for (Bucket aBckEle : aAmzObj.listBuckets()) {
 				if (aBckNam.equals(aBckEle.getName())) {
 					return true;
 				}
 
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Uploads onto a designated bucket.
 	 * 
 	 * @param aBckNam
 	 *            the received bucket name
 	 * @param src
 	 *            the received source
 	 * @param aKey
 	 *            the received key
 	 */
 	public void uploadOntoBucket(String aBckNam, Object src, String aKey) {
 		// Checks if the received Object is a File
 		if (aBckNam != null && aKey != null) {
 			if (src instanceof File) {
 				aAmzObj.putObject(new PutObjectRequest(aBckNam, aKey,
 						(File) src));
 			} else if (src instanceof InputStream) {
 				try {
 					Long contentLength = null;
 					InputStream is = (InputStream) src;
 					byte[] contentBytes;
 
 					contentBytes = IOUtils.toByteArray(is);
 					contentLength = Long.valueOf(contentBytes.length);
					is = new ByteArrayInputStream(contentBytes);
					
 					ObjectMetadata metadata = new ObjectMetadata();
 					metadata.setContentLength(contentLength);
 					aAmzObj.putObject(new PutObjectRequest(aBckNam, aKey,
 							(InputStream) src, metadata));
 				} catch (IOException e) {
 				}
 			}
 		}
 	}
 
 	/**
 	 * Downloads a Object from designated bucket.
 	 * 
 	 * @param aBckNam
 	 *            the bucket name
 	 * @param aKey
 	 *            the key which will be searched after
 	 * @return returns a Object if nothing null
 	 */
 	public S3ObjectInputStream downloadFromBucket(String aBckNam, String aKey) {
 		if (aBckNam != null && aKey != null) {
 			S3Object aS3Obj = aAmzObj.getObject(new GetObjectRequest(aBckNam,
 					aKey));
 			if (aS3Obj != null) {
 				return aS3Obj.getObjectContent();
 
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Lists content from designated bucket
 	 * 
 	 * @param aBckNam
 	 *            the given bucket name
 	 * @throws IOException
 	 */
 	public List<String> listBucketContent(String aBckNam) throws IOException {
 
 		ObjectListing objCnt = aAmzObj.listObjects(aBckNam);
 		List<S3ObjectSummary> objLst = objCnt.getObjectSummaries();
 		List<String> strLst = new ArrayList<String>();
 
 		for (S3ObjectSummary s3ObjectSummaryEle : objLst) {
 			S3Object aS3Obj = aAmzObj.getObject(aBckNam,
 					s3ObjectSummaryEle.getKey());
 
 			StringWriter writer = new StringWriter();
 			IOUtils.copy(aS3Obj.getObjectContent(), writer,
 					Charset.defaultCharset());
 			String theString = writer.toString();
 			strLst.add(theString);
 		}
 
 		return strLst;
 	}
 
 	/**
 	 * Displays the inputstream line by line
 	 * 
 	 * @param input
 	 *            the inputstream
 	 * @throws IOException
 	 */
 	private static void displayTextInputStream(InputStream input)
 			throws IOException {
 		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
 		while (true) {
 			String line = reader.readLine();
 			if (line == null)
 				break;
 
 			System.out.println("    " + line);
 		}
 		System.out.println();
 	}
 
 }
