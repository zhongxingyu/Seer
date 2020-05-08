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
 package br.octahedron.cotopaxi.interceptor;
 
 import br.octahedron.cotopaxi.controller.ControllerResponse;
import br.octahedron.cotopaxi.controller.InputController;
 import br.octahedron.cotopaxi.view.response.TemplateResponse;
 
 /**
 * An interceptor that intercepts the {@link TemplateResponse} processing. It intercepts the
  * processing before the render, making possible decorate the writer to be used to render the
  * response or add attributes to output. This interceptors are globally and are executed for every
 * {@link TemplateResponse}.
  * 
  * @author Danilo Queiroz - daniloqueiroz@octahedron.com.br
  */
 public abstract class TemplateInterceptor extends InputController {
 
 	/**
	 * Executes this interceptor, for the given {@link TemplateResponse}, before the response
 	 * be render. It permits this interceptor to add attributes to {@link ControllerResponse} output
 	 * and/or decorates the writer to be used to render response.
 	 * 
 	 */
 	public abstract void preRender(TemplateResponse response);
 
 }
