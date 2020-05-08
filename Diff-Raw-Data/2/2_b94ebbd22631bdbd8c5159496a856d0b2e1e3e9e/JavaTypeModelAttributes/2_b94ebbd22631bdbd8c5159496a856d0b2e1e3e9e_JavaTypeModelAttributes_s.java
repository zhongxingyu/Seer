 /*
  * JavaTypeModelAttributes.java
  *
  * Created on April 22, 2007, 11:25 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 
 package ua.gradsoft.javachecker.attributes;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.PrintWriter;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import ua.gradsoft.javachecker.EntityNotFoundException;
 import ua.gradsoft.javachecker.FileAndLine;
 import ua.gradsoft.javachecker.JUtils;
 import ua.gradsoft.javachecker.Main;
 import ua.gradsoft.javachecker.NotSupportedException;
 import ua.gradsoft.javachecker.models.*;
 import ua.gradsoft.javachecker.models.expressions.JavaObjectConstantExpressionModel;
 import ua.gradsoft.termware.Term;
 import ua.gradsoft.termware.TermHelper;
 import ua.gradsoft.termware.TermWare;
 import ua.gradsoft.termware.TermWareException;
 import ua.gradsoft.termware.exceptions.AssertException;
 
 /**
  *Such object is implicitly binded with each loaded Java type.
  * @author rssh
  */
 public class JavaTypeModelAttributes {
     
     
     public JavaTypeModelAttributes(JavaTypeModel owner) {
         owner_=owner;
     }
     
     public JavaTypeModel  getTypeModel() {
         return owner_;
     }
     
     
     public AttributesData  getData() throws TermWareException {
         if(!isLoaded()) {
             load();
         }
         return data_;
     }
     
     /**
      *get Attribute for type with name <code> name </code> if one was defined,
      *otherwise return null.
      */
     public Term  getTypeAttribute(String name) throws TermWareException {
         if (!isLoaded()) {
             load();
         }
         return data_.getAttribute(name);
     }
     
     public void  setTypeAttribute(String name, Term value) throws TermWareException {
         if (!isLoaded()) {
             load();
         }
         data_.getGeneralAttributes().put(name,value);
     }
     
     
     public Term findInheriedTypeAttribute(String name) throws TermWareException {
         Term retval = getTypeAttribute(name);
         if (retval!=null) {
             if (!retval.isNil()) {
                 return retval;
             }
         }
         if (owner_.isClass()) {
             JavaTypeModel superOwner = null;
             try {
                 superOwner=owner_.getSuperClass();
                 retval = superOwner.getAttributes().findInheriedTypeAttribute(name);
             }catch(NotSupportedException ex){
                 /* do nothing */;
             }catch(EntityNotFoundException ex){
                 throw new AssertException("Unable to get superclass",ex);
             }
             if (retval!=null) {
                 if (!retval.isNil()) {
                     data_.getGeneralAttributes().put(name,retval);
                     return retval;
                 }
             }
             try {
                 List<JavaTypeModel> superInterfaces = owner_.getSuperInterfaces();
                 for(JavaTypeModel si: superInterfaces) {
                     retval = si.getAttributes().findInheriedTypeAttribute(name);
                     if (retval!=null) {
                         if (!retval.isNil()) {
                             data_.getGeneralAttributes().put(name,retval);
                         }
                     }
                 }
             }catch(NotSupportedException ex){
                 ;
             }
         }else if (owner_.isInterface()) {
             try {
                 List<JavaTypeModel> superInterfaces = owner_.getSuperInterfaces();
                 for(JavaTypeModel si: superInterfaces) {
                     retval = si.getAttributes().findInheriedTypeAttribute(name);
                     if (retval!=null) {
                         if (!retval.isNil()) {
                             data_.getGeneralAttributes().put(name,retval);
                             return retval;
                         }
                     }
                 }
             }catch(NotSupportedException ex){
                 throw new AssertException("Impossible",ex);
             }
         }
         return retval;
     }
     
     
     public Term getTopLevelBlockOwnerAttribute(JavaTopLevelBlockOwnerModel blockOwner,String name) throws TermWareException
     {
         return getChildAttribute(JavaTopLevelBlockOwnerModelHelper.getStringSignature(blockOwner),name);
     }
     
     public void setTopLevelBlockOwnerAttribute(JavaTopLevelBlockOwnerModel blockOwner,String name,Term value)  throws TermWareException
     {
         setChildAttribute(JavaTopLevelBlockOwnerModelHelper.getStringSignature(blockOwner),name,value);
     }
     
     public AttributesData getTopLevelBlockOwnerChildAttributes(JavaTopLevelBlockOwnerModel blockOwner,String childName)  throws TermWareException
     {
         return getChildChildAttributes(JavaTopLevelBlockOwnerModelHelper.getStringSignature(blockOwner),childName);
     }
     
     
     public Term  getMethodAttribute(JavaMethodModel methodModel, String name)  throws TermWareException
     {
         return getTopLevelBlockOwnerAttribute(methodModel,name);
     }
     
     public void  setMethodAttribute(JavaMethodModel methodModel, String name, Term value)  throws TermWareException
     {
         setTopLevelBlockOwnerAttribute(methodModel,name,value);
     }
     
     public Term  getConstructorAttribute(JavaConstructorModel constructorModel, String name)  throws TermWareException
     {
         return getTopLevelBlockOwnerAttribute(constructorModel,name);
     }
     
     public void  setConstructorAttribute(JavaConstructorModel constructorModel, String name, Term value)  throws TermWareException
     {
         setTopLevelBlockOwnerAttribute(constructorModel,name,value);
     }
     
     public Term  getFieldAttribute(String fieldName, String name)  throws TermWareException
     {
         return getChildAttribute(fieldName,name);
     }
     
     public void  setFieldAttribute(String fieldName, String name, Term value)  throws TermWareException
     {
         setChildAttribute(fieldName,name,value);
     }
     
   
     public void print(PrintWriter out) {
         if (!isLoaded()) {
             try {
                 load();
             }catch(TermWareException ex){
                 out.println("(error during loading:"+ex.getMessage());
                 ex.printStackTrace(out);
                 out.println(")");
                 return;
             }
         }
         data_.print(out);
     }
     
     private Term getChildAttribute(String childName, String name) throws TermWareException
     {
        if (!isLoaded()) {        
              load();
         }        
         AttributesData child = data_.getChilds().get(childName);
         if (child==null) {
             return TermUtils.createNil();
         }else{
             Term retval = child.getGeneralAttributes().get(name);
             return retval==null ? TermUtils.createNil() : retval;
         }
     }
     
     private void setChildAttribute(String childName,String name,Term value) throws TermWareException
     { 
        if (!isLoaded()) {
             load();
         }        
         HashMap<String,AttributesData> childs = data_.getChilds();
         if (childs==null) {
             childs=new HashMap<String,AttributesData>();
             data_.setChilds(childs);            
         }
         AttributesData child = childs.get(childName);
         if (child==null) {
             child=new AttributesData();
             data_.getChilds().put(childName,child);
         }
         child.getGeneralAttributes().put(name,value);
     }
     
     private AttributesData getChildChildAttributes(String childName, String childChildName) throws TermWareException
     {
       if (!isLoaded())  {
           load();
       }      
       AttributesData childs = data_.getOrCreateChild(childName);
       AttributesData childsChilds = childs.getOrCreateChild(childChildName);
       return childsChilds;      
     }
     
     protected void finalize() {
         if (!owner_.isNested()) {
             if (!Main.isInShutdown()) {
                 synchronized(this) {
                     try {
                         save();
                     }catch(TermWareException ex){
                         LOG.log(Level.WARNING,"exception during saving properties",ex);
                     }
                 }
             }
         }
     }
     
     
     
     private boolean isLoaded() {
         return data_!=null;
     }
     
     private synchronized void load() throws TermWareException {
         if (!isLoaded()) {
             if (owner_.isNested()) {
                 try {
                     data_=owner_.getEnclosedType().getAttributesData().getOrCreateChild(owner_.getName());
                 }catch(NotSupportedException ex){
                     throw new AssertException("isNested is true, but getEnclosedType is not supported in "+owner_.getFullName());
                 }
             }else{
                 String fullLoadName = createAttributesFileName();
                 File f = new File(fullLoadName);
                 if (f.exists()) {
                     ObjectInputStream oi = null;
                     try {
                         oi=new ObjectInputStream(new FileInputStream(f));
                     }catch(FileNotFoundException ex){
                         LOG.log(Level.WARNING,"File.exists() is true, but file not found",ex);
                         throw new AssertException("Impossible situation during opening file "+fullLoadName,ex);
                     }catch(IOException ex){
                         throw new AssertException("Unable to create object stream from "+f.getAbsoluteFile(),ex);
                     }
                     Object o=null;
                     try {
                         o = oi.readObject();
                     }catch(IOException ex){
                         throw new AssertException("Error during reading object stream",ex);
                     }catch(ClassNotFoundException ex){
                         throw new AssertException("Error during reading object stream",ex);
                     }finally{
                         try {
                             oi.close();
                         }catch(IOException ex){
                             LOG.log(Level.WARNING,"exception during closing object stream",ex);
                         }
                     }
                     if (o!=null) {
                         if (o instanceof AttributesData) {
                             data_ = (AttributesData)o;
                         }else{
                             throw new AssertException("Type of object in "+fullLoadName+" is not JavaTypeModelAttributesData");
                         }
                     }
                 }
             }
         }
         if (data_==null) {
             data_=new AttributesData();
             try {
                 loadConfigTypeAttributes();
             }catch(TermWareException ex){
                 LOG.log(Level.WARNING,"exception during loading attributes",ex);
             }catch(EntityNotFoundException ex){
                 LOG.log(Level.WARNING,"exception during loading attributes", ex);
             }
             try {
                 data_.merge(loadSourceAttributes());
             }catch(TermWareException ex){
                 LOG.log(Level.WARNING,"exception dureing reading source attributes",ex);
             }catch(EntityNotFoundException ex){
                 LOG.log(Level.WARNING,"exception dureing reading source attributes",ex);
             }
         }
     }
     
     private synchronized void save() throws TermWareException {
         if (isLoaded()) {          
             
             if (data_.isEmpty()) {
                 return;
             }
             
             String fullLoadName = createAttributesFileName();
             File f = new File(fullLoadName);
                         
             if (!f.exists()) {
                 try {                    
                     f.createNewFile();
                 }catch(IOException ex){
                     throw new AssertException("Can't create file "+f.getAbsolutePath(),ex);
                 }
                 f.deleteOnExit();
             }
             
             ObjectOutputStream oo=null;
             try {
                 oo=new ObjectOutputStream(new FileOutputStream(f));
                 oo.writeObject(data_);
             }catch(FileNotFoundException ex){
                 throw new AssertException("Can't open file "+f.getAbsolutePath()+" for writing",ex);
             }catch(IOException ex){
                 throw new AssertException("Can't output object to file "+f.getAbsolutePath(),ex);
             }finally{
                 if (oo!=null) {
                     try {
                         oo.close();
                     }catch(IOException ex){
                         LOG.log(Level.WARNING,"exception diring closing just-writed swp file",ex);
                     }
                 }
             }
         }
     }
     
     
     
     private void loadConfigTypeAttributes() throws TermWareException, EntityNotFoundException {
         AttributesData data = Main.getFacts().getAttributesStorage().readConfiguratedAttributes(owner_);
         if (data_==null) {
             data_=data;
         }else{
             data_.merge(data);
         }
     }
     
     private AttributesData loadSourceAttributes() throws TermWareException, EntityNotFoundException {
         AttributesData retval = new AttributesData();        
         loadSourceAttributes(owner_,retval);
         return retval;
     }
     
     private void loadSourceAttributes(JavaTypeModel tm, AttributesData data) throws TermWareException, EntityNotFoundException {
         JavaAnnotationInstanceModel typeAnnotation = null ;
         try {
             typeAnnotation = tm.getAnnotation("ua.gradsoft.javachecker.annotations.TypeCheckerProperties");
         }catch(NotSupportedException ex){
             // all is ok
             ;
         }
         if (typeAnnotation!=null) {
             if (typeAnnotation.hasElement("value")) {
                 try {
                     fillSourceValueExpression(data,typeAnnotation.getElement("value"));
                 }catch(NotSupportedException ex){
                     throw new AssertException("element value must be defined in annotation ");
                 }
             }
         }
         Map<String,JavaTypeModel> nestedTypeModels=null;
         try {
             nestedTypeModels=tm.getNestedTypeModels();
         }catch(NotSupportedException ex){
             nestedTypeModels=Collections.emptyMap();
         }
         for(Map.Entry<String,JavaTypeModel> ntm: nestedTypeModels.entrySet()) {
             AttributesData next = new AttributesData();
             loadSourceAttributes(ntm.getValue(),next);
             if (!next.isEmpty()) {
                 AttributesData nd = data.getOrCreateChild(ntm.getKey());
                 nd.merge(next);
             }
         }
         List<JavaConstructorModel> constructors = tm.getConstructorModels();
         for(JavaConstructorModel cn: constructors) {
             JavaAnnotationInstanceModel ann = cn.getAnnotationsMap().get("ua.gradsoft.javachecker.annotations.ConstructorCheckerProperties");
             if (ann==null) {
                 continue;
             }
             if (ann.hasElement("value")) {
                 String signature = JavaTopLevelBlockOwnerModelHelper.getStringSignature(cn);
                 AttributesData nd = data.getOrCreateChild(signature);
                 try {
                     fillSourceValueExpression(nd,ann.getElement("value"));
                 }catch(NotSupportedException ex){
                     throw new AssertException("value element must be present",ex);
                 }
                 List<JavaFormalParameterModel> fps = null;
                 try {
                     fps=cn.getFormalParametersList();
                 }catch(EntityNotFoundException ex){
                     throw new AssertException(ex.getMessage(),ex);
                 }
                 for(JavaFormalParameterModel fp: fps) {
                     JavaAnnotationInstanceModel fpa = fp.getAnnotationsMap().get("ua.gradsoft.javachecker.annotations.ParameterCheckerProperties");
                     if (fpa==null) {
                         continue;
                     }
                     AttributesData fpd = nd.getOrCreateChild(fp.getName());
                     try {
                         fillSourceValueExpression(fpd,fpa.getElement("value"));
                     }catch(NotSupportedException ex){
                         throw new AssertException("Value annotation must be present",ex);
                     }
                 }
             }
         }
         Map<String,List<JavaMethodModel>> methodModels = null;
         if (tm.hasMethodModels()) {
           try {
             methodModels=tm.getMethodModels();
           }catch(NotSupportedException ex){
             LOG.log(Level.WARNING,"getMethodModels is unsupported in class "+tm.getFullName());
             methodModels=Collections.emptyMap();
           }
         }else{
            methodModels=Collections.emptyMap(); 
         }
         for(Map.Entry<String,List<JavaMethodModel>> e:methodModels.entrySet()) {
             for(JavaMethodModel m: e.getValue()) {
                 JavaAnnotationInstanceModel ann = m.getAnnotationsMap().get("ua.gradsoft.javachecker.annotation.MethodCheckerProperties");
                 if (ann==null) continue;
                 String signature = JavaTopLevelBlockOwnerModelHelper.getStringSignature(m);
                 AttributesData nd = data.getOrCreateChild(signature);
                 try {
                     fillSourceValueExpression(nd,ann.getElement("value"));
                 }catch(NotSupportedException ex){
                     throw new AssertException("Value element must be present in MethodCheckerProperties",ex);
                 }
                 List<JavaFormalParameterModel> fps = null;
                 try {
                     fps=m.getFormalParametersList();
                 }catch(EntityNotFoundException ex){
                     throw new AssertException(ex.getMessage(),ex);
                 }
                 for(JavaFormalParameterModel fp: fps) {
                     JavaAnnotationInstanceModel fpa = fp.getAnnotationsMap().get("ua.gradsoft.javachecker.annotations.ParameterCheckerProperties");
                     if (fpa==null) {
                         continue;
                     }
                     AttributesData fpd = nd.getOrCreateChild(fp.getName());
                     try {
                         fillSourceValueExpression(fpd,fpa.getElement("value"));
                     }catch(NotSupportedException ex){
                         throw new AssertException("value element must be present in ParameterCheckerProperties annotation",ex);
                     }
                 }
             }
         }
         if (tm.hasMemberVariableModels()) {
           try{  
             Map<String,JavaMemberVariableModel> fieldModels = tm.getMemberVariableModels();
             for(JavaMemberVariableModel v: fieldModels.values()) {
                 JavaAnnotationInstanceModel ann = v.getAnnotationsMap().get("ua.gradsoft.javachecker.annotations.FieldCheckerProperties");
                 if (ann==null) continue;
                 AttributesData nd = data.getOrCreateChild(v.getName());
                 fillSourceValueExpression(nd,ann.getElement("value"));
             } 
           }catch(NotSupportedException ex){
               LOG.log(Level.WARNING,"getMemberVariables is unsupported for "+tm.getFullName(),ex);
           }
         }        
     }
     
     private void fillSourceValueExpression(AttributesData data, JavaExpressionModel expr) throws TermWareException, EntityNotFoundException {
         String svalue=null;
         switch(expr.getKind()) {
             case ANNOTATION_MEMBER_VALUE_ARRAY_INITIALIZER:
             case ARRAY_INITIALIZER:
             {
                 // subexpressions  must be a string literals.
                 boolean isName=true;
                 String name=null;
                 String value=null;
                 List<JavaExpressionModel> subexpressions=null;
                 try{
                     subexpressions=expr.getSubExpressions();
                 }catch(EntityNotFoundException ex){
                     FileAndLine fl = ex.getFileAndLine();
                     throw new AssertException(ex.getMessage()+" file "+fl.getFname()+", line "+fl.getLine());
                 }
                 for(JavaExpressionModel e:subexpressions){
                     if (e.getKind()!=JavaExpressionKind.STRING_LITERAL) {
                         throw new AssertException("String literal exprected, have "+e.getKind());
                     }
                     JavaObjectConstantExpressionModel oe=(JavaObjectConstantExpressionModel)e;
                     Object ooe = oe.getConstant();
                     if (ooe instanceof String) {
                         svalue=(String)ooe;
                     }else{
                         throw new AssertException("String expected. internal error");
                     }
                     if (isName) {
                         name=svalue;
                         isName=false;
                     }else{
                         value=svalue;
                         isName=true;
                         Term tvalue = TermWare.getInstance().getTermFactory().createParsedTerm(value);
                         data.getGeneralAttributes().put(name,tvalue);
                     }
                 }
             }
             break;
             default:
                 try {
                     throw new AssertException("TypeCheckerProperty must be array initializer, have "+expr.getKind()+", model="+TermHelper.termToString(expr.getModelTerm()));
                 }catch(EntityNotFoundException ex){
                     throw new AssertException(ex.getMessage()+" "+ex.getFileAndLine().getFname()+","+ex.getFileAndLine().getLine(),ex);
                 }
         }
     }
     
     private String createAttributesFileName()
     {
             String tmpDir = Main.getTmpDir();
             String packageName=owner_.getPackageModel().getName();
             //String dirName=JUtils.createDirectoryNameFromPackageName(Main.getTmpDir(),owner_.getPackageModel().getName());
             String fname = JUtils.createSourceFileNameFromClassName(owner_.getName(),".jcswp");
             String fullLoadName = tmpDir+File.separator+packageName+"_"+fname;          
             // now change <, >, ' ' on codes.
            fullLoadName=fullLoadName.replace("<","_lt_").replace(">","_gt_").replace(" ","_20_");
             return fullLoadName;
     }
     
     private JavaTypeModel        owner_;
     private AttributesData       data_;
     
     
     
     private final static Logger LOG = Logger.getLogger(JavaTypeModelAttributes.class.getName());
     
 }
