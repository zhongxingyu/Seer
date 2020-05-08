 /*
  * Copyright (C) 2006 Thomas Chemineau
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 package net.aepik.casl.ui.ldap;
 
 import net.aepik.casl.core.ldap.Schema;
 import net.aepik.casl.core.ldap.SchemaObject;
 import net.aepik.casl.core.ldap.SchemaSyntax;
 import net.aepik.casl.core.ldap.SchemaValue;
 import net.aepik.casl.core.util.Config;
 import net.aepik.casl.core.sddl.SDDL_ACLString;
 import net.aepik.casl.ui.util.NoEditableTableModel;
 import net.aepik.casl.ui.sddl.SDDL_ACLEditListener;
 import org.jdesktop.swingx.JXHeader;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Vector;
 import javax.accessibility.Accessible;
 import javax.accessibility.AccessibleContext;
 import javax.swing.BorderFactory;
 import javax.swing.DefaultCellEditor;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.JScrollPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 /**
  * Fenêtre qui permet de visualiser les données d'un objet du schéma.
  */
 public class SchemaObjectEditorFrame extends JFrame implements ActionListener, WindowListener
 {
 
 	private static final long serialVersionUID = 0;
 
 	/**
 	 * Le schéma concerné
 	 */
 	private Schema schema;
 
 	/**
 	 * L'object concerné
 	 */
 	private SchemaObject objetSchema;
 
 	/**
 	 * La fenêtre appelante
 	 */
 	private JFrame mainFrame;
 
 	/**
 	 * La liste des valeurs
 	 */
 	private Hashtable<String,JComponent> values;
 
 	/**
 	 * La liste des labels
 	 */
 	private Hashtable<String,JComponent> labels;
 
 	/**
 	 * La liste des valeurs présentes
 	 */
 	private Hashtable<String,JCheckBox> valuesPresent;
 
 	/**
 	 * Le bouton Ok
 	 */
 	private JButton boutonOk = new JButton("Valider");
 
 	/**
 	 * Le bouton Annuler
 	 */
 	private JButton boutonAnnuler = new JButton("Annuler");
 
 	/**
 	 * Build a new SchemaObjectEditorFrame.
 	 * @param j The parent frame
 	 * @param s The schema where is the object to edit
 	 * @param so The object to edit
 	 */
 	public SchemaObjectEditorFrame ( JFrame f, Schema s, SchemaObject so )
 	{
 		super();
 		schema = s;
 		objetSchema = so;
 		mainFrame = f;
 		init();
 		build();
 	}
 
 	/**
 	 * Gère les actions de la vue et permet de modifier les données.
 	 * @param e L'action soulevée par un élément du panel.
 	 */
 	public void actionPerformed ( ActionEvent e )
 	{
 		Object o = e.getSource();
 		if (o == boutonOk)
 		{
 			if (this.checkNoBlankValues())
 			{
 				this.saveValues();
 				this.windowClosing(null);
 				this.schema.notifyUpdates(true);
 			}
 			else
 			{
 				JOptionPane.showMessageDialog(
 					this,
 					"Impossible de spécifier des valeurs nulles",
 					"Erreur",
 					JOptionPane.ERROR_MESSAGE
 				);
 			}
 		}
 		else if (o == boutonAnnuler)
 		{
 			this.windowClosing(null);
 		}
 	}
 
 	/**
 	 * Build frame.
 	 */
 	private void build ()
 	{
 		setTitle( "Propriétés de l'objet " + objetSchema.getId());
 		setSize( 700, 400 );
 		setResizable( false );
 		setLocationRelativeTo( mainFrame );
 
 		if( mainFrame!=null )
 			setIconImage( mainFrame.getIconImage() );
 
 		// - Panel bouton du bas -
 
 		JPanel boutonsPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
 		boutonsPanel.add( boutonOk );
 		boutonsPanel.add( boutonAnnuler );
 
 		// - Description -
 
 		JTextArea textAreaValues = new JTextArea(
 				"Vous pouvez apporter des modifications sur les paramêtres"
 				+ " de cet objet d'identifiant " + objetSchema.getId() + ". Les"
 				+ " modifications apportées à des paramêtres non-cochés ne"
 				+ " seront pas prises en compte." );
 		textAreaValues.setEditable( false );
 		textAreaValues.setLineWrap( true );
 		textAreaValues.setWrapStyleWord( true );
 		textAreaValues.setFont( (new JLabel()).getFont() );
 		textAreaValues.setBorder( BorderFactory.createEmptyBorder( 7, 8, 7, 8 ) );
 		textAreaValues.setBackground( (new JLabel()).getBackground() );
 
 		// - La table des valeurs -
 		// Correction : Tri alphabétique des valeurs
 
 		Enumeration<String> k = values.keys();
 		String[] keys = new String[ values.size() ];
 
 		for( int i=0; i<keys.length; i++ )
 			keys[i] = k.nextElement();
 		Arrays.sort( keys );
 
 		JPanel colonne1 = new JPanel( new GridLayout( keys.length, 1 ) );
 		JPanel colonne2 = new JPanel( new GridLayout( keys.length, 1 ) );
 
 		// Enumeration<JComponent> l = labels.elements();
 		// Enumeration<JComponent> v = values.elements();
 		// Enumeration<String> k = values.keys();
 		// JPanel colonne1 = new JPanel( new GridLayout( values.size(), 1 ) );
 		// JPanel colonne2 = new JPanel( new GridLayout( values.size(), 1 ) );
 
 		for( int i=0; keys!=null && i<keys.length; i++ ) {
 
 		// while( l.hasMoreElements() && v.hasMoreElements() && k.hasMoreElements() ) {
 
 			String key = keys[i];
 			JComponent label = labels.get( key );
 			JComponent value = values.get( key );
 			JCheckBox checkbox = valuesPresent.get( key );
 
 			// String key = k.nextElement();
 			// JComponent label = l.nextElement();
 			// JComponent value = v.nextElement();
 			// JCheckBox checkbox = valuesPresent.get( key );
 
 			if( !value.isEnabled() && value instanceof JTextField ) {
 
 				//value.setEnabled( true );
 
 				JButton b = new JButton( "..." );
 				b.setBorder( BorderFactory.createCompoundBorder(
 						BorderFactory.createMatteBorder( 0, 5, 0, 0, Color.white ),
 						b.getBorder() ) );
 				b.addActionListener( new SchemaValueEditorLauncher(
 						b, (JTextField) value, objetSchema, key ) );
 
 				JPanel tmp = new JPanel( new BorderLayout() );
 				tmp.add( value, BorderLayout.CENTER );
 				tmp.add( b, BorderLayout.EAST );
 				tmp.setOpaque( false );
 				value = tmp ;
 			}
 
 			JPanel panelTmp1 = new JPanel( new BorderLayout() );
 			panelTmp1.add( checkbox, BorderLayout.WEST );
 			panelTmp1.add( label, BorderLayout.CENTER );
 			panelTmp1.setBorder( BorderFactory.createEmptyBorder( 2, 0, 2, 3 ) );
 			colonne1.add( panelTmp1 );
 
 			JPanel panelTmp2 = new JPanel( new BorderLayout() );
 			panelTmp2.add( value, BorderLayout.CENTER );
 			panelTmp2.setBorder( BorderFactory.createEmptyBorder( 2, 3, 2, 3 ) );
 			colonne2.add( panelTmp2 );
 
 			checkbox.setOpaque( false );
 			label.setOpaque( false );
 			value.setOpaque( false );
 			panelTmp1.setOpaque( false );
 			panelTmp2.setOpaque( false );
 		}
 
 		JPanel tablePanel = new JPanel( new BorderLayout() );
 		tablePanel.add( colonne1, BorderLayout.WEST );
 		tablePanel.add( colonne2, BorderLayout.EAST );
 		tablePanel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
 
 		colonne1.setOpaque( false );
 		colonne2.setOpaque( false );
 		tablePanel.setOpaque( false );
 
 		JPanel tablePanelContainer = new JPanel( new BorderLayout() );
 		tablePanelContainer.add( tablePanel, BorderLayout.NORTH );
 		tablePanelContainer.setBackground( Color.white );
 
 		JList tmpForBorderList = new JList();
 		JScrollPane tmpForBorderScroller = new JScrollPane( tmpForBorderList );
 
 		JScrollPane tableScroller = new JScrollPane( tablePanelContainer );
 		//tableScroller.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );
 		tableScroller.setBorder( BorderFactory.createCompoundBorder(
 				BorderFactory.createEmptyBorder( 5, 6, 1, 6 ),
 				BorderFactory.createCompoundBorder(
 					BorderFactory.createTitledBorder( " Listes des paramêtres " ),
 					BorderFactory.createCompoundBorder(
 						BorderFactory.createEmptyBorder( 0, 5, 5, 5 ),
 						tmpForBorderScroller.getBorder() ) ) ) );
 
 		// - Organisation générale -
 
 		JPanel mainPanel = new JPanel( new BorderLayout() );
 		mainPanel.add( textAreaValues, BorderLayout.NORTH );
 		mainPanel.add( tableScroller, BorderLayout.CENTER );
 
 		JPanel mainPanelContainer = new JPanel( new BorderLayout() );
 		mainPanelContainer.add( mainPanel, BorderLayout.CENTER );
 		mainPanelContainer.add( boutonsPanel, BorderLayout.SOUTH );
 		mainPanelContainer.setBorder( BorderFactory.createEmptyBorder( 2, 1, 1, 1 ) );
 
 		getContentPane().add( mainPanelContainer );
 
 		// - Listeners -
 
 		addWindowListener( this );
 		boutonOk.addActionListener( this );
 		boutonAnnuler.addActionListener( this );
 	}
 
 	/**
 	 * Teste si aucune valeur est vide.
 	 * @return boolean True si toutes les valeurs sont ok, c'est à dire non vide.
 	**/
 	private boolean checkNoBlankValues ()
 	{
 		boolean erreur = false;
 		Enumeration<JComponent> v = values.elements();
 		Enumeration<JCheckBox> c = valuesPresent.elements();
 		while (!erreur && v.hasMoreElements())
 		{
 			JComponent composant = v.nextElement();
 			JCheckBox checkbox = c.nextElement();
 			if (checkbox.isSelected() && composant instanceof JTextField && ((JTextField) composant).getText().length() == 0)
 			{
 				erreur = true;
 			}
 		}
 		return !erreur;
 	}
 
 	/**
 	 * Initialize graphical components with values.
 	 */
 	private void init ()
 	{
 		String objectType = this.objetSchema.getType();
 		SchemaSyntax syntax = objetSchema.getSyntax();
 		String[] params_name = syntax.getParameters(objectType);
 
 		if (params_name == null || params_name.length == 0)
 		{
 			return;
 		}
 
 		this.labels = new Hashtable<String,JComponent>();
 		this.values = new Hashtable<String,JComponent>();
 		this.valuesPresent = new Hashtable<String,JCheckBox>();
 		Vector<String> parametresEffectues = new Vector<String>();
 
 		for (int i = 0; i < params_name.length; i++)
 		{
 			if (parametresEffectues.contains(params_name[i]))
 			{
 				continue;
 			}
 
 			JComponent label = null ;
 			JComponent composant = null ;
 			JCheckBox checkbox = new JCheckBox();
 			String[] param_values;
 			String[] param_others = syntax.getOthersParametersFor(objectType, params_name[i]);
 
 			// If SUP parameters, add corresponding object names.
 			if (params_name[i].compareTo("SUP") == 0)
 			{
 	                        SchemaObject[] objects = this.schema.getObjectsInOrder(objectType);
 				param_values = new String[objects.length];
 				for (int j = 0; j < objects.length; j++)
 				{
 					param_values[j] = objects[j].getNameFirstValue();
 				}
 			}
 
 			// Else, read the syntax to find needed values.
 			else
 			{
 				param_values = syntax.getParameterDefaultValues(objectType, params_name[i]);
 			}
 
 			// Plusieurs clefs.
 			// C'est une liste de clefs possibles.
 			if (param_others != null && param_others.length > 1)
 			{
 				label = new JComboBox(param_others);
 				composant = new JLabel();
 				boolean ok = false;
 
 				for (int j = 0; j < param_others.length && !ok; j++)
 				{
 					if (objetSchema.isKeyExists(param_others[j]))
 					{
 						ok = true;
 						((JComboBox) label).setSelectedItem(param_others[j]);
 						checkbox.setSelected(true);
 					}
 				}
 				for (int j = 0; j < param_others.length; j++)
 				{
 					parametresEffectues.add(param_others[j]);
 				}
 
 			}
 
 			// Aucune valeur possible
 			// La présence du paramêtre suffit => JCheckBox.
 			else if (param_values.length == 0)
 			{
 				label = new JLabel(params_name[i]);
 				composant = new JLabel("");
 				parametresEffectues.add(params_name[i]);
 				checkbox.setSelected(objetSchema.isKeyExists(params_name[i]));
 
 			}
 
 			// Une valeur, on regarde si c'est une valeur précise.
 			// - Si c'est une chaîne qui peut être quelconque => JTextField.
 			// - Si la valeur est en fait un objet => JTextField + Objet.
 			else if (param_values.length == 1 && param_values[0] != null)
 			{
 				String tmp = objetSchema.isKeyExists( params_name[i] )
 				           ? objetSchema.getValue( params_name[i] ).toString()
 				           : param_values[0];
 
 				label = new JLabel(params_name[i]);
 				composant = new JTextField(tmp, 30);
 				parametresEffectues.add(params_name[i]);
 				checkbox.setSelected(objetSchema.isKeyExists(params_name[i]));
 
 			}
 
 			// Plusieurs valeurs.
 			// C'est une liste de choix possibles => JComboBox.
 			else if (param_values.length > 1)
 			{
 				String tmp = objetSchema.isKeyExists(params_name[i])
 				           ? objetSchema.getValue(params_name[i]).toString()
 				           : param_values[0];
 
 				label = new JLabel(params_name[i]);
 				composant = new JComboBox(param_values);
 				((JComboBox) composant).setSelectedItem(tmp);
 				parametresEffectues.add(params_name[i]);
 				checkbox.setSelected(objetSchema.isKeyExists(params_name[i]));
 			}
 
 			// Set elements into the frame.
 			if (composant != null && label != null)
 			{
 				this.labels.put(params_name[i], label);
 				this.values.put(params_name[i], composant);
 				this.valuesPresent.put(params_name[i], checkbox);
 				SchemaValue v = syntax.createSchemaValue(objectType, params_name[i], null);
 				if (v.isValues() && !(composant instanceof JComboBox))
 				{
 					composant.setEnabled(false);
 				}
 			}
 		} // end for
 	}
 
 	/**
 	 * Save values into the object.
 	**/
 	private void saveValues ()
 	{
 		Enumeration<String> k = values.keys();
 		Enumeration<JComponent> l = labels.elements();
 		Enumeration<JComponent> v = values.elements();
 		Enumeration<JCheckBox> c = valuesPresent.elements();
 		SchemaSyntax syntax = objetSchema.getSyntax();
 
 		while (v.hasMoreElements() && k.hasMoreElements())
 		{
 			String key = k.nextElement();
 			JComponent label = l.nextElement();
 			JComponent composant = v.nextElement();
 			JCheckBox checkbox = c.nextElement();
 
 			// If SUP key, we have to modify the parent of this object.
 			// SUP element should be a JTextField.
 			if (key.compareToIgnoreCase("SUP") == 0 && composant instanceof JComboBox)
 			{
 				SchemaObject parent = null;
 				if (checkbox.isSelected())
 				{
 					String parentName = ((JComboBox) composant).getSelectedItem().toString().trim();
 					parent = parentName.length() > 0 ? schema.getObjectByName(parentName) : null;
 				}
 				objetSchema.setParent(parent);
 			}
 
 			// Si la checkbox n'est pas sélectionnée, il faut regarder si les
 			// valeurs sont présentes dans l'objet, auquel cas on les supprime.
 			if (!checkbox.isSelected())
 			{
 				if (label instanceof JComboBox)
 				{
 					objetSchema.delValue(((JComboBox) label).getSelectedItem().toString());
 				}
 				else
 				{
 					objetSchema.delValue(key);
 				}
 			}
 
 			// Ajout => deux possiblités :
 			// - la clef est simple = c'est un JLabel
 			// - la clef est multiple = c'est un JComboBox.
 			else
 			{
 				if (label instanceof JLabel)
 				{
 					if (composant instanceof JLabel)
 					{
 						SchemaValue sv = syntax.createSchemaValue(objetSchema.getType(), key, "");
 						objetSchema.delValue(((JLabel) label).getText());
 						objetSchema.addValue(((JLabel) label).getText(), sv);
 					}
 					else if (composant instanceof JTextField)
 					{
 						SchemaValue sv = syntax.createSchemaValue(objetSchema.getType(), key,
 							((JTextField) composant).getText()
 						);
 						objetSchema.delValue(((JLabel) label).getText());
 						objetSchema.addValue(((JLabel) label).getText(), sv);
 					}
 					else if (composant instanceof JComboBox)
 					{
 						SchemaValue sv = syntax.createSchemaValue(objetSchema.getType(), key,
 							((JComboBox) composant).getSelectedItem().toString());
 						objetSchema.delValue(((JLabel) label).getText());
 						objetSchema.addValue(((JLabel) label).getText(), sv);
 					}
 				}
 				else if (label instanceof JComboBox)
 				{
 					SchemaValue sv = syntax.createSchemaValue(objetSchema.getType(),
 						((JComboBox) label).getSelectedItem().toString(), "");
 					objetSchema.delValue(((JComboBox) label).getSelectedItem().toString());
 					objetSchema.addValue(((JComboBox) label).getSelectedItem().toString(), sv);
 				}
 			}
 		}
 	}
 
 	public void windowActivated ( WindowEvent e )
 	{
 	}
 
 	public void windowClosed ( WindowEvent e )
 	{
 	}
 
 	public void windowClosing ( WindowEvent e )
 	{
 		this.setVisible(false);
 	}
 
 	public void windowDeactivated ( WindowEvent e )
 	{
 	}
 
 	public void windowDeiconified ( WindowEvent e )
 	{
 	}
 
 	public void windowIconified ( WindowEvent e )
 	{
 	}
 
 	public void windowOpened ( WindowEvent e )
 	{
 	}
 
 }
