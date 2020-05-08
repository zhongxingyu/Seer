 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.modelinglab.ocl.core.ast.expressions;
 
 import com.google.common.base.Joiner;
 import org.modelinglab.ocl.core.ast.types.Classifier;
 import org.modelinglab.ocl.core.ast.utils.OclExpressionsVisitor;
 import org.modelinglab.ocl.core.ast.utils.OclVisitor;
 import org.modelinglab.ocl.core.exceptions.OclEvaluationException;
 import org.modelinglab.ocl.core.values.OclValue;
 
 /**
  *
  * @author Gonzalo Ortiz Jaureguizar (gortiz at software.imdea.org)
  */
 public abstract class IteratorExp extends LoopExp {
     private static final long serialVersionUID = 1L;
 
     Classifier type;
 
     public IteratorExp() {
     }
 
     /**
      * Copy constructor. It is very important to kwnow that <b>UML classes, attributes, 
      * associations and operations are NOT cloned!</b>
      */
     protected IteratorExp(IteratorExp other) {
         super(other);
         type = other.type;
     }
     
     @Override
     public Classifier getType() {
         return type;
     }
     
     @Override
     public void setType(Classifier type) {
         this.type = type;
     }
     
     @Override
     public void setName(String name) {
         modifyAttempt();
         setNameProtected(name);
     }
     
     @Override
     public String getName() {
         return getNameProtected();
     }
 
     @Override
     public OclValue getStaticEvaluation() throws OclEvaluationException {
         return null;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (!(obj instanceof IteratorExp)) {
             return false;
         }
         final IteratorExp other = (IteratorExp) obj;
         if (this.type != other.type && (this.type == null || !this.type.equals(other.type))) {
             return false;
         }
         return super.equals(other);
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 13 * hash + (this.type != null ? this.type.hashCode() : 0);
         return 13 * hash + super.hashCode();
     }
 
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder();
         if (getSource() == null) {
             sb.append("<nullSource>");
         }
         else {
             sb.append(source.toString());
         }
         sb.append("->");
         sb.append(getName()).append('(').append(Joiner.on(", ").join(getIterators())).append(" | ");
         if (getBody() == null) {
             sb.append("<nullBody>");
         }
         else {
             sb.append(getBody());
         }
         sb.append(')');
         return sb.toString();
     }
     
     @Override
     public String toText() {
         checkIsValid();
         StringBuilder sb = new StringBuilder();
         
         assert getSource() != null;
         sb.append(source.toString());
         
         sb.append("->");
 
         sb.append(getName()).append('(');
         
         if (!getIterators().isEmpty()) {
             sb.append(Joiner.on(", ").join(getIterators())).append(" | ");
         }
         
         assert getBody() != null;
        sb.append(getBody());
         
         sb.append(')');
         
         return sb.toString();
     }
 
     @Override
     public <Result, Arg> Result accept(OclVisitor<Result, Arg> visitor, Arg arguments) {
         return visitor.visit(this, arguments);
     }
 
     @Override
     public <Result, Arg> Result accept(OclExpressionsVisitor<Result, Arg> visitor, Arg arguments) {
         return visitor.visit(this, arguments);
     }
     
 }
