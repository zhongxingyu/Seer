 /*
  * Copyright 2000-2004 the Jnome development team.
  *
  * @author Marko van Dooren
  * @author Nele Smeets
  * @author Kristof Mertens
  * @author Jan Dockx
  *
  * This file is part of Jnome.
  *
  * Jnome is free software; you can redistribute it and/or modify it under the
  * terms of the GNU General Public License as published by the Free Software
  * Foundation; either version 2 of the License, or (at your option) any later
  * version.
  *
  * Jnome is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along with
  * Jnome; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
  * Suite 330, Boston, MA 02111-1307 USA
  */
 package chameleon.support.modifier;
 
 import org.rejuse.property.PropertySet;
 
 import chameleon.core.element.Element;
 import chameleon.core.modifier.ModifierContainer;
 import chameleon.core.modifier.ModifierImpl;
 
 /**
  * @author Marko van Dooren
  */
 public class Static extends ModifierImpl<Static, ModifierContainer> {
 
 	public Static() {
 		
 	}
 
 	@Override
 	public Static clone() {
 		return new Static();
 	}
 	 /*@
   @ behavior
   @
   @ post \result.contains(language().OVERRIDABLE.inverse());
   @ post \result.contains(language().DEFINED);
   @ post \result.size() == 2;
   @*/
  public PropertySet<Element> impliedProperties() {
   return createSet(language().CLASS);
  }
 
 }
