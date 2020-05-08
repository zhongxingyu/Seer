 package ru.tulupov.nsuconnect.helper;
 
 
 import android.content.Context;
 import android.content.Intent;
 
 import ru.tulupov.nsuconnect.activity.HomeActivity;
 
 public class IntentActionHelper {
 
     public static final Intent getHomeIntent(Context context) {
         return new Intent(context, HomeActivity.class);
     }

    public static Intent getSelectFromGallery1Intent(final Context context) {
        return new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    }

    public static Intent getCameraIntent(final Context context) {
        return new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
    }
 }
