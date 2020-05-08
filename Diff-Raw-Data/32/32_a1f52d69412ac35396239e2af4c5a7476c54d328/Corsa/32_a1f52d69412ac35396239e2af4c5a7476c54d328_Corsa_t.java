 package modelloTreni;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.*;
 
 import org.hibernate.annotations.OnDelete;
 import org.hibernate.annotations.OnDeleteAction;
 
 @Entity
 @Table(name = "corse")
 public class Corsa implements Serializable {
 
     /**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 
 	@Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private int id;
 
 
 	@ManyToOne
 	@OnDelete(action = OnDeleteAction.NO_ACTION)
 	private Tratta tratta;
     
     @Enumerated(EnumType.STRING)
 	private Tipologia tipo;
     
     @OneToMany(cascade=CascadeType.REMOVE)
 	private List<Fermata> fermate = new ArrayList<Fermata>();
     
     
     
     Corsa(){
     	
     }
 
 	Corsa(Tratta tratta, Tipologia tipo, ArrayList<Fermata> fermate) {
 		super();
 		this.tratta = tratta;
 		this.tipo = tipo;
 		this.fermate = fermate;
 	}
 
 
 	public Integer getId() {
 		return id;
 	}
 
 
 	 void setId(int id) {
 		this.id = id;
 	}
 
 
 	public Tratta getTratta() {
 		return tratta;
 	}
 
 	void setTratta(Tratta tratta) {
 		this.tratta = tratta;
 	}
 
 	public Tipologia getTipo() {
 		return tipo;
 	}
 
 	void setTipo(Tipologia tipo) {
 		this.tipo = tipo;
 	}
 
 	public ArrayList<Fermata> getFermate() {
 		return new ArrayList<Fermata>(fermate);
 	}
 
 	void setFermate(ArrayList<Fermata> fermate) {
 		this.fermate = fermate;
 	}
 	
 	public Fermata getFermataPartenza(){
 		return fermate.get(0);
 	}
 	
 	//FIXME creare una classe con il metodo isBefore
 	public Fermata getFermataArrivo(){
 		//return fermate.get(fermate.size()-1);
 		Fermata fermataArrivo = fermate.get(0);
 		for(int i=1; i<fermate.size(); i++){
 			if(TrainManager.isBefore(fermataArrivo.getTime(), fermate.get(i).getTime())){
 				fermataArrivo = fermate.get(i);
 			}
 		}
 		return fermataArrivo;
 	}
 	
 	public void aggiungiFermata(Fermata fermata){
		fermate.add(fermata);
//		if((fermate.size()==0)){
//			fermate.add(fermata);
//		}else{
//			int size = fermate.size();
//			for(int i=0; i<size;i++){
//				Fermata f = fermate.get(i);
//				if (TrainManager.isBefore(fermata.getTime(), f.getTime())){
//					fermate.add(i, fermata);
//				}else{
//					if(i==size-1){
//						fermate.add(fermata);
//					}
//				}
//			}
//		}
 	}
 	
 	public void togliFermata(Fermata fermata){
 		fermate.remove(fermata);
 	}
 
 	@Override
 	public String toString() {
 		String s = "======\n"+getId();
 		for (Fermata f : fermate) {
 			s = s + "\n" + f.toString();
 		}
 		s = s + "\n======";
 		return s;
 	}
 
 }
