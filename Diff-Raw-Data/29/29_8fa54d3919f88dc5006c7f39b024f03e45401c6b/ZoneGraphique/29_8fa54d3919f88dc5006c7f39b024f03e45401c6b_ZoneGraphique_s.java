 package com.ei3info.tp.lofteurs;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.text.NumberFormat;
 
 import javax.swing.JButton;
 import javax.swing.JFormattedTextField;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 /**
  * une classe comportant une zone graphique dans laquelle on peut dessiner ; le
  * dessin est refait automatiquement par la classe Panel associée ; tous les
  * objets de type ObjetDessinable ajoutés à la liste sont redessinés par un
  * appel à leur méthode dessinerObjet(Graphics g)
  * @see ObjectDessinable,LoftPanel
  * @author moreau
  */
 public class ZoneGraphique extends JFrame
 {
 
     private JPanel              loftPanel;
     private JPanel              actionPanel;
     private JPanel              parametresPanel;
     private JPanel              resumePanel;
     private JPanel              resumeContenuPanel;
     private JScrollPane         jsp;
     private JPanel              footerPanel;
     private Saison              saison;
     private JLabel              lDuree             = new JLabel();
     private int                 nombreCommentaires = 0;
     
 
     private JFormattedTextField tfPErratiques      = new JFormattedTextField(
                                                            NumberFormat
                                                                    .getIntegerInstance());
     private JFormattedTextField tfVoraces          = new JFormattedTextField(
                                                            NumberFormat
                                                                    .getIntegerInstance());
     private JFormattedTextField tfCannibales       = new JFormattedTextField(
                                                            NumberFormat
                                                                    .getIntegerInstance());
     private JFormattedTextField tfLapins           = new JFormattedTextField(
                                                            NumberFormat
                                                                    .getIntegerInstance());
 
     /**
      * constructeur
      * @param titre
      *            le nom de l'application
      */
     public ZoneGraphique(String titre, Saison saison)
     {
         // appel au constructeur de base
         super(titre);
         this.saison = saison;
         // ajout d'une taille par défaut
         setSize(800, 800);
 
         // ajout d'un listener
         addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e)
             {
                 System.exit(0);
             }
         });
 
         loftPanel = new JPanel();
         actionPanel = creerActionPanel();
         parametresPanel = creerParametresPanel();
         resumePanel = creerResumePanel();
         footerPanel = creerFooterPanel();
         this.getContentPane().setLayout(new BorderLayout());
         this.getContentPane().add(actionPanel, BorderLayout.SOUTH);
         this.getContentPane().add(parametresPanel, BorderLayout.EAST);
         this.getContentPane().add(resumePanel, BorderLayout.WEST);
         this.getContentPane().add(footerPanel, BorderLayout.NORTH);
         pack();
         setVisible(true);
         this.setResizable(false);
     }
 
     private JPanel creerActionPanel()
     {
         JPanel panel = new JPanel();
         lDuree = new JLabel("Non commencé");
         lDuree.setFont(new Font("consolas", Font.BOLD, 20));
 
         panel.add(lDuree);
         return panel;
     }
 
     private JPanel creerFooterPanel()
     {
         JPanel panel = new JPanel();
         JLabel text = new JLabel("a B & S production");
         text.setFont(new Font(null, Font.ITALIC, 12));
 
         FlowLayout fl = new FlowLayout(FlowLayout.RIGHT);
         panel.setLayout(fl);
         panel.add(text);
         return panel;
     }
 
     private JPanel creerResumePanel()
     {
         JPanel panel = new JPanel();
         JLabel titreResumePanel = new JLabel("Résumé des événements");
 
         resumeContenuPanel = new JPanel();
         jsp = new JScrollPane(resumeContenuPanel);
         jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
         jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jsp.setPreferredSize(new Dimension(250, 400));
         resumeContenuPanel.setLayout(new GridBagLayout());
 
         panel.setLayout(new BorderLayout());
         panel.add(titreResumePanel, BorderLayout.NORTH);
         panel.add(jsp, BorderLayout.CENTER);
         return panel;
     }
 
     private JPanel creerParametresPanel()
     {
         JPanel panel = new JPanel();
 
         JLabel lPErratiques = new JLabel("% Erratiques");
         JLabel lPVoraces = new JLabel("% Voraces");
         JLabel lPCannibales = new JLabel("% Cannibales");
         JLabel lPLapins = new JLabel("% Lapins");
 
         tfPErratiques.setColumns(5);
         tfVoraces.setColumns(5);
         tfCannibales.setColumns(5);
         tfLapins.setColumns(5);
         tfPErratiques.setText(String
                 .valueOf((int) (Saison.proportionErratique * 100)));
         tfVoraces
                 .setText(String.valueOf((int) (Saison.proportionVorace * 100)));
         tfCannibales.setText(String
                 .valueOf((int) (Saison.proportionCannibale * 100)));
         tfLapins.setText(String.valueOf((int) (Saison.proportionLapin * 100)));
 
         JButton bRelancer = new JButton("Relancer");
         bRelancer.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent arg0)
             {
                 loftPanel = new JPanel();
                 try
                 {
 
                     Float fErratique = new Float(tfPErratiques.getText());
                     Float fVorace = new Float(tfVoraces.getText());
                     Float fCannibale = new Float(tfCannibales.getText());
                     Float fLapin = new Float(tfLapins.getText());
 
                     Saison.proportionErratique = fErratique / 100;
                     Saison.proportionVorace = fVorace / 100;
                     Saison.proportionCannibale = fCannibale / 100;
                     Saison.proportionLapin = fLapin / 100;
                 } catch (NumberFormatException e)
                 {
                     JOptionPane
                             .showMessageDialog(
                                     ZoneGraphique.this,
                                     "Veuillez rentrer des nombres corrects pour les pourcentages",
                                     "Format incorrect",
                                     JOptionPane.ERROR_MESSAGE);
                 }
                 resumeContenuPanel.removeAll();
                 saison.redemarrerSaison();
             }
 
         });
 
         panel.setLayout(new GridBagLayout());
         panel.add(lPErratiques, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                 GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,
                         10, 5, 10), 0, 0));
         panel.add(lPVoraces, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                 GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,
                         10, 5, 10), 0, 0));
         panel.add(lPCannibales, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                 GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,
                         10, 5, 10), 0, 0));
         panel.add(lPLapins, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                 GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,
                         10, 5, 10), 0, 0));
 
         panel.add(tfPErratiques, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                 GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,
                         5, 5, 10), 0, 0));
         panel.add(tfVoraces, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                 GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,
                         5, 5, 10), 0, 0));
         panel.add(tfCannibales, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
                 GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,
                         5, 5, 10), 0, 0));
         panel.add(tfLapins, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
                 GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,
                         5, 5, 10), 0, 0));
 
         panel.add(bRelancer, new GridBagConstraints(0, 5, 2, 1, 0.0, 0.0,
                 GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
                         5, 10, 5, 20), 0, 0));
         return panel;
     }
 
     public void setLoftPanel(JPanel loftPanel)
     {
         if (loftPanel != null)
         {
             this.getContentPane().remove(loftPanel);
         }
 
         this.loftPanel = loftPanel;
         this.getContentPane().add(loftPanel, BorderLayout.CENTER);
         loftPanel.repaint();
         this.getContentPane().validate();
         this.getContentPane().repaint();
         this.validate();
         this.repaint();
         pack();
     }
 
     /**
      * largeur de la partie dessinable
      */
     public int getWidth()
     {
         return getContentPane().getWidth();
     }
 
     /**
      * hauteur de la partie dessinable
      */
     public int getHeight()
     {
         return getContentPane().getHeight();
     }
 
     public void setTime(int heures)
     {
         this.lDuree.setText(heures / 24 + " jour " + heures % 24 + " h");
     }
 
     public void afficherEvenement(String evenement)
     {
         afficherEvenement(evenement, Color.BLACK);
     }
 
     public void afficherEvenement(String evenement, Color couleur)
     {
         afficherEvenement(evenement, couleur, false);
     }
 
     public void afficherEvenement(String evenement, Color couleur,
             boolean sauterLigne)
     {
         afficherEvenement(evenement, couleur, sauterLigne, 0, true);
     }
 
     public void afficherEvenement(String duree, String evenement, Color couleur,
             boolean sauterLigne)
     {
         afficherEvenement(duree, new Color(100, 100, 100), false, 0, false);
         afficherEvenement(evenement, couleur, sauterLigne, 1, true);
     }
     
     private void afficherEvenement(String evenement, Color couleur,
             boolean sauterLigne, int positionX, boolean finLigne)
     {
         JLabel lEvenement = new JLabel(evenement);
         lEvenement.setForeground(couleur);
 
         int verticalInset = (sauterLigne) ? 15 : 2;
         Insets ligneInsets = new Insets(2, 2, verticalInset, 5);
         int longueur = (finLigne) ? GridBagConstraints.REMAINDER : 1;
         
         resumeContenuPanel.add(lEvenement, new GridBagConstraints(positionX,
                 nombreCommentaires, longueur, 1, 0.0, 0.0, GridBagConstraints.WEST,
                 GridBagConstraints.VERTICAL, ligneInsets, 0, 0));
         System.out.println(evenement);
         if (finLigne)
         {
             nombreCommentaires++;
         }
         
         jsp.validate();
         jsp.repaint();
         jsp.getVerticalScrollBar().setValue(
                 jsp.getVerticalScrollBar().getMaximum());
     }
 }
