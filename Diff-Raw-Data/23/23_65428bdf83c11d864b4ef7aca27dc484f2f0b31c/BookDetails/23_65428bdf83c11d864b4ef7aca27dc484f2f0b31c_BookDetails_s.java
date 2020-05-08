 package org.abstractbinary.booktrader;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 public class BookDetails extends Activity {
     /* Debugging */
     static final String TAG = "BookTrader";
 
     /* Constants */
     static final int DIALOG_LOADING = 0;
 
     /* Commonly used widgets */
     TextView bookTitleLabel;
     ProgressDialog loadingDialog;
 
     /* Internal gubbins */
     String bookIdentifier;
     Book book;
     Handler detailsHandler;
     BookTraderAPI api;
 
     /* Activity life-cycle */
 
     /** Called when the activity is first created.  Mhm.  I am not
      * suprised at all by this, hence the informational content of
      * this comment up to this sentence is null.*/
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.book_details);
 
         bookTitleLabel = (TextView)findViewById(R.id.book_title_label);
 
         bookIdentifier = getIntent().getData().getPathSegments().get(0);
 
         detailsHandler = new Handler() {
                 @Override
                 public void handleMessage(Message msg) {
                     switch (msg.what) {
                     case BookTraderAPI.DETAILS_START:
                         showDialog(DIALOG_LOADING);
                         break;
                     case BookTraderAPI.DETAILS_GOT:
                         handleDetailsGot((Book)msg.obj);
                         break;
                     case BookTraderAPI.DETAILS_ERROR:
                         if (loadingDialog != null)
                             loadingDialog.dismiss();
                         Toast.makeText(BookDetails.this, "double trouble",
                                        Toast.LENGTH_SHORT).show();
                         Log.v(TAG, "Failed to get book details: " +
                               (Exception)msg.obj);
                         break;
                     case DownloadCache.DOWNLOAD_DONE:
                         handleCoverDownloadDone
                             ((Drawable)
                              ((DownloadCache.DownloadResult)msg.obj).result);
                         break;
                     case DownloadCache.DOWNLOAD_ERROR:
                         Log.v(TAG, "Failed to get cover: " +
                               (Exception)
                               ((DownloadCache.DownloadResult)msg.obj).result);
                         break;
                     case BookTraderAPI.DETAILS_HAVE:
                         markHad();
                         break;
                     case BookTraderAPI.DETAILS_WANT:
                         markWanted();
                         break;
                     default:
                         throw new RuntimeException("unknown message type: " +
                                                    msg.what);
                     }
                 }
             };
 
         api = BookTraderAPI.getInstance();
         api.doGetBookDetails(bookIdentifier, detailsHandler);
 
         bookTitleLabel.setText(bookIdentifier);
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         final Dialog dialog;
         switch (id) {
         case DIALOG_LOADING:
             loadingDialog = new ProgressDialog(this);
             dialog = loadingDialog;
             loadingDialog.setMessage(getResources().getText(R.string.loading));
             break;
         default:
             throw new RuntimeException("unknown dialog type + " + id);
         }
         return dialog;
     }
 
 
     /* Event handlers */
 
     /** Called when the book details have been downloaded */
     void handleDetailsGot(Book book) {
         if (loadingDialog != null)
             loadingDialog.dismiss();
 
         this.book = book;
         book.getCover(detailsHandler);
 
         bookTitleLabel.setText(book.title);
         ((TextView)findViewById(R.id.book_subtitle_label)).setText
             (book.subtitle);
         StringBuilder authors = new StringBuilder();
         for (int i = 0; i < book.authors.size(); i++) {
             authors.append(book.authors.get(i));
             if (i < book.authors.size() - 2)
                 authors.append(", ");
             if (i == book.authors.size() - 2)
                 authors.append(" and ");
         }
         ((TextView)findViewById(R.id.book_authors_label)).setText
             (authors.toString());
         if (api.loggedIn && book.owners.contains(api.currentUser))
             markHad();
         if (api.loggedIn && book.coveters.contains(api.currentUser))
             markWanted();
     }
 
 
     /* Callbacks */
 
     /** Called when the cover image has finished downloading */
     void handleCoverDownloadDone(Drawable image) {
         if (image != null)
             ((ImageView)findViewById
              (R.id.cover_view)).setImageDrawable(image);
     }
 
     /** Called when the user the user clicks a have button */
     public void have(View v) {
         api.doHave(bookIdentifier, detailsHandler);
     }
 
     /** Called when the user the user clicks a have button */
     public void want(View v) {
         api.doWant(bookIdentifier, detailsHandler);
     }
 
 
     /* Utilities */
 
     void markWanted() {
         Button wantButton = (Button)findViewById(R.id.want_button);
         wantButton.setText(getResources().getString(R.string.already_want));
         wantButton.setEnabled(false);
     }
 
     void markHad() {
         Button haveButton = (Button)findViewById(R.id.have_button);
         haveButton.setText(getResources().getString(R.string.already_have));
         haveButton.setEnabled(false);
         findViewById(R.id.want_button).setVisibility(View.INVISIBLE);
     }
 }
