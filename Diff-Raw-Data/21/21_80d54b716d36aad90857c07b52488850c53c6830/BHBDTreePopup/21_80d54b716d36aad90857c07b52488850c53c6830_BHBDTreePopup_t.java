 package org.bh.plugin.gcc.branchspecific;
 
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPopupMenu;
 import javax.swing.tree.DefaultTreeModel;
 import org.bh.gui.swing.comp.BHComboBox.Item;
 import org.bh.data.types.StringValue;
 import org.bh.data.DTOBranch;
 import org.bh.data.DTOBusinessData;
 import org.bh.data.DTOCompany;
 import org.bh.data.DTOPeriod;
 import org.bh.data.IPeriodicalValuesDTO;
 import org.bh.platform.i18n.BHTranslator;
 import org.bh.platform.i18n.ITranslator;
 import org.bh.plugin.gcc.data.DTOGCCBalanceSheet;
 import org.bh.plugin.gcc.data.DTOGCCProfitLossStatementCostOfSales;
 import org.bh.plugin.gcc.data.DTOGCCProfitLossStatementTotalCost;
 
 /**
  * <short_description>
  * 
  * <p>
  * <detailed_description>
  * 
  * @author simon
  * @version 1.0, 18.01.2012
  * 
  */
 public class BHBDTreePopup extends JPopupMenu {
 	ITranslator translator;
 	static BHBDTree bhbdTree;
 
 	public enum Key {
 		BranchAdd, BranchRemove, CompanyAdd, CompanyRemove, PeriodAdd, CompanyAddDesc, PeriodRemove, YearError, YearInsert, TypeChoose, CostOfSales, TotalCosts, TypeChooseTop;
 
 		@Override
 		public String toString() {
 			return getClass().getName() + "." + super.toString();
 		}
 	}
 
 	public enum Type {
 		BRANCH, COMPANY, PERIOD;
 	}
 
 	public BHBDTreePopup(Type type, BHBDTree bhbdTree) {
 		this.bhbdTree = bhbdTree;
 		translator = BHTranslator.getInstance();
 		if (type == Type.BRANCH) {
 
 			JMenuItem brem = new JMenuItem(
 					translator.translate(BHBDTreePopup.Key.BranchRemove));
 
 			brem.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent ae) {
 
 					// REM NODE
 					BHBusinessDataTreeNode treenode = (BHBusinessDataTreeNode) BHBDTreePopup.bhbdTree
 							.getSelectionPath().getLastPathComponent();
 					BHBusinessDataTreeNode parentbranch = (BHBusinessDataTreeNode) treenode
 							.getParent();
 					DTOBusinessData mybd = (DTOBusinessData) parentbranch
 							.getUserObject();
 					mybd.removeChild((DTOBranch) treenode.getUserObject());
 					DefaultTreeModel model = (DefaultTreeModel) BHBDTreePopup.bhbdTree
 							.getModel();
 					model.removeNodeFromParent(treenode);
 					String s = ((DTOBranch) treenode.getUserObject()).get(
 							DTOBranch.Key.BRANCH_KEY_MAIN_CATEGORY).toString()
 							+ "."
 							+ ((DTOBranch) treenode.getUserObject()).get(
 									DTOBranch.Key.BRANCH_KEY_MID_CATEGORY)
 									.toString()
 							+ "."
 							+ ((DTOBranch) treenode.getUserObject()).get(
 									DTOBranch.Key.BRANCH_KEY_SUB_CATEGORY)
 									.toString();
 					MaintainCompanyDataFrame.branchbox
 							.addItem(new Item(s, null));
 
 				}
 			});
 
 			this.add(brem);
 			this.addSeparator();
 			JMenuItem compadd = new JMenuItem(
 					translator.translate(BHBDTreePopup.Key.CompanyAdd));
 			compadd.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent ae) {
 
 					// NEW NODE
 					String inputname = JOptionPane.showInputDialog(
 							null,
 							translator
 									.translate(BHBDTreePopup.Key.CompanyAddDesc),
 							translator.translate(BHBDTreePopup.Key.CompanyAdd),
 							1);
 					if (inputname.length()>0){
 					BHBusinessDataTreeNode parentbranch = (BHBusinessDataTreeNode) BHBDTreePopup.bhbdTree
 							.getSelectionPath().getLastPathComponent();
 					DTOBranch branch = (DTOBranch) parentbranch.getUserObject();
 
 					DTOCompany newCompanyDTO = new DTOCompany();
 					newCompanyDTO.put(DTOCompany.Key.NAME, new StringValue(
 							inputname));
 					branch.addChild(newCompanyDTO);
 					DefaultTreeModel model = (DefaultTreeModel) BHBDTreePopup.bhbdTree
 							.getModel();
 					model.insertNodeInto(new BHBusinessDataTreeNode(
 							newCompanyDTO), parentbranch, parentbranch
 							.getChildCount());
 					}
 				}
 			});
 			this.add(compadd);
 
 		} 
 		else if (type == Type.COMPANY) {
 
 			JMenuItem compadd = new JMenuItem(
 					translator.translate(BHBDTreePopup.Key.CompanyAdd));
 
 			compadd.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent ae) {
 
 					// NEW NODE
 					String inputname = JOptionPane.showInputDialog(
 							null,
 							translator
 									.translate(BHBDTreePopup.Key.CompanyAddDesc),
 							translator.translate(BHBDTreePopup.Key.CompanyAdd),
 							1);
 					if (inputname.length()>0){
 					BHBusinessDataTreeNode bhdfwzgz = (BHBusinessDataTreeNode) BHBDTreePopup.bhbdTree
 							.getSelectionPath().getLastPathComponent();
 					BHBusinessDataTreeNode parentbranch = (BHBusinessDataTreeNode) bhdfwzgz
 							.getParent();
 					DTOBranch branch = (DTOBranch) parentbranch.getUserObject();
 
 					DTOCompany newCompanyDTO = new DTOCompany();
 					newCompanyDTO.put(DTOCompany.Key.NAME, new StringValue(
 							inputname));
 					branch.addChild(newCompanyDTO);
 					DefaultTreeModel model = (DefaultTreeModel) BHBDTreePopup.bhbdTree
 							.getModel();
 					model.insertNodeInto(new BHBusinessDataTreeNode(
 							newCompanyDTO), parentbranch, parentbranch
 							.getChildCount());
 
 				}}
 			});
 
 			JMenuItem comprem = new JMenuItem(
 					translator.translate(BHBDTreePopup.Key.CompanyRemove));
 
 			comprem.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent ae) {
 
 					// REM NODE
 					BHBusinessDataTreeNode bhdfwzgz = (BHBusinessDataTreeNode) BHBDTreePopup.bhbdTree
 							.getSelectionPath().getLastPathComponent();
 					BHBusinessDataTreeNode parentbranch = (BHBusinessDataTreeNode) bhdfwzgz
 							.getParent();
 					DTOBranch branch = (DTOBranch) parentbranch.getUserObject();
 					branch.removeChild((DTOCompany) bhdfwzgz.getUserObject());
 					DefaultTreeModel model = (DefaultTreeModel) BHBDTreePopup.bhbdTree
 							.getModel();
 					model.removeNodeFromParent(bhdfwzgz);
 
 				}
 			});
 
 			this.add(compadd);
 			this.add(comprem);
 			this.addSeparator();
 
 			JMenuItem peradd = new JMenuItem(
 					translator.translate(BHBDTreePopup.Key.PeriodAdd));
 			peradd.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent ae) {
 					// peradd.add)
 					DTOPeriod p = new DTOPeriod();
 					String inputyear = JOptionPane.showInputDialog(null,
 							translator.translate(BHBDTreePopup.Key.YearInsert),
 							translator.translate(BHBDTreePopup.Key.YearInsert),
 							1);
 					try {
 						Integer.parseInt(inputyear);
 
 						p.put(DTOPeriod.Key.NAME, new StringValue(inputyear));
 						p.addChild(new DTOGCCBalanceSheet());
 						BHBusinessDataTreeNode parentcomp = (BHBusinessDataTreeNode) BHBDTreePopup.bhbdTree
 								.getSelectionPath().getLastPathComponent();
 						DTOCompany mybd = (DTOCompany) parentcomp
 								.getUserObject();
 						if (mybd.getChildrenSize() == 0) {
 							String s = (String) JOptionPane.showInputDialog(
 									null,
 									translator
 											.translate(BHBDTreePopup.Key.TypeChoose),
 									translator
 											.translate(BHBDTreePopup.Key.TypeChooseTop),
 									JOptionPane.PLAIN_MESSAGE,
 									null,
 									new String[] {
 											translator
 													.translate(BHBDTreePopup.Key.CostOfSales),
 											translator
 													.translate(BHBDTreePopup.Key.TotalCosts) },
 									"");
 
 							if (s.equals(translator
 									.translate(BHBDTreePopup.Key.CostOfSales))) {
 								p.addChild(new DTOGCCProfitLossStatementCostOfSales());
 							} else {
 								p.addChild(new DTOGCCProfitLossStatementTotalCost());
 
 							}
 
 							mybd.addChild(p);
 
 							DefaultTreeModel model = (DefaultTreeModel) BHBDTreePopup.bhbdTree
 									.getModel();
 							model.insertNodeInto(new BHBusinessDataTreeNode(p),
 									parentcomp, parentcomp.getChildCount());
 
 						} else {
 
 							// WAS IST ES???
 							IPeriodicalValuesDTO myProfitStatement = mybd
 									.getChild(0).getChild(1);
 
 							if (myProfitStatement.getUniqueId() == DTOGCCProfitLossStatementTotalCost
 									.getUniqueIdStatic()) {
 
 								p.addChild(new DTOGCCProfitLossStatementTotalCost());
 
 							}
 
 							// CostOfSales Method
 							if (myProfitStatement.getUniqueId() == DTOGCCProfitLossStatementCostOfSales
 									.getUniqueIdStatic()) {
 
 								p.addChild(new DTOGCCProfitLossStatementCostOfSales());
 
 							}
 
 							DTOCompany parentcom = mybd;
 							parentcom.addChild(p);
 
 							DefaultTreeModel model = (DefaultTreeModel) BHBDTreePopup.bhbdTree
 									.getModel();
 							model.insertNodeInto(new BHBusinessDataTreeNode(p),
 									parentcomp, parentcomp.getChildCount());
 
 						}
 		
 
 						// If you're here, the return value was null/empty.
 
 					} catch (Exception e) {
 
 						JOptionPane.showMessageDialog(null, translator
 								.translate(BHBDTreePopup.Key.YearError));
 					}
 				}
 			});
 			this.add(peradd);
 
 		} else if (type == Type.PERIOD) {
 
 			JMenuItem padd = new JMenuItem(
 					translator.translate(BHBDTreePopup.Key.PeriodAdd));
 
 			padd.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent ae) {
 
 					BHBusinessDataTreeNode currPeriod = (BHBusinessDataTreeNode) BHBDTreePopup.bhbdTree
 							.getSelectionPath().getLastPathComponent();
 					// get the ProfitLossStatements
 					IPeriodicalValuesDTO myProfitStatement = ((DTOPeriod) (currPeriod
 							.getUserObject())).getChild(1);
 					// TotalCost Method
 					DTOPeriod p = new DTOPeriod();
 					String inputname = JOptionPane.showInputDialog(null,
 							translator.translate(BHBDTreePopup.Key.PeriodAdd),
 							translator.translate(BHBDTreePopup.Key.PeriodAdd),
 							1);
 
 					try {
 						Integer.parseInt(inputname);
 
 						p.put(DTOPeriod.Key.NAME, new StringValue(inputname));
 
 						p.addChild(new DTOGCCBalanceSheet());
 
 						if (myProfitStatement.getUniqueId() == DTOGCCProfitLossStatementTotalCost
 								.getUniqueIdStatic()) {
 
 							p.addChild(new DTOGCCProfitLossStatementTotalCost());
 
 						}
 
 						// CostOfSales Method
 						if (myProfitStatement.getUniqueId() == DTOGCCProfitLossStatementCostOfSales
 								.getUniqueIdStatic()) {
 
 							p.addChild(new DTOGCCProfitLossStatementCostOfSales());
 
 						}
 
 						DTOCompany parentcom = (DTOCompany) ((BHBusinessDataTreeNode) currPeriod
 								.getParent()).getUserObject();
 						parentcom.addChild(p);
 
 						DefaultTreeModel model = (DefaultTreeModel) BHBDTreePopup.bhbdTree
 								.getModel();
 						model.insertNodeInto(new BHBusinessDataTreeNode(p),
 								((BHBusinessDataTreeNode) currPeriod
 										.getParent()),
 								((BHBusinessDataTreeNode) currPeriod
 										.getParent()).getChildCount());
 					} catch (Exception e) {
 						JOptionPane.showMessageDialog(null, translator
 								.translate(BHBDTreePopup.Key.YearError));
 					}
 
 				
 
 				}
 			});
 
 			this.add(padd);
 
 			JMenuItem prem = new JMenuItem(
 					translator.translate(BHBDTreePopup.Key.PeriodRemove));
 
 			prem.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent ae) {
 
 					// REMOVE NODE
					BHBusinessDataTreeNode bhdf = (BHBusinessDataTreeNode) BHBDTreePopup.bhbdTree
 							.getSelectionPath().getLastPathComponent();
					BHBusinessDataTreeNode parentbranch = (BHBusinessDataTreeNode) bhdf
 							.getParent();
 					DTOCompany mybd = (DTOCompany) parentbranch.getUserObject();
					mybd.removeChild((DTOPeriod) bhdf.getUserObject());
 					DefaultTreeModel model = (DefaultTreeModel) BHBDTreePopup.bhbdTree
 							.getModel();
					model.removeNodeFromParent(bhdf);
 
 				}
 			});
 
 			this.add(prem);
 		}
 	}
 
 }
