 /*OsmUi is a user interface for Osmosis
     Copyright (C) 2011  Verena Käfer, Peter Vollmer, Niklas Schnelle
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or 
     any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package de.osmui.util;
 
 import static org.junit.Assert.*;
 
 
 import org.junit.Test;
 
 import de.osmui.model.osm.TTask;
 import de.osmui.model.pipelinemodel.AbstractTask;
 import de.osmui.model.pipelinemodel.CommonTask;
 import de.osmui.util.exceptions.TaskNameUnknownException;
 
 /**
  * @author Niklas Schnelle, Peter Vollmer, Verena käfer
  * 
  * @see TaskManager
  * 
  */
 public class TaskManagerTest {
 	@Test public void createTask() throws TaskNameUnknownException{
 		AbstractTask task = new CommonTask("tee");
 		TaskManager manager = new TaskManager();
 		AbstractTask s = manager.createTask("tee");
 		assertEquals("Description==null",task.getName(), s.getName());
 	}
 	
 	/**
 	 * @see TaskManager#getTaskDescription(String)
 	 * 
 	 * @throws TaskNameUnknownException
 	 */
 	@Test public void getTaskDescription() throws TaskNameUnknownException{
 		TTask result = new TTask();
 		result.setName("tee");
 		TaskManager manager = new TaskManager();
 		TTask test = manager.getTaskDescription("tee");
 		assertEquals(result.getName(), test.getName());		
 	}
 	
 	/**
 	 * @see TaskManager#getTaskDescription(AbstractTask)
 	 * 
 	 * @throws TaskNameUnknownException
 	 */
 	@Test public void getTaskDescription2() throws TaskNameUnknownException{
 		AbstractTask task = new CommonTask("tee");
 		TaskManager manager = new TaskManager();
 		TTask test = manager.getTaskDescription(task);
 		assertEquals("tee", test.getName());
 	}
 	
 	@Test public void getCompatibleTasks(){
 		TaskManager manager = new TaskManager();
 		String s = "tee";
		assertEquals(24, manager.getCompatibleTasks(s).size());
 	}
 }
