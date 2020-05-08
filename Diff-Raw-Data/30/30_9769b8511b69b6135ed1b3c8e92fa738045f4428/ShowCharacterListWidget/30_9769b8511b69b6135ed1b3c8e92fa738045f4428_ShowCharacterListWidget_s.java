 package org.timadorus.webapp.client.character.ui.characterlist;
 
 import java.util.List;
 
 import org.timadorus.webapp.beans.Character;
 import org.timadorus.webapp.beans.User;
 import org.timadorus.webapp.client.DefaultTimadorusWebApp;
 import org.timadorus.webapp.client.character.ui.DefaultActionHandler;
 import org.timadorus.webapp.client.character.ui.showcharacter.CharacterActionHandler;
 import org.timadorus.webapp.client.eventhandling.events.ShowCharacterListEvent;
 import org.timadorus.webapp.client.eventhandling.events.ShowSelectCharacterDetailEvent;
 import org.timadorus.webapp.client.eventhandling.events.ShowSelectCharacterShortEvent;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.FormPanel;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PasswordTextBox;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * Panel to show a list of characters
  * 
  * @author aaz210
  * 
  */
 public class ShowCharacterListWidget extends FormPanel implements ShowCharacterListDialog.Display {
 
   private Grid grid;
 
   private VerticalPanel panel;
 
   private HTML headline;
 
   private PasswordTextBox passbox;
 
   private Button confirm;
 
   private Button back;
 
   private CharacterActionHandler detailsHandler;
 
   private CharacterActionHandler characterDeleteHandler;
 
   public ShowCharacterListWidget() {
     super();
 
     panel = new VerticalPanel();
     headline = new HTML("<h1>Liste der registrierten Charaktere</h1>");
     passbox = new PasswordTextBox();
     confirm = new Button("Loeschen bestaetigen");
     back = new Button("Zurueck");
 
    grid = new Grid();
     grid.setBorderWidth(0);
 
     grid.setBorderWidth(0);
     grid.setWidget(0, 0, new Label("Es wurden keine Charaktere gefunden."));
 
     panel.clear();
     panel.add(headline);
     panel.add(grid);
     setContent(panel);
 
     grid = new Grid(1, 1);
     grid.setWidget(0, 0, new Label("Waiting for ajax response..."));
 
     panel.setStyleName("panel");
     panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
     panel.setWidth("100%");
 
     panel.add(headline);
    panel.add(grid);
 
     RootPanel.get("content").clear();
     RootPanel.get("content").add(panel);
 
     RootPanel.get("information").clear();
   }
 
   public void onDetailsButtonClick(Character character) {
     grid.clear();
     panel.clear();
     headline = new HTML("<h1>Charakter-Details</h1>");
     if (detailsHandler != null) {
       detailsHandler.onAction(character, "");
     }
     panel.add(headline);
     panel.add(grid);
   }
 
   @Override
   public void onDeleteButtonClick() {
     grid.clear();
     panel.clear();
     final int rows = 4;
     grid = new Grid(rows, 1);
     headline = new HTML("<h1>Charakter löschen</h1>");
     grid.setWidget(0, 0, new Label("Sind Sie sich sicher, dass Sie diesen Charakter löschen wollen?"));
     grid.setWidget(1, 0, passbox);
     grid.setWidget(2, 0, confirm);
     final int row = 3;
     grid.setWidget(row, 0, back);
     panel.add(headline);
     panel.add(grid);
   }
 
   /**
    * Handles the character deletion.
    */
   private void onConfirmDeleteCharacter(Character character) {
     if (characterDeleteHandler != null) {
       characterDeleteHandler.onAction(character, passbox.getText());
     }
   }
 
   /**
    * Shows a dialog box with a title and message.
    * 
    * @param title
    *          the title of the dialog box
    * @param message
    *          the message in the dialog box
    */
   public void showDialogBox(String title, String message) {
     // Create the popup dialog box
     final DialogBox dialogBox = new DialogBox();
 
     dialogBox.setText(title);
     dialogBox.setAnimationEnabled(true);
     final Button closeButton = new Button("Close");
 
     // We can set the id of a widget by accessing its Element
     closeButton.getElement().setId("closeButton");
 
     VerticalPanel dialogVPanel = new VerticalPanel();
     dialogVPanel.addStyleName("dialogVPanel");
 
     dialogVPanel.add(new HTML((new StringBuffer().append("<b>").append(message).append("</b>")).toString()));
     dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
     dialogVPanel.add(closeButton);
     dialogBox.setWidget(dialogVPanel);
 
     dialogBox.center();
 
     // Add a handler to close the DialogBox
     closeButton.addClickHandler(new ClickHandler() {
       /**
        * Will be triggered if the the button has been clicked.
        * 
        * @param event
        *          The ClickEvent object
        */
       public void onClick(ClickEvent event) {
         dialogBox.hide();
       }
     });
   }
 
   /**
    * Clears the panel and adds a widget to it.
    * 
    * @param w
    *          the widget to be added.
    */
   private void setContent(Widget w) {
     RootPanel.get("content").clear();
     RootPanel.get("content").add(w);
   }
 
   @Override
   public FormPanel getFormPanel() {
     return this;
   }
 
   @Override
   public void addBackButtonHandler(final DefaultActionHandler handler) {
     back.addClickHandler(new ClickHandler() {
 
       @Override
       public void onClick(ClickEvent event) {
         handler.onAction();
       }
     });
   }
 
   @Override
   public void characterDeleteSuccessfull() {
     showDialogBox("Information", "Ihr Charakter wurde erfolgreich entfernt!");
   }
 
   @Override
   public void characterDeleteFailure() {
     passbox.setText("");
     showDialogBox("Fehlermeldung", "Passwort falsch! Versuchen Sie es erneut!");
   }
 
   @Override
   public void addDeleteConfirmedHandler(CharacterActionHandler handler) {
     this.characterDeleteHandler = handler;
   }
 
   @Override
   public void addDetailsButtonHandler(CharacterActionHandler handler) {
     this.detailsHandler = handler;
   }
 
   @Override
   public void loadShowCharacterDialog(DefaultTimadorusWebApp entry, Character character, User user) {
     entry.fireEvent(new ShowSelectCharacterDetailEvent(grid, 0, 0, user, character));
   }
 
   @Override
   public void loadShowCharacterListDialog(DefaultTimadorusWebApp entry, User user) {
     entry.fireEvent(new ShowCharacterListEvent(user));
   }
 
   @Override
   public void setCharacterList(List<Character> characters, User user, DefaultTimadorusWebApp entry) {
     if (characters.size() > 0) {
      grid.clear();
       int i = 0;
       for (final Character character : characters) {
         final Button delete = new Button("L&ouml;schen");
         final Button details = new Button("Details");
 
         // creates a click and keyboard handler for the confirm button
         class ConfirmHandler implements ClickHandler, KeyUpHandler {
           /**
            * Will be triggered if the button was clicked.
            * 
            * @param event
            *          The click event
            */
           public void onClick(ClickEvent event) {
             onConfirmDeleteCharacter(character);
           }
 
           /**
            * Will be triggered if the "Enter" button was hit while located in an input field
            * 
            * @param event
            *          The KeyUpEvent
            */
           public void onKeyUp(KeyUpEvent event) {
             if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
               onConfirmDeleteCharacter(character);
             }
           }
         }
 
         delete.addClickHandler(new ClickHandler() {
 
           @Override
           public void onClick(ClickEvent event) {
             onDeleteButtonClick();
           }
         });
         details.addClickHandler(new ClickHandler() {
 
           @Override
           public void onClick(ClickEvent event) {
             onDetailsButtonClick(character);
           }
         });
 
         ConfirmHandler confirmHandler = new ConfirmHandler();
         confirm.addClickHandler(confirmHandler);
         passbox.addKeyUpHandler(confirmHandler);
 
         entry.fireEvent(new ShowSelectCharacterShortEvent(grid, i, 0, user, character));
         grid.setWidget(i, 1, details);
         grid.setWidget(i, 2, delete);
         i++;
       }
     } else {
       grid = new Grid(1, 1);
       grid.setBorderWidth(0);
       grid.setWidget(0, 0, new Label("Es wurden keine Charaktere gefunden."));
     }
   }
 }
