 package br.com.am.action;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.struts2.convention.annotation.Action;
 import org.apache.struts2.convention.annotation.Result;
 
 import br.com.am.action.enuns.PaginaEnum;
 import br.com.am.bo.DespesaBO;
 import br.com.am.model.Despesa;
 import br.com.am.model.Processo;
 import br.com.am.model.SelectObject;
 import br.com.am.model.TipoDespesa;
 
 /**
  * Class Action LancarDespesas
  * @author JDGR
  * @since 18/09/2012
  */
 public class LancarDespesasAction extends GenericAction{
 	
 	private static final long serialVersionUID = 6688816828187072391L;
 	
 	private List<Processo> processos = new ArrayList<Processo>();
 	private Double valorTotalDespesas;
 	private Integer numeroProcesso;
 	private Integer codigoLancamento;
 	
 	private List<Despesa> despesas = new ArrayList<Despesa>();
 	private List<TipoDespesa> tiposDespesas = new ArrayList<TipoDespesa>();
 	private Despesa despesa;
 	
 	private String jSonTipoDespesa;
 	private String jSonValorDespesa;
 	private String jSonObservacaoDespesa;
 	
 	
 	/**
 	 * Action que direciona para as pginas da funcionalidade de lanar despesas.
 	 * @author JDGR
 	 * @return String
 	 * @since 18/09/2012
 	 */
 	@Action(value="forwardLancarDespesa", results={
 			@Result(location="/pages/despesa/lancarDespesa.jsp", name="lancar"),
 			@Result(location="/erro.jsp", name="erro")
 	})
 	public String forwardLancarDespesa(){
 		if(PaginaEnum.LANCAR_DESPESA.getDescricao().equals(paginaDirecionar)){
 			tiposDespesas = DespesaBO.consultarTiposDespesas();
 			return PaginaEnum.LANCAR_DESPESA.getDescricao();
 		} else {
 			return String.valueOf(PaginaEnum.ERRO.getDescricao());
 		}
 	}
 	
 	/**
 	 * Action que cadastra despesas.
 	 * @author JDGR
 	 * @return String
 	 * @since 18/09/2012
 	 */
 	@Action(value="cadastrarDespesa", results={
 			@Result(location="/pages/despesa/lancarDespesa.jsp", name="lancar"),
 			@Result(location="/erro.jsp", name="erro")
 	})
 	public String cadastrarDespesa(){
 		try {
 			DespesaBO.lancarDespesa(despesa);
 		} catch (Exception e) {
 			mensagem = e.getMessage();
 			e.printStackTrace();
 		}
 		return PaginaEnum.LANCAR_DESPESA.getDescricao();
 	}
 	
 	/**
 	 * Action que altera despesa.
 	 * @author JDGR
 	 * @return String
 	 * @since 18/09/2012
 	 */
 	@Action(value="alterarDespesa", results={
 			@Result(location="/pages/despesa/lancarDespesa.jsp", name="lancar"),
 			@Result(location="/erro.jsp", name="erro")
 	})
 	public String alterarDespesa(){
 		try {
 			DespesaBO.atualizarDespesa(despesa);
 		} catch (Exception e) {
 			mensagem = e.getMessage();
 			e.printStackTrace();
 		}
 		return PaginaEnum.LANCAR_DESPESA.getDescricao();
 	}
 	
 	/**
 	 * Action que excluir despesas.
 	 * @author JDGR
 	 * @return String
 	 * @since 18/09/2012
 	 */
 	@Action(value="excluirDespesa", results={
 			@Result(location="/pages/despesa/lancarDespesa.jsp", name="lancar"),
 			@Result(location="/erro.jsp", name="erro")
 	})
 	public String excluirDespesa(){
 		try {
 			DespesaBO.deletarDespesa(despesa.getCodigoLancamento());
 		} catch (Exception e) {
 			mensagem = e.getMessage();
 			e.printStackTrace();
 		}
 		return PaginaEnum.LANCAR_DESPESA.getDescricao();
 	}
 	
 	/**
 	 * Action que pesquisa o processo.
 	 * @author JDGR
 	 * @return String
 	 * @since 18/09/2012
 	 */
 	@Action(value="pesquisarProcessoDespesas", results={
 			@Result(location="/pages/despesa/lancarDespesa.jsp", name="lancar"),
 			@Result(location="/erro.jsp", name="erro")
 	})
 	public String pesquisarProcessoDespesas(){
 		try {
 			processos = new ArrayList<Processo>();
 			processos.add(DespesaBO.consultarProcesso(numeroProcesso));
 			despesas = DespesaBO.consultarDespesasPorProcesso(numeroProcesso);
 			valorTotalDespesas = DespesaBO.somarDespesaPorProcesso(numeroProcesso);
 			session.put("despesas", despesas);
 		} catch (Exception e) {
 			mensagem = e.getMessage();
 			e.printStackTrace();
 		}
 		return PaginaEnum.LANCAR_DESPESA.getDescricao();
 	}
 	
 	/**
 	 * Mtodo que localizar despesa selecionada.
 	 * @author JDGR
 	 * @return String
 	 * @since 18/09/2012
 	 */
 	@Action(value="localizarDespesa", results={
 			@Result(name="lancar", type="json", params={
 					"despesaLocalizada", "processos, " +
 					"valorTotalDespesas, numeroProcesso, despesas, " +
 					"tiposDespesas, despesa, codigoLancamento"
 			})
 	})
 	public String localizarDespesa(){
 		despesas = (List<Despesa>)session.get("despesas");
 		for(Despesa d: despesas){
 			if(d.getCodigoLancamento() == codigoLancamento){
 				despesa = d;
 				break;
 			}
 		}
 		jSonTipoDespesa = String.valueOf(despesa.getTipoDespesa().getCodigoDespesa());
 		jSonValorDespesa = String.valueOf(despesa.getValorDespesa());
 		jSonObservacaoDespesa = despesa.getObservacao();
 		return PaginaEnum.LANCAR_DESPESA.getDescricao();
 	}
 	
 	public Despesa getDespesa() {
 		return despesa;
 	}
 	public void setDespesa(Despesa despesa) {
 		this.despesa = despesa;
 	}
 
 	public List<Processo> getProcessos() {
 		return processos;
 	}
 
 	public void setProcessos(List<Processo> processos) {
 		this.processos = processos;
 	}
 
 	public List<TipoDespesa> getTiposDespesas() {
 		return tiposDespesas;
 	}
 
 	public void setTiposDespesas(List<TipoDespesa> tiposDespesas) {
 		this.tiposDespesas = tiposDespesas;
 	}
 
 	public Integer getNumeroProcesso() {
 		return numeroProcesso;
 	}
 
 	public void setNumeroProcesso(Integer numeroProcesso) {
 		this.numeroProcesso = numeroProcesso;
 	}
 
 	public Double getValorTotalDespesas() {
 		return valorTotalDespesas;
 	}
 
 	public void setValorTotalDespesas(Double valorTotalDespesas) {
 		this.valorTotalDespesas = valorTotalDespesas;
 	}
 
 	public String getjSonTipoDespesa() {
 		return jSonTipoDespesa;
 	}
 
 	public void setjSonTipoDespesa(String jSonTipoDespesa) {
 		this.jSonTipoDespesa = jSonTipoDespesa;
 	}
 
 	public String getjSonValorDespesa() {
 		return jSonValorDespesa;
 	}
 
 	public void setjSonValorDespesa(String jSonValorDespesa) {
 		this.jSonValorDespesa = jSonValorDespesa;
 	}
 
 	public String getjSonObservacaoDespesa() {
 		return jSonObservacaoDespesa;
 	}
 
 	public void setjSonObservacaoDespesa(String jSonObservacaoDespesa) {
 		this.jSonObservacaoDespesa = jSonObservacaoDespesa;
 	}
 
 	public List<Despesa> getDespesas() {
 		return despesas;
 	}
 
 	public void setDespesas(List<Despesa> despesas) {
 		this.despesas = despesas;
 	}
 
 	public Integer getCodigoLancamento() {
 		return codigoLancamento;
 	}
 
 	public void setCodigoLancamento(Integer codigoLancamento) {
 		this.codigoLancamento = codigoLancamento;
 	}
 	
 }
