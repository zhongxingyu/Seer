 package minijava.typechecker.implementation;
 
 import java.util.HashSet;
 import java.util.Iterator;
 
 import minijava.ast.*;
 import minijava.typechecker.ErrorReport;
 import minijava.typechecker.TypeChecked;
 import minijava.util.FunTable;
 import minijava.util.FunTable.Entry;
 import minijava.visitor.Visitor;
 
 public class TypeCheckVisitor implements Visitor<TypeChecked>
 {
   private FunTable<Info>  table;
   private ErrorReport     error;
   
   private String          currentClass,
                           currentMethod;
   
   public TypeCheckVisitor(FunTable<Info> table, ErrorReport error)
   {
     this.table = table;
     this.error = error;
   }
   
   @Override
   public <T extends AST> TypeChecked visit(NodeList<T> ns)
   {
     // Iterate all nodes and perform type checking
     for(int i = 0; i < ns.size(); ++i) { ns.elementAt(i).accept(this); }
     
     return new TypeCheckedImplementation(null);
   }
 
   @Override
   public TypeChecked visit(Program p)
   {
     // Type check MainClass and other classes
     p.mainClass.accept(this);
     p.classes.accept(this);
     
     // Reset context
     this.currentClass = null;
     this.currentMethod = null;
     
     return new TypeCheckedImplementation(null);
   }
 
   @Override
   public TypeChecked visit(MainClass c)
   {
     // Set main context
     this.currentClass   = c.className;
     this.currentMethod  = "main";
     
     // Type check statements
     return c.statement.accept(this);
   }
 
   @Override
   public TypeChecked visit(ClassDecl d)
   {
     ClassInfo info                = this.lookupClassInfo(d.name);
     boolean hasSuperclass         = (d.superName != null),
             isValidSuperclass     = true,
             isValidClass          = (info != null),
             isValidMethodOverride = true,
             isUniqueClass         = true;
     
     if(hasSuperclass)
     {
       // Check superclass type
       ClassInfo superInfo = this.lookupClassInfo(d.superName);
       isValidSuperclass = (superInfo != null);
       
       // Check if there are fields that are already declared and inherited from 
       // the superclass
       if(isValidSuperclass)
       {
         // Iterate entire parent hierarchy
         ClassInfo currentInfo = superInfo;
         do
         {
           Iterator<Entry<Info>> it = info.fields.iterator();
           while(it.hasNext())
           {
             String id = it.next().getId();
             if(currentInfo.fields.lookup(id) != null)
             {
               // Method declared with same name by superclass
               this.error.fieldOverriding(new ObjectType(d.name), id);
               isValidMethodOverride = false;
             }
           }
           
           currentInfo = this.lookupClassInfo(currentInfo.superClass);
         }
         while(currentInfo != null);
       }
     }
     
     // Check for duplicate class declarations
     int counter = 0;
     Iterator<Entry<Info>> it = this.table.iterator();
     while(it.hasNext())
     {
       if(it.next().getId().equals(d.name)) { ++counter; }
     }
     isUniqueClass = (counter == 1);
     
     if( !isValidSuperclass ||
         !isValidClass ||
         !isValidMethodOverride ||
         !isUniqueClass)
     {
       // Report type errors
       if(!isValidSuperclass)  { this.error.undefinedId(d.superName); }
       if(!isValidClass)       { this.error.undefinedId(d.name); }
       if(!isUniqueClass)      { this.error.duplicateDefinition(d.name); }
       return null;
     }
     
     // Set class context
     this.currentClass = d.name;
     
     // Check duplicate field names
     HashSet<String> map = new HashSet<String>();
     for(int i = 0; i < d.vars.size(); ++i)
     {
       String fieldName = d.vars.elementAt(i).name;
       if(map.contains(fieldName))
       {
         this.error.duplicateDefinition(fieldName);
       }
       else
       {
         map.add(fieldName);
       }
     }
     
     // Check duplicate method names
     map = new HashSet<String>();
     for(int i = 0; i < d.methods.size(); ++i)
     {
       String methodName = d.methods.elementAt(i).name;
       if(map.contains(methodName))
       {
         this.error.duplicateDefinition(methodName);
       }
       else
       {
         map.add(methodName);
       }
     }
     
     d.methods.accept(this);
     d.vars.accept(this);
     
     // Reset class context
     this.currentClass = null;
     
     return new TypeCheckedImplementation(null);
   }
 
   @Override
   public TypeChecked visit(VarDecl n)
   {
     Type type = n.type;
     
     // Check identifier and declared object type (if necessary)
     ClassInfo c               = this.lookupClassInfo(this.currentClass);
     MethodInfo m              = (MethodInfo) c.methods.lookup(this.currentMethod);
     boolean isValidIdentifier = ((m != null &&
                                   ( m.locals.lookup(n.name) != null ||
                                     m.formals.lookup(n.name) != null)) ||
                                 c.fields.lookup(n.name) != null),
             isValidType = (!(type instanceof ObjectType) ||
                           this.lookupClassInfo(((ObjectType) type).name) != null);
     
     if(!isValidIdentifier || !isValidType)
     {
       // Report type errors
       if(!isValidIdentifier)  { this.error.undefinedId(n.name); }
       if(!isValidType)        { this.error.undefinedId(((ObjectType) type).name); }
       return null;
     }
     
     return new TypeCheckedImplementation(type);
   }
 
   @Override
   public TypeChecked visit(MethodDecl n)
   {
     Type type = n.returnType;
     
     // Check method identifier and object return type (if necessary)
     ClassInfo c = (ClassInfo) this.lookupClassInfo(this.currentClass);
     MethodInfo m = (MethodInfo) c.methods.lookup(n.name);
     boolean isValidMethod = (m != null),
             isValidType = (!(type instanceof ObjectType) ||
                           this.lookupClassInfo(((ObjectType) type).name) != null);
     
     if(!isValidMethod || !isValidType)
     {
       // Report type errors
       if(!isValidMethod)  { this.error.undefinedId(n.name); }
       if(!isValidType)    { this.error.undefinedId(((ObjectType) type).name); }
       return null;
     }
     
     // Set method context
     this.currentMethod = n.name;
     
     // Compare method with superclass' declaration (if it exists)
     if(c.superClass != null)
     {
       ClassInfo currentInfo = this.lookupClassInfo(c.superClass);
       while(currentInfo != null)
       {
         // Check for method in superclass
         MethodInfo superMethod = (MethodInfo) currentInfo.methods.lookup(this.currentMethod);
         if(superMethod != null)
         {
           // Compare method signatures
           // NOTE: This language does not support method overloading. Arguments 
           // length is expected to be the same.
           int length = m.formalsList.size();
           if(length != superMethod.formalsList.size())
           {
             this.error.badMethodOverriding(new ObjectType(this.currentClass), this.currentMethod);
             return null;
           }
           
           // Check that the method signature is exactly the same
           for(int i = 0; i < length; ++i)
           {
             if(!m.formalsList.get(i).type.equals(superMethod.formalsList.get(i).type))
             {
               this.error.badMethodOverriding(new ObjectType(this.currentClass), this.currentMethod);
               return null;
             }
           }
           
           // Check return type is the same
           if(!m.returnType.equals(superMethod.returnType))
           {
             this.error.badMethodOverriding(new ObjectType(this.currentClass), this.currentMethod);
             return null;
           }
         }
         
         currentInfo = this.lookupClassInfo(currentInfo.superClass);
       }
     }
     
     // Check for duplicate arguments
     // Add unique arguments to map
     HashSet<String> map = new HashSet<String>();
     for(int i = 0; i < n.formals.size(); ++i)
     {
       String formalName = n.formals.elementAt(i).name;
       if(map.contains(formalName))
       {
         this.error.duplicateDefinition(formalName);
       }
       else
       {
         map.add(formalName);
       }
     }
     
     // Check for duplicate local variables
     // Add unique local variables to map
     for(int i = 0; i < n.vars.size(); ++i)
     {
       String varName = n.vars.elementAt(i).name;
       if(map.contains(varName))
       {
         this.error.duplicateDefinition(varName);
       }
       else
       {
         map.add(varName);
       }
     }
     
     // Type check arguments, local variables, and statements
     n.formals.accept(this);
     n.vars.accept(this);
     n.statements.accept(this);
     
     TypeCheckedImplementation t = (TypeCheckedImplementation) n.returnExp.accept(this);
     
     // Reset method context
     this.currentMethod = null;
     
     // Check return type
     if(t != null && !t.type.equals(m.returnType))
     {
       this.error.typeError(n.returnExp, m.returnType, t.type);
     }
     
     return t;
   }
 
   @Override
   public TypeChecked visit(IntArrayType n)
   {
     return new TypeCheckedImplementation(n);
   }
 
   @Override
   public TypeChecked visit(BooleanType n)
   {
     return new TypeCheckedImplementation(n);
   }
 
   @Override
   public TypeChecked visit(IntegerType n)
   {
     return new TypeCheckedImplementation(n);
   }
 
   @Override
   public TypeChecked visit(ObjectType n)
   {
     return new TypeCheckedImplementation(n);
   }
 
   @Override
   public TypeChecked visit(Block b)
   {
     // Type check statements
     return b.statements.accept(this);
   }
 
   @Override
   public TypeChecked visit(If n)
   {
     TypeCheckedImplementation t = (TypeCheckedImplementation) n.tst.accept(this);
     if(t != null && !t.type.equals(new BooleanType()))
     {
       // Report type errors
       this.error.typeError(n.tst, new BooleanType(), t.type);
       return null;
     }
     
     // Type check then, and else statements
     n.thn.accept(this);
     n.els.accept(this);
     
     return t;
   }
 
   @Override
   public TypeChecked visit(While n)
   {
     TypeCheckedImplementation t = (TypeCheckedImplementation) n.tst.accept(this);
     if(t != null && !t.type.equals(new BooleanType()))
     {
       // Report type errors
       this.error.typeError(n.tst, new BooleanType(), t.type);
       return null;
     }
     
     // Type check body
     n.body.accept(this);
     
     return t;
   }
 
   @Override
   public TypeChecked visit(Print n)
   {
     TypeCheckedImplementation t = (TypeCheckedImplementation) n.exp.accept(this);
     if(t != null && !t.type.equals(new IntegerType()))
     {
       // Report type errors
       this.error.typeError(n.exp, new IntegerType(), t.type);
     }
     
     return t;
   }
 
   @Override
   public TypeChecked visit(Assign n)
   {
     // Type check value
     TypeCheckedImplementation t = (TypeCheckedImplementation) n.value.accept(this);
     if(t == null) { return null; }
     
     // Check for valid variable
     VarInfo v = this.lookupVarInfo(n.name);
     if(v == null) { return null; }
     
     if(!t.type.equals(v.type))
     {
       // Report type errors
       this.error.typeError(n.value, v.type, t.type);
       return null;
     }
     
     return t;
   }
 
   @Override
   public TypeChecked visit(ArrayAssign n)
   {
     // Check for valid variable
     VarInfo v = this.lookupVarInfo(n.name);
     if(v == null) { return null; }
     
     if(!v.type.equals(new IntArrayType()))
     {
      // Report type errors
       this.error.typeError(new IdentifierExp(n.name), new IntArrayType(), v.type);
       return null;
     }
     
     // Type check value and index
     TypeCheckedImplementation t1  = (TypeCheckedImplementation) n.value.accept(this),
                               t2  = (TypeCheckedImplementation) n.index.accept(this);
     boolean isValidTypeLeft       = t1.type.equals(new IntegerType()),
             isValidTypeRight      = t2.type.equals(new IntegerType());
     
     if(!isValidTypeLeft || !isValidTypeRight)
     {
       // Report type errors
       if(!isValidTypeLeft)  { this.error.typeError( n.value,
                                                     new IntegerType(),
                                                     t1.type); }
       if(!isValidTypeRight) { this.error.typeError( n.index,
                                                     new IntegerType(),
                                                     t2.type); }
       return null;
     }
     
     return new TypeCheckedImplementation(new IntegerType());
   }
 
   @Override
   public TypeChecked visit(And n)
   {
     // Type check left and right expressions
     TypeCheckedImplementation t1  = (TypeCheckedImplementation) n.e1.accept(this),
                               t2  = (TypeCheckedImplementation) n.e2.accept(this);
     boolean isValidTypeLeft       = t1.type.equals(new BooleanType()),
             isValidTypeRight      = t2.type.equals(new BooleanType());
     
     if(!isValidTypeLeft || !isValidTypeRight)
     {
       // Report type errors
       if(!isValidTypeLeft)  { this.error.typeError( n.e1,
                                                     new BooleanType(),
                                                     t1.type); }
       if(!isValidTypeRight) { this.error.typeError( n.e2,
                                                     new BooleanType(),
                                                     t2.type); }
       return null;
     }
     
     return t1;
   }
 
   @Override
   public TypeChecked visit(LessThan n)
   {
     // Type check left and right expressions
     TypeCheckedImplementation t1  = (TypeCheckedImplementation) n.e1.accept(this),
                               t2  = (TypeCheckedImplementation) n.e2.accept(this);
     boolean isValidTypeLeft       = t1.type.equals(new IntegerType()),
             isValidTypeRight      = t2.type.equals(new IntegerType());
     
     if(!isValidTypeLeft || !isValidTypeRight)
     {
       // Report type errors
       if(!isValidTypeLeft)  { this.error.typeError( n.e1,
                                                     new IntegerType(),
                                                     t1.type); }
       if(!isValidTypeRight) { this.error.typeError( n.e2,
                                                     new IntegerType(),
                                                     t2.type); }
       return null;
     }
     
     return new TypeCheckedImplementation(new BooleanType());
   }
 
   @Override
   public TypeChecked visit(Plus n)
   {
     // Type check left and right expressions
     TypeCheckedImplementation t1  = (TypeCheckedImplementation) n.e1.accept(this),
                               t2  = (TypeCheckedImplementation) n.e2.accept(this);
     boolean isValidTypeLeft       = (t1 == null || t1.type.equals(new IntegerType())),
             isValidTypeRight      = (t2 == null || t2.type.equals(new IntegerType()));
     
     if(!isValidTypeLeft || !isValidTypeRight)
     {
       // Report type errors
       if(!isValidTypeLeft)  { this.error.typeError( n.e1,
                                                     new IntegerType(),
                                                     t1.type); }
       if(!isValidTypeRight) { this.error.typeError( n.e2,
                                                     new IntegerType(),
                                                     t2.type); }
       return null;
     }
     
     return t1;
   }
 
   @Override
   public TypeChecked visit(Minus n)
   {
     // Type check left and right expressions
     TypeCheckedImplementation t1  = (TypeCheckedImplementation) n.e1.accept(this),
                               t2  = (TypeCheckedImplementation) n.e2.accept(this);
     boolean isValidTypeLeft       = t1.type.equals(new IntegerType()),
             isValidTypeRight      = t2.type.equals(new IntegerType());
     
     if(!isValidTypeLeft || !isValidTypeRight)
     {
       // Report type errors
       if(!isValidTypeLeft)  { this.error.typeError( n.e1,
                                                     new IntegerType(),
                                                     t1.type); }
       if(!isValidTypeRight) { this.error.typeError( n.e2,
                                                     new IntegerType(),
                                                     t2.type); }
       return null;
     }
     
     return t1;
   }
 
   @Override
   public TypeChecked visit(Times n)
   {
     // Type check left and right expressions
     TypeCheckedImplementation t1  = (TypeCheckedImplementation) n.e1.accept(this),
                               t2  = (TypeCheckedImplementation) n.e2.accept(this);
     boolean isValidTypeLeft       = t1.type.equals(new IntegerType()),
             isValidTypeRight      = t2.type.equals(new IntegerType());
     
     if(!isValidTypeLeft || !isValidTypeRight)
     {
       // Report type errors
       if(!isValidTypeLeft)  { this.error.typeError( n.e1,
                                                     new IntegerType(),
                                                     t1.type); }
       if(!isValidTypeRight) { this.error.typeError( n.e2,
                                                     new IntegerType(),
                                                     t2.type); }
       return null;
     }
     
     return t1;
   }
 
   @Override
   public TypeChecked visit(ArrayLookup n)
   {
     // Type check array and index
     TypeCheckedImplementation t1 = (TypeCheckedImplementation) n.array.accept(this),
                               t2 = (TypeCheckedImplementation) n.index.accept(this);
     
     // Report type errors
     if(!t1.type.equals(new IntArrayType()))
     {
       this.error.typeError(n.array, new IntArrayType(), t1.type);
       return null;
     }
     
     if(!t2.type.equals(new IntegerType()))
     {
       this.error.typeError(n.index, new IntegerType(), t2.type);
       return null;
     }
     
     return new TypeCheckedImplementation(new IntegerType());
   }
 
   @Override
   public TypeChecked visit(ArrayLength n)
   {
     // Type check array
     TypeCheckedImplementation t1 = (TypeCheckedImplementation) n.array.accept(this);
     if(!t1.type.equals(new IntArrayType()))
     {
       // Report type errors
       this.error.typeError(n.array, new IntArrayType(), t1.type);
       return null;
     }
     
     return new TypeCheckedImplementation(new IntegerType());
   }
 
   @Override
   public TypeChecked visit(Call n)
   {
     // Type check receiever
     TypeCheckedImplementation t = (TypeCheckedImplementation) n.receiver.accept(this);
     if(t == null) { return null; }
     
     // Check for valid class declaration
     if(!(t.type instanceof ObjectType))
     {
       this.error.typeErrorExpectObjectType(n.receiver, t.type);
       return null;
     }
     
     ObjectType objectType = (ObjectType) t.type;
     ClassInfo c = this.lookupClassInfo(objectType.name);
     if(c == null)
     {
       this.error.undefinedId(objectType.name);
       return null;
     }
     
     // Check for method declaration
     // Iterate entire parent hierarchy in case method is inherited
     MethodInfo m = null;
     ClassInfo currentInfo = c;
     do
     {
       m = (MethodInfo) currentInfo.methods.lookup(n.name);
       
       // Method found in class hierarchy
       if(m != null) { break; }
       
       currentInfo = this.lookupClassInfo(currentInfo.superClass);
     }
     while(currentInfo != null);
     
     // Check for valid method declaration
     if(m == null)
     {
       this.error.undefinedId(n.name);
       return null;
     }
     
     // Check argument count
     if(m.formalsList.size() != n.rands.size())
     {
       this.error.wrongNumberOfArguments(n, m.formalsList.size());
       return null;
     }
     
     // Check argument types
     for(int i = 0; i < m.formalsList.size(); ++i)
     {
       TypeCheckedImplementation randType = (TypeCheckedImplementation) n.rands.elementAt(i).accept(this);
       if(randType != null && !m.formalsList.get(i).type.equals(randType.type))
       {
         // Check class hierarchy only if both expected and actual types are ObjectTypes
         boolean argIsSubclass = false;
         if(m.formalsList.get(i).type instanceof ObjectType && randType.type instanceof ObjectType)
         {
           String expectedClassName = ((ObjectType) m.formalsList.get(i).type).name;
           String argClassName = ((ObjectType) randType.type).name;
           
           // Find the superclass name and iterate through parents
           ClassInfo info = this.lookupClassInfo(argClassName);
           while(info != null && info.superClass != null && !argIsSubclass)
           {
             if(expectedClassName.equals(info.superClass))
             {
               // The argument is a subclass of the expected class, so mark found
               // and don't error in the future
               argIsSubclass = true;
             }
             else
             {
               // Find the superclass and reiterate
               info = this.lookupClassInfo(info.superClass);
             }
           }
         }
         
         // Report type error if it doesn't match anything
         if(!argIsSubclass)
         {
           this.error.typeError(n.rands.elementAt(i), m.formalsList.get(i).type, randType.type);
         }
       }
     }
     
     return new TypeCheckedImplementation(m.returnType);
   }
 
   @Override
   public TypeChecked visit(IntegerLiteral n)
   {
     return new TypeCheckedImplementation(new IntegerType());
   }
 
   @Override
   public TypeChecked visit(BooleanLiteral n)
   {
     return new TypeCheckedImplementation(new BooleanType());
   }
 
   @Override
   public TypeChecked visit(IdentifierExp n)
   {
     // Check for valid variable
     VarInfo v = this.lookupVarInfo(n.name);
     if(v == null) { return null; }
     
     return new TypeCheckedImplementation(v.type);
   }
 
   @Override
   public TypeChecked visit(This n)
   {
     return new TypeCheckedImplementation(new ObjectType(this.currentClass));
   }
 
   @Override
   public TypeChecked visit(NewArray n)
   {
     return new TypeCheckedImplementation(new IntArrayType());
   }
 
   @Override
   public TypeChecked visit(NewObject n)
   {
     // Check for valid class declaration
     ClassInfo c = this.lookupClassInfo(n.typeName);
     if(c == null)
     {
       this.error.undefinedId(n.typeName);
       return null;
     }
     
     return new TypeCheckedImplementation(new ObjectType(n.typeName));
   }
 
   @Override
   public TypeChecked visit(Not n)
   {
     // Check for valid boolean expression
     TypeCheckedImplementation t = (TypeCheckedImplementation) n.e.accept(this);
     if(t != null && !t.type.equals(new BooleanType()))
     {
       this.error.typeError(n.e, new BooleanType(), t.type);
     }
     
     return t;
   }
   
   // Helper method for ClassInfo lookup in the symbol table
   private ClassInfo lookupClassInfo(String id)
   {
     return (ClassInfo) this.table.lookup(id);
   }
 
   // Helper method for VarInfo lookup in the symbol table using the current 
   // class and current method
   private VarInfo lookupVarInfo(String id)
   {
     // Obtain ClassInfo for current class
     ClassInfo c = this.lookupClassInfo(this.currentClass);
     
     // Obtain MethodInfo for current method in the current class
     MethodInfo m = (MethodInfo) c.methods.lookup(this.currentMethod);
     Info v = null;
     
     // Check for valid method declaration
     if(m != null)
     {
       // Attempt to locate identifier in the method's local varaibles
       v = m.locals.lookup(id);
       if(v == null)
       {
         // Attempt to locate identifier in the method's arguments
         v = m.formals.lookup(id);
       }
     }
     
     if(v == null)
     {
       // Check for identifier in class declaration
       // If not found, check if it is inherited
       // Iterate entire parent hierarchy
       do
       {
         // Attempt to locate identifier in class declaration
         v = c.fields.lookup(id);
         if(v != null)
         {
           // Identifier found in class hierarchy
           break;
         }
         
         c = this.lookupClassInfo(c.superClass);
       }
       while(c != null);
       
       if(v == null)
       {
         // Identifier could not be found
         this.error.undefinedId(id);
         return null;
       }
     }
     
     return (VarInfo) v;
   }
 }
