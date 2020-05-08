 /*
  *  Copyright (C) 2010 Markus Echterhoff <tam@edu.uni-klu.ac.at>,
  *                      Daniel Hoelbling (http://www.tigraine.at),
  *                      Augustin Malle
  *
  *  This file is part of EvoPaint.
  *
  *  EvoPaint is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with EvoPaint.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package evopaint.gui;
 
 import evopaint.Configuration;
 import evopaint.EvoPaint;
 import evopaint.Manifest;
 import evopaint.Selection;
 import evopaint.commands.DeleteCurrentSelectionCommand;
 import evopaint.commands.FillSelectionCommand;
 import evopaint.commands.FillSelectionCommandScattered;
 import evopaint.commands.SelectAllCommand;
 import evopaint.gui.listeners.SelectionListenerFactory;
 import evopaint.util.logging.Logger;
 
 import javax.swing.*;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Observable;
 import java.util.Observer;
 
 /**
  *
  * @author Markus Echterhoff <tam@edu.uni-klu.ac.at>
  * @author Daniel Hoelbling (http://www.tigraine.at)
  * @author Augustin Malle
  */
 public class MenuBar extends JMenuBar implements Observer {
     private Configuration configuration;
     private EvoPaint evopaint;
     private Showcase showcase;
     private JMenu selectionMenu;
     private JMenu activeSelections;
     private Wizard nw;
     private MenuBar mb;
 
     public MenuBar(Configuration configuration, final EvoPaint evopaint, SelectionListenerFactory listenerFactory, Showcase showcase) {
         this.configuration = configuration;
         this.evopaint = evopaint;
         this.showcase = showcase;
         this.mb=this;
         showcase.getCurrentSelections().addObserver(this);
         // World Menu
         JMenu worldMenu = new JMenu();
         worldMenu.setText("World");
         add(worldMenu);
 
         // File Menu Items        
         JMenuItem newItem = new JMenuItem();
         newItem.setText("New");
         newItem.addActionListener(new ActionListener() {
 		
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				// TODO Auto-generated method stub
 				nw = new Wizard(mb);
 			}
 		});
         
         
         worldMenu.add(newItem);
         
         worldMenu.add(new JMenuItem("Open..."));
         worldMenu.add(new JMenuItem("Save"));
         worldMenu.add(new JMenuItem("Save as..."));
         worldMenu.add(new JMenuItem("Import..."));
              
         JMenuItem exportItem = new JMenuItem();
         exportItem.setText("Export");
         exportItem.addActionListener(new ExportDialog(configuration, evopaint));
         
         worldMenu.add(exportItem);
         
         worldMenu.add(new JMenuItem("Options..."));
         JMenuItem endItem = new JMenuItem();
         endItem.setText("End");
         endItem.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 if (JOptionPane.showConfirmDialog(getRootPane(), "Do you really want to Exit?", "Exit EvoPaint", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                     System.exit(0);
                 }
             }
         });
         worldMenu.add(endItem);
 
         // selection menu
         selectionMenu = new JMenu("Selection");
         add(selectionMenu);
 
 
         JMenuItem selectAll = new JMenuItem("Select All");
         selectAll.addActionListener(new SelectAllCommand(showcase, evopaint.getConfiguration()));
         selectionMenu.add(selectAll);
 
         JMenuItem selectionSetName = new JMenuItem("Set Name...");
         selectionMenu.add(selectionSetName);
         selectionSetName.addActionListener(listenerFactory.CreateSelectionSetNameListener());
        JMenuItem fillSelection = new JMenuItem("Fill");
        fillSelection.addActionListener(new FillSelectionCommand(showcase));
        selectionMenu.add(fillSelection);
        JMenuItem fillHalfSelection = new JMenuItem("Fill 50%");
        fillHalfSelection.addActionListener(new FillSelectionCommandScattered(showcase));
        selectionMenu.add(fillHalfSelection);
         selectionMenu.add(new JMenuItem("Open as new"));
         selectionMenu.add(new JMenuItem("Copy"));
         selectionMenu.add(new JMenuItem("Options..."));
         activeSelections = new JMenu("Selections");
         JMenuItem deleteCurrentSelection = new JMenuItem("Delete current");
         selectionMenu.add(deleteCurrentSelection);
         deleteCurrentSelection.addActionListener(new DeleteCurrentSelectionCommand(showcase));
         selectionMenu.add(activeSelections);
         JMenuItem clearSelections = new JMenuItem("Clear Selections");
         clearSelections.addActionListener(listenerFactory.CreateClearSelectionsListener());
         selectionMenu.add(clearSelections);
 
         // info menu
         JMenu infoMenu = new JMenu();
         infoMenu.setText("Info");
         add(infoMenu);
 
         JMenuItem userGuide = new JMenuItem();
         userGuide.setText("User Guide");
         userGuide.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 //.getDesktop().browse(new URI("http://www.your.url"));
                 try {
                     java.awt.Desktop.getDesktop().browse(new URI(Manifest.USER_GUIDE_URL));
                 } catch (IOException e1) {
                     Logger.log.error("Exception occurred during opening of Users guide: \n%s", e1);
                 } catch (URISyntaxException e1) {
                     Logger.log.error("Exception occurred during opening of Users guide: \n%s", e1);
                 }
             }
         });
         infoMenu.add(userGuide);
 
         JMenuItem getCode = new JMenuItem();
         getCode.setText("Get the source code");
         getCode.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 try {
                     java.awt.Desktop.getDesktop().browse(new URI(Manifest.CODE_DOWNLOAD_URL));
                 } catch (URISyntaxException e1) {
                     Logger.log.error("Exception occurred during opening of Get The Code : \n%s", e1);
                 } catch (IOException e1) {
                     Logger.log.error("Exception occurred during opening of Get The Code : \n%s", e1);
                 }
             }
         });
         infoMenu.add(getCode);
 
         JMenuItem about = new JMenuItem();
         about.setText("About");
         about.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 String msg = "EvoPaint is being developed as part of a software lab\n" +
                                 "for the Bachelor's Degree at the\n" +
                                 "University of Klagenfurt, Austria.\n" +
                                 "\n" +
                                 "Enjoy.";
                 JOptionPane.showMessageDialog(getRootPane(), msg, "About EvoPaint", JOptionPane.INFORMATION_MESSAGE);
             }
         });
         infoMenu.add(about);
 
     }
 /*
     public void addSelection(Selection selection) {
         activeSelections.add(new SelectionWrapper(selection, showcase));
     }*/
 
     public void update(Observable o, Object arg) {
         SelectionList.SelectionListEventArgs eventEvent = (SelectionList.SelectionListEventArgs) arg;
         if (eventEvent.getChangeType() == SelectionList.ChangeType.LIST_CLEARED) {
             activeSelections.removeAll();
         }
         if (eventEvent.getChangeType() == SelectionList.ChangeType.ITEM_ADDED) {
             activeSelections.add(new SelectionWrapper(eventEvent.getSelection(), showcase));
         }
         if (eventEvent.getChangeType() == SelectionList.ChangeType.ITEM_DELETED) {
             for(int i = 0; i < activeSelections.getItemCount(); i++) {
                 SelectionWrapper wrapper = (SelectionWrapper)activeSelections.getItem(i);
                 if (wrapper.selection == eventEvent.getSelection()) {
                     activeSelections.remove(i);
                     break;
                 }
             }
         }
     }
 
     private class SelectionWrapper extends JMenuItem implements Observer
     {
         private Selection selection;
         private SelectionManager selectionManager;
 
         private SelectionWrapper(Selection selection, SelectionManager manager) {
             selectionManager = manager;
             selection.addObserver(this);
             this.selection = selection;
             UpdateName(selection);
             addMouseListener(new SelectionMouseListener());
         }
 
         public void update(Observable o, Object arg) {
             Selection selection = (Selection) o;
             UpdateName(selection);
         }
 
         private void UpdateName(Selection selection) {
             this.setText(selection.getSelectionName());
         }
 
         private class SelectionMouseListener implements MouseListener
         {
             public void mouseClicked(MouseEvent e) {}
 
             public void mousePressed(MouseEvent e) {}
 
             public void mouseReleased(MouseEvent e) {
                 selectionManager.setActiveSelection(selection);
             }
 
             public void mouseEntered(MouseEvent e) {
                 selection.setHighlighted(true);
             }
 
             public void mouseExited(MouseEvent e) {
                 selection.setHighlighted(false);
             }
         }
     }
 
 	public void newEvolution(int x, int y) {
 		//evopaint.getConfiguration().running = false;
 		//todo wizard code & implementation of a new evolution
 		
 		//Configuration newConf = new Configuration();
             //    newConf.dimension = new Dimension(x,y);
 		//evopaint.setConfiguration(newConf);
 		
 		
 		//evopaint.setWorld(new World(new Pixel[newConf.dimension.width * newConf.dimension.height],0, newConf));
 		//evopaint.setPerception(new Perception(new BufferedImage
                // (newConf.dimension.width, newConf.dimension.height,
               //  BufferedImage.TYPE_INT_RGB)));
 
       //  evopaint.getPerception().createImage(configuration.world);
        
         //check evopaint
 		//check config
 		//check work
 		// implement
 		// new config 
 		// evopaint= config
 		// evopaint work
         
 		//evopaint.getFrame().setConfiguration(newConf);
 		//evopaint.getFrame().initializeCommands(evopaint);
 		//evopaint.getFrame().initSecond(evopaint);
 		
 
 		//evopaint.getConfiguration().running = true;
 		
 	}
 }
