 package sia.ui;
 
 import java.awt.Color;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.eclipse.jface.window.ApplicationWindow;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.browser.Browser;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.ToolItem;
 import org.eclipse.swt.widgets.Tree;
 import sia.models.Contact;
 import sia.models.ContactAccount;
 import sia.models.Conversation;
 import sia.models.Message;
 import sia.ui.importui.ImportWizard;
 import sia.utils.Dictionaries;
 import org.eclipse.swt.widgets.TreeItem;
 import org.sormula.SormulaException;
 
 /**
  * 
  * @author Agnieszka Glabala
  * 
  */
 public class Start extends ApplicationWindow {
 	private Table conversationsTable;
 	private Composite composite;
 	private Tree contactsTree;
 	private Map<TreeItem, ContactAccount> mapContactAccount;
 	private Map<TreeItem, Contact> mapContact;
 	private List<Conversation> conversations;
 	private Browser conversationBrowser;
 	private Text contactsKeyword;
 	private Text messagesKeyword;
 	private boolean searchButton = false;
 	private String lastSearchContact = "a";
 	private String lastSearchMessage = "a";
 	private String orderbyConversation = "time";
 	private boolean asc = false;
 	public Start() {
 		super(null);
 	}
 
 	/**
 	 * Configure shell
 	 */
 	@Override
 	protected void configureShell(Shell shell) {
 		super.configureShell(shell);
 
 		shell.setSize(882, 557);
 		shell.setText("SIA - SMS and IM Archiver");
 	}
 
 	/**
 	 * Open the window.
 	 */
 	public void run() {
 		setBlockOnOpen(true);
 		open();
 		Display.getCurrent().dispose();
 	}
 
 	/**
 	 * Create contents of the window.
 	 */
 	protected Control createContents(Composite parent) {
 		composite = new Composite(parent, SWT.NONE);
 		composite.setLayout(new GridLayout(1, false));
 
 		// TOOLBAR
 		ToolBar toolBar = new ToolBar(composite, SWT.FLAT | SWT.RIGHT);
 		GridData gridData = new GridData(GridData.FILL_HORIZONTAL, GridData.VERTICAL_ALIGN_BEGINNING, true, false);
 		toolBar.setLayoutData(gridData);
 
 		ToolItem importButton = new ToolItem(toolBar, SWT.NONE);
 		importButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				ImportWizard importWizard = new ImportWizard();
 				// Instantiates the wizard container with the wizard and opens
 				// it
 				WizardDialog dialog = new WizardDialog(composite.getShell(), importWizard);
 				dialog.addPageChangingListener(importWizard);
 				dialog.addPageChangedListener(importWizard);
 				dialog.create();
 
 				dialog.getShell().setSize(dialog.getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT));
 				if (dialog.open() == 0) {
					lastSearchContact = "refreshed";
 					fillContactTree("");
 				}
 			}
 		});
 		importButton.setImage(sia.ui.org.eclipse.wb.swt.SWTResourceManager.getImage(Start.class,
 				"/sia/ui/resources/import.png"));
 		importButton.setText("Import");
 
 		ToolItem exportButton = new ToolItem(toolBar, SWT.NONE);
 		exportButton.setImage(sia.ui.org.eclipse.wb.swt.SWTResourceManager.getImage(Start.class,
 				"/sia/ui/resources/export.png"));
 		exportButton.setText("Export");
 
 		ToolItem preferencesButton = new ToolItem(toolBar, SWT.NONE);
 		preferencesButton.setImage(sia.ui.org.eclipse.wb.swt.SWTResourceManager.getImage(Start.class,
 				"/sia/ui/resources/properties.png"));
 		preferencesButton.setText("Preferences");
 
 		ToolItem synchronizeButton = new ToolItem(toolBar, SWT.NONE);
 		synchronizeButton.setImage(sia.ui.org.eclipse.wb.swt.SWTResourceManager.getImage(Start.class,
 				"/sia/ui/resources/sync.png"));
 		synchronizeButton.setText("Synchronize");
 		// END TOOLBAR
 
 		gridData = new GridData(GridData.FILL, GridData.FILL, false, true);
 
 		// MIDDLE COMPOSITE - SASHFORM
 		SashForm sashForm = new SashForm(composite, SWT.NONE);
 		sashForm.setLayoutData(gridData);
 		// END MIDDLE COMPOSITE
 
 		// LEFT COMPOSITE
 		Composite compositeLeft = new Composite(sashForm, SWT.NONE);
 		compositeLeft.setLayout(new GridLayout(2, false));
 
 		if (!searchButton) {
 			Label sL = new Label(compositeLeft, SWT.NONE);
 			sL.setText("Search: ");
 		}
 		contactsKeyword = new Text(compositeLeft, SWT.SINGLE | SWT.BORDER);
 		GridData gd_contactsKeyword = new GridData(GridData.FILL_HORIZONTAL);
 		gd_contactsKeyword.verticalAlignment = SWT.FILL;
 		contactsKeyword.setLayoutData(gd_contactsKeyword);
 		contactsKeyword.addModifyListener(new ModifyListener() {
 
 			@Override
 			public void modifyText(ModifyEvent arg0) {
 				fillContactTree(contactsKeyword.getText());
 			}
 		});
 		if (searchButton) {
 			Button contactsSearch = new Button(compositeLeft, SWT.PUSH);
 			contactsSearch.setImage(sia.ui.org.eclipse.wb.swt.SWTResourceManager.getImage(Start.class,
 					"/sia/ui/resources/find.png"));
 			contactsSearch.setText("Search");
 			contactsSearch.addSelectionListener(new SelectionAdapter() {
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					fillContactTree(contactsKeyword.getText());
 				}
 			});
 		}
 
 		ScrolledComposite contactsScrolledComposite = new ScrolledComposite(compositeLeft, SWT.BORDER | SWT.H_SCROLL
 				| SWT.V_SCROLL);
 		contactsScrolledComposite.setExpandHorizontal(true);
 		contactsScrolledComposite.setExpandVertical(true);
 		gridData = new GridData(GridData.FILL_BOTH);
 		gridData.horizontalSpan = 2;
 		contactsScrolledComposite.setLayoutData(gridData);
 
 		contactsTree = new Tree(contactsScrolledComposite, SWT.BORDER);
 		contactsTree.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event e) {
 				TreeItem[] selection = contactsTree.getSelection();
 				for (int i = 0; i < selection.length; i++) {
 					if (mapContact.containsKey(selection[i])) {
 						setConversations(mapContact.get(selection[i]), "", "time");
 					} else if (mapContactAccount.containsKey(selection[i])) {
 						setConversations(mapContactAccount.get(selection[i]), "", "time");
 					} else {
 						throw new IllegalArgumentException("Incorrect selection.");
 					}
 				}
 			}
 		});
 		contactsScrolledComposite.setContent(contactsTree);
 		contactsScrolledComposite.setMinSize(contactsTree.computeSize(SWT.DEFAULT, SWT.DEFAULT));
 		// END LEFT COMPOSITE
 
 		// RIGHT COMPOSITE
 		Composite compositeRight = new Composite(sashForm, SWT.NONE);
 		compositeRight.setLayout(new GridLayout(2, false));
 		if (!searchButton) {
 			Label sL = new Label(compositeRight, SWT.NONE);
 			sL.setText("Search: ");
 		}
 		messagesKeyword = new Text(compositeRight, SWT.SINGLE | SWT.BORDER);
 		GridData gd_messagesKeyword = new GridData(GridData.FILL_HORIZONTAL);
 		gd_messagesKeyword.verticalAlignment = SWT.FILL;
 		messagesKeyword.setLayoutData(gd_messagesKeyword);
 		messagesKeyword.addModifyListener(new ModifyListener() {
 
 			@Override
 			public void modifyText(ModifyEvent arg0) {
 				TreeItem[] selection = contactsTree.getSelection();
 				for (int i = 0; i < selection.length; i++) {
 					if (mapContact.containsKey(selection[i])) {
 						setConversations(mapContact.get(selection[i]), messagesKeyword.getText(), "time");
 					} else if (mapContactAccount.containsKey(selection[i])) {
 						setConversations(mapContactAccount.get(selection[i]), messagesKeyword.getText(), "time");
 					} else {
 						throw new IllegalArgumentException("Incorrect selection.");
 					}
 				}
 			}
 		});
 		if (searchButton) {
 			Button messagesSearch = new Button(compositeRight, SWT.PUSH);
 			messagesSearch.setImage(sia.ui.org.eclipse.wb.swt.SWTResourceManager.getImage(Start.class,
 					"/sia/ui/resources/find.png"));
 			messagesSearch.setText("Search");
 		}
 
 		SashForm sashForm_1 = new SashForm(compositeRight, SWT.VERTICAL);
 		GridData gridData1 = new GridData(GridData.FILL_BOTH);
 		gridData1.horizontalSpan = 2;
 		sashForm_1.setLayoutData(gridData1);
 		ScrolledComposite messagesScrolledComposite = new ScrolledComposite(sashForm_1, SWT.BORDER | SWT.H_SCROLL
 				| SWT.V_SCROLL);
 		messagesScrolledComposite.setExpandHorizontal(true);
 		messagesScrolledComposite.setExpandVertical(true);
 
 		conversationsTable = new Table(messagesScrolledComposite, SWT.BORDER | SWT.FULL_SELECTION);
 		conversationsTable.setHeaderVisible(true);
 		conversationsTable.setLinesVisible(true);
 		conversationsTable.addSelectionListener(new SelectionListener() {
 
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				setConversation(conversationsTable.getSelectionIndex());
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent arg0) {
 			}
 		});
 
 		TableColumn tblclmnContact = new TableColumn(conversationsTable, SWT.NONE);
 		tblclmnContact.setWidth(100);
 		tblclmnContact.setText("Contact");
 
 		TableColumn tblclmnTitle = new TableColumn(conversationsTable, SWT.NONE);
 		tblclmnTitle.setWidth(233);
 		tblclmnTitle.setText("Title");
 		tblclmnTitle.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event e) {
 				TreeItem[] selection = contactsTree.getSelection();
 				for (int i = 0; i < selection.length; i++) {
 					if (mapContact.containsKey(selection[i])) {
 						setConversations(mapContact.get(selection[i]), lastSearchContact, "title");
 					} else if (mapContactAccount.containsKey(selection[i])) {
 						setConversations(mapContactAccount.get(selection[i]), lastSearchContact, "title");
 					} else {
 						throw new IllegalArgumentException("Incorrect selection.");
 					}
 				}
 			}
 		});
 
 		TableColumn tblclmnTime = new TableColumn(conversationsTable, SWT.NONE);
 		tblclmnTime.setResizable(false);
 		tblclmnTime.setWidth(119);
 		tblclmnTime.setText("Time");
 		tblclmnTime.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event e) {
 				TreeItem[] selection = contactsTree.getSelection();
 				for (int i = 0; i < selection.length; i++) {
 					if (mapContact.containsKey(selection[i])) {
 						setConversations(mapContact.get(selection[i]), lastSearchContact, "time");
 					} else if (mapContactAccount.containsKey(selection[i])) {
 						setConversations(mapContactAccount.get(selection[i]), lastSearchContact, "time");
 					} else {
 						throw new IllegalArgumentException("Incorrect selection.");
 					}
 				}
 			}
 		});
 
 		TableColumn tblclmnLength = new TableColumn(conversationsTable, SWT.NONE);
 		tblclmnLength.setResizable(false);
 		tblclmnLength.setWidth(83);
 		tblclmnLength.setText("Length");
 		tblclmnLength.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event e) {
 				TreeItem[] selection = contactsTree.getSelection();
 				for (int i = 0; i < selection.length; i++) {
 					if (mapContact.containsKey(selection[i])) {
 						setConversations(mapContact.get(selection[i]), lastSearchContact, "length");
 					} else if (mapContactAccount.containsKey(selection[i])) {
 						setConversations(mapContactAccount.get(selection[i]), lastSearchContact, "length");
 					} else {
 						throw new IllegalArgumentException("Incorrect selection.");
 					}
 				}
 			}
 		});
 
 		messagesScrolledComposite.setContent(conversationsTable);
 		messagesScrolledComposite.setMinSize(conversationsTable.computeSize(SWT.DEFAULT, SWT.DEFAULT));
 
 		ScrolledComposite scrolledComposite = new ScrolledComposite(sashForm_1, SWT.BORDER | SWT.H_SCROLL
 				| SWT.V_SCROLL);
 		scrolledComposite.setExpandHorizontal(true);
 		scrolledComposite.setExpandVertical(true);
 
 		conversationBrowser = new Browser(scrolledComposite, SWT.NONE);
 		scrolledComposite.setContent(conversationBrowser);
 		scrolledComposite.setMinSize(conversationBrowser.computeSize(SWT.DEFAULT, SWT.DEFAULT));
 		sashForm_1.setWeights(new int[] { 1, 1 });
 		sashForm.setWeights(new int[] { 264, 613 });
 		// RIGHT COMPOSITE
 
 		// STATUS
 		Label status = new Label(composite, SWT.NONE);
 		gridData = new GridData(GridData.FILL_HORIZONTAL, GridData.VERTICAL_ALIGN_END, false, false);
 		status.setLayoutData(gridData);
 		status.setText("");
 		// END STATUS
 
 		fillContactTree("");
 
 		return composite;
 	}
 
 	private void fillContactTree(String s) {
 		if (contactsTree != null && !lastSearchContact.equals(s)) {
 			lastSearchContact = s;
 			contactsTree.removeAll();
 			List<Contact> contacts = Dictionaries.getInstance().getContacts();
 			mapContact = new HashMap<TreeItem, Contact>();
 			mapContactAccount = new HashMap<TreeItem, ContactAccount>();
 			for (Contact c : contacts) {
 				boolean contactContains = s.length() == 0 || c.getFirstname().indexOf(s) != -1
 						|| c.getLastname().indexOf(s) != -1 || c.getName().indexOf(s) != -1;
 				TreeItem contactItem = new TreeItem(contactsTree, SWT.NONE);
 				contactItem.setText(c.getName());
 				mapContact.put(contactItem, c);
 				for (ContactAccount ca : c.getContactAccounts()) {
 					if (contactContains || ca.getUid().toLowerCase().indexOf(s.toLowerCase().trim()) != -1) {
 						TreeItem contactAccountItem = new TreeItem(contactItem, SWT.NONE);
 						contactAccountItem.setText(ca.getUid());
 						contactAccountItem.setImage(sia.ui.org.eclipse.wb.swt.SWTResourceManager.getImage(Start.class,
 								"/sia/ui/resources/protocols/" + ca.getProtocol().getIcon()));
 						mapContactAccount.put(contactAccountItem, ca);
 					}
 				}
 				if (contactItem.getItems().length == 0) {
 					contactItem.dispose();
 				}
 			}
 		}
 	}
 
 	private void setConversations(Contact contact, String s, String orderby) {
 		if (!lastSearchMessage.equals(s)) {
 			lastSearchContact = s;
 			conversations = null;
 			String order = orderby+" ";
 			if(orderbyConversation.equals(orderby)) {
 				if(asc) {
 					order += "DESC";
 					asc=false;
 				} else {
 					asc=true;
 				} 
 			} else {
 				orderbyConversation = orderby;
 				asc = true;
 			}
 			try {
 				 conversations = SIA
 						.getInstance()
 						.getORM()
 						.getTable(Conversation.class)
 						.selectAllCustom(
 								"WHERE id IN (SELECT DISTINCT m.conversationId FROM main.message m JOIN main.conversation c ON m.conversationId = c.id " +
 								" JOIN contactaccount ca ON ca.id = c.contactAccountId AND ca.contactId = " + contact.getId() + " WHERE message LIKE '%" + s + "%') ORDER BY "+order);
 			} catch (SormulaException e) {
 				SIA.getInstance().handleException("An unexpected error occured when loading conversations.", e);
 			}
 			TableItem[] tis = conversationsTable.getItems();
 			for (TableItem ti : tis) {
 				ti.dispose();
 			}
 			for (Conversation conv : conversations) {
 				TableItem ti = new TableItem(conversationsTable, SWT.NONE);
 				ti.setText(new String[] { conv.getContactAccount().getContact().getName(), conv.getTitle(),
 						conv.getTime().toString(), conv.getLength() + "" });
 			}
 		}
 	}
 
 	private void setConversations(ContactAccount contactAccount, String s, String orderby) {
 		if (!lastSearchMessage.equals(s)) {
 			lastSearchContact = s;
 			conversations = null;
 			String order = orderby+" ";
 			if(orderbyConversation.equals(orderby)) {
 				if(asc) {
 					order += "DESC";
 					asc=false;
 				} else {
 					order+= "ASC";
 					asc=true;
 				} 
 			} else {
 				orderbyConversation = orderby;
 			}
 			try {
 				conversations = SIA
 								.getInstance()
 								.getORM()
 								.getTable(Conversation.class)
 								.selectAllCustom(
 										"WHERE id IN (SELECT DISTINCT m.conversationId FROM main.message m JOIN main.conversation c ON m.conversationId = c.id AND c.contactAccountId = "
 												+ contactAccount.getId() + " WHERE message LIKE '%" + s + "%') ORDER BY "+order);
 			} catch (SormulaException e) {
 				SIA.getInstance().handleException("An unexpected error occured when loading conversations.", e);
 			}
 		}
 		Collections.sort(conversations);
 		TableItem[] tis = conversationsTable.getItems();
 		for (TableItem ti : tis) {
 			ti.dispose();
 		}
 		for (Conversation conv : conversations) {
 			TableItem ti = new TableItem(conversationsTable, SWT.NONE);
 			ti.setText(new String[] { conv.getContactAccount().getContact().getName(), conv.getTitle(), conv.getTime().toString(),
 					conv.getLength() + "" });
 		}
 	}
 
 	private void setConversation(int n) {
 		Color c = new Color(0xEFEFEF);
 		Color cReceiver = new Color(0xDEF7FF);
 		Conversation conv = conversations.get(n);
 		List<Message> m;
 		if (conv.getMessages() == null || conv.getMessages().isEmpty()) {
 			try {
 				conv.setMessages(SIA.getInstance().getORM().getTable(Message.class)
 						.selectAllCustom("WHERE conversationId = " + conv.getId() + " ORDER BY time"));
 			} catch (SormulaException e) {
 				SIA.getInstance().handleException("An unexpected error occured when loading messages.", e);
 			}
 		}
 		m = conv.getMessages();
 		StringBuilder html = new StringBuilder();
 		html.append("<!doctype html>");
 		html.append("<html>");
 		html.append("<head>");
 		html.append("<title>Tytuł strony</title>");
 		html.append("<style type=\"text/css\">");
 		html.append("body {font: 10px system;}");
 
 		html.append(".received {");
 		html.append("border: 1px solid " + c2h(cReceiver.darker()) + ";");
 		html.append("background-color:" + c2h(cReceiver.brighter()) + ";");
 		html.append("background-image: -webkit-gradient(linear, left top, left bottom, from("
 				+ c2h(cReceiver.brighter()) + "), +to(" + c2h(cReceiver) + "));");
 		html.append("background-image: -webkit-linear-gradient(top, " + c2h(cReceiver.brighter()) + ", "
 				+ c2h(cReceiver) + ");");
 		html.append("background-image: -moz-linear-gradient(top, " + c2h(cReceiver.brighter()) + ", " + c2h(cReceiver)
 				+ ");");
 		html.append("background-image: linear-gradient(top, " + c2h(cReceiver.brighter()) + ", " + c2h(cReceiver)
 				+ ");");
 		html.append("color:" + c2h(cReceiver.darker().darker().darker().darker()) + ";}");
 
 		html.append(".sent {");
 		html.append("-webkit-border-radius: 5px;");
 		html.append("-moz-border-radius: 5px; ");
 		html.append("border-radius: 5px;");
 		html.append("border: 1px solid " + c2h(c.darker()) + ";");
 		html.append("background-color:" + c2h(c.brighter()) + ";");
 		html.append("background-image: -webkit-gradient(linear, left top, left bottom, from(" + c2h(c.brighter())
 				+ "), +to(" + c2h(c) + "));");
 		html.append("background-image: -webkit-linear-gradient(top, " + c2h(c.brighter()) + ", " + c2h(c) + ");");
 		html.append("background-image: -moz-linear-gradient(top, " + c2h(c.brighter()) + ", " + c2h(c) + ");");
 		html.append("background-image: linear-gradient(top, " + c2h(c.brighter()) + ", " + c2h(c) + ");");
 		html.append("color:" + c2h(c.darker().darker().darker().darker()) + ";}");
 
 		html.append(".received p, .sent p {");
 		html.append("margin: 0 0 3px 30px;");
 		// html.append("position: relative;");
 		// html.append("clear: right;");
 		html.append("}");
 
 		html.append(".received, .sent {");
 		html.append("margin: 5px;");
 		html.append("padding: 5px;");
 		html.append("-webkit-border-radius: 5px;");
 		html.append("-moz-border-radius: 5px; ");
 		html.append("border-radius: 5px;");
 		html.append("}");
 		html.append(".clear {");
 		html.append("clear:both;");
 		html.append("line-height: 0.2em;");
 		html.append("}");
 		html.append(".time {");
 		html.append("float: right;");
 		html.append("clear: right;");
 		html.append("}");
 		html.append(".avatar {");
 		html.append("float: left;");
 		html.append("}");
 		html.append("</style>");
 		html.append("</head>");
 		html.append("<body>");
 		html.append("<header>");
 		html.append("");
 		html.append("</header> ");
 		html.append("");
 		for (int i = 0; i < m.size(); i++) {
 			if (m.get(i).getReceived() > 0) {
 				html.append("<div class=\"received\">");
 			} else {
 				html.append("<div class=\"sent\">");
 			}
 			html.append("<div class=\"avatar\">");
 			html.append("<img src=\"/sia/ui/resources/properties.png\" />");
 			html.append("</div>");
 			html.append("<p>");
 			html.append(m.get(i).getMessage());
 			html.append("<span class=\"time\">");
 			html.append(m.get(i).getTime());
 			html.append("</span>");
 			html.append("</p>");
 			html.append("<div style=\"clear:both;\"></div>");
 			while (i + 1 < m.size() && m.get(i).getReceived() == m.get(i + 1).getReceived()
 					&& m.get(i + 1).getTime().getTime() - m.get(i).getTime().getTime() < 1800000) {
 				html.append("<p>");
 				html.append(m.get(i + 1).getMessage());
 				html.append("<span class=\"time\">");
 				html.append(m.get(i + 1).getTime());
 				html.append("</span>");
 				html.append("</p>");
 				html.append("<div class=\"clear\">&nbsp;</div>");
 				i++;
 			}
 
 			html.append("</div>");
 		}
 		html.append("");
 		html.append("</body>");
 		html.append("</html>");
 		conversationBrowser.setText(html.toString());
 	}
 
 	private String c2h(Color c) {
 		return "#" + Integer.toHexString(c.getRGB()).substring(2);
 	}
 }
