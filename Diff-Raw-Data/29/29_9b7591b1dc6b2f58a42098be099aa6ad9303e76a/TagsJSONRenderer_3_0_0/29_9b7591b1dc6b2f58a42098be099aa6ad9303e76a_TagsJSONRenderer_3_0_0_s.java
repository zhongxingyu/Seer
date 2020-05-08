 package com.amee.platform.resource.tag.v_3_0;
 
 import com.amee.base.domain.Since;
 import com.amee.base.resource.ResponseHelper;
 import com.amee.domain.tag.Tag;
 import com.amee.platform.resource.tag.v_3_2.TagsJSONRenderer_3_2_0;
import org.json.JSONArray;
 import org.json.JSONObject;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 
 @Service
 @Scope("prototype")
 @Since("3.0.0")
 public class TagsJSONRenderer_3_0_0 extends TagsJSONRenderer_3_2_0 {
 
     private JSONObject rootObj;
    private JSONArray tagsArr;
 
     @Override
     public void newTag(Tag tag) {
         JSONObject tagObj = new JSONObject();
         ResponseHelper.put(tagObj, "tag", tag.getTag());
         if (tag.hasCount()) {
             ResponseHelper.put(tagObj, "count", tag.getCount());
         }
         tagsArr.put(tagObj);
     }
 }
