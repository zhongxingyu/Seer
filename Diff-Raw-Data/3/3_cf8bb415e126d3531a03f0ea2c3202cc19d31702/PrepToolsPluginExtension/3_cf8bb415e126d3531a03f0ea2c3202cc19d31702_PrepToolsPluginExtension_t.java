 package ch.sbs.plugin.preptools;
 
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.AbstractAction;
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.JComponent;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JRadioButtonMenuItem;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.SwingUtilities;
 import javax.swing.event.CaretEvent;
 import javax.swing.event.CaretListener;
 import javax.swing.text.Document;
 
 import ro.sync.exml.editor.EditorPageConstants;
 import ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension;
 import ro.sync.exml.workspace.api.editor.WSEditor;
 import ro.sync.exml.workspace.api.editor.page.text.WSTextEditorPage;
 import ro.sync.exml.workspace.api.listeners.WSEditorChangeListener;
 import ro.sync.exml.workspace.api.standalone.MenuBarCustomizer;
 import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
 import ro.sync.exml.workspace.api.standalone.ToolbarComponentsCustomizer;
 import ro.sync.exml.workspace.api.standalone.ToolbarInfo;
 import ro.sync.exml.workspace.api.standalone.ViewComponentCustomizer;
 import ro.sync.exml.workspace.api.standalone.ViewInfo;
 import ro.sync.ui.Icons;
 import ch.sbs.plugin.preptools.DocumentMetaInfo.MetaInfo;
 import ch.sbs.utils.preptools.FileUtils;
 import ch.sbs.utils.preptools.Match.PositionMatch;
 import ch.sbs.utils.preptools.PropsUtils;
 
 /**
  * Plugin extension - workspace access extension.
  * NOTE: the build file expects this class to be called ...Extension.
  * 
  */
 public class PrepToolsPluginExtension implements WorkspaceAccessPluginExtension {
 
 	private List<PrepTool> prepTools;
 
 	/**
 	 * A method to support DocumentMetaInfo's independence of specific
 	 * preptools.
 	 * 
 	 * @param document
 	 * @return
 	 */
 	Map<String, MetaInfo> getToolSpecificMetaInfos(final Document document) {
 		final Map<String, MetaInfo> toolSpecific = new HashMap<String, MetaInfo>();
 		for (final PrepTool preptool : prepTools) {
 			toolSpecific.put(preptool.getLabel(),
 					preptool.makeMetaInfo(document));
 		}
 		return toolSpecific;
 	}
 
 	private void disableAllActions(final DocumentMetaInfo theDocumentMetaInfo) {
 		setAllActions(theDocumentMetaInfo, false);
 	}
 
 	private void setAllActions(final DocumentMetaInfo theDocumentMetaInfo,
 			final boolean enabled) {
 		if (theDocumentMetaInfo != null) {
 			theDocumentMetaInfo.getCurrentPrepTool().setAllActionsEnabled(
 					enabled);
 		}
 	}
 
 	private void populatePrepTools() {
 		prepTools = PrepToolLoader.loadPrepTools(this);
 	}
 
 	private static void setBold(final JMenuItem item) {
 		item.setFont(item.getFont().deriveFont(Font.BOLD));
 	}
 
 	private static void setPlain(final JMenuItem item) {
 		item.setFont(item.getFont().deriveFont(Font.PLAIN));
 	}
 
 	void setPrepToolItemDone(final JMenuItem item) {
 		setPlain(item);
 	}
 
 	void disableMenuPrepTools() {
 		menuPrepTools.setEnabled(false);
 	}
 
 	void enableMenuPrepTools() {
 		menuPrepTools.setEnabled(true);
 	}
 
 	void setPrepToolItemDone(int i) {
 		setPrepToolItemDone(menuPrepTools.getItem(i));
 	}
 
 	void setPrepToolItemNormal(final JMenuItem item) {
 		setBold(item);
 	}
 
 	void setPrepToolItemNormal(int i) {
 		setPrepToolItemNormal(menuPrepTools.getItem(i));
 	}
 
 	void updatePrepToolItems() {
 		int i = 0;
 		for (final PrepTool preptool : prepTools) {
 			if (getDocumentMetaInfo().getToolSpecificMetaInfo(
 					preptool.getLabel()).isDone()) {
 				setPrepToolItemDone(i);
 			}
 			else {
 				setPrepToolItemNormal(i);
 			}
 			i++;
 		}
 	}
 
 	void selectPrepToolItem(int i) {
 		menuPrepTools.getItem(i).setSelected(true);
 	}
 
 	private PrepTool getDefaultPrepTool() {
 		return prepTools.get(0);
 	}
 
 	JPanel toolbarPanel;
 
 	/**
 	 * The PrepTools messages area.
 	 */
 	private JTextArea prepToolsMessagesArea;
 
 	/**
 	 * Plugin workspace access.
 	 */
 	private StandalonePluginWorkspace pluginWorkspaceAccess;
 
 	/**
 	 * If <code>true</code> then the plugin is running.
 	 */
 	private boolean runPlugin;
 
 	private boolean applicationClosing;
 
 	private MyCaretListener caretHandler;
 
 	private JMenu menuPrepTools;
 
 	/**
 	 * @see ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension#applicationStarted(ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace)
 	 */
 	@Override
 	public void applicationStarted(
 			final StandalonePluginWorkspace thePluginWorkspaceAccess) {
 		pluginWorkspaceAccess = thePluginWorkspaceAccess;
 		final URL resource = getClass().getResource(
 				"/" + getClass().getName().replace('.', '/') + ".class");
 		runPlugin = "jar".equals(resource.getProtocol()) ? true : System
 				.getProperty("cms.sample.plugin") != null;
 		if (runPlugin) {
 
 			caretHandler = new MyCaretListener();
 
 			populatePrepTools();
 
 			pluginWorkspaceAccess.addMenuBarCustomizer(new MenuBarCustomizer() {
 				/**
 				 * @see ro.sync.exml.workspace.api.standalone.MenuBarCustomizer#customizeMainMenu(javax.swing.JMenuBar)
 				 */
 				@Override
 				public void customizeMainMenu(final JMenuBar mainMenuBar) {
 					// PrepTools menu
 					menuPrepTools = createPrepToolsMenu();
 					menuPrepTools.setMnemonic(KeyEvent.VK_R);
 					// Add the Preptools menu before the Help menu
 					mainMenuBar.add(menuPrepTools,
 							mainMenuBar.getMenuCount() - 1);
 				}
 			});
 
 			pluginWorkspaceAccess.addEditorChangeListener(
 					new WSEditorChangeListener() {
 
 						// explore modal dialog instead of toolbar:
 						// http://www.javacoffeebreak.com/faq/faq0019.html
 
 						@Override
 						public void editorOpened(URL editorLocation) {
 							final WSEditor editorAccess = getWsEditor();
 							final WSTextEditorPage page = getPage(editorAccess);
 							if (page == null) {
 								final boolean isText = EditorPageConstants.PAGE_TEXT
 										.equals(editorAccess.getCurrentPageID());
 								showMessage("document could not be accessed "
 										+ (isText ? "(unknown reason)"
 												: "(current page is not "
 														+ EditorPageConstants.PAGE_TEXT
 														+ " but "
 														+ editorAccess
 																.getCurrentPageID()
 														+ ")"));
 								return;
 							}
 							// we don't do much else here since in every case
 							// editorSelected will be called after editorOpened.
 
 							// addCaretHandler();
 						}
 
 						@SuppressWarnings("unused")
 						private void addCaretHandler() {
 							final JTextArea ta = getJTextArea();
 							ta.addCaretListener(caretHandler);
 						}
 
 						@Override
 						public void editorClosed(final URL editorLocation) {
 							final DocumentMetaInfo dmi = getDocumentMetaInfo(editorLocation);
 							if (dmi.isProcessing() && !applicationClosing) {
 								// we can't veto closing!
 								if (showConfirmDialog(
 										"v-form: Continue?",
 										"Document "
 												+ FileUtils
 														.basename(editorLocation)
 												+ " was still being processed. Want to continue?")) {
 									SwingUtilities.invokeLater(new Runnable() {
 										@Override
 										public void run() {
 											pluginWorkspaceAccess
 													.open(editorLocation);
 											dmi.setPageAndDocument(PrepToolsPluginExtension.this);
 											dmi.setCurrentState();
 											final PositionMatch match = dmi
 													.getCurrentPositionMatch();
 											getPage()
 													.select(match.startOffset
 															.getOffset(),
 															match.endOffset
 																	.getOffset());
 											dmi.resetManualEdit();
 										}
 									});
 
 								}
 								else {
 									removeDocumentMetaInfo(dmi);
 								}
 							}
 							else {
 								removeDocumentMetaInfo(dmi);
 							}
 						};
 
 						@Override
 						public void editorPageChanged(URL editorLocation) {
 							final DocumentMetaInfo dmi = getDocumentMetaInfo(editorLocation);
 							dmi.setCurrentEditorPage(getPageId());
 							dmi.setCurrentState();
 						};
 
 						@Override
 						public void editorSelected(URL editorLocation) {
 							final DocumentMetaInfo dmi = getDocumentMetaInfo(editorLocation);
 							if (dmi != null) {
 								PrepTool currentPrepTool = dmi
 										.getCurrentPrepTool();
 								if (currentPrepTool == null) {
 									currentPrepTool = getDefaultPrepTool();
 								}
 								updatePrepToolItems();
 								currentPrepTool.activate();
 							}
 						};
 					}, StandalonePluginWorkspace.MAIN_EDITING_AREA);
 
 			pluginWorkspaceAccess
 					.addToolbarComponentsCustomizer(new ToolbarComponentsCustomizer() {
 						/**
 						 * @see ro.sync.exml.workspace.api.standalone.ToolbarComponentsCustomizer#customizeToolbar(ro.sync.exml.workspace.api.standalone.ToolbarInfo)
 						 */
 						@Override
 						public void customizeToolbar(ToolbarInfo theToolbarInfo) {
 							if (ToolbarComponentsCustomizer.CUSTOM
 									.equals(theToolbarInfo.getToolbarID())) {
 
 								toolbarPanel = new JPanel();
 								toolbarPanel.setLayout(new BoxLayout(
 										toolbarPanel, BoxLayout.LINE_AXIS));
 								theToolbarInfo
 										.setComponents(new JComponent[] { toolbarPanel });
 								theToolbarInfo.setTitle("PrepTools");
 
 								SwingUtilities.invokeLater(new Runnable() {
 
 									@Override
 									public void run() {
 										getDefaultPrepTool().activate();
 									}
 								});
 							}
 						}
 					});
 
 			pluginWorkspaceAccess
 					.addViewComponentCustomizer(new ViewComponentCustomizer() {
 						/**
 						 * @see ro.sync.exml.workspace.api.standalone.ViewComponentCustomizer#customizeView(ro.sync.exml.workspace.api.standalone.ViewInfo)
 						 */
 						@Override
 						public void customizeView(ViewInfo viewInfo) {
 							if (ViewComponentCustomizer.CUSTOM.equals(viewInfo
 									.getViewID())) {
 								prepToolsMessagesArea = new JTextArea(
 										"PrepTools Session History:");
 								viewInfo.setComponent(new JScrollPane(
 										prepToolsMessagesArea));
 								viewInfo.setTitle("PrepTools Messages");
 								viewInfo.setIcon(Icons.CMS_MESSAGES_CUSTOM_VIEW);
 								showMessage(getVersion());
 							}
 							else if ("Project".equals(viewInfo.getViewID())) {
 								viewInfo.setTitle("PrepTools Project");
 							}
 						}
 
 					});
 		}
 	}
 
 	@SuppressWarnings("serial")
 	protected JMenu createPrepToolsMenu() {
 		final JMenu menuPrepTools = new JMenu("PrepTools");
 		final ButtonGroup group = new ButtonGroup();
 
 		for (final PrepTool preptool : prepTools) {
 			final JMenuItem item = new JRadioButtonMenuItem(
 					new AbstractAction() {
 
 						@Override
 						public void actionPerformed(ActionEvent e) {
 							final MetaInfo metaInfo = getDocumentMetaInfo()
 									.getCurrentToolSpecificMetaInfo();
 							final PrepTool currentPrepTool = getDocumentMetaInfo()
 									.getCurrentPrepTool();
 							final String label = currentPrepTool.getLabel();
 							if (preptool != currentPrepTool
 									&& (!metaInfo.isProcessing() || showConfirmDialog(
 											"PrepTools",
 											"Not Yet Done Processing " + label
 													+ "!", "Switch Anyway",
 											"Cancel"))) {
 								preptool.activate();
 							}
 							else {
 								// not the smartest way, but since
 								// there won't be more than 20 preptools
 								// the linear search (indexOf) of
 								// the current tool should be bearable.
 								selectPrepToolItem(prepTools
 										.indexOf(currentPrepTool));
 							}
 						}
 					});
 			item.setText(preptool.getLabel());
 			item.setMnemonic(preptool.getMnemonic());
 			menuPrepTools.add(item);
 			group.add(item);
 		}
 
 		return menuPrepTools;
 	}
 
 	private final Map<URL, DocumentMetaInfo> documentMetaInfos = new HashMap<URL, DocumentMetaInfo>();
 
 	DocumentMetaInfo getDocumentMetaInfo() {
 		return getDocumentMetaInfo(getEditorLocation());
 	}
 
 	DocumentMetaInfo getDocumentMetaInfo(final URL editorLocation) {
 		if (documentMetaInfos.containsKey(editorLocation)) {
 			return documentMetaInfos.get(editorLocation);
 		}
 		if (getPage() == null) {
 			return null;
 		}
 
 		final DocumentMetaInfo documentMetaInformation = new DocumentMetaInfo(
 				this);
 
 		documentMetaInfos.put(editorLocation, documentMetaInformation);
 		return documentMetaInformation;
 	}
 
 	private void removeDocumentMetaInfo(final DocumentMetaInfo dmi) {
 		disableAllActions(dmi);
 		dmi.finish();
 		documentMetaInfos.remove(dmi.getUrl());
		if (documentMetaInfos.isEmpty()) {
			disableMenuPrepTools();
		}
 	}
 
 	private String getVersion() {
 		final String key = "stamp";
 		final String filename = "stamp.properties";
 		final String version = PropsUtils.loadForClass(this.getClass(),
 				filename).getProperty(key);
 		final String returnString;
 		if (version != null && version.length() > 0) {
 			returnString = version;
 		}
 		else {
 			returnString = "'" + key + "' not found in props file " + filename;
 		}
 
 		return returnString;
 	}
 
 	/**
 	 * @see ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension#applicationClosing()
 	 */
 	@Override
 	public boolean applicationClosing() {
 		final StringBuilder sb = new StringBuilder();
 		boolean showDialog = false;
 		if (runPlugin) {
 			for (final URL url : documentMetaInfos.keySet()) {
 				if (documentMetaInfos.get(url).isProcessing()) {
 					showDialog = true;
 					sb.append("\n- ");
 					sb.append(FileUtils.basename(url));
 				}
 			}
 
 			if (showDialog) {
 				sb.insert(0,
 						"The following documents are still being processed:");
 				sb.append("\n\nProceed with closing anyway?");
 				applicationClosing = showConfirmDialog("v-form: Close?",
 						sb.toString());
 				return applicationClosing;
 			}
 		}
 		applicationClosing = true;
 		return true;
 	}
 
 	void showMessage(final String msg) {
 		if (prepToolsMessagesArea != null) {
 			prepToolsMessagesArea.append("\n");
 			prepToolsMessagesArea.append(msg);
 			// crashes with NullPointer:
 			// if (pluginWorkspaceAccess != null) {
 			// pluginWorkspaceAccess.showView(ViewComponentCustomizer.CUSTOM,
 			// true);
 			// }
 		}
 	}
 
 	void showDialog(final String msg) {
 		pluginWorkspaceAccess.showInformationMessage(msg);
 		pluginWorkspaceAccess.showView(ViewComponentCustomizer.CUSTOM, true);
 	}
 
 	boolean showConfirmDialog(final String title, final String msg) {
 		return showConfirmDialog(title, msg, "Ok", "Cancel");
 	}
 
 	/**
 	 * @param title
 	 * @param msg
 	 * @param confirm
 	 * @param deny
 	 * @return true if user clicked "confirm", false if user clicked "deny"
 	 */
 	boolean showConfirmDialog(final String title, final String msg,
 			final String confirm, final String deny) {
 		return pluginWorkspaceAccess.showConfirmDialog(title, msg,
 				new String[] { confirm, deny }, new int[] { 0, 1 }) == 0;
 	}
 
 	public static WSTextEditorPage getPage(final WSEditor editorAccess) {
 		if (editorAccess == null) {
 			return null;
 		}
 		if (!(editorAccess.getCurrentPage() instanceof WSTextEditorPage)) {
 			// showDialog("This function is only available in the Text page, not the Author page.");
 			return null;
 		}
 		WSTextEditorPage aWSTextEditorPage = (WSTextEditorPage) editorAccess
 				.getCurrentPage();
 		return aWSTextEditorPage;
 	}
 
 	public WSTextEditorPage getPage() {
 		return getPage(getWsEditor());
 	}
 
 	WSEditor getWsEditor() {
 		final WSEditor editorAccess = pluginWorkspaceAccess
 				.getCurrentEditorAccess(StandalonePluginWorkspace.MAIN_EDITING_AREA);
 		return editorAccess;
 	}
 
 	URL getEditorLocation() {
 		return getWsEditor() != null ? getWsEditor().getEditorLocation() : null;
 	}
 
 	String getPageId() {
 		return getWsEditor().getCurrentPageID();
 	}
 
 	private JTextArea getJTextArea() {
 		final Object tc = getPage(getWsEditor()).getTextComponent();
 		if (!(tc instanceof JTextArea)) {
 			return null;
 		}
 		final JTextArea ta = (JTextArea) tc;
 		return ta;
 	}
 
 	/*
 	 * This is actually redundant to my own book-keeping in
 	 * ProceedAction.handleManualCursorMovement
 	 */
 	class MyCaretListener implements CaretListener {
 
 		@Override
 		public void caretUpdate(CaretEvent e) {
 			getDocumentMetaInfo().setManualEdit();
 		}
 
 	}
 }
