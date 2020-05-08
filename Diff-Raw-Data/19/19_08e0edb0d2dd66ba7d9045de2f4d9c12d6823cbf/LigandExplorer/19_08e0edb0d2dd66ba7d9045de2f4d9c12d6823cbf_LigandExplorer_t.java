 /*
  * BioJava development code
  *
  * This code may be freely distributed and modified under the
  * terms of the GNU Lesser General Public Licence. This should
  * be distributed with the code. If you do not have a copy,
  * see:
  *
  * http://www.gnu.org/copyleft/lesser.html
  *
  * Copyright for this code is held jointly by the individual
  * authors. These should be listed in @author doc comments.
  *
  * For more information on the BioJava project and its aims,
  * or to join the biojava-l mailing list, visit the home page
  * at:
  *
  * http://www.biojava.org/
  *
  * This code was contributed from the Molecular Biology Toolkit
  * (MBT) project at the University of California San Diego.
  *
  * Please reference J.L. Moreland, A.Gramada, O.V. Buzko, Qing
  * Zhang and P.E. Bourne 2005 The Molecular Biology Toolkit (MBT):
  * A Modular Platform for Developing Molecular Visualization
  * Applications. BMC Bioinformatics, 6:21.
  *
  * The MBT project was funded as part of the National Institutes
  * of Health PPG grant number 1-P01-GM63208 and its National
  * Institute of General Medical Sciences (NIGMS) division. Ongoing
  * development for the MBT project is managed by the RCSB
  * Protein Data Bank(http://www.pdb.org) and supported by funds
  * from the National Science Foundation (NSF), the National
  * Institute of General Medical Sciences (NIGMS), the Office of
  * Science, Department of Energy (DOE), the National Library of
  * Medicine (NLM), the National Cancer Institute (NCI), the
  * National Center for Research Resources (NCRR), the National
  * Institute of Biomedical Imaging and Bioengineering (NIBIB),
  * the National Institute of Neurological Disorders and Stroke
  * (NINDS), and the National Institute of Diabetes and Digestive
  * and Kidney Diseases (NIDDK).
  *
  * Created on 2007/07/19
  *
  */ 
 package org.rcsb.lx.controllers.app;
 
 
 // package edu.sdsc.vis.viewers;
 
 // CORE JAVA
 
import java.awt.EventQueue;
 import java.awt.image.BufferedImage;
 import java.net.URL;
 
 import org.rcsb.lx.controllers.scene.LXSceneController;
 import org.rcsb.lx.controllers.update.LXUpdateController;
 import org.rcsb.lx.glscene.jogl.LXGlGeometryViewer;
 import org.rcsb.lx.glscene.jogl.LXSceneNode;
 import org.rcsb.lx.model.LXModel;
 import org.rcsb.lx.ui.LXDocumentFrame;
 import org.rcsb.mbt.model.StructureModel;
 import org.rcsb.mbt.model.util.ExternReferences;
 import org.rcsb.uiApp.controllers.update.UpdateController;
 import org.rcsb.uiApp.ui.mainframe.DocumentFrameBase;
 import org.rcsb.vf.controllers.app.VFAppBase;
 import org.rcsb.vf.controllers.scene.SceneController;
 import org.rcsb.vf.glscene.jogl.GlGeometryViewer;
 import org.rcsb.vf.glscene.jogl.JoglSceneNode;
 import org.rcsb.vf.ui.VFDocumentFrameBase;
 
 
 
 
 /**
  * LigandExplorer.java
  * <P>
  * 
  * @author John L. Moreland
  * @copyright SDSC
  * @see
  */
 public class LigandExplorer extends VFAppBase
 {
 	public class LXAppModuleFactory extends VFAppBase.VFAppModuleFactory
 	{
 		/* (non-Javadoc)
 		 * @see edu.sdsc.mbt.app_controller.JAppBase.AppModuleFactory#createSceneController()
 		 */
 		@Override
 		public SceneController createSceneController() {
 			return new LXSceneController();
 		}
 
 		/* (non-Javadoc)
 		 * @see edu.sdsc.mbt.app_controller.JAppBase.AppModuleFactory#createDocFrame(java.lang.String)
 		 */
 		@Override
 		public DocumentFrameBase createDocFrame(String name, URL iconUrl)
 		{
 			return new LXDocumentFrame(name, iconUrl);
 		}
 
 		/* (non-Javadoc)
 		 * @see edu.sdsc.mbt.app_controller.JAppBase.AppModuleFactory#createGlGeometryViewer()
 		 */
 		@Override
 		public GlGeometryViewer createGlGeometryViewer()
 		{
 			return new LXGlGeometryViewer();
 		}
 
 		/* (non-Javadoc)
 		 * @see edu.sdsc.mbt.app_controller.JAppBase.AppModuleFactory#createModel()
 		 */
 		@Override
 		public StructureModel createModel()
 		{
 			LXModel model = new LXModel();
 			ExternReferences.registerResidueNameModifier(model);
 							// make sure this is registered as soon as it is created.
 			return model;
 		}
 
 
 		/* (non-Javadoc)
 		 * @see edu.sdsc.mbt.app_controller.JAppBase.AppModuleFactory#createViewUpdateController()
 		 */
 		@Override
 		public UpdateController createUpdateController()
 		{
 			return new LXUpdateController();
 		}
 
 		/* (non-Javadoc)
 		 * @see org.rcsb.mbt.appController.AppBase.AppModuleFactory#createSceneNode()
 		 */
 		@Override
 		public JoglSceneNode createSceneNode() {
 			return new LXSceneNode();
 		}
 	}
 	
 	public static boolean saveInteractionsFlag = false;
 
 	public static void main(final String[] args)
 	{
		EventQueue.invokeLater(new Runnable() { public void run() {
			final LigandExplorer app = new LigandExplorer(args);

			for (int argIX = 0; argIX < args.length; argIX++)
				if (args[argIX].equals("-ligand"))
					app.properties.setProperty("ligand", args[++argIX]);
			// field args for app-specific args

			app.initialize(true);
		}});
	}
	
	public static void mainOld(final String[] args)
	{
 		final LigandExplorer app = new LigandExplorer(args);
 		
 		for (int argIX = 0; argIX < args.length; argIX++)
 		if (args[argIX].equals("-ligand"))
 			app.properties.setProperty("ligand", args[++argIX]);
 						// field args for app-specific args
 
 		app.initialize(true);
 	}
 	
 	@Override
 	public String toString() { return "RCSB Ligand Explorer"; }
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param args - argument list from the main() caller.
	 */	
 	public LigandExplorer(final String[] args)
 	{
 		super(args);
 		ExternReferences.setIsLigandExplorer();
 	}
 
 	@Override
 	public void initialize(final boolean isApplication)
 	{
 		appModuleFactory = new LXAppModuleFactory();
 		activeFrame = appModuleFactory.createDocFrame("PDB Ligand Explorer " + LXVersionInformation.version() + " (Powered by the MBT)",
 												      LigandExplorer.class.getResource("images/icon_128_LX.png"));
 	
 		super.initialize(true);
 		
 		final String showAsymmetricUnitOnly = this.properties.getProperty("show_asymmetric_unit_only");
 		SceneController sceneController = ((LXDocumentFrame)activeFrame).getSceneController();
 		if (showAsymmetricUnitOnly != null && showAsymmetricUnitOnly.equals("true"))
 			sceneController.setShowAsymmetricUnitOnly(true);
 		
 
 		else
 			sceneController.setShowAsymmetricUnitOnly(false);
 		
 		activeFrame.initialize(true);
 		
 		final String structureUrlParam = this.properties.getProperty("structure_url");
 		
 		if (structureUrlParam != null)
 			((VFDocumentFrameBase)activeFrame).loadURL(structureUrlParam);
 		
 		String structureIdList = this.properties.getProperty("structure_id_list");
 
 		if (structureIdList != null)
 			((VFDocumentFrameBase)activeFrame).loadURL(structureIdList.split(","));
 	}
 	
 
 	public BufferedImage screenshot = null;
 
 
 	/*
 	 * Convenience access overrides
 	 * @return
 	 */
 	public static LigandExplorer getApp() { return (LigandExplorer)VFAppBase.getApp(); }	
 	@Override
 	public LXDocumentFrame getActiveFrame() { return (LXDocumentFrame)activeFrame; }
 	public static LXDocumentFrame sgetActiveFrame() { return getApp().getActiveFrame(); }	
 	public static LXModel sgetModel() { return sgetActiveFrame().getModel(); }
 	public static LXUpdateController sgetUpdateController() { return sgetActiveFrame().getUpdateController(); }
 	public static LXSceneController sgetSceneController() { return sgetActiveFrame().getSceneController(); }
 	public static LXGlGeometryViewer sgetGlGeometryViewer() { return sgetActiveFrame().getGlGeometryViewer(); }
 }
