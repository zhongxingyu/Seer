 package utlik.anna.pack;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.logging.Logger;
 
 import javax.imageio.ImageIO;
 import javax.swing.DefaultListModel;
 import javax.swing.GroupLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JColorChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JTextArea;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingConstants;
 import javax.swing.event.ListDataListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 /**
  * 
  * Class UserInterface responsible for UI layout and UI elements,
  * 
  * @author anna
  */
 
 public class UserInterface extends JFrame {
 	private static final long serialVersionUID = 8731622220764113056L;
 	
 	private boolean[] indexPageButtons = new boolean[9];
 	private boolean[] tracksButtons = new boolean[9];
 	
 	public static final int OPENXML = 1;
 	public static final int OPENFOLDER = 2;
 	public static final int OPENPICTURE = 3;
 	public static final int ADDINFO = 4;
 	public static final int CREATEHTML = 5;
 	public static final int REMOVEPICTURE = 6;
 	public static final int HEADPICTURE = 7;
 	public static final int TAGCLOUD = 8;
 	public static final int SPONSORS = 9;
 
 	private String destinationDirPath;
 	private JLabel xmlFile;
 	private JLabel destFolder;
 	private JLabel tagCloudSourceData;
 	private final JTextArea sponsorsInfo;
 	private final JTextArea infoConference;
 
 	private GroupLayout layout;
 	private DefaultListModel listModel;
 	private JList listOfPictures;
 	private JLabel headPicture;
 	private JCheckBox myCalendarCB;
 	private JCheckBox programScheduleCB;
 	
 
 	private JCheckBox tracksCB;
 	private JCheckBox authorsCB;
 	private JCheckBox aboutConfCB;
 	private JCheckBox tagCloudCB;
 	private JCheckBox floorPlanCB;
 	private JCheckBox sponsorsCB;
 	private JCheckBox researchCB;
 	private JCheckBox challangesandvisionCB;
 	private JCheckBox industrialCB;
 	private JCheckBox phdworkshopCB;
 	private JCheckBox tutorialCB;
 	private JCheckBox workshopsCB;
 	private JCheckBox keynoteCB;
 	private JCheckBox demoCB;
 	private JCheckBox panelCB;
 
 	private BufferedImage myPicture;
 
 	private JLabel picsLabel;
 
 	private JTextArea roomName;
 //	private final BrowserCanvas browserCanvas;
 	
 	public JList locationNamesList;
 
 	private DefaultListModel listLocationsModel;
 
 	public DefaultListModel getListLocationsModel() {
 		return listLocationsModel;
 	}
 
 	public JLabel getTagCloudSourceData() {
 		return tagCloudSourceData;
 	}
 
 	public JTextArea getSponsorsInfo() {
 		return sponsorsInfo;
 	}
 
 	public static long getSerialversionuid() {
 		return serialVersionUID;
 	}
 
 	public boolean[] getIndexPageButtons() {
 		return indexPageButtons;
 	}
 
 	public boolean[] getTracksButtons() {
 		return tracksButtons;
 	}
 
 	public String getDestinationDirPath() {
 		return destinationDirPath;
 	}
 
 	public JLabel getXmlFile() {
 		return xmlFile;
 	}
 
 	public JLabel getDestFolder() {
 		return destFolder;
 	}
 
 	public JTextArea getInfoConference() {
 		return infoConference;
 	}
 
 	public GroupLayout getLayout() {
 		return layout;
 	}
 
 	public DefaultListModel getListModel() {
 		return listModel;
 	}
 
 	public JList getListOfPictures() {
 		return listOfPictures;
 	}
 
 	public JLabel getHeadPicture() {
 		return headPicture;
 	}
 
 	public JCheckBox getMyCalendarCB() {
 		return myCalendarCB;
 	}
 
 	public JCheckBox getProgramScheduleCB() {
 		return programScheduleCB;
 	}
 
 	public JCheckBox getTracksCB() {
 		return tracksCB;
 	}
 
 	public JCheckBox getAuthorsCB() {
 		return authorsCB;
 	}
 
 	public JCheckBox getAboutConfCB() {
 		return aboutConfCB;
 	}
 
 	public JCheckBox getTagCloudCB() {
 		return tagCloudCB;
 	}
 
 	public JCheckBox getFloorPlanCB() {
 		return floorPlanCB;
 	}
 
 	public JCheckBox getSponsorsCB() {
 		return sponsorsCB;
 	}
 
 	public void setDestinationDirPath(String destinationDirPath) {
 		this.destinationDirPath = destinationDirPath;
 	}
 
 	public JCheckBox getResearchCB() {
 		return researchCB;
 	}
 
 	public JCheckBox getChallangesandvisionCB() {
 		return challangesandvisionCB;
 	}
 
 	public JCheckBox getIndustrialCB() {
 		return industrialCB;
 	}
 
 	public JCheckBox getPhdworkshopCB() {
 		return phdworkshopCB;
 	}
 
 	public JCheckBox getTutorialCB() {
 		return tutorialCB;
 	}
 
 	public JCheckBox getWorkshopsCB() {
 		return workshopsCB;
 	}
 
 	public JCheckBox getKeynoteCB() {
 		return keynoteCB;
 	}
 
 	public JCheckBox getDemoCB() {
 		return demoCB;
 	}
 
 	public JCheckBox getPanelCB() {
 		return panelCB;
 	}
 //	public BrowserCanvas getBrowserCanvas() {
 //		return browserCanvas;
 //	}
 
 	public JLabel getPicsLabel() {
 		return picsLabel;
 	}
 
 	/**
 	 * Constructor of UserInterface
 	 */
 	public UserInterface() {
 		JSeparator separatorVertical = new JSeparator(SwingConstants.VERTICAL);
 		JSeparator separatorHorisontal = new JSeparator(
 				SwingConstants.HORIZONTAL);
 
 //		browserCanvas = new BrowserCanvas();
 //		browserCanvas.setPreferredSize(new Dimension(400, 800));
 		// JPanel panel = new JPanel(new BorderLayout());
 		// panel.add(browserCanvas, BorderLayout.CENTER);
 		try {
 			
 			myPicture = ImageIO.read(new File("./assets/location.png"));
 		} catch (IOException e2) {
 			// TODO Auto-generated catch block
 			e2.printStackTrace();
 		}
 		
 		picsLabel = new JLabel(new ImageIcon( myPicture.getScaledInstance(110,200, Image.SCALE_SMOOTH) ));
 		// Labels for text insertion
 		xmlFile = new JLabel("", SwingConstants.LEFT);
 		destFolder = new JLabel("", SwingConstants.LEFT);
 		headPicture = new JLabel("", SwingConstants.LEFT);
 		infoConference = new JTextArea(2, 2);
 		tagCloudSourceData = new JLabel("", SwingConstants.LEFT);
 		sponsorsInfo = new JTextArea(2, 2);
 		roomName = new JTextArea(2, 2);
 		infoConference.setEditable(true);
 		infoConference.setLineWrap(true);
 		roomName.setEditable(false);
 		roomName.setLineWrap(true);
 		sponsorsInfo.setEditable(false);
 		sponsorsInfo.setLineWrap(true);
 
 		// Label with text
 		JLabel folderLabel = new JLabel("Destination folder",
 				SwingConstants.LEFT);
 		JLabel textlabel = new JLabel("XML data file", SwingConstants.CENTER);
 		JLabel infoLabel = new JLabel("Information about conference",
 				SwingConstants.LEFT);
 		JLabel headPictureLabel = new JLabel(
 				"Picture for head of a page (size 7x*1x)");
 		JLabel picLabel = new JLabel("Floor plan pictures", SwingConstants.LEFT);
 		JLabel programLabel = new JLabel("Buttons for Program Schedule");
 		JLabel papersLabel = new JLabel("Buttons for Papers");
 		JLabel generalInfo = new JLabel("Buttons for General Info");
 		JLabel tracksLabel = new JLabel("Buttons for track types");
 		JLabel sponsorsInfoLabel = new JLabel("Sponsors page info");
 		JLabel tagCloudSourceDataLabel = new JLabel(
 				"Raw source data for TagCloud");
 
 		// Buttons
 		JButton openB = new JButton("Open XML file");
 		JButton createB = new JButton("Create HTML");
 		JButton openDestinationFolderB = new JButton("Open folder");
 		JButton addInfoB = new JButton("Add info");
 		final JButton colorB = new JButton("Choose color");
 		final JButton addPictureB = new JButton("Add picture");
 		addPictureB.setEnabled(false);
 		final JButton removePicB = new JButton("Remove picture");
 		removePicB.setEnabled(false);
 		JButton addHeadPictB = new JButton("Add head picture");
 		final JButton addTagCloudInfoB = new JButton("Provide TagCloud");
 		addTagCloudInfoB.setEnabled(false);
 		final JButton addSponsorsInfoB = new JButton("Add sponsors");
 		addSponsorsInfoB.setEnabled(false);
 
 		colorB.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				// TODO Auto-generated method stub
 				Color bgColor = JColorChooser.showDialog(UserInterface.this,
 						"Choose Background Color", getBackground());
 				System.out.println(Integer.toHexString(bgColor.getRGB()));
 				colorB.setBackground(bgColor);
 			}
 		});
 		// JCheckBoxes
 		myCalendarCB = new JCheckBox("My calendar");
 		myCalendarCB.setSelected(true);
 		programScheduleCB = new JCheckBox("Conference program");
 		programScheduleCB.setSelected(true);
 		tracksCB = new JCheckBox("Tracks");
 		tracksCB.setSelected(true);
 		authorsCB = new JCheckBox("Authors");
 		authorsCB.setSelected(true);
 		aboutConfCB = new JCheckBox("About conference");
 		aboutConfCB.setSelected(true);
 		floorPlanCB = new JCheckBox("Floor plan");
 
 		researchCB = new JCheckBox("Research track");
 		industrialCB = new JCheckBox("Industrial track");
 		challangesandvisionCB = new JCheckBox("Challanges and Vision track");
 		tutorialCB = new JCheckBox("Tutorial track");
 		phdworkshopCB = new JCheckBox("PhD Workshop");
 		workshopsCB = new JCheckBox("Workshop");
 		keynoteCB = new JCheckBox("Keynote");
 		panelCB = new JCheckBox("Panel");
 		demoCB = new JCheckBox("Demo track");
 
 		sponsorsCB = new JCheckBox("Sponsors");
 		tagCloudCB = new JCheckBox("Tag Cloud");
 
 		sponsorsCB.addItemListener(new ItemListener() {
 
 			public void itemStateChanged(ItemEvent e) {
 				System.err.println(e.getStateChange());
 				switch (e.getStateChange()) {
 				case 1:
 					addSponsorsInfoB.setEnabled(true);
 					sponsorsInfo.setEditable(true);
 					break;
 				case 2:
 					addSponsorsInfoB.setEnabled(false);
 					sponsorsInfo.setText("");
 					sponsorsInfo.setEditable(false);
 					break;
 				}
 
 			}
 		});
 		tagCloudCB.addItemListener(new ItemListener() {
 
 			public void itemStateChanged(ItemEvent e) {
 				System.err.println(e.getStateChange());
 				switch (e.getStateChange()) {
 				case 1:
 					addTagCloudInfoB.setEnabled(true);
 					break;
 				case 2:
 					addTagCloudInfoB.setEnabled(false);
 					tagCloudSourceData.setText("");
 					break;
 				}
 			}
 		});
 		floorPlanCB.addItemListener(new ItemListener() {
 
 			public void itemStateChanged(ItemEvent e) {
 				System.err.println(e.getStateChange());
 				switch (e.getStateChange()) {
 				case 1:
 					addPictureB.setEnabled(true);
 					removePicB.setEnabled(true);
 					roomName.setEditable(true);
 					
 					break;
 				case 2:
 					addPictureB.setEnabled(false);
 					removePicB.setEnabled(false);
 					listModel.clear();
 					roomName.setText("");
 					roomName.setEditable(false);
 					break;
 				}
 			}
 		});
 
 		// Font for labels with text
 		folderLabel.setFont(new Font("Serif", Font.BOLD, 15));
 		infoLabel.setFont(new Font("Serif", Font.BOLD, 15));
 		headPictureLabel.setFont(new Font("Serif", Font.BOLD, 15));
 		picLabel.setFont(new Font("Serif", Font.BOLD, 15));
 		textlabel.setFont(new Font("Serif", Font.BOLD, 15));
 		programLabel.setFont(new Font("Serif", Font.BOLD, 15));
 		papersLabel.setFont(new Font("Serif", Font.BOLD, 15));
 		generalInfo.setFont(new Font("Serif", Font.BOLD, 15));
 		tracksLabel.setFont(new Font("Serif", Font.BOLD, 15));
 		sponsorsInfoLabel.setFont(new Font("Serif", Font.BOLD, 15));
 		tagCloudSourceDataLabel.setFont(new Font("Serif", Font.BOLD, 15));
 
 		// ActionListeners to buttons
 		addHeadPictB.addActionListener(new Main.OnButtonClick(HEADPICTURE));
 		addInfoB.addActionListener(new Main.OnButtonClick(ADDINFO));
 		addPictureB.addActionListener(new Main.OnButtonClick(OPENPICTURE));
 		openB.addActionListener(new Main.OnButtonClick(OPENXML));
 		createB.addActionListener(new Main.OnButtonClick(CREATEHTML));
 		openDestinationFolderB.addActionListener(new Main.OnButtonClick(
 				OPENFOLDER));
 		removePicB.addActionListener(new Main.OnButtonClick(REMOVEPICTURE));
 		addTagCloudInfoB.addActionListener(new Main.OnButtonClick(TAGCLOUD));
 		addSponsorsInfoB.addActionListener(new Main.OnButtonClick(SPONSORS));
 
 		// List with multiple pictures paths
 		listLocationsModel = new DefaultListModel();
 		locationNamesList = new JList(listLocationsModel);
 		locationNamesList.addListSelectionListener(new ListSelectionListener() {
 			
 			@Override
 			public void valueChanged(ListSelectionEvent arg0) {
 				// TODO Auto-generated method stub
 				System.out.println(arg0.getClass());
 			if (locationNamesList.getSelectedValue()==null){
 				JOptionPane.showMessageDialog(null,"Element was removed");
 			} else {
 				String newName = JOptionPane.showInputDialog("Rename the location", locationNamesList.getSelectedValue());
 				if (newName==null){
 					newName = (String) locationNamesList.getSelectedValue();
 				}
 				listLocationsModel.set(locationNamesList.getSelectedIndex(), newName);
 			}
 			}
 		} );
 		
 		listModel = new DefaultListModel();
 		listOfPictures = new JList(listModel);
 		listOfPictures.addListSelectionListener(new ListSelectionListener() {
 			@Override
 			public void valueChanged(ListSelectionEvent arg0) {
 				// TODO Auto-generated method stub
 				if (listOfPictures.getSelectedValue()==null){
 					JOptionPane.showMessageDialog(null,"Element was removed");
 				} else {
 					try {
 					myPicture = ImageIO.read(new File((String) listOfPictures.getSelectedValue()));
					int w = myPicture.getWidth();
					int h = myPicture.getHeight();
//					myPicture.getScaledInstance((int) w/15,(int) h/15, Image.SCALE_SMOOTH);
 					picsLabel.setIcon(new ImageIcon(myPicture.getScaledInstance(200,200, Image.SCALE_SMOOTH)));
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 		});
 		listOfPictures.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 
 		// Scroll panels for infotext and list of pictures
 		JScrollPane scrollPane = new JScrollPane(infoConference);
 		scrollPane.setMinimumSize(new Dimension(100, 100));
 		JScrollPane scrollListPane = new JScrollPane(listOfPictures);
 		JScrollPane scrollPaneSponsors = new JScrollPane(sponsorsInfo);
 		scrollPaneSponsors.setMinimumSize(new Dimension(100, 100));
 		JScrollPane scrollPaneLocNames = new JScrollPane(locationNamesList);
 		scrollPaneLocNames.setMinimumSize(new Dimension(100, 100));
 
 		// ToolTips for everything
 		openB.setToolTipText("Open XML data file");
 		infoConference
 				.setToolTipText("For nice style of result page HTML formatted text is preffered");
 
 		myCalendarCB.setToolTipText("Here will be tips");
 		programScheduleCB.setToolTipText("Here will be tips");
 		tracksCB.setToolTipText("Here will be tips");
 		authorsCB.setToolTipText("Here will be tips");
 		tagCloudCB.setToolTipText("Here will be tips");
 		sponsorsCB.setToolTipText("Here will be tips");
 		aboutConfCB.setToolTipText("Here will be tips");
 		floorPlanCB.setToolTipText("Here will be tips");
 
 		industrialCB.setToolTipText("Here will be tips");
 		researchCB.setToolTipText("Here will be tips");
 		challangesandvisionCB.setToolTipText("Here will be tips");
 		tutorialCB.setToolTipText("Here will be tips");
 		demoCB.setToolTipText("Here will be tips");
 		phdworkshopCB.setToolTipText("Here will be tips");
 		workshopsCB.setToolTipText("Here will be tips");
 		keynoteCB.setToolTipText("Here will be tips");
 		panelCB.setToolTipText("Here will be tips");
 
 		JPanel p = new JPanel();
 
 		layout = new GroupLayout(p);
 		layout.setHorizontalGroup(layout
 				.createSequentialGroup()
 				// labels
 				.addGroup(
 						layout.createParallelGroup(
 								GroupLayout.Alignment.LEADING, true)
 								.addGroup(
 										layout.createSequentialGroup()
 												.addGroup(
 														layout.createParallelGroup(
 																GroupLayout.Alignment.LEADING,
 																true)
 																.addComponent(
 																		programLabel)
 																.addGroup(
 																		layout.createSequentialGroup()
 																				.addComponent(
 																						myCalendarCB)
 																				.addComponent(
 																						programScheduleCB))
 
 																.addComponent(
 																		papersLabel)
 																.addGroup(
 																		layout.createSequentialGroup()
 																				.addComponent(
 																						tracksCB)
 																				.addComponent(
 																						authorsCB)
 																				.addComponent(
 																						tagCloudCB))
 																.addComponent(
 																		generalInfo)
 																.addGroup(
 																		layout.createSequentialGroup()
 																				.addComponent(
 																						aboutConfCB)
 																				.addComponent(
 																						sponsorsCB)
 																				.addComponent(
 																						floorPlanCB)))
 												.addGap(50)
 												.addGroup(
 														layout.createParallelGroup(
 																GroupLayout.Alignment.LEADING,
 																true)
 																.addComponent(
 																		tracksLabel)
 																.addGroup(
 																		layout.createSequentialGroup()
 																				.addComponent(
 																						industrialCB)
 																				.addComponent(
 																						researchCB))
 																.addGroup(
 																		layout.createSequentialGroup()
 																				.addComponent(
 																						demoCB)
 																				.addComponent(
 																						panelCB))
 
 																.addGroup(
 																		layout.createSequentialGroup()
 																				.addComponent(
 																						tutorialCB)
 																				.addComponent(
 																						phdworkshopCB))
 
 																.addGroup(
 																		layout.createSequentialGroup()
 																				.addComponent(
 																						workshopsCB)
 																				.addComponent(
 																						keynoteCB))
 																.addComponent(
 																		challangesandvisionCB)))
 								.addComponent(separatorHorisontal)
 								.addComponent(textlabel,
 										GroupLayout.Alignment.CENTER)
 								.addComponent(xmlFile)
 								.addComponent(folderLabel,
 										GroupLayout.Alignment.CENTER)
 								.addComponent(destFolder)
 								.addComponent(infoLabel,
 										GroupLayout.Alignment.CENTER)
 								.addComponent(scrollPane)
 								.addComponent(picLabel,
 										GroupLayout.Alignment.CENTER)
 								.addGroup(layout.createSequentialGroup().addComponent(scrollListPane).addComponent(picsLabel).addComponent(scrollPaneLocNames))
 //								.addComponent(scrollListPane)
 								.addComponent(headPictureLabel,
 										GroupLayout.Alignment.CENTER)
 								.addComponent(headPicture)
 								.addComponent(tagCloudSourceDataLabel)
 								.addComponent(tagCloudSourceData)
 								.addComponent(sponsorsInfoLabel)
 								.addComponent(scrollPaneSponsors))
 				.addGap(30)
 				// buttons
 				.addGroup(
 						layout.createParallelGroup(
 								GroupLayout.Alignment.LEADING, false)
 								.addComponent(separatorHorisontal)
 								.addComponent(openB, GroupLayout.DEFAULT_SIZE,
 										GroupLayout.DEFAULT_SIZE,
 										Short.MAX_VALUE)
 								.addComponent(openDestinationFolderB,
 										GroupLayout.DEFAULT_SIZE,
 										GroupLayout.DEFAULT_SIZE,
 										Short.MAX_VALUE)
 								.addComponent(addInfoB,
 										GroupLayout.DEFAULT_SIZE,
 										GroupLayout.DEFAULT_SIZE,
 										Short.MAX_VALUE)
 								.addComponent(addPictureB,
 										GroupLayout.DEFAULT_SIZE,
 										GroupLayout.DEFAULT_SIZE,
 										Short.MAX_VALUE)
 								.addComponent(removePicB,
 										GroupLayout.DEFAULT_SIZE,
 										GroupLayout.DEFAULT_SIZE,
 										Short.MAX_VALUE)
 //								.addComponent(locationNamesList,
 //										GroupLayout.DEFAULT_SIZE,
 //										GroupLayout.DEFAULT_SIZE,
 //										Short.MAX_VALUE)
 								.addComponent(addHeadPictB,
 										GroupLayout.DEFAULT_SIZE,
 										GroupLayout.DEFAULT_SIZE,
 										Short.MAX_VALUE)
 								.addComponent(addTagCloudInfoB,
 										GroupLayout.DEFAULT_SIZE,
 										GroupLayout.DEFAULT_SIZE,
 										Short.MAX_VALUE)
 								.addComponent(addSponsorsInfoB,
 										GroupLayout.DEFAULT_SIZE,
 										GroupLayout.DEFAULT_SIZE,
 										Short.MAX_VALUE)
 								.addComponent(createB,
 										GroupLayout.DEFAULT_SIZE,
 										GroupLayout.DEFAULT_SIZE,
 										Short.MAX_VALUE)
 								.addComponent(colorB, GroupLayout.DEFAULT_SIZE,
 										GroupLayout.DEFAULT_SIZE,
 										Short.MAX_VALUE))
 				.addComponent(separatorVertical)
				.addComponent(picsLabel)
 
 		);
 
 		layout.setVerticalGroup(layout
 				.createParallelGroup(GroupLayout.Alignment.LEADING, true)
 				// labels
 				.addGroup(
 						layout.createSequentialGroup()
 								.addGroup(
 										layout.createParallelGroup(
 												GroupLayout.Alignment.LEADING,
 												true)
 												.addGroup(
 														layout.createSequentialGroup()
 																.addComponent(
 																		programLabel)
 																.addGroup(
 																		layout.createParallelGroup(
 																				GroupLayout.Alignment.LEADING,
 																				true)
 																				.addComponent(
 																						myCalendarCB)
 
 																				.addComponent(
 																						programScheduleCB))
 
 																.addComponent(
 																		papersLabel)
 																.addGroup(
 																		layout.createParallelGroup(
 																				GroupLayout.Alignment.LEADING,
 																				true)
 																				.addComponent(
 																						tracksCB)
 																				.addComponent(
 																						authorsCB)
 																				.addComponent(
 																						tagCloudCB))
 																.addComponent(
 																		generalInfo)
 																.addGroup(
 																		layout.createParallelGroup(
 																				GroupLayout.Alignment.LEADING,
 																				true)
 																				.addComponent(
 																						aboutConfCB)
 																				.addComponent(
 																						sponsorsCB)
 																				.addComponent(
 																						floorPlanCB)))
 												.addGroup(
 														layout.createSequentialGroup()
 																.addComponent(
 																		tracksLabel)
 																.addGroup(
 																		layout.createParallelGroup(
 																				GroupLayout.Alignment.LEADING,
 																				true)
 																				.addComponent(
 																						industrialCB)
 																				.addComponent(
 																						researchCB))
 																.addGroup(
 																		layout.createParallelGroup(
 																				GroupLayout.Alignment.LEADING,
 																				true)
 																				.addComponent(
 																						demoCB)
 																				.addComponent(
 																						panelCB))
 																.addGroup(
 																		layout.createParallelGroup(
 																				GroupLayout.Alignment.LEADING,
 																				true)
 
 																				.addComponent(
 																						tutorialCB)
 																				.addComponent(
 																						phdworkshopCB))
 																.addGroup(
 																		layout.createParallelGroup(
 																				GroupLayout.Alignment.LEADING,
 																				true)
 																				.addComponent(
 																						workshopsCB)
 																				.addComponent(
 																						keynoteCB))
 																.addComponent(
 																		challangesandvisionCB)))
 								.addComponent(separatorHorisontal)
 								.addComponent(textlabel)
 
 								.addGroup(
 										layout.createParallelGroup(
 												GroupLayout.Alignment.LEADING,
 												true).addComponent(xmlFile)
 												.addComponent(openB))
 								.addComponent(folderLabel)
 								.addGroup(
 										layout.createParallelGroup(
 												GroupLayout.Alignment.LEADING,
 												true)
 												.addComponent(destFolder)
 												.addComponent(
 														openDestinationFolderB))
 								.addComponent(infoLabel)
 								.addGroup(
 										layout.createParallelGroup(
 												GroupLayout.Alignment.LEADING,
 												true).addComponent(scrollPane)
 												.addComponent(addInfoB))
 								.addComponent(picLabel)
 								.addGroup(
 										layout.createParallelGroup(
 												GroupLayout.Alignment.LEADING,
 												true)
 												.addComponent(scrollListPane).addComponent(picsLabel).addComponent(scrollPaneLocNames)
 												.addGroup(
 														layout.createSequentialGroup()
 																.addComponent(
 																		addPictureB)
 																.addComponent(
 																		removePicB)
 //																.addComponent(locationNamesList)
 																))
 								.addComponent(headPictureLabel)
 								.addGroup(
 										layout.createParallelGroup(
 												GroupLayout.Alignment.LEADING,
 												true).addComponent(headPicture)
 												.addComponent(addHeadPictB))
 								.addComponent(tagCloudSourceDataLabel)
 								.addGroup(
 										layout.createParallelGroup(
 												GroupLayout.Alignment.LEADING,
 												true)
 												.addComponent(
 														tagCloudSourceData)
 												.addComponent(addTagCloudInfoB))
 								.addComponent(sponsorsInfoLabel)
 								.addGroup(
 										layout.createParallelGroup(
 												GroupLayout.Alignment.LEADING,
 												true)
 												.addComponent(
 														scrollPaneSponsors)
 												.addComponent(addSponsorsInfoB))
 								.addComponent(createB).addComponent(colorB))
 				.addComponent(separatorVertical)
 
				.addComponent(picsLabel)
 				);
 
 		layout.setAutoCreateGaps(true);
 		layout.setAutoCreateContainerGaps(true);
 		p.setLayout(layout);
 		JScrollPane sPane = new JScrollPane(p);
 		getContentPane().add(sPane);
 
 		getRootPane().setDefaultButton(createB);
 		pack();
 //		setPreferredSize(new Dimension(700, 500));
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setExtendedState(JFrame.MAXIMIZED_BOTH);
 //		browserCanvas.connect();
 //		browserCanvas.getBrowser().getDisplay().asyncExec(new Runnable() {
 //		      @Override
 //		      public void run() {
 //		    	  browserCanvas.getBrowser().setUrl("file:///home/anna/FINAL/index.html");
 //		      }
 //		});
 		
 		
 	}
 	
 	
 	public static class LocationPictureObject{
 		String pathToLocPicture;
 		String pictureName;
 		public String getPathToLocPicture() {
 			return pathToLocPicture;
 		}
 
 		public void setPathToLocPicture(String pathToLocPicture) {
 			this.pathToLocPicture = pathToLocPicture;
 		}
 
 		public String getPictureName() {
 			return pictureName;
 		}
 
 		public void setPictureName(String pictureName) {
 			this.pictureName = pictureName;
 		}
 
 
 		
 		public LocationPictureObject(String path, String name){
 			pathToLocPicture=path;
 			pictureName=name;
 		}
 	}
 
 }
