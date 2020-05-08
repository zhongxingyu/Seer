 /*
  * This file is part of AMEE.
  *
  * Copyright (c) 2007, 2008, 2009 AMEE UK LIMITED (help@amee.com).
  *
  * AMEE is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  *
  * AMEE is free software and is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Created by http://www.dgen.net.
  * Website http://www.amee.cc
  */
 package com.amee.restlet.profile;
 
 import com.amee.restlet.BaseResource;
 import com.amee.service.auth.ResourceActions;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import java.io.Serializable;
 
 @Component("profileActionsResource")
 @Scope("prototype")
 public class ProfileActionsResource extends BaseResource implements Serializable {
 
     @Autowired
     @Qualifier("profileActions")
     private ResourceActions profileActions;
 
     @Autowired
     @Qualifier("profileCategoryActions")
     private ResourceActions profileCategoryActions;
 
     @Autowired
     @Qualifier("profileItemActions")
     private ResourceActions profileItemActions;
 
     @Autowired
     @Qualifier("profileItemValueActions")
     private ResourceActions profileItemValueActions;
 
     @Override
     public String getTemplatePath() {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public JSONObject getJSONObject() throws JSONException {
         JSONObject obj = new JSONObject();
         obj.put("profile", profileActions.getJSONObject());
         obj.put("profileCategory", profileCategoryActions.getJSONObject());
         obj.put("profileItem", profileItemActions.getJSONObject());
        obj.put("profileItemValu", profileItemValueActions.getJSONObject());
         return obj;
     }
 
     @Override
     public Element getElement(Document document) {
         throw new UnsupportedOperationException();
     }
 }
