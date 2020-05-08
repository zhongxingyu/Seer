 /* ========================================================================= *
  * Boarder                                                                   *
  * http://boarder.mikuz.org/                                                 *
  * ========================================================================= *
  * Copyright (C) 2013 Boarder                                                *
  *                                                                           *
  * Licensed under the Apache License, Version 2.0 (the "License");           *
  * you may not use this file except in compliance with the License.          *
  * You may obtain a copy of the License at                                   *
  *                                                                           *
  *     http://www.apache.org/licenses/LICENSE-2.0                            *
  *                                                                           *
  * Unless required by applicable law or agreed to in writing, software       *
  * distributed under the License is distributed on an "AS IS" BASIS,         *
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *
  * See the License for the specific language governing permissions and       *
  * limitations under the License.                                            *
  * ========================================================================= */
 
 package fi.mikuz.boarder.gui;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.TreeMap;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.thoughtworks.xstream.XStream;
 
 import fi.mikuz.boarder.R;
 import fi.mikuz.boarder.app.BoarderListActivity;
 import fi.mikuz.boarder.component.soundboard.GraphicalSound;
 import fi.mikuz.boarder.util.ImageDrawing;
 import fi.mikuz.boarder.util.XStreamUtil;
 
 public class FileExplorer extends BoarderListActivity {
 	
 	public static final String ACTION_SELECT_BACKGROUND_FILE = "selectBackgroundFile";
 	public static final String ACTION_SELECT_SOUND_IMAGE_FILE = "selectSoundImageFile";
 	public static final String ACTION_SELECT_SOUND_ACTIVE_IMAGE_FILE = "selectSoundActiveImageFile";
 	public static final String ACTION_CHANGE_SOUND_PATH = "changeSoundPath";
 	public static final String ACTION_ADD_GRAPHICAL_SOUND = "addGraphicalSound";
 	
 	public static final String EXTRA_ACTION_KEY = "actionKey";
 	public static final String EXTRA_BOARD_NAME_KEY = "boardNameKey";
 	
	public static final String CURRENT_DIRECTORY_KEY = "currentDirectoryKey";
  
 	private List<String> mPath = null;
	private File mCurrentDir = null;
 	private boolean mSdDir = false;
 	private String mSdcard = Environment.getExternalStorageDirectory().toString();
 	private String mBoardPath = mSdcard;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);  
 		setContentView(R.layout.file_browser);
 		mBoardPath = SoundboardMenu.mSbDir.getPath() + "/" + getIntent().getExtras().getString(FileExplorer.EXTRA_BOARD_NAME_KEY);
 		
 		String initDir = null;
 		try {
			initDir = savedInstanceState.getString(CURRENT_DIRECTORY_KEY);
 		} catch (NullPointerException e) {
 			initDir = mSdcard; // Use default init dir
 		}
 		
 		getDir(initDir);
 	}
 	
 	@Override
     protected void onSaveInstanceState(Bundle outState) {
     	super.onSaveInstanceState(outState);
    	outState.putString(CURRENT_DIRECTORY_KEY, mCurrentDir.getAbsolutePath());
 	}
 
 	@Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         if ((keyCode == KeyEvent.KEYCODE_BACK)) {
             if(mSdDir) {
             	return super.onKeyDown(keyCode, event);
             } else {
             	getDir(mPath.get(1));
             }
             return false;
         } else {
         	return super.onKeyDown(keyCode, event);
         }
     }
     
     class ComparatorTest implements Comparator<Object> {  
     	public int compare (Object o1, Object o2) {
     		String s1 = (String) o1; 
     	    String s2 = (String) o2; 
     	    return s1.toUpperCase().compareTo(s2.toUpperCase()); 
     	}                                      
     	public boolean equals(Object o) {
     	    return compare(this, o) == 0; 
     	}  
     }
     
     private void getDir(String dirPath) {
      setTitle(this.getString(R.string.select_item) + " - " + dirPath);
      
     final List<String> mItem = new ArrayList<String>();
      mPath = new ArrayList<String>();
      
     mCurrentDir = new File(dirPath);
     File[] files = mCurrentDir.listFiles();
      
      if (dirPath.equals(mSdcard)) {
     	 mItem.add("Board dir");
     	 mPath.add(mBoardPath);
     	 mSdDir = true;
      } else {
 	     mItem.add("SD card");
 	     mPath.add(mSdcard);
 	     mItem.add("../");
    	 mPath.add(mCurrentDir.getParent()); 
     	 mSdDir = false;
      }
      
      mItem.add("Pause function");
 	 mPath.add(SoundboardMenu.mPauseSoundFilePath);
      
      Map<String,String> fileMap = new TreeMap<String,String>(new ComparatorTest());
      Map<String,String> dirMap = new TreeMap<String,String>(new ComparatorTest());
 
      for(int i=0; i < files.length; i++) {
     	 File file = files[i];
        if(file.isDirectory()) {
     	   dirMap.put(file.getName() + "/", file.getPath());
        } else {
     	   fileMap.put(file.getName(), file.getPath());
        }
      }
      
      Set<Entry<String, String>> sortedFiles = fileMap.entrySet();
      Set<Entry<String, String>> sortedDirs = dirMap.entrySet();
 
      Iterator<Entry<String, String>> dirIt = sortedDirs.iterator();
      while (dirIt.hasNext()) {
     	 Entry<String, String> dir = dirIt.next();
     	 mPath.add(dir.getValue());
     	 mItem.add(dir.getKey());
      }
      
      Iterator<Entry<String, String>> fileIt = sortedFiles.iterator();
      while (fileIt.hasNext()) {
     	 Entry<String, String> file = fileIt.next();
     	 mPath.add(file.getValue());
     	 mItem.add(file.getKey());
      }
 
      ArrayAdapter<String> fileList =
     	 new ArrayAdapter<String>(this, R.layout.file_browser_row, mItem);
      setListAdapter(fileList);
     }
 
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) {
   
    	final File selectedFile = new File(mPath.get(position));
   
     	if (selectedFile.isDirectory()) {
     		if(selectedFile.canRead())
     			getDir(mPath.get(position));
     		else {
 				Toast msg = Toast.makeText(this, "Can't read " + selectedFile.getName(), Toast.LENGTH_LONG);
 				msg.show();
     		}
     	} else {
     		final String action = getIntent().getExtras().getString(FileExplorer.EXTRA_ACTION_KEY);
     		
     		if (action.equals(FileExplorer.ACTION_SELECT_BACKGROUND_FILE)) {
     			try {
     				new Canvas().drawBitmap(ImageDrawing.decodeFile(null, selectedFile), 0, 0, null);
     				
         			Bundle bundle = new Bundle();
         			bundle.putString(FileExplorer.ACTION_SELECT_BACKGROUND_FILE, selectedFile.getAbsoluteFile().toString());
         			
         			Intent intent = new Intent();
     				intent.putExtras(bundle);
     				
     				setResult(RESULT_OK, intent);
     				finish();
     			} catch (NullPointerException e) {
 					Toast msg = Toast.makeText(this, "Can't draw " + selectedFile.getName(), Toast.LENGTH_LONG);
 					msg.show();
 				}
 				
     		} else if (action.equals(FileExplorer.ACTION_SELECT_SOUND_IMAGE_FILE)) {
     			try {
     				new Canvas().drawBitmap(ImageDrawing.decodeFile(null, selectedFile), 0, 0, null);
     				
 	    			Bundle bundle = new Bundle();
 	    			bundle.putString(FileExplorer.ACTION_SELECT_SOUND_IMAGE_FILE, selectedFile.getAbsoluteFile().toString());
 	    			
 	    			Intent intent = new Intent();
 					intent.putExtras(bundle);
 					
 					setResult(RESULT_OK, intent);
 					finish();
     			} catch (NullPointerException e) {
 					Toast msg = Toast.makeText(this, "Can't draw " + selectedFile.getName(), Toast.LENGTH_LONG);
 					msg.show();
 				}
     			
     		} else if (action.equals(FileExplorer.ACTION_SELECT_SOUND_ACTIVE_IMAGE_FILE)) {
     			try {
     				new Canvas().drawBitmap(BitmapFactory.decodeFile(selectedFile.getAbsolutePath()), 0, 0, null);
     				
 	    			Bundle bundle = new Bundle();
 	    			bundle.putString(FileExplorer.ACTION_SELECT_SOUND_ACTIVE_IMAGE_FILE, selectedFile.getAbsoluteFile().toString());
 	    			
 	    			Intent intent = new Intent();
 					intent.putExtras(bundle);
 					
 					setResult(RESULT_OK, intent);
 					finish();
     			} catch (NullPointerException e) {
 					Toast msg = Toast.makeText(this, "Can't draw " + selectedFile.getName(), Toast.LENGTH_LONG);
 					msg.show();
 				}
 				
     		} else if (action.equals(FileExplorer.ACTION_CHANGE_SOUND_PATH)) {
     			Bundle bundle = new Bundle();
     			bundle.putString(FileExplorer.ACTION_CHANGE_SOUND_PATH, selectedFile.getAbsoluteFile().toString());
     			
     			Intent intent = new Intent();
 				intent.putExtras(bundle);
 				
 				setResult(RESULT_OK, intent);
 				finish();
 				
     		} else {
     		
 	    		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 	
 	    		alert.setTitle("Set name for sound");
 	    		
 	    		if (selectedFile.getAbsolutePath().toString().equals(SoundboardMenu.mPauseSoundFilePath)) { 
 	    			alert.setMessage("Pauses or resumes playing sounds when activated.");
 	    		} else {
 	    			alert.setMessage(selectedFile.getAbsolutePath().toString());
 	    		}
 	
 	    		final EditText input = new EditText(this);
 	    		alert.setView(input);
 		  
 	    		String inputText = (selectedFile.getName().contains(".")) ? 
 				selectedFile.getName().subSequence(0, selectedFile.getName().indexOf(".")).toString() :
 				selectedFile.getName();
 		  
 				input.setText(inputText);
 	
 				alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 						
 						Bundle bundle = new Bundle();
 				  
 						if (action.equals(FileExplorer.ACTION_ADD_GRAPHICAL_SOUND)) {
 							GraphicalSound sound = new GraphicalSound();
 							sound.setName(input.getText().toString());
 							sound.setPath(selectedFile.getAbsoluteFile());
 							sound.setNameFrameX(50);
 							sound.setNameFrameY(50);
 							Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.sound);
 							sound.setImageWidthHeight(getApplicationContext(), image.getHeight(), image.getHeight());
 					  
 							XStream xstream = XStreamUtil.graphicalBoardXStream();
 							bundle.putString(FileExplorer.ACTION_ADD_GRAPHICAL_SOUND, xstream.toXML(sound));
 						}
 				  
 						Intent intent = new Intent();
 						intent.putExtras(bundle);
 						
 						setResult(RESULT_OK, intent);
 						finish();
 						
 					}
 				});
 	
 				alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 					}
 				});
 	
 				alert.show();
 			
     		}
     	}
     }
 }
