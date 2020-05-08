 package batalhanaval.exceptions;
 
 /**
 * Exceção que indica que a posição do tabuleiro
 * já foi atingida.
  *  
  * @author Darlan P. de Campos
 * @author Roger de Córdova Farias
  */
 @SuppressWarnings("serial")
 public class PosicaoJaAtingidaException extends Exception {
 
 	public PosicaoJaAtingidaException() {
		super("Posição já atingida!");
 	}
 }
