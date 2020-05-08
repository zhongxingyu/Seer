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
 
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 
 import org.hobsoft.symmetry.AbstractPeerHandler;
 import org.hobsoft.symmetry.PeerManager;
 import org.hobsoft.symmetry.ui.Button;
 import org.hobsoft.symmetry.ui.Image;
 import org.hobsoft.symmetry.ui.event.ActionListener;
 import org.hobsoft.symmetry.ui.swing.event.ActionListenerAdapter;
 
 /**
  * 
  * 
  * @author Mark Hobson
  */
 public class SwingButtonPeer extends AbstractPeerHandler
 {
 	// constructors -----------------------------------------------------------
 	
 	public SwingButtonPeer(PeerManager peerManager)
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
 		return new JButton();
 	}
 	
 	// PropertyChangeListener methods -----------------------------------------
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void propertyChange(PropertyChangeEvent event)
 	{
 		Button component = (Button) event.getSource();
 		String name = event.getPropertyName();
 		Object oldValue = event.getOldValue();
 		Object newValue = event.getNewValue();
		JButton peer = (JButton) getPeerManager().getPeer(component);
 		
 		if (Button.ACTION_LISTENERS_PROPERTY.equals(name))
 		{
 			for (java.awt.event.ActionListener oldListener : peer.getActionListeners())
 			{
 				peer.removeActionListener(oldListener);
 			}
 			
 			for (ActionListener newListener : (ActionListener[]) newValue)
 			{
 				peer.addActionListener(new ActionListenerAdapter(component, newListener));
 			}
 		}
 		else if (Button.IMAGE_PROPERTY.equals(name))
 		{
 			Icon icon = (newValue != null) ? new ImageIcon(((Image) newValue).getData()) : null;
 			
 			peer.setIcon(icon);
 		}
 		else if (Button.ENABLED_PROPERTY.equals(name))
 		{
 			peer.setEnabled(((Boolean) newValue).booleanValue());
 		}
 		else if (Button.MNEMONIC_PROPERTY.equals(name))
 		{
 			peer.setMnemonic(((Integer) newValue).intValue());
 		}
 		else if (Button.TEXT_PROPERTY.equals(name))
 		{
 			peer.setText((String) newValue);
 		}
 		else if (Button.TOOL_TIP_PROPERTY.equals(name))
 		{
 			peer.setToolTipText((String) newValue);
 		}
 	}
 }
