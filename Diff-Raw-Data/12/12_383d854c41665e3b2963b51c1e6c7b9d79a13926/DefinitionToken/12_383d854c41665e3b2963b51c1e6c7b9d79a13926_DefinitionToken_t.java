 package jp.ac.osaka_u.ist.sel.metricstool.main.ast.token;
 
 
 /**
  * ̗vf̒`\g[NNX
  * 
  * @author kou-tngt
  *
  */
 public class DefinitionToken extends AstTokenAdapter {
 
     /**
      * NX`\萔CX^X.
      */
     public static final DefinitionToken CLASS_DEFINITION = new DefinitionToken("CLASS_DEFINITION") {
         @Override
         public boolean isClassDefinition() {
             return true;
         }
         
         @Override
         public boolean isBlockDefinition() {
             return true;
         }
     };
 
     /**
      * RXgN^`\萔CX^X.
      */
     public static final DefinitionToken CONSTRUCTOR_DEFINITION = new DefinitionToken(
             "CONSTRUCTOR_DEFINITION") {
         @Override
         public boolean isConstructorDefinition() {
             return true;
         }
         
         @Override
         public boolean isBlockDefinition() {
             return true;
         }
     };
 
     /**
      * tB[h`\萔CX^X.
      */
     public static final DefinitionToken FIELD_DEFINITION = new DefinitionToken("FIELD_DEFINITION") {
         @Override
         public boolean isFieldDefinition() {
             return true;
         }
     };
 
     /**
      * [Jp[^iforcatch߂̍ŏɐ錾ϐj̒`\萔CX^X
      */
     public static final DefinitionToken LOCAL_PARAMETER_DEFINITION = new DefinitionToken(
             "LOCAL_PARAMETER_DEFINITION") {
         @Override
         public boolean isLocalParameterDefinition() {
             return true;
         }
     };
 
     /**
      * [Jϐ`\萔CX^X.
      */
     public static final DefinitionToken LOCALVARIABLE_DEFINITION = new DefinitionToken(
             "LOCALVARIABLE_DEFINITION") {
         @Override
         public boolean isLocalVariableDefinition() {
             return true;
         }
     };
 
     /**
      * \bh`\萔CX^X.
      */
     public static final DefinitionToken METHOD_DEFINITION = new DefinitionToken("METHOD_DEFINITION") {
         @Override
         public boolean isMethodDefinition() {
             return true;
         }
         
         @Override
         public boolean isBlockDefinition() {
             return true;
         }
     };
 
     /**
      * \bhp[^`\萔CX^X.
      */
     public static final DefinitionToken METHOD_PARAMETER_DEFINITION = new DefinitionToken(
             "METHOD_PARAMETER_DEFINITION") {
         @Override
         public boolean isMethodParameterDefinition() {
             return true;
         }
     };
 
     /**
      * OԂ̒`\萔CX^X.
      */
     public static final DefinitionToken NAMESPACE_DEFINITION = new DefinitionToken(
             "NAMESPACE_DEFINITION") {
         @Override
         public boolean isNameSpaceDefinition() {
             return true;
         }
     };
     
     /**
      * ^p[^̒`\萔CX^X.
      */
     public static final DefinitionToken TYPE_PARAMETER_DEFINITION = new DefinitionToken("TYPE_PARAMETER_DEFINITION"){
         @Override
         public boolean isTypeParameterDefinition(){
             return true;
         }
     };
 
     /**
      * w肳ꂽŏRXgN^.
      * @param text ̃g[N\.
      */
     public DefinitionToken(final String text) {
         super(text);
     }
 }
