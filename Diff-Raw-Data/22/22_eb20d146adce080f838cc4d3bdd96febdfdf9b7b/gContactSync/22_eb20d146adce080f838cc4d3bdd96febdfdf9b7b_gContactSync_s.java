 /*
  * Copyright (c) 2010 Google Inc., 2011 Josh Geenen
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package org.pirules.gcontactsync.android;
 
 import com.google.api.client.googleapis.GoogleHeaders;
 import com.google.api.client.http.HttpResponse;
 import com.google.api.client.http.HttpResponseException;
 import com.google.api.client.http.HttpTransport;
 import com.google.api.client.http.apache.ApacheHttpTransport;
 import com.google.api.client.http.javanet.NetHttpTransport;
 import org.pirules.gcontactsync.android.model.contact.ContactEntry;
 import org.pirules.gcontactsync.android.model.contact.ContactFeed;
 import org.pirules.gcontactsync.android.model.contact.ContactUrl;
 import org.pirules.gcontactsync.android.model.group.GroupEntry;
 import org.pirules.gcontactsync.android.model.group.GroupFeed;
 import org.pirules.gcontactsync.android.model.group.GroupUrl;
import org.pirules.gcontactsync.android.util.RedirectHandler;
 import org.pirules.gcontactsync.android.util.Util;
 
 import com.google.api.client.util.DateTime;
import com.google.api.client.xml.atom.AtomParser;
 import com.google.common.collect.Lists;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Build;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.ArrayAdapter;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * gContactSync for Android
  * <p>
  * To enable logging of HTTP requests/responses, run this command: {@code adb shell setprop
  * log.tag.HttpTransport DEBUG}. Then press-and-hold a contact, and enable "Logging".
  * </p>
  * 
  * Originally based on Google's Calendar v2 Android Sample project by Yaniv Inbar
  *
  * @author Yaniv Inbar, Josh Geenen
  */
 public final class gContactSync extends ListActivity {
 
   private static final String AUTH_TOKEN_TYPE = "cp";
 
   private static final String TAG = "gContactSync-Android";
   
   private static final int VERSION_MAJOR = 0;
   private static final int VERSION_MINOR = 0;
   private static final int VERSION_RELEASE = 1;
 
   private static final boolean LOGGING_DEFAULT = false;
 
   private static final int MENU_ADD = 0;
 
   private static final int MENU_ACCOUNTS = 1;
 
   private static final int CONTEXT_EDIT = 0;
 
   private static final int CONTEXT_DELETE = 1;
 
   private static final int CONTEXT_LOGGING = 2;
   
   private static final int CONTEXT_SELECT_GROUP = 3;
 
   private static final int REQUEST_AUTHENTICATE = 0;
 
   private static final String PREF = "MyPrefs";
 
   private static final int DIALOG_ACCOUNTS = 0;
 
   private static HttpTransport transport;
 
   private String authToken;
 
   private final List<ContactEntry> contacts = Lists.newArrayList();
   private final List<GroupEntry> groups = Lists.newArrayList();
   
   private GroupEntry currentGroup = null;
 
   /** SDK 2.2 ("FroYo") version build number. */
   private static final int FROYO = 8;
 
   public gContactSync() {
     if (Build.VERSION.SDK_INT <= FROYO) {
       transport = new ApacheHttpTransport();
     } else {
       transport = new NetHttpTransport();
     }
     GoogleHeaders headers = new GoogleHeaders();
     headers.setApplicationName(TAG + "/" + VERSION_MAJOR + "." + VERSION_MINOR);
     headers.gdataVersion = "3";
     transport.defaultHeaders = headers;
     AtomParser parser = new AtomParser();
     parser.namespaceDictionary = Util.DICTIONARY;
     transport.addParser(parser);
   }
 
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     SharedPreferences settings = getSharedPreferences(PREF, 0);
     setLogging(settings.getBoolean("logging", LOGGING_DEFAULT));
     getListView().setTextFilterEnabled(true);
     registerForContextMenu(getListView());
     gotAccount(false);
   }
 
   @Override
   protected Dialog onCreateDialog(int id) {
     switch (id) {
       case DIALOG_ACCOUNTS:
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle("Select a Google account");
         final AccountManager manager = AccountManager.get(this);
         final Account[] accounts = manager.getAccountsByType("com.google");
         final int size = accounts.length;
         String[] names = new String[size];
         for (int i = 0; i < size; i++) {
           names[i] = accounts[i].name;
         }
         builder.setItems(names, new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int which) {
             gotAccount(manager, accounts[which]);
           }
         });
         return builder.create();
     }
     return null;
   }
 
   private void gotAccount(boolean tokenExpired) {
     SharedPreferences settings = getSharedPreferences(PREF, 0);
     String accountName = settings.getString("accountName", null);
     if (accountName != null) {
       AccountManager manager = AccountManager.get(this);
       Account[] accounts = manager.getAccountsByType("com.google");
       int size = accounts.length;
       for (int i = 0; i < size; i++) {
         Account account = accounts[i];
         if (accountName.equals(account.name)) {
           if (tokenExpired) {
             manager.invalidateAuthToken("com.google", this.authToken);
           }
           gotAccount(manager, account);
           return;
         }
       }
     }
     showDialog(DIALOG_ACCOUNTS);
   }
 
   void gotAccount(final AccountManager manager, final Account account) {
     SharedPreferences settings = getSharedPreferences(PREF, 0);
     SharedPreferences.Editor editor = settings.edit();
     editor.putString("accountName", account.name);
     editor.commit();
     new Thread() {
 
       @Override
       public void run() {
         try {
           final Bundle bundle =
               manager.getAuthToken(account, AUTH_TOKEN_TYPE, true, null, null).getResult();
           runOnUiThread(new Runnable() {
 
             public void run() {
               try {
                 if (bundle.containsKey(AccountManager.KEY_INTENT)) {
                   Intent intent = bundle.getParcelable(AccountManager.KEY_INTENT);
                   int flags = intent.getFlags();
                   flags &= ~Intent.FLAG_ACTIVITY_NEW_TASK;
                   intent.setFlags(flags);
                   startActivityForResult(intent, REQUEST_AUTHENTICATE);
                 } else if (bundle.containsKey(AccountManager.KEY_AUTHTOKEN)) {
                   authenticated(bundle.getString(AccountManager.KEY_AUTHTOKEN));
                 }
               } catch (Exception e) {
                 handleException(e);
               }
             }
           });
         } catch (Exception e) {
           handleException(e);
         }
       }
     }.start();
   }
 
   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     super.onActivityResult(requestCode, resultCode, data);
     switch (requestCode) {
       case REQUEST_AUTHENTICATE:
         if (resultCode == RESULT_OK) {
           gotAccount(false);
         } else {
           showDialog(DIALOG_ACCOUNTS);
         }
         break;
     }
   }
 
   void authenticated(String authToken) {
     this.authToken = authToken;
     ((GoogleHeaders) transport.defaultHeaders).setGoogleLogin(authToken);
     //RedirectHandler.resetSessionId(transport);
     executeRefreshContacts();
   }
 
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
     if (currentGroup != null) {
       menu.add(0, MENU_ADD, 0, "New contact");
     }
     menu.add(0, MENU_ACCOUNTS, 0, "Switch Account");
     return true;
   }
 
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
     switch (item.getItemId()) {
       case MENU_ADD:
         ContactUrl url = ContactUrl.forAllContactsFeed();
         ContactEntry contact = new ContactEntry();
         contact.title = "Contact " + new DateTime(new Date());
         try {
           contact.executeInsert(transport, url);
         } catch (IOException e) {
           handleException(e);
         }
         executeRefreshContacts();
         return true;
       case MENU_ACCOUNTS:
         currentGroup = null;
         showDialog(DIALOG_ACCOUNTS);
         return true;
     }
     return false;
   }
 
   @Override
   public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
     if (currentGroup == null) {
       AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
       currentGroup = groups.get((int) info.id);
       executeRefreshContacts();
       return;
     }
     // TODO cleanup
     super.onCreateContextMenu(menu, v, menuInfo);
     //menu.add(0, CONTEXT_EDIT, 0, "Update Title");
     if (currentGroup == null) {
       menu.add(0, CONTEXT_SELECT_GROUP, 0, "Select Group");
     } else {
       // Only allow deletion of contacts for now
       menu.add(0, CONTEXT_DELETE, 0, "Delete");
     }
     SharedPreferences settings = getSharedPreferences(PREF, 0);
     boolean logging = settings.getBoolean("logging", false);
     menu.add(0, CONTEXT_LOGGING, 0, "Logging").setCheckable(true).setChecked(logging);
   }
   
   /*
   @Override
   public boolean onMenuItemSelected(int featureId, MenuItem item) {
     if (currentGroup == null) {
       currentGroup = groups.get(featureId);
       executeRefreshContacts();
       return true;
     }
     return super.onMenuItemSelected(featureId, item);
   }
   */
 
   @Override
   public boolean onContextItemSelected(MenuItem item) {
     AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
     
     // For now if there's no current group then select the group
     if (currentGroup == null) {
       currentGroup = groups.get((int) info.id);
       executeRefreshContacts();
       return true;
     }
     
     ContactEntry contact = contacts.get((int) info.id);
     try {
       switch (item.getItemId()) {
         case CONTEXT_SELECT_GROUP:
           currentGroup = groups.get((int) info.id);
           executeRefreshContacts();
           return true;
         case CONTEXT_EDIT:
           ContactEntry patchedContact = contact.clone();
           patchedContact.title = contact.title + " UPDATED " + new DateTime(new Date());
           patchedContact.executePatchRelativeToOriginal(transport, contact);
           executeRefreshContacts();
           return true;
         case CONTEXT_DELETE:
           contact.executeDelete(transport);
           executeRefreshContacts();
           return true;
         case CONTEXT_LOGGING:
           SharedPreferences settings = getSharedPreferences(PREF, 0);
           boolean logging = settings.getBoolean("logging", LOGGING_DEFAULT);
           setLogging(!logging);
           return true;
         default:
           return super.onContextItemSelected(item);
       }
     } catch (IOException e) {
       handleException(e);
     }
     return false;
   }
   
   // When back is pressed go back to the list of groups, if already there then
   // fallback on the superclass' default action
   @Override
   public void onBackPressed () {
     if (currentGroup != null) {
       currentGroup = null;
       executeRefreshContacts();
     }
     else {
       super.onBackPressed();
     }
   }
 
   private void executeRefreshContacts() {
     String[] names;
     contacts.clear();
     groups.clear();
     try {
       if (currentGroup == null) {
         GroupUrl groupUrl = GroupUrl.forAllGroupsFeed();
         GroupFeed groupFeed = GroupFeed.executeGet(transport, groupUrl);
         GroupEntry allContacts = new GroupEntry();
         allContacts.id = null;
         allContacts.title = "All Contacts";
         groups.add(allContacts);
         if (groupFeed.groups != null) {
           groups.addAll(groupFeed.groups);
         }
         
       }
       else {
         ContactUrl contactUrl = ContactUrl.forAllContactsFeed();
         contactUrl.group = currentGroup.getContactFeedLink();
         //new ContactUrl(currentGroup.getContactFeedLink());
         ContactFeed contactFeed = ContactFeed.executeGet(transport, contactUrl);
         if (contactFeed.contacts != null) {
           contacts.addAll(contactFeed.contacts);
         }
       }
       int numGroups = groups.size();
       int numContacts = contacts.size();
       names = new String[numContacts + numGroups];
       for (int i = 0; i < numGroups; i++) {
         names[i] = groups.get(i).title;
         names[i] = names[i].replaceFirst("System Group: ", "");
       }
       for (int i = 0; i < numContacts; i++) {
         names[i + numGroups] = contacts.get(i).title;
       }
     } catch (IOException e) {
       handleException(e);
       names = new String[] {e.getMessage()};
       contacts.clear();
     }
     setListAdapter(
         new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names));
   }
 
   private void setLogging(boolean logging) {
     Logger.getLogger("com.google.api.client").setLevel(logging ? Level.CONFIG : Level.OFF);
     SharedPreferences settings = getSharedPreferences(PREF, 0);
     boolean currentSetting = settings.getBoolean("logging", false);
     if (currentSetting != logging) {
       SharedPreferences.Editor editor = settings.edit();
       editor.putBoolean("logging", logging);
       editor.commit();
     }
   }
 
   void handleException(Exception e) {
     e.printStackTrace();
     SharedPreferences settings = getSharedPreferences(PREF, 0);
     boolean log = settings.getBoolean("logging", false);
     if (e instanceof HttpResponseException) {
       HttpResponse response = ((HttpResponseException) e).response;
       int statusCode = response.statusCode;
       try {
         response.ignore();
       } catch (IOException e1) {
         e1.printStackTrace();
       }
       if (statusCode == 401 || statusCode == 403) {
         gotAccount(true);
         return;
       }
       if (log) {
         try {
           Log.e(TAG, response.parseAsString());
         } catch (IOException parseException) {
           parseException.printStackTrace();
         }
       }
     }
     if (log) {
       Log.e(TAG, e.getMessage(), e);
     }
   }
 }
