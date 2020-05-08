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
                 "ab:",
                 "af:",
                 "ar:",
                 "an:",
                 "arc:",
                 "ast:",
                 "az:",
                 "ba:",
                 "bat-smg:",
                 "bar:",
                 "be:",
                 "bi:",
                 "bg:",
                 "be-x-old:",
                 "bcl:",
                 "bs:",
                 "bn:",
                 "br:",
                 "ca:",
                 "cbk-zam:",
                 "cdo:",
                 "cs:",
                 "crh:",
                 "cy:",
                 "da:",
                 "de:",
                 "dv:",
                 "eo:",
                 "el:",
                 "es:",
                 "et:",
                 "eu:",
                 "fa:",
                 "fi:",
                 "fo:",
                 "File:",
                 "fur:",
                 "fr:",
                 "fy:",
                 "ga:",
                 "gd:",
                 "gl:",
                 "ha:",
                 "hak:",
                 "haw:",
                 "he:",
                 "hi:",
                 "hif:",
                 "hr:",
                 "hu:",
                 "hy:",
                 "ia:",
                 "id:",
                 "ig:",
                 "Image:",
                 "imdbname:",
                 "is:",
                 "io:",
                 "ilo:",
                 "it:",
                 "ja:",
                 "jv:",
                 "ka:",
                 "kab:",
                 "kn:",
                 "km:",
                 "ko:",
                 "kk:",
                 "koi:",
                 "krc:",
                 "ku:",
                 "ky:",
                 "la:",
                 "lb:",
                 "lad:",
                 "ln:",
                 "li:",
                 "lt:",
                 "ltg:",
                 "lv:",
                 "mg:",
                 "mk:",
                 "mn:",
                 "mi:",
                 "ml:",
                 "mr:",
                 "ms:",
                 "mzn:",
                 "my:",
                 "na:",
                 "nds:",
                 "nap:",
                 "nl:",
                 "nn:",
                 "nrm:",
                 "no:",
                 "nov:",
                 "pcd:",
                 "pap:",
                 "pl:",
                 "pnt:",
                 "ps:",
                 "pt:",
                 "ro:",
                 "ru:",
                 "rue:",
                 "roa-rup:",
                 "s:",
                 "sah:",
                 "scn:",
                 "ss:",
                 "srn:",
                 "so:",
                 "si:",
                 "sh:",
                 "simple:",
                 "sk:",
                 "sl:",
                 "stq:",
                 "sq:",
                 "sr:",
                 "su:",
                 "sv:",
                 "sw:",
                 "szl:",
                 "ta:",
                 "te:",
                 "tet:",
                 "th:",
                 "tl:",
                 "tr:",
                 "tt:",
                 "uk:",
                 "ug:",
                 "qu:",
                 "uz:",
                 "ur:",
                 "vi:",
                 "vls:",
                 "vec:",
                 "Wikipedia:",
                 "wikt:",
                 "WP:",
                 "war:",
                 "wo:",
                 "xal:",
                 "yi:",
                 "yo:",
                 "zh-min-nan:",
                 "za:",
                 "zea:",
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
