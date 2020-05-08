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
 
 package com.google.code.geobeagle.gpx.gpx;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.easymock.PowerMock;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest( {
     GpxReader.class
 })
 public class GpxReaderTest {
 
     @Test
     public void testGetFilename() throws Exception {
         GpxReader gpxReader = new GpxReader("foo.gpx");
         assertEquals("foo.gpx", gpxReader.getFilename());
     }
 
     @Test
     public void testOpen() throws Exception {
         FileReader fileReader = PowerMock.createMock(FileReader.class);
         BufferedReader bufferedReader = PowerMock.createMock(BufferedReader.class);
 
        PowerMock.expectNew(FileReader.class, "/sdcard/foo.gpx").andReturn(fileReader);
         PowerMock.expectNew(BufferedReader.class, fileReader).andReturn(bufferedReader);
 
         PowerMock.replayAll();
         assertEquals(bufferedReader, new GpxReader("foo.gpx").open());
         PowerMock.verifyAll();
     }
 }
