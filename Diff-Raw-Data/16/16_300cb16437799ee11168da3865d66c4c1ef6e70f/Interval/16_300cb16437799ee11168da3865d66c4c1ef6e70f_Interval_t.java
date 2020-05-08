 import java.util.Observable;
 import java.util.Observer;
 
 
 
public class Interval implements Observer,java.io.Serializable {
 
 	/** 
 	 * @uml.property name="tascaPare"
 	 * @uml.associationEnd multiplicity="(1 1)" inverse="intervals:Tasca"
 	 */
 	private Tasca tascaPare = new Tasca(null, null);
 
 	/** 
 	 * @uml.property name="dataInicial"
 	 */
 	private String dataInicial;
 
 	/** 
 	 * Getter of the property <tt>dataInicial</tt>
 	 * @return  Returns the dataInicial.
 	 * @uml.property  name="dataInicial"
 	 */
 	public String getDataInicial() {
 		return dataInicial;
 	}
 
 	/** 
 	 * @uml.property name="dataFinal"
 	 */
 	private String dataFinal;
 
 	/** 
 	 * Getter of the property <tt>dataFinal</tt>
 	 * @return  Returns the dataFinal.
 	 * @uml.property  name="dataFinal"
 	 */
 	public String getDataFinal() {
 		return dataFinal;
 	}
 
 	/** 
 	 * @uml.property name="durada"
 	 */
 	private long durada = 0;
 
 	/** 
 	 * Getter of the property <tt>durada</tt>
 	 * @return  Returns the durada.
 	 * @uml.property  name="durada"
 	 */
 	public long getDurada() {
 		return durada;
 	}
 
 	/** 
 	 * Getter of the property <tt>tascaPare</tt>
 	 * @return  Returns the tasca.
 	 * @uml.property  name="tascaPare"
 	 */
 	public Tasca getTascaPare() {
 		return tascaPare;
 	}
 
 	@Override
 		public void update(Observable arg0, Object arg1) {
 			System.out.println(tascaPare.getIdentificador().getNom() +": "+ durada++ + "segundos.");
 		}
 
 		/** 
 		 * Setter of the property <tt>durada</tt>
 		 * @param durada  The durada to set.
 		 * @uml.property  name="durada"
 		 */
 		public void setDurada(long durada) {
 			this.durada = durada;
 		}
 
 		/**
 		 * Setter of the property <tt>tascaPare</tt>
 		 * @param tascaPare  The tascaPare to set.
 		 * @uml.property  name="tascaPare"
 		 */
 		public void setTascaPare(Tasca tascaPare) {
 			this.tascaPare = tascaPare;
 		}
 
 			
 			/**
 			 */
 			public Interval(Tasca tascaPare, String dataInicial){
 				this.tascaPare = tascaPare;
 				this.dataInicial = dataInicial;
 				this.dataFinal = dataInicial;
 				
 			}
 
 			/** 
 			 * Setter of the property <tt>dataInicial</tt>
 			 * @param dataInicial  The dataInicial to set.
 			 * @uml.property  name="dataInicial"
 			 */
 			public void setDataInicial(String dataInicial) {
 				this.dataInicial = dataInicial;
 			}
 
 			/** 
 			 * Setter of the property <tt>dataFinal</tt>
 			 * @param dataFinal  The dataFinal to set.
 			 * @uml.property  name="dataFinal"
 			 */
 			public void setDataFinal(String dataFinal) {
 				this.dataFinal = dataFinal;
 			}
 
 			
 			/**
 			 */
 
 }
