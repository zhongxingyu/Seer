 package edu.virginia.cs4240.webmetrics.gui;
 
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.ComboBoxModel;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.GroupLayout;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.LayoutStyle;
 import javax.swing.WindowConstants;
 
 import edu.virginia.cs4240.webmetrics.Main;
 import edu.virginia.cs4240.webmetrics.modules.AllModules;
 import edu.virginia.cs4240.webmetrics.modules.DisplayOption;
 import edu.virginia.cs4240.webmetrics.modules.ElementModule;
 import edu.virginia.cs4240.webmetrics.modules.PageModule;
 
 
 /**
 * This code was edited or generated using CloudGarden's Jigloo
 * SWT/Swing GUI Builder, which is free for non-commercial
 * use. If Jigloo is being used commercially (ie, by a corporation,
 * company or business for any purpose whatever) then you
 * should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details.
 * Use of Jigloo implies acceptance of these licensing terms.
 * A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
 * THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
 * LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
 public class GUIFrame extends javax.swing.JFrame {
 	private JScrollPane jScrollPane1;
 	private JButton fetch;
 	private JScrollPane jScrollPane2;
 	private JLabel text1;
 	private JComboBox statChoice;
 	private JTextArea statText;
 	private JTextField urlText;
 	private JTextArea htmlText;
 	private Main controller;
 	private final LinkedList<DisplayOption> options;
 	
 	/**
 	* Auto-generated main method to display this JFrame
 	*/
 
 	public GUIFrame(Main controller) throws IOException{		
 		super();
 		
 		this.controller = controller;
 		options = new LinkedList<DisplayOption>();
 		options.add(new DisplayOption("Element Statistics", new ElementModule()));
 		options.add(new DisplayOption("Page Statistics", new PageModule()));
 		options.addFirst(new DisplayOption("All Statistics", new AllModules((List<DisplayOption>) options.clone())));
 		
 		initGUI();
 	}
 	
 	private void initGUI() {
 		try {
 			GroupLayout thisLayout = new GroupLayout((JComponent)getContentPane());
 			getContentPane().setLayout(thisLayout);
 			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 			{
 				htmlText = new JTextArea();
 				jScrollPane1 = new JScrollPane(htmlText);
 				jScrollPane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
 				jScrollPane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 				htmlText.setText(" ");
 				htmlText.setPreferredSize(new java.awt.Dimension(352, 1299));
 				htmlText.setEditable(false);
 			}
 			{
 				urlText = new JTextField();
 				urlText.setText("https://www.google.com");
 			}
 			{
 				ComboBoxModel statChoiceModel = 
 						new DefaultComboBoxModel(options.toArray());
 				statChoice = new JComboBox();
 				statChoice.setModel(statChoiceModel);
 			}
 			{
 				text1 = new JLabel();
 				text1.setText("Input URL:");
 			}
 			{
 				fetch = new JButton();
 				fetch.setText("Fetch");
 				fetch.setRolloverEnabled(true);
 				fetch.addMouseListener(new MouseAdapter() {
 					public void mouseClicked(MouseEvent evt) {
 						String url= urlText.getText();
 						DisplayOption choice = (DisplayOption) statChoice.getSelectedItem();
 						String html = "";
 						try {
 							html = controller.fetchPage(url);
 						} catch(Exception e) {
 							JOptionPane.showMessageDialog(null, "Invalid URL", "Error", JOptionPane.ERROR_MESSAGE);
 							// Alert the user that the page doesn't work
 						}
 						htmlText.setText(html);
						try {
							statText.setText(choice.fetchOptionStatistics());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
 					}
 				});
 			}		
 			{
 				statText = new JTextArea();
 				jScrollPane2 = new JScrollPane(statText);
 				jScrollPane2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
 				jScrollPane2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);	
 			
 				statText.setText(" ");
 				statText.setEditable(false);
 			}
 			thisLayout.setVerticalGroup(thisLayout.createSequentialGroup()
 				.addContainerGap()
 				.addGroup(thisLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 				    .addComponent(urlText, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
 				    .addComponent(fetch, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE)
 				    .addComponent(text1, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
 				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 				.addComponent(statChoice, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
 				.addGap(16)
 				.addGroup(thisLayout.createParallelGroup()
 				    .addComponent(jScrollPane1, GroupLayout.Alignment.LEADING, 0, 352, Short.MAX_VALUE)
 				    .addComponent(jScrollPane2, GroupLayout.Alignment.LEADING, 0, 352, Short.MAX_VALUE))
 				.addContainerGap());
 			thisLayout.setHorizontalGroup(thisLayout.createSequentialGroup()
 				.addContainerGap(23, 23)
 				.addGroup(thisLayout.createParallelGroup()
 				    .addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
 				        .addGroup(thisLayout.createParallelGroup()
 				            .addComponent(jScrollPane2, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 242, GroupLayout.PREFERRED_SIZE)
 				            .addComponent(statChoice, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 242, GroupLayout.PREFERRED_SIZE))
 				        .addGap(26)
 				        .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 370, GroupLayout.PREFERRED_SIZE)
 				        .addGap(0, 0, Short.MAX_VALUE))
 				    .addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
 				        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 				        .addComponent(text1, GroupLayout.PREFERRED_SIZE, 98, GroupLayout.PREFERRED_SIZE)
 				        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 				        .addComponent(urlText, 0, 415, Short.MAX_VALUE)
 				        .addGap(24)
 				        .addComponent(fetch, GroupLayout.PREFERRED_SIZE, 101, GroupLayout.PREFERRED_SIZE)))
 				.addContainerGap(17, 17));
 			pack();
 			this.setSize(696, 495);
 		} catch (Exception e) {
 		    //add your error handling code here
 			e.printStackTrace();
 		}
 	}
 
 }
