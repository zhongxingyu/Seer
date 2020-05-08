 package tests;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Test;
 
 import common.DiffFile;
 import common.DiffFile.DiffType;
 import common.HistoryGraph;
 import common.Repository;
 import common.Revision;
 import common.Util;
 
 import org.apache.commons.io.FileUtils;
 
 public class RepositoryTest {
 	public static final String ANT_COMMAND = "ant";
 
 	private static final String TAR_FILE = "test/sample_repositories.tar";
 	private static final String DEST_DIR = "test";
 	private static final String SAMPLE_REPOSITORIES = "test/sample_repositories";
 	
     private static final String[] DIRECTORIES = {"test/sample_repositories/repo1", 
     											"test/sample_repositories/repo2",
     											"test/sample_repositories/repo3",
     											"test/sample_repositories/repo4",
     											"test/sample_repositories/repo5"};
         
     private static final Repository REPOSITORY_1 = new Repository(DIRECTORIES[0], ANT_COMMAND);
     private static final Repository REPOSITORY_2 = new Repository(DIRECTORIES[1], ANT_COMMAND);
     private static final Repository REPOSITORY_3 = new Repository(DIRECTORIES[2], ANT_COMMAND);
     private static final Repository REPOSITORY_4 = new Repository(DIRECTORIES[3], ANT_COMMAND);
     private static final Repository REPOSITORY_5 = new Repository(DIRECTORIES[4], ANT_COMMAND);
     
 	private static final List<DiffFile> DIFF_FILES = new ArrayList<DiffFile>();
 	static {
 		DiffFile diffFile = new DiffFile(DiffType.MODIFIED, "tmpfile");
 		DIFF_FILES.add(diffFile);
 	}
 
 	/** commit ids in hGraph 1 **/
 	private static final String COMMIT_1_1 = "8fa00ca";
 	
 	/** commit ids in hGraph 2 **/
 	private static final String COMMIT_1_2 = "3e3a0ef";
 	private static final String COMMIT_2_2 = "a667caa";
 	
 	/** commit ids in hGraph 3 **/
 	private static final String COMMIT_1_3 = "e082fcd";
 	private static final String COMMIT_2_3 = "43c4a2e";
 	private static final String COMMIT_3_3 = "2512c4a";
 	
 	/** commit ids in hGraph 4 **/
 	private static final String COMMIT_1_4 = "ce7fccb";
 	private static final String COMMIT_2_4 = "50bb766";
 	private static final String COMMIT_3_4 = "8d4a966";
 	private static final String COMMIT_4_4 = "11f34d6";
 	
 	/** commit ids in hGraph 5 **/
 	private static final String COMMIT_1_5 = "eab94e5";
 	private static final String COMMIT_2_5 = "d863c56";
 	private static final String COMMIT_3_5 = "7d72bc0";
 	private static final String COMMIT_4_5 = "e14c9c6";
 	private static final String COMMIT_5_5 = "35c25ec";
 	private static final String COMMIT_6_5 = "e654cdb";
 	
 	/** a revision in a particular hGraph **/
 	private static final Revision REVISION_1_1 = new Revision(REPOSITORY_1, COMMIT_1_1);
 	
 	private static final Revision REVISION_1_2 = new Revision(REPOSITORY_2, COMMIT_1_2);
 	private static final Revision REVISION_2_2 = new Revision(REPOSITORY_2, COMMIT_2_2);
 	static {
 		REVISION_2_2.addParent(REVISION_1_2, DIFF_FILES);
 	}
 	
 	private static final Revision REVISION_1_3 = new Revision(REPOSITORY_3, COMMIT_1_3);
 	private static final Revision REVISION_2_3 = new Revision(REPOSITORY_3, COMMIT_2_3);
 	private static final Revision REVISION_3_3 = new Revision(REPOSITORY_3, COMMIT_3_3);
 	static {
 		REVISION_2_3.addParent(REVISION_1_3, DIFF_FILES);
 		REVISION_3_3.addParent(REVISION_2_3, DIFF_FILES);
 	}
 	
 	private static final Revision REVISION_1_4 = new Revision(REPOSITORY_4, COMMIT_1_4);
 	private static final Revision REVISION_2_4 = new Revision(REPOSITORY_4, COMMIT_2_4);
 	private static final Revision REVISION_3_4 = new Revision(REPOSITORY_4, COMMIT_3_4);
 	private static final Revision REVISION_4_4 = new Revision(REPOSITORY_4, COMMIT_4_4);
 	static {
 		REVISION_2_4.addParent(REVISION_1_4, DIFF_FILES);
 		REVISION_3_4.addParent(REVISION_1_4, DIFF_FILES);
 		REVISION_4_4.addParent(REVISION_3_4, DIFF_FILES);
		REVISION_4_4.addParent(REVISION_2_4, DIFF_FILES);
 	}
 	
 	private static final Revision REVISION_1_5 = new Revision(REPOSITORY_5, COMMIT_1_5);
 	private static final Revision REVISION_2_5 = new Revision(REPOSITORY_5, COMMIT_2_5);
 	private static final Revision REVISION_3_5 = new Revision(REPOSITORY_5, COMMIT_3_5);
 	private static final Revision REVISION_4_5 = new Revision(REPOSITORY_5, COMMIT_4_5);
 	private static final Revision REVISION_5_5 = new Revision(REPOSITORY_5, COMMIT_5_5);
 	private static final Revision REVISION_6_5 = new Revision(REPOSITORY_5, COMMIT_6_5);
 	static {
 		REVISION_2_5.addParent(REVISION_1_5, DIFF_FILES);
 		REVISION_3_5.addParent(REVISION_1_5, DIFF_FILES);
 		REVISION_4_5.addParent(REVISION_3_5, DIFF_FILES);
		REVISION_4_5.addParent(REVISION_2_5, DIFF_FILES);
 		REVISION_5_5.addParent(REVISION_1_5, DIFF_FILES);
 		REVISION_6_5.addParent(REVISION_5_5, DIFF_FILES);
		REVISION_6_5.addParent(REVISION_4_5, DIFF_FILES);
 	}
 	/***************************************/
 		
 	/** a set of revisions in a particular hGraph **/
 	private static final List<Revision> REVISIONS_1 = new ArrayList<Revision>();
 	static {
 		REVISIONS_1.add(REVISION_1_1);
 	}
 	
 	private static final List<Revision> REVISIONS_2 = new ArrayList<Revision>();
 	static {
 		REVISIONS_2.add(REVISION_2_2);
 		REVISIONS_2.add(REVISION_1_2);
 	}
 	
 	private static final List<Revision> REVISIONS_3 = new ArrayList<Revision>();
 	static {
 		REVISIONS_3.add(REVISION_3_3);
 		REVISIONS_3.add(REVISION_2_3);
 		REVISIONS_3.add(REVISION_1_3);
 	}
 	
 	private static final List<Revision> REVISIONS_4 = new ArrayList<Revision>();
 	static {
 		REVISIONS_4.add(REVISION_4_4);
 		REVISIONS_4.add(REVISION_3_4);
 		REVISIONS_4.add(REVISION_2_4);
 		REVISIONS_4.add(REVISION_1_4);
 	}
 	
 	private static final List<Revision> REVISIONS_5 = new ArrayList<Revision>();
 	static {
 		REVISIONS_5.add(REVISION_6_5);
 		REVISIONS_5.add(REVISION_5_5);
 		REVISIONS_5.add(REVISION_4_5);
 		REVISIONS_5.add(REVISION_3_5);
 		REVISIONS_5.add(REVISION_2_5);
 		REVISIONS_5.add(REVISION_1_5);
 	}
 	/***********************************************/
 	
 	private static final String[] START_COMMIT_IDS = {COMMIT_1_1, COMMIT_2_2, COMMIT_3_3, COMMIT_4_4, COMMIT_6_5};
 	
 	private static final HistoryGraph HGRAPH_1 = new HistoryGraph();
 	private static final HistoryGraph HGRAPH_2 = new HistoryGraph();
 	private static final HistoryGraph HGRAPH_3 = new HistoryGraph();
 	private static final HistoryGraph HGRAPH_4 = new HistoryGraph();
 	private static final HistoryGraph HGRAPH_5 = new HistoryGraph();
 	static {
 		buildHistoryGraph(HGRAPH_1, REVISIONS_1);
 		buildHistoryGraph(HGRAPH_2, REVISIONS_2);
 		buildHistoryGraph(HGRAPH_3, REVISIONS_3);
 		buildHistoryGraph(HGRAPH_4, REVISIONS_4);
 		buildHistoryGraph(HGRAPH_5, REVISIONS_5);
 	}
 	
 	private static final HistoryGraph[] EXPECTED_HGRAPHS = {HGRAPH_1, HGRAPH_2, HGRAPH_3, HGRAPH_4, HGRAPH_5};
 	
 	@Test
 	public void testBuildHistoryGraphOnSampleRepositories() {
 		assertTrue(untarSampleRepositories());
 		
 		for (int i = 0; i < DIRECTORIES.length; i++) {
 			Repository repo = new Repository(DIRECTORIES[i], ANT_COMMAND);
 	
 			HistoryGraph actualHGraph = null;
 			actualHGraph = repo.buildHistoryGraph(START_COMMIT_IDS[i]);
 			
 			assertNotNull("constructor returns null on " + DIRECTORIES[i], actualHGraph);
 			assertEquals("result mismatched on " + DIRECTORIES[i], EXPECTED_HGRAPHS[i], actualHGraph);
 		}
 		
 		assertTrue(deleteSampleRepositores());
 	}
 	
 	@Test
 	public void testBuildHistoryGraphOnVoldemortRepository() {
 		// TODO : implement test
 	}
 
 	private static void buildHistoryGraph(HistoryGraph hGraph, List<Revision> revisions) {
 		for (Revision revision : revisions) {
 			hGraph.addRevision(revision);
 		}
 	}
 
 	private static boolean untarSampleRepositories() {
 		try {
 			Util.untar(TAR_FILE, DEST_DIR);
 			return true;
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		return false;
 	}
 	
 	private static boolean deleteSampleRepositores() {
 		File target = new File(SAMPLE_REPOSITORIES);
 		try {
 			FileUtils.deleteDirectory(target);
 			return true;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 }
