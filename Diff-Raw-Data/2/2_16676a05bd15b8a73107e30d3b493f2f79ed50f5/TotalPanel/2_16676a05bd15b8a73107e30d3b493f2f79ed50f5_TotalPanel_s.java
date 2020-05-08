 /*
  * Copyright 2011, MyCellar
  *
  * This file is part of MyCellar.
  *
  * MyCellar is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  *
  * MyCellar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with MyCellar. If not, see <http://www.gnu.org/licenses/>.
  */
 package fr.peralta.mycellar.interfaces.client.web.components.booking;
 
 import java.util.Map.Entry;
 
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.panel.GenericPanel;
 import org.apache.wicket.model.AbstractReadOnlyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 import fr.peralta.mycellar.domain.booking.Booking;
 import fr.peralta.mycellar.domain.booking.BookingBottle;
 import fr.peralta.mycellar.interfaces.client.web.renderers.RendererServiceFacade;
 
 /**
  * @author speralta
  */
 public class TotalPanel extends GenericPanel<Booking> {
 
     private static final long serialVersionUID = 201205231822L;
 
     @SpringBean
     private RendererServiceFacade rendererServiceFacade;
 
     /**
      * @param id
      * @param model
      */
     public TotalPanel(String id, IModel<Booking> model) {
         super(id, model);
         add(new Label("totalBottles", new AbstractReadOnlyModel<Integer>() {
             private static final long serialVersionUID = 201205221511L;
 
             /**
              * {@inheritDoc}
              */
             @Override
             public Integer getObject() {
                 Integer sum = 0;
                 if (TotalPanel.this.getModelObject() != null) {
                     for (Integer quantity : TotalPanel.this.getModelObject().getQuantities()
                             .values()) {
                         if (quantity != null) {
                             sum += quantity;
                         }
                     }
                 }
                 return sum;
             }
 
         }));
         add(new Label("totalPrice", new AbstractReadOnlyModel<String>() {
 
             private static final long serialVersionUID = 201205231511L;
 
             /**
              * {@inheritDoc}
              */
             @Override
             public String getObject() {
                 Float sum = 0f;
                 if (TotalPanel.this.getModelObject() != null) {
                     for (Entry<BookingBottle, Integer> entry : TotalPanel.this.getModelObject()
                             .getQuantities().entrySet()) {
                         if (entry.getValue() != null) {
                             sum += entry.getKey().getPrice() * entry.getValue();
                         }
                     }
                 }
                 return rendererServiceFacade.render(sum);
             }
 
         }));
         add(new Label("vat", new AbstractReadOnlyModel<String>() {
 
             private static final long serialVersionUID = 201205231511L;
 
             /**
              * {@inheritDoc}
              */
             @Override
             public String getObject() {
                 Float sum = 0f;
                 if (TotalPanel.this.getModelObject() != null) {
                     for (Entry<BookingBottle, Integer> entry : TotalPanel.this.getModelObject()
                             .getQuantities().entrySet()) {
                         if (entry.getValue() != null) {
                             sum += entry.getKey().getPrice() * entry.getValue();
                         }
                     }
                 }
                return rendererServiceFacade.render(sum * 0.196f);
             }
 
         }));
     }
 
 }
