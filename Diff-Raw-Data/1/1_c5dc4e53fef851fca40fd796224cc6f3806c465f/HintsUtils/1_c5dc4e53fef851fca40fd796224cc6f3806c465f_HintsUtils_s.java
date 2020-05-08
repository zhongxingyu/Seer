 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.netbeans.modules.javafx.editor.hints;
 
 import com.sun.tools.mjavac.code.Symbol.MethodSymbol;
 import com.sun.tools.javafx.code.JavafxClassSymbol;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.TypeElement;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 import javax.swing.text.JTextComponent;
 import org.netbeans.api.javafx.source.CompilationInfo;
 import org.netbeans.api.javafx.source.ElementUtilities;
 import org.netbeans.editor.Utilities;
 import org.netbeans.modules.javafx.editor.JavaFXDocument;
 
 /**
  *
  * @author karol harezlak
  */
 final class HintsUtils {
 
     static final String TAB = "    "; //NOI18N
     static final String EXCEPTION_UOE = "java.lang.UnsupportedOperationException"; //NOI18N
 
     private HintsUtils() {
     }
 
     static String getMethodName(String fullMethodName) {
         String methodName;
         if (fullMethodName.contains(".")) { //NOI18N
             int start = fullMethodName.lastIndexOf("."); //NOI18N
             int end = fullMethodName.length();
             methodName = fullMethodName.substring(start + 1, end).replace("()", "").trim(); //NOI18N
         } else {
             methodName = fullMethodName;
         }
 
         return methodName.trim();
     }
 
     static String getPackageName(String fqn) {
         String methodName;
         if (fqn.contains(".")) { //NOI18N
             int end = fqn.lastIndexOf("."); //NOI18N
             methodName = fqn.substring(0, end);
         } else {
             methodName = ""; //NOI!8N
         }
 
         return methodName.trim();
     }
 
     static String getClassSimpleName(String fqName) {
         int start = fqName.lastIndexOf(".") + 1; //NOI18N
         if (start > 0) {
             fqName = fqName.substring(start);
         }
         fqName = fqName.replace("{", "").replace("}", ""); //NOI18N
         return fqName.trim();
     }
 
     static boolean checkString(String name) {
         return Pattern.compile("[!@#%^&*(){}\\|:'?/><~`]").matcher(name).find(); //NOI18N
     }
 
     static MethodSymbol isAlreadyDefined(Collection<MethodSymbol> overriddenMethodList, MethodSymbol method, CompilationInfo compilationInfo) {
         if (overriddenMethodList != null && !overriddenMethodList.isEmpty()) {
             for (MethodSymbol overriddenMethod : overriddenMethodList) {
                 String overrriddenName = overriddenMethod.getSimpleName().toString();
                 if (!method.getSimpleName().toString().equals(overrriddenName)) {
                     continue;
                 }
                 TypeElement typeOverridden = ElementUtilities.enclosingTypeElement(overriddenMethod);
                 try {
                     if (compilationInfo.getElementUtilities().alreadyDefinedIn(overrriddenName, method, typeOverridden)) {
                         return overriddenMethod;
                     }
                 } catch (NullPointerException ex) {
                     ex.printStackTrace();
                 }
             }
         }
 
         return null;
     }
     //TODO Should be replaced with proper formating ASAP
     static String calculateSpace(int startPosition, Document document) {
         String text = null;
         try {
             text = document.getText(document.getStartPosition().getOffset(), startPosition);
         } catch (BadLocationException ex) {
             ex.printStackTrace();
             return "";
         }
         int lastIndex = -1;
         if (text != null && text.length() > 1) {
             lastIndex = text.lastIndexOf("\n"); //NOI18N
         }
         int charNumber = -1;
 
         if (lastIndex > 0) {
             int varIndex = 0;
             String line = text.substring(lastIndex, startPosition);
             Pattern pattern = Pattern.compile("[a-z]"); //NOI18N
             Matcher matcher = pattern.matcher(line);
             if (matcher.find()) {
                 varIndex = matcher.start();
                 charNumber = varIndex - 1;
             } else {
                 charNumber = line.length();
             }
 
         }
         if (charNumber < 0) {
             return ""; //NOI18N
         }
         StringBuilder space = new StringBuilder(charNumber);
         for (int i = 0; i < charNumber - 1; i++) {
             space.append(" "); //NOI18M
         }
         return space.toString();
     }
 
     static boolean isAnnon(Element element) {
         if (!(element instanceof JavafxClassSymbol)) {
             return false;
         }
         JavafxClassSymbol classSymbol = ((JavafxClassSymbol) element);
        classSymbol.getSuperTypes();
         if (!classSymbol.isLocal()) {
             return false;
         }
         String name = element.toString();
         int lastIndex = name.lastIndexOf("$"); //NOI18N
         if (lastIndex < 0) {
             return false;
         }
         if (!name.substring(lastIndex).contains("anon")) { //NOI18N
             return false;
         }
 
         return true;
     }
 
     static JTextComponent getEditorComponent(Document document) {
         JTextComponent target = Utilities.getFocusedComponent();
         if (target != null) {
             return target;
         }
         if (document instanceof JavaFXDocument) {
             JavaFXDocument d = (JavaFXDocument) document;
             if (d.getEditor() instanceof JTextComponent) {
                 return (JTextComponent) d.getEditor();
             }
         }
         
         return null;
     }
 }
