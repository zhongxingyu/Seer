 package org.concord.LabBook;
 
 public abstract class LabObjectFactory
 {
	protected static LabObjectFactory me = null;
 	public int factoryID = -1;
 	public LabObjDescriptor []labBookObjDesc;
 
 	protected LabObjectFactory(int id)
 	{
		me = this;
 		factoryID = id;
 	}
 	
 	public LabObject makeNewObj(int objectType){
 		return makeNewObj(objectType,true);
 	}
 	public LabObject makeNewObj(int objectType,boolean doInit){
 		LabObject obj = constructObj(objectType);
 		if(doInit) initializeObj(obj);
 		else if(obj != null) obj.factory = this;
 
 		return obj;
 	}
 	
	public static void initializeObj(LabObject obj)
 	{
		if(me != null && obj != null ){
 			obj.init();
			obj.factory = me;
 		}
 	}
 
 	public abstract LabObject constructObj(int objectType);
 	public abstract void createLabBookObjDescriptors();
 	
 	public LabObjDescriptor [] getLabBookObjDesc(){
 		if(labBookObjDesc == null){
 			createLabBookObjDescriptors();
 		}
 		return labBookObjDesc;
 	}
 	
 	
 	public int getFactoryType()
 	{
 		return factoryID;
 	}
 	
 	/*
 		every factory should implement static method:
 		public static LabObject create(int objID);
 	*/
 	
 }
