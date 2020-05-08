 /*
  * $Id: $
  *
  * Copyright (C) 2009 by Brockmann Consult (info@brockmann-consult.de)
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the
  * Free Software Foundation. This program is distributed in the hope it will
  * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
  * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.esa.beam.chris.operators;
 
 import org.esa.beam.util.math.MathUtils;
 
 /**
  * CHRIS Constants, some differ for each mode
  *
  * @author Marco Zuehlke
  * @version $Revision$ $Date$
  */
 class ChrisModeConstants {
     
     private final int nCols;
     private final int nLines;
     private final double tpl;
     private final double fov;
     private static final double ftt =  1.2096E-03;
     
     private ChrisModeConstants(int nCols, int nLines, double tpl, double fov) {
         this.nCols = nCols;
         this.nLines = nLines;
         this.tpl = tpl;
         this.fov = fov;
     }
     
     /**
      * Number of Columns
      * for mode 1: (372 detected, 374 reported)
      */
     int getNCols() {
         return nCols;
     }
     
     /**
      * Number of Lines
      */
     int getNLines() {
         return nLines;
     }
     
     /**
      * Integration Time per Line [s]
      */
     double getTpl() {
         return tpl;
     }
     
     /**
      * Field Of View [rad]
      */
     double getFov() {
         return fov;
     }
     
     /**
      * Frame Transfer Time: [s]  every line. common to all modes
      */
     public double getFtt() {
         return ftt;
     }
     
     /**
      * Instant Field of View [rad]
      */
     double getIfov() {
        return fov / ftt;
     }
     
     /**
      * Total time for one line
      */
     double getDt() {
         return tpl + ftt;
     }
     
     /**
      * Time for one image acquisition
      */
     double getTimg() {
         return nLines * getDt();
     }
     
     //  Modes 0 and 20 characteristics are missing (already requested to Mike Cutter)
     private static final ChrisModeConstants MODE_1 = new ChrisModeConstants(372, 374, 24.1899E-03, 1.29261 * MathUtils.DTOR); 
     private static final ChrisModeConstants MODE_5 = new ChrisModeConstants(370, 748, 11.4912E-03, 0.63939 * MathUtils.DTOR); 
     private static final ChrisModeConstants MODE_X = new ChrisModeConstants(744, 748, 11.4912E-03, 1.28570 * MathUtils.DTOR); 
 
     static ChrisModeConstants get(int mode) {
         if (mode == 1) {
             return MODE_1;
         } else if (mode == 5){
             return MODE_5;
         } else {
             // Modes 2, 3, 3A(30) and 4 share characteristics 
             return MODE_X;
         }
     }
 }
