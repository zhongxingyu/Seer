 package org.jboss.pressgang.ccms.contentspec.processor;
 
 import static com.natpryce.makeiteasy.MakeItEasy.a;
 import static com.natpryce.makeiteasy.MakeItEasy.make;
 import static org.hamcrest.core.Is.is;
 import static org.hamcrest.core.StringContains.containsString;
 import static org.junit.Assert.assertThat;
 import static org.mockito.BDDMockito.given;
 import static org.mockito.Matchers.anyInt;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.when;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import net.sf.ipsedixit.annotation.ArbitraryString;
 import net.sf.ipsedixit.core.StringType;
 import org.jboss.pressgang.ccms.contentspec.ContentSpec;
 import org.jboss.pressgang.ccms.contentspec.SpecTopic;
 import org.jboss.pressgang.ccms.contentspec.constants.CSConstants;
 import org.jboss.pressgang.ccms.contentspec.entities.InjectionOptions;
 import org.jboss.pressgang.ccms.contentspec.test.makers.validator.ContentSpecMaker;
 import org.jboss.pressgang.ccms.provider.ContentSpecProvider;
 import org.jboss.pressgang.ccms.provider.TagProvider;
 import org.jboss.pressgang.ccms.provider.TopicProvider;
 import org.jboss.pressgang.ccms.utils.common.HashUtilities;
 import org.jboss.pressgang.ccms.wrapper.CSNodeWrapper;
 import org.jboss.pressgang.ccms.wrapper.ContentSpecWrapper;
 import org.jboss.pressgang.ccms.wrapper.PropertyTagInContentSpecWrapper;
 import org.jboss.pressgang.ccms.wrapper.TagWrapper;
 import org.jboss.pressgang.ccms.wrapper.TopicWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.CollectionWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.UpdateableCollectionWrapper;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.powermock.api.mockito.PowerMockito;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 
 /**
  * @author kamiller@redhat.com (Katie Miller)
  */
 @PrepareForTest({HashUtilities.class})
 public class ContentSpecValidatorPostValidateTest extends ContentSpecValidatorTest {
 
     @ArbitraryString(type = StringType.ALPHANUMERIC) String username;
     @ArbitraryString(type = StringType.ALPHA) String strictTopicType;
     @ArbitraryString(type = StringType.ALPHA) String tagname;
     @Mock ContentSpecProvider contentSpecProvider;
     @Mock ContentSpecWrapper contentSpecWrapper;
     @Mock UpdateableCollectionWrapper<CSNodeWrapper> metaData;
     @Mock PropertyTagInContentSpecWrapper propertyTag;
     @Mock TagProvider tagProvider;
     @Mock CollectionWrapper<TagWrapper> tagWrapperCollection;
     @Mock List<TagWrapper> tagWrapperList;
     @Mock TagWrapper tagWrapper;
     @Mock TopicProvider topicProvider;
     @Mock TopicWrapper topicWrapper;
 
     @Before
     public void setUp() {
         when(dataProviderFactory.getProvider(ContentSpecProvider.class)).thenReturn(contentSpecProvider);
         when(dataProviderFactory.getProvider(TagProvider.class)).thenReturn(tagProvider);
         when(dataProviderFactory.getProvider(TopicProvider.class)).thenReturn(topicProvider);
         super.setUp();
     }
 
     @Test
     public void shouldPostValidateValidContentSpecWithoutId() {
         // Given a valid content spec that has no id set
         ContentSpec contentSpec = make(a(ContentSpecMaker.ContentSpec));
         contentSpec.setId(null);
 
         // When the spec is postvalidated
         boolean result = validator.postValidateContentSpec(contentSpec, username);
 
         // Then the result should be a success
         assertThat(result, is(true));
         // And no error messages should be output
         assertThat(logger.getLogMessages().toString(), containsString("[]"));
     }
 
     @Test
     public void shouldPostValidateValidContentSpecWithId() {
         // Given a valid content spec that has an id set
         ContentSpec contentSpec = make(a(ContentSpecMaker.ContentSpec));
         // And a valid content spec wrapper for the id and revision specified
         given(contentSpecProvider.getContentSpec(anyInt(), anyInt())).willReturn(contentSpecWrapper);
        given(contentSpecProvider.getContentSpecAsString(anyInt(), anyInt())).willReturn("");
         given(contentSpecWrapper.getChildren()).willReturn(metaData);
         given(metaData.getItems()).willReturn(new ArrayList<CSNodeWrapper>());
         // And that the checksum of the server content spec version matches the local one
         PowerMockito.mockStatic(HashUtilities.class);
         given(HashUtilities.generateMD5(anyString())).willReturn(contentSpec.getChecksum());
 
         // When the spec is postvalidated
         boolean result = validator.postValidateContentSpec(contentSpec, username);
 
         // Then the result should be a success
         assertThat(result, is(true));
         // And no error messages should be output
         assertThat(logger.getLogMessages().toString(), containsString("[]"));
     }
 
     @Test
     public void shouldLogErrorAndFailIfSpecIdOrRevisionSpecifiedInvalid() {
         // Given a valid content spec that has an id set
         ContentSpec contentSpec = make(a(ContentSpecMaker.ContentSpec));
         // And no content spec wrapper returned for the id and revision specified
         given(contentSpecProvider.getContentSpec(anyInt(), anyInt())).willReturn(null);
 
         // When the spec is postvalidated
         boolean result = validator.postValidateContentSpec(contentSpec, username);
 
         // Then the result should be a failure
         assertThat(result, is(false));
         // And an error message should be output
         assertThat(logger.getLogMessages().toString(), containsString("The Content Specification ID doesn't exist in the database."));
     }
 
     @Test
     public void shouldLogErrorAndFailIfServerAndSpecCheckSumsDoNotMatch() {
         // Given a valid content spec that has an id set
         ContentSpec contentSpec = make(a(ContentSpecMaker.ContentSpec));
         // And a valid content spec wrapper for the id and revision specified
         given(contentSpecProvider.getContentSpec(anyInt(), anyInt())).willReturn(contentSpecWrapper);
        given(contentSpecProvider.getContentSpecAsString(anyInt(), anyInt())).willReturn("CHECKSUM=" + username);
         given(contentSpecWrapper.getChildren()).willReturn(metaData);
         given(metaData.getItems()).willReturn(new ArrayList<CSNodeWrapper>());
 
         // When the spec is postvalidated
         boolean result = validator.postValidateContentSpec(contentSpec, username);
 
         // Then the result should be a failure
         assertThat(result, is(false));
         // And an error message should be output
         assertThat(logger.getLogMessages().toString(), containsString("Invalid Content Specification! Checksums must match to be edited."));
     }
 
     @Test
     public void shouldLogErrorAndFailIfLocalSpecCheckSumsNotFound() {
         // Given a valid content spec that has an id set but no checksum
         ContentSpec contentSpec = make(a(ContentSpecMaker.ContentSpec));
         contentSpec.setChecksum(null);
         // And a valid content spec wrapper for the id and revision specified
         given(contentSpecProvider.getContentSpec(anyInt(), anyInt())).willReturn(contentSpecWrapper);
        given(contentSpecProvider.getContentSpecAsString(anyInt(), anyInt())).willReturn("");
         given(contentSpecWrapper.getChildren()).willReturn(metaData);
         given(metaData.getItems()).willReturn(new ArrayList<CSNodeWrapper>());
 
         // When the spec is postvalidated
         boolean result = validator.postValidateContentSpec(contentSpec, username);
 
         // Then the result should be a failure
         assertThat(result, is(false));
         // And an error message should be output
         assertThat(logger.getLogMessages().toString(), containsString("Invalid Content Specification! Checksums must match to be edited."));
     }
 
     @Test
     public void shouldNotCalculateSpecCheckSumsIfOptionSetToIgnore() {
         // Given a valid content spec that has an id set but no checksum
         ContentSpec contentSpec = make(a(ContentSpecMaker.ContentSpec));
         contentSpec.setChecksum(null);
         // And a valid content spec wrapper for the id and revision specified
         given(contentSpecProvider.getContentSpec(anyInt(), anyInt())).willReturn(contentSpecWrapper);
        given(contentSpecProvider.getContentSpecAsString(anyInt(), anyInt())).willReturn("");
         given(contentSpecWrapper.getChildren()).willReturn(metaData);
         given(metaData.getItems()).willReturn(new ArrayList<CSNodeWrapper>());
         // And the ignoreChecksum option is set
         given(processingOptions.isIgnoreChecksum()).willReturn(true);
         PowerMockito.mockStatic(HashUtilities.class);
 
         // When the spec is postvalidated
         boolean result = validator.postValidateContentSpec(contentSpec, username);
 
         // Then the result should be a success as the checksums are ignored
         assertThat(result, is(true));
         // And no error messages should be output
         assertThat(logger.getLogMessages().toString(), containsString("[]"));
         // And the checksum method should not have been called
         PowerMockito.verifyStatic(Mockito.never());
         HashUtilities.generateMD5(anyString());
     }
 
     @Test
     public void shouldLogErrorAndFailIfSpecReadOnly() {
         // Given a valid content spec that has an id set
         ContentSpec contentSpec = make(a(ContentSpecMaker.ContentSpec));
         // And a valid content spec wrapper for the id and revision specified that specifies the spec is read-only
         given(contentSpecProvider.getContentSpec(anyInt(), anyInt())).willReturn(contentSpecWrapper);
        given(contentSpecProvider.getContentSpecAsString(anyInt(), anyInt())).willReturn("");
         given(contentSpecWrapper.getChildren()).willReturn(metaData);
         given(contentSpecWrapper.getProperty(CSConstants.CSP_READ_ONLY_PROPERTY_TAG_ID)).willReturn(propertyTag);
         given(propertyTag.getValue()).willReturn("foo");
         given(metaData.getItems()).willReturn(new ArrayList<CSNodeWrapper>());
         // And the ignoreChecksum option is set
         given(processingOptions.isIgnoreChecksum()).willReturn(true);
 
         // When the spec is postvalidated
         boolean result = validator.postValidateContentSpec(contentSpec, username);
 
         // Then the result should be a failure
         assertThat(result, is(false));
         // And an error message should be output
         assertThat(logger.getLogMessages().toString(),
                 containsString("Invalid Content Specification! The content specification is read-only."));
     }
 
     @Test
     public void shouldSucceedIfSpecReadOnlyTagContainsUsername() {
         // Given a valid content spec that has an id set
         ContentSpec contentSpec = make(a(ContentSpecMaker.ContentSpec));
         // And a valid content spec wrapper for the id and revision specified
         given(contentSpecProvider.getContentSpec(anyInt(), anyInt())).willReturn(contentSpecWrapper);
        given(contentSpecProvider.getContentSpecAsString(anyInt(), anyInt())).willReturn("");
         given(contentSpecWrapper.getChildren()).willReturn(metaData);
         given(contentSpecWrapper.getProperty(CSConstants.CSP_READ_ONLY_PROPERTY_TAG_ID)).willReturn(propertyTag);
         given(metaData.getItems()).willReturn(new ArrayList<CSNodeWrapper>());
         // And the wrapper has the read-only tag set but it contains the username
         given(propertyTag.getValue()).willReturn(username);
         // And the ignoreChecksum option is set
         given(processingOptions.isIgnoreChecksum()).willReturn(true);
 
         // When the spec is postvalidated
         boolean result = validator.postValidateContentSpec(contentSpec, username);
 
         // Then the result should be a success
         assertThat(result, is(true));
         // And no error messages should be output
         assertThat(logger.getLogMessages().toString(), containsString("[]"));
     }
 
     @Test
     public void shouldLogErrorAndFailIfInjectionTypeDoesNotMatchTag() {
         // Given a valid content spec that has no id set
         ContentSpec contentSpec = make(a(ContentSpecMaker.ContentSpec));
         contentSpec.setId(null);
         // And injection options are set
         contentSpec.setInjectionOptions(new InjectionOptions("[" + strictTopicType + "]"));
         // And no tag wrappers will be returned for a type
         given(tagProvider.getTagByName(strictTopicType)).willReturn(null);
 
         // When the spec is postvalidated
         boolean result = validator.postValidateContentSpec(contentSpec, username);
 
         // Then the result should be a failure
         assertThat(result, is(false));
         // And an error message should be output
         assertThat(logger.getLogMessages().toString(),
                 containsString("The injection type \"" + strictTopicType + "\" doesn't exist or isn't a Type."));
     }
 
     @Test
     public void shouldLogErrorAndFailIfInjectionTypeTagNotAType() {
         // Given a valid content spec that has no id set
         ContentSpec contentSpec = make(a(ContentSpecMaker.ContentSpec));
         contentSpec.setId(null);
         // And injection options are set
         contentSpec.setInjectionOptions(new InjectionOptions("[" + strictTopicType + "]"));
         // And a tag wrapper will be returned but it's not a type tag
         given(tagProvider.getTagByName(strictTopicType)).willReturn(tagWrapper);
         given(tagWrapper.containedInCategory(CSConstants.TYPE_CATEGORY_ID)).willReturn(false);
 
         // When the spec is postvalidated
         boolean result = validator.postValidateContentSpec(contentSpec, username);
 
         // Then the result should be a failure
         assertThat(result, is(false));
         // And an error message should be output
         assertThat(logger.getLogMessages().toString(),
                 containsString("The injection type \"" + strictTopicType + "\" doesn't exist or isn't a Type."));
     }
 
     @Test
     public void shouldLogErrorAndFailIfLevelInvalid() {
         // Given a valid content spec that has no id set
         ContentSpec contentSpec = make(a(ContentSpecMaker.ContentSpec));
         contentSpec.setId(null);
         // And has an invalid level
         contentSpec.getBaseLevel().setTags(Arrays.asList(tagname));
         given(tagProvider.getTagByName(anyString())).willReturn(null);
 
         // When the spec is postvalidated
         boolean result = validator.postValidateContentSpec(contentSpec, username);
 
         // Then the result should be a failure
         assertThat(result, is(false));
         // And an error message should be output
         assertThat(logger.getLogMessages().toString(),
                 containsString("Invalid Content Specification! Tag \"" + tagname + "\" doesn't exist."));
     }
 
     @Test
     public void shouldLogErrorAndFailIfRevisionHistoryInvalid() {
         // Given a valid content spec that has no id set
         ContentSpec contentSpec = make(a(ContentSpecMaker.ContentSpec));
         contentSpec.setId(null);
         // And has an invalid Revision History
         contentSpec.setRevisionHistory(new SpecTopic(0, "Revision History"));
         given(topicProvider.getTopic(anyInt(), anyInt())).willReturn(null);
 
         // When the spec is postvalidated
         boolean result = validator.postValidateContentSpec(contentSpec, username);
 
         // Then the result should be a failure
         assertThat(result, is(false));
         // And an error message should be output
         assertThat(logger.getLogMessages().toString(), containsString("Invalid Topic!"));
     }
 
     @Test
     public void shouldLogErrorAndFailIfFeedbackInvalid() {
         // Given a valid content spec that has no id set
         ContentSpec contentSpec = make(a(ContentSpecMaker.ContentSpec));
         contentSpec.setId(null);
         // And has an invalid Feedback topic
         contentSpec.setFeedback(new SpecTopic(0, "Feedback"));
         given(topicProvider.getTopic(anyInt(), anyInt())).willReturn(null);
 
         // When the spec is postvalidated
         boolean result = validator.postValidateContentSpec(contentSpec, username);
 
         // Then the result should be a failure
         assertThat(result, is(false));
         // And an error message should be output
         assertThat(logger.getLogMessages().toString(), containsString("Invalid Topic!"));
     }
 
     @Test
     public void shouldLogErrorAndFailIfLegalNoticeInvalid() {
         // Given a valid content spec that has no id set
         ContentSpec contentSpec = make(a(ContentSpecMaker.ContentSpec));
         contentSpec.setId(null);
         // And has an invalid Legal Notice
         contentSpec.setLegalNotice(new SpecTopic(0, "Legal Notice"));
         given(topicProvider.getTopic(anyInt(), anyInt())).willReturn(null);
 
         // When the spec is postvalidated
         boolean result = validator.postValidateContentSpec(contentSpec, username);
 
         // Then the result should be a failure
         assertThat(result, is(false));
         // And an error message should be output
         assertThat(logger.getLogMessages().toString(), containsString("Invalid Topic!"));
     }
 }
