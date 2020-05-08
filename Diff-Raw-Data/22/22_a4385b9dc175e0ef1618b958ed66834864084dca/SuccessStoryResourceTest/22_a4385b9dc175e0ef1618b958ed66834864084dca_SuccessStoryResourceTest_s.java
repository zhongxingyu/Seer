 package org.bloodtorrent.resources;
 
 import org.bloodtorrent.IllegalDataException;
 import org.bloodtorrent.dto.SuccessStory;
 import org.bloodtorrent.repository.SuccessStoryRepository;
 import org.junit.Before;
import org.junit.Ignore;
 import org.junit.Test;
 
 import javax.validation.ConstraintViolation;
 import javax.validation.Validation;
 import javax.validation.Validator;
 import javax.validation.ValidatorFactory;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Set;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.core.IsCollectionContaining.hasItem;
 import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: sds
  * Date: 13. 3. 22
  * Time: 오전 11:19
  * To change this template use File | Settings | File Templates.
  */
 public class SuccessStoryResourceTest {
 
    private SuccessStoryResource resource;
     private SuccessStoryRepository repository;
     private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
     private final Validator validator = factory.getValidator();
 
     @Before
     public void setUpResource() {
        repository = mock(SuccessStoryRepository.class);
         resource = new SuccessStoryResource(repository);
     }
 
     private SuccessStory createNewSuccessStory() {
         SuccessStory story = new SuccessStory();
         story.setTitle("Sample Story");
         story.setStory("Hello, world!");
         return story;
     }
 
     @Test
     public void shouldBeShownWhenThereIsOneStory(){
         SuccessStory story = createNewSuccessStory();
         when(repository.list()).thenReturn(Arrays.asList(story));
 
         List<SuccessStory> stories = repository.list();
         assertThat(stories, hasItem(story));
 
         verify(repository).list();
     }
 
     @Test
    @Ignore
     public void shouldBeShownWhenThereAreTwoStories(){
         SuccessStory story1 = createNewSuccessStory();
         SuccessStory story2 = createNewSuccessStory();
         when(repository.list()).thenReturn(Arrays.asList(story1, story2));
 
         List<SuccessStory> stories = repository.list();
         assertThat(stories, hasItem(story1));
         assertThat(stories, hasItem(story2));
     }
 
     @Test
     public void shouldBeZeroSizeWhenThereIsNoStory() throws IllegalDataException {
         assertThat(resource.numberOfStories(), is(0));
     }
 
     @Test
     public void shouldBeOneSizeWhenThereIsOneStory() throws IllegalDataException {
         SuccessStory story = createNewSuccessStory();
         when(repository.list()).thenReturn(Arrays.asList(story));
         assertThat(resource.numberOfStories(), is(1));
     }
 
     @Test
     public void shouldBeTwoSizeWhenThereAreTwoStories() throws IllegalDataException {
         SuccessStory story1 = createNewSuccessStory();
         SuccessStory story2 = createNewSuccessStory();
         List<SuccessStory> list = new ArrayList<SuccessStory>();
         list.add(story1);
         list.add(story2);
         when(repository.list()).thenReturn(list);
 
         assertThat(resource.numberOfStories(), is(2));
     }
 
     @Test(expected = IllegalDataException.class)
     public void shouldThrowExceptionWhenThereAreFourStories() throws IllegalDataException {
         when(repository.list()).thenReturn(Arrays.asList(createNewSuccessStory(), createNewSuccessStory(), createNewSuccessStory(), createNewSuccessStory()));
 
         resource.numberOfStories();
     }
 
     @Test
     public void titleShouldNotBeEmpty() {
         SuccessStory story = createNewSuccessStory();
         setDummyString(story, "title", 0);
         Set<ConstraintViolation<SuccessStory>> constraintViolations = validator.validateProperty(story, "title");
         assertThat(constraintViolations.size(), is(1));
     }
 
     @Test
     public void descriptionShouldNotBeEmpty() {
         SuccessStory story = createNewSuccessStory();
         setDummyString(story, "story", 0);
         Set<ConstraintViolation<SuccessStory>> constraintViolations = validator.validateProperty(story, "story");
         assertThat(constraintViolations.size(), is(1));
     }
 
     private <T> void setDummyString(T t, String property, int num) {
         String dummyText = makeDummyString(num);
         invokeMethod(t, property, dummyText);
     }
 
     private <T> void setDummyNumericString(T t, String property, int num) {
         String dummyNumericText = makeDummyNumericString(num);
         invokeMethod(t, property, dummyNumericText);
     }
 
     private <T> void invokeMethod(T t, String property, String dummyText) {
         for (Method method : t.getClass().getMethods()) {
             String methodName = method.getName();
             if(methodName.startsWith("set") && methodName.toLowerCase().endsWith(property.toLowerCase())) {
                 try {
                     method.invoke(t, dummyText);
                 } catch (IllegalAccessException e) {
                     e.printStackTrace();
                 } catch (InvocationTargetException e) {
                     e.printStackTrace();
                 }
             }
         }
     }
 
     private String makeDummyString(int num, String dummyChar) {
         StringBuilder stringBuilder = new StringBuilder();
         for(int i = 0 ; i < num ; i++){
             stringBuilder.append(dummyChar);
         }
         return stringBuilder.toString();
     }
 
     private String makeDummyString(int num) {
         return makeDummyString(num, "*");
     }
 
     private String makeDummyNumericString(int num) {
         return makeDummyString(num, "1");
     }
 }
