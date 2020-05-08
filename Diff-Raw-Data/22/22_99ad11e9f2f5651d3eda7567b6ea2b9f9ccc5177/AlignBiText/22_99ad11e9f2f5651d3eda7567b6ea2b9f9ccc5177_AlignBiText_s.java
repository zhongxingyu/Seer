 /**
  * ********
  * Copyright © 2010-2012 Olanto Foundation Geneva
  *
  * This file is part of myCAT.
  *
  * myCAT is free software: you can redistribute it and/or modify it under the
  * terms of the GNU Affero General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  *
  * myCAT is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with myCAT. If not, see <http://www.gnu.org/licenses/>.
  *
  *********
  */
 package org.olanto.mapman.server;
 
 import java.rmi.RemoteException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.olanto.idxvli.server.IndexService_MyCat;
 
 /**
  * classe pour l'alignement de bitext.
  *
  * add possibility to insert a CR between paragraph
  *
  */
 public class AlignBiText {
 
     public SegDoc source;
     public SegDoc target;
     public IntMap map;
     public String query;
     static IndexService_MyCat is;
     static MapService ms;
     static String rootTxt;
    static int ncal;
     static boolean skipLine;
 
     public AlignBiText(String fileso, String langso, String langta, String query, int w, int h) {
 
         // initialisisation en cas d'erreur
         System.out.println("file source:" + fileso);
         if (fileso.contains("/Glossaries") && fileso.contains("_" + langso)) {
             fileso = fileso.replace(langso + "/", "XX/");
             System.out.println("Glossaries -> new file source:" + fileso);
         }
         source = new SegDoc(fileso, "file source:" + fileso + " language:" + langso + "\n\n", langso);
 
         target = new SegDoc("N/A", "file target:" + "N/A" + " language:" + langta + "\n\n", langta);
         map = new IntMap(3);
         source.positions = getLineStat(source.lines, w, h, source.content.length());
        source.Ncal = ncal;
         target.positions = getLineStat(target.lines, w, h, target.content.length());
        target.Ncal = ncal;
 
 
         if (is == null) {
             is = org.olanto.conman.server.GetContentService.getServiceMYCAT("rmi://localhost/VLI");
         }
         if (ms == null) {
             ms = GetMapService.getServiceMAP("rmi://localhost/MAP");
         }
         if (rootTxt == null) {
             try {
                 rootTxt = is.getROOT_CORPUS_TXT();
             } catch (RemoteException ex) {
                 source.lines[1] = "Error:no index server";
                 Logger.getLogger(AlignBiText.class.getName()).log(Level.SEVERE, null, ex);
                 return; //stop processing
             }
         }
         // tester la langue ....
         // chercher les documents
         fileso = rootTxt + "/" + fileso;
         try {
             skipLine = ms.isSkipLine(); // initialisation from parameter
         } catch (RemoteException ex) {
             Logger.getLogger(AlignBiText.class.getName()).log(Level.SEVERE, null, ex);
         }
         String filepivot = getNameOfDocForThisLang(fileso, "EN");
         String fileta = getNameOfDocForThisLang(fileso, langta);
         //System.out.println("fileso"+fileso+"\n"+"fileta"+fileta+"\n"+"filepivot"+filepivot);
         source = new SegDoc(fileso, langso);
         if (source.lines[1].startsWith("*** ERROR")) {
             System.out.println("no source file");
             return; //stop processing
         }
         source.positions = getLineStat(source.lines, w, h, source.content.length());
        source.Ncal = ncal;
         target = new SegDoc(fileta, langta);
         if (target.lines[1] != null && target.lines[1].startsWith("*** ERROR")) {
             System.out.println("no target file");
             return; //stop processing
         }
 
         target.positions = getLineStat(target.lines, w, h, target.content.length());
        target.Ncal = ncal;
         try {
             // chercher le numÃ©ro du doc pivot
             int docpivot = is.getDocId(filepivot);
             if (docpivot != -1) { // existe un docpivot
                 //System.out.println(" doc id :"+docso);
                 map = ms.getMap(docpivot, langso, langta);
                 if (skipLine) {
                     // map.dump("before skip");
                     map = map.skipLine();  // remap with empty line
                     //map.dump("after skip");
                 }
             } else { // pas de docpivot
                 map = null;
             }
             if (map == null) {
                 map = new IntMap(source.lines.length, target.lines.length);
             }
 
         } catch (RemoteException ex) {
             System.out.println("source and target files exist but they have no map (build a geometric one!)");
             map = new IntMap(source.lines.length, target.lines.length);
             Logger.getLogger(AlignBiText.class.getName()).log(Level.SEVERE, null, ex);
         }
 
        // dump();
 
     }
 
     public void dump() {
         source.dump("source");
         target.dump("target");
         map.dump("map");
     }
 
     public static String getNameOfDocForThisLang(String name, String Lang) {
         int lenRootTxt = rootTxt.length();
         return rootTxt + "/" + Lang + name.substring(lenRootTxt + 3);
     }
 // renommer les variables et ajouter des commentaires dans tout le code
 // Matrice (nombre de lignes, position du top, correction, position en pixel)
 
     public static int[][] getLineStat(String[] lines, int w, int h, int length) {
        ncal = 0;
         int[][] calc = new int[lines.length][5];
         if ((lines[0].length() / w) <= 1) {
             ncal++;
         }
         calc[0][0] = (lines[0].length() / w) + 1;// nombre de lignes dans la textarea de la phrase courante
         calc[0][1] = 0;// position du curseur pour la phrase courante dans la textarea
         calc[0][2] = 0;// correction pour la position du curseur pour IE afin de mettre au milieu la phrase
         calc[0][3] = 0;// nombre de lignes avant la phrase courante
         calc[0][4] = ncal;// nombre de phrases comportant une seule ligne avant la phrase courante
 
 
         float lin2 = 0, lin4 = 0;
         int pos = 0, len = 0, corr = 0, lin1 = 0, lin = 0, j = 0, lin3 = 0, ln = 0;
 
         for (int i = 1; i < lines.length; i++) {
             if (lines[i - 1].length() == 0) {
                 lin1++;
             } else {
                 lin1 += (lines[i - 1].length() / w);
             }
 
             lin3 = (lines[i - 1].length() / w);
             lin2 = (float) lines[i - 1].length() / (float) w;
             lin4 = lin2 - lin3;
             lin1 = lin4 > 0f ? lin1 + 1 : lin1;
 
             len = lines[i].length();
             //ajouter et simuler le comportement du contenu dans un textArea.
             pos += lines[i - 1].length() + 1;
             lin = (len / w) + 1;
             if (lin == 1) {
                 ncal++;
             }
             calc[i][0] = lin;
             calc[i][1] = pos;
             calc[i][3] = lin1;
             calc[i][4] = ncal;
         }
 
         for (int i = 1; i < lines.length; i++) {
             ln = calc[i][0];
             int ln1 = 1;
             corr = calc[i][1];
             j = i + 1;
             // adjust all correction to fit with the middle position
             // for all lines, if the sum of the line numbers is 
             // bigger than the half of the textarea size then put the position on the next line
             while ((j < lines.length) && (ln <= (h / 2) + 1)) {
                 ln1 = ln;
                 ln += calc[j][0];
                 corr = calc[j][1];
                 j++;
             }
             // add the correction for the rest of the lines 
             // when the actual last sentence makes more tha one line
             if ((h / 2) > ln1) {
                 corr += w * (((h / 2) - ln1) + 1);
             }
             // adjust to the middle of the line
             corr += lines[i].length() / 2 + 1;
             if (corr < length) {
                 calc[i][2] = corr;
             } else {
                 calc[i][2] = length;
             }
         }
         return calc;
     }
 }
