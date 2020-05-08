 /**
  * 
  */
 package funciones;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.util.ArrayList;
 import java.util.ListIterator;
 
 import metodosnumericos.Matriz;
 import metodosnumericos.SistemaEcuacionesLineales;
 import resources.CustomException;
 import resources.O;
 import resources.math.Big;
 import resources.math.Constantes.FuncionTrig;
 import resources.math.Constantes.Tipo;
 import resources.math.Interval;
 
 /**
  * La clase {@code Funcion} define una funcin explcita de la forma
  * {@code y = F(x)}. 
  * Todas las propiedades de esta funcin dependen de las propiedades de los
  * {@code trminos}. Por lo tanto es posible obtener una funcin polinmica de
  * grado n, o una simple funcin trigonomtrica, exponencial o logartmica, o
  * una funcin con trminos combinados.
  * @author Jedabero
  *
  */
 public class Funcion{
 	
 	/**
 	 * Funcin que representa el cero
 	 */
 	public static Funcion ZERO = new Funcion(Termino.ZERO);
 	
 	private ArrayList<Termino> terminos;
 	private Tipo tipo;
 	
 	private String generic;
 	private String specific;
 	private String toString;
 	
 	/**
 	 * @return el Tipo
 	 */
 	public Tipo getTipoFuncion() {
 		return tipo;
 	}
 
 	/**
 	 * @param tipo Tipo 
 	 */
 	public void setTipoFuncion(Tipo tipo) {
 		this.tipo = tipo;
 	}
 
 	/**
 	 * Regresa la representacin general del trmino.
 	 * @return la representacin general
 	 */
 	public String getGeneric() {
 		return generic;
 	}
 
 	/**
 	 * Regresa la representacin especfica del trmino.
 	 * @return la representacin especfica
 	 */
 	public String getSpecific() {
 		return specific;
 	}
 
 	/**
 	 * @return lista de trminos
 	 */
 	public ArrayList<Termino> getTerminos() {
 		return terminos;
 	}
 
 	/**
 	 * @param terminos la lista de trminos
 	 */
 	public void setTerminos(ArrayList<Termino> terminos) {
 		this.terminos = terminos;
 	}
 	
 	/**
 	 * Evala y regresa el valor de la funcin.
 	 * @param x el valor de la variable independiente
 	 * @return el valor evaluado
 	 */
 	public BigDecimal valorImagen(BigDecimal x){
 		ListIterator<Termino> iterator;
 		Termino term;
 		BigDecimal y = new BigDecimal(0);
 		for (iterator = getTerminos().listIterator(); iterator.hasNext();) {
 			term = iterator.next();
 			y = y.add(term.valorImagen(x));
 		}
 		return y;
 	}
 	
 	/** Inicializa la representacin especfica y general del trmino. */
 	public void initGenEsp(){
 		ListIterator<Termino> iterator;
 		String g = "<html>";
 		String s = g;
 		toString = "";
 		
 		for (iterator = getTerminos().listIterator(); iterator.hasNext();) {
 			Termino term = iterator.next();
 			boolean positiveA = term.getA().signum()==1;
 			boolean indexIs0 = iterator.previousIndex()==0;
 			g += (indexIs0?"":(positiveA?" + ":" - "))+ term.getGeneric();
 			s += (indexIs0?"":(positiveA?" + ":" ")) + term.getSpecific();
 			toString += (indexIs0?"":" + ") + term;
 		}
 		
 		this.generic = g+"</html>";
 		this.specific = s+"</html>";
 	}
 	
 	public String toString(){
 		return toString;
 	}
 	
 	/**
 	 * @param lt la lista de trminos que crea la funcin
 	 * 
 	 */
 	public Funcion(ArrayList<Termino> lt){
 		this.setTerminos(lt);
 		initGenEsp();
 	}
 	
 	/**
 	 * @param t el trmino que crea la funcin singular
 	 * 
 	 */
 	public Funcion(Termino t){
 		ArrayList<Termino> alT = new ArrayList<Termino>();
 		alT.add(t);
 		this.setTerminos(alT);
 		this.setTipoFuncion(t.getTipoFuncion());
 		initGenEsp();
 	}
 	
 	/**
 	 * Crea un funcin polinmica de grado {@code grado} con todos los trminos
 	 * 
 	 * @param n el {@code grado} de la funcin
 	 * @param coefs el array con los coeficientes
 	 * @return una funcin polinmica de grado {@code n} con todos los
 	 * trminos
 	 * @throws CustomException 
 	 */
 	public static Funcion polinomio(int n, BigDecimal[] coefs)
 			throws CustomException{
 		if(coefs.length<=n) throw CustomException.arrayIncompleto();
 		ArrayList<Termino> alT = new ArrayList<Termino>();
 		alT.add(Termino.constante(coefs[0]));
 		for(int i=1;i<=n;i++){
 			alT.add(Termino.monomio(i, coefs[i]));
 		}
 		return new Funcion(alT);
 	}
 	
 	/**
 	 * Crea un funcin trigonomtrica de tipo {@code ft}
 	 * @param ft 
 	 * @param coefA 
 	 * @param coefB 
 	 * @return una funcin trigonomtrica de tipo {@code ft}
 	 */
 	public static Funcion trigonometrica(FuncionTrig ft, BigDecimal coefA,
 			BigDecimal coefB){
 		ArrayList<Termino> alT = new ArrayList<Termino>();
 		alT.add(Termino.trigonometrico(ft, coefA, coefB));
 		return new Funcion(alT);
 	}
 	
 	//suma
 	/**
 	 * @param t
 	 * @return la suma de esta funcin y el trmino t
 	 * @throws CustomException
 	 */
 	public Funcion sumar(Termino t)throws CustomException {
 		Funcion tempF = copia();
 		switch (t.getTipoFuncion()) {
 		case CONSTANTE:
 		case POLINOMICA:
 			boolean added = false;
 			ArrayList<Termino> alt = tempF.getTerminos();
 			for (int i = 0; i < alt.size(); i++) {
 				Termino tempT = alt.get(i);
 				if(tempT.getGrado()==t.getGrado()){
 					tempT = tempT.suma(t);
 					alt.set(i, tempT);
 					added = true;
 				}
 			}
 			if (!added) {
 				tempF.getTerminos().add(t);
 			}
 			break;
 
 		default:
 			//TODO soportar sumas de trminos de tipos que no sean POLINOMIO
 			throw new CustomException("tipo de suma no soportado todava");
 		}
 		tempF.initGenEsp();
 		return tempF;
 	}
 	
 	/**
 	 * @param f
 	 * @return well this is obviuos now
 	 * @throws CustomException
 	 */
 	public Funcion sumar(Funcion f){
 		Funcion temp = copia();
 		for (int i = 0; i < f.getTerminos().size(); i++) {
 			try {
 				temp = temp.sumar(f.getTerminos().get(i));
 			} catch (CustomException e) {
 				e.printStackTrace();
 			}
 		}
 		return temp;
 	}
 	
 	/**
 	 * @param vF
 	 * @return la funcin resultante de la suma de las funciones en el vector vF
 	 */
 	public static Funcion sumar(Funcion vF[]){
 		Funcion sum = ZERO;
 		for (int i = 0; i < vF.length; i++) {
 			sum = sum.sumar(vF[i]);
 		}
 		return sum;
 	}
 	
 	//resta
 	//multiplicacin
 	/**
 	 * @param multiplicando
 	 * @return la funcin multiplicada por multiplicando
 	 */
 	public Funcion multiplica(BigDecimal multiplicando){
 		ArrayList<Termino> alt = new ArrayList<Termino>();
 		for (Termino termino : getTerminos()) {
 			alt.add(termino.multiplica(multiplicando));
 		}
 		Funcion tempF = new Funcion(alt);
 		return tempF;
 	}
 
 	//divisin?
 	/**
 	 * @return una copia de esta funcin
 	 */
 	public Funcion copia(){
 		ArrayList<Termino> alT = new ArrayList<Termino>();
 		for (Termino termino : getTerminos()) {
 			alT.add(termino);
 		}
 		return new Funcion(alT);
 	}
 	
 	/**
 	 * Crea una funcin que representa (x + a)^n
 	 * @param n el grado
 	 * @param a la constante a 
 	 * @param sign el signo del binomio
 	 * @return (x+a)^n desarrollado
 	 * @throws Exception 
 	 */
 	public static Funcion binomionN(int n, BigDecimal a){
 		BigDecimal temps[] = new BigDecimal[n+1];
 		for (int i = 0; i < temps.length; i++) {
 			try {
 				temps[i] = Big.combinatoria(n, i).multiply(a.pow(i));
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		
 		BigDecimal coefs[] = new BigDecimal[n+1];
 		for (int i = 0; i < coefs.length; i++) {
 			coefs[i] = temps[n-i];
 		}
 		
 		try {
 			return Funcion.polinomio(n, coefs);
 		} catch (CustomException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * @param ai el array con las constantes
 	 * @return La funcin (x + a0)(x + a1)...(x + ai)...(x + an) desarrollada. 
 	 * @throws CustomException
 	 */
 	public static Funcion polinomioProductorio(BigDecimal ai[]) throws CustomException {
 		BigDecimal temps[] = new BigDecimal[ai.length+1];
 		temps[0] = BigDecimal.ONE;
 		for (int i = 1; i < temps.length; i++) {
 			try {
 				temps[i] = Big.sumaCombinaciones(ai, i);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		BigDecimal coefs[] = new BigDecimal[temps.length];
 		for (int i = 0; i < coefs.length; i++) {
 			coefs[i] = temps[temps.length-1-i];
 		}
 		return Funcion.polinomio(ai.length, coefs);
 	}
 	
 	/**
 	 * Crea una funcin a partir de un conjunto de puntos
 	 * @param x puntos
 	 * @param fx valor de la funcin en los puntos x
 	 * @return una funcin polinmica aproximada a los puntos dados
 	 * @throws Exception 
 	 */
 	public static Funcion aproximacionPolinomialLangrange(BigDecimal x[],
 			BigDecimal fx[]) throws Exception {
 		int numPuntos = x.length;
 		if(numPuntos!=fx.length){	//Se verifica que los puntos estn completos
 			throw CustomException.arrayIncompleto();
 		}else{
 			//Este vector corresponde a los polinomios formados 
 			//por cada trmino de la sumatoria.
 			Funcion polsLagr[] = new Funcion[numPuntos];
 			for (int i = 0; i < numPuntos; i++) {
 				//divisor = Productoria(Xi - Xj)
 				BigDecimal divisor = Big.productoDiferencias(i, x);
 				//F(Xi) y cada divisor no dependen de X, por lo tanto su divisin
 				//es una constante que luego ser multiplicada a cada polinomio
				BigDecimal fxi_PIdxi = fx[i].divide(divisor, 10, RoundingMode.HALF_UP);
				//(x-x0)(x-x1)...(x-xn)
 				BigDecimal negativeXs[] = new BigDecimal[numPuntos];
 				negativeXs = Big.removeElementAt(i, x);
 				for (int j = 0; j < negativeXs.length; j++) {
 					negativeXs[j] = negativeXs[j].negate();
 				}
 				polsLagr[i] = Funcion.polinomioProductorio(negativeXs);
 				polsLagr[i] = polsLagr[i].multiplica(fxi_PIdxi);
 				
 			}
 			
 			return Funcion.sumar(polsLagr);
 		}
 		
 	}
 	
 	/**
 	 * @param x
 	 * @param fx
 	 * @return una funcin polinmica aproximada a los puntos dados
 	 * @throws Exception
 	 */
 	public static Funcion aproximacionPolinomialNewton(BigDecimal x[],
 			BigDecimal fx[]) throws Exception {
 		int numPuntos = x.length;
 		if(numPuntos!=fx.length){	//Se verifica que los puntos estn completos
 			throw CustomException.arrayIncompleto();
 		}else{
			//Se calculan todas las diferencias divididas
 			BigDecimal dd[] = Big.listaDiferenciasDivididas(x, fx);
			//Se calculan cada una de las funciones
 			Funcion[] parte = new Funcion[numPuntos];
 			parte[0] = new Funcion(Termino.constante(dd[0]));
 			for (int i = 1; i < parte.length; i++) {
 				BigDecimal a[] = new BigDecimal[i];
 				for (int j = 0; j < a.length;a[j] = x[j].negate(), j++);
 				parte[i] = polinomioProductorio(a).multiplica(dd[i]);
 			}
 			
 			return sumar(parte);
 		}
 	}
 	
 	/**
 	 * Crea una funcin a partir de un conjunto de puntos
 	 * @param x puntos
 	 * @param fx valor de la funcin en los puntos x
 	 * @return una funcin polinmica aproximada a los puntos dados
 	 * @throws Exception 
 	 */
 	public static Funcion aproximacionPolinomialSimple(BigDecimal x[],
 			BigDecimal fx[]) throws Exception {
 		int numPuntos = x.length;
 		if(numPuntos!=fx.length){	//Se verifica que los puntos estn completos
 			throw CustomException.arrayIncompleto();
 		}else{
 			//Inicializacin del SEL
 			BigDecimal matriz[][] = new BigDecimal[numPuntos][numPuntos+1];
 			for (int i = 0; i < numPuntos; i++) {
 				for (int j = 0; j < numPuntos; j++) {
 					matriz[i][j] = x[i].pow(j);
 				}
 				matriz[i][numPuntos] = fx[i];
 			}
 			SistemaEcuacionesLineales sel = new SistemaEcuacionesLineales(matriz);
 			
 			//Se resuelve el sistema por el mtodo de Cramer
 			Matriz coef = sel.metodoCramer();
 			
 			//Por ltimo se pasa la matriz 1xn a un vector n
 			BigDecimal coefs[] = new BigDecimal[numPuntos];
 			for (int i = 0; i < coefs.length; i++) {
 				coefs[i] = coef.getMatriz()[i][0].stripTrailingZeros();
 			}
 			
 			//Finalmente se crea el polinomio.
 			return Funcion.polinomio(numPuntos-1, coefs);
 			
 		}
 	}
 	
 	/**
 	 * @return la derivada de esta funcin
 	 */
 	public Funcion derivada(){
 		ArrayList<Termino> alT = new ArrayList<Termino>();
 		for (ListIterator<Termino> iterator = getTerminos().listIterator();
 				iterator.hasNext();) {
 			alT.add(iterator.next().derivada()); 
 		}
 		return new Funcion(alT);
 	}
 	
 	private int firstNonZeroCoef(){
 		for (int i = 0; i < getTerminos().size(); i++) {
 			if (getTerminos().get(i).getA().signum()!=0) {
 				return i;
 			}
 		}
 		return -1;
 	}
 	
 	private Funcion gx(int fnzc){
 		ArrayList<Termino> alT = new ArrayList<Termino>(getTerminos().size()-1);
 		switch (fnzc) {
 		case 0:		//Si A0 es != 0, ent g(x) = -A0/h(x), h(x)=:
 			for (int i = 1; i < getTerminos().size(); i++) {
 				Termino t = getTerminos().get(i);
 				if (t.getGrado()<=1) {
 					alT.add(Termino.constante(t.getA()));
 				} else {
 					alT.add(Termino.monomio(t.getGrado()-1, t.getA()));
 				}
 			}
 			return new Funcion(alT);
 			
 		case -1:	//Esto indica que todos los coeficientes son 0, lo cual nunca debe suceder
 			O.pln(-1+" <- wat?");
 			return null;
 		default:	//Si Ai != 0, ent g(x) =:
 			Termino x1 = getTerminos().get(1);
 			for (int i = 0; i < getTerminos().size(); i++) {
 				if(i!=1){
 					Termino t = getTerminos().get(i);
 					t.setA(t.getA().divide(x1.getA(), RoundingMode.HALF_UP).negate());
 					alT.add(t);
 				}
 			}
 			return new Funcion(alT);
 			
 		}
 	}
 	
 	/**
 	 * @param tol tolerancia del error
 	 * @param maxIt mximo nmero de iteraciones
 	 * @param x0 punto inicial
 	 * @return la raz ms cercana a x0
 	 * @throws Exception 
 	 */
 	public BigDecimal metodoPuntoFijo(BigDecimal tol, int maxIt, BigDecimal x0)
 			throws Exception{
 		//Obtener g(x)
 		int fnzc = firstNonZeroCoef();	//Se localiza el la posicin del primer coeficiente diferente de 0
 		Funcion gx = gx(fnzc);
 		
 		boolean fin = false;	//Switch
 		int k = 0;				//ndice de la iteracin
 		BigDecimal xr = BigDecimal.ZERO;
 		while((!fin)&&(k<=maxIt)){
 			BigDecimal e = xr.subtract(x0).abs();	//Error inicial
 			switch (fnzc) {		//xr = g(x0)
 			case 0:
 				Termino t = getTerminos().get(0);
 				xr = t.getA().negate().divide(gx.valorImagen(x0),
 						tol.scale()+3, RoundingMode.HALF_UP);
 				break;
 			case -1:
 				O.pln(-1+" <- again wat?");
 				break;
 			default:
 				xr = gx.valorImagen(x0);
 				break;
 			}
 			
 			if (e.compareTo(tol)<1) {	//Error igual o por debajo de la tolerancia?
 				fin = true;
 			}
 			k++;	//Siguiente iteracin
 			//Asignacin de variables para siguiente iteracin
 			BigDecimal temp = x0;
 			x0 = xr;
 			xr = temp;
 		}
 		
 		if (fin) {
 			O.pln("x = "+x0);
 			return x0;
 		} else {
 			throw new Exception("No converge dentro del valor mximo de iteracin");
 		}
 		
 	}
 	
 	private boolean rootExistentialityCriterion(Interval ab){
 		BigDecimal fa = valorImagen(ab.min());
 		BigDecimal fb = valorImagen(ab.max());
 		BigDecimal ce = fa.multiply(fb);
 		switch (ce.signum()) {
 		case -1:
 			return true;
 		default:
 			return false;
 		}
 	}
 	
 	/**
 	 * @param tol tolerancia del error
 	 * @param maxIt mximo nmero de iteraciones
 	 * @param ab intervalo a evaluar
 	 * @return la raz dentro del intervalo [a,b]
 	 * @throws Exception
 	 */
 	public BigDecimal metodoBiseccion(BigDecimal tol, int maxIt, Interval ab)
 			throws Exception {
 		if (rootExistentialityCriterion(ab)) {
 			boolean fin = false;	//Switch
 			int k = 0;				//ndice de la iteracin
 			BigDecimal xa = BigDecimal.ZERO;
 			while((!fin)&&(k<=maxIt)){
 				BigDecimal xm = ab.centre();	//valor medio
 				BigDecimal e = xa.subtract(xm).abs();	//Error inicial
 				if (e.compareTo(tol)<1) {	//Error igual o por debajo de la tolerancia?
 					fin = true;
 				}else{
 					BigDecimal fxm = valorImagen(xm);
 					if (fxm.signum()>0) {
 						ab.setMax(xm);
 					} else {
 						ab.setMin(xm);
 					}
 				}
 				xa = xm;
 				k++;
 			}
 			
 			if (fin) {
 				O.pln("x = "+xa);
 				return xa;
 			} else {
 				throw new Exception("No converge dentro del valor mximo de iteracin");
 			}
 			
 		} else {
 			throw new Exception("No existe la raz dentro del intervalo.");
 		}
 	}
 	
 	/**
 	 * @param tol tolerancia del error
 	 * @param maxIt mximo nmero de iteraciones
 	 * @param x0 punto inicial
 	 * @return la raz ms cercana a x0
 	 * @throws Exception
 	 */
 	public BigDecimal metodoNewtonRaphson(BigDecimal tol, int maxIt,
 			BigDecimal x0) throws Exception {
 		boolean fin = false;	//Switch
 		int k = 0;				//ndice de la iteracin
 		while((!fin)&&(k<=maxIt)){
 			BigDecimal fx = valorImagen(x0);
 			BigDecimal fpx = derivada().valorImagen(x0);
 			BigDecimal fx_fpx = fx.divide(fpx, tol.scale()+3, RoundingMode.HALF_UP);
 			BigDecimal xr = x0.subtract(fx_fpx);
 			BigDecimal e = x0.subtract(xr).abs();	//Error inicial
 			if (e.compareTo(tol)<1) {	//Error igual o por debajo de la tolerancia?
 				fin = true;
 			}
 			BigDecimal temp = x0;
 			x0 = xr;
 			xr = temp;
 			k++;
 		}
 		
 		if (fin) {
 			O.pln("x = "+x0);
 			return x0;
 		} else {
 			throw new Exception("No converge dentro del valor mximo de iteracin");
 		}
 	}
 	
 	/**
 	 * @param tol tolerancia del error
 	 * @param maxIt mximo nmero de iteraciones
 	 * @param x0 punto inicial
 	 * @param x1 punto secundario
 	 * @return la raz ms cercana a x0 y x1
 	 * @throws Exception
 	 */
 	public BigDecimal metodoSecante(BigDecimal tol, int maxIt, BigDecimal x0,
 			BigDecimal x1) throws Exception {
 		boolean fin = false;	//Switch
 		int k = 0;				//ndice de la iteracin
 		while((!fin)&&(k<=maxIt)){
 			BigDecimal fx0 = valorImagen(x0);
 			BigDecimal fx1 = valorImagen(x1);
 			BigDecimal x1_x0 = x1.subtract(x0);
 			BigDecimal fx1_fx0 = fx1.subtract(fx0);
 			BigDecimal fr = x1_x0.multiply(fx1).divide(fx1_fx0, tol.scale()+3, RoundingMode.HALF_UP);
 			BigDecimal xr = x1.subtract(fr);
 			BigDecimal e = x0.subtract(xr).abs();	//Error inicial
 			if (e.compareTo(tol)<1) {	//Error igual o por debajo de la tolerancia?
 				fin = true;
 			}else{
 				x0 = x1;
 				x1 = xr;
 				k++;
 			}
 			
 		}
 		
 		if (fin) {
 			O.pln("x = "+x0);
 			return x0;
 		} else {
 			throw new Exception("No converge dentro del valor mximo de iteracin");
 		}
 	}
 	
 	/**
 	 * @param tol tolerancia del error
 	 * @param maxIt mximo nmero de iteraciones
 	 * @param ab intervalo a evaluar
 	 * @return la raz dentro del intervalo [a,b]
 	 * @throws Exception
 	 */
 	public BigDecimal metodoRegulaFalsi(BigDecimal tol, int maxIt, Interval ab)
 			throws Exception {
 		if (rootExistentialityCriterion(ab)) {
 			boolean fin = false;	//Switch
 			int k = 0;				//ndice de la iteracin
 			BigDecimal xa = BigDecimal.ZERO;
 			while((!fin)&&(k<=maxIt)){
 				BigDecimal fa = valorImagen(ab.min());
 				BigDecimal fb = valorImagen(ab.max());
 				BigDecimal fb_fa = fb.subtract(fa);
 				BigDecimal fr = ab.length().multiply(fb).divide(fb_fa, tol.scale()+3, RoundingMode.HALF_UP);
 				BigDecimal xr = ab.max().subtract(fr);
 				BigDecimal e = xa.subtract(xr).abs();	//Error inicial
 				if (e.compareTo(tol)<1) {	//Error igual o por debajo de la tolerancia?
 					fin = true;
 				}else{
 					BigDecimal fxr = valorImagen(xr);
 					if (fxr.signum()>1) {
 						ab.setMax(xr);
 					} else {
 						ab.setMin(xr);
 					}
 					xa = xr;
 					k++;
 				}
 				
 			}
 			
 			if (fin) {
 				O.pln("x = "+xa);
 				return xa;
 			} else {
 				throw new Exception("No converge dentro del valor mximo de iteracin");
 			}
 			
 		} else {
 			throw new Exception("No existe la raz dentro del intervalo.");
 		}
 		
 	}
 	
 }
