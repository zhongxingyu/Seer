 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.modelinglab.ocl.core.ast.types;
 
 import org.modelinglab.ocl.core.ast.expressions.TypeExp;
 import org.modelinglab.ocl.core.ast.utils.ClassifierVisitor;
 import org.modelinglab.ocl.core.ast.utils.OclVisitor;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author Gonzalo Ortiz Jaureguizar (gortiz at software.imdea.org)
  */
 public class ClassifierType extends Classifier  {
     private static final long serialVersionUID = 1L;
 
     private final Classifier referredClassifier;
 
     /**
      * This constructor should only be called by a Classifier object. You should use a 
      * {@link Classifier#getClassifierType() } to create instances of this class.
      */
     public ClassifierType(Classifier referredClassifier) {
         this.referredClassifier = referredClassifier;
     }
 
     protected ClassifierType(ClassifierType other) {
         super(other);
         referredClassifier = other.referredClassifier.clone();
     }
 
     @Override
     public ClassifierType getClassifierType() {
         return new ClassifierType(this);
     }
 
     public Classifier getReferredClassifier() {
         return referredClassifier;
     }
     
     @Override
     protected boolean conformsToProtected(Classifier conformsTarget, TemplateRestrictions  instanciatedTemplates) {
         if (!(conformsTarget instanceof ClassifierType)) {
             return false;
         }
         ClassifierType otherClassifierType = (ClassifierType) conformsTarget;
         return getReferredClassifier().conformsTo(otherClassifierType.getReferredClassifier(), instanciatedTemplates);
     }
 
     @Override
     public List<Classifier> getSuperClassifiers() {
         List<Classifier> result = new ArrayList<Classifier>(1);
         result.add(AnyType.getInstance());
         return result;
     }
     
     @Override
     public String toString() {
         return "Classifier<"+getReferredClassifier().toString()+">";
     }
 
     @Override
     public String getName() {
         return getReferredClassifier().getName();
     }
     
     public TypeExp createExpression() {
         return new TypeExp(this);
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         return true;
     }
 
     @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }

    @Override
     public ClassifierType clone() {
         return new ClassifierType(this);
     }
 
     @Override
     public <Result, Arg> Result accept(ClassifierVisitor<Result, Arg> visitor, Arg arguments) {
         return visitor.visit(this, arguments);
     }
 
     @Override
     public <Result, Arg> Result accept(OclVisitor<Result, Arg> visitor, Arg arguments) {
         return visitor.visit(this, arguments);
     }
     
 }
