 package cc.hughes.droidchatty;
 
 import org.xml.sax.XMLReader;
 
 import android.text.Editable;
 import android.text.Html;
 import android.text.Spannable;
 import android.text.Spanned;
 import android.text.Html.TagHandler;
 import android.view.View;
 
 public class PostFormatter {
 
     public static Spanned formatContent(Post post, boolean multiLine)
     {
         return formatContent(post, null, multiLine);
     }
     
     public static Spanned formatContent(Post post, View view, boolean multiLine)
     {
         return formatContent(post.getUserName(), post.getContent(), view, multiLine);
     }
 
     public static Spanned formatContent(Thread thread, boolean multiLine)
     {
         return formatContent(thread.getUserName(), thread.getContent(), null, multiLine);
     }
 
     public static Spanned formatContent(String userName, String content, final View view, boolean multiLine)
     {
         // special case fix for shacknews links
         if (userName.equalsIgnoreCase("shacknews"))
             content = content.replaceAll("&lt;(/?)a(.*?)&gt;", "<$1a$2>");
 
         // convert shack's css into real font colors since Html.fromHtml doesn't supporty css of any kind
         // i'd rather just handle all this crap in the TagHandler below but getting the class attribute is not easy
         content = content.replaceAll("<span class=\"jt_red\">(.*?)</span>", "<font color=\"#ff0000\">$1</font>");
         content = content.replaceAll("<span class=\"jt_green\">(.*?)</span>", "<font color=\"#8dc63f\">$1</font>");
         content = content.replaceAll("<span class=\"jt_pink\">(.*?)</span>", "<font color=\"#f49ac1\">$1</font>");
         content = content.replaceAll("<span class=\"jt_olive\">(.*?)</span>", "<font color=\"#808000\">$1</font>");
         content = content.replaceAll("<span class=\"jt_fuchsia\">(.*?)</span>", "<font color=\"#c0ffc0\">$1</font>");
         content = content.replaceAll("<span class=\"jt_yellow\">(.*?)</span>", "<font color=\"#ffde00\">$1</font>");
         content = content.replaceAll("<span class=\"jt_blue\">(.*?)</span>", "<font color=\"#44aedf\">$1</font>");
         content = content.replaceAll("<span class=\"jt_lime\">(.*?)</span>",  "<font color=\"#c0ffc0\">$1</font>");
         content = content.replaceAll("<span class=\"jt_orange\">(.*?)</span>", "<font color=\"#f7941c\">$1</font>");
         content = content.replaceAll("<span class=\"jt_bold\">(.*?)</span>", "<b>$1</b>");
         content = content.replaceAll("<span class=\"jt_italic\">(.*?)</span>", "<i>$1</i>");
         content = content.replaceAll("<span class=\"jt_underline\">(.*?)</span>", "<u>$1</u>");
        content = content.replaceAll("<span class=\"jt_strike\">(.*?)</span>", "<del>$1</del>");
         content = content.replaceAll("<span class=\"jt_spoiler\".*?>(.*?)</span>", "<spoiler>$1</spoiler>");
 
         // if this is for a preview, change newlines into spaces
         if (!multiLine)
             content = content.replaceAll("<br />", " ");
 
         return Html.fromHtml(content, null, new TagHandler()
         {
             public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader)
             {
                 // handle our awesome fake spoiler tag
                 if (tag.equals("spoiler"))
                 {
                     int len = output.length();
                     if (opening)
                     {
                         output.setSpan(new SpoilerSpan(null), len, len, Spannable.SPAN_MARK_MARK);
                     }
                     else
                     {
                         Object obj = getLast(output, SpoilerSpan.class);
                         int where = output.getSpanStart(obj);
                         
                         output.removeSpan(obj);
                         
                         if (where != len)
                         {
                             output.setSpan(new SpoilerSpan(view), where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                         }
                     }
                 }
             }
         });
     }
     
     private static Object getLast(Editable text, Class<?> kind)
     {
         Object[] objs = text.getSpans(0, text.length(), kind);
         
         for (int i = objs.length - 1; i >= 0; i--)
             if (text.getSpanFlags(objs[i]) == Spannable.SPAN_MARK_MARK)
                 return objs[i];
         
         return null;
     }
     
 }
