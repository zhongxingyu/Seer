 /**
  *
  * Copyright 2010 Laurent Desgrange
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 package net.desgrange.pwad.service;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Arrays;
 
 import net.desgrange.pwad.model.Album;
 import net.desgrange.pwad.model.Picture;
 import net.desgrange.pwad.service.exceptions.BadUrlException;
 import net.desgrange.pwad.service.exceptions.DownloadFailedException;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 import org.junit.rules.TemporaryFolder;
 
 import com.google.gdata.client.photos.PicasawebService;
 import com.google.gdata.data.TextConstruct;
 import com.google.gdata.data.media.mediarss.MediaContent;
 import com.google.gdata.data.media.mediarss.MediaGroup;
 import com.google.gdata.data.photos.AlbumFeed;
 import com.google.gdata.data.photos.GphotoEntry;
 import com.google.gdata.data.photos.PhotoEntry;
 import com.google.gdata.util.common.xml.XmlWriter;
 
 public class PwadServiceTest {
     private PwadService pwadService;
     private PicasawebService picasawebService;
     @Rule
     public final ExpectedException thrown = ExpectedException.none();
     @Rule
     public TemporaryFolder testFolder = new TemporaryFolder();
 
     @Before
     public void setUp() throws Exception {
         picasawebService = mock(PicasawebService.class);
         pwadService = new PwadService(picasawebService);
     }
 
     @Test
     public void testGetAlbumByInvitationLink() throws Exception {
         final AlbumFeed albumFeed = mock(AlbumFeed.class);
         final String expectedUrl = "http://picasaweb.google.com/data/feed/api/user/my_name/albumid/1234567890?kind=photo&imgmax=d";
         when(picasawebService.getFeed(new URL(expectedUrl), AlbumFeed.class)).thenReturn(albumFeed);
         when(albumFeed.getGphotoId()).thenReturn("1234567890");
         when(albumFeed.getTitle()).thenReturn(new PlainTextConstruct("My album"));
         when(albumFeed.getEntries()).thenReturn(Arrays.asList(createEntry("01", "100_0001.JPG"), createEntry("02", "100_0002.JPG")));
 
         final Album actual = pwadService.getAlbumByInvitationLink("http://picasaweb.google.fr/lh/sredir?uname=my_name&target=ALBUM&id=1234567890&authkey=ABC123DE-FGH456IJK78&invite=LMN90OPQ&feat=email");
         assertEquals("1234567890", actual.getId());
         assertEquals("My album", actual.getName());
         assertEquals(2, actual.getPictures().size());
         assertEquals("01", actual.getPictures().get(0).getId());
         assertEquals("100_0001.JPG", actual.getPictures().get(0).getName());
         assertEquals("http://foo/bar/100_0001.JPG", actual.getPictures().get(0).getUrl());
     }
 
     @Test
     public void testGetAlbumByInvitationLinkThrowsAnExceptionWhenGivenUrlIsInvalid() {
         thrown.expect(BadUrlException.class);
         pwadService.getAlbumByInvitationLink("bad url");
     }
 
     @Test
     public void testGetAlbumByInvitationLinkThrowsAnExceptionWhenGivenUrlIsNotSupported() {
         thrown.expect(BadUrlException.class);
         thrown.expectMessage("The link provided is not supported.");
         pwadService.getAlbumByInvitationLink("http://picasaweb.google.fr/lh/sredir");
     }
 
     @Test
     public void testDownloadPicture() throws Exception {
         final File outputFolder = testFolder.newFolder("output_folder");
         final URL pictureFile = getClass().getResource("/pictures/100_0001.JPG");
         final Picture picture = new Picture();
         picture.setName("01.JPG");
         picture.setUrl("file://" + pictureFile.getFile());
 
         pwadService.downloadPicture(picture, outputFolder);
         assertTrue(FileUtils.contentEquals(new File(pictureFile.toURI()), outputFolder.listFiles()[0]));
     }
 
     @Test
     public void testDownloadPictureThrowsAnExceptionIfDownloadFailed() {
         final File outputFolder = testFolder.newFolder("output_folder");
         final Picture picture = new Picture();
         picture.setName("02.JPG");
         picture.setUrl("bad url");
 
         thrown.expect(DownloadFailedException.class);
         pwadService.downloadPicture(picture, outputFolder);
     }
 
    @SuppressWarnings("rawtypes")
     private GphotoEntry createEntry(final String id, final String name) {
         final MediaContent content = new MediaContent();
         content.setUrl("http://foo/bar/" + name);
         final MediaGroup mediaGroup = new MediaGroup();
         mediaGroup.addContent(content);
         final PhotoEntry entry = new PhotoEntry();
         entry.setId(id);
         entry.setTitle(new PlainTextConstruct(name));
         entry.setMediaGroup(mediaGroup);
         return entry;
     }
 
     private static class PlainTextConstruct extends TextConstruct {
         private final String text;
 
         public PlainTextConstruct(final String text) {
             this.text = text;
         }
 
         @Override
         public void generateAtom(final XmlWriter w, final String elementName) throws IOException {
         }
 
         @Override
         public void generateRss(final XmlWriter w, final String elementName, final RssFormat rssFormat) throws IOException {
         }
 
         @Override
         public String getPlainText() {
             return text;
         }
 
         @Override
         public int getType() {
             return 0;
         }
 
         @Override
         public boolean isEmpty() {
             return false;
         }
     }
 }
