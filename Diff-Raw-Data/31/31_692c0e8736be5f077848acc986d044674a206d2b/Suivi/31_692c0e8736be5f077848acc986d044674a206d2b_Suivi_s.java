 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package vue;
 
 import interface_windows.Carton;
 import interface_windows.InterfaceWindows;
 import interface_windows.Palette;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 class Erreur {
     public String msg, horaire; public int id;
     public Erreur(String msg, String horaire, int id) {
         this.msg = msg;
         this.horaire = horaire;
         this.id = id;
     }
 }
 
 /**
  *
  * @author michael
  */
 public class Suivi extends javax.swing.JFrame {
     
     public static String horaire_format(String horaire) {
         return horaire.substring(0,2)
        + ":" + horaire.substring(2,4)
        + ":" + horaire.substring(4,6);
     }
     
     static final int NB_ERREURS = 7;
     //static final int MAXPAL = 1024; // TODO: test si valeur trop grande dans Paramétrage et dans code moteur
     
         //Variable ici car l'entrepôt est toujours consideré comme vide au début de l'application
     //int nb_palettes = 1;
     //int nb_palettes = 0;
     int nb_palette_A_commande = 0;
     int nb_palette_B_commande = 0;
     InterfaceWindows app;
     MessageReceiver message_receiver = new MessageReceiver(this);
     //String[] liste_erreur = new String[NB_ERREURS];
     List<Erreur> liste_erreur = new ArrayList<>();
     //Map<Integer,String> erreurs = new HashMap<>();
     
     
     /*Variable utilisée pour avoir un bon format dans les listes*/
     //String[] liste_def_palette = new String[MAXPAL];
     //String[] liste_def_carton = new String[MAXPAL];
     //Palette liste_palette[] = new Palette[MAXPAL];
     //Palette palette_initiale = new Palette(0, "A", 0);
     //List<Carton> liste_carton = new LinkedList<>();
     List<Palette> liste_palette = new ArrayList<>();
     int nb_palette_actuel_A = 0;
     int nb_palette_actuel_B = 0;
     int selected_palette_index;
     
     class MessageReceiver extends Thread {
         
         Suivi suivi;
         
         public MessageReceiver(Suivi suivi) {
             this.suivi = suivi;
         }
 
         @Override
         public void run() {
             System.out.println("Starting message receiver...");
             String msg = "";
             //liste_palette[0] = palette_initiale;
             //int indice_palettes_ajout_carton = 0;
             //Palette en_remplissage = null;
             Map<String,Palette> en_remplissage = new HashMap<>();
             while (true) {
                 try {
                     //ecoute d'un message du serveur
                     msg = suivi.app.network.listen_message();
                     System.out.println("Received message: '"+msg+"'");
                 } catch (IOException ex) {
                     //System.err.println("Could not establish server socket!");
                     app.error("Socket", "Could not establish client socket!", ex);
                 }
 
                 if (msg.length() == 0)
                 {
                     app.error("Null message received!", "A null message was received. Stopping listening.");
                     throw new Error("Null message received!");
                 }
                 try {
                     String decoupe[] = msg.split(" ");
                     
                     if ("E".equals(msg.substring(0, 1)))
                     {
 
                         //Reception et traitement d'une erreur
                         //String decoupe[] = msg.split(" ");
                         if (decoupe[1].equals("Fi")) {
                             app.info("Quitting", "Received a stop message, quitting...");
                             quit_app();
                             return;
                         } else {
                             int id_erreur = Integer.parseInt(decoupe[1]);
                             String horaire = decoupe[2];
                             /*
                             switch(id_erreur){
                                 case 0: liste_erreur[id_erreur] = id_erreur + " : AU"; break;
                                 case 1: liste_erreur[id_erreur] = id_erreur + " : trop de pièces defectueuses"; break;
                                 case 2: liste_erreur[id_erreur] = id_erreur + " : plus de carton"; break;
                                 case 3: liste_erreur[id_erreur] = id_erreur + " : imprimante HS"; break;
                                 case 4: liste_erreur[id_erreur] = id_erreur + " : trop de cartons dans la file d'attente"; break;
                                 case 5: liste_erreur[id_erreur] = id_erreur + " : plus de palette"; break;
                                 case 6: liste_erreur[id_erreur] = id_erreur + " : problème de conditionnement de palette"; break;    
                             }                                    
                             */
                             /*
                             switch(id_erreur){
                                 case 0: erreurs.put(id_erreur, id_erreur + " : AU"); break;
                                 case 1: erreurs.put(id_erreur, id_erreur + " : trop de pièces defectueuses"); break;
                                 case 2: erreurs.put(id_erreur, id_erreur + " : plus de carton"); break;
                                 case 3: erreurs.put(id_erreur, id_erreur + " : imprimante HS"); break;
                                 case 4: erreurs.put(id_erreur, id_erreur + " : trop de cartons dans la file d'attente"); break;
                                 case 5: erreurs.put(id_erreur, id_erreur + " : plus de palette"); break;
                                 case 6: erreurs.put(id_erreur, id_erreur + " : problème de conditionnement de palette"); break;    
                             }  
                             */
                             String msg_erreur = id_erreur + ": ";
                             switch(id_erreur){
                                 case 0: msg_erreur += "AU"; break;
                                 case 1: msg_erreur += "Trop de pièces defectueuses"; break;
                                 case 2: msg_erreur += "Plus de carton"; break;
                                 case 3: msg_erreur += "Imprimante HS"; break;
                                 case 4: msg_erreur += "Trop de cartons dans la file d'attente"; break;
                                 case 5: msg_erreur += "Plus de palette"; break;
                                 case 6: msg_erreur += "Problème de conditionnement de palette"; break;     
                             }
                             //System.out.println(msg_erreur);
                             msg_erreur += " (" + horaire_format(horaire) + ")";
                             System.out.println(msg_erreur);
                             liste_erreur.add(new Erreur(msg_erreur, horaire, id_erreur));
                             
                             //j_erreur.setListData(liste_erreur);
                             //B_reprise.setEnabled(true);
                             
                             maj_erreurs();
                         }
                     }
                     else if ("L C".equals(msg.substring(0, 3)))
                     {
                         
                         //Reception d'un carton
                         //String decoupe[] = msg.split(" ");
                         int id_carton = Integer.parseInt(decoupe[2]);
                         int id_palette = Integer.parseInt(decoupe[3]);
                         String type_piece = decoupe[4];
                         int pourcentage = Integer.parseInt(decoupe[5]);
                         String horaire = decoupe[6];
                         
                         /*
                         if (en_remplissage == null) {
                             en_remplissage = new Palette(type_piece);
                             //liste_palette[nb_palettes] = en_remplissage;
                             //nb_palettes++;
                             liste_palette.add(en_remplissage);
                         }
                         */
                         //Palette p = en_remplissage.get(id_palette);
                         Palette p = en_remplissage.get(Palette.get_unique_id_for(id_palette, type_piece));
                         if (p == null) {
                             //en_remplissage = new Palette(type_piece);
                             p = new Palette(id_palette, type_piece);
                             en_remplissage.put(p.get_unique_id(), p);
                             liste_palette.add(p);
                         }
                         
                         //ajout dans sa palette
                         Carton carton = new Carton(id_carton, type_piece, horaire, pourcentage);
                         //liste_palette[indice_palettes_ajout_carton].ajouter_carton(carton);
                         //liste_palette[nb_palettes-1].ajouter_carton(carton);
                         //en_remplissage.ajouter_carton(carton);
                         p.ajouter_carton(carton);
                         maj_carton();
                         maj_palettes();
                     }
                     else if ("L P".equals(msg.substring(0, 3)))
                     {
                         /*
                         //Reception d'une palette
                         
                         //String decoupe[] = msg.split(" ");
                         int id_palette = Integer.parseInt(decoupe[2]);
                         String type_palette = decoupe[3];
                         int horaire = Integer.parseInt(decoupe[4]); 
                         
                         switch (type_palette) {
                             case "A":
                                 nb_palette_actuel_A++;
                                 break;
                             case "B":
                                 nb_palette_actuel_B++;
                                 break;
                             default:
                                 throw new Error("Unsupported type of Palette: "+type_palette);
                         }
                         
                         //indice_palettes_ajout_carton++; //mise à jour de l'indice de la liste de palette
                         nb_palettes++; //mise à jour du nombre de palette
                         
                         Palette palette = new Palette(id_palette, type_palette, horaire);
                         //liste_palette[indice_palettes_ajout_carton] = palette;
                         liste_palette[nb_palettes-1] = palette;
                         
                         //MAJ de la liste de palettes
                         maj_palettes();*/
                         
                         int id_palette = Integer.parseInt(decoupe[2]);
                         String type_palette = decoupe[3];
                         String horaire = decoupe[4]; 
                         
                         switch (type_palette) {
                             case "A":
                                 nb_palette_actuel_A++;
                                 break;
                             case "B":
                                 nb_palette_actuel_B++;
                                 break;
                             default:
                                 throw new Error("Unsupported type of Palette: "+type_palette);
                         }
                         /*
                         en_remplissage.finaliser(id_palette, horaire);
                         en_remplissage = null;
                         * */
                         en_remplissage.get(Palette.get_unique_id_for(id_palette, type_palette)).finaliser(horaire);
                         maj_palettes();
                     }
                 } catch (NumberFormatException ex) {
                     app.error("Invalid message received", "An invalid message was received: "+msg, ex);
                 } catch (ArrayIndexOutOfBoundsException ex) {
                     app.error("Invalid message received", "Error while parsing message: "+msg, ex);
                 }
                 //maj_erreurs();
             }
         }
         
         private void quit_app() {
             dispose();
             app.quit();
         }
         
     }
     
     void maj_palettes() {
         String[] liste_def_palette = new String[liste_palette.size()];
         //liste_def_palette[i] = palette.toString();
         /*for (int i = 0; i < nb_palettes; i++) {
             liste_def_palette[i] = liste_palette[i].toString();
         }*/
         int i = 0;
         for (Palette p : liste_palette)
             liste_def_palette[i++] = p.toString();
         j_palette.setListData(liste_def_palette);
     }
     
     void maj_carton() {
         if (selected_palette_index >= 0) {
             Palette p = liste_palette.get(selected_palette_index);
             List<Carton> liste_carton = p.getListeCarton();
             String[] liste_def_carton = new String[liste_carton.size()];
             for(int j = 0; j < liste_carton.size(); j++)
             {
                 Carton carton = (Carton)liste_carton.get(j);
                 liste_def_carton[j] = carton.ToString();
             }
             j_carton.setListData(liste_def_carton);
         }
     }
     
    void maj_erreurs() {
         String[] erreurs = new String[liste_erreur.size()];
         int i = 0;
         for (Erreur e : liste_erreur)
             erreurs[i++] = e.msg;
         //System.out.println(j_erreur.getSelectedIndex());
         //B_reprise.setEnabled(j_erreur.getSelectedIndex() >= 0 && liste_erreur.size() != 0);
         j_erreur.setListData(erreurs);
        B_reprise.setEnabled(!liste_erreur.isEmpty());
     }
     
     /**
      * Creation d'une nouvelle forme Suivi
      */
     public Suivi(InterfaceWindows inter) {
         app = inter;
         setLocationByPlatform(true);
         initComponents();
         /*for (int j = 0; j<NB_ERREURS; j++)
         {
             liste_erreur[j] = "";
         }*/
         message_receiver.start();
     }
     /*
     public void set_nb_palettes_A_commande(int nb_palette_A_commande) {
         this.nb_palette_A_commande = nb_palette_A_commande;
     }
 
     public void set_nb_palette_B_commande(int nb_palette_B_commande) {
         this.nb_palette_B_commande = nb_palette_B_commande;
     }
     */
     
     public void commande(int nb_palette_A_commande, int nb_palette_B_commande) {
         //this.nb_palette_B_commande = nb_palette_B_commande;
         /*
         int j = 1;
         if (nb_palette_A_commande > 0) {
             while("A".equals(liste_palette[j].get_type_palette()) && !liste_palette[j].is_disponible())
             {
                 j++;
             }
             for(;nb_palette_A_commande > 0;j++)
             {
                 nb_palette_A_commande--;
                 liste_palette[j].set_disponible(false);
                 //liste_def_palette[j]= "Palette envoyé";                        
             }
         }
         while ("A".equals(liste_palette[j].get_type_palette())) {
             j++;
         }
         if (nb_palette_B_commande > 0) {
             while("B".equals(liste_palette[j].get_type_palette()) && !liste_palette[j].is_disponible())
             {
                 j++;
             }
             for(;nb_palette_B_commande > 0;j++)
             {
                 nb_palette_B_commande--;
                 liste_palette[j].set_disponible(false);
                 //liste_def_palette[j]= "Palette envoyé";                        
             }
         }
         //j_palette.setListData(liste_def_palette);*/
         int i = 0;
         while (i < liste_palette.size() && (nb_palette_A_commande > 0 || nb_palette_B_commande > 0)) {
             //app.info("", liste_palette[i].toString());
             Palette p = liste_palette.get(i);
             if (p.is_disponible()) {
                 //System.out.println(p);
                 if (nb_palette_A_commande > 0 && "A".equals(p.get_type_palette())) {
                     p.set_disponible(false);
                     nb_palette_A_commande--;
                     nb_palette_actuel_A--;
                 }
                 else if (nb_palette_B_commande > 0 && "B".equals(p.get_type_palette())) {
                     p.set_disponible(false);
                     nb_palette_B_commande--;
                     nb_palette_actuel_B--;
                 }
             }
             i++;
         }
         maj_palettes();
         if (nb_palette_A_commande > 0 || nb_palette_B_commande > 0) {
             app.error("Commande palette", "Impossible de retirer le nombre de palettes demandées."
                     + " Il reste "+nb_palette_A_commande+" palettes A et "+nb_palette_B_commande+" palettes B.");
             throw new Error("Inconsistent data!");
         }
     }
     
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jLabel2 = new javax.swing.JLabel();
         B_commande = new javax.swing.JButton();
         B_reprise = new javax.swing.JButton();
         B_arret = new javax.swing.JButton();
         jScrollPane1 = new javax.swing.JScrollPane();
         j_palette = new javax.swing.JList();
         jScrollPane2 = new javax.swing.JScrollPane();
         j_carton = new javax.swing.JList();
         jScrollPane3 = new javax.swing.JScrollPane();
         j_erreur = new javax.swing.JList();
         jLabel3 = new javax.swing.JLabel();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
         setTitle("Interface de suivi du lot");
         addWindowListener(new java.awt.event.WindowAdapter() {
             public void windowClosing(java.awt.event.WindowEvent evt) {
                 formWindowClosing(evt);
             }
         });
 
         jLabel2.setText("Erreur detectées :");
 
         B_commande.setText("Commande");
         B_commande.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 B_commandeActionPerformed(evt);
             }
         });
 
         B_reprise.setText("Reprise");
         B_reprise.setEnabled(false);
         B_reprise.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 B_repriseActionPerformed(evt);
             }
         });
 
         B_arret.setText("Arrêt");
         B_arret.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 B_arretActionPerformed(evt);
             }
         });
 
         j_palette.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
         j_palette.setMaximumSize(null);
         j_palette.setMinimumSize(null);
         j_palette.setName(""); // NOI18N
         j_palette.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
             public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                 j_paletteValueChanged(evt);
             }
         });
         jScrollPane1.setViewportView(j_palette);
 
         j_carton.setModel(new javax.swing.AbstractListModel() {
             String[] strings = { "Pas de palette selectionnée" };
             public int getSize() { return strings.length; }
             public Object getElementAt(int i) { return strings[i]; }
         });
         j_carton.setMaximumSize(null);
         j_carton.setMinimumSize(null);
         jScrollPane2.setViewportView(j_carton);
 
         j_erreur.setMaximumSize(new java.awt.Dimension(80, 100));
         j_erreur.setMinimumSize(new java.awt.Dimension(60, 100));
         j_erreur.setVisibleRowCount(2);
         j_erreur.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
             public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                 j_erreursValueChanged(evt);
             }
         });
         jScrollPane3.setViewportView(j_erreur);
 
         jLabel3.setText("Palettes en stock :");
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane3)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(B_reprise)
                         .addGap(18, 18, 18)
                         .addComponent(B_commande)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(B_arret))
                     .addGroup(layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel2)
                             .addComponent(jLabel3))
                         .addGap(0, 0, Short.MAX_VALUE))
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 348, Short.MAX_VALUE)))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jLabel2)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jLabel3)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                     .addComponent(jScrollPane1))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(B_reprise)
                     .addComponent(B_arret)
                     .addComponent(B_commande))
                 .addContainerGap())
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
     
     /*
      * ouverure de la fenêtre commande
      * 
      */
     private void B_commandeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_B_commandeActionPerformed
         // Envoi d'une commande
         System.out.println("Commande");
         /*int nb_palette_actuel_A = 0;
         int nb_palette_actuel_B = 0;
         for(int j = 1; j < nb_palettes; j++) {
             if("A".equals(liste_palette[j].get_type_palette()) && liste_palette[j].is_disponible())
             {
                 nb_palette_actuel_A++;
             }
             else if ("B".equals(liste_palette[j].get_type_palette()) && liste_palette[j].is_disponible())
             {
                 nb_palette_actuel_B++;
             }           
         }*/
         Commande fc = new Commande(app, this, nb_palette_actuel_A, nb_palette_actuel_B);
         fc.setVisible(true);
         /*fc.addWindowListener(new WindowAdapter() {
  
             @Override
             public void windowClosed(WindowEvent e)
             {
                 int j = 1;
                 if(nb_palette_A_commande > 0) {
                     while("A".equals(liste_palette[j].get_type_palette()) && !liste_palette[j].is_disponible())
                     {
                         j++;
                     }
                     for(;nb_palette_A_commande > 0;j++)
                     {
                         nb_palette_A_commande--;
                         liste_palette[j].set_disponible(false);
                         //liste_def_palette[j]= "Palette envoyé";                        
                     }
                 }
                 while("A".equals(liste_palette[j].get_type_palette())) {
                     j++;
                 }
                 if(nb_palette_B_commande > 0) {
                     while("B".equals(liste_palette[j].get_type_palette()) && !liste_palette[j].is_disponible())
                     {
                         j++;
                     }
                     for(;nb_palette_B_commande > 0;j++)
                     {
                         nb_palette_B_commande--;
                         liste_palette[j].set_disponible(false);
                         //liste_def_palette[j]= "Palette envoyé";                        
                     }
                 }
                 //j_palette.setListData(liste_def_palette);
                 maj_palettes();
             }
         });*/
     }//GEN-LAST:event_B_commandeActionPerformed
 
     /*
      * Envoi d'un message de reprise après une erreur.
      * 
      */
     private void B_repriseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_B_repriseActionPerformed
         int erreur_index = j_erreur.getSelectedIndex();
         
         if (erreur_index < 0) {
             if (liste_erreur.size() == 1) {
                 erreur_index = 0;
             } else {
                 app.error("Reprise", "Sélectionnez une erreur à reprendre.");
                 return;
             }
         }
         
         try {
             System.out.println("Message de reprise");
             app.network.send_message("2 " + liste_erreur.get(erreur_index).id);
         } catch (IOException ex) {
            app.error("IO Exception", "Could not send the command to the host!");
            ex.printStackTrace(System.err);
         }
         /*liste_erreur[erreur] = "";
         j_erreur.setListData(liste_erreur);
         boolean vide = true;
         for (int j = 0; j < NB_ERREURS; j++) {
             if (!"".equals(liste_erreur[j])) {
                 vide = false;
             }
         }
         if (vide){
             B_reprise.setEnabled(false);
         }*/
        liste_erreur.remove(erreur_index);
         maj_erreurs();
     }//GEN-LAST:event_B_repriseActionPerformed
     
     /*
      * Envoi d'un message d'arrêt de production après une erreur.
      * 
      */
     private void B_arretActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_B_arretActionPerformed
         System.out.println("Message d'arrêt");
         try {
             app.network.send_message("3");
            quit();
         } catch (IOException ex) {
            app.error("IO Exception", "Could not send the command to the host!");
            ex.printStackTrace(System.err);
         }
     }//GEN-LAST:event_B_arretActionPerformed
     
     /*
      * Selection d'une palette dans la liste
      * 
      */
     private void j_paletteValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_j_paletteValueChanged
         
         /*int index_palette = j_palette.getSelectedIndex();
         if (index_palette == -1)
         {
             //cas ou rien n'est sélectionné pour cause de mise à jour de la liste
         }
         else
         {
             Palette p = liste_palette[index_palette];
             if (p != null) {
                 //liste_carton = p.getListeCarton();
                 List<Carton> liste_carton = p.getListeCarton();
                 String[] liste_def_carton = new String[liste_carton.size()];
                 for(int j = 0; j < liste_carton.size(); j++)
                 {
                     Carton carton = (Carton)liste_carton.get(j);
                     liste_def_carton[j] = carton.ToString();
                 }
                 j_carton.setListData(liste_def_carton);
                 maj_carton(p);
             }
         }*/
         selected_palette_index = j_palette.getSelectedIndex();
         maj_carton();
     }//GEN-LAST:event_j_paletteValueChanged
     
     void quit() {
         app.quit();
         if(message_receiver.isAlive())
             message_receiver.stop();
         dispose();
     }
     
     private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
         quit();
     }//GEN-LAST:event_formWindowClosing
 
     private void j_erreursValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_j_erreursValueChanged
        System.out.println("OK!!!!!!!! "+liste_erreur.size());
        maj_carton();
     }//GEN-LAST:event_j_erreursValueChanged
 
     /*
     public static void main(String args[]) {
         
         java.awt.EventQueue.invokeLater(new Runnable() {
 
             public void run() {
                 new Suivi().setVisible(true);
             }
         });
     }*/
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton B_arret;
     private javax.swing.JButton B_commande;
     private javax.swing.JButton B_reprise;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JScrollPane jScrollPane3;
     private javax.swing.JList j_carton;
     private javax.swing.JList j_erreur;
     private javax.swing.JList j_palette;
     // End of variables declaration//GEN-END:variables
 }
