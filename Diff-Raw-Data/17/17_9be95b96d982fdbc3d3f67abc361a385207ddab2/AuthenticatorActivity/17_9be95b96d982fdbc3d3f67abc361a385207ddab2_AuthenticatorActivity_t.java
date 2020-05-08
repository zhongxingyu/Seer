 
 package com.github.arekolek.knurek.auth;
 
 import android.accounts.Account;
 import android.accounts.AccountAuthenticatorActivity;
 import android.accounts.AccountAuthenticatorResponse;
 import android.accounts.AccountManager;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.View;
 import android.widget.Toast;
 
 import com.github.arekolek.knurek.R;
import com.github.arekolek.knurek.sync.CustomHeaderInterceptor;
 import com.googlecode.androidannotations.annotations.AfterInject;
 import com.googlecode.androidannotations.annotations.Background;
 import com.googlecode.androidannotations.annotations.Click;
 import com.googlecode.androidannotations.annotations.EActivity;
 import com.googlecode.androidannotations.annotations.InstanceState;
 import com.googlecode.androidannotations.annotations.UiThread;
 import com.googlecode.androidannotations.annotations.ViewById;
 import com.googlecode.androidannotations.annotations.rest.RestService;
 
import org.springframework.http.client.ClientHttpRequestInterceptor;
 import org.springframework.web.client.RestClientException;
 
import java.util.ArrayList;
import java.util.List;

 @EActivity(R.layout.activity_login)
 public class AuthenticatorActivity extends AccountAuthenticatorActivity {
 
     private static final String TAG = "AuthenticatorActivity";
 
     public static Intent intent(Context context, AccountAuthenticatorResponse response) {
         Intent intent = AuthenticatorActivity_.intent(context).get();
         intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
         return intent;
     }
 
     @InstanceState
     String identifier;
 
     @RestService
     KnurekRestClient client;
 
     @ViewById
     View loading;
 
     @ViewById
     View step1;
 
     @ViewById
     View step2;
 
     @AfterInject
     void initClient() {
         RestUtils.setClientTimeout(client, 2000);
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<ClientHttpRequestInterceptor>();
        interceptors.add(new CustomHeaderInterceptor());
        client.getRestTemplate().setInterceptors(interceptors);
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         showView(loading);
 
         if (identifier == null) {
             getIdentifier();
         } else {
             getUsername();
         }
     }
 
     @UiThread
     void showView(View view) {
         loading.setVisibility(View.GONE);
         step1.setVisibility(View.GONE);
         step2.setVisibility(View.GONE);
 
         view.setVisibility(View.VISIBLE);
     }
 
     @Background
     void getUsername() {
         try {
             Auth auth = client.getUsername(identifier);
             if (auth != null && !TextUtils.isEmpty(auth.name)) {
                 setUsername(auth.name);
                 return;
             }
         } catch (RestClientException e) {
         }
         showView(step1);
     }
 
     @UiThread
     void setUsername(String name) {
         String type = Constants.ACCOUNT_TYPE;
 
         AccountManager.get(this).addAccountExplicitly(new Account(name, type), identifier, null);
 
         Intent intent = new Intent();
         intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, name);
         intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, type);
         setAccountAuthenticatorResult(intent.getExtras());
         setResult(RESULT_OK, intent);
 
         showView(step2);
     }
 
     @Background
     void getIdentifier() {
         try {
             Auth auth = client.getIdentifier();
             setIdentifier(auth.identifier);
         } catch (RestClientException e) {
             Log.e(TAG, "Connection error in getIdentifier", e);
             showError(RestUtils.getErrorMessage(e));
         }
     }
 
     @UiThread
     void setIdentifier(String identifier) {
         this.identifier = identifier;
         showView(step1);
     }
 
     @UiThread
     void showError(String message) {
         Toast.makeText(this, message, Toast.LENGTH_LONG).show();
         finish();
     }
 
     @Click(R.id.submit)
     void onSubmit() {
         Uri uri = Uri.parse(Constants.ROOT_URL + "/api/auth/?identifier=" + identifier);
         startActivity(new Intent(Intent.ACTION_VIEW, uri));
     }
 
     @Click(R.id.finish)
     void onFinish() {
         finish();
     }
 
 }
