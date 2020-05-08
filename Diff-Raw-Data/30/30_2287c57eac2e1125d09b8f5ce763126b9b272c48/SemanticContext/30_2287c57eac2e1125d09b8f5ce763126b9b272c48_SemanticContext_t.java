 package descent.internal.compiler.parser;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.runtime.Assert;
 
 import descent.core.IProblemRequestor;
 import descent.core.compiler.IProblem;
 
 public class SemanticContext {
 	
 	public boolean BREAKABI = true;
 	public boolean IN_GCC = false;
 	public boolean _DH = true;
 	private IProblemRequestor problemRequestor;
 	public StringTable typeStringTable;
 	public Global global = new Global();
 	
 	// TODO file imports should be selectable in a dialog or something
 	public Map<String, File> fileImports = new HashMap<String, File>();
 	
 	public ClassDeclaration object; // ClassDeclaration::object
 	public ClassDeclaration classinfo; // ClassDeclaration::classinfo
 	public ClassDeclaration typeinfo;
 	public ClassDeclaration typeinfoclass;
 	public ClassDeclaration typeinfointerface;
 	public ClassDeclaration typeinfostruct;
 	public ClassDeclaration typeinfotypedef;
 	public ClassDeclaration typeinfopointer;
 	public ClassDeclaration typeinfoarray;
 	public ClassDeclaration typeinfostaticarray;
 	public ClassDeclaration typeinfoassociativearray;
 	public ClassDeclaration typeinfoenum;
 	public ClassDeclaration typeinfofunction;
 	public ClassDeclaration typeinfodelegate;
 	public ClassDeclaration typeinfotypelist;
 	
 	public ClassDeclaration moduleinfo;
 	
 	public Type tvoidptr;
 	public int dprogress;
 	
 	public DsymbolTable st;
 	public int apiLevel;
 	
 	public SemanticContext(IProblemRequestor problemRequestor, int apiLevel) {
 		this.problemRequestor = problemRequestor;
 		this.apiLevel = apiLevel;
 		this.typeStringTable = new StringTable();
 		
 		StandardLibraryHelper slh = StandardLibraryHelper.getInstance(apiLevel);
 		this.object = slh.Object;
 		this.classinfo = slh.ClassInfo;
 		this.typeinfo = slh.TypeInfo;
 		this.typeinfoclass = slh.TypeInfo_Class;
 		this.typeinfointerface = slh.TypeInfo_Interface;
 		this.typeinfostruct = slh.TypeInfo_Struct;
 		this.typeinfotypedef = slh.TypeInfo_Typedef;
 		this.typeinfopointer = slh.TypeInfo_Pointer;
 		this.typeinfoarray = slh.TypeInfo_Array;
 		this.typeinfostaticarray = slh.TypeInfo_StaticArray;
 		this.typeinfoassociativearray = slh.TypeInfo_AssociativeArray;
 		this.typeinfoenum = slh.TypeInfo_Enum;
 		this.typeinfofunction = slh.TypeInfo_Function;
 		this.typeinfodelegate = slh.TypeInfo_Delegate;
 		this.typeinfotypelist = slh.TypeInfo_Tuple;
 		
 		this.tvoidptr = Type.tvoid.pointerTo(this);
 	}
 	
 	public void acceptProblem(IProblem problem) {
		if (global.gag == 0) {
			problemRequestor.acceptProblem(problem);
		}
		global.errors++;
 	}
 	
 	public void multiplyDefined(Dsymbol s1, Dsymbol s2) {
 		acceptProblem(Problem.newSemanticMemberError(IProblem.DuplicatedSymbol, 0, s2.ident.start, s2.ident.length, new String[] { new String(s2.ident.ident) }));
 		acceptProblem(Problem.newSemanticMemberError(IProblem.DuplicatedSymbol, 0, s1.ident.start, s1.ident.length, new String[] { new String(s1.ident.ident) }));		
 	}
 	
 	private int generatedIds;	
 	public IdentifierExp generateId(String prefix) {
 		String name = prefix + ++generatedIds;
 		char[] id = name.toCharArray();
 		return new IdentifierExp(Loc.ZERO, id);
 	}
 
 	public FuncDeclaration genCfunc(Type treturn, char[] id) {
 		FuncDeclaration fd;
 		TypeFunction tf;
 		Dsymbol s;
 
 		// See if already in table
 		if (st == null)
 			st = new DsymbolTable();
 		s = st.lookup(id);
 		if (s != null) {
 			fd = s.isFuncDeclaration();
 			Assert.isNotNull(fd);
 			Assert.isTrue(fd.type.nextOf().equals(treturn));
 		} else {
 			tf = new TypeFunction(null, treturn, 0, LINK.LINKc);
 			fd = new FuncDeclaration(Loc.ZERO, new IdentifierExp(Loc.ZERO, id), STC.STCstatic, tf);
 			fd.protection = PROT.PROTpublic;
 			fd.linkage = LINK.LINKc;
 
 			st.insert(fd);
 		}
 		return fd;
 	}
 
 }
