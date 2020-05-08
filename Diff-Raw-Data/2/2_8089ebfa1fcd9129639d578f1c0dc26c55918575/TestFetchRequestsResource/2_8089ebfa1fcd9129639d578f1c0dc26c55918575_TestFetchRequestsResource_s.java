 package net.kokkeli.resources;
 
 import java.util.ArrayList;
 
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.Response;
 
 import net.kokkeli.data.FetchRequest;
 import net.kokkeli.data.PlayList;
 import net.kokkeli.data.Track;
 import net.kokkeli.data.services.IFetchRequestService;
 import net.kokkeli.data.services.IPlaylistService;
 import net.kokkeli.data.services.ServiceException;
 import net.kokkeli.resources.models.BaseModel;
 import net.kokkeli.resources.models.ModelFetchRequests;
 import net.kokkeli.server.NotAuthenticatedException;
 import net.kokkeli.server.RenderException;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.*;
 
 public class TestFetchRequestsResource extends ResourceTestsBase {
     private FetchRequestsResource resource;
     private IFetchRequestService mockFetchRequestService;
     private IPlaylistService mockPlaylistService; 
     
     @Override
     public void before() throws Exception {
         mockFetchRequestService = mock(IFetchRequestService.class);
         mockPlaylistService = mock(IPlaylistService.class);
         
         resource = new FetchRequestsResource(getLogger(), getTemplateService(), getPlayer(), getSessionService(), getSettings(), mockFetchRequestService, mockPlaylistService);
     }
     
     @Test
     public void testIndexRedirectWhenRenderingExceptionIsThrown() throws NotAuthenticatedException, ServiceException, RenderException{
         when(getTemplateService().process(any(String.class), any(BaseModel.class))).thenThrow(new RenderException("Boom says database!"));
         assertRedirectError(resource.index(buildRequest()), "There was a problem with rendering the template.");
     }
     
     @Test
     public void testIndexRedirectWhenServiceExceptionIsThrown() throws NotAuthenticatedException, ServiceException, RenderException{
         when(mockFetchRequestService.get()).thenThrow(new ServiceException("Boom says database!"));
         assertRedirectError(resource.index(buildRequest()), "Something went wrong with service.");
     }
     
     @Test
     public void testIndexReturnsCorrectItems() throws NotAuthenticatedException, ServiceException, RenderException{
         ArrayList<FetchRequest> requests = new ArrayList<FetchRequest>();
         FetchRequest basicRequest = new FetchRequest();
         basicRequest.setDestinationFile("Test destination");
         basicRequest.setHandler("test handler");
         basicRequest.setLocation("Test location");
         requests.add(basicRequest);
         
         when(mockFetchRequestService.get()).thenReturn(requests);
         
         ModelAnswer answer = new ModelAnswer();
         when(getTemplateService().process(any(String.class), any(BaseModel.class))).thenAnswer(answer);
         
         Response response = resource.index(buildRequest());
         assertModelResponse(response, answer, null, null);
         
         BaseModel base = answer.getModel();
         Assert.assertTrue(base.getModel() instanceof ModelFetchRequests);
     }
     
     @Test
     public void testCreateRequestGet() throws NotAuthenticatedException, RenderException, ServiceException{
         ModelAnswer answer = new ModelAnswer();
         when(getTemplateService().process(any(String.class), any(BaseModel.class))).thenAnswer(answer);
         
        ArrayList<PlayList> playlists = new ArrayList<>();
         
         for (int i = 0; i < 7; i++) {
             PlayList list = new PlayList(i);
             list.setName("Name " + i);
             playlists.add(list);
         }
         
         when(mockPlaylistService.getIdNames()).thenReturn(playlists);
         
         Response response = resource.createRequest(buildRequest());
         assertModelResponse(response, answer, null, null);
     }
     
     @Test
     public void testRemoveHandledCallsService() throws NotAuthenticatedException, ServiceException{
         resource.removeHandled(buildRequest());
         
         verify(mockFetchRequestService).removeHandled();
     }
     
     @Test
     public void testRemoveRequestCallsService() throws NotAuthenticatedException, ServiceException, BadRequestException{
         long id = 343;
         
         resource.removeRequest(buildRequest(), createIdPost(id));
         
         verify(mockFetchRequestService).remove(id);
     }
     
     @Test
     public void testRemoveRequestThrowsBadRequestExceptionWhenIdCantBeParsed() throws NotAuthenticatedException, ServiceException{
         @SuppressWarnings("unchecked")
         MultivaluedMap<String, String> map = mock(MultivaluedMap.class);
         when(map.getFirst(anyString())).thenReturn(null);
         when(map.containsKey(any())).thenReturn(false);
         
         try {
             resource.removeRequest(buildRequest(), map);
             Assert.fail("There should have been an exception when there is no id");
         } catch (BadRequestException e) {
             // This should happen.
         }
         
         try {
             resource.removeRequest(buildRequest(), createIdPost("invalid id"));
             Assert.fail("There should have been an exception when there is no id");
         } catch (BadRequestException e) {
             // This should happen.
         }
     }
     
     @Test
     public void testRequestsReturnsCorrectRequests() throws NotAuthenticatedException, ServiceException{
         ArrayList<FetchRequest> requests = new ArrayList<FetchRequest>();
         
         for (int i = 0; i < 3; i++) {
             FetchRequest r = new FetchRequest();
             r.setDestinationFile("Destination " + i);
             r.setHandler("Handler " + i);
             r.setId(i);
             r.setLocation("Location " + i);
             r.setTrack(new Track());
             r.setPlaylist(new PlayList(i));
             requests.add(r);
         }
         
         when(mockFetchRequestService.get()).thenReturn(requests);
         
         ModelFetchRequests model = resource.requests(buildRequest());
         Assert.assertEquals(3, model.getItems().size());
     }
 }
