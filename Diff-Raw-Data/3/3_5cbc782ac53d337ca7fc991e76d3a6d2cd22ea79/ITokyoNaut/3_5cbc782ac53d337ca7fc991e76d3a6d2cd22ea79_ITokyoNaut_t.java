 /*
  * The Tokyo Project is hosted on Sourceforge:
  * http://sourceforge.net/projects/tokyo/
  * 
  * Copyright (c) 2005-2007 Eric Bréchemier
  * http://eric.brechemier.name
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * 
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301 USA
  */
 package net.sf.tokyo;
 
 /**
  * ITokyoNaut is the one single interface making up Tokyo API.<br/>
  *
  * <p>
  * The name "TokyoNaut" is composed of "Tokyo" which inspired this project, while "Naut" is a shorthand 
  * for Nautilus, Captain Nemo's submarine in "Twenty Thousand Leagues Under the Sea" by Jules Verne. 
  * Captain Nemo's motto, "Mobilis in Mobili", fits well with this flexible data navigation API.
  * </p>
  *
  * <p>
  * TokyoNaut instances (classes that implement the ITokyoNaut interface) are expected to be stateless. 
  * Each call of "morph" is intended to rewrite the input data by interpreting a set of instructions 
  * or "rules", one step at a time, and updating the associated state for use in following runs. 
  * While "morph" modifies the data, the state, and even possibly the rules, "remain" is read only and 
  * returns an integer value giving an (optimistic) assessment of remaining steps.
  * </p>
  * 
  * @author Eric Br&eacute;chemier
  * @version Harajuku
  */
 public interface ITokyoNaut
 {
   
   /**
    * Assess Remaining Steps in an optimistic way.<br/>
    *
    * <p>
    * The optimistic evaluation will always return a number lower or equal to the number
    * of remaining steps before the completion of the task at hand as expressed by rules.
    * </p>
    *
    * @param rules list of rewrite operations (read only)
    * @param state current state resulting from previous morph operation (read only)
    * @param values current values (read only)
   * @return optimistic evaluation of remaining steps (subsequent calls of morph required to complete the operation).
   *         Negative values are reserved for error codes.
    */ 
   public int remain(Object[] rules, Object[] state, Object[] values);
   
   /**
    * Apply one step of rewrite operations to data.<br/>
    *
    * <p>
    * Rules are interpreted by the TokyoNaut to rewrite the values, one step at a time.
    * The state param is a useful storage for the internal state of the processing (if needed)
    * in order to keep the operation stateless.
    * </p>
    *
    * @param rules list of rewrite operations (read/write)
    * @param state current state resulting from previous morph operation (read/write)
    * @param values current values (read/write)
    */
   public void morph(Object[] rules, Object[] state, Object[] values);
   
 }
