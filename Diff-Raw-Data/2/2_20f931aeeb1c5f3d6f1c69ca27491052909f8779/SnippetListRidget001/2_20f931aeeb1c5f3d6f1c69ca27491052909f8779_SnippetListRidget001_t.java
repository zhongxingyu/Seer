 /*******************************************************************************
  * Copyright (c) 2007, 2011 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.sample.snippets;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import org.eclipse.core.databinding.observable.list.IObservableList;
 import org.eclipse.core.databinding.observable.list.WritableList;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 
 import org.eclipse.riena.beans.common.DayPojo;
 import org.eclipse.riena.beans.common.TypedBean;
 import org.eclipse.riena.ui.ridgets.IListRidget;
 import org.eclipse.riena.ui.ridgets.ISelectableRidget;
 import org.eclipse.riena.ui.ridgets.swt.SwtRidgetFactory;
 import org.eclipse.riena.ui.swt.utils.UIControlsFactory;
 
 /**
  * Demonstrates listening to selection changes on a list ridget.
  */
 public class SnippetListRidget001 {
 
 	public SnippetListRidget001(final Shell shell) {
 		shell.setLayout(new FillLayout());
 		final org.eclipse.swt.widgets.List list = UIControlsFactory.createList(shell, false, true);
 
 		final IListRidget listRidget = (IListRidget) SwtRidgetFactory.createRidget(list);
 		listRidget.setSelectionType(ISelectableRidget.SelectionType.SINGLE);
 		final IObservableList input = new WritableList(DayPojo.createWeek(), DayPojo.class);
 		listRidget.bindToModel(input, DayPojo.class, "english"); //$NON-NLS-1$
 		listRidget.updateFromModel();
 
		final TypedBean<DayPojo> selection = new TypedBean<DayPojo>(DayPojo.class);
 		selection.addPropertyChangeListener(new PropertyChangeListener() {
 			public void propertyChange(final PropertyChangeEvent evt) {
 				final DayPojo node = selection.getValue();
 				System.out.println("Selection: " + node.getEnglish()); //$NON-NLS-1$
 			}
 		});
 		listRidget.bindSingleSelectionToModel(selection, "value"); //$NON-NLS-1$
 	}
 
 	public static void main(final String[] args) {
 		final Display display = Display.getDefault();
 		try {
 			final Shell shell = new Shell();
 			shell.setText(SnippetListRidget001.class.getSimpleName());
 			new SnippetListRidget001(shell);
 			shell.pack();
 			shell.open();
 			while (!shell.isDisposed()) {
 				if (!display.readAndDispatch()) {
 					display.sleep();
 				}
 			}
 		} finally {
 			display.dispose();
 		}
 	}
 
 }
