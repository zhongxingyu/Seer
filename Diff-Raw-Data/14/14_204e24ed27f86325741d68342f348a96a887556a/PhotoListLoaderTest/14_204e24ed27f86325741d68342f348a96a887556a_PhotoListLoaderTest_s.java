 package au.com.dius.resilience.test.unit.loader;
 
 import android.content.Context;
 import au.com.dius.resilience.loader.PhotoListLoader;
 import au.com.dius.resilience.model.Photo;
 import au.com.dius.resilience.persistence.repository.Repository;
 import au.com.dius.resilience.test.unit.utils.ResilienceTestRunner;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.sameInstance;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.when;
 
 @RunWith(ResilienceTestRunner.class)
 public class PhotoListLoaderTest {
 
   private static final String INCIDENT_ID = "100";
   @Mock
   private Context mockContext;
 
   @Mock
   private Repository mockRepository;
 
   @Mock
   private Photo mockPhoto;
 
   private PhotoListLoader listLoader;
 
 
   @Before
   public void setUp() throws NoSuchFieldException {
     MockitoAnnotations.initMocks(this);
 
     listLoader = new PhotoListLoader(mockContext, mockRepository, INCIDENT_ID);
   }
 
   @Test
   public void onBackgroudLoadShouldDelegateToRepository() {
    List<Photo> photos = new ArrayList<Photo>();
     when(mockRepository.findPhotoByIncident(INCIDENT_ID)).thenReturn(mockPhoto);
 
     final List<Photo> photoList = listLoader.loadInBackground();
    assertThat(photoList.size(), equalTo(photos.size()));
 
     assertThat(photoList.get(0), sameInstance(mockPhoto));
   }
 
   @Test
   public void onBackgroudLoadShouldHandleNullPhoto() {
     when(mockRepository.findPhotoByIncident(INCIDENT_ID)).thenReturn(null);
 
     final List<Photo> photoList = listLoader.loadInBackground();
     assertThat(photoList.size(), equalTo(0));
   }
 }
