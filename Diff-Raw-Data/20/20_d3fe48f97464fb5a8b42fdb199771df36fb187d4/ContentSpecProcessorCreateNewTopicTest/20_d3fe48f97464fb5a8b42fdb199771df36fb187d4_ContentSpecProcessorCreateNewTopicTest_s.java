 package org.jboss.pressgang.ccms.contentspec.processor;
 
 import static com.natpryce.makeiteasy.MakeItEasy.a;
 import static com.natpryce.makeiteasy.MakeItEasy.make;
 import static com.natpryce.makeiteasy.MakeItEasy.with;
 import static org.hamcrest.Matchers.containsString;
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.fail;
 import static org.mockito.Matchers.anyInt;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
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
 import org.jboss.pressgang.ccms.utils.constants.CommonConstants;
 import org.junit.Test;
 import org.mockito.Mock;
 
 public class ContentSpecProcessorCreateNewTopicTest extends ContentSpecProcessorTest {
     @Arbitrary Integer id;
     @Arbitrary String title;
     @Arbitrary String type;
     @Arbitrary String randomString;
     @ArbitraryString(type = StringType.ALPHANUMERIC) String username;
 
     @Mock TopicWrapper topicWrapper;
     @Mock PropertyTagWrapper cspIdPropertyTag;
     @Mock PropertyTagWrapper addedByPropertyTag;
     @Mock PropertyTagInTopicWrapper cspIdPropertyTagInTopic;
     @Mock PropertyTagInTopicWrapper addedByPropertyTagInTopic;
     @Mock CollectionWrapper<TagWrapper> tagCollection;
     @Mock UpdateableCollectionWrapper<PropertyTagInTopicWrapper> propertyTagCollection;
     @Mock CollectionWrapper<TopicSourceURLWrapper> topicSourceURLCollection;
     @Mock TagWrapper writerTag;
 
     @Test
     public void shouldCreateSpecTopic() {
         // Given a SpecTopic with just an id and title
         SpecTopic specTopic = make(a(SpecTopicMaker.SpecTopic, with(SpecTopicMaker.id, "N"), with(SpecTopicMaker.uniqueId, "L-N"),
                 with(SpecTopicMaker.title, title), with(SpecTopicMaker.type, type), with(SpecTopicMaker.assignedWriter, username),
                 with(SpecTopicMaker.description, randomString)));
         // Setup the basic details
         setupBaseTopicMocks();
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
     }
 
     @Test
     public void shouldCreateSpecTopicWithTags() {
         String tag1 = "Test";
         String tag2 = "Test2";
         final TagWrapper tag1Wrapper = mock(TagWrapper.class);
         final TagWrapper tag2Wrapper = mock(TagWrapper.class);
         final CollectionWrapper<TagWrapper> tag1Collection = makeTagCollection(tag1, tag1Wrapper);
         final CollectionWrapper<TagWrapper> tag2Collection = makeTagCollection(tag2, tag2Wrapper);
         // Given a list of tags
         final List<String> tags = Arrays.asList(tag1, tag2);
         // and a SpecTopic with just an id and title
         SpecTopic specTopic = make(a(SpecTopicMaker.SpecTopic, with(SpecTopicMaker.id, "N"), with(SpecTopicMaker.uniqueId, "L-N"),
                 with(SpecTopicMaker.title, title), with(SpecTopicMaker.type, type), with(SpecTopicMaker.assignedWriter, username),
                 with(SpecTopicMaker.description, randomString), with(SpecTopicMaker.tags, tags)));
         // Setup the basic details
         setupBaseTopicMocks();
         // and setup the basic valid mocks
         setupValidBaseTopicMocks();
         // and the tags exist
         when(tagProvider.getTagsByName(tag1)).thenReturn(tag1Collection);
         when(tagProvider.getTagsByName(tag2)).thenReturn(tag2Collection);
 
         TopicWrapper topic = null;
         try {
             topic = processor.createTopicEntity(providerFactory, specTopic);
         } catch (Exception e) {
             fail("Creating a topic should not have thrown an exception");
         }
 
         // Then the base topic should be valid
         verifyValidBaseTopic(topic);
         // and the tags were added
         verify(tagCollection, times(1)).addNewItem(tag1Wrapper);
         verify(tagCollection, times(1)).addNewItem(tag2Wrapper);
         // and the topic had the tags set
        verify(topic, times(1)).setTags(eq(tagCollection));
     }
 
     @Test
     public void shouldCreateSpecTopicWithSourceUrls() {
         String url1 = "http://www.example.com/";
         String url2 = "http://www.domain.com/";
         final TopicSourceURLWrapper urlWrapper = mock(TopicSourceURLWrapper.class);
         // Given a list of urls
         final List<String> urls = Arrays.asList(url1, url2);
         // and a SpecTopic with just an id and title
         SpecTopic specTopic = make(a(SpecTopicMaker.SpecTopic, with(SpecTopicMaker.id, "N"), with(SpecTopicMaker.uniqueId, "L-N"),
                 with(SpecTopicMaker.title, title), with(SpecTopicMaker.type, type), with(SpecTopicMaker.assignedWriter, username),
                 with(SpecTopicMaker.description, randomString), with(SpecTopicMaker.urls, urls)));
         // Setup the basic details
         setupBaseTopicMocks();
         // and setup the basic valid mocks
         setupValidBaseTopicMocks();
         // and creating a new source url is setup
         when(topicSourceURLProvider.newTopicSourceURL(eq(topicWrapper))).thenReturn(urlWrapper);
 
         TopicWrapper topic = null;
         try {
             topic = processor.createTopicEntity(providerFactory, specTopic);
         } catch (Exception e) {
             fail("Creating a topic should not have thrown an exception");
         }
 
         // Then the base topic should be valid
         verifyValidBaseTopic(topic);
         // and the urls were added
         verify(topicSourceURLCollection, times(2)).addNewItem(urlWrapper);
         // and the urls had the right data added
         verify(urlWrapper, times(1)).setUrl(url1);
         verify(urlWrapper, times(1)).setUrl(url2);
         // and the topic had the source urls set
         verify(topic, times(1)).setSourceURLs(eq(topicSourceURLCollection));
     }
 
     @Test
     public void shouldThrowExceptionWhenCreateSpecTopicWithInvalidType() {
         // Given a SpecTopic with just an id and title
         SpecTopic specTopic = make(a(SpecTopicMaker.SpecTopic, with(SpecTopicMaker.id, "N"), with(SpecTopicMaker.uniqueId, "L-N"),
                 with(SpecTopicMaker.title, title), with(SpecTopicMaker.type, type), with(SpecTopicMaker.assignedWriter, username),
                 with(SpecTopicMaker.description, randomString)));
         // Setup the basic details
         setupBaseTopicMocks();
         // and the tag provider won't return a type
         when(tagProvider.getTagsByName(eq(type))).thenReturn(null);
 
         TopicWrapper topic = null;
         try {
             topic = processor.createTopicEntity(providerFactory, specTopic);
             fail("Creating a topic should have thrown an exception");
         } catch (Exception e) {
             // Then there should be a log message about the error
             assertThat(logger.getLogMessages().size(), is(1));
             assertThat(logger.getLogMessages().get(0).toString(), containsString("Invalid Topic! Type doesn't exist."));
         }
     }
 
     @Test
     public void shouldThrowExceptionWhenCreateSpecTopicWhenTypeNotFound() {
         final CollectionWrapper<TagWrapper> types = mock(CollectionWrapper.class);
         // Given a SpecTopic with just an id and title
         SpecTopic specTopic = make(a(SpecTopicMaker.SpecTopic, with(SpecTopicMaker.id, "N"), with(SpecTopicMaker.uniqueId, "L-N"),
                 with(SpecTopicMaker.title, title), with(SpecTopicMaker.type, type), with(SpecTopicMaker.assignedWriter, username),
                 with(SpecTopicMaker.description, randomString)));
         // Setup the basic details
         setupBaseTopicMocks();
         // and the tag provider won't return a type
         when(tagProvider.getTagsByName(eq(type))).thenReturn(types);
         when(types.size()).thenReturn(0);
 
         TopicWrapper topic = null;
         try {
             topic = processor.createTopicEntity(providerFactory, specTopic);
             fail("Creating a topic should have thrown an exception");
         } catch (Exception e) {
             // Then there should be a log message about the error
             assertThat(logger.getLogMessages().size(), is(1));
             assertThat(logger.getLogMessages().get(0).toString(), containsString("Invalid Topic! Type doesn't exist."));
         }
     }
 
     protected void setupBaseTopicMocks() {
         // and the topic provider will return a new topic
         when(topicProvider.newTopic()).thenReturn(topicWrapper);
         // and the tag provider will return a new tag collection
         when(tagProvider.newTagCollection()).thenReturn(tagCollection);
         when(topicWrapper.getTags()).thenReturn(tagCollection);
         // and the property tag provider will return a new property tag collection
         when(propertyTagProvider.newPropertyTagInTopicCollection()).thenReturn(propertyTagCollection);
         when(topicWrapper.getProperties()).thenReturn(propertyTagCollection);
         // and the property tag provider will return a property tag
         when(propertyTagProvider.getPropertyTag(CSConstants.CSP_PROPERTY_ID)).thenReturn(cspIdPropertyTag);
         when(propertyTagProvider.getPropertyTag(CSConstants.ADDED_BY_PROPERTY_TAG_ID)).thenReturn(addedByPropertyTag);
         // and the topic source url provider will return a new topic source url collection
         when(topicSourceURLProvider.newTopicSourceURLCollection(eq(topicWrapper))).thenReturn(topicSourceURLCollection);
         when(topicWrapper.getSourceURLs()).thenReturn(topicSourceURLCollection);
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
         // and the topic has the title
         verify(topic, times(1)).setTitle(title);
         // and the id is null
         verify(topic, times(0)).setId(anyInt());
         // and the xml is an empty string
         verify(topic, times(1)).setXml("");
         // and the description was set
         verify(topic, times(1)).setDescription(randomString);
         // and the doctype is set
         verify(topic, times(1)).setXmlDoctype(CommonConstants.DOCBOOK_45);
         // and the locale is set
         verify(topic, times(1)).setLocale(CommonConstants.DEFAULT_LOCALE);
         // and the topic had the properties set
         verify(topic, times(1)).setProperties(eq(propertyTagCollection));
         // and the topic had the CSP property tag set
         verify(propertyTagCollection, times(1)).addNewItem(cspIdPropertyTagInTopic);
         verify(cspIdPropertyTagInTopic, times(1)).setValue("L-N");
         // and the topic had the AddedBy property tag set
         verify(propertyTagCollection, times(1)).addNewItem(addedByPropertyTagInTopic);
         verify(addedByPropertyTagInTopic, times(1)).setValue(username);
         // and that the topic has the assigned writer set
         verify(tagCollection, times(1)).addNewItem(writerTag);
     }
 }
