 package lab.mobile.ntu;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import dalvik.system.DexClassLoader;
 import android.app.Service;
 import android.content.Intent;
 import android.os.IBinder;
 import android.util.Log;
 
 public class typeCheckerService extends Service {
     public static final String TYPE_CHECKER_ACTION = "lab.mobile.ntu.TYPE_CHECKER";
     public static final String LOG_TAG = "typeChecker";
     public static final String LOG_TAG_RESULT = "typeCheckerResult";
     String _appName;
     String _className;
     String _methodName;
     String _fieldName;
     String[] _parameters = {};
     boolean _found = false;
     boolean _private;
     String _returnType = "NotFound";
     String _comp1 = null;
     String _comp2 = null;
     ClassLoader classloader;
 
     @Override
     public IBinder onBind(Intent intent) {
         // TODO Auto-generated method stub
         Log.d(LOG_TAG, "onBind");
         return null;
     }
 
     @Override
     public void onCreate() {
         // TODO Auto-generated method stub
         Log.d(LOG_TAG, "------  onCreate    -------");
         classloader = this.getClassLoader();
         super.onCreate();
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         // TODO Auto-generated method stub
         Log.d(LOG_TAG, "------onStartCommand-------");
         _returnType = "NotFound";
         _className = intent.getStringExtra("classname");
         _methodName = intent.getStringExtra("methodname");
         _fieldName = intent.getStringExtra("fieldname");
         _appName = intent.getStringExtra("appname");
         _private = intent.getBooleanExtra("private", true);
         String paras = intent.getStringExtra("parameter");
         _comp1 = intent.getStringExtra("comp1");
         _comp2 = intent.getStringExtra("comp2");
         if (_appName != null) {
             Log.d(LOG_TAG, "appname: " + _appName);
             classloader = new DexClassLoader(_appName, getDir("dex", 0)
                     .getAbsolutePath(), null, classloader);
         }
         if (_className != null) {
             if (_className.equals("android.content.res.XmlResourceParser")) {
                 _className = "android.util.AttributeSet";
             }
             Log.d(LOG_TAG, "classname: " + _className);
         }
         if (_methodName != null) {
             Log.d(LOG_TAG, "methodname: " + _methodName);
         }
         if (_fieldName != null) {
             Log.d(LOG_TAG, "fieldname: " + _fieldName);
         }
         if (paras != null) {
             Log.d(LOG_TAG, "parameter: " + paras);
             _parameters = paras.split(",");
         }
 
         Class<?> targetClass = classLoader();
 
         if (targetClass != null) {
             if (_methodName != null) {
                 inheritedMethod(targetClass);
                 if (!_found) {
                     _returnType = "NotFound-method";
                 }
             } else if (_fieldName != null) {
                 inheritedField(targetClass);
                 if (!_found) {
                     _returnType = "NotFound-field";
                 }
             }
         }
         else if(_comp1 != null && _comp2 != null) {
             Log.d(LOG_TAG,_comp1 + "," + _comp2);
             if(inheritedCompareFromString( _comp1, _comp2)) {
                 _returnType = "true";
             }
             else {
                 _returnType = "false";
             }
         }
         Log.d(LOG_TAG_RESULT, _returnType);
         stopSelf();
         return super.onStartCommand(intent, flags, startId);
     }
 
     boolean inheritedCompareFromString(String par1, String par2) {
         boolean result = false;
         // Log.d(LOG_TAG, par1.getName() +"  ->  "+ par2);
         if (par1.equals(par2)
                 || par1.equals("java.lang.Class")) {
             result = true;
         } else {
             try {
                 Pattern pattern = Pattern.compile("(\\[+)L(.*);");
                 Matcher matcher1 = pattern.matcher(par1);
                 Matcher matcher2 = pattern.matcher(par2);
                 // Log.d(LOG_TAG, par1.getName() + " " + par2);
                 if (matcher1.matches() && matcher2.matches()) {
                    if (matcher1.group(1).equals(matcher2.group(1))
                            && matcher1.group(2).equals("java.lang.Object")) {
                        result = true;
                     } else {
                         result = false;
                     }
                 } else {
 
                     Class<?> superPar2 = classloader.loadClass(par2);
                     for (Class<?> interfaceType : superPar2.getInterfaces()) {
                         if (interfaceType.getName().equals(par1)) {
                             result = true;
                             break;
                         }
                     }
                     if (!result) {
                         superPar2 = superPar2.getSuperclass();
                         if (superPar2 != null) {
                             result = inheritedCompareFromString(par1, superPar2.getName());
                         }
                     }
                     // else {
                     //
                     // }
                 }
             } catch (ClassNotFoundException e) {
                 // TODO Auto-generated catch block
                 if (par2.equals("null")) {
                     return true;
                 } else if (par1.equals("boolean")
                         && par2.equals("int")) {
                     return true;
                 }
                 // Log.d(LOG_TAG, "class not found");
             }
         }
         return result;
     }
 
     Class<?> classLoader() {
         Class<?> targetClass = null;
 
         try {
             targetClass = classloader.loadClass(_className);
         } catch (ClassNotFoundException e) {
             // TODO Auto-generated catch block
             Log.d(LOG_TAG, "NotFound-class");
             return null;
         } catch (UnsatisfiedLinkError e) {
             Log.d(LOG_TAG, "NotFound-JNI error");
             return null;
         } catch (NoClassDefFoundError e) {
             Log.d(LOG_TAG, "NotFound-NoDef exception");
             return null;
         } catch (RuntimeException e) {
             Log.d(LOG_TAG, "NotFound-Runtime exception");
             return null;
         }
         return targetClass;
     }
 
     void inheritedMethod(Class<?> targetClass) {
         if (targetClass == null)
             return;
         Method[] allMethodInClass = null;
         if(_private) {
             allMethodInClass = targetClass.getDeclaredMethods();
         }
         else {
             allMethodInClass = targetClass.getMethods();
         }
         for (Method method : allMethodInClass) {
             String methodName = method.getName();
             if (methodName.equals(_methodName)) {
                 if (compareParameter(method.getParameterTypes(), _parameters)) {
                     _found = true;
                     _returnType = method.getReturnType().getName();
                     return;
                 }
             }
         }
         inheritedMethod(targetClass.getSuperclass());
     }
 
     void inheritedField(Class<?> targetClass) {
         if (targetClass == null)
             return;
         try {
             Field field = null;
             if(_private) {
                 field = targetClass.getDeclaredField(_fieldName);
             }
             else {
                 field = targetClass.getField(_fieldName);
             }
             _returnType = field.getType().getName();
             _found = true;
         } catch (Exception e) {
             // TODO Auto-generated catch block
             inheritedField(targetClass.getSuperclass());
         }
     }
 
     boolean compareParameter(Class<?>[] parameter1, String[] parameter2) {
         if (parameter1.length == parameter2.length) {
             for (int i = 0; i < parameter1.length; ++i) {
                 Log.d(LOG_TAG, parameter1[i].getName() + "-" + parameter2[i]);
                 if (!inheritedCompare(parameter1[i], parameter2[i])) {
                     return false;
                 }
             }
             return true;
         } else {
             return false;
         }
     }
 
     boolean inheritedCompare(Class<?> par1, String par2) {
         boolean result = false;
         // Log.d(LOG_TAG, par1.getName() +"  ->  "+ par2);
         if (par1.getName().equals(par2)
                 || par1.getName().equals("java.lang.Class")) {
             result = true;
         } else {
             try {
                 Pattern pattern = Pattern.compile("(\\[+)L(.*);");
                 Matcher matcher1 = pattern.matcher(par1.getName());
                 Matcher matcher2 = pattern.matcher(par2);
                 // Log.d(LOG_TAG, par1.getName() + " " + par2);
                 if (matcher1.matches() && matcher2.matches()) {
                    if (matcher1.group(1).equals(matcher2.group(1))
                            && matcher1.group(2).equals("java.lang.Object")) {
                        result = true;
                     } else {
                         result = false;
                     }
                 } else {
 
                     Class<?> superPar2 = classloader.loadClass(par2);
                     for (Class<?> interfaceType : superPar2.getInterfaces()) {
                         if (interfaceType.getName().equals(par1.getName())) {
                             result = true;
                             break;
                         }
                     }
                     if (!result) {
                         superPar2 = superPar2.getSuperclass();
                         if (superPar2 != null) {
                             result = inheritedCompare(par1, superPar2.getName());
                         }
                     }
                     // else {
                     //
                     // }
                 }
             } catch (ClassNotFoundException e) {
                 // TODO Auto-generated catch block
                 if (par2.equals("null")) {
                     return true;
                 } else if (par1.getName().equals("boolean")
                         && par2.equals("int")) {
                     return true;
                 }
                 // Log.d(LOG_TAG, "class not found");
             }
         }
         return result;
     }
 }
 
