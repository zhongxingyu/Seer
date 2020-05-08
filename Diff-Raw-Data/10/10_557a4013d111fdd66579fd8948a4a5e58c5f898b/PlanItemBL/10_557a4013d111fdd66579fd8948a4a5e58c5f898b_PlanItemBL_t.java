 /*
  jBilling - The Enterprise Open Source Billing System
  Copyright (C) 2003-2011 Enterprise jBilling Software Ltd. and Emiliano Conde
 
  This file is part of jbilling.
 
  jbilling is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  jbilling is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.
 
  You should have received a copy of the GNU Affero General Public License
  along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.sapienter.jbilling.server.item;
 
 import com.sapienter.jbilling.common.SessionInternalError;
 import com.sapienter.jbilling.server.item.db.ItemDTO;
 import com.sapienter.jbilling.server.item.db.PlanDTO;
 import com.sapienter.jbilling.server.item.db.PlanItemBundleDTO;
 import com.sapienter.jbilling.server.item.db.PlanItemDTO;
 import com.sapienter.jbilling.server.order.db.OrderLineDTO;
 import com.sapienter.jbilling.server.pricing.PriceModelBL;
 import com.sapienter.jbilling.server.pricing.db.PriceModelDTO;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * PlanItemDTO business logic. Currently only contains static factory methods for converting
  * PlanItemDTO objects into their web-service equivalents.
  *
  * @author Brian Cowdery
  * @since 20-09-2010
  */
 public class PlanItemBL {
 
     /**
      * Convert a given PlanItemWS web-service object into a PlanItemDTO.
      *
      * The PlanItemWS web-service object must have an affected item id, price model
      * and price model currency id or an exception will be thrown.
      *
      * @param ws ws object to convert
      * @return converted DTO object
      * @throws SessionInternalError if required field is missing
      */
     public static PlanItemDTO getDTO(PlanItemWS ws) {
         if (ws != null) {
             if (ws.getItemId() == null)
                 throw new SessionInternalError("PlanItemWS must have an affected item.");
 
             // affected item
             ItemDTO item = new ItemBL(ws.getItemId()).getEntity();
 
             // price model
             PriceModelDTO model = PriceModelBL.getDTO(ws.getModel());
 
             // bundled items
             PlanItemBundleDTO bundle = PlanItemBundleBL.getDTO(ws.getBundle());
 
             return new PlanItemDTO(ws, item, model, bundle);
         }
         return null;
     }
 
     /**
      * Converts a list of PlanItemWS web-service objects into PlanItemDTO objects.
      *
      * @see #getDTO(PlanItemWS)
      *
      * @param objects ws objects to convert
      * @return a list of converted DTO objects, or an empty list if ws objects list was empty.
      */
     public static List<PlanItemDTO> getDTO(List<PlanItemWS> objects) {
         List<PlanItemDTO> planItems = new ArrayList<PlanItemDTO>(objects.size());
         for (PlanItemWS ws : objects)
             planItems.add(getDTO(ws));
         return planItems;
     }
 
 
     /**
      * Convert a given PlanItemDTO into a PlanItemWS web-service object.
      * @param dto dto to convert
      * @return converted web-service object
      */
     public static PlanItemWS getWS(PlanItemDTO dto) {
         return dto != null ? new PlanItemWS(dto) : null;
     }
 
 
     /**
      * Converts a list of PlanItemDTO objects into PlanItemWS web-service objects.
      *
      * @see #getWS(PlanItemDTO)
      *
      * @param objects dto objects to convert
      * @return a list of converted WS objects, or an empty list if dto objects list was empty.
      */
     public static List<PlanItemWS> getWS(List<PlanItemDTO> objects) {
         List<PlanItemWS> planItems = new ArrayList<PlanItemWS>(objects.size());
         for (PlanItemDTO dto : objects)
             planItems.add(new PlanItemWS(dto));
         return planItems;
     }
 
     /**
      * Collects the individual plan items from all order lines that contain a plan. Used to
      * condense a list of "subscription order lines" down to the individual prices and bundled
      * items to be applied.
      *
      * @param lines order lines containing plan subscriptions
      * @return list of plan items from all plans in the order lines
      */
     public static List<PlanItemDTO> collectPlanItems(List<OrderLineDTO> lines) {
         List<PlanItemDTO> planItems = new ArrayList<PlanItemDTO>();
 
         for (OrderLineDTO line : lines) {
             if (line.getDeleted() == 0) {
                 for (PlanDTO plan : new PlanBL().getPlansBySubscriptionItem(line.getItemId())) {
                     for (PlanItemDTO planItem : plan.getPlanItems()) {
                         planItems.add(planItem);
                     }
                 }
             }
         }
 
         return planItems;
     }
 }
