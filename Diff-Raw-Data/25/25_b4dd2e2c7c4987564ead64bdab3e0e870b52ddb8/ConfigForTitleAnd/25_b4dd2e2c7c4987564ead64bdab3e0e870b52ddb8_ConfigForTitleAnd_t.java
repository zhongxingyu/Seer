 package org.softwareFm.swtBasics.text;
 
 import org.eclipse.jface.resource.ImageRegistry;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.widgets.Display;
 import org.softwareFm.swtBasics.images.Images;
 import org.softwareFm.swtBasics.images.Resources;
 import org.softwareFm.utilities.resources.IResourceGetter;
 
 public class ConfigForTitleAnd {
 
 	public static ConfigForTitleAnd createForBasics(Display display) {
 		return ConfigForTitleAnd.create(display, Resources.resourceGetterWithBasics(), Images.withBasics(display));
 	}
 
 	public static ConfigForTitleAnd create(Display display, IResourceGetter resourceGetter, ImageRegistry imageRegistry) {
		return new ConfigForTitleAnd(resourceGetter, imageRegistry, display.getSystemColor(SWT.COLOR_GRAY), display.getSystemColor(SWT.COLOR_WHITE));
 	}
 
 	public final IResourceGetter resourceGetter;
 	public final ImageRegistry imageRegistry;
 
 	public final Color normalBackground;
 	public final Color editingBackground;
 	public final int style;
 
 	public final int titleWidth;
 	public final int buttonsWidth;
 	public final int buttonSpacer;
 	public final int titleHeight;
 	public final int buttonHeight;
 
 	public ConfigForTitleAnd(IResourceGetter resourceGetter, ImageRegistry imageRegistry, Color normalBackground, Color editingBackground) {
 		this(resourceGetter, imageRegistry, normalBackground, editingBackground, SWT.NULL, 100, 100, 21, 16, 23);
 	}
 
 	public ConfigForTitleAnd(IResourceGetter resourceGetter, ImageRegistry imageRegistry, Color normalBackground, Color editingBackground, int style, int titleWidth, int buttonsWidth, int buttonSpacer, int titleHeight, int buttonHeight) {
 		this.resourceGetter = resourceGetter;
 		this.imageRegistry = imageRegistry;
 		this.normalBackground = normalBackground;
 		this.editingBackground = editingBackground;
 		this.style = style;
 		this.titleWidth = titleWidth;
 		this.buttonsWidth = buttonsWidth;
 		this.buttonSpacer = buttonSpacer;
 		this.titleHeight = titleHeight;
 		this.buttonHeight = buttonHeight;
 	}
 
 	public ConfigForTitleAnd withTitleWidth(int titleWidth) {
 		return new ConfigForTitleAnd(resourceGetter, imageRegistry, normalBackground, editingBackground, style, titleWidth, buttonsWidth, buttonSpacer, titleHeight, buttonHeight);
 	}
 
 	public ConfigForTitleAnd withButtonsWidth(int buttonsWidth) {
 		return new ConfigForTitleAnd(resourceGetter, imageRegistry, normalBackground, editingBackground, style, titleWidth, buttonsWidth, buttonSpacer, titleHeight, buttonHeight);
 	}
 
 	public ConfigForTitleAnd withButtonSpacer(int buttonSpacer) {
 		return new ConfigForTitleAnd(resourceGetter, imageRegistry, normalBackground, editingBackground, style, titleWidth, buttonsWidth, buttonSpacer, titleHeight, buttonHeight);
 	}
 
 	public ConfigForTitleAnd withTitleHeight(int titleHeight) {
 		return new ConfigForTitleAnd(resourceGetter, imageRegistry, normalBackground, editingBackground, style, titleWidth, buttonsWidth, buttonSpacer, titleHeight, buttonHeight);
 	}
 
 	public ConfigForTitleAnd withButtonHeight(int buttonHeight) {
 		return new ConfigForTitleAnd(resourceGetter, imageRegistry, normalBackground, editingBackground, style, titleWidth, buttonsWidth, buttonSpacer, titleHeight, buttonHeight);
 	}
 
 	public ConfigForTitleAnd withNormalBackground(Color normalBackground) {
 		return new ConfigForTitleAnd(resourceGetter, imageRegistry, normalBackground, editingBackground, style, titleWidth, buttonsWidth, buttonSpacer, titleHeight, buttonHeight);
 	}
 
 	public ConfigForTitleAnd withEditingBackground(Color editingBackground) {
 		return new ConfigForTitleAnd(resourceGetter, imageRegistry, normalBackground, editingBackground, style, titleWidth, buttonsWidth, buttonSpacer, titleHeight, buttonHeight);
 	}
 
 	public static ConfigForTitleAnd createForDialogs(Display display, IResourceGetter resourceGetter, ImageRegistry imageRegistry) {
 		return create(display, resourceGetter, imageRegistry);
 	}
 
 }
