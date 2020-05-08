 package cm.view;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Toolkit;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import javax.swing.BorderFactory;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFormattedTextField;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.SwingConstants;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.filechooser.FileFilter;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.stream.XMLStreamWriter;
 
 import org.dyno.visual.swing.layouts.Bilateral;
 import org.dyno.visual.swing.layouts.Constraints;
 import org.dyno.visual.swing.layouts.GroupLayout;
 import org.dyno.visual.swing.layouts.Leading;
 import org.dyno.visual.swing.layouts.Trailing;
 import org.xml.sax.InputSource;
 
 import cm.model.EffectBase;
 import cm.model.EffectBase.Duration;
 import cm.model.Power;
 import cm.model.Settings;
 import cm.model.Stats;
 import cm.util.AutoCompletion;
 import cm.view.render.EffectBaseCellRenderer;
 import cm.view.render.PowerDetailsCellRenderer;
 
 import com.sun.xml.internal.ws.api.streaming.XMLStreamReaderFactory;
 import com.sun.xml.internal.ws.api.streaming.XMLStreamWriterFactory;
 
 //VS4E -- DO NOT REMOVE THIS LINE!
 public class Statblock extends JDialog {
 
 	private static final long serialVersionUID = 1L;
 	private JLabel jLabelName;
 	private JTextField jTextFieldName;
 	private JPanel jPanelDescription;
 	private JLabel jLabelLevel;
 	private JLabel jLabelTypeKeywords;
 	private JSpinner jSpinnerLevel;
 	private JLabel jLabelRole;
 	private JComboBox jComboBoxRole2;
 	private JComboBox jComboBoxRole;
 	private JCheckBox jCheckBoxLeader;
 	private JCheckBox jCheckBoxPC;
 	private JTextField jTextFieldTypeKeywords;
 	private JPanel jPanelDefenses;
 	private JLabel jLabelAC;
 	private JLabel jLabelFortitude;
 	private JLabel jLabelReflex;
 	private JLabel jLabelWill;
 	private JFormattedTextField jFormattedTextFieldAC;
 	private JFormattedTextField jFormattedTextFieldFortitude;
 	private JFormattedTextField jFormattedTextFieldReflex;
 	private JFormattedTextField jFormattedTextFieldWill;
 	private JPanel jPanelAttributes;
 	private JLabel jLabelStr;
 	private JLabel jLabelWis;
 	private JLabel jLabelDex;
 	private JFormattedTextField jFormattedTextFieldStr;
 	private JFormattedTextField jFormattedTextFieldDex;
 	private JFormattedTextField jFormattedTextFieldWis;
 	private JLabel jLabelCon;
 	private JLabel jLabelInt;
 	private JLabel jLabelCha;
 	private JFormattedTextField jFormattedTextFieldCon;
 	private JFormattedTextField jFormattedTextFieldInt;
 	private JFormattedTextField jFormattedTextFieldCha;
 	private JPanel jPanelXPValue;
 	private JFormattedTextField jFormattedTextFieldXPValue;
 	private JPanel jPanelHitPoints;
 	private JLabel jLabelMax;
 	private JLabel jLabelSurges;
 	private JFormattedTextField jFormattedTextFieldMax;
 	private JFormattedTextField jFormattedTextFieldSurges;
 	private JTabbedPane jTabbedPaneTraits;
 	private JPanel jPanelTraits;
 	private JLabel jLabelSenses;
 	private JTextField jTextFieldSenses;
 	private JTextField jTextFieldImmune;
 	private JTextField jTextFieldResist;
 	private JTextField jTextFieldVulnerable;
 	private JTextField jTextFieldRegen;
 	private JTextField jTextFieldSpeed;
 	private JLabel jLabelImmune;
 	private JLabel jLabelResist;
 	private JLabel jLabelVulnerable;
 	private JLabel jLabelRegen;
 	private JLabel jLabelSpeed;
 	private JFormattedTextField jFormattedTextFieldInitiative;
 	private JLabel jLabelInitiative;
 	private JLabel jLabelActionPts;
 	private JFormattedTextField jFormattedTextFieldActionPts;
 	private JLabel jLabelPowerPts;
 	private JFormattedTextField jFormattedTextFieldPowerPts;
 	private JLabel jLabelSaveBonus;
 	private JFormattedTextField jFormattedTextFieldSaveBonus;
 	private JTextField jTextFieldAlignment;
 	private JTextField jTextFieldSkills;
 	private JTextField jTextFieldFeats;
 	private JTextField jTextFieldLanguages;
 	private JTextField jTextFieldEquipment;
 	private JTextField jTextFieldSource;
 	private JLabel jLabelAlignment;
 	private JLabel jLabelSkills;
 	private JLabel jLabelFeats;
 	private JLabel jLabelLanguages;
 	private JLabel jLabelEquipment;
 	private JLabel jLabelSource;
 	private JPanel jPanelGeneratedEffects;
 	private JLabel jLabelEffect;
 	private JLabel jLabelDuration;
 	private JComboBox jComboBoxEffect;
 	private JComboBox jComboBoxDuration;
 	private JCheckBox jCheckBoxBeneficial;
 	private JCheckBox jCheckBoxHidden;
 	private JButton jButtonAdd;
 	private JButton jButtonEdit;
 	private JButton jButtonExport;
 	private JButton jButtonImport;
 	private JButton jButtonDelete;
 	private JList jListEffects;
 	private JScrollPane jScrollPaneEffects;
 	private JTextPane jTextPaneNotes;
 	private JScrollPane jScrollPaneNotes;
 	private JPanel jPanelPowers;
 	private JButton jButtonRTFPaste;
 	private JButton jButtonCBLoad;
 	private JButton jButtonOK;
 	private JButton jButtonCancel;
 	private JList jListPowers;
 	private JScrollPane jScrollPanePowers;
 	private JButton jButtonPowerNew;
 	private JButton jButtonPowerDelete;
 	private JButton jButtonPowerUp;
 	private JButton jButtonPowerDown;
 	private JLabel jLabelPowerName;
 	private JTextField jTextFieldPowerName;
 	private JTextField jTextFieldPowerAction;
 	private JTextField jTextFieldPowerKeywords;
 	private JTextField jTextFieldPowerURL;
 	private JLabel jLabelPowerAction;
 	private JLabel jLabelPowerKeywords;
 	private JLabel jLabelPowerURL;
 	private JLabel jLabelPowerIcon;
 	private JComboBox jComboBoxPowerIcon;
 	private JCheckBox jCheckBoxPowerAura;
 	private JTextArea jTextAreaPowerDescription;
 	private JScrollPane jScrollPanePowerDescription;
 	
 	public Statblock() {
 		initComponents();
 	}
 
 	private void initComponents() {
 		setTitle("Statblock Viewer");
 		setFont(new Font("Dialog", Font.PLAIN, 12));
 		setBackground(Color.white);
 		setResizable(false);
 		setModal(true);
 		setForeground(Color.black);
 		setLayout(new GroupLayout());
 		add(getJLabelName(), new Constraints(new Leading(12, 12, 12), new Leading(12, 12, 12)));
 		add(getJTextFieldName(), new Constraints(new Leading(54, 396, 10, 10), new Leading(10, 12, 12)));
 		add(getJPanelDescription(), new Constraints(new Leading(12, 438, 12, 12), new Leading(34, 70, 10, 10)));
 		add(getJPanelDefenses(), new Constraints(new Leading(12, 117, 10, 10), new Leading(110, 112, 10, 10)));
 		add(getJPanelAttributes(), new Constraints(new Leading(131, 161, 10, 10), new Leading(110, 112, 12, 12)));
 		add(getJPanelXPValue(), new Constraints(new Leading(293, 157, 12, 12), new Leading(110, 47, 10, 10)));
 		add(getJPanelHitPoints(), new Constraints(new Leading(293, 156, 12, 12), new Leading(163, 59, 12, 12)));
 		add(getJTabbedPaneTraits(), new Constraints(new Leading(12, 438, 12, 12), new Bilateral(223, 0, 7)));
 		add(getJPanelPowers(), new Constraints(new Bilateral(456, 12, 0), new Bilateral(10, 44, 0)));
 		add(getJButtonRTFPaste(), new Constraints(new Leading(456, 12, 12), new Trailing(12, 50, 50)));
 		add(getJButtonCBLoad(), new Constraints(new Leading(553, 12, 12), new Trailing(12, 50, 50)));
 		add(getJButtonCancel(), new Constraints(new Trailing(12, 703, 703), new Trailing(12, 50, 50)));
 		add(getJButtonOK(), new Constraints(new Trailing(91, 646, 646), new Trailing(12, 50, 50)));
 		pack();
 	}
 
 	private JFormattedTextField getJFormattedTextFieldAuraSize() {
 		if (jFormattedTextFieldAuraSize == null) {
 			jFormattedTextFieldAuraSize = new JFormattedTextField();
 			jFormattedTextFieldAuraSize.setText("0");
 			jFormattedTextFieldAuraSize.setEnabled(false);
 			jFormattedTextFieldAuraSize.addFocusListener(new FocusAdapter() {
 	
 				public void focusLost(FocusEvent event) {
 					jFormattedTextFieldAuraSizeFocusFocusLost(event);
 				}
 			});
 		}
 		return jFormattedTextFieldAuraSize;
 	}
 
 	private JScrollPane getJScrollPanePowerDescription() {
 		if (jScrollPanePowerDescription == null) {
 			jScrollPanePowerDescription = new JScrollPane();
 			jScrollPanePowerDescription.setBorder(BorderFactory.createTitledBorder(null, "Description", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
 					new Font("Dialog", Font.PLAIN, 12), new Color(51, 51, 51)));
 			jScrollPanePowerDescription.setViewportView(getJTextAreaPowerDescription());
 		}
 		return jScrollPanePowerDescription;
 	}
 
 	private JTextArea getJTextAreaPowerDescription() {
 		if (jTextAreaPowerDescription == null) {
 			jTextAreaPowerDescription = new JTextArea();
 			jTextAreaPowerDescription.setEnabled(false);
 			jTextAreaPowerDescription.addFocusListener(new FocusAdapter() {
 	
 				public void focusLost(FocusEvent event) {
 					jTextAreaPowerDescriptionFocusFocusLost(event);
 				}
 			});
 		}
 		return jTextAreaPowerDescription;
 	}
 
 	private JCheckBox getJCheckBoxPowerAura() {
 		if (jCheckBoxPowerAura == null) {
 			jCheckBoxPowerAura = new JCheckBox();
 			jCheckBoxPowerAura.setText("Aura");
 			jCheckBoxPowerAura.setEnabled(false);
 			jCheckBoxPowerAura.addActionListener(new ActionListener() {
 	
 				public void actionPerformed(ActionEvent event) {
 					jCheckBoxPowerAuraActionActionPerformed(event);
 				}
 			});
 		}
 		return jCheckBoxPowerAura;
 	}
 
 	private JComboBox getJComboBoxPowerIcon() {
 		if (jComboBoxPowerIcon == null) {
 			jComboBoxPowerIcon = new JComboBox();
 			jComboBoxPowerIcon.setModel(new DefaultComboBoxModel(new Object[] { "(no icon)", "Basic Melee", "Basic Ranged", "Melee", "Ranged", "Close", "Area" }));
 			jComboBoxPowerIcon.setDoubleBuffered(false);
 			jComboBoxPowerIcon.setBorder(null);
 			jComboBoxPowerIcon.setEnabled(false);
 			jComboBoxPowerIcon.addFocusListener(new FocusAdapter() {
 	
 				public void focusLost(FocusEvent event) {
 					jComboBoxPowerIconFocusFocusLost(event);
 				}
 			});
 		}
 		return jComboBoxPowerIcon;
 	}
 
 	private JLabel getJLabelPowerIcon() {
 		if (jLabelPowerIcon == null) {
 			jLabelPowerIcon = new JLabel();
 			jLabelPowerIcon.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jLabelPowerIcon.setHorizontalAlignment(SwingConstants.TRAILING);
 			jLabelPowerIcon.setText("Icon");
 		}
 		return jLabelPowerIcon;
 	}
 
 	private JLabel getJLabelPowerURL() {
 		if (jLabelPowerURL == null) {
 			jLabelPowerURL = new JLabel();
 			jLabelPowerURL.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jLabelPowerURL.setHorizontalAlignment(SwingConstants.TRAILING);
 			jLabelPowerURL.setText("URL");
 		}
 		return jLabelPowerURL;
 	}
 
 	private JLabel getJLabelPowerKeywords() {
 		if (jLabelPowerKeywords == null) {
 			jLabelPowerKeywords = new JLabel();
 			jLabelPowerKeywords.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jLabelPowerKeywords.setHorizontalAlignment(SwingConstants.TRAILING);
 			jLabelPowerKeywords.setText("Keywords");
 		}
 		return jLabelPowerKeywords;
 	}
 
 	private JLabel getJLabelPowerAction() {
 		if (jLabelPowerAction == null) {
 			jLabelPowerAction = new JLabel();
 			jLabelPowerAction.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jLabelPowerAction.setHorizontalAlignment(SwingConstants.TRAILING);
 			jLabelPowerAction.setText("Action");
 		}
 		return jLabelPowerAction;
 	}
 
 	private JTextField getJTextFieldPowerURL() {
 		if (jTextFieldPowerURL == null) {
 			jTextFieldPowerURL = new JTextField();
 			jTextFieldPowerURL.setEnabled(false);
 		}
 		return jTextFieldPowerURL;
 	}
 
 	private JTextField getJTextFieldPowerKeywords() {
 		if (jTextFieldPowerKeywords == null) {
 			jTextFieldPowerKeywords = new JTextField();
 			jTextFieldPowerKeywords.setEnabled(false);
 			jTextFieldPowerKeywords.addFocusListener(new FocusAdapter() {
 	
 				public void focusLost(FocusEvent event) {
 					jTextFieldPowerKeywordsFocusFocusLost(event);
 				}
 			});
 		}
 		return jTextFieldPowerKeywords;
 	}
 
 	private JTextField getJTextFieldPowerAction() {
 		if (jTextFieldPowerAction == null) {
 			jTextFieldPowerAction = new JTextField();
 			jTextFieldPowerAction.setEnabled(false);
 			jTextFieldPowerAction.addFocusListener(new FocusAdapter() {
 	
 				public void focusLost(FocusEvent event) {
 					jTextFieldPowerActionFocusFocusLost(event);
 				}
 			});
 		}
 		return jTextFieldPowerAction;
 	}
 
 	private JTextField getJTextFieldPowerName() {
 		if (jTextFieldPowerName == null) {
 			jTextFieldPowerName = new JTextField();
 			jTextFieldPowerName.setEnabled(false);
 			jTextFieldPowerName.addFocusListener(new FocusAdapter() {
 	
 				public void focusLost(FocusEvent event) {
 					jTextFieldPowerNameFocusFocusLost(event);
 				}
 			});
 		}
 		return jTextFieldPowerName;
 	}
 
 	private JLabel getJLabelPowerName() {
 		if (jLabelPowerName == null) {
 			jLabelPowerName = new JLabel();
 			jLabelPowerName.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jLabelPowerName.setHorizontalAlignment(SwingConstants.TRAILING);
 			jLabelPowerName.setText("Name");
 		}
 		return jLabelPowerName;
 	}
 
 	private JButton getJButtonPowerDown() {
 		if (jButtonPowerDown == null) {
 			jButtonPowerDown = new JButton();
 			jButtonPowerDown.setText("Down");
 			jButtonPowerDown.setEnabled(false);
 			jButtonPowerDown.addActionListener(new ActionListener() {
 	
 				public void actionPerformed(ActionEvent event) {
 					jButtonPowerDownActionActionPerformed(event);
 				}
 			});
 		}
 		return jButtonPowerDown;
 	}
 
 	private JButton getJButtonPowerUp() {
 		if (jButtonPowerUp == null) {
 			jButtonPowerUp = new JButton();
 			jButtonPowerUp.setText("Up");
 			jButtonPowerUp.setEnabled(false);
 			jButtonPowerUp.addActionListener(new ActionListener() {
 	
 				public void actionPerformed(ActionEvent event) {
 					jButtonPowerUpActionActionPerformed(event);
 				}
 			});
 		}
 		return jButtonPowerUp;
 	}
 
 	private JButton getJButtonPowerDelete() {
 		if (jButtonPowerDelete == null) {
 			jButtonPowerDelete = new JButton();
 			jButtonPowerDelete.setText("Delete");
 			jButtonPowerDelete.setEnabled(false);
 			jButtonPowerDelete.addActionListener(new ActionListener() {
 	
 				public void actionPerformed(ActionEvent event) {
 					jButtonPowerDeleteActionActionPerformed(event);
 				}
 			});
 		}
 		return jButtonPowerDelete;
 	}
 
 	private JButton getJButtonPowerNew() {
 		if (jButtonPowerNew == null) {
 			jButtonPowerNew = new JButton();
 			jButtonPowerNew.setText("New");
 			jButtonPowerNew.addActionListener(new ActionListener() {
 	
 				public void actionPerformed(ActionEvent event) {
 					jButtonPowerNewActionActionPerformed(event);
 				}
 			});
 		}
 		return jButtonPowerNew;
 	}
 
 	private JScrollPane getJScrollPanePowers() {
 		if (jScrollPanePowers == null) {
 			jScrollPanePowers = new JScrollPane();
 			jScrollPanePowers.setViewportView(getJListPowers());
 		}
 		return jScrollPanePowers;
 	}
 
 	private JList getJListPowers() {
 		if (jListPowers == null) {
 			jListPowers = new JList();
 			DefaultListModel listModel = new DefaultListModel();
 			jListPowers.setModel(listModel);
 			jListPowers.setCellRenderer(new PowerDetailsCellRenderer());
 			jListPowers.addListSelectionListener(new ListSelectionListener() {
 	
 				public void valueChanged(ListSelectionEvent event) {
 					jListPowersListSelectionValueChanged(event);
 				}
 			});
 		}
 		return jListPowers;
 	}
 
 	private JButton getJButtonCancel() {
 		if (jButtonCancel == null) {
 			jButtonCancel = new JButton();
 			jButtonCancel.setText("Cancel");
 			jButtonCancel.addActionListener(new ActionListener() {
 	
 				public void actionPerformed(ActionEvent event) {
 					jButtonCancelActionActionPerformed(event);
 				}
 			});
 		}
 		return jButtonCancel;
 	}
 
 	private JButton getJButtonOK() {
 		if (jButtonOK == null) {
 			jButtonOK = new JButton();
 			jButtonOK.setText("OK");
 			jButtonOK.addActionListener(new ActionListener() {
 	
 				public void actionPerformed(ActionEvent event) {
 					jButtonOKActionActionPerformed(event);
 				}
 			});
 		}
 		return jButtonOK;
 	}
 
 	private JButton getJButtonCBLoad() {
 		if (jButtonCBLoad == null) {
 			jButtonCBLoad = new JButton();
 			jButtonCBLoad.setText("CB Load");
 			jButtonCBLoad.addActionListener(new ActionListener() {
 	
 				public void actionPerformed(ActionEvent event) {
 					jButtonCBLoadActionActionPerformed(event);
 				}
 			});
 		}
 		return jButtonCBLoad;
 	}
 
 	private JButton getJButtonRTFPaste() {
 		if (jButtonRTFPaste == null) {
 			jButtonRTFPaste = new JButton();
 			jButtonRTFPaste.setText("RTF Paste");
 			jButtonRTFPaste.addActionListener(new ActionListener() {
 	
 				public void actionPerformed(ActionEvent event) {
 					jButtonRTFPasteActionActionPerformed(event);
 				}
 			});
 		}
 		return jButtonRTFPaste;
 	}
 
 	private JPanel getJPanelPowers() {
 		if (jPanelPowers == null) {
 			jPanelPowers = new JPanel();
 			jPanelPowers.setBorder(BorderFactory.createTitledBorder(null, "Powers", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, new Font("Dialog",
 					Font.PLAIN, 12), new Color(51, 51, 51)));
 			jPanelPowers.setLayout(new GroupLayout());
 			jPanelPowers.add(getJScrollPanePowers(), new Constraints(new Bilateral(11, 76, 22), new Leading(0, 184, 10, 10)));
 			jPanelPowers.add(getJButtonPowerNew(), new Constraints(new Trailing(0, 74, 104, 341), new Leading(0, 12, 12)));
			jPanelPowers.add(getJButtonPowerDelete(), new Constraints(new Trailing(0, 74, 104, 341), new Leading(31, 12, 12)));
 			jPanelPowers.add(getJButtonPowerUp(), new Constraints(new Trailing(0, 74, 104, 341), new Leading(125, 12, 12)));
 			jPanelPowers.add(getJButtonPowerDown(), new Constraints(new Trailing(0, 74, 104, 341), new Leading(157, 12, 12)));
 			jPanelPowers.add(getJLabelPowerName(), new Constraints(new Leading(12, 63, 22, 253), new Leading(191, 12, 12)));
 			jPanelPowers.add(getJLabelPowerAction(), new Constraints(new Leading(12, 63, 22, 253), new Leading(213, 12, 12)));
 			jPanelPowers.add(getJLabelPowerKeywords(), new Constraints(new Leading(12, 63, 22, 253), new Leading(235, 12, 12)));
 			jPanelPowers.add(getJLabelPowerURL(), new Constraints(new Leading(12, 63, 22, 253), new Leading(258, 12, 12)));
 			jPanelPowers.add(getJLabelPowerIcon(), new Constraints(new Leading(12, 63, 22, 253), new Leading(282, 12, 12)));
 			jPanelPowers.add(getJComboBoxPowerIcon(), new Constraints(new Leading(87, 12, 12), new Leading(278, 12, 12)));
 			jPanelPowers.add(getJCheckBoxPowerAura(), new Constraints(new Leading(200, 12, 12), new Leading(278, 12, 12)));
 			jPanelPowers.add(getJScrollPanePowerDescription(), new Constraints(new Bilateral(0, 0, 31), new Bilateral(301, 0, 47)));
 			jPanelPowers.add(getJTextFieldPowerName(), new Constraints(new Bilateral(87, 0, 235), new Leading(189, 12, 12)));
 			jPanelPowers.add(getJTextFieldPowerAction(), new Constraints(new Bilateral(87, 0, 235), new Leading(211, 12, 12)));
 			jPanelPowers.add(getJTextFieldPowerKeywords(), new Constraints(new Bilateral(87, 0, 235), new Leading(233, 12, 12)));
 			jPanelPowers.add(getJTextFieldPowerURL(), new Constraints(new Bilateral(87, 0, 235), new Leading(256, 12, 12)));
 			jPanelPowers.add(getJFormattedTextFieldAuraSize(), new Constraints(new Leading(260, 23, 10, 10), new Leading(280, 53, 48)));
 		}
 		return jPanelPowers;
 	}
 
 	private JScrollPane getJScrollPaneNotes() {
 		if (jScrollPaneNotes == null) {
 			jScrollPaneNotes = new JScrollPane();
 			jScrollPaneNotes.setViewportView(getJTextPaneNotes());
 		}
 		return jScrollPaneNotes;
 	}
 
 	private JTextPane getJTextPaneNotes() {
 		if (jTextPaneNotes == null) {
 			jTextPaneNotes = new JTextPane();
 		}
 		return jTextPaneNotes;
 	}
 
 	private JScrollPane getJScrollPaneEffects() {
 		if (jScrollPaneEffects == null) {
 			jScrollPaneEffects = new JScrollPane();
 			jScrollPaneEffects.setViewportView(getJListEffects());
 		}
 		return jScrollPaneEffects;
 	}
 
 	private JList getJListEffects() {
 		if (jListEffects == null) {
 			jListEffects = new JList();
 			DefaultListModel listModel = new DefaultListModel();
 			jListEffects.setModel(listModel);
 			jListEffects.setCellRenderer(new EffectBaseCellRenderer());
 			jListEffects.addListSelectionListener(new ListSelectionListener() {
 	
 				public void valueChanged(ListSelectionEvent event) {
 					jListEffectsListSelectionValueChanged(event);
 				}
 			});
 		}
 		return jListEffects;
 	}
 
 	private JButton getJButtonDelete() {
 		if (jButtonDelete == null) {
 			jButtonDelete = new JButton();
 			jButtonDelete.setText("Delete");
 			jButtonDelete.setEnabled(false);
 			jButtonDelete.addActionListener(new ActionListener() {
 	
 				public void actionPerformed(ActionEvent event) {
 					jButtonDeleteActionActionPerformed(event);
 				}
 			});
 		}
 		return jButtonDelete;
 	}
 
 	private JButton getJButtonImport() {
 		if (jButtonImport == null) {
 			jButtonImport = new JButton();
 			jButtonImport.setText("Import");
 			jButtonImport.addActionListener(new ActionListener() {
 	
 				public void actionPerformed(ActionEvent event) {
 					jButtonImportActionActionPerformed(event);
 				}
 			});
 		}
 		return jButtonImport;
 	}
 
 	private JButton getJButtonExport() {
 		if (jButtonExport == null) {
 			jButtonExport = new JButton();
 			jButtonExport.setText("Export");
 			jButtonExport.addActionListener(new ActionListener() {
 	
 				public void actionPerformed(ActionEvent event) {
 					jButtonExportActionActionPerformed(event);
 				}
 			});
 		}
 		return jButtonExport;
 	}
 
 	private JButton getJButtonEdit() {
 		if (jButtonEdit == null) {
 			jButtonEdit = new JButton();
 			jButtonEdit.setText("Edit");
 			jButtonEdit.setEnabled(false);
 		}
 		return jButtonEdit;
 	}
 
 	private JButton getJButtonAdd() {
 		if (jButtonAdd == null) {
 			jButtonAdd = new JButton();
 			jButtonAdd.setText("Add");
 			jButtonAdd.addActionListener(new ActionListener() {
 	
 				public void actionPerformed(ActionEvent event) {
 					jButtonAddActionActionPerformed(event);
 				}
 			});
 		}
 		return jButtonAdd;
 	}
 
 	private JCheckBox getJCheckBoxHidden() {
 		if (jCheckBoxHidden == null) {
 			jCheckBoxHidden = new JCheckBox();
 			jCheckBoxHidden.setText("Hidden");
 		}
 		return jCheckBoxHidden;
 	}
 
 	private JCheckBox getJCheckBoxBeneficial() {
 		if (jCheckBoxBeneficial == null) {
 			jCheckBoxBeneficial = new JCheckBox();
 			jCheckBoxBeneficial.setText("Beneficial");
 		}
 		return jCheckBoxBeneficial;
 	}
 
 	private JComboBox getJComboBoxDuration() {
 		if (jComboBoxDuration == null) {
 			jComboBoxDuration = new JComboBox();
 			jComboBoxDuration.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jComboBoxDuration.setModel(new DefaultComboBoxModel(new Object[] { "", "End of Source's Next Turn", "End of Target's Next Turn", "End of the Current Turn", "End of the Encounter", "Save Ends", "Special", "Start of Source's Next Turn", "Start of Target's Next Turn", "Sustained", "Permanent" }));
 			jComboBoxDuration.setDoubleBuffered(false);
 			jComboBoxDuration.setBorder(null);
 			jComboBoxDuration.setRequestFocusEnabled(false);
 		}
 		return jComboBoxDuration;
 	}
 
 	private JComboBox getJComboBoxEffect() {
 		if (jComboBoxEffect == null) {
 			jComboBoxEffect = new JComboBox();
 			jComboBoxEffect.setEditable(true);
 			jComboBoxEffect.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jComboBoxEffect.setModel(new DefaultComboBoxModel(new Object[] { "", "Attack Penalty", "Blinded", "Dazed", "Deafened", "Defense Penalty", "Dominated",
 					"Full Defense (+2 all def)", "Granting Combat Advantage", "Immobilized", "Marked", "Ongoing Damage", "Petrified", "Prone", "Regeneration",
 					"Resist", "Restrained", "Second Wind (+2 all def)", "Slowed", "Stunned", "Surprised", "Unconscious", "Vulnerability", "Weakened" }));
 			jComboBoxEffect.setDoubleBuffered(false);
 			jComboBoxEffect.setBorder(null);
 			jComboBoxEffect.setRequestFocusEnabled(false);
 			AutoCompletion.enable(jComboBoxEffect);
 		}
 		return jComboBoxEffect;
 	}
 
 	private JLabel getJLabelDuration() {
 		if (jLabelDuration == null) {
 			jLabelDuration = new JLabel();
 			jLabelDuration.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jLabelDuration.setHorizontalAlignment(SwingConstants.TRAILING);
 			jLabelDuration.setText("Duration");
 		}
 		return jLabelDuration;
 	}
 
 	private JLabel getJLabelEffect() {
 		if (jLabelEffect == null) {
 			jLabelEffect = new JLabel();
 			jLabelEffect.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jLabelEffect.setHorizontalAlignment(SwingConstants.TRAILING);
 			jLabelEffect.setText("Effect");
 		}
 		return jLabelEffect;
 	}
 
 	private JPanel getJPanelGeneratedEffects() {
 		if (jPanelGeneratedEffects == null) {
 			jPanelGeneratedEffects = new JPanel();
 			jPanelGeneratedEffects.setLayout(new GroupLayout());
 			jPanelGeneratedEffects.add(getJButtonEdit(), new Constraints(new Trailing(12, 76, 118, 346), new Leading(12, 12, 12)));
 			jPanelGeneratedEffects.add(getJButtonExport(), new Constraints(new Trailing(12, 76, 118, 346), new Leading(44, 12, 12)));
 			jPanelGeneratedEffects.add(getJButtonImport(), new Constraints(new Trailing(12, 76, 118, 346), new Leading(76, 12, 12)));
 			jPanelGeneratedEffects.add(getJButtonDelete(), new Constraints(new Trailing(12, 76, 118, 346), new Leading(108, 12, 12)));
 			jPanelGeneratedEffects.add(getJScrollPaneEffects(), new Constraints(new Leading(84, 250, 95, 95), new Leading(12, 243, 12, 12)));
 			jPanelGeneratedEffects.add(getJLabelEffect(), new Constraints(new Leading(12, 60, 12, 12), new Leading(265, 12, 12)));
 			jPanelGeneratedEffects.add(getJLabelDuration(), new Constraints(new Leading(12, 60, 12, 12), new Leading(294, 12, 12)));
 			jPanelGeneratedEffects.add(getJComboBoxEffect(), new Constraints(new Leading(84, 250, 95, 95), new Leading(261, 12, 12)));
 			jPanelGeneratedEffects.add(getJComboBoxDuration(), new Constraints(new Leading(84, 250, 95, 95), new Leading(290, 12, 12)));
 			jPanelGeneratedEffects.add(getJCheckBoxBeneficial(), new Constraints(new Leading(84, 8, 8), new Leading(317, 8, 8)));
 			jPanelGeneratedEffects.add(getJCheckBoxHidden(), new Constraints(new Leading(177, 8, 8), new Leading(317, 8, 8)));
 			jPanelGeneratedEffects.add(getJButtonAdd(), new Constraints(new Leading(270, 64, 12, 12), new Leading(317, 12, 12)));
 		}
 		return jPanelGeneratedEffects;
 	}
 
 	private JLabel getJLabelSource() {
 		if (jLabelSource == null) {
 			jLabelSource = new JLabel();
 			jLabelSource.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jLabelSource.setHorizontalAlignment(SwingConstants.TRAILING);
 			jLabelSource.setText("Source");
 		}
 		return jLabelSource;
 	}
 
 	private JLabel getJLabelEquipment() {
 		if (jLabelEquipment == null) {
 			jLabelEquipment = new JLabel();
 			jLabelEquipment.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jLabelEquipment.setHorizontalAlignment(SwingConstants.TRAILING);
 			jLabelEquipment.setText("Equipment");
 		}
 		return jLabelEquipment;
 	}
 
 	private JLabel getJLabelLanguages() {
 		if (jLabelLanguages == null) {
 			jLabelLanguages = new JLabel();
 			jLabelLanguages.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jLabelLanguages.setHorizontalAlignment(SwingConstants.TRAILING);
 			jLabelLanguages.setText("Languages");
 		}
 		return jLabelLanguages;
 	}
 
 	private JLabel getJLabelFeats() {
 		if (jLabelFeats == null) {
 			jLabelFeats = new JLabel();
 			jLabelFeats.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jLabelFeats.setHorizontalAlignment(SwingConstants.TRAILING);
 			jLabelFeats.setText("Feats");
 		}
 		return jLabelFeats;
 	}
 
 	private JLabel getJLabelSkills() {
 		if (jLabelSkills == null) {
 			jLabelSkills = new JLabel();
 			jLabelSkills.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jLabelSkills.setHorizontalAlignment(SwingConstants.TRAILING);
 			jLabelSkills.setText("Skills");
 		}
 		return jLabelSkills;
 	}
 
 	private JLabel getJLabelAlignment() {
 		if (jLabelAlignment == null) {
 			jLabelAlignment = new JLabel();
 			jLabelAlignment.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jLabelAlignment.setHorizontalAlignment(SwingConstants.TRAILING);
 			jLabelAlignment.setText("Alignment");
 		}
 		return jLabelAlignment;
 	}
 
 	private JTextField getJTextFieldSource() {
 		if (jTextFieldSource == null) {
 			jTextFieldSource = new JTextField();
 		}
 		return jTextFieldSource;
 	}
 
 	private JTextField getJTextFieldEquipment() {
 		if (jTextFieldEquipment == null) {
 			jTextFieldEquipment = new JTextField();
 		}
 		return jTextFieldEquipment;
 	}
 
 	private JTextField getJTextFieldLanguages() {
 		if (jTextFieldLanguages == null) {
 			jTextFieldLanguages = new JTextField();
 		}
 		return jTextFieldLanguages;
 	}
 
 	private JTextField getJTextFieldFeats() {
 		if (jTextFieldFeats == null) {
 			jTextFieldFeats = new JTextField();
 		}
 		return jTextFieldFeats;
 	}
 
 	private JTextField getJTextFieldSkills() {
 		if (jTextFieldSkills == null) {
 			jTextFieldSkills = new JTextField();
 		}
 		return jTextFieldSkills;
 	}
 
 	private JTextField getJTextFieldAlignment() {
 		if (jTextFieldAlignment == null) {
 			jTextFieldAlignment = new JTextField();
 		}
 		return jTextFieldAlignment;
 	}
 
 	private JFormattedTextField getJFormattedTextFieldSaveBonus() {
 		if (jFormattedTextFieldSaveBonus == null) {
 			jFormattedTextFieldSaveBonus = new JFormattedTextField(NumberFormat.getInstance());
 			jFormattedTextFieldSaveBonus.setText("0");
 		}
 		return jFormattedTextFieldSaveBonus;
 	}
 
 	private JLabel getJLabelSaveBonus() {
 		if (jLabelSaveBonus == null) {
 			jLabelSaveBonus = new JLabel();
 			jLabelSaveBonus.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jLabelSaveBonus.setHorizontalAlignment(SwingConstants.TRAILING);
 			jLabelSaveBonus.setText("Save Bonus");
 		}
 		return jLabelSaveBonus;
 	}
 
 	private JFormattedTextField getJFormattedTextFieldPowerPts() {
 		if (jFormattedTextFieldPowerPts == null) {
 			jFormattedTextFieldPowerPts = new JFormattedTextField(NumberFormat.getInstance());
 			jFormattedTextFieldPowerPts.setText("0");
 		}
 		return jFormattedTextFieldPowerPts;
 	}
 
 	private JLabel getJLabelPowerPts() {
 		if (jLabelPowerPts == null) {
 			jLabelPowerPts = new JLabel();
 			jLabelPowerPts.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jLabelPowerPts.setHorizontalAlignment(SwingConstants.TRAILING);
 			jLabelPowerPts.setText("Power Pts");
 		}
 		return jLabelPowerPts;
 	}
 
 	private JFormattedTextField getJFormattedTextFieldActionPts() {
 		if (jFormattedTextFieldActionPts == null) {
 			jFormattedTextFieldActionPts = new JFormattedTextField(NumberFormat.getInstance());
 			jFormattedTextFieldActionPts.setText("0");
 		}
 		return jFormattedTextFieldActionPts;
 	}
 
 	private JLabel getJLabelActionPts() {
 		if (jLabelActionPts == null) {
 			jLabelActionPts = new JLabel();
 			jLabelActionPts.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jLabelActionPts.setHorizontalAlignment(SwingConstants.TRAILING);
 			jLabelActionPts.setText("Action Pts");
 		}
 		return jLabelActionPts;
 	}
 
 	private JLabel getJLabelInitiative() {
 		if (jLabelInitiative == null) {
 			jLabelInitiative = new JLabel();
 			jLabelInitiative.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jLabelInitiative.setHorizontalAlignment(SwingConstants.TRAILING);
 			jLabelInitiative.setText("Initiative");
 		}
 		return jLabelInitiative;
 	}
 
 	private JFormattedTextField getJFormattedTextFieldInitiative() {
 		if (jFormattedTextFieldInitiative == null) {
 			jFormattedTextFieldInitiative = new JFormattedTextField(NumberFormat.getInstance());
 			jFormattedTextFieldInitiative.setText("0");
 		}
 		return jFormattedTextFieldInitiative;
 	}
 
 	private JLabel getJLabelSpeed() {
 		if (jLabelSpeed == null) {
 			jLabelSpeed = new JLabel();
 			jLabelSpeed.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jLabelSpeed.setHorizontalAlignment(SwingConstants.TRAILING);
 			jLabelSpeed.setText("Speed");
 		}
 		return jLabelSpeed;
 	}
 
 	private JLabel getJLabelRegen() {
 		if (jLabelRegen == null) {
 			jLabelRegen = new JLabel();
 			jLabelRegen.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jLabelRegen.setHorizontalAlignment(SwingConstants.TRAILING);
 			jLabelRegen.setText("Regen");
 		}
 		return jLabelRegen;
 	}
 
 	private JLabel getJLabelVulnerable() {
 		if (jLabelVulnerable == null) {
 			jLabelVulnerable = new JLabel();
 			jLabelVulnerable.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jLabelVulnerable.setHorizontalAlignment(SwingConstants.TRAILING);
 			jLabelVulnerable.setText("Vulnerable");
 		}
 		return jLabelVulnerable;
 	}
 
 	private JLabel getJLabelResist() {
 		if (jLabelResist == null) {
 			jLabelResist = new JLabel();
 			jLabelResist.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jLabelResist.setHorizontalAlignment(SwingConstants.TRAILING);
 			jLabelResist.setText("Resist");
 		}
 		return jLabelResist;
 	}
 
 	private JLabel getJLabelImmune() {
 		if (jLabelImmune == null) {
 			jLabelImmune = new JLabel();
 			jLabelImmune.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jLabelImmune.setHorizontalAlignment(SwingConstants.TRAILING);
 			jLabelImmune.setText("Immune");
 		}
 		return jLabelImmune;
 	}
 
 	private JTextField getJTextFieldSpeed() {
 		if (jTextFieldSpeed == null) {
 			jTextFieldSpeed = new JTextField();
 		}
 		return jTextFieldSpeed;
 	}
 
 	private JTextField getJTextFieldRegen() {
 		if (jTextFieldRegen == null) {
 			jTextFieldRegen = new JTextField();
 		}
 		return jTextFieldRegen;
 	}
 
 	private JTextField getJTextFieldVulnerable() {
 		if (jTextFieldVulnerable == null) {
 			jTextFieldVulnerable = new JTextField();
 		}
 		return jTextFieldVulnerable;
 	}
 
 	private JTextField getJTextFieldResist() {
 		if (jTextFieldResist == null) {
 			jTextFieldResist = new JTextField();
 		}
 		return jTextFieldResist;
 	}
 
 	private JTextField getJTextFieldImmune() {
 		if (jTextFieldImmune == null) {
 			jTextFieldImmune = new JTextField();
 		}
 		return jTextFieldImmune;
 	}
 
 	private JTextField getJTextFieldSenses() {
 		if (jTextFieldSenses == null) {
 			jTextFieldSenses = new JTextField();
 		}
 		return jTextFieldSenses;
 	}
 
 	private JLabel getJLabelSenses() {
 		if (jLabelSenses == null) {
 			jLabelSenses = new JLabel();
 			jLabelSenses.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jLabelSenses.setHorizontalAlignment(SwingConstants.TRAILING);
 			jLabelSenses.setText("Senses");
 		}
 		return jLabelSenses;
 	}
 
 	private JPanel getJPanelTraits() {
 		if (jPanelTraits == null) {
 			jPanelTraits = new JPanel();
 			jPanelTraits.setLayout(new GroupLayout());
 			jPanelTraits.add(getJLabelSenses(), new Constraints(new Leading(4, 65, 28, 28), new Leading(4, 10, 10)));
 			jPanelTraits.add(getJLabelImmune(), new Constraints(new Leading(4, 65, 28, 28), new Leading(26, 10, 10)));
 			jPanelTraits.add(getJLabelResist(), new Constraints(new Leading(4, 65, 28, 28), new Leading(48, 10, 10)));
 			jPanelTraits.add(getJLabelVulnerable(), new Constraints(new Leading(4, 65, 28, 28), new Leading(70, 10, 10)));
 			jPanelTraits.add(getJLabelRegen(), new Constraints(new Leading(4, 65, 28, 28), new Leading(91, 10, 10)));
 			jPanelTraits.add(getJLabelSpeed(), new Constraints(new Leading(4, 65, 28, 28), new Leading(113, 10, 10)));
 			jPanelTraits.add(getJLabelInitiative(), new Constraints(new Leading(4, 65, 28, 28), new Leading(134, 10, 10)));
 			jPanelTraits.add(getJLabelActionPts(), new Constraints(new Leading(100, 65, 28, 28), new Leading(134, 10, 10)));
 			jPanelTraits.add(getJLabelPowerPts(), new Constraints(new Leading(200, 65, 28, 28), new Leading(134, 10, 10)));
 			jPanelTraits.add(getJLabelSaveBonus(), new Constraints(new Leading(300, 70, 28, 28), new Leading(134, 10, 10)));
 			jPanelTraits.add(getJLabelAlignment(), new Constraints(new Leading(4, 65, 28, 28), new Leading(156, 10, 10)));
 			jPanelTraits.add(getJLabelSkills(), new Constraints(new Leading(4, 65, 28, 28), new Leading(179, 10, 10)));
 			jPanelTraits.add(getJLabelFeats(), new Constraints(new Leading(4, 65, 28, 28), new Leading(202, 10, 10)));
 			jPanelTraits.add(getJLabelLanguages(), new Constraints(new Leading(4, 65, 28, 28), new Leading(225, 10, 10)));
 			jPanelTraits.add(getJLabelEquipment(), new Constraints(new Leading(4, 65, 28, 28), new Leading(250, 10, 10)));
 			jPanelTraits.add(getJLabelSource(), new Constraints(new Leading(4, 65, 28, 28), new Leading(272, 10, 10)));
 			jPanelTraits.add(getJTextFieldSenses(), new Constraints(new Bilateral(76, 12, 4), new Leading(4, 10, 10)));
 			jPanelTraits.add(getJTextFieldImmune(), new Constraints(new Bilateral(76, 12, 4), new Leading(26, 10, 10)));
 			jPanelTraits.add(getJTextFieldResist(), new Constraints(new Bilateral(76, 12, 4), new Leading(48, 10, 10)));
 			jPanelTraits.add(getJTextFieldVulnerable(), new Constraints(new Bilateral(76, 12, 4), new Leading(70, 10, 10)));
 			jPanelTraits.add(getJTextFieldRegen(), new Constraints(new Bilateral(76, 12, 4), new Leading(91, 10, 10)));
 			jPanelTraits.add(getJTextFieldSpeed(), new Constraints(new Bilateral(76, 12, 4), new Leading(112, 10, 10)));
 			jPanelTraits.add(getJFormattedTextFieldInitiative(), new Constraints(new Leading(76, 23, 10, 10), new Leading(133, 10, 10)));
 			jPanelTraits.add(getJFormattedTextFieldActionPts(), new Constraints(new Leading(170, 23, 10, 10), new Leading(133, 10, 10)));
 			jPanelTraits.add(getJFormattedTextFieldPowerPts(), new Constraints(new Leading(270, 23, 10, 10), new Leading(133, 10, 10)));
 			jPanelTraits.add(getJFormattedTextFieldSaveBonus(), new Constraints(new Leading(375, 23, 10, 10), new Leading(133, 10, 10)));
 			jPanelTraits.add(getJTextFieldAlignment(), new Constraints(new Bilateral(76, 12, 4), new Leading(155, 10, 10)));
 			jPanelTraits.add(getJTextFieldSkills(), new Constraints(new Bilateral(76, 12, 4), new Leading(178, 10, 10)));
 			jPanelTraits.add(getJTextFieldFeats(), new Constraints(new Bilateral(76, 12, 4), new Leading(201, 10, 10)));
 			jPanelTraits.add(getJTextFieldLanguages(), new Constraints(new Bilateral(76, 12, 4), new Leading(225, 10, 10)));
 			jPanelTraits.add(getJTextFieldEquipment(), new Constraints(new Bilateral(76, 12, 4), new Leading(248, 10, 10)));
 			jPanelTraits.add(getJTextFieldSource(), new Constraints(new Bilateral(76, 12, 4), new Leading(270, 10, 10)));
 		}
 		return jPanelTraits;
 	}
 
 	private JTabbedPane getJTabbedPaneTraits() {
 		if (jTabbedPaneTraits == null) {
 			jTabbedPaneTraits = new JTabbedPane();
 			jTabbedPaneTraits.addTab("Traits", getJPanelTraits());
 			jTabbedPaneTraits.addTab("Generated Effects", getJPanelGeneratedEffects());
 			jTabbedPaneTraits.addTab("Notes", getJScrollPaneNotes());
 		}
 		return jTabbedPaneTraits;
 	}
 
 	private JFormattedTextField getJFormattedTextFieldSurges() {
 		if (jFormattedTextFieldSurges == null) {
 			jFormattedTextFieldSurges = new JFormattedTextField(NumberFormat.getInstance());
 			jFormattedTextFieldSurges.setText("0");
 		}
 		return jFormattedTextFieldSurges;
 	}
 
 	private JFormattedTextField getJFormattedTextFieldMax() {
 		if (jFormattedTextFieldMax == null) {
 			jFormattedTextFieldMax = new JFormattedTextField(NumberFormat.getInstance());
 			jFormattedTextFieldMax.setText("0");
 		}
 		return jFormattedTextFieldMax;
 	}
 
 	private JLabel getJLabelSurges() {
 		if (jLabelSurges == null) {
 			jLabelSurges = new JLabel();
 			jLabelSurges.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jLabelSurges.setText("Surges");
 		}
 		return jLabelSurges;
 	}
 
 	private JLabel getJLabelMax() {
 		if (jLabelMax == null) {
 			jLabelMax = new JLabel();
 			jLabelMax.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jLabelMax.setText("Max");
 		}
 		return jLabelMax;
 	}
 
 	private JPanel getJPanelHitPoints() {
 		if (jPanelHitPoints == null) {
 			jPanelHitPoints = new JPanel();
 			jPanelHitPoints.setBorder(BorderFactory.createTitledBorder(null, "Hit Points", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, new Font("Dialog",
 					Font.PLAIN, 12), new Color(51, 51, 51)));
 			jPanelHitPoints.setLayout(new GroupLayout());
 			jPanelHitPoints.add(getJLabelMax(), new Constraints(new Leading(0, 16, 23), new Leading(4, 12, 12)));
 			jPanelHitPoints.add(getJFormattedTextFieldMax(), new Constraints(new Leading(24, 45, 10, 10), new Leading(4, 12, 12)));
 			jPanelHitPoints.add(getJLabelSurges(), new Constraints(new Leading(72, 42, 10, 10), new Leading(4, 12, 12)));
 			jPanelHitPoints.add(getJFormattedTextFieldSurges(), new Constraints(new Leading(114, 30, 10, 10), new Leading(4, 12, 12)));
 		}
 		return jPanelHitPoints;
 	}
 
 	private JFormattedTextField getJFormattedTextFieldXPValue() {
 		if (jFormattedTextFieldXPValue == null) {
 			jFormattedTextFieldXPValue = new JFormattedTextField(NumberFormat.getInstance());
 			jFormattedTextFieldXPValue.setText("0");
 		}
 		return jFormattedTextFieldXPValue;
 	}
 
 	private JPanel getJPanelXPValue() {
 		if (jPanelXPValue == null) {
 			jPanelXPValue = new JPanel();
 			jPanelXPValue.setBorder(BorderFactory.createTitledBorder(null, "XP Value", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, new Font("Dialog",
 					Font.PLAIN, 12), new Color(51, 51, 51)));
 			jPanelXPValue.setLayout(new GroupLayout());
 			jPanelXPValue.add(getJFormattedTextFieldXPValue(), new Constraints(new Bilateral(0, 0, 4), new Leading(0, 12, 12)));
 		}
 		return jPanelXPValue;
 	}
 
 	private JFormattedTextField getJFormattedTextFieldCha() {
 		if (jFormattedTextFieldCha == null) {
 			jFormattedTextFieldCha = new JFormattedTextField(NumberFormat.getInstance());
 			jFormattedTextFieldCha.setText("0");
 		}
 		return jFormattedTextFieldCha;
 	}
 
 	private JFormattedTextField getJFormattedTextFieldInt() {
 		if (jFormattedTextFieldInt == null) {
 			jFormattedTextFieldInt = new JFormattedTextField(NumberFormat.getInstance());
 			jFormattedTextFieldInt.setText("0");
 		}
 		return jFormattedTextFieldInt;
 	}
 
 	private JFormattedTextField getJFormattedTextFieldCon() {
 		if (jFormattedTextFieldCon == null) {
 			jFormattedTextFieldCon = new JFormattedTextField(NumberFormat.getInstance());
 			jFormattedTextFieldCon.setText("0");
 		}
 		return jFormattedTextFieldCon;
 	}
 
 	private JLabel getJLabelCha() {
 		if (jLabelCha == null) {
 			jLabelCha = new JLabel();
 			jLabelCha.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jLabelCha.setHorizontalAlignment(SwingConstants.TRAILING);
 			jLabelCha.setText("Cha");
 		}
 		return jLabelCha;
 	}
 
 	private JLabel getJLabelInt() {
 		if (jLabelInt == null) {
 			jLabelInt = new JLabel();
 			jLabelInt.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jLabelInt.setHorizontalAlignment(SwingConstants.TRAILING);
 			jLabelInt.setText("Int");
 		}
 		return jLabelInt;
 	}
 
 	private JLabel getJLabelCon() {
 		if (jLabelCon == null) {
 			jLabelCon = new JLabel();
 			jLabelCon.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jLabelCon.setHorizontalAlignment(SwingConstants.TRAILING);
 			jLabelCon.setText("Con");
 		}
 		return jLabelCon;
 	}
 
 	private JFormattedTextField getJFormattedTextFieldWis() {
 		if (jFormattedTextFieldWis == null) {
 			jFormattedTextFieldWis = new JFormattedTextField(NumberFormat.getInstance());
 			jFormattedTextFieldWis.setText("0");
 		}
 		return jFormattedTextFieldWis;
 	}
 
 	private JFormattedTextField getJFormattedTextFieldDex() {
 		if (jFormattedTextFieldDex == null) {
 			jFormattedTextFieldDex = new JFormattedTextField(NumberFormat.getInstance());
 			jFormattedTextFieldDex.setText("0");
 		}
 		return jFormattedTextFieldDex;
 	}
 
 	private JFormattedTextField getJFormattedTextFieldStr() {
 		if (jFormattedTextFieldStr == null) {
 			jFormattedTextFieldStr = new JFormattedTextField(NumberFormat.getInstance());
 			jFormattedTextFieldStr.setText("0");
 		}
 		return jFormattedTextFieldStr;
 	}
 
 	private JLabel getJLabelDex() {
 		if (jLabelDex == null) {
 			jLabelDex = new JLabel();
 			jLabelDex.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jLabelDex.setHorizontalAlignment(SwingConstants.TRAILING);
 			jLabelDex.setText("Dex");
 		}
 		return jLabelDex;
 	}
 
 	private JLabel getJLabelWis() {
 		if (jLabelWis == null) {
 			jLabelWis = new JLabel();
 			jLabelWis.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jLabelWis.setHorizontalAlignment(SwingConstants.TRAILING);
 			jLabelWis.setText("Wis");
 		}
 		return jLabelWis;
 	}
 
 	private JLabel getJLabelStr() {
 		if (jLabelStr == null) {
 			jLabelStr = new JLabel();
 			jLabelStr.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jLabelStr.setHorizontalAlignment(SwingConstants.TRAILING);
 			jLabelStr.setText("Str");
 		}
 		return jLabelStr;
 	}
 
 	private JPanel getJPanelAttributes() {
 		if (jPanelAttributes == null) {
 			jPanelAttributes = new JPanel();
 			jPanelAttributes.setBorder(BorderFactory.createTitledBorder(null, "Attributes", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, new Font("Dialog",
 					Font.PLAIN, 12), new Color(51, 51, 51)));
 			jPanelAttributes.setLayout(new GroupLayout());
 			jPanelAttributes.add(getJLabelStr(), new Constraints(new Leading(0, 24, 12, 12), new Leading(0, 12, 12)));
 			jPanelAttributes.add(getJLabelDex(), new Constraints(new Leading(0, 24, 12, 12), new Leading(34, 12, 12)));
 			jPanelAttributes.add(getJLabelWis(), new Constraints(new Leading(0, 24, 12, 12), new Leading(68, 12, 12)));
 			jPanelAttributes.add(getJFormattedTextFieldStr(), new Constraints(new Leading(36, 33, 12, 12), new Leading(0, 12, 12)));
 			jPanelAttributes.add(getJFormattedTextFieldDex(), new Constraints(new Leading(36, 33, 12, 12), new Leading(34, 12, 12)));
 			jPanelAttributes.add(getJFormattedTextFieldWis(), new Constraints(new Leading(36, 33, 12, 12), new Leading(68, 12, 12)));
 			jPanelAttributes.add(getJLabelCon(), new Constraints(new Leading(81, 24, 12, 12), new Leading(0, 12, 12)));
 			jPanelAttributes.add(getJLabelInt(), new Constraints(new Leading(81, 24, 12, 12), new Leading(34, 12, 12)));
 			jPanelAttributes.add(getJLabelCha(), new Constraints(new Leading(81, 24, 12, 12), new Leading(68, 12, 12)));
 			jPanelAttributes.add(getJFormattedTextFieldCon(), new Constraints(new Leading(117, 33, 12, 12), new Leading(0, 12, 12)));
 			jPanelAttributes.add(getJFormattedTextFieldInt(), new Constraints(new Leading(117, 33, 12, 12), new Leading(34, 12, 12)));
 			jPanelAttributes.add(getJFormattedTextFieldCha(), new Constraints(new Leading(117, 33, 12, 12), new Leading(68, 12, 12)));
 		}
 		return jPanelAttributes;
 	}
 
 	private JFormattedTextField getJFormattedTextFieldWill() {
 		if (jFormattedTextFieldWill == null) {
 			jFormattedTextFieldWill = new JFormattedTextField(NumberFormat.getInstance());
 			jFormattedTextFieldWill.setText("0");
 		}
 		return jFormattedTextFieldWill;
 	}
 
 	private JFormattedTextField getJFormattedTextFieldReflex() {
 		if (jFormattedTextFieldReflex == null) {
 			jFormattedTextFieldReflex = new JFormattedTextField(NumberFormat.getInstance());
 			jFormattedTextFieldReflex.setText("0");
 		}
 		return jFormattedTextFieldReflex;
 	}
 
 	private JFormattedTextField getJFormattedTextFieldFortitude() {
 		if (jFormattedTextFieldFortitude == null) {
 			jFormattedTextFieldFortitude = new JFormattedTextField(NumberFormat.getInstance());
 			jFormattedTextFieldFortitude.setText("0");
 		}
 		return jFormattedTextFieldFortitude;
 	}
 
 	private JFormattedTextField getJFormattedTextFieldAC() {
 		if (jFormattedTextFieldAC == null) {
 			jFormattedTextFieldAC = new JFormattedTextField(NumberFormat.getInstance());
 			jFormattedTextFieldAC.setText("0");
 		}
 		return jFormattedTextFieldAC;
 	}
 
 	private JLabel getJLabelWill() {
 		if (jLabelWill == null) {
 			jLabelWill = new JLabel();
 			jLabelWill.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jLabelWill.setHorizontalAlignment(SwingConstants.TRAILING);
 			jLabelWill.setText("Will");
 		}
 		return jLabelWill;
 	}
 
 	private JLabel getJLabelReflex() {
 		if (jLabelReflex == null) {
 			jLabelReflex = new JLabel();
 			jLabelReflex.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jLabelReflex.setHorizontalAlignment(SwingConstants.TRAILING);
 			jLabelReflex.setText("Reflex");
 		}
 		return jLabelReflex;
 	}
 
 	private JLabel getJLabelFortitude() {
 		if (jLabelFortitude == null) {
 			jLabelFortitude = new JLabel();
 			jLabelFortitude.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jLabelFortitude.setHorizontalAlignment(SwingConstants.TRAILING);
 			jLabelFortitude.setText("Fortitude");
 		}
 		return jLabelFortitude;
 	}
 
 	private JLabel getJLabelAC() {
 		if (jLabelAC == null) {
 			jLabelAC = new JLabel();
 			jLabelAC.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jLabelAC.setHorizontalAlignment(SwingConstants.TRAILING);
 			jLabelAC.setText("AC");
 		}
 		return jLabelAC;
 	}
 
 	private JPanel getJPanelDefenses() {
 		if (jPanelDefenses == null) {
 			jPanelDefenses = new JPanel();
 			jPanelDefenses.setBorder(BorderFactory.createTitledBorder(null, "Defenses", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, new Font("Dialog",
 					Font.PLAIN, 12), new Color(51, 51, 51)));
 			jPanelDefenses.setLayout(new GroupLayout());
 			jPanelDefenses.add(getJLabelAC(), new Constraints(new Leading(12, 48, 12, 12), new Leading(0, 12, 12)));
 			jPanelDefenses.add(getJLabelFortitude(), new Constraints(new Leading(12, 48, 12, 12), new Leading(22, 12, 12)));
 			jPanelDefenses.add(getJLabelReflex(), new Constraints(new Leading(12, 48, 12, 12), new Leading(44, 12, 12)));
 			jPanelDefenses.add(getJLabelWill(), new Constraints(new Leading(12, 48, 12, 12), new Leading(66, 12, 12)));
 			jPanelDefenses.add(getJFormattedTextFieldAC(), new Constraints(new Leading(72, 33, 10, 10), new Leading(0, 12, 12)));
 			jPanelDefenses.add(getJFormattedTextFieldFortitude(), new Constraints(new Leading(72, 33, 10, 10), new Leading(22, 12, 12)));
 			jPanelDefenses.add(getJFormattedTextFieldReflex(), new Constraints(new Leading(72, 33, 10, 10), new Leading(44, 12, 12)));
 			jPanelDefenses.add(getJFormattedTextFieldWill(), new Constraints(new Leading(72, 33, 10, 10), new Leading(66, 12, 12)));
 		}
 		return jPanelDefenses;
 	}
 
 	private JTextField getJTextFieldTypeKeywords() {
 		if (jTextFieldTypeKeywords == null) {
 			jTextFieldTypeKeywords = new JTextField();
 		}
 		return jTextFieldTypeKeywords;
 	}
 
 	private JCheckBox getJCheckBoxPC() {
 		if (jCheckBoxPC == null) {
 			jCheckBoxPC = new JCheckBox();
 			jCheckBoxPC.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jCheckBoxPC.setText("PC");
 			jCheckBoxPC.addChangeListener(new ChangeListener() {
 	
 				public void stateChanged(ChangeEvent event) {
 					jCheckBoxPCChangeStateChanged(event);
 				}
 			});
 		}
 		return jCheckBoxPC;
 	}
 
 	private JCheckBox getJCheckBoxLeader() {
 		if (jCheckBoxLeader == null) {
 			jCheckBoxLeader = new JCheckBox();
 			jCheckBoxLeader.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jCheckBoxLeader.setText("Leader");
 		}
 		return jCheckBoxLeader;
 	}
 
 	private JComboBox getJComboBoxRole() {
 		if (jComboBoxRole == null) {
 			jComboBoxRole = new JComboBox();
 			jComboBoxRole.setModel(new DefaultComboBoxModel(new Object[] { "", "Artillery", "Blaster", "Brute", "Controller", "Lurker", "Minion", "Obstacle", "Puzzle", "Skirmisher", "Soldier", "Warder", "Hero", "Companion" }));
 			jComboBoxRole.setDoubleBuffered(false);
 			jComboBoxRole.setBorder(null);
 		}
 		return jComboBoxRole;
 	}
 
 	private JComboBox getJComboBoxRole2() {
 		if (jComboBoxRole2 == null) {
 			jComboBoxRole2 = new JComboBox();
 			jComboBoxRole2.setModel(new DefaultComboBoxModel(new Object[] { "", "Elite", "Solo" }));
 			jComboBoxRole2.setBorder(null);
 			jComboBoxRole2.setDoubleBuffered(false);
 			jComboBoxRole2.addItemListener(new ItemListener() {
 	
 				public void itemStateChanged(ItemEvent event) {
 					jComboBoxRoleItemItemStateChanged(event);
 				}
 			});
 		}
 		return jComboBoxRole2;
 	}
 
 	private JLabel getJLabelRole() {
 		if (jLabelRole == null) {
 			jLabelRole = new JLabel();
 			jLabelRole.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jLabelRole.setHorizontalAlignment(SwingConstants.TRAILING);
 			jLabelRole.setText("Role");
 		}
 		return jLabelRole;
 	}
 
 	private JSpinner getJSpinnerLevel() {
 		if (jSpinnerLevel == null) {
 			jSpinnerLevel = new JSpinner();
 		}
 		return jSpinnerLevel;
 	}
 
 	private JLabel getJLabelTypeKeywords() {
 		if (jLabelTypeKeywords == null) {
 			jLabelTypeKeywords = new JLabel();
 			jLabelTypeKeywords.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jLabelTypeKeywords.setText("Type/Keywords");
 		}
 		return jLabelTypeKeywords;
 	}
 
 	private JLabel getJLabelLevel() {
 		if (jLabelLevel == null) {
 			jLabelLevel = new JLabel();
 			jLabelLevel.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jLabelLevel.setHorizontalAlignment(SwingConstants.TRAILING);
 			jLabelLevel.setText("Level");
 		}
 		return jLabelLevel;
 	}
 
 	private JPanel getJPanelDescription() {
 		if (jPanelDescription == null) {
 			jPanelDescription = new JPanel();
 			jPanelDescription.setBorder(BorderFactory.createTitledBorder(null, "Description", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, new Font(
 					"Dialog", Font.PLAIN, 12), new Color(51, 51, 51)));
 			jPanelDescription.setLayout(new GroupLayout());
 			jPanelDescription.add(getJLabelLevel(), new Constraints(new Leading(12, 32, 79, 79), new Leading(0, 12, 12)));
 			jPanelDescription.add(getJLabelTypeKeywords(), new Constraints(new Leading(120, 79, 79), new Leading(0, 12, 12)));
 			jPanelDescription.add(getJLabelRole(), new Constraints(new Leading(12, 32, 62, 62), new Leading(24, 12, 12)));
 			jPanelDescription.add(getJSpinnerLevel(), new Constraints(new Leading(56, 40, 274, 274), new Leading(0, 12, 12)));
 			jPanelDescription.add(getJTextFieldTypeKeywords(), new Constraints(new Leading(211, 140, 78, 78), new Leading(0, 12, 12)));
 			jPanelDescription.add(getJComboBoxRole2(), new Constraints(new Leading(56, 80, 56, 56), new Leading(21, 20, 12, 12)));
 			jPanelDescription.add(getJComboBoxRole(), new Constraints(new Leading(142, 208, 56, 56), new Leading(21, 20, 12, 12)));
 			jPanelDescription.add(getJCheckBoxLeader(), new Constraints(new Trailing(8, 68, 10, 10), new Leading(0, 17, 8, 8)));
 			jPanelDescription.add(getJCheckBoxPC(), new Constraints(new Trailing(8, 68, 245, 299), new Leading(21, 20, 8, 8)));
 		}
 		return jPanelDescription;
 	}
 
 	private JTextField getJTextFieldName() {
 		if (jTextFieldName == null) {
 			jTextFieldName = new JTextField();
 		}
 		return jTextFieldName;
 	}
 
 	private JLabel getJLabelName() {
 		if (jLabelName == null) {
 			jLabelName = new JLabel();
 			jLabelName.setFont(new Font("Dialog", Font.PLAIN, 12));
 			jLabelName.setText("Name");
 		}
 		return jLabelName;
 	}
 
 	private Stats _stat = new Stats();
 	private List<Power> _statPowers = new ArrayList<Power>();
 	private SortedSet<EffectBase> _presetEffects = new TreeSet<EffectBase>();
 	private Boolean _powerChanged = false;
 	private JFormattedTextField jFormattedTextFieldAuraSize;
 	
 	/**
 	 * Creates a new form for editing the given statblock.
 	 * @param stat the statblock
 	 * @param c 
 	 */
 	public Statblock(Stats stat, JDialog c) {
 		super(c);
 		initComponents();
		setLocationRelativeTo(c);
 		
 		setStat(stat);
 		moveClassToFields(stat);
 	}
 	
 	/**
 	 * Returns the statblock for this form.
 	 * @return the statblock
 	 */
 	public Stats getStat() {
 		return _stat;
 	}
 
 	/**
 	 * Sets the statblock for this form.
 	 * @param stat the statblock
 	 */
 	private void setStat(Stats stat) {
 		_stat = stat;				
 	}
 
 	/**
 	 * Returns a list of the statblock's {@link Power}s.
 	 * @return the list
 	 */
 	private List<Power> getStatPowers() {
 		return _statPowers;
 	}
 
 	/**
 	 * Loads the statblock data into form fields.
 	 * @param stat the statblock
 	 */
 	private void moveClassToFields(Stats stat) {
 		getJTextFieldName().setText(stat.getName());
 		getJSpinnerLevel().setValue(stat.getLevel());
 		getJComboBoxRole().setSelectedItem(stat.getRole());
 		getJComboBoxRole2().setSelectedItem(stat.getRole2());
 		getJCheckBoxLeader().setSelected(stat.isLeader());
 		
 		getJTextFieldTypeKeywords().setText(stat.getType());
 		getJFormattedTextFieldXPValue().setText(stat.getXP().toString());
 		
 		getJTextFieldSenses().setText(stat.getSenses());
 		getJTextFieldSpeed().setText(stat.getSpeed());
 		
 		getJTextFieldImmune().setText(stat.getImmunity());
 		getJTextFieldResist().setText(stat.getResistance());
 		getJTextFieldVulnerable().setText(stat.getVulnerability());
 		getJTextFieldRegen().setText(stat.getRegen());
 		
 		getJFormattedTextFieldInitiative().setText(stat.getInit().toString());
 		getJFormattedTextFieldSaveBonus().setText(stat.getSaveBonus().toString());
 		getJFormattedTextFieldActionPts().setText(stat.getActionPoints().toString());
 		getJFormattedTextFieldPowerPts().setText(stat.getPowerPoints().toString());
 		getJFormattedTextFieldMax().setText(stat.getMaxHP().toString());
 		getJFormattedTextFieldSurges().setText(stat.getSurges().toString());
 		
 		getJFormattedTextFieldAC().setText(stat.getAC().toString());
 		getJFormattedTextFieldFortitude().setText(stat.getFort().toString());
 		getJFormattedTextFieldReflex().setText(stat.getRef().toString());
 		getJFormattedTextFieldWill().setText(stat.getWill().toString());
 		
 		getJFormattedTextFieldStr().setText(stat.getStr().toString());
 		getJFormattedTextFieldCon().setText(stat.getCon().toString());
 		getJFormattedTextFieldDex().setText(stat.getDex().toString());
 		getJFormattedTextFieldInt().setText(stat.getInt().toString());
 		getJFormattedTextFieldWis().setText(stat.getWis().toString());
 		getJFormattedTextFieldCha().setText(stat.getCha().toString());
 		
 	    getJTextFieldAlignment().setText(stat.getAlignment());
 	    getJTextFieldSkills().setText(stat.getSkills());
 	    getJTextFieldFeats().setText(stat.getFeats());
 	    getJTextFieldLanguages().setText(stat.getLanguages());
 	    getJTextFieldEquipment().setText(stat.getEquipment());
 	    getJTextFieldSource().setText(stat.getSource());
 	    getJTextPaneNotes().setText(stat.getNotes());
 	    
 	    getStatPowers().clear();
 	    for (Power pow : stat.getPowerList()) {
 	    	getStatPowers().add(pow);
 	    }
 	    resetPowerListFromArray();
 	    
 	    for (EffectBase eff : stat.getPresetEffects().values()) {
 	    	presetEffectAdd(new EffectBase(eff));
 	    }
 	    resetEffectListFromArray();
 	}
 
 	/**
 	 * Loads field data into Stats class.
 	 * @param stat the stats
 	 */
 	private void moveFieldsToClass(Stats stat) {
 	    stat.setName(getJTextFieldName().getText());
 	    stat.setLevel((Integer) getJSpinnerLevel().getValue());
 	    stat.setRole((String) getJComboBoxRole().getSelectedItem());
 	    stat.setRole2((String) getJComboBoxRole2().getSelectedItem());
 	    stat.setLeader(getJCheckBoxLeader().isSelected());
 	    stat.setType(getJTextFieldTypeKeywords().getText());
 	    stat.setXP(Integer.valueOf(getJFormattedTextFieldXPValue().getText().replace(",", "")));
 
 	    stat.setSenses(getJTextFieldSenses().getText());
 	    stat.setSpeed(getJTextFieldSpeed().getText());
 
 	    stat.setImmunity(getJTextFieldImmune().getText());
 	    stat.setResistance(getJTextFieldResist().getText());
 	    stat.setVulnerability(getJTextFieldVulnerable().getText());
 	    stat.setRegen(getJTextFieldRegen().getText());
 
 	    stat.setInit(Integer.valueOf(getJFormattedTextFieldInitiative().getText().replace(",", "")));
 	    stat.setSaveBonus(Integer.valueOf(getJFormattedTextFieldSaveBonus().getText().replace(",", "")));
 	    stat.setActionPoints(Integer.valueOf(getJFormattedTextFieldActionPts().getText()));
 	    stat.setPowerPoints(Integer.valueOf(getJFormattedTextFieldPowerPts().getText()));
 	    stat.setMaxHP(Integer.valueOf(getJFormattedTextFieldMax().getText().replace(",", "")));
 	    stat.setSurges(Integer.valueOf(getJFormattedTextFieldSurges().getText().replace(",", "")));
 
 	    stat.setAC(Integer.valueOf(getJFormattedTextFieldAC().getText().replace(",", "")));
 	    stat.setFort(Integer.valueOf(getJFormattedTextFieldFortitude().getText().replace(",", "")));
 	    stat.setRef(Integer.valueOf(getJFormattedTextFieldReflex().getText().replace(",", "")));
 	    stat.setWill(Integer.valueOf(getJFormattedTextFieldWill().getText().replace(",", "")));
 
 	    stat.setStr(Integer.valueOf(getJFormattedTextFieldStr().getText().replace(",", "")));
 	    stat.setCon(Integer.valueOf(getJFormattedTextFieldCon().getText().replace(",", "")));
 	    stat.setDex(Integer.valueOf(getJFormattedTextFieldDex().getText().replace(",", "")));
 	    stat.setInt(Integer.valueOf(getJFormattedTextFieldInt().getText().replace(",", "")));
 	    stat.setWis(Integer.valueOf(getJFormattedTextFieldWis().getText().replace(",", "")));
 	    stat.setCha(Integer.valueOf(getJFormattedTextFieldCha().getText().replace(",", "")));
 
 	    stat.setAlignment(getJTextFieldAlignment().getText());
 	    stat.setSkills(getJTextFieldSkills().getText());
 	    stat.setFeats(getJTextFieldFeats().getText());
 	    stat.setLanguages(getJTextFieldLanguages().getText());
 	    stat.setEquipment(getJTextFieldEquipment().getText());
 	    stat.setSource(getJTextFieldSource().getText());
 	    stat.setNotes(getJTextPaneNotes().getText());
 	    
 	    stat.getPowerList().clear();
 	    for (Power pow : getStatPowers()) {
 	    	if (!pow.getName().isEmpty()) {
 	    		stat.getPowerList().add(new Power(pow));
 	    	}
 	    }
 	    stat.getPresetEffects().clear();
 	    for (EffectBase eff : getPresetEffects().toArray(new EffectBase[0])) {
 	    	stat.presetEffectAdd(new EffectBase(eff));
 	    }
 	}
 
 	/**
 	 * Returns the preset effects stored in the form.
 	 * @return the preset effects
 	 */
 	private SortedSet<EffectBase> getPresetEffects() {
 		return _presetEffects;
 	}
 
 	/**
 	 * Event: PC checked/unchecked.
 	 * @param event
 	 */
 	private void jCheckBoxPCChangeStateChanged(ChangeEvent event) {
 		if(getJCheckBoxPC().isSelected()) {
 			getJComboBoxRole2().setSelectedItem("Hero");
 			getJComboBoxRole().setSelectedItem("");
 			getJCheckBoxLeader().setSelected(false);
 			
 			getJComboBoxRole().setEnabled(false);
 			getJCheckBoxLeader().setEnabled(false);
 		} else {
 			getJComboBoxRole().setEnabled(true);
 			getJCheckBoxLeader().setEnabled(true);
 			if (((String)getJComboBoxRole2().getSelectedItem()).contentEquals("Hero")) {
 				getJComboBoxRole2().setSelectedItem("");
 			}
 		}
 	}
 
 	/**
 	 * Event: Role changed.
 	 * @param event
 	 */
 	private void jComboBoxRoleItemItemStateChanged(ItemEvent event) {
 		if (((String)getJComboBoxRole2().getSelectedItem()).contentEquals("Hero")
 				&& !getJCheckBoxPC().isSelected()) {
 			getJCheckBoxPC().doClick();
 		} else if (!((String)getJComboBoxRole2().getSelectedItem()).contentEquals("Hero")
 				&& getJCheckBoxPC().isSelected()) {
 			getJCheckBoxPC().doClick();
 		}
 	}
 
 	/**
 	 * Event: Cancel pressed.
 	 * @param event
 	 */
 	private void jButtonCancelActionActionPerformed(ActionEvent event) {
 		setStat(null);
 		setVisible(false);
 	}
 
 	/**
 	 * Event: OK pressed.
 	 * @param event
 	 */
 	private void jButtonOKActionActionPerformed(ActionEvent event) {
 		setStat(new Stats());
 		moveFieldsToClass(getStat());
 		
 		if (!getStat().isValid()) {
 			JOptionPane.showMessageDialog(this, "Please assign a name, level, and role to this statblock.\n" +
 					"It cannot be saved until these values are assigned.", "Missing Information", JOptionPane.ERROR_MESSAGE);
 			setStat(null);
 		} else {
 			this.setVisible(false);
 		}
 	}
 
 	/**
 	 * Event: RTF Paste pressed.
 	 * @param event
 	 */
 	private void jButtonRTFPasteActionActionPerformed(ActionEvent event) {
 		Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
 		if (cb.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
 			setStat(new Stats());
 			try {
 				getStat().setStatsRTF((String) cb.getData(DataFlavor.stringFlavor));
 			} catch (UnsupportedFlavorException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			
 			if (getStat().isValid()) {
 				moveClassToFields(getStat());
 			}
 		}
 	}
 
 	/**
 	 * Event: CB Load pressed.
 	 * @param event
 	 */
 	private void jButtonCBLoadActionActionPerformed(ActionEvent event) {
 		setStat(new Stats());
 		
 		JFileChooser fc = new JFileChooser();
 		fc.setDialogTitle("Load Character File");
 		fc.setCurrentDirectory(Settings.getWorkingDirectory());
 		fc.setFileFilter(new FileFilter() {
 			
 			@Override
 			public String getDescription() {
 				return "Character files (*.dnd4e)";
 			}
 			
 			@Override
 			public boolean accept(File f) {
 				return (f.isDirectory() || f.getName().endsWith(".dnd4e"));
 			}
 		});
 		
 		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
 			Settings.setWorkingDirectory(fc.getCurrentDirectory());
 			if (getStat().loadFromCBFile(fc.getSelectedFile().getAbsolutePath())) {
 				if (getStat().isValid()) {
 					moveClassToFields(getStat());
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Reloads the power list UI from the list of powers in the class.
 	 */
 	private void resetPowerListFromArray() {
 		DefaultListModel model = (DefaultListModel) getJListPowers().getModel();
 		model.clear();
 		
 		if (model.isEmpty()) {
 			for (Power pow : getStatPowers()) {
 				model.addElement(pow);
 			}
 
 			setPowerChanged(false);
 		}
 	}
 
 	/**
 	 * Sets an indicator of if a power was changed.
 	 * @param powerChanged true, if a power was changed
 	 */
 	private void setPowerChanged(Boolean powerChanged) {
 		_powerChanged = powerChanged;
 	}
 
 	/**
 	 * Event: Powers list, selection changed.
 	 * @param event
 	 */
 	private void jListPowersListSelectionValueChanged(ListSelectionEvent event) {
 		DefaultListModel model = (DefaultListModel) getJListPowers().getModel();
 		if (getJListPowers().getSelectedIndex() >= 0) {
 			Power pow = (Power) getJListPowers().getSelectedValue();
 			powDataEnable();
 			powDataLoad(pow);
 			getJButtonPowerDelete().setEnabled(true);
 			
 			if (model.size() > 1 && getJListPowers().getSelectedIndex() > 0) {
 				getJButtonPowerUp().setEnabled(true);
 			} else {
 				getJButtonPowerUp().setEnabled(false);
 			}
 			if (model.size() > 1 && getJListPowers().getSelectedIndex() < model.size() - 1) {
 				getJButtonPowerDown().setEnabled(true);
 			} else {
 				getJButtonPowerDown().setEnabled(false);
 			}
 		} else {
 			powDataDisable();
 			powDataClear();
 			getJButtonPowerDelete().setEnabled(false);
 			getJButtonPowerUp().setEnabled(false);
 			getJButtonPowerDown().setEnabled(false);
 			if (isPowerChanged()) {
 				resetPowerListFromArray();
 			}
 		}
 	}
 	
 	/**
 	 * Returns an indicator of if the power information changed.
 	 * @return true, if the power informatin changed
 	 */
 	private Boolean isPowerChanged() {
 		return _powerChanged;
 	}
 
 	/**
 	 * Enables power data fields.
 	 */
 	private void powDataEnable() {
 		getJTextFieldPowerName().setEnabled(true);
 		getJTextFieldPowerKeywords().setEnabled(true);
 		getJTextFieldPowerAction().setEnabled(true);
 		getJTextAreaPowerDescription().setEnabled(true);
 		getJCheckBoxPowerAura().setEnabled(true);
 		if (getJCheckBoxPowerAura().isSelected()) {
 			getJFormattedTextFieldAuraSize().setEnabled(true);
 			getJComboBoxPowerIcon().setEnabled(false);
 		} else {
 			getJFormattedTextFieldAuraSize().setEnabled(false);
 			getJComboBoxPowerIcon().setEnabled(true);
 		}
 	}
 	
 	/**
 	 * Disables power data fields.
 	 */
 	private void powDataDisable() {
 		getJTextFieldPowerName().setEnabled(false);
 		getJTextFieldPowerKeywords().setEnabled(false);
 		getJTextFieldPowerAction().setEnabled(false);
 		getJTextAreaPowerDescription().setEnabled(false);
 		getJCheckBoxPowerAura().setEnabled(false);
 		getJFormattedTextFieldAuraSize().setEnabled(false);
 		getJComboBoxPowerIcon().setEnabled(false);
 	}
 	
 	/**
 	 * Clears power data fields.
 	 */
 	private void powDataClear() {
 		getJTextFieldPowerName().setText("");
 		getJComboBoxPowerIcon().setSelectedItem(null);
 		getJTextFieldPowerKeywords().setText("");
 		getJTextFieldPowerAction().setText("");
 		getJFormattedTextFieldAuraSize().setText("0");
 		getJCheckBoxPowerAura().setSelected(false);
 		getJComboBoxPowerIcon().setEnabled(false);
 		getJFormattedTextFieldAuraSize().setEnabled(false);
 		getJTextAreaPowerDescription().setText("");
 	}
 	
 	/**
 	 * Loads power data into data fields.
 	 * @param pow the power for which data should be loaded
 	 */
 	private void powDataLoad(Power pow) {
 		getJTextFieldPowerName().setText(pow.getName());
 		getJTextFieldPowerKeywords().setText(pow.getKeywords());
 		getJTextFieldPowerAction().setText(pow.getAction());
 		getJTextFieldPowerURL().setText(pow.getURL());
 		getJTextAreaPowerDescription().setText(pow.getDesc());
 		if (pow.isAura()) {
 			getJComboBoxPowerIcon().setSelectedIndex(0);
 			getJComboBoxPowerIcon().setEnabled(false);
 			getJCheckBoxPowerAura().setSelected(true);
 			getJFormattedTextFieldAuraSize().setEnabled(true);
 			getJFormattedTextFieldAuraSize().setText(pow.getAura().toString());
 		} else {
 			getJComboBoxPowerIcon().setSelectedItem(pow.getType());
 			getJComboBoxPowerIcon().setEnabled(true);
 			getJCheckBoxPowerAura().setSelected(false);
 			getJFormattedTextFieldAuraSize().setEnabled(false);
 			getJFormattedTextFieldAuraSize().setText("0");
 		}
 		setPowerChanged(false);
 	}
 
 	/**
 	 * Event: Aura checked/unchecked.
 	 * @param event
 	 */
 	private void jCheckBoxPowerAuraActionActionPerformed(ActionEvent event) {
 		if (getJCheckBoxPowerAura().isSelected()) {
 			getJComboBoxPowerIcon().setEnabled(false);
 			getJFormattedTextFieldAuraSize().setEnabled(true);
 			if (Integer.valueOf(getJFormattedTextFieldAuraSize().getText()) < 1) {
 				getJFormattedTextFieldAuraSize().setText("1");
 				getJFormattedTextFieldAuraSize().requestFocusInWindow();
 			}
 		} else {
 			getJComboBoxPowerIcon().setEnabled(true);
 			getJFormattedTextFieldAuraSize().setEnabled(false);
 			getJFormattedTextFieldAuraSize().setText("0");
 			if (isPowerDataValid()) {
 				Power pow = getStatPowers().get(getJListPowers().getSelectedIndex());
 				pow.setAura(0);
 				getJListPowers().repaint();
 			}
 		}
 		setPowerChanged(true);
 	}
 	
 	/**
 	 * Returns an indication of the power data being valid.
 	 * @return true, if the power data is valid
 	 */
 	private Boolean isPowerDataValid() {
 		if (((DefaultListModel) getJListPowers().getModel()).size() < 1) {
 			powDataDisable();
 			powDataClear();
 			return false;
 		} else if (getJListPowers().getSelectedIndex() >= getStatPowers().size()) {
 			powDataDisable();
 			powDataClear();
 			return false;
 		}
 		
 		return true;
 	}
 
 	/**
 	 * Event: Power Name focus lost.
 	 * @param event
 	 */
 	private void jTextFieldPowerNameFocusFocusLost(FocusEvent event) {
 		if (isPowerDataValid()) {
 			Power pow = getStatPowers().get(getJListPowers().getSelectedIndex());
 			if (getJTextFieldPowerName().getText().isEmpty()) {
 				getJTextFieldPowerName().setText("(no name)");
 			}
 			pow.setName(getJTextFieldPowerName().getText());
 			setPowerChanged(true);
 			getJListPowers().repaint();
 		}
 	}
 
 	/**
 	 * Event: Power Icon focus lost.
 	 * @param event
 	 */
 	private void jComboBoxPowerIconFocusFocusLost(FocusEvent event) {
 		if (isPowerDataValid()) {
 			Power pow = getStatPowers().get(getJListPowers().getSelectedIndex());
 			pow.setType((String) getJComboBoxPowerIcon().getSelectedItem());
 			setPowerChanged(true);
 			getJListPowers().repaint();
 		}
 	}
 
 	/**
 	 * Event: Power Keywords focus lost.
 	 * @param event
 	 */
 	private void jTextFieldPowerKeywordsFocusFocusLost(FocusEvent event) {
 		if (isPowerDataValid()) {
 			Power pow = getStatPowers().get(getJListPowers().getSelectedIndex());
 			pow.setKeywords(getJTextFieldPowerKeywords().getText());
 			setPowerChanged(true);
 			getJListPowers().repaint();
 		}
 	}
 
 	/**
 	 * Event: Power Action focus lost.
 	 * @param event
 	 */
 	private void jTextFieldPowerActionFocusFocusLost(FocusEvent event) {
 		if (isPowerDataValid()) {
 			Power pow = getStatPowers().get(getJListPowers().getSelectedIndex());
 			pow.setAction(getJTextFieldPowerAction().getText());
 			setPowerChanged(true);
 			getJListPowers().repaint();
 		}
 	}
 
 	/**
 	 * Event: Power Description focus lost.
 	 * @param event
 	 */
 	private void jTextAreaPowerDescriptionFocusFocusLost(FocusEvent event) {
 		if (isPowerDataValid()) {
 			Power pow = getStatPowers().get(getJListPowers().getSelectedIndex());
 			pow.setDesc(getJTextAreaPowerDescription().getText());
 			setPowerChanged(true);
 			getJListPowers().repaint();
 		}
 	}
 
 	/**
 	 * Event: Power New pressed.
 	 * @param event
 	 */
 	private void jButtonPowerNewActionActionPerformed(ActionEvent event) {
 		Power pow = new Power();
 		getStatPowers().add(pow);
 		resetPowerListFromArray();
 		getJListPowers().setSelectedValue(pow, true);
 	}
 
 	/**
 	 * Event: Power Delete pressed.
 	 * @param event
 	 */
 	private void jButtonPowerDeleteActionActionPerformed(ActionEvent event) {
 		if (isPowerDataValid()) {
 			getStatPowers().remove(getJListPowers().getSelectedIndex());
 			resetPowerListFromArray();
 			getJListPowers().setSelectedIndex(-1);
 		}
 	}
 
 	/**
 	 * Event: Power Up pressed.
 	 * @param event
 	 */
 	private void jButtonPowerUpActionActionPerformed(ActionEvent event) {
 		if (isPowerDataValid()) {
 			Integer index = getJListPowers().getSelectedIndex();
 			if (index > 0) {
 				Power tempPow = getStatPowers().get(index);
 				getStatPowers().set(index, getStatPowers().get(index - 1));
 				index--;
 				getStatPowers().set(index, tempPow);
 				resetPowerListFromArray();
 				getJListPowers().setSelectedValue(tempPow, true);
 			}
 		}
 	}
 
 	/**
 	 * Event: Power Down pressed.
 	 * @param event
 	 */
 	private void jButtonPowerDownActionActionPerformed(ActionEvent event) {
 		if (isPowerDataValid()) {
 			Integer index = getJListPowers().getSelectedIndex();
 			if (index < ((DefaultListModel) getJListPowers().getModel()).size() - 1) {
 				Power tempPow = getStatPowers().get(index);
 				getStatPowers().set(index, getStatPowers().get(index + 1));
 				index++;
 				getStatPowers().set(index, tempPow);
 				resetPowerListFromArray();
 				getJListPowers().setSelectedValue(tempPow, true);
 			}
 		}
 	}
 	
 	/**
 	 * Adds a preset effect to the list.
 	 * @param eff
 	 */
 	private void presetEffectAdd(EffectBase eff) {
 		if (eff.isValid() && !getPresetEffects().contains(eff)) {
 			getPresetEffects().add(eff);
 		}
 	}
 	
 	/**
 	 * Clears effect fields.
 	 */
 	private void clearEffect() {
 		getJComboBoxEffect().setSelectedIndex(-1);
 		getJComboBoxDuration().setSelectedIndex(-1);
 		getJCheckBoxBeneficial().setSelected(false);
 		getJCheckBoxHidden().setSelected(false);
 	}
 	
 	/**
 	 * Reloads the effect list from the array in the class.
 	 */
 	private void resetEffectListFromArray() {
 		DefaultListModel model = (DefaultListModel) getJListEffects().getModel();
 		model.clear();
 		
 		if (model.isEmpty()) {
 			getJButtonDelete().setEnabled(false);
 
 			for (EffectBase eff : getPresetEffects().toArray(new EffectBase[0])) {
 				model.addElement(eff);
 			}
 		}
 	}
 
 	/**
 	 * Event: Effect Add clicked.
 	 * @param event
 	 */
 	private void jButtonAddActionActionPerformed(ActionEvent event) {
 		EffectBase eff = new EffectBase((String)getJComboBoxEffect().getSelectedItem(),
 				(Duration)getJComboBoxDuration().getSelectedItem(),
 				getJCheckBoxBeneficial().isSelected());
 		if (eff.isValid()) {
 			presetEffectAdd(eff);
 			clearEffect();
 			resetEffectListFromArray();
 		} else {
 			JOptionPane
 					.showMessageDialog(
 							this,
 							"Please assign a name and duration to the effect. It cannot be added until these values are assigned.",
 							"Missing Effect Info", JOptionPane.ERROR_MESSAGE);
 		}
 	}
 
 	/**
 	 * Eevent Delete pressed.
 	 * @param event
 	 */
 	private void jButtonDeleteActionActionPerformed(ActionEvent event) {
 		if (getJListEffects().getSelectedIndex() >= 0) {
 			getPresetEffects().remove(getJListEffects().getSelectedValue());
 			resetEffectListFromArray();
 		}
 	}
 
 	/**
 	 * Event: Effect list selection changed.
 	 * @param event
 	 */
 	private void jListEffectsListSelectionValueChanged(ListSelectionEvent event) {
 		if (getJListEffects().getSelectedIndex() >= 0) {
 			getJButtonDelete().setEnabled(true);
 		} else {
 			getJButtonDelete().setEnabled(false);
 		}
 	}
 
 	/**
 	 * Event: Effect Export pressed.
 	 * @param event
 	 */
 	private void jButtonExportActionActionPerformed(ActionEvent event) {
 		JFileChooser fc = new JFileChooser();
 		fc.setDialogTitle("Export to XML File");
 		fc.setCurrentDirectory(Settings.getWorkingDirectory());
 		fc.setFileFilter(new FileFilter() {
 			
 			@Override
 			public String getDescription() {
 				return "XML files (*.xml)";
 			}
 			
 			@Override
 			public boolean accept(File f) {
 				return (f.isDirectory() || f.getName().endsWith(".xml"));
 			}
 		});
 		
 		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
 			Settings.setWorkingDirectory(fc.getCurrentDirectory());
 			try {
 				File out = fc.getSelectedFile();
 				if (out.exists()) {
 					out.delete();
 				}
 				out.createNewFile();
 				XMLStreamWriter writer = XMLStreamWriterFactory.create(new FileOutputStream(out));
 				writer.writeStartElement("effects");
 				for (EffectBase eff : getPresetEffects().toArray(new EffectBase[0])) {
 					eff.exportXML(writer);
 				}
 				writer.writeEndElement();
 				writer.close();
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (XMLStreamException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Event: Effect Import pressed.
 	 * @param event
 	 */
 	private void jButtonImportActionActionPerformed(ActionEvent event) {
 		getPresetEffects().clear();
 		
 		JFileChooser fc = new JFileChooser();
 		fc.setDialogTitle("Load XML File");
 		fc.setCurrentDirectory(Settings.getWorkingDirectory());
 		fc.setFileFilter(new FileFilter() {
 			
 			@Override
 			public String getDescription() {
 				return "XML files (*.xml)";
 			}
 			
 			@Override
 			public boolean accept(File f) {
 				return (f.isDirectory() || f.getName().endsWith(".xml"));
 			}
 		});
 		
 		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
 			Settings.setWorkingDirectory(fc.getCurrentDirectory());
 			InputSource input;
 			try {
 				input = new InputSource(new FileInputStream(fc.getSelectedFile()));
 				XMLStreamReader reader = XMLStreamReaderFactory.create(input, false);
 				while (reader.hasNext()) {
 					reader.next();
 					EffectBase eff = new EffectBase();
 					eff.importXML(reader);
 					presetEffectAdd(eff);
 				}
 				reader.close();
 				resetEffectListFromArray();
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (XMLStreamException e) {
 				e.printStackTrace();
 			}
 		}		
 	}
 
 	/**
 	 * Event: Aura size focus lost.
 	 * @param event
 	 */
 	private void jFormattedTextFieldAuraSizeFocusFocusLost(FocusEvent event) {
 		if (isPowerDataValid()) {
 			Power pow = getStatPowers().get(getJListPowers().getSelectedIndex());
 			pow.setAura(Integer.valueOf(getJFormattedTextFieldAuraSize().getText()));
 			setPowerChanged(true);
 			getJListPowers().repaint();
 		}
 	}
 }
