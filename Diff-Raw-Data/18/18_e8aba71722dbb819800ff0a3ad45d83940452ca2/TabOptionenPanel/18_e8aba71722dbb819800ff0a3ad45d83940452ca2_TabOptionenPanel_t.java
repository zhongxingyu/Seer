 // %3158012967:de.hattrickorganizer.gui.menu.option%
 package de.hattrickorganizer.gui.menu.option;
 
 import de.hattrickorganizer.gui.templates.ImagePanel;
 import de.hattrickorganizer.model.OptionManager;
 
 import java.awt.GridLayout;
 import java.awt.event.ItemEvent;
 
 import javax.swing.JCheckBox;
 
 
 /**
  * Alle weiteren Optionen, die Keine Formeln sind
  */
 final class TabOptionenPanel extends ImagePanel implements java.awt.event.ItemListener {
     //~ Instance fields ----------------------------------------------------------------------------
 
 	private static final long serialVersionUID = 1L;
 //	private JCheckBox m_jchArenasizer;
     private JCheckBox m_jchAufstellung;
     private JCheckBox m_jchInformation;
     private JCheckBox m_jchLigatabelle;
     private JCheckBox m_jchSpiele;
     private JCheckBox m_jchSpielerAnalyse;
 
     //private ComboBoxPanel       m_jcbHTIP           = null;
     private JCheckBox m_jchSpieleruebersicht;
     private JCheckBox m_jchStatistik;
     private JCheckBox m_jchTransferscout;
 
     //~ Constructors -------------------------------------------------------------------------------
 
     /**
      * Creates a new TabOptionenPanel object.
      */
     protected TabOptionenPanel() {
         initComponents();
     }
 
     //~ Methods ------------------------------------------------------------------------------------
 
     /**
      * TODO Missing Method Documentation
      *
      * @param itemEvent TODO Missing Method Parameter Documentation
      */
     public final void itemStateChanged(ItemEvent itemEvent) {
     	
     	// New Tab can not be shown immediately
     	if (itemEvent.getStateChange()== ItemEvent.SELECTED)
     		OptionManager.instance().setRestartNeeded();
     	// ReInit deletes the deselected Tab
     	if (itemEvent.getStateChange()== ItemEvent.DESELECTED)
     		OptionManager.instance().setReInitNeeded();
 
         gui.UserParameter.temp().tempTabSpieleruebersicht = !m_jchSpieleruebersicht.isSelected();
         gui.UserParameter.temp().tempTabAufstellung = !m_jchAufstellung.isSelected();
         gui.UserParameter.temp().tempTabLigatabelle = !m_jchLigatabelle.isSelected();
         gui.UserParameter.temp().tempTabSpiele = !m_jchSpiele.isSelected();
         gui.UserParameter.temp().tempTabSpieleranalyse = !m_jchSpielerAnalyse.isSelected();
         gui.UserParameter.temp().tempTabStatistik = !m_jchStatistik.isSelected();
         gui.UserParameter.temp().tempTabTransferscout = !m_jchTransferscout.isSelected();
 //        gui.UserParameter.temp().tempTabArenasizer = !m_jchArenasizer.isSelected();
         gui.UserParameter.temp().tempTabInformation = !m_jchInformation.isSelected();
     }
 
     /**
      * TODO Missing Method Documentation
      */
     private void initComponents() {
         setLayout(new GridLayout(9, 1, 4, 4));
 
         //        m_jcbHTIP= new ComboBoxPanel( model.HOVerwaltung.instance().getLanguageString( "Hattrick" ), HT_IP_ADRESSEN, 120 );
         //        m_jcbHTIP.setSelectedItem ( gui.UserParameter.temp ().htip );
         //        m_jcbHTIP.addItemListener ( this );
         //        add( m_jcbHTIP );
         m_jchSpieleruebersicht = new JCheckBox(de.hattrickorganizer.model.HOVerwaltung.instance().getLanguageString("Spieleruebersicht"));
         m_jchSpieleruebersicht.setToolTipText(de.hattrickorganizer.model.HOVerwaltung.instance().getLanguageString("tt_Optionen_TabManagement"));
         m_jchSpieleruebersicht.setOpaque(false);
         m_jchSpieleruebersicht.setSelected(!gui.UserParameter.temp().tempTabSpieleruebersicht);
         m_jchSpieleruebersicht.addItemListener(this);
         add(m_jchSpieleruebersicht);
 
         m_jchAufstellung = new JCheckBox(de.hattrickorganizer.model.HOVerwaltung.instance().getLanguageString("Aufstellung"));
         m_jchAufstellung.setToolTipText(de.hattrickorganizer.model.HOVerwaltung.instance().getLanguageString("tt_Optionen_TabManagement"));
         m_jchAufstellung.setOpaque(false);
         m_jchAufstellung.setSelected(!gui.UserParameter.temp().tempTabAufstellung);
         m_jchAufstellung.addItemListener(this);
         add(m_jchAufstellung);
 
         m_jchLigatabelle = new JCheckBox(de.hattrickorganizer.model.HOVerwaltung.instance().getLanguageString("Ligatabelle"));
         m_jchLigatabelle.setToolTipText(de.hattrickorganizer.model.HOVerwaltung.instance().getLanguageString("tt_Optionen_TabManagement"));
         m_jchLigatabelle.setOpaque(false);
         m_jchLigatabelle.setSelected(!gui.UserParameter.temp().tempTabLigatabelle);
         m_jchLigatabelle.addItemListener(this);
         add(m_jchLigatabelle);
 
         m_jchSpiele = new JCheckBox(de.hattrickorganizer.model.HOVerwaltung.instance().getLanguageString("Spiele"));
         m_jchSpiele.setToolTipText(de.hattrickorganizer.model.HOVerwaltung.instance().getLanguageString("tt_Optionen_TabManagement"));
         m_jchSpiele.setOpaque(false);
         m_jchSpiele.setSelected(!gui.UserParameter.temp().tempTabSpiele);
         m_jchSpiele.addItemListener(this);
         add(m_jchSpiele);
 
         m_jchSpielerAnalyse = new JCheckBox(de.hattrickorganizer.model.HOVerwaltung.instance().getLanguageString("SpielerAnalyse"));
         m_jchSpielerAnalyse.setToolTipText(de.hattrickorganizer.model.HOVerwaltung.instance().getLanguageString("tt_Optionen_TabManagement"));
         m_jchSpielerAnalyse.setOpaque(false);
         m_jchSpielerAnalyse.setSelected(!gui.UserParameter.temp().tempTabSpieleranalyse);
         m_jchSpielerAnalyse.addItemListener(this);
         add(m_jchSpielerAnalyse);
 
         m_jchStatistik = new JCheckBox(de.hattrickorganizer.model.HOVerwaltung.instance().getLanguageString("Statistik"));
         m_jchStatistik.setToolTipText(de.hattrickorganizer.model.HOVerwaltung.instance().getLanguageString("tt_Optionen_TabManagement"));
         m_jchStatistik.setOpaque(false);
         m_jchStatistik.setSelected(!gui.UserParameter.temp().tempTabStatistik);
         m_jchStatistik.addItemListener(this);
         add(m_jchStatistik);
 
        m_jchTransferscout = new JCheckBox(de.hattrickorganizer.model.HOVerwaltung.instance().getLanguageString("Transfers"));
         m_jchTransferscout.setToolTipText(de.hattrickorganizer.model.HOVerwaltung.instance().getLanguageString("tt_Optionen_TabManagement"));
         m_jchTransferscout.setOpaque(false);
         m_jchTransferscout.setSelected(!gui.UserParameter.temp().tempTabTransferscout);
         m_jchTransferscout.addItemListener(this);
         add(m_jchTransferscout);
 
 //        m_jchArenasizer = new JCheckBox(de.hattrickorganizer.model.HOVerwaltung.instance().getLanguageString("ArenaSizer"));
 //        m_jchArenasizer.setToolTipText(de.hattrickorganizer.model.HOVerwaltung.instance().getLanguageString("tt_Optionen_TabManagement"));
 //        m_jchArenasizer.setOpaque(false);
 //        m_jchArenasizer.setSelected(!gui.UserParameter.temp().tempTabArenasizer);
 //        m_jchArenasizer.addItemListener(this);
 //        add(m_jchArenasizer);
 
         m_jchInformation = new JCheckBox(de.hattrickorganizer.model.HOVerwaltung.instance().getLanguageString("Verschiedenes"));
         m_jchInformation.setToolTipText(de.hattrickorganizer.model.HOVerwaltung.instance().getLanguageString("tt_Optionen_TabManagement"));
         m_jchInformation.setOpaque(false);
         m_jchInformation.setSelected(!gui.UserParameter.temp().tempTabInformation);
         m_jchInformation.addItemListener(this);
         add(m_jchInformation);
     }
 }
