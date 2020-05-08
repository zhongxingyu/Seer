 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *    Florian Pirchner - FontDescriptor
  *******************************************************************************/
 package org.eclipse.riena.ui.swt.lnf.rienadefault;
 
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.Set;
 
import org.osgi.service.log.LogService;

import org.eclipse.equinox.log.Logger;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Resource;
 
import org.eclipse.riena.core.Log4r;
 import org.eclipse.riena.core.injector.Inject;
 import org.eclipse.riena.core.util.StringUtils;
 import org.eclipse.riena.internal.ui.swt.Activator;
 import org.eclipse.riena.ui.swt.lnf.ColorLnfResource;
 import org.eclipse.riena.ui.swt.lnf.FontDescriptor;
 import org.eclipse.riena.ui.swt.lnf.ILnfRenderer;
 import org.eclipse.riena.ui.swt.lnf.ILnfRendererDesc;
 import org.eclipse.riena.ui.swt.lnf.ILnfResource;
 import org.eclipse.riena.ui.swt.lnf.ILnfTheme;
 import org.eclipse.riena.ui.swt.lnf.LnfManager;
 
 /**
  * Default Look and Feel of Riena.
  */
 public class RienaDefaultLnf {
 
	private static final Logger LOGGER = Log4r.getLogger(Activator.getDefault(), RienaDefaultLnf.class);

 	private static final String LNF_RENDERER_EXTENSION_ID = "org.eclipse.riena.ui.swt.lnfrenderer"; //$NON-NLS-1$
 	private static final String DEFAULT_THEME_CLASSNAME = RienaDefaultTheme.class.getName();
 	private Map<String, ILnfResource> resourceTable = new Hashtable<String, ILnfResource>();
 	private Map<String, Object> settingTable = new Hashtable<String, Object>();
 	private Map<String, ILnfRenderer> rendererTable = new Hashtable<String, ILnfRenderer>();
 	private ILnfTheme theme;
 	private boolean initialized;
 
 	public Map<String, ILnfResource> getResourceTable() {
 		return resourceTable;
 	}
 
 	protected Map<String, ILnfRenderer> getRendererTable() {
 		return rendererTable;
 	}
 
 	protected Map<String, Object> getSettingTable() {
 		return settingTable;
 	}
 
 	/**
 	 * Initializes the Look and Feel. Fills the tables of resources and
 	 * renderers.
 	 */
 	public void initialize() {
 		if (!isInitialized()) {
 			uninitialize();
 			setInitialized(true);
 			initWidgetRendererDefaults();
 			initResourceDefaults();
 		}
 	}
 
 	/**
 	 * Uninitializes the Look and Feel. Disposes resources and clears the tables
 	 * of resources and renderers.
 	 */
 	public void uninitialize() {
 		disposeAllResources();
 		getResourceTable().clear();
 		getRendererTable().clear();
 		getSettingTable().clear();
 		setInitialized(false);
 	}
 
 	/**
 	 * Initializes the table with the renderers.<br>
 	 * Injects the renderers of the extension into the table of renderers.
 	 */
 	protected void initWidgetRendererDefaults() {
 
 		if (Activator.getDefault() != null) {
 			Inject.extension(LNF_RENDERER_EXTENSION_ID).into(this).andStart(Activator.getDefault().getContext());
 		}
 
 	}
 
 	/**
 	 * Puts the given renderers into the table of renderer.
 	 * 
 	 * @param rendererDescriptors
 	 *            - descriptors of renderer
 	 */
 	public void update(ILnfRendererDesc[] rendererDescriptors) {
 		if (rendererDescriptors == null) {
 			return;
 		}
 
 		for (ILnfRendererDesc rendererDescriptor : rendererDescriptors) {
 			String id = rendererDescriptor.getLnfId();
 			if (StringUtils.isEmpty(id) || id.equals(getLnfId())) {
 				if (StringUtils.isEmpty(id)) {
 					if (getRendererTable().get(rendererDescriptor.getLnfKey()) != null) {
 						continue;
 					}
 				}
 				ILnfRenderer renderer = rendererDescriptor.createRenderer();
 				getRendererTable().put(rendererDescriptor.getLnfKey(), renderer);
 			}
 		}
 
 	}
 
 	/**
 	 * Initializes the table with the resources.
 	 */
 	protected void initResourceDefaults() {
 		initColorDefaults();
 		initFontDefaults();
 		initImageDefaults();
 		initSettingsDefaults();
 	}
 
 	/**
 	 * Puts the colors to resource table.
 	 */
 	protected void initColorDefaults() {
 
 		getResourceTable().put("black", new ColorLnfResource(0, 0, 0)); //$NON-NLS-1$
 		getResourceTable().put("white", new ColorLnfResource(255, 255, 255)); //$NON-NLS-1$
 
 		if (getTheme() != null) {
 			getTheme().addCustomColors(getResourceTable());
 		}
 	}
 
 	/**
 	 * Puts the fonts to resource table.
 	 */
 	protected void initFontDefaults() {
 		if (getTheme() != null) {
 			getTheme().addCustomFonts(getResourceTable());
 		}
 	}
 
 	/**
 	 * Puts the images to resource table.
 	 */
 	protected void initImageDefaults() {
 		if (getTheme() != null) {
 			getTheme().addCustomImages(getResourceTable());
 		}
 	}
 
 	/**
 	 * Puts settings to the table.
 	 */
 	protected void initSettingsDefaults() {
 		if (getTheme() != null) {
 			getTheme().addCustomSettings(getSettingTable());
 		}
 	}
 
 	/**
 	 * Disposes all resources of the Look and Feel.
 	 */
 	private void disposeAllResources() {
 		Set<String> keys = getResourceTable().keySet();
 		for (String key : keys) {
 			ILnfResource value = getResourceTable().get(key);
 			if (value != null) {
 				value.dispose();
 			}
 		}
 	}
 
 	/**
 	 * Returns the resource for the given key.
 	 * 
 	 * @param key
 	 *            - key whose associated resource is to be returned.
 	 * @return the resource to which this map maps the specified key, or
 	 *         <code>null</code> if the map contains no mapping for this key.
 	 */
 	public Resource getResource(String key) {
 		ILnfResource value = getResourceTable().get(key);
 		if (value != null) {
 			return value.getResource();
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * Returns the color for the given key.
 	 * 
 	 * @param key
 	 *            - key whose associated color is to be returned.
 	 * @return the color to which this map maps the specified key, or
 	 *         <code>null</code> if the map contains no mapping for this key.
 	 */
 	public Color getColor(String key) {
 		Resource value = getResource(key);
 		if (value instanceof Color) {
 			return (Color) value;
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * Returns the font for the given key.
 	 * 
 	 * @param key
 	 *            - key whose associated font is to be returned.
 	 * @return the font to which this map maps the specified key, or
 	 *         <code>null</code> if the map contains no mapping for this key.
 	 */
 	public Font getFont(String key) {
 		Resource value = getResource(key);
 		if (value instanceof Font) {
 			return (Font) value;
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * Returns the font for the given key. The passed properties height and
 	 * style will be applied to the font. <br>
 	 * 
 	 * @param key
 	 *            - key whose associated font is to be returned.
 	 * @param height
 	 *            - the font height to use. If it is < 0, the
 	 *            <code>LnfKeyConstants.FONTDESCRIPTOR_DEFAULT_HEIGHT</code>
 	 *            will be used. See also {@link FontData#setHeight(int)}.
 	 * @param style
 	 *            - the font style to use. See also
 	 *            {@link FontData#setStyle(int)}.
 	 * 
 	 * @return the font to which this map maps the specified key with differing
 	 *         height and style, or <code>null</code> if the map contains no
 	 *         mapping for this lnfKeyConstants key.
 	 * @since 1.2
 	 */
 	public Font getFont(String key, int height, int style) {
 		FontDescriptor fontDescriptor = new FontDescriptor(key, height, style, this);
 		return fontDescriptor.getFont();
 	}
 
 	/**
 	 * Returns the image for the given key.
 	 * 
 	 * @param key
 	 *            - key whose associated image is to be returned.
 	 * @return the image to which this map maps the specified key, or
 	 *         <code>null</code> if the map contains no mapping for this key.
 	 */
 	public Image getImage(String key) {
 		Resource value = getResource(key);
 		if (value instanceof Image) {
 			return (Image) value;
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * Returns the renderer for the given key.
 	 * 
 	 * @param key
 	 *            - key whose associated renderer is to be returned.
 	 * @return the renderer to which this renderer maps the specified key, or
 	 *         <code>null</code> if the map contains no mapping for this key.
 	 */
 	public ILnfRenderer getRenderer(String key) {
 		return getRendererTable().get(key);
 	}
 
 	/**
 	 * Returns the setting for the given key
 	 * 
 	 * @param key
 	 *            - key whose associated setting is to be returned.
 	 * @return the setting to which this setting maps the specified key, or
 	 *         <code>null</code> if the map contains no mapping for this key.
 	 */
 	public Object getSetting(String key) {
 		return getSettingTable().get(key);
 	}
 
 	/**
 	 * Returns the integer value of the setting for the given key
 	 * 
 	 * @param key
 	 *            - key whose associated setting is to be returned.
 	 * @return the setting to which this setting maps the specified key, or
 	 *         <code>null</code> if the map contains no mapping for this key.
 	 */
 	public Integer getIntegerSetting(String key) {
 		Object value = getSetting(key);
 		if (value instanceof Integer) {
 			return (Integer) value;
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * Returns the integer value of the setting for the given key. If no value
 	 * is set, the given default value is returned.
 	 * 
 	 * @param key
 	 *            - key whose associated setting is to be returned.
 	 * @param defaultValue
 	 *            - value to return, if no value is set
 	 * @return the setting to which this setting maps the specified key, or the
 	 *         default value if the map contains no mapping for this key.
 	 * @since 1.2
 	 */
 	public Integer getIntegerSetting(String key, Integer defaultValue) {
 		Integer value = getIntegerSetting(key);
 		if (value == null) {
 			value = defaultValue;
 		}
 		return value;
 	}
 
 	/**
 	 * Returns the boolean value of the setting for the given key
 	 * 
 	 * @param key
 	 *            - key whose associated setting is to be returned.
 	 * @return the setting to which this setting maps the specified key, or
 	 *         <code>false</code> if the map contains no mapping for this key.
 	 */
 	public Boolean getBooleanSetting(String key) {
 		return getBooleanSetting(key, false);
 	}
 
 	/**
 	 * Returns the boolean value of the setting for the given key
 	 * 
 	 * @param key
 	 *            - key whose associated setting is to be returned.
 	 * @return the setting to which this setting maps the specified key, or
 	 *         <code>false</code> if the map contains no mapping for this key.
 	 * @since 1.2
 	 */
 	public Boolean getBooleanSetting(String key, boolean defalutValue) {
 		Object value = getSetting(key);
 		if (value instanceof Boolean) {
 			return (Boolean) value;
 		} else {
 			return defalutValue;
 		}
 	}
 
 	/**
 	 * Returns the String value of the setting for the given key
 	 * 
 	 * @param key
 	 *            - key whose associated setting is to be returned.
 	 * @return the setting to which this setting maps the specified key, or
 	 *         <code>null</code> if the map contains no mapping for this key.
 	 */
 	public String getStringSetting(String key) {
 		Object value = getSetting(key);
 		if (value instanceof String) {
 			return (String) value;
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * Loads the theme specified by the given class name.
 	 * 
 	 * @param themeClassName
 	 *            - a string specifying the name of the class that implements
 	 *            the theme
 	 * @return theme
 	 * @throws ClassNotFoundException
 	 * @throws InstantiationException
 	 * @throws IllegalAccessException
 	 */
	private static ILnfTheme createTheme(String themeClassName) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		ClassLoader classLoader = LnfManager.class.getClassLoader();
		Class<?> themeClass = classLoader.loadClass(themeClassName);
		return (ILnfTheme) themeClass.newInstance();
 	}
 
 	/**
 	 * Returns the current theme of the Look and Feel.
 	 * 
 	 * @return theme
 	 */
 	public ILnfTheme getTheme() {
 		if (theme == null) {
			try {
				theme = createTheme(DEFAULT_THEME_CLASSNAME);
			} catch (Exception e) {
				String message = "can't load " + DEFAULT_THEME_CLASSNAME; //$NON-NLS-1$
				LOGGER.log(LogService.LOG_ERROR, message, e);
				throw new Error(message);
			}
 		}
 		if (!isInitialized()) {
 			initialize();
 		}
 		return theme;
 	}
 
 	/**
 	 * Sets the theme to be used by the Look and Feel.
 	 * 
 	 * @param newTheme
 	 *            - the theme to be used
 	 */
 	public void setTheme(ILnfTheme newTheme) {
 		if (theme != newTheme) {
 			theme = newTheme;
 			setInitialized(false);
 		}
 	}
 
 	/**
 	 * @return the initialize
 	 */
 	private boolean isInitialized() {
 		return initialized;
 	}
 
 	/**
 	 * @param initialize
 	 *            the initialize to set
 	 */
 	private void setInitialized(boolean initialized) {
 		this.initialized = initialized;
 	}
 
 	/**
 	 * The ID of this Look and Feel.<br>
 	 * The ID of the default LnF is empty.
 	 * 
 	 * @return look'n'feel ID
 	 */
 	protected String getLnfId() {
 		return ""; //$NON-NLS-1$
 	}
 
 }
