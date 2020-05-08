 /**
  * 26/11/2011 14:52:18 Copyright (C) 2011 Darío L. García
  * 
  * <a rel="license" href="http://creativecommons.org/licenses/by/3.0/"><img
  * alt="Creative Commons License" style="border-width:0"
  * src="http://i.creativecommons.org/l/by/3.0/88x31.png" /></a><br />
  * <span xmlns:dct="http://purl.org/dc/terms/" href="http://purl.org/dc/dcmitype/Text"
  * property="dct:title" rel="dct:type">Software</span> by <span
  * xmlns:cc="http://creativecommons.org/ns#" property="cc:attributionName">Darío García</span> is
  * licensed under a <a rel="license" href="http://creativecommons.org/licenses/by/3.0/">Creative
  * Commons Attribution 3.0 Unported License</a>.
  */
 package net.gaia.vortex.lowlevel;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicLong;
 
 import net.gaia.vortex.protocol.messages.ContenidoVortex;
 import net.gaia.vortex.protocol.messages.IdVortex;
 import net.gaia.vortex.protocol.messages.MensajeVortex;
 import net.gaia.vortex.protocol.messages.MetamensajeVortex;
 import net.gaia.vortex.protocol.messages.conn.CerrarConexion;
 import net.gaia.vortex.protocol.messages.routing.AcuseConsumo;
 import net.gaia.vortex.protocol.messages.tags.ReemplazarTags;
 
 import com.google.common.collect.Lists;
 
 /**
  * Esta clase presenta varios métodos utiles para crear escenarios para los tests
  * 
  * @author D. García
  */
 public class EscenarioDeTest {
 
 	private final AtomicLong secuencer = new AtomicLong(0);
 
 	/**
 	 * Crea un mensaje dummy para testear
 	 * 
 	 * @param tagDelMensaje
 	 * 
 	 * @return El mensaje creado
 	 */
 	public MensajeVortex crearMensajeDeTest(final String tagDelMensaje) {
 		final IdVortex identificacion = createId();
 		final List<String> tags = Lists.newArrayList(tagDelMensaje);
 		final ContenidoVortex contenidoVortex = ContenidoVortex.create("tipo", "valor");
 		final MensajeVortex mensajeVortex = MensajeVortex.create(contenidoVortex, identificacion, tags);
 		return mensajeVortex;
 	}
 
 	/**
 	 * Crea un ID para tests
 	 */
 	private IdVortex createId() {
 		return IdVortex.create("1", "1", -1L, 1L);
 	}
 
 	/**
 	 * Crea un mensaje que está mal armado
 	 * 
 	 * @return El mensaje creado pero incompleto
 	 */
 	public MensajeVortex crearMensajeDeTestSinHash() {
 		final MensajeVortex mensajeDeTest = crearMensajeDeTest();
 		// Le quitamos el hash
 		mensajeDeTest.getIdentificacion().setHashDelContenido(null);
 		return mensajeDeTest;
 	}
 
 	/**
 	 * Crea un metamensaje de publicación de tags que reemplaza tags previos
 	 * 
 	 * @param enviablesORecibibles
 	 *            Los tags declarados
 	 * @return El mensaje creado que contiene el metamensaje
 	 */
 	public MensajeVortex crearMetamensajeDePublicacionDeTags(final String... enviablesORecibibles) {
 		final ArrayList<String> tagsDeclarados = Lists.newArrayList(enviablesORecibibles);
 		return crearMetamensajeDePublicacionDeTags(tagsDeclarados);
 	}
 
 	/**
 	 * Crea un metamensaje de publicación de tags que reemplaza tags previos
 	 * 
 	 * @param tagsDeclarados
 	 *            Los tags declarados enviables o recibibles
 	 * @return El mensaje creado que contiene el metamensaje
 	 */
 	public MensajeVortex crearMetamensajeDePublicacionDeTags(final List<String> tagsDeclarados) {
 		final ReemplazarTags metamensaje = ReemplazarTags.create(tagsDeclarados);
 		return crearMetamensaje(metamensaje);
 	}
 
 	/**
 	 * Crea el mensaje para el metamensaje indicado. El mensaje generado utiliza identificaciones
 	 * distintas para cada mensaje
 	 * 
 	 * @param metamensaje
 	 *            El metamensaje a envolver en un mensaje vortex
 	 * @return El mensaje para el metamensaje
 	 */
 	private MensajeVortex crearMetamensaje(final MetamensajeVortex metamensaje) {
 		final ContenidoVortex contenido = ContenidoVortex.create(metamensaje.getClass().getName(), metamensaje);
 		final IdVortex identificacion = createId();
 		final List<String> tagsDelMensaje = Lists.newArrayList(MensajeVortex.TAG_INTERCAMBIO_VECINO);
 		final MensajeVortex mensajeVortex = MensajeVortex.create(contenido, identificacion, tagsDelMensaje);
 		mensajeVortex.getIdentificacion().setNumeroDeSecuencia(secuencer.getAndIncrement());
 		return mensajeVortex;
 	}
 
 	public MensajeVortex crearMensajeDeTest() {
 		return crearMensajeDeTest("TAG_DE_PRUEBA");
 	}
 
 	public MensajeVortex crearMensajeDeConsumo(final IdVortex idMensaje) {
 		final AcuseConsumo acuse = AcuseConsumo.create();
 		acuse.setIdMensajeConsumido(idMensaje);
 		acuse.setCantidadConsumidos(1L);
 		final MensajeVortex mensajeVortex = crearMetamensaje(acuse);
 		return mensajeVortex;
 	}
 
 	/**
 	 * Crea el mensaje para cerrar la conexión desde el protocolo
 	 */
 	public MensajeVortex crearMensajeDeCierreDeConexion() {
		final MetamensajeVortex cierreConexion = CerrarConexion.create();
 		return crearMetamensaje(cierreConexion);
 	}
 
 	/**
 	 * Crea un mensaje de test asegurando que el ID sea válido
 	 * 
 	 * @param tagsDelMensaje
 	 * @return
 	 */
 	public MensajeVortex crearMensajeDeTestConIDNuevo(final String tagsDelMensaje) {
 		final MensajeVortex mensaje = crearMensajeDeTest(tagsDelMensaje);
 		mensaje.getIdentificacion().setNumeroDeSecuencia(secuencer.getAndIncrement());
 		return mensaje;
 	}
 
 	/**
 	 * Crea un metamensaje inválido de publicación de tags
 	 * 
 	 * @return El mensaje inválido
 	 */
 	public MensajeVortex crearMetamensajeAgregarTagsSinTags() {
 		final MensajeVortex mensaje = crearMetamensajeDePublicacionDeTags();
 		final ContenidoVortex contenido = mensaje.getContenido();
 		final ReemplazarTags reemplazo = (ReemplazarTags) contenido.getValor();
 		// Invalidamos el mensaje
 		reemplazo.setTags(null);
 		return mensaje;
 	}
 
 }
