 /*
  * Copyright 2006 Luca Garulli (luca.garulli--at--assetdata.it)
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
 
 package org.romaframework.aspect.i18n;
 
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.text.NumberFormat;
 import java.text.SimpleDateFormat;
 import java.util.Locale;
 
 import org.romaframework.aspect.i18n.feature.I18nFieldFeatures;
 import org.romaframework.aspect.session.SessionAspect;
 import org.romaframework.core.Roma;
 import org.romaframework.core.Utility;
 import org.romaframework.core.flow.Controller;
 import org.romaframework.core.flow.SchemaFieldListener;
 import org.romaframework.core.module.SelfRegistrantConfigurableModule;
 import org.romaframework.core.schema.Feature;
 import org.romaframework.core.schema.SchemaAction;
 import org.romaframework.core.schema.SchemaClassDefinition;
 import org.romaframework.core.schema.SchemaClassElement;
 import org.romaframework.core.schema.SchemaElement;
 import org.romaframework.core.schema.SchemaEvent;
 import org.romaframework.core.schema.SchemaFeatures;
 import org.romaframework.core.schema.SchemaField;
 import org.romaframework.core.schema.SchemaObject;
 import org.romaframework.core.util.parser.ObjectVariableResolver;
 
 /**
  * I18N Aspect abstract implementation handle the aspect configuration. Extend this if you want to write your own Aspect
  * implementation.
  * 
  * @author Luca Garulli (luca.garulli--at--assetdata.it)
  */
public abstract class I18NAspectAbstract extends SelfRegistrantConfigurableModule<String> implements I18NAspect, SchemaFieldListener {
 
 	private static final String	DATE_TIME_FORMAT_VAR	= "Application.DateTimeFormat";
 	private static final String	TIME_FORMAT_VAR				= "Application.TimeFormat";
 	private static final String	DATE_FORMAT_VAR				= "Application.DateFormat";
 	private static final String	NUMBER_FORMAT_VAR			= "Application.NumberFormat";
 	public static final String	LABEL_NAME						= "label";
 
 	public I18NAspectAbstract() {
 		Controller.getInstance().registerListener(SchemaFieldListener.class, this);
 	}
 
 	public void beginConfigClass(SchemaClassDefinition iClass) {
 	}
 
 	public void endConfigClass(SchemaClassDefinition iClass) {
 	}
 
 	public void configField(SchemaField iField) {
 
 		setFieldDefaults(iField);
 	}
 
 	private void setFieldDefaults(SchemaField field) {
 	}
 
 	public Object onAfterFieldRead(Object iContent, SchemaField iField, Object iCurrentValue) {
 		if (iContent == null || iCurrentValue == null) {
 			return null;
 		}
 
 		String key = (String) iField.getFeature(I18nFieldFeatures.KEY);
 
 		if (key == null) {
 			return iCurrentValue;
 		}
 
 		Object ret = resolve(iField, iCurrentValue.toString(), I18NType.CONTENT, iContent);
 		if (ret != null) {
 			return ret;
 		} else {
 			return iCurrentValue;
 		}
 	}
 
 	public Locale getLocale() {
 		Locale loc = null;
 		SessionAspect sess = Roma.aspect(SessionAspect.ASPECT_NAME);
 		if (sess != null)
 			loc = sess.getActiveLocale();
 
 		if (loc == null)
 			loc = Locale.getDefault();
 		return loc;
 	}
 
 	public Object onBeforeFieldRead(Object content, SchemaField field, Object currentValue) {
 		return IGNORED;
 	}
 
 	public Object onBeforeFieldWrite(Object content, SchemaField field, Object currentValue) {
 		return currentValue;
 	}
 
 	public Object onAfterFieldWrite(Object content, SchemaField field, Object currentValue) {
 		return currentValue;
 	}
 
 	public void configAction(SchemaAction action) {
 	}
 
 	public void configClass(SchemaClassDefinition class1) {
 	}
 
 	public void configEvent(SchemaEvent event) {
 	}
 
 	public Object getUnderlyingComponent() {
 		return null;
 	}
 
 	public DateFormat getDateTimeFormat() {
 		return getDateTimeFormat(getLocale());
 	}
 
 	public DateFormat getDateTimeFormat(Locale iLocale) {
 		if (iLocale == null) {
 			iLocale = getLocale();
 		}
 		String format = find(DATE_TIME_FORMAT_VAR, iLocale);
 		if (format == null)
 			format = "dd/MM/yyyy HH:mm:ss";
 		return new SimpleDateFormat(format);
 	}
 
 	public DateFormat getDateFormat() {
 		return getDateFormat(getLocale());
 	}
 
 	public DateFormat getDateFormat(Locale iLocale) {
 		if (iLocale == null) {
 			iLocale = getLocale();
 		}
 		String format = find(DATE_FORMAT_VAR, iLocale);
 		if (format == null)
 			format = "dd/MM/yyyy";
 		return new SimpleDateFormat(format, iLocale);
 	}
 
 	public DateFormat getTimeFormat() {
 		return getTimeFormat(getLocale());
 	}
 
 	public DateFormat getTimeFormat(Locale iLocale) {
 		if (iLocale == null) {
 			iLocale = getLocale();
 		}
 		String format = find(TIME_FORMAT_VAR, iLocale);
 		if (format == null)
 			format = "HH:mm:ss";
 		return new SimpleDateFormat(format, iLocale);
 
 	}
 
 	public NumberFormat getNumberFormat() {
 		return getNumberFormat(getLocale());
 	}
 
 	public NumberFormat getNumberFormat(Locale iLocale) {
 		if (iLocale == null) {
 			iLocale = getLocale();
 		}
 		String format = find(NUMBER_FORMAT_VAR, iLocale);
 		if (format == null)
 			format = "###,###,###.#####";
 		return new DecimalFormat(format, new DecimalFormatSymbols(iLocale));
 	}
 
 	public String get(Object obj, I18NType type, Object... iArgs) {
 		return get(getSchemaClassDefinition(obj), type, rebuildArgs(obj, iArgs));
 	}
 
 	protected SchemaClassDefinition getSchemaClassDefinition(Object obj) {
 		SessionAspect sessionAspect = Roma.session();
 		if (sessionAspect != null)
 			return sessionAspect.getSchemaObject(obj);
 		return Roma.schema().getSchemaClass(obj);
 	}
 
 	protected SchemaFeatures getSchemaElement(Object obj, String key) {
 		SchemaClassDefinition scd = getSchemaClassDefinition(obj);
 		if (scd == null || key == null)
 			return null;
 		SchemaFeatures sf = scd.getField(key);
 		if (sf == null) {
 			sf = scd.getAction(key);
 		}
 		return sf;
 	}
 
 	public String get(Object obj, String key, I18NType type, Object... iArgs) {
 		return get(getSchemaElement(obj, key), type, rebuildArgs(obj, iArgs));
 	}
 
 	private Object[] rebuildArgs(Object obj, Object[] args) {
 		if (args == null)
 			return new Object[] { obj };
 		if (args.length > 0 && args[0] == obj)
 			return args;
 		Object[] newa = new Object[args.length + 1];
 		newa[0] = obj;
 		System.arraycopy(args, 0, newa, 1, args.length);
 		return newa;
 	}
 
 	public String get(SchemaFeatures iObjectClass, I18NType type, Object... iArgs) {
 		return getFeatures(iObjectClass, type.getName(), null, iArgs);
 	}
 
 	public String get(SchemaFeatures iObjectClass, String customType, Object... iArgs) {
 		return getFeatures(iObjectClass, CONTEXT_SEPARATOR + customType, null, iArgs);
 	}
 
 	public String get(SchemaFeatures iObjectClass, I18NType type, Feature<String> featureOverride, Object... iArgs) {
 		return getFeatures(iObjectClass, type.getName(), featureOverride, iArgs);
 	}
 
 	private String getFeatures(SchemaFeatures iObjectClass, String type, Feature<String> featureOverride, Object... iArgs) {
 		if (iObjectClass == null || type == null)
 			return "";
 		if (iObjectClass instanceof SchemaObject) {
 			iArgs = rebuildArgs(((SchemaObject) iObjectClass).getInstance(), iArgs);
 		}
 		if (featureOverride != null && iObjectClass.isRuntimeSet(featureOverride)) {
 			return iObjectClass.getFeature(featureOverride);
 		}
 		String solved = null;
 		if (iObjectClass instanceof SchemaElement) {
 			solved = findWithSchemaElement(((SchemaElement) iObjectClass), type);
 		} else if (iObjectClass instanceof SchemaClassDefinition) {
 			solved = findWithSchemaClass(((SchemaClassDefinition) iObjectClass), type);
 		}
 		if (solved != null)
 			return fill(solved, iArgs);
 		if (featureOverride != null && iObjectClass.isSettedFeature(featureOverride)) {
 			return iObjectClass.getFeature(featureOverride);
 		}
 		if (".label".equals(type)) {
 			if (iObjectClass instanceof SchemaElement)
 				return Utility.getClearName(((SchemaElement) iObjectClass).getName());
 			return Utility.getClearName(((SchemaClassDefinition) iObjectClass).getName());
 		}
 		return "";
 	}
 
 	private String findWithSchemaElement(SchemaElement element, String type) {
 		if (!(element instanceof SchemaClassElement))
 			return null;
 		SchemaClassElement classElement = (SchemaClassElement) element;
 		return findWithSchemaClass(classElement.getEntity(), CONTEXT_SEPARATOR + classElement.getName() + type);
 	}
 
 	private String findWithSchemaClass(SchemaClassDefinition clazz, String type) {
 		Class<?> entity = (Class<?>) clazz.getSchemaClass().getLanguageType();
 		StringBuilder builder = new StringBuilder();
 		do {
 			builder.append(entity.getSimpleName());
 			builder.append(type);
 			String res = find(builder.toString());
 			if (res != null) {
 				put(((Class<?>) clazz.getSchemaClass().getLanguageType()).getSimpleName() + type, res, getLocale());
 				return res;
 			}
 			builder.setLength(0);
 			entity = entity.getSuperclass();
 		} while (entity != null);
 		return null;
 	}
 
 	public String get(Locale locale, String string, Object... iArgs) {
 		String find = find(string, locale);
 		if (find == null)
 			return "";
 		return fill(find, iArgs);
 	}
 
 	public String get(String string, Object... iArgs) {
 		String find = find(string);
 		if (find == null)
 			return "";
 		return fill(find, iArgs);
 	}
 
 	public String resolve(String iText, Object... iArgs) {
 		if (!iText.startsWith("$"))
 			return fill(iText, iArgs);
 		return get(iText.substring(1), iArgs);
 	}
 
 	public String resolve(Object obj, String iText, I18NType type, Object... iArgs) {
 		iArgs = rebuildArgs(obj, iArgs);
 		if (!iText.startsWith(VARNAME_PREFIX))
 			return fill(iText, iArgs);
 		SchemaClassDefinition scd = getSchemaClassDefinition(obj);
 		if (scd == null)
 			return "";
 		String str = findWithSchemaClass(scd, CONTEXT_SEPARATOR + iText.substring(1) + type.getName());
 		if (str == null)
 			return "";
 		return fill(str, iArgs);
 	}
 
 	public String resolve(Object obj, String iText, String customType, Object... iArgs) {
 		iArgs = rebuildArgs(obj, iArgs);
 		if (!iText.startsWith(VARNAME_PREFIX))
 			return fill(iText, iArgs);
 		SchemaClassDefinition scd = getSchemaClassDefinition(obj);
 		String str = findWithSchemaClass(scd, CONTEXT_SEPARATOR + iText.substring(1) + CONTEXT_SEPARATOR + customType);
 		if (str == null)
 			return "";
 		return fill(str, iArgs);
 	}
 
 	protected String find(String toFind) {
 		return find(toFind, getLocale());
 	}
 
 	protected abstract String find(String toFind, Locale locale);
 
 	protected abstract void put(String toFind, String value, Locale locale);
 
 	protected String fill(String toFill, Object[] iArgs) {
 		if (toFill != null && iArgs != null && iArgs.length > 0)
 			toFill = new ObjectVariableResolver(toFill).resolveVariables(iArgs);
 		return toFill;
 	}
 
 	public String aspectName() {
 		return ASPECT_NAME;
 	}
 }
