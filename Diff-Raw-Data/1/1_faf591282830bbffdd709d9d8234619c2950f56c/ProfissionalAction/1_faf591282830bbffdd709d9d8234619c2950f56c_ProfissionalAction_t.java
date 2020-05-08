 package br.com.webcare.controller;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.collections.CollectionUtils;
 
 import br.com.webcare.model.Bairro;
 import br.com.webcare.model.Cidade;
 import br.com.webcare.model.Convenio;
 import br.com.webcare.model.Especializacao;
 import br.com.webcare.model.Estado;
 import br.com.webcare.model.Profissional;
 import br.com.webcare.model.RegiaoPreferencial;
 import br.com.webcare.service.excepition.ConvenioException;
 import br.com.webcare.service.impl.MenuService;
 import br.com.webcare.service.impl.ProfissionalService;
 import br.com.webcare.service.interfaces.IMenuService;
 import br.com.webcare.service.interfaces.IProfissionalService;
 
 public class ProfissionalAction extends GenericAction{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	public final static String COMBOS = "combos";
 	public final static String LISTA_REGIAO_PREFERENCIAL_SESSAO = "LISTA_REGIAO_PREFERENCIAL_SESSAO";
 	public final static String LISTAR_REGIAO_PREFERENCIAL = "listarRegiaoPreferencial";
 	public final static String COMBOS_REGIAO_PREFERENCIAL = "combosRegiaoPreferencial";
 
 	private Integer idProfissional;	
 	private String tipoCombo;	
 	private Integer idBusca;
 	private String metodoCarregarCombos;
 	private Integer idRegiaoPreferencial;
 	private RegiaoPreferencial regiaoPreferencial;
 	
 	private List<Profissional> profissionais = new ArrayList<Profissional>();
 	private Profissional profissional = new Profissional();
 	private List<Convenio> listaConvenio = new ArrayList<Convenio>();
 	private List<Convenio> listaConvenioSelecionados = new ArrayList<Convenio>();
 	private List<Especializacao> listaEspecializacao = new ArrayList<Especializacao>();
 	private List<Especializacao> listaEspecializacaoSelecionados = new ArrayList<Especializacao>();
 	private List<Convenio> opcoesConvenio = new ArrayList<Convenio>();
 	
 	private List<Estado> listaEstado = new ArrayList<Estado>();
 	private List<Cidade> listaCidade = new ArrayList<Cidade>();
 	private List<Bairro> listaBairro = new ArrayList<Bairro>();
 
 	private IProfissionalService profissionalService = new ProfissionalService();
 	private IMenuService menuService = new MenuService();
 
 	private int index;
 
 	
 	
 	public void prepare(){
 	}
 
 	public String index() throws Exception{
 		String result = "index";
 		return result;
 	}
 
 
 	public String list() throws Exception{
 		profissionais = profissionalService.listAll(Profissional.class, "nome");
 		System.out.println("Plano_list()...");
 		return "list";
 	}
 	
 	@SuppressWarnings("unchecked")
 	private void prepararFormulario() throws Exception{
 		if(profissional != null && profissional.getDataNascimento() != null && profissional.getDataNascimentoStr() == null){
 			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
 			profissional.setDataNascimentoStr(format.format(profissional.getDataNascimento()));    
 		}		
 		listaConvenio = profissionalService.listAll(Convenio.class, "nome");
 		listaEspecializacao = profissionalService.listAll(Especializacao.class, "nome");		
 		listaConvenio = (List<Convenio>) CollectionUtils.subtract(listaConvenio, listaConvenioSelecionados);
 		listaEspecializacao = (List<Especializacao>) CollectionUtils.subtract(listaEspecializacao, listaEspecializacaoSelecionados);
 		listaEstado = profissionalService.listAll(Estado.class, "nome");
 		
 		if(profissional.getEstado() != null){
 			listaCidade = menuService.buscarCidadesPorEstado(profissional.getEstado().getId());
 		}
 		if(profissional.getCidade() != null){
 			listaBairro = menuService.buscarBairroPorCidade(profissional.getCidade().getId());		
 		}
 		
 		
 	}
 
 	public String editar() throws Exception {
 		try {
 			if (idProfissional != null){
 				profissional = profissionalService.buscarProfissional(idProfissional);
 				listaConvenioSelecionados = profissional.getConvenios();
 				listaEspecializacaoSelecionados = profissional.getEspecializacoes();
 				prepararFormulario();
 				
 				if(profissional.getListaRegiaoPreferencial() == null){
 					profissional.setListaRegiaoPreferencial(new ArrayList<RegiaoPreferencial>());
 				}
 				
 				putSession(LISTA_REGIAO_PREFERENCIAL_SESSAO, profissional.getListaRegiaoPreferencial());
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return "form";
 	}
 
 	private void validaForm() throws Exception {
 
 		/*	if (user.getName() == null || user.getName().isEmpty()) {
 			this.addActionError(getMessage("user.name.required"));
 		}
 		if (user.getName().length() < 3) {
 			this.addActionError(getMessage("user.name.length"));
 		}
 
 		if (user.getLogin() == null || user.getLogin().isEmpty()) {
 			this.addActionError(getMessage("user.login.required"));
 		}
 		User us = userDAO.buscarPorLogin2(user.getLogin(), user.getId());
 		if (us != null) {
 			this.addActionError(getMessage("user.login.exists"));
 		}
 		if (user.getPassword() == null || user.getPassword().isEmpty()) {
 			this.addActionError(getMessage("user.password.required"));
 		}
 
 		User us2 = userDAO.buscarPorEmail2(user.getEmail(), user.getId());
 		if (us2 != null) {
 			this.addActionError(getMessage("user.email.exists"));
 		}
 		if (user.getEmail() == null || user.getEmail().isEmpty()) {
 			this.addActionError(getMessage("user.email.required"));
 		}
 		if (user.getUserType() == null || user.getUserType().getId() == null) {
 			this.addActionError(getMessage("user.type.required"));
 		}*/
 	}
 
 
 	public String add() throws Exception{
 		try {
 //			String result = "index";
 			if(profissional != null){			
 				SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
 				profissional.setDataNascimento(format.parse(profissional.getDataNascimentoStr()));			
 				profissional.setConvenios(listaConvenioSelecionados);
 				profissional.setEspecializacoes(listaEspecializacaoSelecionados);
				profissional.setListaRegiaoPreferencial(getListaRegiaoPreferencialSessao());
 				if(profissionalService.save(profissional)){
 					return list();
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		prepararFormulario();
 		
 		return "form";
 	}
 
 	public String form(){
 		try {
 			putSession(LISTA_REGIAO_PREFERENCIAL_SESSAO, new ArrayList<RegiaoPreferencial>());
 			prepararFormulario();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return "form";
 	}
 
 
 
 	public String update() throws Exception{
 		String result = "index";
 		profissional.setId(idProfissional);
 		profissional.setConvenios(listaConvenioSelecionados);
 		profissional.setEspecializacoes(listaEspecializacaoSelecionados);		
 		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
 		profissional.setDataNascimento(format.parse(profissional.getDataNascimentoStr()));
 		profissional.setListaRegiaoPreferencial(getListaRegiaoPreferencialSessao());
 		
 		profissionalService.update(profissional);
 
 		result = list();	
 		return result;
 	}
 
 	public String delete() throws Exception{
 		String result = "index";
 		profissionalService.delete(Profissional.class, idProfissional);
 		result = list();	
 		return result;
 	}
 	
 	public String addConv()throws Exception{
 		String result = "index";
 		
 		return result;
 	}
 	
 	public String carregarCombos(){
 		try {
 			
 			if(MenuAction.TIPO_COMBO_CIDADE.equals(tipoCombo)){
 				listaCidade = menuService.buscarCidadesPorEstado(idBusca);
 			}else if(MenuAction.TIPO_COMBO_BAIRRO.equals(tipoCombo)){
 				listaBairro = menuService.buscarBairroPorCidade(idBusca);
 			}
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return COMBOS;
 	}
 	
 	public String carregarCombosRegiaoPreferencial(){
 		try {
 			
 			if(MenuAction.TIPO_COMBO_CIDADE.equals(tipoCombo)){
 				listaCidade = menuService.buscarCidadesPorEstado(idBusca);
 			}else if(MenuAction.TIPO_COMBO_BAIRRO.equals(tipoCombo)){
 				listaBairro = menuService.buscarBairroPorCidade(idBusca);
 			}
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return COMBOS_REGIAO_PREFERENCIAL;
 	}
 	
 	public String incluirRegiaoPreferencial(){
 		try {
 			regiaoPreferencial.setEstado((Estado) menuService.findById(Estado.class, regiaoPreferencial.getEstado().getId()));
 			regiaoPreferencial.setCidade((Cidade) menuService.findById(Cidade.class, regiaoPreferencial.getCidade().getId()));
 			regiaoPreferencial.setBairro((Bairro) menuService.findById(Bairro.class, regiaoPreferencial.getBairro().getId()));
 			
 			getListaRegiaoPreferencialSessao().add(regiaoPreferencial);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return LISTAR_REGIAO_PREFERENCIAL;
 	}
 	
 	public String removerRegiaoPreferencial(){
 		try {
 			
 			getListaRegiaoPreferencialSessao().remove(index);		
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return LISTAR_REGIAO_PREFERENCIAL;
 	}
 
 	
 	/*GET AN SET*/
 	
 	public List<RegiaoPreferencial> getListaRegiaoPreferencialSessao(){
 		return (List<RegiaoPreferencial>) getSession(LISTA_REGIAO_PREFERENCIAL_SESSAO);
 	}
 
 	public Integer getIdProfissional() {
 		return idProfissional;
 	}
 
 	public void setIdProfissional(Integer idProfissional) {
 		this.idProfissional = idProfissional;
 	}
 	
 	public Profissional getProfissional() {
 		return profissional;
 	}
 	
 	public void setProfissional(Profissional profissional) {
 		this.profissional = profissional;
 	}
 
 
 	public IProfissionalService getProfissionalService() {
 		return profissionalService;
 	}
 
 
 	public void setProfissionalService(IProfissionalService profissionalService) {
 		this.profissionalService = profissionalService;
 	}
 
 
 	public static long getSerialversionuid() {
 		return serialVersionUID;
 	}
 
 
 	public List<Profissional> getProfissionais() {
 		return profissionais;
 	}
 
 
 	public void setProfissionais(List<Profissional> profissionais) {
 		this.profissionais = profissionais;
 	}
 
 	public List<Convenio> getOpcoesConvenio() throws ConvenioException {	 
 		return opcoesConvenio;
 	}
 
 
 	public void setOpcoesConvenio(List<Convenio> opcoesConvenio) {
 		this.opcoesConvenio = opcoesConvenio;
 	}
 
 
 	public List<Convenio> getListaConvenio() {
 		return listaConvenio;
 	}
 
 
 	public void setListaConvenio(List<Convenio> listaConvenio) {
 		this.listaConvenio = listaConvenio;
 	}
 
 
 	public List<Convenio> getListaConvenioSelecionados() {
 		return listaConvenioSelecionados;
 	}
 
 
 	public void setListaConvenioSelecionados(
 			List<Convenio> listaConvenioSelecionados) {
 		this.listaConvenioSelecionados = listaConvenioSelecionados;
 	}
 
 
 	public List<Especializacao> getListaEspecializacao() {
 		return listaEspecializacao;
 	}
 
 
 	public void setListaEspecializacao(List<Especializacao> listaEspecializacao) {
 		this.listaEspecializacao = listaEspecializacao;
 	}
 
 
 	public List<Especializacao> getListaEspecializacaoSelecionados() {
 		return listaEspecializacaoSelecionados;
 	}
 
 
 	public void setListaEspecializacaoSelecionados(
 			List<Especializacao> listaEspecializacaoSelecionados) {
 		this.listaEspecializacaoSelecionados = listaEspecializacaoSelecionados;
 	}
 
 	public List<Estado> getListaEstado() {
 		return listaEstado;
 	}
 
 	public void setListaEstado(List<Estado> listaEstado) {
 		this.listaEstado = listaEstado;
 	}
 
 	public List<Cidade> getListaCidade() {
 		return listaCidade;
 	}
 
 	public void setListaCidade(List<Cidade> listaCidade) {
 		this.listaCidade = listaCidade;
 	}
 
 	public List<Bairro> getListaBairro() {
 		return listaBairro;
 	}
 
 	public void setListaBairro(List<Bairro> listaBairro) {
 		this.listaBairro = listaBairro;
 	}
 
 	public String getTipoCombo() {
 		return tipoCombo;
 	}
 
 	public void setTipoCombo(String tipoCombo) {
 		this.tipoCombo = tipoCombo;
 	}
 
 	public Integer getIdBusca() {
 		return idBusca;
 	}
 
 	public void setIdBusca(Integer idBusca) {
 		this.idBusca = idBusca;
 	}
 
 	
 	public String getMetodoCarregarCombos() {
 		return metodoCarregarCombos;
 	}
 
 	public void setMetodoCarregarCombos(String metodoCarregarCombos) {
 		this.metodoCarregarCombos = metodoCarregarCombos;
 	}
 
 	public Integer getIdRegiaoPreferencial() {
 		return idRegiaoPreferencial;
 	}
 
 	public void setIdRegiaoPreferencial(Integer idRegiaoPreferencial) {
 		this.idRegiaoPreferencial = idRegiaoPreferencial;
 	}
 
 	public RegiaoPreferencial getRegiaoPreferencial() {
 		return regiaoPreferencial;
 	}
 
 	public void setRegiaoPreferencial(RegiaoPreferencial regiaoPreferencial) {
 		this.regiaoPreferencial = regiaoPreferencial;
 	}
 
 	public IMenuService getMenuService() {
 		return menuService;
 	}
 
 	public void setMenuService(IMenuService menuService) {
 		this.menuService = menuService;
 	}
 
 	public int getIndex() {
 		return index;
 	}
 
 	public void setIndex(int index) {
 		this.index = index;
 	}
 	
 	
 	
 
 }
