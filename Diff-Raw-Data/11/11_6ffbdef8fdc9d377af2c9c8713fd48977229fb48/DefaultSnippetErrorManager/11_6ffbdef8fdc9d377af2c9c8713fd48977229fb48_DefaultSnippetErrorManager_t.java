 package com.atlassian.confluence.extra.snippet;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.atlassian.bandana.BandanaManager;
 import com.atlassian.confluence.pages.PageManager;
 import com.atlassian.confluence.setup.bandana.ConfluenceBandanaContext;
 import com.atlassian.confluence.spaces.Space;
 import com.atlassian.confluence.spaces.SpaceManager;
 
 /**
  * Manages snippet errors
  */
 public class DefaultSnippetErrorManager implements SnippetErrorManager {
   public static final String PERSISTENCE_KEY = "snippet.errors";
   private final BandanaManager bandanaManager;
   private final SpaceManager spaceManager;
   private final PageManager pageManager;
   private static final int QUEUE_SIZE = 20;
 
   public DefaultSnippetErrorManager(BandanaManager bandanaManager, SpaceManager spaceManager, PageManager pageManager) {
     this.bandanaManager = bandanaManager;
     this.spaceManager = spaceManager;
     this.pageManager = pageManager;
   }
 
   public void clear(String spaceKey) {
     saveErrors(spaceKey, null);
   }
 
   public void clear() {
     for (Object o : spaceManager.getAllSpaces()) {
       Space space = (Space) o;
       saveErrors(space.getKey(), null);
     }
     saveErrors(null, null);
   }
 
   public List<SnippetError> get(String spaceKey) {
     List<String> errors = retrieveErrors(spaceKey);
 
     List<SnippetError> result = new ArrayList<SnippetError>();
     for (String error : errors) {
       result.add(new SnippetError(error, pageManager));
     }
     return result;
   }
 
   List<String> retrieveErrors(String spaceKey) {
     List<String> messages = (List<String>) bandanaManager.getValue(new ConfluenceBandanaContext(spaceKey),
         PERSISTENCE_KEY);
     if (messages == null) {
       messages = new ArrayList<String>();
     } else {
       messages = new ArrayList<String>(messages);
     }
 
     return messages;
   }
 
   private void saveErrors(String spaceKey, List<String> errors) {
     bandanaManager.setValue(new ConfluenceBandanaContext(spaceKey), PERSISTENCE_KEY, errors);
   }
 
   public void add(SnippetError error) {
     List<String> errors = retrieveErrors(error.getSpaceKey());
    if (!errors.contains(error)) {
      errors.add(0, error.toString());
      if (errors.size() >= QUEUE_SIZE)
        errors.remove(QUEUE_SIZE - 1);
      saveErrors(error.getSpaceKey(), errors);
    }
   }
 }
