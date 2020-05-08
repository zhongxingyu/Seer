 /*
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
 package org.hobsoft.symmetry.demo.jaxrs;
 
 import javax.ws.rs.ext.ContextResolver;
 import javax.ws.rs.ext.Provider;
 
 import org.hobsoft.symmetry.hydrate.ComponentRenderKit;
 import org.hobsoft.symmetry.ui.html.HtmlRenderKit;
 
 /**
  * 
  * 
  * @author Mark Hobson
  */
 @Provider
 public class HtmlRenderKitProvider implements ContextResolver<ComponentRenderKit>
 {
 	// ContextResolver methods ------------------------------------------------
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public ComponentRenderKit getContext(Class<?> type)
 	{
 		return new HtmlRenderKit();
 	}
 }
