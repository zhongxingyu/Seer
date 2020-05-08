 package org.geoserver.wms.web.data;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.wicket.WicketRuntimeException;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.AjaxLink;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.SubmitLink;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.form.upload.FileUpload;
 import org.apache.wicket.markup.html.form.upload.FileUploadField;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.Model;
 import org.geoserver.catalog.Catalog;
 import org.geoserver.catalog.StyleInfo;
import org.geoserver.ows.util.ResponseUtils;
 import org.geoserver.web.GeoServerSecuredPage;
 
 /**
  * Allows for editing a new style, includes file upload
  */
 @SuppressWarnings("serial")
 public class StyleNewPage extends GeoServerSecuredPage {
 
    TextField nameTextField;
     SLDEditorPanel sldEditorPanel;
     FileUploadField fileUploadField;
     
     public StyleNewPage() {
         StyleInfo s = getCatalog().getFactory().createStyle();
         
         Form form = new Form( "form", new CompoundPropertyModel( s ) );
         add(form);
         
        form.add( nameTextField = new TextField( "name", String.class ) );
         form.add( sldEditorPanel = new SLDEditorPanel( "sld", new Model() ) );
         sldEditorPanel.setOutputMarkupId( true );
         
         try {
             sldEditorPanel.setRawSLD(getClass().getResourceAsStream( "template.sld"));
         } 
         catch (IOException e) {
             throw new WicketRuntimeException( e );
         }
         
         form.add( fileUploadField = new FileUploadField( "filename" ) );
         
         SubmitLink uploadLink = new SubmitLink( "upload", form ) {
             @Override
             public void onSubmit() {
                 FileUpload upload = fileUploadField.getFileUpload();
                if ( upload == null ) {
                    warn( "No file selected.");
                    return;
                }
                 ByteArrayOutputStream bout = new ByteArrayOutputStream();
                 
                 try {
                     IOUtils.copy(upload.getInputStream(), bout );
                     sldEditorPanel.setRawSLD( new ByteArrayInputStream( bout.toByteArray() ) );
                     sldEditorPanel.updateModel();
                 } 
                 catch (IOException e) {
                     throw new WicketRuntimeException( e );
                 }
                 
                 //update the style object
                 StyleInfo s = (StyleInfo) getForm().getModelObject();
                 s.setFilename( upload.getClientFileName() );
                
                if ( s.getName() == null || "".equals( s.getName().trim() ) ) {
                    //set it
                    nameTextField.setModelValue( ResponseUtils.stripExtension( upload.getClientFileName() ) );
                    nameTextField.modelChanged();
                }
             }
         };
         form.add( uploadLink );
         
         SubmitLink submitLink = new SubmitLink( "submit", form ) {
             @Override
             public void onSubmit() {
                 //add the style
                 Catalog catalog = getCatalog();
                 StyleInfo s = (StyleInfo) getForm().getModelObject();
                 
                 if ( s.getFilename() == null ) {
                     //TODO: check that this does not overrite any existing files
                     s.setFilename( s.getName() + ".sld" );
                 }
                 try {
                     getCatalog().add( s );    
                 }
                 catch( Exception e ) {
                     getForm().error( e );
                     return;
                 }
 
                 //write out the SLD
                 try {
                     catalog.getResourcePool().writeStyle( s, 
                             new ByteArrayInputStream( sldEditorPanel.getRawSLD().getBytes() ) );
                 } 
                 catch (IOException e) {
                     throw new WicketRuntimeException(e);
                 }
                 
                 setResponsePage(StylePage.class);
             } 
         };
         form.add( submitLink );
         
         AjaxLink cancelLink = new AjaxLink( "cancel" ) {
             @Override
             public void onClick(AjaxRequestTarget target) {
                 setResponsePage( StylePage.class );
             }
         };
         form.add( cancelLink );
     }
 }
