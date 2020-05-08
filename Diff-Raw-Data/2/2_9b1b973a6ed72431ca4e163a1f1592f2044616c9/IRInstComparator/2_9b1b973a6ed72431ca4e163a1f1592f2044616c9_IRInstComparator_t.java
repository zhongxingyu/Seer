 /*
  * Copyright (C) 2011 Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  *  *
  * This program is free software: you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option)
  * any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
  * more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package fr.jamgotchian.abcd.core.ir;
 
 import com.google.common.base.Predicate;
 import fr.jamgotchian.abcd.core.common.ABCDException;
 
 /**
  *
  * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  */
 public class IRInstComparator implements IRInstVisitor<Boolean, IRInst> {
 
     private final IRInstSeq otherSeq;
 
     private final VariableMapping mapping;
 
     public static boolean equal(IRInstSeq seq, IRInstSeq otherSeq,
                                 VariableMapping mapping) {
         return seq.accept(new IRInstComparator(otherSeq, mapping), null);
     }
 
     public IRInstComparator(IRInstSeq otherSeq, VariableMapping mapping) {
         this.otherSeq = otherSeq;
         this.mapping = mapping;
     }
 
     public Boolean visit(IRInstSeq seq, IRInst arg) {
         IRInstSeq seqC = seq.copyIf(new Predicate<IRInst>() {
             public boolean apply(IRInst inst) {
                 return !inst.isIgnored();
             }
         });
         IRInstSeq otherSeqC = otherSeq.copyIf(new Predicate<IRInst>() {
             public boolean apply(IRInst inst) {
                 return !inst.isIgnored();
             }
         });
         if (seqC.size() != otherSeqC.size()) {
             return Boolean.FALSE;
         }
         for (int i = 0; i < seqC.size(); i++) {
             IRInst inst = seqC.get(i);
             IRInst otherInst = otherSeqC.get(i);
             if (Boolean.FALSE.equals(inst.accept(this, otherInst))) {
                 return Boolean.FALSE;
             }
         }
         return Boolean.TRUE;
     }
 
     public Boolean visit(ArrayLengthInst inst, IRInst arg) {
         if (!(arg instanceof ArrayLengthInst)) {
             return Boolean.FALSE;
         }
         ArrayLengthInst inst2 = (ArrayLengthInst) arg;
         return mapping.defEqual(inst.getResult(), inst2.getResult())
                 && mapping.useEqual(inst.getArray(), inst2.getArray());
     }
 
     public Boolean visit(AssignConstInst inst, IRInst arg) {
         if (!(arg instanceof AssignConstInst)) {
             return Boolean.FALSE;
         }
         AssignConstInst inst2 = (AssignConstInst) arg;
         return mapping.defEqual(inst.getResult(), inst2.getResult())
                 && inst.getConst().equals(inst2.getConst());
     }
 
     public Boolean visit(AssignVarInst inst, IRInst arg) {
         if (!(arg instanceof AssignVarInst)) {
             return Boolean.FALSE;
         }
         AssignVarInst inst2 = (AssignVarInst) arg;
         return mapping.defEqual(inst.getResult(), inst2.getResult())
                 && mapping.useEqual(inst.getValue(), inst2.getValue());
     }
 
     public Boolean visit(BinaryInst inst, IRInst arg) {
         if (!(arg instanceof BinaryInst)) {
             return Boolean.FALSE;
         }
         BinaryInst inst2 = (BinaryInst) arg;
         return mapping.defEqual(inst.getResult(), inst2.getResult())
                 && mapping.useEqual(inst.getLeft(), inst2.getLeft())
                 && mapping.useEqual(inst.getRight(), inst2.getRight())
                 && inst.getOperator() == inst2.getOperator();
     }
 
     public Boolean visit(CallMethodInst inst, IRInst arg) {
         if (!(arg instanceof CallMethodInst)) {
             return Boolean.FALSE;
         }
         CallMethodInst inst2 = (CallMethodInst) arg;
         return mapping.defEqual(inst.getResult(), inst2.getResult())
                 && mapping.useEqual(inst.getObject(), inst2.getObject())
                 && inst.getSignature().equals(inst2.getSignature())
                 && mapping.usesEqual(inst.getArguments(), inst2.getArguments());
     }
 
     public Boolean visit(CallStaticMethodInst inst, IRInst arg) {
         if (!(arg instanceof CallStaticMethodInst)) {
             return Boolean.FALSE;
         }
         CallStaticMethodInst inst2 = (CallStaticMethodInst) arg;
         return mapping.defEqual(inst.getResult(), inst2.getResult())
                 && inst.getScope().equals(inst2.getScope())
                 && inst.getSignature().equals(inst2.getSignature())
                 && mapping.usesEqual(inst.getArguments(), inst2.getArguments());
     }
 
     public Boolean visit(CastInst inst, IRInst arg) {
         if (!(arg instanceof CastInst)) {
             return Boolean.FALSE;
         }
         CastInst inst2 = (CastInst) arg;
        return mapping.defEqual(inst.getResult(), inst2.getResult())
                 && inst.getCastType().equals(inst2.getCastType())
                 && mapping.useEqual(inst.getVar(), inst2.getVar());
     }
 
     public Boolean visit(ConditionalInst inst, IRInst arg) {
         if (!(arg instanceof ConditionalInst)) {
             return Boolean.FALSE;
         }
         ConditionalInst inst2 = (ConditionalInst) arg;
         return mapping.defEqual(inst.getResult(), inst2.getResult())
                 && mapping.useEqual(inst.getCond(), inst2.getCond())
                 && mapping.useEqual(inst.getThen(), inst2.getThen())
                 && mapping.useEqual(inst.getElse(), inst2.getElse());
     }
 
     public Boolean visit(GetArrayInst inst, IRInst arg) {
         if (!(arg instanceof GetArrayInst)) {
             return Boolean.FALSE;
         }
         GetArrayInst inst2 = (GetArrayInst) arg;
         return mapping.defEqual(inst.getResult(), inst2.getResult())
                 && mapping.useEqual(inst.getArray(), inst2.getArray())
                 && mapping.useEqual(inst.getIndex(), inst2.getIndex());
     }
 
     public Boolean visit(SetArrayInst inst, IRInst arg) {
         if (!(arg instanceof SetArrayInst)) {
             return Boolean.FALSE;
         }
         SetArrayInst inst2 = (SetArrayInst) arg;
         return mapping.useEqual(inst.getArray(), inst2.getArray())
                 && mapping.useEqual(inst.getIndex(), inst2.getIndex())
                 && mapping.useEqual(inst.getValue(), inst2.getValue());
     }
 
     public Boolean visit(GetFieldInst inst, IRInst arg) {
         if (!(arg instanceof GetFieldInst)) {
             return Boolean.FALSE;
         }
         GetFieldInst inst2 = (GetFieldInst) arg;
         return mapping.defEqual(inst.getResult(), inst2.getResult())
                 && mapping.useEqual(inst.getObject(), inst2.getObject())
                 && inst.getFieldName().equals(inst2.getFieldName());
     }
 
     public Boolean visit(SetFieldInst inst, IRInst arg) {
         if (!(arg instanceof SetFieldInst)) {
             return Boolean.FALSE;
         }
         SetFieldInst inst2 = (SetFieldInst) arg;
         return mapping.useEqual(inst.getObject(), inst2.getObject())
                 && inst.getFieldName().equals(inst2.getFieldName())
                && mapping.useEqual(inst.getValue(), inst2.getValue());
     }
 
     public Boolean visit(JumpIfInst inst, IRInst arg) {
         if (!(arg instanceof JumpIfInst)) {
             return Boolean.FALSE;
         }
         JumpIfInst inst2 = (JumpIfInst) arg;
         return mapping.useEqual(inst.getCond(), inst2.getCond());
     }
 
     public Boolean visit(InstanceOfInst inst, IRInst arg) {
         if (!(arg instanceof InstanceOfInst)) {
             return Boolean.FALSE;
         }
         InstanceOfInst inst2 = (InstanceOfInst) arg;
         return mapping.defEqual(inst.getResult(), inst2.getResult())
                 && inst.getType().equals(inst2.getType())
                 && mapping.useEqual(inst.getVar(), inst2.getVar());
     }
 
     public Boolean visit(MonitorEnterInst inst, IRInst arg) {
         if (!(arg instanceof MonitorEnterInst)) {
             return Boolean.FALSE;
         }
         MonitorEnterInst inst2 = (MonitorEnterInst) arg;
         return mapping.useEqual(inst.getObj(), inst2.getObj());
     }
 
     public Boolean visit(MonitorExitInst inst, IRInst arg) {
         if (!(arg instanceof MonitorExitInst)) {
             return Boolean.FALSE;
         }
         MonitorExitInst inst2 = (MonitorExitInst) arg;
         return mapping.useEqual(inst.getObj(), inst2.getObj());
     }
 
     public Boolean visit(NewArrayInst inst, IRInst arg) {
         if (!(arg instanceof NewArrayInst)) {
             return Boolean.FALSE;
         }
         NewArrayInst inst2 = (NewArrayInst) arg;
         return mapping.defEqual(inst.getResult(), inst2.getResult())
                 && inst.getType().equals(inst2.getType())
                 && mapping.usesEqual(inst.getDimensions(), inst2.getDimensions());
     }
 
     public Boolean visit(NewObjectInst inst, IRInst arg) {
         if (!(arg instanceof NewObjectInst)) {
             return Boolean.FALSE;
         }
         NewObjectInst inst2 = (NewObjectInst) arg;
         return mapping.defEqual(inst.getResult(), inst2.getResult())
                 && inst.getType().equals(inst2.getType())
                 && mapping.usesEqual(inst.getArguments(), inst2.getArguments());
     }
 
     public Boolean visit(ReturnInst inst, IRInst arg) {
         if (!(arg instanceof ReturnInst)) {
             return Boolean.FALSE;
         }
         ReturnInst inst2 = (ReturnInst) arg;
         return mapping.useEqual(inst.getVar(), inst2.getVar());
     }
 
     public Boolean visit(SwitchInst inst, IRInst arg) {
         if (!(arg instanceof SwitchInst)) {
             return Boolean.FALSE;
         }
         SwitchInst inst2 = (SwitchInst) arg;
         return mapping.useEqual(inst.getIndex(), inst2.getIndex());
     }
 
     public Boolean visit(ThrowInst inst, IRInst arg) {
         if (!(arg instanceof ThrowInst)) {
             return Boolean.FALSE;
         }
         ThrowInst inst2 = (ThrowInst) arg;
         return mapping.useEqual(inst.getVar(), inst2.getVar());
     }
 
     public Boolean visit(UnaryInst inst, IRInst arg) {
         if (!(arg instanceof UnaryInst)) {
             return Boolean.FALSE;
         }
         UnaryInst inst2 = (UnaryInst) arg;
         return mapping.defEqual(inst.getResult(), inst2.getResult())
                 && inst.getOperator() == inst2.getOperator()
                 && mapping.useEqual(inst.getVar(), inst2.getVar());
     }
 
     public Boolean visit(ChoiceInst inst, IRInst arg) {
         if (!(arg instanceof ChoiceInst)) {
             return Boolean.FALSE;
         }
         ChoiceInst inst2 = (ChoiceInst) arg;
         return mapping.defEqual(inst.getResult(), inst2.getResult())
                 && mapping.usesEqual(inst.getChoices(), inst2.getChoices());
     }
 
     public Boolean visit(PhiInst inst, IRInst arg) {
         throw new ABCDException("Should not have Phi instruction during finally uninlining");
     }
 
     public Boolean visit(GetStaticFieldInst inst, IRInst arg) {
         if (!(arg instanceof GetStaticFieldInst)) {
             return Boolean.FALSE;
         }
         GetStaticFieldInst inst2 = (GetStaticFieldInst) arg;
         return mapping.defEqual(inst.getResult(), inst2.getResult())
                 && inst.getScope().equals(inst2.getScope())
                 && inst.getFieldName().equals(inst2.getFieldName());
     }
 
     public Boolean visit(SetStaticFieldInst inst, IRInst arg) {
         if (!(arg instanceof SetStaticFieldInst)) {
             return Boolean.FALSE;
         }
         SetStaticFieldInst inst2 = (SetStaticFieldInst) arg;
         return inst.getScope().equals(inst2.getScope())
                 && inst.getFieldName().equals(inst2.getFieldName())
                 && mapping.useEqual(inst.getValue(), inst2.getValue());
     }
 }
