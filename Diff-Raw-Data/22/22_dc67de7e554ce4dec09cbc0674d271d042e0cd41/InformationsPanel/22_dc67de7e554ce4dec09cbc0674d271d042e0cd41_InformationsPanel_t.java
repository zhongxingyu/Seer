 // %1451261274:de.hattrickorganizer.gui.info%
 package ho.module.misc;
 
 import ho.core.gui.RefreshManager;
 import ho.core.gui.Refreshable;
 import ho.core.gui.comp.panel.ImagePanel;
 
 import java.awt.BorderLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Insets;
 
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 
 
 /**
  * Zeigt die allgemeinen Informationen
  */
 public class InformationsPanel extends ImagePanel implements Refreshable {
 	
 	private static final long serialVersionUID = 1218148161116371590L;
 	
     //~ Instance fields ----------------------------------------------------------------------------
 
 	private TeamPanel m_jpBasics = new TeamPanel();
     private FinanzenPanel m_jpAktuelleFinanzen= new FinanzenPanel(true);
     private FinanzenPanel m_jpVorwochenFinanzen= new FinanzenPanel(false);
     private MiscPanel m_jpSonstiges = new MiscPanel();
     private StaffPanel m_jpTrainerStab = new StaffPanel();
 
     //~ Constructors -------------------------------------------------------------------------------
 
     /**
      * Creates a new InformationsPanel object.
      */
     public InformationsPanel() {
         initComponents();
         RefreshManager.instance().registerRefreshable(this);
        reInit();
     }
 
     
    @Override
	public final void reInit() {
     	m_jpBasics.setLabels();
     	m_jpAktuelleFinanzen.setLabels();
     	m_jpVorwochenFinanzen.setLabels();
     	m_jpSonstiges.setLabels();
     	m_jpTrainerStab.setLabels();
     }
 
    @Override
	public final void refresh() {
     		m_jpBasics.setLabels();
     		m_jpAktuelleFinanzen.setLabels();
     		m_jpVorwochenFinanzen.setLabels();
     		m_jpSonstiges.setLabels();
     		m_jpTrainerStab.setLabels();
     }
 
     //----------init-----------------------------------------------
     private void initComponents() {
         final GridBagLayout layout = new GridBagLayout();
         final GridBagConstraints constraints = new GridBagConstraints();
         constraints.anchor = GridBagConstraints.NORTH;
         constraints.fill = GridBagConstraints.NONE;
         constraints.weightx = 0.0;
         constraints.weighty = 0.0;
         constraints.insets = new Insets(4, 4, 4, 4);
 
         setLayout(new BorderLayout());
 
         final JPanel mainPanel = new ImagePanel();
         mainPanel.setLayout(new GridLayout(2, 1, 4, 4));
 
         JPanel panel = new ImagePanel();
         panel.setLayout(layout);
 
         constraints.gridx = 0;
         constraints.gridy = 0;
         constraints.gridwidth = 1;
         layout.setConstraints(m_jpBasics, constraints);
         panel.add(m_jpBasics);
 
         constraints.gridx = 1;
         constraints.gridy = 0;
         constraints.gridwidth = 1;
         layout.setConstraints(m_jpTrainerStab, constraints);
         panel.add(m_jpTrainerStab);
 
         constraints.gridx = 2;
         constraints.gridy = 0;
         constraints.gridwidth = 1;
         layout.setConstraints(m_jpSonstiges, constraints);
         panel.add(m_jpSonstiges);
 
         mainPanel.add(panel);
 
         panel = new ImagePanel();
         panel.setLayout(layout);
 
         constraints.gridx = 0;
         constraints.gridy = 0;
         constraints.gridwidth = 1;
         layout.setConstraints(m_jpAktuelleFinanzen, constraints);
         panel.add(m_jpAktuelleFinanzen);
 
         constraints.gridx = 1;
         constraints.gridy = 0;
         constraints.gridwidth = 1;
         layout.setConstraints(m_jpVorwochenFinanzen, constraints);
         panel.add(m_jpVorwochenFinanzen);
 
         mainPanel.add(panel);
 
         final JScrollPane scrollpane = new JScrollPane(mainPanel);
         add(scrollpane);
 
     }
 
 }
