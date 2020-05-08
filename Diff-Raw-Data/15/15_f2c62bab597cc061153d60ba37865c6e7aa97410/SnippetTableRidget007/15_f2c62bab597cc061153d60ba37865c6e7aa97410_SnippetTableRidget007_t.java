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
 package org.eclipse.riena.sample.snippets;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.databinding.observable.list.IObservableList;
 import org.eclipse.core.databinding.observable.list.WritableList;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 
 import org.eclipse.riena.core.marker.IMarker;
 import org.eclipse.riena.ui.core.marker.RowErrorMessageMarker;
 import org.eclipse.riena.ui.ridgets.IActionListener;
 import org.eclipse.riena.ui.ridgets.IActionRidget;
 import org.eclipse.riena.ui.ridgets.ITableRidget;
 import org.eclipse.riena.ui.ridgets.swt.SwtRidgetFactory;
 import org.eclipse.riena.ui.swt.utils.UIControlsFactory;
 
 /**
  * Demostrates how to use a {@link RowErrorMessageMarker} to mark errors in the
  * rows of a table ridget.
  */
 public class SnippetTableRidget007 {
 
 	public SnippetTableRidget007(Shell shell) {
 		Composite compTable = UIControlsFactory.createComposite(shell);
 		GridDataFactory.fillDefaults().grab(true, true).applyTo(compTable);
		final Table table = UIControlsFactory.createTable(compTable, SWT.SINGLE | SWT.FULL_SELECTION);
 
 		Composite compButton = UIControlsFactory.createComposite(shell);
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(compButton);
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(compButton);
 		Button btnMark = UIControlsFactory.createButton(compButton, "&Mark"); //$NON-NLS-1$
 		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.FILL).grab(true, false).applyTo(btnMark);
 		Button btnUnmark = UIControlsFactory.createButton(compButton, "&Unmark"); //$NON-NLS-1$
		Button btnLines = UIControlsFactory.createButton(compButton, "Toggle &Lines"); //$NON-NLS-1$
 
 		final ITableRidget tableRidget = (ITableRidget) SwtRidgetFactory.createRidget(table);
 		String[] columnPropertyNames = { "pseudonym", "name", "appearance" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
 		String[] columnHeaders = { "Pseudonym", "Name", "First appearance" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
 		IObservableList input = new WritableList(createInput(), SuperHero.class);
 		tableRidget.bindToModel(input, SuperHero.class, columnPropertyNames, columnHeaders);
 		tableRidget.updateFromModel();
 		tableRidget.setToolTipText("The ORIGINAL table tooltip!"); //$NON-NLS-1$
 
 		IActionRidget ridgetMark = (IActionRidget) SwtRidgetFactory.createRidget(btnMark);
 		ridgetMark.addListener(new IActionListener() {
 			public void callback() {
 				List<Object> selection = tableRidget.getSelection();
 				if (!selection.isEmpty()) {
 					Object value = selection.get(0);
 					System.out.println("mark: " + value); //$NON-NLS-1$
 					IMarker marker = new RowErrorMessageMarker("There is an error with: " + value, value); //$NON-NLS-1$
 					tableRidget.addMarker(marker);
 				}
 			}
 		});
 
 		IActionRidget ridgetUnmark = (IActionRidget) SwtRidgetFactory.createRidget(btnUnmark);
 		ridgetUnmark.addListener(new IActionListener() {
 			public void callback() {
 				List<Object> selection = tableRidget.getSelection();
 				if (!selection.isEmpty()) {
 					Object value = selection.get(0);
 					System.out.println("unmark: " + value); //$NON-NLS-1$
 					IMarker marker = new RowErrorMessageMarker(null, value);
 					tableRidget.removeMarker(marker);
 				}
 			}
 		});

		IActionRidget ridgetLines = (IActionRidget) SwtRidgetFactory.createRidget(btnLines);
		ridgetLines.addListener(new IActionListener() {
			public void callback() {
				table.setLinesVisible(!table.getLinesVisible());
			}
		});
 	}
 
 	public static void main(String[] args) {
 		Display display = Display.getDefault();
 		try {
 			Shell shell = UIControlsFactory.createShell(display);
 			shell.setText(SnippetTableRidget007.class.getSimpleName());
 			GridLayoutFactory.fillDefaults().applyTo(shell);
 
 			new SnippetTableRidget007(shell);
 			shell.setSize(400, 250);
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
 
 	// helping methods
 	//////////////////
 
 	private List<SuperHero> createInput() {
 		List<SuperHero> nodes = new ArrayList<SuperHero>(7);
 		nodes.add(new SuperHero("Superman", "Clark Kent", 1938)); //$NON-NLS-1$ //$NON-NLS-2$
 		nodes.add(new SuperHero("Batman", "Bruce Wayne", 1939)); //$NON-NLS-1$ //$NON-NLS-2$
 		nodes.add(new SuperHero("Zorro", "Don Diego de la Vega", 1919)); //$NON-NLS-1$ //$NON-NLS-2$
 		nodes.add(new SuperHero("Flash Gordon", "Gordon Ferrao", 1934)); //$NON-NLS-1$ //$NON-NLS-2$
 		nodes.add(new SuperHero("Hulk", "Bruce Banner", 1962)); //$NON-NLS-1$ //$NON-NLS-2$
 		nodes.add(new SuperHero("Spider-Man", "Peter Parker", 1962)); //$NON-NLS-1$ //$NON-NLS-2$
 		nodes.add(new SuperHero("Silver Surfer", "Norrin Radd", 1966)); //$NON-NLS-1$ //$NON-NLS-2$
 
 		return nodes;
 	}
 
 	private static final class SuperHero {
 		private String pseudonym;
 		private String name;
 		private Integer appearance;
 
 		public SuperHero(String pseudonym, String name, int appearance) {
 			this.pseudonym = pseudonym;
 			this.name = name;
 			this.appearance = appearance;
 		}
 
 		@SuppressWarnings("unused")
 		public String getPseudonym() {
 			return pseudonym;
 		}
 
 		@SuppressWarnings("unused")
 		public String getName() {
 			return name;
 		}
 
 		@SuppressWarnings("unused")
 		public Integer getAppearance() {
 			return appearance;
 		}
 
 		@Override
 		public String toString() {
 			return String.format("%s (%s)", pseudonym, name); //$NON-NLS-1$
 		}
 	}
 }
