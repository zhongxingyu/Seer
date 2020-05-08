 package org.opencredo.s3;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 //import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 @RunWith(MockitoJUnitRunner.class)
 public class S3TemplateTests {
 
 	//Log log = LogFactory.getLog(this.getClass());
 	private final Log logger = LogFactory.getLog(S3TemplateTests.class);
 	private S3Template s3Template;
 	
 	/*
 	@Mock
 	private S3Service s3Service; */
 	
 	@Before
 	public void init(){
 		s3Template = new S3Template();
 		s3Template.setDefaultBucketName("oc-test");
 		s3Template.setAccessKey("AKIAJJC4KITQHSAY43MQ");
 		s3Template.setSecretAccessKey("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");
 		s3Template.connect();
 		logger.debug("default bucket: "+ s3Template.getDefaultBucketName());
 	}
 	
 	@Test
 	public void testSendStringRuns(){	
 		String testStringToSent = new String("Test string");
 		s3Template.send("testKey1", testStringToSent);
 	}
 	
 	@Test
 	public void listBucketsPrintOutput(){
 		String [] bucketNames = s3Template.listBuckets();
 		logger.debug("buckets: "+ bucketNames);
 	}
 	
 }
