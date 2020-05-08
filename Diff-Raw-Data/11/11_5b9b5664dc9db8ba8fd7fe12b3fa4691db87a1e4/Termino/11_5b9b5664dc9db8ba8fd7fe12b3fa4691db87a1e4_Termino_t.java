 /**
  * 
  */
 package funciones;
 
 import java.math.BigDecimal;
 
 import resources.CustomException;
 import resources.O;
 import resources.math.Big;
 import resources.math.Constantes.FuncionTrig;
 import resources.math.Constantes.Tipo;
 import resources.math.M;
 
 /**
  * La clase {@code Termino} define lo que es un trmino en una funcin.
  * Cada trmino tiene los siguientes componentes:
  * <dl>
  * <dt>Coeficiente de trmino {@code A}
  * <dd>La constante que multiplica la funcin especfica del trmino.
  * <dt>Una funcin {@code F(x)}
  * <dd>Depender del tipo de funcin; sea un {@code monomio} de grado {@code n},
  * una funcin {@code trigonomtrica}, {@code logartmica}, {@code exponencial},
  * etc.
  * <dt>Coeficiente de variable {@code B}
  * <dd>Una constante que multiplica la variable {@code x}.
  * <dt>Representacin general y especfica del trmino
  * <dd>General: {@code A*F(B*x)}<br>
  * Especfica: {@code 3*sin(2x)}
  * </dl>
  * <p>
  * Es usada en la clase {@link Funcion} para definir cada un de los trminos.
  * <p>
  * Creada como rediseo de la clase {@link FuncionBase} antes usada para definir
  * funciones.
  * 
  * @author <a href="https://twitter.com/Jedabero" target="_blank">Jedabero</a>
  * @since 0.4
  */
 public class Termino {
 	
 	/**
 	 * Trmino que representa el cero
 	 */
 	public static Termino ZERO = Termino.constante(BigDecimal.ZERO);
 	
 	private BigDecimal A;
 	
 	private BigDecimal B;
 	
 	private Tipo funcion;
 	
 	private FuncionTrig funTrig;
 	private static boolean xInRadians = true;
 	private static boolean xInDegrees = !xInRadians;
 	
 	private int grado;
 	
 	private String generic;
 	private String specific;
 	private String toString;
 
 	/**
 	 * Regresa el valor del coeficiente del trmino.
 	 * @return coeficiente A
 	 */
 	public BigDecimal getA() {
 		return A;
 	}
 
 	/**
 	 * Modifica el valor del coeficiente del trmino.
 	 * @param	a el nuevo valor del coeficiente.
 	 */
 	public void setA(BigDecimal a) {
 		A = a;
 	}
 
 	/**
 	 * Regresa el valor del coeficiente de la variable.
 	 * @return el valor del coeficiente
 	 */
 	public BigDecimal getB() {
 		return B;
 	}
 
 	/**
 	 * Modifica valor del coeficiente de la variable.
 	 * @param	b el nuevo valor del coeficiente
 	 */
 	public void setB(BigDecimal b) {
 		B = b;
 	}
 	
 	/**
 	 * Regresa el tipo de funcin de este trmino.
 	 * @return el tipo de funcin
 	 */
 	public Tipo getTipoFuncion() {
 		return funcion;
 	}
 
 	/**
 	 * Modifica el actual tipo de funcin.
 	 * @param	funcion el nuevo tipo de funcin.
 	 */
 	public void setTipoFuncion(Tipo funcion) {
 		this.funcion = funcion;
 	}
 	
 	/**
 	 * Regresa el tipo de funcin de este trmino.
 	 * @return el tipo de funcin
 	 */
 	public FuncionTrig getFunTrig() {
 		return funTrig;
 	}
 
 	/**
 	 * Modifica el actual tipo de funcin.
 	 * @param	ft el nuevo tipo de funcin.
 	 */
 	public void setFunTrig(FuncionTrig ft) {
 		this.funTrig = ft;
 	}
 	
 	/**
 	 * @return the xInRadians
 	 */
 	public static boolean isXinRadians() {
 		return xInRadians;
 	}
 
 	/**
 	 * @param xInRad the xInRadians to set
 	 */
 	public static void setXinRadians(boolean xInRad) {
 		xInRadians = xInRad;
 		xInDegrees = !xInRad;
 	}
 
 	/**
 	 * @return the xInDegrees
 	 */
 	public static boolean isXinDegrees() {
 		return xInDegrees;
 	}
 
 	/**
 	 * @param xInDeg the xInDegrees to set
 	 */
 	public void setXinDegrees(boolean xInDeg) {
 		xInDegrees = xInDeg;
 		xInRadians = !xInDeg;
 	}
 	
 	/**
 	 * Regresa el grado de  la funcin polinmica.
 	 * @return the grado
 	 */
 	public int getGrado() {
 		return grado;
 	}
 
 	/**
 	 * Modifica el grado de la funcin polinmica
 	 * @param	grado el nuevo grado 
 	 */
 	public void setGrado(int grado) {
 		this.grado = grado;
 	}
 
 	/**
 	 * Evala y regresa el valor del trmino.
 	 * @param	x el valor de la variable independiente
 	 * @return el valor evaluado.
 	 */
 	public BigDecimal valorImagen(BigDecimal x) {
 		switch(this.funcion){
 		case CONSTANTE:
 			return getA();
 		case POLINOMICA:
 			return getA().multiply(x.pow(getGrado()));
 		case TRIGONOMETRICA:
 			return getA().multiply(M.calculaTrig(getFunTrig(),
 					x.multiply(getB()), isXinRadians(), isXinDegrees()));
 		case EXPONENCIAL:
 			return getA().multiply(Big.exp(x.multiply(getB())));
 		case LOGARITMICA:
 			return getA().multiply(Big.ln(x.multiply(getB())));
 		case RACIONAL:
 			break;
 		default: 
 			return null;
 		}
 		return null;
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
 
 	/** Inicializa la representacin especfica y general del trmino. */
 	public void initGenEsp(){
 		String gS = "";
 		switch(getTipoFuncion()){
 		case CONSTANTE:
 			gS += "A";
 			break;
 		case POLINOMICA:
 			int g = getGrado();
 			switch(g){
 			case 0: gS += "A"; break;
 			case 1: gS += "Ax"; break;
 			default: gS += "Ax<sup>"+g+"</sup>"; break;
 			}
 			break;
 		case TRIGONOMETRICA:
 			FuncionTrig ft = getFunTrig();
 			switch(ft){
 			case SIN: gS += "Asin(Bx)"; break;
 			case COS: gS += "Acos(Bx)"; break;
 			case TAN: gS += "Atan(Bx)"; break;
 			case SEC: gS += "Asec(Bx)"; break;
 			case CSC: gS += "Acsc(Bx)"; break;
 			case COT: gS += "Acot(Bx)"; break;
 			default: gS += ""; break;
 			}
 			break;
 		case EXPONENCIAL: gS += "Ae<sup>Bx</sup>"; break;
 		case LOGARITMICA: gS += "Aln(Bx)"; break;
 		case RACIONAL: /* TODO RACIONAL */ break;
 		default: break;
 		}
 		this.generic = gS;
 		
 		String sS = "";
 		BigDecimal a = getA();
 		int signA = a.signum();
 		boolean Aeq1 = a.abs().compareTo(BigDecimal.ONE)==0;
 		boolean Aeq0 = signA==0;
 		BigDecimal b = getB();
 		int signB = b.signum();
 		boolean Beq1 = b.abs().compareTo(BigDecimal.ONE)==0;
 		boolean Beq0 = signB==0;
 		boolean TermEq0 = Aeq0;
 		boolean TermEq1 = false;
 		boolean TermEqInf = false;
 		
 		if(!TermEq0||!TermEq1||!TermEqInf){
 			if(signA==-1) sS +="- ";
 			
 			switch(getTipoFuncion()){
 			case CONSTANTE:
 				toString = ""+a;
 				sS += a.abs();
 				break;
 			case POLINOMICA:
 				int g = getGrado();
 				toString = a+"*x^"+g;
 				switch(g){
 				case 0:
 					sS += a.abs();
 					break;
 				case 1:
 					if(!Aeq1) sS += a.abs();
 					sS += "x";
 					break;
 				default:
 					if(!Aeq1) sS += a.abs();
 					sS += "x<sup>"+g+"</sup>";
 					break;
 				}
 				break;
 				
 			case TRIGONOMETRICA:
 				FuncionTrig ft = getFunTrig();
 				toString = a+"*"+ft+"("+b+"*x)";
 				if(!Beq0){
 					if(!Aeq1) sS += a.abs();
 					
 					switch(ft){
 					case SIN:
 						sS += "sin(";
 						break;
 					case COS:
 						sS += "cos(";
 						break;
 					case TAN:
 						sS += "tan(";
 						break;
 					case SEC:
 						sS += "sec(";
 						break;
 					case CSC:
 						sS += "csc(";
 						break;
 					case COT:
 						sS += "cot(";
 						break;
 					default:
 						sS += "";
 						break;
 					}
 					if(signB==-1) sS +="-";
 					if(!Beq1) sS += b.abs();
 					sS += "x)";
 					break;
 				}else{
 					switch(ft){
 					case SIN:
 					case TAN:
 						break;
 					case COS:
 					case SEC:
 						break;
 					case CSC:
 					case COT:
 						break;
 					default:
 					}
 				}
 			case EXPONENCIAL:
 				toString = a+"*e^("+b+"*x)";
 				if(!Aeq1) sS += a.abs();
 				sS += "e<sup>";
 				if(signB==-1) sS +="-";
 				if(!Beq1) sS += b.abs();
 				sS += "x</sup>";
 				break;
 				
 			case LOGARITMICA:
 				toString = a+"*ln("+b+"*x)";
 				if(!Aeq1) sS += a.abs();
 				sS += "ln(";
 				if(signB==-1) sS +="-";
 				if(!Beq1) sS += b.abs();
 				sS += "x)";
 				break;
 				
 			case RACIONAL:
 				break;
 			default: break;
 			}
 		}else if(TermEq0){
 			sS += "0";
 		}else if(TermEq1){
 			sS += "1";
 		}else if(TermEqInf){
 			sS += "Inf";
 		}
 		
 		this.specific = sS;
 	}
 	
 	public String toString(){
 		return toString;
 	}
 	
 	/**
 	 * Crea un trmino con todos los parmetros.
 	 * @param	a el coeficiente del trmino
 	 * @param	b el coeficiente de la variable
 	 * @param	f el tipo de funcin
 	 * @param	g el grado del trmino
 	 * @param	ft el tipo de funcin trigonomtrica
 	 * @throws CustomException 
 	 */
 	public Termino(BigDecimal a, BigDecimal b, Tipo f, int g,
 			FuncionTrig ft) throws CustomException{
 		A = a;
 		B = b;
 		funcion = f;
 		grado = g;
 		funTrig = ft;
 		switch (funcion) {
 		case CONSTANTE:
 			break;
 		case POLINOMICA:
 			if((g<1) || (g>999999999)){
 				throw CustomException.gradoMenorQue1();
 			}
 			break;
 		case TRIGONOMETRICA:
 			switch(funTrig){
 			case SIN:
 			case COS:
 			case TAN:
 			case SEC:
 				break;
 			case CSC:
 			case COT:
 				if(b.signum()==0){
 					throw CustomException.coeficienteIgualA0(ft+"(0) = NaN");
 				}
 				break;
 			default:
 			}
 			break;
 		case EXPONENCIAL:
 			break;
 		case LOGARITMICA:
 			if(b.signum()==0){
 				throw CustomException.coeficienteIgualA0("ln(0) = NaN");
 			}
 			break;
 		case RACIONAL:
 			break;
 		default:
 			
 			break;
 		}
 		
 		initGenEsp();
 	}
 	
 	/**
 	 * Crea un termino constante cuya imagen siempre ser el valor de coef.
 	 * @param coef
 	 * @return un termino constante
 	 */
 	public static Termino constante(BigDecimal coef){
 		try{
 			return new Termino(coef, BigDecimal.ZERO, Tipo.CONSTANTE, 0, null);
 		}catch(CustomException ce){
 			ce.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Crea un termino de tipo monomio
 	 * @param grado	el grado del monomio
 	 * @param coef	el coeficiente del trmino
 	 * @return un termino de tipo polinmico de grado {@code grado}
 	 */
 	public static Termino monomio(int grado, BigDecimal coef){
 		try{
 			return new Termino(coef, BigDecimal.ZERO, Tipo.POLINOMICA, grado, null);
 		}catch(CustomException et){
 			et.printStackTrace();
 			return constante(coef);
 		}
 	}
 	
 	/**
 	 * @param ft	el tipo de funcin trigonomtrica
 	 * @param coefA	el coeficiente del trmino
 	 * @param coefB	el coeficiente que acompaa a x
 	 * @return un termino de tipo trigonomtrico tipo {@code ft}
 	 */
 	public static Termino trigonometrico(FuncionTrig ft, BigDecimal coefA,
 			BigDecimal coefB){
 		try{
 			return new Termino(coefA, coefB, Tipo.TRIGONOMETRICA, 0, ft);
 		}catch(CustomException et){
 			et.printStackTrace();
 			return trigonometrico(ft, BigDecimal.ONE, coefB);
 		}
 	}
 	
 	/**
 	 * @param coefA el coeficiente del trmino
 	 * @param coefB el coeficiente que acompaa a x
 	 * @return un termino de tipo exponencial
 	 */
 	public static Termino exponencial(BigDecimal coefA, BigDecimal coefB){
 		try{
 			return new Termino(coefA, coefB, Tipo.EXPONENCIAL, 0, null);
 		}catch(CustomException et){
 			et.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Crea un trmino de tipo logartmico con los parmetros dados.
 	 * @param coefA el coeficiente del trmino
 	 * @param coefB el coeficiente que acompaa a x
 	 * @return un termino de tipo logartmico
 	 */
 	public static Termino logaritmo(BigDecimal coefA, BigDecimal coefB){
 		try{
 			return new Termino(coefA, coefB, Tipo.LOGARITMICA, 0, null);
 		}catch(CustomException et){
 			String etm = et.getMessage();
 			O.pln("Error al crear funcin logartmica: "+etm);
 			return logaritmo(coefA, BigDecimal.ONE);
 		}
 	}
 	
 	/**
 	 * @param t
 	 * @return un ter
 	 * @throws CustomException si el termino t es diferente a este termino.
 	 */
 	public Termino suma(Termino t) throws CustomException {
 		Termino temp = copia();
 		if(getTipoFuncion().equals(t.getTipoFuncion())){
 			switch (funcion) {
 			case CONSTANTE:
				temp.actualiza(getA().add(t.getA()).stripTrailingZeros());
 				break;
 			case POLINOMICA:
 				if(getGrado()==t.getGrado()){
					temp.actualiza(getA().add(t.getA()).stripTrailingZeros());
 				}else{
 					throw new CustomException("Termino a sumar de grado diferente.");
 				}
 				break;
 			case TRIGONOMETRICA:
 				if(getFunTrig().equals(t.getFunTrig())){
					temp.actualiza(getA().add(t.getA()).stripTrailingZeros());
 				}else{
 					throw new CustomException("Termino trigonometrico a sumar de tipo diferente.");
 				}
 				break;
 			case EXPONENCIAL:
 			case LOGARITMICA:
 				if(getB().compareTo(t.getB())==0){
					temp.actualiza(getA().add(t.getA()).stripTrailingZeros());
 				}else{
 					throw new CustomException("Terminos incompatibles.");
 				}
 				break;
 			case RACIONAL:
 				break;
 			default:
 				
 				break;
 			}
 			return temp;
 		}else{
 			throw CustomException.tipoIncorrecto();
 		}
 		
 	}
 	
 	/**
 	 * @param m
 	 * @return un termino cuyo coeficiente a es multiplicado por m.
 	 */
 	public Termino multiplica(BigDecimal m){
 		Termino t = copia();
 		t.actualiza(getA().multiply(m).stripTrailingZeros());
 		return t;
 	}
 	
 	/**
 	 * @return una copia de este termino
 	 */
 	public Termino copia(){
 		Termino t = null;
 		try {
 			t = new Termino(getA(), getB(), getTipoFuncion(), getGrado(), getFunTrig());
 		} catch (CustomException e) {
 			e.printStackTrace();
 		}
 		return t;
 	}
 	
 	/**
 	 * Actualiza todos los parmetros de la funcin.
 	 * @param a Constante A
 	 * @param b Constante B
 	 * @param tf Tipo de la funcin
 	 * @param g grado del polinomio, si es del tipo
 	 * @param ft tipos de la funcin trigonomtrica, si es del tipo
 	 */
 	private void actualiza(BigDecimal a, BigDecimal b, Tipo tf, int g,
 			FuncionTrig ft){
 		setA(a);
 		setTipoFuncion(tf);
 		switch (tf) {
 		case POLINOMICA:
 			setGrado(g);
 			break;
 		case TRIGONOMETRICA:
 			setFunTrig(ft);
 		default:
 			setB(b);
 			break;
 		}
 		
 		initGenEsp();
 		
 	}
 	
 	/**
 	 * Actualiza el parmetro de un trmino constante.
 	 * @param a Coeficiente A
 	 */
 	public void actualiza(BigDecimal a){
 		actualiza(a, getB(), getTipoFuncion(), getGrado(), getFunTrig());
 	}
 	
 	/**
 	 * Actualiza los parmetros del trmino polinmico
 	 * @param a Coeficiente A
 	 * @param g Grado de la funcin
 	 */
 	public void actualizaPol(BigDecimal a, int g){
 		actualiza(a, getB(), getTipoFuncion(), g, getFunTrig());
 	}
 	
 	/**
 	 * Actualiza los parmetros del trmino trigonomtrico
 	 * @param a coeficiente A
 	 * @param b constante B
 	 * @param ft tipo de la funcin trigonomtrica
 	 */
 	public void actualizaTrig(BigDecimal a, BigDecimal b, FuncionTrig ft){
 		actualiza(a, b, getTipoFuncion(), getGrado(), ft);
 	}
 	
 	/**
 	 * Actualiza las constantes A y B.
 	 * @param a Coeficiente A
 	 * @param b Constante B
 	 */
 	public void actualiza(BigDecimal a, BigDecimal b){
 		actualiza(a, b, getTipoFuncion(), getGrado(), getFunTrig());
 	}
 	/*
 	public Termino multiplicar(BigDecimal multiplicando){
 		BigDecimal newA = getA().multiply(multiplicando);
 		Termino multiplicado;
 		return null;
 	}
 	*/
 	/**
 	 * TODO derivada del resto de funciones
 	 * @return la derivada de este termino
 	 */
 	public Termino derivada(){
 		switch (this.getTipoFuncion()) {
 		case CONSTANTE:
 			return Termino.constante(BigDecimal.ZERO);
 		case POLINOMICA:
 			BigDecimal a = getA().multiply(BigDecimal.valueOf(getGrado()));
 			int g = getGrado() - 1;
 			switch (g) {
 			case 0:
 				return Termino.constante(a);
 			default:
 				try {
 					return monomio(g, a);
 				} catch (Exception e) {
 					O.pln(e);
 					return null;
 				}
 			}
 			
 		case TRIGONOMETRICA:
 			return null;
 		case EXPONENCIAL:
 			return null;
 		case LOGARITMICA:
 			return null;
 		case RACIONAL:
 			break;
 		default: 
 			return null;
 		}
 		return null;
 	}
 	
 }
