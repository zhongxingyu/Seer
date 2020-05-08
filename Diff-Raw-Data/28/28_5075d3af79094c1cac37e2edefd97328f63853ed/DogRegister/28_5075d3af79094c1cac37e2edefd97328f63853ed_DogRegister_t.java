 package mx.ferreyra.dogapp;
 
 import java.io.ByteArrayOutputStream;
 import java.text.SimpleDateFormat;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import mx.ferreyra.dogapp.fragments.DatePickerFragment;
 import mx.ferreyra.dogapp.fragments.DatePickerFragment.MyDate;
 import mx.ferreyra.dogapp.pojos.DogProfilePojo;
 import mx.ferreyra.dogapp.recursos.Recursos;
 import mx.ferreyra.dogapp.ui.UI;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.content.res.Configuration;
 import android.graphics.Bitmap;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.provider.MediaStore.Images.Media;
 import android.support.v4.app.FragmentActivity;
 import android.util.Base64;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 
 @SuppressLint("NewApi")
 public class DogRegister extends FragmentActivity {
 
     static final int DATE_DIALOG_ID = 0;
 
     private View activityRootView;
     protected Activity context;
 
     //Variables internas para el control del registro
     //Vistas  del perro
 
     private EditText dogNameField;
     private EditText dogBreedField;
 
     private Button dogGenderField;
     private Button dogLifeStyleField;
     private Button dogActivityField;
     private ImageView dogPhoto;
     private Button dogBirthday;
 
     private ImageButton btRemoveImage;
 
 
     //Vistas del dueno
 
     private EditText ownerNameField;
     private Button ownerGenderField;
     private Button ownerStateField;
     private Button ownerBirthDay;
 
 
 
 
     private int dogGender = -1;
     private int dogLifeStyle =-1;
     private int dogActivity =-1;
 
     //vars. fec. nacimiento
     private int dogYear = -1;
     private int dogMonth = -1;
     private int dogDay = -1;
 
 
     private int ownerGender = -1;
     private int ownerState = -1;
 
     //vars. fec. nacimiento
     private int ownerYear = -1;
     private int ownerMonth = -1;
     private int ownerDay = -1;
 
     private Bitmap dogImage;
 
 
     private DogProfilePojo dogProfilePojo;
 
     SimpleDateFormat formater = new SimpleDateFormat("dd / MMM / yyyy");
 
     @SuppressWarnings("deprecation")
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.register_dog);
 
         context = this;
         activityRootView = findViewById(R.id.activityrootview);
 
         //Vistas del perro
         dogNameField = (EditText) findViewById(R.id.dog_name_field);
         dogBreedField = (EditText) findViewById(R.id.dog_name_field);
         dogGenderField = (Button) findViewById(R.id.dog_gender_field);
         dogLifeStyleField = (Button) findViewById(R.id.dog_life_style_field);
         dogActivityField = (Button) findViewById(R.id.dog_activity_field);
         dogPhoto = (ImageView) findViewById(R.id.dog_photo);
         dogBirthday = (Button) findViewById(R.id.dog_birthday);
 
         //Vistas del dueno
         ownerNameField = (EditText) findViewById(R.id.owner_name_field);
         ownerGenderField = (Button) findViewById(R.id.owner_gender_field);
         ownerStateField = (Button) findViewById(R.id.owner_state);
         ownerBirthDay = (Button) findViewById(R.id.owner_birthday);
 
 
         btRemoveImage = (ImageButton) findViewById(R.id.bt_remove);
 
 
         Bundle extras = getIntent().getExtras();
         if (extras != null) {
             dogProfilePojo = (DogProfilePojo) extras.get("DOG_PROFILE_POJO");
 
 
 
 
 
             dogNameField.setText(dogProfilePojo.mascotaNombre);
             dogBreedField.setText(dogProfilePojo.mascotaRaza);
 
             dogGenderField.setHint(Recursos.GENDER[dogProfilePojo.mascotaIdGenero-1]);
             dogGender = dogProfilePojo.mascotaIdGenero-1;
 
             dogLifeStyleField.setHint(Recursos.LIFE_STYLE[dogProfilePojo.mascotaIdTipoVida-1]);
             dogLifeStyle = dogProfilePojo.mascotaIdTipoVida-1;
 
             dogActivityField.setHint(Recursos.ACTIVITY[dogProfilePojo.mascotaIdActividadFisica-1]);
             dogActivity = dogProfilePojo.mascotaIdActividadFisica-1;
 
             dogPhoto.setImageBitmap(dogProfilePojo.getMascotaImagen());
             dogImage = dogProfilePojo.getMascotaImagen();
             btRemoveImage.setVisibility(View.VISIBLE);
 
             dogBirthday.setHint(formater.format( dogProfilePojo.mascotaFechaCumpleanos ));
             dogYear = dogProfilePojo.mascotaFechaCumpleanos.getYear()-100+2000;
             dogMonth = dogProfilePojo.mascotaFechaCumpleanos.getMonth();
             dogDay = dogProfilePojo.mascotaFechaCumpleanos.getDate();
 
 
             ownerNameField.setText(dogProfilePojo.duenoNombre);
 
             ownerGenderField.setHint(Recursos.GENDER_OWNER[dogProfilePojo.duenoIdGenero-1]);
             ownerGender = dogProfilePojo.duenoIdGenero-1;
 
             ownerBirthDay.setHint(formater.format( dogProfilePojo.duenoFechaCumpleanos ));
             ownerYear = dogProfilePojo.duenoFechaCumpleanos.getYear()-100+2000;
             ownerMonth = dogProfilePojo.duenoFechaCumpleanos.getMonth();
             ownerDay = dogProfilePojo.duenoFechaCumpleanos.getDate();
 
             ownerStateField.setHint(Recursos.STATES[dogProfilePojo.duenoIdEstado-1]);
             ownerState = dogProfilePojo.duenoIdEstado-1;
 
 
         }
 
     };
 
 
 
     /*
      * Listener de los botones que tienen funion de spinner para no tener una opcion ya selecionada desde un principio
      * */
 
     public void onClickDogGenderFieldButton(View view) {
         AlertDialog.Builder builder;
         checkAndHideKeyboard(null);
         builder = Build.VERSION.SDK_INT>=11 ?
                 new AlertDialog.Builder(context,AlertDialog.THEME_HOLO_LIGHT) :
                     new AlertDialog.Builder(context);
                 builder.setTitle("Genero");
                 builder.setSingleChoiceItems(Recursos.GENDER,  dogGender, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int item) {
                         dogGenderField.setHint(Recursos.GENDER[item]);
                         dogGender = item;
                         dialog.dismiss();
                     }
                 });
                 builder.create().show();
     }
 
     public void onClickDogLifeStyleFieldButton(View view) {
         AlertDialog.Builder builder;
         checkAndHideKeyboard(null);
         builder = Build.VERSION.SDK_INT>=11 ?
                 new AlertDialog.Builder(context,AlertDialog.THEME_HOLO_LIGHT) :
                     new AlertDialog.Builder(context);
                 builder.setTitle("Tipo de Vida");
                 builder.setSingleChoiceItems(Recursos.LIFE_STYLE,  dogLifeStyle, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int item) {
                         dogLifeStyleField.setHint(Recursos.LIFE_STYLE[item]);
                         dogLifeStyle = item;
                         dialog.dismiss();
                     }
                 });
                 builder.create().show();
     }
 
     public void onClickDogActivityFieldButton(View view) {
         AlertDialog.Builder builder;
         checkAndHideKeyboard(null);
         builder = Build.VERSION.SDK_INT>=11 ?
                 new AlertDialog.Builder(context,AlertDialog.THEME_HOLO_LIGHT) :
                     new AlertDialog.Builder(context);
                 builder.setTitle("Actividad F\u00edsica");
                 builder.setSingleChoiceItems(Recursos.ACTIVITY,  dogActivity, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int item) {
                         //Registro.this.tvSexo.setText(Recursos.GENDER[item]);
                         dogActivityField.setHint(Recursos.ACTIVITY[item]);
                         dogActivity = item;
                         dialog.dismiss();
                     }
                 });
                 builder.create().show();
     }
 
     public void onClickDogBirthdayButton(View view) {
         DatePickerFragment dogDialogBirtday = new DatePickerFragment();
 
         dogDialogBirtday.setDate(dogYear, dogMonth, dogDay);
         dogDialogBirtday.setInterface(myDogDate);
         dogDialogBirtday.show(getSupportFragmentManager(), "dateOwnerPicker");
     }
 
     public void onClickOwnerBirthdayButton(View view) {
         DatePickerFragment dogDialogBirtday = new DatePickerFragment();
 
         dogDialogBirtday.setDate(ownerYear, ownerMonth, ownerDay);
         dogDialogBirtday.setInterface(myOwnerDate);
         dogDialogBirtday.show(getSupportFragmentManager(), "dateDoogPicker");
     }
 
     public void onClickSendButton(View v) {
         if(!validateForm())
             return;
 
         ByteArrayOutputStream stream = new ByteArrayOutputStream();
         dogImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
         byte[] byteArray = stream.toByteArray();
         String encodedImageStr = Base64.encodeToString(byteArray,Base64.DEFAULT);
 
         Map<String, String> map = new HashMap<String, String>();
 
         String userId = DogUtil.getInstance().getCurrentUserId().toString();
         map.put("idUsuario", userId);
         map.put("duenoNombre", dogNameField.getText().toString());
         map.put("mascotaRaza", dogBreedField.getText().toString());
         map.put("mascotaIdGenero", String.valueOf(dogGender+1));
         map.put("mascotaIdTipoVida", String.valueOf(dogLifeStyle+1));
         map.put("mascotaIdActividadFisica", String.valueOf(dogActivity+1));
         map.put("mascotaImagen", encodedImageStr);
         map.put("mascotaFechaCumpleanos", dogYear + "-" + (dogMonth<10 ? "0"+dogMonth : dogMonth) + "-"+ dogDay);
 
         map.put("duenoNombre", ownerNameField.getText().toString());
         map.put("duenoIdGenero", String.valueOf(ownerGender+1));
         map.put("duenoFechaCumpleanos", ownerYear + "-" + (ownerMonth<10 ? "0"+ownerMonth : ownerMonth) + "-"+ ownerDay);
         map.put("duenoIdEstado", String.valueOf(ownerState));
 
         DogRegisterAsync dogRegisterAsync =  new DogRegisterAsync (context);
         dogRegisterAsync.setMap(map);
         dogRegisterAsync.execute();
     }
 
     private boolean validateForm() {
         if(dogNameField.getText().equals("")) {
             dogNameField.requestFocus();
             UI.showAlertDialog("Upps!!",
                     "Ingrese el nombre del perro",
                     "OK", this, null);
             return false;
         }
 
         if(dogBreedField.getText().equals("")) {
             dogBreedField.requestFocus();
             UI.showAlertDialog("Upps!!",
                     "Ingrese la raza del perro",
                     "OK", this, null);
             return false;
         }
 
         if(ownerNameField.getText().equals("")) {
             ownerNameField.requestFocus();
             UI.showAlertDialog("Upps!!",
                     "Ingrese el nombre del due\u00f1o",
                     "OK", this, null);
             return false;
         }
 
         if(dogImage == null) {
             UI.showAlertDialog("Upps!!",
                     "Seleccione una imagen",
                     "OK", this, null);
             return false;
         }
 
         if(dogGender == -1 || dogLifeStyle == -1 || dogActivity == -1 ||
                 dogYear == -1 || dogMonth == -1 || dogDay == -1 ||
                 ownerGender == -1 || ownerYear == -1 || ownerMonth == -1 ||
                 ownerDay == -1 || ownerState == -1) {
             UI.showAlertDialog("Upps!!",
                     "Favor de llenar todos los campos antes de continuar",
                     "OK", this, null);
             return false;
         }
 
         return true;
     }
 
     MyDate myDogDate = new MyDate(){
         @Override
         public void getDate(int year, int month, int day) {
             dogYear = year;
             dogMonth = month+1;
             dogDay = day;
 
             dogBirthday.setHint(day + " / " + Recursos.MONTHS[month] + " / " + year);
         }
 
     };
 
 
     MyDate myOwnerDate = new MyDate(){
         @Override
         public void getDate(int year, int month, int day) {
             ownerYear = year;
             ownerMonth = month;
             ownerDay = day;
 
             ownerBirthDay.setHint(day + " / " + Recursos.MONTHS[month] + " / " + year);
         }
 
     };
 
     public void onClickOwnerGenderButton(View view) {
         AlertDialog.Builder builder;
         checkAndHideKeyboard(null);
         builder = Build.VERSION.SDK_INT>=11 ?
                 new AlertDialog.Builder(context,AlertDialog.THEME_HOLO_LIGHT) :
                     new AlertDialog.Builder(context);
                 builder.setTitle("Genero");
                 builder.setSingleChoiceItems(Recursos.GENDER_OWNER,  ownerGender, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int item) {
                         ownerGenderField.setHint(Recursos.GENDER_OWNER[item]);
                         ownerGender = item;
                         dialog.dismiss();
                     }
                 });
                 builder.create().show();
     }
 
     public void onClickOwnerStateButton(View view) {
         AlertDialog.Builder builder;
         checkAndHideKeyboard(null);
         builder = Build.VERSION.SDK_INT>=11 ?
                 new AlertDialog.Builder(context,AlertDialog.THEME_HOLO_LIGHT) :
                     new AlertDialog.Builder(context);
                 builder.setTitle("Estado");
                 builder.setSingleChoiceItems(Recursos.STATES,  ownerState, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int item) {
                         //Registro.this.tvSexo.setText(Recursos.GENDER[item]);
                         ownerStateField.setHint(Recursos.STATES[item]);
                         ownerState = item;
                         dialog.dismiss();
                     }
                 });
                 builder.create().show();
     }
 
     public void onClickDogPhotoImageView(View view) {
         getPhoto(view);
     }
 
     public void onClickRemoveImageView(View view) {
         dogImage = null;
         dogPhoto.setImageResource(R.drawable.bg_avatar_camera);
         btRemoveImage.setVisibility(View.INVISIBLE);
     }
 
     public void getPhoto (View view){
 
         final CharSequence[] items = {"Desde mis imagenes", "Tomar una foto"};
 
         AlertDialog.Builder builder = new AlertDialog.Builder(context);
         builder.setTitle("Tomar Fotograf\u00eda");
         builder.setItems(items, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int item) {
                 if (item == 0)
                     getPhotofromAlbum();
                 else if (item == 1)
                     takePhoto();
             }
         });
         builder.setCancelable(true);
         AlertDialog alert = builder.create();
         alert.show();
     }
 
 
     private void getPhotofromAlbum() {
         Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
         intent.setType("image/*");
         startActivityForResult(intent, 1);
     }
 
     private void takePhoto() {
         final String action = MediaStore.ACTION_IMAGE_CAPTURE;
         if(isIntentAvailable(context, action))
             startActivityForResult(new Intent(action), 0);
     }
 
     public static boolean isIntentAvailable(Context context, String action) {
         List<ResolveInfo> list =
                 context.getPackageManager()
                 .queryIntentActivities(new Intent(action),
                         PackageManager.MATCH_DEFAULT_ONLY);
         return list.size() > 0;
     }
 
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent intent) {
         super.onActivityResult(requestCode, resultCode, intent);
 
         Bitmap mBitmap = null;
         if (resultCode == Activity.RESULT_OK && intent != null){
             if(requestCode == 0)
                 mBitmap = (Bitmap)intent.getExtras().get("data");
             else if(requestCode == 1){
                 try {
                     mBitmap = Media.getBitmap(context.getContentResolver(), intent.getData());
                 }catch (Exception e) {
                     e.printStackTrace();
                 }
             }
 
             if (mBitmap != null){
                 dogImage = UtilsBitmap.resize(mBitmap, 100, 100);
                 dogPhoto.setImageBitmap(dogImage);
                 btRemoveImage.setVisibility(View.VISIBLE);
             }
         }
     }
 
     public void onClickActivityRootView(View view) {
         checkAndHideKeyboard(view);
     }
 
     public void checkAndHideKeyboard(View view){
         if(getResources().getConfiguration().keyboardHidden == Configuration.KEYBOARDHIDDEN_NO){
             InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
             imm.hideSoftInputFromWindow(activityRootView.getWindowToken(), 0);
         }
     }
 
 
 
     //AsyncTasj del Registro del Perfil del Perro
 
 
     protected class DogRegisterAsync extends AsyncTask<Void, Integer, Integer> {
         private Context context;
         private ProgressDialog dialog;
         private Map<String, String> map;
 
         public DogRegisterAsync(Context context) {
             this.context = context;
         }
 
 
         public void setMap (Map<String, String> map ){
             this.map = map;
             return;
         }
 
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             dialog = new ProgressDialog(context);
             dialog.setMessage(context.getString(R.string.please_wait_signing_up));
             dialog.show();
         }
 
         @Override
         protected Integer doInBackground(Void... params) {
             WsDogUtils wsDogUtils = new WsDogUtils(context);
             try {
                if(DogUtil.getInstance().getCurrentDogId() == null || DogUtil.getInstance().getCurrentDogId()<0)
                     return wsDogUtils.insertDuenoMascota(map);
                else
                    return wsDogUtils.editDuenoMascota(map);
             } catch (Exception e) {
                 return null;
             }
         }
 
         @Override
         protected void onPostExecute(Integer result) {
             super.onPostExecute(result);
 
             dialog.dismiss();
 
 
             Intent intent = new Intent();
             intent.putExtra("ID_PET", result);
 
             DogUtil.getInstance().saveCurrentDogId(result);
 
             setResult(Activity.RESULT_OK, intent);
             finish();
 
         }
     }
 
 }
