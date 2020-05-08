 package com.greymax.vkplayer.ui.components.autocomplete;
 
 import com.sun.javafx.scene.control.skin.SkinBase;
 import javafx.beans.InvalidationListener;
 import javafx.beans.Observable;
 import javafx.beans.value.ChangeListener;
 import javafx.beans.value.ObservableValue;
 import javafx.collections.FXCollections;
 import javafx.collections.ObservableList;
 import javafx.event.Event;
 import javafx.event.EventHandler;
 import javafx.scene.Node;
 import javafx.scene.control.ListCell;
 import javafx.scene.control.ListView;
 import javafx.scene.control.TextField;
 import javafx.scene.input.KeyCode;
 import javafx.scene.input.KeyEvent;
 import javafx.scene.input.MouseEvent;
 import javafx.stage.Popup;
 import javafx.stage.Window;
 import javafx.util.Callback;
 
 import java.util.Iterator;
 
 public class AutoFillTextBoxSkin<T> extends SkinBase<AutoFillTextBox<T>, AutoFillTextBoxBehavior<T>>
     implements ChangeListener<String>, EventHandler {
 
   private static final int TITLE_HEIGHT = 28;
   private static final int WINDOW_BORDER = 8;
   private ListView listview;
   private TextField textbox;
   private AutoFillTextBox autofillTextbox;
   private ObservableList data;
   private Popup popup;
   private String temporaryTxt = "";
 
   public Window getWindow() {
     return this.autofillTextbox.getScene().getWindow();
   }
 
   public AutoFillTextBoxSkin(AutoFillTextBox text) {
     super(text, new AutoFillTextBoxBehavior(text));
 
     this.autofillTextbox = text;
 
     this.listview = text.getListview();
     if (text.getFilterMode()) {
       this.listview.setItems(text.getData());
     }
     this.listview.itemsProperty().addListener(new ChangeListener() {
       public void changed(ObservableValue ov, Object t, Object t1) {
         if ((AutoFillTextBoxSkin.this.listview.getItems().size() > 0) && (AutoFillTextBoxSkin.this.listview.getItems() != null)) {
           AutoFillTextBoxSkin.this.showPopup();
         } else {
           AutoFillTextBoxSkin.this.hidePopup();
         }
       }
     });
     this.listview.setOnMouseReleased(this);
     this.listview.setOnKeyReleased(this);
 
     this.listview.setCellFactory(new Callback() {
 
       @Override
       public Object call(Object o) {
 
         final ListCell cell = new ListCell() {
           public void updateItem(Object item, boolean empty) {
             super.updateItem(item, empty);
             if (item != null)
               setText(item.toString());
           }
         };
         cell.focusedProperty().addListener(new InvalidationListener() {
           public void invalidated(Observable ove) {
             ObservableValue ov = (ObservableValue) ove;
             if ((cell.getItem() != null) && (cell.isFocused())) {
               String prev = null;
 
               if (AutoFillTextBoxSkin.this.temporaryTxt.length() <= 0) {
                 if (AutoFillTextBoxSkin.this.listview.getItems().size() != AutoFillTextBoxSkin.this.data.size()) {
                   AutoFillTextBoxSkin.this.temporaryTxt = AutoFillTextBoxSkin.this.textbox.getText();
                 }
               }
               prev = AutoFillTextBoxSkin.this.temporaryTxt;
               AutoFillTextBoxSkin.this.textbox.textProperty().removeListener(AutoFillTextBoxSkin.this);
 
               AutoFillTextBoxSkin.this.textbox.textProperty().setValue(cell.getItem().toString());
 
               AutoFillTextBoxSkin.this.textbox.textProperty().addListener(AutoFillTextBoxSkin.this);
               AutoFillTextBoxSkin.this.textbox.selectRange(prev.length(), cell.getItem().toString().length());
             }
           }
         });
         return cell;
       }
     });
     this.textbox = text.getTextbox();
 
     this.textbox.setOnKeyPressed(this);
     this.textbox.textProperty().addListener(this);
 
     this.textbox.focusedProperty().addListener(new ChangeListener() {
       public void changed(ObservableValue ov, Object t, Object t1) {
         AutoFillTextBoxSkin.this.textbox.end();
       }
     });
     this.popup = new Popup();
     this.popup.setAutoHide(true);
     this.popup.getContent().add(this.listview);
 
     this.data = text.getData();
     FXCollections.sort(this.data);
 
     getChildren().addAll(new Node[]{this.textbox});
   }
 
   public void selectList() {
     Object i = this.listview.getSelectionModel().getSelectedItem();
     if (i != null) {
       hidePopup();
       String oldValue = this.textbox.getText();
       this.textbox.setText(this.listview.getSelectionModel().getSelectedItem().toString());
       this.textbox.requestFocus();
       this.textbox.requestLayout();
       this.textbox.end();
       this.temporaryTxt = "";
       String newValue = this.textbox.getText();
       fireSelectionEvent(oldValue, newValue);
     }
   }
 
   private void fireSelectionEvent(Object oldValue, Object newValue) {
     for (int i = 0; i < this.autofillTextbox.getSelectionListeners().size(); i++) {
       ChangeListener listener = (ChangeListener) this.autofillTextbox.getSelectionListeners().get(i);
       listener.changed(null, oldValue, newValue);
     }
   }
 
   public void handle(Event evt) {
     if (evt.getEventType() == KeyEvent.KEY_PRESSED) {
       KeyEvent t = (KeyEvent) evt;
       if (t.getSource() == this.textbox) {
         if (t.getCode() == KeyCode.DOWN) {
           if (this.popup.isShowing()) {
             this.listview.requestFocus();
             this.listview.getSelectionModel().select(0);
           }
         }
       }
     } else if (evt.getEventType() == KeyEvent.KEY_RELEASED) {
       KeyEvent t = (KeyEvent) evt;
       if (t.getSource() == this.listview) {
         if (t.getCode() == KeyCode.ENTER) {
           selectList();
         } else if ((t.getCode() == KeyCode.UP) &&
             (this.listview.getSelectionModel().getSelectedIndex() == 0)) {
           this.textbox.requestFocus();
         }
 
       }
 
     } else if (evt.getEventType() == MouseEvent.MOUSE_RELEASED) {
       if (evt.getSource() == this.listview)
         selectList();
     }
   }
 
   protected double computeMaxHeight(double width) {
     return Math.max(22.0D, this.textbox.getHeight());
   }
 
   public void setPrefSize(double d, double d1) {
     super.setPrefSize(d, d1);
   }
 
   protected double computePrefHeight(double width) {
     return Math.max(22.0D, this.textbox.getPrefHeight());
   }
 
   protected double computeMinHeight(double width) {
     return Math.max(22.0D, this.textbox.getPrefHeight());
   }
 
   protected double computePrefWidth(double height) {
     return Math.max(100.0D, this.textbox.getPrefWidth());
   }
 
   protected double computeMaxWidth(double height) {
     return Math.max(100.0D, this.textbox.getPrefWidth());
   }
 
   protected double computeMinWidth(double height) {
     return Math.max(100.0D, this.textbox.getPrefWidth());
   }
 
   public void showPopupWithAllItems() {
     this.listview.setItems(this.data);
     showPopup();
   }
 
   public void showPopup() {
     if (null == this.listview.getItems() || this.listview.getItems().size() == 0)
       return;
 
     this.listview.setPrefWidth(this.textbox.getWidth());
 
     if (this.listview.getItems().size() > 6)
       this.listview.setPrefHeight(144.0D);
     else {
       this.listview.setPrefHeight(this.listview.getItems().size() * 24);
     }
     this.listview.impl_updatePG();
     this.listview.impl_transformsChanged();
 
     this.popup.show(getWindow(),
        getWindow().getX() + this.textbox.localToScene(0.0D, 0.0D).getX() + this.textbox.getScene().getX() - 1.0D,
        getWindow().getY() + this.textbox.localToScene(0.0D, 0.0D).getY() + this.textbox.getScene().getY() + 20.0D);
 
     this.listview.getSelectionModel().clearSelection();
     this.listview.getFocusModel().focus(-1);
   }
 
   public void hidePopup() {
     this.popup.hide();
   }
 
   public void changed(ObservableValue<? extends String> ov, String t, String t1) {
     if (((String) ov.getValue()).toString().length() > 0) {
       String txtdata = this.textbox.getText().trim();
 
       int limit = 0;
       if (txtdata.length() > 0) {
         ObservableList list = FXCollections.observableArrayList();
         String compare = txtdata.toLowerCase();
         for (Iterator i$ = this.data.iterator(); i$.hasNext(); ) {
           Object dat = i$.next();
           String str = dat.toString().toLowerCase();
 
           if (str.startsWith(compare)) {
             list.add(dat);
             limit++;
           }
           if (limit == this.autofillTextbox.getListLimit())
             break;
         }
         if ((this.listview.getItems().containsAll(list))
             && (this.listview.getItems().size() == list.size())
             && (this.listview.getItems() != null)) {
           showPopup();
         }
         else
           this.listview.setItems(list);
 
       } else if (this.autofillTextbox.getFilterMode()) {
         this.listview.setItems(this.data);
       } else {
         this.listview.setItems(null);
       }
 
     }
 
     if (((String) ov.getValue()).toString().length() <= 0) {
       this.temporaryTxt = "";
       if (this.autofillTextbox.getFilterMode()) {
         this.listview.setItems(this.data);
         showPopup();
       } else {
         hidePopup();
       }
     }
   }
 }
