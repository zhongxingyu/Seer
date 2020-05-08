 package org.purejava.lnae.threads;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import lotus.domino.Database;
 import lotus.domino.DbDirectory;
 import lotus.domino.DocumentCollection;
 import lotus.domino.NotesException;
 import lotus.domino.NotesThread;
 import lotus.domino.Session;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeItem;
 import org.eclipse.wb.swt.SWTResourceManager;
 import org.purejava.lnae.swt.App;
 import org.purejava.lnae.swt.InfoDialog;
 import org.purejava.lnae.swt.SelectDatabaseDialog;
 import org.purejava.utilities.FileSizeCalc;
 import org.purejava.utilities.LotusNotesDatabaseProperties;
 
 public class SelectDatabaseThread extends Thread {
 	
 	private InfoDialog infoDialog;
 	private SelectDatabaseDialog selectDBDialog;
 	private App app;
 	private DbDirectory directory;
 	private Map<String,ArrayList<LotusNotesDatabaseProperties>> treeMap = new TreeMap<String, ArrayList<LotusNotesDatabaseProperties>>();
 	private Tree tree;
 	private TreeItem treeItem;
 	private LotusNotesDatabaseProperties database;
 	
 	/**
 	 * Opens an InfoDialog and runs a task in background
 	 * the task searches for all databases on the server,
 	 * creates a tree containing all databases and displays
 	 * it in a DatabaseDialog 
 	 * 
 	 * @author Ralph Plawetzki
 	 * @param app
 	 */
 	public SelectDatabaseThread(App app) {
 		super();
 		this.app = app;
 	}
 
 	@Override
 	public void run() {
 		
 		NotesThread.sinitThread();
 	
 		// open InfoDialog
 		app.getDisplay().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				infoDialog = new InfoDialog(app, SWT.BORDER | SWT.TITLE | SWT.APPLICATION_MODAL, "Reading and parsing database directory...");
 				infoDialog.open();
 			}
 		});
 
 		// create SelectDatabaseDialog which contains Tree
 		app.getDisplay().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				selectDBDialog = new SelectDatabaseDialog(app, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
 				tree = selectDBDialog.getTree();
 			}
 		});
 		
 		// depending on OS use different separators between directories
 		String seperator;
 		if (System.getProperties().getProperty("os.name").contains("Windows")) {
 			seperator = "\\\\";
 		} else {
 			seperator = "/";
 		}
 		
 		// get all databases from DbDirectory on server
 		// and fill treeMap accordingly
 		try {
 			final Session session = app.getSession();
 			directory = session.getDbDirectory(session.getEnvironmentString("MailServer", true));
 			Database nextDB = directory.getFirstDatabase(DbDirectory.DATABASE);
 			
 			while (nextDB !=null && !nextDB.equals("")) {
 				String [] devided = nextDB.getFilePath().split(seperator);
 				List<String> devidedAsList = new ArrayList<String>(); 
 				devidedAsList.addAll(Arrays.asList(devided));
 				
 				String path = "";
 				while (devidedAsList.size() > 1) {
 					path += devidedAsList.get(0) + "/";
 					devidedAsList.remove(0);
 				}
 				
 				// this is a database in the root-directory of the server
 				// set path to something that sorts these databases to the end of the tree
 				if (path.equals("")) { path = "zzzzzzzzzzzzzzzzzzzzzzzzzzzz";}  
 				ArrayList<LotusNotesDatabaseProperties> list = new ArrayList<LotusNotesDatabaseProperties>();
 				if (treeMap.containsKey(path)) {
 					list = treeMap.get(path);
 				}
 				list.add(new LotusNotesDatabaseProperties(nextDB.getTitle(), nextDB.getFilePath()));
 				treeMap.put(path, list);
 				
 				nextDB = directory.getNextDatabase();
 			}
 			
 			// sort databases in directories
 			for (String uniquePath : treeMap.keySet()) {
 				ArrayList<LotusNotesDatabaseProperties> unsorted = treeMap.get(uniquePath);
 				Sorter sort = new Sorter();
 				Collections.sort(unsorted, sort);
 			}
 	
 			// now iterate over treeMap and fill Tree with contents of treeMap
 			for (String uniquePath : treeMap.keySet()) {
 				String[] devided = uniquePath.split("/");
 				List<String> devidedAsList = new ArrayList<String>();
 				devidedAsList.addAll(Arrays.asList(devided));
 	
 				final String item = devidedAsList.get(0);
 				if (item.equals("zzzzzzzzzzzzzzzzzzzzzzzzzzzz")) {
 					app.getDisplay().asyncExec(new Runnable() {
 						@Override
 						public void run() {
 							ArrayList<LotusNotesDatabaseProperties> databases = new ArrayList<LotusNotesDatabaseProperties>();
 							databases = treeMap.get(item);
 							for (final LotusNotesDatabaseProperties db : databases) {
 								TreeItem i = new TreeItem(tree, SWT.NONE);
 								i.setText(db.getTitle());
 								i.setImage(SWTResourceManager.getImage(SelectDatabaseThread.class, "/org/purejava/lnae/images/Logo_klein.png"));
 								i.setData(db.getFullPath());
 							}
 						}
 					});
 					continue;
 				}
 				app.getDisplay().asyncExec(new Runnable() {
 					@Override
 					public void run() {
 						treeItem = getOrCreateItem(tree, item);
 					}
 				});
 				
 				devidedAsList.remove(0);
 	
 				// ... some more directories to process
 				while (!devidedAsList.isEmpty()) {
 					final String item1 = devidedAsList.get(0);
 					app.getDisplay().asyncExec(new Runnable() {
 						@Override
 						public void run() {
 							treeItem = getOrCreateItem(treeItem, item1);
 						}
 					});
 					devidedAsList.remove(0);
 				}
 				
 				// we reached the final directory that contains only databases
 				ArrayList<LotusNotesDatabaseProperties> databases = new ArrayList<LotusNotesDatabaseProperties>();
 				databases = treeMap.get(uniquePath);
 				for (final LotusNotesDatabaseProperties db : databases) {
 					app.getDisplay().asyncExec(new Runnable() {
 						@Override
 						public void run() {
 							TreeItem i = new TreeItem(treeItem, SWT.NONE);
 							i.setText(db.getTitle());
 							i.setImage(SWTResourceManager.getImage(SelectDatabaseThread.class, "/org/purejava/lnae/images/Logo_klein.png"));
 							i.setData(db.getFullPath());
 						}
 					});
 				}
 			}
 			
 			// if a TreeItem gets selected, display info
 			// for the database associated with this TreeItem
 			app.getDisplay().asyncExec(new Runnable() {
 				@Override
 				public void run() {
 					tree.addListener(SWT.Selection, new Listener() {
 	
 						@Override
 						public void handleEvent(Event event) {
 							TreeItem item = (TreeItem) event.item;
 							if (item.getData() != null) {
 								try {
 									selectDBDialog.setCursor(SWT.CURSOR_WAIT);
 									Database lnDB = session.getDatabase(session.getEnvironmentString("MailServer", true), (String) item.getData());
 									selectDBDialog.setLabelDatabaseTitle(item.getText());
 									Double size = lnDB.getSize();
 									long sizeAsLong = size.longValue();
 									selectDBDialog.setDatabaseSize(sizeAsLong);
 									String sizeAsText = FileSizeCalc.getReadableFileSize(sizeAsLong);
 									selectDBDialog.setLabelDatabaseSize(sizeAsText);
 									DocumentCollection dc = lnDB.getAllDocuments();
 									selectDBDialog.setLabelDocCount(Integer.toString(dc.getCount()));
 									selectDBDialog.setFullPath((String) item.getData());
 									selectDBDialog.setCursor(SWT.CURSOR_ARROW);
 								} catch (NotesException e) {
 									MessageBox mb = new MessageBox(selectDBDialog.getShell(), SWT.ICON_ERROR | SWT.OK);
 									mb.setText("Lotus Notes Error");
 									mb.setMessage(e.text);
 									mb.open();
 								}
 							}
 						}
 	
 					});
 				}
 			});
 			
 			// close InfoDialog
 			app.getDisplay().asyncExec(new Runnable() {
 				@Override
 				public void run() {
 					infoDialog.getShell().dispose();
 				}
 			});
 			
 			// open SelectDatabaseDialog and get the database it returns
 			app.getDisplay().asyncExec(new Runnable() {
 				@Override
 				public void run() {
 					database = selectDBDialog.open();
 					if (database != null) {
 						app.getApplicationGUI().setLabelDatabase(database.getTitle());
 						app.getApplicationGUI().setLabelMb(database.getSizeAsText());
 						app.getApplicationGUI().setLabelDC(database.getDocCount());
 						app.setFullPath(database.getFullPath());
 						app.setDatabaseSize(database.getDbSize());
 						app.getApplicationGUI().setBtnExtractAttachments(true);
 					}
 				}
 			});
 			
		} catch (NotesException e) {
			if (! infoDialog.getShell().isDisposed()) infoDialog.getShell().dispose();
			if (! selectDBDialog.getShell().isDisposed()) selectDBDialog.getShell().dispose();
			MessageBox mb = new MessageBox(app, SWT.ICON_ERROR | SWT.OK);
			mb.setText("Lotus Notes Error");
			mb.setMessage(e.text);
			mb.open();
 		} finally {
 			NotesThread.stermThread();
 		}
 	}
 	
 	/**
 	 * Retrieves TreeItem from a given node
 	 * (which is either Tree or TreeItem) identified by given text
 	 * 
 	 * @param node Tree or TreeItem to be searched
 	 * @param text that specifies the TreeItem to be searched
 	 * @return the found TreeItem, if found in node or a new TreeItem otherwise
 	 */
 	private <T> TreeItem getOrCreateItem(T node, String text) {
 		TreeItem ti = null;
 		TreeItem [] tis = null;
 		if (node instanceof Tree) {
 			tis = ((Tree) node).getItems();
 		} else {
 			tis = ((TreeItem) node).getItems();
 		} 
 		boolean found = false;
 		
 		for (int j=0; j<tis.length; j++) {
 			if (tis[j].getText().equals(text)) {
 				found = true; ti = tis[j]; break;
 			}
 		}
 		if (!found) {
 			if (node instanceof Tree) {
 				ti = new TreeItem((Tree) node, SWT.NONE);
 			} else {
 				ti = new TreeItem((TreeItem) node, SWT.NONE);
 			}
 			ti.setText(text);
 			ti.setImage(SWTResourceManager.getImage(SelectDatabaseThread.class, "/org/purejava/lnae/images/folder-20110506.png"));
 		}
 		return ti;
 	}
 	
 	/**
 	 * Class to make sorting LotusNotesDatabaseProperties possible
 	 */
 	class Sorter implements Comparator<LotusNotesDatabaseProperties> {
 		@Override
 		public int compare(LotusNotesDatabaseProperties one, LotusNotesDatabaseProperties two) {
 			return one.getTitle().compareTo(two.getTitle());
 		}
 	}
 }
