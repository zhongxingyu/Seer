 /* *********************************************************************** *
  * project: org.jcompas.*
  * RunSimpleMetronome.java
  *                                                                         *
  * *********************************************************************** *
  *                                                                         *
  * copyright       : (C) 2012 by the members listed in the COPYING,        *
  *                   LICENSE and WARRANTY file.                            *
  * email           :                                                       *
  *                                                                         *
  * *********************************************************************** *
  *                                                                         *
  *   This program is free software; you can redistribute it and/or modify  *
  *   it under the terms of the GNU General Public License as published by  *
  *   the Free Software Foundation; either version 2 of the License, or     *
  *   (at your option) any later version.                                   *
  *   See also COPYING, LICENSE and WARRANTY file                           *
  *                                                                         *
  * *********************************************************************** */
 package org.jcompas.executables;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.swing.JFrame;
 import javax.swing.WindowConstants;
 
 import org.apache.log4j.BasicConfigurator;
 
 import org.jcompas.model.CompasFactory;
 import org.jcompas.model.CompasInformation;
 import org.jcompas.model.RelojRunner;
 import org.jcompas.model.sound.Clap;
 import org.jcompas.model.sound.MetronomeRunner;
 import org.jcompas.model.sound.MonoSoundClap;
 import org.jcompas.model.sound.Pattern;
 import org.jcompas.model.sound.Pattern.Golpe;
 import org.jcompas.model.sound.Pattern.Musician;
 import org.jcompas.model.sound.SimpleMetronome;
 import org.jcompas.view.Reloj;
 import org.jcompas.view.SimpleReloj;
 
 /**
  * @author thibautd
  */
 public class RunSimpleMetronome {
 	public static void main(final String[] args) throws FileNotFoundException {
 		BasicConfigurator.configure();
 		CompasInformation compas = CompasFactory.createBuleriasCompas();
 
 		Clap fuerte = new MonoSoundClap(
 				"fuerte",
				new FileInputStream( "/home/thibautd/code/workspace-perso/jCompas/src/resources/sounds/palmas/sonora-fuerte-1.wav" ));
 		Clap bajo = new MonoSoundClap(
 				"bajo",
				new FileInputStream( "/home/thibautd/code/workspace-perso/jCompas/src/resources/sounds/palmas/sonora-baja-1.wav" ));
 
 		List<Golpe> golpes = new ArrayList<Golpe>();
 		golpes.add( new Golpe( fuerte , 0 ) );
 		golpes.add( new Golpe( bajo , 1 / 12d ) );
 		golpes.add( new Golpe( bajo , 2 / 12d ) );
 		golpes.add( new Golpe( fuerte , 3 / 12d ) );
 		golpes.add( new Golpe( bajo , 4 / 12d ) );
 		golpes.add( new Golpe( bajo , 5 / 12d ) );
 		golpes.add( new Golpe( fuerte , 6 / 12d ) );
 		golpes.add( new Golpe( bajo , 7 / 12d ) );
 		golpes.add( new Golpe( fuerte , 8 / 12d ) );
 		golpes.add( new Golpe( bajo , 9 / 12d ) );
 		golpes.add( new Golpe( fuerte , 10 / 12d ) );
 		golpes.add( new Golpe( bajo , 11 / 12d ) );
 
 		Musician m = new Musician( "Jose" , golpes );
 
 		Pattern pattern = new Pattern(
 				"simple bulerias",
 				Arrays.asList( m ),
 				1,
 				1);
 
 		MetronomeRunner mrunner =
 			new MetronomeRunner(
 					new SimpleMetronome(
 						pattern,
 						compas));
 
 
 		JFrame window = new JFrame();
 		Reloj reloj = new SimpleReloj( compas );
 		window.getContentPane().add( reloj.getView() );
 		window.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
 		window.setVisible( true );
 		RelojRunner rrunner = new RelojRunner( reloj );
 
 		long start = System.currentTimeMillis() + 1000;
 		mrunner.start(
 				start,
 				3000 );
 		rrunner.start(
 				start,
 				3000);
 	}
 }
 
