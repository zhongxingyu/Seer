 /* Modeling - Application to model threats.
  *
  * Copyright (C) 2010  INBio (Instituto Nacional de Biodiversidad)
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
 package org.inbio.modeling.web.controller;
 
 import com.lowagie.text.Document;
 import com.lowagie.text.pdf.PdfWriter;
 import java.io.FileInputStream;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import org.inbio.modeling.core.dto.GrassLayerDTO;
 import org.inbio.modeling.core.manager.ExportManager;
 import org.inbio.modeling.core.manager.FileManager;
 import org.inbio.modeling.core.manager.GrassManager;
 import org.inbio.modeling.web.form.ExportData;
 import org.inbio.modeling.web.form.converter.FormDTOConverter;
 import org.inbio.modeling.web.session.CurrentInstanceData;
 import org.inbio.modeling.web.session.SessionUtils;
 import org.springframework.validation.BindException;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.AbstractFormController;
 
 public class ExportController extends AbstractFormController {
 
     private GrassManager grassManagerImpl;
     private FileManager fileManagerImpl;
     private ExportManager exportManagerImpl;
     private ShowMapController showMapControllerImpl;
 
     @Override
     protected ModelAndView showForm(HttpServletRequest request
             , HttpServletResponse response
             , BindException errors) {
 
        ModelAndView model = null;

		if(errors != null && errors.hasErrors()){
            model = new ModelAndView();
			model.addAllObjects(errors.getModel());
        }
         return null;
     }
 
 
     @Override
     protected ModelAndView processFormSubmission(HttpServletRequest request
             , HttpServletResponse response
             , Object command
             , BindException errors) {
 
         HttpSession session = null;
         String type = null;
 
         if(errors.hasErrors())
             return showForm(request, response, errors);
 
         CurrentInstanceData currentInstanceData = null;
         ExportData exportForm = (ExportData)command;
 
         // retrieve the type.
         type = exportForm.getType();
 
         // retrieve the session Information.
         session = request.getSession();
         currentInstanceData = SessionUtils.isSessionAlive(session);
 
         if(currentInstanceData != null){
 
             if(exportForm.getType().equals("PDF")){ // PDF File
 
                 try {
                     this.exportPDF(currentInstanceData, response);
                 } catch (Exception ex) {
                     ex = new Exception("errors.cantExportPDF", ex);
                     Logger.getLogger(ColumnController.class.getName()).log(Level.SEVERE, null, ex);
                     errors.reject(ex.getMessage());
                     return showForm(request, response, errors);
                 }
                 
             } else if(exportForm.getType().equals("SHP")){ // Shapefile
 
                 try {
                     response = this.exportShapefile(currentInstanceData, response);
                 } catch (Exception ex) {
                     ex = new Exception("errors.cantExportShape", ex);
                     Logger.getLogger(ColumnController.class.getName()).log(Level.SEVERE, null, ex);
                     errors.reject(ex.getMessage());
                     return showForm(request, response, errors);
                 }
             }else{ // PNG
 
                 try {
                     response = this.exportImage(currentInstanceData, response);
                 } catch (Exception ex) {
                     ex = new Exception("errors.cantExportImage", ex);
                     Logger.getLogger(ColumnController.class.getName()).log(Level.SEVERE, null, ex);
                     errors.reject(ex.getMessage());
                     return showForm(request, response, errors);
                 }
             }
 
         }else{
             Exception ex = new Exception("errors.noSession");
             Logger.getLogger(ColumnController.class.getName()).log(Level.SEVERE, null, ex);
             errors.reject(ex.getMessage());
             return showForm(request, response, errors);
         }
 
 
         return showMapControllerImpl.showForm(request, response, errors);
     }
 
     private void exportPDF(CurrentInstanceData currentInstanceData, HttpServletResponse response) throws Exception{
 
         response.setContentType("application/pdf");
         response.setHeader("Content-Disposition","attachment; filename=reporte.pdf;");
         Document document = new Document();
 
         PdfWriter.getInstance(document, response.getOutputStream());
         document.open();
 
         document =  exportManagerImpl.exportPDF(document,
                 currentInstanceData.getResolution(),
                 currentInstanceData.getImageName(),
                 currentInstanceData.getLimitLayerName(),
                 FormDTOConverter.convert(currentInstanceData.getLayerList(), GrassLayerDTO.class),
                 currentInstanceData.getUserSessionId());
 
         document.close();
     }
 
 
     /**
      *
      * @param currentInstanceData
      */
     private  HttpServletResponse exportShapefile( CurrentInstanceData currentInstanceData, HttpServletResponse response) throws Exception{
 
 
         //generate a zip with all the files of the shapefile
         this.grassManagerImpl.exportLayer2Shapefile(
                 new GrassLayerDTO(currentInstanceData.getImageName(), 1L)
                 , currentInstanceData.getUserSessionId());
 
         FileInputStream fis = null;
         byte[] bytez = new byte[1024];
 
 
         //verify that the file exists and create a File Stream to read it.
         fis = this.exportManagerImpl.exportShapefile(currentInstanceData.getImageName(), currentInstanceData.getUserSessionId());
 
 
         if(fis != null){
 
             // set the content type so the browser can see this as it is
             response.setContentType("application/zip");
             response.setHeader("Content-Disposition","attachment; filename=ShapefileAmenazas.zip");
 
             // send the picture
             while(fis.read(bytez)!=-1){
                 response.getOutputStream().write(bytez);
             }
             fis.close();
             response.getOutputStream().close();
         }
         return response;
     }
     /**
      *
      * @param currentInstanceData
      */
     private  HttpServletResponse exportImage( CurrentInstanceData currentInstanceData, HttpServletResponse response) throws Exception{
 
 
         FileInputStream fis = null;
         byte[] bytez = new byte[1024];
 
         fis = this.exportManagerImpl.exportImage(currentInstanceData.getImageName(), currentInstanceData.getUserSessionId());
 
         if(fis != null){
 
             // set the content type so the browser can see this as it is
             response.setContentType("image/png");
             response.setHeader("Content-Disposition","attachment; filename=MapaAmenazas.png");
 
             // send the picture
             while(fis.read(bytez)!=-1){
                 response.getOutputStream().write(bytez);
             }
             fis.close();
             response.getOutputStream().close();
         }
 
         return response;
     }
 
     public FileManager getFileManagerImpl() {
         return fileManagerImpl;
     }
 
     public void setFileManagerImpl(FileManager fileManagerImpl) {
         this.fileManagerImpl = fileManagerImpl;
     }
 
     public GrassManager getGrassManagerImpl() {
         return grassManagerImpl;
     }
 
     public void setGrassManagerImpl(GrassManager grassManagerImpl) {
         this.grassManagerImpl = grassManagerImpl;
     }
 
     public ShowMapController getShowMapControllerImpl() {
         return showMapControllerImpl;
     }
 
     public void setShowMapControllerImpl(ShowMapController showMapControllerImpl) {
         this.showMapControllerImpl = showMapControllerImpl;
     }
 
     public ExportManager getExportManagerImpl() {
         return exportManagerImpl;
     }
 
     public void setExportManagerImpl(ExportManager exportManagerImpl) {
         this.exportManagerImpl = exportManagerImpl;
     }
 }
