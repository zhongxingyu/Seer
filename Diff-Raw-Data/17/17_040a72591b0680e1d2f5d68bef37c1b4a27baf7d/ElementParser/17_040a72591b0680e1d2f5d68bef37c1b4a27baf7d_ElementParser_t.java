 package br.com.adaptworks.scraper.element;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import br.com.adaptworks.scraper.tag.Tag;
 import br.com.adaptworks.scraper.tag.TagReader;
 
 /**
  * @author jonasabreu
  * 
  */
 final public class ElementParser {
 
     private final Pattern pattern = Pattern.compile("(?s)<([^<>]*?)>([^<>]*)");
     private final String relevantTags;
     private final boolean shouldClean;
 
     public ElementParser(final String relevantTags) {
         this.relevantTags = relevantTags;
         shouldClean = true;
     }
 
     public ElementParser() {
         shouldClean = false;
         relevantTags = null;
     }
 
     public List<Element> parse(final String template) {
         String cleanTemplate = template;
         if (shouldClean) {
             cleanTemplate = cleanTemplate(relevantTags, template);
         }
         ArrayList<Element> elements = new ArrayList<Element>();
         Matcher matcher = pattern.matcher(cleanTemplate);
         while (matcher.find()) {
             Tag tag = new TagReader().readTag(matcher.group(1));
             String elementContent = null;
 
             if (matcher.group(2).length() != 0) {
                 elementContent = matcher.group(2);
             }
 
             elements.add(tag.type().createElement(tag, elementContent));
 
         }
 
         return elements;
     }
 
     private String cleanTemplate(final String relevantTags, final String template) {
        String regex = "(?s)(?i)<(?!(?i:" + relevantTags + ")\\b)[^<>]+>";
         return template.replaceAll(regex, "");
     }
 
 }
