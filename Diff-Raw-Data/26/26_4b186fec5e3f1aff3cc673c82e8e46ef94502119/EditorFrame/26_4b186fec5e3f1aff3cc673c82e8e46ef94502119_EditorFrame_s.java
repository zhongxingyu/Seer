 /**
  * Copyright (c) 2004-2007 Rensselaer Polytechnic Institute
  * Copyright (c) 2007 NEES Cyberinfrastructure Center
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  *
  * For more information: http://nees.rpi.edu/3dviewer/
  */
 
 package org.nees.rpi.vis.ui;
 
 import org.nees.rpi.vis.*;
 import org.nees.rpi.vis.exporters.M3DVExporter;
 import org.nees.rpi.vis.exporters.M3DVCExporter;
 import org.nees.rpi.vis.loaders.DataFileLoader;
 import org.nees.rpi.vis.loaders.DelimiterType;
 import org.nees.rpi.vis.loaders.DataFileLoaderException;
 import org.nees.rpi.vis.loaders.M3DVCLoader;
 
 import javax.swing.*;
 import javax.vecmath.Point3f;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.File;
 import java.net.MalformedURLException;
 
 /**
  * An M3DV model editor window
  */
 public class EditorFrame extends VisFrame
 {
 	/** The top-most component container of the frame */
 	JPanel contentPane;
 
 	/** the file to save the model in editing to */
 	File saveFile;
 
 	/** the new DVModel edited by the user */
 	DVModel modelInEditing;
 
 	/** the panel containing the main menu */
 	EditorMainMenuPanel mainMenu = new EditorMainMenuPanel();
 
 	/** the panel containing the model elements list being edited */
 	EditorModelEditPanel modelEditPanel = new EditorModelEditPanel();
 
 	/** the panel containing the GUI elements for the model data files */
 	EditorDataFilePanel datafilePanel = new EditorDataFilePanel();
 
 	/** The panel containing the GUI elements for the model options */
 	EditorOptionsMenuPanel optionsPanel = new EditorOptionsMenuPanel();
 
 	public EditorFrame()
 	{
 		super("modeleditorwindow");
 		initFrame();
 		initMenusAndSettings();
 		initShapeEditList();
 		initChannelList();
 		initNewModel();
 	}
 
 	/**
 	 * initializes the main frame and sets it up before any child
 	 * components are added
 	 */
 	private void initFrame()
 	{
 		setTitle("3DDV Model Editor");
 		contentPane = new JPanel();
 		setContentPane(contentPane);
 		contentPane.setOpaque(true);
 		contentPane.setBackground(Color.WHITE);
 		contentPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
 		setLayout(new BorderLayout());
 	}
 
 	/**
 	 * initializes the section of the window devoted to menus and settings
 	 */
 	private void initMenusAndSettings()
 	{
 		JPanel menusPanel = new JPanel();
 		menusPanel.setOpaque(false);
 		menusPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,5));
 
 		menusPanel.setLayout(new BoxLayout(menusPanel, BoxLayout.Y_AXIS));
 		menusPanel.add(mainMenu);
 		EditorModelMenuPanel modelMenu = new EditorModelMenuPanel();
 		menusPanel.add(modelMenu);
 		menusPanel.add(optionsPanel);
 
 		// Hack to addjust the spacing for this panel
 		JPanel spacingPanel = new JPanel();
 		spacingPanel.setPreferredSize(new Dimension(200, 2000));
 		spacingPanel.setOpaque(false);
 		menusPanel.add(spacingPanel);
 
 		contentPane.add(menusPanel, BorderLayout.WEST);
 
 		//set actions
 		mainMenu.setOpenModelAction(new OpenModelAction());
 		mainMenu.setSaveModelAction(new SaveModelInEditingAction());
 		mainMenu.setSaveModelAsAction(new SaveModelInEditingAsAction());
 		mainMenu.setCloseWindowAction(new CloseFrameAction());
 		modelMenu.setImportDataFileAction(new ImportDataFileAction());
 	}
 
 	/**
 	 * initializes the section of the window used to display the M3DV model elements
 	 */
 	private void initShapeEditList()
 	{
 		modelEditPanel.setRemoveSelectedAction(new RemoveSelectedShapesAction());
 		contentPane.add(modelEditPanel, BorderLayout.CENTER);
 	}
 
 	/**
 	 * initializes the section of the window used to display the data chennels imported
 	 * from data files
 	 */
 	private void initChannelList()
 	{
 		contentPane.add(datafilePanel, BorderLayout.EAST);
 		datafilePanel.setAddSelectedAction(new AddSelectedChannelsAction());
 	}
 
 	/**
 	 * initializes the frame to edit a new model
 	 */
 	private void initNewModel()
 	{
 		DVModel model = new DVModel("Untitled Model");
 		model.addGroup(new DVGroup("Data Channels"));
 		setModel(model);
 	}
 
 	private void setModel(DVModel model)
 	{
 		modelInEditing = model;
 		modelEditPanel.setModel(modelInEditing);
 		setSaveFile(null);
 	}
 
 	/**
 	 * Adds a new shape to the model being edited
 	 */
 	private void addShape(DVShape shape, DVGroup parent)
 	{
 		modelInEditing.addShape(shape, parent);
 		modelEditPanel.addShape(shape);
 	}
 
 	/**
 	 * loads data channels from a data file and imports the channel info
 	 * into the frame UI
 	 *
 	 * @param datafile
 	 */
 	private void loadChannels(File datafile)
 	{
 		DataFileLoader datafileLoader;
		DVDataFile dvDataFile;
 
 		if (optionsPanel.getDataFileDelimiter() == DelimiterType.TAB_DELIMITED)
 			datafileLoader = new DataFileLoader(DelimiterType.TAB_DELIMITED);
 		else
 			datafileLoader = new DataFileLoader(DelimiterType.COMMA_DELIMITED);
 
 		try
 		{
 			dvDataFile = datafileLoader.loadFile(datafile.toURL());
			datafilePanel.addDataFile(dvDataFile);
 		}
 		catch (MalformedURLException e)
 		{
			e.printStackTrace();
 		}
 		catch (DataFileLoaderException e)
 		{
			e.printStackTrace();
 		}
 	}
 
 	private void setSaveFile(File savefile)
 	{
 		saveFile = savefile;
 		if (saveFile == null)
 			mainMenu.saveModelButton.setEnabled(false);
 		else
 			mainMenu.saveModelButton.setEnabled(true);
 	}
 
 	/**
 	 * Action to close the main model editor window
 	 */
 	class CloseFrameAction extends AbstractAction
 	{
 		public void actionPerformed(ActionEvent actionEvent)
 		{
 			EditorFrame.this.setVisible(false);
 		}
 	}
 
 	/**
 	 * Action to open an existing model for editing
 	 */
 	class OpenModelAction extends AbstractAction
 	{
 		public void actionPerformed(ActionEvent actionEvent)
 		{
 			AppSettings APP = AppSettings.getInstance();
 			String lastDir = APP.getString(frameid + ".lastsavedir");
 			JFileChooser fc = new JFileChooser();
 			if (lastDir != null)
 				fc.setCurrentDirectory(new File(lastDir));
 			fc.setFileFilter(new SimpleFileFilter("3ddv", APP.getM3dvFileChooserDescriptor()));
 			int returnval = fc.showOpenDialog(EditorFrame.this);
 			if (returnval == JFileChooser.APPROVE_OPTION)
 			{
 				AppSettings.getInstance().setProperty(
 								frameid + ".lastsavedir", fc.getCurrentDirectory().toString());
 				M3DVCLoader loader = new M3DVCLoader(fc.getSelectedFile());
 				DVModel loadedmodel = loader.load();
 				if (loadedmodel == null || loader.isError())
 				{
 					String errorMsg = "The following errors occured while loading your model file:\n\n";
 					for (int i=0; i<loader.getErrors().length; i++)
 						errorMsg += loader.getErrors()[i].toString() + "\n";
 					System.out.println(errorMsg);
 					JOptionPane.showMessageDialog(EditorFrame.this, errorMsg, "Model File Load Error", JOptionPane.ERROR_MESSAGE);
 				}
 				else
 					setModel(loadedmodel);
 			}
 		}
 	}
 
 	/**
 	 * Action to save a model to an existing file
 	 */
 	class SaveModelInEditingAction extends AbstractAction
 	{
 		public void actionPerformed(ActionEvent actionEvent)
 		{
 			M3DVExporter.export(modelInEditing, saveFile);
 		}
 	}
 
 	/**
 	 * Action to save a model in editing as a new file name
 	 */
 	class SaveModelInEditingAsAction extends AbstractAction
 	{
 		public void actionPerformed(ActionEvent actionEvent)
 		{
 			AppSettings APP = AppSettings.getInstance();
 			String lastDir = APP.getString(frameid + ".lastsavedir");
 			JFileChooser fc = new JFileChooser();
 			if (lastDir != null)
 				fc.setCurrentDirectory(new File(lastDir));
 			fc.setFileFilter(new SimpleFileFilter("3ddv", APP.getM3dvFileChooserDescriptor()));
 			int returnval = fc.showSaveDialog(EditorFrame.this);
 			if (returnval == JFileChooser.APPROVE_OPTION)
 			{
 				AppSettings.getInstance().setProperty(
 								frameid + ".lastsavedir", fc.getCurrentDirectory().toString());
 				setSaveFile(fc.getSelectedFile());
 				M3DVCExporter.export(modelInEditing, saveFile);
 			}
 		}
 	}
 
 	/**
 	 * Action to prompt user for a data file to import and initiate the
 	 * data import process.
 	 *
 	 */
 	class ImportDataFileAction extends AbstractAction
 	{
 		public void actionPerformed(ActionEvent actionEvent)
 		{
 			String lastDir = AppSettings.getInstance().getString(frameid + ".lastimportdir");
 			JFileChooser fc = new JFileChooser();
 			if (lastDir != null)
 				fc.setCurrentDirectory(new File(lastDir));
 			fc.setFileFilter(new SimpleFileFilter("txt", "Text Data File (*.txt)"));
 			int returnval = fc.showOpenDialog(EditorFrame.this);
 			if (returnval == JFileChooser.APPROVE_OPTION)
 			{
 				AppSettings.getInstance().setProperty(
 								frameid + ".lastimportdir", fc.getCurrentDirectory().toString());
 				loadChannels(fc.getSelectedFile());
 			}
 		}
 	}
 
 	/**
 	 * Action to add the user selected data channels to the model
 	 */
 	class AddSelectedChannelsAction extends AbstractAction
 	{
 		public void actionPerformed(ActionEvent actionEvent)
 		{
 			java.util.List<DVDataChannel> channels = datafilePanel.getSelectedChannels();
 
 			// time channel is the first one
 			DVDataFile parent = null;
 			DVDataChannel timeChannel = null;
 
 			for (DVDataChannel channel : channels)
 			{
 				if (parent != channel.getParent())
 				{
 					parent = channel.getParent();
 					timeChannel = parent.getTimeChannel();
 					modelInEditing.addDataFile(parent);
 				}
 				DVShape newshape = DVShapeFactory.createPressureSensor(new Point3f(0,0,0));
 				newshape.setName(channel.getName());
 				DVShapeData data = new DVShapeData();
 				if (channel.hasUnit())
 					data.setSeries(
 							timeChannel.getName(), timeChannel.getData(), timeChannel.getUnit(),
 							channel.getName(), channel.getData(), channel.getUnit());
 				else
 					data.setSeries(
 							timeChannel.getName(), timeChannel.getData(), timeChannel.getUnit(),
 							channel.getName(), channel.getData(), channel.getUnit());
 				newshape.setShapeData(data);
 				//add to the first group until logic is added to let the user select the group
 				addShape(newshape, modelInEditing.getGroups().get(0));
 			}
 		}
 	}
 
 	/**
 	 * Action to remove the user selected shapes from the model list
 	 */
 	class RemoveSelectedShapesAction extends AbstractAction
 	{
 		public void actionPerformed(ActionEvent actionEvent)
 		{
 			java.util.List<DVShape> shapes = modelEditPanel.getSelectedShapes();
 			for (DVShape shape : shapes)
 				modelInEditing.removeShape(shape);
 			modelEditPanel.resetShapeList();
 		}
 	}
 
 	public static void main(String args[])
 	{
 		new EditorFrame().setVisible(true);
 	}
 }
 
