 package com.lumpundform.animacion;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Animation;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.utils.Array;
 import com.lumpundform.actores.ObjetoActor;
 import com.lumpundform.utilerias.D;
 import com.lumpundform.utilerias.SpriteSheet;
 import com.lumpundform.utilerias.Texturas;
 
 /**
  * Guarda las animaciones a usarse en el juego. Puede tener mas de una
  * {@link Animation} si es que la animación tiene principio y fin.
  * 
  * @author Sergio Valencia
  * 
  */
 public class Animacion {
 	private ObjetoActor actor;
 
 	private float tiempoPorCuadro;
 
 	private boolean terminada;
	private boolean marcadaTerminar;
 
 	// Animaciones
 	private String animacionAnterior;
 	private String animacionActual;
 	private Map<String, Animation> animaciones;
 
 	// Cuadros
 	private Array<TextureRegion> cuadrosInicio;
 	private Array<TextureRegion> cuadrosLoop;
 	private Array<TextureRegion> cuadrosFin;
 
 	/**
 	 * Se busca en el archivo datos.xml de acuerdo al nombre del
 	 * {@link ObjetoActor} y al tipo de animación para generar la
 	 * {@link Animacion} correcta.
 	 * 
 	 * @param actor
 	 *            El {@link ObjetoActor} del cual leer la animación
 	 * @param tipo
 	 *            El nombre de la animación.
 	 */
 	public Animacion(ObjetoActor actor, String tipo) {
 		this.actor = actor;
 
 		tiempoPorCuadro = 0.05f;
 
 		SpriteSheet ss = D.ss(actor.getName(), tipo);
 
 		Texture texturaAnimacion = Texturas.get(ss.getRuta());
 		TextureRegion[][] tmp = TextureRegion.split(texturaAnimacion, (int) actor.getWidthTextura(),
 				(int) actor.getHeightTextura());
 		cuadrosLoop = new Array<TextureRegion>();
 		for (int i = 0; i < ss.getRenglones(); i++) {
 			for (int j = ss.getColumnasOffset(); j < ss.getColumnas() + ss.getColumnasOffset(); j++) {
 				cuadrosLoop.add(tmp[i][j]);
 			}
 		}
 
 		animaciones = new HashMap<String, Animation>();
 		if (ss.getCuadrosInicio() > 0) {
 			cuadrosInicio = new Array<TextureRegion>();
 			for (int i = 0; i < ss.getCuadrosInicio(); i++) {
 				cuadrosInicio.add(cuadrosLoop.get(i));
 			}
 			animaciones.put("inicio", new Animation(tiempoPorCuadro, cuadrosInicio));
 			animacionActual = "inicio";
 			cuadrosLoop.removeAll(cuadrosInicio, true);
 			cuadrosLoop.shrink();
 		}
 		if (ss.getCuadrosFin() > 0) {
 			int cuadros = ss.getCuadrosFin();
 			cuadrosFin = new Array<TextureRegion>();
 			for (int cf = 0; cf < cuadros; cf++) {
 				cuadrosFin.add(cuadrosLoop.get(cuadrosLoop.size - (cuadros - cf)));
 			}
 			animaciones.put("fin", new Animation(tiempoPorCuadro, cuadrosFin));
 			cuadrosLoop.removeAll(cuadrosFin, true);
 			cuadrosLoop.shrink();
 		}
 		animaciones.put("loop", new Animation(tiempoPorCuadro, cuadrosLoop));
 		if (animacionActual == null)
 			animacionActual = "loop";
 	}
 
 	/**
 	 * Calcula cuál es la animación a usar (inicio, loop o fin) dependiendo del
 	 * tiempo transcurrido.
 	 * 
 	 * @return La {@link Animation} a usar.
 	 */
 	public Animation actual() {
 		if (animacionActual == "inicio" && animaciones.get("inicio") != null
 				&& actor.getTiempoTranscurrido() <= tiempoPorCuadro * cuadrosInicio.size) {
 			animacionActual = "inicio";
 		} else if (animacionActual == "fin" && animaciones.get("fin") != null) {
 			if (actor.getTiempoTranscurrido() > tiempoPorCuadro * (cuadrosFin.size - 1)) {
 				terminada = true;
 			}
 			animacionActual = "fin";
 		} else {
 			animacionActual = "loop";
 		}
 		if (animacionActual != animacionAnterior) {
 			actor.setTiempoTranscurrido(0.0f);
 		}
 		animacionAnterior = animacionActual;
 		return animaciones.get(animacionActual);
 	}
 
 	/**
 	 * Termina la animación. Si tiene animación final, la ejecuta antes de
 	 * terminar.
 	 */
 	public void terminar() {
		if (!marcadaTerminar) {
			if (animaciones.get("fin") != null) {
				animacionActual = "fin";
				actor.setTiempoTranscurrido(0.0f);
			} else {
				terminada = true;
			}
			marcadaTerminar = true;
 		}
 	}
 
 	public boolean isTerminada() {
 		return terminada;
 	}
 
 }
