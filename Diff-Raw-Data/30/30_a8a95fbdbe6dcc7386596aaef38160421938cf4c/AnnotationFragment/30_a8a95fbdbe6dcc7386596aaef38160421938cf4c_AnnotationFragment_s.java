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
 
 import java.util.ArrayList;
 
 import story.book.controller.StoryCreationController;
 import story.book.controller.StoryReadController;
 import story.book.model.Annotation;
 import story.book.model.AudioIllustration;
 import story.book.model.Illustration;
 import story.book.model.StoryFragment;
 import story.book.model.TextIllustration;
 import android.annotation.TargetApi;
 import android.app.ActionBar.LayoutParams;
 import android.app.Fragment;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.provider.MediaStore;
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
 public class AnnotationFragment extends Fragment {
 	StoryReadController SRC;
 	StoryFragment SF;
 	ArrayList<Pair<View, Annotation>> annotationList;
 	View rootView;
 	int nextFragmentID;
 	StoryCreationController SCC;
 
 	String author;
 	Boolean editMode = true;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		setHasOptionsMenu(true);
		View rootView = inflater.inflate(R.layout.annotation_fragment, container, false);
 
 		author = StoryApplication.getNickname();
 		SRC = ((StoryFragmentReadActivity)this.getActivity()).getController();
		SF = SRC.getStartingFragment();
 		SCC = new StoryCreationController();
 		annotationList = new ArrayList<Pair<View, Annotation>>();
 		
 		displayAnnotations(SF, rootView);
 		return rootView;
 
 	}
 	
 	@Override
 	public void onResume() {
 		getAnnotations();
 		displayAnnotations(SF, rootView);
 	}
 	
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		// Inflate the menu items for use in the action bar
 		super.onCreateOptionsMenu(menu, inflater);
 		inflater.inflate(R.menu.add_annotation_menu, menu);
 
 	}
 
 	Uri auri;
 	private enum Actions {PHOTO, VIDEO}
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.text:
			addNewTextAnnotation(this.getActivity().findViewById(R.id.reading_fragment));
 			return true;
 		case R.id.take_photo:
 			Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 			auri = SCC.getFreeUri(".jpg");
 			i.putExtra(MediaStore.EXTRA_OUTPUT, auri);
 			startActivityForResult(i, Actions.PHOTO.ordinal());
 			return true;
 		case R.id.addGalleryPhoto:
 			i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
 			startActivityForResult(i, Actions.PHOTO.ordinal());
 			return true;
 		
 		case R.id.audio:
 			AudioIllustration audio = new AudioIllustration();
 //			annotationList.add(new Pair<View, Annotation>(audio.getView(editMode), audio));
 			return true;
 
 		case R.id.video:
 
 			return true;
 
 		case R.id.record_video:
 			i = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
 			i.putExtra(MediaStore.EXTRA_OUTPUT, SCC.getFreeUri(".mp4"));
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
 
 		annotationList = new ArrayList<Pair<View, Annotation>>();
 		for (Annotation i : annotations) {
 			annotationList.add(new Pair<View, Annotation>(i.getView(editMode), i));
 		}
 	}
 	
 	/**
 	 * displayAnnotations() displays all annotations as views 
 	 * by getting them from the corresponding fragment 
 	 * 
 	 * @param StoryFragment 	StoryFragment object
 	 * @param View	 where annotations will be displayed
 	 */
 	private void displayAnnotations(StoryFragment SF, View rootView) {
 		
		RelativeLayout layout = (RelativeLayout) rootView.findViewById(R.id.reading_fragment);
		ArrayList<Annotation> annotations = SF.getAnnotations();
		
 		int position = 0;
 		if (annotationList.isEmpty() == false) {
 			// Display illustrations
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
 //				ImageIllustration image = new ImageIllustration(auri);
 //				annotationList.add(new Pair<View, Annotation>(image.getView(editMode), image));
 				update();
 			}
 			if(requestCode == Actions.VIDEO.ordinal()) {
 //				VideoIllustration video = new VideoIllustration(auri);
 //				annotationList.add(new Pair<View, Annotation>(video.getView(editMode), video));
 				update();
 			}
 		}
 	}
 	
 	/**
 	 * update() removes all views in the annotation fragment and redisplays them to show changes
 	 */
 	public void update() {
		((ViewGroup) this.getView()).removeAllViews();
 		displayAnnotations(SF, rootView);
 	}
 	
 	/**
 	 * addNewTextIllustration() creates a new EditText for users to enter the text
 	 * for a TextIllustration.
 	 */
 	private void addNewTextAnnotation(View v) {
 		// TODO Auto-generated method stub
 		EditText newText = new EditText(v.getContext());
 		newText.setHint("Enter text here");
 		annotationList.add(new Pair<View, Annotation>(newText, null));
		update();
 	}
 
 	/**
 	 * Loads the annotations for the fragment; used after a decision branch
 	 * has been selected in the <code>ReadingFragment</code>
 	 */
 	public void getAnnotations() {
 		SF = SRC.getStoryFragment((StoryFragmentReadActivity.SF).getFragmentID());
 		update();
 	}
 	
 	/**
 	 * saveAnnotation() saves any new annotations
 	 */
 	public void saveFragment() {
 		int top = annotationList.size();
 		for (int i = 0; i < top; i++) {
 			if (annotationList.get(i).second == null) {
 				// Saving a text illustration
 				String illString = ((EditText)annotationList.get(i).first).getText().toString();
 				TextIllustration text = new TextIllustration(illString);
 				if(illString.length() > 0) {
 					Annotation <TextIllustration> a = new Annotation<TextIllustration>(author, text);
 					SF.addAnnotation(a);
 				}
 				else {
 					annotationList.remove(i);
 				}
 	
 			}
 			else {
 				// Saving an audio, image, or view illustration
 //				Annotation<?> a = new Annotation(author, annotationList.get(i).second);
 //				SF.addAnnotation((annotationList.get(i)));
 			}
 		}
 	}
 }
