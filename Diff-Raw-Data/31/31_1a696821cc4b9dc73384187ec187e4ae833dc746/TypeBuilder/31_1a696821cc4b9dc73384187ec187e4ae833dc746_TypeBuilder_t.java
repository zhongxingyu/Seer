 package jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.statemanager.StateChangeEvent;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.statemanager.TypeDescriptionStateManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.statemanager.StateChangeEvent.StateChangeEventType;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.token.AstToken;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.visitor.AstVisitEvent;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.PrimitiveTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.VoidTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedArrayTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedReferenceTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedTypeInfo;
 
 public class TypeBuilder extends CompoundDataBuilder<UnresolvedTypeInfo>{
     
     public TypeBuilder(BuildDataManager buildDataManager){
         if (null == buildDataManager){
             throw new NullPointerException("nameSpaceManager is null.");
         }
         
         this.buildDataManager = buildDataManager;
         addStateManager(stateMachine);
         addInnerBuilder(identifierBuilder);
         identifierBuilder.deactivate();
     }
     
     public void entered(AstVisitEvent event){
         super.entered(event);
         
         if (isActive() && stateMachine.isEntered()){
             AstToken token = event.getToken();
             
             if (null == primitiveType && null == voidType){
                 if (token.isPrimitiveType()){
                     this.primitiveType = PrimitiveTypeInfo.getType(token.toString());
                 }
                 else if (token.isVoidType()){
                     this.voidType = VoidTypeInfo.getInstance();
                 }
             }
         }
     }
     
     public void exited(AstVisitEvent event){
         if (isActive() && stateMachine.isEntered()){
             AstToken token = event.getToken();
             if (token.isArrayDeclarator()){
                 arrayCount++;
             }
         }
         
         super.exited(event);
     }
 
     @Override
     public void stateChangend(StateChangeEvent<AstVisitEvent> event) {
         if (isActive()){
             StateChangeEventType type = event.getType();
             if (type.equals(TypeDescriptionStateManager.TYPE_STATE.ENTER_TYPE)){
                 identifierBuilder.activate();
             } else if (type.equals(TypeDescriptionStateManager.TYPE_STATE.EXIT_TYPE)){
                 identifierBuilder.deactivate();
                 buildType();
             }
         }
     }
     
     private void buildType(){
         UnresolvedTypeInfo resultType = null;
         
         if (null != primitiveType){
             resultType = primitiveType;
             primitiveType = null;
         } else if (null != voidType){
             resultType = voidType;
             voidType = null;
         } else if (identifierBuilder.hasBuiltData()){
             String[] identifier = identifierBuilder.popLastBuiltData();
             
             assert(0 != identifier.length) : "Illegal state: identifier was not built.";
             
            String[] trueName = buildDataManager.resolveAliase(identifier);
             
            resultType = new UnresolvedReferenceTypeInfo(buildDataManager.getAvailableNameSpaceSet(),trueName);
             
         } else {
             assert(false) : "Illegal state: type can not built.";
         }
         
         if (0 < arrayCount){
             resultType = UnresolvedArrayTypeInfo.getType(resultType,arrayCount);
         }
         
         arrayCount = 0;
         registBuiltData(resultType);
     }
     
     private int arrayCount;
     
     private PrimitiveTypeInfo primitiveType;
     private VoidTypeInfo voidType;
     
     private final IdentifierBuilder identifierBuilder = new IdentifierBuilder();
     
     private final BuildDataManager buildDataManager;
     private final TypeDescriptionStateManager stateMachine = new TypeDescriptionStateManager();
 }
