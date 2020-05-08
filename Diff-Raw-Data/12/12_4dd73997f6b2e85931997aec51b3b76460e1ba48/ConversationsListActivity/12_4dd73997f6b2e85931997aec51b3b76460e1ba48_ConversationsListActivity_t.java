 
 package eu.nerdz.app.messenger.activities;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.accounts.AccountManagerCallback;
 import android.accounts.AccountManagerFuture;
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.annotation.SuppressLint;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v7.app.ActionBarActivity;
 import android.text.Html;
 import android.util.Log;
 import android.util.Pair;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import eu.nerdz.api.ContentException;
 import eu.nerdz.api.HttpException;
 import eu.nerdz.api.Nerdz;
 import eu.nerdz.api.UserInfo;
 import eu.nerdz.api.messages.Conversation;
 import eu.nerdz.api.messages.ConversationHandler;
 import eu.nerdz.api.messages.Message;
 import eu.nerdz.app.Keys;
 import eu.nerdz.app.messenger.Prefs;
 import eu.nerdz.app.messenger.R;
 
 public class ConversationsListActivity extends ActionBarActivity {
 
     private static final String TAG = "NdzConvsListAct";
     LinkedList<Pair<Conversation, MessageContainer>> mConversations;
     private View mConversationsListLayoutView;
     private View mFetchStatusView;
     private View mNoConversationsMsgView;
     private ConversationsListAdapter mConversationsListAdapter;
     private ConversationFetch mConversationFetch;
     private UserInfo mUserInfo;
 
     @SuppressWarnings("unchecked")
     @Override
     public void onCreate(Bundle savedInstanceState) {
 
         Log.d(TAG, "onCreate(" + savedInstanceState + ")");
 
         super.onCreate(savedInstanceState);
 
         this.setContentView(R.layout.layout_conversations_list);
 
         this.mConversations = new LinkedList<Pair<Conversation, MessageContainer>>();
 
         this.mConversationsListLayoutView = this.findViewById(R.id.conversations_list_layout);
         this.mFetchStatusView = this.findViewById(R.id.fetch_status);
         this.mNoConversationsMsgView = this.findViewById(R.id.no_conversations_msg);
 
         this.mConversationsListAdapter = new ConversationsListAdapter(this.mConversations);
 
         ListView conversationsList = (ListView) this.findViewById(R.id.conversations);
         conversationsList.setAdapter(this.mConversationsListAdapter);
         conversationsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
 
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 
                 Log.d(TAG, "onItemClick()");
 
                 Intent intent = new Intent(ConversationsListActivity.this, ConversationActivity.class);
                 intent.putExtra(Keys.NERDZ_INFO, ConversationsListActivity.this.mUserInfo);
                 Conversation conversation = ConversationsListActivity.this.mConversations.get(position).first;
                 conversation.setHasNewMessages(false);
                 ConversationsListActivity.this.mConversationsListAdapter.notifyDataSetChanged();
                 intent.putExtra(Keys.SELECTED_ITEM, conversation);
                 ConversationsListActivity.this.startActivityForResult(intent, Keys.MESSAGE);
             }
         });
 
         if (savedInstanceState == null) {
             if (this.mUserInfo == null) {
                 Intent intent = this.getIntent();
                 Serializable serializable = intent.getSerializableExtra(Keys.NERDZ_INFO);
 
                 if (serializable == null || !(serializable instanceof UserInfo)) {
                     this.shortToast(R.string.error_invalid_login);
                     this.finish();
                 } else {
                     this.mUserInfo = (UserInfo) serializable;
                 }
             }
         } else {
             this.mUserInfo = (UserInfo) savedInstanceState.getSerializable(Keys.NERDZ_INFO);
         }
 
         this.fetchConversations();
 
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
 
         this.getMenuInflater().inflate(R.menu.menu_conversations_list, menu);
 
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 
         switch (item.getItemId()) {
             case R.id.clist_delete_account: {
                 AccountManager am = AccountManager.get(this);
                 Account account = am.getAccountsByType(this.getString(R.string.account_type))[0];
                 am.removeAccount(account, new AccountManagerCallback<Boolean>() {
 
                     @Override
                     public void run(AccountManagerFuture<Boolean> future) {
 
                         while (true) {
                             if (future.isDone()) {
                                 break;
                             }
                         }
 
                         ConversationsListActivity.this.shortToast(ConversationsListActivity.this.getString(R.string.account_deleted));
 
                         Intent intent = new Intent(ConversationsListActivity.this, SplashScreenActivity.class);
                         ConversationsListActivity.this.startActivity(intent);
                         ConversationsListActivity.this.finish();
 
                     }
                 }, null);
                 return true;
             }
             case R.id.clist_refresh_button: {
                 this.fetchConversations();
                 return true;
             }
             case R.id.clist_new_conversation: {
                 Intent intent = new Intent(ConversationsListActivity.this, NewMessageActivity.class);
                 intent.putExtra(Keys.NERDZ_INFO, ConversationsListActivity.this.mUserInfo);
                 ConversationsListActivity.this.startActivityForResult(intent, Keys.MESSAGE);
                 return true;
             }
             default: {
                 return super.onOptionsItemSelected(item);
             }
         }
 
     }
 
     @Override
     public void onRestoreInstanceState(Bundle outState) {
         Log.d(TAG, "onRestoreInstanceState(" + outState + ")");
 
         super.onRestoreInstanceState(outState);
 
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 
         if (requestCode == Keys.MESSAGE) {
 
             if(resultCode == Activity.RESULT_OK){
                 this.updateListWithMessage((Message) data.getSerializableExtra(Keys.OPERATION_RESULT));
             }
 
         }
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
 
         Log.d(TAG, "onSaveInstanceState()");
 
         outState.putSerializable(Keys.NERDZ_INFO, this.mUserInfo);
         super.onSaveInstanceState(outState);
     }
 
     /**
      * Shows the progress UI, or hides it
      */
     @SuppressLint("Override")
     @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
     private void showProgress(final boolean show) {
 
         // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
         // for very easy animations. If available, use these APIs to fade-in
         // the progress spinner.
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
             int shortAnimTime = this.getResources().getInteger(android.R.integer.config_shortAnimTime);
 
             this.mFetchStatusView.setVisibility(View.VISIBLE);
             this.mFetchStatusView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
 
                 @SuppressLint("Override")
                 public void onAnimationEnd(Animator animation) {
 
                     ConversationsListActivity.this.mFetchStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
                 }
             });
 
             if (!show) {
                 final View ourView = (this.mConversations == null ? this.mNoConversationsMsgView : this.mConversationsListLayoutView);
                 ourView.setVisibility(View.VISIBLE);
                 ourView.animate().setDuration(shortAnimTime).alpha(1).setListener(new AnimatorListenerAdapter() {
 
                     @SuppressLint("Override")
                     public void onAnimationEnd(Animator animation) {
 
                         ourView.setVisibility(View.VISIBLE);
                     }
                 });
             } else {
                 final View ourView = (this.mNoConversationsMsgView.getVisibility() == View.VISIBLE ? this.mNoConversationsMsgView : this.mConversationsListLayoutView);
                 ourView.setVisibility(View.VISIBLE);
                 ourView.animate().setDuration(shortAnimTime).alpha(0).setListener(new AnimatorListenerAdapter() {
 
                     @SuppressLint("Override")
                     public void onAnimationEnd(Animator animation) {
 
                         ourView.setVisibility(View.GONE);
                     }
                 });
             }
         } else {
             // The ViewPropertyAnimator APIs are not available, so simply show
             // and hide the relevant UI components.
             this.mFetchStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
             if (!show) {
                 final View ourView = (this.mConversations == null ? this.mNoConversationsMsgView : this.mConversationsListLayoutView);
                 ourView.setVisibility(View.VISIBLE);
             } else {
                 final View ourView = (this.mNoConversationsMsgView.getVisibility() == View.VISIBLE ? this.mNoConversationsMsgView : this.mConversationsListLayoutView);
                 ourView.setVisibility(View.GONE);
             }
         }
     }
 
     private void fetchConversations() {
 
         Log.d(TAG, "fetchConversations()");
 
         this.showProgress(true);
         if (this.mConversationFetch != null && this.mConversationFetch.getStatus() == AsyncTask.Status.RUNNING) {
             this.mConversationFetch.cancel(true);
         }
         (this.mConversationFetch = new ConversationFetch(this.mUserInfo)).execute();
 
     }
 
     private void updateList() {
 
         Log.d(TAG, "updateList()");
 
         ConversationsListActivity.this.showProgress(false);
         this.mConversationsListAdapter.notifyDataSetChanged();
 
     }
 
     private void shortToast(int id) {
 
         this.shortToast(this.getString(id));
     }
 
     private void shortToast(String msg) {
 
         Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
     }
 
     static class ViewHolder {
 
         public TextView userName, message, date;
 
         public ViewHolder(TextView userName, TextView message, TextView date) {
             this.userName = userName;
             this.message = message;
             this.date = date;
         }
     }
 
     private class ConversationFetch extends AsyncTask<Void, Void, Pair<ArrayList<Pair<Conversation, MessageContainer>>, Throwable>> {
 
         private ConversationHandler mHandler;
 
         public ConversationFetch(UserInfo userInfo) {
 
             try {
                 this.mHandler = Nerdz.getImplementation(Prefs.getImplementationName()).restoreMessenger(userInfo).getConversationHandler();
             } catch (Exception e) {
                 ConversationsListActivity.this.shortToast(e.getLocalizedMessage());
                 ConversationsListActivity.this.finish();
             }
         }
 
         @Override
         protected Pair<ArrayList<Pair<Conversation, MessageContainer>>, Throwable> doInBackground(Void... params) {
 
             Log.d(TAG, "doInBackground()");
 
             try {
                 List<Conversation> conversations = this.mHandler.getConversations();
                 if (conversations == null) {
                     return null;
                 }
                 ArrayList<Pair<Conversation, MessageContainer>> newList = new ArrayList<Pair<Conversation, MessageContainer>>(conversations.size());
                 for (Conversation conversation : conversations) {
                     Message sample = this.mHandler.getLastMessage(conversation);
                     newList.add(Pair.create(conversation, new MessageContainer(sample)));
                 }
                 return Pair.create(newList, null);
             } catch (Throwable t) {
                 return Pair.create(null, t);
             }
 
         }
 
         @Override
         protected void onCancelled() {
             ConversationsListActivity.this.mConversations.clear();
             ConversationsListActivity.this.showProgress(false);
         }
 
         @Override
         protected void onPostExecute(Pair<ArrayList<Pair<Conversation, MessageContainer>>, Throwable> result) {
 
             Log.d(TAG, "onPostExecute()");
 
             Throwable t = result.second;
 
             if (t != null) {
                 Log.w(TAG, "received a " + t.getClass().toString() + " throwable");
                 Log.w(TAG, Log.getStackTraceString(t));
 
                 if (t instanceof ContentException) {
                     ConversationsListActivity.this.shortToast("There's something weird in NERDZ Beta. Please, blame Robertof ASAP: " + t.getLocalizedMessage());
                 } else if (t instanceof IOException) {
                     ConversationsListActivity.this.shortToast("Network error: " + t.getLocalizedMessage());
                 } else if (t instanceof HttpException) {
                     ConversationsListActivity.this.shortToast("HTTP Error: " + t.getLocalizedMessage());
                 } else {
                     ConversationsListActivity.this.shortToast("Exception: " + t.getLocalizedMessage());
                 }
                 ConversationsListActivity.this.finish();
                 return;
             }
 
             ConversationsListActivity.this.mConversations.clear();
             ConversationsListActivity.this.mConversations.addAll(result.first);
 
             ConversationsListActivity.this.updateList();
         }
 
     }
 
     static String formatDate(Date date, Context context) {
 
         Calendar now = Calendar.getInstance();
 
         Calendar yesterday = Calendar.getInstance();
         yesterday.add(Calendar.DATE, -1);
 
         Calendar ourDate = Calendar.getInstance();
         ourDate.setTime(date);
 
         String buffer = (yesterday.get(Calendar.YEAR) == ourDate.get(Calendar.YEAR) &&
                          yesterday.get(Calendar.DAY_OF_YEAR) == ourDate.get(Calendar.DAY_OF_YEAR))
                         ? context.getString(R.string.yesterday) + ", "
                         : (now.get(Calendar.YEAR) == ourDate.get(Calendar.YEAR) &&
                            now.get(Calendar.DAY_OF_YEAR) == ourDate.get(Calendar.DAY_OF_YEAR))
                           ? ""
                           : DateFormat.getDateInstance().format(date) + ", ";
 
         buffer += DateFormat.getTimeInstance().format(date);
 
         return buffer;
 
     }
 
     private class ConversationsListAdapter extends ArrayAdapter<Pair<Conversation, MessageContainer>> {
 
         private List<Pair<Conversation, MessageContainer>> mConversations;
         private ActionBarActivity mActivity;
 
         public ConversationsListAdapter(List<Pair<Conversation, MessageContainer>> conversations) {
             super(ConversationsListActivity.this, R.layout.conversation_list_element, conversations);
             this.mConversations = conversations;
             this.mActivity = ConversationsListActivity.this;
         }
 
         @Override
         public View getView(int position, View rowView, ViewGroup parent) {
 
             Pair<Conversation, MessageContainer> element = this.mConversations.get(position);
 
             ViewHolder tag;
 
             if (rowView == null) {
                 LayoutInflater layoutInflater = this.mActivity.getLayoutInflater();
                 rowView = layoutInflater.inflate(R.layout.conversation_list_element, null);
                 tag = new ViewHolder(
                         (TextView) rowView.findViewById(R.id.user_name_field),
                         (TextView) rowView.findViewById(R.id.msg_preview_field),
                         (TextView) rowView.findViewById(R.id.last_date_field)
                 );
                 rowView.setTag(tag);
             } else {
                 tag = (ViewHolder) rowView.getTag();
             }
 
             rowView.setBackgroundColor(element.first.hasNewMessages() ? Color.parseColor("#FFFFED") : Color.WHITE);
 
            String otherName = element.first.getOtherName();
            tag.userName.setText(otherName);
 
             String message = element.second.getContent();
 
             int indexOf = message.indexOf('\n');
 
             message = (indexOf > -1) ? message.substring(0, indexOf) : message;
 
             int senderId = element.second.received()
                            ? element.second.thisConversation().getOtherID()
                            : ConversationsListActivity.this.mUserInfo.getNerdzID();
 
             message = (senderId != element.first.getOtherID()) ? this.mActivity.getString(R.string.you) + ": " + message : message;
 
             tag.message.setText(Html.fromHtml(message));
 
             tag.date.setText(ConversationsListActivity.formatDate(element.first.getLastDate(), this.mActivity) + ' ');
 
             return rowView;
         }
 
 
     }
 
     /**
      * Why I need this? Because Android pairs are immutable, and this is definitely a problem.
     * Not willing to use Apache MutablePairs (with all the problems from mixing the two), this is the best solution.
      */
     static class MessageContainer implements Message {
         private Message mMessage;
 
         public MessageContainer(Message message) {
             this.mMessage = message;
         }
 
         @Override
         public Conversation thisConversation() {
             return this.mMessage.thisConversation();
         }
 
         @Override
         public boolean received() {
             return this.mMessage.received();
         }
 
         @Override
         public boolean read() {
             return this.mMessage.read();
         }
 
         @Override
         public String getContent() {
             return this.mMessage.getContent();
         }
 
         @Override
         public Date getDate() {
             return this.mMessage.getDate();
         }
 
         public Message getInnerMessage() {
             return this.mMessage;
         }
 
         public void setInnerMessage(Message message) {
             this.mMessage = message;
         }
     }
 
     void updateListWithMessage(Message message) {
         for(Pair<Conversation, MessageContainer> element : this.mConversations) {
             if (element.first.getOtherID() == message.thisConversation().getOtherID()) {
                 element.first.updateConversation(message);
                 element.second.setInnerMessage(message);
 
                 this.sortConversationList();
 
                 break;
             }
         }
     }
 
     void sortConversationList() {
         Collections.sort(this.mConversations, new Comparator<Pair<Conversation, MessageContainer>>() {
 
             //newer conversations are lesser than older. Yes, that's weird.
             @Override
             public int compare(Pair<Conversation, MessageContainer> lhs, Pair<Conversation, MessageContainer> rhs) {
                 return -lhs.first.getLastDate().compareTo(rhs.first.getLastDate());
             }
         });
 
         this.mConversationsListAdapter.notifyDataSetChanged();
     }
 
 }
