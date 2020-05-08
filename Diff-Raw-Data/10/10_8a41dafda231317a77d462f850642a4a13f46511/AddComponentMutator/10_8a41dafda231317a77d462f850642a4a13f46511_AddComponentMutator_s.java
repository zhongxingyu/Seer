 package org.kevoree.genetic.sample.fillAllNodes;
 
 import org.kevoree.ComponentInstance;
 import org.kevoree.ContainerNode;
 import org.kevoree.ContainerRoot;
 import org.kevoree.KevoreeFactory;
 import org.kevoree.genetic.framework.KevoreeMutationOperator;
 import org.kevoree.impl.DefaultKevoreeFactory;
 
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: duke
  * Date: 12/02/13
  * Time: 16:27
  */
 public class AddComponentMutator implements KevoreeMutationOperator {
 
     private String typeName = "";
     private KevoreeFactory factory = new DefaultKevoreeFactory();
 
     public AddComponentMutator(String _typeName) {
         typeName = _typeName;
     }
 
     @Override
     public ContainerRoot mutate(ContainerRoot parent) {
         //Parent is mutable, and already protected
        List<Object> targets = parent.selectByQuery("nodes[{components.size > 0 }]");
        if (targets.size() > 0) {
             ComponentInstance instance = factory.createComponentInstance();
             instance.setTypeDefinition(parent.findTypeDefinitionsByID(typeName));
             ((ContainerNode) targets.get(0)).addComponents(instance);

            System.out.println("Add");

        } else {
            System.out.println("Empty");
         }
         return parent;
     }
 }
