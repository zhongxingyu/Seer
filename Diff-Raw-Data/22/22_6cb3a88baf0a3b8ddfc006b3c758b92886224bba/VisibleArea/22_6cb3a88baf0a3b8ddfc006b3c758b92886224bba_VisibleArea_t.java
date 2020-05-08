 package mainJump;
 
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.ArrayList;
 
 public class VisibleArea extends JPanel implements ActionListener {
 
 	Timer timer;
 	private ArrayList<gameObject> objects;
 	gameObject player;
 
     private final PlatformBuilder platformBuilder = new PlatformBuilder(this);
 
 
     public VisibleArea()
 	{
        ShowMenu();
        StartGame();
    }

    private void ShowMenu()
    {

    }

    private void StartGame() {
        dataModel.INSTANCE.score = 1;
         dataModel.INSTANCE.time = 1;
        addKeyListener(new TAdapter());
         this.addComponentListener(new Listener());
         setFocusable(true);

        setDoubleBuffered(true);
        player = new Player();
         ScoreObject scoreObject = new ScoreObject();
 
         objects = new ArrayList<gameObject>();
         objects.add(player);
         objects.add(scoreObject);
 
         timer = new Timer(35, this);
         timer.setInitialDelay(300);
         timer.start();
     }
 
     public ArrayList<gameObject> getObjects() {
         return objects;
     }
 
     private class TAdapter extends KeyAdapter {
 
         public void keyReleased(KeyEvent e) {
             player.keyReleased(e);
         }
 
         public void keyPressed(KeyEvent e) {
         	player.keyPressed(e);
         }
     }
 	
 	public void paint(Graphics g)
     {
       super.paint(g);
 
       Graphics2D g2 = (Graphics2D) g;
 
       RenderingHints rh =
             new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                              RenderingHints.VALUE_ANTIALIAS_ON);
 
       rh.put(RenderingHints.KEY_RENDERING,
              RenderingHints.VALUE_RENDER_QUALITY);
 
       g2.setRenderingHints(rh);
 
       Dimension size = getSize();
       double w = size.getWidth();
       double h = size.getHeight();
 
       DrawAllShapes(g2);
       
       KillWrongObjects(w,h);
       
       Toolkit.getDefaultToolkit().sync();
       g.dispose();
     }
 	
 	
     private void MoveAllShapes() {
     	for (gameObject o : objects) {
 			o.OnTimer();
     	}
     }
 
 
 	private void BornNewObjects() {
         if (dataModel.INSTANCE.time == 2)
             platformBuilder.MakeInitialPlatforms();
         platformBuilder.TryMakePlatform();
     }
 
     private void KillWrongObjects(double w, double h) {
 		ArrayList<gameObject> toDeleteObjects = new ArrayList<gameObject>();
 		for (gameObject o : objects) {
             if (!((o instanceof ScoreObject) || (o instanceof Player)))
 			    if (!o.IsInWindow(w,h)  )
 				toDeleteObjects.add(o);
 		}
 		objects.removeAll(toDeleteObjects);
 	}
 
 
 	private void DrawAllShapes(Graphics2D g2) {
     	for (gameObject o : objects) {
 			o.Draw(g2);
 		}
 		
 	}
 	
 	private void SetPlayerOnPlatform()
 	{
 		for (gameObject o : objects)
 			if (o instanceof Platform)
 			{
 				if (IsOnSameLevelWithPlayer(o) && IfInsideBoundariesOfThePlatform(o))
 				{
 					((Player)player).onPlatform = true;
                     dataModel.INSTANCE.score++;
 					return;
 				}
 			}
 		((Player)player).onPlatform = false;
 	}
 
     private boolean IfInsideBoundariesOfThePlatform(gameObject o) {
         int margin = player.dim.width / 2 - 1;
         return ((o.x<player.x+margin)&&(o.x + o.dim.width + margin >player.x + player.dim.width));
     }
 
     private boolean IsOnSameLevelWithPlayer(gameObject o) {
         return (o.y == player.y + player.dim.height);
     }
 
     @Override
 	public void actionPerformed(ActionEvent e) {
         dataModel.INSTANCE.time++;
 
 		BornNewObjects();
 	    MoveAllShapes();
 	    SetPlayerOnPlatform();
         ValidateGameOver();
 		repaint();
 	}
 
     private void ValidateGameOver() {
         if (player.y > dataModel.INSTANCE.getDimension().getHeight()-35)
             timer.stop();
     }
 
 
     //onResize
     public class Listener implements ComponentListener
     {
 
         @Override
         public void componentResized(ComponentEvent componentEvent) {
             dataModel.INSTANCE.setDimensions(componentEvent.getComponent().getSize());
             repaint();
         }
 
         @Override
         public void componentMoved(ComponentEvent componentEvent) {
             //To change body of implemented methods use File | Settings | File Templates.
         }
 
         @Override
         public void componentShown(ComponentEvent componentEvent) {
             //To change body of implemented methods use File | Settings | File Templates.
         }
 
         @Override
         public void componentHidden(ComponentEvent componentEvent) {
             //To change body of implemented methods use File | Settings | File Templates.
         }
     }
 
 }
