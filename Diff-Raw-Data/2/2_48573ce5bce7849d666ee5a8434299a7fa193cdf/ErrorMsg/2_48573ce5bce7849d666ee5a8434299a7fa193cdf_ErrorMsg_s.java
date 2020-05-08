 /////////////////////////////////////////////////////////////////
 // This static final singleton class provides string literals for
 // 131 programming assignment 1.  These literals are meant to be
 // used with the Formatter class, provided separately, which uses
 // C/C++ printf-like conventions.
 /////////////////////////////////////////////////////////////////
 
 class ErrorMsg
 {
 
 // Contains return at end to remind that header appears on its own line.
     public static final String errorHeader = "Error, \"%F\", line %D:\n";
 
 /////////////////////////////////////////////////////////////////
 // 131 - "Check 0"
 //
 // These messages are for errors for which there is no single
 // corresponding check in the spec or they are used in the Parser.
 /////////////////////////////////////////////////////////////////
 
     public static final String syntax_error =
         "syntax error near \"%S\".";
 
     public static final String undeclared_id =
         "undeclared identifier '%S'.";
 
     public static final String redeclared_id =
         "redeclared identifier '%S'.";
 
     public static final String not_function =
         "'%S' is not a function.";
 
     public static final String not_type =
         "'%S' is not a type.";
 
 
 /////////////////////////////////////////////////////////////////
 // 131 - PHASE I
 /////////////////////////////////////////////////////////////////
 
 // Check 0 //
     public static final String error0g_Scope  =
         "Identifier %S is undeclared in the global scope.";
 
 // Check 1 //
     public static final String error1n_Expr  =
         "Incompatible type %T to binary operator %O, numeric expected.";
 
     public static final String error1w_Expr  =
         "Incompatible type %T to binary operator %O, equivalent to %T expected.";
 
     public static final String error1u_Expr  =
         "Incompatible type %T to unary operator %O, equivalent to %T expected.";
 
     public static final String error1b_Expr  =
         "Incompatible types to operator: %T %O %T;\n  both must be numeric, or both equivalent to bool.";
 
 // Check 2 //
     public static final String error2_Type  =
         "Incompatible type %T to operator %O, equivalent to int, float, or pointer expected.";
 
     public static final String error2_Lval  =
         "Operand to %O is not a modifiable L-value.";
 
 // Check 3 //
 // NOTE!  You don't need to use the formatter with this message
     public static final String error3a_Assign =
         "Left-hand operand is not assignable (not a modifiable L-value).";
 
     public static final String error3b_Assign =
         "Value of type %T not assignable to variable of type %T.";
 
 // Check 4 //
     public static final String error4_Test   =
         "bool or int required for conditional test, %T found.";
 
 // Check 5 //
     public static final String error5n_Call  =
         "Number of arguments (%D) differs from number of parameters (%D).";
 
     public static final String error5a_Call  =
         "Argument of type %T not assignable to value parameter %S, of type %T.";
 
     public static final String error5r_Call  =
         "Argument of type %T not equivalent to reference parameter %S, of type %T.";
 
     public static final String error5c_Call  =
         "Argument passed to reference parameter %S (type %T) is not a modifiable L-value.";
 
 // Check 6 //
 // NOTE!  You don't need to use the formatter with this message
     public static final String error6a_Return_expr =
         "Return in function requires a result expression, none found.";
 
     public static final String error6a_Return_type =
         "Type of return expression (%T), not assignment compatible with function's return type (%T).";
 
     public static final String error6b_Return_equiv =
         "Type of return expression (%T) is not equivalent to the function's return type (%T).";
 
 // NOTE!  You don't need to use the formatter with this message
     public static final String error6b_Return_modlval =
         "Return expression is not a modifiable L-value for function that returns by reference.";
 
 // NOTE!  You don't need to use the formatter with this message
     public static final String error6c_Return_missing  =
         "Return required in function, none found.";
 
 // Check 7 //
     public static final String error7_Exit  =
         "Exit expression (type %T) is not assignable to int.";
 
 /////////////////////////////////////////////////////////////////
 // 131 - PHASE II
 /////////////////////////////////////////////////////////////////
 
 // Check 8 //
     public static final String error8_CompileTime  =
         "Initialization value of constant named %S not known at compile time.";
 
     public static final String error8_Assign  =
         "Initialization value of type %T not assignable to constant/variable of type %T.";
 
 // NOTE!  You don't need to use the formatter with this message
     public static final String error8_Arithmetic  =
         "Arithmetic exception occurred during constant folding.";
 
 // Check 9 //
 // No new messages for this check -- reuse appropriate messages from
 // earlier checks.
 
 // Check 10 //
     public static final String error10i_Array =
         "Index expression type (%T) in array declaration not equivalent to int.";
 
 // NOTE!  You don't need to use the formatter with this message
     public static final String error10c_Array =
         "Value of index expression not known at compile time.";
 
     public static final String error10z_Array =
         "Index expression value (%D) in array declaration must be > 0.";
 
 // Check 11 //
     public static final String error11t_ArrExp =
         "Type of expression referenced by array subscript (%T) is not of array or pointer type.";
 
     public static final String error11i_ArrExp =
         "Type of index expression in array reference (%T) not equivalent to int.";
 
 // NOTE: The notation '[' means up to and including, ')' means up to and NOT including
     public static final String error11b_ArrExp =
         "Index value of %D is outside legal range [0,%D).";
 
     public static final String error11_TooManyInitExpr =
         "Number of initializer expressions exceeds the array size.";
 
     public static final String error11_NonConstInitExpr =
         "Array initialization expression is not a constant expression.";
 
 // Check 12 //
     public static final String error12a_Foreach =
       "Type of expression referenced in foreach is not of array type.";
 
     public static final String error12v_Foreach =
       "Foreach array element of type %T not assignable to value iteration variable %S, of type %T.";
 
     public static final String error12r_Foreach =
      "Foreach array element of type %T not equivalent to reference iteration variable %S, of type %T."
 
 // NOTE!  You don't need to use the formatter with this message
     public static final String error12_Break =
         "Break does not occur in a loop.";
 
 // NOTE!  You don't need to use the formatter with this message
     public static final String error12_Continue =
         "Continue does not occur in a loop.";
 
 // Check 13 //
 // NOTE!  If the field is a struct function, only the name without
 //        parentheses should be provided to %S
     public static final String error13a_Struct =
         "Field %S declared second time in struct.";
 
     public static final String error13b_Struct =
         "Size of field %S cannot be determined at compile time.";
 
 // Check 14 //
     public static final String error14t_StructExp =
         "Type of expression referenced by \".\" (%T) is not a struct.";
 
 // NOTE!  For printing a struct function call, only the name without
 //        parentheses should be provided to %S
     public static final String error14f_StructExp =
         "Referenced field %S not found in type %T.";
 
     public static final String error14b_StructExpThis =
         "Referenced field %S not found in current struct.";
 
 /////////////////////////////////////////////////////////////////
 // 131 - PHASE III
 /////////////////////////////////////////////////////////////////
 
 // Check 15 //
     public static final String error15_Receiver =
         "Incompatible type %T to unary dereference operator *, pointer expected.";
 
     public static final String error15_ReceiverArrow =
         "Incompatible type %T to operator ->, pointer to struct expected.";
 
 // Check 16 //
 // NOTE!  You don't need to use the formatter with this message
     public static final String error16_New_var =
         "Operand to \"new\" is not a modifiable L-value.";
 
     public static final String error16_New =
         "Type of new's operand must be of pointer type, %T found.";
 
 // NOTE!  You don't need to use the formatter with this message
     public static final String error16_Delete_var =
         "Operand to \"delete\" is not a modifiable L-value.";
 
     public static final String error16_Delete =
         "Type of delete's operand must be of pointer type, %T found.";
 
 // Check 17 //
     public static final String error17_Expr =
         "Incompatible types to operator %O:\n"
         + "    %T,\n"
         + "    %T;\n"
         + "  both must be of equivalent pointer type.";
 
 // Check 18 //
 // No new messages for this check -- reuse appropriate messages from
 // earlier checks.
 
 // Check 19 //
 // NOTE!  You don't need to use the formatter with this message
     public static final String error19_Sizeof =
         "Invalid operand to sizeof. Not a type or not addressable.";
 
 // Check 20 //
     public static final String error20_Cast =
         "Invalid type cast. Type %T to type %T is not supported.";
 
 // Check 21 //
     public static final String error21_AddressOf =
         "Non-addressable argument of type %T to address-of operator.";
 
 // Check 22 - Extra Credit //
     public static final String error22_Decl  =
         "Duplicate declaration of overloaded function %S.";
 
     public static final String error22_Illegal  =
         "Illegal call to overloaded function %S.";
 
 }
