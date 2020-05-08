 package mySampleApplication.client;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.uibinder.client.UiTemplate;
 import com.google.gwt.user.client.ui.*;
 import com.google.gwt.user.client.ui.TextArea;
 
 
 public class AddTextDialog extends CafaWidget implements Dialog {
 
     @UiTemplate("AddTextDialog.ui.xml")
     interface AddTextDialogUiBinder extends UiBinder<Widget, AddTextDialog> {
     }
 
     private static AddTextDialogUiBinder uiBinder = GWT.create(AddTextDialogUiBinder.class);
 
 
     @UiField
     TextArea textArea;
 
     @UiField
     HTMLPanel tablePanel;
 
     @UiField
     Button sendTextButton;
 
     @UiField
     Button translateTextButton;
 
     @UiField
     Button translateSubtitlesButton;
 
     public AddTextDialog() {
         super();
         initWidget(uiBinder.createAndBindUi(this));
     }
 
     public void init() {
         sendTextButton.setVisible(true);
         textArea.setVisible(true);
         translateTextButton.setVisible(false);
         translateSubtitlesButton.setVisible(false);
         tablePanel.setVisible(false);
     }
 
     @UiHandler("textArea")
    public void textAreaClick(ClickEvent e) {
         textArea.setHeight("480px");
    }
 
 
      @UiHandler("sendTextButton")
 	public void sendTextButtonClick(ClickEvent e) {
          tablePanel.setVisible(true);
          textArea.setVisible(false);
          sendTextButton.setVisible(false);
          translateTextButton.setVisible(true);
          translateSubtitlesButton.setVisible(true);
          process(textArea.getText());
 	}
 
     @UiHandler("translateTextButton")
 	public void translateTextButtonClick(ClickEvent e) {
         setComponentVisibility();
          translateText(textArea.getText());
 	}
 
         @UiHandler("translateSubtitlesButton")
 	public void translateSubtitlesButtonClick(ClickEvent e) {
          setComponentVisibility();
          translateSubtitles(textArea.getText());
 	}
 
     private void setComponentVisibility() {
         textArea.setVisible(true);
         sendTextButton.setVisible(true);
         translateTextButton.setVisible(false);
         translateSubtitlesButton.setVisible(false);
         tablePanel.setVisible(false);
     }
 
     public native void process(String text) /*-{
          $wnd.EnglishTranslator.extractWords(text, function(words){
              $wnd.popup.createTable(words);
          });
     }-*/;
 
 
     public void setText(String text){
         textArea.setText(text);
     }
 
 
     public native String translateText(String text) /*-{
          var words = $wnd.popup.listWordsFromTable();
         var instance = this;
 
         $wnd.EnglishTranslator.translate(words, function(translatedWords) {
              $wnd.ajaxExecutor.loadOptions(function(optionsData){
                 var pattern = optionsData['textTemplate'];
                 text = $wnd.EnglishTranslator.putTranslationInText(translatedWords, text, pattern);
                 instance.@mySampleApplication.client.AddTextDialog::setText(Ljava/lang/String;)(text);
              });
         });
     }-*/;
 
      public native String translateSubtitles(String text) /*-{
          var words = $wnd.popup.listWordsFromTable();
         var instance = this;
 
         $wnd.EnglishTranslator.translate(words, function(translatedWords) {
              $wnd.ajaxExecutor.loadOptions(function(optionsData){
                 var pattern = optionsData['subtitleTemplate'];
                 text = $wnd.EnglishTranslator.putTranslationInText(translatedWords, text, pattern);
                 instance.@mySampleApplication.client.AddTextDialog::setText(Ljava/lang/String;)(text);
              });
         });
     }-*/;
 
 
 }
