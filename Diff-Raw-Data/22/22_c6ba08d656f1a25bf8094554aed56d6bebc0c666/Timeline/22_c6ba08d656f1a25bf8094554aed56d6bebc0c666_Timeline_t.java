 package gui;
 
 import gui.drawings.Drawing;
 
 import java.util.Vector;
 
 import javax.swing.JFrame;
 
 import web.GoogleComm;
 import web.XMLParser;
 
 import javax.swing.JTextArea;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 
 import javax.swing.JSlider;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import app.Pair;
 
 public class Timeline extends JFrame {
 	private static final long serialVersionUID = 386127950518737040L;
 	JTextArea textArea;
 	JSlider slider;
 	JTimelinePanel panel;
 	GoogleComm gcom;
 	Vector<Pair<String,String>> v = null;
 	String title;
 	public Timeline(GoogleComm gcom, String title) {
 		this.gcom = gcom;
 		this.title = title;
 		textArea = new JTextArea();
 		getContentPane().add(textArea, BorderLayout.NORTH);
 
 		textArea.setText("No data yet");
 		
 		slider = new JSlider(JSlider.HORIZONTAL);
 		getContentPane().add(slider, BorderLayout.SOUTH);
 		slider.setSnapToTicks(true);
 		slider.setMajorTickSpacing(10);
 		slider.setMinorTickSpacing(1);
 
 		slider.setMinimum(0);
		slider.setMaximum(0);
 		slider.setValue(0);
 		
 		panel = new JTimelinePanel();
 		panel.setBackground(Color.white);
 		getContentPane().add(panel, BorderLayout.CENTER);
 
 		slider.addChangeListener(new ChangeListener() {
 			@Override
 			public void stateChanged(ChangeEvent arg0) {
 				int id = slider.getValue();
 				if (v == null)
 					return;
				if (id >= v.size())
 					return;
 				textArea.setText(v.get(id).getK());
 				String content = v.get(id).getV();
 				if (content.length() > 0) {
 					XMLParser parser = new XMLParser(content);
 					Vector<Drawing> dr = parser.getDrawings();
 					panel.setTimelineImage(dr);
 					panel.updateUI();
 				}
 			}
 		});
 
 		(new Thread(){
 			public void run() {
 				init();
 			}
 		}).start();
 		
 		this.setVisible(true);
 		this.setLocationRelativeTo(null);
 		this.setSize(700, 500);
 	}
 	
 	public void init() {
 		v = gcom.getRevisions(title);
 		SwingUtilities.invokeLater(new Runnable() {
 			
 			@Override
 			public void run() {
				if (v.size() > 0) {
 					textArea.setText(v.get(0).getK());
					slider.setMinimum(0);
					slider.setMaximum(v.size()-1);
					slider.setValue(0);
				}
 			}
 		});
 	}
 	
 }
 
 class JTimelinePanel extends JPanel {
 	private static final long serialVersionUID = 1L;
 	private BufferedImage image;
 
     public JTimelinePanel() {
        image = null;
     }
     
     public void setTimelineImage(Vector<Drawing> dr) {
     	image = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_ARGB);
 		Graphics2D g2 = image.createGraphics();
 		g2.setColor(Color.white);
 		g2.fillRect(0, 0, image.getWidth(), image.getHeight());
 		for (Drawing d : dr) {
 			d.draw(g2);
 		}
     }
 
     @Override
     public void paint(Graphics g) {
     	super.paint(g);
         ((Graphics2D)g).drawImage(image, 0, 0, this);
     }
 
 }
