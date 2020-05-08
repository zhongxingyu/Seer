 /*
  * Filename:    ReaderUi.java
  * Author:      Pekka Nurminen
  * History:     Created: 07022013
  * Description: Main application class.
  */
 package com.reader.ui;
 
 import java.util.ArrayList;
 
 import com.reader.data.FeedContainer;
 import com.reader.io.AtomReader;
 import com.reader.io.FileRdr;
 import com.reader.io.FileWrtr;
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.vaadin.data.Container;
 import com.vaadin.data.Item;
 import com.vaadin.data.Property;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.data.fieldgroup.FieldGroup;
 import com.vaadin.data.util.IndexedContainer;
 import com.vaadin.server.VaadinRequest;
 import com.vaadin.shared.ui.label.ContentMode;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.HorizontalSplitPanel;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Table;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.UI;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Window;
 
 /**
  * Main UI class.
  * 
  * @author pekka
  */
 @SuppressWarnings("serial")
 public class ReaderUI extends UI {
 
     /** The feed list table. */
     private final Table feedListTable = new Table();
 
     /** The entry list table. */
     private final Table entryListTable = new Table();
 
     /** The feed search. */
     private final TextField feedSearchTextField = new TextField();
 
     /** The entry search. */
     private final TextField entrySearchTextField = new TextField();
 
     /** The add new feed. */
     private final Button addNewFeedButton = new Button("New feed");
 
     /** The remove feed. */
     private final Button removeFeedButton = new Button("Remove feed");
 
     /** The help. */
     private final Button helpButton = new Button("Help");
 
     /** The read area. */
     Label readArea = new Label();
 
     /**
      * The feedContainer. Filled in the application initialization. Contents are
      * read from file. Afterwards the file is updated, but the file itself is
      * not read anymore.
      */
     FeedContainer feedContainer = fillFeedContainer();
 
     /** The indexed feed container. */
     IndexedContainer indexedFeedContainer = insertFeeds();
 
     /** The Constant FEEDNAME. */
     private static final String FEEDNAME = "Feed";
 
     /** The Constant feedNames. */
     private static final String[] feedNames = new String[] { FEEDNAME };
 
     /** The indexed entry container. */
     IndexedContainer indexedEntryContainer = new IndexedContainer(); // insertEntries();
 
     /** The Constant ENTRYNAME. */
     private static final String ENTRYNAME = "Entry";
 
     /** The Constant entryNames. */
     private static final String[] entryNames = new String[] { ENTRYNAME };
 
     /** The Constant fieldNames. */
     private static final String[] fieldNames = new String[] { FEEDNAME,
             ENTRYNAME };
 
     /** The read area fields. */
     private final FieldGroup readAreaFieldsFieldGroup = new FieldGroup();
 
     /** The main window. */
     Window mainWindow;
 
     /** The add feed window. */
     Window addFeedWindow;
 
     /** The help window. */
     Window helpWindow;
 
     /** The selected item. */
     String selectedItem;
 
     /** The selected item url. */
     String selectedItemUrl;
 
     /** The selected entry. */
     String selectedEntry;
 
     // ONLY FOR TESTING
     /** The username. */
     private final String username = "Pekka";
 
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
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.ui.UI#init(com.vaadin.server.VaadinRequest)
      */
     @Override
     protected void init(VaadinRequest request) {
 
         initToolBar();
         initLayout();
         initFeedList();
         initEntryList();
         initButtons();
     }
 
     /**
      * Inits the application buttons.
      */
     private void initButtons() {
 
         addNewFeedButton.addClickListener(new ClickListener() {
             @Override
             public void buttonClick(ClickEvent event) {
                 addFeedWindow = new AddFeedWindow();
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
                 String selected = selectedItem;
                 Object firstObject = feedListTable.firstItemId();
                 feedListTable.unselect(selectedObject);
 
                 System.out.println("Removing " + selectedObject + " "
                         + selected);
 
                 // TODO: Removing:
                 // 1. Only one item available in the list. --> What to select??
                 // 2. First item will be deleted. --> Select the second item? Or
                 // null?
 
                 System.out.println("Feedcontainer size before deletion: "
                         + feedContainer.getSize());
 
                 // Remove the item from the feed container
                 feedContainer.removeFeed(selected);
                 System.out.println("Feedcontainer size after deletion:"
                         + feedContainer.getSize());
                 // Remove the item from the list
                 feedListTable.removeItem(selectedItem);
 
                 // Write the updated feedContainer to the file
                 String[] tmp = feedContainer.getFeedsAsArray();
                 String p = "";
                 for (String s : tmp) {
                     System.out.println("Feeds after deletion: " + s);
                     p = p + s;
                 }
                 FileWrtr writer = new FileWrtr();
                writer.createFile("/Users/pekka/Documents/workspace/Reader/"
                        + username + "_Feeds.txt", p);
                 // TODO: How to refresh the feed list!
 
                 indexedFeedContainer = insertFeeds();
                 initFeedList();
                 entryListTable.removeAllItems();
                 if (feedListTable.size() > 0) {
                     feedListTable.select(firstObject);
                 }
             }
 
         });
 
     }
 
     /**
      * Inits the read area when the user has selected a feed and entry. TODO: ?
      * Could the layout be changed automatically depending on the content? If
      * the content contains a picture for example, the read area could be
      * repositioned somehow.
      */
     private void initReadArea() {
 
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
 
                     readArea.setContentMode(ContentMode.HTML);
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
 
                                 initReadArea();
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
         feedListTable.setVisibleColumns(new String[] { FEEDNAME });
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
                         if (pty == feedListTable) {
                             Item item = feedListTable.getItem(feedListTable
                                     .getValue());
                             Object feedId = feedListTable.getValue();
 
                             if (item != null) {
                                 selectedItem = item.toString();
                                 selectedItemUrl = feedContainer
                                         .getFeedUrl(selectedItem);
                             }
                             if (feedId != null) {
                                 entryListTable
                                         .setContainerDataSource(changeEntryListContents(feedId));
                             }
                         }
 
                     }
 
                     private Container changeEntryListContents(Object feedId) {
                         IndexedContainer ic = new IndexedContainer();
 
                         // Insert table title
                         for (String p : entryNames) {
                             ic.addContainerProperty(p, String.class, "");
                         }
 
                         AtomReader ar = new AtomReader();
                         SyndFeed selectedFeed = ar.getFeed(selectedItemUrl);
                         ArrayList<SyndEntry> entries = ar
                                 .getEntries(selectedFeed);
 
                         ArrayList<String> entryTitles = new ArrayList<String>();
                         for (SyndEntry entry : entries) {
                             entryTitles.add(entry.getTitle());
                         }
 
                         String[] enames = entryTitles
                                 .toArray(new String[entries.size()]);
 
                         for (int i = 0; i < enames.length; i++) {
                             Object id = ic.addItem();
                             ic.getContainerProperty(id, ENTRYNAME).setValue(
                                     enames[i]);
                         }
 
                         return ic;
                     }
                 });
     }
 
     /**
      * Inits the layout. See: https://vaadin.com/tutorial
      */
     private void initLayout() {
 
         HorizontalSplitPanel splitPanel = new HorizontalSplitPanel();
         Component toolbar = initToolBar();
         VerticalLayout vlayout = new VerticalLayout(toolbar, splitPanel);
 
         setContent(vlayout);
 
         HorizontalSplitPanel splitPanel2 = new HorizontalSplitPanel();
 
         splitPanel.addComponent(leftLayout);
         splitPanel.addComponent(splitPanel2);
 
         splitPanel2.addComponent(centerLayout);
         splitPanel2.addComponent(rightLayout);
 
         leftLayout.addComponent(feedListTable);
         leftLayout.addComponent(feedSearchTextField);
 
         centerLayout.addComponent(entryListTable);
         centerLayout.addComponent(entrySearchTextField);
 
         leftLayout.setSizeFull();
         leftLayout.setExpandRatio(feedListTable, 1);
         feedListTable.setSizeFull();
         feedSearchTextField.setSizeFull();
         feedSearchTextField.setValue("Search feeds");
         rightLayout.setMargin(true);
 
         centerLayout.setSizeFull();
         centerLayout.setExpandRatio(entryListTable, 1);
         entryListTable.setSizeFull();
         entrySearchTextField.setSizeFull();
         entrySearchTextField.setValue("Search entries");
         rightLayout.addComponent(readArea);
 
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
 
     public FeedContainer fillFeedContainer() {
         FeedContainer fc = new FeedContainer();
         System.out.println("Reading feeds from the file...");
         FileRdr fileReader = new FileRdr();
         String fileContents = fileReader
                .readFile2("/Users/pekka/Documents/workspace/Reader/"
                        + username + "_feeds.txt");
 
         // File is not empty:
         System.out.println("File length: " + fileContents.length());
         if (fileContents.length() != 0) {
             String[] splittedFile = fileContents.split("\n");
 
             for (String s : splittedFile) {
                 String[] splittedRow = s.split("\\|");
                 fc.addFeed(splittedRow[1], splittedRow[0]);
             }
         }
         return fc;
     }
 
     public IndexedContainer insertFeeds() {
 
         System.out.println("Listing feeds");
         IndexedContainer ic = new IndexedContainer();
 
         // Add the column name to the table.
         for (String p : feedNames) {
             ic.addContainerProperty(p, String.class, "");
         }
 
         // Feed container is empty. Insert dummy feed, disable selection.
         if (feedContainer.getSize() == 0) {
             System.out
                     .println("FeedContainer is empty! Guide user to add Atom feeds!");
             System.out.println("Selected: " + feedListTable.getValue());
 
             String[] fnames = { "Start by adding feeds!" };
 
             for (int i = 0; i < fnames.length; i++) {
                 Object id = ic.addItem();
                 ic.getContainerProperty(id, FEEDNAME).setValue(fnames[i]);
             }
             feedListTable.setSelectable(false);
             feedListTable.setReadOnly(true);
 
         } else {
             feedListTable.setSelectable(true);
             feedListTable.setReadOnly(false);
 
             String[] fnames = feedContainer.getFeedTitlesAsArray();
 
             for (String s : fnames) {
                 System.out.println("Feednames: " + s);
             }
 
             for (int i = 0; i < fnames.length; i++) {
                 Object id = ic.addItem();
                 ic.getContainerProperty(id, FEEDNAME).setValue(fnames[i]);
             }
         }
 
         return ic;
     }
 }
