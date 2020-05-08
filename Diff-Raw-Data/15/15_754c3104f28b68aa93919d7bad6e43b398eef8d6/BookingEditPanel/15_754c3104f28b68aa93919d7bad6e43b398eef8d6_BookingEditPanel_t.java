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
 package fr.peralta.mycellar.interfaces.client.web.components.booking.edit;
 
 import java.util.Map;
 
 import org.apache.wicket.event.IEvent;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.ComponentPropertyModel;
 import org.apache.wicket.model.StringResourceModel;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fr.peralta.mycellar.domain.booking.Booking;
 import fr.peralta.mycellar.domain.booking.BookingBottle;
 import fr.peralta.mycellar.interfaces.client.web.components.booking.autocomplete.BookingEventSimpleAutoComplete;
 import fr.peralta.mycellar.interfaces.client.web.components.booking.set.QuantitiesView;
 import fr.peralta.mycellar.interfaces.client.web.components.shared.Action;
 import fr.peralta.mycellar.interfaces.client.web.components.shared.AjaxTool;
 import fr.peralta.mycellar.interfaces.client.web.components.user.autocomplete.UserSimpleAutoComplete;
 import fr.peralta.mycellar.interfaces.client.web.shared.LoggingHelper;
 
 /**
  * @author speralta
  */
 public class BookingEditPanel extends Panel {
 
     private static final long serialVersionUID = 201107252130L;
     private static final Logger logger = LoggerFactory.getLogger(BookingEditPanel.class);
 
     private BookingEventSimpleAutoComplete bookingEventSimpleAutoComplete;
     private UserSimpleAutoComplete userSimpleAutoComplete;
     private QuantitiesView quantitiesView;
 
     private boolean initialized = false;
 
     /**
      * @param id
      */
     public BookingEditPanel(String id) {
         this(id, false);
     }
 
     /**
      * @param id
      * @param readonly
      */
     public BookingEditPanel(String id, boolean readonly) {
         super(id);
         add(userSimpleAutoComplete = new UserSimpleAutoComplete("customer",
                 new StringResourceModel("customer", null), null));
         add(bookingEventSimpleAutoComplete = new BookingEventSimpleAutoComplete("bookingEvent",
                 new StringResourceModel("bookingEvent", null)));
         add(quantitiesView = new QuantitiesView("bookingEvent.bottles", readonly));
         add(new WebMarkupContainer("noBottles") {
             private static final long serialVersionUID = 201108082329L;
 
             /**
              * {@inheritDoc}
              */
             @Override
             public boolean isVisible() {
                 return (bookingEventSimpleAutoComplete.getModelObject() == null)
                         || (bookingEventSimpleAutoComplete.getModelObject().getBottles().size() == 0);
             }
         });
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void onBeforeRender() {
         if (!initialized) {
             quantitiesView.setMapModel(new ComponentPropertyModel<Map<BookingBottle, Integer>>(
                     "quantities").wrapOnAssignment(quantitiesView));
             initialized = true;
         }
         super.onBeforeRender();
     }
 
     public BookingEditPanel setCustomerView() {
         userSimpleAutoComplete.setVisibilityAllowed(false);
         bookingEventSimpleAutoComplete.setVisibilityAllowed(false);
         return this;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void onEvent(IEvent<?> event) {
         LoggingHelper.logEventReceived(logger, event);
         if (event.getPayload() instanceof Action) {
             Action action = (Action) event.getPayload();
             switch (action) {
             case MODEL_CHANGED:
                 if (event.getSource() == bookingEventSimpleAutoComplete) {
                    Map<BookingBottle, Integer> quantities = ((Booking) getParent()
                            .getDefaultModelObject()).getQuantities();
                     quantities.clear();
                     AjaxTool.ajaxReRender(this);
                     event.stop();
                 }
                 break;
             default:
                 break;
             }
         }
         LoggingHelper.logEventProcessed(logger, event);
     }
 
 }
