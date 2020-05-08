 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.client.controller.test;
 
 import org.eclipse.riena.example.client.controllers.TableSubModuleController;
 import org.eclipse.riena.internal.core.test.collect.NonUITestCase;
 import org.eclipse.riena.navigation.ISubModuleNode;
 import org.eclipse.riena.navigation.ui.swt.controllers.AbstractSubModuleControllerTest;
 import org.eclipse.riena.ui.ridgets.IActionRidget;
 import org.eclipse.riena.ui.ridgets.ITableRidget;
 
 /**
  * Tests for the TableSubModuleController.
  */
 @NonUITestCase
 public class TableSubModuleControllerTest extends AbstractSubModuleControllerTest<TableSubModuleController> {
 
 	@Override
 	protected TableSubModuleController createController(ISubModuleNode node) {
 		return new TableSubModuleController(node);
 	}
 
 	public void testButtonDelete() {

 		ITableRidget table = getController().getRidget(ITableRidget.class, "table"); //$NON-NLS-1$
 		int count = table.getOptionCount();
 
 		IActionRidget buttonDelete = getController().getRidget(IActionRidget.class, "buttonDelete"); //$NON-NLS-1$
 		// select and delete one item
		table.setSelection(0);
 
		// FIXME wait for 
		// 298033: Make row selection in TableRidget independent of the UIControl https://bugs.eclipse.org/bugs/show_bug.cgi?id=298033
		// assertEquals(0, table.getSelectionIndex());
 		assertEquals("[Adventure]", table.getSelection().toString());
 
 		buttonDelete.fireAction();
 		assertEquals(count - 1, table.getOptionCount());
 
		// FIXME wait for 
		// 298033: Make row selection in TableRidget independent of the UIControl https://bugs.eclipse.org/bugs/show_bug.cgi?id=298033
		//assertTrue(table.getSelection().isEmpty());
		//assertFalse(buttonDelete.isEnabled());
 
 		table.setSelection(0);
 
 		table.clearSelection();
 		assertEquals(-1, table.getSelectionIndex());
 		assertTrue(table.getSelection().isEmpty());
 
 		assertFalse(buttonDelete.isEnabled());
 
 		// nothing selected -> nothing deleted
 		buttonDelete.fireAction();
 		assertEquals(count - 1, table.getOptionCount());
 
 		// select and delete another item
 		table.setSelection(0);
 		assertFalse(table.getSelection().isEmpty());
 		buttonDelete.fireAction();
 		assertEquals(count - 2, table.getOptionCount());
 	}
 
 	public void testAddButton() {
 		ITableRidget table = getController().getRidget(ITableRidget.class, "table"); //$NON-NLS-1$
 		IActionRidget buttonAdd = getController().getRidget(IActionRidget.class, "buttonAddSibling"); //$NON-NLS-1$
 		int count = table.getOptionCount();
 
 		buttonAdd.fireAction();
 		assertEquals(count + 1, table.getOptionCount());
 		assertEquals("A_NEW_SIBLING", table.getOption(count).toString());
 
 		table.clearSelection();
 		assertTrue(buttonAdd.isEnabled());
 	}
 }
