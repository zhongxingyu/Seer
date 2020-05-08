 /****************************************************************************
  * Copyright Fabien Sartor 
  * Contributors: Fabien Sartor (fabien.sartor@gmail.com)
  *               http://fasar.fr
  *  
  * This software is a computer program whose purpose to compute differences 
  * between two files.
  *
  ****************************************************************************
  *
  *  This software is governed by the CeCILL license under French law and
  *  abiding by the rules of distribution of free software.  You can  use, 
  *  modify and/ or redistribute the software under the terms of the CeCILL
  *  license as circulated by CEA, CNRS and INRIA at the following URL: 
  *  "http://www.cecill.info". 
  *  
  *  As a counterpart to the access to the source code and  rights to copy,
  *  modify and redistribute granted by the license, users are provided only
  *  with a limited warranty  and the software's author,  the holder of the
  *  economic rights,  and the successive licensors  have only  limited
  *  liability. 
  *  
  *  In this respect, the user's attention is drawn to the risks associated
  *  with loading,  using,  modifying and/or developing or reproducing the
  *  software by the user in light of its specific status of free software,
  *  that may mean  that it is complicated to manipulate,  and  that  also
  *  therefore means  that it is reserved for developers  and  experienced
  *  professionals having in-depth computer knowledge. Users are therefore
  *  encouraged to load and test the software's suitability as regards their
  *  requirements in conditions enabling the security of their systems and/or 
  *  data to be ensured and,  more generally, to use and operate it in the 
  *  same conditions as regards security. 
  *  
  *  The fact that you are presently reading this means that you have had
  *  knowledge of the CeCILL license and that you accept its terms. 
  *
  ****************************************************************************
  */
 
 package fsart.diffTools.gui;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
import org.apache.log4j.spi.LoggerFactory;
 
 import javax.swing.*;
 import java.awt.*;
 
 /**
  * User: fabien
  * Date: 24/04/12
  * Time: 21:39
  */
 public class DiffToolsGui {
 
     static private Log log = LogFactory.getLog(DiffToolsGui.class);
 
 
     public static void main(String[] args) {
         //Create and set up the window.
         JFrame frame = new JFrame("DiffTools");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
         log.debug("Demarrage de l'application");
 
         DiffToolsMainPanel gui = new DiffToolsMainPanel();
 
         frame.add(gui.getPanel());
         frame.setMinimumSize(gui.getPanel().getMinimumSize());
         frame.pack();
         //frame.setMinimumSize(gui.getPanel().getMinimumSize());
         frame.setLocationRelativeTo(frame.getParent());
         frame.setVisible(true);
         //System.exit(0);
     }
 }
