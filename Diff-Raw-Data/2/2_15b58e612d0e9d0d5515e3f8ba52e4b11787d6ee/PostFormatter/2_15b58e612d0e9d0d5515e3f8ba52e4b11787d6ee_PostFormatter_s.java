 package cc.hughes.droidchatty;
 
 import android.text.Html;
 import android.text.Spanned;
 
 public class PostFormatter {
 
     public static Spanned formatContent(Post post, boolean multiLine)
     {
         return formatContent(post.getUserName(), post.getContent(), multiLine);
     }
 
     public static Spanned formatContent(Thread thread, boolean multiLine)
     {
         return formatContent(thread.getUserName(), thread.getContent(), multiLine);
     }
 
     public static Spanned formatContent(String userName, String content, boolean multiLine)
     {
         // special case fix for shacknews links
         if (userName.equalsIgnoreCase("shacknews"))
             content = content.replaceAll("&lt;(/?)a(.*?)&gt;", "<$1a$2>");
 
         // convert shack's css into real font colors since Html.fromHtml doesn't supporty css of any kind
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
        content = content.replaceAll("<span class=\"jt_strike\">(.*?)</span>", "<del>1</del>");
 
         // if this is for a preview, change newlines into spaces
         if (!multiLine)
             content = content.replaceAll("<br />", " ");
 
         return Html.fromHtml(content);
     }
 
 }
