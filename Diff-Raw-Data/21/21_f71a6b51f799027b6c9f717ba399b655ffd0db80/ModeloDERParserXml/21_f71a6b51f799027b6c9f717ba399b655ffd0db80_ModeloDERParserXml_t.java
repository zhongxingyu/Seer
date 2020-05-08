 package mereditor.xml;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import mereditor.control.AtributoControl;
 import mereditor.control.DiagramaDERControl;
 import mereditor.control.EntidadControl;
 import mereditor.control.JerarquiaControl;
 import mereditor.control.RelacionControl;
 import mereditor.modelo.Atributo;
 import mereditor.modelo.DiagramaDER;
 import mereditor.modelo.Entidad;
 import mereditor.modelo.Entidad.Identificador;
 import mereditor.modelo.Jerarquia;
 import mereditor.modelo.Proyecto;
 import mereditor.modelo.Relacion;
 import mereditor.modelo.Validacion;
 import mereditor.modelo.base.Componente;
 
 import org.w3c.dom.Attr;
 import org.w3c.dom.DOMException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 public class ModeloDERParserXml extends ModeloParserXml {
 
 	public ModeloDERParserXml(Proyecto proyecto, String path) throws Exception {
 		super(proyecto,path);
 		
 	}
 
 	public ModeloDERParserXml(Proyecto proyecto) throws Exception {
 		super(proyecto);
 	}
 
 	/**
 	 * Genera un documento de XML del modelo del proyecto.
 	 * 
 	 * @param proyecto
 	 *            Instancia del proyecto del que se quiere generar el XML.
 	 * @return Documento XML.
 	 * @throws DOMException
 	 * @throws Exception
 	 */
 	public Document generarXml() throws DOMException, Exception {
 		Document doc = this.docBuilder.newDocument();
 		this.root = doc.createElement(Constants.PROYECTO_TAG);
 		doc.appendChild(this.root);
 
 		for (Componente componente : proyecto.getComponentes()) {
 			if (componente.es(Entidad.class) || componente.es(Relacion.class) || componente.es(Jerarquia.class)
 					|| componente == proyecto.getDiagramaRaiz())
 				this.root.appendChild(this.convertirXmlizable(componente).toXml(this));
 		}
 
 		this.root.appendChild(this.convertirXmlizable(this.proyecto.getValidacion()).toXml(this));
 
 		return doc;
 	}
 
 	/**
 	 * Parsea y devuelve el modelo del proyecto contenido en el archivo XML
 	 * asociado.
 	 * 
 	 * @return DiagramaDER principal
 	 * @throws Exception
 	 */
 	@Override
 	public Object parsearXml() throws Exception {
 		// Obtener el id del diagrama principal
 		Element diagramaXml = XmlHelper.querySingle(this.root, Constants.DIAGRAMA_QUERY);
 		DiagramaDER diagrama = (DiagramaDER) this.resolver(this.obtenerId(diagramaXml));
 		// Obtener la validacion principal
 		Validacion validacion = (Validacion) this.obtenerValidacion(this.root);
 
 		this.proyecto.setDiagramaRaiz(diagrama);
 		this.proyecto.setValidacion(validacion);
 
 		/*
 		 * Recorrer todos los elemento de primer nivel (menos validacion) para
		 * tener en cuenta los que existen pero no fueron agregados a ning�n
 		 * diagrama.
 		 */
 		List<Element> elementos = XmlHelper.query(this.root, Constants.ELEMENTOS_PRIMER_NIVEL_QUERY);
 		for (Element elemento : elementos)
 			this.resolver(this.obtenerId(elemento));
 		return proyecto;
 	}
 
 	
 
 	/**
 	 * Obtiene una lista de atributos correspondientes a un componente.
 	 * 
 	 * @param elemento
 	 * @return
 	 * @throws Exception
 	 */
 	List<Atributo> obtenerAtributos(Element elemento) throws Exception {
 		List<Element> atributosXml = XmlHelper.query(elemento, Constants.ATRIBUTOS_QUERY);
 		List<Atributo> atributos = new ArrayList<>();
 
 		for (Element atributoXml : atributosXml) {
 			Atributo atributo = (Atributo) this.resolver(this.obtenerId(atributoXml));
 			atributos.add(atributo);
 		}
 
 		return atributos;
 	}
 
 	
 
 	/**
 	 * Obtiene una lista de los identificadores externos de una entidad.
 	 * 
 	 * @param elemento
 	 * @return
 	 * @throws Exception
 	 */
 	List<Identificador> obtenerIdentificadores(Element elemento, Entidad entidad) throws Exception {
 		List<Element> identificadoresXml = XmlHelper.query(elemento, Constants.IDENTIFICADORES_QUERY);
 		List<Identificador> identificadores = new ArrayList<>();
 
 		for (Element identificadorXml : identificadoresXml) {
 			Identificador identificador = entidad.new Identificador(entidad);
 			
 			for(Entidad identificadorEntidad : this.obtenerReferenciasEntidad(identificadorXml)) {
 				identificador.addEntidad(identificadorEntidad);
 			}
 			
 			for(Atributo identificadorAtributo : this.obtenerReferenciasAtributo(identificadorXml)) {
 				identificador.addAtributo(identificadorAtributo);
 			}
 
 			identificadores.add(identificador);
 		}
 
 		return identificadores;
 	}
 	
 	List<Entidad> obtenerReferenciasEntidad(Element elemento) throws Exception {
 		List<Element> referenciasXml = XmlHelper.query(elemento, Constants.ENTIDAD_REF_QUERY);
 		List<Entidad> entidades = new ArrayList<>();
 		
 		for (Element referenciaXml : referenciasXml) {
 			entidades.add((Entidad)this.obtenerReferencia(referenciaXml));
 		}
 		
 		return entidades;
 	}
 
 	List<Atributo> obtenerReferenciasAtributo(Element elemento) throws Exception {
 		List<Element> referenciasXml = XmlHelper.query(elemento, Constants.ATRIBUTO_REF_QUERY);
 		List<Atributo> atributos = new ArrayList<>();
 		
 		for (Element referenciaXml : referenciasXml) {
 			atributos.add((Atributo)this.obtenerReferencia(referenciaXml));
 		}
 		
 		return atributos;
 	}
 
 	/**
 	 * Obtiene todos los componentes hijos de un diagrama.
 	 * 
 	 * @param elemento
 	 * @return
 	 * @throws Exception
 	 */
 	List<Componente> obtenerComponentes(Element elemento) throws Exception {
 		List<Element> diagramasXml = XmlHelper.query(elemento, Constants.DIAGRAMA_COMPONENTES_QUERY);
 		List<Componente> componentes = new ArrayList<>();
 
 		for (Element diagramaXml : diagramasXml)
 			componentes.add(this.obtenerReferencia(diagramaXml));
 
 		return componentes;
 	}
 
 	/**
 	 * Obtiene todos los diagramas hijos de un diagrama.
 	 * 
 	 * @param elemento
 	 * @return
 	 * @throws Exception
 	 */
 	List<Componente> obtenerDiagramas(Element elemento) throws Exception {
 		List<Element> diagramasXml = XmlHelper.query(elemento, Constants.DIAGRAMA_DIAGRAMAS_QUERY);
 		List<Componente> diagramas = new ArrayList<>();
 
 		for (Element diagramaXml : diagramasXml)
 			diagramas.add(this.resolver(this.obtenerId(diagramaXml)));
 
 		return diagramas;
 	}
 
 	/**
 	 * Obtiene el atributo original de un atributo de tipo copia.
 	 * 
 	 * @param elemento
 	 * @return
 	 * @throws Exception
 	 */
 	Atributo obtenerOriginalAtributo(Element elemento) throws Exception {
 		Element element = XmlHelper.querySingle(elemento, Constants.ORIGINAL_QUERY);
 
 		if (element != null)
 			return (Atributo) this.resolver(element.getAttribute(Constants.IDREF_ATTR));
 
 		return null;
 	}
 
 	/**
 	 * Obtiene la entidad generica de una jerarquia.
 	 * 
 	 * @param elemento
 	 * @return
 	 * @throws Exception
 	 */
 	Componente obtenerGenerica(Element elemento) throws Exception {
 		Element generica = XmlHelper.querySingle(elemento, Constants.GENERICA_QUERY);
 		String id = generica.getAttribute(Constants.IDREF_ATTR);
 
 		return this.resolver(id);
 	}
 
 	/**
 	 * Obtiene la lista de entidades derivadas de una jerarquia.
 	 * 
 	 * @param elemento
 	 * @return
 	 * @throws Exception
 	 */
 	List<Componente> obtenerDerivadas(Element elemento) throws Exception {
 		List<Element> derivadasXml = XmlHelper.query(elemento, Constants.DERIVADAS_QUERY);
 		List<Componente> derivadas = new ArrayList<>();
 
 		for (Element derivadaXml : derivadasXml) {
 			String id = derivadaXml.getAttribute(Constants.IDREF_ATTR);
 			derivadas.add(this.resolver(id));
 		}
 
 		return derivadas;
 	}
 
 	/**
 	 * Obtiene la entidad participante de la relacion.
 	 * 
 	 * @param elemento
 	 * @return
 	 * @throws Exception
 	 */
 	Componente obtenerEntidadParticipante(Element elemento) throws Exception {
 		Element entidadRefXml = XmlHelper.querySingle(elemento, Constants.ENTIDAD_REF_QUERY);
 		return this.obtenerReferencia(entidadRefXml);
 	}
 
 	/**
 	 * Obtiene el valor del estado de un elemento de validacion.
 	 * 
 	 * @param elemento
 	 * @return
 	 */
 	String obtenerEstado(Element elemento) {
 		String estado = elemento.getAttribute(Constants.ESTADO_ATTR);
 		return estado;
 	}
 
 	/**
 	 * Obtiene las observacion de un elemento de validacion.
 	 * 
 	 * @param elemento
 	 * @return
 	 */
 	String obtenerObservaciones(Element elemento) {
 		Element observacionesXml = XmlHelper.querySingle(elemento, Constants.OBSERVACIONES_QUERY);
 		return observacionesXml == null ? null : observacionesXml.getTextContent();
 	}
 
 	
 
 	/**
 	 * Obtiene el valor del atributo tipo de un elemento.
 	 * 
 	 * @param elemento
 	 * @return
 	 */
 	String obtenerTipo(Element elemento) {
 		return elemento.getAttribute(Constants.TIPO_ATTR);
 	}
 
 	/**
 	 * Obtiene la cardinalidad minima y maxima de un atributo o relacion.
 	 * 
 	 * @param elemento
 	 * @return
 	 */
 	String[] obtenerCardinalidad(Element elemento) {
 		Element cardinalidad = XmlHelper.querySingle(elemento, Constants.CARDINALIDAD_QUERY);
 		return new String[] { cardinalidad.getAttribute(Constants.CARDINALIDAD_MIN_ATTR),
 				cardinalidad.getAttribute(Constants.CARDINALIDAD_MAX_ATTR) };
 	}
 
 	/**
 	 * Obtiene el valor de la formula de un atributo.
 	 * 
 	 * @param elemento
 	 * @return
 	 */
 	String obtenerFormulaAtributo(Element elemento) {
 		Element element = XmlHelper.querySingle(elemento, Constants.FORMULA_QUERY);
 		return element == null ? null : element.getTextContent();
 	}
 
 	/**
 	 * Obtiene los elemento de participantes de un elemento relacion.
 	 * 
 	 * @param elemento
 	 * @return
 	 */
 	List<Element> obtenerParticipantes(Element elemento) {
 		return XmlHelper.query(elemento, Constants.PARTICIPANTES_QUERY);
 	}
 
 	/**
 	 * Obtiene el rol de la entidad participante de la relacion.
 	 * 
 	 * @param elemento
 	 * @return
 	 */
 	String obtenerRol(Element elemento) {
 		Element rolXml = XmlHelper.querySingle(elemento, Constants.ROL_QUERY);
 		return rolXml == null ? null : rolXml.getTextContent();
 	}
 
 	/**
 	 * Obtiene el objeto de validacion asociado con un diagrama.
 	 * 
 	 * @param elemento
 	 * @return
 	 * @throws Exception
 	 */
 	Validacion obtenerValidacion(Element elemento) throws Exception {
 		Element validacionXml = XmlHelper.querySingle(elemento, Constants.VALIDACION_QUERY);
 		ValidacionXml validacion = (ValidacionXml) this.mapeoXmlizable(validacionXml);
 		validacion.fromXml(validacionXml, this);
 		return validacion;
 	}
 
 	/**
	 * Devuelve una instancia de la clase correspondiente de parseo seg�n el
 	 * nombre del elemento a parsear.
 	 * 
 	 * @param element
 	 * @return
 	 * @throws Exception
 	 */
 	protected Xmlizable mapeoXmlizable(Element element) throws Exception {
 		switch (element.getNodeName()) {
 		case Constants.ENTIDAD_TAG:
 			return new EntidadXml();
 		case Constants.RELACION_TAG:
 			return new RelacionXml();
 		case Constants.JERARQUIA_TAG:
 			return new JerarquiaXml();
 		case Constants.DIAGRAMA_TAG:
 			return new DiagramaXml(this.proyecto);
 		case Constants.ATRIBUTO_TAG:
 			return new AtributoXml();
 		case Constants.VALIDACION_TAG:
 			return new ValidacionXml();
 		}
 
 		throw new Exception("No existe un mapeo para: " + element.getNodeName());
 	}
 
 	protected Xmlizable convertirXmlizable(Object componente) throws Exception {
 		if (Entidad.class.isInstance(componente))
 			return new EntidadXml((EntidadControl) componente);
 		if (Relacion.class.isInstance(componente))
 			return new RelacionXml((RelacionControl) componente);
 		if (Jerarquia.class.isInstance(componente))
 			return new JerarquiaXml((JerarquiaControl) componente);
//		if (DiagramaDER.class.isInstance(componente))
//			return new DiagramaXml(this.proyecto, (DiagramaDERControl) componente);
 		if (Atributo.class.isInstance(componente))
 			return new AtributoXml((AtributoControl) componente);
 		if (Validacion.class.isInstance(componente))
 			return new ValidacionXml((Validacion) componente);
 
 		throw new Exception("No existe un mapeo para: " + componente.toString());
 	}
 
 	
 
 	Attr agregarTipo(Element elemento, String valor) {
 		return this.agregarAtributo(elemento, Constants.TIPO_ATTR, valor);
 	}
 
 	Element agregarElementoAtributos(Element elemento) {
 		return this.agregarElemento(elemento, Constants.ATRIBUTOS_TAG, "");
 	}
 
 	Element agregarIdentificadores(Element elemento) {
 		return this.agregarElemento(elemento, Constants.IDENTIFICADORES_TAG);
 	}
 	
 	Element agregarIdentificador(Element elemento) {
 		return this.agregarElemento(elemento, Constants.IDENTIFICADOR_TAG);
 	}
 
 	Element agregarComponentes(Element elemento) {
 		return this.agregarElemento(elemento, Constants.COMPONENTES_TAG);
 	}
 
 	Element agregarComponente(Element componentesElement, String id) {
 		Element componenteElement = this.agregarElemento(componentesElement, Constants.COMPONENTE_TAG);
 		this.agregarAtributo(componenteElement, Constants.IDREF_ATTR, id);
 		return componenteElement;
 	}
 
 	Element agregarDiagramas(Element elemento) {
 		return this.agregarElemento(elemento, Constants.DIAGRAMAS_TAG);
 	}
 
 	Element agregarCardinalidad(Element elemento, String cardinalidadMinima, String cardinalidadMaxima) {
 		Element cardinalidad = this.agregarElemento(elemento, Constants.CARDINALIDAD_TAG);
 		this.agregarAtributo(cardinalidad, Constants.CARDINALIDAD_MIN_ATTR, cardinalidadMinima);
 		this.agregarAtributo(cardinalidad, Constants.CARDINALIDAD_MAX_ATTR, cardinalidadMaxima);
 		return cardinalidad;
 	}
 
 	Element agregarFormula(Element elemento, String formula) {
 		Element formulaElemento = this.agregarElemento(elemento, Constants.FORMULA_TAG, formula);
 		return formulaElemento;
 	}
 
 	Element agregarOriginal(Element elemento, String id) {
 		Element origen = this.agregarElemento(elemento, Constants.ORIGEN_TAG);
 		Element refAtributo = this.agregarElemento(origen, Constants.REFATRIBUTO_TAG);
 		this.agregarAtributo(refAtributo, Constants.IDREF_ATTR, id);
 		return origen;
 	}
 
 	Element agregarReferenciaAtributo(Element elemento, String id) {
 		Element refAtributo = this.agregarElemento(elemento, Constants.REFATRIBUTO_TAG);
 		this.agregarAtributo(refAtributo, Constants.IDREF_ATTR, id);
 		return refAtributo;
 	}
 
 	Element agregarReferenciaEntidad(Element elemento, String id) {
 		Element refEntidad = this.agregarElemento(elemento, Constants.REFENTIDAD_TAG);
 		this.agregarAtributo(refEntidad, Constants.IDREF_ATTR, id);
 		return refEntidad;
 	}
 
 	Element agregarGenerica(Element elemento, String id) {
 		Element genericaElemento = this.agregarElemento(elemento, Constants.GENERICA_TAG);
 		this.agregarReferenciaEntidad(genericaElemento, id);
 		return genericaElemento;
 	}
 
 	Element agregarDerivadas(Element elemento) {
 		return this.agregarElemento(elemento, Constants.DERIVADAS_TAG);
 	}
 
 	Element agregarDerivada(Element derivadasElement, String id) {
 		return this.agregarReferenciaEntidad(derivadasElement, id);
 	}
 
 	Element agregarParticipantes(Element elemento) {
 		return this.agregarElemento(elemento, Constants.PARTICIPANTES_TAG);
 	}
 
 	Element agregarParticipante(Element elemento) {
 		return this.agregarElemento(elemento, Constants.PARTICIPANTE_TAG);
 	}
 
 	Attr agregarEstado(Element elemento, String estado) {
 		return this.agregarAtributo(elemento, Constants.ESTADO_ATTR, estado);
 	}
 
 	Element agregarObservaciones(Element elemento, String observaciones) {
 		return this.agregarElemento(elemento, Constants.OBSERVACIONES_TAG, observaciones);
 	}
 
 	Element agregarRol(Element elemento, String rol) {
 		return this.agregarElemento(elemento, Constants.ROL_TAG, rol);
 	}
 
 	
 }
