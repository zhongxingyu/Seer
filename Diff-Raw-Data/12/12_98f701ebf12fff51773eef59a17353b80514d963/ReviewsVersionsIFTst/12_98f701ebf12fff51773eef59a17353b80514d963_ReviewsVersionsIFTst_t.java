 /*******************************************************************************
  * Copyright (c) 2010 Ericsson
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * Contributors:
  *   Alvaro Sanchez-Leon - Initial Implementation
  *******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.core.rfs;
 
 import static org.junit.Assert.fail;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.egit.core.op.AddToIndexOperation;
 import org.eclipse.egit.core.op.CommitOperation;
 import org.eclipse.egit.core.project.RepositoryMapping;
 import org.eclipse.egit.core.test.GitTestCase;
 import org.eclipse.egit.core.test.TestRepository;
 import org.eclipse.egit.core.test.TestUtils;
 import org.eclipse.jgit.lib.Repository;
 import org.eclipse.mylyn.reviews.r4e.core.TstGeneral;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EFileContext;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EItem;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EUser;
 import org.eclipse.mylyn.reviews.r4e.core.model.RModelFactory;
 import org.eclipse.mylyn.reviews.r4e.core.versions.ReviewVersionsException;
 import org.eclipse.mylyn.reviews.r4e.core.versions.ReviewsVersionsIF;
 import org.eclipse.mylyn.reviews.r4e.core.versions.ReviewsVersionsIF.CommitDescriptor;
 import org.eclipse.mylyn.reviews.r4e.core.versions.ReviewsVersionsIF.FileVersionInfo;
 import org.eclipse.mylyn.reviews.r4e.core.versions.ReviewsVersionsIFFactory;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * @author lmcalvs
  * 
  */
 public class ReviewsVersionsIFTst extends GitTestCase {
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 	private IProject			fIProject		= null;
 	private TestRepository		testRepo		= null;
 	private Repository			repo			= null;
 	private ReviewsVersionsIF	versionsProx	= null;
 	private List<IResource>	resources		= new ArrayList<IResource>();
 	private IFile				ifileA			= null;
 
 	private IFile				ifileB			= null;
 
 	// ------------------------------------------------------------------------
 	// Setup and Teardown
 	// ------------------------------------------------------------------------
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 		super.setUp();
 		TstGeneral.activateTracer();
 		fIProject = project.getProject();
 		fIProject.open(null);
 		testRepo = new TestRepository(gitDir);
 		testRepo.connect(fIProject);
 		repo = RepositoryMapping.getMapping(fIProject).getRepository();
 
 		versionsProx = ReviewsVersionsIFFactory.instance.getVersionsIF(fIProject);
 
 		// Add a couple of files to the project
 		ifileA = testUtils.addFileToProject(fIProject, "sub/a.txt", "a text");
 		ifileB = testUtils.addFileToProject(fIProject, "sub/b.txt", "b text");
 
 		// Add folder to index and commit
 		resources.add(project.getProject().getFolder("sub"));
 		new AddToIndexOperation(resources).execute(null);
 		CommitOperation commitOperation = new CommitOperation(null, null, null, TestUtils.AUTHOR, TestUtils.COMMITTER,
 				"first commit");
 		commitOperation.setCommitAll(true);
		commitOperation.setRepository(repo);
 		commitOperation.execute(null);
 		fIProject.refreshLocal(IProject.DEPTH_INFINITE, null);
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@After
 	public void tearDown() throws Exception {
 		testRepo.disconnect(fIProject);
 		fIProject.close(null);
 		testRepo.dispose();
 		repo = null;
 		super.tearDown();
 	}
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 	/**
 	 * Test method for
 	 * {@link org.eclipse.mylyn.reviews.r4e.core.versions.impl.ReviewsVersionsIF#isProviderSupported(org.eclipse.core.resources.IProject)}
 	 * .
 	 */
 	@Test
 	public void testIsProviderSupported() {
 		boolean supported = versionsProx.isProviderSupported(fIProject);
 		Assert.assertTrue("not supported", supported);
 	}
 
 	/**
 	 * Test method for
 	 * {@link org.eclipse.mylyn.reviews.r4e.core.versions.impl.ReviewsVersionsIF#getLastCommitInfo(org.eclipse.core.resources.IProject)}
 	 * .
 	 */
 	@Test
 	public void testGetLastCommitInfo() {
 		CommitDescriptor cdesc = null;
 		try {
 			cdesc = versionsProx.getLastCommitInfo(fIProject);
 		} catch (ReviewVersionsException e) {
 			e.printStackTrace();
 			fail("Exception");
 		}
 
 		printCommitDescriptor(cdesc);
 
 	}
 
 	/**
 	 * Test method for
 	 * {@link org.eclipse.mylyn.reviews.r4e.core.versions.impl.ReviewsVersionsIF#getCommitInfo(org.eclipse.core.resources.IProject, java.lang.String)}
 	 * .
 	 */
 	@Test
 	public void testGetCommitInfo() {
 		CommitDescriptor cdesc = null;
 		try {
 			String lastCommitId = versionsProx.getLastCommitInfo(fIProject).getId();
 			cdesc = versionsProx.getCommitInfo(fIProject, lastCommitId);
 		} catch (ReviewVersionsException e) {
 			e.printStackTrace();
 			fail("Exception");
 		}
 
 		printCommitDescriptor(cdesc);
 	}
 
 	/**
 	 * Test method for
 	 * {@link org.eclipse.mylyn.reviews.r4e.core.versions.impl.ReviewsVersionsIF#createCommitReviewItem(org.eclipse.core.resources.IProject, java.lang.String, java.lang.String)}
 	 * .
 	 */
 	@Test
 	public void testCreateCommitReviewItem() {
 		try {
 			R4EItem revItem = createLastCommitRevItem();
 			System.out.println(revItem.toString());
 		} catch (ReviewVersionsException e) {
 			Collection<String> details = e.getDetails();
 			if (details != null) {
 				for (Iterator<String> iterator = details.iterator(); iterator.hasNext();) {
 					String info = iterator.next();
 					System.out.println(info);
 				}
 			}
 			e.printStackTrace();
 			fail("Exception");
 		}
 
 	}
 
 	/**
 	 * Test method for
 	 * {@link org.eclipse.mylyn.reviews.r4e.core.versions.impl.ReviewsVersionsIF#getFileVersionInfo(org.eclipse.core.resources.IFile)}
 	 * .
 	 */
 	@Test
 	public void testGetFileVersionInfo() {
 		FileVersionInfo info = null;
 		try {
 			info = versionsProx.getFileVersionInfo(ifileA);
 		} catch (ReviewVersionsException e) {
 			e.printStackTrace();
 			fail("Exception");
 		}
 		Assert.assertEquals("ecb10eba688e6ce0c7b5af81ed48c0d764d604fc", info.getId());
 		Assert.assertEquals("Project-1/sub/a.txt", info.getRepositoryPath());
 		Assert.assertEquals("a.txt", info.getName());
 
 		try {
 			info = versionsProx.getFileVersionInfo(ifileB);
 		} catch (ReviewVersionsException e) {
 			e.printStackTrace();
 			fail("Exception");
 		}
 
 		Assert.assertEquals("Project-1/sub/b.txt", info.getRepositoryPath());
 		Assert.assertEquals("b.txt", info.getName());
 	}
 
 	/**
 	 * Test method for
 	 * {@link org.eclipse.mylyn.reviews.r4e.core.versions.impl.ReviewsVersionsIF#openCompareSession(org.eclipse.mylyn.reviews.r4e.core.model.R4EItem)}
 	 * .
 	 */
 	@Test
 	public void testOpenCompareSession() {
 		R4EItem revItem = null;
 		try {
 			revItem = createLastCommitRevItem();
 		} catch (ReviewVersionsException e) {
 			e.printStackTrace();
 			fail("Exception");
 		}
 
 		List<R4EFileContext> contextLst = revItem.getFileContextList();
 		if (contextLst == null || contextLst.size() < 1) {
 			fail("Unexpected empty context");
 		}
 
 		// take the first one
 		R4EFileContext context = contextLst.get(0);
 
 		String bookingId = null;
 		try {
 			bookingId = versionsProx.openCompareSession(revItem);
 			versionsProx.openCompareEditor(bookingId, context);
 		} catch (ReviewVersionsException e) {
 			e.printStackTrace();
 			fail("Exception");
 		}
 		versionsProx.closeCompareSession(bookingId);
 	}
 
 	/**
 	 * Test method for
 	 * {@link org.eclipse.mylyn.reviews.r4e.core.versions.impl.ReviewsVersionsIF#openCompareEditor(java.lang.String, org.eclipse.mylyn.reviews.r4e.core.model.R4EFileContext)}
 	 * .
 	 */
 	@Test
 	public void testOpenCompareEditor() {
 		fail("Not yet implemented");
 	}
 
 	/**
 	 * Test method for
 	 * {@link org.eclipse.mylyn.reviews.r4e.core.versions.impl.ReviewsVersionsIF#closeCompareSession(java.lang.String)}.
 	 */
 	@Test
 	public void testCloseCompareSession() {
 		fail("Not yet implemented");
 	}
 
 	/**
 	 * @return
 	 * @throws ReviewVersionsException
 	 */
 	private R4EItem createLastCommitRevItem() throws ReviewVersionsException {
 		R4EUser reviewUser = RModelFactory.eINSTANCE.createR4EUser();
 		reviewUser.setId("userIdX");
 	
 		String lastCommitId = versionsProx.getLastCommitInfo(fIProject).getId();
 		R4EItem revItem = versionsProx.createCommitReviewItem(fIProject, lastCommitId, reviewUser);
 		return revItem;
 	}
 
 	private void printCommitDescriptor(CommitDescriptor cdesc) {
 		System.out.println("Author: " + cdesc.getAuthor());
 		System.out.println("Commiter: " + cdesc.getCommitter());
 		System.out.println("Id: " + cdesc.getId());
 		System.out.println("Message: " + cdesc.getMessage());
 		System.out.println("Title: " + cdesc.getTitle());
 		String[] changeSets = cdesc.getChangeSet();
 		for (int i = 0; i < changeSets.length; i++) {
 			String change = changeSets[i];
 			System.out.println("Change " + i + ": " + change);
 		}
 	
 		System.out.println("Commit Date: " + new Date(cdesc.getCommitDate()));
 	
 		String[] parents = cdesc.getParentIDs();
 		for (int i = 0; i < parents.length; i++) {
 			String parent = parents[i];
 			System.out.println("Parent " + i + ": " + parent);
 		}
 	}
 
 }
 
 
