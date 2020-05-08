 package com.lastcrusade.fanclub.service;
 
 import java.util.Arrays;
 import java.util.List;
 
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 
 import com.lastcrusade.fanclub.model.SongMetadata;
 import com.lastcrusade.fanclub.util.BroadcastRegistrar;
 import com.lastcrusade.fanclub.util.IBroadcastActionHandler;
 
 public class MusicLibraryServiceTest extends AServiceTest<MusicLibraryService> {
 
     private static final String TAG = MusicLibraryServiceTest.class.getName();
 
     private class TestHandler implements IBroadcastActionHandler {
 
         private int receiveActionCalled = 0;
 
         @Override
         public void onReceiveAction(Context context, Intent intent) {
             this.receiveActionCalled++;
             Log.i(TAG, "onReceiveAction called");
         }
 
         public int getReceiveActionCalled() {
             return receiveActionCalled;
         }
     }
 
     private class InappropriateActionTestHandler implements
             IBroadcastActionHandler {
 
         @Override
         public void onReceiveAction(Context context, Intent intent) {
             fail(intent.getAction() + " action should not be sent.");
         }
     }
 
     public MusicLibraryServiceTest() {
         super(MusicLibraryService.class);
     }
 
     /**
      * Test that the music library service starts empty.
      * 
      * We expect the library to start with whatever is in the device's
      * library, which for the purposes of this test is nothing.
      * 
      * NOTE: This depends on the platform on which its run.  Emulators
      * will start empty, unless there's something installed on them
      * that has sound.
      * 
      */
     public void testLibraryStartsEmpty() {
         MusicLibraryService service = getTheService();
         // empty by default
         assertTrue(service.getLibrary().isEmpty());
     }
 
     /**
      * Test that the library list returned by the service is unmodifiable.
      * 
      * We expect an unmodifiable list, to prevent callers from doing anything dumb.
      * 
      */
     public void testLibraryIsUnmodifiable() {
         MusicLibraryService service = getTheService();
 
         // add one song, make sure we have 1 good item
         SongMetadata meta = new SongMetadata(1,
                 "Bridge over troubled waters", "The Tacoma Narrows",
                 "Shake Rattle and Roll", "00:11:22:33:44:55");
         service.updateLibrary(Arrays.asList(meta), true);
         List<SongMetadata> library = service.getLibrary();
 
         // list should be unmodifiable
         try {
             library.clear();
             fail("Library collection should be unmodifiable");
         } catch (Exception e) {
             // fall thru, this is correct
         }
     }
     /**
      * Test normal operations of getting and updating the library.
      * 
      * We expect the library to grow as more things are added, and for the data
      * to remain intact (not modified by the library).
      * 
      */
     public void testGetAndUpdateLibrary() {
         MusicLibraryService service = getTheService();
         BroadcastRegistrar registrar = new BroadcastRegistrar();
         try {
             TestHandler handler = new TestHandler();
             registrar.addAction(MusicLibraryService.ACTION_LIBRARY_UPDATED,
                     handler).register(service);
 
             // add one song, make sure we have 1 good item
             SongMetadata meta = new SongMetadata(1,
                     "Bridge over troubled waters", "The Tacoma Narrows",
                     "Shake Rattle and Roll", "00:11:22:33:44:55");
             service.updateLibrary(Arrays.asList(meta), true);
             List<SongMetadata> library = service.getLibrary();
             assertEquals(1, library.size());
             assertSongMetaEquals(meta, library.get(0));
 
             // add two more, make sure we have 3 good songs
             SongMetadata meta2 = new SongMetadata(1, "Frisky Frisco",
                     "The Golden Gates", "California", "00:11:22:33:44:56");
             SongMetadata meta3 = new SongMetadata(2, "Crosstown",
                     "Booklyn Bombs", "NYC, The place to be",
                     "00:11:22:33:44:56");
             service.updateLibrary(Arrays.asList(meta2, meta3), true);
             library = service.getLibrary();
             assertEquals(3, library.size());
             // check the individual items (in the order they were added)
             // NOTE: this is a good test, because the above items hashed order
             // is meta2, meta, meta3.  Therefore, if the part of the library
             // that maintains order is broken, this will fail.
             assertSongMetaEquals(meta,  library.get(0));
             assertSongMetaEquals(meta2, library.get(1));
             assertSongMetaEquals(meta3, library.get(2));
 
             // this should have been called 2 times (one for each call to
             // updateLibrary)
             assertEquals(2, handler.getReceiveActionCalled());
         } finally {
             registrar.unregister();
         }
     }
 
     /**
      * Test what happens when we update an existing song.
      * 
      * We expect the library not to grow, and the old data to be replaced.
      * 
      */
     public void testGetUpdateLibraryExisting() {
         MusicLibraryService service = getTheService();
         BroadcastRegistrar registrar = new BroadcastRegistrar();
         try {
             TestHandler handler = new TestHandler();
             registrar.addAction(MusicLibraryService.ACTION_LIBRARY_UPDATED,
                     handler).register(service);
 
             // add one song, make sure we have 1 good item
             SongMetadata meta = new SongMetadata(1,
                     "Bridge over troubled waters", "The Tacoma Narrows",
                     "Shake Rattle and Roll", "00:11:22:33:44:55");
             service.updateLibrary(Arrays.asList(meta), true);
             List<SongMetadata> library = service.getLibrary();
             assertEquals(1, library.size());
             assertSongMetaEquals(meta, library.get(0));
 
             // add a metadata with the same id and mac address...this should
             // replace the Bridge over troubled waters metadata.
             SongMetadata meta2 = new SongMetadata(1, "Frisky Frisco",
                     "The Golden Gates", "California", "00:11:22:33:44:55");
             service.updateLibrary(Arrays.asList(meta2), true);
             library = service.getLibrary();
             assertEquals(1, library.size());
             // check the individual items
             assertSongMetaEquals(meta2, library.get(0));
 
             // this should have been called 2 times (one for each call to
             // updateLibrary.
             assertEquals(2, handler.getReceiveActionCalled());
         } finally {
             registrar.unregister();
         }
     }
 
     /**
      * Test what happens when we update an existing song.
      * 
      * We expect the library not to grow, and the old data to be replaced.
      * 
      */
     public void testRemoveLibraryForAddress() {
         MusicLibraryService service = getTheService();
         BroadcastRegistrar registrar = new BroadcastRegistrar();
         try {
             TestHandler handler = new TestHandler();
             registrar.addAction(MusicLibraryService.ACTION_LIBRARY_UPDATED,
                     handler).register(service);
 
             // add one song, make sure we have 1 good item
             SongMetadata meta = new SongMetadata(1,
                     "Bridge over troubled waters", "The Tacoma Narrows",
                     "Shake Rattle and Roll", "00:11:22:33:44:55");
             // add a metadata with the same id and mac address...this should
             // replace the Bridge over troubled waters metadata.
             SongMetadata meta2 = new SongMetadata(2, "Frisky Frisco",
                     "The Golden Gates", "California", "00:11:22:33:44:55");
             service.updateLibrary(Arrays.asList(meta, meta2), true);
             List<SongMetadata> library = service.getLibrary();
             assertEquals(1, library.size());
             assertSongMetaEquals(meta,  library.get(0));
             assertSongMetaEquals(meta2, library.get(1));
 
             service.removeLibraryForAddress("00:11:22:33:44:55", true);
             library = service.getLibrary();
             assertEquals(0, library.size());
 
             // this should have been called 2 times (one for each call to
             // updateLibrary.
             assertEquals(2, handler.getReceiveActionCalled());
         } finally {
             registrar.unregister();
         }
     }
 
     /**
      * @param meta
      * @param libraryItem
      */
     private void assertSongMetaEquals(SongMetadata meta,
             SongMetadata libraryItem) {
         assertEquals(meta.getTitle(), libraryItem.getTitle());
         assertEquals(meta.getArtist(), libraryItem.getArtist());
         assertEquals(meta.getAlbum(), libraryItem.getAlbum());
         assertEquals(meta.getMacAddress(), libraryItem.getMacAddress());
     }
 }
