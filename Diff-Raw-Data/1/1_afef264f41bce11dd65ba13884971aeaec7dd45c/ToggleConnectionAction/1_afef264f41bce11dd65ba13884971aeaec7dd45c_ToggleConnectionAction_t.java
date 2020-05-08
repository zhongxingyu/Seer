 package net.bioclipse.xws4j.actions;
 
 import org.eclipse.jface.action.Action;
 import net.bioclipse.xws4j.Activator;
 import net.bioclipse.xws4j.DefaultClientCurator;
 
 /**
  * 
  * This file is part of the Bioclipse xws4j Plug-in.
  * 
  * Copyright (C) 2008 Johannes Wagener
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation; either version 3 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, see <http://www.gnu.org/licenses>.
  * 
  * @author Johannes Wagener
  */
 public class ToggleConnectionAction extends Action {
 	final String CONNECT_DESC = "Connect";
 	final String DISCONNECT_DESC = "Disconnect";
 
 	public ToggleConnectionAction() {
 		super();
 	}
 	
 	public void update() {
 		if (Activator.getDefaultClientCurator().isClientConnected() == true) {
 			this.setText(DISCONNECT_DESC);
 			setImageDescriptor(Activator.getImageDescriptor("icons/png/disconnected.png"));
 		} else {
 			this.setText(CONNECT_DESC);
 			setImageDescriptor(Activator.getImageDescriptor("icons/png/connected.png"));
 		}
 	}
 
 	public void run() {
 		DefaultClientCurator clientcurator = Activator.getDefaultClientCurator();
 		
 		if (clientcurator.isClientConnected() == true)
 			clientcurator.disconnectClient();
 		else {
 			try {
 				clientcurator.connectClient();
 			} catch (Exception e) {
				clientcurator.disconnectClient();
 			}
 		}
 	}
 }
