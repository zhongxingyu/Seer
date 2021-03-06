 package dtool.ast.references;
 
 import static melnorme.utilbox.core.Assert.AssertNamespace.assertFail;
 
 import java.util.Collection;
 
 import dtool.ast.ASTCodePrinter;
 import dtool.ast.ASTNodeTypes;
 import dtool.ast.IASTVisitor;
 import dtool.ast.definitions.DefUnit;
 import dtool.parser.Token;
 import dtool.refmodel.DefUnitSearch;
 import dtool.refmodel.IScopeNode;
 import dtool.refmodel.PrefixDefUnitSearch;
 import dtool.refmodel.ReferenceResolver;
 import dtool.refmodel.api.IModuleResolver;
 
 public class RefPrimitive extends NamedReference {
 	
 	public final Token primitive;
 	
 	public RefPrimitive(Token primitiveToken) {
 		this.primitive = primitiveToken;
 	}
 	
 	@Override
 	public ASTNodeTypes getNodeType() {
 		return ASTNodeTypes.REF_PRIMITIVE;
 	}
 	
 	@Override
 	public String getTargetSimpleName() {
 		return primitive.source;
 	}
 	
 	@Override
 	public void accept0(IASTVisitor visitor) {
 		visitor.visit(this);
 		visitor.endVisit(this);
 	}
 	
 	
 	@Override
 	public Collection<DefUnit> findTargetDefUnits(IModuleResolver moduleResolver, boolean findOneOnly) {
 		DefUnitSearch search = new DefUnitSearch(getTargetSimpleName(), this, this.getStartPos(), 
 			findOneOnly, moduleResolver);
		assertFail(); // TODO /*BUG here*/
 		return search.getMatchDefUnits();
 	}
 	
 	@Override
 	public void doSearch(PrefixDefUnitSearch search) {
		assertFail(); // TODO /*BUG here*/
		IScopeNode lookupScope = null;
 		ReferenceResolver.findDefUnitInExtendedScope(lookupScope, search);
 	}
 	
 	@Override
 	public void toStringAsCode(ASTCodePrinter cp) {
 		cp.appendToken(primitive);
 	}
 	
 }
