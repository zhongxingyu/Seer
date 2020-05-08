 package com.shaubert.dirty.client;
 
 import com.shaubert.blogadapter.client.DataLoaderRequest;
 import com.shaubert.blogadapter.client.PagerDataParserResult;
 import com.shaubert.blogadapter.client.Parser;
 import com.shaubert.blogadapter.client.ParserResultList;
 import com.shaubert.dirty.client.DirtyRecord.Image;
 import com.shaubert.dirty.client.HtmlTagFinder.AttributeWithValue.Constraint;
 import com.shaubert.dirty.client.HtmlTagFinder.Callback;
 import com.shaubert.dirty.client.HtmlTagFinder.Rule;
 import com.shaubert.dirty.client.HtmlTagFinder.TagNode;
 import com.shaubert.util.HtmlHelper;
 import com.shaubert.util.HtmlHelper.ImageHandler;
 import com.shaubert.util.Shlog;
 
 import android.text.SpannableStringBuilder;
 import android.text.TextUtils;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 public class DirtyCommentParser extends HtmlParser implements Parser {
 
     private static final String COMMENTS_HTML_PARSING_TIMER = "comments receiving and html parsing time";
     
     private static final String COMMENT_DIV_CLASS = "comment indent_";
     
     private static final Shlog SHLOG = new Shlog(DirtyPostParser.class.getSimpleName());
 
     private DirtyRecordParser helperParser;
     
     private PagerDataParserResult<DirtyComment> result;
     
     public DirtyCommentParser() {
         helperParser = new DirtyRecordParser();
     }
     
     @SuppressWarnings("unchecked")
     @Override
     public ParserResultList<DirtyComment> parse(DataLoaderRequest request, InputStream inputStream) throws IOException {
         this.result = new PagerDataParserResult<DirtyComment>();
         this.result.setResult(new ArrayList<DirtyComment>());
         
         addTagFinder(new HtmlTagFinder(new Rule("div")
                 .withAttributeWithValue("class", COMMENT_DIV_CLASS, Constraint.STARTS_WITH)
                 .withAttribute("id"), commentCallback));
         
         SHLOG.d("start loading and parsing html data");
         SHLOG.resetTimer(COMMENTS_HTML_PARSING_TIMER);
         parse(inputStream);
         SHLOG.logTimer(COMMENTS_HTML_PARSING_TIMER);
         
         return result;
     }
 
     private Callback commentCallback = new Callback() {
         @Override
         public void onTagFounded(HtmlTagFinder finder, TagNode foundTag) {
             parseComment(foundTag);
         }
     };
 
     protected void parseComment(TagNode tag) {
         final DirtyComment comment = new DirtyComment();
         long commentId = Long.parseLong(tag.getAttributes().getValue("", "id"));
         SHLOG.d("parsing comment " + commentId);
         comment.setServerId(commentId);
         String indentLevelStr = tag.getAttributes().getValue("", "class").split(" ")[1];
         comment.setIndentLevel(Integer.parseInt(indentLevelStr.substring(7)));
         
         TagNode body = tag.getNotContentChilds().get(0).getNotContentChilds().get(0);
         TagNode info = tag.getNotContentChilds().get(0).getNotContentChilds().get(1);
         
         SpannableStringBuilder builder = new SpannableStringBuilder();
         final List<Image> images = new ArrayList<Image>();
         HtmlHelper.convertToSpannable(body, builder, null, new ImageHandler() {
             @Override
             public void onImgTag(String src, int widht, int height) {
                 images.add(new Image(src, widht, height));
                 
                 String videoUrl = helperParser.tryToConvertToVideoUrl(src);
                 if (!TextUtils.isEmpty(videoUrl)) {
                     comment.setVideoUrl(videoUrl);
                 }
             }
         });
         while (builder.length() > 0 && builder.charAt(builder.length() - 1) == '\n') {
             builder.delete(builder.length() - 1, builder.length());
         }
         comment.setSpannedText(builder);
         comment.setFormattedText(HtmlHelper.toHtml(builder));
         comment.setMessage(builder.toString());
         comment.setImages(images.toArray(new Image[images.size()]));
         
         List<TagNode> infoTags = info.getNotContentChilds();
        comment.setAuthor(infoTags.get(1).getChilds().get(0).getText());
        comment.setAuthorLink("http://www.d3.ru" + infoTags.get(1).getAttributes().getValue("", "href"));
        String commentDate = info.getChilds().get(3).getText().substring(2);
         comment.setCreationDate(helperParser.parseDate(commentDate));
        comment.setVotesCount(Integer.parseInt(info.findAll(new Rule("div").withAttributeWithValue("class", "vote c_vote"))
                .get(0).getNotContentChilds().get(0).getChilds().get(0).getText()));
        
         comment.setOrder(result.getResult().size());
         result.getResult().add(comment);
     } 
 }
