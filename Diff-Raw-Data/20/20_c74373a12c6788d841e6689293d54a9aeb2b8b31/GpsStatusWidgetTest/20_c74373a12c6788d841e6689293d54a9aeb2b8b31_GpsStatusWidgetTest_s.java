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
 
 package com.google.code.geobeagle.gpsstatuswidget;
 
 import static org.easymock.EasyMock.expect;
 
 import com.google.code.geobeagle.Clock;
 import com.google.code.geobeagle.GeoFix;
 import com.google.code.geobeagle.GeoFixProvider;
 import com.google.code.geobeagle.GeoFixProviderLive;
 import com.google.code.geobeagle.R;
 import com.google.code.geobeagle.formatting.DistanceFormatter;
 import org.easymock.EasyMock;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.easymock.PowerMock;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import android.content.Context;
 import android.graphics.Color;
import android.location.Location;
 import android.location.LocationProvider;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.View;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest( {
         Bundle.class, Color.class, Handler.class, LinearLayout.class, TextView.class,
         GpsStatusWidget.class, Context.class
 })
 public class GpsStatusWidgetTest {
     @Test
     public void testFadeMeter() {
         View parent = PowerMock.createMock(View.class);
         Clock clock = PowerMock.createMock(Clock.class);
         MeterBars meterBars = PowerMock.createMock(MeterBars.class);
 
         expect(clock.getCurrentTime()).andReturn(1000L);
         meterBars.setLag(0);
         parent.postInvalidateDelayed(100);
 
         PowerMock.replayAll();
         new MeterFader(parent, meterBars, clock).paint();
         // Log.d("GeoBeagle", "painting " + lastUpdateLag);
         PowerMock.verifyAll();
     }
 
     @Test
     public void testFadeMeterLastDelay() {
         View parent = PowerMock.createMock(View.class);
         Clock clock = PowerMock.createMock(Clock.class);
         MeterBars meterBars = PowerMock.createMock(MeterBars.class);
 
         expect(clock.getCurrentTime()).andReturn(1000L);
         meterBars.setLag(0);
         parent.postInvalidateDelayed(100);
 
         expect(clock.getCurrentTime()).andReturn(2000L);
         meterBars.setLag(1000);
 
         PowerMock.replayAll();
         final MeterFader meterFader = new MeterFader(parent, meterBars, clock);
         meterFader.paint();
         meterFader.paint();
         // Log.d("GeoBeagle", "painting " + lastUpdateLag);
         PowerMock.verifyAll();
     }
 
     @Test
     public void testFadeMeterReset() {
         View parent = PowerMock.createMock(View.class);
         Clock clock = PowerMock.createMock(Clock.class);
         MeterBars meterBars = PowerMock.createMock(MeterBars.class);
 
         expect(clock.getCurrentTime()).andReturn(1000L);
         meterBars.setLag(0);
         parent.postInvalidateDelayed(100);
 
         expect(clock.getCurrentTime()).andReturn(1100L);
         meterBars.setLag(100);
         parent.postInvalidateDelayed(100);
 
         parent.postInvalidate();
 
         expect(clock.getCurrentTime()).andReturn(1200L);
         meterBars.setLag(0);
         parent.postInvalidateDelayed(100);
 
         PowerMock.replayAll();
         final MeterFader meterFader = new MeterFader(parent, meterBars, clock);
         meterFader.paint();
         meterFader.paint();
         meterFader.reset();
         meterFader.paint();
         // Log.d("GeoBeagle", "painting " + lastUpdateLag);
         PowerMock.verifyAll();
     }
 
     @Test
     public void testFadeMeterTwice() {
         View parent = PowerMock.createMock(View.class);
         Clock clock = PowerMock.createMock(Clock.class);
         MeterBars meterBars = PowerMock.createMock(MeterBars.class);
 
         expect(clock.getCurrentTime()).andReturn(1000L);
         meterBars.setLag(0);
         parent.postInvalidateDelayed(100);
 
         expect(clock.getCurrentTime()).andReturn(1100L);
         meterBars.setLag(100);
         parent.postInvalidateDelayed(100);
 
         PowerMock.replayAll();
         final MeterFader meterFader = new MeterFader(parent, meterBars, clock);
         meterFader.paint();
         meterFader.paint();
         // Log.d("GeoBeagle", "painting " + lastUpdateLag);
         PowerMock.verifyAll();
     }
 
     @Test
     public void testGpsStatusWidget_OnLocationChangedNullLocation() {
         PowerMock.suppressConstructor(LinearLayout.class);

        new GpsStatusWidgetDelegate(null, null, null, null, null, null, null, null).refresh();
     }
 
     @Test
     public void testGpsWidget_SetEnabledDisabled() {
         TextView status = PowerMock.createMock(TextView.class);
         PowerMock.suppressConstructor(LinearLayout.class);
 
         status.setText("gps ENABLED");
         status.setText("gps DISABLED");
 
         PowerMock.replayAll();
         GpsStatusWidgetDelegate gpsStatusWidget = new GpsStatusWidgetDelegate(null, null, null,
                 null, null, null, status, null);
         //TODO: GPS widget no longer gets provider status changes... it should get the info somehow
        gpsStatusWidget.onProviderEnabled("gps");
        gpsStatusWidget.onProviderDisabled("gps");
         PowerMock.verifyAll();
     }
 
     @Test
     public void testMeterWrapper_SetAccuracyAzimuth() {
         MeterBars meterBars = PowerMock.createMock(MeterBars.class);
         TextView accuracyView = PowerMock.createMock(TextView.class);
         DistanceFormatter distanceFormatter = PowerMock.createMock(DistanceFormatter.class);
 
         EasyMock.expect(distanceFormatter.formatDistance(1.2f)).andReturn("1m");
         accuracyView.setText("1m");
         meterBars.set(1.2f, 0);
 
         EasyMock.expect(distanceFormatter.formatDistance(1.2f)).andReturn("1m");
         meterBars.set(1.2f, 280);
 
         EasyMock.expect(distanceFormatter.formatDistance(2.2f)).andReturn("2m");
         accuracyView.setText("2m");
         meterBars.set(2.2f, 280);
         EasyMock.expect(distanceFormatter.formatDistance(2.2f)).andReturn("2m");
 
         PowerMock.replayAll();
         final Meter meter = new Meter(meterBars, accuracyView);
         meter.setAccuracy(1.2f, distanceFormatter);
         meter.setAzimuth(280);
         meter.setAccuracy(2.2f, distanceFormatter);
         PowerMock.verifyAll();
     }
 
     @Test
     public void testMeterWrapper_SetDisabled() {
         MeterBars meterBars = PowerMock.createMock(MeterBars.class);
         TextView accuracyView = PowerMock.createMock(TextView.class);
 
         accuracyView.setText("");
         meterBars.set(Float.MAX_VALUE, 0);
 
         PowerMock.replayAll();
         final Meter meter = new Meter(meterBars, accuracyView);
         meter.setDisabled();
         PowerMock.verifyAll();
     }
 
     @Test
     public void testOnLocationChanged() {
         MeterFader meterFader = PowerMock.createMock(MeterFader.class);
         Meter meter = PowerMock.createMock(Meter.class);
         TextLagUpdater textLagUpdater = PowerMock.createMock(TextLagUpdater.class);
         TextView provider = PowerMock.createMock(TextView.class);
         GeoFix location = PowerMock.createMock(GeoFix.class);
         GeoFixProvider geoFixProvider = PowerMock.createMock(GeoFixProviderLive.class);
         DistanceFormatter distanceFormatter = PowerMock.createMock(DistanceFormatter.class);
 
         expect(geoFixProvider.getLocation()).andReturn(location);
         expect(location.getProvider()).andReturn("gps");
         expect(location.getAccuracy()).andReturn(1.2f);
         expect(location.getTime()).andReturn(1000L);
         provider.setText("gps");
         meter.setAccuracy(1.2f, distanceFormatter);
         meterFader.reset();
         textLagUpdater.reset(1000);
 
         PowerMock.replayAll();
         final GpsStatusWidgetDelegate gpsStatusWidgetDelegate = new GpsStatusWidgetDelegate(
                 geoFixProvider, null, meter, meterFader, provider, null, null,
                 textLagUpdater);
         gpsStatusWidgetDelegate.setDistanceFormatter(distanceFormatter);
         gpsStatusWidgetDelegate.refresh();
         PowerMock.verifyAll();
     }
 
     @Test
     public void testOnLocationChangedProviderDisabled() {
         Meter meter = PowerMock.createMock(Meter.class);
         TextLagUpdater textLagUpdater = PowerMock.createMock(TextLagUpdater.class);
         GeoFix location = PowerMock.createMock(GeoFix.class);
         GeoFixProvider geoFixProvider = PowerMock.createMock(GeoFixProviderLive.class);
 
         expect(geoFixProvider.isProviderEnabled()).andReturn(false);
         expect(geoFixProvider.getLocation()).andReturn(location);
         textLagUpdater.setDisabled();
         meter.setDisabled();
 
         PowerMock.replayAll();
         new GpsStatusWidgetDelegate(geoFixProvider, null, meter, null, null, null, null,
                 textLagUpdater).refresh();
         PowerMock.verifyAll();
     }
 
     @Test
     public void testPaint() {
         MeterFader meterFader = PowerMock.createMock(MeterFader.class);
 
         meterFader.paint();
 
         PowerMock.replayAll();
         new GpsStatusWidgetDelegate(null, null, null, meterFader, null, null, null, null).paint();
         PowerMock.verifyAll();
     }
 
     @Test
     public void testSetStatus() {
         Context context = PowerMock.createMock(Context.class);
         TextView status = PowerMock.createMock(TextView.class);
         DistanceFormatter distanceFormatter = PowerMock.createMock(DistanceFormatter.class);
 
         PowerMock.suppressConstructor(LinearLayout.class);
 
         expect(context.getString(R.string.out_of_service)).andReturn("OUT OF SERVICE");
         expect(context.getString(R.string.available)).andReturn("AVAILABLE");
         expect(context.getString(R.string.temporarily_unavailable)).andReturn(
                 "TEMPORARILY UNAVAILABLE");
         status.setText("gps status: OUT OF SERVICE");
         status.setText("network status: AVAILABLE");
         status.setText("gps status: TEMPORARILY UNAVAILABLE");
 
         PowerMock.replayAll();
         GpsStatusWidgetDelegate gpsStatusWidget = new GpsStatusWidgetDelegate(null,
                 distanceFormatter, null, null, null, context, status, null);
         gpsStatusWidget.onStatusChanged("gps", LocationProvider.OUT_OF_SERVICE, null);
         gpsStatusWidget.onStatusChanged("network", LocationProvider.AVAILABLE, null);
         gpsStatusWidget.onStatusChanged("gps", LocationProvider.TEMPORARILY_UNAVAILABLE, null);
         PowerMock.verifyAll();
     }
 
     @Test
     public void testUpdateGpsWidgetRunnable() {
         TextLagUpdater textLagUpdater = PowerMock.createMock(TextLagUpdater.class);
         GeoFixProvider geoFixProvider = PowerMock
                 .createMock(GeoFixProviderLive.class);
         Meter meter = PowerMock.createMock(Meter.class);
         Handler handler = PowerMock.createMock(Handler.class);
 
         textLagUpdater.updateTextLag();
         expect(geoFixProvider.getAzimuth()).andReturn(42f);
         meter.setAzimuth(42f);
         EasyMock
                 .expect(
                         handler.postDelayed(EasyMock.isA(UpdateGpsWidgetRunnable.class), EasyMock
                                 .eq(500L))).andReturn(true);
 
         PowerMock.replayAll();
         new UpdateGpsWidgetRunnable(handler, geoFixProvider, meter, textLagUpdater).run();
         PowerMock.verifyAll();
     }
 }
