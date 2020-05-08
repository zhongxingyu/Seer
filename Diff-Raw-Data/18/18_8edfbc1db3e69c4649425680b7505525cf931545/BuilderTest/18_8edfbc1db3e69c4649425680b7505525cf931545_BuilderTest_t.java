/**
 * Copyright (c) 2012 centeractive ag. All Rights Reserved.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
 package com.centeractive.ws.builder;
 
 import com.centeractive.ws.builder.core.SoapBuilder;
 import com.centeractive.ws.builder.soap.domain.OperationWrapper;
 import com.centeractive.ws.builder.utils.ResourceUtils;
 import org.junit.Test;
 
 import javax.wsdl.Binding;
 import javax.wsdl.BindingOperation;
 import javax.wsdl.Definition;
 import javax.wsdl.WSDLException;
 import java.net.URL;
 import java.util.List;
 
 import static junit.framework.Assert.assertNotNull;
 
 /**
  * User: Tom Bujok (tomasz.bujok@centeractive.com)
  * Date: 15/08/12
  * Time: 12:01 PM
  */
 public class BuilderTest {
 
 
     @Test
     public void testLoadSnowboard_Bug_OP_851() throws WSDLException {
         URL wsdlUrl = ResourceUtils.getResourceWithAbsolutePackagePath("builder", "snowboard.wsdl");
         SoapBuilder builder = new SoapBuilder(wsdlUrl);
         for (Object b : builder.getDefinition().getAllBindings().values()) {
             Binding binding = (Binding) b;
             for (BindingOperation op : (List<BindingOperation>) binding.getBindingOperations()) {
                 OperationWrapper wrapper = SoapBuilder.getOperation(binding, op);
                 assertNotNull(wrapper);
             }
         }
     }
 
 }
