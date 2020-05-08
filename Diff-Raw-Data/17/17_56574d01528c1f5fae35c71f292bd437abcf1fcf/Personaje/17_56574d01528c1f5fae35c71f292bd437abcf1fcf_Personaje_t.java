 package com.lumpundform.actores;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Vector2;
 import com.lumpundform.acciones.PersonajeAction;
 import com.lumpundform.escenario.EscenarioBase;
 import com.lumpundform.eventos.Evento;
 import com.lumpundform.excepciones.HabilidadInexistenteException;
 import com.lumpundform.habilidades.Habilidad;
 import com.lumpundform.habilidades.Habilidades;
 import com.lumpundform.indicadores.BarraVida;
 import com.lumpundform.indicadores.EtiquetaCantidad;
 import com.lumpundform.utilerias.Fuentes;
 
 /**
  * Clase para todos los personajes del juego, amigos y enemigos.
  * 
  * @author Sergio Valencia
  * 
  */
 public class Personaje extends ObjetoActor {
 	// Valores estáticos de los personajes
 	public static enum Estado {
 		DETENIDO, MOVIMIENTO, CAYENDO, COLISIONANDO;
 	}
 
 	// Estado
 	private Estado estado;
 
 	// Habilidades
 	private Map<String, Habilidad> habilidades;
 
 	// Vida y Mana
 	private float vida;
 	private float mana;
 	private float vidaMax;
 	private float manaMax;
 	private float manaPorSegundo;
 	private BarraVida barraVida;
 
 	// Movimiento
 	private float distanciaAlejamiento;
 
 	private boolean enemigo;
 
 	/**
 	 * Inicializa al {@link Personaje} con todos sus datos necesarios. Sólo debe
 	 * usarse por subclases de {@link Personaje}.
 	 * 
 	 * @param nombre
 	 *            El nombre del {@link ObjetoActor}.
 	 * @param puntoOrigen
 	 *            La posición en donde se va a crear al {@link ObjetoActor}.
 	 */
 	protected Personaje(String nombre, Vector2 puntoOrigen) {
 		super(nombre);
 
 		setHabilidades(new HashMap<String, Habilidad>());
 
 		setX(puntoOrigen.x);
 		setY(puntoOrigen.y);
 
 		setCaer(true);
 
 		addAction(new PersonajeAction());
 	}
 
 	/**
 	 * Carga todas las habilidades del {@link Personaje} dado.
 	 * 
 	 * @param habilidades
 	 *            Los nombres de todas las habilidades.
 	 */
 	protected void cargarHabilidades(String... habilidades) {
 		for (String habilidad : habilidades) {
 			getHabilidades().put(habilidad, Habilidades.nueva(this, habilidad));
 		}
 	}
 
 	/**
 	 * Realiza la {@link Habilidad} especificada del {@link Personaje}.
 	 * 
 	 * @param nombre
 	 *            El nombre de la habilidad a realizar.
 	 */
 	public void habilidad(String nombre) {
 		habilidad(nombre, null);
 	}
 
 	/**
 	 * Realiza la {@link Habilidad} especificada del {@link Personaje} en la
 	 * posición dada.
 	 * 
 	 * @param nombre
 	 *            El nombre de la habilidad a realizar.
 	 * @param pos
 	 *            La posición en donde se va a realizar la habilidad.
 	 */
 	public void habilidad(String nombre, Vector2 pos) {
 		getHabilidad(nombre).ejecutar(pos);
 	}
 
 	/**
 	 * Lee la {@link Habilidad} con el nombre dado del {@link Personaje}.
 	 * 
 	 * @param nombre
 	 *            El nombre de la habilidad.
 	 * @return La {@link Habilidad}.
 	 */
 	protected Habilidad getHabilidad(String nombre) {
 		if (getHabilidades().containsKey(nombre)) {
 			return getHabilidades().get(nombre);
 		} else {
 			throw new HabilidadInexistenteException(getName(), nombre);
 		}
 	}
 
 	/**
 	 * Mueve al {@link Personaje} de acuerdo a su velocidad hacia la dirección
 	 * de su destino.
 	 * 
 	 * @param delta
 	 *            El delta de {@link Screen#render(float)}.
 	 */
 	public void moverDestino(float delta) {
 		if (getDireccionDestinoX() == Direccion.IZQUIERDA && getX() > getDestinoX()) {
 			moverIzquierda(delta);
 		} else if (getDireccionDestinoX() == Direccion.DERECHA && getX() < getDestinoX()) {
 			moverDerecha(delta);
 		}
 	}
 
 	@Override
 	public void moverDerecha(float delta) {
 		super.moverDerecha(delta);
 		if (getEstado() != Estado.CAYENDO) {
 			setEstado(Estado.MOVIMIENTO);
 			setDireccionX(Direccion.DERECHA);
 		}
 	}
 
 	@Override
 	public void moverIzquierda(float delta) {
 		super.moverIzquierda(delta);
 		if (getEstado() != Estado.CAYENDO) {
 			setEstado(Estado.MOVIMIENTO);
 			setDireccionX(Direccion.IZQUIERDA);
 		}
 	}
 
 	@Override
 	protected TextureRegion getCuadroActual() {
 		// Revisar de cual animación se va a agarrar el cuadro actual
 		String nombreAnimacion;
 		switch (getEstado()) {
 		case DETENIDO:
 		default:
 			nombreAnimacion = "detenido";
 			break;
 		case MOVIMIENTO:
 			nombreAnimacion = "corriendo";
 			break;
 		case CAYENDO:
 			nombreAnimacion = "cayendo";
 			break;
 		}
 		if (isColisionActores()) {
 			nombreAnimacion = "colisionando";
 		}
 
 		if (!getAnimacion().containsKey(nombreAnimacion)) {
 			nombreAnimacion = "detenido";
 		}
 
 		return getAnimacion().get(nombreAnimacion).getKeyFrame(getTiempoTranscurrido(), true);
 	}
 
 	/**
 	 * Cambia la dirección del {@link Personaje} hacia donde se encuentra el
 	 * {@link Heroe} en el escenario.
 	 */
 	public void voltearHaciaHeroe() {
 		setDireccionX(getDireccionPosicionHeroe());
 	}
 
 	/**
 	 * Calcula si la distancia entre el {@link Personaje} y el {@link Heroe} es
 	 * mayor a su máxima distancia de separación.
 	 * 
 	 * @return <code>true</code> si la distancia es mayor a la máxima,
 	 *         <code>false</code> si no.
 	 */
 	public boolean lejosDeHeroe() {
 		Heroe heroe = getHeroeEscenario();
 		return ((derecha() && (heroe.getXCentro() - getXCentro()) > getDistanciaAlejamiento()) || ((getXCentro() - heroe
 				.getXCentro()) > getDistanciaAlejamiento()));
 	}
 
 	/**
 	 * Calcula la dirección hacia donde esta el {@link Heroe} con respecto al
 	 * {@link Personaje}.
 	 * 
 	 * @return La {@link com.lumpundform.actores.ObjetoActor.Direccion}
 	 *         correcta.
 	 */
 	public Direccion getDireccionPosicionHeroe() {
 		Heroe heroe = getHeroeEscenario();
 		if (heroe.getPosicionCentro().x < getPosicionCentro().x) {
 			return Direccion.IZQUIERDA;
 		} else {
 			return Direccion.DERECHA;
 		}
 	}
 
 	/**
 	 * Regresa al {@link Heroe}.
 	 * 
 	 * @return El {@link Heroe}.
 	 */
 	public Heroe getHeroeEscenario() {
 		EscenarioBase escenario = (EscenarioBase) getStage();
 		return escenario.getHeroe();
 	}
 
 	/**
 	 * Reduce el cooldown de todas las {@link Habilidad}es del {@link Personaje}
 	 * .
 	 * 
 	 * @param delta
 	 *            El delta de {@link Screen#render(float)}.
 	 */
 	public void reducirCooldownHabilidades(float delta) {
 		Iterator<Habilidad> i = getHabilidades().values().iterator();
 		while (i.hasNext()) {
 			i.next().reducirCooldown(delta);
 		}
 	}
 
 	/**
 	 * Aumenta el mana del {@link Personaje} por tiempo.
 	 * 
 	 * @param delta
 	 *            El delta de {@link Screen#render(float)}.
 	 */
 	public void aumentarMana(float delta) {
 		if (getManaPorSegundo() > 0) {
 			setMana(getMana() + (getManaPorSegundo() * delta));
 		}
 
 		if (getMana() >= getManaMax()) {
 			setMana(getManaMax());
 		}
 	}
 
 	/**
 	 * Quita vida a un {@link Personaje}. Se encarga de que hacer con el
 	 * {@link Personaje} cuando haya muerto.
 	 * 
 	 * @param dano
 	 *            La cantidad de vida a quitar.
 	 */
 	public void quitarVida(float dano) {
 		EscenarioBase escenario = (EscenarioBase) getStage();
 		Evento evento = escenario.getEvento(getPerteneceAEvento());
 		hacerDano(dano);
 		if (getVida() <= 0.0f) {
 			if (evento != null) {
 				evento.matarPersonaje();
 			}
 			if (isEnemigo()) {
 				escenario.crearPocion(getPosicionCentro());
 				getBarraVida().remove();
 				remove();
 			} else {
 				// TODO: Que hacer cuando el héroe muere
 			}
 		}
 	}
 
 	/**
 	 * Quita vida a un {@link Personaje} y muestra la cantidad de daño que se
 	 * hizo con un número arriba del {@link Personaje}.
 	 * 
 	 * @param dano
 	 *            La cantidad de daño a hacer.
 	 */
 	protected void hacerDano(float dano) {
 		Color color = isEnemigo() ? Fuentes.regular().getColor() : Color.RED;
 		getStage().addActor(new EtiquetaCantidad(dano + "", getEsquina("sup-izq"), color));
 		setVida(getVida() - dano);
 
 		if (getVida() < 0) {
 			setVida(0);
 		}
 	}
 
 	/**
 	 * Quita mana a un {@link Personaje}.
 	 * 
 	 * @param mana
 	 *            La cantidad de mana a quitar.
 	 */
 	public void quitarMana(float mana) {
 		this.setMana(this.getMana() - mana);
 		if (this.getMana() <= 0.0f) {
 			this.setMana(0.0f);
 		}
 	}
 
 	/**
 	 * Calcula si el enemigo colisionaría con otros enemigos al moverse.
 	 * 
 	 * @param delta
 	 *            El delta de {@link Screen#render(float)}.
 	 * @return <code>true</code> si sí colisionaría, <code>false</code> si no.
 	 */
	public boolean colisionConEnemigos(float delta) {
 		boolean colision = false;
 
 		EscenarioBase escenario = (EscenarioBase) getStage();
 
 		for (Personaje p : escenario.getActores(Personaje.class)) {
			if (p.isEnemigo() && (p.getId() < getId()) && getHitbox().estaColisionando(p.getHitbox())) {
 				colision = true;
 			}
 		}
 
 		return colision;
 	}
 
 	public Estado getEstado() {
 		return estado;
 	}
 
 	public void setEstado(Estado estado) {
 		this.estado = estado;
 	}
 
 	public float getVida() {
 		return vida;
 	}
 
 	public void setVida(float vida) {
 		this.vida = vida;
 	}
 
 	public float getVidaMax() {
 		return vidaMax;
 	}
 
 	public void setVidaMax(float vidaMax) {
 		this.vidaMax = vidaMax;
 	}
 
 	public float getMana() {
 		return mana;
 	}
 
 	public void setMana(float mana) {
 		this.mana = mana;
 	}
 
 	public float getManaMax() {
 		return manaMax;
 	}
 
 	public void setManaMax(float manaMax) {
 		this.manaMax = manaMax;
 	}
 
 	public float getManaPorSegundo() {
 		return manaPorSegundo;
 	}
 
 	public void setManaPorSegundo(float manaPorSegundo) {
 		this.manaPorSegundo = manaPorSegundo;
 	}
 
 	public Map<String, Habilidad> getHabilidades() {
 		return habilidades;
 	}
 
 	public void setHabilidades(Map<String, Habilidad> habilidades) {
 		this.habilidades = habilidades;
 	}
 
 	public boolean isEnemigo() {
 		return enemigo;
 	}
 
 	public void setEnemigo(boolean enemigo) {
 		this.enemigo = enemigo;
 	}
 
 	public BarraVida getBarraVida() {
 		return barraVida;
 	}
 
 	public void setBarraVida(BarraVida barraVida) {
 		this.barraVida = barraVida;
 	}
 
 	public float getDistanciaAlejamiento() {
 		return distanciaAlejamiento;
 	}
 
 	public void setDistanciaAlejamiento(float distanciaAlejamiento) {
 		this.distanciaAlejamiento = distanciaAlejamiento;
 	}
 }
