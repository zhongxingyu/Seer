 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, available at the root
  * application directory.
  */
 package org.geoserver.web.wicket;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.markup.html.form.FormComponentPanel;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 
 import com.vividsolutions.jts.geom.Envelope;
 
 /**
  * A form component for a {@link Envelope} object.
  * 
  * @author Justin Deoliveira, OpenGeo
  * @author Andrea Aime, OpenGeo
  */
 public class EnvelopePanel extends FormComponentPanel {
 
     Double minX,minY,maxX,maxY;
     CoordinateReferenceSystem crs;
     
     public EnvelopePanel(String id, ReferencedEnvelope e) {
         this(id, new Model(e));
     }
     
     public EnvelopePanel(String id, IModel model) {
         super(id, model);
         
         updateFields();
         
         add( new TextField( "minX", new PropertyModel(this, "minX")) );
         add( new TextField( "minY", new PropertyModel(this, "minY")) );
         add( new TextField( "maxX", new PropertyModel(this, "maxX") ) );
         add( new TextField( "maxY", new PropertyModel(this, "maxY")) );
     }
 
     private void updateFields() {
         ReferencedEnvelope e = (ReferencedEnvelope) getModelObject();
         if(e != null) {
             this.minX = e.getMinX();
             this.minY = e.getMinY();
             this.maxX = e.getMaxX();
             this.maxY = e.getMaxY();
             this.crs = e.getCoordinateReferenceSystem();
         }
     }
    
     public void setReadOnly( final boolean readOnly ) {
         visitChildren( TextField.class, new org.apache.wicket.Component.IVisitor() {
 
             public Object component(Component component) {
                 component.setEnabled( !readOnly );
                 return null;
             }
         });
     }
     
     @Override
     protected void convertInput() {
        visitChildren( TextField.class, new org.apache.wicket.Component.IVisitor() {

            public Object component(Component component) {
                ((TextField) component).processInput();
                return null;
            }
        });
        
         // update the envelope model
         if(minX != null && maxX != null && minY != null && maxX != null)
             setConvertedInput(new ReferencedEnvelope(minX, maxX, minY, maxY, crs));
         else
             setConvertedInput(null);
     }
     
     @Override
     protected void onModelChanged() {
         super.onModelChanged();
         // when the client programmatically changed the model, update the fields
         // so that the textfields will change too
         updateFields();
     }
 }
