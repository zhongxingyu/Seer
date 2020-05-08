 package org.sammelbox.controller.menu;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.widgets.FileDialog;
 import org.sammelbox.controller.filesystem.FileSystemAccessWrapper;
 import org.sammelbox.controller.i18n.Translator;
 import org.sammelbox.view.ApplicationUI;
 import org.sammelbox.view.browser.BrowserFacade;
 import org.sammelbox.view.browser.Utilities;
 import org.sammelbox.view.sidepanes.EmptySidepane;
 import org.sammelbox.view.various.PanelType;
 
 public final class HelpMenuItemListener {
 	private HelpMenuItemListener() {
 		// not needed
 	}
 	
 	static SelectionAdapter getDumpHTMLListener() {
 		return new SelectionAdapter() {
 			@Override
			public void widgetSelected(SelectionEvent arg0) {				
 				FileDialog saveFileDialog = new FileDialog(ApplicationUI.getShell(), SWT.SAVE);
 				saveFileDialog.setText(Translator.toBeTranslated("Dump HTML"));
 				saveFileDialog.setFilterPath(System.getProperty("user.home"));
 				String[] filterExt = { "*.html" };
 				saveFileDialog.setFilterExtensions(filterExt);
 				
 				String filepath = saveFileDialog.open();
 				if (filepath != null) {
 					FileSystemAccessWrapper.writeToFile(Utilities.getLastPageAsHtml(), filepath);
 				}
 			}
 		};
 	}
 	
 	static SelectionAdapter getHelpContentsListener() {
 		return new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				// No default album is selected on help
 				ApplicationUI.refreshAlbumList();
 				BrowserFacade.loadHtmlFromInputStream(
 						ApplicationUI.getShell().getClass().getClassLoader().getResourceAsStream("htmlfiles/help.html"));
 				ApplicationUI.changeRightCompositeTo(PanelType.HELP, EmptySidepane.build(ApplicationUI.getThreePanelComposite()));
 			}
 		};
 	}
 	
 	static SelectionAdapter getAboutListener() {
 		return new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				// No default album is selected on help
 				ApplicationUI.refreshAlbumList();
 				BrowserFacade.loadHtmlFromInputStream(
 						ApplicationUI.getShell().getClass().getClassLoader().getResourceAsStream("htmlfiles/about.html"));
 				ApplicationUI.changeRightCompositeTo(PanelType.HELP, EmptySidepane.build(ApplicationUI.getThreePanelComposite()));
 			}
 		};
 	}
 
 	public static SelectionListener getShowBrowserInfoListener() {
 		return new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				ApplicationUI.refreshAlbumList();
 				BrowserFacade.loadHtmlFromInputStream(
 						ApplicationUI.getShell().getClass().getClassLoader().getResourceAsStream("htmlfiles/browserinfo.html"));
 				ApplicationUI.changeRightCompositeTo(PanelType.HELP, EmptySidepane.build(ApplicationUI.getThreePanelComposite()));
 			}
 		};
 	}
 }
