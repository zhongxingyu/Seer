 package com.maxwolffe.TangRestart;
 
 import android.app.ListFragment;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 
 public class MyListFragment extends ListFragment {
 
 	  @Override
 	  public void onActivityCreated(Bundle savedInstanceState) {
 	    super.onActivityCreated(savedInstanceState);
 	    String[] values = new String[] { "Resources", "Female Anatomy", "Male Anatomy", "Female Sexual Response",
 	        "Male Sexual Response", "Female Ejaculation", "Semen Facts", "Chlamydia", "Crabs",
 	        "Gonorrhea", "Hepatitis", "HPV", "Herpes", "HIV/AIDS", "Molluscum Contagiosum", "Scabies", "Syphilis",
 	        "Trichomoniasis", "Urinary Tract Infections"};
 	    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
 	        android.R.layout.simple_list_item_1, values);
 	    setListAdapter(adapter);
 	  }
 
 	  @Override
 	  public void onListItemClick(ListView l, View v, int position, long id) {
 		  int intId = (int) id;
 		  Intent FactIntent = new Intent(this.getActivity(),Connection.class);
 		  switch(intId){
 		  	case 0:
 		  		FactIntent.putExtra(MainActivity.EXTRA_MESSAGE, "file:///android_asset/resources.html");
 		  		break;
 		  	case 1:
 		  		FactIntent.putExtra(MainActivity.EXTRA_MESSAGE, "file:///android_asset/female_a.html");
 		  		break;
 		  	case 2: 
 		  		FactIntent.putExtra(MainActivity.EXTRA_MESSAGE, "file:///android_asset/male_a.html");
 		  		break;
 		  	case 3:
 		  		FactIntent.putExtra(MainActivity.EXTRA_MESSAGE, "file:///android_asset/female_sexual_response.html");
 		  		break;
 		  	case 4:
 		  		FactIntent.putExtra(MainActivity.EXTRA_MESSAGE, "file:///android_asset/male_sexual_response.html");
 		  		break;
 		  	case 5:
 		  		FactIntent.putExtra(MainActivity.EXTRA_MESSAGE, "file:///android_asset/female_ejaculation.html");
 		  		break;
 		  	case 6:
 		  		FactIntent.putExtra(MainActivity.EXTRA_MESSAGE, "file:///android_asset/semen_facts.html");
 		  		break;
 		  	case 7:
 		  		FactIntent.putExtra(MainActivity.EXTRA_MESSAGE, "file:///android_asset/Chlamydia.html");
 		  		break;
 		  	case 8:
 		  		FactIntent.putExtra(MainActivity.EXTRA_MESSAGE, "file:///android_asset/Crabs.html");
 		  		break;
 		  	case 9:
 		  		FactIntent.putExtra(MainActivity.EXTRA_MESSAGE, "file:///android_asset/Gonorrhea.html");
 		  		break;
 		  	case 10:
 		  		FactIntent.putExtra(MainActivity.EXTRA_MESSAGE, "file:///android_asset/Hepatitis.html");
 		  		break;
 		  	case 11:
		  		FactIntent.putExtra(MainActivity.EXTRA_MESSAGE, "file:///android_asset/HPV.html");
 		  		break;
 		  	case 12:
		  		FactIntent.putExtra(MainActivity.EXTRA_MESSAGE, "file:///android_asset/Herpes.html");
 		  		break;
 		  	case 13:
		  		FactIntent.putExtra(MainActivity.EXTRA_MESSAGE, "file:///android_asset/HIV.html");
 		  		break;
 		  	case 14:
		  		FactIntent.putExtra(MainActivity.EXTRA_MESSAGE, "file:///android_asset/MC.html");
 		  		break;
 		  	case 15:
		  		FactIntent.putExtra(MainActivity.EXTRA_MESSAGE, "file:///android_asset/Scabies.html");
 		  		break;
 		  	case 16:
		  		FactIntent.putExtra(MainActivity.EXTRA_MESSAGE, "file:///android_asset/Syphilis.html");
 		  		break;
 		  	case 17:
		  		FactIntent.putExtra(MainActivity.EXTRA_MESSAGE, "file:///android_asset/Trichomoniasis.html");
		  		break;
		  	case 18:
 		  		FactIntent.putExtra(MainActivity.EXTRA_MESSAGE, "file:///android_asset/UTI.html");
 		  		break;
 		  	default:
 		  		FactIntent.putExtra(MainActivity.EXTRA_MESSAGE, "file:///android_asset/HIV.html");
 		  }
 		  	startActivity(FactIntent);
 
 	  }
 	} 
