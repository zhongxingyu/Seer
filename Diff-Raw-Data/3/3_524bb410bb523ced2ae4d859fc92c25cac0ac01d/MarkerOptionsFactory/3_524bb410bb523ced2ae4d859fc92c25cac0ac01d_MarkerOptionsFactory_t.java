 package se.chalmers.krogkollen.map;
 
 import android.content.res.Resources;
 import android.graphics.*;
 import android.util.DisplayMetrics;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 import se.chalmers.krogkollen.R;
 import se.chalmers.krogkollen.pub.IPub;
 
 /*
  * This file is part of Krogkollen.
  *
  * Krogkollen is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Krogkollen is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Krogkollen.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /**
  * Builds settings for markers in Google Maps V2 for Android.
  *
  * @author Johan Backman
  */
 public class MarkerOptionsFactory {
 
     /**
      * Creates marker options with the specified text and background from Android resources.
      *
      * @param resources APP resources.
      * @param pub the pub that should be displayed on the marker.
      * @return a new google maps marker.
      */
     public static MarkerOptions createMarkerOptions(final DisplayMetrics displayMetrics, final Resources resources, final IPub pub) {
 
         // Make the bitmap mutable, since an object retrieved from resources is set to immutable by
         // default.
         Bitmap bitmap = getBackgroundPicture(pub.getQueueTime(), resources);
         Bitmap bitmapResult = bitmap.copy(Bitmap.Config.ARGB_8888, true);
         bitmap.recycle();
 
         // Create a canvas so the text can be drawn on the image.
         Canvas canvas = new Canvas(bitmapResult);
 
         // Add text to canvas.
         Paint paint = new Paint();
         paint.setColor(Color.rgb(44, 44, 44));
         paint.setTextSize(resources.getDimensionPixelSize(R.dimen.marker_font_size_main));
         paint.setTypeface(Typeface.SANS_SERIF);
         String mainText = pub.getName();
         if (mainText.length() > 10) {                           // if the text is too long cut it
            mainText = mainText.substring(0, 9);
            mainText += ".";
         }
         canvas.drawText(mainText, displayMetrics.xdpi * 0.06f,
                 displayMetrics.ydpi * 0.15f, paint);
         paint.setColor(Color.rgb(141, 141, 141));
         paint.setTextSize(resources.getDimensionPixelSize(R.dimen.marker_font_size_sub));
         canvas.drawText((pub.getTodaysOpeningHours().toString()),
                 displayMetrics.xdpi * 0.06f, displayMetrics.ydpi * 0.238f, paint);
 
         // Finalize the markerOptions.
         MarkerOptions options = new MarkerOptions()
                 .position(new LatLng(pub.getLatitude(), pub.getLongitude()))
                 .icon(BitmapDescriptorFactory.fromBitmap(bitmapResult))
                 .anchor(0.3f, 0.94f)
                 .title(pub.getID());
 
         return options;
     }
 
     // Find the right background to use.
     private static Bitmap getBackgroundPicture(int queueTime, final Resources resources) {
         switch (queueTime) {
             case 1:
                 return BitmapFactory.decodeResource(resources, R.drawable.green_marker_bg);
             case 2:
                 return BitmapFactory.decodeResource(resources, R.drawable.yellow_marker_bg);
             case 3:
                 return BitmapFactory.decodeResource(resources, R.drawable.red_marker_bg);
             default:
                 return BitmapFactory.decodeResource(resources, R.drawable.gray_marker_bg);
         }
     }
 }
