 package mx.ferreyra.dogapp;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.text.SimpleDateFormat;
 import java.util.Map;
 
 import mx.ferreyra.dogapp.pojos.DogProfilePojo;
 import mx.ferreyra.dogapp.recursos.Recursos;
 import mx.ferreyra.dogapp.ui.UI;
 import static mx.ferreyra.dogapp.ui.DialogHelper.*;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Base64;
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
 
         Integer tmp = DogUtil.getInstance().getCurrentDogId();
 
 
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
 
     public void editProfile(View v){
 
         Intent intent = new Intent(this, DogRegister.class);
         intent.putExtra("DOG_PROFILE_POJO", dogProfilePojo);
 
         startActivityForResult(intent, DogUtil.DOG_EDIT_PROFILE);
         return;
     }
 
 
     protected class DogProfileAsync extends
     AsyncTask<Void, Integer, DogProfilePojo> {
         private final Context context;
         private ProgressDialog dialog;
         private Map<String, String> map;
 
         public DogProfileAsync(Context context) {
             this.context = context;
         }
 
         /*
          * public void setMap (Map<String, String> map ){ this.map = map;
          * return; }
          */
 
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             dialog = new ProgressDialog(context);
             dialog.setMessage("Espere un momento de favor");
             dialog.show();
         }
 
         @Override
         protected DogProfilePojo doInBackground(Void... params) {
             WsDogUtils wsDogUtils = new WsDogUtils(context);
             try {
                 Integer userid = DogUtil.getInstance().getCurrentUserId();
 
                 String[][] result = wsDogUtils.getDuenosMascotasByIdUsuario(userid);
 
                 if(result==null || result.length==0) {
                     // No results returned
                     return null;
                 }
 
                 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 
                 dogProfilePojo = new DogProfilePojo();
 
 
                 dogProfilePojo.idDueno = Integer.valueOf(result[0][0]);
                 dogProfilePojo.idUsusario = Integer.valueOf(result[0][1]);
                 dogProfilePojo.duenoNombre = result[0][2];
                 dogProfilePojo.duenoIdGenero = Integer.valueOf(result[0][3]);
                 dogProfilePojo.duenoFechaCumpleanos = sdf.parse(result[0][4]);
                 dogProfilePojo.duenoIdEstado = Integer.valueOf(result[0][5]);
 
                 dogProfilePojo.mascotaNombre = result[0][6];
                 dogProfilePojo.mascotaRaza = result[0][7];
                 dogProfilePojo.mascotaIdGenero = Integer.valueOf(result[0][8]);
                 dogProfilePojo.mascotaIdTipoVida = Integer.valueOf(result[0][9]);
                 dogProfilePojo.mascotaIdActividadFisica = Integer.valueOf(result[0][10]);
                 dogProfilePojo.mascotaFechaCumpleanos = sdf.parse(result[0][11]);
                 //                byte[] bytes = Base64.decode(result[0][12], Base64.DEFAULT);
                 //                InputStream is = new ByteArrayInputStream(bytes);
                 //                Bitmap bmp = BitmapFactory.decodeStream(is);
 
                 dogProfilePojo.setMascotaImagen ( result[0][12]);
 
                 dogProfilePojo.tip = result[0][16];
 
                 return dogProfilePojo;
             } catch(Exception e) {
                 return null;
             }
         }
 
         @Override
         protected void onPostExecute(DogProfilePojo result) {
             super.onPostExecute(result);
 
             if (result == null){
                showOkDialog(this.context, "No hay mascotas registradas", null);
                 startActivityForResult(new Intent(context, DogRegister.class),DogUtil.DOG_PROFILE);
             } else {
                 SimpleDateFormat formater = new SimpleDateFormat("MMM dd, yyyy");
                 dogBreed.setText(result.mascotaRaza);
                 dogGender.setText(Recursos.GENDER[result.mascotaIdGenero-1]);
                 dogLifeStyle.setText(Recursos.LIFE_STYLE[result.mascotaIdTipoVida-1]);
                 dogBirthDay.setText(formater.format(result.mascotaFechaCumpleanos));
                 dogTip.setText(result.tip);
                 dogImage.setImageBitmap(result.getMascotaImagen());
             }
 
 
 
             // String breed = result[0][7];
             // String gender = result[0][3];
             // String lifestyle = result[0][0];
             // String birthday = result[0][0];
             // String tip = result[0][0];
             // String image = result[0][0];
             //
             //
             //
             dialog.dismiss();
 
         }
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode,
             Intent intent) {
         super.onActivityResult(requestCode, resultCode, intent);
 
         if (resultCode == Activity.RESULT_OK && intent != null) {
             if (requestCode == DogUtil.DOG_PROFILE || requestCode ==  DogUtil.DOG_EDIT_PROFILE) {
                 if (DogUtil.getInstance().getCurrentDogId() ==  null ||
                     DogUtil.getInstance().getCurrentDogId()<0) {
                     UI.showAlertDialog("Ups!",
                             "Ha ocurrido un error",
                             "OK", context, null);
                 } else {
                     DogProfileAsync async =  new DogProfileAsync (context);
                     async.execute();
                 }
             }
 
         }
         else {
             finish();
         }
 
 
     }
 }
