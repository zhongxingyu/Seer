 package editor.abilities;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import linewars.configfilehandler.ConfigData;
 import linewars.configfilehandler.ConfigData.NoSuchKeyException;
 import linewars.configfilehandler.ParserKeys;
 import editor.BigFrameworkGuy;
 import editor.ConfigurationEditor;
 import editor.URISelector;
 import editor.URISelector.SelectorOptions;
 
 /**
  * 
  * @author Connor Schenck
  *
  */
 public class AbilityEditor extends JPanel implements ConfigurationEditor, SelectorOptions {
 
 	private URISelector abilityType;
 	
 	private JPanel subPanel;
 	private URISelector uriSelector;
 	private JTextField numberBox;
 	private BigFrameworkGuy bfg;
 	private int selectedAbility = -1;
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -8201026782173643981L;
 	
 	
 	
 	public AbilityEditor(BigFrameworkGuy guy) {
 		this.setLayout(new BorderLayout());
 		this.setPreferredSize(new Dimension(800, 600));
 		abilityType = new URISelector("Ability Type", this); 
 		this.add(abilityType, BorderLayout.NORTH);
 		subPanel = new JPanel();
 		this.add(subPanel, BorderLayout.CENTER);
 		bfg = guy;
 	}
 
 	@Override
 	public void setData(ConfigData cd) {
 		setData(cd, false);
 	}
 
 	@Override
 	public void forceSetData(ConfigData cd) {
 		setData(cd, true);
 	}
 	
 	private void setData(ConfigData cd, boolean force)
 	{
 		String type = null;
 		try {
 			type = cd.getString(ParserKeys.type);
 		} catch(NoSuchKeyException e) {
 			if(force)
 				return;
 			else
 				throw new IllegalArgumentException("This config data object does not specify a type");
 		}
 		
 		abilityType.setSelectedURI(type);
 		
 		if(type.equals("ConstructUnit"))
 			selectedAbility = 0;
 		else if(type.equals("ResearchTech"))
 			selectedAbility = 1;
 		else if(type.equals("Shoot"))
 			selectedAbility = 2;
 		else if(type.equals("GenerateStuff"))
 			selectedAbility = 3;
 		else
 		{
 			if(force)
 				return;
 			else
 				throw new IllegalArgumentException("The type in this config data object is not valid");
 		}
 		
 		this.uriSelected(this.getOptions()[selectedAbility]);
 		
 		ParserKeys uri = null;
 		ParserKeys number = null;
 		if(selectedAbility == 0)
 		{
 			uri = ParserKeys.unitURI;
 			number = ParserKeys.buildTime;
 		}
 		if(selectedAbility == 1)
 		{
 			uri = ParserKeys.techURI;
 		}
 		if(selectedAbility == 2)
 		{
 			uri = ParserKeys.projectileURI;
 			number = ParserKeys.range;
 		}
 		if(selectedAbility == 3)
 		{
 			number = ParserKeys.stuffIncome;
 		}
 		
 		if(numberBox != null)
 			try {
 				numberBox.setText(cd.getString(number));
 			} catch (NoSuchKeyException e) {
 				if(!force)
 					throw new IllegalArgumentException(number + " isn't specified in this config data object");
 			}
 		if(uriSelector != null)
 			try {
 				uriSelector.setSelectedURI(cd.getString(uri));
 			} catch(NoSuchKeyException e) {
 				if(!force)
 					throw new IllegalArgumentException(uri + " isn't specified in this config data object");
 			}
 	}
 
 	@Override
 	public void reset() {
 		abilityType.setSelectedURI("");
 		subPanel.removeAll();
 		uriSelector = null;
 		numberBox = null;
 		selectedAbility = -1;
 	}
 
 	@Override
 	public ConfigData getData() {
 		ConfigData cd = new ConfigData();
 		if(selectedAbility < 0)
 			return cd;
 		
 		String type = null;
 		ParserKeys uri = null;
 		ParserKeys number = null;
 		if(selectedAbility == 0)
 		{
 			type = "ConstructUnit";
 			uri = ParserKeys.unitURI;
 			number = ParserKeys.buildTime;
 		}
 		if(selectedAbility == 1)
 		{
 			type = "ResearchTech";
 			uri = ParserKeys.techURI;
 		}
 		if(selectedAbility == 2)
 		{
 			type = "Shoot";
 			uri = ParserKeys.projectileURI;
 			number = ParserKeys.range;
 		}
 		if(selectedAbility == 3)
 		{
 			type = "GenerateStuff";
 			number = ParserKeys.stuffIncome;
 		}
 		
 		cd.set(ParserKeys.type, type);
 		
 		if(uri != null)
 			cd.set(uri, uriSelector.getSelectedURI());
 		if(number != null)
 		{
 			try {
 				cd.set(number, Double.valueOf(numberBox.getText()));
 			} catch(NumberFormatException e) {
 				
 			}
 		}
 		
 		return cd;
 		
 	}
 
 	@Override
 	public boolean isValidConfig() {
 		if(selectedAbility < 0)
 			return false;
 		if(uriSelector != null && (uriSelector.getSelectedURI() == null || uriSelector.getSelectedURI().equals("")))
 			return false;
 		if(numberBox != null)
 		{
 			try {
 				Double.valueOf(numberBox.getText());
 			} catch(NumberFormatException e) {
 				return false;
 			}
 		}
 		
 		return true;
 	}
 
 	@Override
 	public ParserKeys getType() {
 		return ParserKeys.abilityURI;
 	}
 
 	@Override
 	public JPanel getPanel() {
 		return this;
 	}
 
 	@Override
 	public String[] getOptions() {
 		return new String[]{"Construct Unit",
 							"Research Tech",
 							"Shoot",
 							"Generate Stuff"};
 	}
 
 	@Override
 	public void uriSelected(String uri) {
 		subPanel.removeAll();
 		for(int j = 0; j < this.getOptions().length; j++)
 			if(uri.equals(this.getOptions()[j]))
 				selectedAbility = j;
 		
 		String label = null;
 		SelectorOptions so = null;
 		String numberLabel = null;
 		if(selectedAbility == 0)
 		{
 			label = "Unit";
 			so = new UnitSelector();
 			numberLabel = "Build Time (ms)";
 		}
 		if(selectedAbility == 1)
 		{
 			label = "Tech";
 			so = new TechSelector();
 		}
 		if(selectedAbility == 2)
 		{
 			so = new ProjectileSelector();
 			label = "Projectile";
 			numberLabel = "Range";
 		}
 		if(selectedAbility == 3)
 			numberLabel = "Stuff per second";
 		
 		if(so != null)
 		{
 			uriSelector = new URISelector(label, so);
 			subPanel.add(uriSelector);
 		}
 		else
		{
			subPanel.remove(uriSelector);
 			uriSelector = null;
		}
 		
 		if(numberLabel != null)
 		{
 			subPanel.add(new JLabel(numberLabel));
 			numberBox = new JTextField();
 			numberBox.setColumns(10);
 			subPanel.add(numberBox);
 		}
 		else
		{
			subPanel.remove(numberBox);
 			numberBox = null;
		}
 		
 		this.validate();
		this.updateUI();
 	}
 	
 	private class UnitSelector implements SelectorOptions {
 
 		@Override
 		public String[] getOptions() {
 			return bfg.getUnitURIs();
 		}
 		@Override
 		public void uriSelected(String uri) {}
 	}
 	
 	private class TechSelector implements SelectorOptions {
 
 		@Override
 		public String[] getOptions() {
 			return bfg.getTechURIs();
 		}
 		@Override
 		public void uriSelected(String uri) {}
 	}
 	
 	private class ProjectileSelector implements SelectorOptions {
 
 		@Override
 		public String[] getOptions() {
 			return bfg.getProjectileURIs();
 		}
 		@Override
 		public void uriSelected(String uri) {}
 	}
 
 }
