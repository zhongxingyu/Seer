 /*******************************************************************************
  * Copyright (c) 2014 Formal Mind GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Ingo Weigelt - initial API and implementation
  *     Michael Jastram - adding SUPPORTED_OPERATIONS
  ******************************************************************************/
 package org.eclipse.rmf.reqif10.search.test;
 
 import static org.junit.Assert.fail;
 
 import java.util.GregorianCalendar;
 import java.util.Set;
 import java.util.TimeZone;
 
 import org.eclipse.rmf.reqif10.AttributeDefinitionDate;
 import org.eclipse.rmf.reqif10.AttributeValueDate;
 import org.eclipse.rmf.reqif10.DatatypeDefinitionDate;
 import org.eclipse.rmf.reqif10.ReqIF10Factory;
 import org.eclipse.rmf.reqif10.SpecObject;
 import org.eclipse.rmf.reqif10.search.filter.DateFilter;
 import org.eclipse.rmf.reqif10.search.filter.DateFilter.InternalAttribute;
 import org.eclipse.rmf.reqif10.search.filter.IFilter;
 import org.eclipse.rmf.reqif10.search.filter.IFilter.Operator;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 
 public class DateFilterTest extends AbstractFilterTest{
 
 	SpecObject specObject;
 	AttributeDefinitionDate attributeDefinition; 
 
 	@Rule public ExpectedException thrown= ExpectedException.none();
 	
 	@Override	
 	public void createFixture(Object value){
 		GregorianCalendar theValue = (GregorianCalendar) value;
 		
 		attributeDefinition = ReqIF10Factory.eINSTANCE.createAttributeDefinitionDate();
 		attributeDefinition.setIdentifier("AD_ID0");
 		DatatypeDefinitionDate definition = ReqIF10Factory.eINSTANCE.createDatatypeDefinitionDate();
 		definition.setIdentifier("DD_ID0");
 		attributeDefinition.setType(definition);
 		AttributeValueDate attributeValue = ReqIF10Factory.eINSTANCE.createAttributeValueDate();
 		attributeValue.setDefinition(attributeDefinition);
 		attributeValue.setTheValue(theValue);
 		SpecObject specObject = ReqIF10Factory.eINSTANCE.createSpecObject();
 		specObject.getValues().add(attributeValue);
 		specObject.setLastChange(new GregorianCalendar(2014, 11, 03));
 		
 		createSpecObjectType(specObject, attributeDefinition);
 		
 		setFixture(specObject);
 	}	
 	
 	
 	@Before
 	public void setUp(){
 		createFixture(new GregorianCalendar(2014, 11, 03));
 	}
 	
 	
 	@Test
 	public void testIs() throws Exception {
 		DateFilter filter;
 			
 		filter = new DateFilter(IFilter.Operator.IS, new GregorianCalendar(2014, 11, 03), null,  attributeDefinition);
 		doMatch(filter, true);
 		
 		filter = new DateFilter(IFilter.Operator.IS, new GregorianCalendar(2014, 11, 04), null,  attributeDefinition);
 		doMatch(filter, false);
 
 		filter = new DateFilter(IFilter.Operator.IS, new GregorianCalendar(2014,11,03,0,0,0), null,  attributeDefinition);
 		doMatch(filter, true);
 		
 		// we do the match on date only, the time of the day should be ignored 
 		createFixture(new GregorianCalendar(2014, 11, 03, 13, 14, 15));		
 		filter = new DateFilter(IFilter.Operator.IS, new GregorianCalendar(2014,11,03), null,  attributeDefinition);
 		doMatch(filter, true);
 				
 		// for is and is_not operator the time of the day is ignored
 		filter = new DateFilter(IFilter.Operator.IS, new GregorianCalendar(2014,11,03,20,0,0), null,  attributeDefinition);
 		doMatch(filter, true);
 		
 		
 		GregorianCalendar fixtureDate = new GregorianCalendar(2014, 11, 3, 23, 0, 0);
 		fixtureDate.setTimeZone(TimeZone.getTimeZone("GMT"));
		GregorianCalendar filterDate = new GregorianCalendar(TimeZone.getTimeZone("GMT+9"));
 		filterDate.set(2014, 11, 4, 8, 0, 0);
 		System.out.println((filterDate.getTimeInMillis() - fixtureDate.getTimeInMillis()) / 1000 / 60 / 60);
 		
 		createFixture(fixtureDate);		
 		filter = new DateFilter(IFilter.Operator.IS, filterDate, null,  attributeDefinition);
 		doMatch(filter, true);
 	}
 	
 	
 	@Test
 	public void testIsOnInternal() throws Exception {
 		DateFilter filter;
 		
 		filter = new DateFilter(IFilter.Operator.IS, new GregorianCalendar(2014, 11, 03), null,  DateFilter.InternalAttribute.LAST_CHANGE);
 		doMatch(filter, true);
 		
 		filter = new DateFilter(IFilter.Operator.IS, new GregorianCalendar(2014, 11, 04), null,  DateFilter.InternalAttribute.LAST_CHANGE);
 		doMatch(filter, false);
 
 		filter = new DateFilter(IFilter.Operator.IS, new GregorianCalendar(2014,11,03,0,0,0), null,  DateFilter.InternalAttribute.LAST_CHANGE);
 		doMatch(filter, true);
 	}
 	
 	
 	@Test
 	public void testIsNot() throws Exception {
 		DateFilter filter;
 		
 		filter = new DateFilter(IFilter.Operator.IS_NOT, new GregorianCalendar(2014, 11, 04), null,  attributeDefinition);
 		doMatch(filter, true);
 
 		filter = new DateFilter(IFilter.Operator.IS_NOT, new GregorianCalendar(2014, 11, 03), null,  attributeDefinition);
 		doMatch(filter, false);
 	}
 	
 	
 	@Test
 	public void testIsNotOnInternal() throws Exception {
 		DateFilter filter;
 		
 		filter = new DateFilter(IFilter.Operator.IS_NOT, new GregorianCalendar(2014, 11, 04), null,  DateFilter.InternalAttribute.LAST_CHANGE);
 		doMatch(filter, true);
 
 		filter = new DateFilter(IFilter.Operator.IS_NOT, new GregorianCalendar(2014, 11, 03), null,  DateFilter.InternalAttribute.LAST_CHANGE);
 		doMatch(filter, false);
 	}
 	
 
 	@Test
 	public void testBetween() throws Exception {
 		DateFilter filter;
 		
 		filter = new DateFilter(IFilter.Operator.BETWEEN, new GregorianCalendar(2014, 11, 01), new GregorianCalendar(2014, 11, 04), attributeDefinition);
 		doMatch(filter, true);
 		filter = new DateFilter(IFilter.Operator.BETWEEN, new GregorianCalendar(2014, 11, 04), new GregorianCalendar(2014, 11, 01), attributeDefinition);
 		doMatch(filter, true);
 
 		filter = new DateFilter(IFilter.Operator.BETWEEN, new GregorianCalendar(2014, 11, 03), new GregorianCalendar(2014, 11, 03), attributeDefinition);
 		doMatch(filter, true);
 		
 		filter = new DateFilter(IFilter.Operator.BETWEEN, new GregorianCalendar(2014, 11, 01), new GregorianCalendar(2014, 11, 03), attributeDefinition);
 		doMatch(filter, true);
 		filter = new DateFilter(IFilter.Operator.BETWEEN, new GregorianCalendar(2014, 11, 03), new GregorianCalendar(2014, 11, 06), attributeDefinition);
 		doMatch(filter, true);
 		
 		filter = new DateFilter(IFilter.Operator.BETWEEN, new GregorianCalendar(2014, 01, 01), new GregorianCalendar(2014, 11, 01), attributeDefinition);
 		doMatch(filter, false);
 	}
 		
 	@Test
 	public void testBetweenOnInternal() throws Exception {
 		DateFilter filter;
 		
 		filter = new DateFilter(IFilter.Operator.BETWEEN, new GregorianCalendar(2014, 11, 01), new GregorianCalendar(2014, 11, 04), DateFilter.InternalAttribute.LAST_CHANGE);
 		doMatch(filter, true);
 
 		filter = new DateFilter(IFilter.Operator.BETWEEN, new GregorianCalendar(2014, 11, 03), new GregorianCalendar(2014, 11, 03), DateFilter.InternalAttribute.LAST_CHANGE);
 		doMatch(filter, true);
 		
 		filter = new DateFilter(IFilter.Operator.BETWEEN, new GregorianCalendar(2014, 01, 01), new GregorianCalendar(2014, 11, 01), DateFilter.InternalAttribute.LAST_CHANGE);
 		doMatch(filter, false);
 	}
 	
 	@Test
 	public void testBefore() throws Exception {
 		DateFilter filter;
 		
 		filter = new DateFilter(IFilter.Operator.BEFORE, new GregorianCalendar(2015, 1, 1), new GregorianCalendar(2014, 11, 04), attributeDefinition);
 		doMatch(filter, true);
 
 		filter = new DateFilter(IFilter.Operator.BEFORE, new GregorianCalendar(2014, 11, 3), null, attributeDefinition);
 		doMatch(filter, false);
 		
 		filter = new DateFilter(IFilter.Operator.BEFORE, new GregorianCalendar(2014, 1, 1), null, attributeDefinition);
 		doMatch(filter, false);
 	}
 	
 	@Test
 	public void testBeforeOnInternal(){
 		DateFilter filter;
 		filter = new DateFilter(IFilter.Operator.BEFORE, new GregorianCalendar(2015, 1, 1), new GregorianCalendar(2014, 11, 04), DateFilter.InternalAttribute.LAST_CHANGE);
 		doMatch(filter, true);
 
 		filter = new DateFilter(IFilter.Operator.BEFORE, new GregorianCalendar(2014, 11, 3), null, DateFilter.InternalAttribute.LAST_CHANGE);
 		doMatch(filter, false);
 		
 		filter = new DateFilter(IFilter.Operator.BEFORE, new GregorianCalendar(2014, 1, 1), null, DateFilter.InternalAttribute.LAST_CHANGE);
 		doMatch(filter, false);
 	}
 	
 	@Test
 	public void testAfter() throws Exception {
 		DateFilter filter;
 		
 		filter = new DateFilter(IFilter.Operator.AFTER, new GregorianCalendar(2000, 1, 1), null, attributeDefinition);
 		doMatch(filter, true);
 
 		filter = new DateFilter(IFilter.Operator.AFTER, new GregorianCalendar(3000, 1, 1), null, attributeDefinition);
 		doMatch(filter, false);
 		
 		filter = new DateFilter(IFilter.Operator.AFTER, new GregorianCalendar(2014, 11, 03), null, attributeDefinition);
 		doMatch(filter, false);
 	}
 	
 	@Test
 	public void testAfterOnInternal() throws Exception {
 		DateFilter filter;
 		
 		filter = new DateFilter(IFilter.Operator.AFTER, new GregorianCalendar(2000, 1, 1), null, DateFilter.InternalAttribute.LAST_CHANGE);
 		doMatch(filter, true);
 
 		filter = new DateFilter(IFilter.Operator.AFTER, new GregorianCalendar(3000, 1, 1), null, DateFilter.InternalAttribute.LAST_CHANGE);
 		doMatch(filter, false);
 		
 		filter = new DateFilter(IFilter.Operator.AFTER, new GregorianCalendar(2014, 11, 03), null, DateFilter.InternalAttribute.LAST_CHANGE);
 		doMatch(filter, false);
 	}
 	
 	
 	public void doEmptyTest(){
 		DateFilter filter;
 		
 		filter = new DateFilter(IFilter.Operator.IS, new GregorianCalendar(2014, 1, 1), null, attributeDefinition);
 		doMatch(filter, false);
 		
 		filter = new DateFilter(IFilter.Operator.IS_NOT, new GregorianCalendar(2014, 1, 1), null, attributeDefinition);
 		doMatch(filter, true);
 		
 		filter = new DateFilter(IFilter.Operator.BETWEEN, new GregorianCalendar(2014, 1, 1), new GregorianCalendar(2014, 1, 1), attributeDefinition);
 		doMatch(filter, false);
 		
 		filter = new DateFilter(IFilter.Operator.BEFORE, new GregorianCalendar(2014, 1, 1), null, attributeDefinition);
 		doMatch(filter, false);
 		
 		filter = new DateFilter(IFilter.Operator.AFTER, new GregorianCalendar(2000, 1, 1), null, attributeDefinition);
 		doMatch(filter, false);
 	}
 	
 	
 	@Test
 	public void testInternalAttributes() throws Exception {
 		// TODO: validate that all operators are tested against internalAttribute 
 		fail("not yet implemented");
 	}
 	
 	
 	@Test
 	public void testIsSet() throws Exception {
 		DateFilter filter;
 		
 		filter = new DateFilter(IFilter.Operator.IS_SET, new GregorianCalendar(2014, 1, 1), null, attributeDefinition);
 		doMatch(filter, true);
 		
 		AttributeDefinitionDate attributeDefinition2 = ReqIF10Factory.eINSTANCE.createAttributeDefinitionDate();
 		attributeDefinition2.setIdentifier("AD_ID1");
 		
 		filter = new DateFilter(IFilter.Operator.IS_SET, new GregorianCalendar(2014, 1, 1), null, attributeDefinition2);
 		doMatch(filter, false);
 	}
 	
 	
 	@Test
 	public void testIsNotSet() throws Exception {
 		DateFilter filter;
 		
 		filter = new DateFilter(IFilter.Operator.IS_NOT_SET, new GregorianCalendar(2014, 1, 1), null, attributeDefinition);
 		doMatch(filter, false);
 		
 		AttributeDefinitionDate attributeDefinition2 = ReqIF10Factory.eINSTANCE.createAttributeDefinitionDate();
 		attributeDefinition2.setIdentifier("AD_ID1");
 		
 		filter = new DateFilter(IFilter.Operator.IS_NOT_SET, new GregorianCalendar(2014, 1, 1), null, attributeDefinition2);
 		doMatch(filter, true);
 	}
 	
 	
 	
 	@Test
 	public void testIsSetInternal() throws Exception {
 		DateFilter filter;
 		
 		filter = new DateFilter(Operator.IS_SET, new GregorianCalendar(2014, 1, 1), null, DateFilter.InternalAttribute.LAST_CHANGE);
 		doMatch(filter, true);
 		
 		filter = new DateFilter(Operator.IS_SET, null, null, DateFilter.InternalAttribute.LAST_CHANGE);
 		doMatch(filter, true);
 		
 		getFixture().setLastChange(null);
 		filter = new DateFilter(Operator.IS_SET, null, null, DateFilter.InternalAttribute.LAST_CHANGE);
 		doMatch(filter, false);
 	}
 	
 	@Test
 	public void testIsNotSetInternal() throws Exception {
 		DateFilter filter;
 		
 		filter = new DateFilter(Operator.IS_NOT_SET, new GregorianCalendar(2014, 1, 1), null, DateFilter.InternalAttribute.LAST_CHANGE);
 		doMatch(filter, false);
 		
 		filter = new DateFilter(Operator.IS_NOT_SET, null, null, DateFilter.InternalAttribute.LAST_CHANGE);
 		doMatch(filter, false);
 		
 		getFixture().setLastChange(null);
 		filter = new DateFilter(Operator.IS_NOT_SET, null, null, DateFilter.InternalAttribute.LAST_CHANGE);
 		doMatch(filter, true);
 	}
 	
 	
 	@Test
 	public void testExceptionsAttributeDefinition() throws Exception {
 		thrown.expect(IllegalArgumentException.class);
 		new DateFilter(IFilter.Operator.BEFORE, new GregorianCalendar(2014, 1, 1), null, (AttributeDefinitionDate) null);
 	}
 	
 	@Test
 	public void testExceptionsInternalAttribute() throws Exception {
 		thrown.expect(IllegalArgumentException.class);
 		new DateFilter(IFilter.Operator.BEFORE, new GregorianCalendar(2014, 1, 1), null, (InternalAttribute) null);
 	}
 	
 	@Override
 	public DateFilter createFilterInstance(Operator operator) {
 		return new DateFilter(operator, new GregorianCalendar(2014, 1, 1), null, attributeDefinition);
 	}
 
 	@Override
 	public Set<Operator> getSupportedOperators() {
 		return DateFilter.SUPPORTED_OPERATORS;
 	}
 	
 }
