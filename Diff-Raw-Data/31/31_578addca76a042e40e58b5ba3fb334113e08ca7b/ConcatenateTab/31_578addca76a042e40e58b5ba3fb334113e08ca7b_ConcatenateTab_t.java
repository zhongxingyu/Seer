 package net.sapium.livestreamprocessor.gui;
 
 import java.io.File;
 import java.util.LinkedList;
 
 import net.sapium.livestreamprocessor.utils.Concatenator;
 import net.sapium.livestreamprocessor.utils.ProgressChangedListener;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.DirectoryDialog;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeItem;
 
 public class ConcatenateTab extends TabContent {
     public Text folderTextBox;
     public Button folderButton;
     public Text outputTextBox;
     public Button outputButton;
     public Tree tree;
     private TreeItem itemBeingDragged;
 
     public ConcatenateTab(Composite arg0, int arg1) {
         super(arg0, arg1);
     }
 
     @Override
     protected void createContents() {
         this.setLayout(new FormLayout());
 
         // Source folder row
         Label folderLabel = new Label(this, SWT.NONE);
         FormData fd_folderLabel = new FormData();
         fd_folderLabel.top = new FormAttachment(0, 13);
         fd_folderLabel.left = new FormAttachment(0, 10);
         folderLabel.setLayoutData(fd_folderLabel);
         folderLabel.setText("Source folder: ");
 
         folderTextBox = new Text(this, SWT.BORDER);
         FormData fd_folderTextBox = new FormData();
         fd_folderTextBox.left = new FormAttachment(folderLabel, 6);
         fd_folderTextBox.top = new FormAttachment(folderLabel, -3, SWT.TOP);
         folderTextBox.setLayoutData(fd_folderTextBox);
 
         folderButton = new Button(this, SWT.NONE);
         fd_folderTextBox.right = new FormAttachment(folderButton, -6);
         folderButton.setText("Browse");
         FormData fd_folderButton = new FormData();
         fd_folderButton.right = new FormAttachment(100, -10);
         fd_folderButton.top = new FormAttachment(folderLabel, -5, SWT.TOP);
         folderButton.setLayoutData(fd_folderButton);
 
         // Output file row
         Label outputLabel = new Label(this, SWT.NONE);
         FormData fd_outputLabel = new FormData();
         fd_outputLabel.top = new FormAttachment(folderLabel, 13);
         fd_outputLabel.left = new FormAttachment(0, 10);
         outputLabel.setLayoutData(fd_outputLabel);
         outputLabel.setText("Output file: ");
 
         outputTextBox = new Text(this, SWT.BORDER);
         FormData fd_outputTextBox = new FormData();
         fd_outputTextBox.left = new FormAttachment(folderTextBox, 0, SWT.LEFT);
         fd_outputTextBox.top = new FormAttachment(outputLabel, -3, SWT.TOP);
         outputTextBox.setLayoutData(fd_outputTextBox);
 
         outputButton = new Button(this, SWT.NONE);
         fd_outputTextBox.right = new FormAttachment(outputButton, -6);
         outputButton.setText("Browse");
         FormData fd_outputButton = new FormData();
         fd_outputButton.right = new FormAttachment(100, -10);
         fd_outputButton.top = new FormAttachment(outputLabel, -5, SWT.TOP);
         outputButton.setLayoutData(fd_outputButton);
 
         tree = new Tree(this, SWT.BORDER | SWT.CHECK | SWT.SINGLE);
         FormData fd_table = new FormData();
         fd_table.top = new FormAttachment(outputTextBox, 6);
         fd_table.left = new FormAttachment(0, 10);
         fd_table.right = new FormAttachment(100, -10);
         fd_table.bottom = new FormAttachment(100, -10);
         tree.setLayoutData(fd_table);
         tree.setHeaderVisible(false);
         tree.setLinesVisible(true);
     }
 
     @Override
     protected void addListeners() {
         folderTextBox.addModifyListener(new ModifyListener(){
             @Override
             public void modifyText(ModifyEvent e) {
                 String path = folderTextBox.getText();
                 File folder = new File(path);
                 if(folder.exists() && folder.isDirectory()){
                     String[] names = folder.list();
                     for (int i = 0; i < names.length; i++) {
                         TreeItem item = new TreeItem(tree, SWT.NONE);
                         item.setText(names[i]);
                     }
                 }else{
                     tree.removeAll();
                 }
             }
         });
         
         final DirectoryDialog folderDialog = new DirectoryDialog((Shell) this.getParent().getParent(), SWT.OPEN);
         folderDialog.setMessage("Choose a video source folder");
 
         folderButton.addSelectionListener(new SelectionListener() {
             public void widgetSelected(SelectionEvent e) {
                 String result = folderDialog.open();
                 if (result != null) {
                     folderTextBox.setText(result);
                 }
             }
 
             public void widgetDefaultSelected(SelectionEvent e) {
             }
         });
 
         final FileDialog outputDialog = new FileDialog((Shell) this.getParent().getParent(), SWT.SAVE);
        outputDialog.setFilterNames(filterNames);
        outputDialog.setFilterExtensions(filterExtensions);
         outputDialog.setOverwrite(true);
 
         outputButton.addSelectionListener(new SelectionListener() {
             public void widgetSelected(SelectionEvent e) {
                 String result = outputDialog.open();
                 if (result != null) {
                     outputTextBox.setText(result);
                 }
             }
 
             public void widgetDefaultSelected(SelectionEvent e) {
             }
         });
 
         tree.addListener(SWT.DragDetect, new Listener() {
             public void handleEvent(Event event) {
                 TreeItem item = tree.getItem(new Point(event.x, event.y));
                 if (item == null)
                     return;
                 itemBeingDragged = item;
             }
         });
 
         tree.addListener(SWT.MouseMove, new Listener() {
             public void handleEvent(Event event) {
                 if (itemBeingDragged == null)
                     return;
 
                 TreeItem item = tree.getItem(new Point(25, event.y));
                 if (item == null) {
                     tree.setInsertMark(tree.getItem(tree.getItemCount() - 1), false);
                 } else {
                     tree.setInsertMark(item, true);
                 }
             }
         });
 
         tree.addListener(SWT.MouseUp, new Listener() {
             public void handleEvent(Event event) {
                 if (itemBeingDragged == null)
                     return;
                 TreeItem item = tree.getItem(new Point(25, event.y));
                 if (item != itemBeingDragged) {
                     /* determine insertion index */
                     TreeItem[] items = tree.getItems();
                     int index = -1;
                     if (item == null) {
                         index = items.length;
                     } else {
                         for (int i = 0; i < items.length; i++) {
                             if (items[i] == item) {
                                 index = i;
                                 break;
                             }
                         }
                     }
                     if (index != -1) { /* always true in this trivial example */
                         TreeItem newItem = new TreeItem(tree, SWT.NONE, index);
                         newItem.setText(itemBeingDragged.getText());
                         newItem.setChecked(itemBeingDragged.getChecked());
                         itemBeingDragged.dispose();
                         tree.setSelection(new TreeItem[] { newItem });
                     }
                 }
                 tree.setInsertMark(null, false);
                 itemBeingDragged = null;
             }
         });
     }
     
     @Override
     protected void start(ProgressChangedListener listener) {
         if (!outputTextBox.getText().equals("") && !folderTextBox.getText().equals("")) {
             File outputFile = new File(outputTextBox.getText());
 
             if (!outputFile.exists()) {
                 TreeItem[] items = tree.getItems();
                 LinkedList<TreeItem> checkedItems = new LinkedList<TreeItem>();
                 for(int i=0; i<items.length; i++){
                     if(items[i].getChecked()){
                         checkedItems.add(items[i]);
                     }
                 }
                 int size = checkedItems.size();
                 if (size > 0) {
                     File[] fileList = new File[size];
                     for (int i = 0; i < size; i++) {
                         fileList[i] = new File(new File(folderTextBox.getText()).getAbsolutePath() + "\\" + checkedItems.get(i).getText());
                     }
                     
                     processor = new Concatenator(listener, fileList, outputFile.getAbsolutePath());
                     Thread concatenateThread = new Thread(processor);
                     concatenateThread.start();
                 }
             }
         }
     }
 }
