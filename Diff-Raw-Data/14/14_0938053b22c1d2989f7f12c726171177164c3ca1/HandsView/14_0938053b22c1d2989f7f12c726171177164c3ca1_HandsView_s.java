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
 package cc.warlock.rcp.stormfront.ui.views;
 
 import java.util.ArrayList;
 
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.part.ViewPart;
 
 import cc.warlock.core.client.IProperty;
 import cc.warlock.core.client.IPropertyListener;
 import cc.warlock.core.client.IWarlockClient;
 import cc.warlock.core.client.WarlockClientAdapter;
 import cc.warlock.core.client.WarlockClientRegistry;
 import cc.warlock.core.client.internal.ClientProperty;
 import cc.warlock.core.client.settings.IWindowSettings;
 import cc.warlock.core.stormfront.client.IStormFrontClient;
 import cc.warlock.core.stormfront.settings.IStormFrontClientSettings;
 import cc.warlock.rcp.stormfront.ui.StormFrontSharedImages;
 import cc.warlock.rcp.ui.client.SWTPropertyListener;
 import cc.warlock.rcp.util.ColorUtil;
 import cc.warlock.rcp.views.GameView;
 import cc.warlock.rcp.views.IGameViewFocusListener;
 
 public class HandsView extends ViewPart implements IPropertyListener<String> 
 {
 	public static final String VIEW_ID = "cc.warlock.rcp.stormfront.ui.views.HandsView";
 	protected static HandsView _instance;
 	
 	protected GradientInfo leftHandInfo, rightHandInfo, spellInfo;
 	protected IStormFrontClient activeClient;
 	protected ArrayList<IStormFrontClient> clients = new ArrayList<IStormFrontClient>();
 	
 	public HandsView ()
 	{
 		_instance = this;
 		
 		WarlockClientRegistry.addWarlockClientListener(new WarlockClientAdapter() {
 			public void clientConnected(final IWarlockClient client) {
 				if (client instanceof IStormFrontClient)
 				{
 					Display.getDefault().asyncExec(new Runnable() {
 						public void run () {
 							setActiveClient((IStormFrontClient)client);
 						}
 					});
 				}
 			}
 		});
 		GameView.addGameViewFocusListener(new IGameViewFocusListener() {
 			public void gameViewFocused(GameView gameView) {
 				if (gameView instanceof StormFrontGameView)
 				{
 					HandsView.this.gameViewFocused((StormFrontGameView)gameView);
 				}
 			}
 		});
 	}
 	
 	private class GradientInfo extends Canvas implements PaintListener
 	{
 		protected String text;
 		protected Image image;
 		protected Color background, foreground, gradientColor, tabColor;
 		protected Font textFont;
 		
 		public GradientInfo (Composite parent, Image image)
 		{
 			super(parent, SWT.NONE);
 			
 			this.image = image;
 			
 			addPaintListener(this);
 			this.textFont = new Font(getDisplay(), JFaceResources.getHeaderFont().getFontData()[0].getName(), 10, SWT.NONE);
 			this.foreground = new Color(getDisplay(), 255, 255, 255);
 			this.background = new Color(getDisplay(), 0, 0, 0);
 			this.gradientColor = getGradientColor(50, true);
 		}
 		
 		private Color getGradientColor (int factor, boolean lighter)
 		{
 			int red = 0;
 			int green = 0;
 			int blue = 0;
 			
 			if (lighter) 
 			{
 				red = background.getRed() < (255 - factor) ? background.getRed() + factor : 255;
 				green = background.getGreen() < (255 - factor) ? background.getGreen() + factor : 255;
 				blue = background.getBlue() < (255 - factor) ? background.getBlue() + factor : 255;
 			}
 			else {
 				red = background.getRed() > factor ? background.getRed() - factor : 0;
 				green = background.getRed() > factor ? background.getRed() - factor : 0;
 				blue = background.getRed() > factor ? background.getRed() - factor : 0;
 			}
 			
 			return new Color(getShell().getDisplay(), red, green, blue);
 		}
 		
 		@Override
 		public void setForeground(Color color) {
 			this.foreground = color;
 			redraw();
 		}
 		
 		@Override
 		public void setBackground(Color color) {
 			this.background = color;
 			this.gradientColor = getGradientColor(125, true);
 			
 			redraw();
 		}
 		
 		public void paintControl(PaintEvent e) {
 			Rectangle bounds = getBounds();
 			Rectangle imageBounds = image.getBounds();
 			int tabPadding = 4;
 			
 			e.gc.setBackground(background);
 			e.gc.setForeground(gradientColor);
 			e.gc.fillRectangle(0, 0, bounds.width, bounds.height);
 			if(text != null)
 				e.gc.fillGradientRectangle(tabPadding/2, tabPadding/2, bounds.width - tabPadding/2, e.gc.textExtent(text).y, true);
 			
 			e.gc.setForeground(foreground);
 			e.gc.drawImage(image, tabPadding/2, tabPadding/2);
			e.gc.drawText(text, imageBounds.width + tabPadding, tabPadding, true);
 		}
 		
 		public void setText(String text) {
 			this.text = text;
 			redraw();
 		}
 	}
 	
 	@Override
 	public void createPartControl(Composite parent) {
 		parent.setLayout(new FillLayout());
 		parent.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
 		
 		Composite main = new Composite(parent, SWT.NONE);
 		GridLayout layout = new GridLayout(3, false);
 		layout.horizontalSpacing = 0;
 		layout.verticalSpacing = 0;
 		layout.marginHeight = 0;
 		layout.marginWidth = 0;
 		main.setLayout(layout);
 		main.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
 		
 		leftHandInfo = new GradientInfo(main, StormFrontSharedImages.getImage(StormFrontSharedImages.IMG_LEFT_HAND_SMALL));
 		
 		leftHandInfo.setForeground(new Color(main.getDisplay(), 240, 240, 255));
 		leftHandInfo.setBackground(new Color(main.getDisplay(), 25, 25, 50));
 		leftHandInfo.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
 		
 		rightHandInfo = new GradientInfo(main, StormFrontSharedImages.getImage(StormFrontSharedImages.IMG_RIGHT_HAND_SMALL));
 			
 		rightHandInfo.setForeground(new Color(main.getDisplay(), 240, 240, 255));
 		rightHandInfo.setBackground(new Color(main.getDisplay(), 25, 25, 50));
 		rightHandInfo.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
 		
 		spellInfo = new GradientInfo(main, StormFrontSharedImages.getImage(StormFrontSharedImages.IMG_SPELL_HAND_SMALL));
 			
 		spellInfo.setForeground(new Color(main.getDisplay(), 240, 240, 255));
 		spellInfo.setBackground(new Color(main.getDisplay(), 25, 25, 50));
 		spellInfo.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
 		clear();
 	}
 	
 	protected void gameViewFocused (StormFrontGameView gameView)
 	{
 		if (gameView.getStormFrontClient() != null)
 		{
 			setActiveClient(gameView.getStormFrontClient());
 		}
 	}
 	
 	@Override
 	public void setFocus() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void setActiveClient (IStormFrontClient client)
 	{
 		if (client == null) return;
 		
 		this.activeClient = client;
 		
 		if (!clients.contains(client))
 		{
 			clear();
 			client.getLeftHand().addListener(new SWTPropertyListener<String>(this));
 			client.getRightHand().addListener(new SWTPropertyListener<String>(this));
 			client.getCurrentSpell().addListener(new SWTPropertyListener<String>(this));
 			
 			clients.add(client);
 		} else {
 			propertyChanged(client.getLeftHand(), null);
 			propertyChanged(client.getRightHand(), null);
 			propertyChanged(client.getCurrentSpell(), null);
 			loadSettings(client.getStormFrontClientSettings());
 		}
 	}
 
 	protected void setColors (Color fg, Color bg)
 	{
 		leftHandInfo.setBackground(bg);
 		leftHandInfo.setForeground(fg);
 		
 		rightHandInfo.setBackground(bg);
 		rightHandInfo.setForeground(fg);
 		
 		spellInfo.setBackground(bg);
 		spellInfo.setForeground(fg);
 	}
 	
 	public void loadSettings (IStormFrontClientSettings settings)
 	{
 		if(settings == null)
 			return;
 		IWindowSettings mainSettings = settings.getMainWindowSettings();
 		if(mainSettings == null)
 			return;
 		
 		Color bg = ColorUtil.warlockColorToColor(settings.getStormFrontClient().getStormFrontSkin().getMainBackground());
 		Color fg = ColorUtil.warlockColorToColor(settings.getStormFrontClient().getStormFrontSkin().getMainForeground());
 		
 		setColors(fg, bg);
 	}
 	
 	public void propertyActivated(IProperty<String> property) {}
 
 	public void propertyChanged(IProperty<String> property, String oldValue) {
 		if (property != null && property instanceof ClientProperty)
 		{
 			ClientProperty<String> clientProperty = (ClientProperty<String>) property;
 			if (clientProperty.getClient() == activeClient)
 			{
 				if (property.getName().equals("leftHand")) {
 					leftHandInfo.setText(property.get());
 				}
 				else if (property.getName().equals("rightHand")) {
 					rightHandInfo.setText(property.get());
 				}
 				else if (property.getName().equals("currentSpell")) {
 					spellInfo.setText(property.get());
 				}
 			}
 		}
 	}
 
 	public void propertyCleared(IProperty<String> property, String oldValue) {
 		if (property != null && property instanceof ClientProperty)
 		{
 			ClientProperty<String> clientProperty = (ClientProperty<String>) property;
 			if (clientProperty.getClient() == activeClient)
 			{
 				if (property.getName().equals("leftHand"))
 					leftHandInfo.setText("Empty");
 				else if (property.getName().equals("rightHand"))
 					rightHandInfo.setText("Empty");
 				else if (property.getName().equals("currentSpell"))
 					spellInfo.setText("None");
 			}
 		}
 	}
 	
 	protected void clear ()
 	{
 		leftHandInfo.setText("Empty");
 		rightHandInfo.setText("Empty");
 		spellInfo.setText("None");
 	}
 	
 	public static HandsView getDefault ()
 	{
 		return _instance;
 	}
 }
