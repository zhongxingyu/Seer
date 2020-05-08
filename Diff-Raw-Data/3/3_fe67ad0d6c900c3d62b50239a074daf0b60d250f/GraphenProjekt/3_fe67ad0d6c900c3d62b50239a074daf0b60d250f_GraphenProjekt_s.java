 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package graphenprojekt;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 //schallala
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import javax.swing.*;
 
 
 import javax.swing.JOptionPane;
 
 /**
  *
  * @author george,Sascha
  */
 public class GraphenProjekt extends JFrame {
 
     /**
      * Fenstervariablen
      */
     JButton neuer_knoten;
     JButton neue_kante;
     JButton weg_zeichnen;
     JButton stein_entfernen;
     JButton kante_entfernen;
     //optionsdialog
     JDialog d = new JDialog();
     Graph_adjmat graph;
     JFileChooser dateiauswahl;
     
     
     /**
      * Spielfeldvariablen
      */
     int maximale_Knotenanzahl = 7;
     int Knotendurchmesser = 30;
     int Knotendurchmesser_markiert = 34;
     int xpadding = 70;
     int ypadding = 80;
     int button_breite = 140;
     int button_höhe = 20;
     int font_size = 13;
     char tempknoten;
     
      // Menüleiste
     JMenuBar menueLeiste;
     // Menüleiste Elemente
     JMenuItem beenden;
     JMenuItem datei;
     JMenuItem speichern;
     JMenuItem laden;
     
     static Color color_markiert = new Color(100, 200, 100);
     static Color color_knoten = Color.BLACK;
     static Color color_knoten_zeichen = Color.WHITE;
     static Color color_kante = Color.blue;
     static Color color_kante_markiert = new Color(100, 200, 100);
     
     
     static final int NICHTS = 0;
     static final int STEIN_LEGEN = 1;
     static final int NEUE_KANTE = 2;
     static final int STEIN_ENTFERNEN = 3;
     static final int KANTE_ENTFERNEN = 4;
     static final int WEG_ZEICHNEN = 5;
     int action = NICHTS;
     
     
     
     Knoten tmp_start = null;
 
     public void KanteZeichnen(Knoten von, Knoten bis, Graphics g) {
 
         int x = (bis.x - von.x);
         int y = bis.y - von.y;
         //nx, ny = normalisierter vektor in pfeilrichtung
         double nx = x / Math.sqrt(x * x + y * y);
         double ny = y / Math.sqrt(x * x + y * y);
 
         int vonX = von.x;
         int vonY = von.y;
         //damit unser Pfeil auf den Rand vom nächsten Knoten zeigt
         //muss der Steinradius in x und y richtung korrekt abgezogen
         //werden. also vom Mittelpunkt des  Zielknotens Knotendurchmesser                     
         //in Richtung des Anfangsknotens. 
         int bisX = (int) (bis.x - nx * Knotendurchmesser / 2);
         int bisY = (int) (bis.y - ny * Knotendurchmesser / 2);
 
         double l = 10.0; // Pfeilspitzenlänge 
         double a = Math.PI / 4 - Math.atan2((bisY - vonY), (bisX - vonX));
         double c = Math.cos(a) * l;
         double s = Math.sin(a) * l;
         g.drawLine(bisX, bisY, (int) (bisX - s), (int) (bisY - c));
         g.drawLine(bisX, bisY, (int) (bisX - c), (int) (bisY + s));
         g.drawLine(von.x, von.y, bisX, bisY);
 
     }
 
     @Override
     public void paint(Graphics gg) {
 
         //gebufferte ausgabe
         BufferedImage bufferedImage = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_ARGB);
         Graphics2D g = bufferedImage.createGraphics();
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                         RenderingHints.VALUE_ANTIALIAS_ON);
         super.paintComponents(g);
         //Durchlaufe das Spielfeld und Zeichne es
         g.setFont(new Font("Sans", Font.BOLD, font_size));
 
 
         g.setColor(Color.black);
         
         /**
          * *
          * erst die Linien zeichnen, damit sie nicht später die Knoten
          * überdecken...
          */
         for (int k = 0; k < maximale_Knotenanzahl; k++) {
 
             for (int j = 0; j < maximale_Knotenanzahl; j++) {
                 if (graph.kante[k][j] != 0) {
 
                     g.setColor(color_kante);
                    if(graph.knoten[k].markiert && graph.knoten[j].markiert)
                         g.setColor(color_kante_markiert);
                     KanteZeichnen(graph.knoten[k], graph.knoten[j], g);
 
                 }
             }
             /**
              * jetzt die eigentlichen Knoten zeichnen
              */
             for (int j = 0; j < maximale_Knotenanzahl; j++) {
 
                 if (graph.knoten[j] != null) {
 
                     //markierte Knoten zeichnen
                     if (graph.knoten[j].markiert) {
                         
                         g.setColor(color_markiert);
                         
                         g.fillOval(graph.knoten[j].x - Knotendurchmesser_markiert / 2,
                                 graph.knoten[j].y - Knotendurchmesser_markiert / 2,
                                 Knotendurchmesser_markiert,
                                 Knotendurchmesser_markiert);
                     }
                     //standardknoten zeichnen
                     g.setColor(color_knoten);
                     g.fillOval(graph.knoten[j].x - Knotendurchmesser / 2, graph.knoten[j].y - Knotendurchmesser / 2, Knotendurchmesser, Knotendurchmesser);
                     g.setColor(color_knoten_zeichen);
                     g.drawString("" + graph.knoten[j].data,
                             graph.knoten[j].x - font_size / 4,
                             graph.knoten[j].y + font_size / 2);
 
                 }
             }
 
 
 
         }
         //Doppelpufferung
         Graphics2D g2dComponent = (Graphics2D) gg;
         g2dComponent.drawImage(bufferedImage, null, 0, 0);
         g.clearRect(xpadding, ypadding, this.getWidth(), this.getHeight());
 
     }
 
     GraphenProjekt() {
 
         //	Angaben zum Fenster		
         setTitle("Graph");
 
         graph = new Graph_adjmat(maximale_Knotenanzahl);
         graph.toleranz = Knotendurchmesser / 2;
 
         setLocation(300, 300);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setLayout(null);
 
         dateiauswahl = new JFileChooser();
         setResizable(true);
         this.setSize(600, 600);
         menueLeiste = new JMenuBar();
         addMouseListener(new CMeinMausAdapter());
 
          
         // neuer Knoten
         neuer_knoten = new JButton("neuer Knoten");
         neuer_knoten.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent arg0) {
                 action = STEIN_LEGEN;
                 repaint();
                 // Auswahldialog
                 String tempknotenstring = (String) JOptionPane.showInputDialog(null,
                         "Zeichen",
                         "neuen knoten wählen",
                         JOptionPane.QUESTION_MESSAGE,
                         null, null,"");
                 tempknoten = tempknotenstring.charAt(0);
             }
         });
         neuer_knoten.setBounds(0, 0, button_breite, button_höhe);
         this.add(neuer_knoten);
 
         // neue Kante
         neue_kante = new JButton("neue Kante");
         neue_kante.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent arg0) {
                 action = NEUE_KANTE;
                 repaint();
             }
         });
         neue_kante.setBounds(button_breite, 0, button_breite, button_höhe);
         this.add(neue_kante);
 
         
         // neue Kante
         weg_zeichnen = new JButton("zeige Weg");
         weg_zeichnen.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent arg0) {
                 action = WEG_ZEICHNEN;
                 repaint();
             }
         });
         weg_zeichnen.setBounds(button_breite*2 , 0, button_breite, button_höhe);
         this.add(weg_zeichnen);
         
         
         // lösche Knoten
         stein_entfernen = new JButton("lösche Knoten");
         stein_entfernen.addActionListener(new ActionListener() {
              @Override
              public void actionPerformed(ActionEvent arg0) {
                  action = STEIN_ENTFERNEN;
                  repaint();
                  //Auswahldialog
                  
              }
         });
         stein_entfernen.setBounds(0, button_höhe, button_breite, button_höhe);
         this.add(stein_entfernen);
         
         // Kante entfernen
         kante_entfernen = new JButton("Kante entfernen");
         kante_entfernen.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent arg0) {
                 action = KANTE_ENTFERNEN;
                 repaint();
             }
         });
         kante_entfernen.setBounds(button_breite, button_höhe, button_breite, button_höhe);
         this.add(kante_entfernen);
         
        
 
         // Menüelemente erzeugen
         datei = new JMenu("Datei");
         beenden = new JMenuItem("Beenden");
 
         beenden.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent arg0) {
                 System.exit(0);
             }
         });
 
 
 
         speichern = new JMenuItem("Speichern");
         speichern.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent arg0) {
                 
                 int rueckgabeWert = dateiauswahl.showOpenDialog(null);
                 dateiauswahl.setDialogType(JFileChooser.SAVE_DIALOG);
                 /* Abfrage, ob auf "Öffnen" geklickt wurde */
                 if(rueckgabeWert == JFileChooser.APPROVE_OPTION)
                 {
                      // Ausgabe der ausgewaehlten Datei
                     graph.dateischreiben(dateiauswahl.getSelectedFile().getName());
                 }
                 
             }
           
         });
 
         laden = new JMenuItem("Laden");
         laden.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent arg0) {
                 
                 
                graph = new Graph_adjmat(maximale_Knotenanzahl); 
                graph.toleranz = Knotendurchmesser / 2;
                
                 int rueckgabeWert = dateiauswahl.showOpenDialog(null);
                 dateiauswahl.setDialogType(JFileChooser.OPEN_DIALOG);
                 /* Abfrage, ob auf "Öffnen" geklickt wurde */
                 if(rueckgabeWert == JFileChooser.APPROVE_OPTION)
                 {
                      // Ausgabe der ausgewaehlten Datei
                     graph.dateilesen(dateiauswahl.getSelectedFile().getName());
                 }
                 
               
                repaint();
             }
 
            
         });
 
         // Menüelemente hinzufügen
         menueLeiste.add(datei);
         
         // Untermenüelemente hinzufügen
         datei.add(laden);
         datei.add(speichern);
         datei.add(beenden);
         
         this.add(menueLeiste);
         this.setJMenuBar(menueLeiste);
 
         setVisible(true);
     }
 
     /**
      * für Click-Events auf das Graphenfeld
      */
     class CMeinMausAdapter extends MouseAdapter {
 
         @Override
         public void mousePressed(MouseEvent e) {
             
             graph.AlleEntmarkieren();
             int x, y;
             x = e.getX();
             y = e.getY();
             Knoten tst;
             if(x > xpadding && y > ypadding){
             switch (action) {
 
                 case NEUE_KANTE:
                      
                      tst = graph.KnotenAnStelle(x, y);
 
                     if (tst != null) {
 
                         if (tmp_start == null) {
                             tst.markiert = !tst.markiert;
                             tmp_start = tst;
                         } else {
 
                             int Abstand =
                                     (int) Math.sqrt(Math.pow(tmp_start.x - tst.x, 2) + Math.pow(tmp_start.y - tst.y, 2));
                             if (!tmp_start.equals(tst)) {
                                 graph.kanteneu(tmp_start.data, tst.data, Abstand);
                             }
                             tst.markiert = false;
                             tmp_start.markiert = false;
                             tmp_start = null;
                             action = NICHTS;
 
                         }
                     }
                     break;
                 case STEIN_ENTFERNEN:
                     tst = graph.KnotenAnStelle(x, y);
                     if(tst != null){
                         graph.knotenloeschen(tst.data, graph);
                     }
                     action = NICHTS;
                     break;
                 case STEIN_LEGEN:
                     if (graph.knotenneu(x, y, tempknoten)) {
 
                         action = NICHTS;
                         tempknoten = ' ';
                     }
                     break;
                 case WEG_ZEICHNEN:
                     tst = graph.KnotenAnStelle(x, y);
 
                     if (tst != null) {
 
                         if (tmp_start == null) {
                             tst.markiert = !tst.markiert;
                             tmp_start = tst;
                         } else {
 
                             if (!tmp_start.equals(tst)) {
                                
                                graph.Dijkstra(tmp_start, tst);
                                repaint();
                             }
                            
                             tmp_start = null;
                             action = NICHTS;
 
                         }
                     }
                     break;
                 case KANTE_ENTFERNEN:
                     tst = graph.KnotenAnStelle(x, y);
                     if (tst != null) {
                         if (tmp_start == null) {
                             tst.markiert = !tst.markiert;
                             tmp_start = tst;
                         } 
                         else { 
                             if (!tmp_start.equals(tst)) {
                                 graph.kanteloeschen(tmp_start.data, tst.data);
                             }
                             tst.markiert = false;
                             tmp_start.markiert = false;
                             tmp_start = null;
                             action = NICHTS;
                         }
                     }
                     break;
 
             }
             repaint();
             }
         }
     }
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
 
         new GraphenProjekt();
     }
 }
