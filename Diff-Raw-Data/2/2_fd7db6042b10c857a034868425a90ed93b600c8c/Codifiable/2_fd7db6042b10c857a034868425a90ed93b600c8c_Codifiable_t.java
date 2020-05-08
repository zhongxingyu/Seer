 /*******************************************************************************
  * Copyright (c) 2013 See AUTHORS file.
  * 
  * This file is part of SleepFighter.
  * 
  * SleepFighter is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SleepFighter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SleepFighter. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package se.toxbee.sleepfighter.utils.model;
 
 import se.toxbee.sleepfighter.utils.factory.FactoryInstantiator;
 
 /**
  * A {@link Codifiable} is an object that can be converted to and from an integer code.<br/>
  * The object must honor the following conditions:
  * <ul>
  *	<li>It must define at least one public default {@link Factory}.<br/>
  *	<li>Given a {@link Factory} of type C and instance c,<br/>
  *		<code>C.produce( c.toCode() ).equals( c ) == true</code>
  *	</li>
  * </ul>
  *
  * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
  * @version 1.0
  * @since Nov 14, 2013
  */
 public interface Codifiable {
 	/**
 	 * Converts the object to an integer representation.
 	 *
	 * @return the integer code.
 	 */
 	public int toCode();
 
 	public static interface Factory extends FactoryInstantiator<Integer, Codifiable> {
 	}
 }
