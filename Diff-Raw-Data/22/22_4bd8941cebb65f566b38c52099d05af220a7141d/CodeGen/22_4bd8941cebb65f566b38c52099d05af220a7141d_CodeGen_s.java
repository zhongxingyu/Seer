 package yapl.interfaces;
 
 import yapl.lib.ArrayType;
 import yapl.lib.Type;
 import yapl.lib.YAPLException;
 
 /**
  * Interface to code generator methods called by the parser. Some of these
  * methods should also implement type checking.
  * <p>
  * An implementation of this interface should not emit assembler or machine code
  * directly, but is expected to "generate" generic 3-address-code by calling
  * methods of the {@link yapl.interfaces.TargetArch} interface only.
  * </p>
  * 
  * @author Mario Taschwer
  * @version $Id: CodeGen.java 141 2010-03-16 17:17:56Z mt $
  */
 public interface CodeGen {
 
 	/**
 	 * Generate a new (assembler) label. Labels must be unique.
 	 */
 	public String newLabel();
 
 	/** Emit an assembler label. */
 	public void writeLabel(String label);
 
 	/** Emit an assembler label with comment. */
 	public void writeLabel(String label, String comment);
 
 	/**
 	 * Load the value represented by <code>attr</code> into a register, if
 	 * <code>attr</code> does not already represent a register value. The
 	 * {@link Attrib#getRegister() register property} of <code>attr</code>
 	 * will be set to the register number.
 	 * 
 	 * @throws YAPLException
 	 *             (NoMoreRegs) if there are no free registers available.
 	 * @throws YAPLException
 	 *             (Internal) if the {@link Attrib#getKind() kind} or
 	 *             {@link Attrib#getType() type} properties of <code>attr</code>
 	 *             have an unexpected value.
 	 */
 	public void loadReg(Attrib attr) 
 	throws YAPLException;
 
 	/**
 	 * Free the register used by the register operand <code>attr</code>. Has
 	 * no effect if <code>attr</code> does not represent a register operand.
 	 * The operand's {@link Attrib#getKind() kind} will be set to
 	 * {@link Attrib#None}.
 	 */
 	public void freeReg(Attrib attr);
 
 	/**
 	 * Allocate space for a memory object (i.e. a variable). If the symbol
 	 * belongs to a {@link Symbol#isGlobal() global scope}, space will be
 	 * allocated on the heap; otherwise, space will be allocated in the current
 	 * stack frame.
 	 * 
 	 * @param sym
 	 *            the symbol to allocate space for. The symbol's
 	 *            {@link Symbol#getOffset() address offset} will be updated.
 	 * @throws YAPLException (Internal)
 	 *            if <code>sym</code> does not provide sufficient information
 	 *            (data type, scope), etc.
 	 */
 	public void allocVariable(Symbol sym) 
 	throws YAPLException;
 
 	/**
 	 * Store length of given array dimension.
 	 * The stored array dimensions are needed for run-time allocation of
 	 * the array, see {@link #allocArray(Type)}.
 	 * @param dim       dimension number; starts at 0.
 	 * @param length    operand representing the dimension length.
 	 *                  Its register is released.
 	 * @throws YAPLException (TooManyDims)
 	 *                  if <code>dim</code> exceeds an implementation-defined maximum.
 	 */
 	public void storeArrayDim(int dim, Attrib length)
 	throws YAPLException;
 	
 	/**
 	 * Allocate array at run time.
 	 * @param arrayType  array type.
 	 * @return           Attrib object representing a register or stack operand
 	 *                   containing the array start address.
 	 * @throws YAPLException
 	 */
 	public Attrib allocArray(ArrayType arrayType)
 	throws YAPLException;
 	
 	/**
 	 * Update a formal parameter's {@link Symbol#getOffset() address offset}.
 	 * Must not generate code.
 	 * 
 	 * @param sym
 	 *            the formal parameter symbol.
 	 * @param pos
 	 *            the parameter position within the list of formal parameters
 	 *            (starts at 0).
 	 */
 	public void setParamOffset(Symbol sym, int pos);
 
 	/**
 	 * Generate code for address offset computation of an array element.
 	 * 
 	 * @param arr
 	 *            the operand representing the array base address; its
 	 *            {@link Attrib#getKind() kind},
 	 *            {@link Attrib#getRegister() register}, and
 	 *            {@link Attrib#getType() type} attributes will be updated in
 	 *            place to represent the <em>array element</em>,
 	 *            see {@link Attrib#ArrayElement}.
 	 * @param index
 	 *            the operand (expression) representing the index of the array
 	 *            element (starts at 0). It will be linked to <code>arr</code>
 	 *            using {@link Attrib#setOffsetAttrib(Attrib)}.
 	 * @throws YAPLException
 	 *             (Internal) if <code>arr</code> does not represent an array
 	 *             type.
 	 */
 	public void arrayOffset(Attrib arr, Attrib index) 
 	throws YAPLException;
 
 	/**
 	 * Generate code for array length computation at run time.
 	 * @param arr   operand representing the array.
 	 * @return      the object referenced by <code>arr</code>, updated to represent
 	 *              the number of elements of the first dimension of <code>arr</code>.
 	 * @throws YAPLException
 	 */
 	public Attrib arrayLength(Attrib arr)
 	throws YAPLException;
 	
 	/**
 	 * Generate code for variable assignment.
 	 * 
 	 * @param lvalue
 	 *            left-hand side value of assignment (target).
 	 * @param expr
 	 *            right-hand side value of assignment (source).
 	 * @throws YAPLException
 	 *             (Internal) if <code>lvalue</code> has an illegal
 	 *             {@link Attrib#getKind() kind property}.
 	 */
 	public void assign(Attrib lvalue, Attrib expr) 
 	throws YAPLException;
 
 	/**
 	 * Check types and generate code for unary operation <code>x = op x</code>.
 	 * <code>x</code> will be updated in place to represent the result.
 	 * 
 	 * @param op
 	 *            the operator symbol.
 	 * @param x
 	 *            the operand.
 	 * @return the object referenced by <code>x</code>.
 	 * @throws YAPLException
 	 *             (Internal) if the operator symbol is not a valid unary
 	 *             operator.
 	 * @throws YAPLException
 	 *             (IllegalOp1Type) if the operand type cannot be applied to the
 	 *             given operator.
 	 */
	public Attrib op1(Token op, Attrib x) 
 	throws YAPLException;
 
 	/**
 	 * Check types and generate code for binary operation
 	 * <code>x = x op y</code>. <code>x</code> will be updated in place to
 	 * represent the result.
 	 * 
 	 * @param x
 	 *            the left operand.
 	 * @param op
 	 *            the operator symbol.
 	 * @param y
 	 *            the right operand.
 	 * @return the object referenced by <code>x</code>.
 	 * @throws YAPLException
 	 *             (Internal) if the operator symbol is not a valid binary
 	 *             operator.
 	 * @throws YAPLException
 	 *             (IllegalOp2Type) if the operand types cannot be applied to
 	 *             the given operator.
 	 */
	public Attrib op2(Attrib x, Token op, Attrib y) 
 	throws YAPLException;
 
 	/**
 	 * Check types and generate code for relational operation
 	 * <code>x op y</code>. The result is represented by <code>x</code>,
 	 * which is updated in place. Its type is set to Boolean.
 	 * 
 	 * @param x
 	 *            the left operand.
 	 * @param op
 	 *            the operator symbol.
 	 * @param y
 	 *            the right operand.
 	 * @return the object referenced by <code>x</code>.
 	 * @throws YAPLException
 	 *             (Internal) if the operator symbol is not a valid relational
 	 *             operator.
 	 * @throws YAPLException
 	 *             (IllegalRelOpType) if the operand types cannot be applied to
 	 *             the given operator.
 	 */
	public Attrib relOp(Attrib x, Token op, Attrib y) 
 	throws YAPLException;
 
 	/**
 	 * Check types and generate code for equality operation <code>x op y</code>.
 	 * The result is represented by <code>x</code>, which is updated in
 	 * place. Its type is set to Boolean.
 	 * 
 	 * @param x
 	 *            the left operand.
 	 * @param op
 	 *            the operator symbol.
 	 * @param y
 	 *            the right operand.
 	 * @return the object referenced by <code>x</code>.
 	 * @throws YAPLException
 	 *             (Internal) if the operator symbol is not a valid relational
 	 *             operator.
 	 * @throws YAPLException
 	 *             (IllegalEqualOpType) if the operand types cannot be applied to
 	 *             the given operator.
 	 */
	public Attrib equalOp(Attrib x, Token op, Attrib y) 
 	throws YAPLException;
 
 	/**
 	 * Enter procedure.
 	 * 
 	 * @param proc
 	 *            the procedure symbol; the formal parameters must already be
 	 *            attached as a linked list (see {@link Symbol#getNextSymbol()}).
 	 *            If <code>proc == null</code>, code for the main program
 	 *            (entry point) will be generated.
 	 */
 	public void enterProc(Symbol proc) 
 	throws YAPLException;
 
 	/**
 	 * Exit procedure.
 	 * 
 	 * @param proc
 	 *            the procedure symbol; if <code>proc == null</code>,
 	 *            exit from the main program.
 	 */
 	public void exitProc(Symbol proc) 
 	throws YAPLException;
 
 	/**
 	 * Return from procedure. The return value type is <em>not</em> checked -
 	 * this should happen before calling this method.
 	 * 
 	 * @param proc
 	 *            the symbol representing the procedure containing the RETURN
 	 *            statement; if <code>proc == null</code>, return from the
 	 *            main program.
 	 * @param returnVal
 	 *            the operand representing the value to be returned by the
 	 *            procedure. May be <code>null</code> if the procedure does
 	 *            not return a value.
 	 */
 	public void returnFromProc(Symbol proc, Attrib returnVal)
 	throws YAPLException;
 
 	/**
 	 * Procedure call.
 	 * 
 	 * @param proc
 	 *            the procedure symbol.
 	 * @param args
 	 *            the Attrib objects representing the argument values; may be
 	 *            <code>null</code>.
 	 * @return a new Attrib object representing the procedure's result;
 	 *         <code>null</code> if the procedure does not return a value.
 	 */
 	public Attrib callProc(Symbol proc, Attrib[] args) 
 	throws YAPLException;
 
 	/**
 	 * Call the predefined <code>write</code> procedure.
 	 * 
 	 * @param string
 	 *            string to be written to standard output, enclosed in double
 	 *            quotes.
 	 */
 	public void callWrite(String string) 
 	throws YAPLException;
 
 	/**
 	 * Emit a branch instruction jumping to <code>label</code> if
 	 * <code>condition</code> is <code>false</code>.
 	 */
 	public void branchIfFalse(Attrib condition, String label)
 	throws YAPLException;
 
 	/** Emit an unconditional jump to <code>label</code>. */
 	public void jump(String label);
 
 }
