 package edu.umb.jsVGL.client.Grader;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.xml.client.Document;
 import com.google.gwt.xml.client.Element;
 import com.google.gwt.xml.client.XMLParser;
 
 import edu.umb.jsVGL.client.GeneticModels.Cage;
 import edu.umb.jsVGL.client.GeneticModels.GeneModel;
 import edu.umb.jsVGL.client.GeneticModels.GeneticModel;
 import edu.umb.jsVGL.client.GeneticModels.PhenotypeProcessor;
 import edu.umb.jsVGL.client.ModelBuilder.ModelBuilderUI;
 import edu.umb.jsVGL.client.ModelBuilder.ModelDetailsPanel;
 import edu.umb.jsVGL.client.ModelBuilder.ModelPane;
 import edu.umb.jsVGL.client.VGL.CageUI;
 
 /*
  * Compares correct answer (GeneticModel) with student answer (ModelBuilderUI)
  * returns info for direct on-line grading
  * = Success or failure in matching - not details of their answer
  * this is meant to be machine readable, not human readable
  * 
  * also includes html result that summarizes this
  *  the html is human readable
  */
 public class AutoGrader {
 
 	// allowable difference between
 	private static final float ERROR_TOLERANCE = 0.1f;
 
 	public static GradeResult grade(ArrayList<CageUI> cageCollection, GeneticModel gm, ModelBuilderUI mbui) {
 		
 		// for grade HTML
 		StringBuffer b = new StringBuffer(); 
 		b.append("<html><body>");
 		// start with true; if any false then overall is false
 		boolean allModelCorrect = true;
 		boolean allModelJustified = true;
 		
 		// set up to score cages
 		Iterator<CageUI> it = cageCollection.iterator();
 		ArrayList<Cage> cages = new ArrayList<Cage>();
 		while (it.hasNext()) {
 			CageUI cui = it.next();
 			Cage c = cui.getCage();
 			cages.add(c);
 		}
 		CageScorer cageScorer = new CageScorer(cages, mbui);
 
 		Document doc = XMLParser.createDocument();
 		Element e = doc.createElement("Grade");  // root element
 
 		// some basic info
 		Element p = doc.createElement("Problem");
 		
 		Element pNameEl = doc.createElement("ProblemFileName");
 		pNameEl.appendChild(doc.createTextNode(gm.getProblemFileName()));
 		p.appendChild(pNameEl);
 		
 		Element pModeEl = doc.createElement("PracticeMode");
 		pModeEl.appendChild(doc.createTextNode(String.valueOf(gm.isBeginnerMode())));
 		e.appendChild(pModeEl);
		
		Element numCageEl = doc.createElement("NumCages");
		numCageEl.appendChild(doc.createTextNode(String.valueOf(cages.size())));
		e.appendChild(numCageEl);
 
 		/*
 		 * Then, see if there's epistasis or complementation
 		 * if there is, then the #geneModels > #characters
 		 * so you have to be careful and process it differently
 		 */
 		if (gm.getPhenoTypeProcessor().getInteractionType() == PhenotypeProcessor.NO_INTERACTION) {
 			for (int i = 0; i < gm.getNumberOfGeneModels(); i++) {
 				Element geneEl = doc.createElement("Gene");
 
 				Element indexEl = doc.createElement("Index");
 				indexEl.appendChild(doc.createTextNode(String.valueOf(i)));
 				geneEl.appendChild(indexEl);
 
 				// get right answer and student answer
 				GeneModel geneModel = gm.getGeneModelByIndex(i);
 				ModelPane modelPane = mbui.getModelPanes()[i];
 
 				// the character
 				Element charEl = doc.createElement("Character");
 				charEl.appendChild(doc.createTextNode(geneModel.getCharacter()));
 				geneEl.appendChild(charEl);
 
 				/*
 				 * if this is null, it means that there was no choice with this
 				 * menu - that is, it was certain to be sex-linked or autosomal
 				 * so it's not appropriate to give a grade here
 				 */
 				if (modelPane.getSexLinkageChoice() != null) {
 					boolean sexLinkageCorrect = false;
 					if (gm.isGeneModelSexLinkedByIndex(i)) {
 						if (gm.getSexLinkageType()) {
 							// XX/XY
 							if (modelPane.getSexLinkageChoice().equals("XX Female/XY Male")) sexLinkageCorrect = true;
 						} else {
 							// ZZ/ZW
 							if (modelPane.getSexLinkageChoice().equals("ZZ Male/ZW Female")) sexLinkageCorrect = true;
 						}
 					} else {
 						// not sex-linked
 						if (modelPane.getSexLinkageChoice().equals("Not Sex-Linked")) sexLinkageCorrect = true;
 					}
 					
 					// if any wrong, mark the problem wrong
 					if (!sexLinkageCorrect) allModelCorrect = false; 
 					
 					// now the xml
 					Element slEl = doc.createElement("SexLinkage");
 					Element slCorEl = doc.createElement("Correct");
 					slCorEl.appendChild(doc.createTextNode(String.valueOf(sexLinkageCorrect)));
 					slEl.appendChild(slCorEl);
 
 					CageScoreResult slCsr = cageScorer.scoreCage(modelPane.getSexLinkageCageChoice());
 					if (slCsr == null) {
 						Element slJusEl = doc.createElement("Justified");
 						slJusEl.appendChild(doc.createTextNode(String.valueOf(false)));
 						slEl.appendChild(slJusEl);
 						allModelJustified = false;
 					} else {
 						// sex-linkage choice must match correct type
 						if(gm.isGeneModelSexLinkedByIndex(i)) {
 							// sex-linked, so cage must show sex-linkage
 							Element slJusEl = doc.createElement("Justified");
 							slJusEl.appendChild(
 									doc.createTextNode(
 											String.valueOf(slCsr.getCageScoreForCharacter(i).showsSexLinkage)));
 							slEl.appendChild(slJusEl);
 							if (!slCsr.getCageScoreForCharacter(i).showsSexLinkage) allModelJustified = false;
 						} else {
 							// not sex-linked, so cage must not show sex-linkage
 							Element slJusEl = doc.createElement("Justified");
 							slJusEl.appendChild(
 									doc.createTextNode(
 											String.valueOf(!slCsr.getCageScoreForCharacter(i).showsSexLinkage)));
 							slEl.appendChild(slJusEl);
 							if (slCsr.getCageScoreForCharacter(i).showsSexLinkage) allModelJustified = false;
 						}
 					}
 					geneEl.appendChild(slEl);
 				}
 
 				/*
 				 * number of alleles
 				 * 
 				 * first see if there was really a choice for the student here
 				 */
 				boolean numAllelesCorrect = true;
 				// see if they really had a choice or not
 				if (modelPane.getAlleleNumberChoice() != 0) {
 					numAllelesCorrect = (modelPane.getAlleleNumberChoice() == geneModel.getNumAlleles());
 					Element naEl = doc.createElement("NumberOfAlleles");
 					Element naCorEl = doc.createElement("Correct");
 					naCorEl.appendChild(
 							doc.createTextNode(String.valueOf(numAllelesCorrect)));
 					naEl.appendChild(naCorEl);
 					geneEl.appendChild(naEl);
 					if (!numAllelesCorrect) allModelCorrect = false; 
 				}
 
 
 				/*
 				 * these are the raw selected strings (eg "Simple dominance") 
 				 * 
 				 * also, can't get type right if number of alleles is wrong
 				 */
 				boolean interactionTypeCorrect = false;
 				if (numAllelesCorrect) {
 					if (modelPane.getInteractionTypeChoice() != null) {
 						String studentDomTypeText = modelPane.getInteractionTypeChoice();
 						if (geneModel.getDomTypeText().equals("Simple") 
 								&& studentDomTypeText.equals("Simple Dominance")) interactionTypeCorrect = true;
 						if (geneModel.getDomTypeText().equals("Circular")
 								&& studentDomTypeText.equals("Circular Dominance")) interactionTypeCorrect = true;
 						if (geneModel.getDomTypeText().equals("Hierarchical")
 								&& studentDomTypeText.equals("Hierarchical Dominance")) interactionTypeCorrect = true;
 						if (geneModel.getDomTypeText().equals("Incomplete")
 								&& studentDomTypeText.equals("Incomplete Dominance")) interactionTypeCorrect = true;
 						Element itEl = doc.createElement("InteractionType");
 						Element itCorEl = doc.createElement("Correct");
 						itCorEl.appendChild(doc.createTextNode(String.valueOf(interactionTypeCorrect)));
 						itEl.appendChild(itCorEl);
 						geneEl.appendChild(itEl);
 						if (!interactionTypeCorrect) allModelCorrect = false;
 					} else {
 						interactionTypeCorrect = true;	// if they didn't have to enter it, it's OK 
 					}
 				}
 
 				/*
 				 * these are also the raw selected strings
 				 * in the local language, so need to match with translated version
 				 * 
 				 * first, see if type is OK
 				 * 	if not, then the details can't be right
 				 */
 				boolean detailsCorrect = true;
 				if (!interactionTypeCorrect) {
 					detailsCorrect = false;
 				} else {
 					/*
 					 */
 					ModelDetailsPanel mdp = modelPane.getModelDetailsPanel();
 					/*
 					 * first, deal with special cases
 					 * 	in inc dom, the homozygote traits are exchangable
 					 * 		(they don't have to match exactly, as long as the pair is right)
 					 * 		2-allele: t1 and t3 can be swapped as long as both are right
 					 * 		3-allele: 3 circular permutations are all OK
 					 * in circ dom, the circular permutations are all ok
 					 * 
 					 */				
 					if ((geneModel.getNumAlleles() == 2) && (geneModel.getDomTypeText().equals("Incomplete"))) {
 						detailsCorrect = false;
 						// check to be sure they've instantiated the choices
 						if (mdp.t1Choices != null) {
 							// check het pheno first - if it's wrong, give up
 							if (mdp.t3Choices.getItemText(mdp.t3Choices.getSelectedIndex()).equals(geneModel.t3.getTraitName())) {
 
 								// try either permutation of the homozygotes
 								// direct match
 								if (
 										(mdp.t1Choices.getItemText(mdp.t1Choices.getSelectedIndex()).equals(geneModel.t1.getTraitName())) &&
 										(mdp.t2Choices.getItemText(mdp.t2Choices.getSelectedIndex()).equals(geneModel.t2.getTraitName())) 
 										){
 									detailsCorrect = true;
 								}
 								// swapped
 								if (
 										(mdp.t1Choices.getItemText(mdp.t1Choices.getSelectedIndex()).equals(geneModel.t2.getTraitName())) &&
 										(mdp.t2Choices.getItemText(mdp.t2Choices.getSelectedIndex()).equals(geneModel.t1.getTraitName())) 
 										){
 									detailsCorrect = true;
 								}
 							} 
 						}
 
 					} else if ((geneModel.getNumAlleles() == 3) && (geneModel.getDomTypeText().equals("Incomplete"))) {
 						detailsCorrect = false;
 						/* only 3 possibilities to be correct
 						 * 	normal slot		alternatives
 						 *		1			1	3	2
 						 *		2			2	1	3
 						 *		3			3	2	1
 						 *		4			4	6	5
 						 *		5			5	4	6
 						 *		6			6	5	4
 						 * use the one found at slot 1 to decide which case it is, then go into detail
 						 */
 						// check to be sure they've instantiated the choices
 						if (mdp.t1Choices != null) {
 							// then, see which permutation it is
 							if (mdp.t1Choices.getItemText(mdp.t1Choices.getSelectedIndex()).equals(geneModel.t1.getTraitName())) {
 								// first alternative; provisionally right unless a mismatch
 								detailsCorrect = true;
 								if (!mdp.t2Choices.getItemText(mdp.t2Choices.getSelectedIndex()).equals(geneModel.t2.getTraitName())) detailsCorrect = false;
 								if (!mdp.t3Choices.getItemText(mdp.t3Choices.getSelectedIndex()).equals(geneModel.t3.getTraitName())) detailsCorrect = false;
 								if (!mdp.t4Choices.getItemText(mdp.t4Choices.getSelectedIndex()).equals(geneModel.t4.getTraitName())) detailsCorrect = false;
 								if (!mdp.t5Choices.getItemText(mdp.t5Choices.getSelectedIndex()).equals(geneModel.t5.getTraitName())) detailsCorrect = false;
 								if (!mdp.t6Choices.getItemText(mdp.t6Choices.getSelectedIndex()).equals(geneModel.t6.getTraitName())) detailsCorrect = false;
 
 							} else if (mdp.t1Choices.getItemText(mdp.t1Choices.getSelectedIndex()).equals(geneModel.t3.getTraitName())){
 								// second alternative; provisionally right unless a mismatch
 								detailsCorrect = true;
 								if (!mdp.t2Choices.getItemText(mdp.t2Choices.getSelectedIndex()).equals(geneModel.t1.getTraitName())) detailsCorrect = false;
 								if (!mdp.t3Choices.getItemText(mdp.t3Choices.getSelectedIndex()).equals(geneModel.t2.getTraitName())) detailsCorrect = false;
 								if (!mdp.t4Choices.getItemText(mdp.t4Choices.getSelectedIndex()).equals(geneModel.t6.getTraitName())) detailsCorrect = false;
 								if (!mdp.t5Choices.getItemText(mdp.t5Choices.getSelectedIndex()).equals(geneModel.t4.getTraitName())) detailsCorrect = false;
 								if (!mdp.t6Choices.getItemText(mdp.t6Choices.getSelectedIndex()).equals(geneModel.t5.getTraitName())) detailsCorrect = false;
 
 							} else if (mdp.t1Choices.getItemText(mdp.t1Choices.getSelectedIndex()).equals(geneModel.t2.getTraitName())) {
 								// third alternative; provisionally right unless a mismatch
 								detailsCorrect = true;
 								if (!mdp.t2Choices.getItemText(mdp.t2Choices.getSelectedIndex()).equals(geneModel.t3.getTraitName())) detailsCorrect = false;
 								if (!mdp.t3Choices.getItemText(mdp.t3Choices.getSelectedIndex()).equals(geneModel.t1.getTraitName())) detailsCorrect = false;
 								if (!mdp.t4Choices.getItemText(mdp.t4Choices.getSelectedIndex()).equals(geneModel.t5.getTraitName())) detailsCorrect = false;
 								if (!mdp.t5Choices.getItemText(mdp.t5Choices.getSelectedIndex()).equals(geneModel.t6.getTraitName())) detailsCorrect = false;
 								if (!mdp.t6Choices.getItemText(mdp.t6Choices.getSelectedIndex()).equals(geneModel.t4.getTraitName())) detailsCorrect = false;
 							}
 							// none of the alternatives, so details aren't correct
 						}
 					} else if ((geneModel.getNumAlleles() == 3) && (geneModel.getDomTypeText().equals("Circular"))) {
 						detailsCorrect = false;
 						/*
 						 * 		normal slot		alternatives
 						 *			1			1	3	2
 						 *			2			2	1	3
 						 *			3			3	2	1
 						 */
 						// check to be sure they've instantiated the choices
 						if (mdp.t1Choices != null) {
 							// then, see which permutation it is
 							if (mdp.t1Choices.getItemText(mdp.t1Choices.getSelectedIndex()).equals(geneModel.t1.getTraitName())) {
 								// first alternative; provisionally right unless a mismatch
 								detailsCorrect = true;
 								if (!mdp.t2Choices.getItemText(mdp.t2Choices.getSelectedIndex()).equals(geneModel.t2.getTraitName())) detailsCorrect = false;
 								if (!mdp.t3Choices.getItemText(mdp.t3Choices.getSelectedIndex()).equals(geneModel.t3.getTraitName())) detailsCorrect = false;
 
 							} else if (mdp.t1Choices.getItemText(mdp.t1Choices.getSelectedIndex()).equals(geneModel.t3.getTraitName())){
 								// second alternative; provisionally right unless a mismatch
 								detailsCorrect = true;
 								if (!mdp.t2Choices.getItemText(mdp.t2Choices.getSelectedIndex()).equals(geneModel.t1.getTraitName())) detailsCorrect = false;
 								if (!mdp.t3Choices.getItemText(mdp.t3Choices.getSelectedIndex()).equals(geneModel.t2.getTraitName())) detailsCorrect = false;
 
 							} else if (mdp.t1Choices.getItemText(mdp.t1Choices.getSelectedIndex()).equals(geneModel.t2.getTraitName())) {
 								// third alternative; provisionally right unless a mismatch
 								detailsCorrect = true;
 								if (!mdp.t2Choices.getItemText(mdp.t2Choices.getSelectedIndex()).equals(geneModel.t3.getTraitName())) detailsCorrect = false;
 								if (!mdp.t3Choices.getItemText(mdp.t3Choices.getSelectedIndex()).equals(geneModel.t1.getTraitName())) detailsCorrect = false;
 
 							}
 							// none of the alternatives, so details aren't correct
 						}
 					} else {
 						/*
 						 * it's not one of the exceptions with multiple possibilities
 						 * there's only one right answer here, so:
 						 * check each entry; if any mismatch, it's wrong
 						 */
 						if ((mdp.t1Choices != null) && (geneModel.t1 != null)) {
 							if (!mdp.t1Choices.getItemText(mdp.t1Choices.getSelectedIndex()).equals(geneModel.t1.getTraitName())) detailsCorrect = false;
 						}
 
 						if ((mdp.t2Choices != null) && (geneModel.t2 != null)) {
 							if (!mdp.t2Choices.getItemText(mdp.t2Choices.getSelectedIndex()).equals(geneModel.t2.getTraitName())) detailsCorrect = false;
 						}
 
 						if ((mdp.t3Choices != null) && (geneModel.t3 != null)) {
 							if (!mdp.t3Choices.getItemText(mdp.t3Choices.getSelectedIndex()).equals(geneModel.t3.getTraitName())) detailsCorrect = false;
 						}
 
 						if ((mdp.t4Choices != null) && (geneModel.t4 != null)) {
 							if (!mdp.t4Choices.getItemText(mdp.t4Choices.getSelectedIndex()).equals(geneModel.t4.getTraitName())) detailsCorrect = false;
 						}
 
 						if ((mdp.t5Choices != null) && (geneModel.t5 != null)) {
 							if (!mdp.t5Choices.getItemText(mdp.t5Choices.getSelectedIndex()).equals(geneModel.t5.getTraitName())) detailsCorrect = false;
 						}
 
 						if ((mdp.t6Choices != null) && (geneModel.t6 != null)) {
 							if (!mdp.t6Choices.getItemText(mdp.t6Choices.getSelectedIndex()).equals(geneModel.t6.getTraitName())) detailsCorrect = false;
 						}
 					}
 				}
 				Element idEl = doc.createElement("InteractionDetails");
 				Element idCorEl = doc.createElement("Correct");
 				idCorEl.appendChild(doc.createTextNode(String.valueOf(detailsCorrect)));
 				idEl.appendChild(idCorEl);
 				if (!detailsCorrect) allModelCorrect = false;
 				
 				int interactionCageChoice = modelPane.getInteractionCageChoice();
 				CageScoreResult inCsr = cageScorer.scoreCage(interactionCageChoice);
 				Element idJusEl = doc.createElement("Justified");
 				if (inCsr == null) {
 					idJusEl.appendChild(doc.createTextNode(String.valueOf(false)));
 					idEl.appendChild(idJusEl);
 					allModelJustified = false;
 				} else {
 					idJusEl.appendChild(doc.createTextNode(String.valueOf(inCsr.getCageScoreForCharacter(i).showsInteraction)));
 					idEl.appendChild(idJusEl);
 					if (!inCsr.getCageScoreForCharacter(i).showsInteraction) allModelJustified = false;
 				}
 				geneEl.appendChild(idEl);
 
 				e.appendChild(geneEl);
 			}
 
 		} else {
 			/*
 			 * it's epistasis or complementation, so it must be treated differently
 			 * there will only be one trait but two genes
 			 * (technically, it is possible to have a third gene in these problems
 			 *   but we have not made any problem types with that option; if we did this
 			 *   the grading would break)
 			 *   
 			 */
 			Element gmEl = doc.createElement("Character");
 
 			Element charNameEl = doc.createElement("Name");
 			charNameEl.appendChild(doc.createTextNode(gm.getPhenoTypeProcessor().getCharacter()));
 			gmEl.appendChild(charNameEl);
 
 			boolean interactionTypeGrade = false;
 			ModelPane mp = mbui.getModelPanes()[0]; // only one model pane in these problems
 			if (gm.getPhenoTypeProcessor().getInteractionType() == PhenotypeProcessor.COMPLEMENTATION) {
 				if (mp.getInteractionTypeChoice().equals("Complementation")) interactionTypeGrade = true;
 			} else {
 				if (mp.getInteractionTypeChoice().equals("Epistasis")) interactionTypeGrade = true;
 			}
 			Element itEl = doc.createElement("InteractionType");
 			Element itCorEl = doc.createElement("Correct");
 			itCorEl.appendChild(doc.createTextNode(String.valueOf(interactionTypeGrade)));
 			itEl.appendChild(itCorEl);
 			CageScoreResult inCsr = cageScorer.scoreCage(mp.getInteractionCageChoice());
 			Element itJusEl = doc.createElement("Justified");
 			if (inCsr == null) {
 				itJusEl.appendChild(doc.createTextNode(String.valueOf(false)));
 				itEl.appendChild(itJusEl);
 				allModelJustified = false;
 			} else {
 				itJusEl.appendChild(doc.createTextNode(
 						String.valueOf(inCsr.getCageScoreForCharacter(0).showsInteraction)));
 				itEl.appendChild(itJusEl);
 				if (!inCsr.getCageScoreForCharacter(0).showsInteraction) allModelJustified = false;
 			}
 			gmEl.appendChild(itEl);
 
 			/*
 			 * these are also the raw selected strings
 			 * 
 			 * but, if the type is wrong, the details CAN'T be right
 			 */
 			boolean detailsCorrect = true;
 			if (!interactionTypeGrade) {
 				detailsCorrect = false;
 			} else {
 				ModelDetailsPanel mdp = mp.getModelDetailsPanel();
 				// check each entry; if any mismatch, it's wrong
 				if ((mdp.t1Choices != null) && (gm.getPhenoTypeProcessor().getT1() != null)) {
 					if (!mdp.t1Choices.getItemText(mdp.t1Choices.getSelectedIndex()).equals(gm.getPhenoTypeProcessor().getT1().getTraitName())) detailsCorrect = false;
 				}
 
 				if ((mdp.t2Choices != null) && (gm.getPhenoTypeProcessor().getT2() != null)) {
 					if (!mdp.t2Choices.getItemText(mdp.t2Choices.getSelectedIndex()).equals(gm.getPhenoTypeProcessor().getT2().getTraitName())) detailsCorrect = false;
 				}
 
 				if ((mdp.t3Choices != null) && (gm.getPhenoTypeProcessor().getT3() != null)) {
 					if (!mdp.t3Choices.getItemText(mdp.t3Choices.getSelectedIndex()).equals(gm.getPhenoTypeProcessor().getT3().getTraitName())) detailsCorrect = false;
 				}
 			}
 			Element dEl = doc.createElement("InteractionDetails");
 			Element dCorEl = doc.createElement("Correct");
 			dCorEl.appendChild(doc.createTextNode(String.valueOf(detailsCorrect)));
 			dEl.appendChild(dCorEl);
 			gmEl.appendChild(dEl);
 			if (!detailsCorrect) allModelCorrect = false;
 
 			e.appendChild(gmEl);
 		}
 
 		/*
 		 * linkage, if present
 		 */
 		if (mbui.getLinkagePanel() != null) {
 			/*
 			 * find data for each gene pair
 			 *  12, 23, 13
 			 * encode like this:
 			 * 	rf = 0.5 => unlinked
 			 *  rf < 0.5 => linked with given rf
 			 */
 			double rf12 = 0.5f;
 			double rf23 = 0.5f;
 			double rf13 = 0.5f;
 
 			// see if both on same chromosome
 			// always have 2 genes here
 			if (gm.isGeneModelSexLinkedByIndex(0) && gm.isGeneModelSexLinkedByIndex(1)) {
 				rf12 = gm.getSexChromosomeModel().getRecombinationFrequencies().get(0);
 			}
 			if (!gm.isGeneModelSexLinkedByIndex(0) && !gm.isGeneModelSexLinkedByIndex(1)) {
 				rf12 = gm.getAutosomeModel().getRecombinationFrequencies().get(0);
 			}
 
 			// see if you need to check the other two
 			if (gm.getNumberOfGeneModels() == 3) {
 				// do gene 2 and 3
 				if (gm.isGeneModelSexLinkedByIndex(1) && gm.isGeneModelSexLinkedByIndex(2)) {
 					rf23 = gm.getSexChromosomeModel().getRecombinationFrequencies().get(1);
 				}
 				if (!gm.isGeneModelSexLinkedByIndex(1) && !gm.isGeneModelSexLinkedByIndex(2)) {
 					rf23 = gm.getAutosomeModel().getRecombinationFrequencies().get(1);
 				}
 
 				/*
 				 * now gene 1 and 3
 				 *   this depends on if 1 and 2 are linked
 				 *     if they're not, it's the first rf in the list
 				 *     otherwise, you have to calculate it using the Kosambi formula
 				 *       for adding rfs
 				 */
 				if (gm.isGeneModelSexLinkedByIndex(1) && gm.isGeneModelSexLinkedByIndex(2)) {
 					if (rf12 == 0.5f) {
 						rf13 = gm.getSexChromosomeModel().getRecombinationFrequencies().get(0);
 					} else {
 						rf13 = 0.5 * Math.tanh(2 *(rf12 + rf23));
 					}
 				}
 				if (!gm.isGeneModelSexLinkedByIndex(1) && !gm.isGeneModelSexLinkedByIndex(2)) {
 					if (rf12 == 0.5f) {
 						rf13 = gm.getAutosomeModel().getRecombinationFrequencies().get(0);
 					} else {
 						rf13 = 0.5 * Math.tanh(2 *(rf12 + rf23));
 
 					}
 				}
 
 			}
 
 			// now, see if they're right
 			boolean linkageCorrect = true;
 			double totalError = 0.0f;
 			double maxError = Double.MIN_VALUE;
 			// always have 1-2
 			double student_rf12 = mbui.getLinkagePanel().getG1G2LinkageChoice();
 			if (student_rf12 != -1.0f) {
 				double error12 = Math.abs(rf12 - student_rf12);
 				totalError = totalError + error12;
 				if (error12 > maxError) maxError = error12;
 				if (error12 > ERROR_TOLERANCE) linkageCorrect = false;
 			} else {
 				linkageCorrect = false;
 			}
 
 			if (gm.getNumberOfGeneModels() == 3) {
 				double student_rf23 = mbui.getLinkagePanel().getG2G3LinkageChoice();
 				if (student_rf23 != -1.0f) {
 					double error23 = Math.abs(rf23 - student_rf23);
 					totalError = totalError + error23;
 					if (error23 > maxError) maxError = error23;
 					if (error23 > ERROR_TOLERANCE) linkageCorrect = false;
 				} else {
 					linkageCorrect = false;
 				}
 
 				double student_rf13 = mbui.getLinkagePanel().getG1G3LinkageChoice();
 				if (student_rf13 != -1.0f) {
 					double error13 = Math.abs(rf13 - student_rf13);
 					totalError = totalError + error13;
 					if (error13 > maxError) maxError = error13;
 					if (error13 > ERROR_TOLERANCE) linkageCorrect = false;				
 				} else {
 					linkageCorrect = false;
 				}
 			}
 			Element le = doc.createElement("Linkage");
 			Element lCorEl = doc.createElement("Correct");
 			lCorEl.appendChild(doc.createTextNode(String.valueOf(linkageCorrect)));
 			double averageError = totalError/gm.getNumberOfGeneModels();
 			lCorEl.setAttribute("AverageError", NumberFormat.getFormat("0.000").format(averageError));
 			lCorEl.setAttribute("MaxError", NumberFormat.getFormat("0.000").format(maxError));
 			le.appendChild(lCorEl);
 			if (!linkageCorrect) allModelCorrect = false;
 
 			// see if justified by right cages
 			boolean linkageJustified = true;
 			/*
 			 * always do 1-2
 			 *   use a negative test - if any fail, then fail overall
 			 */
 			CageScoreResult g1g2LinkageCageChoiceResult = cageScorer.scoreCage(mbui.getLinkagePanel().getG1G2LinkageRelevantCage());
 			if (g1g2LinkageCageChoiceResult == null) {
 				linkageJustified = false;  // they selected "?"
 			} else {
 				if (!g1g2LinkageCageChoiceResult.getCageScoreForCharacter(0).capableOfShowingLinkage || 
 						!g1g2LinkageCageChoiceResult.getCageScoreForCharacter(1).capableOfShowingLinkage) linkageJustified = false;
 			}
 
 			// if needed, then do 2-3 and 1-3
 			if (gm.getNumberOfGeneModels() == 3) {
 				CageScoreResult g2g3LinkageCageChoiceResult = cageScorer.scoreCage(mbui.getLinkagePanel().getG2G3LinkageRelevantCage());
 				if (g2g3LinkageCageChoiceResult == null) {
 					linkageJustified = false;  // they selected "?"
 				} else {
 					if (!g2g3LinkageCageChoiceResult.getCageScoreForCharacter(1).capableOfShowingLinkage || 
 							!g2g3LinkageCageChoiceResult.getCageScoreForCharacter(2).capableOfShowingLinkage) linkageJustified = false;
 				}
 
 				CageScoreResult g1g3LinkageCageChoiceResult = cageScorer.scoreCage(mbui.getLinkagePanel().getG1G3LinkageRelevantCage());
 				if (g1g3LinkageCageChoiceResult == null) {
 					linkageJustified = false;  // they selected "?"
 				} else {
 					if (!g1g3LinkageCageChoiceResult.getCageScoreForCharacter(0).capableOfShowingLinkage || 
 							!g1g3LinkageCageChoiceResult.getCageScoreForCharacter(2).capableOfShowingLinkage) linkageJustified = false;
 				}
 			}	
 
 			Element lJusEl = doc.createElement("Justified");
 			lJusEl.appendChild(doc.createTextNode(String.valueOf(linkageJustified)));
 			le.appendChild(lJusEl);
 			e.appendChild(le);
 			if (!linkageJustified) allModelJustified = false;
 		}
 		
 		b.append("<ul>\n");
 		if (allModelCorrect) {
 			b.append("<li><font color='green'>All parts of the genetic model you entered are correct.</font></li>\n");
 		} else {
 			b.append("<li><font color='red'>One or more parts of the genetic model you entered are incorrect.</font></li>\n");
 		}
 		if (allModelJustified) {
 			b.append("<li><font color='green'>All the cage(s) you selected to justify your model show relevant evidence.</font></li>\n");
 		} else {
 			b.append("<li><font color='red'>One or more of the cage(s) you selected to justify your model do not show relevant evidence.</font></li>\n");
 		}
 		
 		return new GradeResult(b.toString(), e.toString());
 	}
 }
