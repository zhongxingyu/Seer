 /*
  * Copyright (c) 2009. Orange Leap Inc. Active Constituent
  * Relationship Management Platform.
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
 
 package com.orangeleap.tangerine.test.controller;
 
 import com.orangeleap.tangerine.controller.TangerineForm;
 import com.orangeleap.tangerine.controller.gift.AbstractPaymentFormController;
 import com.orangeleap.tangerine.domain.AbstractEntity;
 import com.orangeleap.tangerine.domain.Constituent;
 import com.orangeleap.tangerine.domain.paymentInfo.Gift;
 import com.orangeleap.tangerine.test.BaseTest;
 import com.orangeleap.tangerine.test.dataprovider.AbstractPaymentDataProvider;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 import javax.servlet.http.HttpServletRequest;
 import java.math.BigDecimal;
 import java.text.SimpleDateFormat;
 import java.util.Map;
 
 /**
  * User: alexlo
  * Date: Jul 16, 2009
  * Time: 10:14:35 AM
  */
 public class AbstractPaymentFormControllerTest extends BaseTest {
 
 	@Test(dataProvider = "setupAbstractPaymentForm", dataProviderClass = AbstractPaymentDataProvider.class)
 	public void testConvertFormToDomain(HttpServletRequest request, TangerineForm form, Map<String, Object> paramMap) throws Exception {
 
 		MockPaymentFormController controller = new MockPaymentFormController();
 		controller.convertFormToDomain(request, form, paramMap);
 
 		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
 
 		Gift gift = (Gift) form.getDomainObject();
 		Assert.assertEquals(gift.getAmount(), new BigDecimal("45.99"), "Amount = " + gift.getAmount());
 		Assert.assertEquals(new Long(gift.getId()), new Long(100L), "Id = " + gift.getId());
 		Assert.assertEquals(gift.getPaymentType(), "Check", "PaymentType = " + gift.getPaymentType());
 		Assert.assertEquals(gift.getCheckNumber(), "111", "CheckNumber = " + gift.getCheckNumber());
 		Assert.assertEquals(gift.getComments(), "My gift to you", "Comments = " + gift.getComments());
 		Assert.assertEquals(gift.getConstituent().getFirstName(), "firstname", "FirstName = " + gift.getConstituent().getFirstName());
 		Assert.assertEquals(gift.getConstituent().getLastName(), "lastname", "LastName = " + gift.getConstituent().getLastName());
 		Assert.assertEquals(gift.getPaymentType(), "Check", "PaymentType = " + gift.getPaymentType());
 		Assert.assertNull(gift.getPaymentSource(), "PaymentSource != null");
 		Assert.assertEquals(format.format(gift.getPostedDate()), "01/01/1999", "PostedDate = " + gift.getPostedDate());
 		Assert.assertTrue(gift.isPosted(), "Posted = false");
 		Assert.assertFalse(gift.isDeductible(), "Deductible = true");
 		Assert.assertEquals(gift.getCustomFieldValue("reference"), "Joe Blow", "customFieldMap[reference].value = " + gift.getCustomFieldValue("reference"));
 		Assert.assertEquals(gift.getCustomFieldValue("daddyo"), "787", "customFieldMap[daddyo].value = " + gift.getCustomFieldValue("daddyo"));
 		Assert.assertEquals(gift.getCustomFieldValue("momma"), "Yo Mama", "customFieldMap[momma].value = " + gift.getCustomFieldValue("momma"));
 
 		Assert.assertNotNull(gift.getDistributionLines(), "distributionLines were null");
 		Assert.assertEquals(gift.getDistributionLines().size(), 2, "distributionLines size = " + gift.getDistributionLines().size());
 		Assert.assertEquals(gift.getDistributionLines().get(0).getAmount(), new BigDecimal("45"), "DistroLine[0].amount = " + gift.getDistributionLines().get(0).getAmount());
 		Assert.assertEquals(gift.getDistributionLines().get(0).getPercentage(), new BigDecimal("98"), "DistroLine[0].percentage = " + gift.getDistributionLines().get(0).getPercentage());
 		Assert.assertEquals(gift.getDistributionLines().get(0).getProjectCode(), "Trial", "DistroLine[0].projectCode = " + gift.getDistributionLines().get(0).getProjectCode());
 		Assert.assertEquals(gift.getDistributionLines().get(0).getCustomFieldValue("tribute"), "inMemoryOf", "DistroLine[0].customFieldMap[tribute].value = " + gift.getDistributionLines().get(0).getCustomFieldValue("tribute"));
 
 		Assert.assertEquals(gift.getDistributionLines().get(1).getAmount(), new BigDecimal(".99"), "DistroLine[1].amount = " + gift.getDistributionLines().get(1).getAmount());
 		Assert.assertEquals(gift.getDistributionLines().get(1).getPercentage(), new BigDecimal(2), "DistroLine[1].percentage = " + gift.getDistributionLines().get(1).getPercentage());
 		Assert.assertEquals(gift.getDistributionLines().get(1).getCustomFieldValue("onBehalfOf"), "Fooey", "DistroLine[1].customFieldMap[onBehalfOf].value = " + gift.getDistributionLines().get(1).getCustomFieldValue("onBehalfOf"));
 		Assert.assertEquals(gift.getDistributionLines().get(1).getCustomFieldValue("taxDeductible"), "true", "DistroLine[1].customFieldMap[taxDeductible].value = " + gift.getDistributionLines().get(1).getCustomFieldValue("taxDeductible"));
 
 		/* Check the form bean to see if the re-indexing was correct */
 		Assert.assertEquals(form.getFieldValueFromUnescapedFieldName("distributionLines[0].amount"), "45", "form.distributionLines[0].amount = " + form.getFieldValueFromUnescapedFieldName("distributionLines[0].amount"));
 		Assert.assertEquals(form.getFieldValueFromUnescapedFieldName("distributionLines[0].percentage"), "98", "form.distributionLines[0].percentage = " + form.getFieldValueFromUnescapedFieldName("distributionLines[0].percentage"));
 		Assert.assertEquals(form.getFieldValueFromUnescapedFieldName("distributionLines[0].projectCode"), "Trial", "form.distributionLines[0].projectCode = " + form.getFieldValueFromUnescapedFieldName("distributionLines[0].projectCode"));
 		Assert.assertEquals(form.getFieldValueFromUnescapedFieldName("distributionLines[0].customFieldMap[tribute]"), "inMemoryOf", "form.distributionLines[0].customFieldMap[tribute] = " + form.getFieldValueFromUnescapedFieldName("distributionLines[0].customFieldMap[tribute]"));
 		Assert.assertEquals(form.getFieldValueFromUnescapedFieldName("distributionLines[1].amount"), ".99", "form.distributionLines[1].amount = " + form.getFieldValueFromUnescapedFieldName("distributionLines[1].amount"));
 		Assert.assertEquals(form.getFieldValueFromUnescapedFieldName("distributionLines[1].percentage"), "2", "form.distributionLines[1].percentage = " + form.getFieldValueFromUnescapedFieldName("distributionLines[1].percentage"));
 		Assert.assertEquals(form.getFieldValueFromUnescapedFieldName("distributionLines[1].customFieldMap[onBehalfOf]"), "Fooey", "form.distributionLines[1].customFieldMap[onBehalfOf] = " + form.getFieldValueFromUnescapedFieldName("distributionLines[1].customFieldMap[onBehalfOf]"));
 		Assert.assertEquals(form.getFieldValueFromUnescapedFieldName("distributionLines[1].customFieldMap[taxDeductible]"), "true", "form.distributionLines[1].customFieldMap[taxDeductible] = " + form.getFieldValueFromUnescapedFieldName("distributionLines[1].customFieldMap[taxDeductible]"));
 	}
 
 	class MockPaymentFormController extends AbstractPaymentFormController {
 
 		@Override
 		protected AbstractEntity findEntity(HttpServletRequest request) {
 			return null;
 		}
 
 		@Override
 		public void convertFormToDomain(HttpServletRequest request, TangerineForm form, Map<String, Object> paramMap) throws Exception {
 			super.convertFormToDomain(request, form, paramMap);
 		}
 
 		@Override
 		protected Constituent getConstituent(HttpServletRequest request) {
 			return new Constituent();
 		}
 	}
 }
