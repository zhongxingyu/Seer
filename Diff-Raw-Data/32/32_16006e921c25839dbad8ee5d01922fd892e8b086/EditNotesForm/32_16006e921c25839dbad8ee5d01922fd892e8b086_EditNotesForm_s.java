 package com.codesortium.photoswithnotes.view;
 
 import android.content.Context;
 import android.net.Uri;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.codesortium.photoswithnotes.R;
 import com.codesortium.photoswithnotes.db.Category;
 import com.codesortium.photoswithnotes.db.Photo;
 import com.codesortium.photoswithnotes.model.DatabaseModel;
 import com.google.inject.Inject;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.List;
 
 import jsignal.Signal;
 import roboguice.RoboGuice;
 
 /**
  * Created by jim on 30/10/2013.
  */
 public class EditNotesForm extends LinearLayout
 {
     private static final String TAG = EditNotesForm.class.getSimpleName();
 
     @Inject
     private DatabaseModel model;
 
     //event signals sent by this view for listeners to bind to.
 	public final Signal onImageClicked;
 	public final Signal onTitleChanged;
 	public final Signal onNotesChanged;
 
     //Visual elements in view xml.
 	private ImageView imgThumbnail;
 	private EditText txtTitle;
 	private EditText txtNotes;
 
     //Private vars
 
     public EditNotesForm(Context context, AttributeSet attrs)
     {
         super(context, attrs);
 
         //Setup signals
 		onImageClicked = new Signal();
 		onTitleChanged = new Signal(Photo.class, String.class);
 		onNotesChanged = new Signal(Photo.class, String.class);
 
         if(!isInEditMode())
         {
             //Inject dependancies
             RoboGuice.injectMembers(context, this);
 
             //listen for events
             //model.onUpdate.add(this, "populateForm");
         }
     }
 
     @Override
     protected void onFinishInflate()
     {
         super.onFinishInflate();
 
 		imgThumbnail = (ImageView)findViewById(R.id.imgThumbnail);
 		txtTitle = (EditText)findViewById(R.id.txtTitle);
 		txtNotes = (EditText)findViewById(R.id.txtNotes);
 
 		imgThumbnail.setOnClickListener(new OnClickListener()
 		{
 			@Override
 			public void onClick(View v)
 			{
 				onImageClicked.dispatch();
 			}
 		});
 
         populateForm();
     }
 
     public void populateForm()
     {
         if(!isInEditMode())
         {
            imgThumbnail.setImageURI(Uri.fromFile(new File(model.getEditingPhoto().getUrl())));
 
 			txtTitle.setText(model.getEditingPhoto().getTitle());
 			txtNotes.setText(model.getEditingPhoto().getNotes());
         }
     }
 }
