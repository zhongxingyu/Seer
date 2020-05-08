 package edu.ualberta.med.biobank.barcodegenerator.views;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.eclipse.swt.custom.TableEditor;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.layout.RowLayout;
 
 import edu.ualberta.med.biobank.barcodegenerator.dialogs.StringInputDialog;
 import edu.ualberta.med.biobank.barcodegenerator.template.Template;
 import edu.ualberta.med.biobank.barcodegenerator.template.TemplateStore;
 import edu.ualberta.med.biobank.barcodegenerator.template.presets.cbsr.CBSRTemplate;
 
 public class TemplateEditorView extends ViewPart {
 
 	public static final String ID = "edu.ualberta.med.biobank.barcodegenerator.views.TemplateEditorView";
 	private Composite top = null;
 	private Group group = null;
 	private Composite composite = null;
 	private Composite composite1 = null;
 	private Composite composite2 = null;
 	private Composite composite3 = null;
 	private Group group1 = null;
 	private Composite composite4 = null;
 	private Button deleteButton = null;
 	private Button copyButton = null;
 	private Button newButton = null;
 	private Button helpButton = null;
 	private Button cancelButton = null;
 	private Button saveAllButton = null;
 	private Composite composite5 = null;
 	private Label label = null;
 	private Text templateNameText = null;
 	private Label label1 = null;
 	private Text jasperFileText = null;
 	private Button browseButton = null;
 	private List list = null;
 	private Group composite6 = null;
 	private Table configTable = null;
 	private TableEditor editor = null;
 	private Text currentTextEditor = null;
 	private TableItem currentTableItem = null;
 	private Shell shell;
 	
 	// editing the second column
 	final int EDITABLECOLUMN = 1;
 
 	private TemplateStore templateStore = new TemplateStore();
 	private Template templateSelected = null;
 
 	private void loadTemplateStore() {
 		// TODO load store from proper location
 		try {
 			templateStore.loadStore(new File("Store.dat"));
 		} catch (IOException e) {
 			templateStore = new TemplateStore();
 			Error("Preference Store Loading",
 					"Could not load the preference store. IOException ");
 		} catch (ClassNotFoundException e) {
 			templateStore = new TemplateStore();
 			Error("Preference Store Loading",
 					"SERIOUS ERROR: Could not load the preference store. Class not found!");
 		}
 	}
 
 	@Override
 	public void createPartControl(Composite parent) {
 		shell = parent.getShell();
 		loadTemplateStore();
 
 		top = new Composite(parent, SWT.NONE);
 		top.setLayout(new GridLayout());
 
 		createGroup();
 	}
 
 	@Override
 	public void setFocus() {
 	}
 
 	/**
 	 * This method initializes group
 	 * 
 	 */
 	private void createGroup() {
 		GridData gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.grabExcessVerticalSpace = true;
 		gridData.verticalAlignment = GridData.FILL;
 		group = new Group(top, SWT.NONE);
 		group.setText("Templates Editor");
 		group.setLayoutData(gridData);
 		createComposite();
 		group.setLayout(new GridLayout());
 		createComposite1();
 	}
 
 	/**
 	 * This method initializes composite
 	 * 
 	 */
 	private void createComposite() {
 		GridLayout gridLayout = new GridLayout();
 		gridLayout.numColumns = 3;
 		GridData gridData1 = new GridData();
 		gridData1.horizontalAlignment = GridData.FILL;
 		gridData1.grabExcessHorizontalSpace = true;
 		gridData1.grabExcessVerticalSpace = true;
 		gridData1.verticalAlignment = GridData.FILL;
 		composite = new Composite(group, SWT.NONE);
 		createComposite2();
 		composite.setLayoutData(gridData1);
 		composite.setLayout(gridLayout);
 		@SuppressWarnings("unused")
 		Label filler = new Label(composite, SWT.NONE);
 		createComposite3();
 	}
 
 	/**
 	 * This method initializes composite1
 	 * 
 	 */
 	private void createComposite1() {
 		GridData gridData2 = new GridData();
 		gridData2.horizontalAlignment = GridData.FILL;
 		gridData2.grabExcessHorizontalSpace = true;
 		gridData2.grabExcessVerticalSpace = false;
 		gridData2.verticalAlignment = GridData.CENTER;
 		composite1 = new Composite(group, SWT.NONE);
 		composite1.setLayoutData(gridData2);
 		composite1.setLayout(new FillLayout());
 		helpButton = new Button(composite1, SWT.NONE);
 		helpButton.setText("Help");
 		helpButton.addSelectionListener(helpListener);
 
 		@SuppressWarnings("unused")
 		Label filler22 = new Label(composite1, SWT.NONE);
 		@SuppressWarnings("unused")
 		Label filler21 = new Label(composite1, SWT.NONE);
 		@SuppressWarnings("unused")
 		Label filler2 = new Label(composite1, SWT.NONE);
 		cancelButton = new Button(composite1, SWT.NONE);
 		cancelButton.setText("Cancel");
 		cancelButton.addSelectionListener(cancelListener);
 
 		saveAllButton = new Button(composite1, SWT.NONE);
 		saveAllButton.setText("Save All ");
 		saveAllButton.addSelectionListener(saveAllListener);
 	}
 
 	/**
 	 * This method initializes composite2
 	 * 
 	 */
 	private void createComposite2() {
 		GridData gridData3 = new GridData();
 		gridData3.horizontalAlignment = GridData.BEGINNING;
 		gridData3.grabExcessVerticalSpace = true;
 		gridData3.grabExcessHorizontalSpace = false;
 		gridData3.verticalAlignment = GridData.FILL;
 		composite2 = new Composite(composite, SWT.NONE);
 		createGroup1();
 		composite2.setLayout(new GridLayout());
 		composite2.setLayoutData(gridData3);
 		createComposite4();
 	}
 
 	/**
 	 * This method initializes composite3
 	 * 
 	 */
 	private void createComposite3() {
 		GridData gridData4 = new GridData();
 		gridData4.horizontalAlignment = GridData.FILL;
 		gridData4.grabExcessHorizontalSpace = true;
 		gridData4.grabExcessVerticalSpace = true;
 		gridData4.verticalAlignment = GridData.FILL;
 		composite3 = new Composite(composite, SWT.NONE);
 		createComposite5();
 		composite3.setLayoutData(gridData4);
 		createComposite62();
 		composite3.setLayout(new GridLayout());
 	}
 
 	/**
 	 * This method initializes group1
 	 * 
 	 */
 	private void createGroup1() {
 		GridData gridData6 = new GridData();
 		gridData6.grabExcessVerticalSpace = true;
 		gridData6.verticalAlignment = GridData.FILL;
 		gridData6.grabExcessHorizontalSpace = true;
 		gridData6.horizontalAlignment = GridData.FILL;
 		FillLayout fillLayout1 = new FillLayout();
 		fillLayout1.type = org.eclipse.swt.SWT.VERTICAL;
 		group1 = new Group(composite2, SWT.NONE);
 		group1.setText("Templates");
 		group1.setLayoutData(gridData6);
 		group1.setLayout(fillLayout1);
 		list = new List(group1, SWT.BORDER | SWT.V_SCROLL);
 		list.addSelectionListener(listListener);
 		for (String s : templateStore.getTemplateNames())
 			list.add(s);
 		list.redraw();
 
 	}
 
 	private SelectionListener listListener = new SelectionListener() {
 		@Override
 		public void widgetSelected(SelectionEvent e) {
 			String[] selectedItems = list.getSelection();
 			if (selectedItems.length == 1) {
 				Template t = templateStore.getTemplate(selectedItems[0]);
 				setSelectedTemplate(t);
 			} else {
 				setSelectedTemplate(null);
 			}
 		}
 
 		@Override
 		public void widgetDefaultSelected(SelectionEvent e) {
 			widgetSelected(e);
 
 		}
 	};
 
 	private void updateJasperFileText(String selectedName) {
 
 		if (templateSelected == null)
 			return;
 
 		if (!((CBSRTemplate) templateSelected).jasperFileDataExists()) {
 			jasperFileText.setText("Select a Jasper file.");
 			jasperFileText.setBackground(new Color(shell.getDisplay(), 255, 0,
 					0));
 		} else {
 			if (selectedName == null) {
 				selectedName = "Jasper file loaded";
 			}
 			jasperFileText.setText(selectedName);
 			jasperFileText.setBackground(new Color(shell.getDisplay(), 255,
 					255, 255));
 		}
 		jasperFileText.redraw();
 	}
 
 	private void setSelectedTemplate(Template t) {
 		templateSelected = t;
 		if (t != null) {
 			templateNameText.setText(t.getName());
 
 			updateJasperFileText(null);
 
 			populateTable(configTable, ((CBSRTemplate) templateSelected)
 					.getConfiguration().getSettings());
 		} else {
 			templateNameText.setText("Select a template.");
 			jasperFileText.setText("");
 			populateTable(configTable, null);
 		}
 	}
 
 	/**
 	 * This method initializes composite4
 	 * 
 	 */
 	private void createComposite4() {
 
 		composite4 = new Composite(composite2, SWT.NONE);
 		composite4.setLayout(new RowLayout());
 		deleteButton = new Button(composite4, SWT.NONE);
 		deleteButton.setText("Delete ");
 		deleteButton.addSelectionListener(new SelectionListener() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (templateSelected != null) {
 					if (templateStore.removeTemplate(templateSelected)) {
 						list.remove(templateSelected.getName());
 
 						if (list.getItemCount() > 0) {
 							list.deselectAll();
 
 							int lastItemIndex = list.getItemCount() - 1;
 
 							list.select(lastItemIndex);
 							setSelectedTemplate(templateStore.getTemplate(list
 									.getItem(lastItemIndex)));
 
 						} else {
 							setSelectedTemplate(null);
 						}
 
 					} else {
 						Error("Template not in Template Store.",
 								"Template does not exist, already deleted.");
 					}
 				}
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 				widgetSelected(e);
 			}
 		});
 
 		copyButton = new Button(composite4, SWT.NONE);
 		copyButton.setText("Copy ");
 		copyButton.addSelectionListener(new SelectionListener() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (templateSelected != null) {
 
 					StringInputDialog dialog = new StringInputDialog(
 							"Cloned Template Name",
 							"What is the name of the cloned template?", shell,
 							SWT.NONE);
 					String cloneName = dialog.open(templateSelected.getName()
 							+ " copy");
 
 					if (cloneName != null) {
 
 						Template clone = new CBSRTemplate();
 						Template.Clone(templateSelected, clone);
 						clone.setName(cloneName);
 
 						if (templateStore.addTemplate(clone)) {
 							list.add(clone.getName());
 							list.redraw();
 						} else {
 							Error("Template Exists",
 									"Duplicate name collision. Your cloned template must have a unique name.");
 						}
 					}
 				}
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 				widgetSelected(e);
 			}
 		});
 
 		newButton = new Button(composite4, SWT.NONE);
 		newButton.setText("New");
 		newButton.addSelectionListener(new SelectionListener() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 
 				StringInputDialog dialog = new StringInputDialog(
 						"New Template Name",
 						"What is the name of this new template?", shell,
 						SWT.NONE);
 				String newTemplateName = dialog.open(null);
 
 				if (newTemplateName != null) {
 					CBSRTemplate ct = new CBSRTemplate();
 					ct.setJasperFileData(null);
 					ct.setDefaultConfiguration();
 					ct.setName(newTemplateName);
 
 					if (templateStore.addTemplate(ct)) {
 						list.add(ct.getName());
 						list.redraw();
 					} else {
 						Error("Template Exists",
 								"Your new template must have a unique name.");
 					}
 				}
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 				widgetSelected(e);
 			}
 		});
 	}
 
 	/**
 	 * This method initializes composite5
 	 * 
 	 */
 	private void createComposite5() {
 		GridData gridData11 = new GridData();
 		gridData11.horizontalAlignment = GridData.FILL;
 		gridData11.grabExcessHorizontalSpace = true;
 		gridData11.verticalAlignment = GridData.CENTER;
 		GridData gridData8 = new GridData();
 		gridData8.horizontalAlignment = GridData.FILL;
 		gridData8.grabExcessHorizontalSpace = true;
 		gridData8.verticalAlignment = GridData.CENTER;
 		GridData gridData7 = new GridData();
 		gridData7.grabExcessHorizontalSpace = true;
 		gridData7.verticalAlignment = GridData.CENTER;
 		gridData7.horizontalAlignment = GridData.FILL;
 		GridLayout gridLayout2 = new GridLayout();
 		gridLayout2.numColumns = 3;
 		composite5 = new Composite(composite3, SWT.NONE);
 		composite5.setLayout(gridLayout2);
 		composite5.setLayoutData(gridData11);
 		label = new Label(composite5, SWT.NONE);
 		label.setText("Template Name:");
 		templateNameText = new Text(composite5, SWT.BORDER);
 		templateNameText.setEditable(false);
 		templateNameText.setLayoutData(gridData7);
 		@SuppressWarnings("unused")
 		Label filler7 = new Label(composite5, SWT.NONE);
 		label1 = new Label(composite5, SWT.NONE);
 		label1.setText("Jasper File:");
 		jasperFileText = new Text(composite5, SWT.BORDER);
 		jasperFileText.setEditable(false);
 		jasperFileText.setLayoutData(gridData8);
 		browseButton = new Button(composite5, SWT.NONE);
 		browseButton.setText("Browse...");
 		browseButton.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent event) {
 				if (templateSelected == null)
 					return;
 
 				FileDialog fd = new FileDialog(shell, SWT.OPEN);
 				fd.setText("Select Jasper File");
 				String[] filterExt = { "*.jrxml" };
 				fd.setFilterExtensions(filterExt);
 				String selected = fd.open();
 				if (selected != null) {
 
 					File selectedFile = new File(selected);
 					if (!selectedFile.exists()) {
 						Error("Jasper File Non-existant",
 								"Could not find the selected Jasper file.");
 						return;
 					}
 					byte[] jasperFileData;
 					try {
 						jasperFileData = getBytesFromFile(selectedFile);
 					} catch (IOException e) {
 						Error("Loading Jasper File",
 								"Could not read the specified jasper file.\n\n"
 										+ e.getMessage());
 						return;
 					}
 					((CBSRTemplate) templateSelected)
 							.setJasperFileData(jasperFileData);
 					updateJasperFileText(selected);
 				}
 			}
 
 			public void widgetDefaultSelected(SelectionEvent event) {
 				widgetSelected(event);
 			}
 		});
 	}
 
 	// http://www.java-tips.org/java-se-tips/java.io/reading-a-file-into-a-byte-array.html
 	public static byte[] getBytesFromFile(File file) throws IOException {
 		InputStream is = new FileInputStream(file);
 
 		byte[] bytes = new byte[(int) file.length()];
 
 		int offset = 0;
 		int numRead = 0;
 		while (offset < bytes.length
 				&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
 			offset += numRead;
 		}
 
 		if (offset < bytes.length) {
 			throw new IOException("Could not completely read file "
 					+ file.getName());
 		}
 
 		is.close();
 		return bytes;
 	}
 
 	/**
 	 * This method initializes composite6
 	 * 
 	 */
 
 	/**
 	 * This method initializes composite6
 	 * 
 	 */
 	private void createComposite62() {
 		GridData gridData10 = new GridData();
 		gridData10.horizontalAlignment = GridData.FILL;
 		gridData10.grabExcessVerticalSpace = true;
 		gridData10.verticalAlignment = GridData.FILL;
 		GridData gridData9 = new GridData();
 		gridData9.grabExcessHorizontalSpace = true;
 		gridData9.verticalAlignment = GridData.FILL;
 		gridData9.grabExcessVerticalSpace = true;
 		gridData9.widthHint = -1;
 		gridData9.horizontalAlignment = GridData.FILL;
 		composite6 = new Group(composite3, SWT.NONE);
 		composite6.setLayout(new GridLayout());
 		composite6.setText("Configuration");
 		composite6.setLayoutData(gridData10);
 		createTable(composite6);
 	}
 
 	private void createTable(final Composite c) {
 		GridData gridData9 = new GridData();
 		gridData9.grabExcessHorizontalSpace = true;
 		gridData9.verticalAlignment = GridData.FILL;
 		gridData9.grabExcessVerticalSpace = true;
 		gridData9.widthHint = -1;
 		gridData9.horizontalAlignment = GridData.FILL;
 
 		configTable = new Table(c, SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
 		configTable.setHeaderVisible(true);
 		configTable.setLayoutData(gridData9);
 		configTable.setLinesVisible(true);
 
 		editor = new TableEditor(configTable);
 		// The editor must have the same size as the cell and must
 		// not be any smaller than 50 pixels.
 		editor.horizontalAlignment = SWT.LEFT;
 		editor.grabHorizontal = true;
 		editor.minimumWidth = 50;
 
 
 
 		// TODO explain the ROOT fields and width,height fields
 		configTable.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				// Clean up any previous editor control
 				Control oldEditor = editor.getEditor();
 				if (oldEditor != null)
 					oldEditor.dispose();
 
 				// Identify the selected row
 				currentTableItem = (TableItem) e.item;
 
 				if (currentTableItem == null)
 					return;
 				// The control that will be the editor must be a child of the
 				// Table
 				currentTextEditor = new Text(configTable, SWT.NONE);
 				currentTextEditor.setText(currentTableItem.getText(EDITABLECOLUMN));
 
 				currentTextEditor.addModifyListener(new ModifyListener() {
 					public void modifyText(ModifyEvent me) {
 						Text text = (Text) editor.getEditor();
 
 						editor.getItem()
 								.setText(EDITABLECOLUMN, text.getText());
 
 						// must be 4 valid numbers in the range of -1000 to
 						// 1000.
 						boolean valid = true;
 						if (currentTableItem != null
 								&& currentTableItem.getText(EDITABLECOLUMN) != null
 								&& currentTableItem.getText(EDITABLECOLUMN).split(",").length == 4)
 							for (String s : currentTableItem.getText(EDITABLECOLUMN).split(
 									",")) {
 
 								try {
 									int parsedInt = Integer.parseInt(s);
 									if (parsedInt <= -1000 || parsedInt >= 1000)
 										valid = false;
 
 								} catch (NumberFormatException e) {
 									valid = false;
 								}
 							}
 						else
 							valid = false;
 
 						if (valid)
 							currentTableItem.setForeground(new Color(shell.getDisplay(), 0,
 									0, 0));
 						else
 							currentTableItem.setForeground(new Color(shell.getDisplay(),
 									255, 0, 0));
 
 						if (templateSelected != null && valid) {
 							((CBSRTemplate) templateSelected)
 									.getConfiguration().setSettingsEntry(
 											currentTableItem.getText(0),
 											String2Rect(currentTableItem.getText(1)));
 						}
 
 					}
 				});
 
 				currentTextEditor.selectAll();
 				currentTextEditor.setFocus();
 				editor.setEditor(currentTextEditor, currentTableItem, EDITABLECOLUMN);
 			}
 		});
 
 		// FIXME make table set column width work correctly.
 		// remove this column name hack
 		String[] columnNames = {
 				"Variable                                                                              ",
 				"Value" };
 
 		TableColumn[] column = new TableColumn[2];
 		column[0] = new TableColumn(configTable, SWT.LEFT);
 		column[0].setText(columnNames[0]);
 		column[0].setWidth(200);
 
 		column[1] = new TableColumn(configTable, SWT.LEFT);
 		column[1].setText(columnNames[1]);
 
 		for (int i = 0, n = column.length; i < n; i++) {
 			column[i].pack();
 		}
 
 	}
 
 	private void sortTableColumn1() {
 		TableItem[] items = configTable.getItems();
 		for (int i = 1; i < items.length; i++) {
 			String value1 = items[i].getText(0);
 			for (int j = 0; j < i; j++) {
 				String value2 = items[j].getText(0);
 				if (value1.compareTo(value2) < 0) {
 					String[] values = { items[i].getText(0),
 							items[i].getText(1) };
 					items[i].dispose();
 					TableItem item = new TableItem(configTable, SWT.NONE, j);
 					item.setText(values);
 					items = configTable.getItems();
 					break;
 				}
 			}
 		}
 	}
 	private void populateTable(Table t, Map<String, Rectangle> data) {
 
 		
 		// remove any editor if a cell is a being modified in a previous table layout.
 		if(currentTextEditor != null)
 			currentTextEditor.dispose();
 		currentTextEditor = null;
 		editor.setEditor(null, currentTableItem, EDITABLECOLUMN);
 		
 		t.removeAll();
 
 		if (data == null) {
 			return;
 		}
 
 		for (Entry<String, Rectangle> e : data.entrySet()) {
 
 			TableItem item = new TableItem(t, SWT.NONE);
 			item.setText(new String[] { e.getKey(), rect2String(e.getValue()) });
 		}
 		sortTableColumn1();
 
 		t.redraw();
 	}
 
 	private static String rect2String(Rectangle r) {
 		return r.x + "," + r.y + "," + r.width + "," + r.height;
 	}
 
 	private static Rectangle String2Rect(String s) {
 
 		String[] parts = s.split(",");
 
 		Rectangle r = null;
 
 		if (parts.length == 4) {
 			try {
 				r = new Rectangle(Integer.parseInt(parts[0]),
 						Integer.parseInt(parts[1]), Integer.parseInt(parts[2]),
 						Integer.parseInt(parts[3]));
 			} catch (NumberFormatException nfe) {
 				throw new RuntimeException(
 						"Failed to parse integers in string to rect converstion.");
 			}
 		} else {
 			throw new RuntimeException(
 					"Invalid number of items  in string to rect converstion. ");
 		}
 		return r;
 
 	}
 
 	private void Error(String title, String message) {
 		MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
 		messageBox.setMessage(message);
 		messageBox.setText(title);
 		messageBox.open();
 	}
 
 	private SelectionListener helpListener = new SelectionListener() {
 		@Override
 		public void widgetSelected(SelectionEvent e) {
 			// TODO make help dialog.
 		}
 
 		@Override
 		public void widgetDefaultSelected(SelectionEvent e) {
 			widgetSelected(e);
 		}
 	};
 
 	private SelectionListener cancelListener = new SelectionListener() {
 		@Override
 		public void widgetSelected(SelectionEvent e) {
 			MessageBox messageBox = new MessageBox(shell, SWT.ICON_QUESTION
 					| SWT.YES | SWT.NO);
 			messageBox
 					.setMessage("Do you really want to close the template editor?");
 			messageBox.setText("Closing Template Editor");
 			int response = messageBox.open();
 			if (response == SWT.YES) {
 				// TODO close view
 			}
 		}
 
 		@Override
 		public void widgetDefaultSelected(SelectionEvent e) {
 			widgetSelected(e);
 		}
 	};
 
 	/**
 	 * Saves the entire preference store and all the changed template
 	 * information as a serialized object.
 	 */
 	private SelectionListener saveAllListener = new SelectionListener() {
 		@Override
 		public void widgetSelected(SelectionEvent e) {
 
 			// TODO save store to proper location
 			try {
 				templateStore.saveStore(new File("Store.dat"));
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			}
 
 			MessageBox messageBox = new MessageBox(shell, SWT.ICON_QUESTION
 					| SWT.YES | SWT.NO);
 			messageBox
 					.setMessage("Template information has been saved!!\n\nDo you want to close this editor?");
 			messageBox.setText("Template Editor Saving");
 			int response = messageBox.open();
 			if (response == SWT.YES) {
 				// TODO close view
 			}
 		}
 
 		@Override
 		public void widgetDefaultSelected(SelectionEvent e) {
 			widgetSelected(e);
 
 		}
 	};
 
 } // @jve:decl-index=0:visual-constraint="10,10,559,504"
