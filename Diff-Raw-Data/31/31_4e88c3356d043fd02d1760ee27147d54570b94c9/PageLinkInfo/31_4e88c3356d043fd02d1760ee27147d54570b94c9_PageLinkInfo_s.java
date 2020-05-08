 package wikipedia.network;
 
 import java.util.Collection;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.joda.time.DateTime;
 
 import util.Const;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Lists;
 
 /**
  * Infos about the outgoing links of a page a the given point in time
  */
 public final class PageLinkInfo {
 
     private final String pageTitle;
     private final int pageID;
     private final DateTime revisionDate;
     private final List<String> outgoingLinks;
 
     public PageLinkInfo(final String pageName, final DateTime revisionDate,
             final List<String> outgoingLinks, final int pageID) {
         this.pageTitle = pageName;
         this.revisionDate = revisionDate;
         this.outgoingLinks = outgoingLinks;
         this.pageID = pageID;
     }
 
     @Override
     public String toString() {
         return "PageLinkInfo [pageName=" + pageTitle + ", revisionDate=" + revisionDate
                 + ", outgoingLinks=" + StringUtils.join(outgoingLinks, "\n") + "]";
     }
 
     public String getPageTitle() {
         return pageTitle;
     }
 
     public int getPageID() {
         return pageID;
     }
 
     public DateTime getTimeStamp() {
         return revisionDate;
     }
 
     public String getLinksAsString() {
         return StringUtils.join(outgoingLinks, Const.LINK_SEPARATOR);
     }
 
     public List<String> getLinks() {
         return outgoingLinks;
     }
 
     public Collection<String> getFilteredLinks() {
         return Collections2.filter(outgoingLinks, new Predicate<String>() {
             @Override
             public boolean apply(final String input) {
                 return notInBlockList(input);
             }
         });
     }
 
     protected boolean notInBlockList(final String input) {
         List<String> igoredByPrefix = Lists.newArrayList(
                 "#",
                 "af:",
                 "ar:",
                 "arc:",
                 "az:",
                 "bg:",
                 "bn:",
                 "br:",
                 "ca:",
                 "cbk-zam:",
                 "cs:",
                 "cy:",
                 "da:",
                 "de:",
                 "dv:",
                 "eo:",
                 "es:",
                 "et:",
                 "eu:",
                 "fa:",
                 "fi:",
                 "File:",
                 "fr:",
                 "fy:",
                 "ga:",
                 "gd:",
                 "gl:",
                 "he:",
                 "hi:",
                 "hr:",
                 "hu:",
                 "hy:",
                 "id:",
                 "ig:",
                 "Image:",
                 "is:",
                 "it:",
                 "ja:",
                 "jv:",
                 "ka:",
                 "kab:",
                 "kn:",
                 "ko:",
                 "la:",
                 "ln:",
                 "lt:",
                 "lv:",
                 "mg:",
                 "mk:",
                 "mn:",
                 "ml:",
                 "mr:",
                 "ms:",
                 "my:",
                 "nl:",
                 "nn:",
                 "no:",
                 "pcd:",
                 "pl:",
                 "pt:",
                 "ro:",
                 "ru:",
                 "s:",
                 "si:",
                 "sh:",
                 "simple:",
                 "sk:",
                 "sl:",
                 "sq:",
                 "sr:",
                 "sv:",
                 "sv:",
                 "sw:",
                 "ta:",
                 "te:",
                 "th:",
                 "tl:",
                 "tr:",
                 "tt:",
                 "uk:",
                 "uz:",
                 "vi:",
                 "Wikipedia:",
                 "WP:",
                 "yi:",
                 "yo:",
                 "zh-min-nan:",
                 "zh:",
                 "am:");
 
         for (String prefix : igoredByPrefix) {
             if (input.startsWith(prefix)) {
                 return false;
             }
         }
         return true;
     }
 
 }
