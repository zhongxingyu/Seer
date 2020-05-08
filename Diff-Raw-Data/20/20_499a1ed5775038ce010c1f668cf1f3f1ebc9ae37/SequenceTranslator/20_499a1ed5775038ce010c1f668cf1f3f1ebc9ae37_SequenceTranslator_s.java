 package com.fing.pis.bizativiti.core.bpmn;
 
 import javax.xml.bind.JAXBElement;
 
 import org.omg.spec.bpmn._20100524.model.TDocumentation;
 import org.omg.spec.bpmn._20100524.model.TExpression;
 import org.omg.spec.bpmn._20100524.model.TFlowElement;
 import org.omg.spec.bpmn._20100524.model.TSequenceFlow;
 import org.omg.spec.dd._20100524.di.DiagramElement;
 
 import com.fing.pis.bizativiti.common.metamodel.MetamodelConnector;
 import com.fing.pis.bizativiti.common.metamodel.MetamodelSequenceFlow;
 
 public class SequenceTranslator extends ATranslator {
 
     @Override
     public JAXBElement<? extends TFlowElement> getFlowElement(Object o, TranslatorState e) {
 
         MetamodelSequenceFlow sequence = (MetamodelSequenceFlow) o;
         TSequenceFlow sequenceFlow = e.getModelFactory().createTSequenceFlow();
         sequenceFlow.setId(sequence.getId());
         TFlowElement referenced = e.getElements().get(sequence.getFrom().getId());
         if (referenced == null)
             throw new IllegalArgumentException("From Element of bpmn connector " + sequence.getId() + " is null");
         sequenceFlow.setSourceRef(referenced);  // Aca no va el id, sino que va el objeto referenciado! :)
 
         referenced = e.getElements().get(sequence.getTo().getId());
         if (referenced == null)
             throw new IllegalArgumentException("To Element of bpmn connector " + sequence.getId() + " is null");
         sequenceFlow.setTargetRef(referenced);    // Aca no va el id, sino que va el objeto referenciado! :)
 
         if ("CONDITION".equals(sequence.getConditionType())) {
             TExpression exp = new TExpression();
 
             exp.setId(sequence.getCondition());
             sequenceFlow.setConditionExpression(exp);
         }
         if (sequence.getName() != null) {
             sequenceFlow.setName(sequence.getName());
         }
         if (sequence.getDescription() != null) {
             TDocumentation documentation = new TDocumentation();
            documentation.getContent().add(sequence.getDescription());
             sequenceFlow.getDocumentation().add(documentation);
         }
 
         return e.getModelFactory().createSequenceFlow(sequenceFlow);
     }
 
     @Override
     public JAXBElement<? extends DiagramElement> getDiagramElement(Object o, TranslatorState e) {
         return Helper.createEdge((MetamodelConnector) o);
     }
 
 }
