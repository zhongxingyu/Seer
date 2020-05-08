 package uom.dl.reasoner;
 
 import java.util.Arrays;
 import java.util.HashSet;
 
 import uom.dl.elements.AtMostConcept;
 import uom.dl.elements.AtomicConcept;
 import uom.dl.elements.AtomicRole;
 import uom.dl.elements.Concept;
 import uom.dl.elements.ExistsConcept;
 import uom.dl.elements.ForAllConcept;
 import uom.dl.elements.Individual;
 import uom.dl.elements.IntersectionConcept;
 import uom.dl.elements.NotConcept;
 import uom.dl.elements.UnionConcept;
 import uom.dl.utils.ConceptFactory;
 
 public class ReasonerTester {
 
 	public static void main(String[] args) {
 		AtomicConcept A = new AtomicConcept("A");
 		AtomicConcept B = new AtomicConcept("B");
 		AtomicConcept C = new AtomicConcept("C");
 		AtomicConcept D = new AtomicConcept("D");
 		AtomicRole R = new AtomicRole("R");
 		//HashSet<Concept> conSet = new HashSet<>(Arrays.asList(A, B, new UnionConcept(new IntersectionConcept(D, new NotConcept(C)), C), new NotConcept(D)));
 		HashSet<Concept> conSet = new HashSet<>(Arrays.asList(
 				(Concept)new IntersectionConcept(A, B), 
 				new UnionConcept(new NotConcept(A), C), 
 				new UnionConcept(new NotConcept(C), new NotConcept(B)),
 				new ExistsConcept(new AtomicRole("V"), new ExistsConcept(new AtomicRole("R"), C))
 			));
 	
 		conSet = new HashSet<>(Arrays.asList(
 				(Concept)new UnionConcept(D, A), 
 				new UnionConcept(
 						new IntersectionConcept(A, B),
 						new UnionConcept(D, A))
 			));
 		
 		/*
 		conSet = new HashSet<>(Arrays.asList(
 				(Concept)new UnionConcept(D, A), 
 				new UnionConcept(
 						new IntersectionConcept(A, B),
 						new ExistsConcept(new AtomicRole("R"), new ExistsConcept(new AtomicRole("R"), C)))
 			));
 		*/
 		
 		conSet = new HashSet<>(Arrays.asList(
 				new ExistsConcept(R, A),
 				new ExistsConcept(R, B),
 				new ForAllConcept(R, 
 						new UnionConcept(new NotConcept(A), new NotConcept(C))),
 				new AtMostConcept(1, R)
 			));
 		/*
 		conSet = new HashSet<>(Arrays.asList(
 				(Concept)new ExistsConcept(R, A),
 				new ExistsConcept(R, B),
 				//new ExistsConcept(R, C),
 				new ForAllConcept(R, new UnionConcept(new NotConcept(A), new NotConcept(B))),
 				new AtMostConcept(1, R)
 				//new AtLeastConcept(3, R)
 			));
 		
 		Role p1 = new AtomicRole("P1");
 		Role p2 = new AtomicRole("P2");
 		Role p3 = new AtomicRole("P3");
 		Concept c11 = new AtomicConcept("C11");
 		Concept c12 = new AtomicConcept("C12");
 		Concept c21 = new AtomicConcept("C21");
 		Concept c22 = new AtomicConcept("C22");
 		Concept c31 = new AtomicConcept("C31");
 		Concept c32 = new AtomicConcept("C32");
 		
 		Set<Concept> set1 = new HashSet<>();
 		set1.add(new ExistsConcept(p3, c31));
 		set1.add(new ExistsConcept(p3, c32));
 		Concept conA = new IntersectionConcept(
 				new ExistsConcept(p3, c31), 
 				new ExistsConcept(p3, c32));
 		Concept con1 = new ForAllConcept(p2, conA);
 		Concept con2 = new ExistsConcept(p2, 
 				new  ForAllConcept(p3, c22));
 		Concept con3 = new ExistsConcept(p2, 
 				new  ForAllConcept(p3, c21));
 		
 		Concept con321 = ConceptFactory.intersectionOfConcepts(
 				new HashSet<>(Arrays.asList(con3, con2, con1)));
 		
 		
 		conSet = new HashSet<>(Arrays.asList(
 				(Concept) new ExistsConcept(p1, new ForAllConcept(p2, new ForAllConcept(p3, c11))),
 				new ExistsConcept(p1, new ForAllConcept(p2, new ForAllConcept(p3, c12))),
 				new ForAllConcept(p1, con321),
 				new AtMostConcept(1, p1) 
 			));*/
 		/*
 		conSet = new HashSet<>(Arrays.asList(
 				(Concept)new NotConcept(A),
 				A
 			));
 		conSet = new HashSet<>(Arrays.asList(
 				(Concept)new UnionConcept(A, B),
 				new ExistsConcept(R, A),
 				new NotConcept(A)
 			));*/
 		//Check concept containment
 		//check semantic branching
 		conSet = new HashSet<>(Arrays.asList(
 				new NotConcept(A),
 				new UnionConcept(A, B),
				new UnionConcept(A, C)
 			));
 		Concept wholeConcept = ConceptFactory.intersectionOfConcepts(conSet);
 		System.out.println(wholeConcept);
 		boolean containConcept = ConceptFactory.contains(wholeConcept, D);
 		System.out.println("Contains D: " + containConcept);
 		containConcept = ConceptFactory.contains(wholeConcept, C);
 		System.out.println("Contains C: " + containConcept);
 		System.out.println(ConceptFactory.getAllAtomicConcepts(wholeConcept));
 		
 		
 		ConceptAssertion ca = new ConceptAssertion(wholeConcept, new Individual('b'));
 		TableauxAlgorithm tableaux = new TableauxAlgorithm();
 		Model model = tableaux.findModel(ca);
 		tableaux.getClashes().add(model);
 		if (model.isSatisfiable()) {
 			System.out.println(model.getInterpretation());
 			//model.printModel(true);
 			System.out.println("No of models: " + (TList.NO_OF_DUPLICATES+1));
 			System.out.println("No of clashes: " + tableaux.getClashes().size());
 		} else {
 			System.out.println("No Valid Interpretation");
 		}
 		Model.printModel(tableaux.getClashes(), true);
 		//subclassing test
 		/*
 		Concept c1 = new AtMostConcept(1, R);
 		Concept c2 = new AtMostConcept(3, R);
 		
 		log.info("C1 subsumes C2: " + TableauxAlgorithm.subsumes(c1, c2));
 		log.info("C2 subsumes C1: " + TableauxAlgorithm.subsumes(c2, c1));
 		*/
 	}	
 }
