 /**
  * Copyright (C) 2010 Julien SMADJA <julien.smadja@gmail.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package fr.fluxx.web.customtag;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.List;
 
 import javax.faces.component.FacesComponent;
 import javax.faces.component.UIComponentBase;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 
 import fr.fluxx.core.Services;
 import fr.fluxx.core.domain.Feed;
import fr.fluxx.core.service.CategoryService;
 import fr.fluxx.core.service.FeedService;
 
 @FacesComponent(value = "tagline")
 public class TagLine extends UIComponentBase {
 
	private final CategoryService categoryService;
 
 	public TagLine() {
 		super();
		categoryService = Services.getCategoryService();
 	}
 
 	@Override
 	public String getFamily() {
 		return "fluxx";
 	}
 
 	@Override
 	public void encodeEnd(FacesContext context) throws IOException {
 		ResponseWriter responseWriter = context.getResponseWriter();
 		Feed feed = (Feed) getAttributes().get("feed");
		List<String> categories = categoryService.findCategoriesByFeed(feed);
 		Collections.sort(categories);
 		for (String category : categories) {
 			responseWriter.startElement("img", this);
 			responseWriter.writeAttribute("src", context.getExternalContext().getRequestContextPath()
 					+ "/myimages/tag.png", "src");
 			responseWriter.endElement("img");
 			responseWriter.write(category);
 			responseWriter.write("&nbsp;&nbsp;&nbsp;");
 		}
 	}
 
 }
