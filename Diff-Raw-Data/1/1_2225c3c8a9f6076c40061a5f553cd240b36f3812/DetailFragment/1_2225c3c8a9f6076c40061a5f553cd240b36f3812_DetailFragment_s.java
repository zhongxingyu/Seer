 package net.solutinno.websearch;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v7.app.ActionBar;
 import android.support.v7.app.ActionBarActivity;
 import android.util.Patterns;
 import android.view.LayoutInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 import net.solutinno.util.DrawableHelper;
 import net.solutinno.websearch.data.DataProvider;
 import net.solutinno.websearch.data.SearchEngine;
 import net.solutinno.websearch.data.SearchEngineCursor;
 import net.solutinno.listener.SelectItemListener;
 import net.solutinno.util.NetworkHelper;
 import net.solutinno.util.StringHelper;
 import net.solutinno.websearch.provider.OpenSearchProvider;
 
 import java.net.URL;
 import java.util.UUID;
 
 public class DetailFragment extends Fragment implements SelectItemListener {
 
     private final int ICON_WIDTH = 48;
     private final int ICON_HEIGHT = 48;
 
     EditText mFieldImportUrl;
     EditText mFieldName;
     EditText mFieldUrl;
     EditText mFieldImageUrl;
     EditText mFieldDescription;
     ImageView mButtonAddSearchTerm;
     Button mButtonImport;
     Button mButtonLoadImage;
 
     SearchEngine mEngine;
 
     //TODO: Need to free the memory!!
     DetailController mDetailController;
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         mFieldImportUrl = (EditText) getView().findViewById(R.id.detail_fieldImportFromUrl);
         mFieldName = (EditText) getView().findViewById(R.id.detail_fieldName);
         mFieldUrl = (EditText) getView().findViewById(R.id.detail_fieldUrl);
         mFieldImageUrl = (EditText) getView().findViewById(R.id.detail_fieldImageUrl);
         mFieldDescription = (EditText) getView().findViewById(R.id.detail_fieldDescription);
 
         mButtonAddSearchTerm = (ImageView) getView().findViewById(R.id.detail_buttonAddSearchTerm);
         mButtonImport = (Button) getView().findViewById(R.id.detail_buttonImport);
         mButtonLoadImage = (Button) getView().findViewById(R.id.detail_buttonLoadImage);
 
         mButtonLoadImage.setOnClickListener(mButtonLoadImageClickListener);
         mButtonAddSearchTerm.setOnClickListener(mButtonAddSearchTermClickListener);
         mButtonImport.setOnClickListener(mButtonImportClickListener);
 
         UUID id = getActivity().getIntent().hasExtra(SearchEngineCursor.COLUMN_ID) ?  UUID.fromString(getActivity().getIntent().getStringExtra(SearchEngineCursor.COLUMN_ID)) : null;
         onSelectItem(id);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         return inflater.inflate(R.layout.fragment_detail, container, false);
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
 
     @Override
     public void onSelectItem(UUID id) {
         if (id != null) {
             mEngine = DataProvider.getSearchEngine(getActivity(), id);
         }
         else mEngine = new SearchEngine();
 
         setData();
     }
 
     View.OnClickListener mButtonImportClickListener = new View.OnClickListener() {
         @Override
         public void onClick(View view) {
             final String urlStr = StringHelper.GetStringFromCharSequence(mFieldImportUrl.getText());
             if (!Patterns.WEB_URL.matcher(urlStr).matches()) {
                 Toast.makeText(getActivity(), R.string.error_invalid_url, Toast.LENGTH_LONG).show();
                 return;
             }
             new AsyncTask<String, Integer, SearchEngine>() {
                 @Override
                 protected SearchEngine doInBackground(String... urls) {
                     return OpenSearchProvider.ReadOpenSearchXmlFromUrl(urls[0]);
                 }
                 @Override
                 protected void onPostExecute(SearchEngine engine) {
                     if (engine != null) {
                         UUID id = mEngine.id;
                         mEngine = engine;
                         mEngine.id = id == null ? UUID.randomUUID() : id;
                         setData();
                         mButtonLoadImageClickListener.onClick(null);
                     }
                 }
             }.execute(urlStr);
         }
     };
 
     View.OnClickListener mButtonAddSearchTermClickListener = new View.OnClickListener() {
         @Override
         public void onClick(View view) {
             if (getActivity().getCurrentFocus() != mFieldUrl) return;
 
             String text = StringHelper.GetStringFromCharSequence(mFieldUrl.getText());
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
             final URL url;
             try { url = new URL(StringHelper.GetStringFromCharSequence(mFieldImageUrl.getText())); }
             catch (Exception ex) {
                 Toast.makeText(getActivity(), R.string.error_invalid_url, Toast.LENGTH_LONG).show();
                 return;
             }
             new AsyncTask<URL, Integer, Bitmap>() {
                 @Override
                 protected Bitmap doInBackground(URL... urls) {
                     byte[] data = NetworkHelper.DownloadBinary(urls[0]);
                     return BitmapFactory.decodeByteArray(data, 0, data.length);
                 }
                 @Override
                 protected void onPostExecute(Bitmap bitmap) {
                     ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
                     BitmapDrawable icon = (BitmapDrawable) DrawableHelper.GetDrawableFromBitmap(bitmap, ICON_WIDTH, ICON_HEIGHT);
                     actionBar.setIcon(icon);
                     mEngine.image = icon;
                 }
             }.execute(url);
         }
     };
 
     public void SetDetailController(DetailController controller) {
         mDetailController = controller;
     }
 
 
     public void ClearFields() {
         mFieldName.setText("");
         mFieldUrl.setText("");
         mFieldImageUrl.setText("");
         mFieldDescription.setText("");
         ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle("");
         ((ActionBarActivity) getActivity()).getSupportActionBar().setIcon(R.drawable.ic_launcher);
     }
 
     private void setData() {
         ClearFields();
         if (mEngine.id != null) {
             mFieldName.setText(mEngine.name);
             mFieldUrl.setText(mEngine.url);
             mFieldImageUrl.setText(mEngine.imageUrl);
             mFieldDescription.setText(mEngine.description);
             if (mEngine.imageUri != null) {
                 Bitmap bmp = BitmapFactory.decodeFile(mEngine.imageUri.getPath());
                 if (bmp != null) {
                     ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
                     BitmapDrawable icon = (BitmapDrawable) DrawableHelper.GetDrawableFromBitmap(bmp, ICON_WIDTH, ICON_HEIGHT);
                     actionBar.setIcon(icon);
                 }
                 else ((ActionBarActivity) getActivity()).getSupportActionBar().setIcon(R.drawable.ic_launcher);
             }
             else ((ActionBarActivity) getActivity()).getSupportActionBar().setIcon(R.drawable.ic_launcher);
         }
     }
 
     private SearchEngine getData() {
         SearchEngine result = new SearchEngine();
         result.id = mEngine.id == null ? UUID.randomUUID() : mEngine.id;
         result.name = StringHelper.GetStringFromCharSequence(mFieldName.getText());
         result.url = StringHelper.GetStringFromCharSequence(mFieldUrl.getText());
         result.imageUrl = StringHelper.GetStringFromCharSequence(mFieldImageUrl.getText());
         result.description = StringHelper.GetStringFromCharSequence(mFieldDescription.getText());
         result.image = mEngine.image;
         return result;
     }
 
     public void Cancel() {
         if (mDetailController != null) {
             mDetailController.OnDetailFinish(MODE_CANCEL, null);
         }
     }
 
     public void Save() {
         String url = StringHelper.GetStringFromCharSequence(mFieldUrl.getText());
         boolean valid = !StringHelper.IsNullOrEmpty(mFieldName.getText())
             || !StringHelper.IsNullOrEmpty(url)
             || Patterns.WEB_URL.matcher(url.replace(SearchEngine.SEARCH_TERM, "")).matches();
 
         if (valid) {
             DataProvider.updateSearchEngine(getActivity(), getData());
         }
 
         if (mDetailController != null) {
             mDetailController.OnDetailFinish(MODE_UPDATE, valid ? mEngine : null);
         }
     }
 
     public void Delete() {
         DialogInterface.OnClickListener click = new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialogInterface, int i) {
                 if (i == DialogInterface.BUTTON_POSITIVE) {
                     DataProvider.deleteSearchEngine(getActivity(), getData());
                     if (mDetailController != null) {
                         mDetailController.OnDetailFinish(MODE_DELETE, mEngine);
                     }
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
 
     public static interface DetailController
     {
         void OnDetailFinish(int mode, SearchEngine engine);
     }
 }
 
 
