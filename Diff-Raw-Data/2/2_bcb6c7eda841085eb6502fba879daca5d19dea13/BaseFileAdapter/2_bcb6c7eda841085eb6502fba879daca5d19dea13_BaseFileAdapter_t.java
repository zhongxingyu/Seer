 /*
  *    Copyright (c) 2012 Hai Bison
  *
  *    See the file LICENSE at the root directory of this project for copying
  *    permission.
  */
 
 package group.pals.android.lib.ui.filechooser;
 
 import group.pals.android.lib.ui.filechooser.prefs.DisplayPrefs;
 import group.pals.android.lib.ui.filechooser.prefs.DisplayPrefs.FileTimeDisplay;
 import group.pals.android.lib.ui.filechooser.providers.BaseFileProviderUtils;
 import group.pals.android.lib.ui.filechooser.providers.basefile.BaseFileContract.BaseFile;
 import group.pals.android.lib.ui.filechooser.utils.Converter;
 import group.pals.android.lib.ui.filechooser.utils.DateUtils;
 import group.pals.android.lib.ui.filechooser.utils.FileUtils;
 import group.pals.android.lib.ui.filechooser.utils.Ui;
 import group.pals.android.lib.ui.filechooser.utils.ui.ContextMenuUtils;
 import group.pals.android.lib.ui.filechooser.utils.ui.LoadingDialog;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.net.Uri;
 import android.support.v4.widget.ResourceCursorAdapter;
 import android.util.SparseArray;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class BaseFileAdapter extends ResourceCursorAdapter {
 
     /**
      * Listener for building context menu editor.
      * 
      * @author Hai Bison
      * @since v5.1 beta
      */
     public static interface OnBuildOptionsMenuListener {
 
         /**
          * Will be called after the user touched on the icon of the item.
          * 
          * @param view
          *            the view displaying the item.
          * @param cursor
          *            the item which its icon has been touched.
          */
         void onBuildOptionsMenu(View view, Cursor cursor);
 
         /**
          * Will be called after the user touched and held ("long click") on the
          * icon of the item.
          * 
          * @param view
          *            the view displaying the item.
          * @param cursor
          *            the item which its icon has been touched.
          */
         void onBuildAdvancedOptionsMenu(View view, Cursor cursor);
     }// OnBuildOptionsMenuListener
 
     private final int mFilterMode;
     private final FileTimeDisplay mFileTimeDisplay;
     private final Integer[] mAdvancedSelectionOptions;
     private boolean mMultiSelection;
     private OnBuildOptionsMenuListener mOnBuildOptionsMenuListener;
 
     public BaseFileAdapter(Context context, int filterMode, boolean multiSelection) {
         super(context, R.layout.afc_file_item, null, 0);
         mFilterMode = filterMode;
         mMultiSelection = multiSelection;
 
         switch (mFilterMode) {
         case BaseFile._FilterFilesAndDirectories:
             mAdvancedSelectionOptions = new Integer[] { R.string.afc_cmd_advanced_selection_all,
                     R.string.afc_cmd_advanced_selection_none, R.string.afc_cmd_advanced_selection_invert,
                     R.string.afc_cmd_select_all_files, R.string.afc_cmd_select_all_folders };
             break;// _FilterFilesAndDirectories
         default:
             mAdvancedSelectionOptions = new Integer[] { R.string.afc_cmd_advanced_selection_all,
                     R.string.afc_cmd_advanced_selection_none, R.string.afc_cmd_advanced_selection_invert };
             break;// _FilterDirectoriesOnly and _FilterFilesOnly
         }
 
         mFileTimeDisplay = new FileTimeDisplay(DisplayPrefs.isShowTimeForOldDaysThisYear(context),
                 DisplayPrefs.isShowTimeForOldDays(context));
     }// BaseFileAdapter()
 
     @Override
     public int getCount() {
         /*
          * The last item is used for information from the provider, we ignore
          * it.
          */
         int count = super.getCount();
         return count > 0 ? count - 1 : 0;
     }// getCount()
 
     /**
      * The "view holder"
      * 
      * @author Hai Bison
      * 
      */
     private static final class Bag {
 
         ImageView mImageIcon;
         ImageView mImageLockedSymbol;
         TextView mTxtFileName;
         TextView mTxtFileInfo;
         CheckBox mCheckboxSelection;
     }// Bag
 
     private static class BagInfo {
 
         boolean mChecked = false;
         boolean mMarkedAsDeleted = false;
         Uri mUri;
     }// BagChildInfo
 
     /**
      * Map of child IDs to {@link BagChildInfo}.
      */
     private final SparseArray<BagInfo> mSelectedChildrenMap = new SparseArray<BagInfo>();
 
     @Override
     public void bindView(View view, Context context, Cursor cursor) {
         Bag bag = (Bag) view.getTag();
 
         if (bag == null) {
             bag = new Bag();
             bag.mImageIcon = (ImageView) view.findViewById(R.id.afc_file_item_imageview_icon);
             bag.mImageLockedSymbol = (ImageView) view.findViewById(R.id.afc_file_item_imageview_locked_symbol);
             bag.mTxtFileName = (TextView) view.findViewById(R.id.afc_file_item_textview_filename);
             bag.mTxtFileInfo = (TextView) view.findViewById(R.id.afc_file_item_textview_file_info);
             bag.mCheckboxSelection = (CheckBox) view.findViewById(R.id.afc_file_item_checkbox_selection);
 
             view.setTag(bag);
         }
 
         final int _id = cursor.getInt(cursor.getColumnIndex(BaseFile._ID));
         final Uri _uri = BaseFileProviderUtils.getUri(cursor);
 
         final BagInfo _bagInfo;
         if (mSelectedChildrenMap.get(_id) == null) {
             _bagInfo = new BagInfo();
             _bagInfo.mUri = _uri;
             mSelectedChildrenMap.put(_id, _bagInfo);
         } else
             _bagInfo = mSelectedChildrenMap.get(_id);
 
         /*
          * Update views.
          */
 
         /*
          * Use single line for grid view, multiline for list view
          */
         bag.mTxtFileName.setSingleLine(view.getParent() instanceof GridView);
 
         /*
          * File icon.
          */
         bag.mImageLockedSymbol
                 .setVisibility(cursor.getInt(cursor.getColumnIndex(BaseFile._ColumnCanRead)) > 0 ? View.GONE
                         : View.VISIBLE);
         bag.mImageIcon.setImageResource(FileUtils.getResIcon(
                 cursor.getInt(cursor.getColumnIndex(BaseFile._ColumnType)), BaseFileProviderUtils.getFileName(cursor)));
         bag.mImageIcon.setOnTouchListener(mImageIconOnTouchListener);
         bag.mImageIcon
                 .setOnClickListener(BaseFileProviderUtils.isDirectory(cursor) ? newImageIconOnClickListener(cursor
                         .getPosition()) : null);
 
         /*
          * Filename.
          */
         bag.mTxtFileName.setText(BaseFileProviderUtils.getFileName(cursor));
         Ui.strikeOutText(bag.mTxtFileName, _bagInfo.mMarkedAsDeleted);
 
         /*
          * File info.
          */
        String time = DateUtils.formatDate(context,
                 cursor.getLong(cursor.getColumnIndex(BaseFile._ColumnModificationTime)), mFileTimeDisplay);
         if (BaseFileProviderUtils.isFile(cursor))
             bag.mTxtFileInfo.setText(String.format("%s, %s",
                     Converter.sizeToStr(cursor.getLong(cursor.getColumnIndex(BaseFile._ColumnSize))), time));
         else
             bag.mTxtFileInfo.setText(time);
 
         /*
          * Check box.
          */
         if (mMultiSelection) {
             if (mFilterMode == BaseFile._FilterFilesOnly && BaseFileProviderUtils.isDirectory(cursor)) {
                 bag.mCheckboxSelection.setVisibility(View.GONE);
             } else {
                 bag.mCheckboxSelection.setVisibility(View.VISIBLE);
 
                 bag.mCheckboxSelection.setOnCheckedChangeListener(null);
                 bag.mCheckboxSelection.setChecked(_bagInfo.mChecked);
                 bag.mCheckboxSelection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
 
                     @Override
                     public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                         _bagInfo.mChecked = isChecked;
                     }// onCheckedChanged()
                 });
 
                 bag.mCheckboxSelection.setOnLongClickListener(mCheckboxSelectionOnLongClickListener);
             }
         } else
             bag.mCheckboxSelection.setVisibility(View.GONE);
     }// bindView()
 
     @Override
     public void changeCursor(Cursor cursor) {
         super.changeCursor(cursor);
         synchronized (mSelectedChildrenMap) {
             mSelectedChildrenMap.clear();
         }
     }// changeCursor()
 
     /*
      * UTILITIES.
      */
 
     /**
      * Sets the listener {@link OnBuildOptionsMenuListener}.
      * 
      * @param listener
      *            the listener.
      */
     public void setBuildOptionsMenuListener(OnBuildOptionsMenuListener listener) {
         mOnBuildOptionsMenuListener = listener;
     }// setBuildOptionsMenuListener()
 
     /**
      * Gets the listener {@link OnBuildOptionsMenuListener}.
      * 
      * @return the listener.
      */
     public OnBuildOptionsMenuListener getOnBuildOptionsMenuListener() {
         return mOnBuildOptionsMenuListener;
     }// getOnBuildOptionsMenuListener()
 
     /**
      * Gets the short name of this path.
      * 
      * @return the path name, can be {@code null} if there is no data.
      */
     public String getPathName() {
         Cursor cursor = getCursor();
         if (cursor == null || !cursor.moveToLast())
             return null;
         return BaseFileProviderUtils.getFileName(cursor);
     }// getPathName()
 
     /**
      * Selects all items.<br>
      * <b>Note:</b> This will <i>not</i> notify data set for changes after done.
      * 
      * @param selected
      *            {@code true} or {@code false}.
      */
     private void asyncSelectAll(boolean selected) {
         int count = getCount();
         for (int i = 0; i < count; i++) {
             Cursor cursor = (Cursor) getItem(i);
 
             int fileType = cursor.getInt(cursor.getColumnIndex(BaseFile._ColumnType));
             if ((mFilterMode == BaseFile._FilterDirectoriesOnly && fileType == BaseFile._FileTypeFile)
                     || (mFilterMode == BaseFile._FilterFilesOnly && fileType == BaseFile._FileTypeDirectory))
                 continue;
 
             final int _id = cursor.getInt(cursor.getColumnIndex(BaseFile._ID));
             BagInfo b = mSelectedChildrenMap.get(_id);
             if (b == null) {
                 b = new BagInfo();
                 mSelectedChildrenMap.put(_id, b);
             }
             b.mChecked = selected;
         }// for i
     }// asyncSelectAll()
 
     /**
      * Selects all items.<br>
      * <b>Note:</b> This calls {@link #notifyDataSetChanged()} after done.
      * 
      * @param selected
      *            {@code true} or {@code false}.
      */
     public synchronized void selectAll(boolean selected) {
         asyncSelectAll(selected);
         notifyDataSetChanged();
     }// selectAll()
 
     /**
      * Inverts selection of all items.<br>
      * <b>Note:</b> This will <i>not</i> notify data set for changes after done.
      */
     private void asyncInvertSelection() {
         int count = getCount();
         for (int i = 0; i < count; i++) {
             Cursor cursor = (Cursor) getItem(i);
 
             int fileType = cursor.getInt(cursor.getColumnIndex(BaseFile._ColumnType));
             if ((mFilterMode == BaseFile._FilterDirectoriesOnly && fileType == BaseFile._FileTypeFile)
                     || (mFilterMode == BaseFile._FilterFilesOnly && fileType == BaseFile._FileTypeDirectory))
                 continue;
 
             final int _id = cursor.getInt(cursor.getColumnIndex(BaseFile._ID));
             BagInfo b = mSelectedChildrenMap.get(_id);
             if (b == null) {
                 b = new BagInfo();
                 mSelectedChildrenMap.put(_id, b);
             }
             b.mChecked = !b.mChecked;
         }// for i
     }// asyncInvertSelection()
 
     /**
      * Inverts selection of all items.<br>
      * <b>Note:</b> This calls {@link #notifyDataSetChanged()} after done.
      */
     public synchronized void invertSelection() {
         asyncInvertSelection();
         notifyDataSetChanged();
     }// invertSelection()
 
     /**
      * Checks if item with {@code id} is selected or not.
      * 
      * @param id
      *            the database ID.
      * @return {@code true} or {@code false}.
      */
     public boolean isSelected(int id) {
         synchronized (mSelectedChildrenMap) {
             return mSelectedChildrenMap.get(id) != null ? mSelectedChildrenMap.get(id).mChecked : false;
         }
     }// isSelected()
 
     /**
      * Gets selected items.
      * 
      * @return list of URIs, can be empty.
      */
     public ArrayList<Uri> getSelectedItems() {
         ArrayList<Uri> res = new ArrayList<Uri>();
 
         synchronized (mSelectedChildrenMap) {
             for (int i = 0; i < mSelectedChildrenMap.size(); i++)
                 if (mSelectedChildrenMap.get(mSelectedChildrenMap.keyAt(i)).mChecked)
                     res.add(mSelectedChildrenMap.get(mSelectedChildrenMap.keyAt(i)).mUri);
         }
 
         return res;
     }// getSelectedItems()
 
     /**
      * Marks all selected items as deleted.<br>
      * <b>Note:</b> This calls {@link #notifyDataSetChanged()} after done.
      * 
      * @param deleted
      *            {@code true} or {@code false}.
      */
     public void markSelectedItemsAsDeleted(boolean deleted) {
         synchronized (mSelectedChildrenMap) {
             for (int i = 0; i < mSelectedChildrenMap.size(); i++)
                 if (mSelectedChildrenMap.get(mSelectedChildrenMap.keyAt(i)).mChecked)
                     mSelectedChildrenMap.get(mSelectedChildrenMap.keyAt(i)).mMarkedAsDeleted = deleted;
         }
 
         notifyDataSetChanged();
     }// markSelectedItemsAsDeleted()
 
     /**
      * Marks specified item as deleted.<br>
      * <b>Note:</b> This calls {@link #notifyDataSetChanged()} after done.
      * 
      * @param id
      *            the ID of the item.
      * @param deleted
      *            {@code true} or {@code false}.
      */
     public void markItemAsDeleted(int id, boolean deleted) {
         synchronized (mSelectedChildrenMap) {
             if (mSelectedChildrenMap.get(id) != null) {
                 mSelectedChildrenMap.get(id).mMarkedAsDeleted = deleted;
                 notifyDataSetChanged();
             }
         }
     }// markItemAsDeleted()
 
     /*
      * LISTENERS
      */
 
     /**
      * If the user touches the list item, and the image icon <i>declared</i> a
      * selector in XML, then that selector works. But we just want the selector
      * to work only when the user touches the image, hence this listener.
      */
     private final View.OnTouchListener mImageIconOnTouchListener = new View.OnTouchListener() {
 
         @Override
         public boolean onTouch(View v, MotionEvent event) {
             switch (event.getAction()) {
             case MotionEvent.ACTION_DOWN:
                 v.setBackgroundResource(R.drawable.afc_button_sort_symbol_dark_pressed);
                 break;
             case MotionEvent.ACTION_UP:
                 v.setBackgroundResource(0);
                 break;
             }
             return false;
         }// onTouch()
     };// mImageIconOnTouchListener
 
     /**
      * Creates new listener to handle click event of image icon.
      * 
      * @param cursorPosition
      *            the cursor position.
      * @return the listener.
      */
     private View.OnClickListener newImageIconOnClickListener(final int cursorPosition) {
         return new View.OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 if (getOnBuildOptionsMenuListener() != null)
                     getOnBuildOptionsMenuListener().onBuildOptionsMenu(v, (Cursor) getItem(cursorPosition));
             }// onClick()
         };
     }// newImageIconOnClickListener()
 
     private final View.OnLongClickListener mCheckboxSelectionOnLongClickListener = new View.OnLongClickListener() {
 
         @Override
         public boolean onLongClick(final View v) {
             ContextMenuUtils.showContextMenu(v.getContext(), 0, R.string.afc_title_advanced_selection,
                     mAdvancedSelectionOptions, new ContextMenuUtils.OnMenuItemClickListener() {
 
                         @Override
                         public void onClick(final int resId) {
                             new LoadingDialog(v.getContext(), R.string.afc_msg_loading, false) {
 
                                 @Override
                                 protected Object doInBackground(Void... params) {
                                     if (resId == R.string.afc_cmd_advanced_selection_all)
                                         asyncSelectAll(true);
                                     else if (resId == R.string.afc_cmd_advanced_selection_none)
                                         asyncSelectAll(false);
                                     else if (resId == R.string.afc_cmd_advanced_selection_invert)
                                         asyncInvertSelection();
                                     else if (resId == R.string.afc_cmd_select_all_files)
                                         asyncInvertSelection();
                                     else if (resId == R.string.afc_cmd_select_all_folders)
                                         asyncInvertSelection();
 
                                     return null;
                                 }// doInBackground()
 
                                 @Override
                                 protected void onPostExecute(Object result) {
                                     super.onPostExecute(result);
                                     notifyDataSetChanged();
                                 }// onPostExecute()
                             };
                         }// onClick()
                     });
 
             return true;
         }// onLongClick()
     };// mCheckboxSelectionOnLongClickListener
 }
