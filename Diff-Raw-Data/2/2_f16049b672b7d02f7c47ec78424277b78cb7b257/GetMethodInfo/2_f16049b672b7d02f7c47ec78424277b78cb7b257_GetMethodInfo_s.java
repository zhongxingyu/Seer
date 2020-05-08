 
 package org.jbundle.app.program.manual.util;
 
 /**
  *  WriteJava
  *  Copyright (c) 2005 jbundle.org. All rights reserved.
  */
 import org.jbundle.base.db.RecordOwner;
 import org.jbundle.base.field.BaseField;
 import org.jbundle.base.util.DBConstants;
 import org.jbundle.app.program.db.ClassInfo;
 import org.jbundle.app.program.db.LogicFile;
 import org.jbundle.model.DBException;
 
 
 //*******************************************************************
 //  GetMethodInfo -
 //*******************************************************************
 // Required:
 //  valid m_LogicFile - method for the class to get info about
 // Returns:
 //  valid m_LogicFile - with correct interface
     
 public class GetMethodInfo extends Object
 {
     LogicFile m_LogicFile;
     ClassInfo m_ClassInfo;
     
     public GetMethodInfo(RecordOwner recordOwner)
     {
         m_LogicFile = null;
         m_ClassInfo = null;
         LogicFile logicFile = new LogicFile(recordOwner);
         m_LogicFile = logicFile;
         m_LogicFile.setKeyArea(LogicFile.kMethodClassNameKey);
         ClassInfo classInfo = new ClassInfo(recordOwner);
         m_ClassInfo = classInfo;
     }
     public void free()
     {
         if (m_LogicFile != null)
             m_LogicFile.free();
         if (m_ClassInfo != null)
             m_ClassInfo.free();
     }
     public void getTheMethodInfo(LogicFile logicFile, MethodInfo methodInfo)
     {
         try   {
             String methodClass, strMethodName, strBaseClass;
             strMethodName = logicFile.getField(LogicFile.kMethodName).getString();
             methodClass = logicFile.getField(LogicFile.kMethodClassName).getString();
         
         //d   m_ClassInfo.AddNew();
             m_ClassInfo.getField(ClassInfo.kClassName).setString(methodClass);      // Class of this record
             m_ClassInfo.setKeyArea(ClassInfo.kClassNameKey);
             while (m_ClassInfo.seek("="))
             {
         //d     m_LogicFile.AddNew();
                 strBaseClass = m_ClassInfo.getField(ClassInfo.kBaseClassName).getString();
                 m_LogicFile.getField(LogicFile.kMethodName).setString(strMethodName);
                 if (strMethodName == methodClass) // For method initialization, use ClassName
                     m_LogicFile.getField(LogicFile.kMethodName).setString(strBaseClass);
                 m_LogicFile.getField(LogicFile.kMethodClassName).setString(strBaseClass);
                 m_LogicFile.setKeyArea(LogicFile.kMethodClassNameKey);
                 if (m_LogicFile.seek("="))
                     this.moveupMethodInfo(logicFile, false);
         //d     m_ClassInfo.AddNew();
                 m_ClassInfo.getField(ClassInfo.kClassName).setString(strBaseClass);     // Class of this record
             }
             if (!logicFile.getField(LogicFile.kCopyFrom).isNull())
             {   // Copy code from another class
                 m_LogicFile.getField(LogicFile.kMethodName).setString(strMethodName);
                 if (strMethodName == methodClass) // For method initialization, use ClassName
                     m_LogicFile.getField(LogicFile.kMethodName).moveFieldToThis(logicFile.getField(LogicFile.kCopyFrom));
                 m_LogicFile.getField(LogicFile.kMethodClassName).moveFieldToThis(logicFile.getField(LogicFile.kCopyFrom));
                 m_LogicFile.setKeyArea(LogicFile.kMethodClassNameKey);
                 if (m_LogicFile.seek("="))
                     this.moveupMethodInfo(logicFile, true);                
             }
             methodInfo.strHeaderInterface = logicFile.getField(LogicFile.kMethodInterface).getString();
             if (methodInfo.strHeaderInterface.length() > 150)
             {
                 methodInfo.strMethodReturns = methodInfo.strHeaderInterface.substring(150, methodInfo.strHeaderInterface.length());
                 int pos = methodInfo.strMethodReturns.indexOf(' ');
                 if (pos != -1)
                     methodInfo.strHeaderInterface = methodInfo.strHeaderInterface.substring(0, pos+150) + "\n" + methodInfo.strHeaderInterface.substring(pos+150, methodInfo.strHeaderInterface.length());
             }
             methodInfo.strMethodReturns = logicFile.getField(LogicFile.kMethodReturns).getString();   // Value returned
             methodInfo.strMethodThrows = logicFile.getField(LogicFile.kLogicThrows).getString();    // Value returned
             if (methodInfo.strMethodReturns.length() == 0)
                 methodInfo.strMethodReturns = "void";
             if (strMethodName.equalsIgnoreCase("finalize"))
                 strMethodName = "~C" + methodClass;   // No return on ~delete method
             if (strMethodName.length() != 0) if (strMethodName.charAt(0) == '~')
                 methodInfo.strMethodReturns = ""; // No return on ~delete method
         // This line really should read the base classes' method interface, not this classes' (only for overrides)
             if (methodInfo.strHeaderInterface.length() == 0) if (strMethodName == methodClass)
                 methodInfo.strHeaderInterface = "Record record, String strName, int iDataLength, String strDesc, Object strDefault";
             if (methodInfo.strHeaderInterface.length() == 0)
                 methodInfo.strHeaderInterface = "void";
             methodInfo.strMethodInterface = methodInfo.strHeaderInterface;
         
             if (methodInfo.strMethodInterface.equalsIgnoreCase("void"))
                 methodInfo.strMethodInterface = "";
         
         // Get rid of all the default assignments for the method version (ie., = xxx)
             while (true)
             {
                 int equalSign = methodInfo.strMethodInterface.indexOf("=");
                 if (equalSign == -1)
                     break;          // No more "="s
                 if (methodInfo.strMethodInterface.charAt(equalSign - 1) == ' ')
                     equalSign -= 1;
                 int j = 0;
                 for (j = equalSign + 1; j < methodInfo.strMethodInterface.length(); ++j)
                     {
                     if (methodInfo.strMethodInterface.charAt(j) == ',')
                         break;
                     }
                 int rightChars = methodInfo.strMethodInterface.length() - j;
                 if (rightChars < 0)
                     rightChars = 0;
                 methodInfo.strMethodInterface = methodInfo.strMethodInterface.substring(1, equalSign) + methodInfo.strMethodInterface.substring(methodInfo.strMethodInterface.length() - rightChars, methodInfo.strMethodInterface.length());
             }
         } catch (DBException e)   {
         }
     }
     //*******************************************************************
     //  MoveupMethodInfo
     //*******************************************************************
     //
     public void moveupMethodInfo(LogicFile logicFile, boolean bCopySource)
     {
         BaseField field;
         String thisFieldStr;
         int count = logicFile.getFieldCount() + DBConstants.MAIN_FIELD;
         for (int i = DBConstants.MAIN_FIELD; i < count; i++)
         {
             if (i == LogicFile.kLogicSource)
                 if (!bCopySource)
                     continue;
             field = logicFile.getField(i);
             if (field.getString().length() == 0)
             {
                 thisFieldStr = m_LogicFile.getField(i).getString();
                 if (thisFieldStr.length() != 0)
                     field.setString(thisFieldStr);
             }
         }
     }
 }
