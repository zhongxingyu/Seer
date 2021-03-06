 /* -*- tab-width: 4 -*-
  *
  * Electric(tm) VLSI Design System
  *
  * File: Metal.java
  *
  * Copyright (c) 2008 Sun Microsystems and Static Free Software
  *
  * Electric(tm) is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  *
  * Electric(tm) is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Electric(tm); see the file COPYING.  If not, write to
  * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  * Boston, Mass 02111-1307, USA.
  */
 package com.sun.electric.tool.user.tecEditWizard;
 
 import com.sun.electric.database.text.TextUtils;
 import com.sun.electric.tool.Job;
 import com.sun.electric.tool.user.Resources;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 /**
  * Class to handle the "Metal" tab of the Numeric Technology Editor dialog.
  */
 public class Metal extends TechEditWizardPanel
 {
     private JPanel metal;
     private JLabel [] widthLabel;
     private JLabel [] spacingLabel;
     private JTextField [] spacing;
     private JTextField [] spacingRule;
     private JTextField [] width;
     private JTextField [] widthRule;
     private int numMetals;
     private TechEditWizard parent;
 
     /** Creates new form Metal */
 	public Metal(TechEditWizard parent, boolean modal)
 	{
 		super(parent, modal);
 		this.parent = parent;
 
         setTitle("Metal");
         setName("");
 
         metal = new JPanel();
         metal.setLayout(new GridBagLayout());
 
         JLabel heading = new JLabel("Metal Parameters");
         GridBagConstraints gbc = new GridBagConstraints();
         gbc.gridx = 0;   gbc.gridy = 0;
         gbc.gridwidth = 3;
         gbc.insets = new Insets(4, 4, 4, 4);
         metal.add(heading, gbc);
 
         JLabel image = new JLabel();
 		image.setIcon(Resources.getResource(getClass(), "Metal.png"));
         gbc = new GridBagConstraints();
         gbc.gridx = 0;   gbc.gridy = 1;
         gbc.gridwidth = 3;
         gbc.insets = new Insets(4, 4, 4, 4);
         metal.add(image, gbc);
 
         JButton addMetal = new JButton("Add Metal");
         gbc = new GridBagConstraints();
         gbc.gridx = 0;   gbc.gridy = 2;
         gbc.insets = new Insets(4, 4, 4, 4);
         addMetal.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent evt) { addMetal(); }
         });
         metal.add(addMetal, gbc);
 
         JButton removeMetal = new JButton("Remove Metal");
         gbc = new GridBagConstraints();
         gbc.gridx = 1;   gbc.gridy = 2;
         gbc.insets = new Insets(4, 4, 4, 4);
         removeMetal.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent evt) { removeMetal(); }
         });
         metal.add(removeMetal, gbc);
 
         JLabel l1 = new JLabel("Distance");
         gbc = new GridBagConstraints();
         gbc.gridx = 1;   gbc.gridy = 3;
         metal.add(l1, gbc);
 
         JLabel l2 = new JLabel("Rule Name");
         gbc = new GridBagConstraints();
         gbc.gridx = 2;   gbc.gridy = 3;
         metal.add(l2, gbc);
 
         JLabel nano = new JLabel("Distances are in nanometers");
         gbc = new GridBagConstraints();
         gbc.gridx = 0;   gbc.gridy = 99;
         gbc.gridwidth = 3;
         gbc.insets = new Insets(4, 4, 4, 4);
         metal.add(nano, gbc);
 	}
 
 	/** return the panel to use for this Numeric Technology Editor tab. */
 	public JPanel getPanel() { return metal; }
 
 	/** return the name of this Numeric Technology Editor tab. */
 	public String getName() { return "Metal"; }
 
 	/**
 	 * Method called at the start of the dialog.
 	 * Caches current values and displays them in the Metal tab.
 	 */
 	public void init()
 	{
 		TechEditWizardData data = wizard.getTechEditData();
         numMetals = data.getNumMetalLayers();
         widthLabel = new JLabel[numMetals];
         width = new JTextField[numMetals];
         widthRule = new JTextField[numMetals];
         spacingLabel = new JLabel[numMetals];
         spacing = new JTextField[numMetals];
         spacingRule = new JTextField[numMetals];
         for(int i=0; i<numMetals; i++)
         {
         	addMetalLayer(i);
         	width[i].setText(Double.toString(data.getMetalWidth()[i].v));
         	widthRule[i].setText(data.getMetalWidth()[i].rule);
         	spacing[i].setText(Double.toString(data.getMetalSpacing()[i].v));
         	spacingRule[i].setText(data.getMetalSpacing()[i].rule);
         }
 	}
 
 	/**
 	 * Method to create the dialog fields for a metal layer.
 	 * @param i the metal layer to fill-in.
 	 */
 	private void addMetalLayer(int i)
 	{
     	widthLabel[i] = new JLabel("Metal-" + (i+1) + " width (A):");
     	GridBagConstraints gbc = new GridBagConstraints();
     	gbc.gridx = 0;   gbc.gridy = 4+i*2;
         gbc.anchor = GridBagConstraints.WEST;
         gbc.insets = new Insets(4, 4, 1, 0);
         metal.add(widthLabel[i], gbc);
 
         width[i] = new JTextField();
         width[i].setColumns(8);
     	gbc = new GridBagConstraints();
     	gbc.gridx = 1;   gbc.gridy = 4+i*2;
         gbc.insets = new Insets(4, 0, 1, 2);
         metal.add(width[i], gbc);
 
         widthRule[i] = new JTextField();
         widthRule[i].setColumns(8);
     	gbc = new GridBagConstraints();
     	gbc.gridx = 2;   gbc.gridy = 4+i*2;
         gbc.insets = new Insets(4, 0, 1, 2);
         metal.add(widthRule[i], gbc);
 
         spacingLabel[i] = new JLabel("Metal-" + (i+1) + " spacing (B):");
     	gbc = new GridBagConstraints();
     	gbc.gridx = 0;   gbc.gridy = 5+i*2;
         gbc.anchor = GridBagConstraints.WEST;
         gbc.insets = new Insets(1, 4, 4, 0);
         metal.add(spacingLabel[i], gbc);
 
         spacing[i] = new JTextField();
         spacing[i].setColumns(8);
     	gbc = new GridBagConstraints();
     	gbc.gridx = 1;   gbc.gridy = 5+i*2;
         gbc.insets = new Insets(1, 0, 4, 2);
         metal.add(spacing[i], gbc);
 
         spacingRule[i] = new JTextField();
         spacingRule[i].setColumns(8);
     	gbc = new GridBagConstraints();
     	gbc.gridx = 2;   gbc.gridy = 5+i*2;
         gbc.insets = new Insets(1, 0, 4, 2);
         metal.add(spacingRule[i], gbc);
 	}
 
 	/**
 	 * Method called when the user clicks "Add Metal"
 	 */
 	private void addMetal()
 	{
         numMetals++;
 	    JLabel [] newWidthLabel = new JLabel[numMetals];
 	    JTextField [] newWidth = new JTextField[numMetals];
 	    JTextField [] newWidthRule = new JTextField[numMetals];
 	    JLabel [] newSpacingLabel = new JLabel[numMetals];
 	    JTextField [] newSpacing = new JTextField[numMetals];
 	    JTextField [] newSpacingRule = new JTextField[numMetals];

        System.arraycopy(width, 0, newWidth, 0, numMetals-1);
        System.arraycopy(widthLabel, 0, newWidthLabel, 0, numMetals-1);
        System.arraycopy(widthRule, 0, newWidthRule, 0, numMetals-1);
        System.arraycopy(spacingLabel, 0, newSpacingLabel, 0, numMetals-1);
        System.arraycopy(spacing, 0, newSpacing, 0, numMetals-1);
        System.arraycopy(spacingRule, 0, newSpacingRule, 0, numMetals-1);
        
        widthLabel = newWidthLabel;
 	    width = newWidth;
 	    widthRule = newWidthRule;
 	    spacingLabel = newSpacingLabel;
 	    spacing = newSpacing;
 	    spacingRule = newSpacingRule;
 	    addMetalLayer(numMetals-1);
 		parent.pack();
 	}
 
 	/**
 	 * Method called when the user clicks "Remove Metal"
 	 */
 	private void removeMetal()
 	{
 		if (numMetals <= 1)
 		{
 			Job.getUserInterface().showErrorMessage("Cannot delete the last metal layer: must be at least one",
 				"Illegal Operation");
 			return;
 		}
         numMetals--;
         metal.remove(widthLabel[numMetals]);
         metal.remove(width[numMetals]);
         metal.remove(widthRule[numMetals]);
         metal.remove(spacingLabel[numMetals]);
         metal.remove(spacing[numMetals]);
         metal.remove(spacingRule[numMetals]);
 		parent.pack();
 	}
 
 	/**
 	 * Method called when the "OK" panel is hit.
 	 * Updates any changed fields in the Metal tab.
 	 */
 	public void term()
 	{
 		TechEditWizardData data = wizard.getTechEditData();
 		data.setNumMetalLayers(numMetals);
         for(int i=0; i<numMetals; i++)
         {
         	data.setMetalWidth(i, new WizardField(TextUtils.atof(width[i].getText()), widthRule[i].getText()));
         	data.setMetalSpacing(i, new WizardField(TextUtils.atof(spacing[i].getText()), spacingRule[i].getText()));
         }
 	}
 }
