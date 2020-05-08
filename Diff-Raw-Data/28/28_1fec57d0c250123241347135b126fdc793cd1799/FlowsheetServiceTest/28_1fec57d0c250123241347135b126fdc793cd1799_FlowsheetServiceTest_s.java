 package org.openmrs.module;
 
 import java.util.List;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.flowsheet.FlowsheetEntry;
 import org.openmrs.module.flowsheet.impl.FlowsheetService;
 import org.openmrs.test.BaseContextSensitiveTest;
 
 public class FlowsheetServiceTest extends BaseContextSensitiveTest {
 	private List<FlowsheetEntry> getFlowSheetEntry(int personId) {
 		return service.getFlowsheet(personId).getEntries();
 	}
 
 	private FlowsheetService service;
 	private FlowsheetEntry entry;
 
 	@Before
 	public void setUp() {
 		service = Context.getService(FlowsheetService.class);
 		entry = getFlowSheetEntry(7).get(0);
 	}
 
 	@Test
 	public void shouldReturnObservationsForPerson() {
 		Assert.assertEquals(0, getFlowSheetEntry(1).size());
 		Assert.assertEquals(9, getFlowSheetEntry(7).size());
 	}
 
 	@Test
 	public void shouldReturnConceptNameForEachObservation() {
 		Assert.assertEquals("WEIGHT (KG)", entry.getName());
 	}
 
 	@Test
 	public void shouldReturnValueForEachObservation() {
 		Assert.assertEquals("50.0", entry.getValue());
 	}
 
 	@Test
 	public void shouldReturnDataTypeForEachObservation() {
 		Assert.assertEquals("Numeric", entry.getDataType());
 	}
 
 	@Test
 	public void shouldReturnClassTypeForEachObservation() {
 		Assert.assertEquals("Test", entry.getClassType());
 	}
 
 	@Test
 	public void shouldReturnDateForEachObservation() {
		Assert.assertEquals("2008-07-01 00:00:00.0", entry.getDate());
 	}
 
 	@Test
 	public void shouldReturnUnitForEachObservation() throws Exception {
 		Assert.assertEquals("kg", entry.getNumeric().getUnit());
 	}
 
 	@Test
 	public void shouldReturnHiValueEachObservation() throws Exception {
 		Assert.assertEquals("250.0", entry.getNumeric().getHi().toString());
 	}
 }
