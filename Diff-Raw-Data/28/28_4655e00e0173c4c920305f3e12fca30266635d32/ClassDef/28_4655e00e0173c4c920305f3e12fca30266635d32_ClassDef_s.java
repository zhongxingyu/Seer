 package edu.uwlax.toze.domain;
 
 import javax.xml.bind.annotation.*;
 import java.util.ArrayList;
 import java.util.List;
 
 @XmlRootElement(name = "classDef")
 @XmlType(propOrder =
                  {
                          "name",
                          "visibilityList",
                          "inheritedClass",
                          "specObjectList",
                          "state",
                          "initialState",
                          "operationList"
                  })
 public class ClassDef extends SpecObject
 {
     private String name;
     private String visibilityList;
     private InheritedClass inheritedClass;
 
     private List<SpecObject> specObjectList = new ArrayList<SpecObject>();
 
     private State state;
     private InitialState initialState;
     private List<Operation> operationList = new ArrayList<Operation>();
 
     private Specification specification;
 
     public String getName()
     {
         return name;
     }
 
     public void setName(String name)
     {
         this.name = name;
     }
 
     public String getVisibilityList()
     {
         return visibilityList;
     }
 
     public void setVisibilityList(String visibilityList)
     {
         this.visibilityList = visibilityList;
     }
 
     public InheritedClass getInheritedClass()
     {
         return inheritedClass;
     }
 
     public void setInheritedClass(InheritedClass inheritedClass)
     {
         this.inheritedClass = inheritedClass;
     }
 
     @XmlElements
             ({
                      @XmlElement(name = "basicTypeDef", type=BasicTypeDef.class),
                      @XmlElement(name = "axiomaticDef", type=AxiomaticDef.class),
                      @XmlElement(name = "abbreviationDef", type=AbbreviationDef.class),
                      @XmlElement(name = "freeTypeDef", type=FreeTypeDef.class)
              })
     public List<SpecObject> getSpecObjectList()
     {
        return new ArrayList<SpecObject>(specObjectList);
     }
 
     public void setSpecObjectList(List<SpecObject> specObjectList)
     {
         this.specObjectList = specObjectList;
     }
 
     public void addSpecObject(final SpecObject specObject)
     {
         specObjectList.add(specObject);
     }
 
     public void removeSpecObject(final SpecObject specObject)
     {
         specObjectList.remove(specObject);
     }
 
     public State getState()
     {
         return state;
     }
 
     public void setState(State state)
     {
         this.state = state;
     }
 
     public InitialState getInitialState()
     {
         return initialState;
     }
 
     public void setInitialState(InitialState initialState)
     {
         this.initialState = initialState;
     }
 
     @XmlElement(name = "operation")
     public List<Operation> getOperationList()
     {
        return new ArrayList<Operation>(operationList);
     }
 
     public void setOperationList(List<Operation> operationList)
     {
        this.operationList = new ArrayList<Operation>(operationList);
     }
 
     public void addOperation(final Operation operation)
     {
         operationList.add(operation);
     }
 
     public void removeOperation(final Operation operation)
     {
         operationList.remove(operation);
     }
 
     @XmlTransient
     public Specification getSpecification()
     {
         return specification;
     }
 
     public void setSpecification(Specification specification)
     {
         this.specification = specification;
     }
 
     @Override
     public void clearErrors()
     {
         super.clearErrors();
 
         notNullClearErrors(inheritedClass);
         notNullClearErrors(initialState);
         notNullClearErrors(state);
 
         for (SpecObject specObject : specObjectList)
             {
             specObject.clearErrors();
             }
 
         for (Operation operation : operationList)
             {
             operation.clearErrors();
             }
     }
 
     @Override
     public ClassDef clone() throws CloneNotSupportedException
     {
         super.clone();
 
         ClassDef classClone = new ClassDef();
 
         for (SpecObject specObject : specObjectList)
             {
             SpecObject specObjectClone = specObject.clone();
 
             if (specObject instanceof SpecDefinition)
                 {
                 ((SpecDefinition)specObjectClone).setClassDef(classClone);
                 }
             classClone.addSpecObject(specObject.clone());
             }
 
         for (Operation operation : operationList)
             {
             Operation operationClone = operation.clone();
             operationClone.setClassDef(classClone);
             classClone.addOperation(operationClone);
             }
 
         classClone.setName(this.getName().toString());
 
         if (this.getInheritedClass() != null)
             {
             InheritedClass inheritedClassClone = this.getInheritedClass().clone();
             inheritedClassClone.setClassDef(classClone);
             classClone.setInheritedClass(this.getInheritedClass().clone());
             }
 
         if (this.getInitialState() != null)
             {
             InitialState initialStateClone = this.getInitialState().clone();
             initialStateClone.setClassDef(classClone);
             classClone.setInitialState(initialStateClone);
             }
 
         if (this.getState() != null)
             {
             State stateClone = this.getState().clone();
             stateClone.setClassDef(classClone);
             classClone.setState(stateClone);
             }
 
         classClone.setVisibilityList(this.getVisibilityList());
 
         return classClone;
     }
 }
