 package org.aesthete.swingobjects;
 
 import java.awt.Container;
 import java.lang.reflect.Field;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.*;
 import javax.swing.text.JTextComponent;
 
 import org.aesthete.swingobjects.annotations.*;
 import org.aesthete.swingobjects.datamap.DataMapper;
 import org.aesthete.swingobjects.exceptions.*;
 import org.aesthete.swingobjects.scope.RequestScope;
 import org.aesthete.swingobjects.scope.RequestScopeObject;
 import org.aesthete.swingobjects.util.DateUtils;
 import org.aesthete.swingobjects.util.ReflectionCallback;
 import org.aesthete.swingobjects.util.ReflectionUtils;
 import org.aesthete.swingobjects.view.CommonUI;
 import org.aesthete.swingobjects.view.Components;
 import org.aesthete.swingobjects.view.FrameFactory;
 import org.aesthete.swingobjects.workers.SwingWorkerInterface;
 import org.apache.commons.lang3.StringUtils;
 
 /**
  *
  * @author sethu
  */
 public class ActionProcessor {
 
 	public enum CLIENT_PROPS{BORDER,ENABLED,TOOLTIP};
 	boolean isError=false;
 	private RequestScopeObject scopeObj;
 	private List<JComponent> fieldsOfContainer;
 
 	private ActionProcessor() {
 		scopeObj=RequestScope.getRequestObj();
 		fieldsOfContainer=new ArrayList<JComponent>();
 	}
 
 	public static void processAction(Object container,SwingWorkerInterface swingworker){
 		ActionProcessor processor=new ActionProcessor();
 		try{
 			try {
                 if(!swingworker.proceed()){
                     return;
                 }
                 processor.initCompsAndValidate(container,swingworker);
                 if(!processor.isError) {
                     DataMapper.mapData(container);
                     processor.isError=!swingworker.validateAndPopulate(processor.scopeObj);
 					if(!processor.isError) {
 						processor.scopeObj.setContainer(container);
 						processor.scopeObj.setFieldsOfTheContainer(processor.fieldsOfContainer);
 						swingworker.execute();
 					}else{
 						processor.showErrorDialog();
 					}
 				}else {
 					processor.showErrorDialog();
 				}
 			}finally {
 				CommonUI.restoreComponentsToInitialState(processor.fieldsOfContainer);
 				if(processor.isError) {
 					RequestScope.endOfRequest(processor.scopeObj);
 				}
 			}
 		}
 		catch(Exception e){
 			CommonUI.restoreComponentsToInitialState(processor.fieldsOfContainer);
 			if(e instanceof SwingObjectsExceptions) {
 				CommonUI.showErrorDialogForComponent((SwingObjectsExceptions)e);
 			}else {
 				CommonUI.showErrorDialogForComponent(new SwingObjectRunException(e,ErrorSeverity.SEVERE, FrameFactory.class));
 			}
 		}
 	}
 
 	private void showErrorDialog() {
 		if(scopeObj.getErrorObj()!=null) {
 			CommonUI.showErrorDialogForComponent(scopeObj.getErrorObj());
 		}else {
 			CommonUI.showErrorDialogForComponent(new SwingObjectException("swingobj.commonerror", (Throwable)null,ErrorSeverity.ERROR, ActionProcessor.class));
 		}
 	}
 
 	private void initCompsAndValidate(final Object container, final SwingWorkerInterface swingworker) throws IllegalArgumentException, IllegalAccessException {
 		ReflectionUtils.iterateOverFields(container.getClass(), Container.class, new ReflectionCallback<Field>() {
 			private Object prop=null;
 			@Override
 			public boolean filter(Field field) {
 				try {
 					prop=field.get(container);
 					return prop!=null;
 				}catch(Exception e){
 					throw new SwingObjectRunException(e, ErrorSeverity.SEVERE, DataMapper.class);
 				}
 			}
 
 			@Override
 			public void consume(Field field) {
 				try {
 					if (Components.class.isAssignableFrom(prop.getClass())) {
 						initCompsAndValidate(prop, swingworker);
 					}else if(JComponent.class.isAssignableFrom(field.getType())) {
 						fieldsOfContainer.add((JComponent)prop);
 						CommonUI.initComponent(prop);
 						trimTexts(field, prop);
                         handleValidations(field, container, swingworker);
 					}
 				}catch(Exception e){
 					throw new SwingObjectRunException(e, ErrorSeverity.SEVERE, DataMapper.class);
 				}
 			}
 		});
 	}
 
     private void handleValidations(Field field, Object container, SwingWorkerInterface swingworker) throws IllegalAccessException {
         handleRequiredEmptyChecks(field, container, swingworker);
         handleTableSelectARow(field,container,swingworker);
         handleValidDate(field,container,swingworker);
         handleTableEnterData(field, container, swingworker);
     }
 
     private void handleTableEnterData(Field field, Object container, SwingWorkerInterface swingworker) throws IllegalAccessException {
         TableRowsExist tableRowsExist=field.getAnnotation(TableRowsExist.class);
         if(tableRowsExist!=null && isCheckSupposedToExecuteBasedOnAction(tableRowsExist.value(), swingworker.getAction())){
             Object swingObjTableObj=field.get(container);
             if(swingObjTableObj instanceof JTable){
                 JTable table=(JTable)swingObjTableObj;
                 if(table.getRowCount()<=0){
                     scopeObj.setErrorObj(new SwingObjectException("swingobj.placeholdererror",
                             ErrorSeverity.ERROR,ActionProcessor.class,tableRowsExist.errorMsg()));
                     this.isError=true;
                 }
             }
         }
     }
 
     private void handleValidDate(Field field, Object container, SwingWorkerInterface swingworker) throws IllegalAccessException {
         ValidDate validDate=field.getAnnotation(ValidDate.class);
         if(validDate!=null && isCheckSupposedToExecuteBasedOnAction(validDate.value(), swingworker.getAction())){
             Object jtextComponentObj = field.get(container);
             if(jtextComponentObj instanceof JTextComponent){
                 JTextComponent textComponent= (JTextComponent) jtextComponentObj;
                 String text=textComponent.getText();
                 if(StringUtils.isBlank(text)){
                    return;
                 }
                 try{
                     DateUtils.getDateFromFormatOfString(text);
                 }catch (InvalidDateException e){
                     this.isError=true;
                     CommonUI.setErrorBorderAndTooltip(textComponent, e.getMessage());
                 }
             }
         }
     }
 
     private void handleTableSelectARow(Field field, Object container, SwingWorkerInterface swingworker) throws IllegalAccessException {
         TableSelectARow selectARow = field.getAnnotation(TableSelectARow.class);
         if(selectARow!=null && isCheckSupposedToExecuteBasedOnAction(selectARow.value(),swingworker.getAction())){
             Object fieldObj = field.get(container);
             if(fieldObj instanceof JTable){
                 JTable table=(JTable)fieldObj;
                 if(table.getSelectedRow()<0){
                     scopeObj.setErrorObj(new SwingObjectException("swingobj.placeholdererror",ErrorSeverity.ERROR,ActionProcessor.class,selectARow.errorMsg()));
                     this.isError=true;
                 }
             }
         }
     }
 
     private void handleRequiredEmptyChecks(Field field, Object container, SwingWorkerInterface swingworker) throws IllegalAccessException {
         Required reqAnno = field.getAnnotation(Required.class);
         ShouldBeEmpty empty = field.getAnnotation(ShouldBeEmpty.class);
 
         if (reqAnno != null || empty != null) {
             boolean isError=checkForRequired(reqAnno!=null,
                     reqAnno!=null? reqAnno.errorMsg() : empty.errorMsg(),
                     reqAnno!=null? reqAnno.value() : empty.value(),
                             field,container,swingworker.getAction());
             if(isError) {
                 this.isError=true;
             }
         }
     }
 
 
     private boolean checkForRequired(boolean isRequired, String msg, String[] actions,
 			Field field, Object container,String action) throws IllegalArgumentException, IllegalAccessException {
 		if(isCheckSupposedToExecuteBasedOnAction(actions, action)){
 			Object fieldObj = field.get(container);
 			if(fieldObj instanceof JComponent){
 				JComponent jcomponent = (JComponent)fieldObj;
 				boolean isError=false;
                 if(fieldObj instanceof JFormattedTextField){
                     if(isRequired && StringUtils.isBlank(((JTextComponent)fieldObj).getText())){
                         isError=true;
                     }else if(!isRequired && StringUtils.isNotBlank(((JTextComponent)fieldObj).getText())){
                         isError=true;
                     }
                 }else if(fieldObj instanceof JTextComponent){
 					if(isRequired && StringUtils.isEmpty(((JTextComponent)fieldObj).getText())){
 						isError=true;
 					}else if(!isRequired && StringUtils.isNotEmpty(((JTextComponent)fieldObj).getText())){
 						isError=true;
 					}
 				}else if(fieldObj instanceof JComboBox){
 					JComboBox cb=(JComboBox)fieldObj;
 					if(isRequired && cb.getSelectedIndex()==-1){
 						isError=true;
 					}else if(isRequired && (cb.getSelectedItem()==null || StringUtils.isEmpty(cb.getSelectedItem().toString()))){
 						isError=true;
 					}else if(!isRequired && (cb.getSelectedItem()!=null && StringUtils.isNotEmpty(cb.getSelectedItem().toString()))){
 						isError=true;
 					}
 				}else if(fieldObj instanceof JList){
 					JList list=(JList)fieldObj;
 					if(isRequired && list.getSelectedIndex()==-1){
 						isError=true;
 					}else if(isRequired && (list.getSelectedValue()==null || StringUtils.isEmpty(list.getSelectedValue().toString()))){
 						isError=true;
 					}else if(!isRequired && (list.getSelectedValue()!=null && StringUtils.isNotEmpty(list.getSelectedValue().toString()))){
 						isError=true;
 					}
 				}else if(fieldObj instanceof JToggleButton){
 					JToggleButton tglbtn=(JCheckBox)fieldObj;
 					if(isRequired && !tglbtn.isSelected()){
 						isError=true;
 					}else if(!isRequired && tglbtn.isSelected()){
 						isError=true;
 					}
 				}
 
 				if(isError){
 					CommonUI.setErrorBorderAndTooltip(jcomponent, msg);
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
     private boolean isCheckSupposedToExecuteBasedOnAction(String[] actions, String action) {
         return actions==null || actions.length==0
             ||	(actions.length>0 && "ALL".equals(actions[0]))
             ||  (actions.length>0 && StringUtils.isNotEmpty(action) &&	action.equals(actions[0]));
     }
 
     private void trimTexts(Field field, Object prop) {
 		if(prop instanceof JTextComponent) {
 			JTextComponent txtComp=(JTextComponent)prop;
 
 			Trim trim=field.getAnnotation(Trim.class);
 			if(trim!=null) {
 				if(trim.value()==YesNo.YES) {
 					txtComp.setText(txtComp.getText()==null?null:txtComp.getText().trim());
 				}
 			}else if("true".equals(SwingObjProps.getSwingObjProperty("guielements.texttrim"))){
 				txtComp.setText(txtComp.getText()==null?null:txtComp.getText().trim());
 			}
 		}
 	}
 }
