 package src.problemas;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.ListIterator;
 
 public class Investigador {
 	public String nombre;
 	public String estado;
 	public List<Investigador> amistades;
 	public int distancia;
 
	public Investigador(){
		estado = "no encontrado";
		amistades = new ArrayList<Investigador>();
		distancia = Integer.MAX_VALUE;
	}
	
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
			if (amigos.next().nombre.equals(this.nombre)){
 				esta = true;
 			}
 		}
 		if (!esta){
 			amistades.add(amigo);
 			amigo.amistades.add(this);
 		}
 	}
	
	public void dameAmigos(){
		ListIterator<Investigador> amigosIt = amistades.listIterator();
		System.out.println("Los amigos de " + nombre + " son:");
		while(amigosIt.hasNext()){
			System.out.println(amigosIt.next().nombre + " ");
		}
	}
}
