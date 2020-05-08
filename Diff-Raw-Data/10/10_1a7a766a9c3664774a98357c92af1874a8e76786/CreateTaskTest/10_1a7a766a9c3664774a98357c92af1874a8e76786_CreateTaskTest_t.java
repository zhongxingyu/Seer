 package com.horsefire.syncaws.tasks;
 
 import static org.easymock.EasyMock.anyObject;
 import static org.easymock.EasyMock.createMock;
 import static org.easymock.EasyMock.eq;
 import static org.easymock.EasyMock.expect;
 import static org.easymock.EasyMock.expectLastCall;
 import static org.easymock.EasyMock.replay;
 import static org.easymock.EasyMock.verify;
 
 import java.io.File;
 import java.util.Collection;
 import java.util.Collections;
 
 import junit.framework.TestCase;
 
 import com.horsefire.syncaws.CommandLineArgs;
 import com.horsefire.syncaws.CommandLineArgsBuilder;
 import com.horsefire.syncaws.ConfigService;
 import com.horsefire.syncaws.Project;
 import com.horsefire.syncaws.UuidGenerator;
 import com.horsefire.syncaws.aws.AwsClient;
 import com.horsefire.syncaws.aws.UrlService;
 import com.horsefire.util.FileDelegate;
 
 public class CreateTaskTest extends TestCase {
 
 	public void testCreateProjectWithWindowsPath() throws Exception {
 		final String dirPath = "C:\\Users\\steve";
 		final String properDirPath = "C:/Users/steve";
 		final String projectName = "myProject";
 		CommandLineArgs options = new CommandLineArgsBuilder().dir(dirPath)
 				.project(projectName).build();
 
 		ConfigService configService = createMock(ConfigService.class);
 		UuidGenerator generator = createMock(UuidGenerator.class);
 		AwsClient client = createMock(AwsClient.class);
 		UrlService urlService = createMock(UrlService.class);
 		FileDelegate fileDelegate = createMock(FileDelegate.class);
 
 		expect(fileDelegate.isDirectory(eq(new File(options.getDir()))))
 				.andReturn(true);
		expect(fileDelegate.getAbsolutePath(eq(new File(options.getDir()))))
				.andReturn(dirPath);
 
 		Collection<Project> projects = Collections.emptyList();
 		expect(configService.getProjects()).andReturn(projects);
 
 		final String guidString = "guidString";
 		expect(generator.getId()).andReturn(guidString);
 
 		String url = "url/for/html/file";
 		expect(urlService.getHtmlFile((Project) anyObject())).andReturn(url);
 		client.putHtml((String) anyObject(), eq(url));
 
 		url = "url/for/index/list";
 		expect(urlService.getIndexList((Project) anyObject())).andReturn(url);
 		client.putJson((String) anyObject(), eq(url));
 
 		configService.addProject((Project) anyObject());
 		expectLastCall().andDelegateTo(new ConfigService(null) {
 			@Override
 			public void addProject(Project project) {
 				assertEquals(guidString, project.getId());
 				assertEquals(properDirPath, project.getBaseDir());
 				assertEquals(projectName, project.getName());
 			}
 		});
 
 		replay(configService, generator, client, urlService, fileDelegate);
 
 		CreateTask task = new CreateTask(options, configService, generator,
 				client, urlService, fileDelegate);
 		task.validate();
 		task.run();
 
 		verify(configService, generator, client, urlService, fileDelegate);
 	}
 }
