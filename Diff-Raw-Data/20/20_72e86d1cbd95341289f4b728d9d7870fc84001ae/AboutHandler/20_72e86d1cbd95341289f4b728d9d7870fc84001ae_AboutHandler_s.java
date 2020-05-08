 /*******************************************************************************
  * Copyright (c) 2010 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package de.hswt.hrm.main.handlers;
 
 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.Arrays;
 import javax.inject.Named;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.UnsupportedAudioFileException;
 
 import org.eclipse.e4.core.di.annotations.Execute;
 import org.eclipse.e4.ui.services.IServiceConstants;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.widgets.Shell;
 
 public class AboutHandler {
 	@Execute
 	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {
 		String[] developer = new String[] { "Tobias Placht", "Anton Schreck",
 			"Lucas Haering", "Marek Bieber", "Benjamin Pabst", "Michael Sieger","Stefan Kleeberger"};
 		StringBuilder sb = new StringBuilder();
 		Arrays.sort(developer);
 		for (String s : developer) {
 			sb.append(s);
 			sb.append("\n");
 		}
 
 		// EASTER EGG
 		new Thread(new Runnable() {
 			
 			@Override
 			public void run() {
 				try {
					Clip clip = AudioSystem.getClip();
					Path p = Paths.get("..", "resources", "media", "freestiloer.m4a");
					if (!Files.exists(p)) {
						// TODO: log!
					}
 					
 					AudioInputStream audioStream = AudioSystem.getAudioInputStream(
							p.toFile());
 					clip.open(audioStream);
 					clip.start();
 				}
 				catch (UnsupportedAudioFileException|LineUnavailableException|IOException e) {
					// TODO: log!
 				}
 			}
 		}).start();			
 		// EASTER EGG END
 		
 		MessageDialog.openInformation(shell, "Developed by", sb.toString());
 
 	}
 }
