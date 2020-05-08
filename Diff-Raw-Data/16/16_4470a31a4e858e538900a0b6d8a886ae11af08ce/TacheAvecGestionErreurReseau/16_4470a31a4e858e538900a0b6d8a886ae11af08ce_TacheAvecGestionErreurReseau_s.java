 package fr.ybo.ybotv.android.util;
 
 import android.content.Context;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.widget.Toast;
 import fr.ybo.ybotv.android.R;
 import fr.ybo.ybotv.android.exception.YboTvErreurReseau;
 
 public abstract class TacheAvecGestionErreurReseau extends AsyncTask<Void, Void, Void> {
 
 
     private Context context;
 
 
     private boolean erreurReseau = false;
 
     public TacheAvecGestionErreurReseau(Context context) {
         this.context = context;
     }
 
     protected abstract void myDoBackground() throws YboTvErreurReseau;
 
     protected Void doInBackground(Void... params) {
         try {
             myDoBackground();
         } catch (YboTvErreurReseau exception) {
             Log.e("YboTv", "Erreur reseau");
             Log.e("YboTv", Log.getStackTraceString(exception));
             erreurReseau = true;
         }
         return null;
     }
 
     @Override
     protected void onPostExecute(Void result) {
         if (erreurReseau && context != null) {
             Toast.makeText(context.getApplicationContext(), context.getString(R.string.erreurReseau), Toast.LENGTH_LONG).show();
         }
         super.onPostExecute(result);
     }
 }
