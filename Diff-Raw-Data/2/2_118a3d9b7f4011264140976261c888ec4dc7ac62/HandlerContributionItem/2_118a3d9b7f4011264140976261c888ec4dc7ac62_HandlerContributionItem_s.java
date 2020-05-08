 /*******************************************************************************
  * Copyright (c) 2006-2013 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *     Stephan Wahlbrink - modified to use handlers
  *******************************************************************************/
 
 package de.walware.ecommons.ui.actions;
 
 import java.util.Collections;
 import java.util.Map;
 
 import org.eclipse.core.commands.Command;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.commands.HandlerEvent;
 import org.eclipse.core.commands.IHandler2;
 import org.eclipse.core.commands.IHandlerListener;
 import org.eclipse.core.commands.ParameterizedCommand;
 import org.eclipse.core.commands.common.NotDefinedException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.action.ContributionItem;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.bindings.BindingManagerEvent;
 import org.eclipse.jface.bindings.IBindingManagerListener;
 import org.eclipse.jface.bindings.TriggerSequence;
 import org.eclipse.jface.resource.DeviceResourceException;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.jface.resource.LocalResourceManager;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.ToolItem;
 import org.eclipse.swt.widgets.Widget;
 import org.eclipse.ui.commands.ICommandImageService;
 import org.eclipse.ui.commands.ICommandService;
 import org.eclipse.ui.commands.IElementReference;
 import org.eclipse.ui.commands.IElementUpdater;
 import org.eclipse.ui.handlers.IHandlerService;
 import org.eclipse.ui.help.IWorkbenchHelpSystem;
 import org.eclipse.ui.internal.services.IWorkbenchLocationService;
 import org.eclipse.ui.keys.IBindingService;
 import org.eclipse.ui.menus.CommandContributionItemParameter;
 import org.eclipse.ui.menus.IMenuService;
 import org.eclipse.ui.menus.UIElement;
 import org.eclipse.ui.services.IServiceLocator;
 import org.eclipse.ui.statushandlers.StatusManager;
 
 import de.walware.ecommons.ui.SharedUIResources;
 import de.walware.ecommons.ui.util.MenuUtil;
 
 
 /**
  * A contribution item which delegates to a handler of a command (not using all service).
  * It was created as workaround for toolbars in views (wrong enablement when lost focus).
  * <p>
  * It currently supports placement in menus and toolbars.
  * </p>
  * <p>
  * This class may be instantiated; it is not intended to be subclassed.
  * </p>
  */
 public class HandlerContributionItem extends ContributionItem {
 	
 	
 	public static final String NO_COMMAND_ID = "NO_COMMAND"; //$NON-NLS-1$
 	
 	
 	/**
 	 * A push button tool item or menu item.
 	 */
 	public static final int STYLE_PUSH = SWT.PUSH;
 	
 	/**
 	 * A checked tool item or menu item.
 	 */
 	public static final int STYLE_CHECK = SWT.CHECK;
 	
 	/**
 	 * A radio-button style menu item.
 	 */
 	public static final int STYLE_RADIO = SWT.RADIO;
 	
 	/**
 	 * A ToolBar pulldown item.
 	 */
 	public static final int STYLE_PULLDOWN = SWT.DROP_DOWN;
 	
 	/**
 	 * Mode bit: Show text on tool items or buttons, even if an image is
 	 * present. If this mode bit is not set, text is only shown on tool items if
 	 * there is no image present.
 	 * 
 	 * @since 3.4
 	 */
 	public static final int MODE_FORCE_TEXT = 1;
 	
 	private static final int MODE_NO_COMMAND = 1 << 28;
 	
 	private static final int MODE_CALLBACK_CALL = 1 << 24;
 	private static final int MODE_CALLBACK_EXPL = 1 << 25;
 	
 	
 	private LocalResourceManager localResourceManager;
 	
 	private Listener menuItemListener;
 	
 	private Widget widget;
 	
 	private IMenuService menuService;
 	
 	private ICommandService commandService;
 	private IHandlerService handlerService;
 	private IBindingService bindingService;
 	
 	protected final Display display;
 	
 	private ParameterizedCommand command;
 	private IHandler2 commandHandler;
 	
 	private ImageDescriptor icon;
 	
 	private String label;
 	
 	private String tooltip;
 	
 	private ImageDescriptor disabledIcon;
 	
 	private ImageDescriptor hoverIcon;
 	
 	private String mnemonic;
 	
 	private IElementReference elementRef;
 	
 	private boolean checkedState;
 	
 	private int style;
 	
 	private IHandlerListener commandListener;
 	
 	private String dropDownMenuOverride;
 	
 	private IWorkbenchHelpSystem workbenchHelpSystem;
 	
 	private String helpContextId;
 	
 	private int mode = 0;
 	
 	private boolean visibleEnabled;
 	
 	// items contributed
 	private String contributedLabel;
 	
 	private String contributedTooltip;
 	
 	private ImageDescriptor contributedIcon;
 	
 	private ImageDescriptor contributedDisabledIcon;
 	
 	private ImageDescriptor contributedHoverIcon;
 	
 	private IServiceLocator serviceLocator;
 	
 	private final UIElement callback;
 	
 	private final IBindingManagerListener bindingManagerListener = new IBindingManagerListener() {
 		@Override
 		public void bindingManagerChanged(final BindingManagerEvent event) {
 			if (event.isActiveBindingsChanged()
 					&& event.isActiveBindingsChangedFor(getCommand())) {
 				update();
 			}
 		}
 	};
 	
 	
 	/**
 	 * Create a CommandContributionItem to place in a ContributionManager.
 	 * 
 	 * @param contributionParameters
 	 *            paramters necessary to render this contribution item.
 	 */
 	public HandlerContributionItem(
 			final CommandContributionItemParameter contributionParameters, final IHandler2 handler) {
 		super(contributionParameters.id);
 		
 		this.contributedIcon = this.icon = contributionParameters.icon;
 		this.contributedTooltip = this.tooltip = contributionParameters.tooltip;
 		this.contributedDisabledIcon = this.disabledIcon = contributionParameters.disabledIcon;
 		this.contributedHoverIcon = this.hoverIcon = contributionParameters.hoverIcon;
 		this.contributedLabel = this.label = contributionParameters.label;
 		this.mnemonic = contributionParameters.mnemonic;
 		this.style = contributionParameters.style;
 		this.helpContextId = contributionParameters.helpContextId;
 		this.visibleEnabled = contributionParameters.visibleEnabled;
 		this.serviceLocator = contributionParameters.serviceLocator;
 		
 		try {
 			menuService = (IMenuService) serviceLocator
 					.getService(IMenuService.class);
 		} catch (NullPointerException e) {}
 		commandService = (ICommandService) serviceLocator
 				.getService(ICommandService.class);
 		handlerService = (IHandlerService) serviceLocator
 				.getService(IHandlerService.class);
 		bindingService = (IBindingService) serviceLocator
 				.getService(IBindingService.class);
 		final IWorkbenchLocationService workbenchLocationService = (IWorkbenchLocationService) serviceLocator
 				.getService(IWorkbenchLocationService.class);
 		display = workbenchLocationService.getWorkbench().getDisplay();
 		
 		createCommand(contributionParameters.commandId,
 				contributionParameters.parameters);
 		commandHandler = handler;
 		
 		callback = (handler instanceof IElementUpdater) ? new UIElement(
 				contributionParameters.serviceLocator) {
 			
 			private boolean beginCallback() {
 				if ((mode & MODE_NO_COMMAND) != 0) {
 					mode |= MODE_CALLBACK_CALL;
 					return true;
 				}
 				if (command.getCommand().getHandler() == commandHandler) {
 					mode |= MODE_CALLBACK_CALL;
 					return true;
 				}
 				return false;
 			}
 			
 			private void endCallback() {
 				mode &= ~MODE_CALLBACK_CALL;
 			}
 			
 			@Override
 			public void setChecked(final boolean checked) {
 				if (beginCallback()) {
 					try {
 						HandlerContributionItem.this.setChecked(checked);
 					}
 					finally {
 						endCallback();
 					}
 				}
 			}
 			
 			@Override
 			public void setDisabledIcon(final ImageDescriptor desc) {
 				if (beginCallback()) {
 					try {
 						HandlerContributionItem.this.setDisabledIcon(desc);
 					}
 					finally {
 						endCallback();
 					}
 				}
 			}
 			
 			@Override
 			public void setHoverIcon(final ImageDescriptor desc) {
 				if (beginCallback()) {
 					try {
 						HandlerContributionItem.this.setHoverIcon(desc);
 					}
 					finally {
 						endCallback();
 					}
 				}
 			}
 			
 			@Override
 			public void setIcon(final ImageDescriptor desc) {
 				if (beginCallback()) {
 					try {
 						HandlerContributionItem.this.setIcon(desc);
 					}
 					finally {
 						endCallback();
 					}
 				}
 			}
 			
 			@Override
 			public void setText(final String text) {
 				if (beginCallback()) {
 					try {
 						HandlerContributionItem.this.setText(text);
 					}
 					finally {
 						endCallback();
 					}
 				}
 			}
 			
 			@Override
 			public void setTooltip(final String text) {
 				if (beginCallback()) {
 					try {
 						HandlerContributionItem.this.setTooltip(text);
 					}
 					finally {
 						endCallback();
 					}
 				}
 			}
 			
 			@Override
 			public void setDropDownId(final String id) {
 				if (beginCallback()) {
 					try {
 						HandlerContributionItem.this.dropDownMenuOverride = id;
 					}
 					finally {
 						endCallback();
 					}
 				}
 			}
 		} : null;
		establishReferences();
 		if (command != null) {
 			setImages(contributionParameters.serviceLocator,
 					contributionParameters.iconStyle);
 		}
 	}
 	
 	
 	protected IServiceLocator getServiceLocator() {
 		return this.serviceLocator;
 	}
 	
 	private void setImages(final IServiceLocator locator, final String iconStyle) {
 		if (icon == null && command != null) {
 			final ICommandImageService service = (ICommandImageService) locator
 					.getService(ICommandImageService.class);
 			icon = service.getImageDescriptor(command.getId(),
 					ICommandImageService.TYPE_DEFAULT, iconStyle);
 			disabledIcon = service.getImageDescriptor(command.getId(),
 					ICommandImageService.TYPE_DISABLED, iconStyle);
 			hoverIcon = service.getImageDescriptor(command.getId(),
 					ICommandImageService.TYPE_HOVER, iconStyle);
 			
 			if (contributedIcon == null) {
 				contributedIcon = icon;
 			}
 			if (contributedDisabledIcon == null) {
 				contributedDisabledIcon = disabledIcon;
 			}
 			if (contributedHoverIcon == null) {
 				contributedHoverIcon = hoverIcon;
 			}
 		}
 	}
 	
 	private IHandlerListener getHandlerListener() {
 		if (commandListener == null) {
 			commandListener = new IHandlerListener() {
 				@Override
 				public void handlerChanged(final HandlerEvent commandEvent) {
 					if (commandEvent.isHandledChanged()
 							|| commandEvent.isEnabledChanged()
 							) {
 						updateCommandProperties(commandEvent);
 					}
 				}
 			};
 		}
 		return commandListener;
 	}
 	
 	private void updateCommandProperties(final HandlerEvent commandEvent) {
 		if (commandEvent.isHandledChanged()) {
 			dropDownMenuOverride = null;
 		}
 		final Runnable update = new Runnable() {
 			@Override
 			public void run() {
 				update(null);
 			}
 		};
 		if (display.getThread() == Thread.currentThread()) {
 			update.run();
 		} else {
 			display.asyncExec(update);
 		}
 	}
 	
 	/**
 	 * Returns the ParameterizedCommand for this contribution.
 	 * <p>
 	 * <strong>NOTE:</strong> The returned object should be treated
 	 * as 'read-only', do <b>not</b> execute this instance or attempt
 	 * to modify its state.
 	 * </p>
 	 * @return The parameterized command for this contribution. May be
 	 *         <code>null</code>.
 	 */
 	public ParameterizedCommand getCommand() {
 		return command;
 	}
 	
 	void createCommand(final String commandId, final Map parameters) {
 		if (commandId == null) {
 			StatusManager.getManager().handle(new Status(IStatus.ERROR, SharedUIResources.PLUGIN_ID,
 					"Unable to create menu item \"" + getId() + "\", no command id")); //$NON-NLS-1$ //$NON-NLS-2$
 			return;
 		}
 		if (commandId == NO_COMMAND_ID) {
 			mode |= MODE_NO_COMMAND;
 			return;
 		}
 		final Command cmd = commandService.getCommand(commandId);
 		if (!cmd.isDefined()) {
 			StatusManager.getManager().handle(new Status(IStatus.ERROR, SharedUIResources.PLUGIN_ID,
 					"Unable to create menu item \"" + getId() + "\", command \"" + commandId + "\" not defined")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 			return;
 		}
 		command = ParameterizedCommand.generateCommand(cmd, parameters);
 	}
 	
 	/**
 	 * Provide info on the rendering data contained in this item.
 	 * 
 	 * @return a {@link CommandContributionItemParameter}. Valid fields are
 	 *         serviceLocator, id, style, icon, disabledIcon, hoverIcon, label,
 	 *         helpContextId, mnemonic, tooltip. The Object will never be
 	 *         <code>null</code>, although any of the fields may be
 	 *         <code>null</code>.
 	 * @since 3.7
 	 */
 	public CommandContributionItemParameter getData() {
 		CommandContributionItemParameter data = new CommandContributionItemParameter(
 				serviceLocator, getId(), null, style);
 		data.icon = contributedIcon;
 		data.disabledIcon = contributedDisabledIcon;
 		data.hoverIcon = contributedHoverIcon;
 		data.label = contributedLabel;
 		data.tooltip = contributedTooltip;
 		data.helpContextId = helpContextId;
 		data.mnemonic = mnemonic;
 		return data;
 	}
 	
 	@Override
 	public void fill(final Menu parent, final int index) {
 		if (command == null && (mode & MODE_NO_COMMAND) == 0) {
 			return;
 		}
 		if (widget != null || parent == null) {
 			return;
 		}
 		
 		// Menus don't support the pulldown style
 		int tmpStyle = style;
 		if (tmpStyle == STYLE_PULLDOWN) {
 			tmpStyle = STYLE_PUSH;
 		}
 		
 		MenuItem item = null;
 		if (index >= 0) {
 			item = new MenuItem(parent, tmpStyle, index);
 		} else {
 			item = new MenuItem(parent, tmpStyle);
 		}
 		item.setData(this);
 		if (workbenchHelpSystem != null) {
 			workbenchHelpSystem.setHelp(item, helpContextId);
 		}
 		item.addListener(SWT.Dispose, getItemListener());
 		item.addListener(SWT.Selection, getItemListener());
 		widget = item;
 		
 		update(null);
 		updateIcons();
 		
 		establishReferences();
 	}
 	
 	@Override
 	public void fill(final ToolBar parent, final int index) {
 		if (command == null && (mode & MODE_NO_COMMAND) == 0) {
 			return;
 		}
 		if (widget != null || parent == null) {
 			return;
 		}
 		
 		ToolItem item = null;
 		if (index >= 0) {
 			item = new ToolItem(parent, style, index);
 		} else {
 			item = new ToolItem(parent, style);
 		}
 		
 		item.setData(this);
 		
 		item.addListener(SWT.Selection, getItemListener());
 		item.addListener(SWT.Dispose, getItemListener());
 		widget = item;
 		
 		update(null);
 		updateIcons();
 		
 		establishReferences();
 	}
 	
 	@Override
 	public void fill(final Composite parent) {
 		if (command == null && (mode & MODE_NO_COMMAND) == 0) {
 			return;
 		}
 		if (widget != null || parent == null) {
 			return;
 		}
 		
 		// Buttons don't support the pulldown style
 		int tmpStyle = style;
 		if (tmpStyle == STYLE_PULLDOWN) {
 			tmpStyle = STYLE_PUSH;
 		}
 		
 		final Button item = new Button(parent, tmpStyle);
 		item.setData(this);
 		if (workbenchHelpSystem != null) {
 			workbenchHelpSystem.setHelp(item, helpContextId);
 		}
 		item.addListener(SWT.Dispose, getItemListener());
 		item.addListener(SWT.Selection, getItemListener());
 		widget = item;
 		
 		update(null);
 		updateIcons();
 		
 		establishReferences();
 	}
 	
 	@Override
 	public void update() {
 		update(null);
 	}
 	
 	@Override
 	public void update(final String id) {
 		if (widget != null) {
 			if ((mode & MODE_CALLBACK_EXPL) == MODE_CALLBACK_EXPL) {
 				return;
 			}
 			if ((mode & (MODE_NO_COMMAND | MODE_CALLBACK_CALL)) == (MODE_NO_COMMAND)
 					&& callback != null) {
 				mode |= MODE_CALLBACK_EXPL;
 				try {
 					label = contributedLabel;
 					tooltip = contributedTooltip;
 					icon = contributedIcon;
 					disabledIcon = contributedDisabledIcon;
 					hoverIcon = contributedHoverIcon;
 					((IElementUpdater) commandHandler).updateElement(callback, Collections.EMPTY_MAP);
 				}
 				finally {
 					mode &= ~MODE_CALLBACK_EXPL;
 				}
 			}
 			
 			if (widget instanceof MenuItem) {
 				updateMenuItem();
 			} else if (widget instanceof ToolItem) {
 				updateToolItem();
 			} else if (widget instanceof Button) {
 				updateButton();
 			}
 		}
 	}
 	
 	private void updateMenuItem() {
 		final MenuItem item = (MenuItem) widget;
 		
 		final boolean shouldBeEnabled = isEnabled();
 		
 		// disabled command + visibility follows enablement == disposed
 		if (item.isDisposed()) {
 			return;
 		}
 		
 		String text = label;
 		if (text == null) {
 			if (command != null) {
 				try {
 					text = command.getCommand().getName();
 				} catch (final NotDefinedException e) {
 					StatusManager.getManager().handle(new Status(IStatus.ERROR, SharedUIResources.PLUGIN_ID,
 							"Update item failed " + getId(), e)); //$NON-NLS-1$
 				}
 			}
 		}
 		text = updateMnemonic(text);
 		
 		String keyBindingText = null;
 		if (command != null) {
 			final TriggerSequence binding = bindingService.getBestActiveBindingFor(command);
 			if (binding != null) {
 				keyBindingText = binding.format();
 			}
 		}
 		if (text != null) {
 			if (keyBindingText == null) {
 				item.setText(text);
 			} else {
 				item.setText(text + '\t' + keyBindingText);
 			}
 		}
 		
 		if (item.getSelection() != checkedState) {
 			item.setSelection(checkedState);
 		}
 		
 		if (item.getEnabled() != shouldBeEnabled) {
 			item.setEnabled(shouldBeEnabled);
 		}
 	}
 	
 	private void updateToolItem() {
 		final ToolItem item = (ToolItem) widget;
 		
 		final boolean shouldBeEnabled = isEnabled();
 		
 		// disabled command + visibility follows enablement == disposed
 		if (item.isDisposed()) {
 			return;
 		}
 		
 		String text = label;
 		if (text == null) {
 			if (command != null) {
 				try {
 					text = command.getCommand().getName();
 				} catch (final NotDefinedException e) {
 					StatusManager.getManager().handle(new Status(IStatus.ERROR, SharedUIResources.PLUGIN_ID,
 							"Update item failed " + getId(), e)); //$NON-NLS-1$
 				}
 			}
 		}
 		
 		if ((icon == null || (mode & MODE_FORCE_TEXT) == MODE_FORCE_TEXT)
 				&& text != null) {
 			item.setText(text);
 		}
 		
 		final String toolTipText = getToolTipText(text);
 		item.setToolTipText(toolTipText);
 		
 		if (item.getSelection() != checkedState) {
 			item.setSelection(checkedState);
 		}
 		
 		if (item.getEnabled() != shouldBeEnabled) {
 			item.setEnabled(shouldBeEnabled);
 		}
 	}
 	
 	private void updateButton() {
 		final Button item = (Button) widget;
 		
 		final boolean shouldBeEnabled = isEnabled();
 		
 		// disabled command + visibility follows enablement == disposed
 		if (item.isDisposed()) {
 			return;
 		}
 		
 		String text = label;
 		if (text == null) {
 			if (command != null) {
 				try {
 					text = command.getCommand().getName();
 				} catch (final NotDefinedException e) {
 					StatusManager.getManager().handle(new Status(IStatus.ERROR, SharedUIResources.PLUGIN_ID,
 							"Update item failed " + getId(), e)); //$NON-NLS-1$
 				}
 			}
 		}
 		
 		if (text != null) {
 			item.setText(text);
 		}
 		
 		final String toolTipText = getToolTipText(text);
 		item.setToolTipText(toolTipText);
 		
 		if (item.getSelection() != checkedState) {
 			item.setSelection(checkedState);
 		}
 		
 		if (item.getEnabled() != shouldBeEnabled) {
 			item.setEnabled(shouldBeEnabled);
 		}
 	}
 	
 	private String getToolTipText(final String text) {
 		String tooltipText = tooltip;
 		if (tooltip == null) {
 			if (text != null) {
 				tooltipText = text;
 			} else {
 				tooltipText = ""; //$NON-NLS-1$
 			}
 		}
 		
 		if (command != null) {
 			final TriggerSequence activeBinding = bindingService.getBestActiveBindingFor(command);
 			if (activeBinding != null && !activeBinding.isEmpty()) {
 				final String acceleratorText = activeBinding.format();
 				if (acceleratorText != null
 						&& acceleratorText.length() != 0) {
 					tooltipText = NLS.bind("{0} ({1})", tooltipText, acceleratorText); //$NON-NLS-1$
 				}
 			}
 		}
 		
 		return tooltipText;
 	}
 	
 	private String updateMnemonic(final String s) {
 		if (mnemonic == null || s == null) {
 			return s;
 		}
 		final int idx = s.indexOf(mnemonic);
 		if (idx == -1) {
 			return s;
 		}
 		
 		return s.substring(0, idx) + '&' + s.substring(idx);
 	}
 	
 	private void handleWidgetDispose(final Event event) {
 		if (event.widget == widget) {
 			disconnectReferences();
 			widget.removeListener(SWT.Selection, getItemListener());
 			widget.removeListener(SWT.Dispose, getItemListener());
 			widget = null;
 			disposeOldImages();
 		}
 	}
 	
 	private void establishReferences() {
 		if (command != null || (mode & MODE_NO_COMMAND) != 0) {
 			commandHandler.addHandlerListener(getHandlerListener());
 		}
 		if (command != null) {
 			if (callback != null) {
 				try {
 					elementRef = commandService.registerElementForCommand(command, callback);
 				}
 				catch (final NotDefinedException e) {
 					StatusManager.getManager().handle(new Status(IStatus.ERROR, SharedUIResources.PLUGIN_ID,
 							"Unable to register menu item \"" + getId() + "\", command \"" + command.getId() + "\" not defined")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 				}
 			}
 		}
 		bindingService.addBindingManagerListener(bindingManagerListener);
 	}
 	
 	private void disconnectReferences() {
 		if (elementRef != null) {
 			commandService.unregisterElement(elementRef);
 			elementRef = null;
 		}
 		if (commandListener != null) {
 			commandHandler.removeHandlerListener(commandListener);
 			commandListener = null;
 		}
 		if (bindingService != null) {
 			bindingService.removeBindingManagerListener(bindingManagerListener);
 		}
 	}
 	
 	@Override
 	public void dispose() {
 		if (widget != null) {
 			widget.dispose();
 			widget = null;
 		}
 		
 		disconnectReferences();
 		
 		command = null;
 		commandHandler = null;
 		commandService = null;
 		bindingService = null;
 		menuService = null;
 		handlerService = null;
 		disposeOldImages();
 		super.dispose();
 	}
 	
 	private void disposeOldImages() {
 		if (localResourceManager != null) {
 			localResourceManager.dispose();
 			localResourceManager = null;
 		}
 	}
 	
 	private Listener getItemListener() {
 		if (menuItemListener == null) {
 			menuItemListener = new Listener() {
 				@Override
 				public void handleEvent(final Event event) {
 					switch (event.type) {
 					case SWT.Dispose:
 						handleWidgetDispose(event);
 						break;
 					case SWT.Selection:
 						if (event.widget != null) {
 							handleWidgetSelection(event);
 						}
 						break;
 					}
 				}
 			};
 		}
 		return menuItemListener;
 	}
 	
 	private void handleWidgetSelection(final Event event) {
 		// Special check for ToolBar dropdowns...
 		if (openDropDownMenu(event)) {
 			return;
 		}
 		
 		if ((style & (SWT.TOGGLE | SWT.CHECK)) != 0) {
 			if (event.widget instanceof ToolItem) {
 				checkedState = ((ToolItem) event.widget).getSelection();
 			} else if (event.widget instanceof MenuItem) {
 				checkedState = ((MenuItem) event.widget).getSelection();
 			}
 		}
 		
 		try {
 			final ExecutionEvent executionEvent = command != null ?
 					handlerService.createExecutionEvent(command, event) :
 					new ExecutionEvent(null, Collections.EMPTY_MAP, null, handlerService.createContextSnapshot(true));
 			commandHandler.execute(executionEvent);
 		} catch (final ExecutionException e) {
 			StatusManager.getManager().handle(new Status(IStatus.ERROR, SharedUIResources.PLUGIN_ID,
 					"Failed to execute item " + getId(), e)); //$NON-NLS-1$
 //		} catch (NotDefinedException e) {
 //			WorkbenchPlugin.log("Failed to execute item " //$NON-NLS-1$
 //					+ getId(), e);
 //		} catch (NotEnabledException e) {
 //			WorkbenchPlugin.log("Failed to execute item " //$NON-NLS-1$
 //					+ getId(), e);
 //		} catch (NotHandledException e) {
 //			WorkbenchPlugin.log("Failed to execute item " //$NON-NLS-1$
 //					+ getId(), e);
 		}
 	}
 	
 	/**
 	 * Determines if the selection was on the dropdown affordance and, if so,
 	 * opens the drop down menu (populated using the same id as this item...
 	 * 
 	 * @param event
 	 *            The <code>SWT.Selection</code> event to be tested
 	 * 
 	 * @return <code>true</code> iff a drop down menu was opened
 	 */
 	private boolean openDropDownMenu(final Event event) {
 		final Widget item = event.widget;
 		if (item != null) {
 			final int style = item.getStyle();
 			if ((style & SWT.DROP_DOWN) != 0) {
 				if (event.detail == 4) { // on drop-down button
 					final ToolItem ti = (ToolItem) item;
 					
 					final MenuManager menuManager = new MenuManager();
 					final Menu menu = menuManager.createContextMenu(ti.getParent());
 					if (workbenchHelpSystem != null) {
 						workbenchHelpSystem.setHelp(menu, helpContextId);
 					}
 					initDropDownMenu(menuManager);
 					
 					// position the menu below the drop down item
 					final Point point = ti.getParent().toDisplay(
 							new Point(event.x, event.y));
 					menu.setLocation(point.x, point.y); // waiting for SWT
 					// 0.42
 					menu.setVisible(true);
 					return true; // we don't fire the action
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	protected void initDropDownMenu(final MenuManager menuManager) {
 		String id = dropDownMenuOverride;
 		if (id == null) {
 			id = getId();
 		}
 		MenuUtil.registerOneWayMenu(menuManager, id);
 	}
 	
 	private void updateIcons() {
 		if (widget instanceof MenuItem) {
 			final MenuItem item = (MenuItem) widget;
 			final LocalResourceManager m = new LocalResourceManager(JFaceResources
 					.getResources());
 			try {
 				item.setImage(icon == null ? null : m.createImage(icon));
 			} catch (final DeviceResourceException e) {
 				icon = ImageDescriptor.getMissingImageDescriptor();
 				item.setImage(m.createImage(icon));
 				// as we replaced the failed icon, log the message once.
 				StatusManager.getManager().handle(new Status(IStatus.ERROR, SharedUIResources.PLUGIN_ID,
 						"Failed to load image", e)); //$NON-NLS-1$
 			}
 			disposeOldImages();
 			localResourceManager = m;
 		} else if (widget instanceof ToolItem) {
 			final ToolItem item = (ToolItem) widget;
 			final LocalResourceManager m = new LocalResourceManager(JFaceResources
 					.getResources());
 			item.setDisabledImage(disabledIcon == null ? null : m
 					.createImage(disabledIcon));
 			item.setHotImage(hoverIcon == null ? null : m
 					.createImage(hoverIcon));
 			item.setImage(icon == null ? null : m.createImage(icon));
 			disposeOldImages();
 			localResourceManager = m;
 		}
 	}
 	
 	public void setText(final String text) {
 		label = text;
 		update(null);
 	}
 	
 	public void setChecked(final boolean checked) {
 		if (checkedState == checked) {
 			return;
 		}
 		checkedState = checked;
 		if (widget instanceof MenuItem) {
 			((MenuItem) widget).setSelection(checkedState);
 		} else if (widget instanceof ToolItem) {
 			((ToolItem) widget).setSelection(checkedState);
 		}
 	}
 	
 	public void setTooltip(final String text) {
 		tooltip = text;
 		if (widget instanceof ToolItem) {
 			((ToolItem) widget).setToolTipText(text);
 		}
 	}
 	
 	public void setIcon(final ImageDescriptor desc) {
 		icon = desc;
 		updateIcons();
 	}
 	
 	public void setDisabledIcon(final ImageDescriptor desc) {
 		disabledIcon = desc;
 		updateIcons();
 	}
 	
 	public void setHoverIcon(final ImageDescriptor desc) {
 		hoverIcon = desc;
 		updateIcons();
 	}
 	
 	@Override
 	public boolean isEnabled() {
 		if (commandHandler != null) {
 			commandHandler.setEnabled((menuService != null) ? menuService.getCurrentState() : null);
 			return commandHandler.isEnabled();
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean isVisible() {
 		if (visibleEnabled) {
 			return super.isVisible() && isEnabled();
 		}
 		return super.isVisible();
 	}
 	
 }
