 package edu.stanford.mdocent;
 
 import java.util.Vector;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 import edu.stanford.mdocent.data.Node;
 import edu.stanford.mdocent.data.Node.Brief;
 import edu.stanford.mdocent.data.Page;
 import edu.stanford.mdocent.data.Section;
 import edu.stanford.mdocent.data.Tour;
 import edu.stanford.mdocent.db.Constants;
 
 
 public class AddNodeActivity extends Activity {
 
 	private static final String TAG = "AddNodeActivity";
 	private final static int nodeRequestCode = 4;
 	private Node newNode;
 	private Tour curTour;
 	private int curTourID;
 	private int curNodeID;
 
 	@Override
 	public void onActivityResult(int requestCode,int resultCode,Intent data){
 		super.onActivityResult(requestCode, resultCode, data);
 		if (resultCode == RESULT_OK){
 			Log.v(TAG, "RESULT_OK");
 			retrieveNode(data);
 		}
 		if (resultCode == RESULT_CANCELED){
 			Log.v(TAG, "RESULT_CANCELED");
 			//etrieveNode(data);
 		}
 		else if (resultCode ==Constants.RESULT_RETURN){
 			Log.v(TAG, "RESULT_RETURN");
 			Intent intent = new Intent(this, CreateTourActivity.class );
 			setResult(Constants.RESULT_RETURN, intent);
 			finish();
 		}
 	}
 
 	private void retrieveNode(Intent data){
 		curTourID = data.getExtras().getInt("tourID");
 		curNodeID = data.getExtras().getInt("nodeID");
 		curTour = Tour.getTourById(curTourID, false);
 		Vector<Node> nodeVec = curTour.getTourNodes();
 		for(int i = 0; i < nodeVec.size(); i++){
 			if(nodeVec.get(i).getNodeId()==curNodeID){
 				curNodeID=nodeVec.get(i).getNodeId();
 				Log.v(TAG, "Retrieved node: "+curNodeID);
 			}
 			else{
 				Log.v(TAG, "Node was null");
 			}
 		}
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.addnode);
 		Intent sender = getIntent();
 		curTourID = sender.getExtras().getInt("tourID");
 		curTour = Tour.getTourById(curTourID, true);
 
 		newNode = new Node();
 		newNode.appendPage(new Page());
 		newNode.setLatitude(sender.getExtras().getDouble("nodeLat"));
 		newNode.setLongitude(sender.getExtras().getDouble("nodeLon"));
 		newNode = curTour.appendNode(newNode);
 
 		curNodeID =  newNode.getNodeId();
 		Log.v(TAG, "New Node is not null: " +(newNode!=null));
 		
 		Button loginButton = (Button) findViewById(R.id.button1);
 		loginButton.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View v) {
 				EditText nameText = (EditText)findViewById(R.id.editText1);
 				String nameStr = nameText.getText().toString();
 				EditText descriptionText = (EditText)findViewById(R.id.editText2);
 				String descriptionStr = descriptionText.getText().toString();
 				//ADD NODE TO TOUR
 
 				Log.v(TAG, "New Node is not null: " +(newNode!=null));
 				Log.v(TAG, "Current node ID: "+curNodeID);
 				
 				Brief newBrief = newNode.getBrief();
 				newBrief.setDesc(descriptionStr);
 				newBrief.setTitle(nameStr);
 				/*newNode.save();// start test
 				Page curPage = newNode.getPages().get(0);
 				
 					Section newSection = new Section();
 					newSection.setContentType(Constants.PLAIN_TEXT);
 					newSection.setContent("OK BRO");
 					curPage.appendSection(newSection);
 
 				newNode.save();//end test */
 				newNode.save();
 				Toast.makeText(getApplicationContext(),"New Node Added.", Toast.LENGTH_LONG).show();
 				startCreateTourSuccess();
 
 			}
 		});
 
 		Button cancelButton = (Button) findViewById(R.id.button2);
 		cancelButton.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View v) {
 				Vector<Node>nodeVec=curTour.getTourNodes();
 				for(int i = 0; i < nodeVec.size(); i++){
 					if(nodeVec.get(i).getNodeId()==curNodeID){
 						nodeVec.remove(i);
 					}
 					else{
 						Log.v(TAG, "Node was null");
 					}
 				}
 				startCreateTourCancel();	
 			}
 		});
 
 		Button addPageButton = (Button) findViewById(R.id.button3);
 		addPageButton.setOnClickListener(new OnClickListener(){
 			public void onClick(View v) {
 				startAddPage();	
 			}
 		});
 
 	}
 	
 	public void startAddPage (){
 		Intent intent = new Intent(this, AddPageActivity.class );
 		intent.putExtra("tourID", curTourID);
 		intent.putExtra("nodeID", curNodeID);
 		startActivityForResult(intent,nodeRequestCode);
 	}
 	public void startCreateTourCancel (){
 		Intent intent = new Intent(this, CreateTourActivity.class );
 		intent.putExtra("tourID", curTourID);
 		setResult(RESULT_CANCELED, intent);
 		finish();
 	}
 	public void startCreateTourSuccess (){
 		Intent intent = new Intent(this, CreateTourActivity.class );
 		intent.putExtra("tourID", curTourID);
 		setResult(RESULT_OK, intent);
 		finish();
 	}
 	/*
 	public void finishCreateTour (){
 		Intent intent = new Intent(this, CreateTourActivity.class );
 		setResult(Constants.RESULT_RETURN, intent);
 	    finish();
 	}*/
 }
