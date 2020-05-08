/*******************************************************************************
 * Copyright (c) 2013 Rudy D'hauwe @ Whizu
 * Licensed under the EUPL V.1.1
 *   
 * This Software is provided to You under the terms of the European 
 * Union Public License (the EUPL) version 1.1 as published by the 
 * European Union. Any use of this Software, other than as authorized 
 * under this License is strictly prohibited (to the extent such use 
 * is covered by a right of the copyright holder of this Software).
 *
 * This Software is provided under the License on an AS IS basis and 
 * without warranties of any kind concerning the Software, including 
 * without limitation merchantability, fitness for a particular purpose, 
 * absence of defects or errors, accuracy, and non-infringement of 
 * intellectual property rights other than copyright. This disclaimer 
 * of warranty is an essential part of the License and a condition for 
 * the grant of any rights to this Software.
 *   
 * For more  details, see <http://joinup.ec.europa.eu/software/page/eupl>.
 *
 * Contributors:
 *     2013 - Rudy D'hauwe @ Whizu - initial API and implementation
 *******************************************************************************/
 package org.whizu.ui;
 
 import org.whizu.dom.Markup;
 import org.whizu.jquery.Function;
 import org.whizu.jquery.Script;
 import org.whizu.widget.Widget;
 
 /**
  * @author Rudy D'hauwe
  */
 class Timeout extends Widget {
 
 	private Function action;
 	private int milliseconds;
 
 	public Timeout(int milliseconds, Function action) {
 		this.milliseconds = milliseconds;
 		this.action = action;
 		compile();
 	}
 
 	@Override
 	public Markup compile() {
 		Script script = compile(action);
 		String text = "setTimeout(function(){" + script.toJavaScript() + "}," + milliseconds + ")";
 		System.out.println("Timeout script: " + text);
 		getRequest().execute(text);
 		return null;
 	}
 }
