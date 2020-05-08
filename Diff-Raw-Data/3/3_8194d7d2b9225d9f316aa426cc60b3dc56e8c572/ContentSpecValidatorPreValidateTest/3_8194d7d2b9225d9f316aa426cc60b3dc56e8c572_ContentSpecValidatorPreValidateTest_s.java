 package org.jboss.pressgang.ccms.contentspec.processor;
 
 import static com.natpryce.makeiteasy.MakeItEasy.a;
 import static com.natpryce.makeiteasy.MakeItEasy.make;
 import static com.natpryce.makeiteasy.MakeItEasy.with;
 import static org.hamcrest.core.Is.is;
 import static org.hamcrest.core.StringContains.containsString;
 import static org.jboss.pressgang.ccms.contentspec.test.makers.shared.SpecTopicMaker.id;
 import static org.jboss.pressgang.ccms.contentspec.test.makers.shared.SpecTopicMaker.revision;
 import static org.jboss.pressgang.ccms.contentspec.test.makers.validator.ContentSpecMaker.ContentSpec;
 import static org.jboss.pressgang.ccms.contentspec.test.makers.validator.ContentSpecMaker.bookType;
 import static org.jboss.pressgang.ccms.contentspec.test.makers.validator.ContentSpecMaker.bookVersion;
 import static org.jboss.pressgang.ccms.contentspec.test.makers.validator.ContentSpecMaker.copyrightHolder;
 import static org.jboss.pressgang.ccms.contentspec.test.makers.validator.ContentSpecMaker.copyrightYear;
 import static org.jboss.pressgang.ccms.contentspec.test.makers.validator.ContentSpecMaker.description;
 import static org.jboss.pressgang.ccms.contentspec.test.makers.validator.ContentSpecMaker.dtd;
 import static org.jboss.pressgang.ccms.contentspec.test.makers.validator.ContentSpecMaker.edition;
 import static org.jboss.pressgang.ccms.contentspec.test.makers.validator.ContentSpecMaker.product;
 import static org.jboss.pressgang.ccms.contentspec.test.makers.validator.ContentSpecMaker.subtitle;
 import static org.jboss.pressgang.ccms.contentspec.test.makers.validator.ContentSpecMaker.title;
 import static org.jboss.pressgang.ccms.contentspec.test.makers.validator.ContentSpecMaker.version;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Mockito.when;
 
 import net.sf.ipsedixit.annotation.Arbitrary;
 import org.jboss.pressgang.ccms.contentspec.ContentSpec;
 import org.jboss.pressgang.ccms.contentspec.Level;
 import org.jboss.pressgang.ccms.contentspec.SpecTopic;
import org.jboss.pressgang.ccms.contentspec.constants.CSConstants;
 import org.jboss.pressgang.ccms.contentspec.enums.BookType;
 import org.jboss.pressgang.ccms.contentspec.enums.LevelType;
 import org.jboss.pressgang.ccms.contentspec.test.makers.shared.LevelMaker;
 import org.jboss.pressgang.ccms.contentspec.test.makers.shared.SpecTopicMaker;
 import org.jboss.pressgang.ccms.contentspec.test.makers.validator.ContentSpecMaker;
 import org.jboss.pressgang.ccms.provider.StringConstantProvider;
 import org.jboss.pressgang.ccms.wrapper.StringConstantWrapper;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.powermock.core.classloader.annotations.PowerMockIgnore;
 
 /**
  * @author kamiller@redhat.com (Katie Miller)
  */
 @PowerMockIgnore({"javax.xml.parsers.*", "org.apache.xerces.jaxp.*", "org.xml.sax.*", "org.w3c.dom.*"})
 public class ContentSpecValidatorPreValidateTest extends ContentSpecValidatorTest {
     @Arbitrary Integer randomInt;
     @Arbitrary String randomString;
     @Mock StringConstantProvider stringConstantProvider;
     @Mock StringConstantWrapper stringConstantWrapper;
 
     @Override
     @Before
     public void setUp() {
         super.setUp();
         when(dataProviderFactory.getProvider(StringConstantProvider.class)).thenReturn(stringConstantProvider);
        when(stringConstantProvider.getStringConstant(CSConstants.VALID_ENTITIES_STRING_CONSTANT_ID)).thenReturn(stringConstantWrapper);
         when(stringConstantWrapper.getValue()).thenReturn("BZURL\nPRODUCT");
     }
 
     @Test
     public void shouldPreValidateValidContentSpec() {
         // Given a valid content spec
         ContentSpec contentSpec = make(a(ContentSpec, with(subtitle, randomString), with(description, "Some Description")));
         // with a level and spec topic
         addLevelAndTopicToContentSpec(contentSpec);
 
         // When the spec is prevalidated
         boolean result = validator.preValidateContentSpec(contentSpec);
 
         // Then the result should be a success
         assertThat(result, is(true));
         // And no error messages should be output
         assertThat(logger.getLogMessages().toString(), containsString("[]"));
     }
 
     @Test
     public void shouldFailAndLogErrorWhenInvalidBookType() {
         // Given an otherwise valid content spec with an invalid book type
         ContentSpec contentSpec = make(a(ContentSpec, with(bookType, BookType.INVALID)));
         // with a level and spec topic
         addLevelAndTopicToContentSpec(contentSpec);
 
         // When the spec is prevalidated
         boolean result = validator.preValidateContentSpec(contentSpec);
 
         // Then the result should be a failure
         assertThat(result, is(false));
         // And an error message should be output
         assertThat(logger.getLogMessages().toString(),
                 containsString("Invalid Content Specification! The specified book type is not valid."));
     }
 
     @Test
     public void shouldFailAndLogErrorWhenInvalidVersion() {
         // Given an otherwise valid content spec with an invalid version
         ContentSpec contentSpec = make(a(ContentSpec, with(version, "AAA")));
         // with a level and spec topic
         addLevelAndTopicToContentSpec(contentSpec);
 
         // When the spec is prevalidated
         boolean result = validator.preValidateContentSpec(contentSpec);
 
         // Then the result should be a failure
         assertThat(result, is(false));
         // And an error message should be output
         assertThat(logger.getLogMessages().toString(), containsString("Invalid Version specified. The value must be a valid version."));
     }
 
     @Test
     public void shouldFailAndLogErrorWhenInvalidEdition() {
         // Given an otherwise valid content spec with an invalid edition
         ContentSpec contentSpec = make(a(ContentSpec, with(edition, "AAA")));
         // with a level and spec topic
         addLevelAndTopicToContentSpec(contentSpec);
 
         // When the spec is prevalidated
         boolean result = validator.preValidateContentSpec(contentSpec);
 
         // Then the result should be a failure
         assertThat(result, is(false));
         // And an error message should be output
         assertThat(logger.getLogMessages().toString(), containsString("Invalid Edition specified. The value must be a valid version."));
     }
 
     @Test
     public void shouldFailAndLogErrorWhenInvalidBookVersion() {
         // Given an otherwise valid content spec with an invalid book version
         ContentSpec contentSpec = make(a(ContentSpec, with(bookVersion, "AAA")));
         // with a level and spec topic
         addLevelAndTopicToContentSpec(contentSpec);
 
         // When the spec is prevalidated
         boolean result = validator.preValidateContentSpec(contentSpec);
 
         // Then the result should be a failure
         assertThat(result, is(false));
         // And an error message should be output
         assertThat(logger.getLogMessages().toString(),
                 containsString("Invalid Book Version specified. The value must be a valid version."));
     }
 
     @Test
     public void shouldFailAndLogErrorWhenEmptyTitle() {
         // Given an otherwise valid content spec with an empty title
         ContentSpec contentSpec = make(a(ContentSpec, with(title, "")));
         // with a level and spec topic
         addLevelAndTopicToContentSpec(contentSpec);
 
         // When the spec is prevalidated
         boolean result = validator.preValidateContentSpec(contentSpec);
 
         // Then the result should be a failure
         assertThat(result, is(false));
         // And an error message should be output
         assertThat(logger.getLogMessages().toString(), containsString("Invalid Content Specification! No Title."));
     }
 
     @Test
     public void shouldFailAndLogErrorWhenEmptyProduct() {
         // Given an otherwise valid content spec with an empty product
         ContentSpec contentSpec = make(a(ContentSpec, with(product, "")));
         // with a level and spec topic
         addLevelAndTopicToContentSpec(contentSpec);
 
         // When the spec is prevalidated
         boolean result = validator.preValidateContentSpec(contentSpec);
 
         // Then the result should be a failure
         assertThat(result, is(false));
         // And an error message should be output
         assertThat(logger.getLogMessages().toString(), containsString("Invalid Content Specification! No Product specified."));
     }
 
     @Test
     public void shouldFailAndLogErrorWhenEmptyDtd() {
         // Given an otherwise valid content spec with an empty DTD
         ContentSpec contentSpec = make(a(ContentSpec, with(dtd, "")));
         // with a level and spec topic
         addLevelAndTopicToContentSpec(contentSpec);
 
         // When the spec is prevalidated
         boolean result = validator.preValidateContentSpec(contentSpec);
 
         // Then the result should be a failure
         assertThat(result, is(false));
         // And an error message should be output
         assertThat(logger.getLogMessages().toString(), containsString("Invalid Content Specification! No DTD specified."));
     }
 
     @Test
     public void shouldFailAndLogErrorWhenUnexpectedDtd() {
         // Given an otherwise valid content spec with an unexpected DTD
         ContentSpec contentSpec = make(a(ContentSpec, with(dtd, "Docbook 3")));
         // with a level and spec topic
         addLevelAndTopicToContentSpec(contentSpec);
 
         // When the spec is prevalidated
         boolean result = validator.preValidateContentSpec(contentSpec);
 
         // Then the result should be a failure
         assertThat(result, is(false));
         // And an error message should be output
         assertThat(logger.getLogMessages().toString(), containsString(
                 "Invalid Content Specification! DTD specified is unsupported. Docbook 4.5 is the only currently supported DTD."));
     }
 
     @Test
     public void shouldFailAndLogErrorWhenEmptyCopyrightHolder() {
         // Given an otherwise valid content spec with an empty copyright holder
         ContentSpec contentSpec = make(a(ContentSpec, with(copyrightHolder, "")));
         // with a level and spec topic
         addLevelAndTopicToContentSpec(contentSpec);
 
         // When the spec is prevalidated
         boolean result = validator.preValidateContentSpec(contentSpec);
 
         // Then the result should be a failure
         assertThat(result, is(false));
         // And an error message should be output
         assertThat(logger.getLogMessages().toString(),
                 containsString("Invalid Content Specification! A Copyright Holder must be specified."));
     }
 
     @Test
     public void shouldFailAndLogErrorWhenLevelsInvalid() {
         // Given an otherwise valid content spec with invalid levels
         ContentSpec contentSpec = make(a(ContentSpec));
 
         // When the spec is prevalidated
         boolean result = validator.preValidateContentSpec(contentSpec);
 
         // Then the result should be a failure
         assertThat(result, is(false));
         // And an error message should be output
         assertThat(logger.getLogMessages().toString(), containsString("No topics or levels"));
     }
 
     @Test
     public void shouldFailAndLogErrorWhenTopicsHaveSameIdAndDifferentRevisions() {
         // Given an otherwise valid content spec with two spec topics that are existing topics with the same id
         // but different revisions
         ContentSpec contentSpec = make(a(ContentSpec));
         SpecTopic specTopic = make(a(SpecTopicMaker.SpecTopic, with(id, randomInt.toString()), with(revision, randomInt)));
         SpecTopic specTopic2 = make(a(SpecTopicMaker.SpecTopic, with(id, randomInt.toString()), with(revision, randomInt + 1)));
         final Level level = make(a(LevelMaker.Level, with(LevelMaker.levelType, LevelType.CHAPTER)));
         contentSpec.getBaseLevel().appendChild(level);
         level.appendChild(specTopic);
         level.appendChild(specTopic2);
 
         // When the spec is prevalidated
         boolean result = validator.preValidateContentSpec(contentSpec);
 
         // Then the result should be a failure
         assertThat(result, is(false));
         // And an error message should be output
         assertThat(logger.getLogMessages().toString(), containsString(
                 "Invalid Content Specification! Topic " + randomInt + " has two or more different revisions included in the Content Specification."));
     }
 
     @Test
     public void shouldFailAndLogErrorWhenInvalidCopyrightYear() {
         // Given an invalid content spec because of an invalid copyright year
         ContentSpec contentSpec = make(a(ContentSpec, with(copyrightYear, randomString)));
         // with a level and spec topic
         addLevelAndTopicToContentSpec(contentSpec);
 
         // When the spec is prevalidated
         boolean result = validator.preValidateContentSpec(contentSpec);
 
         // Then the result should be a failure
         assertThat(result, is(false));
         // And an error message should be output
         assertThat(logger.getLogMessages().toString(), containsString("The Copyright Year is invalid."));
     }
 
     @Test
     public void shouldFailAndLogErrorWhenInvalidRevisionHistory() {
         // Given an invalid content spec because of an invalid revision history
         ContentSpec contentSpec = make(a(ContentSpec));
         contentSpec.setRevisionHistory(new SpecTopic(0, null));
         // with a level and spec topic
         addLevelAndTopicToContentSpec(contentSpec);
 
         // When the spec is prevalidated
         boolean result = validator.preValidateContentSpec(contentSpec);
 
         // Then the result should be a failure
         assertThat(result, is(false));
         // And an error message should be output
         assertThat(logger.getLogMessages().toString(), containsString("Invalid Topic!"));
     }
 
     @Test
     public void shouldFailAndLogErrorWhenInvalidFeedback() {
         // Given an invalid content spec because of an invalid feedback
         ContentSpec contentSpec = make(a(ContentSpec));
         contentSpec.setFeedback(new SpecTopic(0, null));
         // with a level and spec topic
         addLevelAndTopicToContentSpec(contentSpec);
 
         // When the spec is prevalidated
         boolean result = validator.preValidateContentSpec(contentSpec);
 
         // Then the result should be a failure
         assertThat(result, is(false));
         // And an error message should be output
         assertThat(logger.getLogMessages().toString(), containsString("Invalid Topic!"));
     }
 
     @Test
     public void shouldFailAndLogErrorWhenInvalidLegalNotice() {
         // Given an invalid content spec because of an invalid legal notice
         ContentSpec contentSpec = make(a(ContentSpec));
         contentSpec.setLegalNotice(new SpecTopic(0, null));
         // with a level and spec topic
         addLevelAndTopicToContentSpec(contentSpec);
 
         // When the spec is prevalidated
         boolean result = validator.preValidateContentSpec(contentSpec);
 
         // Then the result should be a failure
         assertThat(result, is(false));
         // And an error message should be output
         assertThat(logger.getLogMessages().toString(), containsString("Invalid Topic!"));
     }
 
     @Test
     public void shouldFailAndLogErrorWhenInvalidAuthorGroup() {
         // Given an invalid content spec because of an invalid feedback
         ContentSpec contentSpec = make(a(ContentSpec));
         contentSpec.setAuthorGroup(new SpecTopic(0, null));
         // with a level and spec topic
         addLevelAndTopicToContentSpec(contentSpec);
 
         // When the spec is prevalidated
         boolean result = validator.preValidateContentSpec(contentSpec);
 
         // Then the result should be a failure
         assertThat(result, is(false));
         // And an error message should be output
         assertThat(logger.getLogMessages().toString(), containsString("Invalid Topic!"));
     }
 
     @Test
     public void shouldFailAndLogErrorWhenInvalidPubsnumber() {
         // Given an otherwise valid content spec with an invalid pubsnumber
         ContentSpec contentSpec = make(a(ContentSpec));
         contentSpec.setPubsNumber(-1);
         // with a level and spec topic
         addLevelAndTopicToContentSpec(contentSpec);
 
         // When the spec is prevalidated
         boolean result = validator.preValidateContentSpec(contentSpec);
 
         // Then the result should be a failure
         assertThat(result, is(false));
         // And an error message should be output
         assertThat(logger.getLogMessages().toString(), containsString("Invalid Pubsnumber specified. The value must be a positive number."));
     }
 
     @Test
     public void shouldFailWhenEntitiesIsInvalid() {
         // Given an invalid XML entity declaration
         final String entities = "<!ENTITY test \"http://example.com/query?word+with%20spaces\">";
         // and a content spec to store the entity
         ContentSpec contentSpec = make(a(ContentSpecMaker.ContentSpec));
         contentSpec.setEntities(entities);
         // with a level and spec topic
         addLevelAndTopicToContentSpec(contentSpec);
 
         // When validating the entities
         boolean result = validator.preValidateContentSpec(contentSpec);
 
         // Then the result should be false and should have a useful error message
         assertFalse(result);
         assertThat(logger.getLogMessages().toString(),
                 containsString("Invalid Content Specification! Invalid XML Entities. XML Error " + "Message: "));
     }
 
     @Test
     public void shouldSucceedWithValidEntities() {
         // Given a valid XML entity declaration
         final String entities = "<!ENTITY BZURL \"http://example.com/query?word+with+spaces\">\n" +
                 "<!ENTITY PRODUCT \"&test;\">";
         // and a content spec to store the entity
         ContentSpec contentSpec = make(a(ContentSpecMaker.ContentSpec));
         contentSpec.setEntities(entities);
         // with a level and spec topic
         addLevelAndTopicToContentSpec(contentSpec);
 
         // When validating the entities
         boolean result = validator.preValidateContentSpec(contentSpec);
 
         // Then the result should be true
         assertTrue(result);
     }
 
     @Test
     public void shouldPrintErrorWithInvalidAbstract() {
         // Given a content spec with an invalid abstract
         ContentSpec contentSpec = make(a(ContentSpec, with(description, "<blah>" + randomInt)));
         // with a level and spec topic
         addLevelAndTopicToContentSpec(contentSpec);
 
         // When validating the abstract
         boolean result = validator.preValidateContentSpec(contentSpec);
 
         // Then the result should be false
         assertFalse(result);
         // and an error should have been printed
         assertThat(logger.getLogMessages().toString(), containsString(
                 "Invalid Content Specification! The abstract is not valid XML. Error Message: "));
     }
 
     @Test
     public void shouldPrintWarningWhenConflictingConditions() {
         // Given a content spec with conflicting conditions
         ContentSpec contentSpec = make(a(ContentSpecMaker.ContentSpec));
         contentSpec.getBaseLevel().setConditionStatement(randomString);
         contentSpec.setPublicanCfg("condition: " + randomString);
         // with a level and spec topic
         addLevelAndTopicToContentSpec(contentSpec);
 
         // When validating the conflicting conditions
         boolean result = validator.preValidateContentSpec(contentSpec);
 
         // Then the result should be true
         assertTrue(result);
         // and a warning should have been printed
         assertThat(logger.getLogMessages().toString(), containsString(
                 "A condition has been defined in publican.cfg and as such the condition will be ignored."));
     }
 
     @Test
     public void shouldPrintWarningWhenConflictingConditionsInCustomCfg() {
         // Given a content spec with conflicting conditions
         ContentSpec contentSpec = make(a(ContentSpecMaker.ContentSpec));
         contentSpec.getBaseLevel().setConditionStatement(randomString);
         contentSpec.setAdditionalPublicanCfg("beta", "condition: " + randomString);
         contentSpec.setDefaultPublicanCfg("beta");
         // with a level and spec topic
         addLevelAndTopicToContentSpec(contentSpec);
 
         // When validating the conflicting conditions
         boolean result = validator.preValidateContentSpec(contentSpec);
 
         // Then the result should be true
         assertTrue(result);
         // and a warning should have been printed
         assertThat(logger.getLogMessages().toString(), containsString(
                 "A condition has been defined in publican.cfg and as such the condition will be ignored."));
     }
 
     private void addLevelAndTopicToContentSpec(final ContentSpec contentSpec) {
         // with a level and spec topic
         Level childLevel = make(a(LevelMaker.Level, with(LevelMaker.levelType, LevelType.APPENDIX)));
         contentSpec.getBaseLevel().appendChild(childLevel);
         childLevel.appendChild(make(a(SpecTopicMaker.SpecTopic)));
     }
 }
