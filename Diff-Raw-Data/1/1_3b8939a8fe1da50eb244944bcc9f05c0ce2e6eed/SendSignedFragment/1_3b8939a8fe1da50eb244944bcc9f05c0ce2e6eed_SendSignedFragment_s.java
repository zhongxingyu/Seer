 package com.lookout.keymaster.fragments;
 
 import android.app.Fragment;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import com.lookout.keymaster.R;
 import com.lookout.keymaster.gpg.GPGCli;
 import com.lookout.keymaster.gpg.GPGFactory;
 import com.lookout.keymaster.gpg.GPGKey;
 
 /**
  * Created with IntelliJ IDEA.
  * User: ayerra
  * Date: 5/29/13
  * Time: 11:02 PM
  * To change this template use File | Settings | File Templates.
  */
 
 public class SendSignedFragment extends Fragment {
     SimpleAdapter adapter;
 
     View rootView;
 
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
 
         rootView = inflater.inflate(R.layout.fragment_send_signed, container, false);
 
         Log.i("LookoutPG", "Siginging " + GPGFactory.getReceivedKeyId());
         new SendingKeyTask2(GPGFactory.getReceivedKey(), GPGFactory.getReceivedKeyId()).execute();
 
         getActivity().setTitle("Key Exchange");
         return rootView;
     }
 
 
     private void setTextForId(int id, String txt) {
         TextView tv = (TextView)rootView.findViewById(id);
         tv.setText(txt);
     }
 
     private class SendingKeyTask2 extends AsyncTask<Void, Void, GPGKey> {
         String keyId, key;
 
         public SendingKeyTask2(String key, String keyId)  {
             this.key = key;
             this.keyId = keyId;
         }
 
         protected GPGKey doInBackground(Void... voids) {
             return GPGCli.getInstance().getPublicKey(keyId);
         }
 
         protected void onPostExecute(GPGKey result) {
             setTextForId(R.id.signed_short_id, keyId);
            setTextForId(R.id.signed_created, result.getPrimaryKeyId().getCreationDate());
             setTextForId(R.id.signed_full_name, result.getPrimaryKeyId().getUserId());
             setTextForId(R.id.signed_email, "");
         }
     }
 }
