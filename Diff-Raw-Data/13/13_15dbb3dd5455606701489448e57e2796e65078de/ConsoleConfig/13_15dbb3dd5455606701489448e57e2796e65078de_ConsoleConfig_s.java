 package pt.up.fe.dceg.accu.console;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.Constructor;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 import org.dom4j.io.OutputFormat;
 import org.dom4j.io.SAXReader;
 import org.dom4j.io.XMLWriter;
 
 import pt.up.fe.dceg.accu.AboutPanel;
 import pt.up.fe.dceg.accu.BeaconCfgPanel;
 import pt.up.fe.dceg.accu.EntityListPanel;
 import pt.up.fe.dceg.accu.FinderPanel;
 import pt.up.fe.dceg.accu.MapPanel;
 import pt.up.fe.dceg.accu.PlanStatePanel;
 import pt.up.fe.dceg.accu.PreferenceAction;
 import pt.up.fe.dceg.accu.QuitCommand;
 import pt.up.fe.dceg.accu.StateViewPanel;
 import pt.up.fe.dceg.accu.SystemListPanel;
 import pt.up.fe.dceg.accu.TeleOpPanel;
 import pt.up.fe.dceg.accu.TestPanel;
 import pt.up.fe.dceg.accu.TtsTestPanel;
 import pt.up.fe.dceg.accu.panel.AccuBaseAction;
 import pt.up.fe.dceg.accu.panel.AccuPanelContainer;
 import android.content.Context;
 import android.util.Log;
 
 /**
  * Console Configuration container
  * Responsible for reading/updating XML configuration file and PanelContainer
  * @author jqcorreia
  *
  */
 
 public class ConsoleConfig 
 {
 	private static final String TAG = "ConsoleConfig";
 	private static final String pluginListFileName = "plugin.list";
 	Document doc;
 	InputStream is;
 	AccuPanelContainer container;
 	String fileName;
 	Context context;
 	
 	public ArrayList<PanelConfigItem> panelList = new ArrayList<PanelConfigItem>();
 	public ConsoleConfig()
 	{
 		
 	}
 	public ConsoleConfig(File f)
 	{
 		try {
 			is = new FileInputStream(f);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		initialize();
 	}
 	public ConsoleConfig(Context context, String file, AccuPanelContainer container)
 	{
 		fileName = file;
 		this.container = container;
 		this.context = context;
 		
 		try {
 			is = context.openFileInput(file);
 			initialize();
 		} catch (FileNotFoundException e) {
 			Log.e(TAG,"Config file " + file + " not found using default config");
 			useDefaultConfig();
 		}
 	}
 	
 	/**
 	 * Method to read the plugin list and generate instances of PanelConfigItem
 	 */
 	public void readPluginList()
 	{
 		try {
 			BufferedReader br = new BufferedReader(new InputStreamReader(context.getAssets().open(pluginListFileName)));
 			
 			String line = br.readLine();
 			while(line != null)
 			{
 				line = line.replace('/', '.');
 				line = line.substring(2,line.length()-5);
 				panelList.add(new PanelConfigItem(Class.forName(line),false, 100));
 				line = br.readLine();
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public void initialize()
 	{
 		// Read plugin.list from assets directory and populate possible action/panel list
 		readPluginList();
 		// Do this to prevent erroneous configurations
 		if(readConfiguration())
 		{
 			// Parse all the action present in config file and add them to an actionList
 			parseActions();
 		}
 		else
 			useDefaultConfig();
 	}
 	
 	@SuppressWarnings("rawtypes")
 	public void initPanels()
 	{
 		container.clear();
 		for (PanelConfigItem pci : panelList) {
 			try {
 				if(pci.isActive())
 				{
 					Class c = pci.panelClass;
 					Class[] types = { Context.class };
 					Constructor cons = c.getConstructor(types);
 					container.addAction((AccuBaseAction) cons
 							.newInstance(context));
 				}
 			} catch (Exception e) {
 				// Deal with just 1 generic exception instead of 7(!?)
 				// Readable code
 				e.printStackTrace();
 			}
 		}
 		
 		// Default end-of-list actions
 		container.addAction(new ConsoleConfigurator(context)); 
 		container.addAction(new AboutPanel(context)); 
 		container.addAction(new QuitCommand(context));
 		container.getSelector().adapter.notifyDataSetChanged();
 		container.openPanel(0); // For now open the first panel on the list        
 	}
 	
 	public boolean readConfiguration()
 	{  
		SAXReader reader = new SAXReader();
 		try {
 			doc = reader.read(is);
 			
 		} catch (DocumentException e) {
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 	
 	@SuppressWarnings("rawtypes")
 	void parseActions()
 	{
 		Element root = doc.getRootElement();
 		for(Iterator i = root.elementIterator("action"); i.hasNext();)
 		{
 			Element el = (Element) i.next();
 			String packageName = el.attributeValue("package");
 			boolean active = Boolean.parseBoolean(el.attributeValue("active"));
 			int order = Integer.parseInt(el.attributeValue("order"));
 			
 			try {
 				for(PanelConfigItem pci : panelList)
 				{
 					if(Class.forName(packageName).getSimpleName().equals(pci.panelClass.getSimpleName()))
 					{
 						pci.active = active;
 						pci.order = order;
 						break;
 					}
 				}
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			}
 		}
 		Collections.sort(panelList);
 	}
 	
     void useDefaultConfig()
     {
     	panelList.clear();
         panelList.add(new PanelConfigItem(SystemListPanel.class,true,1));
         panelList.add(new PanelConfigItem(StateViewPanel.class,true,2));
         panelList.add(new PanelConfigItem(EntityListPanel.class,true,3));
         panelList.add(new PanelConfigItem(PlanStatePanel.class,true,4));
         panelList.add(new PanelConfigItem(MapPanel.class,true,5));
         panelList.add(new PanelConfigItem(TeleOpPanel.class,true,6));
         panelList.add(new PanelConfigItem(FinderPanel.class,true,8));
         panelList.add(new PanelConfigItem(BeaconCfgPanel.class,true,9));
         panelList.add(new PanelConfigItem(PreferenceAction.class,true,10));
         panelList.add(new PanelConfigItem(TestPanel.class,true,11));
         panelList.add(new PanelConfigItem(TtsTestPanel.class,true,12));        
     }
     
 	/**
 	 * Method that updates the XML configuration file as well the main application Panel Container associated
 	 * @param newPanelList
 	 */
 	public void updateConfig()
 	{
 		// Save new Configuration to config.xml
 		try {
 			FileOutputStream fos = context.openFileOutput("config.xml", Context.MODE_PRIVATE);
 			XMLWriter write = new XMLWriter(fos, OutputFormat.createPrettyPrint());
 			
 			Document newDoc = DocumentHelper.createDocument();
 			Element root = newDoc.addElement("actions");
 			int c = 0;
 			for(PanelConfigItem pci : panelList)
 			{
 				root.addElement("action")
 					.addAttribute("package", pci.panelClass.getName())
 					.addAttribute("active", String.valueOf(pci.isActive()))
 					.addAttribute("order", String.valueOf(c++));
 			}
 			write.write(newDoc);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		// Reinitialize the panels based on new configuration (for now restarts all panels)
 		initPanels();
 	}
 	
 }
