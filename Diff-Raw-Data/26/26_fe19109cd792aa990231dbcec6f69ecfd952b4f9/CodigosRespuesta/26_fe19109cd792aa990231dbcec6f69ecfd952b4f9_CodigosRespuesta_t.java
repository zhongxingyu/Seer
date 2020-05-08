 package practicaacs.fap;
 
 public enum CodigosRespuesta {
 	CONSACEPTADA(00,"Consulta Aceptada."), 
 	CONSDEN(10,"Consulta Denegada."), 
 	CAPTARJ(11,"Consulta Denegada con Captura de Tarjeta."), 
 	TARJETANVALIDA(12,"Consulta Denegada, Tarjeta no Válida."),
 	CUENTANVALIDA(13,"Consulta Denegada, Cuenta especificada no válida."), 
 	IMPORTEEXCLIMITE(14,"Consulta Denegada,el importe especificado excede el límite para la Tarjeta especificada."), 
 	TRANSCUENTASIGUALES(21,"Consulta Denegada, En operación de Traspaso la Cuenta Origen es igual a la Cuenta Destino."),
 	TRANSSINFONDOS(22,"Consulta Denegada, En operación de Traspaso la Cuenta Origen no tiene fondos suficientes para traspasar el IMPORTE especificado."), 
 	TRANSCUENTAORINVALIDA(23,"Consulta Denegada, En operación de Traspaso la Cuenta Origen no es válida."), 
 	TRANSCUENTADESNVALIDA(24,"Consulta Denegada, En operación de Traspaso la Cuenta Destino no es válida.");
 	
 	private int valor;
 	private String msg;
 	
 	private CodigosRespuesta(int valor, String msg){
 		this.valor = valor;
 		this.msg = msg;
 	}
 	
 	public String getMensaje(){
 		return this.msg;
 	}
 	
 	public String getCodigo(){
 		if (this.valor < 10){
 			return '0' + (new Integer(this.valor)).toString();
 		}
 		return (new Integer(this.valor)).toString();
 		
 	}
 
 	public static CodigosRespuesta parse(String string) throws CodigoNoValidoException {
 		try{
 			int cod = new Integer(string);
 			for(CodigosRespuesta c : CodigosRespuesta.values()){
 				if(cod == c.valor)
 					return c;
 			}
 		}catch(NumberFormatException e){}
 		
 		throw new CodigoNoValidoException("O valor '" + string + "' non é un código válido.");
 	}

	public boolean respuestaAceptada(){
		return this.valor == 00;
	}
	
	
 	
 }
