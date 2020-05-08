 package com.wishlistery.domain;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Objects;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
import org.codehaus.jackson.annotate.JsonIgnore;
 import org.codehaus.jackson.annotate.JsonIgnoreProperties;
 import org.springframework.data.annotation.Id;
 import org.springframework.data.mongodb.core.mapping.Document;
 
 import com.google.common.base.Joiner;
 import com.google.common.base.Splitter;
 import com.google.common.base.Strings;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 
 @Document
 @JsonIgnoreProperties({"blank", "persisted"})
 public class Wishlist extends BaseEntity implements Serializable {
     private static final String VIEWS_PATTERN = "\\[([ \\w,]+)\\]";
 
    private static final String DESCRIPTION_PATTERN = "\\(([^)]+)\\)";
 
     private static final String LINK_PATTERN = "\\b((?:http|www\\.)[\\w\\p{Punct}]+)\\b";
 
     private static final long serialVersionUID = 1L;
     
     @Id    
     private String id;
     
     private String name;
     private String description;
         
     private List<WishlistItem> items = new ArrayList<>();
     private List<String> views = new ArrayList<>(); //list of view names, actual assignment is done as attribute of item
     private List<String> categories = new ArrayList<>(); //list of category names, actual assignment is done as attribute of item
     
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     @Override
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public List<String> getViews() {
         return views;
     }
 
     public void setViews(List<String> views) {
         this.views = views;
     }
     
     public boolean isBlank() {
         return items == null || items.isEmpty();
     }
 
     public List<WishlistItem> getItems() {
         return items;
     }
 
     public void addItem(WishlistItem item) {
         item.setId(nextId());
         items.add(item);
     }
     
     public void removeItem(WishlistItem item) {
         items.remove(item);
     }
     
     public List<String> getCategories() {
         return categories;
     }
 
     public void setCategories(List<String> categories) {
         this.categories = categories;
     }
 
     public List<WishlistItem> getItemsInCategory(String name) {
         return getItemsInCategory(items, Strings.emptyToNull(name));
     }
     
     public List<WishlistItem> getItemsInViewAndCategory(String view, String category) {
         return getItemsInCategory(getItemsInView(view), name);
     }
     
     public List<WishlistItem> getItemsInCategory(List<WishlistItem> itemList, String category) {
         List<WishlistItem> results = new ArrayList<>();
         for (WishlistItem item : itemList) {
             if (Objects.equals(category, item.getCategory())) { 
                 results.add(item);
             }
         }
         return results;
     }
     
     public List<WishlistItem> getItemsInView(String view) {
         List<WishlistItem> results = new ArrayList<>();
         for (WishlistItem item : items) {
             if (item.getViews().contains(view)) { 
                 results.add(item);
             }
         }
         return results;
     }
 
     private int nextId() {
        int max = -1;
         for (WishlistItem item : items) {
             if (item.getId() > max) { max = item.getId(); }
         }
         return max + 1;
     }
 
     public void addView(String viewName) {
         if (!views.contains(viewName)) {
             views.add(viewName);
         }
     }
 
     public void removeView(String viewName) {
         if (!getItemsInView(viewName).isEmpty()) {
             throw new IllegalStateException("cannot remove a view while items still belong to it");
         }
         views.remove(viewName);        
     }
     
     public void addCategory(String categoryName) {
         if (!categories.contains(categoryName)) {
             categories.add(categoryName);
         }
     }
 
     public void removeCategory(String categoryName) {
         if (!getItemsInCategory(categoryName).isEmpty()) {
             throw new IllegalStateException("cannot remove a category while items still belong to it");
         }
         categories.remove(categoryName);        
     }
 
     public WishlistItem getItem(int itemId) {
         for (WishlistItem item : items) {
             if (item.getId() == itemId) {
                 return item;
             }
         }
         return null;
     }
 
     public void quickEdit(String text) {
         categories.clear();
         items.clear();
         views.clear();
         
         if (text == null) {
             return; //maybe should just clear the list??
         }
         // read lines
         // if line contains :, or next line starts with \w+[-*]\w+ assume it contains category
         // if that line contains further words, split on [;,]
         //    parse split line as items
         // if already in a category assume line starts an item
         //   parse line for (description) http://lookslikealink.com [views] 
         String[] lines = text.split("\n");
         String category = null;
         Set<String> uniqueViews = new LinkedHashSet<>();
         
         for (int i = 0; i < lines.length; i++) {
             String line = lines[i].trim();
             if (line.trim().isEmpty()) { continue; }
             
             if (line.contains(":") || i+1 < lines.length && lines[i+1].matches("\\w+[-*].+")) {
                 // this line has a category
                 String[] parts = line.split(":");
                 category = parts[0].trim();
                 categories.add(category);
                 
                 if (parts.length == 2) {
                     String[] inlineItems = parts[1].trim().split("[;,]");
                     for (String itemString : inlineItems) {
                         addItem(itemString, category, uniqueViews);
                     }
                     
                 }
             } else {
                 // this is just an item
                 StringBuilder itemString = new StringBuilder(line);
                 for (int j = i+1; j < lines.length; j++) {
                     String nextLine = lines[j];
                     String nextLineTrim = nextLine.trim();
                     boolean matchesAttribute = nextLineTrim.matches(LINK_PATTERN) || nextLineTrim.matches(DESCRIPTION_PATTERN)
                                                 || nextLineTrim.matches(VIEWS_PATTERN);
                     
                     if (!nextLineTrim.isEmpty() && nextLineTrim.charAt(0) != nextLine.charAt(0) || matchesAttribute) { 
                      // if trimming removes characters from the front
                         if (!matchesAttribute) {
                             itemString.append(" (").append(nextLineTrim).append(") ");
                         } else {
                             itemString.append(' ').append(nextLineTrim).append(' ');
                         }
                     } else {
                         addItem(itemString, category, uniqueViews);
                         i = j-1;
                         break;
                     }
                 }
             }
             
         }
         
         views = Lists.newArrayList(uniqueViews);
     }
 
     private void addItem(CharSequence itemString, String category, Set<String> uniqueViews) {
         WishlistItem item = parseItemString(itemString);
         item.setCategory(category);
         uniqueViews.addAll(item.getViews());
        addItem(item);
     }
 
     WishlistItem parseItemString(CharSequence itemString) {
         StringBuilder itemSb = new StringBuilder(itemString);
         WishlistItem item = new WishlistItem();
         item.setDescription(matchGroupAndReplace(itemSb, DESCRIPTION_PATTERN));
         item.setLink(matchGroupAndReplace(itemSb, LINK_PATTERN));
         String views = matchGroupAndReplace(itemSb, VIEWS_PATTERN);
         if (views != null) {
             item.setViews(Sets.newHashSet(Splitter.on(',').trimResults().split(views)));
         }
         item.setTitle(itemSb.toString().trim());
         return item;
     }
     
     private String matchGroupAndReplace(StringBuilder text, String pattern) {
         Matcher m = Pattern.compile(pattern).matcher(text);
         if (m.find() && m.groupCount() == 1) {
             String match = m.group(1);
             text.delete(m.start(), m.end());
             return match.trim();
         }
         return null;
     }
     
    @JsonIgnore
     public String getQuickEditText() {
         StringBuilder result = new StringBuilder();
         result.append(getQuickEditText(null));
         for (String cat : categories) {
             result.append(getQuickEditText(cat));
         }
         return result.toString();
                 
     }
     
     private String getQuickEditText(String category) {
         StringBuilder result = new StringBuilder();
         if (category != null) {
             result.append(category).append(":\n\n");
         }
         
         for (WishlistItem item : getItemsInCategory(category)) {
             result.append(item.getTitle()).append('\n');
             if (item.getDescription() != null) { appendLine(result, item.getDescription()); }
             if (item.getLink() != null) { appendLine(result, item.getLink()); }
             
             if (!item.getViews().isEmpty()) {
                 appendLine(result, "[" + Joiner.on(',').join(item.getViews()) + "]");
             }
             appendLine(result, "");
         }
         
         return result.append('\n').toString();
     }
     
     private void appendLine(StringBuilder sb, Object o) {
         sb.append("   ").append(o).append('\n');
     }
     
 }
