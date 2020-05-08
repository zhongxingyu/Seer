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
 package br.octahedron.cotopaxi.view.response;
 
 import java.io.IOException;
 import java.io.OutputStream;
import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletResponse;
 
 import br.octahedron.cotopaxi.controller.ControllerContext;
 import br.octahedron.cotopaxi.inject.Inject;
 import br.octahedron.cotopaxi.view.OutputStreamBuilder;
 import br.octahedron.cotopaxi.view.render.TemplateRender;
 
 /**
  * A {@link RenderableResponse} that render and write templates.
  * 
  * @see {@link TemplateRender}
  * 
  * @author Danilo Queiroz - daniloqueiroz@octahedron.com.br
  */
 public class TemplateResponse extends RenderableResponse {
 
 	@Inject
 	private TemplateRender templateRender;
 
 	private OutputStreamBuilder builder = null;
 	private String template;
	
	public TemplateResponse(String template, int code, Map<String, Object> output, Locale locale) {
		super(code, output, new HashMap<String, String>(), new HashMap<String, String>(), locale);
		this.template = template;
	}
 
 	public TemplateResponse(String template, int code, Map<String, Object> output, Map<String, String> cookies, Map<String, String> headers,
 			Locale locale) {
 		super(code, output, cookies, headers, locale);
 		this.template = template;
 	}
 
 	public TemplateResponse(String template, int code, ControllerContext context) {
 		this(template, code, context.getOutput(), context.getCookies(), context.getHeaders(), context.getLocale());
 	}
 
 	/**
 	 * @param templateRender
 	 *            the templateRender to set
 	 */
 	public void setTemplateRender(TemplateRender templateRender) {
 		this.templateRender = templateRender;
 	}
 
 	/**
 	 * Gets the {@link OutputStream} to be used to write output.
 	 * 
 	 * @param servletResponse
 	 *            The {@link ServletResponse} to be used to create the {@link OutputStream}
 	 * @return The {@link OutputStream} to be used to write response.
 	 * 
 	 * @throws IOException
 	 *             If some error occurs loading the {@link OutputStream}
 	 */
 	protected OutputStream getOutputStream(HttpServletResponse servletResponse) throws IOException {
 		OutputStream outStream = servletResponse.getOutputStream();
 		if (this.builder != null) {
 			outStream = this.builder.createOutputStream(outStream);
 		}
 
 		return outStream;
 	}
 
 	/**
 	 * Sets the {@link OutputStreamBuilder} to be used to create the Ou
 	 * 
 	 * @param gzipBuilder
 	 */
 	public void setOutputStreamBuilder(OutputStreamBuilder gzipBuilder) {
 		this.builder = gzipBuilder;
 	}
 
 	/**
 	 * Adds a new object to output. If there's already exists an object for the given key, the
 	 * original one is kept.
 	 * 
 	 * @param key
 	 *            The object's key
 	 * 
 	 * @param value
 	 *            The object's value
 	 */
 	public void addOutput(String key, Object value) {
 		if (!this.output.containsKey(key)) {
 			this.output.put(key, value);
 		}
 	}
 
 	/**
 	 * Adds a new object to output. If there's already exists an object for the given key, the
 	 * original one is kept.
 	 * 
 	 * @param key
 	 *            The object's key
 	 * 
 	 * @param value
 	 *            The object's value
 	 */
 	public void addHeader(String key, String value) {
 		if (!this.headers.containsKey(key)) {
 			this.headers.put(key, value);
 		}
 	}
 
 	/**
 	 * Adds a new object to output. If there's already exists an object for the given key, the
 	 * original one is kept.
 	 * 
 	 * @param key
 	 *            The object's key
 	 * 
 	 * @param value
 	 *            The object's value
 	 */
 	public void addCookies(String key, String value) {
 		if (!this.cookies.containsKey(key)) {
 			this.cookies.put(key, value);
 		}
 	}
 	
 	/**
 	 * Gets the current locale to be used to render this template
 	 */
 	public Locale getLocale() {
 		return this.locale;
 	}
 	
 	/**
 	 * Set's the locale to be used to render this template.
 	 */
 	public void setLocale(Locale lc) {
 		this.locale = lc;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 */
 	@Override
 	public String getContentType() {
 		return "text/html";
 	}
 
 	/*
 	 * (non-Javadoc)
 	 */
 	@Override
 	protected void render() {
 		this.templateRender.render(this.template, this.output, this.writer);
 	}
 }
