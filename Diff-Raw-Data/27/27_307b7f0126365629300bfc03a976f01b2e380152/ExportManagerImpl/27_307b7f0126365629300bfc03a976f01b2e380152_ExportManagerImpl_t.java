 /* Modeling - Application to model threats.
  *
  * Copyright (C) 2010  INBio ( Instituto Nacional de Biodiversidad )
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.inbio.modeling.core.manager.impl;
 
 import com.lowagie.text.Chunk;
 import com.lowagie.text.Document;
 import com.lowagie.text.Image;
 import com.lowagie.text.Paragraph;
 import com.lowagie.text.pdf.PdfPTable;
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.List;
 import org.inbio.modeling.core.dto.CategoryDTO;
 import org.inbio.modeling.core.dto.GrassLayerDTO;
 import org.inbio.modeling.core.manager.ExportManager;
 
 /**
  *
  * @author asanabria
  */
 public class ExportManagerImpl implements ExportManager {
 
     private String mapsHome;
     private String tempDir;
 
     @Override
     public FileInputStream exportImage(String layerName, long suffix) throws Exception{
 
         File f = new File(mapsHome+layerName+"_T_"+suffix+".png");
 
         FileInputStream fis = null;
 
         if(f.exists()){
             fis = new FileInputStream(f);
         }
 
         return fis;
     }
 
     @Override
     public FileInputStream exportShapefile(String layerName, long suffix) throws Exception {
 
         File f = new File(tempDir+layerName+"_T_"+suffix+".zip");
         FileInputStream fis = null;
 
         if(f.exists()){
             fis = new FileInputStream(f);
         }
 
         return fis;
     }
 
     @Override
     public Document exportPDF(Document document,
             Double resolution,
             String imageName,
             String limitLayerName,
             List<GrassLayerDTO>  layerList,
             long currentSessionId) throws Exception{
 
         //imagen que posee el mapa
         Image foto = Image.getInstance(mapsHome+imageName+"_T_"+currentSessionId+".png");
         //imagen que posee la escala
         Image foto2 = Image.getInstance(mapsHome+"scale.jpg");
 
         foto.scaleToFit(300, 300);
         foto2.scaleToFit(20, 100);
         document.add(new Paragraph("\n"));
         document.add(new Paragraph("Resolution: " + resolution));
         document.add(new Paragraph("\n"));
 
         foto.setAlignment(Chunk.ALIGN_CENTER);
         foto2.setAlignment(Chunk.ALIGN_LEFT);
 
         document.add(foto);
         document.add(new Paragraph("\n"));
         document.add(new Paragraph("High threat"));
         document.add(foto2);
         document.add(new Paragraph("Low threat"));
         document.add(new Paragraph("\n"));
 
         List<CategoryDTO> categorys;
         PdfPTable table;
         for (GrassLayerDTO grassLayerDTO : layerList) {
 
             if(grassLayerDTO.getType().match("areas")) {
                 document.add(new Paragraph("\n"));
                 document.add(new Paragraph("\n"));
                 document.add(new Paragraph(grassLayerDTO.getName()));
                 document.add(new Paragraph("\n"));
                 document.add(new Paragraph("Categorys:"));
                 document.add(new Paragraph("\n"));
 
                 table = new PdfPTable(2);
                 categorys = grassLayerDTO.getCategories();
                 for (CategoryDTO categoryDTO : categorys) {
 
                    if(categoryDTO != null){
                        table.addCell(categoryDTO.getValue());
                        table.addCell(categoryDTO.getDescription());
                    }
                 }
 
                 document.add(table);
 
             } else {
 
                 document.add(new Paragraph("\n"));
                 document.add(new Paragraph("\n"));
                 document.add(new Paragraph(grassLayerDTO.getName()));
                 document.add(new Paragraph("\n"));
                 document.add(new Paragraph("Intervals:"));
                 document.add(new Paragraph("\n"));
 
                 table = new PdfPTable(1);
                 categorys = grassLayerDTO.getCategories();
 
                 for (CategoryDTO categoryDTO : categorys) {
                     document.add(new Paragraph("        " + categoryDTO.getValue()));
                 }
             }
         }
 
         return document;
     }
 
 
     /*
      * ==================================================================
      */
 
     public String getMapsHome() {
         return mapsHome;
     }
 
     public void setMapsHome(String mapsHome) {
         this.mapsHome = mapsHome;
     }
 
     public String getTempDir() {
         return tempDir;
     }
 
     public void setTempDir(String tempDir) {
         this.tempDir = tempDir;
     }
 
 
 }
