 package org.ieeedtu.troika.fragment;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.StrictMode;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.util.EntityUtils;
 import org.ieeedtu.troika.R;
 import org.ieeedtu.troika.utils.SubmitRegister;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 
 /**
  * A simple {@link android.support.v4.app.Fragment} subclass.
  * Activities that contain this fragment must implement the
  * {@link RegisterFragment.OnFragmentInteractionListener} interface
  * to handle interaction events.
  * Use the {@link RegisterFragment#newInstance} factory method to
  * create an instance of this fragment.
  */
 public class RegisterFragment extends Fragment {
 
     private String registerName = new String();
     private String registerEmail = new String();
     private String registerPhone = new String();
     private String registerTeam = new String();
 
     // TODO: Rename parameter arguments, choose names that match
     // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
     private static final String ARG_PARAM1 = "param1";
     private static final String ARG_PARAM2 = "param2";
 
     // TODO: Rename and change types of parameters
     private String mParam1;
     private String mParam2;
 
     private OnFragmentInteractionListener mListener;
 
     /**
      * Use this factory method to create a new instance of
      * this fragment using the provided parameters.
      *
      * @param param1 Parameter 1.
      * @param param2 Parameter 2.
      * @return A new instance of fragment RegisterFragment.
      */
     // TODO: Rename and change types and number of parameters
     public static RegisterFragment newInstance(String param1, String param2) {
         RegisterFragment fragment = new RegisterFragment();
         Bundle args = new Bundle();
         args.putString(ARG_PARAM1, param1);
         args.putString(ARG_PARAM2, param2);
         fragment.setArguments(args);
         return fragment;
     }
 
     public RegisterFragment() {
         // Required empty public constructor
     }
 
     @SuppressLint("NewApi")
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         if (getArguments() != null) {
             mParam1 = getArguments().getString(ARG_PARAM1);
             mParam2 = getArguments().getString(ARG_PARAM2);
         }
 
         StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
         StrictMode.setThreadPolicy(policy);
 
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
         // Inflate the layout for this fragment
         View rootView = inflater.inflate(R.layout.fragment_register, container, false);
         final EditText rName, rEmail, rPhone, rTeam;
         final CheckBox
                 bits,
                 bots,
                 brainwave,
                 bytes,
                 design_pro,
                 electrocution,
                 envision,
                 ether_avatar,
                 inspironnature,
                 junkyard,
                 mist,
                 radix,
                 spac,
                 technovision,
                 todo_en_uno,
                 vihaan;
 
         rName = (EditText) rootView.findViewById(R.id.register_name);
         rEmail = (EditText) rootView.findViewById(R.id.register_email);
         rPhone = (EditText) rootView.findViewById(R.id.register_phone);
         rTeam = (EditText) rootView.findViewById(R.id.register_teamname);
         bits = (CheckBox) rootView.findViewById(R.id.register_bits_check);
         bots = (CheckBox) rootView.findViewById(R.id.register_bots_check);
         brainwave = (CheckBox) rootView.findViewById(R.id.register_brainwave_check);
         bytes = (CheckBox) rootView.findViewById(R.id.register_bytes_check);
         design_pro = (CheckBox) rootView.findViewById(R.id.register_design_pro_check);
         electrocution = (CheckBox) rootView.findViewById(R.id.register_electrocution_check);
         envision = (CheckBox) rootView.findViewById(R.id.register_envision_check);
         ether_avatar = (CheckBox) rootView.findViewById(R.id.register_ether_avatar_check);
         inspironnature = (CheckBox) rootView.findViewById(R.id.register_inspironnature_check);
         junkyard = (CheckBox) rootView.findViewById(R.id.register_junkyard_check);
         mist = (CheckBox) rootView.findViewById(R.id.register_mist_check);
         radix = (CheckBox) rootView.findViewById(R.id.register_radix_check);
         spac = (CheckBox) rootView.findViewById(R.id.register_spac_check);
         technovision = (CheckBox) rootView.findViewById(R.id.register_technovision_check);
         todo_en_uno = (CheckBox) rootView.findViewById(R.id.register_todo_en_uno_check);
         vihaan = (CheckBox) rootView.findViewById(R.id.register_vihaan_check);
 
         Button submit = (Button) rootView.findViewById(R.id.register_submit);
         submit.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 registerName = rName.getText().toString();
                 registerEmail = rEmail.getText().toString();
                 registerPhone = rPhone.getText().toString();
                 registerTeam = rTeam.getText().toString();
 
                 if (registerName.length() < 1
                         || registerEmail.length() < 1
                         || registerPhone.length() < 1
                         || registerTeam.length() < 1) {
                     Toast invalid = Toast.makeText(getActivity().getApplicationContext(), "Name, Email, Phone and Team are required", Toast.LENGTH_SHORT);
                     invalid.show();
                     return;
                 }
 
                 String reg_bits = String.valueOf(bits.isChecked());
                 String reg_bots = String.valueOf(bots.isChecked());
                 String reg_brainwave = String.valueOf(brainwave.isChecked());
                 String reg_bytes = String.valueOf(bytes.isChecked());
                 String reg_design_pro = String.valueOf(design_pro.isChecked());
                 String reg_electrocution = String.valueOf(electrocution.isChecked());
                 String reg_envision = String.valueOf(envision.isChecked());
                 String reg_ether_avatar = String.valueOf(ether_avatar.isChecked());
                 String reg_inspironnature = String.valueOf(inspironnature.isChecked());
                 String reg_junkyard = String.valueOf(junkyard.isChecked());
                 String reg_mist = String.valueOf(mist.isChecked());
                 String reg_radix = String.valueOf(radix.isChecked());
                 String reg_spac = String.valueOf(spac.isChecked());
                 String reg_technovision = String.valueOf(technovision.isChecked());
                 String reg_todo_en_uno = String.valueOf(todo_en_uno.isChecked());
                 String reg_vihaan = String.valueOf(vihaan.isChecked());
 
 
                 HttpClient submitClient = new DefaultHttpClient();
                HttpPost submitPost = new HttpPost("http://ieeedtu.com/appdata/submit-app.php");
                 List<NameValuePair> postPair = new ArrayList<NameValuePair>();
                 postPair.add(new BasicNameValuePair("name", registerName));
                 postPair.add(new BasicNameValuePair("email", registerEmail));
                 postPair.add(new BasicNameValuePair("phone", registerPhone));
                 postPair.add(new BasicNameValuePair("teamname", registerTeam));
                 postPair.add(new BasicNameValuePair("bits", reg_bits));
                 postPair.add(new BasicNameValuePair("bots", reg_bots));
                 postPair.add(new BasicNameValuePair("brainwave", reg_brainwave));
                 postPair.add(new BasicNameValuePair("bytes", reg_bytes));
                 postPair.add(new BasicNameValuePair("design_pro", reg_design_pro));
                 postPair.add(new BasicNameValuePair("electrocution", reg_electrocution));
                 postPair.add(new BasicNameValuePair("envision", reg_envision));
                 postPair.add(new BasicNameValuePair("ether_avatar", reg_ether_avatar));
                 postPair.add(new BasicNameValuePair("inspironnature", reg_inspironnature));
                 postPair.add(new BasicNameValuePair("junkyard", reg_junkyard));
                 postPair.add(new BasicNameValuePair("mist", reg_mist));
                 postPair.add(new BasicNameValuePair("radix", reg_radix));
                 postPair.add(new BasicNameValuePair("spac", reg_spac));
                 postPair.add(new BasicNameValuePair("technovision", reg_technovision));
                 postPair.add(new BasicNameValuePair("todo_en_uno", reg_todo_en_uno));
                 postPair.add(new BasicNameValuePair("vihaan", reg_vihaan));
 
                 try {
                     submitPost.setEntity(new UrlEncodedFormEntity(postPair));
                 } catch (UnsupportedEncodingException e) {
                     e.printStackTrace();
                 }
                 Log.d("TROIKA REGISTER", postPair.toString());
                 SubmitRegister submitRegister = new SubmitRegister(submitClient, submitPost);
                 submitRegister.execute();
             }
         });
 
         return rootView;
     }
 
     // TODO: Rename method, update argument and hook method into UI event
     public void onButtonPressed(Uri uri) {
         if (mListener != null) {
             mListener.onFragmentInteraction(uri);
         }
     }
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
         try {
             mListener = (OnFragmentInteractionListener) activity;
         } catch (ClassCastException e) {
             throw new ClassCastException(activity.toString()
                     + " must implement OnFragmentInteractionListener");
         }
     }
 
     @Override
     public void onDetach() {
         super.onDetach();
         mListener = null;
     }
 
     /**
      * This interface must be implemented by activities that contain this
      * fragment to allow an interaction in this fragment to be communicated
      * to the activity and potentially other fragments contained in that
      * activity.
      * <p/>
      * See the Android Training lesson <a href=
      * "http://developer.android.com/training/basics/fragments/communicating.html"
      * >Communicating with Other Fragments</a> for more information.
      */
     public interface OnFragmentInteractionListener {
         // TODO: Update argument type and name
         public void onFragmentInteraction(Uri uri);
     }
 
 }
