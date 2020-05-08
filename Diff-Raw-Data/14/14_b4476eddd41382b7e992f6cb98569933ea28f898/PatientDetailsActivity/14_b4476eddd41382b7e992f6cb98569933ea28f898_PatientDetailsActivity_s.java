 package com.medicalequation.me.gui.activity;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.LoaderManager;
 import android.content.*;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.animation.AnimationUtils;
 import android.widget.CheckBox;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.ViewFlipper;
 import com.medicalequation.me.C;
 import com.medicalequation.me.R;
 import com.medicalequation.me.db.PatientProvider;
 import com.medicalequation.me.db.PatientTable;
 import com.medicalequation.me.db.TreatmentTable;
 import com.medicalequation.me.exception.ValidateException;
 import com.medicalequation.me.gui.CalculateTask;
 import com.medicalequation.me.gui.TreatmentAdapter;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Иван Гусев
  * Date: 07.05.13
  * Time: 1:20
  * May the Force be with you, always
  */
 public class PatientDetailsActivity extends Activity implements Handler.Callback, LoaderManager.LoaderCallbacks<Cursor> {
 
 
     private static final String EDIT_MODE_KEY = "edit_mode";
     private static final String ID_KEY = "id";
 
     private boolean mEditMode;
 
     private ViewFlipper mFlipper;
     private ViewHolder mHolder;
     private Handler mHandler;
     private View mCalcWrapper;
     private Cursor cursor;
     private TreatmentAdapter mAdapter;
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putBoolean(EDIT_MODE_KEY, mEditMode);
     }
 
     @Override
     protected void onRestoreInstanceState(Bundle savedInstanceState) {
         super.onRestoreInstanceState(savedInstanceState);
         mEditMode = savedInstanceState.getBoolean(EDIT_MODE_KEY);
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.a_patient);
         mHandler = new Handler(this);
         mFlipper = (ViewFlipper) findViewById(R.id.flipper);
         mAdapter = new TreatmentAdapter(this);
         mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
     }
 
     @Override
     protected void onStart() {
         super.onStart();
        long id = getIntent().getLongExtra(C.Extra.ID, 0);
        if (mEditMode = id == 0) {
             refreshUI(null);
         } else {
             Bundle b = new Bundle();
             b.putLong(ID_KEY, id);
             getLoaderManager().initLoader(1, b, this);
         }
         getLoaderManager().initLoader(0, null, this);
     }
 
     @Override
     protected void onStop() {
         super.onStop();
         getLoaderManager().destroyLoader(0);
         getLoaderManager().destroyLoader(1);
     }
 
     private void refreshUI(Cursor cursor) {
         this.cursor = cursor;
         mFlipper.setDisplayedChild(mEditMode ? 1 : 0);
         if ((mHolder = (ViewHolder) mFlipper.getCurrentView().getTag()) == null) {
             mHolder = new ViewHolder();
             mHolder.fio = (TextView) mFlipper.getCurrentView().findViewById(R.id.patient_fio);
             mHolder.age = (TextView) mFlipper.getCurrentView().findViewById(R.id.patient_age);
             mHolder.stageTNM = (TextView) mFlipper.getCurrentView().findViewById(R.id.patient_stageTNM);
             mHolder.psa = (TextView) mFlipper.getCurrentView().findViewById(R.id.patient_psa);
             mHolder.glisson = (TextView) mFlipper.getCurrentView().findViewById(R.id.patient_glisson);
             mHolder.prostateVolume = (TextView) mFlipper.getCurrentView().findViewById(R.id.patient_prostateVolume);
             mHolder.prostateVolumeHifu = (TextView) mFlipper.getCurrentView().findViewById(R.id.patient_prostateVolumeHifu);
             mHolder.prostateLengthHifu = (TextView) mFlipper.getCurrentView().findViewById(R.id.patient_prostateLengthHifu);
             mHolder.prostateWidthHifu = (TextView) mFlipper.getCurrentView().findViewById(R.id.patient_prostateWidthHifu);
             mHolder.prostateHeightHifu = (TextView) mFlipper.getCurrentView().findViewById(R.id.patient_prostateHeightHifu);
             mHolder.maxUrineVelocity = (TextView) mFlipper.getCurrentView().findViewById(R.id.patient_maxUrineVelocity);
             mHolder.avUrineVelocity = (TextView) mFlipper.getCurrentView().findViewById(R.id.patient_avUrineVelocity);
             mHolder.residualUrine = (TextView) mFlipper.getCurrentView().findViewById(R.id.patient_residualUrine);
             mHolder.treatment = mFlipper.getCurrentView().findViewById(R.id.patient_treatment);
             mHolder.urinaryIncontinence = (TextView) mFlipper.getCurrentView().findViewById(R.id.patient_urinaryIncontinence);
             mHolder.acuteUrinaryRetention = (TextView) mFlipper.getCurrentView().findViewById(R.id.patient_acuteUrinaryRetention);
             mHolder.diseaseProgression = (TextView) mFlipper.getCurrentView().findViewById(R.id.patient_diseaseProgression);
             mHolder.stricture = (TextView) mFlipper.getCurrentView().findViewById(R.id.patient_stricture);
             mHolder.healed = mFlipper.getCurrentView().findViewById(R.id.patient_healed);
             if (mEditMode) {
                 mCalcWrapper = findViewById(R.id.calculateWrapper);
                 ((Spinner) mHolder.treatment).setAdapter(mAdapter);
                 mHolder.healed.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         mCalcWrapper.setVisibility(((CheckBox) v).isChecked() ? View.GONE : View.VISIBLE);
                     }
                 });
             }
         }
         if (cursor != null && cursor.moveToFirst()) {
             mHolder.fio.setText(cursor.getString(cursor.getColumnIndex(PatientTable.CN_FIO)));
             mHolder.age.setText(cursor.getString(cursor.getColumnIndex(PatientTable.CN_AGE)));
             mHolder.stageTNM.setText(cursor.getString(cursor.getColumnIndex(PatientTable.CN_STAGE_TNM)));
             mHolder.psa.setText(cursor.getString(cursor.getColumnIndex(PatientTable.CN_PSA)));
             mHolder.glisson.setText(cursor.getString(cursor.getColumnIndex(PatientTable.CN_GLISSON)));
             mHolder.prostateVolume.setText(cursor.getString(cursor.getColumnIndex(PatientTable.CN_PROSTATE_VOLUME)));
             mHolder.prostateVolumeHifu.setText(cursor.getString(cursor.getColumnIndex(PatientTable.CN_PROSTATE_VOLUME_HIFU)));
             mHolder.prostateLengthHifu.setText(cursor.getString(cursor.getColumnIndex(PatientTable.CN_PROSTATE_LENGTH_HIFU)));
             mHolder.prostateWidthHifu.setText(cursor.getString(cursor.getColumnIndex(PatientTable.CN_PROSTATE_WIDTH_HIFU)));
             mHolder.prostateHeightHifu.setText(cursor.getString(cursor.getColumnIndex(PatientTable.CN_PROSTATE_HEIGHT_HIFU)));
             mHolder.maxUrineVelocity.setText(cursor.getString(cursor.getColumnIndex(PatientTable.CN_MAX_URINE_VELOCITY)));
             mHolder.avUrineVelocity.setText(cursor.getString(cursor.getColumnIndex(PatientTable.CN_AV_URINE_VELOCITY)));
             mHolder.residualUrine.setText(cursor.getString(cursor.getColumnIndex(PatientTable.CN_RESIDUAL_URINE)));
             mHolder.urinaryIncontinence.setText(cursor.getString(cursor.getColumnIndex(PatientTable.CN_URINARY_INCONTINENCE)));
             mHolder.acuteUrinaryRetention.setText(cursor.getString(cursor.getColumnIndex(PatientTable.CN_ACUTE_URINARY_RETENTION)));
             mHolder.diseaseProgression.setText(cursor.getString(cursor.getColumnIndex(PatientTable.CN_DISEASE_PROGRESSION)));
             mHolder.stricture.setText(cursor.getString(cursor.getColumnIndex(PatientTable.CN_STRICTURE)));
             boolean healed = cursor.getInt(cursor.getColumnIndex(PatientTable.CN_HEALED)) == 1;
             long recommendedTherapy = cursor.getLong(cursor.getColumnIndex(PatientTable.СТ_TREATMENT_ID));
             if (mEditMode) {
                 mCalcWrapper.setVisibility(healed ? View.GONE : View.VISIBLE);
                 ((CheckBox) mHolder.healed).setChecked(healed);
                 ((Spinner) mHolder.treatment).setSelection(mAdapter.getPositionById(recommendedTherapy));
             } else {
                 ((TextView) mHolder.healed).setText(healed ? R.string.yes : R.string.no);
                 ((TextView) mHolder.treatment).setText(getTreatmentName(recommendedTherapy));
             }
         }
         mFlipper.getCurrentView().setTag(mHolder);
     }
 
     private String getTreatmentName(long id) {
         String result = getString(R.string.not_recommended);
         Cursor c = getContentResolver().query(Uri.parse(PatientProvider.TREATMENT_URI + "/" + id), null, null, null, null);
         if (c != null) {
             if (c.moveToFirst()) {
                 result = c.getString(c.getColumnIndex(TreatmentTable.CN_NAME));
             }
             c.close();
         }
         return result;
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         menu.clear();
         getMenuInflater().inflate(mEditMode ? R.menu.m_complete : R.menu.m_patient_details, menu);
         mFlipper.setInAnimation(this, mEditMode ? R.anim.slide_in_left : R.anim.slide_in_right);
         mFlipper.setOutAnimation(this, mEditMode ? R.anim.slide_out_right : R.anim.slide_out_left);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (item.getItemId() == R.id.edit) {
             mEditMode = !mEditMode;
             refreshUI(cursor);
             invalidateOptionsMenu();
         } else if (item.getItemId() == R.id.delete) {
             showDeleteDialog();
         } else if (item.getItemId() == R.id.done) {
             if (savePatient()) {
                 mEditMode = !mEditMode;
                 refreshUI(cursor);
                 invalidateOptionsMenu();
             }
         }
         return true;
     }
 
     private void showDeleteDialog() {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle(getString(R.string.deletion));
         builder.setMessage(getString(R.string.delete_confirmation));
         DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 if (which == DialogInterface.BUTTON_POSITIVE) {
                     Uri uriForDel = Uri.parse(PatientProvider.CONTENT_URI + "/" + cursor.getLong(cursor.getColumnIndex(PatientTable.CN_ID)));
                     getContentResolver().delete(uriForDel, null, null);
                     finish();
                 }
                 dialog.dismiss();
             }
         };
         builder.setNegativeButton(R.string.no, listener);
         builder.setPositiveButton(R.string.yes, listener);
         builder.create().show();
     }
 
     private boolean savePatient() {
         ContentValues cv = new ContentValues();
         String fio = mHolder.fio.getText().toString();
         if (fio.trim().isEmpty()) {
             mHolder.fio.setError(getString(R.string.e_emptyFio));
             mHolder.fio.requestFocus();
             return false;
         }
         int errorFieldPos = 1;
         try {
             int age = Integer.valueOf(mHolder.age.getText().toString());
             if (age < 1 || age > 120) {
                 throw new ValidateException(ValidateException.WRONG_AGE);
             }
             cv.put(PatientTable.CN_AGE, age);
             errorFieldPos++;
             cv.put(PatientTable.CN_STAGE_TNM, Integer.valueOf(mHolder.stageTNM.getText().toString()));
             errorFieldPos++;
             cv.put(PatientTable.CN_PSA, Double.valueOf(mHolder.psa.getText().toString()));
             errorFieldPos++;
             int glisson = Integer.valueOf(mHolder.glisson.getText().toString());
             if (glisson < 2 || glisson > 10) {
                 throw new ValidateException(ValidateException.WRONG_GLISSON);
             }
             cv.put(PatientTable.CN_GLISSON, glisson);
             errorFieldPos++;
             cv.put(PatientTable.CN_PROSTATE_VOLUME, Double.valueOf(mHolder.prostateVolume.getText().toString()));
             errorFieldPos++;
             cv.put(PatientTable.CN_PROSTATE_VOLUME_HIFU, Double.valueOf(mHolder.prostateVolumeHifu.getText().toString()));
             errorFieldPos++;
             cv.put(PatientTable.CN_PROSTATE_LENGTH_HIFU, Double.valueOf(mHolder.prostateLengthHifu.getText().toString()));
             errorFieldPos++;
             cv.put(PatientTable.CN_PROSTATE_WIDTH_HIFU, Double.valueOf(mHolder.prostateWidthHifu.getText().toString()));
             errorFieldPos++;
             cv.put(PatientTable.CN_PROSTATE_HEIGHT_HIFU, Double.valueOf(mHolder.prostateHeightHifu.getText().toString()));
             errorFieldPos++;
             cv.put(PatientTable.CN_MAX_URINE_VELOCITY, Double.valueOf(mHolder.maxUrineVelocity.getText().toString()));
             errorFieldPos++;
             cv.put(PatientTable.CN_AV_URINE_VELOCITY, Double.valueOf(mHolder.avUrineVelocity.getText().toString()));
             errorFieldPos++;
             cv.put(PatientTable.CN_RESIDUAL_URINE, Double.valueOf(mHolder.residualUrine.getText().toString()));
         } catch (ValidateException e) {
             showValidateErrorField(errorFieldPos, e);
             return false;
         } catch (NumberFormatException e) {
             showFormatErrorField(errorFieldPos);
             return false;
         }
         cv.put(PatientTable.CN_FIO, fio);
         cv.put(PatientTable.CN_URINARY_INCONTINENCE, mHolder.urinaryIncontinence.getText().toString());
         cv.put(PatientTable.CN_ACUTE_URINARY_RETENTION, mHolder.acuteUrinaryRetention.getText().toString());
         cv.put(PatientTable.CN_DISEASE_PROGRESSION, mHolder.diseaseProgression.getText().toString());
         cv.put(PatientTable.CN_STRICTURE, mHolder.stricture.getText().toString());
         cv.put(PatientTable.CN_HEALED, ((CheckBox) mHolder.healed).isChecked() ? 1 : 0);
         cv.put(PatientTable.СТ_TREATMENT_ID, ((Spinner) mHolder.treatment).getSelectedItemId());
         long id;
         if (cursor != null) {
             id = cursor.getLong(cursor.getColumnIndex(PatientTable.CN_ID));
             getContentResolver().update(Uri.parse(PatientProvider.CONTENT_URI + "/" + id), cv, null, null);
         } else {
             id = ContentUris.parseId(getContentResolver().insert(PatientProvider.CONTENT_URI, cv));
         }
         cursor = getContentResolver().query(Uri.parse(PatientProvider.CONTENT_URI + "/" + id), null, null, null, null);
         return true;
     }
 
     public void onClick(View v) {
         if (v.getId() == R.id.calculate) {
             Double[] params = new Double[13];
             int errorFieldPos = 1;
             params[0] = 1.0;
             try {
                 params[1] = Double.valueOf(Integer.valueOf(mHolder.age.getText().toString()));
                 if (params[1] < 1 || params[1] > 120) {
                     throw new ValidateException(ValidateException.WRONG_AGE);
                 }
                 errorFieldPos++;
                 params[2] = Double.valueOf(Integer.valueOf(mHolder.stageTNM.getText().toString()));
                 errorFieldPos++;
                 params[3] = Double.valueOf(mHolder.psa.getText().toString());
                 errorFieldPos++;
                 params[4] = Double.valueOf(Integer.valueOf(mHolder.glisson.getText().toString()));
                 if (params[4] < 2 || params[4] > 10) {
                     throw new ValidateException(ValidateException.WRONG_GLISSON);
                 }
                 errorFieldPos++;
                 params[5] = Double.valueOf(mHolder.prostateVolume.getText().toString());
                 errorFieldPos++;
                 params[6] = Double.valueOf(mHolder.prostateVolumeHifu.getText().toString());
                 errorFieldPos++;
                 params[7] = Double.valueOf(mHolder.prostateLengthHifu.getText().toString());
                 errorFieldPos++;
                 params[8] = Double.valueOf(mHolder.prostateWidthHifu.getText().toString());
                 errorFieldPos++;
                 params[9] = Double.valueOf(mHolder.prostateHeightHifu.getText().toString());
                 errorFieldPos++;
                 params[10] = Double.valueOf(mHolder.maxUrineVelocity.getText().toString());
                 errorFieldPos++;
                 params[11] = Double.valueOf(mHolder.avUrineVelocity.getText().toString());
                 errorFieldPos++;
                 params[12] = Double.valueOf(mHolder.residualUrine.getText().toString());
 
                 new CalculateTask(this, mHandler).execute(params);
             } catch (ValidateException e) {
                 showValidateErrorField(errorFieldPos, e);
             } catch (NumberFormatException e) {
                 showFormatErrorField(errorFieldPos);
             }
         }
     }
 
     private void showValidateErrorField(int errorFieldPos, ValidateException e) {
         switch (errorFieldPos) {
             case 1:
                 mHolder.age.setError(e.getMessage());
                 mHolder.age.requestFocus();
                 break;
             case 4:
                 mHolder.glisson.setError(e.getMessage());
                 mHolder.glisson.requestFocus();
                 break;
         }
     }
 
     private void showFormatErrorField(int errorFieldPos) {
         switch (errorFieldPos) {
             case 1:
                 mHolder.age.setError(getString(R.string.e_illegalInput));
                 mHolder.age.requestFocus();
                 break;
             case 2:
                 mHolder.stageTNM.setError(getString(R.string.e_illegalInput));
                 mHolder.stageTNM.requestFocus();
                 break;
             case 3:
                 mHolder.psa.setError(getString(R.string.e_illegalInput));
                 mHolder.psa.requestFocus();
                 break;
             case 4:
                 mHolder.glisson.setError(getString(R.string.e_illegalInput));
                 mHolder.glisson.requestFocus();
                 break;
             case 5:
                 mHolder.prostateVolume.setError(getString(R.string.e_illegalInput));
                 mHolder.prostateVolume.requestFocus();
                 break;
             case 6:
                 mHolder.prostateVolumeHifu.setError(getString(R.string.e_illegalInput));
                 mHolder.prostateVolumeHifu.requestFocus();
                 break;
             case 7:
                 mHolder.prostateLengthHifu.setError(getString(R.string.e_illegalInput));
                 mHolder.prostateLengthHifu.requestFocus();
                 break;
             case 8:
                 mHolder.prostateWidthHifu.setError(getString(R.string.e_illegalInput));
                 mHolder.prostateWidthHifu.requestFocus();
                 break;
             case 9:
                 mHolder.prostateHeightHifu.setError(getString(R.string.e_illegalInput));
                 mHolder.prostateHeightHifu.requestFocus();
                 break;
             case 10:
                 mHolder.maxUrineVelocity.setError(getString(R.string.e_illegalInput));
                 mHolder.maxUrineVelocity.requestFocus();
                 break;
             case 11:
                 mHolder.avUrineVelocity.setError(getString(R.string.e_illegalInput));
                 mHolder.avUrineVelocity.requestFocus();
                 break;
             case 12:
                 mHolder.residualUrine.setError(getString(R.string.e_illegalInput));
                 mHolder.residualUrine.requestFocus();
                 break;
         }
     }
 
     @Override
     public boolean handleMessage(Message msg) {
         if (msg.what == C.What.RESULT) {
             Bundle data = msg.getData();
             double[] y = data.getDoubleArray(C.Extra.RESULTS);
             ((Spinner) mHolder.treatment).setSelection(data.getInt(C.Extra.TREATMENT_INDEX));
             mHolder.urinaryIncontinence.setText(String.valueOf(new BigDecimal(y[0]).setScale(2, RoundingMode.HALF_UP).doubleValue()));
             mHolder.acuteUrinaryRetention.setText(String.valueOf(new BigDecimal(y[1]).setScale(2, RoundingMode.HALF_UP).doubleValue()));
             mHolder.diseaseProgression.setText(String.valueOf(new BigDecimal(y[2]).setScale(2, RoundingMode.HALF_UP).doubleValue()));
             mHolder.stricture.setText(String.valueOf(new BigDecimal(y[3]).setScale(2, RoundingMode.HALF_UP).doubleValue()));
             View layout = findViewById(R.id.calcLayout);
             layout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fly_results));
             return true;
         }
         return false;
     }
 
     @Override
     public void onBackPressed() {
         if (mEditMode && cursor != null) {
             mEditMode = !mEditMode;
             refreshUI(cursor);
             invalidateOptionsMenu();
         } else {
             super.onBackPressed();
         }
     }
 
     private List<TreatmentAdapter.KeyValueItem> parseCursor(Cursor cursor) {
         List<TreatmentAdapter.KeyValueItem> keyValueItems = new ArrayList<TreatmentAdapter.KeyValueItem>();
         if (cursor != null) {
             if (cursor.moveToFirst()) {
                 do {
                     keyValueItems.add(new TreatmentAdapter.KeyValueItem(cursor.getLong(0), cursor.getString(1)));
                 } while (cursor.moveToNext());
             }
             cursor.close();
         }
         return keyValueItems;
     }
 
     @Override
     public Loader<Cursor> onCreateLoader(int id, Bundle args) {
         if (id == 0) {
             return new CursorLoader(this, PatientProvider.TREATMENT_URI, null, null, null, null);
         } else {
             return new CursorLoader(this, Uri.parse(PatientProvider.CONTENT_URI + "/" + args.getLong(ID_KEY)), null, null, null, null);
         }
     }
 
     @Override
     public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
         if (loader.getId() == 0) {
             mAdapter.setItems(data);
         } else {
             refreshUI(data);
         }
     }
 
     @Override
     public void onLoaderReset(Loader<Cursor> loader) {
         mAdapter.setItems(null);
     }
 
     static class ViewHolder {
         public TextView fio;
         public TextView age;
         public TextView stageTNM;
         public TextView psa;
         public TextView glisson;
         public TextView prostateVolume;
         public TextView prostateVolumeHifu;
         public TextView prostateLengthHifu;
         public TextView prostateWidthHifu;
         public TextView prostateHeightHifu;
         public TextView maxUrineVelocity;
         public TextView avUrineVelocity;
         public TextView residualUrine;
         public View treatment;
         public TextView urinaryIncontinence;
         public TextView acuteUrinaryRetention;
         public TextView diseaseProgression;
         public TextView stricture;
         public View healed;
     }
 }
