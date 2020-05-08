 /////////////////////////////////////////////////////////////////////////
 //
 // © University of Southampton IT Innovation Centre, 2011
 //
 // Copyright in this library belongs to the University of Southampton
 // University Road, Highfield, Southampton, UK, SO17 1BJ
 //
 // This software may not be used, sold, licensed, transferred, copied
 // or reproduced in whole or in part in any manner or form or in or
 // on any media by any person other than in accordance with the terms
 // of the Licence Agreement supplied with the software, or otherwise
 // without the prior written consent of the copyright owners.
 //
 // This software is distributed WITHOUT ANY WARRANTY, without even the
 // implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 // PURPOSE, except where stated in the Licence Agreement supplied with
 // the software.
 //
 //	Created By :			Thomas Leonard
 //	Created Date :			2011-08-17
 //	Created for Project :		SERSCIS
 //
 /////////////////////////////////////////////////////////////////////////
 //
 //  License : GNU Lesser General Public License, version 2.1
 //
 /////////////////////////////////////////////////////////////////////////
 
 package eu.serscis.sam.gui;
 
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.storage.IRelation;
import org.deri.iris.api.basics.IQuery;
 import org.deri.iris.api.basics.ITuple;
 import org.deri.iris.api.basics.IPredicate;
 import java.util.Arrays;
 import org.eclipse.swt.program.Program;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import eu.serscis.sam.Graph;
 import eu.serscis.sam.Eval;
 import eu.serscis.sam.Results;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.File;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.*;
 import org.eclipse.swt.layout.RowLayout;
import static org.deri.iris.factory.Factory.*;
 
 public class GUI {
 	private File myFile;
 	private Results results;
 	private Display display;
 	private Shell shell;
 	private Label mainImage;
 	private Color white;
 	private MenuItem relationsMenuHeader;
 	private Menu relationsMenu;
 	private List messageList;
 
 	public GUI(File file) throws Exception {
 		myFile = file;
 
 		display = new Display();
 		white = new Color(display, 255, 255, 255);
 		shell = new Shell(display, SWT.RESIZE);
 		shell.setText("SAM");
 
 		Menu menuBar = new Menu(shell, SWT.BAR);
 		shell.setMenuBar(menuBar);
 
 		MenuItem fileMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
 		fileMenuHeader.setText("&File");
 
 		Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
 		fileMenuHeader.setMenu(fileMenu);
 
 		MenuItem openItem = new MenuItem(fileMenu, SWT.PUSH);
 		openItem.setText("&Open...");
 		openItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				FileDialog dialog = new FileDialog(shell);
 				dialog.setFilterExtensions(new String[] {"*.sam", "*"});
 				if (myFile != null) {
 					dialog.setFileName(myFile.getPath());
 				}
 				String path = dialog.open();
 				if (path != null) {
 					myFile = new File(path);
 					evaluate();
 				}
 			}
 		});
 
 		MenuItem examplesItem = new MenuItem(fileMenu, SWT.CASCADE);
 		examplesItem.setText("Open example");
 		examplesItem.setMenu(makeExamplesMenu(openItem));
 
 		MenuItem reloadItem = new MenuItem(fileMenu, SWT.PUSH);
 		reloadItem.setText("&Reload\tF5");
 		reloadItem.setAccelerator(SWT.F5);
 		reloadItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				System.out.println("Reloading...");
 				evaluate();
 				System.out.println("Reloaded");
 			}
 		});
 
 		MenuItem quitItem = new MenuItem(fileMenu, SWT.PUSH);
 		quitItem.setText("&Quit");
 		quitItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				System.exit(1);
 			}
 		});
 
 		relationsMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
 		relationsMenuHeader.setText("&Relations");
 		relationsMenu = new Menu(shell, SWT.DROP_DOWN);
 		relationsMenuHeader.setMenu(relationsMenu);
 
 		MenuItem helpMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
 		helpMenuHeader.setText("&Help");
 		Menu helpMenu = new Menu(shell, SWT.DROP_DOWN);
 		helpMenuHeader.setMenu(helpMenu);
 
 		MenuItem userGuide = new MenuItem(helpMenu, 0);
 		userGuide.setText("User Guide");
 		userGuide.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				Program.launch("http://www.serscis.eu/sam/");
 			}
 		});
 
 		MenuItem about = new MenuItem(helpMenu, 0);
 		about.setText("About");
 		about.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				MessageBox box = new MessageBox(shell, SWT.OK);
 				box.setText("About");
 				box.setMessage("SERSCIS Access Modeller\n© University of Southampton IT Innovation Centre, 2011");
 				box.open();
 			}
 		});
 
 		mainImage = new Label(shell, SWT.CENTER);
 		mainImage.setBackground(white);
 
 		messageList = new List(shell, 0);
 
 		GridLayout gridLayout = new GridLayout();
  		gridLayout.numColumns = 1;
 		gridLayout.marginHeight = 0;
 		gridLayout.marginWidth = 0;
 		gridLayout.verticalSpacing = 0;
 
 		GridData imageLayoutData = new GridData();
 		imageLayoutData.horizontalAlignment = GridData.FILL;
 		imageLayoutData.verticalAlignment = GridData.FILL;
 		imageLayoutData.grabExcessHorizontalSpace = true;
 		imageLayoutData.grabExcessVerticalSpace = true;
 		mainImage.setLayoutData(imageLayoutData);
 
 		GridData assertionsLayoutData = new GridData();
 		assertionsLayoutData.horizontalAlignment = GridData.FILL;
 		assertionsLayoutData.verticalAlignment = GridData.FILL;
 		assertionsLayoutData.grabExcessHorizontalSpace = true;
 		assertionsLayoutData.grabExcessVerticalSpace = false;
 		messageList.setLayoutData(assertionsLayoutData);
 
  		shell.setLayout(gridLayout);
 
 		evaluate();
 
 		shell.open();
 
 		while (!shell.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 		display.dispose();
 	}
 
 	private Menu makeExamplesMenu(MenuItem parent) {
 		Menu examplesMenu = new Menu(parent);
 
 		final File examplesDir = new File(System.getenv("SAM_EXAMPLES"));
 
 		String[] files = examplesDir.list();
 		Arrays.sort(files);
 		for (final String name : files) {
 			if (name.endsWith(".sam") && !name.endsWith("-common.sam")) {
 				MenuItem item = new MenuItem(examplesMenu, SWT.PUSH);
 				item.setText(name);
 				item.addSelectionListener(new SelectionAdapter() {
 					public void widgetSelected(SelectionEvent e) {
 						myFile = new File(examplesDir, name);
 						evaluate();
 					}
 				});
 			}
 		}
 
 		return examplesMenu;
 	}
 
 	private void evaluate() {
 		try {
 			relationsMenuHeader.setEnabled(false);
 			messageList.removeAll();
 
 			for (MenuItem item : relationsMenu.getItems()) {
 				item.dispose();
 			}
 
 			if (myFile == null) {
 				addInfo("Open a file to start");
 				return;
 			}
 
 			Eval eval = new Eval();
 			results = eval.evaluate(myFile);
 
 			if (results.exception != null) {
 				results.exception.printStackTrace();
 				String msg = results.exception.getMessage();
 				if (msg == null) {
 					msg = results.exception.toString();
 				}
 				addWarning(msg);
 			} else if (results.phase != Results.Phase.Success) {
 				addWarning("Problem in " + results.phase + " phase");
 			} else {
 				addInfo("OK");
 			}
 
 			for (String msg : results.errors) {
 				addWarning(msg);
 			}
 
 			if (results.finalKnowledgeBase != null) {
 				File tmpFile = File.createTempFile("sam-", "-graph.png");
 				Image image;
 				try {
 					Graph.graph(results.finalKnowledgeBase, tmpFile);
 					InputStream is = new FileInputStream(tmpFile);
 					try {
 						image = new Image(display, is);
 					} finally {
 						is.close();
 					}
 				} finally {
 					tmpFile.delete();
 				}
 
 				mainImage.setImage(image);
 				mainImage.pack();
 
 				relationsMenuHeader.setEnabled(true);
 
 				IPredicate[] relations = results.model.declared.keySet().toArray(new IPredicate[] {});
 				Arrays.sort(relations);
 				for (final IPredicate pred : relations) {
 					MenuItem item = new MenuItem(relationsMenu, SWT.PUSH);
 					final ITuple args = results.model.declared.get(pred);
 					item.setText(pred.toString() + args);
 					item.addSelectionListener(new SelectionAdapter() {
 						public void widgetSelected(SelectionEvent e) {
 							try {
								ILiteral lit = BASIC.createLiteral(true, pred, args);
								IQuery query = BASIC.createQuery(lit);
								IRelation rel = results.finalKnowledgeBase.execute(query);
								new RelationViewer(shell, rel, pred, args);
 							} catch (Throwable ex) {
 								ex.printStackTrace();
 							}
 						}
 					});
 				}
 			}
 
 			messageList.pack();
 		} catch (Exception ex) {
 			throw new RuntimeException(ex);
 		}
 
 		shell.layout();
 	}
 
 	private void addWarning(String msg) {
 		messageList.add(msg);
 	}
 
 	private void addInfo(String msg) {
 		messageList.add(msg);
 	}
 }
