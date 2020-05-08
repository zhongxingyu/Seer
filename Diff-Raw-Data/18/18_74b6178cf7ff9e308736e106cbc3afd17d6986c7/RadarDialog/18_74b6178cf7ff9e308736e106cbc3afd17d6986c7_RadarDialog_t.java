 /*
**  $Id: RadarDialog.java,v 1.2 2006/06/21 15:17:08 rpelisse Exp $
 **
 **  Copyright (C) 2006 ESME SUDRIA ( www.esme.fr ) 
 **
 **  Authors: 
 **	BOUCHER Nicolas <shoub_n@hotmail.com>
 **  	MODELIN Maxence  <maxence_modelin@hotmail.com>
 **  	MULOT Louis <vindic@noos.fr>
 **
 **  This program is free software; you can redistribute it and/or modify
 ** it under the terms of the GNU General Public License as published by
 **  the Free Software Foundation; either version 2 of the License, or
 **  (at your option) any later version.
 **
 ** This program is distributed in the hope that it will be useful,
 **  but WITHOUT ANY WARRANTY; without even the implied warranty of
 **  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 **  GNU General Public License for more details.
 **
 **  You should have received a copy of the GNU General Public License
 ** along with this program; if not, write to the Free Software
 ** Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 **
 **
 */
 /*
  * Created on 31 mai 2006
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package org.qsos.interfaces;
 
 
 
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.TitleAreaDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Shell;
 import org.qsos.data.Messages;
 import org.qsos.main.JQ;
 import org.qsos.radar.GenerateRadar;
 
 /**
  * This class represents the Action to make radar dialog
  * 
  * <br>
  * first step:
  * <br>open new dialog box to choose the contents
  * 
  * <br><br>second step:
  * <br>Test if they are at minimum 4 contents check oR 7 maximun
  * 
  * <br><br>Third step:
  * <br>Create a new window with the image of the radar
  */
 /**
  * @author MULOT_L
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class RadarDialog extends  TitleAreaDialog
 {
 
 	private JQ window;
 	private GenerateRadar generateRadar;
 	
 	
 	
 	/**
 	 * @param parent
 	 * @param w
 	 */
 	public RadarDialog(Shell parent, JQ w) 
 	{
 		super(parent);
 		setShellStyle( SWT.MODELESS  | SWT.TITLE);
 		
 		window = w;
 	}
 	
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
 	 */
 	protected Control createContents(Composite parent)
 	{
 		Control contents = super.createContents(parent);
 		String title= ((String) ((SheetCTabItem) window.getCTabFolder().getSelection()).getSaveLibQSOS().getAppname()).concat(((SheetCTabItem) window.getCTabFolder().getSelection()).getSaveLibQSOS().getRelease());
 
 		// Set the title
 		setTitle(title);
 		
 		// Set the message
 		setMessage(Messages.getString("RadarDialog.SelectFields4to7")); //$NON-NLS-1$
 			
 		
 		return contents;
 		
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
 	 */
 	protected Control createDialogArea (Composite parent)
 	{
 		Composite composite = (Composite) super.createDialogArea(parent);
 		
 		GenerateRadar generateFactor = new GenerateRadar(window);
 		generateFactor.createCheckboxTree(parent);
 		generateRadar = generateFactor;
 		
 		return composite;
 		
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
 	 */
 	protected void createButtonsForButtonBar(Composite parent)
 	{
 
 		createButton (parent, IDialogConstants.OPEN_ID,Messages.getString("RadarDialog.buttonCreateRadar"),true); //$NON-NLS-1$
 
 		createButton(parent, IDialogConstants.CLOSE_ID, Messages.getString("RadarDialog.close"),false); //$NON-NLS-1$
 
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
 	 */
 	protected void buttonPressed (int buttonId)
 	{
 		if (buttonId == IDialogConstants.CLOSE_ID)
 		{
 			setReturnCode (buttonId);
 			close();
 		}
 		else if (buttonId == IDialogConstants.OPEN_ID)
 		{
 			// When you push the button a new Image of the radar is create
 			generateRadar.createRadar();
			close();
 		}
 	}
 	
 
 	public void run()
 	{
 		this.open();
		
 	}
 
 }
