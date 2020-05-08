 package com.abudko.scheduled.service.huuto;
 
 import static java.util.Arrays.asList;
 import static org.hamcrest.Matchers.containsString;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.notNullValue;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.springframework.test.util.ReflectionTestUtils.setField;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.Locale;
 
 import org.hamcrest.Matchers;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.springframework.context.ApplicationContext;
 
 import com.abudko.reseller.huuto.image.ImageManipulator;
 import com.abudko.reseller.huuto.query.enumeration.Category;
 import com.abudko.reseller.huuto.query.service.item.ItemResponse;
 import com.abudko.reseller.huuto.query.service.list.ListResponse;
 import com.abudko.scheduled.csv.PhotoData;
 import com.abudko.scheduled.service.PhotoManager;
 
 @RunWith(MockitoJUnitRunner.class)
 public class PublishManagerTest {
 
     @Mock
     private PhotoManager photoManager;
 
     @Mock
     private ApplicationContext context;
 
     @Mock
     private ImageManipulator imageManipulator;
 
     @Mock
     private AlbumMapper albumMapper;
 
     @InjectMocks
     private PublishManager publishManager = new PublishManager();
 
     @Before
     public void setup() {
         setField(publishManager, "imageTempFileLocation", "imageTempFileLocation");
         when(albumMapper.getAlbumId(Mockito.anyString(), Mockito.any(Integer.class))).thenReturn("23876523");
     }
 
     @Test
     public void testPublish() throws Exception {
         ListResponse listResponse1 = new ListResponse();
         listResponse1.setSize("90");
         listResponse1.setItemResponse(new ItemResponse());
         ListResponse listResponse2 = new ListResponse();
         listResponse2.setSize("90");
         listResponse2.setItemResponse(new ItemResponse());
         ListResponse listResponse3 = new ListResponse();
         listResponse3.setSize("90");
         listResponse3.setItemResponse(new ItemResponse());
         List<ListResponse> list = Arrays.asList(listResponse1, listResponse2, listResponse3);
 
         publishManager.publishResults(Category.TALVIHAALARI, list);
 
         verify(photoManager, times(3)).publishPhoto(Mockito.any(PhotoData.class));
     }
 
     @Test
     public void testCropImage() throws Exception {
         ListResponse listResponse = new ListResponse();
         listResponse.setSize("90");
         ItemResponse item = new ItemResponse();
         final String imgBaseSrc = "imgBaseSrc";
         item.setImgBaseSrc(imgBaseSrc);
         listResponse.setItemResponse(item);
         List<ListResponse> list = Arrays.asList(listResponse);
         final String location = "gjyug";
         setField(publishManager, "imageTempFileLocation", location);
 
         publishManager.publishResults(Category.TALVIHAALARI, list);
 
         verify(imageManipulator).storeImage(imgBaseSrc + "-orig.jpg", "file:" + location);
     }
 
     @Test
     public void testDescriptionId() throws Exception {
         ListResponse listResponse = new ListResponse();
         listResponse.setSize("90");
         ItemResponse itemResponse = new ItemResponse();
         final String id = "id";
         itemResponse.setId(id);
         listResponse.setItemResponse(itemResponse);
         when(
                 context.getMessage(Mockito.anyString(),
                         Mockito.argThat(Matchers.<Object> hasItemInArray(is((Object) id))), Mockito.any(Locale.class)))
                 .thenReturn(id);
 
         publishManager.publishResults(Category.TALVIHAALARI, asList(listResponse));
 
         verify(photoManager).publishPhoto(
                 Mockito.argThat(Matchers.<PhotoData> hasProperty("description", containsString(id))));
     }
 
     @Test
     public void testDescriptionSize() throws Exception {
         ListResponse listResponse = new ListResponse();
         ItemResponse itemResponse = new ItemResponse();
         final String size = "90";
         listResponse.setSize(size);
         listResponse.setItemResponse(itemResponse);
         when(
                 context.getMessage(Mockito.anyString(),
                         Mockito.argThat(Matchers.<Object> hasItemInArray(is((Object) size))), Mockito.any(Locale.class)))
                 .thenReturn(size);
 
         publishManager.publishResults(Category.TALVIHAALARI, asList(listResponse));
 
         verify(photoManager).publishPhoto(
                 Mockito.argThat(Matchers.<PhotoData> hasProperty("description", containsString(size))));
     }
 
     @Test
     public void testDescriptionBrand() throws Exception {
         ListResponse listResponse = new ListResponse();
         listResponse.setSize("90");
         ItemResponse itemResponse = new ItemResponse();
         final String brand = "reima";
         listResponse.setBrand(brand);
         listResponse.setItemResponse(itemResponse);
         when(
                 context.getMessage(Mockito.anyString(),
                         Mockito.argThat(Matchers.<Object> hasItemInArray(is((Object) brand))),
                         Mockito.any(Locale.class))).thenReturn(brand);
 
         publishManager.publishResults(Category.TALVIHAALARI, asList(listResponse));
 
         verify(photoManager).publishPhoto(
                 Mockito.argThat(Matchers.<PhotoData> hasProperty("description", containsString(brand))));
     }
 
     @Test
     public void testDescriptionNewPrice() throws Exception {
         ListResponse listResponse = new ListResponse();
         listResponse.setSize("90");
         ItemResponse itemResponse = new ItemResponse();
         final String newPrice = "100";
         itemResponse.setNewPrice(newPrice);
         listResponse.setItemResponse(itemResponse);
         when(
                 context.getMessage(Mockito.anyString(),
                         Mockito.argThat(Matchers.<Object> hasItemInArray(is((Object) newPrice))),
                         Mockito.any(Locale.class))).thenReturn(newPrice);
 
         publishManager.publishResults(Category.TALVIHAALARI, asList(listResponse));
 
         verify(photoManager).publishPhoto(
                 Mockito.argThat(Matchers.<PhotoData> hasProperty("description", containsString(newPrice))));
     }
 
     @Test
     public void testGroupId() throws Exception {
         ListResponse listResponse = new ListResponse();
         listResponse.setSize("90");
         ItemResponse itemResponse = new ItemResponse();
         listResponse.setItemResponse(itemResponse);
 
         publishManager.publishResults(Category.TALVIHAALARI, asList(listResponse));
 
         verify(photoManager).publishPhoto(Mockito.argThat(Matchers.<PhotoData> hasProperty("groupId", notNullValue())));
     }
 
     @Test
     public void testAlbumId() throws Exception {
         ListResponse listResponse = new ListResponse();
         listResponse.setSize("90");
         ItemResponse itemResponse = new ItemResponse();
         listResponse.setItemResponse(itemResponse);
 
         publishManager.publishResults(Category.TALVIHAALARI, asList(listResponse));
 
         verify(photoManager).publishPhoto(Mockito.argThat(Matchers.<PhotoData> hasProperty("albumId", notNullValue())));
     }
 
     @Test
     public void testFileLocationResource() throws Exception {
         ListResponse listResponse = new ListResponse();
         listResponse.setSize("90");
         ItemResponse itemResponse = new ItemResponse();
         listResponse.setItemResponse(itemResponse);
 
         publishManager.publishResults(Category.TALVIHAALARI, asList(listResponse));
 
         verify(photoManager).publishPhoto(
                 Mockito.argThat(Matchers.<PhotoData> hasProperty("fileResource", notNullValue())));
     }
 }
