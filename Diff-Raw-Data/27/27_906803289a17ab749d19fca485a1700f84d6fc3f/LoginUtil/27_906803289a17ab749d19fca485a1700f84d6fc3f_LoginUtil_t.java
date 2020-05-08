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
 package cc.warlock.rcp.stormfront.ui.util;
 
 import java.io.IOException;
 import java.util.Map;
 
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 
 import cc.warlock.core.client.IWarlockStyle;
 import cc.warlock.core.client.WarlockString;
 import cc.warlock.core.client.internal.WarlockStyle;
 import cc.warlock.core.network.IConnection.ErrorType;
 import cc.warlock.core.stormfront.client.IStormFrontClient;
 import cc.warlock.core.stormfront.network.SGEConnection;
 import cc.warlock.rcp.stormfront.ui.StormFrontPerspectiveFactory;
 import cc.warlock.rcp.stormfront.ui.views.BarsView;
 import cc.warlock.rcp.stormfront.ui.views.HandsView;
 import cc.warlock.rcp.stormfront.ui.views.StormFrontGameView;
 import cc.warlock.rcp.util.RCPUtil;
 import cc.warlock.rcp.views.GameView;
 
 public class LoginUtil {
 
 	public static void connect (StormFrontGameView gameView, Map<String,String> loginProperties)
 	{
 		String server = loginProperties.get("GAMEHOST");
 		int port = Integer.parseInt (loginProperties.get("GAMEPORT"));
 		String key = loginProperties.get("KEY");
 		
 		// TODO: Somehow make sure this is a StormFrontClient rather than getting a random client/GameView.
 		IStormFrontClient client = StormFrontClientFactory.createStormFrontClient();
 		IStormFrontClient sfclient = (IStormFrontClient) client;
 		sfclient.getGameCode().set(loginProperties.get("GAMECODE"));
 		gameView.setClient(client);
 		
 		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 		
 		try {
 			if (page.findView(HandsView.VIEW_ID) == null)
 			{
 				page.showView(HandsView.VIEW_ID);
 			}
 			if (page.findView(BarsView.VIEW_ID) == null)
 			{
 				page.showView(BarsView.VIEW_ID);
 			}
 		} catch (PartInitException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		try {
 			client.connect(server, port, key);
 			gameView.setFocus();
 			
 		} catch (IOException e) {
 			String errorConnectMessage =
 			"******************************************************************\n" +
 			"* The connection was refused, possibly meaning the server is currently down,\n" +
 			"* or your internet connection is not active\n" +
 			"******************************************************************\n";
 			
 			IWarlockStyle style = new WarlockStyle();
 			style.addStyleType(IWarlockStyle.StyleType.MONOSPACE);
 			
 			client.getDefaultStream().send(new WarlockString(errorConnectMessage, style));
 		}
 	}
 	
 	public static void connectAndOpenGameView (Map<String,String> loginProperties, String characterName)
 	{
 		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 		if (!page.getPerspective().getId().equals(StormFrontPerspectiveFactory.PERSPECTIVE_ID))
 		{
 			RCPUtil.openPerspective(StormFrontPerspectiveFactory.PERSPECTIVE_ID);
 		}
 		
 		StormFrontGameView firstEmptyView = null;
 		for (GameView view : GameView.getOpenGameViews()) {
			if (!(view instanceof StormFrontGameView))
				continue;

			if (view.getWarlockClient() == null
					|| view.getWarlockClient().getConnection() == null
					|| !view.getWarlockClient().getConnection().isConnected()) {
				firstEmptyView = (StormFrontGameView) view;
				break;
			}
 		}
 		
 		if (firstEmptyView != null)
 		{
 			// reuse the existing view if it's already created
 			
 			GameView.initializeGameView(firstEmptyView);
 			connect(firstEmptyView, loginProperties);
 		}
 		else 
 		{
 			connect((StormFrontGameView) StormFrontGameView.createNext(StormFrontGameView.VIEW_ID, characterName), loginProperties);
 		}
 	}
 	
 	public static void showAuthenticationError (int status)
 	{
 		String message = "Warlock was unable to log your account in due to the following error: ";
 		message += getAuthenticationError(status);
 		
 		String title = "Login Error: ";
 		
 		switch (status)
 		{
 			case SGEConnection.ACCOUNT_REJECTED:
 			{
 				title += "Account Rejected";
 			} break;
 			case SGEConnection.INVALID_ACCOUNT:
 			{
 				title += "Invalid Account";
 			} break;
 			case SGEConnection.INVALID_PASSWORD:
 			{
 				title += "Wrong Password";
 			} break;
 			case SGEConnection.ACCOUNT_EXPIRED:
 			{
 				title += "Account Expired";
 			} break;
 		}
 		
 		if (title != null && message != null)
 			MessageDialog.openError(Display.getDefault().getActiveShell(), title, message);
 	}
 	
 	public static String getAuthenticationError (int status)
 	{
 		switch (status)
 		{
 			case SGEConnection.ACCOUNT_REJECTED:
 				return "The account was rejected by the server.";
 			case SGEConnection.INVALID_ACCOUNT:
 				return "The account was not recognized by the server.";
 			case SGEConnection.INVALID_PASSWORD:
 				return "The password you entered was incorrect.";
 			case SGEConnection.ACCOUNT_EXPIRED:
 				return "This account or subscription has expired.";
 		}
 		
 		return null;
 	}
 	
 	public static void showConnectionError (ErrorType errorType)
 	{
 		switch (errorType) {
 			case ConnectionRefused: {
 				MessageDialog.openError(Display.getDefault().getActiveShell(), "Connection to SGE Refused", 
 					"The connection to eaccess.play.net was refused. This could be caused by a server outage, or a firewall blocking access.");
 			} break;
 			case UnknownHost: {
 				MessageDialog.openError(Display.getDefault().getActiveShell(), "Error connecting to SGE", 
 					"There was an error connecting to SGE: The server: \"eaccess.play.net\" was returned as unknown by your DNS.");
 			} break;
 			default: break;
 		}
 	}
 }
