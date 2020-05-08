 package jpod.gui;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.ArrayList;
 
 import javax.swing.*;
 
 import jpod.JPod;
 import jpod.gui.widgets.BaseWidget;
 import jpod.gui.widgets.BasicSettings;
 import jpod.gui.widgets.EffectSettings;
 import line6.*;
 import line6.commands.ChangeParameterCommand;
 import line6.commands.GetPresetCommand;
 import line6.commands.Parameter;
 /**
  * @author Mateusz Szygenda
  *
  */
 
 import line6.commands.values.AmpModel;
 import line6.commands.values.Effect;
 
 
 public class MainWindow extends javax.swing.JFrame {
 	private JComboBox devicesCB;
 	private Device activeDevice;
 	private ArrayList<BaseWidget> widgets;
 	
 	public MainWindow()
 	{
 		super("JPod");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
 		activeDevice = null;
 		widgets = new ArrayList<BaseWidget>();
 		
 		BasicSettings basicSettingsWidget = new BasicSettings(activeDevice);
 		EffectSettings effectSettingsWidget = new EffectSettings(activeDevice);
 		
 		widgets.add(basicSettingsWidget);
 		widgets.add(effectSettingsWidget);
 		
 		//pre configuration
 	
 		//Creating widgets
 		devicesCB = new JComboBox();
 		
 		//Adding widgets to the window
 		add(devicesCB);
 		add(basicSettingsWidget);
 		add(effectSettingsWidget);
 		
 		//post configuration
 		
 		devicesCB.addItemListener(new DeviceChangedEvent());
 		refreshDevicesCombobox();
 		
 		pack();
 	}
 	
 	protected void raiseActiveDeviceChanged()
 	{
 		for(BaseWidget widget : widgets)
 		{
 			widget.setActiveDevice(activeDevice);
 		}
		activeDevice.synchronize();		
 	}
 	
 	protected void reset()
 	{
 		
 	}
 	
 	protected void refreshDevicesCombobox()
 	{
 		for(int i = 0, count = JPod.devices.size(); i < count; i++)
 		{
 			devicesCB.addItem(JPod.devices.get(i));
 		}
 		activeDevice = (Device)devicesCB.getSelectedItem();
 		raiseActiveDeviceChanged();
 	}
 	
 	class DeviceChangedEvent implements ItemListener
 	{
 		public void itemStateChanged(ItemEvent e)
 		{
 			Device dev = (Device)e.getItem();
 			if(dev != null)
 			{
 				activeDevice = dev;
 				reset();
 			}
 		}
 	}
 }
