 package com.abbyy.cloudocr.optionsfragments;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.support.v4.widget.SimpleCursorAdapter;
 import android.support.v4.widget.SimpleCursorAdapter.CursorToStringConverter;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.LayoutInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.Spinner;
 import android.widget.SpinnerAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.abbyy.cloudocr.R;
 import com.abbyy.cloudocr.SettingsActivity;
 import com.abbyy.cloudocr.TasksManagerService;
 import com.abbyy.cloudocr.database.TasksContract;
 import com.abbyy.cloudocr.utils.CloudClient;
 import com.abbyy.cloudocr.utils.LanguageHelper;
 
 public class ProcessBusinessCardOptionsFragment extends ProcessOptionsFragment
 		implements OnClickListener {
 	private static final int LOADER_AUTOCOMPLETE = 1;
 
 	private static final int ACTIVITY_GET_FILE = 1;
 	private static final int ACTIVITY_TAKE_PICTURE = 2;
 
 	private LanguageHelper mLanguageHelper;
 
 	private String mExportFormat;
 	private String mDescription;
 
 	private Spinner mExportFormatView;
 	private EditText mDescriptionView;
 	private Button mManageLanguagesView;
 	private TextView mFileViewHint;
 	private ImageView mFileView;
 
 	private Uri mFileUri;
 
 	// Necessary for autocompletion
 	private AutoCompleteTextView mAddLanguagesView;
 	private SimpleCursorAdapter mAutoCompleteAdapter;
 	private String mLanguage;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		if (container == null) {
 			return null;
 		}
 		return inflater.inflate(R.layout.process_business_card_fragment,
 				container, false);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		prefs = getActivity().getSharedPreferences(
 				SettingsActivity.PREFERENCES_PROCESS_IMAGE,
 				Activity.MODE_PRIVATE);
 		mLanguageHelper = new LanguageHelper(getActivity().getResources()
 				.getStringArray(R.array.languages));
 		super.onActivityCreated(savedInstanceState);
		if (getArguments() != null && getArguments().containsKey(ARG_FILE_PATH)) {
 			addFile(Uri.parse(getArguments().getString(ARG_FILE_PATH)));
 		}
 		if (savedInstanceState != null
 				&& savedInstanceState.containsKey(ARG_FILE_PATH)) {
 			mFileUri = Uri.parse(savedInstanceState.getString(ARG_FILE_PATH));
 
 			if (mFileView != null) {
 				mFileViewHint.setVisibility(View.GONE);
 				mFileView.setVisibility(View.VISIBLE);
 				setPreview();
 			}
 		}
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		if (mFileUri != null) {
 			outState.putString(ARG_FILE_PATH, mFileUri.toString());
 		}
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_process:
 			launchTask();
 			break;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 		return true;
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		switch (requestCode) {
 		case ACTIVITY_GET_FILE:
 			break;
 		case ACTIVITY_TAKE_PICTURE:
 			galleryAddPic();
 			break;
 		}
 
 		switch (resultCode) {
 		case Activity.RESULT_OK:
 			if (data != null) {
 				addFile(data.getData());
 			} else {
 				addFile(mFileUri);
 			}
 		}
 	}
 
 	@Override
 	public void addFile(Uri filePath) {
 		mFileUri = filePath;
 		setPreview();
 	}
 
 	private void setPreview() {
 		if (mFileView == null) {
 			return;
 		}
 		try {
 			mFileViewHint.setVisibility(View.GONE);
 			mFileView.setVisibility(View.VISIBLE);
 			mFileView.setImageBitmap(getSmallImage());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private Bitmap getSmallImage()
 			throws FileNotFoundException {
 		// Create a BitmapFactory Options to scale the image down
 		BitmapFactory.Options options = new BitmapFactory.Options();
 		// This parameter avoids the next call image from being loaded into
 		// memory
 		options.inJustDecodeBounds = true;
 		BitmapFactory.decodeStream(getActivity().getContentResolver()
 				.openInputStream(mFileUri));
 
 		// Obtain a sampling ratio
 		int outHeight = options.outHeight;
 		int outWidth = options.outWidth;
 		options.inSampleSize = getSampleSize(outHeight, outWidth);
 		// Now we can decode the image and load the small sample size into
 		// memory
 		options.inJustDecodeBounds = false;
 		return BitmapFactory.decodeStream(getActivity().getContentResolver()
 				.openInputStream(mFileUri));
 	}
 
 	private int getSampleSize(int originalHeight, int originalWidth) {
 		int requiredHeight = mFileView.getHeight();
 		int requiredWidth = mFileView.getWidth();
 		if (requiredHeight == 0) {
 			return 4;
 		}
 		int sampleSize = 1;
 		if (requiredHeight < originalHeight || requiredWidth < originalWidth) {
 			final int heightRatio = Math.round((float) requiredHeight
 					/ (float) originalHeight);
 			final int widthRatio = Math.round((float) requiredWidth
 					/ (float) originalWidth);
 			sampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
 		}
 		return sampleSize;
 	}
 
 	@Override
 	public void launchTask() {
 		if (mFileUri == null) {
 			Toast.makeText(getActivity(), R.string.error_no_file_selected,
 					Toast.LENGTH_LONG).show();
 		} else {
 			saveDefaultOptions();
 			Intent service = new Intent(getActivity(),
 					TasksManagerService.class);
 			service.putExtra(TasksManagerService.EXTRA_FILE_PATH,
 					mFileUri.toString());
 			service.putExtra(TasksManagerService.EXTRA_EXPORT_FORMAT,
 					mExportFormat);
 			service.putExtra(TasksManagerService.EXTRA_ACTION,
 					TasksManagerService.ACTION_CREATE_NEW_TASK);
 			service.putExtra(TasksManagerService.EXTRA_CREATE,
 					R.string.process_business_card);
 			service.putExtra(TasksManagerService.EXTRA_NEW_TASK_OPTIONS,
 					getOptions());
 			getActivity().startService(service);
 		}
 	}
 
 	@Override
 	public boolean saveDefaultOptions() {
 		Bundle bundle = getOptions();
 		SharedPreferences.Editor editor = prefs.edit();
 		editor.putString(CloudClient.ARGUMENT_EXPORT_FORMAT,
 				bundle.getString(CloudClient.ARGUMENT_EXPORT_FORMAT));
 		editor.putString(CloudClient.ARGUMENT_DESCRIPTION,
 				bundle.getString(CloudClient.ARGUMENT_DESCRIPTION));
 		editor.putString(CloudClient.ARGUMENT_LANGUAGE,
 				bundle.getString(CloudClient.ARGUMENT_LANGUAGE));
 		return editor.commit();
 	}
 
 	@Override
 	public boolean loadDefaultOptions() {
 		mExportFormat = prefs.getString(CloudClient.ARGUMENT_EXPORT_FORMAT, "");
 		mDescription = prefs.getString(CloudClient.ARGUMENT_DESCRIPTION, "");
 		mLanguageHelper.createLanguages(prefs.getString(
 				CloudClient.ARGUMENT_LANGUAGE, ""));
 
 		return true;
 	}
 
 	public Bundle getOptions() {
 		mDescription = mDescriptionView.getText().toString();
 		Bundle bundle = new Bundle();
 		bundle.putString(CloudClient.ARGUMENT_EXPORT_FORMAT, mExportFormat);
 		bundle.putString(CloudClient.ARGUMENT_DESCRIPTION, mDescription);
 		bundle.putString(CloudClient.ARGUMENT_LANGUAGE,
 				mLanguageHelper.getLanguages());
 		return bundle;
 	}
 
 	@Override
 	boolean setViews() {
 		mExportFormatView = (Spinner) getActivity().findViewById(
 				R.id.option_export_format);
 
 		if (mExportFormatView == null) {
 			return false;
 		}
 		mDescriptionView = (EditText) getActivity().findViewById(
 				R.id.option_description);
 		mAddLanguagesView = (AutoCompleteTextView) getActivity().findViewById(
 				R.id.option_add_languages);
 		mManageLanguagesView = (Button) getActivity().findViewById(
 				R.id.option_button_manage_language);
 		mFileView = (ImageView) getActivity().findViewById(
 				R.id.option_file_path);
 		mFileViewHint = (TextView) getActivity().findViewById(
 				R.id.option_file_path_hint);
 		
 		mExportFormatView.setAdapter(getExportFormatsAdapter());
 		mExportFormatView
 				.setOnItemSelectedListener(getOnItemSelectedListener());
 
 		mManageLanguagesView.setOnClickListener(this);
 
 		mFileViewHint.setOnClickListener(this);
 		mFileView.setOnClickListener(this);
 
 		prepareLanguageAutoComplete();
 		return true;
 	}
 
 	private void prepareLanguageAutoComplete() {
 		// Prepare the adapter for the language auto complete field
 		String[] from = { TasksContract.LanguagesTable.LANGUAGE };
 		int[] to = { android.R.id.text1 };
 		mAutoCompleteAdapter = new SimpleCursorAdapter(getActivity(),
 				android.R.layout.simple_spinner_dropdown_item, null, from, to,
 				0);
 
 		mAddLanguagesView.setAdapter(mAutoCompleteAdapter);
 		mAddLanguagesView.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> view, View v, int position,
 					long id) {
 				TextView languageView = (TextView) v;
 				if (mLanguageHelper.addLanguage(languageView.getText()
 						.toString())) {
 					Toast.makeText(
 							getActivity(),
 							languageView.getText().toString() + " "
 									+ getString(R.string.added),
 							Toast.LENGTH_SHORT).show();
 					mAddLanguagesView.setText("");
 				}
 			}
 		});
 		mAddLanguagesView.setThreshold(0);
 
 		// Set the text watcher
 		mAddLanguagesView.addTextChangedListener(new TextWatcher() {
 			@Override
 			public void afterTextChanged(Editable s) {
 				if (!s.equals("")) {
 					mLanguage = s.toString();
 					getActivity().getSupportLoaderManager().restartLoader(
 							LOADER_AUTOCOMPLETE, null,
 							new LanguageLoaderHelper());
 				} else {
 					mLanguage = "";
 				}
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 			}
 
 			@Override
 			public void onTextChanged(CharSequence s, int start, int count,
 					int after) {
 			}
 
 		});
 
 		// Add the right language to the field
 		mAutoCompleteAdapter
 				.setCursorToStringConverter(new CursorToStringConverter() {
 					@Override
 					public CharSequence convertToString(Cursor cursor) {
 						return cursor.getString(cursor
 								.getColumnIndex(TasksContract.LanguagesTable.LANGUAGE));
 					}
 				});
 	}
 
 	private void manageLanguages() {
 		Toast.makeText(getActivity(), mLanguageHelper.getLanguages(),
 				Toast.LENGTH_SHORT).show();
 
 	}
 
 	private OnItemSelectedListener getOnItemSelectedListener() {
 		OnItemSelectedListener listener = new OnItemSelectedListener() {
 			@Override
 			public void onItemSelected(AdapterView<?> adapterView, View v,
 					int position, long id) {
 				String[] exportFormats = getActivity().getResources()
 						.getStringArray(R.array.business_card_export_formats);
 
 				mExportFormat = exportFormats[position];
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> v) {
 			}
 		};
 		return listener;
 	}
 
 	private SpinnerAdapter getExportFormatsAdapter() {
 		ArrayAdapter<String> adapter = null;
 		adapter = new ArrayAdapter<String>(getActivity(),
 				android.R.layout.simple_spinner_item, getActivity()
 						.getResources().getStringArray(
 								R.array.business_card_export_formats));
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		return adapter;
 	}
 
 	private class LanguageLoaderHelper implements LoaderCallbacks<Cursor> {
 
 		@Override
 		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
 			Uri uri = TasksContract.CONTENT_LANGUAGES;
 			String selection = TasksContract.LanguagesTable.LANGUAGE
 					+ " LIKE ?";
 			String[] selectionArgs = { "%" + mLanguage + "%" };
 			Loader<Cursor> loader = new CursorLoader(getActivity(), uri, null,
 					selection, selectionArgs, null);
 			return loader;
 		}
 
 		@Override
 		public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
 			mAutoCompleteAdapter.swapCursor(cursor);
 		}
 
 		@Override
 		public void onLoaderReset(Loader<Cursor> loader) {
 			mAutoCompleteAdapter.swapCursor(null);
 		}
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.option_file_path:
 		case R.id.option_file_path_hint:
 			takePicture();
 			break;
 		case R.id.option_button_manage_language:
 			manageLanguages();
 			break;
 		}
 	}
 
 	private void takePicture() {
 		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
 		intent.setType("image/*");
 		startActivityForResult(intent, ACTIVITY_GET_FILE);
 		// Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 		// mFileUri = FileManager
 		// .getOutputMediaFileUri(FileManager.MEDIA_TYPE_IMAGE);
 		// if (mFileUri == null) {
 		// return;
 		// }
 		// intent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);
 		// startActivityForResult(intent, ACTIVITY_TAKE_PICTURE);
 	}
 
 	private void galleryAddPic() {
 		new Thread(new GalleryAddThread().addContext(getActivity())).start();
 	}
 
 	private class GalleryAddThread implements Runnable {
 		Context mContext;
 
 		@Override
 		public void run() {
 			Intent mediaScanIntent = new Intent(
 					Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
 			File f = new File(mFileUri.toString());
 			Uri contentUri = Uri.fromFile(f);
 			mediaScanIntent.setData(contentUri);
 			mContext.sendBroadcast(mediaScanIntent);
 			return;
 		}
 
 		public GalleryAddThread addContext(Context context) {
 			mContext = context;
 			return this;
 		}
 	}
 }
