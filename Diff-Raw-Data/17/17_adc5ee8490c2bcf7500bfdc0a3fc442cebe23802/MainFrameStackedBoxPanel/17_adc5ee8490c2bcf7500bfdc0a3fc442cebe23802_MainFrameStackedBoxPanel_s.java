 package edu.wustl.cab2b.client.ui.mainframe;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Iterator;
 import java.util.Vector;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import edu.wustl.cab2b.client.ui.RiverLayout;
import edu.wustl.cab2b.client.ui.WindowUtilities;
 import edu.wustl.cab2b.client.ui.controls.Cab2bHyperlink;
 import edu.wustl.cab2b.client.ui.controls.Cab2bLabel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bPanel;
 import edu.wustl.cab2b.client.ui.controls.StackedBox;
 import edu.wustl.common.util.logger.Logger;
 
 public class MainFrameStackedBoxPanel extends Cab2bPanel {
 
 	private static final long serialVersionUID = 1L;
 
 	StackedBox stackedBox;
 
 	JPanel mySearchQueriesPanel;
 
 	JPanel popularSearchCategoryPanel;
 
 	JPanel myExperimentsPanel;
 
 	MainFrame mainFrame;
 
 	public MainFrameStackedBoxPanel(MainFrame mainFrame) {
 		this.mainFrame = mainFrame;
 		this.setLayout(new BorderLayout());
 		stackedBox = new StackedBox();
 		stackedBox.setTitleBackgroundColor(new Color(200, 200, 220));
 		JScrollPane scrollPane = new JScrollPane(stackedBox);
 		scrollPane.setBorder(null);
 		this.add(scrollPane, BorderLayout.CENTER);
 
 		// the status pane
 		mySearchQueriesPanel = new Cab2bPanel();
 		mySearchQueriesPanel.setLayout(new RiverLayout(10, 5));
 		mySearchQueriesPanel.setPreferredSize(new Dimension(250, 150));
 		mySearchQueriesPanel.setOpaque(false);
 		stackedBox.addBox("My Search Queries", mySearchQueriesPanel, "mysearchqueries_icon.gif",
 				false);
 
 		// the profiling results
 		popularSearchCategoryPanel = new Cab2bPanel();
 		popularSearchCategoryPanel.setLayout(new RiverLayout(10, 5));
 		popularSearchCategoryPanel.setOpaque(false);
 		popularSearchCategoryPanel.setPreferredSize(new Dimension(250, 150));
 		stackedBox.addBox("Popular Categories", popularSearchCategoryPanel,
 				"popularcategories_icon.gif", false);
 
 		// the saved snapshots pane
 		myExperimentsPanel = new Cab2bPanel();
 		myExperimentsPanel.setLayout(new RiverLayout(10, 5));
 		myExperimentsPanel.setPreferredSize(new Dimension(250, 150));
 		myExperimentsPanel.setOpaque(false);
 		stackedBox.addBox("My Experiments", myExperimentsPanel, "arrow_icon_mo.gif", false);
 
 		stackedBox.setPreferredSize(new Dimension(250, 500));
 		stackedBox.setMinimumSize(new Dimension(250, 500));
 	}
 
 	public void setDataForMySearchQueriesPanel(Vector data) {
 		Logger.out.debug("setDataForMySearchQueriesPanel :: data " + data);
 		mySearchQueriesPanel.removeAll();
 		mySearchQueriesPanel.add(new Cab2bLabel());
 		Iterator iter = data.iterator();
 		while (iter.hasNext()) {
 			Object obj = iter.next();
 			String hyperlinkName = obj.toString();
 			Cab2bHyperlink hyperlink = new Cab2bHyperlink();
 			hyperlink.setText(hyperlinkName);
 			mySearchQueriesPanel.add("br", hyperlink);
 		}
 		mySearchQueriesPanel.revalidate();
 		this.validate();
 	}
 
 	public void setDataForPopularSearchCategoriesPanel(Vector data) {
 		Logger.out.debug("setDataForPopularSearchCategoriesPanel :: data " + data);
 		popularSearchCategoryPanel.removeAll();
 		popularSearchCategoryPanel.add(new Cab2bLabel());
 		Logger.out.debug("data " + data);
 		Iterator iter = data.iterator();
 		while (iter.hasNext()) {
 			Object obj = iter.next();
 			String hyperlinkName = obj.toString();
 			Cab2bHyperlink hyperlink = new Cab2bHyperlink();
 			hyperlink.setText(hyperlinkName);
 			popularSearchCategoryPanel.add("br", hyperlink);
 		}
 		popularSearchCategoryPanel.revalidate();
 	}
 
 	public void setDataForMyExperimentsPanel(Vector data) {
 
 		Logger.out.debug("setDataForMyExperimentsPanel :: data " + data);
 		myExperimentsPanel.removeAll();
 		myExperimentsPanel.add(new Cab2bLabel());
 		Iterator iter = data.iterator();
 		while (iter.hasNext()) {
 			Object obj = iter.next();
 			String hyperlinkName = obj.toString();
 			Cab2bHyperlink hyperlink = new Cab2bHyperlink();
 			hyperlink.setBounds(new Rectangle(5, 5, 5, 5));
 			hyperlink.setText(hyperlinkName);
 			hyperlink.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {/*
 															 * Logger.out.debug("Clicked
 															 * on expt link");
 															 * 
 															 * mainFrame.setOpenExperimentWelcomePanel();
 															 * mainFrame.globalNavigationPanel.tabButtons[0].setBackground(mainFrame.globalNavigationPanel.navigationButtonBgColorUnSelected);
 															 * mainFrame.globalNavigationPanel.tabButtons[2].setBackground(mainFrame.globalNavigationPanel.navigationButtonBgColorSelected);
 															 * updateUI();
 															 */
 				}
 			});
 			myExperimentsPanel.add("br", hyperlink);
 		}
 		myExperimentsPanel.revalidate();
 	}
 
 	static JLabel makeBold(JLabel label) {
 		label.setFont(label.getFont().deriveFont(Font.BOLD));
 		return label;
 	}

	public static void main(String[] args) {
		Logger.configure("");
		MainFrameStackedBoxPanel myStackedBox = new MainFrameStackedBoxPanel(null);
		WindowUtilities.showInFrame(myStackedBox, "MyStacked Box");
		Vector data = new Vector();
		data.add("SNP Experiment");
		myStackedBox.setDataForPopularSearchCategoriesPanel(data);
	}
}
