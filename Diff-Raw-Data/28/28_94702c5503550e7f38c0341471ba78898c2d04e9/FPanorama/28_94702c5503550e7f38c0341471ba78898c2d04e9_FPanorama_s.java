 /**
  * 
  */
 package javavis.jip2d.functions;
 
 import java.util.ArrayList;
 import java.util.PriorityQueue;
 
 import javavis.base.JIPException;
 import javavis.base.parameter.ParamFloat;
 import javavis.base.parameter.ParamInt;
 import javavis.jip2d.base.Function2D;
 import javavis.jip2d.base.FunctionGroup;
 import javavis.jip2d.base.JIPImage;
 import javavis.jip2d.base.Sequence;
 import javavis.jip2d.base.bitmaps.JIPBmpByte;
 import javavis.jip2d.base.geometrics.JIPGeomPoint;
 import javavis.jip2d.base.geometrics.Point2D;
 import javavis.jip2d.base.geometrics.Segment;
 
 /**
  * @author EPS
  * 
  */
 public class FPanorama extends Function2D {
 
 	int ventana;
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 5457374739368893537L;
 
 	/**
 	 * 
 	 */
 	public FPanorama() {
 		name = "Panormica";
 		description = "Crea una panormica a partir de una secuencia";
 		groupFunc = FunctionGroup.Applic;
 
 		ParamFloat p0 = new ParamFloat("FNitzberg", false, true);
 		p0.setDescription("Threshold para Nitzberg");
 		p0.setDefault(50.0f);
 		addParam(p0);
 
 		ParamInt p1 = new ParamInt("ventana", false, true);
 		p1.setDescription("Ventana para la correlacin cruzada");
 		p1.setDefault(1);
 		addParam(p1);
 
 		ParamFloat p2 = new ParamFloat("Lambda", false, true);
 		p2.setDescription("Margen de error para considerar un punto equivalente");
 		p2.setDefault(0.5f);
 		addParam(p2);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * javavis.jip2d.base.Function2D#processImg(javavis.jip2d.base.JIPImage)
 	 */
 	@Override
 	public JIPImage processImg(JIPImage img) throws JIPException {
 		throw new JIPException(
 				"La funcin debe aplicarse sobre una secuencia, no una imagen");
 	}
 
 	@Override
 	public Sequence processSeq(Sequence seq) throws JIPException {
 		int frames = seq.getNumFrames();
 		Sequence original = new Sequence(seq);
 		Sequence grises;
 		if (frames < 2) {
 			throw new JIPException(
 					"La secuencia debe tener al menos dos imgenes");
 		}
 
 		ColorToGray ctg = new ColorToGray();
 		ctg.setParamValue("gray", "BYTE");
 		Nitzberg nitz = new Nitzberg();
 		nitz.setParamValue("thres", getParamValueFloat("FNitzberg"));
 
 		grises = ctg.processSeq(seq);
 		seq = nitz.processSeq(grises);
 
 		for (int i = 0; i < frames - 1; i++) {// recorremos desde el primero
 												// hasta el penltimo, y
 												// accedemos siempre a uno y el
 												// siguiente
 			JIPGeomPoint nitzPrimera = (JIPGeomPoint) seq.getFrame(i);
 			JIPGeomPoint nitzSegunda = (JIPGeomPoint) seq.getFrame(i + 1);
 
 			JIPBmpByte primera = (JIPBmpByte) grises.getFrame(i);
 			JIPBmpByte segunda = (JIPBmpByte) grises.getFrame(i + 1);
 			ArrayList<Segment> desplazamientos = recorrerPuntos(nitzPrimera, nitzSegunda, primera, segunda);
 		}
 
 		return seq;
 	}
 
 	/**
 	 * @param nitzPrimera
 	 * @param nitzSegunda
 	 * @param primera
 	 *            TODO
 	 * @param segunda
 	 *            TODO
 	 * @throws JIPException
 	 */
 	private ArrayList<Segment> recorrerPuntos(JIPGeomPoint nitzPrimera,
 			JIPGeomPoint nitzSegunda, JIPBmpByte primera, JIPBmpByte segunda)
 			throws JIPException {
 		ventana = getParamValueInt("ventana");
 		double divisor, mediaPrimera, mediaSegunda;
 		ArrayList<Segment> desplazamientos = new ArrayList<Segment>();
 		
 
 		ArrayList<Recorte> recortadosPrimera = getRecortes(nitzPrimera, primera);
 		double divisorPrimera;
 		ArrayList<Double> mediasPrimera = new ArrayList<Double>();
 		ArrayList<Double> divisoresPrimera = new ArrayList<Double>();
 		for (int i = 0; i < recortadosPrimera.size(); i++) {
 			double media = calcularMedia((JIPBmpByte)recortadosPrimera.get(i).getRecorte());
 			mediasPrimera.add(media);
 			//System.out.println(media);
 			divisorPrimera = calcularDivisor((JIPBmpByte)recortadosPrimera.get(i).getRecorte(),media);
 			divisoresPrimera.add(divisorPrimera);
 		}
 		
 		ArrayList<Recorte> recortadosSegunda = getRecortes(nitzSegunda, segunda);
 		double divisorSegunda;
 		ArrayList<Double> mediasSegunda = new ArrayList<Double>();
 		ArrayList<Double> divisoresSegunda = new ArrayList<Double>();
 		for (int i = 0; i < recortadosSegunda.size(); i++) {
 			double media = calcularMedia((JIPBmpByte)recortadosSegunda.get(i).getRecorte());
 			mediasSegunda.add(media);
 			//System.out.println(media);
 			divisorSegunda = calcularDivisor((JIPBmpByte)recortadosSegunda.get(i).getRecorte(),media);
 			divisoresSegunda.add(divisorSegunda);
 		}
 		
 		
 		//ArrayList<Double> resultados = new ArrayList<Double>();
 		// para cada recorte de la primera imagen
		for (int i = 0; i < divisoresPrimera.size(); i++) {
 			PriorityQueue<Correlacion> correlaciones = new PriorityQueue<FPanorama.Correlacion>();
 			mediaPrimera = mediasPrimera.get(i);
 			Recorte recortePrimera = recortadosPrimera.get(i);
 			double imagen1[] = ((JIPBmpByte) recortePrimera.getRecorte()).getAllPixels();
 			// para cada recorte de la segunda imagen
			for (int j = 0; j < divisoresSegunda.size(); j++) {
 				divisor = divisoresPrimera.get(i)* divisoresSegunda.get(j);
 				
 				mediaSegunda = mediasSegunda.get(j);
 				
				Recorte recorteSegunda = recortadosPrimera.get(j);
 				Correlacion nueva = new Correlacion();
 				nueva.setPrimerPunto(recortePrimera.getPunto());
 				nueva.setSegundoPunto(recorteSegunda.getPunto());
 				double acumulado = 0;
 				 
 				double imagen2[] = ((JIPBmpByte) recorteSegunda.getRecorte()).getAllPixels();
 				
 				for (int k = 0; k < imagen1.length; k++) {
 					acumulado += (imagen1[k] - mediaPrimera) * (imagen2[k] - mediaSegunda); 
 				}
 				double res = acumulado / divisor;
 				nueva.setCorrelacion(res);
 				correlaciones.add(nueva);
 				//resultados.add(res);
 				//System.out.println("[" + i + ", " + j + "] " + res);
 			}
 			Correlacion CC1 = correlaciones.poll();
 			Correlacion CC2 = correlaciones.poll();
 			
 			if(CC1.getCorrelacion()*getParamValueFloat("Lambda")>CC2.getCorrelacion()) {
 				desplazamientos.add(new Segment(CC1.getPrimerPunto(), CC1.getSegundoPunto()));
 			}
 			
 		}
 		
 		return desplazamientos;
 	}
 
 	/**
 	 * @param nitz imagen con los puntos calculados por Nitzberg.
 	 * @param imagen
 	 * @param ventana
 	 * @param mediaPrimera
 	 * @param recortados
 	 * @return
 	 * @throws JIPException
 	 */
 	private ArrayList<Recorte> getRecortes(JIPGeomPoint nitz, JIPBmpByte imagen)
 			throws JIPException {
 		ArrayList<Recorte >recortados = new ArrayList<FPanorama.Recorte>();
 		for (int j = 0; j < nitz.getLength(); j++) {
 			Recorte nuevo = new Recorte();
 			Point2D punto = nitz.getPoint(j);
 
 			
 			int anchoPrimera = imagen.getWidth();
 			int altoPrimera = imagen.getHeight();
 
 			// calculamos la ventana...
 
 			int filaInicioPrimera = punto.getY() - ventana;
 			if (filaInicioPrimera < 0) {
 				filaInicioPrimera = 0;
 				continue;
 			}
 			/*
 			 * int filaFinPrimera = puntoPrimera.getX()+ventana; if
 			 * (filaFinPrimera >= primera.getHeight()) { filaFinPrimera =
 			 * primera.getHeight()-1; }
 			 */
 
 			int columnaInicioPrimera = punto.getX() - ventana;
 			if (columnaInicioPrimera < 0) {
 				columnaInicioPrimera = 0;
 				continue;
 			}
 			/*
 			 * int columnaFinPrimera = puntoPrimera.getY()+ventana; if
 			 * (filaFinPrimera >= primera.getWidth()) { filaFinPrimera =
 			 * primera.getWidth()-1; }
 			 */
 
 			int tamanoVentana = ventana * 2 + 1;
 			try {
 				Crop recorte = new Crop();
 				recorte.setParamValue("y", filaInicioPrimera);
 				recorte.setParamValue("x", columnaInicioPrimera);
 				if(columnaInicioPrimera + (tamanoVentana)> anchoPrimera)
 					continue;
 				recorte.setParamValue("w",  tamanoVentana);
 				if(filaInicioPrimera + (tamanoVentana)> altoPrimera)
 					continue;
 				recorte.setParamValue("h", tamanoVentana);
 				JIPImage ventanaRecortada = recorte.processImg(imagen);
 				nuevo.setPunto(punto);
 				nuevo.setRecorte(ventanaRecortada);
 				recortados.add(nuevo);
 			} catch (JIPException e) {
 				JIPException aux = new JIPException("ERROR RECORTE - j = " + j + " - filaInicio = " + filaInicioPrimera + " - columnaInicio = " + columnaInicioPrimera + "- "
 						+ e.getMessage());
 				throw aux;
 			}
 		}
 		return recortados;
 	}
 
 	/**
 	 * @param imagen
 	 * @param ancho
 	 * @param filaInicioPrimera
 	 * @param columnaInicioPrimera
 	 * @param tamanoVentana
 	 * @throws JIPException 
 	 */
 	private float calcularMedia(JIPBmpByte imagen) throws JIPException {
 		float media;
 		double bytes[] = imagen.getAllPixels();
 		int tamanoTotalVentana = imagen.getHeight()*imagen.getWidth();
 		long acumulado = 0; 
 		for (int i =0; i < imagen.getHeight(); i++) {
 			for (int l = 0; l < imagen.getWidth(); l++) {
 				acumulado += bytes[i*imagen.getWidth() + l]; 
 				//System.out.println("["+i+","+l+"]="+bytes[i*imagen.getWidth() + l]);
 			} 
 		} 
 		media = (float)acumulado/(float)tamanoTotalVentana;
 		return media;
 	}
 
 	/**
 	 * @param imagen
 	 * @param ancho
 	 * @param filaInicioPrimera
 	 * @param columnaInicioPrimera
 	 * @param tamanoVentana
 	 * @throws JIPException 
 	 */
 	private double calcularDivisor(JIPBmpByte imagen, double media) throws JIPException {
 		double bytes[] = imagen.getAllPixels();
 		double acumulado = 0;
 		for (int i = 0; i < bytes.length; i++) {
 			double aux = bytes[i] - media;
 			acumulado += aux*aux;
 		}
 		return Math.sqrt(acumulado);
 	}
 	
 	class Correlacion implements Comparable<Correlacion>{
 		private Point2D primerPunto, segundoPunto;
 		private double correlacion;
 		
 		public Correlacion() {}
 		
 		public double getCorrelacion(){
 			return correlacion;
 		}
 		
 		public Point2D getPrimerPunto(){
 			return primerPunto;
 		}
 		
 		public Point2D getSegundoPunto(){
 			return segundoPunto;
 		}
 		
 		public void setCorrelacion(double corr){
 			correlacion = corr;
 		}
 		public void setPrimerPunto(Point2D punto){
 			primerPunto = punto;
 		}
 		public void setSegundoPunto(Point2D punto){
 			segundoPunto = punto;
 		}
 
 		@Override
 		public int compareTo(Correlacion o) {
 			if (o.getCorrelacion() > correlacion)
 				return 1;
 			else if (o.getCorrelacion() < correlacion)
 				return -1;
 			else
 				return 0;
 		}
 		
 		public String toString() {
 			return "["+primerPunto+"]->["+segundoPunto+"]"+correlacion;
 		}
 		
 	}
 	
 	class Recorte{
 		private Point2D punto;
 		private JIPImage recorte;
 		
 		public Point2D getPunto() {
 			return punto;
 		}
 		public JIPImage getRecorte() {
 			return recorte;
 		}
 		public void setPunto(Point2D punto) {
 			this.punto = punto;
 		}
 		public void setRecorte(JIPImage recorte) {
 			this.recorte = recorte;
 		}
 		
 	}
 	
 }
