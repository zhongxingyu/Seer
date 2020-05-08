 package domainviewer;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 
 import databaselogger.DBLogger;
 import domain.Controller;
 import domain.LiteratureGroupManager;
 import domain.LiteratureGrouping;
 import domain.LiteratureProduct;
 import domain.LiteratureProductManager;
 import domain.LiteratureReview;
 import domain.LiteratureReviewManager;
 import domain.Review;
 import domain.SystemChanged;
 
 public class MainPanel extends JPanel implements SystemChanged{
 	private JPanel groupPanel1 = new JPanel();
 	private JTextField selGroupNameField;
 	private JList litRevList;
 	
 	private JPanel groupPanel2 = new JPanel();
 	
 	private JPanel litRevPanel1 = new JPanel();
 	
 	private JPanel litRevPanel2 = new JPanel();
 	private JTextField selLitRevName;
 	private JTextField selProdName;
 	private JList reviewList;
 	
 	private JTextArea reviewText;
 	private JTextField reviewName;
 	
 	private JPanel prodPanel1 = new JPanel();
 	private JButton linkProducts;
 	private JButton unlinkProducts;
 	
 	private JPanel prodPanel2 = new JPanel();
 	private JTextField selProdField;
 	private JTextField prodTitle;
 	private JTextField prodYear;
 	private JTextArea prodReference;
 	private JTextField prodLocation;
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 6025535697164174250L;
 	
 	public MainPanel(){
 		super();
 		setLayout(new GridLayout(2,3));
 		setJPanelGroup1(groupPanel1);
 		add(groupPanel1);
 		setJPanelLitRev1(litRevPanel1);
 		add(litRevPanel1);
 		setJPanelProd1(prodPanel1);
 		add(prodPanel1);
 		add(groupPanel2);
 		setJPanelLitRev2(litRevPanel2);
 		add(litRevPanel2);
 		setJPanelProd2(prodPanel2);
 		add(prodPanel2);
 		Controller.getInstance().addStateListener(this);
 		this.stateChanged(0);
 	}
 	
 	public JScrollPane prepareJlist(JList list){
 		//Show List Model
 		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
 		list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
 		list.setVisibleRowCount(-1);
 		
 		JScrollPane listScroller = new JScrollPane(list);
 		listScroller.setPreferredSize(new Dimension(180, 35));
 		return listScroller;
 	}
 	
 	public void setJPanelProd2(JPanel panel){
 		panel.setLayout(new BorderLayout());
 		JPanel topPanel = new JPanel();topPanel.setLayout(new GridLayout(3,1));
 		
 		JPanel selProdPanel = new JPanel();selProdPanel.setLayout(new GridLayout(1,2));
 		JLabel selProduct = new JLabel("Selcted Product:");
 		selProdField = new JTextField(10);
 		selProdPanel.add(selProduct);selProdPanel.add(selProdField);
 		
 		JPanel titlePanel = new JPanel();titlePanel.setLayout(new GridLayout(1,2));
 		JLabel titleLabel = new JLabel("Title");
 		prodTitle = new JTextField(10);
 		titlePanel.add(titleLabel);titlePanel.add(prodTitle);
 		
 		JPanel yearPanel = new JPanel();yearPanel.setLayout(new GridLayout(1,2));
 		JLabel yearLabel = new JLabel("Year");
 		prodYear = new JTextField(10);
 		yearPanel.add(yearLabel);yearPanel.add(prodYear);
 		
 		topPanel.add(selProdPanel);topPanel.add(titlePanel);topPanel.add(yearPanel);
 		
 		prodReference = new JTextArea(4,4);
 		JScrollPane prodRefPane = new JScrollPane(prodReference);
 		
 		prodLocation=new JTextField(10);
 		
 		JButton save = new JButton("Save Products");
 		ActionCreationFactory.getInstance().createSaveProduct(save,prodTitle,prodYear,prodReference,prodLocation,selProdField);
 		panel.add(topPanel,BorderLayout.NORTH);
 		panel.add(prodRefPane,BorderLayout.CENTER);
 		
 		JPanel southPanel = new JPanel();southPanel.setLayout(new GridLayout(1,3));
 		
 		southPanel.add(new JLabel("File Location"));southPanel.add(prodLocation);southPanel.add(save);
 		panel.add(southPanel,BorderLayout.SOUTH);
 	}
 	
 	public void setJPanelLitRev2(JPanel panel){
 		panel.setLayout(new GridLayout(2,1));
 		JPanel top = new JPanel();
 		JPanel bottom = new JPanel();
 		JButton remove = new JButton("Delete");
 		ActionCreationFactory.getInstance().createDeleteAction(remove,domain.System.Rev);
 		JLabel selLitRev = new JLabel("Sel Lit Rev: ");
 		selLitRevName = new JTextField(10);
 		JLabel selProd = new JLabel("Sel Product: ");
 		selProdName = new JTextField(10);
 		reviewList = new JList();
 		JScrollPane reviewListPane = prepareJlist(reviewList);
 		ActionCreationFactory.getInstance().createJListListeners(reviewList, domain.System.Rev);
 		top.setLayout(new BorderLayout());
 		JPanel selPanel = new JPanel();selPanel.setLayout(new GridLayout(2,2));
 		selPanel.add(selLitRev);selPanel.add(selLitRevName);
 		selPanel.add(selProd);selPanel.add(selProdName);
 		top.add(selPanel,BorderLayout.NORTH);
 		top.add(remove,BorderLayout.WEST);
 		top.add(reviewListPane,BorderLayout.CENTER);
 		
 		bottom.setLayout(new BorderLayout());
 		JLabel reviewNameLabel = new JLabel("Selected Review:");
 		reviewName = new JTextField(10);
 		JPanel infoPanel = new JPanel();infoPanel.setLayout(new GridLayout(1,2));
 		infoPanel.add(reviewNameLabel);infoPanel.add(reviewName);
 		reviewText = new JTextArea(16,30);
 		JScrollPane revPane = new JScrollPane(reviewText);
 		JButton save = new JButton("Save Review");
 		ActionCreationFactory.getInstance().createSaveReview(save,reviewText,reviewList,reviewName,selLitRevName);
 		bottom.add(infoPanel,BorderLayout.NORTH);
 		//bottom.add(revPane,BorderLayout.CENTER);
 		groupPanel2.add(revPane);
 		bottom.add(save,BorderLayout.SOUTH);
 		
 		panel.add(top);panel.add(bottom);
 	}
 	
 	public void setJPanelProd1(JPanel panel){
 		panel.setLayout(new GridLayout(2,1));
 		JPanel blank = new JPanel();
 		JPanel listList = new JPanel();
 		JButton setProductInLitRev = new JButton("Set Product");
 		JButton deleteProduct = new JButton("Delete Product");
 		linkProducts = new JButton("Link Products");
 		unlinkProducts = new JButton("UnLink Products");
 		
 		ActionCreationFactory.getInstance().createDeleteAction(deleteProduct, domain.System.LitProd);
 		ActionCreationFactory.getInstance().createSetSelectedListener(setProductInLitRev, domain.System.Group);
 		ActionCreationFactory.getInstance().createLinkAction(linkProducts);
 		ActionCreationFactory.getInstance().createUnLinkAction(unlinkProducts);
 		
 		JPanel buttonPanel = new JPanel();buttonPanel.setLayout(new GridLayout(4,1));
 		buttonPanel.add(setProductInLitRev);buttonPanel.add(deleteProduct);
 		buttonPanel.add(linkProducts);buttonPanel.add(unlinkProducts);
 		JLabel litProdName = new JLabel("Literature Products");
 		JList litProdList = new JList();
 		litProdList.setModel(LiteratureProductManager.getInstance().getListModel());
 		ActionCreationFactory.getInstance().createJListListeners(litProdList, domain.System.LitProd);
		//prepareJlist(litProdList);
 		listList.setLayout(new BorderLayout());
 		listList.add(litProdName,BorderLayout.NORTH);
 		listList.add(buttonPanel,BorderLayout.WEST);
		listList.add(prepareJlist(litProdList),BorderLayout.CENTER);
 		panel.add(blank);panel.add(listList);
 	}
 	
 	public void setJPanelLitRev1(JPanel panel){
 		panel.setLayout(new GridLayout(2,1));
 		JPanel blank = new JPanel();
 		JPanel listList = new JPanel();
 		JButton removeLitRev = new JButton("Remove Lit Rev");
 		JButton deleteLitRev = new JButton("Delete Lit Rev");
 		ActionCreationFactory.getInstance().createRemoveListener(removeLitRev, domain.System.Group);
 		ActionCreationFactory.getInstance().createDeleteAction(deleteLitRev, domain.System.LitRev);
 		JButton addLitRev = new JButton("Add Lit Review");
 		ActionCreationFactory.getInstance().createAddListener(addLitRev, domain.System.Group);
 		JPanel buttonPanel = new JPanel();buttonPanel.setLayout(new GridLayout(3,1));
 		buttonPanel.add(addLitRev);buttonPanel.add(removeLitRev);buttonPanel.add(deleteLitRev);
 		JLabel litRevName = new JLabel("Literature Reviews");
 		JList litRevList = new JList();
 		ActionCreationFactory.getInstance().createJListListeners(litRevList, domain.System.LitRev);
 		litRevList.setModel(LiteratureReviewManager.getInstance().getLitRevModel());
		//prepareJlist(litRevList);
 		listList.setLayout(new BorderLayout());
 		listList.add(litRevName,BorderLayout.NORTH);
 		listList.add(buttonPanel,BorderLayout.WEST);
		listList.add(prepareJlist(litRevList),BorderLayout.CENTER);
 		panel.add(blank);panel.add(listList);
 	}
 	
 	public void setJPanelGroup1(JPanel panel){
 		panel.setLayout(new GridLayout(2,1));
 		JPanel top = new JPanel();
 		JButton groupDeleteButton = new JButton("Delete Group");
 		ActionCreationFactory.getInstance().createDeleteAction(groupDeleteButton, domain.System.Group);
 		JLabel groupLabel = new JLabel("Groups");
 		JList groupList = new JList();
 		JLabel selGroupName = new JLabel("Selected Group Name ");
 		selGroupNameField = new JTextField(10);
 		JButton groupSaveButton = new JButton("Save Group Name");
 		ActionCreationFactory.getInstance().createGroupSaveAction(groupSaveButton,selGroupNameField);
 		JPanel westButtons = new JPanel();westButtons.setLayout(new GridLayout(2,1));
 		westButtons.add(groupDeleteButton);westButtons.add(groupSaveButton);
 		top.setLayout(new BorderLayout());
 		top.add(groupLabel,BorderLayout.NORTH);
 		top.add(westButtons,BorderLayout.WEST);
 		top.add(this.prepareJlist(groupList),BorderLayout.CENTER);
 		JPanel southPanel = new JPanel();
 		southPanel.add(selGroupName);southPanel.add(selGroupNameField);
 		top.add(southPanel,BorderLayout.SOUTH);
 		groupList.setModel(LiteratureGroupManager.getInstance().getListModel());
 		ActionCreationFactory.getInstance().createJListListeners(groupList, domain.System.Group);
 		
 		JPanel bottom = new JPanel();
 		bottom.setLayout(new BorderLayout());
 		JLabel litRevLabel = new JLabel("Groups Collective Literature Reviews ");
 		litRevList = new JList();
 		ActionCreationFactory.getInstance().createJListListeners(litRevList, domain.System.LitRev);
 		this.prepareJlist(litRevList);
 		bottom.add(litRevLabel,BorderLayout.NORTH);
 		//bottom.add(removeLitRev,BorderLayout.WEST);
 		bottom.add(prepareJlist(litRevList),BorderLayout.CENTER);
 		
 		panel.add(top);panel.add(bottom);
 	}
 
 	@Override
 	public void selectChanged(Object selected) {
 		if(selected!=null){
 			if(selected instanceof LiteratureGrouping){
 				DBLogger.getInstance().print("MainPanel", "New Select "+((LiteratureGrouping)selected).getName());
 				this.selGroupNameField.setText(""+((LiteratureGrouping)selected).getName());
 				litRevList.setModel(((LiteratureGrouping)selected).getLitrevgrouping() );
 			}else
 			if(selected instanceof LiteratureReview){
 				DBLogger.getInstance().print("MainPanel", "New Select "+((LiteratureReview)selected));
 				this.selLitRevName.setText(""+((LiteratureReview)selected).getName());
 				this.selProdName.setText(""+((LiteratureReview)selected).getLitProduct().getName());
 				reviewList.setModel(((LiteratureReview)selected).getListModel() );
 			}else
 			if(selected instanceof Review){
 				DBLogger.getInstance().print("MainPanel", "New Select "+((Review)selected).getName());
 				this.reviewName.setText(""+((Review)selected).getName());
 				this.reviewText.setText(""+((Review)selected).getText());
 			}else
 			if(selected instanceof LiteratureProduct){
 				DBLogger.getInstance().print("MainPanel", "New Select "+((LiteratureProduct)selected).getName());
 				this.selProdField.setText(""+((LiteratureProduct)selected).getName());
 				this.prodReference.setText(""+((LiteratureProduct)selected).getName());
 				this.prodReference.setText(((LiteratureProduct)selected).getProductRef());
 				this.prodTitle.setText(((LiteratureProduct)selected).getProductTitle());
 				this.prodYear.setText(((LiteratureProduct)selected).getProductYear());
 				this.prodLocation.setText(((LiteratureProduct)selected).getFileLocation());
 			}
 		}
 	}
     
 	@Override
 	public void stateChanged(int state) {
 		if(state==8){
 			linkProducts.setEnabled(true);unlinkProducts.setEnabled(true);
 		}else{
 			linkProducts.setEnabled(false);unlinkProducts.setEnabled(false);
 		}
 	}
 }
