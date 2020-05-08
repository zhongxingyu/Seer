 /*
  * jPOS Project [http://jpos.org]
  * Copyright (C) 2000-2010 Alejandro P. Revilla
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.jpos.ee.pm.core.operations;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import org.jpos.ee.Constants;
 import org.jpos.ee.pm.converter.Converter;
 import org.jpos.ee.pm.converter.ConverterException;
 import org.jpos.ee.pm.converter.IgnoreConvertionException;
 import org.jpos.ee.pm.core.*;
 import org.jpos.ee.pm.validator.ValidationResult;
 import org.jpos.ee.pm.validator.Validator;
 import org.jpos.util.DisplacedList;
 import org.jpos.util.LogEvent;
 import org.jpos.util.Logger;
 
 /**
  *
  * @author jpaoletti
  */
 public class OperationCommandSupport implements OperationCommand {
 
     public static final String PM_ENTITY_INSTANCE = "PM_ENTITY_INSTANCE";
     public static final String LAST_PM_ID = "LAST_PM_ID";
     public static final String PM_ID = "PM_ID";
     public static final String OPERATIONS = "operations";
     public static final String PM_ITEM = "item";
     private String operationId;
     private Operation operation;
 
     public OperationCommandSupport(String operationId) {
         this.operationId = operationId;
     }
 
     protected boolean prepare(PMContext ctx) throws PMException {
         //No session or no user when user is required.
         if (ctx.getPMSession() == null || (checkUser() && ctx.getUser() == null)) {
             throw new PMUnauthorizedException();
         }
         configureEntityContainer(ctx);
         operation = configureOperations(ctx);
         configureSelected(ctx);
         //Try to refresh selected object, if there is one
         refreshSelectedObject(ctx, null);
         return true;
     }
 
     public void excecute(PMContext ctx) throws PMException {
         boolean step = prepare(ctx);
         if (step) {
             internalExecute(ctx);
         }
     }
 
     protected void internalExecute(PMContext ctx) throws PMException {
         ctx.getPresentationManager().debug(this, "Executing operation " + getOperationId());
         /* Validate de operation*/
         if (ctx.getSelected() != null) {
             validate(ctx);
         }
 
         Object tx = null;
         try {
             if (openTransaction()) {
                 tx = ctx.getPresentationManager().getPersistenceManager().startTransaction(ctx);
                 ctx.getPresentationManager().debug(this, "Started Transaction " + tx);
             }
             if (operation != null && operation.getContext() != null) {
                 operation.getContext().preExecute(ctx);
             }
 
             /** EXCECUTES THE OPERATION **/
             doExecute(ctx);
 
             if (operation != null && operation.getContext() != null) {
                 operation.getContext().postExecute(ctx);
             }
 
             /*if(isAuditable(ctx)){
             logRevision (ctx.getDB(), (ctx.getEntity()!=null)?ctx.getEntity().getId():null, ctx.getOper_id(), ctx.getUser());
             }*/
             try {
                 if (tx != null) {
                     ctx.getPresentationManager().debug(this, "Commiting Transaction " + tx);
                     ctx.getPresentationManager().getPersistenceManager().commit(ctx, tx);
                 }
             } catch (Exception e) {
                 ctx.getPresentationManager().error(e);
                throw new PMException("pm_core.cannot.commit.txn");
             }
             tx = null;
         } catch (PMException e) {
             throw e;
         } catch (Exception e) {
             ctx.getPresentationManager().error(e);
             throw new PMException(e);
         } finally {
             if (tx != null) {
                 ctx.getPresentationManager().debug(this, "Rolling Back Transaction " + tx);
                 try {
                     ctx.getPresentationManager().getPersistenceManager().rollback(ctx, tx);
                 } catch (Exception e) {
                     ctx.getPresentationManager().error(e);
                 }
             }
         }
     }
 
     /**
      *
      */
     private void validate(PMContext ctx) throws PMException {
         if (ctx.getBoolean("validate", true)) {
             if (ctx.getOperation() != null && ctx.getOperation().getValidators() != null && ctx.getSelected() != null) {
                 for (Validator ev : ctx.getOperation().getValidators()) {
                     ctx.put(PM_ENTITY_INSTANCE, ctx.getSelected().getInstance());
                     ValidationResult vr = ev.validate(ctx);
 
                     ctx.getErrors().addAll(vr.getMessages());
                     if (!vr.isSuccessful()) {
                         throw new PMException();
                     }
                 }
             }
         }
     }
 
     public void configureSelected(PMContext ctx) throws NumberFormatException, PMException {
         if (ctx.getBoolean("clean_selected", false)) {
             ctx.getEntityContainer().setSelected(null);
         }
         //If we get item param, we change the selected item on the container
         String item = ctx.getString(PM_ITEM);
         if (item != null && !item.trim().equals("")) {
             Integer index = Integer.parseInt(item);
             ctx.getPresentationManager().debug(this, "Getting row index: " + index);
             if (index != null) {
                 DisplacedList<Object> al = new DisplacedList<Object>();
                 al.addAll(ctx.getList().getContents());
                 al.setDisplacement(ctx.getList().getContents().getDisplacement());
                 ctx.getEntityContainer().setSelected(new EntityInstanceWrapper(al.get(index)));
             }
         } else {
             String identified = (String) ctx.getParameter("identified");
             if (identified != null && identified.trim().compareTo("") != 0) {
                 ctx.getPresentationManager().debug(this, "Getting row identified by: " + identified);
                 String[] ss = identified.split(":");
                 //TODO Throw exception when the size of this is not 2
                 if (ss.length != 2) {
                     ctx.getPresentationManager().error("Ivalid row identifier!");
                 } else {
                     String prop = ss[0];
                     String value = ss[1];
                     EntityInstanceWrapper wrapper = new EntityInstanceWrapper(ctx.getEntity().getDataAccess().getItem(ctx, prop, value));
                     ctx.getEntityContainer().setSelected(wrapper);
                 }
             } else {
                 ctx.getPresentationManager().debug(this, "Row Selection ignored");
             }
         }
         refreshSelectedObject(ctx, null);
         if (ctx.getOperation() != null && ctx.getOperation().getContext() != null) {
             ctx.getOperation().getContext().preConversion(ctx);
         }
         if (checkSelected() && ctx.getEntityContainer().getSelected() == null) {
             throw new PMException("unknow.item");
         }
     }
 
     public Operation configureOperations(PMContext ctx) throws PMException {
         final Operation operation = (ctx.hasEntity()) ? ctx.getEntity().getOperations().getOperation(operationId) : null;
         ctx.setOperation(operation);
         if (ctx.hasEntity()) {
             ctx.getEntityContainer().setOperation(operation);
             if (ctx.getEntity().isWeak()) {
                 ctx.getEntityContainer().setOwner(getEntityContainer(ctx, ctx.getEntity().getOwner().getEntityId()));
                 if (ctx.getEntityContainer().getOwner() == null) {
                     throw new PMException("owner.not.exists");
                 }
             } else {
                 ctx.getEntityContainer().setOwner(null);
             }
         }
         if (ctx.hasEntityContainer()) {
             ctx.put(OPERATIONS, ctx.getEntity().getOperations().getOperationsFor(ctx.getOperation()));
         }
         return operation;
     }
 
     public Object refreshSelectedObject(PMContext ctx, EntityContainer container) throws PMException {
         EntityContainer entityContainer = container;
 
         if (entityContainer == null) {
             entityContainer = ctx.getEntityContainer(true);
         }
 
         if (entityContainer == null) {
             return null;
         }
         EntityInstanceWrapper origin = entityContainer.getSelected();
 
         if (origin != null) {
             if (!entityContainer.isSelectedNew()) {
                 Object o = ctx.getEntity().getDataAccess().refresh(ctx, origin.getInstance());
                 entityContainer.setSelected(new EntityInstanceWrapper(o));
                 if (o == null) {
                     ctx.getPresentationManager().warn("Fresh instance is null while origin was '" + origin.getInstance() + "'");
                 }
                 return o;
             } else {
                 return origin.getInstance();
             }
         }
         return null;
     }
 
     protected Collection<Object> getOwnerCollection(PMContext ctx) throws PMException {
         final Object object = refreshSelectedObject(ctx, ctx.getEntityContainer().getOwner());
         final Collection<Object> collection = (Collection<Object>) ctx.getPresentationManager().get(object, ctx.getEntity().getOwner().getEntityProperty());
         return collection;
     }
 
     protected EntityContainer getEntityContainer(PMContext ctx, String eid) {
         return (EntityContainer) ctx.getPMSession().get(EntityContainer.buildId(PresentationManager.HASH, eid));
     }
 
     protected boolean configureEntityContainer(PMContext ctx) throws PMException {
         String pmid = ctx.getString(PM_ID);
         if (pmid == null) {
             pmid = ctx.getPMSession().getString(LAST_PM_ID);
         } else {
             ctx.getPMSession().put(LAST_PM_ID, pmid);
         }
         boolean fail = false;
         if (pmid == null) {
             if (checkEntity()) {
                 ctx.getEntityContainer();
             }
         } else {
             try {
                 ctx.setEntityContainer(ctx.getEntityContainer(pmid));
             } catch (PMException e) {
                 ctx.getErrors().clear();
             }
             if (!ctx.hasEntityContainer()) {
                 ctx.setEntityContainer(ctx.getPresentationManager().newEntityContainer(pmid));
                 if (checkEntity()) {
                     ctx.getPMSession().setContainer(pmid, ctx.getEntityContainer());
                 } else {
                     try {
                         ctx.getPMSession().setContainer(pmid, ctx.getEntityContainer());
                     } catch (Exception e) {
                         ctx.getErrors().clear();
                     }
                 }
             }
         }
         return !fail;
     }
 
     /**
      * Forces execute to check if any user is logged in
      */
     protected boolean checkUser() {
         return true;
     }
 
     protected boolean checkEntity() {
         return false;
     }
 
     protected boolean checkSelected() {
         return false;
     }
 
     protected boolean openTransaction() {
         return false;
     }
 
     public String getOperationId() {
         return operationId;
     }
 
     protected PMService getPMService() throws PMException {
         try {
             return (PMService) PresentationManager.getPm().getService();
         } catch (Exception e) {
             throw new PMException();
         }
     }
 
     protected void proccessField(PMContext ctx, Field field, EntityInstanceWrapper wrapper) throws PMException {
         LogEvent evt = ctx.getPresentationManager().getLog().createDebug();
         evt.addMessage("Field [" + field.getId() + "] ");
         final List<String> parameterValues = getParameterValues(ctx, field);
         int i = 0;
         for (String value : parameterValues) {
             evt.addMessage("    Object to convert: " + value);
             try {
                 final Converter converter = field.getConverters().getConverterForOperation(ctx.getOperation().getId());
                 Object converted = getConvertedValue(ctx, field, value, wrapper, converter);
                 evt.addMessage("    Object converted: " + converted);
                 doProcessField(wrapper, i, converter, ctx, field, converted);
             } catch (IgnoreConvertionException e) {
                 //Do nothing, just ignore conversion.
             }
             i++;
         }
         Logger.log(evt);
     }
 
     protected void doProcessField(EntityInstanceWrapper wrapper, int i, final Converter converter, PMContext ctx, Field field, Object converted) throws PMException {
         final Object o = wrapper.getInstance(i);
         if (converter.getValidate()) {
             if (validateField(ctx, field, wrapper, converted)) {
                 ctx.getPresentationManager().set(o, field.getProperty(), converted);
             }
         } else {
             ctx.getPresentationManager().set(o, field.getProperty(), converted);
         }
     }
 
     protected Object getConvertedValue(PMContext ctx, Field field, String values, EntityInstanceWrapper wrapper, final Converter converter) throws ConverterException {
         if(converter==null) throw new IgnoreConvertionException();
         ctx.put(Constants.PM_FIELD, field);
         ctx.put(Constants.PM_FIELD_VALUE, values);
         ctx.put(Constants.PM_ENTITY_INSTANCE_WRAPPER, wrapper);
         final Object converted = converter.build(ctx);
         return converted;
     }
 
     private boolean validateField(PMContext ctx, Field field, EntityInstanceWrapper wrapper, Object o) throws PMException {
         boolean ok = true;
         if (field.getValidators() != null) {
             for (Validator fv : field.getValidators()) {
                 ctx.put(Constants.PM_ENTITY_INSTANCE, wrapper.getInstance());
                 ctx.put(Constants.PM_FIELD, field);
                 ctx.put(Constants.PM_FIELD_VALUE, o);
                 ValidationResult vr = fv.validate(ctx);
                 ctx.getErrors().addAll(vr.getMessages());
                 ok = ok && vr.isSuccessful();
             }
         }
         return ok;
     }
 
     private String getParamValues(PMContext ctx, String name, String separator) {
         String[] ss = (String[]) ctx.getParameters(name);
         if (ss != null) {
             StringBuilder s = new StringBuilder();
             if (ss != null && ss.length > 0) {
                 s.append(ss[0]);
             }
 
             //In this case we have a multivalue input
             for (int i = 1; i < ss.length; i++) {
                 s.append(separator);
                 s.append(ss[i]);
             }
             return s.toString();
         } else {
             return null;
         }
     }
 
     protected List<String> getParameterValues(PMContext ctx, Field field) {
         List<String> result = new ArrayList<String>();
         String eid = "f_" + field.getId();
         String s = getParamValues(ctx, eid, ";");
         int i = 0;
         if (s == null) {
             s = "";
         }
         while (s != null) {
             result.add(s);
             i++;
             s = getParamValues(ctx, eid + "_" + i, ";");
         }
         return result;
     }
 
     protected void doExecute(PMContext ctx) throws PMException{
         
     }
 }
