 package at.yawk.fimfiction.core;
 
 import at.yawk.fimfiction.data.*;
 import com.google.common.base.Preconditions;
 import java.net.URL;
 import java.util.Set;
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 
 import static at.yawk.fimfiction.data.SearchParameters.SearchParameter.*;
 import static at.yawk.fimfiction.net.NetUtil.createUrlNonNull;
 import static at.yawk.fimfiction.net.NetUtil.encodeUtf8;
 
 /**
  * Builder class used for creating URL objects out of SearchParameters or story IDs and page numbers.
  *
  * @author Jonas Konrad (yawkat)
  */
 public class SearchUrl {
     private int id;
     private int page;
     @Nullable private CompiledSearchParameters parameters;
 
     private SearchUrl() {}
 
     public static SearchUrl create() { return new SearchUrl(); }
 
     /*
      * Depending on whether id() or parameters() is called last, the builder will be chosen.
      */
 
     /**
      * Changes this builder's mode to story detail and assigns the searching ID.
      *
      * @return this object.
      */
     @Nonnull
     public SearchUrl id(int id) {
         Preconditions.checkArgument(id > 0);
         this.id = id;
         // we're using the ID field and no parameters
         parameters = null;
         return this;
     }
 
     /**
      * Convenience method for #id.
      *
      * @throws MissingKeyException if no ID key is set.
      */
     @Nonnull
     public SearchUrl id(@Nonnull Story story) throws MissingKeyException {
         Preconditions.checkNotNull(story);
 
         return id(story.getInt(Story.StoryKey.ID));
     }
 
     /**
      * Sets the page for the search mode of this builder.
      *
      * @return this object.
      * @throws IllegalArgumentException if the page is below 0.
      */
     @Nonnull
     public SearchUrl page(int page) {
         Preconditions.checkArgument(page >= 0);
         this.page = page;
         return this;
     }
 
     /**
      * Compiles the given non-null SearchParameters and sets this builder's parameters to those.
      *
      * @return this object.
      * @throws MissingKeyException if a key is missing during parameter compilation.
      */
     @Nonnull
     public SearchUrl parameters(@Nonnull SearchParameters parameters) throws MissingKeyException {
         return parameters(CompiledSearchParameters.compile(parameters));
     }
 
     /**
      * Sets this builder's mode to search and assigns it the given parameters.
      *
      * @return this object.
      * @throws NullPointerException if the parameter is null.
      */
     @Nonnull
     public SearchUrl parameters(@Nonnull CompiledSearchParameters parameters) {
         Preconditions.checkNotNull(parameters);
 
         this.parameters = parameters;
         return this;
     }
 
     /**
      * Builds a final URL from the previously set parameters / story id.
      */
     @Nonnull
     public URL build() {
         CompiledSearchParameters parameters = this.parameters; // race conditions
         if (parameters == null) {
             return createUrlNonNull("http://www.fimfiction.net/story/" + id);
         } else {
             return createUrlNonNull(parameters.url + (page + 1));
         }
     }
 
     /**
      * Caching class that can be used to cache a search URL for reuse. This class can be used on a SearchUrl with
      * different page numbers, allowing faster processing of multi-page searches.
      */
     public static class CompiledSearchParameters {
         final String url;
 
         private CompiledSearchParameters(String url) {
             this.url = url;
         }
 
         /**
          * Compiles the given SearchParameters.
          *
          * @return the compiled parameters.
          * @throws NullPointerException if the parameter is null.
          * @throws MissingKeyException  if a required key is missing.
          */
         @Nonnull
         public static CompiledSearchParameters compile(@Nonnull SearchParameters p) throws MissingKeyException {
             Preconditions.checkNotNull(p);
 
             StringBuilder result = new StringBuilder("http://www.fimfiction.net/index.php?view=category");
 
             if (p.has(NAME)) { result.append("&term=").append(encodeUtf8(p.getString(NAME))); }
             if (p.has(ORDER)) { result.append("&order=").append(encodeOrder(p.<Order>get(ORDER))); }
 
             if (p.getBoolean(SEX, false)) { result.append("&sex=1"); }
             if (p.getBoolean(GORE, false)) { result.append("&gore=1"); }
             if (p.getBoolean(COMPLETED, false)) { result.append("&completed=1"); }
             if (p.has(WORD_COUNT_MINIMUM)) {
                 result.append("&minimum_words=").append(p.getInt(WORD_COUNT_MINIMUM, 1) - 1);
             }
             if (p.has(WORD_COUNT_MAXIMUM)) {
                 result.append("&maximum_words=").append(p.getInt(WORD_COUNT_MAXIMUM) + 1);
             }
 
             if (p.getBoolean(FAVORITED, false)) { result.append("&tracking=1"); }
             if (p.getBoolean(UNREAD, false)) { result.append("&unread=1"); }
             if (p.getBoolean(READ_LATER, false)) { result.append("&read_it_later=1"); }
 
             if (p.has(CONTENT_RATING)) {
                 result.append("&content_rating=").append(p.<ContentRating>get(CONTENT_RATING).ordinal());
             }
             if (p.has(USER)) {
                 User user = p.get(USER);
                 result.append("&user=").append(user.getInt(User.UserKey.ID));
             }
 
             if (p.has(CATEGORIES_INCLUDED)) {
                 for (Category category : p.<Set<Category>>get(CATEGORIES_INCLUDED)) {
                     result.append("&tags[]=category%3A").append(encodeCategory(category));
                 }
             }
             if (p.has(CATEGORIES_EXCLUDED)) {
                 for (Category category : p.<Set<Category>>get(CATEGORIES_EXCLUDED)) {
                     result.append("&tags[]=-category%3A").append(encodeCategory(category));
                 }
             }
             if (p.has(CHARACTERS_INCLUDED)) {
                 for (FimCharacter character : p.<Set<FimCharacter>>get(CHARACTERS_INCLUDED)) {
                     result.append("&tags[]=character%3A").append(character.getFimfictionId());
                 }
             }
             if (p.has(CHARACTERS_EXCLUDED)) {
                 for (FimCharacter character : p.<Set<FimCharacter>>get(CHARACTERS_EXCLUDED)) {
                     result.append("&tags[]=-character%3A").append(character.getFimfictionId());
                 }
             }
 
             result.append("&page="); // the page number will be appended in #build()
 
             return new CompiledSearchParameters(result.toString());
         }
 
         @Nonnull
         private static String encodeOrder(@Nonnull Order order) {
             switch (order) {
             case COMMENT_COUNT:
                 return "comments";
             case FIRST_POSTED_DATE:
                 return "latest";
             case HOT:
                 return "heat";
             case UPDATE_DATE:
                 return "updated";
             case VIEW_COUNT:
                 return "views";
             case WORD_COUNT:
                 return "words";
             default:
                 return order.getId();
             }
         }
 
         @Nonnull
         private static String encodeCategory(@Nonnull Category category) {
             return category.getId();
         }
 
         @Override
         public String toString() {
             return getClass().getSimpleName() + "{" + url + "}";
         }
     }
 }
