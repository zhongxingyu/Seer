 /**
  * Swift Ticket -- Back End
  *
  * Copyright (C) 2013, Jonathan Gillett, Daniel Smullen, and Rayan Alfaheid
  * All rights reserved.
  *
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
public class FatalError extends Exception
 {
    /**
     * 
     */
    private static final long serialVersionUID = 888363566861043122L;
 
     /**
      * 
      * @param exception
      * @param file
      */
     public FatalError(ExceptionCodes exception, String file)
     {
        super(exception.toString(), new Throwable(file));
     }
 }
