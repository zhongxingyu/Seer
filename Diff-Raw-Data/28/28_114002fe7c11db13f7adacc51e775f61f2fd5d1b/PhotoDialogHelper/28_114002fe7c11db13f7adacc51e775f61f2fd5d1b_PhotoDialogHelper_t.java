 package ru.redsolution.rosyama;
 
 import java.io.File;
 
 import ru.redsolution.rosyama.data.Rosyama;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.util.Log;
 
 /**
  * Диалог добавления фотографии со вспомогательными функциями.
  * 
  * @author alexander.ivanov
  * 
  */
 public class PhotoDialogHelper extends ItemizedDialogBuilder implements
 		ItemizedDialogClickListener {
 	/**
 	 * Запрошенный URL.
 	 */
 	private static final String SAVED_REQUESTED_URI = "SAVED_REQUESTED_URI";
 
 	/**
 	 * Номер варианта с созданием фотографии.
 	 */
 	private static final int SOURCE_MAKE_PHOTO_ID = 0;
 
 	/**
 	 * Номер варианта с выбором изображения из галереи.
 	 */
 	private static final int SOURCE_SELECT_PHOTO_ID = 1;
 
 	/**
 	 * Код запроса создания фото.
 	 */
 	private static final int ACTIVITY_CAPTURE_IMAGE_REQUEST_CODE = 0;
 
 	/**
 	 * Код запроса выбора фото.
 	 */
 	private static final int ACTIVITY_SELECT_IMAGE_REQUEST_CODE = 1;
 
 	/**
 	 * Приложение.
 	 */
 	private final Rosyama rosyama;
 
 	/**
 	 * Менеджер местоположения.
 	 */
 	private final LocationManager locationManager;
 
 	/**
 	 * URL изображения.
 	 */
 	private Uri requestedUri;
 
 	public PhotoDialogHelper(Activity activity, int dialogId,
 			Bundle savedInstanceState) {
 		super(activity, null, dialogId, activity
 				.getString(R.string.photo_source), new String[] {
 				activity.getString(R.string.make_photo),
 				activity.getString(R.string.select_photo), });
 		rosyama = (Rosyama) activity.getApplication();
 		locationManager = (LocationManager) activity
 				.getSystemService(Context.LOCATION_SERVICE);
 		requestedUri = null;
 		if (savedInstanceState != null) {
 			String stringUri = savedInstanceState
 					.getString(SAVED_REQUESTED_URI);
 			if (stringUri != null)
 				requestedUri = Uri.parse(stringUri);
 		}
 		setListener(this);
 	}
 
 	public void onSaveInstanceState(Bundle outState) {
 		outState.putString(SAVED_REQUESTED_URI, requestedUri == null ? null
 				: requestedUri.toString());
 	}
 
 	public Uri getResultUri(int requestCode, int resultCode, Intent data) {
 		switch (requestCode) {
 		case ACTIVITY_CAPTURE_IMAGE_REQUEST_CODE:
 			locationManager.removeUpdates(rosyama);
 			if (resultCode != Activity.RESULT_OK)
 				requestedUri = null;
 			else {
 				return requestedUri;
 			}
 			break;
 		case ACTIVITY_SELECT_IMAGE_REQUEST_CODE:
 			if (resultCode == Activity.RESULT_OK) {
 				Uri selectedImage = data.getData();
 				Log.i(this.toString(), selectedImage.toString());
 				String[] filePathColumn = { MediaStore.Images.Media.DATA };
 				Cursor cursor = activity.getContentResolver().query(
 						selectedImage, filePathColumn, null, null, null);
				if (cursor == null)
					return null;
 				cursor.moveToFirst();
 				String path = cursor.getString(cursor
 						.getColumnIndex(filePathColumn[0]));
 				Log.i(this.toString(), path);
 				cursor.close();
 				Log.i(this.toString(), Uri.fromFile(new File(path)).toString());
 				return Uri.fromFile(new File(path));
 			}
 			break;
 		}
 		return null;
 	}
 
 	@Override
 	public void onSelect(ItemizedDialogBuilder builder, int selected) {
 		Intent intent;
 		switch (selected) {
 		case SOURCE_MAKE_PHOTO_ID:
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 0, 0, rosyama);
			locationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 0, 0, rosyama);
 			requestedUri = Rosyama.getNextJpegUri();
 			intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 			intent.putExtra(MediaStore.EXTRA_OUTPUT, requestedUri);
 			activity.startActivityForResult(intent,
 					ACTIVITY_CAPTURE_IMAGE_REQUEST_CODE);
 			break;
 		case SOURCE_SELECT_PHOTO_ID:
			intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			activity.startActivityForResult(
					Intent.createChooser(intent, "Select Picture"),
 					ACTIVITY_SELECT_IMAGE_REQUEST_CODE);
			// intent = new Intent(
			// Intent.ACTION_PICK,
			// android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			// activity.startActivityForResult(intent,
			// ACTIVITY_SELECT_IMAGE_REQUEST_CODE);
 			break;
 		}
 	}
 
 	@Override
 	public void onCancel(ItemizedDialogBuilder builder) {
 	}
 }
