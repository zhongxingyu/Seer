 /* The MIT License
  * 
  * Copyright (c) 2005 David Rice, Trevor Croft, Jay Gorrell
  * 
  * Permission is hereby granted, free of charge, to any person 
  * obtaining a copy of this software and associated documentation files 
  * (the "Software"), to deal in the Software without restriction, 
  * including without limitation the rights to use, copy, modify, merge, 
  * publish, distribute, sublicense, and/or sell copies of the Software, 
  * and to permit persons to whom the Software is furnished to do so, 
  * subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be 
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS 
  * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN 
  * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
  * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
  * SOFTWARE.
  */
 
 package net.rptools.maptool.client.ui.token;
 
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Toolkit;
 import java.awt.Transparency;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JRadioButton;
 import javax.swing.JTextField;
 
 import com.jeta.forms.components.image.ImageComponent;
 
 import net.rptools.lib.swing.SwingUtil;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.swing.AbeilleDialog;
 import net.rptools.maptool.model.Asset;
 import net.rptools.maptool.model.AssetManager;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.util.ImageManager;
 
 /**
  * This dialog is used to display all of the token states and notes to the user.
  * 
  * @author jgorrell
  * @version $Revision$ $Date$ $Author$
  */
 public class NewTokenDialog extends AbeilleDialog {
 
 	/**
 	 * The size used to constrain the icon.
 	 */
 	public static final int SIZE = 64;
 
 	private Token token;
 	private boolean success;
 	
 	private int centerX;
 	private int centerY;
 	
 	/**
 	 * Create a new token notes dialog.
 	 * 
 	 * @param token
 	 *            The token being displayed.
 	 */
 	public NewTokenDialog(Token token, int x, int y) {
 		super("net/rptools/maptool/client/ui/forms/newTokenDialog.jfrm", MapTool.getFrame(), "New Token", true);
 		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 		this.token = token;
 		centerX = x;
 		centerY = y;
 		
 		init();
 		
 		pack();
 	}
 	
 	private void init() {
 		
 		initNameTextField();
 		initGMNameTextField();
 		initNPCTypeRadio();
 		
 		initTokenIconPanel();
 		
 		initOKButton();
 		initCancelButton();
 		
 	}
 
 	public ImageComponent getTokenIconPanel() {
 		return (ImageComponent) getComponent("tokenIcon");
 	}
 
 	public JTextField getNameTextField() {
 		return (JTextField) getComponent("name");
 	}
 	
 	public JTextField getGMNameTextField() {
 		return (JTextField) getComponent("gmName");
 	}
 
 	public JRadioButton getNPCTypeRadio() {
 		return (JRadioButton) getComponent("npcType");
 	}
 	
 	public JRadioButton getPCTypeRadio() {
 		return (JRadioButton) getComponent("pcType");
 	}
 	
 	public JButton getOKButton() {
 		return (JButton) getComponent("okButton");
 	}
 	
 	public JButton getCancelButton() {
 		return (JButton) getComponent("cancelButton");
 	}
 	
 	public void initNameTextField() {
 		getNameTextField().setText(token.getName());
 	}
 	
 	public void initGMNameTextField() {
		getGMNameTextField().setText(token.getGMName());
 	}
 	
 	public void initNPCTypeRadio() {
 		getNPCTypeRadio().setSelected(true);
 	}
 	
 	private void initTokenIconPanel() {
 		getTokenIconPanel().setPreferredSize(new Dimension(100, 100));
 		getTokenIconPanel().setMinimumSize(new Dimension(100, 100));
 		getTokenIconPanel().setIcon(getTokenIcon());
 	}
 	
 	private void initOKButton() {
 		getOKButton().addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				success = true;
 				updateToken();
 				close();
 			}
 		});
 		getRootPane().setDefaultButton(getOKButton());
 	}
 	
 	private void initCancelButton() {
 		getCancelButton().addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				success = false;
 				close();
 			}
 		});
 	}
 
 	public boolean isSuccess() {
 		return success;
 	}
 	
 	private void close() {
 		setVisible(false);
 	}
 	
 	@Override
 	public void setVisible(boolean b) {
 		if(b) {
 			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 			Dimension size = getSize();
 			int x = centerX - size.width/2;
 			int y = centerY - size.height/2;
 			if (x < 0) {x = 0;}
 			if (y < 0) {y = 0;}
 			if (x+size.width > screenSize.width) {x = screenSize.width-size.width;}
 			if (y+size.height > screenSize.height) {y = screenSize.height-size.height;}
 			
 			setLocation(x, y);
 		}
 		super.setVisible(b);
 	}
 
 	/*---------------------------------------------------------------------------------------------
 	 * Instance Methods
 	 *-------------------------------------------------------------------------------------------*/
 
 	/**
 	 * Update the token to match the state of the dialog
 	 */
 	public void updateToken() {
 
 		token.setName(getNameTextField().getText());
 		token.setGMName(getGMNameTextField().getText());
 		if (getNPCTypeRadio().isSelected()) {
 			token.setType(Token.Type.NPC);
 		}
 		if (getPCTypeRadio().isSelected()) {
 			token.setType(Token.Type.PC);
 		}
 	}
 
 	/**
 	 * Get and icon from the asset manager and scale it properly.
 	 * 
 	 * @return An icon scaled to fit within a cell.
 	 */
 	private Icon getTokenIcon() {
 
 		// Get the base image && find the new size for the icon
 		BufferedImage assetImage = null;
 		Asset asset = AssetManager.getAsset(token.getAssetID());
 		if (asset == null) {
 			assetImage = ImageManager.UNKNOWN_IMAGE;
 		} else {
 			assetImage = ImageManager.getImageAndWait(asset);
 		}
 
 		// Need to resize?
 		Dimension imgSize = new Dimension(assetImage.getWidth(), assetImage.getHeight());
 		SwingUtil.constrainTo(imgSize, SIZE);
 		BufferedImage image = new BufferedImage(imgSize.width, imgSize.height, Transparency.BITMASK);
 		Graphics2D g = image.createGraphics();
 		g.drawImage(assetImage, (SIZE - imgSize.width)/2, (SIZE - imgSize.height)/2, imgSize.width, imgSize.height, null);
 		g.dispose();
 		return new ImageIcon(image);
 	}
 
 }
