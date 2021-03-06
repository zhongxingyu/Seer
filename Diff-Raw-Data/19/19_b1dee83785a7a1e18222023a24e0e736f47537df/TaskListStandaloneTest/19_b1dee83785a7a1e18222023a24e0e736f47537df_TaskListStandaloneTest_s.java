 /*******************************************************************************
  * Copyright (c) 2004 - 2005 University Of British Columbia and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     University Of British Columbia - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.mylar.tasklist.tests;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.eclipse.mylar.tasklist.ITask;
 import org.eclipse.mylar.tasklist.ITaskListExternalizer;
 import org.eclipse.mylar.tasklist.MylarTaskListPlugin;
 import org.eclipse.mylar.tasklist.internal.Task;
 import org.eclipse.mylar.tasklist.internal.TaskList;
 import org.eclipse.mylar.tasklist.internal.TaskListManager;
 import org.eclipse.mylar.tasklist.internal.TaskListWriter;
 
 /**
  * Can be run without workbench
  * 
  * @author Mik Kersten
  */
 public class TaskListStandaloneTest extends TestCase {
 
 	private TaskListManager manager;
 	
 	private File file;
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		List<ITaskListExternalizer> externalizers = new ArrayList<ITaskListExternalizer>();
 		
 		// bugzilla externalizer requires workbench
 //		externalizers.add(new BugzillaTaskExternalizer());
 		
 		TaskListWriter writer = new TaskListWriter();
 		writer.setDelegateExternalizers(externalizers);
 		
 		file = new File("foo" + MylarTaskListPlugin.FILE_EXTENSION);
 		file.deleteOnExit();
 		manager = new TaskListManager(writer, file, 1);
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 
 	public void testDates() {
 		Date start = Calendar.getInstance().getTime();
 		Task task = new Task("1", "task 1", true);
 		
 		manager.moveToRoot(task);
 		assertDatesCloseEnough(task.getCreationDate(), start);
 		
 		task.setCompleted(true);
 		assertDatesCloseEnough(task.getCompletionDate(), start);
 				
 		task.setReminderDate(start);
 		assertDatesCloseEnough(task.getReminderDate(), start);
 		
 		assertEquals(manager.getTaskList().getRoots().size(), 1);
 		manager.saveTaskList();
 		
 		assertNotNull(manager.getTaskList());
 		TaskList list = new TaskList();
 		manager.setTaskList(list);
 		assertEquals(0, manager.getTaskList().getRootTasks().size());
 		manager.readTaskList();
 		
 		assertNotNull(manager.getTaskList());
		System.err.println(">>> " + manager.getTaskList().getCategories());
 		assertEquals(1, manager.getTaskList().getRootTasks().size());
 
 		List<ITask> readList = manager.getTaskList().getRootTasks();
 		ITask readTask = readList.get(0);
 		assertTrue(readTask.getDescription(true).equals("task 1"));
 		
 		assertEquals(task.getCreationDate(), readTask.getCreationDate());
 		assertEquals(task.getCompletionDate(), readTask.getCompletionDate());
 		assertEquals(task.getReminderDate(), readTask.getReminderDate());
 	}
 	
 	public void assertDatesCloseEnough(Date first, Date second) {
 		assertTrue(second.getTime() - first.getTime() < 100);
 	}
 }
