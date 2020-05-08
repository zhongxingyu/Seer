 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.hobsoft.symmetry.ui.swing;
 
 import java.beans.PropertyChangeEvent;
 
import javax.swing.AbstractButton;
 import javax.swing.JToggleButton;
 
 import org.hobsoft.symmetry.PeerManager;
 import org.hobsoft.symmetry.ui.ToggleButton;
 
 /**
  * 
  * 
  * @author Mark Hobson
  */
 public class SwingToggleButtonPeer extends SwingButtonPeer
 {
 	// constructors -----------------------------------------------------------
 	
 	public SwingToggleButtonPeer(PeerManager peerManager)
 	{
 		super(peerManager);
 	}
 	
 	// PeerHandler methods ----------------------------------------------------
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Object createPeer(Object component)
 	{
 		return new JToggleButton();
 	}
 	
 	// PropertyChangeListener methods -----------------------------------------
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void propertyChange(PropertyChangeEvent event)
 	{
 		Object component = event.getSource();
 		String name = event.getPropertyName();
 //		Object oldValue = event.getOldValue();
 		Object newValue = event.getNewValue();
		AbstractButton button = (AbstractButton) getPeerManager().getPeer(component);
 		
 		if (ToggleButton.SELECTED_PROPERTY.equals(name))
 		{
 			button.setSelected(((Boolean) newValue).booleanValue());
 		}
 		else
 		{
 			super.propertyChange(event);
 		}
 	}
 }
