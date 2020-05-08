 /*
  * libqcw - A library for parsing, manipulating, and writing QCharts (TM) 
  * workspace files.
  *
  * Copyright (C) 2008 Jon Nall
  * QCharts is a registered service mark of eSignal, Inc.
  * RagingBull is a registered service mark of eSignal, Inc.
  *
  * Licensed under the Open Software License version 2.0 (the "License"); you
  * may not use this file except in compliance with the License. You should
  * have received a copy of the License along with this software; if not, you
  * may obtain a copy of the License at
  *
  * http://opensource.org/licenses/osl-2.0.php
  *
  * This software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
  * CONDITIONS OF ANY KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  */
 
 package org.stuntaz.qcwtool.dialogs;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.layout.TableColumnLayout;
 import org.eclipse.jface.viewers.ColumnWeightData;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Dialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Text;
 
 /**
  * Dialog for specifying symbol replacement.
  * 
  * @author nall
  *
  */
 public final class ReplaceDialog
     extends Dialog
 {
     public class MappingEntry
     {
         public final String key;
         public final String value;
 
         public MappingEntry(final String key, final String value)
         {
             this.key = key;
             this.value = value;
         }
     }
 
     private String qcwFileName;
     private boolean removeOldEntries = false;
     private final List<MappingEntry> symbolMappingList = new LinkedList<MappingEntry>();
 
     /**
      * Creates a new dialog
      * 
      * @param parent the SWT parent
      */
     public ReplaceDialog(final Shell parent)
     {
         super(parent, SWT.APPLICATION_MODAL);
     }
 
     /**
      * Gets the filename containing the workspace to modify
      * 
      * @return the name of the workspace file
      */
     public String getFilename()
     {
         return qcwFileName;
     }
 
     /**
      * Returns whether the user wants to remove old entries
      * 
      * @return true if the user wants to remove old entries.
      */
     public boolean getRemoveOldEntries()
     {
         return removeOldEntries;
     }
 
     /**
      * Opens the dialog and blocks until the user has closed it.
      * 
      * @return a map of symbol name replacements
      */
     public List<MappingEntry> open()
     {
         final Shell parent = getParent();
         final Shell shell = new Shell(parent, SWT.DIALOG_TRIM
             | SWT.APPLICATION_MODAL);
         shell.setText("Define Symbol Replacement Mapping");
 
         makeDialog(shell);
 
         shell.open();
         final Display display = parent.getDisplay();
         while (!shell.isDisposed())
         {
             if (!display.readAndDispatch()) display.sleep();
         }
 
         System.out.println("open() complete");
         System.out.println("file: " + getFilename());
         System.out.println("map entries: " + symbolMappingList.size());
         return symbolMappingList;
     }
 
     private void makeDialog(final Shell shell)
     {
         shell.setSize(550, 400);
         final GridLayout layout = new GridLayout();
         layout.numColumns = 5;
         shell.setLayout(layout);
 
         // ROW 1
         final Label l1 = new Label(shell, SWT.NONE);
         l1.setText("QCW File:");
         GridData data = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
         l1.setLayoutData(data);
 
         final Text inFileLabel = new Text(shell, SWT.SINGLE | SWT.BORDER);
         data = new GridData(SWT.FILL, SWT.NONE, true, false);
         data.horizontalSpan = 3;
         inFileLabel.setLayoutData(data);
         inFileLabel.addModifyListener(new ModifyListener()
         {
             public void modifyText(final ModifyEvent e)
             {
                 qcwFileName = inFileLabel.getText();
             }
         });
 
         final Button bFile = new Button(shell, SWT.PUSH);
         bFile.setText("Choose File...");
         data = new GridData(SWT.FILL, SWT.CENTER, false, false);
         bFile.setLayoutData(data);
         bFile.addSelectionListener(new SelectionListener()
         {
 
             public void widgetDefaultSelected(final SelectionEvent e)
             {
                 widgetSelected(e);
             }
 
             public void widgetSelected(final SelectionEvent e)
             {
                 final FileDialog fd = new FileDialog(shell, SWT.OPEN);
                 fd.setText("Open QCW Workspace File");
                 final String[] filterExt = { "*.qcw", "*.*" };
                 fd.setFilterExtensions(filterExt);
                 final String fileName = fd.open();
                 inFileLabel.setText(fileName);
                 qcwFileName = fileName;
             }
 
         });
 
         // ROW 2
         final Composite topTable = new Composite(shell, SWT.BORDER);
         topTable.setLayout(new FillLayout());
         data = new GridData(SWT.FILL, SWT.FILL, true, true);
         data.horizontalSpan = 4;
         data.verticalSpan = 3;
         data.verticalIndent = 25;
         topTable.setLayoutData(data);
 
         final Composite sComp = new Composite(topTable, SWT.NONE);
         final Table symbolTable = new Table(sComp, SWT.FULL_SELECTION
             | SWT.MULTI | SWT.BORDER);
         final TableColumnLayout tLayout = new TableColumnLayout();
         sComp.setLayout(tLayout);
 
         final TableColumn c1 = new TableColumn(symbolTable, SWT.LEFT);
         c1.setText("Current Symbol");
         tLayout.setColumnData(c1, new ColumnWeightData(50, 150));
         final TableColumn c2 = new TableColumn(symbolTable, SWT.LEFT);
         c2.setText("New Symbol");
         tLayout.setColumnData(c2, new ColumnWeightData(50, 150));
 
         symbolTable.setHeaderVisible(true);
         symbolTable.setLinesVisible(true);
         symbolTable.pack();
 
         final Button addButton = new Button(shell, SWT.PUSH);
         addButton.setText("Add Symbol Mapping...");
         data = new GridData(SWT.FILL, SWT.BOTTOM, false, false);
         data.verticalIndent = 25;
         addButton.setLayoutData(data);
         addButton.addSelectionListener(new SelectionListener()
         {
             public void widgetDefaultSelected(final SelectionEvent e)
             {
                 widgetSelected(e);
             }
 
             public void widgetSelected(final SelectionEvent e)
             {
                 final SymbolEntryDialog sed = new SymbolEntryDialog(shell);
                 final String[] pair = sed.open();
                 if (pair[0] != null)
                 {
                     final TableItem item = new TableItem(symbolTable, SWT.NONE);
                     item.setText(pair);
                 }
             }
         });
 
         // ROW 3
         final Button addFileButton = new Button(shell, SWT.PUSH);
         addFileButton.setText("Load Map File...");
         addFileButton
             .setToolTipText("A map file should consist of a series of symbol mappings: oldSymbol = newSymbol, one per line");
         data = new GridData(SWT.FILL, SWT.CENTER, false, false);
         addFileButton.setLayoutData(data);
         addFileButton.addSelectionListener(new SelectionListener()
         {
 
             public void widgetDefaultSelected(final SelectionEvent e)
             {
                 widgetSelected(e);
             }
 
             public void widgetSelected(final SelectionEvent e)
             {
                 final FileDialog fd = new FileDialog(shell, SWT.OPEN);
                 fd.setText("Open Symbol Map File");
                 final String[] filterExt = { "*.txt", "*.*" };
                 fd.setFilterExtensions(filterExt);
                 final String fileName = fd.open();
                 try
                 {
                     final BufferedReader input = new BufferedReader(
                         new FileReader(fileName));
                     String line;
                     while ((line = input.readLine()) != null)
                     {
                         final String[] kvpair = line.split("=");
                         if (kvpair.length == 2)
                         {
                             kvpair[0] = kvpair[0].trim();
                             kvpair[1] = kvpair[1].trim();
                             final TableItem item = new TableItem(symbolTable,
                                 SWT.NONE);
                             item.setText(kvpair);
                         }
                         else
                         {
                             // Ignore invalid output
                         }
                     }
                 }
                 catch (final FileNotFoundException e1)
                 {
                     MessageDialog.openError(shell, "Error Opening File",
                         "Cannot open file " + fileName + " for reading");
                 }
                 catch (final IOException e2)
                 {
                     MessageDialog.openError(shell, "Error Reading File",
                         "Error reading mapping file: " + e2.getMessage());
                 }
             }
         });
 
         // ROW 4
         final Button delButton = new Button(shell, SWT.PUSH);
         delButton.setText("Remove Selected Mapping(s)");
         data = new GridData(SWT.FILL, SWT.TOP, false, false);
         delButton.setLayoutData(data);
         delButton.addSelectionListener(new SelectionListener()
         {
 
             public void widgetDefaultSelected(final SelectionEvent e)
             {
                 widgetSelected(e);
             }
 
             public void widgetSelected(final SelectionEvent e)
             {
                 symbolTable.remove(symbolTable.getSelectionIndices());
             }
         });
 
         // ROW 5
         Composite spacer = new Composite(shell, SWT.NONE);
         data = new GridData(SWT.FILL, SWT.TOP, true, false);
         data.horizontalSpan = 4;
         data.verticalIndent = 25;
         spacer.setLayoutData(data);
 
         final Button okButton = new Button(shell, SWT.PUSH);
         okButton.setText("Replace and Save...");
         data = new GridData(SWT.FILL, SWT.BOTTOM, false, false);
         data.verticalIndent = 25;
         okButton.setLayoutData(data);
         okButton.addSelectionListener(new SelectionListener()
         {
 
             public void widgetDefaultSelected(final SelectionEvent e)
             {
                 widgetSelected(e);
             }
 
             public void widgetSelected(final SelectionEvent e)
             {
                 // Check for valid filename
                 if (getFilename() == null || getFilename().trim().length() == 0)
                 {
                     MessageDialog.openError(shell, "Invalid Filename",
                         "Please enter a valid filename");
                 }
                 else if (symbolTable.getItems().length == 0)
                 {
                     MessageDialog.openError(shell, "Enter Symbol Mappings",
                         "Please enter at least one symbol mapping");
                 }
                 else
                 {
                     // Populate map
                     for (final TableItem item : symbolTable.getItems())
                     {
                         if (item.getText(0).length() > 0)
                         {
                             MappingEntry entry = new MappingEntry(item.getText(0), item.getText(1));
                             symbolMappingList.add(entry);
                         }
                     }
 
                     System.out.println("Replace selected. Map contains "
                         + symbolMappingList.size() + "entries");
                     shell.dispose();
                 }
             }
 
         });
 
         // ROW 6
         final Button check = new Button(shell, SWT.CHECK);
         check.setText("Remove the current symbols after replacement");
         check
             .setToolTipText("If this is checked, any entries for replaced symbol will be removed from the resulting workspace");
         check.setSelection(false);
         data = new GridData(SWT.RIGHT, SWT.TOP, false, false);
         data.horizontalSpan = 2;
         check.setLayoutData(data);
         check.addSelectionListener(new SelectionListener()
         {
 
             public void widgetDefaultSelected(final SelectionEvent e)
             {
                 widgetSelected(e);
             }
 
             public void widgetSelected(final SelectionEvent e)
             {
                 removeOldEntries = ((Button) e.getSource()).getSelection();
             }
 
         });
 
         spacer = new Composite(shell, SWT.NONE);
         data = new GridData(SWT.FILL, SWT.TOP, true, false);
         data.horizontalSpan = 2;
         spacer.setLayoutData(data);
 
         final Button cancelButton = new Button(shell, SWT.PUSH);
         cancelButton.setText("Cancel");
         data = new GridData(SWT.FILL, SWT.TOP, false, false);
         cancelButton.setLayoutData(data);
         cancelButton.addSelectionListener(new SelectionListener()
         {
 
             // Just close this dialog without doing anything
             public void widgetDefaultSelected(final SelectionEvent e)
             {
                 widgetSelected(e);
             }
 
             public void widgetSelected(final SelectionEvent e)
             {
                 shell.dispose();
             }
         });
     }
 }
