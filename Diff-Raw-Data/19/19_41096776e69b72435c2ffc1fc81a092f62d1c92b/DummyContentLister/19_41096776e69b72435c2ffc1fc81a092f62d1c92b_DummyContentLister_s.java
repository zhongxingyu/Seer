 package org.atlasapi.search.searcher;
 
 import java.util.List;
 
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.ContentGroup;
 import org.atlasapi.persistence.content.RetrospectiveContentLister;
 
 import com.google.common.collect.ImmutableList;
 
 public class DummyContentLister implements RetrospectiveContentLister {
     
     private List<Content> content;
     private List<ContentGroup> groups;
 
     public DummyContentLister(List<Content> respondWith) {
         this.content = respondWith;
         this.groups = ImmutableList.of();
     }
     
     public void loadLister(List<Content> respondWith) {
         this.content = respondWith;
     }
     
     public void loadGroupLister(List<ContentGroup> respondWith) {
         this.groups = respondWith;
     }
 
     @Override
     public List<Content> listAllRoots(String arg0, int arg1) {
         List<Content> result = ImmutableList.copyOf(content);
         content = ImmutableList.<Content>of();
         return result;
     }
 
     @Override
     public List<ContentGroup> listAllContentGroups(String arg0, int arg1) {
         List<ContentGroup> result = ImmutableList.copyOf(groups);
         groups = ImmutableList.<ContentGroup>of();
         return result;
     }
 }
