 /*
     jBilling - The Enterprise Open Source Billing System
     Copyright (C) 2003-2009 Enterprise jBilling Software Ltd. and Emiliano Conde
 
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
 import com.sapienter.jbilling.server.item.db.ItemDAS;
 import com.sapienter.jbilling.server.item.db.ItemDTO;
 import com.sapienter.jbilling.server.item.db.ItemTypeDTO;
 import com.sapienter.jbilling.server.item.tasks.IPricing;
 import com.sapienter.jbilling.server.item.tasks.PricingResult;
 import com.sapienter.jbilling.server.order.Usage;
 import com.sapienter.jbilling.server.order.db.OrderDTO;
 import com.sapienter.jbilling.server.order.db.OrderLineDAS;
 import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskManager;
 import com.sapienter.jbilling.server.pricing.PriceModelBL;
 import com.sapienter.jbilling.server.pricing.db.PriceModelDTO;
 import com.sapienter.jbilling.server.pricing.db.PriceModelStrategy;
 import com.sapienter.jbilling.server.user.EntityBL;
 import com.sapienter.jbilling.server.user.UserBL;
 import com.sapienter.jbilling.server.user.db.CompanyDAS;
 import com.sapienter.jbilling.server.user.db.CompanyDTO;
 import com.sapienter.jbilling.server.util.Constants;
 import com.sapienter.jbilling.server.util.audit.EventLogger;
 import org.apache.log4j.Logger;
 
 import java.math.BigDecimal;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 public class ItemBL {
 
     private static final Logger LOG = Logger.getLogger(ItemBL.class);
 
     private ItemDAS itemDas = null;
     private ItemDTO item = null;
     private EventLogger eLogger = null;
     private String priceCurrencySymbol = null;
     private List<PricingField> pricingFields = null;
 
     public ItemBL(Integer itemId)
             throws SessionInternalError {
         try {
             init();
             set(itemId);
         } catch (Exception e) {
             throw new SessionInternalError("Setting item", ItemBL.class, e);
         }
     }
 
     public ItemBL() {
         init();
     }
 
     public ItemBL(ItemDTO item) {
         this.item = item;
         init();
     }
 
     public void set(Integer itemId) {
         item = itemDas.find(itemId);
     }
 
     private void init() {
         eLogger = EventLogger.getInstance();
         itemDas = new ItemDAS();
     }
 
     public ItemDTO getEntity() {
         return item;
     }
 
     public Integer create(ItemDTO dto, Integer languageId) {
         EntityBL entity = new EntityBL(dto.getEntityId());
         if (languageId == null) {
             languageId = entity.getEntity().getLanguageId();
         }
 
         if (dto.getHasDecimals() != null) {
             dto.setHasDecimals(dto.getHasDecimals());
         } else {
             dto.setHasDecimals(0);
         }
 
         // Backwards compatible with the old ItemDTOEx Web Service API, use the
         // transient price field as the rate for a default pricing model.
         if (dto.getPrice() != null) {
             dto.setDefaultPrice(getDefaultPrice(dto.getPrice()));
         }
 
         // default currency for new prices (if currency is not explicitly set)
        if (dto.getDefaultPrice() != null && dto.getDefaultPrice().getCurrency() != null) {
             dto.getDefaultPrice().setCurrency(entity.getEntity().getCurrency());
         }
 
         // validate all pricing attributes
         if (dto.getDefaultPrice() != null) {
             PriceModelBL.validateAttributes(dto.getDefaultPrice());
         }
 
         dto.setDeleted(0);
 
         item = itemDas.save(dto);
 
         item.setDescription(dto.getDescription(), languageId);
         updateTypes(dto);
 
         return item.getId();
     }
 
     public void update(Integer executorId, ItemDTO dto, Integer languageId)  {
         eLogger.audit(executorId, null, Constants.TABLE_ITEM, item.getId(),
                 EventLogger.MODULE_ITEM_MAINTENANCE,
                 EventLogger.ROW_UPDATED, null, null, null);
 
         item.setNumber(dto.getNumber());
         item.setPriceManual(dto.getPriceManual());
         item.setDescription(dto.getDescription(), languageId);
         item.setPercentage(dto.getPercentage());
         item.setHasDecimals(dto.getHasDecimals());
 
         updateDefaultPrice(dto);
         updateTypes(dto);
 
         itemDas.save(item);
     }
 
     /**
      * Constructs a METERED PriceModelDTO with the given rate to be used as
      * the default price for items. This type of price model matches the old
      * "$ per unit" style pricing for basic items.
      *
      * @param rate rate per unit
      * @return price model
      */
     private PriceModelDTO getDefaultPrice(BigDecimal rate) {
         PriceModelDTO model = new PriceModelDTO();
         model.setRate(rate);
         model.setType(PriceModelStrategy.METERED);
 
         return model;
     }
 
     /**
      * Updates the price of this item to that of the given ItemDTO. This method
      * handles updates to the price using both the items default price model, and
      * the transient price attribute.
      *
      * If the given dto has a price through {@link ItemDTO#getPrice()}, then the
      * default price model rate will be set to the price. Otherwise the given dto's
      * price model is used to update.
      *
      * @param dto item holding the updates to apply to this item
      */
     private void updateDefaultPrice(ItemDTO dto) {
         if (item.getDefaultPrice() == null) {
             // new default price
             if (dto.getDefaultPrice() != null) {
                 item.setDefaultPrice(dto.getDefaultPrice());
             } else if (dto.getPrice() != null) {
                 item.setDefaultPrice(getDefaultPrice(dto.getPrice()));
             }
 
         } else {
             // update existing default price
             if (dto.getDefaultPrice() != null) {
                 item.setDefaultPrice(dto.getDefaultPrice());
             } else if (dto.getPrice() != null) {
                 item.getDefaultPrice().setRate(dto.getPrice());
             }
         }
 
         // default price currency should always be the entity currency
         if (item.getDefaultPrice() != null && item.getDefaultPrice().getCurrency() == null) {
             item.getDefaultPrice().setCurrency(item.getEntity().getCurrency());
         }
     }
 
     private void updateTypes(ItemDTO dto)
             {
         // update the types relationship
         Collection types = item.getItemTypes();
         types.clear();
         ItemTypeBL typeBl = new ItemTypeBL();
         // TODO verify that all the categories belong to the same
         // order_line_type_id
         for (int f=0; f < dto.getTypes().length; f++) {
             typeBl.set(dto.getTypes()[f]);
             types.add(typeBl.getEntity());
         }
     }
     
     public void delete(Integer executorId) {
         item.setDeleted(new Integer(1));
         itemDas.flush();
         itemDas.clear();
 
         eLogger.audit(executorId, null, Constants.TABLE_ITEM, item.getId(),
                 EventLogger.MODULE_ITEM_MAINTENANCE,
                 EventLogger.ROW_DELETED, null, null, null);
     }
 
     public boolean validateDecimals( Integer hasDecimals ){
         if( hasDecimals == 0 ){
             if(new OrderLineDAS().findLinesWithDecimals(item.getId()) > 0) {
                 return false;
             }
         }
         return true;
     }
 
     /**
      * Returns the basic price for an item and currency, without including purchase quantity or
      * the users current usage in the pricing calculation.
      *
      * This method does not execute any pricing plug-ins and does not use quantity or usage
      * values for {@link PriceModelDTO#applyTo(PricingResult, BigDecimal, Usage)}
      * price calculations.
      *
      * @param item item to price
      * @param currencyId currency id of requested price
      * @return The price in the requested currency
      */
     public BigDecimal getPriceByCurrency(ItemDTO item, Integer userId, Integer currencyId)  {
         if (item.getDefaultPrice() != null) {
             // empty usage for default pricing
             Usage usage = new Usage();
             usage.setAmount(BigDecimal.ZERO);
             usage.setQuantity(BigDecimal.ZERO);
 
             // calculate default price from strategy
             PricingResult result = new PricingResult(item.getId(), userId, currencyId);
             List<PricingField> fields = Collections.emptyList();
             item.getDefaultPrice().applyTo(result, fields, BigDecimal.ONE, usage);
             return result.getPrice();
         }
         return BigDecimal.ZERO;
     }
 
 
     public BigDecimal getPrice(Integer userId, BigDecimal quantity, Integer entityId) throws SessionInternalError {
         UserBL user = new UserBL(userId);
         return getPrice(userId, user.getCurrencyId(), quantity, entityId, null);
     }
 
     public BigDecimal getPrice(Integer userId, Integer currencyId, BigDecimal quantity, Integer entityId) throws SessionInternalError {
         UserBL user = new UserBL(userId);
         return getPrice(userId, currencyId, quantity, entityId, null);
     }
 
     /**
      * Will find the right price considering the user's special prices and which
      * currencies had been entered in the prices table.
      *
      * @param userId user id
      * @param currencyId currency id
      * @param entityId entity id
      * @param order order being created or edited, maybe used for additional pricing calculations
      * @return The price in the requested currency. It always returns a price,
      * otherwise an exception for lack of pricing for an item
      */
     public BigDecimal getPrice(Integer userId, Integer currencyId, BigDecimal quantity, Integer entityId, OrderDTO order)
             throws SessionInternalError {
 
         if (currencyId == null || entityId == null) {
             throw new SessionInternalError("Can't get a price with null parameters. "
                                            + "currencyId = " + currencyId
                                            + " entityId = " + entityId);
         }
 
         CurrencyBL currencyBL;
         try {
             currencyBL = new CurrencyBL(currencyId);
             priceCurrencySymbol = currencyBL.getEntity().getSymbol();
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
 
         // default "simple" price
         BigDecimal price = getPriceByCurrency(item, userId, currencyId);
 
         // run a plug-in with external logic (rules), if available
         try {
             PluggableTaskManager<IPricing> taskManager
                     = new PluggableTaskManager<IPricing>(entityId, Constants.PLUGGABLE_TASK_ITEM_PRICING);
             IPricing myTask = taskManager.getNextClass();
 
             while(myTask != null) {
                 price = myTask.getPrice(item.getId(), quantity, userId, currencyId, pricingFields, price, order);
                 myTask = taskManager.getNextClass();
             }
         } catch (Exception e) {
             throw new SessionInternalError("Item pricing task error", ItemBL.class, e);
         }
 
         return price;
     }
 
     /**
      * Returns an ItemDTO constructed for the given language and entity, priced for the
      * given user and currency.
      *
      * @param languageId id of the users language
      * @param userId id of the user purchasing the item
      * @param entityId id of the entity
      * @param currencyId id of the currency
      * @return item dto
      * @throws SessionInternalError if an internal exception occurs processing request
      */
     public ItemDTO getDTO(Integer languageId, Integer userId, Integer entityId, Integer currencyId)
         throws SessionInternalError {
 
         return getDTO(languageId, userId, entityId, currencyId, BigDecimal.ONE, null);
     }
 
 
     /**
      * Returns an ItemDTO constructed for the given language and entity, priced for the
      * given user, currency and the amount being purchased.
      *
      * @param languageId id of the users language
      * @param userId id of the user purchasing the item
      * @param entityId id of the entity
      * @param currencyId id of the currency
      * @param quantity quantity being purchased
      * @return item dto
      * @throws SessionInternalError if an internal exception occurs processing request
      */
     public ItemDTO getDTO(Integer languageId, Integer userId, Integer entityId, Integer currencyId, BigDecimal quantity)
         throws SessionInternalError {
         return getDTO(languageId, userId, entityId, currencyId, quantity, null);
     }
 
 
     /**
      * Returns an ItemDTO constructed for the given language and entity, priced for the
      * given user, currency and the amount being purchased.
      *
      * If an order is given, then the order quantities will impact the price calculations
      * for item prices that include usage.
      *
      * @param languageId id of the users language
      * @param userId id of the user purchasing the item
      * @param entityId id of the entity
      * @param currencyId id of the currency
      * @param quantity quantity being purchased
      * @param order order that this item is to be added to. may be null if no order operation.
      * @return item dto
      * @throws SessionInternalError if an internal exception occurs processing request
      */
     public ItemDTO getDTO(Integer languageId, Integer userId, Integer entityId, Integer currencyId, BigDecimal quantity,
                           OrderDTO order) throws SessionInternalError {
 
         ItemDTO dto = new ItemDTO(
             item.getId(),
             item.getInternalNumber(),
             item.getEntity(),
             item.getDescription(languageId),
             item.getPriceManual(),
             item.getDeleted(),
             currencyId,
             null,
             item.getPercentage(),
             null, // to be set right after
             item.getHasDecimals() );
 
         dto.setDefaultPrice(item.getDefaultPrice());
 
         // calculate a true price using the pricing plug-in, pricing takes into
         // account plans, special prices and the quantity of the item being purchased.
         if (currencyId != null && dto.getPercentage() == null) {
             dto.setPrice(getPrice(userId, currencyId, quantity, entityId, order));
         }
 
         // set the types
         Integer types[] = new Integer[item.getItemTypes().size()];
         int index = 0;
         for (Iterator it = item.getItemTypes().iterator(); it.hasNext();
                 index++) {
             ItemTypeDTO type = (ItemTypeDTO) it.next();
 
             types[index] = type.getId();
 
             // it is assumed that an item belongs to categories that have
             // all the same order_line_type_id
             dto.setOrderLineTypeId(type.getOrderLineTypeId());
         }
         dto.setTypes(types);
 
         LOG.debug("Got item: " + dto.getId() + ", price: " + dto.getPrice());
 
         return dto;
     }
 
     public ItemDTO getDTO(ItemDTOEx other) {
         ItemDTO retValue = new ItemDTO();
 
         if (other.getId() != null) {
             retValue.setId(other.getId());
         }
 
         retValue.setEntity(new CompanyDAS().find(other.getEntityId()));
         retValue.setNumber(other.getNumber());
         retValue.setPercentage(other.getPercentageAsDecimal());
         retValue.setPriceManual(other.getPriceManual());
         retValue.setDeleted(other.getDeleted());
         retValue.setHasDecimals(other.getHasDecimals());
         retValue.setDescription(other.getDescription());
         retValue.setTypes(other.getTypes());
         retValue.setPromoCode(other.getPromoCode());
         retValue.setCurrencyId(other.getCurrencyId());
         retValue.setPrice(other.getPriceAsDecimal());
         retValue.setOrderLineTypeId(other.getOrderLineTypeId());
 
         // convert PriceModelWS to PriceModelDTO
         retValue.setDefaultPrice(PriceModelBL.getDTO(other.getDefaultPrice()));
 
         return retValue;
     }
 
     public ItemDTOEx getWS(ItemDTO other) {
         if (other == null) {
             other = item;
         }
 
         ItemDTOEx retValue = new ItemDTOEx();
         retValue.setId(other.getId());
 
         retValue.setEntityId(other.getEntity().getId());
         retValue.setNumber(other.getInternalNumber());
         retValue.setPercentage(other.getPercentage());
         retValue.setPriceManual(other.getPriceManual());
         retValue.setDeleted(other.getDeleted());
         retValue.setHasDecimals(other.getHasDecimals());
         retValue.setDescription(other.getDescription());
         retValue.setTypes(other.getTypes());
         retValue.setPromoCode(other.getPromoCode());
         retValue.setCurrencyId(other.getCurrencyId());
         retValue.setPrice(other.getPrice());
         retValue.setOrderLineTypeId(other.getOrderLineTypeId());
 
         // convert PriceModelDTO to PriceModelWS
         retValue.setDefaultPrice(PriceModelBL.getWS(other.getDefaultPrice()));
 
         return retValue;
     }
 
     /**
      * @return
      */
     public String getPriceCurrencySymbol() {
         return priceCurrencySymbol;
     }
 
     /**
      * Returns all items for the given entity.
      * @param entityId
      * The id of the entity.
      * @return an array of all items
      */
     public ItemDTOEx[] getAllItems(Integer entityId) {
         EntityBL entityBL = new EntityBL(entityId);
         CompanyDTO entity = entityBL.getEntity();
         Collection itemEntities = entity.getItems();
         ItemDTOEx[] items = new ItemDTOEx[itemEntities.size()];
 
         // iterate through returned item entities, converting them into a DTO
         int index = 0;
         for (ItemDTO item: entity.getItems()) {
             set(item.getId());
             items[index++] = getWS(getDTO(entity.getLanguageId(),
                     null, entityId, entity.getCurrencyId()));
         }
 
         return items;
     }
 
     /**
      * Returns all items for the given item type (category) id. If no results
      * are found an empty array is returned.
      *
      * @see ItemDAS#findAllByItemType(Integer)
      *
      * @param itemTypeId item type (category) id
      * @return array of found items, empty if none found
      */
     public ItemDTOEx[] getAllItemsByType(Integer itemTypeId) {
         List<ItemDTO> results = new ItemDAS().findAllByItemType(itemTypeId);
         ItemDTOEx[] items = new ItemDTOEx[results.size()];
 
         int index = 0;
         for (ItemDTO item : results)
             items[index++] = getWS(item);
 
         return items;
     }
 
     public void setPricingFields(List<PricingField> fields) {
         pricingFields = fields;
     }
 }
