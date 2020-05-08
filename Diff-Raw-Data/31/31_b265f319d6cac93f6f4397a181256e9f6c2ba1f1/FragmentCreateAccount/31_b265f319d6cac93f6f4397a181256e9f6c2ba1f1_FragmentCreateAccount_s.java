 package com.example.airbrushrecorder.fragments;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.app.Activity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.EditText;
 
 import com.example.airbrushrecorder.R;
 import com.example.airbrushrecorder.WebInterface;
 
 public class FragmentCreateAccount extends Fragment
 {
 	private Boolean m_logging = false;
 	/*
 	private OnToggleRecordingListener m_listener;
 	
 	public interface OnToggleRecordingListener
 	{
 		public void togglePathLogging(View view);
 	}
 	*/
 	
 	@Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
 	{
 		super.onCreateView(inflater, container, savedInstanceState);
 		return inflater.inflate(R.layout.fragment_create_account, container, false);
 	}
 	
 	@Override
 	public void onAttach(Activity activity)
 	{
 		super.onAttach(activity);
 	}
 	
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 	}
 	
 	private void createAccount()
 	{
 		EditText nameField = (EditText) getView().findViewById(R.id.edit_text_name);
 		String name = nameField.getText().toString();
 		EditText surnameField = (EditText) getView().findViewById(R.id.edit_text_surname);
 		String surname = surnameField.getText().toString();
 		EditText emailField = (EditText) getView().findViewById(R.id.edit_text_email);
 		String email = emailField.getText().toString();
 		EditText passwordField = (EditText) getView().findViewById(R.id.edit_text_password);
 		String password = passwordField.getText().toString();
 		
		WebInterface webInterface = new WebInterface();
 		password = WebInterface.saltPassword(password, email);
 		webInterface.createAccount(name, surname, email, password);
 	}
 }
