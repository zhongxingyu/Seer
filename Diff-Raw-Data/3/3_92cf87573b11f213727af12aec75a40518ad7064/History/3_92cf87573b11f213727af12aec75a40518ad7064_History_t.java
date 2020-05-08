 /*******************************************************************************
  * Copyright (c) 2012-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Stephan Wahlbrink - initial API and implementation
  *******************************************************************************/
 
 package de.walware.ecommons.ui.components;
 
 import static org.eclipse.ui.IWorkbenchCommandConstants.NAVIGATE_BACKWARD_HISTORY;
 import static org.eclipse.ui.IWorkbenchCommandConstants.NAVIGATE_FORWARD_HISTORY;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.commands.IHandler2;
 import org.eclipse.jface.action.IMenuListener2;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.commands.ICommandService;
 import org.eclipse.ui.commands.IElementUpdater;
 import org.eclipse.ui.handlers.IHandlerService;
 import org.eclipse.ui.menus.CommandContributionItemParameter;
 import org.eclipse.ui.menus.UIElement;
 import org.eclipse.ui.services.IServiceLocator;
 
 import de.walware.ecommons.ui.SharedMessages;
 import de.walware.ecommons.ui.actions.HandlerContributionItem;
 
 
 public class History<E> {
 	
 	
 	private static final int HISTORY_SIZE = 100;
 	
 	private static final int HISTORY_SHOWN = 10;
 	
 	
 	private class NavigateHandler extends AbstractHandler implements IElementUpdater {
 		
 		private final int fRelPos;
 		
 		public NavigateHandler(final int relPos) {
 			fRelPos = relPos;
 		}
 		
 		@Override
 		public void setEnabled(final Object evaluationContext) {
 			final int pos = getPosition(fRelPos);
 			setBaseEnabled(pos >= 0 && pos < fList.size());
 		}
 		
 		@Override
 		public Object execute(final ExecutionEvent event) throws ExecutionException {
 			select(fRelPos);
 			return null;
 		}
 		
 		@Override
 		public void updateElement(final UIElement element, final Map parameters) {
 			final int pos = getPosition(fRelPos);
 			if (pos >= 0 && pos < fList.size()) {
 				final String label = getLabel(fList.get(pos));
				setBaseEnabled(true);
 				element.setText(label);
 				element.setTooltip(NLS.bind((fRelPos <= 0) ?
 						SharedMessages.NavigateBack_1_tooltip :
 						SharedMessages.NavigateForward_1_tooltip,
 						label ));
 			}
 			else {
				setBaseEnabled(false);
 				element.setText(null);
 				element.setTooltip(null);
 			}
 		}
 		
 	}
 	
 	private abstract class NavigateDropdownContribItem extends HandlerContributionItem {
 		
 		public NavigateDropdownContribItem(
 				final CommandContributionItemParameter contributionParameters,
 				final IHandler2 handler) {
 			super(contributionParameters, handler);
 		}
 		
 		
 		@Override
 		protected void initDropDownMenu(final MenuManager menuManager) {
 			menuManager.addMenuListener(new IMenuListener2() {
 				@Override
 				public void menuAboutToShow(final IMenuManager manager) {
 					addItems(menuManager);
 				}
 				@Override
 				public void menuAboutToHide(final IMenuManager manager) {
 					display.asyncExec(new Runnable() {
 						@Override
 						public void run() {
 							menuManager.dispose();
 						}
 					});
 				}
 			});
 		}
 		
 		protected abstract void addItems(IMenuManager menuManager);
 		
 		protected void addItem(final IMenuManager menuManager, final int relPos) {
 			menuManager.add(new HandlerContributionItem(new CommandContributionItemParameter(
 					getServiceLocator(), null, NO_COMMAND_ID,
 					HandlerContributionItem.STYLE_PUSH) ,
 					new NavigateHandler(relPos) ));
 		}
 		
 	}
 	
 	
 	private final List<E> fList = new ArrayList<E>(HISTORY_SIZE);
 	
 	private int fCurrentPosition = -1;
 	private boolean fCurrentPositionSelected;
 	
 	private IServiceLocator fServiceLocator;
 	private final List<IHandler2> fHandler = new ArrayList<IHandler2>();
 	
 	private ISelectionProvider fSelectionProvider;
 	
 	
 	public History() {
 	}
 	
 	
 	public void setSelectionProvider(final ISelectionProvider provider) {
 		fSelectionProvider = provider;
 	}
 	
 	public void addActions(final ToolBarManager toolBar,
 			final IServiceLocator serviceLocator) {
 		fServiceLocator = serviceLocator;
 		final IHandlerService handlerService = (IHandlerService) serviceLocator
 				.getService(IHandlerService.class);
 		final ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
 		{	final NavigateHandler handler = new NavigateHandler(-1);
 			toolBar.add(new NavigateDropdownContribItem(new CommandContributionItemParameter(
 					serviceLocator, null, NAVIGATE_BACKWARD_HISTORY, null,
 					sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_BACK),
 					sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_BACK_DISABLED), null,
 					null, null, null,
 					HandlerContributionItem.STYLE_PULLDOWN,
 					null, false), handler ) {
 				@Override
 				protected void addItems(final IMenuManager menuManager) {
 					if (fCurrentPosition < 0) {
 						return;
 					}
 					int last = fCurrentPosition;
 					if (!fCurrentPositionSelected) {
 						last++;
 					}
 					if (last > HISTORY_SHOWN) {
 						last = HISTORY_SHOWN;
 					}
 					last = -last;
 					for (int relPos = -1; relPos >= last; relPos--) {
 						addItem(menuManager, relPos);
 					}
 				}
 			});
 			fHandler.add(handler);
 			handlerService.activateHandler(NAVIGATE_BACKWARD_HISTORY, handler);
 		}
 		{	final NavigateHandler handler = new NavigateHandler(1);
 			toolBar.add(new NavigateDropdownContribItem(new CommandContributionItemParameter(
 					serviceLocator, null, NAVIGATE_FORWARD_HISTORY, null,
 					sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD),
 					sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD_DISABLED), null,
 					null, null, null,
 					HandlerContributionItem.STYLE_PULLDOWN,
 					null, false), handler ) {
 				@Override
 				protected void addItems(final IMenuManager menuManager) {
 					if (fCurrentPosition < 0) {
 						return;
 					}
 					int last = fList.size() - fCurrentPosition - 1;
 					if (last > HISTORY_SHOWN) {
 						last = HISTORY_SHOWN;
 					}
 					for (int relPos = 1; relPos <= last; relPos++) {
 						menuManager.add(new HandlerContributionItem(new CommandContributionItemParameter(
 								getServiceLocator(), null, NO_COMMAND_ID,
 								HandlerContributionItem.STYLE_PUSH) ,
 								new NavigateHandler(relPos) ));
 					}
 				}
 			});
 			fHandler.add(handler);
 			handlerService.activateHandler(NAVIGATE_FORWARD_HISTORY, handler);
 		}
 	}	
 	
 	public void selected(final E entry) {
 		if (entry == null) {
 			fCurrentPositionSelected = false;
 		}
 		else if (fCurrentPosition < 0 || !fList.get(fCurrentPosition).equals(entry)) {
 			fList.subList(fCurrentPosition + 1, fList.size()).clear();
 			
 			if (fList.size() >= HISTORY_SIZE) {
 				fList.remove(0);
 				fCurrentPosition--;
 			}
 			fList.add(entry);
 			fCurrentPosition++;
 			fCurrentPositionSelected = true;
 		}
 		updateControls();
 	}
 	
 	protected int getPosition(final int relPos) {
 		int pos = fCurrentPosition + relPos;
 		if (!fCurrentPositionSelected && relPos < 0) {
 			pos++;
 		}
 		return pos;
 	}
 	
 	public void select(final int relPos) {
 		final int pos = getPosition(relPos);
 		if (pos >= 0 && pos < fList.size()) {
 			fCurrentPosition = pos;
 			updateControls();
 			select(fList.get(pos));
 		}
 	}
 	
 	protected void select(final E entry) {
 		fSelectionProvider.setSelection(new StructuredSelection(entry));
 	}
 	
 	protected String getLabel(final E entry) {
 		return entry.toString();
 	}
 	
 	protected void updateControls() {
 //		for (final IHandler2 handler : fHandler) {
 //			handler.setEnabled(null);
 //		}
 		if (fServiceLocator != null) {
 			final ICommandService commandService = (ICommandService) fServiceLocator.getService(
 					ICommandService.class);
 			commandService.refreshElements(NAVIGATE_BACKWARD_HISTORY, null);
 			commandService.refreshElements(NAVIGATE_FORWARD_HISTORY, null);
 		}
 	}
 	
 }
