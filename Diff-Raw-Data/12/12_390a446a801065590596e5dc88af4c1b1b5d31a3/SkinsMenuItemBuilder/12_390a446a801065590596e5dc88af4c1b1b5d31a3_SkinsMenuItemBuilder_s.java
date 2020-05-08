 /*******************************************************************************
  * Copyright (c) 2009 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Ian Trimble - initial API and implementation
  *******************************************************************************/ 
 package org.eclipse.jst.pagedesigner.editors.actions;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jst.jsf.common.metadata.Model;
 import org.eclipse.jst.jsf.common.metadata.Trait;
 import org.eclipse.jst.jsf.common.metadata.internal.TraitValueHelper;
 import org.eclipse.jst.jsf.common.metadata.query.ITaglibDomainMetaDataModelContext;
 import org.eclipse.jst.jsf.common.metadata.query.TaglibDomainMetaDataQueryHelper;
 import org.eclipse.jst.pagedesigner.dtresourceprovider.DTResourceProviderFactory;
 import org.eclipse.jst.pagedesigner.dtresourceprovider.DTSkinManager;
 import org.eclipse.jst.pagedesigner.dtresourceprovider.IDTResourceProvider;
 import org.eclipse.jst.pagedesigner.dtresourceprovider.IDTSkin;
 import org.eclipse.jst.pagedesigner.utils.EditorUtil;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.ui.IWorkbenchActionConstants;
 
 /**
  * Builds menu items for managing skins.
  * 
  * @author Ian Trimble - Oracle
  */
 public class SkinsMenuItemBuilder {
 
 	private static final String DATAKEY_DTSKIN = "DATAKEY_DTSKIN"; //$NON-NLS-1$
 	private static final String DATAKEY_NSURI = "DTATKEY_NSURI"; //$NON-NLS-1$
 
 	private static List<Menu> menuList = new ArrayList<Menu>();
 
 	private IProject project;
 
 	/**
 	 * Constructs an instance.
 	 * 
 	 * @param project IProject instance for which to get skin information.
 	 */
 	public SkinsMenuItemBuilder(IProject project) {
 		this.project = project;
 	}
 
 	/**
 	 * Builds menu items (adds a separator and then menu items to end of
 	 * specified menu.
 	 * 
 	 * @param menu Menu instance to which to add menu items.
 	 */
 	public void buildMenuItems(Menu menu) {
 		for (Menu oldMenu: menuList) {
 			oldMenu.dispose();
 		}
 		if (menu != null) {
 			List<TaglibData> taglibDataList = getTaglibDataList();
 			if (taglibDataList.size() > 0) {
 				new MenuItem(menu, SWT.SEPARATOR);
 			}
 			for (TaglibData taglibData: taglibDataList) {
 				String nsURI = taglibData.getNSURI();
 				DTSkinManager dtSkinManager = DTSkinManager.getInstance(project);
 				IDTSkin currentDTSkin = dtSkinManager.getCurrentSkin(nsURI);
 				MenuItem menuItem = new MenuItem(menu, SWT.CASCADE);
 				menuItem.setText(taglibData.getName());
 				Menu skinMenu = new Menu(menuItem);
 				menuList.add(skinMenu);
 				menuItem.setMenu(skinMenu);
 				List<IDTSkin> dtSkins = dtSkinManager.getSkins(nsURI);
 				for (IDTSkin dtSkin: dtSkins) {
 					MenuItem skinMenuItem;
 					if (currentDTSkin == dtSkin) {
 						skinMenuItem = new MenuItem(skinMenu, SWT.CHECK);
 						skinMenuItem.setSelection(true);
 					} else {
 						skinMenuItem = new MenuItem(skinMenu, SWT.PUSH);
 					}
 					skinMenuItem.setText(dtSkin.getName());
 					skinMenuItem.setData(DATAKEY_DTSKIN, dtSkin);
 					skinMenuItem.setData(DATAKEY_NSURI, nsURI);
 					skinMenuItem.addSelectionListener(new SkinSelectionListener());
 				}
 			}
 		}
 	}
 
 	/**
 	 * Builds menu managers.
 	 * 
 	 * @param parent Parent menu manager to which to append new menu managers.
 	 */
 	public void buildMenuManagers(IMenuManager parent) {
 		List<TaglibData> taglibDataList = getTaglibDataList();
 		if (taglibDataList.size() > 0) {
 			parent.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, new Separator());
 		}
 		for (TaglibData taglibData: taglibDataList) {
 			String nsURI = taglibData.getNSURI();
 			MenuManager newMgr = new MenuManager(taglibData.getName());
 			DTSkinManager dtSkinManager = DTSkinManager.getInstance(project);
 			IDTSkin currentDTSkin = dtSkinManager.getCurrentSkin(nsURI);
 			List<IDTSkin> dtSkins = dtSkinManager.getSkins(nsURI);
 			for (IDTSkin dtSkin: dtSkins) {
 				newMgr.add(new ChangeCurrentSkinAction(nsURI, dtSkin, dtSkin == currentDTSkin));
 			}
 			parent.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, newMgr);
 		}
 	}
 
 	private List<TaglibData> getTaglibDataList() {
 		List<TaglibData> taglibDataList = new ArrayList<TaglibData>();
 		List<IDTResourceProvider> dtResourceProviders =
 			DTResourceProviderFactory.getInstance().getActiveDTResourceProviders(project);
 		for (IDTResourceProvider dtResourceProvider: dtResourceProviders) {
 			TaglibData taglibData = new TaglibData(dtResourceProvider.getId());
 			if (!taglibDataList.contains(taglibData)) {
 				taglibDataList.add(taglibData);
 			}
 		}
 		return taglibDataList;
 	}
 
 
 
 	/**
 	 * Used to hold and pass taglib-related data.
 	 */
 	private class TaglibData {
 		private String nsURI;
 		private String name;
 		public TaglibData(String nsURI) {
 			this.nsURI = nsURI;
 			ITaglibDomainMetaDataModelContext modelContext = TaglibDomainMetaDataQueryHelper.createMetaDataModelContext(project, nsURI);
 			Model model = TaglibDomainMetaDataQueryHelper.getModel(modelContext);
 			Trait trait = TaglibDomainMetaDataQueryHelper.getTrait(model, "display-label"); //$NON-NLS-1$
 			this.name = TraitValueHelper.getValueAsString(trait);
 		}
 		public String getNSURI() {
 			return nsURI;
 		}
 		public String getName() {
 			return name;
 		}
 		/*
 		 * (non-Javadoc)
 		 * @see java.lang.Object#hashCode()
 		 */
 		public int hashCode() {
 			int nsURIHashCode = 0;
 			if (nsURI != null) {
 				nsURIHashCode = nsURI.hashCode();
 			}
 			int nameHashCode = 0;
 			if (name != null) {
 				nameHashCode = name.hashCode();
 			}
 			return nameHashCode | nsURIHashCode ;
 		}
 	}
 
 
 
 	/**
 	 * Selection listener for skin menu items.
 	 */
 	private class SkinSelectionListener implements SelectionListener {
 		/*
 		 * (non-Javadoc)
 		 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
 		 */
 		public void widgetSelected(SelectionEvent event) {
 			String nsURI = (String)event.widget.getData(DATAKEY_NSURI);
 			IDTSkin dtSkin = (IDTSkin)event.widget.getData(DATAKEY_DTSKIN);
 			DTSkinManager.getInstance(project).setCurrentSkin(nsURI, dtSkin);
 			EditorUtil.refreshAllWPEDesignViewers();
 		}
 		/*
 		 * (non-Javadoc)
 		 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
 		 */
 		public void widgetDefaultSelected(SelectionEvent event) {
 			widgetSelected(event);
 		}
 	}
 
 
 
 	/**
 	 * Action for changing current skin.
 	 */
 	private class ChangeCurrentSkinAction extends Action {
 		private String nsURI;
 		private IDTSkin dtSkin;
 		/**
 		 * Creates an instance.
 		 * 
 		 * @param nsURI NSURI of taglib.
 		 * @param dtSkin IDTSkin instance.
 		 * @param checked true if IDTSkin instance is current for nsURI.
 		 */
 		public ChangeCurrentSkinAction(String nsURI, IDTSkin dtSkin, boolean checked) {
 			super(dtSkin.getName());
 			this.nsURI = nsURI;
 			this.dtSkin = dtSkin;
 			setChecked(checked);
 		}
 		/*
 		 * (non-Javadoc)
 		 * @see org.eclipse.jface.action.Action#run()
 		 */
 		public void run() {
 			DTSkinManager.getInstance(project).setCurrentSkin(nsURI, dtSkin);
 			EditorUtil.refreshAllWPEDesignViewers();
 		}
 	}
 
 }
