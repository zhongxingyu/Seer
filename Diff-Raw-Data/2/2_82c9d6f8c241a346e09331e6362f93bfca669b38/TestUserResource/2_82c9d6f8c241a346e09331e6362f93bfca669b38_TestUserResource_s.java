 package net.kokkeli.resources;
 
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.Response;
 
 import static org.junit.Assert.assertEquals;
 import net.kokkeli.data.Role;
 import net.kokkeli.data.User;
 import net.kokkeli.data.db.NotFoundInDatabase;
 import net.kokkeli.data.services.IUserService;
 import net.kokkeli.data.services.ServiceException;
 import net.kokkeli.resources.models.BaseModel;
 import net.kokkeli.server.NotAuthenticatedException;
 import net.kokkeli.server.RenderException;
 
 import org.junit.Assert;
 import org.junit.Test;
 import com.sun.jersey.api.NotFoundException;
 
 import static org.mockito.Mockito.*;
 
 public class TestUserResource extends ResourceTestsBase{
     private static final long EXISTING_USER_ID = 54;
     private static final long NONEXISTING_ID = -3;    
     private static final String FORM_USERNAME = "username";
     private static final String FORM_ROLE = "role";
     private static final String FORM_ID = "id";
     
     private static final String INVALID_CHARACHTERS_USERNAME = "editedUser<";
     private static final String EMPTY_USERNAME = "";
     
     private IUserService mockUserService;
     
     private User existing;
     
     private UsersResource userResource;
 
     public void before() throws NotFoundInDatabase, ServiceException {
         mockUserService = mock(IUserService.class);
         
         existing = new User(EXISTING_USER_ID, "user", Role.NONE);
         
         when(mockUserService.get(EXISTING_USER_ID)).thenReturn(existing);
         when(mockUserService.get(NONEXISTING_ID)).thenThrow(new NotFoundInDatabase("User not found"));
         
         userResource = new UsersResource(getLogger(), getTemplateService(), mockUserService, getPlayer(), getSessionService(), getSettings());
     }
     
     // USER DETAILS GET
     @Test
     public void testGetDetailsRedirectsWhenUserIsNotFound() throws RenderException, ServiceException, NotAuthenticatedException{
         assertRedirectAndError(userResource.userDetails(buildRequest(), NONEXISTING_ID), "User not found.");
     }
     
     @Test
     public void testGetDetailsRedirectWhenTemplateCantBeProcessed() throws RenderException, NotFoundException, NotAuthenticatedException{
         when(getTemplateService().process(any(String.class), any(BaseModel.class))).thenThrow(new RenderException("Rendering failed"));
         assertRedirectAndError(userResource.userDetails(buildRequest(), EXISTING_USER_ID), "There was a problem with rendering the template.");
     }
     
     @Test
     public void testGetDetailsPutsTemplateAndOkInResponse() throws NotFoundException, ServiceException, RenderException, NotAuthenticatedException {
         final String processedTemplate = "Jeeah";
         
         when(getTemplateService().process(any(String.class), any(BaseModel.class))).thenReturn(processedTemplate);
         
         Response r = userResource.userDetails(buildRequest(), EXISTING_USER_ID);
         assertEquals(processedTemplate, r.getEntity().toString());
         assertEquals(RESPONSE_OK, r.getStatus());
         verify(getTemplateService()).process(anyString(), isA(BaseModel.class));
     }
     
     //EDIT GET
     @Test
     public void testGetEditThrowsNotFoundException() throws ServiceException, NotAuthenticatedException{
         assertRedirectAndError(userResource.userEdit(buildRequest(), NONEXISTING_ID), "User not found.");
     }
     
     @Test
     public void testGetEditThrowsServiceExceptionWhenTemplateServiceFails() throws RenderException, NotAuthenticatedException{
         when(getTemplateService().process(any(String.class), any(BaseModel.class))).thenThrow(new RenderException("Rendering failed"));
         assertRedirectAndError(userResource.userEdit(buildRequest(), EXISTING_USER_ID), "There was a problem with rendering the template.");
     }
     
     @Test
     public void testGetEditPutsTemplateAndOkInResponse() throws RenderException, NotFoundException, ServiceException, NotAuthenticatedException{
         final String processedTemplate = "Jeeah";
         when(getTemplateService().process(any(String.class), any(BaseModel.class))).thenReturn(processedTemplate);
         
         Response r = userResource.userEdit(buildRequest(), EXISTING_USER_ID);
         assertEquals(processedTemplate, r.getEntity().toString());
         assertEquals(RESPONSE_OK, r.getStatus());
         verify(getTemplateService()).process(anyString(), isA(BaseModel.class));
     }
     
     //EDIT POST
     @Test
     public void testPostEditUpdatesUser() throws ServiceException, BadRequestException, RenderException, NotAuthenticatedException, NotFoundInDatabase{
         final String newUsername = "editedUser";
         final Role newRole = Role.ADMIN;
         
         Response r = userResource.userEdit(buildRequest(), editUserPost(EXISTING_USER_ID, newUsername, newRole));
         assertSessionInfo("User edited.");
         assertEquals(REDIRECT, r.getStatus());
         verify(mockUserService, times(1)).update(new User(EXISTING_USER_ID, newUsername, newRole));
     }
     
     @Test
     public void testPostEditWithInvalidUsernameReturnsError() throws RenderException, BadRequestException, NotAuthenticatedException {
         final Role newRole = Role.ADMIN;
         ModelAnswer answer = new ModelAnswer();
         when(getTemplateService().process(any(String.class), any(BaseModel.class))).thenAnswer(answer);
         
         assertModelResponse(userResource.userEdit(buildRequest(), editUserPost(EXISTING_USER_ID, INVALID_CHARACHTERS_USERNAME, newRole)), answer, "Invalid username.", null);
         assertModelResponse(userResource.userEdit(buildRequest(), editUserPost(EXISTING_USER_ID, EMPTY_USERNAME, newRole)), answer, "Username is required.", null);
     }
     
     @Test
     public void testPostEditWithExistingUsernameReturnsError() throws ServiceException, RenderException, BadRequestException, NotAuthenticatedException{
         final String existing = "existing";
         final Role newRole = Role.ADMIN;
         
         ModelAnswer answer = new ModelAnswer();
         when(getTemplateService().process(any(String.class), any(BaseModel.class))).thenAnswer(answer);
         when(mockUserService.exists(any(String.class))).thenReturn(true);
         
         assertModelResponse(userResource.userEdit(buildRequest(), editUserPost(EXISTING_USER_ID, existing, newRole)), answer, "Username already exists.", null);
     }
     
     //CREATE POST
     @Test
     public void testCreatePostWithWrongUsernameReturnsError() throws RenderException, BadRequestException, ServiceException, NotAuthenticatedException{
         ModelAnswer answer = new ModelAnswer();
         when(getTemplateService().process(any(String.class), any(BaseModel.class))).thenAnswer(answer);
         
         assertModelResponse(userResource.userCreate(buildRequest(), createUserPost(INVALID_CHARACHTERS_USERNAME, Role.ADMIN)), answer, "Username was invalid.", null);
         assertModelResponse(userResource.userCreate(buildRequest(), createUserPost(EMPTY_USERNAME, Role.ADMIN)), answer, "Username is required.", null);
     }
     
     @Test
     public void testCreatePostWithCorrectUsernameSucceeds() throws RenderException, BadRequestException, ServiceException, NotAuthenticatedException{
         final String username = "fdsfsd";
         final Role role = Role.ADMIN;
         final long anyId = 434;
         
         User mockUser = mock(User.class);
         when(mockUser.getId()).thenReturn(anyId);
         when(mockUserService.add(any(User.class))).thenReturn(mockUser);
         
         Response r = userResource.userCreate(buildRequest(), createUserPost(username, Role.ADMIN));
         assertEquals(REDIRECT, r.getStatus());
         assertSessionInfo("User created.");
         verify(mockUserService, times(1)).add(new User(username, role));
     }
 
     //CREATE GET
     @Test
     public void testCreateGetReturnsResponse() throws RenderException, NotAuthenticatedException {
         ModelAnswer answer = new ModelAnswer();
         when(getTemplateService().process(any(String.class), any(BaseModel.class))).thenAnswer(answer);
         
         Response r = userResource.userCreate(buildRequest());
         Assert.assertNotNull(r);
         assertEquals(RESPONSE_OK, r.getStatus());
         
         Assert.assertNull(answer.getModel().getError());
         Assert.assertNull(answer.getModel().getInfo());
         Assert.assertNull(answer.getModel().getModel());
     }
     
     @Test
     public void testCreateGetReturnsRedirectWhenRenderingFails() throws RenderException, NotAuthenticatedException
     {
         when(getTemplateService().process(any(String.class), any(BaseModel.class))).thenThrow(new RenderException("Suprise exception."));
         assertRedirectAndError(userResource.userCreate(buildRequest()), "There was a problem with rendering the template.");
     }
     
     /**
      * Mocks MultivaluedMap var user creation posts.
      * @param username
      * @param role
      * @return
      */
     private MultivaluedMap<String, String> createUserPost(String username, Role role){
         @SuppressWarnings("unchecked")
         MultivaluedMap<String, String> map = mock(MultivaluedMap.class);
         
         when(map.containsKey(FORM_USERNAME)).thenReturn(true);
         when(map.containsKey(FORM_ROLE)).thenReturn(true);
         
         when(map.getFirst(FORM_USERNAME)).thenReturn(username);
         when(map.getFirst(FORM_ROLE)).thenReturn(role.name().toUpperCase());
         return map;
     }
     
     /**
     * Mocks MultivaluedMap var user dit posts.
      * @param username
      * @param role
      * @return
      */
     private MultivaluedMap<String, String> editUserPost(long id, String username, Role role){
         MultivaluedMap<String, String> map = createUserPost(username, role);
         
         when(map.containsKey(FORM_ID)).thenReturn(true);
         when(map.getFirst(FORM_ID)).thenReturn(id + "");
         
         return map;
     }
     
     /**
      * Asserts that Response is redirect and error is correct.
      * @param response Response
      * @param error Error
      */
     private void assertRedirectAndError(Response response, String error){
         assertSessionError(error);
         Assert.assertEquals(REDIRECT, response.getStatus());
     }
 }
