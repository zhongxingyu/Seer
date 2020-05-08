 /*
 Copyright (C) 2012 by CapCaval.org
 
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 */
 package org.capcaval.c3.componentmanager._impl.tools;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.capcaval.c3.component.ComponentEvent;
 import org.capcaval.c3.component.ComponentEventSubscribe;
 
 public class ComponentEventSubscribeImpl <T extends ComponentEvent> implements ComponentEventSubscribe <T> {
 
 	public List<T> observerList = new ArrayList<T>();
 	
 	@Override
 	public <S extends ComponentEvent> void subscribe(S observer) {
 		// add a new observer in order to receive T events
 		this.observerList.add((T) observer);
 	}
 
 	@Override
 	public void unsubscribe(T observer) {
 		// remove an observer in order to stop receiving T events
 		this.observerList.remove(observer);
 	}
 
 }
