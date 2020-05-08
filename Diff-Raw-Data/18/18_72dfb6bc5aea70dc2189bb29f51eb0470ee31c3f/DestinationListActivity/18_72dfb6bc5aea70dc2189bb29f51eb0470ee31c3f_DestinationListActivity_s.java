 package destinationsalinas.historictour;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 
 public class DestinationListActivity extends Activity {
 
 	private List<DestinationClass> destinationList;
 
 	public List<DestinationClass> getDestinationList() {
 		return destinationList;
 	}
 
 	public void setDestinationList(List<DestinationClass> destinationList) {
 		this.destinationList = destinationList;
 	}
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.browsedestination);
 
 		destinationList=new ArrayList<DestinationClass>();
 		DestinationManager dm = new DestinationManager(this.getResources());
 		destinationList=dm.getDestinationList();
 		for(int i=0;i<destinationList.size();i++){
 			System.out.println("**"+destinationList.get(i).getAddress());
 		}
 		ListView browseDestinationList = (ListView) findViewById(R.id.destinationList);
 		ItemListener itemListener = new ItemListener(); 
 		
 		browseDestinationList.setAdapter(new DestinationAdapter(this,R.layout.destinationitem, destinationList));
 		browseDestinationList.setOnItemClickListener(itemListener);
 	}
 	
 	private class ItemListener implements OnItemClickListener {
 
 		@Override
 		public void onItemClick(AdapterView<?> parent, View view, int position,
 				long id) {
 			DestinationClass clickedDest = destinationList.get(position);
 			int destNum = clickedDest.getNumber(); 
 			startDestinationActivity(destNum, view);
 		}
 		
 	}
 	
 	protected void startDestinationActivity(int destNum, View v) {
 		Intent i = new Intent(); 
		i.putExtra("destination-number", destNum);
 		i.setClass(v.getContext(), DestinationActivity.class);
 		this.startActivity(i);
 	}
 	
 	
 
 }
