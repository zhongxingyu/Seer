 package com.betable;
 
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.Toast;
 
 import com.betable.fragment.BetableLogin;
 import com.betable.fragment.BetableLogin.BetableLoginListener;
 
 public class BetableActivity extends FragmentActivity implements
         BetableLoginListener {
 
     Button loginButton;
     BetableLogin loginView;
 
     private final String clientId = "FNTRHDBp7OGwfFVJPo9OBni5p65A0cwo";
    private final String clientSecret = "y75FbTEoLBTopxlchKM2luyzaIIj5Dkf";
    private final String redirectUri = "https://caseycrites.com/betable/whack-a-malone";
     private String accessToken;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         this.loginButton = (Button) this
                 .findViewById(R.id.betable_login_button);
 
         this.loginButton.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 BetableActivity.this.loginView.show(
                         BetableActivity.this.getSupportFragmentManager(),
                         "betable-login");
             }
 
         });
 
        this.loginView = new BetableLogin(this.clientId, this.clientSecret,
                this.redirectUri);
     }
 
     @Override
     public void onSuccessfulLogin(String accessToken) {
        Toast.makeText(this,
                "Hooray, we have an access token! It's " + accessToken,
                 Toast.LENGTH_LONG).show();
         this.accessToken = accessToken;
         this.loginView.dismiss();
     }
 
     @Override
    public void onFailedLogin(String reason) {
        Toast.makeText(this, "Bummer, something went wrong. " + reason,
                 Toast.LENGTH_LONG).show();
        if (this.loginView.isVisible()) {
            this.loginView.dismiss();
        }
     }
 }
