 package com.amee.platform.resource.tag.v_3_0;
 
 import com.amee.base.domain.Since;
 import com.amee.base.resource.RequestWrapper;
 import com.amee.domain.IAMEEEntityReference;
 import com.amee.domain.tag.Tag;
 import com.amee.platform.resource.tag.TagsResource;
 import com.amee.platform.resource.tag.v_3_2.TagsBuilder_3_2_0;
 import com.amee.service.tag.TagService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 
 @Service
 @Scope("prototype")
 @Since("3.0.0")
 public class TagsBuilder_3_0_0 extends TagsBuilder_3_2_0 {
 
     @Autowired
     private TagService tagService;
 
     @Override
     public void handle(RequestWrapper requestWrapper, IAMEEEntityReference entity) {
         TagsResource.Renderer renderer = getRenderer(requestWrapper);
         renderer.start();
        for (Tag tag : tagService.getTags(entity)) {
             renderer.newTag(tag);
         }
     }
 }
