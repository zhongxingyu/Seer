 package com.example.prolog;
 
 
 import java.util.ArrayList;
 
 import com.example.prolog.db.ContactsDataSource;
 import com.example.prolog.model.Interaction;
 
 import android.app.Fragment;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ExpandableListView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ViewInteractionFragment extends Fragment {
 
 	public static final String LOGTAG="EXPLORECA";
 	private ContactsDataSource datasource;
 	private Button buttonAdd;
 	private ExpandableListView expandList;
 	private ExpandListAdapterFragmentInteractions expAdapter;
 	private ArrayList<ExpandListGroupFragmentInteractions> expListItems;
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		
 		super.onActivityCreated(savedInstanceState);
 		datasource=new ContactsDataSource(getActivity());
 		expandList = (ExpandableListView) getView().findViewById(R.id.fragmentInteractionsExpList);
		expListItems = SetStandardGroups();
	    expAdapter = new ExpandListAdapterFragmentInteractions(getActivity(), expListItems);
	    expandList.setAdapter(expAdapter);
 	    
 		buttonAdd = (Button) getView().findViewById(R.id.fragmentInteractionsAddButton);
 		buttonAdd.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				Intent i = new Intent(getActivity().getBaseContext(),NewInteracionActivity.class);
 				long contatcId =  getArguments().getLong("contactId");
 				i.putExtra("contactId", contatcId);
 				startActivity(i);
 			}
 		});
 		
 		
 		
 		/*Interaction interaction=new Interaction();
 		interaction.setText("some stuff");
 		datasource.createInteraction(interaction);
 		ArrayList<Interaction> list=new ArrayList<Interaction>();
 		list=datasource.findInteractionsbyContactId(0);
 		 for (Interaction inter : list)
 		      Toast.makeText(getActivity(), inter.getText(), Toast.LENGTH_LONG).show();*/ //created for test purposes
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 
 		return (RelativeLayout) inflater.inflate(R.layout.fragment_view_interactions, container, false);
 	}
 	
 	@Override
 	public void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		datasource.open();
 	}
 	
 	@Override
 	public void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 		datasource.close();
 	}
 
 	
 	
     public ArrayList<ExpandListGroupFragmentInteractions> SetStandardGroups() {
     	
     	ArrayList<Interaction> listInteractions;
     	ArrayList<ExpandListGroupFragmentInteractions> list = new ArrayList<ExpandListGroupFragmentInteractions>();
         ExpandListGroupFragmentInteractions gru1 = new ExpandListGroupFragmentInteractions();
        // gru1.setName("Phone Contacts");
         
         //Interaction interactionx=new Interaction();
         
         //gru1.setExpandListChildFragmentInteractions(interactionx);
         
         datasource.open();
         listInteractions=datasource.findInteractionsbyContactId(getArguments().getLong("contactId"));
         
         for (Interaction interaction : listInteractions)
         {
         	gru1=new ExpandListGroupFragmentInteractions();
         	gru1.setName(interaction.getDate());
         	gru1.setExpandListChildFragmentInteractions(interaction);
         	list.add(gru1);
         	Log.i(LOGTAG, interaction.getText());
         }
 
         	
       
         
         
         return list;
     }
 
 }
