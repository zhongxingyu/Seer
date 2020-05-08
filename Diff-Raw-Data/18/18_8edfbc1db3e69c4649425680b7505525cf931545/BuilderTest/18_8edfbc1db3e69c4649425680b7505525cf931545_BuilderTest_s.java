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
