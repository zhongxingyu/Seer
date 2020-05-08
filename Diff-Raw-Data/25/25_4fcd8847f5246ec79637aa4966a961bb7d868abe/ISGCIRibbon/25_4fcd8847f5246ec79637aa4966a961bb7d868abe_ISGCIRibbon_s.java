 package teo.isgci.view.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.io.IOException;
 import java.net.URL;
 
 import javax.imageio.ImageIO;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.Icon;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.ListCellRenderer;
 import javax.swing.border.EmptyBorder;
 
 import org.pushingpixels.flamingo.api.common.HorizontalAlignment;
 import org.pushingpixels.flamingo.api.common.JCommandButton;
 import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
 import org.pushingpixels.flamingo.api.common.RichTooltip;
 import org.pushingpixels.flamingo.api.common.icon.ImageWrapperResizableIcon;
 import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
 import org.pushingpixels.flamingo.api.ribbon.JRibbon;
 import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
 import org.pushingpixels.flamingo.api.ribbon.JRibbonComponent;
 import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenu;
 import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryPrimary;
 import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;
 import org.pushingpixels.flamingo.api.ribbon.RibbonTask;
 import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizePolicies;
 
 import teo.isgci.core.App;
 import teo.isgci.core.GraphView;
 import teo.isgci.core.NodeView;
 import teo.isgci.core.ViewManager;
 import teo.isgci.data.problem.Problem;
 
 import com.jgoodies.forms.builder.DefaultFormBuilder;
 import com.jgoodies.forms.layout.FormLayout;
 
 public class ISGCIRibbon extends JRibbon implements ActionListener{
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	private ISGCIMainFrame parent;
 	private JComboBox<String> colPackage;
 	private JCheckBox unprop = new JCheckBox();
 	private JComboBox<String> boBox;
 	private JComboBox<String> probBox;
 	private ColorBox colBoxL, colBoxP, colBoxNpc, colBoxI, colBoxU;
 	
 	
 	public ISGCIRibbon(ISGCIMainFrame par) {
 		super();
 		parent = par;
 		configureRibbon();
 		this.setVisible(true);
 	}
 	
 	
 protected ResizableIcon getIcon(String ressourceString) {
 	ResizableIcon icon = new SimpleResizableIcon(RibbonElementPriority.TOP, 48, 48);
 	try{
        URL resource = ISGCIMainFrame.class.getClassLoader().getResource(ressourceString);
        icon = ImageWrapperResizableIcon.getIcon(resource, new Dimension(48, 48));
     } catch( Exception ex) {
     }	
 	return icon;
 }
 	
 	protected JRibbonBand getExportBand() {
 		JRibbonBand result = new JRibbonBand("Export As",
 				new SimpleResizableIcon(RibbonElementPriority.TOP, 48, 48));
 		result.setExpandButtonKeyTip("FY");
 		result.setCollapsedStateKeyTip("ZD");
 
 		result.startGroup();
 				
 		JCommandButton graphMLButton = new JCommandButton("Export to File", getIcon("images/Export.png"));
 		graphMLButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				App.getViewManager(parent).export();
 		        JDialog export = new ExportDialog(parent);
 		        export.setLocation(50, 50);
 		        export.pack();
 		        export.setVisible(true);
 			}
 		});
 		
 		
 		result.addCommandButton(graphMLButton, RibbonElementPriority.TOP);
 
 
 		result.setResizePolicies(CoreRibbonResizePolicies.getCorePoliciesNone(result));
 
 		return result;
 	}
 	
 	protected JRibbonBand getGraphClassesBand() {
 		JRibbonBand result = new JRibbonBand("Graph Classes",
 				new SimpleResizableIcon(RibbonElementPriority.TOP, 152, 48));
 		result.setExpandButtonKeyTip("FY");
 		result.setCollapsedStateKeyTip("ZD");
 
 		result.startGroup();
 		JCommandButton dbButton = new JCommandButton("Browse Classdatabase", getIcon("images/Database.png"));
 		dbButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				JDialog info = new GraphClassInformationDialog(parent);
 	            info.setLocation(50, 50);
 	            info.pack();
 	            info.setSize(800, 600);
 	            info.setVisible(true);
 			}
 		});
 		result.addCommandButton(dbButton, RibbonElementPriority.TOP);
 		
 		JCommandButton relationButton = new JCommandButton("Find Classrelation", getIcon("images/Relation.jpg"));
 		relationButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				JDialog check = new CheckInclusionDialog(parent);
 	            check.setLocation(50, 50);
 	            check.pack();
 	            check.setSize(700, 400);
 	            check.setVisible(true);
 			}
 		});
 		result.addCommandButton(relationButton, RibbonElementPriority.TOP);
 		
 		JCommandButton drawButton = new JCommandButton("Draw Graphclasses", getIcon("images/Draw.png"));
 		drawButton.setMaximumSize(new Dimension(48, 48));
 		drawButton.setMinimumSize(new Dimension(48, 48));
 		drawButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				JDialog select = new GraphClassSelectionDialog(parent);
 	            select.setLocation(50, 50);
 	            select.pack();
 	            select.setSize(500, 400);
 	            select.setVisible(true);
 			}
 		});
 		result.addCommandButton(drawButton, RibbonElementPriority.TOP);
 
 
 		result.setResizePolicies(CoreRibbonResizePolicies.getCorePoliciesNone(result));
 
 		return result;
 	}
 	
 	protected JRibbonBand getMarkUnproperBand() {
 		JRibbonBand result = new JRibbonBand("Mark unproper Inclusions",
 				new SimpleResizableIcon(RibbonElementPriority.TOP, 48, 48));
 		result.setExpandButtonKeyTip("FY");
 		result.setCollapsedStateKeyTip("ZD");
 
 		result.startGroup();
 		
 		unprop.setSelected(true);
 		unprop.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				App.getViewManager(parent).setDrawUnproper(
 	                    unprop.isSelected());
 			}
 		});
 		result.addRibbonComponent(new JRibbonComponent(
 				null,
 				"Mark unproper Inclusions", unprop));
 
 
 
 		result.setResizePolicies(CoreRibbonResizePolicies.getCorePoliciesNone(result));
 		return result;
 	}
 	
 	protected JRibbonBand getPreferencesAndSearchBand() {
 		JRibbonBand preferencesBand = new JRibbonBand("View Actions",
 				new SimpleResizableIcon(RibbonElementPriority.TOP, 48, 48));
 		preferencesBand.setResizePolicies(CoreRibbonResizePolicies
 				.getCorePoliciesRestrictive(preferencesBand));
 
 		preferencesBand.startGroup();
 		JCommandButton naming = new JCommandButton("Naming Preferences",
 				getIcon("images/Preferences.png"));
 		naming.setActionKeyTip("Y");
 		naming.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				JDialog d = new NamingDialog(parent);
 		        d.setLocation(50,50);
 		        d.pack();
 		        d.setVisible(true);
 			}
 		});
 				
 		preferencesBand.addCommandButton(naming,
 				RibbonElementPriority.TOP);
 
 		JCommandButton search = new JCommandButton("Seach in Drawing", getIcon("images/Search.png"));
 		search.setActionKeyTip("E");
 		search.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				JDialog searchDiag = new SearchDialog(parent);
 				searchDiag.setLocation(50,50);
 				searchDiag.setVisible(true);
 			}
 		});
 				
 		preferencesBand.addCommandButton(search,
 				RibbonElementPriority.TOP);
 		preferencesBand.setResizePolicies(CoreRibbonResizePolicies.getCorePoliciesNone(preferencesBand));
 		return preferencesBand;
 	}
 		
 	protected JRibbonBand getColoringBand() {
 		
 		JRibbonBand coloringBand = new JRibbonBand("Problems",
 				new SimpleResizableIcon(RibbonElementPriority.TOP, 48, 48));
 		colBoxL = new ColorBox();
 		colBoxL.addItemListener(new ItemListener() {
             public void itemStateChanged(final ItemEvent e) {
                 colorBoxItemStateChanged(e);
             }
         });
 		
 		colBoxP = new ColorBox();
 		colBoxP.addItemListener(new ItemListener() {
             public void itemStateChanged(final ItemEvent e) {
                 colorBoxItemStateChanged(e);
             }
         });
 		
 		colBoxNpc = new ColorBox();
 		colBoxNpc.addItemListener(new ItemListener() {
             public void itemStateChanged(final ItemEvent e) {
                 colorBoxItemStateChanged(e);
             }
         });
 		
 		colBoxI = new ColorBox();
 		colBoxI.addItemListener(new ItemListener() {
             public void itemStateChanged(final ItemEvent e) {
                 colorBoxItemStateChanged(e);
             }
         });
 		
 		colBoxU = new ColorBox();
 		colBoxU.addItemListener(new ItemListener() {
             public void itemStateChanged(final ItemEvent e) {
                 colorBoxItemStateChanged(e);
             }
         });
 			
         colBoxL.setSelectedIndex(5);
 		colBoxP.setSelectedIndex(6);
 		colBoxNpc.setSelectedIndex(11);
 		colBoxI.setSelectedIndex(12);
 		colBoxU.setSelectedIndex(14);
 		
 		
 		JRibbonComponent colClasses = new JRibbonComponent(null, "Color for Linear		   		   	", colBoxL);
 		colClasses.setKeyTip("AI");
 		colClasses.setEnabled(true);
 		colClasses.setResizingAware(true);
 		colClasses.setHorizontalAlignment(HorizontalAlignment.FILL);
 		coloringBand.addRibbonComponent(colClasses);
 		
 		colClasses = new JRibbonComponent(null, "Color for Polynomial	 	", colBoxP);
 		colClasses.setKeyTip("AI");
 		colClasses.setEnabled(true);
 		colClasses.setResizingAware(true);
 		colClasses.setHorizontalAlignment(HorizontalAlignment.FILL);
 		coloringBand.addRibbonComponent(colClasses);
 		
 		
 		colClasses = new JRibbonComponent(null, "Color for NP-Complete ", colBoxNpc);
 		colClasses.setKeyTip("AI");
 		colClasses.setEnabled(true);
 		colClasses.setResizingAware(true);
 		colClasses.setHorizontalAlignment(HorizontalAlignment.FILL);
 		coloringBand.addRibbonComponent(colClasses);
 		
 		colClasses = new JRibbonComponent(null, "Color for Intermediate", colBoxI);
 		colClasses.setKeyTip("AI");
 		colClasses.setEnabled(true);
 		colClasses.setResizingAware(true);
 		colClasses.setHorizontalAlignment(HorizontalAlignment.FILL);
 		coloringBand.addRibbonComponent(colClasses);
 		
 		colClasses = new JRibbonComponent(null, "Color for Unknown     ", colBoxU);
 		colClasses.setKeyTip("AI");
 		colClasses.setEnabled(true);
 		colClasses.setResizingAware(true);
 		colClasses.setHorizontalAlignment(HorizontalAlignment.FILL);
 		coloringBand.addRibbonComponent(colClasses);
 		
 		return coloringBand;
 	}
 
 	protected JRibbonBand getColoringPackageBand() {
 	
 		JRibbonBand coloringBand = new JRibbonBand("Coloring Packages",
 				new SimpleResizableIcon(RibbonElementPriority.TOP, 48, 48));
 		colPackage = new JComboBox<String>(
 				new String[] { "Standard", 
 						"Color blind (monochrome)"});
 		colPackage.addActionListener(this);
 		
 		
 		
 		JRibbonComponent colClasses = new JRibbonComponent(null, "Coloring Package", colPackage);
 		colClasses.setKeyTip("AI");
 		colClasses.setEnabled(true);
 		colClasses.setResizingAware(true);
 		colClasses.setHorizontalAlignment(HorizontalAlignment.FILL);
 		coloringBand.addRibbonComponent(colClasses);
 		return coloringBand;
 }
 	
 	protected JRibbonBand getProblemBand() {
 		JRibbonBand problemsBand = new JRibbonBand("Problems",
 				new SimpleResizableIcon(RibbonElementPriority.TOP, 48, 48));
 
 		boBox = new JComboBox<String>(
 				new String[] { "Recognition", 
 						"Treewidth", 
 						"Cliquewidth",
 						"Cliquewidth expression",
 						"Weighted independent set",
 						"Independent set",
 						"Weighted clique",
 						"Clique",
 						"Domination",
 						"Colourability",
 						"Clique cover",
 						"3-Colourability",
 						"Cutwidth",
 						"Hamiltonian circle",
 						"Hamiltonian path",
 						"Weighted feedback vertex set",
 						"Feedback vertex set"});
 		boBox.addActionListener(this);
 		JRibbonComponent boClasses = new JRibbonComponent(null, "Boundary/ Open Classes", boBox);
 		
 		boClasses.setKeyTip("AI");
 		boClasses.setEnabled(true);
 		boClasses.setResizingAware(true);
 		boClasses.setHorizontalAlignment(HorizontalAlignment.FILL);
 		problemsBand.addRibbonComponent(boClasses);
 		
 		
 		probBox = new JComboBox<String>(
 				new String[] { "None", 
 						"Recognition", 
 						"Treewidth",
 						"Cliquewidth",
 						"Cliquewidth expression",
 						"Weighted independent set",
 						"Independent set",
 						"Weighted clique",
 						"Clique",
 						"Domination",
 						"Colourability",
 						"Clique cover",
 						"3-Colourability",
 						"Cutwidth",
 						"Hamiltonian circle",
 						"Hamiltonian path",
 						"Weighted feedback vertex set",
 						"Feedback vertex set"});
 		probBox.addActionListener(this);
 		
 		JRibbonComponent probClasses = new JRibbonComponent(null, "Problems", probBox);
 		probClasses.setKeyTip("AI");
 		probClasses.setEnabled(true);
 		probClasses.setResizingAware(true);
 		probClasses.setHorizontalAlignment(HorizontalAlignment.FILL);
 		problemsBand.addRibbonComponent(probClasses);
 		
 		return problemsBand;
 
 	}
 	
 	
 	protected JRibbonBand getHelpBand() {
 		JRibbonBand helpBand = new JRibbonBand("Help",
 				new SimpleResizableIcon(RibbonElementPriority.TOP, 48, 48));
 		helpBand.setResizePolicies(CoreRibbonResizePolicies
 				.getCorePoliciesRestrictive(helpBand));
 
 		helpBand.startGroup();
 		JCommandButton smallGraph = new JCommandButton("Small Graphs", getIcon("images/favicon.png"));
 		smallGraph.setActionKeyTip("S");
 		smallGraph.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
				parent.loader.showDocument("smallgraphs.html");
 			}
 		});
 		helpBand.addCommandButton(smallGraph,
 				RibbonElementPriority.TOP);
 		
 		JCommandButton help = new JCommandButton("Help", getIcon("images/Help.png"));
 		help.setActionKeyTip("H");
 		help.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
				parent.loader.showDocument("help.html");
 			}
 		});
 		helpBand.addCommandButton(help,
 				RibbonElementPriority.TOP);
 
 		JCommandButton about = new JCommandButton("About", getIcon("images/About.png"));
 		about.setActionKeyTip("A");
 		about.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				JDialog select = new AboutDialog(parent);
 	            select.setLocation(50, 50);
 	            select.setVisible(true);
 			}
 		});
 		helpBand.addCommandButton(about,
 				RibbonElementPriority.TOP);
 		helpBand.setResizePolicies(CoreRibbonResizePolicies.getCorePoliciesNone(helpBand));
 		return helpBand;
 	}
 	
 	protected JRibbonBand getLayoutBand() {
 		JRibbonBand layoutBand = new JRibbonBand("Layouting",
 				new SimpleResizableIcon(RibbonElementPriority.TOP, 48, 48));
 		layoutBand.setResizePolicies(CoreRibbonResizePolicies
 				.getCorePoliciesRestrictive(layoutBand));
 
 		layoutBand.startGroup();
 		JCommandButton layGraph = new JCommandButton("Update Graph Layout", getIcon("images/Layout.png"));
 		layGraph.setActionKeyTip("L");
 		layGraph.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				App.getDrawingService(parent).doLayout();
 			}
 		});
 		layoutBand.addCommandButton(layGraph,
 				RibbonElementPriority.TOP);
 		
 		layoutBand.setResizePolicies(CoreRibbonResizePolicies.getCorePoliciesNone(layoutBand));
 		return layoutBand;
 	}
 	
 	public void configureRibbon() {
 
 		/*
 		 * ExportBar Alle ExportOptionen hier
 		 */
 		JRibbonBand exportBand = this.getExportBand();
 		//JRibbonBand quickExportBand = this.getQuickExportBand();
 		//JRibbonBand quickExportPrefBand = this.getQuickExportPrefBand();
 		RibbonTask fileTask = new RibbonTask("Extras", exportBand);//, quickExportBand, quickExportPrefBand);
 		fileTask.setKeyTip("F");
 
 		/*
 		 * ViewBand Alle ViewOptionen hier
 		 */
 		JRibbonBand unproperBand = this.getMarkUnproperBand();
 		JRibbonBand preferencesAndSearchBand = this.getPreferencesAndSearchBand();
 		JRibbonBand layoutBand = this.getLayoutBand();;
 		RibbonTask viewTask = new RibbonTask("View", unproperBand, preferencesAndSearchBand, layoutBand);
 		/*viewTask
 				.setResizeSequencingPolicy(new CoreRibbonResizeSequencingPolicies.CollapseFromLast(
 						viewTask));*/
 		viewTask.setKeyTip("V");
 		
 		JRibbonBand graphClasses = this.getGraphClassesBand();
 		//JRibbonBand quickExportBand = this.getQuickExportBand();
 		//JRibbonBand quickExportPrefBand = this.getQuickExportPrefBand();
 		RibbonTask gcTask = new RibbonTask("Graph Classes", graphClasses);//, quickExportBand, quickExportPrefBand);
 		
 		JRibbonBand problemTask = this.getProblemBand();
 		JRibbonBand colorTask = this.getColoringBand();
 		JRibbonBand colorPackTask = this.getColoringPackageBand();
 		//JRibbonBand quickExportBand = this.getQuickExportBand();
 		//JRibbonBand quickExportPrefBand = this.getQuickExportPrefBand();
 		RibbonTask probTask = new RibbonTask("Problems", problemTask, colorTask, colorPackTask);//, quickExportBand, quickExportPrefBand);
 		
 		JRibbonBand helpClasses = this.getHelpBand();
 		//JRibbonBand quickExportBand = this.getQuickExportBand();
 		//JRibbonBand quickExportPrefBand = this.getQuickExportPrefBand();
 		RibbonTask helpTask = new RibbonTask("Help", helpClasses);//, quickExportBand, quickExportPrefBand);
 
 		parent.getRibbon().addTask(gcTask);
 		parent.getRibbon().addTask(probTask);
 		parent.getRibbon().addTask(viewTask);
 		parent.getRibbon().addTask(fileTask);
 		parent.getRibbon().addTask(helpTask);
 
 		parent.getRibbon().configureHelp(new SimpleResizableIcon(RibbonElementPriority.TOP, 10, 10),
 				new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
						JOptionPane.showMessageDialog(parent,
								"Help button clicked");
 					}
 				});
 
 		
 		// application menu
 		configureApplicationMenu();
 
 		JPanel controlPanel = new JPanel();
 		controlPanel.setBorder(new EmptyBorder(20, 0, 0, 5));
 		FormLayout lm = new FormLayout("right:pref, 4dlu, fill:pref:grow", "");
 		DefaultFormBuilder builder = new DefaultFormBuilder(lm, controlPanel);
 		parent.getRibbon().add(controlPanel, BorderLayout.EAST);
 	
 	}
 	
 protected void configureApplicationMenu() {
 		
 		RibbonApplicationMenuEntryPrimary newWindow = new RibbonApplicationMenuEntryPrimary(
 				getIcon("images/NewWindow.png"), "New Window", new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						try {
 							new ISGCIMainFrame(parent.loader);
 						} catch (Exception e1) {
 							// TODO Auto-generated catch block
 							e1.printStackTrace();
 						}
 					}
 				}, CommandButtonKind.ACTION_ONLY);
 		newWindow.setActionKeyTip("N");
 
 		RibbonApplicationMenuEntryPrimary exitWindow = new RibbonApplicationMenuEntryPrimary(
 				getIcon("images/Exit.png"), "Exit", new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						parent.closeWindow();
 					}
 				}, CommandButtonKind.ACTION_ONLY);
 		exitWindow.setActionKeyTip("X");
 
 		
 
 		RibbonApplicationMenu applicationMenu = new RibbonApplicationMenu();
 		applicationMenu.addMenuEntry(newWindow);
 		applicationMenu.addMenuEntry(exitWindow);		
 
 		parent.getRibbon().setApplicationMenu(applicationMenu);
 
 		RichTooltip appMenuRichTooltip = new RichTooltip();
 		appMenuRichTooltip.setTitle("Tooltip 1");
 		appMenuRichTooltip.addDescriptionSection("Tooltip 2");
 		
 		try {
 			appMenuRichTooltip
 					.setMainImage(ImageIO
 							.read(ISGCIMainFrame.class.getClassLoader().getResource("images/appmenubutton-tooltip-main.png")));
 			appMenuRichTooltip.setFooterImage(ImageIO
 					.read(ISGCIMainFrame.class
 							.getClassLoader().getResource("images/Help.png")));
 		} catch (IOException ioe) {
 			System.out.println(ioe.getMessage());
 		}
 		appMenuRichTooltip.addFooterSection("Foot Tooltip");
 		parent.getRibbon().setApplicationMenuRichTooltip(appMenuRichTooltip);
 		parent.getRibbon().setApplicationMenuKeyTip("F");
 	}
 
 class ColorBox extends JComboBox{
     private final Icon[] COLOR_ICONS;
     public final String LABELS[] = {
         "BLACK","BLUE","CYAN","DARK_GRAY", "GRAY", "GREEN", "DARK_GREEN","LIGHT_GRAY",
         "MAGENTA","ORANGE","PINK","RED", "BRIGHT_RED", "DARK_RED","WHITE","YELLOW", "LIGHTER_GRAY"
     };
     public final Color COLORS[] = {
         Color.BLACK,Color.BLUE,Color.CYAN,Color.DARK_GRAY,Color.GRAY,Color.GREEN, Color.green.darker(), Color.LIGHT_GRAY,
         Color.MAGENTA,Color.ORANGE,Color.PINK,Color.RED, Color.RED.brighter(), Color.red.darker(),Color.WHITE,Color.YELLOW, Color.LIGHT_GRAY.brighter()
     };
     
     
     
     public ColorBox(){
         super();
         //Load the COLOR_ICONS and create an array of indexes:
         COLOR_ICONS = new Icon[LABELS.length];
         final Integer[] INT_ARRAY = new Integer[LABELS.length];
         for (int i = 0; i < LABELS.length; i++) {
             INT_ARRAY[i] = new Integer(i);
             COLOR_ICONS[i] = new ColorIcon(COLORS[i], new Dimension(30, 15));
         }
         setModel(new DefaultComboBoxModel(INT_ARRAY));
         setRenderer(new ComboBoxRenderer());
     }
     class ComboBoxRenderer extends JLabel implements ListCellRenderer {
         public ComboBoxRenderer() {setOpaque(true);}
         public Component getListCellRendererComponent(
                 final JList list, final Object value, final int index,
                 final boolean isSelected, final boolean cellHasFocus) {
             setBackground(list.getBackground());
             setForeground(list.getForeground());
             if (isSelected) {
                 setBackground(list.getSelectionBackground());
                 setForeground(list.getSelectionForeground());
             }
             //Set the icon and text:
             int selectedIndex = ((Integer)value).intValue();
             setIcon(COLOR_ICONS[selectedIndex]);
             setText(LABELS[selectedIndex]);
             return this;
         }
     }
     class ColorIcon implements Icon {
         final private Color color;
         final private Dimension size;
         public ColorIcon(final Color color, final Dimension size) {
             this.color = color;
             this.size = size;
         }
         public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
             g.setColor(color);
             g.fillRect(x, y, getIconWidth(), getIconHeight());
         }
         public int getIconWidth() {return size.width;}
         public int getIconHeight() {return size.height;}
     }
 }
 
 
 private void colorBoxItemStateChanged(final ItemEvent e){
     if(e.getStateChange() == ItemEvent.DESELECTED) return;
     int selectedIndexL = colBoxL.getSelectedIndex();
     int selectedIndexP = colBoxP.getSelectedIndex();
     int selectedIndexI = colBoxI.getSelectedIndex();
     int selectedIndexNpc = colBoxNpc.getSelectedIndex();
     int selectedIndexU = colBoxU.getSelectedIndex();
         
     ViewManager viewManager = App.getViewManager(parent);	
     for (GraphView gv : viewManager.getCurrentViews()) {
         for (NodeView v : gv.getNodes()) {
            v.setColoring(colBoxL.COLORS[selectedIndexL], colBoxP.COLORS[selectedIndexP], colBoxI.COLORS[selectedIndexI], colBoxNpc.COLORS[selectedIndexNpc], colBoxU.COLORS[selectedIndexU]);    
         }
     }
     viewManager.setComplexityColors();
     viewManager.updateProblem();
     
     
 }
 
 public void actionPerformed(ActionEvent event) {
 	JComboBox cb = (JComboBox)event.getSource();
 	
 	if (cb.equals(boBox)) {
 		JDialog open=new OpenProblemDialog(parent,				
                 boBox.getSelectedItem().toString());
         open.setLocation(50, 50);
         open.setVisible(true);
 	}
 	
 	if (cb.equals(probBox)) {
 		App.getViewManager(parent).setProblem(App.DataProvider.getProblem(probBox.getSelectedItem().toString()));
 	}
 	
 	if (cb.equals(colPackage)) {
 		System.out.println(colPackage.getSelectedItem().toString());
 		if (colPackage.getSelectedItem().toString() == "Standard") {
 			colBoxL.setSelectedIndex(5);
 			colBoxP.setSelectedIndex(6);
 			colBoxNpc.setSelectedIndex(11);
 			colBoxI.setSelectedIndex(12);
 			colBoxU.setSelectedIndex(14);
 		} else {
 			colBoxL.setSelectedIndex(3);
 			colBoxP.setSelectedIndex(4);
 			colBoxNpc.setSelectedIndex(7);
 			colBoxI.setSelectedIndex(16);
 			colBoxU.setSelectedIndex(14);
 		}
 	}
 }
 }
