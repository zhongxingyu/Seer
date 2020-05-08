 package yeti.environments.cofoja;
 
 /**
  
  YETI - York Extensible Testing Infrastructure
  
  Copyright (c) 2009-2010, Manuel Oriol <manuel.oriol@gmail.com> - University of York
  All rights reserved.
  
  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are met:
  1. Redistributions of source code must retain the above copyright
  notice, this list of conditions and the following disclaimer.
  2. Redistributions in binary form must reproduce the above copyright
  notice, this list of conditions and the following disclaimer in the
  documentation and/or other materials provided with the distribution.
  3. All advertising materials mentioning features or use of this software
  must display the following acknowledgement:
  This product includes software developed by the University of York.
  4. Neither the name of the University of York nor the
  names of its contributors may be used to endorse or promote products
  derived from this software without specific prior written permission.
  
  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER ''AS IS'' AND ANY
  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY
  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  
  **/ 
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.Arrays;
 
 import yeti.YetiCallException;
 import yeti.YetiCard;
 import yeti.YetiLog;
 import yeti.YetiModule;
 import yeti.YetiName;
 import yeti.YetiType;
 import yeti.environments.YetiSecurityException;
 import yeti.environments.java.YetiJavaRoutine;
 
 /**
  * Class that represents a routine in Yeti which is annotated with CoFoJa. <br>
  * 
  * When making the call either to a constructor or a method  annotated 
  * with CoFoJa then the strategy for determining if there is an error is 
  * determined by the template method suggested in 
  * "JML and JUnit way of unit testing and its implementation" paper
  * by Y. Cheon and G. T. Leavens for running a test case and deciding 
  * the test output
  * 
  * @@author Vasileios Dimitriadis (vd508@cs.york.ac.uk, vdimitr@hotmail.com)
  * @author  Manuel Oriol (manuel@cs.york.ac.uk)
  * @date  13 Jul 2011
  *
  */
 public abstract class YetiCoFoJaRoutine extends YetiJavaRoutine {
 	
 	/**
 	 * Create a CoFoJa Routine
 	 * 
 	 * @param name
 	 * @param openSlots
 	 * @param returnType
 	 * @param originatingModule
 	 */
 	public YetiCoFoJaRoutine(YetiName name, YetiType[] openSlots, YetiType returnType, YetiModule originatingModule) {
 		super(name, openSlots, returnType, originatingModule);
 	}
 	
 	@Override
 	public Object makeCall(YetiCard[] arg) {
 		String log = null;
 		
 		// invoke the constructor with the arguments
 		// if there is an exception then
 		// depending on its type 
 		// decide the outcome of the test
 		try {
 			
 			try {
 				this.incnTimesCalled();
 				makeEffectiveCall(arg);
 				this.incnTimesCalledSuccessfully();
 			} catch(YetiCallException e) {
 				log = e.getLog();
 				throw e.getOriginalThrowable();
 			}
 		} catch (InstantiationException e) {
 			// should never happen
 			// e.printStackTrace();
 			return null;
 		} catch (IllegalArgumentException e) {
 			// should never happen
 			//e.printStackTrace();
 			return null;
 		} catch (IllegalAccessException e) {
 			// should never happen
 			// e.printStackTrace();
 			return null;
 		} catch (InvocationTargetException e) {
 			
 			boolean isBug = true;
 			// if we are here, we found a bug.
 			// we first print the log
 			//TODO log can be null here if thread is killed
 			YetiLog.printYetiLog("try {"+log+");} catch(Throwable t){}", this);
 			
 			// then print the exception
 			if ((e.getCause() instanceof RuntimeException && !isAcceptable(e.getCause())) || e.getCause() instanceof Error  ) {
 				if (e.getCause() instanceof ThreadDeath) {
					isBug = true;
                     YetiLog.printYetiLog("/**POSSIBLE BUG FOUND: TIMEOUT" + e.getCause().getMessage() + " **/", this);
                     isBug = true;
                     this.incnTimesCalledUndecidable();
 				} else {
 					if (e.getCause() instanceof YetiSecurityException) {
 						isBug = true;
 						YetiLog.printYetiLog("/**POSSIBLE BUG FOUND: " + e.getCause().getMessage() + " **/", this);
 						this.incnTimesCalledUndecidable();
 
 					} else if (e.getCause() instanceof com.google.java.contract.PreconditionError) {
 						// if the cause is a precondition violation of a CoFoJa precondition of second-level
 						StackTraceElement[] s = e.getCause().getStackTrace();
 						// if the precondition is not first-level
 						isBug=false;
 						if (!s[2].getClassName().startsWith("sun.reflect.")){
 							isBug=true;
 							YetiLog.printYetiLog("/**BUG FOUND: CONTRACT EXCEPTION: "+ e.getCause().getMessage() +" **/", this);
 							this.incnTimesCalledUnsuccessfully();
 						} else {
 							YetiLog.printYetiLog("/** NORMAL EXCEPTION: CoFoJa PreconditionError**/", this);
 							this.incnTimesCalledSuccessfully();
 						}
 
 					} else {
 						isBug=true;
 						YetiLog.printYetiLog("/**BUG FOUND: RUNTIME EXCEPTION "+ e.getCause().getMessage() +" **/", this);
 						this.incnTimesCalledUnsuccessfully();
 					}
 				}
 			}
 			else {
 			
 				 YetiLog.printYetiLog("/**NORMAL EXCEPTION:"+ e.getCause().getMessage() +" **/", this);
 				this.incnTimesCalledSuccessfully();
 			}
 			
 			if (isBug) {
 				YetiLog.printYetiThrowable(e.getCause(), this);
 			}
 			return null;
 
 		} catch (Error e) {
 			// if we are here there was a serious error
 			// we print it
 			YetiLog.printYetiLog("try {"+log+");} catch(Throwable t){}", this);
			
			YetiLog.printYetiLog("/**BUG FOUND: ERROR" + e.getCause().getMessage() + " **/", this);
 			YetiLog.printYetiThrowable(e.getCause(), this);
 			this.incnTimesCalledUnsuccessfully();
 			return null;
 		}
 		catch (Throwable e){
 			// should never happen
 			System.out.println("This should never happen!!!");
 			System.out.println("Trying to call " + this + " with these args=" + Arrays.toString(arg));
 			e.printStackTrace();
 			return null;
 		}
 		return this.lastCallResult;
 	}
 }
