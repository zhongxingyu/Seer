 package de.hszg.atocc.core.util.test.services;
 
 import de.hszg.atocc.core.util.AbstractRestfulWebService;
 import de.hszg.atocc.core.util.XmlUtilService;
 
 import org.restlet.resource.Get;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
public class GetTestService extends AbstractRestfulWebService {
 
     private static final String TEST_ROOT = "testRoot";
     private static final String TEST_ELEMENT = "testElement";
     
     @Get
    public final Document represent() {
         final XmlUtilService xmlUtils = getService(XmlUtilService.class);
 
        final Document document = xmlUtils.createEmptyDocument();
 
         final Element root = document.createElement(TEST_ROOT);
         document.appendChild(root);

        Element element = document.createElement(TEST_ELEMENT);
        root.appendChild(element);

        element = document.createElement(TEST_ELEMENT);
        root.appendChild(element);

        element = document.createElement(TEST_ELEMENT);
        root.appendChild(element);
 
         return xmlUtils.createResult(document);
     }
 
 }
