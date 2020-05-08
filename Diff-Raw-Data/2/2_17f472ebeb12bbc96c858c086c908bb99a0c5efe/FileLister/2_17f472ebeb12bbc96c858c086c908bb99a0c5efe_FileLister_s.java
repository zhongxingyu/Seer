 /*
  * Jvifm - Java vifm (File Manager with vi like key binding)
  *
  * Copyright (C) 2006 wsn <shrek.wang@gmail.com>
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  *
  */
 
 package net.sf.jvifm.ui;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import net.sf.jvifm.CommandBuffer;
 import net.sf.jvifm.Main;
 import net.sf.jvifm.ResourceManager;
 import net.sf.jvifm.control.Command;
 import net.sf.jvifm.control.CommandRunner;
 import net.sf.jvifm.control.CopyCommand;
 import net.sf.jvifm.control.MoveCommand;
 import net.sf.jvifm.control.RemoveCommand;
 import net.sf.jvifm.control.UnCompressCommand;
 import net.sf.jvifm.model.FileListerListener;
 import net.sf.jvifm.model.FileModelListener;
 import net.sf.jvifm.model.FileModelManager;
 import net.sf.jvifm.model.HistoryManager;
 import net.sf.jvifm.model.MimeManager;
 import net.sf.jvifm.model.Shortcut;
 import net.sf.jvifm.model.ShortcutsManager;
 import net.sf.jvifm.model.filter.WildcardFilter2;
 import net.sf.jvifm.ui.factory.GuiDataFactory;
 import net.sf.jvifm.util.FileComprator;
 import net.sf.jvifm.util.StringUtil;
 import net.sf.jvifm.util.ZipUtil;
 
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.io.filefilter.AndFileFilter;
 import org.apache.commons.io.filefilter.IOFileFilter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyleRange;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.custom.TableCursor;
 import org.eclipse.swt.custom.TableEditor;
 import org.eclipse.swt.dnd.Clipboard;
 import org.eclipse.swt.dnd.FileTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.FocusAdapter;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.TraverseEvent;
 import org.eclipse.swt.events.TraverseListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Text;
 
 
 public class FileLister implements ViLister, Panel , FileModelListener {
 	
 	public static final String FS_ROOT = ""; //$NON-NLS-1$
 	public static final String ADD_ITEM = "ADD_ITEM"; //$NON-NLS-1$
 	public static final String REMOVE_ITEM = "REMOVE_ITEM"; //$NON-NLS-1$
 
 	private CommandRunner commandRunner = CommandRunner.getInstance();
 	private static String ENV_OS = System.getProperty("os.name"); //$NON-NLS-1$
 	
 
 	private Table table;
 	private Composite container;
 
 	private Composite mainArea;
 
 	private TableCursor cursor;
 	private TableEditor editor;
 	private StyledText textLocation;
 	private Label lblStatus;
 	private Button btnUpDir;
 	private Button btnTopDir;
 	private TableColumn columnName;
 	private TableColumn columnSize;
 	private TableColumn columnDate;
 	private Image folderImage = null;
 	private Image fileImage = null;
 	private Image driveImage = null;
 	private FileManager fileManager = null;
 	private String commandBuffer = null;
 
 	private String pos = null;
 	private String pwd;
 	private boolean showDetail = true;
 	private HistoryManager historyManager = new HistoryManager();
 	private FileModelManager fileModelManager=FileModelManager.getInstance();
 	private int sizeWidth;
 	private int dateWidth;
 
 	private String filterString = null;
 	private String searchString = null;
 	private String countString = null;
 	private String sortColumn = "name"; //$NON-NLS-1$
 	private boolean isReverse = false;
 
 	private Mode operateMode = Mode.NORMAL;
 	private int currentRow = 0;
 	private int origRow = 0;
 
     private File[] tableContentFileArray = null;
 	private TreeSet<File> tableContentFiles = null;
     private boolean winRoot = false;
     private String selectedName = "";
     private boolean hasMatchSelectedName = false;
 	
 	private ArrayList<FileListerListener> listeners = new ArrayList<FileListerListener>();
 	private Color tableDefaultBackground = null;
 	
 	private boolean syncWithFileTree = false;
 
 	public Control getControl() {
 		return container;
 	}
 
 	public FileLister(Composite parent, int style, String pwd, String pos1) {
 		fileManager = Main.fileManager;
 		this.pos = pos1;
 		initResource();
 
 		parent.setLayout(new FillLayout());
 
 		container = new Composite(parent, SWT.NONE);
 		GridLayout layout=GuiDataFactory.createkGridLayout(1, 0, 0, 0, 0, true);
 		container.setLayout(layout);
 
 		mainArea = new Composite(container, SWT.NONE);
 		layout = GuiDataFactory.createkGridLayout(1, 0, 0, 0, 0, true);
 		mainArea.setLayout(layout);
 		
 		mainArea.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		initMainArea();
 		
 		initListener();
 		lblStatus = new Label(container, SWT.BORDER);
 		lblStatus.setLayoutData( new GridData(GridData.FILL_HORIZONTAL));
 
 		if (pwd == null || pwd.trim().equals("")) {
 			pwd=FS_ROOT;
 		}
 		this.pwd = pwd;
 		fileModelManager.addListener(this);
 		visit(pwd);
 	}
 
 	private void initMainArea() {
 		Composite headGroup = new Composite(mainArea, SWT.NONE);
 		headGroup.setLayoutData( new GridData(GridData.FILL_HORIZONTAL));
 
 		GridLayout layout = GuiDataFactory.createkGridLayout(3, 0, 0, 0, 0, false);
 		headGroup.setLayout(layout);
 
 		btnUpDir = new Button(headGroup, SWT.PUSH);
 		btnUpDir.setText(".."); //$NON-NLS-1$
 		btnUpDir.setLayoutData(new GridData());
 
 		btnTopDir = new Button(headGroup, SWT.PUSH);
 		btnTopDir.setText("/"); //$NON-NLS-1$
 		btnTopDir.setLayoutData(new GridData());
 
 		textLocation = new StyledText(headGroup, SWT.SINGLE | SWT.BORDER);
 		textLocation.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 		table = new Table(mainArea, SWT.MULTI  | SWT.FULL_SELECTION | SWT.VIRTUAL);
 		table.setLayoutData(new GridData(GridData.FILL_BOTH));
 		table.setHeaderVisible(true);
 		table.setLinesVisible(false);
         table.addListener(SWT.SetData, new Listener() {
             public void handleEvent(Event e) {
               TableItem item = (TableItem)e.item;
               int index = table.indexOf(item);
               generateOneItem(item,tableContentFileArray[index],index);
             }
         });
         
 		this.tableDefaultBackground=table.getBackground();
 
 		columnName = new TableColumn(table, SWT.BORDER);
 		columnName.setText(Messages.getString("FileLister.nameColumnTitle")); //$NON-NLS-1$
 		columnName.setData("sortColumn", "name");
 		columnName.setWidth(180);
 		columnName.addListener(SWT.Selection, sortListener);
 
 		columnSize = new TableColumn(table, SWT.BORDER);
 		columnSize.setText(Messages.getString("FileLister.sizeColumnTitle")); //$NON-NLS-1$
 		columnSize.setData("sortColumn", "size");
 		columnSize.setWidth(sizeWidth);
 		columnSize.addListener(SWT.Selection, sortListener);
 
 		columnDate = new TableColumn(table, SWT.BORDER);
 		columnDate.setText(Messages.getString("FileLister.dateColumnTitle")); //$NON-NLS-1$
 		columnDate.setData("sortColumn", "date");
 		columnDate.setWidth(dateWidth);
 		columnDate.addListener(SWT.Selection, sortListener);
 
 		table.setSortColumn(columnName);
 		table.setSortDirection(SWT.UP);
 
 	}
 
 	Listener sortListener = new Listener() {
 		public void handleEvent(Event e) {
 			sort((TableColumn) e.widget);
 		}
 	};
 
 	public void sort(String column) {
 		if (column.equals("name")) {
 			sort(columnName);
 		} else if (column.equals("date")) {
 			sort(columnDate);
 		} else if (column.equals("size")) {
 			sort(columnSize);
 		}
 
 	}
 
 	private void sort(TableColumn column) {
 		TableColumn tableSortColumn = table.getSortColumn();
 		int dir = table.getSortDirection();
 		if (tableSortColumn == column) {
 			dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
 		} else {
 			table.setSortColumn(column);
 			dir = SWT.UP;
 		}
 		sortColumn = (String) column.getData("sortColumn");
 		table.setSortColumn(column);
 		table.setSortDirection(dir);
 
 		if (table.getSortDirection() == SWT.DOWN) {
 			isReverse = true;
 		} else {
 			isReverse = false;
 		}
 		
 		int itemCount=table.getItemCount();
 		if (itemCount==0) return;
 		File[] currentFiles = new File[itemCount];
 		for (int i=0; i<itemCount; i++) {
 			currentFiles[i]=new File(getItemFullPath(i));
 		}
 		//this.setTableContentFiles(currentFiles);
 		//sortList(currentFiles, sortColumn, isReverse);
 		//table.removeAll();
 		String dirPosInfo = (String) historyManager.getSelectedItem(pwd);
 		this.selectedName = dirPosInfo;
 		generateItems(currentFiles, false);
 		updateStatusText();
 	}
 
 	public void addListener(FileListerListener listener) {
 		this.listeners.add(listener);
 	}
 
 	public void removeListener(FileListerListener listener) {
 		this.listeners.remove(listener);
 	}
 
 	public void notifyChangeSelection() {
 		for (FileListerListener listener: listeners) {
 			listener.onChangeSelection(getItemFullPath(currentRow));
 		}
 	}
 	
 	public void notifyChangePwd(String oldPath, String newPath) {
 		for (FileListerListener listener: listeners) {
 			listener.onChangePwd(oldPath, newPath);
 		}
 	}
 
 
 	private void initListener() {
 		btnUpDir.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent arg0) {
 				upOneDir();
 			}
 		});
 		btnTopDir.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent arg0) {
 				visit(FS_ROOT);
 			}
 		});
 		textLocation.addKeyListener(new KeyAdapter() {
 			public void keyPressed(KeyEvent event) {
 				if (event.character == SWT.CR) {
 					visit(textLocation.getText());
 					refreshHistoryInfo();
 					table.setFocus();
 				}
 				if (event.keyCode == SWT.ESC || (event.keyCode=='[' && event.stateMask == SWT.CTRL)) {
 					table.setFocus();
 				}
 			}
 
 		});
 
 		table.addTraverseListener(new TraverseListener() {
 			public void keyTraversed(TraverseEvent event) {
 				event.doit = false;
 			}
 		});
 		table.addKeyListener(new FileListerKeyListener(this));
 
 		table.addFocusListener(new FocusAdapter() {
 			public void focusGained(FocusEvent e) {
 				setTabTitle();
 				table.setBackground(ResourceManager.ActiveListerBackground);
 			}
 			public void focusLost(FocusEvent e) {
 				table.setBackground(tableDefaultBackground);
 			}
 		});
 		
 		table.addListener(SWT.Paint, new Listener() {
 			public void handleEvent(Event event) {
 				Point tableSize = table.getSize();
 				if (table.getItemCount() > 1 ) {
 					Rectangle bottomRect = table.getItem(table.getItemCount() - 1).getBounds();
 					int y = bottomRect.y + bottomRect.height;
 					event.gc.fillRectangle(0, y, tableSize.x, tableSize.y - y);
 				}
 			}
 		});
 
 		table.addListener(SWT.EraseItem, new Listener() {
 			public void handleEvent(Event event) {
 				TableColumn sortColumn = table.getSortColumn();
 				if (sortColumn == null)
 					return;
 				int columnIndex = table.indexOf(sortColumn);
 				if (columnIndex != event.index)
 					return;
 				event.gc.fillRectangle(event.getBounds());
 			}
 		});
 
 
 		
 		table.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent arg0) {
 				currentRow = table.getSelectionIndex();
                 changeLocationText();
 				setTabTitle();
 			}
 
 			public void widgetDefaultSelected(SelectionEvent arg0) {
 				enterPath();
 			}
 		});
 		table.addListener(SWT.MenuDetect, new Listener() {
 			public void handleEvent(Event arg0) {
 				int selectionIndex=table.getSelectionIndex();
 				System.out.println(selectionIndex);
 				
 				Menu menu = createPopMenu();
 				menu.setLocation(arg0.x, arg0.y);
 				menu.setVisible(true);
 			}
 		});
 		table.addMouseListener(new MouseAdapter() {
 			public void mouseDown(MouseEvent event) {
 				switchToNormalMode();
 				fileManager.activePanel(pos);
 			}
 		});
 	}
 
 	public void searchNext(boolean isForward) {
 		if (searchString != null) {
 			if (isForward) {
 				incSearch(searchString, true, false);
 			} else {
 				incSearch(searchString, false, false);
 			}
 		}
 	}
 
 	public HistoryManager getHistoryInfo() {
 		return this.historyManager;
 	}
 
 	public String[] getSelectionFiles() {
 		TableItem[] items = table.getSelection();
 		String[] result = new String[items.length];
 
 		for (int i = 0; i < items.length; i++) {
 			result[i] = FilenameUtils.concat(pwd, items[i].getText(0));
 		}
 		return result;
 
 	}
 
 	private void toggleSelection(int index) {
 		if (table.isSelected(index)) {
 			delSelection(index);
 		} else {
 			addSelection(index);
 		}
 	}
 
 	private void addSelection(int index) {
 		int[] selections = table.getSelectionIndices();
 		int[] tmp = new int[selections.length + 1];
 		for (int i = 0; i < selections.length; i++) {
 			tmp[i] = selections[i];
 		}
 		tmp[tmp.length - 1] = index;
 		table.setSelection(tmp);
 		table.showSelection();
 
 	}
 
 	private void delSelection(int index) {
 		int[] selections = table.getSelectionIndices();
 		if (selections.length <= 1)
 			return;
 		int[] tmp = new int[selections.length];
 		for (int i = 0; i < selections.length; i++) {
 			if (index == selections[i])
 				continue;
 			tmp[i] = selections[i];
 		}
 		table.setSelection(tmp);
 		table.showSelection();
 
 	}
 
 	private void initResource() {
 		fileImage = ResourceManager.getImage("file.png"); //$NON-NLS-1$
 		folderImage = ResourceManager.getImage("folder.png"); //$NON-NLS-1$
 		driveImage = ResourceManager.getImage("drive.png"); //$NON-NLS-1$
 
 		GC gc = new GC(Display.getDefault());
 		sizeWidth = gc.stringExtent("000Kb.00").x + 10; //$NON-NLS-1$
 		dateWidth = gc.stringExtent("0000-00-00 00:00").x + 20; //$NON-NLS-1$
 	}
 
 	public void tagCurrentItem() {
 		if (this.getOperateMode() != Mode.TAG) {
 			switchToTagMode();
 		} else {
 			toggleSelection(currentRow);
 		}
 
 	}
 
 	public void switchToTagMode() {
 		setOperateMode(Mode.TAG);
 		currentRow = table.getSelectionIndex();
 
 		if (cursor == null || cursor.isDisposed()) {
 			cursor = new TableCursor(table, SWT.NONE);
 			cursor.setSelection(table.getSelectionIndex(), 0);
 			cursor.setFocus();
 			cursor.addKeyListener(new FileListerKeyListener(this));
 		}
 
 	}
 
 	private Menu createPopMenu() {
 		Menu menu = new Menu(table.getShell(), SWT.POP_UP);
 		MenuItem editItem = new MenuItem(menu, SWT.PUSH);
 		editItem.setText(Messages.getString("FileLister.menuitemEdit")); //$NON-NLS-1$
 		editItem.setImage(ResourceManager.getImage("gtk-edit.png")); //$NON-NLS-1$
 		editItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent arg0) {
 				String nextEntry = getItemFullPath(currentRow);
 				File file = new File(nextEntry);
 				if (nextEntry == null || file == null)
 					return;
 				if (file.isFile() && file.canRead()) {
 					Util.editFile(getPwd(),nextEntry);
 				}
 			}
 		});
 
 		MenuItem openItem = new MenuItem(menu, SWT.PUSH);
         menu.setDefaultItem(openItem);
 		openItem.setText(Messages
 				.getString("FileLister.menuitemOpenWithDefault")); //$NON-NLS-1$
 		openItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				openWithDefault();
 			}
 		});
 		
 		new MenuItem(menu, SWT.SEPARATOR);
 		
 		String operatedFile = getItemFullPath(currentRow);
         String ext = FilenameUtils.getExtension(operatedFile);
         MimeManager mimeManager=MimeManager.getInstance();
         List<String> appPaths=mimeManager.get(ext);
 
         if (appPaths != null) {
             for (final String appPath: appPaths) {
                 String baseName = FilenameUtils.getBaseName(appPath);
                 MenuItem openWithItem = new MenuItem(menu, SWT.PUSH);
                 String msg=Messages.getString("FileLister.menuitemOpenWith");
                 msg=msg.replaceAll("\\$", baseName);
                 openWithItem.setText(msg);
                 openWithItem.addSelectionListener(new SelectionAdapter() {
                     public void widgetSelected(SelectionEvent event) {
                         openWith(appPath);
                     }
                 });
             }
         }
 
 		MenuItem openMethodItem = new MenuItem(menu, SWT.PUSH);
 		openMethodItem.setText(Messages
 				.getString("FileLister.menuitemOpenMethod")); //$NON-NLS-1$
 		openMethodItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				openWith(null);
 			}
 		});
 
 		new MenuItem(menu, SWT.SEPARATOR);
 		
 		MenuItem createFolderMenuItem = new MenuItem(menu, SWT.PUSH);
 		createFolderMenuItem.setText(Messages.getString("FileManager.menuitemCreateFolder")); //$NON-NLS-1$
 		createFolderMenuItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent arg0) {
 				doAddItem("folder");
 			}
 		});
 		
 		MenuItem createFileMenuItem = new MenuItem(menu, SWT.PUSH);
 		createFileMenuItem.setText(Messages.getString("FileManager.menuitemCreateFile")); //$NON-NLS-1$
 		createFileMenuItem.setImage(ResourceManager.getImage("file-new.png")); //$NON-NLS-1$
 		createFileMenuItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent arg0) {
 				doAddItem("file");
 			}
 		});
 		
 		new MenuItem(menu, SWT.SEPARATOR);
 
 		MenuItem cutItem = new MenuItem(menu, SWT.PUSH);
 		cutItem.setText(Messages.getString("FileLister.menuitemCut")); //$NON-NLS-1$
 		cutItem.setImage(ResourceManager.getImage("edit-cut.png")); //$NON-NLS-1$
 		cutItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent arg0) {
 				doCut();
 			}
 		});
 		MenuItem copyItem = new MenuItem(menu, SWT.PUSH);
 		copyItem.setText(Messages.getString("FileLister.menuitemCopy")); //$NON-NLS-1$
 		copyItem.setImage(ResourceManager.getImage("edit-copy.png")); //$NON-NLS-1$
 		copyItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent arg0) {
 				doYank();
 			}
 		});
 
 		MenuItem pasteItem = new MenuItem(menu, SWT.PUSH);
 		pasteItem.setText(Messages.getString("FileLister.menuitemPaste")); //$NON-NLS-1$
 		pasteItem.setImage(ResourceManager.getImage("edit-paste.png")); //$NON-NLS-1$
 		pasteItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent arg0) {
 				doPaste();
 			}
 		});
 		new MenuItem(menu, SWT.SEPARATOR);
 
 		MenuItem renameItem = new MenuItem(menu, SWT.PUSH);
 		renameItem.setText(Messages.getString("FileLister.menuitemRename")); //$NON-NLS-1$
 		renameItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent arg0) {
 				doChange();
 			}
 		});
 
 		MenuItem deleteItem = new MenuItem(menu, SWT.PUSH);
 		deleteItem.setText(Messages.getString("FileLister.menuitemDelete")); //$NON-NLS-1$
 		deleteItem.setImage(ResourceManager.getImage("edit-delete.png")); //$NON-NLS-1$
 		deleteItem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent arg0) {
 				doDelete();
 			}
 		});
 
 
 		return menu;
 	}
 
 	public void switchToVTagMode() {
 		this.setOperateMode(Mode.VTAG);
 		this.origRow = currentRow;
 	}
 
 	public void switchToOrigMode() {
 		this.setOperateMode(Mode.ORIG);
 	}
 
 	public void switchPanel() {
 		fileManager.changePanel();
 	}
 
 	public void detail() {
 
 		columnSize = new TableColumn(table, SWT.BORDER);
 		columnSize.setText(Messages.getString("FileLister.sizeColumnTitle")); //$NON-NLS-1$
 		columnSize.setData("sortColumn", "size");
 		columnSize.setWidth(sizeWidth);
 		columnSize.addListener(SWT.Selection, sortListener);
 
 		columnDate = new TableColumn(table, SWT.BORDER);
 		columnDate.setText(Messages.getString("FileLister.dateColumnTitle")); //$NON-NLS-1$
 		columnDate.setData("sortColumn", "date");
 		columnDate.setWidth(dateWidth);
 		columnDate.addListener(SWT.Selection, sortListener);
 		showDetail = true;
 	}
 
 	public void brief() {
 		//columnSize.dispose();
 		//columnDate.dispose();
 		showDetail = false;
 	}
 
 	public void gotoLine() {
 		if (commandBuffer == null) {
 			commandBuffer = "g"; //$NON-NLS-1$
 		} else {
 			if (commandBuffer.equals("g")) { //$NON-NLS-1$
 				cursorTop();
 			}
 			commandBuffer = null;
 		}
 	}
 
 	public void doCount(char value) {
 		if (this.countString == null) {
 			countString = String.valueOf(value);
 		} else {
 			countString = countString + String.valueOf(value);
 		}
 	}
 
 	public void doDelete() {
 		Command command = new RemoveCommand(getSelectionFiles(), this.getPwd());
 		command.setFileLister(this);
 		commandRunner.run(command);
 		switchToNormalMode();
 	}
 
 	public void doCut() {
 
 		String[] selectionFiles = getSelectionFiles();
 
 		CommandBuffer commandBuffer = CommandBuffer.getInstance();
 		commandBuffer.setImpendingCommand("mv"); //$NON-NLS-1$
 		commandBuffer.setCommandSourceFiles(selectionFiles);
 		commandBuffer.setCommandSourcePath(pwd);
 
 		fileManager.setStatusInfo(selectionFiles.length + " Items moved."); //$NON-NLS-1$
 		switchToNormalMode();
 		table.setSelection(currentRow);
 
 	}
 
 	public void doYank() {
 
 		String[] selectionFiles = getSelectionFiles();
 		CommandBuffer commandBuffer = CommandBuffer.getInstance();
 		commandBuffer.setImpendingCommand("cp"); //$NON-NLS-1$
 		commandBuffer.setCommandSourceFiles(selectionFiles);
 		commandBuffer.setCommandSourcePath(pwd);
 
 		fileManager.setStatusInfo(selectionFiles.length + " Items copyed."); //$NON-NLS-1$
 		switchToNormalMode();
 		table.setSelection(currentRow);
 
 	}
 
 	public void doCopyToClipboard() {
 
 		String[] selectionFiles = getSelectionFiles();
 		Clipboard clipboard = new Clipboard(Main.display);
 		FileTransfer transfer = FileTransfer.getInstance();
 		clipboard.setContents(new Object[] { selectionFiles },
 				new Transfer[] { transfer });
 		clipboard.dispose();
 		fileManager.setStatusInfo(selectionFiles.length
 				+ " Items copyed to clipboard."); //$NON-NLS-1$
 		switchToNormalMode();
 
 	}
 
 	public void doPaste() {
 
 		CommandBuffer commandBuffer = CommandBuffer.getInstance();
 		String[] srcFiles = commandBuffer.getCommandSourceFiles();
 		String operate = commandBuffer.getImpendingCommand();
 
 		if (operate == null || srcFiles == null)
 			return;
 		String srcPath = commandBuffer.getCommandSourcePath();
 
 		Command command = null;
 		if (operate.equals("cp")) { //$NON-NLS-1$
 			command = new CopyCommand(srcPath, pwd, srcFiles);
 		} else if (operate.equals("mv")) { //$NON-NLS-1$
 			command = new MoveCommand(srcPath, pwd, srcFiles);
 		}
 		command.setFileLister(this);
 		command.setInActiveFileLister(fileManager.getInActivePanel());
 		commandRunner.run(command);
 
 		// refresh();
 		commandBuffer.setImpendingCommand(null);
 		commandBuffer.setCommandSourceFiles(null);
 
 		switchToNormalMode();
 
 	}
 
 	public void doPasteFromClipboard() {
 		Clipboard clipboard = new Clipboard(Main.display);
 		FileTransfer transfer = FileTransfer.getInstance();
 		String[] data = (String[]) clipboard.getContents(transfer);
 		if (data != null) {
 			Command command = new CopyCommand(null, pwd, data);
 			command.setFileLister(this);
 			commandRunner.run(command);
 		}
 		clipboard.dispose();
 		switchToNormalMode();
 	}
 
 	public void doChange() {
 		editFileName();
 	}
 
 	public void doAddItem(final String type) {
 		TableItem item = new TableItem(table, SWT.BORDER);
 		if (type.equals("file")) {
 			item.setImage(fileImage);
 		} else if (type.equals("folder")) {
 			item.setImage(folderImage);
 		}
 		currentRow = table.getItemCount() - 1;
 		doSelect();
 
 		editor = createTableEditor();
 		final Text textWidget = (Text) editor.getEditor();
 		textWidget.addKeyListener(new ItemTextKeyListener(type));
 		textWidget.addFocusListener(new FocusAdapter() {
 			public void focusLost(FocusEvent event) {
 				updateEditItem(textWidget, type);
 			}
 		});
 		textWidget.addKeyListener(new ItemTextKeyListener(type));
 	}
 	
 	
 	
 	private void updateEditItem(Text textEditor,String itemType) {
 		if (!textEditor.getText().trim().equals("")) {
 			TableItem item = table.getItem(currentRow);
 			File currentFile = new File(FilenameUtils.concat(pwd, textEditor.getText()));
 			boolean isSuccess = false;
 			if (itemType.equals("file")) {
                 isSuccess = true;
 				if (!currentFile.exists()) {
 					try {
 						currentFile.createNewFile();
 					} catch (IOException e) {
                         isSuccess = false;
 					}
 				} else {
 					currentFile.setLastModified(System
 							.currentTimeMillis());
 				}
 
 			} else if (itemType.equals("folder")) {
 				isSuccess = fileModelManager.mkdir(currentFile.getPath());
 
 			} else if (itemType.equals("rename")) {
 				isSuccess = currentFile.renameTo(
 						new File(FilenameUtils .concat(pwd, textEditor.getText().trim())));
 			}
 
 			if (isSuccess) {
 				fileManager.setStatusInfo("command successed."); //$NON-NLS-1$
 			} else {
 				fileManager.setStatusInfo("command failed."); //$NON-NLS-1$
 			}
 
 			item.setText(0, textEditor.getText().trim());
 			textEditor.dispose();
 			editor.dispose();
 		}
         updateStatusText();
 	}
 
 	class ItemTextKeyListener extends KeyAdapter {
 		private String itemType;
 
 		public ItemTextKeyListener(String itemType) {
 			this.itemType = itemType;
 		}
 
 		public void keyPressed(KeyEvent event) {
 			Text textEditor = (Text) event.widget;
 
 			if (textEditor.isDisposed())
 				return;
 			if (event.keyCode == SWT.CR) { //$NON-NLS-1$
 				updateEditItem(textEditor,itemType);
 				table.setFocus();
 			}
 			if (event.keyCode == SWT.ESC || (event.keyCode=='[' && event.stateMask == SWT.CTRL)) {
 				if (itemType.equals("file") || itemType.equals("folder")) {
 					table.remove(currentRow);
 					if (table.getItemCount() > 0)
 						table.setSelection(table.getItemCount() - 1);
 				}
 				textEditor.dispose();
 				table.setFocus();
 			}
 		}
 	}
 
 	private TableEditor createTableEditor() {
 		editor = new TableEditor(table);
 		editor.horizontalAlignment = SWT.LEFT;
 		editor.grabHorizontal = true;
 		editor.minimumWidth = 20;
 
 		Control oldEditor = editor.getEditor();
 		if (oldEditor != null)
 			oldEditor.dispose();
 
 		TableItem item = table.getItem(currentRow);
 		Text text = new Text(table, SWT.NONE);
 		text.setFocus();
 
 		editor.setEditor(text, item, 0);
 
 		return editor;
 	}
 
 	private void editFileName() {
 		editor = new TableEditor(table);
 		editor.horizontalAlignment = SWT.LEFT;
 		editor.grabHorizontal = true;
 		editor.minimumWidth = 20;
 		Control oldEditor = editor.getEditor();
 		if (oldEditor != null)
 			oldEditor.dispose();
 
 		TableItem item = table.getItem(currentRow);
 		if (item == null)
 			return;
 
 		final Text newEditor = new Text(table, SWT.NONE);
 		newEditor.setText(item.getText(0));
 		newEditor.addFocusListener(new FocusAdapter() {
 			public void focusLost(FocusEvent arg0) {
 				newEditor.dispose();
 			}
 		});
 		newEditor.addKeyListener(new KeyAdapter() {
 			public void keyPressed(KeyEvent event) {
 				if (event.keyCode == SWT.CR
 						&& !newEditor.getText().trim().equals("")) { //$NON-NLS-1$
 					TableItem item = table.getItem(currentRow);
 					File currentFile = new File(FilenameUtils.concat(pwd, item
 							.getText(0)));
 					boolean isSuccess = currentFile.renameTo(new File(
 							FilenameUtils.concat(pwd, newEditor.getText()
 									.trim())));
 					if (isSuccess) {
 						fileManager.setStatusInfo("rename filename successed."); //$NON-NLS-1$
 					} else {
 						fileManager.setStatusInfo("rename filename failed."); //$NON-NLS-1$
 					}
 					item.setText(newEditor.getText().trim());
 					newEditor.dispose();
 					table.setFocus();
 				}
 				if (event.keyCode == SWT.ESC || (event.keyCode=='[' && event.stateMask == SWT.CTRL)) {
 					newEditor.dispose();
 					table.setFocus();
 				}
 			}
 
 		});
 		newEditor.selectAll();
 		newEditor.setFocus();
 		editor.setEditor(newEditor, item, 0);
 	}
 
 	private void select(int origRow, int currentRow) {
 		if (origRow > currentRow) {
 			table.setSelection(currentRow, origRow);
 		} else {
 			table.setSelection(origRow, currentRow);
 		}
 	}
 
 	public void selectAll() {
 		table.setSelection(0, table.getItemCount());
 	}
 
 	private void doSelect() {
 
 		if (table.getItemCount() == 0)
 			return;
 
 		switch (getOperateMode()) {
 		case NORMAL :
 			table.setSelection(currentRow);
 			break;
 		case VTAG :
 			select(origRow, currentRow);
 			table.showItem(table.getItem(currentRow));
 			break;
 		case TAG :
 			if (!cursor.isDisposed()) {
 				cursor.setSelection(currentRow, 0);
 			}
 		}
         changeLocationText();
 	}
 
 	public void cursorDown() {
 		cursorDown(1);
 	}
 
 	public void cursorDown(int count) {
 		int i = 0;
 		while (i < count && currentRow < table.getItemCount() - 1) {
 			i++;
 			currentRow++;
 		}
 		doSelect();
 	}
 
 	public void cursorUp() {
 		cursorUp(1);
 	}
 
 	public void cursorUp(int count) {
 		int i = 0;
 		while (i < count && currentRow > 0) {
 			i++;
 			currentRow--;
 		}
 		doSelect();
 	}
 
 	public void cursorHead() {
 		currentRow = table.getTopIndex();
 
 		doSelect();
 	}
 
 	public void cursorLast() {
 		if (table.getItemCount() < 1)
 			return;
 		int count = table.getSize().y / table.getItem(0).getBounds().height - 1;
 		currentRow = table.getTopIndex() + count - 2;
 		if (currentRow >= table.getItemCount())
 			currentRow = table.getItemCount() - 1;
 		doSelect();
 	}
 
 	public void cursorMiddle() {
 		if (table.getItemCount() < 1)
 			return;
 		int count = table.getSize().y / table.getItem(0).getBounds().height - 1;
 		if (count >= table.getItemCount())
 			count = table.getItemCount() - 1;
 		currentRow = table.getTopIndex() + (count / 2);
 		doSelect();
 	}
 
 	public void cursorBottom() {
 		currentRow = table.getItemCount() - 1;
 		doSelect();
 
 	}
 
 	public void cursorTop() {
 		currentRow = 0;
 		doSelect();
 	}
 
 	public void back(int count) {
 		String path = historyManager.back(count);
 		if (path != null)
 			changePwd(path,true);
 	}
 
 	public void back() {
 		back(1);
 	}
 
 	public void forward(int count) {
 		String path = historyManager.forward(count);
 		if (path != null)
 			changePwd(path,true);
 	}
 
 	public void forward() {
 		forward(1);
 	}
 
 	public void redraw() {
 		table.redraw();
 	}
 
 	public void active() {
 		table.setFocus();
 		setTabTitle();
 		setModeIndicate();
 	}
 
 
 	public void setCursorPosition(int row) {
 		currentRow = row;
 		doSelect();
 	}
 
 	public boolean setFocus() {
 		return table.setFocus();
 	}
 
 	public void refresh() {
 		visit(pwd);
 	}
 
 	public String getPwd() {
 		return pwd;
 	}
 
 	public int getCurrentRow() {
 		return this.currentRow;
 	}
 
 
 	public void refreshHistoryInfo() {
 		String tmpPwd = pwd;
 		int index = 0;
 		while (true) {
 			index = tmpPwd.lastIndexOf(File.separator);
 			if (index < 0)
 				break;
 			String selection = tmpPwd.substring(index + 1);
 			tmpPwd = tmpPwd.substring(0, index);
 			if (tmpPwd.endsWith(":")) { //$NON-NLS-1$
 				historyManager.setSelectedItem("", tmpPwd); //$NON-NLS-1$ //$NON-NLS-2$
 				historyManager.setSelectedItem(tmpPwd, selection); //$NON-NLS-1$
 			} else {
 				if (index == 0) {
 					historyManager.setSelectedItem(File.separator, selection);
 				} else {
 					historyManager.setSelectedItem(tmpPwd, selection);
 				}
 			}
 		}
 	}
 
 	public void visit(String path) {
 		visit(path,true);
 	}
 	
 	public void visit(String path, boolean fireChangePwd) {
 		File file = new File(path);
 		if (!file.exists() && !FS_ROOT.equals(path)) {
 			fileManager.setStatusInfo(path + " not existed.");
 			return;
 		}
 
 		changePwd(path, fireChangePwd);
 		refreshHistoryInfo();
 		historyManager.addToHistory(path);
 	}
 	
 	private File[] getReadAbleRoots() {
 		ArrayList<File> rootList = new ArrayList<File>();
 		for (File file : File.listRoots() ) {
 			if (file.canRead()) {
 				rootList.add(file);
 			}
 		}
 		return rootList.toArray(new File[]{});
 	}
 
 	private void setTableContentFiles(File[] files,boolean append) {
 		if (this.winRoot == true) {
 			this.tableContentFileArray = files;
 		} else {
 			if (!append) {
 				this.tableContentFiles = new TreeSet<File>(FileComprator.getFileComprator(this.sortColumn, this.isReverse));
 				for (File file : files) {
 					this.tableContentFiles.add(file);
 				}
 				this.tableContentFileArray = this.tableContentFiles.toArray(new File[files.length]);
 			} else {
 				 File[] newFileContentArray = new File[tableContentFileArray.length + files.length];
 		         System.arraycopy(this.tableContentFileArray, 0, newFileContentArray, 0, this.tableContentFileArray.length);
 		         System.arraycopy(files, 0, newFileContentArray, newFileContentArray.length-1, files.length);
 		         this.tableContentFileArray =newFileContentArray;
 			}
 		}
 		table.setItemCount(tableContentFileArray.length);
 		table.clearAll();
 		
 		for (int i=0; i<this.tableContentFileArray.length; i++) {
 			File file = this.tableContentFileArray[i];
 		 if (file.getName().equals(selectedName)||
				 (this.winRoot==true && file.getPath().equals(selectedName+"\\"))) {
 				currentRow = i;
 				hasMatchSelectedName = true;
 			}
 		}
 	}
 	public void changePwd(String path,boolean fireChangeEvent) {
 
 		if (getOperateMode() != Mode.ORIG )
 			switchToNormalMode();
 		String nextEntry = getItemFullPath(currentRow);
 		if (nextEntry != null) {
 			File file = new File(nextEntry);
 			historyManager.setSelectedItem(pwd, file.getName());
 		}
 		if (pwd.equals(""))historyManager.setSelectedItem(pwd, nextEntry); //$NON-NLS-1$
 		
 		if (fireChangeEvent && syncWithFileTree) {
 			notifyChangePwd(pwd, path);
 		}
 
 		File[] currentFiles = null;
 		// if ( path.endsWith(":")) path=path+File.separator; //$NON-NLS-1$
 		// change pwd to file system root
 		columnDate.setText(Messages.getString("FileLister.dateColumnTitle")); //$NON-NLS-1$
 		if (path.equals(FS_ROOT)) {
 			if (ENV_OS.substring(0, 3).equalsIgnoreCase("win")) { //$NON-NLS-1$
 				this.winRoot = true;
 				String dirPosInfo = (String) historyManager.getSelectedItem(path);
 				this.selectedName = dirPosInfo;
 				setTableContentFiles(this.getReadAbleRoots(),false);
 				pwd = ""; //$NON-NLS-1$
 				table.setSelection(currentRow);
 				return;
 
 			} else {
 				path = "/"; //$NON-NLS-1$
 			}
 		}
 		
 		this.winRoot = false;
 		File file = null;
 		if (path.endsWith(":")) {
 			file = new File(path + File.separator);
 		} else {
 			file = new File(path);
 		}
 
 		if (file.isDirectory()) {
 			currentFiles = getFilteredFiles(file, filterString);
 			if (currentFiles == null)
 				return;
 			pwd = path;
 			boolean isReverse;
 			if (table.getSortDirection() == SWT.DOWN) {
 				isReverse = true;
 			} else {
 				isReverse = false;
 			}
 
 			//sortList(currentFiles, sortColumn, isReverse);
 			String dirPosInfo = (String) historyManager.getSelectedItem(path);
 			this.selectedName = dirPosInfo;
 			generateItems(currentFiles, false);
 			updateStatusText();
             changeLocationText();
 
 		}
 		filterString = null;
 		setTabTitle();
 
 	}
 
     private void changeLocationText() {
         String currentPath = ""; 
     	if (table.getItemCount() == 0) {
     		currentPath=getPwd();
 	    } else {
 			File selectFile = new File(getItemFullPath(currentRow));
 	        if (selectFile.isDirectory()) {
 	            currentPath=selectFile.getPath();
 	        } else {
 	            currentPath=selectFile.getParent();
 	        }
 	    }
         String tmpPath = getLastLongestPath(currentPath);
         File file = new File(tmpPath);
         while(true) {
             if (file.exists()) {
                 textLocation.setText(file.getAbsolutePath());
                 break;
             }
             file = file.getParentFile();
             if (file == null ) {
                 textLocation.setText(currentPath);
                 break;
             }
         }
         if (currentPath.indexOf(File.separator) > -1) {
             int length = currentPath.substring(currentPath.lastIndexOf(File.separator))
                     .length() - 1;
             if (length > 1) {
                 StyleRange style1 = new StyleRange();
                 style1.start = currentPath.length() - length;
 
                 style1.length = length;
                 style1.fontStyle = SWT.BOLD;
                 style1.background = new Color(textLocation.getDisplay(), 166, 166, 166);
                 textLocation.setStyleRange(style1);
             }
         }
 
         // win root driver c:\ d:\ etc.
         if (currentPath.endsWith(":\\")) { //$NON-NLS-1$
             StyleRange style1 = new StyleRange();
             style1.start = 0;
             style1.length = currentPath.length() - 1;
             style1.fontStyle = SWT.BOLD;
             style1.background = new Color(textLocation.getDisplay(), 166,
                     166, 166);
             textLocation.setStyleRange(style1);
         }
     }
 
 	private String getLastLongestPath(String path) {
 
 		if (path.indexOf(File.separator) < 0)
 			return path;
 		LinkedList<String> list = historyManager.getFullHistory();
 		if (list.size() < 0)
 			return path;
 
 		if (!path.endsWith(File.separator))
 			path = path + File.separator;
 		String longestPath = path;
 
 		int lastMatchIndex = -1;
 		for (int i = list.size() - 1; i > 0; i--) {
 			String his = (String) list.get(i);
 			if (his.startsWith(path)) {
 				lastMatchIndex = i;
 				longestPath = his;
 				break;
 			}
 		}
 		while (true) {
 			if (lastMatchIndex < 1)
 				break;
 			String tmp = (String) list.get(lastMatchIndex--);
 
 			if (!tmp.startsWith(longestPath))
 				break;
 			longestPath = tmp;
 		}
 		if (longestPath.endsWith(File.separator))
 			longestPath = longestPath.substring(0, longestPath.length() - 1);
 		return longestPath;
 
 	}
 
 	public Point getSize() {
 		return table.computeSize(SWT.DEFAULT, SWT.DEFAULT);
 	}
 
 	public void pack() {
 		for (int i = 0; i < table.getColumnCount(); i++) {
 			table.getColumn(i).pack();
 		}
 		Main.fileManager.pack();
 	}
 
 	public void setTabTitle() {
 		if (!table.isFocusControl())
 			return;
 		if (fileManager == null)
 			return;
 		if (pwd.trim().equals(""))fileManager.renameTab("File System"); //$NON-NLS-1$ //$NON-NLS-2$
 		File file = new File(pwd);
 		if (file != null && !file.getName().equals("")) //$NON-NLS-1$
 			fileManager.renameTab(file.getName() + " "); //$NON-NLS-1$
 		else
 			fileManager.renameTab(pwd + " "); //$NON-NLS-1$
 	}
 
 	public void removeAllItem() {
 		tableContentFileArray = new File[]{};;
 		table.setItemCount(tableContentFileArray.length);
 		table.clearAll();
 		
 	}
 
     public void updateStatusText() {
 		lblStatus.setText("total " + table.getItemCount() + " items"); //$NON-NLS-1$ //$NON-NLS-2$
     }
 
 	private File[] getFilteredFiles(File file, String filterString) {
 		File[] subFiles;
 		IOFileFilter fileFilter=Util.getDefaultFileFilter();
 		
 		if (filterString == null) {
 			subFiles = file.listFiles((FileFilter) fileFilter);
 		} else {
 			fileFilter = new AndFileFilter(fileFilter,
 					new WildcardFilter2(filterString));
 			subFiles = file.listFiles((FileFilter) fileFilter);
 
 		}
 		return subFiles;
 	}
 
 	private void generateOneItemForWinRoot(TableItem item, File file,int index) {
 		item.setText(0,file.getPath().substring(0, 2));
 		
 		item.setText(1, StringUtil.formatSize(file.getTotalSpace()));
 		item.setText(2, StringUtil.formatSize(file.getFreeSpace()));
 		item.setImage(0, driveImage);
 		String selection = (String) historyManager.getSelectedItem(""); //$NON-NLS-1$
 		if (file.getPath().equalsIgnoreCase( selection + File.separator)) {
 			currentRow = index;
 		}
 	}
 	
     public void generateOneItem(TableItem item,File file,int index) {
 
     	if (this.winRoot == true ) {
     		generateOneItemForWinRoot(item,file,index);
     		return;
     	}
 		int nameStart=pwd.endsWith(File.separator) ? pwd.length() : pwd.length()+1;
         String subFilePath = file.getPath().substring(nameStart);
         item.setText(0, subFilePath);
         item.setImage(0,ResourceManager.getMimeImage(file));
        
         if (showDetail) {
             if (file.isDirectory()) {
                 item.setText(1, "--");
             } else {
                 item.setText(1, StringUtil.formatSize(file.length()));
             }
             item.setText(2, StringUtil.formatDate(file.lastModified()));
         } else {
             item.setText(1,"");
             item.setText(2,"");
         }
     }
 
 	public void generateItems(File[] subFiles, boolean appendMode) {
 
 		if (subFiles == null )
 			subFiles = new File[]{};
 		hasMatchSelectedName = false;
 		this.setTableContentFiles(subFiles,appendMode);
         
 		if (appendMode) {
 			currentRow=table.getItemCount()-1;
 		} else {
 			if (!hasMatchSelectedName) currentRow = 0;
 		}
 		if (currentRow >-1) table.setSelection(currentRow);
 		updateStatusText();
 	}
 
 	public void doUnCompress(boolean extractToPwd) {
 		String[] selectionFiles = getSelectionFiles();
 		if (selectionFiles == null || selectionFiles.length <= 0)
 			return;
 		String archFileName = selectionFiles[0];
 		String baseName = "";
 		if (archFileName.endsWith(".tar.gz")) {
 			baseName = archFileName.substring(0, archFileName.length() - 7);
 		} else if (archFileName.endsWith(".tar.bz2")) {
 			baseName = archFileName.substring(0, archFileName.length() - 8);
 		} else {
 			baseName = FilenameUtils.getBaseName(archFileName);
 		}
 
 		Command command = null;
 		if (extractToPwd) {
 			command = new UnCompressCommand(archFileName, pwd);
 		} else {
 			command = new UnCompressCommand(archFileName, FilenameUtils.concat(
 					pwd, baseName));
 		}
 		command.setFileLister(this);
 		commandRunner.run(command);
 
 	}
 
 	public void doCompress() {
 		String[] selectionFiles = getSelectionFiles();
 		ZipUtil zipUtil = new ZipUtil();
 		if (selectionFiles == null || selectionFiles.length <= 0)
 			return;
 		// String baseName=FilenameUtils.getBaseName(selectionFiles[0]);
 
 		try {
 			zipUtil.zip(FilenameUtils.concat(pwd, new File(pwd).getName()),
 					selectionFiles);
 		} catch (Exception e) {
 			Util.openMessageWindow(e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	public String getPos() {
 		return pos;
 	}
 
 	public void setPos(String pos) {
 		this.pos = pos;
 	}
 
 	public void switchToNormalMode() {
 		if (this.getOperateMode() == Mode.TAG ) {
 			if (!cursor.isDisposed())
 				cursor.dispose();
 			table.setFocus();
 		}
 		this.setOperateMode(Mode.NORMAL);
 	}
 
 	public void cancelOperate() {
 
 		switchToNormalMode();
 		table.setFocus();
 		table.setSelection(currentRow);
 	}
 
 	public void filterPwd(String filterString) {
 		this.filterString = filterString;
 		visit(pwd);
 	}
 
 	private String getItemFullPath(int row) {
 		
 		TableItem[] items = table.getItems();
 		if (items.length == 0)
 			return null;
 		String nextEntry = ""; //$NON-NLS-1$
 
 		if (pwd.equals("")) { //$NON-NLS-1$
 			nextEntry = items[row].getText(0);
 		} else {
 			if (pwd.endsWith(File.separator)) {
 				nextEntry = pwd + items[row].getText(0);
 			} else {
 				nextEntry = pwd + File.separator + items[row].getText(0);
 			}
 		}
 		return nextEntry;
 		
 	}
 
 	public void enterPath() {
 		enterPath(1);
 	}
 
 	public void enterPath(int count) {
 		int i = 0;
 
 		String[] selection = getSelectionFiles();
 		if (selection.length > 1) {
 			Util.editFile(getPwd(),selection);
 			if (getOperateMode() != Mode.ORIG)
 				switchToNormalMode();
 			return;
 		}
 		String nextEntry = getItemFullPath(currentRow);
 		if (nextEntry == null)
 			return;
 		String historyDir = (String) historyManager.getSelectedItem(nextEntry);
 
 		while (i < count - 1 && historyDir != null
 				&& new File(nextEntry).isDirectory()) {
 			nextEntry = FilenameUtils.concat(nextEntry, historyDir);
 			historyDir = (String) historyManager.getSelectedItem(nextEntry);
 			i++;
 		}
 		File file = new File(nextEntry);
 		if (file.isFile() && count == 1) {
 			Util.editFile(getPwd(),nextEntry);
 		} else if (file.isFile() && count != 1) {
 			file = file.getParentFile();
 			visit(file.getPath());
 		} else {
 			visit(file.getPath());
 		}
 
 	}
 
 	public void openWithDefault() {
 		String path = getItemFullPath(currentRow);
 		Util.openFileWithDefaultApp(path);
 	}
 	
 	public void openWith(String appName) {
 		String operatedFile = getItemFullPath(currentRow);
         String ext = FilenameUtils.getExtension(operatedFile);
         if (appName == null ) {
             FileDialog fd = new FileDialog(Main.shell, SWT.OPEN);
             appName = fd.open();
         }
 		if (appName != null) {
 			try {
 				Runtime.getRuntime().exec(new String[] { appName,operatedFile }, null, new File(getPwd()));
                 MimeManager mimeManager = MimeManager.getInstance();
                 mimeManager.add(ext,appName);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	
 
 	public void incSearch(String pattern, boolean isForward, boolean isIncrease) {
 
 		this.searchString = pattern;
 		int curSearchPos = 0;
 		boolean isFind = false;
 		if (!isIncrease)
 			curSearchPos = 1;
 		TableItem[] items = table.getItems();
 
 		for (int i = 0; i < items.length - 1; i++) {
 			int nextPos = 0;
 			if (isForward) {
 				nextPos = currentRow + curSearchPos;
 				if (nextPos >= items.length) {
 					curSearchPos = curSearchPos - items.length;
 					nextPos = currentRow + curSearchPos;
 				}
 			} else {
 				nextPos = currentRow - curSearchPos;
 				if (nextPos < 0) {
 					curSearchPos = curSearchPos - items.length;
 					nextPos = currentRow - curSearchPos;
 				}
 			}
 
 			if (items[nextPos].getText(0).toLowerCase().indexOf(pattern) > -1) {
 				isFind = true;
 				currentRow = nextPos;
 				table.setSelection(currentRow);
 				break;
 			}
 			curSearchPos++;
 		}
 
 		if (!isIncrease && !isFind)
 			fileManager.setStatusInfo("file not found"); //$NON-NLS-1$
 		redraw();
 	}
 
 	public boolean isDisposed() {
 		return table.isDisposed();
 	}
 
 	public void upOneDir(int count) {
 		if (pwd.equals(""))return; //$NON-NLS-1$
 		if (pwd.endsWith(":")) { //$NON-NLS-1$
 			visit(FS_ROOT);
 			return;
 		}
 		String tmp = pwd;
 		int i = 0;
 		while (i < count && tmp.lastIndexOf(File.separator) > -1) {
 			tmp = tmp.substring(0, tmp.lastIndexOf(File.separator));
 			i++;
 		}
 		if (tmp.equals("")) { //$NON-NLS-1$
 			visit(FS_ROOT);
 		} else {
 			visit(tmp);
 		}
 	}
 
 	public void upOneDir() {
 		upOneDir(1);
 	}
 
 	public Display getDisplay() {
 		return table.getDisplay();
 	}
 
 	public void editAddress() {
 		textLocation.selectAll();
 		textLocation.setFocus();
 	}
 
 	public void addToView(File file) {
 		File tmpFile=file;
 		while (true ) {
 			String dstDir=tmpFile.getParent();
 			if (dstDir == null ) break;
 			if (dstDir.endsWith(File.separator)) {
 				dstDir=dstDir.substring(0,dstDir.length()-1);
 			}
 			if (pwd.equalsIgnoreCase(dstDir)) {
 				int itemIndex=searchTableItem(tmpFile.getName());
 				if (itemIndex < 0 ) {
 					String dirPosInfo = (String) historyManager.getSelectedItem(pwd);
 					this.selectedName = dirPosInfo;
 					  File[] newFileContentArray = new File[this.tableContentFileArray.length + 1];
 			          System.arraycopy(this.tableContentFileArray, 0, newFileContentArray, 0, this.tableContentFileArray.length);
 			          newFileContentArray[newFileContentArray.length-1] = tmpFile;
 			          this.tableContentFileArray = newFileContentArray;
 			          table.setItemCount(tableContentFileArray.length);
 			          table.clearAll();
 				      currentRow=table.getItemCount()-1;
 					  table.setSelection(currentRow);
 				} else {
 					updateItem(tmpFile,itemIndex);
 				}
 			    updateStatusText();
 			    break;
 			}
 			tmpFile=new File(dstDir);
 		}
 
 	}
 	
 	public void updateItem(File file,int itemIndex) {
 		TableItem item=null;
 		if (showDetail) {
 			item=table.getItem(itemIndex);
 			if (file.isDirectory()) {
 				item.setText(1, "--");
 			} else {
 				item.setText(1, StringUtil.formatSize(file.length()));
 			}
 			item.setText(2, StringUtil.formatDate(file.lastModified()));
 		} else {
 			item = table.getItem(itemIndex);
 			item.setText(1,"");
 			item.setText(2,"");
 		}
 		table.setSelection(item);
 		currentRow = table.getSelectionIndex();
 	}
 
 	public void removeFromView(File file) {
 		
 		//TableItem[] items = table.getItems();
 		List<File> remainedFileList = new ArrayList<File>();
 		int firstIndex = 0;
 		boolean hasDeletedItem = false;
 		for (int i = 0; i < this.tableContentFileArray.length; i++) {
 			String abspath=this.tableContentFileArray[i].getAbsolutePath();
 			if (!abspath.equalsIgnoreCase(file.getPath())) {
 				remainedFileList.add(this.tableContentFileArray[i]);
 			} else {
 				if (firstIndex ==0) {
 					firstIndex = i;
 					hasDeletedItem = true;
 				}
 			}
 		}
 		if (!hasDeletedItem) return;
 		 
 		this.tableContentFileArray = new File[remainedFileList.size()];
 		for (int i=0; i<tableContentFileArray.length; i++) {
 			tableContentFileArray[i] = remainedFileList.get(i);
 		}
 		table.setItemCount(tableContentFileArray.length);
         table.clearAll();
 		
 		if (firstIndex < table.getItemCount() - 1) {
 			currentRow = firstIndex;
 		} else {
 			currentRow = table.getItemCount() - 1;
 		}
 		table.setSelection(currentRow);
 			
         updateStatusText();
         changeLocationText();
 
 	}
 
 	public void addshortcuts() {
 		String[] selectionFiles = getSelectionFiles();
 		File file = new File(selectionFiles[0]);
 		if (!file.isFile()) {
 			fileManager.setStatusInfo(file.getName()+" is not a file.");
 			return;
 		}
 		Shortcut shortcuts = new Shortcut();
 		shortcuts.setName(file.getName());
 		shortcuts.setText(file.getPath());
 		ShortcutsManager scm = ShortcutsManager.getInstance();
 		scm.add(shortcuts);
 
 	}
 
 	public int searchTableItem(String name) {
 		TableItem[] items = table.getItems();
 		for (int i = 0; i < items.length; i++) {
 			if (name.equals(items[i].getText(0)))
 				return i;
 		}
 		return -1;
 	}
 
 	public void activeWidget() {
 		table.setFocus();
 	}
 
 	public void dispose() {
 		//container.dispose();
 	}
 	
 	public boolean isOrigMode() {
 		return this.getOperateMode()==Mode.ORIG;
 	}
 
 	public void setOperateMode(Mode operateMode) {
 		this.operateMode = operateMode;
 		setModeIndicate();
 	}
 	
 	public void setModeIndicate() {
 		String modeTip="";
 		if (operateMode == Mode.ORIG) {
 			modeTip=" -- Original-- ";
 		} else if (operateMode == Mode.TAG) {
 			modeTip=" -- Tag -- ";
 		} else if (operateMode == Mode.VTAG) {
 			modeTip = " -- Visual -- ";
 		}
 		this.fileManager.setModeIndicate(modeTip);
 	}
 
 	public Mode getOperateMode() {
 		return operateMode;
 	}
 
 	@Override
 	public void onAdd(final File file) {
 		Display.getDefault().syncExec(new Runnable() {
 			public void run() {
 				addToView(file);
 			}
 		});
 		
 	}
 
 	@Override
 	public void onRemove(final File file) {
 		Display.getDefault().syncExec(new Runnable() {
 			public void run() {
 				removeFromView(file);
 			}
 		});
 	}
 	
 	
 }
