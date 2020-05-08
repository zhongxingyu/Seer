 package de.hszg.atocc.autoedit.minimize.internal;
 
 import de.hszg.atocc.core.util.AbstractXmlTransformationService;
 import de.hszg.atocc.core.util.AutomatonService;
 import de.hszg.atocc.core.util.SchemaNotRegisteredException;
 import de.hszg.atocc.core.util.SerializationException;
 import de.hszg.atocc.core.util.XmlTransformationException;
 import de.hszg.atocc.core.util.XmlValidationException;
 import de.hszg.atocc.core.util.automaton.Automaton;
 import de.hszg.atocc.core.util.automaton.InvalidStateException;
 import de.hszg.atocc.core.util.automaton.InvalidTransitionException;
 
 public final class Minimize extends AbstractXmlTransformationService {
 
     private AutomatonService automatonUtils;
 
     private Automaton automaton;
     private Automaton minimalAutomaton;
 
     @Override
     protected void transform() throws XmlTransformationException {
         tryToGetRequiredServices();
 
         try {
             initialize();
 
             minimize();
 
             setOutput(automatonUtils.automatonToXml(minimalAutomaton));
 
             validateOutput("AUTOMATON");
         } catch (final RuntimeException | SchemaNotRegisteredException e) {
             throw new XmlTransformationException("Minimize|TRANSFORM_FAILED", e);
         } catch (final SerializationException | XmlValidationException | InvalidStateException
                 | InvalidTransitionException e) {
             throw new XmlTransformationException("Minimize|INVALID_INPUT", e);
         }
     }
 
     private void initialize() throws XmlValidationException, SerializationException,
             SchemaNotRegisteredException {
         validateInput("AUTOMATON");
 
         automaton = automatonUtils.automatonFrom(getInput());
     }
 
     private void minimize() throws InvalidStateException, InvalidTransitionException {
         switch (automaton.getType()) {
         case DEA:
            final DeaMinimizer minimizer = new DeaMinimizer(automaton);
             minimalAutomaton = minimizer.minimize();
             break;
 
         default:
             throw new RuntimeException("INVALID_AUTOMATON_TYPE");
         }
 
     }
 
     private void tryToGetRequiredServices() {
         automatonUtils = getService(AutomatonService.class);
     }
 }
