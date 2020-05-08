 package hu.rgai.android.test;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Parcelable;
 import android.support.v7.app.ActionBarActivity;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Toast;
 import hu.rgai.android.intent.beens.FullSimpleMessageParc;
 import hu.rgai.android.intent.beens.MessageListElementParc;
 import hu.rgai.android.intent.beens.PersonAndr;
 import hu.rgai.android.intent.beens.account.AccountAndr;
 import hu.uszeged.inf.rgai.messagelog.MessageProvider;
 import hu.uszeged.inf.rgai.messagelog.SimpleEmailMessageProvider;
 import hu.uszeged.inf.rgai.messagelog.beans.account.EmailAccount;
 import hu.uszeged.inf.rgai.messagelog.beans.fullmessage.FullEmailMessage;
 import hu.uszeged.inf.rgai.messagelog.beans.account.GmailAccount;
 import hu.uszeged.inf.rgai.messagelog.beans.fullmessage.FullMessage;
 import hu.uszeged.inf.rgai.messagelog.beans.fullmessage.FullSimpleMessage;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.mail.MessagingException;
 import javax.mail.NoSuchProviderException;
 import net.htmlparser.jericho.Source;
 
 public class EmailDisplayer extends ActionBarActivity {
 
   private ProgressDialog pd = null;
   private Handler handler = null;
   private FullSimpleMessageParc content = null;
   private String subject = null;
   private boolean loadedWithContent = false;
   private String emailID = "-1";
   private AccountAndr account;
   private PersonAndr from;
   
   private WebView webView = null;
   private WebViewClient webViewClient = null;
   private String mailCharCode = "UTF-8";
   private Context context = this; 
   
   public static final int MESSAGE_REPLY_REQ_CODE = 1;
   
   @Override
   public void onCreate(Bundle icicle) {
     super.onCreate(icicle);
     
     setContentView(R.layout.email_displayer);
     webView = (WebView) findViewById(R.id.email_content);
     webView.getSettings().setDefaultTextEncodingName(mailCharCode);
     
     MessageListElementParc mlep = (MessageListElementParc)getIntent().getExtras().getParcelable("msg_list_element");
     
     emailID = mlep.getId();
     account = getIntent().getExtras().getParcelable("account");
     subject = mlep.getTitle();
     from = (PersonAndr)mlep.getFrom();
     getSupportActionBar().setTitle(account.getAccountType().toString() + " | " + account.getDisplayName());
     if (mlep.getFullMessage() != null) {
       loadedWithContent = true;
       content = (FullSimpleMessageParc)mlep.getFullMessage();
 //      webView.loadData(content, "text/html", mailCharCode);
 //      webView.loadDataWithBaseURL(null, content, "text/html", mailCharCode, null);
       displayMessage();
     } else {
       handler = new EmailContentTaskHandler();
       EmailContentGetter contentGetter = new EmailContentGetter(this, handler, account);
       contentGetter.execute(emailID);
 
       pd = new ProgressDialog(this);
       pd.setMessage("Fetching email content...");
       pd.setCancelable(false);
       pd.show();
     }
     
     
     webViewClient = new WebViewClient(){
 
         @Override
         public boolean shouldOverrideUrlLoading(WebView view, String url) {
             if(url.startsWith("mailto:")){
                 Intent intent = new Intent(context , MessageReply.class);
                 Source source = new Source(content.getContent());
                 //intent.putExtra("content", source.getRenderer().toString());
                 //intent.putExtra("subject", subject);
                 intent.putExtra("account", (Parcelable)account);
                 intent.putExtra("from", from);
                 startActivityForResult(intent, MESSAGE_REPLY_REQ_CODE);
                 return true;
             }
 
                 return true;
             }
        };
        
        webView.setWebViewClient(webViewClient);      
     
   }
   
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
     MenuInflater inflater = getMenuInflater();
     inflater.inflate(R.menu.message_options_menu, menu);
     return true;
   }
   
   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
     super.onActivityResult(requestCode, resultCode, data);
     switch (requestCode) {
       case (MESSAGE_REPLY_REQ_CODE):
         if (resultCode == MessageReply.MESSAGE_SENT_OK) {
           Toast.makeText(this, "Message sent", Toast.LENGTH_LONG).show();
         } else if (resultCode == MessageReply.MESSAGE_SENT_FAILED) {
           Toast.makeText(this, "Failed to send message ", Toast.LENGTH_LONG).show();
         }
         break;
     }
   }
   
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
     // Handle item selection
     switch (item.getItemId()) {
       case R.id.message_reply:
         Intent intent = new Intent(this, MessageReply.class);
         Source source = new Source(content.getContent());
         intent.putExtra("content", source.getRenderer().toString());
         intent.putExtra("subject", subject);
         intent.putExtra("account", (Parcelable)account);
         intent.putExtra("from", from);
         startActivityForResult(intent, MESSAGE_REPLY_REQ_CODE);
         return true;
 //        EmailReplySender replySender = new EmailReplySender();
 //        replySender.execute();
 //        return true;
       default:
         return super.onOptionsItemSelected(item);
     }
   }
 
   @Override
   public void finish() {
     if (!loadedWithContent) {
       Intent resultIntent = new Intent();
       resultIntent.putExtra("message_data", content);
       resultIntent.putExtra("message_id", emailID);
       
 //      if (account.getAccountType().equals(MessageProvider.Type.EMAIL)) {
         resultIntent.putExtra("account", (Parcelable)account);
 //      } else if (account.getAccountType().equals(MessageProvider.Type.GMAIL)) {
 //        resultIntent.putExtra("account", new GmailAccountParc((GmailAccount)account));
 //      } else if (account.getAccountType().equals(MessageProvider.Type.FACEBOOK)) {
 //        resultIntent.putExtra("account", new FacebookAccountParc((FacebookAccount)account));
 //      }
       setResult(Activity.RESULT_OK, resultIntent);
     }
     super.finish(); //To change body of generated methods, choose Tools | Templates.
   }
   
   private void displayMessage() {
     String mail = from.getId();
     String c = "<b>" +from.getName() +"</b>" + "<br/>" + "<small>" + "<a href=\"mailto:" + mail +"\">"+ mail + "</a>" + "</small>"+ "<br/>" + "<hr>" +"<br/>" + content.getContent();
     webView.loadDataWithBaseURL(null, c.replaceAll("\n", "<br/>"), "text/html", mailCharCode, null);
   }
   
   
   
 
   
   private class EmailContentTaskHandler extends Handler {
     
     @Override
     public void handleMessage(Message msg) {
       Bundle bundle = msg.getData();
       if (bundle != null) {
         if (bundle.get("content") != null) {
           content = bundle.getParcelable("content");
           
           // content holds a simple Person object, but "from" is came from the MainActivity
           // which is already a PersonAndr, so override it with it, so when creating parcelable
           // there will not be an error
           content.setFrom(from);
 
           displayMessage();
           if (pd != null) {
             pd.dismiss();
           }
         }
       }
     }
   }
   
   private class EmailContentGetter extends AsyncTask<String, Integer, FullSimpleMessageParc> {
 
     private Context context;
     Handler handler;
     AccountAndr account;
     
     public EmailContentGetter(Context context, Handler handler, AccountAndr account) {
       this.context = context;
       this.handler = handler;
       this.account = account;
     }
     
     
     
     @Override
     protected FullSimpleMessageParc doInBackground(String... params) {
 //      SharedPreferences sharedPref = getSharedPreferences(getString(R.string.settings_email_file_key), Context.MODE_PRIVATE);
 //      String email = sharedPref.getString(getString(R.string.settings_saved_email), "");
 //      String pass = sharedPref.getString(getString(R.string.settings_saved_pass), "");
 //      String imap = sharedPref.getString(getString(R.string.settings_saved_imap), "");
 //      MailProvider2 em = new MailProvider2(email, pass, imap, Pass.smtp);
       FullSimpleMessageParc fsm = null;
       
       try {
         if (account.getAccountType().equals(MessageProvider.Type.EMAIL)) {
           SimpleEmailMessageProvider semp = new SimpleEmailMessageProvider((EmailAccount)account);
           fsm = new FullSimpleMessageParc((FullSimpleMessage)semp.getMessage(params[0]));
 //          content = fm.getContent();
         } else if (account.getAccountType().equals(MessageProvider.Type.GMAIL)) {
           SimpleEmailMessageProvider semp = new SimpleEmailMessageProvider((GmailAccount)account);
           fsm = new FullSimpleMessageParc((FullSimpleMessage)semp.getMessage(params[0]));
         } else if (account.getAccountType().equals(MessageProvider.Type.FACEBOOK)) {
           // TODO: getting facebook message
         }
       } catch (NoSuchProviderException ex) {
         Logger.getLogger(EmailDisplayer.class.getName()).log(Level.SEVERE, null, ex);
       } catch (MessagingException ex) {
         Logger.getLogger(EmailDisplayer.class.getName()).log(Level.SEVERE, null, ex);
       } catch (IOException ex) {
         Logger.getLogger(EmailDisplayer.class.getName()).log(Level.SEVERE, null, ex);
       }
 //      try {
 //        content = em.getMailContent2(params[0]);
 //      } catch (IOException ex) {
 //        Logger.getLogger(MyService.class.getName()).log(Level.SEVERE, null, ex);
 //      } catch (MessagingException ex) {
 //        Logger.getLogger(EmailDisplayer.class.getName()).log(Level.SEVERE, null, ex);
 //      }
 //
       return fsm;
 //      return "";
     }
 
     @Override
     protected void onPostExecute(FullSimpleMessageParc result) {
       Message msg = handler.obtainMessage();
       Bundle bundle = new Bundle();
       bundle.putParcelable("content", result);
       msg.setData(bundle);
       handler.sendMessage(msg);
     }
 
 
 //    @Override
 //    protected void onProgressUpdate(Integer... values) {
 //      Log.d(Constants.LOG, "onProgressUpdate");
 //      Message msg = handler.obtainMessage();
 //      Bundle bundle = new Bundle();
 //
 //      bundle.putInt("progress", values[0]);
 //      msg.setData(bundle);
 //      handler.sendMessage(msg);
 //    }
   }
 
 }
