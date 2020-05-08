 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package br.usp.icmc.projectcg2012.engine;
 
 import br.usp.icmc.projectcg2012.models.*;
 import br.usp.icmc.projectcg2012.view.Renderer;
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.MemoryImageSource;
 import java.util.ArrayList;
 import java.util.Iterator;
 import javax.media.opengl.GLCanvas;
 
 /**
  *
  * @author gustavo
  */
 public class InputDevice implements MouseListener, MouseMotionListener, KeyListener
 {
 
     private GLCanvas canvas;
     private Renderer renderer;
     private int prevMouseX;
     private int prevMouseY;
     private boolean mousePosKnown = false;
     private boolean isRelative = true;
     private float alpha;  // angulo de visao atual da camera, direita esquerda
     private float beta;  // angulo de visao atual da camera, cima baixo
     private float posx;  // posicao x atual da camera
     private float posy;  // posicao y atual da camera, provavelmente nunca sera mudado
     private float posz;  // posicao z atual da camera
     private float ang_senoide;
     private ArrayList skyboxModels;
     private ArrayList animatedModels;
 
     public InputDevice(GLCanvas canvas, Renderer renderer)
     {
         this.canvas = canvas;
         this.renderer = renderer;
         this.alpha = this.renderer.getAlpha();
         this.beta = this.renderer.getBeta();
         this.posx = this.renderer.getPosx();
         this.posy = this.renderer.getPosy();
         this.posz = this.renderer.getPosz();
         this.ang_senoide = this.renderer.getAngSenoide();
         this.skyboxModels = this.renderer.getSkyboxModels();
         this.animatedModels = this.renderer.getAnimatedModels();
     }
 
     public void setCanvas(GLCanvas canvas)
     {
         this.canvas = canvas;
     }
 
     public void setRenderer(Renderer renderer)
     {
         this.renderer = renderer;
     }
 
     public void setRelative(boolean bool)
     {
         this.isRelative = bool;
     }
 
     public boolean isRelative()
     {
         return this.isRelative;
     }
 
     public void mouseReleased(MouseEvent e)
     {
     }
 
     public void mouseEntered(MouseEvent e)
     {
     }
 
     public void mouseExited(MouseEvent e)
     {
     }
 
     public void mouseDragged(MouseEvent me)
     {
     }
 
     public void mouseClicked(MouseEvent me)
     {
     }
 
     public void mousePressed(MouseEvent me)
     {
     }
 
     public void mouseMoved(MouseEvent me)
     {
         if (isRelative())
         {
             processMouseMove(me, this.canvas);
             centerMouse(this.canvas);
         }
     }
 
     public void keyTyped(KeyEvent ke)
     {
     }
 
     public void keyPressed(KeyEvent ke)
     {
         Iterator it;
         SkyboxModel skyboxModel;
         AnimatedModel animatedModel;
         switch (ke.getKeyCode())
         {
             case KeyEvent.VK_W:
                 it = skyboxModels.iterator();
                 while (it.hasNext())
                 {
                     skyboxModel = (SkyboxModel) it.next();
                     if (skyboxModel.collision(posx, posy, posz,
                             ((posx) * ((float) Math.sin(alpha))), 1.8f + posy, (posz) * ((float) Math.cos(alpha)), 0.06f) == true)
                     {
                         return;
                     }
                 }
                 posz -= 0.06 * (Math.cos(alpha));
                 posx += 0.06 * (Math.sin(alpha));
                 ang_senoide += 0.25f;
                 renderer.setPosx(posx);
                 renderer.setPosz(posz);
                 renderer.setAngSenoide(ang_senoide);
                 break;
             case KeyEvent.VK_S:
                 it = skyboxModels.iterator();
                 while (it.hasNext())
                 {
                     skyboxModel = (SkyboxModel) it.next();
                     if (skyboxModel.collision(posx, posy, posz,
                             ((posx) * ((float) Math.sin(alpha))), 1.8f + posy, (posz) * ((float) Math.cos(alpha)), -0.06f) == true)
                     {
                         return;
                     }
                 }
 
                 posz += 0.06 * (Math.cos(alpha));
                 posx -= 0.06 * (Math.sin(alpha));
                 ang_senoide -= 0.25f;
                 renderer.setPosx(posx);
                 renderer.setPosz(posz);
                 renderer.setAngSenoide(ang_senoide);
                 break;
             case KeyEvent.VK_A:
 //                posz = (float) (posz - 0.06f * Math.cos(alpha));
 //                posx = (float) (posx + 0.06f * Math.sin(alpha));    
                 renderer.setPosx(posx);
                 renderer.setPosz(posz);
                 break;
             case KeyEvent.VK_D:
 //                posz = (float) (posz + 0.06f * Math.cos(alpha));
 //                posx = (float) (posx - 0.06f * Math.sin(alpha));
                 renderer.setPosx(posx);
                 renderer.setPosz(posz);
                 break;
             case KeyEvent.VK_P:
                 setRelative(!isRelative());
             case KeyEvent.VK_SPACE:
                 it = animatedModels.iterator();
                 while (it.hasNext())
                 {
                     animatedModel = (AnimatedModel) it.next();
                     if (animatedModel.isNear(posx, posy, posz))
                     {
                         animatedModel.animate();
                     }
 
                 }
                 break;
         }
     }
 
     public void keyReleased(KeyEvent ke)
     {
     }
 
     public void centerMouse(GLCanvas canvas)
     {
         Point locOnScreen = canvas.getLocationOnScreen();
 
         int middleX = locOnScreen.x + (canvas.getWidth() / 2);
         int middleY = locOnScreen.y + (canvas.getHeight() / 2);
 
         try
         {
             Robot rob = new Robot();
             // Re-setting mouse coordinates.
             rob.mouseMove(middleX, middleY);
             prevMouseX = middleX;
             prevMouseY = middleY;
         } catch (Exception e)
         {
             System.out.println(e);
         }
     }
 
     public void processMouseMove(MouseEvent mouse, GLCanvas canvas)
     {
         int curMouseX = mouse.getX();
         int curMouseY = mouse.getY();
 
         final int maxStep = 5;
 
         int rightStep = curMouseX - (canvas.getWidth() / 2);
         if (rightStep > maxStep)
         {
             alpha = renderer.getAlpha() + 0.035f; /*
              * DIREITA
              */
             renderer.setAlpha(alpha);
             rightStep = maxStep;
         } else if (rightStep < -maxStep)
         {
             alpha = renderer.getAlpha() - 0.035f; /*
              * ESQUERDA
              */
             renderer.setAlpha(alpha);
             rightStep = -maxStep;
 
         }
 
         int upStep = curMouseY - (canvas.getHeight() / 2);
         if (upStep > maxStep)
         {
             beta = renderer.getBeta() - 0.035f;      /*
              * BAIXO
              */
             renderer.setBeta(beta);
             upStep = maxStep;
         } else if (upStep < -maxStep)
         {
             beta = renderer.getBeta() + 0.035f;     /*
              * CIMA
              */
             renderer.setBeta(beta);
             upStep = -maxStep;
         }
     }
 
     public void hideCursor(GLCanvas canvas)
     {
         int w = 1;
         int h = 1;
         int pix[] = new int[w * h];
 
         Image image = Toolkit.getDefaultToolkit().createImage(
                 new MemoryImageSource(w, h, pix, 0, w));
         Cursor cursor =
                 Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0, 0), "custom");
         canvas.setCursor(cursor);
     }
 }
