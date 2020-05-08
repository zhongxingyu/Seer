 package jp.ac.osaka_u.ist.sel.metricstool.main.ast.java;
 
 
 import java.util.Stack;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.BuildDataManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.CompoundDataBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.IdentifierBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.java.JavaAnonymousClassStateManager.ANONYMOUSCLASS_STATE;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.statemanager.StateChangeEvent;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.statemanager.StateChangeEvent.StateChangeEventType;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.visitor.AstVisitEvent;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedReferenceTypeInfo;
 
 
 public class JavaAnonymousClassBuilder extends CompoundDataBuilder<UnresolvedClassInfo> {
 
     public JavaAnonymousClassBuilder(final BuildDataManager buildManager) {
         this(buildManager, new IdentifierBuilder());
     }
 
     public JavaAnonymousClassBuilder(final BuildDataManager buildManager, final IdentifierBuilder identifierBuilder) {
         if (null == buildManager) {
             throw new NullPointerException("buildManager is null.");
         }
 
         if (null == identifierBuilder) {
             throw new NullPointerException("identifierBuilder is null");
         }
 
         this.buildDataManager = buildManager;
         this.identifierBuilder = identifierBuilder;
 
         addStateManager(stateManager);
 
         addInnerBuilder(identifierBuilder);
     }
 
     @Override
     public void stateChangend(final StateChangeEvent<AstVisitEvent> event) {
         final StateChangeEventType type = event.getType();
         if (type.equals(ANONYMOUSCLASS_STATE.ENTER_ANONYMOUSCLASS)) {
             identifierBuilder.deactivate();
             final int builtIdentifierCount = identifierBuilder.getBuiltDataCount()
                     - builtIdentifierCountStack.pop();
             
             AstVisitEvent trigger = event.getTrigger();
             
             UnresolvedClassInfo anonymousClass = buildAnonymousClass(builtIdentifierCount);
             anonymousClass.setFromLine(trigger.getStartLine());
             anonymousClass.setFromColumn(trigger.getStartColumn());
             anonymousClass.setToLine(trigger.getEndLine());
             anonymousClass.setToColumn(trigger.getEndColumn());
             regist(anonymousClass);
             buildDataManager.enterClassBlock();
         } else if (type.equals(ANONYMOUSCLASS_STATE.EXIT_ANONYMOUSCLASS)) {
             endAnonymousClassDef();
         } else if (type.equals(ANONYMOUSCLASS_STATE.ENTER_INSTANTIATION)) {
             builtIdentifierCountStack.push(identifierBuilder.getBuiltDataCount());
 
             identifierBuilder.activate();
         }
     }
 
     protected UnresolvedClassInfo buildAnonymousClass(final int builtIdentifierCount) {
         final UnresolvedClassInfo outer = buildDataManager.getCurrentClass();
         final int anonymousCount = buildDataManager.getAnonymousClassCount(outer);
 
         final UnresolvedClassInfo anonymous = new UnresolvedClassInfo();
         anonymous.setClassName(outer.getClassName() + JAVA_ANONYMOUSCLASS_NAME_MARKER
                 + anonymousCount);
 
         String[] builtName = null;
         for (int i = 0; i < builtIdentifierCount; i++) {
             builtName = identifierBuilder.popLastBuiltData();
         }
 
         if (null != builtName) {
             final String[] trueName = buildDataManager.resolveAliase(builtName);
 
             assert (null != trueName && 0 < trueName.length) : "Illegal state: resolved super type name was empty.";
 
             final UnresolvedReferenceTypeInfo superType = new UnresolvedReferenceTypeInfo(
                     buildDataManager.getAvailableNameSpaceSet(), trueName);
             anonymous.addSuperClass(superType);
         }
 
         return anonymous;
     }
 
     protected void regist(final UnresolvedClassInfo classInfo) {
         registBuiltData(classInfo);
         buildDataManager.startClassDefinition(classInfo);
     }
 
     protected void endAnonymousClassDef() {
         buildDataManager.endClassDefinition();
     }
 
     public final static String JAVA_ANONYMOUSCLASS_NAME_MARKER = "$";
 
     private final Stack<Integer> builtIdentifierCountStack = new Stack<Integer>();
 
     private final JavaAnonymousClassStateManager stateManager = new JavaAnonymousClassStateManager();
 
     private final BuildDataManager buildDataManager;
 
     private final IdentifierBuilder identifierBuilder;
 }
