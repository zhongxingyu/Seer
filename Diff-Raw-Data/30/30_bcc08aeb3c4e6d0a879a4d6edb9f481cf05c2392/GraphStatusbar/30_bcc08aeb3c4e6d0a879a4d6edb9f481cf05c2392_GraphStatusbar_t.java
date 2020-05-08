 /**
 *   ORCC rapid content creation for entertainment, education and media production
 *   Copyright (C) 2012 Michael Heinzelmann, Michael Heinzelmann IT-Consulting
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 package org.mcuosmipcuter.orcc.soundvis.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.Calendar;
 
 import javax.imageio.ImageIO;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JColorChooser;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JSpinner;
 import javax.swing.JTextField;
 import javax.swing.SpinnerModel;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import org.mcuosmipcuter.orcc.soundvis.Context;
 import org.mcuosmipcuter.orcc.soundvis.gui.GraphPanel.BGImageType;
 import org.mcuosmipcuter.orcc.soundvis.gui.listeners.FileDialogActionListener;
 
 
 /**
  * Status bar panel that works with a {@link GraphPanel}
  * @author Michael Heinzelmann
  */
 public class GraphStatusbar extends JPanel {
 
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * Sets up background, watermark and zoom components
 	 * @param graphicPanel the panel to work for
 	 */
 	public GraphStatusbar(final GraphPanel graphicPanel) {
 		
 		add(new JLabel("background: "));
 		ButtonGroup bgGroup = new ButtonGroup();
 		final JRadioButton optionColor = new JRadioButton("color");
 		optionColor.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				if(optionColor.isSelected()) {
 					graphicPanel.setBgImageType(BGImageType.COLOR);
 				}
 			}
 		});
 		add(optionColor);
 		final JButton bgColorButton = new JButton(" ");
 		bgColorButton.addActionListener(new ActionListener() {	
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				Color newColor = JColorChooser.showDialog(graphicPanel, getName(), null);
 				if(newColor != null) {
 					bgColorButton.setBackground(newColor);
 					graphicPanel.setBgColor(newColor);
					graphicPanel.setBgImageType(BGImageType.COLOR);
					optionColor.setSelected(true);
 				}
 			}});
 		add(bgColorButton);
 		final JRadioButton optionImage = new JRadioButton("image");
 		optionImage.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				if(optionImage.isSelected()) {
 					graphicPanel.setBgImageType(BGImageType.IMAGE);
 				}
 			}
 		});
 		add(optionImage);
 		JButton imageFile = new JButton("...");
 		FileDialogActionListener.CallBack callBack = new FileDialogActionListener.CallBack(){
 			@Override
 			public void fileSelected(File file) {
 				try {
 					BufferedImage bgImage = ImageIO.read(file);
 					graphicPanel.setBgImage(bgImage);
					graphicPanel.setBgImageType(BGImageType.IMAGE);
					optionImage.setSelected(true);
 				} catch (IOException ex) {
 					throw new RuntimeException(ex);
 				}
 				
 			}
 		};
 		imageFile.addActionListener(new FileDialogActionListener(graphicPanel, callBack, "set as background"));
 		add(imageFile);
 		final JRadioButton optionNone = new JRadioButton("none");
 		optionNone.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				if(optionNone.isSelected()) {
 					graphicPanel.setBgImageType(BGImageType.NONE);
 				}
 			}
 		});
 		add(optionNone);
 		if(graphicPanel.getBgImageType() == BGImageType.COLOR) {
 			optionColor.setSelected(true);
 		}
 		else if(graphicPanel.getBgImageType() == BGImageType.IMAGE) {
 			optionImage.setSelected(true);
 		}
 		else {
 			optionNone.setSelected(true);
 		}
 		bgGroup.add(optionColor);
 		bgGroup.add(optionImage);
 		bgGroup.add(optionNone);
 		
 		int year = Calendar.getInstance().get(Calendar.YEAR);
 		String user = System.getProperty("user.name");
 		String text = year + " " + user + " graphics by soundvis";
 		final JTextField watermarkText = new JTextField(text);
 
 		watermarkText.addFocusListener(new FocusListener() {
 			@Override
 			public void focusLost(FocusEvent arg0) {
 				Context.setWaterMarkText(watermarkText.getText());
 			}
 			@Override
 			public void focusGained(FocusEvent arg0) {
 			}
 		});
 		Context.setWaterMarkText(watermarkText.getText()); // initial
 
 		final JCheckBox useWaterMark = new JCheckBox();
 		useWaterMark.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent arg0) {
 				graphicPanel.setUseWaterMark(useWaterMark.isSelected());
 				Context.setWaterMarkText(watermarkText.getText());
 			}
 		});
 	
 		SpinnerModel sm = new SpinnerNumberModel(50, 25, 100, 25);
 		final JSpinner zoom = new JSpinner(sm);
 		zoom.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent arg0) {
 				int v = (Integer)zoom.getValue();
 				float zoomFactor = v / 100f;
 				graphicPanel.setZoomFactor(zoomFactor);
 				graphicPanel.repaint();
 			}
 		});
 		add(useWaterMark);
 		add(new JLabel("watermark:"));
 		add(watermarkText);
 		add(new JLabel("zoom %"), BorderLayout.CENTER);
 		add(zoom);
 	}
 
 }
