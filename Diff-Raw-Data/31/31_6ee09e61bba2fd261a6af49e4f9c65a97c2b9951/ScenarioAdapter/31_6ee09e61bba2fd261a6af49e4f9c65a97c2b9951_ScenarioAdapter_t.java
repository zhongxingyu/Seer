 /**
  * Bristlecone Test Tools for Databases
  * Copyright (C) 2006-2007 Continuent Inc.
  * Contact: bristlecone@lists.forge.continuent.org
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of version 2 of the GNU General Public License as
  * published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA
  *
  * Initial developer(s): Robert Hodges and Ralph Hannus.
  * Contributor(s):
  */
 
 package com.continuent.bristlecone.benchmark;
 
 import java.util.Properties;
 
 /**
  * Implements a scenario adapter class that has all methods required and 
  * optional with implementations.  Clients may subclass and override methods
  * as desired.  
  * 
  * @author rhodges
  */
 public class ScenarioAdapter implements Scenario
 {
   /**
    * Perform initialization of instance data. 
    */
   public void initialize(Properties properties) throws Exception
   {
   }
 
   /**
    * One-time initialization that is executed before prepare() is called on
    * any scenario instance.
    */
   public void globalPrepare() throws Exception
   {
   }
 
   /**
    * One-time clean-up that is executed after cleanup() is called on all
    * scenario instances.
    */
   public void globalCleanup() throws Exception
   {
   }
 
   /**
    * Initialize the scenario for execution.
    */
   public void prepare() throws Exception
   {
   }
 
   /** 
    * Runs a single scenario iteration.  The iteration method should perform
    * a single operation and then return. 
    * 
    * @param iterationCount Current iteration count, starting with 1 and 
    * incremented with each additional iteration
    */
   public void iterate(long iterationCount) throws Exception
   {
   }
 
   /**
    * Clean up at end of scenario execution.
    */
   public void cleanup() throws Exception
   {
   }
 }
