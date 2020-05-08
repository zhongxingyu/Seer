 package org.vpac.grisu.clients.blender.swing;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.Set;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSeparator;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.filechooser.FileFilter;
 
 import org.apache.commons.lang.StringUtils;
 import org.vpac.grisu.clients.blender.BlendFile;
 import org.vpac.grisu.clients.blender.GrisuBlenderJob;
 import org.vpac.grisu.settings.ClientPropertiesManager;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 import com.jgoodies.forms.layout.Sizes;
 import com.jidesoft.swing.FolderChooser;
 import com.jidesoft.swing.RangeSlider;
 
 public class BlenderBasicJobPropertiesPanel extends JPanel {
 
 	public static final String LAST_BLENDER_FILE_DIR = "lastBlenderFileDir";
 
 	public static final FileFilter BLEND_FILE_FILTER = new FileFilter() {
 
 		@Override
 		public boolean accept(File f) {
 			if (f.isDirectory() || f.getName().endsWith(".blend")) {
 				return true;
 			} else {
 				return false;
 			}
 		}
 
 		@Override
 		public String getDescription() {
 			return "*.blend";
 		}
 	};
 
 	private JTextField blendFileTextField;
 	private JButton blendFileBrowseButton;
 	private JLabel lblBlendFile;
 
 	private BlendFile blendFileObject;
 	private File dotBlendFile;
 	private File fluidsFolder;
 
 	private JLabel lblFluidsFolder;
 	private JTextField fluidsfolderTextField;
 	private JButton btnBrowse;
 	private RangeSlider slider;
 	private JLabel startLabel;
 	private JLabel endLabel;
 	private JLabel firstLabel;
 	private JTextField textField;
 	private JLabel lastLabel;
 	private JTextField textField_1;
 	private JButton button;
 	private JButton infoButton;
 	private JCheckBox chckbxSpecifyFrameRange;
 	private JLabel lblFormat;
 	private JComboBox comboBox;
 	private JLabel lblWalltimePerFrame;
 	private JComboBox hoursCombobox;
 	private JLabel lblHours;
 	private JComboBox minutesCombobox;
 	private JLabel lblMinutes;
 	private JLabel lblJobname;
 	private JTextField textField_2;
 	private JLabel lblGroup;
 	private JComboBox comboBox_3;
 	// private final ServiceInterface si;
 	// private final UserEnvironmentManager em;
 	private final DefaultComboBoxModel voModel = new DefaultComboBoxModel();
 
 	private final GrisuBlenderJob job = null;
 	private final BlenderJobCreationPanel parent;
 	private JSeparator separator;
 	private JSeparator separator_1;
 	private JSeparator separator_2;
 
 	public BlenderBasicJobPropertiesPanel(BlenderJobCreationPanel parent) {
 
 		this.parent = parent;
 
 		setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(50dlu;default)"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				new ColumnSpec(ColumnSpec.FILL, Sizes.bounded(Sizes.PREFERRED,
 						Sizes.constant("50dlu", true), Sizes.constant("50dlu",
 								true)), 0),
 								FormFactory.RELATED_GAP_COLSPEC,
 								ColumnSpec.decode("max(30dlu;default)"),
 								FormFactory.RELATED_GAP_COLSPEC,
 								ColumnSpec.decode("max(15dlu;default):grow"),
 								FormFactory.RELATED_GAP_COLSPEC,
 								ColumnSpec.decode("max(50dlu;default)"),
 								FormFactory.RELATED_GAP_COLSPEC,
 								new ColumnSpec(ColumnSpec.FILL, Sizes.bounded(Sizes.PREFERRED,
 										Sizes.constant("23dlu", true), Sizes.constant("50dlu",
 												true)), 0),
 												FormFactory.RELATED_GAP_COLSPEC,
 												new ColumnSpec(ColumnSpec.FILL, Sizes.bounded(Sizes.PREFERRED,
 														Sizes.constant("23dlu", true), Sizes.constant("50dlu",
 																true)), 0), FormFactory.RELATED_GAP_COLSPEC,
 																ColumnSpec.decode("max(50dlu;default)"),
 																FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
 				RowSpec.decode("8dlu"), FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				RowSpec.decode("max(13dlu;default)"),
 				FormFactory.DEFAULT_ROWSPEC, RowSpec.decode("8dlu"),
 				FormFactory.DEFAULT_ROWSPEC, RowSpec.decode("12dlu"),
 				FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, }));
 		add(getLblBlendFile(), "2, 2, 13, 1");
 		add(getBlendFileTextField(), "2, 4, 11, 1, fill, default");
 		add(getInfoButton(), "14, 4");
 		add(getBlendFileBrowseButton(), "16, 4");
 		add(getLblFluidsFolder(), "2, 6, 13, 1");
 		add(getFluidsfolderTextField(), "2, 8, 11, 1, fill, default");
 		add(getUnsetFluidFolderButton(), "14, 8");
 		add(getBtnBrowse(), "16, 8");
 		add(getSeparator_1(), "2, 9, 15, 1, default, bottom");
 		add(getChckbxSpecifyFrameRange(), "2, 10, 15, 1");
 		add(getSlider(), "2, 11, 15, 1");
 		add(getStartLabel(), "2, 12, left, bottom");
 		add(getEndLabel(), "16, 12, right, bottom");
 		add(getFirstLabel(), "2, 13, right, default");
 		add(getFirstField(), "4, 13, fill, default");
 		add(getLastField(), "12, 13, 3, 1, fill, default");
 		add(getLastLabel(), "16, 13, left, default");
 		add(getSeparator_2(), "2, 14, 15, 1");
 		add(getLblFormat(), "2, 15, left, default");
 		add(getFormatCombobox(), "4, 15, 3, 1");
 		add(getSeparator(), "2, 16, 10, 1");
 		add(getLblHours(), "12, 16, 3, 1, right, bottom");
 		add(getLblMinutes(), "16, 16, right, bottom");
 		add(getLblWalltimePerFrame(), "2, 17, 9, 1, right, default");
 		add(getHoursCombobox(), "12, 17, 3, 1, fill, default");
 		add(getMinutesCombobox(), "16, 17, fill, default");
 		add(getLblJobname(), "2, 19, right, default");
 		add(getJobNameTextField(), "4, 19, 3, 1, fill, default");
 		add(getLblGroup(), "8, 19, 3, 1, right, default");
 		add(getVOComboBox(), "12, 19, 5, 1, fill, default");
 		// add(getStatusTextArea(), "2, 26, 15, 1, fill, fill");
 	}
 
 	public void createBlendFile() {
 
 		if (dotBlendFile == null) {
 			return;
 		}
 		try {
 			blendFileObject = new BlendFile(dotBlendFile, fluidsFolder);
 
 			getJobNameTextField().setText(parent.setBlendFile(blendFileObject));
 			getSlider().setMinimum(blendFileObject.getStartFrame());
 			getSlider().setMaximum(blendFileObject.getEndFrame());
 			getSlider().setLowValue(blendFileObject.getStartFrame());
 			getSlider().setHighValue(blendFileObject.getEndFrame());
 			getStartLabel().setText("Min: " + blendFileObject.getStartFrame());
 			getEndLabel().setText("Max: " + blendFileObject.getEndFrame());
 
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return;
 		} catch (Exception ex) {
 			ex.printStackTrace();
 
 			parent.addMessage("Could not parse blendfile: "
 					+ ex.getLocalizedMessage());
 			return;
 
 		} finally {
 
 			getSlider().setEnabled(true);
 			getInfoButton().setEnabled(true);
 
 		}
 	}
 
 	private void enableManualFrameSelection(boolean checked) {
 
 		getStartLabel().setEnabled(checked);
 		getEndLabel().setEnabled(checked);
 		getFirstField().setEnabled(checked);
 		getLastLabel().setEnabled(checked);
 		getSlider().setEnabled(checked);
 		getFirstField().setEnabled(checked);
 		getLastField().setEnabled(checked);
 	}
 
 	private JButton getBlendFileBrowseButton() {
 		if (blendFileBrowseButton == null) {
 			blendFileBrowseButton = new JButton("Browse");
 			blendFileBrowseButton.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent arg0) {
 
 					String lastDir = ClientPropertiesManager
 					.getProperty(LAST_BLENDER_FILE_DIR);
 					if (StringUtils.isBlank(lastDir)
 							|| !new File(lastDir).exists()) {
 						lastDir = System.getProperty("user.home");
 					}
 
 					final JFileChooser fc = new JFileChooser();
 					fc.setDialogTitle("Open .blend file");
 
 					// Choose only files, not directories
 					fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
 					fc.setCurrentDirectory(new File(lastDir));
 					// Set filter for Java source files.
 					fc.setFileFilter(BLEND_FILE_FILTER);
 
 					int result = fc
 					.showOpenDialog(BlenderBasicJobPropertiesPanel.this);
 
 					if (result == JFileChooser.APPROVE_OPTION) {
 						new Thread() {
 							@Override
 							public void run() {
 								parent.lockUI(true);
 								ClientPropertiesManager.setProperty(
 										LAST_BLENDER_FILE_DIR, fc
 										.getCurrentDirectory()
 										.toString());
 								parent.addMessage("Parsing blend file: "
 										+ fc.getSelectedFile().toString()
 										+ "\n");
 
 								setDotBlendFile(fc.getSelectedFile());
 
 								if (blendFileObject != null) {
 
 									parent.addMessage(blendFileObject
 											.getParseMessage());
 									getBlendFileTextField().setText(
 											blendFileObject.getFile()
 											.toString());
 								}
 								getChckbxSpecifyFrameRange().setEnabled(true);
 								parent.lockUI(false);
 							}
 						}.start();
 					}
 
 				}
 			});
 		}
 		return blendFileBrowseButton;
 	}
 
 	private JTextField getBlendFileTextField() {
 		if (blendFileTextField == null) {
 			blendFileTextField = new JTextField();
 			blendFileTextField.setEditable(false);
 			blendFileTextField.setColumns(10);
 		}
 		return blendFileTextField;
 	}
 
 	private JButton getBtnBrowse() {
 		if (btnBrowse == null) {
 			btnBrowse = new JButton("Browse");
 			btnBrowse.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 
 					FolderChooser fc = new FolderChooser();
 
 					String lastDir = ClientPropertiesManager
 					.getProperty(LAST_BLENDER_FILE_DIR);
 					if (StringUtils.isBlank(lastDir)
 							|| !new File(lastDir).exists()) {
 						lastDir = System.getProperty("user.home");
 					}
 					fc.setCurrentDirectory(new File(lastDir));
 
 					int result = fc
 					.showOpenDialog(BlenderBasicJobPropertiesPanel.this
 							.getTopLevelAncestor());
 					if (result == JFileChooser.APPROVE_OPTION) {
 
 						File temp = fc.getSelectedFolder();
 						if ((temp != null) && temp.exists()) {
 							getFluidsfolderTextField().setText(temp.toString());
 							setFluidsFolder(temp);
 							button.setEnabled(true);
 						}
 
 					}
 
 				}
 			});
 		}
 		return btnBrowse;
 	}
 
 	private JCheckBox getChckbxSpecifyFrameRange() {
 		if (chckbxSpecifyFrameRange == null) {
 			chckbxSpecifyFrameRange = new JCheckBox(
 			"Specify frame range to render");
 			chckbxSpecifyFrameRange.setEnabled(false);
 			chckbxSpecifyFrameRange.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 
 					boolean checked = chckbxSpecifyFrameRange.isSelected();
 					enableManualFrameSelection(checked);
 
 				}
 			});
 		}
 		return chckbxSpecifyFrameRange;
 	}
 
 	public int getCurrentWalltimeInSeconds() {
 
 		int hours = Integer.parseInt((String) getHoursCombobox()
 				.getSelectedItem());
 		int minutes = Integer.parseInt((String) getMinutesCombobox()
 				.getSelectedItem());
 
 		return hours * 3600 + minutes * 60;
 	}
 
 	private JLabel getEndLabel() {
 		if (endLabel == null) {
 			endLabel = new JLabel("Max: n/a");
 			endLabel.setEnabled(false);
 		}
 		return endLabel;
 	}
 
 	private JTextField getFirstField() {
 		if (textField == null) {
 			textField = new JTextField();
 			textField.setEnabled(false);
 			textField.setEditable(false);
 			textField.setColumns(10);
 		}
 		return textField;
 	}
 
 	public int getFirstFrame() {
 		if (getChckbxSpecifyFrameRange().isSelected()) {
 			return getSlider().getLowValue();
 		} else {
 			return blendFileObject.getStartFrame();
 		}
 	}
 
 	private JLabel getFirstLabel() {
 		if (firstLabel == null) {
 			firstLabel = new JLabel("First frame");
 			firstLabel.setEnabled(false);
 		}
 		return firstLabel;
 	}
 
 	private JTextField getFluidsfolderTextField() {
 		if (fluidsfolderTextField == null) {
 			fluidsfolderTextField = new JTextField();
 			fluidsfolderTextField.setEditable(false);
 			fluidsfolderTextField.setColumns(10);
 		}
 		return fluidsfolderTextField;
 	}
 
 	private JComboBox getFormatCombobox() {
 		if (comboBox == null) {
 			comboBox = new JComboBox();
 			comboBox.setModel(new DefaultComboBoxModel(new String[] { "TGA",
 					"IRIS", "HAMX", "JPEG", "MOVIE", "IRIZ", "RAWTGA",
 					"AVIRAW", "AVIJPEG", "PNG", "BMP", "FRAMESERVER" }));
 			comboBox.setSelectedItem("PNG");
 		}
 		return comboBox;
 	}
 
 	private JComboBox getHoursCombobox() {
 		if (hoursCombobox == null) {
 			hoursCombobox = new JComboBox();
 			hoursCombobox.setModel(new DefaultComboBoxModel(new String[] { "0",
 					"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11",
 					"12", "24", "36" }));
 		}
 		return hoursCombobox;
 	}
 
 	private JButton getInfoButton() {
 		if (infoButton == null) {
 			infoButton = new JButton("Info");
 			infoButton.setEnabled(false);
 		}
 		return infoButton;
 	}
 
 	private JTextField getJobNameTextField() {
 		if (textField_2 == null) {
 			textField_2 = new JTextField();
 			textField_2.addKeyListener(new KeyAdapter() {
 				@Override
 				public void keyReleased(KeyEvent e) {
 
 					parent.setJobname(textField_2.getText());
 
 				}
 			});
 			textField_2.setColumns(10);
 		}
 		return textField_2;
 	}
 
 	private JTextField getLastField() {
 		if (textField_1 == null) {
 			textField_1 = new JTextField();
 			textField_1.setEnabled(false);
 			textField_1.setEditable(false);
 			textField_1.setColumns(10);
 		}
 		return textField_1;
 	}
 
 	public int getLastFrame() {
 		if (getChckbxSpecifyFrameRange().isSelected()) {
 			return getSlider().getHighValue();
 		} else {
 			return blendFileObject.getEndFrame();
 		}
 	}
 
 	private JLabel getLastLabel() {
 		if (lastLabel == null) {
 			lastLabel = new JLabel("Last frame");
 			lastLabel.setEnabled(false);
 		}
 		return lastLabel;
 	}
 
 	private JLabel getLblBlendFile() {
 		if (lblBlendFile == null) {
 			lblBlendFile = new JLabel("Blend file");
 		}
 		return lblBlendFile;
 	}
 
 	private JLabel getLblFluidsFolder() {
 		if (lblFluidsFolder == null) {
 			lblFluidsFolder = new JLabel("Fluids folder (optional)");
 		}
 		return lblFluidsFolder;
 	}
 
 	private JLabel getLblFormat() {
 		if (lblFormat == null) {
 			lblFormat = new JLabel("Format");
 		}
 		return lblFormat;
 	}
 
 	private JLabel getLblGroup() {
 		if (lblGroup == null) {
 			lblGroup = new JLabel("Group");
 		}
 		return lblGroup;
 	}
 
 	private JLabel getLblHours() {
 		if (lblHours == null) {
 			lblHours = new JLabel("Hours");
 		}
 		return lblHours;
 	}
 
 	private JLabel getLblJobname() {
 		if (lblJobname == null) {
 			lblJobname = new JLabel("Jobname");
 		}
 		return lblJobname;
 	}
 
 	private JLabel getLblMinutes() {
 		if (lblMinutes == null) {
 			lblMinutes = new JLabel("Minutes");
 		}
 		return lblMinutes;
 	}
 
 	private JLabel getLblWalltimePerFrame() {
 		if (lblWalltimePerFrame == null) {
 			lblWalltimePerFrame = new JLabel(
 			"Walltime per frame (for most complex frame)");
 		}
 		return lblWalltimePerFrame;
 	}
 
 	private JComboBox getMinutesCombobox() {
 		if (minutesCombobox == null) {
 			minutesCombobox = new JComboBox();
 			minutesCombobox.setModel(new DefaultComboBoxModel(new String[] {
 					"00", "10", "20", "30", "40", "50" }));
 			minutesCombobox.setSelectedItem("20");
 		}
 		return minutesCombobox;
 	}
 
 	public String getSelectedFqan() {
 
 		return (String) getVOComboBox().getSelectedItem();
 	}
 
 	private JSeparator getSeparator() {
 		if (separator == null) {
 			separator = new JSeparator();
 		}
 		return separator;
 	}
 
 	private JSeparator getSeparator_1() {
 		if (separator_1 == null) {
 			separator_1 = new JSeparator();
 		}
 		return separator_1;
 	}
 
 	private JSeparator getSeparator_2() {
 		if (separator_2 == null) {
 			separator_2 = new JSeparator();
 		}
 		return separator_2;
 	}
 
 	private RangeSlider getSlider() {
 		if (slider == null) {
 			slider = new RangeSlider(0, 100, 0, 0);
 			slider.setEnabled(false);
 			slider.setPaintTicks(false);
 			slider.addChangeListener(new ChangeListener() {
 				public void stateChanged(ChangeEvent e) {
 					getFirstField().setText("" + slider.getLowValue());
 					getLastField().setText("" + slider.getHighValue());
 				}
 			});
 		}
 		return slider;
 	}
 
 	private JLabel getStartLabel() {
 		if (startLabel == null) {
 			startLabel = new JLabel("Min: n/a");
 			startLabel.setEnabled(false);
 		}
 		return startLabel;
 	}
 
 	private JButton getUnsetFluidFolderButton() {
 		if (button == null) {
 			button = new JButton("X");
 			button.setEnabled(false);
 			button.addActionListener(new ActionListener() {
 
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					getFluidsfolderTextField().setText(null);
 					setFluidsFolder(null);
 					button.setEnabled(false);
 				}
 			});
 		}
 		return button;
 	}
 
 	private JComboBox getVOComboBox() {
 		if (comboBox_3 == null) {
 			comboBox_3 = new JComboBox(voModel);
 			comboBox_3.addItemListener(new ItemListener() {
 
 				@Override
 				public void itemStateChanged(ItemEvent e) {
 					if (ItemEvent.SELECTED == e.getStateChange()) {
 						parent.setFqan((String) comboBox_3.getSelectedItem());
 					}
 
 				}
 			});
 		}
 		return comboBox_3;
 	}
 
 	public void lockUI(final boolean lock) {
 
 		SwingUtilities.invokeLater(new Thread() {
 
 			@Override
 			public void run() {
 
 				getBlendFileBrowseButton().setEnabled(!lock);
 				getBtnBrowse().setEnabled(!lock);
 				getChckbxSpecifyFrameRange().setEnabled(!lock);
 				getHoursCombobox().setEnabled(!lock);
 				getMinutesCombobox().setEnabled(!lock);
 				getBlendFileTextField().setEnabled(!lock);
 				getFluidsfolderTextField().setEnabled(!lock);
 				getFormatCombobox().setEnabled(!lock);
 				getJobNameTextField().setEnabled(!lock);
 				getInfoButton().setEnabled(!lock);
 				getUnsetFluidFolderButton().setEnabled(!lock);
 				getVOComboBox().setEnabled(!lock);
 				if (lock) {
 					enableManualFrameSelection(false);
 				} else {
 					if (getChckbxSpecifyFrameRange().isSelected()) {
 						enableManualFrameSelection(true);
 					} else {
 						enableManualFrameSelection(false);
 					}
 				}
 
 			}
 
 		});
 	}
 
 	public void setAvailableVos(Set<String> vos) {
 
 		voModel.removeAllElements();
 
 		for ( String vo : vos ) {
 			voModel.addElement(vo);
 		}
 
 	}
 
 	private void setDotBlendFile(File blendFile) {
 		this.dotBlendFile = blendFile;
 		createBlendFile();
 
 	}
 
 	private void setFluidsFolder(File folder) {
 		this.fluidsFolder = folder;
 		if ((dotBlendFile != null) && dotBlendFile.exists()) {
 			createBlendFile();
 		}
 	}
 }
