 package com.github.mailerific.client;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.DecoratorPanel;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 public class DialogFactory {
 
     private static DialogBox SINGLETON_SUCCESS;
     private static DialogBox SINGLETON_PROMOTE;
 
     private DialogFactory() {
     }
 
     private static final String FIRST_TIME_CONTENTS = "<div class=\"dialog-body\"><p>Since this is your first time here,"
             + " let me introduce you to Mailerific with a few quick examples. Mailerific is a service that"
             + " allows you to send messages using URLs. You can use it to send reminders,"
             + " messages and tasks to yourself or to other people just by using your browser's address bar."
             + " For example, to send yourself a reminder to feed the cat when you get home,"
             + " just type the following in your address bar:</p>"
             + "<div>"
             + GWT.getHostPageBaseURL()
             + "i/Feed the cat after reading this!</div>"
             + "<p>An e-mail will be  automatically sent to your GMail account. That's it, no set-up"
             + " necessary.</p><p>In addition to reminders, you can also send e-mails to other e-mail accounts.</p>"
             + "<div>"
             + GWT.getHostPageBaseURL()
             + "o/john@example.com/Hey John! Let's meet up this friday!</div>"
             + "<p>The above URLs can be sent anytime, anyplace, as long as you are logged in to your"
             + " Google account. If you want to enable sending without logging in, there's an"
             + " option in the Settings where you can enable public access using the /p namespace. Enjoy!</p></div>";
 
     public static DialogBox createFirstTimeDialog() {
         return createGenericDialog("Welcome to Mailerific!",
                 FIRST_TIME_CONTENTS);
     }
 
     public static DialogBox createErrorDialog() {
         return createGenericDialog(
                 "Oops!",
                 "It looks like something went wrong. We apologize for the inconvenience and "
                         + "hope to resolve it soon. Thanks for your understanding!",
                 false, "600px");
     }
 
     public static DialogBox createPromoteDialog() {
         if (SINGLETON_PROMOTE == null) {
             SINGLETON_PROMOTE = createPromoteDialogInternal();
         }
         return SINGLETON_PROMOTE;
     }
 
     public static DialogBox createNotLoggedInErrorDialog() {
         return createGenericDialog("Please sign in",
                 "It looks like your session has expired. Please log-in again.",
                 false, "300px");
     }
 
     public static DialogBox createSaveSuccessDialog() {
         if (SINGLETON_SUCCESS == null) {
             SINGLETON_SUCCESS = createGenericDialog("Settings saved",
                     "Your settings have been saved.", false, "300px");
         }
         return SINGLETON_SUCCESS;
     }
 
     public static DialogBox createInvalidInputDialog() {
         return createGenericDialog(
                 "Invalid Input",
                 "Invalid input has been detected. Please fix it before saving.",
                 false, "300px");
     }
 
     private static DialogBox createGenericDialog(final String caption,
             final String text) {
         return createGenericDialog(caption, text, true, "600px");
     }
 
     public static DialogBox createSignOutDialog(final Long id, final String url) {
         return createSignOutDialogInternal(id, url);
     }
 
     private static DialogBox createGenericDialog(final String caption,
             final String text, final boolean showCloseButton, final String width) {
         final DialogBox dialogBox = new DialogBox(!showCloseButton);
         dialogBox.setText(caption);
         VerticalPanel dialogContents = new VerticalPanel();
         dialogContents.setSpacing(4);
         dialogBox.setWidget(dialogContents);
 
         // Add some text to the dialog
         HTML details = new HTML(text);
         dialogContents.add(details);
         dialogContents.setCellHorizontalAlignment(details,
                 HasHorizontalAlignment.ALIGN_CENTER);
 
         if (showCloseButton) {
             // Add a close button at the bottom of the dialog
             Label button = new Label("Close");
             button.setStyleName("button-style");
             button.setWidth("50px");
             button.addClickHandler(new ClickHandler() {
                 public void onClick(final ClickEvent event) {
                     dialogBox.hide();
                 }
             });
             DecoratorPanel buttonPanel = new DecoratorPanel();
             buttonPanel.add(button);
             buttonPanel.addStyleName("content-button");
             dialogContents.add(buttonPanel);
             dialogContents.setCellHorizontalAlignment(buttonPanel,
                     HasHorizontalAlignment.ALIGN_RIGHT);
         }
 
         dialogBox.setAnimationEnabled(true);
         dialogBox.setWidth(width);
         // Return the dialog box
         return dialogBox;
     }
 
     private static DialogBox createPromoteDialogInternal() {
         final DialogBox dialogBox = new DialogBox(true);
         dialogBox.setText("Promote Mailerific!");
         HorizontalPanel dialogContents = new HorizontalPanel();
         dialogContents.setSpacing(10);
         dialogBox.setWidget(dialogContents);
 
         Label button = new Label("Tweet this!");
         button.setStyleName("button-style");
         button.addClickHandler(new ClickHandler() {
             @Override
             public void onClick(final ClickEvent event) {
                 Window.Location
                         .assign("http://twitter.com?status=Try out Mailerific! http://mailerific.appspot.com");
             }
         });
         DecoratorPanel buttonPanel = new DecoratorPanel();
         buttonPanel.add(button);
         buttonPanel.addStyleName("content-button");
         dialogContents.add(buttonPanel);
 
         button = new Label("Follow me on Twitter");
         button.setStyleName("button-style");
         button.addClickHandler(new ClickHandler() {
             @Override
             public void onClick(final ClickEvent event) {
                 Window.Location.assign("http://twitter.com/weynsee");
             }
         });
         buttonPanel = new DecoratorPanel();
         buttonPanel.add(button);
         buttonPanel.addStyleName("content-button");
         dialogContents.add(buttonPanel);
 
         return dialogBox;
     }
 
     private static DialogBox createSignOutDialogInternal(final Long id,
             final String logoutUrl) {
         final DialogBox dialogBox = new DialogBox(true);
         dialogBox.setText("Signing out");
         VerticalPanel dialogContents = new VerticalPanel();
         dialogContents.setSpacing(10);
         dialogBox.setWidget(dialogContents);
 
         HTML label = new HTML(
                 "Do you wish to sign out just from this session, or permanently?<br/> If you sign out completely, all your messages in Mailerific will be deleted.");
         dialogContents.add(label);
 
         HorizontalPanel buttons = new HorizontalPanel();
         buttons.setWidth("100%");
         buttons.setSpacing(4);
         Label button = new Label("Don't worry, I'll be back!");
         button.setStyleName("button-style");
         button.addClickHandler(new ClickHandler() {
             @Override
             public void onClick(final ClickEvent event) {
                 Window.Location.assign(logoutUrl);
             }
         });
 
         DecoratorPanel buttonPanel = new DecoratorPanel();
         buttonPanel.add(button);
         buttonPanel.addStyleName("content-button");
         buttons.add(buttonPanel);
         buttons.setCellHorizontalAlignment(buttonPanel,
                 HorizontalPanel.ALIGN_LEFT);
 
        button = new Label("This isn't for me");
         button.setStyleName("button-style");
         button.addClickHandler(new ClickHandler() {
             @Override
             public void onClick(final ClickEvent event) {
                 UserAccountServiceAsync.RPC.removeUser(id,
                         new DefaultCallback<Void>() {
                             @Override
                             public void onSuccess(final Void result) {
                                 Window.Location.assign(logoutUrl);
                             }
                         });
             }
         });
         DecoratorPanel buttonPanel2 = new DecoratorPanel();
         buttonPanel2.add(button);
         buttonPanel2.addStyleName("content-button");
         buttons.add(buttonPanel2);
         buttons.setCellHorizontalAlignment(buttonPanel2,
                 HorizontalPanel.ALIGN_RIGHT);
 
         dialogContents.add(buttons);
 
         return dialogBox;
     }
 }
