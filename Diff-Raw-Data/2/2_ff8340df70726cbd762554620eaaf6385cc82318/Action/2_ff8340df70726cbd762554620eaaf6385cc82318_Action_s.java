 package simumatch.datamanager;
 
 /**
  * An action that can be performed by a team in a match.
  */
 public enum Action {
 	// preparatories actions
 	MOTIVARSE,
 	PINTARSE,
 	PELEA_AFICIONES,
 	CREAR_PANCARTA,
 	PROMOCIONAR_EQUIPO,
 	HACKEAR_PAGINA,
 	ORGANIZAR_CENA,
 	ORGANIZAR_HOMENAJE,
 	CONTRATAR_RRPP,
 	FINANCIAR_EVENTO,
 	MEJORAR_GRADAS,
 	SOBORNAR_LINIER,
	INCETIVO_ECONOMICO,
 	ASCENDER_TRABAJO,
 	APOSTAR,
 
 	// in match actions
 	SALTO_ESPONTANEO,
 	INICIAR_OLA,
 	PUNTERO_LASER,
 	TIRAR_BENGALA,
 	BEBER_CERVEZA,
 	ENTREVISTA_INTERMEDIO,
 	RETRANSMITIR_PARTIDO,
 	HABLAR_SPEAKER,
 	ACTIVAR_SOBORNO,
 	DOBLAR_APUESTA;
 	
 	/**
 	 * @param name
 	 *            The name of the action to retrieve
 	 * @return The <tt>Action</tt> associated with the given <tt>name</tt>, or <tt>null</tt> if there is no such
 	 *         <tt>Action</tt>
 	 */
 	public static Action get ( String name ) {
 		try {
 			return Enum.valueOf( Action.class, name.trim().toUpperCase() );
 		} catch ( IllegalArgumentException exc ) {
 			return null;
 		}
 	}
 }
