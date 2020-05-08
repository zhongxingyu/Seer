 package org.sammelbox.controller.menu;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.Map;
 import java.util.Scanner;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.widgets.FileDialog;
 import org.sammelbox.controller.filesystem.FileSystemAccessWrapper;
 import org.sammelbox.controller.i18n.Translator;
 import org.sammelbox.controller.managers.BuildInformationManager;
 import org.sammelbox.view.ApplicationUI;
 import org.sammelbox.view.browser.BrowserFacade;
 import org.sammelbox.view.browser.BrowserUtils;
 import org.sammelbox.view.sidepanes.EmptySidepane;
 import org.sammelbox.view.various.ComponentFactory;
 import org.sammelbox.view.various.PanelType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public final class HelpMenuItemListener {
 	private static final Logger LOGGER = LoggerFactory.getLogger(HelpMenuItemListener.class);
 	
 	private HelpMenuItemListener() {
 		// not needed
 	}
 	
 	static SelectionAdapter getDumpHTMLListener() {
 		return new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {				
 				FileDialog saveFileDialog = new FileDialog(ApplicationUI.getShell(), SWT.SAVE);
 				// Debugging -> no need to translate
 				saveFileDialog.setText("Dump HTML");
 				saveFileDialog.setFilterPath(System.getProperty("user.home"));
 				String[] filterExt = { "*.html" };
 				saveFileDialog.setFilterExtensions(filterExt);
 				
 				String filepath = saveFileDialog.open();
 				if (filepath != null) {
 					FileSystemAccessWrapper.writeToFile(BrowserUtils.getLastPageAsHtml(), filepath);
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
 				BrowserFacade.showHelpPage();
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
 				Map<String, String> templateContent = BrowserFacade.getInitializedContentMap();
 				templateContent.put("HEADER", Translator.toBeTranslated("License"));
 				templateContent.put("MESSAGE", Translator.toBeTranslated("Sammelbox: Collection Manager</b> A free and open-source collection manager for Windows & Linux</b>Copyright (C) 2011 Jerome Wagener & Paul Bicheler"));
 				BrowserFacade.fillAndLoadTemplate("about.html", templateContent);
 				ApplicationUI.changeRightCompositeTo(PanelType.HELP, EmptySidepane.build(ApplicationUI.getThreePanelComposite()));
 			}
 		};
 	}
 
 	public static SelectionListener getShowBrowserInfoListener() {
 		return new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				ApplicationUI.refreshAlbumList();
 				BrowserFacade.loadHtmlFile("browserinfo.html");
 				ApplicationUI.changeRightCompositeTo(PanelType.HELP, EmptySidepane.build(ApplicationUI.getThreePanelComposite()));
 			}
 		};
 	}
 
 	public static SelectionListener getCheckForUpdatesListener() {		
 		return new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				try (Scanner versionScanner = new Scanner(new URL("http://www.sammelbox.org/current.php").openStream(), "UTF-8").useDelimiter("\\A")) {
 					String currentVersion = versionScanner.next();
 					
					System.out.println(currentVersion);
					
 					if (BuildInformationManager.instance().getPublicVersionString().trim().equals(currentVersion.trim())) {
 						ComponentFactory.getMessageBox(
 								Translator.toBeTranslated("Current version installed"), 
 								Translator.toBeTranslated("It looks like you have the current version of Sammelbox installed. Great!"),
 								SWT.ICON_INFORMATION).open();
 					} else {
 						ComponentFactory.getMessageBox(
 								Translator.toBeTranslated("Sammelbox is out-of-date"), 
 								Translator.toBeTranslated("It looks like a newer version of Sammelbox is available. You can download it from www.sammelbox.org"),
 								SWT.ICON_INFORMATION).open();
 					}
 				} catch (IOException ioe) {
 					LOGGER.error("An error occured while checking for updates.", ioe);
 					
 					ComponentFactory.getMessageBox(
 							Translator.toBeTranslated("Could not connect to www.sammelbox.org"), 
 							Translator.toBeTranslated("Please check again later, or go directly to www.sammelbox.org to check for updates!"),
 							SWT.ICON_INFORMATION).open();
 				}
 			}
 		};
 	}
 }
