 package com.danielpecos.gtdtm.activities.tasks;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.util.HashMap;
 
 import android.app.ProgressDialog;
 import android.os.AsyncTask;
 import android.util.Log;
 
 import com.danielpecos.gtdtm.R;
 import com.danielpecos.gtdtm.model.TaskManager;
 import com.danielpecos.gtdtm.model.beans.Context;
 import com.danielpecos.gtdtm.model.beans.Project;
 import com.danielpecos.gtdtm.model.beans.Task;
 import com.danielpecos.gtdtm.model.beans.TaskContainer;
 
 public class LoadDataFileAsyncTask extends AsyncTask<Object, Integer, String>{
 
 	private android.content.Context ctx;
 
 	ProgressDialog progressDialog;
 
 	private OnFinishedListener onFinishedListener;
 
 	public LoadDataFileAsyncTask(android.content.Context ctx) {
 		this.ctx = ctx;
 	}
 
 	@Override
 	protected String doInBackground(Object... params) {
 
 		String fileName = (String)params[0];
 		String result = null;
 
 		TaskManager taskManager = TaskManager.getInstance(ctx);
 		File file = null;
 		File root = TaskManager.SDCARD_DIR;
 
 		if (root.canRead()){
 			file = new File(root, fileName);
 			if (file.canRead()) {
 				Log.d(TaskManager.TAG, "Loading contexts data from file: " + file.getAbsolutePath() + "...");
 
 				FileInputStream fos = null;
 				ObjectInputStream ois = null;
 				try {
 					fos = new FileInputStream(file.getAbsolutePath());
 					ois = new ObjectInputStream(fos);
 
 					HashMap<Long, Context> readContexts = (HashMap<Long, Context>) ois.readObject();
 					ois.close();
 
 					taskManager.emptyDatabase(ctx);
 
 					for (Context readContext : readContexts.values()) {
 						Context newContext = taskManager.createContext(ctx, readContext.getName());
 						for (Project readProject: readContext.getProjects()) {
 							Project newProject = newContext.createProject(ctx, readProject.getName(), readProject.getDescription());
 							importTasks(ctx, newProject, readProject);
 						}
 						importTasks(ctx, newContext, readContext);
 					}
 
 					Log.d(TaskManager.TAG, "Data successfully loaded");
 					result = String.format(ctx.getString(R.string.file_loadOk), fileName);
 				} catch (FileNotFoundException e) {
 					Log.e(TaskManager.TAG, "File " + fileName + " could not be found", e);
 				} catch (IOException e) {
 					Log.e(TaskManager.TAG, "File " + fileName + " could not be read", e);
 				} catch (ClassNotFoundException e) {
 					Log.e(TaskManager.TAG, "Unable to load object", e);
 				} finally {
 					if (fos != null) {
 						try {
 							fos.close();
 						} catch (IOException e) {
 							Log.e(TaskManager.TAG, "Error closing file input stream", e);
 						}
 					}
 					if (ois != null) {
 						try {
 							ois.close();
 						} catch (IOException e) {
 							Log.e(TaskManager.TAG, "Error closing object input stream", e);
 						}
 					}
 				}
 			} else {
 				result = ctx.getString(R.string.error_file_reading);
 			}
 		} else {
 			Log.w(TaskManager.TAG, "Impossible to write to SD card");
 			result = ctx.getString(R.string.error_sdcard);
 		}
 		return result;
 	}
 
 	private void importTasks(android.content.Context ctx, TaskContainer container, TaskContainer oldContainer) {
 		for (Task oldTask: oldContainer) {
 			container.createTask(ctx, oldTask.getName(), oldTask.getDescription(), oldTask.getPriority())
 			.setDueDate(oldTask.getDueDate())
 			.setLocation(oldTask.getLocation())
 			.setPicture(oldTask.getPicture())
 			.setStatus(oldTask.getStatus())
 			.store(ctx);
 		}
 	}
 
 	@Override
 	protected void onPreExecute() {
 		super.onPreExecute();
 		progressDialog = ProgressDialog.show(ctx, "", ctx.getString(R.string.file_loadingData), true);
 	}
 
 	@Override
 	protected void onPostExecute(String response) {
 		super.onPostExecute(response);
		try { 
			progressDialog.dismiss();
		} catch (Exception e) {
			Log.w(TaskManager.TAG, "Exception raised dismissing progress dialog");
		}
 		if (this.onFinishedListener != null) {
 			onFinishedListener.onFinish(response);
 		}
 	}
 
 	public void setOnFinishedListener(OnFinishedListener onFinishedListener) {
 		this.onFinishedListener = onFinishedListener;
 	}
 }
