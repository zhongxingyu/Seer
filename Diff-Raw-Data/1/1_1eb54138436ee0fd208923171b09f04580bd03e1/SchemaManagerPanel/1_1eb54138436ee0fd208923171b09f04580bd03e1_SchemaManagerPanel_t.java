 /*
  * Copyright (C) 2006-2010 Thomas Chemineau
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
 import net.aepik.casl.core.SchemaManager;
 import net.aepik.casl.ui.ManagerFrame;
 import net.aepik.casl.ui.util.SimpleTabbedPaneUI;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.util.Hashtable;
 import javax.swing.BorderFactory;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JSeparator;
 import javax.swing.JTabbedPane;
 import javax.swing.KeyStroke;
 import javax.swing.SingleSelectionModel;
 
 public class SchemaManagerPanel extends JPanel
 {
 
 	private static final long serialVersionUID = 0;
 
 	/**
 	 * L'item de menu Ouvrir un fichier
 	 */
 	public JMenuItem item_openFile = new JMenuItem( "Ouvrir..." );
 
 	/**
 	 * L'item de menu Fermer
 	 */
 	public JMenuItem item_closeFile = new JMenuItem( "Fermer" );
 
 	/**
 	 * L'item de menu Fermer tout
 	 */
 	public JMenuItem item_closeAllFiles = new JMenuItem( "Fermer tout" );
 
 	/**
 	 * L'item de menu Enregistrer
 	 */
 	public JMenuItem item_saveFile = new JMenuItem( "Enregistrer..." );
 
 	/**
 	 * L'item de menu Renommer
 	 */
 	public JMenuItem item_renameFile = new JMenuItem( "Renommer..." );
 
 	/**
 	 * L'item de menu Chercher
 	 */
 	public JMenuItem item_search = new JMenuItem("Chercher");
 
 	/**
 	 * L'item de menu Propriété
 	 */
 	public JMenuItem item_propriety = new JMenuItem( "Propriétés" );
 
 	/**
 	 * L'item de menu Renommer pour le popup
 	 */
 	public JMenuItem item_renameFile2 = new JMenuItem( "Renommer..." );
 
 	/**
 	 * L'item de menu Chercher pour le popup
 	 */
 	public JMenuItem item_search2 = new JMenuItem("Chercher");
 
 	/**
 	 * L'item de menu Convertir pour le popup
 	 */
 	public JMenuItem item_propriety2 = new JMenuItem( "Propriétés" );
 
 	/**
 	 * L'item de menu fermer pour le popup
 	 */
 	public JMenuItem item_closeFile2 = new JMenuItem( "Fermer" );
 
 	/**
 	 * L'item de menu Enregistrer pour le popup
 	 */
 	public JMenuItem item_saveFile2 = new JMenuItem( "Enregistrer..." );
 
 	/**
 	 * Le manager de schemas
 	 */
 	private SchemaManager schemaManager ;
 
 	/**
 	 * Le listener du manager de schémas
 	 */
 	private SchemaManagerListener schemaManagerListener ;
 
 	/**
 	 * L'onglet contenant tous les schemas
 	 */
 	private JTabbedPane onglets ;
 
 	/**
 	 * Le menu Popup pour le système d'onglet
 	 */
 	private JPopupMenu popupMenu ;
 
 	/**
 	 * Build a new SchemaManagerPanel object.
 	 * @param sm A SchemaManager object.
 	 */
 	public SchemaManagerPanel ( SchemaManager sm )
 	{
 		super( new BorderLayout() );
 		schemaManager = sm;
 		popupMenu = new JPopupMenu();
 		onglets = new JTabbedPane();
 		initFrame();
 		updateButtonsStatus();
 	}
 
 	/**
 	 * Ajoute un listener pour cet objet.
 	 * @param listener Un objet SchemaManagerListener.
 	 */
 	public void addSchemaManagerListener ( SchemaManagerListener l )
 	{
 		item_openFile.addActionListener(l);
 		item_closeFile.addActionListener(l);
 		item_closeAllFiles.addActionListener(l);
 		item_saveFile.addActionListener(l);
 		item_propriety.addActionListener(l);
 		item_renameFile.addActionListener(l);
 		item_search.addActionListener(l);
 
 		item_closeFile2.addActionListener(l);
 		item_propriety2.addActionListener(l);
 		item_renameFile2.addActionListener(l);
 		item_saveFile2.addActionListener(l);
 		item_search2.addActionListener(l);
 
 		onglets.addChangeListener(l);
 		onglets.addMouseListener(l);
 
 		schemaManagerListener = l;
 	}
 
 	/**
 	 * Retourne le schemaPanel sélectionné.
 	 * @return SchemaPanel Un SchemaPanel sélectionné dans le système d'onglets.
 	 */
 	public SchemaPanel getSelectedSchemaPanel ()
 	{
 		SchemaPanel p = null ;
 		try
 		{
 			p = (SchemaPanel) onglets.getSelectedComponent();
 		}
 		catch (Exception e) {}
 		return p;
 	}
 
 	/**
 	 * Retourne l'identifiant du schemaPanel sélectionné.
 	 * @return SchemaPanel Un identifiant.
 	 */
 	public String getSelectedSchemaPanelId ()
 	{
 		String s = null;
 		try
 		{
 			s = onglets.getTitleAt(onglets.getSelectedIndex());
 		}
 		catch (Exception e) {}
 		return s;
 	}
 
 	/**
 	 * Supprime tous les SchemaPanel dans le système d'onglet.
 	 */
 	public void removeAll ()
 	{
 		onglets.removeAll();
 	}
 
 	/**
 	 * Supprime un listener pour cet objet.
 	 * @param listener Un objet SchemaManagerListener.
 	 */
 	public void removeSchemaManagerListener ( SchemaManagerListener l )
 	{
 		item_openFile.removeActionListener(l);
 		item_closeFile.removeActionListener(l);
 		item_closeAllFiles.removeActionListener(l);
 		item_saveFile.removeActionListener(l);
 		item_propriety.removeActionListener(l);
 		item_renameFile.removeActionListener(l);
 		item_search.removeActionListener(l);
 
 		item_closeFile2.removeActionListener(l);
 		item_propriety2.removeActionListener(l);
 		item_renameFile2.removeActionListener(l);
 		item_saveFile2.removeActionListener(l);
 		item_search2.removeActionListener(l);
 
 		onglets.removeChangeListener(l);
 		onglets.removeMouseListener(l);
 
 		schemaManagerListener = null ;
 	}
 
 	/**
 	 * Selectionne le SchemaPanel d'identifiant id.
 	 * @param id Un identifiant pour désigner le schéma à sélectionner.
 	 */
 	public void selectSchemaPanel ( String id )
 	{
 		int index = onglets.indexOfTab(id);
 		if (index != -1)
 		{
 			onglets.setSelectedIndex(index);
 			schemaManager.setCurrentSchema(id);
 			//for( int i=0; i<onglets.getTabCount(); i++ ) {
 			//	if( onglets.getTitleAt( i ).equals( id ) ) {
 			//		onglets.setBackgroundAt( i, null );
 			//		onglets.setForegroundAt( i, null );
 			//	} else {
 			//		onglets.setBackgroundAt( i, new Color( 236, 232, 224 ) );
 			//		onglets.setForegroundAt( i, Color.gray );
 			//	}
 			//}
 		}
 	}
 
 	/**
 	 * Affiche le menu contextuel pour l'objet indiqué.
 	 * @param e Le composant sur lequel afficher le menu popup.
 	 * @param x La coordonnée x du menu popup.
 	 * @param y La coordonnée y du menu popup.
 	 */
 	public void showPopupMenu ( Component e, int x, int y )
 	{
 		if (e != null && schemaManager.getNbSchemas() != 0)
 		{
 			try
 			{
 				popupMenu.show(e, x, y);
 			}
 			catch (NullPointerException ex) {}
 		}
 	}
 
 	/**
 	 * Met à jour le status enabled/disabled des boutons.
 	 */
 	public void updateButtonsStatus ()
 	{
 		if (onglets.getTabCount() > 1)
 		{
 			item_closeFile.setEnabled(true);
 			item_closeAllFiles.setEnabled(true);
 			item_saveFile.setEnabled(true);
 			item_propriety.setEnabled(true);
 			item_renameFile.setEnabled(true);
 			item_search.setEnabled(true);
 		}
 		else if (onglets.getTabCount() == 1)
 		{
 			item_closeFile.setEnabled(true);
 			item_closeAllFiles.setEnabled(false);
 			item_saveFile.setEnabled(true);
 			item_propriety.setEnabled(true);
 			item_renameFile.setEnabled(true);
 			item_search.setEnabled(true);
 		}
 		else
 		{
 			item_closeFile.setEnabled(false);
 			item_closeAllFiles.setEnabled(false);
 			item_saveFile.setEnabled(false);
 			item_propriety.setEnabled(false);
 			item_renameFile.setEnabled(false);
 			item_search.setEnabled(false);
 		}
 	}
 
 	/**
 	 * Met à jour les onglets.
 	 */
 	public void updateTabs ()
 	{
 		//
 		// On regarde quels sont les schémas qui ont disparus.
 		// On supprime ceux qui sont toujours dans la vue
 		// mais plus dans le manager.
 		//
 		for (int i = onglets.getTabCount() - 1; i >= 0; i--)
 		{
 			if (!schemaManager.isSchemaIdExists(onglets.getTitleAt(i)))
 			{
 				onglets.remove(i);
 			}
 		}
 
 		//
 		// On regarde quels sont les schémas qui sont apparus.
 		// On ajoute ceux qui sont dans le manager mais pas dans la vue.
 		//
 		Schema[] s = schemaManager.getSchemas();
 		String[] k = schemaManager.getSchemaIds();
 		for (int i = 0; i < s.length; i++)
 		{
 			if (onglets.indexOfTab(k[i]) == -1)
 			{
 				SchemaPanel p = new SchemaPanel(s[i]);
 				SchemaListener l = new SchemaListener(schemaManagerListener, s[i], p);
 				p.addSchemaListener(l);
 				onglets.add(k[i], p);
 			}
 		}
 	}
 
 	private void initFrame ()
 	{
 		//
 		// Menu Popup
 		//
 		popupMenu.add(item_renameFile2);
 		popupMenu.add(item_saveFile2);
 		popupMenu.add(item_closeFile2);
 		popupMenu.addSeparator();
 		popupMenu.add(item_search2);
 		popupMenu.add(item_propriety2);
 
 		//
 		// Onglets
 		//
 		SimpleTabbedPaneUI mtpui = new SimpleTabbedPaneUI();
 		onglets.setUI(mtpui);
 		onglets.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
 		JPanel ongletsPanel = new JPanel(new BorderLayout());
 		ongletsPanel.add(onglets, BorderLayout.CENTER);
 
 		//
 		// Organisation Générale
 		//
 		super.add(ongletsPanel);
 
 		//
 		// Raccourcis Clavier
 		//
 		item_openFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
 		item_saveFile.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
 		item_closeFile.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));
 	}
 }
 
