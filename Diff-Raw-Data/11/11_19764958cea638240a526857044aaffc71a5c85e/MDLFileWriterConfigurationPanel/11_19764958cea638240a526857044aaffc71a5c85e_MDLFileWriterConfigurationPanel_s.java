 /*
  * Copyright (C) 2010 by Andreas Truszkowski <ATruszkowski@gmx.de>
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  * All we ask is that proper credit is given for our work, which includes
  * - but is not limited to - adding the above copyright notice to the beginning
  * of your source code files, and to any copyright notice that you may distribute
  * with programs based on this work.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
  */
 package org.openscience.cdk.applications.taverna.ui.io;
 
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.io.File;
 import java.net.URL;
 
 import javax.swing.AbstractAction;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;
 
 import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
 import org.openscience.cdk.applications.taverna.CDKActivityConfigurationBean;
 import org.openscience.cdk.applications.taverna.Constants;
 
 /**
  * Configuration panel for MDL file writing activities.
  * 
  * @author Andreas Truszkowski
  * 
  */
 public class MDLFileWriterConfigurationPanel extends
 		ActivityConfigurationPanel<AbstractCDKActivity, CDKActivityConfigurationBean> {
 
 	private static final long serialVersionUID = -1161055144757128604L;
 
 	private AbstractCDKActivity activity;
 	private CDKActivityConfigurationBean configBean;
 
 	private File file = null;
 	private JTextField filePathField = new JTextField();
 
 	private AbstractAction chooseFileAction = new AbstractAction() {
 
 		public void actionPerformed(ActionEvent e) {
 			JFileChooser openDialog = new JFileChooser(new File("."));
 			openDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 			if (openDialog.showOpenDialog(MDLFileWriterConfigurationPanel.this) == JFileChooser.APPROVE_OPTION) {
 				MDLFileWriterConfigurationPanel.this.file = openDialog.getSelectedFile();
 				MDLFileWriterConfigurationPanel.this.showValue();
 			}
 		}
 	};
 
 	public MDLFileWriterConfigurationPanel(AbstractCDKActivity activity) {
 		this.activity = activity;
 		this.configBean = this.activity.getConfiguration();
 		this.initGUI();
 	}
 
 	protected void initGUI() {
 		try {
 			this.removeAll();
 			this.setLayout(new GridLayout(2, 0, 1, 1));
 			JLabel label = new JLabel("Output directory:");
 			this.add(label);
 			JPanel filePanel = new JPanel();
 			filePanel.setLayout(new FlowLayout());
 			// filePanel.setSize(200, 0);
 			this.filePathField = new JTextField();
 			this.filePathField.setEditable(false);
 			this.filePathField.setPreferredSize(new Dimension(250, 25));
 			filePanel.add(this.filePathField);
 			JButton fileChooserButton = new JButton(this.chooseFileAction);
			URL url = ClassLoader.getSystemResource("icons/open.gif");
 			ImageIcon icon = new ImageIcon(url);
 			fileChooserButton.setPreferredSize(new Dimension(25, 25));
 			fileChooserButton.setIcon(icon);
 			filePanel.add(fileChooserButton);
 			this.add(filePanel);
 
 			this.refreshConfiguration();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void showValue() {
 		this.filePathField.setText(this.file.getPath());
 		this.filePathField.repaint();
 		this.revalidate();
 	}
 
 	@Override
 	public boolean checkValues() {
 		if (this.file != null && this.file.exists()) {
 			return true;
 		}
 		JOptionPane.showMessageDialog(this, "Chosen directory is not valid!", "Invalid directory", JOptionPane.ERROR_MESSAGE);
 		// Not valid, return false
 		return false;
 	}
 
 	@Override
 	public CDKActivityConfigurationBean getConfiguration() {
 		return this.configBean;
 	}
 
 	@Override
 	public boolean isConfigurationChanged() {
 		if (this.file == null) {
 			return false;
 		}
 		File file = (File) configBean.getAdditionalProperty(Constants.PROPERTY_FILE);
 		return !this.file.equals(file);
 	}
 
 	@Override
 	public void noteConfiguration() {
 		this.configBean = (CDKActivityConfigurationBean) this.cloneBean(this.configBean);
 		this.configBean.addAdditionalProperty(Constants.PROPERTY_FILE, this.file);
 		this.filePathField.setText(this.file.getPath());
 		this.filePathField.repaint();
 	}
 
 	@Override
 	public void refreshConfiguration() {
 		this.file = (File) configBean.getAdditionalProperty(Constants.PROPERTY_FILE);
 		if (this.file != null) {
 			this.filePathField.setText(this.file.getAbsolutePath());
 			this.filePathField.repaint();
 		}
 	}
 
 }
