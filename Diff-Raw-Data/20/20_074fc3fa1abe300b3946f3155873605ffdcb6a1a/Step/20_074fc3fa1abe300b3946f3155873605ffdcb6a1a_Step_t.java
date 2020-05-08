 
 package com.openfeint.qa.core.caze.step;
 
 import com.openfeint.qa.core.caze.TestCase;
 import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
 import com.openfeint.qa.core.util.StringUtil;
 import com.openfeint.qa.core.util.type.TypeConversionException;
 import com.openfeint.qa.core.util.type.TypeConverter;
 
 import android.util.Log;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.concurrent.Semaphore;
 import java.util.concurrent.TimeUnit;
 
 import junit.framework.AssertionFailedError;
 
 /***
  * @author thunderzhulei
  * @category reflect a single text step defined in feature to a real java method
  *           in StepDefinition.java. including execute current step itself.
  */
 public class Step implements Observer {
     private String command;
 
     private String keyword;
 
     private boolean wait = false;
 
    private Semaphore crossStepSync = new Semaphore(0, true);
 
     public String getKeyword() {
         return keyword;
     }
 
     public void setKeyword(String keyword) {
         this.keyword = keyword;
     }
 
     public String getCommand() {
         return command;
     }
 
     public void setCommand(String command) {
         this.command = command;
     }
 
     private Class<?> ref_class;
 
     private Method ref_method;
 
     private Type[] ref_method_param_types;
 
     public Class<?> getRef_class() {
         return ref_class;
     }
 
     public void setRef_class(Class<?> refClass) {
         ref_class = refClass;
     }
 
     public Method getRef_method() {
         return ref_method;
     }
 
     public void setRef_method(Method refMethod) {
         ref_method = refMethod;
     }
 
     public Type[] getRef_method_param_types() {
         return ref_method_param_types;
     }
 
     public void setRef_method_param_types(Type[] refMethodParamTypes) {
         ref_method_param_types = refMethodParamTypes;
     }
 
     public Object[] getRef_method_params() {
         return ref_method_params;
     }
 
     public void setRef_method_params(Object[] refMethodParams) {
         ref_method_params = refMethodParams;
     }
 
     private Object[] ref_method_params;
 
     @SuppressWarnings("finally")
     public synchronized StepResult invoke() {
         int res = TestCase.RESULT.FAILED;
         String comm = "";
         try {
             Log.v(StringUtil.DEBUG_TAG,
                     "invoking [" + command + "] with step [" + ref_class.getName() + "."
                             + ref_method.getName() + "]");
             Object stepDefinition = this.getRef_class().newInstance();
             ((BasicStepDefinition) stepDefinition).addObserver(this);
 
             this.ref_method.invoke(stepDefinition, this.buildRef_Params());
             if (wait) {
                 String exp = "timeout exception in step [" + ref_class.getName() + "."
                         + ref_method.getName() + "]";
                 try {
                     boolean t = crossStepSync.tryAcquire(1, 10000, TimeUnit.MILLISECONDS);
                     if (!t) {
                        crossStepSync = new Semaphore(0, true);
                         throw new InvocationTargetException(new Exception(exp), exp);
                     }
                 } catch (InterruptedException e) {
                    crossStepSync = new Semaphore(0, true);
                     throw new InvocationTargetException(new Exception(exp), exp);
                 }
             }
             res = TestCase.RESULT.PASSED;
         } catch (IllegalArgumentException e) {
             Log.e(StringUtil.DEBUG_TAG, e.getCause().getMessage());
         } catch (IllegalAccessException ek) {
             Log.e(StringUtil.DEBUG_TAG, ek.getCause().getMessage());
         } catch (InvocationTargetException er) {
             // most case failed reason raised here
             comm = command + " ==> " + er.getCause().getMessage();
             // for NullPointointException use
             er.printStackTrace();
             Log.e(StringUtil.DEBUG_TAG, comm);
            
         } finally {
             return new StepResult(res, comm);
         }
     }
 
     private Object[] buildRef_Params() throws TypeConversionException {
         ArrayList<Object> raw_params = new ArrayList<Object>();
         for (int i = 0; i < this.getRef_method_param_types().length; i++) {
             raw_params.add(TypeConverter.convertSimpleType(this.getRef_method_params()[i],
                     this.getRef_method_param_types()[i]));
         }
         return raw_params.toArray();
     }
 
     @Override
     public void update(Observable observable, Object data) {
         // TODO Auto-generated method stub
         if (((String) data).equals("WAIT_SIG")) {
             wait = true;
         } else if (((String) data).equals("NOTIFY_SIG")) {
             crossStepSync.release(1);
         }
     }
 }
