 package klient;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.net.URL;
 
 import javax.swing.*;
 
 public class KlientGUI extends JPanel implements ActionListener {
 
 	private String[] tags;
 	private int visning;
 	private BildeBuffer bilder;
 
 	
 	private Timer timer;
 
 	public KlientGUI()
 	{
 		this.setPreferredSize(new Dimension(500,500));
 		
 		bilder = new BildeBuffer(1000);
 		Thread bbtraad = new Thread(bilder);
 		bbtraad.start();
 		
		timer = new Timer(2500, this);
 		timer.start();
 
 		// Lag GUIKomponenter og sett GUI modus
 	}
 
 	private void ByggGUI(int modusnr)
 	{
 		removeAll();
 		// Bygg GUI for spesifik modus
 	}
 
 	public String[] LesTags()
 	{
 		return tags;
 	}
 
 
 	public String[] LesTags(int max)
 	{
 		if(tags.length < max)
 			max = tags.length;
 		String[] tmp = new String[max];
 		for (int i = 0; i<max; i++)
 			tmp[i] = tags[i];
 		return tmp;
 	}
 
 	public void GiBilder(URL[] l)
 	{
 		bilder.Oppdater(l);
 		visning = 0;
 	}
 
 	private void VisNesteBilde()
 	{
 		visning++;
 		if(visning>=bilder.Bufferedlength())
 			visning = 0;
 		repaint();
 	}
 
 	BufferedImage bilde = null;
 	
 	@Override
 	public void paintComponent(Graphics g)
 	{
 		BufferedImage tmpbilde = bilder.HentBilde(visning);
 		if(tmpbilde != null)
 			bilde = tmpbilde;
 		if(bilde != null)
			g.drawImage(bilde, 0, 0, this.getWidth(), this.getHeight(), this);
 		else
			g.drawString("Laster bilder fra nettet, venligst vent...", 50, 50);
 	}
 
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		if(arg0.getSource() == timer)
 			VisNesteBilde();
 	}
 
 }
