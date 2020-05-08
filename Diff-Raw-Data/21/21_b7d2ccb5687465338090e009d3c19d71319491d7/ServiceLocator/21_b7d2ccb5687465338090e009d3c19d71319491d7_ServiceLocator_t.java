 /*
  *  This file is part of xor
  *  Copyright © 2009, Steen Manniche.
  * 
  *  xor is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  xor is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  * 
  *  You should have received a copy of the GNU General Public License
  *  along with xor.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package net.manniche.xor.server;
 
 import java.lang.reflect.InvocationTargetException;
 import net.manniche.xor.types.ObjectRepositoryService;
 
 
 /**
  * The ServiceLocator class is responsible for returning implementations of
  * Object repository services. Only services (classes) that are defined as an
  * {@link ObjectRepositoryServiceType} can be requested from the ServiceLocator.
  *
  * @author Steen Manniche
  */
public final class ServiceLocator
{
 
    private ServiceLocator()
    {
    }
     /**
      * Given the type definition of an Object Repository Service, this method
      * will return an implementation of the Object Repository Service.
     * @param serviceClass the class type of the service (Obtained through the {@link ObjectRepositoryServiceType#getClassofService()})
      * @return the implementation of the service
     * @throws NoSuchMethodException if no constructor could be found on the service class
     * @throws InstantiationException if the service class cannot be instantiated
     * @throws IllegalAccessException if the service class constructor is not accessible
     * @throws InvocationTargetException if the service class cannot be invoked
     *
      * @see ObjectRepositoryService
      * @see ObjectRepositoryServiceType
      */
    public static ObjectRepositoryService getService( final Class< ? extends ObjectRepositoryService > serviceClass ) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
     {
         return serviceClass.getConstructor().newInstance();
     }
 }
