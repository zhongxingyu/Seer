 package com.maton.tools.stiletto.view;
 
 import java.io.File;
 import java.util.HashMap;
 
 import org.eclipse.jface.window.ApplicationWindow;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CTabFolder;
 import org.eclipse.swt.custom.CTabFolder2Adapter;
 import org.eclipse.swt.custom.CTabFolderEvent;
 import org.eclipse.swt.custom.CTabItem;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.FileDialog;
 
 public class BundleContainer {
 
 	private static BundleContainer INSTANCE;
 
 	protected ApplicationWindow window;
 	protected CTabFolder container;
 	protected HashMap<CTabItem, BundleEditor> bundles;
 	protected BundleEditor current;
 
 	public BundleContainer(ApplicationWindow window, Composite parent) {
 		if (INSTANCE != null) {
 			throw new RuntimeException(
 					"Only one BundleContainer can be created");
 		}
 
 		bundles = new HashMap<CTabItem, BundleEditor>();
 
 		this.window = window;
 		container = new CTabFolder(parent, SWT.BORDER);
 
 		current = null;
 
 		build();
 	}
 
 	protected void build() {
 		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 		container.setSimple(false);
 		container.setUnselectedImageVisible(false);
 		container.setUnselectedCloseVisible(false);
 
 		container.addSelectionListener(new SelectionListener() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				selectionTab((CTabItem) e.item);
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 				selectionTab((CTabItem) e.item);
 			}
 		});
 
 		container.addCTabFolder2Listener(new CTabFolder2Adapter() {
 			@Override
 			public void close(CTabFolderEvent event) {
 				disposeTab((CTabItem) event.item);
 			}
 		});
 
 		INSTANCE = this;
 	}
 
 	protected void selectionTab(CTabItem item) {
 		current = bundles.get(item);
 
 		if (current == null) {
 			Toolbar.getInstance().enable(false);
 		} else {
 			Toolbar.getInstance().enable(true);
 		}
 	}
 
 	public CTabFolder getContainer() {
 		return container;
 	}
 
 	public static BundleContainer getInstance() {
 		return INSTANCE;
 	}
 
 	public BundleEditor newBundle() {
 		FileDialog dialog = new FileDialog(container.getShell(), SWT.SAVE);
 		dialog.setText("Create a new Stiletto Bundle...");
 		dialog.setFilterNames(new String[] { "Stiletto Files (*.stil)" });
 		dialog.setFilterExtensions(new String[] { "*.stil" });
 		String fileName = dialog.open();
 
 		BundleEditor bundle = null;
 
 		if (fileName != null) {
 			File file = new File(fileName);
 			file.delete();
 
			current = new BundleEditor(container, file);
			bundle = current;
 			bundle.build();
 
 			container.setSelection(bundle.getItem());
 			bundles.put(bundle.getItem(), bundle);
 			selectionTab(bundle.getItem());
 		}
 
 		return bundle;
 	}
 
 	public BundleEditor[] openBundle() {
 		FileDialog dialog = new FileDialog(container.getShell(), SWT.MULTI);
 		dialog.setText("Open a Stiletto Bundle...");
 		dialog.setFilterNames(new String[] { "Stiletto Files (*.stil)" });
 		dialog.setFilterExtensions(new String[] { "*.stil" });
 		String opened = dialog.open();
 
 		BundleEditor[] bundle = null;
 
 		if (opened != null) {
 			File basePath = new File(opened).getParentFile();
 
 			String[] files = dialog.getFileNames();
 			bundle = new BundleEditor[files.length];
 			int idx = 0;
 			for (String f : files) {
 				File file = new File(basePath, f);
 
 				current = new BundleEditor(container, file);
 				current.build();
 				bundle[idx] = current;
 
 				container.setSelection(bundle[idx].getItem());
 				bundles.put(bundle[idx].getItem(), bundle[idx]);
 				selectionTab(bundle[idx].getItem());
 
 				idx++;
 			}
 		}
 
 		return bundle;
 	}
 
 	protected void disposeTab(CTabItem item) {
 		bundles.remove(item);
 		selectionTab(null);
 	}
 
 	public BundleEditor getCurrent() {
 		return current;
 	}
 }
