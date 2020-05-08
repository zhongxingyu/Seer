 package org.atlasapi.output.simple;
 
 import java.util.List;
 import java.util.Set;
 
 import org.atlasapi.application.ApplicationConfiguration;
 import org.atlasapi.media.entity.ChildRef;
 import org.atlasapi.media.entity.simple.ContentIdentifier;
 import org.atlasapi.output.Annotation;
 
 import com.google.common.collect.Lists;
 
 public class ContentGroupModelSimplifier extends DescribedModelSimplifier<org.atlasapi.media.entity.ContentGroup, org.atlasapi.media.entity.simple.ContentGroup> {
 
     @Override
     public org.atlasapi.media.entity.simple.ContentGroup simplify(org.atlasapi.media.entity.ContentGroup model, Set<Annotation> annotations, ApplicationConfiguration config) {
 
         org.atlasapi.media.entity.simple.ContentGroup simple = new org.atlasapi.media.entity.simple.ContentGroup();
 
         copyBasicDescribedAttributes(model, simple, annotations);
 
         simple.setContent(simpleContentListFrom(model.getContents()));
        simple.setGroupType(model.getType().toString());
 
         return simple;
     }
 
     private List<ContentIdentifier> simpleContentListFrom(Iterable<ChildRef> contents) {
         List<ContentIdentifier> contentList = Lists.newArrayList();
         for (ChildRef ref : contents) {
             contentList.add(ContentIdentifier.identifierFor(ref));
         }
         return contentList;
     }
 }
