 /*
  *  PathFind -- a Diamond system for pathology
  *
  *  Copyright (c) 2009 Carnegie Mellon University
  *  All rights reserved.
  *
  *  PathFind is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, version 2.
  *
  *  PathFind is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with PathFind. If not, see <http://www.gnu.org/licenses/>.
  *
  *  Linking PathFind statically or dynamically with other modules is
  *  making a combined work based on PathFind. Thus, the terms and
  *  conditions of the GNU General Public License cover the whole
  *  combination.
  *
  *  In addition, as a special exception, the copyright holders of
  *  PathFind give you permission to combine PathFind with free software
  *  programs or libraries that are released under the GNU LGPL or the
  *  Eclipse Public License 1.0. You may copy and distribute such a system
  *  following the terms of the GNU GPL for PathFind and the licenses of
  *  the other code concerned, provided that you include the source code of
  *  that other code when and as the GNU GPL requires distribution of source
  *  code.
  *
  *  Note that people who make modified versions of PathFind are not
  *  obligated to grant this special exception for their modified versions;
  *  it is their choice whether to do so. The GNU General Public License
  *  gives permission to release a modified version without this exception;
  *  this exception also makes it possible to release a modified version
  *  which carries forward this exception.
  */
 
 package edu.cmu.cs.diamond.pathfind;
 
 import java.awt.geom.Path2D;
 import java.util.Scanner;
 
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 
 import edu.cmu.cs.diamond.opendiamond.ObjectIdentifier;
 import edu.cmu.cs.diamond.opendiamond.Util;
 
 public class ResultIcon {
 
     private final ImageIcon icon;
 
     private final ObjectIdentifier identifier;
 
    private final byte[] quickhash1;
    private final byte[] tilebounds;
 
     public ResultIcon(ImageIcon icon, ObjectIdentifier identifier,
                       byte[] quickhash1, byte[] tilebounds) {
         this.icon = icon;
         this.identifier = identifier;
        this.quickhash1 = quickhash1;
        this.tilebounds = tilebounds;
     }
 
     public Icon getIcon() {
         return icon;
     }
 
     public ObjectIdentifier getObjectIdentifier() {
         return identifier;
     }
 
     public String getQuickHash1() {
        return Util.extractString(quickhash1);
     }
 
     public Path2D getTileBounds(long width) {
        String bounds = Util.extractString(tilebounds);
 
        Scanner sc = new Scanner(bounds);
         Double xmin = sc.nextDouble() * width;
         Double ymin = sc.nextDouble() * width;
         Double xmax = sc.nextDouble() * width;
         Double ymax = sc.nextDouble() * width;
 
         Path2D p = new Path2D.Double();
         p.moveTo(xmin, ymin);
         p.lineTo(xmin, ymax);
         p.lineTo(xmax, ymax);
         p.lineTo(xmax, ymin);
         p.closePath();
         return p;
     }
 }
