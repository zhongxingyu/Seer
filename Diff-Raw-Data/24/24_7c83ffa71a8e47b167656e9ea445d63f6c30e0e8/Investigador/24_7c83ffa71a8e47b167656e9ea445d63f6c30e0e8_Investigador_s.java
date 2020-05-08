 package src.problemas;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.ListIterator;
 
 public class Investigador {
 	public String nombre;
 	public String estado;
 	public List<Investigador> amistades;
 	public int distancia;
 
 	public Investigador(String name){
 		nombre = name;
 		estado = "no encontrado";
 		amistades = new ArrayList<Investigador>();
 		distancia = Integer.MAX_VALUE;
 	}
	
 	public void encontrado(){
 		estado = "encontrado";
 	}
 	
 	public void visitado(){
 		estado = "visitado";
 	}
 	
 	public void aumentar(){
 		int temp = distancia;
 		if (distancia == Integer.MAX_VALUE){
 			temp = 0;
 		}
 		temp++;
 		distancia = temp;
 	}
 	
 	public void AgregarAmigo(Investigador amigo){
 		boolean esta = false;
 		ListIterator<Investigador> amigos = amigo.amistades.listIterator();
 		while (amigos.hasNext()){
			if (amigos.next().nombre.equals(amigo.nombre)){
 				esta = true;
 			}
 		}
 		if (!esta){
 			amistades.add(amigo);
 			amigo.amistades.add(this);
 		}
 	}
}
