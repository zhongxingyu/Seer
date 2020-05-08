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
 package cc.warlock.rcp.prefs;
 
 import org.eclipse.jface.preference.ColorSelector;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
 import org.eclipse.ui.IWorkbenchPropertyPage;
 import org.eclipse.ui.dialogs.PropertyPage;
 
 import cc.warlock.rcp.util.FontSelector;
 
public class WarlockPreferencePage extends PropertyPage implements
 		IWorkbenchPropertyPage, SelectionListener, IPropertyChangeListener {
 	
	protected Control createContents(Composite parent) {
		Composite main = new Composite (parent, SWT.NONE);
		
		return main;
	}
	
 	public Button createButton(Composite parent, int flags)
 	{
 		Button button = new Button(parent, flags);
 		button.addSelectionListener(this);
 		
 		return button;
 	}
 	
 	public Button createButton(Composite parent, String text, int flags)
 	{
 		Button button = createButton(parent, flags);
 		button.setText(text);
 		
 		return button;
 	}
 	
 	public Button createButton(Composite parent)
 	{
 		return createButton(parent, SWT.PUSH);
 	}
 	
 	public Button createButton(Composite parent, String text)
 	{
 		return createButton(parent, text, SWT.PUSH);
 	}
 	
 	public Button createCheckbox(Composite parent)
 	{
 		return createButton(parent, SWT.CHECK);
 	}
 	
 	public Button createCheckbox(Composite parent, String text)
 	{
 		return createButton(parent, text, SWT.CHECK);
 	}
 	
 	public Button createRadio(Composite parent)
 	{
 		return createButton(parent, SWT.RADIO);
 	}
 	
 	public Button createRadio(Composite parent, String text)
 	{
 		return createButton(parent, text, SWT.RADIO);
 	}
 
 	public ColorSelector createColorSelector (Composite parent)
 	{
 		ColorSelector selector = new ColorSelector(parent);
 		selector.addListener(this);
 		
 		return selector;
 	}
 	
 	public FontSelector createFontSelector (Composite parent)
 	{
 		FontSelector selector = new FontSelector(parent);
 		selector.addListener(this);
 		
 		return selector;
 	}
 	
 	public void widgetDefaultSelected(SelectionEvent e) {
 		widgetSelected(e);
 	}
 	
 	public void widgetSelected(SelectionEvent e) {
 		if (e.getSource() instanceof Button)
 		{
 			buttonPressed((Button)e.getSource());
 		}
 	}
 	
 	protected void buttonPressed(Button button) {
 		
 	}
 	
 	public void propertyChange(PropertyChangeEvent event) {
 		if (event.getSource() instanceof ColorSelector)
 		{
 			colorSelectorChanged((ColorSelector)event.getSource());
 		}
 		else if (event.getSource() instanceof FontSelector)
 		{
 			fontSelectorChanged((FontSelector)event.getSource());
 		}
 	}
 	
 	protected void colorSelectorChanged(ColorSelector selector) {
 		
 	}
 	
 	protected void fontSelectorChanged(FontSelector selector) {
 		
 	}
 }
