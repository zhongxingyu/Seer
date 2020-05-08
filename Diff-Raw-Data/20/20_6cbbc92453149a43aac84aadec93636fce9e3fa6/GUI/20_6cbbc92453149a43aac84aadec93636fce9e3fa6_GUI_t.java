 package org.smartsnip.client;
 
 import org.smartsnip.shared.XSearch;
 import org.smartsnip.shared.XSnippet;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 /**
  * The GUI
  * 
  * Putting together different Widgets
  * 
  * 
  */
 public class GUI {
 
 	/**
 	 * This inner class is used for return results in instantiated popup dialogs
 	 * 
 	 * @param <E>
 	 *            Generic type of the return value
 	 */
 	private static class ReturnValue<E> {
 		/** Value that is returned */
 		private E value = null;
 
 		/**
 		 * Set return value
 		 * 
 		 * @param value
 		 *            value to be set
 		 */
 		public void setValue(E value) {
 			this.value = value;
 		}
 
 		/**
 		 * @return the previously set return value, or null, if not yet set
 		 */
 		public E getValue() {
 			return value;
 		}
 	}
 
 	// visible in package, Control can modify
 	ResultArea myResultArea = null;
 	StatusArea myStatusArea = null;
 	CatArea myCatArea = null;
 	TagArea myTagArea = null;
 	CommentArea myCommentArea = null;
 	Meta myMeta = null;
 	Footer myFooter = null;
 	SnipArea mySnipArea = null;
 	PersonalArea myPersonalArea = null;
 	SearchArea mySearchArea = null;
 	ModeratorArea myModeratorArea = null;
 	SearchToolbar mySearchToolbar = null;
 
 	Control control = Control.getInstance();
 
 	// Create userPanel
 	SimplePanel userPanel = new SimplePanel();
 	// Create searchPanel
 	SimplePanel searchPanel = new SimplePanel();
 	// Create dataPanel
 	HorizontalPanel dataPanel = new HorizontalPanel();
 	// Create footerPanel
 	SimplePanel footerPanel = new SimplePanel();
 
 	/**
 	 * This callback registers itself in the constructor and handles the update
 	 * of the search page. It is called, when the search results come to the
 	 * client.
 	 * 
 	 * This is the callback for {@link Search#search()}, when an arbitary GUI
 	 * entity fires a search
 	 */
 	private final AsyncCallback<XSearch> searchAsynCallback = new AsyncCallback<XSearch>() {
 
 		@Override
 		public void onSuccess(XSearch result) {
 			String status = result.totalresults + " results in "
 					+ convertSearchTime(Control.search.getSearchTime());
 			updateSearchPage(result, status);
 		}
 
 		@Override
 		public void onFailure(Throwable caught) {
 			String status = "Search failed";
 			if (caught != null)
 				status += ": " + caught.getMessage();
 
 			updateSearchPage(null, status);
 		}
 
 		/**
 		 * This call converts a given time interval given in milliseconds into a
 		 * string, that is readable for humans
 		 * 
 		 * @param millis
 		 *            milliseconds to be converted
 		 * @return a human readable format with possible less precision than the
 		 *         milliseconds
 		 */
 		private String convertSearchTime(long millis) {
 			if (millis < 0)
 				return "- " + convertSearchTime(-millis);
 
 			if (millis < 1000)
 				return millis + " ms";
 			int tenthSeconds = (int) (millis / 100); // 10th-seconds
 														// ("Zehntelsekunden")
 			if (tenthSeconds < 100) {
 				float result = tenthSeconds / (float) 10.0;
 				return result + " s";
 			}
 
 			return (millis / 1000) + " s";
 		}
 	};
 
 	/**
 	 * Start the GUI
 	 * 
 	 */
 	public void getReady() {
 		// Adds a personalized CSS-File
 		Resources.INSTANCE.css().ensureInjected();
 		Resources.INSTANCE.cssPrettify().ensureInjected();
 
 		// Get the updated userdata
 		Control control = Control.getInstance();
 		control.refresh();
 
 		// Create the Page
 		initComponents();
 		createBasicPage();
 		// showSearchPage();
 		// showImpressum();
 
 		control.showSnippetOfDay();
 
 		// showPersonalPage();
 		// showLoginPopup();
 		// showRegisterPopup();
 		// showTestPopup();
 
 		// Register the search callback
 		Control.search.addCallback(searchAsynCallback);
 	}
 
 	/** Create components */
 	private void initComponents() {
 		mySearchToolbar = new SearchToolbar();
 		myCatArea = new CatArea();
 		myTagArea = new TagArea();
 
 		// Add a handler that is called, when a search is done
 		Control.search.addCallback(new AsyncCallback<XSearch>() {
 
 			@Override
 			public void onSuccess(XSearch result) {
 				onSearchDone(true);
 			}
 
 			@Override
 			public void onFailure(Throwable caught) {
 				onSearchDone(false);
 			}
 		});
 	}
 
 	/**
 	 * Preparing the basics widgets for the GUI
 	 * 
 	 */
 	public void createBasicPage() {
 
 		// Fill userPanel
 		myMeta = new Meta();
 		userPanel.add(myMeta);
 
 		// Fill searchPanel
 		mySearchArea = new SearchArea();
 		searchPanel.add(mySearchArea);
 
 		// Fill footerPanel
 		myFooter = new Footer();
 		footerPanel.setWidth("100%");
 		footerPanel.add(myFooter);
 
 		// Fix Panels to divs
 		RootPanel.get("user").add(userPanel);
 		RootPanel.get("search").add(searchPanel);
 		RootPanel.get("data").add(dataPanel);
 		RootPanel.get("footer").add(footerPanel);
 
 		// Sets Cursor
 		// z.B. ewSymbolTextBox.setFocus(true);
 
 	}
 
 	/**
 	 * shows the search page
 	 * 
 	 */
 	public void showSearchPage() {
 
 		dataPanel.clear();
 
 		myResultArea = new ResultArea();
 		myStatusArea = new StatusArea();
 
 		VerticalPanel leftPanel = new VerticalPanel();
 		leftPanel.setStyleName("leftPanel");
 
 		VerticalPanel rightPanel = new VerticalPanel();
 		rightPanel.setStyleName("rightPanel");
 
 		leftPanel.add(myStatusArea);
 		leftPanel.add(myResultArea);
 
 		rightPanel.add(myCatArea);
 		rightPanel.add(myTagArea);
 		rightPanel.add(mySearchToolbar);
 		dataPanel.add(leftPanel);
 		dataPanel.add(rightPanel);
 
 	}
 
 	/**
 	 * shows the snippet page
 	 * 
 	 */
 	public void showSnipPage(XSnippet snip) {
 		dataPanel.clear();
 		mySnipArea = new SnipArea(snip);
		mySnipArea.update(); // Updade increases viewcounter
 		dataPanel.add(mySnipArea);
 		myCommentArea = new CommentArea(snip);
 		dataPanel.add(myCommentArea);
 	}
 
 	/**
 	 * shows the Personal page
 	 * 
 	 */
 	public void showPersonalPage() {
 		dataPanel.clear();
 		myPersonalArea = new PersonalArea();
 		VerticalPanel vertPanel = new VerticalPanel();
 		dataPanel.add(myPersonalArea);
 		dataPanel.add(vertPanel);
 		myPersonalArea.updateSnippets();
 	}
 
 	/**
 	 * shows the login popup
 	 * 
 	 */
 	public void showLoginPopup() {
 
 		Window.scrollTo(0, 0);
 		PopupPanel loginPanel = new PopupPanel(false);
 		loginPanel.setStyleName("Login");
 		loginPanel.setTitle("Login");
 		Login login = new Login(loginPanel);
 		loginPanel.setWidget(login);
 		loginPanel.setGlassEnabled(true);
 		loginPanel.setPopupPosition(90, 104);
 		loginPanel.setWidth("250px");
 		loginPanel.show();
 	}
 
 	/**
 	 * shows a test popup
 	 * 
 	 * @param String
 	 *            the titlee of the popup
 	 * 
 	 */
 	public void showTestPopup(String test) {
 
 		final PopupPanel loginPanel = new PopupPanel(true, true);
 		Button close = new Button("<b>Close</b>");
 
 		loginPanel.setTitle(test);
 		VerticalPanel vertPanel = new VerticalPanel();
 		Label lname = new Label(test);
 		vertPanel.add(lname);
 		vertPanel.add(close);
 		loginPanel.setWidget(vertPanel);
 		loginPanel.setGlassEnabled(true);
 		loginPanel.setPopupPosition(110, 100);
 		loginPanel.setWidth("340px");
 		loginPanel.show();
 		close.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				loginPanel.hide();
 			}
 		});
 	}
 
 	/**
 	 * Shows a simple confirmation dialog, with the YES/NO Option.
 	 * 
 	 * @param message
 	 *            Message to be displayed
 	 * @return true if the user clicked YES, false if the user clicked NO
 	 */
 	public boolean showConfirmPopup(String message) {
 		return showConfirmPopup(message, "Confirmation");
 	}
 
 	/**
 	 * Shows a simple confirmation dialog, with the YES/NO Option.
 	 * 
 	 * @param message
 	 *            Message to be displayed
 	 * @param title
 	 *            Title of the message to be displayed
 	 * @return true if the user clicked YES, false if the user clicked NO
 	 */
 	public boolean showConfirmPopup(String message, String title) {
 		if (message == null)
 			message = "";
 		if (title == null)
 			title = "";
 
 		final PopupPanel confirmPopup = new PopupPanel(true, true);
 		Button btnYes = new Button("<b>Yes</b>");
 		Button btnNo = new Button("<b>No</b>");
 
 		confirmPopup.setTitle("Error");
 		VerticalPanel vertPanel = new VerticalPanel();
 		HorizontalPanel pnlButtons = new HorizontalPanel();
 		Label lname = new Label(message);
 		vertPanel.add(lname);
 		pnlButtons.add(btnYes);
 		pnlButtons.add(btnNo);
 		vertPanel.add(pnlButtons);
 		confirmPopup.setWidget(vertPanel);
 		confirmPopup.setGlassEnabled(true);
 		confirmPopup.setPopupPosition(110, 100);
 		confirmPopup.setWidth("340px");
 
 		final ReturnValue<Boolean> result = new ReturnValue<Boolean>();
 		btnNo.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				result.setValue(Boolean.FALSE);
 				confirmPopup.hide();
 			}
 		});
 		btnYes.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				result.setValue(Boolean.TRUE);
 				confirmPopup.hide();
 			}
 		});
 		confirmPopup.show();
 
 		return result.getValue() == Boolean.TRUE;
 	}
 
 	/**
 	 * shows a error popup
 	 * 
 	 * @param String
 	 *            the error message
 	 * @deprecated Deprecated because it uses less fuctionality than
 	 *             {@link #showErrorPopup(String, Throwable)} Use this method
 	 *             instant
 	 * 
 	 */
 	@Deprecated
 	public void showErrorPopup(String message) {
 		if (message == null || message.isEmpty())
 			return;
 
 		final PopupPanel errorPopup = new PopupPanel(true, true);
 		Button close = new Button("<b>Close</b>");
 
 		errorPopup.setTitle("Error");
 		VerticalPanel vertPanel = new VerticalPanel();
 		Label lname = new Label(message);
 		vertPanel.add(lname);
 		vertPanel.add(close);
 		errorPopup.setWidget(vertPanel);
 		errorPopup.setGlassEnabled(true);
 		errorPopup.setPopupPosition(110, 100);
 		errorPopup.setWidth("340px");
 		errorPopup.show();
 		close.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				errorPopup.hide();
 			}
 		});
 	}
 
 	/**
 	 * Shows a error popup. This error popup is more detailed than
 	 * {@link #showErrorPopup(String)} and gives the user some more tools to
 	 * investigate the error
 	 * 
 	 * @param String
 	 *            the error message
 	 * @param Throwable
 	 *            the error
 	 * 
 	 */
 	public void showErrorPopup(String message, final Throwable cause) {
 		if (message == null)
 			message = "";
 		if (cause != null) {
 			String causeMessage = cause.getMessage();
 			if (causeMessage != null)
 				message = message + "\n" + cause.getMessage();
 		}
 
 		final PopupPanel errorPopup = new PopupPanel(true, true);
 		Button close = new Button("<b>Close</b>");
 		final Anchor anchDetails = new Anchor("Show me more details ... ");
 
 		errorPopup.setTitle("Error");
 		final VerticalPanel vertPanel = new VerticalPanel();
 		Label lname = new Label(message);
 		final Label lblErrorTrace = new Label("");
 		vertPanel.add(lname);
 		vertPanel.add(close);
 		vertPanel.add(lblErrorTrace);
 		vertPanel.add(anchDetails);
 		lblErrorTrace.setVisible(false);
 		errorPopup.setWidget(vertPanel);
 		errorPopup.setGlassEnabled(true);
 		errorPopup.setPopupPosition(110, 100);
 		errorPopup.setWidth("340px");
 		errorPopup.show();
 		close.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				errorPopup.hide();
 			}
 		});
 		anchDetails.addClickHandler(new ClickHandler() {
 
 			boolean enabled = false;
 
 			@Override
 			public void onClick(ClickEvent event) {
 				if (enabled)
 					return;
 				enabled = true;
 
 				// Show more details
 				lblErrorTrace.setVisible(true);
 				anchDetails.setEnabled(false);
 				printErrorTrace();
 			}
 
 			private void printErrorTrace() {
 				lblErrorTrace.setText("Not yet implemented, unfortunately ");
 
 				// TODO Print error stack trace
 
 			}
 		});
 	}
 
 	/**
 	 * shows the register popup
 	 * 
 	 * 
 	 */
 	public void showRegisterPopup() {
 
 		Window.scrollTo(0, 0);
 		PopupPanel registerPanel = new PopupPanel(true);
 
 		registerPanel.setStyleName("Register");
 		registerPanel.setTitle("Register");
 		Register register = new Register(registerPanel);
 		registerPanel.setWidget(register);
 		registerPanel.setGlassEnabled(true);
 		registerPanel.setPopupPosition(90, 104);
 		registerPanel.setWidth("250px");
 		registerPanel.show();
 	}
 
 	/**
 	 * shows the impressum
 	 * 
 	 * 
 	 */
 	public void showImpressum() {
 
 		Window.scrollTo(0, 0);
 		dataPanel.clear();
 		HTML impressum = new HTML(
 				"<p><br /><strong>Verantwortlich für den Inhalt<br /></strong>Paul Opitz <br />"
 						+ "Roveretoplatz 2 <br />6330 Kufstein <br />Österreich</p><p><br /><strong>Haftung</strong><br />"
 						+ "Die Inhalte unserer Seiten wurden mit größter Sorgfalt erstellt. Für die Richtigkeit, Vollständigkeit und Aktualität der Inhalte kann ich jedoch keine Gewähr übernehmen.</p><p><br /><strong>Haftung für Links</strong><br />Unser Angebot enthält Links zu externen Webseiten Dritter, auf deren Inhalte ich keinen Einfluss habe. Deshalb kann ich für diese fremden Inhalte auch keine Gewähr übernehmen. "
 						+ "Für die Inhalte der verlinkten Seiten ist stets der jeweilige Anbieter "
 						+ "oder Betreiber der Seiten verantwortlich. Die verlinkten Seiten wurden zum Zeitpunkt der Verlinkung auf mögliche Rechtsverstöße überprüft. Rechtswidrige Inhalte waren zum Zeitpunkt der Verlinkung nicht erkennbar. Eine permanente inhaltliche Kontrolle der verlinkten Seiten ist jedoch ohne konkrete Anhaltspunkte einer Rechtsverletzung nicht zumutbar. Bei Bekanntwerden von Rechtsverletzungen werde ich derartige Links umgehend entfernen.</p><br /><strong>Rechte</strong><br />"
 						+ "Die Inhalte stehen unter einer Creative Commons Namensnennung-Nicht-kommerziell-Weitergabe unter gleichen Bedingungen 3.0 Österreich Lizenz.<p><br /><strong>Datenschutz</strong><br />Die Nutzung unserer Webseite ist in der Regel ohne Angabe personenbezogener Daten möglich. Soweit auf meinen Seiten personenbezogene Daten (beispielsweise Name, Anschrift oder E-Mail-Adressen) erhoben werden, erfolgt dies, soweit möglich, stets auf freiwilliger Basis. Diese Daten werden ohne Ihre ausdrückliche Zustimmung nicht an Dritte weitergegeben.Ich weise darauf hin, dass die Datenübertragung im Internet (z.B. bei der Kommunikation per E-Mail) Sicherheitslücken aufweisen kann. Ein lückenloser Schutz der Daten vor dem Zugriff durch Dritte ist nicht möglich.Der Nutzung von im Rahmen der Impressumspflicht veröffentlichten Kontaktdaten durch Dritte zur Übersendung von nicht ausdrücklich angeforderter Werbung und Informationsmaterialien wird hiermit ausdrücklich widersprochen. Der Betreiber der Seiten behaltet sich ausdrücklich rechtliche Schritte im Falle der unverlangten Zusendung von Werbeinformationen, etwa durch Spam-Mails, vor.</p>");
 		impressum.setWidth("800px");
 		dataPanel.add(impressum);
 	}
 
 	/**
 	 * updates the search pagel. Internal call for
 	 * {@link GUI#searchAsynCallback}
 	 * 
 	 * @param XSearch
 	 *            the search result
 	 * @param String
 	 *            the current search status
 	 * 
 	 * 
 	 */
 	private void updateSearchPage(XSearch result, String status) {
 		showSearchPage();
 
 		myStatusArea.setStatus(status);
 		if (result != null) {
 			myResultArea.update(result.snippets);
 			myCatArea.update(result.categories);
 			myTagArea.update(result.tagsAppearingInSearchString);
 			mySearchArea.update();
 		}
 
 		mySearchArea.updateSuggestions();
 	}
 
 	/**
 	 * shows the contact form
 	 * 
 	 * 
 	 */
 	public void showContactForm() {
 
 		Window.scrollTo(0, 0);
 		PopupPanel ppnlContact = new PopupPanel(false);
 		ppnlContact.setStyleName("contactForm");
 		ppnlContact.setTitle("Contact");
 		Contact contact = new Contact(ppnlContact);
 		ppnlContact.setWidget(contact);
 		ppnlContact.setGlassEnabled(true);
 		ppnlContact.setPopupPosition(90, 104);
 		ppnlContact.setWidth("250px");
 		ppnlContact.show();
 	}
 
 	/**
 	 * shows the create snippet popup
 	 * 
 	 * 
 	 */
 	public void showCreateSnippetForm() {
 		Window.scrollTo(0, 0);
 		PopupPanel ppnlSnippet = new PopupPanel(true, true);
 		ppnlSnippet.setStyleName("contactForm");
 		ppnlSnippet.setTitle("Create snippet");
 		CreateSnippet newSnippet = new CreateSnippet(ppnlSnippet, "Create");
 		ppnlSnippet.setWidget(newSnippet);
 		ppnlSnippet.setGlassEnabled(true);
 		ppnlSnippet.setPopupPosition(90, 104);
 		ppnlSnippet.setWidth("250px");
 		ppnlSnippet.show();
 
 	}
 
 	/**
 	 * Creates a popup window with a link
 	 * 
 	 * @param String
 	 *            the download message
 	 * @param convertToLink
 	 *            link to be created
 	 */
 	public void showDownloadPopup(String message, String convertToLink) {
 
 		Window.scrollTo(0, 0);
 
 		if (message == null)
 			message = "";
 		if (convertToLink == null || convertToLink.isEmpty())
 			return;
 
 		final PopupPanel popup = new PopupPanel(true, true);
 		Button close = new Button("<b>Close</b>");
 		Anchor link = new Anchor(convertToLink);
 		link.setHref(convertToLink);
 
 		popup.setTitle("Link ");
 		VerticalPanel vertPanel = new VerticalPanel();
 		Label lname = new Label(message);
 		vertPanel.add(lname);
 		vertPanel.add(close);
 		popup.setWidget(vertPanel);
 		popup.setGlassEnabled(true);
 		popup.setPopupPosition(90, 104);
 		popup.setWidth("250px");
 		popup.show();
 		close.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				popup.hide();
 			}
 		});
 	}
 
 	/**
 	 * shows the moderator page
 	 * 
 	 * 
 	 */
 	public void showModPage() {
 		dataPanel.clear();
 		myModeratorArea = new ModeratorArea();
 		dataPanel.add(myModeratorArea);
 	}
 
 	/**
 	 * shows upload window
 	 * 
 	 * @param snippetID
 	 *            Hash ID of the snippet the uploaded file belongs to
 	 */
 	public void showUploadSnippet(long snippetID) {
 
 		Window.scrollTo(0, 0);
 		PopupPanel ppnlSnippet = new PopupPanel(true, true);
 		ppnlSnippet.setStyleName("uploadForm");
 		ppnlSnippet.setTitle("Upload Snippet");
 		Upload myUpload = new Upload(ppnlSnippet, "upload", "snip_id="
 				+ snippetID);
 		ppnlSnippet.setWidget(myUpload);
 		ppnlSnippet.setGlassEnabled(true);
 		ppnlSnippet.setPopupPosition(90, 104);
 		ppnlSnippet.setWidth("250px");
 		ppnlSnippet.show();
 
 	}
 
 	/**
 	 * This method is called by {@link Search#search()} when a new search has
 	 * started
 	 */
 	void onSearchStart() {
 		showSearchPage();
 		myStatusArea.setStatus("Searching ... ");
 		mySearchArea.disableSearchButton();
 
 		mySearchToolbar.setEnabled(false);
 		myCatArea.setEnabled(false);
 		myTagArea.setEnabled(false);
 
 	}
 
 	/**
 	 * This method is called by a search callback initialised in
 	 * {@link GUI#initComponents}
 	 */
 	void onSearchDone(boolean success) {
 		mySearchToolbar.setEnabled(success);
 		myCatArea.setEnabled(success);
 		myTagArea.setEnabled(success);
 	}
 
 }
