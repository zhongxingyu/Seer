 /*
  * Filename:    AddFeedWindow.java
  * Author:      Pekka Nurminen
  * History:     Created: 07022013
  * Description: Vaadin window for adding new feed process.
  */
 package com.reader.ui;
 
 import java.util.ArrayList;
 
 import com.reader.data.Feed;
 import com.reader.io.AtomReader;
 import com.reader.io.FileRdr;
 import com.reader.io.FileWrtr;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Window;
 
 /**
  * The Class AddFeedWindow.
  */
 public class AddFeedWindow extends Window {
 
     /** The feed address text field. */
     private final TextField feedAddressTextField = new TextField("Feed address");
 
     /** The add button. */
     private final Button addButton = new Button("Add");
 
     /**
      * ONLY FOR TESTING! The username that is used for saving the file. Replace
      * with the user context that is set during the login. Eventually could be
      * used also with a database.
      */
     private final String username = "Pekka";
 
     // TODO: ? Do we need a cancel button or is the X enough for cancelling the
     // operation? If needed, use the button below and create a method that gets
     // the current window and closes it.
 
     // private final Button cancelButton = new Button("Cancel");
 
     // TODO: Handling of malformed URL.
     // TODO: Correct URL but not containing FEED.
 
     /**
      * Instantiates a new adds the feed window.
      */
     public AddFeedWindow() {
         setCaption("Add new feed");
         setModal(true);
         VerticalLayout layout = new VerticalLayout();
         feedAddressTextField.setSizeFull();
         addButton.setSizeFull();
         layout.addComponent(feedAddressTextField);
         layout.addComponent(addButton);
         // layout.addComponent(cancelButton);
         setContent(layout);
         final Feed feed = new Feed();
 
         /**
          * Clicklistener for add button.
          */
         addButton.addClickListener(new ClickListener() {
             @Override
             public void buttonClick(ClickEvent event) {
                 // TODO: Check that the textfield is not empty!
                 // TODO: Check that the textfield contains an URL (if possible).
                 String url = feedAddressTextField.getValue();
 
                 if (!isDuplicate(url)) {
                     FileWrtr writer = new FileWrtr();
                     AtomReader atomReader = new AtomReader();
 
                     feed.setUri(feedAddressTextField.getValue());
 
                     feed.setTitle(atomReader.getFeedTitle(atomReader
                             .getFeed(feed.getUri())));
 
                     feed.setEntryCount(atomReader.getEntries(
                             atomReader.getFeed(url)).size());
 
                     System.out.println("Adding: " + feed.toString());
 
                    writer.appendToFile(
                            "/Users/pekka/Documents/workspace/Reader/"
                                    + username + "_Feeds.txt", feed.toString());
                 }
 
                 // TODO: Close the window on success, below does not work.
                 // getApplication().getMainWindow.removeWindow(getWindow());
 
                 // TODO: Refresh the feedlist on the main page automatically.
             }
 
             /*
              * Method for checking if the feed under addition is already
              * container in the user's feed list file.
              */
             private boolean isDuplicate(String url) {
 
                 boolean result = false;
                 FileRdr fileReader = new FileRdr();
                 ArrayList<String> contents = fileReader
                        .readFile("/Users/pekka/Documents/workspace/Reader/"
                                + username + "_feeds.txt");
 
                 for (String s : contents) {
                     String[] splittedRow = s.split("\\|");
                     if (splittedRow[0].equals(url)) {
                         System.out
                                 .println("Duplicate! Show user notification.");
                         result = true;
                         break;
                     } else {
                         System.out.println("Not duplicate, add to list.");
 
                         result = false;
                     }
                 }
                 return result;
             }
         });
 
         // TODO: Cancel button eventhandling if needed. If not needed -> delete.
 
         // cancelButton.addClickListener(new ClickListener() {
         // @Override
         // public void buttonClick(ClickEvent event) {
         // System.out.println("Cancel button pressed.");
         // (Window)
         // }
         // });
     }
 }
