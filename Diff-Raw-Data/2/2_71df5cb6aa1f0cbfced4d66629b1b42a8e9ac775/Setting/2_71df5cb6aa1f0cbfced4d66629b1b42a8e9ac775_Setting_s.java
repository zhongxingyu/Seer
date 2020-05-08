 // Copyright (c) 2002 Sean Kelly
 // All rights reserved.
 // 
 // Redistribution and use in source and binary forms, with or without
 // modification, are permitted provided that the following conditions are
 // met:
 // 
 // 1. Redistributions of source code must retain the above copyright
 //    notice, this list of conditions and the following disclaimer.
 // 2. Redistributions in binary form must reproduce the above copyright
 //    notice, this list of conditions and the following disclaimer in the
 //    documentation and/or other materials provided with the
 //    distribution.
 // 
 // THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 // ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 // IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 // PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS
 // BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 // CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 // SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 // BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 // WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 // OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 // IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 //
 // $Id$
 
 package net.sourceforge.xmlresume;
 
 import javax.xml.transform.Transformer;
 
 /**
  * A setting of a single parameter.
  *
  * @author Kelly
  */
 class Setting {
 	/**
 	 * Creates a new <code>Setting</code> instance.
 	 *
 	 * @param parameter The parameter being set.
 	 * @param value The value to which it's being set.
 	 */
 	public Setting(Parameter parameter, String value) {
 		this.parameter = parameter;
 		this.value = value;
 	}
 
 	/**
 	 * Get the name of the parameter being set.
 	 *
 	 * @return Its name.
 	 */
 	public String getName() {
 		return parameter.getName();
 	}
 
 	/**
 	 * Get the paramter being set.
 	 *
 	 * @return a <code>Parameter</code> value.
 	 */
 	public Parameter getParameter() {
 		return parameter;
 	}
 
 	/**
 	 * Get the value to which the parameter is being set.
 	 *
 	 * @return a <code>String</code> value.
 	 */
 	public String getValue() {
 		return value;
 	}
 
 	/**
 	 * Apply this parameter setting to the given transformer.
 	 *
 	 * @param transformer Transformer in which to set the parameter to a certain value.
 	 */
 	public void applyTo(Transformer transformer) {
 		transformer.setParameter(getName(), getValue());
 	}
 
 	public String toString() {
 		return getName() + "=" + getValue();
 	}
 
 	public int hashCode() {
 		return (parameter.hashCode() << 16) ^ value.hashCode();
 	}
 
 	public boolean equals(Object obj) {
 		if (obj == this) return true;
 		if (!(obj instanceof Setting)) return false;
 		Setting rhs = (Setting) obj;
 		return parameter.equals(rhs.parameter) && value.equals(rhs.value);
 	}
 
 	/** Parameter I'm setting .*/
 	private Parameter parameter;
 
	/** Value to wchih I'm setting the parameter. */
 	private String value;
 }
