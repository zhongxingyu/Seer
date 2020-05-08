 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 
 
 
 /**
  * @uml.dependency   supplier="Periode"
  */
 public abstract class Informe {
 	
 	public Informe(Periode periode)
 	{
 		this.setPeriode(periode);
 	}
 	
 	protected void crearTaulaPeriode() {
        taulaPeriode = new Taula(0,2);
         ArrayList<String> capcelera = new ArrayList<String>();
         capcelera.add("");
         capcelera.add("Data");
         getTaulaPeriode().afegeixFila(capcelera);
         
         ArrayList<String> fila1 = new ArrayList<String>();
         fila1.add("Desde");
         fila1.add(this.getPeriode().getDataInicialString());
         getTaulaPeriode().afegeixFila(fila1);
         
         ArrayList<String> fila2 = new ArrayList<String>();
         fila2.add("Fins a");
         fila2.add(this.getPeriode().getDataFinalString());
         getTaulaPeriode().afegeixFila(fila2);
         
         ArrayList<String> fila3 = new ArrayList<String>();
         fila3.add("Data de generaci de l'informe");
         fila3.add(new Date().toString());
         getTaulaPeriode().afegeixFila(fila3);
 	 }
 
 	protected void crearTaulaProjectes() {
         taulaProjectes = new Taula(0,5);
         ArrayList<String> capcelera = new ArrayList<String>();
         capcelera.add("ID");
         capcelera.add("Projecte");
         capcelera.add("Data Inici");
         capcelera.add("Data Fi");
         capcelera.add("Durada");
         getTaulaProjectes().afegeixFila(capcelera);
 	 }
 	
 	protected void crearTaulaSubprojectes() {
         taulaSubprojectes = new Taula(0,5);
         ArrayList<String> capcelera = new ArrayList<String>();
         capcelera.add("ID");
         capcelera.add("Subprojecte");
         capcelera.add("Data Inici");
         capcelera.add("Data Fi");
         capcelera.add("Durada");
         getTaulaSubprojectes().afegeixFila(capcelera);
 	 }
 	
 	protected void crearTaulaTasques() {
         taulaTasques = new Taula(0,5);
         ArrayList<String> capcelera = new ArrayList<String>();
         capcelera.add("ID");
         capcelera.add("Tasca");
         capcelera.add("Data Inici");
         capcelera.add("Data Fi");
         capcelera.add("Durada");
         getTaulaTasques().afegeixFila(capcelera);
 	 }
 	
 	protected void crearTaulaIntervals() {
         taulaTasques = new Taula(0,5);
         ArrayList<String> capcelera = new ArrayList<String>();
         capcelera.add("ID Tasca");
         capcelera.add("Tasca");
         capcelera.add("Data Inici");
         capcelera.add("Data Fi");
         capcelera.add("Durada");
         getTaulaIntervals().afegeixFila(capcelera);
 	 }
 	
 
 	
 	public abstract void generarInforme(ProjecteArrel projecteArrel);
 	/**
 	 * @uml.property   name="generadorInforme"
 	 * @uml.associationEnd   inverse="informe:GeneradorInforme"
 	 */
 	private GeneradorInforme generadorInforme;
 
 	/** 
 	 * Getter of the property <tt>generadorInforme</tt>
 	 * @return  Returns the generadorInforme.
 	 * @uml.property  name="generadorInforme"
 	 */
 	public GeneradorInforme getGeneradorInforme() {
 		return generadorInforme;
 	}
 
 	/** 
 	 * Setter of the property <tt>generadorInforme</tt>
 	 * @param generadorInforme  The generadorInforme to set.
 	 * @uml.property  name="generadorInforme"
 	 */
 	public void setGeneradorInforme(GeneradorInforme generadorInforme) {
 		this.generadorInforme = generadorInforme;
 	}
 
 	/** 
 	 * @uml.property name="periode"
 	 * @uml.associationEnd multiplicity="(1 1)" inverse="informe:Periode"
 	 */
 	private Periode periodeInforme = new Periode();
 
 	/** 
 	 * Getter of the property <tt>periode</tt>
 	 * @return  Returns the periode.
 	 * @uml.property  name="periode"
 	 */
 	public Periode getPeriode() {
 		return periodeInforme;
 	}
 
 	/** 
 	 * Setter of the property <tt>periode</tt>
 	 * @param periode  The periode to set.
 	 * @uml.property  name="periode"
 	 */
 	public void setPeriode(Periode periode) {
 		this.periodeInforme = periode;
 	}
 	/**Metode comprova si dos periodes el de informe i un periode qualsevol(interval, projecte i tasca) es tallan en algun moment
 	 * @param PeriodeActivitats Periode de intervals, projectes i tasca.
 	 */
 	public Boolean Intersecta(Periode periodeActivitat)
 	{
 		/*
 		 * Existen 4 posibles casos de intersecion
 		 * 1. dataInici i dataFinal de periodeActivitat i periodeInforme son iguales(coinciden totalmente)
 		 * 2. dataInici de periodeActivitat es mayor a dataInici periodeInforme, y dataFinal de periodeActivitat es menor que dataFinal periodeInforme(esta dentro del periodo)  
 		 * 3. dataInici de periodeActivitat es menor a dataInici periodeInforme, y  dataFinal de periodeActivitat es menor que dataFinal periodeInforme pero mayor que dataInici de periodeInforme(coincide un trozo del final)
 		 * 4. dataInici de periodeActivitat es mayor que dataInici de periodeInforme pero menor que dataFinal de periodeInforme, y dataFinal de periodeActivitat es mayor que dataFinal de periodeInforme(coincide un trozo del principio)
 		 * */
 
 		
 		/*Caso 1 y 2*/
         if ((periodeActivitat.getDataInicial().after(periodeInforme.getDataInicial()) 
         || periodeActivitat.getDataInicial().equals(periodeInforme.getDataInicial())) 
 
         &&(periodeActivitat.getDataFinal().before(periodeInforme.getDataFinal()) 
 
         || periodeActivitat.getDataFinal().equals(periodeInforme.getDataFinal()))) 
         	return true;
         
 
     	/*Caso 3*/
     	if((periodeActivitat.getDataInicial().before(periodeInforme.getDataInicial()))
     	&& (periodeActivitat.getDataFinal().before(periodeInforme.getDataFinal()) &&(periodeActivitat.getDataFinal().after(periodeInforme.getDataInicial()))))
     		return true;
     		
     	
 
 		/*Caso 4*/
 		if((periodeActivitat.getDataInicial().after(periodeInforme.getDataInicial()) && (periodeActivitat.getDataInicial().before(periodeInforme.getDataFinal())))
 				&&(periodeActivitat.getDataFinal().after(periodeInforme.getDataFinal())))
 			return true;
 	
         return false;
 	}
 	/**
 	 * @uml.property  name="taulaProjectes"
 	 */
 	private Taula taulaProjectes = null;
 
 	/** 
 	 * Getter of the property <tt>taulaProjecte</tt>
 	 * @return  Returns the taulaProjecte.
 	 * @uml.property  name="taulaProjectes"
 	 */
 	public Taula getTaulaProjectes() {
 		return taulaProjectes;
 	}
 
 
 	/** 
 	 * Setter of the property <tt>taulaProjecte</tt>
 	 * @param taulaProjecte  The taulaProjecte to set.
 	 * @uml.property  name="taulaProjectes"
 	 */
 	public void setTaulaProjectes(Taula taulaProjectes) {
 		this.taulaProjectes = taulaProjectes;
 	}
 	/**
 	 * @uml.property  name="taulaTasques"
 	 */
 	private Taula taulaTasques = null;
 
 	/**
 	 * Getter of the property <tt>taulaTasques</tt>
 	 * @return  Returns the taulaTasques.
 	 * @uml.property  name="taulaTasques"
 	 */
 	public Taula getTaulaTasques() {
 		return taulaTasques;
 	}
 
 
 	/**
 	 * Setter of the property <tt>taulaTasques</tt>
 	 * @param taulaTasques  The taulaTasques to set.
 	 * @uml.property  name="taulaTasques"
 	 */
 	public void setTaulaTasques(Taula taulaTasques) {
 		this.taulaTasques = taulaTasques;
 	}
 	/**
 	 * @uml.property  name="taulaSubprojectes"
 	 */
 	private Taula taulaSubprojectes = null;
 
 	/**
 	 * Getter of the property <tt>taulaSubprojectes</tt>
 	 * @return  Returns the taulaSubprojectes.
 	 * @uml.property  name="taulaSubprojectes"
 	 */
 	public Taula getTaulaSubprojectes() {
 		return taulaSubprojectes;
 	}
 
 
 	/**
 	 * Setter of the property <tt>taulaSubprojectes</tt>
 	 * @param taulaSubprojectes  The taulaSubprojectes to set.
 	 * @uml.property  name="taulaSubprojectes"
 	 */
 	public void setTaulaSubprojectes(Taula taulaSubprojectes) {
 		this.taulaSubprojectes = taulaSubprojectes;
 	}
 	/**
 	 * @uml.property  name="taulaIntervals"
 	 */
 	private Taula taulaIntervals = null;
 
 	/**
 	 * Getter of the property <tt>taulaIntervals</tt>
 	 * @return  Returns the taulaIntervals.
 	 * @uml.property  name="taulaIntervals"
 	 */
 	public Taula getTaulaIntervals() {
 		return taulaIntervals;
 	}
 
 
 	/**
 	 * Setter of the property <tt>taulaIntervals</tt>
 	 * @param taulaIntervals  The taulaIntervals to set.
 	 * @uml.property  name="taulaIntervals"
 	 */
 	public void setTaulaIntervals(Taula taulaIntervals) {
 		this.taulaIntervals = taulaIntervals;
 	}
 
 
 		
 		/**
 		 */
 		public abstract ArrayList<Taula> getTaules();
 		
 		/**
 		 * @uml.property  name="taulaPeriode"
 		 */
 		private Taula taulaPeriode = null;
 
 		/**
 		 * Getter of the property <tt>taulaPeriode</tt>
 		 * @return  Returns the taulaPeriode.
 		 * @uml.property  name="taulaPeriode"
 		 */
 		public Taula getTaulaPeriode() {
 			return taulaPeriode;
 		}
 
 		/**
 		 * Setter of the property <tt>taulaPeriode</tt>
 		 * @param taulaPeriode  The taulaPeriode to set.
 		 * @uml.property  name="taulaPeriode"
 		 */
 		public void setTaulaPeriode(Taula taulaPeriode) {
 			this.taulaPeriode = taulaPeriode;
 		}
 
 		
 }
