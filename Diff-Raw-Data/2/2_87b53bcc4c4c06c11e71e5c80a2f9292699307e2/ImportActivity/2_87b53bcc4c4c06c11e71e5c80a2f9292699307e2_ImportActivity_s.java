 package sk.peterjurkovic.dril;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import sk.peterjurkovic.dril.csv.CSVReader;
 import sk.peterjurkovic.dril.db.WordDBAdapter;
 import sk.peterjurkovic.dril.model.Word;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 public class ImportActivity extends Activity {
 
 	  final int ACTIVITY_CHOOSE_FILE = 1;
 	  
 	  long lectureId;
 
 	  @Override
 	  public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.import_activity);
 
 	    Intent i = getIntent();
 	    
 	    lectureId = i.getLongExtra(EditLectureActivity.EXTRA_LECTURE_ID, 0);
 	    
 	    ((TextView)findViewById(R.id.importLectureName))
 	    		.setText(WordActivity.getLectureName(this, lectureId));
 	    
 	    ImageButton goHome = (ImageButton) findViewById(R.id.home);
         goHome.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 startActivity( new Intent(ImportActivity.this, DashboardActivity.class) );
             }
         });
 	    
 	    Button btn = (Button) this.findViewById(R.id.importBtn);
 	    btn.setOnClickListener(new OnClickListener() {
 	      @Override
 	      public void onClick(View v) {
 	        Intent chooseFile;
 	        Intent intent;
 	        chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
 	        chooseFile.setType("file/*");
 	        intent = Intent.createChooser(chooseFile, "Choose a file");
 	        startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
 	      }
 	    });
 	  }
 
 	  
 	  @Override
 	  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 	    switch(requestCode) {
 	      case ACTIVITY_CHOOSE_FILE: {
 	        if (resultCode == RESULT_OK){
 	          Uri uri = data.getData();
 	          String filePath = uri.getPath();
 	          Log.d("FILERIEDER", filePath);
 	          new ImportData(filePath, this).execute();
 	        }
 	      }
 	    }
 	  }
 	  
 	  
 			private class ImportData extends AsyncTask<Void, Void, Integer>{
 			  
 		
 			  private ProgressDialog dialog;
 			  private String filePath;
 			  private Context context;
 			  
 			  public ImportData(String filePath, Context context){
 				  this.filePath = filePath;
 				  this.context = context;
 				  dialog = ProgressDialog.show( this.context , "" , 
 							this.context.getResources().getString(R.string.loading), true);
 			  }
 			  
 			  @Override
 				protected void onPreExecute() {
 				    dialog.show();
 					Log.d("FILERIDER", "starting reading file");
 				}
 			  
 			  
 				@Override
 				protected Integer doInBackground(Void... params) {
 					List<Word> words = readFile(filePath);
 					try{
 						WordDBAdapter wordDBAdapter = new WordDBAdapter(context);
 						wordDBAdapter.saveWordList(words);
 					}catch(Exception e){
 						return -1;
 					}
 					
 					return words.size();
 				}
 				
 				@Override
 					protected void onPostExecute(Integer result) {
 						String resultMessage;
 						if(result == 0){
 							resultMessage = getResources().getString( R.string.import_failed);
 						}else{
 							resultMessage = getResources().getString( R.string.import_success, result);
 						}
 						dialog.hide();
 						showResultDialog(resultMessage);
 					}
 			}
 	  
 	  
 			
 			
 	  private List<Word> readFile(String fileLocation){
 			CSVReader reader = null;
 			List<Word> words = new ArrayList<Word>();
 			try {
 				reader = new CSVReader( new FileReader(fileLocation) );
 
 			    String[] nextLine;
 			    while ((nextLine = reader.readNext()) != null) {
 			    	
 			        if(nextLine.length == 2)
 			        	words.add(new Word(nextLine[0], nextLine[1], lectureId));
 			    	Log.d("CSV",  nextLine[0] + " - " + nextLine[1]);
 			    } 
 			} catch (FileNotFoundException e) {
 				Log.e("FILERIEDER", "CSV file not found", e);
 
 			} catch (IOException e) {
 				Log.e("FILERIEDER", "PARSER ERROR", e);
 			}finally{
 				try {
 					reader.close();
 				} catch (IOException e) {
 					Log.e("FILERIEDER", "CAN NOT CLOSE FILE", e);
 				}
 			}
 			  return words;
 		  }
 	  
 	  
 	  public void showResultDialog(String responseMsg){
 			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
 			alertDialogBuilder
 				.setTitle(R.string.import_status)
 				.setMessage(responseMsg)
 				.setCancelable(false)
 				.setNegativeButton(R.string.ok,new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog,int id) {
 								dialog.cancel();
 							}
 				});
 
 			AlertDialog alertDialog = alertDialogBuilder.create();
 			alertDialog.show();
 		}
 
 	    
 	  
 	} 
