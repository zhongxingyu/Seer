 package com.t_oster.notenschrank.gui;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.io.IOException;
 import java.security.InvalidParameterException;
 import java.util.LinkedList;
 
 import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.border.BevelBorder;
 
 import com.t_oster.notenschrank.data.Sheet;
 
 public class PreviewPanel extends JPanel implements Runnable, MouseListener, MouseWheelListener{
 	
 	/**
 	 * 
 	 */
 	public static final int IMAGE_RELOADED = 0;
 	
 	private static final long serialVersionUID = 595838121389049252L;
 	private JLabel widget;
 	private Image image;
 	private Sheet sheet;
 	private boolean refreshing=false;
 	private LinkedList<ActionListener> aListeners = new LinkedList<ActionListener>();
 	public void addActionListener(ActionListener a){
 		aListeners.add(a);
 	}
 	public void removeActionListener(ActionListener a){
 		aListeners.remove(a);
 	}
 	
 	public Image getShownImage(){
 		synchronized(this){
 			return image;
 		}
 	}
 	
 	public PreviewPanel(Component parent){
 		this.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
 		this.setPreferredSize(new Dimension(300,200));
 		widget = new JLabel("keine Datei zum einsortieren");
 		this.add(widget);
 		this.validate();
 		this.setVisible(true);
 		this.addMouseListener(this);
 		this.addMouseWheelListener(this);
 	}
 	
 	public PreviewPanel(Component parent, Sheet sheet) {
 		this(parent);
 		this.showSheet(sheet);
 	}
 
 	private int zoomLevel=0;
 	private Dimension rPos = new Dimension(0,0);
 	private Dimension rSize = new Dimension(100,30);
 	public void zoom(boolean zoomIn, boolean zoomLeft, boolean zoomUp){
 		if (zoomLevel==0 && !zoomIn){
 			return;
 		}
 		if (zoomIn){
 			zoomLevel++;
 			rSize.width/=2;
 			rSize.height/=2;
 			if (!zoomLeft){
 				rPos.width+=rSize.width;
 			}
 			if (!zoomUp){
 				rPos.height+=rSize.height;
 			}
 		}
 		else{
 			zoomLevel--;
 			rSize.width=(2*rSize.width)%101;
 			rSize.height=(2*rSize.height)%101;
 			if (rPos.width+rSize.width>100){
 				rPos.width=(100-rSize.width);
 			}
 			if (rPos.height+rSize.height>100){
 				rPos.height=(100-rSize.height);			}
 		}
 		this.refresh();
 	}
 	
 	public void showSheet(Sheet s){
 		synchronized (this){
 			this.sheet=s;
 		}
 		this.refresh();
 	}
 	
 	public Sheet getSheet(){
 		synchronized(this){
 			return this.sheet;
 		}
 	}
 	
 	public void refresh(){
 		synchronized (this){
 			if (!this.refreshing){
 				this.refreshing=true;
 				this.image=null;
 				new Thread(this).start();
 			}
 			else{
 				System.out.println("refresh dropped");
 			}
 		}
 	}
 	
 	@Override
 	public void run() {
 		Sheet mysheet;
 		synchronized (this){
 			mysheet=sheet;
 		}
 		if (mysheet==null){
 			this.widget.setText("keine Datei zum einsortieren");
 			this.add(widget);
 		}
 		else{
 			try {
 				int width = this.getWidth();
 				int height =  this.getHeight();
 				if (width==0 || height==0){
 					synchronized(this){
 						refreshing=false;
 						return;
 					}
 				}
 				JProgressBar tmp = new JProgressBar();
 				tmp.setIndeterminate(true);
 				this.add(tmp);
 				this.widget.setText("...lade...");
 				this.add(widget);
 				this.validate();
 				//Bildgrösse
 				
 				
 				//Passe relative höhe an um in Bildgrösse zu bleiben
 				this.rSize.height = this.rSize.width*height/width;
 				if (rPos.height<0){
 					rPos.height=0;
 				}
 				if (rPos.height>100){
 					rPos.height=100;
 				}
 				if (rPos.height+rSize.height>100){
 					rPos.height=100-rSize.height;
 				}
 				Image i = sheet.getPreview(rPos, rSize, new Dimension(width,height));
 				this.remove(tmp);
 				this.remove(widget);
 				widget.setText("");
 				synchronized (this){
 					this.image = i;
 				}
 				this.validate();
 				for(ActionListener a:aListeners){
 					a.actionPerformed(new ActionEvent(this, IMAGE_RELOADED, "image refreshed"));
 				}
 			} catch (IOException e) {
 				this.widget.setText("Fehler beim Anzeigen...\n"+e.getMessage());
 				this.validate();
 			} catch (InvalidParameterException e){
 				System.err.println(e.getMessage());
 				widget.setText("Fehler");
 				this.validate();
 			}
 			
 		}
 		this.validate();
 		this.repaint();
 		synchronized(this){
 			this.refreshing=false;
 		}
 	}
 
 	int oldwidth=0;
 	int oldheight=0;
 	public void paintComponent(Graphics g){
 		super.paintComponent(g);
 		if (this.getWidth()!= oldwidth || this.getHeight()!=oldheight){
 			oldwidth=this.getWidth();
 			oldheight=this.getHeight();
 			this.refresh();
 		}
 		synchronized (this){
 			if (image!=null){
 				g.drawImage(image, 0, 0, null);
 			}
 		}
 	}
 	
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		boolean zoomIn=false;
 		if (e.getButton()==MouseEvent.BUTTON1){
 			zoomIn=true;
 		}
 		boolean zoomLeft = (e.getPoint().x<=this.getSize().width/2);
 		boolean zoomUp = (e.getPoint().y<=this.getSize().height/2);
 		this.zoom(zoomIn,zoomLeft,zoomUp);
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseWheelMoved(MouseWheelEvent e) {
 		Dimension oldpos = new Dimension(rPos);
 		rPos.height=rPos.height+e.getWheelRotation()*(rSize.height/3);
 		if (rPos.height<0){
 			rPos.height=0;
 		}
 		else if (rPos.height+rSize.height>100){
 			rPos.height=100-rSize.height;
 		}
 		if (!oldpos.equals(rPos)){
 			this.refresh();
 		}
 	}
 }
