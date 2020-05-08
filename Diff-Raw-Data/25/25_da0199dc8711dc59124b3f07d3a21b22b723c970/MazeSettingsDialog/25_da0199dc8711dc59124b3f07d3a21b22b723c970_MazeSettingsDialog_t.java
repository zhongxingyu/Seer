 package edu.ucsb.cs56.projects.games.cs56_games_maze;
 
 import java.awt.*;
 import javax.swing.*;
 import javax.swing.text.*;
 import javax.swing.border.*;
 import java.awt.event.*;
 import java.beans.*;
 
 /** A JDialog that displays the MazeSettingsPanel to allow settings changes
     @author Evan West
     @version CS56 S13 UCSB
  */
 public class MazeSettingsDialog extends JDialog{
 
     private MazeSettings settings;
     private MazeSettingsPanel panel;
 
     /** Constructor for dialog
 	@param settings MazeSettings object that will be both read and written to as necessary.
      */
    public MazeSettingsDialog(MazeSettings settings, MazeGui gm){
 	super();
 	this.setTitle("Settings");
 	this.settings=settings;
	this.panel = new MazeSettingsPanel(settings, gm);
 	this.setContentPane(this.panel);
 	this.pack();
     }
 
 }
