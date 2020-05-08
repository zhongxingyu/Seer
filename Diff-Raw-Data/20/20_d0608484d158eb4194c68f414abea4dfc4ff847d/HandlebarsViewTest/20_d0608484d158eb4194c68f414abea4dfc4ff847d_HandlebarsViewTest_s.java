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
 package com.threewks.thundr.view.handlebars;
 
 import static org.hamcrest.Matchers.*;
 import static org.junit.Assert.assertThat;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.junit.Test;
 
 public class HandlebarsViewTest {
 
 	@Test
 	public void shouldRetainViewNameAndModel() {
 		Map<String, Object> model = new HashMap<String, Object>();
 		HandlebarsView handlebarsView = new HandlebarsView("/path/view", model);
 		assertThat(handlebarsView.getView(), is("/path/view.hbs"));
 		assertThat(handlebarsView.getModel(), is(model));
 	}
 
 	@Test
 	public void shouldRetainViewNameAndHaveEmptyModel() {
 		HandlebarsView handlebarsView = new HandlebarsView("path/view");
 		assertThat(handlebarsView.getView(), is("/WEB-INF/hbs/path/view.hbs"));
 		assertThat(handlebarsView.getModel(), is(notNullValue()));
 		assertThat(handlebarsView.getModel().size(), is(0));
 	}
 
 	@Test
 	public void shouldHaveViewNameAndCompleteViewNameAsToString() {
 		Map<String, Object> model = new HashMap<String, Object>();
 		model.put("thing", "in");
 		HandlebarsView handlebarsView = new HandlebarsView("/path/view", model);
 		assertThat(handlebarsView.toString(), is("/path/view (/path/view.hbs)"));
 	}
	
 	@Test
 	public void shouldReturnViewPathRelativeToWebInfAndForHbsWhenPartialViewNameGiven() {
 		assertThat(new HandlebarsView("view").getView(), is("/WEB-INF/hbs/view.hbs"));
 		assertThat(new HandlebarsView("view.hbs").getView(), is("/WEB-INF/hbs/view.hbs"));
 		assertThat(new HandlebarsView("path/view.hbs").getView(), is("/WEB-INF/hbs/path/view.hbs"));
 		assertThat(new HandlebarsView("path/view").getView(), is("/WEB-INF/hbs/path/view.hbs"));
 	}
 
 	@Test
 	public void shouldReturnViewPathWithSuffixIfNoneProvided() {
 		assertThat(new HandlebarsView("view").getView(), is("/WEB-INF/hbs/view.hbs"));
 		assertThat(new HandlebarsView("path/view").getView(), is("/WEB-INF/hbs/path/view.hbs"));
 		assertThat(new HandlebarsView("view.html").getView(), is("/WEB-INF/hbs/view.html"));
 		assertThat(new HandlebarsView("path/view.html").getView(), is("/WEB-INF/hbs/path/view.html"));
 	}
 	@Test
 	public void shouldReturnViewNameForToString() {
 		assertThat(new HandlebarsView("/WEB-INF/hbs/view.hbs").toString(), is("/WEB-INF/hbs/view.hbs"));
 		assertThat(new HandlebarsView("view").toString(), is("view (/WEB-INF/hbs/view.hbs)"));
 		assertThat(new HandlebarsView("view.hbs").toString(), is("view.hbs (/WEB-INF/hbs/view.hbs)"));
 		assertThat(new HandlebarsView("path/view.hbs").toString(), is("path/view.hbs (/WEB-INF/hbs/path/view.hbs)"));
 		assertThat(new HandlebarsView("path/view").toString(), is("path/view (/WEB-INF/hbs/path/view.hbs)"));
 	}
 }
