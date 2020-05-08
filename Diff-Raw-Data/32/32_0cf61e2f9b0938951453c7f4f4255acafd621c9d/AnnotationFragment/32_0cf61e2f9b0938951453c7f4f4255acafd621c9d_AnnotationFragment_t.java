 /* CMPUT301F13T06-Adventure Club: A choose-your-own-adventure story platform
  * Copyright (C) 2013 Alexander Cheung, Jessica Surya, Vina Nguyen, Anthony Ou,
  * Nancy Pham-Nguyen
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package story.book.view;
 
 import java.io.File;
 import java.util.ArrayList;
 
 import story.book.controller.FragmentCreationController;
 import story.book.controller.StoryReadController;
 import story.book.model.Annotation;
 import story.book.model.AudioIllustration;
 import story.book.model.Illustration;
 import story.book.model.ImageIllustration;
 import story.book.model.StoryFragment;
 import story.book.model.TextIllustration;
 import story.book.model.VideoIllustration;
 import android.annotation.TargetApi;
 import android.app.ActionBar.LayoutParams;
 import android.app.Fragment;
 import android.content.Intent;
 import android.database.Cursor;
 import android.hardware.Camera.Size;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.text.InputType;
 import android.util.Log;
 import android.util.Pair;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.EditText;
 import android.widget.RelativeLayout;
 
 /**
  * Annotation fragment is the right tab in <code>StoryFragmentReadActivity</code> 
  * This is the view for displaying all image annotations posted by users on the 
  * currently open in <code>Reading Fragment</code> (reading tab)
  * 
  * @author Jessica Surya
  *
  */
 @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
 public class AnnotationFragment extends Fragment implements StoryView {
 	StoryReadController SRC;
 	StoryFragment SF;
 	ArrayList<Pair<View, Annotation>> annotationList;
 	View rootView;
 	int nextFragmentID;
 	FragmentCreationController FCC;
 
 	String author;
 	Boolean editMode = true;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		setHasOptionsMenu(true);
 		rootView = inflater.inflate(R.layout.annotation_fragment, container, false);
 
 		author = StoryApplication.getNickname();
 		SRC = ((StoryFragmentReadActivity)this.getActivity()).getController();
 		SF = SRC.getStartingFragment();
 		FCC = new FragmentCreationController(SF.getFragmentID());
 
 		annotationList = new ArrayList<Pair<View, Annotation>>();
 		SF.addView(this);
 		getFragmentAnnotations();
 		loadAnnotations();
 		displayAnnotations();
 
 		if (savedInstanceState != null && savedInstanceState.containsKey("cameraImageUri")) {
 			auri = Uri.parse(savedInstanceState.getString("cameraImageUri"));
 		}
 		if(auri != null)
 			Log.v("some uri", auri.toString());
 		return rootView;
 
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		saveAnnotations();
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		getFragmentAnnotations();
 		loadAnnotations();
 		displayAnnotations();
 	}
 
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		// Inflate the menu items for use in the action bar
 		super.onCreateOptionsMenu(menu, inflater);
 		inflater.inflate(R.menu.add_annotation_menu, menu);
 
 	}
 
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		if (auri != null) {
 			outState.putString("cameraImageUri", auri.toString());
 		}
 	}
 
 	Uri auri;
 	private enum Actions {PHOTO, VIDEO, GALLERY}
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.text:
 			saveAnnotations();
 			addNewTextAnnotation();
 			return true;
 		case R.id.take_photo:
 			Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 			auri = FCC.getFreeUri(".jpg");
 			i.putExtra(MediaStore.EXTRA_OUTPUT, auri);
 			startActivityForResult(i, Actions.PHOTO.ordinal());
 			return true;
 		case R.id.addGalleryPhoto:
 			i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
 			startActivityForResult(i, Actions.GALLERY.ordinal());
 			return true;
 		case R.id.audio:
 			AudioIllustration audio = new AudioIllustration(FCC.getFreeUri(".3gp"));
 			annotationList.add(new Pair<View, Annotation>(
 					audio.getView(FCC.getStoryPath(),editMode,this.getActivity()), 
 					new Annotation(StoryApplication.getNickname(),audio)));
 			return true;
 
 		case R.id.video:
 
 			return true;
 
 		case R.id.record_video:
 			i = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
 			i.putExtra(MediaStore.EXTRA_OUTPUT, FCC.getFreeUri(".mp4"));
 			i.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
 			startActivityForResult(i, Actions.VIDEO.ordinal());
 			return true;
 
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	/**
 	 * loadAnnotations() loads annotation views from the current story fragment
 	 */
 	private void loadAnnotations() {
 		ArrayList<Annotation> annotations = SF.getAnnotations();
 
 		annotationList.clear();
 		for (Annotation i : annotations) {
 			annotationList.add(new Pair<View, Annotation>(i.getView(SRC.getStoryPath(),editMode,this.getActivity()), i));
 		}
 	}
 
 	/**
 	 * displayAnnotations() displays all annotations as views 
 	 * by getting them from the corresponding fragment 
 	 * 
 	 * @param StoryFragment 	StoryFragment object
 	 * @param View	 where annotations will be displayed
 	 */
 	private void displayAnnotations() {
 
 		RelativeLayout layout = (RelativeLayout) rootView.findViewById(R.id.annotation_fragment);
 
 		int position = 0;
 		if (annotationList.isEmpty() == false) {
 			// Display illustrations
 			((ViewGroup) layout).removeAllViews();
 			for (Pair <View, Annotation> t: annotationList) {
 				t.first.setId(position + 1);
 				RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(LayoutParams.
 						MATCH_PARENT,LayoutParams.WRAP_CONTENT); 
 				p.addRule(RelativeLayout.BELOW, position);
 				t.first.setLayoutParams(p);
 
 				((ViewGroup) layout).addView(t.first, p);
 				position++;
 			}
 		}
 	}
 
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if(resultCode == android.app.Activity.RESULT_OK ) {
 			if(requestCode == Actions.PHOTO.ordinal()) {
 				ImageIllustration image = new ImageIllustration(auri);
 				annotationList.add(
 						new Pair<View, Annotation>(image.getView(FCC.getStoryPath(), editMode, this.getActivity()), 
 								new Annotation(StoryApplication.getNickname(),image)));
 				saveAnnotations();
 			}
 			if(requestCode == Actions.VIDEO.ordinal()) {
 				VideoIllustration video = new VideoIllustration(auri);
 				annotationList.add(
 						new Pair<View, Annotation>(video.getView(FCC.getStoryPath(), editMode, this.getActivity()), 
 								new Annotation(StoryApplication.getNickname(),video)));
 				saveAnnotations();
 			}
 			if(requestCode == Actions.GALLERY.ordinal()) {
 				File f;
 				Cursor cursor = this.getActivity().getContentResolver().query(data.getData(), null, null, null, null);
 				if (cursor == null) { // Source is Dropbox or other similar local file path
 					f = new File((data.getData().getPath()));
 				} else { 
 					cursor.moveToFirst(); 
 					int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
 					f = new File(cursor.getString(idx));
 				}
 				ImageIllustration image = new ImageIllustration(Uri.fromFile(f), FCC.getFreeUri(".jpg"));
 				annotationList.add(
 						new Pair<View, Annotation>(image.getView(FCC.getStoryPath(), editMode, this.getActivity()), 
 								new Annotation(StoryApplication.getNickname(),image)));
 				saveAnnotations();
 			}
 		}
 	}
 
 
 	/**
 	 * addNewTextIllustration() creates a new EditText for users to enter the text
 	 * for a TextIllustration.
 	 */
 	private void addNewTextAnnotation() {
 		// TODO Auto-generated method stub
 		EditText newText = new EditText(rootView.getContext());
 		newText.setHint("Enter text here");
 		annotationList.add(new Pair<View, Annotation>(newText, null));
 		displayAnnotations();
 	}
 
 	/**
 	 * Loads the annotations for the fragment; used after a decision branch
 	 * has been selected in the <code>ReadingFragment</code>
 	 */
 	public void getFragmentAnnotations() {
 		SF = SRC.getStoryFragment((StoryFragmentReadActivity.SF).getFragmentID());
 
 	}
 
 	/**
 	 * saveAnnotation() saves any new annotations
 	 */
 	public void saveAnnotations() {
 		int top = annotationList.size();
 		Log.d("DEBUG: total number of annotations", String.valueOf(top));
 
 		if(annotationList.size() > SF.getAnnotations().size()) {
 
 			Pair<View, Annotation> i = annotationList.get(top-1);
 
 			if (i.second == null) {
 				// Saving a text illustration
 				String illString = ((EditText)i.first).getText().toString();
 				Log.d("DEBUG: Text annotation to be saved", String.valueOf(illString));
 				if(illString.length() > 0) {
 					TextIllustration text = new TextIllustration(illString);
					Annotation a = new Annotation(author, text);
 					FCC.addAnnotation(a);
 				}
 			}
 			else {
 				// Saving an audio, image, or view illustration
 				Annotation a = new Annotation(author, i.second.getIllustration());
 				FCC.addAnnotation(i.second);
 			}
 		}
 		FCC.saveStory();
 	}
 
 	/**
 	 * update(SF); redisplays annotations to show changes to model
 	 */
 	@Override
 	public void update(Object model) {
 		// TODO Auto-generated method stub
 
 		getFragmentAnnotations();
 		loadAnnotations();
 		displayAnnotations();
 	}
 
 	private void removeViews() {
 		rootView = this.getView().findViewById(R.id.reading_fragment);
 		((ViewGroup) rootView).removeAllViews();
 	}
 }
