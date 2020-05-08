 package jp.ac.osaka_u.ist.sel.metricstool.main.ast.java;
 
 
 import java.util.Stack;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.BuildDataManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.CompoundDataBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.NameBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.statemanager.StateChangeEvent;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.statemanager.StateChangeEvent.StateChangeEventType;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.visitor.AstVisitEvent;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FileInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FileInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ModifierInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedClassTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedFieldInfo;
 
 
 /**
  * @author kou-tngt, t-miyake
  *
  */
 public class JavaEnumElementBuilder extends CompoundDataBuilder<UnresolvedFieldInfo> {
 
     public JavaEnumElementBuilder(BuildDataManager buildDataManager) {
 
         if (null == buildDataManager) {
             throw new NullPointerException("builderManager is null.");
         }
 
         this.buildManager = buildDataManager;
 
         addStateManager(this.stateManager);
         addInnerBuilder(this.nameBuilder);
     }
 
     @Override
     public void stateChangend(StateChangeEvent<AstVisitEvent> event) {
         StateChangeEventType type = event.getType();
         if (isActive()) {
             if (type.equals(JavaEnumElementStateManager.ENUM_ELEMENT_STATE.ENTER_ENUM_ELEMENT)) {
                 nameBuilder.clearBuiltData();
                 enumClassStack.push(buildManager.getCurrentClass());
                 nameBuilder.activate();
             } else if (type
                     .equals(JavaEnumElementStateManager.ENUM_ELEMENT_STATE.EXIT_ENUM_ELEMENT)) {
                 nameBuilder.deactivate();
                 AstVisitEvent trigger = event.getTrigger();
                 buildEnumElement(trigger.getStartLine(), trigger.getStartColumn(), trigger
                         .getEndLine(), trigger.getEndColumn());
             } else if (type
                     .equals(JavaEnumElementStateManager.ENUM_ELEMENT_STATE.ENTER_ENUM_ANONYMOUS_CLASS)) {
                 nameBuilder.deactivate();
                 AstVisitEvent trigger = event.getTrigger();
                 buildAnonymousClass(trigger.getStartLine(), trigger.getStartColumn(), trigger
                         .getEndLine(), trigger.getEndColumn());
             } else if (type
                     .equals(JavaEnumElementStateManager.ENUM_ELEMENT_STATE.EXIT_ENUM_ANONYMOUS_CLASS)) {
                 endAnonymousClass();
             }
         }
     }
 
     protected void buildAnonymousClass(int startLine, int startColumn, int endLine, int endColumn) {
         if (null != buildManager && !enumClassStack.isEmpty()) {
             UnresolvedClassInfo enumClass = enumClassStack.peek();
             
             final FileInfo currentFile = FileInfoManager.getInstance().getCurrentFile();
            assert null != currentFile : "Illegal state: the file information was not registered to FileInfoManager";
             
             UnresolvedClassInfo enumAnonymous = new UnresolvedClassInfo(currentFile);
             int count = buildManager.getAnonymousClassCount(enumClass);
             enumAnonymous.setClassName(enumClass.getClassName()
                     + JavaAnonymousClassBuilder.JAVA_ANONYMOUSCLASS_NAME_MARKER + count);
 
             UnresolvedClassTypeInfo superClassReference = new UnresolvedClassTypeInfo(
                     buildManager.getAllAvaliableNames(), enumClass.getFullQualifiedName());
 
             enumAnonymous.addSuperClass(superClassReference);
 
             enumAnonymous.setFromLine(startLine);
             enumAnonymous.setFromColumn(startColumn);
             enumAnonymous.setToLine(endLine);
             enumAnonymous.setToColumn(endColumn);
 
             buildManager.startClassDefinition(enumAnonymous);
             buildManager.enterClassBlock();
         }
     }
 
     protected void buildEnumElement(int startLine, int startColumn, int endLine, int endColumn) {
         String[] name = nameBuilder.getFirstBuiltData();
         if (null != name && name.length > 0 && !enumClassStack.isEmpty() && null != buildManager) {
             String elementName = name[0];
             UnresolvedClassInfo enumClass = enumClassStack.peek();
             UnresolvedFieldInfo element = new UnresolvedFieldInfo(elementName,
                     UnresolvedClassTypeInfo.getInstance(enumClass), enumClass);
             modifierInterpriter.interprit(defaultModifiers, element, element);
 
             element.setFromLine(startLine);
             element.setFromColumn(startColumn);
             element.setToLine(endLine);
             element.setToColumn(endColumn);
 
             buildManager.addField(element);
             enumClass.addDefinedField(element);
         }
     }
 
     protected void endAnonymousClass() {
         buildManager.endClassDefinition();
     }
 
     private final JavaEnumElementStateManager stateManager = new JavaEnumElementStateManager();
 
     private final BuildDataManager buildManager;
 
     private final NameBuilder nameBuilder = new NameBuilder();
 
     private final Stack<UnresolvedClassInfo> enumClassStack = new Stack<UnresolvedClassInfo>();
 
     private final JavaModifiersInterpriter modifierInterpriter = new JavaModifiersInterpriter();
 
     private static final ModifierInfo[] defaultModifiers = new ModifierInfo[] {
             ModifierInfo.getModifierInfo("public"), ModifierInfo.getModifierInfo("static"),
             ModifierInfo.getModifierInfo("final") };
 }
