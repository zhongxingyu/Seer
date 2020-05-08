 package test.java;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Map;
 
 import me.s3for.common.Common;
 import me.s3for.common.FileUtils;
 import me.s3for.common.S3Utils;
 import me.s3for.common.StringUtils;
 
 import org.apache.log4j.Level;
 import org.testng.Assert;
 import org.testng.annotations.BeforeGroups;
 import org.testng.annotations.Test;
 
 import com.amazonaws.services.s3.model.CompleteMultipartUploadResult;
 import com.amazonaws.services.s3.model.ObjectMetadata;
 import com.amazonaws.services.s3.model.Permission;
 import com.amazonaws.services.s3.model.S3Object;
 
 public class Api_08_MultipartUpload_Test extends TestInitialize {
 
 	S3Utils s3Utils, s3UtilsAws;
 	CompleteMultipartUploadResult cMultiUpResult, cMultiUpResultAws;
	String fileName = "test_1mb.file";
 	String filePath = FileUtils.getRootPath() + "\\static\\" + fileName;
 	int partSizeMb = 1;
 	File file, fileAws;
 
 	/**
 	 * @desc The code to be run before each test
 	 */
 
 	@BeforeGroups(groups = { "api" })
 	public void before() {
 		logger.setLevel(Level.ERROR);
 
 		// initiate S3 and AWS
 		s3Utils = new S3Utils(keyS3, secretS3, serverS3);
 		s3UtilsAws = new S3Utils();
 
 		// set bucket to work with
 		s3Utils.setBacket(bucketName);
 		s3UtilsAws.setBacket(bucketNameAws);
 
 		// put a multipart file into a basket
 		// cMultiUpResult = s3Utils
 		// .multipartUpload(fileName, filePath, partSizeMb);
 		// cMultiUpResultAws = s3UtilsAws.multipartUpload(fileName, filePath,
 		// partSizeMb);
 
 		// s3Utils.setObjectAcl(fileName, Permission.Read);
 		// s3UtilsAws.setObjectAcl(fileName, Permission.Read);
 
 	}
 
 	/**
 	 * @desc Check ability to delete object from a bucket
 	 * 
 	 * @throws IOException
 	 */
 
 	@Test(groups = { "api" })
 	public void multipartUploadTest() throws Exception {
 
 		// Get S3 objects
 		S3Object s3Object = s3Utils.get(fileName);
 		S3Object s3ObjectAws = s3UtilsAws.get(fileName);
 
 		ObjectMetadata s3ObjectMetadata = s3Object.getObjectMetadata();
 		ObjectMetadata s3ObjectMetadataAws = s3ObjectAws.getObjectMetadata();
 
 		Assert.assertEquals(file.length(), s3ObjectMetadata.getContentLength());
 		Assert.assertEquals(file.length(),
 				s3ObjectMetadataAws.getContentLength());
 
 		// Get file content
 		String content = StringUtils.inputStreamToString(s3Object
 				.getObjectContent());
 		String contentAws = StringUtils.inputStreamToString(s3ObjectAws
 				.getObjectContent());
 
 		Assert.assertEquals(content, contentAws);
 
 		Map<String, Object> map = Common.compareMaps(
 				s3ObjectMetadata.getRawMetadata(),
 				s3ObjectMetadataAws.getRawMetadata(), avoidKeys);
 
 		System.out.println("Metadata: S3 vs AWS");
 		Common.printMap(map);
 
 		Assert.assertTrue(map.size() == 0, "Objects' metadata are not the same");
 
 	}
 }
