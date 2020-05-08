 // PatroDyne: Patron Supported Dynamic Executables
 // Released under LGPL license. See terms at http://www.gnu.org.
 package org.patrodyne.etl.transformio.tui;
 
 import static org.patrodyne.etl.transformio.PreferenceKey.ConsoleHistorySize;
 import static org.patrodyne.etl.transformio.PreferenceKey.InitialSourceEditMode;
 import static org.patrodyne.etl.transformio.PreferenceKey.InitialWrapSourceData;
 import static org.patrodyne.etl.transformio.PreferenceKey.InitialWrapTargetData;
 import static org.patrodyne.etl.transformio.PreferenceKey.InitialWrapConsoleData;
 
 import java.util.prefs.Preferences;
 
 import charva.awt.*;
 import charva.awt.event.*;
 import charvax.swing.*;
 import charvax.swing.border.*;
 
 //import java.awt.*;
 //import java.awt.event.*;
 //import javax.swing.*;
 //import javax.swing.border.*;
 
 /**
  * The Class PreferencesManager is a JDialog to configure preferences.
  * 
  * @author Rick O'Sullivan
  */
 public class PreferencesManager
 	extends JDialog
 {
 	private final JPanel contentPanel = new JPanel();
 	private static final int DEFAULT_CONSOLE_HISTORY_SIZE = 100;
 
 	private static String preferencesPath;
 	
 	/**
 	 * Gets the preferences path.
 	 *
 	 * @return the preferences path
 	 */
 	protected static String getPreferencesPath()
 	{
 		return preferencesPath;
 	}
 	/**
 	 * Sets the preferences path.
 	 *
 	 * @param preferencesPath the new preferences path
 	 */
 	protected static void setPreferencesPath(String preferencesPath)
 	{
 		PreferencesManager.preferencesPath = preferencesPath;
 	}
 
 	private static Preferences preferences;
 	/**
 	 * Gets the preferences.
 	 *
 	 * @return the preferences
 	 */
 	protected static Preferences getPreferences()
 	{
 		if ( preferences == null )
 			setPreferences(Preferences.userRoot().node(getPreferencesPath()));
 		return preferences;
 	}
 	/**
 	 * Sets the preferences.
 	 *
 	 * @param preferences the new preferences
 	 */
 	protected static void setPreferences(Preferences preferences)
 	{
 		PreferencesManager.preferences = preferences;
 	}
 	
 	private static Integer preferredConsoleHistorySize;
 	/**
 	 * Gets the preferred console history size.
 	 *
 	 * @return the preferred console history size
 	 */
 	protected static Integer getPreferredConsoleHistorySize()
 	{
 		if ( preferredConsoleHistorySize == null )
 			preferredConsoleHistorySize = getPreferences().getInt(ConsoleHistorySize.name(), 
 				DEFAULT_CONSOLE_HISTORY_SIZE);
 		return preferredConsoleHistorySize;
 	}
 	/**
 	 * Sets the preferred console history size.
 	 *
 	 * @param preferredConsoleHistorySize the new preferred console history size
 	 */
 	protected static void setPreferredConsoleHistorySize(Integer preferredConsoleHistorySize)
 	{
 		getPreferences().putInt(ConsoleHistorySize.name(), preferredConsoleHistorySize);
 		PreferencesManager.preferredConsoleHistorySize = preferredConsoleHistorySize;
 	}
 	
 	/**
 	 * Sets the preferred console history size.
 	 *
 	 * @param preferredConsoleHistorySize the new preferred console history size
 	 */
 	protected static void setPreferredConsoleHistorySize(String preferredConsoleHistorySize)
 	{
 		try
 		{
 			setPreferredConsoleHistorySize(Integer.parseInt(preferredConsoleHistorySize));
 		}
 		catch ( NumberFormatException nfe)
 		{
 			// No update.
 		}
 	}
 
 	private static Boolean preferredInitialSourceEditMode;
 	/**
 	 * Gets the preferred initial source edit mode.
 	 *
 	 * @return the preferred initial source edit mode
 	 */
 	protected static Boolean getPreferredInitialSourceEditMode()
 	{
 		if ( preferredInitialSourceEditMode == null )
 		{
 			preferredInitialSourceEditMode =
 				getPreferences().getBoolean(InitialSourceEditMode.name(), false);
 		}
 		return preferredInitialSourceEditMode;
 	}
 	/**
 	 * Sets the preferred initial source edit mode.
 	 *
 	 * @param preferredInitialSourceEditMode the new preferred initial source edit mode
 	 */
 	protected static void setPreferredInitialSourceEditMode(Boolean preferredInitialSourceEditMode)
 	{
 		getPreferences().putBoolean(InitialSourceEditMode.name(), preferredInitialSourceEditMode);
 		PreferencesManager.preferredInitialSourceEditMode = preferredInitialSourceEditMode;
 	}
 	
 	private static Boolean preferredInitialWrapSourceData;
 	/**
 	 * Gets the preferred initial wrap source data.
 	 *
 	 * @return the preferred initial wrap source data
 	 */
 	protected static Boolean getPreferredInitialWrapSourceData()
 	{
 		if ( preferredInitialWrapSourceData == null )
 		{
 			preferredInitialWrapSourceData =
 				getPreferences().getBoolean(InitialWrapSourceData.name(), false);
 		}
 		return preferredInitialWrapSourceData;
 	}
 	/**
 	 * Sets the preferred initial wrap source data.
 	 *
 	 * @param preferredInitialWrapSourceData the new preferred initial wrap source data
 	 */
 	protected static void setPreferredInitialWrapSourceData(Boolean preferredInitialWrapSourceData)
 	{
 		getPreferences().putBoolean(InitialWrapSourceData.name(), preferredInitialWrapSourceData);
 		PreferencesManager.preferredInitialWrapSourceData = preferredInitialWrapSourceData;
 	}
 	
 	private static Boolean preferredInitialWrapTargetData;
 	/**
 	 * Gets the preferred initial wrap target data.
 	 *
 	 * @return the preferred initial wrap target data
 	 */
 	protected static Boolean getPreferredInitialWrapTargetData()
 	{
 		if ( preferredInitialWrapTargetData == null )
 		{
 			preferredInitialWrapTargetData =
 				getPreferences().getBoolean(InitialWrapTargetData.name(), false);
 		}
 		return preferredInitialWrapTargetData;
 	}
 	/**
 	 * Sets the preferred initial wrap target data.
 	 *
 	 * @param preferredInitialWrapTargetData the new preferred initial wrap target data
 	 */
 	protected static void setPreferredInitialWrapTargetData(Boolean preferredInitialWrapTargetData)
 	{
 		getPreferences().putBoolean(InitialWrapTargetData.name(), preferredInitialWrapTargetData);
 		PreferencesManager.preferredInitialWrapTargetData = preferredInitialWrapTargetData;
 	}
 	
 	private static Boolean preferredInitialWrapConsoleData;
 	/**
 	 * Gets the preferred initial wrap console data.
 	 *
 	 * @return the preferred initial wrap console data
 	 */
 	protected static Boolean getPreferredInitialWrapConsoleData()
 	{
 		if ( preferredInitialWrapConsoleData == null )
 		{
 			preferredInitialWrapConsoleData =
 				getPreferences().getBoolean(InitialWrapConsoleData.name(), false);
 		}
 		return preferredInitialWrapConsoleData;
 	}
 	/**
 	 * Sets the preferred initial wrap console data.
 	 *
 	 * @param preferredInitialWrapConsoleData the new preferred initial wrap console data
 	 */
 	protected static void setPreferredInitialWrapConsoleData(Boolean preferredInitialWrapConsoleData)
 	{
 		getPreferences().putBoolean(InitialWrapConsoleData.name(), preferredInitialWrapConsoleData);
 		PreferencesManager.preferredInitialWrapConsoleData = preferredInitialWrapConsoleData;
 	}
 	
 	/**
 	 * Create the dialog.
 	 *
 	 * @param parent the parent
 	 * @param preferencesPath the preferences path
 	 */
 	public PreferencesManager(Frame parent, String preferencesPath)
 	{
 		super(parent);
 		setPreferencesPath(preferencesPath);
 		setTitle("PreferencesManager");
 		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
 		contentPanel.setBorder(new EmptyBorder(1, 1, 1, 2));
 		getContentPane().add(contentPanel);
 		final JTextField txtfldConsoleHistoryLines = new JTextField();
 		final JCheckBox chckbxInitialSourceEditMode = new JCheckBox("initially allow editing of source data");
 		final JCheckBox chckbxInitialWrapSourceData = new JCheckBox("initially wrap data in the source pane");
 		final JCheckBox chckbxInitialWrapTargetData = new JCheckBox("initially wrap data in the target pane");
 		final JCheckBox chckbxInitialWrapConsoleData = new JCheckBox("initially wrap data in the console pane");
 		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));
 		{
 			JTabbedPane tabbedPane = new JTabbedPane();
 			contentPanel.add(tabbedPane);
 			{
 				JPanel generalPanel = new JPanel();
 				tabbedPane.addTab("General", null, generalPanel, null);
 				GridBagLayout gbl_generalPanel = new GridBagLayout();
 				generalPanel.setLayout(gbl_generalPanel);
 				{
 					JLabel lblConsoleHistory = new JLabel("Console History");
 					GridBagConstraints gbc_lblConsoleHistory = new GridBagConstraints();
 					gbc_lblConsoleHistory.insets = new Insets(0, 0, 0, 4);
 					gbc_lblConsoleHistory.anchor = GridBagConstraints.WEST;
 					gbc_lblConsoleHistory.gridx = 0;
 					gbc_lblConsoleHistory.gridy = 0;
 					generalPanel.add(lblConsoleHistory, gbc_lblConsoleHistory);
 				}
 				{
 					JPanel consoleHistoryPanel = new JPanel();
 					GridBagConstraints gbc_consoleHistoryPanel = new GridBagConstraints();
 					gbc_consoleHistoryPanel.anchor = GridBagConstraints.WEST;
 					gbc_consoleHistoryPanel.gridx = 1;
 					gbc_consoleHistoryPanel.gridy = 0;
 					generalPanel.add(consoleHistoryPanel, gbc_consoleHistoryPanel);
 					consoleHistoryPanel.setLayout(new BoxLayout(consoleHistoryPanel, BoxLayout.X_AXIS));
 					{
 						txtfldConsoleHistoryLines.setText(""+getPreferredConsoleHistorySize());
 						consoleHistoryPanel.add(txtfldConsoleHistoryLines);
 						txtfldConsoleHistoryLines.setColumns(5);
 					}
 					{
 						JLabel lblConsoleHistoryLines = new JLabel(" lines");
 						consoleHistoryPanel.add(lblConsoleHistoryLines);
 					}
 				}
 				{
 					JLabel lblSourceEditMode = new JLabel("Source Edit Mode");
 					GridBagConstraints gbc_lblSourceEditMode = new GridBagConstraints();
 					gbc_lblSourceEditMode.anchor = GridBagConstraints.WEST;
 					gbc_lblSourceEditMode.gridx = 0;
 					gbc_lblSourceEditMode.gridy = 1;
 					generalPanel.add(lblSourceEditMode, gbc_lblSourceEditMode);
 				}
 				{
 					chckbxInitialSourceEditMode.setSelected(getPreferredInitialSourceEditMode());
 					GridBagConstraints gbc_chckbxInitialSourceEditMode = new GridBagConstraints();
 					gbc_chckbxInitialSourceEditMode.anchor = GridBagConstraints.WEST;
 					gbc_chckbxInitialSourceEditMode.gridx = 1;
 					gbc_chckbxInitialSourceEditMode.gridy = 1;
 					generalPanel.add(chckbxInitialSourceEditMode, gbc_chckbxInitialSourceEditMode);
 				}
 				{
 					JLabel lblSourceWrapMode = new JLabel("Source Wrap Mode");
 					GridBagConstraints gbc_lblSourceWrapMode = new GridBagConstraints();
 					gbc_lblSourceWrapMode.anchor = GridBagConstraints.WEST;
 					gbc_lblSourceWrapMode.gridx = 0;
 					gbc_lblSourceWrapMode.gridy = 2;
 					generalPanel.add(lblSourceWrapMode, gbc_lblSourceWrapMode);
 				}
 				{
 					chckbxInitialWrapSourceData.setSelected(getPreferredInitialWrapSourceData());
 					GridBagConstraints gbc_chckbxInitialWrapSourceData = new GridBagConstraints();
 					gbc_chckbxInitialWrapSourceData.anchor = GridBagConstraints.WEST;
 					gbc_chckbxInitialWrapSourceData.gridx = 1;
 					gbc_chckbxInitialWrapSourceData.gridy = 2;
 					generalPanel.add(chckbxInitialWrapSourceData, gbc_chckbxInitialWrapSourceData);
 				}
 				{
 					JLabel lblTargetWrapMode = new JLabel("Target Wrap Mode");
 					GridBagConstraints gbc_lblTargetWrapMode = new GridBagConstraints();
 					gbc_lblTargetWrapMode.anchor = GridBagConstraints.WEST;
 					gbc_lblTargetWrapMode.gridx = 0;
 					gbc_lblTargetWrapMode.gridy = 3;
 					generalPanel.add(lblTargetWrapMode, gbc_lblTargetWrapMode);
 				}
 				{
 					chckbxInitialWrapTargetData.setSelected(getPreferredInitialWrapTargetData());
 					GridBagConstraints gbc_chckbxInitialWrapTargetData = new GridBagConstraints();
 					gbc_chckbxInitialWrapTargetData.anchor = GridBagConstraints.WEST;
 					gbc_chckbxInitialWrapTargetData.gridx = 1;
 					gbc_chckbxInitialWrapTargetData.gridy = 3;
 					generalPanel.add(chckbxInitialWrapTargetData, gbc_chckbxInitialWrapTargetData);
 				}
 				{
 					JLabel lblConsoleWrapMode = new JLabel("Console Wrap Mode");
 					GridBagConstraints gbc_lblConsoleWrapMode = new GridBagConstraints();
 					gbc_lblConsoleWrapMode.anchor = GridBagConstraints.WEST;
 					gbc_lblConsoleWrapMode.gridx = 0;
 					gbc_lblConsoleWrapMode.gridy = 4;
 					generalPanel.add(lblConsoleWrapMode, gbc_lblConsoleWrapMode);
 				}
 				{
 					chckbxInitialWrapConsoleData.setSelected(getPreferredInitialWrapConsoleData());
 					GridBagConstraints gbc_chckbxInitialWrapConsoleData = new GridBagConstraints();
 					gbc_chckbxInitialWrapConsoleData.anchor = GridBagConstraints.WEST;
 					gbc_chckbxInitialWrapConsoleData.gridx = 1;
 					gbc_chckbxInitialWrapConsoleData.gridy = 4;
 					generalPanel.add(chckbxInitialWrapConsoleData, gbc_chckbxInitialWrapConsoleData);
 				}
 			}
 		}
 		{
 			JPanel buttonPane = new JPanel();
 			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT, 1, 0));
 			{
 				JButton okButton = new JButton("OK");
 				okButton.setActionCommand("OK");
 				buttonPane.add(okButton);
 				okButton.addActionListener
 				(
 					new ActionListener()
 					{
 						@Override
 						public void actionPerformed(ActionEvent e)
 						{
 							setPreferredConsoleHistorySize(txtfldConsoleHistoryLines.getText());
 							setPreferredInitialSourceEditMode(chckbxInitialSourceEditMode.isSelected());
 							setPreferredInitialWrapSourceData(chckbxInitialWrapSourceData.isSelected());
 							setPreferredInitialWrapTargetData(chckbxInitialWrapTargetData.isSelected());
 							setPreferredInitialWrapConsoleData(chckbxInitialWrapConsoleData.isSelected());
 							setVisible(false);
 						}
 					}
 				);
 			}
 			{
 				JButton cancelButton = new JButton("Cancel");
 				cancelButton.setActionCommand("Cancel");
 				buttonPane.add(cancelButton);
 				cancelButton.addActionListener
 				(
 					new ActionListener()
 					{
 						@Override
 						public void actionPerformed(ActionEvent e)
 						{
 							setVisible(false);
 						}
 					}
 				);
 			}
 			getContentPane().add(buttonPane);
 		}
 		pack();
 		setLocationRelativeTo(parent);
 	}
 }
 
