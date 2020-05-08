 /**
  * 
  */
 package com.antonio081014.filemanagement;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.AdapterView;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ImageView.ScaleType;
 
 /**
  * @author antonio081014
  * 
  */
 public class Images extends Activity {
     private static final String TAG = "FMS";
 
     private MyAdapter myAdapter;
     private String msg;
     private ListView listview;
 
     private int[] show_ID;
     private String[] show_Name;
     private String[] show_Path;
 
     private static final int dialog_delete = 0;
     private static final int dialog_display_picture = 1;
     private static final int dialog_sureToDelete = 2;
     private static final int dialog_playAudio = 3;
     private static final int dialog_playMovie = 4;
     private static final int dialog_search = 5;
 
     private static final int menu_search = 0;
     private static final int menu_unfilter = 1;
 
     private int current_position = -1;
     private boolean isByName = false;
     private MediaPlayer mPlayer;
 
     BaseAdapter myBaseAdapter = new BaseAdapter() {
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 	    // TODO Auto-generated method stub
 	    LinearLayout all = new LinearLayout(Images.this);
 	    all.setOrientation(LinearLayout.HORIZONTAL);
 	    // all.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
 	    // LayoutParams.WRAP_CONTENT));
 	    // all.setBackgroundResource(R.drawable.list_row_background);
 	    ImageView imgV = new ImageView(Images.this);
 	    if (msg.compareTo(Main.IMAGE) == 0) {
 		// Bitmap myBitmap =
 		// BitmapFactory.decodeFile(show_Path[position]);
 		// if (myBitmap != null)
 		// imgV.setImageBitmap(myBitmap);
 		// else
 		imgV.setBackgroundResource(R.drawable.icon_image);
 
 	    } else if (msg.compareTo(Main.AUDIO) == 0) {
 		imgV.setBackgroundResource(R.drawable.icon_audio);
 	    } else if (msg.compareTo(Main.MOVIE) == 0)
 		imgV.setBackgroundResource(R.drawable.icon_movie);
 	    else if (msg.compareTo(Main.DOCUMENTS) == 0)
 		imgV.setBackgroundResource(R.drawable.icon_docs);
 	    else if (msg.compareTo(Main.ZIPS) == 0)
 		imgV.setBackgroundResource(R.drawable.icon_zips);
 	    else if (msg.compareTo(Main.APKS) == 0)
 		imgV.setBackgroundResource(R.drawable.icon_apks);
 	    else
 		imgV.setBackgroundResource(R.drawable.ic_launcher);
 
 	    imgV.setScaleType(ScaleType.CENTER);
 
 	    imgV.setAdjustViewBounds(true);
 	    imgV.setMaxHeight(30);
 	    imgV.setMaxWidth(23);
 	    imgV.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
 		    LayoutParams.WRAP_CONTENT));
 	    all.addView(imgV);
 
 	    LinearLayout sub = new LinearLayout(Images.this);
 	    sub.setOrientation(LinearLayout.VERTICAL);
 	    sub.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
 		    LayoutParams.WRAP_CONTENT));
 
 	    TextView tv_name = new TextView(Images.this);
 	    tv_name.setText(show_Name[position]);
 	    tv_name.setTextSize(18);
 	    tv_name.setTextColor(Color.MAGENTA);
 	    // tv_name.setLayoutParams(new
 	    // LayoutParams(LayoutParams.FILL_PARENT,
 	    // LayoutParams.WRAP_CONTENT));
 
 	    TextView tv_path = new TextView(Images.this);
 	    tv_path.setText(show_Path[position]);
 	    tv_path.setTextSize(10);
 	    tv_path.setTextColor(Color.DKGRAY);
 	    // tv_path.setLayoutParams(new
 	    // LayoutParams(LayoutParams.FILL_PARENT,
 	    // LayoutParams.WRAP_CONTENT));
 
 	    sub.addView(tv_name);
 	    sub.addView(tv_path);
 
 	    // if (position % 2 == 0)
 	    // all.setBackgroundColor(Color.BLACK);
 	    // else
 	    // all.setBackgroundColor(Color.WHITE);
 	    all.setBackgroundColor(Color.TRANSPARENT);
 	    all.addView(sub);
 	    return all;
 	}
 
 	@Override
 	public long getItemId(int position) {
 	    // TODO Auto-generated method stub
 	    return 0;
 	}
 
 	@Override
 	public Object getItem(int position) {
 	    // TODO Auto-generated method stub
 	    return null;
 	}
 
 	@Override
 	public int getCount() {
 	    // TODO Auto-generated method stub
 	    if (show_Name != null)
 		return show_Name.length;
 	    return 0;
 	}
     };
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
 	super.onCreate(savedInstanceState);
 	setContentView(R.layout.imageview);
 	myAdapter = new MyAdapter(this);
 	msg = (String) getIntent().getExtras().get("NAME");
 	setTitle(msg + " List");
 	listview = (ListView) findViewById(R.id.list_display);
 	listview.setAdapter(myBaseAdapter);
 	fillDatas(msg, false);
 	mPlayer = new MediaPlayer();
 	mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
 
 	    @Override
 	    public void onCompletion(MediaPlayer mp) {
 		mPlayer.reset();
 	    }
 	});
 	registerForContextMenu(listview);
 
 	// Preview the file selected;
 	listview.setOnItemClickListener(new OnItemClickListener() {
 	    @Override
 	    public void onItemClick(AdapterView<?> arg0, View view,
 		    int position, long id) {
 		current_position = position;
 		preview();
 	    }
 	});
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v,
 	    ContextMenuInfo menuInfo) {
 	super.onCreateContextMenu(menu, v, menuInfo);
 	menu.setHeaderTitle(R.string.contextMenu);
 	MenuInflater inflater = getMenuInflater();
 	inflater.inflate(R.menu.single_file_menu, menu);
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
 	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
 		.getMenuInfo();
 	current_position = info.position;
 	Log.i(TAG, show_Name[current_position]);
 
 	switch (item.getItemId()) {
 	case R.id.menu_delete:
 	    showDialog(dialog_sureToDelete);
 	    return true;
 	case R.id.menu_share:
 	    share();
 	    return true;
 	default:
 	    return super.onContextItemSelected(item);
 	}
     }
 
     protected void preview() {
 	if (msg.compareTo(Main.IMAGE) == 0) {
 	    showDialog(dialog_display_picture);
 	    return;
 	}
 	if (msg.compareTo(Main.AUDIO) == 0) {
 	    showDialog(dialog_playAudio);
 	    return;
 	}
 	if (msg.compareTo(Main.MOVIE) == 0) {
 	    showDialog(dialog_playMovie);
 	    return;
 	}
     }
 
     protected void share() {
 	if (msg.compareTo(Main.IMAGE) == 0) {
 	    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
 	    Uri screenshotUri = Uri.parse(show_Path[current_position]);
 
 	    sharingIntent.setType("image/*");
 	    sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
 	    startActivity(Intent.createChooser(sharingIntent,
 		    "Share image using"));
 	} else if (msg.compareTo(Main.AUDIO) == 0) {
 	    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
 	    Uri screenshotUri = Uri.parse(show_Path[current_position]);
 
 	    sharingIntent.setType("audio/*");
 	    sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
 	    startActivity(Intent.createChooser(sharingIntent,
 		    "Share audio using"));
 	} else if (msg.compareTo(Main.MOVIE) == 0) {
 	    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
 	    Uri screenshotUri = Uri.parse(show_Path[current_position]);
 
 	    sharingIntent.setType("video/*");
 	    sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
 	    startActivity(Intent.createChooser(sharingIntent,
 		    "Share movie using"));
 	} else {
 	    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
 	    Uri screenshotUri = Uri.parse(show_Path[current_position]);
 
 	    sharingIntent.setType("application/*");
 	    sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
 	    startActivity(Intent.createChooser(sharingIntent,
 		    "Share files using"));
 	}
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
 	Dialog dialog = null;
 	LayoutInflater inflater;
 	View layout;
 	Context mContext = this; // getApplicationContext();
 	AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
 	switch (id) {
 	case dialog_delete:
 	    builder.setMessage("Are you sure to delete this file?")
 		    .setCancelable(false).setPositiveButton("Yes",
 			    new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog,
 					int id) {
 				    myAdapter.deleteFile(
 					    show_ID[current_position], true);
 				    current_position = -1;
 				    fillDatas(msg, false);
 				}
 			    }).setNegativeButton("No",
 			    new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog,
 					int id) {
 				    removeDialog(dialog_delete);
 				}
 			    });
 	    dialog = builder.create();
 	    break;
 	case dialog_display_picture:
 	    // Inflate the layout view;
 	    Log.i(TAG, show_Name[current_position]);
 	    // Toast.makeText(mContext, show_Name[current_position],
 	    // Toast.LENGTH_LONG).show();
 	    inflater = (LayoutInflater) mContext
 		    .getSystemService(LAYOUT_INFLATER_SERVICE);
 	    layout = inflater.inflate(R.layout.dialog_display_image,
 		    (ViewGroup) findViewById(R.id.layout_root));
 
 	    // Assign the image to the right view;
 	    ImageView iv = (ImageView) layout
 		    .findViewById(R.id.dialog_display_image);
 	    BitmapFactory.Options options = new BitmapFactory.Options();
 	    // will results in a much smaller image than the original
 	    options.inSampleSize = 8;
 	    // Bitmap picture = BitmapFactory.decodeFile(
 	    // show_Path[current_position], options);
 	    final Bitmap picture = BitmapFactory
 		    .decodeFile(show_Path[current_position]);
 	    iv.setImageBitmap(picture);
 
 	    // Construct the dialog;
 	    builder.setTitle(show_Name[current_position]);
 	    builder.setView(layout);
 	    builder.setPositiveButton("Delete",
 		    new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 			    // TODO Auto-generated method stub
 			    showDialog(dialog_sureToDelete);
 			}
 		    }).setNegativeButton("Done",
 		    new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 			    // TODO Auto-generated method stub
 			    removeDialog(dialog_display_picture);
 			}
 		    });
 	    dialog = builder.create();
 	    break;
 	case dialog_sureToDelete:
 	    builder.setTitle("Delete " + show_Name[current_position]);
 	    builder
 		    .setMessage("1. System Delete: delete the item from system. Non-revertable.\n"
 			    + "2. View Delete: delete the item from view, it still exists on the system.\n"
 			    + "3. Cancel: delete nothing.");
 	    builder.setPositiveButton("System Delete",
 		    new DialogInterface.OnClickListener() {

 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 			    myAdapter.deleteFile(show_ID[current_position],
 				    false);
 			}
 		    }).setNeutralButton("View Delete",
 		    new DialogInterface.OnClickListener() {

 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 			    myAdapter.deleteFile(show_ID[current_position],
 				    true);
 			}
 		    }).setNegativeButton("Cancel",
 		    new DialogInterface.OnClickListener() {

 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 			    removeDialog(dialog_sureToDelete);
 			}
 		    });
 	    dialog = builder.create();
 	    break;
 	case dialog_playAudio:
 	    builder.setTitle("Playing " + show_Name[current_position]);
 	    builder.setCancelable(false);
 	    builder.setPositiveButton("Start",
 		    new DialogInterface.OnClickListener() {

 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 			}
 		    }).setNegativeButton("Close",
 		    new DialogInterface.OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 			}
 		    });
 	    dialog = builder.create();
 	    dialog.setOnShowListener(new DialogInterface.OnShowListener() {
 		@Override
 		public void onShow(DialogInterface dialog) {
 		    // TODO Auto-generated method stub
 		    Button startPlayer = ((AlertDialog) dialog)
 			    .getButton(dialog.BUTTON_POSITIVE);
 		    startPlayer.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 			    if (mPlayer.isPlaying() == false) {
 				try {
 				    mPlayer
 					    .setAudioStreamType(AudioManager.STREAM_MUSIC);
 				    mPlayer
 					    .setDataSource(show_Path[current_position]);
 				    mPlayer.prepare();
 				    mPlayer.start();
 				} catch (Exception e) {
 				    e.printStackTrace();
 				}
 			    }
 			}
 		    });
 
 		    Button closeButton = ((AlertDialog) dialog)
 			    .getButton(dialog.BUTTON_NEGATIVE);
 		    closeButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 			    if (mPlayer.isPlaying())
 				mPlayer.stop();
 			    mPlayer.reset();
 			    removeDialog(dialog_playAudio);
 			}
 		    });
 		}
 	    });
 	    break;
 	case dialog_playMovie:
 	    break;
 	case dialog_search:
 	    inflater = (LayoutInflater) mContext
 		    .getSystemService(LAYOUT_INFLATER_SERVICE);
 	    layout = inflater.inflate(R.layout.search_box,
 		    (ViewGroup) findViewById(R.id.layout_root));
 	    final EditText et_searchContent = (EditText) layout
 		    .findViewById(R.id.et_searchBoxContent);
 
 	    // Construct the dialog;
 	    builder.setTitle("Search");
 	    builder.setView(layout);
 	    builder.setPositiveButton("Go",
 		    new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 			    String searchContent = et_searchContent.getText()
 				    .toString();
 			    fillDatas(searchContent, true);
 			}
 		    }).setNegativeButton("Cancel",
 		    new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 			    removeDialog(dialog_search);
 			}
 		    });
 	    dialog = builder.create();
 	    break;
 	default:
 	    super.onCreateDialog(id);
 	}
 	return dialog;
     }
 
     @Override
     protected void onResume() {
 	myAdapter = new MyAdapter(this);
 	// myAdapter.open();
 	fillDatas(msg, false);
 	myBaseAdapter.notifyDataSetChanged();
 	super.onResume();
     }
 
     private void fillDatas(String str, boolean isByName) {
 	if (isByName == false) {
 	    fillDataByType(str);
 	} else {
 	    fillDataByName(str);
 	}
     }
 
     private void fillDataByName(String str) {
 	List<Integer> list_ids = new ArrayList<Integer>();
 	List<String> list_names = new ArrayList<String>();
 	List<String> list_paths = new ArrayList<String>();
 	for (int i = 0; i < show_Name.length; i++) {
 	    if (show_Name[i].contains(str)) {
 		list_ids.add(show_ID[i]);
 		list_names.add(show_Name[i]);
 		list_paths.add(show_Path[i]);
 	    }
 	}
 	if (list_ids.size() <= 0) {
 	    Toast.makeText(this, "No matching content exists",
 		    Toast.LENGTH_LONG).show();
 	    return;
 	}
 	show_ID = new int[list_ids.size()];
 	show_Name = new String[list_ids.size()];
 	show_Path = new String[list_ids.size()];
 	for (int i = 0; i < list_ids.size(); i++) {
 	    show_ID[i] = list_ids.get(i);
 	    show_Name[i] = list_names.get(i);
 	    show_Path[i] = list_paths.get(i);
 	}
     }
 
     private void fillDataByType(String str) {
 	myAdapter.open();
 	Cursor mCursor = myAdapter.fetchFileByType(str);
 
 	int idIndex = mCursor.getColumnIndex(DatabaseHelper.KEY_ROWID);
 	int nameIndex = mCursor.getColumnIndex(DatabaseHelper.KEY_NAME);
 	int pathIndex = mCursor.getColumnIndex(DatabaseHelper.KEY_PATH);
 	show_ID = new int[mCursor.getCount()];
 	show_Name = new String[mCursor.getCount()];
 	show_Path = new String[mCursor.getCount()];
 
 	int i = 0;
 	for (mCursor.moveToFirst(); !(mCursor.isAfterLast()); mCursor
 		.moveToNext()) {
 	    show_ID[i] = mCursor.getInt(idIndex);
 	    show_Name[i] = mCursor.getString(nameIndex);
 	    show_Path[i] = mCursor.getString(pathIndex);
 	    i++;
 	}
 	mCursor.close();
 	myAdapter.close();
     }
 
     @Override
     public void onPause() {
 	super.onPause();
 	mPlayer.release();
 	myAdapter.close();
     }
 
     @Override
     protected void onDestroy() {
 	super.onDestroy();
 	mPlayer.release();
 	if (myAdapter != null) {
 	    myAdapter.close();
 	}
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
 	super.onCreateOptionsMenu(menu);
 	menu.add(0, menu_unfilter, 0, R.string.menu_unfilter);
 	menu.add(0, menu_search, 0, R.string.menu_search);
 	return true;
     }
 
     @Override
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
 	switch (item.getItemId()) {
 	case menu_unfilter:
 	    fillDatas(msg, false);
 	    myBaseAdapter.notifyDataSetChanged();
 	    return true;
 	case menu_search:
 	    showDialog(dialog_search);
 	    myBaseAdapter.notifyDataSetChanged();
 	    return true;
 	default:
 	    return super.onMenuItemSelected(featureId, item);
 	}
     }
 }
