 package org.wikimedia.commons.contributions;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.drawable.BitmapDrawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.support.v4.widget.CursorAdapter;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.*;
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 import com.nostra13.universalimageloader.core.DisplayImageOptions;
 
 import java.io.*;
 import java.util.*;
 
 
 import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
 import org.wikimedia.commons.*;
 import org.wikimedia.commons.R;
 
 public class ContributionsListFragment extends SherlockFragment {
 
     private final static int SELECT_FROM_GALLERY = 1;
     private final static int SELECT_FROM_CAMERA = 2;
 
     private GridView contributionsList;
     private TextView waitingMessage;
     private TextView emptyMessage;
 
     private ContributionsListAdapter contributionsAdapter;
 
     private DisplayImageOptions contributionDisplayOptions;
     private Cursor allContributions;
 
     private static class ContributionViewHolder {
         final ImageView imageView;
         final TextView titleView;
         final TextView stateView;
         final TextView seqNumView;
         final ProgressBar progressView;
 
         String url;
 
         ContributionViewHolder(View parent) {
             imageView = (ImageView)parent.findViewById(R.id.contributionImage);
             titleView = (TextView)parent.findViewById(R.id.contributionTitle);
             stateView = (TextView)parent.findViewById(R.id.contributionState);
             seqNumView = (TextView)parent.findViewById(R.id.contributionSequenceNumber);
             progressView = (ProgressBar)parent.findViewById(R.id.contributionProgress);
         }
     }
 
     private class ContributionsListAdapter extends CursorAdapter {
 
         public ContributionsListAdapter(Context context, Cursor c, int flags) {
             super(context, c, flags);
         }
 
         @Override
         public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
             View parent = getActivity().getLayoutInflater().inflate(R.layout.layout_contribution, viewGroup, false);
             parent.setTag(new ContributionViewHolder(parent));
             return parent;
         }
 
         @Override
         public void bindView(View view, Context context, Cursor cursor) {
             // hack: hide the 'first sync' message once we've loaded a cell
             clearSyncMessage();
 
             final ContributionViewHolder views = (ContributionViewHolder)view.getTag();
             Contribution contribution = Contribution.fromCursor(cursor);
 
             String actualUrl = TextUtils.isEmpty(contribution.getImageUrl()) ? contribution.getLocalUri().toString() : contribution.getThumbnailUrl(320);
 
             if(views.url == null || !views.url.equals(actualUrl)) {
                 if(actualUrl.startsWith("http")) {
                     MediaWikiImageView mwImageView = (MediaWikiImageView)views.imageView;
                     mwImageView.setMedia(contribution, ((CommonsApplication) getActivity().getApplicationContext()).getImageLoader());
                     // FIXME: For transparent images
                 } else {
                     com.nostra13.universalimageloader.core.ImageLoader.getInstance().displayImage(actualUrl, views.imageView, contributionDisplayOptions, new SimpleImageLoadingListener() {
 
                         @Override
                         public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                             if(loadedImage.hasAlpha()) {
                                 views.imageView.setBackgroundResource(android.R.color.white);
                             }
                             views.seqNumView.setVisibility(View.GONE);
                         }
 
                     });
                 }
                 views.url = actualUrl;
             }
 
             BitmapDrawable actualImageDrawable = (BitmapDrawable)views.imageView.getDrawable();
             if(actualImageDrawable != null && actualImageDrawable.getBitmap() != null && actualImageDrawable.getBitmap().hasAlpha()) {
                 views.imageView.setBackgroundResource(android.R.color.white);
             } else {
                 views.imageView.setBackgroundDrawable(null);
             }
 
             views.titleView.setText(contribution.getDisplayTitle());
 
             views.seqNumView.setText(String.valueOf(cursor.getPosition() + 1));
             views.seqNumView.setVisibility(View.VISIBLE);
 
             switch(contribution.getState()) {
                 case Contribution.STATE_COMPLETED:
                     views.stateView.setVisibility(View.GONE);
                     views.progressView.setVisibility(View.GONE);
                     views.stateView.setText("");
                     break;
                 case Contribution.STATE_QUEUED:
                     views.stateView.setVisibility(View.VISIBLE);
                     views.progressView.setVisibility(View.GONE);
                     views.stateView.setText(R.string.contribution_state_queued);
                     break;
                 case Contribution.STATE_IN_PROGRESS:
                     views.stateView.setVisibility(View.GONE);
                     views.progressView.setVisibility(View.VISIBLE);
                     long total = contribution.getDataLength();
                     long transferred = contribution.getTransferred();
                     if(transferred == 0 || transferred >= total) {
                         views.progressView.setIndeterminate(true);
                     } else {
                         views.progressView.setProgress((int)(((double)transferred / (double)total) * 100));
                     }
                     break;
                 case Contribution.STATE_FAILED:
                     views.stateView.setVisibility(View.VISIBLE);
                     views.stateView.setText(R.string.contribution_state_failed);
                     views.progressView.setVisibility(View.GONE);
                     break;
             }
 
         }
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         return inflater.inflate(R.layout.fragment_contributions, container, false);
     }
 
     public void setCursor(Cursor cursor) {
         if(allContributions == null) {
             contributionsAdapter = new ContributionsListAdapter(this.getActivity(), cursor, 0);
             contributionsList.setAdapter(contributionsAdapter);
         }
         allContributions = cursor;
         contributionsAdapter.swapCursor(cursor);
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putInt("grid-position", contributionsList.getFirstVisiblePosition());
         outState.putParcelable("lastGeneratedCaptureURI", lastGeneratedCaptureURI);
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         switch(requestCode) {
             case SELECT_FROM_GALLERY:
                 if(resultCode == Activity.RESULT_OK) {
                     Intent shareIntent = new Intent(getActivity(), ShareActivity.class);
                     shareIntent.setAction(Intent.ACTION_SEND);
 
                     shareIntent.setType(getActivity().getContentResolver().getType(data.getData()));
                     shareIntent.putExtra(Intent.EXTRA_STREAM, data.getData());
                     shareIntent.putExtra(UploadService.EXTRA_SOURCE, Contribution.SOURCE_GALLERY);
                     startActivity(shareIntent);
                 }
                 break;
             case SELECT_FROM_CAMERA:
                 if(resultCode == Activity.RESULT_OK) {
                     Intent shareIntent = new Intent(getActivity(), ShareActivity.class);
                     shareIntent.setAction(Intent.ACTION_SEND);
                     Log.d("Commons", "Uri is " + lastGeneratedCaptureURI);
                     shareIntent.setType("image/jpeg"); //FIXME: Find out appropriate mime type
                     shareIntent.putExtra(Intent.EXTRA_STREAM, lastGeneratedCaptureURI);
                     shareIntent.putExtra(UploadService.EXTRA_SOURCE, Contribution.SOURCE_CAMERA);
                     startActivity(shareIntent);
                 }
                 break;
         }
     }
 
     // See http://stackoverflow.com/a/5054673/17865 for why this is done
     private Uri lastGeneratedCaptureURI;
 
     private void reGenerateImageCaptureURI() {
         String storageState = Environment.getExternalStorageState();
         if(storageState.equals(Environment.MEDIA_MOUNTED)) {
 
             String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Commons/images/" + new Date().getTime() + ".jpg";
             File _photoFile = new File(path);
             try {
                 if(_photoFile.exists() == false) {
                     _photoFile.getParentFile().mkdirs();
                     _photoFile.createNewFile();
                 }
 
             } catch (IOException e) {
                 Log.e("Commons", "Could not create file: " + path, e);
             }
 
             lastGeneratedCaptureURI = Uri.fromFile(_photoFile);
         }   else {
             throw new RuntimeException("No external storage found!");
         }
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch(item.getItemId()) {
             case R.id.menu_from_gallery:
                 Intent pickImageIntent = new Intent(Intent.ACTION_GET_CONTENT);
                 pickImageIntent.setType("image/*");
                 startActivityForResult(pickImageIntent,  SELECT_FROM_GALLERY);
                 return true;
             case R.id.menu_from_camera:
                 Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                 reGenerateImageCaptureURI();
                 takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, lastGeneratedCaptureURI);
                 startActivityForResult(takePictureIntent, SELECT_FROM_CAMERA);
                 return true;
             case R.id.menu_settings:
                 Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
                 startActivity(settingsIntent);
                 return true;
             case R.id.menu_about:
                 Intent aboutIntent = new Intent(getActivity(),  AboutActivity.class);
                 startActivity(aboutIntent);
                 return true;
             case R.id.menu_feedback:
                 Intent feedbackIntent = new Intent(Intent.ACTION_SEND);
                 feedbackIntent.setType("message/rfc822");
                 feedbackIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { CommonsApplication.FEEDBACK_EMAIL });
                 feedbackIntent.putExtra(Intent.EXTRA_SUBJECT, String.format(CommonsApplication.FEEDBACK_EMAIL_SUBJECT, CommonsApplication.APPLICATION_VERSION));
                 startActivity(feedbackIntent);
 
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         menu.clear(); // See http://stackoverflow.com/a/8495697/17865
         inflater.inflate(R.menu.fragment_contributions_list, menu);
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setHasOptionsMenu(true);
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         contributionsList = (GridView)getView().findViewById(R.id.contributionsList);
         waitingMessage = (TextView)getView().findViewById(R.id.waitingMessage);
         emptyMessage = (TextView)getView().findViewById(R.id.waitingMessage);
         contributionDisplayOptions = Utils.getGenericDisplayOptions().build();
 
         contributionsList.setOnItemClickListener((AdapterView.OnItemClickListener)getActivity());
         if(savedInstanceState != null) {
             Log.d("Commons", "Scrolling to " + savedInstanceState.getInt("grid-position"));
             lastGeneratedCaptureURI = (Uri) savedInstanceState.getParcelable("lastGeneratedCaptureURI");
             contributionsList.setSelection(savedInstanceState.getInt("grid-position"));
         }
 
         SharedPreferences prefs = this.getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
         String lastModified = prefs.getString("lastSyncTimestamp", "");
         if (lastModified.equals("")) {
             waitingMessage.setVisibility(View.VISIBLE);
         }
     }
 
     private void clearSyncMessage() {
         waitingMessage.setVisibility(View.GONE);
     }
 }
