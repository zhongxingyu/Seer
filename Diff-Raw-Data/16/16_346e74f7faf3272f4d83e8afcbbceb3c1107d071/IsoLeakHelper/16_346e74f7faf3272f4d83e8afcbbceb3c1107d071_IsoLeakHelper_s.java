 /*
  * Copyright (c) 2005 by the University of California, Irvine
  * All Rights Reserved.
  * 
  * This software is released under the terms of the RTZen license, which
  * you should have received along with this software. If not, you may
  * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
  */
 
 package iSoLeak;
 
 import javax.realtime.*;
 import java.io.*;
 
 public class IsoLeakHelper{
     private static OutputStream leakOut = null;
     private static OutputStream memHierarchyOut = null;
     private static long startTime = System.currentTimeMillis();
    private static boolean logLeaks = false;
     public synchronized static void init(){
         //System.out.println("this sucks, butthead");
 	if( leakOut == null ){
             try{
                 long l = (new java.util.Random()).nextLong();
                 leakOut = new FileOutputStream( "leak" + l + ".log" );
                 memHierarchyOut = new FileOutputStream( "mem" + l + ".log" );
                 System.out.println( "IsoLeak runId:" + l );
             }catch( Exception e ){
                 e.printStackTrace();
             }
         }
     }
 
    private static final int maxNumThreads = 1000;
    private static final int maxMemRegions = 1000;
     private static final int maxLeaks = 10;
 
     private static final int THRD_ID = 0;
     private static final int THRD_LOG_MEM = 1;
     private static final int THRD_LOG_EVENT_TYPE = 2;
     private static final int THRD_LOG_EVENT_METHOD = 3;
     private static final int THRD_LOG_SCOPED_SIZE = 4;
     private static final int THRD_LOG_IMMORTAL_SIZE = 5;
 
     private static final int LEAK_LOG_BEFORE_EVENT_TYPE = 0;
     private static final int LEAK_LOG_BEFORE_EVENT_METHOD = 1;
     private static final int LEAK_LOG_AFTER_EVENT_TYPE = 2;
     private static final int LEAK_LOG_AFTER_EVENT_METHOD = 3;
     private static final int LEAK_LOG_TYPE = 4;
     private static final int LEAK_LOG_SIZE = 5;
     private static final int LEAK_LOG_TIME = 6;
     private static final int LEAK_LOG_FSIZE = 7;
     private static final int LEAK_LOG_MEMID = 8;
 
     private static final int MEMH_CHILD = 0;
     private static final int MEMH_PARENT = 1;
     private static final int MEMH_PORTAL = 2;
 
     private static final int EVNT_ENTR_MTHD = 0;
     private static final int EVNT_EXIT_MTHD = 1;
     private static final int LEAK_SCOPED = 0;
     private static final int LEAK_IMMORTAL = 1;
 
     private static long threadLog[][] = new long[maxNumThreads][6];
     private static long memHierarchy[][] = new long[maxMemRegions][3];
     private static long leakLog[][] = new long[maxLeaks][9];
     private static int totThreads=0;
     private static int totMems=0;
     private static int totLeaks=0;
 
     private static int findThread( long threadId ){
         try{
 //System.out.println("threads: " + totThreads + " tid: " + threadId);
            for( int i=0;i<totThreads && i<maxNumThreads;i++ ){
                 if( threadLog[i][THRD_ID] == threadId ){
                     return i;
                 }
             }
             return -1;
         }finally{
         }
     }
 
     private static long recordPortal(){
         long portalObj = -1;
         if( RealtimeThread.getCurrentMemoryArea() instanceof ScopedMemory ){
             Object portal = ((javax.realtime.ScopedMemory)javax.realtime.RealtimeThread.getCurrentMemoryArea()).getPortal();
             if( portal instanceof IsoLeakAnotated ){
                 portalObj = ((IsoLeakAnotated)portal).__isoLeak_classId();
             }else{
                 portalObj = -1;
             }
         }else{
             portalObj = -2;
         }
         return portalObj;
     }
 
     private static void addParent( long child , long parent ) throws Exception{
         for( int i=0;i<totMems;i++ ){
             if( memHierarchy[i][MEMH_CHILD] == child ){
                 if( recordPortal() >= 0 ){
                     if( memHierarchy[i][MEMH_PORTAL] == 0 ){
                         __isoLeak_write( memHierarchyOut , child );
                         memHierarchyOut.write( '=' );
                         __isoLeak_write( memHierarchyOut , recordPortal() );
                         memHierarchyOut.write( '\n' );
                         memHierarchy[i][MEMH_PORTAL] = recordPortal();
                     }
                 }
                 return;
             }
         }
         int i = totMems++;
         memHierarchy[i][MEMH_CHILD] = child;
         memHierarchy[i][MEMH_PARENT] = parent;
         try{
             __isoLeak_write( memHierarchyOut , child );
             memHierarchyOut.write( '-' );
             memHierarchyOut.write( '>' );
             __isoLeak_write( memHierarchyOut , parent );
             memHierarchyOut.write( '\n' );
         }catch( Exception e ){}
     }
 
     public synchronized static final void __iSoLeak_beginLeakMeasurement(){
         logLeaks = true;
     }
     
     public synchronized static void __iSoLeak_enterMethod( long methodId ){
         init();
         try{
             long mem = RealtimeThread.getCurrentMemoryArea().hashCode();
             //long thread = RealtimeThread.currentRealtimeThread().hashCode();
             long thread = Thread.currentThread().hashCode();
             long scopedSize = RealtimeThread.getCurrentMemoryArea().memoryConsumed();
             long immSize = ImmortalMemory.instance().memoryConsumed();
             int threadPos = findThread( thread );
             boolean thrdWasFound = threadPos != -1;

             //Scoped Leak
             if( thrdWasFound && logLeaks && threadLog[threadPos][THRD_LOG_SCOPED_SIZE] != scopedSize ){
                 int log = 1;
                 leakLog[log][LEAK_LOG_BEFORE_EVENT_TYPE] = threadLog[threadPos][THRD_LOG_EVENT_TYPE];
                 leakLog[log][LEAK_LOG_BEFORE_EVENT_METHOD] = threadLog[threadPos][THRD_LOG_EVENT_METHOD];
                 leakLog[log][LEAK_LOG_AFTER_EVENT_TYPE] = EVNT_EXIT_MTHD;
                 leakLog[log][LEAK_LOG_AFTER_EVENT_METHOD] = methodId;
                 leakLog[log][LEAK_LOG_TYPE] = LEAK_SCOPED;
                 leakLog[log][LEAK_LOG_SIZE] = scopedSize - threadLog[threadPos][THRD_LOG_SCOPED_SIZE];
                 leakLog[log][LEAK_LOG_TIME] = System.currentTimeMillis() - startTime;
                 leakLog[log][LEAK_LOG_FSIZE] = scopedSize;
                 leakLog[log][LEAK_LOG_MEMID] = mem;
                 __isoLeak_writeLeak( log );
             }
             //Immortal Leak
             if( thrdWasFound && logLeaks && threadLog[threadPos][THRD_LOG_IMMORTAL_SIZE] != immSize ){
                 int log = 1;
                 leakLog[log][LEAK_LOG_BEFORE_EVENT_TYPE] = threadLog[threadPos][THRD_LOG_EVENT_TYPE];
                 leakLog[log][LEAK_LOG_BEFORE_EVENT_METHOD] = threadLog[threadPos][THRD_LOG_EVENT_METHOD];
                 leakLog[log][LEAK_LOG_AFTER_EVENT_TYPE] = EVNT_EXIT_MTHD;
                 leakLog[log][LEAK_LOG_AFTER_EVENT_METHOD] = methodId;
                 leakLog[log][LEAK_LOG_TYPE] = LEAK_IMMORTAL;
                 leakLog[log][LEAK_LOG_SIZE] = immSize - threadLog[threadPos][THRD_LOG_IMMORTAL_SIZE];
                 leakLog[log][LEAK_LOG_TIME] = System.currentTimeMillis() - startTime;
                 leakLog[log][LEAK_LOG_FSIZE] = immSize;
                 leakLog[log][LEAK_LOG_MEMID] = -1;
                 __isoLeak_writeLeak( log );
             }
             //Mem Hierarchy
             if( thrdWasFound ){
                 addParent( mem , threadLog[threadPos][THRD_LOG_MEM] );
             }else{
                 threadPos = totThreads++;
             }
 
             threadLog[threadPos][THRD_ID] = thread;
             threadLog[threadPos][THRD_LOG_MEM] = mem;
             threadLog[threadPos][THRD_LOG_EVENT_TYPE] = EVNT_ENTR_MTHD;
             threadLog[threadPos][THRD_LOG_EVENT_METHOD] = methodId;
             threadLog[threadPos][THRD_LOG_SCOPED_SIZE] = scopedSize;
             threadLog[threadPos][THRD_LOG_IMMORTAL_SIZE] = immSize;
         }catch( Exception e ){
             e.printStackTrace();
         }
     }
 
     public synchronized static void __iSoLeak_exitMethod( long methodId ){
         try{
             init();
             long mem = RealtimeThread.getCurrentMemoryArea().hashCode();
             long thread = Thread.currentThread().hashCode();
             //long thread = RealtimeThread.currentRealtimeThread().hashCode();
             long scopedSize = RealtimeThread.getCurrentMemoryArea().memoryConsumed();
             long immSize = ImmortalMemory.instance().memoryConsumed();
             int threadPos = findThread( thread );
             boolean thrdWasFound = threadPos != -1;

             //Scoped Leak
             if( thrdWasFound && logLeaks && threadLog[threadPos][THRD_LOG_SCOPED_SIZE] != scopedSize ){
                 int log = 1;
                 leakLog[log][LEAK_LOG_BEFORE_EVENT_TYPE] = threadLog[threadPos][THRD_LOG_EVENT_TYPE];
                 leakLog[log][LEAK_LOG_BEFORE_EVENT_METHOD] = threadLog[threadPos][THRD_LOG_EVENT_METHOD];
                 leakLog[log][LEAK_LOG_AFTER_EVENT_TYPE] = EVNT_EXIT_MTHD;
                 leakLog[log][LEAK_LOG_AFTER_EVENT_METHOD] = methodId;
                 leakLog[log][LEAK_LOG_TYPE] = LEAK_SCOPED;
                 leakLog[log][LEAK_LOG_SIZE] = scopedSize - threadLog[threadPos][THRD_LOG_SCOPED_SIZE];
                 leakLog[log][LEAK_LOG_TIME] = System.currentTimeMillis() - startTime;
                 leakLog[log][LEAK_LOG_FSIZE] = scopedSize;
                 leakLog[log][LEAK_LOG_MEMID] = mem;
                 __isoLeak_writeLeak( log );
             }
             //Immortal Leak
             if( thrdWasFound && logLeaks && threadLog[threadPos][THRD_LOG_IMMORTAL_SIZE] != immSize ){
                 int log = 1;
                 leakLog[log][LEAK_LOG_BEFORE_EVENT_TYPE] = threadLog[threadPos][THRD_LOG_EVENT_TYPE];
                 leakLog[log][LEAK_LOG_BEFORE_EVENT_METHOD] = threadLog[threadPos][THRD_LOG_EVENT_METHOD];
                 leakLog[log][LEAK_LOG_AFTER_EVENT_TYPE] = EVNT_EXIT_MTHD;
                 leakLog[log][LEAK_LOG_AFTER_EVENT_METHOD] = methodId;
                 leakLog[log][LEAK_LOG_TYPE] = LEAK_IMMORTAL;
                 leakLog[log][LEAK_LOG_SIZE] = immSize - threadLog[threadPos][THRD_LOG_IMMORTAL_SIZE];
                 leakLog[log][LEAK_LOG_TIME] = System.currentTimeMillis() - startTime;
                 leakLog[log][LEAK_LOG_FSIZE] = immSize;
                 leakLog[log][LEAK_LOG_MEMID] = -1;
                 __isoLeak_writeLeak( log );
             }
             //Mem Hierarchy
             if( !thrdWasFound ){
                 threadPos = totThreads++;
             }
 
             threadLog[threadPos][THRD_ID] = thread;
             threadLog[threadPos][THRD_LOG_MEM] = mem;
             threadLog[threadPos][THRD_LOG_EVENT_TYPE] = EVNT_EXIT_MTHD;
             threadLog[threadPos][THRD_LOG_EVENT_METHOD] = methodId;
             threadLog[threadPos][THRD_LOG_SCOPED_SIZE] = scopedSize;
             threadLog[threadPos][THRD_LOG_IMMORTAL_SIZE] = immSize;
         }catch( Exception e ){
             e.printStackTrace();
         }
     }
 
     private static void __isoLeak_writeLeak( int logId ) throws Exception{
         __isoLeak_write( leakOut , leakLog[logId][LEAK_LOG_BEFORE_EVENT_TYPE] );
         leakOut.write( ',' );
         __isoLeak_write( leakOut , leakLog[logId][LEAK_LOG_BEFORE_EVENT_METHOD] );
         leakOut.write( ',' );
         __isoLeak_write( leakOut , leakLog[logId][LEAK_LOG_AFTER_EVENT_TYPE] );
         leakOut.write( ',' );
         __isoLeak_write( leakOut , leakLog[logId][LEAK_LOG_AFTER_EVENT_METHOD] );
         leakOut.write( ',' );
         __isoLeak_write( leakOut , leakLog[logId][LEAK_LOG_TYPE] );
         leakOut.write( ',' );
         __isoLeak_write( leakOut , leakLog[logId][LEAK_LOG_SIZE] );
         leakOut.write( ',' );
         __isoLeak_write( leakOut , leakLog[logId][LEAK_LOG_TIME] );
         leakOut.write( ',' );
         __isoLeak_write( leakOut , leakLog[logId][LEAK_LOG_FSIZE] );
         leakOut.write( ',' );
         __isoLeak_write( leakOut , leakLog[logId][LEAK_LOG_MEMID] );
         leakOut.write( '\n' );
     }
 
     private static void __isoLeak_write( OutputStream out , long a) throws Exception{
         if(a < 0){
             a = -a;
             out.write( '-' );
         }
         for( long i = 10000000000L ; i > 0 ; i /= 10 ){
             byte b = (byte) (a/i);
             out.write( b + '0' );
             a -= (b*i);
         }
     }
 }
