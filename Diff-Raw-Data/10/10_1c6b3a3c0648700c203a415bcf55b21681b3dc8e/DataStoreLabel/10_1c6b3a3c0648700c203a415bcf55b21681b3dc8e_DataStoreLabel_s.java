 /*
  *  Copyright (C) 2004  The Concord Consortium, Inc.,
  *  10 Concord Crossing, Concord, MA 01742
  *
  *  Web Site: http://www.concord.org
  *  Email: info@concord.org
  *
  *  This library is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public
  *  License as published by the Free Software Foundation; either
  *  version 2.1 of the License, or (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this library; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * END LICENSE */
 
 /*
  * Created on Jan 26, 2005
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package org.concord.data.ui;
 
 import javax.swing.JTextField;
 
 import org.concord.framework.data.stream.DataChannelDescription;
 import org.concord.framework.data.stream.DataStore;
 import org.concord.framework.data.stream.DataStoreEvent;
 import org.concord.framework.data.stream.DataStoreListener;
 
 /**
  * @author scott
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class DataStoreLabel extends JTextField
 	implements DataStoreListener
 {
 	/**
 	 * Not intended to be serialized, added to remove compile warning.
 	 */
 	private static final long serialVersionUID = 1L;
 
 	DataStore dataStore = null;
 	int channel = 0;
     private float currentValue;
 	
 	public DataStoreLabel(DataStore dataStore, int channel)
 	{
 		this.dataStore = dataStore;
 		this.channel = channel;
 		dataStore.addDataStoreListener(this);
 		setEditable(false);
 		updateValue();
 	}
 
 	public float getValue()
 	{
 	    return currentValue;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.concord.framework.data.stream.DataStoreListener#dataAdded(org.concord.framework.data.stream.DataStoreEvent)
 	 */
 	public void dataAdded(DataStoreEvent evt) 
 	{
 		updateValue();
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.concord.framework.data.stream.DataStoreListener#dataChanged(org.concord.framework.data.stream.DataStoreEvent)
 	 */
 	public void dataChanged(DataStoreEvent evt) 
 	{
 		updateValue();
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.concord.framework.data.stream.DataStoreListener#dataChannelDescChanged(org.concord.framework.data.stream.DataStoreEvent)
 	 */
 	public void dataChannelDescChanged(DataStoreEvent evt) 
 	{
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.concord.framework.data.stream.DataStoreListener#dataRemoved(org.concord.framework.data.stream.DataStoreEvent)
 	 */
 	public void dataRemoved(DataStoreEvent evt) 
 	{
 	}
 	
 	public void dispose()
 	{
 		dataStore.removeDataStoreListener(this);
 	}
 
 	private void updateValue()
 	{
 		int numSamples = dataStore.getTotalNumSamples();
 		if(numSamples == 0) return;
 				
 		Float lastValue = 
 			(Float)dataStore.getValueAt(numSamples-1, channel);
 
 		if(lastValue == null) {
 		    System.err.println("DataStoreLabel: null last value");
 		    return;
 		}
 		setValue(lastValue.floatValue());
 	}
 	
 	/**
 	 * Sets the value of the label. 
 	 * Pays attention to the properties of the channel that is being displayed
 	 * (channel description), specially the precision
 	 * @param val	float value to display
 	 */
 	private void setValue(float val)
 	{
 	    
 		DataChannelDescription channelDesc = 
 			dataStore.getDataChannelDescription(channel);
 		
 		//If the channel has a description, display the value with the correct precision
 		if (channelDesc != null){
 			if (channelDesc.isUsePrecision()){
 				double precision = Math.pow(10, channelDesc.getPrecision());
 				val = (float)(Math.floor((val / (precision)) + 0.5) * precision);
 			}
 		}
 
 		currentValue = val;
 
 		setText(Float.toString(val));
 	}
 	
 }
