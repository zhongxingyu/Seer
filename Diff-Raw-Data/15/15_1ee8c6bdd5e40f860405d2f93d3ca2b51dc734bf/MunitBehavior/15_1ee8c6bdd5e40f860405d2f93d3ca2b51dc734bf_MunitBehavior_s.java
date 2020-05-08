 package org.mule.tooling.ui.contribution.munit;
 
 import java.util.List;
 
 import javax.xml.bind.JAXBElement;
 
 import org.dom4j.Element;
 import org.mule.tooling.core.builder.container.ContainerBehavior;
 import org.mule.tooling.core.builder.messageflow.MuleToFlowTransformer;
 import org.mule.tooling.model.messageflow.Container;
 import org.mule.tooling.model.messageflow.MessageFlowEntity;
 import org.mule.tooling.model.messageflow.MuleConfiguration;
 import org.mule.tooling.model.messageflow.NestedContainer;
 import org.mule.tooling.model.module.ContainerDefinition;
 import org.mule.tooling.model.module.GraphicalContainerDefinition;
 import org.mule.tooling.model.module.NodeDefinition;
 
 /**
  * <p>
  * {@link ContainerBehavior} for the Munit tests, this class defines where to put graphically the elements inside the Munit test. Spy elements and Mock elements goes inside the
  * Setup part of the Munit test, the rest of the elements goes to the core container of the Munit test.
  * </p>
  * 
  */
 public class MunitBehavior implements ContainerBehavior {
 
     @Override
     public void handleContainerElements(List<Element> elements, Container container, ContainerDefinition containerDefinition, MuleConfiguration muleConfiguration,
             String runtimeVersion, MuleToFlowTransformer muleToFlowTransformer) {
 
         for (Element element : elements) {
             NodeDefinition def = muleToFlowTransformer.getDefinitionForElement(element, runtimeVersion);
             JAXBElement<? extends MessageFlowEntity> entity = muleToFlowTransformer.getPopulatedNode(element, muleConfiguration, def, runtimeVersion);
 
             if (isMockOrSpyElement(element)) {
                 getSetupContainer(container, containerDefinition).getCompartmentLaneEntries().add(entity);
             } else {
                 getCoreContainer(container, containerDefinition).getCompartmentLaneEntries().add(entity);
 
             }
         }
     }
 
     private boolean isMockOrSpyElement(Element element) {
        return MunitPlugin.MOCK_NAMESPACE.equals(element.getNamespace()) && ("when".equals(element.getName()) || "spy".equals(element.getName()));
     }
 
     private NestedContainer getSetupContainer(Container container, ContainerDefinition containerDefinition) {
         List<GraphicalContainerDefinition> graphicalContainerDefinitions = containerDefinition.getGraphicalContainerDefinitions();
         GraphicalContainerDefinition setupContainerDef = graphicalContainerDefinitions.get(0);
         NestedContainer setupContainer = container.getNestedContainer(setupContainerDef.getId());
         return setupContainer;
     }
 
     private NestedContainer getCoreContainer(Container container, ContainerDefinition containerDefinition) {
         List<GraphicalContainerDefinition> graphicalContainerDefinitions = containerDefinition.getGraphicalContainerDefinitions();
         GraphicalContainerDefinition coreContainerDef = graphicalContainerDefinitions.get(1);
         return container.getNestedContainer(coreContainerDef.getId());
     }
 
 }
