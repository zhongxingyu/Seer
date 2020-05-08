 package com.display;
 
 import javax.swing.ImageIcon;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 
 import java.awt.Color;
 import java.awt.FlowLayout;
 import java.awt.Image;
 
 import models.Arteriole;
 import models.Artery;
 import models.Capillary;
 import models.ElasticTube;
 import models.FirstArtery;
 import models.FourthVentricle;
 import models.Hemisphere;
 import models.SAS;
 import models.SimpleVariable;
 import models.SpinalCord;
 import models.ThirdVentricle;
 import models.Vein;
 import models.Veinule;
 import models.VenousSinus;
 import models.Ventricle;
 import net.miginfocom.swing.MigLayout;
 import javax.swing.JButton;
 
 import org.jscroll.widgets.JScrollInternalFrame;
 
 import params.WindowManager;
 import utils.SEButils;
 
 import com.display.images.IconLibrary;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 
 public class TubePanel extends JPanel {
 	public enum TubeClass{FirstArtery,Artery,Arteriole,Capillary,Veinule,Vein,VenousSinus, Ventricle,FourthVentricle,ThirdVentricle,SAS,SpinalCord};
 	public static boolean linkModeActivated = false;
 	public static JScrollInternalFrame linkModeActivatedSource = null;
 	private static final Color activatedColor = Color.orange;
 	private JScrollInternalFrame parentInternalFrame;
 	private ElasticTube tube;
 	private ConcurrentLinkedQueue<LineLink> lineLinks;
 	private TubeClass tubeType;
 
 	
 	
 	private JButton btnAddLink;
 	private JButton btnDisplayCharts;
 	private JButton btnRemovebloc;
 	private JButton btnEditinit;
 	private JPanel panel;
 	private JLabel lblLength;
 	private JTextField txtVallength;
 	private JLabel lblAlpha;
 	private JTextField textValAlpha;
 	private JLabel lblElastance;
 	private JTextField txtValelastance;
 	private JLabel lblArea;
 	private JTextField txtValarea;
 	private JLabel lblFin;
 	private JTextField txtValfin;
 	private JLabel lblFout;
 	private JTextField txtValfout;
 	private JLabel lblPressure;
 	private JTextField txtValpressure;
 	private JButton btnRestore;
 	
 	public TubePanel( ElasticTube tube, TubeClass type) {
 		setTube(tube);
         setLineLinks(new ConcurrentLinkedQueue<LineLink>());
         setTubeType(type);
         
 		setPreferredSize(new Dimension(235, 111));
 		setLayout(new MigLayout("", "[grow][grow][grow][grow]", "[62.00,grow][]"));
 		Image img = IconLibrary.LINKICON;
 		img = img.getScaledInstance(20, 20,  java.awt.Image.SCALE_SMOOTH); 
 		img = IconLibrary.EDITVARIABLESICON;
 		img = img.getScaledInstance(20, 20,  java.awt.Image.SCALE_SMOOTH); 
 		
 		btnAddLink = new JButton();
 		img = IconLibrary.LINKICON;
 		img = img.getScaledInstance(20, 20,  java.awt.Image.SCALE_SMOOTH); 
 		
 		panel = new JPanel();
 		add(panel, "cell 0 0 4 1,grow");
 		panel.setLayout(new MigLayout("", "[grow,left][38.00,left][grow,left][grow,left][grow,left][grow,left][grow][]", "[center][center]"));
 		
 		lblLength = new JLabel(ElasticTube.LENGTH_LABEL);
 		panel.add(lblLength, "cell 0 0,alignx trailing");
 		
 		txtVallength = new JTextField();
 		txtVallength.setEditable(false);
 		txtVallength.setText(""+tube.getLength().getValue());
 		panel.add(txtVallength, "cell 1 0,alignx left");
 		txtVallength.setColumns(3);
 		
 		lblAlpha = new JLabel(ElasticTube.ALPHA_LABEL);
 		panel.add(lblAlpha, "cell 2 0,alignx left");
 		
 		textValAlpha = new JTextField(""+tube.getAlpha().getValue());
 		textValAlpha.setEditable(false);
 		panel.add(textValAlpha, "cell 3 0,alignx left");
 		textValAlpha.setColumns(3);
 		
 		lblElastance = new JLabel(ElasticTube.ELASTANCE_LABEL);
 		panel.add(lblElastance, "cell 4 0,alignx left");
 		
 		txtValelastance = new JTextField(""+tube.getElastance().getValue());
 		txtValelastance.setEditable(false);
 		panel.add(txtValelastance, "cell 5 0,alignx left");
 		txtValelastance.setColumns(3);
 		
 		lblPressure = new JLabel(ElasticTube.PRESSURE_LABEL);
 		panel.add(lblPressure, "cell 6 0,alignx left");
 		
 		txtValpressure = new JTextField(""+tube.getPressure().getValue());
 		txtValpressure.setEditable(false);
 		panel.add(txtValpressure, "cell 7 0,alignx left");
 		txtValpressure.setColumns(3);
 		
 		lblArea = new JLabel(ElasticTube.AREA_LABEL);
 		panel.add(lblArea, "cell 0 1,alignx trailing");
 		
 		txtValarea = new JTextField(""+tube.getArea().getValue());
 		txtValarea.setEditable(false);
 		panel.add(txtValarea, "flowx,cell 1 1,alignx left");
 		txtValarea.setColumns(3);
 		
 		lblFin = new JLabel(ElasticTube.FLOWIN_LABEL);
 		panel.add(lblFin, "cell 2 1,alignx left");
 		
 		txtValfin = new JTextField(""+tube.getFlowin().getValue());
 		txtValfin.setEditable(false);
 		panel.add(txtValfin, "cell 3 1,alignx left");
 		txtValfin.setColumns(3);
 		
 		lblFout = new JLabel(ElasticTube.FLOWOUT_LABEL);
 		panel.add(lblFout, "cell 4 1,alignx left");
 		
 		txtValfout = new JTextField(""+tube.getFlowout().getValue());
 		txtValfout.setEditable(false);
 		panel.add(txtValfout, "cell 5 1,alignx left");
 		txtValfout.setColumns(3);
 		
 
 		btnAddLink.setIcon(new ImageIcon(img));
 		add(btnAddLink, "cell 0 1,grow");
 		btnRestore = new JButton();
 		img = IconLibrary.RESTOREICON;
 		img = img.getScaledInstance(20, 20,  java.awt.Image.SCALE_SMOOTH); 
 		panel.add(btnRestore, "cell 6 1 2 1,grow");
 		btnRestore.setIcon(new ImageIcon(img));
 		
 		btnDisplayCharts = new JButton();
 		img = IconLibrary.CHARTICON;
 		btnDisplayCharts.setEnabled(false);
 		img = img.getScaledInstance(20, 20,  java.awt.Image.SCALE_SMOOTH); 
 		btnDisplayCharts.setIcon(new ImageIcon(img));
 		add(btnDisplayCharts, "cell 1 1,grow");
 		img = IconLibrary.RMBLOCKICON;
 		img = img.getScaledInstance(20, 20,  java.awt.Image.SCALE_SMOOTH); 
 		
 		btnRemovebloc = new JButton();
 		btnRemovebloc.setIcon(new ImageIcon(img));
 		add(btnRemovebloc, "cell 2 1,grow");
 		if(getTubeType() == TubeClass.VenousSinus || getTubeType() == TubeClass.FirstArtery)
 			btnRemovebloc.setEnabled(false);
 		
 		btnEditinit = new JButton();
 		add(btnEditinit, "cell 3 1,grow");
 		img = IconLibrary.EDITVARIABLESICON;
 		img = img.getScaledInstance(20, 20,  java.awt.Image.SCALE_SMOOTH); 
 		btnEditinit.setIcon(new ImageIcon(img));
 		
 		
 		
 		// Listeners
 		btnDisplayCharts.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				ArrayList<SimpleVariable> list = new ArrayList<SimpleVariable>();
 				list.add(getTube().getArea());
 				list.add(getTube().getPressure());
 				list.add(getTube().getFlowin());
 				list.add(getTube().getFlowout());
 				WindowManager.MAINWINDOW.getPlotPanel().setVarToDisplay(list);
 				WindowManager.MAINWINDOW.getTabbedPane().setSelectedIndex(2);
 			}
 		});
 		btnAddLink.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if(linkModeActivated && linkModeActivatedSource!=null){
 					if(linkModeActivatedSource != parentInternalFrame){
 						addLineLinkAsChild(new LineLink(linkModeActivatedSource, parentInternalFrame));
 					}
 					resetBtnAddLink();
 					linkModeActivated = false;
 					linkModeActivatedSource = null;
 					WindowManager.MAINWINDOW.getToolBarArchitectLeft().setEnabled(true);
 					WindowManager.MAINWINDOW.getToolBarArchitectRight().setEnabled(true);
 					for(JScrollInternalFrame jsf : parentInternalFrame.getJsDesktopPane().getInternalFrames()){
 						jsf.getTubePanel().resetBtnAddLink();
 					}
 				}else{
 					WindowManager.MAINWINDOW.getToolBarArchitectLeft().setEnabled(false);
 					WindowManager.MAINWINDOW.getToolBarArchitectRight().setEnabled(false);
 					btnRemovebloc.setEnabled(false);
 					btnDisplayCharts.setEnabled(false);
 					btnEditinit.setEnabled(false);
 					linkModeActivatedSource = parentInternalFrame;
 					linkModeActivated = true;
 					btnAddLink.setBackground(Color.cyan);
 					for(JScrollInternalFrame jsf : parentInternalFrame.getJsDesktopPane().getInternalFrames()){
 						System.out.println(jsf);
 						TubePanel ltubep = jsf.getTubePanel();
 						if(jsf != parentInternalFrame){
 							if(isLinkedWith(jsf)){
 								ltubep.deactivateLinkMode();
 								continue;
 							}
 							
 							if((ltubep.getTube().getHemisphere() == Hemisphere.LEFT || ltubep.getTube().getHemisphere() == Hemisphere.RIGHT)
 									&& (getTube().getHemisphere() == Hemisphere.LEFT || getTube().getHemisphere() == Hemisphere.RIGHT)
 									&& ltubep.getTube().getHemisphere() != getTube().getHemisphere()){
 								ltubep.deactivateLinkMode();
 								continue;
 							}
 							if(tubeType == ltubep.getTubeType() && (tubeType == TubeClass.Artery || tubeType == TubeClass.Vein)){
 								ltubep.activateLinkMode();
 							}else{
 								if(tubeType == TubeClass.Artery && ltubep.getTubeType() == TubeClass.FirstArtery){
 									ltubep.deactivateLinkMode();
 									continue;
 								}
 								if(ltubep.getTube().getHemisphere() == getTube().getHemisphere() || ltubep.getTube().getHemisphere() == Hemisphere.BOTH || ( tubeType == TubeClass.FirstArtery)){
 									switch(tubeType){
 									case FirstArtery:
 										if(ltubep.getTubeType() == TubeClass.Artery)
 											ltubep.activateLinkMode();
 										else
 											ltubep.deactivateLinkMode();
 										break;
 									case Artery:
 										if(ltubep.getTubeType() == TubeClass.Arteriole)
 											ltubep.activateLinkMode();
 										else
 											ltubep.deactivateLinkMode();
 										break;
 									case Arteriole:
 										if(ltubep.getTubeType() == TubeClass.Capillary)
 											ltubep.activateLinkMode();
 										else
 											ltubep.deactivateLinkMode();
 										break;
 									case Capillary:
 										if(ltubep.getTubeType() == TubeClass.Veinule)
 											ltubep.activateLinkMode();
 										else
 											ltubep.deactivateLinkMode();
 										break;
 									case Veinule:
 										if(ltubep.getTubeType() == TubeClass.Vein)
 											ltubep.activateLinkMode();
 										else
 											ltubep.deactivateLinkMode();
 										break;
 									case Vein:
 										if(ltubep.getTubeType() == TubeClass.VenousSinus)
 											ltubep.activateLinkMode();
 										else
 											ltubep.deactivateLinkMode();
 										break;
 									default:
 										break;
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 
 
 
 
 		});
 		btnRestore.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				switch(getTubeType()){
 				case FirstArtery:
 					txtVallength.setText(""+FirstArtery.DEFAULT_LENGTH);
 					textValAlpha.setText(""+FirstArtery.DEFAULT_ALPHA);
 					txtValelastance.setText(""+FirstArtery.DEFAULT_ELASTANCE);
 					txtValarea.setText(""+FirstArtery.DEFAULT_AREA);
 					txtValfin.setText(""+FirstArtery.DEFAULT_FLOWIN);
 					txtValfout.setText(""+FirstArtery.DEFAULT_FLOWOUT);
 					txtValpressure.setText(""+FirstArtery.DEFAULT_PRESSURE);
 					break;
 				case Artery:
 					txtVallength.setText(""+Artery.DEFAULT_LENGTH);
 					textValAlpha.setText(""+Artery.DEFAULT_ALPHA);
 					txtValelastance.setText(""+Artery.DEFAULT_ELASTANCE);
 					txtValarea.setText(""+Artery.DEFAULT_AREA);
 					txtValfin.setText(""+Artery.DEFAULT_FLOWIN);
 					txtValfout.setText(""+Artery.DEFAULT_FLOWOUT);
 					txtValpressure.setText(""+Artery.DEFAULT_PRESSURE);
 					break;
 				case Arteriole:
 					txtVallength.setText(""+Arteriole.DEFAULT_LENGTH);
 					textValAlpha.setText(""+Arteriole.DEFAULT_ALPHA);
 					txtValelastance.setText(""+Arteriole.DEFAULT_ELASTANCE);
 					txtValarea.setText(""+Arteriole.DEFAULT_AREA);
 					txtValfin.setText(""+Arteriole.DEFAULT_FLOWIN);
 					txtValfout.setText(""+Arteriole.DEFAULT_FLOWOUT);
 					txtValpressure.setText(""+Arteriole.DEFAULT_PRESSURE);
 					break;
 				case Capillary:
 					txtVallength.setText(""+Capillary.DEFAULT_LENGTH);
 					textValAlpha.setText(""+Capillary.DEFAULT_ALPHA);
 					txtValelastance.setText(""+Capillary.DEFAULT_ELASTANCE);
 					txtValarea.setText(""+Capillary.DEFAULT_AREA);
 					txtValfin.setText(""+Capillary.DEFAULT_FLOWIN);
 					txtValfout.setText(""+Capillary.DEFAULT_FLOWOUT);
 					txtValpressure.setText(""+Capillary.DEFAULT_PRESSURE);
 					break;
 				case Veinule:
 					txtVallength.setText(""+Veinule.DEFAULT_LENGTH);
 					textValAlpha.setText(""+Veinule.DEFAULT_ALPHA);
 					txtValelastance.setText(""+Veinule.DEFAULT_ELASTANCE);
 					txtValarea.setText(""+Veinule.DEFAULT_AREA);
 					txtValfin.setText(""+Veinule.DEFAULT_FLOWIN);
 					txtValfout.setText(""+Veinule.DEFAULT_FLOWOUT);
 					txtValpressure.setText(""+Veinule.DEFAULT_PRESSURE);
 					break;
 				case Vein:
 					txtVallength.setText(""+Vein.DEFAULT_LENGTH);
 					textValAlpha.setText(""+Vein.DEFAULT_ALPHA);
 					txtValelastance.setText(""+Vein.DEFAULT_ELASTANCE);
 					txtValarea.setText(""+Vein.DEFAULT_AREA);
 					txtValfin.setText(""+Vein.DEFAULT_FLOWIN);
 					txtValfout.setText(""+Vein.DEFAULT_FLOWOUT);
 					txtValpressure.setText(""+Vein.DEFAULT_PRESSURE);
 					break;
 				case VenousSinus:
 					txtVallength.setText(""+VenousSinus.DEFAULT_LENGTH);
 					textValAlpha.setText(""+VenousSinus.DEFAULT_ALPHA);
 					txtValelastance.setText(""+VenousSinus.DEFAULT_ELASTANCE);
 					txtValarea.setText(""+VenousSinus.DEFAULT_AREA);
 					txtValfin.setText(""+VenousSinus.DEFAULT_FLOWIN);
 					txtValfout.setText(""+VenousSinus.DEFAULT_FLOWOUT);
 					txtValpressure.setText(""+VenousSinus.DEFAULT_PRESSURE);
 					break;
 				case Ventricle:
 					txtVallength.setText(""+Ventricle.DEFAULT_LENGTH);
 					textValAlpha.setText(""+Ventricle.DEFAULT_ALPHA);
 					txtValelastance.setText(""+Ventricle.DEFAULT_ELASTANCE);
 					txtValarea.setText(""+Ventricle.DEFAULT_AREA);
 					txtValfin.setText(""+Ventricle.DEFAULT_FLOWIN);
 					txtValfout.setText(""+Ventricle.DEFAULT_FLOWOUT);
 					txtValpressure.setText(""+Ventricle.DEFAULT_PRESSURE);
 					break;
 				case FourthVentricle:
 					txtVallength.setText(""+FourthVentricle.DEFAULT_LENGTH);
 					textValAlpha.setText(""+FourthVentricle.DEFAULT_ALPHA);
 					txtValelastance.setText(""+FourthVentricle.DEFAULT_ELASTANCE);
 					txtValarea.setText(""+FourthVentricle.DEFAULT_AREA);
 					txtValfin.setText(""+FourthVentricle.DEFAULT_FLOWIN);
 					txtValfout.setText(""+FourthVentricle.DEFAULT_FLOWOUT);
 					txtValpressure.setText(""+FourthVentricle.DEFAULT_PRESSURE);
 					break;
 				case ThirdVentricle:
 					txtVallength.setText(""+ThirdVentricle.DEFAULT_LENGTH);
 					textValAlpha.setText(""+ThirdVentricle.DEFAULT_ALPHA);
 					txtValelastance.setText(""+ThirdVentricle.DEFAULT_ELASTANCE);
 					txtValarea.setText(""+ThirdVentricle.DEFAULT_AREA);
 					txtValfin.setText(""+ThirdVentricle.DEFAULT_FLOWIN);
 					txtValfout.setText(""+ThirdVentricle.DEFAULT_FLOWOUT);
 					txtValpressure.setText(""+ThirdVentricle.DEFAULT_PRESSURE);
 					break;
 				case SAS:
 					txtVallength.setText(""+SAS.DEFAULT_LENGTH);
 					textValAlpha.setText(""+SAS.DEFAULT_ALPHA);
 					txtValelastance.setText(""+SAS.DEFAULT_ELASTANCE);
 					txtValarea.setText(""+SAS.DEFAULT_AREA);
 					txtValfin.setText(""+SAS.DEFAULT_FLOWIN);
 					txtValfout.setText(""+SAS.DEFAULT_FLOWOUT);
 					txtValpressure.setText(""+SAS.DEFAULT_PRESSURE);
 					break;
 				case SpinalCord:
 					txtVallength.setText(""+SpinalCord.DEFAULT_LENGTH);
 					textValAlpha.setText(""+SpinalCord.DEFAULT_ALPHA);
 					txtValelastance.setText(""+SpinalCord.DEFAULT_ELASTANCE);
 					txtValarea.setText(""+SpinalCord.DEFAULT_AREA);
 					txtValfin.setText(""+SpinalCord.DEFAULT_FLOWIN);
 					txtValfout.setText(""+SpinalCord.DEFAULT_FLOWOUT);
 					txtValpressure.setText(""+SpinalCord.DEFAULT_PRESSURE);
 					break;
 				}
 				
 				fillTubeInfo();
 			}
 		});
 		btnRemovebloc.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				SwingUtilities.invokeLater(new Runnable() {
 					
 					@Override
 					public void run() {
 						for(LineLink line : getLineLinks()){
 							line.delete();
 						}
 						getParentInternalFrame().getJsDesktopPane().removeInternalFrame(getParentInternalFrame());
 						getParentInternalFrame().dispose();
 					}
 				});
 				
 			}
 		});
 		btnEditinit.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				SwingUtilities.invokeLater(new Runnable() {
 					
 					@Override
 					public void run() {
 						if(btnEditinit.getBackground() == Color.cyan){
 							try{
 								fillTubeInfo();
 								setFieldsEditable(false);
 								btnEditinit.setBackground(null);
 							}catch(Exception e){
 								e.printStackTrace();
 							}
 						}else{
 							btnEditinit.setBackground(Color.cyan);
 							setFieldsEditable(true);
 						}
 						
 					}
 
 					
 				});
 				
 			}
 		});
 	}
 	
 	private boolean isLinkedWith(JScrollInternalFrame jsf) {
 		if(lineLinks.isEmpty())
 			return false;
 		for(LineLink line:lineLinks){
 			if(line.getParent() == jsf || line.getChild() == jsf){
 				return true;
 			}else{
 				if(line.getParent() != jsf && line.getChild() == getParentInternalFrame() && line.getParent().getTubePanel().getTube().getChildren().contains(jsf.getTubePanel().getTube())){
 					return true;
 				}else{
 					if(line.getParent() == getParentInternalFrame()){
 						if(iterativeCheckChildLink(jsf,line.getChild()))
 							return true;
 					}
 				}
 			}
 		}
 		for(LineLink line : lineLinks){
 			if(line.getChild() == getParentInternalFrame()){
 				if(iterativeCheckParentLink(jsf,line.getParent()))
 					return true;
 			}
 		}
 		return false;
 	}
 	
 	private boolean iterativeCheckParentLink(JScrollInternalFrame jsf, JScrollInternalFrame parent) {
 		if(parent == jsf)
 			return true;
 		
 		for(LineLink li2 : parent.getLineLinks()){
 			if(li2.getParent() == parent){
 				if(li2.getChild() == jsf)
 					return true;
 				continue;
 			}else{
 				
 				if(iterativeCheckParentLink(jsf,li2.getParent()))
 					return true;
 			}
 		}
 		return false;
 	}
 
 	private void fillTubeInfo(){
 		getTube().setLength(Float.parseFloat(txtVallength.getText()));
 		getTube().setAlpha(Float.parseFloat(textValAlpha.getText()));
 		getTube().setElastance(Float.parseFloat(txtValelastance.getText()));
 		getTube().setArea(Float.parseFloat(txtValarea.getText()));
 		getTube().setInitialArea(Float.parseFloat(txtValarea.getText()));
 		getTube().setFlowin(Float.parseFloat(txtValfin.getText()));
 		getTube().setFlowout(Float.parseFloat(txtValfout.getText()));
 		getTube().setPressure(Float.parseFloat(txtValpressure.getText()));
 	}
 	
 	/**
 	 * Refresh les valeurs affiche dans le panel a partir du tube
 	 */
 	public void refreshDisplayFromTube(){
 		txtVallength.setText(""+tube.getLength().getValue());
 		textValAlpha.setText(""+tube.getAlpha().getValue());
 		txtValelastance.setText(""+tube.getElastance().getValue());
 		txtValarea.setText(""+tube.getArea().getValue());
 		txtValfin.setText(""+tube.getFlowin().getValue());
 		txtValfout.setText(""+tube.getFlowout().getValue());
 		txtValpressure.setText(""+tube.getPressure().getValue());
 		btnDisplayCharts.setEnabled(tube.getArea().getValueInTime() != null || tube.getFlowin().getValueInTime() != null || tube.getFlowout().getValueInTime() != null
 				|| tube.getPressure().getValueInTime() != null);
 	}
 	private void setFieldsEditable(boolean b) {
 		txtVallength.setEditable(b);
 		textValAlpha.setEditable(b);
 		txtValelastance.setEditable(b);
 		txtValarea.setEditable(b);
 		txtValfin.setEditable(b);
 		txtValfout.setEditable(b);
 		txtValpressure.setEditable(b);
 	}
 	
 	private boolean iterativeCheckChildLink(JScrollInternalFrame jsf, JScrollInternalFrame child){
 		if(child == jsf)
 			return true;
 		
 		for(LineLink li2 : child.getLineLinks()){
 			if(li2.getChild() == child){
 				if(li2.getParent() == jsf)// A voir
 					return true;
 				continue;
 			}else{
 				if(iterativeCheckChildLink(jsf,li2.getChild()))
 					return true;
 			}
 		}
 		return false;
 	}
 	/**
 	 * Permet de mettre les boutons d'une couleur special en mode active
 	 */
 	public void activateLinkMode() {
 		btnAddLink.setBackground(activatedColor);
 		btnAddLink.setEnabled(true);
 		btnRemovebloc.setEnabled(false);
 		btnDisplayCharts.setEnabled(false);
 		btnEditinit.setEnabled(false);
 	}
 	public void deactivateLinkMode(){
 		btnAddLink.setBackground(null);
 		btnAddLink.setEnabled(false);
 		btnRemovebloc.setEnabled(false);
 		btnDisplayCharts.setEnabled(false);
 		btnEditinit.setEnabled(false);
 	}
 	public void resetBtnAddLink(){
 		btnAddLink.setBackground(null);
 		if(getTubeType() == TubeClass.VenousSinus)
 			btnAddLink.setEnabled(false);
 		else
 			btnAddLink.setEnabled(true);
 		if(getTubeType() != TubeClass.VenousSinus && getTubeType() != TubeClass.FirstArtery)
 			btnRemovebloc.setEnabled(true);
 		btnDisplayCharts.setEnabled(true);
 		btnEditinit.setEnabled(true);
 		getParentInternalFrame().getJsDesktopPane().repaint();
 	}
 	
 	/**
 	 * @return the lineLinks
 	 */
 	public ConcurrentLinkedQueue<LineLink> getLineLinks() {
 		return lineLinks;
 	}
 
 	/**
 	 * @param lineLinks the lineLinks to set
 	 */
 	public void setLineLinks(ConcurrentLinkedQueue<LineLink> lineLinks) {
 		this.lineLinks = lineLinks;
 	}
 	public void addLineLinkAsChild(LineLink line) {
		if(this.lineLinks.contains(line))
			return;
 		this.lineLinks.add(line);
 		tube.addParent(line.getParent().getTubePanel().getTube());
 	}
 	
 	public void addLineLinkAsParent(LineLink line){
		if(this.lineLinks.contains(line))
			return;
 		this.lineLinks.add(line);
 	}
 	
 	public void removeLineLinkAsParent(LineLink line){
 		if(!tube.removeChild(line.getChild().getTubePanel().getTube())){
 			System.err.println("Unable to remove child tube from "+tube.getName());
 		}
 		this.lineLinks.remove(line);
 	}
 	
 	public void removeLineLinkAsChild(LineLink line){
 		if(!tube.removeParent(line.getParent().getTubePanel().getTube())){
 			System.err.println("Unable to remove parent tube from "+tube.getName());
 		}
 		this.lineLinks.remove(line);
 	}
 	
 	/**
 	 * @return the tubeType
 	 */
 	public TubeClass getTubeType() {
 		return tubeType;
 	}
 	/**
 	 * @param tubeType the tubeType to set
 	 */
 	public void setTubeType(TubeClass tubeType) {
 		this.tubeType = tubeType;
 	}
 	/**
 	 * @return the tube
 	 */
 	public ElasticTube getTube() {
 		return tube;
 	}
 	/**
 	 * @param tube the tube to set
 	 */
 	public void setTube(ElasticTube tube) {
 		this.tube = tube;
 	}
 	/**
 	 * @return the parentInternalFrame
 	 */
 	public JScrollInternalFrame getParentInternalFrame() {
 		return parentInternalFrame;
 	}
 	/**
 	 * @param parentInternalFrame the parentInternalFrame to set
 	 */
 	public void setParentInternalFrame(JScrollInternalFrame parentInternalFrame) {
 		this.parentInternalFrame = parentInternalFrame;
 	}
 
 	public void delete() {
 		for(LineLink line : getLineLinks())
 			line.delete();
 	}
 
 	public void saveInfoTo(HashMap<String, String> paramsByTube,
 			HashSet<String> links) {
 		// on recupere les params du tube
 		String params = "";
 		params+=tube.getLength().getName()+SEButils.SAVE_PARAM_VALUE_SEPARATOR+tube.getLength().getValue();
 		params+=SEButils.SAVE_PARAM_SEPARATOR+tube.getAlpha().getName()+SEButils.SAVE_PARAM_VALUE_SEPARATOR+tube.getAlpha().getValue();
 		params+=SEButils.SAVE_PARAM_SEPARATOR+tube.getElastance().getName()+SEButils.SAVE_PARAM_VALUE_SEPARATOR+tube.getElastance().getValue();
 		params+=SEButils.SAVE_PARAM_SEPARATOR+tube.getArea().getName()+SEButils.SAVE_PARAM_VALUE_SEPARATOR+tube.getArea().getValue();
 		params+=SEButils.SAVE_PARAM_SEPARATOR+tube.getInitialArea().getName()+SEButils.SAVE_PARAM_VALUE_SEPARATOR+tube.getInitialArea().getValue();
 		params+=SEButils.SAVE_PARAM_SEPARATOR+tube.getFlowin().getName()+SEButils.SAVE_PARAM_VALUE_SEPARATOR+tube.getFlowin().getValue();
 		params+=SEButils.SAVE_PARAM_SEPARATOR+tube.getFlowout().getName()+SEButils.SAVE_PARAM_VALUE_SEPARATOR+tube.getFlowout().getValue();
 		params+=SEButils.SAVE_PARAM_SEPARATOR+tube.getPressure().getName()+SEButils.SAVE_PARAM_VALUE_SEPARATOR+tube.getPressure().getValue();
 		
 		paramsByTube.put(tube.getName(), params);
 		for(LineLink line : getLineLinks()){
 			if(line.getParent() == getParentInternalFrame()){
 				links.add(tube.getName()+SEButils.SAVE_LINK_SEPARATOR+line.getChild().getTubePanel().getTube().getName());
 				line.getChild().getTubePanel().saveInfoTo(paramsByTube, links);
 			}
 		}
 	}
 
 }
