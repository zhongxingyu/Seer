 package com.lumpundform.actores;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector2;
 import com.lumpundform.acciones.HeroeAction;
 import com.lumpundform.colision.Rectangulo;
 import com.lumpundform.escenario.EscenarioBase;
 import com.lumpundform.excepciones.TipoInvalidoException;
 import com.lumpundform.habilidades.Habilidad;
 import com.lumpundform.indicadores.EtiquetaCantidad;
 import com.lumpundform.pociones.PocionMana;
 import com.lumpundform.pociones.PocionVida;
 
 /**
  * El héroe del juego. Sólamente debe haber una instancia de {@link Heroe} en un
  * momento dado.
  * 
  * @author Sergio Valencia
  * 
  */
 public class Heroe extends Mago {
 	private List<Habilidad> habilidadesInterfaz;
 	private float cooldownDano = 0.0f;
 	private float deltaTransparente = 0.0f;
 	private boolean transparente = false;
 	private int movimiento = 0;
 
 	private Map<String, Integer> pociones = new HashMap<String, Integer>();
 	private Map<String, Integer> pocionesMax = new HashMap<String, Integer>();
 
 	/**
 	 * Inicializa al {@link Heroe} con todos sus datos necesarios.
 	 * 
 	 * @param puntoOrigen
 	 *            El punto donde se va a originar el {@link ObjetoActor}.
 	 */
 	public Heroe(Vector2 puntoOrigen) {
 		super("heroe", puntoOrigen);
 
 		setWidth(125.0f);
 		setHeight(150.0f);
 
 		setHitbox(new Rectangulo(getHeight(), getWidth() / 3));
 
 		setEstado(Estado.DETENIDO);
 		setDestinoX(getX());
 		setDireccionX(Direccion.DERECHA);
 		setVelocidad(500);
 
 		setVida(100.0f);
 		setVidaMax(100.0f);
 		setMana(100.0f);
 		setManaMax(100.0f);
 		setManaPorSegundo(5.0f);
 
 		getPociones().put("vida", 3);
 		getPociones().put("mana", 3);
 		getPocionesMax().put("vida", 3);
 		getPocionesMax().put("mana", 3);
 
 		cargarAnimaciones("detenido", "corriendo", "colisionando", "cayendo");
		cargarHabilidades("teletransportar", "disparar", "escudo");
 		setHabilidadesInterfaz();
 
 		addAction(new HeroeAction());
 	}
 
 	/**
 	 * Carga las habilidades que se van a poner en el UI.
 	 */
 	protected void setHabilidadesInterfaz() {
 		// TODO: cargar habilidadesInterfaz de los settings
 		habilidadesInterfaz = new ArrayList<Habilidad>();
 		getHabilidadesInterfaz().add(getHabilidad("disparar"));
 		getHabilidadesInterfaz().add(getHabilidad("escudo"));
 		getHabilidadesInterfaz().add(getHabilidad("teletransportar"));
 	}
 
 	@Override
 	public void draw(SpriteBatch batch, float parentAlpha) {
 		float alpha = parentAlpha;
 		if (isTransparente()) {
 			alpha *= 0.5f;
 		}
 		super.draw(batch, alpha);
 	}
 
 	/**
 	 * Alterna la transparencia del {@link Heroe} después de recibir daño para
 	 * indicar que no puede recibir daño por un momento.
 	 * 
 	 * @param delta
 	 *            Delta que viene de {@link #act(float)}.
 	 */
 	public void actualizarTransparente(float delta) {
 		setDeltaTransparente(getDeltaTransparente() + delta);
 		if (getCooldownDano() <= 0.0f) {
 			setTransparente(false);
 			setDeltaTransparente(0.0f);
 		} else if (getDeltaTransparente() >= 0.08f) {
 			if (isTransparente()) {
 				setTransparente(false);
 			} else {
 				setTransparente(true);
 			}
 			setDeltaTransparente(0.0f);
 		}
 	}
 
 	@Override
 	protected void hacerDano(float dano) {
 		if (getCooldownDano() <= 0.0f) {
 			super.hacerDano(dano);
 			setCooldownDano(1.0f);
 		}
 	}
 
 	/**
 	 * Mueve al {@link Heroe} al presionar las teclas adecuadas.
 	 * 
 	 * @param delta
 	 *            El delta de {@link Screen#render(float)}.
 	 * @param direccion
 	 *            La dirección a la que se está moviendo el {@link Heroe}.
 	 */
 	public void moverHeroe(float delta, String direccion) {
 		if (!isTeletransportar()) {
 			float d = delta;
 			if (!isColisionPiso()) {
 				d = delta * 0.75f;
 			}
 			if (direccion.equals("izquierda")) {
 				moverIzquierda(d);
 			} else if (direccion.equals("derecha")) {
 				moverDerecha(d);
 			}
 		}
 	}
 
 	/**
 	 * Intenta agarrar una poción e incrementar su cuenta de pociones si aún no
 	 * está al máximo de pociones permitidas.
 	 * 
 	 * @param tipo
 	 *            El tipo de poción a agarrar, <code>"vida"</code> ó
 	 *            <code>"mana"</code>.
 	 * @return Si agarró la poción.
 	 */
 	public boolean agarrarPocion(String tipo) {
 		if (getPociones().get(tipo) >= getPocionesMax().get(tipo)) {
 			return false;
 		} else {
 			getPociones().put(tipo, getPociones().get(tipo) + 1);
 			return true;
 		}
 	}
 
 	/**
 	 * Intenta usar una poción si la cantidad de vida ó mana no están a su
 	 * máximo y si tiene pociones del tipo dado.
 	 * 
 	 * @param tipo
 	 *            El tipo de poción que se va a usar, <code>"vida"</code> o
 	 *            <code>"mana"</code>.
 	 */
 	public void usarPocion(String tipo) {
 		if (getPociones().get(tipo) > 0) {
 			if (getValor(tipo) < getValor(tipo, true)) {
 				aumentarValorPocion(tipo);
 				getPociones().put(tipo, getPociones().get(tipo) - 1);
 			}
 		}
 	}
 
 	/**
 	 * Aumenta la vida ó mana consecuencia de usar una poción.
 	 * 
 	 * @param tipo
 	 *            El tipo de valor que se va a aumentar, <code>"vida"</code> o
 	 *            <code>"mana"</code>.
 	 */
 	public void aumentarValorPocion(String tipo) {
 		float cantidad;
 		Color colorEtiqueta;
 		if (tipo.equals("vida")) {
 			cantidad = PocionVida.cantidad;
 			colorEtiqueta = Color.GREEN;
 		} else {
 			cantidad = PocionMana.cantidad;
 			colorEtiqueta = Color.BLUE;
 		}
 		setValor(tipo, getValor(tipo) + cantidad);
 		getStage().addActor(new EtiquetaCantidad("+" + cantidad, getEsquina("sup-izq"), colorEtiqueta));
 		if (getValor(tipo) > getValor(tipo, true)) {
 			setValor(tipo, getValor(tipo, true));
 		}
 	}
 	
 	@Override
 	public void quitar() {
 		((EscenarioBase) getStage()).setHeroeMuerto(true);
 		super.quitar();
 	}
 
 	/**
 	 * Agarra el valor de la vida o mana. Para usarse con las funciones que usen
 	 * tipo vida o mana.
 	 * 
 	 * @param tipo
 	 * @return El valor.
 	 */
 	private Float getValor(String tipo) {
 		return getValor(tipo, false);
 	}
 
 	/**
 	 * Agarra el valor de la vida o mana, opcionalmente se puede agarrar el
 	 * valor máximo. Para usarse con las funciones que usen tipo vida o mana.
 	 * 
 	 * @param tipo
 	 * @param max
 	 *            Si se quiere agarrar el valor máximo.
 	 * @return El valor.
 	 */
 	private Float getValor(String tipo, boolean max) {
 		if (tipo.equals("vida")) {
 			return max ? getVidaMax() : getVida();
 		} else if (tipo.equals("mana")) {
 			return max ? getManaMax() : getMana();
 		} else {
 			throw new TipoInvalidoException(tipo);
 		}
 	}
 
 	/**
 	 * Poner el valor de la vida o mana.
 	 * 
 	 * @param tipo
 	 * @param valor
 	 * @see #getValor(String)
 	 */
 	private void setValor(String tipo, float valor) {
 		setValor(tipo, valor, false);
 	}
 
 	/**
 	 * Poner el valor de la vida o mana.
 	 * 
 	 * @param tipo
 	 * @param valor
 	 * @param max
 	 * @see #getValor(String, boolean)
 	 */
 	private void setValor(String tipo, float valor, boolean max) {
 		if (tipo.equals("vida")) {
 			if (max)
 				setVidaMax(valor);
 			else
 				setVida(valor);
 		} else if (tipo.equals("mana")) {
 			if (max)
 				setManaMax(valor);
 			else
 				setMana(valor);
 		} else {
 			throw new TipoInvalidoException(tipo);
 		}
 	}
 
 	public List<Habilidad> getHabilidadesInterfaz() {
 		return habilidadesInterfaz;
 	}
 
 	public float getCooldownDano() {
 		return cooldownDano;
 	}
 
 	public void setCooldownDano(float cooldownDano) {
 		this.cooldownDano = cooldownDano;
 	}
 
 	public boolean isTransparente() {
 		return transparente;
 	}
 
 	public void setTransparente(boolean transparente) {
 		this.transparente = transparente;
 	}
 
 	public float getDeltaTransparente() {
 		return deltaTransparente;
 	}
 
 	public void setDeltaTransparente(float deltaTransparente) {
 		this.deltaTransparente = deltaTransparente;
 	}
 
 	public Map<String, Integer> getPociones() {
 		return pociones;
 	}
 
 	public Map<String, Integer> getPocionesMax() {
 		return pocionesMax;
 	}
 
 	public int getMovimiento() {
 		return movimiento;
 	}
 
 	public void setMovimiento(int movimiento) {
 		this.movimiento = movimiento;
 	}
 }
