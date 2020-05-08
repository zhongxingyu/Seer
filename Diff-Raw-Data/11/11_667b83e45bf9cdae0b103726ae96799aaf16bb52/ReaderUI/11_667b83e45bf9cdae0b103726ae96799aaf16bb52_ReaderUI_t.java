 /*
  * Filename:    ReaderUi.java
  * Author:      Pekka Nurminen
  * History:     Created: 07022013
  * Description: Main application class.
  */
 package com.reader.ui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.reader.data.GLOBAL_VARIABLES;
 import com.reader.io.AtomReader;
 import com.reader.io.FileRdr;
 import com.reader.io.FileWrtr;
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.vaadin.data.Item;
 import com.vaadin.data.Property;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.data.Property.ValueChangeListener;
 import com.vaadin.data.fieldgroup.FieldGroup;
 import com.vaadin.data.util.IndexedContainer;
 import com.vaadin.event.FieldEvents.TextChangeEvent;
 import com.vaadin.event.FieldEvents.TextChangeListener;
 import com.vaadin.server.VaadinRequest;
 import com.vaadin.shared.ui.label.ContentMode;
 import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.HorizontalSplitPanel;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.NativeSelect;
 import com.vaadin.ui.Table;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.UI;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Window;
 import com.vaadin.ui.Window.CloseEvent;
 import com.vaadin.ui.Window.CloseListener;
 
 /**
  * Main UI class.
  * 
  * @author pekka
  */
 @SuppressWarnings("serial")
 public class ReaderUI extends UI {
 
     /*
      * UI COMPONENTS
      */
     /**
      * The left layout that will contain the feed list table and the feed search
      * text field.
      */
     VerticalLayout leftLayout = new VerticalLayout();
 
     /**
      * The center layout that will contain the entry list table and the entry
      * search.
      */
     VerticalLayout centerLayout = new VerticalLayout();
 
     /**
      * The right layout that will contain the reading area fields and possibly
      * some buttons for different viewing options.
      */
     VerticalLayout rightLayout = new VerticalLayout();
 
     /** The feed list table. */
     private final Table feedListTable = new Table();
 
     /** The entry list table. */
     private final Table entryListTable = new Table();
 
     /** The feed search. */
     private final TextField feedSearchTextField = new TextField();
 
     /** The entry search. */
     private final TextField entrySearchTextField = new TextField();
 
     /** The add new feed. */
     private final Button addNewFeedButton = new Button("Subscribe to new feed");
 
     /** The remove feed. */
     private final Button removeFeedButton = new Button("Unsubscribe from feed");
 
     /** The help. */
     private final Button helpButton = new Button("Help");
 
     private final Button loginButton = new Button("Login");
     private final Button logoutButton = new Button("Logout");
 
     final NativeSelect select = new NativeSelect("View as...");
 
     /** The read area. */
     Label readArea = new Label();
 
     /*
      * WINDOWS
      */
     /** The main window. */
     Window mainWindow;
 
     /** The add feed window. */
     Window addFeedWindow;
 
     /** The help window. */
     Window helpWindow;
 
     Window loginWindow;
 
     /*
      * DATA CONTAINERS
      */
     /**
      * The feedContainer. Filled in the application initialization. Contents are
      * read from file. Afterwards the file is updated, but the file itself is
      * not read anymore.
      */
     // FeedContainer feedContainer = fillFeedContainer();
 
     /** The indexed feed container. */
     // IndexedContainer indexedFeedContainer = insertFeeds();
     IndexedContainer indexedFeedContainer;
     /** The indexed entry container. */
     // IndexedContainer indexedEntryContainer = insertEntries();
     IndexedContainer indexedEntryContainer;
     /*
      * VARIABLES
      */
     /** The Constant FEEDNAME. */
     private static final String FEEDNAME = "Feed";
     private static final String FEEDURL = "Url";
     private static final String FEEDENTRIES = "Entrycount";
 
     /** The Constant feedNames. */
     private static final String[] feedNames = new String[] { FEEDNAME, FEEDURL,
             FEEDENTRIES };
 
     /** The Constant ENTRYNAME. */
     private static final String ENTRYNAME = "Entry";
 
     /** The Constant entryNames. */
     private static final String[] entryNames = new String[] { ENTRYNAME };
 
     /** The Constant fieldNames. */
     private static final String[] fieldNames = new String[] { FEEDNAME,
             ENTRYNAME };
 
     /** The read area fields. */
     private final FieldGroup readAreaFieldsFieldGroup = new FieldGroup();
 
     /** The selected item. */
     String selectedItem;
 
     /** The selected item url. */
     String selectedItemUrl;
 
     /** The selected entry. */
     String selectedEntry;
 
     // ONLY FOR TESTING
     /** The username. */
     // private final String username = "Pekka";
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.ui.UI#init(com.vaadin.server.VaadinRequest)
      */
     @Override
     protected void init(VaadinRequest request) {
 
         GLOBAL_VARIABLES.setFilePath("/Users/pekka/git/Reader/Reader/");
         GLOBAL_VARIABLES.setUserStorePath("/Users/pekka/git/Reader/Reader/");
 
         initToolBar();
         initLayout();
         // initFeedList();
         // initEntryList();
         initButtons();
         initFeedSearch();
         initEntrySearch();
         System.out.println(GLOBAL_VARIABLES.getUsrName());
         if (GLOBAL_VARIABLES.getUsrName().equals("")) {
             initLoginWindow();
 
         } else {
             indexedFeedContainer = insertFeeds();
             indexedEntryContainer = insertEntries();
             initLayout();
             initFeedList();
             initEntryList();
             initButtons();
         }
 
     }
 
     private void initLoginWindow() {
         loginWindow = new LoginWindow();
         loginWindow.addCloseListener(new CloseListener() {
 
             @Override
             public void windowClose(CloseEvent e) {
                 System.out.println("Loginwindow closed.");
                 if (GLOBAL_VARIABLES.getUsrName().equals("")) {
                     logoutButton.setVisible(false);
                     loginButton.setVisible(true);
 
                 } else {
                     loginButton.setVisible(false);
                     logoutButton.setVisible(true);
 
                 }
                 indexedFeedContainer = insertFeeds();
                 indexedEntryContainer = insertEntries();
                 initFeedList();
                 initEntryList();
 
             }
         });
         loginWindow.setWidth("100%");
         loginWindow.setModal(true);
         addWindow(loginWindow);
     }
 
     private void initEntrySearch() {
         entrySearchTextField.setInputPrompt("Filter entries");
         entrySearchTextField.setTextChangeEventMode(TextChangeEventMode.LAZY);
         entrySearchTextField.addTextChangeListener(new TextChangeListener() {
             @Override
             public void textChange(final TextChangeEvent event) {
                 indexedEntryContainer.removeAllContainerFilters();
                 System.out.println(event.getText());
                 indexedEntryContainer.addContainerFilter(new EntryFilter(event
                         .getText()));
             }
         });
     }
 
     private void initFeedSearch() {
         feedSearchTextField.setInputPrompt("Filter feeds");
         feedSearchTextField.setTextChangeEventMode(TextChangeEventMode.LAZY);
         feedSearchTextField.addTextChangeListener(new TextChangeListener() {
             @Override
             public void textChange(final TextChangeEvent event) {
                 indexedFeedContainer.removeAllContainerFilters();
                 indexedFeedContainer.addContainerFilter(new FeedFilter(event
                         .getText()));
             }
         });
 
     }
 
     /**
      * Inits the application buttons.
      */
     private void initButtons() {
 
         loginButton.addClickListener(new ClickListener() {
 
             @Override
             public void buttonClick(ClickEvent event) {
 
                 initLoginWindow();
 
             }
         });
 
         logoutButton.addClickListener(new ClickListener() {
 
             @Override
             public void buttonClick(ClickEvent event) {
                 GLOBAL_VARIABLES.setUsrName("");
 
                 indexedFeedContainer = insertFeeds();
                 indexedEntryContainer = insertEntries();
                 initFeedList();
                 initEntryList();
 
                 initLoginWindow();
             }
         });
 
         addNewFeedButton.addClickListener(new ClickListener() {
             @Override
             public void buttonClick(ClickEvent event) {
                 addFeedWindow = new AddFeedWindow(indexedFeedContainer);
                 addFeedWindow.setWidth("50%");
                 addWindow(addFeedWindow);
             }
         });
 
         helpButton.addClickListener(new ClickListener() {
 
             @Override
             public void buttonClick(ClickEvent event) {
                 helpWindow = new HelpWindow();
                 helpWindow.setWidth("50%");
                 addWindow(helpWindow);
             }
         });
 
         removeFeedButton.addClickListener(new ClickListener() {
 
             @Override
             public void buttonClick(ClickEvent event) {
                 // Get the selected object
                 Object selectedObject = feedListTable.getValue();
 
                 indexedFeedContainer.removeItem(selectedObject);
 
                 List<?> ids = indexedFeedContainer.getItemIds();
                 String writeMe = "";
                 for (Object id : ids) {
                     String url = (String) indexedFeedContainer
                             .getContainerProperty(id, "Url").getValue();
 
                     String title = (String) indexedFeedContainer
                             .getContainerProperty(id, "Feed").getValue();
 
                     String count = (String) indexedFeedContainer
                             .getContainerProperty(id, "Entrycount").getValue();
 
                    // Check that no dummy data is in the table:
                    if (!(title.equals("Start by adding feeds!") && url
                            .equals(""))) {
                        writeMe = writeMe + url + "|" + title + "|" + count
                                + "\n";

                    }
                 }
                 FileWrtr fw = new FileWrtr();
                 fw.createFile(
                         GLOBAL_VARIABLES.getFilePath()
                                 + GLOBAL_VARIABLES.getUsrName()
                                 + GLOBAL_VARIABLES.getFileName(), writeMe);
 
                 entryListTable.removeAllItems();
 
             }
 
         });
 
         select.addValueChangeListener(new ValueChangeListener() {
 
             @Override
             public void valueChange(ValueChangeEvent event) {
                 String selection = (String) event.getProperty().getValue();
                 initReadArea(selection);
 
             }
         });
 
     }
 
     /**
      * Inits the read area when the user has selected a feed and entry. TODO: ?
      * Could the layout be changed automatically depending on the content? If
      * the content contains a picture for example, the read area could be
      * repositioned somehow.
      * 
      * @param selection
      */
     private void initReadArea(String selection) {
 
         AtomReader ar = new AtomReader();
         System.out.println("Initializing read area for: " + selectedEntry);
 
         if (selectedItemUrl != null) {
             SyndFeed selectedFeed = ar.getFeed(selectedItemUrl);
             ArrayList<SyndEntry> entries = ar.getEntries(selectedFeed);
 
             for (SyndEntry entry : entries) {
                 if (entry.getTitle().equals(selectedEntry)) {
                     System.out.println("Found " + entry.getTitle()
                             + ", continue initializing.");
 
                     ArrayList<String> entryContent = ar.getEntryContent(entry);
 
                     String contents = "";
                     if (selection.equals("RAW")) {
                         readArea.setContentMode(ContentMode.RAW);
                     } else if (selection.equals("XML")) {
                         readArea.setContentMode(ContentMode.XML);
                     } else if (selection.equals("TEXT")) {
                         readArea.setContentMode(ContentMode.TEXT);
                     } else if (selection.equals("PREFORMATTED")) {
                         readArea.setContentMode(ContentMode.PREFORMATTED);
                     } else {
                         readArea.setContentMode(ContentMode.HTML);
                     }
                     readArea.setCaption(selectedEntry);
                     readArea.setWidth("100%");
                     readArea.setHeight("100%");
 
                     // TODO: Add different textfields for all of the different
                     // contents of the entry.
                     // The entry is not Atom -> Entrycontent size is not set -->
                     // Show only link (and other valid information) to the RSS
                     // feed for the user.
 
                     // Different information of the feed:
                     String author = "";
                     System.out.println("Auth: " + entry.getAuthor());
                     if (entry.getAuthor().length() > 0
                             || entry.getAuthor() != null) {
                         author = entry.getAuthor();
                     }
                     String publishDate = "";
                     System.out.println("Pb:" + entry.getPublishedDate());
                     if (entry.getPublishedDate() != null) {
                         publishDate = entry.getPublishedDate().toString();
                     }
                     String link = "";
                     if (entry.getLink() != null) {
                         link = entry.getLink();
                     }
                     // String[] authors = (String[])
                     // entry.getAuthors().toArray(
                     // new String[entry.getAuthors().size()]);
                     // String[] categories = (String[])
                     // entry.getCategories()
                     // .toArray(new String[entry.getCategories().size()]);
                     // String[] contributors = (String[]) entry
                     // .getContributors()
                     // .toArray(new String[entry.getContributors().size()]);
                     // String publishDate =
                     // entry.getPublishedDate().toString();
                     // String link = entry.getLink();
                     // String[] links = (String[]) entry.getLinks().toArray(
                     // new String[entry.getLinks().size()]);
                     // author, authors[], categories[], contributors[],
                     // publishdate, link, links[]
 
                     if (entryContent.size() == 0) {
                         contents = "No content, check more from:<br />"
                                 + "<a href=" + entry.getLink()
                                 + " target=\"_blank\">" + selectedEntry
                                 + "</a>";
 
                         contents = contents + "<br />Author:" + author;
                         contents = contents + "<br />Publish date: "
                                 + publishDate;
                         contents = contents + "<br />Link: " + link;
 
                         readArea.setValue(contents);
                     } else {
                         for (String content : entryContent) {
                             contents = contents + content;
                         }
 
                         contents = contents + "<br />Author:" + author;
                         contents = contents + "<br />Publish date: "
                                 + publishDate;
                         contents = contents + "<br />Link: " + link;
 
                         readArea.setValue(contents);
 
                     }
 
                     // for (String fieldName : fieldNames) {
                     // TextField field = new TextField(fieldName);
                     //
                     // rightLayout.addComponent(field);
                     // field.setWidth("100%");
                     // readFields.bind(field, fieldName);
                     // }
                 }
             }
 
         }
     }
 
     /**
      * Inits the entry list.
      */
     private void initEntryList() {
         entryListTable.setContainerDataSource(indexedEntryContainer);
         entryListTable.setVisible(true);
         entryListTable.setSelectable(true);
         entryListTable.setImmediate(true);
         entryListTable.setNullSelectionAllowed(false);
 
         /*
          * Selection listener. Triggered when the user selects an entry from the
          * entrytable.
          */
         entryListTable
                 .addValueChangeListener(new Property.ValueChangeListener() {
 
                     @Override
                     public void valueChange(ValueChangeEvent event) {
                         Property pty = event.getProperty();
                         if (pty == entryListTable) {
                             Item item = entryListTable.getItem(entryListTable
                                     .getValue());
                             Object entryId = entryListTable.getValue();
 
                             if (entryId != null) {
                                 selectedEntry = item.toString();
                                 System.out.println("Selected entry: "
                                         + selectedEntry);
 
                                 initReadArea(select.getValue().toString());
                             }
                         }
 
                     }
                 });
     }
 
     /**
      * Inits the feed list.
      */
     private void initFeedList() {
         feedListTable.setContainerDataSource(indexedFeedContainer);
         feedListTable.setVisibleColumns(new String[] { FEEDNAME, FEEDURL });
         feedListTable.setSelectable(true);
         feedListTable.setImmediate(true);
         feedListTable.setNullSelectionAllowed(false);
 
         /*
          * Selection listener. Triggered when the user selects a feed from the
          * feed table.
          */
         feedListTable
                 .addValueChangeListener(new Property.ValueChangeListener() {
                     @Override
                     public void valueChange(ValueChangeEvent event) {
                         Property pty = event.getProperty();
                         selectedEntry = null;
                         selectedItem = null;
                         if (!(pty == null)) {
                             Item item = feedListTable.getItem(feedListTable
                                     .getValue()); // title + url + entries
                             String url2 = (String) indexedFeedContainer
                                     .getContainerProperty(pty.getValue(), "Url")
                                     .getValue();
                             selectedItem = item.toString();
                             selectedItemUrl = url2;
 
                             if (selectedItem != null) {
                                 entrySearchTextField.setValue("");
                                 indexedEntryContainer = insertEntries();
                                 entryListTable
                                         .setContainerDataSource(indexedEntryContainer);
                             }
                         }
 
                     }
 
                 });
     }
 
     private IndexedContainer insertEntries() {
         IndexedContainer ic = new IndexedContainer();
 
         // Insert table title
         for (String p : entryNames) {
             ic.addContainerProperty(p, String.class, "");
         }
 
         if (selectedItem != null) {
             AtomReader ar = new AtomReader();
             SyndFeed selectedFeed = ar.getFeed(selectedItemUrl);
             ArrayList<SyndEntry> entries = ar.getEntries(selectedFeed);
 
             ArrayList<String> entryTitles = new ArrayList<String>();
             for (SyndEntry entry : entries) {
                 entryTitles.add(entry.getTitle());
             }
 
             String[] enames = entryTitles.toArray(new String[entries.size()]);
 
             for (int i = 0; i < enames.length; i++) {
                 Object id = ic.addItem();
                 ic.getContainerProperty(id, ENTRYNAME).setValue(enames[i]);
             }
             entryListTable.setSelectable(true);
             entryListTable.setReadOnly(false);
         } else {
             String[] enames = { "" };
 
             for (int i = 0; i < enames.length; i++) {
                 Object id = ic.addItem();
                 ic.getContainerProperty(id, ENTRYNAME).setValue(enames[i]);
             }
             entryListTable.setSelectable(false);
             entryListTable.setReadOnly(true);
         }
 
         return ic;
     }
 
     /**
      * Inits the layout. See: https://vaadin.com/tutorial
      */
     private void initLayout() {
 
         HorizontalSplitPanel splitPanel = new HorizontalSplitPanel();
         Component toolbar = initToolBar();
 
         HorizontalSplitPanel splitPanel2 = new HorizontalSplitPanel();
 
         splitPanel.addComponent(leftLayout);
         splitPanel.addComponent(splitPanel2);
         splitPanel.setSizeFull();
 
         splitPanel2.addComponent(centerLayout);
         splitPanel2.addComponent(rightLayout);
 
         leftLayout.addComponent(feedListTable);
         leftLayout.addComponent(feedSearchTextField);
 
         centerLayout.addComponent(entryListTable);
         centerLayout.addComponent(entrySearchTextField);
 
         leftLayout.setSizeFull();
         leftLayout.setExpandRatio(feedListTable, 1);
         centerLayout.setExpandRatio(entryListTable, 1);
         feedListTable.setSizeFull();
         // feedSearchTextField.setSizeFull();
         rightLayout.setMargin(true);
 
         centerLayout.setSizeFull();
         // centerLayout.setExpandRatio(entryListTable, 1);
         entryListTable.setSizeFull();
         // entrySearchTextField.setSizeFull();
 
         HorizontalLayout btnHolder = new HorizontalLayout();
         // Create a selection component
 
         select.setNullSelectionAllowed(false);
 
         // Add some items and give each an item ID
         select.addItem("HTML");
         select.addItem("XML");
         select.addItem("TEXT");
         select.addItem("PREFORMATTED");
         select.addItem("RAW");
         select.select("HTML");
         select.setImmediate(true);
         btnHolder.addComponent(select);
 
         rightLayout.addComponent(btnHolder);
 
         rightLayout.addComponent(readArea);
         rightLayout.setExpandRatio(readArea, 1);
         leftLayout.setExpandRatio(feedSearchTextField, 0.0f);
         centerLayout.setExpandRatio(entrySearchTextField, 0.0f);
         feedSearchTextField.setWidth("100%");
         entrySearchTextField.setWidth("100%");
 
         VerticalLayout vlayout = new VerticalLayout(toolbar, splitPanel);
         vlayout.setExpandRatio(toolbar, 0.0f);
         vlayout.setExpandRatio(splitPanel, 1.0f);
         vlayout.setSizeFull();
         setContent(vlayout);
 
     }
 
     /**
      * Inits the tool bar.
      * 
      * @return the component
      */
     private Component initToolBar() {
         HorizontalLayout lo = new HorizontalLayout();
         lo.addComponent(addNewFeedButton);
         lo.addComponent(removeFeedButton);
         lo.addComponent(helpButton);
         lo.addComponent(loginButton);
         lo.addComponent(logoutButton);
         lo.setSizeUndefined();
         return lo;
     }
 
     /**
      * Insert feeds to the indexed container that holds the feeds. The contents
      * are shown in the leftmost table. The contents are read from a file. TODO:
      * Handling of exceptions. TODO: If the list is empty, could a help window
      * etc be automatically shown to the user that guides what to do next?
      * 
      * @return the indexed container
      */
 
     public IndexedContainer insertFeeds() {
         System.out.println("Listing feeds");
         IndexedContainer ic = new IndexedContainer();
 
         // Add the column name to the table.
         for (String p : feedNames) {
             ic.addContainerProperty(p, String.class, "");
         }
 
         ArrayList<String> fileContents = new FileRdr()
                 .readFile(GLOBAL_VARIABLES.getFilePath()
                         + GLOBAL_VARIABLES.getUsrName()
                         + GLOBAL_VARIABLES.getFileName());
 
         // Feed container is empty. Insert dummy feed, disable selection.
         if (fileContents.size() == 0) {
             System.out.println("Selected: " + feedListTable.getValue());
 
             Object id = ic.addItem();
             ic.getContainerProperty(id, FEEDNAME).setValue(
                     "Start by adding feeds!");
             ic.getContainerProperty(id, FEEDURL).setValue("");
 
             feedListTable.setSelectable(false);
             feedListTable.setReadOnly(true);
 
         } else {
             feedListTable.setSelectable(true);
             feedListTable.setReadOnly(false);
 
             for (String s : fileContents) {
                 String[] row = s.split("\\|");
                 String url = row[0];
                 String name = row[1];
                 String entries = row[2];
                System.out.println("Row: " + s);
 
                 Object id = ic.addItem();
                 ic.getContainerProperty(id, FEEDNAME).setValue(name);
                 ic.getContainerProperty(id, FEEDURL).setValue(url);
                 ic.getContainerProperty(id, FEEDENTRIES).setValue(entries);
             }
         }
         return ic;
     }
 }
