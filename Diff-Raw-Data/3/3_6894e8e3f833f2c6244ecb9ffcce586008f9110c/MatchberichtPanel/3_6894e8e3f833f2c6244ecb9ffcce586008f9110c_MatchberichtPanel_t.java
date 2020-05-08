 // %3087735495:de.hattrickorganizer.gui.matches%
 package ho.module.matches;
 
 import ho.core.gui.HOMainFrame;
 import ho.core.gui.comp.panel.ImagePanel;
 import ho.core.gui.theme.HOIconName;
 import ho.core.gui.theme.ThemeManager;
 import ho.core.model.HOVerwaltung;
 import ho.core.model.match.MatchKurzInfo;
 import ho.core.model.match.Matchdetails;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionListener;
 import java.sql.Timestamp;
 
 import javax.swing.JButton;
 import javax.swing.JDialog;
 
 
 
 class MatchberichtPanel extends ImagePanel implements ActionListener {
 	
 	private static final long serialVersionUID = -9014579382145462648L;
 
     private JButton maxButton = new JButton(ThemeManager.getIcon(HOIconName.MAXLINEUP));
     private MatchKurzInfo matchKurzInfo;
     private MatchberichtEditorPanel m_clMatchbericht = new MatchberichtEditorPanel();
     private String matchText = "";
 
     MatchberichtPanel(boolean withButton) {
 
         setLayout(new BorderLayout());
 
         add(m_clMatchbericht, BorderLayout.CENTER);
 
         if (withButton) {
             final GridBagLayout layout = new GridBagLayout();
             final GridBagConstraints constraints = new GridBagConstraints();
             constraints.anchor = GridBagConstraints.SOUTHEAST;
             constraints.fill = GridBagConstraints.NONE;
             constraints.weighty = 1.0;
             constraints.weightx = 1.0;
             constraints.insets = new Insets(4, 6, 4, 6);
 
             final ImagePanel buttonPanel = new ImagePanel(layout);
 
             maxButton.setToolTipText(HOVerwaltung.instance().getLanguageString("tt_Matchbericht_Maximieren"));
             maxButton.setEnabled(false);
             maxButton.setPreferredSize(new Dimension(25, 25));
             maxButton.addActionListener(this);
             layout.setConstraints(maxButton, constraints);
             buttonPanel.add(maxButton);
 
             add(buttonPanel, BorderLayout.SOUTH);
         }
     }
 
     public final void setText(String text) {
         matchText = text;
         m_clMatchbericht.setText(text);
     }
 
     public final void actionPerformed(java.awt.event.ActionEvent actionEvent) {
         //Dialog mit Matchbericht erzeugen
         final String titel = matchKurzInfo.getHeimName() + " - " + matchKurzInfo.getGastName()
                              + " ( " + matchKurzInfo.getHeimTore() + " : "
                              + matchKurzInfo.getGastTore() + " )";
         final JDialog matchdialog = new JDialog(HOMainFrame.instance(),titel);
        matchdialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
         matchdialog.getContentPane().setLayout(new BorderLayout());
 
         final MatchberichtPanel berichtpanel = new MatchberichtPanel(false);
         berichtpanel.setText(matchText);
         matchdialog.getContentPane().add(berichtpanel, BorderLayout.CENTER);
 
         matchdialog.setLocation(50, 50);
         matchdialog.setSize(600, HOMainFrame.instance().getHeight() - 100);
         matchdialog.setVisible(true);
     }
 
     public final void clear() {
         matchKurzInfo = null;
         matchText = "";
         m_clMatchbericht.clear();
         maxButton.setEnabled(false);
     }
 
 
     public final void refresh(MatchKurzInfo info,Matchdetails details) {
         matchKurzInfo = info;
 
         if ((info != null)
             && info.getMatchDateAsTimestamp().before(new Timestamp(System.currentTimeMillis()))) {
             matchText = details.getMatchreport();
             maxButton.setEnabled(true);
         } else {
             maxButton.setEnabled(false);
             matchText = "";
         }
 
         m_clMatchbericht.setText(matchText);
     }
 }
