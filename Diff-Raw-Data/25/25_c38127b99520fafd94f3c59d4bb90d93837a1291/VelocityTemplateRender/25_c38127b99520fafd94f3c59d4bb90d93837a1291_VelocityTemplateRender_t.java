 /*
  *  This file is part of Cotopaxi.
  *
  *  Cotopaxi is free software: you can redistribute it and/or modify
  *  it under the terms of the Lesser GNU General Public License as published
  *  by the Free Software Foundation, either version 3 of the License, or
  *  any later version.
  *
  *  Cotopaxi is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  Lesser GNU General Public License for more details.
  *
  *  You should have received a copy of the Lesser GNU General Public License
  *  along with Cotopaxi. If not, see <http://www.gnu.org/licenses/>.
  */
 package br.octahedron.cotopaxi.view.render;
 
import static java.nio.charset.Charset.defaultCharset;
 import static br.octahedron.cotopaxi.CotopaxiProperty.getProperty;
 import static br.octahedron.cotopaxi.CotopaxiProperty.TEMPLATE_FOLDER;
 import java.io.Writer;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.velocity.Template;
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.VelocityEngine;
 
 import br.octahedron.util.Log;
 
 /**
  * VelocityTemplateRender is responsible for rendering velocity templates.
  * 
  * Generally used on controllers to render the attributes of request.
  * 
  * @author Vítor Avelino - vitoravelino@octahedron.com.br
  * @author Danilo Queiroz - daniloqueiroz@octahedron.com.br
  */
 public class VelocityTemplateRender implements TemplateRender {
 
 	private static final Log log = new Log(VelocityTemplateRender.class);
 	private static final String VELOCIMACRO_LIBRARY = "macros.vm";  
 	
 	private final VelocityEngine engine = new VelocityEngine();
 	private String templateFolder;
 	
 	public VelocityTemplateRender() {
 		this.templateFolder = getProperty(TEMPLATE_FOLDER);
 		if (!this.templateFolder.endsWith("/")) {
 			this.templateFolder += '/';
 		}
 		Properties p = new Properties();
 		p.setProperty("velocimacro.library", this.templateFolder + VELOCIMACRO_LIBRARY);
 		// FIXME need refactoring. use the same solution as the OutputStreamWriter
		p.setProperty("input.encoding", defaultCharset().toString());
		p.setProperty("output.encoding", defaultCharset().toString());
 		engine.init(p);
 	}
 	
 	/* (non-Javadoc)
 	 * @see br.octahedron.cotopaxi.view.render.TemplateRender#render(java.lang.String, java.util.Map, java.io.Writer)
 	 */
 	public void render(String templatePath, Map<String,Object> output, Writer writer) {
 		log.info("Rendering template %s", templatePath);
 		VelocityContext context = new VelocityContext(output);
 		Template template = engine.getTemplate(this.templateFolder + templatePath);
 		template.merge(context, writer);
 	}
 }
 
