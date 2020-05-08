 /**
  * \file CompletedTask.java
  * \brief The CompletedTask class
  * \package types;
 */
 
 /*
    
 This file is part of opensearch.
 Copyright © 2009, Dansk Bibliotekscenter a/s, 
 Tempovej 7-11, DK-2750 Ballerup, Denmark. CVR: 15149043
 
 opensearch is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 opensearch is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with opensearch.  If not, see <http://www.gnu.org/licenses/>.
 */
 package dk.dbc.opensearch.common.types;
 
 
 import java.util.concurrent.FutureTask;
 
 
 /**
  * The purpose of the CompletedTask is to hold information about a
  * completed threadpooljob. it contains a futureTask representing the
  * job, and a float which is the return value of the job.
  */
 public class CompletedTask<V> 
 { 
     private FutureTask future;
     private V result;
 
     
     /**
      * Constructor of the CompletedTask instance.
      * 
      * @param future the FutureTask of the completed task
      * @param result the result of the completed task
      */
     public CompletedTask( FutureTask future, V result) 
     {
         this.future = future;
         this.result = result;
     }
    
     
     /**
      * Gets the future
      * 
      * @return The future
      */
     public FutureTask getFuture()
     {
         return future;
     }
 
     
     /**
      * Gets the result
      * 
      * @return The result
      */
     public V getResult()
     {
         return result;
     }
 
     
     /**
      * Sets the future of the completedTask
      * 
      * @param The future
      */    
     public void setFuture( FutureTask future )
     {
             this.future = future;
     }
 
     
     /**
      * Sets the result of the completedTask
      * 
      * @param The result
      */
     public void setResult( V result )
     {
         this.result = result;
     }
 }
