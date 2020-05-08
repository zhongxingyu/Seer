 package ro.koch.resourcefacades.facades;
 
 import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.getFirst;
 import ro.koch.resourcefacades.TitleAndSummary;
 
import com.google.common.base.Splitter;

 public class TextTitleAndSummary implements TitleAndSummary {
     private final String text;
    private final static Splitter titleSplitter = Splitter.on("\n").limit(1)
                                                  .trimResults();
 
     public TextTitleAndSummary(String text) {
         this.text = checkNotNull(text);
     }
 
     @Override public String getTitle() {
        String firstLine = getFirst(titleSplitter.split(text),"NO TITLE");
        return firstLine.substring(0, 90);
     }
 
     @Override public String getSummary() {
         return text;
     }

 }
