 /*
  * This file is a component of thundr, a software library from 3wks.
  * Read more: http://www.3wks.com.au/thundr
  * Copyright (C) 2013 3wks, <thundr@3wks.com.au>
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
 package com.threewks.thundr.view.json;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import jodd.util.MimeTypes;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.threewks.thundr.view.ViewResolutionException;
 import com.threewks.thundr.view.ViewResolver;
 
 public class JsonViewResolver implements ViewResolver<JsonView> {
 	private GsonBuilder gsonBuilder;
 
 	public JsonViewResolver() {
 		this(new GsonBuilder());
 	}
 
 	public JsonViewResolver(GsonBuilder gsonBuilder) {
 		this.gsonBuilder = gsonBuilder;
 	}
 
 	@Override
 	public void resolve(HttpServletRequest req, HttpServletResponse resp, JsonView viewResult) {
 		Object output = viewResult.getOutput();
 		try {
             Gson create = gsonBuilder.create();
 			String json = create.toJson(output);
 			resp.setContentType(MimeTypes.MIME_APPLICATION_JSON);
 			resp.setContentLength(json.getBytes().length);
 			resp.setStatus(HttpServletResponse.SC_OK);
             resp.getWriter().write(json);

 		} catch (Exception e) {
 			throw new ViewResolutionException(e, "Failed to generate JSON output for object '%s': %s", output.toString(), e.getMessage());
 		}
 	}
 	
 	@Override
 	public String toString() {
 		return this.getClass().getSimpleName();
 	}
 }
