 /*
  * This source file is part of CaesarJ 
  * For the latest info, see http://caesarj.org/
  * 
  * Copyright  2003-2005 
  * Darmstadt University of Technology, Software Technology Group
  * Also see acknowledgements in readme.txt
  * 
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  * 
 * $Id: CompileAndCheckErrorTest.java,v 1.3 2005-03-02 17:41:42 gasiunas Exp $
  */
 
 package org.caesarj.test.suite;
 
 import java.lang.reflect.Field;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.caesarj.compiler.constants.CaesarMessages;
 import org.caesarj.compiler.constants.KjcMessages;
 import org.caesarj.util.CWarning;
 import org.caesarj.util.MessageDescription;
 import org.caesarj.util.PositionedError;
 
 /**
  * ...
  * 
  * @author Ivica Aracic
  */
 public class CompileAndCheckErrorTest extends CompileTest {
 
     private String errorCode;
     private MessageDescription msgDesc;
     
     public CompileAndCheckErrorTest(CaesarTestSuite testSuite, String id, String description, String codeBlock, String errorCode) {
         super(testSuite, id, description, codeBlock);
         this.errorCode = errorCode;
         
         if (!errorCode.equals(TestProperties.UNDEF_MESSAGE)) {
         	msgDesc = findMessageDescription(KjcMessages.class, errorCode);
 	        if(msgDesc == null) {
 	            msgDesc = findMessageDescription(CaesarMessages.class, errorCode);
 	            
 	            if(msgDesc == null) {
 	                throw new RuntimeException('"'+errorCode+"\" can not be found in KjcMessages or in CaesarMessages");
 	            }
 	        }
         }
     }
     
     protected MessageDescription findMessageDescription(Class clazz, String fieldName) {
         try {
             Field f = clazz.getField(fieldName);
             return (MessageDescription)f.get(null);
         }
         catch (Exception e) {
             return null;
         }
     }
     
     public void compilerFailed() {
         checkErrors(new MessageDescription[]{msgDesc});
     }
     
     public void compilerSuccess() {
         fail("failed : compiler success: "+getId()+" : "+getDescription());
     }
     
     private void checkErrors(MessageDescription[] expected){
         List errors = new LinkedList();
         for (Iterator iter = positionedErrorList.iterator(); iter.hasNext();) {
             Object o = iter.next();
             // ignore warnings which are a subtype of PositionedError
             if (!(o instanceof CWarning)) {
                 errors.add(o);
             }
         }
         PositionedError[] found = (PositionedError[]) errors.toArray( new PositionedError[0] );
         
         if (errorCode.equals(TestProperties.UNDEF_MESSAGE)) {
         	if (!TestProperties.instance().ignoreUndefMessages()) {
         		fail("failed : undefined message: " + getId() + " : "+getDescription());
         	}
         }
        
        if(found.length == 0)
            fail("No errors found : "+getId()+" : "+getDescription());
        
        if(expected.length != found.length)
            fail("More ("+errors.size()+") errors found than expected: "+getId()+" : "+getDescription());
        
        for (int i = 0; i < found.length; i++) {
            if(!(expected[i] == found[i].getFormattedMessage().getDescription()))
                fail("Unexpected error occured : "+found[i].getMessage()+" : "+getId()+" : "+getDescription()); 
         }
     }
 }
