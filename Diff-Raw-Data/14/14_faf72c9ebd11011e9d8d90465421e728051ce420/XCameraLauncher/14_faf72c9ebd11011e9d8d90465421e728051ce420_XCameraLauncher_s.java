 
 /*
  Copyright 2012-2013, Polyvi Inc. (http://polyvi.github.io/openxface)
  This program is distributed under the terms of the GNU General Public License.
 
  This file is part of xFace.
 
  xFace is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  xFace is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with xFace.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package org.apache.cordova.camera;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import org.apache.cordova.camera.CameraLauncher;
 
 import com.polyvi.xface.core.XConfiguration;
 import com.polyvi.xface.util.XConstant;
 import com.polyvi.xface.util.XLog;
 import com.polyvi.xface.util.XNotification;
 import com.polyvi.xface.util.XPathResolver;
 import com.polyvi.xface.util.XStringUtils;
 import com.polyvi.xface.util.XUtils;
 import com.polyvi.xface.view.XAppWebView;
 
 import android.app.Activity;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 
 public class XCameraLauncher extends CameraLauncher {
     private static final String CLASS_NAME = XCameraLauncher.class.getSimpleName();
     // 拍照类型
     private static final int PHOTOLIBRARY = 0;
     private static final int CAMERA = 1;
     private static final int SAVEDPHOTOALBUM = 2;
     private static final int JPEG = 0;
     private static final int PNG = 1;
 
     private static final String RESIZED_PIC_NAME = "Resize.jpg";
     private static final String MEDIA_MIME_TYPE = "image/jpeg";
     // 返回图片数据
     private static final int DATA_URL = 0;
     // 返回图片路径
     private static final int FILE_URI = 1;
     // 在android上和FILE_URI用法一样
     private static final int NATIVE_URI = 2;
 
     // alert提示框显示的时间
     private static final long DURATION = 4000;
 
     private static final int PICTURE_CUT_REQUEST_CODE = XUtils
             .genActivityRequestCode();
 
     // 如果需要对图片进行裁剪，此Uri用于指向裁剪后图片的路径，此路径在工作空间中，不在相册路径中
     private Uri mCroppedImageUri;
     // 图像资源类型(PHOTOLIBRARY, CAMERA, SAVEDPHOTOALBUM)
     private int mSrcType;
     // 目标图像的数据类型(DATA_URL, FILE_URI，NATIVE_URI)
     private int mDestType;
     private int mNumPics;
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent intent) {
         XResourceManager.init(cordova.getActivity());
         if (PICTURE_CUT_REQUEST_CODE == requestCode) {
             if (mSrcType == CAMERA) {
                 if (resultCode == Activity.RESULT_OK) {
                     cameraSucess(intent);
                     return;
                 }
             } else if ((mSrcType == PHOTOLIBRARY)
                     || (mSrcType == SAVEDPHOTOALBUM)) {
                 if (resultCode == Activity.RESULT_OK) {
                     photoSucess(intent);
                     return;
                 }
             }
 
             if (resultCode == Activity.RESULT_CANCELED) {
                 this.failPicture("Cut picture cancelled.");
             } else {
                 this.failPicture("Did not complete!");
             }
             return;
         }
 
         mSrcType = (requestCode / 16) - 1;
         mDestType = (requestCode % 16) - 1;
         if (mSrcType == CAMERA) {
             if (resultCode == Activity.RESULT_OK) {
                 if (allowEdit) {
                     startCutPhotoAfterCamera(intent);
                 } else {
                     super.onActivityResult(requestCode, resultCode, intent);
                 }
             } else {
                 super.onActivityResult(requestCode, resultCode, intent);
             }
         } else if ((mSrcType == PHOTOLIBRARY) || (mSrcType == SAVEDPHOTOALBUM)) {
             if (resultCode == Activity.RESULT_OK && null != intent) {
                 if (allowEdit) {
                     imageUri = intent.getData();
                     startCutPhoto(imageUri);
                 } else {
                     super.onActivityResult(requestCode, resultCode, intent);
                 }
             } else {
                 super.onActivityResult(requestCode, resultCode, intent);
             }
         } else {
             super.onActivityResult(requestCode, resultCode, intent);
         }
     }
 
     /**
      * 在拍照过后启动裁剪 注意由于裁剪的图片可能来自不同的位置
      *
      * @param intent
      */
     private void startCutPhotoAfterCamera(Intent intent) {
         // 当mDstType为FILE_URI时，intent为null，拍照的图片存放在mImageUri中
         if (null != intent && null != intent.getData()) {
             startCutPhoto(intent.getData());
         } else {
             startCutPhoto(imageUri);
         }
     }
 
     /**
      * 裁剪图片
      * 由于在图片裁剪时，裁剪长宽是由用户决定的，所以这里不必将用户指定要裁剪图片的长宽值传给裁剪程序
      *
      * @param uri
      */
     private boolean startCutPhoto(Uri uri) {
         if (!(targetWidth > 0 && targetWidth > 0)) {
             XLog.e(CLASS_NAME,
                     "Width and height must be larger than 1 when you want to crop image.");
             callbackContext
                     .error("you must give target width and height for crop photo.");
             return false;
         }
 
         // 为裁剪后的图片在程序工作空间中创建一个空文件，裁剪程序会将裁剪后的图片数据填入此文件中
         Uri cropped_image_uri = createEmptyFileForCroppedImageBeforeCrop();
         Intent intent = new Intent();
         intent.putExtra(ImageCroppingActivity.SOURCE_IMAGE_URI, uri);
         intent.putExtra(ImageCroppingActivity.CROPPED_IMAGE_URI,
                 cropped_image_uri);
         intent.setClass(cordova.getActivity(), ImageCroppingActivity.class);
         cordova.getActivity().startActivityForResult(intent,
                 PICTURE_CUT_REQUEST_CODE);
         return true;
     }
 
     /**
      * 选择图片或者直接提供图片路径，在程序工作空间创建一个空文件，裁剪程序将会把裁剪后的图片数据放到
      * 这个文件中，同时得到裁剪后图片的URI，因为此文件在裁剪前后的URI都不会改变。
      *
      * @return 裁剪后图片的URI
      */
     private Uri createEmptyFileForCroppedImageBeforeCrop() {
         String cropped_image_name = null;
         if (encodingType == JPEG) {
             String tempStr = System.currentTimeMillis() + ".jpg";
             cropped_image_name = tempStr + "_cropped.jpg";
         } else if (encodingType == PNG) {
             String tempStr = System.currentTimeMillis() + ".png";
             cropped_image_name = tempStr + "_cropped.png";
         } else {
             throw new IllegalArgumentException("Invalid Encoding Type: "
                     + encodingType);
         }
 
         File cropped_image = new File(getWorkSpace(), cropped_image_name);
         mCroppedImageUri = null;
         if (cropped_image.exists()) {
             cropped_image.delete();
         }
         try {
             cropped_image.createNewFile();
             mCroppedImageUri = Uri.fromFile(cropped_image);
         } catch (IOException e) {
             e.printStackTrace();
         }
         return mCroppedImageUri;
     }
 
     private void cameraSucess(Intent intent) {
         try {
             Bitmap bitmap = null;
             try {
                 // 获取裁剪过后image的bitmap
                 if (allowEdit) {
                     // 裁剪后，裁剪程序返回的是裁剪图片的数据，Android系统自带的裁剪程序就是返回图片数据
                     // 有时图片数据可能很大，所以下面返回图片的URI更好些
                     Bundle extras = intent.getExtras();
                     if (extras != null) {
                         bitmap = extras.getParcelable("data");
                     }
 
                     // 裁剪后，裁剪程序返回的是裁剪图片的URI
                     if ((bitmap == null) || (extras == null)) {
                         bitmap = getCroppedBitmap(intent);
                     }
                 }
             } catch (OutOfMemoryError e) {
                 XNotification notification = new XNotification(cordova);
                 notification.alert("Size of Image is too large!",
                         "Save Image Error", "OK", null, DURATION);
                 this.failPicture("Size of image is too large");
                 return;
             }
 
             // 对图片重新量化，以减少其体积
             Bitmap scaleBitmap = scaleBitmap(bitmap);
             Uri uri = null;
             if (mDestType == DATA_URL) {
                 processPicture(scaleBitmap);
                 checkForDuplicateImage(DATA_URL);
             } else if (mDestType == FILE_URI || mDestType == NATIVE_URI) {
                 if (!this.saveToPhotoAlbum) {
                     String suffixName = null;
                     if (encodingType == JPEG) {
                         suffixName = ".jpg";
                     } else if (encodingType == PNG) {
                         suffixName = ".png";
                     } else {
                         throw new IllegalArgumentException(
                                 "Invalid Encoding Type: " + encodingType);
                     }
                     String photoName = System.currentTimeMillis() + suffixName;
 
                     uri = Uri.fromFile(new File(getWorkSpace(), photoName));
                 } else {
                     uri = getUriFromMediaStore();
                 }
                 if (uri == null) {
                     this.failPicture("Error capturing image - no media storage found.");
                 }
 
                 // 压缩图像
                 OutputStream os = cordova.getActivity().getContentResolver()
                         .openOutputStream(uri);
                 scaleBitmap.compress(Bitmap.CompressFormat.JPEG, mQuality, os);
                 os.close();
 
                 // 将图像路径作为参数，调用success callback
                 XPathResolver pathResolver = new XPathResolver(uri.toString(),
                        "", cordova.getActivity());
                 this.callbackContext.success(XConstant.FILE_SCHEME
                        + pathResolver.resolve());
             }
             scaleBitmap.recycle();
             scaleBitmap = null;
             cleanup(FILE_URI, imageUri, uri, bitmap);
         } catch (IOException e) {
             this.failPicture("Error capturing image.");
         }
     }
 
     /**
      * 根据图片URI获取图片Bitmap对象
      *
      * @param intent
      *            包含有裁剪后图片URI的intent对象
      * @return Bitmap 上面intent对象中的URI所对应的图片的数据对象
      */
     private Bitmap getCroppedBitmap(Intent intent) {
         Bitmap bitmap = null;
         InputStream is = null;
         Uri uri = null; // 裁剪后图片的URI，此图片在应用程序工作空间中
 
         if (intent == null) {
             uri = mCroppedImageUri;
         } else {
             uri = intent
                     .getParcelableExtra(ImageCroppingActivity.CROPPED_IMAGE_URI);
         }
 
         try {
             is = getInputStream(uri);
             bitmap = BitmapFactory.decodeStream(is);
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             if (is != null) {
                 try {
                     is.close();
                 } catch (IOException ignored) {
                 }
             }
         }
         return bitmap;
     }
 
     private InputStream getInputStream(Uri mUri) throws IOException {
         try {
             if (mUri.getScheme().equals("file")) {
                 return new java.io.FileInputStream(mUri.getPath());
             } else {
                 return cordova.getActivity().getContentResolver()
                         .openInputStream(mUri);
             }
         } catch (FileNotFoundException ex) {
             return null;
         }
     }
 
     /**
      * 返回媒体入口
      *
      * @return uri
      */
     private Uri getUriFromMediaStore() {
         Uri uri = null;
         ContentValues values = new ContentValues();
         values.put(android.provider.MediaStore.Images.Media.MIME_TYPE,
                 MEDIA_MIME_TYPE);
         try {
             uri = cordova
                     .getActivity()
                     .getContentResolver()
                     .insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                             values);
         } catch (UnsupportedOperationException e) {
             XLog.d(CLASS_NAME, "Can't write to external media storage.");
             try {
                 uri = cordova
                         .getActivity()
                         .getContentResolver()
                         .insert(android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI,
                                 values);
             } catch (UnsupportedOperationException ex) {
                 XLog.d(CLASS_NAME, "Can't write to internal media storage.");
                 this.failPicture("Error capturing image - no media storage found.");
                 return null;
             }
         }
         return uri;
     }
 
     /**
      * 计算图像区域
      *
      * @param bitmap
      *            源图像.
      * @return Bitmap 目标图像.
      */
     private Bitmap scaleBitmap(Bitmap bitmap) {
         int newWidth = targetWidth;
         int newHeight = targetHeight;
         int origWidth = bitmap.getWidth();
         int origHeight = bitmap.getHeight();
 
         // 计算目标图像区域
         if (newWidth <= 0 && newHeight <= 0) {
             return bitmap;
         } else if (newWidth > 0 && newHeight <= 0) {
             newHeight = (newWidth * origHeight) / origWidth;
         } else if (newWidth <= 0 && newHeight > 0) {
             newWidth = (newHeight * origWidth) / origHeight;
         } else {
             double newRatio = newWidth / (double) newHeight;
             double origRatio = origWidth / (double) origHeight;
 
             if (origRatio > newRatio) {
                 newHeight = (newWidth * origHeight) / origWidth;
             } else if (origRatio < newRatio) {
                 newWidth = (newHeight * origWidth) / origHeight;
             }
         }
         return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
     }
 
     /**
      * 照相后完成旧图片的删除等回收工作.
      */
     private void cleanup(int imageType, Uri oldImage, Uri newImage,
             Bitmap bitmap) {
         if (bitmap != null) {
             bitmap.recycle();
             bitmap = null;
         }
 
         // Clean up initial camera-written image file.
         String filePath = oldImage.toString();
         if (filePath.startsWith("file://")) {
             filePath = filePath.substring(7);
         }
         (new File(filePath)).delete();
 
         checkForDuplicateImage(imageType);
         System.gc();
     }
 
     /**
      * 去掉重复的图像
      *
      * @param type
      *            FILE_URI，NATIVE_URI或者 DATA_URL
      */
     private void checkForDuplicateImage(int type) {
         int diff = 1;
         Cursor cursor = queryImgDB();
         int currentNumOfImages = 0;
         if (null != cursor) {
             currentNumOfImages = cursor.getCount();
         }
 
         if (type == FILE_URI || type == NATIVE_URI) {
             diff = 2;
         }
 
         // 删除重复的图片
         if ((currentNumOfImages - mNumPics) == diff) {
             cursor.moveToLast();
             int id = Integer.valueOf(cursor.getString(cursor
                     .getColumnIndex(MediaStore.Images.Media._ID))) - 1;
             Uri uri = Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                     + "/" + id);
             cordova.getActivity().getContentResolver().delete(uri, null, null);
         }
     }
 
     private Cursor queryImgDB() {
         Uri contentUri = null;
         if (Environment.getExternalStorageState().equals(
                 Environment.MEDIA_MOUNTED)) {
             contentUri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
         } else {
             contentUri = android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI;
         }
 
         return cordova
                 .getActivity()
                 .getContentResolver()
                 .query(contentUri,
                         new String[] { MediaStore.Images.Media._ID }, null,
                         null, null);
     }
 
     private void photoSucess(Intent intent) {
         // 此处的URI是不不进行裁剪，直接从相册中选择图片后的URI，亦即，此URI是指向相册中的某张图片
         // 由于裁剪程序本身也需要执行此函数，所以在try-catch中需要进行另外处理
         Uri uri = intent.getData();
         if (null == uri) {
             uri = imageUri;
         }
         ContentResolver resolver = cordova.getActivity().getContentResolver();
         XPathResolver pathResolver = new XPathResolver(null == uri ? null
                : uri.toString(), "", cordova.getActivity());
         Bitmap bitmap = null;
         try {
             if (!allowEdit) {
                String path = pathResolver.resolve();
                 if (!XStringUtils.isEmptyString(path)) {
                     bitmap = XUtils.decodeBitmap(path);
                 }
             } else {
                 // 裁剪后，裁剪程序返回的是裁剪图片的数据，Android系统自带的裁剪程序就是返回图片数据
                 bitmap = intent.getExtras().getParcelable("data");
 
                 // 裁剪后，裁剪程序返回的是裁剪图片的URI
                 if (bitmap == null) {
                     bitmap = getCroppedBitmap(intent);
                 }
             }
         } catch (OutOfMemoryError e) {
             this.failPicture("OutOfMemoryError when decode image.");
             return;
         }
         if (mDestType == DATA_URL) {
             int rotate = 0;
             String[] cols = { MediaStore.Images.Media.ORIENTATION };
             Cursor cursor = resolver.query(uri, cols, null, null, null);
             if (null != cursor) {
                 cursor.moveToPosition(0);
                 rotate = cursor.getInt(0);
                 cursor.close();
             }
             if (0 != rotate) {
                 Matrix matrix = new Matrix();
                 matrix.setRotate(rotate);
                 bitmap = bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                         bitmap.getHeight(), matrix, true);
             }
             bitmap = scaleBitmap(bitmap);
             processPicture(bitmap);
             bitmap.recycle();
             bitmap = null;
             System.gc();
         } else if (targetHeight > 0 && targetWidth > 0) {
             try {
                 Bitmap scaleBitmap = scaleBitmap(bitmap);
 
                 String fileName = XConfiguration.getInstance()
                         .getWorkDirectory() + RESIZED_PIC_NAME;
                 OutputStream os = new FileOutputStream(fileName);
                 scaleBitmap.compress(Bitmap.CompressFormat.JPEG, mQuality, os);
                 os.close();
 
                 bitmap.recycle();
                 bitmap = null;
                 scaleBitmap.recycle();
                 scaleBitmap = null;
 
                 this.callbackContext.success("file://" + fileName + "?"
                         + System.currentTimeMillis());
                 System.gc();
             } catch (Exception e) {
                 this.failPicture("Error retrieving image.");
                 return;
             }
         } else {
             this.callbackContext.success(XConstant.FILE_SCHEME
                    + pathResolver.resolve());
         }
     }
 
     /**
      * 获取workspace路径
      *
      * @return
      */
     private String getWorkSpace() {
         XAppWebView webView = (XAppWebView) this.webView;
         return webView.getOwnerApp().getWorkSpace();
     }
 }
