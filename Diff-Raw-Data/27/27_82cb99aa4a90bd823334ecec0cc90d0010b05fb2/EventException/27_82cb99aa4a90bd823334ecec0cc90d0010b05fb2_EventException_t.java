 /*
  * This file is part of OpenBASE.
  *
  * Copyright © 2012, Kyle Repinski
  * OpenBASE is licensed under the GNU Lesser General Public License.
  *
  * OpenBASE is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * OpenBASE is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package mwisbest.openbase.exception;
 
 public class EventException extends Exception
 {
 	private static final long serialVersionUID = -2282936942228123571L;
	
 	public EventException()
 	{
 		super();
 	}
 	
 	public EventException( String message )
 	{
 		super( message );
 	}
 	
 	public EventException( String message, Throwable cause )
 	{
 		super( message, cause );
 	}
 	
 	public EventException( Throwable cause )
 	{
 		super( cause );
 	}
 }
