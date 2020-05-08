 package edu.uwlax.toze.objectz;
 
 
 /*
  * Abstract Syntax Tree
  *
  * This file contains all of the classes that represent nodes in the AST.
  */
 
 import edu.uwlax.toze.domain.*;
 import edu.uwlax.toze.editor.TozeFontMap;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Ast
 {
     static private List<TozeToken> errors = new ArrayList<TozeToken>();
     static private String m_strAst = "";
 
     static public List<TozeToken> getErrors()
     {
         return new ArrayList<TozeToken>(errors);
     }
 
     static public boolean hasErrors()
     {
         return !errors.isEmpty();
     }
 
     static public void clearErrors()
     {
         errors.clear();
     }
 
     public class AstBase
     {
         TozeToken m_token = null;
         AstSymbolTable m_stable = null;
 
         public void reportTypeError(String msg,
                                     TozeToken token, SpecObject specObject, String property)
         {
             if (specObject != null)
                 {
                 token.setErrorType(ErrorType.TypeError);
                 token.setDescription(specObject.getClass().getSimpleName() + "->" + property + ": " + msg);
                 }
 //            else
 //                {
 //                token.setDescription(msg);
 //                }
             specObject.setErrorForProperty(property, token);
             errors.add(token);
         }
 
         public void printSpaces(int l)
         {
             int i;
             for (i = 0; i < l * 2; i++)
                 {
                 System.out.print(" ");
                 m_strAst += " ";
                 }
         }
 
         public void print(int l, String text)
         {
             printSpaces(l);
             System.out.println(text);
             m_strAst += text + "\n";
         }
 
         public void print(int l)
         {
             print(l, "no name");
             m_strAst += "no name\n";
         }
 
         public void add(AstBase n)
         {
             if (n == null)
                 {
                 return;
                 }
             System.out.println("Lost node " + n.toString());
         }
 
         public AstType getType()
         {
             return new AstType();
         }
 
         public void checkType()
         {
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
         }
 
         public void populateSymbolTable()
         {
         }
     }
 
     public class AstVector extends AstBase
     {
         List m_list = new ArrayList();
     }
 
     public class AstSpec extends AstBase
     {
         private Specification specification;
 
         public AstSpec(Specification specification)
         {
             this.specification = specification;
         }
 
         List m_paras = new ArrayList();
 
         public void add(AstBase n)
         {
             if (n instanceof AstPara)
                 {
                 m_paras.add((AstPara) n);
                 }
             else
                 {
                 super.add(n);
                 }
         }
 
         public void print(int l)
         {
             int i;
 
             m_strAst = "";
             print(l, "Spec");
             for (i = 0; i < m_paras.size(); i++)
                 {
                 ((AstBase) m_paras.get(i)).print(l + 1);
                 }
         }
 
         public void populateTypeTable(AstSymbolTable t)
         {
             if (m_stable == null)
                 {
                 m_stable = new AstSymbolTable();
                 }
             int i;
             for (i = 0; i < m_paras.size(); i++)
                 {
                 ((AstBase) m_paras.get(i)).populateTypeTable(m_stable);
                 }
         }
 
         public void populateSymbolTable(AstSymbolTable t)
         {
             int i;
             for (i = 0; i < m_paras.size(); i++)
                 {
                 ((AstBase) m_paras.get(i)).populateSymbolTable();
                 }
         }
 
         public void checkType()
         {
             int i;
             int iterations = 0;
 
             while (true)
                 {
                 m_stable.reset();
                 Ast.errors.clear();
                 for (i = 0; i < m_paras.size(); i++)
                     {
                     ((AstBase) m_paras.get(i)).checkType();
                     }
                 iterations++;
                 if (!m_stable.m_undefinedSymbolOccurred
                         && !m_stable.m_undefinedTypeOccurred)
                     {
                     break;
                     }
                 if (!m_stable.m_newSymbolSet)
                     {
                     break; // if no new symbols were defined then break
                     }
                 if (iterations > 10)
                     {
                     break;
                     }
                 }
         }
     }
 
     /*
      * Global Paragraphs
      */
     public class AstPara extends AstBase
     {
     }
 
     public class AstBasicTypeDefinition extends AstPara
     {
         private BasicTypeDef basicTypeDef;
 
         public AstBasicTypeDefinition(BasicTypeDef basicTypeDef)
         {
             this.basicTypeDef = basicTypeDef;
         }
 
         List m_identifiers = new ArrayList();
 
         private boolean containsId(AstIdentifier id)
         {
             int i = 0;
             String val;
             for (i = 0; i < m_identifiers.size(); i++)
                 {
                 val = ((AstIdentifier) m_identifiers.get(i)).m_token.m_value;
                 if (id.m_token.m_value.equals(val))
                     {
                     return true;
                     }
                 }
             return false;
         }
 
         public void add(AstBase n)
         {
             if (n instanceof AstIdentifier)
                 {
                 m_identifiers.add(n);
                 }
             else if (n instanceof AstVector)
                 {
                 int i;
                 AstVector v = (AstVector) n;
                 for (i = 0; i < v.m_list.size(); i++)
                     {
                     AstIdentifier id = (AstIdentifier) v.m_list.get(i);
                     m_identifiers.add(id);
                     }
                 }
             else
                 {
                 super.add(n);
                 }
         }
 
         public void populateSymbolTable()
         {
             int i;
             boolean added;
             for (i = 0; i < m_identifiers.size(); i++)
                 {
                 AstIdentifier id = (AstIdentifier) m_identifiers.get(i);
                 String name = id.getName();
                 AstType type = new AstType(AstType.TYPE_SET);
                 type.m_setType = new AstType(AstType.TYPE_BASIC);
                 type.m_setType.m_name = name;
                 added = m_stable.addType(name, type);
                 if (!added)
                     {
                     id.checkType();
                     }
                 }
         }
     }
 
     public class AstAxiomaticDefinition extends AstPara
     {
         AxiomaticDef axiomaticDef;
 
         AstDeclaration m_declaration;
         AstPredicateList m_predicateList;
 
         public AstAxiomaticDefinition(AxiomaticDef axiomaticDef)
         {
             this.axiomaticDef = axiomaticDef;
         }
 
         public void print(int l)
         {
             print(l, "AxiomaticDefinition");
             if (m_declaration != null)
                 {
                 m_declaration.print(l + 1);
                 }
             if (m_predicateList != null)
                 {
                 m_predicateList.print(l + 1);
                 }
         }
 
         public void add(AstBase n)
         {
             if (n instanceof AstDeclaration)
                 {
                 if (m_declaration == null)
                     {
                     m_declaration = (AstDeclaration) n;
                     }
                 else
                     {
                     m_declaration.add(n);
                     }
                 }
             else if (n instanceof AstPredicateList)
                 {
                 m_predicateList = (AstPredicateList) n;
                 }
             else if (n instanceof AstVector)
                 {
                 m_predicateList = new AstPredicateList();
                 AstVector v = (AstVector) n;
                 int i;
                 for (i = 0; i < v.m_list.size(); i++)
                     {
                     AstBase b = (AstBase) v.m_list.get(i);
                     m_predicateList.m_predicates.add(b);
                     }
                 }
             else if (n instanceof AstPredicate)
                 {
                 if (m_predicateList == null)
                     {
                     m_predicateList = new AstPredicateList();
                     }
                 m_predicateList.add(n);
                 }
             else
                 {
                 super.add(n);
                 }
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             if (m_declaration != null)
                 {
                 m_declaration.populateTypeTable(m_stable);
                 }
             if (m_predicateList != null)
                 {
                 m_predicateList.populateTypeTable(m_stable);
                 }
         }
 
         public void populateSymbolTable()
         {
             if (m_declaration != null)
                 {
                 m_declaration.populateSymbolTable();
                 }
             if (m_predicateList != null)
                 {
                 m_predicateList.populateSymbolTable();
                 }
         }
 
         public void checkType()
         {
             if (m_declaration != null)
                 {
                 m_declaration.checkType();
                 }
             if (m_predicateList != null)
                 {
                 m_predicateList.checkType();
                 }
         }
     }
 
     public class AstGenericDefinition extends AstPara
     {
         GenericDef genericDef;
 
         AstFormalParameters m_formalParameters;
         AstDeclaration m_declaration;
         AstPredicateList m_predicateList;
 
         public AstGenericDefinition(GenericDef genericDef)
         {
             this.genericDef = genericDef;
         }
 
         public void add(AstBase n)
         {
             if (n instanceof AstFormalParameters)
                 {
                 m_formalParameters = (AstFormalParameters) n;
                 }
             else if (n instanceof AstDeclaration)
                 {
                 m_declaration = (AstDeclaration) n;
                 }
             else if (n instanceof AstPredicateList)
                 {
                 m_predicateList = (AstPredicateList) n;
                 }
             else if (n instanceof AstVector)
                 {
                 if (m_predicateList == null)
                     {
                     m_predicateList = new AstPredicateList();
                     }
                 int i;
                 List v = ((AstVector) n).m_list;
                 for (i = 0; i < v.size(); i++)
                     {
                     m_predicateList.add((AstBase) v.get(i));
                     }
                 }
             else
                 {
                 super.add(n);
                 }
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = new AstSymbolTable(stable);
             stable.addKid(m_stable);
             if (m_formalParameters != null)
                 {
                 m_formalParameters.populateTypeTable(m_stable);
                 }
             if (m_declaration != null)
                 {
                 m_declaration.populateTypeTable(m_stable);
                 }
             if (m_predicateList != null)
                 {
                 m_predicateList.populateTypeTable(m_stable);
                 }
             m_stable.setNoAddLocal();
         }
 
         public void populateSymbolTable()
         {
             boolean added;
 
             if (m_formalParameters != null)
                 {
                 /*
                  * Add the formal parameters as symbols that are available
                  * to the generic functions.
                  */
                 /*
                  * int i;
                  * for (i=0; i<m_formalParameters.m_identifiers.size(); i++)
                  * {
                  * AstIdentifier id =
                  * (AstIdentifier)m_formalParameters.m_identifiers.get(i);
                  * AstType type = new AstType(AstType.TYPE_SET);
                  * type.m_setType = new AstType(AstType.TYPE_GENERIC);
                  * added = m_stable.add(id.getName(), type);
                  * if (!added) id.checkType();
                  * }
                  */
                 }
 
             if (m_declaration != null)
                 {
                 m_declaration.populateSymbolTable();
                 }
             if (m_predicateList != null)
                 {
                 m_predicateList.populateSymbolTable();
                 }
         }
 
         public void checkType()
         {
             if (m_declaration != null)
                 {
                 m_declaration.checkType();
                 }
             if (m_predicateList != null)
                 {
                 m_predicateList.checkType();
                 }
         }
     }
 
     public class AstAbbreviationDefinition extends AstPara
     {
         AbbreviationDef abbreviationDef;
 
         AstAbbreviation m_abbreviation;
         AstExpression m_expression;
 
         public AstAbbreviationDefinition(AbbreviationDef abbreviationDef)
         {
             this.abbreviationDef = abbreviationDef;
         }
 
         public void add(AstBase n)
         {
             if (n instanceof AstAbbreviation)
                 {
                 m_abbreviation = (AstAbbreviation) n;
                 }
             else if (n instanceof AstExpression)
                 {
                 m_expression = (AstExpression) n;
                 }
             else
                 {
                 super.add(n);
                 }
         }
 
         public void print(int l)
         {
             print(l, "AbbreviationDefinition");
             m_abbreviation.print(l + 1);
             m_expression.print(l + 1);
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             if (m_expression != null)
                 {
                 m_expression.populateTypeTable(m_stable);
                 }
         }
 
         public void populateSymbolTable()
         {
             boolean added;
             added = m_stable.add(m_abbreviation.getName(), new AstType());
             if (!added)
                 {
                 m_abbreviation.checkType();
                 }
             if (m_expression != null)
                 {
                 m_expression.populateSymbolTable();
                 }
         }
 
         public void checkType()
         {
             if (m_expression != null)
                 {
                 AstType type = m_expression.getType();
                 m_stable.setSymbol(m_abbreviation.getName(), type);
                 }
         }
     }
 
     public class AstFreeTypeDefinition extends AstPara
     {
         FreeTypeDef freeTypeDef;
 
         AstIdentifier m_identifier;
         List m_branches = new ArrayList();
         AstType m_type;
 
         public AstFreeTypeDefinition(FreeTypeDef freeTypeDef)
         {
             this.freeTypeDef = freeTypeDef;
         }
 
         public void print(int l)
         {
             print(l, "FreeTypeDefinition");
             if (m_identifier != null)
                 {
                 m_identifier.print(l + 1);
                 }
             int i;
             for (i = 0; i < m_branches.size(); i++)
                 {
                 ((AstBranch) m_branches.get(i)).print(l + 1);
                 }
         }
 
         public void add(AstBase n)
         {
             if (n instanceof AstIdentifier)
                 {
                 m_identifier = (AstIdentifier) n;
                 }
             else if (n instanceof AstVector)
                 {
                 AstVector vnode = (AstVector) n;
                 int i;
                 for (i = 0; i < vnode.m_list.size(); i++)
                     {
                     m_branches.add(vnode.m_list.get(i));
                     }
                 }
             else
                 {
                 super.add(n);
                 }
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             boolean added;
 
             m_stable = stable;
             AstType type = new AstType(AstType.TYPE_SET);
             type.m_setType = new AstType(AstType.TYPE_FREE);
             type.m_setType.m_name = m_identifier.getName();
             added = m_stable.addType(m_identifier.getName(), type);
             if (!added)
                 {
                 m_identifier.checkType();
                 }
             m_type = type.m_setType;
 
             int i;
             for (i = 0; i < m_branches.size(); i++)
                 {
                 AstBranch branch = (AstBranch) m_branches.get(i);
                 branch.populateTypeTable(m_stable);
                 }
         }
 
         public void populateSymbolTable()
         {
             int i;
             for (i = 0; i < m_branches.size(); i++)
                 {
                 AstBranch branch = (AstBranch) m_branches.get(i);
                 branch.populateSymbolTable(m_type);
                 }
         }
     }
 
     public class AstSchema extends AstPara
     {
     }
 
     public class AstSchema1 extends AstSchema
     {
         AstSchemaHeader m_schemaHeader;
         AstDeclaration m_declaration;
         AstPredicateList m_predicateList;
 
         public void print(int l)
         {
             print(l, "Schema");
             if (m_schemaHeader != null)
                 {
                 m_schemaHeader.print(l + 1);
                 }
             if (m_declaration != null)
                 {
                 m_declaration.print(l + 1);
                 }
             if (m_predicateList != null)
                 {
                 m_predicateList.print(l + 1);
                 }
         }
 
         public void add(AstBase n)
         {
             if (n instanceof AstSchemaHeader)
                 {
                 m_schemaHeader = (AstSchemaHeader) n;
                 }
             else if (n instanceof AstDeclaration)
                 {
                 if (m_declaration == null)
                     {
                     m_declaration = (AstDeclaration) n;
                     }
                 else
                     {
                     m_declaration.add(n);
                     }
                 }
             else if (n instanceof AstPredicateList)
                 {
                 m_predicateList = (AstPredicateList) n;
                 }
             else
                 {
                 super.add(n);
                 }
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = new AstSymbolTable(stable);
             stable.addKid(m_stable);
 
             if (m_schemaHeader != null)
                 {
                 m_schemaHeader.populateTypeTable(m_stable);
                 }
             if (m_declaration != null)
                 {
                 m_declaration.populateTypeTable(m_stable);
                 }
             if (m_predicateList != null)
                 {
                 m_predicateList.populateTypeTable(m_stable);
                 }
         }
 
         public void populateSymbolTable()
         {
             if (m_schemaHeader != null)
                 {
                 m_schemaHeader.populateSymbolTable();
                 }
             if (m_declaration != null)
                 {
                 m_declaration.populateSymbolTable();
                 }
             if (m_predicateList != null)
                 {
                 m_predicateList.populateSymbolTable();
                 }
         }
 
         public void checkType()
         {
             if (m_declaration != null)
                 {
                 m_declaration.checkType();
                 }
             if (m_predicateList != null)
                 {
                 m_predicateList.checkType();
                 }
         }
     }
 
     public class AstSchema2 extends AstSchema
     {
         AstSchemaHeader m_schemaHeader;
         AstSchemaExpression m_schemaExpression;
 
         public void add(AstBase n)
         {
             if (n instanceof AstSchemaHeader)
                 {
                 m_schemaHeader = (AstSchemaHeader) n;
                 }
             else if (n instanceof AstSchemaExpression)
                 {
                 m_schemaExpression = (AstSchemaExpression) n;
                 }
             else
                 {
                 super.add(n);
                 }
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             if (m_schemaExpression != null)
                 {
                 m_schemaExpression.populateTypeTable(m_stable);
                 }
         }
 
         public void populateSymbolTable()
         {
             if (m_schemaExpression != null)
                 {
                 m_schemaExpression.populateSymbolTable();
                 }
         }
 
         public void checkType()
         {
             if (m_schemaExpression != null)
                 {
                 m_schemaExpression.checkType();
                 }
         }
     }
 
     public class AstClass extends AstPara
     {
         ClassDef classDef;
 
         AstClassName m_className;
         AstFormalParameters m_formalParameters;
         AstVisibilityList m_visibilityList;
         List m_inheritedClasses = new ArrayList();
         List m_localDefinitions = new ArrayList();
         AstState m_state;
         AstInitialState m_initialState;
         List m_operations = new ArrayList();
 
         public AstClass(ClassDef classDef)
         {
             this.classDef = classDef;
         }
 
         public void print(int l)
         {
             print(l, "Class - " + m_className.getName());
         }
 
         public void add(AstBase n)
         {
             if (n instanceof AstClassHeader)
                 {
                 m_className = ((AstClassHeader) n).m_className;
                 m_formalParameters = ((AstClassHeader) n).m_formalParameters;
                 }
             else if (n instanceof AstVisibilityList)
                 {
                 m_visibilityList = (AstVisibilityList) n;
                 }
             else if (n instanceof AstInheritedClass)
                 {
                 m_inheritedClasses.add(n);
                 }
             else if (n instanceof AstVector)
                 {
                 m_inheritedClasses = ((AstVector) n).m_list;
                 }
             else if (n instanceof AstBasicTypeDefinition)
                 {
                 m_localDefinitions.add(n);
                 }
             else if (n instanceof AstAxiomaticDefinition)
                 {
                 m_localDefinitions.add(n);
                 }
             else if (n instanceof AstAbbreviationDefinition)
                 {
                 m_localDefinitions.add(n);
                 }
             else if (n instanceof AstFreeTypeDefinition)
                 {
                 m_localDefinitions.add(n);
                 }
             else if (n instanceof AstState)
                 {
                 m_state = (AstState) n;
                 }
             else if (n instanceof AstInitialState)
                 {
                 m_initialState = (AstInitialState) n;
                 }
             else if (n instanceof AstOperation)
                 {
                 m_operations.add(n);
                 }
             else
                 {
                 super.add(n);
                 }
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             int i;
             String className = "";
             boolean added = false;
 
             m_stable = new AstSymbolTable(stable);
             if (m_className != null)
                 {
                 className = m_className.getName();
                 }
             m_stable.m_name = className;
             stable.addKid(m_stable);
 
             if (m_formalParameters != null)
                 {
                 m_formalParameters.populateTypeTable(m_stable);
                 }
 
             AstType type = new AstType(AstType.TYPE_SET);
             type.m_setType = new AstType(AstType.TYPE_CLASS);
             type.m_setType.m_name = className;
             type.m_setType.m_classMembers = m_stable;
             if (m_formalParameters != null)
                 {
                 type.m_setType.m_numClassFormalParamList = m_formalParameters.getNumParameters();
                 }
             added = stable.addType(className, type);
             if (!added)
                 {
                 m_className.reportTypeError(className + " is already added as a class",
                                             m_className.m_token,
                                             this.classDef,
                                             "name"
                                             );
                 }
 
             for (i = 0; i < m_inheritedClasses.size(); i++)
                 {
                 AstInheritedClass ic = ((AstInheritedClass) m_inheritedClasses.get(i));
                 ic.populateTypeTable(m_stable);
                 }
 
             for (i = 0; i < m_localDefinitions.size(); i++)
                 {
                 ((AstBase) m_localDefinitions.get(i)).populateTypeTable(m_stable);
                 }
 
             if (m_state != null)
                 {
                 m_state.populateTypeTable(m_stable);
                 }
             if (m_initialState != null)
                 {
                 m_initialState.populateTypeTable(m_stable);
                 }
 
             for (i = 0; i < m_operations.size(); i++)
                 {
                 ((AstBase) m_operations.get(i)).populateTypeTable(m_stable);
                 }
         }
 
         public void populateSymbolTable()
         {
             int i;
 
             if (m_formalParameters != null)
                 {
                 m_formalParameters.populateSymbolTable();
                 }
 
             for (i = 0; i < m_inheritedClasses.size(); i++)
                 {
                 AstInheritedClass ic = ((AstInheritedClass) m_inheritedClasses.get(i));
                 ic.populateSymbolTable();
                 }
 
             for (i = 0; i < m_localDefinitions.size(); i++)
                 {
                 ((AstBase) m_localDefinitions.get(i)).populateSymbolTable();
                 }
 
             if (m_state != null)
                 {
                 m_state.populateSymbolTable();
                 }
             if (m_initialState != null)
                 {
                 m_initialState.populateSymbolTable();
                 }
 
             for (i = 0; i < m_operations.size(); i++)
                 {
                 ((AstBase) m_operations.get(i)).populateSymbolTable();
                 }
         }
 
         public void checkType()
         {
             int i;
             boolean thisClassOK = true;
 
             for (i = 0; i < m_inheritedClasses.size(); i++)
                 {
                 //thisClassOK = true;
 
                 /*
                  * Ensure that the inherited class exists.
                  */
 
                 AstInheritedClass ic = ((AstInheritedClass) m_inheritedClasses.get(i));
                 ic.checkType();
                 /*
                  * AstType t = m_stable.getTypeDef(ic.m_className.getName());
                  * if (t == null)
                  * {
                  * ic.reportTypeError(ic.m_className.getName() + " is not
                  * defined",
                  * ic.m_className.m_token);
                  * thisClassOK = false;
                  * }
                  * else
                  * {
                  * if (!t.isClassDef())
                  * {
                  * reportTypeError(ic.m_className.getName() + " must be a
                  * class",
                  * ic.m_className.m_token);
                  * thisClassOK = false;
                  * }
                  * }
                  */
 
                 /*
                  * Add a reference to the inherited class.
                  */
                 /*
                  * if (thisClassOK)
                  * {
                  * m_stable.add(ic.m_className.getName(),
                  * t.getSetType().m_classMembers);
                  * }
                  */
                 }
 
             for (i = 0; i < m_localDefinitions.size(); i++)
                 {
                 ((AstBase) m_localDefinitions.get(i)).checkType();
                 }
             if (m_state != null)
                 {
                 m_state.checkType();
                 }
             if (m_initialState != null)
                 {
                 m_initialState.checkType();
                 }
 
             for (i = 0; i < m_operations.size(); i++)
                 {
                 ((AstBase) m_operations.get(i)).checkType();
                 }
 
             if (m_visibilityList != null)
                 {
                 m_visibilityList.setVisible(m_stable);
                 }
         }
     }
 
     public class AstPredicatePara extends AstPara
     {
         Specification specification;
 
         AstPredicateList m_predicateList;
 
         public AstPredicatePara(Specification specification)
         {
             this.specification = specification;
         }
 
         public void add(AstBase n)
         {
             if (n instanceof AstPredicate)
                 {
                 if (m_predicateList == null)
                     {
                     m_predicateList = new AstPredicateList();
                     }
                 m_predicateList.add(n);
                 }
             else if (n instanceof AstVector)
                 {
                 if (m_predicateList == null)
                     {
                     m_predicateList = new AstPredicateList();
                     }
                 m_predicateList.add(n);
                 }
             else
                 {
                 super.add(n);
                 }
         }
 
         public void print(int l)
         {
             print(l, "Predicate");
             if (m_predicateList != null)
                 {
                 m_predicateList.print(l + 1);
                 }
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             if (m_predicateList != null)
                 {
                 m_predicateList.populateTypeTable(m_stable);
                 }
         }
 
         public void populateSymbolTable()
         {
             if (m_predicateList != null)
                 {
                 m_predicateList.populateSymbolTable();
                 }
         }
 
         public void checkType()
         {
             if (m_predicateList != null)
                 {
                 m_predicateList.checkType();
                 }
         }
     }
 
     public class AstPredicateList extends AstBase
     {
         List m_predicates = new ArrayList();
 
         public void add(AstBase n)
         {
             if (n instanceof AstPredicate)
                 {
                 m_predicates.add(n);
                 }
             else if (n instanceof AstVector)
                 {
                 int i;
                 List v = ((AstVector) n).m_list;
                 for (i = 0; i < v.size(); i++)
                     {
                     m_predicates.add((AstPredicate) v.get(i));
                     }
                 }
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             int i;
             m_stable = stable;
             for (i = 0; i < m_predicates.size(); i++)
                 {
                 ((AstBase) m_predicates.get(i)).populateTypeTable(m_stable);
                 }
         }
 
         public void populateSymbolTable()
         {
             int i;
             for (i = 0; i < m_predicates.size(); i++)
                 {
                 ((AstBase) m_predicates.get(i)).populateSymbolTable();
                 }
         }
 
         public void checkType()
         {
             int i;
             for (i = 0; i < m_predicates.size(); i++)
                 {
                 ((AstBase) m_predicates.get(i)).checkType();
                 }
         }
     }
 
     /*
      * Class Paragraphs
      */
     public class AstClassHeader extends AstBase
     {
         ClassDef classDef;
         AstClassName m_className;
         AstFormalParameters m_formalParameters;
 
         public AstClassHeader(ClassDef classDef)
         {
             this.classDef = classDef;
         }
     }
 
     public class AstVisibilityList extends AstBase
     {
         ClassDef classDef;
         AstDeclarationNameList m_declarationNameList;
 
         public AstVisibilityList(ClassDef classDef)
         {
             this.classDef = classDef;
 
         }
         public void add(AstBase n)
         {
             if (n instanceof AstDeclarationNameList)
                 {
                 m_declarationNameList = (AstDeclarationNameList) n;
 //            m_ttcl = m_declarationNameList.m_ttcl;
                 }
             else
                 {
                 super.add(n);
                 }
         }
 
         public void setVisible(AstSymbolTable stable)
         {
             int i;
             if (m_declarationNameList != null)
                 {
                 for (i = 0; i < m_declarationNameList.m_declarationNameList.size(); i++)
                     {
                     AstDeclarationName name = (AstDeclarationName) m_declarationNameList.m_declarationNameList.get(i);
                     if (!stable.addVisible(name.getName()))
                         {
                         reportTypeError(
                                 name.getName() + " is not a valid member and cannot be included in the visiblity list",
                                 name.m_token,
                                 this.classDef,
                                 "visibilityList"
                                 );
                         }
                     }
                 }
         }
     }
 
     public class AstInheritedClass extends AstBase
     {
         InheritedClass inheritedClass;
 
         AstClassName m_className;
         AstActualParameters m_actualParameters;
         AstRenameList m_renameList;
 
         public AstInheritedClass(InheritedClass inheritedClass)
         {
             this.inheritedClass = inheritedClass;
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             if (m_actualParameters != null)
                 {
                 m_actualParameters.populateTypeTable(m_stable);
                 }
         }
 
         public void populateTypeTable()
         {
             if (m_actualParameters != null)
                 {
                 m_actualParameters.populateSymbolTable();
                 }
         }
 
         public void checkType()
         {
             AstType type = m_stable.getTypeDef(m_className.getName());
             if (type == null)
                 {
                 this.reportTypeError(m_className.getName() + " is not defined as a class.",
                                      m_className.m_token,
                                      this.inheritedClass,
                                      "name"
                 );
                 return;
                 }
             if (type.getType() != AstType.TYPE_SET)
                 {
                 this.reportTypeError(m_className.getName() + " is not defined as a class.",
                                      m_className.m_token,
                                      this.inheritedClass,
                                      "name"
                 );
                 }
             if (type.getSetType().getType() != AstType.TYPE_CLASS)
                 {
                 this.reportTypeError(m_className.getName() + " is not defined as a class.",
                                      m_className.m_token,
                                      this.inheritedClass,
                                      "name"
                 );
                 return;
                 }
             int numParams = 0;
             if (m_actualParameters != null)
                 {
                 numParams = m_actualParameters.getNumParams();
                 }
             if (numParams != type.getSetType().m_numClassFormalParamList)
                 {
                 reportTypeError(
                         "The number of actual parameters for an inherited class must match the number of formal parameters in the definition.",
                         m_className.m_token,
                         this.inheritedClass,
                         "name"
                 );
                 }
             m_stable.add(m_className.getName(), type.getSetType().m_classMembers);
 
             if (m_actualParameters != null)
                 {
                 m_actualParameters.checkType();
                 }
         }
     }
 
     public class AstState extends AstBase
     {
         State state;
         AstDeclaration m_declaration;
         AstDeclaration m_deltaDeclaration;
         AstPredicateList m_predicateList;
 
         public AstState(State state)
         {
             this.state = state;
         }
 
         public void add(AstBase n)
         {
             if (n instanceof AstDeclaration)
                 {
                 if (m_declaration == null)
                     {
                     m_declaration = (AstDeclaration) n;
                     }
                 else
                     {
                     m_declaration.add(n);
                     }
                 }
             else if (n instanceof AstPredicateList)
                 {
                 if (m_predicateList == null)
                     {
                     m_predicateList = (AstPredicateList) n;
                     }
                 else
                     {
                     m_predicateList.add(n);
                     }
                 }
             else if (n instanceof AstPredicate)
                 {
                 if (m_predicateList == null)
                     {
                     m_predicateList = new AstPredicateList();
                     }
                 m_predicateList.add(n);
                 }
             else if (n instanceof AstVector)
                 {
                 if (m_predicateList == null)
                     {
                     m_predicateList = new AstPredicateList();
                     }
                 int i;
                 List v = ((AstVector) n).m_list;
                 for (i = 0; i < v.size(); i++)
                     {
                     m_predicateList.add((AstPredicate) v.get(i));
                     }
                 }
             else
                 {
                 super.add(n);
                 }
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             if (m_declaration != null)
                 {
                 m_declaration.populateTypeTable(m_stable);
                 }
             if (m_deltaDeclaration != null)
                 {
                 m_deltaDeclaration.populateTypeTable(m_stable);
                 }
             if (m_predicateList != null)
                 {
                 m_predicateList.populateTypeTable(m_stable);
                 }
         }
 
         public void populateSymbolTable()
         {
             if (m_declaration != null)
                 {
                 m_declaration.populateSymbolTable();
                 }
             if (m_deltaDeclaration != null)
                 {
                 m_deltaDeclaration.populateSymbolTable();
                 }
             if (m_predicateList != null)
                 {
                 m_predicateList.populateSymbolTable();
                 }
         }
 
         public void checkType()
         {
             if (m_declaration != null)
                 {
                 m_declaration.checkType();
                 }
             if (m_predicateList != null)
                 {
                 m_predicateList.checkType();
                 }
         }
     }
 
     public class AstInitialState extends AstBase
     {
         InitialState initialState;
         AstPredicateList m_predicateList;
 
         public AstInitialState(InitialState initialState)
         {
             this.initialState = initialState;
         }
 
         public void add(AstBase n)
         {
             if (n instanceof AstPredicateList)
                 {
                 if (m_predicateList == null)
                     {
                     m_predicateList = (AstPredicateList) n;
                     }
                 else
                     {
                     m_predicateList.add(n);
                     }
                 }
             else if (n instanceof AstPredicate)
                 {
                 if (m_predicateList == null)
                     {
                     m_predicateList = new AstPredicateList();
                     m_predicateList.add((AstPredicate) n);
                     }
                 else
                     {
                     m_predicateList.add(n);
                     }
                 }
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             if (m_predicateList != null)
                 {
                 m_predicateList.populateTypeTable(m_stable);
                 }
         }
 
         public void populateSymbolTable()
         {
             m_stable.add("INIT", new AstType());
             if (m_predicateList != null)
                 {
                 m_predicateList.populateSymbolTable();
                 }
         }
 
         public void checkType()
         {
             if (m_predicateList != null)
                 {
                 m_predicateList.checkType();
                 }
         }
     }
 
     public class AstOperation extends AstBase
     {
         Operation operation;
 
         AstOperationName m_operationName;
         AstDeltaList m_deltaList;
         AstDeclaration m_declaration;
         AstPredicateList m_predicateList;
         AstOperationExpression m_operationExpression;
 
         public AstOperation(Operation operation)
         {
             this.operation = operation;
         }
 
         public void add(AstBase n)
         {
             if (n instanceof AstDeclaration)
                 {
                 if (m_declaration == null)
                     {
                     m_declaration = (AstDeclaration) n;
                     }
                 else
                     {
                     m_declaration.add(n);
                     }
                 }
             else if (n instanceof AstOperationName)
                 {
                 m_operationName = (AstOperationName) n;
                 }
             else if (n instanceof AstPredicateList)
                 {
                 if (m_predicateList == null)
                     {
                     m_predicateList = (AstPredicateList) n;
                     }
                 else
                     {
                     m_predicateList.add(n);
                     }
                 }
             else if (n instanceof AstPredicate)
                 {
                 if (m_predicateList == null)
                     {
                     m_predicateList = new AstPredicateList();
                     }
                 m_predicateList.add(n);
                 }
             else if (n instanceof AstOperationExpression)
                 {
                 m_operationExpression = (AstOperationExpression) n;
                 }
             else if (n instanceof AstDeltaList)
                 {
                 m_deltaList = (AstDeltaList) n;
                 }
             else if (n instanceof AstDeclarationNameList)
                 {
                 m_deltaList = new AstDeltaList(this.operation);
                 m_deltaList.m_declarationNameList = (AstDeclarationNameList) n;
 //            m_deltaList.m_ttcl = n.m_ttcl;
                 }
             else if (n instanceof AstIdentifier)
                 {
                 m_operationName = new AstOperationName(operation);
                 m_operationName.m_identifier = (AstIdentifier) n;
                 }
             else if (n instanceof AstVector)
                 {
                 if (m_predicateList == null)
                     {
                     m_predicateList = new AstPredicateList();
                     }
                 int i;
                 List v = ((AstVector) n).m_list;
                 for (i = 0; i < v.size(); i++)
                     {
                     m_predicateList.m_predicates.add((AstBase) v.get(i));
                     }
                 }
             else
                 {
                 super.add(n);
                 }
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             String operationName = "";
             if (m_operationName != null)
                 {
                 operationName = m_operationName.getName();
                 }
             m_stable = new AstSymbolTable(stable);
             m_stable.m_name = operationName;
             m_stable.m_isOperation = true;
             stable.addKid(m_stable);
 
             AstType type = new AstType(AstType.TYPE_OPERATION);
             type.m_name = operationName;
             type.m_classMembers = m_stable;
             stable.add(operationName, type);
 
             if (m_declaration != null)
                 {
                 m_declaration.populateTypeTable(m_stable);
                 }
             if (m_predicateList != null)
                 {
                 m_predicateList.populateTypeTable(m_stable);
                 }
             if (m_operationExpression != null)
                 {
                 m_operationExpression.populateTypeTable(m_stable);
                 }
         }
 
         public void populateSymbolTable()
         {
             boolean added;
 
             String operationName = "";
             if (m_operationName != null)
                 {
                 operationName = m_operationName.getName();
                 }
             AstType type = new AstType(AstType.TYPE_OPERATION);
             added = m_stable.add(operationName, type);
             if (!added)
                 {
                 m_operationName.checkType();
                 }
             if (m_declaration != null)
                 {
                 m_declaration.populateSymbolTable();
                 }
             if (m_predicateList != null)
                 {
                 m_predicateList.populateSymbolTable();
                 }
             if (m_operationExpression != null)
                 {
                 m_operationExpression.populateSymbolTable();
                 }
         }
 
         public void checkType()
         {
             if (m_declaration != null)
                 {
                 m_declaration.checkType();
                 }
             if (m_predicateList != null)
                 {
                 m_predicateList.checkType();
                 }
             if (m_operationExpression != null)
                 {
                 m_operationExpression.checkType();
                 }
 
             if (m_deltaList != null)
                 {
                 int i;
                 List v = m_deltaList.m_declarationNameList.m_declarationNameList;
                 for (i = 0; i < v.size(); i++)
                     {
                     AstDeclarationName id = (AstDeclarationName) v.get(i);
                     AstType type = m_stable.getType(id.getName());
                     if (type == null)
                         {
                         id.reportTypeError(id.getName() + " is not defined",
                                            id.m_token,
                                            this.operation,
                                            "deltaList"
                         );
                         }
                     }
                 }
         }
     }
 
     /*
      * Abbreviation
      */
     public class AstAbbreviation extends AstBase
     {
         public String getName()
         {
             return "";
         }
     }
 
     public class AstAbbreviation1 extends AstAbbreviation
     {
         SpecObject specObject;
         String property;
         AstVariableName m_variableName;
         AstFormalParameters m_formalParameters;
 
         AstAbbreviation1(SpecObject specObject, String property)
         {
             this.specObject = specObject;
             this.property = property;
         }
 
         public void print(int l)
         {
             print(l, "Abbreviation");
             m_variableName.print(l + 1);
             if (m_formalParameters != null)
                 {
                 m_formalParameters.print(l + 1);
                 }
         }
 
         public String getName()
         {
             return m_variableName.getName();
         }
 
         public void checkType()
         {
             reportTypeError(m_variableName.getName() + " is already defined",
                             m_variableName.m_token,
                             specObject,
                             property
             );
         }
     }
 
     public class AstAbbreviation2 extends AstAbbreviation
     {
         AstPrefixGenericName m_prefixGenericName;
         AstIdentifier m_identifier;
 
         public void print(int l)
         {
             print(l, "Abbreviation");
             m_prefixGenericName.print(l + 1);
             m_identifier.print(l + 1);
         }
 
         public String getName()
         {
             return m_identifier.getName();
         }
     }
 
     public class AstAbbreviation3 extends AstAbbreviation
     {
         AstIdentifier m_identifier1;
         AstInfixGenericName m_infixGenericName;
         AstIdentifier m_identifier2;
 
         public void print(int l)
         {
             print(l, "Abbreviation");
             m_identifier1.print(l + 1);
             m_infixGenericName.print(l + 1);
             m_identifier2.print(l + 1);
         }
     }
 
     /*
      * Inherited Class
      */
     /*
      * Branch
      */
     public class AstBranch extends AstPara
     {
         public void populateSymbolTable(AstType type)
         {
         }
     }
 
     public class AstBranch1 extends AstBranch
     {
         SpecObject specObject;
         String property;
 
         AstIdentifier m_identifier;
 
         public AstBranch1(SpecObject specObject, String property)
         {
             this.specObject = specObject;
             this.property = property;
         }
         public void print(int l)
         {
             if (m_identifier != null)
                 {
                 m_identifier.print(l);
                 }
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
         }
 
         public void populateSymbolTable(AstType type)
         {
             boolean add = m_stable.add(m_identifier.getName(), type);
             if (!add)
                 {
                 String name = m_identifier.getName();
                 String msg = name + " has already been defined as part of another type.";
                 TozeToken token = m_identifier.m_token;
                 this.reportTypeError(msg,
                                      token,
                                      specObject,
                                      property
                 );
                 }
         }
     }
 
     public class AstBranch2 extends AstBranch
     {
         AstVariableName m_variableName;
         AstExpression m_expression;
     }
 
     /*
      * BasicDeclaration
      */
     public class AstBasicDeclaration extends AstPara
     {
     }
 
     public class AstBasicDeclaration1 extends AstBasicDeclaration
     {
         SpecObject specObject;
         String property;
 
         AstDeclarationNameList m_declarationNameList;
         AstExpression m_expression;
 
         public AstBasicDeclaration1(SpecObject specObject, String property)
         {
             this.specObject = specObject;
         }
 
         public void print(int l)
         {
             print(l, "BasicDeclaration");
             m_declarationNameList.print(l + 1);
             m_expression.print(l + 1);
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             m_expression.populateTypeTable(m_stable);
         }
 
         public void populateSymbolTable()
         {
             m_expression.populateSymbolTable();
             m_declarationNameList.populateSymbolTable(m_stable, new AstType());
         }
 
         public void checkType()
         {
             AstType type = m_expression.getType();
             if (type.isUndefined())
                 {
                 return;
                 }
             AstType setType = type.getSetType();
             if (setType == null)
                 {
                 reportTypeError("Expressions used in declarations must result in a set",
                                 m_token,
                                 specObject,
                                 property
                 );
                 return;
                 }
             m_declarationNameList.checkType(m_stable, type.getSetType());
         }
 
         public AstType getType()
         {
             return m_expression.getType();
         }
     }
 
     public class AstBasicDeclaration2 extends AstBasicDeclaration
     {
         AstSchemaReference m_schemaReference;
 
         public void print(int l)
         {
             print(l, "BasicDeclaration");
             m_schemaReference.print(l + 1);
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             m_schemaReference.populateTypeTable(m_stable);
         }
 
         public void checkType()
         {
             m_schemaReference.checkType();
         }
     }
 
     /*
      *
      */
     public class AstSchemaHeader extends AstBase
     {
         SpecObject specObject;
         String property;
 
         AstSchemaName m_schemaName;
         AstFormalParameters m_formalParameters;
         boolean added = false;
 
         public AstSchemaHeader(SpecObject specObject, String property)
         {
             this.specObject = specObject;
             this.property = property;
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             if (m_formalParameters != null)
                 {
                 m_formalParameters.populateTypeTable(m_stable);
                 }
 
             AstType type = new AstType(AstType.TYPE_SET);
             type.m_setType = new AstType(AstType.TYPE_SCHEMA);
             type.m_setType.m_name = m_schemaName.getName();
             type.m_setType.m_classMembers = m_stable;
             added = stable.m_parent.addType(m_schemaName.getName(), type);
             if (!added)
                 {
                 this.reportTypeError(m_schemaName.getName() + " is already defined as a schema.",
                                      m_schemaName.m_token,
                                      specObject,
                                      property
                 );
                 }
         }
 
         public void populateSymbolTable()
         {
             if (m_formalParameters != null)
                 {
                 m_formalParameters.populateSymbolTable();
                 }
         }
     }
 
     public class AstFormalParameters extends AstBase
     {
         List m_identifiers = new ArrayList();
 
         public int getNumParameters()
         {
             return m_identifiers.size();
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             int i;
             boolean added;
 
             m_stable = stable;
 
             for (i = 0; i < m_identifiers.size(); i++)
                 {
                 AstIdentifier id = (AstIdentifier) m_identifiers.get(i);
                 AstType type = new AstType(AstType.TYPE_SET);
                 type.m_setType = new AstType(AstType.TYPE_GENERIC);
                 added = m_stable.addType(id.getName(), type);
                 if (!added)
                     {
                     id.checkType();
                     }
                 }
         }
     }
 
     public class AstSchemaReference extends AstBase
     {
         SpecObject specObject;
         String property;
 
         AstSchemaName m_schemaName;
         AstDecorations m_decorations;
         AstActualParameters m_actualParameters;
         AstRenameList m_renameList;
 
         public AstSchemaReference(SpecObject specObject, String property)
         {
             this.specObject = specObject;
         }
 
         public void print(int l)
         {
             print(l, "SchemaReference");
             m_schemaName.print(l + 1);
             if (m_decorations != null)
                 {
                 m_decorations.print(l + 1);
                 }
             if (m_actualParameters != null)
                 {
                 m_actualParameters.print(l + 1);
                 }
             if (m_renameList != null)
                 {
                 m_renameList.print(l + 1);
                 }
         }
 
         public void checkType()
         {
             AstType type = m_stable.getTypeDef(m_schemaName.getName());
             if (type == null)
                 {
                 reportTypeError("The schema " + m_schemaName.getName() + " does not exist",
                                 m_schemaName.m_token,
                                 specObject,
                                 property
                 );
                 return;
                 }
             if (!type.isSchemaDef())
                 {
                 reportTypeError(m_schemaName.getName() + " is not a schema name",
                                 m_schemaName.m_token,
                                 specObject,
                                 property
                 );
                 return;
                 }
 
             /*
              * Add a reference to the schema so that its members can be
              * accessed.
              */
 
             m_stable.add(m_schemaName.getName(), type.m_setType.m_classMembers);
         }
     }
 
     public class AstSchemaName extends AstBase
     {
         public String getName()
         {
             return m_token.m_value;
         }
 
         public void print(int l)
         {
             print(l, "Schema " + getName());
         }
     }
 
     public class AstDecorations extends AstBase
     {
         List m_decorations = new ArrayList();
 
         public void add(char c)
         {
             m_decorations.add(new Character(c));
         }
 
         public void print(int l)
         {
             print(l, "decorations");
         }
 
         public String getName()
         {
             String name = "";
             int i;
             for (i = 0; i < m_decorations.size(); i++)
                 {
                 if (((Character) m_decorations.get(i)).charValue() != '\'')
                     {
                     name += (Character) m_decorations.get(i);
                     }
                 }
             return name;
         }
     }
 
     public class AstVariableName extends AstBase
     {
         public String getName()
         {
             return "";
         }
     }
 
     public class AstVariableName1 extends AstVariableName
     {
         AstIdentifier m_identifier;
         AstDecorations m_decorations;
 
         public void print(int l)
         {
             print(l, "VariableName");
             m_identifier.print(l + 1);
             if (m_decorations != null)
                 {
                 m_decorations.print(l + 1);
                 }
         }
 
         public String getName()
         {
             if (m_decorations == null)
                 {
                 return m_identifier.getName();
                 }
             return m_identifier.getName() + m_decorations.getName();
         }
     }
 
     public class AstVariableName2 extends AstVariableName
     {
         AstOperatorName m_operatorName;
 
         public void print(int l)
         {
             print(l, "OperatorName");
             m_operatorName.print(l + 1);
         }
 
         public String getName()
         {
             return m_operatorName.m_token.m_value;
         }
     }
 
     public class AstOperatorName extends AstBase
     {
         AstBase m_infixFunctionName;
         AstBase m_infixGenericName;
         AstBase m_infixRelationName;
         AstBase m_prefixGenericName;
         AstBase m_prefixRelationName;
         AstBase m_postfixFunctionName;
         AstBase m_decoration;
         boolean m_hasImage = false;
     }
 
     public class AstIdentifier extends AstBase
     {
         SpecObject specObject;
         String property;
 
         AstDecorations m_decorations;
 
         public AstIdentifier(SpecObject specObject, String property)
         {
             this.specObject = specObject;
             this.property = property;
         }
 
         public String getName()
         {
             if (m_decorations == null)
                 {
                 return m_token.m_value;
                 }
             return m_token.m_value + m_decorations.getName();
         }
 
         public void print(int l)
         {
             print(l, "Identifier = " + m_token.m_value);
             if (m_decorations != null)
                 {
                 m_decorations.print(l + 1);
                 }
         }
 
         public void checkType()
         {
             reportTypeError(m_token.m_value + " is already defined",
                             m_token,
                             specObject,
                             property
             );
         }
     }
 
     public class AstDeclarationNameList extends AstBase
     {
         SpecObject specObject;
         String property;
 
         List m_declarationNameList = new ArrayList();
 
         public AstDeclarationNameList(SpecObject specObject, String property)
         {
             this.specObject = specObject;
             this.property = property;
         }
 
         public void print(int l)
         {
             int i;
             print(l, "DeclarationNameList");
             for (i = 0; i < m_declarationNameList.size(); i++)
                 {
                 ((AstDeclarationName) m_declarationNameList.get(i)).print(l + 1);
                 }
         }
 
         public void populateSymbolTable(AstSymbolTable stable, AstType type)
         {
             int i;
             for (i = 0; i < m_declarationNameList.size(); i++)
                 {
                 String name = ((AstDeclarationName) m_declarationNameList.get(i)).getName();
                 if (!stable.add(name, type))
                     {
                     reportTypeError(name + " is already defined",
                                     ((AstDeclarationName) m_declarationNameList.get(i)).getToken(),
                                     specObject,
                                     property
                     );
                     }
                 }
         }
 
         public void checkType(AstSymbolTable stable, AstType type)
         {
             int i;
             for (i = 0; i < m_declarationNameList.size(); i++)
                 {
                 String name = ((AstDeclarationName) m_declarationNameList.get(i)).getName();
                 stable.setSymbol(name, type);
                 }
         }
     }
 
     public class AstDeclarationName extends AstBase
     {
         public String getName()
         {
             return null;
         }
 
         public TozeToken getToken()
         {
             return null;
         }
     }
 
     public class AstDeclarationName1 extends AstDeclarationName
     {
         AstIdentifier m_identifier;
 
         public void print(int l)
         {
             print(l, "DeclarationName");
             m_identifier.print(l + 1);
         }
 
         public void populateSymbolTable(AstSymbolTable stable, AstType type)
         {
             stable.add(m_identifier.m_token.m_value, type);
         }
 
         public String getName()
         {
             return m_identifier.getName();
         }
 
         public TozeToken getToken()
         {
             return m_identifier.m_token;
         }
     }
 
     public class AstDeclarationName2 extends AstDeclarationName
     {
         AstOperatorName m_operatorName;
 
         public void print(int l)
         {
             print(l, "DeclarationName");
             m_operatorName.print(l + 1);
         }
 
         public TozeToken getToken()
         {
             return m_operatorName.m_token;
         }
     }
 
     public class AstClassName extends AstBase
     {
         public String getName()
         {
             return m_token.m_value;
         }
     }
 
     public class AstActualParameters extends AstBase
     {
         List m_actualParameters = new ArrayList(); // AstActualParameter
 
         public int getNumParams()
         {
             return m_actualParameters.size();
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             int i;
             for (i = 0; i < m_actualParameters.size(); i++)
                 {
                 AstExpression ex = (AstExpression) m_actualParameters.get(i);
                 ex.populateTypeTable(m_stable);
                 }
         }
 
         public void populateSymbolTable()
         {
             int i;
             for (i = 0; i < m_actualParameters.size(); i++)
                 {
                 AstExpression ex = (AstExpression) m_actualParameters.get(i);
                 ex.populateSymbolTable();
                 }
         }
 
         public void checkType()
         {
             int i;
             for (i = 0; i < m_actualParameters.size(); i++)
                 {
                 AstExpression ex = (AstExpression) m_actualParameters.get(i);
                 ex.checkType();
                 }
         }
     }
 
     public class AstActualParameter extends AstBase
     {
         AstExpression m_expression;
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             m_expression.populateTypeTable(m_stable);
         }
 
         public void populateSymbolTable()
         {
             m_expression.populateSymbolTable();
         }
 
         public void checkType()
         {
             m_expression.checkType();
         }
     }
 
     public class AstRenameList extends AstBase
     {
         List m_renameList = new ArrayList(); // AstRenameItem
     }
 
     public class AstRenameItem extends AstBase
     {
         AstDeclarationName m_declarationName1;
         AstDeclarationName m_declarationName2;
     }
 
     public class AstSchemaText extends AstBase
     {
         AstDeclaration m_declaration;
         AstPredicate m_predicate;
 
         public void print(int l)
         {
             print(l, "SchemaText - SchemaExpression");
             m_declaration.print(l + 1);
             if (m_predicate != null)
                 {
                 m_predicate.print(l + 1);
                 }
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             m_declaration.populateTypeTable(m_stable);
             if (m_predicate != null)
                 {
                 m_predicate.populateTypeTable(m_stable);
                 }
         }
 
         public void populateSymbolTable()
         {
             m_declaration.populateSymbolTable();
             if (m_predicate != null)
                 {
                 m_predicate.populateSymbolTable();
                 }
         }
 
         public void checkType()
         {
             m_declaration.checkType();
             if (m_predicate != null)
                 {
                 m_predicate.checkType();
                 }
         }
 
         public AstType getType()
         {
             checkType();
             return m_declaration.getType();
         }
     }
 
     public class AstDeclaration extends AstBase
     {
         List m_decls = new ArrayList();
 
         public void print(int l)
         {
             int i;
             print(l, "Declaration");
             for (i = 0; i < m_decls.size(); i++)
                 {
                 ((AstBase) m_decls.get(i)).print(l + 1);
                 }
         }
 
         public void add(AstBase n)
         {
             if (n instanceof AstDeclaration)
                 {
                 int i;
                 AstDeclaration d = (AstDeclaration) n;
                 for (i = 0; i < d.m_decls.size(); i++)
                     {
                     m_decls.add(d.m_decls.get(i));
                     }
                 }
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             int i;
             m_stable = stable;
             for (i = 0; i < m_decls.size(); i++)
                 {
                 ((AstBase) m_decls.get(i)).populateTypeTable(m_stable);
                 }
         }
 
         public void populateSymbolTable()
         {
             int i;
             for (i = 0; i < m_decls.size(); i++)
                 {
                 ((AstBase) m_decls.get(i)).populateSymbolTable();
                 }
         }
 
         public void checkType()
         {
             int i;
             for (i = 0; i < m_decls.size(); i++)
                 {
                 ((AstBase) m_decls.get(i)).checkType();
                 }
         }
 
         public AstType getType()
         {
             if (m_decls.size() == 0)
                 {
                 return new AstType();
                 }
             return ((AstBase) m_decls.get(0)).getType();
         }
     }
 
     public class AstLetDefinition extends AstBase
     {
         AstVariableName m_variableName;
         AstExpression m_expression;
     }
 
     public class AstOperationName extends AstBase
     {
         Operation operation;
         AstIdentifier m_identifier;
 
         public AstOperationName(Operation operation)
         {
             this.operation = operation;
         }
 
         public String getName()
         {
             return m_identifier.getName();
         }
 
         public void checkType()
         {
             reportTypeError(m_identifier.getName() + " is already defined",
                             m_identifier.m_token,
                             operation,
                             "name"
             );
         }
     }
 
     public class AstDeltaList extends AstBase
     {
         Operation operation;
         AstDeclarationNameList m_declarationNameList;
 
         public AstDeltaList(Operation operation)
         {
             this.operation = operation;
         }
     }
 
     /*
      * OperationExpression
      */
     public class AstOperationExpression extends AstBase
     {
         public void checkType()
         {
             getType();
         }
     }
 
     public class AstAndO extends AstOperationExpression
     {
         AstDeclaration m_declaration;
         AstPredicate m_predicate;
         AstOperationExpression m_operationExpression;
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             m_declaration.populateTypeTable(m_stable);
             m_predicate.populateTypeTable(m_stable);
         }
 
         public void populateSymbolTable()
         {
             m_declaration.populateSymbolTable();
             m_predicate.populateSymbolTable();
         }
 
         public AstType getType()
         {
             m_predicate.checkType();
             return new AstType(AstType.TYPE_BOOL);
         }
     }
 
     public class AstBoxO extends AstOperationExpression
     {
         AstDeclaration m_declaration;
         AstPredicate m_predicate;
         AstOperationExpression m_operationExpression;
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             m_declaration.populateTypeTable(m_stable);
             m_predicate.populateTypeTable(m_stable);
         }
 
         public void populateSymbolTable()
         {
             m_declaration.populateSymbolTable();
             m_predicate.populateSymbolTable();
         }
 
         public AstType getType()
         {
             m_predicate.checkType();
             return new AstType(AstType.TYPE_BOOL);
         }
     }
 
     public class AstCompO extends AstOperationExpression
     {
         AstDeclaration m_declaration;
         AstPredicate m_predicate;
         AstOperationExpression m_operationExpression;
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             m_declaration.populateTypeTable(m_stable);
             m_predicate.populateTypeTable(m_stable);
         }
 
         public void populateSymbolTable()
         {
             m_declaration.populateSymbolTable();
             m_predicate.populateSymbolTable();
         }
 
         public AstType getType()
         {
             m_predicate.checkType();
             return new AstType(AstType.TYPE_BOOL);
         }
     }
 
     public class AstOperationExpressionBinary extends AstOperationExpression
     {
         AstOperationExpression m_expressionL;
         AstOperationExpression m_expressionR;
 
         public String getName()
         {
             if (m_token == null)
                 {
                 return "";
                 }
             return m_token.m_value;
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             m_expressionL.populateTypeTable(m_stable);
             m_expressionR.populateTypeTable(m_stable);
         }
 
         public void populateSymbolTable()
         {
             m_expressionL.populateSymbolTable();
             m_expressionR.populateSymbolTable();
         }
 
         public void checkType()
         {
             AstType typeL = m_expressionL.getType();
             AstType typeR = m_expressionR.getType();
 
             m_expressionL.checkType();
             m_expressionR.checkType();
         }
     }
 
     public class AstMemberOp extends AstOperationExpression
     {
         AstMemberX m_member;
         AstRenameList m_renameList;
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             m_member.populateTypeTable(m_stable);
         }
 
         public void populateSymbolTable()
         {
             if (m_member != null)
                 {
                 m_member.populateSymbolTable();
                 }
         }
 
         public AstType getType()
         {
             if (m_member != null)
                 {
                 m_member.checkType();
                 }
             return new AstType(AstType.TYPE_BOOL);
         }
     }
 
     public class AstOperationExpression1 extends AstOperationExpression
     {
         Operation operation;
 
         AstDeltaList m_deltaList;
         AstPredicate m_predicate;
         AstDeclaration m_declaration;
 
         public AstOperationExpression1(Operation operation)
         {
             this.operation = operation;
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             if (m_declaration != null)
                 {
                 m_declaration.populateTypeTable(m_stable);
                 }
             if (m_predicate != null)
                 {
                 m_predicate.populateTypeTable(m_stable);
                 }
         }
 
         public void populateSymbolTable()
         {
             if (m_declaration != null)
                 {
                 m_declaration.populateSymbolTable();
                 }
             if (m_predicate != null)
                 {
                 m_predicate.populateSymbolTable();
                 }
         }
 
         public AstType getType()
         {
             if (m_declaration != null)
                 {
                 m_declaration.checkType();
                 }
             if (m_predicate != null)
                 {
                 m_predicate.checkType();
                 }
 
             if (m_deltaList != null)
                 {
                 int i;
                 List v = m_deltaList.m_declarationNameList.m_declarationNameList;
                 for (i = 0; i < v.size(); i++)
                     {
                     AstDeclarationName id = (AstDeclarationName) v.get(i);
                     AstType type = m_stable.getType(id.getName());
                     if (type == null)
                         {
                         reportTypeError(id.getName() + " is not defined for this operation",
                                         id.m_token,
                                         operation,
                                         "declaration"
                         );
                         }
                     }
                 }
             return new AstType(AstType.TYPE_BOOL);
         }
     }
 
     public class AstOperationReference extends AstOperationExpression
     {
         Operation operation;
 
         AstIdentifier m_identifier;
         AstRenameList m_renameList;
 
         public AstOperationReference(Operation operation)
         {
             this.operation = operation;
         }
         public void checkType()
         {
             getType();
         }
 
         public AstType getType()
         {
             AstType type = m_stable.getType(m_identifier.getName());
             if (type == null)
                 {
                 reportTypeError("The operation " + m_identifier.getName() + " does not exist",
                                 m_identifier.m_token,
                                 operation,
                                 "name"
                 );
                 return new AstType();
                 }
             if (!type.isOperation())
                 {
                 reportTypeError(m_identifier.getName() + " is not an operation name",
                                 m_identifier.m_token,
                                 operation,
                                 "name"
                 );
                 return new AstType();
                 }
 
             /*
              * Add a reference to the schema so that its members can be
              * accessed.
              */
 
             if (type.m_classMembers != null)
                 {
                 m_stable.add(m_identifier.getName(), type.m_classMembers);
                 }
 
             return type;
         }
     }
 
     /*
      * Predicate
      */
     public class AstPredicate extends AstBase
     {
         SpecObject specObject;
         String property;
 
         public AstPredicate(SpecObject specObject, String property)
         {
             this.specObject = specObject;
             this.property = property;
         }
 
         public void checkType()
         {
             AstType type = getType();
             if (type.getType() != AstType.TYPE_BOOL)
                 {
                 reportTypeError("Predicates must evaluate to a boolean value",
                                 m_token,
                                 specObject,
                                 property
                 );
                 }
         }
     }
 
     public class AstPredicateExpression extends AstPredicate
     {
         AstExpression m_expression;
 
         public AstPredicateExpression(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = new AstSymbolTable(stable);
             stable.addKid(m_stable);
             m_expression.populateTypeTable(m_stable);
         }
 
         public void populateSymbolTable()
         {
             m_expression.populateSymbolTable();
         }
 
         public AstType getType()
         {
             return m_expression.getType();
         }
     }
 
     public class AstForAllP extends AstPredicate
     {
         AstSchemaText m_schemaText;
         AstPredicate m_predicate;
 
         public AstForAllP(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public void print(int l)
         {
             print(l, "ForAll - Predicate");
             m_schemaText.print(l + 1);
             m_predicate.print(l + 1);
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = new AstSymbolTable(stable);
             stable.addKid(m_stable);
             m_schemaText.populateTypeTable(m_stable);
             m_predicate.populateTypeTable(m_stable);
         }
 
         public void populateSymbolTable()
         {
             m_schemaText.populateSymbolTable();
             m_predicate.populateSymbolTable();
         }
 
         public void checkType()
         {
             m_schemaText.checkType();
             m_predicate.getType();
         }
 
         public AstType getType()
         {
             checkType();
             return new AstType(AstType.TYPE_BOOL);
         }
     }
 
     public class AstThereExistsP extends AstPredicate
     {
         AstSchemaText m_schemaText;
         AstPredicate m_predicate;
 
         public AstThereExistsP(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public void print(int l)
         {
             print(l, "ThereExists - Predicate");
             m_schemaText.print(l + 1);
             m_predicate.print(l + 1);
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = new AstSymbolTable(stable);
             stable.addKid(m_stable);
             m_schemaText.populateTypeTable(m_stable);
             m_predicate.populateTypeTable(m_stable);
         }
 
         public void populateSymbolTable()
         {
             m_schemaText.populateSymbolTable();
             m_predicate.populateSymbolTable();
         }
 
         public void checkType()
         {
             m_schemaText.checkType();
             m_predicate.getType();
         }
 
         public AstType getType()
         {
             checkType();
             return new AstType(AstType.TYPE_BOOL);
         }
     }
 
     public class AstThereExists1P extends AstPredicate
     {
         AstSchemaText m_schemaText;
         AstPredicate m_predicate;
 
         public AstThereExists1P(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public void print(int l)
         {
             print(l, "ThereExists1 - Predicate");
             m_schemaText.print(l + 1);
             m_predicate.print(l + 1);
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = new AstSymbolTable(stable);
             stable.addKid(m_stable);
             m_schemaText.populateTypeTable(m_stable);
             m_predicate.populateTypeTable(m_stable);
         }
 
         public void populateSymbolTable()
         {
             m_schemaText.populateSymbolTable();
             m_predicate.populateSymbolTable();
         }
 
         public void checkType()
         {
             m_schemaText.checkType();
             m_predicate.getType();
         }
 
         public AstType getType()
         {
             checkType();
             return new AstType(AstType.TYPE_BOOL);
         }
     }
 
     public class AstLetP extends AstPredicate
     {
         List m_lets = new ArrayList();
         AstPredicate m_predicate;
 
         public AstLetP(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public void print(int l)
         {
             print(l, "let - Predicate");
             m_predicate.print(l + 1);
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             m_predicate.populateTypeTable(m_stable);
         }
 
         public void populateSymbolTable()
         {
             m_predicate.populateSymbolTable();
         }
 
         public void checkType()
         {
             m_predicate.getType();
         }
 
         public AstType getType()
         {
             checkType();
             return new AstType(AstType.TYPE_BOOL);
         }
     }
 
     public class AstAndP extends AstPredicate
     {
         AstPredicate m_predicateL;
         AstPredicate m_predicateR;
 
         public AstAndP(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public void print(int l)
         {
             print(l, "AND - Predicate");
             m_predicateL.print(l + 1);
             m_predicateR.print(l + 1);
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             m_predicateL.populateTypeTable(m_stable);
             m_predicateR.populateTypeTable(m_stable);
         }
 
         public void populateSymbolTable()
         {
             m_predicateL.populateSymbolTable();
             m_predicateR.populateSymbolTable();
         }
 
         public AstType getType()
         {
             m_predicateL.checkType();
             m_predicateR.checkType();
             return new AstType(AstType.TYPE_BOOL);
         }
     }
 
     public class AstOrP extends AstPredicate
     {
         AstPredicate m_predicateL;
         AstPredicate m_predicateR;
 
         public AstOrP(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public void print(int l)
         {
             print(l, "OR - Predicate");
             m_predicateL.print(l + 1);
             m_predicateR.print(l + 1);
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             m_predicateL.populateTypeTable(m_stable);
             m_predicateR.populateTypeTable(m_stable);
         }
 
         public void populateSymbolTable()
         {
             m_predicateL.populateSymbolTable();
             m_predicateR.populateSymbolTable();
         }
 
         public AstType getType()
         {
             m_predicateL.checkType();
             m_predicateR.checkType();
             return new AstType(AstType.TYPE_BOOL);
         }
     }
 
     public class AstImpliesP extends AstPredicate
     {
         AstPredicate m_predicateL;
         AstPredicate m_predicateR;
 
         public AstImpliesP(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public void print(int l)
         {
             print(l, "IMPLIES - Predicate");
             m_predicateL.print(l + 1);
             m_predicateR.print(l + 1);
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             m_predicateL.populateTypeTable(m_stable);
             m_predicateR.populateTypeTable(m_stable);
         }
 
         public void populateSymbolTable()
         {
             m_predicateL.populateSymbolTable();
             m_predicateR.populateSymbolTable();
         }
 
         public AstType getType()
         {
             m_predicateL.checkType();
             m_predicateR.checkType();
             return new AstType(AstType.TYPE_BOOL);
         }
     }
 
     public class AstBiimpliesP extends AstPredicate
     {
         AstPredicate m_predicateL;
         AstPredicate m_predicateR;
 
         public AstBiimpliesP(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public void print(int l)
         {
             print(l, "BIIMPLIES - Predicate");
             m_predicateL.print(l + 1);
             m_predicateR.print(l + 1);
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             m_predicateL.populateTypeTable(m_stable);
             m_predicateR.populateTypeTable(m_stable);
         }
 
         public void populateSymbolTable()
         {
             m_predicateL.populateSymbolTable();
             m_predicateR.populateSymbolTable();
         }
 
         public AstType getType()
         {
             m_predicateL.checkType();
             m_predicateR.checkType();
             return new AstType(AstType.TYPE_BOOL);
         }
     }
 
     public class AstRelationsP extends AstPredicate
     {
         List m_relations = new ArrayList();
         AstExpression m_expressionL;
         AstExpression m_expressionR;
 
         public AstRelationsP(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public void print(int l)
         {
             print(l, getName());
             m_expressionL.print(l + 1);
             m_expressionR.print(l + 1);
         }
 
         public String getName()
         {
             int i;
             AstRelation rnode;
             String name = "";
             for (i = 0; i < m_relations.size(); i++)
                 {
                 rnode = (AstRelation) m_relations.get(0);
                 name += rnode.m_infixRelationName.m_token.m_value + " ";
                 }
             return name;
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             m_expressionL.populateTypeTable(m_stable);
             m_expressionR.populateTypeTable(m_stable);
         }
 
         public void populateSymbolTable()
         {
             m_expressionL.populateSymbolTable();
             m_expressionR.populateSymbolTable();
         }
 
         public AstType getType()
         {
             AstRelation r = (AstRelation) m_relations.get(0);
 
             AstType ret;
             AstType t1 = m_expressionL.getType();
             AstType t2 = m_expressionR.getType();
 
             if ((t1.isUndefined())
                     || (t2.isUndefined()))
                 {
                 return new AstType(AstType.TYPE_BOOL);
                 }
 
             switch (r.m_infixRelationName.m_token.m_id)
                 {
                 case TozeTokenizer.TOKEN_MEM:
                 case TozeTokenizer.TOKEN_NEM:
                     if (!t2.isSet())
                         {
                         reportTypeError("Right side of " + getName() + " must be a set",
                                         r.m_infixRelationName.m_token,
                                         specObject,
                                         property
                         );
                         }
                     if (!t1.isEqual(t2.m_setType))
                         {
                         reportTypeError("Element is not the same type as the set for " + getName(),
                                         r.m_infixRelationName.m_token,
                                         specObject,
                                         property
                         );
                         }
                     break;
                 case TozeTokenizer.TOKEN_PSUBS:
                 case TozeTokenizer.TOKEN_SUBS:
                     if (!t1.isEqual(t2))
                         {
                         reportTypeError("Both sides of " + getName() + " must be sets of the same type",
                                         r.m_infixRelationName.m_token,
                                         specObject,
                                         property
                         );
                         }
                     break;
                 case TozeTokenizer.TOKEN_EQUAL:
                 case TozeTokenizer.TOKEN_NEQ:
                     if (!t1.isCompatible(t2))
                         {
                         reportTypeError("Both sides of " + getName() + " must be the same type",
                                         r.m_infixRelationName.m_token,
                                         specObject,
                                         property
                         );
                         }
                     break;
                 case TozeTokenizer.TOKEN_LESSTHAN:
                 case TozeTokenizer.TOKEN_LEQ:
                 case TozeTokenizer.TOKEN_GEQ:
                 case TozeTokenizer.TOKEN_GREATERTHAN:
                     if ((!t1.isANumber() || !t2.isANumber()))
                         {
                         reportTypeError("Both sides of " + getName() + " must be numbers",
                                         r.m_infixRelationName.m_token,
                                         specObject,
                                         property
                         );
                         }
                     break;
                 case TozeTokenizer.TOKEN_PREFIX:
                 case TozeTokenizer.TOKEN_SUFFIX:
                 case TozeTokenizer.TOKEN_INSEQ:
                     if (!t2.isSequence())
                         {
                         reportTypeError("Right side of " + getName() + " must be a sequence",
                                         r.m_infixRelationName.m_token,
                                         specObject,
                                         property
                         );
                         }
                     if (!t1.isSequence())
                         {
                         reportTypeError("Left side of " + getName() + " must be a sequence",
                                         r.m_infixRelationName.m_token,
                                         specObject,
                                         property
                         );
                         }
                     if (!t1.isEqual(t2))
                         {
                         reportTypeError("Both sequences of " + getName() + " must be of the same type",
                                         r.m_infixRelationName.m_token,
                                         specObject,
                                         property
                         );
                         }
                     return new AstType(AstType.TYPE_BOOL);
                 case TozeTokenizer.TOKEN_BAGMEMBERSHIP:
                 case TozeTokenizer.TOKEN_SQUAREIMAGEOREQUAL:
                 case TozeTokenizer.TOKEN_PARTITIONS:
                     return new AstType();
                 default:
                     return new AstType();
                 }
 
             return new AstType(AstType.TYPE_BOOL);
         }
     }
 
     public class AstRelation extends AstBase
     {
         AstIdentifier m_identifier;
         AstInfixRelationName m_infixRelationName;
 
         public void print(int l)
         {
             if (m_infixRelationName != null)
                 {
                 m_infixRelationName.print(l);
                 }
             if (m_identifier != null)
                 {
                 m_identifier.print(l);
                 }
         }
     }
 
     public class AstPrefixRelationNameP extends AstPredicate
     {
         AstPrefixRelationName m_prefixRelationName;
         AstExpression m_expression;
 
         public AstPrefixRelationNameP(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             m_expression.populateTypeTable(m_stable);
         }
 
         public void populateSymbolTable()
         {
             m_expression.populateSymbolTable();
         }
 
         public void checkType()
         {
             AstType type = m_expression.getType();
             if (!type.isSet())
                 {
                 reportTypeError("Expression for " + m_prefixRelationName.getName() + " must evaluate to a set",
                                 m_expression.m_token,
                                 specObject,
                                 property
                 );
                 }
         }
 
         public AstType getType()
         {
             checkType();
             return new AstType(AstType.TYPE_BOOL);
         }
     }
 
     public class AstSchemaReferenceP extends AstPredicate
     {
         AstSchemaReference m_schemaReference;
 
         public AstSchemaReferenceP(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
     }
 
     public class AstPreP extends AstPredicate
     {
         AstSchemaReference m_schemaReference;
 
         public AstPreP(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
     }
 
     public class AstInitP extends AstPredicate
     {
         AstExpression m_expression;
 
         public AstInitP(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             m_expression.populateTypeTable(m_stable);
         }
 
         public void populateSymbolTable()
         {
             m_expression.populateSymbolTable();
         }
 
         public AstType getType()
         {
             m_expression.getType();
             return new AstType(AstType.TYPE_BOOL);
         }
     }
 
     public class AstTrueFalseP extends AstPredicate
     {
         public AstTrueFalseP(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public AstType getType()
         {
             return new AstType(AstType.TYPE_BOOL);
         }
 
         public void print(int l)
         {
             print(l, m_token.m_value);
         }
     }
 
     public class AstNotP extends AstPredicate
     {
         AstPredicate m_predicate;
 
         public AstNotP(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             m_predicate.populateTypeTable(m_stable);
         }
 
         public void populateSymbolTable()
         {
             m_predicate.populateSymbolTable();
         }
 
         public AstType getType()
         {
             m_predicate.getType();
             return new AstType(AstType.TYPE_BOOL);
         }
     }
 
     public class AstParenP extends AstPredicate
     {
         AstPredicate m_predicate;
 
         public AstParenP(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             m_predicate.populateTypeTable(m_stable);
         }
 
         public void populateSymbolTable()
         {
             m_predicate.populateSymbolTable();
         }
 
         public AstType getType()
         {
             m_predicate.getType();
             return new AstType(AstType.TYPE_BOOL);
         }
     }
 
     /*
      * Schema Expressions
      */
     public class AstSchemaExpression extends AstBase
     {
     }
 
     public class AstForAllS extends AstSchemaExpression
     {
         AstSchemaText m_schemaText;
         AstSchemaExpression m_schemaExpression;
 
         public void print(int l)
         {
             print(l, "ForAll SchemaExpression");
             m_schemaText.print(l + 1);
             m_schemaExpression.print(l + 1);
         }
     }
 
     public class AstThereExistsS extends AstSchemaExpression
     {
         AstSchemaText m_schemaText;
         AstSchemaExpression m_schemaExpression;
 
         public void print(int l)
         {
             print(l, "ThereExists SchemaExpression");
             m_schemaText.print(l + 1);
             m_schemaExpression.print(l + 1);
         }
     }
 
     public class AstThereExists1S extends AstSchemaExpression
     {
         AstSchemaText m_schemaText;
         AstSchemaExpression m_schemaExpression;
 
         public void print(int l)
         {
             print(l, "ThereExists1 SchemaExpression");
             m_schemaText.print(l + 1);
             m_schemaExpression.print(l + 1);
         }
     }
 
     public class AstSchemaTextS extends AstSchemaExpression
     {
         AstSchemaText m_schemaText;
 
         public void print(int l)
         {
             print(l, "SchemaText SchemaExpression");
             m_schemaText.print(l + 1);
         }
     }
 
     public class AstSchemaReferenceS extends AstSchemaExpression
     {
         AstSchemaReference m_schemaReference;
 
         public void print(int l)
         {
             print(l, "SchemaReference SchemaExpression");
             m_schemaReference.print(l + 1);
         }
     }
 
     public class AstNotS extends AstSchemaExpression
     {
         AstSchemaExpression m_schemaExpression;
 
         public void print(int l)
         {
             print(l, "Not SchemaExpression");
             m_schemaExpression.print(l + 1);
         }
     }
 
     public class AstPreS extends AstSchemaExpression
     {
         AstSchemaExpression m_schemaExpression;
 
         public void print(int l)
         {
             print(l, "pre SchemaExpression");
             m_schemaExpression.print(l + 1);
         }
     }
 
     public class AstAndS extends AstSchemaExpression
     {
         AstSchemaExpression m_schemaExpressionL;
         AstSchemaExpression m_schemaExpressionR;
 
         public void print(int l)
         {
             print(l, "AND SchemaExpression");
             m_schemaExpressionL.print(l + 1);
             m_schemaExpressionR.print(l + 1);
         }
     }
 
     public class AstOrS extends AstSchemaExpression
     {
         AstSchemaExpression m_schemaExpressionL;
         AstSchemaExpression m_schemaExpressionR;
 
         public void print(int l)
         {
             print(l, "OR SchemaExpression");
             m_schemaExpressionL.print(l + 1);
             m_schemaExpressionR.print(l + 1);
         }
     }
 
     public class AstImpliesS extends AstSchemaExpression
     {
         AstSchemaExpression m_schemaExpressionL;
         AstSchemaExpression m_schemaExpressionR;
 
         public void print(int l)
         {
             print(l, "IMPLIES SchemaExpression");
             m_schemaExpressionL.print(l + 1);
             m_schemaExpressionR.print(l + 1);
         }
     }
 
     public class AstBiimpliesS extends AstSchemaExpression
     {
         AstSchemaExpression m_schemaExpressionL;
         AstSchemaExpression m_schemaExpressionR;
 
         public void print(int l)
         {
             print(l, "BIIMPLIES SchemaExpression");
             m_schemaExpressionL.print(l + 1);
             m_schemaExpressionR.print(l + 1);
         }
     }
 
     public class AstProjS extends AstSchemaExpression
     {
         AstSchemaExpression m_schemaExpressionL;
         AstSchemaExpression m_schemaExpressionR;
 
         public void print(int l)
         {
             print(l, "PROJ SchemaExpression");
             m_schemaExpressionL.print(l + 1);
             m_schemaExpressionR.print(l + 1);
         }
     }
 
     public class AstBslashS extends AstSchemaExpression
     {
         AstSchemaExpression m_schemaExpression;
         AstDeclarationNameList m_declarationNameList;
 
         public void print(int l)
         {
             print(l, "BSLASH SchemaExpression");
             m_schemaExpression.print(l + 1);
             m_declarationNameList.print(l + 1);
         }
     }
 
     public class AstCompS extends AstSchemaExpression
     {
         AstSchemaExpression m_schemaExpressionL;
         AstSchemaExpression m_schemaExpressionR;
 
         public void print(int l)
         {
             print(l, "COMP SchemaExpression");
             m_schemaExpressionL.print(l + 1);
             m_schemaExpressionR.print(l + 1);
         }
     }
 
     public class AstMgtS extends AstSchemaExpression
     {
         AstSchemaExpression m_schemaExpressionL;
         AstSchemaExpression m_schemaExpressionR;
 
         public void print(int l)
         {
             print(l, ">> SchemaExpression");
             m_schemaExpressionL.print(l + 1);
             m_schemaExpressionR.print(l + 1);
         }
     }
 
     public class AstParenS extends AstSchemaExpression
     {
         AstSchemaExpression m_schemaExpression;
 
         public void print(int l)
         {
             print(l, "() SchemaExpression");
             m_schemaExpression.print(l + 1);
         }
     }
 
     /*
      * Expression
      */
     public class AstExpression extends AstBase
     {
         SpecObject specObject;
         String property;
 
         public AstExpression(SpecObject specObject, String property)
         {
             this.specObject = specObject;
             this.property = property;
         }
 
         public void checkType()
         {
             getType();
         }
     }
 
     public class AstBinaryOpX extends AstExpression
     {
         AstExpression m_expressionL;
         AstExpression m_expressionR;
 
         public AstBinaryOpX(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public void print(int l)
         {
             m_expressionL.print(l);
             m_expressionR.print(l);
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             m_expressionL.populateTypeTable(m_stable);
             m_expressionR.populateTypeTable(m_stable);
         }
 
         public void populateSymbolTable()
         {
             m_expressionL.populateSymbolTable();
             m_expressionR.populateSymbolTable();
         }
 
         public AstType getType()
         {
             AstType typeL = m_expressionL.getType();
             AstType typeR = m_expressionR.getType();
             if ((typeL.isUndefined())
                     || (typeR.isUndefined()))
                 {
                 return new AstType();
                 }
             if (!typeL.isCompatible(typeR))
                 {
                 reportTypeError("Type mismatch",
                                 m_token,
                                 specObject,
                                 property
                 );
                 }
             return typeL.resultantType(typeR);
         }
     }
 
     public class AstIfThenElseX extends AstExpression
     {
         AstPredicate m_predicate;
         AstExpression m_then;
         AstExpression m_else;
 
         public AstIfThenElseX(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public void print(int l)
         {
             print(l, "IfThenElse");
             m_predicate.print(l + 1);
             m_then.print(l + 1);
             m_else.print(l + 1);
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             m_predicate.populateTypeTable(m_stable);
         }
 
         public void populateSymbolTable()
         {
             m_predicate.populateSymbolTable();
             m_then.populateSymbolTable();
             m_else.populateSymbolTable();
         }
 
         public AstType getType()
         {
             AstType t1 = m_then.getType();
             AstType t2 = m_else.getType();
             if ((t1.isUndefined())
                     || (t2.isUndefined()))
                 {
                 return new AstType();
                 }
             if (!t1.isCompatible(t2))
                 {
                 reportTypeError(
                         "The expression used for the then clause is a different type than the one used for the else clause",
                         m_then.m_token,
                         specObject,
                         property
                 );
                 }
             return t1.resultantType(t2);
         }
     }
 
     public class AstCrossProductX extends AstBinaryOpX
     {
         public AstCrossProductX(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public void print(int l)
         {
             print(l, "CrossProduct");
             super.print(l + 1);
         }
 
         public AstType getType()
         {
             AstType typeL = m_expressionL.getType();
             AstType typeR = m_expressionR.getType();
             if ((typeL.isUndefined())
                     || (typeR.isUndefined()))
                 {
                 return new AstType();
                 }
             typeL = typeL.getSetType();
             typeR = typeR.getSetType();
             if ((typeL == null) || (typeR == null))
                 {
                 reportTypeError("Both expressions of a cross-product must evaluate to a set",
                                 m_token,
                                 specObject,
                                 property
                 );
                 return new AstType();
                 }
             AstType result = new AstType(AstType.TYPE_SET);
             result.m_setType = new AstType(AstType.TYPE_TUPLE);
             result.m_setType.newTuple(typeL, typeR);
             return result;
         }
     }
 
     public class AstInfixGenericNameX extends AstBinaryOpX
     {
         AstInfixGenericName m_infixGenericName = new AstInfixGenericName();
 
         public AstInfixGenericNameX(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public void print(int l)
         {
             m_infixGenericName.print(l);
             super.print(l + 1);
         }
 
         public String getName()
         {
             return m_infixGenericName.getName();
         }
 
         public AstType getType()
         {
             AstType typeL = m_expressionL.getType();
             AstType typeR = m_expressionR.getType();
             if ((typeL.isUndefined())
                     || (typeR.isUndefined()))
                 {
                 return new AstType();
                 }
             if ((!typeL.isSet() || !typeR.isSet()))
                 {
                 reportTypeError("Both sides of an infix generic operator must be sets",
                                 m_infixGenericName.m_token,
                                 specObject,
                                 property
                 );
                 return new AstType();
                 }
             AstType type = new AstType(AstType.TYPE_SET);
             type.m_setType = new AstType(AstType.TYPE_SET);
             type.m_setType.m_setType = new AstType(AstType.TYPE_TUPLE);
             type.m_setType.m_setType.newTuple(typeL.getSetType(), typeR.getSetType());
             return type;
         }
     }
 
     public class AstInfixFunctionNameX extends AstBinaryOpX
     {
         AstInfixFunctionName m_infixFunctionName = new AstInfixFunctionName();
 
         public AstInfixFunctionNameX(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public void print(int l)
         {
             m_infixFunctionName.print(l);
             super.print(l + 1);
         }
 
         public AstType getType()
         {
             boolean bad = false;
 
             int tid = m_infixFunctionName.m_token.m_id;
             String tname = m_infixFunctionName.m_token.m_value;
 
             AstType typeL = m_expressionL.getType();
             AstType typeR = m_expressionR.getType();
 
             if ((typeL.isUndefined())
                     || (typeR.isUndefined()))
                 {
                 return new AstType();
                 }
 
             if (tid == TozeTokenizer.TOKEN_MAP)
                 {
                 AstType type = new AstType(AstType.TYPE_TUPLE);
                 type.m_tupleTypes.add(typeL);
                 type.m_tupleTypes.add(typeR);
                 return type;
                 }
             if ((tid == TozeTokenizer.TOKEN_PLUS)
                     || (tid == TozeTokenizer.TOKEN_MINUS)
                     || (tid == TozeTokenizer.TOKEN_DIV)
                     || (tid == TozeTokenizer.TOKEN_MOD)
                     || (tid == TozeTokenizer.TOKEN_FSLASH)
                     || (tid == TozeTokenizer.TOKEN_TIMES))
                 {
                 AstType rtype = typeL.resultantType(typeR);
                 if (!rtype.isANumber())
                     {
                     reportTypeError("The expressions for the " + tname + " operator must be numbers",
                                     m_infixFunctionName.m_token,
                                     specObject,
                                     property
                     );
                     return new AstType();
                     }
                 return rtype;
                 }
 
             if ((tid == TozeTokenizer.TOKEN_UNI)
                     || (tid == TozeTokenizer.TOKEN_SETMINUS)
                     || (tid == TozeTokenizer.TOKEN_INT)
                     || (tid == TozeTokenizer.TOKEN_BSLASH))
                 {
                 if (!typeL.isSet() || !typeR.isSet())
                     {
                     reportTypeError("Expressions for the " + tname + " operator must be sets",
                                     m_infixFunctionName.m_token,
                                     specObject,
                                     property
                     );
                     return new AstType();
                     }
                 if (!typeL.isEqual(typeR))
                     {
                     reportTypeError("Sets must be the same type for the operator " + tname,
                                     m_infixFunctionName.m_token,
                                     specObject,
                                     property
                     );
                     return new AstType();
                     }
                 return typeL;
                 }
 
             if ((tid == TozeTokenizer.TOKEN_CAT)
                     || (tid == TozeTokenizer.TOKEN_DCAT)
                     || (tid == TozeTokenizer.TOKEN_PREFIX)
                     || (tid == TozeTokenizer.TOKEN_SUFFIX))
                 {
                 if (!typeL.isSequence() || !typeR.isSequence())
                     {
                     reportTypeError("Expressions for the " + tname + " operator must be sequences",
                                     m_infixFunctionName.m_token,
                                     specObject,
                                     property
                     );
                     return new AstType();
                     }
                 return typeL;
                 }
 
             if (tid == TozeTokenizer.TOKEN_UPTO)
                 {
                 if (!typeL.isANumber()
                         || !typeR.isANumber())
                     {
                     reportTypeError("Expressions for " + tname + " must be numbers",
                                     m_infixFunctionName.m_token,
                                     specObject,
                                     property
                     );
                     return new AstType();
                     }
                 AstType type = new AstType(AstType.TYPE_SET);
                 type.m_setType = typeL;
                 return type;
                 }
 
             if ((tid == TozeTokenizer.TOKEN_FOVR)
                     || (tid == TozeTokenizer.TOKEN_FCMP)
                     || (tid == TozeTokenizer.TOKEN_CIRC))
                 {
                 if (!typeL.isRelation()
                         || !typeR.isRelation())
                     {
                     reportTypeError("Expressions for " + tname + " must be relations",
                                     m_infixFunctionName.m_token,
                                     specObject,
                                     property
                     );
                     return new AstType();
                     }
                 if (!typeL.isEqual(typeR))
                     {
                     reportTypeError("Relations used in " + tname + " must be of the same type",
                                     m_infixFunctionName.m_token,
                                     specObject,
                                     property
                     );
                     return new AstType();
                     }
                 return typeL;
                 }
 
             if ((tid == TozeTokenizer.TOKEN_DRES)
                     || (tid == TozeTokenizer.TOKEN_DSUB))
                 {
                 if (!typeL.isSet())
                     {
                     reportTypeError("Left-hand argument to " + tname + " must be a set",
                                     m_infixFunctionName.m_token,
                                     specObject,
                                     property
                     );
                     bad = true;
                     }
                 if (!typeR.isRelation())
                     {
                     reportTypeError("Right-hand argument to " + tname + " must be a relation",
                                     m_infixFunctionName.m_token,
                                     specObject,
                                     property
                     );
                     bad = true;
                     }
                 if (bad)
                     {
                     return new AstType();
                     }
                 if (!((AstType) typeR.getSetType().m_tupleTypes.get(0)).isEqual(typeL.getSetType()))
                     {
                     reportTypeError("Type of set must be the same as the domain type of the range",
                                     m_infixFunctionName.m_token,
                                     specObject,
                                     property
                     );
                     return new AstType();
                     }
                 return typeR;
                 }
 
             if ((tid == TozeTokenizer.TOKEN_RRES)
                     || (tid == TozeTokenizer.TOKEN_RSUB))
                 {
                 if (!typeR.isSet())
                     {
                     reportTypeError("Right-hand argument to " + tname + " must be a set",
                                     m_infixFunctionName.m_token,
                                     specObject,
                                     property
                     );
                     bad = true;
                     }
                 if (!typeL.isRelation())
                     {
                     reportTypeError("Left-hand argument to " + tname + " must be a relation",
                                     m_infixFunctionName.m_token,
                                     specObject,
                                     property
                     );
                     bad = true;
                     }
                 if (bad)
                     {
                     return new AstType();
                     }
                 if (!((AstType) typeL.getSetType().m_tupleTypes.get(1)).isEqual(typeR.getSetType()))
                     {
                     reportTypeError("Type of set must be the same as the range type of the relation",
                                     m_infixFunctionName.m_token,
                                     specObject,
                                     property
                     );
                     return new AstType();
                     }
                 return typeL;
                 }
 
             if ((tid == TozeTokenizer.TOKEN_PROJECT))
                 {
                 if (!typeL.isSequence())
                     {
                     reportTypeError("Left-hand expression for a projection must be a sequence",
                                     m_infixFunctionName.m_token,
                                     specObject,
                                     property
                     );
                     bad = true;
                     }
                 if (!typeR.isSet())
                     {
                     reportTypeError("Right-hand expression for a projection must be a set",
                                     m_infixFunctionName.m_token,
                                     specObject,
                                     property
                     );
                     bad = true;
                     }
                 if (bad)
                     {
                     return new AstType();
                     }
                 AstType tl = (AstType) typeL.getSetType().m_tupleTypes.get(1);
                 AstType tr = typeR.getSetType();
                 if (!tl.isCompatible(tr))
                     {
                     reportTypeError("Type of set must be the same as the sequence type.",
                                     m_infixFunctionName.m_token,
                                     specObject,
                                     property
                     );
                     }
                 return typeL;
                 }
 
             if ((tid == TozeTokenizer.TOKEN_EXTRACT))
                 {
                 if (!typeL.isSet())
                     {
                     reportTypeError("Left-hand expression must be a set",
                                     m_infixFunctionName.m_token,
                                     specObject,
                                     property
                     );
                     bad = true;
                     }
                 else
                     {
                     }
                 if (!typeR.isSequence())
                     {
                     reportTypeError("Right-hand expression for a projection must be a sequence",
                                     m_infixFunctionName.m_token,
                                     specObject,
                                     property
                     );
                     bad = true;
                     }
                 if (bad)
                     {
                     return new AstType();
                     }
                 AstType tl = typeL.getSetType();
             /*
              * Check that the set contains natural numbers.
              */
                 if (!tl.isANumber())
                     {
                     reportTypeError("The set must contain numbers",
                                     m_infixFunctionName.m_token,
                                     specObject,
                                     property
                     );
                     }
                 return typeR;
                 }
 
             if (tid == TozeTokenizer.TOKEN_HASH)
                 {
                 if (!typeL.isBag())
                     {
                     reportTypeError("Left-hand expression for '#' must be a bag",
                                     m_infixFunctionName.m_token,
                                     specObject,
                                     property
                     );
                     return new AstType();
                     }
                 if (!((AstType) typeL.getSetType().m_tupleTypes.get(0)).isEqual(typeR))
                     {
                     reportTypeError("Right-hand expression for '#' must be the same type as contained in the bag",
                                     m_infixFunctionName.m_token,
                                     specObject,
                                     property
                     );
                     return new AstType();
                     }
                 return new AstType(AstType.TYPE_NATURAL);
                 }
 
             if (tid == TozeTokenizer.TOKEN_UPLUS)
                 {
                 if (!typeL.isBag()
                         || !typeR.isBag())
                     {
                     reportTypeError("Expressions for bag union must be bags",
                                     m_infixFunctionName.m_token,
                                     specObject,
                                     property
                     );
                     return new AstType();
                     }
 
                 if (!typeL.isEqual(typeR))
                     {
                     reportTypeError("Both sides of bag union must be the same type",
                                     m_infixFunctionName.m_token,
                                     specObject,
                                     property
                     );
                     }
                 }
 
             return new AstType();
         }
     }
 
     public class AstPowerX extends AstExpression
     {
         AstExpression m_expression;
 
         public AstPowerX(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             m_expression.populateTypeTable(m_stable);
         }
 
         public void populateSymbolTable()
         {
             m_expression.populateSymbolTable();
         }
 
         public AstType getType()
         {
             AstType ttype = m_expression.getType();
             if (ttype.isUndefined())
                 {
                 return new AstType();
                 }
             if (ttype.getSetType() == null)
                 {
                 reportTypeError("Power set must be applied to a set",
                                 m_token,
                                 specObject,
                                 property
                 );
                 return new AstType();
                 }
             AstType type = new AstType(AstType.TYPE_SET);
             type.m_setType = ttype;
             return type;
         }
     }
 
     public class AstUnaryOpX extends AstExpression
     {
         AstExpression m_expression;
 
         public AstUnaryOpX(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public void print(int l)
         {
             print(l, m_token.m_value);
             m_expression.print(l + 1);
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             m_expression.populateTypeTable(stable);
         }
 
         public void populateSymbolTable()
         {
             m_expression.populateSymbolTable();
         }
 
         public AstType getType()
         {
             return m_expression.getType();
         }
     }
 
     public class AstPrefixGenericNameX extends AstUnaryOpX
     {
         AstPrefixGenericName m_prefixGenericName = new AstPrefixGenericName();
 
         public AstPrefixGenericNameX(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public void print(int l)
         {
             m_prefixGenericName.print(l);
         }
 
         public AstType getType()
         {
             switch (m_prefixGenericName.m_token.m_id)
                 {
                 case TozeTokenizer.TOKEN_SEQ:
                 case TozeTokenizer.TOKEN_SEQONE:
                 case TozeTokenizer.TOKEN_ISEQ:
                     AstType t = m_expression.getType();
                     if (t.isUndefined())
                         {
                         return new AstType();
                         }
 //               AstType type = new AstType(AstType.TYPE_SET);
 //               type.m_setType = new AstType(AstType.TYPE_SEQUENCE);
                     if (t.isSet())
                         {
                         t = t.getSetType();
                         }
                     AstType rtype = new AstType(AstType.TYPE_SET);
                     rtype.m_setType = new AstType(AstType.TYPE_SEQUENCE);
                     rtype.m_setType.m_setType = new AstType(AstType.TYPE_TUPLE);
                     rtype.m_setType.m_setType.newTuple(new AstType(AstType.TYPE_NATURAL1),
                                                        t
                     );
                     rtype.m_setType.m_setType.m_tupleIsSeq = true;
                     return rtype;
                 case TozeTokenizer.TOKEN_BAG:
                     t = m_expression.getType();
                     if (t.isUndefined())
                         {
                         return new AstType();
                         }
 //               type = new AstType(AstType.TYPE_SET);
 //               type.m_setType = new AstType(AstType.TYPE_BAG);
                     if (t.isSet())
                         {
                         t = t.getSetType();
                         }
                     rtype = new AstType(AstType.TYPE_SET);
                     rtype.m_setType = new AstType(AstType.TYPE_BAG);
                     rtype.m_setType.m_setType = new AstType(AstType.TYPE_TUPLE);
                     rtype.m_setType.m_setType.newTuple(t,
                                                        new AstType(AstType.TYPE_NATURAL1)
                     );
                     rtype.m_setType.m_setType.m_tupleIsSeq = true;
                     return rtype;
                 }
             return new AstType();
         }
     }
 
     public class AstHyphenX extends AstUnaryOpX
     {
         AstDecorations m_decoration;
 
         public AstHyphenX(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             m_expression.populateTypeTable(m_stable);
         }
 
         public void populateSymbolTable()
         {
             m_expression.populateSymbolTable();
         }
 
         public AstType getType()
         {
             AstType t = m_expression.getType();
             if (!t.isANumber())
                 {
                 reportTypeError("The expression for - must be a number",
                                 m_token,
                                 specObject,
                                 property
                 );
                 return new AstType();
                 }
             return t;
         }
     }
 
     public class AstImageX extends AstExpression // (| |)
     {
         AstExpression m_expression1;
         AstExpression m_expression0;
         AstDecorations m_decoration;
 
         public AstImageX(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             m_expression1.populateTypeTable(m_stable);
             m_expression0.populateTypeTable(m_stable);
         }
 
         public void populateSymbolTable()
         {
             m_expression1.populateSymbolTable();
             m_expression0.populateSymbolTable();
         }
 
         public AstType getType()
         {
             boolean bad = false;
             AstType typeL = m_expression1.getType();
             AstType typeM = m_expression0.getType();
 
             if ((typeL.isUndefined())
                     || (typeM.isUndefined()))
                 {
                 return new AstType();
                 }
 
             if (!typeL.isRelation())
                 {
                 reportTypeError("Left-hand expression to the image operator must be a relation",
                                 m_token,
                                 specObject,
                                 property
                 );
                 bad = true;
                 }
             if (!typeM.isSet())
                 {
                 reportTypeError("Inside expression of the image operator must be a set",
                                 m_token,
                                 specObject,
                                 property
                 );
                 bad = true;
                 }
             if (bad)
                 {
                 return new AstType();
                 }
             if (!typeM.getSetType().isEqual((AstType) typeL.getSetType().m_tupleTypes.get(0)))
                 {
                 reportTypeError(
                         "The type of the inside set expression must the the same type as the domain type of the relation for the image operator",
                         m_token,
                         specObject,
                         property
                 );
                 return new AstType();
                 }
             AstType type = new AstType(AstType.TYPE_SET);
             type.m_setType = (AstType) typeL.getSetType().m_tupleTypes.get(1);
             return type;
         }
     }
 
     public class AstExpressionListX extends AstExpression
     {
         List m_expressions = new ArrayList();
 
         public AstExpressionListX(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             int i;
             for (i = 0; i < m_expressions.size(); i++)
                 {
                 ((AstBase) m_expressions.get(i)).populateTypeTable(m_stable);
                 }
         }
 
         public void populateSymbolTable()
         {
             int i;
             for (i = 0; i < m_expressions.size(); i++)
                 {
                 ((AstBase) m_expressions.get(i)).populateSymbolTable();
                 }
         }
 
         public AstType getType()
         {
             AstType type = new AstType(AstType.TYPE_TUPLE);
             int i;
             for (i = 0; i < m_expressions.size(); i++)
                 {
                 AstType t = ((AstBase) m_expressions.get(i)).getType();
                 type.m_tupleTypes.add(t);
                 }
 
             /*
              * Special case where a tuple is surrounded by parenthesis
              */
 
             if (m_expressions.size() == 1)
                 {
                 AstType t = (AstType) type.m_tupleTypes.get(0);
                 if (t.getType() == AstType.TYPE_TUPLE)
                     {
                     return t;
                     }
                 }
 
             return type;
         }
     }
 
     public class AstDistUnionX extends AstExpression
     {
         AstExpression m_expression = null;
 
         public AstDistUnionX(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             m_expression.populateTypeTable(m_stable);
         }
 
         public void populateSymbolTable()
         {
             m_expression.populateSymbolTable();
         }
 
         public AstType getType()
         {
             AstType type = m_expression.getType();
             if (type.isUndefined())
                 {
                 return new AstType();
                 }
             if (type.getType() != AstType.TYPE_SET)
                 {
                 reportTypeError("Expression used in distributed union must be a set of sets.",
                                 m_token,
                                 specObject,
                                 property
                 );
                 return new AstType();
                 }
 
             /*
              * If the set is a tuple, then we need to make sure that each tuple
              * is of the same type.
              */
 
             if (type.getSetType().getType() == AstType.TYPE_TUPLE)
                 {
                 int i;
                 AstType stype = type.getSetType();
                 if (stype.m_tupleTypes.size() == 0)
                     {
                     return new AstType(AstType.TYPE_EMPTY);
                     }
                 AstType type1 = (AstType) stype.m_tupleTypes.get(0);
                 for (i = 1; i < stype.m_tupleTypes.size(); i++)
                     {
                     AstType type2 = (AstType) stype.m_tupleTypes.get(i);
                     if (!type1.isCompatible(type2))
                         {
                         reportTypeError("All sets used in a distributed union must be of the same type.",
                                         m_token,
                                         specObject,
                                         property
                         );
                         return new AstType();
                         }
                     }
                 return type1;
                 }
             else
                 {
                 if (type.getSetType().getType() != AstType.TYPE_SET)
                     {
                     reportTypeError("Expression used in distributed union must be a set of sets.",
                                     m_token,
                                     specObject,
                                     property
                     );
                     return new AstType();
                     }
 
                 return type.getSetType();
                 }
         }
     }
 
     public class AstDistIntersectionX extends AstExpression
     {
         AstExpression m_expression = null;
 
         public AstDistIntersectionX(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             m_expression.populateTypeTable(m_stable);
         }
 
         public void populateSymbolTable()
         {
             m_expression.populateSymbolTable();
         }
 
         public AstType getType()
         {
             AstType type = m_expression.getType();
             if (type.isUndefined())
                 {
                 return new AstType();
                 }
             if (type.getType() != AstType.TYPE_SET)
                 {
                 reportTypeError("Expression used in distributed intersection must be a set of sets.",
                                 m_token,
                                 specObject,
                                 property
                 );
                 return new AstType();
                 }
 
             /*
              * If the set is a tuple, then we need to make sure that each tuple
              * is of the same type.
              */
 
             if (type.getSetType().getType() == AstType.TYPE_TUPLE)
                 {
                 int i;
                 AstType stype = type.getSetType();
                 if (stype.m_tupleTypes.size() == 0)
                     {
                     return new AstType(AstType.TYPE_EMPTY);
                     }
                 AstType type1 = (AstType) stype.m_tupleTypes.get(0);
                 for (i = 1; i < stype.m_tupleTypes.size(); i++)
                     {
                     AstType type2 = (AstType) stype.m_tupleTypes.get(i);
                     if (!type1.isCompatible(type2))
                         {
                         reportTypeError("All sets used in a distributed intersection must be of the same type.",
                                         m_token,
                                         specObject,
                                         property
                         );
                         return new AstType();
                         }
                     }
                 return type1;
                 }
             else
                 {
                 if (type.getSetType().getType() != AstType.TYPE_SET)
                     {
                     reportTypeError("Expression used in distributed intersection must be a set of sets.",
                                     m_token,
                                     specObject,
                                     property
                     );
                     return new AstType();
                     }
 
                 return type.getSetType();
                 }
         }
     }
 
     public class AstVariableX extends AstExpression
     {
         AstVariableName m_variable;
         AstActualParameters m_actualParameters;
 
         public AstVariableX(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
     }
 
     public class AstSelfX extends AstExpression
     {
         public AstSelfX(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
     }
 
     public class AstNumberX extends AstExpression
     {
         public AstNumberX(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public void print(int l)
         {
             print(l, m_token.m_value);
         }
 
         public AstType getType()
         {
             AstType type = new AstType();
             if (m_token.m_value.indexOf('.') >= 0)
                 {
                 type.m_type = AstType.TYPE_REAL;
                 }
             else
                 {
                 type.m_type = AstType.TYPE_NATURAL;
                 }
             return type;
         }
     }
 
     public class AstSchemaReferenceX extends AstExpression
     {
         AstSchemaReference m_schemaReference;
 
         public AstSchemaReferenceX(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
     }
 
     public class AstClassNameX extends AstExpression
     {
         AstClassName m_className;
         AstActualParameters m_actualParameters;
         AstRenameList m_renameList;
 
         public AstClassNameX(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
     }
 
     public class AstDownArrowX extends AstExpression
     {
         AstExpression m_expression;
 
         public AstDownArrowX(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             m_expression.populateTypeTable(m_stable);
         }
 
         public void populateSymbolTable()
         {
             m_expression.populateSymbolTable();
         }
 
         public AstType getType()
         {
             if (m_expression == null)
                 {
                 return new AstType();
                 }
             return m_expression.getType();
         }
     }
 
     public class AstUnionX extends AstBinaryOpX
     {
         public void print(int l)
         {
             print(l, "Union");
             super.print(l + 1);
         }
 
         public AstUnionX(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public AstType getType()
         {
             AstType typeL = m_expressionL.getType();
             AstType typeR = m_expressionR.getType();
 
             if ((typeL.isUndefined())
                     || (typeR.isUndefined()))
                 {
                 return new AstType();
                 }
 
             if (!typeL.isSet() || !typeR.isSet())
                 {
                 reportTypeError(
                         "Expressions for the " + Character.toString(TozeFontMap.CHAR_CUP) + " operator must be sets",
                         m_token,
                         specObject,
                         property
                 );
                 return new AstType();
                 }
             if (!typeL.isEqual(typeR))
                 {
                 reportTypeError(
                         "Sets must be the same type for the operator " + Character.toString(TozeFontMap.CHAR_CUP),
                         m_token,
                         specObject,
                         property
                 );
                 return new AstType();
                 }
             return typeL;
         }
     }
 
     public class AstCopyrightX extends AstUnaryOpX
     {
         public AstCopyrightX(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
     }
 
     public class AstAngleX extends AstExpression
     {
         List m_expressions = new ArrayList();
 
         public AstAngleX(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
     }
 
     public class AstBagX extends AstExpression
     {
         List m_expressions = new ArrayList();
 
         public AstBagX(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public void print(int l)
         {
             int i;
             print(l, "BagExpression");
             for (i = 0; i < m_expressions.size(); i++)
                 {
                 ((AstExpression) m_expressions.get(i)).print(l + 1);
                 }
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             int i;
             for (i = 0; i < m_expressions.size(); i++)
                 {
                 ((AstBase) m_expressions.get(i)).populateTypeTable(m_stable);
                 }
         }
 
         public void populateSymbolTable()
         {
             int i;
             for (i = 0; i < m_expressions.size(); i++)
                 {
                 ((AstBase) m_expressions.get(i)).populateSymbolTable();
                 }
         }
 
         public AstType getType()
         {
             int i;
             boolean wasUndefined = false;
             AstType type = new AstType(AstType.TYPE_BAG);
             if (m_expressions.size() == 0)
                 {
                 return new AstType(AstType.TYPE_EMPTY);
                 }
 
             AstType stype = ((AstBase) m_expressions.get(0)).getType();
             if (stype.isUndefined())
                 {
                 return new AstType();
                 }
 
             for (i = 1; i < m_expressions.size(); i++)
                 {
                 AstType ttype = ((AstBase) m_expressions.get(i)).getType();
                 if (ttype.isUndefined())
                     {
                     wasUndefined = true;
                     }
                 else
                     {
                     if (!ttype.isEqual(stype))
                         {
                         reportTypeError("All expressions of a bag must be of the same type",
                                         m_token,
                                         specObject,
                                         property
                         );
                         }
                     }
                 }
 
             if (wasUndefined)
                 {
                 return new AstType();
                 }
 
             AstType rtype = new AstType(AstType.TYPE_BAG);
             rtype.m_setType = new AstType(AstType.TYPE_TUPLE);
             rtype.m_setType.newTuple(stype,
                                      new AstType(AstType.TYPE_NATURAL1)
             );
             rtype.m_setType.m_tupleIsSeq = false;
             return rtype;
         }
     }
 
     public class AstThetaX extends AstExpression
     {
         AstSchemaName m_schemaName;
         AstDecorations m_decorations;
         AstRenameList m_renameList;
 
         public AstThetaX(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
     }
 
     public class AstMemberX extends AstExpression
     {
         AstExpression m_expression;
         AstVariableName m_variableName;
 
         public AstMemberX(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public void print(int l)
         {
             print(l, "AstMember");
             m_expression.print(l + 1);
             m_variableName.print(l + 1);
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             m_expression.populateTypeTable(m_stable);
         }
 
         public void populateSymbolTable()
         {
             m_expression.populateSymbolTable();
         }
 
         public AstType getType()
         {
             AstType ctype = m_expression.getType();
             if (ctype.isUndefined())
                 {
                 return new AstType();
                 }
             if (ctype == null)
                 {
                 reportTypeError("Undefined class variable",
                                 m_expression.m_token,
                                 specObject,
                                 property
                 );
                 return new AstType();
                 }
             if (ctype.getType() != AstType.TYPE_CLASS)
                 {
                 reportTypeError("Member access must be from a class type",
                                 m_expression.m_token,
                                 specObject,
                                 property
                 );
                 return new AstType();
                 }
            AstType type = null;

            if (m_variableName != null)
                {
                type = ctype.getClassMembers().getTypeVisible(m_variableName.getName());
                }

             if (type == null)
                 {
                // the variable name may be null after a class.member syntax
                // where member is blank, handle as a null and use a unique
                // TozeToken
                String member = "<null>";
                TozeToken token = null;

                if (m_variableName != null)
                    {
                    member = m_variableName.getName();
                    token = m_variableName.m_token;
                    }

                if (token == null)
                    {
                    token = new TozeToken(-1, member, -1, -1);
                    }

                reportTypeError("The member " + member + " is not visible",
                                token,
                                 specObject,
                                 property
                 );
                 return new AstType();
                 }
             if (type.isUndefined())
                 {
                 reportTypeError("The member " + m_variableName.getName() + " is undefined",
                                 m_variableName.m_token,
                                 specObject,
                                 property
                 );
                 return new AstType();
                 }
             return type;
         }
     }
 
     public class AstPostfixFunctionNameX extends AstUnaryOpX
     {
         AstPostfixFunctionName m_postfixFunctionName;
 
         public AstPostfixFunctionNameX(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public AstType getType()
         {
             AstType type = m_expression.getType();
 
             if (type.isUndefined())
                 {
                 return new AstType();
                 }
 
             if ((m_postfixFunctionName.m_token.m_id == TozeTokenizer.TOKEN_TILDE)
                     || (m_postfixFunctionName.m_token.m_id == TozeTokenizer.TOKEN_INV))
                 {
                 if (!type.isRelation())
                     {
                     reportTypeError("The argument to '~' must be a relation",
                                     m_postfixFunctionName.m_token,
                                     specObject,
                                     property
                     );
                     return new AstType();
                     }
 
                 AstType rtype = new AstType(AstType.TYPE_SET);
                 rtype.m_setType = new AstType(AstType.TYPE_TUPLE);
                 rtype.m_setType.newTuple((AstType) type.m_setType.m_tupleTypes.get(1),
                                          (AstType) type.m_setType.m_tupleTypes.get(0)
                 );
                 return rtype;
                 }
             if ((m_postfixFunctionName.m_token.m_id == TozeTokenizer.TOKEN_TCL)
                     || (m_postfixFunctionName.m_token.m_id == TozeTokenizer.TOKEN_RTCL))
                 {
                 if (!type.isRelation())
                     {
                     reportTypeError(
                             "The argument to '" + m_postfixFunctionName.m_token.m_value + "' must be a relation",
                             m_postfixFunctionName.m_token,
                             specObject,
                             property
                     );
                     return new AstType();
                     }
                 }
             return type;
         }
     }
 
     public class AstParenX extends AstExpression
     {
         public AstParenX(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public void print(int l)
         {
             print(l, "Parenthesis");
             super.print(l + 1);
         }
     }
 
     public class AstLambdaX extends AstExpression
     {
         AstSchemaText m_schemaText;
         AstExpression m_expression;
 
         public AstLambdaX(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public void print(int l)
         {
             print(l, "Lambda");
             m_schemaText.print(l + 1);
             m_expression.print(l + 1);
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             m_schemaText.populateTypeTable(m_stable);
             m_expression.populateTypeTable(m_stable);
         }
 
         public void populateSymbolTable()
         {
             m_schemaText.populateSymbolTable();
             m_expression.populateSymbolTable();
         }
 
         public AstType getType()
         {
             m_schemaText.checkType();
             List decls = m_schemaText.m_declaration.m_decls;
             AstType ttype = new AstType(AstType.TYPE_TUPLE);
             int i;
             for (i = 0; i < decls.size(); i++)
                 {
                 if (decls.get(i) instanceof AstBasicDeclaration1)
                     {
                     AstBasicDeclaration1 dec = (AstBasicDeclaration1) decls.get(i);
                     List names = dec.m_declarationNameList.m_declarationNameList;
                     int j;
                     for (j = 0; j < names.size(); j++)
                         {
                         AstDeclarationName name = (AstDeclarationName) names.get(j);
                         AstType nt = m_stable.getType(name.getName());
                         ttype.m_tupleTypes.add(nt);
                         }
                     }
                 }
             AstType et = m_expression.getType();
             if (et.isUndefined())
                 {
                 return new AstType();
                 }
             ttype.m_tupleTypes.add(et);
             AstType type = new AstType(AstType.TYPE_SET);
             type.m_setType = ttype;
             return type;
         }
     }
 
     public class AstMuX extends AstExpression
     {
         AstSchemaText m_schemaText;
         AstExpression m_expression;
 
         public AstMuX(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
     }
 
     public class AstLetX extends AstExpression
     {
         List m_letDefinitions = new ArrayList();
         AstExpression m_expression;
 
         public AstLetX(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
     }
 
     /*
      * This class is a merging of a variable (V), Schema Reference (S), and
      * class (C).
      * This is done because sometimes it is not possible to know at the time of
      * parsing whether something is either of the three.
      *
      * Word -> VSC
      * Word, Decoration -> VS
      * Word, ActualParameters -> VSC
      * Word, RenameList -> SC
      * Word, ActualParameters, RenameList -> SC
      * Word, Decoration, ActualParameters -> VS
      * Word, Decoration, RenameList -> S
      * Word, Decoration, ActualParameters, RenameList -> S
      */
     public class AstVSCX extends AstExpression
     {
         // The Word will be stored in the token in the base class.
         AstDecorations m_decorations;
         AstActualParameters m_actualParameters;
         AstRenameList m_renameList;
 
         public AstVSCX(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public void print(int l)
         {
             print(l, "VSC");
             print(l + 1, m_token.m_value);
             if (m_decorations != null)
                 {
                 m_decorations.print(l + 1);
                 }
             if (m_actualParameters != null)
                 {
                 m_actualParameters.print(l + 1);
                 }
             if (m_renameList != null)
                 {
                 m_renameList.print(l + 1);
                 }
         }
 
         public AstType getType()
         {
             AstType type = m_stable.getTypeDef(getName());
             if (type == null)
                 {
                 type = m_stable.getType(getName());
                 if (type == null)
                     {
                     reportTypeError(getName() + " is undefined",
                                     m_token,
                                     specObject,
                                     property
                     );
                     return new AstType();
                     }
                 if (type.isUndefined())
                     {
                     reportTypeError(getName() + " is undefined",
                                     m_token,
                                     specObject,
                                     property
                     );
                     return new AstType();
                     }
                 }
             return type;
         }
 
         public String getName()
         {
             if (m_decorations == null)
                 {
                 return m_token.m_value;
                 }
             return m_token.m_value + m_decorations.getName();
         }
     }
 
     public class AstSetExpressionX extends AstExpression
     {
         public AstSetExpressionX(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
     }
 
     public class AstSetExpressionX1 extends AstSetExpressionX
     {
         List m_expressions = new ArrayList();
 
         public AstSetExpressionX1(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public void print(int l)
         {
             int i;
             print(l, "SetExpression");
             for (i = 0; i < m_expressions.size(); i++)
                 {
                 ((AstExpression) m_expressions.get(i)).print(l + 1);
                 }
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             int i;
             for (i = 0; i < m_expressions.size(); i++)
                 {
                 ((AstBase) m_expressions.get(i)).populateTypeTable(m_stable);
                 }
         }
 
         public void populateSymbolTable()
         {
             int i;
             for (i = 0; i < m_expressions.size(); i++)
                 {
                 ((AstBase) m_expressions.get(i)).populateSymbolTable();
                 }
         }
 
         public AstType getType()
         {
             AstType type;
             int i;
             boolean wasUndefined = false;
 //ttp
 /*
              * if (m_expressions.size() > 1)
              * {
              * AstType ttype = new AstType();
              * ttype.m_type = AstType.TYPE_TUPLE;
              * for (i=0; i<m_expressions.size(); i++)
              * {
              * type = ((AstBase)m_expressions.get(i)).getType();
              * ttype.m_tupleTypes.add(type);
              * if (type.isUndefined()) wasUndefined = true;
              * }
              * if (wasUndefined) return new AstType();
              * type = new AstType(AstType.TYPE_SET);
              * type.m_setType = ttype;
              * }
              * else if (m_expressions.size() == 1)
              */
             if (m_expressions.size() != 0)
                 {
                 type = new AstType(AstType.TYPE_SET);
                 type.m_setType = ((AstBase) m_expressions.get(0)).getType();
                 if (type.m_setType.isUndefined())
                     {
                     return new AstType();
                     }
                 }
             else
                 {
                 type = new AstType(AstType.TYPE_SET);
                 type.m_setType = new AstType(AstType.TYPE_EMPTY);
                 if (type.m_setType.isUndefined())
                     {
                     return new AstType();
                     }
                 }
             return type;
         }
     }
 
     public class AstSetExpressionX2 extends AstSetExpressionX
     {
         AstSchemaText m_schemaText;
         AstExpression m_expression;
 
         public AstSetExpressionX2(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public void print(int l)
         {
             print(l, "SetExpression");
             if (m_schemaText != null)
                 {
                 m_schemaText.print(l + 1);
                 }
             m_expression.print(l + 1);
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = new AstSymbolTable(stable);
             stable.addKid(m_stable);
             if (m_schemaText != null)
                 {
                 m_schemaText.populateTypeTable(m_stable);
                 }
             if (m_expression != null)
                 {
                 m_expression.populateTypeTable(m_stable);
                 }
         }
 
         public void populateSymbolTable()
         {
             if (m_schemaText != null)
                 {
                 m_schemaText.populateSymbolTable();
                 }
             if (m_expression != null)
                 {
                 m_expression.populateSymbolTable();
                 }
         }
 
         public AstType getType()
         {
             if (m_schemaText != null)
                 {
                 m_schemaText.checkType();
                 }
             if (m_expression == null)
                 {
                 return m_schemaText.getType();
                 }
             AstType type = new AstType(AstType.TYPE_SET);
             type.m_setType = m_expression.getType();
             if (type.m_setType.isUndefined())
                 {
                 return new AstType();
                 }
             return type;
         }
     }
 
     public class AstSetExpressionX3 extends AstSetExpressionX
     {
         public AstSetExpressionX3(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public void print(int l)
         {
             print(l, "SetExpression - " + m_token.m_value);
         }
 
         public AstType getType()
         {
             AstType type = new AstType();
             if (m_token.m_id == TozeTokenizer.TOKEN_NAT)
                 {
                 return m_stable.getTypeDef(m_token.m_value);
                 }
             else if (m_token.m_id == TozeTokenizer.TOKEN_NATONE)
                 {
                 return m_stable.getTypeDef(m_token.m_value);
                 }
             else if (m_token.m_id == TozeTokenizer.TOKEN_INTEGER)
                 {
                 return m_stable.getTypeDef(m_token.m_value);
                 }
             else if (m_token.m_id == TozeTokenizer.TOKEN_REAL)
                 {
                 return m_stable.getTypeDef(m_token.m_value);
                 }
             else if (m_token.m_id == TozeTokenizer.TOKEN_BOOL)
                 {
                 return m_stable.getTypeDef(m_token.m_value);
                 }
             return m_stable.getTypeDef(m_token.m_value);
         }
     }
 
     public class AstSequenceX extends AstExpression
     {
         List m_expressions = new ArrayList();
 
         public AstSequenceX(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public void print(int l)
         {
             print(l, "Sequence");
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             int i;
             for (i = 0; i < m_expressions.size(); i++)
                 {
                 ((AstBase) m_expressions.get(i)).populateTypeTable(m_stable);
                 }
         }
 
         public void populateSymbolTable()
         {
             int i;
             for (i = 0; i < m_expressions.size(); i++)
                 {
                 ((AstBase) m_expressions.get(i)).populateSymbolTable();
                 }
         }
 
         public AstType getType()
         {
             int i;
             boolean wasUndefined = false;
             AstType type = new AstType(AstType.TYPE_SEQUENCE);
             if (m_expressions.size() == 0)
                 {
                 type.m_setType = new AstType(AstType.TYPE_EMPTY);
                 return type;
                 }
 
             AstType stype = ((AstBase) m_expressions.get(0)).getType();
             if (stype.isUndefined())
                 {
                 return new AstType();
                 }
 
             for (i = 1; i < m_expressions.size(); i++)
                 {
                 AstType ttype = ((AstBase) m_expressions.get(i)).getType();
                 if (ttype.isUndefined())
                     {
                     wasUndefined = true;
                     }
                 else
                     {
                     if (!ttype.isEqual(stype))
                         {
                         reportTypeError("All expressions of a sequence must be of the same type",
                                         m_token,
                                         specObject,
                                         property
                         );
                         }
                     }
                 }
 
             if (wasUndefined)
                 {
                 return new AstType();
                 }
 
             AstType rtype = new AstType(AstType.TYPE_SEQUENCE);
             rtype.m_setType = new AstType(AstType.TYPE_TUPLE);
             rtype.m_setType.newTuple(new AstType(AstType.TYPE_NATURAL1),
                                      stype
             );
             rtype.m_setType.m_tupleIsSeq = true;
             return rtype;
         }
     }
 
     public class AstBuiltInFunctionX extends AstUnaryOpX
     {
         public AstBuiltInFunctionX(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public AstType getType()
         {
             AstType type = m_expression.getType();
 
             if (type.isUndefined())
                 {
                 return new AstType();
                 }
 
             if (m_token.m_id == TozeTokenizer.TOKEN_DOM)
                 {
                 if (type.getType() != AstType.TYPE_SET)
                     {
                     reportTypeError("The argument to the function 'dom' must be a set of tuples",
                                     m_token,
                                     specObject,
                                     property
                     );
                     return new AstType();
                     }
                 if (type.getSetType().getType() != AstType.TYPE_TUPLE)
                     {
                     reportTypeError("The argument to the function 'dom' must be a set of tuples",
                                     m_token,
                                     specObject, property
                     );
                     return new AstType();
                     }
                 if (type.getSetType().m_tupleTypes.size() != 2)
                     {
                     reportTypeError("The argument to the function 'dom' must be a tuple of size 2",
                                     m_token,
                                     specObject,
                                     property
                     );
                     return new AstType();
                     }
                 AstType rtype = new AstType(AstType.TYPE_SET);
                 rtype.m_setType = (AstType) type.getSetType().m_tupleTypes.get(0);
                 return rtype;
                 }
 
             if (m_token.m_id == TozeTokenizer.TOKEN_RAN)
                 {
                 if (type.getType() != AstType.TYPE_SET)
                     {
                     reportTypeError("The argument to the function 'ran' must be a set of tuples",
                                     m_token,
                                     specObject,
                                     property
                     );
                     return new AstType();
                     }
                 if (type.getSetType().getType() != AstType.TYPE_TUPLE)
                     {
                     reportTypeError("The argument to the function 'ran' must be a set of tuples",
                                     m_token,
                                     specObject,
                                     property
                     );
                     return new AstType();
                     }
                 if (type.getSetType().m_tupleTypes.size() != 2)
                     {
                     reportTypeError("The argument to the function 'ran' must be a tuple of size 2",
                                     m_token,
                                     specObject,
                                     property
                     );
                     return new AstType();
                     }
                 AstType rtype = new AstType(AstType.TYPE_SET);
                 rtype.m_setType = (AstType) type.getSetType().m_tupleTypes.get(1);
                 return rtype;
                 }
 
             if (m_token.m_id == TozeTokenizer.TOKEN_REV)
                 {
                 if (type.getType() != AstType.TYPE_SEQUENCE)
                     {
                     reportTypeError("The argument to the function 'rev' must be a sequence",
                                     m_token,
                                     specObject,
                                     property
                     );
                     return new AstType();
                     }
                 return type;
                 }
 
             if (m_token.m_id == TozeTokenizer.TOKEN_HEAD)
                 {
                 if (type.getType() != AstType.TYPE_SEQUENCE)
                     {
                     reportTypeError("The argument to the function 'head' must be a sequence",
                                     m_token,
                                     specObject,
                                     property
                     );
                     return new AstType();
                     }
                 return (AstType) type.getSetType().m_tupleTypes.get(1);
 //            return type.m_setType;
                 }
 
             if (m_token.m_id == TozeTokenizer.TOKEN_LAST)
                 {
                 if (type.getType() != AstType.TYPE_SEQUENCE)
                     {
                     reportTypeError("The argument to the function 'last' must be a sequence",
                                     m_token,
                                     specObject,
                                     property
                     );
                     return new AstType();
                     }
                 return (AstType) type.getSetType().m_tupleTypes.get(1);
 //            return type.m_setType;
                 }
 
             if (m_token.m_id == TozeTokenizer.TOKEN_TAIL)
                 {
                 if (type.getType() != AstType.TYPE_SEQUENCE)
                     {
                     reportTypeError("The argument to the function 'tail' must be a sequence",
                                     m_token,
                                     specObject,
                                     property
                     );
                     return new AstType();
                     }
                 return type;
                 }
 
             if (m_token.m_id == TozeTokenizer.TOKEN_FRONT)
                 {
                 if (type.getType() != AstType.TYPE_SEQUENCE)
                     {
                     reportTypeError("The argument to the function 'front' must be a sequence",
                                     m_token,
                                     specObject,
                                     property
                     );
                     return new AstType();
                     }
                 return type;
                 }
 
             if ((m_token.m_id == TozeTokenizer.TOKEN_FIRST)
                     || (m_token.m_id == TozeTokenizer.TOKEN_SECOND))
                 {
                 if ((type.getType() != AstType.TYPE_TUPLE)
                         || (type.m_tupleTypes.size() != 2))
                     {
                     reportTypeError("The argument to " + m_token.m_value + " must be a tuple of size 2",
                                     m_token,
                                     specObject,
                                     property
                     );
                     return new AstType();
                     }
                 if (m_token.m_id == TozeTokenizer.TOKEN_FIRST)
                     {
                     return (AstType) type.m_tupleTypes.get(0);
                     }
                 return (AstType) type.m_tupleTypes.get(1);
                 }
 
             if (m_token.m_id == TozeTokenizer.TOKEN_HASH)
                 {
                 if (!type.isSet() && !type.isSequence())
                     {
                     reportTypeError("Argument to '#' must be either a set or a sequence",
                                     m_token,
                                     specObject,
                                     property
                     );
                     }
                 return new AstType(AstType.TYPE_NATURAL);
                 }
 
             return new AstType();
         }
     }
 
     public class AstFunctionX extends AstExpression
     {
         AstExpression m_fexpression;
         AstExpression m_pexpression;
 
         public AstFunctionX(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             m_fexpression.populateTypeTable(m_stable);
             m_pexpression.populateTypeTable(m_stable);
         }
 
         public void populateSymbolTable()
         {
             m_fexpression.populateSymbolTable();
             m_pexpression.populateSymbolTable();
         }
 
         public AstType getType()
         {
             AstType ftype = m_fexpression.getType();
             AstType ptype = m_pexpression.getType();
 
             if ((ftype.isUndefined())
                     || (ptype.isUndefined()))
                 {
                 return new AstType();
                 }
 
             /*
              * Check the special case where it looks like an expression function
              * but it is really where a single element of a sequence is being
              * referenced.
              */
 
             if (ftype.getType() == AstType.TYPE_SEQUENCE)
                 {
                 if (!ptype.isANumber())
                     {
                     reportTypeError("Subscript to a sequence must be a number",
                                     m_token,
                                     specObject,
                                     property
                     );
                     }
                 return (AstType) ftype.getSetType().m_tupleTypes.get(1);
                 }
 
             if ((ftype.getType() != AstType.TYPE_SET)
                     && (ftype.getType() != AstType.TYPE_SEQUENCE))
                 {
                 reportTypeError("Expression used as a function must evaluate to a set of a tuple",
                                 m_token,
                                 specObject,
                                 property
                 );
                 return new AstType();
                 }
             if (ftype.getSetType().getType() != AstType.TYPE_TUPLE)
                 {
                 reportTypeError("Expression used as a function must evaluate to a tuple",
                                 m_token,
                                 specObject,
                                 property
                 );
                 return new AstType();
                 }
             if (ftype.getSetType().m_tupleTypes.size() < 2)
                 {
                 reportTypeError("The tuple must be a binary relation",
                                 m_token,
                                 specObject,
                                 property
                 );
                 return new AstType();
                 }
 
             //if (ptype.getType() == AstType.TYPE_TUPLE)
             if (ptype.isTuple())
                 {
                 if ((ptype.m_tupleTypes.size() + 1) != ftype.m_setType.m_tupleTypes.size())
                     {
                     reportTypeError("Number of parameters for the function do not match definition.",
                                     m_token,
                                     specObject,
                                     property
                     );
                     }
                 int i;
                 for (i = 0; i < ptype.m_tupleTypes.size(); i++)
                     {
                     AstType t1 = (AstType) ptype.m_tupleTypes.get(i);
                     AstType t2 = (AstType) ftype.m_setType.m_tupleTypes.get(i);
                     if (!t1.isCompatible(t2))
                         {
                         reportTypeError("Parameter " + (i + 1) + " does not match definition.",
                                         m_token,
                                         specObject,
                                         property
                         );
                         }
                     }
                 }
             else
                 {
                 if (!ptype.isEqual((AstType) ftype.getSetType().m_tupleTypes.get(0)))
                     {
                     reportTypeError("Parameter types do not match function parameters",
                                     m_token,
                                     specObject,
                                     property
                     );
                     }
                 }
 
             List vtmp = ftype.getSetType().m_tupleTypes;
             return (AstType) vtmp.get(vtmp.size() - 1);
         }
     }
 
     public class AstPredicateX extends AstExpression
     {
         AstPredicate m_predicate;
 
         public AstPredicateX(SpecObject specObject, String property)
         {
             super(specObject, property);
         }
 
         public void populateTypeTable(AstSymbolTable stable)
         {
             m_stable = stable;
             m_predicate.populateTypeTable(stable);
         }
 
         public void populateSymbolTable()
         {
             m_predicate.populateSymbolTable();
         }
 
         public AstType getType()
         {
             boolean bad = false;
             return new AstType(AstType.TYPE_BOOL);
         }
     }
 
     /*
      * Infix, Postfix, Prefix
      */
     public class AstPrefixGenericName extends AstBase
     {
         AstDecorations m_decoration;
 
         public void print(int l)
         {
             print(l, "PrefixGenericName = " + m_token.m_value);
             if (m_decoration != null)
                 {
                 m_decoration.print(l + 1);
                 }
         }
     }
 
     public class AstInfixGenericName extends AstBase
     {
         AstDecorations m_decoration;
 
         public void print(int l)
         {
             print(l, "InfixGenericName = " + getName());
             if (m_decoration != null)
                 {
                 m_decoration.print(l + 1);
                 }
         }
 
         public String getName()
         {
             return m_token.m_value;
         }
     }
 
     public class AstInfixFunctionName extends AstBase
     {
         AstDecorations m_decoration;
 
         public void print(int l)
         {
             print(l, m_token.m_value);
             if (m_decoration != null)
                 {
                 m_decoration.print(l + 1);
                 }
         }
     }
 
     public class AstInfixRelationName extends AstBase
     {
         AstDecorations m_decoration;
 
         public void print(int l)
         {
             print(l, "InfixRelationName = " + m_token.m_value);
             if (m_decoration != null)
                 {
                 m_decoration.print(l + 1);
                 }
         }
     }
 
     public class AstPrefixRelationName extends AstBase
     {
         public void print(int l)
         {
             print(l, "PrefixRelationName = " + getName());
         }
 
         public String getName()
         {
             return m_token.m_value;
         }
     }
 
     public class AstPostfixFunctionName extends AstBase
     {
         AstDecorations m_decoration;
 
         public void print(int l)
         {
             print(l, "PrefixFunctionName = " + m_token.m_value);
             m_decoration.print(l + 1);
         }
     }
 }
