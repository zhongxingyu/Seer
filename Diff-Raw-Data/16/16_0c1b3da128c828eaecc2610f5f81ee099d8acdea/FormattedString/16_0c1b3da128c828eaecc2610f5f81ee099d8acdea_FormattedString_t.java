 package at.yawk.fimfiction.data;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Lists;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import javax.annotation.Nonnull;
 import org.apache.commons.lang.StringEscapeUtils;
 
 /**
  * A HTML-like formatted string class. It contains a CharSequence as the actual content and a list of start and end
  * tags for formatting. Overridden CharSequence methods (including toString) will work with unformatted content.
  *
  * @author Jonas Konrad (yawkat)
  */
 public class FormattedString implements CharSequence {
     @Nonnull private final CharSequence sequence;
     private final List<Tag> tags;
 
     // CharSequence
 
     @Override
     public int length() { return sequence.length(); }
 
     @Override
     public char charAt(int index) { return sequence.charAt(index); }
 
     @Override
     @Nonnull
     public CharSequence subSequence(int start, int end) { return sequence.subSequence(start, end); }
 
     @Nonnull
     @Override
     public String toString() { return sequence.toString(); }
 
 
     // Constructor
 
     /**
      * Creates a new instance with the given text value and tags.
      */
     public static FormattedString create(@Nonnull CharSequence value, @Nonnull List<Tag> tags) {
         Preconditions.checkNotNull(value);
         Preconditions.checkNotNull(tags);
         return new FormattedString(value.toString(), Lists.newArrayList(tags));
     }
 
     /**
      * Creates a new instance with the given text value and tags.
      */
     public static FormattedString create(@Nonnull CharSequence value, @Nonnull Tag... tags) {
         Preconditions.checkNotNull(tags);
         return create(value, Arrays.asList(tags));
     }
 
     private FormattedString(@Nonnull CharSequence value, @Nonnull List<Tag> tags) {
         this.sequence = value;
         Collections.sort(tags, new Comparator<Tag>() {
             @Override
             public int compare(@Nonnull Tag o1, @Nonnull Tag o2) {
                 return o1.index - o2.index;
             }
         });
         this.tags = Collections.unmodifiableList(tags);
     }
 
     // Formatting
 
     /**
      * Builds a string with appropriate tags in the given markup.
      */
     @Nonnull
     public String buildFormattedText(@Nonnull Markup markup) {
         Preconditions.checkNotNull(markup);
 
         StringBuilder result = new StringBuilder();
         int textPosition = 0;
         for (Tag tag : tags) {
            result.append(transformText(markup, subSequence(textPosition, tag.index)));
             textPosition = tag.index;
             result.append(tag.getTag(markup));
         }
        result.append(transformText(markup, subSequence(textPosition, length())));
         return result.toString();
     }
 
    private CharSequence transformText(Markup markup, CharSequence sequence) {
        if (markup == Markup.HTML) {
            sequence = StringEscapeUtils.escapeHtml(sequence.toString()).replace("\n", "<br/>");
        }
        return sequence;
    }

     /**
      * A specific tag in a formatted string, compromised out of a formatting type, a position and a start/end flag.
      */
     public static class Tag {
         @Nonnull private final Formatting formatting;
         private final int index;
         private final boolean start;
 
         public Tag(@Nonnull Formatting formatting, int index, boolean start) {
             Preconditions.checkNotNull(formatting);
 
             this.formatting = formatting;
             this.index = index;
             this.start = start;
         }
 
         /**
          * Format this tag in the given markup, including brackets.
          */
         @Nonnull
         public String getTag(@Nonnull Markup markup) {
             Preconditions.checkNotNull(markup);
             StringBuilder builder = new StringBuilder();
             builder.append(markup == Markup.HTML ? '<' : '[');
             if (!start) { builder.append('/'); }
             builder.append(formatting.getTag(markup, start));
             builder.append(markup == Markup.HTML ? '>' : ']');
             return builder.toString();
         }
     }
 
     /**
      * Simple formatting class, representing a style such as "bold" or "font-size: 20".
      */
     public static interface Formatting {
         /**
          * Returns the body of this tag. This must not return any other characters such as '[', ']' or '/',
          * but only the body such as "b", "size=1.5em" or "size".
          *
          * @param start If this value is set to false, no parameters such as "=1.5em" should be included.
          */
         String getTag(Markup markup, boolean start);
     }
 
     /**
      * Basic, HTML-like formatting singletons.
      */
     public static enum SimpleFormatting implements Formatting {
         ITALIC("i"),
         BOLD("b"),
         UNDERLINE("u"),
         STRIKETHROUGH("s"),
         CENTER("center");
 
         private final String bbTag;
         private final String htmlTag;
 
         SimpleFormatting(String tag) {
             this(tag, tag);
         }
 
         SimpleFormatting(String bbTag, String htmlTag) {
             this.bbTag = bbTag;
             this.htmlTag = htmlTag;
         }
 
         @Override
         public String getTag(Markup markup, boolean start) {
             Preconditions.checkNotNull(markup);
             return markup == Markup.BB ? bbTag : htmlTag;
         }
     }
 
     /**
      * Creates a new formatting type for the given size in the given unit.
      */
     @Nonnull
     public static Formatting size(float size, @Nonnull SizeUnit unit) {
         Preconditions.checkNotNull(unit);
         final String sizeString = size + (unit == SizeUnit.EM ? "em" : unit == SizeUnit.PT ? "pt" : "px");
         return new Formatting() {
             @Nonnull
             @Override
             public String getTag(Markup markup, boolean start) {
                 Preconditions.checkNotNull(markup);
                 StringBuilder tag = new StringBuilder();
                 if (start) {
                     tag.append(markup == Markup.BB ? "size=" + sizeString : "font size=\"" + sizeString + "\"");
                 } else {
                     tag.append(markup == Markup.BB ? "size" : "font");
                 }
                 return tag.toString();
             }
         };
     }
 
     /**
      * Types of markup supported for formatting text.
      */
     public static enum Markup {
         /**
          * HTML markup. Strings should be escaped properly.
          */
         HTML,
         /**
          * Fimfiction-like BB-Code. Strings will not be escaped.
          */
         BB,
     }
 
     /**
      * Different size units used for #size.
      */
     public static enum SizeUnit {
         EM,
         PX,
         PT,
     }
 
     /**
      * Create a new builder instance.
      */
     public static FormattedStringBuilder builder() {
         return new FormattedStringBuilder();
     }
 
     /**
      * Fast StringBuilder-like class that can be used for efficient string building.
      */
     public static class FormattedStringBuilder implements CharSequence {
         private final StringBuilder text = new StringBuilder();
         private final List<Tag> tags = Lists.newArrayList();
 
         private FormattedStringBuilder() {}
 
         public void append(CharSequence text) {
             this.text.append(text);
         }
 
         public void append(char[] array, int off, int len) {
             this.text.append(array, off, len);
         }
 
         public void append(char c) {
             this.text.append(c);
         }
 
         public void append(Formatting instruction, boolean start) {
             this.tags.add(new Tag(instruction, text.length(), start));
         }
 
         public boolean isEmpty() {
             return tags.isEmpty() && text.length() == 0;
         }
 
         public FormattedString build() {
             return create(text, tags);
         }
 
         @Override
         public int length() {
             return text.length();
         }
 
         @Override
         public char charAt(int index) {
             return text.charAt(index);
         }
 
         @Override
         public CharSequence subSequence(int start, int end) {
             return text.substring(start, end);
         }
     }
 }
