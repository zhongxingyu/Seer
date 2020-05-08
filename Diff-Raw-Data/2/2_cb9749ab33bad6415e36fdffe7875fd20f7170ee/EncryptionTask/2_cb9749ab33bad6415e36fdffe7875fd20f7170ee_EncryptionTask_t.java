 /*
  * OpenNoteSecure is an Android application for reading and writing encrypted
  * text files to an SD card.
  * Copyright (C) 2010  Jared Hatfield
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *  
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.jaredhatfield.opennotesecure.Tasks;
 
 import com.jaredhatfield.opennotesecure.FileManager;
 import com.jaredhatfield.opennotesecure.OpenNoteSecure;
 import com.jaredhatfield.opennotesecure.EncryptionProviders.AESEncryptionProvider;
 import com.jaredhatfield.opennotesecure.EncryptionProviders.DESEncryptionProvider;
 import com.jaredhatfield.opennotesecure.EncryptionProviders.EncryptionException;
 
 import android.os.AsyncTask;
 import android.util.Log;
 
public class EncryptionTask extends AsyncTask<FileTaskHolder, Void, FileTaskHolder>{
 
 	/**
 	 * Perform the task of encrypting and writing out the file.
 	 */
 	@Override
 	protected FileTaskHolder doInBackground(FileTaskHolder... params) {
 		FileTaskHolder holder = params[0];
 		
 		// Encrypt and write out the file.
 		if(holder.getEncryption().equals("None")){
 			Log.i(OpenNoteSecure.TAG, "Encrypting with Algorithm: None");
 			FileManager.Instance().writeFile(holder.getFile(), holder.getEditTextContent().getText().toString());
 		}
 		else if(holder.getEncryption().equals("AES")){
 			Log.i(OpenNoteSecure.TAG, "Encrypting with Algorithm: AES");
 			String plaintext = holder.getEditTextContent().getText().toString();
 			try {
 				AESEncryptionProvider aes = new AESEncryptionProvider(holder.getPassword());
 				String ciphertext = aes.encryptAsBase64(plaintext);
 				FileManager.Instance().writeFile(holder.getFile(), ciphertext);
 			}
 			catch (EncryptionException e) {
 				// It didn't work!!
 				Log.e(OpenNoteSecure.TAG, e.getMessage());
 			}
 		}
 		else if(holder.getEncryption().equals("DES")){
 			Log.i(OpenNoteSecure.TAG, "Encrypting with Algorithm: DES");
 			String plaintext = holder.getEditTextContent().getText().toString();
 			try{
 				DESEncryptionProvider des = new DESEncryptionProvider(holder.getPassword());
 				String ciphertext = des.encryptAsBase64(plaintext);
 				FileManager.Instance().writeFile(holder.getFile(), ciphertext);
 			}
 			catch (EncryptionException e) {
 				// It didn't work!!
 				Log.e(OpenNoteSecure.TAG, e.getMessage());
 			}
 		}
 		
 		// Wait so it looks like the task takes some time to complete
 		try {
 			Thread.sleep(1000);
 		}
 		catch (InterruptedException e) {
 			Log.e(OpenNoteSecure.TAG, e.getMessage());
 		}
 		
 		return holder;
 	}
 	
 	/**
 	 * Dismiss the dialog after the task is complete.
 	 */
 	@Override
     protected void onPostExecute(FileTaskHolder holder)
     {
 		holder.getDialog().dismiss();
     }
 }
