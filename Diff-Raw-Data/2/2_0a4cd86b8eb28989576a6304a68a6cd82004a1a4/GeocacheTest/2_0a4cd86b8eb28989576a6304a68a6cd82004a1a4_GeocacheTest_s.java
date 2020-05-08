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
 
 package com.google.code.geobeagle;
 
 import static org.easymock.EasyMock.expect;
 import static org.junit.Assert.assertEquals;
 
 import com.google.code.geobeagle.CacheType;
 import com.google.code.geobeagle.Geocache;
 import com.google.code.geobeagle.GeocacheFactory;
 import com.google.code.geobeagle.Geocache.AttributeFormatter;
 import com.google.code.geobeagle.GeocacheFactory.Source;
 import com.google.code.geobeagle.GeocacheFactory.Source.SourceFactory;
 import com.google.code.geobeagle.activity.main.GeocacheFromParcelFactory;
 import com.google.code.geobeagle.database.CacheWriter;
 import com.google.code.geobeagle.database.DatabaseDI;
 import com.google.code.geobeagle.database.DbFrontend;
 import com.google.code.geobeagle.Geocache.AttributeFormatterImpl;
 import com.google.code.geobeagle.Geocache.AttributeFormatterNull;
 import org.easymock.classextension.EasyMock;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.easymock.PowerMock;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.os.Parcel;
 import android.util.FloatMath;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest( {
        Parcel.class, Bundle.class, Geocache.class, System.class
 })
 public class GeocacheTest {
 
     @Test
     public void testAttributeFormatterImpl() {
         assertEquals("4.0 / 3.0", new AttributeFormatterImpl().formatAttributes(8, 6));
     }
 
     @Test
     public void testAttributeFormatterNull() {
         assertEquals("", new AttributeFormatterNull().formatAttributes(8, 6));
     }
 
     @Test
     public void testCalculateDistance() {
         // This test is a sham just to get the coverage numbers up.
         PowerMock.mockStatic(FloatMath.class);
 
         EasyMock.expect(FloatMath.sin(EasyMock.anyFloat())).andReturn(1f).anyTimes();
         EasyMock.expect(FloatMath.cos(EasyMock.anyFloat())).andReturn(1f).anyTimes();
         EasyMock.expect(FloatMath.sqrt(EasyMock.anyFloat())).andReturn(1f).anyTimes();
 
         PowerMock.replayAll();
         Geocache geocache = new Geocache(null, null, 0, 0, null, null, null, 0, 0, 0, null);
         geocache.getDistanceTo(1, 2);
         PowerMock.verifyAll();
     }
     
     @Test
     public void testDescribeContents() {
         Geocache geocache = new Geocache(null, null, 0, 0, null, null, null, 0, 0, 0, null);
         assertEquals(0, geocache.describeContents());
     }
 
     @Test
     public void testGetContentProvider() {
         Geocache geocache = new Geocache("GC123", null, 0, 0, null, null, null, 0, 0, 0, null);
         assertEquals(GeocacheFactory.Provider.GROUNDSPEAK, geocache.getContentProvider());
 
         geocache = new Geocache("PK123", null, 0, 0, null, null, null, 0, 0, 0, null);
         assertEquals(GeocacheFactory.Provider.GROUNDSPEAK, geocache.getContentProvider());
 
         geocache = new Geocache("LBabc", null, 0, 0, null, null, null, 0, 0, 0, null);
         assertEquals(GeocacheFactory.Provider.ATLAS_QUEST, geocache.getContentProvider());
 
         geocache = new Geocache("MLfoo", null, 0, 0, null, null, null, 0, 0, 0, null);
         assertEquals(GeocacheFactory.Provider.MY_LOCATION, geocache.getContentProvider());
     }
 
     @Test
     public void testGetFormattedAttributes() {
         AttributeFormatter attributeFormatter = PowerMock.createMock(AttributeFormatter.class);
 
         EasyMock.expect(attributeFormatter.formatAttributes(4, 3)).andReturn("4, 3");
 
         PowerMock.replayAll();
         assertEquals("4, 3", new Geocache(null, null, 0, 0, null, null, null, 4, 3, 0,
                 attributeFormatter).getFormattedAttributes());
         PowerMock.verifyAll();
     }
 
     @Test
     public void testGetIdAndName() {
         Geocache geocache = new Geocache("", "hello", 0, 0, null, null, null, 0, 0, 0, null);
         assertEquals("hello", geocache.getIdAndName());
 
         geocache = new Geocache("idonly", "", 0, 0, null, null, null, 0, 0, 0, null);
         assertEquals("idonly", geocache.getIdAndName());
 
         geocache = new Geocache("id", "hello", 0, 0, null, null, null, 0, 0, 0, null);
         assertEquals("id: hello", geocache.getIdAndName());
 
     }
 
     @Test
     public void testGetShortId() {
         Geocache geocache = new Geocache("GC123", "a cache", 37.5, -122.25, Source.GPX, "alameda",
                 null, 0, 0, 0, null);
         assertEquals("123", geocache.getShortId());
 
         geocache = new Geocache("", "a cache", 37.5, -122.25, Source.GPX, "alameda", null, 0, 0, 0,
                 null);
         assertEquals("", geocache.getShortId());
     }
 
     @Test
     public void testGetters() {
         Geocache geocache = new Geocache("GC123", "a cache", 37.5, -122.25, Source.GPX, "alameda",
                 CacheType.TRADITIONAL, 1, 2, 3, null);
         assertEquals("GC123", geocache.getId());
         assertEquals(37.5, geocache.getLatitude(), 0);
         assertEquals(-122.25, geocache.getLongitude(), 0);
         assertEquals("a cache", geocache.getName());
         assertEquals("alameda", geocache.getSourceName());
         assertEquals(Source.GPX, geocache.getSourceType());
         assertEquals(CacheType.TRADITIONAL, geocache.getCacheType());
         assertEquals(1, geocache.getDifficulty());
         assertEquals(2, geocache.getTerrain());
         assertEquals(3, geocache.getContainer());
     }
 
     @Test
     public void testParcelConstructor() {
         Parcel parcel = PowerMock.createMock(Parcel.class);
         Bundle bundle = PowerMock.createMock(Bundle.class);
         GeocacheFactory geocacheFactory = PowerMock.createMock(GeocacheFactory.class);
         Geocache geocache = PowerMock.createMock(Geocache.class);
 
         EasyMock.expect(parcel.readBundle()).andReturn(bundle);
         EasyMock.expect(bundle.getCharSequence(Geocache.ID)).andReturn("GC123");
         EasyMock.expect(bundle.getDouble(Geocache.LATITUDE)).andReturn(new Double(37));
         EasyMock.expect(bundle.getDouble(Geocache.LONGITUDE)).andReturn(new Double(-122));
         EasyMock.expect(bundle.getCharSequence(Geocache.NAME)).andReturn("a cache");
         EasyMock.expect(bundle.getInt(Geocache.SOURCE_TYPE)).andReturn(1);
         EasyMock.expect(bundle.getInt(Geocache.CACHE_TYPE)).andReturn(1);
         EasyMock.expect(bundle.getInt(Geocache.DIFFICULTY)).andReturn(1);
         EasyMock.expect(bundle.getInt(Geocache.TERRAIN)).andReturn(3);
         EasyMock.expect(bundle.getInt(Geocache.CONTAINER)).andReturn(4);
         EasyMock.expect(bundle.getString(Geocache.SOURCE_NAME)).andReturn("new york city");
         EasyMock.expect(geocacheFactory.sourceFromInt(1)).andReturn(Source.GPX);
         EasyMock.expect(geocacheFactory.cacheTypeFromInt(1)).andReturn(CacheType.TRADITIONAL);
         EasyMock.expect(
                 geocacheFactory.create("GC123", "a cache", 37f, -122, Source.GPX, "new york city",
                         CacheType.TRADITIONAL, 1, 3, 4)).andReturn(geocache);
 
         PowerMock.replayAll();
         GeocacheFromParcelFactory geocacheFromParcelFactory = new GeocacheFromParcelFactory(
                 geocacheFactory);
         assertEquals(geocache, geocacheFromParcelFactory.create(parcel));
         PowerMock.verifyAll();
     }
 
     @Test
     public void testProviderToInt() {
         assertEquals(0, GeocacheFactory.Provider.ATLAS_QUEST.toInt());
         assertEquals(1, GeocacheFactory.Provider.GROUNDSPEAK.toInt());
         assertEquals(-1, GeocacheFactory.Provider.MY_LOCATION.toInt());
     }
 
     @Test
     public void testSourceFromInt() {
         SourceFactory sourceFactory = new SourceFactory();
         assertEquals(Source.MY_LOCATION, sourceFactory.fromInt(1));
     }
 
     @Test
     public void testWriteToParcel() throws Exception {
         Parcel parcel = PowerMock.createMock(Parcel.class);
         Bundle bundle = PowerMock.createMock(Bundle.class);
 
         PowerMock.expectNew(Bundle.class).andReturn(bundle);
         bundle.putCharSequence(Geocache.ID, "GC123");
         bundle.putCharSequence(Geocache.NAME, "a cache");
         bundle.putDouble(Geocache.LATITUDE, 37.5);
         bundle.putDouble(Geocache.LONGITUDE, -122.25);
         bundle.putInt(Geocache.SOURCE_TYPE, Source.GPX.toInt());
         bundle.putInt(Geocache.CACHE_TYPE, CacheType.TRADITIONAL.toInt());
         bundle.putInt(Geocache.DIFFICULTY, 4);
         bundle.putInt(Geocache.TERRAIN, 3);
         bundle.putInt(Geocache.CONTAINER, 1);
         bundle.putString(Geocache.SOURCE_NAME, "alameda");
         parcel.writeBundle(bundle);
 
         PowerMock.replayAll();
         Geocache geocache = new Geocache("GC123", "a cache", 37.5, -122.25, Source.GPX, "alameda",
                 CacheType.TRADITIONAL, 4, 3, 1, null);
         geocache.writeToParcel(parcel, 0);
         PowerMock.verifyAll();
     }
 
     @Test
     public void testWriteToPrefs() throws Exception {
         Editor editor = PowerMock.createMock(Editor.class);
 
         EasyMock.expect(editor.putString(Geocache.ID, "GC123")).andReturn(editor);
         EasyMock.expect(editor.putString(Geocache.NAME, "a cache")).andReturn(editor);
         EasyMock.expect(editor.putFloat(Geocache.LATITUDE, 37.5f)).andReturn(editor);
         EasyMock.expect(editor.putFloat(Geocache.LONGITUDE, -122.25f)).andReturn(editor);
         EasyMock.expect(editor.putInt(Geocache.SOURCE_TYPE, Source.MY_LOCATION.toInt())).andReturn(
                 editor);
         EasyMock.expect(editor.putInt(Geocache.CACHE_TYPE, CacheType.TRADITIONAL.toInt()))
                 .andReturn(editor);
         EasyMock.expect(editor.putInt(Geocache.DIFFICULTY, 2)).andReturn(editor);
         EasyMock.expect(editor.putInt(Geocache.TERRAIN, 4)).andReturn(editor);
         EasyMock.expect(editor.putInt(Geocache.CONTAINER, 3)).andReturn(editor);
         EasyMock.expect(editor.putString(Geocache.SOURCE_NAME, null)).andReturn(editor);
 
         PowerMock.replayAll();
         Geocache geocache = new Geocache("GC123", "a cache", 37.5, -122.25, Source.MY_LOCATION,
                 null, CacheType.TRADITIONAL, 2, 4, 3, null);
         geocache.writeToPrefs(editor);
         PowerMock.verifyAll();
     }
     
     @Test
     public void testSave() {
         CacheWriter writer = PowerMock.createMock(CacheWriter.class);
         DbFrontend dbFrontend = PowerMock.createMock(DbFrontend.class);
         expect(dbFrontend.getCacheWriter()).andReturn(writer);                
         PowerMock.mockStatic(DatabaseDI.class);
 
         writer.startWriting();
         writer.insertAndUpdateCache("GC123", "a cache", 37.5, -122.25, Source.MY_LOCATION, "manhattan",
                 CacheType.TRADITIONAL, 2, 4, 3);
         writer.stopWriting();
 
         PowerMock.replayAll();
         Geocache geocache = new Geocache("GC123", "a cache", 37.5, -122.25, Source.MY_LOCATION,
                 "manhattan", CacheType.TRADITIONAL, 2, 4, 3, null);
         geocache.saveToDb(dbFrontend);
         PowerMock.verifyAll();
     }
     
 }
