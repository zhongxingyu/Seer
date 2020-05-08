 package de.hszg.atocc.kfgedit.transform.internal;
 
 import de.hszg.atocc.core.util.AbstractXmlTransformationService;
 import de.hszg.atocc.core.util.XmlTransformationException;
import de.hszg.atocc.core.util.XmlValidationException;
 
 public class RemoveUnneededNonterminals extends AbstractXmlTransformationService {
 
     @Override
     protected void transform() throws XmlTransformationException {
         
        try {
            validateInput("GRAMMAR");
        } catch (XmlValidationException e) {
            throw new XmlTransformationException("RemoveUnneededNonterminals|INVALID_INPUT", e);
        }
        
        setOutput(getInput());
        
        try {
            validateOutput("GRAMMAR");
        } catch (XmlValidationException e) {
            throw new XmlTransformationException("RemoveUnneededNonterminals|INVALID_OUTPUT", e);
        } 
     }
 
 }
