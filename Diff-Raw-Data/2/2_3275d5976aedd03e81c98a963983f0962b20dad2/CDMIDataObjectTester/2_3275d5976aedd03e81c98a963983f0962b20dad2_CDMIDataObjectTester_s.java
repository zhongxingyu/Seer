 /**
  * Copyright (c) 2011, Pollux
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of Pollux
  * 	  nor the names of its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL Pollux
  * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * @author         Miguel Rojas (miguel.rojas@uni-dortmund.de), Florian Feldhaus (florian.feldhaus@uni-dortmund.de)
  * @version        1.0
  * @lastrevision   15.11.2011
  */
package cloud.cdmi.internal.de.udo.one.extensions.cdmi.tester;
 
 import java.util.Hashtable;
 
 import cloud.cdmi.internal.extensions.cdmi.datatypes.CDMIDataObject;
 import cloud.cdmi.internal.extensions.cdmi.datatypes.CDMIOperation;
 import cloud.cdmi.internal.extensions.cdmi.datatypes.CDMIResponse;
 import cloud.cdmi.internal.extensions.cdmi.datatypes.NonCDMIDataObject;
 import cloud.cdmi.internal.extensions.cdmi.utils.CDMIUtils;
 
 /**
  * 
  * @author Miguel Rojas (email.miguel.rojas@googlemail.com)
  *
  */
 public class CDMIDataObjectTester
 {
     public static void testContainer()
     {
         try
         {
             CDMIDataObject dtObj = new CDMIDataObject( "http://129.217.252.37:2364" );
             Hashtable<String, String> headers = null;
             Hashtable<String, String> body = new Hashtable<String, String>();
             
             System.out.println( "Checking READ..." );
             // -- EXAMPLE #1 -----------------------------------------------------------
             headers = CDMIDataObject.Adapter.defaultHeaders( CDMIOperation.READ );
             CDMIResponse read = dtObj.read( "/Gerd", "myfile.text", headers );
             System.out.println( read.code    );
             System.out.println( read.headers );
             System.out.println( read.body    );
             
             // -- EXAMPLE #2 -----------------------------------------------------------
             String filter = "objectID;objectURI";
             read = dtObj.read( "/Gerd", "bild.png", headers, filter );
             System.out.println( read.code    );
             System.out.println( read.headers );
             System.out.println( read.body    );
 
             Thread.sleep( 1000 );
             
             System.out.println( "Checking UPDATE..." );
             // -- EXAMPLE #3 -----------------------------------------------------------
             headers = CDMIDataObject.Adapter.defaultHeaders( CDMIOperation.UPDATE );
             String[] file = { "application/octet-stream", "bild.png" };
             String newImage = CDMIUtils.encodeUTF8( "d:\\energy.jpg" );
             System.out.println( "Image="+newImage );
             newImage = "BINARY.BIN";
             body.put( "value", newImage );
             CDMIResponse update = dtObj.update( "/Gerd", file, headers, body );
             if ( update != null )
             {
                 System.out.println( update.code    );
                 System.out.println( update.headers );
                 System.out.println( update.body    );
             }
             
             System.out.println( "Checking CREATE..." );
             // -- EXAMPLE #4 -----------------------------------------------------------
             headers = CDMIDataObject.Adapter.defaultHeaders( CDMIOperation.CREATE );
             CDMIResponse create = dtObj.create( "/Gerd", file, headers, body );
             System.out.println( create.code    );
             System.out.println( create.headers );
             System.out.println( create.body    );
             
             System.out.println( "Checking DELETE..." );
             // -- EXAMPLE #5 -----------------------------------------------------------
             CDMIResponse delete = dtObj.delete( "/Gerd", "created.ini", "created.ini" );
             System.out.println( delete.code    );
             System.out.println( delete.headers );
             System.out.println( delete.body    );
             Thread.sleep( 1000 );
             
         }
         catch ( Exception e )
         {
             e.printStackTrace();
         }
     }
     
     public static void testNonContainer()
     {
         try
         {
             // NonCDMIDataObject dtObj = new NonCDMIDataObject( "http://129.217.252.37:2364" );
             NonCDMIDataObject dtObj = new NonCDMIDataObject( "http://129.217.145.206:2364/" ); // DUeSS
             
             System.out.println( "Checking CREATE..." );
             // -- EXAMPLE #1 -----------------------------------------------------------
             CDMIResponse create = dtObj.create( "/container", "spielregeln-rumiQ.pdf", "d:\\spielregeln-rumiQ.pdf", "application/pdf" );
             System.out.println( create.code    );
             System.out.println( create.headers );
             System.out.println( create.body    );
             
             System.out.println( "Checking READ..." );
             // -- EXAMPLE #2 -----------------------------------------------------------
             CDMIResponse read = dtObj.read( "/container", "spielregeln-rumiQ.pdf", "d:\\fromCDMI.pdf" );
             System.out.println( read.code    );
             System.out.println( read.headers );
             System.out.println( read.body    );
 
             /*
             System.out.println( "Checking UPDATE..." );
             // -- EXAMPLE #3 -----------------------------------------------------------
             CDMIResponse udpate = dtObj.update( "/container", "spielregeln-rumiQ.pdf", "d:\\GFD.185.pdf", "application/pdf" );
             System.out.println( udpate.code    );
             System.out.println( udpate.headers );
             System.out.println( udpate.body    );
             */
             /*
             System.out.println( "Checking DELETE..." );
             // -- EXAMPLE #4 -----------------------------------------------------------
             CDMIResponse delete = dtObj.delete( "/WubiClub", "spielregeln-rumiQ.pdf" );
             System.out.println( delete.code    );
             System.out.println( delete.headers );
             System.out.println( delete.body    );
             */
         }
         catch ( Exception e )
         {
             e.printStackTrace();
         }
     }
     
     public static void testLocalNonContainer()
     {
         try
         {
             NonCDMIDataObject dtObj = new NonCDMIDataObject( "http://localhost:2364/" );
             
             System.out.println( "Checking CREATE..." );
             // -- EXAMPLE #1 -----------------------------------------------------------
             CDMIResponse create = dtObj.create( "/", "xxx.iso", "d:\\spielregeln-rumiQ.pdf", "application/binary" );
             System.out.println( create.code    );
             System.out.println( create.headers );
             System.out.println( create.body    );
             
         }
         catch ( Exception e )
         {
             e.printStackTrace();
         }
     }
     
     public static void main( String[] args )
     {
         CDMIDataObjectTester.testLocalNonContainer();
     }
 }
