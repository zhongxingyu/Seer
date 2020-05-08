 package mobi.bbhn.brightberry;
 
 /*
 Copyright (c) 2009, Chris Hallgren & Hallgren Networks
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modification,
 are permitted provided that the following conditions are met:
 
 	* Redistributions of source code must retain the above copyright notice,
 	  this list of conditions and the following disclaimer.
 	* Redistributions in binary form must reproduce the above copyright notice,
 	  this list of conditions and the following disclaimer in the documentation
 	  and/or other materials provided with the distribution.
 	* Neither the name of Chris Hallgren or Hallgren Networks nor the names of
 	  its contributors may be used to endorse or promote products derived from
 	  this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 OF SUCH DAMAGE.
 */
 
 import net.rim.device.api.system.Bitmap;
 import net.rim.device.api.ui.ContextMenu;
 import net.rim.device.api.ui.DrawStyle;
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.Font;
 import net.rim.device.api.ui.MenuItem;
 import net.rim.device.api.ui.UiApplication;
 import net.rim.device.api.ui.component.BitmapField;
 import net.rim.device.api.ui.component.LabelField;
 import net.rim.device.api.ui.component.ListField;
 import net.rim.device.api.ui.component.RichTextField;
 import net.rim.device.api.ui.component.SeparatorField;
 import net.rim.device.api.ui.container.MainScreen;
 
 
 public class BkObjectScreen extends MainScreen {
 	Settings settings = Settings.getInstance();
     RichTextField bodyField = new RichTextField(this.body);
     RichTextField creatorField = new RichTextField(this.body);
     RichTextField locationField = new RichTextField(this.location);
     RichTextField createdField = new RichTextField(this.created_at_as_words);
     Bitmap loading = Bitmap.getBitmapResource("BKIcon.jpg");
     BitmapField photoField = new BitmapField(loading, BitmapField.FIELD_HCENTER|BitmapField.FOCUSABLE|BitmapField.EDITABLE|HIGHLIGHT_FOCUS);
     BkObjectScreen screen = this;
     MenuItem commentItem;
     LabelField title = new LabelField("BrightBerry Details", Field.FIELD_HCENTER);
     MenuItem refreshItem;
     ListField commentlist = new CommentListField();
     Font bold = Font.getDefault().derive(Font.BOLD);
     private String body;
     private String creator;
     private String location;
     private String created_at_as_words;
     private boolean about;
     private String photo;
     private String objectID;
     private Comments[] commentsStream;
     private int commentscount;
     private MenuItem viewDetailsItem;
     private MenuItem commentsDMItem;
 	private MenuItem ViewStreamItem;
 	private String type;
     protected boolean onSavePrompt() {
         return true;
     }
 
     
     public BkObjectScreen(String objectID) {
         Thread updateThread = new BkObjectThread(this.screen, objectID);
         updateThread.start();
         this.objectID = objectID;
         
         super.setTitle(title);
         this.creatorField.setFont(this.bold);
         
         this.commentItem = new MenuItem("Post Comment", 5, 10) {
             public void run() {
             	UiApplication.getUiApplication().pushScreen(new PostCommentScreen(BkObjectScreen.this.objectID));
             }
         };
         this.refreshItem = new MenuItem("Refresh", 6, 10) {
             public void run() {
                 refresh();
             }
         };
         
         this.viewDetailsItem = new MenuItem("View Comment Details", 1, 10) {
             public void run() {
                 String body = commentsStream[commentlist.getSelectedIndex()].getBody();
                 String name = commentsStream[commentlist.getSelectedIndex()].getName();
                 String created = commentsStream[commentlist.getSelectedIndex()].getCreated();
                 UiApplication.getUiApplication().pushScreen(new CommentScreen(BkObjectScreen.this.objectID, name, body, created));
             }
         };
         
         
         this.commentsDMItem = new MenuItem("Send Direct Message", 2, 10) {
             public void run() {
                 String name = commentsStream[commentlist.getSelectedIndex()].getName();
                 UiApplication.getUiApplication().pushScreen(new SendDirectMessageScreen(name));
             }
         };
         
         this.ViewStreamItem = new MenuItem("View User Stream", 3, 10) {
         	public void run() {
         		String creator = commentsStream[commentlist.getSelectedIndex()].getName();
     			UiApplication.getUiApplication().pushScreen(new StreamScreen(true, "person", 0, creator));
         	}
         };
         
         addMenuItem(this.commentItem);
         addMenuItem(MenuItem.separator(4));
         addMenuItem(this.refreshItem);
     }
     
     public void refresh() {
             UiApplication.getUiApplication().popScreen(this);
             UiApplication.getUiApplication().pushScreen(new BkObjectScreen(objectID));
     }
     
     public void updateObject(String type, String body, String creator, String location, String created_at_as_words, boolean about, String photo, int commentscount) {
         this.type = type;
     	if (body.length() > 0) {
                 this.body = body;
         } else if (type.equals("checkin")) {
                 this.body = "Checked in";
         } else {
                 this.body = "";
         }
         this.created_at_as_words = created_at_as_words;
         this.creator = creator;
         this.location = location;
         this.about = about;
         this.photo = photo;
         this.commentscount = commentscount;
         UiApplication.getUiApplication().invokeLater(new Runnable() {
             public void run() {
         		if (BkObjectScreen.this.commentscount > 0) {
                     Thread comments = new CommentThread(BkObjectScreen.this.screen, BkObjectScreen.this.objectID);
                     comments.start();
                 }
                 BkObjectScreen.this.creatorField.setText(BkObjectScreen.this.creator);
                 if (BkObjectScreen.this.about) {
                 	BkObjectScreen.this.locationField.setText("about " + BkObjectScreen.this.location);
                 } else {
                     if (BkObjectScreen.this.location.startsWith("near")) {
                         BkObjectScreen.this.locationField.setText(BkObjectScreen.this.location);
                     } else {
                         BkObjectScreen.this.locationField.setText("@" + BkObjectScreen.this.location);
                     }
                 }
                 BkObjectScreen.this.createdField.setText(BkObjectScreen.this.created_at_as_words + " ago");
                 BkObjectScreen.this.add(creatorField);
                if (BkObjectScreen.this.type.equals("Photo")){
                     Bitmap bmPhoto = HTTPPhoto.getPhoto(BkObjectScreen.this.photo);
                     if (bmPhoto != null) {
                         BkObjectScreen.this.photoField.setBitmap(bmPhoto);
                         BkObjectScreen.this.add(photoField);
                     }
                 }
                 if (BkObjectScreen.this.body.length() > 0 && BkObjectScreen.this.body.equals("null") == false) {
                     BkObjectScreen.this.bodyField.setText(BkObjectScreen.this.body);
                     BkObjectScreen.this.add(bodyField);
                 }
                 BkObjectScreen.this.add(new SeparatorField());
                 BkObjectScreen.this.add(locationField);
                 BkObjectScreen.this.add(new SeparatorField());
                 BkObjectScreen.this.add(createdField);
             }
         });
     }
     
     public void updateComments(final Comments[] commentsStream) {
         this.commentsStream = commentsStream;
         UiApplication.getUiApplication().invokeLater(new Runnable() { 
             public void run() {
                 BkObjectScreen.this.commentlist.setEmptyString("Nothing to see here", DrawStyle.LEFT);
                 BkObjectScreen.this.commentlist.setSize(BkObjectScreen.this.commentsStream.length);
                 BkObjectScreen.this.commentlist.setCallback(new CommentsCallback(BkObjectScreen.this.commentsStream));
                 BkObjectScreen.this.commentlist.setRowHeight(ListField.ROW_HEIGHT_FONT*3);
                 add(new SeparatorField());
                 LabelField commentLabel = new LabelField("Comments (" + commentscount + ")", LabelField.FIELD_HCENTER|LabelField.NON_FOCUSABLE);
                 commentLabel.setFont(Font.getDefault().derive(Font.BOLD, Font.getDefault().getHeight()+5));
                 add(commentLabel);
                 add(new SeparatorField());
                 add(BkObjectScreen.this.commentlist);
             }
         });
     }
     
 	public class CommentListField extends ListField {
 		protected void makeContextMenu(ContextMenu contextMenu) {
 			contextMenu.addItem(viewDetailsItem);
 			contextMenu.addItem(commentsDMItem);
 			contextMenu.addItem(ViewStreamItem);
 		}
 	}
 }
