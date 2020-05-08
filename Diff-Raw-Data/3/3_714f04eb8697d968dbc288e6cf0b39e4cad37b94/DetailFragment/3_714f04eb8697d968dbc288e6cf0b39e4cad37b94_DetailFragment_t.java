 package net.solutinno.websearch;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v7.app.ActionBar;
 import android.support.v7.app.ActionBarActivity;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import net.solutinno.util.DrawableHelper;
 import net.solutinno.util.SoftKeyboardHelper;
 import net.solutinno.util.UrlHelper;
 import net.solutinno.websearch.data.DataProvider;
 import net.solutinno.websearch.data.SearchEngine;
 import net.solutinno.websearch.data.SearchEngineCursor;
 import net.solutinno.util.NetworkHelper;
 import net.solutinno.util.StringHelper;
 import net.solutinno.widget.ToastHandler;
 import net.solutinno.widget.ToastValidationProvider;
 
 import java.lang.ref.WeakReference;
 import java.net.URL;
 import java.util.UUID;
 
 public class DetailFragment extends Fragment {
 
     private final int ICON_WIDTH = 48;
     private final int ICON_HEIGHT = 48;
 
     EditText mFieldName;
     EditText mFieldUrl;
     EditText mFieldImageUrl;
     EditText mFieldDescription;
     ImageView mButtonAddSearchTerm;
     ImageView mButtonLoadImage;
 
     ProgressBar mProgressBar;
 
     SearchEngine mEngine;
 
     ToastValidationProvider mValidationProvider;
     ToastHandler mToastHandler;
 
     WeakReference<CloseListener> mDetailCloseListener;
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         mToastHandler = new ToastHandler(getActivity());
         mValidationProvider = new ToastValidationProvider(mToastHandler);
         mValidationProvider.setOnValidate(mOnValidate);
         return inflater.inflate(R.layout.fragment_detail, container, false);
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         mProgressBar = (ProgressBar) getView().findViewById(R.id.detail_progressBar);
         mProgressBar.setVisibility(View.GONE);
 
         mFieldName = (EditText) getView().findViewById(R.id.detail_fieldName);
         mFieldName.setOnFocusChangeListener(mOnFocusChangeListener);
         mFieldUrl = (EditText) getView().findViewById(R.id.detail_fieldUrl);
         mFieldUrl.setOnFocusChangeListener(mOnFocusChangeListener);
         mFieldImageUrl = (EditText) getView().findViewById(R.id.detail_fieldImageUrl);
         mFieldImageUrl.setOnFocusChangeListener(mOnFocusChangeListener);
         mFieldDescription = (EditText) getView().findViewById(R.id.detail_fieldDescription);
         mFieldDescription.setOnFocusChangeListener(mOnFocusChangeListener);
 
         mButtonAddSearchTerm = (ImageView) getView().findViewById(R.id.detail_buttonAddSearchTerm);
         mButtonLoadImage = (ImageView) getView().findViewById(R.id.detail_buttonLoadImage);
 
         mButtonAddSearchTerm.setOnClickListener(mButtonAddSearchTermClickListener);
         mButtonLoadImage.setOnClickListener(mButtonLoadImageClickListener);
 
         UUID id = getArguments() == null || !getArguments().containsKey(SearchEngineCursor.COLUMN_ID) ? null : UUID.fromString(getArguments().getString(SearchEngineCursor.COLUMN_ID));
         onSelectItem(id);
 
         getView().findViewById(R.id.detail_container).requestFocus();
     }
 
     @Override
     public void onPause() {
         super.onPause();
         mToastHandler.cancel();
         SoftKeyboardHelper.closeSoftKeyboard(getActivity());
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         mDetailCloseListener = null;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()){
             case R.id.action_delete:
                 Delete();
                 break;
             case R.id.action_save:
                 Save();
                 break;
             case android.R.id.home:
             case R.id.action_cancel:
                 Cancel();
                 break;
         }
         return true;
     }
 
     private void onSelectItem(UUID id) {
         if (id != null) mEngine = DataProvider.getSearchEngine(getActivity(), id);
         else mEngine = new SearchEngine();
         setData();
     }
 
     View.OnFocusChangeListener mOnFocusChangeListener = new View.OnFocusChangeListener() {
         @Override
         public void onFocusChange(View view, boolean b) {
             if (view instanceof EditText && b && !StringHelper.isNullOrEmpty(((EditText)view).getText())) {
                 int[] loc = new int[2]; view.getLocationOnScreen(loc);
                 Toast toast = mToastHandler.getToast(((EditText)view).getHint(), Toast.LENGTH_SHORT);
                 toast.setGravity(Gravity.START | Gravity.TOP, loc[0], loc[1]);
                 toast.setMargin(0, 0);
                 mToastHandler.show(toast);
             }
            else if (view instanceof EditText && !b) {
                mToastHandler.cancel();
            }
         }
     };
 
     View.OnClickListener mButtonAddSearchTermClickListener = new View.OnClickListener() {
         @Override
         public void onClick(View view) {
             if (getActivity().getCurrentFocus() != mFieldUrl) return;
             String text = StringHelper.getStringFromCharSequence(mFieldUrl.getText());
             int selStart = mFieldUrl.getSelectionStart();
             int selEnd = mFieldUrl.getSelectionEnd();
             text = text.substring(0, selStart) + SearchEngine.SEARCH_TERM + text.substring(selEnd);
             selEnd = selStart + SearchEngine.SEARCH_TERM.length();
             mFieldUrl.setText(text);
             mFieldUrl.setSelection(selEnd);
         }
     };
 
     View.OnClickListener mButtonLoadImageClickListener = new View.OnClickListener() {
         @Override
         public void onClick(View view) {
             if (!mValidationProvider.validate(mFieldImageUrl)) return;
             mProgressBar.setVisibility(View.VISIBLE);
             String url = StringHelper.getStringFromCharSequence(mFieldImageUrl.getText());
             new AsyncTask<String, Integer, Bitmap>() {
                 @Override
                 protected Bitmap doInBackground(String... urls) {
                     try {
                         byte[] data = NetworkHelper.downloadIntoByteArray(new URL(urls[0]));
                         return BitmapFactory.decodeByteArray(data, 0, data.length);
                     }
                     catch (Exception ex) {
                         ex.printStackTrace();
                         return null;
                     }
                 }
                 @Override
                 protected void onPostExecute(Bitmap bitmap) {
                     mToastHandler.show(mToastHandler.getToast(R.string.information_image_download_successfull, Toast.LENGTH_LONG));
                     ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
                     BitmapDrawable icon = (BitmapDrawable) DrawableHelper.getDrawableFromBitmap(bitmap, ICON_WIDTH, ICON_HEIGHT);
                     actionBar.setIcon(icon);
                     mProgressBar.setVisibility(View.GONE);
                 }
             }.execute(url);
         }
     };
 
     ToastValidationProvider.OnValidate mOnValidate = new ToastValidationProvider.OnValidate() {
         @Override
         public Integer validate(View view) {
             if (view == mFieldName) {
                 String name = StringHelper.getStringFromCharSequence(mFieldName.getText());
                 if (StringHelper.isNullOrEmpty(name))
                     return R.string.error_name_required;
             }
             else if (view == mFieldUrl) {
                 String url = StringHelper.getStringFromCharSequence(mFieldUrl.getText());
                 if (StringHelper.isNullOrEmpty(url))
                     return R.string.error_url_required;
                 if (!UrlHelper.isUrlValid(url.replace(SearchEngine.SEARCH_TERM, "")))
                     return R.string.error_url_invalid;
                 if (!url.contains(SearchEngine.SEARCH_TERM))
                     return R.string.error_url_missing_term;
             }
             else if (view == mFieldImageUrl) {
                 String url = StringHelper.getStringFromCharSequence(mFieldImageUrl.getText());
                 if (StringHelper.isNullOrEmpty(url) || !UrlHelper.isUrlValid(url))
                     return R.string.error_image_url_invalid;
             }
             return null;
         }
     };
 
     public void SetDetailCloseListener(CloseListener listener) {
         mDetailCloseListener = new WeakReference<CloseListener>(listener);
     }
 
     public void ClearFields() {
         mFieldName.setText("");
         mFieldUrl.setText("");
         mFieldImageUrl.setText("");
         mFieldDescription.setText("");
         ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle("");
         ((ActionBarActivity) getActivity()).getSupportActionBar().setIcon(R.drawable.ic_launcher);
     }
 
     private SearchEngine getData() {
         SearchEngine result = new SearchEngine();
         result.id = mEngine.id == null ? UUID.randomUUID() : mEngine.id;
         result.name = StringHelper.getStringFromCharSequence(mFieldName.getText());
         result.url = StringHelper.getStringFromCharSequence(mFieldUrl.getText());
         result.imageUrl = StringHelper.getStringFromCharSequence(mFieldImageUrl.getText());
         result.description = StringHelper.getStringFromCharSequence(mFieldDescription.getText());
         return result;
     }
 
     private void setData() {
         ClearFields();
         if (mEngine.id != null) {
             mFieldName.setText(mEngine.name);
             mFieldUrl.setText(mEngine.url);
             mFieldImageUrl.setText(mEngine.imageUrl);
             mFieldDescription.setText(mEngine.description);
             setImageFromUri(mEngine.imageUri);
         }
     }
 
     private void setImageFromUri(Uri uri) {
         if (uri != null) {
             Bitmap bmp = BitmapFactory.decodeFile(uri.getPath());
             if (bmp != null) {
                 ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
                 BitmapDrawable icon = (BitmapDrawable) DrawableHelper.getDrawableFromBitmap(bmp, ICON_WIDTH, ICON_HEIGHT);
                 actionBar.setIcon(icon);
                 return;
             }
         }
         ((ActionBarActivity) getActivity()).getSupportActionBar().setIcon(R.drawable.ic_launcher);
     }
 
     public void detailClose(int mode) {
         if (mDetailCloseListener != null) {
             final CloseListener detailController = mDetailCloseListener.get();
             if (detailController != null) {
                 detailController.onDetailClosed(mode, mEngine);
             }
         }
     }
 
     public void Cancel() {
         detailClose(MODE_CANCEL);
     }
 
     public void Save() {
         if (mValidationProvider.validate(new TextView[] {mFieldName, mFieldUrl})) {
             mProgressBar.setVisibility(View.VISIBLE);
             new AsyncTask<DetailFragment, Integer, SearchEngine>() {
                 @Override
                 protected SearchEngine doInBackground(DetailFragment... fragments) {
                     mEngine = getData();
                     DataProvider.updateSearchEngine(getActivity(), mEngine);
                     return null;
                 }
                 @Override
                 protected void onPostExecute(SearchEngine engine) {
                     mProgressBar.setVisibility(View.GONE);
                     detailClose(MODE_UPDATE);
                 }
             }.execute(this);
         }
     }
 
     public void Delete() {
         if (mEngine.id == null) {
             detailClose(MODE_DELETE);
             return;
         }
 
         DialogInterface.OnClickListener click = new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialogInterface, int i) {
                 if (i == DialogInterface.BUTTON_POSITIVE) {
                     DataProvider.deleteSearchEngine(getActivity(), getData());
                     detailClose(MODE_DELETE);
                 }
             }
         };
 
         new AlertDialog.Builder(getActivity())
             .setTitle(R.string.dialog_confirmation)
             .setMessage(R.string.confirmation_delete)
             .setCancelable(true)
             .setNegativeButton(R.string.no, click)
             .setPositiveButton(R.string.yes, click)
             .show();
     }
 
     public static final int MODE_DELETE     = -1;
     public static final int MODE_CANCEL     = 0;
     public static final int MODE_UPDATE     = 1;
 
     public static interface CloseListener
     {
         void onDetailClosed(int mode, SearchEngine engine);
     }
 }
 
 
