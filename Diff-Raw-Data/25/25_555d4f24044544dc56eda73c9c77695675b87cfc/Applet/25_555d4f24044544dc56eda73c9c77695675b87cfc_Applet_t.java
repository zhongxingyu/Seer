 package com.pocoplay.midiplayer;
 
 import com.pocoplay.errors.MissingAppletParameter;
 import com.pocoplay.midiplayer.ui.PlayerUI;
 
import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;

 public class Applet extends JApplet {
 
 	private static final long serialVersionUID = 1L;
 	private final PlayerUI midi_player = new PlayerUI();
 	private URL midi_url;
 	
 	private void setupUi() {
 		setLayout(new BorderLayout());
 		getContentPane().add(midi_player, BorderLayout.CENTER);
 	}
 	
 	private void loadParameters() throws MalformedURLException, MissingAppletParameter {
 		URL site;
 		String site_parameter = getParameter("site");
 		if (site_parameter == null)
		{
 			site = getDocumentBase();
		}
 		else
		{
 			site = new URL(site_parameter);
		}
 		
 		String midi_file = getParameter("midi");
 		if (midi_file == null)
		{
 			throw new MissingAppletParameter();
		}

 		try {
 			midi_url = new URL("http", site.getHost(), site.getPort(), midi_file);
 		} catch (MalformedURLException e) {
 			// TODO Gui and log notification of failure.
 		}
 	}
 	
 	/**
 	 * Applet entry point
 	 */
     @Override
 	public void init() {
     	setupUi();
     	try {
 			loadParameters();
 			midi_player.loadMidi(midi_url);
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (MissingAppletParameter e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     }
     
     /**
      * Applet cleanup code
      */
     @Override
     public void destroy() {
     	midi_player.close();
     	super.destroy();
     }
 
 }
