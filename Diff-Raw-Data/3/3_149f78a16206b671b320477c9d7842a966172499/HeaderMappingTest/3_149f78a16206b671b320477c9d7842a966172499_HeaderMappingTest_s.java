 package org.ebayopensource.turmeric.runtime.common.impl.internal.services;
 
 import java.util.List;
 
 import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceCreationException;
 import org.ebayopensource.turmeric.runtime.common.impl.internal.config.NameValue;
 import org.ebayopensource.turmeric.runtime.common.impl.internal.config.OptionList;
 import org.ebayopensource.turmeric.runtime.common.service.HeaderMappingsDesc;
 import org.ebayopensource.turmeric.runtime.common.types.SOAHeaders;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  * This class tests jira 1563, which describes a problem with loading a header mapping.
  * 
  * @author dcarver
  *
  */
 public class HeaderMappingTest {
 
 	@Test
 	public void testSOAPActionHeaderMappingTest() throws Exception {
 		DummyBaseDescFactory factory = new DummyBaseDescFactory("testFactory", false, true);
 		OptionList options = new OptionList();
 		List<NameValue> mappings =  options.getOption();
 		NameValue nv = new NameValue();
 		nv.setName(SOAHeaders.SERVICE_OPERATION_NAME);
 		nv.setValue("header[SOAPAction]");
 		mappings.add(nv);
 		HeaderMappingsDesc hmd = factory.loadHeaderMappings("ExampleService", options, true);
		assertTrue(hmd.getHeaderMap().containsKey("SOAPAction"));
 	}	
 	
 	@Test
 	public void invalidMappingValue() throws Exception {
 		DummyBaseDescFactory factory = new DummyBaseDescFactory("testFactory", false, true);
 		OptionList options = new OptionList();
 		List<NameValue> mappings =  options.getOption();
 		NameValue nv = new NameValue();
 		nv.setName(SOAHeaders.SERVICE_OPERATION_NAME);
 		nv.setValue("SOAPAction");
 		mappings.add(nv);
 		try {
 			factory.loadHeaderMappings("ExampleService", options, true);
 		} catch (ServiceCreationException e) {
 			return;
 		}
 		fail("Invalid Header mapping succeeded.");
 	}
 }
