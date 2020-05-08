 package at.yawk.fimfiction;
 
 import java.io.ByteArrayOutputStream;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import lombok.SneakyThrows;
 
 import org.ccil.cowan.tagsoup.Parser;
 import org.junit.Assert;
 import org.junit.Assume;
 import org.junit.Before;
 import org.junit.Test;
 import org.xml.sax.Attributes;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.DefaultHandler;
 
 import at.yawk.fimfiction.Story.FavoriteState;
 import at.yawk.fimfiction.html.FullSearchParser;
 import at.yawk.fimfiction.operation.AbstractDownloadOperation.DownloadType;
 import at.yawk.fimfiction.operation.ChapterDownloadOperation;
 import at.yawk.fimfiction.operation.DiscardSessionOperation;
 import at.yawk.fimfiction.operation.FavoriteOperation;
 import at.yawk.fimfiction.operation.GetLoggedInUserOperation;
 import at.yawk.fimfiction.operation.GetStoryMetaOperation;
 import at.yawk.fimfiction.operation.LoginOperation;
 import at.yawk.fimfiction.operation.LogoutOperation;
 import at.yawk.fimfiction.operation.ReadLaterOperation;
 import at.yawk.fimfiction.operation.SearchRequest;
 import at.yawk.fimfiction.operation.StoryDownloadOperation;
 
 public class OperationTests {
     private static final int STORY_ID = 10;
     private static final String STORY_TITLE = "Accolade";
     private static final String STORY_URL = "http://www.fimfiction.net/story/10/accolade";
     private static final String STORY_DESCRIPTION_HTML = "<p>Luna and Trixie both want Twilight Sparkle gone. Will teaming up solve their problem once and for all?</p>";
     private static final String STORY_DESCRIPTION_HTML2 = "<p>Luna and Trixie both want Twilight Sparkle gone. Will teaming up solve their problem once and for all?</b></u></i></center></p>";
     private static final String STORY_DESCRIPTION_BB = "Luna and Trixie both want Twilight Sparkle gone. Will teaming up solve their problem once and for all?";
     private static final long STORY_FIRST_POSTED = 1312322400000L;
     private static final long STORY_UPDATED = 1310335200000L;
     private static final String STORY_THUMBNAIL_URL = "http://www.fimfiction-static.net/images/story_images/10_r.jpg";
     private static final String STORY_COVER_URL = "http://www.fimfiction-static.net/images/story_images/10.jpg";
     private static final int STORY_WORD_COUNT = 6947;
     private static final int STORY_CHAPTER_COUNT = 1;
     private static final String STORY_AUTHOR_NAME = "Cereal Velocity";
     private static final Story.Status STORY_STATUS = Story.Status.COMPLETE;
     private static final Story.ContentRating STORY_CONTENT_RATING = Story.ContentRating.EVERYONE;
     private static final Story.Category[] STORY_CATEGORIES = new Story.Category[] {
             Story.Category.ROMANCE,
             Story.Category.COMEDY };
     private static final Character[] STORY_CHARACTERS = new Character[] {
             Character.TWILIGHT_SPARKLE,
             Character.PRINCESS_CELESTIA,
             Character.PRINCESS_LUNA,
             Character.TRIXIE, };
     
     private static final int CHAPTER_ID = 11;
     private static final String CHAPTER_URL = "http://www.fimfiction.net/story/10/1/accolade/accolade";
     
     private final FimFiction session = new FimFiction();
     
     private String username;
     private String password;
     private int userId;
     
     @Before
     public void login() throws Exception {
         Assume.assumeNotNull(System.getProperty("fimfic.username"), System.getProperty("fimfic.password"), System.getProperty("fimfic.userid"));
         
         this.username = System.getProperty("fimfic.username");
         this.password = System.getProperty("fimfic.password");
         this.userId = Integer.parseInt(System.getProperty("fimfic.userid"));
         
         this.relog();
     }
     
     private void relog() throws Exception {
         final LoginOperation login = new LoginOperation(this.username, this.password);
         this.session.executeOperation(login);
         Assert.assertEquals(LoginOperation.Result.SUCCESS, login.getResult());
     }
     
     @Test
     public void testLoggedIn() throws Exception {
         final GetLoggedInUserOperation op = new GetLoggedInUserOperation();
         this.session.executeOperation(op);
         Assert.assertNotNull(op.getResult());
         Assert.assertEquals(this.userId, op.getResult().getId());
     }
     
     @Test
     public void testLogOut() throws Exception {
         try {
             final LogoutOperation op = new LogoutOperation();
             this.session.executeOperation(op);
             
             final GetLoggedInUserOperation check = new GetLoggedInUserOperation();
             this.session.executeOperation(check);
             Assert.assertNull(check.getResult());
             
             this.relog();
         } finally {
             this.relog();
         }
     }
     
     @Test
     public void testDiscardSession() throws Exception {
         try {
             final DiscardSessionOperation op = new DiscardSessionOperation();
             this.session.executeOperation(op);
             
             Assert.assertNull(this.session.getSessionId());
         } finally {
             this.relog();
         }
     }
     
     @Test
     public void testChapterDownloadText() throws Exception {
         final ByteArrayOutputStream buf = new ByteArrayOutputStream();
         final ChapterDownloadOperation op = new ChapterDownloadOperation(buf, DownloadType.TEXT, Chapter.builder().id(CHAPTER_ID).build());
         this.session.executeOperation(op);
         
        Assert.assertEquals("1165a17a66b8529e8807e04a6ece6d89", md5(buf.toByteArray()));
     }
     
     @Test
     public void testChapterDownloadHtml() throws Exception {
         final ByteArrayOutputStream buf = new ByteArrayOutputStream();
         final ChapterDownloadOperation op = new ChapterDownloadOperation(buf, DownloadType.HTML, Chapter.builder().id(CHAPTER_ID).build());
         this.session.executeOperation(op);
         
        Assert.assertEquals("d600f0b0cf99f0e8a86429abe358be38", md5(buf.toByteArray()));
     }
     
     @Test
     public void testStoryDownloadText() throws Exception {
         final ByteArrayOutputStream buf = new ByteArrayOutputStream();
         final StoryDownloadOperation op = new StoryDownloadOperation(buf, DownloadType.TEXT, Story.builder().id(STORY_ID).build());
         this.session.executeOperation(op);
         
        Assert.assertEquals("7f90351f021e4bb91e23bebf9e564c7f", md5(buf.toByteArray()));
     }
     
     @Test
     public void testStoryDownloadHtml() throws Exception {
         final ByteArrayOutputStream buf = new ByteArrayOutputStream();
         final StoryDownloadOperation op = new StoryDownloadOperation(buf, DownloadType.HTML, Story.builder().id(STORY_ID).build());
         this.session.executeOperation(op);
         
        Assert.assertEquals("1a7240e7bc221f54a478b958c0feb1e4", md5(buf.toByteArray()));
     }
     
     @Test
     public void testStoryDownloadEpub() throws Exception {
         final ByteArrayOutputStream buf = new ByteArrayOutputStream();
         final StoryDownloadOperation op = new StoryDownloadOperation(buf, DownloadType.EPUB, Story.builder().id(STORY_ID).build());
         this.session.executeOperation(op);
         
         // we can only check for size here because the result varies with every request.
         Assert.assertTrue(buf.toByteArray().length > 1000);
     }
     
     @Test
     public void testFavorite() throws Exception {
         for (final FavoriteState state : new FavoriteState[] {
                 FavoriteState.FAVORITED_EMAIL,
                 FavoriteState.FAVORITED,
                 FavoriteState.NOT_FAVORITED, }) {
             final FavoriteOperation op = new FavoriteOperation(Story.builder().id(STORY_ID).build(), state);
             this.session.executeOperation(op);
             
             final GetStoryMetaOperation check = new GetStoryMetaOperation(Story.builder().id(STORY_ID).build());
             check.setRequestMethod(GetStoryMetaOperation.RequestMethod.WEB);
             this.session.executeOperation(check);
             
             Assert.assertEquals(state, check.getResult().getFavorited());
         }
     }
     
     @Test
     public void testReadLater() throws Exception {
         for (final boolean b : new boolean[] {
                 true,
                 false }) {
             final ReadLaterOperation op = new ReadLaterOperation(Story.builder().id(STORY_ID).build(), b);
             this.session.executeOperation(op);
             
             final GetStoryMetaOperation check = new GetStoryMetaOperation(Story.builder().id(STORY_ID).build());
             check.setRequestMethod(GetStoryMetaOperation.RequestMethod.WEB);
             this.session.executeOperation(check);
             
             Assert.assertEquals(b, check.getResult().isReadLater());
         }
     }
     
     @Test
     public void testMetaWeb1() throws Exception {
         final GetStoryMetaOperation op = new GetStoryMetaOperation(Story.builder().id(STORY_ID).build());
         op.setRequestMethod(GetStoryMetaOperation.RequestMethod.WEB);
         this.session.executeOperation(op);
         
         Assert.assertEquals(STORY_ID, op.getResult().getId());
         Assert.assertEquals(STORY_TITLE, op.getResult().getTitle());
         Assert.assertEquals(STORY_AUTHOR_NAME, op.getResult().getAuthor().getName());
         Assert.assertEquals(STORY_COVER_URL, op.getResult().getImageUrl().toString());
         Assert.assertEquals(STORY_DESCRIPTION_HTML, op.getResult().getDescription());
         Assert.assertEquals(STORY_FIRST_POSTED, op.getResult().getFirstPostedDate());
         Assert.assertEquals(STORY_THUMBNAIL_URL, op.getResult().getThumbnailUrl().toString());
         Assert.assertEquals(STORY_UPDATED, op.getResult().getModificationDate());
         Assert.assertEquals(STORY_URL, op.getResult().getUrl().toString());
         Assert.assertArrayEquals(STORY_CATEGORIES, op.getResult().getCategories().toArray());
         Assert.assertEquals(STORY_CHAPTER_COUNT, op.getResult().getChapterCount());
         Assert.assertArrayEquals(STORY_CHARACTERS, op.getResult().getCharacters().toArray());
         Assert.assertEquals(STORY_CONTENT_RATING, op.getResult().getContentRating());
         Assert.assertEquals(STORY_STATUS, op.getResult().getStatus());
         Assert.assertEquals(STORY_WORD_COUNT, op.getResult().getWordCount());
         Assert.assertEquals(CHAPTER_ID, op.getResult().getChapters().get(0).getId());
         Assert.assertEquals(CHAPTER_URL, op.getResult().getChapters().get(0).getUrl().toString());
     }
     
     @Test
     public void testMetaWeb2() throws Exception {
         final GetStoryMetaOperation op = new GetStoryMetaOperation(Story.builder().id(14411).build());
         op.setRequestMethod(GetStoryMetaOperation.RequestMethod.WEB);
         this.session.executeOperation(op);
         
         Assert.assertTrue(op.getResult().isGore());
         Assert.assertTrue(op.getResult().isSex());
     }
     
     @Test
     public void testMetaJson1() throws Exception {
         final GetStoryMetaOperation op = new GetStoryMetaOperation(Story.builder().id(STORY_ID).build());
         op.setRequestMethod(GetStoryMetaOperation.RequestMethod.JSON);
         this.session.executeOperation(op);
         
         Assert.assertEquals(STORY_ID, op.getResult().getId());
         Assert.assertEquals(STORY_TITLE, op.getResult().getTitle());
         Assert.assertEquals(STORY_AUTHOR_NAME, op.getResult().getAuthor().getName());
         Assert.assertEquals(STORY_DESCRIPTION_BB, op.getResult().getDescription());
         Assert.assertEquals(STORY_THUMBNAIL_URL, op.getResult().getThumbnailUrl().toString());
         Assert.assertArrayEquals(STORY_CATEGORIES, op.getResult().getCategories().toArray());
         Assert.assertEquals(STORY_CHAPTER_COUNT, op.getResult().getChapterCount());
         Assert.assertEquals(STORY_CONTENT_RATING, op.getResult().getContentRating());
         Assert.assertEquals(STORY_STATUS, op.getResult().getStatus());
         Assert.assertEquals(STORY_WORD_COUNT, op.getResult().getWordCount());
         Assert.assertEquals(CHAPTER_ID, op.getResult().getChapters().get(0).getId());
     }
     
     @Test
     public void testMetaJson2() throws Exception {
         final GetStoryMetaOperation op = new GetStoryMetaOperation(Story.builder().id(STORY_ID).build());
         op.setRequestMethod(GetStoryMetaOperation.RequestMethod.JSON_CONTENT);
         this.session.executeOperation(op);
         
         Assert.assertEquals(STORY_ID, op.getResult().getId());
         Assert.assertEquals(STORY_TITLE, op.getResult().getTitle());
         Assert.assertEquals(STORY_AUTHOR_NAME, op.getResult().getAuthor().getName());
         Assert.assertEquals(STORY_DESCRIPTION_HTML2, op.getResult().getDescription());
         Assert.assertEquals(STORY_THUMBNAIL_URL, op.getResult().getThumbnailUrl().toString());
         Assert.assertArrayEquals(STORY_CATEGORIES, op.getResult().getCategories().toArray());
         Assert.assertEquals(STORY_CHAPTER_COUNT, op.getResult().getChapterCount());
         Assert.assertEquals(STORY_CONTENT_RATING, op.getResult().getContentRating());
         Assert.assertEquals(STORY_WORD_COUNT, op.getResult().getWordCount());
         Assert.assertEquals(CHAPTER_ID, op.getResult().getChapters().get(0).getId());
         Assert.assertEquals("dab8c50280f3bb8e1d514d00a79ece92", md5(op.getResult().getChapters().get(0).getContent().getBytes("UTF-8")));
     }
     
     @Test
     public void testSearchId() throws Exception {
         final SearchRequest op = new SearchRequest();
         op.setPage(0);
         op.setParameters(new SearchParameters());
         op.setRequestMethod(SearchRequest.RequestMethod.ID);
         op.setSuggestedResultCount(10);
         this.session.executeOperation(op);
         
         Assert.assertEquals(10, op.getResult().size());
     }
     
     @Test
     public void testSearchUnread() throws Exception {
         final SearchRequest op = new SearchRequest();
         op.setPage(0);
         op.setParameters(new SearchParameters().withPerspective(User.builder().id(this.userId).build()));
         op.setRequestMethod(SearchRequest.RequestMethod.UNREAD_RSS);
         op.setSuggestedResultCount(10);
         this.session.executeOperation(op);
         
         Assert.assertEquals(10, op.getResult().size());
     }
     
     @Test
     public void testSearchFull() throws Exception {
         final SearchRequest op = new SearchRequest();
         op.setPage(0);
         op.setParameters(new SearchParameters());
         op.setRequestMethod(SearchRequest.RequestMethod.FULL);
         op.setSuggestedResultCount(10);
         this.session.executeOperation(op);
         
         Assert.assertEquals(10, op.getResult().size());
     }
     
     @Test
     public void testAllCharactersImplemented() throws Exception {
         final Method method = FullSearchParser.class.getDeclaredMethod("getImageId", Character.class);
         method.setAccessible(true);
         final XMLReader reader = new Parser();
         reader.setContentHandler(new DefaultHandler() {
             @Override
             @SneakyThrows
             public void startElement(final String uri, final String localName, final String qName, final Attributes atts) throws SAXException {
                 if (qName.equals("img") && atts.getValue("src") != null && atts.getValue("src").startsWith("//www.fimfiction-static.net/images/characters/")) {
                     String imageId = atts.getValue("src").substring(46);
                     if (!imageId.isEmpty()) {
                         imageId = imageId.substring(0, imageId.indexOf('.'));
                         for (final Character character : Character.values()) {
                             if (method.invoke(null, character).equals(imageId)) {
                                 // we're good
                                 return;
                             }
                         }
                         Assert.fail("Unknown character ID: " + imageId);
                     }
                 }
             }
         });
         reader.parse(new InputSource(new URL("http://www.fimfiction.net/index.php?view=category").openStream()));
     }
     
     private static String md5(final byte... data) throws NoSuchAlgorithmException {
         final MessageDigest digest = MessageDigest.getInstance("MD5");
         final StringBuilder hash = new StringBuilder();
         for (final byte b : digest.digest(data)) {
             final int i = b & 0xff;
             if (i < 0x10) {
                 hash.append('0');
             }
             hash.append(Integer.toHexString(i));
         }
         return hash.toString();
     }
 }
