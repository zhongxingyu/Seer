 package codeGeneration;
 
 import org.antlr.runtime.tree.Tree;
 
 import semantics_checks.SemanticsUtils;
 import symbol_table.DATA_TYPES;
 import symbol_table.SymbolTable;
 import symbol_table.VariableSTValue;
 
 public class Statement
 {
 	/**
 	 * @field BUFF_SIZE User-defined buffer size to limit input
 	 */
 	private static final int BUFF_SIZE = 100;
 
 	/**
 	 * Checks statements and passes arguments to generateStatementCode to
 	 * convert it to LLVM code
 	 * 
 	 * @param node
 	 *            Current node
 	 * @param table
 	 *            Current Symbol-Table
 	 * @param gen
 	 *            Current LabelGenerator
 	 * @return returns last node
 	 */
 	public static Tree checkAllStatements(Tree node, SymbolTable table,
 			LabelGenerator gen)
 	{
 		Tree current = node;
 		boolean end_of_statements = false;
 		while (current != null && !end_of_statements)
 		{
 			end_of_statements = generateStatementCode(current, table, gen);
 			if (end_of_statements)
 			{
 				return current;
 			}
 			current = SemanticsUtils.getNextChild(current);
 		}
 		return current;
 
 	}
 
 	/**
 	 * Calls Helper method converting the MAlice code to LLVM code
 	 * 
 	 * @param node
 	 *            Current node
 	 * @param table
 	 *            Current SymbolTable
 	 * @param gen
 	 *            Current LabelGenerator
 	 * @return 'true' if end of all statements, else 'true'
 	 */
 	private static boolean generateStatementCode(Tree node, SymbolTable table,
 			LabelGenerator gen)
 	{
 		if (node.getText() == null || node.getChildCount() == 0)
 		{
 			return true;
 		}
 
 		if (node.getText().contentEquals("was"))
 		{
 			writeWAS(node, table, gen);
 			return false;
 		} else if (node.getText().contentEquals("ate")
 				|| node.getText().contentEquals("drank"))
 		{
 			StatementsCodeGeneratorMagda.writeAteAndDrankCode(node, table, gen,
 					(node.getText().contentEquals("ate") ? "add" : "sub"));
 			return false;
 		} else if (node.getText().contentEquals("became"))
 		{
 			StatementsCodeGeneratorMagda.writeBecameCode(node, table, gen);
 			return false;
 		} else if (node.getText().contentEquals("spoke")
 				|| node.getText().contentEquals("said"))
 		{
 			StatementsCodeGeneratorMagda.writePrintStatementCode(node, table,
 					gen);
 			return false;
 		} else if (node.getText().contentEquals("what"))
 		{
 			writeWHAT(node, table, gen);
 			return false;
 		} else if (node.getText().contentEquals("found"))
 		{
 			StatementsCodeGeneratorMagda.writeFoundCode(node, table, gen);
 			return false;
 		} else if (node.getText().contentEquals("had"))
 		{
 			return false;
 		} else if (node.getText().contentEquals("perhaps"))
 		{
 			ControlStructure.writePerhapsStatements(node, table, gen);
 			return false;
 		} else if (node.getText().contentEquals("either"))
 		{
 			ControlStructure.writeEitherStatement(node, table, gen);
 			return false;
 		} else if (node.getText().contentEquals("eventually"))
 		{
 			ControlStructure.writeEventuallyStatement(node, table, gen);
 			return false;
 		} else if (node.getText().contentEquals("opened"))
 		{
 			return false;
		} else if (node.getChildCount() > 2
 				&& node.getChild(0).getText().contentEquals("(")
				&& node.getChild(node.getChildCount() - 1).getText()
						.contentEquals(")"))
 		{
 			CodeGenerator.addInstruction("call void "
					+ table.lookup(node.getText()).getLocationReg()
					+ Expression.getParamsToFunction(node, table));
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Translates WHAT read-statement to LLVM assembly code
 	 * 
 	 * @param node
 	 *            Current node
 	 * @param table
 	 *            Current SymbolTable
 	 * @param gen
 	 *            Current LabelGenerator
 	 */
 	private static void writeWHAT(Tree node, SymbolTable table,
 			LabelGenerator gen)
 	{
 		CodeGenerator.includeStdin();
 		if (node.getChild(0).getChildCount() == 0)
 		{
 			String arg1 = node.getChild(0).getText();
 			VariableSTValue v = (VariableSTValue) table.lookup(arg1);
 			DATA_TYPES type = v.getType();
 
 			if (type == DATA_TYPES.NUMBER)
 			{
 				String tempReg = gen.getUniqueLabel();
 				String buff = tempReg;
 				CodeGenerator.addInstruction(tempReg + " = alloca ["
 						+ BUFF_SIZE + " x i8], align 16");
 				String regId1 = gen.getUniqueLabel();
 				addGetElementPtrIns(buff, regId1);
 				String regId2 = gen.getUniqueLabel();
 				addLoadIOFileIns(regId2);
 				tempReg = writeCallFGetsIns(gen, regId1, regId2);
 				regId1 = gen.getUniqueLabel();
 				addGetElementPtrIns(buff, regId1);
 				regId2 = gen.getUniqueLabel();
 				CodeGenerator.addInstruction(regId2 + " = call i32 @atoi(i8* "
 						+ regId1 + ") nounwind readonly");
 				CodeGenerator.addInstruction("store i32 " + regId2 + ", i32* "
 						+ v.getLocationReg() + ", align 4");
 				CodeGenerator.includeATOI();
 				CodeGenerator.includeRead();
 			} else if (type == DATA_TYPES.LETTER)
 			{
 				String tempReg = gen.getUniqueLabel();
 				String buff = tempReg;
 				CodeGenerator.addInstruction(tempReg + " = alloca ["
 						+ BUFF_SIZE + " x i8], align 16");
 				String regId1 = gen.getUniqueLabel();
 				addGetElementPtrIns(buff, regId1);
 				String regId2 = gen.getUniqueLabel();
 				addLoadIOFileIns(regId2);
 				tempReg = writeCallFGetsIns(gen, regId1, regId2);
 				regId1 = gen.getUniqueLabel();
 				addGetElementPtrIns(buff, regId1);
 				regId2 = gen.getUniqueLabel();
 				CodeGenerator.addInstruction(regId2 + " = load i8* " + regId1
 						+ ", align 1");
 				CodeGenerator.addInstruction("store i8 " + regId2 + ", i8* "
 						+ v.getLocationReg() + ", align 1");
 				CodeGenerator.includeRead();
 
 			} else if (type == DATA_TYPES.SENTENCE)
 			{
 				String regId1 = gen.getUniqueLabel();
 				String buff = regId1;
 				CodeGenerator.addInstruction(regId1
 						+ " = alloca [100 x i8], align 16");
 				String regId2 = gen.getUniqueLabel();
 				addGetElementPtrIns(regId1, regId2);
 				regId1 = gen.getUniqueLabel();
 				addLoadIOFileIns(regId1);
 				regId1 = writeCallFGetsIns(gen, regId2, regId1);
 				regId2 = gen.getUniqueLabel();
 				addGetElementPtrIns(buff, regId2);
 				CodeGenerator.addInstruction("store i8* " + regId2 + ", i8** "
 						+ v.getLocationReg() + ", align 8");
 
 				CodeGenerator.includeRead();
 			}
 		}
 
 	}
 
 	private static String writeCallFGetsIns(LabelGenerator gen, String regId1,
 			String regId2)
 	{
 		String uniqueLabel = gen.getUniqueLabel();
 		CodeGenerator.addInstruction(uniqueLabel + " = call i8* @fgets(i8* "
 				+ regId1 + ", i32 " + BUFF_SIZE + ", %struct._IO_FILE* "
 				+ regId2 + ")");
 		return uniqueLabel;
 	}
 
 	private static void addGetElementPtrIns(String location, String regId1)
 	{
 		CodeGenerator.addInstruction(regId1 + " = getelementptr inbounds ["
 				+ BUFF_SIZE + " x i8]* " + location + ", i32 0, i32 0 ");
 	}
 
 	private static void addLoadIOFileIns(String regId2)
 	{
 		CodeGenerator.addInstruction(regId2
 				+ " = load %struct._IO_FILE** @stdin, align 8");
 	}
 
 	/**
 	 * Translates WAS declaration-statement to LLVM assembly code
 	 * 
 	 * @param node
 	 *            Current node
 	 * @param table
 	 *            Current SymbolTable
 	 * @param gen
 	 *            Current LabelGenerator
 	 */
 	private static void writeWAS(Tree node, SymbolTable table,
 			LabelGenerator gen)
 	{
 		Tree storable = node.getChild(2);
 		String arg1 = node.getChild(0).getText();
 		String arg2 = node.getChild(1).getText();
 		if (table.getCurrentScopeLevel() == 0)
 		{
 			if (arg2.equals("number"))
 			{
 				table.lookup(arg1).setLocationReg("@" + arg1);
 				if (storable != null)
 				{
 					CodeGenerator.addInstruction("@" + arg1 + " = global i32 "
 							+ Expression.getResultReg(storable, table, gen)
 							+ ", align 4");
 				}
 			} else if (arg2.equals("letter"))
 			{
 				table.lookup(arg1).setLocationReg("@" + arg1);
 				if (storable != null)
 				{
 					CodeGenerator.addInstruction("@" + arg1 + " = global i8 "
 							+ (int) (storable.getText().charAt(1))
 							+ ", align 1");
 				} else
 				{
 					CodeGenerator.addInstruction("@" + arg1
 							+ " = global i8 0, align 1");
 				}
 			} else if (arg2.equals("sentence"))
 			{
 				table.lookup(arg1).setLocationReg("@" + arg1);
 				if (storable != null)
 				{
 					int strLen = storable.getText().length() - 1;
 					String effective = storable.getText().substring(1, strLen);
 					CodeGenerator.addInstruction("@.at" + arg1
 							+ table.getCurrentScopeLevel()
 							+ " = private unnamed_addr constant [" + strLen
 							+ " x i8] c\"" + effective + '\\' + "00"
 							+ "\", align 1", 0);
 					CodeGenerator.addInstruction("@" + arg1
 							+ " = global i8* getelementptr inbounds (["
 							+ strLen + " x i8]* @.at" + arg1
 							+ table.getCurrentScopeLevel()
 							+ ", i32 0, i32 0), align 8");
 				} else
 				{
 					CodeGenerator.addInstruction("@" + arg1
 							+ " = common global i8* null, align 8");
 				}
 			}
 		} else
 		{
 			if (arg2.equals("number"))
 			{
 				table.lookup(arg1).setLocationReg("%" + arg1);
 				CodeGenerator.addInstruction("%" + arg1
 						+ " = alloca i32, align 4");
 				if (storable != null)
 				{
 					CodeGenerator.addInstruction("store i32 "
 							+ Expression.getResultReg(storable, table, gen)
 							+ ", i32* %" + arg1 + ", align 4");
 				}
 			} else if (arg2.equals("letter"))
 			{
 				table.lookup(arg1).setLocationReg("%" + arg1);
 				CodeGenerator.addInstruction("%" + arg1
 						+ " = alloca i8, align 1");
 				if (storable != null)
 				{
 					CodeGenerator.addInstruction("store i8 "
 							+ (int) (storable.getText().charAt(1)) + ", i8* %"
 							+ arg1 + ", align 1");
 				}
 			} else if (arg2.equals("sentence"))
 			{
 				table.lookup(arg1).setLocationReg("%" + arg1);
 				CodeGenerator.addInstruction("%" + arg1
 						+ " = alloca i8*, align 8");
 				if (storable != null)
 				{
 					int strLen = storable.getText().length() - 1;
 					String effective = storable.getText().substring(1, strLen);
 					CodeGenerator.addInstruction("@.at" + arg1
 							+ table.getCurrentScopeLevel()
 							+ " = private unnamed_addr constant [" + strLen
 							+ " x i8] c\"" + effective + '\\' + "00"
 							+ "\", align 1", 0);
 					CodeGenerator
 							.addInstruction("store i8* getelementptr inbounds (["
 									+ strLen
 									+ " x i8]* @.at"
 									+ arg1
 									+ table.getCurrentScopeLevel()
 									+ ", i32 0, i32 0), i8** %"
 									+ arg1
 									+ ", align 8");
 
 				}
 			}
 		}
 	}
 }
