 package com.geek.exercise.fragments;
 
 import android.app.Activity;
 import android.app.Fragment;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 import com.geek.exercise.Constants;
 import com.geek.exercise.R;
 import com.geek.exercise.managers.StateManager;
 import com.geek.exercise.transfer.Account;
 
 /**
  * Created by Pequots34 on 7/9/13.
  */
 public class AccountFragment extends Fragment {
 
     private IAccountListener mAccountListener;
 
     public AccountFragment() {
         super();
     }
 
     @Override
     public void onCreate( Bundle savedInstanceState ) {
         super.onCreate( savedInstanceState );
 
         setRetainInstance( true );
 
         if ( !StateManager.ApplicationManager.INSTANCE.isInitialized() ) {
             StateManager.ApplicationManager.INSTANCE.initialize( getActivity() );
         }
     }
 
     @Override
     public void onAttach( Activity activity ) {
         super.onAttach( activity );
 
         try {
             mAccountListener = (IAccountListener) activity;
         } catch( ClassCastException e ) {
             throw new ClassCastException( e.toString() );
         }
     }
 
     @Override
     public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
         View view = inflater.inflate( R.layout.fragment_account, null, false );
 
         if ( view != null ) {
             final EditText username = (EditText) view.findViewById( R.id.username );
 
             final Button account = (Button) view.findViewById( R.id.account );
 
             account.setOnClickListener( new View.OnClickListener() {
 
                 @Override
                 public void onClick(View v) {
                     if ( TextUtils.isEmpty( username.getText() ) ) {
                         Toast.makeText( getActivity(), getString( R.string.account_username_empty ), Toast.LENGTH_SHORT ).show();
 
                         return;
                     }
 
                     final String name = username.getText().toString().trim();
 
                     long currentTime = System.currentTimeMillis();
 
                     SharedPreferences.Editor editor = getActivity().getSharedPreferences( Constants.ACCOUNT_PREFERENCE_NAME, Context.MODE_PRIVATE ).edit();
 
                    editor.putString( Constants.ACCOUNT_PREFERENCE_NAME, name );
 
                    editor.putLong( Constants.ACCOUNT_PREFERENCE_CREATED_EXTRA, currentTime );
 
                     boolean commited = editor.commit();
 
                     if ( commited ) {
                         StateManager.ApplicationManager.INSTANCE.setAccount( Account.newBuilder()
                             .setUsername( name )
                             .setCreated( currentTime )
                             .build() );
                     }
 
                     mAccountListener.onAccountChanged( name, commited );
                 }
             } );
         }
 
         return view;
     }
 
     public static interface IAccountListener {
 
         public void onAccountChanged( String account, boolean stored );
 
     }
 
 }
