 /*
  *	Copyright (c) 2013 Andrew Fontaine, James Finlay, Jesse Tucker, Jacob Viau, and
  * 	Evan DeGraff
  *
  * 	Permission is hereby granted, free of charge, to any person obtaining a copy of
  * 	this software and associated documentation files (the "Software"), to deal in
  * 	the Software without restriction, including without limitation the rights to
  * 	use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
  * 	the Software, and to permit persons to whom the Software is furnished to do so,
  * 	subject to the following conditions:
  *
  * 	The above copyright notice and this permission notice shall be included in all
  * 	copies or substantial portions of the Software.
  *
  * 	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * 	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
  * 	FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
  * 	COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
  * 	IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
  * 	CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package ca.cmput301f13t03.adventure_datetime.view;
 
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentStatePagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.*;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import ca.cmput301f13t03.adventure_datetime.R;
 import ca.cmput301f13t03.adventure_datetime.model.Image;
 import ca.cmput301f13t03.adventure_datetime.model.Interfaces.ICurrentFragmentListener;
 import ca.cmput301f13t03.adventure_datetime.model.StoryFragment;
 import ca.cmput301f13t03.adventure_datetime.serviceLocator.Locator;
 
 import java.io.File;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * View accessed by clicking image in filmstrip. Provides image/video in full screen.
  *
  * @author James Finlay
  *
  */
 public class FullScreen_Image extends FragmentActivity implements ICurrentFragmentListener {
     private static final String TAG = "FragmentActivity";
     public static final String TAG_AUTHOR = "yolo.swag.AuthorEh";
     public static final int GALLERY = 42;
     public static final int CAMERA = 23;
 
     private StoryFragment _fragment;
     private ViewPager _viewPager;
     private StoryPagerAdapter _pageAdapter;
     private Uri _newImage;
 
     @Override
     public void OnCurrentFragmentChange(StoryFragment newFragment) {
         _fragment = newFragment;
         setUpView();
     }
 
     private void setUpView() {
         if (_fragment == null) return;
         if (_pageAdapter == null) return;
 
         Button gallery = (Button) findViewById(R.id.gallery);
         Button camera = (Button)  findViewById(R.id.camera);
         Button delete = (Button)  findViewById(R.id.action_delete);
 
         gallery.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent i = new Intent(Intent.ACTION_PICK,
                         android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                 startActivityForResult(i, GALLERY);
             }
 
 
         });
         camera.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 File picDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                         "adventure.datetime");
                 if(!picDir.exists()) picDir.mkdirs();
                 File pic = new File(picDir.getPath(), File.separator + _fragment.getFragmentID().toString()
                         + "-" + _fragment.getStoryMedia().size());
                 _newImage = Uri.fromFile(pic);
                 Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                 i.putExtra(MediaStore.EXTRA_OUTPUT, _newImage);
                 startActivityForResult(i, CAMERA);
             }
         });
 
         // turn off author buttons if necessary
         if (!getIntent().getBooleanExtra(TAG_AUTHOR, false)) {
             gallery.setVisibility(View.GONE);
             camera.setVisibility(View.GONE);
             delete.setVisibility(View.GONE);
         }
 
         _pageAdapter.setIllustrations(_fragment.getStoryMedia(), getIntent().getBooleanExtra(TAG_AUTHOR, false));
     }
     @Override
     public void onSaveInstanceState(Bundle outState) {}
 
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         // Fullscreen
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                 WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
         setContentView(R.layout.fullscreen_image);
 
 
         _pageAdapter = new StoryPagerAdapter(getSupportFragmentManager());
         _viewPager = (ViewPager) findViewById(R.id.author_pager);
         _viewPager.setAdapter(_pageAdapter);
 
         setUpView();
     }
 
     @Override
     public void onResume() {
         Locator.getPresenter().Subscribe(this);
         super.onResume();
     }
     @Override
     public void onPause() {
         Locator.getPresenter().Unsubscribe(this);
         super.onPause();
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode,
                                  Intent imageReturnedIntent) {
         super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
 
         if (resultCode != RESULT_OK) {
            if(requestCode == CAMERA) {
                _fragment.removeMedia(_fragment.getStoryMedia().size() - 1);
            }
             return;
         }
 
         switch (requestCode) {
             case GALLERY:
                 try {
                     InputStream is = getContentResolver().openInputStream(imageReturnedIntent.getData());
                     Bitmap bit = BitmapFactory.decodeStream(is);
                     Image selectedImage = new Image(Image.compressBitmap(bit, 85));
                     _fragment.addMedia(selectedImage);
                     Locator.getAuthorController().saveStory();
                 }
                 catch(Exception e) {
                     Log.e(TAG, "Error getting new image", e);
                 }
                 break;
             case CAMERA:
                 try {
                     InputStream is = getContentResolver().openInputStream(_newImage);
                     Bitmap bit = BitmapFactory.decodeStream(is);
                     Image selectedImage = new Image(Image.compressBitmap(bit, 85));
                     _fragment.addMedia(selectedImage);
                     Locator.getAuthorController().saveStory();
                 }
                 catch(Exception e) {
                     Log.e(TAG, "Error getting new image", e);
                 }
                 break;
         }
     }
 
     private class StoryPagerAdapter extends FragmentStatePagerAdapter {
 
         private List<Image> _illustrations;
         private boolean _author;
 
         public StoryPagerAdapter(FragmentManager fm) {
             super(fm);
             _illustrations = new ArrayList<Image>();
         }
 
         public void setIllustrations(List<Image> illustrationIDs, boolean author) {
             _illustrations = illustrationIDs;
             _author = author;
             notifyDataSetChanged();
         }
 
         @Override
         public Fragment getItem(int pos) {
             IllustrationFragment frag = new IllustrationFragment();
             frag.init(_illustrations.get(pos), pos, _illustrations.size(), _author);
 
             return frag;
         }
 
         @Override
         public int getCount() {
             return _illustrations.size();
         }
 
     }
     public static class IllustrationFragment extends Fragment {
 
         private View _rootView;
         private Image _sID;
         private String _position;
         private boolean _author;
 
         public void onCreate(Bundle bundle) {
             super.onCreate(bundle);
         }
         public void init(Image id, int position, int total, boolean author) {
             _sID = id;
             _position = (position+1) + "/" + total;
             _author = author;
             setUpView();
         }
         @Override
         public View onCreateView(LayoutInflater inflater,
                                  ViewGroup container, Bundle savedInstanceState) {
 
             _rootView = inflater.inflate(R.layout.fullscreen_illustration,
                     container, false);
 
             setUpView();
 
             return _rootView;
         }
         private void setUpView() {
             if (_sID == null) return;
             if (_rootView == null) return;
 
             /** Layout items **/
             ImageView image = (ImageView) _rootView.findViewById(R.id.image);
 
             TextView counter = (TextView) _rootView.findViewById(R.id.count);
 
             // TODO: Set counter by location
 
 
 
 
             //bit = BitmapFactory.decodeFile(pic.getAbsolutePath(), opts);
             image.setImageBitmap(_sID.decodeBitmap());
             counter.setText(_position);
 
         }
 
     }
 }
