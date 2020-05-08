 
 
 import java.util.Date;
 import java.util.Vector;
 
public class Tasca extends Activitat {
 
 
 	@Override
 	public void printActivitat() {
 		// TODO Auto-generated method stub
 		System.out.println('-' + "" + this.getIdentificador().getNom());
 	}
 
 		
 		/**
 		 */
 		public Tasca(String nom, String descripcio){
 			this.setIdentificador(new Identificador(nom,descripcio));
 			intervals = new Vector<Interval>();
 			//falta tema identificador
 		}
 
 			
 			/**
 			 */
 			public void run(){
 				this.intervals.add(new Interval(this,new Date().toString()));
 			}
 
 
 			/** 
 			 * @uml.property name="intervals"
 			 * @uml.associationEnd multiplicity="(0 -1)" ordering="true" inverse="tascaPare:Interval"
 			 */
 			private Vector<Interval> intervals;
 
 
 			/** 
 			 * Getter of the property <tt>intervals</tt>
 			 * @return  Returns the intervals.
 			 * @uml.property  name="intervals"
 			 */
 			public Vector<Interval> getIntervals() {
 				return intervals;
 			}
 
 			/** 
 			 * Setter of the property <tt>intervals</tt>
 			 * @param intervals  The intervals to set.
 			 * @uml.property  name="intervals"
 			 */
 			public void setIntervals(Vector<Interval> intervals) {
 				this.intervals = intervals;
 			}
 }
