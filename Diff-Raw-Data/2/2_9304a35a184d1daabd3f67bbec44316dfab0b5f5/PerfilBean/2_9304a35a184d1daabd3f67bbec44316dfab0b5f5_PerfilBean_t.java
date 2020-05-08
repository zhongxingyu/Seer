 package beans;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.FacesContext;
 import javax.servlet.ServletContext;
 
 import org.primefaces.event.FileUploadEvent;
 
 import model.Carona;
 import model.CaronaSolicitada;
 import model.RequestMeetingPoint;
 import model.SolicitacaoPontoEncontro;
 import model.Usuario;
 import controller.Controller;
 import controller.SessionController;
 
 @ViewScoped
 @ManagedBean(name = "perfilBean")
 public class PerfilBean {
 
 	public static String ID;
 	private String id;
 	private String origem;
 	private String destino;
 	private String data;
 	private String hora;
 	private String vagas;
 	private String pontoDeEncontro;
 	private Carona carona;
 	private String LoginDoUsuarioProcurado;
 	private String opcao;
 	int size;
 
 	private Usuario usuario;
 	private CaronaSolicitada selectedCaronaSolicitada;
 	private Carona selectedCarona;
 	private RequestMeetingPoint requestPoint;
 	
 	private List<Carona> caronasDisponiveis;
 	private List<CaronaSolicitada> caronasSolicitadas;
 	private List<CaronaSolicitada> caronasSolicitadasPorMim;
 	private List<RequestMeetingPoint> requestMeetingPoint;
 	private List<RequestMeetingPoint> requestsPontosEncontro;
 	
 
 	public PerfilBean() {
 		if (ID != null) {
 			this.usuario = SessionController.getInstance()
 					.searchSessionById(ID);
 			this.id = ID;
 			this.caronasDisponiveis = new ArrayList<Carona>();
 			this.reset();
 			localizaCaronasDisponiveis();
 			update();
 		} else
 			throw new RuntimeException("Erro no id do perfil");
 	}
 
 	public String logoffButton() {
 		SessionController.getInstance().desconectarSessao(Integer.parseInt(id)); 
 		return "index.xhtml";
 	}
 
 	public void setSize(int size) {
 		this.size = size;
 	}
 
 
 
 	public Usuario getUsuario() {
 		return usuario;
 	}
 
 	public String getLoginDoUsuarioProcurado() {
 		return LoginDoUsuarioProcurado;
 	}
 	
 	public String localizaPerfil() {
 		Usuario user = Controller.getInstance().searchUsuariobyLogin(LoginDoUsuarioProcurado);
 		if(user != null){
 			ShowerProfileBean.ID=String.valueOf(Controller.getInstance().visualizaPerfil(user));
 			return "perfilViewer?faces-redirect=true";
 		}
 		
 		throw new RuntimeException("perfil invalido");
 	}
 	
 	public String meuPerfil(){
 		Usuario user = Controller.getInstance().searchUsuariobyLogin(usuario.getLogin());
 		if(user != null){
 			ShowerProfileBean.ID=String.valueOf(Controller.getInstance().visualizaPerfil(user));
 			return "perfilViewer?faces-redirect=true";
 		}
 		throw new RuntimeException("perfil invalido");
 	}
 	
 	
 	public void setLoginDoUsuarioProcurado(String login) {
 		this.LoginDoUsuarioProcurado = login;
 	}
 
 	public String redirectPerfil() {
 		return "perfil.xhtml";
 	}
 
 	public String cleanAndExit() {
 		this.caronasDisponiveis = new ArrayList<Carona>();
 		return "perfil.xhtml";
 
 	}
 
 
 	public void localizaCaronasDisponiveis() {
 		this.setCaronasDisponiveis(new ArrayList<Carona>());
 		List<Integer> idsCaronas = Controller.getInstance().buscaCarona(
 				this.origem, this.destino);
 		for (Integer id : idsCaronas) {
 			this.caronasDisponiveis.add(Controller.getInstance()
 					.buscaCarona(id));
 		}
 
 	}
 
 
 	public void cadastrarCarona() {
 		try {
 			usuario.adicionaCarona(this.origem, this.destino, this.data,
 					this.hora, this.vagas);
 		} catch (Exception e) {
 			msg(e.getMessage());
 			return;
 		}
 		msg("Carona cadastrada com sucesso");
 		this.reset();
 		localizaCaronasDisponiveis();
 		update();
 	}
 
 	public void solicitarCarona() {
 
 		this.usuario.solicitaVaga(this.selectedCarona);
 		update();
 	}
 
 	private void update() {
 		Map<Integer, CaronaSolicitada> requisicoes = this.usuario.getRequests();
 		caronasSolicitadas = new ArrayList<CaronaSolicitada>();
 		for (Integer id : requisicoes.keySet()) {
 			caronasSolicitadas.add(requisicoes.get(id));
 		}
 
 		this.setRequestMeetingPoint(this.usuario.getRequisicoesPontosPendentes());
 		this.requestsPontosEncontro = this.usuario.getRequisicoesPontosPendentes();
		this.caronasSolicitadasPorMim = this.usuario.getCaronasSolicitadas();
 		System.out.println(caronasSolicitadas);
 	}
 
 	public void reset() {
 		this.setOrigem("");
 		this.setDestino("");
 		this.setData("");
 		this.setHora("");
 		this.setVagas("");
 	}
 
 	public void upload(FileUploadEvent event) {  
 		FacesMessage msg = new FacesMessage(event.getFile().getFileName() + " foi enviado com sucesso.");  
 		FacesContext.getCurrentInstance().addMessage(null, msg);  
 		// Do what you want with the file          
 		System.out.println(1);
 		try {  
 			byte[] foto = event.getFile().getContents();  
 			String nomeArquivo = event.getFile().getFileName();    
 			System.out.println(2);
 			FacesContext facesContext = FacesContext.getCurrentInstance();    
 			ServletContext scontext = (ServletContext) facesContext.getExternalContext().getContext();    
 			String arquivo = scontext.getRealPath("/uploads/imagensTopo/" + nomeArquivo);  
 			System.out.println(3);  
 			//            String arquivo = scontext.getContextPath()+"/uploadis/" + nomeArquivo;  
 			File f=new File(arquivo);  
 			if(!f.getParentFile().exists()) f.getParentFile().mkdirs();  
 			if(!f.exists()) f.createNewFile();  
 			System.out.println("cheguei aqui");
 			System.out.println(f.getAbsolutePath());  
 
 			FileOutputStream fos=new FileOutputStream(arquivo);  
 			fos.write(foto);  
 			System.out.println(4);
 			fos.flush();  
 			System.out.println(5);
 			fos.close();  
 		} catch (IOException e) {  
 			System.out.println("exececao");
 			e.printStackTrace();  
 		}  
 
 	}  
 
 	public Collection<Carona> getCaronas() {
 		return usuario.getCaronas().values();
 	}
 
 	public String getOrigem() {
 		return origem;
 	}
 
 	public void setOrigem(String origem) {
 		this.origem = origem;
 	}
 
 	public String getDestino() {
 		return destino;
 	}
 
 	public void setDestino(String destino) {
 		this.destino = destino;
 	}
 
 	public List<Carona> getCaronasDisponiveis() {
 		return this.caronasDisponiveis;
 	}
 
 	public void setCaronasDisponiveis(List<Carona> caronasDisponiveis) {
 		this.caronasDisponiveis = caronasDisponiveis;
 	}
 
 	public String getData() {
 		return data;
 	}
 
 	public void setData(String data) {
 		this.data = data;
 	}
 
 	public String getHora() {
 		return hora;
 	}
 
 	public void setHora(String hora) {
 		this.hora = hora;
 	}
 
 	public String getVagas() {
 		return vagas;
 	}
 
 	public void setVagas(String vagas) {
 		this.vagas = vagas;
 	}
 
 	public void msg(String text) {
 		FacesContext context = FacesContext.getCurrentInstance();
 
 		context.addMessage(null, new FacesMessage(text));
 	}
 
 	public String getPontoDeEncontro() {
 		return pontoDeEncontro;
 	}
 
 	public void setPontoDeEncontro(String pontoDeEncontro) {
 		this.pontoDeEncontro = pontoDeEncontro;
 	}
 
 	public Carona getCarona() {
 		return carona;
 	}
 
 	public void setCarona(Carona carona) {
 		this.carona = carona;
 	}
 
 	public List<CaronaSolicitada> getCaronasSolicitadas() {
 		return caronasSolicitadas;
 	}
 
 	public void setCaronasSolicitadas(List<CaronaSolicitada> caronasSolicitadas) {
 		this.caronasSolicitadas = caronasSolicitadas;
 	}
 
 	
 	/**
 	 * @return the selectedCarona
 	 */
 	public Carona getSelectedCarona() {
 		return selectedCarona;
 	}
 
 
 	/**
 	 * @param selectedCarona the selectedCarona to set
 	 */
 	public void setSelectedCarona(Carona selectedCarona) {
 		
 		this.selectedCarona = selectedCarona;
 	}
 
 
 	/**
 	 * @return the requestMeetingPoint
 	 */
 	public List<RequestMeetingPoint> getRequestMeetingPoint() {
 		return requestMeetingPoint;
 	}
 
 
 	/**
 	 * @param requestMeetingPoint the requestMeetingPoint to set
 	 */
 	public void setRequestMeetingPoint(List<RequestMeetingPoint> requestMeetingPoint) {
 		this.requestMeetingPoint = requestMeetingPoint;
 	}
 
 	/**
 	 * @return the selectedCaronaSolictada
 	 */
 	public CaronaSolicitada getSelectedCaronaSolicitada() {
 		return selectedCaronaSolicitada;
 	}
 
 	/**
 	 * @param selectedCaronaSolictada the selectedCaronaSolictada to set
 	 */
 	public void setSelectedCaronaSolicitada(CaronaSolicitada selectedCaronaSolictada) {
 		System.out.println("ta mudando");
 		this.selectedCaronaSolicitada = selectedCaronaSolictada;
 		System.out.println(this.selectedCaronaSolicitada.getOrigem());
 	}
 	
 	public void escolheResultado(){
 		if(opcao.equals("1")) aceitaCarona();
 		else if(opcao.equals("2")) rejeitaCarona();
 	}
 	
 	/**
 	 * aceita uma solicitação de carona
 	 */
 	public void aceitaCarona(){
 		Usuario motorista =  this.selectedCaronaSolicitada.getMotorista();
 		int idCarona = this.selectedCaronaSolicitada.getId();
 		
 		motorista.aceitaRequest(idCarona);
 		System.out.println("aceitou");
 	}
 
 	/***
 	 * rejeita a solicitação de carona
 	 */
 	public void rejeitaCarona(){
 		Usuario motorista =  this.selectedCaronaSolicitada.getMotorista();
 		int idCarona = this.selectedCaronaSolicitada.getId();
 		
 		motorista.rejeitarRequest(idCarona);
 		
 	}
 	
 	
 	/**
 	 * @return the opcao
 	 */
 	public String getOpcao() {
 		return opcao;
 	}
 
 	/**
 	 * @param opcao the opcao to set
 	 */
 	public void setOpcao(String opcao) {
 		this.opcao = opcao;
 	}
 
 
 	/**
 	 * @return the requestsPontosEncontro
 	 */
 	public List<RequestMeetingPoint> getRequestsPontosEncontro() {
 		return requestsPontosEncontro;
 	}
 
 	/**
 	 * @param requestsPontosEncontro the requestsPontosEncontro to set
 	 */
 	public void setRequestsPontosEncontro(List<RequestMeetingPoint> requestsPontosEncontro) {
 		this.requestsPontosEncontro = requestsPontosEncontro;
 	}
 	
 	/**
 	 * Sugere ponto de encontro
 	 */
 	public void sugerirPonto(){
 		Controller cont = Controller.getInstance();
 		int idSessao = cont.getSessoes().searchSessionByLogin(this.usuario.getLogin());
 
 		cont.sugerirPontoEncontro(String.valueOf(idSessao), 
 				this.selectedCarona.getId(), this.pontoDeEncontro);
 		update();
 		msg("Ponto sugeiro com sucesso");
 	}
 
 	/**
 	 * @return the requestPoint
 	 */
 	public RequestMeetingPoint getRequestPoint() {
 		return requestPoint;
 	}
 
 	/**
 	 * @param requestPoint the requestPoint to set
 	 */
 	public void setRequestPoint(RequestMeetingPoint requestPoint) {
 		this.requestPoint = requestPoint;
 	}
 	
 	
 	public void escolheResultadoRequest(){
 		int idCarona = this.requestPoint.getIdCarona(); //id da carona mesmo
 		this.selectedCarona = Controller.getInstance().buscaCarona(idCarona);
 		//this.selectedCaronaSolicitada = Controller.getInstance().buscaCaronaSolicitada(idCaronaSolicitada)
 		
 		if(opcao.equals("1")) desistirCarona();
 		else if(opcao.equals("2")) 	solicitarCarona();
 		
 	}
 	
 	/**
 	 * idCaronaSolicitada
 	 */
 	public void desistirCarona() {
 		//CaronaSolicitada c
 		//aqui eu preciso da CaronaSolicatda mas em RequestPoint eu so tenho o id da Carona normal
 		//Controller.getInstance().desitirCarona(idSessao, idCarona, idSugestao);
 	}
 
 	/**
 	 * @return the caronasSolicitadasPorMim
 	 */
 	public List<CaronaSolicitada> getCaronasSolicitadasPorMim() {
 		return caronasSolicitadasPorMim;
 	}
 
 	/**
 	 * @param caronasSolicitadasPorMim the caronasSolicitadasPorMim to set
 	 */
 	public void setCaronasSolicitadasPorMim(List<CaronaSolicitada> caronasSolicitadasPorMim) {
 		this.caronasSolicitadasPorMim = caronasSolicitadasPorMim;
 	}
 }
