 package br.gov.al.itec.sapim.controller;
 
 import static br.com.caelum.vraptor.view.Results.page;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import br.com.caelum.stella.validation.CPFValidator;
 import br.com.caelum.vraptor.Get;
 import br.com.caelum.vraptor.Path;
 import br.com.caelum.vraptor.Post;
 import br.com.caelum.vraptor.Resource;
 import br.com.caelum.vraptor.Result;
 import br.com.caelum.vraptor.Validator;
 import br.com.caelum.vraptor.validator.ValidationMessage;
 import br.gov.al.itec.sapim.dao.DepartamentoDao;
 import br.gov.al.itec.sapim.dao.OrgaoDao;
 import br.gov.al.itec.sapim.dao.PatrimonioDao;
 import br.gov.al.itec.sapim.dao.PerfilDao;
 import br.gov.al.itec.sapim.dao.RecursoDao;
 import br.gov.al.itec.sapim.dao.UnidadeDao;
 import br.gov.al.itec.sapim.dao.UsuarioDao;
 import br.gov.al.itec.sapim.dao.UsuarioDepartamentoDao;
 import br.gov.al.itec.sapim.model.Departamento;
 import br.gov.al.itec.sapim.model.Orgao;
 import br.gov.al.itec.sapim.model.Patrimonio;
 import br.gov.al.itec.sapim.model.Perfil;
 import br.gov.al.itec.sapim.model.Unidade;
 import br.gov.al.itec.sapim.model.Usuario;
 import br.gov.al.itec.sapim.model.UsuarioDepartamento;
 import br.gov.al.itec.sapim.util.HashUtil;
 
 @Resource
 public class UsuarioController {	
 	
 	private ObjectSession objectSession;
 	private Validator validator;
 	private Result result;
 	private UsuarioLogado usuarioLogado;
 	private OrgaoDao orgaoDao;
 	private UnidadeDao unidadeDao;
 	private PerfilDao perfilDao;
 	private PatrimonioDao patrimonioDao;
 	private DepartamentoDao departamentoDao;
 	private UsuarioDao dao;
 	private UsuarioDepartamentoDao usuarioDepartamentoDao;
 	
 	public UsuarioController(Validator validator, UsuarioDao dao,Result result, RecursoDao recursoDao,
 			OrgaoDao orgaoDao, UnidadeDao unidadeDao, UsuarioLogado usuarioLogado, PerfilDao perfilDao,
 			DepartamentoDao departamentoDao, ObjectSession objectSession, PatrimonioDao patrimonioDao,
 			UsuarioDepartamentoDao usuarioDepartamentoDao
 	) {
 		this.dao = dao;
 		this.validator = validator;
 		this.result = result;
 		this.usuarioLogado = usuarioLogado;
 		this.orgaoDao = orgaoDao;
 		this.unidadeDao = unidadeDao;
 		this.perfilDao = perfilDao;
 		this.departamentoDao = departamentoDao;
 		this.objectSession = objectSession;
 		this.patrimonioDao = patrimonioDao;
 		this.usuarioDepartamentoDao = usuarioDepartamentoDao;
 	}
 	
 	@Path("/")
 	public void index() {
 		result.redirectTo(UsuarioController.class).loginForm();
 	}
 	
 	public void loginForm() {
 		result.include("alert", "Agora somente os usuários catalogadores podem cadastrar/editar/remover os itens no catálogo.<br/>" +
         		" Se houver a necessidade de cadastrar novos itens, favor ligar para: 3315-5739.");
 	}
 	
 	public void incompativel() {
 		result.include("alert", "Navegador incompatível com o sistema.<br/> Baixe um navegador compatível.");
 	}
 	
 	public void login(Usuario authUser) {
 		authUser = authUser.login(dao.listAll());
 		
 		if(authUser == null)
 			validator.add(new ValidationMessage("Usuário ou senha inválidos", "erros"));
 		
 		validator.onErrorUse(page()).of(UsuarioController.class).loginForm();
 		
 		usuarioLogado.login(authUser);
 		
 		if(usuarioLogado.getUsuario().getUsuarioDepartamentos().size() == 1) {
 			usuarioLogado.getUsuario().setUsuarioDepartamentoAtivo(usuarioLogado.getUsuario().getUsuarioDepartamentos().get(0));
 			objectSession.setItensMenu(
 					usuarioLogado.getUsuario().getUsuarioDepartamentos().get(0).getPerfil().listarRecursosRaiz()
 			);
 			result.redirectTo(UsuarioController.class).inicio();
 		} else {
 			result.redirectTo(UsuarioController.class).escolherPerfilForm();
 		}
 		
 	}
 	
 	public void escolherPerfilForm() {	
 	}
 	
 	@Post
 	public void escolherPerfil(UsuarioDepartamento usuarioDepartamento) {
 		usuarioLogado.getUsuario().setUsuarioDepartamentoAtivo(usuarioDepartamentoDao.getById(usuarioDepartamento.getId()));
 		objectSession.setItensMenu(usuarioLogado.getUsuario().getUsuarioDepartamentoAtivo().getPerfil().listarRecursosRaiz());
 		result.redirectTo(UsuarioController.class).inicio();
 	}
 	
 	public void logout() {
 		usuarioLogado.logout();
 		result.redirectTo(UsuarioController.class).loginForm();
 	}
 	
 	public void inicio() {
 	}
 	
 	public Usuario form() {
 		//Adiciona a sessão o atributo de usuarioDepartamento.
 		objectSession.setUsuariosDepartamento(new ArrayList<UsuarioDepartamento>());
 
         result.include("orgaoList", orgaoDao.listAll());
         result.include("perfilList", perfilDao.listAll());
         
         return new Usuario();
 	}
 	
 	public List<Departamento> loadDepartamentos(Unidade unidade) {		
 	    return departamentoDao.listByUnidade(unidade);
 	}
 
 	@Get
 	@Path("/usuario/loadPatrimonios/{unidade.id}/{perfil.id}")
 	public List<Patrimonio> loadPatrimonios(Unidade unidade, Perfil perfil) {
 		if(perfil.getId().equals(Perfil.ADMINISTRADOR) || perfil.getId().equals(Perfil.PATRIMONIO))
 			return patrimonioDao.listByUnidade(unidade);
 		else
 			return null;
 	}
 
 	public List<Unidade> loadUnidades(Orgao orgao) {
     	List<Unidade> unidades;
     	
         if (!orgao.getId().equals(0L)) {
             unidades = unidadeDao.listByOrgao(orgao);
         } else {
             unidades = null;
         }
         return unidades;
     }
 
 	public List<Usuario> loadUsuarios(Unidade unidade){
 		List<Usuario> usuarioList = new ArrayList<Usuario>();
 		usuarioList = dao.buscar(null, unidade, false);
 		return usuarioList;
 	}
 	
     public List<Orgao> loadOrgaos() {
         return orgaoDao.listAll();
     }
     
     public List<UsuarioDepartamento> addUsuarioDepartamento(Perfil perfil, Departamento departamento, Patrimonio patrimonio) {
     	List<UsuarioDepartamento> usuariosDepartamento = objectSession.getUsuariosDepartamento();
     	
     	if(perfil.getId().equals(Perfil.ADMINISTRADOR) || perfil.getId().equals(Perfil.PATRIMONIO)) {
     		if(patrimonio.getId().equals(0L))
     			validator.add(new ValidationMessage("Almoxarifado não escolhido.", "error"));
         }
     	
     	if(patrimonio.getId() == null || patrimonio.getId() == 0){
     		validator.add(new ValidationMessage("Selecione um Patrimônio", "erros"));
     	}
     	if(departamento.getId() == null || departamento.getId() == 0){
     		validator.add(new ValidationMessage("Selecione um Departamento", "erros"));
     	}
     	
     	UsuarioDepartamento uD = new UsuarioDepartamento();
         uD.setPerfil(perfilDao.getById(perfil.getId()));
         uD.setPatrimonio(patrimonioDao.getById(patrimonio.getId()));
         uD.setDepartamento(departamentoDao.getById(departamento.getId()));
         
         if(usuariosDepartamento.contains(uD)) {
         	validator.add(new ValidationMessage("Departamento já escolhido", "erros"));
         }    	
     	
     	if(validator.hasErrors()) {
     		result.include("usuarioDepartamentoList", usuariosDepartamento);
     	}
     	
     	validator.onErrorUse(page()).of(UsuarioController.class).addUsuarioDepartamento(null, null, null);
        
         usuariosDepartamento.add(uD);
         return usuariosDepartamento;
     }
     
     public void remUsuarioDepartamento(Departamento departamento) {
     	List<UsuarioDepartamento> usuariosDepartamento = objectSession.getUsuariosDepartamento();
         int i = 0;
         for (UsuarioDepartamento uD : usuariosDepartamento) {
             if (uD.getDepartamento().getId().equals(departamento.getId())) {
                 break;
             }
             i++;
         }
 
         usuariosDepartamento.remove(i);
         
         result.include("usuarioDepartamentoList", usuariosDepartamento);
         result.forwardTo("/WEB-INF/jsp/usuario/addUsuarioDepartamento.jsp");
     }
     
     @Post
     @Path("/usuario")
     public void save(Usuario usuario) {
     	List<UsuarioDepartamento> usuariosDepartamento = objectSession.getUsuariosDepartamento();
     	
         if (usuario.getNome() == null || usuario.getNome().equals("")) {
         	validator.add(new ValidationMessage("Nome do usuário não preenchido.", "error"));
         }
         if (usuario.getCpf() == null || usuario.getCpf().equals("")) {
         	validator.add(new ValidationMessage("CPF não preenchido.", "error"));
         } else {
             try {
     			new CPFValidator().assertValid(usuario.getCpf());
     		} catch (Exception e) {
     			validator.add(new ValidationMessage("Formato de CPF inválido.", "error"));
     		}	
         }
         if (usuario.getFuncao() == null || usuario.getFuncao().equals("")) {
         	validator.add(new ValidationMessage("Função não preenchido.", "error"));
         }
         if (usuario.getEmail() == null || usuario.getEmail().equals("")) {
         	validator.add(new ValidationMessage("Email não preenchido.", "error"));
         }
         if (usuariosDepartamento.size() == 0) {
         	validator.add(new ValidationMessage("Nenhum departamento associado ao usuário.", "error"));
         }        
         if(usuario.getId() == null && usuario.getSenha().length() < 6) {
         	validator.add(new ValidationMessage("A senha deve conter no mínimo 6 caracteres.", "error"));
         }        
 
         //Verifica se já existe um usuário com o cpf escolhido.
         if (usuario.getCpf() != null && !usuario.getCpf().equals("")) {
             Usuario uExample = new Usuario();
             uExample.setCpf(usuario.getCpf());
 
             if (usuario.getId() != null) {
                 if (dao.findByExample(uExample).size() > 1) {
                     validator.add(new ValidationMessage("Já existe um usuário com o CPF escolhido.", "error"));
                 }
             } else if (dao.findByExample(uExample).size() > 0) {
             	validator.add(new ValidationMessage("Já existe um usuário com o CPF escolhido.", "error"));
             }
         }
         
         if(validator.hasErrors()) {     	
             result.include("orgaoList", orgaoDao.listAll());
             result.include("perfilList", perfilDao.listAll());
             result.include("patrimonioList", patrimonioDao.listAll());
             result.include("usuarioDepartamentoList", usuariosDepartamento);
             validator.onErrorUse(page()).of(UsuarioController.class).form();
         }     
         
     	usuario.setUsuarioDepartamentos(usuariosDepartamento);    	
     	usuario.setSenha(HashUtil.SHA(usuario.getSenha()));
 
     	if(usuario.getId() == null)
     		dao.save(usuario);
     	else {
        	if(usuario.getSenha() == null || usuario.getSenha().equals("da39a3ee5e6b4b0d3255bfef95601890afd80709"))
         		usuario.setSenha(dao.getById(usuario.getId()).getSenha());
     		
     		dao.update(usuario);
     	}
     	
     	objectSession.setUsuariosDepartamento(null);
     	
         result.include("message", "Dados do usuário foram salvos com sucesso.");
         result.redirectTo(UsuarioController.class).list();
     }
     
     @Get
     @Path("/usuario")
     public void list() {
    	result.include("usuarioList", dao.buscarUsuariosDesativados(false));
     }
     
     public void buscar(Usuario usuario) {
    	result.include("usuarioList", dao.buscarUsuariosDesativados(usuario));
     	result.forwardTo("/WEB-INF/jsp/usuario/list.jsp");
     }
     
     @Get
     @Path("/usuario/{usuario.id}")
     public void get(Usuario usuario) {
     	usuario = dao.getById(usuario.getId());
     	
 		//Adiciona a sessão o atributo de usuarioDepartamento.
 		objectSession.setUsuariosDepartamento(usuario.getUsuarioDepartamentos());
 		
 		List<Orgao> orgaos = new ArrayList<Orgao>();
 		Orgao orgao = null;
 		List<Unidade> unidades = null;
 		
 		if(usuarioLogado.getUsuario().getUsuarioDepartamentoAtivo().getPerfil().getId().equals(Perfil.ADMINISTRADOR)) {
 	            orgaos = orgaoDao.listAll();
 	    }else{
 	         orgao = usuarioLogado.getUsuario().getUsuarioDepartamentoAtivo().getDepartamento().getUnidade().getOrgao();
 	         orgaos.add(orgao);
 	         unidades = unidadeDao.listByOrgao(orgao);
 	    }
 		
         result.include("patrimonioList", patrimonioDao.listAll());
         result.include("perfilList", perfilDao.listAll());
         result.include("orgaoList", orgaoDao.listAll());
         result.include("unidadeList", unidades);
         result.include("usuarioDepartamentoList", usuario.getUsuarioDepartamentos());
         result.include("usuario", usuario);
         result.forwardTo("/WEB-INF/jsp/usuario/form.jsp");
     }
     
     //FIXME problema de cascateamento com UsuarioDepartamento.
     @Path("/usuario/remove/{usuario.id}")
     public void remove(Usuario usuario) {
     	dao.remove(usuario);
     	result.include("message", "Usuario removido com sucesso!");
     	result.redirectTo(UsuarioController.class).list();
     }
     
     public void formConsumidor() {
     	result.include("departamentoList", departamentoDao.listByUnidade(usuarioLogado.getUsuario().getUsuarioDepartamentoAtivo().getDepartamento().getUnidade()));
     }
     
     //FIXME falta validar o cpf.
     /*public void saveConsumidor(Usuario consumidor, Departamento departamento) {
         if (consumidor.getNome() == null || consumidor.getNome().equals("")) {
         	validator.add(new ValidationMessage("Nome do usuário não preenchido.", "error"));
         }
         if (consumidor.getCpf() == null || consumidor.getCpf().equals("")) {
         	validator.add(new ValidationMessage("CPF não preenchido.", "error"));
         }
         if (consumidor.getFuncao() == null || consumidor.getFuncao().equals("")) {
         	validator.add(new ValidationMessage("Função não preenchida.", "error"));
         }
         if (consumidor.getEmail() == null || consumidor.getEmail().equals("")) {
         	validator.add(new ValidationMessage("Email não preenchido.", "error"));
         }
         if(departamento.getId().equals(0L))
         	validator.add(new ValidationMessage("Departamento não escolhido", "error"));
         if(consumidor.getId() == null && consumidor.getSenha().length() < 6) {
         	validator.add(new ValidationMessage("A senha deve conter no mínimo 6 caracteres.", "error"));
         }
 
         //Verifica se já existe um usuário com o cpf escolhido.
         if (consumidor.getCpf() != null && !consumidor.getCpf().equals("")) {
             Usuario uExample = new Usuario();
             uExample.setCpf(consumidor.getCpf());
 
             if (consumidor.getId() != null) {
                 if (dao.findByExample(uExample).size() > 1) {
                     validator.add(new ValidationMessage("Já existe um usuário com o CPF escolhido.", "error"));
                 }
             } else if (dao.findByExample(uExample).size() > 0) {
             	validator.add(new ValidationMessage("Já existe um usuário com o CPF escolhido.", "error"));
             }
         }
         
         if(validator.hasErrors()) {        	
         	result.include("departamentoList", departamentoDao.listByUnidade(usuarioLogado.getUsuario().getUsuarioDepartamentoAtivo().getDepartamento().getUnidade()));
     		result.include("consumidor", consumidor);
         	validator.onErrorUse(page()).of(UsuarioController.class).formConsumidor();
         }    	
     	
         //Agrega o UsuarioDepartamento ao consumidor.
         Perfil perfil = new Perfil();
 //        perfil.setId(Perfil.CONSUMIDOR);        
         UsuarioDepartamento usuarioDepartamento = new UsuarioDepartamento();
         usuarioDepartamento.setDepartamento(departamento);
         usuarioDepartamento.setPerfil(perfil);
         Collection<UsuarioDepartamento> usuariosDepartamento = new ArrayList<UsuarioDepartamento>();
         usuariosDepartamento.add(usuarioDepartamento);         
         consumidor.setUsuarioDepartamentos(new ArrayList<UsuarioDepartamento>());
         
         dao.save(consumidor);
         result.include("message", "Consumidor salvo com sucesso!");
         result.include("departamentoList", departamentoDao.listByUnidade(usuarioLogado.getUsuario().getUsuarioDepartamentoAtivo().getDepartamento().getUnidade()));
         result.forwardTo("/WEB-INF/jsp/usuario/formConsumidor.jsp");
     }*/
     
     public Usuario formAlterarSenha() {
     	return usuarioLogado.getUsuario();
     }
     
     public void alterarSenha(Usuario usuario, String novaSenha, String confirmacaoNovaSenha) {
     	String senhaAnterior = usuario.getSenha();    	
         Usuario usuarioAnterior = dao.getById(usuario.getId());
         
         if(!HashUtil.SHA(senhaAnterior).equals(usuarioAnterior.getSenha()))
         	validator.add(new ValidationMessage("A senha atual e a anterior não conferem!", "error"));
         if(novaSenha.equals(""))
         	validator.add(new ValidationMessage("A valor do campo nova senha não foi preenchido", "error"));
         if(!novaSenha.equals(confirmacaoNovaSenha))
         	validator.add(new ValidationMessage("Os valores da nova senha e da confirmação da nova senha precisam ser iguais.", "error"));
         if(novaSenha.length() < 6)
         	validator.add(new ValidationMessage("A senha precisa ter no mínimo 6 caracteres", "error"));
         
         if(validator.hasErrors()) {
         	validator.onErrorUse(page()).of(UsuarioController.class).formAlterarSenha();
         }      
         
         usuarioAnterior.setSenha(HashUtil.SHA(novaSenha));
 
         result.include("message", "Senha alterada com sucesso.");
         result.forwardTo(this).inicio();
     }
 
 }
