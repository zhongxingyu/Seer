 package eu.tpmusielak.securephoto.viewer;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.TypedArray;
 import android.os.Bundle;
 import android.view.*;
 import android.widget.*;
 import eu.tpmusielak.securephoto.R;
 import eu.tpmusielak.securephoto.container.SPImageRoll;
 import eu.tpmusielak.securephoto.tools.FileHandling;
 import eu.tpmusielak.securephoto.viewer.lazylist.ImageLoader;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Tomasz P. Musielak
  * Date: 09.02.12
  * Time: 16:33
  */
 public class ViewImages extends Activity {
     public static final int THUMBNAIL_SIZE = 80;
 
     private Context mContext;
     private ListView listView;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setupScreen();
     }
 
     private void setupScreen() {
         setContentView(R.layout.gallery_view);
         getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
         listView = (ListView) findViewById(R.id.gallery_list);
         File[] files = FileHandling.getFiles();
 
         if (!(files == null) && files.length > 0) {
             ImageViewAdapter adapter = new ImageViewAdapter(ViewImages.this, R.layout.gallery_row, R.layout.gallery_roll_row, files);
 
             listView.setAdapter(adapter);
             listView.setOnItemClickListener(new ImageClickListener());
 
             registerForContextMenu(listView);
         } else {
             TextView textView = new TextView(ViewImages.this);
             textView.setText(R.string.no_files_found);
 
             TextView galleryInfo = (TextView) findViewById(R.id.gallery_info);
             galleryInfo.setText(R.string.no_files_found);
 
             galleryInfo.setVisibility(View.VISIBLE);
         }
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.view_menu, menu);
     }
 
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
 
         AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
 
         switch (item.getItemId()) {
             case R.id.delete:
                 deleteFile((File) listView.getItemAtPosition(info.position));
                 break;
             default:
                 break;
         }
 
         return false;
     }
 
     private void deleteFile(final File file) {
         final AlertDialog.Builder builder = new AlertDialog.Builder(ViewImages.this);
 
 
         builder.setMessage(R.string.ask_confirm)
                 .setCancelable(true)
                 .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int i) {
                         boolean success = file.delete();
                         int message = success ? R.string.delete_success : R.string.delete_failure;
                         Toast.makeText(ViewImages.this, message, Toast.LENGTH_SHORT).show();
 
                         setupScreen();
                     }
                 })
                 .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int i) {
                         dialog.cancel();
                     }
                 });
         final AlertDialog alertDialog = builder.create();
         alertDialog.show();
     }
 
 
     private class ImageViewAdapter extends ArrayAdapter<File> {
         // http://android-er.blogspot.com/2010/06/using-convertview-in-getview-to-make.html
 
         // inspired by: https://github.com/thest1/LazyList/
         private final int FRAME_VIEW = 0;
         private final int ROLL_VIEW = 1;
 
         private Context context;
         private int frameLayoutResourceId;
         private int rollLayoutResourceID;
         private File[] files;
 
         private LayoutInflater layoutInflater;
         private ImageLoader imageLoader;
 
         public ImageViewAdapter(Context context, int resourceIDForFrame, int resourceIDForRoll, File[] files) {
             super(context, resourceIDForFrame, files);
             this.context = context;
             this.frameLayoutResourceId = resourceIDForFrame;
             this.rollLayoutResourceID = resourceIDForRoll;
             this.files = files;
 
             layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             imageLoader = new ImageLoader(context);
         }
 
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             ViewHolder holder;
             int itemViewType = getItemViewType(position);
 
             if (convertView == null) {
                 holder = new ViewHolder();
 
                 switch (itemViewType) {
                     case ROLL_VIEW:
                         convertView = layoutInflater.inflate(rollLayoutResourceID, parent, false);
                         holder.image = convertView.findViewById(R.id.gallery_view);
                         break;
                     case FRAME_VIEW:
                     default:
                         convertView = layoutInflater.inflate(frameLayoutResourceId, parent, false);
                         holder.image = convertView.findViewById(R.id.file_view);
                         break;
                 }
 
                 holder.text = (TextView) convertView.findViewById(R.id.roll_descriptor);
                 convertView.setTag(holder);
             } else {
                 holder = (ViewHolder) convertView.getTag();
             }
 
             File file = getItem(position);
             String fileName = file.getName();
 
             holder.text.setText(fileName);
 
             switch (itemViewType) {
                 case ROLL_VIEW:
                     Gallery gallery = (Gallery) holder.image;
                     ImageRollAdapter adapter = (ImageRollAdapter) gallery.getAdapter();
 
                     if (adapter == null) {
                         adapter = new ImageRollAdapter(getContext(), file);
                         gallery.setAdapter(adapter);
                         gallery.setOnItemClickListener(new ImageRollClickListener(file));
                     } else {
                         adapter.setFile(file);
                         ((ImageRollClickListener) gallery.getOnItemClickListener()).setUnderlyingFile(file);
                     }
                     break;
                 case FRAME_VIEW:
                 default:
                     imageLoader.load(new ImageLoader.SingleImage(file), (ImageView) holder.image);
             }
 
             return convertView;
         }
 
         @Override
         public int getViewTypeCount() {
             return 2;
         }
 
         @Override
         public int getItemViewType(int position) {
             File file = getItem(position);
             String fileName = file.getName();
             if (fileName.endsWith(SPImageRoll.DEFAULT_EXTENSION)) {
                 return ROLL_VIEW;
             } else {
                 return FRAME_VIEW;
             }
         }
     }
 
     private static class ViewHolder {
         TextView text;
         View image;
     }
 
     private class ImageClickListener implements AdapterView.OnItemClickListener {
 
         @Override
         public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
             View fileView = view.findViewById(R.id.file_view);
 
             if (fileView == null) {
             } else if (fileView instanceof ImageView) {
                 File file = (File) parent.getItemAtPosition(position);
 
                 Intent i = new Intent(getApplicationContext(), OpenImage.class);
                 i.putExtra("filename", file.getAbsolutePath());
                 startActivity(i);
             }
         }
     }
 
     // Adapter for image roll
     private class ImageRollAdapter extends BaseAdapter {
         int mGalleryItemBackground;
         private Context mContext;
         private File file;
         private ImageLoader imageLoader;
         private SPImageRoll spImageRoll;
 
         public ImageRollAdapter(Context context, File file) {
             mContext = context;
             TypedArray attr = mContext.obtainStyledAttributes(R.styleable.HelloGallery);
             mGalleryItemBackground = attr.getResourceId(
                     R.styleable.HelloGallery_android_galleryItemBackground, 0);
             attr.recycle();
             this.file = file;
 
             try {
                 spImageRoll = SPImageRoll.fromFile(this.file);
             } catch (IOException ignored) {
             } catch (ClassNotFoundException ignored) {
             }
 
             imageLoader = new ImageLoader(mContext);
         }
 
         public void setFile(File file) {
             this.file = file;
             try {
                 spImageRoll = SPImageRoll.fromFile(file);
             } catch (IOException ignored) {
             } catch (ClassNotFoundException ignored) {
             }
         }
 
         public int getCount() {
             return spImageRoll.getFrameCount();
         }
 
         public Object getItem(int position) {
             return position;
         }
 
         public long getItemId(int position) {
             return position;
         }
 
         public View getView(int position, View convertView, ViewGroup parent) {
             ImageView imageView = (ImageView) convertView;
             if (imageView == null) {
                 imageView = new ImageView(mContext);
             }
 
             imageLoader.load(new ImageLoader.ImageRoll(file, position), imageView);
 
             imageView.setLayoutParams(new Gallery.LayoutParams(240, 160));
             imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
             imageView.setBackgroundResource(mGalleryItemBackground);
 
             return imageView;
         }
     }
 
     private class ImageRollClickListener implements AdapterView.OnItemClickListener {
         private File underlyingFile;
 
         private ImageRollClickListener(File underlyingFile) {
             this.underlyingFile = underlyingFile;
         }
 
         public void setUnderlyingFile(File underlyingFile) {
             this.underlyingFile = underlyingFile;
         }
 
         @Override
         public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
             Intent i = new Intent(getApplicationContext(), OpenImage.class);
             i.putExtra("filename", underlyingFile.getAbsolutePath());
             i.putExtra("frameIndex", position);
             startActivity(i);
         }
     }
 
 
 }
