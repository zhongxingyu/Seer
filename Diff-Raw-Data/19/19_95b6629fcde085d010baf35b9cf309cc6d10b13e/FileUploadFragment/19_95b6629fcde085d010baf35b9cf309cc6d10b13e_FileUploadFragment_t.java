 package org.geekosphere.zeitgeist.fragment;
 
 import java.io.File;
 
 import org.geekosphere.zeitgeist.R;
 import org.geekosphere.zeitgeist.broadcastreceiver.LoadingBroadcastReceiver;
 import org.geekosphere.zeitgeist.net.WebRequestBuilder;
 import org.geekosphere.zeitgeist.processor.ZGItemProcessor;
 import org.geekosphere.zeitgeist.util.Util;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import android.annotation.TargetApi;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.provider.MediaStore;
 import android.support.v4.content.CursorLoader;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.Toast;
 import at.diamonddogs.data.dataobjects.WebRequest;
 import at.diamonddogs.service.net.HttpServiceAssister;
 import at.diamonddogs.service.processor.ImageProcessor;
 import at.diamonddogs.service.processor.ServiceProcessor;
 
 import com.actionbarsherlock.app.SherlockFragment;
 
 public class FileUploadFragment extends SherlockFragment implements OnClickListener {
 	private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadFragment.class);
 	private static final float SCALEFACTOR = 512.0f;
 
 	private Uri imageUri;
 	private ImageView image;
 	private HttpServiceAssister assister;
	private Handler handler;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		assister = new HttpServiceAssister(getActivity());
		handler = new Handler();
 	}
 
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View v = inflater.inflate(R.layout.fileuploadfragment, container, false);
 		image = (ImageView) v.findViewById(R.id.fileuploadfragment_iv_preview);
 		v.findViewById(R.id.fileuploadfragment_ib_upload).setOnClickListener(this);
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
 			image.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
 		return v;
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		assister.bindService();
 		setUriFromIntent();
 	}
 
 	private void setUriFromIntent() {
 
 		new Thread() {
 			@Override
 			public void run() {
 				Intent i = getActivity().getIntent();
 				imageUri = (Uri) i.getParcelableExtra(Intent.EXTRA_STREAM);
 				if (imageUri == null) {
 					imageUri = i.getData();
 				}
 				if (imageUri == null) {
 					if (i.hasExtra("android.intent.extra.TEXT")) {
 						imageUri = Uri.parse(i.getStringExtra("android.intent.extra.TEXT"));
 					} else if (i.hasExtra("android.intent.extra.SUBJECT")) {
 						imageUri = Uri.parse(i.getStringExtra("android.intent.extra.SUBJECT"));
 					}
 				}
 				LOGGER.info("Data URI: " + imageUri);
 				if (imageUri != null) {
 					if (imageUri.getScheme().contains("http")) {
 						downloadImage(imageUri);
						postSafly(new Runnable() {
 							@Override
 							public void run() {
 								image.setImageBitmap(getScaledBitmapFromFile(ImageProcessor.getImageFileUrl(imageUri.toString(),
 										getActivity())));
 							}
 						});
 					} else {
						postSafly(new Runnable() {
 							@Override
 							public void run() {
 								Bitmap previewImage = getScaledBitmapFromLocalUri(getActivity());
 								if (previewImage == null) {
 									Toast.makeText(getActivity(), R.string.fragment_fileuploadfragment_couldnotloadimage, Toast.LENGTH_LONG)
 											.show();
 									getActivity().finish();
 								} else {
 									image.setImageBitmap(previewImage);
 								}
 							}
 						});
 					}
 				} else {
 					Toast.makeText(getActivity(), R.string.fileuploadfragment_noimagedata, Toast.LENGTH_SHORT).show();
 					if (!isDetached()) {
 						getActivity().finish();
 					}
 				}
 			}
 		}.start();
	}
 
	private void postSafly(Runnable r) {
		if (handler != null && !isDetached() && getActivity() != null) {
			handler.post(r);
		}
 	}
 
 	private void downloadImage(Uri u) {
		postSafly(new Runnable() {
 			@Override
 			public void run() {
 				Util.showCentricToast(getActivity(), R.string.fragment_fileuploadfragment_attemptingdownloadfromsource, Toast.LENGTH_SHORT);
 			}
 		});
 		LoadingBroadcastReceiver.getInstance().sendLoadingIntent(getActivity());
 		String url = u.toString();
 		WebRequest wr = ImageProcessor.getDefaultImageRequest(url);
 		assister.runSynchronousWebRequest(wr, new ImageProcessor());
 		LoadingBroadcastReceiver.getInstance().sendLoadingDoneIntent(getActivity());
 	}
 
 	private Bitmap getScaledBitmapFromFile(String filePath) {
 		Bitmap src = BitmapFactory.decodeFile(filePath);
 		return scaleImage(src);
 	}
 
 	private Bitmap getScaledBitmapFromLocalUri(Context c) {
 		String filePath = getRealPathFromURI(c, imageUri);
 		if (filePath == null) {
 			return null;
 		}
 		Bitmap src = BitmapFactory.decodeFile(filePath);
 		return scaleImage(src);
 	}
 
 	private Bitmap scaleImage(Bitmap src) {
 		int scaledHight = (int) (src.getHeight() * (SCALEFACTOR / src.getWidth()));
 		return Bitmap.createScaledBitmap(src, (int) SCALEFACTOR, scaledHight, true);
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		assister.unbindService();
 	}
 
 	@Override
 	public void onClick(View v) {
 		uploadCurrentImage();
 	}
 
 	private void uploadCurrentImage() {
 		WebRequest wr = getWebRequestAccordingToUri();
 		Toast.makeText(getActivity(), R.string.fragment_fileuploadfragment_startingimageupload, Toast.LENGTH_SHORT).show();
 
 		LoadingBroadcastReceiver.getInstance().sendLoadingIntent(getActivity());
 
 		assister.runWebRequest(new Handler() {
 			@Override
 			public void handleMessage(Message msg) {
 				if (msg.what == ZGItemProcessor.ID) {
 					LoadingBroadcastReceiver.getInstance().sendLoadingDoneIntent(getActivity());
 					// TODO: 500 - error
 					int statusCode = msg.getData().getInt(ServiceProcessor.BUNDLE_EXTRA_MESSAGE_HTTPSTATUSCODE);
 					if (msg.arg1 == ServiceProcessor.RETURN_MESSAGE_OK) {
 						getActivity().finish();
 					} else {
 						Toast.makeText(getActivity(), getString(R.string.fileuploadfragment_erroruploading), Toast.LENGTH_SHORT).show();
 					}
 				}
 			}
 		}, wr, new ZGItemProcessor());
 	}
 
 	private WebRequest getWebRequestAccordingToUri() {
 		WebRequestBuilder b = new WebRequestBuilder(getActivity());
 		WebRequest wr;
 		if (imageUri.getScheme().contains("http")) {
 			LOGGER.info("Using URL upload (less traffic)");
 			wr = b.newItem().addUploadUrl(new String[] { imageUri.toString() }).build();
 		} else {
 			LOGGER.info("Using binary upload (more traffic)");
 			wr = b.newItem().addUploadFile(new File[] { new File(getRealPathFromURI(getActivity(), imageUri)) }).build();
 		}
 		wr.setReadTimeout(10000);
 		wr.setConnectionTimeout(5000);
 		return wr;
 	}
 
 	private String getRealPathFromURI(Context c, Uri contentUri) {
 		if (contentUri.getScheme().contains("http")) {
 			return ImageProcessor.getImageFileUrl(contentUri.toString(), getActivity());
 		} else {
 			String[] proj = { MediaStore.Images.Media.DATA };
 			CursorLoader loader = new CursorLoader(getActivity(), contentUri, proj, null, null, null);
 			Cursor cursor = loader.loadInBackground();
 			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
 			cursor.moveToFirst();
 			return cursor.getString(column_index);
 		}
 	}
 }
