 /**
  * Copyright 2010 Eric Taix
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and 
  * limitations under the License.
  * 
  */
 package com.bigpupdev.synodroid.adapter;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import com.bigpupdev.synodroid.Synodroid;
 import com.bigpupdev.synodroid.server.SynoServer;
 import com.bigpupdev.synodroid.data.Task;
 import com.bigpupdev.synodroid.data.TaskDetail;
 import com.bigpupdev.synodroid.data.TaskStatus;
 import com.bigpupdev.synodroid.ui.DownloadFragment;
 import com.bigpupdev.synodroid.R;
 import com.bigpupdev.synodroid.utils.UIUtils;
 import com.bigpupdev.synodroid.utils.Utils;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.BaseAdapter;
 import android.widget.CheckBox;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 /**
  * An adaptor for torrents list. This adaptor aims to create a view for each item in the listView
  * 
  * @author eric.taix at gmail.com
  */
 public class TaskAdapter extends BaseAdapter implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
 
 	// List of torrent
 	private List<Task> tasks = new ArrayList<Task>();
 	// List of previous tasks
 	private HashMap<Integer, Integer> uploadRatios = new HashMap<Integer, Integer>();
 	private HashMap<Integer, Long> uploadTimestamp = new HashMap<Integer, Long>();
 	private HashMap<Integer, Boolean> torrents = new HashMap<Integer, Boolean>();
 	// The XML view inflater
 	private final LayoutInflater inflater;
 	// The main activity
 	private DownloadFragment fragment;
 	// Bitmap which is used for unknown progress
 	private BitmapDrawable unknownDrawable;
 	
 	private Context c;
 	private Activity a;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param activityP
 	 *            The current activity
 	 * @param torrentsP
 	 *            List of torrent
 	 */
 	public TaskAdapter(DownloadFragment fragmentP) {
 		fragment = fragmentP;
 		a = fragment.getActivity();
 		c = a.getApplicationContext();
 		inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		Bitmap bitmap = BitmapFactory.decodeResource(fragmentP.getResources(), R.drawable.progress_unknown);
 		bitmap = Utils.getRoundedCornerBitmap(bitmap, 4);
 		unknownDrawable = new BitmapDrawable(bitmap);
 	}
 
 	/**
 	 * Update the torrents list
 	 * 
 	 * @param torrentsP
 	 */
 	public void updateTasks(List<Task> tasksP) {
 		// First update upload informations
 		for (Task task : tasksP) {
 			int taskId = task.taskId;
 			Integer progress = uploadRatios.get(taskId);
 			if (progress != null) {
 				task.uploadProgress = progress;
 			}
 			Long timestamp = uploadTimestamp.get(taskId);
 			if (timestamp != null) {
 				task.uploadTimestamp = timestamp;
 			}
 			Boolean isTorrent = torrents.get(taskId);
 			if (isTorrent != null) {
 				task.isTorrent = isTorrent;
 			}
 		}
 		tasks = tasksP;
 		notifyDataSetChanged();
 	}
 
 	/**
 	 * Update a task with its details
 	 * 
 	 * @param taskId
 	 * @param prorgessP
 	 */
 	public void updateFromDetail(TaskDetail detailP) {
 		Integer upPerc = Utils.computeUploadPercent(detailP);
 		int up = (upPerc != null ? upPerc.intValue() : 0);
 		// Affect the new upload's progress
 		uploadRatios.put(detailP.taskId, up);
 		// The upload ration has been updated: timestamp
 		uploadTimestamp.put(detailP.taskId, System.currentTimeMillis());
 		// Update the torrent flag
 		torrents.put(detailP.taskId, detailP.isTorrent);
 		// Self update
 		updateTasks(tasks);
 	}
 
 	/**
 	 * Return the count of element
 	 * 
 	 * @return The number of torrent in the list
 	 */
 	public int getCount() {
 		if (tasks != null) {
 			return tasks.size();
 		} else {
 			return 0;
 		}
 	}
 
 	/**
 	 * Return the torrent at the defined index
 	 * 
 	 * @param indexP
 	 *            The index to use starting from 0
 	 * @return Instance of Torrent
 	 */
 	public Object getItem(int indexP) {
 		if (tasks != null) {
 			if (indexP < tasks.size()) {
 				return tasks.get(indexP);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Return the item id of the item at index X
 	 * 
 	 * @param indexP
 	 */
 	public long getItemId(int indexP) {
 		try {
 			return tasks.get(indexP).taskId;
 		} catch (Exception e) {
 			return 0;
 		}
 	}
 
 	/**
 	 * Return the view used for the item at position indexP. Always try to reuse an old view
 	 */
 	public View getView(int positionP, View convertViewP, ViewGroup parentP) {
 		LinearLayout view = null;
 		if (convertViewP != null) {
 			view = (LinearLayout) convertViewP;
 		} else {
 			view = (LinearLayout) inflater.inflate(R.layout.task_template, parentP, false);
			LinearLayout parent = (LinearLayout) view.findViewById(R.id.id_parent_view_template);
			final CheckBox checkbox = (CheckBox) view.findViewById(R.id.id_torrent_cb);
			parent.setOnClickListener(new OnClickListener(){
				public void onClick(View arg0) {
					if (checkbox.isChecked()){
						checkbox.setChecked(false);
					}
					else{
						checkbox.setChecked(true);
					}
				}});
 		}
 		try{
 			bindView(view, tasks.get(positionP));
 		}catch (IndexOutOfBoundsException e){
 			view = (LinearLayout) inflater.inflate(R.layout.empty_task_template, parentP, false);
 		}
 		return view;
 	}
 
 	/**
 	 * Bind torrent's data with widget
 	 * 
 	 * @param viewP
 	 * @param taskP
 	 */
 	private void bindView(LinearLayout viewP, final Task taskP) {
 		CheckBox cb  = (CheckBox) viewP.findViewById(R.id.id_torrent_cb);
 		if (UIUtils.isHoneycomb()){
 			cb.setOnCheckedChangeListener(fragment);
 			cb.setTag(taskP);
 		}
 		else{
 			cb.setVisibility(View.GONE);
 		}
 		
 		// Torrent's status icon
 		ImageView image = (ImageView) viewP.findViewById(R.id.id_torrent_icon);
 		IconFacade.bindTorrentStatus(c, image, taskP);
 
 		// The name of the torrent
 		TextView torrentName = (TextView) viewP.findViewById(R.id.id_torrent_name);
 		torrentName.setText(taskP.fileName);
 
 		// The torrent size
 		TextView torrentSize = (TextView) viewP.findViewById(R.id.id_torrent_total_size);
 		torrentSize.setText(taskP.totalSize);
 
 		// The torrent's owner
 		TextView torrentCurrentSize = (TextView) viewP.findViewById(R.id.id_torrent_username);
 		torrentCurrentSize.setText(taskP.creator);
 
 		// Get progress bar
 		ProgressBar downProgress = (ProgressBar) viewP.findViewById(R.id.id_download_progress);
 		ProgressBar upProgress = (ProgressBar) viewP.findViewById(R.id.id_upload_progress);
 		ProgressBar unknownProgress = (ProgressBar) viewP.findViewById(R.id.id_unknow_progress);
 		unknownProgress.setBackgroundDrawable(unknownDrawable);
 		// If state is DOWNLOADING or SEEDING or PAUSED
 		if (taskP.status.equals(TaskStatus.TASK_DOWNLOADING.toString()) || taskP.status.equals(TaskStatus.TASK_SEEDING.toString()) || taskP.status.equals(TaskStatus.TASK_PAUSED.toString())) {
 			downProgress.setVisibility(View.VISIBLE);
 			// If a known value
 			if (taskP.downloadProgress != -1) {
 				downProgress.setProgress(taskP.downloadProgress);
 			}
 			// According to the user's preferences AND if it is a Torrent
 			SynoServer server = ((Synodroid) a.getApplication()).getServer();
 			if (server.getConnection().showUpload) {
 				// If a known value
 				if (taskP.uploadProgress != -1) {
 					upProgress.setVisibility(taskP.isTorrent ? View.VISIBLE : View.INVISIBLE);
 					unknownProgress.setVisibility(View.GONE);
 					upProgress.setProgress(taskP.uploadProgress);
 				}
 				// If no value then hide it !
 				else {
 					upProgress.setVisibility(View.GONE);
 					// Show only for a torrent
 					unknownProgress.setVisibility(taskP.isTorrent ? View.VISIBLE : View.INVISIBLE);
 				}
 			} else {
 				upProgress.setVisibility(View.INVISIBLE);
 				unknownProgress.setVisibility(View.GONE);
 			}
 		}
 		// Hide progress bars
 		else {
 			downProgress.setVisibility(View.INVISIBLE);
 			upProgress.setVisibility(View.INVISIBLE);
 			unknownProgress.setVisibility(View.GONE);
 		}
 		// The current rates
 		TextView torrentRates = (TextView) viewP.findViewById(R.id.id_torrent_speed);
 		String rates = "";
 		if (taskP.downloadRate.length() > 0) {
 			rates += "D:" + taskP.downloadRate + "    ";
 		}
 		if (taskP.uploadRate.length() > 0) {
 			rates += "U:" + taskP.uploadRate;
 		}
 		torrentRates.setText(rates);
 
 		// The estimated time left
 		TextView torrentETA = (TextView) viewP.findViewById(R.id.id_torrent_eta);
 		torrentETA.setText(taskP.eta);
 	}
 
 	/**
 	 * Return the tasks list
 	 * 
 	 * @return
 	 */
 	public List<Task> getTaskList() {
 		return tasks;
 	}
 
 	/**
 	 * Click on a item
 	 */
 	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 		Task task = tasks.get(position);
 		if (task != null) {
 			fragment.onTaskClicked(task);
 		}
 	}
 
 	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
 		Task task = tasks.get(position);
 		if (task != null) {
 			fragment.onTaskLongClicked(task);
 			return true;
 		}
 		return false;
 	}
 }
