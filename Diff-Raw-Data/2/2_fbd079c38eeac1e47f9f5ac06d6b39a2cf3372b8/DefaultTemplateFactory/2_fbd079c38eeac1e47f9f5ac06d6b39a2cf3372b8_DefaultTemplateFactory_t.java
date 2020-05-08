 /*
  * utils - DefaultTemplateFactory.java - Copyright © 2010 David Roden
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package net.pterodactylus.util.template;
 
 import java.io.Reader;
 
 /**
  * Default {@link TemplateFactory} implementation that creates {@link Template}s
  * with {@link HtmlFilter}s and {@link ReplaceFilter}s added.
  *
  * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
  */
 public class DefaultTemplateFactory implements TemplateFactory {
 
 	/** The default instance. */
 	private static DefaultTemplateFactory instance;
 
 	/** Whether to add an {@link HtmlFilter} to created templates. */
 	private final boolean addHtmlFilter;
 
 	/** Whether to add a {@link ReplaceFilter} to created templates. */
 	private final boolean addReplaceFilter;
 
 	/** Whether to add a {@link StoreFilter} to created templates. */
 	private final boolean addStoreFilter;
 
 	/** Whether to add a {@link InsertFilter} to created templates. */
 	private final boolean addInsertFilter;
 
	/** Whether to add a {@link DefaultFilter} to created templates. */
 	private final boolean addDefaultFilter;
 
 	/**
 	 * Creates a new default template factory that adds both an
 	 * {@link HtmlFilter} and a {@link ReplaceFilter} to created templates.
 	 */
 	public DefaultTemplateFactory() {
 		this(true, true);
 	}
 
 	/**
 	 * Creates a new default template factory.
 	 *
 	 * @param addHtmlFilter
 	 *            {@code true} to add an {@link HtmlFilter} to created
 	 *            templates, {@code false} otherwise
 	 * @param addReplaceFilter
 	 *            {@code true} to add a {@link ReplaceFilter} to created
 	 *            templates, {@code false} otherwise
 	 */
 	public DefaultTemplateFactory(boolean addHtmlFilter, boolean addReplaceFilter) {
 		this(addHtmlFilter, addReplaceFilter, true, true);
 	}
 
 	/**
 	 * Creates a new default template factory.
 	 *
 	 * @param addHtmlFilter
 	 *            {@code true} to add an {@link HtmlFilter} to created
 	 *            templates, {@code false} otherwise
 	 * @param addReplaceFilter
 	 *            {@code true} to add a {@link ReplaceFilter} to created
 	 *            templates, {@code false} otherwise
 	 * @param addStoreFilter
 	 *            {@code true} to add a {@link StoreFilter} to created
 	 *            templates, {@code false} otherwise
 	 * @param addInsertFilter
 	 *            {@code true} to add a {@link InsertFilter} to created
 	 *            templates, {@code false} otherwise
 	 */
 	public DefaultTemplateFactory(boolean addHtmlFilter, boolean addReplaceFilter, boolean addStoreFilter, boolean addInsertFilter) {
 		this(addHtmlFilter, addReplaceFilter, addStoreFilter, addInsertFilter, true);
 	}
 
 	/**
 	 * Creates a new default template factory.
 	 *
 	 * @param addHtmlFilter
 	 *            {@code true} to add an {@link HtmlFilter} to created
 	 *            templates, {@code false} otherwise
 	 * @param addReplaceFilter
 	 *            {@code true} to add a {@link ReplaceFilter} to created
 	 *            templates, {@code false} otherwise
 	 * @param addStoreFilter
 	 *            {@code true} to add a {@link StoreFilter} to created
 	 *            templates, {@code false} otherwise
 	 * @param addInsertFilter
 	 *            {@code true} to add a {@link InsertFilter} to created
 	 *            templates, {@code false} otherwise
 	 * @param addDefaultFilter
 	 *            {@code true} to add a {@link DefaultFilter} to created
 	 *            templates, {@code false} otherwise
 	 */
 	public DefaultTemplateFactory(boolean addHtmlFilter, boolean addReplaceFilter, boolean addStoreFilter, boolean addInsertFilter, boolean addDefaultFilter) {
 		this.addHtmlFilter = addHtmlFilter;
 		this.addReplaceFilter = addReplaceFilter;
 		this.addStoreFilter = addStoreFilter;
 		this.addInsertFilter = addInsertFilter;
 		this.addDefaultFilter = addDefaultFilter;
 	}
 
 	/**
 	 * Returns the static default instance of this template factory.
 	 *
 	 * @return The default template factory
 	 */
 	public synchronized static TemplateFactory getInstance() {
 		if (instance == null) {
 			instance = new DefaultTemplateFactory();
 		}
 		return instance;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Template createTemplate(Reader templateSource) {
 		Template template = new Template(templateSource);
 		if (addHtmlFilter) {
 			template.addFilter("html", new HtmlFilter());
 		}
 		if (addReplaceFilter) {
 			template.addFilter("replace", new ReplaceFilter());
 		}
 		if (addStoreFilter) {
 			template.addFilter("store", new StoreFilter());
 		}
 		if (addInsertFilter) {
 			template.addFilter("insert", new InsertFilter());
 		}
 		if (addDefaultFilter) {
 			template.addFilter("default", new DefaultFilter());
 		}
 		return template;
 	}
 
 }
