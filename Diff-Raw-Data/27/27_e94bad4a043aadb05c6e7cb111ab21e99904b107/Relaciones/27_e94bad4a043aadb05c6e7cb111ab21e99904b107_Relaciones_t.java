 package src.problemas;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.ListIterator;
 import java.util.Queue;
 
 public class Relaciones {
 	public ArrayList<Investigador> investigadores;
 	public Investigador source;
 	public Investigador destination;
 	
	public Relaciones(){
		investigadores = new ArrayList<Investigador>();
	}
	
	public Relaciones(ArrayList<Investigador> lista, Investigador p, Investigador q){
		investigadores = lista;
		source = p;
		destination = q;
	}
	
	public void setList(ArrayList<Investigador> lista){
		investigadores = lista;
 	}
 	
 	public void setSource(Investigador inv){
 		source = inv;
 	}
 	
 	public void setDest(Investigador inv){
 		destination = inv;
 	}
 	
 	public void AgregarInvestigador(Investigador inv){
 
 		boolean esta = false;
 		int cantInv = investigadores.size();
 		int i = 0;
 		boolean agrego = true;
 		
 		while (i < cantInv){
 			if (investigadores.get(i).nombre.equals(inv.nombre)){
 				esta = true;
 				ListIterator<Investigador> amigos = investigadores.get(i).amistades.listIterator();
				while (amigos.hasNext() && agrego){
 					if (amigos.next().nombre.equals(inv.amistades.get(0).nombre)){
 						agrego = false;
 					}
 				}
 				if (agrego){
 					investigadores.get(i).AgregarAmigo(inv.amistades.get(0));
 				}
 			}
 			i++;
 		}
 		if (!esta){
 			investigadores.add(inv);
 		}
 	}
 	
 	public void AgregarAmigos(Investigador inv1, Investigador inv2){
 		ListIterator<Investigador> invRel = investigadores.listIterator();
 		while (invRel.hasNext()){
 			int indice = invRel.nextIndex();
 			if (investigadores.get(indice).nombre.equals(inv1.nombre)){
 				inv1 = investigadores.get(indice);
 			}else{
 				if (investigadores.get(indice).nombre.equals(inv2.nombre)){
 					inv2 = investigadores.get(indice);
 				}
 			}
 			invRel.next();
 		}
 		inv1.AgregarAmigo(inv2);
		this.AgregarInvestigador(inv1);
		this.AgregarInvestigador(inv2);
 	}
 	
 	public int BFS(Investigador source, Investigador destination){
 		source.estado = "encontrado";
 		source.distancia = 0;
 		Queue<Investigador> cola = new LinkedList<Investigador>();
 		cola.add(source);
		Investigador actual = new Investigador();
 		ListIterator<Investigador> amigosIter;
 		int indice = 0;
 		while (!cola.isEmpty()){
 			actual = cola.poll();
 			amigosIter = actual.amistades.listIterator();
 			while(amigosIter.hasNext()){
 				indice = amigosIter.nextIndex();
 				if (actual.amistades.get(indice).estado.equals("no encontrado")){
 					actual.amistades.get(indice).estado = "encontrado";
 					actual.amistades.get(indice).distancia = actual.distancia + 1;
 					cola.add(actual.amistades.get(indice));
 				}
 				amigosIter.next();
 			}
 			actual.estado = "visitado";
 		}
 		if (destination.distancia == Integer.MAX_VALUE){
 			return 0;
 		}else{
 			return destination.distancia;
 		}
 	}
 }
