 /*
  ** Licensed under the Apache License, Version 2.0 (the "License");
  ** you may not use this file except in compliance with the License.
  ** You may obtain a copy of the License at
  **
  **     http://www.apache.org/licenses/LICENSE-2.0
  **
  ** Unless required by applicable law or agreed to in writing, software
  ** distributed under the License is distributed on an "AS IS" BASIS,
  ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ** See the License for the specific language governing permissions and
  ** limitations under the License.
  */
 
 package com.google.code.geobeagle.activity.main.view;
 
 import static java.lang.annotation.ElementType.FIELD;
 import static java.lang.annotation.ElementType.METHOD;
 import static java.lang.annotation.ElementType.PARAMETER;
 import static java.lang.annotation.RetentionPolicy.RUNTIME;
 
 import com.google.code.geobeagle.Geocache;
 import com.google.code.geobeagle.R;
import com.google.code.geobeagle.GeoBeaglePackageModule.DifficultyAndTerrainPainterAnnotation;
 import com.google.code.geobeagle.GraphicsGenerator.IconOverlay;
 import com.google.code.geobeagle.GraphicsGenerator.IconOverlayFactory;
 import com.google.code.geobeagle.GraphicsGenerator.IconRenderer;
 import com.google.code.geobeagle.GraphicsGenerator.MapViewBitmapCopier;
 import com.google.code.geobeagle.activity.main.GeoUtils;
 import com.google.code.geobeagle.activity.main.RadarView;
 import com.google.inject.BindingAnnotation;
 import com.google.inject.Inject;
 import com.google.inject.name.Named;
 
 import android.graphics.drawable.Drawable;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import java.lang.annotation.Retention;
 import java.lang.annotation.Target;
 import java.util.List;
 
 public class GeocacheViewer {
     public interface AttributeViewer {
         void setImage(int attributeValue);
     }
 
     public static class LabelledAttributeViewer implements AttributeViewer {
         private final AttributeViewer mUnlabelledAttributeViewer;
         private final TextView mLabel;
 
         public LabelledAttributeViewer(TextView label, AttributeViewer unlabelledAttributeViewer) {
             mUnlabelledAttributeViewer = unlabelledAttributeViewer;
             mLabel = label;
         }
 
         @Override
         public void setImage(int attributeValue) {
             mUnlabelledAttributeViewer.setImage(attributeValue);
             mLabel.setVisibility(attributeValue == 0 ? View.GONE : View.VISIBLE);
         }
     }
 
     @BindingAnnotation @Target({ FIELD, PARAMETER, METHOD }) @Retention(RUNTIME)
     public @interface RibbonImages {}
 
     @BindingAnnotation @Target({ FIELD, PARAMETER, METHOD }) @Retention(RUNTIME)
     public @interface PawImages {}
 
     public static class UnlabelledAttributeViewer implements AttributeViewer {
         private final Drawable[] mDrawables;
         private final ImageView mImageView;
 
         public UnlabelledAttributeViewer(ImageView imageView, Drawable[] drawables) {
             mImageView = imageView;
             mDrawables = drawables;
         }
 
         @Override
         public void setImage(int attributeValue) {
             if (attributeValue == 0) {
                 mImageView.setVisibility(View.GONE);
                 return;
             }
             mImageView.setImageDrawable(mDrawables[attributeValue-1]);
             mImageView.setVisibility(View.VISIBLE);
         }
     }
 
     public static class ResourceImages implements AttributeViewer {
         private final List<Integer> mResources;
         private final ImageView mImageView;
 
         public ResourceImages(ImageView imageView, List<Integer> resources) {
             mImageView = imageView;
             mResources = resources;
         }
 
         @Override
         public void setImage(int attributeValue) {
             mImageView.setImageResource(mResources.get(attributeValue));
         }
     }
  
     public static class NameViewer {
         private final TextView mName;
 
         @Inject
         public NameViewer(@Named("GeocacheName") TextView name) {
             mName = name;
         }
 
         void set(CharSequence name) {
             if (name.length() == 0) {
                 mName.setVisibility(View.GONE);
                 return;
             }
             mName.setText(name);
             mName.setVisibility(View.VISIBLE);
         }
     }
 
     public static final Integer CONTAINER_IMAGES[] = {
             R.drawable.size_1, R.drawable.size_2, R.drawable.size_3, R.drawable.size_4
     };
 
     private final ImageView mCacheTypeImageView;
     private final AttributeViewer mContainer;
     private final AttributeViewer mDifficulty;
     private final TextView mId;
     private final NameViewer mName;
     private final RadarView mRadarView;
     private final AttributeViewer mTerrain;
     private final IconOverlayFactory mIconOverlayFactory;
     private final MapViewBitmapCopier mMapViewBitmapCopier;
     private final IconRenderer mIconRenderer;
 
     @Inject
     public GeocacheViewer(RadarView radarView, @Named("GeocacheId") TextView gcId,
             NameViewer gcName, @Named("GeocacheIcon") ImageView cacheTypeImageView,
             @Named("GeocacheDifficulty") AttributeViewer gcDifficulty,
             @Named("GeocacheTerrain") AttributeViewer gcTerrain, ResourceImages gcContainer,
             IconOverlayFactory iconOverlayFactory, MapViewBitmapCopier mapViewBitmapCopier,
             @DifficultyAndTerrainPainterAnnotation IconRenderer iconRenderer) {
         mRadarView = radarView;
         mId = gcId;
         mName = gcName;
         mCacheTypeImageView = cacheTypeImageView;
         mDifficulty = gcDifficulty;
         mTerrain = gcTerrain;
         mContainer = gcContainer;
         mIconOverlayFactory = iconOverlayFactory;
         mMapViewBitmapCopier = mapViewBitmapCopier;
         mIconRenderer = iconRenderer;
     }
 
     public void set(Geocache geocache) {
         final double latitude = geocache.getLatitude();
         final double longitude = geocache.getLongitude();
         mRadarView.setTarget((int)(latitude * GeoUtils.MILLION),
                 (int)(longitude * GeoUtils.MILLION));
         mId.setText(geocache.getId());
 
         IconOverlay iconOverlay = mIconOverlayFactory.create(geocache, true);
         int iconBig = geocache.getCacheType().iconBig();
         Drawable icon = mIconRenderer.renderIcon(0, 0, iconBig, iconOverlay, mMapViewBitmapCopier);
         mCacheTypeImageView.setImageDrawable(icon);
         mContainer.setImage(geocache.getContainer());
         mDifficulty.setImage(geocache.getDifficulty());
         mTerrain.setImage(geocache.getTerrain());
 
         mName.set(geocache.getName());
     }
 }
