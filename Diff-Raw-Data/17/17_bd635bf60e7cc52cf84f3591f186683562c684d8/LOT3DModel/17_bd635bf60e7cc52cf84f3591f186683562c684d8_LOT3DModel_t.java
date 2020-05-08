 package org.libraryofthings.model;
 
 import org.libraryofthings.LOTEnvironment;
 
 import waazdoh.client.Binary;
 import waazdoh.client.MBinaryID;
 import waazdoh.client.ServiceObject;
 import waazdoh.client.ServiceObjectData;
import waazdoh.cutils.MStringID;
 import waazdoh.cutils.xml.JBean;
 
 public class LOT3DModel implements ServiceObjectData, LOTObject {
 	private static final String BEANNAME = "model3d";
 	private static final String NAME = "name";
 	private static final String BINARYID = "binaryid";
 	//
 	private ServiceObject o;
 	private String name = "";
 	private MBinaryID binaryid;
 	private LOTEnvironment env;
 
 	public LOT3DModel(LOTEnvironment env) {
 		this.env = env;
 		o = new ServiceObject(BEANNAME, env.getClient(), this, env.version);
 		newBinary();
 	}
 
	public LOT3DModel(LOTEnvironment env, MStringID id) {
 		this.env = env;
 		o = new ServiceObject(BEANNAME, env.getClient(), this, env.version);
		o.load(id);
 	}
 
 	@Override
 	public JBean getBean() {
 		JBean b = o.getBean();
 		b.addValue(NAME, name);
 		if (binaryid != null) {
 			b.addValue(BINARYID, binaryid.toString());
 		}
 		//
 		return b;
 	}
 
 	@Override
 	public boolean parseBean(JBean bean) {
 		name = bean.getValue("name");
 		binaryid = new MBinaryID(bean.getIDValue(BINARYID));
 		//
 		return true;
 	}
 
 	public ServiceObject getServiceObject() {
 		return o;
 	}
 
 	@Override
 	public boolean isReady() {
 		if (getBinary() != null && getBinary().isReady()) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public Binary getBinary() {
 		if (env != null && binaryid != null) {
 			return this.env.getBinarySource().getOrDownload(binaryid);
 		} else {
 			return null;
 		}
 	}
 
 	public void setName(String n) {
 		this.name = n;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void newBinary() {
 		String comment = "LOT3DModel";
 		String extension = "bin";
 		binaryid = env.getBinarySource().newBinary(comment, extension).getID();
 	}
 
 	public void publish() {
 		o.publish();
 		getBinary().publish();
 	}

	public void save() {
		if (binaryid != null) {
			getBinary().setReady();
			getBinary().save();
		}
		getServiceObject().save();
	}
 }
