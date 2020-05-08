 package org.jboss.pressgang.ccms.contentspec.processor;
 
 import static com.natpryce.makeiteasy.MakeItEasy.a;
 import static com.natpryce.makeiteasy.MakeItEasy.make;
 import static com.natpryce.makeiteasy.MakeItEasy.with;
 import static org.hamcrest.Matchers.contains;
 import static org.hamcrest.Matchers.containsString;
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.fail;
 
 import java.util.Arrays;
 import java.util.List;
 
 import net.sf.ipsedixit.annotation.Arbitrary;
 import net.sf.ipsedixit.annotation.ArbitraryString;
 import net.sf.ipsedixit.core.StringType;
 import org.jboss.pressgang.ccms.contentspec.Level;
 import org.jboss.pressgang.ccms.contentspec.SpecTopic;
 import org.jboss.pressgang.ccms.contentspec.enums.LevelType;
 import org.jboss.pressgang.ccms.contentspec.enums.RelationshipType;
 import org.jboss.pressgang.ccms.contentspec.exceptions.ParsingException;
 import org.jboss.pressgang.ccms.contentspec.test.makers.parser.TopicRelationshipStringMaker;
 import org.jboss.pressgang.ccms.contentspec.test.makers.parser.TopicStringMaker;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 
 public class ContentSpecParserParseTopicTest extends ContentSpecParserTest {
     @Arbitrary Integer id;
     @Arbitrary Integer randomNumber;
     @ArbitraryString(type = StringType.ALPHANUMERIC) String randomString;
     @ArbitraryString(type = StringType.ALPHANUMERIC) String title;
 
     @Test
     public void shouldReturnTopicWithTitleAndExistingId() {
         // Given a string that represents a topic with a title and id
         String topicString = make(
                 a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title), with(TopicStringMaker.id, id.toString())));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
         } catch (ParsingException e) {
             fail("Parsing should not have failed.");
         }
 
         // Then check that the topic has the right data set
         assertNotNull(topic);
         assertThat(topic.getId(), is(id.toString()));
         assertThat(topic.getTitle(), is(title));
         assertThat(topic.getUniqueId(), is("L" + randomNumber + "-" + id));
     }
 
     @Test
     public void shouldReturnTopicWithTitleExistingIdAndRevision() {
         // Given a string that represents a topic with a title, id and revision
         String topicString = make(
                 a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title), with(TopicStringMaker.id, id.toString()),
                         with(TopicStringMaker.revision, randomNumber.toString())));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
         } catch (ParsingException e) {
             fail("Parsing should not have failed.");
         }
 
         // Then check that the topic has the right data set
         assertNotNull(topic);
         assertThat(topic.getId(), is(id.toString()));
         assertThat(topic.getRevision(), is(randomNumber));
         assertThat(topic.getTitle(), is(title));
         assertThat(topic.getUniqueId(), is("L" + randomNumber + "-" + id));
     }
 
     @Test
     public void shouldThrowExceptionWithTitleExistingIdAndInvalidRevision() {
         // Given a string that represents a topic with a title, id and invalid revision
         String topicString = make(
                 a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title), with(TopicStringMaker.id, id.toString()),
                         with(TopicStringMaker.revision, randomString.toString())));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
             fail("Parsing the topic should have thrown an exception.");
         } catch (ParsingException e) {
             assertThat(e.getMessage(), containsString("Line " + lineNumber + ": Invalid Topic! Revision attribute must be a valid number" +
                     "."));
         }
     }
 
     @Test
     public void shouldReturnTopicWithTitleAndNewIdWithType() {
         // Given a string that represents a topic with a title, new id and a type
         String topicString = make(a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title), with(TopicStringMaker.id, "N"),
                 with(TopicStringMaker.topicType, "Concept")));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
         } catch (ParsingException e) {
             fail("Parsing should not have failed.");
         }
 
         // Then check that the topic has the right data set
         assertNotNull(topic);
         assertThat(topic.getId(), is("N"));
         assertThat(topic.getTitle(), is(title));
         assertThat(topic.getUniqueId(), is("L" + randomNumber + "-N"));
     }
 
     @Test
     public void shouldReturnTopicWithTitleAndUniqueNewIdWithType() {
         String type = "Concept";
         // Given a string that represents a topic with a title, new id and a type
         String topicString = make(a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title), with(TopicStringMaker.id, "N1"),
                 with(TopicStringMaker.topicType, type)));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
         } catch (ParsingException e) {
             fail("Parsing should not have failed.");
         }
 
         // Then check that the topic has the right data set
         assertNotNull(topic);
         assertThat(topic.getId(), is("N1"));
         assertThat(topic.getTitle(), is(title));
         assertThat(topic.getUniqueId(), is("N1"));
         assertThat(topic.getType(), is(type));
     }
 
     @Test
     public void shouldThrowExceptionWithTitleAndDuplicatedUniqueNewIdWithType() {
         // Given a string that represents a topic with a title, new id and a type
         String topicString = make(a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title), with(TopicStringMaker.id, "N1"),
                 with(TopicStringMaker.topicType, "Concept")));
         // and a topic already exists with that unique id
         parser.getSpecTopics().put("N1", new SpecTopic(0, "N1"));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
             fail("Parsing the topic should have thrown an exception.");
         } catch (ParsingException e) {
             assertThat(e.getMessage(),
                     containsString("Line " + lineNumber + ": Invalid Content Specification! Duplicate topic ID ( N1 )."));
         }
     }
 
     @Test
     public void shouldThrowExceptionWithTitleAndNewIdWithoutType() {
         // Given a string that represents a topic with a title and a new id
         String topicString = make(a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title), with(TopicStringMaker.id, "N")));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
             fail("Parsing the topic should have thrown an exception.");
         } catch (ParsingException e) {
             assertThat(e.getMessage(), containsString("Line " + lineNumber + ": Invalid Topic! Title, Type and ID must be specified."));
         }
     }
 
 
     @Test
     public void shouldReturnTopicWithTitleAndCloneId() {
         // Given a string that represents a topic with a title, new id and a type
         String topicString = make(
                 a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title), with(TopicStringMaker.id, "C" + id)));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
         } catch (ParsingException e) {
             fail("Parsing should not have failed.");
         }
 
         // Then check that the topic has the right data set
         assertNotNull(topic);
         assertThat(topic.getId(), is("C" + id));
         assertThat(topic.getTitle(), is(title));
         assertThat(topic.getUniqueId(), is("L" + randomNumber + "-C" + id));
     }
 
     @Test
     public void shouldReturnTopicWithTitleAndAlternateCloneId() {
         // Given a string that represents a topic with a title, new id and a type
         String topicString = make(a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title), with(TopicStringMaker.id, "N"),
                 with(TopicStringMaker.topicType, "C: " + id)));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
         } catch (ParsingException e) {
             fail("Parsing should not have failed.");
         }
 
         // Then check that the topic has the right data set
         assertNotNull(topic);
         assertThat(topic.getId(), is("C" + id));
         assertThat(topic.getTitle(), is(title));
         assertThat(topic.getUniqueId(), is("L" + randomNumber + "-C" + id));
     }
 
     @Test
     public void shouldReturnTopicWithTitleAndDuplicateId() {
         // Given a string that represents a topic with a title, new id and a type
         String topicString = make(
                 a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title), with(TopicStringMaker.id, "X" + id)));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
         } catch (ParsingException e) {
             fail("Parsing should not have failed.");
         }
 
         // Then check that the topic has the right data set
         assertNotNull(topic);
         assertThat(topic.getId(), is("X" + id));
         assertThat(topic.getTitle(), is(title));
         assertThat(topic.getUniqueId(), is("L" + randomNumber + "-X" + id));
     }
 
     @Test
     public void shouldReturnTopicWithTitleAndClonedDuplicateId() {
         // Given a string that represents a topic with a title, new id and a type
         String topicString = make(
                 a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title), with(TopicStringMaker.id, "XC" + id)));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
         } catch (ParsingException e) {
             fail("Parsing should not have failed.");
         }
 
         // Then check that the topic has the right data set
         assertNotNull(topic);
         assertThat(topic.getId(), is("XC" + id));
         assertThat(topic.getTitle(), is(title));
         assertThat(topic.getUniqueId(), is("L" + randomNumber + "-XC" + id));
     }
 
     @Test
     public void shouldPrintWarningWithTitleAndDuplicateIdAndOptions() {
         // Given a string that represents a topic with a title, new id and a type
         String topicString = make(a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title), with(TopicStringMaker.id, "X" + id),
                 with(TopicStringMaker.url, randomString)));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         ArgumentCaptor<String> warningMessage = ArgumentCaptor.forClass(String.class);
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
         } catch (ParsingException e) {
             fail("Parsing should not have failed.");
         }
 
         // Then check that the topic has the right data set
         assertNotNull(topic);
         assertThat(topic.getId(), is("X" + id));
         assertThat(topic.getTitle(), is(title));
         assertThat(topic.getUniqueId(), is("L" + randomNumber + "-X" + id));
         assertThat(logger.getLogMessages().get(0).toString(), containsString("Line " + lineNumber + ": All types, descriptions, " +
                "source urls and writers will be ignored for duplicate Topics."));
     }
 
     @Test
     public void shouldPrintWarningWithTitleAndClonedDuplicateIdAndOptions() {
         // Given a string that represents a topic with a title, new id and a type
         String topicString = make(a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title), with(TopicStringMaker.id, "XC" + id),
                 with(TopicStringMaker.url, randomString)));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
         } catch (ParsingException e) {
             fail("Parsing should not have failed.");
         }
 
         // Then check that the topic has the right data set
         assertNotNull(topic);
         assertThat(topic.getId(), is("XC" + id));
         assertThat(topic.getTitle(), is(title));
         assertThat(topic.getUniqueId(), is("L" + randomNumber + "-XC" + id));
         assertThat(logger.getLogMessages().get(0).toString(), containsString("Line " + lineNumber + ": All types, descriptions, " +
                "source urls and writers will be ignored for duplicate Topics."));
     }
 
     @Test
     public void shouldThrowExceptionWithTitleAndInvalidId() {
         // Given a string that represents a topic with a title and a new id
         String topicString = make(
                 a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title), with(TopicStringMaker.id, randomString)));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
             fail("Parsing the topic should have thrown an exception.");
         } catch (ParsingException e) {
             assertThat(e.getMessage(), containsString("Line " + lineNumber + ": Invalid Topic! Title and ID must be specified."));
         }
     }
 
     @Test
     public void shouldReturnTopicWithTitleIdAndUrl() {
         String url = "http://www.example.com/";
         // Given a string that represents a topic with a title and id
         String topicString = make(
                 a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title), with(TopicStringMaker.id, id.toString()),
                         with(TopicStringMaker.url, url)));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
         } catch (ParsingException e) {
             fail("Parsing should not have failed.");
         }
 
         // Then check that the topic has the right data set
         assertNotNull(topic);
         assertThat(topic.getId(), is(id.toString()));
         assertThat(topic.getTitle(), is(title));
         assertThat(topic.getUniqueId(), is("L" + randomNumber + "-" + id));
         assertThat(topic.getSourceUrls(true), contains(url));
     }
 
     @Test
     public void shouldThrowExceptionWithOnlyTitle() {
         // Given a string that represents a topic with a title and missing brackets
         String topicString = make(
                 a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title), with(TopicStringMaker.missingClosingBracket, true),
                         with(TopicStringMaker.missingOpeningBracket, true)));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
             fail("Parsing the topic should have thrown an exception.");
         } catch (ParsingException e) {
             assertThat(e.getMessage(), containsString("Line " + lineNumber + ": Invalid Topic! Incorrect topic format."));
         }
     }
 
     @Test
     public void shouldThrowExceptionWithMissingOpeningBracket() {
         // Given a string that represents a topic with a title, id and a missing bracket
         String topicString = make(
                 a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title), with(TopicStringMaker.id, id.toString()),
                         with(TopicStringMaker.missingOpeningBracket, true)));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
             fail("Parsing the topic should have thrown an exception.");
         } catch (ParsingException e) {
             assertThat(e.getMessage(),
                     containsString("Line " + lineNumber + ": Invalid Content Specification! Missing opening bracket ([) detected."));
         }
     }
 
     @Test
     public void shouldThrowExceptionWithMissingClosingBracket() {
         // Given a string that represents a topic with a title, id and a missing bracket
         String topicString = make(
                 a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title), with(TopicStringMaker.id, id.toString()),
                         with(TopicStringMaker.missingClosingBracket, true)));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
             fail("Parsing the topic should have thrown an exception.");
         } catch (ParsingException e) {
             assertThat(e.getMessage(),
                     containsString("Line " + lineNumber + ": Invalid Content Specification! Missing ending bracket (]) detected."));
         }
     }
 
     @Test
     public void shouldThrowExceptionWithNoId() {
         // Given a string that represents a topic with a title
         String topicString = make(a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title)));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
             fail("Parsing the topic should have thrown an exception.");
         } catch (ParsingException e) {
             assertThat(e.getMessage(), containsString("Line " + lineNumber + ": Invalid Topic! Title and ID must be specified."));
         }
     }
 
     @Test
     public void shouldThrowExceptionWithMissingVariable() {
         // Given a string that represents a topic title, id and a missing variable
         String topicString = make(
                 a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title), with(TopicStringMaker.id, id.toString()),
                         with(TopicStringMaker.missingVariable, true)));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
             fail("Parsing the topic should have thrown an exception.");
         } catch (ParsingException e) {
             assertThat(e.getMessage(),
                     containsString("Line " + lineNumber + ": Invalid Content Specification! Missing attribute detected."));
         }
     }
 
     @Test
     public void shouldParseTopicWithTargetNumber() {
         // Given a string that represents a topic title, id and a missing variable
         String topicString = make(
                 a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title), with(TopicStringMaker.id, id.toString()),
                         with(TopicStringMaker.targetId, "[T1]")));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
         } catch (ParsingException e) {
             fail("Parsing should not have failed.");
         }
 
         // Then check that the topic has the right data set
         assertNotNull(topic);
         assertThat(topic.getId(), is(id.toString()));
         assertThat(topic.getTitle(), is(title));
         assertThat(topic.getUniqueId(), is("L" + randomNumber + "-" + id));
         assertThat(topic.getTargetId(), is("T1"));
     }
 
     @Test
     public void shouldParseTopicWithTargetString() {
         // Given a string that represents a topic title, id and a missing variable
         String topicString = make(
                 a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title), with(TopicStringMaker.id, id.toString()),
                         with(TopicStringMaker.targetId, "[T-Test-String]")));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
         } catch (ParsingException e) {
             fail("Parsing should not have failed.");
         }
 
         // Then check that the topic has the right data set
         assertNotNull(topic);
         assertThat(topic.getId(), is(id.toString()));
         assertThat(topic.getTitle(), is(title));
         assertThat(topic.getUniqueId(), is("L" + randomNumber + "-" + id));
         assertThat(topic.getTargetId(), is("T-Test-String"));
     }
 
     @Test
     public void shouldThrowExceptionWithInvalidTarget() {
         // Given a string that represents a topic title, id and a missing variable
         String topicString = make(
                 a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title), with(TopicStringMaker.id, id.toString()),
                         with(TopicStringMaker.targetId, "[Test]")));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
             fail("Parsing the topic should have thrown an exception.");
         } catch (ParsingException e) {
             assertThat(e.getMessage(), containsString("Line " + lineNumber + ": Duplicated bracket types found."));
         }
     }
 
     @Test
     public void shouldThrowExceptionWithTargetThatAlreadyExistsAsTopic() {
         // Given a string that represents a topic title, id and a missing variable
         String topicString = make(
                 a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title), with(TopicStringMaker.id, id.toString()),
                         with(TopicStringMaker.targetId, "[T1]")));
         // and the target already exists
         parser.getTargetTopics().put("T1", new SpecTopic(0, title));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
             fail("Parsing the topic should have thrown an exception.");
         } catch (ParsingException e) {
             assertThat(e.getMessage(), containsString("Target ID is duplicated. Target ID's must be unique."));
         }
     }
 
     @Test
     public void shouldThrowExceptionWithTargetThatAlreadyExistsAsLevel() {
         // Given a string that represents a topic title, id and a missing variable
         String topicString = make(
                 a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title), with(TopicStringMaker.id, id.toString()),
                         with(TopicStringMaker.targetId, "[T1]")));
         // and the target already exists
         parser.getTargetLevels().put("T1", new Level(title, LevelType.CHAPTER));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
             fail("Parsing the topic should have thrown an exception.");
         } catch (ParsingException e) {
             assertThat(e.getMessage(), containsString("Target ID is duplicated. Target ID's must be unique."));
         }
     }
 
     @Test
     public void shouldParseTopicWithShortReferToRelationship() {
         // Given a valid refers to relationship
         List<String> topics = Arrays.asList(id.toString(), "T0");
         String refersToRelationship = make(
                 a(TopicRelationshipStringMaker.TopicRelationshipString, with(TopicRelationshipStringMaker.relationshipType, "R"),
                         with(TopicRelationshipStringMaker.relationships, topics)));
         // and a string that represents a topic title and id
         String topicString = make(
                 a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title), with(TopicStringMaker.id, id.toString()),
                         with(TopicStringMaker.relationship, refersToRelationship)));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
         } catch (ParsingException e) {
             fail("Parsing should not have failed.");
         }
 
         // Then check that the topic has the right data set
         String uniqueId = "L" + randomNumber + "-" + id;
         assertNotNull(topic);
         assertThat(topic.getId(), is(id.toString()));
         assertThat(topic.getTitle(), is(title));
         assertThat(topic.getUniqueId(), is(uniqueId));
         // and the right number of relationships exist
         assertThat(parser.getRelationships().size(), is(1));
         assertThat(parser.getRelationships().get(uniqueId).size(), is(2));
         // and the relationships have the right main topic id
         assertThat(parser.getRelationships().get(uniqueId).get(0).getMainRelationshipTopicId(), is(uniqueId));
         assertThat(parser.getRelationships().get(uniqueId).get(1).getMainRelationshipTopicId(), is(uniqueId));
         // and the relationship type is correct
         assertThat(parser.getRelationships().get(uniqueId).get(0).getType(), is(RelationshipType.REFER_TO));
         assertThat(parser.getRelationships().get(uniqueId).get(1).getType(), is(RelationshipType.REFER_TO));
     }
 
     @Test
     public void shouldParseTopicWithLongReferToRelationship() {
         // Given a valid refers to relationship
         List<String> topics = Arrays.asList("Test [" + id + "]", "T0");
         String refersToRelationship = make(
                 a(TopicRelationshipStringMaker.TopicRelationshipString, with(TopicRelationshipStringMaker.relationshipType, "Refer-To"),
                         with(TopicRelationshipStringMaker.longRelationship, true),
                         with(TopicRelationshipStringMaker.relationships, topics)));
         // and the relationship is setup in the lines
         parser.getLines().addAll(Arrays.asList(refersToRelationship.split("\n")));
         // and a string that represents a topic title and id
         String topicString = make(
                 a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title), with(TopicStringMaker.id, id.toString())));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
         } catch (ParsingException e) {
             fail("Parsing should not have failed.");
         }
 
         // Then check that the topic has the right data set
         String uniqueId = "L" + randomNumber + "-" + id;
         assertNotNull(topic);
         assertThat(topic.getId(), is(id.toString()));
         assertThat(topic.getTitle(), is(title));
         assertThat(topic.getUniqueId(), is(uniqueId));
         // and the right number of relationships exist
         assertThat(parser.getRelationships().size(), is(1));
         assertThat(parser.getRelationships().get(uniqueId).size(), is(2));
         // and the relationships have the right main topic id
         assertThat(parser.getRelationships().get(uniqueId).get(0).getMainRelationshipTopicId(), is(uniqueId));
         assertThat(parser.getRelationships().get(uniqueId).get(1).getMainRelationshipTopicId(), is(uniqueId));
         // and the relationship type is correct
         assertThat(parser.getRelationships().get(uniqueId).get(0).getType(), is(RelationshipType.REFER_TO));
         assertThat(parser.getRelationships().get(uniqueId).get(1).getType(), is(RelationshipType.REFER_TO));
     }
 
     @Test
     public void shouldParseTopicWithShortPrerequisiteRelationship() {
         // Given a valid relationship
         List<String> topics = Arrays.asList(id.toString(), "T0");
         String prerequisiteRelationship = make(
                 a(TopicRelationshipStringMaker.TopicRelationshipString, with(TopicRelationshipStringMaker.relationshipType, "P"),
                         with(TopicRelationshipStringMaker.relationships, topics)));
         // and a string that represents a topic title and id
         String topicString = make(
                 a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title), with(TopicStringMaker.id, id.toString()),
                         with(TopicStringMaker.relationship, prerequisiteRelationship)));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
         } catch (ParsingException e) {
             fail("Parsing should not have failed.");
         }
 
         // Then check that the topic has the right data set
         String uniqueId = "L" + randomNumber + "-" + id;
         assertNotNull(topic);
         assertThat(topic.getId(), is(id.toString()));
         assertThat(topic.getTitle(), is(title));
         assertThat(topic.getUniqueId(), is(uniqueId));
         // and the right number of relationships exist
         assertThat(parser.getRelationships().size(), is(1));
         assertThat(parser.getRelationships().get(uniqueId).size(), is(2));
         // and the relationships have the right main topic id
         assertThat(parser.getRelationships().get(uniqueId).get(0).getMainRelationshipTopicId(), is(uniqueId));
         assertThat(parser.getRelationships().get(uniqueId).get(1).getMainRelationshipTopicId(), is(uniqueId));
         // and the relationship type is correct
         assertThat(parser.getRelationships().get(uniqueId).get(0).getType(), is(RelationshipType.PREREQUISITE));
         assertThat(parser.getRelationships().get(uniqueId).get(1).getType(), is(RelationshipType.PREREQUISITE));
     }
 
     @Test
     public void shouldParseTopicWithLongPrerequisiteRelationship() {
         // Given a valid relationship
         List<String> topics = Arrays.asList("Test [" + id + "]", "T0");
         String prerequisiteRelationship = make(
                 a(TopicRelationshipStringMaker.TopicRelationshipString, with(TopicRelationshipStringMaker.relationshipType, "Prerequisite"),
                         with(TopicRelationshipStringMaker.longRelationship, true),
                         with(TopicRelationshipStringMaker.relationships, topics)));
         // and the relationship is setup in the lines
         parser.getLines().addAll(Arrays.asList(prerequisiteRelationship.split("\n")));
         // and a string that represents a topic title and id
         String topicString = make(
                 a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title), with(TopicStringMaker.id, id.toString())));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
         } catch (ParsingException e) {
             fail("Parsing should not have failed.");
         }
 
         // Then check that the topic has the right data set
         String uniqueId = "L" + randomNumber + "-" + id;
         assertNotNull(topic);
         assertThat(topic.getId(), is(id.toString()));
         assertThat(topic.getTitle(), is(title));
         assertThat(topic.getUniqueId(), is(uniqueId));
         // and the right number of relationships exist
         assertThat(parser.getRelationships().size(), is(1));
         assertThat(parser.getRelationships().get(uniqueId).size(), is(2));
         // and the relationships have the right main topic id
         assertThat(parser.getRelationships().get(uniqueId).get(0).getMainRelationshipTopicId(), is(uniqueId));
         assertThat(parser.getRelationships().get(uniqueId).get(1).getMainRelationshipTopicId(), is(uniqueId));
         // and the relationship type is correct
         assertThat(parser.getRelationships().get(uniqueId).get(0).getType(), is(RelationshipType.PREREQUISITE));
         assertThat(parser.getRelationships().get(uniqueId).get(1).getType(), is(RelationshipType.PREREQUISITE));
     }
 
     @Test
     public void shouldParseTopicWithShortLinkListRelationship() {
         // Given a valid relationship
         List<String> topics = Arrays.asList(id.toString(), "T0");
         String linkListRelationship = make(
                 a(TopicRelationshipStringMaker.TopicRelationshipString, with(TopicRelationshipStringMaker.relationshipType, "L"),
                         with(TopicRelationshipStringMaker.relationships, topics)));
         // and a string that represents a topic title and id
         String topicString = make(
                 a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title), with(TopicStringMaker.id, id.toString()),
                         with(TopicStringMaker.relationship, linkListRelationship)));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
         } catch (ParsingException e) {
             fail("Parsing should not have failed.");
         }
 
         // Then check that the topic has the right data set
         String uniqueId = "L" + randomNumber + "-" + id;
         assertNotNull(topic);
         assertThat(topic.getId(), is(id.toString()));
         assertThat(topic.getTitle(), is(title));
         assertThat(topic.getUniqueId(), is(uniqueId));
         // and the right number of relationships exist
         assertThat(parser.getRelationships().size(), is(1));
         assertThat(parser.getRelationships().get(uniqueId).size(), is(2));
         // and the relationships have the right main topic id
         assertThat(parser.getRelationships().get(uniqueId).get(0).getMainRelationshipTopicId(), is(uniqueId));
         assertThat(parser.getRelationships().get(uniqueId).get(1).getMainRelationshipTopicId(), is(uniqueId));
         // and the relationship type is correct
         assertThat(parser.getRelationships().get(uniqueId).get(0).getType(), is(RelationshipType.LINKLIST));
         assertThat(parser.getRelationships().get(uniqueId).get(1).getType(), is(RelationshipType.LINKLIST));
     }
 
     @Test
     public void shouldParseTopicWithLongLinkListRelationship() {
         // Given a valid relationship
         List<String> topics = Arrays.asList("Test [" + id + "]", "T0");
         String linkListRelationship = make(
                 a(TopicRelationshipStringMaker.TopicRelationshipString, with(TopicRelationshipStringMaker.relationshipType, "Link-List"),
                         with(TopicRelationshipStringMaker.longRelationship, true),
                         with(TopicRelationshipStringMaker.relationships, topics)));
         // and the relationship is setup in the lines
         parser.getLines().addAll(Arrays.asList(linkListRelationship.split("\n")));
         // and a string that represents a topic title and id
         String topicString = make(
                 a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title), with(TopicStringMaker.id, id.toString())));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
         } catch (ParsingException e) {
             fail("Parsing should not have failed.");
         }
 
         // Then check that the topic has the right data set
         String uniqueId = "L" + randomNumber + "-" + id;
         assertNotNull(topic);
         assertThat(topic.getId(), is(id.toString()));
         assertThat(topic.getTitle(), is(title));
         assertThat(topic.getUniqueId(), is(uniqueId));
         // and the right number of relationships exist
         assertThat(parser.getRelationships().size(), is(1));
         assertThat(parser.getRelationships().get(uniqueId).size(), is(2));
         // and the relationships have the right main topic id
         assertThat(parser.getRelationships().get(uniqueId).get(0).getMainRelationshipTopicId(), is(uniqueId));
         assertThat(parser.getRelationships().get(uniqueId).get(1).getMainRelationshipTopicId(), is(uniqueId));
         // and the relationship type is correct
         assertThat(parser.getRelationships().get(uniqueId).get(0).getType(), is(RelationshipType.LINKLIST));
         assertThat(parser.getRelationships().get(uniqueId).get(1).getType(), is(RelationshipType.LINKLIST));
     }
 
     @Test
     public void shouldParseTopicWithMultipleLongRelationships() {
         // Given two long relationships
         List<String> topics = Arrays.asList("Test [" + id + "]", "T0");
         List<String> topics2 = Arrays.asList(id.toString(), "Test [T0]");
         String refersToRelationship = make(
                 a(TopicRelationshipStringMaker.TopicRelationshipString, with(TopicRelationshipStringMaker.relationshipType, "Refer-To"),
                         with(TopicRelationshipStringMaker.longRelationship, true),
                         with(TopicRelationshipStringMaker.relationships, topics)));
         String linkListRelationship = make(
                 a(TopicRelationshipStringMaker.TopicRelationshipString, with(TopicRelationshipStringMaker.relationshipType, "Link-List"),
                         with(TopicRelationshipStringMaker.longRelationship, true),
                         with(TopicRelationshipStringMaker.relationships, topics2)));
         // and the relationship is setup in the lines
         parser.getLines().addAll(Arrays.asList((refersToRelationship + linkListRelationship).split("\n")));
         // and a string that represents a topic title and id
         String topicString = make(
                 a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title), with(TopicStringMaker.id, id.toString())));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
         } catch (ParsingException e) {
             fail("Parsing should not have failed.");
         }
 
         // Then check that the topic has the right data set
         String uniqueId = "L" + randomNumber + "-" + id;
         assertNotNull(topic);
         assertThat(topic.getId(), is(id.toString()));
         assertThat(topic.getTitle(), is(title));
         assertThat(topic.getUniqueId(), is(uniqueId));
         // and the right number of relationships exist
         assertThat(parser.getRelationships().size(), is(1));
         assertThat(parser.getRelationships().get(uniqueId).size(), is(4));
         // and the relationships have the right main topic id
         assertThat(parser.getRelationships().get(uniqueId).get(0).getMainRelationshipTopicId(), is(uniqueId));
         assertThat(parser.getRelationships().get(uniqueId).get(1).getMainRelationshipTopicId(), is(uniqueId));
         assertThat(parser.getRelationships().get(uniqueId).get(2).getMainRelationshipTopicId(), is(uniqueId));
         assertThat(parser.getRelationships().get(uniqueId).get(3).getMainRelationshipTopicId(), is(uniqueId));
         // and the relationship type is correct
         assertThat(parser.getRelationships().get(uniqueId).get(0).getType(), is(RelationshipType.REFER_TO));
         assertThat(parser.getRelationships().get(uniqueId).get(1).getType(), is(RelationshipType.REFER_TO));
         assertThat(parser.getRelationships().get(uniqueId).get(2).getType(), is(RelationshipType.LINKLIST));
         assertThat(parser.getRelationships().get(uniqueId).get(3).getType(), is(RelationshipType.LINKLIST));
     }
 
     @Test
     public void shouldThrowExceptionWithShortRelationshipWithMissingVariable() {
         // Given an invalid refers to relationship
         List<String> topics = Arrays.asList(id.toString(), randomNumber.toString());
         String refersToRelationship = make(
                 a(TopicRelationshipStringMaker.TopicRelationshipString, with(TopicRelationshipStringMaker.relationshipType, "R"),
                         with(TopicRelationshipStringMaker.missingVariable, true),
                         with(TopicRelationshipStringMaker.relationships, topics)));
         // and a string that represents a topic title, id and a missing variable
         String topicString = make(
                 a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title), with(TopicStringMaker.id, id.toString()),
                         with(TopicStringMaker.relationship, refersToRelationship)));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
             fail("Parsing the topic should have thrown an exception.");
         } catch (ParsingException e) {
             assertThat(e.getMessage(),
                     containsString("Line " + lineNumber + ": Invalid Content Specification! Missing attribute detected."));
         }
     }
 
     @Test
     public void shouldThrowExceptionWithShortRelationshipWithMissingSeparator() {
         // Given an invalid refers to relationship
         List<String> topics = Arrays.asList(id.toString(), randomNumber.toString());
         String refersToRelationship = make(
                 a(TopicRelationshipStringMaker.TopicRelationshipString, with(TopicRelationshipStringMaker.relationshipType, "R"),
                         with(TopicRelationshipStringMaker.missingSeparator, true),
                         with(TopicRelationshipStringMaker.relationships, topics)));
         // and a string that represents a topic title, id and a missing variable
         String topicString = make(
                 a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title), with(TopicStringMaker.id, id.toString()),
                         with(TopicStringMaker.relationship, refersToRelationship)));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
             fail("Parsing the topic should have thrown an exception.");
         } catch (ParsingException e) {
             assertThat(e.getMessage(),
                     containsString("Line " + lineNumber + ": Invalid Content Specification! Missing separator(,) detected."));
         }
     }
 
     @Test
     public void shouldThrowExceptionWithShortRelationshipWithMissingOpeningBracket() {
         // Given an invalid refers to relationship
         List<String> topics = Arrays.asList(id.toString(), randomNumber.toString());
         String refersToRelationship = make(
                 a(TopicRelationshipStringMaker.TopicRelationshipString, with(TopicRelationshipStringMaker.relationshipType, "R"),
                         with(TopicRelationshipStringMaker.missingOpeningBracket, true),
                         with(TopicRelationshipStringMaker.relationships, topics)));
         // and a string that represents a topic title, id and a missing variable
         String topicString = make(
                 a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title), with(TopicStringMaker.id, id.toString()),
                         with(TopicStringMaker.relationship, refersToRelationship)));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
             fail("Parsing the topic should have thrown an exception.");
         } catch (ParsingException e) {
             assertThat(e.getMessage(),
                     containsString("Line " + lineNumber + ": Invalid Content Specification! Missing opening bracket ([) detected."));
         }
     }
 
     @Test
     public void shouldThrowExceptionWithShortRelationshipWithMissingClosingBracket() {
         // Given an invalid refers to relationship
         List<String> topics = Arrays.asList(id.toString(), randomNumber.toString());
         String refersToRelationship = make(
                 a(TopicRelationshipStringMaker.TopicRelationshipString, with(TopicRelationshipStringMaker.relationshipType, "R"),
                         with(TopicRelationshipStringMaker.missingClosingBracket, true),
                         with(TopicRelationshipStringMaker.relationships, topics)));
         // and a string that represents a topic title, id and a missing variable
         String topicString = make(
                 a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title), with(TopicStringMaker.id, id.toString()),
                         with(TopicStringMaker.relationship, refersToRelationship)));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
             fail("Parsing the topic should have thrown an exception.");
         } catch (ParsingException e) {
             assertThat(e.getMessage(),
                     containsString("Line " + lineNumber + ": Invalid Content Specification! Missing ending bracket (]) detected."));
         }
     }
 
     @Test
     public void shouldThrowExceptionWithLongRelationshipWithMissingVariable() {
         // Given an invalid refers to relationship
         List<String> topics = Arrays.asList(id.toString(), randomNumber.toString());
         String refersToRelationship = make(
                 a(TopicRelationshipStringMaker.TopicRelationshipString, with(TopicRelationshipStringMaker.relationshipType, "R"),
                         with(TopicRelationshipStringMaker.longRelationship, true), with(TopicRelationshipStringMaker.missingVariable, true),
                         with(TopicRelationshipStringMaker.relationships, topics)));
         // and the relationship is setup in the lines
         parser.getLines().addAll(Arrays.asList(refersToRelationship.split("\n")));
         // and a string that represents a topic title, id and a missing variable
         String topicString = make(
                 a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title), with(TopicStringMaker.id, id.toString())));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
             fail("Parsing the topic should have thrown an exception.");
         } catch (ParsingException e) {
             assertThat(e.getMessage(),
                     containsString("Line " + lineNumber + ": Invalid Content Specification! Missing attribute detected."));
         }
     }
 
     @Test
     public void shouldThrowExceptionWithLongRelationshipWithMissingSeparator() {
         // Given an invalid refers to relationship
         List<String> topics = Arrays.asList(id.toString(), randomNumber.toString());
         String refersToRelationship = make(
                 a(TopicRelationshipStringMaker.TopicRelationshipString, with(TopicRelationshipStringMaker.relationshipType, "R"),
                         with(TopicRelationshipStringMaker.longRelationship, true),
                         with(TopicRelationshipStringMaker.missingSeparator, true),
                         with(TopicRelationshipStringMaker.relationships, topics)));
         // and the relationship is setup in the lines
         parser.getLines().addAll(Arrays.asList(refersToRelationship.split("\n")));
         // and a string that represents a topic title, id and a missing variable
         String topicString = make(
                 a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title), with(TopicStringMaker.id, id.toString())));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
             fail("Parsing the topic should have thrown an exception.");
         } catch (ParsingException e) {
             assertThat(e.getMessage(),
                     containsString("Line " + lineNumber + ": Invalid Content Specification! Missing separator(,) detected."));
         }
     }
 
     @Test
     public void shouldThrowExceptionWithLongRelationshipWithMissingOpeningBracket() {
         // Given an invalid refers to relationship
         List<String> topics = Arrays.asList(id.toString(), randomNumber.toString());
         String refersToRelationship = make(
                 a(TopicRelationshipStringMaker.TopicRelationshipString, with(TopicRelationshipStringMaker.relationshipType, "R"),
                         with(TopicRelationshipStringMaker.longRelationship, true),
                         with(TopicRelationshipStringMaker.missingOpeningBracket, true),
                         with(TopicRelationshipStringMaker.relationships, topics)));
         // and the relationship is setup in the lines
         String[] lines = refersToRelationship.split("\n");
         parser.getLines().addAll(Arrays.asList(lines).subList(1, lines.length));
         // and a string that represents a topic title, id and a missing variable
         String topicString = make(
                 a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title), with(TopicStringMaker.id, id.toString()),
                         with(TopicStringMaker.relationship, lines[0])));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
             fail("Parsing the topic should have thrown an exception.");
         } catch (ParsingException e) {
             assertThat(e.getMessage(),
                     containsString("Line " + lineNumber + ": Invalid Content Specification! Missing opening bracket ([) detected."));
         }
     }
 
     @Test
     public void shouldThrowExceptionWithLongRelationshipWithMissingClosingBracket() {
         // Given an invalid refers to relationship
         List<String> topics = Arrays.asList(id.toString(), randomNumber.toString());
         String refersToRelationship = make(
                 a(TopicRelationshipStringMaker.TopicRelationshipString, with(TopicRelationshipStringMaker.relationshipType, "R"),
                         with(TopicRelationshipStringMaker.longRelationship, true),
                         with(TopicRelationshipStringMaker.missingClosingBracket, true),
                         with(TopicRelationshipStringMaker.relationships, topics)));
         // and the relationship is setup in the lines
         parser.getLines().addAll(Arrays.asList(refersToRelationship.split("\n")));
         // and a string that represents a topic title, id and a missing variable
         String topicString = make(
                 a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title), with(TopicStringMaker.id, id.toString())));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
             fail("Parsing the topic should have thrown an exception.");
         } catch (ParsingException e) {
             assertThat(e.getMessage(), containsString("Line " + lineNumber + ": Invalid Content Specification! Missing ending bracket (])" +
                     " detected."));
         }
     }
 
     @Test
     public void shouldThrowExceptionWithNextRelationship() {
         // Given an invalid refers to relationship
         List<String> topics = Arrays.asList(id.toString());
         String nextRelationship = make(
                 a(TopicRelationshipStringMaker.TopicRelationshipString, with(TopicRelationshipStringMaker.relationshipType, "Next:"),
                         with(TopicRelationshipStringMaker.relationships, topics)));
         // and a string that represents a topic title, id and a missing variable
         String topicString = make(
                 a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title), with(TopicStringMaker.id, id.toString()),
                         with(TopicStringMaker.relationship, nextRelationship)));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
             fail("Parsing the topic should have thrown an exception.");
         } catch (ParsingException e) {
             assertThat(e.getMessage(), containsString(
                     "Line " + lineNumber + ": Invalid Topic! Next and Previous relationships can't be used directly. If you wish to use " +
                             "next/previous then please use a Process."));
         }
     }
 
     @Test
     public void shouldThrowExceptionWithPreviousRelationship() {
         // Given an invalid refers to relationship
         List<String> topics = Arrays.asList(id.toString());
         String nextRelationship = make(
                 a(TopicRelationshipStringMaker.TopicRelationshipString, with(TopicRelationshipStringMaker.relationshipType, "Prev:"),
                         with(TopicRelationshipStringMaker.relationships, topics)));
         // and a string that represents a topic title, id and a missing variable
         String topicString = make(
                 a(TopicStringMaker.TopicString, with(TopicStringMaker.title, title), with(TopicStringMaker.id, id.toString()),
                         with(TopicStringMaker.relationship, nextRelationship)));
         // and a line number
         int lineNumber = randomNumber;
 
         // When parsing the topic string
         SpecTopic topic = null;
         try {
             topic = parser.parseTopic(topicString, lineNumber);
             fail("Parsing the topic should have thrown an exception.");
         } catch (ParsingException e) {
             assertThat(e.getMessage(), containsString(
                     "Line " + lineNumber + ": Invalid Topic! Next and Previous relationships can't be used directly. If you wish to use " +
                             "next/previous then please use a Process."));
         }
     }
 }
