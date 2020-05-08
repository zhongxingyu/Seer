 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.colar.netbeans.fan.wizard;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import org.netbeans.spi.options.AdvancedOption;
 import org.netbeans.spi.options.OptionsPanelController;
 
 /**
  * AdvancedOption panel provider fro Fan
  * @author tcolar
  */
 public class FanGlobalSettings extends AdvancedOption
 {
 
 	private static FanGlobalSettings instance;
 	private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
 
 	private FanGlobalSettings()
 	{
 		super();
 	}
 
 	public static synchronized FanGlobalSettings getInstance()
 	{
 		if (instance == null)
 		{
 			instance = new FanGlobalSettings();
 		}
 		return instance;
 	}
 
 	public void addPropertyChangeListener(PropertyChangeListener listener)
 	{
 		propertyChangeSupport.addPropertyChangeListener(listener);
 	}
 
 	public void removePropertyChangeListener(PropertyChangeListener listener)
 	{
 		propertyChangeSupport.removePropertyChangeListener(listener);
 	}
 
 	public String getDisplayName()
 	{
 		return "Fantom options";
 	}
 
 	public String getTooltip()
 	{
 		return "Fantom options";
 	}
 
 	public OptionsPanelController create()
 	{
 		return new FanGlobalSettingsController();
 	}
 }
 
