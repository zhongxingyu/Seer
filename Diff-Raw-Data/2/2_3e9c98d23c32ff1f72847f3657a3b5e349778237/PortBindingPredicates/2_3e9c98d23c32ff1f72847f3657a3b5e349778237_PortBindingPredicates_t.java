 /*
  * Copyright (C) 2011 Herve Quiroz
  *
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 2.1 of the License, or (at your option)
  * any later version.
  * 
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
  */
 package org.trancecode.xproc.binding;
 
 import com.google.common.base.Predicate;
import org.trancecode.xproc.port.PortReference;
 
 /**
  * {@link Predicate} implementations related to {@link PortBinding}.
  * 
  * @author Herve Quiroz
  */
 public final class PortBindingPredicates
 {
     private PortBindingPredicates()
     {
         // No instantiation
     }
 
     public static Predicate<PortBinding> isConnectedTo(final PortReference portReference)
     {
         return new Predicate<PortBinding>()
         {
             @Override
             public boolean apply(final PortBinding portBinding)
             {
                 if (portBinding instanceof PipePortBinding)
                 {
                     final PipePortBinding pipe = (PipePortBinding) portBinding;
                     return pipe.getPortReference().equals(portReference);
                 }
 
                 return false;
             }
         };
     }
 }
