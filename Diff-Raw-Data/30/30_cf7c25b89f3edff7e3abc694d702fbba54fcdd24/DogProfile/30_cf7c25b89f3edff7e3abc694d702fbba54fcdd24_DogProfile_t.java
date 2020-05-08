 package mx.ferreyra.dogapp;
 
 import static mx.ferreyra.dogapp.ui.DialogHelper.ONLY_DISMISS;
 import static mx.ferreyra.dogapp.ui.DialogHelper.showOkDialog;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 
 import mx.ferreyra.dogapp.pojos.DogProfilePojo;
 import mx.ferreyra.dogapp.recursos.Recursos;
 import mx.ferreyra.dogapp.ui.UI;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class DogProfile extends Activity {
 
     private Context context;
 
     private TextView dogBreed;
     private TextView dogGender;
     private TextView dogLifeStyle;
     private TextView dogBirthDay;
     private TextView dogTip;
 
     private ImageView dogImage;
 
     private DogProfilePojo dogProfilePojo;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         context = this;
         setContentView(R.layout.profile_dog);
 
         dogBreed = (TextView) findViewById(R.id.tvBreed);
         dogGender = (TextView) findViewById(R.id.tvGender);
         dogLifeStyle = (TextView) findViewById(R.id.tvLyfeStyle);
         dogBirthDay = (TextView) findViewById(R.id.tvDate);
         dogTip = (TextView) findViewById(R.id.tvTip);
         dogImage = (ImageView) findViewById(R.id.imageView1);
 
         DogProfileAsync async =  new DogProfileAsync (context);
         async.execute();
 
     }
 
     public void editProfile(View view) {
         Intent intent = new Intent(this, DogRegister.class);
         intent.putExtra("DOG_PROFILE_POJO", dogProfilePojo);
 
         startActivityForResult(intent, DogUtil.DOG_EDIT_PROFILE);
         return;
     }
 
     protected class DogProfileAsync extends AsyncTask<Void, Integer, DogProfilePojo> {
         private final Context context;
         private ProgressDialog dialog;
 
         public DogProfileAsync(Context context) {
             this.context = context;
         }
 
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             dialog = new ProgressDialog(context);
             dialog.setMessage("Espere un momento de favor");
             dialog.show();
         }
 
         @Override
         protected DogProfilePojo doInBackground(Void... params) {
            WsDogUtils wsDogUtils = new WsDogUtils();
             try {
                 Integer userid = DogUtil.getInstance().getCurrentUserId();
                 String[][] result = wsDogUtils.getDuenosMascotasByIdUsuario(userid);
 
                 if(result==null || result.length==0) {
                     // No results returned
                     return null;
                 }
 
                 DogProfilePojo pojo = getDogProfilePojo(result[0]);
 
                 // Store owner id
                 DogUtil.getInstance().saveCurrentOwnerId(pojo.getIdDueno());
 
                 return pojo;
             } catch(Exception e) {
                 return null;
             }
         }
 
         @Override
         protected void onPostExecute(DogProfilePojo result) {
             super.onPostExecute(result);
 
             // Hide dialog
             dialog.dismiss();
 
             if(result == null){
                 showOkDialog(this.context, getString(R.string.no_dogs_registered), ONLY_DISMISS);
                 startActivityForResult(new Intent(context, DogRegister.class),DogUtil.DOG_PROFILE);
             } else {
                 // Display pojo data
                 dogProfilePojo = result;
                 pojoToView(result);
             }
         }
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode,
             Intent intent) {
         super.onActivityResult(requestCode, resultCode, intent);
 
         if (resultCode == Activity.RESULT_OK && intent != null) {
             if (requestCode == DogUtil.DOG_PROFILE || requestCode ==  DogUtil.DOG_EDIT_PROFILE) {
                 // Owner id returned from edit mode in DogRegister
                 Integer ownerId = intent.getExtras().getInt("OWNER_ID");
                 if(ownerId ==  null || ownerId < 0) {
                     // Report error
                     UI.showAlertDialog("Ups!",
                             "Ha ocurrido un error",
                             "OK", context, null);
                 } else {
                     // DogProfile update was successful
                     DogProfileAsync async =  new DogProfileAsync (context);
                     async.execute();
                 }
             }
         } else {
             finish();
         }
     }
 
     public void pojoToView(DogProfilePojo pojo) {
         SimpleDateFormat formater = new SimpleDateFormat("MMM dd, yyyy");
         dogBreed.setText(pojo.getMascotaRaza());
         dogGender.setText(Recursos.GENDER[pojo.getMascotaIdGenero()-1]);
         dogLifeStyle.setText(Recursos.LIFE_STYLE[pojo.getMascotaIdTipoVida()-1]);
         dogBirthDay.setText(formater.format(pojo.getMascotaFechaCumpleanos()));
         dogTip.setText(pojo.getTip());
         dogImage.setImageBitmap(pojo.getMascotaImagen());
     }
 
     public static DogProfilePojo getDogProfilePojo(String[] dataAsArray) throws ParseException {
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 
         DogProfilePojo pojo = new DogProfilePojo();
         pojo.setIdDueno(Integer.valueOf(dataAsArray[0]));
         pojo.setIdUsuario(Integer.valueOf(dataAsArray[1]));
         pojo.setDuenoNombre(dataAsArray[2]);
         pojo.setDuenoIdGenero(Integer.valueOf(dataAsArray[3]));
         pojo.setDuenoFechaCumpleanos(sdf.parse(dataAsArray[4]));
         pojo.setDuenoIdEstado(Integer.valueOf(dataAsArray[5]));
         pojo.setMascotaNombre(dataAsArray[6]);
         pojo.setMascotaRaza(dataAsArray[7]);
         pojo.setMascotaIdGenero(Integer.valueOf(dataAsArray[8]));
         pojo.setMascotaIdTipoVida(Integer.valueOf(dataAsArray[9]));
         pojo.setMascotaIdActividadFisica(Integer.valueOf(dataAsArray[10]));
         pojo.setMascotaFechaCumpleanos(sdf.parse(dataAsArray[11]));
         pojo.setMascotaImagen(dataAsArray[12]);
         pojo.setTip(dataAsArray[16]);
         return pojo;
     }
 }
