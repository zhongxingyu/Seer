 package org.aesthete.swingobjects.datamap;
 
 import java.lang.reflect.Field;
 
 import javax.swing.JComponent;
 
 import org.aesthete.swingobjects.annotations.DataBeanName;
 import org.aesthete.swingobjects.datamap.converters.Converter;
 import org.aesthete.swingobjects.datamap.converters.ConverterUtils;
 import org.aesthete.swingobjects.exceptions.ErrorSeverity;
 import org.aesthete.swingobjects.exceptions.SwingObjectRunException;
 import org.aesthete.swingobjects.scope.RequestScope;
 import org.aesthete.swingobjects.scope.RequestScopeObject;
 import org.aesthete.swingobjects.util.ReflectionCallback;
 import org.aesthete.swingobjects.util.ReflectionUtils;
 import org.aesthete.swingobjects.view.Components;
 import org.aesthete.swingobjects.view.FrameFactory;
 
 public class DataMapper {
 
 	public static void mapData(Object container) {
 		try {
 			DataBeanName dataClass = container.getClass().getAnnotation(
 					DataBeanName.class);
 			if (dataClass != null) {
 				String beanName = dataClass.value();
 				SwingObjData swingObjdata=new SwingObjData();
 				populateObject(container,swingObjdata,true);
 				RequestScopeObject scopeObj=RequestScope.getRequestObj();
 				scopeObj.putObjectInMap(beanName, swingObjdata);
 			}
 		} catch (Exception e) {
 			throw new SwingObjectRunException(e,ErrorSeverity.SEVERE, FrameFactory.class);
 
 		}
 	}
 
 	public static void mapGUI(Object container) {
    		DataBeanName dataClass = container.getClass().getAnnotation(DataBeanName.class);
 		if (dataClass != null) {
 			String beanName = dataClass.value();
 			RequestScopeObject scopeObj=RequestScope.getRequestObj();
 			SwingObjData objData=(SwingObjData)scopeObj.getObjectFromMap(beanName);
 			populateObject(container,objData,false);
 		}
 	}
 
 
	private static void populateObject(final Object container,final SwingObjData objData,final boolean isFromGuiToSwingObjData){
 		ReflectionUtils.iterateOverFields(container.getClass(), null, new ReflectionCallback<Field>() {
 			private boolean isJComponent;
             private Object prop;
 
             @Override
 			public boolean filter(Field field) throws IllegalAccessException {
                 prop = field.get(container);
                 if(prop==null){
                     return false;
                 }
                 isJComponent=false;
                if(Components.class.isAssignableFrom(prop.getClass())) {
                    return true;
                }else if(JComponent.class.isAssignableFrom(field.getType())){
                     isJComponent=true;
                     return true;
                 }
 				return false;
 			}
 
 			@Override
 			public void consume(Field field) throws SwingObjectRunException{
 				try{
 					String name=field.getName();
 					if(isJComponent) {
 						Converter converter=ConverterUtils.getConverter(field.getType());
 						if(converter!=null) {
							if(isFromGuiToSwingObjData) {
 								objData.setUnchanged(name, converter.getDataFromViewComponent((JComponent)prop));
 							}else {
 								if(objData.isChanged(name)) {
 									converter.setDataIntoViewComponent(objData.getValue(name),(JComponent)prop);
 								}
 							}
 						}
 					}else {
 						SwingObjData swingObjdata=null;
						if(isFromGuiToSwingObjData) {
 							swingObjdata=new SwingObjData();
							populateObject(prop,swingObjdata,isFromGuiToSwingObjData);
							if(isFromGuiToSwingObjData) {
 								objData.setUnchanged(name, swingObjdata);
 							}
 						}else {
 							swingObjdata=(SwingObjData)objData.getValue(name).getValue();
 							if(swingObjdata.isDataChanged()) {
								populateObject(prop,swingObjdata,isFromGuiToSwingObjData);
 							}
 						}
 					}
 				}catch(Exception e){
 					throw new SwingObjectRunException(e, ErrorSeverity.SEVERE, DataMapper.class);
 				}
 			}
 		});
 	}
 }
