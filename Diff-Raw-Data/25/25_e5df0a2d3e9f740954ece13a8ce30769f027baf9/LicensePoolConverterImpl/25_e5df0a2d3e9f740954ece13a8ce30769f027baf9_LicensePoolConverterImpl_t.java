 package com.pearson.ed.lplc.services.converter.impl;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 import com.pearson.ed.lplc.common.LPLCConstants;
 import com.pearson.ed.lplc.dto.LicensePoolDTO;
 import com.pearson.ed.lplc.dto.UpdateLicensePoolDTO;
 import com.pearson.ed.lplc.exception.ComponentValidationException;
 import com.pearson.ed.lplc.model.LicensePoolMapping;
 import com.pearson.ed.lplc.model.OrderLineItemLPMapping;
 import com.pearson.ed.lplc.model.OrganizationLPMapping;
 import com.pearson.ed.lplc.model.common.LPLCBaseEntity;
 import com.pearson.ed.lplc.services.converter.api.LicensePoolConverter;
 import com.pearson.ed.lplc.ws.schema.CreateLicensePool;
 import com.pearson.ed.lplc.ws.schema.LicensePool;
 import com.pearson.ed.lplc.ws.schema.LicensePoolByOrganizationId;
 import com.pearson.ed.lplc.ws.schema.LicensepoolsByOrganizationId;
 import com.pearson.ed.lplc.ws.schema.StatusType;
 import com.pearson.ed.lplc.ws.schema.UpdateLicensePool;
 
 public class LicensePoolConverterImpl implements LicensePoolConverter {
 	private static final Logger logger = Logger
 			.getLogger(LicensePoolConverterImpl.class);
 
 	/**
 	 * Convert to LicensePoolDTO.
 	 * 
 	 * @param licensepool
 	 * @return LicensePoolDTO.
 	 */
 	public LicensePoolDTO convertLicensePoolToLicensePoolDTO(
 			LicensePool licensepool) {
 		LicensePoolDTO licensepoolDTO = new LicensePoolDTO();
 		licensepoolDTO.setLicensepoolId(licensepool.getLicenseId());
 		licensepoolDTO.setType(licensepool.getType());
 		licensepoolDTO.setDenyManualSubscription(licensepool
 				.getDenyNewSubscription());
 		licensepoolDTO.setStartDate(licensepool.getStartDate()
 				.toGregorianCalendar().getTime());
 		licensepoolDTO.setEndDate(licensepool.getEndDate()
 				.toGregorianCalendar().getTime());
 		licensepoolDTO.setQuantity(licensepool.getQuantity());
 		licensepoolDTO.setUsedLicenses(licensepool.getUsedLicenses());
 		licensepoolDTO.setOrganizationId(licensepool.getOrgnizationId());
 		licensepoolDTO.setProductId(getProducts(licensepool));
 		licensepoolDTO.setOrderLineItemId(licensepool.getOrderLineItemId());
 		licensepoolDTO.setSourceSystem(licensepool.getSourceSystem());
 		licensepoolDTO.setCreatedBy(licensepool.getCreatedBy());
 		licensepoolDTO.setCreatedDate(licensepool.getCreatedDate()
 				.toGregorianCalendar().getTime());
 		licensepoolDTO.setLastUpdatedBy(licensepool.getLastUpdatedBy());
 		licensepoolDTO.setLastUpdatedDate(licensepool.getLastUpdatedDate()
 				.toGregorianCalendar().getTime());
 		return licensepoolDTO;
 
 	}
 
 	private List<String> getProducts(LicensePool licensepool) {
 		List<String> newProductIds = new ArrayList<String>();
 		List<String> productIds = licensepool.getProductId();
 		Iterator<String> iterator = productIds.iterator();
 		while (iterator.hasNext())
 			newProductIds.add(iterator.next());
 		return newProductIds;
 	}
 	
 
 	/**
 	 * Converts a LicensePoolMapping to a LicensePool.
 	 * 
 	 * @param licensepoolMapping
 	 *            the LicensePoolMapping object.
 	 * 
 	 * @return the LicensePool object.
 	 */
 	public LicensePoolDTO convertLicensePoolFromLicensePoolMapping(
 			LicensePoolMapping licensepoolMapping) {
 		LicensePoolDTO licensepool = new LicensePoolDTO();
 		licensepool.setLicensepoolId(licensepoolMapping.getLicensepoolId());
 		licensepool.setType(licensepoolMapping.getType());
 		licensepool.setDenyManualSubscription(licensepoolMapping
 				.getDenyManualSubscription());
 		licensepool.setQuantity(licensepoolMapping.getQuantity());
 		licensepool.setStartDate(licensepoolMapping.getStart_date());
 		licensepool.setEndDate(licensepoolMapping.getEnd_date());
 		licensepool.setOrganizationId(licensepoolMapping.getOrg_id());
 		// List<String> productList = new ArrayList<String>();
 		// Set<ProductLPMapping> products = licensepoolMapping.getProducts();
 		// Iterator<ProductLPMapping> iterator = products.iterator();
 		// while (iterator.hasNext())
 		// productList.add(iterator.next().getProduct_id());
 		// licensepool.setProductId(productList);
 		licensepool.setSourceSystem(licensepoolMapping.getSource_system());
 		licensepool.setCreatedBy(licensepoolMapping.getCreatedBy());
 		licensepool.setCreatedDate(licensepoolMapping.getCreatedDate());
 		licensepool.setLastUpdatedBy(licensepoolMapping.getLastUpdatedBy());
 		licensepool.setLastUpdatedDate(licensepoolMapping.getLastUpdatedDate());
 
 		return licensepool;
 	}
 
 	/**
 	 * Converts a LicensePool to a LicensePoolMappings.
 	 * 
 	 * @param user
 	 *            the LicensePool object.
 	 * @param userMapping
 	 *            the LicensePoolMapping object
 	 * 
 	 * @return the LicensePoolMapping object.
 	 */
 	public LicensePoolMapping convertLicensePoolToLicensePoolMapping(
 			LicensePoolDTO licensepool, LicensePoolMapping licensepoolMapping) {
 		// validate and set mode
 		if (StringUtils.isEmpty(licensepool.getMode())) {
 			if (StringUtils.isEmpty(licensepool.getLicensepoolId())) {
 				licensepool.setMode(LPLCConstants.CREATE_MODE);
 			} else {
 				licensepool.setMode(LPLCConstants.UPDATE_MODE);
 			}
 		}
 
 		return buildLicensePoolMapping(licensepool, licensepoolMapping);
 	}
 
 	private LicensePoolMapping buildLicensePoolMapping(
 			LicensePoolDTO licensepool, LicensePoolMapping licensepoolMapping) {
 		String mode = licensepool.getMode();
 		if (LPLCConstants.CREATE_MODE.equals(mode)) {
 			licensepoolMapping = new LicensePoolMapping();
 		}
 
 		String type = licensepool.getType();
 		if (StringUtils.isNotBlank(type)) {
 			licensepoolMapping.setType(type);
 		}
 		licensepoolMapping.setDenyManualSubscription(licensepool
 				.getDenyManualSubscription());
 
 		licensepoolMapping.setStart_date(licensepool.getStartDate());
 		licensepoolMapping.setEnd_date(licensepool.getEndDate());
 		licensepoolMapping.setQuantity(licensepool.getQuantity());
 		licensepoolMapping.setSource_system(licensepool.getSourceSystem());
 		licensepoolMapping.setOrg_id(licensepool.getOrganizationId());
 		licensepoolMapping.setProducts(getProducts(licensepool));
 		String createdBy = licensepool.getCreatedBy();
 		if (StringUtils.isNotBlank(createdBy))
 			licensepoolMapping.setCreatedBy(createdBy);
 
 		if (LPLCConstants.CREATE_MODE.equals(mode)) {
 			setCreatedValues(licensepoolMapping, licensepool);
 		}
 		setModifiedValues(licensepoolMapping, licensepool);
		if (StringUtils.isNotEmpty(licensepool.getOrderLineItemId()))
			buildOrderLineItemMapping(licensepool, licensepoolMapping, mode,
 				createdBy);
 
 		return licensepoolMapping;
 	}
 
 	private void buildOrderLineItemMapping(LicensePoolDTO licensepool,
 			LicensePoolMapping licensepoolMapping, String mode, String createdBy) {
 		OrderLineItemLPMapping orderLineItem = new OrderLineItemLPMapping();
		orderLineItem.setOrderLineItemId(licensepool.getOrderLineItemId());
		orderLineItem.setLicensepoolMapping(licensepoolMapping);
 		if (StringUtils.isNotBlank(createdBy))
 			orderLineItem.setCreatedBy(createdBy);
 
 		if (LPLCConstants.CREATE_MODE.equals(mode)) {
 			setCreatedValues(orderLineItem, licensepool);
 		}
 		setModifiedValues(orderLineItem, licensepool);
 		licensepoolMapping.getOrderLineItems().add(orderLineItem);
 	}
 
 	private List<String> getProducts(LicensePoolDTO licensepool) {
 		List<String> productList = new ArrayList<String>();
 		List<String> productIds = licensepool.getProductId();
 		for (String product : productIds) {
 			productList.add(product);
 		}
 		return productList;
 	}
 
 	private void setModifiedValues(LPLCBaseEntity lplcBaseEntity,
 			LicensePoolDTO licensepool) {
 		String lastUpdatedBy = licensepool.getLastUpdatedBy();
 		if (StringUtils.isEmpty(lastUpdatedBy)) {
 			lastUpdatedBy = LPLCConstants.DEFAULT_USER;
 		}
 
 		Date lastUpdatedDate = licensepool.getLastUpdatedDate();
 		if (lastUpdatedDate == null) {
 			lastUpdatedDate = LPLCConstants.DEFAULT_DATE;
 		}
 		lplcBaseEntity.setLastUpdatedBy(lastUpdatedBy);
 		lplcBaseEntity.setLastUpdatedDate(lastUpdatedDate);
 	}
 
 	private void setCreatedValues(LPLCBaseEntity lplcBaseEntity,
 			LicensePoolDTO licensepool) {
 		String createdBy = lplcBaseEntity.getCreatedBy();
 		if (StringUtils.isEmpty(createdBy)) {
 			createdBy = LPLCConstants.DEFAULT_USER;
 		}
 
 		Date createdDate = lplcBaseEntity.getCreatedDate();
 		if (createdDate == null) {
 			createdDate = LPLCConstants.DEFAULT_DATE;
 		}
 		lplcBaseEntity.setCreatedBy(createdBy);
 		lplcBaseEntity.setCreatedDate(createdDate);
 	}
 
 	public LicensePoolDTO covertCreateRequestToLicensePoolDTO(
 			CreateLicensePool createLicensePoolSchemaObj) {
 		LicensePoolDTO licensepoolDTO = new LicensePoolDTO();
 		licensepoolDTO.setType(createLicensePoolSchemaObj.getType());
		licensepoolDTO.setDenyManualSubscription((createLicensePoolSchemaObj
				.getDenyNewSubscription()==null)?0:createLicensePoolSchemaObj
						.getDenyNewSubscription().intValue());
 		licensepoolDTO.setStartDate(createLicensePoolSchemaObj.getStartDate()
 				.toGregorianCalendar().getTime());
 		licensepoolDTO.setEndDate(createLicensePoolSchemaObj.getEndDate()
 				.toGregorianCalendar().getTime());
 		licensepoolDTO.setQuantity(createLicensePoolSchemaObj.getQuantity());
 		licensepoolDTO.setOrganizationId(createLicensePoolSchemaObj
 				.getOrgnizationId());
 		licensepoolDTO.setProductId(getProducts(createLicensePoolSchemaObj));
		licensepoolDTO.setOrderLineItemId((createLicensePoolSchemaObj.getOrderLineItemId()==null)?null:createLicensePoolSchemaObj.getOrderLineItemId());
 		licensepoolDTO.setSourceSystem(createLicensePoolSchemaObj
 				.getSourceSystem());
 		licensepoolDTO.setCreatedBy(createLicensePoolSchemaObj.getCreatedBy());
 		return licensepoolDTO;
 	}
 
 	private List<String> getProducts(CreateLicensePool licensepool) {
 		List<String> newProductIds = new ArrayList<String>();
 		List<String> productIds = licensepool.getProductId();
 		if (productIds != null) {
 			Iterator<String> iterator = productIds.iterator();
 			while (iterator.hasNext())
 				newProductIds.add(iterator.next());
 		}
 		return newProductIds;
 	}
 
 	/**
 	 * Converts updateLicensepoolSchema object to update licensepooldto.
 	 * 
 	 * @param licensepool
 	 * @return update licensepool DTO.
 	 */
 	public UpdateLicensePoolDTO covertupdateRequestToUpdateLicensePoolDTO(
 			UpdateLicensePool licensepool) {
 		UpdateLicensePoolDTO updateDTO = new UpdateLicensePoolDTO();
 		updateDTO.setLicensepoolId(licensepool.getLicenseId());
 		if (licensepool.getStartDate() != null)
 			updateDTO.setStartDate(licensepool.getStartDate()
 					.toGregorianCalendar().getTime());
 		if (licensepool.getEndDate() != null)
 			updateDTO.setEndDate(licensepool.getEndDate().toGregorianCalendar()
 					.getTime());
 		if (licensepool.getQuantity() != null)
 			updateDTO.setQuantity(licensepool.getQuantity());
 		if (licensepool.getOrderLineItemId()!=null)
 			updateDTO.setOrderLineItem(licensepool.getOrderLineItemId());
 		return updateDTO;
 
 	}
 	/**
 	 * Convert mapping object to DTO for update licensepool.
 	 * @param updateLicensepool UpdateLicensePoolDTO.
 	 * @param licensepool mapping object.
 	 */
 	public void buildLicensepoolMappingFromUpdateLicensepoolDTO(
 			UpdateLicensePoolDTO updateLicensepool,
 			LicensePoolMapping licensepool) {
 		if (updateLicensepool.getStartDate() != null)
 	    	licensepool.setStart_date(updateLicensepool.getStartDate());
 	    if (updateLicensepool.getEndDate() != null)
 	    	licensepool.setEnd_date(updateLicensepool.getEndDate());
 	    if (updateLicensepool.getQuantity()!= 0)
 	    	licensepool.setQuantity(updateLicensepool.getQuantity());
 	    if (licensepool.getStart_date().after(licensepool.getEnd_date()))
 			throw new ComponentValidationException("Start Date can not be greater than End Date");
 	    if (updateLicensepool.getOrderLineItem()!= null){
 	    	OrderLineItemLPMapping orderLineItem = new OrderLineItemLPMapping();
 	    	orderLineItem.setLicensepoolMapping(licensepool);
 	    	orderLineItem.setOrderLineItemId(updateLicensepool.getOrderLineItem());
 	    	orderLineItem.setCreatedBy(licensepool.getCreatedBy());
 	    	orderLineItem.setCreatedDate(LPLCConstants.DEFAULT_DATE);
 	    	orderLineItem.setLastUpdatedBy(licensepool.getCreatedBy());
 	    	orderLineItem.setLastUpdatedDate(LPLCConstants.DEFAULT_DATE);
 	        licensepool.getOrderLineItems().add(orderLineItem);
 	    }
 	}
 	/**
 	 * This will convert Mapping object to Schema object for GetLicensepoolsByOrganizationId.
 	 * 
 	 * @param licenses list of licensepool mappings.
 	 * @return LicensepoolsByOrganizationId LicensepoolsByOrganizationId.
 	 */
 	public LicensepoolsByOrganizationId convertForGetFromLPMappingToSchema(
 			List<OrganizationLPMapping> licenses)  {
 		LicensepoolsByOrganizationId schemaList = new LicensepoolsByOrganizationId();
 		if (licenses == null)
 			return schemaList;
 		Iterator<OrganizationLPMapping> iterator = licenses.iterator();
 		LicensePoolByOrganizationId licensepoolSchemaObj;
 		while (iterator.hasNext()){
 			licensepoolSchemaObj = new LicensePoolByOrganizationId();
 			OrganizationLPMapping orgLPMapping = iterator.next();
 			if (orgLPMapping == null)
 				continue;
 			licensepoolSchemaObj.getProductId().add(orgLPMapping.getLicensepoolMapping().getProducts().get(0).toString());
 			licensepoolSchemaObj.setLicenseId(orgLPMapping.getLicensepoolMapping().getLicensepoolId());
 			licensepoolSchemaObj.setRootOrganizationId(orgLPMapping.getLicensepoolMapping().getOrg_id());
 			licensepoolSchemaObj.setType(orgLPMapping.getLicensepoolMapping().getType());
 			try {
 				licensepoolSchemaObj.setStartDate(convertToXMLGregorianCalendar(orgLPMapping.getLicensepoolMapping().getStart_date()));
 				licensepoolSchemaObj.setEndDate(convertToXMLGregorianCalendar(orgLPMapping.getLicensepoolMapping().getEnd_date()));
 			} catch (DatatypeConfigurationException e) {
 				logger.log(Level.ERROR, e.getMessage());
 			}	
 			licensepoolSchemaObj.setQuantity(orgLPMapping.getLicensepoolMapping().getQuantity());
 			licensepoolSchemaObj.setUsedLicenses(orgLPMapping.getUsed_quantity());
 			licensepoolSchemaObj.setDenyNewSubscription(orgLPMapping.getDenyManualSubscription());
 			licensepoolSchemaObj.setStatus(getLicenseStatus(orgLPMapping.getLicensepoolMapping().getStart_date(),orgLPMapping.getLicensepoolMapping().getEnd_date()));
 			schemaList.getLicensePoolByOrganizationId().add(licensepoolSchemaObj);
 		}
 		return schemaList;
 	}
	
	
 
 	private XMLGregorianCalendar convertToXMLGregorianCalendar(	Date date)	throws DatatypeConfigurationException {
 		GregorianCalendar gregorianCalendar = new GregorianCalendar();
 		gregorianCalendar.setTime(date);
 		return DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
 	}
 	/*
 	 * This function isolate the logic to figure out license status based on
 	 * current date, start date and end date.
 	 */
 	private StatusType getLicenseStatus(Date startDate, Date endDate) {
 		Date currentDate = new Date();
 		
 		if ((currentDate.compareTo(startDate) > 0)
 				&& (currentDate.compareTo(endDate) < 0))
 			return StatusType.A;
 		else if (currentDate.compareTo(endDate) > 0)
 			return StatusType.E;
 		else if (currentDate.compareTo(startDate) < 0)
 			return StatusType.P;
 		else return null;
 	}
 
 }
