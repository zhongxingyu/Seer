 package jieehd.villain.updater;
 
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AbsListView;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class getfiles extends ListActivity {
 	
  
 
 private List<String> fileList = new ArrayList<String>();
 private List<String> pathList = new ArrayList<String>();
 
 private void ListDir(File f){
     File[] files = f.listFiles();
     fileList.clear();
     pathList.clear();
     for (File file : files){
      fileList.add(file.getPath().substring(28));
      pathList.add(file.getPath());
      final ArrayAdapter<String> directoryList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, fileList);
      setListAdapter(directoryList);
     }
 };
 
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ArrayAdapter<String> directoryList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, fileList);
        setListAdapter(directoryList);
        final String PATH = "/VillainROM/ROMs";
        final File SDDIR = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + PATH);
        ListDir(SDDIR);
        this.getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
     	   public boolean onItemLongClick(AdapterView<?> parent, View view, final int pos, long id) {
     	       AlertDialog.Builder builder = new AlertDialog.Builder(getfiles.this);
     	       builder.setTitle("Options");
     	       builder.setItems(new String []{"Delete", "Re-name", "Install"}, new DialogInterface.OnClickListener() {
     	    	   public void onClick (DialogInterface dialog) {
     	    	           dialog.dismiss();
 
     	    	   }
 
 
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					   dialog.dismiss();
 					   String Path = pathList.get(pos);
 	    	           switch (which) {
 	    	           case 0:
 	    	        	  final  File file = new File(Path);
 	    	        	   boolean deleted = file.delete();
 	    	        	   setListAdapter(directoryList);
 	        	           ListDir(SDDIR);
 	        	           directoryList.notifyDataSetChanged();
 	    	        	   break;
 	    	           case 1:
 	    	        	    AlertDialog.Builder alert = new AlertDialog.Builder(getfiles.this);                 
 	    	        	    alert.setTitle("Rename File");  
 	    	        	    alert.setMessage("Enter new file name");                
 
 	    	        	     // Set an EditText view to get user input   
 	    	        	     final EditText input = new EditText(getfiles.this); 
 	    	        	     alert.setView(input);
 
 	    	        	        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  
 	    	        	        public void onClick(DialogInterface dialog, int whichButton) {  
 	    	        	            String value = input.getText().toString();
 	    	        	            String Path = pathList.get(pos);
 	    	        	            String Name = fileList.get(pos);
 	    	        	            File SDPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + PATH);
 	    	        	            File from = new File(SDPath, Name);
 	    	        	            File to = new File(SDPath, value + ".zip");
 	    	        	            from.renameTo(to);
 	    		    	        	setListAdapter(directoryList);
 	    		        	        ListDir(SDPath);
 	    		        	        directoryList.notifyDataSetChanged();
 	    	        	            return; 
 	    	        	           }  
 	    	        	         });  
 
 	    	        	        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 
 	    	        	            public void onClick(DialogInterface dialog, int which) {
 	    	        	                // TODO Auto-generated method stub
 	    	        	                return;   
 	    	        	            }
 	    	        	        });
 	    	        	        alert.show();
 	    	        	        break;
 	    	        	   
 	    	           case 2:
	    	           	
 	    	           	break;
 	    	        	   
 	    	           }
 				}
     	    	 
     	    	});
     	       	           
     	   
     	    
     	        builder.setCancelable(true);
     	        builder.create().show();
     	        return true;
     	   }
        });
        
        
    }
    
 
  
 }
