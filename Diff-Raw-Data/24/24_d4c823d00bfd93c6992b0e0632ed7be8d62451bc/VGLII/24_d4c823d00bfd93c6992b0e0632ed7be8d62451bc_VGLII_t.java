 package edu.umb.jsVGL.client.VGL;
 
 import java.awt.Dimension;
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Random;
 
 import javax.swing.JOptionPane;
 
 import com.google.gwt.dev.shell.Messages;
 import com.google.gwt.i18n.client.Dictionary;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.xml.client.Document;
 import com.google.gwt.xml.client.Element;
 import com.google.gwt.xml.client.XMLParser;
 
 import edu.umb.jsVGL.client.JsVGL;
 import edu.umb.jsVGL.client.GeneticModels.Cage;
 import edu.umb.jsVGL.client.GeneticModels.CharacterSpecificationBank;
 import edu.umb.jsVGL.client.GeneticModels.GeneticModel;
 import edu.umb.jsVGL.client.GeneticModels.GeneticModelFactory;
 import edu.umb.jsVGL.client.GeneticModels.Organism;
 import edu.umb.jsVGL.client.ModelBuilder.ModelBuilderUI;
 
 /**
  * Nikunj Koolar cs681-3 Fall 2002 - Spring 2003 Project VGL File:
  * Brian White Summer 2008
  * VGLII.java - the UI controller class. Its the heart of almost all UI
  * renditions and manipulations.
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation; either version 2 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place - Suite 330, Boston, MA 02111-1307, USA.
  * 
  * @author Nikunj Koolar & Brian White
  * @version 1.0 $Id$
  */
 public class VGLII {
 
 	/**
 	 * the version number
 	 */
 	public final static String version = "3.3.1 2012-02-25 22:00"; //$NON-NLS-1$
 
 	private Random random;
 
 	/**
 	 * the genetic model for the current problem
 	 */
 	private GeneticModel geneticModel;
 
 	/**
 	 * reference back to enclosing jsVGL
 	 */
 	private JsVGL jsVGL;
 	
 //	private ModelBuilderUI modelBuilder;
 	
 
 	/**
 	 * The collection of Cage UIs associated with the current problem
 	 */
 	private ArrayList<CageUI> cageCollection;
 
 	/**
 	 * The id of the next cage that will be created
 	 */
 	private int nextCageId = 0;
 
 	/**
 	 * The singular instance that holds the current male-female selection for
 	 * crossing
 	 */
 	private SelectionVial selectionVial;
 
 	/**
 	 * Stores the value of the next position on the screen where a cage should
 	 * be displayed
 	 */
 	private int nextCageScreenPositionX;
 	private int nextCageScreenPositionY;
 
 	private boolean changeSinceLastSave;
 
 	/*
 	 * the param dictionary has the parameters for setting up a problem
 	 * this string is its name in the doc
 	 */
 	private Dictionary params;
 
 	/**
 	 * The constructor
 	 * 
 	 */
 	public VGLII(Dictionary params, JsVGL jsVGL) {
 		this.params = params;
 		this.jsVGL = jsVGL;
 		random = new Random();
 		changeSinceLastSave = false;
 	}
 
 	/*
 	 * set up new problem based on parameters submitted in page
 	 */
 	public void newPracticeProblem() {
 		newProblem(true);
 	}
 	public void newGradedProblem() {
 		newProblem(false);
 	}
 
 	private void newProblem(boolean practiceMode) {
 		CharacterSpecificationBank.getInstance().refreshAll();
		geneticModel = GeneticModelFactory.getInstance().createRandomModel(params);
 		if (geneticModel == null) return;
 
 		geneticModel.setBeginnerMode(practiceMode);
 		
 		nextCageId = 0;
 		selectionVial = new SelectionVial();
 		cageCollection = new ArrayList<CageUI>();
 
 		Cage fieldPop = geneticModel.generateFieldPopulation();
 		createCageUI(fieldPop, false);
 		
		jsVGL.getModelBuilderPanel().clear();
		jsVGL.getModelBuilderPanel().add(new ModelBuilderUI(this, geneticModel));
		
 		jsVGL.setButtonState(true);
 
 		changeSinceLastSave = true;
 	}
 
 	/**
 	 * Opens up an existing saved problem, sets up the model, and opens up all
 	 * the cages of that problem.
 	 */
 	public void openProblem(String problemXML) {	
 
 		selectionVial = new SelectionVial();
 		SavedWorkFileData result = null;
 
 		try {
 			result = GeneticModelFactory.getInstance().readModelFromXML(problemXML);
 			if (result == null) return;
 
 			geneticModel = result.getGeneticModel();
 			cageCollection = new ArrayList<CageUI>();
 			nextCageId = 0;
 			reopenCages(result.getCages());
 		} catch (Exception e) {
 			System.out.print(e.getMessage());
 		}
 
 		changeSinceLastSave = false;
 	}
 
 	/**
 	 * Saves the current work done by the user to the edx server.
 	 */
 	public String saveProblem() {
 		if (cageCollection != null) {
 
 			try {
 				Iterator<CageUI> it = cageCollection.iterator();
 				ArrayList<Cage> al = new ArrayList<Cage>();
 				while (it.hasNext()) {
 					CageUI cui = it.next();
 					Cage c = cui.getCage();
 					al.add(c);
 				}
 				Element xmlDoc = getXMLDoc(al);
 				System.out.println(xmlDoc.toString());
 				changeSinceLastSave = false;
 
 
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		return "";
 	}
 
 
 
 	private Element getXMLDoc(ArrayList<Cage> cages) throws Exception {
 		Document d = XMLParser.createDocument();
 		// creating the whole tree
 		Element root = d.createElement("VglII"); 
 
 		root.appendChild(geneticModel.save());
 		Element organisms = d.createElement("Organisms"); 
 		for (int i = 0; i < cages.size(); i++) {
 			Cage c = cages.get(i);
 			organisms.appendChild(c.save());
 		}
 		root.appendChild(organisms);
 
 //		root.appendChild(modelBuilder.save());
 
 		return root;
 	}
 
 
 
 
 	/**
 	 * Method to release temporary objects and re-initialize objects and
 	 * variables before exiting the application or after closing a problem
 	 */
 	public void cleanUp() {
 		if (cageCollection != null) {
 			Iterator<CageUI> it = cageCollection.iterator();
 			while (it.hasNext()) {
 				CageUI c = it.next();
 				it.remove();
 				c.setVisible(false);
 			}
 		}
 		cageCollection = null;
 		geneticModel = null;
 		selectionVial = null;
 		nextCageId = 1;
 		nextCageScreenPositionX = 0;
 		nextCageScreenPositionY = 0;
 //		SummaryChartManager.getInstance().clearSelectedSet();
 //		SummaryChartManager.getInstance().hideSummaryChart();
 //		if (modelBuilder != null) {
 //			modelBuilder.setVisible(false);
 //		}
 //		if (modelBuilderPanel != null) {
 //			modelBuilderPanel.clear();
 //		}
 	}
 
 	/**
 	 * Method that actually sets up the cross between two organisms
 	 */
 	public void crossTwo(boolean isSuperCross) {
 		OrganismUI organismUI1 = selectionVial.getMaleParent();
 		OrganismUI organismUI2 = selectionVial.getFemaleParent();
 		if (organismUI1 != null && organismUI2 != null) {
 			Organism o1 = organismUI1.getOrganism();
 			Organism o2 = organismUI2.getOrganism();
 
 			int numOffspring = 0;
 			if (isSuperCross) {
 				if (jsVGL.getSuperCrossChoice() == 0) return;
 				numOffspring = jsVGL.getSuperCrossChoice();
 			} else {
 				numOffspring = random.nextInt(geneticModel.getMaxOffspring() - geneticModel.getMinOffspring())
 						+ geneticModel.getMinOffspring();
 			}
 
 			Cage c = geneticModel.crossTwo(nextCageId, 
 					o1, 
 					o2, 
 					numOffspring,
 					isSuperCross);
 
 			CageUI cageUI = createCageUI(c, isSuperCross);
 			OrganismUI[] parentUIs = cageUI.getParentUIs();
 			if (parentUIs[0].getOrganism().isMale() == o1.isMale()) {
 				organismUI1.getReferencesList().add(parentUIs[0]);
 				organismUI2.getReferencesList().add(parentUIs[1]);
 				parentUIs[0].setCentralOrganismUI(organismUI1);
 				parentUIs[1].setCentralOrganismUI(organismUI2);
 			} else {
 				organismUI1.getReferencesList().add(parentUIs[1]);
 				organismUI2.getReferencesList().add(parentUIs[0]);
 				parentUIs[1].setCentralOrganismUI(organismUI1);
 				parentUIs[0].setCentralOrganismUI(organismUI2);
 			}
 //			modelBuilder.updateCageChoices(nextCageId);
 			changeSinceLastSave = true;
 		} else {
 //			JOptionPane.showMessageDialog(this, "Virtual Genetics Lab\n"
 //					+ "Cross Two cannot be carried out without two organisms\n"
 //					+ "Please select two organisms and try again",
 //					"Cross Two", JOptionPane.ERROR_MESSAGE); 
 		}
 	}
 
 
 
 	/**
 	 * sets up and displays new summarychart
 	 */
 	private void summaryChart() {
 		SummaryChartManager.getInstance().showSummaryChart(this);
 	}
 
 	/**
 	 * clears selected cages for summary chart
 	 */
 	private void unselectAll() {
 		SummaryChartManager.getInstance().clearSelectedSet();
 	}
 
 	/**
 	 * This method acutally sets up the Cage's UI.
 	 * 
 	 * @param c
 	 *            The cage object whose UI is to be created
 	 * @return the newly created cageUI
 	 */
 	private CageUI createCageUI(Cage c, boolean isSuperCross) {
 		CageUI dlg = null;
 		String details = null;
 		details = geneticModel.toString();
 		dlg = new CageUI(geneticModel.isBeginnerMode(), 
 				isSuperCross,
 				c, 
 				selectionVial,
 				details, 
 				geneticModel.getNumberOfCharacters(),
 				geneticModel.getScrambledCharacterOrder());
 		nextCageId++;
 		if (dlg != null) {
 			cageCollection.add(dlg);
 			calculateCagePosition(dlg);
 			dlg.show();
 		}
 		c.setCageUI(dlg);
 		return dlg;
 	}
 
 	/**
 	 * This method iterates over the collection of cage objects and sets up the
 	 * UI for each of the cages. This method is invoked when an saved problem is
 	 * reopened for work
 	 * 
 	 * @param cages
 	 *            the list of cages
 	 * @throws Exception
 	 *             in case any or all of the cages are not correct
 	 */
 	private void reopenCages(ArrayList<Cage> cages) throws Exception {
 		Iterator<Cage> it = cages.iterator();
 		while (it.hasNext()) {
 			Cage c = it.next();
 			CageUI cageUI = createCageUI(c, c.isSuperCross());
 
 			/*
 			 *  see if the location and visibility have been saved
 			 *  if not, calculate them
 			 */
 			if (c.getXpos() == -1) {
 				calculateCagePosition(cageUI);
 			} else {
 				cageUI.setPopupPosition(c.getXpos(), c.getYpos());
 			}
 			cageUI.setVisible(c.isVisible());
 
 			if (c.getId() > 0) {
 				OrganismUI[] parentUIs = cageUI.getParentUIs();
 				if (parentUIs == null)
 					System.out.println("No parents found for Cage #:" + c.getId());
 				if (parentUIs[0] == null)
 					System.out.println("No parent0 found for Cage #:" + c.getId());
 				if (parentUIs[1] == null)
 					System.out.println("No parent1 found for Cage #:" + c.getId());
 				Organism o1 = parentUIs[0].getOrganism();
 				Organism o2 = parentUIs[1].getOrganism();
 				int o1_Id = o1.getId();
 				int o2_Id = o2.getId();
 				CageUI cage1 = (CageUI) cageCollection.get(o1.getCageId());
 				CageUI cage2 = (CageUI) cageCollection.get(o2.getCageId());
 				if (cage1 != null && cage2 != null) {
 					OrganismUI originalOUI1 = cage1.getOrganismUIFor(o1_Id);
 					OrganismUI originalOUI2 = cage2.getOrganismUIFor(o2_Id);
 					if (originalOUI1 != null && originalOUI2 != null) {
 						if (parentUIs[0].getOrganism().isMale() == originalOUI1
 								.getOrganism().isMale()) {
 							originalOUI1.getReferencesList().add(parentUIs[0]);
 							originalOUI2.getReferencesList().add(parentUIs[1]);
 							parentUIs[0].setCentralOrganismUI(originalOUI1);
 							parentUIs[1].setCentralOrganismUI(originalOUI2);
 						} else {
 							originalOUI1.getReferencesList().add(parentUIs[1]);
 							originalOUI2.getReferencesList().add(parentUIs[0]);
 							parentUIs[1].setCentralOrganismUI(originalOUI1);
 							parentUIs[0].setCentralOrganismUI(originalOUI2);
 						}
 					} else {
 						System.out
 						.println("For Original Organisms of Parents of Cage #:" + c.getId());
 						if (originalOUI1 == null)
 							System.out.println("Organism for: " + o1.getId() + " " + o1.getCageId() + " not found!"); 
 						if (originalOUI2 == null)
 							System.out.println("Orgnaism for: " + o2.getId() + " " + o2.getCageId() + " not found!"); 
 					}
 				} else {
 					System.out.println("For parents of Cage #: " + c.getId()); 
 					if (cage1 == null)
 						System.out.println("Cage for organism " + o1.getId() + " " + o1.getCageId() + " not found!");
 						System.out.println("Cage for organism " + o2.getId() + " " + o2.getCageId() + " not found!"); 
 				}
 			}
 		}
 	}
 
 	/**
 	 * Method to calculate the position of a cage on the screen
 	 * 
 	 * @param cageUI
 	 *            the cage whose position needs to be calculated
 	 */
 	private void calculateCagePosition(CageUI cageUI) {
         int browserWidth = Window.getClientWidth();
         int browserHeight = Window.getClientHeight();
 
 		if ((nextCageScreenPositionX  > browserWidth)
 				|| (nextCageScreenPositionY > browserHeight)) {
 			nextCageScreenPositionX = 20;
 			nextCageScreenPositionY = 40;
 		}
 		cageUI.setPopupPosition(nextCageScreenPositionX, nextCageScreenPositionY);
 		nextCageScreenPositionX += 30;
 		nextCageScreenPositionY += 30;
 	}
 
 	/*
 	 * get list of current cages 
 	 *    needed to create evidenitary cage list for 
 	 *    ModelBuilder's panels
 	 */
 	public String[] getCageList() {
 		int numCages = 0;
 		if (cageCollection != null) {
 			numCages= cageCollection.size();
 		} 
 
 		String[] list = new String[numCages + 1];
 		list[0] = "?";
 		for (int i = 1; i < numCages + 1; i++) {
 			list[i] = "Cage " + i;
 		}
 		return list;
 	}
 
 
 //	public ModelBuilderUI getModelBuilder() {
 //		return modelBuilder;
 //	}
 
 	public void setChangeSinceLastSave() {
 		changeSinceLastSave = true;
 	}
 
 	public GeneticModel getGeneticModel() {
 		return geneticModel;
 	}
 
 }
 
