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
 package com.threewks.thundr.rest.intercept;
 
 
 import com.threewks.thundr.action.method.ActionInterceptor;
 import com.threewks.thundr.http.exception.HttpStatusException;
 import com.threewks.thundr.logger.Logger;
 import com.threewks.thundr.rest.RestView;
 import com.threewks.thundr.rest.RestViewResolver;
 import com.threewks.thundr.rest.dto.ErrorDto;
 import org.apache.commons.lang3.exception.ExceptionUtils;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class RestActionInterceptor implements ActionInterceptor<Rest> {
 
 	RestViewResolver viewResolver;
 
 	public RestActionInterceptor(RestViewResolver viewResolver) {
 		this.viewResolver = viewResolver;
 	}
 
 	@Override
 	public <T> T before(Rest annotation, HttpServletRequest req, HttpServletResponse res) {
 		return null;
 	}
 
 	@Override
 	public <T> T after(Rest annotation, HttpServletRequest req, HttpServletResponse res) {
 		return null;
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public <T> T exception(Rest annotation, Exception e, HttpServletRequest req, HttpServletResponse res) {
		HttpStatusException statusException;
		if (e instanceof  HttpStatusException) {
			statusException = (HttpStatusException) e;
		} else {
			statusException = new HttpStatusException(e, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error: %s", e.getMessage());
		}
 
		String description = (statusException.getStatus() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) ?
				ExceptionUtils.getStackTrace(e) : e.getMessage();
		Logger.error("REST exception: %s - %s", statusException.getStatus(), description);
 
		return (T) new RestView(new ErrorDto(e.getMessage()), statusException.getStatus());
 	}
 }
