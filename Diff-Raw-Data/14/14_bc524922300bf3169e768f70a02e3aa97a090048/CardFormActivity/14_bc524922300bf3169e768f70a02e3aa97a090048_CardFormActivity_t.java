 package com.fyodorwolf.studybudy;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import com.fyodorwolf.studybudy.db.DatabaseAdapter;
 import com.fyodorwolf.studybudy.db.QueryRunner;
 import com.fyodorwolf.studybudy.db.QueryString;
 import com.fyodorwolf.studybudy.db.QueryRunner.QueryRunnerListener;
 import com.fyodorwolf.studybudy.ui.HorizontalListView;
 import com.nostra13.universalimageloader.core.ImageLoader;
 import com.nostra13.universalimageloader.core.assist.FailReason;
 import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class CardFormActivity extends Activity {
 
 	
 	public static final int ACTIVITY_SELECT_IMAGE = 7777;
 	public static final String TAG = "CardFormActivity";
 	public static final String EXTRAS_CARD_ID = "com.fyodorwolf.studyBudy.cardId";
 
 	private static final int IMAGE_REQUEST_CODE = 1;
 	
 	private ArrayList<File> imageFiles = new ArrayList<File>();
 	private String[] imagePaths = new String[0];
 
 	private TextView question;
 	private TextView answer;
 	private long deckId;
 	private long cardId;
 	private String deckName;
 	private DatabaseAdapter myDb;
 
 	@Override protected void onCreate(Bundle savedInstanceState){
         super.onCreate(savedInstanceState);
 		setContentView(R.layout.card_form);
 	    getActionBar().setDisplayHomeAsUpEnabled(true);
 		
 	    myDb = DatabaseAdapter.getInstance();
 		deckId =  getIntent().getExtras().getLong("com.fyodorwolf.studyBudy.deckId");
 		deckName =  getIntent().getExtras().getString("com.fyodorwolf.studyBudy.deckName");
 		question = (TextView) this.findViewById(R.id.question_input);
 		answer = (TextView) this.findViewById(R.id.answer_input);
 		cardId = getIntent().getLongExtra(EXTRAS_CARD_ID, 0);
 
 		setTitle("Add New Card to "+deckName);
 		
 		hideImageGallery();
 		
 		if(cardId > 0){
 			setCardFormData(cardId);
 		}
 		findViewById(R.id.create_card).setOnClickListener(new OnClickListener(){
 			@Override public void onClick(View v){
 				saveCard();
 			}
 		});
 		findViewById(R.id.add_images).setOnClickListener(new OnClickListener(){
 			@Override public void onClick(View v) {
 				Intent multiSelect = new Intent(CardFormActivity.this, MultiPhotoSelectActivity.class);
 				startActivityForResult(multiSelect,IMAGE_REQUEST_CODE);
 			}
 		});
 	}
 
 	@Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if(data != null && resultCode == Activity.RESULT_OK){
 			switch(requestCode) { 
 			  	case (IMAGE_REQUEST_CODE):
 			  	default:
 			  		if(cardId > 0){
 			  			File photosDir = new File(getApplicationContext().getFilesDir()+"/"+cardId+"/");
 						SBApplication.removeFiles(photosDir);
 			  		}
 					imageFiles.clear();
 					hideImageGallery();
 					//set the gallery with the images we got from our image select intent.
 					imagePaths = data.getStringArrayExtra("com.fyodorwolf.studyBudy.imageStrings");
 					if(imagePaths.length>0){
 						for(String imagePath : imagePaths){
 							imageFiles.add(new File(imagePath));
 						}
 						showCardGallery();
 					}
 					break;
 		  	} 
 		}
 		super.onActivityResult(requestCode, resultCode, data); 
 	}
 	
     @Override public void onConfigurationChanged(Configuration newConfig){
     	if(imagePaths.length>0){
 			showCardGallery();
 		}
     }
 
 	@Override  public boolean onCreateOptionsMenu(Menu menu) {
     	if(cardId>0){
 	        getMenuInflater().inflate(R.menu.card_form, menu);
 	        return true;
     	}return false;
     }
 	
 	@Override public boolean onPrepareOptionsMenu(Menu menu){
     	menu.findItem(R.id.card_menu_remove_current_card).setVisible(false);
     	if(cardId>0){
         	menu.findItem(R.id.card_menu_remove_current_card).setVisible(true);
     	}
 		return true;
 	}
 
 	@Override public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);
         switch (item.getItemId()) {
             case android.R.id.home:
             	backToParentActivity();
             	break;
             case R.id.card_menu_remove_current_card:
             	deleteCard().show();
             	break;
         }
 		return true;
     }
     
     private void backToParentActivity() {
         Intent parentActivityIntent = new Intent(this, CardsActivity.class);
         parentActivityIntent.putExtra("com.fyodorwolf.studyBudy.deckId", deckId);
         parentActivityIntent.putExtra("com.fyodorwolf.studyBudy.deckName",  deckName);
         parentActivityIntent.addFlags(
                 Intent.FLAG_ACTIVITY_CLEAR_TOP |
                 Intent.FLAG_ACTIVITY_NEW_TASK);
         startActivity(parentActivityIntent);
         overridePendingTransition(0,0);
         finish();
 	}
 	
 
     /**
      * This method deletes the current deck's card that is being viewed.
      * @return AlertDialog confirming the deletion of a specific card
      */
 	private AlertDialog deleteCard() {
     	AlertDialog myDeleteConfirmationBox = new AlertDialog.Builder(this) 
            //set message, title, and icon
            .setTitle("Delete Card") 
            .setMessage("Are you sure you want to delete this card?") 
            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) { 
                    	dialog.dismiss();
 		           	QueryRunner deleteCardQuery = new QueryRunner(DatabaseAdapter.getInstance());
 		           	deleteCardQuery.setQueryRunnerListener(new QueryRunnerListener(){
 		       			@Override public void onPostExcecute(Cursor cards) {
 		       				backToParentActivity();
 		       			}
 		           	});
 		           	String queryString = QueryString.getDeleteCardQuery(cardId);
 		           	deleteCardQuery.execute(queryString);
                } 
            })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            })
            .create();
        return myDeleteConfirmationBox;
    }
 		
 	/**
 	 * This method Saves the card state and persists the images and data associated with it.
 	 * */
 	private void saveCard() {
 		String question_text = question.getText().toString();
 		String answer_text = answer.getText().toString();
 		if(question_text.length()>0 && answer_text.length()>0){
 			final QueryRunnerListener handlePhotoesCallback = new QueryRunnerListener(){
				@Override public void onPostExcecute(Cursor cursor){
					if(cursor.getCount()>0){
						//JUST CREATED THE CARD.
						cardId = cursor.getLong(0);
					}
 					//MOVE ALL IMAGES TO CARD DIRECTORY
 					if(imageFiles.size()>0){
 						String newFilePath = getApplicationContext().getFilesDir()+"/"+cardId+"/";
 						for(File imageFile : imageFiles){
							String newFileName = Long.toString(System.currentTimeMillis());
							File newImageFile = new File(newFilePath + newFileName);
 							try{
 								SBApplication.copy(imageFile,newImageFile);
 								Log.d(TAG, "copied: "+newImageFile.getAbsolutePath());
 							}catch(Exception e){
 								Log.e(TAG, "unable to copy: "+newImageFile.getAbsolutePath());
 							}
 						}
 					};
 					backToParentActivity();
 				}
 			};
 			if(cardId == 0){
 				//ITS A NEW CARD. SAVE IT AND GET THE CARD ID FIRST.
 				new QueryRunner(myDb,new QueryRunnerListener(){
 					@Override public void onPostExcecute(Cursor notUsed){
 						new QueryRunner(myDb, handlePhotoesCallback).execute(QueryString.getLastCardIdQuery());
 					}
 				}).execute(QueryString.getCreateCardQuery(question_text,answer_text,deckId));
 			}else{
 				//UPDATE EXISTING CARD, AND USE ALREADY DEFINED CARD ID
 				new QueryRunner(myDb,handlePhotoesCallback).execute(QueryString.getUpdateCardQuery(cardId, question_text, answer_text));
 			}
 		}
 	}
 	
 	private void setCardFormData(final long cardId) {
 		setTitle("Edit Card in "+deckName);
 		((Button)findViewById(R.id.create_card)).setText("Save Card");
 		new QueryRunner(myDb, new QueryRunnerListener(){
 			@Override public void onPostExcecute(Cursor cursor) {
 				cursor.moveToFirst();
 				String questionText = cursor.getString(0);
 				String answerText = cursor.getString(1);
 				question.setText(questionText);
 				answer.setText(answerText);
 				imageFiles.clear();
 				File[] photoes = (new File(getApplicationContext().getFilesDir()+"/"+cardId+"/")).listFiles();
 				if(photoes != null){
 					Collections.addAll(imageFiles, photoes);
 					if(imageFiles.size() > 0){
 						showCardGallery();
 					}
 				}
 			}
 		}).execute(QueryString.getCardWithPhotosQuery(cardId));
 	}
 
 	private void showCardGallery() {
 		((Button)this.findViewById(R.id.add_images)).setText("Change Images");
 		ViewGroup tableRow = (ViewGroup) this.findViewById(R.id.create_card_gallary_row);
 		tableRow.setVisibility(View.VISIBLE);
 		final ArrayList<File> galleryItems = imageFiles;
 		final HorizontalListView gallery = (HorizontalListView) findViewById(R.id.photo_list_view);
 		gallery.setOnItemClickListener(new OnItemClickListener(){
 			@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				Intent intent = new Intent();
 				intent.setAction(android.content.Intent.ACTION_VIEW); 
 				intent.setDataAndType(Uri.fromFile(galleryItems.get(position)),"image/*");
 				startActivity(intent);
 			}
 		});
 		gallery.setAdapter(new BaseAdapter(){
 			@Override public int getCount() { return galleryItems.size();}
 			@Override public Object getItem(int position) {return galleryItems.get(position);}
 			@Override public long getItemId(int position) {return Long.valueOf(position);}
 			@Override public View getView(int position, View convertView, ViewGroup parent) {
 				final File myPhoto = galleryItems.get(position);
 				View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_item,null);
 				ImageView myImage = (ImageView) layout.findViewById(R.id.galley_photo_item);
 				ImageLoader.getInstance().displayImage(Uri.fromFile(myPhoto).toString(), myImage,new ImageLoadingListener(){
 					@Override public void onLoadingStarted(String imageUri, View view) {}
 					@Override public void onLoadingCancelled(String imageUri, View view) {}
 					@Override public void onLoadingFailed(String imageUri, View view, FailReason failReason) {}
 					@Override public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
 						((ImageView)view).setImageBitmap(loadedImage);
 					}
 				});
 				return myImage;
 			}
 		});
 	}
 
 	private void hideImageGallery() {
 		this.findViewById(R.id.create_card_gallary_row).setVisibility(View.GONE);
 		((Button)this.findViewById(R.id.add_images)).setText("Add Images");
 	}
 }
