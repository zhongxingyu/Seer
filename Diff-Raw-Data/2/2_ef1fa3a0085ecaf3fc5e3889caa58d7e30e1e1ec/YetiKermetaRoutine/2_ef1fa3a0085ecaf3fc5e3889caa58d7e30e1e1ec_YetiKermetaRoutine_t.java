 package yeti.environments.kermeta;
 
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
 import java.util.HashMap;
 
 import fr.irisa.triskell.kermeta.error.KermetaError;
 import fr.irisa.triskell.kermeta.error.KermetaInterpreterError;
 import fr.irisa.triskell.kermeta.error.KermetaVisitorError;
 import fr.irisa.triskell.kermeta.interpreter.KermetaRaisedException;
 import fr.irisa.triskell.kermeta.language.structure.ClassDefinition;
 import fr.irisa.triskell.kermeta.language.structure.Type;
 import fr.irisa.triskell.kermeta.runtime.RuntimeObject;
 
 import yeti.YetiCallContext;
 import yeti.YetiCallException;
 import yeti.YetiCard;
 import yeti.YetiLog;
 import yeti.YetiLogProcessor;
 import yeti.YetiModule;
 import yeti.YetiName;
 import yeti.YetiRoutine;
 import yeti.YetiType;
 import yeti.YetiVariable;
 import yeti.environments.YetiSecurityException;
 
 /**
  * Class that represents a routine in Kermeta. A routine is supposed to be either a constructor or a  method.
  *  
  * @author Erwan Bousse (erwan.bousse@gmail.com)
  * @date juil. 2011
  *
  */
 public class YetiKermetaRoutine extends YetiRoutine {
 
 
 	/**
 	 * Result of the last call.
 	 */
 	protected YetiVariable lastCallResult=null;
 
 	/**
 	 * The exceptions acceptables for this routine
 	 * TODO What is an exception in Kermeta exactly ?
 	 */
 	public HashMap <String, ClassDefinition> acceptableExceptionTypes = new HashMap <String, ClassDefinition>();
 
 
 	/**
 	 * 
 	 * Creates a Kermeta routine.
 	 * 
 	 * @param name the name of the routine.
 	 * @param openSlots the open slots for the routine.
 	 * @param returnType the type of the returned value.
 	 * @param originatingModule the module in which it was defined
 	 */
 	public YetiKermetaRoutine(YetiName name, YetiType[] openSlots, YetiType returnType, YetiModule originatingModule) {
 		super();
 		this.name = name;
 		this.openSlots = openSlots;
 		this.returnType = returnType;
 		this.originatingModule = originatingModule;
 	}
 
 
 	@Override
 	public boolean checkArguments(YetiCard[] arg) {
 		// TODO Auto-generated method stub (but wht to do ?)
 		return true;
 	}
 
 	/*
 	 * Only here for the subclasses (YetiKermetaMethod and YetiKermetaConstructor
 	 * (non-Javadoc)
 	 * @see yeti.YetiRoutine#makeEffectiveCall(yeti.YetiCard[])
 	 */
 	@Override
 	public String makeEffectiveCall(YetiCard[] arg) throws Throwable {
 		return null;
 	}
 
 
 
 
 
 
 	/*
 	 * TODO entirely, but we have to catch kermeta exceptions...
 	 * (non-Javadoc)
 	 * @see yeti.YetiRoutine#makeCall(yeti.YetiCard[])
 	 */
 	public Object makeCall(YetiCard []arg){
 		// TODO Check that this method does not need the changes that were implemented in other bindings:
 		// Namely, returning explicitly null when the call is failing.
 
 		String log = null;
 		super.makeCall(arg);
 
 		// We make the call, and catch YetiCallException exceptions sent by "makeEffectiveCall"
 		try {
 			try {
 				makeEffectiveCall(arg);
 				this.incnTimesCalledSuccessfully();
 			}
 
 			// If we find a potential error, we log it, and throw it again
 			catch(YetiCallException e) {
 				log = e.getLog();
 
 				//TODO Maybe it's not perfect...
 				// If it's a KermetaVisitorError, we prefer send the cause
 				if(e.getOriginalThrowable() instanceof KermetaVisitorError) {
 					// It may be a InvocationTargetException in which case we throw its cause
 					if (e.getOriginalThrowable().getCause() instanceof InvocationTargetException) {
 						throw e.getOriginalThrowable().getCause().getCause();
 					}
 					// if not, we throw the KermetaVisitorError cause
 					else {
 						throw e.getOriginalThrowable().getCause();
 					}
 				}
 				// And if it's not a KermetaVisitorError, we simply throw it
 				else {
 					throw e.getOriginalThrowable();
 				}
 			}
 
 		}
 
 
 		//----------- Error catching part -----------
 
 
 		
 		
 		// Can this happen ?
 		catch (IllegalArgumentException e) {
 			YetiLog.printDebugLog(this.getSignature()+" IllegalArgumentException", this,true);
 			for(YetiCard c: arg) {
 				YetiLog.printDebugLog("YetiType: "+c.getType().toString()+", real type: "+c.getValue().getClass()+", value: "+c.getValue().toString(),this,true);
 			}
 			// should never happen
 			//e.printStackTrace();
 		}
 
 		// Can this happen ?
 		catch (IllegalAccessException e) {
 			YetiLog.printDebugLog(this.getSignature()+" IllegalAccessException", this,true);
 			// should never happen
 			// e.printStackTrace();
 		}
 
 		// Happens when an infinite loop occurs
 		catch (ThreadDeath e) {
 			YetiLog.printYetiLog("/ **POSSIBLE BUG FOUND: TIMEOUT** /", this);
 			this.incnTimesCalledUndecidable();
 		}
 
 		// Not sure...
 		catch (YetiSecurityException e) {
 			YetiLog.printYetiLog("/ **POSSIBLE BUG FOUND: "+e.getCause().getMessage()+" ** /", this);
 			this.incnTimesCalledUndecidable();
 		}
 
 		// When the interpreter has a problem
 		catch (KermetaInterpreterError e) {
 			YetiLog.printYetiLog("/ **POSSIBLE BUG FOUND: INTERPRETER ERROR: "+e.getMessage()+" ** /", this);
 			this.incnTimesCalledUndecidable();
 		}
 
 		// This is a real Kermeta exception that was thrown because of a real error
 		catch (KermetaRaisedException e) {
 			YetiLog.printYetiLog("/**BUG FOUND: RUNTIME EXCEPTION** /", this);
			YetiLog.printYetiThrowable(e, new YetiCallContext(this,arg,e,"/**BUG FOUND: RUNTIME EXCEPTION** /\n/** "+YetiLog.proc.getTraceFromThrowable(e)+"**/"),true);
 
 			this.incnTimesCalledUnsuccessfully();
 		}
 
 
 		// If anything was forgotten (debug only)
 		catch (Throwable e){
 			System.out.println("######### Throwable caught !  ############");//TODO retirer
 			System.out.println("### Type : "+e.getClass());
 			System.out.println("### Cause : "+e.getCause());
 			if(e.getCause()!=null)
 				System.out.println("### Cause cause : "+e.getCause().getCause());
 			System.out.println("### Routine : "+this );
 			System.out.print("### Arguments : ");
 			for (YetiCard y : arg) {
 				System.out.print(y.getType().getName() + " - id=" + y.getIdentity() +" - value="+y.getValue()+", ");
 			}System.out.println();
 			System.out.println("### Stack : ");
 			e.printStackTrace();
 			System.out.println("### End of the stack");
 		}
 
 
 		// ------- End of the catching part ---------
 
 
 		// In any case, we return the result
 		return this.lastCallResult;
 	}
 
 
 
 }
