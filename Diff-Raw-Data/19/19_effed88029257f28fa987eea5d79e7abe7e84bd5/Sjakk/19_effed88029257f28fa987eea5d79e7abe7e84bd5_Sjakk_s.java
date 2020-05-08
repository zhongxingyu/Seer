 package Kode;
 
 import java.awt.*;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.util.ArrayList;
 import java.util.Iterator;
 import javax.swing.*;
 
 public class Sjakk extends JInternalFrame implements MouseListener, MouseMotionListener {
 
     private boolean meme = false;
     private SjakkTabell sjakkTabell = new SjakkTabell();
     private java.util.List _listeners = new ArrayList();
     private Point kongeHpos;
     private Point kongeSpos;
     private Logg svartLogg = new Logg();
     private Logg hvitLogg = new Logg();
     private Koordinater kord = new Koordinater();
     private int tur = 2;
     private JLayeredPane layeredPane;
     private BrettRute chessBoard;
     private BrikkeLabel chessPiece;
     private BrikkeLabel brikke;
     private int lag;
     private Icon lolW = new ImageIcon("src/Kode/Bilder/lolW.png");
     private Icon lolB = new ImageIcon("src/Kode/Bilder/lolB.png");
     private BondeS bondeS = new BondeS(new ImageIcon("src/Kode/Bilder/BondeS.png"));
     private BondeH bondeH = new BondeH(new ImageIcon("src/Kode/Bilder/BondeH.png"));
     private TarnS tårnS = new TarnS(new ImageIcon("src/Kode/Bilder/TarnS.png"));
     private TarnH tårnH = new TarnH(new ImageIcon("src/Kode/Bilder/TarnH.png"));
     private TarnS tårnShø = tårnS;
     private TarnS tårnSve = tårnS;
     private TarnH tårnve = tårnH;
     private TarnH tårnhø = tårnH;
     private HestS hestS = new HestS(new ImageIcon("src/Kode/Bilder/HestS.png"));
     private HestH hestH = new HestH(new ImageIcon("src/Kode/Bilder/HestH.png"));
     private LoperS løperS = new LoperS(new ImageIcon("src/Kode/Bilder/LoperS.png"));
     private LoperH løperH = new LoperH(new ImageIcon("src/Kode/Bilder/LoperH.png"));
     private DronningS dronningS = new DronningS(new ImageIcon("src/Kode/Bilder/DronningS.png"));
     private DronningH dronningH = new DronningH(new ImageIcon("src/Kode/Bilder/DronningH.png"));
     private KongeS kongeS = new KongeS(new ImageIcon("src/Kode/Bilder/KongeS.png"));
     private KongeH kongeH = new KongeH(new ImageIcon("src/Kode/Bilder/KongeH.png"));
     private Icon hjelpIkon; //Hjelpevariabel for midlertidig ikon funksjon
     private Point startPos;
     private int xAdjustment;
     private int yAdjustment;
 
     public Sjakk() {
         Dimension boardSize = new Dimension(600, 600);
 
         setVisible(true);
         setLocation(100, 100);
         setTitle("");
         setResizable(false);
         setClosable(false);
         setIconifiable(false);
         setMaximizable(false);
         setBorder(null);
         setRootPaneCheckingEnabled(false);
         javax.swing.plaf.InternalFrameUI ifu = getUI();
         ((javax.swing.plaf.basic.BasicInternalFrameUI) ifu).setNorthPane(null);
 
         layeredPane = new JLayeredPane();
         getContentPane().add(layeredPane);
         layeredPane.setPreferredSize(boardSize);
         layeredPane.addMouseListener(this);
         layeredPane.addMouseMotionListener(this);
 
         chessBoard = new BrettRute("src/Kode/Bilder/Sjakkbrett5.png");
         layeredPane.add(chessBoard, JLayeredPane.DEFAULT_LAYER);
         chessBoard.setLayout(new GridLayout(8, 8));
         chessBoard.setPreferredSize(boardSize);
         chessBoard.setBounds(0, 0, boardSize.width, boardSize.height);
 
         for (int i = 0; i < 64; i++) {
             JPanel square = new JPanel(new BorderLayout());
             square.setOpaque(false);
             chessBoard.add(square);
 
         }
 
         //Legger til brikker
         for (int i = 0; i < 8; i++) {
             BrikkeLabel test = new BrikkeLabel(bondeS.getIcon(), bondeS);
             JPanel panel = (JPanel) chessBoard.getComponent(8 + i);
             panel.add(test);
         }
         for (int i = 0; i < 8; i++) {
             BrikkeLabel piece = new BrikkeLabel(bondeH.getIcon(), bondeH);
             JPanel panel = (JPanel) chessBoard.getComponent(48 + i);
             panel.add(piece);
         }
         BrikkeLabel test = new BrikkeLabel(tårnSve.getIcon(), tårnSve);
         JPanel panel = (JPanel) chessBoard.getComponent(0);
         panel.add(test);
         test = new BrikkeLabel(tårnShø.getIcon(), tårnShø);
         panel = (JPanel) chessBoard.getComponent(7);
         panel.add(test);
         test = new BrikkeLabel(tårnhø.getIcon(), tårnhø);
         panel = (JPanel) chessBoard.getComponent(63);
         panel.add(test);
         test = new BrikkeLabel(tårnve.getIcon(), tårnve);
         panel = (JPanel) chessBoard.getComponent(56);
         panel.add(test);
         test = new BrikkeLabel(hestS.getIcon(), hestS);
         panel = (JPanel) chessBoard.getComponent(1);
         panel.add(test);
         test = new BrikkeLabel(hestS.getIcon(), hestS);
         panel = (JPanel) chessBoard.getComponent(6);
         panel.add(test);
         test = new BrikkeLabel(hestH.getIcon(), hestH);
         panel = (JPanel) chessBoard.getComponent(62);
         panel.add(test);
         test = new BrikkeLabel(hestH.getIcon(), hestH);
         panel = (JPanel) chessBoard.getComponent(57);
         panel.add(test);
         test = new BrikkeLabel(løperS.getIcon(), løperS);
         panel = (JPanel) chessBoard.getComponent(5);
         panel.add(test);
         test = new BrikkeLabel(løperS.getIcon(), løperS);
         panel = (JPanel) chessBoard.getComponent(2);
         panel.add(test);
         test = new BrikkeLabel(løperH.getIcon(), løperH);
         panel = (JPanel) chessBoard.getComponent(61);
         panel.add(test);
         test = new BrikkeLabel(løperH.getIcon(), løperH);
         panel = (JPanel) chessBoard.getComponent(58);
         panel.add(test);
         test = new BrikkeLabel(dronningS.getIcon(), dronningS);
         panel = (JPanel) chessBoard.getComponent(3);
         panel.add(test);
         test = new BrikkeLabel(dronningH.getIcon(), dronningH);
         panel = (JPanel) chessBoard.getComponent(59);
         panel.add(test);
         test = new BrikkeLabel(kongeS.getIcon(), kongeS);
         panel = (JPanel) chessBoard.getComponent(4);
         panel.add(test);
         test = new BrikkeLabel(kongeH.getIcon(), kongeH);
         panel = (JPanel) chessBoard.getComponent(60);
         panel.add(test);
 
     }
 
     @Override
     public void mousePressed(MouseEvent e) {
         try {
             chessPiece = null;
             Component c = chessBoard.findComponentAt(e.getX(), e.getY());
 
             if (c instanceof JPanel) {
                 return;
             }
 
             Point parentLocation = c.getParent().getLocation();
             xAdjustment = parentLocation.x - e.getX();
             yAdjustment = parentLocation.y - e.getY();
             chessPiece = (BrikkeLabel) c;
             chessPiece.setLocation(e.getX() + xAdjustment, e.getY() + yAdjustment);
             chessPiece.setSize(chessPiece.getWidth(), chessPiece.getHeight());
             layeredPane.add(chessPiece, JLayeredPane.DRAG_LAYER);
            hjelpIkon = chessPiece.getBrikke().getIcon(); //Hjelpevariabel for midlertidig ikon funksjon
             startPos = chessPiece.getLocation();
         } catch (NullPointerException npe) {
         }
     }
 
     //Flytter brikken rundt
     @Override
     public void mouseDragged(MouseEvent me) {
         if (chessPiece == null) {
             return;
         }
         chessPiece.setLocation(me.getX() + xAdjustment, me.getY() + yAdjustment);
 //        Setter Midlertidig ikon mens brikken blir flyttet
         if (meme) {
             if (chessPiece.getBrikke().getLag() == 1) {
                 chessPiece.setIcon(lolW);
             }
             if (chessPiece.getBrikke().getLag() == 2) {
                 chessPiece.setIcon(lolB);
             }
         }
     }
     
     //Slipper brikken tilbake på brettet
     @Override
     public void mouseReleased(MouseEvent e) {
         flyttBrikke(e);
     }
 
     public void flyttBrikke(MouseEvent e) {
         try {
             if (chessPiece == null) {
                 return;
             }
             //Sjekker om det er hvit eller svart sin tur:
             if ((chessPiece.getBrikke().getLag() == 2 && tur % 2 == 0) || (chessPiece.getBrikke().getLag() == 1 && tur % 2 == 1)) {
                 flyttTilbake();
                 //chessPiece.setIcon(hjelpIkon);
                 return;
             }
             chessPiece.setIcon(hjelpIkon); //setter tilbake til originalt ikon
             Component m = chessBoard.findComponentAt(e.getX(), e.getY());
             Point b;
             if (m instanceof JPanel) {
                 b = m.getLocation();
             } else {
                 b = m.getParent().getLocation();
             }
             if (m instanceof BrikkeLabel) {
                 brikke = (BrikkeLabel) chessBoard.findComponentAt(e.getX(), e.getY());
             }
             xAdjustment = b.x - e.getX();
             yAdjustment = b.y - e.getY();
             //Sjekker om brikken slippes på en annen brikke(og i så fall hvilken farge), eller blank rute
             if (m instanceof BrikkeLabel) {
                 lag = brikke.getBrikke().getLag();
             }
             if (m instanceof JPanel) {
                 lag = 0;
             }
             //Sjekker hva slags brikke som blir flyttet og deretter om det er et lovlig flytt
             if (chessPiece.getBrikke().equals(bondeS)) {
                 if (e.getY() + yAdjustment == startPos.getY() + 150 || e.getY() + yAdjustment == startPos.getY() - 150) {
                     if (chessBoard.findComponentAt((int) startPos.getX(), (int) startPos.getY() + 75) instanceof BrikkeLabel) {
                         flyttTilbake();
                     }
                 }
                 if (bondeS.lovligMove(e.getY() + yAdjustment, e.getX() + xAdjustment, startPos, m, lag)) {
                     //Bytter til dronning når bonde kommer helt over brettet (skal skiftes til valgfri brikke)
                     if (e.getY() + yAdjustment == 525) {
                         chessPiece.setVisible(false);
                         JOptionPane.showMessageDialog(null, "Dronning");
                         chessPiece = new BrikkeLabel(dronningS.getIcon(), dronningS);
                         chessPiece.setVisible(true);
                         flytt(e);
                         return;
                     }
                     flytt(e);
                 } else {
                     flyttTilbake();
                 }
 
             }
             if (chessPiece.getBrikke().equals(bondeH)) {
                 if (e.getY() + yAdjustment == startPos.getY() + 150 || e.getY() + yAdjustment == startPos.getY() - 150) {
                     if (chessBoard.findComponentAt((int) startPos.getX(), (int) startPos.getY() - 75) instanceof BrikkeLabel) {
                         flyttTilbake();
                     }
                 }
                 if (bondeH.lovligMove(e.getY() + yAdjustment, e.getX() + xAdjustment, startPos, m, lag)) {
                     //Bytter til dronning når bonde kommer helt over brettet (skal skiftes til valgfri brikke)
                     if (e.getY() + yAdjustment == 0) {
                         chessPiece.setVisible(false);
                         JOptionPane.showMessageDialog(null, "Dronning");
                         chessPiece = new BrikkeLabel(dronningH.getIcon(), dronningH);
                         chessPiece.setVisible(true);
                         flytt(e);
                         return;
                     }
                     flytt(e);
                 } else {
                     flyttTilbake();
                 }
             }
             if (chessPiece.getBrikke().equals(dronningH)) {
                 int ruter;
                 if (dronningH.lovligMove((int) e.getY() + yAdjustment, (int) e.getX() + xAdjustment, startPos, m, lag)) {
                     if (Math.abs((int) startPos.getX() - ((int) e.getX() + xAdjustment)) == (Math.abs((int) startPos.getY() - ((int) e.getY() + yAdjustment)))) { //if "løper"
                         //Ned venstre
                         if ((int) startPos.getX() > e.getX() + xAdjustment && (int) startPos.getY() < e.getY() + yAdjustment) {
                             ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                             for (int i = 0; i < ruter; i++) {
                                 if (chessBoard.findComponentAt((int) startPos.getX() - (i * 75), (int) startPos.getY() + (i * 75)) instanceof BrikkeLabel) {
                                     flyttTilbake();
                                     return;
                                 }
                             }
                         }
                         //Opp venstre
                         if ((int) startPos.getX() > e.getX() + xAdjustment && (int) startPos.getY() > e.getY() + yAdjustment) {
                             ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                             for (int i = 0; i < ruter; i++) {
                                 if (chessBoard.findComponentAt((int) startPos.getX() - (i * 75), (int) startPos.getY() - (i * 75)) instanceof BrikkeLabel) {
                                     flyttTilbake();
                                     return;
                                 }
                             }
                         }
                         // Opp høyre
                         if ((int) startPos.getX() < e.getX() + xAdjustment && (int) startPos.getY() > e.getY() + yAdjustment) {
                             ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                             for (int i = 0; i < ruter; i++) {
                                 if (chessBoard.findComponentAt((int) startPos.getX() + (i * 75), (int) startPos.getY() - (i * 75)) instanceof BrikkeLabel) {
                                     flyttTilbake();
                                     return;
                                 }
                             }
                         }
                         // Ned høyre
                         if ((int) startPos.getX() < e.getX() + xAdjustment && (int) startPos.getY() < e.getY() + yAdjustment) {
                             ruter = Math.abs(((int) startPos.getX() - (e.getX() + xAdjustment)) / 75);
                             for (int i = 0; i < ruter; i++) {
                                 if (chessBoard.findComponentAt((int) startPos.getX() + (i * 75), (int) startPos.getY() + (i * 75)) instanceof BrikkeLabel) {
                                     flyttTilbake();
                                     return;
                                 }
                             }
                         }
 
                         flytt(e);
                     }
 
                     if (((int) e.getY() + yAdjustment) != (int) startPos.getY() && ((int) e.getX() + xAdjustment) == (int) startPos.getX() || (((int) e.getY() + yAdjustment) == (int) startPos.getY() && ((int) e.getX() + xAdjustment) != (int) startPos.getX())) { //if "tårn"
                         //Y-Retning
                         if (((int) startPos.getX() == e.getX() + xAdjustment) && (((int) e.getY() + yAdjustment) != (int) startPos.getY())) {
                             ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                             if ((int) e.getY() + yAdjustment > (int) startPos.getY()) {
                                 for (int i = 0; i < ruter; i++) {
                                     if (chessBoard.findComponentAt((int) startPos.getX(), (int) startPos.getY() + (i * 75)) instanceof BrikkeLabel) {
                                         flyttTilbake();
                                         return;
                                     }
                                 }
                             } else {
                                 for (int i = 0; i < ruter; i++) {
                                     if (chessBoard.findComponentAt((int) startPos.getX(), (int) startPos.getY() + (i * -75)) instanceof BrikkeLabel) {
                                         flyttTilbake();
                                         return;
                                     }
                                 }
                             }
                         }
                         //X-Retning
                         if (((int) startPos.getY() == e.getY() + yAdjustment) && (((int) e.getX() + xAdjustment) != (int) startPos.getX())) {
                             ruter = Math.abs(((int) startPos.getX() - (e.getX() + xAdjustment)) / 75);
                             if ((int) e.getX() + xAdjustment > (int) startPos.getX()) {
                                 for (int i = 0; i < ruter; i++) {
                                     if (chessBoard.findComponentAt((int) startPos.getX() + (i * 75), (int) startPos.getY()) instanceof BrikkeLabel) {
                                         flyttTilbake();
                                         return;
                                     }
                                 }
                             } else {
                                 for (int i = 0; i < ruter; i++) {
                                     if (chessBoard.findComponentAt((int) startPos.getX() + (i * -75), (int) startPos.getY()) instanceof BrikkeLabel) {
                                         flyttTilbake();
                                         return;
                                     }
                                 }
                             }
                         }
                         flytt(e);
                     }
                 } else {
                     flyttTilbake();
                 }
             }
 
             if (chessPiece.getBrikke().equals(dronningS)) {
                 int ruter;
                 if (dronningS.lovligMove((int) e.getY() + yAdjustment, (int) e.getX() + xAdjustment, startPos, m, lag)) {
                     if (Math.abs((int) startPos.getX() - ((int) e.getX() + xAdjustment)) == (Math.abs((int) startPos.getY() - ((int) e.getY() + yAdjustment)))) { //if "løper"
                         //Ned venstre
                         if ((int) startPos.getX() > e.getX() + xAdjustment && (int) startPos.getY() < e.getY() + yAdjustment) {
                             ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                             for (int i = 0; i < ruter; i++) {
                                 if (chessBoard.findComponentAt((int) startPos.getX() - (i * 75), (int) startPos.getY() + (i * 75)) instanceof BrikkeLabel) {
                                     flyttTilbake();
                                     return;
                                 }
                             }
                         }
                         //Opp venstre
                         if ((int) startPos.getX() > e.getX() + xAdjustment && (int) startPos.getY() > e.getY() + yAdjustment) {
                             ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                             for (int i = 0; i < ruter; i++) {
                                 if (chessBoard.findComponentAt((int) startPos.getX() - (i * 75), (int) startPos.getY() - (i * 75)) instanceof BrikkeLabel) {
                                     flyttTilbake();
                                     return;
                                 }
                             }
                         }
                         // Opp høyre
                         if ((int) startPos.getX() < e.getX() + xAdjustment && (int) startPos.getY() > e.getY() + yAdjustment) {
                             ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                             for (int i = 0; i < ruter; i++) {
                                 if (chessBoard.findComponentAt((int) startPos.getX() + (i * 75), (int) startPos.getY() - (i * 75)) instanceof BrikkeLabel) {
                                     flyttTilbake();
                                     return;
                                 }
                             }
                         }
                         // Ned høyre
                         if ((int) startPos.getX() < e.getX() + xAdjustment && (int) startPos.getY() < e.getY() + yAdjustment) {
                             ruter = Math.abs(((int) startPos.getX() - (e.getX() + xAdjustment)) / 75);
                             for (int i = 0; i < ruter; i++) {
                                 if (chessBoard.findComponentAt((int) startPos.getX() + (i * 75), (int) startPos.getY() + (i * 75)) instanceof BrikkeLabel) {
                                     flyttTilbake();
                                     return;
                                 }
                             }
                         }
 
                         flytt(e);
                     }
 
                     if (((int) e.getY() + yAdjustment) != (int) startPos.getY() && ((int) e.getX() + xAdjustment) == (int) startPos.getX() || (((int) e.getY() + yAdjustment) == (int) startPos.getY() && ((int) e.getX() + xAdjustment) != (int) startPos.getX())) { //if "tårn"
                         //Y-Retning
                         if (((int) startPos.getX() == e.getX() + xAdjustment) && (((int) e.getY() + yAdjustment) != (int) startPos.getY())) {
                             ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                             if ((int) e.getY() + yAdjustment > (int) startPos.getY()) {
                                 for (int i = 0; i < ruter; i++) {
                                     if (chessBoard.findComponentAt((int) startPos.getX(), (int) startPos.getY() + (i * 75)) instanceof BrikkeLabel) {
                                         flyttTilbake();
                                         return;
                                     }
                                 }
                             } else {
                                 for (int i = 0; i < ruter; i++) {
                                     if (chessBoard.findComponentAt((int) startPos.getX(), (int) startPos.getY() + (i * -75)) instanceof BrikkeLabel) {
                                         flyttTilbake();
                                         return;
                                     }
                                 }
                             }
                         }
                         //X-Retning
                         if (((int) startPos.getY() == e.getY() + yAdjustment) && (((int) e.getX() + xAdjustment) != (int) startPos.getX())) {
                             ruter = Math.abs(((int) startPos.getX() - (e.getX() + xAdjustment)) / 75);
                             if ((int) e.getX() + xAdjustment > (int) startPos.getX()) {
                                 for (int i = 0; i < ruter; i++) {
                                     if (chessBoard.findComponentAt((int) startPos.getX() + (i * 75), (int) startPos.getY()) instanceof BrikkeLabel) {
                                         flyttTilbake();
                                         return;
                                     }
                                 }
                             } else {
                                 for (int i = 0; i < ruter; i++) {
                                     if (chessBoard.findComponentAt((int) startPos.getX() + (i * -75), (int) startPos.getY()) instanceof BrikkeLabel) {
                                         flyttTilbake();
                                         return;
                                     }
                                 }
                             }
                         }
                         flytt(e);
                     }
                 } else {
                     flyttTilbake();
                 }
             }
             if (chessPiece.getBrikke().equals(hestS)) {
                 if (hestS.lovligMove(e.getY() + yAdjustment, e.getX() + xAdjustment, startPos, m, lag)) {
                     flytt(e);
                 } else {
                     flyttTilbake();
                 }
             }
             if (chessPiece.getBrikke().equals(hestH)) {
                 if (hestH.lovligMove(e.getY() + yAdjustment, e.getX() + xAdjustment, startPos, m, lag)) {
                     flytt(e);
                 } else {
                     flyttTilbake();
                 }
             }
             if (chessPiece.getBrikke().equals(kongeH)) {
                 if ((e.getY() + yAdjustment == startPos.getY()) && (startPos.getX() - (e.getX() + xAdjustment)) == -150 && !(chessBoard.findComponentAt((e.getX() + xAdjustment),
                         (e.getY() + yAdjustment)) instanceof BrikkeLabel && !(chessBoard.findComponentAt(((int) startPos.getX() + xAdjustment + 75), (int) (startPos.getY() + yAdjustment)) instanceof BrikkeLabel))) {
                     if (kongeH.flyttet() == false && tårnhø.flyttet() == false) {
                         flytt(e);
                         Component c = chessBoard.findComponentAt((int) startPos.getX() + 225, (int) startPos.getY());
                         Container parent = (Container) chessBoard.getComponent(61);
                         parent.add(c);
                         kongeH.setFlyttet();
                         chessPiece.setVisible(true);
                         return;
                     }
                 }
                 if ((e.getY() + yAdjustment == startPos.getY()) && (startPos.getX() - (e.getX() + xAdjustment)) == 150 && !(chessBoard.findComponentAt((e.getX() + xAdjustment),
                         (e.getY() + yAdjustment)) instanceof BrikkeLabel && !(chessBoard.findComponentAt(((int) startPos.getX() + xAdjustment - 75), (int) (startPos.getY() + yAdjustment)) instanceof BrikkeLabel)
                         && !(chessBoard.findComponentAt(((int) startPos.getX() + xAdjustment - 225), (int) (startPos.getY() + yAdjustment)) instanceof BrikkeLabel))) {
                     if (kongeH.flyttet() == false && tårnve.flyttet() == false) {
                         flytt(e);
                         Component c = chessBoard.findComponentAt((int) startPos.getX() - 300, (int) startPos.getY());
                         Container parent = (Container) chessBoard.getComponent(59);
                         parent.add(c);
                         kongeH.setFlyttet();
                         chessPiece.setVisible(true);
                         return;
                     }
                 }
                 if (kongeH.lovligMove(e.getY() + yAdjustment, e.getX() + xAdjustment, startPos, m, lag)) {
                     flytt(e);
                     kongeH.setFlyttet();
                 } else {
                     flyttTilbake();
                 }
             }
 
             if (chessPiece.getBrikke().equals(kongeS)) {
                 if ((e.getY() + yAdjustment == startPos.getY()) && (startPos.getX() - (e.getX() + xAdjustment)) == 150 && !(chessBoard.findComponentAt((e.getX() + xAdjustment),
                         (e.getY() + yAdjustment)) instanceof BrikkeLabel && !(chessBoard.findComponentAt(((int) startPos.getX() + xAdjustment + 75), (int) (startPos.getY() + yAdjustment)) instanceof BrikkeLabel))) {
                     if (kongeS.flyttet() == false && tårnSve.flyttet() == false) {
                         flytt(e);
                         Component c = chessBoard.findComponentAt((int) startPos.getX() - 300, (int) startPos.getY());
                         Container parent = (Container) chessBoard.getComponent(3);
                         parent.add(c);
                         kongeS.setFlyttet();
                         chessPiece.setVisible(true);
                         return;
                     }
                 }
                 if ((e.getY() + yAdjustment == startPos.getY()) && (startPos.getX() - (e.getX() + xAdjustment)) == -150 && !(chessBoard.findComponentAt((e.getX() + xAdjustment),
                         (e.getY() + yAdjustment)) instanceof BrikkeLabel && !(chessBoard.findComponentAt(((int) startPos.getX() + xAdjustment - 75), (int) (startPos.getY() + yAdjustment)) instanceof BrikkeLabel)
                         && !(chessBoard.findComponentAt(((int) startPos.getX() + xAdjustment - 225), (int) (startPos.getY() + yAdjustment)) instanceof BrikkeLabel))) {
                     if (kongeS.flyttet() == false && tårnShø.flyttet() == false) {
                         flytt(e);
                         Component c = chessBoard.findComponentAt((int) startPos.getX() + 225, (int) startPos.getY());
                         Container parent = (Container) chessBoard.getComponent(5);
                         parent.add(c);
                         kongeS.setFlyttet();
                         chessPiece.setVisible(true);
                         return;
                     }
                 }
                 if (kongeH.lovligMove(e.getY() + yAdjustment, e.getX() + xAdjustment, startPos, m, lag)) {
                     flytt(e);
                     kongeH.setFlyttet();
                 } else {
                     flyttTilbake();
                 }
             }
             if (chessPiece.getBrikke().equals(løperH)) {
                 //Ned venstre
                 int ruter;
                 if (løperH.lovligMove((int) e.getY() + yAdjustment, (int) e.getX() + xAdjustment, startPos, m, lag)) {
                     if ((int) startPos.getX() > e.getX() + xAdjustment && (int) startPos.getY() < e.getY() + yAdjustment) {
                         ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                         for (int i = 0; i < ruter; i++) {
                             if (chessBoard.findComponentAt((int) startPos.getX() - (i * 75), (int) startPos.getY() + (i * 75)) instanceof BrikkeLabel) {
                                 flyttTilbake();
                                 return;
                             }
                         }
                     }
                     //Opp venstre
                     if ((int) startPos.getX() > e.getX() + xAdjustment && (int) startPos.getY() > e.getY() + yAdjustment) {
                         ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                         for (int i = 0; i < ruter; i++) {
                             if (chessBoard.findComponentAt((int) startPos.getX() - (i * 75), (int) startPos.getY() - (i * 75)) instanceof BrikkeLabel) {
                                 flyttTilbake();
                                 return;
                             }
                         }
                     }
                     // Opp høyre
                     if ((int) startPos.getX() < e.getX() + xAdjustment && (int) startPos.getY() > e.getY() + yAdjustment) {
                         ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                         for (int i = 0; i < ruter; i++) {
                             if (chessBoard.findComponentAt((int) startPos.getX() + (i * 75), (int) startPos.getY() - (i * 75)) instanceof BrikkeLabel) {
                                 flyttTilbake();
                                 return;
                             }
                         }
                     }
                     // Ned høyre
                     if ((int) startPos.getX() < e.getX() + xAdjustment && (int) startPos.getY() < e.getY() + yAdjustment) {
                         ruter = Math.abs(((int) startPos.getX() - (e.getX() + xAdjustment)) / 75);
                         for (int i = 0; i < ruter; i++) {
                             if (chessBoard.findComponentAt((int) startPos.getX() + (i * 75), (int) startPos.getY() + (i * 75)) instanceof BrikkeLabel) {
                                 flyttTilbake();
                                 return;
                             }
                         }
                     }
                     flytt(e);
                 } else {
                     flyttTilbake();
                 }
             }
             if (chessPiece.getBrikke().equals(løperS)) {
                 //Ned venstre
                 int ruter;
                 if (løperS.lovligMove((int) e.getY() + yAdjustment, (int) e.getX() + xAdjustment, startPos, m, lag)) {
                     if ((int) startPos.getX() > e.getX() + xAdjustment && (int) startPos.getY() < e.getY() + yAdjustment) {
                         ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                         for (int i = 0; i < ruter; i++) {
                             if (chessBoard.findComponentAt((int) startPos.getX() - (i * 75), (int) startPos.getY() + (i * 75)) instanceof BrikkeLabel) {
                                 flyttTilbake();
                                 return;
                             }
                         }
                     }
                     //Opp venstre
                     if ((int) startPos.getX() > e.getX() + xAdjustment && (int) startPos.getY() > e.getY() + yAdjustment) {
                         ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                         for (int i = 0; i < ruter; i++) {
                             if (chessBoard.findComponentAt((int) startPos.getX() - (i * 75), (int) startPos.getY() - (i * 75)) instanceof BrikkeLabel) {
                                 flyttTilbake();
                                 return;
                             }
                         }
                     }
                     // Opp høyre
                     if ((int) startPos.getX() < e.getX() + xAdjustment && (int) startPos.getY() > e.getY() + yAdjustment) {
                         ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                         for (int i = 0; i < ruter; i++) {
                             if (chessBoard.findComponentAt((int) startPos.getX() + (i * 75), (int) startPos.getY() - (i * 75)) instanceof BrikkeLabel) {
                                 flyttTilbake();
                                 return;
                             }
                         }
                     }
                     // Ned høyre
                     if ((int) startPos.getX() < e.getX() + xAdjustment && (int) startPos.getY() < e.getY() + yAdjustment) {
                         ruter = Math.abs(((int) startPos.getX() - (e.getX() + xAdjustment)) / 75);
                         for (int i = 0; i < ruter; i++) {
                             if (chessBoard.findComponentAt((int) startPos.getX() + (i * 75), (int) startPos.getY() + (i * 75)) instanceof BrikkeLabel) {
                                 flyttTilbake();
                                 return;
                             }
                         }
                     }
                     flytt(e);
                 } else {
                     flyttTilbake();
                 }
             }
             if (chessPiece.getBrikke().equals(tårnH)) {
                 int ruter;
                 if (tårnH.lovligMove((int) e.getY() + yAdjustment, (int) e.getX() + xAdjustment, startPos, m, lag)) {
                     //Y-Retning
                     if ((int) startPos.getX() == e.getX() + xAdjustment) {
                         ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                         if ((int) e.getY() + yAdjustment > (int) startPos.getY()) {
                             for (int i = 0; i < ruter; i++) {
                                 if (chessBoard.findComponentAt((int) startPos.getX(), (int) startPos.getY() + (i * 75)) instanceof BrikkeLabel) {
                                     flyttTilbake();
                                     return;
                                 }
                             }
                         } else {
                             for (int i = 0; i < ruter; i++) {
                                 if (chessBoard.findComponentAt((int) startPos.getX(), (int) startPos.getY() + (i * -75)) instanceof BrikkeLabel) {
                                     flyttTilbake();
                                     return;
                                 }
                             }
                         }
                     }
                     //X-Retning
                     if ((int) startPos.getY() == e.getY() + yAdjustment) {
                         ruter = Math.abs(((int) startPos.getX() - (e.getX() + xAdjustment)) / 75);
                         if ((int) e.getX() + xAdjustment > (int) startPos.getX()) {
                             for (int i = 0; i < ruter; i++) {
                                 if (chessBoard.findComponentAt((int) startPos.getX() + (i * 75), (int) startPos.getY()) instanceof BrikkeLabel) {
                                     flyttTilbake();
                                     return;
                                 }
                             }
                         } else {
                             for (int i = 0; i < ruter; i++) {
                                 if (chessBoard.findComponentAt((int) startPos.getX() + (i * -75), (int) startPos.getY()) instanceof BrikkeLabel) {
                                     flyttTilbake();
                                     return;
                                 }
                             }
                         }
                     }
                     flytt(e);
                 } else {
                     flyttTilbake();
                 }
             }
             if (chessPiece.getBrikke().equals(tårnS)) {
                 int ruter;
                 if (tårnS.lovligMove((int) e.getY() + yAdjustment, (int) e.getX() + xAdjustment, startPos, m, lag)) {
                     //Y-Retning
                     if ((int) startPos.getX() == e.getX() + xAdjustment) {
                         ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                         if ((int) e.getY() + yAdjustment > (int) startPos.getY()) {
                             for (int i = 0; i < ruter; i++) {
                                 if (chessBoard.findComponentAt((int) startPos.getX(), (int) startPos.getY() + (i * 75)) instanceof BrikkeLabel) {
                                     flyttTilbake();
                                     return;
                                 }
                             }
                         } else {
                             for (int i = 0; i < ruter; i++) {
                                 if (chessBoard.findComponentAt((int) startPos.getX(), (int) startPos.getY() + (i * -75)) instanceof BrikkeLabel) {
                                     flyttTilbake();
                                     return;
                                 }
                             }
                         }
                     }
                     //X-Retning
                     if ((int) startPos.getY() == e.getY() + yAdjustment) {
                         ruter = Math.abs(((int) startPos.getX() - (e.getX() + xAdjustment)) / 75);
                         if ((int) e.getX() + xAdjustment > (int) startPos.getX()) {
                             for (int i = 0; i < ruter; i++) {
                                 if (chessBoard.findComponentAt((int) startPos.getX() + (i * 75), (int) startPos.getY()) instanceof BrikkeLabel) {
                                     flyttTilbake();
                                     return;
                                 }
                             }
                         } else {
                             for (int i = 0; i < ruter; i++) {
                                 if (chessBoard.findComponentAt((int) startPos.getX() + (i * -75), (int) startPos.getY()) instanceof BrikkeLabel) {
                                     flyttTilbake();
                                     return;
                                 }
                             }
                         }
                     }
                     flytt(e);
                 } else {
                     flyttTilbake();
                 }
             }
 
             chessPiece.setVisible(true);
         } catch (NullPointerException npe) {
             flyttTilbake();
         }
     }
 
     public void flytt(MouseEvent e) {
         //Hvis det står en brikke i ruten allerede blir denne erstattet av den som droppes, om det er en blank rute blir den bare addet til kontaineren i den ruten
         chessPiece.setVisible(false);
         Component c = chessBoard.findComponentAt(e.getX(), e.getY());
 
         if (c instanceof BrikkeLabel) {
             Container parent = c.getParent();
             parent.remove(0);
             parent.add(chessPiece);
             chessBoard.repaint(10);
         } else {
             Container parent = (Container) c;
             parent.add(chessPiece);
             chessBoard.repaint(10);
         }
         if (chessPiece.getBrikke().getLag() == 1) {
             hvitLogg.leggTilLogg(chessPiece.getBrikke().getNavn() + " fra " + kord.getKoord(startPos) + " til " + kord.getKoord((e.getX() + xAdjustment), (e.getY() + yAdjustment)));
         }
         if (chessPiece.getBrikke().getLag() == 2) {
             svartLogg.leggTilLogg(chessPiece.getBrikke().getNavn() + " fra " + kord.getKoord(startPos) + " til " + kord.getKoord((e.getX() + xAdjustment), (e.getY() + yAdjustment)));
         }
         _fireSjakkEvent();
         tur++;
         chessPiece.setVisible(true);
     }
 
     public void flyttTilbake() {
         //Flytter brikken tilbake dit den ble plukket opp
         chessPiece.setVisible(false);
         Component c = chessBoard.findComponentAt(startPos);
 
         if (c instanceof BrikkeLabel) {
             Container parent = c.getParent();
             parent.remove(0);
             parent.add(chessPiece);
             chessBoard.repaint(10);
         } else {
             Container parent = (Container) c;
             parent.add(chessPiece);
             chessBoard.repaint(10);
         }
         chessPiece.setVisible(true);
     }
 
     public void kongeSPos() {
         for (int i = 0; i < 64; i++) {
             if (brikkeType(kord.getPunkt(i)).equals(kongeS)) {
                 kongeSpos = kord.getPunkt(i);
             }
         }
     }
 
     public void kongeHPos() {
         for (int i = 0; i < 64; i++) {
             if (brikkeType(kord.getPunkt(i)).equals(kongeH)) {
                 kongeHpos = kord.getPunkt(i);
             }
         }
     }
 
     public boolean sjekkSjakkS() {
         if (brikkeType((int) (kongeSpos.getX() - 75), (int) (kongeSpos.getY() - 150)).equals(hestH)) {
         }
         return true;
     }
 
     public boolean sjekkSjakkH() {
         return true;
     }
 
     public Brikke brikkeType(Point a) {
         Component c = chessBoard.findComponentAt(a);
         if (c instanceof BrikkeLabel) {
             BrikkeLabel b = (BrikkeLabel) c;
             return b.getBrikke();
         }
         return null;
     }
 
     public Brikke brikkeType(int x, int y) {
         Point a = new Point(x, y);
         Component c = chessBoard.findComponentAt(a);
         if (c instanceof BrikkeLabel) {
             BrikkeLabel b = (BrikkeLabel) c;
             return b.getBrikke();
         }
         return null;
     }
 
     public void refresh() {
         layeredPane.remove(chessBoard);
         layeredPane.add(chessBoard);
         layeredPane.revalidate();
         layeredPane.repaint();
     }
 
     public String getSvartLogg() {
         return svartLogg.toString();
     }
 
     public String getHvitLogg() {
         return hvitLogg.toString();
     }
 
     @Override
     public void mouseClicked(MouseEvent e) {
     }
 
     @Override
     public void mouseMoved(MouseEvent e) {
     }
 
     @Override
     public void mouseEntered(MouseEvent e) {
     }
 
     @Override
     public void mouseExited(MouseEvent e) {
     }
 
     public void tilTabell() {
         for (int i = 0; i < 64; i++) {
             if (chessBoard.findComponentAt(kord.getPunkt(i)) instanceof BrikkeLabel) {
                 sjakkTabell.oppdaterTabell((BrikkeLabel) chessBoard.findComponentAt(kord.getPunkt(i)), i);
             }
         }
         sjakkTabell.oppdaterLogg(getHvitLogg(), 0);
         sjakkTabell.oppdaterLogg(getSvartLogg(), 1);
     }
 
     public void fraTabell() {
         for (int i = 0; i < 64; i++) {
             Component c = sjakkTabell.hentFraTabell(i);
             if (c instanceof BrikkeLabel) {
                 JPanel panel = (JPanel) chessBoard.getComponent(i);
                 panel.add(c);
             }
         }
         hvitLogg.clearLogg();
         svartLogg.clearLogg();
         hvitLogg.leggTilLogg(sjakkTabell.getLogg(0));
         svartLogg.leggTilLogg(sjakkTabell.getLogg(1));
     }
 
     public synchronized void addSjakkListener(SjakkListener l) {
         _listeners.add(l);
     }
 
     public synchronized void removeSjakkListener(SjakkListener l) {
         _listeners.remove(l);
     }
 
     private synchronized void _fireSjakkEvent() {
         SjakkEvent sjakkEvent = new SjakkEvent(this, chessPiece.getBrikke().getLag());
         Iterator listeners = _listeners.iterator();
         while (listeners.hasNext()) {
             ((SjakkListener) listeners.next()).sjakkReceived(sjakkEvent);
         }
     }
 
     public void endreUI(int i) {
        if (meme) {
            meme = false;
        } else {
             meme = true;
         }
         sjakkTabell.nullstill();
         tilTabell();
         sjakkTabell.endreUI(i);
         fraTabell();
         refresh();
     }
 }
