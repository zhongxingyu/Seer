 package org.concord.LabBook;
 
 public class DefaultFactory 
 	extends LabObjectFactory
 {
	private static DefaultFactory me = null;
 	final public static int DEFAULT_FACTORY 	= 0x0001;
 
 	public DefaultFactory()
 	{
 		super(DEFAULT_FACTORY);
		me = this;
 	}
 
     final public static int UNDEFINED 			=0;
     final public static int DICTIONARY 			=1;
     final public static int FORM 				= 2;
     final public static int DOCUMENT 			= 6;
     final public static int OUTPUT_SET 			= 7;
     final public static int QUESTION 			= 8;
     final public static int DRAWING 			= 9;
     final public static int IMAGE 				= 11;
 
 	public LabObject constructObj(int objectType)
     {
 		LabObject obj = null;
 		switch(objectType){
 			case DICTIONARY:
 				obj= new LObjDictionary();
 				break;
 			case FORM:
 				obj= new LObjForm();
 				break;
 			case DOCUMENT:
 				obj= new LObjDocument();
 				break;
 			case OUTPUT_SET:
 				obj= new LObjOutputSet();
 				break;
 			case QUESTION:
 				obj= new LObjQuestion();
 				break;
 			case DRAWING:
 				obj= new LObjDrawing();
 				break;
 			case IMAGE:
 				obj= new LObjImage();
 				break;
 		}
 		return obj;
 	}
 
 	public void createLabBookObjDescriptors(){
 		labBookObjDesc = new LabObjDescriptor[8];
 		labBookObjDesc[0] = new LabObjDescriptor("Folder",DICTIONARY);
 		labBookObjDesc[1] = new LabObjDescriptor("Form",FORM);
 		labBookObjDesc[2] = new LabObjDescriptor("Notes",DOCUMENT);
 		labBookObjDesc[3] = new LabObjDescriptor("OutputSet",OUTPUT_SET);
 		labBookObjDesc[4] = new LabObjDescriptor("Questions",QUESTION);
 		labBookObjDesc[5] = new LabObjDescriptor("Drawing",DRAWING);
 		labBookObjDesc[7] = new LabObjDescriptor("Image",IMAGE);
 	}
 
 	public static LabObject create(int objectType)
 	{
 		if(me == null) return null;
 		return me.makeNewObj(objectType);
 	}
 
 	public static LObjDictionary createDictionary()
 	{
 		return (LObjDictionary)create(DICTIONARY);
 	}
 
 	public static LObjQuestion createQuestion()
 	{
 		return (LObjQuestion) create(QUESTION);
 	}
 
 	public static LObjOutputSet createOutputSet()
 	{
 		return (LObjOutputSet) create(OUTPUT_SET);
 	}
 
 	public static LObjDocument createDocument()
 	{
 		return (LObjDocument) create(DOCUMENT);
 	}
 }
