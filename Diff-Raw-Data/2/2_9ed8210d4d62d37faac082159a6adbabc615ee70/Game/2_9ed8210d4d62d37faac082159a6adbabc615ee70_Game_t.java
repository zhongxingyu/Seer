 package fr.supelec.rez_gif;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 
 public class Game extends Activity implements GameView.EndGameListener {
 
     private Uri m_SelectedImage = null;
     private int m_Width;
     private int m_Height;
     
     /** Called when the activity is first created. */
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         
         // Get the parameters from the Indent
         Intent intent = getIntent();
         m_Width = intent.getIntExtra("width", Options.DEFAULT_SIZE);
         m_Height = intent.getIntExtra("height", Options.DEFAULT_SIZE);
         
         // Find the requested image
         m_SelectedImage = intent.getData();
         String image_file = null;
         if(m_SelectedImage != null)
         {
             String[] filePathColumn = {MediaStore.Images.Media.DATA};
             Cursor cursor = getContentResolver().query(m_SelectedImage, filePathColumn, null, null, null);
             cursor.moveToFirst();
             int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
             image_file = cursor.getString(columnIndex);
             cursor.close();
         }
         
         // Set up the view
         GameView game = new GameView(this, image_file, m_Width, m_Height);
         game.setEndGameListener(this);
         setContentView(game);
     }
 
     public void onGameEnded()
     {
         Intent end_game = new Intent();
         end_game.setClass(this, EndGame.class);
         end_game.setData(m_SelectedImage);
         end_game.putExtra("width", m_Width);
        end_game.putExtra("height", m_Height);
         startActivity(end_game);
         finish();
     }
 
 }
