 package net.digitalprimates.translators.actionscript;
 
 import net.digitalprimates.antlr.TypeScriptListener;
 import net.digitalprimates.antlr.TypeScriptParser;
 import org.antlr.v4.runtime.ParserRuleContext;
 import org.antlr.v4.runtime.TokenStream;
 import org.antlr.v4.runtime.tree.ErrorNode;
 import org.antlr.v4.runtime.tree.TerminalNode;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * User: David "Vandermore" Moore
  * Date: 7/11/13
  * Time: 11:03 AM
  */
 public class TypeScriptToAS implements TypeScriptListener {
     public static final String OPEN_BRACE = "{";
     public static final String CLOSE_BRACE = "}";
     public static final String SEMI_COLON = ";";
 
     protected ExportPreparation exportPrep;
     protected Boolean isArrayType = Boolean.FALSE;
     protected Boolean isInterface = Boolean.FALSE;
    protected Boolean isARestParameter = Boolean.FALSE;

     //These two are used for when the base class is implemented. Ext's base class is just functions and parameters,
     // no class or interface definition.
     //TODO:: This may need to change if we make this more generic, rather than for just ExtJS.
     protected Boolean isDefaultClass = Boolean.FALSE;
 
     protected String possibleClassName;
     protected String interfaceName = "";
     protected String returnType = "";
 
     protected ArrayList<String> methodOverloads;
 
     protected TypeScriptParser parser;
 
     //TODO:: Make a TAB depth method that will insert the correct number of tabs into a file depending on the depth that the parse is on.
 
     public TypeScriptToAS( TypeScriptParser parser ) {
         this.parser = parser;
     }
 
     protected String getASType( String value ) {
         //Need to get just the class name out of the full path.
         String[] fullyQualClassPath = value.split("\\.");
         //Should only return the class name here.
         String asType = fullyQualClassPath[ fullyQualClassPath.length - 1 ];
 
         if (asType != null) {
             if ( !isArrayType ) {
                 switch ( asType ) {
                     case "any" :
                         asType = "*";
                         break;
                     case "bool" :
                         asType = "Boolean";
                         break;
                     case "number" :
                         asType = "Number";
                         break;
                     case "string" :
                         asType = "String";
                         break;
                     default:
                         //Another way to capture the import statements needed.
                         if ( value.equals("HTMLElement") ) {
                             exportPrep.addImports( "randori.webkit.html.HTMLElement" );
                         } else {
                             exportPrep.addImports( value );
                         }
                         break;
                 }
             } else {
                 asType = "Array";
             }
         }
 
         return asType;
     }
 
     protected void writeImports( String imports ) {
         String[] importList = imports.split(",");
         for ( int i = 0; i < importList.length; i++ ) {
             exportPrep.addImports(importList[i]);
         }
     }
 
     protected void resetOverloadTracker() {
         methodOverloads = new ArrayList<String>( 0 );
     }
 
     /**
      * Checks the method names to see if they are being overloaded in any one interface or class.
      * @param methodName
      * @return The method name that should be used when writing out the file.
      */
     protected String checkForOverloads( String methodName ) {
         String returnName = methodName;
         int overloadCounter = 1;
 
         while ( methodOverloads.contains( returnName ) ) {
             returnName = methodName + overloadCounter;
             overloadCounter++;
         }
 
         //Add in the method name, so it will be able to be checked later.
         methodOverloads.add( returnName );
         return returnName;
     }
 
     /**
      * Creates the parameters for modules. This is the default class Ext for ExtJS. Has not been tested for other
      * frameworks.
      * @param ctx
      */
     @Override public void enterAmbientVariableDeclaration(TypeScriptParser.AmbientVariableDeclarationContext ctx) {
         exportPrep.addToPostImport( ExportPreparation.LINE_BREAK );
         exportPrep.addToPostImport( ExportPreparation.TAB );
         exportPrep.addToPostImport( ExportPreparation.TAB );
         exportPrep.addToPostImport( "public var " );
         exportPrep.addToPostImport( ctx.IDENT().getText() );
     }
 
     @Override public void exitAmbientVariableDeclaration(TypeScriptParser.AmbientVariableDeclarationContext ctx) {
         exportPrep.addToPostImport( SEMI_COLON );
     }
 
     @Override public void enterEqualityExpressionNoIn(TypeScriptParser.EqualityExpressionNoInContext ctx) { }
 
     @Override public void exitEqualityExpressionNoIn(TypeScriptParser.EqualityExpressionNoInContext ctx) { }
 
     @Override public void enterVariableDeclarationListNoIn(TypeScriptParser.VariableDeclarationListNoInContext ctx) { }
 
     @Override public void exitVariableDeclarationListNoIn(TypeScriptParser.VariableDeclarationListNoInContext ctx) { }
 
     @Override public void enterArrayLiteral(TypeScriptParser.ArrayLiteralContext ctx) { }
 
     @Override public void exitArrayLiteral(TypeScriptParser.ArrayLiteralContext ctx) { }
 
     @Override public void enterConstructorOverloads(TypeScriptParser.ConstructorOverloadsContext ctx) { }
 
     @Override public void exitConstructorOverloads(TypeScriptParser.ConstructorOverloadsContext ctx) { }
 
     @Override public void enterArrayType(TypeScriptParser.ArrayTypeContext ctx) {
         isArrayType = Boolean.TRUE;
     }
 
     @Override public void exitArrayType(TypeScriptParser.ArrayTypeContext ctx) {
         isArrayType = Boolean.FALSE;
     }
 
     @Override public void enterIndexSignature(TypeScriptParser.IndexSignatureContext ctx) { }
 
     @Override public void exitIndexSignature(TypeScriptParser.IndexSignatureContext ctx) { }
 
     @Override public void enterExpressionStatement(TypeScriptParser.ExpressionStatementContext ctx) { }
 
     @Override public void exitExpressionStatement(TypeScriptParser.ExpressionStatementContext ctx) { }
 
     @Override public void enterMemberAccessorDeclaration(TypeScriptParser.MemberAccessorDeclarationContext ctx) { }
 
     @Override public void exitMemberAccessorDeclaration(TypeScriptParser.MemberAccessorDeclarationContext ctx) { }
 
     @Override public void enterCloseParen(TypeScriptParser.CloseParenContext ctx) {
         exportPrep.addToPostImport( " )" );
     }
 
     @Override public void exitCloseParen(TypeScriptParser.CloseParenContext ctx) { }
 
     @Override public void enterModuleElements(TypeScriptParser.ModuleElementsContext ctx) { }
 
     @Override public void exitModuleElements(TypeScriptParser.ModuleElementsContext ctx) { }
 
     @Override public void enterAmbientModuleIdentification(TypeScriptParser.AmbientModuleIdentificationContext ctx) { }
 
     @Override public void exitAmbientModuleIdentification(TypeScriptParser.AmbientModuleIdentificationContext ctx) { }
 
     @Override public void enterParameterList(TypeScriptParser.ParameterListContext ctx) { }
 
     @Override public void exitParameterList(TypeScriptParser.ParameterListContext ctx) { }
 
     @Override public void enterMemberFunctionImplementation(TypeScriptParser.MemberFunctionImplementationContext ctx) { }
 
     @Override public void exitMemberFunctionImplementation(TypeScriptParser.MemberFunctionImplementationContext ctx) { }
 
     @Override public void enterOpenParen(TypeScriptParser.OpenParenContext ctx) {
         exportPrep.addToPostImport( " ( " );
     }
 
     @Override public void exitOpenParen(TypeScriptParser.OpenParenContext ctx) { }
 
     @Override public void enterWithStatement(TypeScriptParser.WithStatementContext ctx) { }
 
     @Override public void exitWithStatement(TypeScriptParser.WithStatementContext ctx) { }
 
     @Override public void enterImportDeclaration(TypeScriptParser.ImportDeclarationContext ctx) { }
 
     @Override public void exitImportDeclaration(TypeScriptParser.ImportDeclarationContext ctx) { }
 
     @Override public void enterBitwiseORExpressionNoIn(TypeScriptParser.BitwiseORExpressionNoInContext ctx) { }
 
     @Override public void exitBitwiseORExpressionNoIn(TypeScriptParser.BitwiseORExpressionNoInContext ctx) { }
 
     @Override public void enterBitwiseXORExpression(TypeScriptParser.BitwiseXORExpressionContext ctx) { }
 
     @Override public void exitBitwiseXORExpression(TypeScriptParser.BitwiseXORExpressionContext ctx) { }
 
     @Override public void enterAmbientClassBodyElements(TypeScriptParser.AmbientClassBodyElementsContext ctx) { }
 
     @Override public void exitAmbientClassBodyElements(TypeScriptParser.AmbientClassBodyElementsContext ctx) { }
 
     @Override public void enterAssignmentExpression(TypeScriptParser.AssignmentExpressionContext ctx) { }
 
     @Override public void exitAssignmentExpression(TypeScriptParser.AssignmentExpressionContext ctx) { }
 
     @Override public void enterVariableDeclarationNoIn(TypeScriptParser.VariableDeclarationNoInContext ctx) { }
 
     @Override public void exitVariableDeclarationNoIn(TypeScriptParser.VariableDeclarationNoInContext ctx) { }
 
     @Override public void enterAmbientDeclaration(TypeScriptParser.AmbientDeclarationContext ctx) { }
 
     @Override public void exitAmbientDeclaration(TypeScriptParser.AmbientDeclarationContext ctx) { }
 
     @Override public void enterConstructorType(TypeScriptParser.ConstructorTypeContext ctx) { }
 
     @Override public void exitConstructorType(TypeScriptParser.ConstructorTypeContext ctx) { }
 
     @Override public void enterAmbientModuleBody(TypeScriptParser.AmbientModuleBodyContext ctx) { }
 
     @Override public void exitAmbientModuleBody(TypeScriptParser.AmbientModuleBodyContext ctx) { }
 
     @Override public void enterOpenBrace(TypeScriptParser.OpenBraceContext ctx) {
         exportPrep.addToPostImport( " " );
         exportPrep.addToPostImport( OPEN_BRACE );
         exportPrep.addToPostImport( ExportPreparation.LINE_BREAK );
     }
 
     @Override public void exitOpenBrace(TypeScriptParser.OpenBraceContext ctx) { }
 
     @Override public void enterDefaultClause(TypeScriptParser.DefaultClauseContext ctx) { }
 
     @Override public void exitDefaultClause(TypeScriptParser.DefaultClauseContext ctx) { }
 
     @Override public void enterTypeMemberList(TypeScriptParser.TypeMemberListContext ctx) { }
 
     @Override public void exitTypeMemberList(TypeScriptParser.TypeMemberListContext ctx) {
         exportPrep.addToPostImport( SEMI_COLON );
         exportPrep.addToPostImport( ExportPreparation.LINE_BREAK );
     }
 
     @Override public void enterBitwiseANDExpression(TypeScriptParser.BitwiseANDExpressionContext ctx) { }
 
     @Override public void exitBitwiseANDExpression(TypeScriptParser.BitwiseANDExpressionContext ctx) { }
 
     @Override public void enterAssignmentExpressionNoIn(TypeScriptParser.AssignmentExpressionNoInContext ctx) { }
 
     @Override public void exitAssignmentExpressionNoIn(TypeScriptParser.AssignmentExpressionNoInContext ctx) { }
 
     @Override public void enterCatchClause(TypeScriptParser.CatchClauseContext ctx) { }
 
     @Override public void exitCatchClause(TypeScriptParser.CatchClauseContext ctx) { }
 
     @Override public void enterCaseBlock(TypeScriptParser.CaseBlockContext ctx) { }
 
     @Override public void exitCaseBlock(TypeScriptParser.CaseBlockContext ctx) { }
 
     @Override public void enterPropertySignature(TypeScriptParser.PropertySignatureContext ctx) {
         exportPrep.addToPostImport( ExportPreparation.LINE_BREAK );
         exportPrep.addToPostImport( ExportPreparation.TAB );
 
         //NOTE: This is here for AS, for now, since AS doesn't allow variable declarations in Interfaces.
         if ( isInterface ) {
             exportPrep.addToPostImport( "//" );
         }
 
         exportPrep.addToPostImport( ctx.IDENT().getText() );
 
     }
 
     @Override public void exitPropertySignature(TypeScriptParser.PropertySignatureContext ctx) { }
 
     @Override public void enterBreakStatement(TypeScriptParser.BreakStatementContext ctx) { }
 
     @Override public void exitBreakStatement(TypeScriptParser.BreakStatementContext ctx) { }
 
     @Override public void enterObjectLiteral(TypeScriptParser.ObjectLiteralContext ctx) { }
 
     @Override public void exitObjectLiteral(TypeScriptParser.ObjectLiteralContext ctx) { }
 
     @Override public void enterFunctionOverload(TypeScriptParser.FunctionOverloadContext ctx) { }
 
     @Override public void exitFunctionOverload(TypeScriptParser.FunctionOverloadContext ctx) { }
 
     @Override public void enterFinallyClause(TypeScriptParser.FinallyClauseContext ctx) { }
 
     @Override public void exitFinallyClause(TypeScriptParser.FinallyClauseContext ctx) { }
 
     @Override public void enterOptionalParameterList(TypeScriptParser.OptionalParameterListContext ctx) { }
 
     @Override public void exitOptionalParameterList(TypeScriptParser.OptionalParameterListContext ctx) { }
 
     @Override public void enterVariableStatement(TypeScriptParser.VariableStatementContext ctx) { }
 
     @Override public void exitVariableStatement(TypeScriptParser.VariableStatementContext ctx) { }
 
     @Override public void enterPropertyNameAndValueList(TypeScriptParser.PropertyNameAndValueListContext ctx) { }
 
     @Override public void exitPropertyNameAndValueList(TypeScriptParser.PropertyNameAndValueListContext ctx) { }
 
     @Override public void enterBlock(TypeScriptParser.BlockContext ctx) { }
 
     @Override public void exitBlock(TypeScriptParser.BlockContext ctx) { }
 
     @Override public void enterCallExpression(TypeScriptParser.CallExpressionContext ctx) { }
 
     @Override public void exitCallExpression(TypeScriptParser.CallExpressionContext ctx) { }
 
     @Override public void enterPublicOrPrivate(TypeScriptParser.PublicOrPrivateContext ctx) {
         exportPrep.addToPostImport( ctx.getText() );
     }
 
     @Override public void exitPublicOrPrivate(TypeScriptParser.PublicOrPrivateContext ctx) {
         exportPrep.addToPostImport( " " );
     }
 
     @Override public void enterRequiredParameterList(TypeScriptParser.RequiredParameterListContext ctx) { }
 
     @Override public void exitRequiredParameterList(TypeScriptParser.RequiredParameterListContext ctx) { }
 
     @Override public void enterConditionalExpression(TypeScriptParser.ConditionalExpressionContext ctx) { }
 
     @Override public void exitConditionalExpression(TypeScriptParser.ConditionalExpressionContext ctx) { }
 
     @Override public void enterInterfaceName(TypeScriptParser.InterfaceNameContext ctx) { }
 
     @Override public void exitInterfaceName(TypeScriptParser.InterfaceNameContext ctx) { }
 
     @Override public void enterFunctionType(TypeScriptParser.FunctionTypeContext ctx) { }
 
     @Override public void exitFunctionType(TypeScriptParser.FunctionTypeContext ctx) { }
 
     @Override public void enterBitwiseORExpression(TypeScriptParser.BitwiseORExpressionContext ctx) { }
 
     @Override public void exitBitwiseORExpression(TypeScriptParser.BitwiseORExpressionContext ctx) { }
 
     @Override public void enterAmbientClassBody(TypeScriptParser.AmbientClassBodyContext ctx) { }
 
     @Override public void exitAmbientClassBody(TypeScriptParser.AmbientClassBodyContext ctx) { }
 
     @Override public void enterDeclarationSourceFile(TypeScriptParser.DeclarationSourceFileContext ctx) { }
 
     @Override public void exitDeclarationSourceFile(TypeScriptParser.DeclarationSourceFileContext ctx) { }
 
     @Override public void enterIterationStatement(TypeScriptParser.IterationStatementContext ctx) { }
 
     @Override public void exitIterationStatement(TypeScriptParser.IterationStatementContext ctx) { }
 
     @Override public void enterTypeAnnotation(TypeScriptParser.TypeAnnotationContext ctx) {
        if ( !isARestParameter ) {
            exportPrep.addToPostImport( ": " );
        }
     }
 
     @Override public void exitTypeAnnotation(TypeScriptParser.TypeAnnotationContext ctx) { }
 
     @Override public void enterClassElement(TypeScriptParser.ClassElementContext ctx) { }
 
     @Override public void exitClassElement(TypeScriptParser.ClassElementContext ctx) { }
 
     @Override public void enterReturnTypeAnnotation(TypeScriptParser.ReturnTypeAnnotationContext ctx) {
         exportPrep.addToPostImport( ": " );
     }
 
     @Override public void exitReturnTypeAnnotation(TypeScriptParser.ReturnTypeAnnotationContext ctx) { }
 
     @Override public void enterModuleName(TypeScriptParser.ModuleNameContext ctx) { }
 
     @Override public void exitModuleName(TypeScriptParser.ModuleNameContext ctx) { }
 
     @Override public void enterModuleReference(TypeScriptParser.ModuleReferenceContext ctx) { }
 
     @Override public void exitModuleReference(TypeScriptParser.ModuleReferenceContext ctx) { }
 
     @Override public void enterClassDeclaration(TypeScriptParser.ClassDeclarationContext ctx) { }
 
     @Override public void exitClassDeclaration(TypeScriptParser.ClassDeclarationContext ctx) { }
 
     @Override public void enterImplementationSourceFile(TypeScriptParser.ImplementationSourceFileContext ctx) { }
 
     @Override public void exitImplementationSourceFile(TypeScriptParser.ImplementationSourceFileContext ctx) { }
 
     @Override public void enterTypeLiteral(TypeScriptParser.TypeLiteralContext ctx) {}
 
     @Override public void exitTypeLiteral(TypeScriptParser.TypeLiteralContext ctx) { }
 
     @Override public void enterBooleanLiteral(TypeScriptParser.BooleanLiteralContext ctx) { }
 
     @Override public void exitBooleanLiteral(TypeScriptParser.BooleanLiteralContext ctx) { }
 
     @Override public void enterClassHeritage(TypeScriptParser.ClassHeritageContext ctx) { }
 
     @Override public void exitClassHeritage(TypeScriptParser.ClassHeritageContext ctx) { }
 
     @Override public void enterGetAccessorSignature(TypeScriptParser.GetAccessorSignatureContext ctx) { }
 
     @Override public void exitGetAccessorSignature(TypeScriptParser.GetAccessorSignatureContext ctx) { }
 
     @Override public void enterOptionalParameter(TypeScriptParser.OptionalParameterContext ctx) {
         exportPrep.addToPostImport( ctx.IDENT().getText() );
     }
 
     @Override public void exitOptionalParameter(TypeScriptParser.OptionalParameterContext ctx) {
         //See if the initialiser is here, if not, put null.
         if ( ctx.initialiser() == null ) {
             exportPrep.addToPostImport( " = " + "null" );
         }
     }
 
     @Override public void enterMemberExpression(TypeScriptParser.MemberExpressionContext ctx) { }
 
     @Override public void exitMemberExpression(TypeScriptParser.MemberExpressionContext ctx) { }
 
     @Override public void enterTypeName(TypeScriptParser.TypeNameContext ctx) { }
 
     @Override public void exitTypeName(TypeScriptParser.TypeNameContext ctx) { }
 
     @Override public void enterArrowFormalParameters(TypeScriptParser.ArrowFormalParametersContext ctx) { }
 
     @Override public void exitArrowFormalParameters(TypeScriptParser.ArrowFormalParametersContext ctx) { }
 
     @Override public void enterNullLiteral(TypeScriptParser.NullLiteralContext ctx) { }
 
     @Override public void exitNullLiteral(TypeScriptParser.NullLiteralContext ctx) { }
 
     @Override public void enterSwitchStatement(TypeScriptParser.SwitchStatementContext ctx) { }
 
     @Override public void exitSwitchStatement(TypeScriptParser.SwitchStatementContext ctx) { }
 
     @Override public void enterEqualityExpression(TypeScriptParser.EqualityExpressionContext ctx) { }
 
     @Override public void exitEqualityExpression(TypeScriptParser.EqualityExpressionContext ctx) { }
 
     @Override public void enterArguments(TypeScriptParser.ArgumentsContext ctx) { }
 
     @Override public void exitArguments(TypeScriptParser.ArgumentsContext ctx) { }
 
     @Override public void enterConstructorOverload(TypeScriptParser.ConstructorOverloadContext ctx) { }
 
     @Override public void exitConstructorOverload(TypeScriptParser.ConstructorOverloadContext ctx) { }
 
     @Override public void enterTypeBody(TypeScriptParser.TypeBodyContext ctx) { }
 
     @Override public void exitTypeBody(TypeScriptParser.TypeBodyContext ctx) { }
 
     @Override public void enterVariableDeclarationList(TypeScriptParser.VariableDeclarationListContext ctx) { }
 
     @Override public void exitVariableDeclarationList(TypeScriptParser.VariableDeclarationListContext ctx) { }
 
     @Override public void enterThrowStatement(TypeScriptParser.ThrowStatementContext ctx) { }
 
     @Override public void exitThrowStatement(TypeScriptParser.ThrowStatementContext ctx) { }
 
     @Override public void enterFunctionDeclaration(TypeScriptParser.FunctionDeclarationContext ctx) { }
 
     @Override public void exitFunctionDeclaration(TypeScriptParser.FunctionDeclarationContext ctx) { }
 
     @Override public void enterExpression(TypeScriptParser.ExpressionContext ctx) { }
 
     @Override public void exitExpression(TypeScriptParser.ExpressionContext ctx) { }
 
     @Override public void enterCallSignature(TypeScriptParser.CallSignatureContext ctx) { }
 
     @Override public void exitCallSignature(TypeScriptParser.CallSignatureContext ctx) { }
 
     @Override public void enterLeftHandSideExpression(TypeScriptParser.LeftHandSideExpressionContext ctx) { }
 
     @Override public void exitLeftHandSideExpression(TypeScriptParser.LeftHandSideExpressionContext ctx) { }
 
     @Override public void enterInterfaceNameList(TypeScriptParser.InterfaceNameListContext ctx) { }
 
     @Override public void exitInterfaceNameList(TypeScriptParser.InterfaceNameListContext ctx) { }
 
     @Override public void enterFunctionImplementation(TypeScriptParser.FunctionImplementationContext ctx) { }
 
     @Override public void exitFunctionImplementation(TypeScriptParser.FunctionImplementationContext ctx) { }
 
     @Override public void enterSourceElements(TypeScriptParser.SourceElementsContext ctx) { }
 
     @Override public void exitSourceElements(TypeScriptParser.SourceElementsContext ctx) { }
 
     @Override public void enterUnaryExpression(TypeScriptParser.UnaryExpressionContext ctx) { }
 
     @Override public void exitUnaryExpression(TypeScriptParser.UnaryExpressionContext ctx) { }
 
     @Override public void enterAmbientFunctionDeclaration(TypeScriptParser.AmbientFunctionDeclarationContext ctx) {
         exportPrep.addToPostImport( ExportPreparation.LINE_BREAK );
         exportPrep.addToPostImport( ExportPreparation.TAB );
         exportPrep.addToPostImport( ExportPreparation.TAB );
         //Since the grammar for ambientFunctionDeclaration and functionSignature force an IDENT, we can do this.
         exportPrep.addToPostImport( "[JavaScriptMethod (name=\"" + ctx.functionSignature().IDENT().getText() + "\")]" );
         exportPrep.addToPostImport( ExportPreparation.LINE_BREAK );
         exportPrep.addToPostImport( ExportPreparation.TAB );
         exportPrep.addToPostImport( ExportPreparation.TAB );
         exportPrep.addToPostImport( "public function " );
     }
 
     @Override public void exitAmbientFunctionDeclaration(TypeScriptParser.AmbientFunctionDeclarationContext ctx) {
         exportPrep.addToPostImport( " " );
         exportPrep.addToPostImport( OPEN_BRACE );
         exportPrep.addToPostImport( " " );
         exportPrep.addToPostImport( CLOSE_BRACE );
         exportPrep.addToPostImport( ExportPreparation.LINE_BREAK );
     }
 
     @Override public void enterAmbientElement(TypeScriptParser.AmbientElementContext ctx) { }
 
     @Override public void exitAmbientElement(TypeScriptParser.AmbientElementContext ctx) { }
 
     @Override public void enterReturnStatement(TypeScriptParser.ReturnStatementContext ctx) { }
 
     @Override public void exitReturnStatement(TypeScriptParser.ReturnStatementContext ctx) { }
 
     @Override public void enterPrimaryExpression(TypeScriptParser.PrimaryExpressionContext ctx) { }
 
     @Override public void exitPrimaryExpression(TypeScriptParser.PrimaryExpressionContext ctx) { }
 
     @Override public void enterType(TypeScriptParser.TypeContext ctx) { }
 
     @Override public void exitType(TypeScriptParser.TypeContext ctx) { }
 
     @Override public void enterCloseBrace(TypeScriptParser.CloseBraceContext ctx) {
         exportPrep.addToPostImport( CLOSE_BRACE );
         exportPrep.addToPostImport( ExportPreparation.LINE_BREAK );
     }
 
     @Override public void exitCloseBrace(TypeScriptParser.CloseBraceContext ctx) { }
 
     @Override public void enterLogicalANDExpression(TypeScriptParser.LogicalANDExpressionContext ctx) { }
 
     @Override public void exitLogicalANDExpression(TypeScriptParser.LogicalANDExpressionContext ctx) { }
 
     @Override public void enterMemberFunctionOverload(TypeScriptParser.MemberFunctionOverloadContext ctx) { }
 
     @Override public void exitMemberFunctionOverload(TypeScriptParser.MemberFunctionOverloadContext ctx) { }
 
     @Override public void enterRelationalExpressionNoIn(TypeScriptParser.RelationalExpressionNoInContext ctx) { }
 
     @Override public void exitRelationalExpressionNoIn(TypeScriptParser.RelationalExpressionNoInContext ctx) { }
 
     @Override public void enterVariableDeclaration(TypeScriptParser.VariableDeclarationContext ctx) { }
 
     @Override public void exitVariableDeclaration(TypeScriptParser.VariableDeclarationContext ctx) { }
 
     @Override public void enterArrowFunctionExpression(TypeScriptParser.ArrowFunctionExpressionContext ctx) { }
 
     @Override public void exitArrowFunctionExpression(TypeScriptParser.ArrowFunctionExpressionContext ctx) { }
 
     @Override public void enterMemberFunctionOverloads(TypeScriptParser.MemberFunctionOverloadsContext ctx) { }
 
     @Override public void exitMemberFunctionOverloads(TypeScriptParser.MemberFunctionOverloadsContext ctx) { }
 
     @Override public void enterMemberFunctionDeclaration(TypeScriptParser.MemberFunctionDeclarationContext ctx) { }
 
     @Override public void exitMemberFunctionDeclaration(TypeScriptParser.MemberFunctionDeclarationContext ctx) { }
 
     @Override public void enterClassExtendsClause(TypeScriptParser.ClassExtendsClauseContext ctx) {
         exportPrep.addToPostImport( " extends " + ctx.className().getText() );
     }
 
     @Override public void exitClassExtendsClause(TypeScriptParser.ClassExtendsClauseContext ctx) { }
 
     @Override public void enterBitwiseXORExpressionNoIn(TypeScriptParser.BitwiseXORExpressionNoInContext ctx) { }
 
     @Override public void exitBitwiseXORExpressionNoIn(TypeScriptParser.BitwiseXORExpressionNoInContext ctx) { }
 
     @Override public void enterCaseClauses(TypeScriptParser.CaseClausesContext ctx) { }
 
     @Override public void exitCaseClauses(TypeScriptParser.CaseClausesContext ctx) { }
 
     @Override public void enterShiftExpression(TypeScriptParser.ShiftExpressionContext ctx) { }
 
     @Override public void exitShiftExpression(TypeScriptParser.ShiftExpressionContext ctx) { }
 
     @Override public void enterExternalModuleReference(TypeScriptParser.ExternalModuleReferenceContext ctx) { }
 
     @Override public void exitExternalModuleReference(TypeScriptParser.ExternalModuleReferenceContext ctx) { }
 
     @Override public void enterInitialiserNoIn(TypeScriptParser.InitialiserNoInContext ctx) {
         exportPrep.addToPostImport( " = " + ctx.getText() );
     }
 
     @Override public void exitInitialiserNoIn(TypeScriptParser.InitialiserNoInContext ctx) { }
 
     @Override public void enterClassBody(TypeScriptParser.ClassBodyContext ctx) { }
 
     @Override public void exitClassBody(TypeScriptParser.ClassBodyContext ctx) { }
 
     @Override public void enterModuleElement(TypeScriptParser.ModuleElementContext ctx) { }
 
     @Override public void exitModuleElement(TypeScriptParser.ModuleElementContext ctx) { }
 
     @Override public void enterSetAccessorSignature(TypeScriptParser.SetAccessorSignatureContext ctx) { }
 
     @Override public void exitSetAccessorSignature(TypeScriptParser.SetAccessorSignatureContext ctx) { }
 
     @Override public void enterModuleBody(TypeScriptParser.ModuleBodyContext ctx) { }
 
     @Override public void exitModuleBody(TypeScriptParser.ModuleBodyContext ctx) { }
 
     @Override public void enterLabelledStatement(TypeScriptParser.LabelledStatementContext ctx) { }
 
     @Override public void exitLabelledStatement(TypeScriptParser.LabelledStatementContext ctx) { }
 
     @Override public void enterPropertyName(TypeScriptParser.PropertyNameContext ctx) { }
 
     @Override public void exitPropertyName(TypeScriptParser.PropertyNameContext ctx) { }
 
     @Override public void enterInterfaceDeclaration(TypeScriptParser.InterfaceDeclarationContext ctx) {
         //Reset the counter for overloads.
         resetOverloadTracker();
 
         isInterface = Boolean.TRUE;
         interfaceName = ctx.IDENT().getText();
 
         exportPrep.beginOutputFile( ctx.IDENT().getText() + ".as" );
         exportPrep.addToPreImport( "package " + exportPrep.packageStructure );
         exportPrep.addToPreImport( " " );
         exportPrep.addToPreImport( OPEN_BRACE );
         exportPrep.addToPreImport( ExportPreparation.LINE_BREAK );
 
         //Insert import statements
         if ( !( ctx.interfaceExtendsClause() == null ) ) {
             String imports = ctx.interfaceExtendsClause().interfaceNameList().getText();
             writeImports( imports );
         }
 
         exportPrep.addToPostImport( ExportPreparation.LINE_BREAK );
         exportPrep.addToPostImport( ExportPreparation.TAB );
         exportPrep.addToPostImport( "[JavaScript ( export=false )]");
         exportPrep.addToPostImport( ExportPreparation.LINE_BREAK );
         exportPrep.addToPostImport( ExportPreparation.TAB );
         exportPrep.addToPostImport( "public interface " + interfaceName );
     }
 
     @Override public void exitInterfaceDeclaration(TypeScriptParser.InterfaceDeclarationContext ctx) {
         isInterface = Boolean.FALSE;
         exportPrep.addToPostImport( CLOSE_BRACE );
         exportPrep.addToPostImport( ExportPreparation.LINE_BREAK );
         exportPrep.finishOutputFile();
     }
 
     @Override public void enterComma(TypeScriptParser.CommaContext ctx){
         exportPrep.addToPostImport( ", " );
     }
 
     @Override public void exitComma(TypeScriptParser.CommaContext ctx){}
 
     //Package that the class/interface/etc. is located in.
     @Override public void enterAmbientModuleDeclaration(TypeScriptParser.AmbientModuleDeclarationContext ctx) {
         exportPrep = new ExportPreparation();
 
         //Creates the directory structure for the Class and Interface files.
         exportPrep.createDirectories( ctx.ambientModuleIdentification().getText() );
         possibleClassName = ctx.ambientModuleIdentification().getText();
     }
 
     @Override public void exitAmbientModuleDeclaration(TypeScriptParser.AmbientModuleDeclarationContext ctx) {
         if ( isDefaultClass ) {
             exportPrep.addToPostImport( ExportPreparation.TAB );
             exportPrep.addToPostImport( "}" );
             exportPrep.addToPostImport( ExportPreparation.LINE_BREAK );
             exportPrep.addToPostImport( "}" );
             exportPrep.finishOutputFile();
         }
 
         isDefaultClass = Boolean.FALSE;
     }
 
     @Override public void enterAmbientElements(TypeScriptParser.AmbientElementsContext ctx) {
         //Check to see if either classes or interfaces exist after this. If not,
         // then this is the base class and we have to save it out as well.
         // This happens here and not at the enterAmbientElement level, since enterAmbientElement is called several
         // times for the same declaration of the class.
 
         Boolean classHasAnInterface = Boolean.FALSE;
         Boolean isADefaultClass = Boolean.TRUE;
         for ( int i = 0; i < ctx.getChildCount(); i++ ) {
             for ( int j = 0; j < ctx.getChild( i ).getChildCount(); j++ ) {
                 if ( ctx.getChild( i ).getChild(j) instanceof TypeScriptParser.InterfaceDeclarationContext ) {
                     isADefaultClass = Boolean.FALSE;
                     break;
                 } else if ( ctx.getChild( i ).getChild(j) instanceof TypeScriptParser.AmbientClassDeclarationContext ) {
                     isADefaultClass = Boolean.FALSE;
                     break;
                 }
             }
         }
 
         //This check is to see if it could be a default class, and we haven't already started saving the class.
         if ( isADefaultClass && !isDefaultClass ) {
             isDefaultClass = Boolean.TRUE;
             exportPrep.beginOutputFile( possibleClassName + ".as" );
 
             exportPrep.addToPreImport( "package " + exportPrep.packageStructure );
             exportPrep.addToPreImport( " " );
             exportPrep.addToPreImport( OPEN_BRACE );
             exportPrep.addToPreImport( ExportPreparation.LINE_BREAK );
             exportPrep.addToPostImport( ExportPreparation.LINE_BREAK );
             exportPrep.addToPostImport( ExportPreparation.TAB );
             exportPrep.addToPostImport("[JavaScript ( export=false )]");
             exportPrep.addToPostImport( ExportPreparation.LINE_BREAK );
             exportPrep.addToPostImport( ExportPreparation.TAB );
             exportPrep.addToPostImport( "public class " + possibleClassName );
             exportPrep.addToPostImport( " " );
             exportPrep.addToPostImport( OPEN_BRACE );
         }
     }
 
     @Override public void exitAmbientElements(TypeScriptParser.AmbientElementsContext ctx) { }
 
     @Override public void enterTypeMember(TypeScriptParser.TypeMemberContext ctx) { }
 
     @Override public void exitTypeMember(TypeScriptParser.TypeMemberContext ctx) { }
 
     @Override public void enterAmbientConstructorDeclaration(TypeScriptParser.AmbientConstructorDeclarationContext ctx) { }
 
     @Override public void exitAmbientConstructorDeclaration(TypeScriptParser.AmbientConstructorDeclarationContext ctx) { }
 
     @Override public void enterDebuggerStatement(TypeScriptParser.DebuggerStatementContext ctx) { }
 
     @Override public void exitDebuggerStatement(TypeScriptParser.DebuggerStatementContext ctx) { }
 
     @Override public void enterAdditiveExpression(TypeScriptParser.AdditiveExpressionContext ctx) { }
 
     @Override public void exitAdditiveExpression(TypeScriptParser.AdditiveExpressionContext ctx) { }
 
     @Override public void enterPredefinedType(TypeScriptParser.PredefinedTypeContext ctx) {
        if ( !isARestParameter ) {
            String aType = ctx.getText();
 
            aType = getASType( aType );
 
            exportPrep.addToPostImport( aType );
        }

        isARestParameter = Boolean.FALSE;
     }
 
     @Override public void exitPredefinedType(TypeScriptParser.PredefinedTypeContext ctx) { }
 
     @Override public void enterPostfixExpression(TypeScriptParser.PostfixExpressionContext ctx) { }
 
     @Override public void exitPostfixExpression(TypeScriptParser.PostfixExpressionContext ctx) { }
 
     @Override public void enterEmptyStatement(TypeScriptParser.EmptyStatementContext ctx) { }
 
     @Override public void exitEmptyStatement(TypeScriptParser.EmptyStatementContext ctx) { }
 
     @Override public void enterStatement(TypeScriptParser.StatementContext ctx) {
 //        System.out.println("enterStatement" + ctx);
     }
 
     @Override public void exitStatement(TypeScriptParser.StatementContext ctx) {
 //        System.out.println("exitStatement" + ctx);
     }
 
     @Override public void enterStatementList(TypeScriptParser.StatementListContext ctx) { }
 
     @Override public void exitStatementList(TypeScriptParser.StatementListContext ctx) { }
 
     @Override public void enterRestParameter(TypeScriptParser.RestParameterContext ctx) {
         exportPrep.addToPostImport( "..." );
        isARestParameter = Boolean.TRUE;
     }
 
     @Override public void exitRestParameter(TypeScriptParser.RestParameterContext ctx) { }
 
     @Override public void enterSourceFile(TypeScriptParser.SourceFileContext ctx) { }
 
     @Override public void exitSourceFile(TypeScriptParser.SourceFileContext ctx) { }
 
     @Override public void enterProgram(TypeScriptParser.ProgramContext ctx) {
         System.out.println("Entering Program" + ctx);
     }
 
     @Override public void exitProgram(TypeScriptParser.ProgramContext ctx) {
         System.out.println("Exiting Program" + ctx);
     }
 
     @Override public void enterLogicalORExpressionNoIn(TypeScriptParser.LogicalORExpressionNoInContext ctx) { }
 
     @Override public void exitLogicalORExpressionNoIn(TypeScriptParser.LogicalORExpressionNoInContext ctx) { }
 
     @Override public void enterElision(TypeScriptParser.ElisionContext ctx) { }
 
     @Override public void exitElision(TypeScriptParser.ElisionContext ctx) { }
 
     @Override public void enterMemberVariableDeclaration(TypeScriptParser.MemberVariableDeclarationContext ctx) { }
 
     @Override public void exitMemberVariableDeclaration(TypeScriptParser.MemberVariableDeclarationContext ctx) { }
 
     @Override public void enterCaseClause(TypeScriptParser.CaseClauseContext ctx) { }
 
     @Override public void exitCaseClause(TypeScriptParser.CaseClauseContext ctx) { }
 
     @Override public void enterRequiredParameter(TypeScriptParser.RequiredParameterContext ctx) {
         exportPrep.addToPostImport( ctx.IDENT().getText() );
     }
 
     @Override public void exitRequiredParameter(TypeScriptParser.RequiredParameterContext ctx) { }
 
     @Override public void enterConstructorImplementation(TypeScriptParser.ConstructorImplementationContext ctx) { }
 
     @Override public void exitConstructorImplementation(TypeScriptParser.ConstructorImplementationContext ctx) { }
 
     @Override public void enterNewExpression(TypeScriptParser.NewExpressionContext ctx) { }
 
     @Override public void exitNewExpression(TypeScriptParser.NewExpressionContext ctx) { }
 
     @Override public void enterModuleOrTypeName(TypeScriptParser.ModuleOrTypeNameContext ctx) {
        if ( !isARestParameter ) {
            String asType = getASType( ctx.getText() );

            exportPrep.addToPostImport( asType );
        }
 
        isARestParameter = Boolean.FALSE;
     }
 
     @Override public void exitModuleOrTypeName(TypeScriptParser.ModuleOrTypeNameContext ctx) { }
 
     @Override public void enterMultiplicativeExpression(TypeScriptParser.MultiplicativeExpressionContext ctx) { }
 
     @Override public void exitMultiplicativeExpression(TypeScriptParser.MultiplicativeExpressionContext ctx) { }
 
     @Override public void enterAssignmentOperator(TypeScriptParser.AssignmentOperatorContext ctx) { }
 
     @Override public void exitAssignmentOperator(TypeScriptParser.AssignmentOperatorContext ctx) { }
 
     @Override public void enterExpressionNoIn(TypeScriptParser.ExpressionNoInContext ctx) { }
 
     @Override public void exitExpressionNoIn(TypeScriptParser.ExpressionNoInContext ctx) { }
 
     @Override public void enterPropertyAssignment(TypeScriptParser.PropertyAssignmentContext ctx) { }
 
     @Override public void exitPropertyAssignment(TypeScriptParser.PropertyAssignmentContext ctx) { }
 
     @Override public void enterImplementsClause(TypeScriptParser.ImplementsClauseContext ctx) {
 //        System.out.println("enterClassExtendsClause: " + ctx.getText() );
 //        exportPrep.addToPostImport( " implements " + ctx.interfaceNameList().getText() );
     }
 
     @Override public void exitImplementsClause(TypeScriptParser.ImplementsClauseContext ctx) { }
 
     @Override public void enterConditionalExpressionNoIn(TypeScriptParser.ConditionalExpressionNoInContext ctx) { }
 
     @Override public void exitConditionalExpressionNoIn(TypeScriptParser.ConditionalExpressionNoInContext ctx) { }
 
     @Override public void enterPropertySetParameterList(TypeScriptParser.PropertySetParameterListContext ctx) { }
 
     @Override public void exitPropertySetParameterList(TypeScriptParser.PropertySetParameterListContext ctx) { }
 
     @Override public void enterBitwiseANDExpressionNoIn(TypeScriptParser.BitwiseANDExpressionNoInContext ctx) { }
 
     @Override public void exitBitwiseANDExpressionNoIn(TypeScriptParser.BitwiseANDExpressionNoInContext ctx) { }
 
     @Override public void enterLogicalANDExpressionNoIn(TypeScriptParser.LogicalANDExpressionNoInContext ctx) { }
 
     @Override public void exitLogicalANDExpressionNoIn(TypeScriptParser.LogicalANDExpressionNoInContext ctx) { }
 
     @Override public void enterElementList(TypeScriptParser.ElementListContext ctx) { }
 
     @Override public void exitElementList(TypeScriptParser.ElementListContext ctx) { }
 
     @Override public void enterMemberDeclaration(TypeScriptParser.MemberDeclarationContext ctx) { }
 
     @Override public void exitMemberDeclaration(TypeScriptParser.MemberDeclarationContext ctx) { }
 
     @Override public void enterFunctionSignature(TypeScriptParser.FunctionSignatureContext ctx) {
         String functionName = checkForOverloads( ctx.IDENT().getText() );
         //TODO:: putting 'function' here isn't right. In TS interfaces evidently the 'function' keyword is not used?
         //  This will need to change later.
         if ( isInterface ) {
             exportPrep.addToPostImport( ExportPreparation.LINE_BREAK );
             exportPrep.addToPostImport( ExportPreparation.TAB );
             exportPrep.addToPostImport( ExportPreparation.TAB );
             exportPrep.addToPostImport( "[JavaScriptMethod (name=\"" + ctx.IDENT().getText() + "\")]" );
             exportPrep.addToPostImport( ExportPreparation.LINE_BREAK );
             exportPrep.addToPostImport( ExportPreparation.TAB );
             exportPrep.addToPostImport( ExportPreparation.TAB );
             exportPrep.addToPostImport( "function " );
         }
 
         exportPrep.addToPostImport( functionName );
     }
 
     @Override public void exitFunctionSignature(TypeScriptParser.FunctionSignatureContext ctx) { }
 
     @Override public void enterTryStatement(TypeScriptParser.TryStatementContext ctx) { }
 
     @Override public void exitTryStatement(TypeScriptParser.TryStatementContext ctx) { }
 
     @Override public void enterRelationalExpression(TypeScriptParser.RelationalExpressionContext ctx) { }
 
     @Override public void exitRelationalExpression(TypeScriptParser.RelationalExpressionContext ctx) { }
 
     @Override public void enterAmbientStaticDeclaration(TypeScriptParser.AmbientStaticDeclarationContext ctx) {
         exportPrep.addToPostImport( ExportPreparation.LINE_BREAK );
         exportPrep.addToPostImport( ExportPreparation.TAB );
         exportPrep.addToPostImport( ExportPreparation.TAB );
 
         //TODO:: Need to determine if the next token set is a function or a property.
         //Since the grammar for ambientFunctionDeclaration and functionSignature force an IDENT, we can do this.
         exportPrep.addToPostImport( "[JavaScriptMethod (name=\"" + ctx.functionSignature().IDENT().getText() + "\")]" );
         exportPrep.addToPostImport( ExportPreparation.LINE_BREAK );
         exportPrep.addToPostImport( ExportPreparation.TAB );
         exportPrep.addToPostImport( ExportPreparation.TAB );
         exportPrep.addToPostImport( "public static " );
 
         //NOTE:: The ExtJS declaration file doesn't have any properties in it's class definitions. Hardcoding this for now.
         exportPrep.addToPostImport( "function " );
     }
 
     @Override public void exitAmbientStaticDeclaration(TypeScriptParser.AmbientStaticDeclarationContext ctx) {
         exportPrep.addToPostImport( " " );
         //This is done here, since in the .d.ts files, there is no actual function body. So I can't place this content inside the funciton body.
         // In AS, if there is no return declared in a method that has a return type, there will be a compiler error.
         exportPrep.addToPostImport( OPEN_BRACE );
         if ( !returnType.equals( "void" ) ) {
             exportPrep.addToPostImport( ExportPreparation.LINE_BREAK );
 //            exportPrep.writeToFile( getTabIndentLevel( 1 ) );
             exportPrep.addToPostImport( ExportPreparation.TAB );
             exportPrep.addToPostImport( ExportPreparation.TAB );
             exportPrep.addToPostImport( ExportPreparation.TAB );
             exportPrep.addToPostImport( "return " + "null" + ";");
             exportPrep.addToPostImport( ExportPreparation.LINE_BREAK );
             exportPrep.addToPostImport( ExportPreparation.TAB );
             exportPrep.addToPostImport( ExportPreparation.TAB );
 
         } else {
             exportPrep.addToPostImport( " " );
         }
         exportPrep.addToPostImport( CLOSE_BRACE );
 
         exportPrep.addToPostImport( ExportPreparation.LINE_BREAK );
     }
 
     @Override public void enterClassName(TypeScriptParser.ClassNameContext ctx) { }
 
     @Override public void exitClassName(TypeScriptParser.ClassNameContext ctx) { }
 
     @Override public void enterClassElements(TypeScriptParser.ClassElementsContext ctx) { }
 
     @Override public void exitClassElements(TypeScriptParser.ClassElementsContext ctx) { }
 
     @Override public void enterFunctionExpression(TypeScriptParser.FunctionExpressionContext ctx) { }
 
     @Override public void exitFunctionExpression(TypeScriptParser.FunctionExpressionContext ctx) { }
 
     @Override public void enterConstructorDeclaration(TypeScriptParser.ConstructorDeclarationContext ctx) { }
 
     @Override public void exitConstructorDeclaration(TypeScriptParser.ConstructorDeclarationContext ctx) { }
 
     @Override public void enterConstructSignature(TypeScriptParser.ConstructSignatureContext ctx) { }
 
     @Override public void exitConstructSignature(TypeScriptParser.ConstructSignatureContext ctx) { }
 
     @Override public void enterFunctionBody(TypeScriptParser.FunctionBodyContext ctx) { }
 
     @Override public void exitFunctionBody(TypeScriptParser.FunctionBodyContext ctx) { }
 
     @Override public void enterInterfaceExtendsClause(TypeScriptParser.InterfaceExtendsClauseContext ctx) {
         exportPrep.addToPostImport( " extends " );
     }
 
     @Override public void exitInterfaceExtendsClause(TypeScriptParser.InterfaceExtendsClauseContext ctx) { }
 
     @Override public void enterAmbientClassDeclaration(TypeScriptParser.AmbientClassDeclarationContext ctx) {
         //Reset the counter for overloads.
         resetOverloadTracker();
 
         exportPrep.beginOutputFile( ctx.IDENT().getText() + ".as" );
 
         exportPrep.addToPreImport( "package " + exportPrep.packageStructure );
         exportPrep.addToPreImport( " " );
         exportPrep.addToPreImport( OPEN_BRACE );
         //Imports are added in here.
         exportPrep.addToPostImport( ExportPreparation.LINE_BREAK );
         exportPrep.addToPostImport( ExportPreparation.LINE_BREAK );
         exportPrep.addToPostImport( ExportPreparation.TAB );
         exportPrep.addToPostImport("[JavaScript ( export=false )]");
         exportPrep.addToPostImport( ExportPreparation.LINE_BREAK );
         exportPrep.addToPostImport( ExportPreparation.TAB );
         exportPrep.addToPostImport( "public class " + ctx.IDENT().getText() );
 
         //NOTE:: Major assumption here is that all classes have an interface, and that the interface is declared
         // before the class in the definition file.
         // We have to do this, since the class doesn't implement the interface explicitly as you would expect.
         exportPrep.addToPostImport( " " );
         exportPrep.addToPostImport( "implements" );
         exportPrep.addToPostImport( " " );
         exportPrep.addToPostImport( interfaceName );
     }
 
     @Override public void exitAmbientClassDeclaration(TypeScriptParser.AmbientClassDeclarationContext ctx) {
         exportPrep.addToPostImport( ExportPreparation.LINE_BREAK );
         exportPrep.addToPostImport( CLOSE_BRACE );
         exportPrep.addToPostImport( ExportPreparation.LINE_BREAK );
         exportPrep.finishOutputFile();
     }
 
     @Override public void enterContinueStatement(TypeScriptParser.ContinueStatementContext ctx) { }
 
     @Override public void exitContinueStatement(TypeScriptParser.ContinueStatementContext ctx) { }
 
     @Override public void enterArgumentList(TypeScriptParser.ArgumentListContext ctx) { }
 
     @Override public void exitArgumentList(TypeScriptParser.ArgumentListContext ctx) { }
 
     @Override public void enterAmbientClassBodyElement(TypeScriptParser.AmbientClassBodyElementContext ctx) { }
 
     @Override public void exitAmbientClassBodyElement(TypeScriptParser.AmbientClassBodyElementContext ctx) { }
 
     @Override public void enterReturnType(TypeScriptParser.ReturnTypeContext ctx) {
         returnType = ctx.getText();
         if ( returnType.equals( "void" ) ) //The rest is handled in enterType
         {
             exportPrep.addToPostImport( returnType );
         }
     }
 
     @Override public void exitReturnType(TypeScriptParser.ReturnTypeContext ctx) { }
 
     @Override public void enterIfStatement(TypeScriptParser.IfStatementContext ctx) { }
 
     @Override public void exitIfStatement(TypeScriptParser.IfStatementContext ctx) { }
 
     @Override public void enterAmbientMemberDeclaration(TypeScriptParser.AmbientMemberDeclarationContext ctx) {
         //TODO:: Not actually called
         exportPrep.addToPostImport( ExportPreparation.LINE_BREAK );
         exportPrep.addToPostImport( ExportPreparation.TAB );
     }
 
     @Override public void exitAmbientMemberDeclaration(TypeScriptParser.AmbientMemberDeclarationContext ctx) {
         exportPrep.addToPostImport( ExportPreparation.LINE_BREAK );
     }
 
     @Override public void enterExportDeclaration(TypeScriptParser.ExportDeclarationContext ctx) {
         System.out.println( "enterExportDeclaration: " + ctx.getText() );
     }
 
     @Override public void exitExportDeclaration(TypeScriptParser.ExportDeclarationContext ctx) { }
 
     @Override public void enterInitialiser(TypeScriptParser.InitialiserContext ctx) {
         exportPrep.addToPostImport( " = " + ctx.getText() );
     }
 
     @Override public void exitInitialiser(TypeScriptParser.InitialiserContext ctx) { }
 
     @Override public void enterFunctionOverloads(TypeScriptParser.FunctionOverloadsContext ctx) { }
 
     @Override public void exitFunctionOverloads(TypeScriptParser.FunctionOverloadsContext ctx) { }
 
     @Override public void enterLogicalORExpression(TypeScriptParser.LogicalORExpressionContext ctx) { }
 
     @Override public void exitLogicalORExpression(TypeScriptParser.LogicalORExpressionContext ctx) { }
 
     @Override public void enterSourceElement(TypeScriptParser.SourceElementContext ctx) { }
 
     @Override public void exitSourceElement(TypeScriptParser.SourceElementContext ctx) { }
 
     @Override public void enterObjectType(TypeScriptParser.ObjectTypeContext ctx) {
         exportPrep.addToPostImport( " " + OPEN_BRACE );
         exportPrep.addToPostImport( ExportPreparation.LINE_BREAK );
     }
 
     @Override public void exitObjectType(TypeScriptParser.ObjectTypeContext ctx) {
         exportPrep.addToPostImport( CLOSE_BRACE );
         exportPrep.addToPostImport( ExportPreparation.LINE_BREAK );
     }
 
     @Override public void enterModuleDeclaration(TypeScriptParser.ModuleDeclarationContext ctx) { }
 
     @Override public void exitModuleDeclaration(TypeScriptParser.ModuleDeclarationContext ctx) { }
 
     @Override public void enterLiteral(TypeScriptParser.LiteralContext ctx) { }
 
     @Override public void exitLiteral(TypeScriptParser.LiteralContext ctx) { }
 
     @Override public void enterEveryRule(ParserRuleContext ctx) { }
 
     @Override public void exitEveryRule(ParserRuleContext ctx) { }
 
     @Override public void visitTerminal(TerminalNode node) { }
 
     @Override public void visitErrorNode(ErrorNode node) { }
 }
