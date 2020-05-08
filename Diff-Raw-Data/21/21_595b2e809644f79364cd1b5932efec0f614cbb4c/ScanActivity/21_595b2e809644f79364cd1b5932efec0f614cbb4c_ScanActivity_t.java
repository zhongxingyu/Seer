 package se3350y.aleph.firealertscanner;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 import com.dataInput.samplescanner.ScanCodeDemo;
 
 import android.os.Bundle;
 import android.os.Environment;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.text.InputType;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ExpandableListView;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 
 public class ScanActivity extends Activity implements OnItemSelectedListener {
 
 	InputStream in=null;
 
 	private String m_Text = null;
 	private String message = null;
 
 	private ExpandableListAdapter ExpAdapter;
 	private ArrayList<Equipment> ExpListItems;
 	private ExpandableListView ExpandList;
 
 	//Flag to stop onCreate() from auto populating spinners
 	private boolean spinner_flag = false;
 
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_scan);
 		
 
 		//Populate Floor Spinner
 		Spinner spinner = (Spinner) findViewById(R.id.floorSpinner);
 		populate("/Franchisee/Client/clientContract/ServiceAddress/*", spinner, "name");
 
 		spinner.setOnItemSelectedListener(this);
 
 
 
 
 		//STUFF TO DO WITH EXPANDABLE LIST
 
 		// get the listview
 		ExpandList = (ExpandableListView) findViewById(R.id.expandableEquipmentList);
 		ExpListItems = SetStandarGroups();
 		ExpAdapter = new ExpandableListAdapter(ScanActivity.this, ExpListItems);
 		ExpandList.setAdapter(ExpAdapter);
 
 
 	}
 	
 	public void onResume(){
 		super.onResume();
 		
 		Intent intent = getIntent();
 		message = intent.getStringExtra(ScanCodeDemo.EXTRA_MESSAGE);
 	}
 
 	private ArrayList<Equipment> SetStandarGroups() {
 		// TODO Auto-generated method stub
 
 		ArrayList<Equipment> list = new ArrayList<Equipment>();
 		ArrayList<inspectionElement> tempInspectionElements = new ArrayList<inspectionElement>();
 		Equipment tempEquipment;
 
 		Object temp = null;
 
 		//An xpath instance
 		XPath xpath = XPathFactory.newInstance().newXPath();
 
 		//Creates an InputStream and opens the file, then casts to InputSource
 		InputStream in=null;
 		try {
 			in = new FileInputStream(new File(Environment.getExternalStorageDirectory(),"/inspectiondata.xml"));
 			//Toast.makeText(getBaseContext(), "File read from SD card YEAH", Toast.LENGTH_LONG).show();
 		} catch (FileNotFoundException e) {
 			Toast.makeText(getBaseContext(), "Can't read inspection file from SD Card.", Toast.LENGTH_LONG).show();
 			e.printStackTrace();
 		}
 		InputSource is = new InputSource(in);
 
 		//Performs xpath and returns list of nodes
 		NodeList nodes = null;
 
 		try {
 			nodes = (NodeList) xpath.evaluate("/Franchisee/Client/clientContract/ServiceAddress/Floor[@name='First Floor']/Room[@id='R1']/*", is, XPathConstants.NODESET);
 		} catch (XPathExpressionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 
 
 		//An element node to hold the current working node
 		Element element = null;
 		Element attrElement = null;
 
 		NodeList attrNodes = null;
 
 
 		//For each piece of equipment
 		for (int i = 0; i < nodes.getLength(); i++) {
 
 			tempInspectionElements = new ArrayList<inspectionElement>();
 
 			tempEquipment = new Equipment();
 
 			//Add node attribute to string array
 			element = (Element) nodes.item(i);
 
 
 			tempEquipment.setName(element.getNodeName());
 			tempEquipment.setId(element.getAttribute("id"));
 
 
 			//Find Inspection Element Nodes
 			try {
 				attrNodes = (NodeList) xpath.evaluate("./*", element, XPathConstants.NODESET);
 			} catch (XPathExpressionException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 
 
 			//get inspection element names
 			for(int j=0; j < attrNodes.getLength(); j++){
 				attrElement = (Element) attrNodes.item(j);
 
 				//Sees what object type it needs to be
 				if(element.getNodeName().equals("Extinguisher"))
 					temp = new ExtinguisherPassFailElement();
 				else if(element.getNodeName().equals("FireHoseCabinet")){
 
 					//There's two different input options for this one
 					if(attrElement.getAttribute("name").equals("Hose Re-Rack") || attrElement.getAttribute("name").equals("Hydrostatic Test Due"))
 						temp = new FireHoseCabinetYesNoElement();
 					else
 						temp = new FireHoseCabinetGoodPoorElement(ScanActivity.this);
 				}
 				else if(element.getNodeName().equals("EmergencyLight")){
 
 					temp = new EmergencyLightYesNoElement();
 
 				}
 				else
 					temp = null;
 
 				if (temp!=null){
 					((inspectionElement) temp).setName(attrElement.getAttribute("name"));
 					tempInspectionElements.add((inspectionElement) temp);
 				}
 
 			}
 
 
 
 			tempEquipment.setItems(tempInspectionElements);
 
 			list.add(tempEquipment);
 
 
 		}
 
 
 
 		return list;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.scan, menu);
 		return true;
 	}
 
 	private void populate(String expression, Spinner spinner, String attribute){
 
 		//An array of strings to hold the names
 		ArrayList<String> options=new ArrayList<String>();
 
 		//An xpath instance
 		XPath xpath = XPathFactory.newInstance().newXPath();
 
 		//Creates an InputStream and opens the file, then casts to InputSource
 		InputStream in=null;
 		try {
 			in = new FileInputStream(new File(Environment.getExternalStorageDirectory(),"/inspectiondata.xml"));
 			//Toast.makeText(getBaseContext(), "File read from SD card YEAH", Toast.LENGTH_LONG).show();
 		} catch (FileNotFoundException e) {
 			Toast.makeText(getBaseContext(), "Can't read inspection file from SD Card.", Toast.LENGTH_LONG).show();
 			e.printStackTrace();
 		}
 		InputSource is = new InputSource(in);
 
 		//Performs xpath and returns list of nodes
 		NodeList nodes = null;
 
 		try {
 			nodes = (NodeList) xpath.evaluate(expression, is, XPathConstants.NODESET);
 		} catch (XPathExpressionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 
 		//An element node to hold the current working node
 		Element franchisee = null;
 
 		for (int i = 0; i < nodes.getLength(); i++) {
 			//Add node attribute to string array
 			franchisee = (Element) nodes.item(i);
 			options.add(franchisee.getAttribute(attribute));
 		}
 
 		//Create array adapter to change spinner
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,options);
 
 		//Sets spinner
 		spinner.setAdapter(adapter);
 
 	}
 
 	//Spinner listener
 	@Override
 	public void onItemSelected(AdapterView<?> parent, View view, int pos,
 			long id) {
 
 		Log.i("ScanActivity","OnItemSelected Triggered");
 		//Value chosen in spinner that event happened at
 		String spinnerValue = (String) parent.getItemAtPosition(pos);
 		//Stores child spinner so that child spinners can be updated in a chain reaction
 		Spinner spinner_child = null;
 
 
 			Log.i("ScanActivity", "Got into else");
 
 			//Reference to parent spinner
 			Spinner spinner = (Spinner) parent;
 			//Check to see what spinner event occured at
 			if(spinner.getId() == R.id.floorSpinner)
 			{        
 				Log.i("Main Data Entry", "floor spinner event triggered");
 				//get child spinner  
 				spinner_child = (Spinner) findViewById(R.id.roomSpinner);
 				//update child spinner data
 				populate("/Franchisee/Client/clientContract/ServiceAddress/Floor[@name='" + spinnerValue + "']/*",spinner_child,"id");
 				Log.i("Main Data Entry", "floor contract spinner updated");
 		}
 	}
 
 	@Override
 	public void onNothingSelected(AdapterView<?> arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void onScanClick(View view){
 		Log.i("ScanActivity","Scan Button Clicked");
 		
 		Intent intent = new Intent (this, ScanCodeDemo.class);
 		startActivity(intent);
 
 		Equipment temp = new Equipment();
 
		int groupPos = -1;
 
		
 
 		for(int i = 0; i < ExpListItems.size(); i++){
 			temp = ExpListItems.get(i);
 			if(temp.getId().equals(message)){
 				groupPos = i;
 				break;
 			}
 		}
 
		//if scanner returned something
		if(groupPos!=-1)
		{
			
			for(int i = 0; i < ExpListItems.size(); i++){
				ExpandList.collapseGroup(i);
			}
			
			ExpandList.expandGroup(groupPos);
			ExpandList.setSelection(groupPos);
		}
 	}
 
 	public void onManClick(View view){
 		Log.i("Scan Activity", "Manual Button Click");
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("Enter barcode:");
 
 		// Set up the input
 		final EditText input = new EditText(this);
 		// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
 		input.setInputType(InputType.TYPE_CLASS_TEXT);
 		builder.setView(input);
 
 		// Set up the buttons
 		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				m_Text = input.getText().toString();
 				Equipment temp = new Equipment();
 
 				int groupPos = 0;
 
 				for(int i = 0; i < ExpListItems.size(); i++)
 					ExpandList.collapseGroup(i);
 
 				for(int i = 0; i < ExpListItems.size(); i++){
 					temp = ExpListItems.get(i);
 					if(temp.getId().equals(m_Text)){
 						groupPos = i;
 						break;
 					}
 				}
 
 				ExpandList.expandGroup(groupPos);
 			}
 		});
 		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.cancel();
 			}
 		});
 		builder.show();
 
 	}
 
 }
