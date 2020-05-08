 package org.jboss.pressgang.ccms.contentspec.processor;
 
 import static com.natpryce.makeiteasy.MakeItEasy.a;
 import static com.natpryce.makeiteasy.MakeItEasy.make;
 import static com.natpryce.makeiteasy.MakeItEasy.with;
 import static org.hamcrest.core.Is.is;
 import static org.hamcrest.core.StringContains.containsString;
 import static org.jboss.pressgang.ccms.contentspec.test.makers.shared.LevelMaker.tags;
 import static org.jboss.pressgang.ccms.contentspec.test.makers.shared.SpecTopicMaker.id;
 import static org.junit.Assert.assertThat;
 import static org.mockito.BDDMockito.given;
 import static org.mockito.Matchers.anyInt;
 import static org.mockito.Mockito.when;
 
 import java.util.Arrays;
 import java.util.List;
 
import net.sf.ipsedixit.annotation.Arbitrary;
 import net.sf.ipsedixit.annotation.ArbitraryString;
 import net.sf.ipsedixit.core.StringType;
 import org.jboss.pressgang.ccms.contentspec.Level;
 import org.jboss.pressgang.ccms.contentspec.SpecTopic;
 import org.jboss.pressgang.ccms.contentspec.test.makers.shared.LevelMaker;
 import org.jboss.pressgang.ccms.contentspec.test.makers.shared.SpecTopicMaker;
 import org.jboss.pressgang.ccms.provider.TagProvider;
 import org.jboss.pressgang.ccms.provider.TopicProvider;
 import org.jboss.pressgang.ccms.wrapper.TagWrapper;
 import org.jboss.pressgang.ccms.wrapper.TopicWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.CollectionWrapper;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.xml.sax.SAXException;
 
 /**
  * @author kamiller@redhat.com (Katie Miller)
  */
 public class ContentSpecValidatorPostValidateLevelTest extends ContentSpecValidatorTest {
    @Arbitrary Integer rev;
     @ArbitraryString(type = StringType.ALPHA) String tagname;
     @ArbitraryString String title;
     @Mock TagProvider tagProvider;
     @Mock TopicProvider topicProvider;
     @Mock CollectionWrapper<TagWrapper> tagWrapperCollection;
     @Mock List<TagWrapper> tagWrapperList;
     @Mock TagWrapper tagWrapper;
     @Mock TopicWrapper topicWrapper;
 
     @Before
     public void setUp() {
         when(dataProviderFactory.getProvider(TagProvider.class)).thenReturn(tagProvider);
         when(dataProviderFactory.getProvider(TopicProvider.class)).thenReturn(topicProvider);
         super.setUp();
     }
 
     @Test
     public void shouldSucceedIfLevelValid() {
         // Given a valid level
         Level level = make(a(LevelMaker.Level));
 
         // When the level is postvalidated
         boolean result = validator.postValidateLevel(level);
 
         // Then the result should be success
         assertThat(result, is(true));
         // And no error messages should be output
         assertThat(logger.getLogMessages().toString(), containsString("[]"));
     }
 
     @Test
     public void shouldSucceedIfValidLevelAndHasValidChildLevel() {
         // Given a valid level with a valid child level
         Level level = make(a(LevelMaker.Level));
         level.appendChild(make(a(LevelMaker.Level)));
 
         // When the level is postvalidated
         boolean result = validator.postValidateLevel(level);
 
         // Then the result should be success
         assertThat(result, is(true));
         // And no error messages should be output
         assertThat(logger.getLogMessages().toString(), containsString("[]"));
     }
 
     @Test
     public void shouldSucceedIfValidLevelAndHasValidChildSpecTopic() throws SAXException {
         // Given a valid level with a valid child spec topic
         Level level = make(a(LevelMaker.Level));
        level.appendChild(make(a(SpecTopicMaker.SpecTopic, with(SpecTopicMaker.title, title), with(SpecTopicMaker.revision, rev))));
         // And the topic exists
         given(topicProvider.getTopic(anyInt(), anyInt())).willReturn(topicWrapper);
         given(topicWrapper.getTitle()).willReturn(title);
        given(topicWrapper.getRevision()).willReturn(rev);
 
         // When the level is postvalidated
         boolean result = validator.postValidateLevel(level);
 
         // Then the result should be success
         assertThat(result, is(true));
         // And no error messages should be output
         assertThat(logger.getLogMessages().toString(), containsString("[]"));
     }
 
     @Test
     public void shouldLogErrorAndFailIfTopicTagInvalid() {
         // Given a level with an invalid topic tag
         Level level = createLevelWithInvalidTag();
 
         // When the level is postvalidated
         boolean result = validator.postValidateLevel(level);
 
         // Then the result should be a failure
         assertThat(result, is(false));
         // And an error message should be output
         assertThat(logger.getLogMessages().toString(),
                 containsString("Invalid Content Specification! Tag \"" + tagname + "\" doesn't exist."));
     }
 
     @Test
     public void shouldLogErrorAndFailIfChildLevelInvalid() {
         // Given a valid level with an invalid child level
         Level level = make(a(LevelMaker.Level));
         level.appendChild(createLevelWithInvalidTag());
 
         // When the level is postvalidated
         boolean result = validator.postValidateLevel(level);
 
         // Then the result should be a failure
         assertThat(result, is(false));
         // And an error message should be output
         assertThat(logger.getLogMessages().toString(),
                 containsString("Invalid Content Specification! Tag \"" + tagname + "\" doesn't exist."));
     }
 
     @Test
     public void shouldLogErrorAndFailIfChildSpecTopicInvalid() {
         // Given a valid level with an invalid child spec topic that is a new topic
         Level level = make(a(LevelMaker.Level));
         SpecTopic specTopic = make(a(SpecTopicMaker.SpecTopic, with(id, "N123")));
         given(tagProvider.getTagByName(specTopic.getType())).willReturn(null);
         level.appendChild(specTopic);
         // And that the assigned writer is valid
         given(tagProvider.getTagByName(specTopic.getAssignedWriter(false))).willReturn(tagWrapper);
 
         // When the level is postvalidated
         boolean result = validator.postValidateLevel(level);
 
         // Then the result should be a failure
         assertThat(result, is(false));
         // And an error message should be output
         assertThat(logger.getLogMessages().toString(), containsString("Invalid Topic! Type doesn't exist."));
     }
 
     private Level createLevelWithInvalidTag() {
         Level level = make(a(LevelMaker.Level, with(tags, Arrays.asList(tagname))));
         given(tagProvider.getTagByName(tagname)).willReturn(null);
         return level;
     }
 }
