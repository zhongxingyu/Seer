 /**
  * Copyright (C) 2011 Adriano Monteiro Marques
  *
  * Author:  Zubair Nabi <zn.zubairnabi@gmail.com>
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  * USA
  */
 
 package org.umit.icm.mobile;
 
 import java.util.regex.Pattern;
 
 import org.umit.icm.mobile.R;
 
 import android.app.Dialog;
 import android.content.Context;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 import android.widget.RadioButton;
 
 
 
 public class SuggestionDialog extends Dialog {
 	
 	private String selection;
 	private Context contextControl;
     private ReadyListener readyListener;
     EditText etSuggest, etEmail;
     private RadioButton sRB, wRB;
     private final Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile(
             "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
             "\\@" +
             "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
             "(" +
             "\\." +
             "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
             ")+"
         );
     
     public SuggestionDialog(Context context, String selection, 
             ReadyListener readyListener) {
         super(context);
         this.selection = selection;
         this.readyListener = readyListener;
         this.contextControl = context;
     }
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.suggestiondialog);
        setTitle("Suggestion Dialog");
         Button buttonOK = (Button) findViewById(R.id.Button01);
         buttonOK.setOnClickListener(new sendListener());
         etSuggest = (EditText) findViewById(R.id.EditText01);
         etEmail = (EditText) findViewById(R.id.EditTextEmail);
         wRB = (RadioButton)findViewById(R.id.websiterb);
         sRB = (RadioButton)findViewById(R.id.servicerb);
         
     }
     
     public interface ReadyListener {
         public void ready(String selection);
     }
     
     private class sendListener implements android.view.View.OnClickListener {
 
 		@Override
 		public void onClick(View arg0) {
 			// TODO Auto-generated method stub
 			Context context = SuggestionDialog.this.getContext();
 			if((etSuggest.getText().toString().equals("")) && (etEmail.getText().toString().equals(""))){
         		
         		CharSequence text = context.getString(R.string.edit_text_suggestion);
         		int duration = Toast.LENGTH_SHORT;
 
         		Toast toast = Toast.makeText(context, text, duration);
         		toast.show();
         		
         	} 
         	else if(!checkEmail(etEmail.getText().toString())){
         		
         		CharSequence text = context.getString(R.string.toast_email);
         		int duration = Toast.LENGTH_SHORT;
 
         		Toast toast = Toast.makeText(context, text, duration);
         		toast.show();
         	}
         	else{
     	    		if(wRB.isChecked() == true) {
     	    			readyListener.ready(context.getString(R.string.text_selected) + wRB.getText() 
     	    	    			+ "&" + etSuggest.getText().toString() + "&" + etEmail.getText().toString());
     	                SuggestionDialog.this.dismiss();
  
     	    		}
     	    			
     	    		
     	    		else if(sRB.isChecked() == true) {
     	    			readyListener.ready(context.getString(R.string.text_selected) + sRB.getText()
     	    	    			+ "&" + etSuggest.getText().toString() + "&" + etEmail.getText().toString());
     	                SuggestionDialog.this.dismiss();
  
     	    		}
     	    		else{
     	    			
     	        		CharSequence text = context.getString(R.string.toast_selection);
     	        		int duration = Toast.LENGTH_SHORT;
 
     	        		Toast toast = Toast.makeText(context, text, duration);
     	        		toast.show();
     	    		}
         		}                
        		
 			}
 
     	}
     
     private boolean checkEmail(String email) {
         return EMAIL_ADDRESS_PATTERN.matcher(email).matches();
     }
     
 }
