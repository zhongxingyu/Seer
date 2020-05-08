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
 package br.octahedron.cotopaxi.view;
 
import java.util.logging.Logger;

 import org.apache.velocity.Template;
 import org.apache.velocity.app.VelocityEngine;
 import org.apache.velocity.exception.ParseErrorException;
 import org.apache.velocity.exception.ResourceNotFoundException;
 
 import br.octahedron.cotopaxi.config.CotopaxiConfigView;
 
 /**
  * A simple wrapper for {@link VelocityEngine}. It load and configure the velocity engine and
  * provides access to the unique VelocityEngine instance, used to load the Templates.
  * 
  * @author Danilo Penna Queiroz - email@octahedron.com.br
  * 
  */
 public final class VelocityEngineWrapper {
 
	private static final Logger logger = Logger.getLogger(VelocityEngineWrapper.class.getName());
 	private static final VelocityEngine engine = new VelocityEngine();
 	private static boolean init = false;
 
 	/**
 	 * Inits and configure the VelocityEngine
 	 */
 	protected static void init() {
 		if (!init) {
 			CotopaxiConfigView config = CotopaxiConfigView.Handler.getConfigView();
			String templateRoot = config.getTemplateRoot();
			logger.fine("Loading velocity engine. Templates root folder: " + templateRoot);			
 			engine.setProperty("resource.loader", "file");
 			engine.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
			engine.setProperty("file.resource.loader.path", templateRoot);
 			engine.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.Log4JLogChute");
 			engine.setProperty("runtime.log.logsystem.log4j.logger", VelocityEngineWrapper.class.getName());
 			engine.init();
 		}
 	}
 
 	/**
 	 * Loads the template.
 	 * 
 	 * @param template
 	 *            The template file name.
 	 * @return The template loaded.
 	 * @throws ResourceNotFoundException
 	 *             If the template was not found.
 	 * @throws ParseErrorException
 	 *             If there's syntax errors on template.
 	 */
 	public static Template getTemplate(String template) throws ResourceNotFoundException, ParseErrorException {
 		if (!init) {
 			init();
 		}
		logger.fine("Trying to load template: " + template);
 		return engine.getTemplate(template);
 	}
 }
