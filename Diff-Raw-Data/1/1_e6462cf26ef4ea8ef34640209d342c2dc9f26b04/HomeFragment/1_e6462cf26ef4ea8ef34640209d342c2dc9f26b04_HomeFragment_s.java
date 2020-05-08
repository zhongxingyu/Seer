 package com.ecn.urbapp.fragments;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import android.annotation.SuppressLint;
 import android.app.Fragment;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 import com.ecn.urbapp.R;
 import com.ecn.urbapp.activities.LoadExternalProjectsActivity;
 import com.ecn.urbapp.activities.LoadLocalProjectsActivity;
 import com.ecn.urbapp.activities.MainActivity;
 import com.ecn.urbapp.db.ElementType;
 import com.ecn.urbapp.db.Material;
 import com.ecn.urbapp.db.MySQLiteHelper;
 import com.ecn.urbapp.syncToExt.Sync;
 import com.ecn.urbapp.utils.Cst;
 import com.ecn.urbapp.utils.Utils;
 
 
 /**
  * This is the fragment used to make the user choose between the differents type of project.
  * 
  * @author	COHENDET Sébastien
  * 			DAVID Nicolas
  * 			GUILBART Gabriel
  * 			PALOMINOS Sylvain
  * 			PARTY Jules
  * 			RAMBEAU Merwan
  * 			
  */
 
 @SuppressLint("SimpleDateFormat")
 public class HomeFragment extends Fragment implements OnClickListener{
 
 	/**
 	 * Image button to launch the photo native application
 	 */
 	private ImageView imageTakePhoto;
 	/**
 	 * Image button to launch the native document browser application
 	 */
 	private ImageView imagePhoto;
 	/**
 	 * Image button launching the activity LoadLocalProject
 	 */
 	private ImageView imageLoadLocal;
 	/**
 	 * Image button launching the activity LoadDistantProject
 	 */
 	private ImageView imageLoadDistant;
 	/**
 	 * The button for synchronizing materials and types from server
 	 */
 	private Button syncMat = null;
 
 	public static ProgressDialog dialogMater;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
 		View v = inflater.inflate(R.layout.layout_home, null);
 
 		
 		
 		imageTakePhoto = (ImageView) v.findViewById(R.id.home_image_newProject_takePhoto);
 		imageTakePhoto.setOnClickListener(this);
 		
 		imagePhoto = (ImageView) v.findViewById(R.id.home_image_newProject_photo);
 		imagePhoto.setOnClickListener(this);
 		
 		imageLoadLocal = (ImageView) v.findViewById(R.id.home_image_loadLocalProject);
 		imageLoadLocal.setOnClickListener(this);
 		
 		imageLoadDistant = (ImageView) v.findViewById(R.id.home_image_loadDistantProject);
 		imageLoadDistant.setOnClickListener(this);
 		
 		syncMat = (Button) v.findViewById(R.id.home_syncMatAndTypes);
 		syncMat.setOnClickListener(this);
 		
 		return v;
 	}
 
 	@Override
 	public void onClick(View v) {
 		Intent i;
 		//switch on the id of the clicked view
 		switch(v.getId()){
 			case R.id.home_image_newProject_takePhoto :
 				Utils.showToast(MainActivity.baseContext, "Lancement de l'appareil photo", Toast.LENGTH_SHORT);
 				//Setting the directory for the save of the picture to featureapp
 				File folder = new File(Environment.getExternalStorageDirectory(), "featureapp/");
 				folder.mkdirs();
 				//Setting the name of the picture to "Photo_currentDate.jpg"
 				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
 				String currentDateandTime = sdf.format(new Date());
 				File photo = new File(Environment.getExternalStorageDirectory(),"featureapp/Photo_"+currentDateandTime+".jpg");
 				MainActivity.photo.setUrlTemp(photo.getAbsolutePath());
 				//Launching of the photo application
 				i= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 				i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
 		    	getActivity().startActivityForResult(i, Cst.CODE_TAKE_PICTURE);
 		    	break;
 			case R.id.home_image_newProject_photo:
 				Utils.showToast(MainActivity.baseContext, "Lancement de la galerie", Toast.LENGTH_SHORT);
 				i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
 				getActivity().startActivityForResult(i, Cst.CODE_LOAD_PICTURE);
 				break;
 			case R.id.home_image_loadLocalProject:
 				if(MainActivity.internet){
 					i = new Intent(this.getActivity(), LoadExternalProjectsActivity.class);
 					getActivity().startActivityForResult(i,Cst.CODE_LOAD_EXTERNAL_PROJECT);
 				}
 				else{
 					i = new Intent(this.getActivity(), LoadLocalProjectsActivity.class);
 					getActivity().startActivityForResult(i,Cst.CODE_LOAD_LOCAL_PROJECT);
 				}
 				break;
 			case R.id.home_image_loadDistantProject:
             	 i = new Intent(this.getActivity(), LoadExternalProjectsActivity.class);
                  getActivity().startActivityForResult(i,Cst.CODE_LOAD_EXTERNAL_PROJECT);
                  break;
 			case R.id.home_syncMatAndTypes:
 				
 				/**
     			 * Launch the dialog to make user waits
     			 */
     			dialogMater = ProgressDialog.show(getActivity(), "", 
                         "Chargement. Veuillez patienter...", true);
     			
 				MainActivity.datasource.open();
 				String delete_mat = "DELETE FROM "+MySQLiteHelper.TABLE_MATERIAL;
 				String delete_elmtTypes = "DELETE FROM "+MySQLiteHelper.TABLE_ELEMENTTYPE;
 				MainActivity.datasource.getDatabase().rawQuery(delete_mat, null);
 				MainActivity.datasource.getDatabase().rawQuery(delete_elmtTypes, null);
 				MainActivity.elementType.clear();
 				MainActivity.material.clear();
 				Sync s = new Sync();
 				s.getTypeAndMaterialsFromExt();
 				saveElementTypeListToLocal(MainActivity.elementType);
 				saveMaterialListToLocal(MainActivity.material);
 				MainActivity.datasource.close();
                 break;
 
 		}	
 	}
 	public void saveElementTypeListToLocal(ArrayList<ElementType> li){
 		for (ElementType elmtT : li){
 			elmtT.saveToLocal(MainActivity.datasource);
 		}
 	}
 	
 	public void saveMaterialListToLocal(ArrayList<Material> li){
 		for (Material mat : li){
 			mat.saveToLocal(MainActivity.datasource);
 		}
 	}
 }
