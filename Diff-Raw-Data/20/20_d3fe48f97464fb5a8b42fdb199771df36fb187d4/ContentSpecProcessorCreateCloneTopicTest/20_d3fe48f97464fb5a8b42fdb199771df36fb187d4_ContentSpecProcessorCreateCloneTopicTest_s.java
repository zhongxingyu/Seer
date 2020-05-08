 package org.jboss.pressgang.ccms.contentspec.processor;
 
 import static com.natpryce.makeiteasy.MakeItEasy.a;
 import static com.natpryce.makeiteasy.MakeItEasy.make;
 import static com.natpryce.makeiteasy.MakeItEasy.with;
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNotSame;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertSame;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.anyInt;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import net.sf.ipsedixit.annotation.Arbitrary;
 import net.sf.ipsedixit.annotation.ArbitraryString;
 import net.sf.ipsedixit.core.StringType;
 import org.jboss.pressgang.ccms.contentspec.SpecTopic;
 import org.jboss.pressgang.ccms.contentspec.constants.CSConstants;
 import org.jboss.pressgang.ccms.contentspec.test.makers.shared.SpecTopicMaker;
 import org.jboss.pressgang.ccms.contentspec.wrapper.PropertyTagInTopicWrapper;
 import org.jboss.pressgang.ccms.contentspec.wrapper.PropertyTagWrapper;
 import org.jboss.pressgang.ccms.contentspec.wrapper.TagWrapper;
 import org.jboss.pressgang.ccms.contentspec.wrapper.TopicSourceURLWrapper;
 import org.jboss.pressgang.ccms.contentspec.wrapper.TopicWrapper;
 import org.jboss.pressgang.ccms.contentspec.wrapper.collection.CollectionWrapper;
 import org.jboss.pressgang.ccms.contentspec.wrapper.collection.UpdateableCollectionWrapper;
 import org.jboss.pressgang.ccms.contentspec.wrapper.mocks.CollectionWrapperMock;
 import org.jboss.pressgang.ccms.contentspec.wrapper.mocks.TopicWrapperMock;
 import org.jboss.pressgang.ccms.contentspec.wrapper.mocks.UpdateableCollectionWrapperMock;
 import org.jboss.pressgang.ccms.utils.constants.CommonConstants;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 
 public class ContentSpecProcessorCreateCloneTopicTest extends ContentSpecProcessorTest {
     @Arbitrary Integer id;
     @Arbitrary Integer revision;
     @Arbitrary String title;
     @Arbitrary String type;
     @Arbitrary String randomString;
     @Arbitrary String locale;
     @ArbitraryString(type = StringType.ALPHANUMERIC) String username;
 
     @Mock PropertyTagWrapper cspIdPropertyTag;
     @Mock PropertyTagWrapper addedByPropertyTag;
     @Mock PropertyTagInTopicWrapper cspIdPropertyTagInTopic;
     @Mock PropertyTagInTopicWrapper addedByPropertyTagInTopic;
     @Mock PropertyTagInTopicWrapper propertyTagInTopic;
     @Mock TagWrapper writerTag;
 
     // Existing topic mocks
     @Mock TopicWrapper topicWrapper;
     @Mock PropertyTagInTopicWrapper existingCspIdPropertyTagInTopic;
     @Mock PropertyTagInTopicWrapper existingAddedByPropertyTagInTopic;
     @Mock UpdateableCollectionWrapper<PropertyTagInTopicWrapper> existingTopicProperties;
     @Mock CollectionWrapper<TopicSourceURLWrapper> existingTopicSourceURLCollection;
     @Mock CollectionWrapper<TagWrapper> existingTagCollection;
 
     private TopicWrapper newTopicWrapper;
     private CollectionWrapper<TagWrapper> tagCollection;
     private UpdateableCollectionWrapper<PropertyTagInTopicWrapper> propertyTagCollection;
     private CollectionWrapper<TopicSourceURLWrapper> topicSourceURLCollection;
     private List<PropertyTagInTopicWrapper> existingProperties;
 
     @Before
     public void setUpEntities() {
         newTopicWrapper = new TopicWrapperMock();
         tagCollection = new CollectionWrapperMock<TagWrapper>();
         propertyTagCollection = new UpdateableCollectionWrapperMock<PropertyTagInTopicWrapper>();
         topicSourceURLCollection = new CollectionWrapperMock<TopicSourceURLWrapper>();
         existingProperties = new ArrayList<PropertyTagInTopicWrapper>();
     }
 
     @Test
     public void shouldCreateSpecTopic() {
         // Given a SpecTopic with just an id and title
         SpecTopic specTopic = make(a(SpecTopicMaker.SpecTopic, with(SpecTopicMaker.id, "C1"), with(SpecTopicMaker.uniqueId, "L-C1"),
                 with(SpecTopicMaker.title, title), with(SpecTopicMaker.type, type), with(SpecTopicMaker.assignedWriter, username),
                 with(SpecTopicMaker.description, randomString)));
         // Setup the basic details
         setupBaseTopicMocks();
         // Setup the existing topic mocks
         setupExistingTopicMocks();
         // and setup the basic valid mocks
         setupValidBaseTopicMocks();
 
         TopicWrapper topic = null;
         try {
             topic = processor.createTopicEntity(providerFactory, specTopic);
         } catch (Exception e) {
             e.printStackTrace();
             fail("Creating a topic should not have thrown an exception");
         }
 
         // Then the base topic should be valid
         verifyValidBaseTopic(topic);
         verifyUnchangedOriginalTopic();
         // and the property should have added
         verifyValidBaseTopicNewProperties(topic);
     }
 
     @Test
     public void shouldCreateSpecTopicAndSetNewPropertyWhenItAlreadyExistsInOriginalTopic() {
         // Given a SpecTopic and we add a tag just to make a change
         SpecTopic specTopic = make(a(SpecTopicMaker.SpecTopic, with(SpecTopicMaker.id, "C1"), with(SpecTopicMaker.uniqueId, "L-C1"),
                 with(SpecTopicMaker.title, title), with(SpecTopicMaker.type, type), with(SpecTopicMaker.assignedWriter, username),
                 with(SpecTopicMaker.description, randomString), with(SpecTopicMaker.revision, (Integer) null)));
         // Setup the basic details
         setupBaseTopicMocks();
         // Setup the existing topic mocks
         setupExistingTopicMocks();
         // and setup the basic valid mocks
         setupValidBaseTopicMocks();
         // and the csp id already exists
         existingProperties.add(existingCspIdPropertyTagInTopic);
         when(existingCspIdPropertyTagInTopic.getId()).thenReturn(CSConstants.CSP_PROPERTY_ID);
 
         TopicWrapper topic = null;
         try {
             topic = processor.createTopicEntity(providerFactory, specTopic);
         } catch (Exception e) {
             e.printStackTrace();
             fail("Creating a topic should not have thrown an exception");
         }
 
         // Then the base topic should be valid
         verifyValidBaseTopic(topic);
         verifyUnchangedOriginalTopic();
         // and the property should have added
         verifyValidBaseTopicNewProperties(topic);
     }
 
     @Test
     public void shouldCreateSpecTopicWithNewTags() {
         String tag1 = "Test";
         String tag2 = "Test2";
         final TagWrapper tag1Wrapper = mock(TagWrapper.class);
         final TagWrapper tag2Wrapper = mock(TagWrapper.class);
         final CollectionWrapper<TagWrapper> tag1Collection = makeTagCollection(tag1, tag1Wrapper);
         final CollectionWrapper<TagWrapper> tag2Collection = makeTagCollection(tag2, tag2Wrapper);
         // Given a list of tags
         final List<String> tags = Arrays.asList(tag1, tag2);
         // and a SpecTopic
         SpecTopic specTopic = make(a(SpecTopicMaker.SpecTopic, with(SpecTopicMaker.id, "C1"), with(SpecTopicMaker.uniqueId, "L-C1"),
                 with(SpecTopicMaker.title, title), with(SpecTopicMaker.type, type), with(SpecTopicMaker.assignedWriter, username),
                 with(SpecTopicMaker.description, randomString), with(SpecTopicMaker.tags, tags),
                 with(SpecTopicMaker.revision, (Integer) null)));
         // Setup the basic details
         setupBaseTopicMocks();
         // Setup the existing topic mocks
         setupExistingTopicMocks();
         // and setup the basic valid mocks
         setupValidBaseTopicMocks();
         // and the tags exist
         when(tagProvider.getTagsByName(tag1)).thenReturn(tag1Collection);
         when(tag1Wrapper.getId()).thenReturn(1);
         when(tagProvider.getTagsByName(tag2)).thenReturn(tag2Collection);
         when(tag2Wrapper.getId()).thenReturn(2);
 
         TopicWrapper topic = null;
         try {
             topic = processor.createTopicEntity(providerFactory, specTopic);
         } catch (Exception e) {
             fail("Creating a topic should not have thrown an exception");
         }
 
         // Then the base topic should be valid
         verifyValidBaseTopic(topic);
         verifyValidBaseTopicNewProperties(topic);
         verifyUnchangedOriginalTopic();
         // and the tags were added
         assertTrue(tagCollection.getAddItems().contains(tag1Wrapper));
         assertTrue(tagCollection.getAddItems().contains(tag2Wrapper));
         // and only new tags were set, as clones shouldn't have existing tags
         assertThat(tagCollection.getItems().size(), is(3));
         assertThat(tagCollection.getUnchangedItems().size(), is(0));
         assertThat(tagCollection.getRemoveItems().size(), is(0));
     }
 
     @Test
     public void shouldCreateSpecTopicWithNewAndExistingTags() {
         String tag1 = "Test";
         String tag2 = "Test2";
         final TagWrapper tag1Wrapper = mock(TagWrapper.class);
         final TagWrapper tag2Wrapper = mock(TagWrapper.class);
         final CollectionWrapper<TagWrapper> tag1Collection = makeTagCollection(tag1, tag1Wrapper);
         final CollectionWrapper<TagWrapper> tag2Collection = makeTagCollection(tag2, tag2Wrapper);
         final TagWrapper existingTagWrapper = mock(TagWrapper.class);
         // Given a list of tags
         final List<String> tags = Arrays.asList(tag1, tag2);
         // and a SpecTopic
         SpecTopic specTopic = make(a(SpecTopicMaker.SpecTopic, with(SpecTopicMaker.id, "C1"), with(SpecTopicMaker.uniqueId, "L-C1"),
                 with(SpecTopicMaker.title, title), with(SpecTopicMaker.type, type), with(SpecTopicMaker.assignedWriter, username),
                 with(SpecTopicMaker.description, randomString), with(SpecTopicMaker.tags, tags),
                 with(SpecTopicMaker.revision, (Integer) null)));
         // Setup the basic details
         setupBaseTopicMocks();
         // Setup the existing topic mocks
         setupExistingTopicMocks();
         // and setup the basic valid mocks
         setupValidBaseTopicMocks();
         // and the tags exist
         when(tagProvider.getTagsByName(tag1)).thenReturn(tag1Collection);
         when(tag1Wrapper.getId()).thenReturn(1);
         when(tagProvider.getTagsByName(tag2)).thenReturn(tag2Collection);
         when(tag2Wrapper.getId()).thenReturn(2);
         // and the topic already has some tags
         when(existingTagCollection.getItems()).thenReturn(Arrays.asList(existingTagWrapper));
         when(existingTagWrapper.getId()).thenReturn(3);
 
         TopicWrapper topic = null;
         try {
             topic = processor.createTopicEntity(providerFactory, specTopic);
         } catch (Exception e) {
             fail("Creating a topic should not have thrown an exception");
         }
 
         // Then the base topic should be valid
         verifyValidBaseTopic(topic);
         verifyValidBaseTopicNewProperties(topic);
         verifyUnchangedOriginalTopic();
         // and the tags were added
         assertTrue(tagCollection.getAddItems().contains(tag1Wrapper));
         assertTrue(tagCollection.getAddItems().contains(tag2Wrapper));
         // and that the original tag was added as well
         assertTrue(tagCollection.getAddItems().contains(existingTagWrapper));
         // and only new tags were set, as clones shouldn't have existing tags
         assertThat(tagCollection.getItems().size(), is(4));
         assertThat(tagCollection.getUnchangedItems().size(), is(0));
         assertThat(tagCollection.getRemoveItems().size(), is(0));
     }
 
     @Test
     public void shouldCreateSpecTopicWithExistingTagsAndAddTagThatAlreadyExists() {
         String tag1 = "Test";
         String tag2 = "Test2";
         final TagWrapper tag1Wrapper = mock(TagWrapper.class);
         final TagWrapper tag2Wrapper = mock(TagWrapper.class);
         final CollectionWrapper<TagWrapper> tag1Collection = makeTagCollection(tag1, tag1Wrapper);
         final CollectionWrapper<TagWrapper> tag2Collection = makeTagCollection(tag2, tag2Wrapper);
         // Given a list of tags
         final List<String> tags = Arrays.asList(tag1, tag2);
         // and a SpecTopic
         SpecTopic specTopic = make(a(SpecTopicMaker.SpecTopic, with(SpecTopicMaker.id, "C1"), with(SpecTopicMaker.uniqueId, "L-C1"),
                 with(SpecTopicMaker.title, title), with(SpecTopicMaker.type, type), with(SpecTopicMaker.assignedWriter, username),
                 with(SpecTopicMaker.description, randomString), with(SpecTopicMaker.tags, tags),
                 with(SpecTopicMaker.revision, (Integer) null)));
         // Setup the basic details
         setupBaseTopicMocks();
         // Setup the existing topic mocks
         setupExistingTopicMocks();
         // and setup the basic valid mocks
         setupValidBaseTopicMocks();
         // and the tags exist
         when(tagProvider.getTagsByName(tag1)).thenReturn(tag1Collection);
         when(tag1Wrapper.getId()).thenReturn(1);
         when(tagProvider.getTagsByName(tag2)).thenReturn(tag2Collection);
         when(tag2Wrapper.getId()).thenReturn(2);
         // and the topic already has the tag1Wrapper in it's collection
         when(existingTagCollection.getItems()).thenReturn(Arrays.asList(tag1Wrapper));
 
         TopicWrapper topic = null;
         try {
             topic = processor.createTopicEntity(providerFactory, specTopic);
         } catch (Exception e) {
             fail("Creating a topic should not have thrown an exception");
         }
 
         // Then the base topic should be valid
         verifyValidBaseTopic(topic);
         verifyValidBaseTopicNewProperties(topic);
         verifyUnchangedOriginalTopic();
         // and the new tags were added
         assertTrue(tagCollection.getAddItems().contains(tag2Wrapper));
         // and that the original tag was added as well
         assertTrue(tagCollection.getAddItems().contains(tag1Wrapper));
         // and only new tags were set, as clones shouldn't have existing tags
         assertThat(tagCollection.getItems().size(), is(3));
         assertThat(tagCollection.getUnchangedItems().size(), is(0));
         assertThat(tagCollection.getRemoveItems().size(), is(0));
     }
 
     @Test
     public void shouldCreateSpecTopicWithExistingTagsAndAllAddTagsAlreadyExists() {
         String tag1 = "Test";
         String tag2 = "Test2";
         final TagWrapper tag1Wrapper = mock(TagWrapper.class);
         final TagWrapper tag2Wrapper = mock(TagWrapper.class);
         final CollectionWrapper<TagWrapper> tag1Collection = makeTagCollection(tag1, tag1Wrapper);
         final CollectionWrapper<TagWrapper> tag2Collection = makeTagCollection(tag2, tag2Wrapper);
         // Given a list of tags
         final List<String> tags = Arrays.asList(tag1, tag2);
         // and a SpecTopic
         SpecTopic specTopic = make(a(SpecTopicMaker.SpecTopic, with(SpecTopicMaker.id, "C1"), with(SpecTopicMaker.uniqueId, "L-C1"),
                 with(SpecTopicMaker.title, title), with(SpecTopicMaker.type, type), with(SpecTopicMaker.assignedWriter, username),
                 with(SpecTopicMaker.description, randomString), with(SpecTopicMaker.tags, tags),
                 with(SpecTopicMaker.revision, (Integer) null)));
         // Setup the basic details
         setupBaseTopicMocks();
         // Setup the existing topic mocks
         setupExistingTopicMocks();
         // and setup the basic valid mocks
         setupValidBaseTopicMocks();
         // and the tags exist
         when(tagProvider.getTagsByName(tag1)).thenReturn(tag1Collection);
         when(tag1Wrapper.getId()).thenReturn(1);
         when(tagProvider.getTagsByName(tag2)).thenReturn(tag2Collection);
         when(tag2Wrapper.getId()).thenReturn(2);
         // and the topic already has the tag1Wrapper and tag2wrapper in it's collection
         when(existingTagCollection.getItems()).thenReturn(Arrays.asList(tag1Wrapper, tag2Wrapper));
 
         TopicWrapper topic = null;
         try {
             topic = processor.createTopicEntity(providerFactory, specTopic);
         } catch (Exception e) {
             fail("Creating a topic should not have thrown an exception");
         }
 
         // Then the base topic should be valid
         verifyValidBaseTopic(topic);
         verifyValidBaseTopicNewProperties(topic);
         verifyUnchangedOriginalTopic();
         // and the tags should be the same as the original
         assertThat(tagCollection.size(), is(3));
         // and the tags exist in the new state in the collection
         assertTrue(tagCollection.getAddItems().contains(tag1Wrapper));
         assertTrue(tagCollection.getAddItems().contains(tag2Wrapper));
         // and only new tags were set, as clones shouldn't have existing tags
         assertThat(tagCollection.getUnchangedItems().size(), is(0));
         assertThat(tagCollection.getRemoveItems().size(), is(0));
     }
 
     @Test
     public void shouldCreateSpecTopicWithWriterThatAlreadyExists() {
         String writerName = randomString;
         final TagWrapper writerTagWrapper = mock(TagWrapper.class);
 
         // and a SpecTopic
         SpecTopic specTopic = make(a(SpecTopicMaker.SpecTopic, with(SpecTopicMaker.id, "C1"), with(SpecTopicMaker.uniqueId, "L-C1"),
                 with(SpecTopicMaker.title, title), with(SpecTopicMaker.type, type), with(SpecTopicMaker.assignedWriter, username),
                 with(SpecTopicMaker.description, randomString), with(SpecTopicMaker.revision, (Integer) null)));
         // Setup the basic details
         setupBaseTopicMocks();
         // Setup the existing topic mocks
         setupExistingTopicMocks();
         // and setup the basic valid mocks
         setupValidBaseTopicMocks();
         // and a writer tag already exists
         when(writerTagWrapper.getId()).thenReturn(id);
         when(writerTag.getId()).thenReturn(id);
         // and the existing writer tag is in the writer category
         when(writerTagWrapper.containedInCategory(CSConstants.WRITER_CATEGORY_ID)).thenReturn(true);
         // and the topic already has the writerTag in it's collection
         when(existingTagCollection.getItems()).thenReturn(Arrays.asList(writerTagWrapper));
 
         TopicWrapper topic = null;
         try {
             topic = processor.createTopicEntity(providerFactory, specTopic);
         } catch (Exception e) {
             fail("Creating a topic should not have thrown an exception");
         }
 
         // Then the base topic should be valid
         verifyValidBaseTopic(topic);
         verifyValidBaseTopicNewProperties(topic);
         verifyUnchangedOriginalTopic();
         // and the old writer tag isn't in the collection
         assertFalse(tagCollection.getItems().contains(writerTagWrapper));
         // and that the new writer tag was added
         assertTrue(tagCollection.getAddItems().contains(writerTag));
         // and only new tags were set, as clones shouldn't have existing tags
         assertThat(tagCollection.getItems().size(), is(1));
         assertThat(tagCollection.getUnchangedItems().size(), is(0));
         assertThat(tagCollection.getRemoveItems().size(), is(0));
     }
 
     @Test
     public void shouldCreateSpecTopicWithSourceUrls() {
         String url1 = "http://www.example.com/";
         String url2 = "http://www.domain.com/";
         final TopicSourceURLWrapper urlWrapper = mock(TopicSourceURLWrapper.class);
         // Given a list of urls
         final List<String> urls = Arrays.asList(url1, url2);
         // and a SpecTopic with just an id and title
         SpecTopic specTopic = make(a(SpecTopicMaker.SpecTopic, with(SpecTopicMaker.id, "C1"), with(SpecTopicMaker.uniqueId, "L-C1"),
                 with(SpecTopicMaker.title, title), with(SpecTopicMaker.type, type), with(SpecTopicMaker.assignedWriter, username),
                 with(SpecTopicMaker.description, randomString), with(SpecTopicMaker.urls, urls)));
         // Setup the basic details
         setupBaseTopicMocks();
         // Setup the existing topic mocks
         setupExistingTopicMocks();
         // and setup the basic valid mocks
         setupValidBaseTopicMocks();
         // and creating a new source url is setup
         when(topicSourceURLProvider.newTopicSourceURL(eq(newTopicWrapper))).thenReturn(urlWrapper);
 
         TopicWrapper topic = null;
         try {
             topic = processor.createTopicEntity(providerFactory, specTopic);
         } catch (Exception e) {
             e.printStackTrace();
             fail("Creating a topic should not have thrown an exception");
         }
 
         // Then the base topic should be valid
         verifyValidBaseTopic(topic);
         verifyValidBaseTopicNewProperties(topic);
         verifyUnchangedOriginalTopic();
         // and the source url collection will have one object. It should have two url's but since it holds a set and we only can mock one
         // url than the collection will only have the one object.
         assertThat(topicSourceURLCollection.size(), is(1));
         // and the tags exist in the new state in the collection
         assertTrue(topicSourceURLCollection.getAddItems().contains(urlWrapper));
         // and only new source urls were set, as clones shouldn't have existing tags
         assertThat(topicSourceURLCollection.getRemoveItems().size(), is(0));
         assertThat(topicSourceURLCollection.getUnchangedItems().size(), is(0));
     }
 
     protected void setupExistingTopicMocks() {
         // and the topic has an id
         when(topicWrapper.getId()).thenReturn(id);
         // and the topic has a title
         when(topicWrapper.getTitle()).thenReturn(title);
         // and the topic has xml
         when(topicWrapper.getXml()).thenReturn(randomString);
         // and the topic has a doctype
         when(topicWrapper.getXmlDoctype()).thenReturn(CommonConstants.DOCBOOK_45);
         // and the topic has a locale
         when(topicWrapper.getLocale()).thenReturn(locale);
         // and the topic provider will return an existing
         when(topicProvider.getTopic(anyInt(), anyInt())).thenReturn(topicWrapper);
         // and the tag provider will return a new tag collection
         when(topicWrapper.getTags()).thenReturn(existingTagCollection);
         // and the property tag provider will return a new property tag collection
         when(topicWrapper.getProperties()).thenReturn(existingTopicProperties);
         // and the topic source url provider will return a new topic source url collection
         when(topicWrapper.getSourceURLs()).thenReturn(existingTopicSourceURLCollection);
         // and the property tag collection already has the added by tag
         existingProperties.add(existingAddedByPropertyTagInTopic);
         when(existingTopicProperties.getItems()).thenReturn(existingProperties);
         when(existingAddedByPropertyTagInTopic.getId()).thenReturn(CSConstants.ADDED_BY_PROPERTY_TAG_ID);
         when(existingAddedByPropertyTagInTopic.getValue()).thenReturn(username);
     }
 
     protected void setupBaseTopicMocks() {
         // and the topic provider will return a new topic
         when(topicProvider.newTopic()).thenReturn(newTopicWrapper);
         // and the tag provider will return a new tag collection
         when(tagProvider.newTagCollection()).thenReturn(tagCollection);
         newTopicWrapper.setTags(null);
         // and the property tag provider will return a new property tag collection
         when(propertyTagProvider.newPropertyTagInTopicCollection()).thenReturn(propertyTagCollection);
         newTopicWrapper.setProperties(null);
         // and the property tag provider will return a property tag
         when(propertyTagProvider.getPropertyTag(CSConstants.CSP_PROPERTY_ID)).thenReturn(cspIdPropertyTag);
         when(propertyTagProvider.getPropertyTag(CSConstants.ADDED_BY_PROPERTY_TAG_ID)).thenReturn(addedByPropertyTag);
         // and the topic source url provider will return a new topic source url collection
         when(topicSourceURLProvider.newTopicSourceURLCollection(eq(newTopicWrapper))).thenReturn(topicSourceURLCollection);
         newTopicWrapper.setSourceURLs(null);
     }
 
     protected void setupValidBaseTopicMocks() {
         final CollectionWrapper<TagWrapper> typeCollection = makeTagCollection(type);
         final CollectionWrapper<TagWrapper> writerCollection = makeTagCollection(username, writerTag);
         // and the tag provider returns a type
         when(tagProvider.getTagsByName(eq(type))).thenReturn(typeCollection);
         // and the tag provider returns an assigned writer
         when(tagProvider.getTagsByName(eq(username))).thenReturn(writerCollection);
         // and the property tag provider creates a new property tag
         when(propertyTagProvider.newPropertyTagInTopic(eq(cspIdPropertyTag))).thenReturn(cspIdPropertyTagInTopic);
         when(propertyTagProvider.newPropertyTagInTopic(eq(addedByPropertyTag))).thenReturn(addedByPropertyTagInTopic);
     }
 
     protected void verifyValidBaseTopic(final TopicWrapper topic) {
         // Then check the topic isn't null
         assertNotNull(topic);
         // and the topic is the mocked topic
         assertNotSame(topic, is(topicWrapper));
         // and the id was set to null
         assertNull(topic.getId());
         // and the topic title was cloned
         assertThat(topic.getTitle(), is(title));
         // and the topic xml was cloned
         assertThat(topic.getXml(), is(randomString));
         // and the topic doctype was cloned
         assertThat(topic.getXmlDoctype(), is(CommonConstants.DOCBOOK_45));
         // and the locale was cloned
         assertThat(topic.getLocale(), is(locale));
         // check that the added by property was not copied across
         assertFalse(propertyTagCollection.getAddItems().contains(existingAddedByPropertyTagInTopic));
         // check that the new added by property was set
         assertTrue(propertyTagCollection.getAddItems().contains(addedByPropertyTagInTopic));
         // and that the topic has the assigned writer set
         assertTrue(tagCollection.getAddItems().contains(writerTag));
     }
 
     protected void verifyValidBaseTopicNewProperties(final TopicWrapper topic) {
         // and the topic had the properties set
         assertSame(topic.getProperties(), propertyTagCollection);
         // and the topic had the CSP property tag set
         verify(cspIdPropertyTagInTopic, times(1)).setValue("L-C1");
         assertTrue(propertyTagCollection.getAddItems().contains(cspIdPropertyTagInTopic));
     }
 
     protected void verifyUnchangedOriginalTopic() {
         // and the topic title was not changed
         verify(topicWrapper, times(0)).setTitle(anyString());
         // and the id was not changed
         verify(topicWrapper, times(0)).setId(anyInt());
         // and the xml was not changed
         verify(topicWrapper, times(0)).setXml(anyString());
         // and the description was not changed
         verify(topicWrapper, times(0)).setDescription(anyString());
         // and the doctype was not changed
         verify(topicWrapper, times(0)).setXmlDoctype(anyInt());
         // and the locale was not changed
         verify(topicWrapper, times(0)).setLocale(anyString());
         // and the added by value hasn't changed
         verify(existingAddedByPropertyTagInTopic, times(0)).setValue(anyString());
         // and the csp id value hasn't changed
         verify(existingCspIdPropertyTagInTopic, times(0)).setValue(anyString());
         // and the original property tag collection wasn't touched
         verify(existingTopicProperties, times(0)).addItem(any(PropertyTagInTopicWrapper.class));
         verify(existingTopicProperties, times(0)).addNewItem(any(PropertyTagInTopicWrapper.class));
         verify(existingTopicProperties, times(0)).addRemoveItem(any(PropertyTagInTopicWrapper.class));
         verify(existingTopicProperties, times(0)).addUpdateItem(any(PropertyTagInTopicWrapper.class));
         // and the original tag collection wasn't touched
         verify(existingTagCollection, times(0)).addItem(any(TagWrapper.class));
         verify(existingTagCollection, times(0)).addNewItem(any(TagWrapper.class));
         verify(existingTagCollection, times(0)).addRemoveItem(any(TagWrapper.class));
         // and the original topic source url collection wasn't touched
         verify(existingTopicSourceURLCollection, times(0)).addItem(any(TopicSourceURLWrapper.class));
         verify(existingTopicSourceURLCollection, times(0)).addNewItem(any(TopicSourceURLWrapper.class));
         verify(existingTopicSourceURLCollection, times(0)).addRemoveItem(any(TopicSourceURLWrapper.class));
     }
 }
