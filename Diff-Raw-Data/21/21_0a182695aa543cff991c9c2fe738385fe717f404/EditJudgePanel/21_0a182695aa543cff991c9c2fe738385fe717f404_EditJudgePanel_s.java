 package gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.ComponentOrientation;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Font;
 
 import javax.swing.Action;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.DefaultCellEditor;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.DefaultListModel;
 import javax.swing.ImageIcon;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.UIDefaults;
 import javax.swing.UIManager;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.TableModelEvent;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.JTableHeader;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableColumnModel;
 import javax.swing.JTable;
 import javax.swing.JButton;
 
 import code.Constants;
 import code.DataFiles;
 import code.FileUtils;
 import code.StringUtils;
 
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.io.ObjectInputStream.GetField;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Locale;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.Vector;
 import java.awt.FlowLayout;
 import java.awt.CardLayout;
 
 public class EditJudgePanel{
 
 	final static String ANCIENTPANEL = "";
     final static String MODERNPANEL = "";
     
     static Vector<String> comboBoxItems1= new Vector<String>();
     static Vector<String> comboBoxItems2= new Vector<String>();
     static HashMap<Integer, String> groupsMap;
     
 	final static DefaultComboBoxModel ancientComboBoxModel = new DefaultComboBoxModel(comboBoxItems1);
 	final static DefaultComboBoxModel modernComboBoxModel = new DefaultComboBoxModel(comboBoxItems2);
 	
 	final static DefaultTableModel ancientExpDm = new DefaultTableModel();
 	final static DefaultTableModel modernExpDm = new DefaultTableModel();
 	
 	private static HashMap<Integer,Integer> modernGroupsConvertMap;
 	private static HashMap<Integer,Integer> ancientGroupsConvertMap;
 	private static int maxGroupId;
 	private static Set<Integer> ancientPrevRows;
 	private static Set<Integer> modernPrevRows;
 	private static HashMap<String,String> desc2groupMap;
 	
 	private static HashMap<Integer,LinkedList<String>> m_ancientExpanMap;
 	private static HashMap<Integer,LinkedList<String>> m_modernExpanMap;
 	
 	private static JTable ancientTable; 
 	private static JTable modernTable;
 	
 	private static JTable ancientExpTable; 
 	private static JTable modernExpTable;
 	
 	private static JPanel m_panel;
 
 	/**
 	 * Create grid view for manual judgment
 	 * @param targetTermId
 	 * @return
 	 * @throws IOException 
 	 */
 	public static JPanel createJudgementView(String targetTermId, String targetTerm) throws IOException {
 
 		m_panel = new JPanel(new BorderLayout());
 		
 		JPanel captionPanel = new JPanel();
 		
 		final JLabel Text = new JLabel(targetTerm+"               ",JLabel.CENTER);
 //		m_panel.add(Text,  BorderLayout.NORTH);
 		captionPanel.add(Text,  BorderLayout.EAST);
 		captionPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
 		
 		final JButton AddBtn = new JButton("  ");
 		AddBtn.setName(targetTermId);
 		AddBtn.setPreferredSize(new Dimension(200, 20));
 		AddBtn.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				AddRelatedTermGui addGui = new AddRelatedTermGui();
 				try {
 					int generation = 0;
 					if(modernTable.getRowCount()>0)
 						generation = Integer.parseInt(modernTable.getValueAt(0, 8).toString());
 					else if (ancientTable.getRowCount()>0)
 						generation = Integer.parseInt(ancientTable.getValueAt(0, 8).toString());
 					
 					int counter = addGui.getRelatedTerm(Integer.parseInt(AddBtn.getName()),maxGroupId,generation);
 					maxGroupId += counter;
 					m_panel.removeAll();
 					m_panel.add(new JPanel());
 					FrmChange.getFrames()[0].validate();
 				} catch (NumberFormatException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				} catch (IOException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 			}
 
 			});
 
 //		AddBtn.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
 		captionPanel.add(AddBtn,  BorderLayout.WEST);
 		
 		m_panel.add(captionPanel,  BorderLayout.NORTH);
 		
 	    
 		//Two tabs for modern and ancient annotation
 		JTabbedPane tabbedPane = new JTabbedPane();
 		tabbedPane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
 
 		JPanel ancientPanel = new JPanel(new BorderLayout()){
             //Make the panel wider than it really needs, so
             //the window's wide enough for the tabs to stay
             //in one row.
             public Dimension getPreferredSize() {
                 Dimension size = super.getPreferredSize();
                 size.width += 100;
                 return size;
             }
         };
         
         JPanel modernPanel = new JPanel(new BorderLayout()){
             //Make the panel wider than it really needs, so
             //the window's wide enough for the tabs to stay
             //in one row.
             public Dimension getPreferredSize() {
                 Dimension size = super.getPreferredSize();
                 size.width += 100;
                 return size;
             }
         };
 
 
 	    final JButton SaveBtn = new JButton(" ");
 	    SaveBtn.setName(targetTermId);
 		SaveBtn.addActionListener(new ActionListener(){
 		public void actionPerformed(ActionEvent e) {
 			
 //				SubmitJugementGui submit = new SubmitJugementGui();
 //				Vector<Integer> infoVec = submit.getJudgementInfo();
 //				if (infoVec!=null){
 					try {
 						int ancientRowNum = ancientTable.getRowCount();
 						int modernRowNum = modernTable.getRowCount();
 						int ancientValid = validateTableData(ancientRowNum,true);
 						if (ancientValid != -1){
 							JOptionPane.showMessageDialog(null, "    "+ancientValid, "  ",JOptionPane.ERROR_MESSAGE);
 							return;
 						}
 						int modernValid = validateTableData(modernRowNum,false);
 						if (modernValid != -1){
 							JOptionPane.showMessageDialog(null, "    "+modernValid, "  ",JOptionPane.ERROR_MESSAGE);
 							return;
 						}
 						
 						ancientValid = validateTableData2(ancientRowNum,true);
 						if (ancientValid != -1){
 							JOptionPane.showMessageDialog(null, "   "+ancientValid, "  ",JOptionPane.ERROR_MESSAGE);
 							return;
 						}
 						modernValid = validateTableData2(modernRowNum,false);
 						if (modernValid != -1){
 							JOptionPane.showMessageDialog(null, "   "+modernValid, "  ",JOptionPane.ERROR_MESSAGE);
 							return;
 						}
 						
 						File annoDir = new File(Constants.workingDir+Constants.annotatedDir);
 //						if (!annoDir.exists())
 //							annoDir.mkdir();
 						// write annotation file to a new folder
 						String annoFile = Constants.workingDir+Constants.annotatedDir+"/"+SaveBtn.getName()+Constants.judgmentFileSufix;
 						BufferedWriter writer = new BufferedWriter(new FileWriter(annoFile+"_tmp"));
 						writer.write("\n");
 						int groupNum = (maxGroupId>=0?maxGroupId:-1);
 						for(int i=0; i<ancientRowNum; i++){
 							writer.write(ancientTable.getValueAt(i, 1).toString()+"\t");
 							writer.write(ancientTable.getValueAt(i, 6).toString()+"\t");
 							writer.write("1\t");
 							writer.write(((Boolean) ancientTable.getValueAt(i, 2)?1:0) +"\t");
 							int ancientComboIndex = ancientComboBoxModel.getIndexOf(ancientTable.getValueAt(i, 4).toString());
 							if (ancientComboIndex == -1 || ancientComboIndex == 0)
 								writer.write("-1\t");
 							else {
 								if (ancientGroupsConvertMap.containsKey(ancientComboIndex))
 									writer.write(ancientGroupsConvertMap.get(ancientComboIndex)+"\t");
 								else {
 									groupNum++;
 									writer.write(groupNum+"\t");
 									maxGroupId++;
 								}
 							}
 							writer.write(ancientTable.getValueAt(i, 7).toString()+"\t");
 							writer.write(ancientTable.getValueAt(i, 8).toString()+"\t");
 							writer.write(ancientTable.getValueAt(i, 9).toString()+"\n");
 						}
 						
 						for(int i=0; i<modernRowNum; i++){
 							writer.write(modernTable.getValueAt(i, 1).toString()+"\t");
 							writer.write(modernTable.getValueAt(i, 6).toString()+"\t");
 							writer.write("0\t");
 							writer.write(((Boolean) modernTable.getValueAt(i, 2)?1:0) +"\t");
 							int modernComboIndex = modernComboBoxModel.getIndexOf(modernTable.getValueAt(i, 4).toString());
 							if (modernComboIndex == -1 || modernComboIndex == 0)
 								writer.write("-1\t");
 							else {
 								if (modernGroupsConvertMap.containsKey(modernComboIndex))
 									writer.write(modernGroupsConvertMap.get(modernComboIndex)+"\t");
 								else {
 									groupNum++;
 									writer.write(groupNum+"\t");
 									maxGroupId++;
 								}
 							}
 							writer.write(modernTable.getValueAt(i, 7).toString()+"\t");
 							writer.write(modernTable.getValueAt(i, 8).toString()+"\t");
 							writer.write(modernTable.getValueAt(i, 9).toString()+"\n");
 						}
 						writer.close();
 						// remove judgment file
 						File annotatedFile = new File(annoFile);
 						annotatedFile.delete();
 						File newAnnotatedFile = new File(annoFile+"_tmp");
 						newAnnotatedFile.renameTo(new File(annoFile));
 //						File groupsFile = new File(Constants.workingDir+Constants.judgmentsDir+"/"+SaveBtn.getName()+Constants.groupsFileSufix);
 //						if (groupsFile.exists())
 //							groupsFile.delete();
 						// write the expansion file
 						writer = new BufferedWriter(new FileWriter(Constants.workingDir+Constants.inputDir+"/"+Constants.expFileName));
 						SortedSet<Integer> keys = new TreeSet<Integer>(m_ancientExpanMap.keySet());
 						keys.addAll(m_modernExpanMap.keySet());
 					    Iterator<Integer> iterator = keys.iterator();
 					    boolean bFound = false;
 					    while (iterator.hasNext()){
 					    	int id = iterator.next();
 					    	System.out.println(id);
 					    	if(id == Integer.parseInt(SaveBtn.getName())) {
 					    		bFound = true;
 					    		if (ancientExpTable.getRowCount()>0 || modernExpTable.getRowCount() > 0) {
 									int rowNum = ancientExpTable.getRowCount(); 
 									for(int i=0; i<rowNum; i++){
 										String targetTerm = SaveBtn.getName();
 										writer.write(targetTerm + "\t");
 										writer.write(StringUtils.cleanString(ancientExpTable.getValueAt(i, 0).toString())+"\t");
 										writer.write(ancientExpTable.getValueAt(i, 1).toString()+"\t");
 										writer.write("1\t");
 										writer.write(ancientExpTable.getValueAt(i, 2).toString()+"\n");
 									}
 									rowNum = modernExpTable.getRowCount();
 									for(int i=0; i<rowNum; i++){
 										String targetTerm = SaveBtn.getName();
 										writer.write(targetTerm + "\t");
 										writer.write(StringUtils.cleanString(modernExpTable.getValueAt(i, 0).toString())+"\t");
 										writer.write(modernExpTable.getValueAt(i, 1).toString()+"\t");
 										writer.write("0\t");
 										writer.write(modernExpTable.getValueAt(i, 2).toString()+"\n");
 									}
 					    		}
 					    	}
 					    	else {
 					    		if(m_ancientExpanMap.containsKey(id)){
 					    			for(String exp:m_ancientExpanMap.get(id)){
 						    			writer.write(id + "\t");
 										writer.write(exp.split("\t")[0]+"\t");
 										writer.write(exp.split("\t")[1]+"\t");
 										writer.write("1\t");
 										writer.write(exp.split("\t")[3]+"\n");
 					    			}
 					    		}
 					    		if(m_modernExpanMap.containsKey(id)){
 					    			for(String exp:m_modernExpanMap.get(id)){
 						    			writer.write(id + "\t");
 						    			writer.write(exp.split("\t")[0]+"\t");
 										writer.write(exp.split("\t")[1]+"\t");
 										writer.write("0\t");
 										writer.write(exp.split("\t")[3]+"\n");
 					    			}
 					    		}
 					    	}
 					    }
 					    if (!bFound) {
 						    int rowNum = ancientExpTable.getRowCount(); 
 							for(int i=0; i<rowNum; i++){
 								String targetTerm = SaveBtn.getName();
 								writer.write(targetTerm + "\t");
 								writer.write(StringUtils.cleanString(ancientExpTable.getValueAt(i, 0).toString())+"\t");
 								writer.write(ancientExpTable.getValueAt(i, 1).toString()+"\t");
 								writer.write("1\t");
 								writer.write(ancientExpTable.getValueAt(i, 2).toString()+"\n");
 							}
 							rowNum = modernExpTable.getRowCount();
 							for(int i=0; i<rowNum; i++){
 								String targetTerm = SaveBtn.getName();
 								writer.write(targetTerm + "\t");
 								writer.write(StringUtils.cleanString(modernExpTable.getValueAt(i, 0).toString())+"\t");
 								writer.write(modernExpTable.getValueAt(i, 1).toString()+"\t");
 								writer.write("0\t");
 								writer.write(modernExpTable.getValueAt(i, 2).toString()+"\n");
 							}
 					    }
 						writer.close();
 					}
 					catch(IOException e2) {
 					System.out.println("Problem creating table file: " + e2);
 					}
 					SaveBtn.setEnabled(false);
 					m_panel.removeAll();
 					m_panel.add(new JPanel());
 					FrmChange.getFrames()[0].validate();
 				}
 //				}
 		});
 		m_panel.add(SaveBtn,  BorderLayout.SOUTH);
 		
 		loadTableData(targetTermId);
 		loadExpTable(targetTermId);
 	    
 	    
 	    DrawJudgementPanel(modernPanel,false);
 	    DrawJudgementPanel(ancientPanel,true);
 
         tabbedPane.addTab(ANCIENTPANEL, ancientPanel);
 //      tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
         tabbedPane.addTab(MODERNPANEL, modernPanel);
 //      tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
         
        m_panel.add(tabbedPane);
        
        return m_panel;
 	}
 
 
 	/**
 	 * Load empty expansion lists
 	 */
 	private static void loadExpTable(String targetTermId){
 		
 		Vector ancientDataVec = new Vector();
 		
 		Vector dummyHeader = new Vector();
 	    dummyHeader.addElement("");
 	    dummyHeader.addElement("");
 	    dummyHeader.addElement("");
 	   
 	    
 	    int targetId = Integer.parseInt(targetTermId);
 	    if (m_ancientExpanMap.containsKey(targetId)){
 	    	for(String exp:m_ancientExpanMap.get(targetId)){
 	    		String []tokens = exp.split("\t");
 	    		Vector vec = new Vector();
 	    		vec.addElement(tokens[0]);
 	    		vec.addElement(tokens[1]);
 	    		vec.addElement(tokens[2]);
 	    		ancientDataVec.add(vec);
 	    	}
 	    }
 	   
 	    ancientExpDm.setDataVector(ancientDataVec,dummyHeader);
 	    ancientExpTable = new JTable(ancientExpDm){
 	    	@Override
 	            public Component prepareRenderer(TableCellRenderer renderer,
 	                    int rowIndex, int vColIndex) {
 	                Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
 	                    c.setBackground(getBackground());
 	                return c;
 	            }};
 	            ancientExpTable.getColumnModel().getColumn(1).setMinWidth(0);  
 	            ancientExpTable.getColumnModel().getColumn(1).setMaxWidth(0);  
 	    		ancientExpTable.getTableHeader().getColumnModel().getColumn(1).setMinWidth(0);  
 	    		ancientExpTable.getTableHeader().getColumnModel().getColumn(1).setMaxWidth(0);
 	    		
 	    		ancientExpTable.getColumnModel().getColumn(2).setMinWidth(0);  
 	            ancientExpTable.getColumnModel().getColumn(2).setMaxWidth(0);  
 	    		ancientExpTable.getTableHeader().getColumnModel().getColumn(2).setMinWidth(0);  
 	    		ancientExpTable.getTableHeader().getColumnModel().getColumn(2).setMaxWidth(0);
 	    		
 	    		ancientExpTable.addMouseListener(new MouseAdapter() {
 	    	        	@Override
 	    	            public void mouseReleased(MouseEvent e) {
 	    	        		JTable table = (JTable)e.getSource();
 	    	                int rowindex = table.getSelectedRow();
 	    	                int colindex = table.getSelectedColumn();
 	    	                if (colindex != 0)
 	    	                    return;
 	    	                if (e.isPopupTrigger() && e.getComponent() instanceof JTable ) {
 	    	                	ShowFullCellText4EditGui s = new ShowFullCellText4EditGui();
 	    	                	s.showWindow(table.getValueAt(rowindex, colindex).toString(),true, rowindex, true);
 	    	                }
 	    	            }
 	    	        });
 	    		
 	    Vector modernDataVec = new Vector();
 		if (m_modernExpanMap.containsKey(targetId)){
 	    	for(String exp:m_modernExpanMap.get(targetId)){
 	    		String []tokens = exp.split("\t");
 	    		Vector vec = new Vector();
 	    		vec.addElement(tokens[0]);
 	    		vec.addElement(tokens[1]);
 	    		vec.addElement(tokens[2]);
 	    		modernDataVec.add(vec);
 	    	}
 	    }
 	   
 		modernExpDm.setDataVector(modernDataVec,dummyHeader);
 	    modernExpTable = new JTable(modernExpDm){
 	    	@Override
             public Component prepareRenderer(TableCellRenderer renderer,
                     int rowIndex, int vColIndex) {
                 Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
                     c.setBackground(getBackground());
                 return c;
             }};
             
             modernExpTable.getColumnModel().getColumn(1).setMinWidth(0);  
             modernExpTable.getColumnModel().getColumn(1).setMaxWidth(0);  
             modernExpTable.getTableHeader().getColumnModel().getColumn(1).setMinWidth(0);  
             modernExpTable.getTableHeader().getColumnModel().getColumn(1).setMaxWidth(0);
     		
             modernExpTable.getColumnModel().getColumn(2).setMinWidth(0);  
             modernExpTable.getColumnModel().getColumn(2).setMaxWidth(0);  
             modernExpTable.getTableHeader().getColumnModel().getColumn(2).setMinWidth(0);  
             modernExpTable.getTableHeader().getColumnModel().getColumn(2).setMaxWidth(0);
             modernExpTable.addMouseListener(new MouseAdapter() {
 	        	@Override
 	            public void mouseReleased(MouseEvent e) {
 	        		JTable table = (JTable)e.getSource();
 	                int rowindex = table.getSelectedRow();
 	                int colindex = table.getSelectedColumn();
 	                if (colindex != 0)
 	                    return;
 	                if (e.isPopupTrigger() && e.getComponent() instanceof JTable ) {
 	                	ShowFullCellText4EditGui s = new ShowFullCellText4EditGui();
 	                	s.showWindow(table.getValueAt(rowindex, colindex).toString(),true, rowindex,false);
 	                }
 	            }
 	        });
 	}
 	
 	private static void loadTableData(String targetTerm) throws IOException{
 		final DataFiles df = new DataFiles(Constants.workingDir+Constants.annotatedDir);
 		// load previous groups to the list box
 		df.loadDataFile2Table4Edit(targetTerm,Constants.judgmentFileSufix);
 		ancientComboBoxModel.removeAllElements();
 		modernComboBoxModel.removeAllElements();
 		desc2groupMap = new HashMap<String, String>();
 		for(String element:df.getM_ancientGroups()){
 //			String desc = NgramViewer.mergeNgrams(StringUtils.convertStringToList(element));
 			ancientComboBoxModel.addElement(element);
 		}
 		for(String element:df.getM_modernGroups()){
 //			String desc = NgramViewer.mergeNgrams(StringUtils.convertStringToList(element));
 			modernComboBoxModel.addElement(element);
 		}
 		
 		modernGroupsConvertMap = df.getM_modernGroupsConvertMap();
 		ancientGroupsConvertMap = df.getM_ancientGroupsConvertMap();
 		maxGroupId = df.getM_maxGroupId();
 		
 		ancientPrevRows = df.getM_ancientPrevJudgments();
 		modernPrevRows = df.getM_modernPrevJudgments();
 		
 		df.loadExpansionsData(Constants.workingDir+Constants.inputDir+"\\"+Constants.expFileName);
 		m_ancientExpanMap = df.getM_ancientExpanMap();
 		m_modernExpanMap = df.getM_modernExpanMap();
 		
 		
 		
 		//load data tables
 		DefaultTableModel ancientDm = new DefaultTableModel(){
 		      public boolean isCellEditable(int rowIndex, int mColIndex) {
 //		          if(df.getM_ancientPrevJudgments().contains(rowIndex)){
 //		        	  return false;
 //		          }
 		          return true;
 		        }
 		};
 		ancientDm.setDataVector(df.getM_vAncientData(),df.getM_vCols());
 	    
 	    ancientTable = new JTable(ancientDm){ 	
 			@Override
         public Component prepareRenderer(TableCellRenderer renderer,
                 int rowIndex, int vColIndex) {
             Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
             if (df.getM_ancientPrevJudgments().contains(rowIndex)) {
                 c.setBackground(new Color(241,241,241));
             } else {
                 c.setBackground(getBackground());
             }
             if (vColIndex == 0)
             	c.setBackground(new Color(204,255,153));
             return c;
         }
 	    };
         
         ancientTable.addMouseListener(new MouseAdapter() {
         	@Override
             public void mouseReleased(MouseEvent e) {
         		JTable table = (JTable)e.getSource();
                 int rowindex = table.getSelectedRow();
                 int colindex = table.getSelectedColumn();
                 if (colindex != 1)
                     return;
                 if (e.isPopupTrigger() && e.getComponent() instanceof JTable ) {
                 	ShowFullCellText4EditGui s = new ShowFullCellText4EditGui();
                 	s.showWindow(table.getValueAt(rowindex, colindex).toString(),false, rowindex,true);
                 }
             }
         });
         
 	    ancientTable.setName(ANCIENTPANEL);
 	    JTableHeader ancientHeader = ancientTable.getTableHeader();
 	    ancientHeader.setBackground(new Color(255,255,153));
 //	    header.setForeground(Color.WHITE);
 	    
 	    DefaultTableModel modernDm = new DefaultTableModel(){
 	    	public boolean isCellEditable(int rowIndex, int mColIndex) {
 //		          if(df.getM_modernPrevJudgments().contains(rowIndex)){
 //		        	  return false;
 //		          }
 		          return true;
 		        }
 		};
 	    modernDm.setDataVector(df.getM_vModernData(),df.getM_vCols());
 	    
 	    modernTable = new JTable(modernDm){ 	
 			@Override
 	        public Component prepareRenderer(TableCellRenderer renderer,
 	                int rowIndex, int vColIndex) {
 	            Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
 	            if (df.getM_modernPrevJudgments().contains(rowIndex)) {
 	                c.setBackground(new Color(241,241,241));
 	            } else {
 	                c.setBackground(getBackground());
 	            }
 	            if (vColIndex == 0)
 	            	c.setBackground(new Color(204,255,153));
 	            return c;
 	        }};
 	        
 	        modernTable.addMouseListener(new MouseAdapter() {
 	        	@Override
 	            public void mouseReleased(MouseEvent e) {
 	        		JTable table = (JTable)e.getSource();
 	                int rowindex = table.getSelectedRow();
 	                int colindex = table.getSelectedColumn();
 	                if (colindex != 1)
 	                    return;
 	                if (e.isPopupTrigger() && e.getComponent() instanceof JTable ) {
 	                	ShowFullCellText4EditGui s = new ShowFullCellText4EditGui();
 	                	s.showWindow(table.getValueAt(rowindex, colindex).toString(),false, rowindex,false);
 	                }
 	            }
 	        });
 	        
 	    modernTable.setName(MODERNPANEL);
 	    JTableHeader modernHeader = modernTable.getTableHeader();
 	    modernHeader.setBackground(new Color(255,255,153));
 	}
 	/*
 	 * Draw the judgment panel with the annotation table
 	 */
 	private static void DrawJudgementPanel(JPanel panel, boolean isAncient){
 		JTable table;
 		JTable expTable;
 		DefaultComboBoxModel model;
 		Set<Integer> prev;
 		
 		if (isAncient) {
 			table = ancientTable;
 			model = ancientComboBoxModel;
 			expTable = ancientExpTable;
 			prev = ancientPrevRows;
 		}
 		else {
 			table = modernTable;
 			model = modernComboBoxModel;
 			expTable = modernExpTable;
 			prev = modernPrevRows;
 		}
 		
 		table.getTableHeader().setFont(new Font("Dialg", Font.PLAIN, 20));
 		table.setRowHeight(table.getRowHeight()+5);
 		table.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
 		
 		table.getColumnModel().getColumn(6).setMinWidth(0);  
 		table.getColumnModel().getColumn(6).setMaxWidth(0);  
 		table.getTableHeader().getColumnModel().getColumn(6).setMinWidth(0);  
 		table.getTableHeader().getColumnModel().getColumn(6).setMaxWidth(0);
 		
 		table.getColumnModel().getColumn(7).setMinWidth(0);  
 		table.getColumnModel().getColumn(7).setMaxWidth(0);  
 		table.getTableHeader().getColumnModel().getColumn(7).setMinWidth(0);  
 		table.getTableHeader().getColumnModel().getColumn(7).setMaxWidth(0);
 		
 		table.getColumnModel().getColumn(8).setMinWidth(0);  
 		table.getColumnModel().getColumn(8).setMaxWidth(0);  
 		table.getTableHeader().getColumnModel().getColumn(8).setMinWidth(0);  
 		table.getTableHeader().getColumnModel().getColumn(8).setMaxWidth(0);
 
 		table.getColumnModel().getColumn(9).setMinWidth(0);  
 		table.getColumnModel().getColumn(9).setMaxWidth(0);  
 		table.getTableHeader().getColumnModel().getColumn(9).setMinWidth(0);  
 		table.getTableHeader().getColumnModel().getColumn(9).setMaxWidth(0);
 		
 		
 //        table.getColumnModel().removeColumn(table.getColumnModel().getColumn(6));    
 //        table.getColumnModel().removeColumn(table.getColumnModel().getColumn(6));
 	
 		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
 		rightRenderer.setHorizontalAlignment( JLabel.RIGHT );
 		table.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
 		table.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
 		
 	    
 	    JComboBox comboBox = new JComboBox(model);
 	    comboBox.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);                         
 //        ((JComponent)comboBox.getRenderer()).setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
 //	    // Create the combo box editor
 //	    JComboBox comboBox = new JComboBox(ComboBoxTableModel.getValidStates());
 	    comboBox.setEditable(false);
 	    comboBox.setMaximumRowCount(30);
 	    comboBox.setFont(new Font("Dialog", Font.PLAIN, 15));
 //	    comboBox.setSelectedItem(null);
 	    DefaultCellEditor editor = new DefaultCellEditor(comboBox);
 	    
 //	    MyCellEditor editor = new MyCellEditor(comboBox, prev);
 //
 //	    // Assign the editor to the second column
 	    TableColumnModel tcm = table.getColumnModel();
 	    tcm.getColumn(4).setCellEditor(editor);
 	    tcm.getColumn(4).setCellRenderer(new NgramsRenderer());
 	    
 //	    tcm.getColumn(4).setMaxWidth(60);
 	    tcm.getColumn(2).setMaxWidth(50);
 	    tcm.getColumn(3).setMaxWidth(75);
 	    tcm.getColumn(5).setMaxWidth(75);
 	    
 //	    
 	    table.getColumn(".").setCellRenderer(new AddEditGroupButtonRenderer());
 	    table.getColumn(".").setCellEditor(
 	            new AddEditGroupButtonEditor(new JCheckBox()));
 	    
 	    table.getColumn(".").setCellRenderer(new AddEditExpansionButtonRenderer());
 	    table.getColumn(".").setCellEditor(
 	            new AddEditExpansionButtonEditor(new JCheckBox()));
 	    
 	    TableColumn tc = tcm.getColumn(2); 
 	    tc.setCellRenderer(table.getDefaultRenderer(Boolean.class)); 
 	    tc.setCellEditor(table.getDefaultEditor(Boolean.class));
 	    
 	    table.getColumnModel().getColumn(1).setCellRenderer(new NgramsRenderer());
 	    
 	    table.getColumnModel().getColumn(0).setMinWidth(30);
 	    table.getColumnModel().getColumn(0).setMaxWidth(30);
 	    table.getColumnModel().getColumn(0).setPreferredWidth(30);
 	    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
 	    centerRenderer.setHorizontalAlignment( JLabel.CENTER );
 	    table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
 	    
 	   
 	    expTable.setRowHeight(table.getRowHeight()+5);
 	    expTable.setShowGrid(false);
 	    expTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 //	    expTable.getColumnModel().getColumn(0).setCellRenderer(rightRenderer);
 	    expTable.getColumnModel().getColumn(0).setCellRenderer(new NgramsRenderer());
 	    expTable.getColumnModel().getColumn(1).setMinWidth(0);  
 	    expTable.getColumnModel().getColumn(1).setMaxWidth(0);
 	    JScrollPane scrollTable = new JScrollPane(expTable);
 	    scrollTable.setColumnHeader(null);
 	    scrollTable.setPreferredSize(new Dimension(200, 0));
 	    Box tableBox = new Box(BoxLayout.Y_AXIS);
 	    JLabel expLabel = new JLabel("                    ");
 	    tableBox.add(expLabel);
 	    tableBox.add(scrollTable);
 	    tableBox.setOpaque(true);
 	    tableBox.setBackground(Color.ORANGE);
 		panel.add(tableBox,  BorderLayout.WEST);
 		
 		JScrollPane pane = new JScrollPane( table );
 		panel.add( pane, BorderLayout.CENTER );
 	}
 
 
 	private static int validateTableData(int rowNum, boolean isAncient){
 		if (isAncient) {
 			for(int i=0; i<rowNum; i++){
 				boolean judge = (Boolean) ancientTable.getValueAt(i, 2);
 				int ancientComboIndex = ancientComboBoxModel.getIndexOf(ancientTable.getValueAt(i, 4).toString());
 				if (judge && (ancientComboIndex == -1 || ancientComboIndex == 0)){
 					return i+1;
 				}
 			}
 		}
 		else {
 			for(int i=0; i<rowNum; i++){
 				boolean judge = (Boolean) modernTable.getValueAt(i, 2);
 				int modernComboIndex = modernComboBoxModel.getIndexOf(modernTable.getValueAt(i, 4).toString());
 				if (judge && (modernComboIndex == -1 || modernComboIndex == 0)){
 					return i+1;
 				}
 			}
 		}
 		return -1;
 			
 	}
 
 
 private static int validateTableData2(int rowNum, boolean isAncient){
 	if (isAncient) {
 		for(int i=0; i<rowNum; i++){
 			boolean judge = (Boolean) ancientTable.getValueAt(i, 2);
 			int ancientComboIndex = ancientComboBoxModel.getIndexOf(ancientTable.getValueAt(i, 4).toString());
 			if (!judge && ancientComboIndex > 0){
 				return i+1;
 			}
 		}
 	}
 	else {
 		for(int i=0; i<rowNum; i++){
 			boolean judge = (Boolean) modernTable.getValueAt(i, 2);
 			int modernComboIndex = modernComboBoxModel.getIndexOf(modernTable.getValueAt(i, 4).toString());
 			if (!judge && modernComboIndex > 0){
 				return i+1;
 			}
 		}
 	}
 	return -1;
 		
 }
 }
