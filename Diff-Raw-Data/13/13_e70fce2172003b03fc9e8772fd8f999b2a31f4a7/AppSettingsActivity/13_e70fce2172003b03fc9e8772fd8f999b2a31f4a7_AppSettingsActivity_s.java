 /* OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2010, OpenRemote Inc.
 *
 * See the contributors.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
 package org.openremote.android.console;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.http.HttpResponse;
 import org.openremote.android.console.model.AppSettingsModel;
 import org.openremote.android.console.model.ControllerException;
 import org.openremote.android.console.model.ViewHelper;
 import org.openremote.android.console.net.IPAutoDiscoveryServer;
 import org.openremote.android.console.net.ORConnection;
 import org.openremote.android.console.net.ORConnectionDelegate;
 import org.openremote.android.console.net.ORHttpMethod;
 import org.openremote.android.console.ssl.ORPKCS10CertificationRequest;
 import org.openremote.android.console.ssl.ORKeyPair;
 import org.openremote.android.console.ssl.ORKeyStore;
 import org.openremote.android.console.util.FileUtil;
 import org.openremote.android.console.util.StringUtil;
 import org.openremote.android.console.view.PanelSelectSpinnerView;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.Context;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.WindowManager;
 import android.view.inputmethod.InputMethodManager;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 
 /**
  * Application global settings view.
  * It is for configuring controller server, panel identity and security.
  * It also can delete image caches.
  * 
  * @author Tomsky Wang
  * @author Dan Cong
  *
  */
 
 public class AppSettingsActivity extends GenericActivity implements ORConnectionDelegate {
 
    /** The app settings view contains auto discovery, auto servers, custom servers,
     * select panel identity, clear image cache and security configuration. 
    */
    private LinearLayout appSettingsView;
    
    /** The custom list view contains custom server list. */
    private ListView customListView;
    private int currentCustomServerIndex = -1;
    
    /** The auto mode is to indicate if auto discovery servers. */
    private boolean autoMode;
    public static String currentServer = "";
    
    /** The view for selecting panel identity. */
    private PanelSelectSpinnerView panelSelectSpinnerView;
    
    /** The progress layout display auto discovery progress. */
    private LinearLayout progressLayout;
    
    private ProgressDialog loadingPanelProgress;
       
    @Override
    public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
       setTitle(R.string.settings);
       
       this.autoMode = AppSettingsModel.isAutoMode(AppSettingsActivity.this);
       
       // The main layout contains all application configuration items.
       LinearLayout mainLayout = new LinearLayout(this);
       mainLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
       mainLayout.setOrientation(LinearLayout.VERTICAL);
       mainLayout.setBackgroundColor(0);
       mainLayout.setTag(R.string.settings);
       
       loadingPanelProgress = new ProgressDialog(this);
       
       // The scroll view contains appSettingsView, and make the appSettingsView can be scrolled.
       ScrollView scroll = new ScrollView(this);
       scroll.setVerticalScrollBarEnabled(true);
       scroll.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1));
       appSettingsView = new LinearLayout(this);
       appSettingsView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
       appSettingsView.setOrientation(LinearLayout.VERTICAL);
       
       appSettingsView.addView(createAutoLayout());
       appSettingsView.addView(createChooseControllerLabel());
       
       currentServer = "";
       if (autoMode) {
          appSettingsView.addView(constructAutoServersView());
       } else {
          appSettingsView.addView(constructCustomServersView());
       }
       appSettingsView.addView(createChoosePanelLabel());
       panelSelectSpinnerView = new PanelSelectSpinnerView(this);
       appSettingsView.addView(panelSelectSpinnerView);
       
       appSettingsView.addView(createCacheText());
       appSettingsView.addView(createClearImageCacheButton());
       appSettingsView.addView(createSSLLayout());
       scroll.addView(appSettingsView);
       
       mainLayout.addView(scroll);
       mainLayout.addView(createDoneAndCancelLayout());
       
       setContentView(mainLayout);
       initSSLState();
       addOnclickListenerOnDoneButton();
       addOnclickListenerOnCancelButton();
       progressLayout = (LinearLayout)findViewById(R.id.choose_controller_progress);
 
    }
 
    @Override
 	protected void onResume() {
 	   super.onResume();
 
 	   if(getIntent().getBooleanExtra("SSL_CLIENT", false)) {
 		   Toast.makeText(this, "You don't have access yet", Toast.LENGTH_LONG).show();
 		   ViewHelper.showAlertViewWithTitle(this, "No Access", "The administrator didn't approve you yet so you don't have access.");
 	   }
    }
    
    /**
     * Creates the image cache text view.
     * 
     * @return the text view
     */
    private TextView createCacheText() {
       TextView cacheText = new TextView(this);
       cacheText.setPadding(10, 5, 0, 5);
       cacheText.setText("Image Cache:");
       cacheText.setBackgroundColor(Color.DKGRAY);
       return cacheText;
    }
    
    /**
     * Creates the choose panel identity text.
     * 
     * @return the text view
     */
    private TextView createChoosePanelLabel() {
       TextView choosePanelInfo = new TextView(this);
       choosePanelInfo.setPadding(10, 5, 0, 5);
       choosePanelInfo.setText("Choose Panel Identity:");
       choosePanelInfo.setBackgroundColor(Color.DKGRAY);
       return choosePanelInfo;
    }
 
    /**
     * Creates the choose controller bar, which contains "Choose controller:" text and 
     * a progress bar(used in auto discovery servers).
     * 
     * @return the linear layout
     */
    private LinearLayout createChooseControllerLabel() {
       LayoutInflater inflater = (AppSettingsActivity.this).getLayoutInflater();
       LinearLayout chooseController = (LinearLayout)inflater.inflate(R.layout.choose_controller_bar, null);
       return chooseController;
    }
 
    /**
     * Creates the done and cancel layout that is inflated from xml.
     * 
     * @return the linear layout
     */
    private LinearLayout createDoneAndCancelLayout() {
       LayoutInflater inflater = (AppSettingsActivity.this).getLayoutInflater();
       LinearLayout saveAndCancelLayout = (LinearLayout)inflater.inflate(R.layout.bottom_button_bar, null);
       return saveAndCancelLayout;
    }
    
    /**
     * Creates the ssl layout.
     * It contains ssl switch and ssl port.
     * 
     * @return the linear layout
     */
    private LinearLayout createSSLLayout() {
       LayoutInflater inflater = (AppSettingsActivity.this).getLayoutInflater();
       LinearLayout sslLayout = (LinearLayout)inflater.inflate(R.layout.ssl_field_view, null);
       
       return sslLayout;
    }
 
   /**
    * Initializes the SSL related UI widget properties and event handlers to deal with user
    * interactions.
    */
   private void initSSLState()
   {
     // Get UI Widget references...
 
     final ToggleButton sslToggleButton = (ToggleButton)findViewById(R.id.ssl_toggle);
     final EditText sslPortEditField = (EditText)findViewById(R.id.ssl_port);
     
     
     final TextView pin = (TextView)findViewById(R.id.ssl_clientcert_pin);
 
     // Configure UI to current settings state...
 
     boolean sslEnabled = AppSettingsModel.isSSLEnabled(this);
 
     sslToggleButton.setChecked(sslEnabled);
     sslPortEditField.setText("" + AppSettingsModel.getSSLPort(this));
 
     // If SSL is off, disable the port edit field by default...
 
     if (!sslEnabled)
     {
       sslPortEditField.setEnabled(false);
       sslPortEditField.setFocusable(false);
       sslPortEditField.setFocusableInTouchMode(false);
     }
 
     // Manage state changes to SSL toggle...
 
     sslToggleButton.setOnCheckedChangeListener(
         new OnCheckedChangeListener()
         {
           public void onCheckedChanged(CompoundButton buttonView, boolean isEnabled)
           {
 
             // If SSL is being disabled, and the user had soft keyboard open, close it...
 
             if (!isEnabled)
             {
               InputMethodManager input = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
               input.hideSoftInputFromWindow(sslPortEditField.getWindowToken(), 0);
             }
 
             // Set SSL state in config model accordingly...
 
             AppSettingsModel.enableSSL(AppSettingsActivity.this, isEnabled);
 
             // Enable/Disable SSL Port text field according to SSL toggle on/off state...
 
             sslPortEditField.setEnabled(isEnabled);
             sslPortEditField.setFocusable(isEnabled);
             sslPortEditField.setFocusableInTouchMode(isEnabled);
           }
         }
     );
     
     pin.setText("...");
     
     final Handler pinHandler = new Handler() {
     	@Override
     	public void handleMessage(Message msg) {
     		super.handleMessage(msg);
     	    pin.setText(msg.getData().getString("pin"));
     	}
     };
     
     new Thread() {
     	public void run() {  		
     		String pin = ORKeyPair.getInstance().getPIN(getApplicationContext());
 
     		Bundle bundle = new Bundle();
     		bundle.putString("pin", pin);
     		
     		Message msg = pinHandler.obtainMessage();
     		msg.setData(bundle);	
     		
     		msg.sendToTarget();
     	}
     }.start();
     
     sslPortEditField.setOnKeyListener(new OnKeyListener()
     {
       public boolean onKey(View v, int keyCode, KeyEvent event)
       {
         if (keyCode == KeyEvent.KEYCODE_ENTER)
         {
            String sslPortStr = ((EditText)v).getText().toString();
 
            try
            {
                int sslPort = Integer.parseInt(sslPortStr.trim());
                AppSettingsModel.setSSLPort(AppSettingsActivity.this, sslPort);
            }
 
            catch (NumberFormatException ex)
            {
               Toast toast = Toast.makeText(getApplicationContext(), "SSL port format is not correct.", 1);
               toast.show();
 
               return false;
            }
 
            catch (IllegalArgumentException e)
            {
              Toast toast = Toast.makeText(getApplicationContext(), e.getMessage(), 2);
              toast.show();
 
              sslPortEditField.setText("" + AppSettingsModel.getSSLPort(AppSettingsActivity.this));
              
              return false;
            }
         }
 
         return false;
       }
 
     });
       
   }
 
    /**
     * Adds the onclick listener on done button.
     * 
     * If success, start Main activity and reload the application.
     */
    private void addOnclickListenerOnDoneButton() {
       Button doneButton = (Button)findViewById(R.id.setting_done);
       doneButton.setOnClickListener(new OnClickListener() {
          public void onClick(View v) {
             if ("".equals(currentServer)) {
                ViewHelper.showAlertViewWithTitle(AppSettingsActivity.this, "Warning",
                      "No controller. Please configure Controller URL manually.");
                return;
             }
             String selectedPanel = (String)panelSelectSpinnerView.getSelectedItem();
             if (!TextUtils.isEmpty(selectedPanel) && !selectedPanel.equals(PanelSelectSpinnerView.CHOOSE_PANEL)) {
                AppSettingsModel.setCurrentPanelIdentity(AppSettingsActivity.this, selectedPanel);
             } else {
                ViewHelper.showAlertViewWithTitle(AppSettingsActivity.this, "Warning",
                      "No Panel. Please configure Panel Identity manually.");
                return;
             }
             if(AppSettingsModel.isSSLEnabled(getApplicationContext()) 
             		&& AppSettingsModel.getSSLPort(getApplicationContext()) == 8443) {
             	retrieveCertificate();
             	return;
             }
             startMain();
          }
       });
    }
    
    /**
     * Finish the settings activity.
     */
    private void addOnclickListenerOnCancelButton() {
       Button cancelButton = (Button)findViewById(R.id.setting_cancel);
       cancelButton.setOnClickListener(new OnClickListener() {
          public void onClick(View v) {
             finish();
          }
       });
    }
 
    /**
     * Creates the clear image cache button, add listener for clear image cache.
     * @return the relative layout
     */
    private RelativeLayout createClearImageCacheButton() {
       RelativeLayout clearImageView = new RelativeLayout(this);
       clearImageView.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
       
       Button clearImgCacheBtn = new Button(this);
       clearImgCacheBtn.setText("Clear Image Cache");
       RelativeLayout.LayoutParams clearButtonLayout = new RelativeLayout.LayoutParams(150, LayoutParams.WRAP_CONTENT);
       clearButtonLayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
       clearImgCacheBtn.setLayoutParams(clearButtonLayout);
       clearImgCacheBtn.setOnClickListener(new OnClickListener() {
          public void onClick(View v) {
             ViewHelper.showAlertViewWithTitleYesOrNo(AppSettingsActivity.this, "",
                   "Are you sure you want to clear image cache?", new DialogInterface.OnClickListener() {
                      public void onClick(DialogInterface dialog, int which) {
                         FileUtil.clearImagesInCache(AppSettingsActivity.this);
                      }
                   });
          }
       });
       
       clearImageView.addView(clearImgCacheBtn);
       return clearImageView;
    }
 
    /**
     * Creates the auto layout.
     * It contains toggle button to switch auto state, two text view to indicate auto messages.
     * 
     * @return the relative layout
     */
    private RelativeLayout createAutoLayout() {
       RelativeLayout autoLayout = new RelativeLayout(this);
       autoLayout.setPadding(10, 5, 10, 10);
       autoLayout.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, 80));
       TextView autoText = new TextView(this);
       RelativeLayout.LayoutParams autoTextLayout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
       autoTextLayout.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
       autoTextLayout.addRule(RelativeLayout.CENTER_VERTICAL);
       autoText.setLayoutParams(autoTextLayout);
       autoText.setText("Auto Discovery");
 
       ToggleButton autoButton = new ToggleButton(this);
       autoButton.setWidth(150);
       RelativeLayout.LayoutParams autoButtonLayout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
       autoButtonLayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
       autoButtonLayout.addRule(RelativeLayout.CENTER_VERTICAL);
       autoButton.setLayoutParams(autoButtonLayout);
       autoButton.setChecked(autoMode);
       autoButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
          public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
             appSettingsView.removeViewAt(2);
             currentServer = "";
             if (isChecked) {
                IPAutoDiscoveryServer.isInterrupted = false;
                appSettingsView.addView(constructAutoServersView(), 2);
             } else {
                IPAutoDiscoveryServer.isInterrupted = true;
                appSettingsView.addView(constructCustomServersView(), 2);
             }
             AppSettingsModel.setAutoMode(AppSettingsActivity.this, isChecked);
          }
       });
       
       TextView infoText = new TextView(this);
       RelativeLayout.LayoutParams infoTextLayout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
       infoTextLayout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
       infoTextLayout.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
       infoText.setLayoutParams(infoTextLayout);
       infoText.setTextSize(10);
       infoText.setText("Turn off auto-discovery to input controller url manually.");
       
       autoLayout.addView(autoText);
       autoLayout.addView(autoButton);
       autoLayout.addView(infoText);
       return autoLayout;
    }
 
    /**
     * Inits the custom servers from customServers.xml.
     * 
     * @param customServers the custom servers
     */
    private void initCustomServersFromFile(ArrayList<String> customServers) {
       String storedUrls = AppSettingsModel.getCustomServers(this);
       if (! TextUtils.isEmpty(storedUrls)) {
          String[] data = storedUrls.split(",");
          int dataNum = data.length;
          for (int i = 0; i < dataNum; i++) {
             if(!data[i].startsWith("+")){
                customServers.add(data[i]);
             } else {
                currentCustomServerIndex = i;
                customServers.add(data[i].substring(1));
                AppSettingsModel.setCurrentServer(AppSettingsActivity.this, data[i].substring(1));
             }
          }
       }
    }
    
    /**
     * It contains a list view to display custom servers, 
     * "Add" button to add custom server, "Delete" button to delete custom server.
     * The custom servers would be saved in customServers.xml. If click a list item, it would be saved as current server.
     * 
     * @return the linear layout
     */
    private LinearLayout constructCustomServersView() {
       LinearLayout custumeView = new LinearLayout(this);
       custumeView.setOrientation(LinearLayout.VERTICAL);
       custumeView.setPadding(20, 5, 5, 0);
       custumeView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
       
       ArrayList<String> customServers = new ArrayList<String>();
       initCustomServersFromFile(customServers);
       
       RelativeLayout buttonsView = new RelativeLayout(this);
       buttonsView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, 80));
       Button addServer = new Button(this);
       addServer.setWidth(80);
       RelativeLayout.LayoutParams addServerLayout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
       addServerLayout.addRule(RelativeLayout.CENTER_HORIZONTAL);
       addServer.setLayoutParams(addServerLayout);
       addServer.setText("Add");
       addServer.setOnClickListener(new OnClickListener() {
          public void onClick(View v) {
             Intent intent = new Intent();
             intent.setClass(AppSettingsActivity.this, AddServerActivity.class);
             startActivityForResult(intent, Constants.REQUEST_CODE);
          }
          
       });
       Button deleteServer = new Button(this);
       deleteServer.setWidth(80);
       RelativeLayout.LayoutParams deleteServerLayout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
       deleteServerLayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
       deleteServer.setLayoutParams(deleteServerLayout);
       deleteServer.setText("Delete");
       deleteServer.setOnClickListener(new OnClickListener() {
          @SuppressWarnings("unchecked")
          public void onClick(View v) {
             int checkedPosition = customListView.getCheckedItemPosition();
             if (!(checkedPosition == ListView.INVALID_POSITION)) {
                customListView.setItemChecked(checkedPosition, false);
                ((ArrayAdapter<String>)customListView.getAdapter()).remove(customListView.getItemAtPosition(checkedPosition).toString());
                currentServer = "";
                AppSettingsModel.setCurrentServer(AppSettingsActivity.this, currentServer);
                writeCustomServerToFile();
             }
          }
       });
       
       buttonsView.addView(addServer);
       buttonsView.addView(deleteServer);
       
       customListView = new ListView(this);
       customListView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, 200));
       customListView.setCacheColorHint(0);
       final ArrayAdapter<String> serverListAdapter = new ArrayAdapter<String>(appSettingsView.getContext(), R.layout.server_list_item,
               customServers);
       customListView.setAdapter(serverListAdapter);
       customListView.setItemsCanFocus(true);
       customListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
       if (currentCustomServerIndex != -1) {
          customListView.setItemChecked(currentCustomServerIndex, true);
          currentServer = (String)customListView.getItemAtPosition(currentCustomServerIndex);
       }
       customListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
             currentServer = (String)parent.getItemAtPosition(position);
             AppSettingsModel.setCurrentServer(AppSettingsActivity.this, currentServer);
             writeCustomServerToFile();
             requestPanelList();
             requestAccess();
          }
          
       });
       
       custumeView.addView(customListView);
       custumeView.addView(buttonsView);
       requestPanelList();
       requestAccess();
       return custumeView;
   }
 
    /**
     * Received custom server from AddServerActivity, add prefix "http://" before it.
     * Add the result in custom server list and set it as current server.
     * 
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       if (data != null) {
          String result = data.getDataString();
          if (Constants.REQUEST_CODE == requestCode && !TextUtils.isEmpty(result)) {
             if (Constants.RESULT_CONTROLLER_URL == resultCode) {
                currentServer = "http://" + result;
                ArrayAdapter<String> customeListAdapter = (ArrayAdapter<String>) customListView.getAdapter();
                customeListAdapter.add(currentServer);
                customListView.setItemChecked(customeListAdapter.getCount() - 1, true);
                AppSettingsModel.setCurrentServer(AppSettingsActivity.this, currentServer);
                writeCustomServerToFile();
                requestPanelList();
                requestAccess();
             }
          }
       }
    }
   
    /**
     * Auto discovery servers and add them in a list view.
     * Click a list item and make it as current server.
     * 
     * @return the list view
     */
    private ListView constructAutoServersView() {
       final ListView lv = new ListView(this);
       lv.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, 200));
       lv.setPadding(20, 5, 5, 10);
       lv.setBackgroundColor(0);
       lv.setCacheColorHint(0);
       lv.setItemsCanFocus(true);
       lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
       final ArrayAdapter<String> serverListAdapter = new ArrayAdapter<String>(appSettingsView.getContext(), R.layout.server_list_item,
             new ArrayList<String>());
       lv.setAdapter(serverListAdapter);
       
       new IPAutoDiscoveryServer(){
          @Override
          protected void onProgressUpdate(Void... values) {
             if (progressLayout != null) {
                progressLayout.setVisibility(View.VISIBLE);
             }
          }
 
          @Override
          protected void onPostExecute(List<String> result) {
             int length = result.size();
             for (int i = 0; i < length; i++) {
                serverListAdapter.add(result.get(i));
             }
             if (length > 0) {
                lv.setItemChecked(0, true);
                currentServer = serverListAdapter.getItem(0);
                AppSettingsModel.setCurrentServer(AppSettingsActivity.this, currentServer);
             }
             if (progressLayout != null) {
                progressLayout.setVisibility(View.INVISIBLE);
             }
             requestPanelList();
             requestAccess();
          }
       }.execute((Void) null);
       
       lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
             currentServer = (String)parent.getItemAtPosition(position);
             AppSettingsModel.setCurrentServer(AppSettingsActivity.this, currentServer);
             requestPanelList();
             requestAccess();
          }
       });
       
       return lv;
    }
 
    /**
     *  Construct custom servers to a string which split by "," and write it to customServers.xml.
     */
    private void writeCustomServerToFile() {
       int customServerCount = customListView.getCount();
       if (customServerCount > 0) {
          int checkedPosition = customListView.getCheckedItemPosition();
          String customServerUrls = "";
          for (int i = 0; i < customServerCount; i++) {
             if (i != checkedPosition) {
                customServerUrls = customServerUrls + customListView.getItemAtPosition(i).toString();
             } else {
                customServerUrls = customServerUrls + StringUtil.markControllerServerURLSelected(customListView.getItemAtPosition(i).toString());
             }
             if (i != customServerCount - 1) {
             	customServerUrls = customServerUrls + ",";
             }
          }
          if (!TextUtils.isEmpty(customServerUrls)) {
             AppSettingsModel.setCustomServers(customListView.getContext(), customServerUrls);
          }
       }
    }
 
    /**
     * Submits a Certification Request to the controller
     */
    private void requestAccess()
    {
 	   final String hostname = AppSettingsActivity.currentServer;
 	   final ProgressDialog progress = new ProgressDialog(this);
 	   final Handler handler = new Handler()
 	   {
 		   public void handleMessage(Message msg) {
 			   if(msg.what == 200 || msg.what == 501) {
 				   AppSettingsModel.enableSSL(AppSettingsActivity.this, true);
 				   final ToggleButton sslToggleButton = (ToggleButton)findViewById(R.id.ssl_toggle);
 				   sslToggleButton.setChecked(true);
 			   }
			   progress.dismiss();
 		   }
 	   };
 	   
	   progress.show();
 	   new Thread()
 	   {
 		   public void run() {
 			   handler.sendEmptyMessage(
 					   ORPKCS10CertificationRequest.getInstance(getApplicationContext())
 			   			.submitCertificationRequest(hostname)
 			   		);
 		   }
 	   }.start();
    }
 
    private void retrieveCertificate()
    {
 	   final ORKeyStore ks = ORKeyStore.getInstance(getApplicationContext());
 	   final Button doneButton = (Button)findViewById(R.id.setting_done);
 	   doneButton.setEnabled(false);
 	   final ProgressDialog dialog = ProgressDialog.show(this, "Fetching certificate", "Busy fetching certificate");	 
 
 	   
 	   final Handler handler = new Handler()
 	   {
 		   @Override
 		   public void handleMessage(Message msg) {
 			   doneButton.setEnabled(true);
 			   dialog.cancel();
 			   if(msg.what == 0) {
 				   startMain();   
 			   } else {
 				   ViewHelper.showAlertViewWithTitle(AppSettingsActivity.this, "No access", "sadly you don't have access yet");
 			   }
 			   
 		   }  
 	   };
 	   
 	   if(ks.contains(AppSettingsActivity.currentServer)) {
 		   handler.sendEmptyMessage(0);
 	   } else {
 		ks.getSignedChain(
 				AppSettingsModel.getCurrentServer(getApplicationContext()),
 				handler
 			);
 	   }
    }
    
    /**
     * Start {@link Main} activity
     */
    private void startMain()
    {
        Intent intent = new Intent();
        intent.setClass(AppSettingsActivity.this, Main.class);
        startActivity(intent);
        finish();
    }
    
    /**
     * Request panel identity list from controller.
     */
    private void requestPanelList() {
       setEmptySpinnerContent();
      if (!TextUtils.isEmpty(AppSettingsActivity.currentServer)) {
          loadingPanelProgress.show();
          new ORConnection(this.getApplicationContext() ,ORHttpMethod.GET, true, AppSettingsActivity.currentServer + "/rest/panels", this);
       }
    }
    
    @Override
    public void urlConnectionDidFailWithException(Exception e) {
       loadingPanelProgress.dismiss();
    }
 
    @Override
    public void urlConnectionDidReceiveData(InputStream data) {
       loadingPanelProgress.dismiss();
       try {
          DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
          DocumentBuilder builder = factory.newDocumentBuilder();
          Document dom = builder.parse(data);
          Element root = dom.getDocumentElement();
 
          NodeList nodeList = root.getElementsByTagName("panel");
          int nodeNums = nodeList.getLength();
          if (nodeNums == 1) {
             panelSelectSpinnerView.setOnlyPanel(nodeList.item(0).getAttributes().getNamedItem("name").getNodeValue());
          }
       } catch (IOException e) {
          Log.e(Constants.LOG_CATEGORY + "PANEL LIST", "The data is from ORConnection is bad", e);
          return;
       } catch (ParserConfigurationException e) {
          Log.e(Constants.LOG_CATEGORY + "PANEL LIST", "Cant build new Document builder", e);
          return;
       } catch (SAXException e) {
          Log.e(Constants.LOG_CATEGORY + "PANEL LIST", "Parse data error", e);
          return;
       }
       
    }
 
    @Override
    public void urlConnectionDidReceiveResponse(HttpResponse httpResponse) {
       int statusCode = httpResponse.getStatusLine().getStatusCode();
       if (statusCode != Constants.HTTP_SUCCESS) {
          loadingPanelProgress.dismiss();
          if (statusCode == ControllerException.UNAUTHORIZED) {
             LoginDialog loginDialog = new LoginDialog(this);
             loginDialog.setOnClickListener(loginDialog.new OnloginClickListener() {
                @Override
                public void onClick(View v) {
                   super.onClick(v);
                   requestPanelList();
                   requestAccess();
                }
                
             });
          } else {
             // The following code customizes the dialog, because the finish method should do after dialog show and click ok.
             AlertDialog alertDialog = new AlertDialog.Builder(this).create();
             alertDialog.setTitle("Panel List Not Found");
             alertDialog.setMessage(ControllerException.exceptionMessageOfCode(statusCode));
             alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                   return;
                }
             });
             alertDialog.show();
          }
       }
       
    }
    
    private void setEmptySpinnerContent() {
       if (panelSelectSpinnerView != null) {
          panelSelectSpinnerView.setOnlyPanel(PanelSelectSpinnerView.CHOOSE_PANEL);
       }
    }
 }
