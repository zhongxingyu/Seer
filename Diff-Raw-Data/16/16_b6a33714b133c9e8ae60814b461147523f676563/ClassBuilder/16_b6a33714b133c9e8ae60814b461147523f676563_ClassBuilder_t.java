 package jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder;
 
 
 import java.util.Stack;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.statemanager.ClassDefinitionStateManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.statemanager.ModifiersDefinitionStateManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.statemanager.NameStateManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.statemanager.StateChangeEvent;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.statemanager.ClassDefinitionStateManager.CLASS_STATE_CHANGE;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.statemanager.StateChangeEvent.StateChangeEventType;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.visitor.AstVisitEvent;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FileInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FileInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ModifierInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedClassInfo;
 
 
 /**
  * NX̍\zsr_[D
  * 
  * @author kou-tngt
  *
  */
 public class ClassBuilder extends CompoundDataBuilder<UnresolvedClassInfo> {
 
     /**
      * ŗ^ꂽ\zf[^ǗsBuildDataManagerCCq̉߂sModifiersInterpriter
      * ftHg̏Cqr_[COr_[pďD
      * 
      * @param targetDataManager@r_[p\zf[^Ǘ
      * @param interpriter@r_[pCq̈Ӗ͎
      */
     public ClassBuilder(final BuildDataManager targetDataManager, final ModifiersInterpriter interpriter) {
         this(targetDataManager, new ModifiersBuilder(), new NameBuilder(), interpriter);
     }
 
     /**
      * ŗ^ꂽ\zf[^ǗsBuildDataManagerCCq̉߂sModifiersInterpriterC
      * Cqr_[COr_[pďD
      * 
      * @param targetDataManager@r_[p\zf[^Ǘ
      * @param interpriter@r_[pCq̈Ӗ͎
      */
     public ClassBuilder(final BuildDataManager targetDataManager, final ModifiersBuilder modifiersBuilder,
             final NameBuilder nameBuilder, final ModifiersInterpriter interpriter) {
 
         //f[^Ǘ҂nullƍD
         if (null == targetDataManager) {
             throw new NullPointerException("builderManager is null.");
         }
 
         //O\złȂƍD
         if (null == nameBuilder) {
             throw new NullPointerException("nameBuilder is null.");
         }
 
         //ԕωʒmė~̂o^D
         this.addStateManager(this.classStateManager);
         this.addStateManager(new ModifiersDefinitionStateManager());
         this.addStateManager(new NameStateManager());
 
         this.buildManager = targetDataManager;
         this.modifiersBuilder = modifiersBuilder;
         this.nameBuilder = nameBuilder;
         this.interpriter = interpriter;
 
         //Ŏgr_[o^āC炪ANeBuɂȂɎIɃrW^[̃Cxg͂悤ɂD
         //ftHgŔANeBuԂɃZbg
         this.addInnerBuilder(modifiersBuilder);
         this.addInnerBuilder(nameBuilder);
     }
 
     /**
      * o^ꂽXe[g}l[Wԕω󂯎郁\bhD
      * @param event ԕωCxg
      * @see jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.StateDrivenDataBuilder#stateChangend(jp.ac.osaka_u.ist.sel.metricstool.main.ast.statemanager.StateChangeEvent)
      */
     @Override
     public void stateChangend(final StateChangeEvent<AstVisitEvent> event) {
         final StateChangeEventType type = event.getType();
         if (type.equals(CLASS_STATE_CHANGE.ENTER_CLASS_DEF)) {
             //NX`ɓCƂ肠VNX`o^ɍs
             final AstVisitEvent trigger = event.getTrigger();
             this.startClassDefinition(trigger.getStartLine(), trigger.getStartColumn(), trigger
                     .getEndLine(), trigger.getEndColumn());
 
         } else if (type.equals(CLASS_STATE_CHANGE.EXIT_CLASS_DEF)) {
             //NX`ôŁCf[^\zIČnsD
             this.endClassDefinition();
             this.isClassNameBuit = false;
         } else if (type.equals(CLASS_STATE_CHANGE.ENTER_CLASS_BLOCK)) {
             //NX̃ubNɓ̂Ńf[^Ǘ҂ɒʒmāCO\ztO~낷
             this.isClassNameBuit = false;
             this.enterClassblock();
         } else if (this.classStateManager.isInPreDeclaration()) {
             //NXubN̑ÓCNX錾ɋꍇD
             if (type.equals(ModifiersDefinitionStateManager.MODIFIERS_STATE.ENTER_MODIFIERS_DEF)) {
                 //Cq`ɓ̂ŏCqr_ANeBu
                 this.modifiersBuilder.activate();
             } else if (type
                     .equals(ModifiersDefinitionStateManager.MODIFIERS_STATE.EXIT_MODIFIERS_DEF)) {
                 //Cq`ôŏCqr_ANeBuɂďCqo^
                 this.modifiersBuilder.deactivate();
                 this.registModifiers(this.modifiersBuilder.popLastBuiltData());
                 this.modifiersBuilder.clearBuiltData();
             } else if (type.equals(NameStateManager.NAME_STATE.ENTER_NAME) && !this.isClassNameBuit) {
                 //O`ɍŏɓɃNX\z
                 this.nameBuilder.activate();
             } else if (type.equals(NameStateManager.NAME_STATE.EXIT_NAME) && !this.isClassNameBuit) {
                 //ŏɖO`oɃNXo^āCNX\zς݂ł邱Ƃ\tO𗧂Ă
                 this.nameBuilder.deactivate();
                 this.registClassName(this.nameBuilder.getFirstBuiltData());
                 this.nameBuilder.clearBuiltData();
                 this.isClassNameBuit = true;
             }
         }
     }
 
     /**
      * NX`oɌĂяoC
      * f[^Ǘ҂ {@link BuildDataManager#endClassDefinition()} \bhʂĂʒmC
      * \z̃f[^X^bNoD
      * 
      * ̃\bhI[o[Ch邱ƂŁC
      * NX`IƂ̏CӂɕύX邱ƂłD
      */
     protected void endClassDefinition() {
         this.buildManager.endClassDefinition();
         if (!this.buildingClassStack.isEmpty()) {
             this.buildingClassStack.pop();
         }
     }
 
     /**
      * NXubNɓɌĂяoC
      * f[^\zɂ̎|ʒmD
      * 
      * ̃\bhI[o[Ch邱ƂŁC
      * NXubNɓCӂɕύX邱ƂłD
      */
     protected void enterClassblock() {
         this.buildManager.enterClassBlock();
     }
 
     /**
      * \z̃NX񂪂΂CȂnullԂD
      * 
      * @return \z̃NX񂪂΂CȂnullD
      */
     protected final UnresolvedClassInfo getBuildingClassInfo() {
         if (this.hasBuildingClassInfo()) {
             return this.buildingClassStack.peek();
         } else {
             return null;
         }
     }
 
     /**
      * \z̃NX񂪂邩ǂԂD
      * 
      * @return \z̃NX񂪂true
      */
     protected boolean hasBuildingClassInfo() {
         return !this.buildingClassStack.isEmpty();
     }
 
     /**
      * NX`ɓɌĂ΂郁\bhD
      * VBNX̃CX^X𐶐Co^邽߂̃\bh{@link #registClassInfo(UnresolvedClassInfo)}ĂԁD
      * 
      * ̃\bhI[o[Ch邱ƂŁCNX`ɓۂ̋CӂɕύX邱ƂłD
      * 
      * @param startLine NX̊Jnsԍ
      * @param startColumn NX̊Jnԍ
      * @param endLine@NX̏Isԍ
      * @param endColumn@NX̏Iԍ
      */
     protected void startClassDefinition(final int startLine, final int startColumn, final int endLine, final int endColumn) {
         final FileInfo currentFile = FileInfoManager.getInstance().getCurrentFile();
        assert null != currentFile : "Illegal state: the file information was not registered to FileInfoManager";
         
         final UnresolvedClassInfo classInfo = new UnresolvedClassInfo(currentFile);
 
         classInfo.setFromLine(startLine);
         classInfo.setFromColumn(startColumn);
         classInfo.setToLine(endLine);
         classInfo.setToColumn(endColumn);
 
         this.buildingClassStack.push(classInfo);
         this.registClassInfo(classInfo);
     }
 
     /**
      * NX`ɓCVNX쐬ƂɌĂяoC
      * f[^Ǘ҂ɑ΂ĐVNXo^D
      * ܂̏̃NX̓X^bNɂۑD
      * 
      * ̃\bhI[o[Ch邱ƂŁCNX̓o^CӂɕύX邱ƂłD
      * 
      * @param classInfo o^VKNX
      */
     protected void registClassInfo(final UnresolvedClassInfo classInfo) {
         this.registBuiltData(classInfo);
         this.buildManager.startClassDefinition(classInfo);
     }
 
     /**
      * NX񂪍\zꂽɌĂяoCݍ\z̃NXɑ΂āCOo^D
      * \z̃NXf[^X^bN甭łȂꍇ́CȂD
      * 
      * ̃\bhCӂɃI[o[Ch邱ƂŁCO̓o^CӂɕύX邱ƂłD
      * 
      * @param name \z̃NXD
      */
     protected void registClassName(final String[] name) {
         if (this.hasBuildingClassInfo() && 0 < name.length) {
             this.getBuildingClassInfo().setClassName(name[0]);
         }
     }
 
     /**
      * NX̏Cq񂪍\zꂽɌĂяoCRXgN^œo^ꂽModifierInterpriter΁C
      * ݍ\z̃NXɑ΂āCCq߂ʂo^D
      * 
      * ̃\bhI[o[Ch邱ƂŁCCq画f̓o^CӂɕύX邱ƂłD
      * 
      * @param modifiers@NXɕtꂽCq̔z
      */
     protected void registModifiers(final ModifierInfo[] modifiers) {
         if (this.hasBuildingClassInfo()) {
             final UnresolvedClassInfo buildingclass = this.getBuildingClassInfo();
             for (final ModifierInfo modifier : modifiers) {
                 buildingclass.addModifier(modifier);
             }
 
             if (null != this.interpriter) {
                 this.interpriter.interprit(modifiers, buildingclass, null);
             }
         }
     }
 
     /**
      * NX`Ɋ֘ArW^[̏ԂǗCԕωCxgʒmD
      */
     private final ClassDefinitionStateManager classStateManager = new ClassDefinitionStateManager();
 
     /**
      * \zf[^̊Ǘ
      */
     private final BuildDataManager buildManager;
 
     /**
      * Cq\zr_
      */
     private final ModifiersBuilder modifiersBuilder;
 
     /**
      * O\zr_
      */
     private final NameBuilder nameBuilder;
 
     /**
      * Cq̈Ӗ͂sϐD
      */
     private final ModifiersInterpriter interpriter;
 
     /**
      * \z̃NXf[^X^bN
      */
     private final Stack<UnresolvedClassInfo> buildingClassStack = new Stack<UnresolvedClassInfo>();
 
     /**
      * \z̃NXf[^ɑ΂ăNXłɍ\zꂽǂ\
      */
     private boolean isClassNameBuit = false;
 }
