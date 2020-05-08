 package org.iucn.sis.shared.api.structures;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.Grid;
 
 public abstract class CriteriaGrid3_1 extends CriteriaGrid {
 
 	protected String[] categoriesForValidation = { "CR", "EN", "VU" };
 
 	public void buildGrid() {
 		gridA = new Grid(4, 5);
 		gridB = new Grid(6, 5);
 		gridC = new Grid(3, 2);
 		gridD = new Grid(3, 1);
 		gridE = new Grid(1, 1);
 
 		for (Grid grid : getGrids()) {
 			grid.setCellSpacing(4);
 			grid.addClickHandler(new ClickHandler() {
 				public void onClick(ClickEvent event) {
 					updateCriteriaString(createCriteriaString());
 				}
 			});
 		}
 
 		gridA.setWidget(0, 0, createWidget("A1a", 0, 0));
 		gridA.setWidget(0, 1, createWidget("A1b", 0, 1));
 		gridA.setWidget(0, 2, createWidget("A1c", 0, 2));
 		gridA.setWidget(0, 3, createWidget("A1d", 0, 3));
 		gridA.setWidget(0, 4, createWidget("A1e", 0, 4));
 		gridA.setWidget(1, 0, createWidget("A2a", 1, 0));
 		gridA.setWidget(1, 1, createWidget("A2b", 1, 1));
 		gridA.setWidget(1, 2, createWidget("A2c", 1, 2));
 		gridA.setWidget(1, 3, createWidget("A2d", 1, 3));
 		gridA.setWidget(1, 4, createWidget("A2e", 1, 4));
 		// gridA.setWidget(2, 0, createWidget("A3a", 2, 0));
 		gridA.setWidget(2, 1, createWidget("A3b", 2, 1));
 		gridA.setWidget(2, 2, createWidget("A3c", 2, 2));
 		gridA.setWidget(2, 3, createWidget("A3d", 2, 3));
 		gridA.setWidget(2, 4, createWidget("A3e", 2, 4));
 		gridA.setWidget(3, 0, createWidget("A4a", 3, 0));
 		gridA.setWidget(3, 1, createWidget("A4b", 3, 1));
 		gridA.setWidget(3, 2, createWidget("A4c", 3, 2));
 		gridA.setWidget(3, 3, createWidget("A4d", 3, 3));
 		gridA.setWidget(3, 4, createWidget("A4e", 3, 4));
 
 		gridB.setWidget(0, 0, createWidget("B1a", 0, 0));
 		gridB.setWidget(1, 0, createWidget("B1b(i)", 1, 0));
 		gridB.setWidget(1, 1, createWidget("B1b(ii)", 1, 1));
 		gridB.setWidget(1, 2, createWidget("B1b(iii)", 1, 2));
 		gridB.setWidget(1, 3, createWidget("B1b(iv)", 1, 3));
 		gridB.setWidget(1, 4, createWidget("B1b(v)", 1, 4));
 		gridB.setWidget(2, 0, createWidget("B1c(i)", 2, 0));
 		gridB.setWidget(2, 1, createWidget("B1c(ii)", 2, 1));
 		gridB.setWidget(2, 2, createWidget("B1c(iii)", 2, 2));
 		gridB.setWidget(2, 3, createWidget("B1c(iv)", 2, 3));
 		// gridB.setWidget(2, 4, createWidget("B1c(v)", 2, 4));
 		gridB.setWidget(3, 0, createWidget("B2a", 3, 0));
 		gridB.setWidget(4, 0, createWidget("B2b(i)", 4, 0));
 		gridB.setWidget(4, 1, createWidget("B2b(ii)", 4, 1));
 		gridB.setWidget(4, 2, createWidget("B2b(iii)", 4, 2));
 		gridB.setWidget(4, 3, createWidget("B2b(iv)", 4, 3));
 		gridB.setWidget(4, 4, createWidget("B2b(v)", 4, 4));
 		gridB.setWidget(5, 0, createWidget("B2c(i)", 5, 0));
 		gridB.setWidget(5, 1, createWidget("B2c(ii)", 5, 1));
 		gridB.setWidget(5, 2, createWidget("B2c(iii)", 5, 2));
 		gridB.setWidget(5, 3, createWidget("B2c(iv)", 5, 3));
 		// gridB.setWidget(5, 4, createWidget("B2c(v)", 5, 4));
 
 		gridC.setWidget(0, 0, createWidget("C1", 0, 0));
 		gridC.setWidget(1, 0, createWidget("C2a(i)", 1, 0));
 		gridC.setWidget(1, 1, createWidget("C2a(ii)", 1, 1));
 		gridC.setWidget(2, 0, createWidget("C2b", 2, 0));
 
 		gridD.setWidget(0, 0, createWidget("D", 0, 0));
 		gridD.setWidget(1, 0, createWidget("D1", 1, 0));
 		gridD.setWidget(2, 0, createWidget("D2", 2, 0));
 
 		gridE.setWidget(0, 0, createWidget("E", 0, 0));
 	}
 
 	@Override
 	public boolean isCriteriaValid(String criteria, String category) {
 		boolean doValidation = false;
 		boolean valid = true;
 		boolean fastFail = false;
 		for (String cat : categoriesForValidation) {
 			if (cat.equalsIgnoreCase(category)) {
 				doValidation = true;
 				break;
 			}
 		}
 		if (doValidation) {
 			valid = false;
 			if (criteria.contains("A"))
 				valid = true;
 			else {
 
 				// CHECK INVALIDITY OF E CRITERIA
 				if (criteria.contains("E")) {
 					valid = true;
 				}
 				// CHECK INVALIDITY OF D CRITERIA
 				if (criteria.contains("D") && !valid) {
 					if (category.equalsIgnoreCase("VU")) {
 						if (!isChecked("D"))
 							return true;
 						else
 							fastFail = true;
 					} else {
 						if (!isChecked("D1") && !isChecked("D2")) {
 							valid = true;
 						} else {
 							fastFail = true;
 						}
 
 					}
 				}
 				// CHECK INVALIDITY OF C CRITERIA
 				if (!fastFail && criteria.contains("C") && !valid) {
 					boolean contains1 = isChecked("C1");
					boolean contains2 = isChecked("C2a(i)") || isChecked("C2a(ii)") || isChecked("C2b");
 					if (!(contains1 && contains2)) {
 						valid = true;
 					} else {
 						fastFail = true;
 					}
 				}
 				// CHECK INVALIDITY OF B CRITERIA
 				if (!fastFail && criteria.contains("B") && !valid) {
 
 					int numberB1Checked = 0;
 					int numberB2Checked = 0;
 
 					String[] b1bWidgets = { "B1b(i)", "B1b(ii)", "B1b(iii)", "B1b(iv)", "B1b(v)" };
 					String[] b1cWidgets = { "B1c(i)", "B1c(ii)", "B1c(iii)", "B1c(iv)" };
 					String[] b2bWidgets = { "B2b(i)", "B2b(ii)", "B2b(iii)", "B2b(iv)", "B2b(v)" };
 					String[] b2cWidgets = { "B2c(i)", "B2c(ii)", "B2c(iii)", "B2c(iv)" };
 
 					if (isChecked("B1a")) {
 						numberB1Checked++;
 					}
 					for (String possible : b1bWidgets) {
 						if (isChecked(possible)) {
 							numberB1Checked++;
 							break;
 
 						}
 
 					}
 					for (String possible : b1cWidgets) {
 						if (isChecked(possible)) {
 							numberB1Checked++;
 							break;
 						}
 					}
 					if (numberB1Checked >= 2)
 						valid = true;
 					if (!valid) {
 
 						if (isChecked("B2a")) {
 							numberB2Checked++;
 						}
 						for (String possible : b2bWidgets) {
 							if (isChecked(possible)) {
 								numberB2Checked++;
 								break;
 							}
 						}
 						for (String possible : b2cWidgets) {
 							if (isChecked(possible)) {
 								numberB2Checked++;
 								break;
 							}
 						}
 						if (numberB2Checked >= 2)
 							valid = true;
 					}
 
 				}
 
 			}
 		}
 		return valid && !fastFail;
 	}
 
 }
