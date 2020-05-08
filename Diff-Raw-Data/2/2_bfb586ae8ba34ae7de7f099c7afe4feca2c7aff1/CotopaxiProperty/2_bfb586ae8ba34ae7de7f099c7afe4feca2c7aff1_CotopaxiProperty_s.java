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
 package br.octahedron.cotopaxi;
 
 import java.nio.charset.Charset;
 import java.text.DateFormat;
 import java.text.NumberFormat;
 import java.util.TimeZone;
 
 import br.octahedron.cotopaxi.i18n.LocaleManager;
 import br.octahedron.util.DateUtil;
 
 /**
  * The CotopaxiFramework's properties and default values.
  * 
  * To overwrite this values, just use {@link System#setProperty(String, String)} or pass the
  * property value using the -D parameter.
  * 
  * E.g.: System.setProperty("TEMPLATE_FOLDER","tpls/");
  * 
  * 
  * @author Danilo Queiroz - daniloqueiroz@octahedron.com.br
  * 
  */
 public enum CotopaxiProperty {
 
 	/**
 	 * The TemplateRender to be used to render templates. Default:
 	 * br.octahedron.cotopaxi.view.render.VelocityTemplateRender
 	 * 
 	 * @see br.octahedron.cotopaxi.view.render.TemplateRender
 	 * 
 	 */
 	TEMPLATE_RENDER("br.octahedron.cotopaxi.view.render.VelocityTemplateRender"),
 	/**
 	 * Templates' folder. Default: templates/
 	 */
 	TEMPLATE_FOLDER("templates/"),
 	/**
 	 * Server Error (500) template. Default: error.vm
 	 */
 	ERROR_TEMPLATE("error.vm"),
 	/**
 	 * Forbidden Error (403) template. Default: forbidden.vm
 	 */
 	FORBIDDEN_TEMPLATE("notauthorized.vm"),
 	/**
 	 * Bad Request Error (400) template. Default: invalid.vm
 	 */
 	INVALID_TEMPLATE("invalid.vm"),
 	/**
 	 * Not Found Error (404) template. Default: notFound.vm
 	 */
 	NOT_FOUND_TEMPLATE("notfound.vm"),
 	/**
 	 * The property used to binds errors on controller top level. Default: error
 	 */
 	ERROR_PROPERTY("error"),
 	/**
 	 * The property used to binds validation errors on controller top level. Default: invalid
 	 */
 	INVALID_PROPERTY("invalid"),
 	/**
 	 * The application base url. Default: http://localhost:8080
 	 */
 	APPLICATION_BASE_URL("http://localhost:8080"),
 	/**
 	 * The application I18N files base folder. Default: i18n/
 	 */
 	I18N_FOLDER("i18n"),
 	/**
 	 * TODO improve doc I18N Base file. Default: master
 	 * 
 	 * @see LocaleManager
 	 */
 	I18N_BASE_FILE("master"),
 	/**
 	 * List the supported Locales for internationalization (i18n). Locales should be separated by
 	 * commas. E.g.: en_US, en, pt_BR
 	 * 
 	 * Default: en
 	 */
 	I18N_SUPPORTED_LOCALES("en"),
 	/**
 	 * The property used to binds internationalization String map. Default: i18n
 	 */
 	I18N_PROPERTY("i18n"),
 	/**
 	 * The property used to binds internationalization {@link DateFormat}. Default: dateFormat
 	 */
 	I18N_DATE_FORMAT_PROPERTY("dateFormat"),
 	/**
 	 * The property used to binds internationalization {@link NumberFormat}. Default: numberFormat
 	 */
 	I18N_NUMBER_FORMAT_PROPERTY("numberFormat"),
 	/**
 	 * The system {@link TimeZone} to be used by {@link DateUtil} methods. Default: none/JVM
 	 * TimeZone
 	 * 
 	 * The timezone property should indicates the time zone offset. Eg.: +0200, -0300, +0000
 	 */
 	TIMEZONE(null);
 
 	private String defaultValue;
 
 	private CotopaxiProperty(String defaultValue) {
 		this.defaultValue = defaultValue;
 	}
 
 	/**
 	 * Gets the default value
 	 */
 	public String defaultValue() {
 		return this.defaultValue;
 	}
 
 	/**
 	 * The Running Modes for a Cotopaxi Application
 	 */
 	public enum RunningMode {
 		UNKNOWN, TEST, DEVELOPMENT, PRODUCTION;
 	}
 	
 	/**
 	 * Gets the current value for the given property. If the property wasn't defined, it will return
 	 * the default value.
 	 * 
 	 * @param property
 	 *            The {@link CotopaxiProperty} to retrieve the value
 	 * @return The current value for the given property
 	 */
 	public static String property(CotopaxiProperty property) {
 		return System.getProperty(property.name(), property.defaultValue());
 	}
 
 	/**
 	 * Gets the current value for the given property. If the property wasn't defined, it will return
 	 * the default value, if the property is one of the {@link CotopaxiProperty} or
 	 * <code>null</code> for other properties.
 	 * 
 	 * @param property
 	 *            The property name to retrieve the value
 	 * @return The current value for the given property
 	 */
 	public static String property(String property) {
 		return System.getProperty(property);
 	}
 
 	/**
 	 * Gets the charset to be used by application. It tries to retrieve the charset from the
 	 * <b>file.encoding</b> property, that can be defined either at JVM initialization or through
 	 * the configuration file. It the property <b>file.encoding</b> isn't defined, it recovers the
 	 * JVM's default charset using the {@link Charset#defaultCharset()} method.
 	 * 
 	 * @return The application charset to be used.
 	 */
 	public static Charset charset() {
 		String charset = System.getProperty("file.encoding");
 		if (charset != null) {
 			return Charset.forName(charset);
 		} else {
 			return Charset.defaultCharset();
 		}
 	}
 
 	protected static final String RUNNING_MODE_PROP = "ctpx.running.mode";
 
 	/**
 	 * Gets the application {@link RunningMode}
 	 * 
 	 * @return the application {@link RunningMode}
 	 */
 	public static RunningMode runningMode() {
 		String mode = property(RUNNING_MODE_PROP);
 		if (mode == null) {
 			discoverRunningMode();
 		}
 		return RunningMode.valueOf(property(RUNNING_MODE_PROP));
 	}
 
 	/**
 	 * Tries to discover the current running mode. It looks for platform specific environment
 	 * properties
 	 */
 	private static void discoverRunningMode() {
 		String gae = property("com.google.appengine.runtime.environment");
 		if (gae != null) {
			System.setProperty(RUNNING_MODE_PROP, gae);
 		} else {
 			System.setProperty(RUNNING_MODE_PROP, "unknown");
 		}
 	}
 
 	/**
 	 * Reset all the {@link CotopaxiProperty} to they default values.
 	 */
 	public static void forceReset() {
 		for (CotopaxiProperty prop : CotopaxiProperty.values()) {
 			System.clearProperty(prop.name());
 		}
 	}
 
 	/**
 	 * Gets the current value for the given property. If the property wasn't defined, it will return
 	 * the default value, if the property is one of the {@link CotopaxiProperty} or
 	 * <code>null</code> for other properties.
 	 * 
 	 * @param property
 	 *            The property name to retrieve the value
 	 * @return The current value for the given property
 	 * @deprecated
 	 */
 	public static String getProperty(String property) {
 		return property(property);
 	}
 
 	/**
 	 * Gets the current value for the given property. If the property wasn't defined, it will return
 	 * the default value.
 	 * 
 	 * @param property
 	 *            The {@link CotopaxiProperty} to retrieve the value
 	 * @return The current value for the given property
 	 * @deprecated
 	 */
 	public static String getProperty(CotopaxiProperty property) {
 		return property(property);
 	}
 
 	/**
 	 * Gets the charset to be used by application. It tries to retrieve the charset from the
 	 * <b>file.encoding</b> property, that can be defined either at JVM initialization or through
 	 * the configuration file. It the property <b>file.encoding</b> isn't defined, it recovers the
 	 * JVM's default charset using the {@link Charset#defaultCharset()} method.
 	 * 
 	 * @return The application charset to be used.
 	 * @deprecated
 	 */
 	public static Charset getCharset() {
 		return charset();
 	}
 }
