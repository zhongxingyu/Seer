 /*
  * Copyright (c) 2013 KloudTek Ltd
  */
 
 package com.kloudtek.systyrant.dsl.statement;
 
 import com.kloudtek.systyrant.FQName;
 import com.kloudtek.systyrant.Resource;
 import com.kloudtek.systyrant.STContext;
 import com.kloudtek.systyrant.dsl.*;
 import com.kloudtek.systyrant.exception.STRuntimeException;
 import org.jetbrains.annotations.Nullable;
 
 import javax.script.ScriptException;
 import java.util.*;
 
 public class CreateResourceStatement extends Statement {
     private FQName elementName;
     private List<Instance> instances = new ArrayList<>();
     private STContext ctx;
 
     public CreateResourceStatement(STContext ctx, SystyrantLangParser.CreateResourceContext createElementsContext) throws InvalidScriptException {
         this.ctx = ctx;
         elementName = new FQName(createElementsContext.elname.getText());
         Map<String, Parameter> params = new LinkedHashMap<>();
         SystyrantLangParser.CreateResourceParamsContext paramsCtx = createElementsContext.params;
         if (paramsCtx != null) {
             for (SystyrantLangParser.ParameterAssignmentContext pctx : AntLRUtils.nullToEmpty(paramsCtx.parameterAssignment())) {
                 if (pctx != null) {
                     String paramName = pctx.paramName.getText();
                     Parameter parameter = Parameter.create(pctx.staticOrDynamicValue());
                     params.put(paramName, parameter);
                 }
             }
         }
         SystyrantLangParser.CreateResourceStatementsContext resourceStatements = createElementsContext.createResourceStatements();
         if (resourceStatements != null) {
             SystyrantLangParser.CreateResourceSingleInstanceContext singleResource = resourceStatements.createResourceSingleInstance();
             if (singleResource != null) {
                 parseResource(singleResource.createResourceInstanceId(), singleResource.createResourceInstanceElements(), params);
             } else {
                 List<SystyrantLangParser.CreateResourceMultipleInstanceContext> multipleInstances = AntLRUtils.nullToEmpty(resourceStatements.createResourceMultipleInstance());
                 for (SystyrantLangParser.CreateResourceMultipleInstanceContext instance : multipleInstances) {
                     parseResource(instance.createResourceInstanceId(), instance.createResourceInstanceElements(), params);
                 }
             }
         } else {
             instances.add(new Instance(null, null, params));
         }
     }
 
     private void parseResource(SystyrantLangParser.CreateResourceInstanceIdContext resourceInstanceId,
                                List<SystyrantLangParser.CreateResourceInstanceElementsContext> resourceInstanceElements, Map<String, Parameter> params) throws InvalidScriptException {
         String id = AntLRUtils.toString(resourceInstanceId != null ? resourceInstanceId.id : null);
         instances.add(new Instance(id, resourceInstanceElements, params));
     }
 
     public List<Instance> getInstances() {
         return Collections.unmodifiableList(instances);
     }
 
     @Override
     public List<Resource> execute(DSLScript dslScript, Resource parent) throws ScriptException {
         try {
             ArrayList<Resource> resources = new ArrayList<>();
             for (Instance instance : instances) {
                 String oldSource = ctx.getSourceUrl();
                 ctx.setSourceUrl(dslScript.getSourceUrl());
                 assert ctx.getSourceUrl() != null;
                 try {
                     Resource resource = ctx.getResourceManager().createResource(elementName, instance.id, parent, dslScript.getImports());
                     Resource old = ctx.currentResource();
                     ctx.setCurrentResource(resource);
                     for (CreateAction action : instance.actions) {
                         action.execute(dslScript, resource);
                     }
                     resources.add(resource);
                     if (old != null) {
                         ctx.setCurrentResource(old);
                     }
                 } finally {
                     if (oldSource != null) {
                         ctx.setSourceUrl(oldSource);
                     } else {
                         ctx.clearSourceUrl();
                     }
                 }
             }
             return resources;
         } catch (STRuntimeException e) {
             throw new ScriptException(e);
         }
     }
 
     public FQName getElementName() {
         return elementName;
     }
 
     @Override
     public String toString() {
         return "createres{" + elementName + " : " + instances + "}";
     }
 
     public class Instance {
         private String id;
         private final List<CreateAction> actions = new ArrayList<>();
 
         public Instance(@Nullable String id,
                         @Nullable List<SystyrantLangParser.CreateResourceInstanceElementsContext> resourceInstanceElements,
                         @Nullable Map<String, Parameter> params) throws InvalidScriptException {
             this.id = id;
             if (params != null) {
                 for (Map.Entry<String, Parameter> entry : params.entrySet()) {
                     String key = entry.getKey();
                     Parameter value = entry.getValue();
                     if (key.equals("id")) {
                         assignId(value);
                     } else {
                         actions.add(new SetAttrAction(key, value));
                     }
                 }
             }
             for (SystyrantLangParser.CreateResourceInstanceElementsContext rsCtx : AntLRUtils.nullToEmpty(resourceInstanceElements)) {
                 final CreateAction action;
                 if (rsCtx.aspar != null) {
                     String paramName = rsCtx.aspar.paramName.getText().trim().toLowerCase();
                     Parameter parameter = Parameter.create(rsCtx.aspar.staticOrDynamicValue());
                     if (paramName.equals("id")) {
                         assignId(parameter);
                         continue;
                     } else {
                         action = new SetAttrAction(paramName, parameter);
                     }
                 } else if (rsCtx.child != null) {
                     action = new CreateChildAction(rsCtx.child.createResource());
                 } else if (rsCtx.asvar != null) {
                     action = new SetVarAction(rsCtx.asvar);
                 } else {
                     throw new InvalidScriptException("BUG! Unknown statement " + rsCtx.getText());
                 }
                 actions.add(action);
             }
         }
 
         private void assignId(Parameter value) throws InvalidScriptException {
             if (value instanceof StaticParameter) {
                 this.id = value.getRawValue();
                return;
             } else {
                 throw new InvalidScriptException("id " + value.getRawValue() + " in " + elementName + "must be a static value");
             }
         }
 
         public String getId() {
             return id;
         }
 
         @Override
         public String toString() {
             return "instance{" + id + "}";
         }
 
         public Parameter getAttrAssignment(String name) {
             for (CreateAction action : actions) {
                 if (action instanceof SetAttrAction) {
                     SetAttrAction attrAction = (SetAttrAction) action;
                     if (attrAction.paramName.equals(name)) {
                         return attrAction.value;
                     }
                 }
             }
             return null;
         }
     }
 
     public interface CreateAction {
         void execute(DSLScript dslScript, Resource resource) throws STRuntimeException, ScriptException;
     }
 
     public class SetAttrAction implements CreateAction {
         private String paramName;
         private Parameter value;
 
         public SetAttrAction(String paramName, Parameter value) {
             this.paramName = paramName;
             this.value = value;
         }
 
         @Override
         public void execute(DSLScript dslScript, Resource resource) throws STRuntimeException {
             resource.set(paramName, value.eval(ctx, resource));
         }
     }
 
     public class CreateChildAction implements CreateAction {
         private final CreateResourceStatement statement;
 
         public CreateChildAction(SystyrantLangParser.CreateResourceContext createChildCtx) throws InvalidScriptException {
             statement = new CreateResourceStatement(ctx, createChildCtx);
         }
 
         @Override
         public void execute(DSLScript dslScript, Resource resource) throws STRuntimeException, ScriptException {
             statement.execute(dslScript, resource);
         }
     }
 
     public class SetVarAction implements CreateAction {
         private String name;
         private Parameter value;
 
         public SetVarAction(SystyrantLangParser.AssignVariableStatementContext asvar) throws InvalidScriptException {
             name = asvar.var.getText();
             value = Parameter.create(asvar.val);
         }
 
         @Override
         public void execute(DSLScript dslScript, Resource resource) throws STRuntimeException, ScriptException {
             resource.setVar(name, value.eval(ctx, resource));
         }
     }
 }
