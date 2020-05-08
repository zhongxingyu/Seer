 package android.HLMPConnect.FileTransfer;
 
 import android.app.AlertDialog;
 import android.app.ExpandableListActivity;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.ExpandableListAdapter;
 import android.widget.ExpandableListView;
 import android.widget.Toast;
 import android.widget.ExpandableListView.OnChildClickListener;
 import android.widget.SimpleExpandableListAdapter;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import hlmp.CommLayer.Communication;
 import hlmp.CommLayer.NetUser;
 import hlmp.SubProtocol.FileTransfer.FileInformation;
 import hlmp.SubProtocol.FileTransfer.FileInformationList;
 
 import android.HLMPConnect.FilesActivity;
 import android.HLMPConnect.HLMPApplication;
 import android.HLMPConnect.Managers.FilesManager;
 
 
 public class CommunityFilesActivity extends ExpandableListActivity implements OnChildClickListener {
 
 	protected static final String MSG_TAG = "HLMP -> CommunityFilesActivity";
 	
 	private static final String NAME = "NAME";
 	private static final String SIZE = "SIZE";
 	private static final String USER_IP = "USER_IP";
 	private static final String ID = "ID";
 	
 	public static final int UPDATE_USERS_LIST =	0;
 
 	private Communication communication;
 	private FilesManager filesManager;
 	private Hashtable<InetAddress, FileInformationList> communityFiles;
 	private ExpandableListAdapter adapter;
 	private List<Map<String, String>> users;
 	private List<List<Map<String, String>>> files; 
 	
 	
 	private final Handler communityFilesHandler = new Handler() {
         @Override
         public synchronized void handleMessage(Message msg) {
         	if (msg.what == UPDATE_USERS_LIST) {
         		Log.d(MSG_TAG, "update users list");
         		users.clear();
                 files.clear();
                 
         		for (InetAddress userInetAddress : communityFiles.keySet()) {
         			Map<String, String> usersMap = new HashMap<String, String>();
                     users.add(usersMap);
                     
                     NetUser netUser = communication.getNetUserList().getUser(userInetAddress);
                     if (netUser == null) {
                     	continue;
                     }
                     usersMap.put(NAME, netUser.getName());
                     usersMap.put(USER_IP, userInetAddress.getHostAddress());
                     
                     List<Map<String, String>> userFilesMap = new ArrayList<Map<String, String>>();
         			for (FileInformation fileInformation : communityFiles.get(userInetAddress).toArray()) {
         				Map<String, String> fileMap = new HashMap<String, String>();
                         userFilesMap.add(fileMap);
                         fileMap.put(NAME, fileInformation.getName());
                         fileMap.put(SIZE, "" + fileInformation.getSize()/1024 + " KB");
                         fileMap.put(ID, fileInformation.getId().toString());
         			}
         			files.add(userFilesMap);
         		}
         		((BaseExpandableListAdapter) adapter).notifyDataSetChanged();
         	}
         }
     };
 	
     
     
     
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		HLMPApplication application = (HLMPApplication)getApplicationContext();
 		this.communication = application.getCommunication();
         this.filesManager = application.getFilesManager();
         this.communityFiles = filesManager.getCommunityFiles();
         this.filesManager.setCommunityFilesHandler(communityFilesHandler);
         
         this.users = new ArrayList<Map<String, String>>();
         this.files = new ArrayList<List<Map<String, String>>>();
         
         this.adapter = new SimpleExpandableListAdapter(
 				this,
 				this.users,
 				android.R.layout.simple_expandable_list_item_1,
 				new String[] { NAME, USER_IP },
 				new int[] {android.R.id.text1, android.R.id.text2},
 				files,
 				android.R.layout.simple_expandable_list_item_2,
 				new String[] { NAME, SIZE, ID },
 				new int[] {android.R.id.text1, android.R.id.text2, android.R.id.addToDictionary});
 		this.setListAdapter(adapter);
 		this.getExpandableListView().setOnChildClickListener(this);
     }
 	
 	@Override
 	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
 		Map<String, String> usersMap = users.get(groupPosition);
 		Log.d(MSG_TAG, usersMap.get(NAME));
 		Log.d(MSG_TAG, usersMap.get(USER_IP));
 		
 		List<Map<String, String>> filesList = files.get(groupPosition);
 		Map<String, String> fileMap = filesList.get(childPosition);
 		Log.d(MSG_TAG, fileMap.get(NAME));
 		
 		
 		UUID fileInformationId = UUID.fromString(fileMap.get(ID));
 		InetAddress userInetAddress;
 		try {
 			userInetAddress = InetAddress.getByName(usersMap.get(USER_IP));
 		} catch (UnknownHostException e) {
 			return false;
 		}
 		
 		final FilesActivity filesActivity = this.filesManager.getFilesActivity();
 		final NetUser netUser = communication.getNetUserList().getUser(userInetAddress);
 		FileInformationList fileInformationlist = communityFiles.get(userInetAddress);
 		final FileInformation fileInformation = fileInformationlist.getFileInformation(fileInformationId);
 		final FilesManager filesManager_final = this.filesManager;
 		
 		AlertDialog.Builder builder = new AlertDialog.Builder(filesActivity);
 	    builder.setMessage("Are you sure you want to download " + fileInformation.getName() + " ?")
         .setCancelable(false)
         .setPositiveButton("Download", new OnClickListener() {
 			public void onClick(DialogInterface dialog, int arg1) {
         		filesManager_final.sendFileRequest(netUser, fileInformation);
         		Toast.makeText(filesActivity, "Downloading " + fileInformation.getName(), Toast.LENGTH_SHORT).show();
 			}
 		})
         .setNegativeButton("Cancel", null);
 	    
 	    AlertDialog alert = builder.create();
         alert.show();
 		
 	    return true;
     }
 }
