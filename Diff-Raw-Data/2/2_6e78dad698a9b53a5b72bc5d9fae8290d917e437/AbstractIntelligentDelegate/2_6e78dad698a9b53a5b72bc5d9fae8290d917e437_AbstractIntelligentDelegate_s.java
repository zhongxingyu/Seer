 //Copyright 2012-2013 Joshua Scoggins. All rights reserved.
 //
 //Redistribution and use in source and binary forms, with or without modification, are
 //permitted provided that the following conditions are met:
 //
 //   1. Redistributions of source code must retain the above copyright notice, this list of
 //      conditions and the following disclaimer.
 //
 //   2. Redistributions in binary form must reproduce the above copyright notice, this list
 //      of conditions and the following disclaimer in the documentation and/or other materials
 //      provided with the distribution.
 //
 //THIS SOFTWARE IS PROVIDED BY Joshua Scoggins ``AS IS'' AND ANY EXPRESS OR IMPLIED
 //WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 //FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Joshua Scoggins OR
 //CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 //CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 //SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 //ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 //NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 //ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 //
 //The views and conclusions contained in the software and documentation are those of the
 //authors and should not be interpreted as representing official policies, either expressed
 //or implied, of Joshua Scoggins.
 package com.dritanium.delegates;
 import java.util.ArrayList;
 /**
  * An abstract implementation of an IntelligentDelegate
  * @author Joshua Scoggins
  */
 public abstract class AbstractIntelligentDelegate implements IntelligentDelegate {
 
 	private Class[] args;
 
 	public AbstractIntelligentDelegate(int size) {
 		args = new Class[size];
 	}
 	public AbstractIntelligentDelegate(Class[] arguments) {
 		args = arguments;	
 	}
 	public int getArgumentCount() {
 		return args.length;
 	}
 	
 	public boolean checkInput(Object[] input) {
 		if(input.length != getArgumentCount()) {
 			return false;
 		} else {
 			for(int i = 0; i < getArgumentCount(); i++) {
				Class desiredType = args[i].getClass();
 				if(!desiredType.isInstance(input[i])) {
 					return false;
 				}
 			}
 			return true;
 		}
 	}
 	
 	public Class getArgument(int index) {
 		return args[index];
 	}
 
 	public void registerArgument(int index, Class value) {
 		args[index] = value;
 	}
 	
 	
 	public final Object invoke(Object[] input) {
 		//do type checking here
 		if(checkInput(input)) {
 			return invokeImpl(input);
 			//if we support return type checking then it is up
 			//to the programmer to denote if it returns null or not.
 			//I think that's really stupid so no returnType checking
 		} else {
 			throw new TypeCheckingException();
 		}
 	}
 	protected abstract Object invokeImpl(Object[] input);
 	/**
 	 * A method used to get the input used by the run method 
 	 * @return the input used by run()
 	 */
 	protected abstract Object[] getInput();
 	/**
 	 * A method used to set the input used by the run method
 	 * @param input The input used by run() 
 	 */
 	public abstract void setInput(Object[] input);
 
 	/**
 	 * Retrieves the result of the invoking the run method
 	 * @return  the result of calling run
 	 */
 	public abstract Object getResult();
 	/**
 	 * Sets the result from invoking the run method
 	 * @param input The result of calling run()
 	 */
 	protected abstract void setResult(Object input);
 	
 	public void run() {
 		setResult(invoke(getInput()));	
 	}
 }
