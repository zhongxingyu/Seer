 /*
  * Copyright 2010, 2011 Eric Myhre <http://exultant.us>
  * 
  * This file is part of AHSlib.
  *
  * AHSlib is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, version 3 of the License, or
  * (at the original copyright holder's option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package us.exultant.ahs.core;
 
 /**
  * <p>
  * Generic interface for listeners (whether for events or messages).
  * </p>
  * 
 * <h3>exceptions</h3>
 * <p>
 * <b>Listeners are strongly expected NOT to throw unchecked exceptions from their
 * {@link #hear(Object)} method!</b> Listeners can often be decoupled from the systems
 * that actually generate the reason for the listener to have been invoked (i.e. via pipes
 * or event busses, or they may be issued by concurrency control systems, etc), and so
 * throwing exceptions will rarely if ever actually be able to propagate out to a system
 * which could reasonably be considered responsible.
 * </p>
 * 
 * <h3>usage patterns</h3>
  * <p>
  * It is advised in most situations that classes should actually contain a nested class
  * that implements this Listener interface; this allows messages of different types to be
  * routed through one object without name collisions or bad encapsulation breaks (or
  * revealing more functions than necessary on the base class).
  * </p>
  * 
 * <h3>concurrency</h3>
  * <p>
  * Listeners may be presumed to be reentrant or otherwise <b>thread-safe</b> whenever
  * applied in the threading module of the AHS library! If a Listener cannot be safely
  * entered by multiple threads concurrently, the {@link #hear(Object)} method should be
  * synchronized and this departure from theme should be loudly documented due to the
  * potential for this synchronization to have a large performance impact.
  * </p>
  * 
  * @author Eric Myhre <tt>hash@exultant.us</tt>
  * 
  * @param <$M>
  *                the message/event type
  */
 public interface Listener<$M> {
 	/**
 	 * Hear (and respond to) the given event/message.
 	 * 
 	 * @param $m
 	 */
 	public void hear($M $m);
 }
