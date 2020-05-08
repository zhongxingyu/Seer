 package com.pearson.ed.lp.aggregator;
 
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.integration.annotation.Aggregator;
 
 import com.pearson.ed.lp.message.LicensedProductDataCollection;
 import com.pearson.ed.lp.message.OrderLineItemsResponse;
 import com.pearson.ed.lp.message.OrganizationDisplayNamesResponse;
 import com.pearson.ed.lp.message.ProductData;
 import com.pearson.ed.lp.message.ProductEntityIdsResponse;
 import com.pearson.ed.lplc.model.OrganizationLPMapping;
 import com.pearson.rws.licensedproduct.doc.v2.GetLicensedProductResponseElement;
 import com.pearson.rws.licensedproduct.doc.v2.LicensedProduct;
 
 /**
  * Final response message aggregator for the GetLicensedProductV2 service.
  * 
  * @author ULLOYNI
  *
  */
 public class LicensedProductsResponseAggregator {
 
 	private static final Logger LOGGER = LoggerFactory.getLogger(LicensedProductsResponseAggregator.class);
 
 	/**
 	 * Aggregate a list of message objects into a single {@link GetLicensedProductResponseElement}.
 	 * Expected response messages in provided list:
 	 * <ul>
 	 * <li>{@link ProductEntityIdsResponse}</li>
 	 * <li>{@link OrderLineItemsResponse}</li>
 	 * <li>{@link LicensedProductDataCollection}</li>
 	 * <li>{@link OrganizationDisplayNamesResponse}</li>
 	 * </ul>
 	 * 
 	 * @param responseMessages collection of messages to aggregate
 	 * @return {@link GetLicensedProductResponseElement} instance
 	 */
 	@Aggregator
 	public GetLicensedProductResponseElement aggregateResponse(List<Object> responseMessages) {
 		if (LOGGER.isDebugEnabled()) {
 			LOGGER.debug(String.format("Received response messages to aggregate, message count: %d",
 					responseMessages.size()));
 			LOGGER.debug(responseMessages.toString());
 		}
 
 		ProductEntityIdsResponse productsData = null;
 		OrderLineItemsResponse orderData = null;
 		LicensedProductDataCollection licensePoolData = null;
 		OrganizationDisplayNamesResponse organizationData = null;
 
 		for (Object responseMessage : responseMessages) {
 			if (responseMessage instanceof ProductEntityIdsResponse) {
 				productsData = (ProductEntityIdsResponse) responseMessage;
 			} else if (responseMessage instanceof OrderLineItemsResponse) {
 				orderData = (OrderLineItemsResponse) responseMessage;
 			} else if (responseMessage instanceof LicensedProductDataCollection) {
 				licensePoolData = (LicensedProductDataCollection) responseMessage;
 				organizationData = licensePoolData.getOrganizationDisplayNames();
 			}
 		}
 
 		if(productsData == null) {
 			LOGGER.error("Required ProductEntityIdsResponse message not received " +
 					"for final response generation!");
 			throw new NullPointerException("Required ProductEntityIdsResponse message not received " +
 					"for final response generation!");
 		}
 
 		if(orderData == null) {
 			LOGGER.error("Required OrderLineItemsResponse message not received " +
 					"for final response generation!");
 			throw new NullPointerException("Required OrderLineItemsResponse message not received " +
 					"for final response generation!");
 		}
 
 		if(licensePoolData == null) {
 			LOGGER.error("Required LicensedProductDataCollection message not received " +
 					"for final response generation!");
 			throw new NullPointerException("Required LicensedProductDataCollection message not received " +
 					"for final response generation!");
 		}
 
 		GetLicensedProductResponseElement getLicensedProductResponse = new GetLicensedProductResponseElement();
 		List<LicensedProduct> licensedProducts = getLicensedProductResponse.getLicensedProduct();
 
 		for (OrganizationLPMapping licensePool : licensePoolData.getLicensePools().getLicensePools()) {
 			LicensedProduct licensedProduct = new LicensedProduct();
 			licensedProducts.add(licensedProduct);
 
 			String organizationId = licensePool.getLicensepoolMapping().getOrg_id();
 			Long productEntityId = Long.valueOf(licensePool.getLicensepoolMapping().getProduct_id());
 			ProductData productData = productsData.getProductDataByEntityIds().get(productEntityId);
 			String firstOrderLineItemId = licensePool.getLicensepoolMapping().getOrderLineItems().iterator().next()
 					.getOrderLineItemId();
 
 			licensedProduct.setOrganizationId(organizationId);
 			licensedProduct.setLicensePoolId(licensePool.getLicensepoolMapping().getLicensepoolId());
 			licensedProduct.setLicensedOrganizationId(organizationId);
 			licensedProduct.setLicensedOrganizationDisplayName(organizationData.getOrganizationDisplayNamesByIds().get(
 					organizationId));
 			licensedProduct.setLicensePoolType(licensePool.getLicensepoolMapping().getType());
			licensedProduct.setLicensePoolStatus(licensePool.getLicensepoolMapping().getStatus().trim());
 			licensedProduct.setDenyNewSubscription(licensePool.getLicensepoolMapping().getDenyManualSubscription());
 
 			try {
 				licensedProduct.setStartDate(convertToXMLGregorianCalendar(licensePool.getLicensepoolMapping()
 						.getStart_date()));
 				licensedProduct.setEndDate(convertToXMLGregorianCalendar(licensePool.getLicensepoolMapping()
 						.getEnd_date()));
 			} catch (DatatypeConfigurationException e) {
 				LOGGER.error(e.getMessage());
 			}
 
 			licensedProduct.setQuantity(licensePool.getLicensepoolMapping().getQuantity());
 			licensedProduct.setUsedLicenses(licensePool.getUsed_quantity());
 			licensedProduct.setProductId(productData.getProductId());
 			licensedProduct.setProductDisplayName(productData.getDisplayName());
 
 			// optional data
 			if (productData.getShortDescription() != null) {
 				licensedProduct.setProductShortDescription(productData.getShortDescription());
 			}
 			if (productData.getLongDescription() != null) {
 				licensedProduct.setProductLongDescription(productData.getLongDescription());
 			}
 			if (productData.getCgProgram() != null) {
 				licensedProduct.setCGProgram(productData.getCgProgram());
 			}
 			if (!productData.getGradeLevels().isEmpty()) {
 				licensedProduct.getGradeLevel().addAll(productData.getGradeLevels());
 			}
 			if (orderData.getOrderedISBNsByOrderLineItemIds().containsKey(firstOrderLineItemId)) {
 				licensedProduct.setOrderedISBN(orderData.getOrderedISBNsByOrderLineItemIds().get(firstOrderLineItemId));
 			}
 		}
 
 		return getLicensedProductResponse;
 	}
 
 	private XMLGregorianCalendar convertToXMLGregorianCalendar(Date date) throws DatatypeConfigurationException {
 		GregorianCalendar gregorianCalendar = new GregorianCalendar();
 		gregorianCalendar.setTime(date);
 		return DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
 	}
 
 }
