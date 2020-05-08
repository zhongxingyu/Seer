 package net.kokkeli.resources;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.Response;
 
 import net.kokkeli.ISettings;
 import net.kokkeli.data.PlayList;
 import net.kokkeli.data.Role;
 import net.kokkeli.data.Track;
 import net.kokkeli.data.User;
 import net.kokkeli.data.db.NotFoundInDatabase;
 import net.kokkeli.data.services.IPlaylistService;
 import net.kokkeli.data.services.ServiceException;
 import net.kokkeli.resources.models.BaseModel;
 import net.kokkeli.resources.models.ModelPlaylists;
 import net.kokkeli.resources.models.ModelPlaylist;
 import net.kokkeli.server.IFileSystem;
 import net.kokkeli.server.NotAuthenticatedException;
 import net.kokkeli.server.RenderException;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import com.sun.jersey.core.header.FormDataContentDisposition;
 
 import static org.mockito.Mockito.*;
 
 public class TestPlaylistResource extends ResourceTestsBase {
     private static final long EXISTING_PLAYLIST = 654;
     private static final String UPLOADER_USERNAME = "user";
     private static final String TRACKS_FOLDER = "trackfolder";
     private static final String CORRECT_TRACKNAME = "Humppajuna 666";
     private static final String CORRECT_ARTISTNAME = "Humppupumppu";
     private static final String EXISTING_PLAYLIST_NAME = "Great list";
     private static final String FORM_NAME = "name";
 
     private IFileSystem mockFilesystem;
     private ISettings mockSettings;
     private IPlaylistService mockPlaylistService;
 
     private PlaylistsResource resource;
     private PlayList existingList;
 
     @Override
     public void before() throws Exception {
         mockFilesystem = mock(IFileSystem.class);
         mockSettings = mock(ISettings.class);
         mockPlaylistService = mock(IPlaylistService.class);
 
         existingList = new PlayList(EXISTING_PLAYLIST);
         existingList.setName("Heiyah");
 
         when(mockPlaylistService.getPlaylist(EXISTING_PLAYLIST)).thenReturn(
                 existingList);
         when(mockPlaylistService.nameExists(EXISTING_PLAYLIST_NAME))
                 .thenReturn(true);
         when(mockSettings.getTracksFolder()).thenReturn(TRACKS_FOLDER);
 
         resource = new PlaylistsResource(getLogger(), getTemplateService(),
                 getPlayer(), getSessionService(), mockSettings,
                 mockPlaylistService, mockFilesystem);
     }
 
     @Test
     public void testPlaylistsReturnModelWithPlaylists() throws RenderException, NotAuthenticatedException, ServiceException{
         //Mock playlists for service
         ArrayList<PlayList> playlists = new ArrayList<PlayList>();
         PlayList mockList = new PlayList(0);
         mockList.setName("Name 1");
         playlists.add(mockList);
         PlayList mockList2 = new PlayList(1);
         mockList2.setName("Name 2");
         playlists.add(mockList2);
         when(mockPlaylistService.getIdNames()).thenReturn(playlists);
         
         ModelAnswer answer = new ModelAnswer();
         when(getTemplateService().process(any(String.class), any(BaseModel.class))).thenAnswer(answer);
         
         resource.playlists(buildRequest());
         
         BaseModel base = answer.getModel();
         
         Assert.assertTrue(base.getModel() instanceof ModelPlaylists);
         ModelPlaylists model = (ModelPlaylists)base.getModel();
         
         Assert.assertEquals(2, model.getItems().size());
         Assert.assertEquals(mockList.getId(), model.getItems().get(0).getId());
         Assert.assertEquals(mockList.getName(), model.getItems().get(0).getName());
         Assert.assertEquals(mockList2.getId(), model.getItems().get(1).getId());
         Assert.assertEquals(mockList2.getName(), model.getItems().get(1).getName());
     }
     
     @Test
     public void testPlaylistsRedirectsWhenServiceExceptionIsThrown() throws NotAuthenticatedException, ServiceException, RenderException{
         when(mockPlaylistService.getIdNames()).thenThrow(new ServiceException("Boom says database!"));
 
         assertRedirectError(resource.playlists(buildRequest()), "Something went wrong with service.");
     }
     
     @Test
     public void testPlaylistsRedirectsWhenRenderExceptionIsThrown() throws RenderException, NotAuthenticatedException, ServiceException{
         when(mockPlaylistService.getIdNames()).thenReturn(new ArrayList<PlayList>());
         when(getTemplateService().process(any(String.class), any(BaseModel.class))).thenThrow(new RenderException("Boom says database!"));
         
         assertRedirectError(resource.playlists(buildRequest()), "There was a problem with rendering the template.");
     }
     
     @Test
     public void testAddPostWithProperValueTriesToWriteToFile()
             throws ServiceException, NotFoundInDatabase,
             NotAuthenticatedException, IOException {
         final String filename = "filename";
 
         InputStream mockStream = mock(InputStream.class);
         FormDataContentDisposition mockDisposition = mock(FormDataContentDisposition.class);
         when(mockDisposition.getFileName()).thenReturn(filename);
 
         Response r = resource.addUpload(buildRequest(), EXISTING_PLAYLIST,
                 CORRECT_ARTISTNAME, CORRECT_TRACKNAME, mockStream,
                 mockDisposition);
        Assert.assertEquals(REDIRECT, r.getStatus());
         verify(mockFilesystem).writeToFile(any(InputStream.class),
                 eq(TRACKS_FOLDER + "/" + filename));
         verify(mockFilesystem).fileExists(TRACKS_FOLDER + "/" + filename);
     }
 
     @Test
     public void testAddPostReturnsErrorWhenFileExists()
             throws ServiceException, NotFoundInDatabase,
             NotAuthenticatedException, RenderException {
         ModelAnswer answer = new ModelAnswer();
         when(
                 getTemplateService().process(any(String.class),
                         any(BaseModel.class))).thenAnswer(answer);
 
         final String filename = "filename";
         InputStream mockStream = mock(InputStream.class);
         FormDataContentDisposition mockDisposition = mock(FormDataContentDisposition.class);
         when(mockDisposition.getFileName()).thenReturn(filename);
         when(mockFilesystem.fileExists(any(String.class))).thenReturn(true);
 
         assertModelResponse(
                 resource.addUpload(buildRequest(), EXISTING_PLAYLIST,
                         CORRECT_ARTISTNAME, CORRECT_TRACKNAME, mockStream,
                         mockDisposition),
                 answer,
                 "Similar file already exists. Remove existing file, or upload different.",
                 null);
     }
 
     @Test
     public void testCreateChecksForPlaylistNameValidity()
             throws NotAuthenticatedException, ServiceException,
             BadRequestException, RenderException {
         ModelAnswer answer = new ModelAnswer();
         when(getTemplateService().process(any(String.class),
                         any(BaseModel.class))).thenAnswer(answer);
 
         try {
             String nullName = null;
             resource.create(buildRequest(), createCreatePost(nullName));
             Assert.fail("Request with null name should throw bad request.");
         } catch (BadRequestException e) {
             Assert.assertEquals("Form did not contain name.", e.getMessage());
         }
 
         String name = "";
         assertModelResponse(
                 resource.create(buildRequest(), createCreatePost(name)),
                 answer, "Playlist did not have a name.", null);
 
         assertModelResponse(resource.create(buildRequest(),
                 createCreatePost(EXISTING_PLAYLIST_NAME)), answer,
                 String.format("Playlist with name %s already exits.",
                         EXISTING_PLAYLIST_NAME), null);
     }
 
     @Test
     public void testDetailsGetWithExistingPlaylistWorks()
             throws RenderException, ServiceException, NotAuthenticatedException {
         fillPlaylistWithTracks(existingList);
 
         ModelAnswer answer = new ModelAnswer();
         when(
                 getTemplateService().process(any(String.class),
                         any(BaseModel.class))).thenAnswer(answer);
 
         assertModelResponse(RESPONSE_OK,
                 resource.details(buildRequest(), EXISTING_PLAYLIST), answer,
                 null, null);
         ModelPlaylist playlist = (ModelPlaylist) answer.getModel().getModel();
         Assert.assertEquals(existingList.getName(), playlist.getName());
         Assert.assertEquals(existingList.getItems().size(), playlist.getItems()
                 .size());
     }
 
     private static void fillPlaylistWithTracks(PlayList list) {
         for (int i = 0; i < 43; i++) {
             Track item = new Track(i);
             item.setTrackName("Track " + i);
             item.setArtist("Artis");
             item.setLocation("");
             User uploader = new User(UPLOADER_USERNAME, Role.USER);
             item.setUploader(uploader);
             list.getItems().add(item);
         }
     }
 
     private MultivaluedMap<String, String> createCreatePost(String name) {
         @SuppressWarnings("unchecked")
         MultivaluedMap<String, String> map = mock(MultivaluedMap.class);
 
         when(map.containsKey(FORM_NAME)).thenReturn(true);
 
         when(map.getFirst(FORM_NAME)).thenReturn(name);
         return map;
     }
 }
