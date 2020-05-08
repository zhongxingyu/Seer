 /*
  * Copyright (C) 2010 Herve Quiroz
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
 package org.trancecode.xproc.step;
 
 import net.sf.saxon.s9api.QName;
 
 /**
  * {@code p:sink}.
  * 
  * @author Herve Quiroz
  * @see <a href="http://www.w3.org/TR/xproc/#c.sink">p:sink</a>
  */
 @ExternalResources(read = false, write = false)
 public final class SinkStepProcessor extends AbstractStepProcessor
 {
     @Override
     public QName getStepType()
     {
         return XProcSteps.SINK;
     }
 
     @Override
     protected void execute(final StepInput input, final StepOutput output)
     {
        // Do nothing
     }
 }
