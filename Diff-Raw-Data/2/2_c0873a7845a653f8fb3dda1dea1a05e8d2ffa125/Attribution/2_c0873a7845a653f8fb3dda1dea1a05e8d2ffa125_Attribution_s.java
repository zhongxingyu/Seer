 package plg.gr3.parser;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import plg.gr3.data.*;
 import plg.gr3.errors.compile.*;
 import plg.gr3.parser.semfun.AndFun;
 import plg.gr3.parser.semfun.AssignationFun;
 import plg.gr3.parser.semfun.CheckDuplicateIdentifierFun;
 import plg.gr3.parser.semfun.ConcatCodeFun;
 import plg.gr3.parser.semfun.ConcatErrorsFun;
 import plg.gr3.parser.semfun.IncrementFun;
 import plg.gr3.vm.instr.*;
 import es.ucm.fdi.plg.evlib.Atribucion;
 import es.ucm.fdi.plg.evlib.Atributo;
 import es.ucm.fdi.plg.evlib.LAtributo;
 import es.ucm.fdi.plg.evlib.SAtributo;
 import es.ucm.fdi.plg.evlib.SemFun;
 import es.ucm.fdi.plg.evlib.TAtributos;
 
 /**
  * Clase de los atributos del proyecto.
  * 
  * @author PLg Grupo 03 2012/2013
  */
 @SuppressWarnings("javadoc")
 public final class Attribution extends Atribucion {
 
     /**
      * @param obj
      * @return Atributo generado al vuelo
      */
     private static Atributo a (Object obj) {
         return new LAtributo("", obj);
     }
 
     private void asigna (SAtributo attrLeft, Atributo attrRight) {
         dependencias(attrLeft, attrRight);
         calculo(attrLeft, AssignationFun.INSTANCE);
     }
 
     // Program
 
     public TAtributos program_R1 (
         TAtributos sConsts, TAtributos sTypes, TAtributos sVars, TAtributos sSubprogs, TAtributos sInsts)
     {
         regla("Program -> PROGRAM IDENT ILLAVE SConsts STypes SVars SSubprogs SInsts FLLAVE");
         TAtributos attr = atributosPara("Program", "etqh", "tsh", "err", "cod", "dirh", "ts");
 
         // Program.tsh
         calculo(attr.a("tsh"), new SemFun() {
             @Override
             public Object eval (Atributo... args) {
                 return new SymbolTable();
             }
         });
 
         // Program.err
         dependencias(attr.a("err"), sConsts.a("err"), sTypes.a("err"), sVars.a("err"), sSubprogs.a("err"), sInsts
             .a("err"));
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         // Program.cod
         dependencias(attr.a("cod"), sSubprogs.a("etq"), sSubprogs.a("cod"), sInsts.a("cod"), sVars.a("dir"));
         calculo(attr.a("cod"), new SemFun() {
             @Override
             public Object eval (Atributo... attrs) {
                 Integer jump = (Integer) attrs[0].valor();
                 Integer stackAddr = (Integer) attrs[3].valor();
 
                 NaturalValue natSP = NaturalValue.valueOf(stackAddr - 1);
                 NaturalValue natBase = NaturalValue.valueOf(stackAddr);
 
                 List<Instruction> initStack =
                     Arrays.asList(
                         new PushInstruction(natSP), new StoreInstruction(0, Type.NATURAL),
                         new PushInstruction(natBase), new StoreInstruction(1, Type.NATURAL));
 
                 return ConcatCodeFun.INSTANCE.eval(
                     a(initStack), a(new JumpInstruction(jump)), attrs[1], attrs[2], a(new StopInstruction()));
             }
         });
 
         asigna(sVars.a("nivel"), a(Scope.GLOBAL));
 
         // SConsts.tsh
         asigna(sConsts.a("tsh"), attr.a("tsh"));
 
         // STypes.tsh
         asigna(sTypes.a("tsh"), sConsts.a("ts"));
 
         // SVars.tsh
         asigna(sVars.a("tsh"), sTypes.a("ts"));
 
         // SSubprogs.tsh
         asigna(sSubprogs.a("tsh"), sVars.a("ts"));
 
         // SInsts.tsh
         asigna(sInsts.a("tsh"), sSubprogs.a("ts"));
 
         // Program.ts
         asigna(attr.a("ts"), sSubprogs.a("ts"));
 
         // SSubprogs.etqh
         asigna(sSubprogs.a("etqh"), a(5));
 
         // SInsts.etqh
         asigna(sInsts.a("etqh"), sSubprogs.a("etq"));
 
         // Program.dirh = 2
         asigna(attr.a("dirh"), a(2));
 
         asigna(sVars.a("dirh"), attr.a("dirh"));
 
         // Program.err
         dependencias(attr.a("err"), sConsts.a("err"), sTypes.a("err"), sVars.a("err"), sSubprogs.a("err"), sInsts
             .a("err"));
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         return attr;
     }
 
     // SConsts
 
     public TAtributos sConsts_R1 (TAtributos consts) {
         regla("SConst -> CONSTS ILLAVE Consts FLLAVE");
         TAtributos attr = atributosPara("SConst", "tsh", "ts", "err");
 
         asigna(consts.a("tsh"), attr.a("tsh"));
 
         asigna(attr.a("ts"), consts.a("ts"));
 
         dependencias(attr.a("err"), consts.a("err"));
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         return attr;
     }
 
     public TAtributos sConsts_R2 () {
         regla("SConst -> $");
         TAtributos attr = atributosPara("SConst", "ts", "tsh", "err", "etqh");
 
         asigna(attr.a("ts"), attr.a("tsh"));
 
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         return attr;
     }
 
     // Consts
 
     public TAtributos consts_R1 (TAtributos consts_1, TAtributos cons) {
         regla("Consts -> Consts PYC Const");
         TAtributos attr = atributosPara("Consts", "tsh", "ts", "err");
 
         asigna(consts_1.a("tsh"), attr.a("tsh"));
 
         asigna(cons.a("tsh"), consts_1.a("ts"));
 
         dependencias(attr.a("ts"), cons.a("ts"), cons.a("id"), cons.a("valor"), cons.a("tipo"), attr.a("err"));
         calculo(attr.a("ts"), new SemFun() {
             @Override
             public Object eval (Atributo... args) {
 
                 SymbolTable table = (SymbolTable) args[0].valor();
                 Lexeme ident = (Lexeme) args[1].valor();
                 Value value = (Value) args[2].valor();
                 Type type = (Type) args[3].valor();
 
                 if (ident != null) {
                     table.putConstant(ident.getLexeme(), type, value);
                 }
 
                 return table;
             }
         });
 
         dependencias(attr.a("err"), consts_1.a("err"), cons.a("err"), cons.a("ts"), cons.a("id"));
         calculo(attr.a("err"), new SemFun() {
             @Override
             public Object eval (Atributo... args) {
                 CompileError dupErr = CheckDuplicateIdentifierFun.INSTANCE.eval(args[2], args[3], a(Scope.GLOBAL));
 
                 return ConcatErrorsFun.INSTANCE.eval(args[0], args[1], a(dupErr));
             }
         });
 
         return attr;
     }
 
     public TAtributos consts_R2 (TAtributos cons) {
         regla("Consts -> Const");
         TAtributos attr = atributosPara("Consts", "tsh", "ts", "err");
 
         asigna(cons.a("tsh"), attr.a("tsh"));
 
         dependencias(attr.a("ts"), cons.a("ts"), cons.a("id"), cons.a("valor"), cons.a("tipo"), attr.a("err"));
         calculo(attr.a("ts"), new SemFun() {
             @Override
             public Object eval (Atributo... args) {
                 SymbolTable table = (SymbolTable) args[0].valor();
                 Lexeme ident = (Lexeme) args[1].valor();
                 Value value = (Value) args[2].valor();
                 Type type = (Type) args[3].valor();
 
                 if (ident != null && type != Type.ERROR && value != null) {
                     table.putConstant(ident.getLexeme(), type, value);
                 }
 
                 return table;
             }
         });
 
         dependencias(attr.a("err"), cons.a("err"), cons.a("ts"), cons.a("id"));
         calculo(attr.a("err"), new SemFun() {
 
             @Override
             public Object eval (Atributo... args) {
                 CompileError dupErr = CheckDuplicateIdentifierFun.INSTANCE.eval(args[1], args[2], a(Scope.GLOBAL));
 
                 return ConcatErrorsFun.INSTANCE.eval(args[0], a(dupErr));
             }
         });
 
         return attr;
     }
 
     // Const
 
     public TAtributos const_R1 (TAtributos tPrim, Lexeme ident, TAtributos constLit) {
         regla("Const -> CONST TPrim IDENT ASIG ConstLit");
         TAtributos attr = atributosPara("Const", "tsh", "ts", "id", "tipo", "err", "valor");
         LAtributo lexIdent = atributoLexicoPara("IDENT", "lex", ident);
 
         asigna(attr.a("ts"), attr.a("tsh"));
 
         asigna(attr.a("id"), lexIdent);
 
         asigna(attr.a("tipo"), tPrim.a("tipo"));
 
         asigna(attr.a("valor"), constLit.a("valor"));
 
         // Const.err = ¬(compatibles(TPrim.tipo, ConstLit.tipo))
         dependencias(attr.a("err"), tPrim.a("tipo"), constLit.a("tipo"), lexIdent, constLit.a("err"));
         calculo(attr.a("err"), new SemFun() {
             @Override
             public Object eval (Atributo... args) {
                 Type left = (Type) args[0].valor();
                 Type right = (Type) args[1].valor();
                 Lexeme lex = (Lexeme) args[2].valor();
 
                 List<CompileError> errs = new ArrayList<>();
 
                 if (!left.compatible(right)) {
                     if (left != Type.ERROR && right != Type.ERROR) {
                         errs.add(new AssignationTypeError(right, left, lex));
                     }
                 }
 
                 return ConcatErrorsFun.INSTANCE.eval(args[3], a(errs));
             }
         });
 
         return attr;
     }
 
     public TAtributos const_R2 () {
         regla("Const -> $");
         TAtributos attr = atributosPara("Const", "ts", "tsh", "id", "err", "dir", "dirh", "valor", "tipo");
 
         asigna(attr.a("ts"), attr.a("tsh"));
 
         asigna(attr.a("dir"), attr.a("dirh"));
 
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         return attr;
     }
 
     // ConstLit
 
     public TAtributos constLit_R1 (TAtributos lit) {
         regla("ConstLit -> Lit");
         TAtributos attr = atributosPara("ConstLit", "valor", "tipo", "err");
 
         asigna(attr.a("valor"), lit.a("valor"));
 
         asigna(attr.a("tipo"), lit.a("tipo"));
 
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         return attr;
     }
 
     public TAtributos constLit_R2 (TAtributos lit, Lexeme menos) {
         regla("ConstLit -> MENOS Lit");
         TAtributos attr = atributosPara("ConstLit", "tipo", "valor", "err");
         Atributo menosLex = atributoLexicoPara("MENOS", "lex", menos);
 
         dependencias(attr.a("tipo"), lit.a("tipo"));
         calculo(attr.a("tipo"), new SemFun() {
 
             @Override
             public Object eval (Atributo... args) {
                 Type type = (Type) args[0].valor();
 
                 return UnaryOperator.MINUS.getApplyType(type);
             }
         });
 
         dependencias(attr.a("valor"), lit.a("valor"), lit.a("tipo"));
         calculo(attr.a("valor"), new SemFun() {
             @Override
             public Object eval (Atributo... args) {
                 Value value = (Value) args[0].valor();
                 Type type = (Type) args[1].valor();
 
                 return type.isNumeric() ? UnaryOperator.MINUS.apply(value) : null;
             }
         });
 
         dependencias(attr.a("err"), lit.a("tipo"), menosLex);
         calculo(attr.a("err"), new SemFun() {
             @Override
             public Object eval (Atributo... args) {
                 Type type = (Type) args[0].valor();
                 Lexeme menosLex = (Lexeme) args[1].valor();
 
                 return type.isNumeric() ? null : new OperatorError(
                     type, UnaryOperator.MINUS, menosLex.getLine(), menosLex.getColumn());
             }
         });
 
         return attr;
     }
 
     // STypes
 
     public TAtributos sTypes_R1 (TAtributos types) {
         regla("STypes -> TIPOS ILLAVE Types FLLAVE");
         TAtributos attr = atributosPara("STypes", "tsh", "dirh", "ts", "dir", "err");
 
         asigna(attr.a("tsh"), types.a("tsh"));
 
         asigna(types.a("tsh"), attr.a("tsh"));
 
         asigna(attr.a("ts"), types.a("ts"));
 
         asigna(attr.a("dir"), attr.a("dirh"));
 
         dependencias(attr.a("err"), types.a("err"));
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         return attr;
     }
 
     public TAtributos sTypes_R2 () {
         regla("STypes -> $");
         TAtributos attr = atributosPara("STypes", "tsh", "ts", "dir", "dirh", "err");
 
         asigna(attr.a("ts"), attr.a("tsh"));
 
         asigna(attr.a("dir"), attr.a("dirh"));
 
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         return attr;
     }
 
     // Types
 
     public TAtributos types_R1 (TAtributos types_1, TAtributos type) {
         regla("Types -> Types PYC Type");
         TAtributos attr = atributosPara("Types", "tsh", "ts", "tipo", "err");
 
         asigna(types_1.a("tsh"), attr.a("tsh"));
 
         asigna(type.a("tsh"), attr.a("ts"));
 
         dependencias(attr.a("ts"), types_1.a("ts"), type.a("id"), type.a("tipo"), attr.a("err"));
         calculo(attr.a("ts"), new SemFun() {
 
             @Override
             public Object eval (Atributo... args) {
                 SymbolTable ts = (SymbolTable) args[0].valor();
                 Lexeme ident = (Lexeme) args[1].valor();
                 Type type = (Type) args[2].valor();
 
                 if (ident != null) {
                     ts.putType(ident.getLexeme(), type);
                 }
 
                 return ts;
             }
         });
 
         dependencias(attr.a("err"), types_1.a("ts"), type.a("id"), a(Scope.GLOBAL));
         calculo(attr.a("err"), CheckDuplicateIdentifierFun.INSTANCE);
 
         return attr;
     }
 
     public TAtributos types_R2 (TAtributos type) {
         regla("Types -> Type");
         TAtributos attr = atributosPara("Types", "tsh", "ts", "tipo", "err");
 
         asigna(type.a("tsh"), attr.a("tsh"));
 
         dependencias(attr.a("ts"), type.a("ts"), type.a("id"), type.a("tipo"), attr.a("err"));
         calculo(attr.a("ts"), new SemFun() {
 
             @Override
             public Object eval (Atributo... args) {
                 SymbolTable ts = (SymbolTable) args[0].valor();
                 Lexeme ident = (Lexeme) args[1].valor();
                 Type type = (Type) args[2].valor();
 
                 if (ident != null) {
                     ts.putType(ident.getLexeme(), type);
                 }
 
                 return ts;
             }
         });
 
         dependencias(type.a("err"), type.a("ts"), type.a("id"), a(Scope.GLOBAL));
         calculo(type.a("err"), CheckDuplicateIdentifierFun.INSTANCE);
 
         return attr;
     }
 
     // Type
 
     public TAtributos type_R1 (TAtributos typeDesc, Lexeme ident) {
         regla("Type -> TIPO TypeDesc IDENT");
         TAtributos attr = atributosPara("Type", "ts", "tsh", "id", "id", "clase", "nivel", "tipo", "err");
 
         asigna(attr.a("ts"), attr.a("tsh"));
 
         asigna(typeDesc.a("tsh"), attr.a("tsh"));
 
         Atributo lexIdent = atributoLexicoPara("IDENT", "lex", ident);
         asigna(attr.a("id"), lexIdent);
 
         asigna(attr.a("tipo"), typeDesc.a("tipo"));
         // Type.tipo = <t:TypeDesc.tipo, tipo:obtieneCTipo(TypeDesc), tam:desplazamiento(obtieneCTipo(TypeDesc),
         // Type.id)>
 
         calculo(attr.a("tipo"), AssignationFun.INSTANCE);
 
         return attr;
     }
 
     public TAtributos type_R2 () {
         regla("Type -> $");
         TAtributos attr = atributosPara("Type", "ts", "tsh", "id", "err", "tipo", "clase", "nivel");
 
         asigna(attr.a("ts"), attr.a("tsh"));
 
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         return attr;
     }
 
     // SVars
 
     public TAtributos sVars_R1 (TAtributos vars) {
         regla("SVars -> VARS ILLAVE Vars FLLAVE");
         TAtributos attr = atributosPara("SVars", "tsh", "ts", "id", "dirh", "dir", "err", "nivel");
 
         asigna(vars.a("nivel"), attr.a("nivel"));
 
         asigna(vars.a("tsh"), attr.a("tsh"));
 
         asigna(attr.a("ts"), vars.a("ts"));
 
         asigna(vars.a("dirh"), attr.a("dirh"));
 
         asigna(attr.a("dir"), vars.a("dir"));
 
         dependencias(attr.a("err"), vars.a("err"));
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         return attr;
     }
 
     public TAtributos sVars_R2 () {
         regla("SVars -> $");
         TAtributos attr = atributosPara("SVars", "ts", "tsh", "dir", "dirh", "err", "nivel");
 
         asigna(attr.a("ts"), attr.a("tsh"));
 
         asigna(attr.a("dir"), attr.a("dirh"));
 
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         return attr;
     }
 
     // Vars
 
     public TAtributos vars_R1 (TAtributos vars_1, TAtributos var) {
         regla("Vars -> Vars PYC Var");
         TAtributos attr = atributosPara("Vars", "tsh", "ts", "err", "dir", "dirh", "nivel");
 
         asigna(vars_1.a("tsh"), attr.a("tsh"));
 
         asigna(var.a("tsh"), vars_1.a("ts"));
 
         asigna(vars_1.a("nivel"), attr.a("nivel"));
 
         dependencias(attr.a("ts"), var.a("ts"), var.a("id"), attr.a("nivel"), vars_1.a("dir"), var.a("tipo"), attr
             .a("err"));
         calculo(attr.a("ts"), new SemFun() {
 
             @Override
             public Object eval (Atributo... args) {
                 SymbolTable ts = (SymbolTable) args[0].valor();
                 Lexeme ident = (Lexeme) args[1].valor();
                 Scope scope = (Scope) args[2].valor();
                 int address = (int) args[3].valor();
                 Type type = (Type) args[4].valor();
 
                 if (ident != null && type != Type.ERROR) {
                     ts.putVariable(ident.getLexeme(), scope, address, type);
                 }
 
                 return ts;
             }
         });
 
         asigna(vars_1.a("dirh"), attr.a("dirh"));
 
         dependencias(attr.a("dir"), vars_1.a("dir"), var.a("tipo"), attr.a("ts"));
         calculo(attr.a("dir"), new SemFun() {
             @Override
             public Object eval (Atributo... args) {
                 int varDir = (Integer) args[0].valor();
                 Type type = (Type) args[1].valor();
 
                 if (type != null) {
                     return varDir + type.getSize();
                 } else {
                     return varDir;
                 }
             }
         });
 
         dependencias(attr.a("err"), var.a("ts"), var.a("id"), var.a("nivel"), var.a("err"), vars_1.a("err"));
         calculo(attr.a("err"), new SemFun() {
 
             @Override
             public Object eval (Atributo... args) {
                 CompileError dupErr = CheckDuplicateIdentifierFun.INSTANCE.eval(args[0], args[1], args[2]);
                 return ConcatErrorsFun.INSTANCE.eval(args[3], args[4], a(dupErr));
             }
         });
 
         return attr;
     }
 
     public TAtributos vars_R2 (TAtributos var) {
         regla("Vars -> Var");
         TAtributos attr = atributosPara("Vars", "tsh", "ts", "err", "dir", "dirh", "nivel");
 
         asigna(var.a("tsh"), attr.a("tsh"));
 
         dependencias(attr.a("dir"), attr.a("dirh"), var.a("tipo"), attr.a("ts"));
         calculo(attr.a("dir"), new SemFun() {
             @Override
             public Object eval (Atributo... args) {
                 int varDir = (Integer) args[0].valor();
                 Type type = (Type) args[1].valor();
 
                 return varDir + type.getSize();
             }
         });
 
         dependencias(attr.a("ts"), var.a("ts"), var.a("id"), attr.a("nivel"), attr.a("dirh"), var.a("tipo"), attr
             .a("err"));
         calculo(attr.a("ts"), new SemFun() {
 
             @Override
             public Object eval (Atributo... args) {
                 SymbolTable ts = (SymbolTable) args[0].valor();
                 Lexeme ident = (Lexeme) args[1].valor();
                 Scope scope = (Scope) args[2].valor();
                 int address = (int) args[3].valor();
                 Type type = (Type) args[4].valor();
 
                 if (ident != null && type != Type.ERROR) {
                     ts.putVariable(ident.getLexeme(), scope, address, type);
                 }
 
                 return ts;
             }
         });
 
         dependencias(attr.a("err"), var.a("ts"), var.a("id"), var.a("nivel"), var.a("err"));
         calculo(attr.a("err"), new SemFun() {
 
             @Override
             public Object eval (Atributo... args) {
                 CompileError dupErr = CheckDuplicateIdentifierFun.INSTANCE.eval(args[0], args[1], args[2]);
                 return ConcatErrorsFun.INSTANCE.eval(args[3], a(dupErr));
             }
         });
 
         return attr;
     }
 
     // Var
 
     public TAtributos var_R1 (TAtributos typeDesc, Lexeme ident) {
         regla("Var -> VAR TypeDesc IDENT");
         TAtributos attr = atributosPara("Var", "ts", "tsh", "id", "nivel", "tipo", "err");
         Atributo lexIdent = atributoLexicoPara("IDENT", "lex", ident);
 
         asigna(attr.a("ts"), attr.a("tsh"));
 
         asigna(typeDesc.a("tsh"), attr.a("tsh"));
 
         asigna(attr.a("id"), lexIdent);
 
         asigna(attr.a("tipo"), typeDesc.a("tipo"));
 
         asigna(attr.a("err"), typeDesc.a("err"));
 
         // Var.tipo = (si (TypeDesc.tipo == TPrim) {<t:TypeDesc.tipo, tam:1>}
         // si no {<t:ref, id:Var.id, tam: desplazamiento(TypeDesc.tipo, Var.id)>} )
 
         return attr;
     }
 
     public TAtributos var_R2 () {
         regla("Var -> $");
         TAtributos attr = atributosPara("Var", "ts", "tsh", "err", "id", "nivel", "tipo");
         asigna(attr.a("ts"), attr.a("tsh"));
 
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         return attr;
     }
 
     // TypeDesc
 
     public TAtributos typeDesc_R1 (TAtributos tPrim) {
         regla("TypeDesc -> TPrim");
         TAtributos attr = atributosPara("TypeDesc", "tipo", "tsh", "err");
 
         asigna(attr.a("tipo"), tPrim.a("tipo"));
 
         return attr;
     }
 
     public TAtributos typeDesc_R2 (TAtributos tArray) {
         regla("TypeDesc -> TArray");
         TAtributos attr = atributosPara("TypeDesc", "tipo", "tsh", "err");
 
         asigna(attr.a("tipo"), tArray.a("tipo"));
 
         asigna(tArray.a("tsh"), attr.a("tsh"));
 
         asigna(attr.a("err"), tArray.a("err"));
 
         return attr;
     }
 
     public TAtributos typeDesc_R3 (TAtributos tTupla) {
         regla("TypeDesc -> TTupla");
         TAtributos attr = atributosPara("TypeDesc", "tipo", "tsh", "err");
 
         asigna(attr.a("tipo"), tTupla.a("tipo"));
 
         asigna(tTupla.a("tsh"), attr.a("tsh"));
 
         asigna(attr.a("err"), tTupla.a("err"));
 
         return attr;
     }
 
     public TAtributos typeDesc_R4 (Lexeme ident) {
         regla("TypeDesc -> IDENT");
         TAtributos attr = atributosPara("TypeDesc", "tipo", "tsh", "err");
         Atributo identLex = atributoLexicoPara("IDENT", "lex", ident);
 
         dependencias(attr.a("tipo"), attr.a("tsh"), identLex);
         calculo(attr.a("tipo"), new SemFun() {
 
             @Override
             public Object eval (Atributo... args) {
                 SymbolTable table = (SymbolTable) args[0].valor();
                 Lexeme ident = (Lexeme) args[1].valor();
 
                 if (table.hasIdentifier(ident.getLexeme())) {
                     return table.getIdentfierType(ident.getLexeme());
                 }
 
                 return Type.ERROR;
             }
         });
 
         dependencias(attr.a("err"), attr.a("tsh"), identLex);
         calculo(attr.a("err"), new SemFun() {
 
             @Override
             public Object eval (Atributo... args) {
                 SymbolTable table = (SymbolTable) args[0].valor();
                 Lexeme ident = (Lexeme) args[1].valor();
 
                 List<CompileError> errs = new ArrayList<>();
 
                 if (!table.hasIdentifier(ident.getLexeme())) {
                     errs.add(new UndefinedIdentifierError(ident.getLexeme(), ident.getLine(), ident.getColumn()));
 
                 } else {
 
                     ClassDec cd = table.getIdentfierClassDec(ident.getLexeme());
                     if (cd != ClassDec.TYPE) {
                         errs.add(new BadIdentifierClassError(
                             ident.getLexeme(), cd, ClassDec.TYPE, ident.getLine(), ident.getColumn()));
                     }
                 }
 
                 return ConcatErrorsFun.INSTANCE.eval(a(errs));
             }
         });
 
         return attr;
     }
 
     // TPrim
 
     public TAtributos tPrim_R1 () {
         regla("TPrim -> NATURAL");
         TAtributos attr = atributosPara("TPrim", "tipo");
 
         asigna(attr.a("tipo"), a(Type.NATURAL));
 
         return attr;
     }
 
     public TAtributos tPrim_R2 () {
         regla("TPrim -> INTEGER");
         TAtributos attr = atributosPara("TPrim", "tipo");
 
         asigna(attr.a("tipo"), a(Type.INTEGER));
 
         return attr;
     }
 
     public TAtributos tPrim_R3 () {
         regla("TPrim -> FLOAT");
         TAtributos attr = atributosPara("TPrim", "tipo");
 
         asigna(attr.a("tipo"), a(Type.FLOAT));
 
         return attr;
     }
 
     public TAtributos tPrim_R4 () {
         regla("TPrim -> BOOLEAN");
         TAtributos attr = atributosPara("TPrim", "tipo");
 
         asigna(attr.a("tipo"), a(Type.BOOLEAN));
 
         return attr;
     }
 
     public TAtributos tPrim_R5 () {
         regla("TPrim -> CHARACTER");
         TAtributos attr = atributosPara("TPrim", "tipo");
 
         asigna(attr.a("tipo"), a(Type.CHARACTER));
 
         return attr;
     }
 
     // Cast
 
     public TAtributos cast_R1 (Lexeme lex) {
         regla("Cast -> CHAR");
         TAtributos attr = atributosPara("Cast", "tipo", "lex");
 
         asigna(attr.a("tipo"), a(Type.CHARACTER));
 
         asigna(attr.a("lex"), atributoLexicoPara("CHAR", "lex", lex));
 
         return attr;
     }
 
     public TAtributos cast_R2 (Lexeme lex) {
         regla("Cast -> INT");
        TAtributos attr = atributosPara("Cast", "tipo");
 
         asigna(attr.a("tipo"), a(Type.INTEGER));
         asigna(attr.a("lex"), atributoLexicoPara("INT", "lex", lex));
 
         return attr;
     }
 
     public TAtributos cast_R3 (Lexeme lex) {
         regla("Cast -> NAT");
         TAtributos attr = atributosPara("Cast", "tipo", "lex");
 
         asigna(attr.a("tipo"), a(Type.NATURAL));
         asigna(attr.a("lex"), atributoLexicoPara("NAT", "lex", lex));
 
         return attr;
     }
 
     public TAtributos cast_R4 (Lexeme lex) {
         regla("Cast -> FLOAT");
         TAtributos attr = atributosPara("Cast", "tipo", "lex");
 
         asigna(attr.a("tipo"), a(Type.FLOAT));
         asigna(attr.a("lex"), atributoLexicoPara("FLOAT", "lex", lex));
 
         return attr;
     }
 
     // TArray
 
     public TAtributos tArray_R1 (TAtributos typeDesc, Lexeme ident) {
         regla("TArray -> TypeDesc ICORCHETE IDENT FCORCHETE");
         TAtributos attr = atributosPara("TArray", "tsh", "tipo", "err");
         Atributo identLex = atributoLexicoPara("IDENT", "lex", ident);
 
         asigna(typeDesc.a("tsh"), attr.a("tsh"));
 
         dependencias(attr.a("tipo"), typeDesc.a("tipo"), attr.a("tsh"), identLex);
         calculo(attr.a("tipo"), new SemFun() {
 
             @Override
             public Object eval (Atributo... args) {
                 Type type = (Type) args[0].valor();
                 SymbolTable table = (SymbolTable) args[1].valor();
                 Lexeme ident = (Lexeme) args[2].valor();
 
                 if (ident != null && type != Type.ERROR) {
                     String identStr = ident.getLexeme();
 
                     if (table.hasIdentifier(identStr) && table.getIdentfierClassDec(identStr) == ClassDec.CONSTANT
                         && table.getIdentfierType(identStr).compatible(Type.NATURAL))
                     {
                         NaturalValue val = table.getIdentifierValue(identStr, NaturalValue.class);
 
                         return new ArrayType(type, val.getValue());
                     }
                 }
 
                 return Type.ERROR;
             }
         });
 
         dependencias(attr.a("err"), attr.a("tsh"), identLex);
         calculo(attr.a("err"), new SemFun() {
 
             @Override
             public Object eval (Atributo... args) {
                 SymbolTable table = (SymbolTable) args[0].valor();
                 Lexeme ident = (Lexeme) args[1].valor();
                 String identName = ident.getLexeme();
 
                 List<CompileError> errs = new ArrayList<>();
 
                 if (!table.hasIdentifier(identName)) {
                     errs.add(new UndefinedIdentifierError(identName, ident.getLine(), ident.getColumn()));
 
                 } else {
 
                     ClassDec cd = table.getIdentfierClassDec(identName);
                     if (cd != ClassDec.CONSTANT) {
                         errs.add(new BadIdentifierClassError(identName, cd, ClassDec.CONSTANT, ident.getLine(), ident
                             .getColumn()));
                     }
 
                     Type typeFound = table.getIdentfierType(identName);
                     if (!typeFound.compatible(Type.NATURAL)) {
                         if (typeFound != Type.ERROR) {
                             errs.add(new AssignationTypeError(typeFound, Type.NATURAL, ident));
                         }
                     }
                 }
                 return ConcatErrorsFun.INSTANCE.eval(a(errs));
             }
         });
 
         return attr;
     }
 
     public TAtributos tArray_R2 (TAtributos typeDesc, Lexeme litnat) {
         regla("TArray -> TypeDesc ICORCHETE LITNAT FCORCHETE");
         TAtributos attr = atributosPara("TArray", "tipo", "tsh", "err");
         Atributo litnatLex = atributoLexicoPara("LITNAT", "lex", litnat);
 
         asigna(typeDesc.a("tsh"), attr.a("tsh"));
 
         dependencias(attr.a("err"), typeDesc.a("err"), typeDesc.a("tipo"));
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         dependencias(attr.a("tipo"), typeDesc.a("tipo"), litnatLex);
         calculo(attr.a("tipo"), new SemFun() {
 
             @Override
             public Object eval (Atributo... args) {
                 Type type = (Type) args[0].valor();
                 Lexeme litnat = (Lexeme) args[1].valor();
 
                 if (type != Type.ERROR) {
                     return new ArrayType(type, Integer.parseInt(litnat.getLexeme(), 10));
                 }
 
                 return Type.ERROR;
             }
         });
 
         return attr;
     }
 
     // TTupla
 
     public TAtributos tTupla_R1 (TAtributos tupla) {
         regla("TTupla -> IPAR Tupla FPAR");
         TAtributos attr = atributosPara("TTupla", "tipo", "tsh", "err");
 
         asigna(tupla.a("tsh"), attr.a("tsh"));
 
         asigna(attr.a("tipo"), tupla.a("tipo"));
 
         dependencias(attr.a("err"), tupla.a("err"));
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         return attr;
     }
 
     public TAtributos tTupla_R2 () {
         regla("TTupla -> IPAR FPAR");
         TAtributos attr = atributosPara("TTupla", "tipo", "err");
 
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         return attr;
     }
 
     // Tupla
 
     public TAtributos tupla_R1 (TAtributos typeDesc, TAtributos tupla_1) {
         regla("Tupla -> TypeDesc COMA Tupla");
         TAtributos attr = atributosPara("Tupla", "tipo", "tsh", "err");
 
         asigna(typeDesc.a("tsh"), attr.a("tsh"));
 
         dependencias(tupla_1.a("tsh"), attr.a("tsh"));
         calculo(typeDesc.a("tsh"), AssignationFun.INSTANCE);
 
         dependencias(attr.a("tipo"), typeDesc.a("tipo"), tupla_1.a("tipo"));
         calculo(attr.a("tipo"), new SemFun() {
             @Override
             public Object eval (Atributo... args) {
                 Type type = (Type) args[0].valor();
                 TupleType ttype = (TupleType) args[1].valor();
 
                 List<Type> types = new ArrayList<>();
                 types.add(type);
                 types.addAll(ttype.getSubtypes());
 
                 return new TupleType(types);
             }
         });
 
         dependencias(attr.a("err"), typeDesc.a("err"), tupla_1.a("err"));
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         return attr;
     }
 
     public TAtributos tupla_R2 (TAtributos typeDesc) {
         regla("Tupla -> TypeDesc");
         TAtributos attr = atributosPara("Tupla", "tipo", "tsh", "err");
 
         dependencias(attr.a("tipo"), typeDesc.a("tipo"));
         calculo(attr.a("tipo"), new SemFun() {
 
             @Override
             public Object eval (Atributo... args) {
                 Type type = (Type) args[0].valor();
 
                 return new TupleType(Arrays.asList(type));
             }
         });
 
         asigna(typeDesc.a("tsh"), attr.a("tsh"));
 
         dependencias(attr.a("err"), typeDesc.a("err"));
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         return attr;
     }
 
     // SInsts
 
     public TAtributos sInsts_R1 (TAtributos insts) {
         regla("SInsts -> INSTRUCTIONS ILLAVE Insts FLLAVE");
         TAtributos attr = atributosPara("SInsts", "cod", "etq", "etqh", "tsh", "err");
 
         asigna(insts.a("tsh"), attr.a("tsh"));
 
         dependencias(attr.a("err"), insts.a("err"));
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         asigna(attr.a("cod"), insts.a("cod"));
 
         asigna(insts.a("etqh"), attr.a("etqh"));
 
         asigna(attr.a("etq"), insts.a("etq"));
 
         return attr;
     }
 
     // Insts
 
     public TAtributos insts_R1 (TAtributos insts_1, TAtributos inst) {
         regla("Insts -> Insts PYC Inst");
         TAtributos attr = atributosPara("Insts", "cod", "etqh", "etq", "tsh", "err");
 
         asigna(insts_1.a("tsh"), attr.a("tsh"));
 
         asigna(inst.a("tsh"), attr.a("tsh"));
 
         dependencias(attr.a("err"), insts_1.a("err"), inst.a("err"));
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         dependencias(attr.a("cod"), insts_1.a("cod"), inst.a("cod"));
         calculo(attr.a("cod"), ConcatCodeFun.INSTANCE);
 
         asigna(insts_1.a("etqh"), attr.a("etqh"));
 
         asigna(inst.a("etqh"), insts_1.a("etq"));
 
         asigna(attr.a("etq"), inst.a("etq"));
 
         return attr;
     }
 
     public TAtributos insts_R2 (TAtributos inst) {
         regla("Insts -> Inst");
         TAtributos attr = atributosPara("Insts", "cod", "etqh", "etq", "tsh", "err");
 
         asigna(inst.a("tsh"), attr.a("tsh"));
 
         dependencias(attr.a("err"), inst.a("err"));
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         asigna(attr.a("cod"), inst.a("cod"));
 
         asigna(inst.a("etqh"), attr.a("etqh"));
 
         asigna(attr.a("etq"), inst.a("etq"));
 
         return attr;
     }
 
     // Inst
 
     public TAtributos inst_R1 (TAtributos desig, TAtributos expr) {
         regla("Inst -> Desig ASIG Expr");
         TAtributos attr = atributosPara("Inst", "cod", "etqh", "etq", "tsh", "err");
 
         asigna(desig.a("tsh"), attr.a("tsh"));
 
         asigna(expr.a("refh"), a(false));
 
         asigna(expr.a("tsh"), attr.a("tsh"));
 
         // Inst.err = (¬asignacionValida(Desig.tipo, Expr.tipo)) ∨ Expr.err ∨ Desig.err
         dependencias(
             attr.a("err"), desig.a("tipo"), expr.a("tipo"), expr.a("err"), desig.a("err"), desig.a("id"), desig
                 .a("const"));
         calculo(attr.a("err"), new SemFun() {
             @Override
             public Object eval (Atributo... args) {
                 Type desigType = (Type) args[0].valor();
                 Type exprType = (Type) args[1].valor();
                 Lexeme lex = (Lexeme) args[4].valor();
                 Boolean isconst = (Boolean) args[5].valor();
 
                 List<CompileError> errs = new ArrayList<>();
 
                 if (!desigType.compatible(exprType)) {
                     if (exprType != Type.ERROR && desigType != Type.ERROR) {
                         errs.add(new AssignationTypeError(exprType, desigType, lex));
                     }
                 }
 
                 if (isconst) {
                     errs.add(new AssignationToConstantError(lex.getLexeme(), lex.getLine(), lex.getColumn()));
                 }
 
                 return ConcatErrorsFun.INSTANCE.eval(args[2], args[3], a(errs));
             }
         });
 
         dependencias(attr.a("cod"), expr.a("cod"), desig.a("cod"), desig.a("tipo"));
         calculo(attr.a("cod"), new SemFun() {
             @Override
             public Object eval (Atributo... args) {
                 Type type = (Type) args[2].valor();
 
                 Instruction instr =
                     type.isPrimitive() ? new IndirectStoreInstruction(type) : new MoveInstruction(type.getSize());
                 return ConcatCodeFun.INSTANCE.eval(args[0], args[1], a(instr));
             }
         });
 
         asigna(expr.a("etqh"), attr.a("etqh"));
 
         asigna(desig.a("etqh"), expr.a("etq"));
 
         dependencias(attr.a("etq"), desig.a("etq"));
         calculo(attr.a("etq"), new IncrementFun(1));
 
         return attr;
     }
 
     public TAtributos inst_R2 (TAtributos desig) {
         regla("Inst -> IN IPAR Desig FPAR");
         TAtributos attr = atributosPara("Inst", "cod", "etqh", "etq", "tsh", "err");
 
         asigna(desig.a("tsh"), attr.a("tsh"));
 
         dependencias(attr.a("err"), desig.a("err"));
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         dependencias(attr.a("cod"), desig.a("tipo"), desig.a("cod"));
         calculo(attr.a("cod"), new SemFun() {
             @Override
             public Object eval (Atributo... args) {
                 Type type = (Type) args[0].valor();
                 // List<Instruction> code = (List<Instruction>) args[1].valor();
 
                 return ConcatCodeFun.INSTANCE.eval(
                     a(new InputInstruction(type)), args[1], a(new IndirectStoreInstruction(type)));
             }
         });
 
         dependencias(desig.a("etqh"), attr.a("etqh"));
         calculo(desig.a("etqh"), new IncrementFun(1));
 
         dependencias(attr.a("etq"), desig.a("etq"));
         calculo(attr.a("etq"), new IncrementFun(1));
 
         return attr;
     }
 
     public TAtributos inst_R3 (TAtributos expr) {
         regla("Inst -> OUT IPAR Expr FPAR");
         TAtributos attr = atributosPara("Inst", "cod", "etq", "etqh", "tsh", "err");
 
         asigna(expr.a("tsh"), attr.a("tsh"));
 
         asigna(expr.a("refh"), a(false));
 
         dependencias(attr.a("err"), expr.a("err"));
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         dependencias(attr.a("cod"), expr.a("cod"), a(new OutputInstruction()));
         calculo(attr.a("cod"), ConcatCodeFun.INSTANCE);
 
         asigna(expr.a("etqh"), attr.a("etqh"));
 
         dependencias(attr.a("etq"), expr.a("etq"));
         calculo(attr.a("etq"), new IncrementFun(1));
 
         return attr;
     }
 
     public TAtributos inst_R4 () {
         regla("Inst -> SWAP1 IPAR FPAR");
         TAtributos attr = atributosPara("Inst", "cod", "etq", "etqh", "err", "tsh");
 
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         asigna(attr.a("cod"), a(new Swap1Instruction()));
 
         dependencias(attr.a("etq"), attr.a("etqh"));
         calculo(attr.a("etq"), new IncrementFun(1));
 
         return attr;
     }
 
     public TAtributos inst_R5 () {
         regla("Inst -> SWAP2 IPAR FPAR");
         TAtributos attr = atributosPara("Inst", "cod", "etq", "etqh", "err", "tsh");
 
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         asigna(attr.a("cod"), a(new Swap2Instruction()));
 
         dependencias(attr.a("etq"), attr.a("etqh"));
         calculo(attr.a("etq"), new IncrementFun(1));
 
         return attr;
     }
 
     public TAtributos inst_R6 (TAtributos expr, TAtributos insts, TAtributos elseIf) {
         regla("Inst -> IF Expr THEN Insts ElseIf");
         TAtributos attr = atributosPara("Inst", "etqh", "etq", "tsh", "err", "cod");
 
         asigna(expr.a("tsh"), attr.a("tsh"));
 
         asigna(expr.a("refh"), a(false));
 
         asigna(insts.a("tsh"), attr.a("tsh"));
 
         asigna(elseIf.a("tsh"), attr.a("tsh"));
 
         dependencias(attr.a("err"), expr.a("err"), insts.a("err"), elseIf.a("err"));
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         asigna(expr.a("etqh"), attr.a("etqh"));
 
         dependencias(insts.a("etqh"), expr.a("etq"));
         calculo(insts.a("etqh"), new IncrementFun(1));
 
         dependencias(elseIf.a("etqh"), insts.a("etq"));
         calculo(elseIf.a("etqh"), new IncrementFun(1));
 
         asigna(attr.a("etq"), elseIf.a("etq"));
 
         dependencias(attr.a("cod"), expr.a("cod"), insts.a("cod"), elseIf.a("cod"), insts.a("etq"), elseIf.a("etq"));
         calculo(attr.a("cod"), new SemFun() {
             @Override
             public Object eval (Atributo... attrs) {
                 Integer instsEtq = (Integer) attrs[3].valor();
                 Integer elseEtq = (Integer) attrs[4].valor();
 
                 Instruction code1 = new BranchInstruction(instsEtq + 1, BooleanValue.FALSE);
                 Instruction code2 = new JumpInstruction(elseEtq);
 
                 return ConcatCodeFun.INSTANCE.eval(attrs[0], a(code1), attrs[1], a(code2), attrs[2]);
             }
 
         });
 
         return attr;
     }
 
     public TAtributos inst_R7 (TAtributos expr, TAtributos insts) {
         regla("Inst -> WHILE Expr DO Insts ENDWHILE");
         TAtributos attr = atributosPara("Inst", "etqh", "etq", "tsh", "err", "cod");
 
         asigna(expr.a("tsh"), attr.a("tsh"));
 
         asigna(expr.a("refh"), a(false));
 
         asigna(insts.a("tsh"), attr.a("tsh"));
 
         dependencias(attr.a("err"), expr.a("err"), insts.a("err"));
         calculo(attr.a("err"), new SemFun() {
             @Override
             public Object eval (Atributo... args) {
                 return ConcatErrorsFun.INSTANCE.eval(args[0], args[1]);
             }
         });
 
         asigna(expr.a("etqh"), attr.a("etqh"));
 
         dependencias(insts.a("etqh"), expr.a("etq"));
         calculo(insts.a("etqh"), new IncrementFun(1));
 
         dependencias(attr.a("etq"), insts.a("etq"));
         calculo(attr.a("etq"), new IncrementFun(1));
 
         dependencias(attr.a("cod"), expr.a("cod"), insts.a("cod"), insts.a("etq"), attr.a("etqh"));
         calculo(attr.a("cod"), new SemFun() {
             @Override
             public Object eval (Atributo... attrs) {
                 Integer instsEtq = (Integer) attrs[2].valor();
                 Integer attrEtqh = (Integer) attrs[3].valor();
 
                 Instruction code1 = new BranchInstruction(instsEtq + 1, BooleanValue.FALSE);
                 Instruction code2 = new JumpInstruction(attrEtqh);
 
                 return ConcatCodeFun.INSTANCE.eval(attrs[0], a(code1), attrs[1], a(code2));
             }
 
         });
 
         return attr;
     }
 
     public TAtributos inst_R8 (TAtributos instCall) {
         regla("Inst -> InstCall");
         TAtributos attr = atributosPara("Inst", "etqh", "etq", "tsh", "err", "cod");
 
         asigna(instCall.a("tsh"), attr.a("tsh"));
 
         dependencias(attr.a("err"), instCall.a("err"));
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         dependencias(attr.a("cod"), instCall.a("cod"));
         calculo(attr.a("cod"), ConcatCodeFun.INSTANCE);
 
         asigna(instCall.a("etqh"), attr.a("etqh"));
 
         asigna(attr.a("etq"), instCall.a("etq"));
 
         return attr;
     }
 
     public TAtributos inst_R9 () {
         regla("Inst -> $");
         TAtributos attr = atributosPara("Inst", "etqh", "etq", "tsh", "err", "cod");
 
         asigna(attr.a("etq"), attr.a("etqh"));
 
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         calculo(attr.a("cod"), ConcatCodeFun.INSTANCE);
 
         return attr;
     }
 
     // ElseIf
 
     public TAtributos elseIf_R1 (TAtributos insts) {
         regla("ElseIf -> ELSE Insts ENDIF");
         TAtributos attr = atributosPara("ElseIf", "tsh", "err", "cod", "etq", "etqh");
 
         asigna(insts.a("tsh"), attr.a("tsh"));
 
         dependencias(attr.a("err"), insts.a("err"));
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         dependencias(attr.a("cod"), insts.a("cod"));
         calculo(attr.a("cod"), ConcatCodeFun.INSTANCE);
 
         asigna(insts.a("etqh"), attr.a("etqh"));
 
         asigna(attr.a("etq"), insts.a("etq"));
 
         return attr;
     }
 
     public TAtributos elseIf_R2 () {
         regla("ElseIf -> ENDIF");
         TAtributos attr = atributosPara("ElseIf", "err", "cod", "tsh", "etq", "etqh");
 
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         calculo(attr.a("cod"), ConcatCodeFun.INSTANCE);
 
         asigna(attr.a("etq"), attr.a("etqh"));
 
         return attr;
     }
 
     // InstCall
 
     public TAtributos instCall_R1 (Lexeme ident, TAtributos srParams) {
         regla("InstCall -> CALL IDENT IPAR SRParams FPAR");
         TAtributos attr =
             atributosPara(
                 "InstCall", "tsh", "nparams", "nombresubprogh", "listaparamnombres", "err", "cod", "etqh", "etq");
         Atributo identLex = atributoLexicoPara("IDENT", "lex", ident);
 
         asigna(srParams.a("tsh"), attr.a("tsh"));
 
         asigna(srParams.a("nparamsh"), a(0));
 
         asigna(srParams.a("nombresubprogh"), identLex);
 
         dependencias(srParams.a("listaparamnombresh"), identLex);
         calculo(srParams.a("listaparamnombresh"), new SemFun() {
 
             @Override
             public Object eval (Atributo... args) {
                 return Collections.checkedList(new ArrayList<Lexeme>(), Lexeme.class);
             }
         });
 
         dependencias(attr.a("err"), srParams.a("err"), srParams.a("tsh"), identLex, srParams.a("nparams"), srParams
             .a("listaparamnombres"));
         calculo(attr.a("err"), new SemFun() {
             @Override
             public Object eval (Atributo... args) {
                 SymbolTable ts = (SymbolTable) args[1].valor();
                 Lexeme ident = (Lexeme) args[2].valor();
                 Integer nparams = (Integer) args[3].valor();
                 List<Lexeme> lparNames = (List<Lexeme>) args[4].valor();
 
                 List<CompileError> errs = new ArrayList<>();
 
                 // Comprobamos que el identificador exista en la tabla de símbolos
                 if (!ts.hasIdentifier(ident.getLexeme())) {
                     errs.add(new UndefinedIdentifierError(ident.getLexeme(), ident.getLine(), ident.getColumn()));
 
                 } else {
                     Integer numParamsFormales = ts.getIdentifierParams(ident.getLexeme()).size();
 
                     // Comprobamos que el numero de parametros con el que llamamos a la función sea el mismo con el
                     // que esta declarado
                     if (nparams.intValue() != numParamsFormales.intValue()) {
                         errs.add(new InvalidNumberOfParametersError(ident, nparams, numParamsFormales));
                     }
 
                     // Comprobamos que no se han definido parámetros varias veces
                     for (int i = 0; i < lparNames.size(); i++) {
                         Lexeme param = lparNames.get(i);
 
                         boolean cont = true;
                         for (int j = i + 1; j < lparNames.size() && cont; j++) {
                             if (lparNames.get(j).getLexeme().equals(param.getLexeme())) {
                                 errs.add(new DuplicateParameterError(param.getLexeme(), param.getLine(), param
                                     .getColumn()));
                                 cont = false;
                             }
                         }
                     }
                 }
 
                 return ConcatErrorsFun.INSTANCE.eval(args[0], a(errs));
             }
         });
 
         dependencias(attr.a("cod"), srParams.a("cod"), attr.a("tsh"), identLex, srParams.a("etq"));
         calculo(attr.a("cod"), new SemFun() {
 
             @Override
             public Object eval (Atributo... args) {
                 SymbolTable table = (SymbolTable) args[1].valor();
                 Lexeme ident = (Lexeme) args[2].valor();
                 Integer paramsEtq = (Integer) args[3].valor();
 
                 if (!table.hasIdentifier(ident.getLexeme())
                     || table.getIdentfierClassDec(ident.getLexeme()) != ClassDec.SUBPROGRAM)
                 {
                     return ConcatCodeFun.INSTANCE.eval();
                 }
 
                 int funAddr = table.getIdentifierAddress(ident.getLexeme());
                 NaturalValue retAddrValue = NaturalValue.valueOf(paramsEtq + 7);
 
                 // Paso 1: Restructurar los punteros SP y FP
                 // Para ello, siendo SP=M[0] y FP=M[1]:
                 // . M[SP+1] = RETURNADDR
                 // . M[SP+3] = FP <-- Usamos esto como base para los parámetros
                 // . SP = SP + 2
                 // Lo cual se traduce en
                 // PUSH(RETADDR), LOAD(0), PUSH(1), ADD, STORE-IND,
                 // . LOAD(1), LOAD(0), PUSH(2), ADD, STORE-IND,
                 // . LOAD(0), PUSH(3), ADD, STORE(0),
                 // Con esto almacenamos la dir. retorno, actualizamos SP y FP y los dejamos preparados.
                 List<Instruction> code1 =
                     Arrays.asList(
                         new PushInstruction(retAddrValue), new LoadInstruction(0, Type.NATURAL), new PushInstruction(
                             NaturalValue.valueOf(1)),
                         new BinaryOperatorInstruction(BinaryOperator.ADDITION),
                         new IndirectStoreInstruction(Type.NATURAL),
                         // --
                         new LoadInstruction(1, Type.NATURAL), new LoadInstruction(0, Type.NATURAL),
                         new PushInstruction(NaturalValue.valueOf(2)), new BinaryOperatorInstruction(
                             BinaryOperator.ADDITION), new IndirectStoreInstruction(Type.NATURAL),
                         // --
                         new LoadInstruction(0, Type.NATURAL), new PushInstruction(NaturalValue.valueOf(3)),
                         new BinaryOperatorInstruction(BinaryOperator.ADDITION), new StoreInstruction(0, Type.NATURAL)
 
                     );
 
                 // Paso 2: Movimiento de parámetros.
                 // Esto, por suerte, lohace SRParams y compañía. Preguntar allí.
                 Atributo code2 = args[0];
 
                 List<Parameter> params = table.getIdentifierParams(ident.getLexeme());
                 int totalParamSize = 0;
                 for (Parameter param : params) {
                     totalParamSize += param.getType().getSize();
                 }
 
                 // Paso 3: Saltar!
                 // Es decir:
                 // . LOAD(0), STORE(1)
                 // . LOAD(0), PUSH(<tamaño-total-parametros> - 1), ADD, STORE(0)
                 // . JUMP(FUNADDR)
                 List<Instruction> code3 =
                     Arrays.asList(
                         new LoadInstruction(0, Type.NATURAL), new StoreInstruction(1, Type.NATURAL),
                         // --
                         new LoadInstruction(0, Type.NATURAL),
                         new PushInstruction(NaturalValue.valueOf(totalParamSize - 1)), new BinaryOperatorInstruction(
                             BinaryOperator.ADDITION), new StoreInstruction(0, Type.NATURAL),
                         // --
                         new JumpInstruction(funAddr));
 
                 // Paso 4:
                 // Volvemos de la función, toca deshacer:
                 // . SP = FP - 3
                 // . FP = M[FP-1]
                 // Es decir
                 // . LOAD(1), PUSH(3), SUB, STORE(0),
                 // . LOAD(1), PUSH(1), SUB, LOAD-IND, STORE(1),
                 List<Instruction> code4 =
                     Arrays.asList(
                         new LoadInstruction(1, Type.NATURAL), new PushInstruction(NaturalValue.valueOf(3)),
                         new BinaryOperatorInstruction(BinaryOperator.SUBTRACTION),
                         new StoreInstruction(0, Type.NATURAL),
                         // --
                         new LoadInstruction(1, Type.NATURAL), new PushInstruction(NaturalValue.valueOf(1)),
                         new BinaryOperatorInstruction(BinaryOperator.SUBTRACTION), new IndirectLoadInstruction(
                             Type.NATURAL), new StoreInstruction(1, Type.NATURAL));
 
                 return ConcatCodeFun.INSTANCE.eval(a(code1), code2, a(code3), a(code4));
             }
         });
 
         dependencias(srParams.a("etqh"), attr.a("etqh"));
         calculo(srParams.a("etqh"), new IncrementFun(14));
 
         dependencias(attr.a("etq"), srParams.a("etq"));
         calculo(attr.a("etq"), new IncrementFun(16));
 
         return attr;
     }
 
     // SRParams
 
     public TAtributos srParams_R1 (TAtributos rParams) {
         regla("SRParams -> RParams");
         TAtributos attr =
             atributosPara(
                 "SRParams", "tsh", "err", "cod", "etq", "etqh", "nparams", "nparamsh", "nombresubprog",
                 "nombresubprogh", "listaparamnombresh", "listaparamnombres");
 
         asigna(rParams.a("tsh"), attr.a("tsh"));
 
         dependencias(attr.a("err"), rParams.a("err"));
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         dependencias(attr.a("cod"), rParams.a("cod"));
         calculo(attr.a("cod"), ConcatCodeFun.INSTANCE);
 
         asigna(rParams.a("etqh"), attr.a("etqh"));
 
         asigna(attr.a("etq"), rParams.a("etq"));
 
         asigna(rParams.a("nparamsh"), attr.a("nparamsh"));
 
         asigna(attr.a("nparams"), rParams.a("nparams"));
 
         asigna(rParams.a("nombresubprogh"), attr.a("nombresubprogh"));
 
         asigna(rParams.a("listaparamnombresh"), a(new ArrayList<Lexeme>()));
 
         asigna(attr.a("listaparamnombres"), rParams.a("listaparamnombres"));
 
         return attr;
     }
 
     public TAtributos srParams_R2 () {
         regla("SRParams -> $");
         TAtributos attr =
             atributosPara(
                 "SRParams", "err", "cod", "etqh", "etq", "nparamsh", "nparams", "listaparamnombres",
                 "listaparamnombresh", "nombresubprog", "nombresubprogh", "tsh");
 
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         calculo(attr.a("cod"), ConcatCodeFun.INSTANCE);
 
         asigna(attr.a("etq"), attr.a("etqh"));
 
         asigna(attr.a("nparams"), attr.a("nparamsh"));
 
         asigna(attr.a("listaparamnombres"), attr.a("listaparamnombresh"));
 
         return attr;
     }
 
     // RParams
 
     public TAtributos rParams_R1 (TAtributos rParams_1, TAtributos rParam) {
         regla("RParams -> RParams COMA RParam");
         TAtributos attr =
             atributosPara(
                 "RParams", "tsh", "err", "cod", "nparamsh", "nparams", "nombresubprogh", "etqh", "etq",
                 "listaparamnombresh", "listaparamnombres");
 
         asigna(rParams_1.a("tsh"), attr.a("tsh"));
 
         asigna(rParam.a("tsh"), attr.a("tsh"));
 
         dependencias(attr.a("err"), rParams_1.a("err"), rParam.a("err"));
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         dependencias(attr.a("cod"), rParams_1.a("cod"), rParam.a("cod"));
         calculo(attr.a("cod"), ConcatCodeFun.INSTANCE);
 
         asigna(rParams_1.a("nparamsh"), attr.a("nparamsh"));
 
         asigna(rParam.a("nparamsh"), rParams_1.a("nparams"));
 
         asigna(attr.a("nparams"), rParam.a("nparams"));
 
         asigna(rParams_1.a("etqh"), attr.a("etqh"));
 
         asigna(rParam.a("etqh"), rParams_1.a("etq"));
 
         asigna(attr.a("etq"), rParam.a("etq"));
 
         asigna(rParams_1.a("nombresubprogh"), attr.a("nombresubprogh"));
 
         asigna(rParam.a("nombresubprogh"), attr.a("nombresubprogh"));
 
         asigna(rParams_1.a("listaparamnombresh"), attr.a("listaparamnombresh"));
 
         asigna(rParam.a("listaparamnombresh"), rParams_1.a("listaparamnombres"));
 
         asigna(attr.a("listaparamnombres"), rParam.a("listaparamnombres"));
 
         return attr;
     }
 
     public TAtributos rParams_R2 (TAtributos rParam) {
         regla("RParams -> RParam");
         TAtributos attr =
             atributosPara(
                 "RParams", "tsh", "err", "cod", "etq", "etqh", "nparams", "nparamsh", "nombresubprogh",
                 "listaparamnombresh", "listaparamnombres");
 
         asigna(rParam.a("tsh"), attr.a("tsh"));
 
         dependencias(attr.a("cod"), rParam.a("cod"));
         calculo(attr.a("cod"), ConcatCodeFun.INSTANCE);
 
         dependencias(attr.a("err"), rParam.a("err"));
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         asigna(rParam.a("etqh"), attr.a("etqh"));
 
         asigna(attr.a("etq"), rParam.a("etq"));
 
         asigna(rParam.a("nparamsh"), attr.a("nparamsh"));
 
         asigna(attr.a("nparams"), rParam.a("nparams"));
 
         asigna(rParam.a("nombresubprogh"), attr.a("nombresubprogh"));
 
         asigna(rParam.a("listaparamnombresh"), attr.a("listaparamnombresh"));
 
         asigna(attr.a("listaparamnombres"), rParam.a("listaparamnombres"));
 
         return attr;
     }
 
     // RParam
 
     public TAtributos rParam_R1 (Lexeme ident, TAtributos expr) {
         regla("RParam -> IDENT ASIG Expr");
         TAtributos attr =
             atributosPara(
                 "RParam", "tsh", "cod", "etq", "etqh", "nparams", "nparamsh", "nombresubprog", "nombresubprogh",
                 "tipo", "desig", "err", "listaparamnombres", "listaparamnombresh");
         Atributo identLex = atributoLexicoPara("IDENT", "lex", ident);
 
         asigna(expr.a("tsh"), attr.a("tsh"));
 
         dependencias(expr.a("refh"), attr.a("tsh"), identLex, attr.a("nombresubprogh"));
         calculo(expr.a("refh"), new SemFun() {
 
             @Override
             public Object eval (Atributo... args) {
                 SymbolTable table = (SymbolTable) args[0].valor();
                 Lexeme identParamReal = (Lexeme) args[1].valor();
                 Lexeme identSubprog = (Lexeme) args[2].valor();
 
                 if (table.hasIdentifier(identSubprog.getLexeme())) {
                     List<Parameter> parametros = table.getIdentifierParams(identSubprog.getLexeme());
 
                     boolean hasParam = false;
                     Parameter paramFormal = null;
                     for (Parameter param : parametros) {
                         if (identParamReal.getLexeme().equals(param.getName())) {
                             hasParam = true;
                             paramFormal = param;
                         }
                     }
                     return hasParam && paramFormal.isReference();
                 } else {
                     return false;
                 }
             }
         });
 
         dependencias(
             attr.a("err"), expr.a("err"), expr.a("tsh"), identLex, attr.a("tsh"), attr.a("nombresubprogh"), expr
                 .a("tipo"), expr.a("desig"), attr.a("listaparamnombresh"));
         calculo(attr.a("err"), new SemFun() {
             @Override
             public Object eval (Atributo... args) {
                 SymbolTable table = (SymbolTable) args[1].valor();
                 Lexeme identParamReal = (Lexeme) args[2].valor();
                 Lexeme identSubprog = (Lexeme) args[4].valor();
                 List<Lexeme> lparNames = (List<Lexeme>) args[7].valor();
 
                 List<CompileError> errors = new ArrayList<>();
 
                 if (table.hasIdentifier(identSubprog.getLexeme())) {
 
                     List<Parameter> parametros = table.getIdentifierParams(identSubprog.getLexeme());
 
                     // Comprobamos que el identificador del parámetro real esté declarado como parámetro en la TS
                     boolean hasParam = false;
                     Parameter paramFormal = null;
                     for (Parameter param : parametros) {
                         if (identParamReal.getLexeme().equals(param.getName())) {
                             hasParam = true;
                             paramFormal = param;
                         }
                     }
                     if (!hasParam) {
                         errors.add(new UndefinedIdentifierError(
                             identParamReal.getLexeme(), identParamReal.getLine(), identParamReal.getColumn()));
                     }
 
                     // Comprobamos que el tipo del parámetro real se pueda asignar al tipo del parámetro formal
 // declarado.
                     if (paramFormal != null) {
                         Type exprT = (Type) args[5].valor();
                         Type paramT = paramFormal.getType();
 
                         if (!paramT.compatible(exprT)) {
                             if (paramT != Type.ERROR && exprT != Type.ERROR) {
                                 errors.add(new AssignationTypeError(paramT, exprT, identParamReal));
                             }
                         }
 
                         // Comprobamos que la expresion sea un designador
                         boolean esDesig = (boolean) args[6].valor();
                         if (paramFormal.isReference() && !esDesig) {
                             errors.add(new NotADesignatorError(
                                 identParamReal.getLexeme(), identParamReal.getLine(), identParamReal.getColumn()));
                         }
                     }
                 }
 
                 return ConcatErrorsFun.INSTANCE.eval(args[0], a(errors));
             }
         });
 
         dependencias(attr.a("cod"), attr.a("tsh"), identLex, attr.a("err"), expr.a("cod"), attr.a("nombresubprogh"));
         calculo(attr.a("cod"), new SemFun() {
 
             @SuppressWarnings("unchecked")
             @Override
             public Object eval (Atributo... args) {
                 SymbolTable table = (SymbolTable) args[0].valor();
                 Lexeme ident = (Lexeme) args[1].valor();
                 List<CompileError> errs = (List<CompileError>) args[2].valor();
                 Lexeme subName = (Lexeme) args[4].valor();
 
                 if (!errs.isEmpty() || !table.hasIdentifier(ident.getLexeme())) {
                     return ConcatCodeFun.INSTANCE.eval();
                 }
 
                 List<Instruction> code;
 
                 List<Parameter> parameters = table.getIdentifierParams(subName.getLexeme());
                 Parameter param = null;
                 for (Parameter parameter : parameters) {
                     if (ident.getLexeme().equals(parameter.getName())) {
                         param = parameter;
                     }
                 }
 
                 Type type = param.getType();
 
                 if (param.isReference()) {
                     code =
                         Arrays.asList(new LoadInstruction(0, Type.NATURAL), new PushInstruction(NaturalValue
                             .valueOf(param.getOffset())), new BinaryOperatorInstruction(BinaryOperator.ADDITION),
                         // --
                         new IndirectStoreInstruction(Type.NATURAL));
 
                 } else {
                     if (!type.isPrimitive()) {
                         code =
                             Arrays.asList(new LoadInstruction(0, Type.NATURAL), new PushInstruction(NaturalValue
                                 .valueOf(param.getOffset())), new BinaryOperatorInstruction(BinaryOperator.ADDITION),
                             // --
                             new MoveInstruction(type.getSize()));
 
                     } else {
                         code =
                             Arrays.asList(new LoadInstruction(0, Type.NATURAL), new PushInstruction(NaturalValue
                                 .valueOf(param.getOffset())), new BinaryOperatorInstruction(BinaryOperator.ADDITION),
                             // --
                             new IndirectStoreInstruction(Type.NATURAL));
                     }
                 }
 
                 return ConcatCodeFun.INSTANCE.eval(args[3], a(code));
             }
         });
 
         asigna(expr.a("etqh"), attr.a("etqh"));
 
         dependencias(attr.a("etq"), attr.a("tsh"), identLex, attr.a("err"), expr.a("etq"), attr.a("nombresubprogh"));
         calculo(attr.a("etq"), new SemFun() {
 
             @SuppressWarnings("unchecked")
             @Override
             public Object eval (Atributo... args) {
                 SymbolTable table = (SymbolTable) args[0].valor();
                 Lexeme ident = (Lexeme) args[1].valor();
                 List<CompileError> errs = (List<CompileError>) args[2].valor();
                 Integer etq = (Integer) args[3].valor();
                 Lexeme subName = (Lexeme) args[4].valor();
 
                 if (!errs.isEmpty() || !table.hasIdentifier(ident.getLexeme())) {
                     return 0;
                 }
 
                 List<Parameter> parameters = table.getIdentifierParams(subName.getLexeme());
                 Parameter param = null;
                 for (Parameter parameter : parameters) {
                     if (ident.getLexeme().equals(parameter.getName())) {
                         param = parameter;
                     }
                 }
 
                 Type type = param.getType();
 
                 if (param.isReference()) {
                     return etq + 4;
                 } else {
                     if (!type.isPrimitive()) {
                         return etq + 4;
                     } else {
                         return etq + 4;
                     }
                 }
 
             }
         });
 
         dependencias(attr.a("nparams"), attr.a("nparamsh"));
         calculo(attr.a("nparams"), new IncrementFun(1));
 
         dependencias(attr.a("listaparamnombres"), attr.a("listaparamnombresh"), identLex);
         calculo(attr.a("listaparamnombres"), new SemFun() {
 
             @SuppressWarnings("unchecked")
             @Override
             public Object eval (Atributo... args) {
                 List<Lexeme> nombres = (List<Lexeme>) args[0].valor();
                 Lexeme ident = (Lexeme) args[1].valor();
 
                 nombres.add(ident);
                 return nombres;
             }
         });
 
         return attr;
     }
 
     // SSubprogs
 
     public TAtributos sSubprogs_R1 (TAtributos subprogs) {
         regla("SSubprogs -> SUBPROGRAMS ILLAVE Subprogs FLLAVE");
         TAtributos attr = atributosPara("SSubprogs", "etqh", "etq", "tsh", "ts", "err", "cod");
 
         asigna(subprogs.a("tsh"), attr.a("tsh"));
 
         dependencias(attr.a("err"), subprogs.a("err"));
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         asigna(attr.a("cod"), subprogs.a("cod"));
 
         asigna(subprogs.a("etqh"), attr.a("etqh"));
 
         asigna(attr.a("etq"), subprogs.a("etq"));
 
         asigna(attr.a("ts"), subprogs.a("ts"));
 
         return attr;
     }
 
     public TAtributos sSubprogs_R2 () {
         regla("SSubprogs -> SUBPROGRAMS ILLAVE FLLAVE");
         TAtributos attr = atributosPara("SSubprogs", "etqh", "etq", "tsh", "ts", "cod");
 
         // SSublogos.cod = []
         calculo(attr.a("cod"), ConcatCodeFun.INSTANCE);
 
         asigna(attr.a("etq"), attr.a("etqh"));
 
         asigna(attr.a("ts"), attr.a("tsh"));
 
         return attr;
     }
 
     public TAtributos sSubprogs_R3 () {
         regla("SSubprogs -> $");
         TAtributos attr = atributosPara("SSubprogs", "etqh", "etq", "tsh", "ts", "err", "cod");
 
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         asigna(attr.a("ts"), attr.a("tsh"));
 
         // SSublogos.cod = []
         calculo(attr.a("cod"), ConcatCodeFun.INSTANCE);
 
         asigna(attr.a("etq"), attr.a("etqh"));
 
         return attr;
     }
 
     // Subprogs
 
     public TAtributos subprogs_R1 (TAtributos subprogs_1, TAtributos subprog) {
         regla("Subprogs -> Subprogs Subprog");
         TAtributos attr = atributosPara("Subprogs", "tsh", "ts", "err", "cod", "etq", "etqh");
 
         asigna(subprogs_1.a("tsh"), attr.a("tsh"));
 
         asigna(subprog.a("tsh"), subprogs_1.a("ts"));
 
         asigna(attr.a("ts"), subprog.a("ts"));
 
         dependencias(attr.a("err"), subprogs_1.a("err"), subprog.a("err"));
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         dependencias(attr.a("cod"), subprogs_1.a("cod"), subprog.a("cod"));
         calculo(attr.a("cod"), ConcatCodeFun.INSTANCE);
 
         asigna(subprogs_1.a("etqh"), attr.a("etqh"));
 
         asigna(subprog.a("etqh"), subprogs_1.a("etq"));
 
         asigna(attr.a("etq"), subprog.a("etq"));
 
         return attr;
     }
 
     public TAtributos subprogs_R2 (TAtributos subprog) {
         regla("Subprogs -> Subprog");
         TAtributos attr = atributosPara("Subprogs", "tsh", "ts", "err", "cod", "etq", "etqh");
 
         asigna(subprog.a("tsh"), attr.a("tsh"));
 
         asigna(attr.a("ts"), subprog.a("ts"));
 
         dependencias(attr.a("err"), subprog.a("err"));
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         asigna(attr.a("cod"), subprog.a("cod"));
 
         asigna(subprog.a("etqh"), attr.a("etqh"));
 
         asigna(attr.a("etq"), subprog.a("etq"));
 
         return attr;
     }
 
     // Subprog
 
     public TAtributos subprog_R1 (Lexeme ident, TAtributos sfParams, TAtributos sVars, TAtributos sInsts) {
         regla("Subprog -> SUBPROGRAM IDENT IPAR SFParams FPAR ILLAVE SVars SInsts FLLAVE");
         TAtributos attr = atributosPara("Subprog", "dir", "dirh", "tsh", "ts", "cod", "etq", "etqh", "err");
         Atributo identLex = atributoLexicoPara("IDENT", "lex", ident);
 
         asigna(sfParams.a("dirh"), a(0));
 
         dependencias(sfParams.a("tsh"), attr.a("tsh"));
         calculo(sfParams.a("tsh"), new SemFun() {
 
             @Override
             public Object eval (Atributo... args) {
                 SymbolTable table = (SymbolTable) args[0].valor();
                 return new SymbolTable(table);
             }
         });
 
         dependencias(sVars.a("tsh"), sfParams.a("ts"), identLex, sfParams.a("params"), attr.a("etqh"));
         calculo(sVars.a("tsh"), new SemFun() {
             @SuppressWarnings("unchecked")
             @Override
             public Object eval (Atributo... args) {
                 SymbolTable table = (SymbolTable) args[0].valor();
                 Lexeme ident = (Lexeme) args[1].valor();
                 List<Parameter> params = (List<Parameter>) args[2].valor();
                 Integer address = (Integer) args[3].valor();
 
                 table.putSubprogram(ident.getLexeme(), params, address);
 
                 return table;
             }
         });
 
         asigna(sVars.a("nivel"), a(Scope.LOCAL));
 
         asigna(sVars.a("dirh"), sfParams.a("dir"));
 
         asigna(sInsts.a("tsh"), sVars.a("ts"));
 
         dependencias(attr.a("err"), sfParams.a("err"), sInsts.a("err"), sVars.a("err"), identLex, sfParams.a("ts"));
         calculo(attr.a("err"), new SemFun() {
             @SuppressWarnings("unchecked")
             @Override
             public Object eval (Atributo... args) {
                 List<CompileError> sfparamsErr = (List<CompileError>) args[0].valor();
                 List<CompileError> sInstsErr = (List<CompileError>) args[1].valor();
                 List<CompileError> sVarsErr = (List<CompileError>) args[2].valor();
 
                 SymbolTable ts = (SymbolTable) args[4].valor();
                 Lexeme ident = (Lexeme) args[3].valor();
                 // Comprobamos que el identificador del subprograma no exista previamente en la tabla de símbolos
                 CompileError err1 =
                     (!ts.hasIdentifier(ident.getLexeme())) ? new DuplicateIdentifierError(ident.getLexeme(), ident
                         .getLine(), ident.getColumn()) : null;
 
                 CompileError err2 = null;
                 if (ts.hasIdentifier(ident.getLexeme())) {
                     // Comprobamos que no haya parametros formales declarados repetidos
                     List<Parameter> parametros = ts.getIdentifierParams(ident.getLexeme());
 
                     Iterator<Parameter> it1 = parametros.iterator();
                     while (it1.hasNext()) {
                         Parameter element1 = it1.next();
                         Iterator<Parameter> it2 = parametros.iterator();
                         while (it2.hasNext()) {
                             Parameter element2 = it2.next();
                             if (element1 != element2) {
                                 if (element1.getName().equals(element2.getName())) {
                                     err2 =
                                         new DuplicateIdentifierError(element2.getName(), ident.getLine(), ident
                                             .getColumn());
                                 }
                             }
                         }
                     }
                 }
 
                 return ConcatErrorsFun.INSTANCE.eval(a(sfparamsErr), a(sInstsErr), a(sVarsErr), a(err1), a(err2));
 
             }
         });
 
         dependencias(attr.a("cod"), sInsts.a("cod"));
         calculo(attr.a("cod"), new SemFun() {
 
             @Override
             public Object eval (Atributo... args) {
                 // . JUMP-IND(M[FP-2])
                 // Es decir:
                 // . LOAD(1), PUSH(2), SUB, LOAD-IND, RETURN
                 List<Instruction> code =
                     Arrays.asList(
                         new LoadInstruction(1, Type.NATURAL), new PushInstruction(NaturalValue.valueOf(2)),
                         new BinaryOperatorInstruction(BinaryOperator.SUBTRACTION), new IndirectLoadInstruction(
                             Type.NATURAL), new ReturnInstruction());
                 return ConcatCodeFun.INSTANCE.eval(args[0], a(code));
             }
         });
 
         asigna(sInsts.a("etqh"), attr.a("etqh"));
 
         dependencias(attr.a("etq"), sInsts.a("etq"));
         calculo(attr.a("etq"), new IncrementFun(5));
 
         dependencias(attr.a("ts"), attr.a("tsh"), identLex, sfParams.a("params"), attr.a("etqh"));
         calculo(attr.a("ts"), new SemFun() {
             @SuppressWarnings("unchecked")
             @Override
             public Object eval (Atributo... args) {
                 SymbolTable table = (SymbolTable) args[0].valor();
                 Lexeme ident = (Lexeme) args[1].valor();
                 List<Parameter> params = (List<Parameter>) args[2].valor();
                 Integer address = (Integer) args[3].valor();
 
                 table.putSubprogram(ident.getLexeme(), params, address);
 
                 return table;
             }
         });
 
         return attr;
     }
 
     // SFParams
 
     public TAtributos sfParams_R1 (TAtributos fParams) {
         regla("SFParams -> FParams");
         TAtributos attr = atributosPara("SFParams", "tsh", "ts", "dir", "dirh", "err", "params");
 
         // FParams
         asigna(fParams.a("tsh"), attr.a("tsh"));
 
         asigna(attr.a("ts"), fParams.a("ts"));
 
         asigna(attr.a("dir"), fParams.a("dir"));
 
         asigna(fParams.a("dirh"), attr.a("dirh"));
 
         dependencias(attr.a("err"), fParams.a("err"));
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         asigna(attr.a("params"), fParams.a("params"));
 
         return attr;
     }
 
     public TAtributos sfParams_R2 () {
         regla("SFParams -> $");
         TAtributos attr = atributosPara("SFParams", "ts", "tsh", "dir", "dirh", "err", "params");
 
         // sfParams
         asigna(attr.a("ts"), attr.a("tsh"));
 
         asigna(attr.a("dir"), attr.a("dirh"));
 
         asigna(attr.a("params"), a(Collections.emptyList()));
 
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         return attr;
     }
 
     // FParams
     public TAtributos fParams_R1 (TAtributos fParams_1, TAtributos fParam) {
         regla("FParams -> FParams COMA FParam");
         TAtributos attr = atributosPara("FParams", "tsh", "ts", "err", "dir", "dirh", "id", "clase", "tipo", "params");
 
         asigna(fParams_1.a("tsh"), attr.a("tsh"));
 
         asigna(fParams_1.a("dirh"), attr.a("dirh"));
 
         asigna(fParam.a("tsh"), fParams_1.a("tsh"));
 
         asigna(fParam.a("dirh"), fParams_1.a("dir"));
 
         dependencias(attr.a("dir"), fParams_1.a("dir"), fParam.a("tipo"), attr.a("ts"));
         calculo(attr.a("dir"), new SemFun() {
             @Override
             public Object eval (Atributo... args) {
                 int varDir = (Integer) args[0].valor();
                 Type type = (Type) args[1].valor();
 
                 return varDir + type.getSize();
             }
         });
 
         dependencias(attr.a("params"), fParams_1.a("params"), fParam.a("param"));
         calculo(attr.a("params"), new SemFun() {
             @SuppressWarnings("unchecked")
             @Override
             public Object eval (Atributo... args) {
                 List<Parameter> params = (List<Parameter>) args[0].valor();
                 Parameter param = (Parameter) args[1].valor();
 
                 List<Parameter> moreParams = new ArrayList<>(params);
                 moreParams.add(param);
 
                 return moreParams;
             }
         });
 
         dependencias(attr.a("ts"), fParam.a("ts"), fParam.a("id"), fParam.a("clase"), fParams_1.a("dir"), fParam
             .a("tipo"), attr.a("err"));
         calculo(attr.a("ts"), new SemFun() {
 
             @Override
             public Object eval (Atributo... args) {
                 SymbolTable table = (SymbolTable) args[0].valor();
                 Lexeme ident = (Lexeme) args[1].valor();
                 ClassDec cd = (ClassDec) args[2].valor();
                 Integer address = (Integer) args[3].valor();
                 Type type = (Type) args[4].valor();
 
                 if (ident != null) {
                     table.putParam(ident.getLexeme(), address, type, cd == ClassDec.PARAM_REF);
                 }
 
                 return table;
             }
         });
 
         dependencias(attr.a("err"), fParam.a("ts"), fParam.a("id"), a(Scope.LOCAL));
         calculo(attr.a("err"), CheckDuplicateIdentifierFun.INSTANCE);
 
         return attr;
     }
 
     public TAtributos fParams_R2 (TAtributos fParam) {
         regla("FParams -> FParam");
         TAtributos attr = atributosPara("FParams", "tsh", "ts", "id", "dir", "dirh", "tipo", "clase", "err", "params");
 
         asigna(fParam.a("tsh"), attr.a("tsh"));
 
         asigna(fParam.a("dirh"), attr.a("dirh"));
 
         dependencias(attr.a("dir"), attr.a("dirh"), fParam.a("tipo"), attr.a("ts"));
         calculo(attr.a("dir"), new SemFun() {
             @Override
             public Object eval (Atributo... args) {
                 int varDir = (Integer) args[0].valor();
                 Type type = (Type) args[1].valor();
 
                 return varDir + type.getSize();
             }
         });
         dependencias(attr.a("params"), fParam.a("param"));
         calculo(attr.a("params"), new SemFun() {
 
             @Override
             public Object eval (Atributo... args) {
                 Parameter param = (Parameter) args[0].valor();
 
                 return Arrays.asList(param);
             }
         });
 
         dependencias(
             attr.a("ts"), fParam.a("ts"), fParam.a("id"), fParam.a("clase"), fParam.a("dir"), fParam.a("tipo"), attr
                 .a("err"));
         calculo(attr.a("ts"), new SemFun() {
 
             @Override
             public Object eval (Atributo... args) {
                 SymbolTable ts = (SymbolTable) args[0].valor();
                 Lexeme ident = (Lexeme) args[1].valor();
                 ClassDec cd = (ClassDec) args[2].valor();
                 int address = (int) args[3].valor();
                 Type type = (Type) args[4].valor();
 
                 ts.putParam(ident.getLexeme(), address, type, cd == ClassDec.PARAM_REF);
 
                 return ts;
             }
         });
 
         dependencias(attr.a("err"), fParam.a("ts"), fParam.a("id"), a(Scope.LOCAL));
         calculo(attr.a("err"), CheckDuplicateIdentifierFun.INSTANCE);
 
         return attr;
     }
 
     // FParam
 
     public TAtributos fParam_R1 (TAtributos typeDesc, Lexeme ident) {
         regla("FParam -> TypeDesc IDENT");
         TAtributos attr = atributosPara("FParam", "ts", "tsh", "id", "clase", "tipo", "dir", "dirh", "param");
         Atributo identLex = atributoLexicoPara("IDENT", "lex", ident);
 
         asigna(attr.a("dir"), attr.a("dirh"));
 
         asigna(attr.a("ts"), attr.a("tsh"));
 
         asigna(typeDesc.a("tsh"), attr.a("tsh"));
 
         asigna(attr.a("id"), identLex);
 
         asigna(attr.a("clase"), a(ClassDec.PARAM_VALUE));
 
         asigna(attr.a("tipo"), typeDesc.a("tipo"));
 
         dependencias(attr.a("param"), typeDesc.a("tipo"), identLex, attr.a("dirh"));
         calculo(attr.a("param"), new SemFun() {
 
             @Override
             public Object eval (Atributo... args) {
                 Type type = (Type) args[0].valor();
                 Lexeme ident = (Lexeme) args[1].valor();
                 Integer dir = (Integer) args[2].valor();
 
                 return new Parameter(ident.getLexeme(), type, false, dir);
             }
         });
 
         return attr;
     }
 
     public TAtributos fParam_R2 (TAtributos typeDesc, Lexeme ident) {
         regla("FParam -> TypeDesc MUL IDENT");
         TAtributos attr = atributosPara("FParam", "ts", "tsh", "id", "clase", "tipo", "dir", "dirh", "param");
         Atributo identLex = atributoLexicoPara("IDENT", "lex", ident);
 
         asigna(attr.a("dir"), attr.a("dirh"));
 
         asigna(typeDesc.a("tsh"), attr.a("tsh"));
 
         asigna(attr.a("ts"), attr.a("tsh"));
 
         asigna(attr.a("id"), identLex);
 
         asigna(attr.a("clase"), a(ClassDec.PARAM_REF));
 
         asigna(attr.a("tipo"), typeDesc.a("tipo"));
 
         dependencias(attr.a("param"), typeDesc.a("tipo"), identLex, attr.a("dirh"));
         calculo(attr.a("param"), new SemFun() {
 
             @Override
             public Object eval (Atributo... args) {
                 Type type = (Type) args[0].valor();
                 Lexeme ident = (Lexeme) args[1].valor();
                 Integer dir = (Integer) args[2].valor();
 
                 return new Parameter(ident.getLexeme(), type, true, dir);
             }
         });
         return attr;
     }
 
     // Desig
 
     public TAtributos desig_R1 (Lexeme ident) {
         regla("Desig -> IDENT");
         TAtributos attr = atributosPara("Desig", "tipo", "err", "tsh", "etqh", "etq", "cod", "const", "id", "valor");
         Atributo identLex = atributoLexicoPara("IDENT", "lex", ident);
 
         asigna(attr.a("id"), identLex);
 
         dependencias(attr.a("const"), attr.a("tsh"), identLex);
         calculo(attr.a("const"), new SemFun() {
 
             @Override
             public Object eval (Atributo... args) {
                 SymbolTable table = (SymbolTable) args[0].valor();
                 Lexeme ident = (Lexeme) args[1].valor();
 
                 return table.hasIdentifier(ident.getLexeme())
                        && table.getIdentfierClassDec(ident.getLexeme()) == ClassDec.CONSTANT;
             }
         });
 
         dependencias(attr.a("valor"), attr.a("tsh"), identLex);
         calculo(attr.a("valor"), new SemFun() {
 
             @Override
             public Object eval (Atributo... args) {
                 SymbolTable table = (SymbolTable) args[0].valor();
                 Lexeme ident = (Lexeme) args[1].valor();
 
                 if (table.hasIdentifier(ident.getLexeme())
                     && table.getIdentfierClassDec(ident.getLexeme()) == ClassDec.CONSTANT)
                 {
                     return table.getIdentifierValue(ident.getLexeme());
                 }
 
                 return null;
             }
         });
 
         dependencias(attr.a("tipo"), attr.a("tsh"), identLex);
         calculo(attr.a("tipo"), new SemFun() {
             @Override
             public Object eval (Atributo... args) {
                 SymbolTable table = (SymbolTable) args[0].valor();
                 Lexeme ident = (Lexeme) args[1].valor();
 
                 if (table.hasIdentifier(ident.getLexeme())) {
                     return table.getIdentfierType(ident.getLexeme());
                 } else {
                     return Type.ERROR;
                 }
             }
         });
 
         dependencias(attr.a("err"), attr.a("tsh"), identLex);
         calculo(attr.a("err"), new SemFun() {
             @Override
             public Object eval (Atributo... args) {
                 SymbolTable table = (SymbolTable) args[0].valor();
                 Lexeme ident = (Lexeme) args[1].valor();
 
                 List<CompileError> errs = new ArrayList<>();
                 if (!table.hasIdentifier(ident.getLexeme())) {
                     errs.add(new UndefinedIdentifierError(ident.getLexeme(), ident.getLine(), ident.getColumn()));
 
                 } else {
                     ClassDec cdec = table.getIdentfierClassDec(ident.getLexeme());
                     if (cdec == ClassDec.TYPE || cdec == ClassDec.SUBPROGRAM) {
                         errs.add(new BadIdentifierClassError(ident.getLexeme(), cdec, ClassDec.VARIABLE, 0, 0));
                     }
                 }
 
                 return errs;
             }
         });
 
         dependencias(attr.a("etq"), attr.a("tsh"), identLex, attr.a("etqh"));
         calculo(attr.a("etq"), new SemFun() {
             @Override
             public Object eval (Atributo... args) {
                 SymbolTable table = (SymbolTable) args[0].valor();
                 Lexeme lexeme = (Lexeme) args[1].valor();
                 Integer addr = (Integer) args[2].valor();
 
                 if (table.hasIdentifier(lexeme.getLexeme())) {
                     Scope scope = table.getIdentfierScope(lexeme.getLexeme());
                     ClassDec cdec = table.getIdentfierClassDec(lexeme.getLexeme());
 
                     if (scope == Scope.GLOBAL) {
                         if (cdec == ClassDec.VARIABLE) {
                             return addr + 1;
                         } else if (cdec == ClassDec.CONSTANT) {
                             return addr;
                         }
                     } else /* scope == Scope.LOCAL */{
                         if (cdec == ClassDec.VARIABLE || cdec == ClassDec.PARAM_VALUE) {
                             return addr + 3;
 
                         } else if (cdec == ClassDec.PARAM_REF) {
                             return addr + 4;
 
                         }
                     }
                 }
 
                 return addr;
             }
         });
 
         dependencias(attr.a("cod"), attr.a("tsh"), identLex);
         calculo(attr.a("cod"), new SemFun() {
             @Override
             public Object eval (Atributo... args) {
                 SymbolTable table = (SymbolTable) args[0].valor();
                 Lexeme lexeme = (Lexeme) args[1].valor();
 
                 if (table.hasIdentifier(lexeme.getLexeme())) {
                     Scope scope = table.getIdentfierScope(lexeme.getLexeme());
                     ClassDec cdec = table.getIdentfierClassDec(lexeme.getLexeme());
 
                     NaturalValue dirVal = NaturalValue.valueOf(table.getIdentifierAddress(lexeme.getLexeme()));
                     if (scope == Scope.GLOBAL) {
                         if (cdec == ClassDec.VARIABLE) {
                             return new PushInstruction(dirVal);
                         }
                     } else /* scope == Scope.LOCAL */{
                         if (cdec == ClassDec.VARIABLE || cdec == ClassDec.PARAM_VALUE) {
                             return Arrays.asList(
                                 new LoadInstruction(1, Type.NATURAL), new PushInstruction(dirVal),
                                 new BinaryOperatorInstruction(BinaryOperator.ADDITION));
 
                         } else if (cdec == ClassDec.PARAM_REF) {
                             return Arrays.asList(
                                 new LoadInstruction(1, Type.NATURAL), new PushInstruction(dirVal),
                                 new BinaryOperatorInstruction(BinaryOperator.ADDITION), new IndirectLoadInstruction(
                                     Type.NATURAL));
                         }
                     }
                 }
 
                 return ConcatCodeFun.INSTANCE.eval();
             }
         });
 
         return attr;
     }
 
     public TAtributos desig_R2 (TAtributos desig_1, TAtributos expr) {
         regla("Desig -> Desig ICORCHETE Expr FCORCHETE");
         TAtributos attr = atributosPara("Desig", "tsh", "tipo", "err", "cod", "etqh", "etq", "const", "id", "valor");
 
         asigna(attr.a("id"), desig_1.a("id"));
 
         asigna(attr.a("const"), a(false));
         asigna(expr.a("refh"), a(false));
         asigna(attr.a("valor"), desig_1.a("valor"));
 
         dependencias(attr.a("cod"), desig_1.a("cod"), expr.a("cod"), desig_1.a("tipo"));
         calculo(attr.a("cod"), new SemFun() {
             @Override
             public Object eval (Atributo... attrs) {
                 Type type = (Type) attrs[2].valor();
                 if (type instanceof ArrayType) {
                     ArrayType atype = (ArrayType) type;
 
                     Value tam = new IntegerValue(atype.getBaseType().getSize());
 
                     List<Instruction> code =
                         Arrays.asList(
                             new RangeCheckInstruction(atype.getLength()), new PushInstruction(tam),
                             new BinaryOperatorInstruction(BinaryOperator.PRODUCT), new BinaryOperatorInstruction(
                                 BinaryOperator.ADDITION));
                     return ConcatCodeFun.INSTANCE.eval(attrs[0], attrs[1], a(code));
                 }
 
                 return ConcatCodeFun.INSTANCE.eval();
             }
         });
 
         dependencias(attr.a("tipo"), desig_1.a("tipo"));
         calculo(attr.a("tipo"), new SemFun() {
             @Override
             public Object eval (Atributo... args) {
                 Type type = (Type) args[0].valor();
 
                 if (type instanceof ArrayType) {
                     ArrayType atype = (ArrayType) type;
                     return atype.getBaseType();
 
                 } else {
                     return Type.ERROR;
                 }
             }
         });
 
         dependencias(attr.a("err"), desig_1.a("err"), expr.a("err"), desig_1.a("tipo"), attr.a("id"));
         calculo(attr.a("err"), new SemFun() {
             @Override
             public Object eval (Atributo... args) {
                 Type type = (Type) args[2].valor();
                 Lexeme ident = (Lexeme) args[3].valor();
 
                 CompileError err =
                     (type instanceof ArrayType) ? null : new InvalidTypeError(type, ident.getLine(), ident.getColumn());
                 return ConcatErrorsFun.INSTANCE.eval(args[0], args[1], a(err));
             }
         });
 
         asigna(desig_1.a("etqh"), attr.a("etqh"));
 
         asigna(expr.a("etqh"), desig_1.a("etq"));
 
         asigna(expr.a("tsh"), attr.a("tsh"));
 
         asigna(desig_1.a("tsh"), attr.a("tsh"));
 
         dependencias(attr.a("etq"), expr.a("etq"));
         calculo(attr.a("etq"), new IncrementFun(4));
 
         dependencias(attr.a("const"), a(false));
         calculo(attr.a("etqh"), AssignationFun.INSTANCE);
 
         return attr;
     }
 
     public TAtributos desig_R3 (TAtributos desig_1, Lexeme litnat) {
         regla("Desig -> Desig BARRABAJA LITNAT");
         TAtributos attr = atributosPara("Desig", "tsh", "tipo", "err", "cod", "etqh", "etq", "const", "id", "valor");
         Atributo litnatLex = atributoLexicoPara("LITNAT", "lex", litnat);
 
         asigna(attr.a("const"), a(false));
         asigna(attr.a("valor"), desig_1.a("valor"));
 
         asigna(attr.a("id"), desig_1.a("id"));
 
         dependencias(attr.a("cod"), desig_1.a("cod"), litnatLex, desig_1.a("tipo"));
         calculo(attr.a("cod"), new SemFun() {
             @Override
             public Object eval (Atributo... attrs) {
                 Type type = (Type) attrs[2].valor();
                 String lexString = ((Lexeme) attrs[1].valor()).getLexeme();
                 int lexInt = Integer.parseInt(lexString);
 
                 if (type instanceof TupleType) {
                     TupleType ttype = (TupleType) type;
                     return ConcatCodeFun.INSTANCE.eval(attrs[0], a(new PushInstruction(NaturalValue.valueOf(ttype
                         .getOffset(lexInt)))), a(new BinaryOperatorInstruction(BinaryOperator.ADDITION)));
                 } else {
                     return ConcatCodeFun.INSTANCE.eval();
                 }
             }
         });
 
         dependencias(attr.a("tipo"), desig_1.a("tipo"), litnatLex);
         calculo(attr.a("tipo"), new SemFun() {
             @Override
             public Object eval (Atributo... args) {
                 Type type = (Type) args[0].valor();
 
                 if (type instanceof TupleType) {
                     TupleType ttype = (TupleType) type;
                     Lexeme litNat = (Lexeme) args[1].valor();
 
                     int nat = Integer.parseInt(litNat.getLexeme());
                     List<Type> subtypes = ttype.getSubtypes();
 
                     if (nat > subtypes.size()) {
                         return Type.ERROR;
                     } else {
                         return subtypes.get(nat);
                     }
 
                 } else {
                     return Type.ERROR;
                 }
             }
         });
 
         dependencias(attr.a("err"), desig_1.a("err"), desig_1.a("tipo"), attr.a("id"));
         calculo(attr.a("err"), new SemFun() {
             @Override
             public Object eval (Atributo... args) {
                 Type type = (Type) args[1].valor();
                 Lexeme ident = (Lexeme) args[2].valor();
 
                 CompileError err =
                     (type instanceof TupleType) ? null : new InvalidTypeError(type, ident.getLine(), ident.getColumn());
                 return ConcatErrorsFun.INSTANCE.eval(args[0], a(err));
             }
         });
 
         asigna(desig_1.a("etqh"), attr.a("etqh"));
 
         dependencias(attr.a("etq"), desig_1.a("etq"));
         calculo(attr.a("etq"), new IncrementFun(2));
 
         asigna(desig_1.a("tsh"), attr.a("tsh"));
 
         dependencias(attr.a("const"), a(false));
         calculo(attr.a("etqh"), AssignationFun.INSTANCE);
 
         return attr;
     }
 
     // Expr
 
     public TAtributos expr_R1 (TAtributos term_1, TAtributos op0, TAtributos term_2) {
         regla("Expr -> Term Op0 Term");
         TAtributos attr = atributosPara("Expr", "desig", "tipo", "tsh", "err", "cod", "etqh", "etq", "refh");
 
         dependencias(attr.a("cod"), term_1.a("cod"), term_2.a("cod"), op0.a("op"));
         calculo(attr.a("cod"), new SemFun() {
 
             @Override
             public Object eval (Atributo... args) {
                 BinaryOperator op = (BinaryOperator) args[2].valor();
 
                 return ConcatCodeFun.INSTANCE.eval(args[0], args[1], a(new BinaryOperatorInstruction(op)));
             }
         });
 
         asigna(attr.a("desig"), a(false));
 
         dependencias(attr.a("tipo"), term_1.a("tipo"), op0.a("op"), term_2.a("tipo"));
         calculo(attr.a("tipo"), new SemFun() {
             @Override
             public Object eval (Atributo... args) {
                 Type type1 = (Type) args[0].valor();
                 BinaryOperator op = (BinaryOperator) args[1].valor();
                 Type type2 = (Type) args[2].valor();
 
                 return op.getApplyType(type1, type2);
             }
         });
 
         asigna(attr.a("desig"), a(false));
 
         asigna(term_1.a("etqh"), attr.a("etqh"));
 
         asigna(term_2.a("etqh"), term_1.a("etq"));
 
         asigna(term_1.a("tsh"), attr.a("tsh"));
 
         asigna(term_2.a("tsh"), attr.a("tsh"));
 
         asigna(term_1.a("refh"), attr.a("refh"));
 
         asigna(term_2.a("refh"), attr.a("refh"));
 
         dependencias(attr.a("etq"), term_2.a("etq"));
         calculo(attr.a("etq"), new IncrementFun(1));
 
         dependencias(
             attr.a("err"), term_1.a("err"), term_1.a("tipo"), op0.a("op"), term_2.a("err"), term_2.a("tipo"), op0
                 .a("lex"));
         calculo(attr.a("err"), new SemFun() {
 
             @Override
             public Object eval (Atributo... args) {
                 Type type1 = (Type) args[1].valor();
                 BinaryOperator op = (BinaryOperator) args[2].valor();
                 Type type2 = (Type) args[4].valor();
                 Lexeme opLex = (Lexeme) args[5].valor();
 
                 CompileError err =
                     !op.canApply(type1, type2)
                         ? new OperatorError(type1, type2, op, opLex.getLine(), opLex.getColumn()) : null;
 
                 return ConcatErrorsFun.INSTANCE.eval(a(err), args[0], args[3]);
             }
         });
 
         return attr;
     }
 
     public TAtributos expr_R2 (TAtributos term) {
         regla("Expr -> Term");
         TAtributos attr = atributosPara("Expr", "tipo", "tsh", "desig", "err", "cod", "etqh", "etq", "refh");
 
         asigna(attr.a("tipo"), term.a("tipo"));
 
         asigna(attr.a("cod"), term.a("cod"));
 
         asigna(term.a("tsh"), attr.a("tsh"));
 
         asigna(attr.a("desig"), term.a("desig"));
 
         asigna(attr.a("etq"), term.a("etq"));
 
         asigna(term.a("etqh"), attr.a("etqh"));
 
         asigna(term.a("refh"), attr.a("refh"));
 
         dependencias(attr.a("err"), term.a("err"));
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         return attr;
     }
 
     // Term
 
     public TAtributos term_R1 (TAtributos term_1, TAtributos op1, TAtributos fact) {
         regla("Term -> Term Op1 Fact");
         TAtributos attr = atributosPara("Term", "tipo", "tsh", "desig", "op", "etq", "etqh", "cod", "err", "refh");
 
         dependencias(attr.a("cod"), term_1.a("cod"), fact.a("cod"), op1.a("op"));
         calculo(attr.a("cod"), new SemFun() {
 
             @Override
             public Object eval (Atributo... args) {
                 BinaryOperator op = (BinaryOperator) args[2].valor();
 
                 return ConcatCodeFun.INSTANCE.eval(args[0], args[1], a(new BinaryOperatorInstruction(op)));
             }
         });
 
         dependencias(attr.a("tipo"), term_1.a("tipo"), op1.a("op"), fact.a("tipo"));
         calculo(attr.a("tipo"), new SemFun() {
             @Override
             public Object eval (Atributo... args) {
                 Type type1 = (Type) args[0].valor();
                 BinaryOperator op = (BinaryOperator) args[1].valor();
                 Type type2 = (Type) args[2].valor();
 
                 return op.getApplyType(type1, type2);
             }
         });
 
         asigna(term_1.a("tsh"), attr.a("tsh"));
 
         asigna(fact.a("tsh"), attr.a("tsh"));
 
         asigna(attr.a("desig"), a(false));
 
         asigna(term_1.a("etqh"), attr.a("etqh"));
 
         asigna(fact.a("etqh"), term_1.a("etq"));
 
         asigna(term_1.a("refh"), attr.a("refh"));
 
         asigna(fact.a("refh"), attr.a("refh"));
 
         dependencias(attr.a("etq"), fact.a("etq"));
         calculo(attr.a("etq"), new IncrementFun(1));
 
         dependencias(attr.a("err"), term_1.a("err"), term_1.a("tipo"), op1.a("op"), fact.a("err"), fact.a("tipo"), op1
             .a("lex"));
         calculo(attr.a("err"), new SemFun() {
 
             @Override
             public Object eval (Atributo... args) {
                 Type type1 = (Type) args[1].valor();
                 BinaryOperator op = (BinaryOperator) args[2].valor();
                 Type type2 = (Type) args[4].valor();
                 Lexeme opLex = (Lexeme) args[5].valor();
 
                 CompileError err =
                     !op.canApply(type1, type2)
                         ? new OperatorError(type1, type2, op, opLex.getLine(), opLex.getColumn()) : null;
 
                 return ConcatErrorsFun.INSTANCE.eval(args[0], args[3], a(err));
             }
         });
 
         return attr;
     }
 
     public TAtributos term_R2 (TAtributos term_1, TAtributos fact, Lexeme or) {
         regla("Term -> Term OR Fact");
         TAtributos attr = atributosPara("Term", "tipo", "op", "tsh", "desig", "cod", "etq", "etqh", "err", "refh");
         Atributo orLex = atributoLexicoPara("OR", "lex", or);
 
         dependencias(attr.a("cod"), term_1.a("cod"), fact.a("etq"), fact.a("cod"));
         calculo(attr.a("cod"), new SemFun() {
             @Override
             public Object eval (Atributo... attrs) {
                 int factEtq = (int) attrs[1].valor();
                 return ConcatCodeFun.INSTANCE.eval(attrs[0], a(new DuplicateInstruction()), a(new BranchInstruction(
                     factEtq, BooleanValue.TRUE)), a(new DropInstruction()), attrs[2]);
             }
         });
 
         dependencias(attr.a("tipo"), term_1.a("tipo"), fact.a("tipo"));
         calculo(attr.a("tipo"), new SemFun() {
 
             @Override
             public Object eval (Atributo... args) {
                 Type type1 = (Type) args[0].valor();
                 Type type2 = (Type) args[1].valor();
 
                 return BinaryOperator.OR.getApplyType(type1, type2);
             }
         });
 
         dependencias(term_1.a("tsh"), attr.a("tsh"));
         calculo(term_1.a("tipo"), AssignationFun.INSTANCE);
 
         asigna(fact.a("tsh"), attr.a("tsh"));
 
         asigna(attr.a("desig"), a(false));
 
         asigna(term_1.a("etqh"), attr.a("etqh"));
 
         dependencias(fact.a("etqh"), term_1.a("etq"));
         calculo(fact.a("etqh"), new IncrementFun(3));
 
         asigna(attr.a("etq"), fact.a("etq"));
 
         asigna(term_1.a("refh"), attr.a("refh"));
 
         asigna(fact.a("refh"), attr.a("refh"));
 
         dependencias(attr.a("err"), term_1.a("err"), term_1.a("tipo"), fact.a("err"), fact.a("tipo"), orLex);
         calculo(attr.a("err"), new SemFun() {
 
             @Override
             public Object eval (Atributo... args) {
                 Type type1 = (Type) args[1].valor();
                 BinaryOperator op = BinaryOperator.OR;
                 Type type2 = (Type) args[3].valor();
                 Lexeme opLex = (Lexeme) args[4].valor();
 
                 CompileError err =
                     !op.canApply(type1, type2)
                         ? new OperatorError(type1, type2, op, opLex.getLine(), opLex.getColumn()) : null;
 
                 return ConcatErrorsFun.INSTANCE.eval(args[0], args[2], a(err));
             }
         });
 
         return attr;
     }
 
     public TAtributos term_R3 (TAtributos fact) {
         regla("Term -> Fact");
         TAtributos attr = atributosPara("Term", "tipo", "tsh", "desig", "cod", "etqh", "etq", "err", "op", "refh");
 
         asigna(attr.a("tipo"), fact.a("tipo"));
 
         asigna(fact.a("tsh"), attr.a("tsh"));
 
         asigna(attr.a("desig"), fact.a("desig"));
 
         asigna(attr.a("cod"), fact.a("cod"));
 
         asigna(fact.a("etqh"), attr.a("etqh"));
 
         asigna(attr.a("etq"), fact.a("etq"));
 
         asigna(fact.a("refh"), attr.a("refh"));
 
         dependencias(attr.a("err"), fact.a("err"));
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         return attr;
     }
 
     // Fact
 
     public TAtributos fact_R1 (TAtributos fact_1, TAtributos op2, TAtributos shft) {
         regla("Fact -> Fact Op2 Shft");
         TAtributos attr = atributosPara("Fact", "tipo", "tsh", "desig", "err", "cod", "etq", "etqh", "refh");
 
         dependencias(attr.a("cod"), fact_1.a("cod"), shft.a("cod"), op2.a("op"));
         calculo(attr.a("cod"), new SemFun() {
             @Override
             public Object eval (Atributo... args) {
                 BinaryOperator op = (BinaryOperator) args[2].valor();
 
                 return ConcatCodeFun.INSTANCE.eval(args[0], args[1], a(new BinaryOperatorInstruction(op)));
             }
         });
 
         dependencias(attr.a("tipo"), fact_1.a("tipo"), op2.a("op"), shft.a("tipo"));
         calculo(attr.a("tipo"), new SemFun() {
             @Override
             public Object eval (Atributo... args) {
                 Type type1 = (Type) args[0].valor();
                 BinaryOperator op = (BinaryOperator) args[1].valor();
                 Type type2 = (Type) args[2].valor();
 
                 return op.getApplyType(type1, type2);
             }
         });
 
         asigna(fact_1.a("etqh"), attr.a("etqh"));
 
         asigna(shft.a("etqh"), fact_1.a("etq"));
 
         asigna(fact_1.a("refh"), attr.a("refh"));
 
         asigna(shft.a("refh"), attr.a("refh"));
 
         dependencias(attr.a("etq"), shft.a("etq"));
         calculo(attr.a("etq"), new IncrementFun(1));
 
         asigna(fact_1.a("tsh"), attr.a("tsh"));
 
         asigna(shft.a("tsh"), attr.a("tsh"));
 
         asigna(attr.a("desig"), a(false));
 
         calculo(attr.a("desig"), AndFun.INSTANCE);
 
         dependencias(attr.a("err"), fact_1.a("err"), fact_1.a("tipo"), op2.a("op"), shft.a("err"), shft.a("tipo"), op2
             .a("lex"));
         calculo(attr.a("err"), new SemFun() {
 
             @Override
             public Object eval (Atributo... args) {
                 Type type1 = (Type) args[1].valor();
                 BinaryOperator op = (BinaryOperator) args[2].valor();
                 Type type2 = (Type) args[4].valor();
                 Lexeme opLex = (Lexeme) args[5].valor();
 
                 CompileError err =
                     !op.canApply(type1, type2)
                         ? new OperatorError(type1, type2, op, opLex.getLine(), opLex.getColumn()) : null;
 
                 return ConcatErrorsFun.INSTANCE.eval(args[0], args[3], a(err));
             }
         });
 
         return attr;
     }
 
     public TAtributos fact_R2 (TAtributos fact_1, TAtributos shft, Lexeme and) {
         regla("Fact -> Fact AND Shft");
         TAtributos attr = atributosPara("Fact", "tipo", "tsh", "desig", "cod", "etq", "err", "etqh", "refh");
         Atributo andLex = atributoLexicoPara("AND", "lex", and);
 
         dependencias(attr.a("tipo"), fact_1.a("tipo"), shft.a("tipo"));
         calculo(attr.a("tipo"), new SemFun() {
             @Override
             public Object eval (Atributo... args) {
                 Type type1 = (Type) args[0].valor();
                 Type type2 = (Type) args[1].valor();
 
                 return BinaryOperator.AND.getApplyType(type1, type2);
             }
         });
 
         asigna(fact_1.a("tsh"), attr.a("tsh"));
 
         asigna(shft.a("tsh"), attr.a("tsh"));
 
         asigna(attr.a("desig"), a(false));
 
         asigna(fact_1.a("refh"), attr.a("refh"));
 
         asigna(shft.a("refh"), attr.a("refh"));
 
         // Fact0.cod = Fact1.cod || copia || ir-f(Shft.etq ) || desapila || Shft.cod
         dependencias(attr.a("cod"), fact_1.a("cod"), shft.a("etq"), shft.a("cod"));
         calculo(attr.a("cod"), new SemFun() {
             @Override
             public Object eval (Atributo... attrs) {
                 int shftEtq = (int) attrs[1].valor();
                 return ConcatCodeFun.INSTANCE.eval(attrs[0], a(new DuplicateInstruction()), a(new BranchInstruction(
                     shftEtq, BooleanValue.FALSE)), a(new DropInstruction()), attrs[2]);
             }
         });
 
         asigna(fact_1.a("etqh"), attr.a("etqh"));
 
         dependencias(shft.a("etqh"), fact_1.a("etq"));
         calculo(shft.a("etqh"), new IncrementFun(3));
 
         asigna(attr.a("etq"), shft.a("etq"));
 
         dependencias(attr.a("err"), fact_1.a("err"), fact_1.a("tipo"), shft.a("err"), shft.a("tipo"), andLex);
         calculo(attr.a("err"), new SemFun() {
 
             @Override
             public Object eval (Atributo... args) {
                 Type type1 = (Type) args[1].valor();
                 BinaryOperator op = BinaryOperator.AND;
                 Type type2 = (Type) args[3].valor();
                 Lexeme opLex = (Lexeme) args[4].valor();
 
                 CompileError err =
                     !op.canApply(type1, type2)
                         ? new OperatorError(type1, type2, op, opLex.getLine(), opLex.getColumn()) : null;
 
                 return ConcatErrorsFun.INSTANCE.eval(args[0], args[2], a(err));
             }
         });
 
         return attr;
     }
 
     public TAtributos fact_R3 (TAtributos shft) {
         regla("Fact -> Shft");
         TAtributos attr = atributosPara("Fact", "tipo", "tsh", "desig", "cod", "etqh", "etq", "err", "refh");
 
         asigna(attr.a("tipo"), shft.a("tipo"));
 
         asigna(shft.a("tsh"), attr.a("tsh"));
 
         asigna(attr.a("desig"), shft.a("desig"));
 
         asigna(attr.a("cod"), shft.a("cod"));
 
         asigna(shft.a("etqh"), attr.a("etqh"));
 
         asigna(attr.a("etq"), shft.a("etq"));
 
         asigna(shft.a("refh"), attr.a("refh"));
 
         dependencias(attr.a("err"), shft.a("err"));
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         return attr;
     }
 
     // Shft
 
     public TAtributos shft_R1 (TAtributos unary, TAtributos op3, TAtributos shft_1) {
         regla("Shft -> Unary Op3 Shft");
         TAtributos attr = atributosPara("Shft", "tsh", "desig", "tipo", "cod", "etqh", "etq", "err", "refh");
 
         dependencias(attr.a("tipo"), unary.a("tipo"), op3.a("op"), shft_1.a("tipo"));
         calculo(attr.a("tipo"), new SemFun() {
 
             @Override
             public Object eval (Atributo... args) {
                 Type t1 = (Type) args[0].valor();
                 Type t2 = (Type) args[2].valor();
                 BinaryOperator op = (BinaryOperator) args[1].valor();
 
                 return op.getApplyType(t1, t2);
             }
         });
 
         dependencias(unary.a("tsh"), attr.a("tsh"));
         calculo(attr.a("tsh"), AssignationFun.INSTANCE);
 
         asigna(attr.a("tsh"), shft_1.a("tsh"));
 
         asigna(attr.a("desig"), a(false));
 
         asigna(unary.a("refh"), attr.a("refh"));
 
         asigna(shft_1.a("refh"), attr.a("refh"));
 
         dependencias(attr.a("cod"), unary.a("cod"), shft_1.a("cod"), op3.a("op"));
         calculo(attr.a("cod"), new SemFun() {
 
             @Override
             public Object eval (Atributo... args) {
                 BinaryOperator op = (BinaryOperator) args[2].valor();
 
                 return ConcatCodeFun.INSTANCE.eval(args[0], args[1], a(new BinaryOperatorInstruction(op)));
             }
         });
 
         asigna(unary.a("etqh"), attr.a("etqh"));
 
         asigna(shft_1.a("etqh"), unary.a("etq"));
 
         dependencias(attr.a("etq"), shft_1.a("etq"));
         calculo(attr.a("etq"), new IncrementFun(1));
 
         dependencias(
             attr.a("err"), unary.a("err"), unary.a("tipo"), op3.a("op"), shft_1.a("err"), shft_1.a("tipo"), op3
                 .a("lex"));
         calculo(attr.a("err"), new SemFun() {
 
             @Override
             public Object eval (Atributo... args) {
                 Type type1 = (Type) args[1].valor();
                 BinaryOperator op = (BinaryOperator) args[2].valor();
                 Type type2 = (Type) args[4].valor();
                 Lexeme opLex = (Lexeme) args[5].valor();
 
                 CompileError err =
                     !op.canApply(type1, type2)
                         ? new OperatorError(type1, type2, op, opLex.getLine(), opLex.getColumn()) : null;
 
                 return ConcatErrorsFun.INSTANCE.eval(args[0], args[3], a(err));
             }
         });
 
         return attr;
     }
 
     public TAtributos shft_R2 (TAtributos unary) {
         regla("Shft -> Unary");
         TAtributos attr = atributosPara("Shft", "tsh", "tipo", "desig", "cod", "etqh", "etq", "err", "refh");
 
         asigna(unary.a("tsh"), attr.a("tsh"));
 
         asigna(attr.a("tipo"), unary.a("tipo"));
 
         asigna(attr.a("desig"), unary.a("desig"));
 
         asigna(attr.a("cod"), unary.a("cod"));
 
         asigna(unary.a("etqh"), attr.a("etqh"));
 
         asigna(attr.a("etq"), unary.a("etq"));
 
         asigna(unary.a("refh"), attr.a("refh"));
 
         dependencias(attr.a("err"), unary.a("err"));
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         return attr;
     }
 
     // Unary
 
     public TAtributos unary_R1 (TAtributos op4, TAtributos unary_1) {
         regla("Unary -> Op4 Unary");
         TAtributos attr = atributosPara("Unary", "tsh", "tipo", "err", "desig", "cod", "etqh", "etq", "refh");
 
         asigna(unary_1.a("tsh"), attr.a("tsh"));
 
         asigna(attr.a("desig"), a(false));
 
         asigna(unary_1.a("etqh"), attr.a("etqh"));
 
         dependencias(attr.a("etq"), unary_1.a("etq"));
         calculo(attr.a("etq"), new IncrementFun(1));
 
         asigna(unary_1.a("refh"), attr.a("refh"));
 
         dependencias(attr.a("cod"), unary_1.a("cod"), op4.a("op"));
         calculo(attr.a("cod"), new SemFun() {
             @Override
             public Object eval (Atributo... attrs) {
                 UnaryOperator op = (UnaryOperator) attrs[1].valor();
 
                 return ConcatCodeFun.INSTANCE.eval(attrs[0], a(new UnaryOperatorInstruction(op)));
             }
         });
 
         dependencias(attr.a("tipo"), unary_1.a("tipo"), op4.a("op"));
         calculo(attr.a("tipo"), new SemFun() {
             @Override
             public Object eval (Atributo... attrs) {
                 Type t = (Type) attrs[0].valor();
                 UnaryOperator op = (UnaryOperator) attrs[1].valor();
 
                 return op.getApplyType(t);
             }
         });
 
         dependencias(attr.a("err"), op4.a("op"), unary_1.a("err"), unary_1.a("tipo"), op4.a("lex"));
         calculo(attr.a("err"), new SemFun() {
 
             @Override
             public Object eval (Atributo... args) {
                 UnaryOperator op = (UnaryOperator) args[0].valor();
                 Type type = (Type) args[2].valor();
                 Lexeme opLex = (Lexeme) args[3].valor();
 
                 CompileError err =
                     !op.canApply(type) ? new OperatorError(type, op, opLex.getLine(), opLex.getColumn()) : null;
 
                 return ConcatErrorsFun.INSTANCE.eval(args[1], a(err));
             }
         });
 
         return attr;
     }
 
     public TAtributos unary_R2 (TAtributos cast, TAtributos paren) {
         regla("Unary -> IPAR Cast FPAR Paren");
         TAtributos attr = atributosPara("Unary", "tsh", "tipo", "desig", "cod", "etqh", "etq", "err", "refh");
 
         asigna(paren.a("tsh"), attr.a("tsh"));
 
         asigna(attr.a("desig"), a(false));
 
         asigna(paren.a("etqh"), attr.a("etqh"));
 
         dependencias(attr.a("etq"), paren.a("etq"));
         calculo(attr.a("etq"), new IncrementFun(1));
 
         asigna(paren.a("refh"), attr.a("refh"));
 
         dependencias(attr.a("cod"), paren.a("cod"), cast.a("tipo"));
         calculo(attr.a("cod"), new SemFun() {
             @Override
             public Object eval (Atributo... attrs) {
                 Type type = (Type) attrs[1].valor();
                 return ConcatCodeFun.INSTANCE.eval(attrs[0], a(new CastInstruction(type)));
             }
         });
 
         dependencias(attr.a("tipo"), cast.a("tipo"), paren.a("tipo"));
         calculo(attr.a("tipo"), new SemFun() {
             @Override
             public Object eval (Atributo... attrs) {
                 Type tCast = (Type) attrs[0].valor();
                 Type tParen = (Type) attrs[1].valor();
 
                 return Type.canCast(tCast, tParen) ? tCast : Type.ERROR;
             }
         });
 
         dependencias(attr.a("err"), cast.a("tipo"), paren.a("err"), paren.a("tipo"));
         calculo(attr.a("err"), new SemFun() {
 
             @Override
             public Object eval (Atributo... args) {
                 Type type1 = (Type) args[0].valor();
                 Type type2 = (Type) args[2].valor();
                 Lexeme castLex = (Lexeme) args[3].valor();
 
                 CompileError err =
                     Type.canCast(type1, type2) ? null : new CastingError(type2, type1, castLex.getLine(), castLex
                         .getColumn());
 
                 return ConcatErrorsFun.INSTANCE.eval(args[1], a(err));
             }
         });
 
         return attr;
     }
 
     public TAtributos unary_R3 (TAtributos paren) {
         regla("Unary -> Paren");
         TAtributos attr = atributosPara("Unary", "tsh", "tipo", "desig", "cod", "etqh", "etq", "err", "refh");
 
         asigna(paren.a("tsh"), attr.a("tsh"));
 
         asigna(attr.a("desig"), paren.a("desig"));
 
         asigna(attr.a("tipo"), paren.a("tipo"));
 
         asigna(paren.a("etqh"), attr.a("etqh"));
 
         asigna(attr.a("etq"), paren.a("etq"));
 
         asigna(attr.a("cod"), paren.a("cod"));
 
         asigna(paren.a("refh"), attr.a("refh"));
 
         dependencias(attr.a("err"), paren.a("err"));
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         return attr;
     }
 
     // Paren
 
     public TAtributos paren_R1 (TAtributos expr) {
         regla("Paren -> IPAR Expr FPAR");
         TAtributos attr = atributosPara("Paren", "tsh", "tipo", "desig", "cod", "etqh", "etq", "err", "refh");
 
         asigna(expr.a("tsh"), attr.a("tsh"));
 
         asigna(attr.a("desig"), a(false));
 
         asigna(attr.a("tipo"), expr.a("tipo"));
 
         asigna(expr.a("etqh"), attr.a("etqh"));
 
         asigna(attr.a("etq"), expr.a("etq"));
 
         asigna(attr.a("cod"), expr.a("cod"));
 
         asigna(expr.a("refh"), attr.a("refh"));
 
         dependencias(attr.a("err"), expr.a("err"));
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         return attr;
     }
 
     public TAtributos paren_R2 (TAtributos lit) {
         regla("Paren -> Lit");
         TAtributos attr = atributosPara("Paren", "tsh", "tipo", "desig", "cod", "etqh", "etq", "err", "valor", "refh");
 
         asigna(attr.a("desig"), a(false));
 
         asigna(attr.a("tipo"), lit.a("tipo"));
 
         dependencias(attr.a("etq"), attr.a("etqh"));
         calculo(attr.a("etq"), new IncrementFun(1));
 
         dependencias(attr.a("cod"), lit.a("valor"));
         calculo(attr.a("cod"), new SemFun() {
             @Override
             public Object eval (Atributo... attrs) {
                 Value value = (Value) attrs[0].valor();
                 return ConcatCodeFun.INSTANCE.eval(a(new PushInstruction(value)));
 
             }
         });
 
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         return attr;
     }
 
     public TAtributos paren_R3 (TAtributos desig) {
         regla("Paren -> Desig");
         TAtributos attr = atributosPara("Paren", "tsh", "tipo", "desig", "cod", "etqh", "etq", "err", "refh");
 
         asigna(desig.a("tsh"), attr.a("tsh"));
 
         asigna(attr.a("tipo"), desig.a("tipo"));
 
         asigna(attr.a("desig"), a(true));
 
         asigna(desig.a("etqh"), attr.a("etqh"));
 
         dependencias(attr.a("etq"), desig.a("etq"), desig.a("tipo"), attr.a("refh"));
         calculo(attr.a("etq"), new SemFun() {
 
             @Override
             public Object eval (Atributo... args) {
                 Integer etq = (Integer) args[0].valor();
                 Type type = (Type) args[1].valor();
                 Boolean refh = (Boolean) args[2].valor();
 
                 if (type.isPrimitive() && !refh) {
                     return etq + 1;
                 } else {
                     return etq;
                 }
             }
         });
 
         dependencias(attr.a("cod"), desig.a("cod"), desig.a("tipo"), desig.a("const"), desig.a("valor"), attr.a("refh"));
         calculo(attr.a("cod"), new SemFun() {
             @Override
             public Object eval (Atributo... attrs) {
                 Type type = (Type) attrs[1].valor();
                 Boolean constant = (Boolean) attrs[2].valor();
                 Value value = (Value) attrs[3].valor();
                 Boolean refh = (Boolean) attrs[4].valor();
 
                 if (type.isPrimitive()) {
                     if (constant) {
                         return ConcatCodeFun.INSTANCE.eval(attrs[0], a(new PushInstruction(value)));
                     } else if (!refh) {
                         return ConcatCodeFun.INSTANCE.eval(attrs[0], a(new IndirectLoadInstruction(type)));
                     }
                 }
 
                 return ConcatCodeFun.INSTANCE.eval(attrs[0]);
             }
         });
 
         dependencias(attr.a("err"), desig.a("err"));
         calculo(attr.a("err"), ConcatErrorsFun.INSTANCE);
 
         return attr;
     }
 
     // Op0
 
     public TAtributos op0_R1 (Lexeme lex) {
         regla("Op0 -> IGUAL");
         TAtributos attr = atributosPara("Op0", "op", "lex");
 
         asigna(attr.a("op"), a(BinaryOperator.EQUALS));
         asigna(attr.a("lex"), atributoLexicoPara("IGUAL", "lex", lex));
 
         return attr;
     }
 
     public TAtributos op0_R2 (Lexeme lex) {
         regla("Op0 -> NOIGUAL");
         TAtributos attr = atributosPara("Op0", "op", "lex");
 
         asigna(attr.a("op"), a(BinaryOperator.NOT_EQUALS));
         asigna(attr.a("lex"), atributoLexicoPara("NOIGUAL", "lex", lex));
 
         return attr;
     }
 
     public TAtributos op0_R3 (Lexeme lex) {
         regla("Op0 -> MEN");
         TAtributos attr = atributosPara("Op0", "op", "lex");
 
         asigna(attr.a("op"), a(BinaryOperator.LOWER_THAN));
         asigna(attr.a("lex"), atributoLexicoPara("MEN", "lex", lex));
 
         return attr;
     }
 
     public TAtributos op0_R4 (Lexeme lex) {
         regla("Op0 -> MAY");
         TAtributos attr = atributosPara("Op0", "op", "lex");
 
         asigna(attr.a("op"), a(BinaryOperator.GREATER_THAN));
         asigna(attr.a("lex"), atributoLexicoPara("MAY", "lex", lex));
 
         return attr;
     }
 
     public TAtributos op0_R5 (Lexeme lex) {
         regla("Op0 -> MENOIG");
         TAtributos attr = atributosPara("Op0", "op", "lex");
 
         asigna(attr.a("op"), a(BinaryOperator.LOWER_EQUAL));
         asigna(attr.a("lex"), atributoLexicoPara("MENOIG", "lex", lex));
 
         return attr;
     }
 
     public TAtributos op0_R6 (Lexeme lex) {
         regla("Op0 -> MAYOIG");
         TAtributos attr = atributosPara("Op0", "op", "lex");
 
         asigna(attr.a("op"), a(BinaryOperator.GREATER_EQUALS));
         asigna(attr.a("lex"), atributoLexicoPara("MAYOIG", "lex", lex));
 
         return attr;
     }
 
     // Op1
 
     public TAtributos op1_R1 (Lexeme lex) {
         regla("Op1 -> MENOS");
         TAtributos attr = atributosPara("Op1", "op", "lex");
 
         asigna(attr.a("op"), a(BinaryOperator.SUBTRACTION));
         asigna(attr.a("lex"), atributoLexicoPara("MENOS", "lex", lex));
 
         return attr;
     }
 
     public TAtributos op1_R2 (Lexeme lex) {
         regla("Op1 -> MAS");
         TAtributos attr = atributosPara("Op1", "op", "lex");
 
         asigna(attr.a("op"), a(BinaryOperator.ADDITION));
         asigna(attr.a("lex"), atributoLexicoPara("MAS", "lex", lex));
 
         return attr;
     }
 
     // Op2
 
     public TAtributos op2_R1 (Lexeme lex) {
         regla("Op2 -> MOD");
         TAtributos attr = atributosPara("Op2", "op", "lex");
 
         asigna(attr.a("op"), a(BinaryOperator.MODULO));
         asigna(attr.a("lex"), atributoLexicoPara("MOD", "lex", lex));
 
         return attr;
     }
 
     public TAtributos op2_R2 (Lexeme lex) {
         regla("Op2 -> DIV");
         TAtributos attr = atributosPara("Op2", "op", "lex");
 
         asigna(attr.a("op"), a(BinaryOperator.DIVISION));
         asigna(attr.a("lex"), atributoLexicoPara("DIV", "lex", lex));
 
         return attr;
     }
 
     public TAtributos op2_R3 (Lexeme lex) {
         regla("Op2 -> MUL");
         TAtributos attr = atributosPara("Op2", "op", "lex");
 
         asigna(attr.a("op"), a(BinaryOperator.PRODUCT));
         asigna(attr.a("lex"), atributoLexicoPara("MUL", "lex", lex));
 
         return attr;
     }
 
     // Op3
 
     public TAtributos op3_R1 (Lexeme lex) {
         regla("Op3 -> LSH");
         TAtributos attr = atributosPara("Op3", "op", "lex");
 
         asigna(attr.a("op"), a(BinaryOperator.SHIFT_LEFT));
         asigna(attr.a("lex"), atributoLexicoPara("LSH", "lex", lex));
 
         return attr;
     }
 
     public TAtributos op3_R2 (Lexeme lex) {
         regla("Op3 -> RSH");
         TAtributos attr = atributosPara("Op3", "op", "lex");
 
         asigna(attr.a("op"), a(BinaryOperator.SHIFT_RIGHT));
         asigna(attr.a("lex"), atributoLexicoPara("RSH", "lex", lex));
 
         return attr;
     }
 
     // Op4
 
     public TAtributos op4_R1 (Lexeme lex) {
         regla("Op4 -> NOT");
         TAtributos attr = atributosPara("Op4", "op", "lex");
 
         asigna(attr.a("op"), a(UnaryOperator.NOT));
         asigna(attr.a("lex"), atributoLexicoPara("NOT", "lex", lex));
 
         return attr;
     }
 
     public TAtributos op4_R2 (Lexeme lex) {
         regla("Op4 -> MENOS");
         TAtributos attr = atributosPara("Op4", "op", "lex");
 
         asigna(attr.a("op"), a(UnaryOperator.MINUS));
         asigna(attr.a("lex"), atributoLexicoPara("MENOS", "lex", lex));
 
         return attr;
     }
 
     // Lit
 
     public TAtributos lit_R1 (TAtributos litBool) {
         regla("Lit -> LitBool");
         TAtributos attr = atributosPara("Lit", "tipo", "valor");
 
         asigna(attr.a("tipo"), a(Type.BOOLEAN));
 
         asigna(attr.a("valor"), litBool.a("valor"));
 
         return attr;
     }
 
     public TAtributos lit_R2 (TAtributos litNum) {
         regla("Lit -> LitNum");
         TAtributos attr = atributosPara("Lit", "tipo", "valor");
 
         asigna(attr.a("tipo"), litNum.a("tipo"));
 
         asigna(attr.a("valor"), litNum.a("valor"));
 
         return attr;
     }
 
     public TAtributos lit_R3 (Lexeme litChar) {
         regla("Lit -> LITCHAR");
         TAtributos attr = atributosPara("Lit", "tipo", "valor");
 
         asigna(attr.a("tipo"), a(Type.CHARACTER));
 
         dependencias(attr.a("valor"), a(litChar));
         calculo(attr.a("valor"), new SemFun() {
             @Override
             public Object eval (Atributo... args) {
                 Lexeme lexeme = (Lexeme) args[0].valor();
 
                 return CharacterValue.valueOf(lexeme.getLexeme());
             }
         });
 
         return attr;
     }
 
     // LitBool
 
     public TAtributos litBool_R1 () {
         regla("LitBool -> TRUE");
         TAtributos attr = atributosPara("LitBool", "valor");
 
         asigna(attr.a("valor"), a(BooleanValue.TRUE));
 
         return attr;
     }
 
     public TAtributos litBool_R2 () {
         regla("LitBool -> FALSE");
         TAtributos attr = atributosPara("LitBool", "valor");
 
         asigna(attr.a("valor"), a(BooleanValue.FALSE));
 
         return attr;
     }
 
     // LitNum
 
     public TAtributos litNum_R1 (Lexeme litNat) {
         regla("LitNum -> LITNAT");
         Atributo litNatLex = atributoLexicoPara("LITNAT", "lex", litNat);
         TAtributos attr = atributosPara("LitNum", "valor", "tipo");
 
         asigna(attr.a("tipo"), a(Type.NATURAL));
 
         dependencias(attr.a("valor"), litNatLex);
         calculo(attr.a("valor"), new SemFun() {
             @Override
             public Object eval (Atributo... args) {
                 Lexeme lexeme = (Lexeme) args[0].valor();
 
                 return NaturalValue.valueOf(lexeme.getLexeme());
             }
         });
 
         return attr;
     }
 
     public TAtributos litNum_R2 (Lexeme litFloat) {
         regla("LitNum -> LITFLOAT");
         Atributo litFloatLex = atributoLexicoPara("LITFLOAT", "lex", litFloat);
         TAtributos attr = atributosPara("LitNum", "valor", "tipo");
 
         asigna(attr.a("tipo"), a(Type.FLOAT));
 
         dependencias(attr.a("valor"), litFloatLex);
         calculo(attr.a("valor"), new SemFun() {
             @Override
             public Object eval (Atributo... args) {
                 Lexeme lexeme = (Lexeme) args[0].valor();
 
                 return FloatValue.valueOf(lexeme.getLexeme());
             }
         });
 
         return attr;
     }
 
 }
