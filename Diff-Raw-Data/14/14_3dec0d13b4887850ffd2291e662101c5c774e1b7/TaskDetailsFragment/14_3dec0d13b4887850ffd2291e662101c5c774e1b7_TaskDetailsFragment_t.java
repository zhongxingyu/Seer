 package com.abbyy.cloudocr.fragments;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.text.TextUtils;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.TextView;
 
 import com.abbyy.cloudocr.R;
 import com.abbyy.cloudocr.SettingsActivity;
 import com.abbyy.cloudocr.Task;
 import com.abbyy.cloudocr.TaskDetailsActivity;
 import com.abbyy.cloudocr.TasksManagerService;
 import com.abbyy.cloudocr.database.TasksContract;
 
 public class TaskDetailsFragment extends Fragment {
 	private static final int LOADER_TASK_INFO = 0;
 	private String mTaskId;
 
 	private Task mTask;
 
 	Button mDownloadButton;
 
 	public TaskDetailsFragment() {
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.task_details_fragment, container,
 				false);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		loadTaskData();
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		inflater.inflate(R.menu.task_details, menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			getActivity().finish();
 			break;
 		case R.id.menu_settings:
 			Intent intent = new Intent(getActivity(), SettingsActivity.class);
 			startActivity(intent);
 			break;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 		return true;
 	}
 
 	private void loadTaskData() {
 		if (mTaskId == null) {
 			mTaskId = getArguments().getString(
 					TaskDetailsActivity.EXTRA_TASK_ID);
 
 			getActivity().getSupportLoaderManager().initLoader(
 					LOADER_TASK_INFO, null, new TaskInfoHelper());
 		} else {
 			getActivity().getSupportLoaderManager().restartLoader(
 					LOADER_TASK_INFO, null, new TaskInfoHelper());
 		}
 	}
 
 	private class TaskInfoHelper implements LoaderCallbacks<Cursor> {
 
 		@Override
 		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
 			Loader<Cursor> loader = null;
 			Uri uri = TasksContract.CONTENT_TASKS;
 			String selection = TasksContract.TasksTable.TASK_ID + "=?";
 			String[] selectionArgs = { mTaskId };
 			switch (id) {
 			case LOADER_TASK_INFO:
 				loader = new CursorLoader(getActivity(), uri, null, selection,
 						selectionArgs, null);
 				break;
 			}
 			return loader;
 		}
 
 		@Override
 		public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
 			mTask = new Task(cursor);
 			populateScreen();
 		}
 
 		@Override
 		public void onLoaderReset(Loader<Cursor> loader) {
 		}
 
 	}
 
 	private void populateScreen() {
 		TextView taskIdView = (TextView) getActivity().findViewById(
 				R.id.task_details_task_id);
		if(taskIdView == null){
			return;
		}
 		TextView descriptionView = (TextView) getActivity().findViewById(
 				R.id.task_details_description);
 		TextView statusView = (TextView) getActivity().findViewById(
 				R.id.task_details_status);
 		TextView registrationTimeView = (TextView) getActivity().findViewById(
 				R.id.task_details_registration_time);
 		TextView statusChangeTimeView = (TextView) getActivity().findViewById(
 				R.id.task_details_status_change_time);
 		TextView filesCountView = (TextView) getActivity().findViewById(
 				R.id.task_details_files_count);
 		TextView creditsView = (TextView) getActivity().findViewById(
 				R.id.task_details_credits);
 		TextView estimatedProcessingTimeView = (TextView) getActivity()
 				.findViewById(R.id.task_details_estimated_processing_time);
 		TextView errorMessageView = (TextView) getActivity().findViewById(
 				R.id.task_details_error);
 		mDownloadButton = (Button) getActivity().findViewById(
 				R.id.task_details_download);
 
 		taskIdView.setText(getActivity().getString(R.string.details_task_id)
 				+ mTaskId);
 		descriptionView.setText(getActivity().getString(
 				R.string.details_description)
 				+ mTask.mDescription);
 		statusView.setText(getActivity().getString(R.string.details_status)
 				+ mTask.mStatus);
 		registrationTimeView.setText(getActivity().getString(
 				R.string.details_registration_time)
 				+ mTask.mRegistrationDate);
 		filesCountView.setText(getActivity().getString(
 				R.string.details_files_count)
 				+ mTask.mFilesCount);
 		creditsView.setText(getActivity().getString(R.string.details_credits)
 				+ mTask.mCredits);
 		estimatedProcessingTimeView.setText(getActivity().getString(
 				R.string.details_estimated_processing_time)
 				+ mTask.mEstimatedProcessingTime);
 		statusChangeTimeView.setText(getActivity().getString(
 				R.string.details_status_change_time)
 				+ mTask.mStatusChangeDate);
 		if (!TextUtils.isEmpty(mTask.mError)) {
 			errorMessageView.setText(getActivity().getString(
 					R.string.details_error)
 					+ mTask.mError);
 		} else {
 			errorMessageView.setText(mTask.mResultUrl);
 			errorMessageView.setVisibility(View.GONE);
 		}
 		if (!TextUtils.isEmpty(mTask.mResultUrl)) {
 
 			mDownloadButton.setVisibility(View.VISIBLE);
 			mDownloadButton.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					makeDownload();
 				}
 			});
 		} else {
 			mDownloadButton.setVisibility(View.GONE);
 		}
 
 	}
 
 	private void makeDownload() {
 		String filePath = getFileName();
 		String fileType = getExtension();
 		Intent service = new Intent(getActivity(), TasksManagerService.class);
 		service.putExtra(TasksManagerService.EXTRA_ACTION,
 				TasksManagerService.ACTION_DOWNLOAD_RESULT);
 		service.putExtra(TasksManagerService.EXTRA_URL, mTask.mResultUrl);
 		service.putExtra(TasksManagerService.EXTRA_FILE_PATH, filePath);
 		service.putExtra(TasksManagerService.EXTRA_FILE_TYPE, fileType);
 		getActivity().startService(service);
 	}
 
 	private String getExtension() {
 		if(mTask.mResultType == null){
 			return ".pdf";
 		}
 		if (mTask.mResultType.equals(getString(R.string.export_format_alto))) {
 			return ".alto.xml";
 		} else if (mTask.mResultType
 				.equals(getString(R.string.export_format_csv))) {
 			return ".csv";
 		} else if (mTask.mResultType
 				.equals(getString(R.string.export_format_docx))) {
 			return ".docx";
 		} else if (mTask.mResultType
 				.equals(getString(R.string.export_format_pdf_searchable))) {
 			return ".pdf";
 		} else if (mTask.mResultType
 				.equals(getString(R.string.export_format_pdf_text_and_images))) {
 			return ".pdf";
 		} else if (mTask.mResultType
 				.equals(getString(R.string.export_format_pdfa))) {
 			return ".pdf";
 		} else if (mTask.mResultType
 				.equals(getString(R.string.export_format_pptx))) {
 			return ".pttx";
 		} else if (mTask.mResultType
 				.equals(getString(R.string.export_format_rtf))) {
 			return ".rtf";
 		} else if (mTask.mResultType
 				.equals(getString(R.string.export_format_txt))) {
 			return ".txt";
 		} else if (mTask.mResultType
 				.equals(getString(R.string.export_format_vcard))) {
 			return ".vcf";
 		} else if (mTask.mResultType
 				.equals(getString(R.string.export_format_xml))) {
 			return ".xml";
 		} else if (mTask.mResultType
 				.equals(getString(R.string.export_format_xslx))) {
 			return ".xslx";
 		} else {
 			return ".pdf";
 		}
 	}
 
 	private String getFileName() {
 		if (mTask.mFileName == null) {
 			return new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.GERMANY)
 					.format(new Date());
 		}
 		String[] proj = { MediaStore.Images.Media.DATA };
 		CursorLoader loader = new CursorLoader(getActivity(),
 				Uri.parse(mTask.mFileName), proj, null, null, null);
 		Cursor cursor = loader.loadInBackground();
 		int column_index = cursor
 				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
 		cursor.moveToFirst();
 		String fullPath = cursor.getString(column_index);
 		return fullPath.substring(fullPath.lastIndexOf("/") + 1,
 				fullPath.lastIndexOf("."));
 	}
 }
