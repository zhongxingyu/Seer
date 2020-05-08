 package computational_geometry.views;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JTextField;
 
 import computational_geometry.controller.Controller;
 import computational_geometry.model.Listener;
 import computational_geometry.model.Model;
 import computational_geometry.model.beans.Graph;
 import computational_geometry.model.beans.GraphAlgorithm;
 import computational_geometry.model.beans.Point;
 import computational_geometry.model.beans.Segment;
 import computational_geometry.model.data_structures.CircularList;
 import computational_geometry.model.traces.AlgoTrace;
 import computational_geometry.model.traces.Drawer;
 
 public class DrawGraphView extends AbstractPanelView implements Listener {
 
     private CanvasSaisirPointsAfficherSegments canvas;
     JRadioButton drawMark;
     JRadioButton drawType;
     JRadioButton drawCoordinates;
     JRadioButton drawPointPos;
     private JRadioButton stepByStep;
     private JButton nextStep;
     private JLabel lblX;
     private JLabel lblY;
     private JRadioButton colorize;
 
     public DrawGraphView(Model model, Controller controller) {
         super(model, controller);
     }
 
     @Override
     protected void createPanelView() {
 
         // Panel des boutons
         JPanel panelBoutons = new JPanel(new BorderLayout());
 
         JPanel panelActions = new JPanel();
 
         // Creation du champs texte "Nombre de points a generer"
         final JTextField textNombrePoint = new JTextField("50");
         textNombrePoint.setColumns(5);
 
         // Creation du bouton Effacer
         JButton effacer = new JButton("Erase");
 
         // Action du bouton Effacer
         effacer.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 controller.notifyErasePolygonRequest();
             }
         });
 
         // Creation du bouton Rand
         JButton rand = new JButton("Rand");
         rand.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent evt) {
                 // Suppression des points et des segments
                 int n = Integer.parseInt(textNombrePoint.getText());
                 controller.notifyGenerateRandPolygonRequest(n, 2,
                         canvas.getWidth(), 2, canvas.getHeight());
             }
         });
 
         // Creation du bouton "etape par etape"
         stepByStep = new JRadioButton("steps");
         stepByStep.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 nextStep.setVisible(stepByStep.isSelected());
                 controller.stepByStepRequested(stepByStep.isSelected());
             }
         });
         stepByStep.setSelected(model.doSteps());
 
         // Creation du bouton NextStep
         nextStep = new JButton("Next Step");
         nextStep.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 controller.nextStepRequested();
             }
         });
         nextStep.setVisible(false);
 
         JPanel panelCoord = new JPanel(new GridLayout(2, 2));
         lblX = new JLabel();
         lblY = new JLabel();
         panelCoord.add(new JLabel("x = "));
         panelCoord.add(lblX);
         panelCoord.add(new JLabel("y = "));
         panelCoord.add(lblY);
 
         colorize = new JRadioButton("fancy display");
         colorize.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 canvas.repaint();
             }
         });
 
         panelActions.add(colorize);
         panelActions.add(panelCoord);
         panelActions.add(effacer);
         panelActions.add(rand);
         panelActions.add(textNombrePoint);
         panelActions.add(stepByStep);
         panelActions.add(nextStep);
 
         JPanel panelDebug = new JPanel();
 
         drawMark = new JRadioButton("draw mark");
         drawMark.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 canvas.repaint();
             }
         });
 
         drawType = new JRadioButton("draw type");
         drawType.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 canvas.repaint();
             }
         });
 
         drawCoordinates = new JRadioButton("draw coordinates");
         drawCoordinates.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 canvas.repaint();
             }
         });
 
         drawPointPos = new JRadioButton("draw points pos");
         drawPointPos.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 canvas.repaint();
             }
         });
 
         JRadioButton drawNone = new JRadioButton("draw none");
         drawNone.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 canvas.repaint();
             }
         });
 
         ButtonGroup drawGroup = new ButtonGroup();
         drawGroup.add(drawNone);
         drawGroup.add(drawMark);
         drawGroup.add(drawType);
         drawGroup.add(drawCoordinates);
         drawGroup.add(drawPointPos);
 
         panelDebug.add(drawNone);
         panelDebug.add(drawMark);
         panelDebug.add(drawType);
         panelDebug.add(drawCoordinates);
         panelDebug.add(drawPointPos);
 
         panelBoutons.add(panelActions, BorderLayout.NORTH);
         panelBoutons.add(panelDebug, BorderLayout.SOUTH);
 
         canvas = new CanvasSaisirPointsAfficherSegments(this);
 
         panelView = new JPanel(new BorderLayout());
 
         panelView.add(canvas, BorderLayout.CENTER);
         panelView.add(panelBoutons, BorderLayout.SOUTH);
 
         panelView.setPreferredSize(new Dimension(800, 600));
     }
 
     @Override
     public void polygonModified() {
         GraphAlgorithm algo = model.getFirstAlgo();
         AlgoTrace trace = (algo != null) ? algo.getTrace() : null;
         nextStep.setVisible((trace != null) ? !trace.isDone()
                 && stepByStep.isSelected() : false);
         canvas.repaint();
     }
 
     public void setLblX(int x) {
         lblX.setText(x+"");
     }
 
     public void setLblY(int y) {
         lblY.setText(y+"");
     }
 
     public boolean colorize() {
         return this.colorize.isSelected();
     }
 
 }
 
 class CanvasSaisirPointsAfficherSegments extends JPanel implements
         MouseListener, MouseMotionListener {
     private static final long serialVersionUID = 1L;
 
     private DrawGraphView attachedPanel;
 
     private final Color pointColor = Color.GRAY;
     private final Color segmentColor = Color.BLUE;
     private final Color selectedPointColor = Color.RED;
 
     public CanvasSaisirPointsAfficherSegments(DrawGraphView attachedPanel) {
         this.attachedPanel = attachedPanel;
         setBackground(Color.WHITE);
 
         this.addMouseListener(this);
         this.addMouseMotionListener(this);
     }
 
     @Override
     public void paint(Graphics g) {
         g.clearRect(0, 0, getWidth(), getHeight()); // Erase background
         Graph graph = attachedPanel.model.getGraph();
 
         drawSegments(g, graph.getSegments());
         drawPoints(g, graph.getPoints());
 
        if (attachedPanel.model.doSteps()) {
            GraphAlgorithm algo = attachedPanel.model.getFirstAlgo();
            AlgoTrace trace = (algo != null) ? algo.getTrace() : null;
            if (trace != null) {
                if (!trace.isDone()) {
                    trace.drawCurrentState(g, attachedPanel.colorize());
                } else {
                    trace.drawFullResult(g, attachedPanel.colorize());
                }
            }
        } else {
            for (GraphAlgorithm algo : attachedPanel.model.getGraph().getAlgorithms()) {
                AlgoTrace trace = algo.getTrace();
                if (trace != null) {
                    trace.drawFullResult(g, attachedPanel.colorize());
                }
             }
         }
     }
 
     private void drawPoints(Graphics g, CircularList<Point> points) {
         for (int n = 0; n < points.size(); ++n) {
             Point p = points.elementAt(n);
             if (attachedPanel.drawPointPos.isSelected()) {
                 g.drawString(n + "", (int)p.x, (int)p.y - 20);
             }
 
             if (n == attachedPanel.model.getNumSelectedPoint())
                 g.setColor(selectedPointColor);
             else
                 g.setColor(pointColor);
 
             drawPoint(g, p);
         }
     }
 
     private void drawPoint(Graphics g, Point p) {
         Color c = g.getColor();
         g.setColor(pointColor);
         Drawer drawer = SwingDrawer.getInstance(g);
         if (attachedPanel.drawMark.isSelected()) {
             drawer.drawMark(p);
         }
         if (attachedPanel.drawType.isSelected()) {
             drawer.drawType(p);
         }
         if (attachedPanel.drawCoordinates.isSelected()) {
             drawer.drawCoordinates(p);
         }
         drawer.drawPoint(p);
         g.setColor(c);
     }
 
     private void drawSegments(Graphics g, CircularList<Segment> segments) {
         Color c = g.getColor();
         g.setColor(segmentColor);
         Drawer drawer = SwingDrawer.getInstance(g);
         for (Segment s : segments) {
             drawer.drawSegment(s);
         }
         g.setColor(c);
     }
 
     /**
      * Un point est ajoute si on presse le bouton de gauche et si aucun point
      * n'est selectionne. Un point est suuprime si on presse un autre bouton et
      * si un point est selectionne.
      */
     @Override
     public void mousePressed(MouseEvent evt) {
         if (evt.getButton() == 1) {
             attachedPanel.controller.notifyPointAddRequest(evt.getX(),
                     evt.getY());
         } else {
             attachedPanel.controller.notifyPointRemoveRequest(evt.getX(),
                     evt.getY());
         }
     }
 
     /**
      * Le x et y du point numSelectedPoint est modifie si la souris change de
      * position avec un bouton enfonce.
      */
     @Override
     public void mouseDragged(MouseEvent evt) {
         attachedPanel.controller.notifyPointMoveRequest(evt.getX(), evt.getY());
     }
 
     /**
      * Le numSelectedPoint est calcule si la souris change de position sans
      * bouton enfonce.
      */
     @Override
     public void mouseMoved(MouseEvent evt) {
         attachedPanel.setLblX(evt.getX());
         attachedPanel.setLblY(evt.getY());
         attachedPanel.controller.notifyHoverChanged(evt.getX(), evt.getY());
     }
 
     @Override
     public void mouseReleased(MouseEvent evt) {}
 
     @Override
     public void mouseEntered(MouseEvent evt) {}
 
     @Override
     public void mouseExited(MouseEvent evt) {}
 
     @Override
     public void mouseClicked(MouseEvent evt) {}
 }
