 // The Grinder
 // Copyright (C) 2000, 2001  Paco Gomez
 // Copyright (C) 2000, 2001  Philip Aston
 
 // This program is free software; you can redistribute it and/or
 // modify it under the terms of the GNU General Public License
 // as published by the Free Software Foundation; either version 2
 // of the License, or (at your option) any later version.
 
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 
 // You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 
 package net.grinder.console.swingui;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import net.grinder.console.model.ConsoleProperties;
 
 
 /**
  * @author Philip Aston
  * @version $Revision$
  */
class SamplingControlPanel extends JPanel
 {
     private final JSlider m_intervalSlider  = new JSlider(100, 10000, 100);
     private final IntegerField m_collectSampleField =
 	new IntegerField(0, 999999);
     private final IntegerField m_ignoreSampleField =
 	new IntegerField(0, 999999);
 
     private final String m_sampleIntervalString;
     private final String m_ignoreSampleZeroString;
     private final String m_ignoreSampleString;
     private final String m_collectSampleZeroString;
     private final String m_collectSampleString;
     private final String m_msUnit;
     private final String m_msUnits;
     private final String m_sampleUnit;
     private final String m_sampleUnits;
 
     public SamplingControlPanel(Resources resources)
     {
 	m_sampleIntervalString =
 	    resources.getString("sampleInterval.label") + " ";
 
 	m_ignoreSampleZeroString =
 	    resources.getString("ignoreCountZero.label", false);
 	m_ignoreSampleString = resources.getString("ignoreCount.label") + " ";
 
 	m_collectSampleZeroString =
 	    resources.getString("collectCountZero.label", false);
 	m_collectSampleString =
 	    resources.getString("collectCount.label") + " ";
 
 	m_msUnit = " " + resources.getString("ms.unit");
 	m_msUnits = " " + resources.getString("ms.units");
 	m_sampleUnit = " " + resources.getString("sample.unit");
 	m_sampleUnits = " " + resources.getString("sample.units");
 
 	m_intervalSlider.setMajorTickSpacing(1000);
 	m_intervalSlider.setMinorTickSpacing(100);
 	m_intervalSlider.setPaintTicks(true);
 	m_intervalSlider.setSnapToTicks(true);
 
 	final JLabel intervalLabel = new JLabel();
 
 	m_intervalSlider.addChangeListener(
 	    new ChangeListener() {
 		    public void stateChanged(ChangeEvent e) {
 			setIntervalLabelText(intervalLabel, 
 					     m_intervalSlider.getValue());
 			doUpdate();
 		    }
 		}
 	    );
 
 	final JLabel ignoreSampleLabel = new JLabel();
 
 	m_ignoreSampleField.addChangeListener(
 	    new ChangeListener() {
 		    public void stateChanged(ChangeEvent e) {
 			setIgnoreSampleLabelText(
 			    ignoreSampleLabel, m_ignoreSampleField.getValue());
 			doUpdate();
 		    }
 		}
 	    );
 
 	final JLabel collectSampleLabel = new JLabel();
 
 	m_collectSampleField.addChangeListener(
 	    new ChangeListener() {
 		    public void stateChanged(ChangeEvent e) {
 			setCollectSampleLabelText(
 			    collectSampleLabel,
 			    m_collectSampleField.getValue());
 			doUpdate();
 		    }
 		}
 	    );
 
 	final JPanel textFieldLabelPanel = new JPanel(new GridLayout(0, 1));
 	textFieldLabelPanel.add(ignoreSampleLabel);
 	textFieldLabelPanel.add(collectSampleLabel);
 
 	final JPanel textFieldControlPanel = new JPanel(new GridLayout(0, 1));
 	textFieldControlPanel.add(m_ignoreSampleField);
 	textFieldControlPanel.add(m_collectSampleField);
 
 	final JPanel textFieldPanel = new JPanel(new BorderLayout());
 	textFieldPanel.add(textFieldLabelPanel, BorderLayout.CENTER);
 	textFieldPanel.add(textFieldControlPanel, BorderLayout.EAST);
 
 	setLayout(new GridLayout(0, 1));
 	add(intervalLabel);
 	add(m_intervalSlider);
 	add(textFieldPanel);
     }
 
     public void set(ConsoleProperties properties)
     {
 	m_intervalSlider.setValue(properties.getSampleInterval());
 	m_ignoreSampleField.setValue(properties.getIgnoreSampleCount());
 	m_collectSampleField.setValue(properties.getCollectSampleCount());
     }
 
    public void get(ConsoleProperties properties)
    {
	properties.setSampleInterval(m_intervalSlider.getValue());
	properties.setIgnoreSampleCount(m_ignoreSampleField.getValue());
	properties.setCollectSampleCount(m_collectSampleField.getValue());
    }

     protected void update(int sampleInterval, int ignoreSampleCount,
 			  int collectSampleCount)
     {
     }
 
     private void doUpdate()
     {
 	update(m_intervalSlider.getValue(), m_ignoreSampleField.getValue(),
 	       m_collectSampleField.getValue());
     }
     
     private void setIntervalLabelText(JLabel label, int sampleInterval)
     {
 	label.setText(m_sampleIntervalString + sampleInterval +
 		      (sampleInterval == 1 ? m_msUnit : m_msUnits));
     }
 
     private void setIgnoreSampleLabelText(JLabel label, int ignoreSample)
     {
 	if (ignoreSample == 0 && m_ignoreSampleZeroString != null) {
 	    label.setText(m_ignoreSampleZeroString);
 	}
 	else {
 	    label.setText(m_ignoreSampleString + ignoreSample +
 			  (ignoreSample == 1 ? m_sampleUnit : m_sampleUnits));
 	}
     }
 
     private void setCollectSampleLabelText(JLabel label, int collectSample)
     {
 	if (collectSample == 0 && m_collectSampleZeroString != null) {
 	    label.setText(m_collectSampleZeroString);
 	}
 	else {
 	    label.setText(m_collectSampleString + collectSample +
 			  (collectSample == 1 ? m_sampleUnit : m_sampleUnits));
 	}
     }
 }
 
