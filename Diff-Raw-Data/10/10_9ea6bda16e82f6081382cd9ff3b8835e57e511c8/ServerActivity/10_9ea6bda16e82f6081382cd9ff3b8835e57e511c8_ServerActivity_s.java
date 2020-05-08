 package com.cs456.client;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.cs456.project.common.FileListManager;
 import com.cs456.project.common.FileListObject;
 import com.cs456.project.exceptions.AuthenticationException;
 import com.cs456.project.exceptions.DeletionDelayedException;
 import com.cs456.project.exceptions.DisconnectionException;
 import com.cs456.project.exceptions.OutOfDateException;
 import com.cs456.project.exceptions.RequestExecutionException;
 import com.cs456.project.exceptions.RequestPermissionsException;
 
 public class ServerActivity extends ListActivity {
     EditText currLoc;
     EditText dlLoc;
     ListView lview;
     String username;
     CheckBox share;
     CC cc;
 
     private ServerActivity This = this;
     // ProgressDialog pdialog;
     private Handler handle;
     FileListManager mlist;
     Track track = Track.getInstance();
     private List<String> item = null;
     //private List<String> path = null;
     private List<FileListObject> flos = null;
     private TextView myPath;
     private String fullPath;
     private File sdCardRoot = Environment.getExternalStorageDirectory();
     private AlertDialog options;
 
     public void onCreate(Bundle savedInstanceState) {
 	super.onCreate(savedInstanceState);
 	setContentView(R.layout.filelist_server);
 	handle = new Handler();
 	myPath = (TextView) findViewById(R.id.path_server);
 	track = Track.getInstance();
 	track.setContext(this);
 	cc = CC.getInstance();
 	username = cc.getCC().getUsername();
 	item = new ArrayList<String>();
 	//path = new ArrayList<String>();
 	fullPath = "";
 	myPath.setText("Currently grabbing files...");
 	getDir("");
     }
 
     private void getDir(final String dirPath) {
 	new Thread(new Runnable() {
 	    @Override
 	    public void run() {
 		boolean result = false;
 		String msg = "";
 		item = new ArrayList<String>();
 		//path = new ArrayList<String>();
 		try {
 		    mlist = cc.getCC().getFileList(dirPath);
 		    result = true;
 		} catch (AuthenticationException e) {
 		    msg = e.getMessage();
 		} catch (RequestPermissionsException e) {
 		    msg = e.getMessage();
 		} catch (RequestExecutionException e) {
 		    msg = e.getMessage();
 		} catch (DisconnectionException e) {
 		    msg = e.getMessage();
 		}
 		if (!result) {
 		    handleError(msg);
 		    handle.post(new Runnable() {
 			@Override
 			public void run() {
 			    myPath.setText("Failed to load");
 			}
 		    });
 		    return;
 		}
 		handle.post(new Runnable() {
 		    @Override
 		    public void run() {
 			myPath.setText("Location: " + dirPath.trim());
 		    }
 		});
 		int i = 0;
 		//path = new ArrayList<String>();
 		flos = mlist.getAll(true);
 		//fullPath = dirPath.trim();
 		
 		if (!dirPath.trim().equals("")) {
 		    item.add("../");
 		    FileListObject obj = new FileListObject(fullPath + "\\",
 			    fullPath, false, false);
 		    obj.setDisplayName("../");
 		    flos.add(0, obj);
 		    i++;
 		}
 		
 		// starting at 1 to avoid repeating the flos problem
 		for (; i < flos.size(); i++) {
 		    FileListObject obj = flos.get(i);
 		    if (obj.isDirectory())
 			item.add(obj.getDisplayName() + "/");
 		    else
 			item.add(obj.getDisplayName());
 		}
 
 		final ArrayAdapter<String> fileList = new ArrayAdapter<String>(
 			This, R.layout.rowlayout, item);
 		handle.post(new Runnable() {
 
 		    @Override
 		    public void run() {
 			This.setListAdapter(fileList);
 		    }
 		});
 	    }
 	}).start();
     }
 
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) {
 	FileListObject flo = flos.get(position);
 
 	if (flo.isDirectory()) {
 	    if (position == 0) {
 		if (!fullPath.equals("")) {
 		    fullPath = upDirectory(fullPath);
 		} else 
 		    fullPath = downDirectory(fullPath, flo.getDisplayName());
 	    }
 	    else 
 		fullPath = downDirectory(fullPath, flo.getDisplayName());
 	    getDir(fullPath);
 	} else {
 	    alertOptions(flo);
 	}
     }
 
     /***
      * Getting the option to download/change sharing and delete
      * 
      * @param flo
      */
     private void alertOptions(final FileListObject flo) {
 	LayoutInflater factory = LayoutInflater.from(this);
 	final View textEntryView = factory
 		.inflate(R.layout.server_option, null);
 	AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
 	TextView tv = (TextView) textEntryView.findViewById(R.id.options_file);
 	tv.setText("File: " + flo.getDisplayName());
 	ImageButton dlBtn = (ImageButton) textEntryView
 		.findViewById(R.id.server_downloadBtn);
 	dlBtn.setOnClickListener(new OnClickListener() {
 	    @Override
 	    public void onClick(View v) {
 		onDownload(fullPath +"\\"+ flo.getDisplayName());
 	    }
 	});
 
 	ImageButton deleteBtn = (ImageButton) textEntryView
 		.findViewById(R.id.server_deleteBtn);
 	deleteBtn.setOnClickListener(new OnClickListener() {
 	    @Override
 	    public void onClick(View v) {
 		onDelete(parseOutUserName(fullPath+"\\"+flo.getDisplayName()));
 	    }
 	});
 	ImageButton shareBtn = (ImageButton) textEntryView
 		.findViewById(R.id.server_propertiesBtn);
 	shareBtn.setOnClickListener(new OnClickListener() {
 
 	    @Override
 	    public void onClick(View v) {
 		alertSettingBox(
 			parseOutUserName(fullPath+"\\"+flo.getDisplayName()),
 			flo.isShared());
 	    }
 	});
 
 	if (flo.isDeleteOnly()) {
 	    dlBtn.setEnabled(false);
 	    shareBtn.setEnabled(false);
 	}
 	if (!flo.isMyFile()) {
 	    deleteBtn.setEnabled(false);
 	    shareBtn.setEnabled(false);
 	}
 	alertbox.setView(textEntryView);
 	options = alertbox.create();
 	options.setTitle("Server Options");
 	options.show();
     }
 
     /***
      * 
      * @param selected
      */
     private void alertSettingBox(final String obj, boolean isShared) {
 	LayoutInflater factory = LayoutInflater.from(this);
 	final View textEntryView = factory.inflate(R.layout.share_dialog, null);
 	AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
 	currLoc = (EditText) textEntryView.findViewById(R.id.share_file);
 	currLoc.setText(obj);
 	alertbox.setView(textEntryView);
 	share = (CheckBox) textEntryView.findViewById(R.id.share_chkbox);
 	share.setChecked(isShared);
 	alertbox.setPositiveButton("Save",
 		new DialogInterface.OnClickListener() {
 		    @Override
 		    public void onClick(final DialogInterface dialog, int which) {
 			new Thread(new Runnable() {
 			    @Override
 			    public void run() {
 				String msg = "";
 				boolean result = false;
 				try {
 				    cc.getCC().requestPermissionsChange(
 					    currLoc.getText().toString(),
 					    share.isChecked());
 				    result = true;
 				} catch (AuthenticationException e) {
 				    msg = e.getMessage();
 				} catch (RequestPermissionsException e) {
 				    msg = e.getMessage();
 				} catch (RequestExecutionException e) {
 				    msg = e.getMessage();
 				} catch (DisconnectionException e) {
 				    msg = e.getMessage();
 				}
 
 				if (!result) {
 				    handleError(msg);
 				    return;
 				}
 				dialog.dismiss();
 				handle.post(new Runnable() {
 				    @Override
 				    public void run() {
 					Toast.makeText(track.getContext(),
 						"Changed Sharing Settings", 5)
 						.show();
 				    }
 				});
 				options.dismiss();
 				getDir(fullPath);
 			    }
 			}).start();
 		    }
 		});
 
 	alertbox.setNegativeButton("Cancel",
 		new DialogInterface.OnClickListener() {
 		    @Override
 		    public void onClick(DialogInterface dialog, int which) {
 			dialog.cancel();
 		    }
 		});
 	AlertDialog alert = alertbox.create();
 	alert.setTitle("Share file:" + parseFileString(obj));
 	alert.show();
     }
 
     protected void handleError(final String error) {
 	handle.post(new Runnable() {
 
 	    @Override
 	    public void run() {
 		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(This);
 		alertBuilder.setMessage(error);
 		alertBuilder.setCancelable(false);
 
 		alertBuilder.setNeutralButton("OK",
 			new DialogInterface.OnClickListener() {
 			    public void onClick(DialogInterface dialog, int id) {
 				dialog.cancel();
 			    }
 			});
 
 		AlertDialog alert = alertBuilder.create();
 		alert.setTitle("Error!");
 		alert.show();
 	    }
 	});
     }
     
     private String parseOutFirstSlash(String val) {
 	if ('\\'== val.charAt(0)){
 	    val = val.substring(1);
 	}
 	
 	return val;
     }
 
     private String parseOutSlash(String val) {
 	int i = val.lastIndexOf("\\");
 	if (i == -1)
 	    i = 0;
 	return val.substring(0, i);
     }
 
     private String parseFileString(String val) {
 	int i = val.lastIndexOf("\\");
 	if (i == -1)
 	    i = -1;
 	return val.substring(i + 1);
     }
 
     private String parseOutUserName(String val) {
 	String tmp = val;
 	int i = tmp.indexOf("\\");
 	if (i == -1) {
 	    i = 0;
 	}
 	return tmp.substring(i+1);
     }
 
     private String parseUserName(String val) {
 	String tmp = val;
 	int i = tmp.indexOf("\\");
 	if (i == -1) {
 	    i = 0;
 	}
 	return tmp.substring(0, i);
     }
     
     private String downDirectory(String oldDirectoryPath, String newDirectory) {
 	String newDir = "";
 	if(!oldDirectoryPath.equals("")) {
 	    newDir += oldDirectoryPath + "\\";
 	}
 	newDir += newDirectory;
 	
 	return newDir;
     }
     
     private String upDirectory(String oldDirectoryPath) {
 	String newDir = "";
 	int lastIndex = oldDirectoryPath.lastIndexOf("\\");
 	
 	if(lastIndex != -1) {
 	    newDir = oldDirectoryPath.substring(0, lastIndex);
 	}
 			
 	return newDir;
     }
 
     /******* onClick Handlers ***********/
 
     public void onDownload(final String flo) {
 	LayoutInflater factory = LayoutInflater.from(this);
 	final View textEntryView = factory.inflate(R.layout.download_to_client,
 		null);
 	AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
 	dlLoc = (EditText) textEntryView.findViewById(R.id.download_to_loc);
 	dlLoc.setText(sdCardRoot.getAbsolutePath().toString().trim() + "/");
 	alertbox.setTitle("Download " + parseFileString(flo));
 	alertbox.setView(textEntryView);
 	alertbox.setPositiveButton("Save",
 		new DialogInterface.OnClickListener() {
 		    @Override
 		    public void onClick(DialogInterface dialog, int which) {
 			new Thread(new Runnable() {
 			    @Override
 			    public void run() {
 				String msg = "";
 				boolean result = false;
 				try {
				    // How does this work?
 				    cc.getCC().requestFileDownload(
 					    dlLoc.getText().toString(),
 					    parseOutUserName(flo),
 					    parseUserName(flo));
 				    result = true;
 				} catch (AuthenticationException e) {
 				    msg = e.getMessage();
 				} catch (RequestPermissionsException e) {
 				    msg = e.getMessage();
 				} catch (RequestExecutionException e) {
 				    msg = e.getMessage();
 				} catch (DisconnectionException e) {
 				    msg = e.getMessage();
 				} catch (OutOfDateException e) {
 				    handle.post(new Runnable() {
 					@Override
 					public void run() {
 					    // TODO Auto-generated method stub
 					    yesnoDialog(dlLoc.getText()
 						    .toString(),
 						    parseOutUserName(flo),
 						    parseUserName(flo));
 					}
 				    });
 				    return;
 				}
 
 				if (!result) {
 				    handleError(msg);
 				    return;
 				}
 				handle.post(new ParamRunnable(flo) {
 
 				    @Override
 				    public void run() {
 					Toast.makeText(
 						This,
 						"Done downloading "
 							+ parseFileString(flo),
 						5).show();
 				    }
 				});
 				options.dismiss();
 			    }
 			}).start();
 		    }
 		});
 
 	alertbox.show();
     }
 
     private void yesnoDialog(final String sdCardLoc, final String serverLoc,
 	    final String owner) {
 	AlertDialog.Builder alertBuilder = new AlertDialog.Builder(This);
 	alertBuilder.setCancelable(false);
 	alertBuilder
 		.setMessage("The partial file currently downloaded, is out of date, do you wish to delete and try again?");
 	alertBuilder.setPositiveButton("Yes",
 		new DialogInterface.OnClickListener() {
 
 		    @Override
 		    public void onClick(DialogInterface dialog, int which) {
 			new Thread(new Runnable() {
 
 			    @Override
 			    public void run() {
 				boolean result = false;
 				String msg = "";
 				handle.post(new ParamRunnable(sdCardLoc) {
 				    @Override
 				    public void run() {
 					Toast.makeText(track.getContext(),
 						"Start delete of " + param, 5).show();
 				    }
 				});
 				File f = new File(sdCardLoc+".part");
 				result = f.delete();
 				if (!result) {
 				    handleError("Error deleting on SD Card");
 				    return;
 				}
 
 				result = false;
 				handle.post(new ParamRunnable(sdCardLoc) {
 				    @Override
 				    public void run() {
 					Toast.makeText(
 						track.getContext(),
 						"Delete "
 							+ param
 							+ " completed.. Starting download",
 						5).show();
 				    }
 				});
 				try {
 				    cc.getCC().requestFileDownload(sdCardLoc, serverLoc, owner);
 				    result = true;
 				} catch (DisconnectionException e) {
 				    msg = e.getMessage();
 				} catch (AuthenticationException e) {
 				    msg = e.getMessage();
 				} catch (RequestExecutionException e) {
 				    msg = e.getMessage();
 				} catch (RequestPermissionsException e) {
 				    msg = e.getMessage();
 				} catch (OutOfDateException e) {
 				    msg = e.getMessage();
 				}
 				if (!result) {
 				    handleError(msg);
 				    return;
 				}
 
 				handle.post(new ParamRunnable(sdCardLoc) {
 				    @Override
 				    public void run() {
 					Toast.makeText(track.getContext(),
 						"Download of " + param
 							+ " completed", 5).show();
 				    }
 				});
 				options.dismiss();
 			    }
 			}).start();
 
 		    }
 		});
 	alertBuilder.setNegativeButton("No",
 		new DialogInterface.OnClickListener() {
 		    public void onClick(DialogInterface dialog, int id) {
 			dialog.cancel();
 		    }
 		});
 
 	AlertDialog alert = alertBuilder.create();
 	alert.setTitle("Continue Downloading...");
 	alert.show();
     }
 
     public void onDelete(final String flo) {
 	AlertDialog.Builder builder = new AlertDialog.Builder(this);
 	builder.setMessage(
 		"Are you sure you want to delete file " + parseFileString(flo)
 			+ "?")
 		.setCancelable(false)
 		.setPositiveButton("Yes",
 			new DialogInterface.OnClickListener() {
 			    public void onClick(DialogInterface dialog, int id) {
 				new Thread(new Runnable() {
 				    @Override
 				    public void run() {
 					String msg = "";
 					boolean result = false;
 					final String tmp = flo;
 					try {
 					    cc.getCC().requestFileDeletion(tmp);
 					    result = true;
 					} catch (AuthenticationException e) {
 					    msg = e.getMessage();
 					} catch (RequestPermissionsException e) {
 					    msg = e.getMessage();
 					} catch (RequestExecutionException e) {
 					    msg = e.getMessage();
 					} catch (DisconnectionException e) {
 					    msg = e.getMessage();
 					} catch (DeletionDelayedException e) {
 					    msg = e.getMessage();
 					}
 
 					if (!result) {
 					    handleError(msg);
 					    return;
 					}
 					// pdialog.dismiss();
 					handle.post(new ParamRunnable(flo) {
 
 					    @Override
 					    public void run() {
 						Toast.makeText(
 							This,
 							"Delete "
 								+ parseFileString(flo)
 								+ " Completed",
 							5).show();
 					    }
 					});
 					options.dismiss();
 					getDir(fullPath);
 				    }
 				}).start();
 				handle.post(new ParamRunnable(flo) {
 
 				    @Override
 				    public void run() {
 					Toast.makeText(
 						This,
 						"Started deleting "
 							+ parseFileString(flo),
 						5).show();
 				    }
 				});
 
 			    }
 			})
 		.setNegativeButton("No", new DialogInterface.OnClickListener() {
 		    public void onClick(DialogInterface dialog, int id) {
 			dialog.cancel();
 		    }
 		}).show();
     }
 
     /***
      * Need to change this to load another dialog box
      * 
      * @param view
      */
     public void onSettings(String flo) {
 	// alertSettingBox(flo);
     }
 }
