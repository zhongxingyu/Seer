 /**
  * 
  */
 package com.inertia.solutions.cxf.ws.example.provision.web.service;
 
 import java.util.Map;
 
 import javax.jws.WebService;
 
 import org.apache.commons.lang.RandomStringUtils;
 import org.apache.log4j.Logger;
 
 import com.inertia.solutions.cxf.ws.example.provision.beans.GuidBean;
 import com.inertia.solutions.cxf.ws.example.provision.beans.NameBean;
 
 /**
  * Using JRE 1.7.0_07
  * 
  * The Class GuidServiceImpl.
  *
  * @author Ian Hamilton
  * @version 1.0
  * @since 1.0
  */
@WebService(endpointInterface = "com.inertia.solutions.cxf.ws.example.provision.web.service.GuidService")
 public class GuidServiceImpl implements GuidService{
 	static Logger log = Logger.getLogger(GuidServiceImpl.class);
 	
 	/** The Constant RANDOM_LENGTH. */
 	private static final int RANDOM_LENGTH = 10;
 	
 	/** The guid map. */
 	private Map<String, GuidBean> guidMap;
 	
 	/* (non-Javadoc)
 	 * @see com.intertia.cxf.provision.web.service.GuidService#checkForDuplicate(java.lang.String, java.lang.String)
 	 */
 	@Override
 	public Boolean checkForDuplicate(NameBean name) {
 		log.info("Arguments " + name.getFirstName() + "- " + name.getLastName());
 		return guidMap.containsKey(name.getFirstName() + name.getLastName());
 	}
 
 	/* (non-Javadoc)
 	 * @see com.intertia.cxf.provision.web.service.GuidService#getGuid(java.lang.String, java.lang.String)
 	 */
 	//@Override
 	public String getGuid(NameBean name) {
 		String guid = RandomStringUtils.randomAscii(RANDOM_LENGTH);
 		GuidBean bean = new GuidBean();
 		bean.setFirstName(name.getFirstName());
 		bean.setLastName(name.getLastName());
 		bean.setGuid(guid);
 		guidMap.put(bean.toString(), bean);
 		return guid;		
 	}
 
 	/**
 	 * Sets the guid map.
 	 *
 	 * @param guidMap the guid map
 	 */
 	public void setGuidMap(Map<String, GuidBean> guidMap) {
 		this.guidMap = guidMap;
 	}
 
 }
