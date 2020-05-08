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
 
 package com.google.code.geobeagle.activity.cachelist.presenter;
 
 import static org.junit.Assert.assertEquals;
 
 import com.google.code.geobeagle.Geocache;
 import com.google.code.geobeagle.R;
import com.google.code.geobeagle.Tags;
 import com.google.code.geobeagle.activity.cachelist.presenter.GeocacheSummaryRowInflater.RowViews;
import com.google.code.geobeagle.database.DbFrontend;
 import com.google.code.geobeagle.database.DistanceAndBearing;
 import com.google.code.geobeagle.formatting.DistanceFormatter;
 
 import org.easymock.classextension.EasyMock;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.easymock.PowerMock;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
 import android.graphics.drawable.Drawable;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 @PrepareForTest( {
         TextView.class, View.class, RowViews.class, GeocacheSummaryRowInflater.class, Log.class,
         Resources.class
 })
 @RunWith(PowerMockRunner.class)
 public class GeocacheSummaryRowInflaterTest {
 
     @Before
     public void allowLogging() {
         PowerMock.mockStatic(Log.class);
         EasyMock.expect(Log.d((String)EasyMock.anyObject(), (String)EasyMock.anyObject()))
                 .andReturn(0).anyTimes();
     }
 
     @Test
     public void testCacheRowViewsSet() {
         TextView txtCacheName = PowerMock.createMock(TextView.class);
         TextView txtDistance = PowerMock.createMock(TextView.class);
         TextView txtId = PowerMock.createMock(TextView.class);
         TextView txtAttributes = PowerMock.createMock(TextView.class);
         ImageView imageView = PowerMock.createMock(ImageView.class);
         //GeocacheVector geocacheVector = PowerMock.createMock(GeocacheVector.class);
         Geocache geocache = PowerMock.createMock(Geocache.class);
         DistanceFormatter distanceFormatter = PowerMock.createMock(DistanceFormatter.class);
         BearingFormatter relativeBearingFormatter = PowerMock
                 .createMock(RelativeBearingFormatter.class);
         DistanceAndBearing distanceAndBearing = PowerMock.createMock(DistanceAndBearing.class);
         Resources resources = PowerMock.createMock(Resources.class);
         Drawable drawable = PowerMock.createMock(Drawable.class);
        DbFrontend dbFrontend = PowerMock.createMock(DbFrontend.class);
 
         EasyMock.expect(distanceAndBearing.getGeocache()).andReturn(geocache);
        EasyMock.expect(geocache.getIcon(resources, null, dbFrontend)).andReturn(drawable);
         imageView.setImageDrawable(drawable);
        EasyMock.expect(geocache.getId()).andReturn("GC123").anyTimes();
         EasyMock.expect(geocache.getName()).andReturn("my cache");
         EasyMock.expect(geocache.getFormattedAttributes()).andReturn("3.5 / 2.5");
         EasyMock.expect(distanceAndBearing.getDistance()).andReturn(10f);
         EasyMock.expect(distanceAndBearing.getBearing()).andReturn(90f);
         EasyMock.expect(relativeBearingFormatter.formatBearing(90f, 0)).andReturn(">");
         
         EasyMock.expect(
                 distanceFormatter.formatDistance(10))
                 .andReturn("10m");
 
         txtId.setText("GC123");
        EasyMock.expect(dbFrontend.geocacheHasTag("GC123", Tags.ARCHIVED)).andReturn(false);
        EasyMock.expect(dbFrontend.geocacheHasTag("GC123", Tags.UNAVAILABLE)).andReturn(false);
        EasyMock.expect(txtCacheName.getPaintFlags()).andReturn(1);
        txtCacheName.setPaintFlags(1 & ~Paint.STRIKE_THRU_TEXT_FLAG);
        txtCacheName.setTextColor(Color.WHITE);
         txtCacheName.setText("my cache");
         txtDistance.setText("10m >");
         txtAttributes.setText("3.5 / 2.5");
 
         PowerMock.replayAll();
         new GeocacheSummaryRowInflater.RowViews(txtAttributes, txtCacheName, txtDistance,
                 imageView, txtId, resources).set(distanceAndBearing, 0, distanceFormatter, 
                        relativeBearingFormatter, null, dbFrontend);
         PowerMock.verifyAll();
     }
 
     @Test
     public void testInflateExisting() throws Exception {
         View convertView = PowerMock.createMock(View.class);
         DistanceFormatter distanceFormatter = PowerMock.createMock(DistanceFormatter.class);
 
         PowerMock.replayAll();
         assertEquals(convertView, new GeocacheSummaryRowInflater(distanceFormatter, null, null,
                 null, null, null).inflate(convertView));
         PowerMock.verifyAll();
     }
 
     @Test
     public void testInflateNew() throws Exception {
         View view = PowerMock.createMock(View.class);
         LayoutInflater layoutInflater = PowerMock.createMock(LayoutInflater.class);
         RowViews rowViews = PowerMock.createMock(RowViews.class);
         TextView txtCacheName = PowerMock.createMock(TextView.class);
         TextView txtAttributes = PowerMock.createMock(TextView.class);
         TextView txtCacheId = PowerMock.createMock(TextView.class);
         TextView txtDistance = PowerMock.createMock(TextView.class);
         DistanceFormatter distanceFormatter = PowerMock.createMock(DistanceFormatter.class);
         BearingFormatter relativeBearingFormatter = PowerMock
                 .createMock(RelativeBearingFormatter.class);
         PowerMock.mockStatic(Log.class);
         Resources resources = PowerMock.createMock(Resources.class);
         ImageView imageView = PowerMock.createMock(ImageView.class);
 
         EasyMock.expect(Log.d((String)EasyMock.anyObject(), (String)EasyMock.anyObject()))
                 .andReturn(0).anyTimes();
         EasyMock.expect(layoutInflater.inflate(R.layout.cache_row, null)).andReturn(view);
         EasyMock.expect(view.findViewById(R.id.txt_cache)).andReturn(txtCacheName);
         EasyMock.expect(view.findViewById(R.id.distance)).andReturn(txtDistance);
         EasyMock.expect(view.findViewById(R.id.gc_row_icon)).andReturn(imageView);
         EasyMock.expect(view.findViewById(R.id.txt_gcattributes)).andReturn(txtAttributes);
         EasyMock.expect(view.findViewById(R.id.txt_gcid)).andReturn(txtCacheId);
         PowerMock.expectNew(GeocacheSummaryRowInflater.RowViews.class,
                 txtAttributes, txtCacheName,
                 txtDistance, imageView, txtCacheId, resources).andReturn(rowViews);
         view.setTag(rowViews);
 
         PowerMock.replayAll();
         assertEquals(view, new GeocacheSummaryRowInflater(distanceFormatter,
                 layoutInflater, relativeBearingFormatter, resources, null, null)
                 .inflate(null));
         PowerMock.verifyAll();
     }
 
     @Test
     public void testSetBearingFormatterAbsolute() throws Exception {
         AbsoluteBearingFormatter absoluteBearingFormatter = PowerMock
                 .createMock(AbsoluteBearingFormatter.class);
         PowerMock.expectNew(AbsoluteBearingFormatter.class).andReturn(absoluteBearingFormatter);
 
         PowerMock.replayAll();
         final GeocacheSummaryRowInflater geocacheSummaryRowInflater = new GeocacheSummaryRowInflater(
                 null, null, null, null, null, null);
         geocacheSummaryRowInflater.setBearingFormatter(true);
         PowerMock.verifyAll();
     }
 
     @Test
     public void testSetBearingFormatterRelative() throws Exception {
         RelativeBearingFormatter relativeBearingFormatter = PowerMock
                 .createMock(RelativeBearingFormatter.class);
         PowerMock.expectNew(RelativeBearingFormatter.class).andReturn(relativeBearingFormatter);
 
         PowerMock.replayAll();
         final GeocacheSummaryRowInflater geocacheSummaryRowInflater = new GeocacheSummaryRowInflater(
                 null, null, null, null, null, null);
         geocacheSummaryRowInflater.setBearingFormatter(false);
         assertEquals(relativeBearingFormatter, geocacheSummaryRowInflater.getBearingFormatter());
         PowerMock.verifyAll();
     }
 
     @Test
     public void testSetData() {
         View view = PowerMock.createMock(View.class);
         RowViews rowViews = PowerMock.createMock(RowViews.class);
         DistanceFormatter distanceFormatter = PowerMock.createMock(DistanceFormatter.class);
         DistanceFormatter distanceFormatter2 = PowerMock.createMock(DistanceFormatter.class);
         BearingFormatter relativeBearingFormatter = PowerMock
                 .createMock(RelativeBearingFormatter.class);
         DistanceAndBearing distanceAndBearing = PowerMock.createMock(DistanceAndBearing.class);
 
         //EasyMock.expect(geocacheVectors.get(18)).andReturn(geocacheVector);
         EasyMock.expect(view.getTag()).andReturn(rowViews);
         rowViews.set(distanceAndBearing, 18, distanceFormatter2, relativeBearingFormatter, null, null);
 
         PowerMock.replayAll();
         final GeocacheSummaryRowInflater geocacheSummaryRowInflater = new GeocacheSummaryRowInflater(
                 distanceFormatter, null, relativeBearingFormatter, null, null, null);
         geocacheSummaryRowInflater.setDistanceFormatter(distanceFormatter2);
         geocacheSummaryRowInflater.setData(view, distanceAndBearing, 18);
         PowerMock.verifyAll();
     }
 }
