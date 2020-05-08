 package javaworld;
 
 import java.util.Comparator;
 import java.util.Set;
 
 import AST.Access;
 import AST.BodyDecl;
 import AST.ClassDecl;
 import AST.CompilationUnit;
 import AST.ImportDecl;
 import AST.List;
 import AST.Modifier;
 import AST.Modifiers;
 import AST.Opt;
 import AST.PTC;
 import AST.PTClassAddsDecl;
 import AST.PTClassDecl;
 import AST.PTDecl;
 import AST.PTInstDecl;
 import AST.PTInstTuple;
 import AST.PTTemplate;
 import AST.SimpleClass;
 
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableList.Builder;
 import com.google.common.collect.Multimap;
 import com.google.common.collect.Ordering;
 import com.google.common.collect.Sets;
 
 public class PTDeclRew {
 
 	private final PTDecl ptDeclToBeRewritten;
 
 	private ImmutableList<SimpleClassRew> simpleClasses;
 
 	public PTDeclRew(PTDecl ptDeclToBeRewritten) {
 		this.ptDeclToBeRewritten = ptDeclToBeRewritten;
 	}
 
 	protected void flushCaches() {
 		ptDeclToBeRewritten.flushCaches();
 	}
 
 	protected void copyImportDecls() {
 		CompilationUnit ownCU = ptDeclToBeRewritten.getCompilationUnit();
 		for (PTInstDecl instDecl : ptDeclToBeRewritten.getPTInstDecls()) {
 			PTTemplate originator = instDecl.getTemplate();
 			if (originator == null)
 				continue; // error caught elsewhere
 			CompilationUnit cunit = originator.getCompilationUnit();
 			if (cunit == ownCU)
 				continue;
 			for (ImportDecl id : cunit.getImportDeclList()) {
 				if (!ptDeclToBeRewritten.hasImportDecl(id.toString())) {
 					ImportDecl copy = (ImportDecl) id.copy();
 					ownCU.addImportDecl(copy);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Needs extended classes in correct order. Minit dependencies are inherited
 	 * and therefore a superclass must be extended before its child.
 	 */
 	protected void extendAddClassesWithInstantiatons() {
		/* this comparator is created to let the ordering of non-interfering
		 * minit calls be consistent.
		 */
		Comparator<SimpleClassRew> cmp = new Comparator<SimpleClassRew>() {
			
			@Override
			public int compare(SimpleClassRew o1, SimpleClassRew o2) {
				return o1.getName().compareTo(o2.getName());
			}
		};
		 ImmutableList<SimpleClassRew> classes = Ordering.from(cmp).immutableSortedCopy(simpleClasses);
 		Set<String> visited = Sets.newHashSet();
		while (visited.size() < classes.size()) {
			for (SimpleClassRew decl : classes) {
 				String superName = decl.getSuperClassname();
 				if (!visited.contains(decl.getName())) {
 					if (superName == null || visited.contains(superName)) {
 						visited.add(decl.getName());
 						decl.extendClass(getDestinationClassIDsWithInstTuples());
 					}
 				}
 			}
 
 		}
 	}
 
 	protected void createEmptyMissingAddClasses() {
 
 		Multimap<String, PTInstTuple> destinationClassIDsWithInstTuples = getDestinationClassIDsWithInstTuples();
 		Builder<SimpleClassRew> lb = ImmutableList.builder();
 
 		/*
 		 * Add the explicitly defined classes residing in this PTDecl to our
 		 * rewrite list.
 		 */
 		for (SimpleClass decl : ptDeclToBeRewritten.getSimpleClassList())
 			lb.add(new SimpleClassRew(decl));
 		/*
 		 * Does SimpleClassRew really need the whole
 		 * destinationClassIDsWithInstTuples? Why?
 		 */
 
 		/*
 		 * For those classes which are implicitly defined by inst clauses and
 		 * where no concrete destination class is found, add an empty adds
 		 * class. For example: package P { inst T with A => X; // no class X
 		 * adds .. given later }
 		 */
 		Set<String> addClasses = ptDeclToBeRewritten.getAdditionClassNamesSet();
 		Set<String> missingAddsClass = Sets.difference(
 				destinationClassIDsWithInstTuples.keySet(), addClasses);
 
 		for (String name : missingAddsClass) {
 			ClassDecl cls = new PTC(new Modifiers(), name, new Opt<Access>(),
 					new List<Access>(), new List<BodyDecl>());
 			PTClassAddsDecl addClass = new PTClassAddsDecl(cls);
 			ptDeclToBeRewritten.addSimpleClass(addClass);
 			lb.add(new SimpleClassRew(addClass));
 		}
 		simpleClasses = lb.build();
 	}
 
 	/**
 	 * returns a multimap where the key is DestinationClassID (String) and the
 	 * value is a list of InstTuples (for example A => X). Per key there may be
 	 * more than one InstTuple if we are merging several source classes.
 	 */
 	private Multimap<String, PTInstTuple> getDestinationClassIDsWithInstTuples() {
 		Multimap<String, PTInstTuple> destinationClassIDsWithInstTuples = HashMultimap
 				.create();
 		for (PTInstDecl templateInst : ptDeclToBeRewritten.getPTInstDecls()) {
 			for (PTInstTuple instTuple : templateInst.getPTInstTupleList()) {
 				destinationClassIDsWithInstTuples.put(instTuple.getID(),
 						instTuple);
 			}
 		}
 		return destinationClassIDsWithInstTuples;
 	}
 
 	public void createInitIfPackage() {
 		if (ptDeclToBeRewritten instanceof PTTemplate)
 			return;
 		String dummyName = addDummyClass();
 		for (SimpleClassRew x : simpleClasses) {
 			x.createInitConstructor(dummyName);
 			x.createDummyConstructor(dummyName);
 		}
 	}
 
 	private String addDummyClass() {
 		String dummyName = "DUMMY$"; // TODO something better?
 		ClassDecl dummy = new ClassDecl(new Modifiers(), dummyName, new Opt(),
 				new List(), new List());
 		ptDeclToBeRewritten.getSimpleClassList().add(new PTClassDecl(dummy));
 		return dummyName;
 	}
 }
