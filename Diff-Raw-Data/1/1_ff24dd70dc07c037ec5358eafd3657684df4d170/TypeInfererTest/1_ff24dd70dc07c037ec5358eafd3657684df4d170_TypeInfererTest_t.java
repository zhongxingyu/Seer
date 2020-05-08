 package org.zwobble.shed.compiler.typechecker;
 
 import java.util.Collections;
 
 import org.hamcrest.Matcher;
 import org.hamcrest.Matchers;
 import org.junit.Test;
 import org.zwobble.shed.compiler.CompilerErrorDescription;
 import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
 import org.zwobble.shed.compiler.parsing.nodes.CallNode;
 import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
 import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
 import org.zwobble.shed.compiler.parsing.nodes.GlobalDeclaration;
 import org.zwobble.shed.compiler.parsing.nodes.LongLambdaExpressionNode;
 import org.zwobble.shed.compiler.parsing.nodes.MemberAccessNode;
 import org.zwobble.shed.compiler.parsing.nodes.Nodes;
 import org.zwobble.shed.compiler.parsing.nodes.NumberLiteralNode;
 import org.zwobble.shed.compiler.parsing.nodes.ReturnNode;
 import org.zwobble.shed.compiler.parsing.nodes.ShortLambdaExpressionNode;
 import org.zwobble.shed.compiler.parsing.nodes.StringLiteralNode;
 import org.zwobble.shed.compiler.parsing.nodes.TypeApplicationNode;
 import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
 import org.zwobble.shed.compiler.typechecker.errors.InvalidAssignmentError;
 import org.zwobble.shed.compiler.typechecker.errors.NotCallableError;
 import org.zwobble.shed.compiler.typechecker.errors.TypeMismatchError;
 import org.zwobble.shed.compiler.typechecker.errors.UntypedReferenceError;
 import org.zwobble.shed.compiler.types.ClassType;
 import org.zwobble.shed.compiler.types.CoreTypes;
 import org.zwobble.shed.compiler.types.FormalTypeParameter;
 import org.zwobble.shed.compiler.types.InterfaceType;
 import org.zwobble.shed.compiler.types.Interfaces;
 import org.zwobble.shed.compiler.types.ParameterisedFunctionType;
 import org.zwobble.shed.compiler.types.ParameterisedType;
 import org.zwobble.shed.compiler.types.ScalarType;
 import org.zwobble.shed.compiler.types.ScalarTypeInfo;
 import org.zwobble.shed.compiler.types.Type;
 
 import static org.zwobble.shed.compiler.types.FormalTypeParameter.invariantFormalTypeParameter;
 
 import static java.util.Arrays.asList;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 import static org.zwobble.shed.compiler.CompilerErrors.error;
 import static org.zwobble.shed.compiler.CompilerTesting.errorStrings;
 import static org.zwobble.shed.compiler.CompilerTesting.isFailureWithErrors;
 import static org.zwobble.shed.compiler.CompilerTesting.isSuccess;
 import static org.zwobble.shed.compiler.Option.none;
 import static org.zwobble.shed.compiler.Option.some;
 import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
 import static org.zwobble.shed.compiler.parsing.nodes.GlobalDeclaration.globalDeclaration;
 import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
 import static org.zwobble.shed.compiler.typechecker.TypeResult.success;
 import static org.zwobble.shed.compiler.typechecker.ValueInfo.assignableValue;
 import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;
 import static org.zwobble.shed.compiler.types.Interfaces.interfaces;
 import static org.zwobble.shed.compiler.types.Members.members;
 import static org.zwobble.shed.compiler.types.ParameterisedType.parameterisedType;
 import static org.zwobble.shed.compiler.types.TypeApplication.applyTypes;
 import static org.zwobble.shed.compiler.types.Types.typeParameters;
 
 public class TypeInfererTest {
     private final TypeCheckerTestFixture fixture = TypeCheckerTestFixture.build();
 
     private final VariableIdentifierNode doubleReference = fixture.doubleTypeReference();
     private final VariableIdentifierNode stringReference = fixture.stringTypeReference();
     private final VariableIdentifierNode booleanReference = fixture.booleanTypeReference();
     
     @Test public void
     canInferTypeOfBooleanLiteralsAsBoolean() {
         assertThat(inferType(new BooleanLiteralNode(true), standardContext()), isType(CoreTypes.BOOLEAN));
         assertThat(inferType(new BooleanLiteralNode(false), standardContext()), isType(CoreTypes.BOOLEAN));
     }
     
     @Test public void
     canInferTypeOfNumberLiteralsAsNumber() {
         assertThat(inferType(new NumberLiteralNode("2.2"), standardContext()), isType(CoreTypes.DOUBLE));
     }
     
     @Test public void
     canInferTypeOfStringLiteralsAsString() {
         assertThat(inferType(new StringLiteralNode("Everything's as if we never said"), standardContext()), isType(CoreTypes.STRING));
     }
     
     @Test public void
     canInferTypeOfUnitLiteralsAsUnit() {
         assertThat(inferType(Nodes.unit(), standardContext()), isType(CoreTypes.UNIT));
     }
     
     @Test public void
     variableReferencesHaveTypeOfVariable() {
         VariableIdentifierNode reference = new VariableIdentifierNode("value");
         GlobalDeclaration declaration = globalDeclaration("value");
         fixture.addReference(reference, declaration);
         StaticContext context = standardContext();
         context.add(declaration, unassignableValue(CoreTypes.STRING));
         assertThat(inferType(reference, context), isType(CoreTypes.STRING));
     }
     
     @Test public void
     cannotReferToVariableNotInContext() {
         VariableIdentifierNode node = new VariableIdentifierNode("value");
         TypeResult<Type> result = inferType(node, standardContext());
         assertThat(result, is((Object)failure(error(node, new UntypedReferenceError("value")))));
     }
     
     @Test public void
     canInferTypeOfShortLambdaExpressionWithoutArgumentsNorExplicitReturnType() {
         ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
             Collections.<FormalArgumentNode>emptyList(),
             none(ExpressionNode.class),
             new NumberLiteralNode("42")
         );
         TypeResult<Type> result = inferType(functionExpression, standardContext());
         assertThat(result, is(success((Type)CoreTypes.functionTypeOf(CoreTypes.DOUBLE))));
     }
     
     @Test public void
     errorIfCannotTypeBodyOfShortLambdaExpression() {
         ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
             Collections.<FormalArgumentNode>emptyList(),
             none(ExpressionNode.class),
             new VariableIdentifierNode("blah")
         );
         TypeResult<Type> result = inferType(functionExpression, standardContext());
         assertThat(errorStrings(result), is(asList("Could not determine type of reference: blah")));
     }
     
     @Test public void
     errorIfTypeSpecifierAndTypeBodyOfShortLambdaExpressionDoNotAgree() {
         NumberLiteralNode body = new NumberLiteralNode("42");
         ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
             Collections.<FormalArgumentNode>emptyList(),
             some(stringReference),
             body
         );
         TypeResult<Type> result = inferType(functionExpression, standardContext());
         assertThat(
             result.getErrors(),
             is(asList(error(body, new TypeMismatchError(CoreTypes.STRING, CoreTypes.DOUBLE))))
         );
     }
     
     @Test public void
     errorIfCannotFindArgumentType() {
         ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
             asList(
                 new FormalArgumentNode("name", new VariableIdentifierNode("Name")),
                 new FormalArgumentNode("age", doubleReference),
                 new FormalArgumentNode("address", new VariableIdentifierNode("Address"))
             ),
             none(ExpressionNode.class),
             new BooleanLiteralNode(true)
         );
         TypeResult<Type> result = inferType(functionExpression, standardContext());
         assertThat(result, isFailureWithErrors(new UntypedReferenceError("Name"), new UntypedReferenceError("Address")));
     }
     
     @Test public void
     errorIfCannotFindReturnType() {
         StaticContext context = standardContext();
         ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
             Collections.<FormalArgumentNode>emptyList(),
             some(new VariableIdentifierNode("String")),
             new NumberLiteralNode("42")
         );
         TypeResult<Type> result = inferType(functionExpression, context);
         CompilerErrorDescription[] errorsArray = { new UntypedReferenceError("String") };
         assertThat(result, isFailureWithErrors(errorsArray));
     }
     
     @Test public void
     canInferTypesOfArgumentsOfShortLambdaExpression() {
         StaticContext context = standardContext();
         
         ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
             asList(new FormalArgumentNode("name", stringReference), new FormalArgumentNode("age", doubleReference)),
             none(ExpressionNode.class),
             new BooleanLiteralNode(true)
         );
         TypeResult<Type> result = inferType(functionExpression, context);
         assertThat(result, is(success((Type)CoreTypes.functionTypeOf(CoreTypes.STRING, CoreTypes.DOUBLE, CoreTypes.BOOLEAN))));
     }
     
     @Test public void
     canFindTypeOfLongLambdaExpression() {
         LongLambdaExpressionNode functionExpression = new LongLambdaExpressionNode(
             asList(
                 new FormalArgumentNode("name", stringReference),
                 new FormalArgumentNode("age", doubleReference)
             ),
             booleanReference,
             Nodes.block(new ReturnNode(new BooleanLiteralNode(true)))
         );
         TypeResult<Type> result = inferType(functionExpression, standardContext());
         assertThat(result, is(success((Type)CoreTypes.functionTypeOf(CoreTypes.STRING, CoreTypes.DOUBLE, CoreTypes.BOOLEAN))));
     }
     
     @Test public void
     bodyOfLongLambdaExpressionIsTypeChecked() {
         LongLambdaExpressionNode functionExpression = new LongLambdaExpressionNode(
             Collections.<FormalArgumentNode>emptyList(),
             booleanReference,
             Nodes.block(
                 Nodes.immutableVar("x", stringReference, Nodes.bool(true)),
                 new ReturnNode(new BooleanLiteralNode(true))
             )
         );
         TypeResult<Type> result = inferType(functionExpression, standardContext());
         assertThat(result, isFailureWithErrors(new TypeMismatchError(CoreTypes.STRING, CoreTypes.BOOLEAN)));
     }
     
     @Test public void
     bodyOfLongLambdaExpressionMustReturnExpressionOfTypeSpecifiedInSignature() {
         LongLambdaExpressionNode functionExpression = new LongLambdaExpressionNode(
             Collections.<FormalArgumentNode>emptyList(),
             booleanReference,
             Nodes.block(
                 new ReturnNode(new NumberLiteralNode("4.2"))
             )
         );
         TypeResult<Type> result = inferType(functionExpression, standardContext());
         assertThat(errorStrings(result), is(asList("Expected return expression of type \"Boolean\" but was of type \"Double\"")));
     }
     
     @Test public void
     longLambdaExpressionAddsArgumentsToFunctionScope() {
         FormalArgumentNode ageArgument = new FormalArgumentNode("age", doubleReference);
         VariableIdentifierNode ageReference = new VariableIdentifierNode("age");
         fixture.addReference(ageReference, ageArgument);
         
         LongLambdaExpressionNode functionExpression = new LongLambdaExpressionNode(
             asList(
                 new FormalArgumentNode("name", stringReference),
                 ageArgument
             ),
             doubleReference,
             Nodes.block(new ReturnNode(ageReference))
         );
         TypeResult<Type> result = inferType(functionExpression, standardContext());
         assertThat(result, is(success((Type)CoreTypes.functionTypeOf(CoreTypes.STRING, CoreTypes.DOUBLE, CoreTypes.DOUBLE))));
     }
     
     @Test public void
     longLambdaExpressionHandlesUnrecognisedArgumentTypes() {
         LongLambdaExpressionNode functionExpression = new LongLambdaExpressionNode(
             asList(
                 new FormalArgumentNode("name", new VariableIdentifierNode("Strink"))
             ),
             doubleReference,
             Nodes.block(new ReturnNode(new NumberLiteralNode("4")))
         );
         TypeResult<Type> result = inferType(functionExpression, standardContext());
         CompilerErrorDescription[] errorsArray = { new UntypedReferenceError("Strink") };
         assertThat(result, isFailureWithErrors(errorsArray));
     }
     
     @Test public void
     shortLambdaExpressionAddsArgumentsToFunctionScope() {
         FormalArgumentNode ageArgument = new FormalArgumentNode("age", doubleReference);
         VariableIdentifierNode ageReference = new VariableIdentifierNode("age");
         fixture.addReference(ageReference, ageArgument);
         
         ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
             asList(
                 new FormalArgumentNode("name", stringReference),
                 ageArgument
             ),
             none(ExpressionNode.class),
             ageReference
         );
         TypeResult<Type> result = inferType(functionExpression, standardContext());
         assertThat(result, is(success((Type)CoreTypes.functionTypeOf(CoreTypes.STRING, CoreTypes.DOUBLE, CoreTypes.DOUBLE))));
     }
     
     @Test public void
     shortLambdaExpressionHandlesUnrecognisedArgumentTypes() {
         ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
             asList(
                 new FormalArgumentNode("name", new VariableIdentifierNode("Strink")),
                 new FormalArgumentNode("age", new VariableIdentifierNode("Numer"))
             ),
             none(ExpressionNode.class),
             new NumberLiteralNode("4")
         );
         TypeResult<Type> result = inferType(functionExpression, standardContext());
         CompilerErrorDescription[] errorsArray = { new UntypedReferenceError("Strink"), new UntypedReferenceError("Numer") };
         assertThat(result, isFailureWithErrors(errorsArray));
     }
     
     @Test public void
     shortLambdaExpressionHandlesUnrecognisedUntypeableBodyWhenReturnTypeIsExplicit() {
         ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
             asList(
                 new FormalArgumentNode("age", doubleReference)
             ),
             some(doubleReference),
             new VariableIdentifierNode("blah")
         );
         TypeResult<Type> result = inferType(functionExpression, standardContext());
         CompilerErrorDescription[] errorsArray = { new UntypedReferenceError("blah") };
         assertThat(result, isFailureWithErrors(errorsArray));
     }
     
     @Test public void
     bodyOfLongLambdaExpressionMustReturn() {
         LongLambdaExpressionNode functionExpression = new LongLambdaExpressionNode(
             Collections.<FormalArgumentNode>emptyList(),
             booleanReference,
             Nodes.block()
         );
         TypeResult<Type> result = inferType(functionExpression, standardContext());
         assertThat(errorStrings(result), is(asList("Expected return statement")));
     }
     
     @Test public void
     functionCallsHaveTypeOfReturnTypeOfFunctionWithNoArguments() {
         VariableIdentifierNode reference = Nodes.id("magic");
         GlobalDeclaration declaration = globalDeclaration("magic");
         fixture.addReference(reference, declaration);
         
         StaticContext context = standardContext();
         context.add(declaration, unassignableValue(CoreTypes.functionTypeOf(CoreTypes.DOUBLE)));
         
         CallNode call = Nodes.call(reference);
         TypeResult<Type> result = inferType(call, context);
         assertThat(result, isType(CoreTypes.DOUBLE));
     }
     
     @Test public void
     functionCallsHaveTypeOfReturnTypeOfFunctionWithCorrectArguments() {
         VariableIdentifierNode reference = Nodes.id("isLength");
         GlobalDeclaration declaration = globalDeclaration("isLength");
         fixture.addReference(reference, declaration);
         
         StaticContext context = standardContext();
         // isLength: (String, Double) -> Boolean 
         context.add(declaration, unassignableValue(CoreTypes.functionTypeOf(CoreTypes.STRING, CoreTypes.DOUBLE, CoreTypes.BOOLEAN)));
         CallNode call = Nodes.call(reference, Nodes.string("Blah"), Nodes.number("4"));
         TypeResult<Type> result = inferType(call, context);
         assertThat(result, isType(CoreTypes.BOOLEAN));
     }
     
     @Test public void
     errorIfActualArgumentsAreNotAssignableToFormalArguments() {
         VariableIdentifierNode reference = Nodes.id("isLength");
         GlobalDeclaration declaration = globalDeclaration("isLength");
         fixture.addReference(reference, declaration);
         
         StaticContext context = standardContext();
         // isLength: (String, Double) -> Boolean 
         context.add(declaration, unassignableValue(CoreTypes.functionTypeOf(CoreTypes.STRING, CoreTypes.DOUBLE, CoreTypes.BOOLEAN)));
         CallNode call = Nodes.call(reference, Nodes.number("4"), Nodes.string("Blah"));
         TypeResult<Type> result = inferType(call, context);
         assertThat(
             errorStrings(result),
             is(asList(
                 "Expected expression of type String as argument 1, but got expression of type Double",
                 "Expected expression of type Double as argument 2, but got expression of type String"
             ))
         );
     }
     
     @Test public void
     cannotCallTypesThatArentFunctions() {
         VariableIdentifierNode reference = Nodes.id("isLength");
         GlobalDeclaration declaration = globalDeclaration("isLength");
         fixture.addReference(reference, declaration);
         
         StaticContext context = standardContext();
         context.add(declaration, unassignableValue(CoreTypes.BOOLEAN));
         CallNode call = Nodes.call(reference);
         TypeResult<Type> result = inferType(call, context);
         assertThat(result, isFailureWithErrors(new NotCallableError(CoreTypes.BOOLEAN)));
     }
     
     @Test public void
     errorIfCallingFunctionWithWrongNumberOfArguments() {
         VariableIdentifierNode reference = Nodes.id("isLength");
         GlobalDeclaration declaration = globalDeclaration("isLength");
         fixture.addReference(reference, declaration);
         
         StaticContext context = standardContext();
         // isLength: (String, Double) -> Boolean 
         context.add(declaration, unassignableValue(CoreTypes.functionTypeOf(CoreTypes.STRING, CoreTypes.DOUBLE, CoreTypes.BOOLEAN)));
         
         CallNode call = Nodes.call(reference, Nodes.number("4"));
         TypeResult<Type> result = inferType(call, context);
         assertThat(
             errorStrings(result),
             is(asList("Function requires 2 argument(s), but is called with 1"))
         );
     }
     
     @Test public void
     canCallClassConstructor() {
         VariableIdentifierNode reference = Nodes.id("Person");
         GlobalDeclaration declaration = globalDeclaration("Person");
         fixture.addReference(reference, declaration);
         
         StaticContext context = standardContext();
         ClassType type = new ClassType(fullyQualifiedName("Person"));
         context.addClass(declaration, type, typeParameters(), ScalarTypeInfo.EMPTY);
         
         CallNode call = Nodes.call(reference);
         TypeResult<Type> result = inferType(call, context);
         assertThat(result, isType(type));
     }
     
     @Test public void
     memberAccessHasTypeOfMember() {
         VariableIdentifierNode reference = Nodes.id("heAintHeavy");
         GlobalDeclaration declaration = globalDeclaration("heAintHeavy");
         fixture.addReference(reference, declaration);
         
         StaticContext context = standardContext();
         InterfaceType interfaceType = new InterfaceType(fullyQualifiedName("shed", "example", "Brother"));
         context.add(declaration, unassignableValue(interfaceType));
         context.addInfo(interfaceType, new ScalarTypeInfo(interfaces(), members("age", unassignableValue(CoreTypes.DOUBLE))));
         
         MemberAccessNode memberAccess = Nodes.member(reference, "age");
         TypeResult<ValueInfo> result = inferValueInfo(memberAccess, context);
         assertThat(result, is(success(unassignableValue(CoreTypes.DOUBLE))));
     }
     
     @Test public void
     memberAccessIsAssignableIfMemberIsAssignable() {
         VariableIdentifierNode reference = Nodes.id("heAintHeavy");
         GlobalDeclaration declaration = globalDeclaration("heAintHeavy");
         fixture.addReference(reference, declaration);
         
         StaticContext context = standardContext();
         InterfaceType interfaceType = new InterfaceType(fullyQualifiedName("shed", "example", "Brother"));
         context.add(declaration, unassignableValue(interfaceType));
         context.addInfo(interfaceType, new ScalarTypeInfo(interfaces(), members("age", assignableValue(CoreTypes.DOUBLE))));
         
         MemberAccessNode memberAccess = Nodes.member(reference, "age");
         TypeResult<ValueInfo> result = inferValueInfo(memberAccess, context);
         assertThat(result, is(success(assignableValue(CoreTypes.DOUBLE))));
     }
     
     @Test public void
     memberAccessFailsIfInterfaceDoesNotHaveSpecifiedMember() {
         VariableIdentifierNode reference = Nodes.id("heAintHeavy");
         GlobalDeclaration declaration = globalDeclaration("heAintHeavy");
         fixture.addReference(reference, declaration);
         
         StaticContext context = standardContext();
         InterfaceType interfaceType = new InterfaceType(fullyQualifiedName("shed", "example", "Brother"));
         context.add(declaration, unassignableValue(interfaceType));
         context.addInfo(interfaceType, new ScalarTypeInfo(interfaces(), members("age", unassignableValue(CoreTypes.DOUBLE))));
         MemberAccessNode memberAccess = Nodes.member(reference, "height");
         TypeResult<Type> result = inferType(memberAccess, context);
         assertThat(
             errorStrings(result),
             is(asList("No such member: height"))
         );
     }
     
     @Test public void
     applyingTypeUpdatesParameterisedTypeWithType() {
         VariableIdentifierNode listReference = Nodes.id("List");
         GlobalDeclaration listDeclaration = globalDeclaration("List");
         fixture.addReference(listReference, listDeclaration);
         
         StaticContext context = standardContext();
         FormalTypeParameter typeParameter = invariantFormalTypeParameter("T");
         ParameterisedType listTypeFunction = parameterisedType(
             new InterfaceType(fullyQualifiedName("shed", "List")),
             asList(typeParameter)
         );
         context.add(listDeclaration, unassignableValue(listTypeFunction));
         TypeApplicationNode typeApplication = Nodes.typeApply(listReference, doubleReference);
         
         ShortLambdaExpressionNode functionExpression = new ShortLambdaExpressionNode(
             asList(new FormalArgumentNode("dummy", typeApplication)),
             none(ExpressionNode.class),
             new NumberLiteralNode("42")
         );
         TypeResult<Type> result = inferType(functionExpression, context);
         assertThat(result, is(success(
             (Type)CoreTypes.functionTypeOf(applyTypes(listTypeFunction, asList((Type)CoreTypes.DOUBLE)), CoreTypes.DOUBLE)
         )));
     }
     
     @Test public void
     applyingTypeBuildsMetaClassWithUpdatedMembers() {
         VariableIdentifierNode listReference = Nodes.id("List");
         GlobalDeclaration listDeclaration = globalDeclaration("List");
         fixture.addReference(listReference, listDeclaration);
         
         StaticContext context = standardContext();
         FormalTypeParameter typeParameter = invariantFormalTypeParameter("T");
         ScalarTypeInfo listTypeInfo = new ScalarTypeInfo(interfaces(), members("get", unassignableValue(typeParameter)));
         InterfaceType baseListType = new InterfaceType(fullyQualifiedName("shed", "List"));
         ParameterisedType listTypeFunction = parameterisedType(baseListType, asList(typeParameter));
         context.add(listDeclaration, unassignableValue(listTypeFunction));
         context.addInfo(baseListType, listTypeInfo);
         TypeApplicationNode typeApplication = Nodes.typeApply(listReference, doubleReference);
         
         TypeResult<Type> result = inferType(typeApplication, context);
         assertThat(result, isSuccess());
         Type metaClass = result.get();
         ScalarType type = (ScalarType) context.getTypeFromMetaClass(metaClass);
         ScalarTypeInfo typeInfo = context.getInfo(type);
         assertThat(typeInfo.getMembers(), is(members("get", unassignableValue(CoreTypes.DOUBLE))));
     }
     
     @Test public void
     applyingTypeUpdatesFunctionArgumentAndReturnTypes() {
         VariableIdentifierNode identityReference = Nodes.id("identity");
         GlobalDeclaration identityDeclaration = globalDeclaration("identity");
         fixture.addReference(identityReference, identityDeclaration);
         
         StaticContext context = standardContext();
         
         FormalTypeParameter typeParameter = invariantFormalTypeParameter("T");
         context.add(identityDeclaration, unassignableValue(new ParameterisedFunctionType(
             typeParameters(typeParameter, typeParameter),
             asList(typeParameter)
         )));
         CallNode call = Nodes.call(Nodes.typeApply(identityReference, doubleReference), Nodes.number("2"));
         TypeResult<Type> result = inferType(call, context);
         assertThat(result, isType(CoreTypes.DOUBLE));
     }
     
     @Test public void
     assignmentHasTypeOfAssignedValue() {
         VariableIdentifierNode reference = Nodes.id("x");
         GlobalDeclaration declaration = globalDeclaration("x");
         fixture.addReference(reference, declaration);
         
         StaticContext context = standardContext();
         
         context.add(declaration, assignableValue(CoreTypes.DOUBLE));
         
         TypeResult<Type> result = inferType(Nodes.assign(reference, Nodes.number("4")), context);
         assertThat(result, isType(CoreTypes.DOUBLE));
     }
     
     @Test public void
     cannotAssignToUnassignableValue() {
         VariableIdentifierNode reference = Nodes.id("x");
         GlobalDeclaration declaration = globalDeclaration("x");
         fixture.addReference(reference, declaration);
         
         StaticContext context = standardContext();
         
         context.add(declaration, unassignableValue(CoreTypes.DOUBLE));
         
         TypeResult<Type> result = inferType(Nodes.assign(reference, Nodes.number("4")), context);
         CompilerErrorDescription[] errorsArray = { new InvalidAssignmentError() };
         assertThat(result, isFailureWithErrors(errorsArray));
     }
     
     @Test public void
     cannotAssignValueIfNotSubTypeOfVariableType() {
         VariableIdentifierNode reference = Nodes.id("x");
         GlobalDeclaration declaration = globalDeclaration("x");
         fixture.addReference(reference, declaration);
         
         StaticContext context = standardContext();
         
         context.add(declaration, assignableValue(CoreTypes.DOUBLE));
         
         TypeResult<Type> result = inferType(Nodes.assign(reference, Nodes.bool(true)), context);
         CompilerErrorDescription[] errorsArray = { new TypeMismatchError(CoreTypes.DOUBLE, CoreTypes.BOOLEAN) };
         assertThat(result, isFailureWithErrors(errorsArray));
     }
     
     @Test public void
     canAssignValueIfSubTypeOfVariableType() {
         VariableIdentifierNode interfaceReference = Nodes.id("iterable");
         GlobalDeclaration interfaceDeclaration = globalDeclaration("iterable");
         fixture.addReference(interfaceReference, interfaceDeclaration);
 
         VariableIdentifierNode classReference = Nodes.id("iterable");
         GlobalDeclaration classDeclaration = globalDeclaration("iterable");
         fixture.addReference(classReference, classDeclaration);
         
         StaticContext context = standardContext();
         
         InterfaceType interfaceType = new InterfaceType(fullyQualifiedName("shed", "Iterable"));
         ClassType classType = new ClassType(fullyQualifiedName("shed", "List"));
         context.add(interfaceDeclaration, assignableValue(interfaceType));
         context.add(classDeclaration, assignableValue(classType));
         context.addInfo(classType, new ScalarTypeInfo(Interfaces.interfaces(interfaceType), members()));
         
         TypeResult<Type> result = inferType(Nodes.assign(interfaceReference, classReference), context);
         assertThat(result, is(success((Type)classType)));
     }
     
     private TypeResult<Type> inferType(ExpressionNode expression, StaticContext context) {
         return typeInferer(context).inferType(expression);
     }
     
     private TypeResult<ValueInfo> inferValueInfo(ExpressionNode expression, StaticContext context) {
         return typeInferer(context).inferValueInfo(expression);
     }
 
     private TypeInferer typeInferer(StaticContext context) {
         return fixture.get(TypeInferer.class);
     }
     
     private StaticContext standardContext() {
         return fixture.context();
     }
     
     private Matcher<TypeResult<Type>> isType(Type type) {
         return Matchers.is(TypeResult.success(type));
     }
 }
