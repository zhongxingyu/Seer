 /******************************************************************************* 
  * Copyright (c) 2008 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Xavier Coulon - Initial API and implementation 
  ******************************************************************************/
 
 package org.jboss.tools.livereload.ui.internal.command;
 
 import java.net.InetAddress;
 import java.net.MalformedURLException;
 import java.net.SocketException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.GroupMarker;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.TitleAreaDialog;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.Clipboard;
 import org.eclipse.swt.dnd.TextTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.graphics.PaletteData;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Link;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.IWorkbenchActionConstants;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.wst.server.ui.IServerModule;
 import org.jboss.tools.livereload.core.internal.server.jetty.LiveReloadProxyServer;
 import org.jboss.tools.livereload.core.internal.util.Logger;
 import org.jboss.tools.livereload.core.internal.util.NetworkUtils;
 import org.jboss.tools.livereload.core.internal.util.WSTUtils;
 import org.jboss.tools.livereload.ui.internal.util.ImageRepository;
 
 import com.google.zxing.BarcodeFormat;
 import com.google.zxing.EncodeHintType;
 import com.google.zxing.common.BitMatrix;
 import com.google.zxing.qrcode.QRCodeWriter;
 import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
 
 /**
  * @author xcoulon
  * 
  */
 public class OpenInExternalDeviceWebBrowserViaQRCodeDialog extends TitleAreaDialog {
 
 	private static final Image TITLE_IMAGE = ImageRepository.getInstance().getImage("livereload_wiz.png");
 
 	private final IServerModule serverModule;
 
 	private Canvas qrcodeCanvas;
 
 	private Table networkInterfacesTable;
 
 	private Link locationLabel;
 
 	private String serverModuleURL = null;
 	
 	private Image qrcodeImage = null;
 
 	public OpenInExternalDeviceWebBrowserViaQRCodeDialog(final IServerModule module, final Shell parentShell) {
 		super(parentShell);
 		this.serverModule = module;
 	}
 
 	@Override
 	protected Point getInitialSize() {
 		return new Point(400, 600);
 	}
 
 	@Override
 	protected boolean isResizable() {
 		return true;
 	}
 
 	@Override
 	protected Control createContents(Composite parent) {
 		Control control = super.createContents(parent);
 		parent.getShell().setText(DialogMessages.QRCODE_DIALOG_NAME);
 		setTitle(DialogMessages.QRCODE_DIALOG_TITLE);
 		setTitleImage(TITLE_IMAGE);
 		setMessage(DialogMessages.QRCODE_DIALOG_MESSAGE);
 		setDialogHelpAvailable(false);
 		return control;
 	}
 
 	@Override
 	protected Control createDialogArea(final Composite parent) {
 		Label titleSeparator = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
 		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).applyTo(titleSeparator);
 		Composite dialogArea = new Composite(parent, SWT.NONE);
 		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).hint(300, 400).applyTo(dialogArea);
 		GridLayoutFactory.fillDefaults().margins(10, 10).applyTo(dialogArea);
 		// Table filled with network interfaces
 		createNetworkInterfacesTable(dialogArea);
 		// Server Module Location as QR Code and linked location
 		createModuleLocation(dialogArea);
 		Label buttonsSeparator = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
 		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).applyTo(buttonsSeparator);
 		return dialogArea;
 	}
 
 	private String computeURL(final TableItem[] tableItems) {
 		if (tableItems.length > 0) {
 			@SuppressWarnings("unchecked")
 			Entry<String, InetAddress> selectedNetworkInterface = (Entry<String, InetAddress>) tableItems[0].getData();
 			try {
 				final LiveReloadProxyServer liveReloadProxyServer = WSTUtils.findLiveReloadProxyServer(serverModule.getServer());
 				if(liveReloadProxyServer == null) {
 					return null;
 				}
 				final int proxyPort = liveReloadProxyServer.getProxyPort();
 				final String host = selectedNetworkInterface.getValue().getHostAddress();
 				URL url;
 				url = new URL("http", host, proxyPort, "/" + serverModule.getModule()[0].getName());
 				return url.toExternalForm();
 			} catch (MalformedURLException e) {
 				Logger.error("Failed to compute URL", e);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Creates an {@link TableViewer} containing all the {@link InetAddress}es
 	 * 
 	 * @param parent
 	 *            the parent container
 	 * @return
 	 */
 	private void createNetworkInterfacesTable(final Composite parent) {
 		this.networkInterfacesTable = new Table(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
 		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).hint(200, 100)
 				.applyTo(networkInterfacesTable);
 		networkInterfacesTable.setHeaderVisible(true);
 		final TableViewer networkInterfaceViewer = new TableViewer(networkInterfacesTable);
 		final TableColumn networkInterfaceNameColumn = new TableColumn(networkInterfacesTable, SWT.LEFT);
 		networkInterfacesTable.setLinesVisible(true);
 		networkInterfacesTable.setLinesVisible(true);
 		networkInterfaceNameColumn.setAlignment(SWT.LEFT);
 		networkInterfaceNameColumn.setText("Interface Name");
 		networkInterfaceNameColumn.setWidth(100);
 		TableColumn networkInterfaceAddressColumn = new TableColumn(networkInterfacesTable, SWT.RIGHT);
 		networkInterfaceAddressColumn.setAlignment(SWT.LEFT);
 		networkInterfaceAddressColumn.setText("IP Addresses");
 		networkInterfaceAddressColumn.setWidth(100);
 		networkInterfaceViewer.setContentProvider(new NetworkInterfacesContentProvider());
 		networkInterfaceViewer.setLabelProvider(new NetworkInterfacesLabelProvider());
 		networkInterfaceViewer.setInput(retrieveNetworkInterfaces());
 		networkInterfaceViewer.addSelectionChangedListener(new ISelectionChangedListener() {
 			@Override
 			public void selectionChanged(SelectionChangedEvent event) {
 				serverModuleURL = computeURL(networkInterfacesTable.getSelection());
 				if (qrcodeCanvas != null) {
 					qrcodeImage = null;
 					qrcodeCanvas.redraw();
 				}
 				if (locationLabel != null) {
 					locationLabel.setText(toHtmlAnchor(serverModuleURL));
 				}
 			}
 		});
 		if (networkInterfacesTable.getItemCount() > 0) {
 			networkInterfacesTable.setSelection(0);
 			this.serverModuleURL = computeURL(networkInterfacesTable.getSelection());
 		}
 	}
 
 	/**
 	 * Creates a {@link Canvas} widget which will contain the
 	 * {@link IServerModule} location based on the selected {@link InetAddress}
 	 * 
 	 * @param parent
 	 *            the parent container
 	 */
 	private void createModuleLocation(final Composite parent) {
 		final Display display = Display.getCurrent();
 		final Color whiteColor = display.getSystemColor(SWT.COLOR_WHITE);
 		final Color blackColor = display.getSystemColor(SWT.COLOR_BLACK);
 
 		final Composite locationContainer = new Composite(parent, SWT.BORDER);
 		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).hint(300, 300)
 				.applyTo(locationContainer);
 		GridLayoutFactory.fillDefaults().margins(0, 0).applyTo(locationContainer);
 		final GridLayout layout = new GridLayout();
 		locationContainer.setLayout(layout);
 		locationContainer.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
 		qrcodeCanvas = new Canvas(locationContainer, SWT.NULL);
 		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(qrcodeCanvas);
 		GridLayoutFactory.fillDefaults().margins(0, 0).applyTo(qrcodeCanvas);
 		qrcodeCanvas.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
 		qrcodeCanvas.addPaintListener(new PaintListener() {
 
 			public void paintControl(final PaintEvent event) {
 				try {
 					event.gc.setBackground(whiteColor);
 					event.gc.setForeground(blackColor);
 					event.gc.fillRectangle(0, 0, qrcodeCanvas.getSize().x, qrcodeCanvas.getSize().y);
 					final int qrcodeWidth = qrcodeCanvas.getSize().x;
 					final int qrcodeHeight = qrcodeCanvas.getSize().y;
 					final int horizontalOffset = (qrcodeWidth > qrcodeHeight) ? (qrcodeWidth - qrcodeHeight) / 2 : 0;
 					final int verticalOffset = (qrcodeHeight > qrcodeWidth) ? (qrcodeHeight - qrcodeWidth) / 2 : 0;
 					final int qrcodeSize = Math.min(qrcodeWidth, qrcodeHeight);
 					
 					if (serverModuleURL != null && (qrcodeImage == null || qrcodeSize != qrcodeImage.getBounds().height)) {
 						final Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();
 						hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
 						final QRCodeWriter qrCodeWriter = new QRCodeWriter();
 						final BitMatrix qrcodeMatrix = qrCodeWriter.encode(serverModuleURL, BarcodeFormat.QR_CODE, qrcodeSize,
 								qrcodeSize, hintMap);
 						final PaletteData palette = new PaletteData(255, 255, 255);
 						final ImageData imageData = new ImageData(qrcodeSize, qrcodeSize, 8, palette);
 						for (int i = 0; i < qrcodeSize; i++) {
 							for (int j = 0; j < qrcodeSize; j++) {
 								if (qrcodeMatrix.get(i, j)) {
 									imageData.setPixel(i, j, 0);
 								} else {
 									imageData.setPixel(i, j, 255);
 								}
 							}
 						}
 						qrcodeImage = new Image(display, imageData);
 					}
 					if (qrcodeImage != null) {
 						event.gc.drawImage(qrcodeImage, horizontalOffset, verticalOffset);
 					}
 				} catch (Exception e) {
 					Logger.error("Failed to generate QRCode", e);
 				}
 			}
 		});
 		
 		locationLabel = new Link(locationContainer, SWT.NONE);
 		locationLabel.setBackground(whiteColor);
 		if(serverModuleURL != null) {
 			locationLabel.setText(toHtmlAnchor(serverModuleURL));
 		}
 		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).span(1, 1).align(SWT.CENTER, SWT.CENTER).grab(true, false).applyTo(locationLabel);
		locationLabel.addListener(SWT.Selection, new LinkListener(serverModuleURL));
 		createContextMenu(locationLabel, serverModuleURL);
 		
 	}
 
 	/**
 	 * Generates an HTML Anchor that contains the given location
 	 * @return
 	 */
 	private static String toHtmlAnchor(final String location) {
 		if(location != null) {
 			return "<a href=\"" + location + "\">" + location + "</a>";
 		}
 		return null;
 	}
 
 	private void createContextMenu(final Control control, final String serverModuleURL) {
 		final MenuManager menuManager = new MenuManager();
 		menuManager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
 		Menu contextMenu = menuManager.createContextMenu(control);
 		control.setMenu(contextMenu);
		menuManager.add(new CopyToClipboardAction(serverModuleURL));
 	}
 	/**
 	 * @return
 	 * @throws SocketException
 	 */
 	private Map<String, InetAddress> retrieveNetworkInterfaces() {
 		try {
 			return NetworkUtils.retrieveNetworkInterfaces();
 		} catch (SocketException e) {
 			Logger.error("Failed to retrieve local network interfaces", e);
 			return Collections.emptyMap();
 		}
 	}
 
 	@Override
 	protected void createButtonsForButtonBar(Composite parent) {
 		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
 	}
 
 	static class NetworkInterfacesContentProvider implements IStructuredContentProvider {
 
 		private Map<String, InetAddress> networkInterfaces;
 
 		@SuppressWarnings("unchecked")
 		@Override
 		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 			this.networkInterfaces = (Map<String, InetAddress>) newInput;
 		}
 
 		@Override
 		public Object[] getElements(Object inputElement) {
 			final List<Entry<String, InetAddress>> entries = new ArrayList<Map.Entry<String, InetAddress>>(
 					networkInterfaces.entrySet());
 			Collections.sort(entries, new Comparator<Entry<String, InetAddress>>() {
 
 				@Override
 				public int compare(Entry<String, InetAddress> entry, Entry<String, InetAddress> otherEntry) {
 					return entry.getKey().compareTo(otherEntry.getKey());
 				}
 
 			});
 			return entries.toArray();
 		}
 
 		@Override
 		public void dispose() {
 		}
 	}
 
 	static class NetworkInterfacesLabelProvider implements ITableLabelProvider {
 
 		@Override
 		public String getColumnText(Object element, int columnIndex) {
 			if (element instanceof Entry<?, ?>) {
 				@SuppressWarnings("unchecked")
 				final Entry<String, InetAddress> entry = (Entry<String, InetAddress>) element;
 				switch (columnIndex) {
 				case 0:
 					return entry.getKey().toString();
 				case 1:
 					return entry.getValue().getHostAddress();
 				}
 			}
 			return null;
 		}
 
 		@Override
 		public Image getColumnImage(Object element, int columnIndex) {
 			return null;
 		}
 
 		@Override
 		public void addListener(ILabelProviderListener listener) {
 		}
 
 		@Override
 		public void dispose() {
 		}
 
 		@Override
 		public boolean isLabelProperty(Object element, String property) {
 			return false;
 		}
 
 		@Override
 		public void removeListener(ILabelProviderListener listener) {
 		}
 
 	}
 	
	static class LinkListener implements Listener {
		
		private final String url;
		
		public LinkListener(final String url) {
			this.url = url;
		}
 		public void handleEvent(Event event) {
 			try {
				OpenInWebBrowserViaLiveReloadUtils.openInBrowser(new URL(url));
 			} catch (Exception e) {
				Logger.error("Failed to open URL '" + url + "' in an external Browser", e);
 			}
 		}
 	}
 	
	static class CopyToClipboardAction extends Action {
		
		private final String serverModuleURL;
 		
		public CopyToClipboardAction(final String serverModuleURL) {
 			super("Copy", PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
			this.serverModuleURL = serverModuleURL;
 //			setActionDefinitionId(ActionFactory.COPY.getCommandId());
 //			setAccelerator(SWT.CTRL | 'C');
 		}
 
 		@Override
 		public void run() {
 			Clipboard clipboard = new Clipboard(Display.getDefault());
 			clipboard.setContents(
 					new Object[] { serverModuleURL },
 					new Transfer[] { TextTransfer.getInstance() });
 		}
 		
 	}
 
 }
