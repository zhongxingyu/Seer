 package net.kokkeli.server;
 
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 
 import org.junit.Assert;
 
 import net.kokkeli.ISettings;
 import net.kokkeli.data.Role;
 import net.kokkeli.resources.Field;
 import net.kokkeli.resources.models.BaseModel;
 import net.kokkeli.resources.models.ModelPlaylist;
 import net.kokkeli.resources.models.ModelPlaylistItem;
 import net.kokkeli.resources.models.ModelPlaylists;
 import net.kokkeli.resources.models.ModelUser;
 import net.kokkeli.resources.models.ModelUsers;
 import net.kokkeli.resources.models.ViewModel;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import freemarker.template.TemplateModelException;
 
 import static org.mockito.Mockito.*;
 
 public class TestTemplates {
     private static final String CORRECT_TEMPLATE_LOCATION = "target/classes/net/kokkeli/resources/views";
     private static final String CORRECT_TEMPLATE = "index.ftl";
     
     private ITemplateService templateService;
 
     @Before
     public void setUp() throws Exception {
         ISettings mockSettings = mock(ISettings.class);
         when(mockSettings.getTemplatesLocation()).thenReturn(CORRECT_TEMPLATE_LOCATION);
         
         templateService = new Templates(mockSettings);
     }
 
     @Test
     public void testProcessingModelThrowsExceptionWithWrongParameters() throws IOException, TemplateModelException {
         try {
             templateService.process("", new BaseModel());
             fail("Processing should have thrown a rendering exception.");
         } catch (RenderException e) {
             Assert.assertEquals("Template with name:  was not found", e.getMessage());
         }
 
         try {
             templateService.process(null, new BaseModel());
             fail("Processing should have thrown a rendering exception.");
         } catch (RenderException e) {
             Assert.assertEquals("Template name cant be null.", e.getMessage());
         }
         
         try {
             templateService.process(CORRECT_TEMPLATE, null);
             fail("Processing should have thrown a rendering exception.");
         } catch (RenderException e) {
             Assert.assertEquals("Model can't be null.", e.getMessage());
         }
     }
     
     @Test
     public void testInitializingThrowsExceptionWithWrongParameters() throws TemplateModelException{
         final String nonExistingTemplate = "asdf";
         
         try {
             ISettings mockSettings = mock(ISettings.class);
             when(mockSettings.getTemplatesLocation()).thenReturn("");
             
             new Templates(mockSettings);
         } catch (IOException e) {
             Assert.assertEquals("", e.getMessage());
         }
         
         try {
             ISettings mockSettings = mock(ISettings.class);
             when(mockSettings.getTemplatesLocation()).thenReturn(nonExistingTemplate);
             
             new Templates(mockSettings);
         } catch (IOException e) {
             Assert.assertEquals(nonExistingTemplate +" does not exist.", e.getMessage());
         }
     }
     
     @Test
     public void testProcessingWithCorrectValuesDoesntThrowException() throws IOException, RenderException, TemplateModelException{
         String result = templateService.process(CORRECT_TEMPLATE, correctBaseModel());
         
         Assert.assertNotNull("Result should have value.", result);
         Assert.assertTrue("Result should have been longer.", result.length() > 1);
     }
     
      @Test
      public void testProcessingThrowCorrectExceptionIfModelHasInvalidFields(){
          BaseModel base = new BaseModel();
          
          ViewModel invalidField = new ViewModel(){
              @Field
              public void getMake(String pekka){
                  return;
              }
          };
          base.setModel(invalidField);
          
          try {
              templateService.process(CORRECT_TEMPLATE, base);
             Assert.fail("Processing template with invalid model should throw exception.");
         } catch (Exception e) {
             Assert.assertEquals("Provided viewmodel contained Field-annotations with arguments.", e.getMessage());
         }
      }
      
      @Test
      public void testAllTemplatesCanBeRendered() throws RenderException{        
          HashMap<String, Collection<BaseModel>> map = new HashMap<String, Collection<BaseModel>>();
          authenticationViews(map);
          userManagementViews(map);
          playlistViews(map);
          indexView(map);
          
          for (String key : map.keySet()) {
             Collection<BaseModel> models = map.get(key);
             
             for (BaseModel baseModel : models) {
                 templateService.process(key, baseModel);
             }
         }
      }
      
      private static BaseModel correctBaseModel(){
          BaseModel model = new BaseModel();
          model.setNowPlaying("");
          model.setUsername("");
          return model;
      }
      
     private static BaseModel uploadModel(){
         BaseModel model = correctBaseModel();
         ModelPlaylistItem item = new ModelPlaylistItem();
         item.setPlaylistId(0);
         model.setModel(item);
         return model;
     }
     
      private static BaseModel correctBaseModelWithModel(ViewModel view){
          BaseModel model = correctBaseModel();
         
          model.setModel(view);
          return model;
      }
      
      private static void authenticationViews(HashMap<String, Collection<BaseModel>> map){
          ArrayList<BaseModel> models = new ArrayList<BaseModel>();
          models.add(correctBaseModel());
          map.put("authenticate.ftl", models);
      }
      
      private static void userManagementViews(HashMap<String, Collection<BaseModel>> map){
          final String correctUsername = "jeah";
          
          ArrayList<BaseModel> listModels = new ArrayList<BaseModel>();
          listModels.add(correctBaseModelWithModel(new ModelUsers()));
          
          ModelUsers listWithUsers = new ModelUsers();
          listWithUsers.add(new ModelUser(54, correctUsername, Role.ADMIN));
          listWithUsers.add(new ModelUser(14, correctUsername, Role.USER));
          listModels.add(correctBaseModelWithModel(listWithUsers));
          map.put("user/users.ftl", listModels);
          
          ArrayList<BaseModel> createModels = new ArrayList<BaseModel>();
          createModels.add(correctBaseModel());
          map.put("user/user_create.ftl", createModels);
          
          ArrayList<BaseModel> editModels = new ArrayList<BaseModel>();
          editModels.add(correctBaseModelWithModel(new ModelUser(4131, correctUsername, Role.ADMIN)));
          editModels.add(correctBaseModelWithModel(new ModelUser(431, correctUsername, Role.USER)));
          map.put("user/user_edit.ftl", editModels);
          
          ArrayList<BaseModel> detailModels = new ArrayList<BaseModel>();
          detailModels.add(correctBaseModelWithModel(new ModelUser(4131, correctUsername, Role.ADMIN)));
          detailModels.add(correctBaseModelWithModel(new ModelUser(431, correctUsername, Role.USER)));
          map.put("user/user.ftl", detailModels);
      }
      
      private static void playlistViews(HashMap<String, Collection<BaseModel>> map){
          final long playlistId = 5;
          final String playlistName = "Lan playlist";
          
          ArrayList<BaseModel> listModels = new ArrayList<BaseModel>();
          listModels.add(correctBaseModelWithModel(new ModelPlaylists()));
          
          ModelPlaylists listWithItems = new ModelPlaylists();
          for (int i = 0; i < 5; i++) {
             ModelPlaylist list = new ModelPlaylist(i);
             list.setName(playlistName);
             listWithItems.getItems().add(list);
          }
          listModels.add(correctBaseModelWithModel(listWithItems));
          
          map.put("playlist/playlists.ftl", listModels);
          
          ArrayList<BaseModel> models = new ArrayList<BaseModel>();
         
         models.add(uploadModel());
          map.put("playlist/add.ftl", models);
          
          ArrayList<BaseModel> detailModels = new ArrayList<BaseModel>();
          ModelPlaylist emptyList = new ModelPlaylist(playlistId);
          emptyList.setName(playlistName);
          
          detailModels.add(correctBaseModelWithModel(emptyList));
          
          ModelPlaylist playlistWithItems = new ModelPlaylist(playlistId);
          playlistWithItems.setName(playlistName);
          for (int i = 0; i < 7; i++) {
              ModelPlaylistItem item = new ModelPlaylistItem();
              item.setArtist("Mockker " + i);
              item.setTrackName("Murdockmardock " + i);
              item.setUploader("username");
              playlistWithItems.getItems().add(item);
          }
          detailModels.add(correctBaseModelWithModel(playlistWithItems));
          
          map.put("playlist/details.ftl", detailModels);
      }
      
      private static void indexView(HashMap<String, Collection<BaseModel>> map){
          ArrayList<BaseModel> models = new ArrayList<BaseModel>();
          models.add(correctBaseModel());
          
          map.put("index.ftl", models);
      }
 }
