 /*
  * Copyright (c) NASK, NCSC
  * 
  * This file is part of HoneySpider Network 2.0.
  * 
  * This is a free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package pl.nask.hsn2.framwework.workflow.repository;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.io.input.ReaderInputStream;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.testng.annotations.AfterTest;
 import org.testng.annotations.BeforeTest;
 import org.testng.annotations.Test;
 
 import pl.nask.hsn2.framework.workflow.repository.GitWorkflowRepository;
 import pl.nask.hsn2.framework.workflow.repository.WorkflowRepoException;
 import pl.nask.hsn2.framework.workflow.repository.WorkflowVersionInfo;
 
 public class GitRepositoryTest {
 
 	private static final Logger LOGGER = LoggerFactory.getLogger(GitRepositoryTest.class);
	String repoDir = removeEndingFileSeparator(System.getProperty("java.io.tmpdir")) + "/test-git-repo";
 	GitWorkflowRepository repo;
 	String WORKFLOW_NAME = "newTestFile";
 	String testInput2 = "test input 2";
 	String testInput = "test input";
 
 	@BeforeTest
 	public void initRepo() throws WorkflowRepoException, IOException {
 		File r = new File(repoDir);
 		FileUtils.deleteDirectory(r);
 		repo = new GitWorkflowRepository(repoDir, true);
 	}
 
 	@AfterTest
 	public void removeRepo() throws IOException {
 		File r = new File(repoDir);
 		FileUtils.deleteDirectory(r);
 	}
 
	private String removeEndingFileSeparator(String pathToCheck) {
		String fileSeparator = System.getProperty("file.separator");
		String lastPathChar = "" + pathToCheck.charAt(pathToCheck.length() - 1);
		String result;
		if (fileSeparator.equals(lastPathChar)) {
			result = pathToCheck.substring(0, pathToCheck.length() - 1);
		} else {
			result = pathToCheck;
		}
		return result;
	}

 	@Test
 	public void saveNewWorkflow() throws WorkflowRepoException {
 		LOGGER.info("saving workflow...");
 		StringReader reader = new StringReader(testInput);
 		InputStream is = new ReaderInputStream(reader);
 		repo.saveWorkflow(WORKFLOW_NAME, is);
 		Assert.assertTrue(fileExists(WORKFLOW_NAME));
 	}
 
 	@Test(dependsOnMethods = "saveNewWorkflow")
 	public void updateNewWorkflow() throws WorkflowRepoException, InterruptedException {
 		LOGGER.info("updating workflow...");
 		StringReader reader = new StringReader(testInput2);
 		InputStream is = new ReaderInputStream(reader);
 		repo.saveWorkflow(WORKFLOW_NAME, is);
 		Assert.assertTrue(fileExists(WORKFLOW_NAME));
 	}
 
 	@Test(dependsOnMethods = "updateNewWorkflow")
 	public void listWorkflowsTest() throws IOException, WorkflowRepoException {
 		LOGGER.info("listing workflows...");
 
 		// creating file not under git management
 		FileWriter f = new FileWriter(new File(repoDir, "unexpectedFile"));
 		f.write("sadsadasfas");
 		f.close();
 
 		List<String> list = repo.listWorkflowNames();
 		Assert.assertFalse(list.contains(".git"));
 		Assert.assertTrue(list.contains(WORKFLOW_NAME));
 		for (String fileName : list) {
 			LOGGER.info("found file:{}", fileName);
 		}
 		Assert.assertEquals(list.size(), 1);
 		Assert.assertEquals(list.get(0), WORKFLOW_NAME);
 	}
 
 	@Test(dependsOnMethods = "updateNewWorkflow")
 	public void getWorkflowFileHead() throws IOException, WorkflowRepoException {
 		InputStream is = repo.getWorkflowFile(WORKFLOW_NAME, null);
 		Assert.assertNotNull(is);
 		assertSameAs(testInput2, is);
 	}
 
 	@Test(dependsOnMethods = { "updateNewWorkflow", "getVersions" })
 	public void getWorkflowFileVersion1() throws IOException, WorkflowRepoException {
 		List<WorkflowVersionInfo> versions = repo.getVersions(WORKFLOW_NAME);
 		InputStream is = repo.getWorkflowFile(WORKFLOW_NAME, versions.get(0).getVersion()); // same as head!
 		assertSameAs(testInput2, is);
 	}
 
 	@Test(dependsOnMethods = { "updateNewWorkflow", "getVersions" })
 	public void getWorkflowFileVersion2() throws IOException, WorkflowRepoException {
 		List<WorkflowVersionInfo> versions = repo.getVersions(WORKFLOW_NAME);
 		InputStream is = repo.getWorkflowFile(WORKFLOW_NAME, versions.get(1).getVersion()); // same as head!
 		assertSameAs(testInput, is);
 	}
 
 	private void assertSameAs(String expected, InputStream is) throws IOException {
 		try {
 			String content = IOUtils.toString(is);
 			Assert.assertEquals(expected, content);
 		} finally {
 			is.close();
 		}
 	}
 
 	@Test(dependsOnMethods = "updateNewWorkflow")
 	public void getVersions() throws WorkflowRepoException {
 		List<WorkflowVersionInfo> versions = repo.getVersions("newTestFile");
 		Assert.assertNotNull(versions);
 		Assert.assertEquals(2, versions.size());
 		for (WorkflowVersionInfo info : versions) {
 			LOGGER.info("rev={}, ts={}", info.getVersion(), info.getVersionTimestamp());
 		}
 	}
 
 	private boolean fileExists(String name) {
 		return new File(repoDir, name).exists();
 	}
 }
