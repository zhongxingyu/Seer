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
 package br.octahedron.cotopaxi.view.formatter;
 
 import static br.octahedron.cotopaxi.view.VelocityEngineWrapper.getTemplate;
 
 import java.util.Locale;
 import java.util.Map;
 
 import org.apache.velocity.Template;
 import org.apache.velocity.VelocityContext;
 
 import br.octahedron.cotopaxi.view.ContentType;
 import br.octahedron.util.StringBuilderWriter;
 
 /**
  * A formatter for Velocity
  * 
  * @author Danilo Penna Queiroz - daniloqueiroz@octahedron.com.br
  * 
  */
 public class VelocityFormatter extends TemplateFormatter {
 
 	public VelocityFormatter() {
 		super(ContentType.HTML);
 	}
 
 	public VelocityFormatter(String template, Map<String, Object> attributes, Locale lc) {
 		super(ContentType.HTML);
 		this.setTemplate(template);
 		this.setAttributes(attributes);
 		this.setLocale(lc);
 	}
 
 	@Override
 	protected String doFormat() {
 		// TODO locale
 		VelocityContext context = new VelocityContext(this.attributes);
 		Template template = getTemplate(this.template);
 		StringBuilderWriter writer = new StringBuilderWriter();
 		template.merge(context, writer);
 		return writer.getBuffer().toString();
 	}
 }
