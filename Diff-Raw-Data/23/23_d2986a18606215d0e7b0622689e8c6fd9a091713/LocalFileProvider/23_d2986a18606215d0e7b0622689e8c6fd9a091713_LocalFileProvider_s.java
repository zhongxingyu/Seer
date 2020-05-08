 /*
  *    Copyright (c) 2012 Hai Bison
  *
  *    See the file LICENSE at the root directory of this project for copying
  *    permission.
  */
 
 package group.pals.android.lib.ui.filechooser.providers.localfile;
 
 import group.pals.android.lib.ui.filechooser.BuildConfig;
 import group.pals.android.lib.ui.filechooser.R;
 import group.pals.android.lib.ui.filechooser.providers.BaseFileProviderUtils;
 import group.pals.android.lib.ui.filechooser.providers.ProviderUtils;
 import group.pals.android.lib.ui.filechooser.providers.basefile.BaseFileContract.BaseFile;
 import group.pals.android.lib.ui.filechooser.providers.basefile.BaseFileProvider;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.text.Collator;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.concurrent.CancellationException;
 import java.util.regex.Pattern;
 
 import android.content.ContentValues;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.MatrixCursor;
 import android.database.MatrixCursor.RowBuilder;
 import android.net.Uri;
 import android.os.Environment;
 import android.util.Log;
 import android.util.SparseBooleanArray;
 
 /**
  * Local file provider.
  * 
  * @since v5.1 beta
  * @author Hai Bison
  * 
  */
 public class LocalFileProvider extends BaseFileProvider {
 
     /**
      * Used for debugging or something...
      */
     private static final String _ClassName = LocalFileProvider.class.getName();
 
     /*
      * Constants used by the Uri matcher to choose an action based on the
      * pattern of the incoming URI.
      */
 
     /**
      * The incoming URI matches the directory's contents URI pattern.
      */
     private static final int _Directory = 1;
 
     /**
      * The incoming URI matches the single file URI pattern.
      */
     private static final int _File = 2;
 
     /**
      * The incoming URI matches the identification URI pattern.
      */
     private static final int _Api = 3;
 
     /**
      * The incoming URI matches the API command URI pattern.
      */
     private static final int _ApiCommand = 4;
 
     /**
      * A {@link UriMatcher} instance.
      */
     private static final UriMatcher _UriMatcher = new UriMatcher(
             UriMatcher.NO_MATCH);
 
     /**
      * Map of task IDs to their interruption signals.
      */
     private static final SparseBooleanArray _MapInterruption = new SparseBooleanArray();
 
     static {
         _UriMatcher.addURI(LocalFileContract._Authority, BaseFile._PathDir
                 + "/*", _Directory);
         _UriMatcher.addURI(LocalFileContract._Authority, BaseFile._PathFile
                 + "/*", _File);
         _UriMatcher.addURI(LocalFileContract._Authority, BaseFile._PathApi,
                 _Api);
         _UriMatcher.addURI(LocalFileContract._Authority, BaseFile._PathApi
                 + "/*", _ApiCommand);
     }// static
 
     private final Collator mCollator = Collator.getInstance();
     private FileObserverEx mFileObserverEx;
 
     @Override
     public String getType(Uri uri) {
         /*
          * Chooses the MIME type based on the incoming URI pattern.
          */
         switch (_UriMatcher.match(uri)) {
         case _Api:
         case _ApiCommand:
         case _Directory:
             return BaseFile._ContentType;
 
         case _File:
             return BaseFile._ContentItemType;
 
         default:
             throw new IllegalArgumentException("Unknown URI " + uri);
         }
     }// getType()
 
     @Override
     public int delete(Uri uri, String selection, String[] selectionArgs) {
         if (BuildConfig.DEBUG)
             Log.d(_ClassName, "delete() >> " + uri);
 
         int count = 0;
 
         switch (_UriMatcher.match(uri)) {
         case _File: {
             int taskId = ProviderUtils.getIntQueryParam(uri,
                     BaseFile._ParamTaskId, 0);
 
             boolean isRecursive = ProviderUtils.getBooleanQueryParam(uri,
                     BaseFile._ParamRecursive, true);
             File file = extractFile(uri);
             if (file.canWrite()) {
                 File parentFile = file.getParentFile();
 
                 if (file.isFile() || !isRecursive) {
                     if (file.delete())
                         count = 1;
                 } else {
                     _MapInterruption.put(taskId, false);
                     count = deleteFile(taskId, file, isRecursive);
                     if (_MapInterruption.get(taskId))
                         if (BuildConfig.DEBUG)
                             Log.d(_ClassName, "delete() >> cancelled...");
                     _MapInterruption.delete(taskId);
                 }
 
                 if (count > 0) {
                     getContext().getContentResolver()
                             .notifyChange(
                                     BaseFile.genContentUriBase(
                                             LocalFileContract._Authority)
                                             .buildUpon()
                                             .appendPath(
                                                     Uri.fromFile(parentFile)
                                                             .toString())
                                             .build(), null);
                 }
             }
 
             break;// _File
         }
 
         default:
             throw new IllegalArgumentException("Unknown URI " + uri);
         }
 
         if (BuildConfig.DEBUG)
             Log.d(_ClassName, "delete() >> count = " + count);
 
         if (count > 0)
             getContext().getContentResolver().notifyChange(uri, null);
 
         return count;
     }// delete()
 
     @Override
     public Uri insert(Uri uri, ContentValues values) {
         if (BuildConfig.DEBUG)
             Log.d(_ClassName, "insert() >> " + uri);
 
         switch (_UriMatcher.match(uri)) {
         case _Directory:
             File file = extractFile(uri);
             if (!file.isDirectory() || !file.canWrite())
                 return null;
 
             File newFile = new File(String.format("%s/%s",
                     file.getAbsolutePath(),
                     uri.getQueryParameter(BaseFile._ParamName)));
 
             switch (ProviderUtils.getIntQueryParam(uri,
                     BaseFile._ParamFileType, BaseFile._FileTypeDirectory)) {
             case BaseFile._FileTypeDirectory:
                 newFile.mkdir();
                 break;// _FileTypeDirectory
 
             case BaseFile._FileTypeFile:
                 try {
                     newFile.createNewFile();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
                 break;// _FileTypeFile
 
             default:
                 return null;
             }
 
             if (newFile.exists()) {
                 Uri newUri = BaseFile
                         .genContentIdUriBase(LocalFileContract._Authority)
                         .buildUpon()
                         .appendPath(Uri.fromFile(newFile).toString()).build();
                 getContext().getContentResolver().notifyChange(uri, null);
                 return newUri;
             }
             return null;// _File
 
         default:
             throw new IllegalArgumentException("Unknown URI " + uri);
         }
     }// insert()
 
     @Override
     public Cursor query(Uri uri, String[] projection, String selection,
             String[] selectionArgs, String sortOrder) {
         if (BuildConfig.DEBUG)
             Log.d(_ClassName, String.format(
                     "query() >> uri = %s (%s) >> match = %s", uri,
                     uri.getLastPathSegment(), _UriMatcher.match(uri)));
 
         switch (_UriMatcher.match(uri)) {
         case _Api: {
             /*
              * If there is no command given, return provider ID and name.
              */
             MatrixCursor matrixCursor = new MatrixCursor(new String[] {
                     BaseFile._ColumnProviderId, BaseFile._ColumnProviderName,
                     BaseFile._ColumnProviderIconAttr });
             matrixCursor.newRow().add(LocalFileContract._ID)
                     .add(getContext().getString(R.string.afc_phone))
                     .add(R.attr.afc_badge_file_provider_localfile);
             return matrixCursor;
         }
         case _ApiCommand: {
             return doAnswerApiCommand(uri);
         }// _Api
 
         case _Directory: {
             return doListFiles(uri);
         }// _Directory
 
         case _File: {
             return doRetrieveFileInfo(uri);
         }// _File
 
         default:
             throw new IllegalArgumentException("Unknown URI " + uri);
         }
     }// query()
 
     @Override
     public int update(Uri uri, ContentValues values, String selection,
             String[] selectionArgs) {
         /*
          * Currently the library doesn't support this method.
          */
         throw new UnsupportedOperationException();
     }// update()
 
     /*
      * UTILITIES
      */
 
     /**
      * Answers the incoming URI.
      * 
      * @param uri
      *            the request URI.
      * @return the response.
      */
     private MatrixCursor doAnswerApiCommand(Uri uri) {
         MatrixCursor matrixCursor = null;
 
         if (BaseFile._CmdCancel.equals(uri.getLastPathSegment())) {
             int taskId = ProviderUtils.getIntQueryParam(uri,
                     BaseFile._ParamTaskId, 0);
             synchronized (_MapInterruption) {
                 if (taskId == 0) {
                     for (int i = 0; i < _MapInterruption.size(); i++)
                         _MapInterruption.put(_MapInterruption.keyAt(i), true);
                 } else if (_MapInterruption.indexOfKey(taskId) >= 0)
                     _MapInterruption.put(taskId, true);
             }
             return null;
         } else if (BaseFile._CmdGetDefaultPath.equals(uri.getLastPathSegment())) {
             matrixCursor = BaseFileProviderUtils.newBaseFileCursor();
 
             File file = Environment.getExternalStorageDirectory();
             if (file == null || !file.isDirectory())
                 file = new File("/");
             int type = file.isFile() ? BaseFile._FileTypeFile : (file
                     .isDirectory() ? BaseFile._FileTypeDirectory
                     : BaseFile._FileTypeUnknown);
             RowBuilder newRow = matrixCursor.newRow();
             newRow.add(0);// _ID
             newRow.add(BaseFile
                     .genContentIdUriBase(LocalFileContract._Authority)
                     .buildUpon().appendPath(Uri.fromFile(file).toString())
                     .build().toString());
             newRow.add(file.getAbsolutePath());
             newRow.add(file.getName());
             newRow.add(file.canRead() ? 1 : 0);
             newRow.add(file.canWrite() ? 1 : 0);
             newRow.add(file.length());
             newRow.add(type);
             newRow.add(file.lastModified());
         }// get default path
         else if (BaseFile._CmdIsAncestorOf.equals(uri.getLastPathSegment())) {
             return doCheckAncestor(uri);
         } else if (BaseFile._CmdGetParent.equals(uri.getLastPathSegment())) {
             File file = new File(Uri.parse(
                     uri.getQueryParameter(BaseFile._ParamSource)).getPath());
             file = file.getParentFile();
             if (file == null)
                 return null;
 
             matrixCursor = BaseFileProviderUtils.newBaseFileCursor();
 
             int type = file.isFile() ? BaseFile._FileTypeFile : (file
                     .isDirectory() ? BaseFile._FileTypeDirectory : (file
                     .exists() ? BaseFile._FileTypeUnknown
                     : BaseFile._FileTypeNotExisted));
 
             RowBuilder newRow = matrixCursor.newRow();
             newRow.add(0);// _ID
             newRow.add(BaseFile
                     .genContentIdUriBase(LocalFileContract._Authority)
                     .buildUpon().appendPath(Uri.fromFile(file).toString())
                     .build().toString());
             newRow.add(file.getAbsolutePath());
             newRow.add(file.getName());
             newRow.add(file.canRead() ? 1 : 0);
             newRow.add(file.canWrite() ? 1 : 0);
             newRow.add(file.length());
             newRow.add(type);
             newRow.add(file.lastModified());
         } else if (BaseFile._CmdShutdown.equals(uri.getLastPathSegment())) {
             /*
              * TODO Stop all tasks. If the activity call this command in
              * onDestroy(), it seems that this code block will be suspended and
              * started next time the activity starts. So we comment out this.
              * Let the Android system do what it wants to do!!!! I hate this.
              */
             // synchronized (_MapInterruption) {
             // for (int i = 0; i < _MapInterruption.size(); i++)
             // _MapInterruption.put(_MapInterruption.keyAt(i), true);
             // }
 
             if (mFileObserverEx != null) {
                 mFileObserverEx.stopWatching();
                 mFileObserverEx = null;
             }
         }
 
         return matrixCursor;
     }// doAnswerApiCommand()
 
     /**
      * Lists the content of a directory, if available.
      * 
      * @param uri
      *            the URI pointing to a directory.
      * @return the content of a directory, or {@code null} if not available.
      */
     private MatrixCursor doListFiles(Uri uri) {
         MatrixCursor matrixCursor = BaseFileProviderUtils.newBaseFileCursor();
 
         File file = extractFile(uri);
 
         if (BuildConfig.DEBUG)
             Log.d(_ClassName, "srcFile = " + file);
 
         if (!file.isDirectory() || !file.canRead())
             return null;
 
         /*
          * Prepare params...
          */
         int taskId = ProviderUtils.getIntQueryParam(uri, BaseFile._ParamTaskId,
                 0);
         boolean showHiddenFiles = ProviderUtils.getBooleanQueryParam(uri,
                 BaseFile._ParamShowHiddenFiles);
         boolean sortAscending = ProviderUtils.getBooleanQueryParam(uri,
                 BaseFile._ParamSortAscending, true);
         int sortBy = ProviderUtils.getIntQueryParam(uri, BaseFile._ParamSortBy,
                 BaseFile._SortByName);
         int filterMode = ProviderUtils.getIntQueryParam(uri,
                 BaseFile._ParamFilterMode, BaseFile._FilterFilesAndDirectories);
         int limit = ProviderUtils.getIntQueryParam(uri, BaseFile._ParamLimit,
                 1000);
         String positiveRegex = uri
                 .getQueryParameter(BaseFile._ParamPositiveRegexFilter);
         String negativeRegex = uri
                 .getQueryParameter(BaseFile._ParamNegativeRegexFilter);
 
         _MapInterruption.put(taskId, false);
 
         boolean[] hasMoreFiles = { false };
         List<File> files = new ArrayList<File>();
         listFiles(taskId, file, showHiddenFiles, filterMode, limit,
                 positiveRegex, negativeRegex, files, hasMoreFiles);
         if (!_MapInterruption.get(taskId)) {
             sortFiles(taskId, files, sortAscending, sortBy);
             if (!_MapInterruption.get(taskId)) {
                 for (int i = 0; i < files.size(); i++) {
                     if (_MapInterruption.get(taskId))
                         break;
 
                     File f = files.get(i);
                     int type = f.isFile() ? BaseFile._FileTypeFile : (f
                             .isDirectory() ? BaseFile._FileTypeDirectory
                             : BaseFile._FileTypeUnknown);
                     RowBuilder newRow = matrixCursor.newRow();
                     newRow.add(i);// _ID
                     newRow.add(BaseFile
                             .genContentIdUriBase(LocalFileContract._Authority)
                             .buildUpon().appendPath(Uri.fromFile(f).toString())
                             .build().toString());
                     newRow.add(f.getAbsolutePath());
                     newRow.add(f.getName());
                     newRow.add(f.canRead() ? 1 : 0);
                     newRow.add(f.canWrite() ? 1 : 0);
                     newRow.add(f.length());
                     newRow.add(type);
                     newRow.add(f.lastModified());
                 }// for files
 
                 /*
                  * The last row contains:
                  * 
                  * - The ID;
                  * 
                  * - The base file URI to original directory, which has
                  * parameter BaseFile._ParamHasMoreFiles to indicate the
                  * directory has more files or not.
                  * 
                  * - The system absolute path to original directory.
                  * 
                  * - The name of original directory.
                  */
                 RowBuilder newRow = matrixCursor.newRow();
                 newRow.add(files.size());// _ID
                 newRow.add(BaseFile
                         .genContentIdUriBase(LocalFileContract._Authority)
                         .buildUpon()
                         .appendPath(Uri.fromFile(file).toString())
                         .appendQueryParameter(BaseFile._ParamHasMoreFiles,
                                 Boolean.toString(hasMoreFiles[0])).build()
                         .toString());
                 newRow.add(file.getAbsolutePath());
                 newRow.add(file.getName());
             }
         }
 
         try {
             if (_MapInterruption.get(taskId)) {
                 if (BuildConfig.DEBUG)
                     Log.d(_ClassName, "query() >> cancelled...");
                 return null;
             }
         } finally {
             _MapInterruption.delete(taskId);
         }
 
         if (mFileObserverEx != null)
             mFileObserverEx.stopWatching();
         mFileObserverEx = new FileObserverEx(getContext(),
                 file.getAbsolutePath(), uri);
         mFileObserverEx.startWatching();
 
         /*
          * Tells the Cursor what URI to watch, so it knows when its source data
          * changes.
          */
         matrixCursor.setNotificationUri(getContext().getContentResolver(), uri);
         return matrixCursor;
     }// doListFiles()
 
     /**
      * Retrieves file information of a single file.
      * 
      * @param uri
      *            the URI pointing to a file.
      * @return the file information. Can be {@code null}, based on the input
      *         parameters.
      */
     private MatrixCursor doRetrieveFileInfo(Uri uri) {
         MatrixCursor matrixCursor = BaseFileProviderUtils.newBaseFileCursor();
 
         File file = extractFile(uri);
         int type = file.isFile() ? BaseFile._FileTypeFile
                 : (file.isDirectory() ? BaseFile._FileTypeDirectory : (file
                         .exists() ? BaseFile._FileTypeUnknown
                         : BaseFile._FileTypeNotExisted));
         RowBuilder newRow = matrixCursor.newRow();
         newRow.add(0);// _ID
         newRow.add(BaseFile.genContentIdUriBase(LocalFileContract._Authority)
                 .buildUpon().appendPath(Uri.fromFile(file).toString()).build()
                 .toString());
         newRow.add(file.getAbsolutePath());
         newRow.add(file.getName());
         newRow.add(file.canRead() ? 1 : 0);
         newRow.add(file.canWrite() ? 1 : 0);
         newRow.add(file.length());
         newRow.add(type);
         newRow.add(file.lastModified());
 
         return matrixCursor;
     }// doRetrieveFileInfo()
 
     /**
      * Lists all file inside {@code dir}.
      * 
      * @param taskId
      *            the task ID.
      * @param dir
      *            the source directory.
      * @param showHiddenFiles
      *            {@code true} or {@code false}.
      * @param filterMode
      *            can be one of {@link BaseFile#_FilterDirectoriesOnly},
      *            {@link BaseFile#_FilterFilesOnly},
      *            {@link BaseFile#_FilterFilesAndDirectories}.
      * @param limit
      *            the limit.
      * @param positiveRegex
      *            the positive regex filter.
      * @param negativeRegex
      *            the negative regex filter.
      * @param results
      *            the results.
      * @param hasMoreFiles
      *            the first item will contain a value representing that there is
      *            more files (exceeding {@code limit}) or not.
      */
     private void listFiles(final int taskId, final File dir,
             final boolean showHiddenFiles, final int filterMode,
             final int limit, String positiveRegex, String negativeRegex,
             final List<File> results, final boolean hasMoreFiles[]) {
         final Pattern positivePattern = group.pals.android.lib.ui.filechooser.utils.TextUtils
                 .compileRegex(positiveRegex);
         final Pattern negativePattern = group.pals.android.lib.ui.filechooser.utils.TextUtils
                 .compileRegex(negativeRegex);
 
         hasMoreFiles[0] = false;
         try {
             dir.listFiles(new FileFilter() {
 
                 @Override
                 public boolean accept(File pathname) {
                     if (_MapInterruption.get(taskId))
                         throw new CancellationException();
 
                     if (filterMode == BaseFile._FilterDirectoriesOnly
                             && pathname.isFile())
                         return false;
                     if (!showHiddenFiles && pathname.getName().startsWith("."))
                         return false;
                     if (positivePattern != null
                             && !positivePattern.matcher(pathname.getName())
                                     .find())
                         return false;
                     if (negativePattern != null
                             && negativePattern.matcher(pathname.getName())
                                     .find())
                         return false;
 
                     if (results.size() >= limit) {
                         hasMoreFiles[0] = true;
                         throw new CancellationException("Exceeding limit...");
                     }
                     results.add(pathname);
 
                     return false;
                 }// accept()
             });
         } catch (CancellationException e) {
             if (BuildConfig.DEBUG)
                 Log.d(_ClassName, "listFiles() >> cancelled... >> " + e);
         }
     }// listFiles()
 
     /**
      * Sorts {@code files}.
      * 
      * @param taskId
      *            the task ID.
      * @param files
      *            list of files.
      * @param ascending
      *            {@code true} or {@code false}.
      * @param sortBy
      *            can be one of {@link BaseFile.#_SortByModificationTime},
      *            {@link BaseFile.#_SortByName}, {@link BaseFile.#_SortBySize}.
      */
     private void sortFiles(final int taskId, final List<File> files,
             final boolean ascending, final int sortBy) {
         try {
             Collections.sort(files, new Comparator<File>() {
 
                 @Override
                 public int compare(File lhs, File rhs) {
                     if (_MapInterruption.get(taskId))
                         throw new CancellationException();
 
                     if ((lhs.isDirectory() && rhs.isDirectory())
                             || (lhs.isFile() && rhs.isFile())) {
                         // default is to compare by name (case insensitive)
                         int res = mCollator.compare(lhs.getName(),
                                 rhs.getName());
 
                         switch (sortBy) {
                         case BaseFile._SortByName:
                             break;// SortByName
 
                         case BaseFile._SortBySize:
                             if (lhs.length() > rhs.length())
                                 res = 1;
                             else if (lhs.length() < rhs.length())
                                 res = -1;
                             break;// SortBySize
 
                         case BaseFile._SortByModificationTime:
                             if (lhs.lastModified() > rhs.lastModified())
                                 res = 1;
                             else if (lhs.lastModified() < rhs.lastModified())
                                 res = -1;
                             break;// SortByDate
                         }
 
                         return ascending ? res : -res;
                     }
 
                     return lhs.isDirectory() ? -1 : 1;
                 }// compare()
             });
         } catch (CancellationException e) {
             if (BuildConfig.DEBUG)
                 Log.d(_ClassName, "sortFiles() >> cancelled...");
         }
     }// sortFiles()
 
     /**
      * Deletes {@code file}.
      * 
      * @param taskId
      *            the task ID.
      * @param file
      *            {@link File}.
      * @param recursive
      *            if {@code true} and {@code file} is a directory, this thread
      *            will delete all sub files/ folders of it recursively.
      * @return the total files deleted.
      */
     private int deleteFile(final int taskId, final File file,
             final boolean recursive) {
         final int[] count = { 0 };
         if (_MapInterruption.get(taskId))
             return count[0];
 
         if (file.isFile()) {
             if (file.delete())
                 count[0]++;
             return count[0];
         }
 
         /*
          * If the directory is empty, try to delete it and return here.
          */
         if (file.delete()) {
             count[0]++;
             return count[0];
         }
 
         if (!recursive)
             return count[0];
 
         try {
             try {
                 file.listFiles(new FileFilter() {
 
                     @Override
                     public boolean accept(File pathname) {
                         if (_MapInterruption.get(taskId))
                             throw new CancellationException();
 
                         if (pathname.isFile()) {
                             if (pathname.delete())
                                 count[0]++;
                         } else if (pathname.isDirectory()) {
                             if (recursive)
                                 count[0] += deleteFile(taskId, pathname,
                                         recursive);
                             else if (pathname.delete())
                                 count[0]++;
                         }
 
                         return false;
                     }// accept()
                 });
             } catch (CancellationException e) {
                 return count[0];
             }
 
             if (file.delete())
                 count[0]++;
         } catch (Throwable t) {
             // TODO
         }
 
         return count[0];
     }// deleteFile()
 
     /**
      * Checks ancestor with {@link BaseFile#_CmdIsAncestorOf},
      * {@link BaseFile#_ParamSource} and {@link BaseFile#_ParamTarget}.
      * 
      * @param uri
      *            the original URI from client.
      * @return {@code null} if source is not ancestor of target; or a
      *         <i>non-null but empty</i> cursor if the source is.
      */
     private MatrixCursor doCheckAncestor(Uri uri) {
         File source = new File(Uri.parse(
                 uri.getQueryParameter(BaseFile._ParamSource)).getPath());
         File target = new File(Uri.parse(
                 uri.getQueryParameter(BaseFile._ParamTarget)).getPath());
         if (source == null || target == null)
             return null;
 
         boolean validate = ProviderUtils.getBooleanQueryParam(uri,
                 BaseFile._ParamValidate, true);
         if (validate) {
             if (!source.isDirectory() || !target.exists())
                 return null;
         }
 
        if (target.getAbsolutePath().startsWith(source.getAbsolutePath()))
             return new MatrixCursor(new String[0]);
         return null;
     }// doCheckAncestor()
 
     /**
      * Extracts source file from request URI.
      * 
      * @param uri
      *            the original URI.
      * @return the file. Can be {@code null} if the authority is invalid.
      */
     private static File extractFile(Uri uri) {
         if (!LocalFileContract._Authority.equals(uri.getAuthority()))
             return null;
 
         String fileName = Uri.parse(uri.getLastPathSegment()).getPath();
         if (uri.getQueryParameter(BaseFile._ParamAppendPath) != null)
             fileName += Uri.parse(
                     uri.getQueryParameter(BaseFile._ParamAppendPath)).getPath();
         if (uri.getQueryParameter(BaseFile._ParamAppendName) != null)
             fileName += "/" + uri.getQueryParameter(BaseFile._ParamAppendName);
 
         if (BuildConfig.DEBUG)
             Log.d(_ClassName, "extractFile() >> " + fileName);
 
         return new File(fileName);
     }// extractFile()
 }
