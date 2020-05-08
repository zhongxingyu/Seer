 /**
  * 
  */
 package BothellBirder;
 
 import java.awt.BorderLayout;
 import java.awt.Frame;
 import java.awt.GraphicsConfiguration;
 import java.awt.GraphicsEnvironment;
 import java.awt.HeadlessException;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import javax.swing.DefaultListModel;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 
 /**
  * @author Bret Van Hof
  *
  */
 public class GUI extends JFrame 
 {
 	private static final long serialVersionUID = 1L;
 	private JButton nameSearch;
 	private JTextField searchBox;
 	private JList<?> listJList;
 	private JScrollPane jScrollPane;
 	private JPanel listPanel;
 	private ArrayList<ImageIcon> images;
 	private ArrayList<BirdName> birdNames;
 	private ArrayList<BirdName> updater;
 	private BirdNameRetriever birdNamer;
 	private ImageMaker imager;
 	private JPanel nameSearcher;
 	private JPanel imagePanel;
 	private JButton searchByFeatures;
 	private JLabel pic;
 	private JButton resetList;
 	private JPanel buttonPanel;
 	private JButton displayBird;
 	private Map<Integer,String> mySet;
 	private  Map<String, ImageIcon> icons = new TreeMap<String, ImageIcon>();
 	/**
 	 * @throws HeadlessException
 	 */
 	public GUI() throws HeadlessException 
 	{
 		createData();
 		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
 		this.setExtendedState(this.getExtendedState() | this.MAXIMIZED_BOTH);
 		setTitle("BothellBirder by Team 5");
 		setLayout(new BorderLayout());
 		this.setIconImage(Toolkit.getDefaultToolkit().getImage
 				("defaultBird.jpg")); //sets icon
 		initComponents();
 	}
 
 	class Birder implements ActionListener
 	{
 		public void actionPerformed(ActionEvent e)
 		{
 			JFrame display = new BirdGUI((Integer) listJList.getSelectedValue());
 			display.setVisible(true);
 			GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
 			display.setExtendedState(display.getExtendedState() | display.MAXIMIZED_BOTH);
 			setTitle("BothellBirder by Team 5");
 		}
 	}
 
 	class Searcher implements ActionListener
 	{
 		private ActionListener listener;
 		private JButton next = new JButton("Select Feature and Continue Search");
 		private JLabel featuresJlistJLabel = new JLabel();
 		private JList<String> selectableFeaturesJList = new JList<String>();
 		private DefaultListModel<String> model = new DefaultListModel<String>();
 		private Map<String, ArrayList<String>> featureToSelectableFeatureMap = new TreeMap<String, ArrayList<String>>();
 
 		public void actionPerformed(ActionEvent e)
 		{
 
 			descriptionMaker make = new descriptionMaker();
 			try {
 				featureToSelectableFeatureMap = make.getFeatures();
 			} catch (SQLException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			String[] featureName = new String[featureToSelectableFeatureMap.size()];
 			int z = 0;
 
 			for (Map.Entry<String, ArrayList<String>> entry : featureToSelectableFeatureMap.entrySet())
 			{
 					featureName[z] = entry.getKey();
 					z++;
 			}
 			ArrayList<String> listOfSelectableFeatures = new ArrayList<String>
 			(featureToSelectableFeatureMap.get(featureName[0]));//new ArrayList<String>();
 			updater = new ArrayList<BirdName>();
 			birdNamer = new BirdNameRetriever();
 			int IDs = 0;
 			try {
 				IDs = birdNamer.getFeatureID(featureName[0]);
 			} catch (SQLException e2) {
 				// TODO Auto-generated catch block
 				e2.printStackTrace();
 			}
 
 			int[] IDS = new int[listOfSelectableFeatures.size()/2];
 			int counter = 0;
 			for(int g = 1; g < IDS.length; g += 2)
 			{
 				int featureID = Integer.parseInt(listOfSelectableFeatures.get(g));
 				IDS[counter] = featureID;
 				counter++;
 			}
 			for(int index = 0; index < listOfSelectableFeatures.size(); index++)
 			{
 					listOfSelectableFeatures.remove(index+1);
 			}
 			featuresJlistJLabel.setText(featureName[0]);
 			int i = 0;
 			for(String fam: listOfSelectableFeatures)
 			{
 				model.add(i, fam);
 				i++;
 			}
 			for(int index = 0; index < listOfSelectableFeatures.size() - 1; index++)
 			{
 				listOfSelectableFeatures.remove(index+1);
 			}
 			selectableFeaturesJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 			JScrollPane jScrollPanes = new javax.swing.JScrollPane();
 			String[] as = new String[listOfSelectableFeatures.size()];
 			selectableFeaturesJList = new JList<String>(listOfSelectableFeatures.toArray(as));
 			selectableFeaturesJList.setModel(model);
 			jScrollPanes.setViewportView(selectableFeaturesJList);
 			listener = new Next(featureName, 0, selectableFeaturesJList, featureToSelectableFeatureMap, IDS);
 			next.addActionListener(listener);
 			imagePanel.removeAll();
 			imagePanel.add(featuresJlistJLabel);
 			imagePanel.add(jScrollPanes);
 			imagePanel.add(next);
 			imagePanel.revalidate();
 		}
 	}
 	class Next implements ActionListener
 	{
 		private String[] featureNames;
 		private int theIndexOfTheCurrentFeature;
 		private JList<String> selectableFeaturesJList;
 		private  Map<String, ArrayList<String>> featureToSelectableFeatureMap;
 		private JButton next = new JButton("Select Feature and Continue Search");
 		private JList<String> oldSelectableFeaturesJList;
 		private int[] IDS;
 		Next(String[] a, int index, JList<String> b, Map<String, ArrayList<String>> g, int[] counter)
 		{
 			featureNames = a; 
 			theIndexOfTheCurrentFeature = index;
 			oldSelectableFeaturesJList = b;
 			selectableFeaturesJList = b;
 			featureToSelectableFeatureMap = g;
 			IDS = counter;
 		}
 		public void actionPerformed(ActionEvent e)
 		{
 			int now = 1;
 			System.out.println("This is next click #" + now);
 			if(theIndexOfTheCurrentFeature >= featureNames.length - 1)
 			{
 				imagePanel.removeAll();
 				imagePanel.add(pic);
 				imagePanel.revalidate();
 				theIndexOfTheCurrentFeature = 0;
 				return;
 			}
 			else
 			{
 				ArrayList<BirdName> newer = new ArrayList<BirdName>();
 				try 
 				{
 					int featr = birdNamer.getFeatureID(featureNames[theIndexOfTheCurrentFeature]);
 					ArrayList<String> feature = featureToSelectableFeatureMap.get(featureNames[theIndexOfTheCurrentFeature]);
 					updater = birdNamer.updateData(featr, IDS[oldSelectableFeaturesJList.getSelectedIndex()]);
 					int i = 0;
 					DefaultListModel<String> model = new DefaultListModel<String>();
 					ArrayList<String> list = featureToSelectableFeatureMap.get(featureNames[theIndexOfTheCurrentFeature + 1]);
					int[] IDS = {};
					if(feature.size() == 2)
						IDS = new int[2];
					else
					{
						if(feature.size() % 2 == 0)
							 IDS = new int[feature.size()/2];
						else
							IDS = new int[feature.size()/2 + 1];
					}
 					int counter = 0;
 					for(int g = 1; g < IDS.length; g += 2)
 					{
 						int featureID = Integer.parseInt(list.get(g));
 						IDS[counter] = featureID;
 						counter++;
 					}
 					for(int index = 0; index < list.size() - 1; index++)
 					{
 							list.remove(index+1);
 					}
 					for(String fam: list)
 					{
 						model.add(i, fam);
 						i++;
 					}
 					imagePanel.removeAll();
 					JLabel featuresJlistJLabel = new JLabel();
 					featuresJlistJLabel.setText(featureNames[theIndexOfTheCurrentFeature + 1]);
 					imagePanel.add(featuresJlistJLabel);
 					selectableFeaturesJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 					JScrollPane jScrollPanes = new javax.swing.JScrollPane();
 					selectableFeaturesJList = new JList<String>(model);
 					jScrollPanes.setViewportView(selectableFeaturesJList);
 					imagePanel.add(jScrollPanes);
					ActionListener nextAction = makeThis(featureNames, theIndexOfTheCurrentFeature + 1, oldSelectableFeaturesJList,
 							featureToSelectableFeatureMap, IDS);
 					next.addActionListener(nextAction);
 				}
 				catch (SQLException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 				for(BirdName ID : updater)
 				{
 					newer.add(ID);
 				}
 				Set<BirdName> uniqueSetOfBirdNameOb = new LinkedHashSet<BirdName>(newer);
 				listJList.removeAll();
 				Integer[] birdID = new Integer[uniqueSetOfBirdNameOb.size()];
 
 				int k = 0;
 				for(BirdName ID : uniqueSetOfBirdNameOb)
 				{
 					birdID[k] = (Integer)ID.getBirdId();
 					k++;
 				}
 				updateList(updater);
 				imagePanel.add(next);
 				imagePanel.revalidate();
 			}
 		}
 	}
 
 	private void createData()
 	{
 		birdNames = new ArrayList<BirdName>();
 		images = new ArrayList<ImageIcon>();
 		birdNamer = new BirdNameRetriever();
 		imager = new ImageMaker();
 		try 
 		{
 			birdNames = birdNamer.readData();
 		} 
 		catch (SQLException e) 
 		{
 			e.printStackTrace();
 			JOptionPane.showMessageDialog(null, "Can't connect to database.  Closing program!", 
 					"", JOptionPane.WARNING_MESSAGE);
 			//System.exit(-1);
 		}
 	}
 
 	public ActionListener makeThis(String[] a, int b, JList<String> c, Map<String, ArrayList<String>> d, int[] e)
 	{
 		ActionListener nextAction = new Next(a, b, c, d, e);
 		return nextAction;
 	}
 	
 	private void initComponents() 
 	{
 		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
 		createJPanels();
 		createButtons();
 		createOther();
 		createList();
 	}
 
 	private void createOther()
 	{
 		pic = new JLabel(new ImageIcon("defaultBird.jpg"));
 		imagePanel.add(pic);
 		searchBox = new JTextField();
 		searchBox.setText("Enter Bird Name Here");
 		searchBox.setColumns(20);
 		nameSearcher.add(searchBox);
 		searchBox.grabFocus();
 	}
 
 	private void createJPanels()
 	{
 		listPanel = new JPanel();
 		imagePanel = new JPanel();
 		nameSearcher = new JPanel();
 		buttonPanel = new JPanel();
 		this.add( listPanel, BorderLayout.WEST);
 		this.add( nameSearcher, BorderLayout.NORTH);
 		this.add( imagePanel, BorderLayout.EAST);
 		this.add(buttonPanel, BorderLayout.SOUTH);
 	}
 
 	private void createButtons()
 	{
 		nameSearch = new JButton("Search by name");
 		searchByFeatures = new JButton("Search by features");
 		displayBird = new JButton("Display Bird Selected in List");
 		ActionListener dis = new Birder();
 		displayBird.addActionListener(dis);
 		ActionListener feat = new Searcher();
 		searchByFeatures.addActionListener(feat);
 		resetList = new JButton("Reset List to default (clear features search)");
 		nameSearcher.add(nameSearch);
 		buttonPanel.add(searchByFeatures);
 		buttonPanel.add(resetList);
 		buttonPanel.add(displayBird);
 	}
 	private void updateList(ArrayList<BirdName> listOfBirds)
 	{
 		icons = updateJList(listOfBirds);
 		int k = 0;
 		Integer[] birdID = new Integer[listOfBirds.size()];
 		for(BirdName IDa : updater)
 		{
 			birdID[k] = (Integer)IDa.getNameId();
 			k++;
 		}
 		listJList = new JList<Object>(birdID);
 		listJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		jScrollPane = new javax.swing.JScrollPane();
 		jScrollPane.setViewportView(listJList);
 		listPanel.removeAll();
 		listJList.setCellRenderer(new listRenderer(icons, mySet));
 		listPanel.add(jScrollPane, java.awt.BorderLayout.WEST);
 	}
 
 	private void createList()
 	{
 		icons = getInitJListData();
 		Integer[] birdID = new Integer[birdNames.size()];
 		int i = 0;
 		mySet = new TreeMap<Integer, String>();
 		for(BirdName ID : birdNames)
 		{
 			birdID[i] = (Integer)ID.getNameId();
 			mySet.put(ID.getNameId(), ID.getName());
 			i++;
 		}
 		listJList = new JList<Integer>(birdID);
 		listJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		jScrollPane = new javax.swing.JScrollPane();
 		jScrollPane.setViewportView(listJList);
 		listPanel.add(jScrollPane, java.awt.BorderLayout.WEST);
 		listJList.setCellRenderer(new listRenderer(icons, mySet));
 	}
 
 	private Map<String, ImageIcon> getInitJListData()
 	{
 		Map<String, ImageIcon> icons = new TreeMap<String, ImageIcon>();
 		ImageIcon test = new ImageIcon("0a0.jpg");
 		mySet = new TreeMap<Integer, String>();
 		int index = 0;
 		for(BirdName name : birdNames)
 		{
 			try {
 				images.add(imager.readData(name.getBirdId(), name.getNameId()));
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 			icons.put(name.getName(), images.get(index));
 			mySet.put(name.getNameId(), name.getName());
 			System.out.println(name.getName());
 			index++;
 		}
 		if(birdNames.size() == 0)
 			icons.put("empty", test);
 		return icons;
 	}
 
 	private Map<String, ImageIcon> updateJList(ArrayList<BirdName> listOfBirdNameObjects)
 	{
 		Map<String, ImageIcon> icons = new TreeMap<String, ImageIcon>();
 		images = new ArrayList<ImageIcon>();
 		mySet = new TreeMap<Integer, String>();
 		ImageIcon test = new ImageIcon("0a0.jpg");
 		int index = 0;
 		for(BirdName name : listOfBirdNameObjects)
 		{
 			try {
 				images.add(imager.readData(name.getBirdId(), name.getNameId()));
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 			icons.put(name.getName(), images.get(index));
 			mySet.put(name.getNameId(), name.getName());
 			index++;
 		}
 		if(birdNames.size() == 0)
 			icons.put("empty", test);
 		return icons;
 	}
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args)
 	{
 		java.awt.EventQueue.invokeLater(new Runnable() 
 		{
 			public void run() 
 			{
 				JFrame.setDefaultLookAndFeelDecorated(true);
 				new GUI().setVisible(true);                
 			} 
 		});  
 	}
 }
