 package org.imirsel.nema.service;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
import java.util.Map;
 
 import org.imirsel.meandre.client.TransmissionException;
 import org.imirsel.nema.annotatons.parser.beans.DataTypeBean;
 import org.imirsel.nema.model.Component;
 import org.imirsel.nema.model.Property;
 import org.imirsel.nema.service.BaseManagerTestCase;
 import org.imirsel.nema.service.impl.ComponentMetadataServiceImpl;
 import org.imirsel.nema.service.impl.MeandreProxyWrapper;
 
 
 public class ComponentMetadataServiceTest extends BaseManagerTestCase{
 	
 	private ComponentMetadataServiceImpl componentMetadataManager = new ComponentMetadataServiceImpl();
 	private MeandreProxyWrapper meandreProxyWrapper;
 	
 	public ComponentMetadataServiceTest(){
 		meandreProxyWrapper = new MeandreProxyWrapper();
 		meandreProxyWrapper.init();
 		componentMetadataManager.setMeandreProxyWrapper(meandreProxyWrapper);
 	}
 	
 	
 	public void testme() throws TransmissionException{
 		componentMetadataManager.dd();
 	}
 
	
	public void testGetComponentDataType() throws TransmissionException{
 		String componentUri="meandre://seasr.org/components/datatypetestcomponent";
 		String instanceUri = "http://test.org/datatypetest/instance/datatypetestcomponent/1";
 		Component component= new Component();
 		component.setInstanceUri(instanceUri);
 		component.setUri(componentUri);
		Map<String, Property> map=componentMetadataManager.getComponentPropertyDataType(component, instanceUri);
 		System.out.println("--> here "+ map.size());
 		Iterator<String> its = map.keySet().iterator();
 		while(its.hasNext()){
 			String key = its.next();
 			Property property = map.get(key);
 			List<DataTypeBean> ltb = property.getDataTypeBeanList();
 			System.out.println(property.getName() + "  " + property.getDefaultValue());
 			 Iterator<DataTypeBean> itb = ltb.iterator();
 			while(itb.hasNext()){
 				DataTypeBean dt = itb.next();
 				System.out.println("class Name:"+ dt.getClass().getName());
 				System.out.println("Renderer: "+dt.getRenderer());
 			}
 			System.out.println("---------------------");
 			
 		}
 		 
 
 	
 	}
	
 	
 
 }
