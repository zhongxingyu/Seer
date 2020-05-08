 
 
 public class Client {
 
 	/**
 	 * @param args
 	 */
 	
 	public static Projecte projecte = new Projecte("raiz","Noda raiz");
 	
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		rellotge.start();
 		
 		//Primer proyecto
 		Projecte P1=new Projecte("P1","Projecte1");
 		P1.setProjectePare(projecte); //pensar de hacerlo de forma automatica
 		
 		//Tareas(hojas) de P1(padre)
 		P1.addActivitat(new Tasca("T1","Tasca1"));
 		P1.addActivitat(new Tasca("T2","Tasca2"));
 		
 		//Aado P1 "como hijo" de raiz
 		projecte.addActivitat(P1);
 		
 		//Subproyecto(hijo) de P1(padre)
 		Projecte P2=new Projecte("P2","Projecte2");
 		P2.setProjectePare(P1);//pensar de hacerlo de forma automatica
 		//Tareas(hojas) de P2(padre)
 		P2.addActivitat(new Tasca("T2.1","Tasca2.1"));
 		P2.addActivitat(new Tasca("T2.2","Tasca2.2"));
 		
 		//Aado P2 "como hijo" de P1
 		P1.addActivitat(P2);
 		
 		projecte.printActivitat();
 		
 		P1.removeActivitat(P1.getActivitats().lastElement());
 		
 		projecte.printActivitat();
 		
 		/**
 		 * Prueba de Timetasker, escribir 'stop' en la consola
 		 * para parar el intervalo nuevo creado y mostrar tiempo
 		 * del intervalo.
 		 */
 		Tasca tasca = new Tasca("T0","TascaTest");
 		tasca.run();
 		
		dataSaver.guardarActivitatFitxer(tasca);
 		dataSaver.llegirActivitatFitxer("test.ser");
 		
 		rellotge.addObserver(tasca.getIntervals().firstElement());
 
 	}
 
 	/** 
 	 * @uml.property name="projecteArrel"
 	 * @uml.associationEnd multiplicity="(1 1)" inverse="client:Projecte"
 	 */
 	
 
 	/**
 	 * Getter of the property <tt>projecteArrel</tt>
 	 * @return  Returns the projecte.
 	 * @uml.property  name="projecteArrel"
 	 */
 	public Projecte getProjecteArrel() {
 		return projecte;
 	}
 
 	/**
 	 * Setter of the property <tt>projecteArrel</tt>
 	 * @param projecteArrel  The projecte to set.
 	 * @uml.property  name="projecteArrel"
 	 */
 	public void setProjecteArrel(Projecte projecteArrel) {
 		projecte = projecteArrel;
 	}
 
 	/**
 	 * @uml.property  name="rellotge"
 	 * @uml.associationEnd  multiplicity="(1 1)" inverse="client:Rellotge"
 	 */
 	private static Rellotge rellotge = new Rellotge();
 	/**
 	 * @uml.property  name="dataSaver"
 	 * @uml.associationEnd  multiplicity="(1 1)" inverse="client:DataSaver"
 	 */
 	private static DataSaver dataSaver = new DataSaver();
 
 	/**
 	 * @uml.property  name="rellotge"
 	 * @uml.associationEnd  multiplicity="(1 1)" inverse="client:Rellotge"
 	 */
 
 	/**
 	 * @uml.property  name="rellotge"
 	 * @uml.associationEnd  multiplicity="(1 1)" inverse="client:Rellotge"
 	 */
 	
 
 }
