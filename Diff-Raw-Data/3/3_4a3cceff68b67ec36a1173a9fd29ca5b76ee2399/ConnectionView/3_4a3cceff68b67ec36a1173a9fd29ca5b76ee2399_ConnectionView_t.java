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
 package cc.warlock.rcp.views;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.InvalidRegistryObjectException;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandImageService;
 import org.eclipse.ui.commands.ICommandService;
 import org.eclipse.ui.forms.FormColors;
 import org.eclipse.ui.forms.IFormColors;
 import org.eclipse.ui.forms.events.ExpansionAdapter;
 import org.eclipse.ui.forms.events.ExpansionEvent;
 import org.eclipse.ui.forms.events.HyperlinkAdapter;
 import org.eclipse.ui.forms.events.HyperlinkEvent;
 import org.eclipse.ui.forms.widgets.FormText;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.ImageHyperlink;
 import org.eclipse.ui.forms.widgets.ScrolledForm;
 import org.eclipse.ui.forms.widgets.Section;
 import org.eclipse.ui.part.ViewPart;
 
 import cc.warlock.rcp.plugin.Warlock2Plugin;
 import cc.warlock.rcp.ui.ConnectionCommand;
 import cc.warlock.rcp.ui.IConnectionCommand;
 import cc.warlock.rcp.ui.IConnectionCommandProvider;
 import cc.warlock.rcp.ui.WarlockSharedImages;
 import cc.warlock.rcp.util.RCPUtil;
 
 public class ConnectionView extends ViewPart {
 
 	public static final String VIEW_ID = "cc.warlock.rcp.views.ConnectionView"; //$NON-NLS-1$
 	
 	protected FormToolkit toolkit;
 	protected ScrolledForm form;
 	protected Button closeButton;
 	
 	HashMap <String, ArrayList<CommandDescription>> groups = new HashMap<String, ArrayList<CommandDescription>>();
 	public static boolean closeAfterConnect = true;
 	
 	protected class CommandDescription implements Comparable<CommandDescription>
 	{
 		public IConnectionCommand command;
 		public String groupName;
 		public int weight = 0;
 		
 		public int compareTo(CommandDescription o) {
 			return weight - o.weight;
 		}
 	}
 	
 	public ConnectionView() {	}
 
 	@Override
 	public void createPartControl(Composite parent) {
 		FormColors colors = new FormColors(parent.getDisplay());
 		colors.createColor(IFormColors.H_GRADIENT_END, 25, 25, 50);
 		colors.createColor(IFormColors.H_GRADIENT_START, 75, 75, 100);
 		colors.createColor(IFormColors.H_BOTTOM_KEYLINE1, 75, 75, 100);
 		colors.createColor(IFormColors.H_BOTTOM_KEYLINE2, 75, 75, 100);
 		colors.createColor(IFormColors.TITLE, 240, 240, 255);
 
 		toolkit = new FormToolkit(colors);
 		form = toolkit.createScrolledForm(parent);
 		toolkit.decorateFormHeading(form.getForm());
 		
 		form.setImage(WarlockSharedImages.getImage(WarlockSharedImages.IMG_CONNECT));
 		form.setText(ViewMessages.ConnectionView_formTitle);
 		form.getBody().setLayout(new GridLayout(1, false));
 		
 		FormText text = toolkit.createFormText(form.getBody(), true);
 		text.setText(ViewMessages.ConnectionView_welcomeMessage, true, true);
 		text.addHyperlinkListener(new HyperlinkAdapter() {
 			@Override
 			public void linkActivated(HyperlinkEvent e) {
 				if ("warlock_webpage".equals(e.getHref())) {
 					RCPUtil.openURL("http://www.warlock.cc");
 				}
 			}
 		});
 		
 		closeButton = toolkit.createButton(form.getBody(), ViewMessages.ConnectionView_closeWindowButton, SWT.CHECK);
 		closeButton.setSelection(closeAfterConnect);
 		
 		createGroups();
 	}
 	
 	protected ArrayList<CommandDescription> getGroupCommands (String group)
 	{
 		if (!groups.containsKey(group))
 		{
 			groups.put(group, new ArrayList<CommandDescription>());
 		}
 		
 		return groups.get(group);
 	}
 	
 	protected void createGroups ()
 	{
 		IExtension extensions[] = Warlock2Plugin.getDefault().getExtensions("cc.warlock.rcp.connectionCommands"); //$NON-NLS-1$
 		
 		ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
 		ICommandImageService imageService = (ICommandImageService) PlatformUI.getWorkbench().getService(ICommandImageService.class);
 		
 		for (IExtension extension : extensions)
 		{
 			for (IConfigurationElement element : extension.getConfigurationElements())
 			{
 				if (element.getName().equals("connectionCommand")) //$NON-NLS-1$
 				{
 					String commandId = element.getAttribute("commandId"); //$NON-NLS-1$
 					ImageDescriptor descriptor = imageService.getImageDescriptor(commandId);
 					Image image = descriptor == null ? null : descriptor.createImage();
 					
 					CommandDescription command = new CommandDescription();
 					command.command = new ConnectionCommand(commandId);
 					command.groupName = element.getAttribute("groupName"); //$NON-NLS-1$
 					command.weight = Integer.parseInt(element.getAttribute("weight")); //$NON-NLS-1$
 					
 					getGroupCommands(command.groupName).add(command);
 				}
 				else if (element.getName().equals("dynamic")) //$NON-NLS-1$
 				{
 					try {
 						IConnectionCommandProvider item = (IConnectionCommandProvider) element.createExecutableExtension("classname"); //$NON-NLS-1$
 						String groupName = element.getAttribute("groupName"); //$NON-NLS-1$
 						
 						for (IConnectionCommand connectionCommand : item.getConnectionCommands())
 						{
 							CommandDescription command = new CommandDescription();
 							command.command = connectionCommand;
 							command.groupName = element.getAttribute("groupName"); //$NON-NLS-1$
 							
 							getGroupCommands(command.groupName).add(command);
 						}
 					} catch (InvalidRegistryObjectException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (CoreException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 		
 		for (ArrayList<CommandDescription> commands : groups.values())
 		{
 			Collections.sort(commands);
 		}
 		
 		for (String group : groups.keySet())
 		{
 			Section section = createSection(group);
 			Composite composite = toolkit.createComposite(section);
 			composite.setLayout(new GridLayout());
 			
 			for (final CommandDescription command : groups.get(group))
 			{
 				ImageHyperlink link = toolkit.createImageHyperlink(composite, SWT.NONE);
 				link.setText(command.command.getLabel());
 				link.setImage(command.command.getImage());
 				
 				if (command.command.getDescription() != null)
 				{
 					Label label = toolkit.createLabel(composite, command.command.getDescription(), SWT.NONE);
 					GridData data = new GridData();
 					data.horizontalIndent = command.command.getImage() == null ? 10 : command.command.getImage().getBounds().width;
 					
 					label.setLayoutData(data);
 					label.setForeground(new Color(getSite().getShell().getDisplay(), 0x90, 0x90, 0x90));
 					Font font = label.getFont();
 					label.setFont(new Font(font.getDevice(), font.getFontData()[0].getName(), font.getFontData()[0].getHeight()-2, font.getFontData()[0].getStyle()));
 				}
 				
 				link.addHyperlinkListener(new HyperlinkAdapter() {
 					public void linkActivated(HyperlinkEvent e) {
 						runCommand(command.command);
 					}
 				});
 			}
 
 			section.setClient(composite);
 		}
 	}
 	
 	protected void runCommand (IConnectionCommand command)
 	{
 		closeAfterConnect = closeButton.getSelection();
 		
 		command.run();
 	}
 	
 	protected Section createSection (String title)
 	{
 		Section section = toolkit.createSection(form.getBody(), 
 				  Section.DESCRIPTION|Section.TITLE_BAR|
 				  Section.TWISTIE|Section.EXPANDED);
 		section.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
 		section.addExpansionListener(new ExpansionAdapter() {
 			public void expansionStateChanged(ExpansionEvent e) {
 				form.reflow(true);
 			}
 		});
 		
 		section.setText(title);
 		section.setTitleBarBackground(new Color(Display.getDefault(), 25, 25, 50));
 		section.setTitleBarGradientBackground(new Color(Display.getDefault(), 25, 25, 50));
 		return section;
 	}
 	
 	protected static boolean checkedForUpdates = false;
 	
 	@Override
 	public void dispose() {
 		toolkit.dispose();
 		super.dispose();
 	}
 
 	@Override
 	public void setFocus() {
 	}
 }
