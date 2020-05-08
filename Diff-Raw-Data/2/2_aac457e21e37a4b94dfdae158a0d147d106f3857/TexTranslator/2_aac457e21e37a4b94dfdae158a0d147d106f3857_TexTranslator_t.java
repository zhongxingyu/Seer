 package org.eclipse.iee.translator.antlr.translator;
 
 import org.antlr.v4.runtime.ANTLRInputStream;
 import org.antlr.v4.runtime.CommonTokenStream;
 import org.antlr.v4.runtime.ParserRuleContext;
 import org.antlr.v4.runtime.tree.ErrorNode;
 import org.eclipse.iee.translator.antlr.math.MathBaseVisitor;
 import org.eclipse.iee.translator.antlr.math.MathLexer;
 import org.eclipse.iee.translator.antlr.math.MathParser;
 
 public class TexTranslator {
 
 	private static class TexMathVisitor extends MathBaseVisitor<String> {
 
 		// statement rule
 
 		public String visitFunctionDefinition(
 				MathParser.FunctionDefinitionContext ctx) {
 			return visitFunction(ctx.name) + "=" + visit(ctx.value);
 		}
 
 		public String visitVariableAssignment(
 				MathParser.VariableAssignmentContext ctx) {
 			return visit(ctx.name) + "=" + visit(ctx.value);
 		}
 
 		public String visitLogicComparison(MathParser.LogicComparisonContext ctx) {
 			if (ctx.sign.getText().matches(">="))
 				return visit(ctx.left) + "\\ge" + visit(ctx.right);
 			if (ctx.sign.getText().matches("<="))
 				return visit(ctx.left) + "\\le" + visit(ctx.right);
 			if (ctx.sign.getText().matches(">"))
 				return visit(ctx.left) + ">" + visit(ctx.right);
 			if (ctx.sign.getText().matches("<"))
 				return visit(ctx.left) + "<" + visit(ctx.right);
 			if (ctx.sign.getText().matches("!="))
 				return visit(ctx.left) + "\\ne" + visit(ctx.right);
 			if (ctx.sign.getText().matches("=="))
 				return visit(ctx.left) + "==" + visit(ctx.right);
 
 			return visitChildren(ctx);
 		}
 
 		public String visitFunction(MathParser.FunctionContext ctx) {
 			String function = "";
 			function += translateName(ctx.name.getText());
 			function += "(";
 
 			for (int i = 0; i < ctx.params.size(); i++) {
 				function += visit(ctx.params.get(i));
 				if (i != ctx.params.size() - 1)
 					function += ",";
 			}
 
 			function += ")";
 
 			return function;
 		}
 
 		public String visitAdd(MathParser.AddContext ctx) {
 			return visit(ctx.left) + ctx.sign.getText() + visit(ctx.right);
 		}
 
 		public String visitMult(MathParser.MultContext ctx) {
 			if (ctx.sign.getText().matches("\\*"))
 				return visit(ctx.left) + "*" + visit(ctx.right);
 			if (ctx.sign.getText().matches("/"))
 				return "\\frac{" + visit(ctx.left) + "}{" + visit(ctx.right)
 						+ "}";
 			if (ctx.sign.getText().matches("%"))
 				return visit(ctx.left) + " \\mod " + visit(ctx.right);
 
 			return visitChildren(ctx);
 		}
 
 		public String visitPrimaryExpr(MathParser.PrimaryExprContext ctx) {
 			return visitChildren(ctx);
 		}
 
 		public String visitPower(MathParser.PowerContext ctx) {
 			return visit(ctx.left) + "^{" + visit(ctx.right) + "}";
 		}
 
 		public String visitMatrix(MathParser.MatrixContext ctx) {
 			String matrix = "";
 			int i;
 
 			matrix += "$$\\left(\\begin{array}{";
 			int rowsCount = ctx.rows.size();
 			for (i = 0; i < rowsCount; i++)
 				matrix += "c";
			matrix += "}";
 			for (i = 0; i < rowsCount; i++) {
 				matrix += visitMatrixRow(ctx.rows.get(i));
 				if (i != rowsCount - 1)
 					matrix += "\\\\";
 			}
 
 			matrix += "\\end{array}\\right)$$";
 
 			return matrix;
 		}
 
 		public String visitLogicMult(MathParser.LogicMultContext ctx) {
 			return visit(ctx.left) + " \\wedge " + visit(ctx.right);
 		}
 
 		public String visitLogicBrackets(MathParser.LogicBracketsContext ctx) {
 			return '(' + visit(ctx.expr) + ')';
 		}
 
 		public String visitLogicAdd(MathParser.LogicAddContext ctx) {
 			return visit(ctx.left) + " \\vee " + visit(ctx.right);
 		}
 
 		public String visitUnary(MathParser.UnaryContext ctx) {
 			return ctx.sign.getText() + visit(ctx.unaryExpr);
 		}
 
 		public String visitExprBrackets(MathParser.ExprBracketsContext ctx) {
 			return '(' + visit(ctx.bracketedExpr) + ')';
 		}
 
 		public String visitMatrixRow(MathParser.MatrixRowContext ctx) {
 			String row = "";
 
 			for (int i = 0; i < ctx.elements.size(); i++) {
 				row += visit(ctx.elements.get(i));
 				if (i != ctx.elements.size() - 1)
 					row += "&";
 			}
 
 			return row;
 		}
 
 		// primary rule
 
 		public String visitVariable(MathParser.VariableContext ctx) {
 			return translateName(ctx.getText());
 		}
 
 		public String visitFloatNumber(MathParser.FloatNumberContext ctx) {
 			return ctx.getText();
 		}
 
 		public String visitIntNumber(MathParser.IntNumberContext ctx) {
 			return ctx.getText();
 		}
 
 		public String visitMatrixDefinition(
 				MathParser.MatrixDefinitionContext ctx) {
 			return visitChildren(ctx);
 		}
 
 		public String visitMatrixElement(MathParser.MatrixElementContext ctx) {
 			return translateName(ctx.name.getText()) + "_{" + visit(ctx.rowIdx)
 					+ "," + visit(ctx.columnIdx) + "}";
 		}
 
 		public String visitPrimaryFunction(MathParser.PrimaryFunctionContext ctx) {
 			return visitFunction(ctx.function());
 		}
 
 		public String visitMethodCall(MathParser.MethodCallContext ctx) {
 			return translateName(ctx.objName.getText()) + "."
 					+ visitFunction(ctx.objFunction);
 		}
 
 		public String visitProperty(MathParser.PropertyContext ctx) {
 			return translateName(ctx.objName.getText()) + "."
 					+ translateName(ctx.objProperty.getText());
 		}
 		
 	}
 	
 
 	public static String translate(String expression) {
 		String result = "";
 
 		ANTLRInputStream input = new ANTLRInputStream(expression);
 		MathLexer lexer = new MathLexer(input);
 		CommonTokenStream tokens = new CommonTokenStream(lexer);
 		MathParser parser = new MathParser(tokens);
 		parser.setBuildParseTree(true);
 		ParserRuleContext tree = parser.statement();
 
 		TexMathVisitor mathVisitor = new TexMathVisitor();
 		result = mathVisitor.visit(tree);
 
 		return result;
 	}
 
 	private static String translateName(String name) {
 		String translatedName = name;
 
 		if (name.matches("^alpha$"))
 			translatedName = "\\\\alpha";
 		if (name.matches("^beta$"))
 			translatedName = "\\\\beta";
 		if (name.matches("^delta$"))
 			translatedName = "\\\\delta";
 		if (name.matches("^epsilon$"))
 			translatedName = "\\\\epsilon";
 		if (name.matches("^varepsilon$"))
 			translatedName = "\\\\varepsilon";
 		if (name.matches("^zeta$"))
 			translatedName = "\\\\zeta";
 		if (name.matches("^eta$"))
 			translatedName = "\\\\eta";
 		if (name.matches("^theta$"))
 			translatedName = "\\\\theta";
 		if (name.matches("^vartheta$"))
 			translatedName = "\\\\vartheta";
 		if (name.matches("^gamma$"))
 			translatedName = "\\\\gamma";
 		if (name.matches("^kappa$"))
 			translatedName = "\\\\kappa";
 		if (name.matches("^lambda$"))
 			translatedName = "\\\\lambda";
 		if (name.matches("^mu$"))
 			translatedName = "\\\\mu";
 		if (name.matches("^nu$"))
 			translatedName = "\\\\nu";
 		if (name.matches("^xi$"))
 			translatedName = "\\\\xi";
 		if (name.matches("^varpi$"))
 			translatedName = "\\\\varpi";
 		if (name.matches("^rho$"))
 			translatedName = "\\\\rho";
 		if (name.matches("^varrho$"))
 			translatedName = "\\\\varrho";
 		if (name.matches("^sigma$"))
 			translatedName = "\\\\sigma";
 		if (name.matches("^varsigma$"))
 			translatedName = "\\\\varsigma";
 		if (name.matches("^tau$"))
 			translatedName = "\\\\tau";
 		if (name.matches("^upsilon$"))
 			translatedName = "\\\\upsilon";
 		if (name.matches("^phi$"))
 			translatedName = "\\\\phi";
 		if (name.matches("^varphi$"))
 			translatedName = "\\\\varphi";
 		if (name.matches("^chi$"))
 			translatedName = "\\\\chi";
 		if (name.matches("^psi$"))
 			translatedName = "\\\\psi";
 		if (name.matches("^omega$"))
 			translatedName = "\\\\omega";
 		if (name.matches("^Gamma$"))
 			translatedName = "\\\\Gamma";
 		if (name.matches("^Delta$"))
 			translatedName = "\\\\Delta";
 		if (name.matches("^Theta$"))
 			translatedName = "\\\\Theta";
 		if (name.matches("^Lambda$"))
 			translatedName = "\\\\Lambda";
 		if (name.matches("^Xi$"))
 			translatedName = "\\\\Xi";
 		if (name.matches("^Pi$"))
 			translatedName = "\\\\Pi";
 		if (name.matches("^Sigma$"))
 			translatedName = "\\\\Sigma";
 		if (name.matches("^Upsilon$"))
 			translatedName = "\\\\Upsilon";
 		if (name.matches("^Phi$"))
 			translatedName = "\\\\Phi";
 		if (name.matches("^Psi$"))
 			translatedName = "\\\\Psi";
 		if (name.matches("^Omega$"))
 			translatedName = "\\\\Omega";
 		
 		if (name.matches("^alpha_*$"))
 			translatedName = name.replaceFirst("alpha", "\\\\alpha");
 		if (name.matches("^beta_*$"))
 			translatedName = name.replaceFirst("beta", "\\\\beta");
 		if (name.matches("^delta_*$"))
 			translatedName = name.replaceFirst("delta", "\\\\delta");
 		if (name.matches("^epsilon_*$"))
 			translatedName = name.replaceFirst("epsilon", "\\\\epsilon");
 		if (name.matches("^varepsilon_*$"))
 			translatedName = name.replaceFirst("varepsilon", "\\\\varepsilon");
 		if (name.matches("^zeta_*$"))
 			translatedName = name.replaceFirst("zeta", "\\\\zeta");
 		if (name.matches("^eta_*$"))
 			translatedName = name.replaceFirst("eta", "\\\\eta");
 		if (name.matches("^theta_*$"))
 			translatedName = name.replaceFirst("theta", "\\\\theta");
 		if (name.matches("^vartheta_*$"))
 			translatedName = name.replaceFirst("vartheta", "\\\\vartheta");
 		if (name.matches("^gamma_*$"))
 			translatedName = name.replaceFirst("gamma", "\\\\gamma");
 		if (name.matches("^kappa_*$"))
 			translatedName = name.replaceFirst("kappa", "\\\\kappa");
 		if (name.matches("^lambda_*$"))
 			translatedName = name.replaceFirst("lambda", "\\\\lambda");
 		if (name.matches("^mu_*$"))
 			translatedName = name.replaceFirst("mu", "\\\\mu");
 		if (name.matches("^nu_*$"))
 			translatedName = name.replaceFirst("nu", "\\\\nu");
 		if (name.matches("^xi_*$"))
 			translatedName = name.replaceFirst("xi", "\\\\xi");
 		if (name.matches("^varpi_*$"))
 			translatedName = name.replaceFirst("varpi", "\\\\varpi");
 		if (name.matches("^rho_*$"))
 			translatedName = name.replaceFirst("rho", "\\\\rho");
 		if (name.matches("^varrho_*$"))
 			translatedName = name.replaceFirst("varrho", "\\\\varrho");
 		if (name.matches("^sigma_*$"))
 			translatedName = name.replaceFirst("sigma", "\\\\sigma");
 		if (name.matches("^varsigma_*$"))
 			translatedName = name.replaceFirst("varsigma", "\\\\varsigma");
 		if (name.matches("^tau_*$"))
 			translatedName = name.replaceFirst("tau", "\\\\tau");
 		if (name.matches("^upsilon_*$"))
 			translatedName = name.replaceFirst("upsilon", "\\\\upsilon");
 		if (name.matches("^phi_*$"))
 			translatedName = name.replaceFirst("phi", "\\\\phi");
 		if (name.matches("^varphi_*$"))
 			translatedName = name.replaceFirst("varphi", "\\\\varphi");
 		if (name.matches("^chi_*$"))
 			translatedName = name.replaceFirst("chi", "\\\\chi");
 		if (name.matches("^psi_*$"))
 			translatedName = name.replaceFirst("psi", "\\\\psi");
 		if (name.matches("^omega_*$"))
 			translatedName = name.replaceFirst("omega", "\\\\omega");
 		if (name.matches("^Gamma_*$"))
 			translatedName = name.replaceFirst("Gamma", "\\\\Gamma");
 		if (name.matches("^Delta_*$"))
 			translatedName = name.replaceFirst("Delta", "\\\\Delta");
 		if (name.matches("^Theta_*$"))
 			translatedName = name.replaceFirst("Theta", "\\\\Theta");
 		if (name.matches("^Lambda_*$"))
 			translatedName = name.replaceFirst("Lambda", "\\\\Lambda");
 		if (name.matches("^Xi_*$"))
 			translatedName = name.replaceFirst("Xi", "\\\\Xi");
 		if (name.matches("^Pi_*$"))
 			translatedName = name.replaceFirst("Pi", "\\\\Pi");
 		if (name.matches("^Sigma_*$"))
 			translatedName = name.replaceFirst("Sigma", "\\\\Sigma");
 		if (name.matches("^Upsilon_*$"))
 			translatedName = name.replaceFirst("Upsilon", "\\\\Upsilon");
 		if (name.matches("^Phi_*$"))
 			translatedName = name.replaceFirst("Phi", "\\\\Phi");
 		if (name.matches("^Psi_*$"))
 			translatedName = name.replaceFirst("Psi", "\\\\Psi");
 		if (name.matches("^Omega_*$"))
 			translatedName = name.replaceFirst("Omega", "\\\\Omega");
 
 		return translatedName;
 	}
 
 }
