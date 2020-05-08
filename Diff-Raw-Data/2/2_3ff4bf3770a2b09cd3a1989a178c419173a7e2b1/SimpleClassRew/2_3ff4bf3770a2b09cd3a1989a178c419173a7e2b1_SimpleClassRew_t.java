 package javaworld;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.NoSuchElementException;
 import java.util.Set;
 
 import AST.Access;
 import AST.Block;
 import AST.BodyDecl;
 import AST.ClassDecl;
 import AST.ConstructorDecl;
 import AST.Expr;
 import AST.ExprStmt;
 import AST.List;
 import AST.Modifiers;
 import AST.Opt;
 import AST.PTClassDecl;
 import AST.PTDummyClass;
 import AST.ParameterDeclaration;
 import AST.SimpleClass;
 import AST.Stmt;
 import AST.TemplateConstructor;
 import AST.TemplateConstructorAccess;
 import AST.TemplateConstructorAccessShort;
 import AST.TypeAccess;
 
 import com.google.common.base.Joiner;
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Multimap;
 import com.google.common.collect.Sets;
 
 public class SimpleClassRew {
 	private final SimpleClass decl;
 	private Collection<PTDummyClass> dummies;
 	private Set<String> conflicts;
 	private Collection<ClassDeclRew> renamedSources;
 
 	public SimpleClassRew(SimpleClass decl,
 			Multimap<String, PTDummyClass> nameAndDummies) {
 		this.decl = decl;
 		checkIfSane(nameAndDummies);
 		dummies = nameAndDummies.get(decl.getID());
 		renamedSources = getRenamedAgnosticInstClasses();
 		conflicts = getConflicts();
 		computeClassToTemplateMultimap();
 	}
 
 	public void extendClass() {
 		updateSuperName();
 		renameResolvedConflicts();
 
 		if (mergingIsPossible()) {
 			for (ClassDeclRew source : renamedSources) {
 				addDecls(source.getBodyDecls());
 			}
 		}
 		// decl.getClassDecl().getConstructorDeclList()
 	}
 
 	public void addSimpleTemplateConstructorCalls() {
 		ClassDecl classDecl = decl.getClassDecl();
 		LinkedList<ConstructorDecl> constructors = classDecl
 				.getConstructorDeclList();
 		for (ConstructorDecl cd : constructors)
 			addSimpleTemplateConstructorCalls(cd);
 	}
 
 	private void computeClassToTemplateMultimap() {
 		Multimap<String, String> classToTemplates = HashMultimap.create();
 		for (PTDummyClass dummy : dummies) {
 			String classID = dummy.getOriginator().getID();
 			String templateID = dummy.getTemplate().getID();
 			classToTemplates.put(classID, templateID);
 		}
 		decl.getClassDecl().setClassToTemplateMap(classToTemplates);
 	}
 
 	private void updateSuperName() {
 		HashSet<String> names = Sets.newHashSet();
 		for (ClassDeclRew x : renamedSources) {
 			names.add(x.getSuperClassName());
 		}
 		names.remove(null); // classes without superclass
 		try {
 			decl.getClassDecl().setSuperClassAccess(
 					new TypeAccess(Iterables.getOnlyElement(names)));
 		} catch (NoSuchElementException e) { // no superclasses
 		} catch (IllegalArgumentException e) {
 			decl.error(String.format(
 					"Merge error for %s. superklasses %s must be merged.\n",
 					decl.getID(), Joiner.on(" and ").join(names)));
 		}
 	}
 
 	private boolean addsResolvesConflict() {
 		Set<String> addRefinements = decl.getClassDecl().methodSignatures();
 		for (String conflictingName : conflicts) {
 			if (!addRefinements.contains(conflictingName)) {
 				decl.error(conflictingName
 						+ " is an unresolved conflict during merging.\n");
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Had a bug with views, switched to immutableSets. Code may be written more
 	 * concise. Returns intersection of all renamed signatures (fields and
 	 * methods) from all tsuperclasses.
 	 * 
 	 * (since getConflicts needs all classes renamed. maybe it should use a
 	 * renamed copy for these purposes.)
 	 */
 	private Set<String> getConflicts() {
 		Set<String> collisions = ImmutableSet.of();
 		Set<String> allDefinitions = ImmutableSet.copyOf((decl.getClassDecl()
 				.methodSignatures()));
 		for (ClassDeclRew decl : renamedSources) {
 			Set<String> instanceDecls = decl.getSignatures();
 			Set<String> localCollisions = Sets.intersection(instanceDecls,
 					allDefinitions);
 			allDefinitions = ImmutableSet.copyOf(Sets.union(allDefinitions,
 					instanceDecls));
 			collisions = ImmutableSet.copyOf(Sets.union(collisions,
 					localCollisions));
 		}
 		return collisions;
 	}
 
 	private void renameResolvedConflicts() {
 		for (ClassDeclRew decl : renamedSources)
 			decl.renameMatchingMethods(conflicts);
 	}
 
 	// TODO tautology?
 	private boolean addsHasOwnConstructor() {
 		return decl.getClassDecl().getConstructorDeclList().size() > 0;
 	}
 
 	private boolean mergingIsPossible() {
 		return addsResolvesConflict() && addsHasOwnConstructor();
 	}
 
 	private void addDecls(List<BodyDecl> bodyDecls) {
 		ClassDecl target = decl.getClassDecl();
 		for (BodyDecl bodyDecl : bodyDecls) {
 			if (!(bodyDecl instanceof ConstructorDecl)) {
 				target.addBodyDecl(bodyDecl);
 			}
 		}
 	}
 
 	/**
 	 * Renamed classes are not cross checked. If there's a method name conflict
 	 * that the adds class resolves, then the correct renaming of the
 	 * conflicting classes will be performed later on.
 	 * 
 	 * @return all classes that will be merge into current.
 	 */
 	private Collection<ClassDeclRew> getRenamedAgnosticInstClasses() {
 		Collection<ClassDeclRew> instClasses = Lists.newLinkedList();
 		for (PTDummyClass x : dummies) {
 			DummyRew dummyr = new DummyRew(x);
 			ClassDeclRew ext = dummyr.getRenamedSourceClass();
 			instClasses.add(ext);
 		}
 		return instClasses;
 	}
 
 	private void checkIfSane(Multimap<String, PTDummyClass> nameAndDummies) {
 		if (nameAndDummies.containsKey(decl.getID())) {
 			if (!decl.isAddsClass()) {
 				decl.error("Class "
 						+ decl.getID()
 						+ " has dual roles. It's both defined as an inpedendent class and as a template adds class.\n");
 			}
 		} else if (decl.isAddsClass()) {
 			decl.error(decl.getID()
 					+ " is an add class, template source class not found!\n");
 		}
 	}
 
 	private void addSimpleTemplateConstructorCalls(ConstructorDecl consDecl) {
 		ClassDecl classDecl = decl.getClassDecl();
 		List<Stmt> bodyDecls = consDecl.getBlock().getStmts();
 		Collection<TemplateConstructorAccess> accesses = consDecl
 				.getTemplateConstructorAccesses();
 		Collection<TemplateConstructor> emptyConstructors = Lists
 				.newLinkedList();
 		for (TemplateConstructor x : classDecl.getTemplateConstructors())
 			if (x.hasNoParameter())
 				emptyConstructors.add(x);
 
 		for (TemplateConstructor x : emptyConstructors) {
 			if (isNotCalledFrom(x, accesses)) {
 				Stmt a = createAccess(x);
 				bodyDecls.add(a);
 			}
 		}
 	}
 
 	private Stmt createAccess(TemplateConstructor x) {
 		String templateName = x.getTemplateID();
 		String tclassID = x.getTClassID();
 
 		TemplateConstructorAccess access = new TemplateConstructorAccess(
				Util.toName(templateName,tclassID), new List<Expr>(), tclassID, templateName);
 		return new ExprStmt(access);
 	}
 
 	private boolean isNotCalledFrom(TemplateConstructor x,
 			Collection<TemplateConstructorAccess> accesses) {
 		for (TemplateConstructorAccess access : accesses) {
 			if (access.getTClassID().equals(x.getTClassID()))
 				return false;
 		}
 		return true;
 	}
 }
