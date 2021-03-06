 /*******************************************************************************
  * Copyright (c) 2004 - 2006 Mylar committers and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 
 package org.eclipse.mylyn.tasks.tests;
 
 import java.io.File;
 import java.io.RandomAccessFile;
 
 import junit.framework.TestCase;
 
 import org.eclipse.core.runtime.Status;
 import org.eclipse.mylyn.core.MylarStatusHandler;
 import org.eclipse.mylyn.internal.tasks.ui.actions.CopyAttachmentToClipboardJob;
 import org.eclipse.mylyn.internal.tasks.ui.actions.DownloadAttachmentJob;
 import org.eclipse.mylyn.tasks.core.RepositoryAttachment;
 import org.eclipse.mylyn.tasks.core.TaskRepository;
 import org.eclipse.mylyn.tasks.core.TaskRepositoryManager;
 import org.eclipse.mylyn.tasks.tests.connector.MockAttachmentHandler;
 import org.eclipse.mylyn.tasks.tests.connector.MockRepositoryConnector;
 import org.eclipse.mylyn.tasks.tests.connector.MockStatusHandler;
 import org.eclipse.mylyn.tasks.ui.TasksUiPlugin;
 import org.eclipse.swt.dnd.Clipboard;
 import org.eclipse.swt.dnd.TextTransfer;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * Test task attachment jobs.
  * 
  * @author Steffen Pingel
  */
 public class AttachmentJobTest extends TestCase {
 
 	private TaskRepositoryManager manager;
 
 	private MockRepositoryConnector connector;
 
 	private MockAttachmentHandler attachmentHandler;
 
 	private MockStatusHandler statusHandler;
 
 	private TaskRepository repository;
 
 	private RepositoryAttachment attachment;
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 
 		manager = TasksUiPlugin.getRepositoryManager();
 
 		repository = new TaskRepository(MockRepositoryConnector.REPOSITORY_KIND,
 				MockRepositoryConnector.REPOSITORY_URL);
 		manager.addRepository(repository, TasksUiPlugin.getDefault().getRepositoriesFilePath());
 
 		attachmentHandler = new MockAttachmentHandler();
 
 		connector = new MockRepositoryConnector();
 		connector.setAttachmentHandler(attachmentHandler);
 		manager.addRepositoryConnector(connector);
 
 		statusHandler = new MockStatusHandler();
 		MylarStatusHandler.addStatusHandler(statusHandler);
 		
 		attachment = new RepositoryAttachment(null);
 		attachment.setRepositoryKind(repository.getKind());
 		attachment.setRepositoryUrl(repository.getUrl());
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		if (statusHandler != null) {
 			MylarStatusHandler.removeStatusHandler(statusHandler);
 		}
 	}
 
 	public void testCopyToClipboardAction() throws Exception {
 		String expected = "attachment content";
 		attachmentHandler.setAttachmentData(expected.getBytes());
 
 		CopyAttachmentToClipboardJob job = new CopyAttachmentToClipboardJob(attachment);
 		job.schedule();
 		job.join();
 
		// wait for any scheduled SWT jobs to finish
		while (PlatformUI.getWorkbench().getDisplay().readAndDispatch()) {
		}

 		Clipboard clipboard = new Clipboard(PlatformUI.getWorkbench().getDisplay());
 		assertEquals(expected, clipboard.getContents(TextTransfer.getInstance()));
 	}
 
 	public void testDownloadAttachmentJob() throws Exception {
 		File file = File.createTempFile("mylyn", null);
 		file.deleteOnExit();
 
 		String expected = "attachment\ncontent";
 		attachmentHandler.setAttachmentData(expected.getBytes());
 
 		DownloadAttachmentJob job = new DownloadAttachmentJob(attachment, file);
 		job.schedule();
 		job.join();
 
 		assertEquals(Status.OK_STATUS, job.getResult());
 		statusHandler.assertNoStatus();
 		
 		RandomAccessFile raf = new RandomAccessFile(file, "r");
 		byte[] data = new byte[expected.getBytes().length];
 		try {
 			raf.readFully(data);
 		} finally {
 			raf.close();
 		}
 		assertEquals(expected, new String(data));
 	}
 }
