 package jpaoletti.jpm.core.operations;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import jpaoletti.jpm.converter.Converter;
 import jpaoletti.jpm.converter.ConverterException;
 import jpaoletti.jpm.converter.IgnoreConvertionException;
 import jpaoletti.jpm.core.*;
 import jpaoletti.jpm.core.exception.NotAuthenticatedException;
 import jpaoletti.jpm.core.exception.NotAuthorizedException;
 import jpaoletti.jpm.core.message.MessageFactory;
 import jpaoletti.jpm.validator.ValidationResult;
 import jpaoletti.jpm.validator.Validator;
 
 /**
  *
  * @author jpaoletti
  */
 public class OperationCommandSupport extends PMCoreObject implements OperationCommand {
 
     public static final String UNESPECTED_ERROR = "pm_core.unespected.error";
     public static final String FINISH = "finish";
     public static final String LAST_PM_ID = "LAST_PM_ID";
     public static final String PM_ID = "PM_ID";
     public static final String OPERATIONS = "operations";
     public static final String PM_ITEM = "item";
     private String operationId;
     private Operation operation;
 
     public OperationCommandSupport(String operationId) {
         this.operationId = operationId;
     }
 
     protected InstanceId buildInstanceId(Entity entity, final String item) throws NumberFormatException {
         InstanceId instanceId;
         if (entity.isIdentified()) {
             instanceId = new InstanceId(item);
         } else {
             instanceId = new InstanceId(Integer.parseInt(item));
         }
         return instanceId;
     }
 
     protected boolean prepare(PMContext ctx) throws PMException {
         //No session or no user when user is required.
         if (ctx.getPmsession() == null || (checkUser() && ctx.getUser() == null)) {
             throw new NotAuthenticatedException();
         }
         configureEntityContainer(ctx);
         configureSelected(ctx);
         operation = configureOperations(ctx);
         if (operation != null && operation.getPerm() != null && !ctx.getUser().hasPermission(operation.getPerm())) {
             throw new NotAuthorizedException();
         }
         //Try to refresh selected object, if there is one
         refreshSelectedObject(ctx, null);
         return true;
     }
 
     @Override
     public boolean execute(PMContext ctx) throws PMException {
         boolean step = prepare(ctx);
         if (step) {
             internalExecute(ctx);
         }
         return step;
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
                 tx = ctx.getPersistenceManager().startTransaction(ctx);
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
                     ctx.getPersistenceManager().commit(ctx, tx);
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
             throw new PMException(UNESPECTED_ERROR, e);
         } finally {
             if (tx != null) {
                 ctx.getPresentationManager().debug(this, "Rolling Back Transaction " + tx);
                 try {
                     ctx.getPersistenceManager().rollback(ctx, tx);
                     rollback(ctx);
                 } catch (Exception e) {
                     ctx.getPresentationManager().error(e);
                 }
             }
         }
     }
 
     protected void rollback(PMContext ctx) throws PMException {
         final EntityContainer c = ctx.getEntityContainer(true);
         //We need to remove reference of new objects
         if (c != null) {
             if (ctx.getSelected() != null && ctx.getEntityContainer().isSelectedNew()) {
                 if (c.getOwner() != null) {
                     final Object object = ctx.getEntityContainer().getOwner().getSelected().getInstance();
                     final Collection<Object> collection = (Collection<Object>) ctx.getPresentationManager().get(object, ctx.getEntity().getOwner().getEntityProperty());
                     collection.remove(ctx.getSelected().getInstance());
                 }
                 ctx.getEntityContainer().setSelected(null);
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
                     ctx.setEntityInstance(ctx.getSelected().getInstance());
                     ValidationResult vr = ev.validate(ctx);
                     ctx.getMessages().addAll(vr.getMessages());
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
         //If we get item param, we change the selected item on the container.
         //This may be either an identification field value or an index depending
         //on entity.isIdentified value
         final String item = ctx.getString(PM_ITEM);
         if (item != null && !item.trim().equals("")) {
             final Object instance = ctx.getDataAccess().getItem(ctx, buildInstanceId(ctx.getEntity(), item));
             ctx.getEntityContainer().setSelected(new EntityInstanceWrapper(instance));
         } else {
             final String identified = (String) ctx.getParameter("identified");
             if (identified != null && identified.trim().compareTo("") != 0) {
                 ctx.getPresentationManager().debug(this, "Getting row identified by: " + identified);
                 String[] ss = identified.split(":");
                 if (ss.length == 2) {
                     final String prop = ss[0];
                     final String value = ss[1];
                     final Object object = ctx.getEntity().getDataAccess().getItem(ctx, prop, value);
                     if (object != null) {
                         final EntityInstanceWrapper wrapper = new EntityInstanceWrapper(object);
                         ctx.getEntityContainer().setSelected(wrapper);
                     }
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
             ctx.addMessage(MessageFactory.error(ctx.getEntity(), "unknow.item"));
         }
     }
 
     public Operation configureOperations(PMContext ctx) throws PMException {
         final Operation op = (ctx.hasEntity()) ? ctx.getEntity().getOperations().getOperation(operationId) : null;
         ctx.setOperation(op);
         if (ctx.hasEntity()) {
             ctx.getEntityContainer().setOperation(op);
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
             final Object item = (ctx.getSelected() == null) ? null : ctx.getSelected().getInstance();
             ctx.put(OPERATIONS, ctx.getEntity().getOperations().getOperationsFor(ctx, item, ctx.getOperation()));
         }
         return op;
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
                 Object o;
                 try {
                     o = ctx.getEntity().getDataAccess().refresh(ctx, origin.getInstance());
                 } catch (Exception e) {
                     o = null;
                 }
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
         return (EntityContainer) ctx.getPmsession().getContainer(EntityContainer.buildId(eid));
     }
 
     protected boolean configureEntityContainer(PMContext ctx) throws PMException {
         String pmid = ctx.getString(PM_ID);
         if (pmid == null) {
             pmid = ctx.getPmsession().getString(LAST_PM_ID);
         } else {
             ctx.getPmsession().put(LAST_PM_ID, pmid);
         }
         boolean fail = false;
         if (pmid == null) {
             if (checkEntity()) {
                 ctx.getEntityContainer();
             }
         } else {
             ctx.setEntityContainer(ctx.getEntityContainer(pmid));
             if (!ctx.hasEntityContainer() && checkEntity()) {
                 throw new PMException("pm_core.entity.not.found");
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
 
     @Override
     public String getOperationId() {
         return operationId;
     }
 
     protected void proccessField(PMContext ctx, Field field, EntityInstanceWrapper wrapper) {
         final List<Object> parameterValues = getParameterValues(ctx, field);
         int i = 0;
         for (Object value : parameterValues) {
             try {
                 final Converter converter = field.getConverters().getConverterForOperation(ctx.getOperation().getId());
                 Object converted = getConvertedValue(ctx, field, value, wrapper, converter);
                 doProcessField(wrapper, i, converter, ctx, field, converted);
             } catch (IgnoreConvertionException e) {
                 //Do nothing, just ignore conversion.
             } catch (ConverterException e) {
                 ctx.getPresentationManager().error(String.format("Error converting %s.%s : %s", ctx.getEntity().getId(), field.getId(), e.getMessage()));
                 ctx.getPresentationManager().warn(e);
                 ctx.addMessage(MessageFactory.error(ctx.getEntity(), field, e.getMsg().getKey()));
             } catch (Exception e) {
                 ctx.getPresentationManager().error(String.format("Error converting %s.%s : %s", ctx.getEntity().getId(), field.getId(), e.getMessage()));
                 ctx.getPresentationManager().warn(e);
                 ctx.addMessage(MessageFactory.error(ctx.getEntity(), field, UNESPECTED_ERROR));
             }
             i++;
         }
     }
 
     protected void doProcessField(EntityInstanceWrapper wrapper, int i, final Converter converter, PMContext ctx, Field field, Object converted) {
         final Object o = wrapper.getInstance(i);
         if (converter.getValidate()) {
             if (validateField(ctx, field, wrapper, converted)) {
                 ctx.getPresentationManager().set(o, field.getProperty(), converted);
             }
         } else {
             ctx.getPresentationManager().set(o, field.getProperty(), converted);
         }
     }
 
     protected Object getConvertedValue(PMContext ctx, Field field, Object values, EntityInstanceWrapper wrapper, final Converter converter) throws ConverterException {
         if (converter == null) {
             throw new IgnoreConvertionException();
         }
         ctx.setField(field);
         ctx.setFieldValue(values);
         ctx.setEntityInstanceWrapper(wrapper);
         final Object converted = converter.build(ctx);
         return converted;
     }
 
     private boolean validateField(PMContext ctx, Field field, EntityInstanceWrapper wrapper, Object o) {
         boolean ok = true;
         if (field.getValidators() != null) {
             for (Validator fv : field.getValidators()) {
                 ctx.setEntityInstance(wrapper.getInstance());
                 ctx.setField(field);
                 ctx.setFieldValue(o);
                 ValidationResult vr = fv.validate(ctx);
                 ctx.getMessages().addAll(vr.getMessages());
                 ok = ok && vr.isSuccessful();
             }
         }
         return ok;
     }
 
     private Object getParamValues(PMContext ctx, String name, String separator) {
         final Object parameters = ctx.getParameter(name);
         if (parameters == null) {
             return null;
         }
        //The following is kept backward compatibility
        if (parameters instanceof Object[]) {
            final Object[] ss = (Object[]) parameters;
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
         } else {
             return parameters;
         }
     }
 
     protected List<Object> getParameterValues(PMContext ctx, Field field) {
        final List<Object> result = new ArrayList<Object>();
         String eid = "f_" + field.getId();
         Object s = getParamValues(ctx, eid, ";");
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
 
     /**
      * @return the selected instances 
      */
     public List<Object> getSelectedInstances(PMContext ctx) throws PMException {
         final List<Object> result = new ArrayList<Object>();
         final List<InstanceId> selectedIndexes = ctx.getEntityContainer().getSelectedInstanceIds();
         for (InstanceId id : selectedIndexes) {
             result.add(ctx.getDataAccess().getItem(ctx, id));
         }
         return result;
     }
 
     protected void doExecute(PMContext ctx) throws PMException {
     }
 
     /**
      * Indicates if the operation is finished. The operation has ended when
      * the "finished" parameter is present.
      * 
      */
     protected boolean finished(PMContext ctx) {
         return ctx.getParameter(FINISH) != null;
     }
 
     protected void assertNotNull(Object o, String msgkey) throws PMException {
         if (o == null) {
             throw new PMException(msgkey);
         }
     }
 
     protected void assertTrue(boolean b, String msgkey) throws PMException {
         if (!b) {
             throw new PMException(msgkey);
         }
     }
 }
