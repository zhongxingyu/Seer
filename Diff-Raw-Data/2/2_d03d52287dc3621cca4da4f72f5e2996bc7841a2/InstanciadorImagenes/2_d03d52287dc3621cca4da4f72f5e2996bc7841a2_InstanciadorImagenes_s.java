 package instanciadores;
 
 import java.awt.Image;
 
 import javax.swing.ImageIcon;
 
 import municiones.MinaSubmarinaPorContacto;
 import municiones.Municion;
 import naves.EstadoDeSalud;
 import naves.SeccionDeNave;
 import naves.Sentido;
 
 public class InstanciadorImagenes {
 	//Instancia las imagenes segun el tipo
 	
 	static public ImageIcon nave(SeccionDeNave seccion){
 		//Se fija que el tipo de seccion, en donde esta ubicada(proa, popa, etc), el sentido y el estado
 		//Va formando el nombre de la imagen segun su estado
 		String tipo;
 		String estado;
 		String posicion = "BORRAME";
 		String sentido;
 		Sentido sentidoPreProcesado;
 		
 		String rutaImagen;
 		
 //				V= vertical
 //				U = up
 //				H= horizontal
 //				D= diagonal
 //				R= right
 //				L =left
 //				D= down
 		
 		//Obtengo el tipo:
 		tipo = seccion.obtenerTipoDeNave();
 		
 		//Obtengo la ubicacion de la seccion
 		if (!seccion.esProa() && !seccion.esPopa()){
 			posicion = "seccionMedia";
 		}
 		else if (seccion.esProa()){
 			posicion = "proa";
 		}
 		else{ //popa
 			posicion = "popa";
 		}
 		
 		
 		//obtengo el sentido, solo me importa si esta en dianogan o vertical/horizontal.
 		
 		sentidoPreProcesado = seccion.sentido();
 		if (sentidoPreProcesado == Sentido.OESTE){
 			sentido = "HL";
 		}
		if (sentidoPreProcesado == Sentido.ESTE){
 			sentido = "HR";
 		}
 		else if (sentidoPreProcesado == Sentido.NORESTE){
 			sentido = "DRU";
 		}
 		else if (sentidoPreProcesado == Sentido.NOROESTE){
 			sentido = "DLU";
 		}
 		else if (sentidoPreProcesado == Sentido.SUDESTE){
 			sentido = "DLD";
 		}
 		else if (sentidoPreProcesado == Sentido.SUDOESTE){
 			sentido = "DRD";
 		}
 		else if(sentidoPreProcesado == Sentido.SUR){
 			sentido = "VD";
 		}
 		else {//NORTE
 			sentido = "VU";
 		}
 				
 		//obtengo el estado:
 		
 		if(seccion.estado() == EstadoDeSalud.SANO){
 			estado = "Sana";
 		}
 		else{
 			estado = "Daniada";
 		}
 		
 		rutaImagen = "estaticos/Naves/"+posicion+estado+sentido+".png";
 		//instancio la imagen
 		
 		return  new ImageIcon(rutaImagen);//.getImage();		
 	}
 //	
 	static public ImageIcon municion(Municion municion){
 		//falta implementar
 		return new ImageIcon("estaticos/Minas/minaPuntual.png");
 	}
 
 }
