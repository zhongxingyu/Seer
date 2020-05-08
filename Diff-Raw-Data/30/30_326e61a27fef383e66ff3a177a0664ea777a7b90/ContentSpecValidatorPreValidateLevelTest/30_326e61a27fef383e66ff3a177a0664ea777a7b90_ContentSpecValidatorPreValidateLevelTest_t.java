 package org.jboss.pressgang.ccms.contentspec.processor;
 
 import static com.natpryce.makeiteasy.MakeItEasy.a;
 import static com.natpryce.makeiteasy.MakeItEasy.make;
 import static com.natpryce.makeiteasy.MakeItEasy.with;
 import static java.util.Arrays.asList;
 import static org.hamcrest.core.StringContains.containsString;
 import static org.jboss.pressgang.ccms.contentspec.TestUtil.selectRandomLevelType;
 import static org.jboss.pressgang.ccms.contentspec.TestUtil.selectRandomListItem;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertThat;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import net.sf.ipsedixit.annotation.Arbitrary;
 import org.jboss.pressgang.ccms.contentspec.ContentSpec;
 import org.jboss.pressgang.ccms.contentspec.InitialContent;
 import org.jboss.pressgang.ccms.contentspec.Level;
 import org.jboss.pressgang.ccms.contentspec.SpecTopic;
import org.jboss.pressgang.ccms.contentspec.constants.CSConstants;
 import org.jboss.pressgang.ccms.contentspec.enums.BookType;
 import org.jboss.pressgang.ccms.contentspec.enums.LevelType;
 import org.jboss.pressgang.ccms.contentspec.test.makers.shared.LevelMaker;
 import org.jboss.pressgang.ccms.contentspec.test.makers.shared.SpecTopicMaker;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 
 public class ContentSpecValidatorPreValidateLevelTest extends ContentSpecValidatorTest {
     @Arbitrary BookType bookType;
 
     @Mock ContentSpec contentSpec;
 
     Map<String, SpecTopic> specTopicMap;
 
     @Before
     public void setUp() {
         specTopicMap = new HashMap<String, SpecTopic>();
         super.setUp();
     }
 
     @Test
     public void shouldFailAndLogErrorWhenNoLevelType() {
         // Given a Level without a level type
         Level level = make(a(LevelMaker.Level, with(LevelMaker.levelType, (LevelType) null)));
         // and a child level so that it is otherwise valid
         addTopicToLevel(level);
 
         // When validating the level
         boolean result = validator.preValidateLevel(level, specTopicMap, false, bookType, contentSpec);
 
         // Then the result should be false
         assertFalse(result);
         // and an error should have been printed
         assertThat(logger.getLogMessages().toString(),
                 containsString("An error occurred during processing please try again and if another failure occurs please log a bug."));
     }
 
     @Test
     public void shouldFailAndLogErrorWhenNoChildren() {
         // Given a level that isn't a part
         ArrayList<LevelType> levelTypes = new ArrayList<LevelType>(asList(LevelType.values()));
         levelTypes.remove(LevelType.PART);
         LevelType levelType = selectRandomListItem(levelTypes);
         Level level = make(a(LevelMaker.Level, with(LevelMaker.levelType, levelType)));
         // and a parent level
         addParentToLevel(level, LevelType.BASE);
 
         // When validating the level
         boolean result = validator.preValidateLevel(level, specTopicMap, false, bookType, contentSpec);
 
         // Then the result should be false
         assertFalse(result);
         // and an error should have been printed
         assertThat(logger.getLogMessages().toString(), containsString("Invalid " + levelType.getTitle() + "! No topics or levels in this " +
                 "" + levelType.getTitle() + "."));
     }
 
     @Test
     public void shouldFailAndLogErrorWhenNoChildrenLevelsForPart() {
         // Given a level that is a part
         Level level = make(a(LevelMaker.Level, with(LevelMaker.levelType, LevelType.PART)));
         // and a parent level
         addParentToLevel(level, LevelType.BASE);
 
         // When validating the level
         boolean result = validator.preValidateLevel(level, specTopicMap, false, bookType, contentSpec);
 
         // Then the result should be false
         assertFalse(result);
         // and an error should have been printed
         assertThat(logger.getLogMessages().toString(), containsString("Invalid Part! No levels in this Part."));
     }
 
     @Test
     public void shouldFailAndLogErrorWhenOnlyInitialContentForSections() {
         // Given a section level
         Level level = make(a(LevelMaker.Level, with(LevelMaker.levelType, LevelType.SECTION)));
         // with an initial content child
         InitialContent initialContent = new InitialContent();
         addTopicToLevel(initialContent);
         level.appendChild(initialContent);
         // and a parent level
         addParentToLevel(level, LevelType.CHAPTER);
 
         // When validating the level
         boolean result = validator.preValidateLevel(level, specTopicMap, false, BookType.BOOK, contentSpec);
 
         // Then the result should be false
         assertFalse(result);
         // and an error should have been printed
        assertThat(logger.getLogMessages().toString(), containsString("Invalid Section! No levels in this " +
        "Section and only one " + CSConstants.LEVEL_INITIAL_CONTENT + " topic. Sections with only an " + CSConstants
                .LEVEL_INITIAL_CONTENT + " topic are just ordinary topics, so please use the regular topic syntax."));
     }
 
     @Test
     public void shouldFailAndLogErrorWhenNoTitle() {
         // Given a level with no title
         LevelType levelType = selectRandomLevelType();
         Level level = make(a(LevelMaker.Level, with(LevelMaker.levelType, levelType), with(LevelMaker.title, (String) null)));
         // and a parent level
         addParentToLevel(level, LevelType.BASE);
 
         // When validating the level
         boolean result = validator.preValidateLevel(level, specTopicMap, false, bookType, contentSpec);
 
         // Then the result should be false
         assertFalse(result);
         // and an error should have been printed
         assertThat(logger.getLogMessages().toString(), containsString("Invalid " + levelType.getTitle() + "! No title."));
     }
 
     @Test
     public void shouldFailWhenChildLevelIsNotValid() {
         // Given a valid level
         Level level = make(a(LevelMaker.Level, with(LevelMaker.levelType, LevelType.CHAPTER)));
         // and a parent level
         addParentToLevel(level, LevelType.BASE);
         // and an invalid child level
         Level child = make(a(LevelMaker.Level, with(LevelMaker.levelType, LevelType.SECTION)));
         level.appendChild(child);
 
         // When validating the level
         boolean result = validator.preValidateLevel(level, specTopicMap, false, BookType.BOOK, contentSpec);
 
         // Then the result should be false
         assertFalse(result);
     }
 
     @Test
     public void shouldFailWhenChildTopicIsNotValid() {
         // Given a valid level
         Level level = make(a(LevelMaker.Level, with(LevelMaker.levelType, LevelType.CHAPTER)));
         // and a parent level
         addParentToLevel(level, LevelType.BASE);
         // and an invalid child topic
         SpecTopic topic = make(a(SpecTopicMaker.SpecTopic, with(SpecTopicMaker.title, (String) null)));
         level.appendChild(topic);
 
         // When validating the level
         boolean result = validator.preValidateLevel(level, specTopicMap, false, BookType.BOOK, contentSpec);
 
         // Then the result should be false
         assertFalse(result);
     }
 
     @Test
     public void shouldFailAndLogErrorWhenAppendixUsedOutsideOfBaseLevelForArticle() {
         // Given an appendix
         Level appendix = make(a(LevelMaker.Level, with(LevelMaker.levelType, LevelType.APPENDIX)));
         // and a parent level that is a part
         addParentToLevel(appendix, LevelType.PART);
         // and a child node
         addTopicToLevel(appendix);
 
         // When validating the level
         boolean result = validator.preValidateLevel(appendix, specTopicMap, false, BookType.ARTICLE, contentSpec);
 
         // Then the result should be false
         assertFalse(result);
         // and an error should have been printed
         assertThat(logger.getLogMessages().toString(),
                 containsString("Invalid Content Specification! An Appendix must have no indentation."));
     }
 
     @Test
     public void shouldFailAndLogErrorWhenAppendixIsNotLastForArticle() {
         // Given an appendix
         Level appendix = make(a(LevelMaker.Level, with(LevelMaker.levelType, LevelType.APPENDIX)));
         // and a parent level
         addParentToLevel(appendix, LevelType.BASE);
         // and a child node
         addTopicToLevel(appendix);
         // and another level after the appendix
         Level chapter = make(a(LevelMaker.Level, with(LevelMaker.levelType, LevelType.CHAPTER)));
         appendix.getParent().appendChild(chapter);
 
         // When validating the level
         boolean result = validator.preValidateLevel(appendix, specTopicMap, false, BookType.ARTICLE_DRAFT, contentSpec);
 
         // Then the result should be false
         assertFalse(result);
         // and an error should have been printed
         assertThat(logger.getLogMessages().toString(),
                 containsString("Invalid Content Specification! An Appendix must be at the end of the content specification."));
     }
 
     @Test
     public void shouldFailAndLogErrorWhenAppendixUsedOutsideOfBaseLevelOrPartForBook() {
         // Given an appendix
         Level appendix = make(a(LevelMaker.Level, with(LevelMaker.levelType, LevelType.APPENDIX)));
         // and a parent level that is a part
         addParentToLevel(appendix, LevelType.CHAPTER);
         // and a child node
         addTopicToLevel(appendix);
 
         // When validating the level
         boolean result = validator.preValidateLevel(appendix, specTopicMap, false, BookType.BOOK_DRAFT, contentSpec);
 
         // Then the result should be false
         assertFalse(result);
         // and an error should have been printed
         assertThat(logger.getLogMessages().toString(),
                 containsString("Invalid Content Specification! An Appendix must be within a " + "\"Part\" or have no indentation."));
     }
 
     @Test
     public void shouldFailAndLogErrorWhenAppendixIsNotLastForBook() {
         // Given an appendix
         Level appendix = make(a(LevelMaker.Level, with(LevelMaker.levelType, LevelType.APPENDIX)));
         // and a parent level
         addParentToLevel(appendix, LevelType.BASE);
         // and a child node
         addTopicToLevel(appendix);
         // and another level after the appendix
         Level chapter = make(a(LevelMaker.Level, with(LevelMaker.levelType, LevelType.CHAPTER)));
         appendix.getParent().appendChild(chapter);
 
         // When validating the level
         boolean result = validator.preValidateLevel(appendix, specTopicMap, false, BookType.BOOK, contentSpec);
 
         // Then the result should be false
         assertFalse(result);
         // and an error should have been printed
         assertThat(logger.getLogMessages().toString(),
                 containsString("Invalid Content Specification! An Appendix must be at the end of the content specification."));
     }
 
     @Test
     public void shouldFailAndLogErrorWhenChapterUsedInArticle() {
         // Given a chapter
         Level chapter = make(a(LevelMaker.Level, with(LevelMaker.levelType, LevelType.CHAPTER)));
         // and a parent level that is a part
         addParentToLevel(chapter, LevelType.PART);
         // and a child node
         addTopicToLevel(chapter);
 
         // When validating the level
         boolean result = validator.preValidateLevel(chapter, specTopicMap, false, BookType.ARTICLE, contentSpec);
 
         // Then the result should be false
         assertFalse(result);
         // and an error should have been printed
         assertThat(logger.getLogMessages().toString(),
                 containsString("Invalid Content Specification! Chapters can't be used in Articles."));
     }
 
     @Test
     public void shouldFailAndLogErrorWhenChapterUsedOutsideOfBaseLevelOrPartForBook() {
         // Given a chapter
         Level chapter = make(a(LevelMaker.Level, with(LevelMaker.levelType, LevelType.CHAPTER)));
         // and a parent level that is a chapter
         addParentToLevel(chapter, LevelType.CHAPTER);
         // and a child node
         addTopicToLevel(chapter);
 
         // When validating the level
         boolean result = validator.preValidateLevel(chapter, specTopicMap, false, BookType.BOOK, contentSpec);
 
         // Then the result should be false
         assertFalse(result);
         // and an error should have been printed
         assertThat(logger.getLogMessages().toString(),
                 containsString("Invalid Content Specification! A Chapter must be within a " + "\"Part\" or have no indentation."));
     }
 
     @Test
     public void shouldFailAndLogErrorWhenPartUsedInArticle() {
         // Given a part
         Level part = make(a(LevelMaker.Level, with(LevelMaker.levelType, LevelType.PART)));
         // and a parent level
         addParentToLevel(part, LevelType.BASE);
         // and a child node
         addTopicToLevel(part);
 
         // When validating the level
         boolean result = validator.preValidateLevel(part, specTopicMap, false, BookType.ARTICLE, contentSpec);
 
         // Then the result should be false
         assertFalse(result);
         // and an error should have been printed
         assertThat(logger.getLogMessages().toString(), containsString("Invalid Content Specification! Parts can't be used in Articles."));
     }
 
     @Test
     public void shouldFailAndLogErrorWhenPartUsedOutsideOfBaseLevelForBook() {
         // Given a part
         Level part = make(a(LevelMaker.Level, with(LevelMaker.levelType, LevelType.PART)));
         // and a parent level that is a chapter
         addParentToLevel(part, LevelType.CHAPTER);
         // and a child node
         addTopicToLevel(part);
 
         // When validating the level
         boolean result = validator.preValidateLevel(part, specTopicMap, false, BookType.BOOK, contentSpec);
 
         // Then the result should be false
         assertFalse(result);
         // and an error should have been printed
         assertThat(logger.getLogMessages().toString(), containsString("Invalid Content Specification! A Part must have no indentation."));
     }
 
     @Test
     public void shouldFailAndLogErrorWhenPrefaceUsedInArticle() {
         // Given a preface
         Level preface = make(a(LevelMaker.Level, with(LevelMaker.levelType, LevelType.PREFACE)));
         // and a parent level that is a part
         addParentToLevel(preface, LevelType.PART);
         // and a child node
         addTopicToLevel(preface);
 
         // When validating the level
         boolean result = validator.preValidateLevel(preface, specTopicMap, false, BookType.ARTICLE, contentSpec);
 
         // Then the result should be false
         assertFalse(result);
         // and an error should have been printed
         assertThat(logger.getLogMessages().toString(),
                 containsString("Invalid Content Specification! Prefaces can't be used in Articles."));
     }
 
     @Test
     public void shouldFailAndLogErrorWhenPrefaceUsedOutsideOfBaseLevelOrPartForBook() {
         // Given a preface
         Level preface = make(a(LevelMaker.Level, with(LevelMaker.levelType, LevelType.PREFACE)));
         // and a parent level that is a chapter
         addParentToLevel(preface, LevelType.CHAPTER);
         // and a child node
         addTopicToLevel(preface);
 
         // When validating the level
         boolean result = validator.preValidateLevel(preface, specTopicMap, false, BookType.BOOK, contentSpec);
 
         // Then the result should be false
         assertFalse(result);
         // and an error should have been printed
         assertThat(logger.getLogMessages().toString(),
                 containsString("Invalid Content Specification! A Preface must be within a " + "\"Part\" or have no indentation."));
     }
 
     @Test
     public void shouldFailAndLogErrorWhenProcessUsedInArticle() {
         // Given a preface
         Level process = make(a(LevelMaker.Level, with(LevelMaker.levelType, LevelType.PROCESS)));
         // and a parent level that is a part
         addParentToLevel(process, LevelType.PART);
         // and a child node
         addTopicToLevel(process);
 
         // When validating the level
         boolean result = validator.preValidateLevel(process, specTopicMap, false, BookType.ARTICLE, contentSpec);
 
         // Then the result should be false
         assertFalse(result);
         // and an error should have been printed
         assertThat(logger.getLogMessages().toString(),
                 containsString("Invalid Content Specification! Processes can't be used in Articles" + "."));
     }
 
     @Test
     public void shouldFailAndLogErrorWhenProcessHasChildrenForBook() {
         // Given a preface
         Level process = make(a(LevelMaker.Level, with(LevelMaker.levelType, LevelType.PROCESS)));
         // and a parent level that is a chapter
         addParentToLevel(process, LevelType.CHAPTER);
         // and a child level
         addChildToLevel(process);
 
         // When validating the level
         boolean result = validator.preValidateLevel(process, specTopicMap, false, BookType.BOOK, contentSpec);
 
         // Then the result should be false
         assertFalse(result);
         // and an error should have been printed
         assertThat(logger.getLogMessages().toString(), containsString(
                 "Invalid Process! A process cannot contain Chapters, " + "Sections, Appendixes, Prefaces or other Processes."));
     }
 
     @Test
     public void shouldFailAndLogErrorWhenSectionUsedOutsideOfBaseLevelOrSectionForArticle() {
         // Given a section
         Level section = make(a(LevelMaker.Level, with(LevelMaker.levelType, LevelType.SECTION)));
         // and a parent level that is a part
         addParentToLevel(section, LevelType.PART);
         // and a child node
         addTopicToLevel(section);
 
         // When validating the level
         boolean result = validator.preValidateLevel(section, specTopicMap, false, BookType.ARTICLE, contentSpec);
 
         // Then the result should be false
         assertFalse(result);
         // and an error should have been printed
         assertThat(logger.getLogMessages().toString(),
                 containsString("Invalid Content Specification! A Section must be within another section."));
     }
 
     @Test
     public void shouldFailAndLogErrorWhenSectionUsedAtBaseLevelForBook() {
         // Given a section
         Level section = make(a(LevelMaker.Level, with(LevelMaker.levelType, LevelType.SECTION)));
         // and a parent level
         addParentToLevel(section, LevelType.BASE);
         // and a child node
         addTopicToLevel(section);
 
         // When validating the level
         boolean result = validator.preValidateLevel(section, specTopicMap, false, BookType.BOOK, contentSpec);
 
         // Then the result should be false
         assertFalse(result);
         // and an error should have been printed
         assertThat(logger.getLogMessages().toString(),
                 containsString("Invalid Content Specification! A Section must be within a Chapter, Preface, Article or Appendix."));
     }
 
     @Test
     public void shouldFailAndLogErrorWhenInitialContentIsNotFirstForArticle() {
         // Given an initial content container
         Level initialContent = make(a(LevelMaker.Level, with(LevelMaker.levelType, LevelType.INITIAL_CONTENT)));
         // and a parent level that is a part
         addParentToLevel(initialContent, LevelType.SECTION);
         // and a child node
         addTopicToLevel(initialContent);
         // and another level before the initial content
         Level section = make(a(LevelMaker.Level, with(LevelMaker.levelType, LevelType.SECTION)));
         initialContent.getParent().insertBefore(section, initialContent);
 
         // When validating the level
         boolean result = validator.preValidateLevel(initialContent, specTopicMap, false, BookType.ARTICLE, contentSpec);
 
         // Then the result should be false
         assertFalse(result);
         // and an error should have been printed
         assertThat(logger.getLogMessages().toString(),
                 containsString("Invalid Content Specification! Initial Text must be at the start of the Section."));
     }
 
     @Test
     public void shouldFailAndLogErrorWhenInitialContentIsNotFirstForBook() {
         // Given an initial content container
         Level initialContent = make(a(LevelMaker.Level, with(LevelMaker.levelType, LevelType.INITIAL_CONTENT)));
         // and a parent level
         addParentToLevel(initialContent, LevelType.BASE);
         // and a child node
         addTopicToLevel(initialContent);
         // and another level before the initial content
         Level chapter = make(a(LevelMaker.Level, with(LevelMaker.levelType, LevelType.CHAPTER)));
         initialContent.getParent().insertBefore(chapter, initialContent);
 
         // When validating the level
         boolean result = validator.preValidateLevel(initialContent, specTopicMap, false, BookType.BOOK, contentSpec);
 
         // Then the result should be false
         assertFalse(result);
         // and an error should have been printed
         assertThat(logger.getLogMessages().toString(),
                 containsString("Invalid Content Specification! Initial Text must be at the start of the Content Specification."));
     }
 
     private void addTopicToLevel(final Level level) {
         // with a spec topic
         level.appendChild(make(a(SpecTopicMaker.SpecTopic)));
     }
 
     private void addChildToLevel(final Level level) {
         // with a spec topic
         level.appendChild(make(a(LevelMaker.Level, with(LevelMaker.levelType, LevelType.SECTION))));
     }
 
     private void addParentToLevel(final Level level, final LevelType parentType) {
         Level parent = make(a(LevelMaker.Level, with(LevelMaker.levelType, parentType)));
         parent.appendChild(level);
     }
 }
