 package com.cocacola.climateambassador.ui.activities;
 
 import android.os.Bundle;
 import android.text.Html;
 import android.widget.TextView;
 
 import com.cocacola.climateambassador.HasModel;
 import com.cocacola.climateambassador.R;
 import com.cocacola.climateambassador.models.Case;
 import com.cocacola.climateambassador.models.Document;
 import com.cocacola.climateambassador.models.FileType;
 import com.cocacola.climateambassador.ui.views.BulletPointLayout;
 import com.cocacola.climateambassador.ui.views.DocumentView;
 import com.cocacola.climateambassador.util.JsonAssetsLoader;
 import com.cocacola.climateambassador.util.Toaster;
 import com.google.gson.JsonSyntaxException;
 
 import java.io.IOException;
 
 import javax.inject.Inject;
 
 import butterknife.InjectView;
 
 /**
  * Created by realandylawton on 9/7/13.
  */
 public class VisionActivity extends CaActivity implements HasModel<Case> {
 
     @Inject
     JsonAssetsLoader mJsonAssetsLoader;
 
     @InjectView(R.id.title)
     TextView mTitleView;
 
     @InjectView(R.id.body_text)
     TextView mBodyTextView;
 
     @InjectView(R.id.bullet_points)
     BulletPointLayout mBulletPointLayout;
 
     @InjectView(R.id.vision_callout)
     TextView mCalloutView;
 
     private Case mCase;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
 
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.activity_2020_vision);
 
         Case module = getModel();
        
         mTitleView.setText(module.getTitle());
         mBodyTextView.setText(Html.fromHtml(module.getBodyText()));
 
         mBulletPointLayout.setBulletPoints(module.getBulletPointFrame().getBulletPoints());
 
         mCalloutView.setText(module.getTextFrames().get(0).getBodyText());
 
     }
 
     @Override
     public Case getModel() {
 
         if(mCase == null) {
             try {
                 mCase = mJsonAssetsLoader.parseCaseFromJsonFile(getJsonAssetFilename());
             }
             catch (IOException e) {
                 onAssetLoadError();
             } catch (JsonSyntaxException e) {
                 onAssetLoadError();
             }
         }
 
         return mCase;
     }
 
     private void onAssetLoadError() {
         Toaster.toast(this, "Failed To Load: " + getJsonAssetFilename());
     }
 
     @Override
     public String getJsonAssetFilename() {
         return "for_suppliers_2020_vision.json";
     }
 
 
 }
