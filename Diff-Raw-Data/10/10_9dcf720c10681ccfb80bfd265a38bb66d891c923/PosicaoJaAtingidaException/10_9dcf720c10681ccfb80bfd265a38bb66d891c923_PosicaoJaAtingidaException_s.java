 package batalhanaval.exceptions;
 
 /**
 * Exceo que indica que a posio do tabuleiro
 * j foi atingida.
  *  
  * @author Darlan P. de Campos
 * @author Roger de Crdova Farias
  */
 @SuppressWarnings("serial")
 public class PosicaoJaAtingidaException extends Exception {
 
 	public PosicaoJaAtingidaException() {
		super("Posio j atingida!");
 	}
 }
