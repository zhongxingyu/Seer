 package Code;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.util.ArrayList;
 import java.util.Iterator;
 import javax.swing.*;
 import static javax.swing.JOptionPane.*;
 
 public class Chess extends JInternalFrame implements MouseListener, MouseMotionListener {
 
     private boolean passanten;
     private int enPassantB;
     private int enPassantW;
     private Point enPassantPW;
     private Point enPassantPB;
     private java.util.List _listeners = new ArrayList();
     private Point startPos;
     private int xAdjustment;
     private int yAdjustment;
     private int turn = 2;
     private int team;
     private Log blackLog = new Log();
     private Log whiteLog = new Log();
     private Coordinates kord = new Coordinates();
     private ChessTable chessTable = new ChessTable();
     private JLayeredPane layeredPane;
     private JLabel chessBoard;
     private PieceLabel chessPiece;
     private PieceLabel piece;
     private boolean meme = false;
     private Icon hjelpIkon; //Hjelpevariabel for midlertidig ikon funksjon
     private Icon lolW = new ImageIcon(getClass().getResource("/Pictures/nyancat2.gif"));
     private Icon lolB = new ImageIcon(getClass().getResource("/Pictures/nyancat3.gif"));
     private PawnB pawnB = new PawnB(new ImageIcon(getClass().getResource("/Pictures/PawnB.png")));
     private PawnW pawnW = new PawnW(new ImageIcon(getClass().getResource("/Pictures/PawnW.png")));
     private RookB rookB = new RookB(new ImageIcon(getClass().getResource("/Pictures/RookB.png")));
     private RookW rookW = new RookW(new ImageIcon(getClass().getResource("/Pictures/RookW.png")));
     private RookB rookBright = rookB;
     private RookB rookBleft = rookB;
     private RookW rookWleft = rookW;
     private RookW rookWright = rookW;
     private KnightB knightB = new KnightB(new ImageIcon(getClass().getResource("/Pictures/KnightB.png")));
     private KnightW knightW = new KnightW(new ImageIcon(getClass().getResource("/Pictures/KnightW.png")));
     private BishopB bishopB = new BishopB(new ImageIcon(getClass().getResource("/Pictures/BishopB.png")));
     private BishopW bishopW = new BishopW(new ImageIcon(getClass().getResource("/Pictures/BishopW.png")));
     private QueenB queenB = new QueenB(new ImageIcon(getClass().getResource("/Pictures/QueenB.png")));
     private QueenW queenW = new QueenW(new ImageIcon(getClass().getResource("/Pictures/QueenW.png")));
     private KingB kingB = new KingB(new ImageIcon(getClass().getResource("/Pictures/KingB.png")));
     private KingW kingW = new KingW(new ImageIcon(getClass().getResource("/Pictures/KingW.png")));
 
     public Chess() {
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
 
         chessBoard = new JLabel(new ImageIcon(getClass().getResource("/Pictures/Chessboard.png")));
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
             PieceLabel test = new PieceLabel(pawnB.getIcon(), pawnB);
             JPanel panel = (JPanel) chessBoard.getComponent(8 + i);
             panel.add(test);
         }
         for (int i = 0; i < 8; i++) {
             PieceLabel piece = new PieceLabel(pawnW.getIcon(), pawnW);
             JPanel panel = (JPanel) chessBoard.getComponent(48 + i);
             panel.add(piece);
         }
         PieceLabel test = new PieceLabel(rookBleft.getIcon(), rookBleft);
         JPanel panel = (JPanel) chessBoard.getComponent(0);
         panel.add(test);
         test = new PieceLabel(rookBright.getIcon(), rookBright);
         panel = (JPanel) chessBoard.getComponent(7);
         panel.add(test);
         test = new PieceLabel(rookWright.getIcon(), rookWright);
         panel = (JPanel) chessBoard.getComponent(63);
         panel.add(test);
         test = new PieceLabel(rookWleft.getIcon(), rookWleft);
         panel = (JPanel) chessBoard.getComponent(56);
         panel.add(test);
         test = new PieceLabel(knightB.getIcon(), knightB);
         panel = (JPanel) chessBoard.getComponent(1);
         panel.add(test);
         test = new PieceLabel(knightB.getIcon(), knightB);
         panel = (JPanel) chessBoard.getComponent(6);
         panel.add(test);
         test = new PieceLabel(knightW.getIcon(), knightW);
         panel = (JPanel) chessBoard.getComponent(62);
         panel.add(test);
         test = new PieceLabel(knightW.getIcon(), knightW);
         panel = (JPanel) chessBoard.getComponent(57);
         panel.add(test);
         test = new PieceLabel(bishopB.getIcon(), bishopB);
         panel = (JPanel) chessBoard.getComponent(5);
         panel.add(test);
         test = new PieceLabel(bishopB.getIcon(), bishopB);
         panel = (JPanel) chessBoard.getComponent(2);
         panel.add(test);
         test = new PieceLabel(bishopW.getIcon(), bishopW);
         panel = (JPanel) chessBoard.getComponent(61);
         panel.add(test);
         test = new PieceLabel(bishopW.getIcon(), bishopW);
         panel = (JPanel) chessBoard.getComponent(58);
         panel.add(test);
         test = new PieceLabel(queenB.getIcon(), queenB);
         panel = (JPanel) chessBoard.getComponent(3);
         panel.add(test);
         test = new PieceLabel(queenW.getIcon(), queenW);
         panel = (JPanel) chessBoard.getComponent(59);
         panel.add(test);
         test = new PieceLabel(kingB.getIcon(), kingB);
         panel = (JPanel) chessBoard.getComponent(4);
         panel.add(test);
         test = new PieceLabel(kingW.getIcon(), kingW);
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
             chessPiece = (PieceLabel) c;
             chessPiece.setLocation(e.getX() + xAdjustment, e.getY() + yAdjustment);
             chessPiece.setSize(chessPiece.getWidth(), chessPiece.getHeight());
             layeredPane.add(chessPiece, JLayeredPane.DRAG_LAYER);
             hjelpIkon = chessPiece.getIcon(); //Hjelpevariabel for midlertidig ikon funksjon
             startPos = chessPiece.getLocation();
         } catch (NullPointerException npe) {
         }
     }
 
     //moveer piecen rundt
     @Override
     public void mouseDragged(MouseEvent me) {
         if (chessPiece == null) {
             return;
         }
         chessPiece.setLocation(me.getX() + xAdjustment, me.getY() + yAdjustment);
 //        Setter Midlertidig ikon mens piecen blir move
         if (meme) {
             if (chessPiece.getPiece().getTeam() == 1) {
                 chessPiece.setIcon(lolW);
             }
             if (chessPiece.getPiece().getTeam() == 2) {
                 chessPiece.setIcon(lolB);
             }
         }
     }
 
     //Slipper piecen tilbake på brettet
     @Override
     public void mouseReleased(MouseEvent e) {
         if (chessBoard.findComponentAt(e.getX(), e.getY()) instanceof PieceLabel) {
             piece = (PieceLabel) chessBoard.findComponentAt(e.getX(), e.getY());
         } else {
             piece = null;
         }
         if (chessPiece == null) {
             return;
         }
         //Sjekker om det er hvit eller svart sin tur:
         if ((chessPiece.getPiece().getTeam() == 2 && turn % 2 == 0) || (chessPiece.getPiece().getTeam() == 1 && turn % 2 == 1)) {
             moveBack();
             //chessPiece.setIcon(hjelpIkon);
             return;
         }
         if (meme) {
             chessPiece.setIcon(hjelpIkon); //setter tilbake til originalt ikon
         }
         Component m = chessBoard.findComponentAt(e.getX(), e.getY());
         Point b;
         if (m instanceof JPanel) {
             b = m.getLocation();
         } else {
             b = m.getParent().getLocation();
         }
         if (m instanceof PieceLabel) {
             piece = (PieceLabel) chessBoard.findComponentAt(e.getX(), e.getY());
         }
         xAdjustment = b.x - e.getX();
         yAdjustment = b.y - e.getY();
         //Sjekker om piecen slippes på en annen piece(og i så fall hvilken farge), eller blank rute
         if (m instanceof PieceLabel) {
             team = piece.getPiece().getTeam();
         }
         if (m instanceof JPanel) {
             team = 0;
         }
         if (movepiece(e, m)) {
             turnChange(e);
         }
         refresh();
         toTable();
         System.out.println(passanten);
         passanten = false;
         System.out.println("Sjakk Hvit: " + chessTable.checkW(kingWpos()));
         System.out.println("Sjakk Svart: " + chessTable.checkB(kingBpos()));
     }
 
     public boolean movepiece(MouseEvent e, Component m) {
         boolean moved = false;
         try {
             //Sjekker hva steams brikke som blir move og deretter om det er et lovlig move
             if (chessPiece.getPiece().equals(pawnB)) {
                 if (movePawnB(e, m)) {
                     moved = true;
                     pawnW.setPassant(false);
                 }
             }
             if (chessPiece.getPiece().equals(pawnW)) {
                 if (movePawnW(e, m)) {
                     moved = true;
                     pawnB.setPassant(false);
                 }
 
             }
             if (chessPiece.getPiece().equals(queenW)) {
                 if (moveQueenW(e, m)) {
                     moved = true;
                     pawnB.setPassant(false);
                 }
             }
 
             if (chessPiece.getPiece().equals(queenB)) {
                 if (moveQueenB(e, m)) {
                     moved = true;
                     pawnW.setPassant(false);
                 }
             }
             if (chessPiece.getPiece().equals(knightB)) {
                 if (moveKnightB(e, m)) {
                     moved = true;
                     pawnW.setPassant(false);
                 }
             }
             if (chessPiece.getPiece().equals(knightW)) {
                 if (moveKnightW(e, m)) {
                     moved = true;
                     pawnB.setPassant(false);
                 }
             }
             if (chessPiece.getPiece().equals(kingW)) {
                 if (moveKingW(e, m)) {
                     moved = true;
                     pawnB.setPassant(false);
                 }
             }
 
             if (chessPiece.getPiece().equals(kingB)) {
                 if (moveKingB(e, m)) {
                     moved = true;
                     pawnW.setPassant(false);
                 }
             }
             if (chessPiece.getPiece().equals(bishopW)) {
                 if (moveBishopW(e, m)) {
                     moved = true;
                     pawnB.setPassant(false);
                 }
             }
             if (chessPiece.getPiece().equals(bishopB)) {
                 if (moveBishopB(e, m)) {
                     moved = true;
                     pawnW.setPassant(false);
                 }
             }
             if (chessPiece.getPiece().equals(rookW)) {
                 if (moveRookW(e, m)) {
                     moved = true;
                     pawnB.setPassant(false);
                 }
             }
             if (chessPiece.getPiece().equals(rookB)) {
                 if (moveRookB(e, m)) {
                     moved = true;
                     pawnW.setPassant(false);
                 }
             }
             if (enPassantPW != null) {
                 if ((enPassantPW.getY() == (e.getY() + yAdjustment)) && (enPassantPW.getX() == (e.getX() + xAdjustment))) {
                     if (enPassant() == true) {
                         moved = true;
                         pawnB.setPassant(false);
                         passanten = true;
                     }
                 }
             }
             if (enPassantPB != null) {
                 if ((enPassantPB.getY() == (e.getY() + yAdjustment)) && (enPassantPB.getX() == (e.getX() + xAdjustment))) {
                     if (enPassant() == true) {
                         moved = true;
                         pawnW.setPassant(false);
                         passanten = true;
                     }
                 }
             }
             chessPiece.setVisible(true);
             if (chessPiece.getPiece().getTeam() == 1 && chessTable.checkW(kingWpos()) == true) {
                 if (piece instanceof PieceLabel) {
                     replacePiece(e, piece);
                 }
                 moveBack();
                 return false;
             }
             if (chessPiece.getPiece().getTeam() == 2 && chessTable.checkB(kingBpos()) == true) {
                 if (piece instanceof PieceLabel) {
                     replacePiece(e, piece);
                 }
                 moveBack();
                 return false;
             }
 
         } catch (NullPointerException npe) {
             System.out.println("Nullpointer");
             moveBack();
             return false;
         }
         if (moved) {
             return true;
         } else {
             return false;
         }
     }
 
     public void move(MouseEvent e) {
         //Hvis det står en piece i ruten allerede blir denne erstattet av den som droppes, om det er en blank rute blir den bare addet til kontaineren i den ruten
         chessPiece.setVisible(false);
         Component c = chessBoard.findComponentAt(e.getX(), e.getY());
 
         if (c instanceof PieceLabel) {
             Container parent = c.getParent();
             parent.remove(0);
             parent.add(chessPiece);
         } else {
             Container parent = (Container) c;
             parent.add(chessPiece);
         }
         chessPiece.setVisible(true);
         toTable();
     }
 
     public void turnChange(MouseEvent e) {
         if (chessPiece.getPiece().getTeam() == 1) {
             whiteLog.leggTilLogg(chessPiece.getPiece().getName() + " from " + kord.getKoord(startPos) + " to " + kord.getKoord((e.getX() + xAdjustment), (e.getY() + yAdjustment)));
         }
         if (chessPiece.getPiece().getTeam() == 2) {
             blackLog.leggTilLogg(chessPiece.getPiece().getName() + " from " + kord.getKoord(startPos) + " to " + kord.getKoord((e.getX() + xAdjustment), (e.getY() + yAdjustment)));
         }
         fireChessEvent();
         turn++;
     }
 
     public void moveBack() {
         //flytter piecen tilbake dit den ble plukket opp
         chessPiece.setVisible(false);
         chessPiece.setIcon(hjelpIkon);
         Component c = chessBoard.findComponentAt(startPos);
 
         if (c instanceof PieceLabel) {
             Container parent = c.getParent();
             parent.remove(0);
             parent.add(chessPiece);
         } else {
             Container parent = (Container) c;
             parent.add(chessPiece);
         }
         chessPiece.setVisible(true);
         toTable();
     }
 
     public int kingBpos() {
         for (int i = 0; i < 64; i++) {
             if (chessBoard.findComponentAt(kord.getPunkt(i)) instanceof PieceLabel) {
                 if (pieceType(kord.getPunkt(i)).equals(kingB)) {
                     return i;
                 }
             }
         }
         return -1;
     }
 
     public int kingWpos() {
         for (int i = 0; i < 64; i++) {
             if (chessBoard.findComponentAt(kord.getPunkt(i)) instanceof PieceLabel) {
                 if (pieceType(kord.getPunkt(i)).equals(kingW)) {
                     return i;
                 }
             }
         }
         return -1;
     }
 
     public Piece pieceType(Point a) {
         Component c = chessBoard.findComponentAt(a);
         if (c instanceof PieceLabel) {
             PieceLabel b = (PieceLabel) c;
             return b.getPiece();
         }
         return null;
     }
 
     public Piece pieceType(int x, int y) {
         Point a = new Point(x, y);
         Component c = chessBoard.findComponentAt(a);
         if (c instanceof PieceLabel) {
             PieceLabel b = (PieceLabel) c;
             return b.getPiece();
         }
         return null;
     }
 
     public void refresh() {
         layeredPane.remove(chessBoard);
         layeredPane.add(chessBoard);
         layeredPane.revalidate();
         layeredPane.repaint();
     }
 
     public String getBlackLog() {
         return blackLog.toString();
     }
 
     public String getWhiteLog() {
         return whiteLog.toString();
     }
 
     public void replacePiece(MouseEvent e, PieceLabel p) {
         chessPiece.setVisible(false);
         Container c = (JPanel) chessBoard.findComponentAt(e.getX(), e.getY());
         c.add(p);
         chessPiece.setVisible(true);
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
 
     public void toTable() {
         refresh();
         chessTable.reset();
         for (int i = 0; i < 64; i++) {
             if (chessBoard.findComponentAt(kord.getPunkt(i)) instanceof PieceLabel) {
                 chessTable.updateTable((PieceLabel) chessBoard.findComponentAt(kord.getPunkt(i)), i);
             }
         }
         chessTable.updateLog(getWhiteLog(), 0);
         chessTable.updateLog(getBlackLog(), 1);
         chessTable.updateTwoTable();
     }
 
     public void fromTable() {
         for (int i = 0; i < 64; i++) {
             Component c = chessTable.getTable(i);
             if (c instanceof PieceLabel) {
                 JPanel panel = (JPanel) chessBoard.getComponent(i);
                 panel.add(c);
             }
         }
         whiteLog.clearLogg();
         blackLog.clearLogg();
        whiteLog.leggTilLogg(chessTable.getLog(0));
        blackLog.leggTilLogg(chessTable.getLog(1));
     }
 
     public synchronized void addChessListener(ChessListener l) {
         _listeners.add(l);
     }
 
     public synchronized void removeChessListener(ChessListener l) {
         _listeners.remove(l);
     }
 
     private synchronized void fireChessEvent() {
         if (piece != null) {
             ChessEvent chessEvent = new ChessEvent(this, chessPiece.getPiece().getTeam(), piece.getPiece());
             chessEvent.setPassant(passanten);
             Iterator listeners = _listeners.iterator();
             while (listeners.hasNext()) {
                 ((ChessListener) listeners.next()).chessReceived(chessEvent);
             }
         } else {
             ChessEvent chessEvent = new ChessEvent(this, chessPiece.getPiece().getTeam());
             chessEvent.setPassant(passanten);
             Iterator listeners = _listeners.iterator();
             while (listeners.hasNext()) {
                 ((ChessListener) listeners.next()).chessReceived(chessEvent);
             }
         }
     }
 
     public void changeUI(int i) {
         if (i == 1) {
             meme = true;
         }
         if (i == 2) {
             meme = false;
         }
         chessTable.reset();
         toTable();
         chessTable.changeUI(i);
         fromTable();
         refresh();
     }
 
     public void optionDialog(int lag) {
         JButton button1 = new JButton(knightB.getIcon());
         JButton button2 = new JButton(knightW.getIcon());
         JButton button3 = new JButton(queenB.getIcon());
         JButton button4 = new JButton(queenW.getIcon());
         button1.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 chessPiece = new PieceLabel(knightB.getIcon(), knightB);
             }
         });
         button2.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 chessPiece = new PieceLabel(knightW.getIcon(), knightW);
             }
         });
         button3.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 chessPiece = new PieceLabel(queenB.getIcon(), queenB);
             }
         });
         button4.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 chessPiece = new PieceLabel(queenW.getIcon(), queenW);
             }
         });
 
         if (chessPiece.getPiece().getTeam() == 1) {
             Object[] group = {button2, button4};
             showConfirmDialog(null, group, "Choose piece", OK_OPTION, QUESTION_MESSAGE, pawnW.getIcon());
         }
         if (chessPiece.getPiece().getTeam() == 2) {
             Object[] group = {button1, button3};
             showConfirmDialog(null, group, "Choose piece", OK_OPTION, QUESTION_MESSAGE, pawnB.getIcon());
         }
 
     }
 
     public boolean movePawnB(MouseEvent e, Component m) {
         if (e.getY() + yAdjustment == startPos.getY() + 150 || e.getY() + yAdjustment == startPos.getY() - 150) {
             enPassantPB = (new Point((int) startPos.getX(), (int) startPos.getY() + 75));
             enPassantB = turn + 2;
             if (chessBoard.findComponentAt((int) startPos.getX(), (int) startPos.getY() + 75) instanceof PieceLabel) {
                 moveBack();
                 return false;
             }
         }
         if (pawnB.legalMove(e.getY() + yAdjustment, e.getX() + xAdjustment, startPos, m, team)) {
             //Bytter til dronning når bonde kommer helt over brettet (skal skiftes til valgfri brikke)
             if (e.getY() + yAdjustment == 525) {
                 chessPiece.setVisible(false);
                 optionDialog(chessPiece.getPiece().getTeam());
                 chessPiece.setVisible(true);
                 move(e);
                 return true;
             }
             move(e);
             return true;
         } else {
             moveBack();
             return false;
         }
     }
 
     public boolean movePawnW(MouseEvent e, Component m) {
         if (e.getY() + yAdjustment == startPos.getY() + 150 || e.getY() + yAdjustment == startPos.getY() - 150) {
             enPassantPW = (new Point((int) startPos.getX(), (int) startPos.getY() - 75));
             enPassantW = turn + 2;
             if (chessBoard.findComponentAt((int) startPos.getX(), (int) startPos.getY() - 75) instanceof PieceLabel) {
                 moveBack();
                 return false;
             }
         }
         if (pawnW.legalMove(e.getY() + yAdjustment, e.getX() + xAdjustment, startPos, m, team)) {
             //Bytter til dronning når bonde kommer helt over brettet (skal skiftes til valgfri piece)
             if (e.getY() + yAdjustment == 0) {
                 chessPiece.setVisible(false);
                 optionDialog(chessPiece.getPiece().getTeam());
                 chessPiece.setVisible(true);
                 move(e);
                 return true;
             }
             move(e);
             return true;
         } else {
             moveBack();
             return false;
         }
     }
 
     public boolean moveQueenW(MouseEvent e, Component m) {
         int ruter;
         if (queenW.legalMove((int) e.getY() + yAdjustment, (int) e.getX() + xAdjustment, startPos, m, team)) {
             if (Math.abs((int) startPos.getX() - ((int) e.getX() + xAdjustment)) == (Math.abs((int) startPos.getY() - ((int) e.getY() + yAdjustment)))) { //if "løper"
                 //Ned venstre
                 if ((int) startPos.getX() > e.getX() + xAdjustment && (int) startPos.getY() < e.getY() + yAdjustment) {
                     ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                     for (int i = 0; i < ruter; i++) {
                         if (chessBoard.findComponentAt((int) startPos.getX() - (i * 75), (int) startPos.getY() + (i * 75)) instanceof PieceLabel) {
                             moveBack();
                             return false;
                         }
                     }
                 }
                 //Opp venstre
                 if ((int) startPos.getX() > e.getX() + xAdjustment && (int) startPos.getY() > e.getY() + yAdjustment) {
                     ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                     for (int i = 0; i < ruter; i++) {
                         if (chessBoard.findComponentAt((int) startPos.getX() - (i * 75), (int) startPos.getY() - (i * 75)) instanceof PieceLabel) {
                             moveBack();
                             return false;
                         }
                     }
                 }
                 // Opp høyre
                 if ((int) startPos.getX() < e.getX() + xAdjustment && (int) startPos.getY() > e.getY() + yAdjustment) {
                     ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                     for (int i = 0; i < ruter; i++) {
                         if (chessBoard.findComponentAt((int) startPos.getX() + (i * 75), (int) startPos.getY() - (i * 75)) instanceof PieceLabel) {
                             moveBack();
                             return false;
                         }
                     }
                 }
                 // Ned høyre
                 if ((int) startPos.getX() < e.getX() + xAdjustment && (int) startPos.getY() < e.getY() + yAdjustment) {
                     ruter = Math.abs(((int) startPos.getX() - (e.getX() + xAdjustment)) / 75);
                     for (int i = 0; i < ruter; i++) {
                         if (chessBoard.findComponentAt((int) startPos.getX() + (i * 75), (int) startPos.getY() + (i * 75)) instanceof PieceLabel) {
                             moveBack();
                             return false;
                         }
                     }
                 }
 
                 move(e);
                 return true;
             }
 
             if (((int) e.getY() + yAdjustment) != (int) startPos.getY() && ((int) e.getX() + xAdjustment) == (int) startPos.getX() || (((int) e.getY() + yAdjustment) == (int) startPos.getY() && ((int) e.getX() + xAdjustment) != (int) startPos.getX())) { //if "tårn"
                 //Y-Retning
                 if (((int) startPos.getX() == e.getX() + xAdjustment) && (((int) e.getY() + yAdjustment) != (int) startPos.getY())) {
                     ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                     if ((int) e.getY() + yAdjustment > (int) startPos.getY()) {
                         for (int i = 0; i < ruter; i++) {
                             if (chessBoard.findComponentAt((int) startPos.getX(), (int) startPos.getY() + (i * 75)) instanceof PieceLabel) {
                                 moveBack();
                                 return false;
                             }
                         }
                     } else {
                         for (int i = 0; i < ruter; i++) {
                             if (chessBoard.findComponentAt((int) startPos.getX(), (int) startPos.getY() + (i * -75)) instanceof PieceLabel) {
                                 moveBack();
                                 return false;
                             }
                         }
                     }
                 }
                 //X-Retning
                 if (((int) startPos.getY() == e.getY() + yAdjustment) && (((int) e.getX() + xAdjustment) != (int) startPos.getX())) {
                     ruter = Math.abs(((int) startPos.getX() - (e.getX() + xAdjustment)) / 75);
                     if ((int) e.getX() + xAdjustment > (int) startPos.getX()) {
                         for (int i = 0; i < ruter; i++) {
                             if (chessBoard.findComponentAt((int) startPos.getX() + (i * 75), (int) startPos.getY()) instanceof PieceLabel) {
                                 moveBack();
                                 return false;
                             }
                         }
                     } else {
                         for (int i = 0; i < ruter; i++) {
                             if (chessBoard.findComponentAt((int) startPos.getX() + (i * -75), (int) startPos.getY()) instanceof PieceLabel) {
                                 moveBack();
                                 return false;
                             }
                         }
                     }
                 }
                 move(e);
                 return true;
             }
         } else {
             moveBack();
             return false;
         }
         return false;
     }
 
     public boolean moveQueenB(MouseEvent e, Component m) {
         int ruter;
         if (queenB.legalMove((int) e.getY() + yAdjustment, (int) e.getX() + xAdjustment, startPos, m, team)) {
             if (Math.abs((int) startPos.getX() - ((int) e.getX() + xAdjustment)) == (Math.abs((int) startPos.getY() - ((int) e.getY() + yAdjustment)))) { //if "løper"
                 //Ned venstre
                 if ((int) startPos.getX() > e.getX() + xAdjustment && (int) startPos.getY() < e.getY() + yAdjustment) {
                     ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                     for (int i = 0; i < ruter; i++) {
                         if (chessBoard.findComponentAt((int) startPos.getX() - (i * 75), (int) startPos.getY() + (i * 75)) instanceof PieceLabel) {
                             moveBack();
                             return false;
                         }
                     }
                 }
                 //Opp venstre
                 if ((int) startPos.getX() > e.getX() + xAdjustment && (int) startPos.getY() > e.getY() + yAdjustment) {
                     ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                     for (int i = 0; i < ruter; i++) {
                         if (chessBoard.findComponentAt((int) startPos.getX() - (i * 75), (int) startPos.getY() - (i * 75)) instanceof PieceLabel) {
                             moveBack();
                             return false;
                         }
                     }
                 }
                 // Opp høyre
                 if ((int) startPos.getX() < e.getX() + xAdjustment && (int) startPos.getY() > e.getY() + yAdjustment) {
                     ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                     for (int i = 0; i < ruter; i++) {
                         if (chessBoard.findComponentAt((int) startPos.getX() + (i * 75), (int) startPos.getY() - (i * 75)) instanceof PieceLabel) {
                             moveBack();
                             return false;
                         }
                     }
                 }
                 // Ned høyre
                 if ((int) startPos.getX() < e.getX() + xAdjustment && (int) startPos.getY() < e.getY() + yAdjustment) {
                     ruter = Math.abs(((int) startPos.getX() - (e.getX() + xAdjustment)) / 75);
                     for (int i = 0; i < ruter; i++) {
                         if (chessBoard.findComponentAt((int) startPos.getX() + (i * 75), (int) startPos.getY() + (i * 75)) instanceof PieceLabel) {
                             moveBack();
                             return false;
                         }
                     }
                 }
 
                 move(e);
                 return true;
             }
 
             if (((int) e.getY() + yAdjustment) != (int) startPos.getY() && ((int) e.getX() + xAdjustment) == (int) startPos.getX() || (((int) e.getY() + yAdjustment) == (int) startPos.getY() && ((int) e.getX() + xAdjustment) != (int) startPos.getX())) { //if "tårn"
                 //Y-Retning
                 if (((int) startPos.getX() == e.getX() + xAdjustment) && (((int) e.getY() + yAdjustment) != (int) startPos.getY())) {
                     ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                     if ((int) e.getY() + yAdjustment > (int) startPos.getY()) {
                         for (int i = 0; i < ruter; i++) {
                             if (chessBoard.findComponentAt((int) startPos.getX(), (int) startPos.getY() + (i * 75)) instanceof PieceLabel) {
                                 moveBack();
                                 return false;
                             }
                         }
                     } else {
                         for (int i = 0; i < ruter; i++) {
                             if (chessBoard.findComponentAt((int) startPos.getX(), (int) startPos.getY() + (i * -75)) instanceof PieceLabel) {
                                 moveBack();
                                 return false;
                             }
                         }
                     }
                 }
                 //X-Retning
                 if (((int) startPos.getY() == e.getY() + yAdjustment) && (((int) e.getX() + xAdjustment) != (int) startPos.getX())) {
                     ruter = Math.abs(((int) startPos.getX() - (e.getX() + xAdjustment)) / 75);
                     if ((int) e.getX() + xAdjustment > (int) startPos.getX()) {
                         for (int i = 0; i < ruter; i++) {
                             if (chessBoard.findComponentAt((int) startPos.getX() + (i * 75), (int) startPos.getY()) instanceof PieceLabel) {
                                 moveBack();
                                 return false;
                             }
                         }
                     } else {
                         for (int i = 0; i < ruter; i++) {
                             if (chessBoard.findComponentAt((int) startPos.getX() + (i * -75), (int) startPos.getY()) instanceof PieceLabel) {
                                 moveBack();
                                 return false;
                             }
                         }
                     }
                 }
                 move(e);
                 return true;
             }
         } else {
             moveBack();
             return false;
         }
         return false;
     }
 
     public boolean moveKnightB(MouseEvent e, Component m) {
         if (knightB.legalMove(e.getY() + yAdjustment, e.getX() + xAdjustment, startPos, m, team)) {
             move(e);
             return true;
         } else {
             moveBack();
             return false;
         }
     }
 
     public boolean moveKnightW(MouseEvent e, Component m) {
         if (knightW.legalMove(e.getY() + yAdjustment, e.getX() + xAdjustment, startPos, m, team)) {
             move(e);
             return true;
         } else {
             moveBack();
             return false;
         }
 
     }
 
     public boolean moveKingW(MouseEvent e, Component m) {
         if ((e.getY() + yAdjustment == startPos.getY()) && (startPos.getX() - (e.getX() + xAdjustment)) == -150 && !(chessBoard.findComponentAt((e.getX() + xAdjustment),
                 (e.getY() + yAdjustment)) instanceof PieceLabel && !(chessBoard.findComponentAt(((int) startPos.getX() + xAdjustment + 75), (int) (startPos.getY() + yAdjustment)) instanceof PieceLabel))) {
             if (kingW.move() == false && rookWright.move() == false) {
                 move(e);
                 Component c = chessBoard.findComponentAt((int) startPos.getX() + 225, (int) startPos.getY());
                 Container parent = (Container) chessBoard.getComponent(61);
                 parent.add(c);
                 kingW.setMove();
                 chessPiece.setVisible(true);
                 return true;
             }
         }
         if ((e.getY() + yAdjustment == startPos.getY()) && (startPos.getX() - (e.getX() + xAdjustment)) == 150 && !(chessBoard.findComponentAt((e.getX() + xAdjustment),
                 (e.getY() + yAdjustment)) instanceof PieceLabel && !(chessBoard.findComponentAt(((int) startPos.getX() + xAdjustment - 75), (int) (startPos.getY() + yAdjustment)) instanceof PieceLabel)
                 && !(chessBoard.findComponentAt(((int) startPos.getX() + xAdjustment - 225), (int) (startPos.getY() + yAdjustment)) instanceof PieceLabel))) {
             if (kingW.move() == false && rookWleft.move() == false) {
                 move(e);
                 Component c = chessBoard.findComponentAt((int) startPos.getX() - 300, (int) startPos.getY());
                 Container parent = (Container) chessBoard.getComponent(59);
                 parent.add(c);
                 kingW.setMove();
                 chessPiece.setVisible(true);
                 return true;
             }
         }
         if (kingW.legalMove(e.getY() + yAdjustment, e.getX() + xAdjustment, startPos, m, team)) {
             move(e);
             kingW.setMove();
             return true;
         } else {
             moveBack();
             return false;
         }
     }
 
     public boolean moveKingB(MouseEvent e, Component m) {
         if ((e.getY() + yAdjustment == startPos.getY()) && (startPos.getX() - (e.getX() + xAdjustment)) == 150 && !(chessBoard.findComponentAt((e.getX() + xAdjustment),
                 (e.getY() + yAdjustment)) instanceof PieceLabel && !(chessBoard.findComponentAt(((int) startPos.getX() + xAdjustment + 75), (int) (startPos.getY() + yAdjustment)) instanceof PieceLabel))) {
             if (kingB.move() == false && rookBleft.move() == false) {
                 move(e);
                 Component c = chessBoard.findComponentAt((int) startPos.getX() - 300, (int) startPos.getY());
                 Container parent = (Container) chessBoard.getComponent(3);
                 parent.add(c);
                 kingB.setMove();
                 chessPiece.setVisible(true);
                 return true;
             }
         }
         if ((e.getY() + yAdjustment == startPos.getY()) && (startPos.getX() - (e.getX() + xAdjustment)) == -150 && !(chessBoard.findComponentAt((e.getX() + xAdjustment),
                 (e.getY() + yAdjustment)) instanceof PieceLabel && !(chessBoard.findComponentAt(((int) startPos.getX() + xAdjustment - 75), (int) (startPos.getY() + yAdjustment)) instanceof PieceLabel)
                 && !(chessBoard.findComponentAt(((int) startPos.getX() + xAdjustment - 225), (int) (startPos.getY() + yAdjustment)) instanceof PieceLabel))) {
             if (kingB.move() == false && rookBright.move() == false) {
                 move(e);
                 Component c = chessBoard.findComponentAt((int) startPos.getX() + 225, (int) startPos.getY());
                 Container parent = (Container) chessBoard.getComponent(5);
                 parent.add(c);
                 kingB.setMove();
                 chessPiece.setVisible(true);
                 return true;
             }
         }
         if (kingB.legalMove(e.getY() + yAdjustment, e.getX() + xAdjustment, startPos, m, team)) {
             move(e);
             kingB.setMove();
             return true;
         } else {
             moveBack();
             return false;
         }
     }
 
     public boolean moveBishopW(MouseEvent e, Component m) {
         //Ned venstre
         int ruter;
         if (bishopW.legalMove((int) e.getY() + yAdjustment, (int) e.getX() + xAdjustment, startPos, m, team)) {
             if ((int) startPos.getX() > e.getX() + xAdjustment && (int) startPos.getY() < e.getY() + yAdjustment) {
                 ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                 for (int i = 0; i < ruter; i++) {
                     if (chessBoard.findComponentAt((int) startPos.getX() - (i * 75), (int) startPos.getY() + (i * 75)) instanceof PieceLabel) {
                         moveBack();
                         return false;
                     }
                 }
             }
             //Opp venstre
             if ((int) startPos.getX() > e.getX() + xAdjustment && (int) startPos.getY() > e.getY() + yAdjustment) {
                 ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                 for (int i = 0; i < ruter; i++) {
                     if (chessBoard.findComponentAt((int) startPos.getX() - (i * 75), (int) startPos.getY() - (i * 75)) instanceof PieceLabel) {
                         moveBack();
                         return false;
                     }
                 }
             }
             // Opp høyre
             if ((int) startPos.getX() < e.getX() + xAdjustment && (int) startPos.getY() > e.getY() + yAdjustment) {
                 ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                 for (int i = 0; i < ruter; i++) {
                     if (chessBoard.findComponentAt((int) startPos.getX() + (i * 75), (int) startPos.getY() - (i * 75)) instanceof PieceLabel) {
                         moveBack();
                         return false;
                     }
                 }
             }
             // Ned høyre
             if ((int) startPos.getX() < e.getX() + xAdjustment && (int) startPos.getY() < e.getY() + yAdjustment) {
                 ruter = Math.abs(((int) startPos.getX() - (e.getX() + xAdjustment)) / 75);
                 for (int i = 0; i < ruter; i++) {
                     if (chessBoard.findComponentAt((int) startPos.getX() + (i * 75), (int) startPos.getY() + (i * 75)) instanceof PieceLabel) {
                         moveBack();
                         return false;
                     }
                 }
             }
             move(e);
             return true;
         } else {
             moveBack();
             return false;
         }
     }
 
     public boolean moveBishopB(MouseEvent e, Component m) {
         //Ned venstre
         int ruter;
         if (bishopB.legalMove((int) e.getY() + yAdjustment, (int) e.getX() + xAdjustment, startPos, m, team)) {
             if ((int) startPos.getX() > e.getX() + xAdjustment && (int) startPos.getY() < e.getY() + yAdjustment) {
                 ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                 for (int i = 0; i < ruter; i++) {
                     if (chessBoard.findComponentAt((int) startPos.getX() - (i * 75), (int) startPos.getY() + (i * 75)) instanceof PieceLabel) {
                         moveBack();
                         return false;
                     }
                 }
             }
             //Opp venstre
             if ((int) startPos.getX() > e.getX() + xAdjustment && (int) startPos.getY() > e.getY() + yAdjustment) {
                 ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                 for (int i = 0; i < ruter; i++) {
                     if (chessBoard.findComponentAt((int) startPos.getX() - (i * 75), (int) startPos.getY() - (i * 75)) instanceof PieceLabel) {
                         moveBack();
                         return false;
                     }
                 }
             }
             // Opp høyre
             if ((int) startPos.getX() < e.getX() + xAdjustment && (int) startPos.getY() > e.getY() + yAdjustment) {
                 ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                 for (int i = 0; i < ruter; i++) {
                     if (chessBoard.findComponentAt((int) startPos.getX() + (i * 75), (int) startPos.getY() - (i * 75)) instanceof PieceLabel) {
                         moveBack();
                         return false;
                     }
                 }
             }
             // Ned høyre
             if ((int) startPos.getX() < e.getX() + xAdjustment && (int) startPos.getY() < e.getY() + yAdjustment) {
                 ruter = Math.abs(((int) startPos.getX() - (e.getX() + xAdjustment)) / 75);
                 for (int i = 0; i < ruter; i++) {
                     if (chessBoard.findComponentAt((int) startPos.getX() + (i * 75), (int) startPos.getY() + (i * 75)) instanceof PieceLabel) {
                         moveBack();
                         return false;
                     }
                 }
             }
             move(e);
             return true;
         } else {
             moveBack();
             return false;
         }
     }
 
     public boolean moveRookW(MouseEvent e, Component m) {
         int ruter;
         if (rookW.legalMove((int) e.getY() + yAdjustment, (int) e.getX() + xAdjustment, startPos, m, team)) {
             //Y-Retning
             if ((int) startPos.getX() == e.getX() + xAdjustment) {
                 ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                 if ((int) e.getY() + yAdjustment > (int) startPos.getY()) {
                     for (int i = 0; i < ruter; i++) {
                         if (chessBoard.findComponentAt((int) startPos.getX(), (int) startPos.getY() + (i * 75)) instanceof PieceLabel) {
                             moveBack();
                             return false;
                         }
                     }
                 } else {
                     for (int i = 0; i < ruter; i++) {
                         if (chessBoard.findComponentAt((int) startPos.getX(), (int) startPos.getY() + (i * -75)) instanceof PieceLabel) {
                             moveBack();
                             return false;
                         }
                     }
                 }
             }
             //X-Retning
             if ((int) startPos.getY() == e.getY() + yAdjustment) {
                 ruter = Math.abs(((int) startPos.getX() - (e.getX() + xAdjustment)) / 75);
                 if ((int) e.getX() + xAdjustment > (int) startPos.getX()) {
                     for (int i = 0; i < ruter; i++) {
                         if (chessBoard.findComponentAt((int) startPos.getX() + (i * 75), (int) startPos.getY()) instanceof PieceLabel) {
                             moveBack();
                             return false;
                         }
                     }
                 } else {
                     for (int i = 0; i < ruter; i++) {
                         if (chessBoard.findComponentAt((int) startPos.getX() + (i * -75), (int) startPos.getY()) instanceof PieceLabel) {
                             moveBack();
                             return false;
                         }
                     }
                 }
             }
             move(e);
             return true;
         } else {
             moveBack();
             return false;
         }
     }
 
     public boolean moveRookB(MouseEvent e, Component m) {
         int ruter;
         if (rookB.legalMove((int) e.getY() + yAdjustment, (int) e.getX() + xAdjustment, startPos, m, team)) {
             //Y-Retning
             if ((int) startPos.getX() == e.getX() + xAdjustment) {
                 ruter = Math.abs(((int) startPos.getY() - (e.getY() + yAdjustment)) / 75);
                 if ((int) e.getY() + yAdjustment > (int) startPos.getY()) {
                     for (int i = 0; i < ruter; i++) {
                         if (chessBoard.findComponentAt((int) startPos.getX(), (int) startPos.getY() + (i * 75)) instanceof PieceLabel) {
                             moveBack();
                             return false;
                         }
                     }
                 } else {
                     for (int i = 0; i < ruter; i++) {
                         if (chessBoard.findComponentAt((int) startPos.getX(), (int) startPos.getY() + (i * -75)) instanceof PieceLabel) {
                             moveBack();
                             return false;
                         }
                     }
                 }
             }
             //X-Retning
             if ((int) startPos.getY() == e.getY() + yAdjustment) {
                 ruter = Math.abs(((int) startPos.getX() - (e.getX() + xAdjustment)) / 75);
                 if ((int) e.getX() + xAdjustment > (int) startPos.getX()) {
                     for (int i = 0; i < ruter; i++) {
                         if (chessBoard.findComponentAt((int) startPos.getX() + (i * 75), (int) startPos.getY()) instanceof PieceLabel) {
                             moveBack();
                             return false;
                         }
                     }
                 } else {
                     for (int i = 0; i < ruter; i++) {
                         if (chessBoard.findComponentAt((int) startPos.getX() + (i * -75), (int) startPos.getY()) instanceof PieceLabel) {
                             moveBack();
                             return false;
                         }
                     }
                 }
             }
             move(e);
             return true;
         } else {
             moveBack();
             return false;
         }
     }
 
     public void replacePawn() {
         chessPiece.setVisible(false);
         if (chessPiece.getPiece().getTeam() == 1) {
             Container c = (JPanel) chessBoard.findComponentAt((int) enPassantPW.getX(), (int) enPassantPW.getY() + 75);
             c.add(new PieceLabel(pawnW.getIcon(), pawnW));
         }
         if (chessPiece.getPiece().getTeam() == 2) {
             Container c = (JPanel) chessBoard.findComponentAt((int) enPassantPB.getX(), (int) enPassantPB.getY() - 75);
             c.add(new PieceLabel(pawnB.getIcon(), pawnB));
         }
         chessPiece.setVisible(true);
     }
 
     public boolean enPassant() {
         if (chessPiece.getPiece().getTeam() == 1 && pawnB.getPassant() == true) {
             if (chessPiece.getPiece() == pawnW) {
                 if (chessBoard.findComponentAt((int) enPassantPB.getX() + 75, (int) enPassantPB.getY() + 75) instanceof PieceLabel) {
                     Piece a = (Piece) ((PieceLabel) chessBoard.findComponentAt((int) enPassantPB.getX() + 75, (int) enPassantPB.getY() + 75)).getPiece();
                     if (a.equals(pawnW)) {
                         if (((int) enPassantPB.getX() + 75) == startPos.getX() && ((int) enPassantPB.getY() + 75) == startPos.getY()) {
                             JPanel panel = new JPanel(new BorderLayout());
                             panel.setOpaque(false);
                             Component c = chessBoard.findComponentAt((int) enPassantPB.getX(), (int) enPassantPB.getY() + 75);
                             Container parent = c.getParent();
                             parent.remove(0);
                             Component c2 = chessBoard.findComponentAt(startPos);
                             Container parent2 = c2.getParent();
                             parent2.remove(0);
                             Component d = chessBoard.findComponentAt((int) enPassantPB.getX(), (int) enPassantPB.getY());
                             Container parent3 = (Container) d;
                             parent3.add(chessPiece);
                             return true;
                         }
                     }
                 }
                 if (chessBoard.findComponentAt((int) enPassantPB.getX() - 75, (int) enPassantPB.getY() + 75) instanceof PieceLabel) {
                     Piece a = (Piece) ((PieceLabel) chessBoard.findComponentAt((int) enPassantPB.getX() - 75, (int) enPassantPB.getY() + 75)).getPiece();
                     if (a.equals(pawnW)) {
                         if (((int) enPassantPB.getX() - 75) == startPos.getX() && ((int) enPassantPB.getY() + 75) == startPos.getY()) {
                             JPanel panel = new JPanel(new BorderLayout());
                             panel.setOpaque(false);
                             Component c = chessBoard.findComponentAt((int) enPassantPB.getX(), (int) enPassantPB.getY() + 75);
                             Container parent = c.getParent();
                             parent.remove(0);
                             Component c2 = chessBoard.findComponentAt(startPos);
                             Container parent2 = c2.getParent();
                             parent2.remove(0);
                             Component d = chessBoard.findComponentAt((int) enPassantPB.getX(), (int) enPassantPB.getY());
                             Container parent3 = (Container) d;
                             parent3.add(chessPiece);
                             return true;
                         }
                     }
                 }
             }
         }
         if (chessPiece.getPiece().getTeam() == 2 && pawnW.getPassant() == true) {
             if (chessPiece.getPiece() == pawnB) {
                 //Ned høyre
                 if (chessBoard.findComponentAt((int) enPassantPB.getX() - 75, (int) enPassantPB.getY() - 75) instanceof PieceLabel) {
                     Piece a = (Piece) ((PieceLabel) chessBoard.findComponentAt((int) enPassantPB.getX() - 75, (int) enPassantPB.getY() - 75)).getPiece();
                     if (a.equals(pawnB)) {
                         if (((int) enPassantPW.getX() - 75) == startPos.getX() && ((int) enPassantPW.getY() - 75) == startPos.getY()) {
                             JPanel panel = new JPanel(new BorderLayout());
                             panel.setOpaque(false);
                             Component c = chessBoard.findComponentAt((int) enPassantPW.getX(), (int) enPassantPW.getY() - 75);
                             Container parent = c.getParent();
                             parent.remove(0);
                             Component c2 = chessBoard.findComponentAt(startPos);
                             Container parent2 = c2.getParent();
                             parent2.remove(0);
                             Component d = chessBoard.findComponentAt((int) enPassantPW.getX(), (int) enPassantPW.getY());
                             Container parent3 = (Container) d;
                             parent3.add(chessPiece);
                             return true;
                         }
                     }
                 }
                        //Ned venstre
                 if (chessBoard.findComponentAt((int) enPassantPB.getX() + 75, (int) enPassantPB.getY() - 75) instanceof PieceLabel) {
                     Piece a = (Piece) ((PieceLabel) chessBoard.findComponentAt((int) enPassantPB.getX() + 75, (int) enPassantPB.getY() - 75)).getPiece();
                     if (a.equals(pawnB)) {
                         if (((int) enPassantPW.getX() + 75) == startPos.getX() && ((int) enPassantPW.getY() - 75) == startPos.getY()) {
                             JPanel panel = new JPanel(new BorderLayout());
                             panel.setOpaque(false);
                             Component c = chessBoard.findComponentAt((int) enPassantPW.getX(), (int) enPassantPW.getY() - 75);
                             Container parent = c.getParent();
                             parent.remove(0);
                             Component c2 = chessBoard.findComponentAt(startPos);
                             Container parent2 = c2.getParent();
                             parent2.remove(0);
                             Component d = chessBoard.findComponentAt((int) enPassantPW.getX(), (int) enPassantPW.getY());
                             Container parent3 = (Container) d;
                             parent3.add(chessPiece);
                             return true;
                         }
                     }
                 }
             }
         }
         return false;
     }
    public ChessTable getTable(){
         return chessTable;
     }
    public void loadGame(ChessTable table){
         chessTable = table;
     }
 }
