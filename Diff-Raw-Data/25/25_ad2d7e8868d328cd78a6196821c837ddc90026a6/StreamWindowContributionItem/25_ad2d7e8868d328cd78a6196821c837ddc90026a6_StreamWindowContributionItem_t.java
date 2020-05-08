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
 package cc.warlock.rcp.stormfront.ui.menu;
 
import java.util.ArrayList;

 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.ActionContributionItem;
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.actions.CompoundContributionItem;
 
import cc.warlock.core.client.IStreamFilter;
 import cc.warlock.core.stormfront.client.IStormFrontClient;
 import cc.warlock.rcp.actions.OpenStreamWindowAction;
 import cc.warlock.rcp.views.DebugView;
 import cc.warlock.rcp.views.GameView;
 import cc.warlock.rcp.views.ScriptManager;
 import cc.warlock.rcp.views.StreamView;
 
 
 public class StreamWindowContributionItem extends CompoundContributionItem {
 
 	private class DebugAction extends Action {
 		
 		private static final String title = "Debug";
 		
 		public DebugAction() {
 			super(title, Action.AS_CHECK_BOX);
 		}
 		
 		public void run() {
 			try {
 				DebugView view = (DebugView)
 				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(DebugView.VIEW_ID, null, IWorkbenchPage.VIEW_VISIBLE);
 				GameView inFocus = GameView.getGameViewInFocus();
 				if (inFocus != null) {
 					view.setClient(inFocus.getWarlockClient());
 				}
 				
 			} catch(Exception e) {
 				e.printStackTrace();
 			}
 		}
 		
 		@Override
 		public String getText() {
 	 		return title;
 		}
 	}
 	
 	private class ScriptManagerAction extends Action {
 		
 		private static final String title = "Scripts Manager";
 		
 		public ScriptManagerAction() {
 			super(title, Action.AS_CHECK_BOX);
 		}
 		
 		public void run() {
 			try {
 				ScriptManager view = (ScriptManager)
 				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(ScriptManager.VIEW_ID, null, IWorkbenchPage.VIEW_VISIBLE);
 				
 			} catch(Exception e) {
 				e.printStackTrace();
 			}
 		}
 		
 		@Override
 		public String getText() {
 	 		return title;
 		}
 	}
 	
 	@Override
 	protected IContributionItem[] getContributionItems() {	
		ArrayList<IContributionItem> items = new ArrayList<IContributionItem>();
		items.add(streamContribution("Thoughts", IStormFrontClient.THOUGHTS_STREAM_NAME, StreamView.TOP_STREAM_PREFIX));
		items.add(streamContribution("Inventory", IStormFrontClient.INVENTORY_STREAM_NAME, StreamView.RIGHT_STREAM_PREFIX));
		items.add(streamContribution("Deaths", IStormFrontClient.DEATH_STREAM_NAME, StreamView.TOP_STREAM_PREFIX));
		items.add(streamContribution("Current Room", IStormFrontClient.ROOM_STREAM_NAME, StreamView.RIGHT_STREAM_PREFIX));
		items.add(streamContribution("Familiar", IStormFrontClient.FAMILIAR_STREAM_NAME, StreamView.RIGHT_STREAM_PREFIX));
		items.add(streamContribution("My Bag", IStormFrontClient.STOW_STREAM_NAME, StreamView.RIGHT_STREAM_PREFIX));
		items.add(new ActionContributionItem(new DebugAction()));
		//items.add(new ActionContributionItem(new ScriptManagerAction()));
		return items.toArray(new IContributionItem[items.size()]);
 	}
 	
 	protected IContributionItem streamContribution(String label, String streamName, String prefix)
 	{
 		return new ActionContributionItem(new OpenStreamWindowAction(label, streamName, prefix));
 	}
 
 }
