 /*
  * $Id$ $Revision:
  * 1.43 $ $Date$
  * 
  * ==============================================================================
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package wicket.contrib.database.hibernate;
 
 import java.io.Serializable;
 
 /**
  * Base class for persistent entities.
  */
 public abstract class HibernateDatabaseObject implements Serializable
 {
 	private Long id;
 
 	/**
 	 * Construct.
 	 */
 	public HibernateDatabaseObject()
 	{
 	}
 
 	/**
 	 * Returns the unique identifier.
 	 * 
 	 * @return the unique identifier.
 	 */
 	public final Long getId()
 	{
 		return id;
 	}
     
     /**
      * @return True if the object has not yet been assigned a valid id
      */
     public final boolean isNew()
     {
         return getId() == null || getId().longValue() == -1;
     }
 
 	/**
 	 * Sets the unique identifier.
 	 * 
 	 * @param id
 	 *            the unique identifier
 	 */
 	public final void setId(Long id)
 	{
 		this.id = id;
 	}
 
 	/**
 	 * @see java.lang.Object#toString()
 	 */
 	public String toString()
 	{
 		String clsName = getClass().getName();
 		String simpleClsName = clsName.substring(clsName.lastIndexOf('.') + 1);
 		return simpleClsName + "{id=" + getId() + "}";
 	}
 
 	/**
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	public boolean equals(Object that)
 	{
 		if (that instanceof HibernateDatabaseObject)
 		{
 			if (id != null)
 			{
 				return id.equals((((HibernateDatabaseObject)that).id));
 			}
 			else
 			{
				return this == that;
 			}
 		}
 		else
 		{
 			return false;
 		}
 	}
 
 	/**
 	 * @see java.lang.Object#hashCode()
 	 */
 	public int hashCode()
 	{
 		if (id != null)
 		{
 			return id.hashCode();
 		}
 		return super.hashCode();
 	}
 }
