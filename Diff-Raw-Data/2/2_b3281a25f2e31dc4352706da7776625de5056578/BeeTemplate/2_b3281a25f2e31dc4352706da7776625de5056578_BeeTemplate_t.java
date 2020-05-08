 /*
  * Copyright 2009 zaichu xiao
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package zcu.xutil.misc;
 
 import java.io.IOException;
 import java.util.Locale;
 import java.util.Map;
 import java.util.ResourceBundle;
 
 import org.beetl.core.Configuration;
 import org.beetl.core.Context;
 import org.beetl.core.GroupTemplate;
 import org.beetl.core.Template;
 import org.beetl.core.Function;
 import org.beetl.core.resource.FileResourceLoader;
 
 import zcu.xutil.web.Resolver;
 import zcu.xutil.web.WebContext;
 import zcu.xutil.web.Webutil;
 import static zcu.xutil.Objutil.*;
 
 public class BeeTemplate extends Configuration implements Resolver {
 	private static final Function nocache = new Function() {
 		public Object call(Object[] args, Context ctx) {
 			WebContext wc = (WebContext) ctx.getGlobal("xutils");
 			Webutil.applyCacheSeconds(wc.getResponse(), isEmpty(args) ? 0 : convert(args[0], int.class));
 			return null;
 		}
 	};
 
 	ResourceBundle bundle;
 	private volatile GroupTemplate group;
 
 	public BeeTemplate() throws IOException {
 		setNativeCall(true);
 	}
 
 	public synchronized void setRoot(String root) {
 		if (group == null) {
 			GroupTemplate g = new GroupTemplate(new FileResourceLoader(root, getCharset()), this);
 			g.registerFunction("nocache", nocache);
 			g.registerFunction("i18n", new Function() {
 				@Override
 				public Object call(Object[] args, Context ctx) {
 					Object o = args[0];
 					return o == null || bundle == null ? o : bundle.getString(o.toString());
 				}
 			});
 			g.registerFunction("error", new Function() {
 				@Override
 				public Object call(Object[] args, Context ctx) {
 					Map<String, String> errors = Webutil.getRequestErrors(((WebContext) ctx.getGlobal("xutils"))
 							.getRequest());
 					if (!isEmpty(args)) {
 						String key = errors.get(args[0]);
 						return key == null || bundle == null ? key : bundle.getString(key);
 					}
 					StringBuilder sb  = null;
 					for (String s : errors.values())
						(sb == null ? (sb = new StringBuilder()) :	sb.append("<br/>")).append(bundle == null ? s : bundle.getString(s));
 					return sb == null ? null : sb.toString();
 				}
 			});
 			group = g;
 		}
 	}
 
 	public void setLocale(Locale locale) {
 		bundle = ResourceBundle.getBundle(systring("xutils.web.buldle.name", "xutils-i18n"),
 				locale == null ? Locale.getDefault() : locale);
 	}
 
 	@Override
 	public void resolve(String view, Map<String, Object> variables, WebContext context) throws IOException {
 		if (group == null)
 			setRoot(context.getServletContext().getRealPath("/beetl"));
 		Template t = group.getTemplate(view);
 		t.binding(variables);
 		t.binding("xutils", context);
 		t.renderTo(Webutil.getTemplateWriter(context.getResponse()));
 	}
 }
