 package com.eps_hioa_2013.JointAttentionResearchApp;
 
 import java.io.File;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.Menu;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
 	public final static String EXTRA_SESSION = "com.eps_hioa_2013.JointAttentionResearchApp.EXTRA_SESSION";
 
 	private Session mysession;
 
 	private List<Element> elementlist; //all Elements are in here
 	//valid extensions
	private String[] videoExtensions  = {"wav", "vlc", "gif", "wmv", "mp4"};
 	private String[] soundExtensions  = {"mp3"};
 	private String[] pictureExtensions  = {"jpg", "png", "gif"};
 	
 	Bundle bundle;
 	
 	//Just activates the first View
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		System.out.println("MainActivity started");
 		
 		//set full screen
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		
 	    //connect the .java to the .xml make sure you set fullscreen first in code else it will crash
 		setContentView(R.layout.activity_main);
 		
 		//hides keyboard until user presses a field
 		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);	
 		
 		
 		checkExistingDirs();
 		
 	}
 	
 	
 
 	//Dont know yet
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	//creates new Intent and saves the Strings were typed in into a new Session-Object called mysession
 	//then passes mysession over to ModuleActivity
 	public void onclick_login(View view) {
         // Do something in response to button
     	Intent intent = new Intent(this, ModuleActivity.class);
     	
     	EditText editText3 = (EditText) findViewById(R.id.password);
     	String string_password = editText3.getText().toString();
     	
     	EditText editText = (EditText) findViewById(R.id.participant);
     	String string_participant = editText.getText().toString();
     	
     	EditText editText2 = (EditText) findViewById(R.id.researcher);
     	String string_researcher = editText2.getText().toString();
 
 
     	//to not get annoyed everytime you start the programm to test it
     	if(((string_password == null) || (string_password.equals("")))
     	|| ((string_participant == null) || (string_participant.equals("")))
     	|| ((string_researcher == null) || (string_researcher.equals(""))))
     	{
     		//shows error if one field is empty
     		Toast.makeText(getApplicationContext(), "One of the fields is empty", Toast.LENGTH_SHORT).show();
     	}
     	else
     	{
 
     		//mysession gets created, serialized, packed in the bundle and then sent to the next activity
     		mysession = new Session(string_password, string_participant, string_researcher);
     		elementlist = LoadElements();
     		mysession.setElementlist(elementlist);
     		bundle = new Bundle();    		
     		bundle.putSerializable(EXTRA_SESSION, (Serializable) mysession);
         	intent.putExtras(bundle);
         	
 
         	//following two lines: The password gets checked if it is correct        	
         	if(checkpassword(string_password) == true) startActivity(intent);
         	else Toast.makeText(getApplicationContext(), "Invalid password", Toast.LENGTH_SHORT).show();
     	}
     	
     }
 	
 	public Boolean checkpassword(String password)
 	{
 		if(password.equals("admin")) return true;
 		else return false;
 	}
 	
 	public List<Element> LoadElements()
 	{
 		//Start values
 		ArrayList<Element> elementList = new ArrayList<Element>();		
 		int size = 0;
 		String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() +"/Jara/Elements/";
 		
 		//Search for all files in dirPath and add their paths to a String array.
 		List<String> paths = new ArrayList<String>();
 		File directory = new File(dirPath);
 		File[] files = directory.listFiles();		
 		size = files.length;
 		for (int i = 0; i < size; ++i) {
 		    paths.add(files[i].getAbsolutePath());		    
 		}
 		
 		//Create elements and put hem into the arraylist
 		for (int i = 0; i < paths.size(); ++i)
 		{
 			Element element = createElement(paths.get(i));
 			if(element!=null)
 			{
 				elementList.add(element);
 			}
 		}
 		
 		return elementList;
 	}
 	
 	
 	private Element createElement(String elementPath) {
 		int type = checkFileType(elementPath);
 		Element createdElement = null;
 		 switch (type) 
 		{
 			case 0:  //nothing happens file type is 'unknown'/unsupported
 			break;
 			case 1:  
 				createdElement = new ElementPicture(elementPath, type);
 			break;
 			case 2:  
 				createdElement = new ElementSound(elementPath, type);
 			break;				
 			case 3:
 				createdElement = new ElementVideo(elementPath, type);
 			break;
 		}
 		return createdElement;
 	}
 
 	//returns number based on the file type.
 	// 0 = unknown, 1 = picture, 2 = sound, 3 = video;
 	//wanted to use a switch but not all java versions support switches with strings
 	private int checkFileType(String path)
 	{		
 		int type = 0;
 		String extension = path.substring(path.lastIndexOf(".")+1);
 		if(Arrays.asList(videoExtensions).contains(extension))
 		{
 			type = 3;
 		}
 		else if (Arrays.asList(soundExtensions).contains(extension))
 		{
 			type = 2;
 		}
 		else if (Arrays.asList(pictureExtensions).contains(extension))
 		{
 			type = 1;
 		}				
 		return type;
 	}
 	
 	private void checkExistingDirs() {
 		//Create Java directory if it doesn't exist
 				String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath();
 				File f = new File(dirPath+"/Jara");
 				File g = new File(dirPath+"/Jara/Statistics");
 				File h = new File(dirPath+"/Jara/Elements");
 				if (!f.isDirectory())
 				{
 					f.mkdirs();
 				}
 				if (!g.isDirectory())
 				{
 					g.mkdirs();
 				}
 				if (!h.isDirectory())
 				{
 					h.mkdirs();
 				}
 	}
 	
 
 }
