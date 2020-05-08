 package de.hszg.atocc.kfgedit.transform.internal;
 
 import de.hszg.atocc.core.util.AbstractXmlTransformationService;
 import de.hszg.atocc.core.util.GrammarService;
 import de.hszg.atocc.core.util.SerializationException;
 import de.hszg.atocc.core.util.XmlTransformationException;
 import de.hszg.atocc.core.util.XmlValidationException;
 import de.hszg.atocc.core.util.grammar.Grammar;
 
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

 public class RemoveUnneededChainRules extends AbstractXmlTransformationService {
 
     private GrammarService grammarService;
     private Grammar grammar;
     
     @Override
     protected void transform() throws XmlTransformationException {
         tryToGetRequiredServices();
         
         try {
             validateInput("GRAMMAR");
 
             grammar = grammarService.grammarFrom(getInput());
 
             removeUnneccessaryChainRules();
 
             setOutput(grammarService.grammarToXml(grammar));
         } catch (XmlValidationException e) {
             throw new XmlTransformationException("RemoveUnneededChainRules|INVALID_INPUT", e);
         } catch (SerializationException e) {
             throw new XmlTransformationException("RemoveUnneededChainRules|SERVICE_ERROR", e);
         }
 
         try {
             validateOutput("GRAMMAR");
         } catch (XmlValidationException e) {
             throw new XmlTransformationException("RemoveUnneededChainRules|INVALID_OUTPUT", e);
         }
     }
 
     private void removeUnneccessaryChainRules() {
        final List<String> x = renameNonterminals();
    }

    private List<String> renameNonterminals() {
        final Set<String> nonTerminals = grammar.findNonTerminals();
        final List<String> x = new ArrayList<>(nonTerminals.size());
        
        // TODO
         
        return x;
     }
 
     private void tryToGetRequiredServices() {
         grammarService = getService(GrammarService.class);
     }
 }
