 package swp_compiler_ss13.common.ir;
 
import java.util.List;

 import swp_compiler_ss13.common.backend.Quadruple;
 import swp_compiler_ss13.common.parser.AST;
 
 /**
  * Proposed interface for intermediate code generator.
  * 
  * @author "Frank Zechert", "Danny Maasch"
  * @version 1
  * @see <a target="_top" href=
  *      "https://github.com/swp-uebersetzerbau-ss13/common/wiki/Intermediate-Code-Generator"
  *      >Intermediate Code Generator Wiki</a>
  * @see <a target="_top"
  *      href="https://github.com/swp-uebersetzerbau-ss13/common/issues/1"
  *      >Intermediate Code Generator Issue Tracker</a>
  */
 public interface IntermediateCodeGenerator
 {
 
 	/**
 	 * Generates quadruple representation of three address code from the correct
 	 * abstract syntax tree (AST).
 	 * 
 	 * @param ast
 	 *            The semantically correct abstract syntax tree.
 	 * @throws IntermediateCodeGeneratorException
 	 *             Thrown if an error occurs.
 	 */
 	public List<Quadruple> generateIntermediateCode(AST ast)
 			throws IntermediateCodeGeneratorException;
 }
