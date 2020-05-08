 package jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.expression;
 
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.BuildDataManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.EntityUsageInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.UnitInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.VariableInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.VariableUsageInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedEntityUsageInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedFieldUsageInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedUnknownUsageInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedVariableUsageInfo;
 
 
 /**
  * 
  * @author kou-tngt, t-miyake
  * 
  */
 public class CompoundIdentifierElement extends IdentifierElement {
 
     public CompoundIdentifierElement(final IdentifierElement owner, final String name,
             final int fromLine, final int fromColumn, final int toLine, final int toColumn) {
         super(name, fromLine, fromColumn, toLine, toColumn);
 
         if (null == owner) {
             throw new NullPointerException("owner is null.");
         }
 
         this.owner = owner;
 
         final String[] ownerName = owner.getQualifiedName();
         final String[] thisName = new String[ownerName.length + 1];
         System.arraycopy(ownerName, 0, thisName, 0, ownerName.length);
         thisName[thisName.length - 1] = name;
         this.qualifiedName = thisName;
     }
 
     public ExpressionElement getOwner() {
         return this.owner;
     }
 
     public UnresolvedTypeInfo getType() {
         return null;
     }
 
     @Override
     public UnresolvedVariableUsageInfo<? extends VariableUsageInfo<? extends VariableInfo<? extends UnitInfo>>> resolveAsAssignmetedVariable(
             final BuildDataManager buildDataManager) {
         this.ownerUsage = this.resolveOwner(buildDataManager);
         final UnresolvedFieldUsageInfo fieldUsage = new UnresolvedFieldUsageInfo(buildDataManager
                 .getAllAvaliableNames(), this.ownerUsage, this.name, false, this.fromLine,
                 this.fromColumn, this.toLine, this.toColumn);
         buildDataManager.addVariableUsage(fieldUsage);
         
         this.usage = fieldUsage;
         
         return fieldUsage;
     }
 
     @Override
     public IdentifierElement resolveAsCalledMethod(final BuildDataManager buildDataManager) {
         this.ownerUsage = this.resolveOwner(buildDataManager);
         return this;
     }
 
     @Override
     public UnresolvedVariableUsageInfo<? extends VariableUsageInfo<? extends VariableInfo<? extends UnitInfo>>> resolveAsReferencedVariable(
             final BuildDataManager buildDataManager) {
         this.ownerUsage = this.resolveOwner(buildDataManager);
         final UnresolvedFieldUsageInfo fieldUsage = new UnresolvedFieldUsageInfo(buildDataManager
                 .getAllAvaliableNames(), this.ownerUsage, this.name, true, this.fromLine,
                 this.fromColumn, this.toLine, this.toColumn);
         buildDataManager.addVariableUsage(fieldUsage);
         
         this.usage = fieldUsage;
         
         return fieldUsage;
     }
 
     @Override
     public UnresolvedEntityUsageInfo<? extends EntityUsageInfo> resolveReferencedEntityIfPossible(
             final BuildDataManager buildDataManager) {
         this.ownerUsage = this.owner.resolveReferencedEntityIfPossible(buildDataManager);
 
         if (this.ownerUsage != null) {
             final UnresolvedFieldUsageInfo fieldUsage = new UnresolvedFieldUsageInfo(
                     buildDataManager.getAllAvaliableNames(), this.ownerUsage, this.name, true,
                     this.fromLine, this.fromColumn, this.toLine, this.toColumn);
             buildDataManager.addVariableUsage(fieldUsage);
             
             this.usage = fieldUsage;
             
             return fieldUsage;
         }
 
         return null;
     }
 
     protected UnresolvedEntityUsageInfo<? extends EntityUsageInfo> resolveOwner(final BuildDataManager buildDataManager) {
         this.ownerUsage = this.owner.resolveReferencedEntityIfPossible(buildDataManager);
 
         return null != this.ownerUsage ? this.ownerUsage : new UnresolvedUnknownUsageInfo(
                 buildDataManager.getAllAvaliableNames(), this.owner.getQualifiedName(),
                this.fromLine, this.fromColumn, this.toLine, this.toColumn);
     }
 
     private final IdentifierElement owner;
 
 }
