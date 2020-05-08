 package phillykeyspots.frpapp;
 
 import phillykeyspots.frpapp.XmlParser.Entry;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 /**
  * Loads the Information for an individual Keyspot.
  * 
  * @author btopportaldev
  *
  */
 public class KeyspotInfoFragment extends Fragment {
 
 	
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
 		return inflater.inflate(R.layout.fragment_keyspot_info, container, false);
 	}
 	
 	public void onResume(){
 		super.onResume();
 		updateInfo();
 	}
 	
 	/**
 	 * Takes the entry and inputs relevant information into an array.
 	 * Creates a TextView for each item in the array.
 	 * Adds the TextView to the Layout.
 	 */
 	private void updateInfo(){
 		Entry entry = ((KeyspotActivity)getActivity()).getEntry();
 		String[] info = {"Name: "+entry.keyspot, 
				"Managing Partner: "+entry.managing_partner,
 				"Contact: "+entry.contact,
 				"City: "+entry.city,
 				"Zip Code: "+entry.postal_code,
 				"Address: "+entry.street,
 				"Hours: "+entry.hours,
 				"Workstations: "+entry.workstations,
 				"Restrictions: "+entry.restrictions,
 				"Wi-Fi: "+entry.wi_fi,
 				"Latitude: "+entry.latitude,
 				"Longitude: "+entry.longitude};
 		LinearLayout layout = (LinearLayout)getActivity().findViewById(R.id.keyspot_info);
 		for(String line: info){
 			TextView temp = new TextView(getActivity());
 			temp.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
 			temp.setText(line);
 			temp.setTextColor(getResources().getColor(R.color.blue647C));
 			temp.setTextSize((float) 20);
 			layout.addView(temp);
 		}
 	}
 }
