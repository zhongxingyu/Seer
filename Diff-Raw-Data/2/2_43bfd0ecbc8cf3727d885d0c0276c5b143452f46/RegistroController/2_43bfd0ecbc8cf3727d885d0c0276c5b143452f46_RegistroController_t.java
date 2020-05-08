 package com.myl.controller;
 
 import java.util.Date;
 import java.util.List;
 
 import javax.inject.Named;
 
 import org.apache.struts2.convention.annotation.Namespace;
 import org.apache.struts2.convention.annotation.Result;
 import org.apache.struts2.convention.annotation.Results;
 import org.apache.struts2.interceptor.validation.SkipValidation;
 import org.apache.struts2.rest.DefaultHttpHeaders;
 import org.apache.struts2.rest.HttpHeaders;
 
 import com.myl.modelo.Deck;
 import com.myl.modelo.Pais;
 import com.myl.modelo.Usuario;
 import com.myl.negocio.DeckNegocio;
 import com.myl.negocio.PaisNegocio;
 import com.myl.negocio.UsuarioNegocio;
 import com.opensymphony.xwork2.ActionSupport;
 import com.opensymphony.xwork2.ModelDriven;
 import com.opensymphony.xwork2.validator.annotations.EmailValidator;
 import com.opensymphony.xwork2.validator.annotations.IntRangeFieldValidator;
 import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
 import com.opensymphony.xwork2.validator.annotations.RegexFieldValidator;
 import com.opensymphony.xwork2.validator.annotations.Validations;
 import com.opensymphony.xwork2.validator.annotations.ValidatorType;
 
 @Named
 @Results({ @Result(name = "registered", type = "redirectAction", params = {
 		"actionName", "login" }) })
 public class RegistroController extends ActionSupport implements
 		ModelDriven<Usuario> {
 
 	private static final long serialVersionUID = 1L;
 
 	private Integer idSel;
 
 	private Usuario model = null;
 	private Usuario usuario;
 	private UsuarioNegocio usuarioNegocio;
 	private DeckNegocio deckNegocio;
 	private List<Deck> lista;
 	private Deck deck;
 
 	private String confirmPass;
 	private List<Pais> listPaises;
 	
 	private PaisNegocio paisNegocio;
 	
 	@SkipValidation
 	public String editNew() {
 		listPaises=paisNegocio.findAll(); 
 		return "editNew";
 	}
 
 	public void validateCreate() {		
 
 		if (!model.getPassword().equals(confirmPass)) {
 			addActionError("Las contraseñas no son iguales");
 		}
 
 		Usuario aux = new Usuario();
 		aux.setLogin(model.getLogin());
 		if (!usuarioNegocio.findByExample(aux).isEmpty()) {
 			addActionError("Nombre de usuario no disponible");
 		}
 		
 		aux=new Usuario();
 		aux.setEmail(model.getEmail());
 		if (!usuarioNegocio.findByExample(aux).isEmpty()) {
 			addActionError("El correo electrónico ingresado ya está registrado");
 		}
 		
 		if (hasFieldErrors() || hasActionErrors()) {
 			listPaises=paisNegocio.findAll();
 		}
 	}
 
 	@Validations(requiredStrings = {
 			@RequiredStringValidator(fieldName = "model.login", type = ValidatorType.FIELD, key = "Introduce un nombre de usuario"),			
 			@RequiredStringValidator(fieldName = "model.password", type = ValidatorType.FIELD, key = "Introduce la contraseña"),
 			@RequiredStringValidator(fieldName = "model.email", type = ValidatorType.FIELD, key = "Introduce tu correo electrónico"),			
 			@RequiredStringValidator(fieldName = "confirmPass", type = ValidatorType.FIELD, key = "Confirma la contraseña")},			
 			regexFields = {
 			@RegexFieldValidator(fieldName = "model.login", type = ValidatorType.FIELD, key = "Nombre de usuario no válido", expression = "[A-Z[a-z][0-9]]+")},
 			intRangeFields={
 			@IntRangeFieldValidator(fieldName="model.idPais", type = ValidatorType.FIELD, message="Selecciona tu pais", min = "1")},
 			emails={
 			@EmailValidator(fieldName="model.email", type=ValidatorType.FIELD, message="Correo electrónico no válido")
 			})
 	public HttpHeaders create() {
 		model.setDeckPred(0);
		model.setWons(0);
		model.setLost(0);
 		model.setFhRegistro(new Date());
 		model = usuarioNegocio.save(model);
 
 		return new DefaultHttpHeaders("registered").setLocationId(model
 				.getIdUsuario());
 	}
 
 	public Usuario getUsuario() {
 		return usuario;
 	}
 
 	public void setUsuario(Usuario usuario) {
 		this.usuario = usuario;
 	}
 	
 	public Integer getIdSel() {
 		return idSel;
 	}
 
 	public void setIdSel(Integer idSel) {
 		this.idSel = idSel;
 		if (idSel != null) {
 			model = usuarioNegocio.findById(idSel);
 		}
 	}
 
 	@Override
 	public Usuario getModel() {
 		if (model == null) {
 			model = new Usuario();
 		}
 		return model;
 	}
 
 	public void setModel(Usuario model) {
 		this.model = model;
 	}
 
 	public List<Deck> getLista() {
 		return lista;
 	}
 
 	public void setLista(List<Deck> lista) {
 		this.lista = lista;
 	}
 	
 	public DeckNegocio getDeckNegocio() {
 		return deckNegocio;
 	}
 
 	public void setDeckNegocio(DeckNegocio deckNegocio) {
 		this.deckNegocio = deckNegocio;
 	}
 
 	public UsuarioNegocio getUsuarioNegocio() {
 		return usuarioNegocio;
 	}
 
 	public void setUsuarioNegocio(UsuarioNegocio usuarioNegocio) {
 		this.usuarioNegocio = usuarioNegocio;
 	}
 
 	public String getConfirmPass() {
 		return confirmPass;
 	}
 
 	public void setConfirmPass(String confirmPass) {
 		this.confirmPass = confirmPass;
 	}
 	
 	public Deck getDeck() {
 		return deck;
 	}
 
 	public void setDeck(Deck deck) {
 		this.deck = deck;
 	}
 
 	public List<Pais> getListPaises() {
 		return listPaises;
 	}
 
 	public void setListPaises(List<Pais> listPaises) {
 		this.listPaises = listPaises;
 	}
 
 	public PaisNegocio getPaisNegocio() {
 		return paisNegocio;
 	}
 
 	public void setPaisNegocio(PaisNegocio paisNegocio) {
 		this.paisNegocio = paisNegocio;
 	}
 
 }
