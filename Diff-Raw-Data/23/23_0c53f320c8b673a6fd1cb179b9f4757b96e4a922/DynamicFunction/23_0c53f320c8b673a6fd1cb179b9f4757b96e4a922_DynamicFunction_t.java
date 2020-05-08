 //Copyright 2012 Joshua Scoggins. All rights reserved.
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
 package functions;
 
 public class DynamicFunction<T> implements Runnable {
 
	private DelegateBody<T> body;
	private Delegate functor;
 	private Object[] arguments;
 
 	public NonLocalClosedVariable<T> getReturnContainer() {
 		return body.getReturnContainer();
 	}
 
 	public void setReturnContainer(NonLocalClosedVariable<T> returnContainer) {
 		body.setReturnContainer(returnContainer);
 	}
 
	public Delegate getFunctor() {
 		return functor;
 	}
 
	public void setFunctor(Delegate functor) {
 		this.functor = functor;
 	}

	public DelegateBody<T> getBody() {
 		return body;
 	}
 
	public void setBody(DelegateBody<T> body) {
 		this.body = body;
 	}
 
 	public T getReturnContainerValue() {
 		return getReturnContainer().getActualValue();
 	}
 
 	public Object[] getArguments() {
 		return arguments;
 	}
 
 	public void setArguments(Object[] arguments) {
 		this.arguments = arguments;
 	}
 
	public DynamicFunction(DelegateBody<T> body, String[] variables, 
 			NonLocalClosedVariable<T> returnContainer) {
 		getBody(body);
 		getFunctor(defun(variables(variables), body));
 		getReturnContainer(returnContainer);
 	}
 
	public DynamicFunction(DelegateBody<T> body, String[] variables) {
 		this(body, variables, new NonLocalClosedVariable<T>());
 	}
 
 	public Object invoke(Object[] args) {
 		return functor.invoke(args);
 	}
 
 	public void run() {
 		functor.run();
 	}
 }
