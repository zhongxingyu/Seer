 /**
  * Warlock, the open-source cross-platform game client
  *  
  * Copyright 2008, Warlock LLC, and individual contributors as indicated
  * by the @authors tag. 
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 /*
  * Created on Dec 30, 2004
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package cc.warlock.rcp.application;
 
 import org.eclipse.ui.IFolderLayout;
 import org.eclipse.ui.IPageLayout;
 import org.eclipse.ui.IPerspectiveFactory;
 import org.eclipse.ui.IPlaceholderFolderLayout;
 
 import cc.warlock.rcp.views.ConnectionView;
 
 /**
  * @author Marshall
  */
 public class WarlockPerspectiveFactory implements IPerspectiveFactory {
 
 	public static final String WARLOCK_PERSPECTIVE_ID = "cc.warlock.warlockPerspective";
 	public static final String BOTTOM_FOLDER_ID = "cc.warlock.bottomFolder";
 	public static final String TOP_FOLDER_ID = "cc.warlock.topFolder";
 	public static final String RIGHT_FOLDER_ID = "cc.warlock.rightFolder";
 	public static final String LEFT_FOLDER_ID = "cc.warlock.leftFolder";
 	public static final String MAIN_FOLDER_ID = "cc.warlock.mainFolder";
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
 	 */
 	public void createInitialLayout(IPageLayout layout) {
 		layout.setEditorAreaVisible(false);
 		IFolderLayout mainFolder = layout.createFolder(MAIN_FOLDER_ID, IPageLayout.BOTTOM, 0.15f, layout.getEditorArea());
 		mainFolder.addView(ConnectionView.VIEW_ID);
 		
 		mainFolder.addPlaceholder("*GameView:*");
 
 //		layout.addStandaloneView(HandsView.VIEW_ID, false, IPageLayout.TOP, 0.05f, GameView.VIEW_ID);
 //		layout.addStandaloneView(StatusView.VIEW_ID, false, IPageLayout.RIGHT, .5f, HandsView.VIEW_ID);
 		
 		IPlaceholderFolderLayout topFolder =
 			layout.createPlaceholderFolder(TOP_FOLDER_ID, IPageLayout.TOP, 0.15f, MAIN_FOLDER_ID);
 		topFolder.addPlaceholder("*topStream:*");
 		
 //		topFolder.addPlaceholder(StreamView.DEATH_VIEW_ID);
 //		topFolder.addPlaceholder(StreamView.THOUGHTS_VIEW_ID);
 		
 		IPlaceholderFolderLayout rightFolder =
 			layout.createPlaceholderFolder(RIGHT_FOLDER_ID, IPageLayout.RIGHT, 0.75f, MAIN_FOLDER_ID);
 		rightFolder.addPlaceholder("*rightStream:*");
 		
 		IPlaceholderFolderLayout leftFolder =
			layout.createPlaceholderFolder(LEFT_FOLDER_ID, IPageLayout.LEFT, 0.75f, MAIN_FOLDER_ID);
 		leftFolder.addPlaceholder("*leftStream:*");
 		
 //		rightFolder.addPlaceholder(StreamView.INVENTORY_VIEW_ID);
 		rightFolder.addPlaceholder("*DebugView:*");
 		
 		IPlaceholderFolderLayout bottomFolder =
 			layout.createPlaceholderFolder(BOTTOM_FOLDER_ID, IPageLayout.BOTTOM, 0.95f, MAIN_FOLDER_ID);
 		bottomFolder.addPlaceholder("*bottomStream*");
 		
 //		IFolderLayout folder = layout.createFolder(BOTTOM_FOLDER_ID, IPageLayout.BOTTOM, 0.90f, GameView.VIEW_ID);
 //		layout.addStandaloneView(BarsView.VIEW_ID, false, IPageLayout.BOTTOM, 0.95f, GameView.VIEW_ID);
 //		folder.addView("org.eclipse.pde.runtime.LogView");
 		
 //		layout.addView(CompassView.VIEW_ID, IPageLayout.RIGHT, 0.85f, BarsView.VIEW_ID);
 	}
 
 }
