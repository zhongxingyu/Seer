 package com.horsefire.syncaws.tasks;
 
 import static org.easymock.EasyMock.createMock;
 import static org.easymock.EasyMock.expect;
 import static org.easymock.EasyMock.expectLastCall;
 import static org.easymock.EasyMock.replay;
 import static org.easymock.EasyMock.verify;
 import junit.framework.TestCase;
 
 import org.jets3t.service.S3ServiceException;
 import org.junit.Test;
 
 import com.horsefire.syncaws.ConfigService;
 import com.horsefire.syncaws.aws.AwsClient;
 
 public class ValidateTaskTest extends TestCase {
 
 	@Test
 	public void testHappyPath() throws Exception {
 		ConfigService configService = createMock(ConfigService.class);
 		expect(configService.getBaseUrl()).andReturn("");
 		AwsClient awsClient = createMock(AwsClient.class);
 		awsClient.testConnection();
 		expectLastCall().once();
 		replay(configService, awsClient);
 		ValidateTask task = new ValidateTask(configService, awsClient);
 		task.validate();
 		task.run();
 		verify(configService, awsClient);
 	}
 
 	@Test
 	public void testConfigFailure() throws Exception {
 		ConfigService configService = createMock(ConfigService.class);
		expect(configService.getBaseUrl()).andThrow(new RuntimeException());
 		AwsClient awsClient = createMock(AwsClient.class);
 		replay(configService, awsClient);
 		ValidateTask task = new ValidateTask(configService, awsClient);
 		task.validate();
 		try {
 			task.run();
 			fail("Should have thrown an exception");
 		} catch (RuntimeException e) {
 			// Message should be about the config system
 			assertTrue(e.getMessage().indexOf("config") != -1);
 		}
 		verify(configService, awsClient);
 	}
 
 	@Test
 	public void testS3Failure() throws Exception {
 		ConfigService configService = createMock(ConfigService.class);
 		expect(configService.getBaseUrl()).andReturn("");
 		AwsClient awsClient = createMock(AwsClient.class);
 		awsClient.testConnection();
 		expectLastCall().andThrow(new S3ServiceException()).once();
 		replay(configService, awsClient);
 		ValidateTask task = new ValidateTask(configService, awsClient);
 		task.validate();
 		try {
 			task.run();
 			fail("Should have thrown an exception");
 		} catch (RuntimeException e) {
 			// Message should be about connecting to S3
 			assertTrue(e.getMessage().indexOf("S3") != -1);
 		}
 		verify(configService, awsClient);
 	}
 }
