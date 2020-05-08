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
 
 package com.google.code.geobeagle.xmlimport;
 
 import static org.easymock.EasyMock.expect;
 import static org.junit.Assert.assertTrue;
 
 import com.google.code.geobeagle.ErrorDisplayer;
import com.google.code.geobeagle.GeocacheFactory;
 import com.google.code.geobeagle.R;
 import com.google.code.geobeagle.xmlimport.EventHelperDI.EventHelperFactory;
 import com.google.code.geobeagle.xmlimport.GpxImporterDI.MessageHandler;
 import com.google.code.geobeagle.xmlimport.ImportThreadDelegate.ImportThreadHelper;
 import com.google.code.geobeagle.xmlimport.gpx.GpxAndZipFiles;
 import com.google.code.geobeagle.xmlimport.gpx.IGpxReader;
 import com.google.code.geobeagle.xmlimport.gpx.GpxAndZipFiles.GpxAndZipFilenameFilter;
 import com.google.code.geobeagle.xmlimport.gpx.GpxAndZipFiles.GpxFilesAndZipFilesIter;
 
 import org.easymock.EasyMock;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.easymock.PowerMock;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 import org.xmlpull.v1.XmlPullParserException;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.Reader;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest( {
         GpxAndZipFilenameFilter.class, File.class, GpxAndZipFiles.class, IGpxReader.class
 })
 public class ImportThreadDelegateTest {
 
     @Test
     public void testHelperCleanup() {
         MessageHandler messageHandler = PowerMock.createMock(MessageHandler.class);
 
         messageHandler.loadComplete();
 
         PowerMock.replayAll();
        new ImportThreadHelper(null, messageHandler, null, null, null, null).cleanup();
         PowerMock.verifyAll();
     }
 
     @Test
     public void testHelperEndNoFiles() {
         GpxLoader gpxLoader = PowerMock.createMock(GpxLoader.class);
         ErrorDisplayer errorDisplayer = PowerMock.createMock(ErrorDisplayer.class);
        GeocacheFactory geocacheFactory = PowerMock.createMock(GeocacheFactory.class);
 
         errorDisplayer.displayError(R.string.error_no_gpx_files);
         gpxLoader.end();
        geocacheFactory.flushCacheIcons();
 
         PowerMock.replayAll();
        new ImportThreadHelper(gpxLoader, null, null, null, errorDisplayer, geocacheFactory).end();
         PowerMock.verifyAll();
     }
 
     @Test
     public void testHelperProcessFileAndEnd() throws XmlPullParserException, IOException {
         GpxLoader gpxLoader = PowerMock.createMock(GpxLoader.class);
         IGpxReader gpxFile = PowerMock.createMock(IGpxReader.class);
         Reader reader = PowerMock.createMock(Reader.class);
         EventHelperFactory eventHelperFactory = PowerMock.createMock(EventHelperFactory.class);
         EventHelper eventHelper = PowerMock.createMock(EventHelper.class);
         EventHandler eventHandler = PowerMock.createMock(EventHandler.class);
         EventHandlers eventHandlers = PowerMock.createMock(EventHandlers.class);
        GeocacheFactory geocacheFactory = PowerMock.createMock(GeocacheFactory.class);
 
         EasyMock.expect(gpxFile.getFilename()).andReturn("foo.gpx");
         EasyMock.expect(eventHandlers.get("foo.gpx")).andReturn(eventHandler);
         EasyMock.expect(eventHelperFactory.create(eventHandler)).andReturn(eventHelper);
         EasyMock.expect(gpxFile.open()).andReturn(reader);
         gpxLoader.open("foo.gpx", reader);
         EasyMock.expect(gpxLoader.load(eventHelper)).andReturn(true);
         gpxLoader.end();
        geocacheFactory.flushCacheIcons();
        
         PowerMock.replayAll();
        ImportThreadHelper importThreadHelper = new ImportThreadHelper(
                gpxLoader, null, eventHelperFactory, eventHandlers, null,
                geocacheFactory);
         assertTrue(importThreadHelper.processFile(gpxFile));
         importThreadHelper.end();
         PowerMock.verifyAll();
     }
 
     @Test
     public void testHelperStart() {
         GpxLoader gpxLoader = PowerMock.createMock(GpxLoader.class);
 
         gpxLoader.start();
 
         PowerMock.replayAll();
        new ImportThreadHelper(gpxLoader, null, null, null, null, null).start();
         PowerMock.verifyAll();
     }
 
     @Test
     public void testRun() throws FileNotFoundException, XmlPullParserException, IOException {
         GpxAndZipFiles gpxAndZipFiles = PowerMock.createMock(GpxAndZipFiles.class);
         GpxFilesAndZipFilesIter gpxFilesAndZipFilesIter = PowerMock
                 .createMock(GpxFilesAndZipFilesIter.class);
         ImportThreadHelper importThreadHelper = PowerMock.createMock(ImportThreadHelper.class);
         IGpxReader iGpxFile = PowerMock.createMock(IGpxReader.class);
 
         expect(gpxAndZipFiles.iterator()).andReturn(gpxFilesAndZipFilesIter);
         importThreadHelper.start();
         expect(gpxFilesAndZipFilesIter.hasNext()).andReturn(true);
         expect(gpxFilesAndZipFilesIter.next()).andReturn(iGpxFile);
         expect(importThreadHelper.processFile(iGpxFile)).andReturn(true);
         expect(gpxFilesAndZipFilesIter.hasNext()).andReturn(false);
         importThreadHelper.end();
         importThreadHelper.cleanup();
 
         PowerMock.replayAll();
         new ImportThreadDelegate(gpxAndZipFiles, importThreadHelper, null).run();
         PowerMock.verifyAll();
     }
 
     @Test
     public void testRunAborted() throws FileNotFoundException, XmlPullParserException, IOException {
         GpxAndZipFiles gpxAndZipFiles = PowerMock.createMock(GpxAndZipFiles.class);
         GpxFilesAndZipFilesIter gpxFilesAndZipFilesIter = PowerMock
                 .createMock(GpxFilesAndZipFilesIter.class);
         ImportThreadHelper importThreadHelper = PowerMock.createMock(ImportThreadHelper.class);
         IGpxReader iGpxFile = PowerMock.createMock(IGpxReader.class);
 
         expect(gpxAndZipFiles.iterator()).andReturn(gpxFilesAndZipFilesIter);
         importThreadHelper.start();
         expect(gpxFilesAndZipFilesIter.hasNext()).andReturn(true);
         expect(gpxFilesAndZipFilesIter.next()).andReturn(iGpxFile);
         expect(importThreadHelper.processFile(iGpxFile)).andReturn(false);
         importThreadHelper.cleanup();
 
         PowerMock.replayAll();
         new ImportThreadDelegate(gpxAndZipFiles, importThreadHelper, null).run();
         PowerMock.verifyAll();
     }
 
     @Test
     public void testRunFileNotFound() {
         GpxAndZipFiles gpxAndZipFiles = PowerMock.createMock(GpxAndZipFiles.class);
         ImportThreadHelper importThreadHelper = PowerMock.createMock(ImportThreadHelper.class);
         ErrorDisplayer errorDisplayer = PowerMock.createMock(ErrorDisplayer.class);
         final FileNotFoundException e = new FileNotFoundException("foo.gpx");
 
         errorDisplayer.displayError(R.string.error_opening_file, "foo.gpx");
         importThreadHelper.cleanup();
 
         PowerMock.replayAll();
         ImportThreadDelegate importThreadDelegate = new ImportThreadDelegate(gpxAndZipFiles,
                 importThreadHelper, errorDisplayer) {
             @Override
             protected void tryRun() throws IOException {
                 throw e;
             }
         };
         importThreadDelegate.run();
         PowerMock.verifyAll();
 
     }
 
     @Test
     public void testRunXmlPullParserException() {
         GpxAndZipFiles gpxAndZipFiles = PowerMock.createMock(GpxAndZipFiles.class);
         ImportThreadHelper importThreadHelper = PowerMock.createMock(ImportThreadHelper.class);
         ErrorDisplayer errorDisplayer = PowerMock.createMock(ErrorDisplayer.class);
         final XmlPullParserException e = PowerMock.createMock(XmlPullParserException.class);
 
         EasyMock.expect(e.getMessage()).andReturn("xml exception");
         errorDisplayer.displayError(R.string.error_parsing_file, "xml exception");
         importThreadHelper.cleanup();
 
         PowerMock.replayAll();
         ImportThreadDelegate importThreadDelegate = new ImportThreadDelegate(gpxAndZipFiles,
                 importThreadHelper, errorDisplayer) {
             @Override
             protected void tryRun() throws XmlPullParserException {
                 throw e;
             }
         };
         importThreadDelegate.run();
         PowerMock.verifyAll();
 
     }
 
     @Test
     public void testRunIOException() {
         GpxAndZipFiles gpxAndZipFiles = PowerMock.createMock(GpxAndZipFiles.class);
         ImportThreadHelper importThreadHelper = PowerMock.createMock(ImportThreadHelper.class);
         ErrorDisplayer errorDisplayer = PowerMock.createMock(ErrorDisplayer.class);
         final IOException e = PowerMock.createMock(IOException.class);
         EasyMock.expect(e.getMessage()).andReturn("problem reading file");
         importThreadHelper.cleanup();
         errorDisplayer.displayError(R.string.error_reading_file, "problem reading file");
 
         PowerMock.replayAll();
         ImportThreadDelegate importThreadDelegate = new ImportThreadDelegate(gpxAndZipFiles,
                 importThreadHelper, errorDisplayer) {
             @Override
             protected void tryRun() throws IOException {
                 throw e;
             }
         };
         importThreadDelegate.run();
         PowerMock.verifyAll();
     }
 
     @Test
     public void testRunIteratorFail() {
         GpxAndZipFiles gpxAndZipFiles = PowerMock.createMock(GpxAndZipFiles.class);
         ImportThreadHelper importThreadHelper = PowerMock.createMock(ImportThreadHelper.class);
         ErrorDisplayer errorDisplayer = PowerMock.createMock(ErrorDisplayer.class);
 
         expect(gpxAndZipFiles.iterator()).andReturn(null);
         errorDisplayer.displayError(R.string.error_cant_read_sd);
         importThreadHelper.cleanup();
 
         PowerMock.replayAll();
         ImportThreadDelegate importThreadDelegate = new ImportThreadDelegate(gpxAndZipFiles,
                 importThreadHelper, errorDisplayer);
         importThreadDelegate.run();
         PowerMock.verifyAll();
     }
 
     @Test
     public void testRunNoFiles() throws IOException {
         GpxAndZipFiles gpxAndZipFiles = PowerMock.createMock(GpxAndZipFiles.class);
         GpxFilesAndZipFilesIter gpxFilesAndZipFilesIter = PowerMock
                 .createMock(GpxFilesAndZipFilesIter.class);
         ImportThreadHelper importThreadHelper = PowerMock.createMock(ImportThreadHelper.class);
 
         expect(gpxAndZipFiles.iterator()).andReturn(gpxFilesAndZipFilesIter);
         expect(gpxFilesAndZipFilesIter.hasNext()).andReturn(false);
         importThreadHelper.start();
         importThreadHelper.end();
         importThreadHelper.cleanup();
 
         PowerMock.replayAll();
         new ImportThreadDelegate(gpxAndZipFiles, importThreadHelper, null).run();
         PowerMock.verifyAll();
     }
 
     @Test
     public void testRunNoFilesToImport() throws FileNotFoundException, IOException {
         GpxAndZipFiles gpxAndZipFiles = PowerMock.createMock(GpxAndZipFiles.class);
         GpxFilesAndZipFilesIter gpxFilesAndZipFilesIter = PowerMock
                 .createMock(GpxFilesAndZipFilesIter.class);
         ImportThreadHelper importThreadHelper = PowerMock.createMock(ImportThreadHelper.class);
 
         expect(gpxAndZipFiles.iterator()).andReturn(gpxFilesAndZipFilesIter);
         importThreadHelper.start();
         expect(gpxFilesAndZipFilesIter.hasNext()).andReturn(false);
         importThreadHelper.end();
         importThreadHelper.cleanup();
 
         PowerMock.replayAll();
         new ImportThreadDelegate(gpxAndZipFiles, importThreadHelper, null).run();
         PowerMock.verifyAll();
     }
 
     @Test
     public void testRunThrowRightAway() {
         GpxAndZipFiles gpxAndZipFiles = PowerMock.createMock(GpxAndZipFiles.class);
         ImportThreadHelper importThreadHelper = PowerMock.createMock(ImportThreadHelper.class);
 
         expect(gpxAndZipFiles.iterator()).andThrow(new RuntimeException());
         importThreadHelper.cleanup();
 
         PowerMock.replayAll();
         try {
             new ImportThreadDelegate(gpxAndZipFiles, importThreadHelper, null).run();
         } catch (Exception e) {
 
         }
         PowerMock.verifyAll();
     }
 }
