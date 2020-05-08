 package mx.ferreyra.dogapp;
 
 import static mx.ferreyra.dogapp.ui.DialogHelper.ONLY_DISMISS;
 import static mx.ferreyra.dogapp.ui.DialogHelper.showOkDialog;
 
 import java.io.ByteArrayOutputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import mx.ferreyra.dogapp.fragments.DatePickerFragment;
 import mx.ferreyra.dogapp.fragments.DatePickerFragment.MyDate;
 import mx.ferreyra.dogapp.pojos.DogProfilePojo;
 import mx.ferreyra.dogapp.recursos.Recursos;
 import mx.ferreyra.dogapp.ui.UI;
 
 import org.xmlpull.v1.XmlPullParserException;
 
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
 import android.util.Log;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 
 @SuppressLint("NewApi")
 public class DogRegister extends FragmentActivity {
 
     static final int DATE_DIALOG_ID = 0;
     static final String[] DOG_AGE_RANGES = {
         "Menos de 1 a\u00f1o",
         "Entre 1 y 7 a\u00f1os",
         "M\u00e1s de 7 a\u00f1os"
     };
 
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
     private int dogAgeRange =-1;
 
     private int ownerGender = -1;
     private int ownerState = -1;
 
     //vars. fec. nacimiento
     private int ownerYear = -1;
     private int ownerMonth = -1;
     private int ownerDay = -1;
 
     private Bitmap dogImage;
 
 
     private DogProfilePojo dogProfilePojo;
 
     SimpleDateFormat formater = new SimpleDateFormat("dd / MMM / yyyy");
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.register_dog);
 
         context = this;
         activityRootView = findViewById(R.id.activityrootview);
         dogNameField = (EditText) findViewById(R.id.dog_name_field);
         dogBreedField = (EditText) findViewById(R.id.dog_breed_field);
         dogGenderField = (Button) findViewById(R.id.dog_gender_field);
         dogLifeStyleField = (Button) findViewById(R.id.dog_life_style_field);
         dogActivityField = (Button) findViewById(R.id.dog_activity_field);
         dogPhoto = (ImageView) findViewById(R.id.dog_photo);
         dogBirthday = (Button) findViewById(R.id.dog_birthday);
         ownerNameField = (EditText) findViewById(R.id.owner_name_field);
         ownerGenderField = (Button) findViewById(R.id.owner_gender_field);
         ownerStateField = (Button) findViewById(R.id.owner_state);
         ownerBirthDay = (Button) findViewById(R.id.owner_birthday);
         btRemoveImage = (ImageButton) findViewById(R.id.bt_remove);
 
         // Check if parameters
         Bundle extras = getIntent().getExtras();
         if(extras != null) {
             DogProfilePojo pojo = (DogProfilePojo)extras.get("DOG_PROFILE_POJO");
             if(pojo == null) {
                 Log.w(DogUtil.DEBUG_TAG, "Pojo should not be null");
             } else {
                 // Load pojo data on view
                 pojoToView(pojo);
             }
         }
     };
 
     public void pojoToView(DogProfilePojo pojo) {
         dogNameField.setText(pojo.getMascotaNombre());
         dogBreedField.setText(pojo.getMascotaRaza());
         dogGenderField.setHint(Recursos.GENDER[pojo.getMascotaIdGenero()-1]);
         dogGender = pojo.getMascotaIdGenero()-1;
         dogLifeStyleField.setHint(Recursos.LIFE_STYLE[pojo.getMascotaIdTipoVida()-1]);
         dogLifeStyle = pojo.getMascotaIdTipoVida()-1;
         dogActivityField.setHint(Recursos.ACTIVITY[pojo.getMascotaIdActividadFisica()-1]);
         dogActivity = pojo.getMascotaIdActividadFisica()-1;
         dogPhoto.setImageBitmap(pojo.getMascotaImagen());
         dogImage = pojo.getMascotaImagen();
         btRemoveImage.setVisibility(View.VISIBLE);
 
         // Age range
         int nowYear = Calendar.getInstance().get(Calendar.YEAR);
         int dogBirthdayYear = pojo.getMascotaFechaCumpleanos().getYear();
         int difference = nowYear - dogBirthdayYear;
         String tag;
         if(pojo.getMascotaFechaCumpleanos()==null) {
             tag = "";
         } else {
             if(difference==0) {
                 tag = ": " + DOG_AGE_RANGES[dogAgeRange = 0];
             } else if(difference<7) {
                 tag = ": " + DOG_AGE_RANGES[dogAgeRange = 1];
             } else {
                 tag = ": " + DOG_AGE_RANGES[dogAgeRange = 2];
             }
         }
         dogBirthday.setHint(getString(R.string.dog_age) + tag);
         ownerNameField.setText(pojo.getDuenoNombre());
         ownerGenderField.setHint(Recursos.GENDER_OWNER[pojo.getDuenoIdGenero()-1]);
         ownerGender = pojo.getDuenoIdGenero()-1;
         ownerBirthDay.setHint(formater.format( pojo.getDuenoFechaCumpleanos() ));
         ownerYear = pojo.getDuenoFechaCumpleanos().getYear()-100+2000;
         ownerMonth = pojo.getDuenoFechaCumpleanos().getMonth();
         ownerDay = pojo.getDuenoFechaCumpleanos().getDate();
         ownerStateField.setHint(Recursos.STATES[pojo.getDuenoIdEstado()-1]);
         ownerState = pojo.getDuenoIdEstado()-1;
     }
 
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
         AlertDialog.Builder builder;
         checkAndHideKeyboard(null);
 
         builder = Build.VERSION.SDK_INT>=11 ?
                 new AlertDialog.Builder(context,AlertDialog.THEME_HOLO_LIGHT) :
                     new AlertDialog.Builder(context);
                 builder.setTitle(getString(R.string.dog_age));
                 builder.setSingleChoiceItems(DOG_AGE_RANGES, dogAgeRange, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int item) {
                         dogBirthday.setHint(getString(R.string.age) + ": " + DOG_AGE_RANGES[item]);
                         dogAgeRange = item;
                         dialog.dismiss();
                     }
                 });
                 builder.create().show();
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
 
         Map<String, String> map = viewToMap();
 
         // Register async task
         DogRegisterAsync dogRegisterAsync =  new DogRegisterAsync(context);
         dogRegisterAsync.setMap(map);
         dogRegisterAsync.execute();
     }
 
     public Map<String, String> viewToMap() {
         Map<String, String> map = new HashMap<String, String>();
 
         Integer userId = DogUtil.getInstance().getCurrentUserId();
         Integer ownerId = DogUtil.getInstance().getCurrentOwnerId();
         if(userId!=null)
             map.put("idUsuario", userId.toString());
         if(ownerId!=null)
             map.put("idDueno", ownerId.toString());
 
         map.put("mascotaNombre", dogNameField.getText().toString());
         map.put("mascotaRaza", dogBreedField.getText().toString());
         map.put("mascotaIdGenero", String.valueOf(dogGender+1));
         map.put("mascotaIdTipoVida", String.valueOf(dogLifeStyle+1));
         map.put("mascotaIdActividadFisica", String.valueOf(dogActivity+1));
 
         Calendar cal = Calendar.getInstance();
         int diff = dogAgeRange == 0 ? 0 : (dogAgeRange == 1 ? -6 : -10);
         cal.add(Calendar.YEAR, diff);
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
         map.put("mascotaFechaCumpleanos", sdf.format(cal.getTime()));
 
         // Owner data
         map.put("duenoNombre", ownerNameField.getText().toString());
         map.put("duenoIdGenero", String.valueOf(ownerGender+1));
         map.put("duenoFechaCumpleanos", ownerYear + "-" + (ownerMonth<10 ? "0"+ownerMonth : ownerMonth) + "-"+ ownerDay);
         map.put("duenoIdEstado", String.valueOf(ownerState));
 
         map.put("comentarios1", "");
         map.put("comentarios2", "");
 
         // Dog Image
         ByteArrayOutputStream stream = new ByteArrayOutputStream();
         dogImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
         byte[] byteArray = stream.toByteArray();
         String encodedImageStr = Base64.encodeToString(byteArray,Base64.DEFAULT);
         map.put("mascotaImagen", encodedImageStr);
 
         return map;
     }
 
     private boolean validateForm() {
         if(dogNameField.getText().equals("")) {
             dogNameField.requestFocus();
             UI.showAlertDialog("Upps!!",
                     "Ingrese el nombre del perro",
                     getString(android.R.string.ok), this, null);
             return false;
         }
 
         if(dogBreedField.getText().equals("")) {
             dogBreedField.requestFocus();
             UI.showAlertDialog("Upps!!",
                     "Ingrese la raza del perro",
                     getString(android.R.string.ok), this, null);
             return false;
         }
 
         if(ownerNameField.getText().equals("")) {
             ownerNameField.requestFocus();
             UI.showAlertDialog("Upps!!",
                     "Ingrese el nombre del due\u00f1o",
                     getString(android.R.string.ok), this, null);
             return false;
         }
 
         if(dogImage == null) {
             UI.showAlertDialog("Upps!!",
                     "Seleccione una imagen",
                     getString(android.R.string.ok), this, null);
             return false;
         }
 
         if(dogGender == -1 || dogLifeStyle == -1 || dogActivity == -1 ||
                 dogAgeRange == -1 ||
                 ownerGender == -1 || ownerYear == -1 || ownerMonth == -1 ||
                 ownerDay == -1 || ownerState == -1) {
             UI.showAlertDialog("Upps!!",
                     "Favor de llenar todos los campos antes de continuar",
                     getString(android.R.string.ok), this, null);
             return false;
         }
 
         return true;
     }
 
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
             @Override
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
                 }catch(FileNotFoundException e) {
                     Log.e(DogUtil.DEBUG_TAG, e.getMessage(), e);
                 } catch (IOException e) {
                     Log.e(DogUtil.DEBUG_TAG, e.getMessage(), e);
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
 
     protected class DogRegisterAsync extends AsyncTask<Void, Integer, Integer> {
         private final Context context;
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
                 Integer userId = DogUtil.getInstance().getCurrentUserId();
                 String[][] dogs = wsDogUtils.getDuenosMascotasByIdUsuario(userId);
                 Integer result;
 
                 if(dogs == null) {
                     // No dogs registered
                     result = wsDogUtils.insertDuenoMascota(map);
                 } else {
                     // At least one dog registered
                     result = wsDogUtils.editDuenoMascota(map);
                 }
                 return result;
             } catch(XmlPullParserException e) {
                 Log.e(DogUtil.DEBUG_TAG, e.getMessage(), e);
                 return null;
             } catch(IOException e) {
                 Log.e(DogUtil.DEBUG_TAG, e.getMessage(), e);
                 return null;
             }
         }
 
         @Override
         protected void onPostExecute(Integer result) {
             super.onPostExecute(result);
             // Hide progress dialog
             dialog.dismiss();
 
             if(result==null) {
                 showOkDialog(context, "NO se pudo registrar", ONLY_DISMISS);
             } else {
                 // Return back results
                 Intent intent = new Intent();
                 intent.putExtra("OWNER_ID", result);
                 setResult(Activity.RESULT_OK, intent);
                 finish();
             }
         }
     }
 
 }
