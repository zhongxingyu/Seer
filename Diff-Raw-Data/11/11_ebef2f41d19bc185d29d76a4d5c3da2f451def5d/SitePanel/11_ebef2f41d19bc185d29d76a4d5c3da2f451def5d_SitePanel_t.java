 package panels;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Vector;
 
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.RepaintManager;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 
 import textOnlyPanels.TextOnlyPanel;
 import windows.DrillWindow;
 import windows.TestWindow;
 import windows.WindowSupportFunctions;
 import calculations.BalanceCalculator;
 import dataInterfaces.Boring;
 import dataInterfaces.DataFactory;
 import dataInterfaces.Site;
 import dataInterfaces.WaterTable;
 import dataInterfaces.WorkingSite;
 import flatFileData.BoringFF;
 import flatFileData.Saving;
 import flatFileData.SiteFF;
 import flatFileData.WaterTableFF;
 
 /**
  * Creates the panel that holds the information for each site, boring logs, test panels, or other
  * pertinant information for a particular site.
  * @author Team Recharge
  *
  */
  
 public class SitePanel extends JPanel implements Serializable{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private ArrayList<BoringLogDisplay> listOfBoringLogDisplays;
 	private ArrayList<GeoTestPanel> listOfGeoTestPanels;
 	private static ArrayList<SitePanel> sitePanelList = new ArrayList<SitePanel>();
 	private int numBores;
 	private JTabbedPane boringTabbedPane;
 	public static enum SiteType {GEOTECH, HYDRO}
 	private SiteType theSiteType;
 	private String siteName;
 	private Site theSite;
 	private boolean boringsLoaded;
 	
 	
 	//private WorkingSite theWorkingSite;
 	private int balance;
 	private static ArrayList<JTabbedPane> paneList = new ArrayList<JTabbedPane>();
 	//private PersistFF persist;
 	public static boolean initialized = false;
 	
 	public Site getTheSite() {
 		return theSite;
 	}
 //	public static SitePanel getSitePanel(String siteName, int id, SiteType siteType){
 //		System.out.println("get site Panel called");
 ////		System.out.println("siteName: " + siteName + " id: " + id + " siteType " + siteType.toString());
 //		for(SitePanel sitePanel : sitePanelList){
 //			System.out.println(sitePanel.siteName);
 //			if(sitePanel.siteName.equals(siteName)){
 //				System.out.println("same site name");
 //			}
 //			if(sitePanel.theSiteType == siteType){
 //				System.out.println("same siteType");
 //			}
 //			if( sitePanel.theSite.getId() == id){
 //				System.out.println("same id");
 //					return sitePanel;
 //			}		
 //			
 //		}
 //		return null;
 //	}
 	public SitePanel(String nodeInfo, SiteType typeOfSite) {
 		//TODO this needs to be updated
 		//theSite = DataFactory.newDemoSite(nodeInfo.substring(0, nodeInfo.length()-2), Integer.parseInt(nodeInfo.substring(nodeInfo.length()-1, nodeInfo.length())), typeOfSite);
 
 		//TODO Create a working site
 		//numBores = WorkingSite.getNumberOfBoring();
 		//Adds the buttons to the button panel for hydro sites
 		if(typeOfSite == SiteType.GEOTECH){
 			//XXX This is a hack to get it working this should be done better soon
 			ButtonPanel.getInstance().addGeotechSiteButtons(this.balance);
 			if(!initialized){
 				initialized = true;
 			}
 		}
 		else{
 			//XXX This is a hack to get it working this should be done better soon 
 			ButtonPanel.getInstance().addHydroSiteButtons(this.balance);
 			if(!initialized){
 				initialized = true;
 			}
 		}
 		
 		
 		theSite = DataFactory.newDemoSite(nodeInfo.substring(0, nodeInfo.length()-2), Integer.parseInt(nodeInfo.substring(nodeInfo.length()-1, nodeInfo.length())), typeOfSite);
 //		System.out.println("nodeInfo: " + nodeInfo);
 		
 		
 		
 		balance = BalanceCalculator.getBalanceAsInt();
 //		System.out.println("balance after we initialize:" + balance);
 		
 		siteName = nodeInfo;
 		//System.out.println(siteName);
 		theSiteType = typeOfSite;
 		
 		
 		setLayout(new GridLayout(1,1));
 		listOfBoringLogDisplays = new ArrayList<BoringLogDisplay>();
 		listOfGeoTestPanels = new ArrayList<GeoTestPanel>();
 		
 		//TODO Query database for balance
 		
 		
 		ButtonPanel.disableBoringLogButtons();
 		
 		boringTabbedPane = new JTabbedPane();
 
 		TextOnlyPanel siteInfo = new TextOnlyPanel(theSite.getDescription());
 		boringTabbedPane.addTab("Welcome", null, siteInfo);
 		boringTabbedPane.setMinimumSize(new Dimension(1,1));
 		boringTabbedPane.addChangeListener(new TabbedWindowListener());
 		boringTabbedPane.addComponentListener(new ResizeListener());
 		add(boringTabbedPane);
 		sitePanelList.add(this);
 		//XXX this is where I will load the borings
 		Saving.loadBorings(this, typeOfSite.toString() + nodeInfo.replace(" ", ""));
 		//persist = new PersistFF(siteName, theSiteType);
 		
 	}
 	
 	public void start(){
 		/*for( BoringFF b: persist.getBorings()){
 			createNewBoringLog(b.getDepth(), b.getxPos(), b.getyPos(),false);
 		}*/
 		//ButtonPanel.disableBoringLogButtons();
 		//redraw();
 		
 		//initialized = true;
 	}
 	
 	public void updateBalance(int cost){
 //		System.out.println("balance: " + balance);
 		BalanceCalculator.setBalance(balance);
 		ButtonPanel.updateBalanceField(cost);
 		balance = BalanceCalculator.getBalanceAsInt();
 	}
 	
 	public int getBalance(){
 		return balance;
 	}
 
 	/**
 	 * Called in order to refresh buttonlist
 	 */
 	public void redraw(){
 		
 		
 		
 		
 		
 		
 		//2/9/13 MS
 		//setLayout(new GridLayout(1,1));
 		System.out.println(this.siteName);
 //		System.out.println("balance in SitePanel redraw: " + this.balance);
 		if(theSiteType == SiteType.GEOTECH)
 			ButtonPanel.getInstance().addGeotechSiteButtons(this.balance);
 		else
 			ButtonPanel.getInstance().addHydroSiteButtons(this.balance);
 		
 		
 		if(listOfBoringLogDisplays.size()<1 || initialized==false)
 			ButtonPanel.disableBoringLogButtons();
 		else 
 			ButtonPanel.enableBoringLogButtons();
 			
 		BalanceCalculator.setBalance(balance);
 		//ButtonPanel.updateBalanceField(0);
 		this.revalidate();
 		this.repaint();
 		
 	}
 
 
 	/**
 	 * Creates a new BorePanel with the given x and y coordinates and the given depth.
 	 * The panel that is created has tabs for both the Full Description and Test Results.
 	 * @param depth - the depth of the boring log
 	 * @param xCoord - x coordinate of soil data
 	 * @param yCoord - y coordinate of soil data
 	 * @throws TransformerException 
 	 */
 	//XXX MS THIS IS WHERE I NEED TO SAVE THE BORING
 	public void createNewBoringLog(int depth, int xCoord, int yCoord, boolean isInitialized, boolean save) throws TransformerException
 	{
 		System.out.println("createNewBoringLog CALLED");
 		numBores++;
 		
 		//TODO this is where the boring would get created
 		//Boring BI = DataFactory.newBoring(-1, xCoord, yCoord, depth, theSite.getEarthModel());
 		Boring BI = DataFactory.newBoring(-1, xCoord, yCoord, depth, theSite);
 		//Boring BI = DataFactory.newBoring();
 		BI.setBoringNumber(numBores);
 		//theWorkingSite.addBoring(BI);
 		if(save){
 			System.out.println("siteType: " + theSiteType.toString());
 			Saving.saveBoring(theSiteType.toString(), theSite.getName(), theSite.getId(), numBores, xCoord, yCoord, depth);
		} else {
			updateBalance(depth * DrillWindow.COST_PER_FOOT_OF_DRILLING);
 		}
 //		Saving.writeXML();
 		
 		
 		
 		BorePanel temp = new BorePanel(BI, theSite.getzScale(), siteName);
 		
 		JScrollPane newTab = new JScrollPane(temp);
 		
 		//newTab.setPreferredSize(RightSidePanel.getInstance().getSize());
 		//newTab.setMaximumSize(RightSidePanel.getInstance().getSize());
 		//TODO
 		/*
 		 * The vertical scrollbar won't allow complete viewing of a large boring log
 		 */
 		//newTab.getVerticalScrollBar().setUnitIncrement(BoringLogDisplay.VERTICAL_INCREMENT);
 		boringTabbedPane.addTab(("B" + numBores), null, newTab);
 		boringTabbedPane.setSelectedComponent(newTab);
	
 		validate();
 		repaint();
 	
 		/*
 		BorePanel temp = new BorePanel(BI, theSite.getzScale(), siteName);
 		boringTabbedPane.addTab(("B" + numBores), null, temp);
 		boringTabbedPane.setSelectedComponent(temp);
 		*/
 		//RightSidePanel.getInstance().createThePanelCorrespondingToNodeName(DefaultMutableTreeNode node);
 		/*
 		if (isInitialized)
 			persist.addBoring((BoringFF)BI);*/
 	}
 	
 	/**
 	 * Creates a new panel that contains tabs for the Full Description and Test Results.
 	 * @author Team Recharge
 	 *
 	 */
 	private class BorePanel extends JPanel
 	{
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 		
 		
 		//TODO
 		/*
 		 * When resizing the window, the borepanel is not being resized to fit the new window size
 		 */
 		private BoringLogDisplay bore;
 		
 		/**
 		 * Creates the Full Description and Test Results tabs at the given location and depth
 		 * @param depth - final depth of the boring log
 		 * @param xCoord - x coordinate of soil data
 		 * @param yCoord - y coordinate of soil data
 		 * @param depthIncrement - increment of depth of the boring log
 		 * @param siteName - name of the site that is being used to generate the boringLog
 		 */
 		BorePanel(Boring BI, int depthIncrement, String siteName)
 		{
 			super();
 			
 			bore = new BoringLogDisplay(BI, depthIncrement, siteName, theSite.getMSL(), theSiteType, theSite);
 			
 			bore.setBoringID("B" + BI.getBoringNumber());
 			listOfBoringLogDisplays.add(bore);
 			System.out.println("boring added to listOfBoringLogDisplays");
 			GeoTestPanel testInfo = new GeoTestPanel(bore, theSiteType);
 			listOfGeoTestPanels.add(testInfo);
 			JTabbedPane someTab = new JTabbedPane();
 			//MS 3/18/13 
 			BoringLogDisplay selectedBoring = listOfBoringLogDisplays.get(BI.getBoringNumber());
 			WaterTable geoWaterTable = new WaterTableFF(siteName, 0, BI.getDepth(), BI.getxPos(), BI.getyPos(), (SiteFF)theSite);
 			selectedBoring.setWaterTable(geoWaterTable);
 			
 			if(RightSidePanel.getInstance().getSize().getWidth()>=1000.0)
 				someTab.setPreferredSize(new Dimension(
 						(int)RightSidePanel.getInstance().getSize().getWidth()-18,
 						(int)RightSidePanel.getInstance().getSize().getHeight()-42));
 			else
 				someTab.setPreferredSize(new Dimension(1000,(int)RightSidePanel.getInstance().getSize().getHeight()-57));
 			
 			
 			someTab.addTab("Full Description", null, bore);
 			
 			
 			someTab.addTab("Test Results", null, testInfo);
 			add(someTab);
 			paneList.add(someTab);
 		}
 		
 	}
 
 	/**
 	 * If the site that is calling this method is a HydroGeological site, this will add a well to
 	 * the boring log. Will through an error if the site is a Geotechnical site.
 	 */
 	public void addWellToBoringLog(int boringIndex, int top, int bottom, int xPos, int yPos, boolean save) {
 		if(!save){
 			System.out.println("LOADING WELL boring index: " + boringIndex);
 		}
 		switch(theSiteType)
 		{
 		case GEOTECH:
 			/*
 			 * As per the way the program is setup, this method should never be
 			 * called for a GeoTech site, however it is needed as per the site interface.
 			 * Does print to the system if it was called.
 			 */
 			
 			System.out.println("addWellToBoringLog was called inside a Geotechnical Site.");
 			break;
 		case HYDRO:
 			//XXX #savewell MS
 			//System.out.println("Coords are  (" + xPos + "," + yPos + ")");
 			//WaterTable waterTable = new WaterTableFF(siteName);
 			BoringLogDisplay selectedBoring = listOfBoringLogDisplays.get(boringIndex);
 			
 			
 			if(selectedBoring.getHasInstalledWell() == true)
 			{
 				WindowSupportFunctions.createErrorMessage("Cannot Install Well" , "Only one well may be installed for each boring");
 			}
 			else
 			{
 				WaterTable waterTable = new WaterTableFF(siteName, top, bottom, xPos, yPos, (SiteFF)theSite);
 				selectedBoring.setWaterTable(waterTable);
 				selectedBoring.setWellTop(top);
 				selectedBoring.setWellBottom(bottom);
 				//selectedBoring.
 				selectedBoring.getParent().repaint();
 				selectedBoring.setHasInstalledWell(true);
 				System.out.println("selectedBoring.getSiteID: " + selectedBoring.getSiteID());
 				System.out.println("boringIndex: " + boringIndex);
				//15 is the cost per foot of screening
				
 				if(save){
 					Saving.saveWell("HYDRO" + selectedBoring.getSiteID().replace(" ", ""), top, bottom, (boringIndex + 1));
				} else {
					updateBalance((bottom - top) * 15);
 				}
 			}
 			break;
 		}
 	}
 	
 	public void performTestsOnBoringLog(int boringIndex, int tests[][])
 	{
 		GeoTestPanel selectedGeoTest = listOfGeoTestPanels.get(boringIndex);
 		selectedGeoTest.setTestsToPerform(tests);
 	}
 	
 	//TODO
 	/*
 	 * Add image of the site to the intro panel for each site
 	 */
 	
 	private class TabbedWindowListener implements ChangeListener
 	{
 		
 		@Override
 		public void stateChanged(ChangeEvent e) {
 			JTabbedPane pane = (JTabbedPane)e.getSource();
 			
 			//Closes the test window since it is specific to one
 			//boring and cant be changed currently
 			TestWindow.closeIfOpen(false);
 			
 			// Get current tab
 			int sel = pane.getSelectedIndex();
 			//The welcome tab will always be at index 0
 			//and thus needs to close the drill window if
 			//open
 			if(sel == 0)
 			{
 				ButtonPanel.disableBoringLogButtons();
 				DrillWindow.closeIfOpen(false);
 			}
 			else
 			{
 				//pane.setPreferredSize(RightSidePanel.getInstance().getSize());
 				ButtonPanel.enableBoringLogButtons();
 			}
 				
 
 		}
 	}
 
 	/**
 	 * This is meant to resize the panes with the window changes
 	 * @author Andrew Hill
 	 *
 	 */
 	private class ResizeListener implements ComponentListener
 	{
 
 		@Override
 		public void componentHidden(ComponentEvent arg0) {
 			for( JTabbedPane p : paneList){
 				if(RightSidePanel.getInstance().getSize().getWidth()>=1000.0)
 					p.setPreferredSize(new Dimension(
 							(int)RightSidePanel.getInstance().getSize().getWidth()-18,
 							(int)RightSidePanel.getInstance().getSize().getHeight()-42));
 				else
 					p.setPreferredSize(new Dimension(1000,(int)RightSidePanel.getInstance().getSize().getHeight()-57));
 				
 			}
 			
 		}
 
 		@Override
 		public void componentMoved(ComponentEvent arg0) {
 			for( JTabbedPane p : paneList){
 				if(RightSidePanel.getInstance().getSize().getWidth()>=1000.0)
 					p.setPreferredSize(new Dimension(
 							(int)RightSidePanel.getInstance().getSize().getWidth()-18,
 							(int)RightSidePanel.getInstance().getSize().getHeight()-42));
 				else
 					p.setPreferredSize(new Dimension(1000,(int)RightSidePanel.getInstance().getSize().getHeight()-57));
 				
 			}
 			
 		}
 
 		@Override
 		public void componentResized(ComponentEvent arg0) {
 			for( JTabbedPane p : paneList){
 				if(RightSidePanel.getInstance().getSize().getWidth()>=1000.0)
 					p.setPreferredSize(new Dimension(
 							(int)RightSidePanel.getInstance().getSize().getWidth()-18,
 							(int)RightSidePanel.getInstance().getSize().getHeight()-42));
 				else
 					p.setPreferredSize(new Dimension(1000,(int)RightSidePanel.getInstance().getSize().getHeight()-57));
 				
 			}
 			
 		}
 
 		@Override
 		public void componentShown(ComponentEvent arg0) {
 			for( JTabbedPane p : paneList){
 				if(RightSidePanel.getInstance().getSize().getWidth()>=1000.0)
 					p.setPreferredSize(new Dimension(
 							(int)RightSidePanel.getInstance().getSize().getWidth()-18,
 							(int)RightSidePanel.getInstance().getSize().getHeight()-42));
 				else
 					p.setPreferredSize(new Dimension(1000,(int)RightSidePanel.getInstance().getSize().getHeight()-57));
 
 			}
 			
 		}
 		
 	}
 	public Vector<String> getNumberedListOfBorings() {
 		Vector<String> listOfBorings = new Vector<String>();
 		for(int i = 1; i<=numBores; i++)
 		{
 			listOfBorings .add(i+"");
 		}
 		return listOfBorings;
 	}
 
 	public BoringLogDisplay getCurrentlyViewedBoring() {
 		int index = boringTabbedPane.getSelectedIndex()-1;
 		return listOfBoringLogDisplays.get(index);
 	}
 	
 	public int getCurrentlyViewedBoringIndex(){
 		return boringTabbedPane.getSelectedIndex();
 	}
 
 	public SiteType getTheSiteType() {
 		return theSiteType;
 	}
 
 	public ArrayList<BoringLogDisplay> getListOfBoringLogs() {
 		return listOfBoringLogDisplays;
 	}
 	
 	public ArrayList<GeoTestPanel> getListOfGeoTestPanels() {
 		return listOfGeoTestPanels;
 	}
 
 }
