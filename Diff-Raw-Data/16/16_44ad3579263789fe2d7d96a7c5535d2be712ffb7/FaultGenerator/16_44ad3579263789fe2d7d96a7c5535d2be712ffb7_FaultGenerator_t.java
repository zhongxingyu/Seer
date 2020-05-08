 package org.codehaus.xfire.gen.jsr181;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.namespace.QName;
 
 import org.codehaus.xfire.fault.FaultInfoException;
 import org.codehaus.xfire.gen.GenerationContext;
 import org.codehaus.xfire.gen.GenerationException;
 import org.codehaus.xfire.gen.GeneratorPlugin;
 import org.codehaus.xfire.gen.SchemaSupport;
 import org.codehaus.xfire.service.FaultInfo;
 import org.codehaus.xfire.service.MessagePartInfo;
 import org.codehaus.xfire.service.OperationInfo;
 import org.codehaus.xfire.service.Service;
 
 import com.sun.codemodel.JClass;
 import com.sun.codemodel.JClassAlreadyExistsException;
 import com.sun.codemodel.JCodeModel;
 import com.sun.codemodel.JDefinedClass;
 import com.sun.codemodel.JExpr;
 import com.sun.codemodel.JMethod;
 import com.sun.codemodel.JMod;
 import com.sun.codemodel.JType;
 
 public class FaultGenerator
     extends AbstractPlugin
     implements GeneratorPlugin
 {
     public static final String EXCEPTION_CLASSES = "exception.classes";
 
     private Map<QName,JClass> exClasses = new HashMap<QName,JClass>();
     
     public void generate(GenerationContext context)
         throws Exception
     {
         context.setProperty(EXCEPTION_CLASSES, exClasses);
         
         for (Iterator itr = context.getServices().entrySet().iterator(); itr.hasNext();)
         {
             Map.Entry entry = (Map.Entry) itr.next();
 
             generate(context, (QName) entry.getKey(), (List<Service>) entry.getValue());
         }
     }
 
     public void generate(GenerationContext context, QName name, List<Service> services)
         throws Exception
     {
         for (Service service : services)
         {
             for (Iterator itr = service.getServiceInfo().getOperations().iterator(); itr.hasNext();)
             {
                 OperationInfo op = (OperationInfo) itr.next();
                 
                 for (Iterator fitr = op.getFaults().iterator(); fitr.hasNext();)
                 {
                     FaultInfo fault = (FaultInfo) fitr.next();
                     
                     List messageParts = fault.getMessageParts();
                     
                     MessagePartInfo part = (MessagePartInfo) messageParts.get(0);
                     
                     if (!exClasses.containsKey(fault.getMessageName()))
                     {
                         JClass excls = generateExceptionClass(context, fault, part);
                         
                         exClasses.put(fault.getMessageName(), excls);
                     }
                 }
             }
         }
     }
 
     protected JClass generateExceptionClass(GenerationContext context, FaultInfo fault, MessagePartInfo part)
         throws GenerationException
     {
         JCodeModel model = context.getCodeModel();
         SchemaSupport schema = context.getSchemaGenerator();
         
         String name = javify(fault.getMessageName().getLocalPart());
         name = name.substring(0, 1).toUpperCase() + name.substring(1);
         JType paramType = schema.getType(context, part.getName(), part.getSchemaType().getSchemaType());
 
         String clsName = getPackage(fault.getOperation().getService().getService().getName(), context) + "." + name;
         
         JDefinedClass exCls;
         try 
         {
             exCls = model._class(clsName);
             
         } 
         catch (JClassAlreadyExistsException e) 
         {
             clsName = clsName + "_Exception";
             try
             {
                 exCls = model._class(clsName);
             }
             catch (JClassAlreadyExistsException e1)
             {
                 // not sure if this is the right thing to do or not.
                 exCls = model._getClass(clsName);
                 return exCls;
             }
         }
         
         
         return generateExceptionClass(part, model, paramType, exCls);
     }
 
     protected JClass generateExceptionClass(MessagePartInfo part, JCodeModel model, JType paramType, JDefinedClass exCls)
     {
         exCls._extends(FaultInfoException.class);
         
         exCls.field(JMod.PRIVATE, paramType, "faultInfo");
         
         JMethod getFaultInfo = exCls.method(JMod.PUBLIC, paramType, "getFaultInfo");
         getFaultInfo.body()._return(JExpr.ref("faultInfo"));
 
         JMethod cons = exCls.constructor(JMod.PUBLIC);
         cons.param(String.class, "message");
         cons.param(paramType, "faultInfo");
         cons.body().invoke("super").arg(JExpr.ref("message"));
         cons.body().assign(JExpr.refthis("faultInfo"), JExpr.ref("faultInfo"));
         
         cons = exCls.constructor(JMod.PUBLIC);
         cons.param(String.class, "message");
         cons.param(Throwable.class, "t");
         cons.param(paramType, "faultInfo");
         cons.body().invoke("super").arg(JExpr.ref("message")).arg(JExpr.ref("t"));
         cons.body().assign(JExpr.refthis("faultInfo"), JExpr.ref("faultInfo"));
                 
         JClass qType = model.ref(QName.class);
         JMethod getName = exCls.method(JMod.PUBLIC + JMod.STATIC, qType, "getFaultName");
         getName.body()._return(
                 JExpr._new(qType).arg(part.getName().getNamespaceURI()).arg(part.getName().getLocalPart()));
 
         return exCls;
     }
 
 }
