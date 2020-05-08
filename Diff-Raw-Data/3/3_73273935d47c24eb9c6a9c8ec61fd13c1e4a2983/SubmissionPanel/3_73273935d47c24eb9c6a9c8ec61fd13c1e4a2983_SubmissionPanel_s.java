 package org.vpac.grisu.client.view.swing.template;
 
 
 
 import java.awt.CardLayout;
 import java.awt.Component;
 import java.awt.Cursor;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.filechooser.FileFilter;
 
 import org.apache.log4j.Logger;
 import org.vpac.grisu.client.control.EnvironmentManager;
 import org.vpac.grisu.client.control.exceptions.JobSubmissionException;
 import org.vpac.grisu.client.control.exceptions.TemplateException;
 import org.vpac.grisu.client.control.template.LocalTemplateManagement;
 import org.vpac.grisu.client.control.template.TemplateManager;
 import org.vpac.grisu.client.model.template.JobCreationInterface;
 import org.vpac.grisu.client.model.template.JsdlTemplate;
 import org.vpac.grisu.client.view.swing.environment.FQANSelectorDialog;
 import org.vpac.grisu.client.view.swing.mainPanel.AddApplicationDialog;
 import org.vpac.grisu.client.view.swing.utils.Utils;
import org.vpac.grisu.control.clientexceptions.NoSuchTemplateExceptionClient;
 import org.vpac.grisu.control.exceptions.NoSuchTemplateException;
 import org.vpac.grisu.model.FqanEvent;
 import org.vpac.grisu.model.FqanListener;
 
 import au.org.arcs.jcommons.constants.Constants;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 
 /**
  * The root panel for job submission. Displays a selection of templates (local &
  * remote).
  * 
  * @author Markus Binsteiner
  * 
  */
 public class SubmissionPanel extends JPanel implements JobCreationInterface,
 		FqanListener {
 
 	private JMenuItem refreshLocalTemplateMenuItem;
 	private JMenuItem refreshRemoteTemplate;
 	private JMenuItem deleteLocalTemplate;
 	private JMenuItem editLocalTemplateItem;
 	private JPopupMenu localPopupMenu;
 	private JMenuItem deleteRemoteItem;
 	private JMenuItem copyToLocalItem;
 	private JPopupMenu remotePopupMenu;
 	private JButton changeButton;
 	private JTextField currentVOField;
 	private JSeparator separator;
 	private JLabel currentVoLabel;
 	private JButton addButtonLocal;
 	private JButton removeButtonLocal;
 	private JButton addButtonRemote;
 	private JButton removeButtonRemote;
 	private JComboBox fqanComboBox;
 	private JButton button;
 	static final Logger myLogger = Logger.getLogger(SubmissionPanel.class
 			.getName());
 
 	private JPanel jsdlTemplatePanel;
 	private JLabel remoteLabel;
 	private JLabel localLabel;
 	private JList localList;
 	private JScrollPane scrollPane_1;
 	private JList remoteList;
 	private JScrollPane scrollPane;
 
 	private TemplateManager templateManager = null;
 	private DefaultListModel remoteListModel = new DefaultListModel();
 	private LocalTemplateListModel localListModel = new LocalTemplateListModel();
 	private String currentTemplate = null;
 	private Map<String, JobPanel> allJobPanels = new HashMap<String, JobPanel>();
 
 	private FormLayout layout = null;
 	
 	private EnvironmentManager em = null;
 	
 	//Create a file chooser
 	final JFileChooser fc = new JFileChooser();
 
 	// private String currentFqan = null;
 
 	/**
 	 * Create the panel
 	 */
 	public SubmissionPanel(EnvironmentManager em) {
 		super();
 		
 	    FileFilter filter1 = new ExtensionFileFilter("XML", new String[] { "xml", "XML" });
 	    fc.setFileFilter(filter1);
 	    
 		this.em = em;
 		layout = new FormLayout(
 			new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("30dlu"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("28dlu"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("default:grow(1.0)"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC},
 			new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("default:grow(1.0)"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("default:grow(1.0)"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC});
 		setLayout(layout);
 		add(getScrollPane(), new CellConstraints(2, 12, 3, 1,
 				CellConstraints.FILL, CellConstraints.FILL));
 		add(getScrollPane_1(), new CellConstraints(2, 18, 3, 1,
 				CellConstraints.FILL, CellConstraints.FILL));
 		add(getLocalLabel(), new CellConstraints(2, 16, 3, 1));
 		add(getRemoteLabel(), new CellConstraints(2, 10, 3, 1));
 		add(getJsdlTemplatePanel(), new CellConstraints(6, 2, 5, 17,
 				CellConstraints.FILL, CellConstraints.FILL));
 		add(getButton(), new CellConstraints(10, 20));
 		//
 
 		em.addFqanListener(this);
 		add(getRemoveButtonRemote(), new CellConstraints(4, 14,
 				CellConstraints.FILL, CellConstraints.DEFAULT));
 		add(getAddButtonRemote(), new CellConstraints(2, 14,
 				CellConstraints.FILL, CellConstraints.DEFAULT));
 		add(getRemoveButtonLocal(), new CellConstraints(4, 20,
 				CellConstraints.FILL, CellConstraints.DEFAULT));
 		add(getAddButtonLocal(), new CellConstraints(2, 20,
 				CellConstraints.FILL, CellConstraints.DEFAULT));
 		add(getCurrentVoLabel(), new CellConstraints(2, 2, 3, 1));
 		add(getSeparator(), new CellConstraints(2, 8, 3, 2));
 		add(getCurrentVOField(), new CellConstraints(2, 4, 3, 1));
 		add(getChangeButton(), new CellConstraints(2, 6, 3, 1,
 				CellConstraints.FILL, CellConstraints.DEFAULT));
 
 		setFqanLabelText(em	.getDefaultFqan());
 		// EnvironmentManager.getDefaultManager().addFqanListener(this);
 
 	}
 
 	private void showLocalTemplateComponents(boolean show) {
 
 		getLocalLabel().setVisible(show);
 		getLocalList().setVisible(show);
 		getAddButtonLocal().setVisible(show);
 		getRemoveButtonLocal().setVisible(show);
 		getScrollPane_1().setVisible(show);
 
 		if (!show) {
 			layout.setRowSpec(16, new RowSpec("0dlu"));
 			layout.setRowSpec(18, new RowSpec("0dlu"));
 		} else {
 			layout.setRowSpec(16, FormFactory.DEFAULT_ROWSPEC);
 			layout.setRowSpec(18, FormFactory.DEFAULT_ROWSPEC);
 		}
 	}
 
 	private void showJobTemplatePanel(final int templateLocation,
 			final String templateName) throws NoSuchTemplateException,
 			TemplateException {
 
 		if (templateName != null) {
 
 			final String old_template = currentTemplate;
 			currentTemplate = templateLocation + "_" + templateName;
 
 			// if ( old_template != null && old_template.equals(currentTemplate)
 			// ) {
 			// allJobPanels.get(currentTemplate).toggleView();
 			// return;
 			// }
 
 			JobPanel tempPanel = allJobPanels.get(templateLocation + "_"
 					+ templateName);
 
 			if (tempPanel == null) {
 				tempPanel = new JobPanel();
 				allJobPanels.put(templateLocation + "_" + templateName,
 						tempPanel);
 				getJsdlTemplatePanel().add(tempPanel,
 						templateLocation + "_" + templateName);
 				CardLayout cl = (CardLayout) (getJsdlTemplatePanel()
 						.getLayout());
 				cl.show(getJsdlTemplatePanel(), templateLocation + "_"
 						+ templateName);
 
 				tempPanel.revalidate();
 				final JobPanel newPanel = tempPanel;
 				new Thread() {
 					public void run() {
 						try {
 							JsdlTemplate tempTemplate = templateManager
 									.getTemplate(templateLocation, templateName);
 							if (tempTemplate == null) {
 								myLogger.warn("No template found for name: "
 										+ templateName);
 								currentTemplate = old_template;
 								throw new NoSuchTemplateExceptionClient(
 										"No template found for name: "
 												+ templateName);
 							}
 
 								newPanel.setTemplate(tempTemplate);
 								myLogger
 										.debug("Template set for new templatePanel.");
 							} catch (Exception e) {
 								e.printStackTrace();
 								myLogger
 										.warn("Could not create panel for template \""
 												+ templateName
 												+ "\": "
 												+ e.getLocalizedMessage());
 								currentTemplate = old_template;
 								
 //								Utils.showErrorMessage(em, SubmissionPanel.this, "cantCreateJobPanel", e);
 								
 //								throw new TemplateException(
 //										"Cound not create panel for template \""
 //												+ templateName + "\": "
 //												+ e.getLocalizedMessage(), e);
 							}
 					}
 				}.start();
 
 			} else {
 				CardLayout cl = (CardLayout) (getJsdlTemplatePanel()
 						.getLayout());
 				cl.show(getJsdlTemplatePanel(), templateLocation + "_"
 						+ templateName);
 			}
 			// allJobPanels.get(templateLocation + "_" + templateName);
 		}
 
 		// currentTemplate = templateLocation+"_"+templateName;
 	}
 
 	public JsdlTemplate getTemplate() {
 
 		return allJobPanels.get(currentTemplate).getTemplate();
 	}
 
 	public void setSubmissionFQAN(String fqan) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void setTemplateManager(TemplateManager manager) {
 		this.templateManager = manager;
 
 		for (String templateName : this.templateManager.getServerTemplates()
 				.keySet()) {
 			remoteListModel.addElement(templateName);
 		}
 		for (String templateName : this.templateManager.getLocalTemplates()
 				.keySet()) {
 			localListModel.addElement(templateName);
 		}
 
 		calculateLocalTemplateVisibility();
 	}
 
 	private void calculateLocalTemplateVisibility() {
 		if (localListModel.size() < 1) {
 			showLocalTemplateComponents(false);
 		} else {
 			showLocalTemplateComponents(true);
 		}
 	}
 
 	/**
 	 * @return
 	 */
 	protected JScrollPane getScrollPane() {
 		if (scrollPane == null) {
 			scrollPane = new JScrollPane();
 			addPopup(scrollPane, getRemotePopupMenu());
 			scrollPane.setViewportView(getRemoteList());
 		}
 		return scrollPane;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JList getRemoteList() {
 		if (remoteList == null) {
 			remoteList = new JList(remoteListModel);
 			remoteList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 			remoteList.addMouseListener(new MouseAdapter() {
 
 				public void mouseClicked(final MouseEvent e) {
 
 					if (SwingUtilities.isRightMouseButton(e)) {
 						getRemoteList().setSelectedIndex(
 								getRemoteList().locationToIndex(e.getPoint()));
 
 						localList.clearSelection();
 						String selectedItem = (String) remoteList
 								.getSelectedValue();
 						myLogger.debug("Right clicked on remote Item "
 								+ selectedItem);
 
 						getRemotePopupMenu().show(e.getComponent(), e.getX(),
 								e.getY());
 
 					} else {
 
 						SubmissionPanel.this.setCursor(Cursor
 								.getPredefinedCursor(Cursor.WAIT_CURSOR));
 
 						localList.clearSelection();
 						String selectedItem = (String) remoteList
 								.getSelectedValue();
 						myLogger.debug("Non-right clicked on remote Item "
 								+ selectedItem);
 
 						try {
 							showJobTemplatePanel(
 									TemplateManager.REMOTE_TEMPLATE_LOCATION,
 									selectedItem);
 						} catch (NoSuchTemplateException e1) {
 							// TODO Auto-generated catch block
 							e1.printStackTrace();
 						} catch (TemplateException e2) {
 							// TODO Auto-generated catch block
 							e2.printStackTrace();
 						} finally {
 							SubmissionPanel.this
 									.setCursor(Cursor
 											.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 						}
 					}
 
 				}
 			});
 		}
 		return remoteList;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JScrollPane getScrollPane_1() {
 		if (scrollPane_1 == null) {
 			scrollPane_1 = new JScrollPane();
 			addPopup(scrollPane_1, getLocalPopupMenu());
 			scrollPane_1.setViewportView(getLocalList());
 		}
 		return scrollPane_1;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JList getLocalList() {
 		if (localList == null) {
 			localList = new JList(localListModel);
 			localList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 			localList.addMouseListener(new MouseAdapter() {
 				public void mouseClicked(final MouseEvent e) {
 
 					if (SwingUtilities.isRightMouseButton(e)) {
 						getLocalList().setSelectedIndex(
 								getLocalList().locationToIndex(e.getPoint()));
 
 						remoteList.clearSelection();
 						String selectedItem = (String) localList
 								.getSelectedValue();
 						myLogger.debug("Right clicked on local Item "
 								+ selectedItem);
 
 						getLocalPopupMenu().show(e.getComponent(), e.getX(),
 								e.getY());
 
 					} else {
 
 						SubmissionPanel.this.setCursor(Cursor
 								.getPredefinedCursor(Cursor.WAIT_CURSOR));
 
 						remoteList.clearSelection();
 						String selectedItem = (String) localList
 								.getSelectedValue();
 						myLogger.debug("Clicked on local Item " + selectedItem);
 
 						try {
 							showJobTemplatePanel(
 									TemplateManager.LOCAL_TEMPLATE_LOCATION,
 									selectedItem);
 						} catch (NoSuchTemplateException e1) {
 							// TODO Auto-generated catch block
 							e1.printStackTrace();
 						} catch (TemplateException e2) {
 							// TODO Auto-generated catch block
 							e2.printStackTrace();
 						} finally {
 							SubmissionPanel.this
 									.setCursor(Cursor
 											.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 						}
 					}
 				}
 			});
 		}
 		return localList;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JLabel getLocalLabel() {
 		if (localLabel == null) {
 			localLabel = new JLabel();
 			localLabel.setText("Local templates");
 		}
 		return localLabel;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JLabel getRemoteLabel() {
 		if (remoteLabel == null) {
 			remoteLabel = new JLabel();
 			remoteLabel.setText("Applications");
 		}
 		return remoteLabel;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JPanel getJsdlTemplatePanel() {
 		if (jsdlTemplatePanel == null) {
 			jsdlTemplatePanel = new JPanel();
 			jsdlTemplatePanel.setLayout(new CardLayout());
 		}
 		return jsdlTemplatePanel;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JButton getButton() {
 		if (button == null) {
 			button = new JButton();
 			button.addActionListener(new ActionListener() {
 				public void actionPerformed(final ActionEvent e) {
 
 					try {
 						JsdlTemplate template = allJobPanels.get(
 								SubmissionPanel.this.currentTemplate)
 								.getTemplate();
 						template.startSubmission(em.getDefaultFqan());
 						
 					} catch (JobSubmissionException e1) {
 
 						Utils.showErrorMessage(em, SubmissionPanel.this,
 								"jobSubmissionError", e1);
 
 					}
 
 				}
 			});
 			button.setText("Submit");
 		}
 		return button;
 	}
 
 	private void setFqanLabelText(String fqan) {
 		String fqan_short = null;
 		if (fqan == null || Constants.NON_VO_FQAN.equals(fqan)) {
 			fqan_short = Constants.NON_VO_FQAN;
 		} else {
 			fqan_short = fqan.substring(fqan.lastIndexOf("/") + 1);
 		}
 		getCurrentVOField().setText(fqan_short);
 		getCurrentVOField().setToolTipText(fqan);
 	}
 
 	public void fqansChanged(FqanEvent event) {
 
 		if (FqanEvent.DEFAULT_FQAN_CHANGED == event.getEvent_type()) {
 			setFqanLabelText(event.getFqan());
 		} else if (FqanEvent.FQAN_ADDED == event.getEvent_type()) {
 			// nothing to do here
 		} else if (FqanEvent.FQAN_REMOVED == event.getEvent_type()) {
 			// nothing to do here
 		} else if (FqanEvent.FQANS_REFRESHED == event.getEvent_type()) {
 			setFqanLabelText(em.getDefaultFqan());
 		}
 	}
 
 	/**
 	 * @return
 	 */
 	protected JButton getRemoveButtonRemote() {
 		if (removeButtonRemote == null) {
 			removeButtonRemote = new JButton();
 			removeButtonRemote.addActionListener(new ActionListener() {
 				public void actionPerformed(final ActionEvent e) {
 
 					removeRemoteTemplateFromList();
 
 				}
 			});
 			removeButtonRemote.setText("-");
 		}
 		return removeButtonRemote;
 	}
 
 	private void removeRemoteTemplateFromList() {
 		int selIndex = getRemoteList().getSelectedIndex();
 		if (selIndex != -1) {
 			String selTemplateName = (String) remoteListModel.get(selIndex);
 
 			SubmissionPanel.this.templateManager
 					.removeServerTemplate(selTemplateName);
 
 			remoteListModel.remove(selIndex);
 		}
 	}
 	
 	private void refreshTemplate(int templateLocation, String templateName) throws NoSuchTemplateException, TemplateException {
 		
 //		final String old_template = currentTemplate;
 //		currentTemplate = templateLocation + "_" + templateName;
 
 		JobPanel tempPanel = allJobPanels.get(templateLocation + "_"
 				+ templateName);
 		
 		getJsdlTemplatePanel().remove(tempPanel);
 		allJobPanels.remove(templateLocation + "_"
 				+ templateName);
 
 		if ( templateLocation == TemplateManager.REMOTE_TEMPLATE_LOCATION ) {
 			templateManager.refreshServerTemplate(templateName);
 			showJobTemplatePanel(TemplateManager.REMOTE_TEMPLATE_LOCATION, templateName);
 		} else if ( templateLocation == TemplateManager.LOCAL_TEMPLATE_LOCATION ) {
 			templateManager.refreshLocalTemplates();
 			showJobTemplatePanel(TemplateManager.LOCAL_TEMPLATE_LOCATION, templateName);
 		}
 
 		
 	}
 
 	/**
 	 * @return
 	 */
 	protected JButton getAddButtonRemote() {
 		if (addButtonRemote == null) {
 			addButtonRemote = new JButton();
 			addButtonRemote.addActionListener(new ActionListener() {
 				public void actionPerformed(final ActionEvent e) {
 
 					AddApplicationDialog aad = new AddApplicationDialog(null,
 							templateManager.getEnvironmentManager()
 									.getServiceInterface());
 
 					aad.setVisible(true);
 
 					String[] apps = aad.getApplicationName();
 
 					for (String app : apps) {
 						templateManager.addServerTemplate(app);
 
 						if (remoteListModel.indexOf(app) < 0) {
 							remoteListModel.addElement(app);
 						}
 					}
 				}
 			});
 			addButtonRemote.setText("+");
 		}
 		return addButtonRemote;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JButton getRemoveButtonLocal() {
 		if (removeButtonLocal == null) {
 			removeButtonLocal = new JButton();
 			removeButtonLocal.addActionListener(new ActionListener() {
 				public void actionPerformed(final ActionEvent e) {
 
 					String templateName = null;
 					try {
 						templateName = (String) localListModel.get(getLocalList()
 							.getSelectedIndex());
 					} catch (Exception ex) {
 						// nothing selected probably
 						return;
 					}
 					
 					int n = JOptionPane.showConfirmDialog(
 						    SubmissionPanel.this,
 						    "<html><body>Are you sure you want to delete the<br><b>"+templateName+"</b><br>template?</body></html>",
 						    "Deleting template",
 						    JOptionPane.YES_NO_OPTION);
 
 					if ( n == JOptionPane.YES_OPTION ) {
 						deleteLocalTemplate();
 					}
 				}
 			});
 			removeButtonLocal.setText("-");
 		}
 		return removeButtonLocal;
 	}
 
 	private void deleteLocalTemplate() {
 		int index = getLocalList().getSelectedIndex();
 		if (index != -1) {
 			String templateName = (String) localListModel.get(getLocalList()
 					.getSelectedIndex());
 			SubmissionPanel.this.templateManager
 					.removeLocalTemplate(templateName);
 			localListModel.removeElement(templateName);
 			calculateLocalTemplateVisibility();
 		}
 
 	}
 
 	/**
 	 * @return
 	 */
 	protected JButton getAddButtonLocal() {
 		if (addButtonLocal == null) {
 			addButtonLocal = new JButton();
 			addButtonLocal.addActionListener(new ActionListener() {
 				public void actionPerformed(final ActionEvent e) {
 
 					addLocalTemplate();
 
 				}
 			});
 			addButtonLocal.setText("+");
 			addButtonLocal.setToolTipText("Add a template file to the local template store.");
 		}
 		return addButtonLocal;
 	}
 	
 	public void addLocalTemplate() {
 		//In response to a button click:
 		int returnVal = fc.showOpenDialog(SubmissionPanel.this);
 		
 		File file = null;
         if (returnVal == JFileChooser.APPROVE_OPTION) {
             file = fc.getSelectedFile();
             //This is where a real application would open the file.
             myLogger.debug("Opening: " + file.getName() + ".");
         } else {
             myLogger.debug("Open command cancelled by user.");
         }
         
         if ( file.exists() ) {
         	try {
         		
         		File newFile = new File(LocalTemplateManagement.TEMPLATE_DIRECTORY, file.getName());
         		
         		if ( newFile.exists() ) {
         			int value = JOptionPane.showConfirmDialog(
         					SubmissionPanel.this,
         				    "A file with the name: \""+newFile.getName()+"\"\n"
         				    + "already exists in the local template store.\n"
         				    + "Do you want to overwrite it?",
         				    "File exists",
         				    JOptionPane.YES_NO_OPTION);
         			
 
         			if (value == JOptionPane.NO_OPTION) {
         				return;
         			} 
 
         		}
 				String localTemplateName = templateManager
 				.addLocalTemplate(file, true);
 				
 				localListModel.addElement(localTemplateName);
 
 				calculateLocalTemplateVisibility();
         		
 				SubmissionPanel.this.templateManager.addLocalTemplate(file, true);
 			} catch (IOException e1) {
 	        	JOptionPane.showMessageDialog(SubmissionPanel.this,
 	        		    "Can't copy file "+file.toString()+" to local template store: "+e1.getLocalizedMessage(),
 	        		    "File error",
 	        		    JOptionPane.ERROR_MESSAGE);
 
 			}
         } else {
         	
         	JOptionPane.showMessageDialog(SubmissionPanel.this,
         		    "File "+file.toString()+" doesn't exist. Can't copy it...",
         		    "File error",
         		    JOptionPane.ERROR_MESSAGE);
 
         	
         }
 	}
 
 	/**
 	 * @return
 	 */
 	protected JLabel getCurrentVoLabel() {
 		if (currentVoLabel == null) {
 			currentVoLabel = new JLabel();
 			currentVoLabel.setText("Current VO:");
 		}
 		return currentVoLabel;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JSeparator getSeparator() {
 		if (separator == null) {
 			separator = new JSeparator();
 		}
 		return separator;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JTextField getCurrentVOField() {
 		if (currentVOField == null) {
 			currentVOField = new JTextField();
 			currentVOField.setEditable(false);
 			currentVOField.setHorizontalAlignment(SwingConstants.CENTER);
 		}
 		return currentVOField;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JButton getChangeButton() {
 		if (changeButton == null) {
 			changeButton = new JButton();
 			changeButton.addActionListener(new ActionListener() {
 				public void actionPerformed(final ActionEvent e) {
 
 					FQANSelectorDialog dialog = new FQANSelectorDialog();
 					dialog.setEnvironmentManager(em);
 					dialog.setVisible(true);
 
 				}
 			});
 			changeButton.setText("Change");
 		}
 		return changeButton;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JPopupMenu getRemotePopupMenu() {
 		if (remotePopupMenu == null) {
 			remotePopupMenu = new JPopupMenu();
 			remotePopupMenu.add(getRefreshRemoteTemplate());
 			remotePopupMenu.add(getCopyToLocalItem());
 			remotePopupMenu.add(getDeleteRemoteItem());
 		}
 		return remotePopupMenu;
 	}
 
 	private static void addPopup(Component component, final JPopupMenu popup) {
 		component.addMouseListener(new MouseAdapter() {
 			public void mousePressed(MouseEvent e) {
 				if (e.isPopupTrigger())
 					showMenu(e);
 			}
 
 			public void mouseReleased(MouseEvent e) {
 				if (e.isPopupTrigger())
 					showMenu(e);
 			}
 
 			private void showMenu(MouseEvent e) {
 				popup.show(e.getComponent(), e.getX(), e.getY());
 			}
 		});
 	}
 
 	/**
 	 * @return
 	 */
 	protected JMenuItem getCopyToLocalItem() {
 		if (copyToLocalItem == null) {
 			copyToLocalItem = new JMenuItem();
 			copyToLocalItem.addActionListener(new ActionListener() {
 				public void actionPerformed(final ActionEvent e) {
 
 					String localTemplateName = templateManager
 							.addLocalTemplate((String) getRemoteList()
 									.getSelectedValue());
 					localListModel.addElement(localTemplateName);
 
 					calculateLocalTemplateVisibility();
 				}
 			});
 			copyToLocalItem.setText("Copy to local template store");
 		}
 		return copyToLocalItem;
 	}
 	
 
 	/**
 	 * @return
 	 */
 	protected JMenuItem getDeleteRemoteItem() {
 		if (deleteRemoteItem == null) {
 			deleteRemoteItem = new JMenuItem();
 			deleteRemoteItem.addActionListener(new ActionListener() {
 				public void actionPerformed(final ActionEvent e) {
 
 					removeRemoteTemplateFromList();
 				}
 			});
 			deleteRemoteItem.setText("Remove from list");
 		}
 		return deleteRemoteItem;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JPopupMenu getLocalPopupMenu() {
 		if (localPopupMenu == null) {
 			localPopupMenu = new JPopupMenu();
 			localPopupMenu.add(getRefreshLocalTemplateMenuItem());
 			localPopupMenu.add(getDeleteLocalTemplate());
 			localPopupMenu.add(getEditLocalTemplateItem());
 		}
 		return localPopupMenu;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JMenuItem getEditLocalTemplateItem() {
 		if (editLocalTemplateItem == null) {
 			editLocalTemplateItem = new JMenuItem();
 			editLocalTemplateItem.setText("Edit template");
 			editLocalTemplateItem.setEnabled(false);
 		}
 		return editLocalTemplateItem;
 	}
 
 	/**
 	 * @return
 	 */
 	protected JMenuItem getDeleteLocalTemplate() {
 		if (deleteLocalTemplate == null) {
 			deleteLocalTemplate = new JMenuItem();
 			deleteLocalTemplate.addActionListener(new ActionListener() {
 				public void actionPerformed(final ActionEvent e) {
 					String templateName = null;
 					try {
 						templateName = (String) localListModel.get(getLocalList()
 							.getSelectedIndex());
 					} catch (Exception ex) {
 						// nothing selected probably
 						return;
 					}
 					int n = JOptionPane.showConfirmDialog(
 						    SubmissionPanel.this,
 						    "<html><body>Are you sure you want to delete the<br><b>"+templateName+"</b><br>template?</body></html>",
 						    "Deleting template",
 						    JOptionPane.YES_NO_OPTION);
 
 					if ( n == JOptionPane.YES_OPTION ) {
 						deleteLocalTemplate();
 					}
 				}
 			});
 			deleteLocalTemplate.setText("Delete template");
 		}
 		return deleteLocalTemplate;
 	}
 	/**
 	 * @return
 	 */
 	protected JMenuItem getRefreshRemoteTemplate() {
 		if (refreshRemoteTemplate == null) {
 			refreshRemoteTemplate = new JMenuItem();
 			refreshRemoteTemplate.addActionListener(new ActionListener() {
 				public void actionPerformed(final ActionEvent e) {
 					
 					String templateName = (String)(getRemoteList().getSelectedValue());
 
 					try {
 						refreshTemplate(TemplateManager.REMOTE_TEMPLATE_LOCATION, templateName);
 					} catch (Exception e1) {
 						Utils.showErrorMessage(em, SubmissionPanel.this, "cantRefreshRemoteTemplate", e1);
 					}
 					
 					
 				}
 			});
 			refreshRemoteTemplate.setText("Refresh template");
 		}
 		return refreshRemoteTemplate;
 	}
 	/**
 	 * @return
 	 */
 	protected JMenuItem getRefreshLocalTemplateMenuItem() {
 		if (refreshLocalTemplateMenuItem == null) {
 			refreshLocalTemplateMenuItem = new JMenuItem();
 			refreshLocalTemplateMenuItem.addActionListener(new ActionListener() {
 				public void actionPerformed(final ActionEvent e) {
 					String templateName = (String)(getLocalList().getSelectedValue());
 
 					try {
 						refreshTemplate(TemplateManager.LOCAL_TEMPLATE_LOCATION, templateName);
 					} catch (Exception e1) {
 						Utils.showErrorMessage(em, SubmissionPanel.this, "cantRefreshLocalTemplate", e1);
 					}
 				}
 			});
 			refreshLocalTemplateMenuItem.setText("Refresh template");
 		}
 		return refreshLocalTemplateMenuItem;
 	}
 	/**
 	 * @return
 	 */
 }
