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
 package cc.warlock.rcp.ui;
 
 import org.eclipse.core.commands.Command;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.commands.common.NotDefinedException;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandImageService;
 import org.eclipse.ui.commands.ICommandService;
 
 public class ConnectionCommand implements IConnectionCommand {
 
 	protected Command command;
 	protected Image image;
 	
 	public ConnectionCommand (String commandId)
 	{
 		ICommandService service =
 			(ICommandService) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(ICommandService.class);
 		ICommandImageService imageService =
 			(ICommandImageService) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(ICommandImageService.class);
 		
 		command = service.getCommand(commandId);
 		
 		ImageDescriptor descriptor = imageService.getImageDescriptor(commandId);
 		if (descriptor != null) {
 			image = descriptor.createImage();
 		}
 	}
 	
 	public String getDescription() {
 		try {
 			return command.getDescription();
 		} catch (NotDefinedException e) {
 		}
 		return "";
 	}
 
 	public Image getImage() {
 		return image;
 	}
 
 	public String getLabel() {
 		try {
 			return command.getName();
 		} catch (NotDefinedException e) {
 		}
 		return "";
 	}
 
 	public void run() {
 		try {
 			//TODO instantiating an ExecutionEvent is "bad" ?
 			command.getHandler().execute(null);
 		} catch (ExecutionException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	protected void finalize() throws Throwable {
 		if (image != null)
 			image.dispose();
 		super.finalize();
 	}
 
 }
