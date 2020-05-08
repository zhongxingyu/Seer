 package com.quanturium.androcloud2.fragments;
 
 import java.io.File;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ClipData;
 import android.content.ClipboardManager;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.AsyncTask.Status;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.webkit.MimeTypeMap;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.cloudapp.api.CloudAppException;
 import com.cloudapp.api.model.CloudAppItem;
 import com.cloudapp.api.model.CloudAppItem.Type;
 import com.cloudapp.impl.model.CloudAppItemImpl;
 import com.quanturium.androcloud2.Constants;
 import com.quanturium.androcloud2.FragmentInitParams;
 import com.quanturium.androcloud2.MyApplication;
 import com.quanturium.androcloud2.R;
 import com.quanturium.androcloud2.activities.MainActivity;
 import com.quanturium.androcloud2.databases.FilesDatabase;
 import com.quanturium.androcloud2.holders.FileDetailsViewHolder;
 import com.quanturium.androcloud2.listeners.ImageCacheTaskListener;
 import com.quanturium.androcloud2.requests.FileActionTask;
 import com.quanturium.androcloud2.requests.FileActionTaskQuery;
 import com.quanturium.androcloud2.requests.FileActionTaskQuery.ActionType;
 import com.quanturium.androcloud2.requests.ImageCacheTask;
 import com.quanturium.androcloud2.tools.Prefs;
 
 public class FileDetailsFragment extends AbstractFragment implements OnClickListener, ImageCacheTaskListener
 {
 	private int						itemId					= 0;
 	private FilesDatabase			database				= null;
 	private FileDetailsViewHolder	viewHolder				= null;
 	private CloudAppItem			item					= null;
 	AlertDialog						alertSaveBeforePreview	= null;
 	AlertDialog						alertRename				= null;
 
 	public enum Action
 	{
 		ACTION_VIEW, ACTION_SAVE, ACTION_SHARE, ACTION_EDIT, ACTION_DELETE
 	}
 
 	@Override
 	protected FragmentInitParams init()
 	{
 		return new FragmentInitParams(R.layout.fragment_filedetails, "File", null, false, false);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState)
 	{
 		super.onActivityCreated(savedInstanceState);
 
 		database = FilesDatabase.getInstance(getActivity());
 
 		Bundle bundle = this.getArguments();
 
 		if (bundle != null)
 		{
 			if (getArguments().containsKey(Constants.ITEM_ID_KEY))
 			{
 				int itemId = getArguments().getInt(Constants.ITEM_ID_KEY, -1);
 				if (itemId != -1)
 				{
 					try
 					{
 						this.itemId = itemId;
 						loadItem();
 
 						if (item != null)
 						{
 							setUI(savedInstanceState);
 							fillUI();
 						}
 
 					} catch (JSONException e)
 					{
 						e.printStackTrace();
 					} catch (CloudAppException e)
 					{
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState)
 	{
 		if (alertSaveBeforePreview != null && alertSaveBeforePreview.isShowing())
 			outState.putBoolean(Constants.FILE_ALERT_SAVE_KEY, true);
 
 		if (alertRename != null && alertRename.isShowing())
 			outState.putBoolean(Constants.FILE_ALERT_RENAME_KEY, true);
 
 		super.onSaveInstanceState(outState);
 	}
 
 	@Override
 	public void onDestroy()
 	{
 		if (alertSaveBeforePreview != null)
 			alertSaveBeforePreview.dismiss();
 
 		if (alertRename != null)
 			alertRename.dismiss();
 
 		super.onDestroy();
 	}
 
 	private void setUI(Bundle savedInstanceState) throws CloudAppException
 	{
 		viewHolder = new FileDetailsViewHolder();
 		viewHolder.actionView = (ImageView) getActivity().findViewById(R.id.filedetailsActionView);
 		viewHolder.actionView.setOnClickListener(this);
 
 		switch (item.getItemType())
 		{
 			case BOOKMARK:
 
 				viewHolder.actionSave = (ImageView) getActivity().findViewById(R.id.filedetailsActionSave);
 				viewHolder.actionSave.setOnClickListener(null);
 				viewHolder.actionSave.setAlpha(0.2f);
 
 				break;
 
 			default:
 
 				viewHolder.actionSave = (ImageView) getActivity().findViewById(R.id.filedetailsActionSave);
 				viewHolder.actionSave.setOnClickListener(this);
 				viewHolder.actionSave.setAlpha(1.0f);
 
 				break;
 		}
 
 		viewHolder.actionShare = (ImageView) getActivity().findViewById(R.id.filedetailsActionShare);
 		viewHolder.actionShare.setOnClickListener(this);
 		viewHolder.actionEdit = (ImageView) getActivity().findViewById(R.id.filedetailsActionEdit);
 		viewHolder.actionEdit.setOnClickListener(this);
 		viewHolder.actionDelete = (ImageView) getActivity().findViewById(R.id.filedetailsActionDelete);
 		viewHolder.actionDelete.setOnClickListener(this);
 
 		viewHolder.contentPicture = (ImageView) getActivity().findViewById(R.id.contentImageView);
 		viewHolder.contentLoader = (ProgressBar) getActivity().findViewById(R.id.contentLoader);
 		viewHolder.contentText = (TextView) getActivity().findViewById(R.id.contentTextView);
 
 		if (savedInstanceState != null)
 		{
 			if (savedInstanceState.containsKey(Constants.FILE_ALERT_SAVE_KEY))
 				createAlertSaveBeforePreview();
 			else if (savedInstanceState.containsKey(Constants.FILE_ALERT_RENAME_KEY))
 				createAlertRename();
 
 		}
 	}
 
 	private void loadItem() throws JSONException, CloudAppException
 	{
 		Cursor cursor = database.getFile(itemId);
 		String json = cursor.getString(cursor.getColumnIndex(FilesDatabase.COL_DATA));
 
 		if (json != null)
 		{
 			item = new CloudAppItemImpl(new JSONObject(json));
 			getActivity().getActionBar().setSubtitle(item.getName());
 		}
 
 		if (item.getItemType() == Type.IMAGE)
 		{
 			ImageCacheTask task = ((MyApplication) getActivity().getApplication()).getImageCacheTask(itemId);
 			if (task != null && task.getStatus() != Status.FINISHED)
 			{
 				task.setCallback(this);
 			}
 			else
 			{
 				task = new ImageCacheTask(getActivity(), this);
 				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, item.getContentUrl());
 				((MyApplication) getActivity().getApplication()).setImageCacheTask(itemId, task);
 			}
 		}
 		else
 		{
 			// do nothing
 		}
 	}
 
 	private void fillUI()
 	{
 		try
 		{
 			switch (item.getItemType())
 			{
 				case IMAGE: // nothing to do, put the loader
 
 					setContent(null, null, true);
 
 					break;
 
 				case TEXT:
 
 					setContent(null, "Save this file to open it", false);
 
 					break;
 
 				case ARCHIVE:
 
 					setContent(null, "Save this file to open it", false);
 
 					break;
 
 				case AUDIO:
 
 					setContent(null, "Save this file to play it", false);
 
 					break;
 
 				case VIDEO:
 
 					setContent(null, "Save this file to play it", false);
 
 					break;
 
 				case BOOKMARK:
 
 					setContent(null, "Click on 'open' below to access your link", false);
 
 					break;
 
 				case UNKNOWN:
 
 					setContent(null, "Save this file to open it", false);
 
 					break;
 			}
 
 		} catch (CloudAppException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private void setContent(Bitmap bitmap, String text, boolean loading)
 	{
 		if (loading)
 		{
 			viewHolder.contentPicture.setVisibility(View.GONE);
 			viewHolder.contentText.setVisibility(View.GONE);
 			viewHolder.contentLoader.setVisibility(View.VISIBLE);
 		}
 		else
 		{
 			viewHolder.contentLoader.setVisibility(View.GONE);
 
 			if (bitmap != null)
 			{
 				viewHolder.contentPicture.setImageBitmap(bitmap);
 				viewHolder.contentText.setVisibility(View.GONE);
 				viewHolder.contentPicture.setVisibility(View.VISIBLE);
 
 			}
 			else
 			{
 				viewHolder.contentText.setText(text);
 				viewHolder.contentPicture.setVisibility(View.GONE);
 				viewHolder.contentText.setVisibility(View.VISIBLE);
 			}
 		}
 	}
 
 	@Override
 	public void onClick(View v)
 	{
 		try
 		{
 			switch (v.getId())
 			{
 				case R.id.filedetailsActionView:
 
 					startAction(Action.ACTION_VIEW);
 
 					break;
 
 				case R.id.filedetailsActionSave:
 
 					startAction(Action.ACTION_SAVE);
 
 					break;
 
 				case R.id.filedetailsActionShare:
 
 					startAction(Action.ACTION_SHARE);
 
 					break;
 
 				case R.id.filedetailsActionEdit:
 
 					startAction(Action.ACTION_EDIT);
 
 					break;
 
 				case R.id.filedetailsActionDelete:
 
 					startAction(Action.ACTION_DELETE);
 
 					break;
 			}
 
 		} catch (CloudAppException e)
 		{
 			// stopLoading();
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void onImageCacheTaskFinish(Bitmap bitmap)
 	{
 		Activity activity = getActivity();
 
 		if (activity != null)
 		{
 			((MyApplication) activity.getApplication()).setImageCacheTask(itemId, null);
 
 			setContent(bitmap, null, false);
 		}
 	}
 
 	@Override
 	public void onImageCacheTaskCancel()
 	{
 		Activity activity = getActivity();
 
 		if (activity != null)
 		{
 			((MyApplication) activity.getApplication()).setImageCacheTask(itemId, null);
 			Toast.makeText(getActivity(), "Error while loading image preview", Toast.LENGTH_SHORT).show();
 			setContent(null, "Error", false);
 		}
 	}
 
 	private void startAction(Action action) throws CloudAppException
 	{
 		switch (action)
 		{
 			case ACTION_VIEW:
 
 				switch (item.getItemType())
 				{
 					case BOOKMARK:
 
 						Intent i1 = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getRedirectUrl()));
 						i1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 						startActivity(i1);
 
 						break;
 
 					default:
 
 						File saveFile = new File(getSavingFolder(), item.getName());
 
 						if (saveFile.exists())
 						{
 							Intent i2 = new Intent();
 							i2.setAction(android.content.Intent.ACTION_VIEW);
 							i2.setDataAndType(Uri.fromFile(saveFile), getMIME());
 							i2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 							startActivity(i2);
 						}
 						else
 						{
 							// We propose to save it before opening it
 							createAlertSaveBeforePreview();
 						}
 
 						break;
 				}
 
 				break;
 
 			case ACTION_SAVE:
 
 				Intent i3 = new Intent();
 				i3.setAction(Constants.INTENT_ACTION_DOWNLOAD);
 				i3.putExtra(Constants.DOWNLOAD_URL_KEY, item.getContentUrl());
 				i3.putExtra(Constants.DOWNLOAD_PATH_KEY, getSavingFolder());
 				i3.putExtra(Constants.DOWNLOAD_NAME_KEY, item.getName());
 				((MyApplication) getActivity().getApplication()).sendToMainService(i3);
 
 				Toast.makeText(getActivity(), "Download started", Toast.LENGTH_SHORT).show();
 
 				break;
 
 			case ACTION_SHARE:
 
 				ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
 				ClipData clip = ClipData.newPlainText(item.getName() + " URL", item.getUrl());
 				clipboard.setPrimaryClip(clip);
 
 				Toast.makeText(getActivity(), "Link copied to your clipboard", Toast.LENGTH_SHORT).show();
 
 				break;
 
 			case ACTION_EDIT:
 
 				createAlertRename();
 
 				break;
 
 			case ACTION_DELETE:				
 				
 				FileActionTaskQuery query = new FileActionTaskQuery(Prefs.getPreferences(getActivity()).getString(Prefs.EMAIL, ""), Prefs.getPreferences(getActivity()).getString(Prefs.PASSWORD, ""), item, ActionType.DELETE, null);
 				FileActionTask deleteTask = new FileActionTask();
 				deleteTask.execute(query);
 
 				MainActivity activity = (MainActivity) getActivity();
 
 				if (activity != null)
 				{
 					item.setDeleted(true);
 					database.updateFile(itemId, item);
 					
					Toast.makeText(activity, "File moved to trash", Toast.LENGTH_SHORT).show();
 					activity.getFragmentManager().popBackStack();
 				}
 
 				break;
 		}
 	}
 
 	// tool methods
 
 	private void createAlertSaveBeforePreview()
 	{
 		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 
 		builder.setTitle("Open file");
 		builder.setMessage("The file has not been found on your device. You must save it first. Would you like to download the file ?");
 		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
 		{
 
 			@Override
 			public void onClick(DialogInterface dialog, int which)
 			{
 				dialog.dismiss();
 			}
 		});
 		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
 		{
 			public void onClick(DialogInterface dialog, int whichButton)
 			{
 				try
 				{
 					startAction(Action.ACTION_SAVE);
 				} catch (CloudAppException e)
 				{
 					e.printStackTrace();
 				}
 			}
 		});
 
 		alertSaveBeforePreview = builder.create();
 		alertSaveBeforePreview.show();
 	}
 
 	private void createAlertRename()
 	{
 		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
 
 		alert.setTitle("New name");
 
 		// Set an EditText view to get user input
 		final EditText input = new EditText(getActivity());
 		input.setText(getNameWithoutExtension());
 		input.setSelectAllOnFocus(true);
 		alert.setView(input);
 		alert.setNegativeButton("Cancel", null);
 		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener()
 		{
 			public void onClick(DialogInterface dialog, int whichButton)
 			{
 				String value = input.getText().toString().trim();
 
 				if (!value.equals("") && !value.equals(getNameWithoutExtension()))
 				{
 					String ext = getExtention();
 					String newName;
 
 					if (ext != null)
 						newName = value + '.' + getExtention();
 					else
 						newName = value;					
 
 					try
 					{												
 						FileActionTaskQuery query = new FileActionTaskQuery(Prefs.getPreferences(getActivity()).getString(Prefs.EMAIL, ""), Prefs.getPreferences(getActivity()).getString(Prefs.PASSWORD, ""), item, ActionType.RENAME, newName);
 						FileActionTask renameTask = new FileActionTask();
 						renameTask.execute(query);						
 						
 						MainActivity activity = (MainActivity) getActivity();
 
 						if (activity != null)
 						{
 							item.setName(newName);
 							database.updateFile(itemId, item);
 							getActivity().getActionBar().setSubtitle(item.getName());
 							
 							Toast.makeText(getActivity(), "File renamed to " + newName, Toast.LENGTH_SHORT).show();
 						}
 
 					} catch (CloudAppException e)
 					{
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 
 			}
 		});
 
 		alertRename = alert.create();
 		alertRename.show();
 
 	}
 
 	private String getSavingFolder() throws CloudAppException
 	{
 		String folder = null;
 
 		Type type = item.getItemType();
 
 		switch (type)
 		{
 			case IMAGE:
 
 				folder = Prefs.getPreferences(getActivity()).getString(Prefs.DIRECTORY_PICTURES, null);
 
 				break;
 
 			case AUDIO:
 
 				folder = Prefs.getPreferences(getActivity()).getString(Prefs.DIRECTORY_MUSICS, null);
 
 				break;
 
 			case VIDEO:
 
 				folder = Prefs.getPreferences(getActivity()).getString(Prefs.DIRECTORY_MOVIES, null);
 
 				break;
 
 			case TEXT:
 
 				folder = Prefs.getPreferences(getActivity()).getString(Prefs.DIRECTORY_TEXTS, null);
 
 				break;
 
 			case ARCHIVE:
 			case UNKNOWN:
 			default:
 
 				folder = Prefs.getPreferences(getActivity()).getString(Prefs.DIRECTORY_UNKOWN, null);
 
 				break;
 		}
 
 		return folder;
 	}
 
 	private String getExtention()
 	{
 		try
 		{
 			int index = item.getRemoteUrl().lastIndexOf(".") + 1;
 
 			if (index == 0) // no file extention
 				throw new Exception();
 
 			return item.getRemoteUrl().substring(index);
 		} catch (CloudAppException e)
 		{
 			return null;
 		} catch (Exception e)
 		{
 			return null;
 		}
 	}
 
 	private String getNameWithoutExtension()
 	{
 		try
 		{
 			int index = item.getName().lastIndexOf(".") + 1;
 
 			if (index == 0) // no file extention
 			{
 				return item.getName();
 			}
 			else
 			{
 				return item.getName().substring(0, index - 1);
 			}
 		} catch (CloudAppException e)
 		{
 			return null;
 		}
 	}
 
 	private String getMIME()
 	{
 		MimeTypeMap mime = MimeTypeMap.getSingleton();
 		return mime.getMimeTypeFromExtension(getExtention());
 	}
 
 }
