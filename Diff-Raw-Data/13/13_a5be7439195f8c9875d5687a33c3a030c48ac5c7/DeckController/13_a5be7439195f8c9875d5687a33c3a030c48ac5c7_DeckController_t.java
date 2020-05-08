 package com.myl.controller;
 
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.inject.Named;
 
 import org.apache.struts2.convention.annotation.Result;
 import org.apache.struts2.convention.annotation.Results;
 import org.apache.struts2.interceptor.validation.SkipValidation;
 import org.apache.struts2.rest.DefaultHttpHeaders;
 import org.apache.struts2.rest.HttpHeaders;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 import com.myl.modelo.Carta;
 import com.myl.modelo.Deck;
 import com.myl.modelo.DeckCarta;
 import com.myl.modelo.Edicion;
 import com.myl.modelo.Usuario;
 import com.myl.negocio.CartaNegocio;
 import com.myl.negocio.DeckCartaNegocio;
 import com.myl.negocio.DeckNegocio;
 import com.myl.negocio.EdicionNegocio;
 import com.myl.util.NombreObjetosSesion;
 import com.opensymphony.xwork2.ActionContext;
 import com.opensymphony.xwork2.ActionSupport;
 import com.opensymphony.xwork2.ModelDriven;
 
 @Named
 @Results({ @Result(name = "success", type = "redirectAction", params = {"actionName", "usuario" })
 ,@Result(name="res", type="json", params={"includeProperties","resultado.*"})
 ,@Result(name="decks", type="json", params={"includeProperties","deckCompleto.*"})
 })
 public class DeckController extends ActionSupport implements ModelDriven<Deck>{
 	
 	
 	private Integer idSel;
 	private Deck model;
 	private Usuario usuario;
 	
 	private List<Carta> resultado;
 	private List<Edicion> ediciones;
 	
 	private EdicionNegocio edicionNegocio;
 	private CartaNegocio cartaNegocio;
 	private DeckNegocio deckNegocio;
 	private DeckCartaNegocio deckCartaNegocio;
 
 	private List<String> razas;
 	private List<DeckCarta> deckCartas;	
 	private String lista;
 	private String criterioJson;
 	
 	private Gson jsonProcessor;
 	private List<Deck> decks;
 	
 	private Deck deckAux;
 	private List<DeckCarta> deck;
 	private List<Carta> deckCompleto;
 	
 	private List<Carta> deckCom;
 	
 	@SkipValidation
 	public HttpHeaders index() {
 		
 		decks=deckNegocio.findAll();
 		
 		
 		return new DefaultHttpHeaders("index").disableCaching();
 	}
 	
 	@SkipValidation
 	public String editNew() {		
 		ediciones=edicionNegocio.findAll();		
 		razas=cartaNegocio.findByCriteria();
 		
 		return "editNew";
 	}
 	
 	public void validateCreate() {
 	}
 	
 	public HttpHeaders create() {
 		jsonProcessor = new Gson();		
 		
 		Type listType = new TypeToken<List<DeckCarta>>() {}.getType();
 		deckCartas=jsonProcessor.fromJson(lista, listType);
 		usuario=(Usuario) ActionContext.getContext().getSession().get(NombreObjetosSesion.USUARIO);
 		
 		model.setUsuarioId(usuario.getIdUsuario());			
 		model=deckNegocio.save(model);
 		
 		for(DeckCarta dc:deckCartas){
 			deckCartaNegocio.insertCard(model.getDeckId(),dc.getCartaId() , dc.getCartaQt());
 		}
 		
 		return new DefaultHttpHeaders("success").setLocationId(model.getDeckId());
 	}
 	
 	@SkipValidation
 	public String edit() {
 		ediciones=edicionNegocio.findAll();		
 		razas=cartaNegocio.findByCriteria();		
 		
 		
 		return "edit";
 	}
 	
 	public void validateUpdate(){
 		
 	}
 		
 	@SkipValidation
 	public String update() {
 		jsonProcessor = new Gson();				
 		Type listType = new TypeToken<List<DeckCarta>>() {}.getType();
 		deckCartas=jsonProcessor.fromJson(lista, listType);
 								
 		model=deckNegocio.save(model);
 		
 		deckCartaNegocio.deleteCardsFromDeck(model.getDeckId());
 		
 		for(DeckCarta dc:deckCartas){
 			deckCartaNegocio.insertCard(model.getDeckId(),dc.getCartaId() , dc.getCartaQt());
 		}
 		
 		return "success";
 	}
 
 	@SkipValidation
 	public String deleteConfirm() {
 		return "deleteConfirm";
 	}
 
 	public void validateDestroy() {
 
 	}
 
 	
 	@SkipValidation
 	public String destroy() {
 		
 		return "success";
 	}
 	
 	
 	public String search(){
 		jsonProcessor = new Gson();				
 		Carta cartaAux=jsonProcessor.fromJson(criterioJson, Carta.class);		
 		resultado= new ArrayList<Carta>();
 		
 		
 		for(Carta carta:cartaNegocio.findByExample(cartaAux)){									
 						
 				Carta aux=new Carta();
 				aux.setId(carta.getId());
 				aux.setIdTemp(carta.getNumero()+carta.getEdicion().getSiglas());
 				aux.setNombre(carta.getNombre());
 				aux.setNumero(carta.getNumero());
				aux.setEfecto(carta.getEfecto());
				aux.setRaza(carta.getRaza());
 				aux.setTipo(carta.getTipo());
 				aux.setFrecuencia(carta.getFrecuencia());
 				aux.setCoste(carta.getCoste());
 				aux.setFuerza(carta.getFuerza());
 				aux.setSiglas(carta.getEdicion().getSiglas());				
 				
 				resultado.add(aux);
 		}
 		
 			
 		return "res";
 	}
 	
 	public String buscarDecks(){
 		deckAux=deckNegocio.findById(idSel);
 		deckCompleto=new ArrayList<Carta>();
 		deck=new ArrayList<DeckCarta>();
 		
 		int c=0;
 		for(Carta carta:deckAux.getCartas()){															
 				Carta aux=new Carta();				
 				aux.setId(carta.getId());
 				aux.setCantidad(deckAux.getDeckCartas().get(c).getCartaQt());
 				aux.setNombre(carta.getNombre());
 				aux.setNumero(carta.getNumero());
 				aux.setEfecto(carta.getEfecto());				
 				aux.setTipo(carta.getTipo());
				aux.setRaza(carta.getRaza());
 				aux.setFrecuencia(carta.getFrecuencia());
 				aux.setCoste(carta.getCoste());
 				aux.setFuerza(carta.getFuerza());
 				aux.setSiglas(carta.getEdicion().getSiglas());
 				deckCompleto.add(aux);
 				c++;
 		}
 				
 		return "decks";
 	}
 	
 	public Integer getIdSel() {
 		return idSel;
 	}
 
 	public void setIdSel(Integer idSel) {
 		this.idSel = idSel;
 		if (idSel != null) {
 			model = deckNegocio.findById(idSel);
 		}
 	}
 
 
 	@Override
 	public Deck getModel() {
 		if (model == null) {
 			model = new Deck();
 		}
 		return model;
 	}
 
 	public void setModel(Deck model) {
 		this.model = model;
 	}
 	
 
 	public Deck getDeckAux() {
 		return deckAux;
 	}
 
 	public void setDeckAux(Deck deckAux) {
 		this.deckAux = deckAux;
 	}
 
 	public List<Carta> getResultado() {
 		return resultado;
 	}
 
 	public void setResultado(List<Carta> resultado) {
 		this.resultado = resultado;
 	}
 
 	public List<Edicion> getEdiciones() {
 		return ediciones;
 	}
 
 	public void setEdiciones(List<Edicion> ediciones) {
 		this.ediciones = ediciones;
 	}
 
 	public EdicionNegocio getEdicionNegocio() {
 		return edicionNegocio;
 	}
 
 	public void setEdicionNegocio(EdicionNegocio edicionNegocio) {
 		this.edicionNegocio = edicionNegocio;
 	}
 
 	public CartaNegocio getCartaNegocio() {
 		return cartaNegocio;
 	}
 
 	public void setCartaNegocio(CartaNegocio cartaNegocio) {
 		this.cartaNegocio = cartaNegocio;
 	}
 
 	public DeckNegocio getDeckNegocio() {
 		return deckNegocio;
 	}
 
 	public void setDeckNegocio(DeckNegocio deckNegocio) {
 		this.deckNegocio = deckNegocio;
 	}
 
 	public DeckCartaNegocio getDeckCartaNegocio() {
 		return deckCartaNegocio;
 	}
 
 	public void setDeckCartaNegocio(DeckCartaNegocio deckCartaNegocio) {
 		this.deckCartaNegocio = deckCartaNegocio;
 	}
 
 	public List<String> getRazas() {
 		return razas;
 	}
 
 	public void setRazas(List<String> razas) {
 		this.razas = razas;
 	}
 
 	public List<DeckCarta> getDeckCartas() {
 		return deckCartas;
 	}
 
 	public void setDeckCartas(List<DeckCarta> deckCartas) {
 		this.deckCartas = deckCartas;
 	}
 
 	public List<DeckCarta> getDeck() {
 		return deck;
 	}
 
 	public void setDeck(List<DeckCarta> deck) {
 		this.deck = deck;
 	}
 
 	public String getLista() {
 		return lista;
 	}
 
 	public void setLista(String lista) {
 		this.lista = lista;
 	}
 
 	public String getCriterioJson() {
 		return criterioJson;
 	}
 
 	public void setCriterioJson(String criterioJson) {
 		this.criterioJson = criterioJson;
 	}
 
 	public Gson getJsonProcessor() {
 		return jsonProcessor;
 	}
 
 	public void setJsonProcessor(Gson jsonProcessor) {
 		this.jsonProcessor = jsonProcessor;
 	}
 
 	public List<Deck> getDecks() {
 		return decks;
 	}
 
 	public void setDecks(List<Deck> decks) {
 		this.decks = decks;
 	}	
 
 	public List<Carta> getDeckCompleto() {
 		return deckCompleto;
 	}
 
 	public void setDeckCompleto(List<Carta> deckCompleto) {
 		this.deckCompleto = deckCompleto;
 	}
 
 	public List<Carta> getDeckCom() {
 		return deckCom;
 	}
 
 	public void setDeckCom(List<Carta> deckCom) {
 		this.deckCom = deckCom;
 	}
 
 	public Usuario getUsuario() {
 		return usuario;
 	}
 
 	public void setUsuario(Usuario usuario) {
 		this.usuario = usuario;
 	}
 
 	
 
 }
 
