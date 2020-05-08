 package br.com.sw2.gac.util;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Properties;
 import java.util.StringTokenizer;
 
 import br.com.sw2.gac.modelo.AplicaMedico;
 import br.com.sw2.gac.modelo.AplicaMedicoPK;
 import br.com.sw2.gac.modelo.CID;
 import br.com.sw2.gac.modelo.Cliente;
 import br.com.sw2.gac.modelo.ClienteDispositivo;
 import br.com.sw2.gac.modelo.ClienteDispositivoPK;
 import br.com.sw2.gac.modelo.Contato;
 import br.com.sw2.gac.modelo.Contrato;
 import br.com.sw2.gac.modelo.Dispositivo;
 import br.com.sw2.gac.modelo.FormaComunica;
 import br.com.sw2.gac.modelo.HistDispositivo;
 import br.com.sw2.gac.modelo.HistDispositivoPK;
 import br.com.sw2.gac.modelo.PacoteServico;
 import br.com.sw2.gac.modelo.Parametro;
 import br.com.sw2.gac.modelo.SMS;
 import br.com.sw2.gac.modelo.Script;
 import br.com.sw2.gac.modelo.TipoDoenca;
 import br.com.sw2.gac.modelo.Tratamento;
 import br.com.sw2.gac.modelo.Usuario;
 import br.com.sw2.gac.tools.TipoDispositivo;
 import br.com.sw2.gac.vo.ClienteVO;
 import br.com.sw2.gac.vo.ContatoVO;
 import br.com.sw2.gac.vo.ContratoVO;
 import br.com.sw2.gac.vo.DispositivoVO;
 import br.com.sw2.gac.vo.DoencaVO;
 import br.com.sw2.gac.vo.FormaContatoVO;
 import br.com.sw2.gac.vo.HistDispositivoVO;
 import br.com.sw2.gac.vo.PacoteServicoVO;
 import br.com.sw2.gac.vo.ParametroVO;
 import br.com.sw2.gac.vo.PerfilVO;
 import br.com.sw2.gac.vo.ScriptVO;
 import br.com.sw2.gac.vo.SmsVO;
 import br.com.sw2.gac.vo.TipoDoencaVO;
 import br.com.sw2.gac.vo.TratamentoVO;
 import br.com.sw2.gac.vo.UsuarioVO;
 
 /**
  * <b>Descrição: Classe para manipulação de objetos.</b> <br>
  * .
  * @author: SW2
  * @version 1.0 Copyright 2012 SmartAngel.
  */
 public final class ObjectUtils {
 
     /** Constante GET. */
     private static final String GET = "get";
 
     /**
      * Construtor Padrao Instancia um novo objeto ObjectUtils.
      */
     private ObjectUtils() {
         super();
     }
 
     /**
      * Realiza verificação sobre o argumento <i>object</i>, se este argumento for nulo retorna
      * Boolean.TRUE, caso contrário Boolean.FALSE.
      * @param object the object
      * @return Boolean
      * @see
      */
     public static Boolean isNull(Object object) {
         return (object == null);
     }
 
     /**
      * Realiza verificação sobre o argumento <i>object</i>, se este argumento n�o for nulo retorna
      * Boolean.TRUE, caso contr�rio Boolean.FALSE.
      * @param object the object
      * @return Boolean
      * @see
      */
     public static Boolean isNotNull(Object object) {
         return (object != null);
     }
 
     /**
      * Recupera o nome do método accessor Get.
      * @param field the field
      * @return java.lang.String Getter
      * @see
      */
     public static String getAccessorGetterName(Field field) {
         return (getAccessorName(field, GET));
     }
 
     /**
      * Recupera o nome do método accessor Get.
      * @param name the name
      * @return java.lang.String Getter
      * @see
      */
     public static String getAccessorGetterName(String name) {
         return (GET + String.valueOf(name.charAt(0)).toUpperCase() + name.substring(1));
     }
 
     /**
      * Recupera o nome do método conforme tipo do método acessor Get ou Set.
      * @param field the field
      * @param accessorType the accessor type
      * @return java.lang.String Getter
      * @see
      */
     private static String getAccessorName(Field field, String accessorType) {
         return (accessorType + String.valueOf(field.getName().charAt(0)).toUpperCase() + field
             .getName().substring(1));
     }
 
     /**
      * Realiza a invocação ao método <i>methodName</i> e objeto <i>object</i> informado.
      * @param methodName Nome do método.
      * @param object Objeto a qual o método ser� executado.
      * @param parameterTypes Tipo dos par�metros para os argumentos do método.
      * @param args Argumentos do método.
      * @return java.lang.Object Valor retornado quando executado o método.
      * @throws Exception the exception
      * @see
      */
     public static Object invokeMethod(String methodName, Object object, Class<?>[] parameterTypes,
         Object[] args) throws Exception {
         try {
             Method method = object.getClass().getMethod(methodName, parameterTypes);
             return (method.invoke(object, args));
         } catch (Exception exception) {
             throw (exception);
         }
     }
 
     /**
      * Realiza a invocação ao método <i>methodName</i> e objeto <i>object</i> informado. Este método
      * deve seguir a especificação Java para métodos getters.
      * @param field the field
      * @param object Objeto a qual o método ser� executado.
      * @return java.lang.Object Valor retornado quando executado o método.
      * @throws Exception the exception
      * @see
      */
     public static Object invokeGetterMethod(Field field, Object object) throws Exception {
         return (ObjectUtils.invokeMethod(ObjectUtils.getAccessorGetterName(field), object,
             new Class[0], new Object[0]));
     }
 
     /**
      * Recupera a representação do método setter do atributo informado pelo argumento <i>field</i>.
      * @param field the field
      * @param object the object
      * @return valor do atributo 'getterMethod'
      * @throws Exception the exception
      * @see
      */
     public static Method getGetterMethod(Field field, Object object) throws Exception {
         return (object.getClass().getDeclaredMethod(ObjectUtils.getAccessorGetterName(field),
             new Class<?>[0]));
     }
 
     /**
      * Verifica se o valor informado <i>value</i> é uma instancia do tipo informado pelo argumento
      * <i>classType</i>.
      * @param value the value
      * @param classType the class type
      * @return boolean
      * @see
      */
     public static Boolean instanceOf(Object value, Class<?> classType) {
         return ((!ObjectUtils.isNull(value)) && (classType.isInstance(value)));
     }
 
     /**
      * Recupera o valor da propriedade do objeto.
      * @param propertyName Nome da propriedade do objeto a ser recuperado o valor.
      * @param object Objeto a ser extraido o valor da propriedade.
      * @return valor do atributo 'propertyValue'
      * @throws Exception the exception
      * @see
      */
     public static Object getPropertyValue(String propertyName, Object object) throws Exception {
         Object parentObject = object;
         String token = null;
         Object propertyValue = null;
         for (StringTokenizer tokens = new StringTokenizer(propertyName, "."); tokens
             .hasMoreTokens();) {
             token = tokens.nextToken();
             try {
                 propertyValue = ObjectUtils.getPropertyValue(parentObject.getClass()
                     .getDeclaredField(token), parentObject);
             } catch (NoSuchFieldException exception) {
                 propertyValue = ObjectUtils.invokeMethod(ObjectUtils.getAccessorGetterName(token),
                     parentObject, new Class<?>[0], new Object[0]);
             }
             if (tokens.hasMoreTokens()) {
                 parentObject = propertyValue;
             }
         }
         return (propertyValue);
     }
 
     /**
      * Recupera o valor da propriedade do objeto.
      * @param property the property
      * @param object the object
      * @return valor do atributo 'propertyValue'
      * @throws Exception the exception
      * @see
      */
     public static Object getPropertyValue(Field property, Object object) throws Exception {
         Object value = null;
         try {
             value = property.get(object);
         } catch (Exception exception) {
             return (ObjectUtils.invokeGetterMethod(property, object));
         }
         return (value);
     }
 
     /**
      * Nome: getResourceAsProperties Recupera o valor do atributo 'resourceAsProperties'.
      * @param resourceName the resource name
      * @return valor do atributo 'resourceAsProperties'
      * @throws Exception the exception
      * @see
      */
     @SuppressWarnings("unchecked")
     public static Properties getResourceAsProperties(String resourceName) throws Exception {
         if (null == resourceName) {
             throw (new IllegalArgumentException("O valor informado pelo argumento "
                 + "[resourceName] Não pode ser nulo"));
         }
 
         InputStream inputStream = ClassLoaderUtils.getDefaultClassLoader().getResourceAsStream(
             resourceName);
 
         Properties properties = new Properties();
         BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
 
         String key;
         StringTokenizer map = null;
         String propertyKey = null;
         Object value = null;
         List<String> list = null;
 
         while ((key = in.readLine()) != null) {
             key = key.trim();
             if (("".equals(key)) || ("#".equals(String.valueOf(key.charAt(0))))) {
                 continue;
             }
             map = new StringTokenizer(key, "=");
             if (map.countTokens() == 2) {
                 propertyKey = map.nextToken().trim();
                 if (properties.containsKey(propertyKey)) {
                     Object propertyValue = properties.get(propertyKey);
                     if (!(propertyValue instanceof List)) {
                         list = new ArrayList<String>();
                         list.add(propertyValue.toString());
                         properties.remove(propertyKey);
                     } else {
                         list = (List<String>) propertyValue;
                     }
                     list.add(map.nextToken().trim());
                     value = list;
                 } else {
                     value = map.nextToken().trim();
                 }
                 properties.put(propertyKey, value);
             }
         }
         return (properties);
     }
 
     /**
      * Nome: parse Converte o objeto ScriptVO em uma entity Script.
      * @param vo the vo
      * @return script
      * @see
      */
     public static Script parse(ScriptVO vo) {
 
         Script entity = new Script();
 
         entity.setIdScript(vo.getIdScript());
         entity.setNmTitulo(vo.getTituloScript());
         entity.setDsDescricao(vo.getDescricaoScript());
         entity.setDsProcesso(vo.getProcessoSeguir());
         entity.setDtInicioValidade(vo.getDtInicioValidade());
         entity.setDtFinalValidade(vo.getDtFinalValidade());
 
         return entity;
     }
 
     /**
      * Nome: parse Converte uma entity Script em um objeto ScriptVO.
      * @param entity the entity
      * @return script vo
      * @see
      */
     public static ScriptVO parse(Script entity) {
 
         ScriptVO vo = new ScriptVO();
 
         vo.setIdScript(entity.getIdScript());
         vo.setTituloScript(entity.getNmTitulo());
         vo.setDescricaoScript(entity.getDsDescricao());
         vo.setProcessoSeguir(entity.getDsProcesso());
         vo.setDtInicioValidade(entity.getDtInicioValidade());
         vo.setDtFinalValidade(entity.getDtFinalValidade());
 
         return vo;
     }
 
     /**
      * Nome: parse Converte uma entity SMS em um objeto SmsVO.
      * @param entity the entity
      * @return sms vo
      * @see
      */
     public static SmsVO parse(SMS entity) {
 
         SmsVO vo = new SmsVO();
         vo.setIdSms(entity.getIdSMS());
         vo.setTitulo(entity.getTpMensagem());
         vo.setTexto(entity.getDsMensagem());
         vo.setDtInicioValidade(entity.getDtInicioValidade());
         vo.setDtTerminoValidade(entity.getDtTerminoValidade());
 
         return vo;
     }
 
     /**
      * Nome: parse Converte o objeto SmsVO em uma entity SMS.
      * @param vo the vo
      * @return sms
      * @see
      */
     public static SMS parse(SmsVO vo) {
 
         SMS entity = new SMS();
         entity.setTpMensagem(vo.getTitulo());
         entity.setDsMensagem(vo.getTexto());
         entity.setDtInicioValidade(vo.getDtInicioValidade());
         entity.setDtTerminoValidade(vo.getDtTerminoValidade());
 
         return entity;
 
     }
 
     /**
      * Nome: parse Converte o objeto UsuarioVO em uma entity Usuario.
      * @param vo the vo
      * @return usuario
      * @see
      */
     public static Usuario parse(UsuarioVO vo) {
         Usuario entity = null;
         if (null != vo) {
             String senhaCriptografada = null;
             if (null != vo.getSenha()) {
                 senhaCriptografada = StringUtil.encriptarTexto(vo.getSenha());
             }
             entity = new Usuario();
             entity.setSenha(senhaCriptografada);
             entity.setLogin(vo.getLogin());
             entity.setNmUsuario(vo.getLogin());
             if (null != vo.getPerfil() && null != vo.getPerfil().getIdPerfil()) {
                 entity.setCdPerfil(vo.getPerfil().getIdPerfil());
             }
         }
         return entity;
     }
 
     /**
      * Nome: parse Converte uma entity Usuario em um objeto UsuarioVO.
      * @param entity the entity
      * @return usuario vo
      * @see
      */
     public static UsuarioVO parse(Usuario entity) {
 
         UsuarioVO vo = new UsuarioVO();
         vo.setSenha(entity.getSenha());
         vo.setLogin(entity.getLogin());
         vo.setNomeUsuario(entity.getLogin());
         PerfilVO perfil = new PerfilVO();
         perfil.setIdPerfil(entity.getCdPerfil());
         vo.setPerfil(perfil);
 
         return vo;
     }
 
     /**
      * Nome: Converte DispositivoVO em Entity.
      * @param dispositivo vo
      * @return Dispositivo entity
      * @see
      */
     public static Dispositivo parse(DispositivoVO dispositivo) {
 
         Dispositivo entity = new Dispositivo();
         entity.setIdDispositivo(dispositivo.getIdDispositivo());
         entity.setTpEstado(dispositivo.getEstadoAtual());
         entity.setTpDispositivo(dispositivo.getTipoDispositivo());
         entity.setDtaEntrada(dispositivo.getDataEntrada());
         entity.setDtaFabrica(dispositivo.getDataFabricacao());
         entity.setDtaProximaManut(dispositivo.getDataProximaManutencao());
         entity.setDtaSucata(dispositivo.getDataSucata());
         entity.setLocal(dispositivo.getLocal());
 
         Usuario usuario = new Usuario();
         usuario.setLogin(dispositivo.getUsuario().getLogin());
         entity.setLogin(usuario);
 
         return entity;
     }
 
     /**
      * Nome: Converte Entity em DispositivoVO.
      * @param entity Dispositivo
      * @return DispositivoVO vo
      * @see
      */
     public static DispositivoVO parse(Dispositivo entity) {
 
         DispositivoVO dispositivo = new DispositivoVO();
         dispositivo.setIdDispositivo(entity.getIdDispositivo());
         dispositivo.setUsuario((UsuarioVO) ObjectUtils.parse(entity.getLogin()));
         dispositivo.setEstadoAtual(entity.getTpEstado());
         dispositivo.setTipoDispositivo(entity.getTpDispositivo());
         dispositivo.setDataEntrada(entity.getDtaEntrada());
         dispositivo.setDataFabricacao(entity.getDtaFabrica());
         dispositivo.setDataProximaManutencao(entity.getDtaProximaManut());
         dispositivo.setDataSucata(entity.getDtaSucata());
         dispositivo.setLocal(entity.getLocal());
 
         return dispositivo;
     }
 
     /**
      * Nome: Converte Entity em PacoteServicoVO.
      * @param entity PacoteServico
      * @return PacoteServicoVO vo
      * @see
      */
     public static PacoteServicoVO parse(PacoteServico entity) {
 
         PacoteServicoVO pacoteServico = new PacoteServicoVO();
         pacoteServico.setIdPacote(entity.getIdServico());
         pacoteServico.setDescricao(entity.getDsServico());
         pacoteServico.setPreco(entity.getPrcMensal());
         pacoteServico.setTitulo(entity.getDsTitulo());
         pacoteServico.setDataInicioValidade(entity.getDtInicioValidade());
         pacoteServico.setDataFinalValidade(entity.getDtFinalValidade());
 
         return pacoteServico;
     }
 
     /**
      * Nome: parse Parses the.
      * @param vo the vo
      * @return pacote servico
      * @see
      */
     public static PacoteServico parse(PacoteServicoVO vo) {
 
         PacoteServico entity = new PacoteServico();
         entity.setIdServico(vo.getIdPacote());
         entity.setDsTitulo(vo.getTitulo());
         entity.setDsServico(vo.getDescricao());
         entity.setPrcMensal(vo.getPreco());
         entity.setDtInicioValidade(vo.getDataInicioValidade());
         entity.setDtFinalValidade(vo.getDataFinalValidade());
 
         return entity;
     }
 
     /**
      * Nome: Converte Entity em ContratoVO.
      * @param entity Contrato
      * @return ContratoVO contrato
      * @see
      */
     public static ContratoVO parse(Contrato entity) {
 
         ContratoVO contrato = new ContratoVO();
         contrato.setNumeroContrato(entity.getNmContrato());
         contrato.setCpfContratante(entity.getNmCPFContratante());
         contrato.setDtFinalValidade(entity.getDtFinalValidade());
         contrato.setDtInicioValidade(entity.getDtInicioValidade());
         contrato.setDtSuspensao(entity.getDtSuspensao());
         contrato.setIdServico(entity.getIdServico().getIdServico());
         contrato.setRgContratante(entity.getNmRGContratante());
         contrato.setNomeContratante(entity.getNmNomeContratante());
         contrato.setUsuario(parse(entity.getLogin()));
         contrato.setDtProxAtual(contrato.getDtProxAtual());
 
         return contrato;
 
     }
 
     /**
      * Nome: Converte um vo de Contrato em uma entity.
      * @param vo ContratoVO
      * @return Contrato entity
      * @see
      */
     public static Contrato parse(ContratoVO vo) {
         Contrato entity = new Contrato();
         entity.setNmContrato(vo.getNumeroContrato());
         entity.setNmCPFContratante(vo.getCpfContratante());
         entity.setDtFinalValidade(vo.getDtFinalValidade());
         entity.setDtInicioValidade(vo.getDtInicioValidade());
         entity.setDtSuspensao(vo.getDtSuspensao());
         PacoteServico pacoteServico = new PacoteServico();
         pacoteServico.setIdServico(vo.getPacoteServico().getIdPacote());
         entity.setIdServico(pacoteServico);
         entity.setNmRGContratante(vo.getRgContratante());
         entity.setNmNomeContratante(vo.getNomeContratante());
         entity.setLogin(parse(vo.getUsuario()));
         entity.setDtProxAtual(vo.getDtProxAtual());
         if (null != vo.getCliente()) {
             // Apesar de estar mapeado como oneTomany o relacionamento no sistema sera oneToOne.
             entity.setClienteList(new ArrayList<Cliente>());
             entity.getClienteList().add(ObjectUtils.parse(vo.getCliente()));
         }
         return entity;
     }
 
     /**
      * Nome: parse Parses the.
      * @param entity the entity
      * @return doenca vo
      * @see
      */
     public static DoencaVO parse(TipoDoenca entity) {
         return null;
     }
 
     /**
      * Nome: Converte Entity em HistDispositivoVO.
      * @param histDispositivo vo
      * @return HistDispositivo entity
      * @see
      */
     public static HistDispositivo parse(HistDispositivoVO histDispositivo) {
 
         HistDispositivo entity = new HistDispositivo();
         entity.setCdEstadoAnterior(histDispositivo.getEstadoAnterior());
 
         HistDispositivoPK tblhistdispositivoPK = new HistDispositivoPK(
             histDispositivo.getDthrMudaEstado(), histDispositivo.getIdDispositivo());
         entity.setTblhistdispositivoPK(tblhistdispositivoPK);
 
         entity.setLogin(histDispositivo.getLogin());
 
         return entity;
     }
 
     /**
      * Nome: Converte HistDispositivoVO em Entity.
      * @param entity HistDispositivo
      * @return HistDispositivoVO vo
      * @see
      */
     public static HistDispositivoVO parse(HistDispositivo entity) {
 
         HistDispositivoVO histDispositivo = new HistDispositivoVO();
         histDispositivo.setEstadoAnterior(entity.getCdEstadoAnterior());
         Dispositivo dispositivo = entity.getDispositivo();
         histDispositivo.setDispositivo(ObjectUtils.parse(dispositivo));
         histDispositivo.setDthrMudaEstado(entity.getTblhistdispositivoPK().getDthrMudaEstado());
         histDispositivo.setIdDispositivo(dispositivo.getIdDispositivo());
         histDispositivo.setLogin(entity.getLogin());
 
         return histDispositivo;
     }
 
     /**
      * Nome: Converte um vo de ParametroVO em uma entity de Parametro Parses the.
      * @param vo the parametro
      * @return parametro
      * @see
      */
     public static Parametro parse(ParametroVO vo) {
 
         Parametro entity = new Parametro();
         entity.setIdParametro(vo.getIdParametro());
         entity.setDiasDados(vo.getDiasDados());
         entity.setDiasBemEstar(vo.getDiasBemEstar());
         entity.setToleraRotinaCliente(vo.getToleraRotinaCliente());
 
         return entity;
     }
 
     /**
      * Nome: parse Parses the.
      * @param entity the entity
      * @return parametro vo
      * @see
      */
     public static ParametroVO parse(Parametro entity) {
 
         ParametroVO vo = new ParametroVO();
         vo.setIdParametro(entity.getIdParametro());
         vo.setDiasDados(entity.getDiasDados());
         vo.setDiasBemEstar(entity.getDiasBemEstar());
         vo.setToleraRotinaCliente(entity.getToleraRotinaCliente());
 
         return vo;
     }
 
     /**
      * Nome: parse Converte um Vo de cliente em uma entity.
      * @param vo the vo
      * @return cliente
      * @see
      */
     public static Cliente parse(ClienteVO vo) {
 
         Cliente entity = new Cliente();
         entity.setNmCPFCliente(vo.getCpf());
         entity.setNrRG(vo.getRg());
         entity.setNmCliente(vo.getNome());
         entity.setDsEndereco(vo.getEndereco().getEndereco());
         entity.setDsBairro(vo.getEndereco().getBairro());
         entity.setDsCidade(vo.getEndereco().getCidade());
         entity.setDsCEP(vo.getEndereco().getCep());
         entity.setDsEstado(vo.getEndereco().getUf());
         entity.setDtNascimento(vo.getDataNascimento());
         entity.setTpSexo(Integer.parseInt(vo.getSexo()));
         entity.setNmNecessidadeEspecial(vo.getNecessidadeEspecial());
         entity.setNmPlanoSaude(vo.getPlanoSaude());
         entity.setDsCobertura(vo.getCobertura());
         entity.setLogin(parse(vo.getUsuario()));
 
         // Formas de contato com o cliente
         entity.setFormaComunicaList(parseToListFormaComunicacaoEntity(
             vo.getListaFormaContato(), entity));
 
         // Lista de contatos do cliente
         List<Contato> listaContatos = new ArrayList<Contato>();
         for (ContatoVO contatoVO : vo.getListaContatos()) {
             Contato contatoEntity = parse(contatoVO);
             contatoEntity.setLogin(entity.getLogin());
             contatoEntity.setNmCPFCliente(entity);
             contatoEntity.setFormaComunicaList(parseToListFormaComunicacaoEntity(
                 contatoVO.getListaFormaContato(), entity));
             listaContatos.add(contatoEntity);
         }
         entity.setContatoList(listaContatos);
 
         // Lsita de dispositivo do cliente
         List<ClienteDispositivo> listaClienteDispositivo = new ArrayList<ClienteDispositivo>();
         for (DispositivoVO item : vo.getListaDispositivos()) {
             ClienteDispositivo cd = new ClienteDispositivo();
             cd.setCliente(entity);
             ClienteDispositivoPK cdpk = new ClienteDispositivoPK();
             cdpk.setIdDispositivo(item.getIdDispositivo());
             cdpk.setNmCPFCliente(entity.getNmCPFCliente());
             cd.setClienteDispositivoPK(cdpk);
             Dispositivo dispEntity = new Dispositivo();
             dispEntity.setIdDispositivo(item.getIdDispositivo());
             cd.setDispositivo(dispEntity);
             listaClienteDispositivo.add(cd);
 
         }
         // Lista de centrais do cliente
         for (DispositivoVO item : vo.getListaCentrais()) {
             ClienteDispositivo cd = new ClienteDispositivo();
             cd.setCliente(entity);
             ClienteDispositivoPK cdpk = new ClienteDispositivoPK();
             cdpk.setIdDispositivo(item.getIdDispositivo());
             cdpk.setNmCPFCliente(entity.getNmCPFCliente());
             cd.setClienteDispositivoPK(cdpk);
             Dispositivo dispEntity = new Dispositivo();
             dispEntity.setTpDispositivo(TipoDispositivo.CentralEletronica.getValue());
             dispEntity.setIdDispositivo(item.getIdDispositivo());
             cd.setDispositivo(dispEntity);
             listaClienteDispositivo.add(cd);
         }
 
         // Lista de doenças
         List<CID> listaDoencasCliente = new ArrayList<CID>();
         for (DoencaVO item : vo.getListaDoencas()) {
             CID doencaEntity = new CID();
             doencaEntity.setCdCID(item.getCodigoCID());
             listaDoencasCliente.add(doencaEntity);
         }
 
         // Lista de tratamentos
         List<Tratamento> listaTratamento = new ArrayList<Tratamento>();
         if (!CollectionUtils.isEmptyOrNull(vo.getListaTratamentos())) {
             for (TratamentoVO item : vo.getListaTratamentos()) {
                 Tratamento tratamento = new Tratamento();
                 tratamento.setNomeTrata(item.getNomeTratamento());
                 tratamento.setDescrTrata(item.getDescricaoTratamento());
                 tratamento.setTpFrequencia(item.getFrequencia());
                 tratamento.setHoraInicial(item.getDataHoraInicial());
                 tratamento.setAplicaMedicoList(new ArrayList<AplicaMedico>());
                 tratamento.setIdTratamento(item.getIdTratamento());
                 tratamento.setCliente(entity);
                if (!CollectionUtils.isEmptyOrNull(item.getListaHorarios())) {
                     for (String horario : item.getListaHorarios()) {
                         Calendar calendar = DateUtil.stringToTime(horario);
                         AplicaMedico aplicaMedico = new AplicaMedico();
                         AplicaMedicoPK aplicaMedicopk = new AplicaMedicoPK();
                         aplicaMedicopk.setHrAplicacao(calendar.getTime());
                         aplicaMedicopk.setIdTratamento(tratamento.getIdTratamento());
                         aplicaMedicopk.setNmCPFCliente(entity.getNmCPFCliente());
                         aplicaMedicopk.setIdTratamento(item.getIdTratamento());
                         aplicaMedico.setAplicaMedicoPK(aplicaMedicopk);
                         // horario.setTratamento(tratamento);
                         tratamento.getAplicaMedicoList().add(aplicaMedico);
                     }
                 }
                 listaTratamento.add(tratamento);
             }
         }
         entity.setCIDList(listaDoencasCliente);
         entity.setClienteDispositivoList(listaClienteDispositivo);
         entity.setTratamentoList(listaTratamento);
 
         return entity;
     }
 
     /**
      * Nome: parseListFormaComunica Converte uma List de FormaComunicacaoVO para uma lista de
      * FormaComunica.
      * @param list the list
      * @param entity the entity
      * @return list
      * @see
      */
     private static List<FormaComunica> parseToListFormaComunicacaoEntity(List<FormaContatoVO> list,
         Cliente entity) {
         List<FormaComunica> listFormaComunica = new ArrayList<FormaComunica>();
         for (FormaContatoVO item : list) {
             FormaComunica formaComunica = ObjectUtils.parse(item);
             formaComunica.setNmCPFCliente(entity);
             listFormaComunica.add(formaComunica);
         }
         return listFormaComunica;
     }
 
     /**
      * Nome: parse Converte um VO de FormaContato em uma entity.
      * @param vo the vo
      * @return forma comunica
      * @see
      */
     public static FormaComunica parse(FormaContatoVO vo) {
         FormaComunica entity = new FormaComunica();
         if (!StringUtil.isVazio(vo.getTelefone(), true)) {
             entity.setFoneContato(vo.getTelefone().replace("-", "").replace("(", "")
                 .replace(")", ""));
         }
         entity.setIdFormaComunica(vo.getIdFormaContato());
         entity.setMailContato(vo.getEmail());
         entity.setTpContato(vo.getTipoContato());
 
         return entity;
 
     }
 
     /**
      * Nome: parse Converte uma entity CID em um objeto DoencaVO.
      * @param entity the entity
      * @return script vo
      * @see
      */
     public static DoencaVO parse(CID entity) {
 
         DoencaVO vo = new DoencaVO();
         vo.setCodigoCID(entity.getCdCID());
         vo.setNomeDoenca(entity.getNmDoenca());
         if (null != entity.getCdTipoDoenca()) {
             TipoDoencaVO tipoDoenca = new TipoDoencaVO();
             tipoDoenca.setCatFinal(entity.getCdTipoDoenca().getCatFinal());
             tipoDoenca.setCatInic(entity.getCdTipoDoenca().getCatInic());
             tipoDoenca.setCdTipoDoenca(entity.getCdTipoDoenca().getCdTipoDoenca());
             tipoDoenca.setDsTipoDoenca(entity.getCdTipoDoenca().getDsTipoDoenca());
             tipoDoenca.setNmCapitulo(entity.getCdTipoDoenca().getNmCapitulo());
             vo.setTipoDoenca(tipoDoenca);
         }
         return vo;
     }
 
     /**
      * Nome: parse parse Converte uma entity Contato em um objeto ContatoVO.
      * @param vo the vo
      * @return contato
      * @see
      */
     public static Contato parse(ContatoVO vo) {
         Contato entity = new Contato();
         entity.setNomeContato(vo.getNome());
         entity.setGrauParentesco(vo.getGrauParentesco());
         if (null != vo.getEndereco()) {
             entity.setEndContato(vo.getEndereco().getEndereco());
             entity.setBaiContato(vo.getEndereco().getBairro());
             entity.setCidContato(vo.getEndereco().getCidade());
             entity.setCepContato(vo.getEndereco().getCep());
         }
         if (vo.isContratante()) {
             entity.setContratante("1");
         } else {
             entity.setContratante("0");
         }
         entity.setGrauParentesco(vo.getGrauParentesco());
         entity.setSqaChamada(vo.getSqaChamada());
 
         if (!CollectionUtils.isEmptyOrNull(vo.getListaFormaContato())) {
             List<FormaComunica> listaFormaComunica = new ArrayList<FormaComunica>();
             for (FormaContatoVO item : vo.getListaFormaContato()) {
                 FormaComunica formaComunica = parse(item);
                 listaFormaComunica.add(formaComunica);
             }
         }
         return entity;
     }
 }
