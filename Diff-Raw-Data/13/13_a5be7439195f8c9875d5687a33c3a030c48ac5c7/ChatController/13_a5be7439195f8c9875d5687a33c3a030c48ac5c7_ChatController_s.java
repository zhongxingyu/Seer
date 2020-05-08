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
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.reflect.TypeToken;
 import com.myl.modelo.Carta;
 import com.myl.modelo.Deck;
 import com.myl.modelo.Usuario;
 import com.myl.negocio.DeckNegocio;
 import com.myl.util.NombreObjetosSesion;
 import com.opensymphony.xwork2.ActionContext;
 import com.opensymphony.xwork2.ActionSupport;
 
 @Named
 @Results({ @Result(name = "success", type = "redirectAction", params = {"actionName", "chat" })
 
 ,@Result(name="cual", type="json", params={"includeProperties","deck1.*"})
 
 }
 )
 public class ChatController extends ActionSupport {
 
 	private static final long serialVersionUID = 8585016072024421730L;
 	private Integer idSel;
 	
 	private Deck deck;
 	private DeckNegocio deckNegocio;
 
 	private List<Carta> deck1;
 	
 	private String user1;
 	private String user2;
 	
 	private Integer deckId;
 	
 	@SkipValidation
 	public HttpHeaders index() {
 		
 		
 		return new DefaultHttpHeaders("index").disableCaching();
 	}
 
 	
 	public String prueba(){
 		String aux2="cual";
 		Usuario usuario=(Usuario) ActionContext.getContext().getSession().get(NombreObjetosSesion.USUARIO);
 		
 		deck=deckNegocio.findById(usuario.getDeckPred());
 		deck1=new ArrayList<Carta>();
 		
 		int count=0;				
 		
 		for(Carta carta:deck.getCartas()){									
 						
 			for(int i=0;i<deck.getDeckCartas().get(count).getCartaQt();i++){
 				Carta aux=new Carta();				
 				aux.setIdTemp(i+carta.getNumero()+carta.getEdicion().getSiglas());
 				aux.setNombre(carta.getNombre());
 				aux.setNumero(carta.getNumero());
				aux.setEfecto(carta.getEfecto());				
 				aux.setTipo(carta.getTipo());
 				aux.setFrecuencia(carta.getFrecuencia());
 				aux.setCoste(carta.getCoste());
 				aux.setFuerza(carta.getFuerza());
 				aux.setSiglas(carta.getEdicion().getSiglas());
 				deck1.add(aux);
 			}
 			count++;
 		}
 		
 		return aux2;
 	}
 	
 	public Integer getIdSel() {
 		return idSel;
 	}
 
 	public void setIdSel(Integer idSel) {
 		this.idSel = idSel;
 	}
 
 	public Deck getDeck() {
 		return deck;
 	}
 
 	public void setDeck(Deck deck) {
 		this.deck = deck;
 	}
 
 	public DeckNegocio getDeckNegocio() {
 		return deckNegocio;
 	}
 
 	public void setDeckNegocio(DeckNegocio deckNegocio) {
 		this.deckNegocio = deckNegocio;
 	}
 
 	public List<Carta> getDeck1() {
 		return deck1;
 	}
 
 	public void setDeck1(List<Carta> deck1) {
 		this.deck1 = deck1;
 	}
 
 
 	public String getUser1() {
 		return user1;
 	}
 
 
 	public void setUser1(String user1) {
 		this.user1 = user1;
 	}
 
 
 	public String getUser2() {
 		return user2;
 	}
 
 
 	public void setUser2(String user2) {
 		this.user2 = user2;
 	}
 
 
 	public Integer getDeckId() {
 		return deckId;
 	}
 
 
 	public void setDeckId(Integer deckId) {
 		this.deckId = deckId;
 	}
 	
 
 }
